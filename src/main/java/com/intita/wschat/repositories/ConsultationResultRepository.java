package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatConsultation;
import com.intita.wschat.models.ChatConsultationResult;
import com.intita.wschat.models.Lang;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 
public interface ConsultationResultRepository extends CrudRepository<ChatConsultationResult, Long> {
	ArrayList<ChatConsultationResult> findAllByRoomNameLike(String nameLike);
	ArrayList<ChatConsultationResult> findAllByRoomAndDateBeforeAndDateAfter(Room room, Date before, Date after);
}
