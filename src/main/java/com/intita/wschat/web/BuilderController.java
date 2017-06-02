package com.intita.wschat.web;

import java.security.Principal;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.config.FlywayMigrationStrategyCustom;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.domain.UserWaitingForTrainer;
import com.intita.wschat.domain.interfaces.IPresentOnForum;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.ChatUserNotFoundException;
import com.intita.wschat.exception.ChatUserNotInRoomException;
import com.intita.wschat.exception.RoomNotFoundException;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.ChatConsultation;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.ConsultationRatings;
import com.intita.wschat.models.Course;
import com.intita.wschat.models.IntitaConsultation;
import com.intita.wschat.models.Lectures;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.RoomPermissions;
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
import com.intita.wschat.util.HtmlUtility;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.RoomController.SubscribedtoRoomsUsersBufferModal;

import jsonview.Views;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Nicolas Haiduchok
 */
@Service
@Controller
public class BuilderController {

	private final static Logger log = LoggerFactory.getLogger(BuilderController.class);
	
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

	private final static ObjectMapper mapper = new ObjectMapper();


	@RequestMapping(value="/getForm/{id}", method = RequestMethod.GET)
	public String  getTeachersTemplate(HttpServletRequest request, @PathVariable("id") Long id, @RequestParam(value = "lang", required = false) String lang, Model model, RedirectAttributes redir,Authentication auth) {
		commonController.getIndex( null, model);
		BotDialogItem item;
		if(lang != null)
			item = dialogItemService.getByIdAndLang(id, lang);
		else
			item = dialogItemService.getById(id);

		if(item == null)
			return "quize_err";
		//model.addAttribute("item", HtmlUtility.escapeQuotes(item.getBody()));
		try {
			model.addAttribute("item", HtmlUtility.escapeQuotes(mapper.writeValueAsString(item)));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("description", HtmlUtility.escapeQuotes(item.getDescription()));
		//	redir.addAttribute("item", item.getBody());

		return "formView";
	}

	@RequestMapping(value="/builder", method = RequestMethod.GET)
	public String  getFormEditorTemplate(HttpServletRequest request,Model model) {
		String lang = chatLangService.getCurrentLang();
		List<ConfigParam> config =  configParamService.getParams();
		HashMap<String,String> configMap = ConfigParam.listAsMap(config);
		configMap.put("currentLang", lang);
		model.addAttribute("lgPack", chatLangService.getLocalizationMap().get(lang));
		model.addAttribute("config", configMap);
		return "builder_index";
	}

}