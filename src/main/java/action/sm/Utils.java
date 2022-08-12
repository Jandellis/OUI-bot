package action.sm;

import action.reminder.Reminder;
import action.reminder.ReminderType;
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


    public static HashMap<Integer, Integer> loadLast3(Sauce sauce) {
        String sauceName = sauce.getName();
        if (sauce == Sauce.guacamole) {
            sauceName = "guac";
        }


        HashMap<Integer, Integer> prices = new HashMap<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT price, age FROM sm WHERE age < 3 and name = '" + sauceName + "' ORDER BY age ");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                prices.put(rs.getInt(2), rs.getInt(1));
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return prices;

    }

    public static void updatePrices(int pico, int guac, int salsa, int hotsauce, int chipotle) {

        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();
            st.executeUpdate("UPDATE sm set age = age + 1");

            st.addBatch("DELETE from sm WHERE age = 3");

            st.addBatch("insert into sm (name, price, age) " +
                    "VALUES ('pico', " + pico + ", 0);");
            st.addBatch("insert into sm (name, price, age) " +
                    "VALUES ('guac', " + guac + ", 0);");
            st.addBatch("insert into sm (name, price, age) " +
                    "VALUES ('salsa', " + salsa + ", 0);");
            st.addBatch("insert into sm (name, price, age) " +
                    "VALUES ('hotsauce', " + hotsauce + ", 0);");
            st.addBatch("insert into sm (name, price, age) " +
                    "VALUES ('chipotle', " + chipotle + ", 0);");

            st.executeBatch();

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

    public static HashMap<Sauce, Integer> loadPrices() {

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, price FROM sm WHERE age = 0 ORDER BY age ");
             ResultSet rs = pst.executeQuery()) {
            HashMap<Sauce, Integer> prices = new HashMap<>();

            while (rs.next()) {
                prices.put(Sauce.getSauce(rs.getString(1)), rs.getInt(2));
            }
            return prices;

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return null;

    }


    public static List<Alert> loadAlerts() {
        List<Alert> alerts = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, trigger, price FROM sm_alerts");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Alert alert = new Alert(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getString(3), rs.getInt(4));
                alerts.add(alert);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return alerts;
    }

    public static List<Alert> loadAlerts(String id) {
        List<Alert> alerts = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, trigger, price FROM sm_alerts  WHERE name = '" + id + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Alert alert = new Alert(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getString(3), rs.getInt(4));
                alerts.add(alert);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return alerts;
    }

    public static void addAlert(String name, AlertType type, String trigger, int price) {
//        deleteAlert(name);
        //delete alert if same one already exists
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("insert into sm_alerts (name, alert_type, trigger, price) " +
                    "VALUES ('" + name + "', '" + type.getName() + "', '" + trigger + "', " + price + ")");
            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }

    }

    public static void deleteAlert(String name) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from sm_alerts WHERE name = '" + name + "'");
            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static List<Sauce> addAlerts(String name, List<Sauce> sauces) {
        deleteAlert(name);
        //delete all old alerts before adding new ones
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();
            con.setAutoCommit(false);

            List<Watch> watches = Utils.loadWatch(name);

            PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, price FROM sm_triggers  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();

            List<Trigger> triggers = new ArrayList<>();

            while (rs.next()) {
                Trigger trigger = new Trigger(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getInt(3));
                triggers.add(trigger);
            }
            List<Sauce> dropAlerts = new ArrayList<>();


            for (Sauce sauce : sauces) {
                for (Trigger trigger : triggers) {
                    if (trigger.getType() == AlertType.high || (
                            trigger.getType() == AlertType.drop && (
                                    trigger.getDrop() == Drop.both || trigger.getDrop() == Drop.owned
                            )
                    )) {

                        if (trigger.getType() == AlertType.drop) {
                            dropAlerts.add(sauce);
                        }
                        st.addBatch("insert into sm_alerts (name, alert_type, trigger, price) " +
                                "VALUES ('" + name + "', '" + trigger.getType() + "', '" + sauce + "', " + trigger.getPrice() + ")");
                    }
                }
            }
            for (Watch watch : watches) {
                for (Trigger trigger : triggers) {
                    if (trigger.getType() == AlertType.low || (
                            trigger.getType() == AlertType.drop && (
                                    trigger.getDrop() == Drop.both || trigger.getDrop() == Drop.watchlist
                            )
                    )) {
                        if (trigger.getType() == AlertType.drop && dropAlerts.contains(watch.getSauce())) {
                            logger.info("All ready got this alert");
                        } else {
                            if (!sauces.contains(watch.getSauce())) {
                                sauces.add(watch.getSauce());
                            }

                            st.addBatch("insert into sm_alerts (name, alert_type, trigger, price) " +
                                    "VALUES ('" + name + "', '" + trigger.getType() + "', '" + watch.getSauce().getName() + "', " + trigger.getPrice() + ")");
                        }
                    }
                }
            }

            st.executeBatch();
            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return sauces;

    }

    public static void addWatchAlerts(String name) {

        //delete all old alerts before adding new ones
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();
            con.setAutoCommit(false);
            List<Watch> watches = Utils.loadWatch(name);

            PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, price FROM sm_triggers  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();

            List<Trigger> triggers = new ArrayList<>();

            while (rs.next()) {
                Trigger trigger = new Trigger(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getInt(3));
                triggers.add(trigger);
            }

            for (Watch watch : watches) {
                for (Trigger trigger : triggers) {
                    if (trigger.getType() == AlertType.low || (
                            trigger.getType() == AlertType.drop && (
                                    trigger.getDrop() == Drop.both || trigger.getDrop() == Drop.watchlist
                            )
                    )) {

                        st.addBatch("insert into sm_alerts (name, alert_type, trigger, price) " +
                                "VALUES ('" + name + "', '" + trigger.getType() + "', '" + watch.getSauce().getName() + "', " + trigger.getPrice() + ")");
                    }
                }
            }

            st.executeBatch();
            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static void addTrigger(String name, AlertType type, int price) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM sm_triggers  WHERE name = '" + name + "' AND alert_type='" + type + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
            }
            if (id != -1) {
                st.executeUpdate("UPDATE sm_triggers SET price = " + price + " WHERE id = " + id);
            } else {
                st.addBatch("insert into sm_triggers (name, alert_type, price) " +
                        "VALUES ('" + name + "', '" + type.getName() + "', " + price + ")");
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

    public static void deleteTrigger(String name) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from sm_triggers WHERE name = '" + name + "'");
            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static List<Trigger> loadTriggers(String id) {
        List<Trigger> alerts = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, price FROM sm_triggers  WHERE name = '" + id + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Trigger alert = new Trigger(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getInt(3));
                alerts.add(alert);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return alerts;
    }








    public static void addWatch(String name, Sauce sauce) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM sm_watch  WHERE name = '" + name + "' AND sauce='" + sauce.getName() + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
            }
            if (id == -1) {
                st.addBatch("insert into sm_watch (name, sauce) " +
                        "VALUES ('" + name + "', '" + sauce.getName() + "')");
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

    public static void deleteWatch(String name) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from sm_watch WHERE name = '" + name + "'");
            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }


    public static List<Watch> loadWatch(String id) {
        List<Watch> watches = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT name, sauce FROM sm_watch  WHERE name = '" + id + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Watch watch = new Watch(rs.getString(1), Sauce.getSauce(rs.getString(2)));
                watches.add(watch);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return watches;
    }




    public static List<SystemReminder> loadReminder(SystemReminderType rem) {
        List<SystemReminder> reminders = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT type, reminder_time, message_id FROM system_reminder  WHERE type = '" + rem.getName() + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                SystemReminder reminder = new SystemReminder( SystemReminderType.getReminderType(rs.getString(1)), rs.getTimestamp(2), rs.getString(3));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminders;
    }

    public static List<SystemReminder> loadReminder() {
        List<SystemReminder> reminders = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT type, reminder_time, message_id FROM system_reminder ");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                SystemReminder reminder = new SystemReminder( SystemReminderType.getReminderType(rs.getString(1)), rs.getTimestamp(2), rs.getString(3));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminders;
    }


    public static SystemReminder addReminder(SystemReminderType type, Timestamp time, String messageId) {
        SystemReminder reminder = new SystemReminder(type, time, messageId);
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM system_reminder  WHERE type='" + type.getName() + "'");
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
            }
            if (id != -1) {
                st.executeUpdate("UPDATE system_reminder SET reminder_time = '" + time + "' WHERE id = " + id);
            } else {
                st.addBatch("insert into system_reminder (type, reminder_time, message_id) " +
                        "VALUES ('" + type.getName() + "', '" + time + "', '"+messageId+"')");
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return reminder;
    }


    public static void deleteReminder(SystemReminderType type) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from system_reminder WHERE type='" + type.getName() + "'");
            st.executeBatch();
//            con.commit();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }
}
