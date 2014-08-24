-- make new user
insert into photo_users values(0, "2014-06-09", "Mike", "Norris", NULL);
insert into photo_users values(1, "2013-09-09", "Sean", "Fast", NULL);
insert into photo_users values(2, "2014-10-09", "Dave", "Shanline", NULL);
insert into photo_users values(3, "2011-07-09", "Bill", "Annocki", NULL);


-- make some friends
insert into users_friend values(NULL, 1, 2);
insert into users_friend values(NULL, 2, 2);
insert into users_friend values(NULL, 0, 3);
  

insert into photo_user_links values(0, 1, 0);
insert into photo_user_links values(1, 1, 1);
insert into photo_user_links values(2, 2, 1);


-- make new photo file
insert into photo_files values(0, "2013-09-09", "somepic.jpg", "this is a caption", "/some/path/to/the/photo/pic.jpg", NULL);
insert into photo_files values(1, "2014-10-10", "mypic.jpg", "me", "/usr/pic.jpg", NULL);


-- make new photo likes
insert into photo_like values(NULL, 2, 1);
insert into photo_like values(NULL, 3, 1);


-- make new photo comments
insert into photo_comments values(NULL, 0, 1, "This is a pic comment.");
insert into photo_comments values(NULL, 1, 1, "This is another pic comment.");


SELECT * FROM photo_users;
SELECT * FROM photo_files;
SELECT * FROM photo_user_links;
SELECT * FROM photo_like;
SELECT * FROM photo_comments;
SELECT * FROM users_friend;

