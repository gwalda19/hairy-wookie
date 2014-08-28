-- make new users
insert into photo_users (joindate, username, password, age) values ("2014-06-09", "Mike Norris", "password", 28);
insert into photo_users (joindate, username, password, age) values ("2013-09-09", "Sean Fast", "password", 30);
insert into photo_users (joindate, username, password, age) values ("2014-10-09", "Dave Shanline", "password", 30);
insert into photo_users (joindate, username, password, age) values ("2011-07-09", "Bill Annocki", "password", 35);
insert into photo_users (joindate, username, password, age) values ("2012-04-18", "David Gwalthney", "radarRules", 27);
insert into photo_users (joindate, username, password, age) values ("2012-12-25", "Santa Clause", "xmasRoxs", 100);


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

insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-22", "dog", "dog chasing tail", "/usr/bin/dog.jpg", 5);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-22", "dog1", "catdog", "/usr/bin/dog1.jpg", 5);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-24", "meme", "Mr T", "/usr/bin/meme.jpg", 5);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-24", "meme1", "IBC", "/usr/bin/meme1.jpg", 5);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-25", "bomb", "Best Photo Bomb!", "/usr/bin/bomb.jpg", 5);
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-25", "bomb1", "Check out the Cat", "/usr/bin/bomb1.jpg", 5);


-- make new photo likes
insert into photo_like values(2, 1);
insert into photo_like values(3, 1);
insert into photo_like values(3, 2);
insert into photo_like values(4, 6);
insert into photo_like values(4, 8);

-- make new photo comments
insert into photo_comments (user_id, photo_id, comment_text) values (1, 1, "This is a pic comment.");
insert into photo_comments (user_id, photo_id, comment_text) values (1, 1, "This is another pic comment.");
insert into photo_comments (user_id, photo_id, comment_text) values (2, 3, "I love it");
insert into photo_comments (user_id, photo_id, comment_text) values (3, 1, "Awesome Picture!");
insert into photo_comments (user_id, photo_id, comment_text) values (3, 1, "Your lying it is.");
insert into photo_comments (user_id, photo_id, comment_text) values (3, 1, "somepic is great!");

-- make some photo comment likes
insert into comment_like values(2, 2);
insert into comment_like values(4, 2);
insert into comment_like values(1, 1);
insert into comment_like values(4, 4);
insert into comment_like values(4, 1);

-- make a group and add some members
insert into user_group (founder_id, foundingdate, groupname, about_text) values (2, "2011-07-09", "YOLO", "You only live once");
insert into group_members (group_id, user_id, joindate) values (1, 1, "2014-08-26");
insert into group_members (group_id, user_id, joindate) values (1, 3, "2014-08-26");
insert into group_members (group_id, user_id, joindate) values (1, 5, "2014-08-26");

insert into user_group (founder_id, foundingdate, groupname, about_text) values (5, "2014-08-28", "Final Project", "Database Final Project");
insert into group_members (group_id, user_id, joindate) values (2, 1, "2014-08-30");
insert into group_members (group_id, user_id, joindate) values (2, 2, "2014-08-30");
insert into group_members (group_id, user_id, joindate) values (2, 3, "2014-08-30");
insert into group_members (group_id, user_id, joindate) values (2, 4, "2014-08-30");
insert into group_members (group_id, user_id, joindate) values (2, 5, "2014-08-30");

insert into user_group (founder_id, foundingdate, groupname, about_text) values (1, "2013-01-23", "Weapon Control Systems", "Group for members of WCS");
insert into user_group (founder_id, foundingdate, groupname, about_text) values (6, "2011-07-14", "Santa's Elves", "All about building toys.");
insert into user_group (founder_id, foundingdate, groupname, about_text) values (3, "2014-08-28", "Display Systems", "Group for members of ADS");


-- make some group comments
insert into group_comments(user_id, group_id, comment_text) values (1, 1, "You wanted the best ... you got the best");
insert into group_comments(user_id, group_id, comment_text) values (2, 1, "Prepare for titanfall");

insert into group_comments(user_id, group_id, comment_text) values (5, 2, "This final is awesome!");
insert into group_comments(user_id, group_id, comment_text) values (1, 2, "How do we write SQL statements?");
insert into group_comments(user_id, group_id, comment_text) values (2, 2, "Go to the tutorial.");


