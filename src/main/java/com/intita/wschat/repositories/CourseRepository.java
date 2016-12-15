package com.intita.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.Course;

public interface CourseRepository extends CrudRepository<Course, Long> {
public ArrayList<Course> findAll();
public ArrayList<Course> findFirst5ByTitleUaLike(String likeStr);
public ArrayList<Course> findFirst5ByTitleRuLike(String likeStr);
public ArrayList<Course> findFirst5ByTitleEnLike(String likeStr);
public Course findByAlias(String alias);
public Course findFirstByTitleUa(String titleUa);
public Course findFirstByTitleRu(String titleRu);
public Course findFirstByTitleEn(String titleEn);
//findFirst5ByLoginNotInAndLoginLike logins, login + "%"

}
