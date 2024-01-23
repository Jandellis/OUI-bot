package database;

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
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
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

public class DatabaseUtils {

    private static DatabaseUtils databaseUtils;

    protected static final Logger logger = LogManager.getLogger("ouiBot");


    private DatabaseUtils() {
    }

    public static DatabaseUtils getInstance() {
        if (databaseUtils == null) {
            databaseUtils = new DatabaseUtils();
        }

        return databaseUtils;
    }

    static Config config = Config.getInstance();
    static String url = config.get("url");

    protected static GatewayDiscordClient gateway;

    public void setGateway(GatewayDiscordClient gateway) {
        DatabaseUtils.gateway = gateway;
    }

    public Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(url);
        return con;
    }


    public void printException(Throwable e) {
        try {
            logger.error("Exception", e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();

            if (sStackTrace.length() > 1000) {
                sStackTrace = sStackTrace.substring(0, 999);
            }

            String finalSStackTrace = sStackTrace;
            gateway.getUserById(Snowflake.of("292839877563908097")).block().getPrivateChannel().flatMap(channel -> {
                channel.createMessage("**something broke in the DB!!**\r\n\r\n " + finalSStackTrace).block();
                logger.info("sent DM");
                return Mono.empty();
            }).block();
        } catch (Throwable e2) {

            logger.error("Exception", e2);
        }
    }
}
