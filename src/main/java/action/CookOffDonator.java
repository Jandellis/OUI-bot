package action;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class CookOffDonator extends CookOff {

    public CookOffDonator() {
        super();
        cookOffChannelLink = "622635554336538654";
        cookOffType = "donator";
    }


    @Override
    public boolean isReminderTime(ZonedDateTime now) {
        // Normalize time to remove seconds and nanoseconds
        LocalTime time = now.toLocalTime().withSecond(0).withNano(0);

        // Base start time is 6:00 AM
        LocalTime startTime = LocalTime.of(tasksEndHour, 0);

        // Calculate minutes between now and the base time
        long minutesSinceStart = Duration.between(startTime, time).toMinutes();

        // Handle negative durations (times before 6am)
        if (minutesSinceStart < 0) {
            minutesSinceStart += 24 * 60; // add 24 hours in minutes
        }

        // Trigger every 4 hours = 240 minutes
        return minutesSinceStart % 240 == 0;
    }

}
