package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intita.wschat.exception.CourseNotFoundException;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Course;
import com.intita.wschat.models.Lectures;
import com.intita.wschat.repositories.ChatUserRepository;
import com.intita.wschat.repositories.CourseRepository;
import com.intita.wschat.repositories.LecturesRepository;

@Service
public class LecturesService {
	
	public static final int UA = 0;
    public static final int RU = 1;
    public static final int EN = 2;
	
@Autowired
LecturesRepository lecturesRepository;


@Transactional
public List<Lectures> getAllLectures(){
	return lecturesRepository.findAll();
}

@Transactional
public Lectures getLectureByTitleUA(String title){	
	return lecturesRepository.findOneByTitleUA(title);
}

@Transactional
public Lectures getLectureByTitleRU(String title){	
	return lecturesRepository.findOneByTitleRU(title);
}

@Transactional
public Lectures getLectureByTitleEN(String title){	
	return lecturesRepository.findOneByTitleEN(title);
}

public List<Lectures> getFirstFiveLecturesByTitleUaLike(String title){
	return lecturesRepository.findFirst5ByTitleUALike(title);
}

public List<Lectures> getFirstFiveLecturesByTitleRuLike(String title){
	return lecturesRepository.findFirst5ByTitleRULike(title);
}

public List<Lectures> getFirstFiveLecturesByTitleEnLike(String title){
	return lecturesRepository.findFirst5ByTitleENLike(title);
}

public List<String> getFirstFiveLecturesTitlesByTitleUaLike(String title){
List<Lectures> lectures = lecturesRepository.findFirst5ByTitleUALike(title + "%");
	 List<String> titles = new ArrayList<>();
	 for (int i = 0; i < lectures.size(); i++)
		 titles.add(lectures.get(i).gettitleUA());
	 return titles;
}

public List<String> getFirstFiveLecturesTitlesByTitleRuLike(String title){
	List<Lectures> lectures = lecturesRepository.findFirst5ByTitleRULike(title+ "%");
	 List<String> titles = new ArrayList<>();
	 for (int i = 0; i < lectures.size(); i++)
		 titles.add(lectures.get(i).gettitleRU());
	 return titles;
}

public List<String> getFirstFiveLecturesTitlesByTitleEnLike(String title){
	List<Lectures> lectures = lecturesRepository.findFirst5ByTitleENLike(title + "%" );
	 List<String> titles = new ArrayList<>();
	 for (int i = 0; i < lectures.size(); i++)
		 titles.add(lectures.get(i).gettitleEN());
	 return titles;
}


}