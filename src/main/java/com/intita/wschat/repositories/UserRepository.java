package com.intita.wschat.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.User;


//http://docs.spring.io/spring-data/jpa/docs/1.4.3.RELEASE/reference/html/jpa.repositories.html
@Qualifier("IntitaConf") 
public interface UserRepository extends CrudRepository<User, Long> {
   User findByLogin(String login);
  //User findByEmail(String email);
  Page<User> findById(Long id, Pageable pageable);
  Page<User> findAll(Pageable pageable);
  List<User> findFirst10ByIdNotIn(List<Long> users);
//  List<User> findFirst5ByLoginAndByPassword( String users, String login);
  List<User> findFirst5ByLoginNotInAndLoginLike( List<String> users, String login);
  @Query(value = "SELECT * FROM USER_ADMIN WHERE ID_USER = ?1", nativeQuery = true)
  Object findInAdminTable(String userId);
 
}