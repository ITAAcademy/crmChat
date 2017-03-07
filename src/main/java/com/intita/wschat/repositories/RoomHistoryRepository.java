package com.intita.wschat.repositories;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.RoomHistory;

public interface RoomHistoryRepository extends CrudRepository<RoomHistory,Long> {
	public RoomHistory findByRoomIdAndChatUserId(Long roomId, Long chatUserId);
}
