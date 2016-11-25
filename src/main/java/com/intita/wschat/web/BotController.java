package com.intita.wschat.web;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.models.BotAnswer;
import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.LangId;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomPermissions;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.BotAnswersService;
import com.intita.wschat.services.BotCategoryService;
import com.intita.wschat.services.BotItemContainerService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.CourseService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.web.ChatController.ChatLangEnum;

@Service
@Controller
public class BotController {
	final int TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT = 15;
	@Autowired
	BotCategoryService botCategoryService;

	@Autowired
	BotItemContainerService botItemContainerService;

	private List<Room> tempRoomAskTenant = new ArrayList<Room>();

	private List<Room> tempRoomAskTenant_wait = new ArrayList<Room>();

	private Map <Long, Timer> waitConsultationUsersTimers = new HashMap<Long, Timer>();

	public void register(Room room, Long userId)
	{
		tempRoomAskTenant.add(room);
		tempRoomAskTenant_wait.add(room);
	}

	private boolean  usersAskTenantsTimerRunning = false;

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private ChatTenantService chatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	@Autowired private ChatLangRepository chatLangRepository;
	@Autowired private ConsultationsService chatConsultationsService;
	@Autowired private CourseService courseService;
	@Autowired private BotAnswersService botAnswerService;


	@Autowired private RoomController roomControler;
	@Autowired private ChatController chatController;

	private Timer timer;

	private ObjectMapper objectMapper = new ObjectMapper();
	private final static Logger log = LoggerFactory.getLogger(BotController.class);

	@PostConstruct
	public void postConstructor(){

		ChatUser chatUser = new ChatUser("Mr.Bot", null);
		chatUser.setId((long)0);
		chatUsersService.updateChatUserInfo(chatUser);

		if (botCategoryService.getCount() > 0)
			return;

		botCategory = botCategoryService.add(new BotCategory("Main category"));
		generateTestSequnce(botCategory);
	}	

	BotDialogItem currentContainer = null;
	BotCategory botCategory;

	public String getJsonContainerBodySimple(String[] itemsText){
		String res = "{";
		for (int i = 0 ; i < itemsText.length;i++){
			if(i!=0)res += ",";
			res += String.format("item%d:{'data':'%s'}",i,itemsText[i]);
		}
		res+="}";
		return res;
	}
	public void generateTestSequnce(BotCategory botCategory){
		String[] container1 = {"Variant1,Variant2,Variant3,Variant4"};
		BotDialogItem testItemContainer1 = botItemContainerService.add(new BotDialogItem(getJsonContainerBodySimple(container1),botCategory,1L,"ua"));//begin
		BotDialogItem testItemContainer2 = botItemContainerService.add(new BotDialogItem(getJsonContainerBodySimple(container1),botCategory,2L,"ua"));
		BotDialogItem testItemContainer3 = botItemContainerService.add(new BotDialogItem(getJsonContainerBodySimple(container1),botCategory,3L,"ua"));
		BotDialogItem testItemContainer4 = botItemContainerService.add(new BotDialogItem(getJsonContainerBodySimple(container1),botCategory,4L,"ua"));//end
		botItemContainerService.update(testItemContainer1);
		botItemContainerService.update(testItemContainer2);
		botItemContainerService.update(testItemContainer3);
		botItemContainerService.update(testItemContainer4);
		botCategory.setMainElement(testItemContainer1);
		botCategoryService.update(botCategory);
		botItemContainerService.update(testItemContainer1);
	}

