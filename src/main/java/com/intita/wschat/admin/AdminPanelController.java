package com.intita.wschat.admin;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.admin.models.MsgRequestModel;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.config.FlywayMigrationStrategyCustom;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.BotItemContainerService;
import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.CourseService;
import com.intita.wschat.services.IntitaMailService;
import com.intita.wschat.services.IntitaSubGtoupService;
import com.intita.wschat.services.LecturesService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.CommonController;
import com.intita.wschat.web.RoomController;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Nicolas Haiduchok
 */
@Service
@Controller
public class AdminPanelController {

	private final static Logger log = LoggerFactory.getLogger(AdminPanelController.class);

	@Autowired ConfigParamService configParamService;
	@Autowired private ProfanityChecker profanityFilter;
	@Autowired private SessionProfanity profanity;
	@Autowired private ParticipantRepository participantRepository;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired private ConsultationsService chatIntitaConsultationService;
	@Autowired private CustomAuthenticationProvider authenticationProvider;
	@Autowired private RoomsService chatRoomsService;
	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private ChatTenantService ChatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	@Autowired private ChatLangRepository chatLangRepository;
	@Autowired private ConsultationsService chatConsultationsService;
	@Autowired private CourseService courseService;
	@Autowired private LecturesService lecturesService;
	@Autowired private BotItemContainerService dialogItemService;
	@Autowired private ChatLangService chatLangService;
	@Autowired private ChatTenantService chatTenantService;
	@Autowired private IntitaSubGtoupService subGroupService;
	@Autowired private FlywayMigrationStrategyCustom flyWayStategy;
	@Autowired private IntitaMailService mailService;
	@Autowired private CommonController commonController;
	@Autowired private RoomController roomController;
	
	

	private final static ObjectMapper mapper = new ObjectMapper();


	@PreAuthorize("hasPermission(null, 'ADMIN')")
	@RequestMapping(value="/admin", method = RequestMethod.GET)
	public String  admin(HttpServletRequest request,Model model, Principal principal) {
		
		chatUsersService.getChatUser(principal);
		String lang = chatLangService.getCurrentLang();
		List<ConfigParam> config =  configParamService.getParams();
		HashMap<String,String> configMap = ConfigParam.listAsMap(config);
		configMap.put("currentLang", lang);
		model.addAttribute("lgPack", chatLangService.getLocalizationMap().get(lang));
		model.addAttribute("config", configMap);
		return "../static/admin-panel/release/index";
		//return "../static/admin-panel/dev-release/index";
	}

	@PreAuthorize("hasPermission(null, 'ADMIN')")
	@RequestMapping(value = "/chat/findUsersWithRoles", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent> getTrainerStudentsById(@RequestParam Integer roles, @RequestParam String info, Principal principal) {
		ChatUser user = chatUsersService.getChatUser(principal);
		List<User> users= new ArrayList<>();
		User iUser = user.getIntitaUser();

		if(iUser != null)
		{
			Long intitaUserId = user.getIntitaUser().getId();
			for(User.Roles role : User.Roles.values())
			{
				if((role.getValue() & roles) == role.getValue())
					users.addAll(userService.getUsersFist5WithRole(info, role));	
			}

		}
		else return new  HashSet<LoginEvent>();

		Set<LoginEvent> userList = new HashSet<>();
		for(User u : users)
		{
			ChatUser chat_user = chatUsersService.getChatUserFromIntitaUser(u, true); 
			userList.add(chatUsersService.getLoginEvent(chat_user));//,participantRepository.isOnline(""+chat_user.getId())));
		}
		return  userList;

	}


	@RequestMapping(value = "/chat/msgHistory", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<ChatMessage> getMsgHistory(Principal principal, @RequestBody MsgRequestModel rqModel) {
		ChatUser first = chatUsersService.getChatUser(rqModel.getUserIdFirst().longValue());
		ChatUser second = chatUsersService.getChatUser(rqModel.getUserIdSecond().longValue());
		
		Date beforeDate = new Date(rqModel.getBeforeDate());
		Date afterDate = new Date(rqModel.getAfterDate());
		
		Room privateRoom = roomController.getPrivateRoom(first, second);
		if(first == null || second == null || beforeDate == null || afterDate == null || privateRoom == null)
			throw new NullArgumentException("");
		
		ArrayList<UserMessage> userMessages= userMessageService.getMessages(privateRoom.getId(), beforeDate, afterDate, null, false, 30);
		ArrayList<ChatMessage> chatMessages = ChatMessage.getAllfromUserMessages(userMessages);
		return chatMessages;
	}

	
	
}