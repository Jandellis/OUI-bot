package action.export;

import action.reminder.Status;
import action.sm.Alert;
import action.sm.AlertType;
import action.sm.Drop;
import action.sm.Trigger;
import action.sm.Watch;
import bot.Config;
import bot.Member;
import bot.Sauce;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils {

    protected static final Logger logger = LogManager.getLogger("ouiBot");

    static Config config = Config.getInstance();
    static String url = config.get("url");
    static String user = config.get("user");
    static String password = config.get("password");


    public static HashMap<Long, List<ExportData>> loadMemberHistory () {
        HashMap<Long, List<ExportData>> history = new HashMap<>();

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();

            LocalDateTime oneWeek = LocalDateTime.now().minusDays(7).minusHours(12);

            PreparedStatement pst = con.prepareStatement("SELECT export_time, name, shack_name, income, shifts, weekly_shifts, tips, donations, happy FROM member_data WHERE export_time > ?  ORDER BY export_time");
            pst.setTimestamp(1, Timestamp.valueOf(oneWeek));
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
                        rs.getDouble(9)
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
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
        return history;
    }


    public static void addMember(Member member, Timestamp exportTime) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
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
                String sql = "insert into member_data (export_time, name, shack_name, income, shifts, weekly_shifts, tips, donations, happy) " +
                        "VALUES (?, ?, ?, ?,?,?,?,?,?)";
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
                p.execute();
            }
            st.executeBatch();
        } catch (SQLException ex) {
            logger.error("Exception", ex);
        }
    }
}
