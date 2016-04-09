package com.intita.wschat.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;
@Entity(name = "course")
public class Course implements Serializable,Comparable<ChatConsultation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	public int compareTo(ChatConsultation arg0) {
		if (arg0==null)return -1;
		return this.getId().compareTo(arg0.getId());
	}
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	@Column(name="course_ID")
	private Long id;
	
	String alias;
	String language;
	@Column(name="title_ua")
	String titleUa;
	@Column(name="title_ru")
	String titleRu;
	@Column(name="title_en")
	String titleEn;
	int level;
	Date start;
	short status;
	@Column(name="modules_count")
	int modulesCount;
	@Column(name="course_price")
	int coursePrice;
	
	@Transient
	@Column(name="for_whom_ua")
	String forWhomUa;
	@Transient
	@Column(name="for_whom_ru")
	String forWhomRu;
	@Transient
	@Column(name="for_whom_en")
	String forWhomEn;
	
	@Transient
	@Column(name="what_you_learn_ua")
	String whatYouLearnUa;
	@Transient
	@Column(name="what_you_learn_ru")
	String whatYouLearnRu;
	@Transient
	@Column(name="what_you_learn_en")
	String whatYouLearnEn;
	
	@Transient
	@Column(name="what_you_get_ua")
	String whatYouGetUa;
	@Transient
	@Column(name="what_you_get_ru")
	String whatYouGetRu;
	@Transient
	@Column(name="what_you_get_en")
	String whatYouGetEn;
	
	@Transient
	@Column(name="course_img")
	String courseImg;
	
	@Transient
	int rating;
	@Transient
	boolean cancelled;
	@Transient
	@Column(name="course_number")
	int courseNumber;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getTitleUa() {
		return titleUa;
	}
	public void setTitleUa(String titleUa) {
		this.titleUa = titleUa;
	}
	public String getTitleRu() {
		return titleRu;
	}
	public void setTitleRu(String titleRu) {
		this.titleRu = titleRu;
	}
	public String getTitleEn() {
		return titleEn;
	}
	public void setTitleEn(String titleEn) {
		this.titleEn = titleEn;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public short getStatus() {
		return status;
	}
	public void setStatus(short status) {
		this.status = status;
	}
	public int getModulesCount() {
		return modulesCount;
	}
	public void setModulesCount(int modulesCount) {
		this.modulesCount = modulesCount;
	}
	public int getCoursePrice() {
		return coursePrice;
	}
	public void setCoursePrice(int coursePrice) {
		this.coursePrice = coursePrice;
	}
	public String getForWhomUa() {
		return forWhomUa;
	}
	public void setForWhomUa(String forWhomUa) {
		this.forWhomUa = forWhomUa;
	}
	public String getForWhomRu() {
		return forWhomRu;
	}
	public void setForWhomRu(String forWhomRu) {
		this.forWhomRu = forWhomRu;
	}
	public String getForWhomEn() {
		return forWhomEn;
	}
	public void setForWhomEn(String forWhomEn) {
		this.forWhomEn = forWhomEn;
	}
	public String getWhatYouLearnUa() {
		return whatYouLearnUa;
	}
	public void setWhatYouLearnUa(String whatYouLearnUa) {
		this.whatYouLearnUa = whatYouLearnUa;
	}
	public String getWhatYouLearnRu() {
		return whatYouLearnRu;
	}
	public void setWhatYouLearnRu(String whatYouLearnRu) {
		this.whatYouLearnRu = whatYouLearnRu;
	}
	public String getWhatYouLearnEn() {
		return whatYouLearnEn;
	}
	public void setWhatYouLearnEn(String whatYouLearnEn) {
		this.whatYouLearnEn = whatYouLearnEn;
	}
	public String getWhatYouGetUa() {
		return whatYouGetUa;
	}
	public void setWhatYouGetUa(String whatYouGetUa) {
		this.whatYouGetUa = whatYouGetUa;
	}
	public String getWhatYouGetRu() {
		return whatYouGetRu;
	}
	public void setWhatYouGetRu(String whatYouGetRu) {
		this.whatYouGetRu = whatYouGetRu;
	}
	public String getWhatYouGetEn() {
		return whatYouGetEn;
	}
	public void setWhatYouGetEn(String whatYouGetEn) {
		this.whatYouGetEn = whatYouGetEn;
	}
	public String getCourseImg() {
		return courseImg;
	}
	public void setCourseImg(String courseImg) {
		this.courseImg = courseImg;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	public int getCourseNumber() {
		return courseNumber;
	}
	public void setCourseNumber(int courseNumber) {
		this.courseNumber = courseNumber;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
