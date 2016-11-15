package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;

/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name="ChatRoom")
public class RoomPermitions implements Serializable,Comparable<RoomPermitions> {
	
	public static final int ADD = 1;
	public static final int REMOVE = 2;
	public static Map<String, Integer> getSupported(){
		Map<String, Integer> aMap = new HashMap<>();
		aMap.put("ADD", ADD);
		aMap.put("REMOVE", REMOVE);
		return aMap;
	}
	
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@ManyToOne
	@NotNull
	private Room room;
	
	@NotNull
	@ManyToOne
	private ChatUser chatUser;
	
	@NotNull
	Date start_date;
	
	@Null
	Date end_date;
	
	@Override
	public int compareTo(RoomPermitions arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	/*
	 * GET/SET
	 */

	
}
