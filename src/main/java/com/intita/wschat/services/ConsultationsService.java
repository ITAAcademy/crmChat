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
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConsultationRatings;
import com.intita.wschat.models.ChatConsultation;
import com.intita.wschat.models.ChatConsultationResult;
import com.intita.wschat.models.IntitaConsultation;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.Room.RoomType;
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
	
	public void setRatings(ChatConsultation cons, Map<Long,Integer> map)
	{
		if(cons != null)
		{
			for (Long id: map.keySet()) {
				chatConsultationResultRepository.save(new ChatConsultationResult(id, map.get(id), cons));
			}
		}
	}
	public void update(ChatConsultation entity)
	{
		chatConsultationRepository.save(entity);
	}
	public Set<ConsultationRatings> getAllSupportedRetings()
	{
		return chatConsultationRatingsRepository.findAll();
	}

	@Transactional(readOnly = false)
	public void getRoomByConsultation(ChatConsultation cons) {
		
	}
	
	@Transactional(readOnly = false)
	public ChatConsultation getConsultationByRoom(Room room) {
		return chatConsultationRepository.findOneByRoom(room);
	}
	
	@Transactional(readOnly = false)
	public IntitaConsultation getIntitaConsultationById(Long iConsId) {
		return chatIntitaConsultationRepository.findOne(iConsId);
	}
	
	@Transactional(readOnly = false)
	public Room getRoomByIntitaConsultationId(Long iConsId) {
		return getRoomByIntitaConsultation(chatIntitaConsultationRepository.findOne(iConsId));
	}
	
	@Transactional(readOnly = false)
	public ChatConsultation getByIntitaConsultationId(Long iConsId) {
		return getByIntitaConsultation(chatIntitaConsultationRepository.findOne(iConsId));
	}
	
	@Transactional(readOnly = false)
	public Room getRoomByIntitaConsultation(IntitaConsultation iCons) {
		ChatConsultation cons = getByIntitaConsultation(iCons);
		
		if(cons == null)
			return null;
		
		return cons.getRoom();
	}
	
	@Transactional(readOnly = false)
	public ChatConsultation getByIntitaConsultation(IntitaConsultation iCons) {
		if(iCons == null)
			return null;

		ChatConsultation result = iCons.getChatConsultation();
		if(result == null)
		{
			Room consultationRoom = new Room();
			consultationRoom.setType((short) RoomType.CONSULTATION);
			consultationRoom.setAuthor(iCons.getAuthor().getChatUser());
			consultationRoom.setName("Consultation_" + new Date().toString());
			consultationRoom.setActive(false);
			chatRoomsService.addUserToRoom(iCons.getConsultant().getChatUser(), consultationRoom);
			result = new ChatConsultation(iCons, consultationRoom);
			chatConsultationRepository.save(result);
		}
		return result;
	}
}

