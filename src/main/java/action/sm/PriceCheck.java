package action.sm;

import action.Action;
import action.reminder.ReminderUtils;
import action.reminder.model.Profile;
import action.sm.model.Alert;
import action.sm.model.AlertType;
import action.sm.model.ChangeRange;
import action.sm.model.Drop;
import action.sm.model.SauceMarketStats;
import action.sm.model.SauceMarketStreak;
import action.sm.model.SystemReminderType;
import bot.Config;
import bot.Sauce;
import bot.SauceObject;
import com.gargoylesoftware.htmlunit.WebClient;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PriceCheck extends Action {


    Config config = Config.getInstance();
    int startMin;
    String smUpdate;
    String smChannel;
    String cheapPing;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    static long chefRole;
    int cheapPrice = 45;
    List<String> smUpdateChannels;
    boolean hideSS;
    Map<Integer, Double> streakOdds;
    int tasksEndHour;


    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public PriceCheck() {

        smUpdate = config.get("smUpdate");
        cheapPing = config.get("cheapPing");
        smChannel = config.get("smChannel");
        startMin = Integer.parseInt(config.get("priceCheck"));
        chefRole = Long.parseLong(config.get("chefRole"));
        cheapPrice = Integer.parseInt(config.get("cheapPrice"));
        smUpdateChannels = Arrays.asList(config.get("smUpdateChannels").split(","));
        hideSS = Boolean.parseBoolean(config.get("hideSS", "true"));
        tasksEndHour = Integer.parseInt(config.get("tasksEndHour"));

        // Load the streak odds
        streakOdds = new HashMap<>();
        streakOdds.put(15, 0.99);
        streakOdds.put(14, 0.99);
        streakOdds.put(13, 0.99);
        streakOdds.put(12, 0.99);
        streakOdds.put(11, 0.99);
        streakOdds.put(10, 0.99);
        streakOdds.put(9, 0.99);
        streakOdds.put(8, 0.99);
        streakOdds.put(7, 0.99);
        streakOdds.put(6, 0.99);
        streakOdds.put(5, 0.99);
        streakOdds.put(4, 0.99);
        streakOdds.put(3, 0.99);
        streakOdds.put(2, 0.99);
        streakOdds.put(1, 0.99);
        streakOdds.put(0, 0.99);
        streakOdds.put(-1, 0.99);
        streakOdds.put(-2, 0.99);
        streakOdds.put(-3, 0.99);
        streakOdds.put(-4, 0.99);
        streakOdds.put(-5, 0.99);
        streakOdds.put(-6, 0.99);
        streakOdds.put(-7, 0.99);
        streakOdds.put(-8, 0.99);
        streakOdds.put(-9, 0.99);
        streakOdds.put(-10, 0.99);
        streakOdds.put(-11, 0.99);
        streakOdds.put(-12, 0.99);
        streakOdds.put(-13, 0.99);
        streakOdds.put(-14, 0.99);
        streakOdds.put(-15, 0.99);


    }

    public static boolean isInOddMonthWindow(LocalDate date) {
        int month = date.getMonthValue();

        // Find nearest odd month (current or previous)
        int oddMonth = (month % 2 == 1) ? month : month - 1;
        if (oddMonth <= 0) {
            oddMonth = 1; // handle January edge case
        }

        int year = date.getYear();
        // If oddMonth > current month, go back one year
        if (oddMonth > month) {
            year -= 1;
        }

        LocalDate startOfOddMonth = LocalDate.of(year, oddMonth, 1);
        LocalDate startWindow = startOfOddMonth.minusWeeks(1).minusDays(1);
        LocalDate endWindow = startOfOddMonth.plusWeeks(2).plusDays(1); // end of 2nd week

        return !date.isBefore(startWindow) && !date.isAfter(endWindow);
    }


    public boolean isInOddMonthWindow2(LocalDateTime date) {
        int month = date.getMonthValue();

        // Find the current odd month or next upcoming odd month
        int oddMonth = (month % 2 == 1) ? month : month + 1;
        if (oddMonth > 12) {
            oddMonth = 11; // cap at November
        }

        int year = date.getYear();
        LocalDateTime startOfOddMonth = LocalDateTime.of(year, oddMonth, 1, tasksEndHour,0,0);
        LocalDateTime startWindow = startOfOddMonth.minusDays(1);             // 2 days before
        LocalDateTime endWindow = startOfOddMonth.plusWeeks(1).plusDays(2);   // 3 days after it ends

        return !date.isBefore(startWindow) && !date.isAfter(endWindow);
    }

    public boolean loadPrices() {
        try {
            //for a week before the start of the odd month until the end of the 2nd week, display ss
            hideSS = !isInOddMonthWindow2(LocalDateTime.now());
            logger.info("Should hide SS " + isInOddMonthWindow2(LocalDateTime.now()));



            WebClient webClient = new WebClient();
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.addRequestHeader("token", "123");
            String data = webClient.getPage("https://tacoshack.dev/api/saucemarket").getWebResponse().getContentAsString();

            logger.info(data);


            JSONParser jsonParser = new JSONParser();

            Object obj = jsonParser.parse(data);
            HashMap<Sauce, Integer> prices = new HashMap<>();
            HashMap<Sauce, SauceObject> SauceObjectPrices = new HashMap<>();
            HashMap<Sauce, Integer> oldPrices = Utils.loadPrices();

            Boolean hasSS = false;
            int samePriceCount = 0;

            for (Sauce sauce : Sauce.values()) {
                if (sauce == Sauce.secret_sauce  ) {
                    // if doing ss check that its still in the data
                    if (((JSONObject) obj).get(sauce.getName())!= null) {
                        hasSS = true;
                    } else {
                        continue;
                    }
                }

                int price = Integer.parseInt(((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("price").toString());
//                JSONArray history = (JSONArray) ((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("history");
                HashMap<Integer, Integer> history = Utils.loadLast(sauce, 24);
                int max = 0;
                int min = 99999999;
                double avg = 0;
                double total = 0;
                for (int i = 0; i < history.size(); i++) {
                    int value = history.get(i+1);
                    if (value > max) {
                        max = value;
                    }
                    if (value < min) {
                        min = value;
                    }
                    total += value;
                    avg = total / (i+1);
                }
                int price2 = price;
                if (oldPrices != null && oldPrices.size() > 0)
                     price2 = oldPrices.get(sauce);
                //Integer.parseInt(((JSONArray) ((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("history")).get(0).toString());

                if (price2 == price) {
                    samePriceCount++;
                }

                logger.info(sauce + " at $" + price);
                prices.put(sauce, price);
                SauceObject sauceObject = new SauceObject(sauce, price2, price);
                sauceObject.setAvg(avg);
                sauceObject.setMax(max);
                sauceObject.setMin(min);
                SauceObjectPrices.put(sauce, sauceObject);
            }

            if (samePriceCount > 4) {
                return false;
            }

            if (!hasSS) {
                prices.put(Sauce.secret_sauce, -1);
            }

            Utils.updatePrices(prices.get(Sauce.pico),
                    prices.get(Sauce.guacamole),
                    prices.get(Sauce.salsa),
                    prices.get(Sauce.hotsauce),
                    prices.get(Sauce.chipotle),
                    prices.get(Sauce.secret_sauce));

            //load the last 24 hours of sauce prices, get the max, min and average.
            //Add that to a SauceObject
            //in addSauce get that to use that and add it to the embed

            logger.info("Loading alerts");
            HashMap<String, StringBuilder> alerts = new HashMap<>();

            Map<Integer, Map<Integer, Integer>> changeCount = Utils.getChangeCount();
            Map<Integer, ChangeRange> changeRanges = new HashMap<>();

            for (Alert alert : Utils.loadAlerts()) {
                logger.info(alert);
                if (!alerts.containsKey(alert.getName())) {
                    alerts.put(alert.getName(), new StringBuilder("__Your alerts <@" + alert.getName() + "> __"));

                }

                if (alert.getType() == AlertType.drop) {
                    HashMap<Integer, Integer> saucePrices = Utils.loadLast3(Sauce.getSauce(alert.getTrigger()));
                    alerts.get(alert.getName()).append(printDrop(saucePrices, Sauce.getSauce(alert.getTrigger()), alert.getName()));
                }
                if (alert.getType() == AlertType.rise) {
                    HashMap<Integer, Integer> saucePrices = Utils.loadLast3(Sauce.getSauce(alert.getTrigger()));
                    alerts.get(alert.getName()).append(printRise(saucePrices, Sauce.getSauce(alert.getTrigger()), alert.getName()));
                }
                if (alert.getType() == AlertType.high) {
                    int price = alert.getPrice();
                    alerts.get(alert.getName()).append(printHigh(prices, price, alert.getName(), alert.getTrigger()));
                }
                if (alert.getType() == AlertType.low) {
                    int price = alert.getPrice();
                    alerts.get(alert.getName()).append(printLow(prices, price, alert.getName(), alert.getTrigger()));
                }
                if (alert.getType() == AlertType.simple) {
                    HashMap<Integer, Integer> saucePrices = Utils.loadLast3(Sauce.getSauce(alert.getTrigger()));
                    alerts.get(alert.getName()).append(printSimple(saucePrices, Sauce.getSauce(alert.getTrigger()), alert));
                }

            }

            alerts.forEach((person, sb) -> {
//                if (hasPermission(person, chefRole)) {
                Profile profile = ReminderUtils.loadProfileById(person);
                boolean sleep = false;

                if (profile != null && profile.getSleepEnd() != null && profile.getSleepStart() != null) {
                    LocalTime start = profile.getSleepStart().toLocalTime();
                    LocalTime end = profile.getSleepEnd().toLocalTime();

                    LocalTime now = LocalTime.now();

                    if (start.isBefore(now)) {
                        if (start.isBefore(end)) {
                            if (end.isAfter(now)) {
                                // start at 20, end at 24, time 22
                                //in range
                                sleep = true;

                            }
                        } else {
                            // start at 20, end at 4, time 22
                            //in range
                            sleep = true;
                        }
                    }
                    if (end.isAfter(now)) {
                        if (start.isAfter(end)) {
                            // start at 20, end at 4, time 2
                            //in range
                            sleep = true;
                        }
                    }
                }


                if (!sleep) {
                    if (sb.toString().equals("__Your alerts <@" + person + "> __")) {

                        logger.info("No alerts for " + person);
                    } else {
                        sb.append("\r\n-----------------------------------\r\n");
                        String channel = Utils.loadAlerts(person).get(0).getChannel();
                        try {
                            boolean suppress = true;
                            if (channel.equals("865115506813960222")) {
                                if (hasPermission(person, chefRole)) {
                                    suppress = false;
                                }
                            } else {
                                suppress = false;
                            }
                            if (!suppress) {
                                client.getChannelById(Snowflake.of(channel)).createMessage(sb.toString()).block();
                            }
                        } catch (Exception e) {
                            printException(e);
                        }
                    }
                } else {
                    logger.info("User is in sleep mode " + person);

                }
            });


            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
            embed.color(Color.SUMMER_SKY);
            embed.title("Sauce Market");
            embed.description("------------------");

            List<SauceMarketStats> stats = Utils.loadHistoryStatsNoSecret();
            Map<String, SauceMarketStreak> streaks = new HashMap<>();// Utils.loadStreakLength();

            addSauce(SauceObjectPrices.get(Sauce.salsa), embed, stats, streaks, changeCount);
            addSauce(SauceObjectPrices.get(Sauce.hotsauce), embed, stats, streaks, changeCount);
            addSauce(SauceObjectPrices.get(Sauce.guacamole), embed, stats, streaks, changeCount);
            addSauce(SauceObjectPrices.get(Sauce.pico), embed, stats, streaks, changeCount);
            addSauce(SauceObjectPrices.get(Sauce.chipotle), embed, stats, streaks, changeCount);
            if (hasSS && !hideSS) {
                SauceObject ss = SauceObjectPrices.get(Sauce.secret_sauce);
                addSauce(ss, embed, stats, streaks, changeCount);
                int change = ss.getPrice() - ss.getOldPrice();
                try {
                    if (change > 0) {
                        // going up

                        List<String> upGifs = new ArrayList<>();
                        upGifs.add("https://tenor.com/view/kpop-demon-hunters-up-up-up-golden-kpop-demon-hunters-golden-we%27re-going-up-up-up-gif-11139720539903983313");
                        upGifs.add("https://tenor.com/view/up-higher-100k-btc-bitcoin-gif-10989690255873800070");
                        upGifs.add("https://tenor.com/view/john-john-crypto-john-the-coin-memecoin-red-candle-gif-3746997840446702790");
                        upGifs.add("https://tenor.com/view/climb-on-gif-27651199");
                        upGifs.add("https://tenor.com/view/the-price-just-went-up-dwayne-johnson-frank-jungle-cruise-price-increase-gif-16356043");
                        Random rand = new Random();
                        int randomNumber = rand.nextInt(upGifs.size());
                        client.getChannelById(Snowflake.of("840395542394568707")).createMessage(upGifs.get(randomNumber)).block();
                    }
                    if (change < 0) {
                        List<String> downGifs = new ArrayList<>();
                        downGifs.add("https://tenor.com/view/rafiki-oops-trip-fall-drop-gif-4172581");
                        downGifs.add("https://tenor.com/view/tree-killer-logging-cutting-cut-gif-11211868");
                        downGifs.add("https://tenor.com/view/catch-it-drop-pass-dropped-ball-drops-it-fumble-gif-23993182");
                        downGifs.add("https://tenor.com/view/fall-falling-gif-19993116");
                        downGifs.add("https://tenor.com/view/coles-coles-down-down-down-down-down-prices-are-down-gif-14532115");
                        downGifs.add("https://tenor.com/view/panda-gif-9601528");
                        Random rand = new Random();
                        int randomNumber = rand.nextInt(downGifs.size());
                        client.getChannelById(Snowflake.of("840395542394568707")).createMessage(downGifs.get(randomNumber)).block();
                    }
                } catch (Exception e) {
                    logger.error("Error adding gifs to channel", e);
                    printException(e);
                }
            }

            printCheap(SauceObjectPrices);

            logger.info("creating chart");
            String filename = "line_chart";
            int i = 0;
            try {
                createChart(data, filename, null);
            } catch (Exception e) {
                while (new File(filename+".png").exists()) {
                    filename = "line_chart_"+i;
                    i++;
                }
                createChart(data, filename, null);
            }
            logger.info("got chart");
            String finalFilename = filename;
            smUpdateChannels.forEach(channel -> {
                try {
                    client.getChannelById(Snowflake.of(channel)).createMessage(embed.build().asRequest()).block();
                    InputStream inputStream = null;
                    inputStream = new BufferedInputStream(new FileInputStream(finalFilename +".png"));
                    MessageCreateSpec msg = MessageCreateSpec.builder()
                            .addFile(MessageCreateFields.File.of(finalFilename +".png", inputStream))
                            .build();

                    client.getChannelById(Snowflake.of(channel)).createMessage(msg.asRequest()).block();
                } catch (Exception e) {
                    printException(e);
                }
            });


            if (hasSS && !hideSS) {
                logger.info("creating chart");
                createChart(data, "ss_chart", Sauce.secret_sauce);
                logger.info("got chart");
                smUpdateChannels.forEach(channel -> {
                    try {
                        InputStream inputStream = null;
                        inputStream = new BufferedInputStream(new FileInputStream("ss_chart.png"));
                        MessageCreateSpec msg = MessageCreateSpec.builder()
                                .addFile(MessageCreateFields.File.of("ss_chart.png", inputStream))
                                .build();

                        client.getChannelById(Snowflake.of(channel)).createMessage(msg.asRequest()).block();
                    } catch (Exception e) {
                        printException(e);
                    }
                });
            }

            logger.info("Finished");
        } catch (Exception e) {
            printException(e);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;

    }

    private void addSauce(SauceObject sauce, EmbedCreateSpec.Builder embed, List<SauceMarketStats> sauceMarketStats, Map<String, SauceMarketStreak> streaks, Map<Integer, Map<Integer, Integer>> changeCount) {
        String name = sauce.getSauce().getUppercaseName();


        int change = sauce.getPrice() - sauce.getOldPrice();
        // find the streak
        // say the odds of the streak continues
        // the odds of the price change to 15-7,6--2, -3--6, -7-15
        // price change -15 to -7 use the windows of 15 to 2 and 1 to -15
        // price change -6 to -3 use the windows of 15 to 7 and 6 to -6 and -7 to -15
        // price change -2 to 6 use the windows of 15 to 7 and 6 to -2 and -3 to -6 and -7 to -15
        // price change 7 to 15 use the windows of 15 to 0 and -1 to -15

        int finalChange = change;
//        SauceMarketStats changeStats = sauceMarketStats.stream().filter(stat -> stat.getChange() == finalChange).findFirst().orElse(null);
        String changeOdds = "";
//        if (changeStats != null) {
//            changeOdds = "\n\uD83D\uDCC8 " + changeStats.getPositive() + "%, \uD83D\uDCC9 " + changeStats.getNegative() + "%, ↔ " + changeStats.getZero() + "%";
//        }
        Map<Integer, Integer> singleChange  = changeCount.get(finalChange);

        if (finalChange >= -15 && finalChange <= -7) {
            changeOdds = buildChangeOdds(
                    singleChange,
                    Arrays.asList(new int[]{15, 2}, new int[]{1, -15}),
                    Arrays.asList("\uD83D\uDCC8 $15 to $2", "\uD83D\uDCC9 $1 to $-15")
            );
        } else if (finalChange >= -6 && finalChange <= -3) {
            changeOdds = buildChangeOdds(
                    singleChange,
                    Arrays.asList(new int[]{15, 7}, new int[]{6, -6}, new int[]{-7, -15}),
                    Arrays.asList("\uD83D\uDCC8 $15 to $7", "↔\uFE0F $6 to $-6", "\uD83D\uDCC9 $-7 to $-15")
            );
        } else if (finalChange >= -2 && finalChange <= 6) {
            changeOdds = buildChangeOdds(
                    singleChange,
                    Arrays.asList(new int[]{15, 7}, new int[]{6, -2}, new int[]{-3, -6}, new int[]{-7, -15}),
                    Arrays.asList("\uD83D\uDCC8 $15 to $7", "\uD83D\uDD3C $6 to $-2", "\uD83D\uDD3D $ -3 to $-6", "\uD83D\uDCC9 $-7 to $-15")
            );
        } else if (finalChange >= 7 && finalChange <= 15) {
            changeOdds = buildChangeOdds(
                    singleChange,
                    Arrays.asList(new int[]{15, 0}, new int[]{-1, -15}),
                    Arrays.asList("\uD83D\uDCC8 $15 to $0", "\uD83D\uDCC9 $-1 to $-15")
            );
        } else {
            changeOdds = "\nNo valid change range detected.";
        }



        String direction = " | <a:up:1015020767244714004> +$" + change;
        if (change < 0) {
            change = change * -1;
            direction = " | <a:down:1015020716929851453> -$" + change;
        }
        if (change == 0) {
            direction = " | <a:orange_dots:1015118419047235585> No Change";
        }
        DecimalFormat df = new DecimalFormat("#.##");
        String stats = "\n**Max** $" + sauce.getMax() + ", **Min** $ " + sauce.getMin() + ", **Avg** $" + df.format(sauce.getAvg()) ;

        String line = "\n------------------------------------";


        embed.addField(name, "$" + sauce.getPrice() + direction + stats + changeOdds + line, false);


//        embed.addField(sauce.getSauce().getName(), "$" + sauce.getPrice(), true);
//        embed.addField("Change", direction, false);

    }

    private static Boolean buySauce(int price, int change) {

        if (change >= -15 && change <= -7) {
            if (price <= 20) {
                return true;
            }
        } else if (change >= -6 && change <= -3) {
            if (price <= 35) {
                return true;
            }
        } else if (change >= -2 && change <= 6) {
            if (price <= 65) {
                return true;
            }
        } else if (change >= 7 && change <= 15) {
            if (price <= 80) {
                return true;
            }
        } else {
            return false;
        }

        return false;
    }


    private static Boolean sellSauce(int price, int change) {

        if (change >= -15 && change <= -7) {
            if (price > 60) {
                return true;
            }
        } else if (change >= -6 && change <= -3) {
            if (price > 80) {
                return true;
            }
        } else if (change >= -2 && change <= 6) {
            return false;
        } else if (change >= 7 && change <= 15) {
            return false;
        } else {
            return false;
        }

        return false;
    }



    private static String buildChangeOdds(
            Map<Integer, Integer> data,
            List<int[]> windows,
            List<String> labels
    ) {
        DecimalFormat df = new DecimalFormat("0.00");
        List<Double> percentages = new ArrayList<>();
        AtomicInteger total = new AtomicInteger();

        // Calculate total
        data.values().forEach(total::addAndGet);

        // Sum values in each window
        for (int[] window : windows) {
            AtomicInteger windowSum = new AtomicInteger();
            int start = window[0];
            int end = window[1];

            data.forEach((key, value) -> {
                if (key <= start && key >= end) {
                    windowSum.addAndGet(value);
                }
            });

            double percent = total.get() > 0
                    ? (double) windowSum.get() / total.get() * 100
                    : 0.0;
            percentages.add(percent);
        }

        // Build output string
        StringBuilder result = new StringBuilder("\n-----**Next Hour Odds**-----");
        for (int i = 0; i < labels.size(); i++) {
            double percentage = percentages.get(i);
            String formatted = df.format(percentage);
            String start = "";
            String end = "";

            if (percentage > 50.0) {
                start = "**";
                end = "**";
            }

            result.append("\n")
                    .append(start)
                    .append(labels.get(i))
                    .append(" | ")
                    .append(formatted)
                    .append("%")
                    .append(end);

        }


        return result.toString();
    }



    public void printCheap(HashMap<Sauce, SauceObject> prices) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);
        sb.append("<@&" + cheapPing + "> we have some cheap sauce\r\n");


        prices.forEach((sauce, sauceObject) -> {
            if (cheapPrice > sauceObject.getPrice() && sauceObject.getPrice() != -1) {
                int difference = sauceObject.getOldPrice() - sauceObject.getPrice();
                String move = " No change";
                if (difference > 0) {
                    move = " :chart_with_downwards_trend: down " + difference;
                }
                if (difference < 0) {
                    difference = difference * -1;
                    move = " :chart_with_upwards_trend: up " + difference;
                }

                sb.append("- " + sauce.getName() + " $" + sauceObject.getPrice() + move + "\n");
                cheap.set(true);
            }
        });

        if (cheap.get())
            client.getChannelById(Snowflake.of(smUpdate)).createMessage(sb.toString()).block();
        else {
            logger.info("No cheap sauce");
        }
    }

    public void startUp() {
        LocalDateTime nextRunTime = LocalDateTime.now().minusMinutes(1);
        boolean sucess = false;

        BufferedReader unlockReader = null;
        try {
            unlockReader = new BufferedReader(new FileReader(new File("priceCheck.txt")));

            String line;
            while ((line = unlockReader.readLine()) != null) {
                nextRunTime = LocalDateTime.parse(line, formatter);
            }
            unlockReader.close();
            logger.info("checking if price check needs to be done");
            if (LocalDateTime.now().isAfter(nextRunTime)) {
                sucess = loadPrices();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        start(sucess);
    }


    /**
     * Run task once a hour
     */
    public void start(boolean success) {

        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running price check");

                Utils.deleteReminder(SystemReminderType.sauce);
                boolean loadPricesSuccess = loadPrices();
                start(loadPricesSuccess);
            }

        };
        long delay = 1;
        // if was successful sleep for an hour
        //otherwise try again in 1min

        LocalDateTime now = LocalDateTime.now();
        int min = now.getMinute();
        if (success || min > 15) {
            delay = computeNextDelay();
        }


        LocalDateTime priceCheckTime = LocalDateTime.now().plusMinutes(delay);

        Utils.addReminder(SystemReminderType.sauce, Timestamp.valueOf(priceCheckTime), "", "");
        logger.info("price check at " + formatter.format(priceCheckTime));
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("priceCheck.txt"));
            writer.write(formatter.format(priceCheckTime));
            writer.close();
        } catch (IOException e) {
            printException(e);
        }

        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    private long computeNextDelay() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        LocalDateTime localNow = LocalDateTime.now();


        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);


        int min = zonedNow.getMinute();
        long delay = 60;
        if (min > startMin)
            delay = (60 + startMin) - min;
        if (min < startMin)
            delay = startMin - min;
        logger.info("running price check with delay of " + delay);

        return delay;
    }

    public void stop() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
        }
    }


    public String printLow(HashMap<Sauce, Integer> prices, int priceTrigger, String person, String sauceName) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);


        prices.forEach((sauce, price) -> {
            if (priceTrigger > price && price != -1 && sauce.getName().equals(sauceName)) {
                logger.info("price is " + price);

                sb.append("\n <a:bluedown:1015028942358454353>  " + sauce.getUppercaseName() + " is low $" + price );
                cheap.set(true);
            }
        });

        if (cheap.get())
