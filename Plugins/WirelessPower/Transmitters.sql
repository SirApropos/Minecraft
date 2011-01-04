-- phpMyAdmin SQL Dump
-- version 3.1.3
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jan 04, 2011 at 04:10 AM
-- Server version: 5.1.40
-- PHP Version: 5.2.9-1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `minecraft`
--

-- --------------------------------------------------------

--
-- Table structure for table `transmitters`
--

CREATE TABLE IF NOT EXISTS `transmitters` (
  `owner` varchar(20) NOT NULL,
  `blocks` varchar(44) NOT NULL,
  `blockType` bit(2) NOT NULL,
  `channel` varchar(20) NOT NULL,
  UNIQUE KEY `coords` (`blocks`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
