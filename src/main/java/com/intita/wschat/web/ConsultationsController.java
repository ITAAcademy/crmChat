package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.ChatConsultation;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.IntitaConsultation;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsService;
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
	
	@Autowired
	private VelocityEngine velocityEngine;
	
	 @Value("${chat.mail.username}")
	 String mail_userName;
	 @Value("${chat.mail.password}")
	 String mail_password;
	 @Value("${chat.mail.host}")
     String mail_host;
	 @Value("${chat.mail.port}")
	 int mail_port;
	 @Value("${chat.mail.protocol}")
	 String mail_protocol;
	
	/*@Autowired
	private JavaMailSenderImpl mailSender;*/
	private JavaMailSenderImpl mailSender; 
	
	@PostConstruct
	public void initMailSender(){
		
		mailSender = new JavaMailSenderImpl();
		mailSender.setProtocol(mail_protocol);
		mailSender.setHost(mail_host);
		mailSender.setPort(mail_port);
		mailSender.setUsername(mail_userName);
		mailSender.setPassword(mail_password);
		Properties mailProps = new Properties();
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
       // mailProps.put("mail.smtp.starttls.required", "true");
       // mailProps.setProperty("mail.smtp.user", userName);
        //mailProps.setProperty("mail.smtp.password", password);
        mailProps.put("mail.smtp.debug", "true");

        mailSender.setJavaMailProperties(mailProps);
		
	}
	
	@Autowired
	ConfigParamService configParamService;

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
	@Autowired private ChatLangRepository chatLangRepository;
	@Autowired private ConsultationsService chatConsultationsService;

	private final static ObjectMapper mapper = new ObjectMapper();

	@RequestMapping(value = "/chat/consultation/info/{id}", method = RequestMethod.POST)
	public 	@ResponseBody String getConsultationInfo(@PathVariable("id") Long consultationIntitaId, Principal principal, HttpRequest req) throws InterruptedException, JsonProcessingException {
		/*
		 * Authorization
		 */
		Map <String, Object>result = new HashMap<>();
		ChatUser cUser = chatUsersService.getChatUser(principal);
		User iUser = cUser.getIntitaUser();

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
	public 	@ResponseBody String getConsultationFromRoom(@PathVariable("id") Long roomId, Principal principal, HttpRequest req) throws InterruptedException, JsonProcessingException {
		ChatConsultation cons = chatConsultationsService.getConsultationByRoom(new Room(roomId));
		if(cons != null) 
			return mapper.writeValueAsString(cons.getId());
		return null;
	}

	@RequestMapping(value = "/chat/consultation/{do}/{id}", method = RequestMethod.POST)
	public 	@ResponseBody ResponseEntity<String> startConsultation(@PathVariable("id") Long consultationIntitaId,@PathVariable("do") String varible, Principal principal, @RequestBody Map<Long,Integer> starts, HttpRequest req) throws InterruptedException, JsonProcessingException {
		/*
		 * Authorization
		 */
		Map <String, Object>result = new HashMap<>();
		ChatUser cUser = chatUsersService.getChatUser(principal);
		User iUser = cUser.getIntitaUser();

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
				if(!participantRepository.isOnline(iCons.getConsultant().getChatUser().getId().toString()))//need wait
					return new ResponseEntity<String>(HttpStatus.METHOD_NOT_ALLOWED);
				
				room.setActive(true);//activate room
				roomService.update(room);
				cons.setStartDate(new Date());
				chatConsultationsService.update(cons);
				return new ResponseEntity<String>(HttpStatus.OK);
			}

			if(varible.equals("finish") && cons.getFinishDate() == null)
			{
				cons.setFinishDate(new Date());
				Room room = cons.getRoom();
				room.setActive(false);
				roomService.update(room);
				chatConsultationsService.update(cons);
				chatConsultationsService.setRatings(cons, starts);
				return new ResponseEntity<String>(HttpStatus.OK);
			}

			return new ResponseEntity<String>(HttpStatus.METHOD_NOT_ALLOWED);
		}
		return new ResponseEntity<String>(HttpStatus.METHOD_NOT_ALLOWED);
	}
	
	@ResponseBody 
	@RequestMapping(value = "/chat/consultation/get_mail/{id}", method = RequestMethod.GET)
	  private boolean sendConsultationEmail(final ChatConsultation chatConsultation,Principal principal,@PathVariable("id") Long consultationId) {
		  ChatUser chatuser = chatUsersService.getChatUser(principal);
		  ChatConsultation consultation = chatConsultationsService.getByIntitaConsultationId(consultationId);
		  User intitaUser = chatuser.getIntitaUser();
          if (intitaUser==null) return false;  
		  MimeMessagePreparator preparator = new MimeMessagePreparator() {
	            public void prepare(MimeMessage mimeMessage) throws Exception {
	                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
	                message.setTo(intitaUser.getEmail());//chatuser.getNickName());//destination email
	                message.setFrom("zigzag2341@gmail.com"); // could be parameterized...
	                Map model = new HashMap();
	                model.put("user", intitaUser.getEmail());
	                Room room = consultation.getRoom();
	                ArrayList<UserMessage> messages = userMessageService.getUserMessagesByRoom(room);
	                ArrayList<ChatMessage> simpleMessage = ChatMessage.getAllfromUserMessages(messages);
	                model.put("messages", simpleMessage);
	                String text = VelocityEngineUtils.mergeTemplateIntoString(
	                        velocityEngine, "consultation_email.vm","UTF-8", model);
	                message.setText(text, true);
	            }
	        };
	       try{ 
	    	   this.mailSender.send(preparator);     
	       }
	       catch(Exception e){
	    	   System.out.println("EMAIL SENDING ERROR, MAYBE AUTH FAILED");
	    	   return false;
	       }
	        return true;
	    }
	
}