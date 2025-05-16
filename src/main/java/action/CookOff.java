package action;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.*;
import java.util.concurrent.*;

import action.export.ExportUtils;
import action.export.model.FranchiseStatType;
import action.sm.Utils;
import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ChannelModifyRequest;
import reactor.core.publisher.Mono;

public class CookOff extends Action {


    String guildId;

    int tasksEndHour;
    String tacoBot = "490707751832649738";
    String cookOffType = "tacochat";
    String cookOffChannelLink = "495827872775143424";
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ZoneId zoneId = ZoneId.systemDefault(); // Or specify your zone, e.g., ZoneId.of("Australia/Sydney")

    public CookOff() {
        guildId = config.get("guildId");


        tasksEndHour = Integer.parseInt(config.get("tasksEndHour"));

    }
    @Override
    public Mono<Object> doAction(Message message) {
        return Mono.empty();
    }

    public void startUp() {
        try {
            scheduleNextReminder(ZonedDateTime.now(zoneId));
        } catch (Throwable e) {
            printException(e);
        }
    }

    private void scheduleNextReminder(ZonedDateTime fromTime) {
        ZonedDateTime nextReminderTime = getNextReminderTime(fromTime);
        long delay = Duration.between(ZonedDateTime.now(zoneId), nextReminderTime).toMillis();
        logger.info("🔔 CookOff "+cookOffType+" Reminder scheduled for: " + nextReminderTime + " or in "+ (delay/1000/60) + " minutes");
        client.getChannelById(Snowflake.of("1362036275821019196")).createMessage("Next Cookoff in "+cookOffType+" in  <t:"+nextReminderTime.toEpochSecond()+":R>").block();

        scheduler.schedule(() -> {
            try {
                logger.info("🔔 CookOff "+cookOffType+"Reminder triggered at: " + ZonedDateTime.now(zoneId));

                //post message to channel
                client.getChannelById(Snowflake.of("1362036275821019196")).createMessage("<@&1362018134827208714>, Cookoff in "+cookOffType+" now at <#" + cookOffChannelLink + "> ").block();

                // Schedule the next one
                scheduleNextReminder(ZonedDateTime.now(zoneId));
            } catch (Throwable e) {
                printException(e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public ZonedDateTime getNextReminderTime(ZonedDateTime fromTime) {
        ZonedDateTime time = fromTime.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
        while (!isReminderTime(time)) {
            time = time.plusMinutes(1);
        }
        return time;
    }

    public boolean isReminderTime(ZonedDateTime time) {
        DayOfWeek day = time.getDayOfWeek();
        LocalTime localTime = time.toLocalTime();

        // Define window: Tuesday 6:00am to Wednesday 6:00am
        ZonedDateTime tuesday6am = time.with(TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY)).withHour(tasksEndHour).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime wednesday6am = tuesday6am.plusDays(1);

        if (!time.isBefore(tuesday6am) && time.isBefore(wednesday6am)) {
            return localTime.getMinute() % 30 == 0;
        } else {
            if (localTime.getMinute() != 30) {
                return false;
            }

            ZonedDateTime wednesday630am = tuesday6am.plusDays(1).withHour(tasksEndHour).withMinute(30);
            long minutes = Duration.between(wednesday630am, time).toMinutes();
            return minutes % 120 == 0;
        }
    }

}