-- make some group comment likes
insert into group_comment_like(user_id, group_comment_id) values (1, 1);
insert into group_comment_like(user_id, group_comment_id) values (3, 2);
insert into group_comment_like(user_id, group_comment_id) values (4, 2);
insert into group_comment_like(user_id, group_comment_id) values (4, 1);

insert into group_comment_like(user_id, group_comment_id) values (5, 5);
insert into group_comment_like(user_id, group_comment_id) values (4, 5);
insert into group_comment_like(user_id, group_comment_id) values (3, 5);


-- make an event and add some members
insert into events(eventdate, eventname, host_id) values ("2014-28-08", "Final Project Presentation", 3);
insert into event_members (event_id, user_id, joindate) values (1, 4, "2014-08-26");
insert into event_members (event_id, user_id, joindate) values (1, 1, "2014-08-26");
insert into event_members (event_id, user_id, joindate) values (1, 5, "2014-08-26");

insert into events(eventdate, eventname, host_id) values ("2014-12-25", "Santa's Workshop", 6);
insert into event_members (event_id, user_id, joindate) values (2, 2, "2014-12-26");
insert into event_members (event_id, user_id, joindate) values (2, 3, "2014-12-26");
insert into event_members (event_id, user_id, joindate) values (2, 6, "2014-12-26");

insert into events(eventdate, eventname, host_id) values ("2015-04-30", "David's Birthday", 5);
insert into events(eventdate, eventname, host_id) values ("2014-09-01", "Bill's Labor Day Party", 3);
insert into events(eventdate, eventname, host_id) values ("2014-10-31", "Sean's Halloween Bash", 2);


-- make some event comments
insert into event_comments(user_id, event_id, comment_text) values (3, 1, "Its better to look good then to feel good");
insert into event_comments(user_id, event_id, comment_text) values (4, 1, "Hello World");

insert into event_comments(user_id, event_id, comment_text) values (2, 2, "Where are we meeting up?");
insert into event_comments(user_id, event_id, comment_text) values (3, 2, "Santa's Workshop duh!");
insert into event_comments(user_id, event_id, comment_text) values (6, 2, "Yeah the North Pole.");


-- make some event comment likes
insert into event_comment_like(user_id, comment_id) values (3, 1);
insert into event_comment_like(user_id, comment_id) values (1, 1);
insert into event_comment_like(user_id, comment_id) values (4, 1);

insert into event_comment_like(user_id, comment_id) values (2, 4);
insert into event_comment_like(user_id, comment_id) values (3, 5);
insert into event_comment_like(user_id, comment_id) values (6, 4);

-- make an album and add some photos to it
insert into photo_albums(owner_id, name_of_album, description) values (2, "Selfies", "Some of my best pics");
update photo_files  set  album_id = 1 where photo_id = 2;
update photo_files  set  album_id = 1 where photo_id = 3;
insert into photo_albums(owner_id, name_of_album, description) values (2, "Cats", "Some of the best cat pics");
update photo_files  set  album_id = 2 where photo_id = 5;
update photo_files  set  album_id = 2 where photo_id = 6;
update photo_files  set  album_id = 2 where photo_id = 7;
update photo_files  set  album_id = 2 where photo_id = 8;

insert into photo_albums(owner_id, name_of_album, description) values (5, "Dogs", "Some of the best dog pics");
update photo_files  set  album_id = 3 where photo_id = 9;
update photo_files  set  album_id = 3 where photo_id = 10;
insert into photo_albums(owner_id, name_of_album, description) values (5, "Memes", "Some of the best meme pics on the net");
update photo_files  set  album_id = 4 where photo_id = 11;
update photo_files  set  album_id = 4 where photo_id = 12;
insert into photo_albums(owner_id, name_of_album, description) values (5, "Photo Bombs", "Some of the best photo bombs on the net");
update photo_files  set  album_id = 5 where photo_id = 13;
update photo_files  set  album_id = 5 where photo_id = 14;


-- Test some foreign key constraints
update users_friend set friend_user_id = 8 where user_id = 3 AND friend_user_id = 2; --No user id 8
update photo_files set owner_id = 8 where owner_id = 3;  --No user 8
insert into group_members (group_id, user_id, joindate) values (7, 1, "2014-08-26");  --No group 7
insert into event_comment_like(user_id, comment_id) values (6, 7);   --No comment 7
insert into photo_albums(owner_id, name_of_album, description) values (8, "Fail", "This insert should fail");

