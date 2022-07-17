package action.reminder;

import action.sm.Alert;
import action.sm.AlertType;
import action.sm.Drop;
import action.sm.Trigger;
import action.sm.Watch;
import bot.Config;
import bot.Sauce;
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
import java.util.HashMap;
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
            } else {
                st.addBatch("insert into reminder (name, type, reminder_time, channel) " +
                        "VALUES ('" + name + "', '" + type.getName() + "', '" + time + "', '"+channel+"')");
            }
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


    public static List<Reminder> loadReminder(String id) {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, type, reminder_time, channel FROM reminder  WHERE name = '" + id + "'");
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








    public static void addProfile(String name, String shack, Status status) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM profile  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
            }
            if (id == -1) {
                st.addBatch("insert into profile (name, shack_name, status) " +
                        "VALUES ('" + name + "', '" + shack + "', '" + status.name() + "')");
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }



    public static Profile loadProfileByName( String shack) {
        Profile profile = null;
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status FROM profile  WHERE shack_name = '" + shack + "'");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                profile = new Profile(rs.getString(1), rs.getString(2), Status.getStatus(rs.getString(3)));

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
             PreparedStatement pst = con.prepareStatement("SELECT name, shack_name, status FROM profile  WHERE name = '" + id + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                profile = new Profile(rs.getString(1), rs.getString(2), Status.getStatus(rs.getString(3)));
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return profile;
    }
}
