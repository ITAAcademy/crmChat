SET NAMES 'utf8';

SET FOREIGN_KEY_CHECKS = 0; 
TRUNCATE chat_consultations_results;
ALTER TABLE chat_consultations_results DROP FOREIGN KEY FK_dg4e7gh6m1hxjyxqkxq3qsnyd; 
ALTER TABLE chat_consultations_results  DROP COLUMN value,  DROP COLUMN consultation_id;
SET FOREIGN_KEY_CHECKS = 1; 
INSERT INTO chat_consultation_ratings (name, active) VALUES ("informative", TRUE);