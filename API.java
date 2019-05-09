package uk.ac.bris.cs.databases.cwk2;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.AdvancedForumSummaryView;
import uk.ac.bris.cs.databases.api.AdvancedForumView;
import uk.ac.bris.cs.databases.api.ForumSummaryView;
import uk.ac.bris.cs.databases.api.ForumView;
import uk.ac.bris.cs.databases.api.AdvancedPersonView;
import uk.ac.bris.cs.databases.api.PostView;
import uk.ac.bris.cs.databases.api.Result;
import uk.ac.bris.cs.databases.api.PersonView;
import uk.ac.bris.cs.databases.api.SimpleForumSummaryView;
import uk.ac.bris.cs.databases.api.SimpleTopicView;
import uk.ac.bris.cs.databases.api.SimpleTopicSummaryView;
import uk.ac.bris.cs.databases.api.TopicView;
import uk.ac.bris.cs.databases.api.TopicSummaryView;
import uk.ac.bris.cs.databases.api.SimplePostView;

/**
 *
 * @author csxdb
 */
public class API implements APIProvider {

    private final Connection c;

    public API(Connection c) {
        this.c = c;
    }

    /* A.1 */

    @Override
    public Result<Map<String, String>> getUsers() {

        try {

            Map<String,String> map = new HashMap<String,String>();
            String name;
            String username;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT name, username FROM Person;"
            );
            ResultSet r = s.executeQuery();
            while (r.next()) {
                name = r.getString("name");
                username = r.getString("username");

                map.put(username,name);
            }
            s.close();

            return Result.success(map);

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<PersonView> getPersonView(String username) {

        if (username == "" || username == null) return Result.failure("getPersonView: username cannot be empty!");

        try {

            String name;
            String user;
            String studentId;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT name, username, stuId FROM Person WHERE username = ?;"
            );

            s.setString(1, username);

            ResultSet r = s.executeQuery();

            if (r.next()) {
                name = r.getString("name");
                user = r.getString("username");
                studentId = r.getString("stuId");
            }
            else return Result.failure("getPersonView: person with this username does not exist!");

            s.close();

            if (studentId == null) studentId = "";

            return Result.success(new PersonView(name, username, studentId));

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result addNewPerson(String name, String username, String studentId) {

        if (name == "" || name == null) return Result.failure("addNewPerson: name cannot be empty!");
        if (username == "" || username == null) return Result.failure("addNewPerson: username cannot be empty!");
        if (studentId == "") return Result.failure("addNewPerson: studentId cannot be empty!");

        try {

            PreparedStatement s = this.c.prepareStatement(
                "INSERT INTO Person (name, username, stuId) VALUES (?, ?, ?);"
            );

            s.setString(1, name);
            s.setString(2, username);
            s.setString(3, studentId);

            s.executeQuery();
            s.close();
            c.commit();

            return Result.success();

            } catch (SQLException e) {
                try {
                    c.rollback();
                } catch (SQLException f) {
                    return Result.fatal(f.getMessage());
                }
                return Result.fatal(e.getMessage());
            }
        }

    /* A.2 */

    @Override
    public Result<List<SimpleForumSummaryView>> getSimpleForums() {

        try {

            int id;
            String name;
            List<SimpleForumSummaryView> list = new ArrayList<SimpleForumSummaryView>();

            PreparedStatement s = this.c.prepareStatement(
                "SELECT id, name FROM Forum;"
            );

            ResultSet r = s.executeQuery();

            while (r.next()) {
                id = r.getInt("id");
                name = r.getString("name");

                list.add(new SimpleForumSummaryView(id, name));
            }
            s.close();

            return Result.success(list);

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result createForum(String title) {

        if (title == "" || title == null) return Result.failure("createForum: title cannot be empty!");

        try {

            PreparedStatement s = this.c.prepareStatement(
                "SELECT * FROM Forum WHERE Forum.name = ?;"
            );
            s.setString(1, title);
            ResultSet r = s.executeQuery();

            if (r.next()) return Result.failure("createForum: forum with this title already exists!");

            s.close();

            s = this.c.prepareStatement(
                "INSERT INTO Forum (name) VALUES (?);"
            );

            s.setString(1, title);
            s.executeQuery();
            s.close();
            c.commit();

            return Result.success();

        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }
    }

    /* A.3 */

    @Override
    public Result<List<ForumSummaryView>> getForums() {

        try {

            String temp;
            int topicId;
            List<ForumSummaryView> list = new ArrayList<ForumSummaryView>();
            String name;
            String topicName;
            int id;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT Forum.id, Forum.name, Topic.id, Topic.title, Post.timePosted FROM Post " +
                "JOIN ( " +
                "    SELECT MAX(Post.timePosted) AS maxTime FROM Post " +
                "    JOIN Topic ON Topic.id = Post.topicId " +
                "    JOIN Forum ON Forum.id = Topic.forumId " +
                "    GROUP BY Forum.id ) a ON a.maxTime = Post.timePosted " +
                "JOIN Topic ON Topic.id = Post.topicId " +
                "RIGHT OUTER JOIN Forum ON Forum.id = Topic.forumId " +
                "ORDER BY Forum.name;"
            );

            ResultSet r = s.executeQuery();

            while (r.next()) {
                id = r.getInt("Forum.id");
                name = r.getString("Forum.name");
                topicName = r.getString("Topic.title");
                temp = r.getString("Topic.id");

                if (temp == null) {
                    list.add(new ForumSummaryView(id,name,null));
                }
                else {
                    topicId = Integer.parseInt(temp);
                    list.add(new ForumSummaryView(id, name, new SimpleTopicSummaryView(topicId, id, topicName)));
                }
            }

            s.close();

            return Result.success(list);

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<ForumView> getForum(int id) {

        try {

            String forumTitle;
            String topicTitle;
            int topicId;
            List<SimpleTopicSummaryView> topicList = new ArrayList<SimpleTopicSummaryView>();

            PreparedStatement s = this.c.prepareStatement(
                "SELECT name FROM Forum WHERE id = ?;"
            );
            s.setInt(1,id);
            ResultSet r = s.executeQuery();

            if (r.next()) forumTitle = r.getString("name");
            else return Result.failure("getForum: Issue with id!");

            s.close();

            s = this.c.prepareStatement(
                "SELECT id, title FROM Topic WHERE forumId = ?;"
            );
            s.setInt(1,id);
            r = s.executeQuery();

            while (r.next()) {
                topicTitle = r.getString("title");
                topicId = Integer.parseInt(r.getString("id"));

                topicList.add(new SimpleTopicSummaryView(topicId,id,topicTitle));
            }

            s.close();

            return Result.success(new ForumView(id,forumTitle,topicList));

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<SimpleTopicView> getSimpleTopic(int topicId) {

        try {

            List<SimplePostView> l = new ArrayList<SimplePostView>();
            String topicTitle;
            String author;
            String text;
            String postedAt;
            int postNumber = 0;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT title FROM Topic WHERE id = ?;"
            );
            s.setInt(1,topicId);
            ResultSet r = s.executeQuery();

            if (r.next()) topicTitle = r.getString("title");
            else return Result.failure("getSimpleTopic: topic with this id does not exist!");

            s = this.c.prepareStatement(
                "SELECT Post.message, Post.timePosted, Person.name FROM Post JOIN Person ON Person.id = Post.personId WHERE Post.topicId = ? ORDER BY Post.id ASC;"
            );

            s.setInt(1,topicId);
            r = s.executeQuery();

            while (r.next()) {
                author = r.getString("Person.name");
                text = r.getString("Post.message");
                postNumber += 1;
                postedAt = r.getString("Post.timePosted");

                l.add(new SimplePostView(postNumber,author,text,postedAt));
            }

            s.close();

            return Result.success(new SimpleTopicView(topicId,topicTitle,l));

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<PostView> getLatestPost(int topicId) {

        try {

            int forumId;
            int postNumber;
            int likes;
            String topicTitle;
            String author;
            String username;
            String text;
            String postedAt;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT Post.message, Post.id, Person.username, Person.name, Post.timePosted, Topic.forumId, Topic.title, b.count FROM " +
                "(" +
                "SELECT MAX(Post.timePosted) AS mptp FROM Post WHERE Post.topicId = ? " +
                ") a " +
                "JOIN Post ON Post.id = a.mptp " +
                "JOIN Person ON Person.id = Post.personId " +
                "JOIN Topic ON Topic.id = Post.topicId " +
                "JOIN (SELECT COUNT(*) AS count, postId FROM PostLikes GROUP BY postId) b ON b.postId = Post.id;"
            );
            s.setInt(1,topicId);
            ResultSet r = s.executeQuery();

            if (r.next()) {
                topicTitle = r.getString("Topic.title");
                forumId = r.getInt("Topic.forumId");
                text = r.getString("Post.message");
                author = r.getString("Person.name");
                username = r.getString("Person.username");
                postedAt = r.getString("Post.timePosted");
                likes = r.getInt("b.count");
            }
            else return Result.failure("getLatestPost: topic with this id does not exist!");

            s.close();

            s = this.c.prepareStatement(
                "SELECT COUNT(*) AS c FROM Post WHERE Post.topicId = ?;"
            );
            s.setInt(1,topicId);
            r = s.executeQuery();

            if (r.next()) postNumber = r.getInt("c");
            else return Result.failure("getLatestPost: error with post number");

            return Result.success(new PostView(forumId, topicId, postNumber, author, username, text, postedAt, likes));

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result createPost(int topicId, String username, String text) {

        try {

            int personId;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT * FROM Topic WHERE id = ?;"
            );
            s.setInt(1,topicId);
            ResultSet r = s.executeQuery();



            if (!r.next()) return Result.failure("createPost: no topic with this exists!");
            s.close();

            s = this.c.prepareStatement(
                "SELECT Person.id FROM Person WHERE Person.username = ?;"
            );
            s.setString(1,username);
            r = s.executeQuery();

            if (r.next()) personId = Integer.parseInt(r.getString("Person.id"));
            else return Result.failure("createPost: no person with this username exists!");

            s.close();

            s = this.c.prepareStatement(
                "INSERT INTO Post (message,topicId,personId,timePosted) VALUES (?,?,?,now());"
            );
            s.setString(1,text);
            s.setInt(2,topicId);
            s.setInt(3,personId);
            s.executeQuery();

            s.close();
            c.commit();

            return Result.success();

        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result createTopic(int forumId, String username, String title, String text) {

        if (title == "" || title == null) {
            return Result.failure("createTopic: title cannot be empty!");
        }
        if (text == "" || text == null) {
            return Result.failure("createTopic: text cannot be empty!");
        }

        try {

            int personId;
            int topicId;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT Person.id FROM Person WHERE Person.username = ?;"
            );
            s.setString(1,username);
            ResultSet r = s.executeQuery();

            if (r.next()) personId = r.getInt("Person.id");
            else return Result.failure("createTopic: no user with this username exists!");

            s.close();

            s = this.c.prepareStatement(
                "INSERT INTO Topic (title,message,forumId,personId) VALUES (?,?,?,?);",
                Statement.RETURN_GENERATED_KEYS
            );
            s.setString(1,title);
            s.setString(2,text);
            s.setInt(3,forumId);
            s.setInt(4,personId);

            s.executeQuery();
            r = s.getGeneratedKeys();

            if (r.next()) topicId = r.getInt(1);
            else return Result.failure("createTopic: failed");

            s = this.c.prepareStatement(
                "INSERT INTO Post (message,topicId,personId,timePosted) VALUES (?,?,?,now());"
            );
            s.setString(1,text);
            s.setInt(2,topicId);
            s.setInt(3,personId);
            s.executeQuery();

            s.close();

            c.commit();

            return Result.success();

        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<Integer> countPostsInTopic(int topicId)  {

        try {

            int count = 0;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT COUNT(*) AS count FROM Post WHERE Post.topicId = ?;"
            );

            s.setInt(1, topicId);
            ResultSet r = s.executeQuery();

            if (r.next()) count = r.getInt("count");
            else return Result.failure("countPostsInTopic: topic with this id does not exist!");

            s.close();

            return Result.success(count);

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    /* B.1 */

    @Override
    public Result likeTopic(String username, int topicId, boolean like) {

        if (username == "" || username == null) return Result.failure("likeTopic: username cannot be empty!");

        try {

            int personId;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT id FROM Person WHERE username = ?;"
            );
            s.setString(1,username);
            ResultSet r = s.executeQuery();

            if (r.next()) personId = r.getInt("id");
            else return Result.failure("likeTopic: no person with this username exists!");

            s.close();

            s = this.c.prepareStatement(
                "SELECT id FROM Topic WHERE id = ?"
            );
            s.setInt(1,topicId);
            r = s.executeQuery();

            if (!r.next()) return Result.failure("likeTopic: topic id does not exist!");

            s.close();

            s = this.c.prepareStatement(
                "SELECT * FROM TopicLikes WHERE topicId = ? AND personId = ?;"
            );
            s.setInt(1,topicId);
            s.setInt(2,personId);
            r = s.executeQuery();

            if (r.next()) {
                s.close();
                if (like) return Result.failure("likeTopic: topic has already been liked!");
                else {
                    s = this.c.prepareStatement(
                        "DELETE FROM TopicLikes WHERE TopicId = ? AND personId = ?;"
                    );
                    s.setInt(1,topicId);
                    s.setInt(2,personId);

                    s.executeQuery();
                }
            }
            else {
                s.close();
                if (like) {
                    s = this.c.prepareStatement(
                        "INSERT INTO TopicLikes (topicId,personId) VALUES (?,?);"
                    );

                    s.setInt(1,topicId);
                    s.setInt(2,personId);

                    s.executeQuery();
                }
                else return Result.failure("likeTopic: cannot unlike topic that has not been liked!");
            }

            s.close();
            c.commit();

            return Result.success();

        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result likePost(String username, int topicId, int post, boolean like) {

        if (username == "" || username == null) return Result.failure("likePost: username cannot be empty!");

        try {

            int personId;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT id FROM Person WHERE username = ?;"
            );

            s.setString(1,username);
            ResultSet r = s.executeQuery();

            if (r.next()) personId = r.getInt("id");
            else return Result.failure("likePost: no person with this username exists!");

            s.close();

            s = this.c.prepareStatement(
                "SELECT * FROM Post " +
                "JOIN Topic ON Post.topicId = Topic.id " +
                "WHERE Post.id = ?;"
            );

            s.setInt(1,post);
            r = s.executeQuery();

            if (!r.next()) return Result.failure("likePost: topic id or post id do not exist!");

            s.close();

            s = this.c.prepareStatement(
                "SELECT * FROM PostLikes WHERE postId = ? AND personId = ?;"
            );

            s.setInt(1,post);
            s.setInt(2,personId);

            r = s.executeQuery();

            if (r.next()) {
                s.close();
                if (like) return Result.failure("likePost: post has already been liked!");
                else {
                    s = this.c.prepareStatement(
                        "DELETE FROM PostLikes WHERE postId = ? AND personId = ?;"
                    );
                    s.setInt(1,post);
                    s.setInt(2,personId);

                    s.executeQuery();
                }
            }
            else {
                s.close();
                if (like) {
                    s = this.c.prepareStatement(
                        "INSERT INTO PostLikes (postId,personId) VALUES (?,?);"
                    );
                    s.setInt(1,post);
                    s.setInt(2,personId);

                    s.executeQuery();
                }
                else return Result.failure("likePost: cannot unlike post that has not been liked!");
            }

            s.close();
            c.commit();

            return Result.success();

        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<List<PersonView>> getLikers(int topicId) {

        try {

            List<PersonView> likers = new ArrayList<PersonView>();
            String name;
            String username;
            String studentId;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT id FROM topic WHERE id = ?"
            );

            ResultSet r = s.executeQuery();
            if (!r.next()) return Result.failure("getLikers: topic with this id does not exist!");
            s.close();

            s = this.c.prepareStatement(
                "SELECT TopicLikes.personId, Person.name, Person.username, Person.stuId FROM TopicLikes " +
                "JOIN Person ON TopicLikes.personId = Person.id " +
                "WHERE topicId = ? " +
                "ORDER BY personId;"
            );

            s.setInt(1,topicId);

            r = s.executeQuery();

            while (r.next()) {
                name = r.getString("Person.name");
                username = r.getString("Person.username");
                studentId = r.getString("Person.stuId");

                likers.add(new PersonView(name, username, studentId));
            }

            s.close();

            return Result.success(likers);

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<TopicView> getTopic(int topicId) {

        try {

            List<PostView> posts = new ArrayList<PostView>();
            int forumId;
            String forumName;
            String title;
            int postNumber = 0;
            String authorName;
            String authorUserName;
            String text;
            String postedAt;
            int likes;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT Forum.Id, Forum.Name, Topic.title FROM Topic " +
                "JOIN Forum ON Forum.id = Topic.forumId " +
                "WHERE Topic.id = ?;"
            );

            s.setInt(1,topicId);

            ResultSet r = s.executeQuery();

            if (r.next()) {
                forumName = r.getString("Forum.Name");
                forumId = r.getInt("Forum.Id");
                title = r.getString("Topic.title");
            }
            else return Result.failure("geTopic: topic with this id does not exist!");

            s.close();

            s = this.c.prepareStatement(
                "SELECT Post.id, Person.name, Person.username," +
                "       Post.message, Post.timePosted, a.likes FROM Post " +
                "JOIN Person ON Person.id = Post.personId " +
                "LEFT OUTER JOIN ( " +
                "    SELECT postId, COUNT(*) AS likes FROM PostLikes GROUP BY postId " +
                ") a ON a.postId = Post.id " +
                "WHERE Post.topicId = ? " +
                "ORDER BY Post.timePosted ASC;"
            );

            s.setInt(1,topicId);
            r = s.executeQuery();

            while (r.next()) {
                ++postNumber;
                authorName = r.getString("Person.name");
                authorUserName = r.getString("Person.username");
                text = r.getString("Post.message");
                postedAt = r.getString("Post.timePosted");
                likes = r.getInt("a.likes");
                posts.add(new PostView(forumId,topicId,postNumber,authorName,authorUserName,text,postedAt,likes));
            }

            s.close();

            return Result.success(new TopicView(forumId,topicId,forumName,title,posts));

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    /* B.2 */

    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<AdvancedPersonView> getAdvancedPersonView(String username) {

        if (username == null || username == "") return Result.failure("getPersonView: username cannot be empty!");

        try {

            List<TopicSummaryView> likedTopics = new ArrayList<TopicSummaryView>();
            int personId;
            String name;
            String studentId;
            int topicLikes;
            int postLikes;

            int topicId;
            int forumId;
            String title;
            String dateCreated;
            String lastPostDate;
            int postCount;
            String lastPostName;
            String topicCreatorName;
            String topicCreatorUsername;
            int likes;

            PreparedStatement s = this.c.prepareStatement(
                "SELECT Person.id, Person.name, Person.stuId, a.count, b.count FROM Person " +
                "LEFT OUTER JOIN ( " +
                "    SELECT COUNT(*) AS count, Post.personId AS ppid FROM PostLikes JOIN Post ON Post.id = PostLikes.postId " +
                "    GROUP BY Post.personId " +
                ") a ON a.ppid = Person.id " +
                "LEFT OUTER JOIN ( " +
                "    SELECT COUNT(*) AS count, Topic.personId AS tpid FROM TopicLikes JOIN Topic ON Topic.id = TopicLikes.topicId " +
                "    GROUP BY Topic.personId " +
                ") b ON b.tpid = Person.id " +
                "WHERE username = ?;"
            );
            s.setString(1,username);
            ResultSet r = s.executeQuery();

            if (r.next()) {
                personId = r.getInt("Person.id");
                name = r.getString("Person.name");
                studentId = r.getString("Person.stuId");
                postLikes = r.getInt("a.count");
                topicLikes = r.getInt("b.count");
            }
            else return Result.failure("getAdvancedPersonView: no person with this username exists!");

            s.close();

            s = this.c.prepareStatement(
                "SELECT TopicLikes.topicId, Forum.id, Topic.title,a.mtp, b.mtp, c.count, d.count, a.pn, a.pu, b.pn FROM TopicLikes " +
                "JOIN Topic ON Topic.id = TopicLikes.topicId " +
                "JOIN Forum ON Forum.id = Topic.forumId " +
                "JOIN ( " +
                "    SELECT MIN(Post.timePosted) AS mtp, Post.topicId AS ptid, Person.name AS pn, Person.username AS pu FROM Post " +
                "    JOIN Person ON Person.id = Post.personId " +
                "    GROUP BY Post.topicId " +
                ") a ON a.ptid = TopicLikes.topicId " +
                "JOIN ( " +
                "    SELECT MAX(Post.timePosted) AS mtp, Post.topicId AS ptid, Person.name AS pn FROM Post " +
                "    JOIN Person ON Person.id = Post.personId " +
                "    GROUP BY Post.topicId " +
                ") b ON b.ptid = TopicLikes.topicId " +
                "JOIN ( " +
                "    SELECT COUNT(*) AS count, TopicLikes.topicId AS tltid FROM TopicLikes " +
                "    GROUP BY TopicLikes.topicId " +
                ") c ON c.tltid = TopicLikes.topicId " +
                "JOIN ( " +
                "    SELECT COUNT(*) AS count, Post.topicId AS ptid FROM Post " +
                "    GROUP BY Post.topicId " +
                ") d ON d.ptid = TopicLikes.topicId " +
                "WHERE TopicLikes.personId = ? " +
                "ORDER BY Topic.title ASC;"
            );
            s.setInt(1,personId);
            r = s.executeQuery();

            while (r.next()) {
                topicId = r.getInt("TopicLikes.topicId");
                forumId = r.getInt("Forum.id");
                title = r.getString("Topic.title");
                dateCreated = r.getString("a.mtp");
                lastPostDate = r.getString("b.mtp");
                postCount = r.getInt("d.count");
                lastPostName = r.getString("b.pn");
                topicCreatorName = r.getString("a.pn");
                topicCreatorUsername = r.getString("a.pu");
                likes = r.getInt("c.count");
                likedTopics.add(new TopicSummaryView(
                    topicId,forumId,title,postCount,
                    dateCreated,lastPostDate,lastPostName,
                    likes,topicCreatorName,topicCreatorUsername
                ));
            }

            s.close();

            if (studentId == null) studentId = "";

            return Result.success(new AdvancedPersonView(name,username,studentId,topicLikes,postLikes,likedTopics));

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<AdvancedForumView> getAdvancedForum(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
