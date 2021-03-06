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

import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.services.common.UsersOperationsService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.BotAnswer;
import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.LangId;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.BotAnswersService;
import com.intita.wschat.services.BotCategoryService;
import com.intita.wschat.services.BotItemContainerService;
import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.services.ChatLangService.ChatLangEnum;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.CourseService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;

import utils.RandomString;

@Controller
public class BotController {
	final int TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT = 15;
	@Autowired
	BotCategoryService botCategoryService;

	@Autowired
	BotItemContainerService botItemContainerService;

	@Autowired
	DTOMapper dtoMapper;


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
	@Autowired private ParticipantRepository participantRepository;
	@Autowired private ChatLangService chatLangService;

	@Autowired private RoomController roomControler;

	@Autowired
	@Lazy
	private UsersOperationsService usersOperationsService;

	private Timer timer;

	private ObjectMapper objectMapper = new ObjectMapper();
	private final static Logger log = LoggerFactory.getLogger(BotController.class);


	BotDialogItem currentContainer = null;
	BotCategory botCategory;

	@PostConstruct
	public void postConstructor(){

		ChatUser chatUser = new ChatUser("Mr.Bot", null);
		chatUser.setId((long)0);
		chatUsersService.updateChatUserInfo(chatUser);

		if (botCategoryService.getCount() > 0)
			return;

		botCategory = botCategoryService.add(new BotCategory("Main category"));
		botItemContainerService.generateTestSequnce(botCategory);
	}

