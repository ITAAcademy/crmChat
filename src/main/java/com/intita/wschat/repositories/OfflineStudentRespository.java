package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.OfflineGroup;
import com.intita.wschat.models.OfflineStudent;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;


public interface OfflineStudentRespository extends CrudRepository<OfflineStudent, Integer> {

	@Query(value = "SELECT id_user FROM offline_students WHERE id_subgroup = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) ", nativeQuery = true)
	  ArrayList<Integer> getStudentsIdByIdSubGroup(Integer idSubGroup);
	
	@Query(value = "SELECT id_user FROM offline_students WHERE id_subgroup IN ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) ", nativeQuery = true)
	  ArrayList<Integer> getStudentsIdByIdSubGroups(ArrayList<Integer> idSubGroup);
}