DROP DATABASE IF EXISTS professor;
CREATE DATABASE professor;
USE professor;
-- 学校
DROP TABLE IF EXISTS institute;
CREATE TABLE institute(
	institute_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	institute_name VARCHAR(128) NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 用户
DROP TABLE IF EXISTS xuser;
CREATE TABLE xuser(
	user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(128) NOT NULL,
	student_num VARCHAR(128),
	password VARCHAR(128) NOT NULL,
	institute_id BIGINT NOT NULL,
	role TINYINT NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 班级
DROP TABLE IF EXISTS xclass;
CREATE TABLE xclass(
	class_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	owner BIGINT NOT NULL,
	class_name VARCHAR(128) NOT NULL
	-- other_score_weight DECIMAL(3, 2) NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 用户-班级联系
DROP TABLE IF EXISTS student_class_rel;
CREATE TABLE student_class_rel(
	student_id BIGINT NOT NULL,
	class_id BIGINT NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 题目
DROP TABLE IF EXISTS problem;
CREATE TABLE problem(
	problem_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT NOT NULL,
	content BLOB(3145728) NOT NULL,
	type TINYINT NOT NULL,
	total INTEGER
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 试卷
DROP TABLE IF EXISTS problem_set;
CREATE TABLE problem_set(
	problem_set_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	owner BIGINT NOT NULL,
	problem_set_name VARCHAR(128) NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 题目-试卷联系
DROP TABLE IF EXISTS problem_set_problem_rel;
CREATE TABLE problem_set_problem_rel(
	problem_set_id BIGINT NOT NULL,
	problem_id BIGINT NOT NULL,
	`order` INTEGER NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 试卷-班级联系
DROP TABLE IF EXISTS problem_set_class_rel;
CREATE TABLE problem_set_class_rel(
	problem_set_id BIGINT NOT NULL,
	class_id BIGINT NOT NULL,
	start_time DATETIME NOT NULL,
	end_time DATETIME NOT NULL,
	problem_set_weight DECIMAL(3, 2) NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 平时成绩
DROP TABLE IF EXISTS other_score;
CREATE TABLE other_score(
	class_id BIGINT NOT NULL,
	student_id BIGINT NOT NULL,
	other_score INTEGER NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 答题卡
DROP TABLE IF EXISTS answer_set;
CREATE TABLE answer_set(
	answer_set_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	student_id BIGINT NOT NULL,
	class_id BIGINT NOT NULL,
	problem_set_id BIGINT NOT NULL,
	face_check INTEGER NOT NULL,
	success_check INTEGER NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 选择题答案
DROP TABLE IF EXISTS simple_answer;
CREATE TABLE simple_answer(
	answer_set_id BIGINT NOT NULL,
	answer TINYINT NOT NULL,
	`order` INTEGER NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 大题答案
DROP TABLE IF EXISTS graphic_answer;
CREATE TABLE graphic_answer(
	answer_set_id BIGINT NOT NULL,
	content VARCHAR(500) NOT NULL,
	score INTEGER,
	`order` INTEGER NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;
-- 日志
DROP TABLE IF EXISTS xlog;
CREATE TABLE xlog(
	user_id BIGINT NOT NULL,
	time DATETIME NOT NULL,
	content VARCHAR(1024) NOT NULL
)ENGINE = InnoDB DEFAULT CHARSET = UTF8;