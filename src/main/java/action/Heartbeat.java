package action;

import action.Action;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat extends Action {

    private static LocalDateTime lastHeartbeat;
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
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
                try {
                    //if failes again, put doHeartbeatCheck here

                    logger.info("Doing heartbeat check, last message " + formatter.format(lastHeartbeat));
                    long minutes = ChronoUnit.MINUTES.between(lastHeartbeat, LocalDateTime.now()) % 60;
                    long sec = ChronoUnit.SECONDS.between(lastHeartbeat, LocalDateTime.now()) % 60;
                    client.getChannelById(Snowflake.of(statusChannel)).createMessage("Heartbeat - Last message " + minutes + " minutes and " + sec + " seconds ago!").block();
                    if (LocalDateTime.now().minusMinutes(delay + 1).isAfter(lastHeartbeat)) {
                        logger.info("Rebooting");
                        client.getChannelById(Snowflake.of(statusChannel)).createMessage("Heartbeat failed, rebooting").block();
                        System.exit(0);
                    }
                } catch (Exception e ){
                    logger.info("something went wrong");
                    printException(e);
                } catch (Throwable e) {
                    logger.info("something went more wrong");
                    e.printStackTrace();
                } finally {
                    doHeartbeatCheck();
                }
            }

        };

        logger.info("Doing heartbeat check in " + delay + " minutes");
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);

    }

}
