package com.intita.wschat.web;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.intita.wschat.annotations.ServerAccess;
import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.ChatRoomType;
import com.intita.wschat.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomRoleInfo;
import com.intita.wschat.models.User;
import com.intita.wschat.repositories.RoomRolesRepository;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UsersService;
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

	@Value("${crmchat.roles.autoupdate:true}")
	Boolean autoUpdateRoles;

	@PostConstruct
	private void autoUpdate(){
		if (autoUpdateRoles)
		updateRoomsForAllRoles(null,false);
	}

	private void updateRoomForRole(UserRole role,boolean notifyUsers){

		RoomRoleInfo info =  roomRolesRepository.findOneByRoleId(role.getValue());
		if(info == null)
		{
			Room room = roomsService.register(role.name(), chatUsersService.getChatUser(BotParam.BOT_ID), ChatRoomType.ROLES_GROUP);
			info = roomRolesRepository.save(new RoomRoleInfo(room, role.getValue()));
		}

		Room room = info.getRoom();
		//roomsService.setAuthor(chatUsersService.getChatUser(BotParam.BOT_ID), room);
		room = roomsService.update(room,notifyUsers);
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
		ChatUser cUser = null;
		User iUser = null;
		if(auth != null)
		{
			ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
			cUser = chatPrincipal.getChatUser();
			iUser = chatPrincipal.getIntitaUser();	
		}
		String name = request.getRemoteHost();
		boolean intitaSide = request.getRemoteHost().equals("127.0.0.1"); 
		if(intitaSide || usersService.checkRole(iUser, UserRole.ADMIN) ) {
			return updateRoomsForAllRoles(tableName,true);
		}
		return false;
	}
	
	private boolean updateRoomsForAllRoles(String tableName,boolean notifyUsers){
		if(tableName==null) {
			roomsService.updateRoomsForAllRoles(notifyUsers);
		}
		else {
			try {
				boolean updated = roomsService.updateRoomForRoleTable(tableName,notifyUsers);
				if (!updated) return false;
			}
			catch(Exception e){
				//e.printStackTrace();
				return false;
			}
		}

		return true;
	}
	


}