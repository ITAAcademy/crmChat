package com.intita.wschat.repositories;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomConfig;
import com.intita.wschat.models.RoomHistory;
import org.springframework.data.repository.CrudRepository;

public interface RoomConfigRepository extends CrudRepository<RoomConfig,Long> {
RoomConfig findByRoomAndUser(Room room,ChatUser user);

}
