package com.intita.wschat.models;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


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
	private int idUserCreated;

	@Column(name="id_user_curator")
	private int idUserCurator;

	private String name;

	private int specialization;

	@Temporal(TemporalType.DATE)
	@Column(name="start_date")
	private Date startDate;

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

	public int getIdUserCreated() {
		return this.idUserCreated;
	}

	public void setIdUserCreated(int idUserCreated) {
		this.idUserCreated = idUserCreated;
	}

	public int getIdUserCurator() {
		return this.idUserCurator;
	}

	public void setIdUserCurator(int idUserCurator) {
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