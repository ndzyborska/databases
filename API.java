package uk.ac.bris.cs.databases.cwk2;

import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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
import uk.ac.bris.cs.databases.api.TopicView;

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
                map.put(name,username);
            }
            s.close();
        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
            }
        return Result.success(map);
    }

    @Override
    public Result<PersonView> getPersonView(String username) {

      PersonView person = null;
      String query = "SELECT * FROM Person WHERE username = ?;";

      try {
          PreparedStatement s = this.c.prepareStatement(
              query
          );

          s.setString(1, username);

          ResultSet r = s.executeQuery();

          while (r.next()) {

            String name = r.getString("name");
            String user = r.getString("username");
            String studentId = r.getString("stuId");
            person = new PersonView(name, username, studentId);
          }

          s.close();

      } catch (SQLException e) {
          return Result.fatal(e.getMessage());
      }

      return Result.success(person);    }

    @Override
    public Result addNewPerson(String name, String username, String studentId) {

        try {

            String query = "INSERT INTO Person (name, username, stuId) VALUES (?, ?, ?);";
            PreparedStatement s = this.c.prepareStatement(
            query
            );

            s.setString(1, name);
            s.setString(2, username);
            s.setString(3, studentId);
            s.close();

            } catch (SQLException e) {
                return Result.fatal(e.getMessage());
            }

            return Result.success();
        }

    /* A.2 */

    @Override
    public Result<List<SimpleForumSummaryView>> getSimpleForums() {

      String query = "SELECT * FROM Forum;";
      List<SimpleForumSummaryView> list = new ArrayList<SimpleForumSummaryView>();

      try {
          PreparedStatement s = this.c.prepareStatement(
              query
          );

          ResultSet r = s.executeQuery();

          while (r.next()) {
            int id = r.getInt("id");
            String name = r.getString("name");
            SimpleForumSummaryView forum = new SimpleForumSummaryView(id, name);
            list.add(forum);
          }

          s.close();

      } catch (SQLException e) {
          return Result.fatal(e.getMessage());
      }

      return Result.success(list);

    }

    @Override
    public Result createForum(String title) {
        throw new UnsupportedOperationException("Test edit");
    }

    /* A.3 */

    @Override
    public Result<List<ForumSummaryView>> getForums() {
        throw new UnsupportedOperationException("Test edit");
    }

    @Override
    public Result<ForumView> getForum(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
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

        //TODO: implement check for if topic exists

        try {
            PreparedStatement s = this.c.prepareStatement(
                "SELECT Post.message, Post.id, Person.username, Person.name, Post.timePosted, Topic.forumId, Topic.title FROM " +
                "(" +
                "SELECT MAX(Post.timePosted) AS mptp FROM Post WHERE Post.topicId = ? " +
                ") a " +
                "JOIN Post ON Post.id = a.mptp " +
                "JOIN Person ON Person.id = Post.personId " +
                "JOIN Topic ON Topic.id = Post.topicId;"
            );
            s.setInt(1,topicId);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                topicTitle = r.getString("Topic.title");
                forumId = Integer.parseInt(r.getString("Topic.forumId"));
                text = r.getString("Post.message");
                author = r.getString("Person.name");
                username = r.getString("Person.username");
                postedAt = r.getString("Post.timePosted");
                //TODO;
                likes = -1;
            }
            else {
                return Result.failure("getLatestPost: error");
            }

            PreparedStatement t = this.c.prepareStatement(
                "SELECT COUNT(*) AS c FROM Post WHERE Post.topicId = ?;"
            );
            t.setInt(1,topicId);
            ResultSet q = t.executeQuery();
            if (q.next()) {
                postNumber = Integer.parseInt(q.getString("c"));
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

        //TODO: implement check for if topic exists
        try {
            PreparedStatement t = this.c.prepareStatement(
                "SELECT Person.id FROM Person WHERE Person.username = ?;"
            );
            t.setString(1,username);
            ResultSet q = t.executeQuery();
            if (q.next()) {
                personId = Integer.parseInt(q.getString("Person.id"));
            }
            else {
                return Result.failure("createPost: no person with this username exists!");
            }
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
                personId = Integer.parseInt(r.getString("Person.id"));
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

            s.close();
            r.close();

            this.createPost(topicId,username,text);
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
    public Result<Integer> countPostsInTopic(int topicId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* B.1 */

    @Override
    public Result likeTopic(String username, int topicId, boolean like) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result likePost(String username, int topicId, int post, boolean like) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<List<PersonView>> getLikers(int topicId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<TopicView> getTopic(int topicId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* B.2 */

    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<AdvancedPersonView> getAdvancedPersonView(String username) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<AdvancedForumView> getAdvancedForum(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
