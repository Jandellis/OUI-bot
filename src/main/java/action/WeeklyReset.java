package action;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import action.export.ExportUtils;
import action.export.model.FranchiseStatType;
import action.sm.Utils;
import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.EmbedData;
import reactor.core.publisher.Mono;

public class WeeklyReset extends Action {


    String guildId;

    int tasksEndHour;
    String tacoBot = "490707751832649738";


    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public WeeklyReset() {
        param = "cytasks";
        guildId = config.get("guildId");

        tasksEndHour = Integer.parseInt(config.get("tasksEndHour"));

    }

    // on start up if no unlock look at last 15min
    // find start message and create lock and unlock time but dont print start message

    @Override
    public Mono<Object> doAction(Message message) {
        try {
            String action = getAction(message);
            if (action != null && hasPermission(message) ) {
                runTask(message.getChannelId().asString());
            }
        } catch (Exception e) {
            printException(e);
        }
        return Mono.empty();
    }


    private void create() {

        // Get the current time
        LocalDateTime now = LocalDateTime.now();

        // Find the next Monday (next occurrence of Monday)
        LocalDateTime nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toLocalDate().atTime(4, 45);

        // If the current time is already past 5 AM on Monday, set the target to next week's Monday
        if (now.isAfter(nextMonday)) {
            nextMonday = nextMonday.plusWeeks(1);
        }

//        // Calculate the duration between now and 5 AM next Monday
//        Duration duration = Duration.between(now, nextMonday);
//
//        // Get the number of minutes
//        long minutes = duration.toMinutes();





//        LocalDateTime unlockTime = LocalDateTime.now().plusMinutes(minutes);
        Utils.addReminder(SystemReminderType.weeklyReset, Timestamp.valueOf(nextMonday), "", "");

        LocalDateTime localNow = LocalDateTime.now();
        runUpdate(ChronoUnit.MINUTES.between(localNow, nextMonday));

    }

    private void runTask(String channelId) {

        try {
            int members = ExportUtils.getMembers("oui");
            double ot = ExportUtils.getFranchiseDouble("oui", FranchiseStatType.ot_estimate);
            double votes = ExportUtils.getFranchiseDouble("oui", FranchiseStatType.vote_estimate);
            Result result = findBestReward((int)votes, (int)ot, 1, 200);
            Result resultCurrent = findBestReward((int)votes, (int)ot, members, members);
            int kickMembers = members - result.members;

            client.getChannelById(Snowflake.of(channelId)).createMessage(
                    "Based on my estimate we should kick " + kickMembers + " members. " +
                            "\nThe reward is " + result.totalReward + " for " + result.members + " members" +
                            "\nWith out kicking anyone we should get... " +
                            "\nThe reward is " + resultCurrent.totalReward + " for " + resultCurrent.members + " members").block();

        } catch (Throwable e) {
            printException(e);
        }

    }

    private void updateChannel(String channel, String newName) {
        ChannelModifyRequest change = ChannelModifyRequest.builder().name(newName).build();

        client.getChannelById(Snowflake.of(channel)).modify(change, "updating channel stats").block();

    }


    public void runUpdate(long delay) {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running update stats");
                //reset franchise donations for the week
                ExportUtils.resetFranchiseDonations("oui");
                runTask("841078057565814845");
                create();
            }
        };

        LocalDateTime lockTime = LocalDateTime.now().plusMinutes(delay);
        logger.info("update stats at " + formatter.format(lockTime));
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }


    public void startUp() {
        LocalDateTime runTime;

        List<SystemReminder> resetTask = Utils.loadReminder(SystemReminderType.weeklyReset);
        LocalDateTime localNow = LocalDateTime.now();

        if (!resetTask.isEmpty()) {
            create();
            runTime = resetTask.get(0).getTime().toLocalDateTime();
            long delay = ChronoUnit.MINUTES.between(localNow, runTime);
            runUpdate(delay);
        } else {
            //run now
//            runTask("841078057565814845");
            //create next run time
            create();

        }


    }

    public static Result findBestReward(int votes, int overtime, int minMembers, int maxMembers) {
        Result bestResult = new Result(0, 0, 0, 0);
//        int maxMembers = 200;

        for (int m = maxMembers; m >= minMembers; m--) {
            // Task 2 – Overtime priority
            int overtimeUsed = 0;
            double reward2 = 0;

            if (overtime >= 15 * m) {
                reward2 = 3.0 * m;
                overtimeUsed = 15 * m;
            } else if (overtime >= 10 * m) {
                reward2 = 2.0 * m;
                overtimeUsed = 10 * m;
            } else if (overtime >= 5 * m) {
                reward2 = 1.0 * m;
                overtimeUsed = 5 * m;
            } else {
                continue;
            }

            // Task 1 – Votes
            int votesUsed = 0;
            double reward1 = 0;

            if (votes >= 3 * m) {
                reward1 = 1.0 * m;
                votesUsed = 3 * m;//should be 5
            } else if (votes >= 1 * m) {
                reward1 = 0.6 * m;
                votesUsed = 1 * m; //should be 3
            } else if (votes >= 1 * m) {
                reward1 = 0.2 * m;
                votesUsed = 1 * m;
            } else {
                continue;
            }

            double totalReward = reward1 + reward2;

            if ( totalReward > bestResult.totalReward) {
                bestResult = new Result(m, votesUsed, overtimeUsed, totalReward);
            }
        }

        return bestResult;
    }

    static class Result {
        int members;
        int votesUsed;
        int overtimeUsed;
        double totalReward;

        public Result(int members, int votesUsed, int overtimeUsed, double totalReward) {
            this.members = members;
            this.votesUsed = votesUsed;
            this.overtimeUsed = overtimeUsed;
            this.totalReward = totalReward;
        }
    }

}
