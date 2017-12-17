package com.intita.wschat.web;

import java.security.Principal;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import com.intita.wschat.admin.models.MsgResponseRatingsModel;
import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.SubscribedtoRoomsUsersBufferModal;
import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.dto.model.ChatRoomDTO;
import com.intita.wschat.dto.model.ChatUserDTO;
import com.intita.wschat.models.*;
import com.intita.wschat.services.common.UsersOperationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.RoomNotFoundException;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsRatingsService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.IntitaMailService;
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

@Controller
public class ConsultationsController {
	

	
	/*@Autowired
	private JavaMailSenderImpl mailSender;*/

	

	
	@Autowired
	ConfigParamService configParamService;
	
	@Autowired
	IntitaMailService mailService;

	@Autowired private ProfanityChecker profanityFilter;

	@Autowired private SessionProfanity profanity;

	@Autowired private ParticipantRepository participantRepository;

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired private CustomAuthenticationProvider authenticationProvider;

	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private ChatTenantService ChatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	@Autowired private ChatLangService chatLangService;
	@Autowired private ConsultationsService chatConsultationsService;
	@Autowired private ConsultationsRatingsService chatConsultationsRatingsService;
	@Autowired private LecturesService lecturesService;
	@Autowired private ConsultationsService chatIntitaConsultationService;
	@Autowired private CommonController commonController;
	@Autowired private RoomsService chatRoomsService;

	@Autowired
	@Lazy
	private UsersOperationsService usersOperationsService;

	private final static ObjectMapper mapper = new ObjectMapper();
	@Autowired
	private DTOMapper dtoMapper;

	@RequestMapping(value = "/chat/consultation/info/{id}", method = RequestMethod.POST)
	public 	@ResponseBody String getConsultationInfo(@PathVariable("id") Long consultationIntitaId, Authentication auth) throws InterruptedException, JsonProcessingException {
		/*
		 * Authorization
		 */
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		Map <String, Object>result = new HashMap<>();
		ChatUser cUser = chatPrincipal.getChatUser();
		User iUser = chatPrincipal.getIntitaUser();

		if(iUser == null)
		{
			result.put("status", -1);//access deny
			return mapper.writeValueAsString(result);
		}

		IntitaConsultation iCons = chatConsultationsService.getIntitaConsultationById(consultationIntitaId);

		/*
		 * Get info
		 */
		if(iCons != null)
		{
			/*
			 * Authorization
			 */
			User author = iCons.getAuthor();
			User consultant = iCons.getConsultant();
			
			if(author.equals(iUser))
				result.put("consultant", false);
			else
				if(consultant.equals(iUser))
					result.put("consultant", true);
				else
				{
					result.put("status", -2);//access deny
					return mapper.writeValueAsString(result);
				}
			/*
			 * 
			 */

			ChatConsultation cons = chatConsultationsService.getByIntitaConsultation(iCons);

			
			if(cons.getStartDate() == null)
				result.put("status", 1);//not begin
			else
			{
				Date finishDate = iCons.getDate();
				finishDate.setTime(iCons.getFinishTime().getTime());
				
				ChatUser chatAuthor = author.getChatUser();
				ChatUser chatConsultant = consultant.getChatUser();
				if(new Date().after(finishDate))
				{
					if(!participantRepository.isOnline(chatAuthor.getId()) || !participantRepository.isOnline(chatConsultant.getId()))
						finishConsultation(cons);
				}
				if(cons.getFinishDate() == null)
					result.put("status", 2);//not finish
				else
					result.put("status", 0);//finish
			}
			result.put("roomId", cons.getRoom().getId());			
			return mapper.writeValueAsString(result);
		}

		result.put("status", -2);//consultation not found
		return mapper.writeValueAsString(result);
	}
	@RequestMapping(value = "/chat/consultation/fromRoom/{id}", method = RequestMethod.POST)
	public 	@ResponseBody String getConsultationFromRoom(@PathVariable("id") Long roomId) throws InterruptedException, JsonProcessingException {
		ChatConsultation cons = chatConsultationsService.getConsultationByRoom(new Room(roomId));
		if(cons != null) 
			return mapper.writeValueAsString(cons.getIntitaConsultation().getId());
		return null;
	}

	private void finishConsultation(ChatConsultation cons)
	{
		cons.setFinishDate(new Date());
		Room room = cons.getRoom();
		room.setActive(false);
		roomService.update(room,true);
		chatConsultationsService.update(cons);
	}
	
