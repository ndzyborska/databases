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


INSERT INTO Post (message,topicId,personId) VALUES ("test post in topic",1,3);
INSERT INTO Post (message,topicId,personId) VALUES ("test post in topic 2",2,4);
INSERT INTO Post (message,topicId,personId) VALUES ("test post in topic",3,3);
INSERT INTO Post (message,topicId,personId) VALUES ("test post in topic 2",4,4);
INSERT INTO Post (message,topicId,personId) VALUES ("test post in topic",5,3);
INSERT INTO Post (message,topicId,personId) VALUES ("test post in topic 2",6,4);
INSERT INTO Post (message,topicId,personId) VALUES ("testx post in topic",1,2);
INSERT INTO Post (message,topicId,personId) VALUES ("testx post in topic 2",2,2);
INSERT INTO Post (message,topicId,personId) VALUES ("testx post in topic",3,2);
INSERT INTO Post (message,topicId,personId) VALUES ("testx post in topic 2",4,2);
INSERT INTO Post (message,topicId,personId) VALUES ("testx post in topic",5,1);
INSERT INTO Post (message,topicId,personId) VALUES ("testx post in topic 2",6,1);
