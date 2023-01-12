package action.sm;

import action.Action;
import action.reminder.EmbedAction;
import bot.Sauce;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateAlerts extends Action implements EmbedAction {

    String tacoBot = "490707751832649738";
    List<String> smChannelList;
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    public UpdateAlerts() {
//        param = "cySmDrop";
        smChannelList = Arrays.asList(config.get("smChannelList").split(","));
    }


    @Override
    public Mono<Object> doAction(Message message) {
        return doAction(message, true);
    }


    public Mono<Object> doAction(Message message, boolean checkEmbeds) {
        AtomicBoolean watched = new AtomicBoolean(false);
        try {
            smChannelList.forEach(channel -> {
                if (message.getChannelId().asString().equals(channel)) {
                    watched.set(true);
                }
            });
            if (watched.get()) {
                if (message.getData().author().id().asString().equals(tacoBot)) {


                    List<EmbedData> embedData;
                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0){

                        embedData = checkEmbeds(message);
                    } else {
                        embedData = message.getData().embeds();
                    }
                    handleEmbedAction(message, embedData);

                    }
                }
//            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }


    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {

        try {
            for (EmbedData embed : embedData) {

                if (embed.author().toOptional().isPresent()) {
                    EmbedAuthorData authorData = embed.author().get();

                    if (authorData.name().get().startsWith("Your Sauces")) {
                        String id = null;
                        if (authorData.iconUrl().isAbsent()) {
                            //message.getChannel().block().createMessage("Sorry unable to update your sauces. If you add an avatar i will be able to update them").block();
                        } else {
                            id = authorData.iconUrl().get().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];
                        }

                        if (id == null) {
                            id = getId(message, embed);
                        }
                        String desc = embed.description().get();
                        List<Sauce> sauces = new ArrayList<>();

                        for (Sauce sauce : Sauce.values()) {
                            if (desc.toLowerCase().contains(sauce.getName())) {
                                sauces.add(sauce);
                            }
                        }

                        try {
                            HashMap<Sauce, Integer> totalProfit = new HashMap<>();
                            HashMap<Sauce, Integer> totalSauces = new HashMap<>();
                            String[] lines = desc.split("\n");
                            for (String line : lines) {
                                if (!line.startsWith("---") && !line.startsWith("```ID")) {
                                    line = line.replace("     ", "  ");
                                    line = line.replace("    ", "  ");
                                    line = line.replace("   ", "  ");
//                                                line = line.replace("   ", "  ");
//                                                line = line.replace("   ", "  ");


                                    String[] entries = line.split("  ");
                                    if (entries.length >= 5) {
                                        Sauce sauce = Sauce.getSauce(entries[1]);
                                        int count = Integer.parseInt(entries[2]);
                                        int price = Integer.parseInt(entries[3].replace("$", "").split(" ")[0]);
                                        int totalCost = count * price;
                                        if (!totalProfit.containsKey(sauce)) {
                                            totalProfit.put(sauce, totalCost);
                                            totalSauces.put(sauce, count);
                                        } else {
                                            totalProfit.put(sauce, totalProfit.get(sauce) + totalCost);
                                            totalSauces.put(sauce, totalSauces.get(sauce) + count);
                                        }
                                    }
                                }
                            }

                            EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();
                            embedBuilder.color(Color.SUMMER_SKY);
                            embedBuilder.title("Your Sauce Portfolio");

                            HashMap<Sauce, Integer> prices = Utils.loadPrices();
                            totalProfit.forEach((sauce, payedPrice) -> {
                                int currentPrice = prices.get(sauce);
                                int totalSauce = totalSauces.get(sauce);
                                int totalSell = currentPrice * totalSauce;
                                int profit = totalSell - payedPrice;
                                embedBuilder.addField(sauce.getUppercaseName(), " - Profit if you sell now **$" +String.format("%,d",profit)+ "**\n - Total if you sell now **$" + String.format("%,d",totalSell) +"**", false);
                            });

                            message.getChannel().block().createMessage(embedBuilder.build()).block();

                        } catch (Exception e) {
                            printException(e);
                        }
                        logger.info("Updating alerts for " + id + " for " + desc);

                        Utils.addAlerts(id, sauces, message.getChannelId().asLong()+"");
                        if (sauces.isEmpty()) {
                            message.getChannel().block().createMessage("Alerts cleared").block();
                        } else {
                            StringBuilder sb = new StringBuilder("Updated alerts");
                            for (Sauce sauce : sauces) {
                                sb.append("\r\n - " + sauce);
                            }
                            message.getChannel().block().createMessage(sb.toString()).block();
                        }
                    }
                } else {
                    if (embed.description().toOptional().isPresent() && embed.description().get().contains("You do not own any sauces!")) {
                        clearSauces(message, embed);
                    }
                }


            }

        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }



    private void clearSauces(Message message, EmbedData embed) {
        AtomicReference<String> userId = new AtomicReference<>("");
        userId.set(getId(message, embed));

        if (userId.get().equals("")) {
            List<MessageData> historic = getMessagesOfChannel(message);
            historic.forEach(messageData -> {
                Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                if (Timestamp.from(messageDataTime).before(Timestamp.from(message.getTimestamp()))) {
                    String noSpace = messageData.content().toLowerCase().replace(" ", "");
                    if (noSpace.contains("!smlist")) {
                        userId.set(messageData.author().id().toString());
                    }
                }
            });
        }
        List<Sauce> sauces = new ArrayList<>();
        Utils.addAlerts(userId.get(), sauces, message.getChannelId().asString());
        message.getChannel().block().createMessage("Alerts cleared").block();

    }

    public static List<MessageData> getMessagesOfChannel(Message message) {

        Snowflake time = Snowflake.of(message.getTimestamp().minus(15, ChronoUnit.SECONDS));
        return message.getRestChannel().getMessagesAfter(time).collectList().block();
    }


//    @Override
//    public void checkMessageAgain(Message message) {
//
//        Runnable taskWrapper = new Runnable() {
//
//            @Override
//            public void run() {
//                logger.info("checking message again");
//                Message msg = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), Snowflake.of(message.getId().asString())).block();
//                doAction(msg, false);
//            }
//
//        };
//        logger.info("checking message again in 2 sec");
//        executorService.schedule(taskWrapper, 2, TimeUnit.SECONDS);
//    }

}
