package com.intita.wschat.repositories;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomConfig;
import com.intita.wschat.models.RoomHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoomConfigRepository extends CrudRepository<RoomConfig,Long> {
RoomConfig findByRoomAndUser(Room room,ChatUser user);
@Query("select conf.room.id from RoomConfig conf where conf.user = ?1")
List<Long> findRoomsByUser(ChatUser User);
}
