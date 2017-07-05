package com.intita.wschat.services.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.ChatRoomType;
import com.intita.wschat.domain.SubscribedtoRoomsUsersBufferModal;
import com.intita.wschat.domain.UserWaitingForTrainer;
import com.intita.wschat.domain.interfaces.IPresentOnForum;
import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.dto.model.UserMessageDTO;
import com.intita.wschat.dto.model.UserMessageWithLikesDTO;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.*;
import com.intita.wschat.services.*;
import com.intita.wschat.util.HtmlUtility;
import com.intita.wschat.web.BotController;
import com.intita.wschat.web.ChatController;
import com.intita.wschat.web.RoomController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by roma on 27.06.17.
 */
@Service
public class UsersOperationsService {

    private final static Logger log = LoggerFactory.getLogger(RoomController.class);
    @Autowired
    ChatUsersService chatUserService;
    @Autowired
    UsersService usersService;
    @Autowired
    RoomsService roomService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private ChatTenantService chatTenantService;
    @Autowired
    private BotCategoryService botCategoryService;
    @Autowired
    private UserMessageService userMessageService;

    private final int PARTICIPANTS_INITIAL_COUNT = 5;

    private List<UserWaitingForTrainer> usersRequiredTrainers = new ArrayList<>();// RoomId,ChatUserId

    private final Map<String, Queue<DeferredResult<String>>> responseBodyQueueForParticipents = new ConcurrentHashMap<String, Queue<DeferredResult<String>>>();// key

    private final Map<Long, ConcurrentLinkedQueue<DeferredResult<String>>> responseRoomBodyQueue = new ConcurrentHashMap<Long, ConcurrentLinkedQueue<DeferredResult<String>>>();// key

        public Map<String, Queue<DeferredResult<String>>> getResponseBodyQueueForParticipents() {
            return responseBodyQueueForParticipents;
        }
        public Map<Long, ConcurrentLinkedQueue<DeferredResult<String>>> getResponseRoomBodyQueue(){
            return responseRoomBodyQueue;
        }
        public List<UserWaitingForTrainer> getUsersRequiredTrainers(){
            return usersRequiredTrainers;
        }

    @Autowired
    DTOMapper dtoMapper;


    private List<Room> tempRoomAskTenant = new ArrayList<Room>();

    private List<Room> tempRoomAskTenant_wait = new ArrayList<Room>();

    private Map<Long, Timer> waitConsultationUsersTimers = new HashMap<Long, Timer>();

    private boolean  usersAskTenantsTimerRunning = false;

    private volatile Map<String, Queue<UserMessage>> messagesBuffer = Collections
            .synchronizedMap(new ConcurrentHashMap<String, Queue<UserMessage>>());// key

    private final ConcurrentHashMap<String, ArrayList<Object>> infoMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ArrayList<Object>>> infoMapForUser = new ConcurrentHashMap<>();


    private final Map<String, Queue<DeferredResult<String>>> responseBodyQueue = new ConcurrentHashMap<String, Queue<DeferredResult<String>>>();// key

    public List<Room> getTempRoomAskTenant_wait(){
        return tempRoomAskTenant_wait;
    }

    public List<Room> getTempRoomAskTenant(){
        return tempRoomAskTenant;
    }

    public Map<Long, Timer> getWaitConsultationUsersTimers(){
        return waitConsultationUsersTimers;
    }

    public void register(Room room, Long userId)
    {
        tempRoomAskTenant.add(room);
        tempRoomAskTenant_wait.add(room);
    }

    public ConcurrentHashMap<String, ArrayList<Object>> getInfoMap(){
        return infoMap;
    }
    public ConcurrentHashMap<Long, ConcurrentHashMap<String, ArrayList<Object>>> getInfoMapForUser(){
        return infoMapForUser;
    }

    public Map<String, Queue<DeferredResult<String>>> getResponseBodyQueu(){
        return responseBodyQueue;
    }


    public Map<String, Queue<UserMessage>> getMessagesBuffer() {
        return messagesBuffer;
    }

