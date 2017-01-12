package com.intita.wschat.models;

import java.io.Serializable;
import javax.persistence.*;


import java.util.Date;


/**
 * The persistent class for the offline_students database table.
 * 
 */
@Entity
@Table(name="offline_students")
@NamedQuery(name="OfflineStudent.findAll", query="SELECT o FROM OfflineStudent o")
public class OfflineStudent implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	@Column(name="assigned_by")
	private int assignedBy;

	@Column(name="cancelled_by")
	private int cancelledBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="end_date")
	private Date endDate;

	@Temporal(TemporalType.DATE)
	@Column(name="graduate_date")
	private Date graduateDate;

	@Column(name="id_user")
	private int idUser;

	@Temporal(TemporalType.DATE)
	@Column(name="start_date")
	private Date startDate;

	//bi-directional many-to-one association to OfflineSubgroup
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="id_subgroup")
	private OfflineSubGroup offlineSubgroup;

	public OfflineStudent() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAssignedBy() {
		return this.assignedBy;
	}

	public void setAssignedBy(int assignedBy) {
		this.assignedBy = assignedBy;
	}

	public int getCancelledBy() {
		return this.cancelledBy;
	}

	public void setCancelledBy(int cancelledBy) {
		this.cancelledBy = cancelledBy;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getGraduateDate() {
		return this.graduateDate;
	}

	public void setGraduateDate(Date graduateDate) {
		this.graduateDate = graduateDate;
	}

	public int getIdUser() {
		return this.idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public OfflineSubGroup getOfflineSubgroup() {
		return this.offlineSubgroup;
	}

	public void setOfflineSubgroup(OfflineSubGroup offlineSubgroup) {
		this.offlineSubgroup = offlineSubgroup;
	}

}