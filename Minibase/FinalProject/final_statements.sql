-- make new user
insert into photo_users values(0, "2014-09-09", "mike", "norris", "");
insert into photo_users values(1, "2014-09-09", "sean", "fast", "");
insert into photo_users values(2, "2014-09-09", "dave", "shaneline", "");
insert into photo_users values(3, "2014-09-09", "bill", "annocki", "");

  
insert into photo_user_links values(0, "1", "0");
insert into photo_user_links values(1, "1", "1");
insert into photo_user_links values(2, "2", "1");


-- make new photo file
insert into photo_files values(0, "2013-09-09", "somepic.jpg", "this is a caption", "/some/path/to/the/photo/pic.jpg", NULL);
insert into photo_files values(1, "2014-10-10", "mypic.jpg", "me", "/usr/pic.jpg", NULL);


-- make new photo_comments
insert into photo_comments values(0, "1", "1", "This is a pic comment.");

SELECT * FROM photo_users;
SELECT * FROM photo_files;
SELECT * FROM photo_user_links;
SELECT * FROM photo_comments;
