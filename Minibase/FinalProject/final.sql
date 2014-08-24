PRAGMA FOREIGN_KEY = ON;

BEGIN TRANSACTION;

DROP TABLE IF EXISTS photo_users;
DROP TABLE IF EXISTS users_friend;
DROP TABLE IF EXISTS photo_files;
DROP TABLE IF EXISTS photo_user_links;
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
  `user_id` integer NOT NULL,
  `joindate` date,
  `username` varchar(20),
  `password` char(40),     
  `profile_pic_id` integer, 
  PRIMARY KEY (user_id),
  FOREIGN KEY (profile_pic_id) REFERENCES photo_files(photo_id)
);

CREATE TABLE `users_friend` (
  `friend_id` integer PRIMARY KEY AUTOINCREMENT,
  `user_id` integer,
  `friend_user_id` integer,
  UNIQUE (user_id, friend_user_id), 
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (friend_user_id) REFERENCES photo_users(user_id)
);

------Photo Tables------
CREATE TABLE `photo_files` (
  `photo_id` integer NOT NULL,
  `uploaddate` date,
  `uploadname` varchar(128),
  `caption` varchar(128),
  `filelocation` varchar(256),
  `album_id` integer,
  PRIMARY KEY (photo_id)
  FOREIGN KEY (album_id) REFERENCES photo_albums(album_id)
);

CREATE TABLE `photo_user_links` (
  `connection_id` integer NOT NULL,
  `user_id` integer,
  `photo_id` integer,
  PRIMARY KEY (connection_id),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (photo_id) REFERENCES photo_files(photo_id)
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
  `connection_id` integer PRIMARY KEY AUTOINCREMENT,
  `user_id` integer,
  `photo_id` integer,
  UNIQUE (user_id, photo_id), 
  FOREIGN KEY(user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY(photo_id) REFERENCES photo_files(photo_id)
);

CREATE TABLE `comment_like` (
  `connection_id` integer PRIMARY KEY AUTOINCREMENT,
  `user_id` integer,
  `comment_id` integer,
  UNIQUE (user_id, comment_id), 
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (comment_id) REFERENCES photo_comments(comment_id)
);

------Groups Tables------
CREATE TABLE `user_group` (
  `group_id` integer NOT NULL,
  `user_id` integer, 
  `joindate` date,
  `groupname` varchar(20),  
  `profile_pic_id` integer, 
  `about_text` varchar(128),
  PRIMARY KEY (group_id),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (profile_pic_id) REFERENCES photo_files(photo_id)
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
  `group_member_id` integer NOT NULL,
  `joindate` date,
  `group_id` integer,
  `user_id` integer,
  PRIMARY KEY (group_member_id),
  FOREIGN KEY (group_id) REFERENCES user_group(group_id),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id)
);

CREATE TABLE `group_comment_like` (
  `group_comment_like_id` integer PRIMARY KEY AUTOINCREMENT,
  `user_id` integer,
  `group_comment_id` integer,
  UNIQUE (user_id, group_comment_id), 
  FOREIGN KEY (group_comment_id) REFERENCES group_comments(comment_id),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id)
);

------Events Tables------
CREATE TABLE `events` (
  `event_id` integer NOT NULL,
  `eventdate` date,
  `eventname` varchar(20),
  `user_id` integer,    
  `profile_pic_id` integer, 
  PRIMARY KEY (event_id),
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (profile_pic_id) REFERENCES photo_files(photo_id)
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
  `event_members_id` integer NOT NULL,
  `joindate` date,
  `event_id` integer,
  `user_id` integer,
  PRIMARY KEY(event_members_id),
  FOREIGN KEY(event_id) REFERENCES events(event_id),
  FOREIGN KEY(user_id) REFERENCES photo_users(user_id)
);

CREATE TABLE `event_comment_like` (
  `event_comment_id` integer PRIMARY KEY AUTOINCREMENT,
  `user_id` integer,
  `comment_id` integer,
  UNIQUE (user_id, comment_id), 
  FOREIGN KEY (user_id) REFERENCES photo_users(user_id),
  FOREIGN KEY (comment_id) REFERENCES event_comments(comment_id)
);

------Albums Table------
CREATE TABLE `photo_albums` (
  `album_id` integer NOT NULL,
  `user_id` integer,
  `name_of_album` varchar(128),
  `description` varchar(128),
  PRIMARY KEY (album_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

COMMIT TRANSACTION;
