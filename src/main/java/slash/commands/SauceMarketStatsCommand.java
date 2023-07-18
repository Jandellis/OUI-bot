package slash.commands;

import action.reminder.ReminderUtils;
import action.reminder.model.FlexStats;
import action.sm.Utils;
import action.sm.model.SauceMarketStats;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
//import discord4j.rest.util.Color;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.HeatMapSeries;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SauceMarketStatsCommand extends SlashCommand {
    @Override
    public String getName() {
        return "saucemarket";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {


        InputStream inputStream = null;
        String chartName = "";

        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        try {
            String type = getParameter("type", "all", event);


            embed.color(discord4j.rest.util.Color.SUMMER_SKY);
            embed.title("Sauce market stats");

            if (!type.equals("all")) {

//                embed.description("Not implemented yet");
//
//                return event.reply()
//                        .withEphemeral(false)
//                        .withEmbeds(embed.build());


                List<SauceMarketStats> stats = Utils.loadHistoryStats(type);
                chartName = createChart(stats, " - "+ type);

                inputStream = new BufferedInputStream(new FileInputStream(chartName + ".png"));


                return event.reply()
                        .withEphemeral(false)
                        .withFiles(MessageCreateFields.File.of(chartName + ".png", inputStream));
            } else {

                // Maybe try https://github.com/knowm/XChart
                //https://github.com/knowm/XChart#heatmapchart
                List<SauceMarketStats> stats = Utils.loadHistoryStats();
//                StringBuilder desc = new StringBuilder();
////                desc.append("`This table shows the chance of the next hour price going up or down based on the current price change \n");
//                desc.append("`\n");
//                desc.append("╔════════╦════════════╦══════════╦═════════╦════════════════╗\n");
//                desc.append("║ Change ║    % Up    ║  % Down  ║  % Zero ║ Times Occurred ║\n");
//                for (SauceMarketStats stat : stats) {
//
//                    String change = stat.getChange() + "  ";
//                    String pos= stat.getPositive() + "  ";;
//                    String neg= stat.getNegative() + "  ";;
//                    String zero= stat.getZero() + "  ";;
//                    String times= stat.getOccurred() + "  ";;
//
//                    while (change.length() < 8) {
//                        change = " " + change;
//                    }
//                    while (pos.length() < 12) {
//                        pos = " " + pos;
//                    }
//                    while (neg.length() < 10) {
//                        neg = " " + neg;
//                    }
//                    while (zero.length() < 9) {
//                        zero = " " + zero;
//                    }
//                    while (times.length() < 16) {
//                        times = " " + times;
//                    }
//
//
//                    desc.append("╠════════╬════════════╬══════════╬═════════╬════════════════╣\n");
//                    desc.append("║"+change + "║" + pos + "║" + neg + "║" + zero + "║" + times+"║\n");
//
//                }
//                desc.append("╚════════╩════════════╩══════════╩═════════╩════════════════╝`");
//                embed.description(desc.toString());


                chartName = createChart(stats, "");

                inputStream = new BufferedInputStream(new FileInputStream(chartName + ".png"));


                return event.reply()
                        .withEphemeral(false)
                        .withFiles(MessageCreateFields.File.of(chartName + ".png", inputStream));


            }

//
//            String name = event.getInteraction().getData().member().get().user().id().asString();

        } catch (Exception e) {
//            throw new RuntimeException(e);
            printException(e, event.getClient());
        }

        return null;


    }


    private String createChart(List<SauceMarketStats> stats, String name) throws IOException {

        HeatMapChart chart = readData(stats, name);
//        logger.info("got chart data");
        String chartName = "./sm_stats";
        BitmapEncoder.saveBitmap(chart, chartName, BitmapEncoder.BitmapFormat.PNG);
        return chartName;
    }


    private HeatMapChart readData(List<SauceMarketStats> data, String name) {
        // Create Chart
//        logger.info("getting builder");
        String title = "Sauce Market Stats" + name;
        HeatMapChart chart = new HeatMapChartBuilder().width(900).height(600)
                .title(title)
//                .xAxisTitle("Time")
//                .yAxisTitle("$")
                .build();

        // Customize Chart
//        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
//        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setYAxisDecimalPattern("#,###");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setYAxisMin(0d);
        chart.getStyler().setShowValue(true);
        //https://colordesigner.io/gradient-generator

        Color[] rangeColors = {
                new Color(150, 251, 162),
                new Color(132, 229, 148),
                new Color(114, 207, 133),
                new Color(97, 185, 119),
                new Color(80, 164, 105),
                new Color(64, 143, 91),
                new Color(49, 123, 77),
                new Color(33, 103, 63)
//                new Color(18, 84, 50),
//                new Color(2, 66, 37)
        };
        chart.getStyler().setRangeColors(rangeColors);

        List<String> xData = new ArrayList<String>();

        List<String> yData = new ArrayList<String>();
//        xData.add("Change");
        xData.add("% Up");
        xData.add("% Down");
        xData.add("% No Change");
        xData.add("Times Occurred");

        List<Number[]>  changeData = new ArrayList<>();

        int column = 0;
        for (SauceMarketStats stat : data) {
            yData.add(stat.getChange() + "");
//            Number[] change = {0, column, stat.getChange()};
//            changeData.add(change);
            Number[] up = {0, column, stat.getPositive()};
            changeData.add(up);
            Number[] down = {1, column, stat.getNegative()};
            changeData.add(down);
            Number[] zero = {2, column, stat.getZero()};
            changeData.add(zero);
            Number[] count = {3, column, stat.getOccurred()};
            changeData.add(count);

            column++;
        }

        HeatMapSeries heatMapSeries = chart.addSeries("heatmap", xData, yData, changeData);
        heatMapSeries.setMax(100.0);


//        for (String id : ids) {
//
//
//            List<Timestamp> xData = new ArrayList<>();
//            List<Long> yData = new ArrayList<>();
//            String name = id;
//            Long lastValue = 0L;
//            Boolean first = true;
//            for (FlexStats dataPoint : data) {
//                if (dataPoint.getName().equalsIgnoreCase(id)) {
//                    Long value = dataPoint.getWork();
//                    if (type.equalsIgnoreCase("tips")) {
//                        value = dataPoint.getTips();
//                    }
//                    if (type.equalsIgnoreCase("donations")) {
//                        value = dataPoint.getDonations();
//                    }
//                    if (type.equalsIgnoreCase("overtime")) {
//                        value = dataPoint.getOvertime();
//                    }
//                    if (type.equalsIgnoreCase("votes")) {
//                        value = dataPoint.getVotes();
//                    }
//
//                    Timestamp position = dataPoint.getImportTime();
//                    if (value > 0 && !first) {
//                        xData.add(position);
//                        yData.add(value - lastValue);
//                    }
//                    name = dataPoint.getShackName();
//                    first = false;
//                    lastValue = value;
//                }
//            }
//            if (xData.size() > 0) {
//                chart.addSeries(name, xData, yData);
//            } else {
//                logger.info("No data for " + name);
//            }
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
