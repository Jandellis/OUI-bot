package action.reminder;

import action.Action;
import action.FranchiseStat;
import action.GiveawayAdd;
import action.Karen;
import action.RushHour;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import action.sm.UpdateAlerts;
import action.upgrades.BuyUpgrade;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestChannel;
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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class EmbedMessage extends Action {

    String tacoBot = "490707751832649738";
    String customerBot = "526268502932455435";
    List<EmbedAction> embedActions = new ArrayList<>();


    private final ScheduledExecutorService executorService =
            Executors.newScheduledThreadPool(15, new ThreadFactory() {

                private final AtomicInteger threadNumber =
                        new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("EmbedMessage-" + threadNumber.getAndIncrement());
                    return thread;
                }
            });

    /**
     * This deals with all messages from tacoshack. They need to be dealt with a bit differently as they are embed messages.
     * When we first get the embed it is empty, so looking at it again a tiny bit in the future will fix this
     * @param gateway
     * @param client
     */
    public EmbedMessage(GatewayDiscordClient gateway, DiscordClient client) {

        CreateReminder createReminder = new CreateReminder();
        createReminder.action(gateway, client);
        embedActions.add(createReminder);

        FranchiseStat franchiseStat = new FranchiseStat();
        franchiseStat.action(gateway, client);
        embedActions.add(franchiseStat);

        GiveawayAdd giveawayAdd = new GiveawayAdd();
        giveawayAdd.action(gateway, client);
        embedActions.add(giveawayAdd);

        CreateBoostReminder createBoostReminder = new CreateBoostReminder();
        createBoostReminder.action(gateway, client);
        embedActions.add(createBoostReminder);

        CreateProfile createProfile = new CreateProfile();
        createProfile.action(gateway, client);
        embedActions.add(createProfile);

        UpdateAlerts updateAlerts = new UpdateAlerts();
        updateAlerts.action(gateway, client);
        embedActions.add(updateAlerts);

        BuyUpgrade buyUpgrade = new BuyUpgrade();
        buyUpgrade.action(gateway, client);
        embedActions.add(buyUpgrade);

        Karen karen = new Karen();
        karen.action(gateway, client);
        embedActions.add(karen);

        RushHour rushHour = new RushHour();
        rushHour.action(gateway, client);
        embedActions.add(rushHour);

    }


    @Override
    public Mono<Object> doAction(Message message) {

            if (message.getData().author().id().asString().equals(tacoBot) || message.getData().author().id().asString().equals(customerBot)) {
                try {

                    if (message.getChannelId().asLong() == 889662502324039690L){
                        logger.info("**************************12345 - got message " + message.toString());
                    }
                    List<EmbedData> embedData;
                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0){
                        logger.info("empty embeds");
                        // not sure why i had this check in place
//                        if (message.getData().interaction().toOptional().isPresent() && message.getData().interaction().get().name().equals("saucemarket buy")) {
//                            logger.info("Skipping message");
//                        } else {

//                        embedData = checkEmbeds(message);
                            if (message.getContent().isEmpty()) {
                                lookForEmbeds(message, 0);
                            } else {
                                logger.info("Message has content, will not check for embeds");
                            }
//                        }
                        return Mono.empty();

                    } else {

                        logger.info("non empty embeds");
                        embedData = message.getData().embeds();
                    }



                    for (EmbedAction embedAction : embedActions) {
                        embedAction.handleEmbedAction(message, embedData);
                    }
                } catch (Exception e) {
                    printException(e);
                }

            }


        return Mono.empty();
    }



    public Mono<Object> doEmbed(Message message, List<EmbedData> embedData) {

        if (message.getData().author().id().asString().equals(tacoBot)  || message.getData().author().id().asString().equals(customerBot)) {
            try {
                for (EmbedAction embedAction : embedActions) {
                    embedAction.handleEmbedAction(message, embedData);
                }
            } catch (Exception e) {
                printException(e);
            }

        }


        return Mono.empty();
    }

//    private void lookForEmbeds(Message message, int count) {
//
//        Runnable taskWrapper = new Runnable() {
//
//            @Override
//            public void run() {
////                logger.info("checking message again");
////                Message msg = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), Snowflake.of(message.getId().asString())).block();
////                doAction(msg);
//
//                List<EmbedData> embedData;
//                logger.info("checking for embeds try "+ count);
//                embedData = checkEmbeds(message);
//                if (embedData.isEmpty()){
//                    int newCount = count + 1;
//                    lookForEmbeds(message, newCount);
//
//                } else {
//                    logger.info("found embeds");
//                    doEmbed(message, embedData);
//                }
//            }
//
//        };
////        logger.info("checking message again in 1 sec");
//        if (count < 5) {
//            executorService.schedule(taskWrapper, 500, TimeUnit.MILLISECONDS);
//        } else {
//
//            logger.info("checking for embeds - hit "+ count);
//        }
//    }

    private void lookForEmbeds(Message message, int count) {
        if (count >= 5) {
            logger.info("giving up waiting for embeds after {} attempts", count);
            return;
        }

        // exponential backoff: 500ms, 1s, 2s, 4s, 8s, 16s, 32s, 64s
        long delayMs = (long) (500 * Math.pow(2, count));

        executorService.schedule(() -> {
            try {
                logger.info("checking for embeds attempt {} (delay was {}ms)", count + 1, delayMs);

                // force fresh fetch from Discord REST API, bypassing cache
                Message freshMessage = message.getClient()
                        .withRetrievalStrategy(EntityRetrievalStrategy.REST)
                        .getMessageById(message.getChannelId(), message.getId())
                        .block();

                if (freshMessage != null && !freshMessage.getData().embeds().isEmpty()) {
                    logger.info("found embeds on attempt {}", count + 1);

                    if (message.getChannelId().asLong() == 889662502324039690L){
                        logger.info("**************************12345 - got message embed " + freshMessage.getData().embeds().stream().findFirst().get().description().get());
                    }
                    //using the old message as fresh message is missing the guild id
                    doEmbed(message, freshMessage.getData().embeds());
                } else {
                    lookForEmbeds(message, count + 1);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("404 Not Found")) {
                 logger.info("Message has been deleted before i can load it");
                } else {
                    printException(e);
                }
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }


}
