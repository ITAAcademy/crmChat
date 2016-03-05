package com.intita.wschat.models;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity(name="config")
public class ConfigParam {
	@Id
	@GeneratedValue
	Long id;
	
	@Size(min = 0, max = 50)
	String param;
	
	@Size(min = 0, max = 50)
	String value;
	
	@Column(name="default")
	@Size(min = 0, max = 50)
	String standart;
	
	@Size(min = 0, max = 50)
	String label;
	
	@Size(min = 0, max = 50)
	String type;
	
	
	boolean hidden;


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getParam() {
		return param;
	}


	public void setParam(String param) {
		this.param = param;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String getStandart() {
		return standart;
	}


	public void setStandart(String standart) {
		this.standart = standart;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public boolean isHidden() {
		return hidden;
	}


	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public static HashMap listAsMap(List<ConfigParam> list){
		HashMap<String,String> resultMap = new HashMap<String,String>();
		
		for(ConfigParam confParam : list){
			if (confParam.param==null || confParam.value==null) return null;
			resultMap.put(confParam.param, confParam.value);
		}
		return resultMap;
	}
	

}
