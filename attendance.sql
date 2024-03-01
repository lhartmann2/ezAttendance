/*Run once before deploying app*/

DROP DATABASE IF EXISTS `attendance`;
CREATE DATABASE `attendance`;

USE `attendance`;

CREATE TABLE `users` (
	`id` INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
	`username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(68) NOT NULL,
    `enabled` TINYINT(1) NOT NULL DEFAULT TRUE,
    `failed_attempt` TINYINT(4) NULL DEFAULT 0,
    `account_non_locked` TINYINT(4) NULL DEFAULT TRUE,
    `lock_time` DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

INSERT INTO `role` (name)
VALUES 
('ROLE_EMPLOYEE'),('ROLE_MANAGER'),('ROLE_ADMIN');

CREATE TABLE `users_roles` (
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  
  PRIMARY KEY (`user_id`,`role_id`),
  
  KEY `FK_ROLE_idx` (`role_id`),
  
  CONSTRAINT `FK_USER_05` FOREIGN KEY (`user_id`) 
  REFERENCES `users` (`id`) 
  ON DELETE NO ACTION ON UPDATE NO ACTION,
  
  CONSTRAINT `FK_ROLE` FOREIGN KEY (`role_id`) 
  REFERENCES `role` (`id`) 
  ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

create table persistent_logins(
    username varchar(50) not null,
    series varchar(64) primary key,
    token varchar(64) not null,
    last_used timestamp not null
);

CREATE TABLE `teacher` (
	`id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NULL,
    `firstName` VARCHAR(25) NOT NULL,
    `lastName` VARCHAR(25) NOT NULL,
    `phone` VARCHAR(10) NOT NULL,
    `email` VARCHAR(50) NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `parent` (
	`id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `firstName` VARCHAR(25) NOT NULL,
    `lastName` VARCHAR(25) NOT NULL,
    `street` VARCHAR(50) NULL,
    `city` VARCHAR(25) NULL,
    `state` VARCHAR(2) NULL,
    `zip` VARCHAR(5) NULL,
    `phone` VARCHAR(10) NOT NULL,
    `email` VARCHAR(50) NULL,
    `other` VARCHAR(512) NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=10;

CREATE TABLE `student` (
	`id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `firstName` VARCHAR(25) NOT NULL,
    `lastName` VARCHAR(25) NOT NULL,
    `nickName` VARCHAR(25) NULL,
    `dob` DATE NOT NULL,
    `phone` VARCHAR(10) NULL,
    `hospital` VARCHAR(96) NULL,
    `allergies` VARCHAR(256) NULL,
    `notes` VARCHAR(1024) NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `parentStudent` (
	`studentID` INT NOT NULL,
    `parentID` INT NOT NULL,
    
    FOREIGN KEY (`studentID`) REFERENCES `student`(`id`),
    FOREIGN KEY (`parentID`) REFERENCES `parent`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `class` (
	`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `teacherID` INT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `dayOfWeek` VARCHAR(10) NOT NULL,
    `startTime` TIME NOT NULL,
    `endTime` TIME NOT NULL,
    `startDate` DATE NOT NULL,
    `endDate` DATE NOT NULL,
    `active` BOOL NOT NULL,
    
    FOREIGN KEY (`teacherID`) REFERENCES `teacher`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `studentClass` (
	`studentID` INT NOT NULL,
    `classID` INT NOT NULL,
    
    FOREIGN KEY (`studentID`) REFERENCES `student`(`id`),
    FOREIGN KEY (`classID`) REFERENCES `class`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `attendanceContainer` (
	`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	`classID` INT NOT NULL,
    `saDate` DATE NOT NULL,
    `taken` BOOL NOT NULL,
    
    FOREIGN KEY (`classID`) REFERENCES `class`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `studentAttendance` (
	`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `containerID` INT NOT NULL,
    `studentID` INT NOT NULL,
    `present` BOOLEAN NOT NULL,
    `excused` BOOLEAN NOT NULL, 
    `reason` VARCHAR(255) NULL,
    
    FOREIGN KEY (`studentID`) REFERENCES `student`(`id`),
    FOREIGN KEY (`containerID`) REFERENCES `attendanceContainer`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `event` (
	`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `eventDate` DATE NOT NULL,
    `eventName` VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;