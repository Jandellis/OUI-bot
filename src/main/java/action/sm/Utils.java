package action.sm;

import bot.Config;
import bot.Sauce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils {


    static Config config = Config.getInstance();
    static String url = config.get("url");
    static String user = config.get("user");
    static String password = config.get("password");


    public static HashMap<Integer, Integer> loadLast3(Sauce sauce) {

        HashMap<Integer, Integer> prices = new HashMap<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement("SELECT price, age FROM sm WHERE age < 3 and name = '" + sauce.getName() + "' ORDER BY age ");
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                prices.put(rs.getInt(2), rs.getInt(1));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
    }


    public static void addAlerts(String name, List<Sauce> sauces) {
        deleteAlert(name);
        //delete all old alerts before adding new ones
        try {
            Connection con = DriverManager.getConnection(url, user, password);

            Statement st = con.createStatement();
            con.setAutoCommit(false);

            PreparedStatement pst = con.prepareStatement("SELECT name, alert_type, price FROM sm_triggers  WHERE name = '" + name + "'");
            ResultSet rs = pst.executeQuery();

            List<Trigger> triggers = new ArrayList<>();

            while (rs.next()) {
                Trigger trigger = new Trigger(rs.getString(1), AlertType.getAlertType(rs.getString(2)), rs.getInt(3));
                triggers.add(trigger);
            }

            for (Sauce sauce : sauces) {
                for (Trigger trigger : triggers) {

                    st.addBatch("insert into sm_alerts (name, alert_type, trigger, price) " +
                            "VALUES ('" + name + "', '" + trigger.getType() + "', '" + sauce + "', " + trigger.getPrice() + ")");
                }
            }

            st.executeBatch();
            con.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
    }


}