//            client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
            return sb.toString();
        else {
            logger.info("No low sauce");
            return "";
        }
    }

    public String printSimple(HashMap<Integer, Integer> prices, Sauce sauce, Alert alert ) {

        //loop through prices
        //find current change for sauce
        // ask if should buy or sell
        // if user owns sauce and should sell, tell them to sell
        // if user does not own sauce and should buy, tell them to buy

        StringBuilder sb = new StringBuilder();
        AtomicBoolean action = new AtomicBoolean(false);

        Integer now = prices.get(0);
        Integer hour1 = prices.get(1);

        logger.info("now: " + now + ", hour + 1: " + hour1);

        if (hour1 == null) {
            hour1 = now;
        }

        Integer dif = now - hour1;
        if (sellSauce(now, dif) && alert.getPrice() == Drop.owned.getPrice()) {
            logger.info("price is +" + now + " and change is " + dif + " sell " + sauce.getUppercaseName());
            sb.append("\n <a:reddown:1015028786292592701>  **SELL** " + sauce.getUppercaseName() + " is falling $" + now +" down " + dif);
            action.set(true);
        }
        if (buySauce(now, dif) && alert.getPrice() == Drop.watchlist.getPrice()) {
            logger.info("price is +" + now + " and change is " + dif + " buy " + sauce.getUppercaseName());
            sb.append("\n <a:greenup:1015028862368878723>  **BUY** " + sauce.getUppercaseName() + " price is $"+now + " high chance price will go higher");
            action.set(true);
        }

        if (action.get())
            return sb.toString();
        else {
            logger.info("No simple actions sauce");
            return "";
        }
    }

    public String printHigh(HashMap<Sauce, Integer> prices, int priceTrigger, String person, String sauceName) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);


        prices.forEach((sauce, price) -> {
            if (priceTrigger < price && price != -1 && sauce.getName().equals(sauceName)) {
                logger.info("price is " + price);
                sb.append("\n <a:greenup:1015028862368878723>  " + sauce.getUppercaseName() + " is high $" + price );
                cheap.set(true);
            }
        });

        if (cheap.get())
            return sb.toString();
