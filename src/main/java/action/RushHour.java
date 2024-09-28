package action;

import action.export.ExportUtils;
import action.export.model.FranchiseConfig;
import action.giveaway.model.GiveawayLog;
import action.giveaway.model.GiveawayWinner;
import action.reminder.EmbedAction;
import action.reminder.ReminderUtils;
import action.reminder.model.FlexStats;
import action.reminder.model.Profile;
import action.sm.Utils;
import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import bot.Clean;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class RushHour extends Action implements EmbedAction {


    String guildId;


    String tacoBot = "490707751832649738";

    String giveawayChannel;
    String giveawayShower;
    String giveawayRole;
    long chefRole;

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    String react;
    Long recruiter;
    boolean openGiveaway;

    public RushHour() {
        param = "ouistartgift";
        guildId = config.get("guildId");
        giveawayChannel = config.get("giveawayChannelEvent");
        giveawayShower = config.get("giveawayChannel");
        giveawayRole = config.get("giveawayRole");
        react = "\uD83C\uDF89";
        recruiter = Long.parseLong(config.get("recruiter"));
        chefRole = Long.parseLong(config.get("chefRole"));
        openGiveaway = Boolean.parseBoolean(config.get("openGiveaway", "false"));

    }

    @Override
    public Mono<Object> doAction(Message message) {
        try {


        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }


    @Override
    protected Mono<Object> doReactionEvent(ReactionAddEvent reactionAddEvent) {

        try {

            if (reactionAddEvent.getEmoji().asUnicodeEmoji().isPresent()){
                if (reactionAddEvent.getEmoji().asUnicodeEmoji().get().getRaw().equals(react)) {

                    //got reaction
                    Message message = reactionAddEvent.getMessage().block();
                    if (message.getAuthor().isPresent()
                            && message.getAuthor().get().getId().asLong() == 962878786066595911L
                            && message.getContent().contains("Rush hour event now")) {

                        Instant rushHourEnd = message.getTimestamp().plus(60, ChronoUnit.MINUTES);
                        ReminderUtils.addRushHour(reactionAddEvent.getUserId().asString(), Timestamp.from(rushHourEnd));
                    }

                }
            }
        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }



    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {
        FranchiseConfig franchiseConfig;
        if (message.getGuildId().isPresent()) {
             franchiseConfig = ExportUtils.getFranchiseConfig(message.getGuildId().get().asString());
        } else {
            franchiseConfig = null;
        }


        if (message.getData().author().id().asString().equals(tacoBot)) {
            if ( embedData.get(0).title().toOptional().isPresent() &&
                    embedData.get(0).title().get().contains("Rush Hour Event Started!")) {
                message.getChannel().flatMap(channel -> {
                    String ping = "";
                    if (franchiseConfig != null && !franchiseConfig.getRushHour().isEmpty()) {
                        ping = "<@&" + franchiseConfig.getRushHour() + "> ";
                    }

                    Message pingMsg = channel.createMessage("Rush hour event now, " + ping + "react with " + react + " to get updated reminders").block();
                    pingMsg.addReaction(ReactionEmoji.unicode(react)).block();
                    return Mono.empty();
                }).block();

            }

        }
        return null;
    }
}
