package com.intita.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.Course;

public interface CourseRepository extends CrudRepository<Course, Long> {
public ArrayList<Course> findAll();
public ArrayList<Course> findFirst5ByTitleUaLike(String likeStr);
public ArrayList<Course> findFirst5ByTitleRuLike(String likeStr);
public ArrayList<Course> findFirst5ByTitleEnLike(String likeStr);
//findFirst5ByLoginNotInAndLoginLike logins, login + "%"

}
