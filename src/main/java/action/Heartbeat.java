package action;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Heartbeat extends Action {

    private static LocalDateTime lastHeartbeat;
//    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    private final ScheduledExecutorService executorService =
            Executors.newScheduledThreadPool(2, new ThreadFactory() {

                private final AtomicInteger threadNumber =
                        new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("Heartbeat-" + threadNumber.getAndIncrement());
                    return thread;
                }
            });
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    int delay = 2;
    String statusChannel = "1002115224876359680";

    public Heartbeat() {
        lastHeartbeat = LocalDateTime.now();
        doHeartbeatCheck();

    }

    @Override
    public Mono<Object> doAction(Message message) {
        try {
            lastHeartbeat = LocalDateTime.now();
//            logger.info("Got heartbeat");

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }


    public void doHeartbeatCheck() {


        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                while (true) {

                    try {
                        //if failes again, put doHeartbeatCheck here

                        logger.info("Doing heartbeat check, last message " + formatter.format(lastHeartbeat));
                        long minutes = ChronoUnit.MINUTES.between(lastHeartbeat, LocalDateTime.now()) % 60;
                        long sec = ChronoUnit.SECONDS.between(lastHeartbeat, LocalDateTime.now()) % 60;
                        client.getChannelById(Snowflake.of(statusChannel)).createMessage("Heartbeat - Last message " + minutes + " minutes and " + sec + " seconds ago!").block();

                        printStats();
                        if (LocalDateTime.now().minusMinutes(delay + 1).isAfter(lastHeartbeat)) {
                            logger.info("Rebooting");
                            client.getChannelById(Snowflake.of(statusChannel)).createMessage("Heartbeat failed, rebooting").block();
                            System.exit(0);
                        }
                    } catch (Exception e) {
                        logger.info("something went wrong");
                        printException(e);
                    } catch (Throwable e) {
                        logger.info("something went more wrong");
                        e.printStackTrace();
                    } finally {
                        try {
                            Thread.sleep(120000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                    doHeartbeatCheck();
                    }

                }
            }

        };

        logger.info("Doing heartbeat check in " + delay + " minutes");
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);

    }

    private void printStats() {
        Map<Thread, StackTraceElement[]> threads =
                Thread.getAllStackTraces();

        ThreadMXBean bean =
                ManagementFactory.getThreadMXBean();

        Map<String, int[]> threadGroups = new HashMap<>();
        Map<Thread.State, Integer> threadStates =
                new EnumMap<>(Thread.State.class);


// ======================================================
// Collect thread information
// ======================================================

        for (Thread thread : threads.keySet()) {

            String name = thread.getName();

            // Get everything before the first "-"
            int dashIndex = name.indexOf("-");

            String groupName;

            if (dashIndex > 0) {
                groupName = name.substring(0, dashIndex);
            } else {
                groupName = name;
            }


            // Get/create counters for this group
            //
            // [0] = total threads
            // [1] = WAITING threads
            //
            int[] counts = threadGroups.get(groupName);

            if (counts == null) {
                counts = new int[2];
                threadGroups.put(groupName, counts);
            }

            // Total threads
            counts[0]++;


            // WAITING threads
            if (thread.getState() == Thread.State.WAITING) {
                counts[1]++;
            }


            // Overall thread state count
            Thread.State state = thread.getState();

            threadStates.put(
                    state,
                    threadStates.getOrDefault(state, 0) + 1
            );
        }


// ======================================================
// Build message
// ======================================================

        StringBuilder message = new StringBuilder();

        message.append("=== JVM THREAD STATS ===\n");

        message.append("Current threads: ")
                .append(bean.getThreadCount())
                .append("\n");

        message.append("Peak threads: ")
                .append(bean.getPeakThreadCount())
                .append("\n");

        message.append("Total threads created: ")
                .append(bean.getTotalStartedThreadCount())
                .append("\n\n");


// ======================================================
// Thread groups
// ======================================================

        message.append("=== THREAD GROUPS ===\n");


// Sort groups by total thread count, highest first
        List<Map.Entry<String, int[]>> sortedGroups =
                new ArrayList<>(threadGroups.entrySet());

        sortedGroups.sort(
                (a, b) -> Integer.compare(
                        b.getValue()[0],
                        a.getValue()[0]
                )
        );


        for (Map.Entry<String, int[]> entry : sortedGroups) {

            String groupName = entry.getKey();

            int[] counts = entry.getValue();

            int total = counts[0];
            int waiting = counts[1];

            message.append(groupName)
                    .append(": ")
                    .append(total)
                    .append(" - ")
                    .append(waiting)
                    .append("\n");
        }


        message.append("\n");


// ======================================================
// Thread states
// ======================================================

        message.append("=== THREAD STATES ===\n");

        for (Thread.State state : Thread.State.values()) {

            int count =
                    threadStates.getOrDefault(state, 0);

            message.append(state)
                    .append(": ")
                    .append(count)
                    .append("\n");
        }


// ======================================================
// Split into Discord-sized messages
// ======================================================

        List<String> messages = new ArrayList<>();

        String[] lines =
                message.toString().split("\n");

        StringBuilder currentMessage =
                new StringBuilder();

        for (String line : lines) {

            // Leave a little safety margin below Discord's
            // 2000 character message limit.
            if (currentMessage.length()
                    + line.length()
                    + 1 > 1900) {

                if (currentMessage.length() > 0) {
                    messages.add(
                            currentMessage.toString()
                    );
                }

                currentMessage.setLength(0);
            }

            currentMessage
                    .append(line)
                    .append("\n");
        }


// Add remaining content
        if (currentMessage.length() > 0) {
            messages.add(
                    currentMessage.toString()
            );
        }


// ======================================================
// Send to Discord
// ======================================================

        for (String discordMessage : messages) {

            client.getChannelById(
                            Snowflake.of(statusChannel)
                    )
                    .createMessage(discordMessage)
                    .block();
        }
    }

}
