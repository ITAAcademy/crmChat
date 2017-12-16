DELIMITER ;
drop procedure if exists find_rooms_avatars;
DELIMITER //
CREATE PROCEDURE find_rooms_avatars
  (IN chat_user_id_param INT(10))
  BEGIN

    CALL find_chat_user_rooms(chat_user_id_param);

    SELECT room.id,MIN(intitaRoomUsers.avatar) FROM chat_room room
    LEFT JOIN chat_room_users roomUsers ON (room.id=rooms_from_users_id or author_id = users_id)
    LEFT JOIN user intitaRoomUsers ON (intitaRoomUsers.id = roomUsers.users_id)
    WHERE
    room.id IN (SELECT id as participanceRoomId FROM chat_user_rooms_temp)
    AND intitaRoomUsers.id != chat_user_id_param
    GROUP BY room.id;

  END//
DELIMITER ;
