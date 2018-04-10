create database TP2_output;

use TP2_output;

create table texte(
	Id int NOT NULL AUTO_INCREMENT,
	En text,
	Fr text,
	PRIMARY KEY (Id)
);

create table image(
	Id int NOT NULL AUTO_INCREMENT,
	Orig mediumblob,
	F1 mediumblob,
	F2 mediumblob,
	PRIMARY KEY (Id)
);
