package slash.commands;

import action.export.ExportUtils;
import action.export.model.DonationLog;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FranchiseStatsCommand extends SlashCommand {
    @Override
    public String getName() {
        return "franchise";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        String franchise = getParameter("franchise", "oui", event).toUpperCase();
        String type = getParameter("type", "balance", event);
        Boolean compressGraph = getParameter("compress", true, event);
        long days = getParameter("days", 30L, event);
        long average = getParameter("average", -1L, event);

        String name = event.getInteraction().getData().member().get().user().id().asString();

        InputStream inputStream = null;
        String chartName = "";
        try {
            chartName = createChart(franchise, days, type, compressGraph, average);

            inputStream = new BufferedInputStream(new FileInputStream(chartName + ".png"));


        } catch (IOException e) {
            e.printStackTrace();
        }


        //Reply to the slash command, with the name the user supplied
        return event.reply()
                .withEphemeral(false)
                .withFiles(MessageCreateFields.File.of(chartName + ".png", inputStream));
    }

    public static class WeeklyStat {
        int year;
        int week;
        long endOfWeekBalance;
        Timestamp endOfWeekTimestamp;

        public WeeklyStat(int year, int week, long endOfWeekBalance, Timestamp endOfWeekTimestamp) {
            this.year = year;
            this.week = week;
            this.endOfWeekBalance = endOfWeekBalance;
            this.endOfWeekTimestamp = endOfWeekTimestamp;
        }
    }



    private String createChart(String name, Long days, String type, Boolean compressGraph, long average) throws IOException {
//        logger.info("getting chart data");
        List<FranchiseStats> data = ExportUtils.loadFranchiseStats(name, days.intValue());
//        List<Timestamp> reset = new ArrayList<>();
//        reset.add(Timestamp.valueOf("2025-10-03 23:33:47"));
//        reset.add(Timestamp.valueOf("2025-10-10 03:02:09"));
//        reset.add(Timestamp.valueOf("2025-11-21 02:01:34"));
        List<FranchiseStats> adjusted = new ArrayList<>();
        List<FranchiseStats> weeklyGrowthStats = new ArrayList<>();
        List<FranchiseStats> adjustedWeeklyGrowthStats = new ArrayList<>();

        if (name.equalsIgnoreCase("OUI")) {
            List<DonationLog> donations = ExportUtils.getDonationLog(days.intValue());


            long cumulativeDonations = 0;
            int donationIndex = 0;

            Long prevBalance = null;   // to detect resets
            long RESET_THRESHOLD = 10_000_000_000L;

            for (FranchiseStats stat : data) {

                Timestamp currentTime = stat.getTime();
                long currentBalance = stat.getBalance();

                // -------------------------------------------------
                // AUTO-RESET IF BALANCE DROPS SIGNIFICANTLY
                // -------------------------------------------------
                if (prevBalance != null) {
                    long drop = prevBalance - currentBalance;

                    if (drop >= RESET_THRESHOLD) {
                        // Reset donation tracking
                        cumulativeDonations = 0;

                        // Move donationIndex to first donation AFTER reset point
                        while (donationIndex < donations.size() &&
                                !donations.get(donationIndex).getDonation_time().after(currentTime))
                        {
                            donationIndex++;
                        }
                    }
                }


                // -------------------------------------------------
                // NORMAL DONATION ACCUMULATION
                // -------------------------------------------------
                while (donationIndex < donations.size() &&
                        donations.get(donationIndex).getDonation_time().getTime() <= currentTime.getTime())
                {
                    cumulativeDonations += donations.get(donationIndex).getDonation();
                    donationIndex++;
                }

                long adjustedBalance = stat.getBalance() - cumulativeDonations;

                // Create a new FranchiseStats entry with adjusted balance
                FranchiseStats adjustedStat = new FranchiseStats(
                        stat.getName(),
                        stat.getIncome(),
                        stat.getSold(),
                        adjustedBalance,
                        stat.getTime()
                );

                adjusted.add(adjustedStat);

                // update previous balance
                prevBalance = currentBalance;
            }
            Map<String, WeeklyStat> weeklyAdjusted = new TreeMap<>();
            Map<String, WeeklyStat> weekly = new TreeMap<>();
            WeekFields wf = WeekFields.ISO;

            for (FranchiseStats stat : adjusted) {

                LocalDate date = stat.getTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                int year = date.get(wf.weekBasedYear());
                int week = date.get(wf.weekOfWeekBasedYear());

                String key = year + "-" + week;

                WeeklyStat existing = weeklyAdjusted.get(key);

                if (existing == null || stat.getTime().after(existing.endOfWeekTimestamp)) {

                    weeklyAdjusted.put(key, new WeeklyStat(
                            year,
                            week,
                            stat.getBalance(),      // adjusted balance
                            stat.getTime()          // last timestamp of the week
                    ));
                }
            }


            for (FranchiseStats stat : data) {

                LocalDate date = stat.getTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                int year = date.get(wf.weekBasedYear());
                int week = date.get(wf.weekOfWeekBasedYear());

                String key = year + "-" + week;

                WeeklyStat existing = weekly.get(key);

                if (existing == null || stat.getTime().after(existing.endOfWeekTimestamp)) {

                    weekly.put(key, new WeeklyStat(
                            year,
                            week,
                            stat.getBalance(),      // adjusted balance
                            stat.getTime()          // last timestamp of the week
                    ));
                }
            }

            WeeklyStat previousAdjusted = null;

            for (WeeklyStat current : weeklyAdjusted.values()) {
                if (previousAdjusted != null) {

                    long growth = current.endOfWeekBalance - previousAdjusted.endOfWeekBalance;

                    // Growth stored as "balance" so it plots in your existing chart
                    adjustedWeeklyGrowthStats.add(new FranchiseStats(
                            name,
                            0L,
                            0L,
                            growth,
                            current.endOfWeekTimestamp
                    ));
                }
                previousAdjusted = current;
            }


            WeeklyStat previous = null;

            for (WeeklyStat current : weekly.values()) {
                if (previous != null) {

                    long growth = current.endOfWeekBalance - previous.endOfWeekBalance;

                    // Growth stored as "balance" so it plots in your existing chart
                    weeklyGrowthStats.add(new FranchiseStats(
                            name,
                            0L,
                            0L,
                            growth,
                            current.endOfWeekTimestamp
                    ));
                }
                previous = current;
            }





        }

        //load data after the date
        //load user data
        //loop though franchise data
        // find the date range
        // look for donations in that range and keep track of the total

        //2025-08-22 08:39:15
        //if avg > 1 load data for days + avg
        if (type.equalsIgnoreCase("balanceWeekly")) {
            data = weeklyGrowthStats;
            adjusted = adjustedWeeklyGrowthStats;
        }

        XYChart chart = readData(data, name, type, compressGraph, adjusted);
//        logger.info("got chart data");
        String chartName = "./franchise_stats" + name;
        BitmapEncoder.saveBitmap(chart, chartName, BitmapEncoder.BitmapFormat.PNG);
        return chartName;
    }


    private XYChart readData(List<FranchiseStats> data, String name, String type, Boolean compressGraph, List<FranchiseStats> adjusted) {
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


        List<Timestamp> xData2 = new ArrayList<>();
        List<Long> yData2 = new ArrayList<>();
//            xData.add(0);
//            yData.add(price);
        for (FranchiseStats dataPoint : adjusted) {
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
                xData2.add(position);
                yData2.add(value);
            }
        }
//            dataset.addSeries(series);
        chart.addSeries("adjusted", xData2, yData2);

//
//        List<Timestamp> xData3 = new ArrayList<>();
//        List<Long> yData3 = new ArrayList<>();
////            xData.add(0);
////            yData.add(price);
//        for (FranchiseStats dataPoint : weekly) {
////            for (int i = 0; i < history.size(); i++) {
//            Long value = dataPoint.getBalance();
//            if (type.equalsIgnoreCase("income")) {
//                value = dataPoint.getIncome();
//            }
//            if (type.equalsIgnoreCase("sold")) {
//                value = dataPoint.getSold();
//            }
//            Timestamp position = dataPoint.getTime();
////                series.add(position, value);
//            if (value > 0) {
//                xData3.add(position);
//                yData3.add(value);
//            }
//        }
////            dataset.addSeries(series);
//            chart.addSeries("weekly", xData3, yData3);

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
