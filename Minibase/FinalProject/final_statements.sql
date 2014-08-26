-- make new user
insert into photo_users values(NULL, "2014-06-09", "Mike Norris", "password", NULL);
insert into photo_users values(NULL, "2013-09-09", "Sean Fast", "password", NULL);
insert into photo_users values(NULL, "2014-10-09", "Dave Shanline", "password", NULL);
insert into photo_users values(NULL, "2011-07-09", "Bill Annocki", "password", NULL);


-- make some friends
insert into users_friend values(1, 2);
insert into users_friend values(2, 1);

insert into users_friend values(2, 0);
insert into users_friend values(0, 2);

insert into users_friend values(0, 3);
insert into users_friend values(3, 0);
  

-- make new photo file
insert into photo_files values(NULL, "2013-09-09", "somepic.jpg", "this is a caption", "/some/path/to/the/photo/pic.jpg", NULL);
insert into photo_files values(NULL, "2014-10-10", "mypic.jpg", "me", "/usr/pic.jpg", NULL);

-- make new photo likes
insert into photo_like values(2, 1);
insert into photo_like values(3, 1);


-- make new photo comments
insert into photo_comments values(NULL, 0, 1, "This is a pic comment.");
insert into photo_comments values(NULL, 1, 1, "This is another pic comment.");


-- make some photo comment likes
insert into comment_like values(2, 1);
insert into comment_like values(0, 1);

SELECT * FROM photo_users;
SELECT * FROM photo_files;
SELECT * FROM photo_user_links;
SELECT * FROM photo_like;
SELECT * FROM photo_comments;
SELECT * FROM comment_like;
SELECT * FROM users_friend;

