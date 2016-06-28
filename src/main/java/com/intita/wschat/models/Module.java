package com.intita.wschat.models;


import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;


/**
 * 
 * @author Samoenko Yuar
 */
@Entity(name="module")
public class Module  {	

	@Id
	@GeneratedValue
	@Column(name="module_ID")
	private Long id;

	String title_ua;

	String title_ru;

	String title_en;

	@Size(min = 0, max = 5)
	String language;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle_ua() {
		return title_ua;
	}

	public void setTitle_ua(String title_ua) {
		this.title_ua = title_ua;
	}

	public String getTitle_ru() {
		return title_ru;
	}

	public void setTitle_ru(String title_ru) {
		this.title_ru = title_ru;
	}

	public String getTitle_en() {
		return title_en;
	}

	public void setTitle_en(String title_en) {
		this.title_en = title_en;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}




}
