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

import com.intita.wschat.annotations.ServerAccess;
import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.ChatRoomType;
import com.intita.wschat.domain.UserRole;
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
import org.springframework.web.bind.annotation.*;

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
import com.intita.wschat.models.RoomRoleInfo;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.repositories.RoomRolesRepository;
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
import com.intita.wschat.web.BotController.BotParam;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Nicolas Haiduchok
 */

@Controller
public class RoleGroupsController {
	@Autowired private UsersService usersService;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private RoomsService roomsService;

	@Autowired private RoomRolesRepository roomRolesRepository;


	private void updateRoomForRole(UserRole role){

		RoomRoleInfo info =  roomRolesRepository.findOneByRoleId(role.getValue());
		if(info == null)
		{
			Room room = roomsService.register(role.name(), chatUsersService.getChatUser(BotParam.BOT_ID), ChatRoomType.ROLES_GROUP);
			info = roomRolesRepository.save(new RoomRoleInfo(room, role.getValue()));
		}

		Room room = info.getRoom();
		//roomsService.setAuthor(chatUsersService.getChatUser(BotParam.BOT_ID), room);
		room = roomsService.update(room);
		ArrayList<ChatUser> cUsersList = null;
		if(role == UserRole.TENANTS)
			cUsersList = chatUsersService.getUsers(usersService.getAllByRole(role));
		else
			cUsersList = chatUsersService.getChatUsersFromIntitaIds(usersService.getAllByRole(role));
		roomsService.replaceUsersInRoom(room, cUsersList);
	}

	//@PostConstruct

	@RequestMapping(value = "roles_operations/update", method = RequestMethod.GET)
	@ResponseBody
	@CrossOrigin(maxAge = 3600, origins = "http://localhost:80")
	@ServerAccess
	private boolean updateRoomsForAllRolesRequest(HttpServletRequest request, Authentication auth, @RequestParam(name="table",required=false) String tableName){
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser cUser = chatPrincipal.getChatUser();
		User iUser = chatPrincipal.getIntitaUser();
		String name = String.valueOf(request.getRemoteHost());
		boolean intitaSide = request.getRemoteHost().equals("0:0:0:0:0:0:0:1"); 
		if(usersService.checkRole(iUser, UserRole.ADMIN) || intitaSide) {
			if(tableName==null) {
				roomsService.updateRoomsForAllRoles();
			}
			else {
				try {
					boolean updated = roomsService.updateRoomForRoleTable(tableName);
					if (!updated) return false;
				}
				catch(Exception e){
					//e.printStackTrace();
					return false;
				}
			}

			return true;
		}
		return false;
	}


}