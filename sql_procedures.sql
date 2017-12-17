DELIMITER ;
drop procedure if exists find_chat_user_rooms;
DELIMITER //
CREATE PROCEDURE find_chat_user_rooms(IN chat_user_id INT(10))
  BEGIN
    DROP TEMPORARY TABLE IF EXISTS chat_user_rooms_temp;
    CREATE TEMPORARY TABLE IF NOT EXISTS  chat_user_rooms_temp (id INT(10))
      AS
        SELECT id FROM (SELECT DISTINCT id FROM chat_room as author_room WHERE author_id = chat_user_id
                        UNION SELECT rooms_from_users_id FROM chat_room_users as participance_room WHERE users_id = chat_user_id) as all_users;

    SELECT * FROM chat_user_rooms_temp;
  END//
DELIMITER ;
drop procedure if exists count_new_messages;
DELIMITER //
CREATE PROCEDURE count_new_messages
  (IN chat_user_id_param INT(10))
  BEGIN
    DECLARE o_id SMALLINT;
    DECLARE done INT DEFAULT FALSE;
    DECLARE cur1 CURSOR FOR SELECT roomId FROM chat_user_rooms_temp ;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    CALL find_chat_user_rooms(chat_user_id_param);

    SELECT message.room_id,count(*)
      as messages_count
    FROM chat_user_message as message WHERE message.room_id IN (SELECT id FROM chat_user_rooms_temp) AND date > (SELECT last_logout FROM chat_user_last_room_date WHERE chat_user_id = chat_user_id_param AND room_id = message.room_id)
    GROUP BY message.room_id;
  END//

DELIMITER ;
drop procedure if exists find_last_messages;
DELIMITER //
CREATE PROCEDURE find_last_messages
  (IN chat_user_id_param INT(10))
  BEGIN
    DECLARE o_id SMALLINT;
    DECLARE done INT DEFAULT FALSE;
    DECLARE cur1 CURSOR FOR SELECT roomId FROM chat_user_rooms_temp ;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    CALL find_chat_user_rooms(chat_user_id_param);

    SELECT room.id,message.body
    FROM chat_room as room JOIN chat_user_message message ON
                                                            message.id = (SELECT id FROM chat_user_message as message2 WHERE message2.room_id = room.id
                                                                          ORDER BY message2.date DESC,message2.id DESC LIMIT 1)
    WHERE (room.id IN (SELECT id FROM chat_user_rooms_temp as user_rooms));
  END//

DELIMITER ;
CALL find_last_messages(583);
CALL find_private_room_participants('2241');