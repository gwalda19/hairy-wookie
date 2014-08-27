PRAGMA FOREIGN_KEYS = ON;

BEGIN TRANSACTION;

DROP TABLE IF EXISTS photo_users;
DROP TABLE IF EXISTS users_friend;
DROP TABLE IF EXISTS photo_files;
DROP TABLE IF EXISTS photo_comments;
DROP TABLE IF EXISTS photo_like;
DROP TABLE IF EXISTS comment_like;
DROP TABLE IF EXISTS user_group;
DROP TABLE IF EXISTS group_comments;
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS group_comment_like;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS event_comments;
DROP TABLE IF EXISTS event_members;
DROP TABLE IF EXISTS event_comment_like;
DROP TABLE IF EXISTS photo_albums;

------Users Tables------
CREATE TABLE `photo_users` (
  `user_id` integer PRIMARY KEY AUTOINCREMENT,
  `joindate` date,
  `username` varchar(20),
  `password` char(40),     
  `profile_pic_id` integer, 
  FOREIGN KEY (profile_pic_id) REFERENCES photo_files(photo_id)
);

CREATE TABLE `users_friend` (
  `user_id` integer,
  `friend_user_id` integer,
  PRIMARY KEY (user_id, friend_user_id), 
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (friend_user_id) REFERENCES photo_users(user_id)
);

------Photo Tables------
CREATE TABLE `photo_files` (
  `photo_id` integer PRIMARY KEY AUTOINCREMENT,
  `uploaddate` date,
  `uploadname` varchar(128),
  `caption` varchar(128),
  `filelocation` varchar(256),
  `owner_id` integer,
  `album_id` integer,
  FOREIGN KEY (album_id) REFERENCES photo_albums(album_id),
  FOREIGN KEY (owner_id) REFERENCES photo_users(user_id)
);

CREATE TABLE `photo_comments` (
  `comment_id` integer PRIMARY KEY AUTOINCREMENT,
  `user_id` integer, 
  `photo_id` integer,
  `comment_text` varchar(128),
  FOREIGN KEY(user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY(photo_id) REFERENCES photo_files(photo_id)
);

CREATE TABLE `photo_like` (
  `user_id` integer,
  `photo_id` integer,
  PRIMARY KEY (user_id, photo_id), 
  FOREIGN KEY(user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY(photo_id) REFERENCES photo_files(photo_id)
);

CREATE TABLE `comment_like` (
  `user_id` integer,
  `comment_id` integer,
  PRIMARY KEY (user_id, comment_id), 
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (comment_id) REFERENCES photo_comments(comment_id)
);

------Groups Tables------
CREATE TABLE `user_group` (
  `group_id` integer PRIMARY KEY AUTOINCREMENT,
  `founder_id` integer, 
  `foundingdate` date,
  `groupname` varchar(20),  
  `group_pic_id` integer, 
  `about_text` varchar(128),
  FOREIGN KEY (founder_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (group_pic_id) REFERENCES photo_files(photo_id)
);

CREATE TABLE `group_comments` (
  `comment_id` integer PRIMARY KEY AUTOINCREMENT,
  `user_id` integer, 
  `group_id` integer,
  `comment_text` varchar(128),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (group_id) REFERENCES user_group(group_id)
);

CREATE TABLE `group_members` (
  `joindate` date,
  `group_id` integer,
  `user_id` integer,
  PRIMARY KEY (user_id, group_id),
  FOREIGN KEY (group_id) REFERENCES user_group(group_id),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id)
);

CREATE TABLE `group_comment_like` (
  `user_id` integer,
  `group_comment_id` integer,
  PRIMARY KEY (user_id, group_comment_id), 
  FOREIGN KEY (group_comment_id) REFERENCES group_comments(comment_id),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id)
);

------Events Tables------
CREATE TABLE `events` (
  `event_id` integer PRIMARY KEY AUTOINCREMENT,
  `eventdate` date,
  `eventname` varchar(20),
  `host_id` integer,  
  `event_pic_id` integer, 
  FOREIGN KEY (host_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (event_pic_id) REFERENCES photo_files(photo_id)
);

CREATE TABLE `event_comments` (
  `comment_id` integer PRIMARY KEY AUTOINCREMENT,
  `user_id` integer, 
  `event_id` integer,
  `comment_text` varchar(128),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (event_id) REFERENCES events(event_id)
);

CREATE TABLE `event_members` (
  `joindate` date,
  `event_id` integer,
  `user_id` integer,
  PRIMARY KEY(event_id, user_id),
  FOREIGN KEY(event_id) REFERENCES events(event_id),
  FOREIGN KEY(user_id) REFERENCES photo_users(user_id)
);

CREATE TABLE `event_comment_like` (
  `user_id` integer,
  `comment_id` integer,
  PRIMARY KEY (user_id, comment_id), 
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (comment_id) REFERENCES event_comments(comment_id)
);

------Albums Table------
CREATE TABLE `photo_albums` (
  `album_id` integer PRIMARY KEY AUTOINCREMENT,
  `owner_id` integer,
  `name_of_album` varchar(128),
  `description` varchar(128),
  FOREIGN KEY (owner_id) REFERENCES photo_users(user_id)
);

COMMIT TRANSACTION;
