package com.intita.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.IntitaSubGroup;
import com.intita.wschat.models.BotAnswer;

public interface IntitaSubGroupRespository extends CrudRepository<IntitaSubGroup, Long> {

	ArrayList<IntitaSubGroup> findAll();
	IntitaSubGroup findFirstByName(String name);
	@Query(value = "SELECT id_user FROM offline_students WHERE id_subgroup = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL)", nativeQuery = true)
	ArrayList<Long> getStudentsBySubGroupId(Long subGroupId);
}
