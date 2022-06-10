package bot;

import action.CheapSaucePing;
import action.GiveawayAdd;
import action.GiveawayMembers;
import action.GiveawayTotal;
import action.Hit;
import action.Import;
import action.Kicked;
import action.Left;
import action.SpeedJar;
import action.Test;
import action.Warn;
import action.Welcome;
import action.sm.AddAlert;
import action.sm.DoAlerts;
import action.sm.UpdateAlerts;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Presence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Bot {

    static String hitThread;
    static String warnChannel;
    static String guildId;
    static String giveawayChannel;
    static String tacoBot = "490707751832649738";
    static Long immunityId;
    static Long firstWarning;
    static Long secondWarning;
    static Long finalWarning;
    static long chefRole;
    private static final Logger logger = LogManager.getLogger("ouiBot");


    public static void main(String[] args) {
        DiscordClient client;
        Config config = Config.getInstance();

        //OUI-Bot-test
        client = DiscordClient.create(config.get("client"));
        warnChannel = config.get("warnChannel");
        hitThread = config.get("hitThread");
        guildId = config.get("guildId");
        giveawayChannel = config.get("giveawayChannel");
        immunityId = Long.parseLong(config.get("immunityId"));
        firstWarning = Long.parseLong(config.get("firstWarning"));//Red
        secondWarning = Long.parseLong(config.get("secondWarning"));//Purple
        finalWarning = Long.parseLong(config.get("finalWarning"));//Green
        chefRole = Long.parseLong(config.get("chefRole"));//Orange


        String url = config.get("url");
        String user = config.get("user");
        String password = config.get("password");

//        logger.debug("Debug log message");
//        logger.info("Info log message");
//        logger.error("Error log message");
//        logger.warn("Warn log message");
//        logger.fatal("Fatal log message");
//        logger.trace("Trace log message");

        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT VERSION()")) {

            if (rs.next()) {
                logger.info("Version -- " + rs.getString(1));
            }

        } catch (SQLException ex) {

        }



        while (true) {
            try {

                Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
                    // ReadyEvent example
                    Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, event ->
                                    Mono.fromRunnable(() -> {
                                        final User self = event.getSelf();
                                        logger.info("Logged in as " + self.getUsername() +" " + self.getDiscriminator());
                                        gateway.updatePresence(ClientPresence.online(ClientActivity.watching("ouiSM for sauce market help"))).subscribe();
                                    }))
                            .then();

                    SpeedJar speedJar = new SpeedJar();
                    speedJar.action(gateway, client);
                    try {
                        speedJar.startUp();
                    } catch (IOException e) {
                        logger.error("Exception", e);
                    }

                    Mono<Void> handlePingCommand = gateway.on(MessageCreateEvent.class, event -> {
                        Message message = event.getMessage();

                        if (message.getContent().equalsIgnoreCase("!ping")) {

                            long time = Duration.between(event.getMessage().getTimestamp(), Instant.now()).toMillis();
                            logger.info("got ping " + time);
                            return message.getChannel()
                                    .flatMap(channel -> channel.createMessage("pong! Latency " + time + "ms"));
                        }

                        return Mono.empty();
                    }).then();

                    // combine them!
                    return printOnLogin.and(handlePingCommand)
//                            .and(input(gateway))
//                            .and(gift(gateway))
                            .and(new Import().action(gateway,client))
                            .and(report(gateway))
                            .and(new Warn().action(gateway, client))
                            .and(new Hit().action(gateway, client))
                            .and(new Kicked().action(gateway, client))
                            .and(new GiveawayAdd().action(gateway, client))
                            .and(new GiveawayMembers().action(gateway, client))
                            .and(new GiveawayTotal().action(gateway, client))
                            .and(new Welcome().action(gateway, client))
                            .and(new CheapSaucePing().action(gateway, client))
                            .and(new SpeedJar().action(gateway, client))
                            .and(new AddAlert().action(gateway, client))
                            .and(new DoAlerts().action(gateway, client))
                            .and(new Test().action(gateway, client))
                            .and(new Left().action(gateway, client))
                            .and(new UpdateAlerts().action(gateway,client));

                });

                login.block();

                Thread.sleep(30000);
            } catch (Throwable e) {
                logger.error("Exception", e);

            }
        }
    }

