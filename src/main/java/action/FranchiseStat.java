package action;

import action.sm.Utils;
import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import com.gargoylesoftware.htmlunit.WebClient;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ChannelModifyRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FranchiseStat extends Action {


    String guildId;

    String shiftsChannel;
    String tacosChannel;
    String membersChannel;
    String boostChannel;
    String balanceChannel;


    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FranchiseStat() {
        param = "Speed Jar Started";
        guildId = config.get("guildId");

        shiftsChannel = config.get("shiftsChannel");
        tacosChannel = config.get("tacosChannel");
        membersChannel = config.get("membersChannel");
        boostChannel = config.get("boostChannel");
        balanceChannel = config.get("balanceChannel");

    }

    // on start up if no unlock look at last 15min
    // find start message and create lock and unlock time but dont print start message

    @Override
    public Mono<Object> doAction(Message message) {

        return Mono.empty();
    }

    private void create() {

        LocalDateTime unlockTime = LocalDateTime.now().plusMinutes(30);
        Utils.addReminder(SystemReminderType.updateStats, Timestamp.valueOf(unlockTime), "", "");

        LocalDateTime localNow = LocalDateTime.now();
        runUpdate(ChronoUnit.MINUTES.between(localNow, unlockTime));

    }

    private void update() {

        try {
            Long tacos = request("https://tacoshack.online/api/franchise/tacos?quantity=25");
            Long members = request("https://tacoshack.online/api/franchise/members?quantity=25");
            Long boost = request("https://tacoshack.online/api/franchise/incomes?quantity=25");
            Long balance = request("https://tacoshack.online/api/franchise/richest?quantity=100");
            Long shifts = request("https://tacoshack.online/api/franchise/shifts?quantity=25");

            ChannelModifyRequest change = ChannelModifyRequest.builder().name("update stats").build();

            updateChannel(tacosChannel, "Tacos Sold: " + String.format("%,d", tacos));
            updateChannel(membersChannel, "Franchise Members: " + String.format("%,d", members));
            updateChannel(boostChannel, "Income Boost: " + String.format("%,d", boost));
            updateChannel(balanceChannel, "Balance: " + String.format("%,d", balance));
            updateChannel(shiftsChannel, "Shifts Worked: " + String.format("%,d", shifts));

        } catch (Throwable e) {
            printException(e);
        }

    }

    private void updateChannel(String channel, String newName) {
        ChannelModifyRequest change = ChannelModifyRequest.builder().name(newName).build();

        client.getChannelById(Snowflake.of(channel)).modify(change, "updating channel stats").block();

    }

    private Long request(String url) throws IOException, ParseException {
        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
//        webClient.addRequestHeader("token", "123");
        String data = webClient.getPage(url).getWebResponse().getContentAsString();

        logger.info(data);
        JSONParser jsonParser = new JSONParser();

        Object obj = jsonParser.parse(data);
        AtomicLong value = new AtomicLong();
        ((JSONArray) obj).forEach(o -> {
            JSONObject franchise = (JSONObject)o;
            if (franchise.get("tag").toString().equals("OUI")) {
                value.set(Long.parseLong(franchise.get("value").toString()));
            }
        });


        return value.get();
    }


    public void runUpdate(long delay) {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running update stats");
                update();
                create();
            }
        };

        LocalDateTime lockTime = LocalDateTime.now().plusMinutes(delay);
        logger.info("update stats at " + formatter.format(lockTime));
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }


    public void startUp() {
        LocalDateTime runTime;

        List<SystemReminder> lockReminder = Utils.loadReminder(SystemReminderType.updateStats);
        LocalDateTime localNow = LocalDateTime.now();

        if (!lockReminder.isEmpty()) {
            runTime = lockReminder.get(0).getTime().toLocalDateTime();
            long delay = ChronoUnit.MINUTES.between(localNow, runTime);
            runUpdate(delay);
        } else {
            //run now
            update();
            //create next run time
            create();

        }


    }

}
