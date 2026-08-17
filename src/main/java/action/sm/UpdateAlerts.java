package action.sm;

import action.Action;
import action.reminder.EmbedAction;
import action.reminder.ReminderUtils;
import action.reminder.model.Profile;
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
//            smChannelList.forEach(channel -> {
//                if (message.getChannelId().asString().equals(channel)) {
//                    watched.set(true);
//                }
//            });
//            if (watched.get()) {
                if (message.getData().author().id().asString().equals(tacoBot)) {


                    List<EmbedData> embedData;
                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0){

                        embedData = checkEmbeds(message);
                    } else {
                        embedData = message.getData().embeds();
                    }
                    handleEmbedAction(message, embedData);

                    }
//                }
//            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }


    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {

        try {
            if (message.getChannelId().asLong() == 889662502324039690L){
                logger.info("12345 - got message " + message.toString());
            }
            for (EmbedData embed : embedData) {
                if (message.getChannelId().asLong() == 889662502324039690L){
                    logger.info("got message " + message.toString());
                    logger.info("got embeed data " + embed.toString());
                }

                if (embed.author().toOptional().isPresent()) {
                    EmbedAuthorData authorData = embed.author().get();
                    if (message.getChannelId().asLong() == 889662502324039690L){
                        logger.info(authorData.name().get());
                    }

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
                            String name = sauce.getName();
                            if (name.equals("secret_sauce")) {
                                name = "secret";
                            }
                            if (desc.toLowerCase().contains(name)) {
                                sauces.add(sauce);
                            }
                        }

                        try {
                            HashMap<Sauce, Integer> totalProfit = new HashMap<>();
                            HashMap<Sauce, Integer> totalSauces = new HashMap<>();
                            String[] lines = desc.split("\n");
                            for (String line : lines) {
                                if (!line.startsWith("---") && !line.startsWith("```ID")) {
                                    line = line.replace("        ", "  ");
                                    line = line.replace("      ", "  ");
                                    line = line.replace("     ", "  ");
                                    line = line.replace("    ", "  ");
                                    line = line.replace("   ", "  ");
//                                                line = line.replace("   ", "  ");
//                                                line = line.replace("   ", "  ");


                                    String[] entries = line.split("  ");
                                    if (entries.length >= 5) {
                                        Sauce sauce = Sauce.getSauce(entries[1]);
                                        int count = Integer.parseInt(entries[2].replace(" ", ""));
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
                                String emote = "<a:reddown:1015028786292592701>";
                                if (profit > 0) {
                                    emote = "<a:greenup:1015028862368878723>";
                                }
                                if (profit == 0) {
                                    emote = "<a:orange_dots:1015118419047235585>";
                                }
                                embedBuilder.addField(sauce.getUppercaseName(), " "+ emote +" Profit if you sell now **$" +String.format("%,d",profit)+
                                        "**\n :small_blue_diamond: Total if you sell now **$" + String.format("%,d",totalSell) +"**", false);
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
                                sb.append("\n :small_orange_diamond: " + sauce.getUppercaseName());
                            }
                            message.getChannel().block().createMessage(sb.toString()).block();
                        }
                    }
                    if (authorData.name().get().startsWith("Purchase Completed")) {
                        try {

                            logger.info("buying sauce");
                            String id = null;
                            if (authorData.iconUrl().isAbsent()) {
                                //message.getChannel().block().createMessage("Sorry unable to update your sauces. If you add an avatar i will be able to update them").block();
                            } else {
                                id = authorData.iconUrl().get().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];
                            }

                            if (id == null) {
                                id = getId(message, embed);
                            }
                            logger.info("found id " + id);
                            String desc = embed.description().get();

                            Sauce sauce = null;

                            for (Sauce sauceValue : Sauce.values()) {
                                String name = sauceValue.getName();
                                if (name.equals("secret_sauce")) {
                                    name = "secret";
                                }
                                if (desc.toLowerCase().contains(name)) {
                                    sauce = sauceValue;
                                }
                            }
                            logger.info("found sauce  " + sauce);

                            Utils.addAlert(id, sauce, message.getChannelId().asLong() + "");
                            logger.info("added alerts");

                            Profile profile = ReminderUtils.loadProfileById(id);
                            if (profile == null) {
                                return Mono.empty();
                            }
                            react(message, profile);
                        } catch (Exception e) {
                            printException(e);
                        }

                    }
                    if (authorData.name().get().startsWith("Sale Completed")) {
                        try {
                            String id = null;
                            if (authorData.iconUrl().isAbsent()) {
                                //message.getChannel().block().createMessage("Sorry unable to update your sauces. If you add an avatar i will be able to update them").block();
                            } else {
                                id = authorData.iconUrl().get().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];
                            }

                            if (id == null) {
                                id = getId(message, embed);
                            }

                            Utils.addAlerts(id, new ArrayList<>(), message.getChannelId().asLong() + "");
                            Profile profile = ReminderUtils.loadProfileById(id);
                            if (profile == null) {
                                return Mono.empty();
                            }
                            react(message, profile);
                            message.getChannel().block().createMessage("Alerts cleared").block();
                        } catch (Exception e) {
                            printException(e);
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
