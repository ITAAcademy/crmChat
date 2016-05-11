package com.intita.wschat.models;


import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;
import  java.sql.Time;


/**
 * 
 * @author Samoenko Yuriy
 */
@Entity(name="lectures")

public class Lectures {
	@Id
	@NotNull
	private Long id;
	
	@NotNull
	@Value("lectureImage.png")
	private String image;
	
	@NotNull
	private String alias;
	
	@Value("Null")
	private Long idModule;
	
	@Value("Null")
	private Long order;
	
	@NotNull
	@Column(name="title_ua")
	private String titleUA;
	
	@NotNull
	@Column(name="title_ru")
	private String titleRU;
	
	@NotNull
	@Column(name="title_en")
	private String titleEN;
	
	@Value("1")
	private Long idType;
	
	@Value("60")
	private Long durationInMinutes;
	
	@Value("Null")
	private String idTeacher;
	
	@NotNull
	@Value("0")
	private short isFree;
	
	@NotNull
	@Value("0")
	private short rate;
	
	@NotNull
	@Value("0")
	private short verified;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Long getIdModule() {
		return idModule;
	}

	public void setIdModule(Long idModule) {
		this.idModule = idModule;
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public String gettitleUA() {
		return titleUA;
	}

	public void settitleUA(String titleUA) {
		this.titleUA = titleUA;
	}

	public String gettitleRU() {
		return titleRU;
	}

	public void settitleRU(String titleRU) {
		this.titleRU = titleRU;
	}

	public String gettitleEN() {
		return titleEN;
	}

	public void settitleEN(String titleEN) {
		this.titleEN = titleEN;
	}

	public Long getIdType() {
		return idType;
	}

	public void setIdType(Long idType) {
		this.idType = idType;
	}

	public Long getDurationInMinutes() {
		return durationInMinutes;
	}

	public void setDurationInMinutes(Long durationInMinutes) {
		this.durationInMinutes = durationInMinutes;
	}

	public String getIdTeacher() {
		return idTeacher;
	}

	public void setIdTeacher(String idTeacher) {
		this.idTeacher = idTeacher;
	}

	public short getIsFree() {
		return isFree;
	}

	public void setIsFree(short isFree) {
		this.isFree = isFree;
	}

	public short getRate() {
		return rate;
	}

	public void setRate(short rate) {
		this.rate = rate;
	}

	public short getVerified() {
		return verified;
	}

	public void setVerified(short verified) {
		this.verified = verified;
	}
	
}
