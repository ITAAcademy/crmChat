package com.intita.wschat.web;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.config.FlywayMigrationStrategyCustom;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.Lectures;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.services.BotItemContainerService;
import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.CourseService;
import com.intita.wschat.services.IntitaSubGtoupService;
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
@Service
@Controller
public class LectureController {

	@Autowired
	ConfigParamService configParamService;
	private final static Logger log = LoggerFactory.getLogger(LectureController.class);

	@Autowired private LecturesService lecturesService;
	@Autowired private ChatLangService chatLangService;

	@RequestMapping(value = "/chat/lectures/getfivelike/", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<Lectures> getLecturesLike(@RequestBody String title) throws JsonProcessingException {
		List<Lectures> lecturesList = new ArrayList<Lectures>();

		int lang = chatLangService.getCurrentLangInt();

		if (lang == lecturesService.EN)
			lecturesList = lecturesService.getFirstFiveLecturesByTitleEnLike(title);
		else
			if (lang == lecturesService.RU)
				lecturesList = lecturesService.getFirstFiveLecturesByTitleRuLike(title);
		if (lang == lecturesService.UA)
			lecturesList = lecturesService.getFirstFiveLecturesByTitleUaLike(title);	

		return  new ArrayList<Lectures>(lecturesList);
	}

	@RequestMapping(value="/chat/lectures/get_five_titles_like/", method = RequestMethod.GET)
	@ResponseBody
	public String getLecturesTitlesLike(@RequestParam String title) throws JsonProcessingException {


		List<String> lecturesList = new ArrayList<>();		
		int lang = chatLangService.getCurrentLangInt();

		if (lang == lecturesService.EN)
			lecturesList = lecturesService.getFirstFiveLecturesTitlesByTitleEnLike(title);
		else
			if (lang == lecturesService.RU)
				lecturesList = lecturesService.getFirstFiveLecturesTitlesByTitleRuLike(title);
		if (lang == lecturesService.UA)
			lecturesList = lecturesService.getFirstFiveLecturesTitlesByTitleUaLike(title);	

		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(lecturesList);
		return jsonInString;
	}
}