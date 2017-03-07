package com.intita.wschat.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomHistory;
import com.intita.wschat.repositories.RoomHistoryRepository;

@Service
public class RoomHistoryService {
@Autowired
RoomHistoryRepository historyRepository;

	public void clearRoomHistory(Room room, ChatUser chatUser){
		RoomHistory clearedRoomHistory;
		clearedRoomHistory = historyRepository.findByRoomIdAndChatUserId(room.getId(), chatUser.getId());
		if (clearedRoomHistory==null){
			clearedRoomHistory = new RoomHistory();
		}
		clearedRoomHistory.setChatUserId(chatUser.getId());
		clearedRoomHistory.setRoomId(room.getId());
		clearedRoomHistory.setClearTime(new Date());
		historyRepository.save(clearedRoomHistory);
	}
	public Date getHistoryClearDate(Long roomId, Long chatUserId){
		RoomHistory roomHistoryCleared = historyRepository.findByRoomIdAndChatUserId(roomId,chatUserId);
		return roomHistoryCleared==null ? null : roomHistoryCleared.getClearTime();
	}
}
