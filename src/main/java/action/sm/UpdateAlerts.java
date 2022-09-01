package action.sm;

import action.Action;
import bot.Sauce;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.MessageData;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateAlerts extends Action {

    String tacoBot = "490707751832649738";
    List<String> smChannelList;
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    public UpdateAlerts() {
        param = "cySmDrop";
        smChannelList = Arrays.asList(config.get("smChannelList").split(","));
    }


    @Override
    public Mono<Object> doAction(Message message) {
        return doAction(message, true);
    }


    public Mono<Object> doAction(Message message, boolean checkEmbeds) {
        AtomicBoolean watched = new AtomicBoolean(false);
        try {
            smChannelList.forEach(channel -> {
                if (message.getChannelId().asString().equals(channel)) {
                    watched.set(true);
                }
            });
            if (watched.get()) {
                if (message.getData().author().id().asString().equals(tacoBot)) {
                    //for some reason the embeds will be empty from slash, but if i load it again it will have data
                    if (checkAge(message)) {
                        checkMessageAgain(message);
                    } else {
                        for (Embed embed : message.getEmbeds()) {

                            if (embed.getAuthor().isPresent()) {
                                EmbedAuthorData authorData = embed.getAuthor().get().getData();

                                if (authorData.name().get().startsWith("Your Sauces")) {
                                    String id = getId(message);
                                    if (id == null) {
                                        if (authorData.iconUrl().isAbsent()) {
                                            message.getChannel().block().createMessage("Sorry unable to update your sauces. If you add an avatar i will be able to update them").block();
                                        } else {
                                            id = authorData.iconUrl().get().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];
                                        }
                                    }
                                    String line = embed.getDescription().get();
                                    List<Sauce> sauces = new ArrayList<>();

                                    for (Sauce sauce : Sauce.values()) {
                                        if (line.toLowerCase().contains(sauce.getName())) {
                                            sauces.add(sauce);
                                        }
                                    }
                                    logger.info("Updating alerts for " + id + " for " + line);

                                    Utils.addAlerts(id, sauces, message.getChannelId().asLong()+"");
                                    if (sauces.isEmpty()) {
                                        message.getChannel().block().createMessage("Alerts cleared").block();
                                    } else {
                                        StringBuilder sb = new StringBuilder("Updated alerts");
                                        for (Sauce sauce : sauces) {
                                            sb.append("\r\n - " + sauce);
                                        }
                                        message.getChannel().block().createMessage(sb.toString()).block();
                                    }
                                }
                            } else {
                                if (embed.getDescription().isPresent() && embed.getDescription().get().contains("You do not own any sauces!")) {
                                    clearSauces(message);
                                }
                            }


                        }
                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }


    private void clearSauces(Message message) {
        AtomicReference<String> userId = new AtomicReference<>("");
        userId.set(getId(message));

        if (userId.get().equals("")) {
            List<MessageData> historic = getMessagesOfChannel(message);
            historic.forEach(messageData -> {
                Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                if (Timestamp.from(messageDataTime).before(Timestamp.from(message.getTimestamp()))) {
                    String noSpace = messageData.content().toLowerCase().replace(" ", "");
                    if (noSpace.contains("!smlist")) {
                        userId.set(messageData.author().id().toString());
                    }
                }
            });
        }
        List<Sauce> sauces = new ArrayList<>();
        Utils.addAlerts(userId.get(), sauces, message.getChannelId().asString());
        message.getChannel().block().createMessage("Alerts cleared").block();

    }

    public static List<MessageData> getMessagesOfChannel(Message message) {

        Snowflake time = Snowflake.of(message.getTimestamp().minus(15, ChronoUnit.SECONDS));
        return message.getRestChannel().getMessagesAfter(time).collectList().block();
    }


//    @Override
//    public void checkMessageAgain(Message message) {
//
//        Runnable taskWrapper = new Runnable() {
//
//            @Override
//            public void run() {
//                logger.info("checking message again");
//                Message msg = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), Snowflake.of(message.getId().asString())).block();
//                doAction(msg, false);
//            }
//
//        };
//        logger.info("checking message again in 2 sec");
//        executorService.schedule(taskWrapper, 2, TimeUnit.SECONDS);
//    }

}
