INSERT INTO Person (name,username,stuId) VALUES ("userA", "uA123","12345");
INSERT INTO Person (name,username,stuId) VALUES ("userB", "uB456","32432");
INSERT INTO Person (name,username,stuId) VALUES ("userC", "uC789","99999");
INSERT INTO Person (name,username) VALUES ("userD", "uD321");

INSERT INTO Forum (name) VALUES ("databases");

INSERT INTO Topic (title,message,forumId,personId) VALUES ("test topic","test message",1,1);
INSERT INTO Topic (title,message,forumId,personId) VALUES ("test topic 2","test message 2",1,2);

INSERT INTO Post (message,topicId,personId) VALUES ("test post in topic",1,3);
INSERT INTO Post (message,topicId,personId) VALUES ("test post in topic 2",2,4); 
