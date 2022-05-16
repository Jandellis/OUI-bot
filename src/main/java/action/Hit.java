package action;

import bot.Clean;
import bot.KickMember;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Hit extends Action {


    String guildId;
    Long finalWarning;
    String hitThread;

    public Hit() {
        param = "ouihit";
        guildId = config.get("guildId");

        finalWarning = Long.parseLong(config.get("finalWarning"));
        hitThread = config.get("hitThread");
    }

    @Override
    public Mono<Object> doAction(Message message) {

        String action = getAction(message);
        if (action != null) {
            return message.getChannel().flatMap(channel -> {

                List<KickMember> kickMemberList = new ArrayList<>();
                List<KickMember> exMembers = new ArrayList<>();
                try {
                    kickMemberList = Clean.mainNoImport("historic.csv");
                    //exMembers = Clean.kickedMembers();
                    System.out.println("processed data");
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

                        System.out.println("checking member " + kickMember.getId());
                        memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(kickMember.getId())).getData().block();

                        memberData.roles().forEach(id -> {
                            if (id.asLong() == finalWarning)
                                serverMembers.add(kickMember);
                        });

                    } catch (ClientException e) {
                        //user left the server
                        System.out.println("user left the server " + kickMember.getId());
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
                client.getChannelById(Snowflake.of(hitThread)).createMessage(hitlist.toString()).block();

                int count = 0;

                for (KickMember kickMember : nonServerMembers) {
                    if (count < 10) {
                        if (kickMember.getDaysNoWork() > 4 || kickMember.getDaysNoWork() > 4) {
//                                            channel.createMessage(kickMember.id.toString()).block();
                            client.getChannelById(Snowflake.of(hitThread)).createMessage(kickMember.getId().toString()).block();
                            count++;
                        }
                    }
                }
                for (KickMember kickMember : serverMembers) {
                    if (count < 10) {
                        client.getChannelById(Snowflake.of(hitThread)).createMessage("<@"+kickMember.getId().toString()+">").block();
                        count++;
                    }
                }


                return channel.createMessage("Done");
            });
        }

        return Mono.empty();
    }
}
