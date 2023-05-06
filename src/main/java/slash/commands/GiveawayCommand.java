package slash.commands;

import action.giveaway.model.GiveawayWinner;
import action.reminder.ReminderUtils;
import action.reminder.model.FlexStats;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.rest.util.Color;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiveawayCommand extends SlashCommand {
    @Override
    public String getName() {
        return "giveaway";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        InputStream inputStream = null;
        String chartName = "";
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        try {
            Boolean leaderboard = getParameter("leaderboard", false, event);
            String lookup = getParameter("lookup", "", event);

            String name = event.getInteraction().getData().member().get().user().id().asString();

            if (leaderboard) {
                List<GiveawayWinner> winners = ReminderUtils.loadGiveawayWins();

                embed.color(Color.SUMMER_SKY);
                embed.title("OUI Giveaway Leaderboard");

                StringBuilder value = new StringBuilder();
                int count = 0;
                for (GiveawayWinner winner : winners) {
                    count++;
                    LocalDateTime now = LocalDateTime.now();
                    winner.getLastWin().toLocalDateTime();
                    long days = Duration.between(winner.getLastWin().toLocalDateTime(), now).toDays();

                    value
                            .append("**" + count + "** <@")
                            .append(winner.getName())
                            .append("> - ")
                            .append(winner.getWins()).append(" - ").append(days).append(" days ago \n");
                    if (count == 30) {
                        break;
                    }

                }
//                embed.addField("Wins - Count - Last Win", value.toString(), true);
                embed.description("**Winner - Count - Last Win**\n" +value.toString());
//                embed.addField("test", "test", true);
            } else {
                String id = event.getInteraction().getData().member().get().user().id().asString();
                if (!lookup.isEmpty()) {
                    id = lookup;
                }
                List<String> ids = new ArrayList<>();
                ids.add(id);
                List<FlexStats> stats = ReminderUtils.loadFlexStats(0, 7, ids);
                FlexStats today = stats.get(stats.size() - 1);
                FlexStats lastWeek = stats.get(0);
                long votes = today.getVotes() - lastWeek.getVotes();
                long ot = today.getOvertime() - lastWeek.getOvertime();
                long work = today.getWork() - lastWeek.getWork();

                embed.color(Color.SUMMER_SKY);
                embed.title("Last 7 days");
                embed.description("Stats for <@" + id + ">");
                embed.addField("Votes", "**" + votes + "** (Need at least 7)", false);
                embed.addField("Overtime", "**" + ot + "** (Need at least 30)", false);
                embed.addField("Work", "**" + work + "** (Need at least 50)", false);
            }


        } catch (
                Exception e) {
//            throw new RuntimeException(e);
            printException(e, event.getClient());
        }


        //Reply to the slash command, with the name the user supplied
        return event.reply()
                .withEphemeral(false)
                .withEmbeds(embed.build());
//                .withContent("results");
    }


}
