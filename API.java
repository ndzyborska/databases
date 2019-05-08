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
        Map<String,String> map = new HashMap<String,String>();
        String name;
        String username;

        try {
            PreparedStatement s = this.c.prepareStatement(
                "SELECT name, username FROM Person;"
            );
            ResultSet r = s.executeQuery();
            while (r.next()) {
                name = r.getString("name");
                username = r.getString("username");

                map.put(username,name);
            }

            r.close();
            s.close();

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }

        return Result.success(map);
    }

    @Override
    public Result<PersonView> getPersonView(String username) {

        if (username == "" || username == null) return Result.failure("getPersonView: username cannot be empty!");

        String name;
        String user;
        String studentId;

        try {
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

            r.close();
            s.close();

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }

        if (studentId == null) studentId = "";

        return Result.success(new PersonView(name, username, studentId));
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

            } catch (SQLException e) {
                try {
                    c.rollback();
                } catch (SQLException f) {
                    return Result.fatal(f.getMessage());
                }
                return Result.fatal(e.getMessage());
            }

            return Result.success();
        }

    /* A.2 */

    @Override
    public Result<List<SimpleForumSummaryView>> getSimpleForums() {

        int id;
        String name;
        List<SimpleForumSummaryView> list = new ArrayList<SimpleForumSummaryView>();

        try {
            PreparedStatement s = this.c.prepareStatement(
                "SELECT id, name FROM Forum;"
            );

            ResultSet r = s.executeQuery();

            while (r.next()) {
                id = r.getInt("id");
                name = r.getString("name");
                list.add(new SimpleForumSummaryView(id, name));
            }

            r.close();
            s.close();

      } catch (SQLException e) {
          return Result.fatal(e.getMessage());
      }

      return Result.success(list);

    }

    @Override
    public Result createForum(String title) {

        if (title == "" || title == null) return Result.failure("createForum: title cannot be empty!");

        try {

            PreparedStatement t = this.c.prepareStatement(
                "SELECT * FROM Forum WHERE Forum.name = ?;"
            );
            t.setString(1, title);
            ResultSet q = t.executeQuery();

            if (q.next()) return Result.failure("createForum: forum with this title already exists!");
            q.close();
            t.close();

            PreparedStatement s = this.c.prepareStatement(
                "INSERT INTO Forum (name) VALUES (?);"
            );

            s.setString(1, title);
            s.executeQuery();
            s.close();
            c.commit();

        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }

        return Result.success();
    }

    /* A.3 */

    @Override
    public Result<List<ForumSummaryView>> getForums() {

      SimpleTopicSummaryView topic;
      String temp;
      int topicId;
      List<ForumSummaryView> list = new ArrayList<ForumSummaryView>();

      try {
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
            int id = r.getInt("Forum.id");
            String name = r.getString("Forum.name");
            String topicName = r.getString("Topic.title");
            temp = r.getString("Topic.id");

            if (temp == null) {
                list.add(new ForumSummaryView(id,name,null));
            }
            else {
                topicId = Integer.parseInt(temp);
                topic = new SimpleTopicSummaryView(topicId, id, topicName);
                list.add(new ForumSummaryView(id, name, topic));
            }

          }

          s.close();
          r.close();

      } catch (SQLException e) {
        System.out.print("error caught");
          return Result.fatal(e.getMessage());

      }

      return Result.success(list);    }

    @Override
    public Result<ForumView> getForum(int id) {
        String forumTitle;
        String topicTitle;
        int topicId;
        List<SimpleTopicSummaryView> topicList = new ArrayList<SimpleTopicSummaryView>();

        try {
            PreparedStatement t = this.c.prepareStatement(
                "SELECT name FROM Forum WHERE id = ?;"
            );
            t.setInt(1,id);
            ResultSet q = t.executeQuery();
            if (q.next()) {
                forumTitle = q.getString("name");
            }
            else {
                return Result.failure("getForum: Issue with id!");
            }
            t.close();
            q.close();

            PreparedStatement s = this.c.prepareStatement(
                "SELECT id, title FROM Topic WHERE forumId = ?;"
            );
            s.setInt(1,id);
            ResultSet r = s.executeQuery();

            while (r.next()) {
                topicTitle = r.getString("title");
                topicId = Integer.parseInt(r.getString("id"));
                topicList.add(new SimpleTopicSummaryView(topicId,id,topicTitle));
            }
            s.close();
            r.close();

        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }

        return Result.success(new ForumView(id,forumTitle,topicList));
    }

    @Override
    public Result<SimpleTopicView> getSimpleTopic(int topicId) {
        List<SimplePostView> l = new ArrayList<SimplePostView>();
        String topicTitle;
        String author;
        String text;
        String postedAt;
        int postNumber = 0;

        try {
            PreparedStatement t = this.c.prepareStatement(
                "SELECT title FROM Topic WHERE id = ?;"
            );
            t.setInt(1,topicId);
            ResultSet q = t.executeQuery();
            if (q.next()) {
                topicTitle = q.getString("title");
            }
            else {
                return Result.failure("getSimpleTopic: topic with this id does not exist!");
            }

            PreparedStatement s = this.c.prepareStatement(
                "SELECT Post.message, Post.timePosted, Person.name FROM Post JOIN Person ON Person.id = Post.personId WHERE Post.topicId = ? ORDER BY Post.id ASC;"
            );
            s.setInt(1,topicId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                author = r.getString("Person.name");
                text = r.getString("Post.message");
                postNumber += 1;
                postedAt = r.getString("Post.timePosted");
                l.add(new SimplePostView(postNumber,author,text,postedAt));
            }


        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }

        return Result.success(new SimpleTopicView(topicId,topicTitle,l));
    }

    @Override
    public Result<PostView> getLatestPost(int topicId) {
        int forumId;
        int postNumber;
        int likes;
        String topicTitle;
        String author;
        String username;
        String text;
        String postedAt;

        try {
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

            PreparedStatement t = this.c.prepareStatement(
                "SELECT COUNT(*) AS c FROM Post WHERE Post.topicId = ?;"
            );
            t.setInt(1,topicId);
            ResultSet q = t.executeQuery();
            if (q.next()) {
                postNumber = q.getInt("c");
            }
            else {
                return Result.failure("getLatestPost: error with post number");
            }


        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }

        return Result.success(new PostView(forumId, topicId, postNumber, author, username, text, postedAt, likes));
    }

    @Override
    public Result createPost(int topicId, String username, String text) {

        int personId;

        try {
            PreparedStatement v = this.c.prepareStatement(
                "SELECT * FROM Topic WHERE id = ?;"
            );
            v.setInt(1,topicId);
            ResultSet p = v.executeQuery();



            if (!p.next()) return Result.failure("createPost: no topic with this exists!");

            PreparedStatement t = this.c.prepareStatement(
                "SELECT Person.id FROM Person WHERE Person.username = ?;"
            );
            t.setString(1,username);
            ResultSet q = t.executeQuery();

            if (q.next()) personId = Integer.parseInt(q.getString("Person.id"));
            else return Result.failure("createPost: no person with this username exists!");

            q.close();
            t.close();

            PreparedStatement s = this.c.prepareStatement(
                "INSERT INTO Post (message,topicId,personId,timePosted) VALUES (?,?,?,now());"
            );
            s.setString(1,text);
            s.setInt(2,topicId);
            s.setInt(3,personId);
            s.executeQuery();

            s.close();
            c.commit();

        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }

        return Result.success();
    }

    @Override
    public Result createTopic(int forumId, String username, String title, String text) {
        int personId;
        int topicId;
        if (title == "" || title == null) {
            return Result.failure("createTopic: title cannot be empty!");
        }
        if (text == "" || text == null) {
            return Result.failure("createTopic: text cannot be empty!");
        }
        try {

            PreparedStatement s = this.c.prepareStatement(
                "SELECT Person.id FROM Person WHERE Person.username = ?;"
            );
            s.setString(1,username);
            ResultSet r = s.executeQuery();

            if (r.next()) {
                personId = r.getInt("Person.id");
            }
            else {
                return Result.failure("createTopic: no user with this username exists!");
            }

            s.close();
            r.close();

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
            if (r.next()) {
                topicId = r.getInt(1);
            }
            else {
                return Result.failure("createTopic: failed");
            }
            s = this.c.prepareStatement(
                "INSERT INTO Post (message,topicId,personId,timePosted) VALUES (?,?,?,now());"
            );
            s.setString(1,text);
            s.setInt(2,topicId);
            s.setInt(3,personId);

            s.executeQuery();

            s.close();
            r.close();

            c.commit();

        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }

        return Result.success();
    }

    @Override
    public Result<Integer> countPostsInTopic(int topicId)  {

            int count = 0;

            try {
                PreparedStatement s = this.c.prepareStatement(
                    "SELECT COUNT(Post.id) AS c FROM Post WHERE Post.topicId = ?;"
                );

                s.setInt(1, topicId);
                ResultSet r = s.executeQuery();

                if (r.next()) {
                  count = r.getInt("count");
                }

                r.close();
                s.close();

            } catch (SQLException e) {
                return Result.fatal(e.getMessage());
            }

            return Result.success(count);
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

          if (r.next())  personId = r.getInt("id");
          else return Result.failure("likeTopic: no person with this username exists!");

          r.close();
          s.close();

          s = this.c.prepareStatement(
              "SELECT id FROM Topic WHERE id = ?"
          );

          s.setInt(1,topicId);
          r = s.executeQuery();

          if (!r.next()) return Result.failure("likeTopic: topic id does not exist!");

          r.close();
          s.close();

          s = this.c.prepareStatement(
              "SELECT * FROM TopicLikes WHERE topicId = ? AND personId = ?;"
          );

          s.setInt(1,topicId);
          s.setInt(2,personId);

          r = s.executeQuery();
          s.close();

          if (r.next()) {
              if (like) return Result.failure("likeTopic: topic has already been liked!");
              else {
                  s = this.c.prepareStatement(
                      "DELETE FROM TopicLikes WHERE TopicId = ? AND personId = ?;"
                  );

                  s.setInt(1,topicId);
                  s.setInt(2,personId);

                  s.executeQuery();
                  s.close();
              }
          }
          else {
              if (like) {
                  s = this.c.prepareStatement(
                      "INSERT INTO TopicLikes (topicId,personId) VALUES (?,?);"
                  );

                  s.setInt(1,topicId);
                  s.setInt(2,personId);

                  s.executeQuery();
                  s.close();
              }
              else return Result.failure("likeTopic: cannot unlike topic that has not been liked!");
          }

          c.commit();


      } catch (SQLException e) {
          try {
              c.rollback();
          } catch (SQLException f) {
              return Result.fatal(f.getMessage());
          }
          return Result.fatal(e.getMessage());
      }

      return Result.success();

    }

    @Override
    public Result likePost(String username, int topicId, int post, boolean like) {

        if (username == "" || username == null) return Result.failure("likePost: username cannot be empty!");

        int personId;

        try {

            PreparedStatement s = this.c.prepareStatement(
                "SELECT id FROM Person WHERE username = ?;"
            );

            s.setString(1,username);
            ResultSet r = s.executeQuery();

            if (r.next()) personId = r.getInt("id");
            else return Result.failure("likePost: no person with this username exists!");

            r.close();
            s.close();

            s = this.c.prepareStatement(
                "SELECT * FROM Post " +
                "JOIN Topic ON Post.topicId = Topic.id " +
                "WHERE Post.id = ?;"
            );

            s.setInt(1,post);
            r = s.executeQuery();

            if (!r.next()) return Result.failure("likePost: topic id or post id do not exist!");

            r.close();
            s.close();

            s = this.c.prepareStatement(
                "SELECT * FROM PostLikes WHERE postId = ? AND personId = ?;"
            );

            s.setInt(1,post);
            s.setInt(2,personId);

            r = s.executeQuery();
            s.close();

            if (r.next()) {
                if (like) return Result.failure("likePost: post has already been liked!");
                else {
                    s = this.c.prepareStatement(
                        "DELETE FROM PostLikes WHERE postId = ? AND personId = ?;"
                    );

                    s.setInt(1,post);
                    s.setInt(2,personId);

                    s.executeQuery();
                    s.close();
                }
            }
            else {
                if (like) {
                    s = this.c.prepareStatement(
                        "INSERT INTO PostLikes (postId,personId) VALUES (?,?);"
                    );

                    s.setInt(1,post);
                    s.setInt(2,personId);

                    s.executeQuery();
                    s.close();
                }
                else return Result.failure("likePost: cannot unlike post that has not been liked!");
            }

            c.commit();


        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal(f.getMessage());
            }
            return Result.fatal(e.getMessage());
        }

        return Result.success();
    }

    @Override
    public Result<List<PersonView>> getLikers(int topicId) {

      List<PersonView> likers = new ArrayList<PersonView>();
      String name;
      String username;
      String studentId;


      try {

         PreparedStatement s = this.c.prepareStatement(
             "SELECT id FROM topic WHERE id = ?"
          );

          ResultSet r = s.executeQuery();
          if (!r.next()) {
            return Result.failure("getLikers: topic with this id does not exist!");
          }
          s.close();
          r.close();


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

          r.close();
          s.close();


      } catch (SQLException e) {
          return Result.fatal(e.getMessage());
      }

      return Result.success(likers);

    }

    @Override
    public Result<TopicView> getTopic(int topicId) {
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

        try {

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
            r.close();

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

            r.close();
            s.close();


        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }

        return Result.success(new TopicView(forumId,topicId,forumName,title,posts));
    }

    /* B.2 */

    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<AdvancedPersonView> getAdvancedPersonView(String username) {

        if (username == null || username == "") {
            return Result.failure("getPersonView: username cannot be empty!");
        }

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

        try {
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
            r.close();

            s = this.c.prepareStatement(
                "SELECT "
            );

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

            r.close();
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
