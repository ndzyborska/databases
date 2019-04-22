INSERT INTO Person (name,username,stuId) VALUES ("userA", "uA123","12345");
INSERT INTO Person (name,username,stuId) VALUES ("userB", "uB456","32432");
INSERT INTO Person (name,username,stuId) VALUES ("userC", "uC789","99999");
INSERT INTO Person (name,username) VALUES ("userD", "uD321");

INSERT INTO Forum (name) VALUES ("databases");
INSERT INTO Forum (name) VALUES ("machine learning");
INSERT INTO Forum (name) VALUES ("calculus");

INSERT INTO Topic (title,message,forumId,personId) VALUES ("test topic","test message",1,1);
INSERT INTO Topic (title,message,forumId,personId) VALUES ("test topic 2","test message 2",1,2);
INSERT INTO Topic (title,message,forumId,personId) VALUES ("ml topic1","test message",2,1);
INSERT INTO Topic (title,message,forumId,personId) VALUES ("ml topic 2","test message 2",2,2);
INSERT INTO Topic (title,message,forumId,personId) VALUES ("calc topic1","test message",3,3);
INSERT INTO Topic (title,message,forumId,personId) VALUES ("calc topic 2","test message 2",3,4);


INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("test post in topic",1,3,'2019-04-22 19:52:21');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("test post in topic 2",2,4,'2019-04-22 19:52:22');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("test post in topic",3,3,'2019-04-22 19:52:23');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("test post in topic 2",4,4,'2019-04-22 19:52:24');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("test post in topic",5,3,'2019-04-22 19:52:25');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("test post in topic 2",6,4,'2019-04-22 19:52:26');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("testx post in topic",1,2,'2019-04-22 19:52:27');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("testx post in topic 2",2,2,'2019-04-22 19:52:28');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("testx post in topic",3,2,'2019-04-22 19:52:29');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("testx post in topic 2",4,2,'2019-04-22 19:52:30');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("testx post in topic",5,1,'2019-04-22 19:52:46');
INSERT INTO Post (message,topicId,personId,timePosted) VALUES ("testx post in topic 2",6,1,'2019-04-22 19:52:58');
