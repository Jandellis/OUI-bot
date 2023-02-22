package action;

import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import action.sm.Utils;
import bot.Clean;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GiveAWay extends Action {


    String guildId;

    String giveawayChannel;
    String giveawayShower;
    String giveawayRole;
    long chefRole;

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    String react;
    Long recruiter;
    boolean openGiveaway;

    public GiveAWay() {
        param = "ouistartgift";
        guildId = config.get("guildId");
        giveawayChannel = config.get("giveawayChannelEvent");
        giveawayShower = config.get("giveawayChannel");
        giveawayRole = config.get("giveawayRole");
        react = "\uD83C\uDF89";
        recruiter = Long.parseLong(config.get("recruiter"));
        chefRole = Long.parseLong(config.get("chefRole"));
        openGiveaway = Boolean.getBoolean(config.get("openGiveaway", "false"));

    }

    @Override
    public Mono<Object> doAction(Message message) {
        try {


            String action = getAction(message);
            if (action == null || !hasPermission(message, recruiter)) {
                return Mono.empty();
            }
            create("");

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

    private void create(String winner) {


        Utils.deleteReminder(SystemReminderType.giveaway);
        LocalDateTime now = LocalDateTime.now();


        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        embed.color(Color.SUMMER_SKY);
        embed.title("Daily Giftaway");
        embed.description("React with "+react+" to enter the giftaway \r\n Must have the role:<@&" + giveawayRole + ">");

        MessageData msg = client.getChannelById(Snowflake.of(giveawayChannel)).createMessage(embed.build().asRequest()).block();

        Message message = gateway.getMessageById(Snowflake.of(giveawayChannel), Snowflake.of(msg.id().toString())).block();
        message.addReaction(ReactionEmoji.unicode(react)).block();


        LocalDateTime endTime = now.plusDays(1);
//        LocalDateTime endTime = now.plusMinutes(2);
        Utils.addReminder(SystemReminderType.giveaway, Timestamp.valueOf(endTime), msg.id().toString(), winner);

        runGiveAWay(ChronoUnit.MINUTES.between(now, endTime));

    }

    private void doGiveAWay() {
        //load system reminder
        //get message
        //work out who reacted
        // check they can all enter
        //caculate a random number
        //give it to that person
        //message that they won and commands to give them the gift
        //print out total for the last day
        //create new giveaway

        try {

            List<SystemReminder> rem = Utils.loadReminder(SystemReminderType.giveaway);
            printTotal(rem.get(0).getName());

            Message message = gateway.getMessageById(Snowflake.of(giveawayChannel), Snowflake.of(rem.get(0).getMessageId())).block();

            List<String> enteredList = new ArrayList<>();

            List<User> users = message.getReactors(ReactionEmoji.unicode(react)).collectList().block();

            users.forEach(user -> {
                try {
                    List<Id> roles = client.getGuildById(Snowflake.of(guildId)).getMember(Snowflake.of(user.getId().asString())).block().roles();
                    roles.forEach((roleId) -> {

                        long roleCheck= Long.parseLong(giveawayRole);
                        if (openGiveaway) {
                            roleCheck = chefRole;
                        }

                        if (roleId.asLong() == roleCheck) {
                            enteredList.add(user.getId().asString());
                        }
                    });
                } catch (Exception e) {
                    logger.info("assuming user has left the server " + user.getId().asString() + ", " + user.getUsername());
                }
            });

            Random rand = new Random();
            String winner = enteredList.get(rand.nextInt(enteredList.size()));

            String winnerMessage = "Congratulations to <@" + winner + ">" +
                    "\r\n <@&875881574409859163>\n" +
                    "\n" +
                    "<@&875880362482491422> to go <#875882488680034326> and use your gift";

            client.getChannelById(Snowflake.of(giveawayChannel)).createMessage(winnerMessage).block();
            client.getChannelById(Snowflake.of(giveawayShower)).createMessage("<@&875880362482491422> you can gift again, please gift the new giveaway winner. \n\n <@" + winner + "> won!!").block();
            client.getChannelById(Snowflake.of(giveawayChannel)).createMessage("`/gift member: <@" + winner + ">`").block();


            create(winner);
        } catch (Exception e) {
            printException(e);
        }

    }

    public void runGiveAWay(long delay) {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running give a way");
                doGiveAWay();
            }

        };

        LocalDateTime lockTime = LocalDateTime.now().plusMinutes(delay);
        logger.info("give a way at " + formatter.format(lockTime));

        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }


    //TODO: NEED TO CALL THIS

    public void startUp(){

        //if file is empty it will not run tasks
        LocalDateTime lockTime = LocalDateTime.now().minusMinutes(1);


        List<SystemReminder> lockReminder = Utils.loadReminder(SystemReminderType.giveaway);


        LocalDateTime localNow = LocalDateTime.now();


        if (!lockReminder.isEmpty()) {
            lockTime = lockReminder.get(0).getTime().toLocalDateTime();
            long delay = ChronoUnit.MINUTES.between(localNow, lockTime);
            runGiveAWay(delay);
        }


    }

    private void printTotal(String winner){
        int total = 0;
        try {
            total = Clean.getGift();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int finalTotal = total;
        String responseMessage = "<@"+winner+"> was gifted $" + String.format("%,d", finalTotal) + " in yesterdays last giveaway"+
                "\r\nIf you would like to help increase this ask a recruiter to become a gifter today!";

        //write message to chat
        client.getChannelById(Snowflake.of("840395542394568707")).createMessage(responseMessage).block();

        //write message to giveaways
        client.getChannelById(Snowflake.of(giveawayChannel)).createMessage(responseMessage).block();
    }
}
