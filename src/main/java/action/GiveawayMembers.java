package action;

import bot.GiftAway;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GiveawayMembers extends Action {

    String guildId;
    String giveawayRole;
    Long recruiter;
    public GiveawayMembers() {
        guildId = config.get("guildId");
        giveawayRole = config.get("giveawayRole");
        param = "gifts";
        recruiter = Long.parseLong(config.get("recruiter"));
    }

    @Override
    protected Mono<Object> doAction(Message message) {

        String action = getAction(message);
        if (action == null || !hasPermission(message, recruiter)) {
            return Mono.empty();
        }

        if (action.equals("reset")) {
            try {
                GiftAway.reset();
                return message.getChannel().flatMap(channel -> channel.createMessage("reset"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (action.equals("export")) {
            return message.getChannel().flatMap(channel -> {

//                System.out.println("Removed giveaway role for all members");
//                gateway.getGuildMembers(Snowflake.of(guildId)).flatMap(member -> {
//                    try {
//                        member.removeRole(Snowflake.of(giveawayRole));
//                        System.out.println("Removed giveaway role for " + member.getUsername());
//                    } catch (Exception e) {
//
//                    }
//                    return Mono.empty();
//                }).then();
                List<String> gifts = new ArrayList<>();
                try {
                    int worklimit = 50;
                    int votelimit = 7;
                    int otlimit = 30;
                    gifts = GiftAway.main(null, null, null, worklimit, otlimit, votelimit, guildId, giveawayRole, client);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                int count = 0;
                RestChannel giveawayThread = client.getChannelById(Snowflake.of("891902065566183484"));

                for (String giftString : gifts) {
                    sb.append(giftString);
                    count++;
                    if (count == 30) {

                        String output = sb.toString();
                        System.out.println(output);
                        giveawayThread.createMessage(output).block();
                        System.out.println("+++++++");
                        sb = new StringBuilder();
                        count = 0;
                    }
                }

                String output = sb.toString();
                System.out.println(output);
                giveawayThread.createMessage(output).block();
                System.out.println("Finished");

                return channel.createMessage("exported");
            });

        } else {

            Snowflake messageId = Snowflake.of(message.getContent().replaceAll(param + " ", ""));
            System.out.println("message id " + messageId);
            return message.getChannel().flatMap(channel -> {
                Message data = channel.getMessageById(messageId).block();
                for (Embed embed : data.getEmbeds()) {
                    String title = embed.getAuthor().get().getData().name().get();
                    String desc = embed.getDescription().get();

                    try {
                        if (title.contains("Shifts")) {
                            GiftAway.addData(desc, "work.csv");
                        }
                        if (title.contains("Votes")) {
                            GiftAway.addData(desc, "vote.csv");
                        }
                        if (title.contains("Overtimes")) {
                            GiftAway.addData(desc, "ot.csv");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(desc);
                }

                return channel.createMessage("Imported Data");
            });
        }

        return Mono.empty();
    }
}
