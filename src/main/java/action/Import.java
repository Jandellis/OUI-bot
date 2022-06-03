package action;

import bot.Clean;
import bot.KickList;
import bot.KickMember;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Import extends Action {


    String guildId;
    Long finalWarning;
    String hitThread;
    Long recruiter;

    public Import() {
        param = "bbfimport";
        guildId = config.get("guildId");

        finalWarning = Long.parseLong(config.get("finalWarning"));
        hitThread = config.get("hitThread");
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
                Message data = channel.getMessageById(messageId).block();

                //download file
                String url = data.getData().attachments().get(0).url();
                KickList kickList = new KickList();
                try {
                    kickList = Clean.main(url, "historic.csv", worklimit, uncleanlimit);
                    System.out.println("processed data");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return channel.createMessage("Imported Data");
            });
        }

        return Mono.empty();
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
                System.out.println("message already deleted");

            }
        }
        br.close();
    }
}
