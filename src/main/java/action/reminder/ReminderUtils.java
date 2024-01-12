package action.reminder;

import action.giveaway.model.GiveawayLog;
import action.giveaway.model.GiveawayWinner;
import action.reminder.model.ReminderSettings;
import action.reminder.model.FlexStats;
import action.reminder.model.Profile;
import action.reminder.model.ProfileStats;
import action.reminder.model.Reminder;
import action.reminder.model.Stats;
import action.reminder.model.Status;
import action.reminder.model.Team;
import action.upgrades.model.LocationEnum;
import bot.Config;
import database.DatabaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReminderUtils {

    protected static final Logger logger = LogManager.getLogger("ouiBot");

    static DatabaseUtils databaseUtils = DatabaseUtils.getInstance();


    public static Reminder addReminder(String name, ReminderType type, Timestamp time, String channel) {
        Reminder reminder = new Reminder(name, type, time, channel);
        try {
            Connection con = databaseUtils.getConnection();
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
                    "VALUES ('" + name + "', '" + type.getName() + "', '" + time + "', '" + channel + "')");
            pst2.execute();

            PreparedStatement pst3 = con.prepareStatement("SELECT id FROM reminder  WHERE name = '" + name + "' AND type='" + type.getName() + "'");
            ResultSet rs3 = pst3.executeQuery();
            while (rs3.next()) {
                reminder.setId(rs3.getInt(1));
            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminder;
    }

    public static boolean lockReminder(Reminder reminder) {
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("UPDATE reminder SET locked = true WHERE id = ?");
            pst.setLong(1, reminder.getId());
            int rs = pst.executeUpdate();
            if (rs == 1) {
                con.close();
                return true;
            }
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }

    public static boolean unlockReminder(Reminder reminder) {
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("UPDATE reminder SET locked = false WHERE id = ?");
            pst.setLong(1, reminder.getId());
            int rs = pst.executeUpdate();
            if (rs == 1) {
                con.close();
                return true;
            }
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }

    public static Reminder addMultipleReminder(String name, ReminderType type, Timestamp time, String channel) {
        Reminder reminder = new Reminder(name, type, time, channel);
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            st.addBatch("insert into reminder (name, type, reminder_time, channel) " +
                    "VALUES ('" + name + "', '" + type.getName() + "', '" + time + "', '" + channel + "')");
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminder;
    }

    public static void deleteReminder(String name, ReminderType type) {
        try {
            Connection con = databaseUtils.getConnection();

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from reminder WHERE name = '" + name + "' AND type='" + type.getName() + "'");
            st.executeBatch();
            con.close();
//            con.commit();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static void deleteReminder(Reminder reminder) {
        try {
            Connection con = databaseUtils.getConnection();

            Statement st = con.createStatement();


            String sql = "DELETE from reminder WHERE name = ? and type = ? and reminder_time = ? ";
            PreparedStatement p = con.prepareStatement(sql);
            p.setString(1, reminder.getName());
            p.setString(2, reminder.getType().getName());
            p.setTimestamp(3, reminder.getTime());
            p.execute();
            con.close();

//            con.setAutoCommit(false);

//            st.addBatch("DELETE from reminder WHERE name = '" + reminder.getName() + "' AND type='" + reminder.getType().getName() + "' AND time='" + reminder.getTime() + "'");
//            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static List<Reminder> loadReminder(String id) {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel FROM reminder  WHERE name = '" + id + "' order by reminder_time");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminders;
    }


    public static List<Reminder> loadReminder(Reminder rem) {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel FROM reminder  WHERE name = '" + rem.getName() + "' and type = '" + rem.getType().getName() + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminders;
    }

    public static List<Reminder> loadReminder(long id) {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel FROM reminder  WHERE id = " + id);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminders;
    }

    public static List<Reminder> loadReminder() {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel, id FROM reminder ");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminder.setId(rs.getLong(5));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminders;
    }


    public static List<Reminder> loadReminderWindow() {
        List<Reminder> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().plusSeconds(70);
        Timestamp timestamp = Timestamp.valueOf(now);
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel, id FROM reminder where reminder_time < ? and (locked = false or locked is null)");
            pst.setTimestamp(1, timestamp);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Reminder reminder = new Reminder(rs.getString(1), ReminderType.getReminderType(rs.getString(2)), rs.getTimestamp(3), rs.getString(4));
                reminder.setId(rs.getLong(5));
                reminders.add(reminder);
            }
            con.close();


        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminders;
    }


    public static boolean addProfile(String name, String shack, Status status, String username) {
        Boolean newProfile = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE profile SET shack_name = ?, status = ?, username = ? WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, shack);
                p.setString(2, status.name());
                p.setString(3, username);
                p.setInt(4, id);
                p.executeUpdate();
//                st.executeUpdate("UPDATE profile SET shack_name = '" + shack + "' WHERE id = " + id);
            }
            if (id == -1) {
                String sql = "insert into profile (name, shack_name, status, enabled, username) " +
                        "VALUES (?, ?, ?, false, ?)";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, name);
                p.setString(2, shack);
                p.setString(3, status.name());
                p.setString(4, username);
                p.execute();
                newProfile = true;
//                st.addBatch("insert into profile (name, shack_name, status, enabled) " +
//                        "VALUES ('" + name + "', '" + shack + "', '" + status.name() + "', false)");
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return newProfile;
    }

    public static boolean enableProfile(String name, boolean enable) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
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
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }


    public static boolean setDepth(String name, int depth) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
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
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }

    public static boolean setUpgrade(String name, int depth) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
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
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }

    public static void addReact(String name, String react) {
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                st.executeUpdate("UPDATE profile SET react = '" + react + "' WHERE id = " + id);
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static void deleteReact(String name) {
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                st.executeUpdate("UPDATE profile SET react = null WHERE id = " + id);
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static void addMessage(String name, String message) {
        try {
            Connection con = databaseUtils.getConnection();
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
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static Profile loadProfileByName(String shack) {
        Profile profile = null;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status, enabled, react, message, depth, upgrade, sleep_Start, sleep_End, dm_reminder, ignored_hidden, dnd, username " +
                    "FROM profile  WHERE shack_name = ?");
            pst.setString(1, shack);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                profile = new Profile(rs.getString(1),
                        rs.getString(2),
                        Status.getStatus(rs.getString(3)),
                        rs.getBoolean(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getInt(7),
                        rs.getInt(8),
                        rs.getTime(9),
                        rs.getTime(10),
                        rs.getBoolean(11),
                        rs.getBoolean(12),
                        rs.getBoolean(13),
                        rs.getString(14));

            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return profile;
    }

    public static void deleteProfile(String name) {
        try {
            Connection con = databaseUtils.getConnection();

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from profile WHERE name = '" + name + "'");
            st.executeBatch();
            con.close();
//            con.commit();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static Profile loadProfileById(String id) {
        Profile profile = null;
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status, enabled, react, message, depth, upgrade, sleep_Start, sleep_End, dm_reminder, ignored_hidden, dnd, username " +
                     "FROM profile  WHERE name = '" + id + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                profile = new Profile(rs.getString(1),
                        rs.getString(2),
                        Status.getStatus(rs.getString(3)),
                        rs.getBoolean(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getInt(7),
                        rs.getInt(8),
                        rs.getTime(9),
                        rs.getTime(10),
                        rs.getBoolean(11),
                        rs.getBoolean(12),
                        rs.getBoolean(13),
                        rs.getString(14));
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return profile;
    }


    public static Profile loadProfileByUserName(String username) {
        Profile profile = null;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status, enabled, react, message, depth, upgrade, sleep_Start, sleep_End, dm_reminder, ignored_hidden, dnd, username " +
                    "FROM profile  WHERE username = ?");
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                profile = new Profile(rs.getString(1),
                        rs.getString(2),
                        Status.getStatus(rs.getString(3)),
                        rs.getBoolean(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getInt(7),
                        rs.getInt(8),
                        rs.getTime(9),
                        rs.getTime(10),
                        rs.getBoolean(11),
                        rs.getBoolean(12),
                        rs.getBoolean(13),
                        rs.getString(14));

            }
            st.executeBatch();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return profile;
    }


    public static boolean updateStatsWork(String id) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE user_stats SET work = work + 1 WHERE name = ?");
            pst.setString(1, id);
            int rs = pst.executeUpdate();
            if (rs == 1) {
                con.close();
                return true;
            }
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }

    public static boolean updateStatsTips(String id) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE user_stats SET tips = tips + 1 WHERE name = ?");
            pst.setString(1, id);
            int rs = pst.executeUpdate();
            if (rs == 1) {
                con.close();
                return true;
            }
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }

    public static boolean updateStatsOvertime(String id) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE user_stats SET overtime = overtime + 1 WHERE name = ?");
            pst.setString(1, id);
            int rs = pst.executeUpdate();
            if (rs == 1) {
                con.close();
                return true;
            }
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }

    public static void createStats(String id, int work, int tips, int ot) {
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            st.addBatch("insert into user_stats (name, work, tips, overtime) " +
                    "VALUES ('" + id + "', " + work + ", " + tips + ", " + ot + ")");
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static List<Stats> loadStats() {
        List<Stats> statsList = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();


            PreparedStatement pst = con.prepareStatement("select name, work, tips, overtime from user_stats");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Stats stats = new Stats(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
//                    reminders.add(reminder);
                statsList.add(stats);
            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }

        return statsList;
    }

    public static List<Stats> loadTeamStats() {
        List<Stats> statsList = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();


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
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }

        return statsList;
    }

    public static void resetStats() {
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            st.addBatch("update user_stats set work = 0, tips = 0, overtime = 0");
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static List<Team> loadTeam(String id) {
        List<Team> teams = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();


            PreparedStatement pst = con.prepareStatement("select name, team, owner, joined from team_stats where name = ?");
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Team team = new Team(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4));
                teams.add(team);
            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }

        return teams;
    }


    public static List<Team> loadTeamMembers(String teamName) {
        List<Team> teams = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();


            PreparedStatement pst = con.prepareStatement("select name, team, owner, joined from team_stats where team = ?");
            pst.setString(1, teamName);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Team team = new Team(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4));
                teams.add(team);
            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }

        return teams;
    }

    public static void createTeam(Team team) {
        List<Team> teams = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();

            String sql = "insert into team_stats (name, team, owner, joined) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement p = con.prepareStatement(sql);
            p.setString(1, team.getName());
            p.setString(2, team.getTeam());
            p.setBoolean(3, team.isOwner());
            p.setBoolean(4, team.isJoined());
            p.execute();
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static void deleteTeam(String name, String teamName) {
        List<Team> teams = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();

            String sql = "delete from  team_stats where name = ? and team = ?";
            PreparedStatement p = con.prepareStatement(sql);
            p.setString(1, name);
            p.setString(2, teamName);
            p.execute();
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static boolean setSleepStart(String name, Time sleepStart) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE profile SET sleep_Start =? WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setTime(1, sleepStart);
                p.setInt(2, id);
                p.execute();
                updated = true;
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }

    public static boolean setSleepEnd(String name, Time sleepEnd) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE profile SET sleep_end =? WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setTime(1, sleepEnd);
                p.setInt(2, id);
                p.execute();
                updated = true;
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }


    public static boolean clearSleep(String name) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE profile SET sleep_end = null, sleep_start = null  WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setInt(1, id);
                p.execute();
                updated = true;
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }


    public static boolean toggleDmReminders(String name) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE profile SET dm_reminder = NOT dm_reminder  WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setInt(1, id);
                p.execute();
                updated = true;
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }

    public static boolean toggleIgnoredHidden(String name) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE profile SET ignored_hidden = NOT ignored_hidden  WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setInt(1, id);
                p.execute();
                updated = true;
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }

    public static boolean toggleDnd(String name) {
        boolean updated = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE profile SET dnd = NOT dnd  WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setInt(1, id);
                p.execute();
                updated = true;
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return updated;
    }


    public static boolean addProfileStats(ProfileStats stats) {
        if (stats.getLocation() == null) {
            return false;
        }

        Boolean newProfile = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            String sql = "insert into profile_stats (name, income, balance, location, import_time) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement p = con.prepareStatement(sql);
            p.setString(1, stats.getName());
            p.setLong(2, stats.getIncome());
            p.setLong(3, stats.getBalance());
            p.setString(4, stats.getLocation().getName());
            p.setTimestamp(5, stats.getImportTime());
            p.execute();

            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return newProfile;
    }


    public static List<ProfileStats> loadProfileStats(String id, int days, LocationEnum location) {
        List<ProfileStats> stats = new ArrayList<>();


        try {


            LocalDateTime now = LocalDateTime.now().minusDays(days);
            Timestamp timestamp = Timestamp.valueOf(now);
            Connection con = databaseUtils.getConnection();
            String sql = "SELECT name, income, balance, location, import_time FROM profile_stats  " +
                    "WHERE name = ? " +
                    "and import_time > ? ";
            if (location != null) {
                sql += "and location = ? ";
            }
            sql += "order by import_time";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, id);

            pst.setTimestamp(2, timestamp);
            if (location != null) {
                pst.setString(3, location.getName());
            }
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ProfileStats stat = new ProfileStats(rs.getString(1), rs.getLong(2), rs.getLong(3), LocationEnum.getLocation(rs.getString(4)), rs.getTimestamp(5));
                stats.add(stat);
            }
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return stats;
    }


    public static List<FlexStats> loadFlexStats(int daysAgoEnd, int days, List<String> ids) {
        List<FlexStats> stats = new ArrayList<>();


        try {

            //SELECT export_time, name, shack_name, shifts, tips FROM member_data

            LocalDateTime end = LocalDateTime.now().minusDays(daysAgoEnd);
            LocalDateTime start = end.minusDays(days);
            Timestamp endStamp = Timestamp.valueOf(end);
            Timestamp startStamp = Timestamp.valueOf(start);
            Connection con = databaseUtils.getConnection();
            String sql = "SELECT export_time, name, shack_name, shifts, tips, donations, votes, overtime FROM member_data  " +
                    "WHERE export_time > ? " +
                    "and export_time < ? ";
            String or = "  ";
            sql += " and (";
//            int count = 3;
            for (String id : ids) {
                sql += or + " name = ? ";
//                pst.setString(count, id);
//                count++;
                or = " or ";
            }
            sql += ") ";
            sql += "order by export_time, name";
            logger.info(sql);

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setTimestamp(1, startStamp);
            pst.setTimestamp(2, endStamp);
            int count = 3;
            for (String id : ids) {
//                sql += " name = ? " + or;
                pst.setString(count, id);
                count++;
//                or = " or ";
            }


            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                FlexStats stat = new FlexStats(rs.getTimestamp(1), rs.getString(2), rs.getString(3), rs.getLong(4), rs.getLong(5), rs.getLong(6), rs.getLong(7), rs.getLong(8));
                stats.add(stat);
            }

            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return stats;
    }

    public static void addWinner(String name) {

        addWinner(name, Timestamp.from(Instant.now()));

//        try {
//            Connection con = databaseUtils.getConnection();
//            Statement st = con.createStatement();
//
//            PreparedStatement pst = con.prepareStatement("SELECT id FROM giveaway_winner  WHERE name = '" + name + "'");
//            ResultSet rs = pst.executeQuery();
//            int id = -1;
//            while (rs.next()) {
//                id = rs.getInt(1);
//                String sql = "UPDATE giveaway_winner SET wins = wins + 1, last_win = ? WHERE id = ?";
//                PreparedStatement p = con.prepareStatement(sql);
//                p.setTimestamp(1, Timestamp.from(Instant.now()));
//                p.executeUpdate();
//            }
//            if (id == -1) {
//                String sql = "insert into giveaway_winner (name, wins, last_win) " +
//                        "VALUES (?, 1, ?)";
//                PreparedStatement p = con.prepareStatement(sql);
//                p.setString(1, name);
//                p.setTimestamp(2, Timestamp.from(Instant.now()));
//                p.execute();
//            }
//            st.executeBatch();
//        } catch (SQLException ex) {
//            databaseUtils.printException(ex);
//        }
    }


    public static void addWinner(String name, Timestamp time) {

        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM giveaway_winner  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE giveaway_winner SET wins = wins + 1, last_win = ? WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setTimestamp(1, time);
                p.setInt(2, id);
                p.executeUpdate();
            }
            if (id == -1) {
                String sql = "insert into giveaway_winner (name, wins, last_win) " +
                        "VALUES (?, 1, ?)";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, name);
                p.setTimestamp(2, time);
                p.execute();
            }

            String sql = "insert into giveaway_log (name, win_time) " +
                    "VALUES (?, ?)";
            PreparedStatement p = con.prepareStatement(sql);
            p.setString(1, name);
            p.setTimestamp(2, time);
            p.execute();
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static List<GiveawayWinner> loadGiveawayWins() {
        List<GiveawayWinner> winers = new ArrayList<>();

        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT name, wins, last_win FROM giveaway_winner Order by wins DESC");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                GiveawayWinner winner = new GiveawayWinner(rs.getString(1), rs.getInt(2), rs.getTimestamp(3));
                winers.add(winner);

            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return winers;
    }


    public static List<GiveawayLog> loadGiveawayLog() {
        return loadGiveawayLog(30);
    }


    public static List<GiveawayLog> loadGiveawayLog(int days) {
        List<GiveawayLog> winers = new ArrayList<>();

        try {
            Connection con = databaseUtils.getConnection();
            LocalDateTime now = LocalDateTime.now().minusDays(days);
            Timestamp nowStamp = Timestamp.valueOf(now);

            PreparedStatement pst = con.prepareStatement("SELECT name, win_time FROM giveaway_log Where win_time > ? Order by win_time DESC");
            pst.setTimestamp(1, nowStamp);
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                GiveawayLog winner = new GiveawayLog(rs.getString(1), rs.getTimestamp(2));
                winers.add(winner);

            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return winers;
    }


    public static ReminderSettings loadReminderSettings(String name) {
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT name, tip, work, overtime, vote, daily, clean, boost FROM reminder_settings " +
                    " WHERE name = ?");
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                ReminderSettings reminderSettings = new ReminderSettings(
                        rs.getString(1),
                        rs.getBoolean(2),
                        rs.getBoolean(3),
                        rs.getBoolean(4),
                        rs.getBoolean(5),
                        rs.getBoolean(6),
                        rs.getBoolean(7),
                        rs.getBoolean(8));
                return reminderSettings;
            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return null;
    }


    public static boolean updateReminderSettings(ReminderSettings reminderSettings) {
        Boolean newProfile = false;
        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM reminder_settings  WHERE name = ?");
            pst.setString(1, reminderSettings.getName());
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                String sql = "UPDATE reminder_settings SET " +
                        "tip = ?, work = ?, overtime = ?, vote = ?, daily = ?, clean = ?, boost = ? WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setBoolean(1, reminderSettings.isTip());
                p.setBoolean(2, reminderSettings.isWork());
                p.setBoolean(3, reminderSettings.isOvertime());
                p.setBoolean(4, reminderSettings.isVote());
                p.setBoolean(5, reminderSettings.isDaily());
                p.setBoolean(6, reminderSettings.isClean());
                p.setBoolean(7, reminderSettings.isBoost());
                p.setInt(8, id);
                p.executeUpdate();
            }
            if (id == -1) {
                String sql = "insert into reminder_settings (name, tip, work, overtime, vote, daily, clean, boost) " +
                        "VALUES (?,?,?,?,?,?,?,?)";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, reminderSettings.getName());
                p.setBoolean(2, reminderSettings.isTip());
                p.setBoolean(3, reminderSettings.isWork());
                p.setBoolean(4, reminderSettings.isOvertime());
                p.setBoolean(5, reminderSettings.isVote());
                p.setBoolean(6, reminderSettings.isDaily());
                p.setBoolean(7, reminderSettings.isClean());
                p.setBoolean(8, reminderSettings.isBoost());
                p.execute();
                newProfile = true;
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return newProfile;
    }

}
