package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intita.wschat.exception.CourseNotFoundException;
import com.intita.wschat.models.Course;
import com.intita.wschat.repositories.CourseRepository;

@Service
public class CourseService {
@Autowired
CourseRepository courseRepository;
@Transactional
public List<Course> getAllCourses(){
	return courseRepository.findAll();
}
@Transactional
public Course getByAlias(String alias){
	return courseRepository.findByAlias(alias);
}
@Transactional
public Course getByTitle(String title,String lang){
	Course course = null;
	String lowcaseLang = lang.toLowerCase();
	switch(lowcaseLang){
	case "ua":
		course = courseRepository.findFirstByTitleUa(title);
		break;
	case "ru":
		course = courseRepository.findFirstByTitleRu(title);
		break;
	case "en":
		course = courseRepository.findFirstByTitleEn(title);
		break;
	}
	if (course == null ) throw new CourseNotFoundException("");
	return course;
	
}
@Transactional
public ArrayList<Course> getAllCoursesWithTitlePrefix(String prefix,String lang){
	String lowCaseLang = lang.toLowerCase();
	switch (lowCaseLang){
	case "ua": return courseRepository.findFirst5ByTitleUaLike(prefix+"%");
	case "ru": return courseRepository.findFirst5ByTitleRuLike(prefix+"%");
	case "en": return courseRepository.findFirst5ByTitleEnLike(prefix+"%");
	default:
		return new ArrayList<Course>();
	}
}
@Transactional
public ArrayList<String> getAllCoursesNamesWithTitlePrefix(String prefix,String lang){
	String lowCaseLang = lang.toLowerCase();
	ArrayList<Course> courses = getAllCoursesWithTitlePrefix(prefix,lang);
	ArrayList<String> coursesNames = new ArrayList();
	for (Course course : courses){
		switch(lowCaseLang){
		case "ua":
			coursesNames.add(course.getTitleUa());
			break;
		case "ru":
			coursesNames.add(course.getTitleRu());
			break;
		case "en":
			coursesNames.add(course.getTitleEn());
			break;
		}
	}
	return coursesNames;
}
}
