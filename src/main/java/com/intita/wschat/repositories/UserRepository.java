package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.User;


//http://docs.spring.io/spring-data/jpa/docs/1.4.3.RELEASE/reference/html/jpa.repositories.html
@Qualifier("IntitaConf") 
public interface UserRepository extends CrudRepository<User, Long> {
   User findByLogin(String login);
  //User findByEmail(String email);
   User findFisrtById(Long id);
  Page<User> findById(Long id, Pageable pageable);
  Page<User> findAll(Pageable pageable);
  List<User> findFirst10ByIdNotIn(List<Long> users);
//  List<User> findFirst5ByLoginAndByPassword( String users, String login);
  List<User> findFirst5ByIdNotInAndLoginLike( List<Long> users, String login);
  List<User> findFirst5ByLoginLike(String login);
  ArrayList<User> findAllByIdIn(List<Long> users);
  @Query("select u.id from User u where u.id in ?1")
  ArrayList<Long> findAllIdsByIdIn(List<Long> users);
  
  @Query("select u from User u where u in (select c.intitaUser from chat_user c) ")
  Page<User> findAllChatUsers(Pageable pageable);
  
  @Query("select u from User u where (u.firstName like ?1 or u.secondName like ?1 or u.login like ?1) and u.id not in ?2")
  List<User> findByNickNameLikeOrLoginLikeOrFirstNameLikeOrSecondNameLikeAndNotInCustom(String nickname, String login, String login1, String login2, List<Long> users, Pageable pageable);

  @Query("select u from User u where (u.nickName like ?1 or u.firstName like ?1 or u.secondName like ?1 or u.login like ?1)")
  List<User> findByNickNameLikeOrLoginLikeOrFirstNameLikeOrSecondNameLike(String searchValue, Pageable pageable);
  
  List<User> findByNickNameLikeOrLoginLikeOrFirstNameLikeOrSecondNameLikeAndIdIn(String nickname, String login, String login1, String login2, List<Long> listIds, Pageable pageable);

  //
  
  @Query(value = "SELECT * FROM user_super_visor WHERE id_user = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1", nativeQuery = true)
  Object findInSupervisorTable(Long userId);
  
  @Query(value = "SELECT * FROM user_admin WHERE id_user = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1", nativeQuery = true)
  Object findInAdminTable(Long userId);
  
  @Query(value = "SELECT * FROM user_tenant WHERE chat_user_id = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1", nativeQuery = true)
  Object findInTenantTable(Long userId);
  
  @Query(value = "SELECT * FROM user_trainer WHERE id_user = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1", nativeQuery = true)
  Object findInTrainerTable(Long userId);
  
  @Query(value = "SELECT * FROM user_student WHERE id_user = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1", nativeQuery = true)
  Object findInStudentTable(Long userId);
  
  @Query(value = "SELECT trainer FROM trainer_student WHERE student = ?1 AND ((start_time <= NOW() AND end_time >= NOW()) OR end_time IS NULL) LIMIT 1", nativeQuery = true)
  Long getTrainerByUserId(Long userId);
  
  @Query(value = "SELECT student FROM trainer_student WHERE trainer = ?1 AND ((start_time <= NOW() AND end_time >= NOW()) OR end_time IS NULL)", nativeQuery = true)
  ArrayList<Integer> getStudentsByTeacherId(Long userId);
  
  @Query(value = "SELECT id_user FROM user_trainer WHERE ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL)", nativeQuery = true)
  HashSet<Integer> findAllTrainers();
  
  @Query(value = "SELECT chat_user_id FROM user_tenant WHERE ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL)", nativeQuery = true)
  Long[] findAllTenants();
  
  /*
   * Birthday queries
   */
  
  
  @Query(value = "SELECT id FROM user WHERE DAY(birthday) = DAY(CURDATE()) AND MONTH(birthday) = MONTH(CURDATE())", nativeQuery = true)
  Long[] findAllByBirthdayToday();
  
  ArrayList<Long> findAllByBirthday(Date date, Pageable page);
  ArrayList<Long> findAllByBirthdayAndIdIn(Date date, List<Long> users, Pageable page);
 
}