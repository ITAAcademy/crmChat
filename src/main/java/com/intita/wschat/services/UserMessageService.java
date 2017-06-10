package com.intita.wschat.services;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.LangId;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.UserMessageRepository;
import com.intita.wschat.web.BotController;
import com.intita.wschat.web.ChatController;

@Service
public class UserMessageService {

	@Autowired private UserMessageRepository userMessageRepository;
	@Autowired private ChatUsersService chatUserService;
	@Autowired private RoomsService roomsService;
	@Autowired private BotCategoryService botCategoryService;
	@Autowired private BotItemContainerService botItemContainerService;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private ChatUserLastRoomDateService chatLastRoomDateService;
	@Autowired private ChatLangService chatLangService;
	@Autowired private RoomHistoryService roomHistoryService;

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getUserMesagges(){
		return (ArrayList<UserMessage>) IteratorUtils.toList(userMessageRepository.findAll().iterator());
	}
	@Transactional(readOnly=true)
	public UserMessage getUserMessage(Long id){
		return userMessageRepository.findOne(id);
	}
	/*@Transactional(readOnly=true)
	public ArrayList<UserMessage> getChatUserMessagesByAuthor(String author) {

		return userMessageRepository.findByAuthor(chatUserService.getChatUser(author));
	}*/
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getChatUserMessagesById(Long id) {

		return wrapBotMessages(userMessageRepository.findByAuthor(chatUserService.getChatUser(id)));
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getUserMessagesByRoom(Room room) {
		return wrapBotMessages(userMessageRepository.findByRoom(room));
	}
	@Transactional(readOnly=false)
	public void removeAllUserMessagesByRoom(Room room) {
		userMessageRepository.delete(userMessageRepository.findByRoom(room));
	}
	@Transactional(readOnly=true)
	public UserMessage getLastUserMessageByRoom(Room room){
		return userMessageRepository.findFirstByRoomOrderByDateDesc(room);
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getFirst20UserMessagesByRoom(Room room) {
		return wrapBotMessages(userMessageRepository.findFirst20ByRoomOrderByIdDesc(room));
	}

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getFirst20UserMessagesByRoom(Room room, String lang,ChatUser user) {
		Date clearDate = roomHistoryService.getHistoryClearDate(room.getId(), user.getId());
		if (clearDate==null)
			return wrapBotMessages(userMessageRepository.findFirst20ByRoomOrderByIdDesc(room), lang);
		else
			return wrapBotMessages(userMessageRepository.findFirst20ByRoomAndDateAfterOrderByIdDesc(room, clearDate), lang);
	}

	public ArrayList<UserMessage> getUserMessagesByRoomId(Long roomId) {
		return wrapBotMessages(userMessageRepository.findByRoom(new Room(roomId)));
	}

	@Transactional()
	public boolean addMessage(ChatUser user, Room room,String body) {
		if(user == null || room == null || body == null) return false;
		//have premition?
		UserMessage userMessage = new UserMessage(user,room,body);
		userMessageRepository.save(userMessage);
		return true;
	}
	@Transactional()
	public boolean addMessage(UserMessage message) {
		if (message==null) return false;
		userMessageRepository.save(message);
		return true;
	}
	@Transactional()
	public boolean addMessages(Iterable<UserMessage> messages) {
		if (messages==null) return false;
		userMessageRepository.save(messages);
		return true;
	}

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByDate(Date date) {
		return userMessageRepository.findAllByDateAfter(date);
	}

	public static boolean isNumber(String str)  
	{  
		try  
		{  
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}


	@SuppressWarnings("deprecation")
	public boolean isValidJSON(final String json) {
		boolean valid = false;
		try {
			final JsonParser parser = new ObjectMapper().getJsonFactory()
					.createJsonParser(json);
			while (parser.nextToken() != null) {
			}
			valid = true;
		} catch (JsonParseException jpe) {
			// jpe.printStackTrace();
		} catch (IOException ioe) {
			// ioe.printStackTrace();
		}

		return valid;
	}
	public ArrayList<UserMessage> wrapBotMessages(ArrayList<UserMessage> input_messages) {
		String lang = chatLangService.getCurrentLang();
		return wrapBotMessages(input_messages, lang);
	}

	public ArrayList<UserMessage> wrapBotMessages(ArrayList<UserMessage> input_messages, String lang) {
		ArrayList<UserMessage> result = new ArrayList<>();
		for (UserMessage message : input_messages) {
			if (message.getAuthor().getId().equals(BotController.BotParam.BOT_ID) && isValidJSON(message.getBody()))
			{
				String originalBody = message.getBody();

				{
					ObjectMapper mapper = new ObjectMapper();
					JsonNode json = null;

					try {
						json = mapper.readTree(originalBody);
					} catch (JsonProcessingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					if (json != null) {
						JsonNode body_body =  json.get("id");
						if (body_body != null) {
							String body_json = body_body.asText();

							if (body_json != null) {
								if (isNumber(body_json))
								{
									Long id = new Integer(body_json).longValue();
									BotDialogItem botDialogItem = botItemContainerService.getByObjectId(new LangId(id, lang));
									if (botDialogItem != null) {
										/*String body = botDialogItem.getBody();
										if (!body.isEmpty()) {
											ObjectNode jsonNode = (ObjectNode)json;
											jsonNode.put("body", body); 
											String res_json_str = jsonNode.toString();
											message.setBody(res_json_str);
										}*/
										try {
											String res_json_str = mapper.writeValueAsString(botDialogItem);
											message.setBody(res_json_str);
										} catch (JsonProcessingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
			result.add(message);
		}
		return result;
	}

	public static boolean isStringJson(String jsonInString ) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(jsonInString);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Transactional
	public ArrayList<UserMessage> getMessages(Long roomId, Date beforeDate,Date afterDate, String body,boolean filesOnly,int messagesCount){
		if (roomId==null) return new ArrayList<UserMessage>();
		System.out.println("roomId:"+roomId);
		ArrayList<UserMessage> messages;

		String whereParam = "";

		// Construct WHERE part
		whereParam += "m.room.id = :roomId";
		if (beforeDate!=null) {
			if (whereParam.length()>0) whereParam += " AND ";
			whereParam +=  "m.date < :beforeDate";
		}
		if(afterDate != null) {
			if(whereParam.length() > 0) whereParam += " AND ";
			whereParam += "m.date >= :afterDate";
		}
		if (body!=null){
			if (whereParam.length()>0) whereParam += " AND ";
			whereParam += "lower(m.body) like lower(:body)";
		}
		if (filesOnly){
			if (whereParam.length()>0) whereParam += " AND ";
			whereParam += "m.attachedFilesJson is not null AND m.attachedFilesJson != :emptyJsonObject";
		}
		String wherePart = "WHERE "+ whereParam;
		String orderPart = " ORDER BY m.date DESC,m.id";


		String hql = "SELECT m FROM chat_user_message m " + " "+wherePart + orderPart;
		System.out.println("hql:"+hql);
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(hql);
		query.setMaxResults(messagesCount);

		//set params
		if(roomId!=null)
			query.setLong("roomId",roomId);
		if (beforeDate!=null)
			query.setTimestamp("beforeDate", beforeDate);
		if (afterDate!=null)
			query.setTimestamp("afterDate", afterDate);
		if (body!=null)
			query.setString("body", "%"+body+"%");
		if (filesOnly){
			query.setString("emptyJsonObject", "[]");
		}

		messages = new ArrayList<>(query.list());
		/*System.out.println("Before date:"+beforeDate);
		for (UserMessage m : messages){
			System.out.println("message:"+m.getBody() + " "+m.getDate());
		}*/
		return wrapBotMessages(messages);
	}

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByRoomDateNotUser(Room room, Date date, ChatUser user)  {
		ArrayList<UserMessage> messages =  userMessageRepository.findAllByDateAfter(date);
		return  wrapBotMessages(messages);
	}

	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByRoomDate(Room room, Date date)  {
		ArrayList<UserMessage> messages =  userMessageRepository.findAllByRoomAndDateAfter(room, date);
		return  wrapBotMessages(messages);
	}
	@Transactional(readOnly=true)
	public ArrayList<UserMessage> getMessagesByRoomDate(Room room, Date date, String lang)  {
		ArrayList<UserMessage> messages =  userMessageRepository.findAllByRoomAndDateAfter(room, date);
		return  wrapBotMessages(messages, lang);
	}

	@Transactional(readOnly=true)
	public Long getMessagesCountByRoomDateNotUser(Room room, Date date, ChatUser user)  {
		Long messages_count =  userMessageRepository.countByRoomAndDateAfterAndAuthorNot(room, date, user);
		return  messages_count;
	}

	@Transactional(readOnly=true)
	public List<UserMessage> getMessagesByDateNotUser(Date date, ChatUser user) {
		List<UserMessage> messages =  userMessageRepository.findAllByDateAfterAndAuthorNot( date, user);
		ArrayList<UserMessage> result =  wrapBotMessages( new ArrayList<UserMessage>(messages));
		return result;
	}

	@Transactional(readOnly=true)
	public Set<UserMessage> getMessagesByNotUser( ChatUser user) {
		Set<UserMessage> messages = userMessageRepository.findAllByAuthorNot( user); 
		ArrayList<UserMessage> result =  wrapBotMessages( new ArrayList<UserMessage>(messages));
		return new HashSet<UserMessage>(result);
	}

	@Transactional
	public Map<Room ,List<ChatMessage>> getAllUnreadedMessages(ChatUser user){
		List<ChatUserLastRoomDate> userRooms = chatLastRoomDateService.getUserLastRoomDates(user);
		Map<Room, List<ChatMessage>> result = new  HashMap<Room, List<ChatMessage>>();
		for (ChatUserLastRoomDate lastRoomEntry : userRooms){
			Room room = lastRoomEntry.getRoom();
			List<UserMessage> unreadedMessages = getMessagesByRoomDate(room, lastRoomEntry.getLastLogout(), "ua");
			List<ChatMessage> unreadedChatMessages = ChatMessage.getAllfromUserMessages(unreadedMessages);
			if(unreadedChatMessages.size()>0)
				result.put(room, unreadedChatMessages);
		}
		return result;
	}
	public Map<Room ,List<ChatMessage>> getAllUnreadedMessagesFrom24Hours(ChatUser user){
		List<ChatUserLastRoomDate> userRooms = chatLastRoomDateService.getUserLastRoomDates(user);
		Map<Room, List<ChatMessage>> result = new  HashMap<Room, List<ChatMessage>>();
		for (ChatUserLastRoomDate lastRoomEntry : userRooms){
			Room room = lastRoomEntry.getRoom();
			Date lastLogoutDate = lastRoomEntry.getLastLogout();
			Date currentDateMinus24Hours = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
			if (lastLogoutDate.before(currentDateMinus24Hours))
			{
				lastLogoutDate = currentDateMinus24Hours;
			}
			List<UserMessage> unreadedMessages = getMessagesByRoomDate(room, lastLogoutDate, "ua");
			List<ChatMessage> unreadedChatMessages = ChatMessage.getAllfromUserMessages(unreadedMessages);
			if(unreadedChatMessages.size()>0)
				result.put(room, unreadedChatMessages);
		}
		return result;
	}
	
	@Transactional
	public List<Date> getMessagesDatesByChatUserAndDate(Long chatUserId,Date early, Date late){
		return userMessageRepository.getMessagesDatesByChatUserAndDateBetween(chatUserId, early, late);
	}

	
	
}
