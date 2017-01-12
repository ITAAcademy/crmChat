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
import com.intita.wschat.models.OfflineSubGroup;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;



public interface OfflineSubGroupRespository extends CrudRepository<OfflineSubGroup, Integer> {

}