package action.export;

import action.Action;
import action.GiveAWay;
import action.Warn;
import action.export.model.Donations;
import action.export.model.GiveawayData;
import action.export.model.WarningData;
import action.export.model.WeeklyBestData;
import action.reminder.DoReminder;
import action.reminder.ReminderUtils;
import action.reminder.model.Reminder;
import action.reminder.ReminderType;
import bot.Clean;
import bot.KickList;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.Id;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Import extends Action {


    String guildId;
    Long finalWarning;
    String hitThread;
    String flex;
    Long recruiter;
    Long immunityId;
    String giveawayRole;
    String zeroVotes;
    boolean skipWarnings;

    public Import() {
        param = "ouiimport";
        guildId = config.get("guildId");

        finalWarning = Long.parseLong(config.get("finalWarning"));
        hitThread = config.get("hitThread");
        flex = config.get("flex");
        recruiter = Long.parseLong(config.get("recruiter"));
        immunityId = Long.parseLong(config.get("immunityId"));
        skipWarnings = Boolean.getBoolean(config.get("skipWarnings", "false"));
        giveawayRole = config.get("giveawayRole");
        zeroVotes = "1102390411252744312";
    }

    @Override
    public Mono<Object> doAction(Message message) {
        String actionData = getAction(message);
        if (actionData != null && hasPermission(message, recruiter)) {
            Snowflake messageId = Snowflake.of(actionData);
            int worklimit = 5;
            int uncleanlimit = 7;

            return message.getChannel().flatMap(channel -> {

                channel.createMessage("Starting import").block();

                Message data = channel.getMessageById(messageId).block();

                //download file
                String url = data.getData().attachments().get(0).url();
                KickList kickList = new KickList();
                try {
                    kickList = Clean.main(url, "historic.csv", worklimit, uncleanlimit, Timestamp.from(data.getTimestamp()));
                    logger.info("processed data");
                    HashMap<Long, List<ExportData>> history = ExportUtils.loadMemberHistory();
                    List<WeeklyBestData> work = new ArrayList<>();
                    List<WeeklyBestData> tips = new ArrayList<>();
                    List<WeeklyBestData> donations = new ArrayList<>();
                    List<WeeklyBestData> votes = new ArrayList<>();
                    List<WeeklyBestData> overtime = new ArrayList<>();
                    List<GiveawayData> giveawayData = new ArrayList<>();

                    history.forEach((id, dataList) -> {
                        //get old entry
                        int startWork = dataList.get(0).getMember().getShifts();
                        int startTips = dataList.get(0).getMember().getTips();
                        long startDonations = dataList.get(0).getMember().getDonations();
                        int startOvertime = dataList.get(0).getMember().getOvertime();
                        int startVotes = dataList.get(0).getMember().getVotes();

                        //get current entry
                        int endWork = dataList.get(dataList.size() - 1).getMember().getShifts();
                        int endTips = dataList.get(dataList.size() - 1).getMember().getTips();
                        long endDonations = dataList.get(dataList.size() - 1).getMember().getDonations();
                        int endOvertime = dataList.get(dataList.size() - 1).getMember().getOvertime();
                        int endVotes = dataList.get(dataList.size() - 1).getMember().getVotes();

                        work.add(new WeeklyBestData(id, endWork - startWork));
                        tips.add(new WeeklyBestData(id, endTips - startTips));
                        donations.add(new WeeklyBestData(id, endDonations - startDonations));
                        overtime.add(new WeeklyBestData(id, endOvertime - startOvertime));
                        votes.add(new WeeklyBestData(id, endVotes - startVotes));
                        giveawayData.add(new GiveawayData(id, endVotes - startVotes, endOvertime - startOvertime, endWork - startWork));

                    });

                    HashMap<Long, List<ExportData>> historyYesterday = ExportUtils.loadMemberHistoryYesterday();
                    HashMap<Long, WeeklyBestData> workYesterday = new HashMap<>();
                    HashMap<Long, WeeklyBestData> tipsYesterday = new HashMap<>();
                    HashMap<Long, WeeklyBestData> donationsYesterday = new HashMap<>();
                    HashMap<Long, WeeklyBestData> overtimeYesterday = new HashMap<>();
                    HashMap<Long, WeeklyBestData> votesYesterday = new HashMap<>();

                    historyYesterday.forEach((id, dataList) -> {
                        //get old entry
                        int startWork = dataList.get(0).getMember().getShifts();
                        int startTips = dataList.get(0).getMember().getTips();
                        long startDonations = dataList.get(0).getMember().getDonations();
                        long startOvertime = dataList.get(0).getMember().getOvertime();
                        long startVotes = dataList.get(0).getMember().getVotes();

                        //get current entry
                        int endWork = dataList.get(dataList.size() - 1).getMember().getShifts();
                        int endTips = dataList.get(dataList.size() - 1).getMember().getTips();
                        long endDonations = dataList.get(dataList.size() - 1).getMember().getDonations();
                        long endOvertime = dataList.get(dataList.size() - 1).getMember().getOvertime();
                        long endVotes = dataList.get(dataList.size() - 1).getMember().getVotes();

                        workYesterday.put(id, new WeeklyBestData(id, endWork - startWork));
                        tipsYesterday.put(id, new WeeklyBestData(id, endTips - startTips));
                        donationsYesterday.put(id, new WeeklyBestData(id, endDonations - startDonations));
                        overtimeYesterday.put(id, new WeeklyBestData(id, endOvertime - startOvertime));
                        votesYesterday.put(id, new WeeklyBestData(id, endVotes - startVotes));

                    });

                    WeeklyBestData.sort(work);
                    WeeklyBestData.sort(tips);
                    WeeklyBestData.sort(donations);
                    WeeklyBestData.sort(overtime);
                    WeeklyBestData.sort(votes);
                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title("OUI Weekly Best");
                    embed.addField("Top Work", getBest(work, workYesterday, false), true);
                    embed.addField("Top Tips", getBest(tips, tipsYesterday,false), false);
                    embed.addField("Top Donations", getBest(donations, donationsYesterday,true), true);


                    EmbedCreateSpec.Builder embed2 = EmbedCreateSpec.builder();
                    embed2.color(Color.SUMMER_SKY);
                    embed2.title("OUI Weekly Best");
                    embed2.addField("Top Overtime", getBest(overtime, overtimeYesterday, false), true);
                    embed2.addField("Top Votes", getBest(votes, votesYesterday,false), false);


                    client.getChannelById(Snowflake.of(flex)).createMessage(embed.build().asRequest()).block();

                    channel.createMessage(embed.build()).block();

                    client.getChannelById(Snowflake.of(flex)).createMessage(embed2.build().asRequest()).block();

                    channel.createMessage(embed2.build()).block();

                    channel.createMessage("Checking Roles").block();

                    checkRoles(history);

                    channel.createMessage("Doing Warnings").block();
                    //be able to skip warning if in downtime like over xmas
                    //maybe add ouiadmin, list all the admin commands


                    if (!skipWarnings) {
                        Warn warn = new Warn();
                        warn.action(gateway, client);
                        warn.doWarnings(5, 300, true);
                    }

                    channel.createMessage("Removing Mercy").block();
                    //load all users with mercy date before now
                    //set date to null
                    //remove role
                    for (WarningData warningData : ExportUtils.loadWarningDataAfterImmunity()) {
                        try {
                        client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                Snowflake.of(warningData.getName()),
                                Snowflake.of(immunityId),
                                "Mercy has expired").block();

                        } catch (ClientException e) {
                            //member left the server
                            logger.info("user left the server " + warningData.getName());

                        }
                        warningData.setImmunityUntil(null);
                        ExportUtils.updateWarningData(warningData);
                    }

                    channel.createMessage("Adding Giveaway").block();
                    //go though everyone and check if they meet the requirements for the giveaway
                    //set the date they will lose the role

                    for (GiveawayData member : giveawayData) {

                        //loop though all
                        //if match give role
                        int match = 0;
                        if (member.getOvertime() >= 30) {
                            match++;
                        }
                        if (member.getWork() >= 50) {
                            match++;
                        }
                        if (member.getVotes() >= 7) {
                            match++;
                        }
                        if (match >= 2) {

                            try {
                                client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                        Snowflake.of(member.getId()),
                                        Snowflake.of(giveawayRole),
                                        "Add Giveaway role").block();

                            } catch (ClientException e) {
                                //member left the server
                                logger.info("user left the server " + member.getId());

                            }
                            LocalDateTime now = LocalDateTime.now().plusDays(7);
                            ExportUtils.updateWarningData(member.getId() + "", Timestamp.valueOf(now));
                        }
                    }

                    channel.createMessage("Removing Giveaway").block();

                    for (WarningData warningData : ExportUtils.loadWarningDataAfterGiveaway()) {
                        try {
                            client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                    Snowflake.of(warningData.getName()),
                                    Snowflake.of(giveawayRole),
                                    "Giveaway role has expired").block();

                        } catch (ClientException e) {
                            //member left the server
                            logger.info("user left the server " + warningData.getName());

                        }
                        warningData.setGiveawayUntil(null);
                        ExportUtils.updateWarningData(warningData);
                    }

                    channel.createMessage("Doing vote check").block();

                    for (GiveawayData member : giveawayData) {

                        try {
                        if (member.getVotes() == 0) {
                            //give user the 0 vote roles
                            client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                    Snowflake.of(member.getId()),
                                    Snowflake.of(zeroVotes),
                                    "Add Giveaway role").block();
                        } else {
                            //remove the 0 vote role
                            client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                    Snowflake.of(member.getId()),
                                    Snowflake.of(zeroVotes),
                                    "Giveaway role has expired").block();
                        }

                        } catch (ClientException e) {
                            //member left the server
                            logger.info("user left the server " + member.getId());

                        }
                    }
                    channel.createMessage("Vote check done").block();


                    Instant reminderTime = message.getTimestamp().plus(23, ChronoUnit.HOURS);

                    Reminder reminder = ReminderUtils.addReminder(message.getAuthor().get().getId().asString(), ReminderType.importData, Timestamp.from(reminderTime), message.getChannelId().asString());
                    DoReminder doReminder = new DoReminder(gateway, client);
                    doReminder.runReminder(reminder);

                } catch (Exception e) {
                    printException(e);
                }


                return channel.createMessage("Imported Data");
            });
        }

        return Mono.empty();
    }

    private void checkRoles(HashMap<Long, List<ExportData>> history) {
        List<Donations> donations = ExportUtils.loadDonations();
        history.forEach((id, dataList) -> {
            //get current donation amount
            long amount = dataList.get(dataList.size()-1).getMember().getDonations();
            logger.info("checking  id " + id + " has " + amount);
            try {
                List<Id> roles = client.getGuildById(Snowflake.of(guildId)).getMember(Snowflake.of(id)).block().roles();
                donations.forEach((donate) -> {
                    AtomicBoolean hasRole = new AtomicBoolean(false);
                    roles.forEach((roleId) -> {
                        if (roleId.asLong() == Long.parseLong(donate.getRole())) {
                            hasRole.set(true);
                        }
                    });
                    if (donate.getMaxDonation() >= amount && donate.getMinDonation() <= amount) {
                        if (!hasRole.get()) {
                            client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                    Snowflake.of(id),
                                    Snowflake.of(donate.getRole()),
                                    "donation role").block();
                            logger.info("giving user role " + donate.getRole());
                        }
                    } else {
                        if (hasRole.get()) {
                            client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                    Snowflake.of(id),
                                    Snowflake.of(donate.getRole()),
                                    "donation role").block();
                        }
                    }

                });
            } catch (ClientException e) {
                //member left the server
                logger.info("user left the server " + id);
            }
        });
    }

    private String getBest(List<WeeklyBestData> weeklyBestData, HashMap<Long, WeeklyBestData> weeklyBestDataYesterday,  boolean money) {

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (WeeklyBestData data : weeklyBestData) {
            if (count == 10) {
                break;
            }
            count++;

            String value = "";
            if (money) {
                value = "$";
            }
            value = value + String.format("%,d", data.getValue());
//            builder.addField(count + start, "**"+ count + "**<@"+ data.getId() +"> - " + value, false);
            long change = data.getValue() - weeklyBestDataYesterday.get(data.getId()).getValue();

            //<a:reddown:1015028786292592701>
            //<a:greenup:1015028862368878723>
            String emote = " <a:greenup:1015028862368878723> ";
            if (change < 0) {
                emote = " <a:reddown:1015028786292592701> ";
            }
            if (change == 0){
                emote = " <a:orange_dots:1015118419047235585> ";
            }

            sb.append("**" + count + "** - <@" + data.getId() + "> - " + value + emote + String.format("%,d", change) +" \r\n");
        }
        return sb.toString();
    }

    private void saveIds(String header, HashMap<Long, String> ids) throws IOException {
        BufferedWriter bwKick = new BufferedWriter(new FileWriter("hitlist.txt"));
        bwKick.write("," + header);
        for (Map.Entry<Long, String> entry : ids.entrySet()) {
            Long memberId = entry.getKey();
            String messageId = entry.getValue();
            bwKick.newLine();
            bwKick.write(memberId + "," + messageId);
        }
        bwKick.close();
    }

    private void deleteOldMessages() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("hitlist.txt")));
        String line;
        while ((line = br.readLine()) != null) {
            try {
                client.getChannelById(Snowflake.of(hitThread)).message(Snowflake.of(line.split(",")[1])).delete("old Message").block();
            } catch (Exception e) {
                logger.info("message already deleted");

            }
        }
        br.close();
    }
}
