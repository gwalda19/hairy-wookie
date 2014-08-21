PRAGMA FOREIGN_KEY = ON;

BEGIN TRANSACTION;

DROP TABLE IF EXISTS photo_users;
DROP TABLE IF EXISTS photo_files;
DROP TABLE IF EXISTS photo_user_links;
DROP TABLE IF EXISTS photo_comments;
DROP TABLE IF EXISTS user_group;
DROP TABLE IF EXISTS group_comments;
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS group_comment_like;
DROP TABLE IF EXISTS photo_like;
DROP TABLE IF EXISTS comment_like;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS event_comments;
DROP TABLE IF EXISTS event_members;
DROP TABLE IF EXISTS event_comment_like;
DROP TABLE IF EXISTS photo_albums;

CREATE TABLE `photo_users` (
  `user_id` int(6) NOT NULL,
  `joindate` date,
  `username` varchar(20),
  `password` char(40),     
  `profile_pic_id` int(8), 
  PRIMARY KEY  (`user_id`)
);

CREATE TABLE `photo_files` (
  `photo_id` int(8) NOT NULL,
  `uploaddate` date,
  `uploadname` varchar(128),
  `caption` varchar(128),
  `filelocation` varchar(256),
  `album_id` int(8),
  PRIMARY KEY  (`photo_id`)
);

CREATE TABLE `photo_user_links` (
  `connection_id` int(8) NOT NULL,
  `user_id` int(6),
  `photo_id` int(8),
  PRIMARY KEY  (`connection_id`)
);

CREATE TABLE `photo_comments` (
  `comment_id` int(8) NOT NULL,
  `user_id` int(6), 
  `photo_id` int(8),
  `comment_text` varchar(128),
  PRIMARY KEY  (`comment_id`)
);

-- new tables------------------------------------------------------------
CREATE TABLE `user_group` (
  `group_id` int(6) NOT NULL,
  `user_id` int(6), 
  `joindate` date,
  `groupname` varchar(20),  
  `profile_pic_id` int(8), 
  `about_text` varchar(128),
  PRIMARY KEY  (`group_id`)
);

CREATE TABLE `group_comments` (
  `comment_id` int(8) NOT NULL,
  `user_id` int(6), 
  `group_id` int(6),
  `comment_text` varchar(128),
  PRIMARY KEY  (`comment_id`)
);

CREATE TABLE `group_members` (
  `group_member_id` int(8) NOT NULL,
  `joindate` date,
  `group_id` int(6),
  PRIMARY KEY(group_member_id),
  FOREIGN KEY(group_id) REFERENCES user_group(group_id)
);

CREATE TABLE `group_comment_like` (
  `group_comment_like_id` int(8) NOT NULL,
  `user_id` int(6),
  `group_comment_id` int(8),
  PRIMARY KEY  (`group_comment_like_id`),
  FOREIGN KEY (group_comment_id) REFERENCES group_comments(comment_id)
);

CREATE TABLE `photo_like` (
  `connection_id` int(8) NOT NULL,
  `user_id` int(6),
  `photo_id` int(8),
  PRIMARY KEY  (`connection_id`)
);

CREATE TABLE `comment_like` (
  `connection_id` int(8) NOT NULL,
  `user_id` int(6),
  `comment_id` int(8),
  PRIMARY KEY  (`connection_id`)
);

CREATE TABLE `events` (
  `event_id` int(6) NOT NULL,
  `eventdate` date,
  `eventname` varchar(20),
  `user_id` int(6),    
  `profile_pic_id` int(8), 
  PRIMARY KEY  (`event_id`)
);

CREATE TABLE `event_comments` (
  `comment_id` int(8) NOT NULL,
  `user_id` int(6), 
  `event_id` int(6),
  `comment_text` varchar(128),
  PRIMARY KEY  (`comment_id`)
);

CREATE TABLE `event_members` (
  `event_members_id` int(8) NOT NULL,
  `joindate` date,
  `event_id` int(6),
  PRIMARY KEY(event_members_id),
  FOREIGN KEY(event_id) REFERENCES events(event_id)
);

CREATE TABLE `event_comment_like` (
  `event_comment_id` int(8) NOT NULL,
  `user_id` int(6),
  `comment_id` int(8),
  PRIMARY KEY  (`event_comment_id`)
);

CREATE TABLE `photo_albums` (
  `album_id` int(8) NOT NULL,
  `user_id`  int(6),
  `name_of_album` varchar(128),
  `description`   varchar(128),
  PRIMARY KEY(album_id),
  FOREIGN KEY(user_id) REFERENCES users(user_id)
);

-- make new user
insert into photo_users values(0, "2014-09-09", "mike", "norris", "");
insert into photo_users values(1, "2014-09-09", "sean", "fast", "");
insert into photo_users values(2, "2014-09-09", "dave", "shaneline", "");
insert into photo_users values(3, "2014-09-09", "bill", "annocki", "");

  
insert into photo_user_links values(0, "1", "1");
insert into photo_user_links values(1, "1", "1");
insert into photo_user_links values(2, "1", "1");


-- make new photo file
insert into photo_files values(0, "2013-09-09", "somepic.jpg", "this is a caption", "/some/path/to/the/photo/pic.jpg");


-- make new photo_comments
insert into photo_comments values(0, "1", "1", "This is a pic comment.");

SELECT * FROM photo_users;
SELECT * FROM photo_files;
SELECT * FROM photo_user_links;
SELECT * FROM photo_comments;

COMMIT;
