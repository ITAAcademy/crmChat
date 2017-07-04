package com.intita.wschat.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConsultationRatings;
import com.intita.wschat.models.ChatConsultation;
import com.intita.wschat.models.ChatConsultationResult;
import com.intita.wschat.models.ChatConsultationResultValue;
import com.intita.wschat.models.IntitaConsultation;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.User.Permissions;
import com.intita.wschat.repositories.ConsultationRatingsRepository;
import com.intita.wschat.repositories.ConsultationRepository;
import com.intita.wschat.repositories.ConsultationResultRepository;
import com.intita.wschat.repositories.ConsultationResultValuesRepository;
import com.intita.wschat.repositories.IntitaConsultationRepository;
import com.intita.wschat.repositories.UserRepository;

@Service
public class ConsultationsRatingsService {

	@Autowired private UserRepository usersRepo;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private RoomsService chatRoomsService;
	@Autowired private ConsultationRepository chatConsultationRepository;
	@Autowired private IntitaConsultationRepository chatIntitaConsultationRepository;
	@Autowired private ConsultationRatingsRepository chatConsultationRatingsRepository;
	@Autowired private ConsultationResultRepository chatConsultationResultRepository;
	@Autowired private ConsultationResultValuesRepository chatConsultationResultValuesRepository;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired private ChatUserLastRoomDateService chatLastRoomDateService;

	@PostConstruct
	@Transactional
	public void postConstruct() {

	}

	@Transactional
	public Set<ConsultationRatings> getAllSupportedRetings()
	{
		return chatConsultationRatingsRepository.findAll();
	}
	
	@Transactional
	public void addRetings(Room room, ChatUser user, List<ChatConsultationResultValue> values)
	{
		ChatConsultationResult consultationResult = new ChatConsultationResult(room, user, values);
		consultationResult = chatConsultationResultRepository.save(consultationResult);		
		for(ChatConsultationResultValue value : values)
			value.setResult(consultationResult);
		chatConsultationResultValuesRepository.save(values);
	}
	
	@Transactional
	public ArrayList<ChatConsultationResult> getRetingsByRoomNameLike(String roomNameContains)
	{
		return chatConsultationResultRepository.findAllByRoomNameLike(roomNameContains);
	}
	
	@Transactional
	public ArrayList<ChatConsultationResult> getRetingsByRoomAndDates(Room room, Date before, Date after)
	{
		return chatConsultationResultRepository.findAllByRoomAndDateBeforeAndDateAfter(room, before, after);
	}
}

