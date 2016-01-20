-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Хост: 127.0.0.1
-- Время создания: Янв 20 2016 г., 19:50
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

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
-- Структура таблицы `chat_tenant`
--

CREATE TABLE IF NOT EXISTS `chat_tenant` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `chat_user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_icdfbpqt3tdjy7d8204mvdcg0` (`chat_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Структура таблицы `chat_user`
--

CREATE TABLE IF NOT EXISTS `chat_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nick_name` varchar(50) DEFAULT NULL,
  `intita_user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_6s4ybyn17xe52cnebr26kooey` (`intita_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Структура таблицы `chat_user_last_room_date`
--

CREATE TABLE IF NOT EXISTS `chat_user_last_room_date` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `last_logout` datetime DEFAULT NULL,
  `chat_user_id` int(11) DEFAULT NULL,
  `room_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_tcagriy0qhcpqgarbl77lrkel` (`chat_user_id`),
  KEY `FK_l12n7117lch4gomo1egq6wabl` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Структура таблицы `chat_user_message`
--

CREATE TABLE IF NOT EXISTS `chat_user_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `body` varchar(64000) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `author_id` int(11) DEFAULT NULL,
  `room_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_2vqyb0vwecdl9a0b1lsdr6pac` (`author_id`),
  KEY `FK_fo86b284hr4fm8kaxxbx78a40` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Структура таблицы `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `chat_room`
--
ALTER TABLE `chat_room`
  ADD CONSTRAINT `FK_fy1be4k30rsfiyj277sx585ev` FOREIGN KEY (`author_id`) REFERENCES `chat_user` (`id`);

--
-- Ограничения внешнего ключа таблицы `chat_room_users`
--
ALTER TABLE `chat_room_users`
  ADD CONSTRAINT `FK_8uci3ndco1sna4jf1t5u33lia` FOREIGN KEY (`rooms_from_users_id`) REFERENCES `chat_room` (`id`),
  ADD CONSTRAINT `FK_9ryl67smn61vqw2j0wrl3lxtw` FOREIGN KEY (`users_id`) REFERENCES `chat_user` (`id`);

--
-- Ограничения внешнего ключа таблицы `chat_tenant`
--
ALTER TABLE `chat_tenant`
  ADD CONSTRAINT `FK_icdfbpqt3tdjy7d8204mvdcg0` FOREIGN KEY (`chat_user_id`) REFERENCES `chat_user` (`id`);

--
-- Ограничения внешнего ключа таблицы `chat_user`
--
ALTER TABLE `chat_user`
  ADD CONSTRAINT `FK_6s4ybyn17xe52cnebr26kooey` FOREIGN KEY (`intita_user_id`) REFERENCES `user` (`id`);

--
-- Ограничения внешнего ключа таблицы `chat_user_last_room_date`
--
ALTER TABLE `chat_user_last_room_date`
  ADD CONSTRAINT `FK_l12n7117lch4gomo1egq6wabl` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`),
  ADD CONSTRAINT `FK_tcagriy0qhcpqgarbl77lrkel` FOREIGN KEY (`chat_user_id`) REFERENCES `chat_user` (`id`);

--
-- Ограничения внешнего ключа таблицы `chat_user_message`
--
ALTER TABLE `chat_user_message`
  ADD CONSTRAINT `FK_fo86b284hr4fm8kaxxbx78a40` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`),
  ADD CONSTRAINT `FK_2vqyb0vwecdl9a0b1lsdr6pac` FOREIGN KEY (`author_id`) REFERENCES `chat_user` (`id`);
  
  --
-- ADD TENANT
--
  INSERT INTO `chat_tenant` (`id`, `chat_user_id`) VALUES ('0', '40');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
