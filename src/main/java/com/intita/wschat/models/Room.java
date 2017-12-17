package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.intita.wschat.domain.ChatRoomType;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;

/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name="ChatRoom")
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "Room.countNewMessages",
				query = "CALL count_new_messages(?1,?2)"
		),
		@NamedNativeQuery(
				name = "Room.findLastMessages",
				query = "CALL find_last_messages(?1)"
		),
		@NamedNativeQuery(
				name = "Room.findAvatars",
				query = "CALL find_rooms_avatars(?1,?2)"
		),
		@NamedNativeQuery(
				name = "Room.findChatUserRooms",
				query = "CALL find_chat_user_rooms(?1,?2,?3)"
		),
		@NamedNativeQuery(
				name = "Room.findPrivateRoomParticipant",
				query = "CALL find_private_room_participants(?1)"
		),
})
public class Room implements Serializable,Comparable<Room> {
	
	@PrePersist
	public void prePersist(){
	    
	}
	
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@OneToMany(mappedBy = "room",fetch = FetchType.LAZY)
	List< ChatUserLastRoomDate> chatUserLastRoomDate;

	@JsonView(Views.Public.class)
	private boolean active = true;

	@NotBlank
	@Size(min = 1, max = 255)
	@Column(unique = false)
	@JsonView(Views.Public.class)
	private String name;

	@NotNull
	@JsonView(Views.Public.class)
	private short type;

	@ManyToOne( fetch = FetchType.LAZY)
	private ChatUser author;

	@OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
	private List<UserMessage> messages = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	private Set<ChatUser> users = new HashSet<>();	

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "room")
	List<BotAnswer> botAnswers = new ArrayList<BotAnswer>();

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "room")
	PrivateRoomInfo privateInfo;

	//@ManyToOne
	//private List<RoomConfig> roomConfigs;
	/*
	 * GET/SET
	 */

	public int getParticipantsCount(){
		return users.size()+1; //+1 because author is also participants;
	}
	public List<UserMessage> getMessages() {
		return messages;
	}


	public List<BotAnswer> getBotAnswers() {
		return botAnswers;
	}

	public Set<ChatUser> getUsers() {
		return users;
	}
	public Set<ChatUser> cloneChatUsers(){
		Set<ChatUser> chatUsers = new HashSet<ChatUser>();
		for (ChatUser u : users){
			chatUsers.add(u);
		}
		return chatUsers;
	}

	public boolean addUser(ChatUser user) {
		return users.add(user);
	}
	public boolean addUsers(ArrayList<ChatUser> users) {
		return this.users.addAll(users);
	}
	public boolean removeUser(ChatUser user) {
		return users.remove(user);
	}
	public void clearUser() {
		users.clear();
	}
	public Room()
	{

	}
	public Room(long id)
	{
		setId(id);
	}
	public ChatUser getAuthor() {
		return author;
	}


	public void setAuthor(ChatUser autor) {
		this.author = autor;
	}

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
	public static List<String> getRoomsNames(Iterable<Room> rooms){
		List<String> result = new ArrayList<String>();
		for (Room room : rooms){
			result.add(room.getName());
		}
		return result;
	}

	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	@JsonIgnore
	public List<ChatUserLastRoomDate> getChatUserLastRoomDate() {
		return chatUserLastRoomDate;
	}
	@JsonIgnore
	public void setChatUserLastRoomDate(List<ChatUserLastRoomDate> chatUserLastRoomDate) {
		this.chatUserLastRoomDate = chatUserLastRoomDate;
	}	
	@Override
	public String toString() {
		return "ChatMessage ";
	}
	public short getType() {
		return type;
	}
	public ChatRoomType getTypeEnum(){
		return ChatRoomType.valueOf(type);
	}
	public void setType(ChatRoomType type) {
		this.type = (short)type.getValue();
	}
	public void setType(short type) {
		this.type = type;
	}

	@Override
	public int compareTo(Room o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
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
		Room other = (Room) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public static Room forId(Long id){
		Room room = new Room();
		room.id = id;
		return room;
	}

}
