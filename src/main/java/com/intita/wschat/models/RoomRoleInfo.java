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
@Entity(name="chat_room_roles")
public class RoomRoleInfo implements Serializable,Comparable<RoomRoleInfo> {

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;
	
	int roleId;

	public RoomRoleInfo(){
		
	}
	public RoomRoleInfo(Room room, int roleId){
		this.room = room;
		this.roleId = roleId;
	}
	@ManyToOne
	@NotNull
	private Room room;

	@Override
	public int compareTo(RoomRoleInfo arg0) {
		// TODO Auto-generated method stub
		return new Long(this.id - arg0.getId()).intValue();
	}
	
	/*
	 * GET/SET
	 */
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	public Room getRoom() {
		return room;
	}
	public void setRoom(Room room) {
		this.room = room;
	}
		


	
}
