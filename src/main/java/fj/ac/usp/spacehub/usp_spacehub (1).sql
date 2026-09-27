-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 27, 2026 at 08:12 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `usp_spacehub`
--

-- --------------------------------------------------------

--
-- Table structure for table `alternative_suggestions`
--

CREATE TABLE `alternative_suggestions` (
  `id` bigint(20) NOT NULL,
  `booking_id` bigint(20) NOT NULL,
  `room_id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `start_time` time(6) NOT NULL,
  `end_time` time(6) NOT NULL,
  `score` double NOT NULL,
  `explanation` varchar(1000) NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'PROPOSED',
  `preserve_original_on_reject` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime(6) NOT NULL DEFAULT current_timestamp(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `alternative_suggestions`
--

INSERT INTO `alternative_suggestions` (`id`, `booking_id`, `room_id`, `date`, `start_time`, `end_time`, `score`, `explanation`, `status`, `preserve_original_on_reject`, `created_at`) VALUES
(1, 3, 5, '2026-09-04', '16:00:00.000000', '18:00:00.000000', 91.5, 'Alternative test option: Multipurpose Room 006 is available after the academic conflict period.', 'PROPOSED', 0, '2026-09-01 00:39:43.349531'),
(2, 2, 4, '2026-09-04', '14:00:00.000000', '16:00:00.000000', 97.1, 'Same time in a different suitable room; TUT-021 passes all hard constraints.', 'PROPOSED', 0, '2026-09-03 11:54:41.000000'),
(3, 3, 4, '2026-09-04', '14:00:00.000000', '16:00:00.000000', 95.7, 'Same time in a different suitable room; TUT-021 passes all hard constraints.', 'PROPOSED', 0, '2026-09-03 11:54:42.000000');

-- --------------------------------------------------------

--
-- Table structure for table `audit_logs`
--

CREATE TABLE `audit_logs` (
  `id` bigint(20) NOT NULL,
  `actor_email` varchar(255) DEFAULT NULL,
  `action` varchar(255) NOT NULL,
  `entity_type` varchar(255) NOT NULL,
  `entity_id` varchar(255) DEFAULT NULL,
  `details` varchar(4000) NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT current_timestamp(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `audit_logs`
--

INSERT INTO `audit_logs` (`id`, `actor_email`, `action`, `entity_type`, `entity_id`, `details`, `created_at`) VALUES
(1, 'admin@usp.ac.fj', 'TEST_DATABASE_SEEDED', 'System', NULL, 'Development database created with rooms, courses, users, conflict requests, approved booking, maintenance, timetable and alternative suggestion test data.', '2026-09-01 00:39:43.965491'),
(2, 'admin@usp.ac.fj', 'USER_CREATED', 'User', '9', 's11232784@student.usp.ac.fj role=STUDENT active=true', '2026-09-03 11:32:45.000000'),
(3, 'lecturer1@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '3', 'No valid room for LAB on 2026-09-01 10:00-12:00', '2026-09-03 11:48:23.000000'),
(4, 'lecturer1@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '3', 'No valid room for LAB on 2026-09-01 10:00-12:00', '2026-09-03 11:48:48.000000'),
(5, 'lecturer1@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '3', 'Top room ICT-LAB-5 score=84.9 for LAB on 2026-09-04 10:00-12:00', '2026-09-03 11:49:06.000000'),
(6, 'admin@usp.ac.fj', 'CONFLICT_DETECTED', 'Booking', '1', '3 competing requests for ICT-LAB-3 2026-09-04 14:00-16:00', '2026-09-03 11:54:34.000000'),
(7, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '1', 'Recommended first with display score 91.2. Academic priority (LAB/TUTORIAL). Course need 100%, time fairness 100%, room fairness 100%, suitability 92%. A workable alternative exists.', '2026-09-03 11:54:34.000000'),
(8, 'admin@usp.ac.fj', 'BOOKING_APPROVED', 'Booking', '1', 'Approved by administrator', '2026-09-03 11:54:38.000000'),
(9, 'admin@usp.ac.fj', 'ALTERNATIVE_SUGGESTED', 'Booking', '2', 'Same time in a different suitable room; TUT-021 passes all hard constraints.', '2026-09-03 11:54:41.000000'),
(10, 'admin@usp.ac.fj', 'ALTERNATIVE_SUGGESTED', 'Booking', '3', 'Same time in a different suitable room; TUT-021 passes all hard constraints.', '2026-09-03 11:54:42.000000'),
(11, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '9', 'Recommended first with display score 82.1. Invalid due to hard constraints: Bookings cannot be made in the past. You already have another active booking request or approved booking during this time.', '2026-09-27 00:17:06.000000'),
(12, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '9', 'Recommended first with display score 82.1. Invalid due to hard constraints: Bookings cannot be made in the past. You already have another active booking request or approved booking during this time.', '2026-09-27 00:31:58.000000'),
(13, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '9', 'Recommended first with display score 82.1. Invalid due to hard constraints: Bookings cannot be made in the past. You already have another active booking request or approved booking during this time.', '2026-09-27 01:04:37.000000'),
(14, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '9', 'Recommended first with display score 82.1. Invalid due to hard constraints: Bookings cannot be made in the past. You already have another active booking request or approved booking during this time.', '2026-09-27 01:18:42.000000'),
(15, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '9', 'Recommended first with display score 82.1. Invalid due to hard constraints: Bookings cannot be made in the past. You already have another active booking request or approved booking during this time.', '2026-09-27 01:21:37.000000'),
(16, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '13', 'Recommended first with display score 98.9. Invalid due to hard constraints: Bookings cannot be made in the past. Room capacity 50 is below the expected class size of 55.', '2026-09-27 01:22:23.000000'),
(17, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '13', 'Recommended first with display score 98.9. Invalid due to hard constraints: Bookings cannot be made in the past. Room capacity 50 is below the expected class size of 55.', '2026-09-27 01:22:50.000000'),
(18, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '29', 'Recommended first with display score 95.9. Invalid due to hard constraints: Bookings cannot be made in the past. Room capacity 40 is below the expected class size of 45. Only 40 operational computers are available for 45 students. You already have another active booking request or approved booking during this time.', '2026-09-27 01:24:07.000000'),
(19, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '19', 'Recommended first with display score 85.1. Invalid due to hard constraints: Bookings cannot be made in the past.', '2026-09-27 01:24:39.000000'),
(20, 'student1@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '6', 'Top room LAB204 score=94.3 for ECA on 2026-09-27 12:00-13:00', '2026-09-27 01:42:29.000000'),
(21, 'student1@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '6', 'No valid room for ECA on 2026-09-27 12:00-13:00', '2026-09-27 01:42:53.000000'),
(22, 'student1@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '6', 'Top room ROOM-006 score=93.0 for ECA on 2026-09-27 12:00-13:00', '2026-09-27 01:43:16.000000'),
(23, 'student1@usp.ac.fj', 'BOOKING_REQUEST_CREATED', 'Booking', '31', 'Submitted after re-validating all hard constraints.', '2026-09-27 01:43:25.000000'),
(24, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '31', 'Recommended first with display score 61.0. Lower ECA priority. Course need 50%, time fairness 100%, room fairness 100%, suitability 90%. A workable alternative exists.', '2026-09-27 01:44:03.000000'),
(25, 'admin@usp.ac.fj', 'BOOKING_APPROVED', 'Booking', '31', 'Approved by administrator', '2026-09-27 01:44:08.000000'),
(26, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '19', 'Recommended first with display score 85.1. Invalid due to hard constraints: Bookings cannot be made in the past.', '2026-09-27 01:44:21.000000'),
(27, 'admin@usp.ac.fj', 'COURSE_UPDATED', 'Course', '1', 'CS214 roll=40', '2026-09-27 02:38:57.000000'),
(28, 'lecturer1@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '3', 'Top room LAB204 score=92.2 for LAB on 2026-09-27 10:00-12:00', '2026-09-27 03:35:54.000000'),
(29, 'lecturer1@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '3', 'Top room LAB204 score=94.4 for LAB on 2026-09-27 10:00-12:00', '2026-09-27 03:54:26.000000'),
(30, 'lecturer1@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '3', 'Top room LAB204 score=94.4 for LAB on 2026-09-27 08:00-12:00', '2026-09-27 04:03:50.000000'),
(31, 'admin@usp.ac.fj', 'COURSE_CREATED', 'Course', '21', 'CS111 roll=50', '2026-09-27 04:09:08.000000'),
(32, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=97.4 for LAB on 2026-10-04 10:00-12:00', '2026-09-27 04:11:35.000000'),
(33, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room CYBER-LAB score=94.4 for LAB on 2026-09-27 10:00-12:00', '2026-09-27 04:13:57.000000'),
(34, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room ROOM603 score=70.5 for ECA on 2026-09-27 10:00-12:00', '2026-09-27 04:16:45.000000'),
(35, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room CYBER-LAB score=94.4 for LAB on 2026-09-28 10:00-12:00', '2026-09-27 04:24:54.000000'),
(36, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room CYBER-LAB score=94.4 for LAB on 2026-09-28 10:00-12:00', '2026-09-27 04:41:56.000000'),
(37, 'lecturer13@usp.ac.fj', 'BOOKING_REQUEST_CREATED', 'Booking', '32', 'Submitted after re-validating all hard constraints.', '2026-09-27 04:42:00.000000'),
(38, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '32', 'Recommended first with display score 92.5. Academic priority (LAB/TUTORIAL). Course need 100%, time fairness 100%, room fairness 100%, suitability 100%. A workable alternative exists.', '2026-09-27 04:42:41.000000'),
(39, 'admin@usp.ac.fj', 'BOOKING_REJECTED', 'Booking', '32', 'irrelevant booking', '2026-09-27 04:43:35.000000'),
(40, 'lecturer13@usp.ac.fj', 'TIMETABLE_ENTRY_CREATED', 'ScheduleEntry', '8', 'Consultation', '2026-09-27 04:46:32.000000'),
(41, 'admin@usp.ac.fj', 'USER_CREATED', 'User', '20', 'shivalsharma00@gmail.com role=STUDENT active=true', '2026-09-27 14:18:22.000000'),
(42, 'itadmin@usp.ac.fj', 'ROOM_MAINTENANCE_ADDED', 'Room', '20', 'Equipment repair', '2026-09-27 14:22:29.000000'),
(43, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for LAB on 2026-09-28 10:00-12:00', '2026-09-27 14:23:07.000000'),
(44, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for LAB on 2026-09-28 10:00-12:00', '2026-09-27 14:23:56.000000'),
(45, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for LAB on 2026-09-28 10:00-12:00', '2026-09-27 14:24:12.000000'),
(46, 'admin@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '1', 'No valid room for LAB on 2026-09-28 10:00-12:00', '2026-09-27 14:26:01.000000'),
(47, 'admin@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '1', 'No valid room for LAB on 2026-09-28 10:00-12:00', '2026-09-27 14:31:54.000000'),
(48, 'admin@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '1', 'No valid room for LAB on 2026-10-04 10:00-12:00', '2026-09-27 14:32:23.000000'),
(49, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for LAB on 2026-09-28 10:00-12:00', '2026-09-27 14:33:32.000000'),
(50, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for TUTORIAL on 2026-09-28 10:00-12:00', '2026-09-27 14:34:06.000000'),
(51, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for LAB on 2026-09-28 10:00-12:00', '2026-09-27 14:34:39.000000'),
(52, 'lecturer13@usp.ac.fj', 'BOOKING_REQUEST_CREATED', 'Booking', '33', 'Submitted after re-validating all hard constraints.', '2026-09-27 14:34:44.000000'),
(53, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room CYBER-LAB score=94.4 for LAB on 2026-09-29 10:00-12:00', '2026-09-27 14:35:07.000000'),
(54, 'lecturer13@usp.ac.fj', 'BOOKING_REQUEST_CREATED', 'Booking', '34', 'Submitted after re-validating all hard constraints.', '2026-09-27 14:35:14.000000'),
(55, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room CYBER-LAB score=94.4 for LAB on 2026-09-30 10:00-12:00', '2026-09-27 14:35:43.000000'),
(56, 'lecturer13@usp.ac.fj', 'BOOKING_REQUEST_CREATED', 'Booking', '35', 'Submitted after re-validating all hard constraints.', '2026-09-27 14:35:52.000000'),
(57, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '33', 'Recommended first with display score 92.0. Academic priority (LAB/TUTORIAL). Course need 100%, time fairness 100%, room fairness 100%, suitability 97%. A workable alternative exists.', '2026-09-27 14:36:32.000000'),
(58, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '33', 'Recommended first with display score 92.0. Academic priority (LAB/TUTORIAL). Course need 100%, time fairness 100%, room fairness 100%, suitability 97%. A workable alternative exists.', '2026-09-27 14:37:08.000000'),
(59, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '33', 'Recommended first with display score 92.0. Academic priority (LAB/TUTORIAL). Course need 100%, time fairness 100%, room fairness 100%, suitability 97%. A workable alternative exists.', '2026-09-27 14:50:09.000000'),
(60, 'lecturer13@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '14', 'No valid room for TUTORIAL on 2026-10-04 10:00-12:00', '2026-09-27 14:50:40.000000'),
(61, 'lecturer13@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '14', 'No valid room for TUTORIAL on 2026-09-29 10:00-12:00', '2026-09-27 14:50:59.000000'),
(62, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room CYBER-LAB score=94.4 for TUTORIAL on 2026-10-07 10:00-12:00', '2026-09-27 14:51:21.000000'),
(63, 'lecturer13@usp.ac.fj', 'BOOKING_REQUEST_CREATED', 'Booking', '36', 'Submitted after re-validating all hard constraints.', '2026-09-27 14:51:46.000000'),
(64, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '33', 'Recommended first with display score 92.0. Academic priority (LAB/TUTORIAL). Course need 100%, time fairness 100%, room fairness 100%, suitability 97%. A workable alternative exists.', '2026-09-27 14:52:10.000000'),
(65, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '33', 'Recommended first with display score 92.0. Academic priority (LAB/TUTORIAL). Course need 100%, time fairness 100%, room fairness 100%, suitability 97%. A workable alternative exists.', '2026-09-27 14:52:42.000000'),
(66, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '35', 'Recommended first with display score 92.0. Academic priority (LAB/TUTORIAL). Course need 100%, time fairness 100%, room fairness 100%, suitability 97%. A workable alternative exists.', '2026-09-27 15:03:35.000000'),
(67, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '9', 'Recommended first with display score 95.4. Invalid due to hard constraints: Bookings cannot be made in the past. You already have another active booking request or approved booking during this time.', '2026-09-27 15:04:15.000000'),
(68, 'admin@usp.ac.fj', 'APPROVAL_RECOMMENDATION', 'Booking', '9', 'Recommended first with display score 95.4. Invalid due to hard constraints: Bookings cannot be made in the past. You already have another active booking request or approved booking during this time.', '2026-09-27 15:57:44.000000'),
(69, 'admin@usp.ac.fj', 'BOOKING_APPROVED', 'Booking', '36', 'Approved by administrator', '2026-09-27 16:15:52.000000'),
(70, 'admin@usp.ac.fj', 'BOOKING_APPROVED', 'Booking', '35', 'Approved by administrator', '2026-09-27 16:16:02.000000'),
(71, 'admin@usp.ac.fj', 'BOOKING_APPROVED', 'Booking', '34', 'Approved by administrator', '2026-09-27 16:16:07.000000'),
(72, 'admin@usp.ac.fj', 'BOOKING_APPROVED', 'Booking', '33', 'Approved by administrator', '2026-09-27 16:16:16.000000'),
(73, 'lecturer13@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '14', 'No valid room for LAB on 2026-10-01 10:00-12:00', '2026-09-27 16:16:55.000000'),
(74, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for LAB on 2026-10-06 10:00-12:00', '2026-09-27 16:17:10.000000'),
(75, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for LAB on 2026-10-08 10:00-12:00', '2026-09-27 16:17:30.000000'),
(76, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room LAB201 score=92.2 for LAB on 2026-10-08 10:00-12:00', '2026-09-27 16:18:01.000000'),
(77, 'lecturer13@usp.ac.fj', 'BOOKING_REQUEST_CREATED', 'Booking', '37', 'Submitted after re-validating all hard constraints.', '2026-09-27 16:18:05.000000'),
(78, 'lecturer13@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '14', 'No valid room for LAB on 2026-09-28 08:00-10:00', '2026-09-27 16:20:39.000000'),
(79, 'lecturer13@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '14', 'No valid room for TUTORIAL on 2026-09-28 10:00-12:00', '2026-09-27 16:41:17.000000'),
(80, 'lecturer13@usp.ac.fj', 'SEARCH_NO_RESULT', 'User', '14', 'No valid room for TUTORIAL on 2026-09-28 14:00-16:00', '2026-09-27 16:41:49.000000'),
(81, 'lecturer13@usp.ac.fj', 'SEARCH_RECOMMENDATION', 'User', '14', 'Top room CYBER-LAB score=90.0 for TUTORIAL on 2026-10-01 14:00-16:00', '2026-09-27 16:42:01.000000'),
(82, 'lecturer13@usp.ac.fj', 'BOOKING_REQUEST_CREATED', 'Booking', '38', 'Submitted after re-validating all hard constraints.', '2026-09-27 16:42:07.000000'),
(83, 'admin@usp.ac.fj', 'BOOKING_REJECTED', 'Booking', '19', 'invalid booking', '2026-09-27 16:43:26.000000');

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
  `id` bigint(20) NOT NULL,
  `requester_id` bigint(20) NOT NULL,
  `course_id` bigint(20) DEFAULT NULL,
  `room_id` bigint(20) NOT NULL,
  `type` varchar(30) NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'PENDING',
  `date` date NOT NULL,
  `start_time` time(6) NOT NULL,
  `end_time` time(6) NOT NULL,
  `expected_students` int(11) NOT NULL,
  `purpose` varchar(1000) NOT NULL,
  `search_score` double DEFAULT NULL,
  `recommendation_reason` varchar(2000) DEFAULT NULL,
  `admin_decision_reason` varchar(2000) DEFAULT NULL,
  `override_reason` varchar(2000) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT current_timestamp(6),
  `updated_at` datetime(6) NOT NULL DEFAULT current_timestamp(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`id`, `requester_id`, `course_id`, `room_id`, `type`, `status`, `date`, `start_time`, `end_time`, `expected_students`, `purpose`, `search_score`, `recommendation_reason`, `admin_decision_reason`, `override_reason`, `created_at`, `updated_at`) VALUES
(1, 3, 1, 1, 'LAB', 'APPROVED', '2026-09-04', '14:00:00.000000', '16:00:00.000000', 42, 'CS214 LAB', 92, 'Test conflict: academic LAB request for ICT-LAB-3.', 'Approved by administrator', NULL, '2026-09-01 00:39:42.290085', '2026-09-03 11:54:38.000000'),
(2, 4, 2, 1, 'TUTORIAL', 'ALTERNATIVE_PROPOSED', '2026-09-04', '14:00:00.000000', '16:00:00.000000', 25, 'CS315 TUTORIAL', 89, 'Test conflict: academic TUTORIAL request for ICT-LAB-3.', 'Competing request approved; alternative proposed.', NULL, '2026-09-01 00:39:42.290085', '2026-09-03 11:54:41.000000'),
(3, 6, NULL, 1, 'ECA', 'ALTERNATIVE_PROPOSED', '2026-09-04', '14:00:00.000000', '16:00:00.000000', 30, 'ECA booking', 84, 'Test conflict: lower-priority ECA request for ICT-LAB-3.', 'Competing request approved; alternative proposed.', NULL, '2026-09-01 00:39:42.290085', '2026-09-03 11:54:42.000000'),
(4, 5, 3, 2, 'LAB', 'APPROVED', '2026-09-05', '10:00:00.000000', '12:00:00.000000', 35, 'CS324 LAB', 94, 'Approved test booking.', 'Approved seed data for room-conflict testing.', NULL, '2026-09-01 00:39:42.605011', '2026-09-01 00:39:42.605011'),
(6, 9, 6, 11, 'LAB', 'APPROVED', '2026-09-01', '08:00:00.000000', '10:00:00.000000', 45, 'Cloud Computing Lab', 90, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(7, 10, 7, 11, 'TUTORIAL', 'APPROVED', '2026-09-02', '10:00:00.000000', '12:00:00.000000', 40, 'Cyber Security Tutorial', 88, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(8, 11, 8, 11, 'LAB', 'APPROVED', '2026-09-03', '13:00:00.000000', '15:00:00.000000', 50, 'Machine Learning Lab', 91, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(9, 12, 9, 11, 'TUTORIAL', 'PENDING', '2026-09-04', '09:00:00.000000', '11:00:00.000000', 30, 'Enterprise Tutorial', 85, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(10, 13, 10, 11, 'LAB', 'APPROVED', '2026-09-05', '14:00:00.000000', '16:00:00.000000', 35, 'Mobile Computing Lab', 92, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(11, 9, 11, 11, 'LAB', 'APPROVED', '2026-09-08', '08:00:00.000000', '10:00:00.000000', 50, 'Analytics Lab', 90, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(12, 10, 12, 11, 'TUTORIAL', 'APPROVED', '2026-09-09', '11:00:00.000000', '13:00:00.000000', 30, 'IT Management Tutorial', 87, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(13, 11, 13, 11, 'LAB', 'PENDING', '2026-09-10', '14:00:00.000000', '16:00:00.000000', 55, 'Operating Systems Lab', 86, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(14, 12, 14, 11, 'LAB', 'APPROVED', '2026-09-11', '08:00:00.000000', '10:00:00.000000', 45, 'Networks Lab', 93, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(15, 13, 15, 11, 'ECA', 'APPROVED', '2026-09-12', '16:00:00.000000', '18:00:00.000000', 25, 'Student Activity', 75, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(16, 9, 6, 12, 'LAB', 'APPROVED', '2026-09-01', '10:00:00.000000', '12:00:00.000000', 45, 'Cloud Lab', 90, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(17, 10, 7, 12, 'LAB', 'APPROVED', '2026-09-02', '13:00:00.000000', '15:00:00.000000', 50, 'Security Lab', 92, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(18, 11, 8, 12, 'LAB', 'APPROVED', '2026-09-03', '08:00:00.000000', '10:00:00.000000', 55, 'AI Lab', 91, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(19, 12, 9, 12, 'TUTORIAL', 'REJECTED', '2026-09-04', '14:00:00.000000', '16:00:00.000000', 30, 'IS Tutorial', 80, 'Calendar seed', 'invalid booking', NULL, '2026-09-04 17:02:55.425068', '2026-09-27 16:43:26.000000'),
(20, 13, 10, 12, 'LAB', 'APPROVED', '2026-09-05', '09:00:00.000000', '11:00:00.000000', 35, 'Mobile Lab', 90, 'Calendar seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(21, 9, 6, 13, 'LAB', 'APPROVED', '2026-09-06', '08:00:00.000000', '10:00:00.000000', 45, 'Room calendar test', 90, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(22, 10, 7, 13, 'LAB', 'APPROVED', '2026-09-07', '10:00:00.000000', '12:00:00.000000', 50, 'Room calendar test', 90, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(23, 11, 8, 13, 'LAB', 'APPROVED', '2026-09-08', '13:00:00.000000', '15:00:00.000000', 55, 'Room calendar test', 90, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(24, 12, 9, 13, 'TUTORIAL', 'APPROVED', '2026-09-09', '08:00:00.000000', '10:00:00.000000', 30, 'Room calendar test', 90, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(25, 13, 10, 13, 'ECA', 'APPROVED', '2026-09-10', '15:00:00.000000', '17:00:00.000000', 30, 'Room calendar test', 70, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(26, 9, 11, 14, 'LAB', 'APPROVED', '2026-09-01', '08:00:00.000000', '10:00:00.000000', 50, 'Calendar test', 90, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(27, 10, 12, 14, 'TUTORIAL', 'APPROVED', '2026-09-02', '10:00:00.000000', '12:00:00.000000', 40, 'Calendar test', 90, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(28, 11, 13, 14, 'LAB', 'APPROVED', '2026-09-03', '13:00:00.000000', '15:00:00.000000', 50, 'Calendar test', 90, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(29, 12, 14, 14, 'LAB', 'PENDING', '2026-09-04', '08:00:00.000000', '10:00:00.000000', 45, 'Calendar test', 90, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(30, 13, 15, 14, 'ECA', 'APPROVED', '2026-09-05', '16:00:00.000000', '18:00:00.000000', 20, 'Calendar test', 70, 'Seed', NULL, NULL, '2026-09-04 17:02:55.425068', '2026-09-04 17:02:55.425068'),
(31, 6, NULL, 5, 'ECA', 'APPROVED', '2026-09-27', '12:00:00.000000', '13:00:00.000000', 40, 'ECA booking', 93, 'Submitted after re-validating all hard constraints.', 'Approved by administrator', NULL, '2026-09-27 01:43:25.000000', '2026-09-27 01:44:08.000000'),
(32, 14, 21, 20, 'LAB', 'REJECTED', '2026-09-28', '10:00:00.000000', '12:00:00.000000', 50, 'CS111 LAB', 94.4, 'Submitted after re-validating all hard constraints.', 'irrelevant booking', NULL, '2026-09-27 04:42:00.000000', '2026-09-27 04:43:35.000000'),
(33, 14, 21, 11, 'LAB', 'APPROVED', '2026-09-28', '10:00:00.000000', '12:00:00.000000', 50, 'CS111 LAB', 92.2, 'Submitted after re-validating all hard constraints.', 'Approved by administrator', NULL, '2026-09-27 14:34:44.000000', '2026-09-27 16:16:16.000000'),
(34, 14, 21, 20, 'LAB', 'APPROVED', '2026-09-29', '10:00:00.000000', '12:00:00.000000', 50, 'CS111 LAB', 94.4, 'Submitted after re-validating all hard constraints.', 'Approved by administrator', NULL, '2026-09-27 14:35:14.000000', '2026-09-27 16:16:07.000000'),
(35, 14, 21, 11, 'LAB', 'APPROVED', '2026-09-30', '10:00:00.000000', '12:00:00.000000', 50, 'CS111 LAB', 92.2, 'Submitted after re-validating all hard constraints.', 'Approved by administrator', NULL, '2026-09-27 14:35:52.000000', '2026-09-27 16:16:02.000000'),
(36, 14, 21, 20, 'TUTORIAL', 'APPROVED', '2026-10-07', '10:00:00.000000', '12:00:00.000000', 50, 'CS111 TUTORIAL', 94.4, 'Submitted after re-validating all hard constraints.', 'Approved by administrator', NULL, '2026-09-27 14:51:46.000000', '2026-09-27 16:15:52.000000'),
(37, 14, 21, 11, 'LAB', 'PENDING', '2026-10-08', '10:00:00.000000', '12:00:00.000000', 50, 'CS111 LAB', 92.2, 'Submitted after re-validating all hard constraints.', NULL, NULL, '2026-09-27 16:18:05.000000', '2026-09-27 16:18:05.000000'),
(38, 14, 21, 2, 'TUTORIAL', 'PENDING', '2026-10-01', '14:00:00.000000', '16:00:00.000000', 50, 'CS111 TUTORIAL', 88, 'Submitted after re-validating all hard constraints.', NULL, NULL, '2026-09-27 16:42:07.000000', '2026-09-27 16:42:07.000000');

-- --------------------------------------------------------

--
-- Table structure for table `booking_required_facilities`
--

CREATE TABLE `booking_required_facilities` (
  `booking_id` bigint(20) NOT NULL,
  `facility_code` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `booking_required_facilities`
--

INSERT INTO `booking_required_facilities` (`booking_id`, `facility_code`) VALUES
(1, 'COMPUTER'),
(1, 'PROJECTOR'),
(2, 'PROJECTOR'),
(2, 'WHITEBOARD'),
(3, 'PROJECTOR'),
(4, 'COMPUTER'),
(4, 'PROJECTOR'),
(31, 'AIR_CONDITIONING'),
(31, 'AUDIO_SYSTEM'),
(32, 'AIR_CONDITIONING'),
(32, 'COMPUTER'),
(32, 'PROJECTOR'),
(32, 'WHITEBOARD'),
(33, 'AIR_CONDITIONING'),
(33, 'COMPUTER'),
(33, 'PROJECTOR'),
(33, 'WHITEBOARD'),
(34, 'AIR_CONDITIONING'),
(34, 'COMPUTER'),
(34, 'PROJECTOR'),
(34, 'WHITEBOARD'),
(35, 'AIR_CONDITIONING'),
(35, 'COMPUTER'),
(35, 'PROJECTOR'),
(35, 'WHITEBOARD'),
(36, 'AIR_CONDITIONING'),
(36, 'COMPUTER'),
(36, 'PROJECTOR'),
(36, 'WHITEBOARD'),
(37, 'AIR_CONDITIONING'),
(37, 'COMPUTER'),
(37, 'PROJECTOR'),
(37, 'WHITEBOARD'),
(38, 'AIR_CONDITIONING'),
(38, 'COMPUTER'),
(38, 'PROJECTOR'),
(38, 'WHITEBOARD');

-- --------------------------------------------------------

--
-- Table structure for table `courses`
--

CREATE TABLE `courses` (
  `id` bigint(20) NOT NULL,
  `code` varchar(30) NOT NULL,
  `name` varchar(255) NOT NULL,
  `course_roll` int(11) NOT NULL,
  `required_lab_sessions` int(11) NOT NULL,
  `required_tutorial_sessions` int(11) NOT NULL,
  `max_weekly_bookings` int(11) NOT NULL DEFAULT 4,
  `prime_time_allowance` int(11) NOT NULL DEFAULT 1,
  `active` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `courses`
--

INSERT INTO `courses` (`id`, `code`, `name`, `course_roll`, `required_lab_sessions`, `required_tutorial_sessions`, `max_weekly_bookings`, `prime_time_allowance`, `active`) VALUES
(1, 'CS214', 'Software Engineering', 40, 3, 1, 4, 2, 1),
(2, 'CS315', 'Information Systems', 25, 1, 3, 4, 2, 1),
(3, 'CS324', 'Distributed Systems', 35, 2, 2, 4, 1, 1),
(4, 'IS314', 'Information Systems Project', 50, 2, 2, 5, 2, 1),
(5, 'CS999', 'Inactive Test Course', 20, 1, 1, 4, 1, 0),
(6, 'CS501', 'Cloud Computing', 45, 2, 2, 4, 2, 1),
(7, 'CS502', 'Cyber Security', 50, 3, 1, 4, 2, 1),
(8, 'CS503', 'Machine Learning', 55, 3, 2, 5, 2, 1),
(9, 'CS504', 'Web Development', 40, 2, 2, 4, 2, 1),
(10, 'CS505', 'Mobile Computing', 35, 2, 1, 4, 1, 1),
(11, 'IS501', 'Enterprise Systems', 45, 1, 3, 4, 1, 1),
(12, 'IS502', 'Data Analytics', 50, 2, 2, 4, 2, 1),
(13, 'IS503', 'IT Management', 30, 1, 2, 4, 1, 1),
(14, 'CS506', 'Operating Systems', 60, 3, 2, 5, 2, 1),
(15, 'CS507', 'Computer Networks', 55, 3, 1, 4, 2, 1),
(16, 'CS508', 'Database Design', 45, 2, 2, 4, 2, 1),
(17, 'IS504', 'Digital Business', 40, 1, 2, 4, 1, 1),
(18, 'CS509', 'Software Testing', 35, 2, 2, 4, 1, 1),
(19, 'CS510', 'Parallel Computing', 50, 3, 1, 4, 2, 1),
(20, 'IS505', 'Project Research', 30, 1, 2, 4, 1, 1),
(21, 'CS111', 'Computer Engineering', 50, 5, 5, 6, 3, 1);

-- --------------------------------------------------------

--
-- Table structure for table `course_lecturers`
--

CREATE TABLE `course_lecturers` (
  `course_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `course_lecturers`
--

INSERT INTO `course_lecturers` (`course_id`, `user_id`) VALUES
(1, 3),
(2, 4),
(3, 3),
(3, 5),
(4, 5),
(5, 4),
(6, 9),
(7, 10),
(8, 11),
(9, 12),
(10, 13),
(11, 9),
(12, 10),
(13, 11),
(14, 12),
(15, 13),
(16, 9),
(17, 10),
(18, 11),
(19, 12),
(20, 13),
(21, 14);

-- --------------------------------------------------------

--
-- Table structure for table `course_required_facilities`
--

CREATE TABLE `course_required_facilities` (
  `course_id` bigint(20) NOT NULL,
  `facility_code` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `course_required_facilities`
--

INSERT INTO `course_required_facilities` (`course_id`, `facility_code`) VALUES
(1, 'COMPUTER'),
(1, 'PROJECTOR'),
(2, 'PROJECTOR'),
(2, 'WHITEBOARD'),
(3, 'COMPUTER'),
(3, 'PROJECTOR'),
(4, 'PROJECTOR'),
(4, 'WHITEBOARD'),
(5, 'PROJECTOR'),
(6, 'COMPUTER'),
(6, 'PROJECTOR'),
(7, 'COMPUTER'),
(7, 'PROJECTOR'),
(8, 'COMPUTER'),
(8, 'PROJECTOR'),
(9, 'PROJECTOR'),
(9, 'WHITEBOARD'),
(10, 'COMPUTER'),
(10, 'PROJECTOR'),
(11, 'COMPUTER'),
(11, 'PROJECTOR'),
(12, 'COMPUTER'),
(12, 'SMART_BOARD'),
(13, 'PROJECTOR'),
(13, 'WHITEBOARD'),
(14, 'COMPUTER'),
(14, 'PROJECTOR'),
(15, 'COMPUTER'),
(15, 'PROJECTOR'),
(16, 'COMPUTER'),
(16, 'PROJECTOR'),
(17, 'PROJECTOR'),
(17, 'WHITEBOARD'),
(18, 'COMPUTER'),
(18, 'PROJECTOR'),
(19, 'COMPUTER'),
(20, 'PROJECTOR'),
(21, 'AIR_CONDITIONING'),
(21, 'COMPUTER'),
(21, 'PROJECTOR'),
(21, 'WHITEBOARD');

-- --------------------------------------------------------

--
-- Table structure for table `maintenance_windows`
--

CREATE TABLE `maintenance_windows` (
  `id` bigint(20) NOT NULL,
  `room_id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `start_time` time(6) NOT NULL,
  `end_time` time(6) NOT NULL,
  `reason` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `maintenance_windows`
--

INSERT INTO `maintenance_windows` (`id`, `room_id`, `date`, `start_time`, `end_time`, `reason`) VALUES
(1, 7, '2026-09-07', '09:00:00.000000', '13:00:00.000000', 'Scheduled computer and network maintenance'),
(2, 13, '2026-09-15', '09:00:00.000000', '13:00:00.000000', 'Projector replacement'),
(3, 18, '2026-09-18', '08:00:00.000000', '12:00:00.000000', 'Computer maintenance'),
(4, 22, '2026-09-20', '10:00:00.000000', '15:00:00.000000', 'Smart board upgrade'),
(5, 20, '2026-09-28', '08:00:00.000000', '12:00:00.000000', 'Equipment repair');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` varchar(2000) NOT NULL,
  `read_flag` tinyint(1) NOT NULL DEFAULT 0,
  `dedup_key` varchar(180) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT current_timestamp(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `read_flag`, `dedup_key`, `created_at`) VALUES
(1, 1, 'Test data loaded', 'The development database was populated with conflict, approval, fairness, maintenance and access-control test scenarios.', 0, 'TEST-SEED-ADMIN', '2026-09-01 00:39:43.657033'),
(2, 3, 'Pending booking test', 'Your seeded CS214 LAB request is waiting for administrator approval.', 0, 'TEST-SEED-LEC1', '2026-09-01 00:39:43.657033'),
(3, 6, 'Alternative available', 'A seeded alternative room/time option is available for your ECA request.', 1, 'TEST-SEED-STUDENT1', '2026-09-01 00:39:43.657033'),
(4, 3, 'Booking approved', 'LAB booking in ICT-LAB-3 on 2026-09-04 from 14:00 to 16:00.', 0, 'APPROVED-1', '2026-09-03 11:54:38.000000'),
(5, 4, 'Alternative booking suggested', 'Original room/time was allocated to another request. Suggested: TUT-021 on 2026-09-04 14:00-16:00.', 0, 'ALTERNATIVE-2-2', '2026-09-03 11:54:41.000000'),
(6, 6, 'Alternative booking suggested', 'Original room/time was allocated to another request. Suggested: TUT-021 on 2026-09-04 14:00-16:00.', 0, 'ALTERNATIVE-3-3', '2026-09-03 11:54:42.000000'),
(7, 3, 'Upcoming booking reminder', 'LAB in ICT-LAB-3 on 2026-09-04 from 14:00 to 16:00', 0, 'REMINDER-1-24', '2026-09-03 20:00:00.000000'),
(8, 5, 'Upcoming booking reminder', 'LAB in ICT-LAB-2 on 2026-09-05 from 10:00 to 12:00', 0, 'REMINDER-4-24', '2026-09-04 17:00:00.000000'),
(9, 6, 'Booking request submitted', 'Your request for ROOM-006 on 2026-09-27 is pending approval.', 0, 'SUBMITTED-31', '2026-09-27 01:43:25.000000'),
(10, 1, 'New booking request', 'Ana Singh submitted a ECA request for ROOM-006 on 2026-09-27.', 0, NULL, '2026-09-27 01:43:25.000000'),
(11, 6, 'Booking approved', 'ECA booking in ROOM-006 on 2026-09-27 from 12:00 to 13:00.', 0, 'APPROVED-31', '2026-09-27 01:44:08.000000'),
(12, 6, 'Upcoming booking reminder', 'ECA in ROOM-006 on 2026-09-27 from 12:00 to 13:00', 0, 'REMINDER-31-24', '2026-09-27 02:00:00.000000'),
(13, 14, 'Booking request submitted', 'Your request for CYBER-LAB on 2026-09-28 is pending approval.', 1, 'SUBMITTED-32', '2026-09-27 04:42:00.000000'),
(14, 1, 'New booking request', 'Daniel Bose submitted a LAB request for CYBER-LAB on 2026-09-28.', 0, NULL, '2026-09-27 04:42:00.000000'),
(15, 14, 'Booking rejected', 'irrelevant booking', 1, 'REJECTED-32', '2026-09-27 04:43:35.000000'),
(16, 14, 'Booking request submitted', 'Your request for LAB201 on 2026-09-28 is pending approval.', 0, 'SUBMITTED-33', '2026-09-27 14:34:44.000000'),
(17, 1, 'New booking request', 'Daniel Bose submitted a LAB request for LAB201 on 2026-09-28.', 0, NULL, '2026-09-27 14:34:44.000000'),
(18, 14, 'Booking request submitted', 'Your request for CYBER-LAB on 2026-09-29 is pending approval.', 0, 'SUBMITTED-34', '2026-09-27 14:35:14.000000'),
(19, 1, 'New booking request', 'Daniel Bose submitted a LAB request for CYBER-LAB on 2026-09-29.', 0, NULL, '2026-09-27 14:35:14.000000'),
(20, 14, 'Booking request submitted', 'Your request for LAB201 on 2026-09-30 is pending approval.', 0, 'SUBMITTED-35', '2026-09-27 14:35:52.000000'),
(21, 1, 'New booking request', 'Daniel Bose submitted a LAB request for LAB201 on 2026-09-30.', 0, NULL, '2026-09-27 14:35:52.000000'),
(22, 14, 'Booking request submitted', 'Your request for CYBER-LAB on 2026-10-07 is pending approval.', 0, 'SUBMITTED-36', '2026-09-27 14:51:46.000000'),
(23, 1, 'New booking request', 'Daniel Bose submitted a TUTORIAL request for CYBER-LAB on 2026-10-07.', 0, NULL, '2026-09-27 14:51:46.000000'),
(24, 14, 'Booking approved', 'TUTORIAL booking in CYBER-LAB on 2026-10-07 from 10:00 to 12:00.', 0, 'APPROVED-36', '2026-09-27 16:15:51.000000'),
(25, 14, 'Booking approved', 'LAB booking in LAB201 on 2026-09-30 from 10:00 to 12:00.', 0, 'APPROVED-35', '2026-09-27 16:16:02.000000'),
(26, 14, 'Booking approved', 'LAB booking in CYBER-LAB on 2026-09-29 from 10:00 to 12:00.', 0, 'APPROVED-34', '2026-09-27 16:16:07.000000'),
(27, 14, 'Booking approved', 'LAB booking in LAB201 on 2026-09-28 from 10:00 to 12:00.', 0, 'APPROVED-33', '2026-09-27 16:16:16.000000'),
(28, 14, 'Booking request submitted', 'Your request for LAB201 on 2026-10-08 is pending approval.', 0, 'SUBMITTED-37', '2026-09-27 16:18:05.000000'),
(29, 1, 'New booking request', 'Daniel Bose submitted a LAB request for LAB201 on 2026-10-08.', 0, NULL, '2026-09-27 16:18:05.000000'),
(30, 14, 'Booking request submitted', 'Your request for ICT-LAB-2 on 2026-10-01 is pending approval.', 0, 'SUBMITTED-38', '2026-09-27 16:42:07.000000'),
(31, 1, 'New booking request', 'Daniel Bose submitted a TUTORIAL request for ICT-LAB-2 on 2026-10-01.', 0, NULL, '2026-09-27 16:42:07.000000'),
(32, 12, 'Booking rejected', 'invalid booking', 1, 'REJECTED-19', '2026-09-27 16:43:26.000000'),
(33, 14, 'Upcoming booking reminder', 'LAB in LAB201 on 2026-09-28 from 10:00 to 12:00', 0, 'REMINDER-33-24', '2026-09-27 17:00:00.000000');

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `id` bigint(20) NOT NULL,
  `code` varchar(30) NOT NULL,
  `name` varchar(255) NOT NULL,
  `building` varchar(255) NOT NULL,
  `capacity` int(11) NOT NULL,
  `computer_count` int(11) NOT NULL,
  `operational_computers` int(11) NOT NULL,
  `projector_available` tinyint(1) NOT NULL DEFAULT 0,
  `projector_operational` tinyint(1) NOT NULL DEFAULT 0,
  `whiteboard_available` tinyint(1) NOT NULL DEFAULT 0,
  `whiteboard_operational` tinyint(1) NOT NULL DEFAULT 0,
  `access_level` varchar(30) NOT NULL DEFAULT 'BOTH',
  `status` varchar(30) NOT NULL DEFAULT 'ACTIVE',
  `department_restriction` varchar(255) DEFAULT NULL,
  `course_restriction` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rooms`
--

INSERT INTO `rooms` (`id`, `code`, `name`, `building`, `capacity`, `computer_count`, `operational_computers`, `projector_available`, `projector_operational`, `whiteboard_available`, `whiteboard_operational`, `access_level`, `status`, `department_restriction`, `course_restriction`) VALUES
(1, 'ICT-LAB-3', 'ICT Lab 3', 'ICT Building', 45, 45, 45, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(2, 'ICT-LAB-2', 'ICT Lab 2', 'ICT Building', 60, 60, 60, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(3, 'MAIN-LAB', 'Main Computer Lab', 'ICT Building', 100, 100, 100, 1, 1, 1, 1, 'LECTURER_ONLY', 'ACTIVE', NULL, NULL),
(4, 'TUT-021', 'Tutorial Room 021', 'Engineering Building', 30, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(5, 'ROOM-006', 'Multipurpose Room 006', 'Student Centre', 50, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(6, 'ICT-LAB-4', 'ICT Lab 4 - Fault Test', 'ICT Building', 40, 40, 34, 1, 0, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(7, 'ICT-LAB-5', 'ICT Lab 5 - Maintenance', 'ICT Building', 35, 35, 35, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(8, 'ENG-DEP-1', 'Engineering Restricted', 'Engineering Building', 45, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', 'ENGINEERING', NULL),
(9, 'CS214-LAB', 'CS214 Restricted Lab', 'ICT Building', 50, 50, 50, 1, 1, 1, 1, 'LECTURER_ONLY', 'ACTIVE', NULL, 'CS214'),
(10, 'OLD-LAB', 'Disabled Legacy Lab', 'ICT Building', 40, 40, 40, 1, 1, 1, 1, 'BOTH', 'DISABLED', NULL, NULL),
(11, 'LAB201', 'Advanced Lab 201', 'ICT Building', 50, 50, 50, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(12, 'LAB202', 'Advanced Lab 202', 'ICT Building', 60, 60, 60, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(13, 'LAB203', 'Security Lab', 'ICT Building', 45, 45, 45, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(14, 'LAB204', 'Network Lab', 'ICT Building', 40, 40, 40, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(15, 'TUT301', 'Tutorial 301', 'Science Building', 30, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(16, 'TUT302', 'Tutorial 302', 'Science Building', 35, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(17, 'SEM401', 'Seminar Hall 401', 'Engineering', 100, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(18, 'SMART501', 'Smart Room 501', 'ICT Building', 45, 20, 20, 1, 1, 1, 1, 'LECTURER_ONLY', 'ACTIVE', NULL, NULL),
(19, 'AI-LAB', 'Artificial Intelligence Lab', 'ICT Building', 55, 55, 55, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(20, 'CYBER-LAB', 'Cyber Security Lab', 'ICT Building', 50, 50, 50, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(21, 'ROOM601', 'Meeting Room 601', 'Student Centre', 25, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(22, 'ROOM602', 'Meeting Room 602', 'Student Centre', 35, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(23, 'ROOM603', 'Innovation Room', 'Student Centre', 50, 10, 10, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL),
(24, 'ROOM604', 'Research Room', 'Research Building', 40, 20, 20, 1, 1, 1, 1, 'LECTURER_ONLY', 'ACTIVE', NULL, NULL),
(25, 'ROOM605', 'Conference Room', 'Research Building', 70, 0, 0, 1, 1, 1, 1, 'BOTH', 'ACTIVE', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `room_facilities`
--

CREATE TABLE `room_facilities` (
  `room_id` bigint(20) NOT NULL,
  `facility_code` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `room_facilities`
--

INSERT INTO `room_facilities` (`room_id`, `facility_code`) VALUES
(1, 'AIR_CONDITIONING'),
(1, 'SPECIALIZED_SOFTWARE'),
(2, 'AIR_CONDITIONING'),
(2, 'SMART_BOARD'),
(3, 'AIR_CONDITIONING'),
(3, 'SPECIALIZED_SOFTWARE'),
(3, 'VIDEO_CONFERENCING'),
(4, 'AIR_CONDITIONING'),
(5, 'AIR_CONDITIONING'),
(5, 'AUDIO_SYSTEM'),
(6, 'AIR_CONDITIONING'),
(7, 'AIR_CONDITIONING'),
(8, 'AIR_CONDITIONING'),
(9, 'AIR_CONDITIONING'),
(9, 'SPECIALIZED_SOFTWARE'),
(10, 'AIR_CONDITIONING'),
(11, 'AIR_CONDITIONING'),
(11, 'SPECIALIZED_SOFTWARE'),
(12, 'AIR_CONDITIONING'),
(12, 'SMART_BOARD'),
(13, 'AIR_CONDITIONING'),
(13, 'VIDEO_CONFERENCING'),
(14, 'AIR_CONDITIONING'),
(15, 'AIR_CONDITIONING'),
(15, 'SMART_BOARD'),
(16, 'AIR_CONDITIONING'),
(16, 'VIDEO_CONFERENCING'),
(17, 'AIR_CONDITIONING'),
(17, 'SMART_BOARD'),
(18, 'AIR_CONDITIONING'),
(18, 'SPECIALIZED_SOFTWARE'),
(19, 'AIR_CONDITIONING'),
(19, 'AUDIO_SYSTEM'),
(20, 'AIR_CONDITIONING'),
(21, 'SPECIALIZED_SOFTWARE'),
(22, 'AIR_CONDITIONING'),
(23, 'SMART_BOARD'),
(24, 'VIDEO_CONFERENCING'),
(25, 'AUDIO_SYSTEM');

-- --------------------------------------------------------

--
-- Table structure for table `room_faulty_facilities`
--

CREATE TABLE `room_faulty_facilities` (
  `room_id` bigint(20) NOT NULL,
  `facility_code` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `room_faulty_facilities`
--

INSERT INTO `room_faulty_facilities` (`room_id`, `facility_code`) VALUES
(6, 'PROJECTOR'),
(13, 'PROJECTOR'),
(18, 'COMPUTER'),
(22, 'SMART_BOARD');

-- --------------------------------------------------------

--
-- Table structure for table `schedule_entries`
--

CREATE TABLE `schedule_entries` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `start_time` time(6) NOT NULL,
  `end_time` time(6) NOT NULL,
  `title` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `schedule_entries`
--

INSERT INTO `schedule_entries` (`id`, `user_id`, `date`, `start_time`, `end_time`, `title`) VALUES
(1, 3, '2026-09-06', '10:00:00.000000', '12:00:00.000000', 'Existing CS214 lecture'),
(2, 4, '2026-09-06', '13:00:00.000000', '15:00:00.000000', 'Existing CS315 lecture'),
(3, 9, '2026-09-10', '08:00:00.000000', '10:00:00.000000', 'Cloud Computing Lecture'),
(4, 10, '2026-09-10', '10:00:00.000000', '12:00:00.000000', 'Cyber Security Lecture'),
(5, 11, '2026-09-11', '13:00:00.000000', '15:00:00.000000', 'Machine Learning Lecture'),
(6, 12, '2026-09-12', '09:00:00.000000', '11:00:00.000000', 'Enterprise Systems Lecture'),
(7, 13, '2026-09-12', '14:00:00.000000', '16:00:00.000000', 'Database Lecture'),
(8, 14, '2026-09-28', '13:00:00.000000', '15:00:00.000000', 'Consultation');

-- --------------------------------------------------------

--
-- Table structure for table `system_settings`
--

CREATE TABLE `system_settings` (
  `setting_key` varchar(80) NOT NULL,
  `setting_value` varchar(500) NOT NULL,
  `description` varchar(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `system_settings`
--

INSERT INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('admin.override.enabled', 'true', 'Allow quota-only administrative overrides with a mandatory reason'),
('booking.day.end', '22:00', 'Latest booking end time'),
('booking.day.start', '08:00', 'Earliest booking start time'),
('booking.horizon.days', '120', 'How many days ahead users may book'),
('booking.max.duration.hours', '4', 'Maximum consecutive booking duration in hours'),
('course.default.max.weekly.bookings', '4', 'Default weekly booking quota for newly created courses'),
('course.default.prime.time.allowance', '1', 'Default weekly prime-time allowance for newly created courses'),
('lecturer.max.daily.bookings', '3', 'Default maximum active booking requests per day'),
('lecturer.max.weekly.bookings', '10', 'Default maximum active booking requests per week'),
('prime.time.end', '12:00', 'Prime-time end'),
('prime.time.start', '08:00', 'Prime-time start'),
('reminder.hours.before', '24', 'Reminder lead time in hours'),
('score.weight.capacity', '20', 'Search weight: capacity efficiency'),
('score.weight.courseNeed', '15', 'Search weight: course booking need'),
('score.weight.facility', '25', 'Search weight: facility suitability'),
('score.weight.roomFairness', '10', 'Search weight: room fairness'),
('score.weight.timeFairness', '20', 'Search weight: time fairness'),
('score.weight.utilization', '10', 'Search weight: room utilization balance'),
('search.result.count', '8', 'Maximum optimized search results');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `user_code` varchar(40) NOT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `department` varchar(255) DEFAULT NULL,
  `max_daily_bookings` int(11) DEFAULT NULL,
  `max_weekly_bookings` int(11) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT current_timestamp(6),
  `updated_at` datetime(6) NOT NULL DEFAULT current_timestamp(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `user_code`, `name`, `email`, `password_hash`, `role`, `active`, `department`, `max_daily_bookings`, `max_weekly_bookings`, `created_at`, `updated_at`) VALUES
(1, 'ADM001', 'System Administrator', 'admin@usp.ac.fj', '$2a$10$P62xHDCzrh0qE1dPi5kT2uEuQwmUjCyViI0XswLv5iSrsfVfDFt9q', 'ADMIN', 1, 'SITEMP', NULL, NULL, '2026-09-01 00:39:40.797938', '2026-09-01 00:39:40.797938'),
(2, 'IT001', 'IT Administrator', 'itadmin@usp.ac.fj', '$2a$10$O76SHsJUkXo67QdFiT8qauCe6K6IC/5thgn2TxNPK.f3cZcQHEWGK', 'IT_ADMIN', 1, 'ITS', NULL, NULL, '2026-09-01 00:39:40.797938', '2026-09-01 00:39:40.797938'),
(3, 'STAFF001', 'Litia Kumar', 'lecturer1@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 1, 'SITEMP', 3, 10, '2026-09-01 00:39:40.797938', '2026-09-01 00:39:40.797938'),
(4, 'STAFF002', 'Jone Ratu', 'lecturer2@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 1, 'SITEMP', 3, 10, '2026-09-01 00:39:40.797938', '2026-09-01 00:39:40.797938'),
(5, 'STAFF003', 'Mere Vakalalabure', 'lecturer3@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 1, 'SITEMP', 3, 10, '2026-09-01 00:39:40.797938', '2026-09-01 00:39:40.797938'),
(6, 'S12345678', 'Ana Singh', 'student1@usp.ac.fj', '$2a$10$hG0xoVHm8dP4CxtbBF6/Iuw5lTmu6QqbproTkNgFXoQ.JM09t.eMq', 'STUDENT', 1, 'SITEMP', NULL, NULL, '2026-09-01 00:39:40.797938', '2026-09-01 00:39:40.797938'),
(7, 'S12345679', 'Pita Tawake', 'student2@usp.ac.fj', '$2a$10$hG0xoVHm8dP4CxtbBF6/Iuw5lTmu6QqbproTkNgFXoQ.JM09t.eMq', 'STUDENT', 1, 'SITEMP', NULL, NULL, '2026-09-01 00:39:40.797938', '2026-09-01 00:39:40.797938'),
(8, 'STAFF099', 'Disabled Lecturer', 'disabled@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 0, 'SITEMP', NULL, NULL, '2026-09-01 00:39:40.797938', '2026-09-01 00:39:40.797938'),
(9, 'S11232784', 'Shival Sharma', 's11232784@student.usp.ac.fj', '$2a$10$1FDo7N1GQvxiVOjmgoFDC.Ma/gdVGaxOj8EOeqb7Ozfh0YNHfGXcy', 'STUDENT', 1, 'ENGINEERING', 1, 1, '2026-09-03 11:32:45.000000', '2026-09-03 11:32:45.000000'),
(10, 'STAFF201', 'Amit Prasad', 'lecturer9@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 1, 'SITEMP', 3, 10, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(11, 'STAFF202', 'Rina Kumar', 'lecturer10@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 1, 'SITEMP', 3, 10, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(12, 'STAFF203', 'Joseph Singh', 'lecturer11@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 1, 'SITEMP', 3, 10, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(13, 'STAFF204', 'Priya Rao', 'lecturer12@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 1, 'SITEMP', 3, 10, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(14, 'STAFF205', 'Daniel Bose', 'lecturer13@usp.ac.fj', '$2a$10$F9nj5InTYWqa9XWneN7GGuYuEXJaVZqkLBVc6AtF27SwEPS/eABRO', 'LECTURER', 1, 'SITEMP', 3, 10, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(15, 'S300001', 'Student One', 'student6@usp.ac.fj', '$2a$10$hG0xoVHm8dP4CxtbBF6/Iuw5lTmu6QqbproTkNgFXoQ.JM09t.eMq', 'STUDENT', 1, 'SITEMP', NULL, NULL, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(16, 'S300002', 'Student Two', 'student7@usp.ac.fj', '$2a$10$hG0xoVHm8dP4CxtbBF6/Iuw5lTmu6QqbproTkNgFXoQ.JM09t.eMq', 'STUDENT', 1, 'SITEMP', NULL, NULL, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(17, 'S300003', 'Student Three', 'student8@usp.ac.fj', '$2a$10$hG0xoVHm8dP4CxtbBF6/Iuw5lTmu6QqbproTkNgFXoQ.JM09t.eMq', 'STUDENT', 1, 'SITEMP', NULL, NULL, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(18, 'S300004', 'Student Four', 'student9@usp.ac.fj', '$2a$10$hG0xoVHm8dP4CxtbBF6/Iuw5lTmu6QqbproTkNgFXoQ.JM09t.eMq', 'STUDENT', 1, 'SITEMP', NULL, NULL, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(19, 'S300005', 'Student Five', 'student10@usp.ac.fj', '$2a$10$hG0xoVHm8dP4CxtbBF6/Iuw5lTmu6QqbproTkNgFXoQ.JM09t.eMq', 'STUDENT', 1, 'SITEMP', NULL, NULL, '2026-09-03 20:01:09.779476', '2026-09-03 20:01:09.779476'),
(20, 'S11232785', 'Shival Sharma', 'shivalsharma00@gmail.com', '$2a$10$QGXTgi.6nMyh7F1beCg49O5TAk7RDyfuJpvXv0lbmpydGRtaTHBYO', 'STUDENT', 1, 'ENGINEERING', 1, 4, '2026-09-27 14:18:22.000000', '2026-09-27 14:18:22.000000');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `alternative_suggestions`
--
ALTER TABLE `alternative_suggestions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_alt_booking` (`booking_id`,`status`,`score`),
  ADD KEY `fk_alt_room` (`room_id`);

--
-- Indexes for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_audit_created` (`created_at`),
  ADD KEY `idx_audit_actor` (`actor_email`);

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_booking_room_date` (`room_id`,`date`,`start_time`,`end_time`,`status`),
  ADD KEY `idx_booking_requester_date` (`requester_id`,`date`,`status`),
  ADD KEY `idx_booking_course_date` (`course_id`,`date`,`status`),
  ADD KEY `idx_booking_status_created` (`status`,`created_at`);

--
-- Indexes for table `booking_required_facilities`
--
ALTER TABLE `booking_required_facilities`
  ADD PRIMARY KEY (`booking_id`,`facility_code`);

--
-- Indexes for table `courses`
--
ALTER TABLE `courses`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_courses_code` (`code`);

--
-- Indexes for table `course_lecturers`
--
ALTER TABLE `course_lecturers`
  ADD PRIMARY KEY (`course_id`,`user_id`),
  ADD KEY `fk_course_lecturers_user` (`user_id`);

--
-- Indexes for table `course_required_facilities`
--
ALTER TABLE `course_required_facilities`
  ADD PRIMARY KEY (`course_id`,`facility_code`);

--
-- Indexes for table `maintenance_windows`
--
ALTER TABLE `maintenance_windows`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_maintenance_room_date` (`room_id`,`date`,`start_time`,`end_time`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_notification_dedup` (`dedup_key`),
  ADD KEY `idx_notification_user` (`user_id`,`read_flag`,`created_at`);

--
-- Indexes for table `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_rooms_code` (`code`);

--
-- Indexes for table `room_facilities`
--
ALTER TABLE `room_facilities`
  ADD PRIMARY KEY (`room_id`,`facility_code`);

--
-- Indexes for table `room_faulty_facilities`
--
ALTER TABLE `room_faulty_facilities`
  ADD PRIMARY KEY (`room_id`,`facility_code`);

--
-- Indexes for table `schedule_entries`
--
ALTER TABLE `schedule_entries`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_schedule_user_date` (`user_id`,`date`,`start_time`,`end_time`);

--
-- Indexes for table `system_settings`
--
ALTER TABLE `system_settings`
  ADD PRIMARY KEY (`setting_key`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_users_user_code` (`user_code`),
  ADD UNIQUE KEY `uk_users_email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `alternative_suggestions`
--
ALTER TABLE `alternative_suggestions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `audit_logs`
--
ALTER TABLE `audit_logs`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=84;

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT for table `courses`
--
ALTER TABLE `courses`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `maintenance_windows`
--
ALTER TABLE `maintenance_windows`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT for table `rooms`
--
ALTER TABLE `rooms`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT for table `schedule_entries`
--
ALTER TABLE `schedule_entries`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `alternative_suggestions`
--
ALTER TABLE `alternative_suggestions`
  ADD CONSTRAINT `fk_alt_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_alt_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`);

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `fk_booking_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  ADD CONSTRAINT `fk_booking_requester` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_booking_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`);

--
-- Constraints for table `booking_required_facilities`
--
ALTER TABLE `booking_required_facilities`
  ADD CONSTRAINT `fk_booking_facilities_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `course_lecturers`
--
ALTER TABLE `course_lecturers`
  ADD CONSTRAINT `fk_course_lecturers_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_course_lecturers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `course_required_facilities`
--
ALTER TABLE `course_required_facilities`
  ADD CONSTRAINT `fk_course_facilities_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `maintenance_windows`
--
ALTER TABLE `maintenance_windows`
  ADD CONSTRAINT `fk_maintenance_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `room_facilities`
--
ALTER TABLE `room_facilities`
  ADD CONSTRAINT `fk_room_facilities_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `room_faulty_facilities`
--
ALTER TABLE `room_faulty_facilities`
  ADD CONSTRAINT `fk_room_faulty_facilities_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `schedule_entries`
--
ALTER TABLE `schedule_entries`
  ADD CONSTRAINT `fk_schedule_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
