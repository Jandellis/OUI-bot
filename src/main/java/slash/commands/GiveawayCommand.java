package slash.commands;

import action.giveaway.model.GiveawayLog;
import action.giveaway.model.GiveawayWinner;
import action.reminder.ReminderUtils;
import action.reminder.model.FlexStats;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
            Boolean history = getParameter("history", false, event);
            String lookup = getParameter("lookup", "", event);
            if (event.getInteraction().getData().member().toOptional().isPresent()) {

                String name = event.getInteraction().getData().member().get().user().id().asString();
            }
            if (leaderboard || history) {
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
                    embed.description("**Winner - Count - Last Win**\n" + value.toString());
//                embed.addField("test", "test", true);
                } else {

                    List<GiveawayLog> winners = ReminderUtils.loadGiveawayLog();

                    embed.color(Color.SUMMER_SKY);
                    embed.title("OUI Giveaway Leaderboard Last 30 days");

                    StringBuilder value = new StringBuilder();
                    AtomicInteger count = new AtomicInteger();
                    HashMap<String, GiveawayWinner> giveawayWinnerHashMap = new HashMap<>();

                    for (GiveawayLog winner : winners) {
                        if (giveawayWinnerHashMap.containsKey(winner.getName())) {
                            giveawayWinnerHashMap.get(winner.getName()).addWin();
                        } else {
                            GiveawayWinner giveawayWinner = new GiveawayWinner(winner.getName(), 1, winner.getLastWin());
                            giveawayWinnerHashMap.put(winner.getName(), giveawayWinner);
                        }
                    }

                    List <GiveawayWinner> sortedWinners = new ArrayList<>(giveawayWinnerHashMap.values());
                    sortedWinners.sort((o1, o2) -> {

                        if (o1.getWins() == o2.getWins()){

                            if (o1.getLastWin().after(o2.getLastWin()) )
                                return -1;
                            else
                                return 1;
                        }

                        if (o1.getWins() < o2.getWins())
                            return 1;
                        else
                            return -1;
                    });

                    sortedWinners.forEach((winner) -> {
                        count.getAndIncrement();

                                LocalDateTime now = LocalDateTime.now();
                                winner.getLastWin().toLocalDateTime();
                                long days = Duration.between(winner.getLastWin().toLocalDateTime(), now).toDays();

                                value
                                        .append("**" + count + "** <@")
                                        .append(winner.getName())
                                        .append("> - ")
                                        .append(winner.getWins()).append(" - ").append(days).append(" days ago \n");
                            }

                    );
//                embed.addField("Wins - Count - Last Win", value.toString(), true);
                    embed.description("**Winner - Count - Last Win**\n" + value.toString());
//                embed.addField("test", "test", true);
                }
            } else {
                if (event.getInteraction().getData().member().toOptional().isPresent()) {
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
