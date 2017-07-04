package com.intita.wschat.admin;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.admin.models.MsgRequestRatingsModel;
import com.intita.wschat.admin.models.MsgResponseRatingsModel;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.config.FlywayMigrationStrategyCustom;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.ChatUserNotFoundException;
import com.intita.wschat.models.ChatConsultationResult;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.BotItemContainerService;
import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsRatingsService;
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
@PreAuthorize("hasPermission(null, 'ADMIN')")
public class AdminPanelRatingsController {

	private final static Logger log = LoggerFactory.getLogger(AdminPanelRatingsController.class);

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
	@Autowired private ConsultationsRatingsService consultationsRatingsService;	
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
	
	
	@RequestMapping(value = "/chat/admin/ratingByRoom", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<MsgResponseRatingsModel> ratingByRoom(@RequestBody MsgRequestRatingsModel rqModel) {
		Room ratingRoom = null;
		
		if(rqModel.getIsUser())
		{
			if(rqModel.getRoomUserIds().length < 2)
				throw new ChatUserNotFoundException("dont enough ids, must be 2");
			ChatUser first = chatUsersService.getChatUser(rqModel.getRoomUserIds()[0]);
			ChatUser second = chatUsersService.getChatUser(rqModel.getRoomUserIds()[1]);
			if(first == null || second == null)
				throw new ChatUserNotFoundException("second or first user not found");
			ratingRoom = roomService.getPrivateRoom(first, second);
		}
		else{
			Long roomId = rqModel.getRoomUserIds()[0];
			ratingRoom = roomService.getRoom(roomId);
		}
		
		Date beforeDate = new Date(rqModel.getBeforeDate());
		Date afterDate = new Date(rqModel.getAfterDate());
		
		 ArrayList<ChatConsultationResult> chatConsultationResults = consultationsRatingsService.getRetingsByRoomAndDates(ratingRoom, beforeDate, afterDate);
		
		return MsgResponseRatingsModel.convertAllChatConsultationResults(chatConsultationResults);
	}
	
}