//            client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
        else {
            logger.info("No high sauce");
            return "";
        }
    }

    public String printDrop(HashMap<Integer, Integer> prices, Sauce sauce, String person) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean dropping = new AtomicBoolean(false);
        sb.append("\n <a:reddown:1015028786292592701>  " + sauce.getUppercaseName() + " is dropping");

        Integer now = prices.get(0);
        Integer hour1 = prices.get(1);
        Integer hour2 = prices.get(2);

        logger.info("now: " + now + ", hour + 1: " + hour1 + ", hour + 2:" + hour2);

        if (hour1 == null) {
            hour1 = now;
        }
        if (hour2 == null) {
            hour2 = hour1;
        }

        Integer dif = now - hour1;
        Integer dif2 = hour1 - hour2;

        if (dif < -9) {
            int drop = dif * -1;
            sb.append("\n  :small_orange_diamond:  down $" + drop + " last hour ");
            dropping.set(true);
        }
        if (dif < 0 && dif2 < 0) {
            int drop2 = (now - hour2) * -1;
            sb.append("\n  :small_orange_diamond:  down $" + drop2 + " last 2 hours");
            dropping.set(true);
        }


        if (dropping.get())
            return sb.toString() + " \n";
//             client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
        else {
            logger.info("No dropping sauce");
            return "";
        }
    }

    public String printRise(HashMap<Integer, Integer> prices, Sauce sauce, String person) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean rising = new AtomicBoolean(false);
        sb.append("\n <a:greenup:1015028862368878723>  " + sauce + " is rising");

        Integer now = prices.get(0);
        Integer hour1 = prices.get(1);
        Integer hour2 = prices.get(2);

        logger.info("now: " + now + ", hour + 1: " + hour1 + ", hour + 2:" + hour2);

        if (hour1 == null) {
            hour1 = now;
        }