    public void filterMessageBot(Long room, UserMessageDTO messageDTO, UserMessage to_save) {
        if (to_save != null) {
            addMessageToBuffer(room, to_save, messageDTO);
            simpMessagingTemplate.convertAndSend(("/topic/" + room.toString() + "/chat.message"), messageDTO);
        }
    }

    @Scheduled(fixedDelay = 600L)
    public void processMessage() {
        ObjectMapper mapper = new ObjectMapper();
        for (String roomId : messagesBuffer.keySet()) {
            Queue<UserMessage> array = messagesBuffer.get(roomId);
            Queue<DeferredResult<String>> responseList = responseBodyQueue.get(roomId);

            if (responseList != null) {
                String str = "";
                try {
                    ArrayList<UserMessage> userMessages = (userMessageService.wrapBotMessages(new ArrayList<>(array), "ua"));
                    str = mapper.writeValueAsString(dtoMapper.mapListUserMessage(userMessages));// @BAG@//dont
                    // save
                    // user
                    // lang
                    // for
                    // bot
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                for (DeferredResult<String> response : responseList) {
                    if (!response.isSetOrExpired())
                        response.setResult(str);
                }
                responseList.clear();
            }
            messagesBuffer.remove(roomId);
        }
    }



    private static final Queue<SubscribedtoRoomsUsersBufferModal> subscribedtoRoomsUsersBuffer = new ConcurrentLinkedQueue<SubscribedtoRoomsUsersBufferModal>();// key


    public Room welcomeTenantToRoomWithGuest(Room room, ChatUser guest) {

        RoomController.UpdateRoomsPacketModal packetModal = getRoomsByAuthorSubscribe(guest, guest.getId());
        simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + guest.getId(),packetModal
        );
        // this said ti author that he nust update room`s list
        addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(guest));
        register(room, guest.getId());
        runUsersAskTenantsTimer(room);
        return room;
    }


    public void waitConsultationUser(Room room) {
        java.util.Timer timer = new java.util.Timer();
        Long roomId = room.getId();
        timer.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        tempRoomAskTenant.add(room);
                        tempRoomAskTenant_wait.add(room);
                        runUsersAskTenantsTimer(room);
                    }
                },
                30000
        );
        waitConsultationUsersTimers.put(roomId, timer);
    }

    public void runUsersAskTenantsTimer(Room room) {
        if (usersAskTenantsTimerRunning == false)
        {
            usersAskTenantsTimerRunning = true;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            usersAskTenantsTimerRunning = false;
                            if (tempRoomAskTenant_wait.size() > 0)
                            {
                                giveTenant(room, true);
                            }
                        }
                    },
                    10000
            );
        }
    }

    public static void addFieldToSubscribedtoRoomsUsersBuffer(SubscribedtoRoomsUsersBufferModal modal) {
        subscribedtoRoomsUsersBuffer.add(modal);
    }

    public RoomController.UpdateRoomsPacketModal getRoomsByAuthorSubscribeAuth(Authentication auth, @DestinationVariable Long userId) { // 000
        ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
        return getRoomsByAuthorSubscribe(chatPrincipal.getChatUser(),userId);
    }

    public RoomController.UpdateRoomsPacketModal getRoomsByAuthorSubscribe(ChatUser chatUser, Long userId) { // 000
        ChatUser user = chatUserService.getChatUser(userId);
        if (user == null || !chatUser.equals(user)) {
            ChatUser user_real = chatUser;
            if (chatUser.getIntitaUser() == null || !usersService.isAdmin(chatUser.getIntitaUser().getId()))
                return null;
        }

        return new RoomController.UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user));
    }


    public Room createDialogWithBot(String roomName, Authentication auth) {
        if (roomName.isEmpty())
            return null;

        ChatUser bot = chatUserService.getChatUser(BotController.BotParam.BOT_ID);
        ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();


        Room room = roomService.register(roomName, bot);
        ChatUser guest = chatPrincipal.getChatUser();
        roomService.addUserToRoom(guest, room);

        // send to user about room apearenced
        Long chatUserId = chatPrincipal.getChatUser().getId();
        simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,
                getRoomsByAuthorSubscribeAuth(auth, chatUserId));
        // this said ti author that he nust update room`s list
        addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(guest));

        String containerString = "Good day. Please choose the category that interests you:\n";
        ArrayList<BotCategory> allCategories = botCategoryService.getAll();
        BotDialogItem mainContainer = BotDialogItem.createFromCategories(allCategories);
        mainContainer.setBody(containerString + mainContainer.getBody());
        ObjectMapper mapper = new ObjectMapper();
        try {
            containerString = mapper.writeValueAsString(mainContainer);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        UserMessage msg = new UserMessage(bot, room, containerString);
        filterMessageWS(room.getId(), dtoMapper.map(msg), BotController.BotParam.getBotAuthentication());

        return room;
    }




    public boolean giveTenant(Long roomId) throws JsonProcessingException {

        Room room_0 = roomService.getRoom(roomId);
        if(room_0 == null)
            return false;

        if (tempRoomAskTenant.contains(room_0))
            return false;

        tempRoomAskTenant.add(room_0);

        boolean isFindedFreeTenant = giveTenant(roomId, true);

        return isFindedFreeTenant;
		/*
		ChatUser c_user = t_user.getChatUser();
		ChatUser b_user = chatUsersService.getChatUser(BotController.BotParam.BOT_ID);

		room_o.setAuthor(c_user);
		roomService.update(room_o);

		roomControler.addUserToRoom(b_user, room_o, b_user.getPrincipal(), true);

		return true;*/
    }


    public boolean giveTenant(Long roomId, boolean needRunTimer) {
        Room room_0 = roomService.getRoom(roomId);
        return giveTenant(room_0, needRunTimer);
    }

    public boolean giveTenant(Room room_0, boolean needRunTimer) {

        //List<Long> ff = chatTenantService.getTenantsBusy();
        if(room_0 == null)
            return false;

        Long roomId = room_0.getId();

        if(participantRepository.isOnline(room_0.getAuthor().getId()))
        {
            ChatTenant t_user = chatTenantService.getFreeTenantNotFromRoom(room_0);//       getRandomTenant();//choose method   789
            if (t_user == null)
            {
                if (tempRoomAskTenant_wait.contains(room_0) == false)
                {
                    tempRoomAskTenant_wait.add(room_0);							//789
                }
                runUsersAskTenantsTimer(room_0);
                return false;
            }


            Long tenantChatUserId = t_user.getChatUser().getId();

            chatTenantService.setTenantBusy(t_user);

            Object[] obj = new Object[] {  tenantChatUserId, roomId };

            askUser(t_user.getChatUser(),"Запит від неавторизізованого користувача?", String.format("/bot/operations/tenant/free/%1$d", roomId), String.format("/%1$d/bot_operations/tenant/refuse/", roomId));

            waitConsultationUser(room_0);

        }
        for (int i = 0; i < tempRoomAskTenant_wait.size(); i++)
        {
            if (tempRoomAskTenant_wait.get(i).getId().equals(roomId))
                tempRoomAskTenant_wait.remove(i);
        }

        if (tempRoomAskTenant_wait.size() > 0 && tempRoomAskTenant.size() > 0)
        {
			/*Long nextUserId = askConsultationUsers.get(0);
			ChatUser user =  chatUsersService.getChatUser(nextUserId);*/

            Room nextRoom = tempRoomAskTenant_wait.get(0);//      getRommInTempRoomAskTenantByChatUser(user);

            if (nextRoom != null)
            {
                if ( giveTenant(nextRoom, false) == false)
                    if (needRunTimer)
                        runUsersAskTenantsTimer(nextRoom);
            }
        }

        return true;
    }

    public void askUser(ChatUser chatUser, String msg, String yesLink, String noLink)
    {
        Map<String, Object> question = new HashMap<>();
        question.put("yesLink", yesLink);
        question.put("noLink", noLink);
        question.put("msg", msg);
        question.put("type", "ask");
        addFieldToUserInfoMap(chatUser, "newAsk_ToChatUserId", question);
        String subscriptionStr = "/topic/users/" + chatUser.getId() + "/info";
        simpMessagingTemplate.convertAndSend(subscriptionStr, question);
    }

    public void addFieldToUserInfoMap(ChatUser user, String key, Object value) {
        if (user != null && !key.isEmpty())
            addFieldToUserInfoMap(user.getId(), key, value);
    }

    public void addFieldToUserInfoMap(Long userId, String key, Object value) {
        ConcurrentHashMap<String, ArrayList<Object>> t_infoMap = infoMapForUser.get(userId);
        if (t_infoMap == null) {
            t_infoMap = new ConcurrentHashMap<>();
            infoMapForUser.put(userId, t_infoMap);
        }

        ArrayList<Object> listElm = t_infoMap.get(userId);
        if (listElm == null) {
            listElm = new ArrayList<>();
            t_infoMap.put(key, listElm);
        }
        listElm.add(value);
        // log.info(String.format("field '%s' with value '%s' added to infomap
        // for user '%s'", key,value,userId));
    }

    public Room createRoomWithTenant(Authentication auth) {

        // boolean botEnable =
        // Boolean.parseBoolean(configService.getParam("botEnable").getValue());
		/*
		 * ChatTenant greeTenante = chatTenantService.getFreeTenant(); if
		 * (greeTenante == null) return null;
		 *
		 * ChatUser roomAuthor = greeTenante.getChatUser();
		 *
		 * chatTenantService.setTenantBusy(greeTenante);
		 */
        // getRandomTenant().getChatUser();
        ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
        ChatUser guest = chatPrincipal.getChatUser();
        String roomName = " " + guest.getNickName().substring(0, 16) + " " + new Date().toString();
        Room room = roomService.register(roomName, guest);

        // roomService.addUserToRoom(guest, room);

        // send to user about room apearenced
        RoomController.UpdateRoomsPacketModal packetModal = getRoomsByAuthorSubscribeAuth(auth, chatPrincipal.getChatUser().getId());
        simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + guest.getId(),packetModal
        );
        // this said ti author that he nust update room`s list
        addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(guest));
        register(room, guest.getId());
        runUsersAskTenantsTimer(room);
        return room;
    }

    @Scheduled(fixedDelay = 2500L)
    public void processRoomsQueues() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        for (SubscribedtoRoomsUsersBufferModal modal : subscribedtoRoomsUsersBuffer) {
            if (modal == null || modal.getChatUser() == null) {
                System.out.println("WARNING: NULL USER");
                continue;
            }
            Queue<DeferredResult<String>> responseList = responseRoomBodyQueue.get(modal.getChatUser().getId());
            if (responseList == null) {
                // System.out.println("WARNING: RESPONSE LIST IS CLEAR");
                continue;
            }
            for (DeferredResult<String> response : responseList) {

                String str;
                if (modal.isReplace())
                    str = mapper.writeValueAsString(new RoomController.UpdateRoomsPacketModal(
                            roomService.getRoomsModelByChatUser(modal.getChatUser()), modal.isReplace()));
                else
                    str = mapper.writeValueAsString(new RoomController.UpdateRoomsPacketModal(
                            roomService.getRoomsByChatUserAndList(modal.getChatUser(), modal.getRoomsForUpdate(),null),
                            modal.isReplace()));

                if (!response.isSetOrExpired())
                    response.setResult(str);
            }
            responseRoomBodyQueue.remove(modal.getChatUser().getId());
            subscribedtoRoomsUsersBuffer.remove(modal);
        }
    }

    public Long addRoomByAuthorLP(String roomName,ArrayList<Long> userIds, Authentication auth) {
        ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
        boolean operationSuccess = true;
        Room room = null;
        ChatUser author = chatPrincipal.getChatUser();
        ArrayList<ChatUser> users = chatUserService.getUsers(userIds);
        if (userIds.size() == users.size())
            room = roomService.register(roomName, author, users);

        if (room == null)
            operationSuccess = false;
        else {
            // users.add(author);
            simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + author.getId(),
                    new RoomController.UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(author)));
            addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(author));

            for (ChatUser chatUser : users) {
                simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUser.getId(),
                        new RoomController.UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(chatUser)));
                addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(chatUser));
            }
        }
        // send to user about room apearenced
        OperationStatus operationStatus = new OperationStatus(OperationStatus.OperationType.ADD_ROOM, operationSuccess, "ADD ROOM");
        String subscriptionStr = "/topic/users/" + author.getId() + "/status";
        // send to user that operation success
        simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
        return room == null ? null : room.getId();
    }

    public boolean removeUserFromRoomFullyWithoutCheckAuthorization(ChatUser user_o, Room room_o) {
        // check for BOT
        if (user_o.getId() == BotController.BotParam.BOT_ID)
            return false;
        // check for private room
        if (room_o.getTypeEnum() == ChatRoomType.PRIVATE) {
            PrivateRoomInfo info = roomService.getPrivateRoomInfo(room_o);
            if (user_o == info.getFirtsUser() || user_o == info.getSecondUser())
                return false;
        }

        roomService.removeUserFromRoom(user_o, room_o);
        // chatUserLastRoomDateService.removeUserLastRoomDate(user_o, room_o);

        addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user_o));
        updateParticipants();// force update
        try {
            processRoomsQueues();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        simpMessagingTemplate.convertAndSend("/topic/" + room_o.getId().toString() + "/chat.participants",
                retrieveParticipantsMessage(room_o.getId(),PARTICIPANTS_INITIAL_COUNT));
        simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(),
                new RoomController.UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user_o)));
        return true;
    }

    public Set<LoginEvent> getParticipants(Room room_o,Long lastParticipantId, int count) {
        Set<LoginEvent> userList = new HashSet<>();
        Long intitaId = null;
        String avatar = "noname.png";
        String authorskype = "";
        User iUser = room_o.getAuthor().getIntitaUser();
        if (iUser != null) {
            intitaId = iUser.getId();
            avatar = iUser.getAvatar();
            authorskype = iUser.getSkype();
        }

        if (lastParticipantId == null) {
            LoginEvent authorLoginEvent = new LoginEvent(intitaId, room_o.getAuthor().getId(),
                    room_o.getAuthor().getNickName(), avatar, authorskype);// participantRepository.isOnline(room_o.getAuthor().getId().toString())
            userList.add(authorLoginEvent);
        }
        List<ChatUser> users = lastParticipantId == null ? roomService.getChatUsers(room_o,count) : roomService.getChatUsersAfterId(room_o,lastParticipantId,count);
        for (ChatUser user : users) {

            intitaId = null;
            avatar = "noname.png";
            String skype="";
            iUser = user.getIntitaUser();
            // Bot avatar
            if (user.getId() == BotController.BotParam.BOT_ID)
                avatar = BotController.BotParam.BOT_AVATAR;

            if (iUser != null) {
                intitaId = iUser.getId();
                avatar = iUser.getAvatar();
                skype = iUser.getSkype();
            }

            userList.add(new LoginEvent(intitaId, user.getId(), user.getNickName(), avatar,skype)); // participantRepository.isOnline(user.getId().toString())));
        }
        return userList;
    }

    public Map<String, Object> retrieveParticipantsMessage(Long room,int participantsCount) {
        Room room_o = roomService.getRoom(room);
        HashMap<String, Object> map = new HashMap();
        if (room_o != null)
            map.put("participants", getParticipants(room_o,null,participantsCount));
        return map;
    }

    @Scheduled(fixedDelay = 15000L)
    public void updateParticipants() {
        ObjectMapper mapper = new ObjectMapper();
        for (String key : responseBodyQueueForParticipents.keySet()) {
            Long longKey = 0L;
            boolean status = true;
            try {
                longKey = Long.parseLong(key);
            } catch (NumberFormatException e) {
                log.info("Participants update error:" + e.getMessage());
                status = false;
            }
            Room room_o = null;
            HashMap<String, Object> result = null;
            if (status) {
                room_o = roomService.getRoom(longKey);
                result = new HashMap();
            }
            if (room_o != null)
                result.put("participants", getParticipants(room_o,null,PARTICIPANTS_INITIAL_COUNT));

            for (DeferredResult<String> response : responseBodyQueueForParticipents.get(key)) {
                // response.setResult("");
                try {
                    if (!response.isSetOrExpired())
                        response.setResult(mapper.writeValueAsString(result));
                    else
                        response.setResult("{}");
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    response.setResult("");
                    e.printStackTrace();
                }

            }
            responseBodyQueueForParticipents.remove(key);
        }

    }

    public UserMessage filterMessageWithoutFakeObj(ChatUser chatUser, UserMessageDTO message, Room room) {
        if (!room.isActive() || (!HtmlUtility.isContentVisible(message.getBody()) && message.getAttachedFiles().isEmpty()))
            return null;

        UserMessage messageToSave = new UserMessage(chatUser, room, message);
        return messageToSave;
    }

    public UserMessageWithLikesDTO filterMessageWS(Long roomId, @Payload UserMessageDTO message,
                                          Authentication auth) {
        ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
        ChatController.CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(roomId, chatPrincipal,
                roomService);// Control room from LP
        if (struct == null)
            return null;

        ChatUser chatUser = chatPrincipal.getChatUser(); // chatUsersService.isMyRoom(roomStr,
        // principal.getName());
        if (chatUser == null)

            return null;

        Room o_room = struct.getRoom();
        UserMessage messageToSave = filterMessageWithoutFakeObj(chatUser, message, o_room);// filterMessage(roomStr,
        // message,
        // principal);
        OperationStatus operationStatus = new OperationStatus(OperationStatus.OperationType.SEND_MESSAGE_TO_ALL, true,
                "SENDING MESSAGE TO ALL USERS");
        String subscriptionStr = "/topic/users/" + chatUser.getId() + "/status";
        if (messageToSave != null) {
            ChatUser tenantIsWaitedByCurrentUser = roomService.isRoomHasStudentWaitingForTrainer(roomId,
                    chatUser);
            if (tenantIsWaitedByCurrentUser != null) {
                addUserRequiredTrainer(roomId, tenantIsWaitedByCurrentUser, chatUser, message.getBody());
            }
            UserMessageWithLikesDTO messageWithLikesDTO = addMessageToBuffer(roomId, messageToSave, message);

            simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
            return messageWithLikesDTO;
        }
        simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
        return null;
    }

    public synchronized UserMessageWithLikesDTO addMessageToBuffer(Long roomId, UserMessage message, UserMessageDTO messageDTO) {
        synchronized (messagesBuffer) {
            Queue<UserMessage> list = messagesBuffer.get(roomId.toString());
            if (list == null) {
                list = new ConcurrentLinkedQueue<>();
                messagesBuffer.put(roomId.toString(), list);
            }
            message = userMessageService.addMessage(message);
            list.add(message);
        }
        UserMessageWithLikesDTO dtoWithLikes = dtoMapper.map(messageDTO);
        dtoWithLikes.setId(message.getId());
        HashMap payload = new HashMap();
        payload.put(roomId, dtoWithLikes);
        Room chatRoom = roomService.getRoom(roomId);
        // send message to WS users
        for (ChatUser user : chatRoom.getUsers()) {
            simpMessagingTemplate.convertAndSend("/topic/" + user.getId() + "/must/get.room.num/chat.message", payload);
        }
        simpMessagingTemplate
                .convertAndSend("/topic/" + chatRoom.getAuthor().getId() + "/must/get.room.num/chat.message", payload);
        addFieldToInfoMap("newMessage", roomId);
        return dtoWithLikes;
    }

    public void addFieldToInfoMap(String key, Object value) {
        ArrayList<Object> listElm = infoMap.get(key);
        if (listElm == null) {
            listElm = new ArrayList<>();
            infoMap.put(key, listElm);
        }
        listElm.add(value);
    }

    public void addUserRequiredTrainer(Long roomId, ChatUser chatUserTrainer, ChatUser chatUser, String lastMessage) {

        LoginEvent studentLE = new LoginEvent(chatUser);
        UserWaitingForTrainer user = new UserWaitingForTrainer(roomId, lastMessage, studentLE);

        usersRequiredTrainers.add(user);

        simpMessagingTemplate.convertAndSend("/topic/chat/room.private/room_require_trainer.add", user);
        log.info("added room (" + roomId + ") required trainer " + chatUserTrainer.getId());
    }

    public boolean isUserWaitTenant(Long roomId) {
        for (Room room : tempRoomAskTenant)
            if (room.getId().equals(roomId))
                return true;
        return false;
    }
    public boolean isChatUserWaitTenant(ChatUser user) {
        for (Room room : tempRoomAskTenant_wait) {
            Set<ChatUser> users = room.getUsers();
            for (ChatUser a_user : users) {
                if (a_user.getId() == user.getId())
                    return true;
            }
        }
        return false;
    }

    public boolean tryAddTenantInListToTrainerLP(Long chatUserId) {
        ChatUser chatUser = chatUserService.getChatUser(chatUserId);
        return tryAddTenantInListToTrainerLP(chatUser);
    }

    public boolean tryAddTenantInListToTrainerLP(ChatUser chatUser) {
        User user = chatUser.getIntitaUser();
        if (user == null)
            return false;
        if (!chatTenantService.isTenant(user.getId())) {
            return false;
        }
        ArrayList<ChatUser> users = new ArrayList<ChatUser>();
        users.add(chatUser);
        propagateAdditionTenantsToList(users);
        return true;
    }

    public void propagateAdditionTenantsToList(ArrayList<ChatUser> usersIds) {
        ArrayList<ChatUser> chatUsers = chatUserService.getAllTrainers();
        if (usersIds.size() <= 0)
            return;

        for (ChatUser tenantUser : usersIds) {
            for (ChatUser trainerUser : chatUsers) {
                if (trainerUser == null)
                    continue;
                Long trainerChatId = trainerUser.getId();
                if (participantRepository.isOnline(trainerChatId)) {
                    ConcurrentHashMap<Long, IPresentOnForum> activeSessions = participantRepository.getActiveSessions();
                    if (activeSessions.get(trainerChatId).isTimeBased())
                        addFieldToUserInfoMap(trainerUser, "tenants.add", chatUserService.getLoginEvent(tenantUser));
                    else if (activeSessions.get(trainerChatId).isConnectionBased()) {
                        // Do nothing now, but may be usable in future
                    }
                }
            }
            groupCastAddTenantToList(tenantUser);
        }
    }

    public void groupCastAddTenantToList(ChatUser tenant) {
        String subscriptionStr = "/topic/chat.tenants.add";
        simpMessagingTemplate.convertAndSend(subscriptionStr, new LoginEvent(tenant));
    }


    public void groupCastRemoveTenantFromList(ChatUser tenant) {
        String subscriptionStr = "/topic/chat.tenants.remove";
        simpMessagingTemplate.convertAndSend(subscriptionStr, new LoginEvent(tenant));
    }

    public void propagateRemovingTenantFromListToTrainer(ChatUser user) {
        if (user == null)
            return;
        ArrayList<ChatUser> users = new ArrayList<ChatUser>();
        users.add(user);
        propagateRemovingTenantsFromList(users);
    }

    public void propagateRemovingTenantsFromList(ArrayList<ChatUser> usersIds) {
        ArrayList<ChatUser> chatUsers = chatUserService.getAllTrainers();
        if (usersIds.size() <= 0)
            return;
        ConcurrentHashMap<Long, IPresentOnForum> activeSessions = participantRepository.getActiveSessions();
        for (ChatUser tenantUser : usersIds) {
            for (ChatUser trainerUser : chatUsers) {
                if (trainerUser == null || !activeSessions.containsKey(trainerUser.getId().toString()))
                    continue;
                Long trainerChatId = trainerUser.getId();
                if (!participantRepository.isOnline(trainerChatId)) {
                    if (activeSessions.get(trainerChatId).isTimeBased())
                        addFieldToUserInfoMap(trainerUser, "tenants.remove",
                                chatUserService.getLoginEvent(tenantUser));
                    else if (activeSessions.get(trainerChatId).isConnectionBased()) {
                        // Do nothing now, but may be usable in future
                    }
                }
            }
            groupCastRemoveTenantFromList(tenantUser);
        }
    }

    public void tryRemoveChatUserRequiredTrainer(ChatUser chatUser) {
        UserWaitingForTrainer userWaiting = null;
        for (UserWaitingForTrainer user : getUsersRequiredTrainers()) {
            if (Long.compare(user.getChatUserId(), chatUser.getId()) == 0) {
                userWaiting = user;
                break;
            }
        }
        if (userWaiting != null) {
            getUsersRequiredTrainers().remove(userWaiting);
            String subscriptionStr = "/topic/chat/room.private/room_require_trainer.remove";
            simpMessagingTemplate.convertAndSend(subscriptionStr, userWaiting.getChatUserId());
        }
    }

    public void filterMessageLP( Long roomId, UserMessageDTO messageDTO,
                                Authentication auth) {
        // checkProfanityAndSanitize(message);//@NEED WEBSOCKET@
        ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
        UserMessage messageToSave = filterMessage(roomId, messageDTO, auth);
        ChatUser chatUser = chatPrincipal.getChatUser();
        if (messageToSave != null) {
            ChatUser trainerIsWaitedByCurrentUser = roomService.isRoomHasStudentWaitingForTrainer(roomId,
                    chatUser);
            if (trainerIsWaitedByCurrentUser != null) {
                addUserRequiredTrainer(roomId, trainerIsWaitedByCurrentUser, chatUser, messageDTO.getBody());
            }
            addMessageToBuffer(roomId, messageToSave, messageDTO);
            simpMessagingTemplate.convertAndSend(("/topic/" + roomId.toString() + "/chat.message"), messageDTO);
        }
    }

    public UserMessage filterMessage(Long roomStr, UserMessageDTO messageDTO, Authentication auth) {
        ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
        ChatController.CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(roomStr, chatPrincipal,
                roomService);// Control room from LP
        if (struct == null || !struct.getRoom().isActive() || messageDTO.getBody().trim().isEmpty())// cant
            // add
            // msg
            return null;

        UserMessage messageToSave = new UserMessage(struct.getUser(), struct.getRoom(), messageDTO);
        return messageToSave;
    }

    public void updateRoomByUser(ChatUser user, Room room,boolean notify) {
        ArrayList<Room> roomForUpdate = new ArrayList<>();
        roomForUpdate.add(room);
        if( notify && participantRepository.isOnline( user.getId() ) )
            sendMessageForUpdateRoomsByUser(user, roomForUpdate);
    }

    public void updateRoomsByUser(ChatUser user, HashMap<String, Object> roomsId) {
        ArrayList<Room> roomForUpdate = new ArrayList<>();

        // HashMap<String, Object> roomsId = (HashMap<String, Object>)
        // params.get("roomForUpdate");
        Set<String> set = roomsId.keySet();

        if (roomsId.size() > 0) {
            for (String string : set) {
                try {
                    roomForUpdate.add(new Room(Long.parseLong(string)));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        sendMessageForUpdateRoomsByUser(user, roomForUpdate);
    }

    public void sendMessageForUpdateRoomsByUser(ChatUser user, ArrayList<Room> roomForUpdate) {
        addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user, roomForUpdate));
        simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user.getId(),
                new RoomController.UpdateRoomsPacketModal(
                        roomService.getRoomsByChatUserAndList(user, roomForUpdate,null), false));
    }



}
