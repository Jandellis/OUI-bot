package bot;

import action.Kicked;
import action.Welcome;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.AttachmentData;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

        while (true) {
            try {

                Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
                    // ReadyEvent example
                    Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, event ->
                                    Mono.fromRunnable(() -> {
                                        final User self = event.getSelf();
                                        System.out.printf("Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
                                    }))
                            .then();

                    Mono<Void> handlePingCommand = gateway.on(MessageCreateEvent.class, event -> {
                        Message message = event.getMessage();

                        if (message.getContent().equalsIgnoreCase("!ping")) {

                            long time = Duration.between(event.getMessage().getTimestamp(), Instant.now()).toMillis();
                            System.out.println("got ping " + time);
                            return message.getChannel()
                                    .flatMap(channel -> channel.createMessage("pong! Latency " + time + "ms"));
                        }

                        return Mono.empty();
                    }).then();

                    // combine them!
                    return printOnLogin.and(handlePingCommand)
                            .and(input(gateway))
                            .and(gift(gateway))
                            .and(report(gateway))
                            .and(warn(gateway, client))
                            .and(hit(gateway, client))
                            .and(new Kicked().action(gateway, client))
                            .and(giveaway(gateway))
                            .and(giveawayMembers(gateway, client))
                            .and(giveawayTotal(gateway))
                            .and(new Welcome().action(gateway, client));

                });

                login.block();

                Thread.sleep(30000);
            } catch (Throwable e) {
                e.printStackTrace();

            }
        }
    }


    private static Mono<Void> input(GatewayDiscordClient gateway) {
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "bbfimport";

            if (message.getContent().toLowerCase().startsWith(param)) {
                System.out.println(message.getContent());

                Snowflake messageId = Snowflake.of(message.getContent().toLowerCase().replaceAll(param + " ", ""));
                System.out.println("message id " + messageId);
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
        }).then();
    }

    private static Mono<Void> gift(GatewayDiscordClient gateway) {

        //work out who is in the giveaway list
       return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "oldgifts";

            if (message.getContent().startsWith(param)) {
                System.out.println(message.getContent());
                System.out.println(message.getContent().replaceAll(param + " ", ""));

                Snowflake messageId = Snowflake.of(message.getContent().replaceAll(param + " ", ""));
                System.out.println("message id " + messageId);
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

                        System.out.println(giftString);
                        channel.createMessage(giftString).block();
                    }

                    return channel.createMessage("done");
                });
            }

            return Mono.empty();
        }).then();
    }

    private static Mono<Void> report(GatewayDiscordClient gateway) {

        //create various report
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "ouireport";

            if (message.getContent().startsWith(param)) {
                System.out.println(message.getContent());
                System.out.println(message.getContent().replaceAll(param + " ", ""));

                String action = message.getContent().replaceAll(param + " ", "");
                System.out.println("action  " + action);
                int worklimit = 5;
                int uncleanlimit = 7;

                return message.getChannel().flatMap(channel -> {

                    List<KickMember> kickMemberList = new ArrayList<>();
                    try {
                        kickMemberList = Clean.mainNoImport("historic.csv");
                        System.out.println("processed data");
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
                        System.out.println(workList);

                        channel.createMessage(uncleanList.toString()).block();
                        System.out.println(uncleanList);
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

            return Mono.empty();
        }).then();
    }

    private static Mono<Void> warn(GatewayDiscordClient gateway, DiscordClient client) {

        //warn users in court and up the warning level
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "ouiwarn";


            if (message.getContent().startsWith(param)) {
                System.out.println(message.getContent());
                System.out.println(message.getContent().replaceAll(param + " ", ""));

                String action = message.getContent().replaceAll(param + " ", "");
                System.out.println("action " + action);

                return message.getChannel().flatMap(channel -> {

                    List<KickMember> kickMemberList = new ArrayList<>();
                    try {
                        kickMemberList = Clean.mainNoImport("historic.csv");
                        System.out.println("processed data");
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
                            System.out.println("user left the server " + kickMember.id);

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
                    System.out.println(workList);

                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(uncleanList.toString()).block();
                    System.out.println(uncleanList);


                    return channel.createMessage("Done");
                });
            }

            return Mono.empty();
        }).then();
    }

    private static Mono<Void> hit(GatewayDiscordClient gateway, DiscordClient client) {

        //create hit list
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "ouihit";


            if (message.getContent().startsWith(param)) {
                System.out.println(message.getContent());
                System.out.println(message.getContent().replaceAll(param + " ", ""));

                String action = message.getContent().replaceAll(param + " ", "");
                System.out.println("action " + action);

                return message.getChannel().flatMap(channel -> {

                    List<KickMember> kickMemberList = new ArrayList<>();
                    List<KickMember> exMembers = new ArrayList<>();
                    try {
                        kickMemberList = Clean.mainNoImport("historic.csv");
                        exMembers = Clean.kickedMembers();
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
                            if (ex.id == kickMember.id) {
                                kicked = true;
                                break;
                            }
                        }
                        if (kicked) {
                            continue;
                        }

                        MemberData memberData = null;
                        try {

                            System.out.println("checking member " + kickMember.id);
                            memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(kickMember.id)).getData().block();

                            memberData.roles().forEach(id -> {
                                if (id.asLong() == finalWarning)
                                    serverMembers.add(kickMember);
                            });

                        } catch (ClientException e) {
                            //user left the server
                            System.out.println("user left the server " + kickMember.id);
                            nonServerMembers.add(kickMember);
                        }
                    }
                    serverMembers.sort((o1, o2) -> {
                        if (o1.daysUnhappy == o2.daysUnhappy)
                            return 0;
                        if (o1.daysUnhappy < o2.daysUnhappy)
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
                            if (kickMember.daysNoWork > 4 || kickMember.daysUnhappy > 4) {
//                                            channel.createMessage(kickMember.id.toString()).block();
                                client.getChannelById(Snowflake.of(hitThread)).createMessage(kickMember.id.toString()).block();
                                count++;
                            }
                        }
                    }
                    for (KickMember kickMember : serverMembers) {
                        if (count < 10) {
                            client.getChannelById(Snowflake.of(hitThread)).createMessage(kickMember.id.toString()).block();
                            count++;
                        }
                    }


                    return channel.createMessage("Done");
                });
            }

            return Mono.empty();
        }).then();

    }

    private static Mono<Void> giveaway(GatewayDiscordClient gateway) {

        //work out how much people got in
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();

            if (message.getChannelId().asString().equals(giveawayChannel)) {
                if (message.getAuthor().get().getId().asString().equals(tacoBot)) {

                    for (Embed embed : message.getEmbeds()) {

                        String line = embed.getDescription().get();

                        if (line.contains(" You have sent a gift of `$")) {

                            String amount = line.replace(" You have sent a gift of `", "");
                            int index = amount.indexOf("$");
                            amount = amount.substring(index + 1);
                            amount = amount.replace(",", "");
                            String[] split = amount.split("` to ");
                            amount = split[0];
                            try {
                                Clean.addGift(Integer.parseInt(amount));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                        System.out.println(embed.getData());
                    }

                }
            }

            return Mono.empty();
        }).then();
    }

    private static Mono<Void> giveawayMembers(GatewayDiscordClient gateway, DiscordClient client) {

        //work out who is in the giveaway list
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            String param = "gifts";

            if (message.getContent().toLowerCase().startsWith(param)) {
                System.out.println(message.getContent());
                System.out.println(message.getContent().replaceAll(param + " ", ""));
                String action = message.getContent().replaceAll(param + " ", "");

                if (action.equals("reset")) {
                    try {
                        GiftAway.reset();
                        return message.getChannel().flatMap(channel -> channel.createMessage("reset"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (action.equals("export")) {
                    return message.getChannel().flatMap(channel -> {
                        List<String> gifts = new ArrayList<>();
                        try {
                            int worklimit = 50;
                            int votelimit = 7;
                            int otlimit = 30;
                            gifts = GiftAway.main(null, null, null, worklimit, otlimit, votelimit);
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

                        return Mono.empty();
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
            }
            return Mono.empty();
        }).then();
    }
    private static Mono<Void> giveawayTotal(GatewayDiscordClient gateway) {
        //work out how much people got in
        return gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();

            if (message.getChannelId().asString().equals("876714404819918918")) {
                if (message.getAuthor().get().getId().asString().equals("530082442967646230")) {
                    if (message.getContent().startsWith("^<@&875881574409859163>")){
                        int total = 0;
                        try {
                            total = Clean.getGift();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int finalTotal = total;
                        String responseMessage = "The total of the last giveaway was $" + String.format("%,d", finalTotal) +
                                "\r\nIf you would like to help increase this ask a recruiter to become a gifter today!";

                        return message.getChannel().flatMap(channel -> channel.createMessage(responseMessage));
                    }
                }
            }

            return Mono.empty();
        }).then();
    }


}