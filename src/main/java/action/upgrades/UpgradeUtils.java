package action.upgrades;

import action.upgrades.model.UserUpgrades;
import bot.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UpgradeUtils {

    protected static final Logger logger = LogManager.getLogger("ouiBot");

    static Config config = Config.getInstance();
    static String url = config.get("url");
    static String user = config.get("user");
    static String password = config.get("password");


    public static List<UserUpgrades> loadUserUpgrades (String name, String location) {

        List<UserUpgrades> upgrades = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            LocalDateTime oneWeek = LocalDateTime.now().minusDays(7).minusHours(12);

            PreparedStatement pst = con.prepareStatement("SELECT name, location, upgrade, progress FROM user_upgrades WHERE name = ? and location = ?");
            pst.setString(1, name);
            pst.setString(2, location);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {

                UserUpgrades userUpgrades = new UserUpgrades(rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getInt(4)
                        );
                upgrades.add(userUpgrades);
            }


            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return upgrades;
    }


    public static void addUserUpgrades(UserUpgrades userUpgrades) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id, name, location, upgrade, progress FROM user_upgrades WHERE name = ? and location = ? and upgrade = ?");
            pst.setString(1, userUpgrades.getName());
            pst.setString(2, userUpgrades.getLocation());
            pst.setString(3, userUpgrades.getUpgrade());
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
            }

            if (id == -1) {
                String sql = "insert into user_upgrades (name, location, upgrade, progress) " +
                        "VALUES (?, ?, ?, ?)";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(1, userUpgrades.getName());
                p.setString(2, userUpgrades.getLocation());
                p.setString(3, userUpgrades.getUpgrade());
                p.setInt(4, userUpgrades.getProgress());
                p.execute();
            } else {
                String sql = "update user_upgrades set progress = ? " +
                        "WHERE name = ? and location = ?  and upgrade = ?";
                PreparedStatement p = con.prepareStatement(sql);
                p.setString(2, userUpgrades.getName());
                p.setString(3, userUpgrades.getLocation());
                p.setString(4, userUpgrades.getUpgrade());
                p.setInt(1, userUpgrades.getProgress());
                p.execute();
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }

}