//        if (hour2 == null) {
//            hour2 = hour1;
//        }

        Integer dif = now - hour1;
//        Integer dif2 = hour1 - hour2;

        if (dif > 9) {
            int rise = dif;
            sb.append("\n  :small_orange_diamond:  up $" + rise + " last hour ");
            rising.set(true);
        }
//        if (dif < 0 && dif2 < 0) {
//            int drop2 = (now - hour2) * -1;
//            sb.append("\r\n  -  down $" + drop2 + " last 2 hours");
//            dropping.set(true);
//        }


        if (rising.get())
            return sb.toString() + " \r\n";
//             client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
        else {
            logger.info("No dropping sauce");
            return "";
        }
    }

    @Override
    protected Mono<Object> doAction(Message message) {
        return Mono.empty();
    }

    /**
     *
     */

    private void createChart(String data, String name, Sauce sauce) throws IOException, ParseException {
        logger.info("getting chart data");
        XYChart chart = readData(data, sauce);
        logger.info("got chart data");
        BitmapEncoder.saveBitmap(chart, "./"+name, BitmapEncoder.BitmapFormat.PNG);

//        JFreeChart chart = ChartFactory.createXYLineChart(
//                "Sauce Market last 24 hours",
//                "Time",
//                "Price",
//                dataset,
//                PlotOrientation.VERTICAL,
//                true,
//                true,
//                false
//        );
//        logger.info("got ChartFactory");
//
//        XYPlot plot = chart.getXYPlot();
//
//        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
//
//        renderer.setSeriesPaint(0, java.awt.Color.RED);
//        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
//        renderer.setSeriesPaint(1, java.awt.Color.BLUE);
//        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
//        renderer.setSeriesPaint(2, java.awt.Color.GREEN);
//        renderer.setSeriesStroke(2, new BasicStroke(2.0f));
//        renderer.setSeriesPaint(3, java.awt.Color.CYAN);
//        renderer.setSeriesStroke(3, new BasicStroke(2.0f));
//        renderer.setSeriesPaint(4, java.awt.Color.MAGENTA);
//        renderer.setSeriesStroke(4, new BasicStroke(2.0f));
//
//        plot.setRenderer(renderer);
//        plot.setBackgroundPaint(java.awt.Color.white);
//        plot.setRangeGridlinesVisible(true);
//        plot.setDomainGridlinesVisible(true);
//
//
//        plot.setRangeGridlinesVisible(true);
//        plot.setRangeGridlinePaint(java.awt.Color.BLACK);
//
//        plot.setDomainGridlinesVisible(true);
//        plot.setDomainGridlinePaint(java.awt.Color.BLACK);
//
//
//        plot.getDomainAxis().setRange(-24, 0);
//
//        chart.getLegend().setFrame(BlockBorder.NONE);
//
//        chart.setTitle(new TextTitle("Sauce Market last 24 hours",
//                        new Font("Arial", Font.BOLD, 18)
//                )
//        );
//
//        ChartUtils.saveChartAsPNG(new File("line_chart.png"), chart, 900, 400);
    }


    private XYChart readData(String data, Sauce inputSauce) throws ParseException {
        // Create Chart
        logger.info("getting builder");
        String title = "Sauce Market";
        if (inputSauce != null ){
            title = inputSauce.getUppercaseName();
        }


        XYChart chart = new XYChartBuilder().width(900).height(600)
                .title(title + " last 48 hours")
                .xAxisTitle("Hours ago")
                .yAxisTitle("Price").build();
        logger.info("got builder");

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
//        chart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
//        chart.getStyler().setYAxisDecimalPattern("$ #,###.##");
        chart.getStyler().setPlotMargin(0);
        if (inputSauce == null || !inputSauce.equals(Sauce.secret_sauce)) {
            chart.getStyler().setYAxisMin(0d);
        }
//        chart.getStyler().setPlotContentSize(.95);


//        XYSeriesCollection dataset = new XYSeriesCollection();
        JSONParser jsonParser = new JSONParser();

        Object obj = jsonParser.parse(data);

        for (Sauce sauce : Sauce.values()) {
            if (inputSauce == null && sauce.equals(Sauce.secret_sauce)) {
                continue;
            }
            if (inputSauce!= null && !inputSauce.equals(sauce)) {
                continue;
            }
//            XYSeries series = new XYSeries(sauce.getUppercaseName());

//            int price = Integer.parseInt(((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("price").toString());
//            JSONArray history = (JSONArray) ((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("history");

            HashMap<Integer, Integer> history = Utils.loadLast(sauce, 48);
//            series.add(0, price);
            List<Integer> xData = new ArrayList<>();
            List<Integer> yData = new ArrayList<>();
//            xData.add(0);
//            yData.add(price);
            for (int i = 0; i < history.size(); i++) {
                int value = history.get(i+1);
                int position = (i) * -1;
//                series.add(position, value);
                xData.add(position);
                yData.add(value);
            }
//            dataset.addSeries(series);
            XYSeries series = chart.addSeries(sauce.getUppercaseName(), xData, yData);
            if (sauce.equals(Sauce.secret_sauce)) {
                series.setLineColor(XChartSeriesColors.MAGENTA);
                series.setMarkerColor(XChartSeriesColors.MAGENTA);

            }
        }

        return chart;
    }
}
