package com.intita.wschat.web;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.event.ActionListener;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	final int TYPEAHEAD_DISPLAYED_CATEGORIES_LIMIT = 5;
	@Autowired
	BotCategoryService botCategoryService;

	@Autowired
	BotItemContainerService botItemContainerService;

	private List<Room> tempRoomAskTenant = new ArrayList<Room>();
	private List<Long> askConsultationUsers = new ArrayList<Long>();

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
	
	@Value("${times.tenantRefuseConsultationTakeFreeTime}")
	 int tenantFreeTime;


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
		BotDialogItem item = botItemContainerService.getByObjectId(new LangId(containerId,ChatController.getCurrentLang()));

		for(int i = 0; i < keys.size(); i++)
		{
			botAnswerService.add(new BotAnswer(keys.get(i), item, room, obj.get(keys.get(i))));
		}
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
	
	public void askTenantToSpendConsultationWS(Long chatUserId, Long roomId) {
		Object[] obj = new Object[] {chatUserId, roomId};
		
		String subscriptionStr = "/topic/users/" + chatUserId + "/info";
		simpMessagingTemplate.convertAndSend(subscriptionStr, obj);
	}	
	
	public void tenantSubmitToSpendConsultationWS(Long chatUserId, Long tenantChatUserId) {
		Object[] obj = new Object[] {chatUserId, tenantChatUserId};
		
		String subscriptionStr = "/topic/users/" + chatUserId + "/submitConsultation";
		simpMessagingTemplate.convertAndSend(subscriptionStr, obj);
		
		subscriptionStr = "/topic/users/" + tenantChatUserId + "/submitConsultation";
		simpMessagingTemplate.convertAndSend(subscriptionStr, obj);
	}



	@RequestMapping(value = "/bot_operations/close/roomId/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public boolean giveTenant(@PathVariable Long roomId, Principal principal) throws JsonProcessingException {

		Room room_0 = roomService.getRoom(roomId);
		if(room_0 == null)
			return false;	
		
		if (tempRoomAskTenant.contains(room_0))
			return false;
		
		tempRoomAskTenant.add(room_0);

		String userIdStr = principal.getName();
		Long userId =  Long.parseLong(userIdStr, 10);
		askConsultationUsers.add(userId);
		
		
		/*
		Room room_0 = roomService.getRoom(roomId);
		if(room_0 == null)
			return false;		

		ChatTenant t_user = chatTenantService.getFreeTenant();//       getRandomTenant();//choose method
		if (t_user == null)
			return false;
		
		chatTenantService.setTenantBusy(t_user);
		  
			
		
		Long tenantChatUserId = t_user.getChatUser().getId();	
		

		Object[] obj = new Object[] {  tenantChatUserId, roomId };
		
		chatController.addFieldToInfoMap("newAskConsultation_ToChatUserId", obj);
		
		askTenantToSpendConsultationWS(tenantChatUserId, roomId );
		*/
		return giveTenant(roomId);
		/*
		ChatUser c_user = t_user.getChatUser();
		ChatUser b_user = chatUsersService.getChatUser(BotController.BotParam.BOT_ID);

		room_o.setAuthor(c_user);
		roomService.update(room_o);

		roomControler.addUserToRoom(b_user, room_o, b_user.getPrincipal(), true);

		return true;*/
	}
	
	public boolean giveTenant(Long roomId) {
		
		List<Long> ff = chatTenantService.getTenantsBusy();

		Room room_0 = roomService.getRoom(roomId);
		if(room_0 == null)
			return false;	

		ChatTenant t_user = chatTenantService.getFreeTenant();//       getRandomTenant();//choose method
		if (t_user == null)
		{
			new java.util.Timer().schedule( 
			        new java.util.TimerTask() {
			            @Override
			            public void run() {
			            	giveTenant(roomId);
			            }
			        }, 
			        30000 
			);
			
			return false;
		}
		
		Long tenantChatUserId = t_user.getChatUser().getId();	

		boolean gg = chatTenantService.setTenantBusy(t_user);

		Object[] obj = new Object[] {  tenantChatUserId, roomId };
		
		chatController.addFieldToInfoMap("newAskConsultation_ToChatUserId", obj);
		
		askTenantToSpendConsultationWS(tenantChatUserId, roomId );
		
		return true;
	}

	@RequestMapping(value = "/bot_operations/tenant/becomeFree",  method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendBecomeFree(Principal principal) {
		chatTenantService.setTenantFree(principal);
	}	
	
	@RequestMapping(value = "/bot_operations/tenant/becomeBusy",  method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendBecomeBusy(Principal principal) {
		chatTenantService.setTenantBusy(principal);
	}
		
	@RequestMapping(value = "/{roomId}/bot_operations/tenant/refuse/",  method = RequestMethod.POST)
	@ResponseBody
	public boolean tenantSendRefused(@PathVariable("roomId") Long roomId, Principal principal) {			
		return giveTenant(roomId);
	}
	
	@RequestMapping(value = "/bot_operations/tenant/did_am_wait_tenant",  method = RequestMethod.POST)
	@ResponseBody
	public boolean isUserWaitTenant(Principal principal) {	
		ChatUser user = chatUsersService.getChatUser(principal);		
		boolean isUserWaitForTenant =  isChatUserWaitTenant(user);
		
		return isUserWaitForTenant;
	}
	
	@RequestMapping(value = "/bot_operations/tenant/did_am_busy_tenant",  method = RequestMethod.POST)
	@ResponseBody
	public Object[]  isTenantBusy(Principal principal) {	
		ChatUser user = chatUsersService.getChatUser(principal);		
	
		boolean isTenant = false;
		boolean isTenantBusy = false;
		ChatTenant tenant = chatTenantService.getChatTenant(user);
		if (tenant != null)
		{
			isTenant = true;
			isTenantBusy = chatTenantService.isTenantBusy(tenant);
		}		
		
		Object[] obj = new Object[] {isTenant, isTenantBusy};
		return obj;
	}
	
	public boolean isChatUserWaitTenant(ChatUser user) {
		Long user_id = user.getId();
		for (Long id : askConsultationUsers) {
			if (id.equals(user_id))
				return true;
		}
		return false;
	}

	@RequestMapping(value = "/bot/operations/tenant/free", method = RequestMethod.POST)
	@ResponseBody
	public void tenantSendFree(Principal principal) {
		Long tenantChatUserId = Long.parseLong(principal.getName());
		
		ChatUser c_user = chatUsersService.getChatUser(tenantChatUserId);
		if (c_user == null)
			return;
		
		//ChatTenant t_user = chatTenantService.getChatTenant(tenantChatUser);
		//ChatUser c_user = t_user.getChatUser();
		//ChatUser b_user = chatUsersService.getChatUser(BotController.BotParam.BOT_ID);

		Room room_ = tempRoomAskTenant.get(0);
		tempRoomAskTenant.remove(0);
		//room_.setAuthor(c_user);
		roomService.update(room_);
		//roomControler.addUserToRoom(b_user, room_, b_user.getPrincipal(), true);
		roomControler.addUserToRoom(c_user, room_, c_user.getPrincipal(), true);		
		
		Long userId = askConsultationUsers.get(0);
		askConsultationUsers.remove(0);
		
		Object[] obj = new Object[] {userId, tenantChatUserId};

		chatController.addFieldToInfoMap("newConsultationWithTenant", obj);
		
		tenantSubmitToSpendConsultationWS(userId, tenantChatUserId);
		
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
