package com.intita.wschat.web;

import javax.servlet.http.HttpServletRequest;

import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
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


  private volatile boolean group_last = true;
  private volatile boolean sub_group_last = true;
  
  @RequestMapping(value = "sub_group_operations/last_status", method = RequestMethod.GET)
  @ResponseBody
  public boolean subGroupStatus() {
    return sub_group_last;
  }
  
  @RequestMapping(value = "group_operations/last_status", method = RequestMethod.GET)
  @ResponseBody
  public boolean groupStatus() {
    return group_last;
  }

  @Async
  @RequestMapping(value = "sub_group_operations/update/{subGroupId}", method = RequestMethod.GET)
  @ResponseBody
  public void subGroupUpdate(@PathVariable Integer subGroupId,  HttpServletRequest request, Authentication auth) throws JsonProcessingException {
    sub_group_last = false;
    ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
    ChatUser user = chatPrincipal.getChatUser();
    User intitaUser = chatPrincipal.getIntitaUser();
    if(intitaUser == null)
      return;

    OfflineSubGroup subGroup = offlineStudentsGroupService.getSubGroup(subGroupId);
    if(subGroup == null || !usersService.isSuperVisor(intitaUser.getId()))
      return;

    offlineStudentsGroupService.updateSubGroupRoom(subGroup, true,auth);
    sub_group_last = true;
  }

  @Async
  @RequestMapping(value = "group_operations/update/{groupId}", method = RequestMethod.GET)
  @ResponseBody
  public void groupUpdate(@PathVariable Integer groupId,  HttpServletRequest request, Authentication auth) throws JsonProcessingException {
    group_last = false;
    ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
    ChatUser user = chatPrincipal.getChatUser();
    User intitaUser =chatPrincipal.getIntitaUser();
    if(intitaUser == null)
      return;

    OfflineGroup subGroup = offlineStudentsGroupService.getGroup(groupId);
    if(subGroup == null || (!usersService.isSuperVisor(intitaUser.getId()) && !usersService.isAdmin(intitaUser.getId())))
      return;

    offlineStudentsGroupService.updateGroupRoom(subGroup, true,auth);
    group_last = true;
  }
}