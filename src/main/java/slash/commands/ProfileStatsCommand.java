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
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
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

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProfileStatsCommand extends SlashCommand {
    @Override
    public String getName() {
        return "profile";
    }
    public String getCustomId() {
        return getName() + "location_select";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    private static final Map<Snowflake, Map<String, Object>> interactionData = new HashMap<>();


    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        String type = getParameter("type", "balance", event);
        Boolean compressGraph = getParameter("compress", true, event);
        long days = getParameter("days", 30L, event);

        // if location is not provided, update this to use the select menu interaction event
        String locationParameter = getParameter("location","nothing", event);
        LocationEnum location = LocationEnum.getLocation(locationParameter);
        if (location == null && !locationParameter.equals("all")) {

            Snowflake userId = event.getInteraction().getUser().getId();
            Map<String, Object> data = new HashMap<>();
            data.put("type", type);
            data.put("compressGraph", compressGraph);
            data.put("days", days);
            interactionData.put(userId, data);

            SelectMenu menu = SelectMenu.of(getCustomId(),
                            SelectMenu.Option.of("Shack", "shack"),
                            SelectMenu.Option.of("Mall", "mall"),
                            SelectMenu.Option.of("Beach", "beach"),
                            SelectMenu.Option.of("Stadium", "stadium"),
                            SelectMenu.Option.of("Amusement", "amusement"),
                            SelectMenu.Option.of("City", "city"),
                            SelectMenu.Option.of("Cantina", "cantina"),
                            SelectMenu.Option.of("Resort", "resort"),
                            SelectMenu.Option.of("Hq", "hq")
                    ).withMinValues(1) // Minimum selection
                    .withMaxValues(8); // Maximum selection

            return event.reply()
                    .withEphemeral(false)
                    .withContent("Select your locations:")
                    .withComponents(ActionRow.of(menu));
        }
        String name = event.getInteraction().getData().member().get().user().id().asString();

        InputStream inputStream = null;
        String chartName = "";
        try {
            List<LocationEnum> locations = new ArrayList<>();
            if (location != null) {
                locations.add(location);
            } else {
                locations = null;
            }
            chartName = createChart(name, days, locations, type, compressGraph);

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

    public  Mono<Void> handle(SelectMenuInteractionEvent event) {

        InputStream inputStream = null;
        String chartName = "";
        try {
            Snowflake userId = event.getInteraction().getUser().getId();

            // Retrieve stored parameters
            Map<String, Object> data = interactionData.get(userId);
            String type = (String) data.get("type");
            boolean compressGraph = (Boolean) data.get("compressGraph");
            long days = (Long) data.get("days");
            List<String> selectedLocations = event.getValues();
            List<LocationEnum> locations = selectedLocations.stream()
                    .map(LocationEnum::getLocation)
                    .collect(Collectors.toList());

            String name = event.getInteraction().getData().member().get().user().id().asString();

            chartName = createChart(name, days, locations, type, compressGraph);

            inputStream = new BufferedInputStream(new FileInputStream(chartName + ".png"));


        } catch (Exception e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        }


        //Reply to the slash command, with the name the user supplied
        return event.reply()
                .withEphemeral(false)
                .withFiles(MessageCreateFields.File.of(chartName + ".png", inputStream));
    }


    private String createChart(String name, Long days, List<LocationEnum> locations, String type, Boolean compressGraph) throws IOException {
//        logger.info("getting chart data");
        List<ProfileStats> data = ReminderUtils.loadProfileStats(name, days.intValue(), locations);
        //if avg > 1 load data for days + avg

        XYChart chart = readData(data, locations, type, compressGraph);
//        logger.info("got chart data");
        String chartName = "./profile_stats" + name;
        BitmapEncoder.saveBitmap(chart, chartName, BitmapEncoder.BitmapFormat.PNG);
        return chartName;
    }

    private XYChart readData(List<ProfileStats> data, List<LocationEnum> locations, String type, Boolean compressGraph) {
        String locationName = "all";
        if (locations != null && locations.size()== 1) {
            locationName = locations.get(0).getPrintName();
        }
        // Create Chart
        String title = "Balance for " + locationName;
        if (type.equalsIgnoreCase("income")) {
            title = "Income for " + locationName;
        }
        XYChart chart = new XYChartBuilder().width(900).height(600)
                .title(title)
                .xAxisTitle("Time")
                .build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setYAxisDecimalPattern("$#,###");
        chart.getStyler().setPlotMargin(0);
        if (!compressGraph) {
            chart.getStyler().setYAxisMin(0d);
        }

        if (locations == null || locations.size() > 1) {

            for (LocationEnum locationEnum : LocationEnum.values()) {

                List<Timestamp> xData = new ArrayList<>();
                List<Long> yData = new ArrayList<>();
                for (ProfileStats dataPoint : data) {
                    if (dataPoint.getLocation() == locationEnum) {
                        Long value = dataPoint.getBalance();
                        if (type.equalsIgnoreCase("income")) {
                            value = dataPoint.getIncome();
                        }
                        Timestamp position = dataPoint.getImportTime();
                        if (value > 0) {
                            xData.add(position);
                            yData.add(value);
                        }
                    }
                }
                if (xData.size() > 0)
                    chart.addSeries(locationEnum.getPrintName(), xData, yData);
            }
        } else {

            List<Timestamp> xData = new ArrayList<>();
            List<Long> yData = new ArrayList<>();
            for (ProfileStats dataPoint : data) {
                Long value = dataPoint.getBalance();
                if (type.equalsIgnoreCase("income")) {
                    value = dataPoint.getIncome();
                }
                Timestamp position = dataPoint.getImportTime();
                if (value > 0) {
                    xData.add(position);
                    yData.add(value);
                }
            }
            chart.addSeries(type, xData, yData);
        }

        return chart;
    }

}
