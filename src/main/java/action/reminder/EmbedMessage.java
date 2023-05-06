package action.reminder;

import action.Action;
import action.FranchiseStat;
import action.GiveawayAdd;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import action.sm.UpdateAlerts;
import action.upgrades.BuyUpgrade;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EmbedMessage extends Action {

    String tacoBot = "490707751832649738";
    List<EmbedAction> embedActions = new ArrayList<>();

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

    }


    @Override
    public Mono<Object> doAction(Message message) {

            if (message.getData().author().id().asString().equals(tacoBot)) {
                try {
                    List<EmbedData> embedData;
                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0){
                        logger.info("empty embeds");
                        if (message.getData().interaction().toOptional().isPresent() && message.getData().interaction().get().name().equals("saucemarket buy")) {
                            logger.info("Skipping message");
                        } else {

//                        embedData = checkEmbeds(message);
                            if (message.getContent().isEmpty()) {
                                lookForEmbeds(message, 0);
                            } else {
                                logger.info("Message has content, will not check for embeds");
                            }
                        }
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

        if (message.getData().author().id().asString().equals(tacoBot)) {
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

    private void lookForEmbeds(Message message, int count) {

        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
//                logger.info("checking message again");
//                Message msg = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), Snowflake.of(message.getId().asString())).block();
//                doAction(msg);

                List<EmbedData> embedData;
                logger.info("checking for embeds try "+ count);
                embedData = checkEmbeds(message);
                if (embedData.isEmpty()){
                    int newCount = count + 1;
                    lookForEmbeds(message, newCount);

                } else {
                    doEmbed(message, embedData);
                }
            }

        };
//        logger.info("checking message again in 1 sec");
        if (count < 5) {
            executorService.schedule(taskWrapper, 500, TimeUnit.MILLISECONDS);
        } else {

            logger.info("checking for embeds - hit "+ count);
        }
    }



}
