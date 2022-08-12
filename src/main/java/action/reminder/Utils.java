package action.reminder;

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
import java.util.ArrayList;
import java.util.List;

public class Utils {

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
            if (id != -1) {
                st.executeUpdate("UPDATE reminder SET reminder_time = '" + time + "' WHERE id = " + id);
                reminder.setId(-2);
                st.executeBatch();
            } else {

                PreparedStatement pst2 = con.prepareStatement("insert into reminder (name, type, reminder_time, channel) " +
                        "VALUES ('" + name + "', '" + type.getName() + "', '" + time + "', '" + channel + "') RETURNING id");
                ResultSet rs2 = pst2.executeQuery();
                while (rs2.next()) {
                    reminder.setId(rs2.getInt(1));
                }
            }
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminder;
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
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel FROM reminder ");
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
                String sql = "UPDATE profile SET shack_name = ? WHERE id = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, shack);
                p.setInt(2, id);
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

            PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status, enabled, react, message FROM profile  WHERE shack_name = ? and enabled = true");
            pst.setString(1, shack);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                profile = new Profile(rs.getString(1), rs.getString(2), Status.getStatus(rs.getString(3)), rs.getBoolean(4), rs.getString(5), rs.getString(6));

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
             PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status, enabled, react, message FROM profile  WHERE name = '" + id + "' and enabled = true");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                profile = new Profile(rs.getString(1), rs.getString(2), Status.getStatus(rs.getString(3)), rs.getBoolean(4), rs.getString(5), rs.getString(6));
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return profile;
    }
}
