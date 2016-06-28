-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Хост: 127.0.0.1
-- Время создания: Июн 17 2016 г., 14:33
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
-- Структура таблицы `user_tenant_categories`
--

CREATE TABLE IF NOT EXISTS `user_tenant_categories` (
  `user_tenant_id` int(11) NOT NULL,
  `bot_category_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_tenant_id`,`bot_category_id`),
  KEY `bot_category_id` (`bot_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Дамп данных таблицы `user_tenant_categories`
--

INSERT INTO `user_tenant_categories` (`user_tenant_id`, `bot_category_id`) VALUES
(1, 1),
(3, 1),
(11, 1),
(9, 2),
(14, 2),
(3, 3);

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `user_tenant_categories`
--
ALTER TABLE `user_tenant_categories`
  ADD CONSTRAINT `user_tenant_categories_ibfk_1` FOREIGN KEY (`user_tenant_id`) REFERENCES `user_tenant` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `user_tenant_categories_ibfk_2` FOREIGN KEY (`bot_category_id`) REFERENCES `bot_category` (`id`) ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
