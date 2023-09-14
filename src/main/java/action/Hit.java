package action;

import bot.Clean;
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

public class Hit extends Action {


    String guildId;
    Long finalWarning;
    String hitThread;
    Long recruiter;

    public Hit() {
        param = "ouihit";
        guildId = config.get("guildId");

        finalWarning = Long.parseLong(config.get("finalWarning"));
        hitThread = config.get("hitThread");
        recruiter = Long.parseLong(config.get("recruiter"));
    }

    @Override
    public Mono<Object> doAction(Message message) {

        String action = getAction(message);
        String exclude = getAction(message, param, 1);
        if (action != null && hasPermission(message, recruiter)) {
            return message.getChannel().flatMap(channel -> {
                int hitListSize = 10;
                try {
                    hitListSize = Integer.parseInt(action);
                } catch (NumberFormatException e) {

                }


                channel.createMessage("creating hitlist...").block();
                try {
                    deleteOldMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                List<KickMember> kickMemberList = new ArrayList<>();
                List<KickMember> exMembers = new ArrayList<>();
                try {
                    kickMemberList = Clean.mainNoImport("OUIhistoric.csv");
                    //exMembers = Clean.kickedMembers();
                    logger.info("processed data");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                                /*
                                find members who left
                                find members who are on final warning and have most days unclean
                                 */
                    List<KickMember> serverMembers = new ArrayList<>();
                    List<KickMember> nonServerMembers = new ArrayList<>();
                    for (KickMember kickMember : kickMemberList) {
                        boolean kicked = false;
                        for (KickMember ex : exMembers) {
                            if (ex.getId() == kickMember.getId()) {
                                kicked = true;
                                break;
                            }
                        }
                        if (kicked) {
                            continue;
                        }

                        MemberData memberData = null;
                        try {

                            logger.info("checking member " + kickMember.getId());
                            memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(kickMember.getId())).getData().block();

                            memberData.roles().forEach(id -> {
                                if (id.asLong() == finalWarning)
                                    serverMembers.add(kickMember);
                            });

                            // remove members who have donated a lot
                            if (exclude!= null && exclude.equals("exclude")) {
                                memberData.roles().forEach(id -> {
                                    if (id.asLong() == 931281334570197022L ||
                                            id.asLong() == 931277539270344744L ||
                                            id.asLong() == 931276083326771220L ||
                                            id.asLong() == 931273576739405864L ||
                                            id.asLong() == 931269453063266375L)
                                        serverMembers.remove(kickMember);
                                });
                            }

                        } catch (ClientException e) {
                            //user left the server
                            logger.info("user left the server " + kickMember.getId());
                            nonServerMembers.add(kickMember);
                        }
                    }
                    serverMembers.sort((o1, o2) -> {
                        if (o1.getDaysUnhappy() == o2.getDaysUnhappy())
                            return 0;
                        if (o1.getDaysUnhappy() < o2.getDaysUnhappy())
                            return 1;
                        else
                            return -1;
                    });
                    //post to thread
                    StringBuilder hitlist = new StringBuilder();
                    hitlist.append("**Please check happiness and delete after kick** \r\n");
                    hitlist.append("**Kick from top down** \r\n");
                    String hitHeader = client.getChannelById(Snowflake.of(hitThread)).createMessage(hitlist.toString()).block().id().asString();

                    int count = 0;
                    HashMap<Long, String> hitMessages = new HashMap<>();

                    for (KickMember kickMember : nonServerMembers) {
                        if (count < hitListSize) {
                            if (kickMember.getDaysNoWork() > 4 || kickMember.getDaysNoWork() > 4) {
//                                            channel.createMessage(kickMember.id.toString()).block();
                                String messageId = client.getChannelById(Snowflake.of(hitThread)).createMessage(kickMember.getId().toString()).block().id().asString();
                                hitMessages.put(kickMember.getId(), messageId);
                                count++;
                            }
                        }
                    }
                    for (KickMember kickMember : serverMembers) {
                        if (count < hitListSize) {
                            String messageId = client.getChannelById(Snowflake.of(hitThread)).createMessage("<@" + kickMember.getId().toString() + ">").block().id().asString();
                            hitMessages.put(kickMember.getId(), messageId);
                            count++;
                        }
                    }
                    try {
                        saveIds(hitHeader, hitMessages);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    printException(e);
                }


                return channel.createMessage("Done");
            });
        }

        return Mono.empty();
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
