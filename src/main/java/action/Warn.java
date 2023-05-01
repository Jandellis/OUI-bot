package action;

import action.export.ExportUtils;
import action.export.model.MemberDonations;
import action.export.model.WarningData;
import bot.Clean;
import bot.KickMember;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Warn extends Action {


    String guildId;
    Long finalWarning;
    String warnChannel;

    Long immunityId;
    Long firstWarning;
    Long secondWarning;
    Long recruiter;

    public Warn() {
        param = "ouiwarn";
        guildId = config.get("guildId");

        warnChannel = config.get("warnChannel");
        immunityId = Long.parseLong(config.get("immunityId"));
        firstWarning = Long.parseLong(config.get("firstWarning"));
        secondWarning = Long.parseLong(config.get("secondWarning"));
        finalWarning = Long.parseLong(config.get("finalWarning"));
        recruiter = Long.parseLong(config.get("recruiter"));
    }

    @Override
    public Mono<Object> doAction(Message message) {

        //warn users in court and up the warning level
        String action = getAction(message);
        if (action != null && hasPermission(message, recruiter)) {

            return message.getChannel().flatMap(channel -> {
                try {
                    channel.createMessage("doing warnings...").block();
                    String messageContent = message.getContent();


                    int level = Integer.parseInt(action);
                    String maxString = getAction(messageContent, param, 1);
                    int max;
                    if (maxString.equals("")) {
                        max = 300;
                    } else {
                        max = Integer.parseInt(getAction(messageContent, param, 1));
                        ;//Integer.parseInt(action);
                    }


                    String oddString = getAction(messageContent, param, 2);
                    boolean odd;
                    if (oddString.equals("")) {
                        odd = false;
                    } else {
                        odd = Boolean.getBoolean(getAction(messageContent, param, 2));
                        ;//Integer.parseInt(action);
                    }

                    doWarnings(level, max, odd);


                } catch (Exception e) {
                    printException(e);
                    return channel.createMessage("Error");
                }


                return channel.createMessage("Done");
            });
        }

        return Mono.empty();
    }

    public void doWarnings(int level, int max, boolean odd) {

        try {

            List<KickMember> kickMemberList = new ArrayList<>();
            try {
                kickMemberList = Clean.mainNoImport("historic.csv");
                logger.info("processed data");
            } catch (Exception e) {
                e.printStackTrace();
            }



            List<String> workList = new ArrayList<>();
            workList.add("**__Please clean, vote, work & donate.__** \r\n");
            workList.add("__You have not done work in a while, start doing shifts otherwise you may be kicked__ \r\n");
            List<String> uncleanList = new ArrayList<>();
            uncleanList.add("**__Please clean, vote, work & donate.__** \r\n");
            uncleanList.add("__Your status is unclean, clean your shack otherwise you may be kicked__ \r\n");
            for (KickMember kickMember : kickMemberList) {
                boolean inServer = false;
                AtomicInteger warning = new AtomicInteger();
                AtomicBoolean imunity = new AtomicBoolean(false);
                MemberData memberData = null;
                try {
                    memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(kickMember.getId())).getData().block();

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
                    //member left the server
                    logger.info("user left the server " + kickMember.getId());

                }
                boolean warnMember = false;
                WarningData warningData = ExportUtils.loadWarningData(kickMember.getId().toString());
                LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(3);


                // hasnt worked in more than x days
                // hasnt worked in less than x days
                // is not on the imunity list
                // odd is false or number of days not worked is odd


                if (inServer
                        && kickMember.getDaysNoWork() >= level
                        && kickMember.getDaysNoWork() <= max
                        && !imunity.get()
                        && (warningData.getLastWarning() == null || warningData.getLastWarning().toLocalDateTime().isBefore(twoDaysAgo))) {
//                    workList.add("<@" + kickMember.getId() + "> \r\n");
                    warnMember = true;

                }

//                if (inServer
//                        && kickMember.getDaysUnhappy() >= level
//                        && kickMember.getDaysUnhappy() <= max
//                        && !imunity.get()
//                        && (warningData.getLastWarning() == null || warningData.getLastWarning().toLocalDateTime().isBefore(twoDaysAgo))) {
//                    uncleanList.add("<@" + kickMember.getId() + "> \r\n");
//                    warnMember = true;
//                }

                if (inServer && warnMember) {
                    if (warning.get() == 0) {
                        //set donations to 0
                        ExportUtils.resetMemberDonations(kickMember.getId().toString());
                        MemberDonations donations = ExportUtils.loadMemberDonations(kickMember.getId().toString());
                        if (donations.getDonation() >= 5000000) {
                            ExportUtils.clearWarning(kickMember.getId().toString());
                            warnMember = false;
                            logger.info("user should be warned, but they have donated to ingore the warning " + kickMember.getId());
                        } else {
                            client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                    Snowflake.of(kickMember.getId()),
                                    Snowflake.of(firstWarning),
                                    "first warning").block();
                        }
                    }
                    if (warning.get() == 1) {
                        client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                Snowflake.of(kickMember.getId()),
                                Snowflake.of(secondWarning),
                                "second warning").block();
                        client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                Snowflake.of(kickMember.getId()),
                                Snowflake.of(firstWarning),
                                "second warning").block();
                    }
                    if (warning.get() == 2) {
                        client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                Snowflake.of(kickMember.getId()),
                                Snowflake.of(finalWarning),
                                "final warning").block();
                        client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                Snowflake.of(kickMember.getId()),
                                Snowflake.of(secondWarning),
                                "final warning").block();
                    }
                    LocalDateTime now = LocalDateTime.now();
                    warningData.setLastWarning(Timestamp.valueOf(now));
                    ExportUtils.updateWarningData(warningData);
                }

                if (inServer && warnMember) {
                    workList.add("<@" + kickMember.getId() + "> \r\n");
                }
            }

            //print list of work warnings
            StringBuilder display = new StringBuilder();
            int count = 0;
            for (String line : workList) {
                count++;
                display.append(line);
                if (count == 30) {
                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(display.toString()).block();
                    logger.info(display);
                    display = new StringBuilder();
                    count = 0;
                }
            }
            if (count > 0) {
                client.getChannelById(Snowflake.of(warnChannel)).createMessage(display.toString()).block();
                logger.info(display);
                display = new StringBuilder();
                count = 0;
            }
