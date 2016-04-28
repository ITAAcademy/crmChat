package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.models.BotItemContainer;
import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.BotItemContainerService;
import com.intita.wschat.services.BotSequenceService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.CourseService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;

@Service
@Controller
public class BotController {
	@Autowired
	BotSequenceService botSeuenceService;

	@Autowired
	BotItemContainerService botItemContainerService;

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
	
	
	@Autowired private RoomController roomControler;

	@PostConstruct
	public void addTestInfoToDb(){
		botSequence = generateTestSequnce();
		botSeuenceService.add(botSequence);
	}

	BotItemContainer currentContainer = null;
	BotCategory botSequence;

	public String getJsonContainerBodySimple(String[] itemsText){
		String res = "{";
		for (int i = 0 ; i < itemsText.length;i++){
			if(i!=0)res += ",";
			res += String.format("item%d:{'data':'%s'}",i,itemsText[i]);
		}
		res+="}";
		return res;
	}
	public BotCategory generateTestSequnce(){
		BotCategory botSequence = new BotCategory();
		String[] container1 = {"Variant1,Variant2,Variant3,Variant4"};
		BotItemContainer testItemContainer1 = botItemContainerService.add(new BotItemContainer(getJsonContainerBodySimple(container1)));//begin
		BotItemContainer testItemContainer2 = botItemContainerService.add(new BotItemContainer(getJsonContainerBodySimple(container1)));
		BotItemContainer testItemContainer3 = botItemContainerService.add(new BotItemContainer(getJsonContainerBodySimple(container1)));
		BotItemContainer testItemContainer4 = botItemContainerService.add(new BotItemContainer(getJsonContainerBodySimple(container1)));//end
		BotItemContainer testItemContainer5 = botItemContainerService.add(new BotItemContainer(getJsonContainerBodySimple(container1)));
		testItemContainer1.addBranch(0, testItemContainer2);
		testItemContainer2.addBranch(0, testItemContainer3);
		testItemContainer3.addBranch(0, testItemContainer4);
		botItemContainerService.update(testItemContainer1);
		botItemContainerService.update(testItemContainer2);
		botItemContainerService.update(testItemContainer3);
		botItemContainerService.update(testItemContainer4);
		botSequence.addElement(testItemContainer1);
		return botSequence;
	}

	@RequestMapping(value = "bot_operations/sequences/{sequenceId}/{containerId}/nextContainer{choseIndex}", method = RequestMethod.GET)
	@ResponseBody
	public String getSequence(@PathVariable Long sequenceId,@PathVariable Long containerId, @PathVariable int choseIndex) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();


		return objectMapper.writeValueAsString(botSequence);
	}


	@RequestMapping(value = "bot_operations/close/roomId/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public void giveTenant(@PathVariable Long roomId) throws JsonProcessingException {
		
		Room room_o = roomService.getRoom(roomId);
		if(room_o == null)
			return;			
			
		ArrayList<ChatTenant> countTenant = chatTenantService.getTenants();
		if(countTenant.isEmpty())
		{
			return;
		}
		int k = new Random().nextInt(countTenant.size());

		ChatTenant t_user = countTenant.get(k);//choose method
		ChatUser c_user = t_user.getChatUser(); 
		
		
		roomControler.addUserToRoom(c_user, room_o, room_o.getAuthor().getPrincipal(), true);
		
		
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
