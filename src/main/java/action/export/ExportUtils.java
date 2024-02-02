package action.export;

import action.export.model.Donations;
import action.export.model.FranchiseConfig;
import action.export.model.FranchiseStatType;
import action.export.model.FranchiseStats;
import action.export.model.MemberDonations;
import action.export.model.WarningData;
import action.reminder.model.ProfileStats;
import action.upgrades.model.LocationEnum;
import bot.Config;
import bot.Member;
import database.DatabaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExportUtils {

    protected static final Logger logger = LogManager.getLogger("ouiBot");

    static Config config = Config.getInstance();
    static String url = config.get("url");
    static String user = config.get("user");
        static String password = config.get("password");
        static DatabaseUtils databaseUtils = DatabaseUtils.getInstance();


    public static HashMap<Long, List<ExportData>> loadMemberHistory(String franchise) {

        LocalDateTime oneWeek = LocalDateTime.now().minusDays(7).minusHours(12);
        LocalDateTime now = LocalDateTime.now();

        return loadMemberHistory(oneWeek, now, franchise);
    }

    public static HashMap<Long, List<ExportData>> loadMemberHistoryYesterday(String franchise) {

        LocalDateTime oneWeek = LocalDateTime.now().minusDays(8).minusHours(12);
        LocalDateTime now = LocalDateTime.now().minusHours(12);

        return loadMemberHistory(oneWeek, now, franchise);
    }

    public static HashMap<Long, List<ExportData>> loadMemberHistory(LocalDateTime start, LocalDateTime end, String franchise) {
        HashMap<Long, List<ExportData>> history = new HashMap<>();

        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();


            PreparedStatement pst = con.prepareStatement("SELECT export_time, name, shack_name, income, shifts, weekly_shifts, tips, donations, happy, overtime, votes, franchise " +
                    "FROM member_data " +
                    "WHERE export_time > ? and " +
                    "franchise = ? and " +
                    "export_time < ?  ORDER BY export_time ");
            pst.setTimestamp(1, Timestamp.valueOf(start));
            pst.setString(2, franchise);
            pst.setTimestamp(3, Timestamp.valueOf(end));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Timestamp exportTime = rs.getTimestamp(1);

                Member member = new Member(Long.parseLong(rs.getString(2)),
                        rs.getString(3),
                        rs.getInt(4),
                        rs.getInt(5),
                        rs.getInt(6),
                        rs.getInt(7),
                        rs.getLong(8),
                        rs.getDouble(9),
                        rs.getInt(10),
                        rs.getInt(11),
                        rs.getString(12)

                );
                ExportData data = new ExportData(member, exportTime);
                if (history.containsKey(member.getId())) {
                    history.get(member.getId()).add(data);
                } else {
                    List<ExportData> dataList = new ArrayList<>();
                    dataList.add(data);
                    history.put(member.getId(), dataList);
                }
            }


            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return history;
    }


    public static void addMember(Member member, Timestamp exportTime) {
        Connection con;
        try {
            con = databaseUtils.getConnection();
            Statement st = con.createStatement();

            PreparedStatement pst = con.prepareStatement("SELECT id FROM member_data  WHERE export_time = ? and name = ?");
            pst.setTimestamp(1, exportTime);
            pst.setString(2, member.getId().toString());
            ResultSet rs = pst.executeQuery();
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
                //the data should be identical... no point updating
            }

//            export_time timestamp,
//            name VARCHAR(25),
//                    shack_name VARCHAR(30),
//                    income int,
//            shifts int,
//            weekly_shifts int,
//            tips int,
//            donations int,
//            happy double


            if (id == -1) {
                String sql = "insert into member_data (export_time, name, shack_name, income, shifts, weekly_shifts, tips, donations, happy, overtime, votes, franchise) " +
                        "VALUES (?, ?, ?, ?,?,?,?,?,?,?,?, ?)";
                PreparedStatement p = con.prepareStatement(sql);
                p.setTimestamp(1, exportTime);
                p.setString(2, member.getId().toString());
                p.setString(3, member.getName());
                p.setInt(4, member.getIncome());
                p.setInt(5, member.getShifts());
                p.setInt(6, member.getWeeklyShifts());
                p.setInt(7, member.getTips());
                p.setLong(8, member.getDonations());
                p.setDouble(9, member.getHappy());
                p.setInt(10, member.getOvertime());
                p.setInt(11, member.getVotes());
                p.setString(12, member.getFranchise());
                p.execute();
            }
            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }


    public static List<Donations> loadDonations(String franchise) {
        List<Donations> donations = new ArrayList<>();

        try {
            Connection con = databaseUtils.getConnection();
            Statement st = con.createStatement();


            PreparedStatement pst = con.prepareStatement("SELECT max_donation, min_donation, role FROM donations where franchise = ?");
            pst.setString(1, franchise);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {

                Donations donation = new Donations(rs.getLong(1),
                        rs.getLong(2),
                        rs.getString(3)
                );
                donations.add(donation);
            }


            st.executeBatch();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return donations;
    }


    public static boolean updateMemberDonations(String id, long donation) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE member_donations SET donation = donation + ? WHERE name = ?");
            pst.setLong(1, donation);
            pst.setString(2, id);
            int rs = pst.executeUpdate();
            if (rs == 1) {
                con.close();
                return true;
            } else {

                pst = con.prepareStatement("INSERT INTO member_donations (name, donation) VALUES (?, ?)");
                pst.setString(1, id);
                pst.setLong(2, donation);
                return pst.execute();
            }
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }

    public static boolean clearWarning(String id) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE member_donations SET donation = donation - 5000000 WHERE name = ?");
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


    public static boolean resetMemberDonations(String id) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE member_donations SET donation = ? WHERE name = ?");
            pst.setLong(1, 0);
            pst.setString(2, id);
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

    public static MemberDonations loadMemberDonations(String id) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT name, donation FROM member_donations WHERE name = ?");
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                MemberDonations memberDonations = new MemberDonations(rs.getLong(2), rs.getString(1));
                con.close();
                return memberDonations;
            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return new MemberDonations(0, id);
    }


    public static boolean updateWarningData(WarningData warningData) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE warning_data SET immunity_until = ?,  last_warning = ? WHERE name = ?");
            pst.setTimestamp(1, warningData.getImmunityUntil());
            pst.setTimestamp(2, warningData.getLastWarning());
            pst.setString(3, warningData.getName());
            int rs = pst.executeUpdate();
            if (rs == 1) {
                con.close();
                return true;
            } else {

                pst = con.prepareStatement("INSERT INTO warning_data (name, last_warning, immunity_until ) VALUES (?, ?, ?)");
                pst.setString(1, warningData.getName());
                pst.setTimestamp(2, warningData.getLastWarning());
                pst.setTimestamp(3, warningData.getImmunityUntil());
                return pst.execute();
            }
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }


    public static boolean updateWarningData(String id, Timestamp giveawayUntil) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE warning_data SET giveaway_until = ? WHERE name = ?");
            pst.setTimestamp(1,giveawayUntil);
            pst.setString(2, id);
            int rs = pst.executeUpdate();
            if (rs == 1) {
                con.close();
                return true;
            } else {

                pst = con.prepareStatement("INSERT INTO warning_data (name, giveaway_until ) VALUES (?, ?)");
                pst.setString(1, id);
                pst.setTimestamp(2, giveawayUntil);
                return pst.execute();
            }
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }


    public static WarningData loadWarningData(String id) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT name, immunity_until, last_warning, giveaway_until FROM warning_data WHERE name = ?");
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                WarningData warningData = new WarningData(
                        rs.getString(1),
                        rs.getTimestamp(2),
                        rs.getTimestamp(3),
                        rs.getTimestamp(4));
                con.close();
                return warningData;
            }
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return new WarningData(id, null, null, null);
    }

    public static List<WarningData> loadWarningDataAfterImmunity() {
        List<WarningData> warningDataList = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();


            PreparedStatement pst = con.prepareStatement("SELECT name, immunity_until, last_warning, giveaway_until FROM warning_data " +
                    "WHERE immunity_until < now()");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                WarningData warningData = new WarningData(
                        rs.getString(1),
                        rs.getTimestamp(2),
                        rs.getTimestamp(3),
                        rs.getTimestamp(4));
                warningDataList.add(warningData);
            }
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return warningDataList;
    }


    public static List<WarningData> loadWarningDataAfterGiveaway() {
        List<WarningData> warningDataList = new ArrayList<>();
        try {
            Connection con = databaseUtils.getConnection();


            PreparedStatement pst = con.prepareStatement("SELECT name, immunity_until, last_warning, giveaway_until FROM warning_data " +
                    "WHERE giveaway_until < now()");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                WarningData warningData = new WarningData(
                        rs.getString(1),
                        rs.getTimestamp(2),
                        rs.getTimestamp(3),
                        rs.getTimestamp(4));
                warningDataList.add(warningData);
            }
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return warningDataList;
    }

    public static void removeMember(String name) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE franchise SET members = members - 1 WHERE name = ?");
            pst.setString(1, name);
            int rs = pst.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static void addMember(String name) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE franchise SET members = members + 1 WHERE name = ?");
            pst.setString(1, name);
            int rs = pst.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static int getMembers(String name) {

        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT members FROM franchise " +
                    "WHERE name = ?");
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return 0;
    }


    public static void updateFranchise(FranchiseStatType type, Long value, String name) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("UPDATE franchise SET " +type.getName()+ " = ? WHERE name = ?");
            pst.setLong(1, value);
            pst.setString(2, name);
            int rs = pst.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
    }

    public static long getFranchise(String name, FranchiseStatType type) {

        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT "+type.getName()+" FROM franchise " +
                    "WHERE name = ?");
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return 0;
    }
    public static FranchiseConfig getFranchiseConfig(String guild) {

        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT guild," +
                    " name," +
                    " warning," +
                    " warning_2," +
                    " warning_3," +
                    " flex," +
                    " recruiter," +
                    " immunity," +
                    " giveawayRole, court FROM franchise_config " +
                    "WHERE guild = ?");
            pst.setString(1, guild);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                return new FranchiseConfig(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10)
                );
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return null;
    }
    public static FranchiseConfig getFranchiseConfigByName(String name) {

        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT guild," +
                    " name," +
                    " warning," +
                    " warning_2," +
                    " warning_3," +
                    " flex," +
                    " recruiter," +
                    " immunity," +
                    " giveawayRole, court FROM franchise_config " +
                    "WHERE name = ?");
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                return new FranchiseConfig(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10)
                );
            }

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return null;
    }


    public static boolean insertFranchiseStats(FranchiseStats franchiseStats) {
        try {
            Connection con = databaseUtils.getConnection();
            PreparedStatement pst = con.prepareStatement("INSERT INTO franchise_stats (name, income, sold, balance, import_time) VALUES (?, ?, ?, ?, ?)");
                pst.setString(1, franchiseStats.getName());
                pst.setLong(2, franchiseStats.getIncome());
                pst.setLong(3, franchiseStats.getSold());
                pst.setLong(4, franchiseStats.getBalance());
                pst.setTimestamp(5, franchiseStats.getTime());
                pst.execute();
                con.close();
                return true;
        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return false;
    }


    public static List<FranchiseStats> loadFranchiseStats(String name, int days) {
        List<FranchiseStats> stats = new ArrayList<>();


        try {


            LocalDateTime now = LocalDateTime.now().minusDays(days);
            Timestamp timestamp = Timestamp.valueOf(now);
            Connection con = databaseUtils.getConnection();
            String sql = "SELECT name, income, sold, balance, import_time FROM franchise_stats  " +
                    "WHERE name = ? " +
                    "and import_time > ? ";
            sql += "order by import_time";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);

            pst.setTimestamp(2, timestamp);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                FranchiseStats stat = new FranchiseStats(rs.getString(1), rs.getLong(2), rs.getLong(3), rs.getLong(4), rs.getTimestamp(5));
                stats.add(stat);
            }
            con.close();

        } catch (SQLException ex) {
            databaseUtils.printException(ex);
        }
        return stats;
    }
}
