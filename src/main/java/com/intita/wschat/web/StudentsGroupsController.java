package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import com.intita.wschat.config.ChatPrincipal;
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
import org.springframework.security.core.Authentication;
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
import com.intita.wschat.models.OfflineGroup;
import com.intita.wschat.models.OfflineSubGroup;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.OfflineStudentsGroupService;
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
public class StudentsGroupsController {
	@Autowired private UsersService usersService;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private OfflineStudentsGroupService offlineStudentsGroupService;
	
	@RequestMapping(value = "sub_group_operations/update/{subGroupId}", method = RequestMethod.GET)
	@ResponseBody
	public boolean subGroupUpdate(@PathVariable Integer subGroupId,  HttpServletRequest request, Authentication auth) throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser user = chatPrincipal.getChatUser();
		User intitaUser = chatPrincipal.getIntitaUser();
		if(intitaUser == null)
			return false;
		
		OfflineSubGroup subGroup = offlineStudentsGroupService.getSubGroup(subGroupId);
		if(subGroup == null || !usersService.isSuperVisor(intitaUser.getId()))
			return false;
		
		offlineStudentsGroupService.updateSubGroupRoom(subGroup, true,auth);
		return true;
	}
	
	@RequestMapping(value = "group_operations/update/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public boolean groupUpdate(@PathVariable Integer groupId,  HttpServletRequest request, Authentication auth) throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser user = chatPrincipal.getChatUser();
		User intitaUser =chatPrincipal.getIntitaUser();
		if(intitaUser == null)
			return false;
		
		OfflineGroup subGroup = offlineStudentsGroupService.getGroup(groupId);
		if(subGroup == null || (!usersService.isSuperVisor(intitaUser.getId()) && !usersService.isAdmin(intitaUser.getId())))
			return false;
		
		offlineStudentsGroupService.updateGroupRoom(subGroup, true,auth);
		
		return true;
	}
}