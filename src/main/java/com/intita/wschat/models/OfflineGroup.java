package com.intita.wschat.models;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Null;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the offline_groups database table.
 * 
 */
@Entity
@Table(name="offline_groups")
@NamedQuery(name="OfflineGroup.findAll", query="SELECT o FROM OfflineGroup o")
public class OfflineGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	private int city;

	@Column(name="id_user_created")
	private Long idUserCreated;

	@Column(name="id_user_curator")
	private Long idUserCurator;

	private String name;

	private int specialization;

	@Temporal(TemporalType.DATE)
	@Column(name="start_date")
	private Date startDate;
	
	
	@OneToOne
	private Room chatRoom;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
	private List<OfflineSubGroup> subGroups;


	public List<OfflineSubGroup> getSubGroups() {
		return subGroups;
	}

	public void setSubGroups(List<OfflineSubGroup> subGroups) {
		this.subGroups = subGroups;
	}

	public Room getChatRoom() {
		return chatRoom;
	}

	public void setChatRoom(Room chatRoom) {
		this.chatRoom = chatRoom;
	}

	public OfflineGroup() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCity() {
		return this.city;
	}

	public void setCity(int city) {
		this.city = city;
	}

	public Long getIdUserCreated() {
		return this.idUserCreated;
	}

	public void setIdUserCreated(Long idUserCreated) {
		this.idUserCreated = idUserCreated;
	}

	public Long getIdUserCurator() {
		return this.idUserCurator;
	}

	public void setIdUserCurator(Long idUserCurator) {
		this.idUserCurator = idUserCurator;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSpecialization() {
		return this.specialization;
	}

	public void setSpecialization(int specialization) {
		this.specialization = specialization;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

}