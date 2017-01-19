package com.intita.wschat.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

@Entity(name="offline_subgroups")
public class IntitaSubGroup {

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	@Column(unique = false)
	private String name;

	@NotBlank
	@Column(unique = false, name="data")
	private String info;

	@NotNull
	@Column(unique = false, name="group")
	private Long group;

	@NotNull
	Long id_user_curator;
	@NotNull
	Long id_user_created;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Long getGroup() {
		return group;
	}

	public void setGroup(Long group) {
		this.group = group;
	}

	public Long getId_user_curator() {
		return id_user_curator;
	}

	public void setId_user_curator(Long id_user_curator) {
		this.id_user_curator = id_user_curator;
	}

	public Long getId_user_created() {
		return id_user_created;
	}

	public void setId_user_created(Long id_user_created) {
		this.id_user_created = id_user_created;
	}

	
}