	@RequestMapping(value = "bot_operations/sequences/{sequenceId}/{containerId}/nextContainer{choseIndex}", method = RequestMethod.GET)
	@ResponseBody
	public String getSequence(@PathVariable Long sequenceId,@PathVariable Long containerId, @PathVariable int choseIndex) throws JsonProcessingException {
		return objectMapper.writeValueAsString(botCategory);
	}
	@RequestMapping(value = "bot_operations/get_bot_dialog_item/{dialogItemId}", method = RequestMethod.GET)
	@ResponseBody
	public String getBotDialogItem(@PathVariable Long dialogItemId,  HttpServletRequest request, Principal principal) throws JsonProcessingException {
		Map<String, BotDialogItem> array = new HashMap<>();
		for(String lang : ChatLangEnum.LANGS)
		{
			BotDialogItem dialogItemTemplate = botItemContainerService.getByIdAndLang(dialogItemId, lang);
			array.put(lang, dialogItemTemplate);
		}
		return objectMapper.writeValueAsString(array);
	}
	@RequestMapping(value = "bot_operations/get_bot_category_names_having_string_first5/{categoryName}", method = RequestMethod.GET)
	@ResponseBody
	public String getBotCategoryNamesHavingString(@PathVariable String categoryName,  HttpServletRequest request, Principal principal) throws JsonProcessingException {
		//TODO
		return objectMapper.writeValueAsString(botCategoryService.getBotCategoriesHavingName(categoryName,TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT));
	}
	@RequestMapping(value = "bot_operations/get_bot_dialog_items_descriptions_having_string_first5/{categoryId}/{description}", method = RequestMethod.GET)
	@ResponseBody
	public String getBotDialogItemNamesHavingString(@PathVariable Long categoryId,@PathVariable String description,  HttpServletRequest request, Principal principal) throws JsonProcessingException {
		//TODO
		return objectMapper.writeValueAsString(botItemContainerService.getBotDialogItemsHavingDescription(description,categoryId,TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT));
	}
	@RequestMapping(value = "bot_operations/get_bot_dialog_items_descriptions_having_string_first5/{categoryId}/", method = RequestMethod.GET)
	@ResponseBody
	public String getBotDialogItems(@PathVariable Long categoryId,  HttpServletRequest request, Principal principal) throws JsonProcessingException {
		//TODO
		return objectMapper.writeValueAsString(botItemContainerService.getBotDialogItems(categoryId,TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT));
	}

	@RequestMapping(value = "bot_operations/create_bot_dialog_item", method = RequestMethod.POST)
	@ResponseBody
	public String addBotDialogItem( HttpServletRequest request, HttpServletResponse response, Principal principal,@RequestBody BotDialogItem payload) throws JsonProcessingException {
		if(payload == null)
		{
			log.error("send params (object) faild!");//@LANG@
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return "Send params (object) faild!";
		}

		BotCategory category = payload.getCategory();
		if (category == null || category.getId() == null){
			log.error("Category is NULL !");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return "Category is NULL !";//@LANG@
		}

		Map<String, BotDialogItem> array = new HashMap<>();
		Long nextId = botItemContainerService.getNextId();

		for(String lang : ChatLangEnum.LANGS)
		{
			BotDialogItem dialogItemTemplate = new BotDialogItem(payload);
			dialogItemTemplate.setIdObject(new LangId(nextId, lang));
			botItemContainerService.add(dialogItemTemplate);
			array.put(lang, dialogItemTemplate);
		}
		return objectMapper.writeValueAsString(array);
	}

	@RequestMapping(value = "bot_operations/save_dialog_item", method = RequestMethod.POST)
	@ResponseBody
	public boolean saveBotDialogItem(HttpServletRequest request,HttpServletResponse response, Principal principal,@RequestBody BotDialogItem payload) throws Exception {
		if(payload == null || payload.getCategory() == null || botCategoryService.getById(payload.getCategory().getId()) == null)
			throw (new Exception(objectMapper.writeValueAsString(payload) +  "	payload not valid"));

		botItemContainerService.update(payload);
		return true;
	}