//
//            //print list of unclean warnings
//            for (String line : uncleanList) {
//                count++;
//                display.append(line);
//                if (count == 30) {
//                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(display.toString()).block();
//                    logger.info(display);
//                    display = new StringBuilder();
//                    count = 0;
//                }
//            }
//            if (count > 0) {
//                client.getChannelById(Snowflake.of(warnChannel)).createMessage(display.toString()).block();
//                logger.info(display);
//            }


//
//                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(workList.toString()).block();
//                    logger.info(workList);

//                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(uncleanList.toString()).block();
//                    logger.info(uncleanList);

        } catch (Exception e) {
            printException(e);
        }

    }


    private double cos(double value) {
        double result = 1 - ((value * value) / (1 * 2)) +
                ((value * value * value * value) / (1 * 2 * 3 * 4)) -
                ((value * value * value * value * value * value) / (1 * 2 * 3 * 4 * 5 * 6));

        return result;
    }

    private double cos2(double value) {
        double result = 1;
        boolean subtract = true;
        for (int i = 0; i < 10; i++) {
            double partTop = 1;
            double partBottom = 1;
            for (int j = 0; j <= i * 2; j++) {
                partTop = partTop * value;
                partBottom = partBottom * j;
            }
            if (subtract) {
                result = result - (partTop / partBottom);
            } else {
                result = result + (partTop / partBottom);
            }
            subtract = !subtract;
        }
        return result;
    }
}
