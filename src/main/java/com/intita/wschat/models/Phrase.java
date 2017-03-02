package com.intita.wschat.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.web.ChatController;

/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name="chat_phrases")
public class Phrase implements Serializable,Comparable<ChatUser> {

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	@Column(unique = false)
	private String text_ua;
	private String text_ru;
	private String text_en;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText(String lang) {
		switch(lang){
		case "en":
			return text_en;
		case "ru":
			return text_ru;
		case "ua":
				return text_ua;
		default:
			return text_ua;
		}		
	}
	public String getText() {
		return getText(ChatLangService.getCurrentLang());
	}

	public void setText(String text,String lang) {
		switch(lang){
		case "en":
			text_en=text;
		case "ru":
			text_ru = text;
		case "ua":
			text_ua = text;
		default:
			text_ua = text;
		}	
	}
	public void setText(String text) {
		setText(text, ChatLangService.getCurrentLang());
	}

	@Override
	public int compareTo(ChatUser arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
