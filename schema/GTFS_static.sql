-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 31, 2017 at 10:23 PM
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
-- Table structure for table `agency`
--

CREATE TABLE IF NOT EXISTS `agency` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `agency_id` varchar(255) DEFAULT NULL,
  `agency_name` varchar(255) NOT NULL,
  `agency_url` varchar(255) NOT NULL,
  `agency_timezone` varchar(50) NOT NULL,
  `agency_lang` varchar(10) DEFAULT NULL,
  `agency_phone` varchar(50) DEFAULT NULL,
  `agency_fare_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_agency_agency_id` (`agency_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=42 ;

-- --------------------------------------------------------

--
-- Table structure for table `blocks`
--

CREATE TABLE IF NOT EXISTS `blocks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sequence` int(11) DEFAULT NULL,
  `block_id` varchar(255) NOT NULL,
  `service_id` varchar(255) NOT NULL,
  `trip_id` varchar(255) NOT NULL,
  `prev_trip_id` varchar(255) DEFAULT NULL,
  `next_trip_id` varchar(255) DEFAULT NULL,
  `start_stop_id` varchar(255) NOT NULL,
  `end_stop_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_blocks_end_stop_id` (`end_stop_id`),
  KEY `ix_blocks_trip_id` (`trip_id`),
  KEY `ix_blocks_start_stop_id` (`start_stop_id`),
  KEY `ix_blocks_block_id` (`block_id`),
  KEY `ix_blocks_service_id` (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `calendar`
--

CREATE TABLE IF NOT EXISTS `calendar` (
  `service_id` varchar(255) NOT NULL,
  `monday` tinyint(1) NOT NULL,
  `tuesday` tinyint(1) NOT NULL,
  `wednesday` tinyint(1) NOT NULL,
  `thursday` tinyint(1) NOT NULL,
  `friday` tinyint(1) NOT NULL,
  `saturday` tinyint(1) NOT NULL,
  `sunday` tinyint(1) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`service_id`),
  KEY `ix_calendar_service_id` (`service_id`),
  KEY `calendar_ix1` (`start_date`,`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `calendar_dates`
--

CREATE TABLE IF NOT EXISTS `calendar_dates` (
  `service_id` varchar(255) NOT NULL,
  `date` date NOT NULL,
  `exception_type` int(11) NOT NULL,
  PRIMARY KEY (`service_id`,`date`),
  KEY `ix_calendar_dates_service_id` (`service_id`),
  KEY `ix_calendar_dates_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `fare_attributes`
--

CREATE TABLE IF NOT EXISTS `fare_attributes` (
  `fare_id` varchar(255) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `currency_type` varchar(255) NOT NULL,
  `payment_method` int(11) NOT NULL,
  `transfers` int(11) DEFAULT NULL,
  `transfer_duration` int(11) DEFAULT NULL,
  `agency_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fare_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `fare_rules`
--

CREATE TABLE IF NOT EXISTS `fare_rules` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fare_id` varchar(255) NOT NULL,
  `route_id` varchar(255) DEFAULT NULL,
  `origin_id` varchar(255) DEFAULT NULL,
  `destination_id` varchar(255) DEFAULT NULL,
  `contains_id` varchar(255) DEFAULT NULL,
  `service_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_fare_rules_fare_id` (`fare_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `feed_info`
--

CREATE TABLE IF NOT EXISTS `feed_info` (
  `feed_publisher_name` varchar(255) NOT NULL,
  `feed_publisher_url` varchar(255) NOT NULL,
  `feed_lang` varchar(255) NOT NULL,
  `feed_start_date` date DEFAULT NULL,
  `feed_end_date` date DEFAULT NULL,
  `feed_version` varchar(255) DEFAULT NULL,
  `feed_license` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`feed_publisher_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `frequencies`
--

CREATE TABLE IF NOT EXISTS `frequencies` (
  `trip_id` varchar(255) NOT NULL,
  `start_time` varchar(8) NOT NULL,
  `end_time` varchar(8) DEFAULT NULL,
  `headway_secs` int(11) DEFAULT NULL,
  `exact_times` int(11) DEFAULT NULL,
  PRIMARY KEY (`trip_id`,`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `patterns`
--

CREATE TABLE IF NOT EXISTS `patterns` (
  `shape_id` varchar(255) NOT NULL,
  `pattern_dist` decimal(20,10) DEFAULT NULL,
  PRIMARY KEY (`shape_id`),
  KEY `ix_patterns_shape_id` (`shape_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `routes`
--

CREATE TABLE IF NOT EXISTS `routes` (
  `route_id` varchar(255) NOT NULL,
  `agency_id` varchar(255) DEFAULT NULL,
  `route_short_name` varchar(255) DEFAULT NULL,
  `route_long_name` varchar(255) DEFAULT NULL,
  `route_desc` varchar(1023) DEFAULT NULL,
  `route_type` int(11) NOT NULL,
  `route_url` varchar(255) DEFAULT NULL,
  `route_color` varchar(6) DEFAULT NULL,
  `route_text_color` varchar(6) DEFAULT NULL,
  `route_sort_order` int(11) DEFAULT NULL,
  `min_headway_minutes` int(11) DEFAULT NULL,
  PRIMARY KEY (`route_id`),
  KEY `ix_routes_route_id` (`route_id`),
  KEY `ix_routes_route_type` (`route_type`),
  KEY `ix_routes_route_sort_order` (`route_sort_order`),
  KEY `ix_routes_agency_id` (`agency_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `route_directions`
--

CREATE TABLE IF NOT EXISTS `route_directions` (
  `route_id` varchar(255) NOT NULL,
  `direction_id` int(11) NOT NULL,
  `direction_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`route_id`,`direction_id`),
  KEY `ix_route_directions_route_id` (`route_id`),
  KEY `ix_route_directions_direction_id` (`direction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `route_filters`
--

CREATE TABLE IF NOT EXISTS `route_filters` (
  `route_id` varchar(255) NOT NULL,
  `agency_id` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`route_id`),
  KEY `ix_route_filters_route_id` (`route_id`),
  KEY `ix_route_filters_agency_id` (`agency_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `route_stops`
--

CREATE TABLE IF NOT EXISTS `route_stops` (
  `route_id` varchar(255) NOT NULL,
  `direction_id` int(11) NOT NULL,
  `stop_id` varchar(255) NOT NULL,
  `order` int(11) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  PRIMARY KEY (`route_id`,`direction_id`,`stop_id`),
  KEY `ix_route_stops_start_date` (`start_date`),
  KEY `ix_route_stops_direction_id` (`direction_id`),
  KEY `ix_route_stops_end_date` (`end_date`),
  KEY `ix_route_stops_stop_id` (`stop_id`),
  KEY `ix_route_stops_route_id` (`route_id`),
  KEY `ix_route_stops_order` (`order`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `route_type`
--

CREATE TABLE IF NOT EXISTS `route_type` (
  `route_type` int(11) NOT NULL,
  `route_type_name` varchar(255) DEFAULT NULL,
  `route_type_desc` varchar(1023) DEFAULT NULL,
  PRIMARY KEY (`route_type`),
  KEY `ix_route_type_route_type` (`route_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `shapes`
--

CREATE TABLE IF NOT EXISTS `shapes` (
  `shape_id` varchar(255) NOT NULL,
  `shape_pt_lat` decimal(12,9) DEFAULT NULL,
  `shape_pt_lon` decimal(12,9) DEFAULT NULL,
  `shape_pt_sequence` int(11) NOT NULL,
  `shape_dist_traveled` decimal(20,10) DEFAULT NULL,
  PRIMARY KEY (`shape_id`,`shape_pt_sequence`),
  KEY `ix_shapes_shape_pt_sequence` (`shape_pt_sequence`),
  KEY `ix_shapes_shape_id` (`shape_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `stops`
--

CREATE TABLE IF NOT EXISTS `stops` (
  `stop_id` varchar(255) NOT NULL,
  `stop_code` varchar(50) DEFAULT NULL,
  `stop_name` varchar(255) NOT NULL,
  `stop_desc` varchar(255) DEFAULT NULL,
  `stop_lat` decimal(12,9) NOT NULL,
  `stop_lon` decimal(12,9) NOT NULL,
  `zone_id` varchar(50) DEFAULT NULL,
  `stop_url` varchar(255) DEFAULT NULL,
  `location_type` int(11) DEFAULT NULL,
  `parent_station` varchar(255) DEFAULT NULL,
  `stop_timezone` varchar(50) DEFAULT NULL,
  `wheelchair_boarding` int(11) DEFAULT NULL,
  `platform_code` varchar(50) DEFAULT NULL,
  `direction` varchar(50) DEFAULT NULL,
  `position` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`stop_id`),
  KEY `ix_stops_stop_id` (`stop_id`),
  KEY `ix_stops_location_type` (`location_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `stop_features`
--

CREATE TABLE IF NOT EXISTS `stop_features` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stop_id` varchar(255) NOT NULL,
  `feature_type` varchar(50) NOT NULL,
  `feature_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_stop_features_stop_id` (`stop_id`),
  KEY `ix_stop_features_feature_type` (`feature_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `stop_times`
--

CREATE TABLE IF NOT EXISTS `stop_times` (
  `trip_id` varchar(255) NOT NULL,
  `stop_id` varchar(255) NOT NULL,
  `stop_sequence` int(11) NOT NULL,
  `arrival_time` varchar(9) DEFAULT NULL,
  `departure_time` varchar(9) DEFAULT NULL,
  `stop_headsign` varchar(255) DEFAULT NULL,
  `pickup_type` int(11) DEFAULT NULL,
  `drop_off_type` int(11) DEFAULT NULL,
  `shape_dist_traveled` decimal(20,10) DEFAULT NULL,
  `timepoint` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`trip_id`,`stop_sequence`),
  KEY `ix_stop_times_trip_id` (`trip_id`),
  KEY `ix_stop_times_timepoint` (`timepoint`),
  KEY `ix_stop_times_departure_time` (`departure_time`),
  KEY `ix_stop_times_stop_id` (`stop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `transfers`
--

CREATE TABLE IF NOT EXISTS `transfers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `from_stop_id` varchar(255) DEFAULT NULL,
  `to_stop_id` varchar(255) DEFAULT NULL,
  `transfer_type` int(11) DEFAULT NULL,
  `min_transfer_time` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_transfers_transfer_type` (`transfer_type`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=82573 ;

-- --------------------------------------------------------

--
-- Table structure for table `trips`
--

CREATE TABLE IF NOT EXISTS `trips` (
  `trip_id` varchar(255) NOT NULL,
  `route_id` varchar(255) NOT NULL,
  `service_id` varchar(255) NOT NULL,
  `direction_id` int(11) DEFAULT NULL,
  `block_id` varchar(255) DEFAULT NULL,
  `shape_id` varchar(255) DEFAULT NULL,
  `trip_type` varchar(255) DEFAULT NULL,
  `trip_headsign` varchar(255) DEFAULT NULL,
  `trip_short_name` varchar(255) DEFAULT NULL,
  `bikes_allowed` int(11) DEFAULT NULL,
  `wheelchair_accessible` int(11) DEFAULT NULL,
  PRIMARY KEY (`trip_id`),
  KEY `ix_trips_block_id` (`block_id`),
  KEY `ix_trips_service_id` (`service_id`),
  KEY `ix_trips_shape_id` (`shape_id`),
  KEY `ix_trips_trip_id` (`trip_id`),
  KEY `ix_trips_route_id` (`route_id`),
  KEY `ix_trips_direction_id` (`direction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `universal_calendar`
--

CREATE TABLE IF NOT EXISTS `universal_calendar` (
  `service_id` varchar(255) NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`service_id`,`date`),
  KEY `ix_universal_calendar_service_id` (`service_id`),
  KEY `ix_universal_calendar_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
