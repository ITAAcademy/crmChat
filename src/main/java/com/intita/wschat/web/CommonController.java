package com.intita.wschat.web;

import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.intita.wschat.config.ChatPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.config.FlywayMigrationStrategyCustom;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.RoomPermissions;
import com.intita.wschat.models.User;
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

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Nicolas Haiduchok
 */
@Service
@Controller
public class CommonController {

	@Autowired
	ConfigParamService configParamService;
	private final static Logger log = LoggerFactory.getLogger(CommonController.class);

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

	
	@CrossOrigin(origins = "*", maxAge=3600)
	@RequestMapping(value = "/static_templates/itaMessegger.html", method = RequestMethod.GET)
	public String getChatTemplate(){
		return "itaMessegger";
	}
	
	public static java.sql.Date getSqlDate(String date_str)
	{	  
		java.util.Date apptDay = null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		java.sql.Date sqlDate = null;

		try
		{
			apptDay = (java.util.Date) df.parse(date_str);
		}
		catch(ParseException e)
		{
			System.out.println("Please set a valid date! Format is yyyy-mm-dd");
		}
		sqlDate = new java.sql.Date(apptDay.getTime());
		return sqlDate;
	}

	public void addLocolizationAndConfigParam(Model model,ChatUser currentUser)
	{
		String lang = chatLangService.getCurrentLang();
		model.addAttribute("lgPack", chatLangService.getLocalizationMap().get(lang));
		List<ConfigParam> config =  configParamService.getParams();
		HashMap<String,String> configMap = ConfigParam.listAsMap(config);
		configMap.put("currentLang", lang);
		model.addAttribute("config", configMap);
		model.addAttribute("phrasesPack", roomService.getEvaluatedPhrases(currentUser));
		model.addAttribute("user_copabilities_supported", RoomPermissions.Permission.getSupported());
	}
	
	@RequestMapping(value="/updateLang", method = RequestMethod.GET)
	@ResponseBody
	public boolean  updateLang(HttpServletRequest request) {
		chatLangService.updateDataFromDatabase();
		return true;
	}

	@RequestMapping(value="/", method = RequestMethod.GET)
	public String  getIndex( @RequestParam(required = false) String before,  Model model) {
		Authentication auth =  authenticationProvider.autorization(authenticationProvider);
		//chatLangService.updateDataFromDatabase();
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		if(before != null)
		{
			return "redirect:"+ before;
		}
		if(auth != null)
		{
			addLocolizationAndConfigParam(model, chatPrincipal.getChatUser());
			log.info("Index login: "  + chatPrincipal.getChatUser().getId());
		}
		return "index";
	}
	
	@RequestMapping(value="/static_templates/{page}.html", method = RequestMethod.GET)
	public ModelAndView  test(@PathVariable String page , final HttpServletResponse response, HttpRequest request, ModelAndView mv, Model model,Principal principal) {
		mv.setViewName("../static/static_templates/" + page);
		mv.addObject("lgPack", chatLangService.getLocalizationMap().get(chatLangService.getCurrentLang()));
		response.setHeader("Cache-Control", "max-age=3600");
		return mv;
	}

	@RequestMapping(value="/{page}.html", method = RequestMethod.GET)
	public String  getTeachersTemplate(HttpRequest request, @PathVariable("page") String page, Model model,Authentication auth) {
		//HashMap<String,Object> result =   new ObjectMapper().readValue(JSON_SOURCE, HashMap.class);
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		addLocolizationAndConfigParam(model,chatPrincipal.getChatUser());
		return page;
	}
	
	@RequestMapping(value="/403", method = RequestMethod.GET)
	public String  accessDeny(HttpRequest request) {
		return "redirect:" + configParamService.getParam("baseUrl").getValue() + "/site/authorize";
	}
	
	
	@RequestMapping(value="/chat/update/users/name", method = RequestMethod.GET)
	@ResponseBody
	public Boolean updateUserName(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser cUser = chatPrincipal.getChatUser();
		//update new info about intitaUser
		chatPrincipal.setIntitaUser(userService.getById(chatPrincipal.getIntitaUser().getId()));
		if(cUser == null)
			return false;
		
		User iUser = chatPrincipal.getIntitaUser();
		if(iUser == null)
			return false;
		
		cUser.setNickName(iUser.getNickName());
		chatUsersService.updateChatUserInfo(cUser);
		return true;
	}
	
	
	
}