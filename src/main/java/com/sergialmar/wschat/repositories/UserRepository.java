package com.sergialmar.wschat.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.sergialmar.wschat.models.User;



public interface UserRepository extends CrudRepository<User, Long> {
   User findByLogin(String login);
  //User findByEmail(String email);
  Page<User> findById(Long id, Pageable pageable);
  Page<User> findAll(Pageable pageable);
  List<User> findFirst10ByIdNotIn(List<Long> users);
//  List<User> findFirst5ByLoginAndByPassword( String users, String login);
  List<User> findFirst5ByLoginNotInAndLoginLike( List<String> users, String login);
}