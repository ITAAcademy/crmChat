package com.intita.wschat.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;

/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name="chat_room_permissions")
public class RoomPermissions implements Serializable,Comparable<RoomPermissions> {

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;
	int permissions;

	public RoomPermissions(){
		
	}
	public RoomPermissions(Room room, ChatUser chatUser,int permissions){
		this.room = room;
		this.chatUser = chatUser;
		this.permissions = permissions;
	}
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
	
	public boolean isActual(){
		if(end_date==null) return true;
		return ((new Date().getTime()) >= start_date.getTime() && (new Date().getTime()) <= end_date.getTime());
	}
	
	@Override
	public int compareTo(RoomPermissions arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	 public void addPermission(ChatUser chatUser,Permission permission){
		 permissions |= permission.getValue();
	 }
	public enum Permission{	
		ADD_USER(1),
		REMOVE_USER(2);
		private int value;
		public static Map<String, Integer> getSupported(){
			Map<String, Integer> aMap = new HashMap<>();
			for (Permission permission : Permission.values()){
				aMap.put(permission.toString(),permission.getValue());
			}
			return aMap;
		}
		public  boolean checkNumberForThisPermission(Integer number){
			return (number & getValue()) == Permission.ADD_USER.getValue();
		}
		private Permission(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public int getPermissions() {
		return permissions;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}
	
	/*
	 * GET/SET
	 */

	
}
