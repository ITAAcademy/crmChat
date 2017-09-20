SET NAMES 'utf8';
CREATE TABLE chat_bookmark_user_message (
    id int(20) NOT NULL AUTO_INCREMENT,
    chat_user_id int(11) NOT NULL,
    chat_room_id int(11) NOT NULL,
    chat_message_id int(11) NOT NULL,
    PRIMARY KEY (id),
   CONSTRAINT FK_bookmark_user FOREIGN KEY (chat_user_id) REFERENCES chat_user(id),
   CONSTRAINT FK_bookmark_room  FOREIGN KEY (chat_room_id) REFERENCES chat_room(id),
   CONSTRAINT FK_bookmark_message  FOREIGN KEY (chat_message_id) REFERENCES chat_user_message(id),
   CONSTRAINT UC_bookmark UNIQUE (chat_user_id,chat_message_id)
);