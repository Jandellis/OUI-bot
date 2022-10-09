package action.reminder;

import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import action.reminder.model.Stats;
import action.reminder.model.Status;
import action.reminder.model.Team;
import bot.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReminderUtils {

    protected static final Logger logger = LogManager.getLogger("ouiBot");

    static Config config = Config.getInstance();
    static String url = config.get("url");
    static String user = config.get("user");
    static String password = config.get("password");


    public static Reminder addReminder(String name, ReminderType type, Timestamp time, String channel) {
        Reminder reminder = new Reminder(name, type, time, channel);
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM reminder  WHERE name = '" + name + "' AND type='" + type.getName() + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
            }
            //maybe make this delete and then add reminder?
            //also do it in a transaction
            if (id != -1) {
                st.execute("DELETE from reminder where id = " + id);
                st.executeBatch();
            }

            PreparedStatement pst2 = con.prepareStatement("insert into reminder (name, type, reminder_time, channel) " +
                    "VALUES ('" + name + "', '" + type.getName() + "', '" + time + "', '" + channel + "') RETURNING id");
            ResultSet rs2 = pst2.executeQuery();
            while (rs2.next()) {
                reminder.setId(rs2.getInt(1));
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminder;
    }

    public static boolean lockReminder(Reminder reminder) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("UPDATE reminder SET locked = true WHERE id = ?");
            pst.setLong(1, reminder.getId());
            int rs = pst.executeUpdate();
            if (rs == 1) {
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return false;
    }

    public static boolean unlockReminder(Reminder reminder) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("UPDATE reminder SET locked = false WHERE id = ?");
            pst.setLong(1, reminder.getId());
            int rs = pst.executeUpdate();
            if (rs == 1) {
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return false;
    }

    public static Reminder addMultipleReminder(String name, ReminderType type, Timestamp time, String channel) {
        Reminder reminder = new Reminder(name, type, time, channel);
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            st.addBatch("insert into reminder (name, type, reminder_time, channel) " +
                    "VALUES ('" + name + "', '" + type.getName() + "', '" + time + "', '" + channel + "')");
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminder;
    }

    public static void deleteReminder(String name, ReminderType type) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from reminder WHERE name = '" + name + "' AND type='" + type.getName() + "'");
            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

    public static void deleteReminder(Reminder reminder) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();


            String sql = "DELETE from reminder WHERE name = ? and type = ? and reminder_time = ? ";
            PreparedStatement p = con.prepareStatement(sql);
            p.setString(1, reminder.getName());
            p.setString(2, reminder.getType().getName());
            p.setTimestamp(3, reminder.getTime());
            p.execute();

//            con.setAutoCommit(false);

//            st.addBatch("DELETE from reminder WHERE name = '" + reminder.getName() + "' AND type='" + reminder.getType().getName() + "' AND time='" + reminder.getTime() + "'");
//            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static List<Reminder> loadReminder(String id) {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel FROM reminder  WHERE name = '" + id + "' order by reminder_time");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminders;
    }


    public static List<Reminder> loadReminder(Reminder rem) {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel FROM reminder  WHERE name = '" + rem.getName() + "' and type = '" + rem.getType().getName() + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminders;
    }

    public static List<Reminder> loadReminder(long id) {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel FROM reminder  WHERE id = " + id);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminders;
    }

    public static List<Reminder> loadReminder() {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel, id FROM reminder ");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminder.setId(rs.getLong(5));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminders;
    }


    public static List<Reminder> loadReminderWindow() {
        List<Reminder> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().plusSeconds(70);
        Timestamp timestamp = Timestamp.valueOf(now);
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel, id FROM reminder where reminder_time < ? and (locked = false or locked is null)");
            pst.setTimestamp(1, timestamp);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminder.setId(rs.getLong(5));
                reminders.add(reminder);
            }


        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminders;
    }


    public static boolean addProfile(String name, String shack, Status status) {
        Boolean newProfile = false;
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE profile SET shack_name = ?, status = ? WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, shack);
                p.setString(2, status.name());
                p.setInt(3, id);
                p.executeUpdate();
//                st.executeUpdate("UPDATE profile SET shack_name = '" + shack + "' WHERE id = " + id);
            }
            if (id == -1) {
                String sql = "insert into profile (name, shack_name, status, enabled) " +
                        "VALUES (?, ?, ?, false)";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, name);
                p.setString(2, shack);
                p.setString(3, status.name());
                p.execute();
                newProfile = true;
