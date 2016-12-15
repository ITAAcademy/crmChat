-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Хост: 127.0.0.1
-- Время создания: Апр 20 2016 г., 21:26
-- Версия сервера: 5.6.17
-- Версия PHP: 5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- База данных: `qa_intita`
--

-- --------------------------------------------------------

--
-- Структура таблицы `chat_lang`
--

DROP TABLE IF EXISTS `chat_lang`;
CREATE TABLE IF NOT EXISTS `chat_lang` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lang` varchar(255) COLLATE utf8_bin NOT NULL,
  `map` text COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=4 ;

--
-- Дамп данных таблицы `chat_lang`
--

INSERT INTO `chat_lang` (`id`, `lang`, `map`) VALUES
(2, 'en', '{\n  "lables": {\n    "chatView": {\n      "goToRooms": "Діалоги",\n      "dialog": "Діалоги",\n      "participants": "Учасники",\n      "send": "Відіслати",\n      "messages": "Повідомлення",\n      "message_placeholder": "Напишіть ваше повідомлення і натисність Enter...",\n      "phrases": "Сталі вирази"\n    },\n    "roomListView": {\n      "newRoom": "Створити діалог",\n      "noMessage": "Повідомлення відсутнє",\n      "participants": "Учасники",\n      "search_placeholder": "Пошук",\n      "addRoomModal":{\n          "close":"Закрити",\n          "create":"Створити",\n          "title":"Створення діалогу",\n          "room_name_placeholder":"Назва кімнати"\n      },\n      "filters":{\n        "anonim":"Анонімні",\n        "private":"Приватні",\n        "consultation":"Консультації"\n      }\n    }\n  },\n  "ratings": {\n    "global": "Наскільки все файно",\n    "smart": "Наскільки все заумно"\n  }\n}'),
(3, 'ua', '{\n  "lables": {\n    "chatView": {\n      "goToRooms": "Діалоги",\n      "dialog": "Діалоги",\n      "participants": "Учасники",\n      "send": "Відіслати",\n      "messages": "Повідомлення",\n      "message_placeholder": "Напишіть ваше повідомлення і натисність Enter...",\n      "phrases": "Сталі вирази"\n    },\n    "roomListView": {\n      "newRoom": "Створити діалог",\n      "noMessage": "Повідомлення відсутнє",\n      "participants": "Учасники",\n      "search_placeholder": "Пошук",\n      "addRoomModal":{\n          "close":"Закрити",\n          "create":"Створити",\n          "title":"Створення діалогу",\n          "room_name_placeholder":"Назва кімнати"\n      },\n      "filters":{\n        "anonim":"Анонімні",\n        "private":"Приватні",\n        "consultation":"Консультації"\n      }\n    }\n  },\n  "ratings": {\n    "global": "Наскільки все файно",\n    "smart": "Наскільки все заумно"\n  }\n}');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
