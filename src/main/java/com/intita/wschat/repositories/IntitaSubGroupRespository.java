package com.intita.wschat.repositories;

import java.math.BigInteger;
import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.IntitaSubGroup;

public interface IntitaSubGroupRespository extends CrudRepository<IntitaSubGroup, Long> {

	ArrayList<IntitaSubGroup> findAll();
	IntitaSubGroup findFirstByName(String name);
	@Query(value = "SELECT id_user FROM offline_students WHERE id_subgroup = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL)", nativeQuery = true)
	ArrayList<Long> getStudentsBySubGroupId(Long subGroupId);
	@Query(value = "SELECT id_user FROM offline_students WHERE id_subgroup in (SELECT id FROM offline_subgroups WHERE id_trainer=?1)", nativeQuery = true)
	ArrayList<Integer> getStudentsByTrainer(Integer trainerUserId);
	@Query(value = "SELECT chat_room_id FROM offline_subgroups WHERE id_trainer=?1", nativeQuery = true)
	ArrayList<BigInteger> getRoomsByTrainer(Integer trainerUserId);
}
