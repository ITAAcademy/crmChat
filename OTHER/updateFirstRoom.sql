-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Хост: 127.0.0.1
-- Время создания: Янв 15 2016 г., 12:12
-- Версия сервера: 5.6.17
-- Версия PHP: 5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- База данных: `test`
--

-- --------------------------------------------------------

--
-- Структура таблицы `chat_room`
--

CREATE TABLE IF NOT EXISTS `chat_room` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `author_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_fy1be4k30rsfiyj277sx585ev` (`author_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;


--
-- Структура таблицы `chat_room_users`
--

CREATE TABLE IF NOT EXISTS `chat_room_users` (
  `rooms_from_users_id` int(11) NOT NULL,
  `users_id` int(11) NOT NULL,
  PRIMARY KEY (`rooms_from_users_id`,`users_id`),
  KEY `FK_9ryl67smn61vqw2j0wrl3lxtw` (`users_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------


--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `chat_room`
--
ALTER TABLE `chat_room`
  ADD CONSTRAINT `FK_fy1be4k30rsfiyj277sx585ev` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`);

--
-- Ограничения внешнего ключа таблицы `chat_room_users`
--
ALTER TABLE `chat_room_users`
  ADD CONSTRAINT `FK_8uci3ndco1sna4jf1t5u33lia` FOREIGN KEY (`rooms_from_users_id`) REFERENCES `chat_room` (`id`),
  ADD CONSTRAINT `FK_9ryl67smn61vqw2j0wrl3lxtw` FOREIGN KEY (`users_id`) REFERENCES `user` (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
