-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 01, 2017 at 12:05 AM
-- Server version: 5.5.54-0ubuntu0.14.04.1
-- PHP Version: 5.5.9-1ubuntu4.21

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `dystonse`
--

-- --------------------------------------------------------

--
-- Table structure for table `vehicle_positions`
--

CREATE TABLE IF NOT EXISTS `vehicle_positions` (
  `oid` int(11) NOT NULL AUTO_INCREMENT,
  `trip_id` varchar(10) DEFAULT NULL,
  `route_id` varchar(10) DEFAULT NULL,
  `trip_start_time` varchar(8) DEFAULT NULL,
  `trip_start_date` varchar(10) DEFAULT NULL,
  `vehicle_id` varchar(10) DEFAULT NULL,
  `vehicle_label` varchar(15) DEFAULT NULL,
  `vehicle_license_plate` varchar(10) DEFAULT NULL,
  `position_latitude` float DEFAULT NULL,
  `position_longitude` float DEFAULT NULL,
  `position_bearing` float DEFAULT NULL,
  `position_speed` float DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