-- Test Check constraints
insert into photo_users (joindate, username, password, age) values ("2012-12-25", "John Robinson", "TestFail", 12); --Age < 15
insert into photo_users (joindate, username, password, age) values ("2012-12-25", "John Robinson", "Fail", 22);  --Password < 8
insert into photo_users (joindate, username, password, age) values ("2012-12-25", "John Robinson", "FailFailFailFailFailFailFailFailFailFailz", 22); --Password > 40
insert into photo_files (uploaddate, uploadname, caption, filelocation, owner_id) values ("2012-04-22", "a", "Fail", "Test", 5);  --Uploadname < 3
insert into user_group (founder_id, foundingdate, groupname, about_text) values (1, "2013-01-23", "WCS", "Test Fail");  --Group name < 4

-- Test Unique constraints
insert into photo_users (joindate, username, password, age) values ("2012-12-25", "David Gwalthney", "TestFail", 16); --username not unique
insert into user_group (founder_id, foundingdate, groupname, about_text) values (1, "2013-01-23", "YOLO", "Test Fail");  --Group name not unique
insert into events(eventdate, eventname, host_id) values ("2016-04-30", "David's Birthday", 5);   --Event name already exists.


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
ON photo_users.user_id = photo_files.owner_id
WHERE photo_users.username = "Sean Fast";

-- List all photos that user Sean has posted in album Cats. (working)
SELECT photo_users.username, photo_files.uploadname, photo_albums.name_of_album
FROM photo_users
INNER JOIN photo_files
INNER JOIN photo_albums
ON    photo_users.user_id  = photo_albums.owner_id
  AND photo_files.album_id = photo_albums.album_id
WHERE photo_users.username = "Sean Fast" AND photo_albums.name_of_album = "Cats";

--List all comments user Dave left on photo somepic.  (working)
SELECT photo_users.username, photo_files.uploadname, photo_comments.comment_text
FROM photo_users
INNER JOIN photo_files
INNER JOIN photo_comments
ON  photo_comments.photo_id = photo_files.photo_id AND photo_comments.user_id = photo_users.user_id
WHERE photo_users.username = "Dave Shanline" AND photo_files.uploadname = "somepic";

--List all users in group YOLO that are attending event Final Project Presentation. (working)
SELECT photo_users.username, user_group.groupname, events.eventname
FROM photo_users
INNER JOIN user_group
INNER JOIN events
INNER JOIN group_members
INNER JOIN event_members
ON photo_users.user_id = group_members.user_id AND photo_users.user_id = event_members.user_id
WHERE user_group.groupname = "YOLO" AND events.eventname = "Final Project Presentation";

--List all users that are in group YOLO that are also friends. (working)
SELECT photo_users.username, user_group.groupname, temp_photo_users.username
FROM photo_users, photo_users temp_photo_users
INNER JOIN user_group
INNER JOIN group_members, group_members temp_group_members
INNER JOIN users_friend
ON     photo_users.user_id = group_members.user_id
   AND photo_users.user_id = users_friend.user_id
   AND users_friend.friend_user_id = temp_group_members.user_id
   AND users_friend.friend_user_id = temp_photo_users.user_id
WHERE user_group.groupname = "YOLO";

--List all users that are friends of Sean Fast. (working)
SELECT photo_users.username, temp_photo_users.username
FROM photo_users, photo_users temp_photo_users
INNER JOIN users_friend
ON photo_users.user_id = users_friend.user_id AND users_friend.friend_user_id = temp_photo_users.user_id
WHERE photo_users.username = "Sean Fast";

--List all items that user Bill Annocki has liked. (working)
SELECT  photo_users.username   , photo_like.photo_id,
        comment_like.comment_id, group_comment_like.group_comment_id,
        event_comment_like.comment_id
FROM photo_users
INNER JOIN photo_like
INNER JOIN comment_like
INNER JOIN group_comment_like
INNER JOIN event_comment_like
ON     photo_users.user_id = photo_like.user_id
   AND photo_users.user_id = comment_like.user_id
   AND photo_users.user_id = group_comment_like.user_id
   AND photo_users.user_id = event_comment_like.user_id
WHERE photo_users.username = "Bill Annocki";


