package com.intita.wschat.admin;

import java.util.*;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.UserRole;
import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.dto.model.*;
import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.admin.models.MsgRequestModel;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.config.FlywayMigrationStrategyCustom;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.Room;
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
@PreAuthorize("hasPermission(null, 'DIRECTOR, SUPER_ADMIN')")
public class AdminPanelController {

  private final static Logger log = LoggerFactory.getLogger(AdminPanelController.class);

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
  @Autowired private RoomController roomController;
  @Autowired private DTOMapper dtoMapper;



  private final static ObjectMapper mapper = new ObjectMapper();



  @RequestMapping(value="/admin", method = RequestMethod.GET)
  public String  admin(HttpServletRequest request,Model model) {

    String lang = chatLangService.getCurrentLang();
    List<ConfigParam> config =  configParamService.getParams();
    HashMap<String,String> configMap = ConfigParam.listAsMap(config);
    configMap.put("currentLang", lang);
    model.addAttribute("lgPack", chatLangService.getLocalizationMap().get(lang));
    model.addAttribute("config", configMap);
    return "../static/admin-panel/release/index";
    //return "../static/admin-panel/dev-release/index";
  }

  @RequestMapping(value = "/chat/findUsers", method = RequestMethod.GET)
  @ResponseBody
  public Set<ExtendedIntitaUserDTO> findUsers(@RequestParam String info, Authentication auth) {
    ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

    List<User> users= new ArrayList<>();

    users.addAll(userService.findUsers(info, 10));

    Set<ExtendedIntitaUserDTO> userList = new HashSet<>();
    for(User u : users)
    {
      //ChatUser chat_user = chatUsersService.getChatUserFromIntitaUser(u, true);
      //userList.add(chatUsersService.getLoginEvent(chat_user));//,participantRepository.isOnline(""+chat_user.getId())));
      userList.add(dtoMapper.mapExtended(u));
    }
    return  userList;

  }

  @RequestMapping(value = "/chat/user/findRooms", method = RequestMethod.GET)
  @ResponseBody
  public List<ChatRoomDTO> findRooms(@RequestParam Long chatUserId,@RequestParam String roomNameLike, Authentication auth) {

    List<Room> rooms = roomService.findChatUserRooms(chatUserId,roomNameLike,10);

    List<ChatRoomDTO> roomsDTO = dtoMapper.mapListRoom(rooms);

    return  roomsDTO;

  }


  @RequestMapping(value = "/chat/user/localization", method = RequestMethod.GET)
  @ResponseBody
  public Map<String,Object> getLocalization(@RequestParam String lang, Authentication auth) {
    return  chatLangService.getLangFromDatabase().get(lang);
  }

  @RequestMapping(value = "/chat/user/localization", method = RequestMethod.POST)
  @ResponseBody
  public void setLocalization(@RequestParam String lang,@RequestBody String translationValue, Authentication auth) {
    chatLangService.saveLangToDatabase(lang,translationValue);
    chatLangService.updateDataFromDatabase();
  }

  @RequestMapping(value = "/chat/findUsersExceptRole", method = RequestMethod.GET)
  @ResponseBody
  public Set<ExtendedIntitaUserDTO> findUsersExceptRole(@RequestParam UserRole role, @RequestParam String info, Authentication auth) {
    ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

    ChatUser user = chatPrincipal.getChatUser();
    List<User> users= new ArrayList<>();
    User iUser = chatPrincipal.getIntitaUser();

    if(iUser != null)
    {
      users.addAll(userService.findUsersWithoutRole(info, 10,role));
    }
    else return new  HashSet<ExtendedIntitaUserDTO>();

    Set<ExtendedIntitaUserDTO> userList = new HashSet<>();
    for(User u : users)
    {
      //ChatUser chat_user = chatUsersService.getChatUserFromIntitaUser(u, true);
      //userList.add(chatUsersService.getLoginEvent(chat_user));//,participantRepository.isOnline(""+chat_user.getId())));
      userList.add(dtoMapper.mapExtended(u));
    }
    return  userList;

  }


  @RequestMapping(value = "/chat/msgHistory", method = RequestMethod.POST)
  @ResponseBody
  public List<UserMessageDTO> getMsgHistory(@RequestBody MsgRequestModel rqModel) {
    ChatUser first = chatUsersService.getByIntitaId(rqModel.getUserIdFirst().longValue());
    ChatUser second = chatUsersService.getByIntitaId(rqModel.getUserIdSecond().longValue());

    Date beforeDate = new Date(rqModel.getBeforeDate());
    Date afterDate = new Date(rqModel.getAfterDate());

    Room privateRoom = roomController.getPrivateRoom(first, second);
    if(first == null || second == null || beforeDate == null || afterDate == null || privateRoom == null)
      throw new NullArgumentException("");

    ArrayList<UserMessage> userMessages= userMessageService.getMessages(privateRoom.getId(), beforeDate, afterDate, null, false,false, 30,true,null);
    List< UserMessageDTO > messagesDTO = dtoMapper.mapListUserMessage(userMessages);
    return messagesDTO;
  }

  @RequestMapping(value = "test/mail/msgHistory", method = RequestMethod.GET)
  @ResponseBody
  public String getMsgHistory(@RequestParam("email") String email) {
    User user = userService.getUser(email);
    if(user == null)
      throw new NullArgumentException("user not found");
     try {
      return mailService.sendUnreadedMessageToIntitaUserFrom24Hours(user);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      return e.getMessage();
    }
     
  }


}