package action.export;

import action.Action;
import bot.Clean;
import bot.KickList;
import bot.KickMember;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.MemberData;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Import extends Action {


    String guildId;
    Long finalWarning;
    String hitThread;
    String flex;
    Long recruiter;

    public Import() {
        param = "bbfimport";
        guildId = config.get("guildId");

        finalWarning = Long.parseLong(config.get("finalWarning"));
        hitThread = config.get("hitThread");
        flex = config.get("flex");
        recruiter = Long.parseLong(config.get("recruiter"));
    }

    @Override
    public Mono<Object> doAction(Message message) {
        String action = getAction(message);
        if (action != null && hasPermission(message, recruiter)) {
            Snowflake messageId = Snowflake.of(action);
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
                } catch (Exception e) {
                    printException(e);
                }
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
                    int endWork = dataList.get(dataList.size()-1).getMember().getShifts();
                    int endTips = dataList.get(dataList.size()-1).getMember().getTips();
                    long endDonations = dataList.get(dataList.size()-1).getMember().getDonations();

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

                return channel.createMessage("Imported Data");
            });
        }

        return Mono.empty();
    }

    private String getBest(List<WeeklyBestData> weeklyBestData, boolean money) {

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (WeeklyBestData data : weeklyBestData) {
            if (count == 10) {
                break;
            }
            count ++;

            String value = "";
            if (money) {
                value = "$";
            }
            value = value + String.format("%,d", data.getValue());
//            builder.addField(count + start, "**"+ count + "**<@"+ data.getId() +"> - " + value, false);
            sb.append("**"+ count + "** - <@"+ data.getId() +"> - " + value + "\r\n");
        }
        return sb.toString();
    }

    private void saveIds(String header, HashMap<Long, String> ids) throws IOException {
        BufferedWriter bwKick = new BufferedWriter(new FileWriter("hitlist.txt"));
        bwKick.write(","+header);
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