//                st.addBatch("insert into profile (name, shack_name, status, enabled) " +
//                        "VALUES ('" + name + "', '" + shack + "', '" + status.name() + "', false)");
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return newProfile;
    }

    public static boolean enableProfile(String name, boolean enable) {
        boolean updated = false;
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                st.executeUpdate("UPDATE profile SET enabled = " + enable + " WHERE id = " + id);
                updated = true;
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return updated;
    }


    public static boolean setDepth(String name, int depth) {
        boolean updated = false;
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                st.executeUpdate("UPDATE profile SET depth = " + depth + " WHERE id = " + id);
                updated = true;
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return updated;
    }

    public static boolean setUpgrade(String name, int depth) {
        boolean updated = false;
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                st.executeUpdate("UPDATE profile SET upgrade = " + depth + " WHERE id = " + id);
                updated = true;
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return updated;
    }

    public static void addReact(String name, String react) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                st.executeUpdate("UPDATE profile SET react = '" + react + "' WHERE id = " + id);
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

    public static void deleteReact(String name) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                st.executeUpdate("UPDATE profile SET react = null WHERE id = " + id);
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

    public static void addMessage(String name, String message) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = ?");
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);

                String sql = "UPDATE profile SET message = ? WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, message);
                p.setInt(2, id);
                p.executeUpdate();
//                st.executeUpdate("UPDATE profile SET message = '" + message + "' WHERE id = " + id);
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static Profile loadProfileByName(String shack) {
        Profile profile = null;
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status, enabled, react, message, depth, upgrade FROM profile  WHERE shack_name = ? and enabled = true");
            pst.setString(1, shack);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                profile = new Profile(rs.getString(1), rs.getString(2), Status.getStatus(rs.getString(3)), rs.getBoolean(4), rs.getString(5), rs.getString(6), rs.getInt(7), rs.getInt(8));

            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return profile;
    }

    public static void deleteProfile(String name) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from profile WHERE name = '" + name + "'");
            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static Profile loadProfileById(String id) {
        Profile profile = null;
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status, enabled, react, message, depth, upgrade FROM profile  WHERE name = '" + id + "' and enabled = true");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                profile = new Profile(rs.getString(1), rs.getString(2), Status.getStatus(rs.getString(3)), rs.getBoolean(4), rs.getString(5), rs.getString(6), rs.getInt(7), rs.getInt(8));
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return profile;
    }


    public static boolean updateStatsWork(String id) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement("UPDATE user_stats SET work = work + 1 WHERE name = ?");
            pst.setString(1, id);
            int rs = pst.executeUpdate();
            if (rs == 1) {
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return false;
    }

    public static boolean updateStatsTips(String id) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement("UPDATE user_stats SET tips = tips + 1 WHERE name = ?");
            pst.setString(1, id);
            int rs = pst.executeUpdate();
            if (rs == 1) {
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return false;
    }

    public static boolean updateStatsOvertime(String id) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement("UPDATE user_stats SET overtime = overtime + 1 WHERE name = ?");
            pst.setString(1, id);
            int rs = pst.executeUpdate();
            if (rs == 1) {
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return false;
    }

    public static void createStats(String id, int work, int tips, int ot) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            st.addBatch("insert into user_stats (name, work, tips, overtime) " +
                    "VALUES ('" + id + "', " + work + ", " + tips + ", " + ot + ")");
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

    public static List<Stats> loadStats() {
        List<Stats> statsList = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, user, password);


            PreparedStatement pst = con.prepareStatement("select name, work, tips, overtime from user_stats");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Stats stats = new Stats(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
//                    reminders.add(reminder);
                statsList.add(stats);
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }

        return statsList;
    }

    public static List<Stats> loadTeamStats() {
        List<Stats> statsList = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, user, password);


            PreparedStatement pst = con.prepareStatement("select t.team, sum(u.work), sum(u.tips), sum(u.overtime) " +
                    "from team_stats t, user_stats u " +
                    "where t.joined = true and t.name = u.name " +
                    "group by t.team");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Stats stats = new Stats(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
//                    reminders.add(reminder);
                statsList.add(stats);
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }

        return statsList;
    }
    public static void resetStats() {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            st.addBatch("update user_stats set work = 0, tips = 0, overtime = 0");
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static List<Team> loadTeam(String id) {
        List<Team> teams = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, user, password);


            PreparedStatement pst = con.prepareStatement("select name, team, owner, joined from team_stats where name = ?");
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Team team = new Team(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4));
                teams.add(team);
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }

        return teams;
    }


    public static List<Team> loadTeamMembers(String teamName) {
        List<Team> teams = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, user, password);




            PreparedStatement pst = con.prepareStatement("select name, team, owner, joined from team_stats where team = ?");
            pst.setString(1, teamName);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Team team = new Team(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4));
                teams.add(team);
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }

        return teams;
    }
    public static void createTeam(Team team) {
        List<Team> teams = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            String sql = "insert into team_stats (name, team, owner, joined) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement p = con.prepareStatement(sql);
            p.setString(1, team.getName());
            p.setString(2, team.getTeam());
            p.setBoolean(3, team.isOwner());
            p.setBoolean(4, team.isJoined());
            p.execute();

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static void deleteTeam(String name, String teamName) {
        List<Team> teams = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            String sql = "delete from  team_stats where name = ? and team = ?";
            PreparedStatement p = con.prepareStatement(sql);
            p.setString(1, name);
            p.setString(2, teamName);
            p.execute();

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

}