	@RequestMapping(value = "bot_operations/{roomId}/submit_dialog_item/{containerId}/next_item/{nextContainerId}", method = RequestMethod.POST)
	@ResponseBody
	public String getSequence(@PathVariable Long roomId,@PathVariable Long containerId, @PathVariable Long nextContainerId, HttpServletRequest request, Principal principal) throws JsonProcessingException {
		/*Iterator it = params.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        log.info("map submitted:");
	        log.info( (pair.getKey() + " = " + pair.getValue() ) );
	        it.remove(); // avoids a ConcurrentModificationException
	    }*/
		String jsonBody="";
		try {
			jsonBody = IOUtils.toString( request.getInputStream(),"UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("jsonbody:"+jsonBody);
		HashMap<String,String> obj =null;
		try {
			obj = new ObjectMapper().readValue(jsonBody,new TypeReference<Map<String, String>>(){});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> keys = new ArrayList<String>(obj.keySet());

		Room room = roomService.getRoom(roomId);
		ChatUser user = chatUsersService.getChatUser(principal);
		BotDialogItem item = botItemContainerService.getByObjectId(new LangId(containerId,ChatController.getCurrentLang()));

		for(int i = 0; i < keys.size(); i++)
		{
			botAnswerService.add(new BotAnswer(keys.get(i), item,user, room, obj.get(keys.get(i))));
		}
		if(nextContainerId == -1)
			return "quize save";

		Map<String,Object> param = new HashMap<String, Object>();
		param.put("nextNode", nextContainerId.toString());//@BAD

		sendNextContainer(roomId, containerId, param, principal);

		return "good";
	}

	private static Object cloneObject(Object obj){
		try{
			Object clone = obj.getClass().newInstance();
			for (Field field : obj.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				field.set(clone, field.get(obj));
			}
			return clone;
		}catch(Exception e){
			return null;
		}
	}

	@RequestMapping(value = "bot_operations/{roomId}/get_bot_container/{containerId}", method = RequestMethod.POST)
	@ResponseBody
	public String sendNextContainer(@PathVariable("roomId") Long roomId, @PathVariable("containerId") Long containerId, @RequestBody Map<String,Object> param, Principal principal) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();

		ChatUser bot = chatUsersService.getChatUser(BotParam.BOT_ID);

		Room room = roomService.getRoom(roomId);

		/*OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM_ON_LOGIN,true,""+room.getId());
		String subscriptionStr = "/topic/users/" + bot.getId() + "/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);*/
		boolean toMainContainer = param.get("category") == null;
		BotDialogItem nextContainer;
		if(!param.containsKey("nextNode"))
			return  "";

		BotDialogItem nextContainerToSave = null;
		Long nextNode = Long.parseLong((String) param.get("nextNode"));

		if(toMainContainer) {
			nextContainer = botItemContainerService.getByObjectId(new LangId(nextNode,ChatController.getCurrentLang()));	
		}
		else {
			nextContainer = botItemContainerService.getByObjectId(new LangId(nextNode,ChatController.getCurrentLang()));	
		}

		/*nextContainerToSave = new BotDialogItem(nextContainer);
		nextContainerToSave.setBody(nextContainer.getIdObject().getId().toString());
		 */
		String containerString = "";
		String containerStringToSave = "";
		try {
			containerString = objectMapper.writeValueAsString(nextContainer);
			containerStringToSave = objectMapper.writeValueAsString(nextContainer.getIdObject());

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		UserMessage msg = new UserMessage(chatUsersService.getChatUser(principal), room, "You answer: " + nextContainer.getIdObject().getId());
		chatController.filterMessageLP(room.getId(), new ChatMessage(msg), principal);

		UserMessage qmsg = new UserMessage(bot, room, containerString);
		UserMessage messageToSave = new UserMessage(bot, room, containerStringToSave);

		chatController.filterMessageBot(room.getId(), new ChatMessage(qmsg), messageToSave);
		return objectMapper.writeValueAsString(botCategory);
	}

	public void tenantSubmitToSpendConsultationWS(Room room, Long tenantChatUserId) {
		Long roomId = room.getId();

		Object[] obj = new Object[] {roomId, tenantChatUserId};

		Set<ChatUser> users = room.getUsers();

		for (ChatUser user : users) {
			Long chatUserId = user.getId();
			String subscriptionStr = "/topic/users/" + chatUserId + "/submitConsultation";
			simpMessagingTemplate.convertAndSend(subscriptionStr, obj);		}

		String subscriptionStr = "/topic/users/" + tenantChatUserId + "/submitConsultation";
		simpMessagingTemplate.convertAndSend(subscriptionStr, obj);
	}



	@RequestMapping(value = "/bot_operations/close/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public boolean giveTenant(@PathVariable Long roomId, Principal principal) throws JsonProcessingException {

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

	public boolean giveTenant(Long roomId, boolean needRunTimer) {
		Room room_0 = roomService.getRoom(roomId);
		return giveTenant(room_0, needRunTimer);
	}

	public void askUser(ChatUser chatUser, String msg, String yesLink, String noLink)
	{

		Map<String, Object> question = new HashMap<>();
		question.put("yesLink", yesLink);
		question.put("noLink", noLink);
		question.put("msg", msg);
		question.put("type", "ask");
		chatController.addFieldToUserInfoMap(chatUser, "newAsk_ToChatUserId", question);
		String subscriptionStr = "/topic/users/" + chatUser.getId() + "/info";
		simpMessagingTemplate.convertAndSend(subscriptionStr, question);
	}

	public boolean giveTenant(Room room_0, boolean needRunTimer) {

		//List<Long> ff = chatTenantService.getTenantsBusy();	
		if(room_0 == null)
			return false;	

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
		Long roomId = room_0.getId();

		Long tenantChatUserId = t_user.getChatUser().getId();	

		chatTenantService.setTenantBusy(t_user);

		Object[] obj = new Object[] {  tenantChatUserId, roomId };

		askUser(t_user.getChatUser(),"Ви згодні на консультацію?", String.format("/bot/operations/tenant/free/%1$d", roomId), String.format("/%1$d/bot_operations/tenant/refuse/", roomId));

		waitConsultationUser(room_0);

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

	/**********************
	 * TRAINER SYSTEM
	 *********************/
	@RequestMapping(value = "/bot_operations/tenant/answerToAddToRoom/{roomId}",  method = RequestMethod.POST)
	@ResponseBody boolean answerToAdd(@RequestParam(value="agree") boolean agree,@PathVariable Long roomId, Principal principal) {
		ChatUser user = chatUsersService.getChatUser(principal);
		Room room = roomService.getRoom(roomId);
		if(room == null)
			return false;
		if(agree)
		{
			roomControler.addUserToRoom(user, room, principal, true);
			Object[] obj = new Object[] {roomId, user};
			chatController.addFieldToUserInfoMap(user, "newConsultationWithTenant", obj);
			tenantSubmitToSpendConsultationWS(room, user.getId());
		}
		return true;
	}

	@RequestMapping(value = "/bot_operations/triner/confirmToHelp/{roomId}",  method = RequestMethod.POST)
	public ResponseEntity<String> confirmToHelp(@PathVariable(value="roomId") Long roomId, Principal principal) {
		ChatUser trainer = chatUsersService.getChatUser(principal);
		User iTrainer = trainer.getIntitaUser();
		if(userService.isTrainer(iTrainer.getId()))
		{
			Room room = roomService.getRoom(roomId);
			if(room == null)
				return new ResponseEntity<>("user is not trainer!!!", HttpStatus.BAD_REQUEST);
			roomControler.addUserToRoom(trainer, room, principal, true);
			//TODO
			//room.getPermissions().get(trainer).addPermission(trainer, RoomPermissions.Permission.ADD);
			//room.getPermissions().get(trainer).addPermission(trainer, RoomPermissions.Permission.REMOVE);
			roomService.update(room);
		}
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@RequestMapping(value = "/bot_operations/tenant/{tenantId}/askToAddToRoom/{roomId}",  method = RequestMethod.POST)
	@ResponseBody
	public boolean askToAdd(@PathVariable(value="tenantId") Long tenantId, @PathVariable(value="roomId") Long roomId, Principal principal) {
		ChatUser user = chatUsersService.getChatUser(principal);

		ChatUser tenant = chatUsersService.getChatUser(tenantId);
		if(tenant == null)
			return false;
		Room room = roomService.getRoom(roomId);
		if(room == null)
			return false;

		chatTenantService.setTenantBusy(tenant);
		askUser(tenant, user.getNickName() + " запрошує Вас приїднатися до співбесіди " + room.getName() + ".\n Ви погоджуєтеся?",
				"/bot_operations/tenant/answerToAddToRoom/" + roomId + "?agree=true", "/bot_operations/tenant/answerToAddToRoom/" + roomId + "?agree=false");
		return true;
	}

	/**********************
	 * TENANT SYSTEM
	 *********************/
	@RequestMapping(value = "/bot_operations/tenant/becomeFree",  method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendBecomeFree(Principal principal) {
		chatTenantService.setTenantFree(principal);
		chatController.groupCastAddTenantToList(chatUsersService.getChatUser(principal));
	}	

	@RequestMapping(value = "/bot_operations/tenant/becomeBusy",  method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendBecomeBusy(Principal principal) {
		chatTenantService.setTenantBusy(principal);
		chatController.groupCastRemoveTenantFromList(chatUsersService.getChatUser(principal));
	}
	/*private void updateTenants(){
		String subscriptionStr = "/topic/chat.tenants.add";
		//ArrayList<LoginEvent> loginEvents = userService.getAllFreeTenantsLoginEvent(chatUser.getId());
		ArrayList<LoginEvent> loginEvents = userService.getAllFreeTenantsLoginEvent();
		simpMessagingTemplate.convertAndSend(subscriptionStr, loginEvents);
	}*/


	@RequestMapping(value = "/bot_operations/tenant/did_am_wait_tenant/{roomId}",  method = RequestMethod.POST)
	@ResponseBody
	public boolean isUserWaitTenant(@PathVariable Long roomId,Principal principal) {	
		for (Room room : tempRoomAskTenant)
			if (room.getId().equals(roomId))
				return true;
		return false;
	}

	@RequestMapping(value = "/bot_operations/tenant/did_am_busy_tenant",  method = RequestMethod.POST)
	@ResponseBody
	public Object[]  isTenantBusy(Principal principal) {
		//ChatUser user = chatUsersService.getChatUser(principal);	
		Long vhatUserId = Long.parseLong(principal.getName());

		boolean isTenant = chatTenantService.isTenant(vhatUserId);
		boolean isTenantBusy = chatTenantService.isTenantBusy(vhatUserId);	

		Object[] obj = new Object[] {isTenant, isTenantBusy};
		return obj;
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
	@RequestMapping(value = "/{roomId}/bot_operations/tenant/refuse/",  method = RequestMethod.POST)
	@ResponseBody
	public boolean tenantSendRefused(@PathVariable("roomId") Long roomId, Principal principal) {	

		Timer timer = waitConsultationUsersTimers.get(roomId);
		timer.cancel();
		timer.purge();
		Room room = roomService.getRoom(roomId);

		if (room == null)
			return false;

		tempRoomAskTenant_wait.add(room);

		return giveTenant(roomId, true);
	}

	@RequestMapping(value = "/bot/operations/tenant/free/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendFree( @PathVariable Long roomId, Principal principal) {
		Long tenantChatUserId = Long.parseLong(principal.getName());

		Timer timer = waitConsultationUsersTimers.get(roomId);
		timer.cancel();
		timer.purge();

		ChatUser c_user = chatUsersService.getChatUser(tenantChatUserId);
		if (c_user == null)
			return;

		if (tempRoomAskTenant.size() == 0)
			return;

		Room room_ = null;

		for (int i = 0; i < tempRoomAskTenant.size(); i++)
			if (tempRoomAskTenant.get(i).getId().equals(roomId))
			{
				room_ = tempRoomAskTenant.get(i);
				tempRoomAskTenant.remove(i);
				break;
			}

		if (room_ == null)
			return;


		for (int i = 0; i < tempRoomAskTenant_wait.size(); i++)
			if (tempRoomAskTenant_wait.get(i).getId().equals(roomId))
			{
				tempRoomAskTenant_wait.remove(i);
				break;
			}

		roomControler.changeAuthor(c_user, room_, principal, true);
		//	roomControler.addUserToRoom(c_user, room_, c_user.getPrincipal(), true);

		Object[] obj = new Object[] {roomId, tenantChatUserId};
		chatController.addFieldToUserInfoMap(c_user, "newConsultationWithTenant", obj);
		tenantSubmitToSpendConsultationWS(room_, tenantChatUserId);
	}




	@RequestMapping(value = "bot_operations/get_all_categories_ids", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<Long> getAllCategoriesIds() throws JsonProcessingException {
		ArrayList<Long> categories = botCategoryService.getAllIds();
		return categories;

	}
	@RequestMapping(value = "bot_operations/get_five_categories_names_like/{categoryName}", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<String> get5CategoriesNamesLike(@PathVariable String categoryName) throws JsonProcessingException {
		ArrayList<String> categories = botCategoryService.getFirst5NamesLike(categoryName);
		return categories;

	}

	@RequestMapping(value = "bot_operations/get_categories_ids_where_names_like/{categoryName}", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<Long> getCategoriesIdsWhereNameLike(@PathVariable String categoryName) throws JsonProcessingException {
		ArrayList<Long> categories = botCategoryService.getIdsWhereNamesLike(categoryName);
		return categories;

	}
	///////
	@RequestMapping(value = "bot_operations/get_five_dialog_items_description_where_description_like/{categoryId}/{description}", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<String> get5DialogItemsDescriptionWhereDescriptionLike(@PathVariable Long categoryId,@PathVariable String description) throws JsonProcessingException {
		ArrayList<String> categories = null;
		if (categoryId==null)categories = botItemContainerService.getFirst5DescriptionsLike(description);
		else
			categories = botItemContainerService.getFirst5DescriptionsLike(description,categoryId);
		return categories;

	}

	@RequestMapping(value = "bot_operations/get_dialog_items_ids_where_description_like/{categoryId}/{description}", method = RequestMethod.GET)
	@ResponseBody
	public String getDialogItemsIdsWhereDescriptionLike(@PathVariable Long categoryId,@PathVariable String description) throws JsonProcessingException {
		ArrayList<Long> dialogItems = null;
		if (categoryId==null)
			dialogItems = botItemContainerService.getIdsWhereDescriptionsLike(description);
		else
			dialogItems = botItemContainerService.getIdsWhereDescriptionsLike(description,categoryId);
		return objectMapper.writeValueAsString(dialogItems);

	}
	/////////
	@RequestMapping(value = "bot_operations/get_all_dialog_items_ids", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<Long> getAllDialogItemsIds() throws JsonProcessingException {
		ArrayList<Long> dialogItems = botItemContainerService.getAllIds();
		return dialogItems;

	}

	@RequestMapping(value = "bot_operations/get_all_category", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<BotCategory> getAllCategory() {
		return botCategoryService.getAll();
	}

	@RequestMapping(value = "bot_operations/set_item/{item}/as_default/{category}", method = RequestMethod.GET)
	@ResponseBody
	public boolean setDialogItemAsDefault(@PathVariable("item") Long itemId, @PathVariable("category") Long categoryId) {
		BotCategory category = botCategoryService.getById(categoryId);
		if(category == null)
			return false;

		BotDialogItem item = botItemContainerService.getById(itemId);
		if(item == null)
			return false;

		category.setMainElement(item);
		botCategoryService.update(category);
		return true;
	}
	@RequestMapping(value = "bot_operations/create_category/{name}", method = RequestMethod.GET)
	@ResponseBody
	public boolean createCategory(@PathVariable("name") String categoryName) {
		BotCategory category = new BotCategory(categoryName);
		if(category == null)
			return false;

		botCategoryService.update(category);
		return true;
	}


	@RequestMapping(value = "bot_operations/get_dialog_items_ids/{categoryId}", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<Long> getDialogItemsIds(@PathVariable Long categoryId) throws JsonProcessingException {
		ArrayList<Long> dialogItems = botItemContainerService.getAllIdsFromCategory(categoryId);
		return dialogItems;

	}

	public static class BotParam{
		public static final long BOT_ID = 0;
		public static final String BOT_AVATAR = "noname.png";
		public static Principal getBotPrincipal()
		{
			return new Principal() {

				@Override
				public String getName() {
					return String.valueOf(BOT_ID);
				}
			};

		}
	}
}
