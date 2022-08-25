package action.sm;

import action.Action;
import action.sm.model.Alert;
import action.sm.model.AlertType;
import action.sm.model.SystemReminderType;
import bot.Config;
import bot.Sauce;
import bot.SauceObject;
import com.gargoylesoftware.htmlunit.WebClient;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PriceCheck extends Action {


    Config config = Config.getInstance();
    int startMin;
    String smUpdate;
    String smChannel;
    String cheapPing;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    static long chefRole;
    int cheapPrice = 45;


    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public PriceCheck() {

        smUpdate = config.get("smUpdate");
        cheapPing = config.get("cheapPing");
        smChannel = config.get("smChannel");
        startMin = Integer.parseInt(config.get("priceCheck"));
        chefRole = Long.parseLong(config.get("chefRole"));
        cheapPrice = Integer.parseInt(config.get("cheapPrice"));
    }

    public void loadPrices() {
        try {


            WebClient webClient = new WebClient();
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.addRequestHeader("token", "123");
            String data = webClient.getPage("https://tacoshack.online/api/saucemarket").getWebResponse().getContentAsString();

            logger.info(data);


            JSONParser jsonParser = new JSONParser();

            Object obj = jsonParser.parse(data);
            HashMap<Sauce, Integer> prices = new HashMap<>();
            HashMap<Sauce, SauceObject> SauceObjectPrices = new HashMap<>();

            for (Sauce sauce : Sauce.values()) {
                int price = Integer.parseInt(((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("price").toString());
                int price2 = Integer.parseInt(((JSONArray) ((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("history")).get(0).toString());

                logger.info(sauce + " at $" + price);
                prices.put(sauce, price);
                SauceObject sauceObject = new SauceObject(sauce, price2, price);
                SauceObjectPrices.put(sauce, sauceObject);
            }


            Utils.updatePrices(prices.get(Sauce.pico),
                    prices.get(Sauce.guacamole),
                    prices.get(Sauce.salsa),
                    prices.get(Sauce.hotsauce),
                    prices.get(Sauce.chipotle));

            logger.info("Loading alerts");
            HashMap<String, StringBuilder> alerts = new HashMap<>();

            for (Alert alert : Utils.loadAlerts()) {
                logger.info(alert);
                if (!alerts.containsKey(alert.getName())) {
                    alerts.put(alert.getName(), new StringBuilder("__Your alerts <@" + alert.getName() + "> __\r\n"));

                }

                if (alert.getType() == AlertType.drop) {
                    HashMap<Integer, Integer> saucePrices = Utils.loadLast3(Sauce.getSauce(alert.getTrigger()));
                    alerts.get(alert.getName()).append(printDrop(saucePrices, Sauce.getSauce(alert.getTrigger()), alert.getName()));
                }
                if (alert.getType() == AlertType.high) {
                    int price = alert.getPrice();
                    alerts.get(alert.getName()).append(printHigh(prices, price, alert.getName(), alert.getTrigger()));
                }
                if (alert.getType() == AlertType.low) {
                    int price = alert.getPrice();
                    alerts.get(alert.getName()).append(printLow(prices, price, alert.getName(), alert.getTrigger()));
                }

            }

            alerts.forEach((person, sb) -> {
                if (hasPermission(person, chefRole)) {
                    if (sb.toString().equals("__Your alerts <@" + person + "> __\r\n")) {

                        logger.info("No alerts for " + person);
                    } else {
                        sb.append("\r\n-----------------------------------\r\n");

                        client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
                    }
                } else {
                    logger.info("User does not have chef role " + person);

                }
            });


            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
            embed.color(Color.SUMMER_SKY);
            embed.title("Sauce Market");
            embed.description("------------------");

            addSauce(SauceObjectPrices.get(Sauce.salsa), embed);
            addSauce(SauceObjectPrices.get(Sauce.hotsauce), embed);
            addSauce(SauceObjectPrices.get(Sauce.guacamole), embed);
            addSauce(SauceObjectPrices.get(Sauce.pico), embed);
            addSauce(SauceObjectPrices.get(Sauce.chipotle), embed);

            client.getChannelById(Snowflake.of(smUpdate)).createMessage(embed.build().asRequest()).block();

            printCheap(SauceObjectPrices);
            logger.info("creating chart");
            createChart(data);
            logger.info("got chart");
            InputStream inputStream = new BufferedInputStream(new FileInputStream("line_chart.png"));
            MessageCreateSpec msg = MessageCreateSpec.builder()
                    .addFile("line_chart.png", inputStream)
                    .build();

            client.getChannelById(Snowflake.of(smUpdate)).createMessage(msg.asRequest()).block();


            logger.info("Finished");
        } catch (Exception e) {
            printException(e);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void addSauce(SauceObject sauce, EmbedCreateSpec.Builder embed) {
        String name = sauce.getSauce().getUppercaseName();


        int change = sauce.getPrice() - sauce.getOldPrice();
        String direction = " | :white_check_mark: +$" + change;
        if (change < 0) {
            change = change * -1;
            direction = " | :small_red_triangle_down: -$" + change;
        }
        if (change == 0) {
            direction = " | :black_small_square: No Change";
        }

        String line = "\r\n------------------";


        embed.addField(name, "$" + sauce.getPrice() + direction + line, false);


//        embed.addField(sauce.getSauce().getName(), "$" + sauce.getPrice(), true);
//        embed.addField("Change", direction, false);

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

                sb.append(" - " + sauce.getName() + " $" + sauceObject.getPrice() + move + "\r\n");
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
                loadPrices();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }


    /**
     * Run task once a hour
     */
    public void start() {

        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running price check");

                Utils.deleteReminder(SystemReminderType.sauce);
                loadPrices();
                start();
            }

        };
        long delay = computeNextDelay();


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

                sb.append(" :small_blue_diamond:  " + sauce.getName() + " is low $" + price + "\r\n");
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

    public String printHigh(HashMap<Sauce, Integer> prices, int priceTrigger, String person, String sauceName) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);


        prices.forEach((sauce, price) -> {
            if (priceTrigger < price && price != -1 && sauce.getName().equals(sauceName)) {
                logger.info("price is " + price);
                sb.append(" :small_orange_diamond:  " + sauce.getName() + " is high $" + price + "\r\n");
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
        sb.append(" :small_blue_diamond:  " + sauce + " is dropping");

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
            sb.append("\r\n  -  down $" + drop + " last hour ");
            dropping.set(true);
        }
        if (dif < 0 && dif2 < 0) {
            int drop2 = (now - hour2) * -1;
            sb.append("\r\n  -  down $" + drop2 + " last 2 hours");
            dropping.set(true);
        }


        if (dropping.get())
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

    private void createChart(String data) throws IOException, ParseException {
        logger.info("getting chart data");
        XYChart chart = readData(data);
        logger.info("got chart data");
        BitmapEncoder.saveBitmap(chart, "./line_chart", BitmapEncoder.BitmapFormat.PNG);

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


    private XYChart readData(String data) throws ParseException {
        // Create Chart
        logger.info("getting builder");
        XYChart chart = new XYChartBuilder().width(900).height(600)
                .title("Sauce Market last 24 hours")
                .xAxisTitle("Hours ago")
                .yAxisTitle("Price").build();
        logger.info("got builder");

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
//        chart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
//        chart.getStyler().setYAxisDecimalPattern("$ #,###.##");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setYAxisMin(0d);
//        chart.getStyler().setPlotContentSize(.95);


//        XYSeriesCollection dataset = new XYSeriesCollection();
        JSONParser jsonParser = new JSONParser();

        Object obj = jsonParser.parse(data);

        for (Sauce sauce : Sauce.values()) {
//            XYSeries series = new XYSeries(sauce.getUppercaseName());

            int price = Integer.parseInt(((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("price").toString());
            JSONArray history = (JSONArray) ((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("history");

//            series.add(0, price);
            List<Integer> xData = new ArrayList<>();
            List<Integer> yData = new ArrayList<>();
            xData.add(0);
            yData.add(price);
            for (int i = 0; i < history.size(); i++) {
                int value = Integer.parseInt(history.get(i).toString());
                int position = (i + 1) * -1;
//                series.add(position, value);
                xData.add(position);
                yData.add(value);
            }
//            dataset.addSeries(series);
            chart.addSeries(sauce.getUppercaseName(), xData, yData);
        }

        return chart;
    }
}
