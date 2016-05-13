package com.intita.wschat.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatConsultation;
import com.intita.wschat.models.ChatConsultationResult;
import com.intita.wschat.models.Lang;
import com.intita.wschat.models.Lectures;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 
public interface LecturesRepository extends CrudRepository<Lectures, Long> {
	public List<Lectures> findFirst5ByTitleUALike(String title);
	public List<Lectures> findFirst5ByImageLike(String image);
	public List<Lectures> findFirst5ByTitleRULike(String title);
	public List<Lectures> findFirst5ByTitleENLike(String title);
	public List<Lectures> findAll();
	public Lectures findOneByTitleUA(String title);
	public Lectures findOneByTitleUALike(String title);	
	public Lectures findOneByTitleRU(String title);
	public Lectures findOneByTitleRULike(String title);	
	public Lectures findOneByTitleEN(String title);
	public Lectures findOneByTitleENLike(String title);
	
	//public Lectures findOneByTitle_ua(String room);
}
