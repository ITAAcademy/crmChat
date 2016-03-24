package com.intita.wschat.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConsultationRatings;
import com.intita.wschat.models.ChatConsultation;
import com.intita.wschat.models.IntitaConsultation;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.User.Permissions;
import com.intita.wschat.repositories.ConsultationRatingsRepository;
import com.intita.wschat.repositories.ConsultationRepository;
import com.intita.wschat.repositories.ConsultationResultRepository;
import com.intita.wschat.repositories.IntitaConsultationRepository;
import com.intita.wschat.repositories.UserRepository;

@Service
public class ConsultationsService {

	@Autowired
	private UserRepository usersRepo;
	@Autowired
	private ChatUsersService chatUsersService;
	@Autowired
	private RoomsService chatRoomsService;
	@Autowired
	private ConsultationRepository chatConsultationRepository;
	@Autowired
	private IntitaConsultationRepository chatIntitaConsultationRepository;
	@Autowired
	private ConsultationRatingsRepository chatConsultationRatingsRepository;
	@Autowired
	private ConsultationResultRepository chatConsultationResultRepository;

	@PostConstruct
	@Transactional
	public void postConstruct() {

	}
	
	public Set<ConsultationRatings> getAllSupportedRetings()
	{
		return chatConsultationRatingsRepository.findAll();
	}

	@Transactional(readOnly = false)
	public void getRoomByConsultation(ChatConsultation cons) {
		
	}
	
	@Transactional(readOnly = false)
	public void getConsultationByRoom(Room room) {
		
	}
	
	@Transactional(readOnly = false)
	public void getRoomByintitaConsultation(IntitaConsultation iCons) {
		
	}
}