	@RequestMapping(value = "bot_operations/sequences/{sequenceId}/{containerId}/nextContainer{choseIndex}", method = RequestMethod.GET)
	@ResponseBody
	public String getSequence(@PathVariable Long sequenceId,@PathVariable Long containerId, @PathVariable int choseIndex) throws JsonProcessingException {
		return objectMapper.writeValueAsString(botCategory);
	}
	@RequestMapping(value = "bot_operations/get_bot_dialog_item/{dialogItemId}", method = RequestMethod.GET)
	@ResponseBody
	public String getBotDialogItem(@PathVariable Long dialogItemId,  HttpServletRequest request) throws JsonProcessingException {
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
	public String getBotCategoryNamesHavingString(@PathVariable String categoryName,  HttpServletRequest request) throws JsonProcessingException {
		//TODO
		ArrayList<BotCategory> category = botCategoryService.getBotCategoriesHavingName(categoryName,TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT);
		return objectMapper.writeValueAsString(category);
	}
	@RequestMapping(value = "bot_operations/get_bot_dialog_items_descriptions_having_string_first5/{categoryId}/{description}", method = RequestMethod.GET)
	@ResponseBody
	public String getBotDialogItemNamesHavingString(@PathVariable Long categoryId,@PathVariable String description,  HttpServletRequest request) throws JsonProcessingException {
		//TODO
		return objectMapper.writeValueAsString(botItemContainerService.getBotDialogItemsHavingDescription(description,categoryId,TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT));
	}
	@RequestMapping(value = "bot_operations/get_bot_dialog_items_descriptions_having_string_first5/{categoryId}/", method = RequestMethod.GET)
	@ResponseBody
	public String getBotDialogItems(@PathVariable Long categoryId,  HttpServletRequest request) throws JsonProcessingException {
		//TODO
		return objectMapper.writeValueAsString(botItemContainerService.getBotDialogItems(categoryId,TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT));
	}

	@RequestMapping(value = "bot_operations/create_bot_dialog_item", method = RequestMethod.POST)
	@ResponseBody
	public String addBotDialogItem( HttpServletRequest request, HttpServletResponse response,@RequestBody BotDialogItem payload) throws JsonProcessingException {
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
	public boolean saveBotDialogItem(HttpServletRequest request,HttpServletResponse response,@RequestBody BotDialogItem payload) throws Exception {
		if(payload == null || payload.getCategory() == null || botCategoryService.getById(payload.getCategory().getId()) == null)
			throw (new Exception(objectMapper.writeValueAsString(payload) +  "	payload not valid"));

		botItemContainerService.update(payload);
		return true;
	}

	@RequestMapping(value = "bot_operations/{roomId}/submit_dialog_item/{containerId}/next_item/{nextContainerId}", method = RequestMethod.POST)
	@ResponseBody
	public String getSequence(@PathVariable Long roomId,@PathVariable Long containerId, @PathVariable Long nextContainerId, HttpServletRequest request, Authentication auth) throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
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
		ChatUser user = chatPrincipal.getChatUser();
		BotDialogItem item = botItemContainerService.getByObjectId(new LangId(containerId, chatLangService.getCurrentLang()));

		for(int i = 0; i < keys.size(); i++)
		{
			botAnswerService.add(new BotAnswer(keys.get(i), item,user, room, obj.get(keys.get(i))));
		}
		if(nextContainerId == -1)
			return "quize save";

		Map<String,Object> param = new HashMap<String, Object>();
		param.put("nextNode", nextContainerId.toString());//@BAD

		sendNextContainer(roomId, containerId, param, auth);

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
	public String sendNextContainer(@PathVariable("roomId") Long roomId, @PathVariable("containerId") Long containerId, @RequestBody Map<String,Object> param, Authentication auth) throws JsonProcessingException {
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
			nextContainer = botItemContainerService.getByObjectId(new LangId(nextNode, chatLangService.getCurrentLang()));	
		}
		else {
			nextContainer = botItemContainerService.getByObjectId(new LangId(nextNode, chatLangService.getCurrentLang()));	
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
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();


		UserMessage msg = new UserMessage(chatPrincipal.getChatUser(), room, "You answer: " + nextContainer.getIdObject().getId());
		usersOperationsService.filterMessageLP(room.getId(), dtoMapper.map(msg), auth);

		UserMessage qmsg = new UserMessage(bot, room, containerString);
		UserMessage messageToSave = new UserMessage(bot, room, containerStringToSave);

		usersOperationsService.filterMessageBot(room.getId(), dtoMapper.map(qmsg), messageToSave);
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
	public boolean giveTenantMapping(@PathVariable Long roomId) throws JsonProcessingException {
		return usersOperationsService.giveTenant(roomId);
	}


	/**********************
	 * TRAINER SYSTEM
	 *********************/
	@RequestMapping(value = "/bot_operations/tenant/answerToAddToRoom/{roomId}",  method = RequestMethod.POST)
	@ResponseBody String answerToAdd(HttpServletResponse response, @RequestParam(value="agree") boolean agree, @RequestParam(value="askId", required=false) String askId, @RequestParam(value="type", required=false) String type,  @PathVariable Long roomId, Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser user = chatPrincipal.getChatUser();
		if(!userService.isTenant(user.getId()))
			return "-1";//is not tenant
		
		response.setContentType("text/html");
		Room room = roomService.getRoom(roomId);
		if(room == null)
			return "-1";//roomNull
		if(agree)
		{
			if(askId != null)
			{
				if(type.equals("onlyFirst"))
				{
					if(askIdStackForTenants.contains(askId))
					{
						askIdStackForTenants.remove(askId);
					}
					else
					{
						return "У розмову вже вступив інший консультант, або до Вас потрапив не вірний запит.";//haha you must be faster//LANG
					}	
				}				
			}

			roomControler.addUserToRoom(user, room, auth, true);
			Object[] obj = new Object[] {roomId, user};
			usersOperationsService.addFieldToUserInfoMap(user, "newConsultationWithTenant", obj);
			tenantSubmitToSpendConsultationWS(room, user.getId());
		}
		return "1";//OK
	}

	@RequestMapping(value = "/bot_operations/triner/confirmToHelp/{roomId}",  method = RequestMethod.POST)
	public ResponseEntity<String> confirmToHelp(@PathVariable(value="roomId") Long roomId, Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser trainer = chatPrincipal.getChatUser();
		User iTrainer = chatPrincipal.getIntitaUser();
		if(userService.isTrainer(iTrainer.getId()))
		{
			Room room = roomService.getRoom(roomId);
			if(room == null)
				return new ResponseEntity<>("user is not trainer!!!", HttpStatus.BAD_REQUEST);
			roomControler.addUserToRoom(trainer, room, auth, true);
			//TODO
			//room.getPermissions().get(trainer).addPermission(trainer, RoomPermissions.Permission.ADD);
			//room.getPermissions().get(trainer).addPermission(trainer, RoomPermissions.Permission.REMOVE);
			roomService.update(room,true);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		else{
			return new ResponseEntity<>("user is not trainer!!!", HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value = "/bot_operations/tenant/{tenantId}/askToAddToRoom/{roomId}",  method = RequestMethod.POST)
	@ResponseBody
	public boolean askToAdd(@PathVariable(value="tenantId") Long tenantId, @PathVariable(value="roomId") Long roomId,Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser user = chatPrincipal.getChatUser();
		ChatUser tenant = chatUsersService.getChatUser(tenantId);
		if(tenant == null)
			return false;
		Room room = roomService.getRoom(roomId);
		if(room == null)
			return false;

		tenantSendBecomeBusy(tenant);
		usersOperationsService.askUser(tenant, user.getNickName() + " запрошує Вас у кімнату " + room.getName() + ".\n Чи згодні Ви?",
				"/bot_operations/tenant/answerToAddToRoom/" + roomId + "?agree=true", "/bot_operations/tenant/answerToAddToRoom/" + roomId + "?agree=false");
		return true;
	}

	public static class TenantAskModelJS{
		private String msg;
		private ArrayList<Long> tenantsIdList;
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		public ArrayList<Long> getTenantsIdList() {
			return tenantsIdList;
		}
		public void setTenantsIdList(ArrayList<Long> tenantsIdList) {
			this.tenantsIdList = tenantsIdList;
		}

		@JsonCreator
		public TenantAskModelJS(@JsonProperty("msg") String msg, @JsonProperty("tenantsIdList") String[] tenantsIdList)
		{
			this.msg = msg;
			this.tenantsIdList = new ArrayList<>();
			for(String tenantId : tenantsIdList)
			{
				this.tenantsIdList.add(Long.parseLong(tenantId));
			}

		}

	}

	private ArrayList<String> askIdStackForTenants = new ArrayList<>();

	@RequestMapping(value = "/bot_operations/tenants/askToAddToRoom/{roomId}",  method = RequestMethod.POST)
	@ResponseBody
	public boolean askToAddTenantsWithCustomMsg(@RequestBody TenantAskModelJS tenantAskModelJS, @PathVariable(value="roomId") Long roomId, Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser user = chatPrincipal.getChatUser();
		
		if(!userService.isTrainer(chatPrincipal.getIntitaUser().getId()))
			return false;//is not tenant
		
		ArrayList<ChatUser> tenants = chatUsersService.getUsers(tenantAskModelJS.getTenantsIdList());

		if(tenants == null)
			return false;
		Room room = roomService.getRoom(roomId);
		if(room == null)
			return false;

		RandomString randString = new RandomString(12);
		String askId = randString.nextString();
		askIdStackForTenants.add(askId);
		new java.util.Timer().schedule( 
				new java.util.TimerTask() {
					@Override
					public void run() {
						askIdStackForTenants.remove(askId);
					}
				}, 
				25000 
				);

		for(ChatUser tenant : tenants)
		{
			tenantSendBecomeBusy(tenant);
			usersOperationsService.askUser(tenant, user.getNickName() + ":\n" + tenantAskModelJS.getMsg(),
					"/bot_operations/tenant/answerToAddToRoom/" + roomId + "?type=onlyFirst&agree=true&askId=" + askId, "/bot_operations/tenant/answerToAddToRoom/" + roomId + "?type=onlyFirst&agree=false&askId=" + askId);
		}
		return true;
	}



	/**********************
	 * TENANT SYSTEM
	 *********************/
	@RequestMapping(value = "/bot_operations/tenant/becomeFree",  method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendBecomeFree(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		chatTenantService.setTenantFree(auth);
		usersOperationsService.groupCastAddTenantToList(chatPrincipal.getChatUser());
	}	

	@RequestMapping(value = "/bot_operations/tenant/becomeBusy",  method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendBecomeBusy(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser tenant = chatPrincipal.getChatUser();
		tenantSendBecomeBusy(tenant);
	}
	public void tenantSendBecomeBusy(ChatUser tenant) {
		chatTenantService.setTenantBusy(tenant);
		usersOperationsService.groupCastRemoveTenantFromList(tenant);
	}
	/*private void updateTenants(){
		String subscriptionStr = "/topic/chat.tenants.add";
		//ArrayList<LoginEvent> loginEvents = userService.getAllFreeTenantsLoginEvent(chatUser.getId());
		ArrayList<LoginEvent> loginEvents = userService.getAllFreeTenantsLoginEvent();
		simpMessagingTemplate.convertAndSend(subscriptionStr, loginEvents);
	}*/


	@RequestMapping(value = "/bot_operations/tenant/did_am_wait_tenant/{roomId}",  method = RequestMethod.POST)
	@ResponseBody
	public boolean isUserWaitTenant(@PathVariable Long roomId) {
		return usersOperationsService.isUserWaitTenant(roomId);
	}

	@RequestMapping(value = "/bot_operations/tenant/did_am_busy_tenant",  method = RequestMethod.POST)
	@ResponseBody
	public Object[]  isTenantBusy(Authentication auth) {
		//ChatUser user = chatUsersService.getChatUser(principal);
		ChatPrincipal chatPrincipal = (ChatPrincipal) auth.getPrincipal();
		Long chatUserId = chatPrincipal.getChatUser().getId();

		boolean isTenant = chatTenantService.isTenant(chatUserId);
		boolean isTenantBusy = chatTenantService.isTenantBusy(chatUserId);

		Object[] obj = new Object[] {isTenant, isTenantBusy};
		return obj;
	}

	@RequestMapping(value = "/{roomId}/bot_operations/tenant/refuse/",  method = RequestMethod.POST)
	@ResponseBody
	public boolean tenantSendRefused(@PathVariable("roomId") Long roomId) {

		Timer timer = usersOperationsService.getWaitConsultationUsersTimers().get(roomId);
		timer.cancel();
		timer.purge();
		Room room = roomService.getRoom(roomId);

		if (room == null)
			return false;

		usersOperationsService.getTempRoomAskTenant_wait().add(room);

		return usersOperationsService.giveTenant(roomId, true);
	}

	@RequestMapping(value = "/bot/operations/tenant/free/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendFree( @PathVariable Long roomId, Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal) auth.getPrincipal();
		Long tenantChatUserId = chatPrincipal.getChatUser().getId();

		Timer timer = usersOperationsService.getWaitConsultationUsersTimers().get(roomId);
		timer.cancel();
		timer.purge();

		ChatUser c_user = chatUsersService.getChatUser(tenantChatUserId);
		if (c_user == null)
			return;

		if (usersOperationsService.getTempRoomAskTenant().size() == 0)
			return;

		Room room_ = null;

		for (int i = 0; i < usersOperationsService.getTempRoomAskTenant().size(); i++)
			if (usersOperationsService.getTempRoomAskTenant().get(i).getId().equals(roomId))
			{
				room_ = usersOperationsService.getTempRoomAskTenant().get(i);
				usersOperationsService.getTempRoomAskTenant().remove(i);
				break;
			}

		if (room_ == null)
			return;


		for (int i = 0; i < usersOperationsService.getTempRoomAskTenant_wait().size(); i++)
			if (usersOperationsService.getTempRoomAskTenant_wait().get(i).getId().equals(roomId))
			{
				usersOperationsService.getTempRoomAskTenant_wait().remove(i);
				break;
			}

		room_.setActive(true);
		roomControler.changeAuthor(c_user, room_, true, auth, true);
		
		//	roomControler.addUserToRoom(c_user, room_, c_user.getPrincipal(), true);

		Object[] obj = new Object[] {roomId, tenantChatUserId};
		usersOperationsService.addFieldToUserInfoMap(c_user, "newConsultationWithTenant", obj);
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
		public static Authentication getBotAuthentication()
		{

			Principal principal = new Principal() {

				@Override
				public String getName() {
					return String.valueOf(BOT_ID);
				}
			};
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal,null);
			return auth;
		}
	}
}
