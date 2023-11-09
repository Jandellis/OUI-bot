package action;

import action.export.ExportUtils;
import action.export.model.FranchiseStatType;
import action.reminder.EmbedAction;
import action.sm.Utils;
import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import com.gargoylesoftware.htmlunit.WebClient;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.EmbedData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class FranchiseStat extends Action implements EmbedAction {


    String guildId;

    String shiftsChannel;
    String tacosChannel;
    String membersChannel;
    String boostChannel;
    String balanceChannel;
    int tasksEndHour;
    String tacoBot = "490707751832649738";


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
        tasksEndHour = Integer.parseInt(config.get("tasksEndHour"));

    }

    // on start up if no unlock look at last 15min
    // find start message and create lock and unlock time but dont print start message

    @Override
    public Mono<Object> doAction(Message message) {
        try {
            if (message.getData().author().id().asString().equals(tacoBot)) {

                List<EmbedData> embedData;
                if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0) {

                    embedData = checkEmbeds(message);
                } else {
                    embedData = message.getData().embeds();
                }
                handleEmbedAction(message, embedData);
                //for some reason the embeds will be empty from slash, but if i load it again it will have data
//                if (checkAge(message)) {
//                    checkMessageAgain(message);
//                } else {
            }
//            }
        } catch (Exception e) {
            printException(e);
        }
        return Mono.empty();
    }

    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {
        try {
            for (EmbedData embed : embedData) {
                if (embed.title().toOptional().isPresent() && embed.description().toOptional().isPresent()) {
                    String title = embed.title().get();

                    if (title.contains("\uD83D\uDCC5 Weekly Tasks")) {
                        String[] lines = embed.description().get().split("\n");
//                                lines[0];// Tasks will be reset in: `6 days`
//                                lines[2];//<:bwemoji:666839150221197331><:bwemoji:666839150221197331><:bwemoji:666839150221197331> **Vote 170 Times** | `69/170`
//                                lines[5];//<:bwemoji:666839150221197331><:bwemoji:666839150221197331><:bwemoji:666839150221197331> **Work 840 Overtime Shifts** | `357/840`

                        LocalDateTime dateTime = LocalDateTime.now();
                        dateTime = dateTime.plusSeconds(60 - dateTime.getSecond());
                        dateTime = dateTime.plusMinutes(60 - dateTime.getMinute());
                        int endHour = tasksEndHour; //18 my time // 6am server time
                        if (dateTime.getHour() > endHour) {
                            dateTime = dateTime.minusHours(dateTime.getHour() - endHour);
                        } else {
                            dateTime = dateTime.plusHours(endHour - dateTime.getHour());
                        }

                        String timeLeft = lines[0].split("`")[1]; //6 days

                        int daysToAdd = 8 - dateTime.getDayOfWeek().getValue();
                        if (daysToAdd < 7 || timeLeft.contains("day"))
                            dateTime = dateTime.plusDays(8 - dateTime.getDayOfWeek().getValue());


                        double days = 6;
                        double pecentage = 1;
                        if (timeLeft.contains("day") || timeLeft.contains("hour")) {
                            days = 7 - Integer.parseInt(timeLeft.split(" ")[0]);
                            Duration delay = Duration.between(LocalDateTime.now(), dateTime);
                            long totalSeconds = 60 * 60 * 24 * 7; // min * hour * day * week
                            pecentage = 1 - (delay.getSeconds() * 1.0 / totalSeconds);
                        }

                        //2 hours
//                                if (timeLeft.contains("hour")) {
//                                    double hours = 24 - Integer.parseInt(timeLeft.split(" ")[0]);
//                                    days = days + hours / 24;
//                                    pecentage = days / 7;
//                                }

                        //56 minutes
                        if (timeLeft.contains("minute")) {
                            double minutes = 60 - Integer.parseInt(timeLeft.split(" ")[0]);
                            double hours = 23.0 / 24.0;
                            days = days + hours + (minutes / 60.0 / 24.0);
                            pecentage = days / 7;
                        }


                        int votes = Integer.parseInt(lines[2].split("`")[1].split("/")[0].replace(",", ""));
                        double voteAvg = votes / days;
//                                double voteEstimate = voteAvg * 7;

                        double voteEstimate = votes / pecentage;
                        int ot = Integer.parseInt(lines[5].split("`")[1].split("/")[0].replace(",", ""));
                        double otAvg = ot / days;
//                                double otEstimate = otAvg * 7;
                        double otEstimate = ot / pecentage;

                        String msg = "I estimate you will get \n" +
                                ":small_blue_diamond: **Votes** : " + new DecimalFormat("#").format(voteEstimate) + "\n" +
                                ":small_blue_diamond: **Ot** : " + new DecimalFormat("#").format(otEstimate);

                        client.getChannelById(message.getChannelId()).createMessage(msg).block();
                    }

                    if (title.contains("...")) {
                        //look for balance
                        //look for sacos sold
                        //look for income
                    }
                } else {
                    if (embed.fields().toOptional().isPresent() && embed.fields().get().get(0).value().contains("[OUI] Oui Da Best Taco")){

                        String balance = embed.fields().get().get(5).value().replace("\uD83D\uDCB5 $", "").replace(",", "");
                        ExportUtils.updateFranchiseStat(FranchiseStatType.balance, Long.parseLong(balance), "oui");
                        String sold = embed.fields().get().get(6).value().replace("\uD83C\uDF2E ", "").replace(",", "");
                        ExportUtils.updateFranchiseStat(FranchiseStatType.sold, Long.parseLong(sold), "oui");
                        String income = embed.fields().get().get(7).value().replace("\uD83D\uDCB8 $", "").replace(",", "");
                        ExportUtils.updateFranchiseStat(FranchiseStatType.income, Long.parseLong(income), "oui");
                        embed.fields().get().get(5).value();//💵 $5,072,587,412 "\uD83D\uDCB5"
                        embed.fields().get().get(6).value();//🌮 14,774,858,494 "\uD83C\uDF2E "
                        embed.fields().get().get(7).value();//💸 $9,900 "\uD83D\uDCB8"
                    }
                }
            }

        } catch (Exception e) {
            printException(e);
        }
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
            request("https://tacoshack.online/api/leaderboard/franchise/all", false);

//            String tacos = request("https://tacoshack.online/api/franchise/tacos?quantity=25", false);
//            String members = request("https://tacoshack.online/api/franchise/members?quantity=100", false);
//            String boost = request("https://tacoshack.online/api/franchise/incomes?quantity=25", false);
//            String balance = request("https://tacoshack.online/api/franchise/richest?quantity=100", true);
//            String shifts = request("https://tacoshack.online/api/franchise/shifts?quantity=25", false);

//            ChannelModifyRequest change = ChannelModifyRequest.builder().name("update stats").build();
//
//            updateChannel(tacosChannel, "Tacos Sold: " + tacos);
//            updateChannel(membersChannel, "Franchise Members: " + members);
//            updateChannel(boostChannel, "Income Boost: " + boost);
//            updateChannel(balanceChannel, "Balance: " + balance);
//            updateChannel(shiftsChannel, "Shifts Worked: " + shifts);

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

    public static String getHumanReadablePriceFromNumber(long number) {

        if (number >= 1000000000000L) {
            return String.format("%.2f Trillion", number / 1000000000000.0);
        }
        if (number >= 1000000000) {
            return String.format("%.2f Billion", number / 1000000000.0);
        }

        if (number >= 1000000) {
            return String.format("%.2f Million", number / 1000000.0);
        }

        if (number >= 100000) {
//            return String.format("%.2fL", number/ 100000.0);
            return String.format("%,d", number);
        }

        if (number >= 1000) {
//            return String.format("%.2fK", number/ 1000.0);
            return String.format("%,d", number);
        }
        return String.valueOf(number);

    }


    private void request(String url, boolean money) throws IOException, ParseException {
        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
//        webClient.addRequestHeader("token", "123");
        String data = webClient.getPage(url).getWebResponse().getContentAsString();

        logger.info(data);
        JSONParser jsonParser = new JSONParser();

        JSONObject obj = (JSONObject)jsonParser.parse(data);
//        AtomicLong value = new AtomicLong(0);
//            JSONObject itemData = (JSONObject) obj;





//                updateChannel(balanceChannel, "Balance: " + getValue((JSONObject)obj.get("richest")));
//
//                updateChannel(tacosChannel, "Tacos Sold: " + getValue((JSONObject)obj.get("tacos")));
//
//                updateChannel(shiftsChannel, "Shifts Worked: " + getValue((JSONObject)obj.get("shifts")));
//
//        updateChannel(boostChannel, "Income Boost: " +getValue((JSONObject)obj.get("income")));

        updateChannel(balanceChannel, "Balance: " + format(ExportUtils.getFranchiseStat("oui", FranchiseStatType.balance)));
        updateChannel(tacosChannel, "Tacos Sold: " + format(ExportUtils.getFranchiseStat("oui", FranchiseStatType.sold)));
        updateChannel(boostChannel, "Income Boost: " + format(ExportUtils.getFranchiseStat("oui", FranchiseStatType.income)));
        updateChannel(membersChannel, "Franchise Members: " + ExportUtils.getMembers("oui"));



//            if (itemData.get("tag").toString().equals("OUI")) {
//                value.set(Long.parseLong(itemData.get("value").toString()));
//            }
//        if (money && value.get() == 0) {
//            value.set(Long.parseLong(((JSONObject) ((JSONArray) obj).get(99)).get("value").toString()));
//            String formatted = format(value.get());
//            return "< " + formatted;
//        }
        //if money and value = 0, look at last element and use < 123
        //if money format 123.2 M, or 12.43 B


//        return format(value.get());
    }

    private String getValue(JSONObject itemData ){
        AtomicLong value = new AtomicLong(0);

        JSONArray franchiseList = ((JSONArray) itemData.get("users"));
        for (Object object : franchiseList) {
            JSONObject franchise = (JSONObject) object;
            if (franchise.get("id").toString().equals("OUI")) {
                value.set(Long.parseLong(franchise.get("value").toString()));
            }
        }
        if (value.get() == 0) {
            value.set(Long.parseLong(((JSONObject) ((JSONArray) franchiseList).get(24)).get("value").toString()));
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
