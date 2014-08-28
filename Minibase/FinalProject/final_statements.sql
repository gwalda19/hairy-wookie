-- make new users
insert into photo_users (joindate, username, password) values ("2014-06-09", "Mike Norris", "password");
insert into photo_users (joindate, username, password) values ("2013-09-09", "Sean Fast", "password");
insert into photo_users (joindate, username, password) values ("2014-10-09", "Dave Shanline", "password");
insert into photo_users (joindate, username, password) values ("2011-07-09", "Bill Annocki", "password");
insert into photo_users (joindate, username, password) values ("2012-04-18", "David Gwalthney", "radar");

-- make some friends
insert into users_friend values(1, 2);
insert into users_friend values(2, 1);

insert into users_friend values(2, 3);
insert into users_friend values(3, 2);

insert into users_friend values(4, 3);
insert into users_friend values(3, 4);

insert into users_friend values(1, 3);
insert into users_friend values(3, 1);

insert into users_friend values(5, 2);
insert into users_friend values(2, 5);

-- make new photo file
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2013-09-09", "somepic", "this is a caption", "/some/path/to/the/photo/somepic.jpg", 1);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2014-10-10", "mypic", "me", "/home/mypic.jpg", 2);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-07-23", "apic", "a thing", "/apic.jpg", 2);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2013-08-29", "pic", "you", "/usr/bin/pic.jpg", 3);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-18", "cat", "picture of a cat", "/usr/bin/cat.jpg", 2);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-18", "cat1", "cat playing a piano", "/usr/bin/cat1.jpg", 2);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-19", "cat2", "cat dancing", "/usr/bin/cat2.jpg", 2);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-19", "cat3", "cat and dog", "/usr/bin/cat3.jpg", 2);


-- make new photo likes
insert into photo_like values(2, 1);
insert into photo_like values(3, 1);
insert into photo_like values(3, 2);


-- make new photo comments
insert into photo_comments (user_id, photo_id, comment_text) values (1, 1, "This is a pic comment.");
insert into photo_comments (user_id, photo_id, comment_text) values (1, 1, "This is another pic comment.");
insert into photo_comments (user_id, photo_id, comment_text) values (2, 3, "I love it");


-- make some photo comment likes
insert into comment_like values(2, 2);
insert into comment_like values(4, 2);
insert into comment_like values(1, 1);


-- make a group and add some members
insert into user_group (founder_id, foundingdate, groupname, about_text) values (2, "2011-07-09", "YOLO", "You only live once");
insert into group_members (group_id, user_id, joindate) values (1, 1, "2014-08-26");
insert into group_members (group_id, user_id, joindate) values (1, 3, "2014-08-26");


-- make some group comments
insert into group_comments(user_id, group_id, comment_text) values (1, 1, "You wanted the best ... you got the best");
insert into group_comments(user_id, group_id, comment_text) values (2, 1, "Prepare for titanfall");


-- make some group comment likes
insert into group_comment_like(user_id, group_comment_id) values (1, 1);
insert into group_comment_like(user_id, group_comment_id) values (3, 2);


-- make an event and add some members
insert into events(eventdate, eventname, host_id) values ("2014-28-08", "Final Project Presentation", 3);
insert into event_members (event_id, user_id, joindate) values (1, 4, "2014-08-26");
insert into event_members (event_id, user_id, joindate) values (1, 1, "2014-08-26");


-- make some event comments
insert into event_comments(user_id, event_id, comment_text) values (3, 1, "Its better to look good then to feel good");
insert into event_comments(user_id, event_id, comment_text) values (4, 1, "Hello World");


-- make some event comment likes
insert into event_comment_like(user_id, comment_id) values (3, 1);
insert into event_comment_like(user_id, comment_id) values (1, 1);


-- make an album and add some photos to it
insert into photo_albums(owner_id, name_of_album, description) values (2, "Selfies", "Some of my best pics");
update photo_files  set  album_id = 1 where photo_id = 2;
update photo_files  set  album_id = 1 where photo_id = 3;
insert into photo_albums(owner_id, name_of_album, description) values (2, "Cats", "Some of the best cat pics");
update photo_files  set  album_id = 2 where photo_id = 5;
update photo_files  set  album_id = 2 where photo_id = 6;
update photo_files  set  album_id = 2 where photo_id = 7;
update photo_files  set  album_id = 2 where photo_id = 8;


SELECT * FROM photo_users;
SELECT * FROM users_friend;
SELECT * FROM photo_files;
SELECT * FROM photo_like;
SELECT * FROM photo_comments;
SELECT * FROM comment_like;
SELECT * FROM user_group;
SELECT * FROM group_members;
SELECT * FROM group_comments;
SELECT * FROM group_comment_like;
SELECT * FROM events;
SELECT * FROM event_members;
SELECT * FROM event_comments;
SELECT * FROM event_comment_like;
SELECT * FROM photo_albums;

--List all photos that user Sean has posted. (working)
SELECT photo_users.username, photo_files.uploadname
FROM photo_users
INNER JOIN photo_files
ON photo_users.user_id = photo_files.owner_id AND photo_users.username = "Sean Fast";

-- List all photos that user Sean has posted in album Cats.
--SELECT photo_users.username, photo_files.uploadname, photo_albums.name_of_album
--FROM photo_users, photo_files, photo_albums
--WHERE photo_users.user_id = photo_files.owner_id AND photo_users.username = "Sean Fast" AND photo_albums.name_of_album = "Cats";

--List all comments user Dave left on photo 1
SELECT photo_users.username, photo_files.uploadname, photo_comments.comment_text
FROM photo_users
INNER JOIN photo_files
INNER JOIN photo_comments
ON photo_users.username = "Dave Shanline"-- AND 



