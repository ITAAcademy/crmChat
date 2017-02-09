package com.intita.wschat.models;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Null;

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

	@ManyToOne
	@JoinColumn(name = "`group`")
	private OfflineGroup group;

	@Column(name = "id_trainer")
	private Long idTrainer;

	@Column(name = "id_user_created")
	private Long idUserCreated;

	@Column(name = "id_user_curator", nullable=true)
	private Long idUserCurator;

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

	public OfflineGroup getGroup() {
		return group;
	}

	public void setGroup(OfflineGroup group) {
		this.group = group;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Long getIdTrainer() {
		return idTrainer;
	}

	public void setIdTrainer(Long idTrainer) {
		this.idTrainer = idTrainer;
	}

	public Long getIdUserCreated() {
		return idUserCreated;
	}

	public void setIdUserCreated(Long idUserCreated) {
		this.idUserCreated = idUserCreated;
	}

	public Long getIdUserCurator() {
		return idUserCurator;
	}

	public void setIdUserCurator(Long idUserCurator) {
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