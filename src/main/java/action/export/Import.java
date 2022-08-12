package action.export;

import action.Action;
import action.reminder.DoReminder;
import action.reminder.Reminder;
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

    public Import() {
        param = "ouiimport";
        guildId = config.get("guildId");

        finalWarning = Long.parseLong(config.get("finalWarning"));
        hitThread = config.get("hitThread");
        flex = config.get("flex");
        recruiter = Long.parseLong(config.get("recruiter"));
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
                    HashMap<Long, List<ExportData>> history = Utils.loadMemberHistory();
                    List<WeeklyBestData> work = new ArrayList<>();
                    List<WeeklyBestData> tips = new ArrayList<>();
                    List<WeeklyBestData> donations = new ArrayList<>();

                    history.forEach((id, dataList) -> {
                        //get old entry
                        int startWork = dataList.get(0).getMember().getShifts();
                        int startTips = dataList.get(0).getMember().getTips();
                        long startDonations = dataList.get(0).getMember().getDonations();

                        //get current entry
                        int endWork = dataList.get(dataList.size() - 1).getMember().getShifts();
                        int endTips = dataList.get(dataList.size() - 1).getMember().getTips();
                        long endDonations = dataList.get(dataList.size() - 1).getMember().getDonations();

                        work.add(new WeeklyBestData(id, endWork - startWork));
                        tips.add(new WeeklyBestData(id, endTips - startTips));
                        donations.add(new WeeklyBestData(id, endDonations - startDonations));

                    });

                    WeeklyBestData.sort(work);
                    WeeklyBestData.sort(tips);
                    WeeklyBestData.sort(donations);
                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title("OUI Weekly Best");
                    embed.addField("Top Work", getBest(work, false), true);
                    embed.addField("Top Tips", getBest(tips, false), false);
                    embed.addField("Top Donations", getBest(donations, true), true);


                    client.getChannelById(Snowflake.of(flex)).createMessage(embed.build().asRequest()).block();

                    channel.createMessage(embed.build()).block();

                    channel.createMessage("Checking Roles").block();

                    checkRoles(history);

                    Instant reminderTime = message.getTimestamp().plus(23, ChronoUnit.HOURS);

                    Reminder reminder = action.reminder.Utils.addReminder(message.getAuthor().get().getId().asString(), ReminderType.importData, Timestamp.from(reminderTime), message.getChannelId().asString());
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
        List<Donations> donations = Utils.loadDonations();
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

    private String getBest(List<WeeklyBestData> weeklyBestData, boolean money) {

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
            sb.append("**" + count + "** - <@" + data.getId() + "> - " + value + "\r\n");
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
