/*Rewritten old functions replacers*/
DELIMITER ;
drop procedure if exists find_chat_user_rooms;
DELIMITER //
CREATE PROCEDURE find_chat_user_rooms(chat_user_id_param INT(10),IN searchQuery VARCHAR(1000),IN maxRecords INT(10))
  BEGIN
        SELECT id FROM (SELECT DISTINCT id FROM chat_room as author_room WHERE author_id = chat_user_id_param AND name LIKE CONCAT('%',searchQuery,'%')
                        UNION SELECT rooms_from_users_id FROM chat_room_users as participance_room WHERE users_id = chat_user_id_param) as all_users LIMIT maxRecords;

  END//
DELIMITER ;
drop procedure if exists count_new_messages;
DELIMITER //
CREATE PROCEDURE count_new_messages
  (IN chat_user_id_param INT(10),IN chat_room_ids_param VARCHAR(1000))
  BEGIN
    set @sql = concat(" SELECT message.room_id,count(*)
      as messages_count
     FROM chat_user_message as message WHERE message.room_id IN (", chat_room_ids_param, ")
     AND date > (SELECT last_logout FROM chat_user_last_room_date WHERE chat_user_id = ",chat_user_id_param," AND room_id = message.room_id)
     GROUP BY message.room_id;");
  PREPARE stmt FROM @sql;
  EXECUTE stmt;
  END//

DELIMITER ;
drop procedure if exists find_last_messages;
DELIMITER //
CREATE PROCEDURE find_last_messages
  (IN chat_room_ids_param VARCHAR(1000))
  BEGIN
    set @sql = concat("SELECT room.id,message.date,message.body
    FROM chat_room as room JOIN chat_user_message message ON
    message.id = (SELECT id FROM chat_user_message as message2 WHERE message2.room_id = room.id
                                                                          ORDER BY message2.date DESC,message2.id DESC LIMIT 1)
    WHERE (room.id IN (",chat_room_ids_param,"));");
    PREPARE stmt FROM @sql;
    EXECUTE stmt;

  END//

DELIMITER ;
/*New*/
DELIMITER ;
drop procedure if exists find_rooms_avatars;
DELIMITER //
CREATE PROCEDURE find_rooms_avatars
  (IN chat_user_id_param INT(10),IN chat_room_ids_param VARCHAR(1000))
  BEGIN
    set @sql = concat("
    SELECT room.id,MIN(intitaRoomUsers.avatar) FROM chat_room room
    LEFT JOIN chat_room_users roomUsers ON (room.id=rooms_from_users_id or author_id = users_id)
    LEFT JOIN user intitaRoomUsers ON (intitaRoomUsers.id = roomUsers.users_id)
    WHERE
    room.id IN (",chat_room_ids_param,")
    AND intitaRoomUsers.id != ", chat_user_id_param,
    " GROUP BY room.id;");
     PREPARE stmt FROM @sql;
    EXECUTE stmt;

  END//
DELIMITER ;