	@RequestMapping(value = "/chat/rooms/create/consultation/", method = RequestMethod.POST)
	@ResponseBody
	public void createConsultation(Authentication auth, @RequestBody Map<Object, String > param/*, @RequestBody String date_str,
			@RequestBody String time_begin, @RequestBody String time_end*/
			) throws ParseException
	{
		String time_begin = param.get("begin");
		String time_end = param.get("end");
		String date_str = param.get("date");
		String lection_title = param.get("lection");
		String teacher_email = param.get("email");

		IntitaConsultation consultation = new IntitaConsultation();	
		Date date = commonController.getSqlDate(date_str);

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

		Time start_time = new Time(formatter.parse(time_begin).getTime());		
		Time endTime = new Time(formatter.parse(time_end).getTime());

		Lectures lecture = null;

		int lang = chatLangService.getCurrentLangInt();

		if (lang == lecturesService.EN)
			lecture = lecturesService.getLectureByTitleEN(lection_title);
		else
			if (lang == lecturesService.RU)
				lecture = lecturesService.getLectureByTitleRU(lection_title);
		if (lang == lecturesService.UA)
			lecture = lecturesService.getLectureByTitleUA(lection_title);

		if (date == null)
		{			
			System.out.println("Date null!!!!!!");
			return;
		}

		if (start_time == null)
		{
			System.out.println("start_time null!!!!!!");
			return;
		}

		if (endTime == null)
		{
			System.out.println("endTime null!!!!!!");
			return;
		}

		if (teacher_email == null)
		{
			System.out.println("teacher_email null!!!!!!");
			return;
		}

		if (lecture == null)
		{
			System.out.println("lecture null!!!!!!");
			return;
		}
		ChatPrincipal principal = (ChatPrincipal)auth.getPrincipal();

		consultation.setDate(date);
		consultation.setStartTime(start_time);
		consultation.setFinishTime(endTime);
		consultation.setAuthor(principal.getIntitaUser());

		User consultant = userService.getUser(teacher_email);

		ChatUser chatUserTest = consultant.getChatUser();
		if (chatUserTest == null)
		{
			chatUserTest = chatUsersService.getChatUserFromIntitaEmail(consultant.getEmail(), false);
			consultant.setChatUser(chatUserTest);
		}

		consultation.setConsultant(consultant);
		consultation.setLecture(lecture);

		IntitaConsultation consultation_registered = chatIntitaConsultationService.registerConsultaion(consultation);

		ChatConsultation chatConsultation = chatConsultationsService.getByIntitaConsultation(consultation_registered);

		//Room room_consultation = chatConsultation.getRoom();
		ChatPrincipal chatPrincipal = (ChatPrincipal)principal;

		Long chatUserId = chatPrincipal.getChatUser().getId();

		List<ChatRoomDTO> list = chatRoomsService.getRoomsByChatUser(chatUserTest.getId());

		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,
				new RoomController.UpdateRoomsPacketModal (list,false));

