package slash.commands;

import action.export.model.WeeklyBestData;
import action.reminder.ReminderUtils;
import action.reminder.model.FlexStats;
import action.reminder.model.ProfileStats;
import action.upgrades.model.LocationEnum;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.MessageCreateFields;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlexStatsCommand extends SlashCommand {
    @Override
    public String getName() {
        return "flex";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        InputStream inputStream = null;
        String chartName = "";
        try {
            String type = getParameter("type", "work", event);
            long days = getParameter("days", 30L, event);
            long daysAgoEnd = getParameter("end", 0L, event);
            String compare = getParameter("compare", "", event);
            long average = getParameter("average", 1L, event);

            String name = event.getInteraction().getData().member().get().user().id().asString();

            chartName = createChart(name, days, type, daysAgoEnd, compare, average);

            inputStream = new BufferedInputStream(new FileInputStream(chartName + ".png"));


        } catch (Exception e) {
//            throw new RuntimeException(e);
            printException(e, event.getClient());
        }


        //Reply to the slash command, with the name the user supplied
        return event.reply()
                .withEphemeral(false)
                .withFiles(MessageCreateFields.File.of(chartName + ".png", inputStream));
    }


    private String createChart(String name, Long days, String type, Long daysAgoEnd, String compare, Long average) throws IOException {
//        logger.info("getting chart data");
        List<String> ids = new ArrayList<>();
        ids.add(name);

        if (!compare.isEmpty()) {
            ids.addAll(Arrays.asList(compare.split(",")));
        }
        logger.info("creating chart for " + ids.toString());

        List<FlexStats> dataEnd = ReminderUtils.loadFlexStats(daysAgoEnd.intValue(), days.intValue(), ids);
        List<FlexStats> dataStart = ReminderUtils.loadFlexStats(daysAgoEnd.intValue() + average.intValue() + 1, days.intValue(), ids);

        if (dataEnd.size() != dataStart.size()) {
            //error!!
            logger.info("lists not equal");
        }
        List<FlexStats> combined = combineData(dataStart, dataEnd, ids);
        XYChart chart = readData(combined, type, ids, average);
//        logger.info("got chart data");
        String chartName = "./flex_stats" + name;
        BitmapEncoder.saveBitmap(chart, chartName, BitmapEncoder.BitmapFormat.PNG);
        return chartName;
    }

    private List<FlexStats> combineData(List<FlexStats> dataStart, List<FlexStats> dataEnd, List<String> ids) {

        List<FlexStats> dataCombined = new ArrayList<>();




//        List<FlexStats> dataStartPadded = padData(dataStart, ids);
//        List<FlexStats> EndStartPadded = padData(dataEnd, ids);

        Map<String, List<FlexStats>> mapDataStart = dataStart.stream().collect(Collectors.groupingBy(FlexStats::getName));
        Map<String, List<FlexStats>> mapDataEnd = dataEnd.stream().collect(Collectors.groupingBy(FlexStats::getName));

        for (String id : mapDataEnd.keySet()) {
            int offset = mapDataEnd.get(id).size() - mapDataStart.get(id).size();
            if (offset < 0)
                offset = 0;


            for (int i = 0; i + offset< mapDataEnd.get(id).size() && i < mapDataStart.get(id).size(); i++) {
                FlexStats dataPointEnd = mapDataEnd.get(id).get(i + offset);
                FlexStats dataPointStart = mapDataStart.get(id).get(i);
                FlexStats dataPoint = new FlexStats(dataPointEnd.getName());
                dataPoint.setDonations(dataPointEnd.getDonations() - dataPointStart.getDonations());
                dataPoint.setTips(dataPointEnd.getTips() - dataPointStart.getTips());
                dataPoint.setWork(dataPointEnd.getWork() - dataPointStart.getWork());
                dataPoint.setOvertime(dataPointEnd.getOvertime() - dataPointStart.getOvertime());
                dataPoint.setVotes(dataPointEnd.getVotes() - dataPointStart.getVotes());
                dataPoint.setImportTime(dataPointEnd.getImportTime());
                dataPoint.setShackName(dataPointEnd.getShackName());
                dataCombined.add(dataPoint);
            }
        }
        return dataCombined;
    }

    private List<FlexStats> padData(List<FlexStats> data, List<String> ids){

        Map<Timestamp, List<FlexStats>> mapData = data.stream().collect(Collectors.groupingBy(FlexStats::getImportTime));
        List<FlexStats> paddedData = new ArrayList<>();
        for (Timestamp time : mapData.keySet()) {
            for (String id : ids) {
                boolean found = false;


                for (FlexStats stats : mapData.get(time)) {
                    if (stats.getName().equals(id)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    FlexStats flexStats = new FlexStats(time, id, id, 0L, 0L, 0L, 0L, 0L);
                    mapData.get(time).add(flexStats);
                }
            }
            paddedData.addAll(mapData.get(time));
        }
        return paddedData;
    }


    private XYChart readData(List<FlexStats> data, String type, List<String> ids, Long average) {
        // Create Chart
//        logger.info("getting builder");
        String title = "Flex stats for " + type;
        XYChart chart = new XYChartBuilder().width(900).height(600)
                .title(title)
                .xAxisTitle("Time")
//                .yAxisTitle("$")
                .build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setYAxisDecimalPattern("#,###");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setYAxisMin(0d);


        for (String id : ids) {


            List<Timestamp> xData = new ArrayList<>();
            List<Long> yData = new ArrayList<>();
            String name = id;
            for (FlexStats dataPoint : data) {
                if (dataPoint.getName().equalsIgnoreCase(id)) {
                    Long value = dataPoint.getWork();
                    if (type.equalsIgnoreCase("tips")) {
                        value = dataPoint.getTips();
                    }
                    if (type.equalsIgnoreCase("donations")) {
                        value = dataPoint.getDonations();
                    }
                    if (type.equalsIgnoreCase("overtime")) {
                        value = dataPoint.getOvertime();
                    }
                    if (type.equalsIgnoreCase("votes")) {
                        value = dataPoint.getVotes();
                    }

                    Timestamp position = dataPoint.getImportTime();
                    xData.add(position);
                    yData.add(value/average);
                    name = dataPoint.getShackName();
                }
            }
            if (xData.size() > 0) {
                chart.addSeries(name, xData, yData);
            } else {
                logger.info("No data for " + name);
            }
        }

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
