package slash.commands;

import action.export.ExportUtils;
import action.export.model.FranchiseStats;
import action.reminder.ReminderUtils;
import action.reminder.model.ProfileStats;
import action.upgrades.model.LocationEnum;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.MessageCreateFields;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FranchiseStatsCommand extends SlashCommand {
    @Override
    public String getName() {
        return "franchise";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        String franchise = getParameter("franchise", "oui", event).toUpperCase();
//        Optional<String> locationPresent = event.getOption("location")
//                .flatMap(ApplicationCommandInteractionOption::getValue)
//                .map(ApplicationCommandInteractionOptionValue::asString);
//        if (locationPresent.isPresent()) {
//            location = LocationEnum.getLocation(locationPresent.get());
//        }

        String type = getParameter("type", "balance", event);
//        Optional<String> typePresent = event.getOption("type")
//                .flatMap(ApplicationCommandInteractionOption::getValue)
//                .map(ApplicationCommandInteractionOptionValue::asString);
//        if (typePresent.isPresent()) {
//            type = typePresent.get();
//        }

        Boolean compressGraph = getParameter("compress", true, event);
//        Optional<Boolean> compressGraphPresent = event.getOption("compressGraph")
//                .flatMap(ApplicationCommandInteractionOption::getValue)
//                .map(ApplicationCommandInteractionOptionValue::asBoolean);
//        if (compressGraphPresent.isPresent()) {
//            compressGraph = compressGraphPresent.get();
//        }

        long days = getParameter("days", 30L, event);

//        Optional<Long> daysPresent = event.getOption("days")
//                .flatMap(ApplicationCommandInteractionOption::getValue)
//                .map(ApplicationCommandInteractionOptionValue::asLong);
//        if (daysPresent.isPresent()) {
//            days = daysPresent.get();
//        }
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
            chartName = createChart(franchise, days, type, compressGraph);

            inputStream = new BufferedInputStream(new FileInputStream(chartName + ".png"));


        } catch (IOException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        }


        //Reply to the slash command, with the name the user supplied
        return event.reply()
                .withEphemeral(false)
                .withFiles(MessageCreateFields.File.of(chartName + ".png", inputStream));
    }


    private String createChart(String name, Long days, String type, Boolean compressGraph) throws IOException {
//        logger.info("getting chart data");
        List<FranchiseStats> data = ExportUtils.loadFranchiseStats(name, days.intValue());
        //if avg > 1 load data for days + avg

        XYChart chart = readData(data, name, type, compressGraph);
//        logger.info("got chart data");
        String chartName = "./franchise_stats" + name;
        BitmapEncoder.saveBitmap(chart, chartName, BitmapEncoder.BitmapFormat.PNG);
        return chartName;
    }


    private XYChart readData(List<FranchiseStats> data, String name, String type, Boolean compressGraph) {
        // Create Chart
//        logger.info("getting builder");
        String title = "Balance for " + name;
        String dollarSign = "$";
        if (type.equalsIgnoreCase("income")) {
            title = "Income for " + name;
        }
        if (type.equalsIgnoreCase("sold")) {
            title = "Sold for " + name;
            dollarSign = "";
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
        chart.getStyler().setYAxisDecimalPattern(dollarSign+"#,###");
        chart.getStyler().setPlotMargin(0);
        if (!compressGraph) {
            chart.getStyler().setYAxisMin(0d);
        }
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
            for (FranchiseStats dataPoint : data) {
//            for (int i = 0; i < history.size(); i++) {
                Long value = dataPoint.getBalance();
                if (type.equalsIgnoreCase("income")) {
                    value = dataPoint.getIncome();
                }
                if (type.equalsIgnoreCase("sold")) {
                    value = dataPoint.getSold();
                }
                Timestamp position = dataPoint.getTime();
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
