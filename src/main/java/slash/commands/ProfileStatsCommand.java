package slash.commands;

import action.reminder.DoReminder;
import action.reminder.ReminderType;
import action.reminder.ReminderUtils;
import action.reminder.model.Profile;
import action.reminder.model.ProfileStats;
import action.reminder.model.Reminder;
import action.upgrades.model.LocationEnum;
import bot.Sauce;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileStatsCommand implements SlashCommand {
    @Override
    public String getName() {
        return "profile";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        LocationEnum location = LocationEnum.mall;
        Optional<String> locationPresent = event.getOption("location")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString);
        if (locationPresent.isPresent()) {
            location = LocationEnum.getLocation(locationPresent.get());
        }

        String type = "balance";
        Optional<String> typePresent = event.getOption("type")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString);
        if (typePresent.isPresent()) {
            type = typePresent.get();
        }

        long days = 30;
        Optional<Long> daysPresent = event.getOption("days")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong);
        if (daysPresent.isPresent()) {
            days = daysPresent.get();
        }
//
//        String response = "Posted ";
//        if (ping) {
//            response = response + " <@465668805448957952>";
//        }


//        react(message, profile);
        String name = event.getInteraction().getData().member().get().user().id().asString();
//        int days = 7;
//        LocationEnum location = LocationEnum.mall;

        InputStream inputStream = null;
        String chartName = "";
        try {
            chartName = createChart(name, days, location, type);

            inputStream = new BufferedInputStream(new FileInputStream(chartName + ".png"));


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        //Reply to the slash command, with the name the user supplied
        return event.reply()
                .withEphemeral(false)
                .withFiles(MessageCreateFields.File.of(chartName + ".png", inputStream));
    }


    private String createChart(String name, Long days, LocationEnum location, String type) throws IOException, ParseException {
//        logger.info("getting chart data");
        List<ProfileStats> data = ReminderUtils.loadProfileStats(name, days.intValue(), location);
        XYChart chart = readData(data, location, type);
//        logger.info("got chart data");
        String chartName = "./profile_stats" + name;
        BitmapEncoder.saveBitmap(chart, chartName, BitmapEncoder.BitmapFormat.PNG);
        return chartName;
    }


    private XYChart readData(List<ProfileStats> data, LocationEnum location, String type) throws ParseException {
        // Create Chart
//        logger.info("getting builder");
        String title = "Balance for " + location.getName();
        if (type.equalsIgnoreCase("income")) {
            title = "Income for " + location.getName();
        }
        XYChart chart = new XYChartBuilder().width(900).height(600)
                .title(title)
                .xAxisTitle("Time")
//                .yAxisTitle("$")
                .build();
//        logger.info("got builder");

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
//        chart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
        chart.getStyler().setYAxisDecimalPattern("$#,###");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setYAxisMin(0d);
//        chart.getStyler().setPlotContentSize(.95);


//        XYSeriesCollection dataset = new XYSeriesCollection();
//        JSONParser jsonParser = new JSONParser();

//        Object obj = jsonParser.parse(data);

//        for (Sauce sauce : Sauce.values()) {
//            XYSeries series = new XYSeries(sauce.getUppercaseName());

//            Long price = Integer.parseInt(((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("price").toString());
//            JSONArray history = (JSONArray) ((JSONObject) ((JSONObject) obj).get(sauce.getName())).get("history");

//            series.add(0, price);
        List<Timestamp> xData = new ArrayList<>();
        List<Long> yData = new ArrayList<>();
//            xData.add(0);
//            yData.add(price);
        for (ProfileStats dataPoint : data) {
//            for (int i = 0; i < history.size(); i++) {
            Long value = dataPoint.getBalance();
            if (type.equalsIgnoreCase("income")) {
                value = dataPoint.getIncome();
            }
            Timestamp position = dataPoint.getImportTime();
//                series.add(position, value);
            if (value > 0) {
                xData.add(position);
                yData.add(value);
            }
        }
//            dataset.addSeries(series);
        chart.addSeries(type, xData, yData);
//        }

        return chart;
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
}
