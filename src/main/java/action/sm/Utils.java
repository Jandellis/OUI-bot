package action.sm;

import action.sm.model.Alert;
import action.sm.model.AlertType;
import action.sm.model.Drop;
import action.sm.model.SauceMarketStats;
import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import action.sm.model.Trigger;
import action.sm.model.Watch;
import bot.Config;
import bot.Sauce;
import database.DatabaseUtils;
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

    
    static DatabaseUtils databaseUtils = DatabaseUtils.getInstance();


    public static HashMap<Integer, Integer> loadLast3(Sauce sauce) {
        String sauceName = sauce.getName();
        if (sauce == Sauce.guacamole) {
            sauceName = "guac";
        }


        HashMap<Integer, Integer> prices = new HashMap<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT price, age FROM sm WHERE age < 3 and name = '" + sauceName + "' ORDER BY age ");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                prices.put(rs.getInt(2), rs.getInt(1));
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return prices;

    }


    public static HashMap<Integer, Integer> loadLast5(Sauce sauce) {
        String sauceName = sauce.getName();
        if (sauce == Sauce.guacamole) {
            sauceName = "guac";
        }


        HashMap<Integer, Integer> prices = new HashMap<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT price, update_time FROM sm_history WHERE name = '" + sauceName + "' ORDER BY update_time desc LIMIT 5 ");
             ResultSet rs = pst.executeQuery()) {

            int count = 1;
            while (rs.next()) {
                prices.put(count, rs.getInt(1));
                count++;
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return prices;

    }

    public static void updatePrices(int pico, int guac, int salsa, int hotsauce, int chipotle) {

        try {
            Connection con = databaseUtils.getConnection();

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

            HashMap<Integer, Integer> picoHistory = loadLast5(Sauce.pico);
            HashMap<Integer, Integer> guacHistory = loadLast5(Sauce.guacamole);
            HashMap<Integer, Integer> salsaHistory = loadLast5(Sauce.salsa);
            HashMap<Integer, Integer> hotsauceHistory = loadLast5(Sauce.hotsauce);
            HashMap<Integer, Integer> chipotleHistory = loadLast5(Sauce.chipotle);
            if (picoHistory.size() == 5) {

                st.addBatch("insert into sm_history (name, price, update_time, last_change, change_1_to_2, change_1_to_3, change_1_to_4, change_1_to_5, change_0_to_1, change_0_to_2, change_0_to_3, change_0_to_4) " +
                        "VALUES ('pico', " + pico + ", DATE_SUB(NOW(), INTERVAL 10 MINUTE), " + (pico - picoHistory.get(1)) + ", " + (picoHistory.get(1) - picoHistory.get(2)) + ", " + (picoHistory.get(1) - picoHistory.get(3)) + ", " + (picoHistory.get(1) - picoHistory.get(4)) + ", " + (picoHistory.get(1) - picoHistory.get(5)) + ", " + (pico - picoHistory.get(1)) + ", " + (pico - picoHistory.get(2)) + ", " + (pico - picoHistory.get(3)) + ", " + (pico - picoHistory.get(4)) + ");");
                st.addBatch("insert into sm_history (name, price, update_time, last_change, change_1_to_2, change_1_to_3, change_1_to_4, change_1_to_5, change_0_to_1, change_0_to_2, change_0_to_3, change_0_to_4) " +
                        "VALUES ('guac', " + guac + ", DATE_SUB(NOW(), INTERVAL 10 MINUTE), " + (guac - guacHistory.get(1)) + ", " + (guacHistory.get(1) - guacHistory.get(2)) + ", " + (guacHistory.get(1) - guacHistory.get(3)) + ", " + (guacHistory.get(1) - guacHistory.get(4)) + ", " + (guacHistory.get(1) - guacHistory.get(5)) + ", " + (guac - guacHistory.get(1)) + ", " + (guac - guacHistory.get(2)) + ", " + (guac - guacHistory.get(3)) + ", " + (guac - guacHistory.get(4)) + ");");
                st.addBatch("insert into sm_history (name, price, update_time, last_change, change_1_to_2, change_1_to_3, change_1_to_4, change_1_to_5, change_0_to_1, change_0_to_2, change_0_to_3, change_0_to_4) " +
                        "VALUES ('salsa', " + salsa + ", DATE_SUB(NOW(), INTERVAL 10 MINUTE), " + (salsa - salsaHistory.get(1)) + ", " + (salsaHistory.get(1) - salsaHistory.get(2)) + ", " + (salsaHistory.get(1) - salsaHistory.get(3)) + ", " + (salsaHistory.get(1) - salsaHistory.get(4)) + ", " + (salsaHistory.get(1) - salsaHistory.get(5)) + ", " + (salsa - salsaHistory.get(1)) + ", " + (salsa - salsaHistory.get(2)) + ", " + (salsa - salsaHistory.get(3)) + ", " + (salsa - salsaHistory.get(4)) + ");");
                st.addBatch("insert into sm_history (name, price, update_time, last_change, change_1_to_2, change_1_to_3, change_1_to_4, change_1_to_5, change_0_to_1, change_0_to_2, change_0_to_3, change_0_to_4) " +
                        "VALUES ('hotsauce', " + hotsauce + ", DATE_SUB(NOW(), INTERVAL 10 MINUTE), " + (hotsauce - hotsauceHistory.get(1)) + ", " + (hotsauceHistory.get(1) - hotsauceHistory.get(2)) + ", " + (hotsauceHistory.get(1) - hotsauceHistory.get(3)) + ", " + (hotsauceHistory.get(1) - hotsauceHistory.get(4)) + ", " + (hotsauceHistory.get(1) - hotsauceHistory.get(5)) + ", " + (hotsauce - hotsauceHistory.get(1)) + ", " + (hotsauce - hotsauceHistory.get(2)) + ", " + (hotsauce - hotsauceHistory.get(3)) + ", " + (hotsauce - hotsauceHistory.get(4)) + ");");
                st.addBatch("insert into sm_history (name, price, update_time, last_change, change_1_to_2, change_1_to_3, change_1_to_4, change_1_to_5, change_0_to_1, change_0_to_2, change_0_to_3, change_0_to_4) " +
                        "VALUES ('chipotle', " + chipotle + ", DATE_SUB(NOW(), INTERVAL 10 MINUTE), " + (chipotle - chipotleHistory.get(1)) + ", " + (chipotleHistory.get(1) - chipotleHistory.get(2)) + ", " + (chipotleHistory.get(1) - chipotleHistory.get(3)) + ", " + (chipotleHistory.get(1) - chipotleHistory.get(4)) + ", " + (chipotleHistory.get(1) - chipotleHistory.get(5)) + ", " + (chipotle - chipotleHistory.get(1)) + ", " + (chipotle - chipotleHistory.get(2)) + ", " + (chipotle - chipotleHistory.get(3)) + ", " + (chipotle - chipotleHistory.get(4)) + ");");


            }
            st.executeBatch();
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static HashMap<Sauce, Integer> loadPrices() {

        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, price FROM sm WHERE age = 0 ORDER BY age ");
             ResultSet rs = pst.executeQuery()) {
            HashMap<Sauce, Integer> prices = new HashMap<>();

            while (rs.next()) {
                prices.put(Sauce.getSauce(rs.getString(1)), rs.getInt(2));
            }
            con.close();
            return prices;

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return null;

    }


    public static List<Alert> loadAlerts() {
        List<Alert> alerts = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, sm_trigger, price, channel FROM sm_alerts");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Alert alert = new Alert(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getString(3), rs.getInt(4), rs.getString(5));
                alerts.add(alert);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return alerts;
    }

    public static List<Alert> loadAlerts(String id) {
        List<Alert> alerts = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, sm_trigger, price, channel FROM sm_alerts  WHERE name = '" + id + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Alert alert = new Alert(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getString(3), rs.getInt(4), rs.getString(5));
                alerts.add(alert);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return alerts;
    }

    public static void addAlert(String name, AlertType type, String trigger, int price, String channel) {
//        deleteAlert(name);
        //delete alert if same one already exists
        try {
            Connection con = databaseUtils.getConnection();

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("insert into sm_alerts (name, alert_type, sm_trigger, price, channel) " +
                    "VALUES ('" + name + "', '" + type.getName() + "', '" + trigger + "', " + price + ", '" + channel + "')");
            st.executeBatch();
            con.close();
//            con.commit();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }

    }

    public static void deleteAlert(String name) {
        try {
            Connection con = databaseUtils.getConnection();

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from sm_alerts WHERE name = '" + name + "'");
            st.executeBatch();
            con.close();
//            con.commit();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static List<Sauce> addAlerts(String name, List<Sauce> sauces, String channel) {
        deleteAlert(name);
        //delete all old alerts before adding new ones
        try {
            Connection con = databaseUtils.getConnection();

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
                        st.addBatch("insert into sm_alerts (name, alert_type, sm_trigger, price, channel) " +
                                "VALUES ('" + name + "', '" + trigger.getType() + "', '" + sauce + "', " + trigger.getPrice() + ", '" + channel + "')");
                    }
                }
            }
            for (Watch watch : watches) {
                for (Trigger trigger : triggers) {
                    // alert is low, or drop type
                    // for drop if its on watch list and both
                    // but not if they own the sauce
                    if ((trigger.getType() == AlertType.low || trigger.getType() == AlertType.rise) && !sauces.contains(watch.getSauce()) || (
                            trigger.getType() == AlertType.drop && (
                                    trigger.getDrop() == Drop.both || trigger.getDrop() == Drop.watchlist
                            )
                    )) {
                        if (trigger.getType() == AlertType.drop && dropAlerts.contains(watch.getSauce())) {
                            logger.info("All ready got this alert");
                        } else {
//                            if (!sauces.contains(watch.getSauce())) {
//                                sauces.add(watch.getSauce());
//                            }

                            st.addBatch("insert into sm_alerts (name, alert_type, sm_trigger, price, channel) " +
                                    "VALUES ('" + name + "', '" + trigger.getType() + "', '" + watch.getSauce().getName() + "', " + trigger.getPrice() + ", '" + channel + "')");
                        }
                    }
                }
            }

            st.executeBatch();
            con.commit();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return sauces;

    }

    public static void addWatchAlerts(String name, String channel) {

        //delete all old alerts before adding new ones
        try {
            Connection con = databaseUtils.getConnection();

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

                        st.addBatch("insert into sm_alerts (name, alert_type, sm_trigger, price, channel) " +
                                "VALUES ('" + name + "', '" + trigger.getType() + "', '" + watch.getSauce().getName() + "', " + trigger.getPrice() + ", '" + channel + "')");
                    }
                }
            }

            st.executeBatch();
            con.commit();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static void addTrigger(String name, AlertType type, int price) {
        try {
            Connection con = databaseUtils.getConnection();
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
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static void deleteTrigger(String name) {
        try {
            Connection con = databaseUtils.getConnection();

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from sm_triggers WHERE name = '" + name + "'");
            st.executeBatch();
            con.close();
//            con.commit();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static List<Trigger> loadTriggers(String id) {
        List<Trigger> alerts = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, price FROM sm_triggers  WHERE name = '" + id + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Trigger alert = new Trigger(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getInt(3));
                alerts.add(alert);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return alerts;
    }


    public static void addWatch(String name, Sauce sauce) {
        try {
            Connection con = databaseUtils.getConnection();
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
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static void deleteWatch(String name) {
        try {
            Connection con = databaseUtils.getConnection();

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from sm_watch WHERE name = '" + name + "'");
            st.executeBatch();
            con.close();
//            con.commit();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static List<Watch> loadWatch(String id) {
        List<Watch> watches = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name, sauce FROM sm_watch  WHERE name = '" + id + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Watch watch = new Watch(rs.getString(1), Sauce.getSauce(rs.getString(2)));
                watches.add(watch);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return watches;
    }


    public static List<SystemReminder> loadReminder(SystemReminderType rem) {
        List<SystemReminder> reminders = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT type, reminder_time, message_id, name FROM system_reminder  WHERE type = '" + rem.getName() + "'");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                SystemReminder reminder = new SystemReminder(SystemReminderType.getReminderType(rs.getString(1)), rs.getTimestamp(2), rs.getString(3), rs.getString(4));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminders;
    }

    public static List<SystemReminder> loadReminder() {
        List<SystemReminder> reminders = new ArrayList<>();
        try (Connection con = databaseUtils.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT type, reminder_time, message_id, name FROM system_reminder ");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                SystemReminder reminder = new SystemReminder(SystemReminderType.getReminderType(rs.getString(1)), rs.getTimestamp(2), rs.getString(3), rs.getString(4));
                reminders.add(reminder);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminders;
    }


    public static SystemReminder addReminder(SystemReminderType type, Timestamp time, String messageId, String name) {
        SystemReminder reminder = new SystemReminder(type, time, messageId, name);
        try {
            Connection con = databaseUtils.getConnection();
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
                st.addBatch("insert into system_reminder (type, reminder_time, message_id, name) " +
                        "VALUES ('" + type.getName() + "', '" + time + "', '" + messageId + "', '" + name + "')");
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return reminder;
    }


    public static void deleteReminder(SystemReminderType type) {
        try {
            Connection con = databaseUtils.getConnection();

            Statement st = con.createStatement();

//            con.setAutoCommit(false);

            st.addBatch("DELETE from system_reminder WHERE type='" + type.getName() + "'");
            st.executeBatch();
            con.close();
//            con.commit();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static List<SauceMarketStats> loadHistoryStats() {


        List<SauceMarketStats> stats = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();

            String sql = "select change_data.change_1_to_2 as change_data, " +
                    "round(z.total / change_data.total * 100, 2) as zero, " +
                    "round(p.total / change_data.total * 100, 2) as positive, " +
                    "round(n.total / change_data.total * 100, 2) as negative, " +
                    "change_data.total as \"times occurred\" " +
                    "from " +
                    "(select change_1_to_2, count(*) as total from sm_history where last_change = 0 group by change_1_to_2) z, " +
                    "(select change_1_to_2, count(*) as total from sm_history where last_change > 0 group by change_1_to_2) p, " +
                    "(select change_1_to_2, count(*) as total from sm_history where last_change < 0 group by change_1_to_2) n, " +
                    "(select change_1_to_2, count(*) as total from sm_history group by change_1_to_2) change_data " +
                    "where z.change_1_to_2 = change_data.change_1_to_2 and " +
                    "p.change_1_to_2 = change_data.change_1_to_2 and " +
                    "n.change_1_to_2 = change_data.change_1_to_2 ";

//            if (!sauce.equals("all")) {
//                sql += " "
//            }


            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                SauceMarketStats stat = new SauceMarketStats(rs.getInt(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getInt(5));
                stats.add(stat);
            }
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return stats;

    }


    public static List<SauceMarketStats> loadHistoryStats(String sauce) {


        List<SauceMarketStats> stats = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();

            String sql = "select change_data.change_1_to_2 as change_data, " +
                    "round(z.total / change_data.total * 100, 2) as zero, " +
                    "round(p.total / change_data.total * 100, 2) as positive, " +
                    "round(n.total / change_data.total * 100, 2) as negative, " +
                    "change_data.total as \"times occurred\" " +
                    "from " +
                    "(select change_1_to_2, count(*) as total from sm_history where last_change = 0 and name = ? group by change_1_to_2) z, " +
                    "(select change_1_to_2, count(*) as total from sm_history where last_change > 0 and name = ? group by change_1_to_2) p, " +
                    "(select change_1_to_2, count(*) as total from sm_history where last_change < 0 and name = ? group by change_1_to_2) n, " +
                    "(select change_1_to_2, count(*) as total from sm_history where name = ? group by change_1_to_2) change_data " +
                    "where z.change_1_to_2 = change_data.change_1_to_2 and " +
                    "p.change_1_to_2 = change_data.change_1_to_2 and " +
                    "n.change_1_to_2 = change_data.change_1_to_2 ";

//            if (!sauce.equals("all")) {
//                sql += " "
//            }


            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, sauce);
            pst.setString(2, sauce);
            pst.setString(3, sauce);
            pst.setString(4, sauce);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                SauceMarketStats stat = new SauceMarketStats(rs.getInt(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getInt(5));
                stats.add(stat);
            }
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return stats;

    }


    public static void addOldPrices(String sauce, int price, int change, int oldChange, Timestamp time) {

        try {
            Connection con = databaseUtils.getConnection();

//            Statement st = con.createStatement();
//
//            st.addBatch("insert into sm_history (name, price, update_time, change, change_1_to_2) " +
//                    "VALUES (?, ?, ?, ?, ?);");
//
//            st.executeBatch();

            PreparedStatement pst = con.prepareStatement("insert into sm_history (name, price, update_time, last_change, change_1_to_2) " +
                    "VALUES (?, ?, ?, ?, ?);");
            pst.setString(1, sauce);
            pst.setInt(2, price);
            pst.setTimestamp(3, time);
            pst.setInt(4, change);
            pst.setInt(5, oldChange);
            pst.execute();
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }
}
