package com.intita.wschat.models;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * The persistent class for the offline_subgroups database table.
 * 
 */
@Entity
@Table(name = "offline_subgroups")

public class OfflineSubGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Integer id;

	private String data;

	@Column(name = "`group`")
	private Integer group_id;

	@Column(name = "id_trainer")
	private Integer idTrainer;

	@Column(name = "id_user_created")
	private Integer idUserCreated;

	@Column(name = "id_user_curator")
	private Integer idUserCurator;

	private String name;

	// bi-directional many-to-one association to OfflineStudent
	@OneToMany(mappedBy = "offlineSubgroup", fetch = FetchType.LAZY)
	private List<OfflineStudent> offlineStudents;

	@OneToOne()
	private Room chatRoom;

	public OfflineSubGroup() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Integer getGroup_id() {
		return group_id;
	}

	public void setGroup_id(Integer group_id) {
		this.group_id = group_id;
	}

	public Integer getIdTrainer() {
		return idTrainer;
	}

	public void setIdTrainer(Integer idTrainer) {
		this.idTrainer = idTrainer;
	}

	public Integer getIdUserCreated() {
		return idUserCreated;
	}

	public void setIdUserCreated(Integer idUserCreated) {
		this.idUserCreated = idUserCreated;
	}

	public Integer getIdUserCurator() {
		return idUserCurator;
	}

	public void setIdUserCurator(Integer idUserCurator) {
		this.idUserCurator = idUserCurator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<OfflineStudent> getOfflineStudents() {
		return offlineStudents;
	}

	public void setOfflineStudents(List<OfflineStudent> offlineStudents) {
		this.offlineStudents = offlineStudents;
	}

	public Room getChatRoom() {
		return chatRoom;
	}

	public void setChatRoom(Room chatRoom) {
		this.chatRoom = chatRoom;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OfflineSubGroup other = (OfflineSubGroup) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}