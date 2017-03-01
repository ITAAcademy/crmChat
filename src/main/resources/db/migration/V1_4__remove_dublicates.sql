
SET NAMES 'utf8';

DELETE FROM chat_room_users WHERE users_id IN (SELECT author_id FROM chat_room WHERE chat_room_users.rooms_from_users_id = id);