		//this said ti author that he nust update room`s list
		ChatUser author = chatPrincipal.getChatUser();
		usersOperationsService.addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(author));
		//777
	}
	
	
	@RequestMapping(value="/getRatings", method = RequestMethod.GET)
	@ResponseBody
	public Set<ConsultationRatings>  getSuportedRatingsWithTranslate(HttpServletRequest request) {
		Set<ConsultationRatings> retings = chatConsultationsRatingsService.getAllSupportedRetings();
		Set<ConsultationRatings> retingsTran = new HashSet<>();
		Map<String, Object> ratingLang = (Map<String, Object>) chatLangService.getLocalization().get("ratings");
		for (ConsultationRatings consultationRatings : retings) {
			ConsultationRatings consultationRatingsCopy = new ConsultationRatings(consultationRatings);
			String translate_rating_name = (String)ratingLang.get(consultationRatingsCopy.getName());
			consultationRatingsCopy.setName(translate_rating_name);
			retingsTran.add(consultationRatingsCopy);
		}
		return retingsTran;
	}
	
	@RequestMapping(value="/addRatingByRoom/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public boolean addRatingByRoom(HttpServletRequest request, @PathVariable("roomId") Long roomId, @RequestBody Map<Long, Integer> ratingsValues, Authentication auth) {
		Room room = chatRoomsService.getRoom(roomId);
		if(room == null)
			throw new RoomNotFoundException("Room id is faild");
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser user = chatPrincipal.getChatUser();
		
		ArrayList<ChatConsultationResultValue> values = new ArrayList<>();
		for(Long ratingId : ratingsValues.keySet())
		{
			ChatConsultationResultValue t = new ChatConsultationResultValue(new ConsultationRatings(ratingId), ratingsValues.get(ratingId));
			values.add(t);
		}
		chatConsultationsRatingsService.addRetings(room, user, values);
		return true;
	}

	@RequestMapping(value="/who_rate_user_info", method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<MsgResponseRatingsModel> findWhoRateUser(@RequestParam Long chatUserId) {
		List<ChatConsultationResult> constulationResults = chatConsultationsRatingsService.findConsultionResultsByPrivateRoomUser(ChatUser.forId(chatUserId));
		return MsgResponseRatingsModel.convertAllChatConsultationResults(constulationResults);
	}
	
	
	@RequestMapping(value="/consultationTemplate.html", method = RequestMethod.GET)
	public String  getConsultationTemplate(HttpRequest request, Model model,Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal) auth.getPrincipal();
		Set<ConsultationRatings> retings = chatConsultationsRatingsService.getAllSupportedRetings();
		Map<String, Object> ratingLang = (Map<String, Object>) chatLangService.getLocalization().get("ratings");
		for (ConsultationRatings consultationRatings : retings) {
			ConsultationRatings consultationRatingsCopy = new ConsultationRatings(consultationRatings);
			String translate_rating_name = (String)ratingLang.get(consultationRatingsCopy.getName());
			consultationRatingsCopy.setName(translate_rating_name);
			//retings.add(consultationRatingsCopy);
		}
		model.addAttribute("ratingsPack", retings);
		commonController.addLocolizationAndConfigParam(model,chatPrincipal.getChatUser());
		return commonController.getTeachersTemplate(request, "consultationTemplate", model,auth);
	}	
	
	@RequestMapping(value = "/chat/consultation/{do}/{id}", method = RequestMethod.POST)
	public 	@ResponseBody ResponseEntity<String> startConsultation(@PathVariable("id") Long consultationIntitaId,@PathVariable("do") String varible, Authentication auth, @RequestBody Map<Long,Integer> starts) throws InterruptedException, JsonProcessingException {
		/*
		 * Authorization
		 */
		Map <String, Object>result = new HashMap<>();
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser cUser = chatPrincipal.getChatUser();
		User iUser = chatPrincipal.getIntitaUser();

		if(iUser == null)
		{
			return new ResponseEntity<String>(HttpStatus.METHOD_NOT_ALLOWED);
		}

		IntitaConsultation iCons = chatConsultationsService.getIntitaConsultationById(consultationIntitaId);

		if(iCons != null && iCons.getAuthor().equals(iUser))
		{

			ChatConsultation cons = chatConsultationsService.getByIntitaConsultation(iCons);

			if(varible.equals("start") && cons.getStartDate() == null)
			{
				Room room = cons.getRoom();
				if(!participantRepository.isOnline(iCons.getConsultant().getChatUser().getId()))//need wait
					return new ResponseEntity<String>(HttpStatus.METHOD_NOT_ALLOWED);
				
				room.setActive(true);//activate room
				roomService.update(room,true);
				cons.setStartDate(new Date());
				chatConsultationsService.update(cons);
				return new ResponseEntity<String>(HttpStatus.OK);
			}

			if(varible.equals("finish") && cons.getFinishDate() == null)
			{
				finishConsultation(cons);
				chatConsultationsService.setRatings(cons, starts);
				return new ResponseEntity<String>(HttpStatus.OK);
			}

			return new ResponseEntity<String>(HttpStatus.METHOD_NOT_ALLOWED);
		}
		return new ResponseEntity<String>(HttpStatus.METHOD_NOT_ALLOWED);
	}
	
	@ResponseBody 
	@RequestMapping(value = "/chat/consultation/get_mail/{id}", method = RequestMethod.GET)
	  private boolean sendConsultationEmail(final ChatConsultation chatConsultation,Authentication auth,@PathVariable("id") Long consultationId) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser chatuser = chatPrincipal.getChatUser();
		  ChatConsultation consultation = chatConsultationsService.getByIntitaConsultationId(consultationId);
		  User intitaUser = chatPrincipal.getIntitaUser();
          if (intitaUser==null) return false;  
		  
	       try{ 
	    	   mailService.sendUnreadedMessageToIntitaUserFrom24Hours(intitaUser);     
	       }
	       catch(Exception e){
	    	   System.out.println("EMAIL SENDING ERROR, MAYBE AUTH FAILED");
	    	   return false;
	       }
	        return true;
	    }
	
}