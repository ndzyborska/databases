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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<PostView> getLatestPost(int topicId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result createPost(int topicId, String username, String text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result createTopic(int forumId, String username, String title, String text) {
        throw new UnsupportedOperationException("Not supported yet.");
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
