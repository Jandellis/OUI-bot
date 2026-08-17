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
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TopLevelGuildChannel;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class RushHour extends Action implements EmbedAction {


    String guildId;


    String tacoBot = "490707751832649738";
    String rushHourChannel = "1289435874240630825";


    String giveawayChannel;
    String giveawayShower;
    String giveawayRole;
    long chefRole;

//    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);


    private final ScheduledExecutorService executorService =
            Executors.newScheduledThreadPool(2, new ThreadFactory() {

                private final AtomicInteger threadNumber =
                        new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("Rushhour-" + threadNumber.getAndIncrement());
                    return thread;
                }
            });
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    String react;
    String reactIngore;
    Long recruiter;
    boolean openGiveaway;

    public RushHour() {
        param = "ouistartgift";
        guildId = config.get("guildId");
        giveawayChannel = config.get("giveawayChannelEvent");
        giveawayShower = config.get("giveawayChannel");
        giveawayRole = config.get("giveawayRole");
        react = "\uD83C\uDF89";

        react = "<:rh:1431319493136879717>";
        reactIngore = "❌";
//        react = "<a:pocketWave:1016913321213050892>";
        recruiter = Long.parseLong(config.get("recruiter"));
        chefRole = Long.parseLong(config.get("chefRole"));
        openGiveaway = Boolean.parseBoolean(config.get("openGiveaway", "false"));

    }

    @Override
    public Mono<Object> doAction(Message message) {
        try {

            String action = getAction(message, "cyrush");

            if ((action != null && action.equalsIgnoreCase("start"))
                    || message.getContent().equalsIgnoreCase("!rush")  &&
                    (message.getAuthor().get().getId().asString().equals("292839877563908097")
                    || message.getAuthor().get().getId().asString().equals("695518297168281640")
                    || message.getAuthor().get().getId().asString().equals("762526280435367986"))) {
                createRushHour(message);
            }
            if (action != null && action.equalsIgnoreCase("join")){

                Instant rushHourEnd = message.getTimestamp().plus(60, ChronoUnit.MINUTES);
                ReminderUtils.addRushHour(message.getAuthor().get().getId().asString(), Timestamp.from(rushHourEnd));
                react(message, react);
            }
            if (action != null && action.equalsIgnoreCase("leave")){
                Instant rushHourEnd = message.getTimestamp();
                ReminderUtils.addRushHour(message.getAuthor().get().getId().asString(), Timestamp.from(rushHourEnd));
                react(message, react);
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }


    @Override
    protected Mono<Object> doReactionEvent(ReactionAddEvent reactionAddEvent) {

        try {
            if (hasReaction(reactionAddEvent, react)) {
                Message message = reactionAddEvent.getMessage().block();
                if (message.getAuthor().isPresent()
                        && message.getAuthor().get().getId().asLong() == 962878786066595911L
                        && message.getContent().contains("Rush hour event now")) {

                    Instant rushHourEnd = message.getTimestamp().plus(60, ChronoUnit.MINUTES);
                    ReminderUtils.addRushHour(reactionAddEvent.getUserId().asString(), Timestamp.from(rushHourEnd));
                }
            }
            if (hasReaction(reactionAddEvent, reactIngore)) {
                Message message = reactionAddEvent.getMessage().block();
                if (message.getAuthor().isPresent()
                        && message.getAuthor().get().getId().asLong() == 962878786066595911L
                        && message.getContent().contains("Rush hour event now")) {

                    Instant rushHourEnd = message.getTimestamp().plus(60, ChronoUnit.MINUTES);
                    ReminderUtils.ignoreRushHour(reactionAddEvent.getUserId().asString(), Timestamp.from(rushHourEnd));
                }
            }
        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

    private boolean hasReaction(ReactionAddEvent reactionAddEvent, String emote) {
        boolean reaction = false;

        if (reactionAddEvent.getEmoji().asUnicodeEmoji().isPresent()){
            if (reactionAddEvent.getEmoji().asUnicodeEmoji().get().getRaw().equals(emote)) {
                reaction = true;
            }
        }


        if (reactionAddEvent.getEmoji().asCustomEmoji().isPresent()){
            if (reactionAddEvent.getEmoji().asCustomEmoji().get().asFormat().equals(emote)) {
                reaction = true;
            }
        }
        return reaction;
    }

    private void createRushHour(Message message) {
        message.getChannel().flatMap(channel -> {
            FranchiseConfig franchiseConfig;
            if (message.getGuildId().isPresent()) {
                logger.info("!!!!!!!!!!!!!!!! " + message.getGuildId().get().asString());


                franchiseConfig = ExportUtils.getFranchiseConfig(message.getGuildId().get().asString());
            } else {
                franchiseConfig = null;
            }

            String ping = "";
            if (franchiseConfig != null
                    && franchiseConfig.getRushHour() != null
                    && !franchiseConfig.getRushHour().isEmpty()) {
                ping = "<@&" + franchiseConfig.getRushHour() + "> ";

                logger.info("!!!!!!!!!!!!!!!! " + franchiseConfig);
                if (franchiseConfig.getName().equals("OUI")) {
                    runEnd(60);
                    boolean doWarnings = true;

                    List<SystemReminder> start = Utils.loadReminder(SystemReminderType.rushHourStart);
                    if (!start.isEmpty()) {
                        LocalDateTime rushHourStart = start.get(0).getTime().toLocalDateTime();
                        LocalDateTime now = LocalDateTime.now();
                        if (rushHourStart.isAfter(now.plusHours(1))) {
                            doWarnings = false;
                        }

                    }
                    if (doWarnings) {
                        runWarn(28*60 - 30);
                        runStart(28*60 - 5);
                    }
                }
            }

            /**
             * do 30 min warning
             * ping nafda and me at start time
             * its started
             * crete system reminder for 60min time       - rush hour end
             * create system reminder for 27.5 hours time - 30min warning
             * create system reminder for 28 hours time   - nafda amd me start ping
             * 60 min after starts, say thanks for joining, next one will start in 27 hours
             */

            Message pingMsg = channel.createMessage("Rush hour event now, " + ping + "react with " + react + " to get updated reminders for the next 60 minutes" + "\n" + "or with " + reactIngore + " to ignore this event").block();
//            pingMsg.addReaction(Emoji.unicode(react)).block();
            react(pingMsg, react);
            react(pingMsg, reactIngore);
            return Mono.empty();
        }).block();
    }



    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {

        if (message.getData().author().id().asString().equals(tacoBot)) {
            if ( embedData.get(0).title().toOptional().isPresent() &&
                    embedData.get(0).title().get().contains("Rush Hour Event Started!")) {
                createRushHour(message);

            }

        }
        return null;
    }


    public void runWarn(long delay) {
        LocalDateTime time = LocalDateTime.now().plusMinutes(delay);
        Utils.addReminder(SystemReminderType.rushHourWarning, Timestamp.valueOf(time), "", "");
        Runnable taskWrapper = () -> {
            logger.info("running rushHour warn");
            warn();
        };
        logger.info("rush hour warn at " + formatter.format(time));
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    public void runStart(long delay) {
        LocalDateTime time = LocalDateTime.now().plusMinutes(delay);
        Utils.addReminder(SystemReminderType.rushHourStart, Timestamp.valueOf(time), "", "");
        Runnable taskWrapper = () -> {
            logger.info("running rushHour start");
            start();
        };
        logger.info("rush hour start at " + formatter.format(time));
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    public void runEnd(long delay) {
        LocalDateTime time = LocalDateTime.now().plusMinutes(delay);
        Utils.addReminder(SystemReminderType.rushHourEnd, Timestamp.valueOf(time), "", "");
        ExportUtils.addFranchiseRushHour("oui" , time);

        Runnable taskWrapper = () -> {
            logger.info("running rushHour end");
            end();
        };
        logger.info("rush hour end at " + formatter.format(time));
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }


    private void warn() {
        List<SystemReminder> warn = Utils.loadReminder(SystemReminderType.rushHourWarning);
        if (timeBetween(warn.get(0)) < 5) {
            Utils.deleteReminder(SystemReminderType.rushHourWarning);
            String rushHour = ExportUtils.getFranchiseConfigByName("OUI").getRushHour();
            client.getChannelById(Snowflake.of(rushHourChannel)).createMessage("<@&" + rushHour + "> will start in 30 min time").block();
        }
    }

    private void start() {
        List<SystemReminder> start = Utils.loadReminder(SystemReminderType.rushHourStart);
        if (timeBetween(start.get(0)) < 5) {
            Utils.deleteReminder(SystemReminderType.rushHourStart);
            client.getChannelById(Snowflake.of(rushHourChannel)).createMessage("<@&1296069096055636010>, please start the rush hour \n</rushhour start:1289034970341314571>").block();
        }
    }

    private void end() {
        List<SystemReminder> end = Utils.loadReminder(SystemReminderType.rushHourEnd);

        if (timeBetween(end.get(0)) < 5) {
            Utils.deleteReminder(SystemReminderType.rushHourEnd);
            ZonedDateTime utcTime = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime startTime = utcTime.plusHours(27);


            List<SystemReminder> start = Utils.loadReminder(SystemReminderType.rushHourStart);
            if (!start.isEmpty()) {
                startTime = start.get(0).getTime().toLocalDateTime().atZone(ZoneOffset.UTC).plusMinutes(5);

            }


            client.getChannelById(Snowflake.of(rushHourChannel)).createMessage("Thanks for joining us, the next rush hour will start in 27 hours - <t:"+startTime.toEpochSecond()+":R> at <t:"+startTime.toEpochSecond()+":f>").block();
        }
    }

    public long timeBetween (SystemReminder reminder) {

        LocalDateTime localNow = LocalDateTime.now();
        LocalDateTime time;
        time = reminder.getTime().toLocalDateTime();
        long between = ChronoUnit.MINUTES.between(localNow, time);
        return between;
    }


    public void startUp() throws IOException {

        List<SystemReminder> start = Utils.loadReminder(SystemReminderType.rushHourStart);
        List<SystemReminder> end = Utils.loadReminder(SystemReminderType.rushHourEnd);
        List<SystemReminder> warn = Utils.loadReminder(SystemReminderType.rushHourWarning);

        LocalDateTime localNow = LocalDateTime.now();
        LocalDateTime time;

        if (!start.isEmpty() ){
            time = start.get(0).getTime().toLocalDateTime();
            long delay = ChronoUnit.MINUTES.between(localNow, time);
            runStart(delay);
        }
        if (!end.isEmpty() ){
            time = end.get(0).getTime().toLocalDateTime();
            long delay = ChronoUnit.MINUTES.between(localNow, time);
            runEnd(delay);
        }
        if (!warn.isEmpty() ){
            time = warn.get(0).getTime().toLocalDateTime();
            long delay = ChronoUnit.MINUTES.between(localNow, time);
            runWarn(delay);
        }
    }


    /**
     * do 30 min warning
     * ping nafda and me at start time
     * its started
     * crete system reminder for 60min time       - rush hour end
     * create system reminder for 27.5 hours time - 30min warning
     * create system reminder for 28 hours time   - nafda amd me start ping
     * 60 min after starts, say thanks for joining, next one will start in 27 hours
     */
}
