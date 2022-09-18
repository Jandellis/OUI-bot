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
            String tacos = request("https://tacoshack.online/api/franchise/tacos?quantity=25", false);
            String members = request("https://tacoshack.online/api/franchise/members?quantity=25", false);
            String boost = request("https://tacoshack.online/api/franchise/incomes?quantity=25", false);
            String balance = request("https://tacoshack.online/api/franchise/richest?quantity=100", true);
            String shifts = request("https://tacoshack.online/api/franchise/shifts?quantity=25", false);

//            ChannelModifyRequest change = ChannelModifyRequest.builder().name("update stats").build();

            updateChannel(tacosChannel, "Tacos Sold: " + tacos);
            updateChannel(membersChannel, "Franchise Members: " + members);
            updateChannel(boostChannel, "Income Boost: " + boost);
            updateChannel(balanceChannel, "Balance: " + balance);
            updateChannel(shiftsChannel, "Shifts Worked: " + shifts);

        } catch (Throwable e) {
            printException(e);
        }

    }

    private void updateChannel(String channel, String newName) {
        ChannelModifyRequest change = ChannelModifyRequest.builder().name(newName).build();

        client.getChannelById(Snowflake.of(channel)).modify(change, "updating channel stats").block();

    }

    private String format(Long value) {
        return getHumanReadablePriceFromNumber(value);
    }

    public static String getHumanReadablePriceFromNumber(long number){

        if(number >= 1000000000000L){
            return String.format("%.2f Trillion", number/ 1000000000000.0);
        }
        if(number >= 1000000000){
            return String.format("%.2f Billion", number/ 1000000000.0);
        }

        if(number >= 1000000){
            return String.format("%.2f Million", number/ 1000000.0);
        }

        if(number >= 100000){
//            return String.format("%.2fL", number/ 100000.0);
            return String.format("%,d", number);
        }

        if(number >=1000){
//            return String.format("%.2fK", number/ 1000.0);
            return String.format("%,d", number);
        }
        return String.valueOf(number);

    }


    private String request(String url, boolean money) throws IOException, ParseException {
        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
//        webClient.addRequestHeader("token", "123");
        String data = webClient.getPage(url).getWebResponse().getContentAsString();

        logger.info(data);
        JSONParser jsonParser = new JSONParser();

        Object obj = jsonParser.parse(data);
        AtomicLong value = new AtomicLong(0);
        ((JSONArray) obj).forEach(o -> {
            JSONObject franchise = (JSONObject)o;
            if (franchise.get("tag").toString().equals("OUI")) {
                value.set(Long.parseLong(franchise.get("value").toString()));
            }
        });
        if (money && value.get() == 0) {
            value.set(Long.parseLong(((JSONObject)((JSONArray) obj).get(99)).get("value").toString()));
            String formatted = format(value.get());
            return "< " + formatted;
        }
        //if money and value = 0, look at last element and use < 123
        //if money format 123.2 M, or 12.43 B


        return format(value.get());
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
