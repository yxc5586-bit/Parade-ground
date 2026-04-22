CREATE DATABASE IF NOT EXISTS `Parade-ground`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `Parade-ground`;

CREATE TABLE IF NOT EXISTS user_info (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  userAccount VARCHAR(64) NOT NULL COMMENT 'User account',
  userPassword VARCHAR(255) NOT NULL COMMENT 'User password',
  userName VARCHAR(64) NOT NULL DEFAULT 'coder_player' COMMENT 'User name',
  currentSalary INT NOT NULL DEFAULT 10000 COMMENT 'Current monthly salary',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  isDelete TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
  PRIMARY KEY (id),
  UNIQUE KEY uk_userAccount (userAccount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User table';

CREATE TABLE IF NOT EXISTS level_info (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  levelId VARCHAR(64) NOT NULL COMMENT 'Business level id',
  levelName VARCHAR(128) NOT NULL COMMENT 'Level name',
  difficulty VARCHAR(32) NOT NULL COMMENT 'Difficulty',
  salaryRange VARCHAR(32) NOT NULL COMMENT 'Salary range',
  tags TEXT NOT NULL COMMENT 'Tag list JSON or text',
  requirement TEXT NOT NULL COMMENT 'Requirement JSON or text',
  options TEXT NOT NULL COMMENT 'Option list JSON or text',
  correctOptionIds TEXT NOT NULL COMMENT 'Correct option id list JSON or text',
  analysisDirection TEXT NULL COMMENT 'Analysis direction',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  isDelete TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
  PRIMARY KEY (id),
  UNIQUE KEY uk_levelId (levelId),
  KEY idx_difficulty_salaryRange (difficulty, salaryRange)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Level table';

CREATE TABLE IF NOT EXISTS answer_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  userId BIGINT UNSIGNED NOT NULL COMMENT 'User id',
  levelId VARCHAR(64) NOT NULL COMMENT 'Business level id',
  selectedOptionIds TEXT NOT NULL COMMENT 'Selected option ids JSON or text',
  correctOptionIds TEXT NOT NULL COMMENT 'Correct option ids JSON or text',
  clientSpendSeconds INT NOT NULL DEFAULT 0 COMMENT 'Client spend seconds',
  score INT NOT NULL DEFAULT 0 COMMENT 'Score',
  salaryChange INT NOT NULL DEFAULT 0 COMMENT 'Salary change amount',
  updatedSalary INT NOT NULL DEFAULT 10000 COMMENT 'Updated monthly salary',
  resultReport TEXT NOT NULL COMMENT 'Result report',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  isDelete TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
  PRIMARY KEY (id),
  KEY idx_userId_createTime (userId, createTime),
  KEY idx_userId_levelId (userId, levelId),
  KEY idx_levelId (levelId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Answer record table';
