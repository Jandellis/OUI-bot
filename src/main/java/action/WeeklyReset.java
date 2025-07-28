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
    String rushHourChannel = "1289435874240630825";


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
                runTask(message.getChannelId().asString(), false);
            }
            action = getAction(message, "cycorrect");
            if (action != null && hasPermission(message) ) {
                runTask(message.getChannelId().asString(), true);
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

    private void runTask(String channelId, boolean isCorrect) {

        try {
            int members = ExportUtils.getMembers("oui");
            double ot = ExportUtils.getFranchiseDouble("oui", FranchiseStatType.ot_estimate);
            double votes = ExportUtils.getFranchiseDouble("oui", FranchiseStatType.vote_estimate);
            Result result = findBestReward((int)votes, (int)ot, 1, 200, isCorrect);
            Result resultCurrent = findBestReward((int)votes, (int)ot, members, members, isCorrect);
            Result resultkick = findBestReward((int)votes, (int)ot, 1, members, isCorrect);
            int kickMembers = members - resultkick.members;

            client.getChannelById(Snowflake.of(channelId)).createMessage(
                    "The max reward we can get with our current numbers is " + result.totalReward + " for " + result.members + " members" +
                    "\nOur Current reward is " + resultCurrent.totalReward + " for " + resultCurrent.members + " members" +
                    "\nIf we kick **"+kickMembers+"** the reward is  " + resultkick.totalReward + " for " + resultkick.members + " members"
            ).block();

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
                client.getChannelById(Snowflake.of(rushHourChannel)).createMessage(
                        "<@&1296069096055636010>, please check for extra rush hours \n</rushhour start:1289034970341314571>"
                ).block();


                runTask("841078057565814845", false);
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

    public static Result findBestReward(int votes, int overtime, int minMembers, int maxMembers, boolean isCorrect) {
        Result bestResult = new Result(0, 0, 0, 0);
//        int maxMembers = 200;
        double multiplier = 1.0;

        for (int m = maxMembers; m >= minMembers; m--) {
            // Task 2 – Overtime priority
            int overtimeUsed = 0;
            double reward2 = 0;

            if (overtime >= 15 * m) {
                if (isCorrect) {
                    multiplier = 3;
                } else {
                    multiplier = 3; // remove when they fix the bug
                }
                reward2 = multiplier * m;
                overtimeUsed = 15 * m;
            } else if (overtime >= 10 * m) {
                if (isCorrect) {
                    multiplier = 2;
                } else {
                    multiplier = 3; // remove when they fix the bug
                }
                reward2 = multiplier * m;
                overtimeUsed = 10 * m;
            } else if (overtime >= 5 * m) {
                if (isCorrect) {
                    multiplier = 1;
                } else {
                    multiplier = 2; // remove when they fix the bug
                }
                reward2 = multiplier * m;
                overtimeUsed = 5 * m;
            } else {
                continue;
            }

            // Task 1 – Votes
            int votesUsed = 0;
            double reward1 = 0;

            if (votes >= 5 * m) {
                if (isCorrect) {
                    multiplier = 1;
                } else {
                    multiplier = 1; // remove when they fix the bug
                }
                reward1 = multiplier * m;
                votesUsed = 5 * m;
            } else if (votes >= 3 * m) {
                if (isCorrect) {
                    multiplier = 0.6;
                } else {
                    multiplier = 1; // remove when they fix the bug
                }
                reward1 = multiplier * m;
                votesUsed = 3 * m;
            } else if (votes >= 1 * m) {
                if (isCorrect) {
                    multiplier = 0.2;
                } else {
                    multiplier = 0.6; // remove when they fix the bug
                }
                reward1 = multiplier * m;
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