/*
    private static Mono<Void> input(GatewayDiscordClient gateway) {
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "bbfimport";

            if (message.getContent().toLowerCase().startsWith(param)) {
                logger.info(message.getContent());

                Snowflake messageId = Snowflake.of(message.getContent().toLowerCase().replaceAll(param + " ", ""));
                logger.info("message id " + messageId);
                int worklimit = 5;
                int uncleanlimit = 7;

                return message.getChannel().flatMap(channel -> {
                    Message data = channel.getMessageById(messageId).block();

                    //download file
                    String url = data.getData().attachments().get(0).url();
                    KickList kickList = new KickList();
                    try {
                        kickList = Clean.main(url, "historic.csv", worklimit, uncleanlimit);
                        logger.info("processed data");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return channel.createMessage("Imported Data");
                });
            }

            return Mono.empty();
        }).then();
    }

    private static Mono<Void> gift(GatewayDiscordClient gateway) {

        //work out who is in the giveaway list
       return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "oldgifts";

            if (message.getContent().startsWith(param)) {
                logger.info(message.getContent());
                logger.info(message.getContent().replaceAll(param + " ", ""));

                Snowflake messageId = Snowflake.of(message.getContent().replaceAll(param + " ", ""));
                logger.info("message id " + messageId);
                int worklimit = 5;
                int votelimit = 7;
                int otlimit = 7;

                return message.getChannel().flatMap(channel -> {
                    Message data = channel.getMessageById(messageId).block();

                    //download file
                    String otUrl = "";
                    String workUrl = "";
                    String voteUrl = "";

                    for (AttachmentData attachmentData : data.getData().attachments()) {
                        if (attachmentData.filename().contains("ot")) {
                            otUrl = attachmentData.url();
                        }
                        if (attachmentData.filename().contains("work")) {
                            workUrl = attachmentData.url();
                        }
                        if (attachmentData.filename().contains("vote")) {
                            voteUrl = attachmentData.url();
                        }
                    }


                    List<String> gifts = new ArrayList<>();
                    try {
                        gifts = GiftAway.main(workUrl, otUrl, voteUrl, worklimit, otlimit, votelimit);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (String giftString : gifts) {

                        logger.info(giftString);
                        channel.createMessage(giftString).block();
                    }

                    return channel.createMessage("done");
                });
            }

            return Mono.empty();
        }).then();
    }
*/
    private static Mono<Void> report(GatewayDiscordClient gateway) {

        //create various report
        return gateway.on(MessageCreateEvent.class, event -> {
            try {
                Message message = event.getMessage();
                String param = "ouireport";

                if (message.getContent().startsWith(param)) {
                    logger.info(message.getContent());
                    logger.info(message.getContent().replaceAll(param + " ", ""));

                    String action = message.getContent().replaceAll(param + " ", "");
                    logger.info("action  " + action);
                    int worklimit = 5;
                    int uncleanlimit = 7;

                    return message.getChannel().flatMap(channel -> {

                        List<KickMember> kickMemberList = new ArrayList<>();
                        try {
                            kickMemberList = Clean.mainNoImport("historic.csv");
                            logger.info("processed data");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (action.equals("report")) {

                            StringBuilder workList = new StringBuilder();
                            workList.append("**No work for " + worklimit + " days** \r\n");
                            StringBuilder uncleanList = new StringBuilder();
                            uncleanList.append("**Unhappy for " + uncleanlimit + " days** \r\n");
                            for (KickMember kickMember : kickMemberList) {

                                if (kickMember.daysNoWork >= worklimit) {
                                    workList.append("<@" + kickMember.id + "> \r\n");
                                }

                                if (kickMember.daysUnhappy >= uncleanlimit) {
                                    uncleanList.append("<@" + kickMember.id + "> \r\n");
                                }
                            }
                            channel.createMessage(workList.toString()).block();
                            logger.info(workList);

                            channel.createMessage(uncleanList.toString()).block();
                            logger.info(uncleanList);
                        }
                        if (action.equals("clean")) {
                            StringBuilder list = new StringBuilder();
                            list.append("**Unhappy for days** \r\n");
                            int count = 0;
                            kickMemberList.sort((o1, o2) -> {
                                if (o1.daysUnhappy == o2.daysUnhappy)
                                    return 0;
                                if (o1.daysUnhappy < o2.daysUnhappy)
                                    return 1;
                                else
                                    return -1;
                            });
                            for (KickMember kickMember : kickMemberList) {
                                if (kickMember.daysUnhappy > 0) {
                                    count++;
                                    list.append("<@" + kickMember.id + "> - " + kickMember.daysUnhappy + "\r\n");
                                    if (count == 49) {
                                        channel.createMessage(list.toString()).block();
                                        list = new StringBuilder();
                                        count = 0;
                                    }
                                }
                            }
                            channel.createMessage(list.toString()).block();

                        }
                        if (action.equals("work")) {
                            StringBuilder list = new StringBuilder();
                            list.append("**No work for days** \r\n");
                            int count = 0;
                            kickMemberList.sort((o1, o2) -> {
                                if (o1.daysNoWork == o2.daysNoWork)
                                    return 0;
                                if (o1.daysNoWork < o2.daysNoWork)
                                    return 1;
                                else
                                    return -1;
                            });
                            for (KickMember kickMember : kickMemberList) {
                                if (kickMember.daysNoWork > 0) {
                                    count++;
                                    list.append("<@" + kickMember.id + "> - " + kickMember.daysNoWork + "\r\n");
                                    if (count == 49) {
                                        channel.createMessage(list.toString()).block();
                                        list = new StringBuilder();
                                        count = 0;
                                    }
                                }
                            }
                            channel.createMessage(list.toString()).block();
                        }


                        return channel.createMessage("Done");
                    });
                }
            } catch (Exception e) {
                logger.error("Exception", e);
            }

            return Mono.empty();
        }).then();
    }
/*
    private static Mono<Void> warn(GatewayDiscordClient gateway, DiscordClient client) {

        //warn users in court and up the warning level
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "ouiwarn";


            if (message.getContent().startsWith(param)) {
                logger.info(message.getContent());
                logger.info(message.getContent().replaceAll(param + " ", ""));

                String action = message.getContent().replaceAll(param + " ", "");
                logger.info("action " + action);

                return message.getChannel().flatMap(channel -> {

                    List<KickMember> kickMemberList = new ArrayList<>();
                    try {
                        kickMemberList = Clean.mainNoImport("historic.csv");
                        logger.info("processed data");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    int level = Integer.parseInt(action);


                    StringBuilder workList = new StringBuilder();
                    workList.append("**__Please clean, vote, work & donate.__** \r\n");
                    workList.append("__You have not done work in a while, start doing shifts otherwise you may be kicked__ \r\n");
                    StringBuilder uncleanList = new StringBuilder();
                    uncleanList.append("**__Please clean, vote, work & donate.__** \r\n");
                    uncleanList.append("__Your status is unclean, clean your shack otherwise you may be kicked__ \r\n");
                    for (KickMember kickMember : kickMemberList) {
                        boolean inServer = false;
                        AtomicInteger warning = new AtomicInteger();
                        AtomicBoolean imunity = new AtomicBoolean(false);
                        MemberData memberData = null;
                        try {
                            memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(kickMember.id)).getData().block();

                            memberData.roles().forEach(id -> {
                                if (id.asLong() == immunityId)
                                    imunity.set(true);
                                if (id.asLong() == firstWarning)
                                    warning.set(1);
                                if (id.asLong() == secondWarning)
                                    warning.set(2);
                                if (id.asLong() == finalWarning)
                                    warning.set(3);
                            });
                            inServer = true;
                        } catch (ClientException e) {
                            //most left the server
                            logger.info("user left the server " + kickMember.id);

                        }
                        boolean warnMember = false;
                        if (kickMember.daysNoWork >= level && !imunity.get()) {
                            workList.append("<@" + kickMember.id + "> \r\n");
                            warnMember = true;

                        }

                        if (kickMember.daysUnhappy >= level && !imunity.get()) {
                            uncleanList.append("<@" + kickMember.id + "> \r\n");
                            warnMember = true;
                        }

                        if (inServer && warnMember) {
                            if (warning.get() == 0) {
                                client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                        Snowflake.of(kickMember.id),
                                        Snowflake.of(firstWarning),
                                        "first warning").block();
                            }
                            if (warning.get() == 1) {
                                client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                        Snowflake.of(kickMember.id),
                                        Snowflake.of(secondWarning),
                                        "second warning").block();
                                client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                        Snowflake.of(kickMember.id),
                                        Snowflake.of(firstWarning),
                                        "second warning").block();
                            }
                            if (warning.get() == 2) {
                                client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                        Snowflake.of(kickMember.id),
                                        Snowflake.of(finalWarning),
                                        "final warning").block();
                                client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                        Snowflake.of(kickMember.id),
                                        Snowflake.of(secondWarning),
                                        "final warning").block();
                            }
                        }
                    }

                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(workList.toString()).block();
                    logger.info(workList);

                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(uncleanList.toString()).block();
                    logger.info(uncleanList);


                    return channel.createMessage("Done");
                });
            }

            return Mono.empty();
        }).then();
    }
*/
//    private static Mono<Void> hit(GatewayDiscordClient gateway, DiscordClient client) {
//
//        //create hit list
//        return gateway.on(MessageCreateEvent.class, event -> {
//            Message message = event.getMessage();
//        }).then();
//
//    }



}