package com.intita.wschat.web;

import javax.servlet.http.HttpServletRequest;

import com.intita.wschat.config.ChatPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.OfflineGroup;
import com.intita.wschat.models.OfflineSubGroup;
import com.intita.wschat.models.User;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.OfflineStudentsGroupService;
import com.intita.wschat.services.UsersService;

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