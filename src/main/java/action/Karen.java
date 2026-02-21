package action;

import action.reminder.EmbedAction;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.EmbedData;
import reactor.core.publisher.Mono;

import java.util.List;

public class Karen extends Action implements EmbedAction {


    String customerChannel;
    String customerChannel2;
    String customerChannel3;
    String customerChannel4;
    String customerChannelPond;
    String customerChannelPond2;
    String customerChannelPond3;
    String customerPing;
    String customerPingPond;
    String customerBot = "526268502932455435";
//    String customerBot = "292839877563908097";
    String office = "841078057565814845";

    String param2;
    String param3;
    String param4;
    String param5;
    String param6;
    String param7;
    String param8;

    public Karen() {
        param = "Someone left a tip for";
        param2 = "feeling a little extra hungry today";
        param3 = "I want to buy";
        param4 = "Sell Game";
        param5 = "Math Game";
        param6 = "Unscramble Modal Game";
        param8 = "Unscramble Button Game";
        param7 = "Trivia Game";
        customerChannel = "840942880775471114";
        customerChannel2 = "1162172605541920811";
        customerChannel3 = "1165959960388190259";
        customerChannel4 = "1451413132009410731";
        customerPing = "931599227824517151";
//        customerPingPond = "1242154110304915478";
//        customerChannelPond = "1229039354664452217";
//        customerChannelPond2 = "1247100235369680937";
//        customerChannelPond3 = "1247203300126756904";

    }

    @Override
    public Mono<Object> doAction(Message message) {
        try {
            if (message.getChannelId().asString().equals(customerChannel) ||
                    message.getChannelId().asString().equals(customerChannel2)||
                    message.getChannelId().asString().equals(customerChannel3)||
                    message.getChannelId().asString().equals(customerChannel4) ){
                if (message.getData().author().id().asString().equals(customerBot)) {

                    //check if its an interaction
                    //check if its an add command or a remove command


//                    if (message.getData().interaction().toOptional().isPresent()) {
//
//                        String userId = message.getData().interaction().get().user().id().toString();
//
//                        String action = message.getData().interaction().get().name();
//                        dmMe( userId+"message" + message.toString());
//                        dmMe( userId+action+"message" +message.getData().embeds().size());
//                        if (action.toLowerCase().contains("remove") || action.toLowerCase().contains("upgrade")) {
////                            try {
////                                Thread.sleep(1000);
////                            } catch (InterruptedException e) {
////                                e.printStackTrace();
////                            }
//                            List<EmbedData> embeds = checkEmbeds(message, true);
//                            if (action.toLowerCase().contains("remove")) {
//                                if (embeds.get(0).description().get().contains("You have removed your")) {
//                                    String msg = "BOO :( <@" + userId + "> removed multiplier in https://discord.com/channels/840395541791768599/" + message.getChannelId().asLong() + "/" + message.getId().asLong();
//                                    client.getChannelById(Snowflake.of(office)).createMessage(msg).block();
//                                }
//                            }
//
//                            if (action.toLowerCase().contains("upgrade")) {
//                                if (embeds.get(0).description().get().contains("Customer bot has been upgraded to a")) {
//                                    String msg = "YAY!! :) <@" + userId + "> added multiplier in https://discord.com/channels/840395541791768599/" + message.getChannelId().asLong() + "/" + message.getId().asLong();
//                                    client.getChannelById(Snowflake.of(office)).createMessage(msg).block();
//                                }
//                            }
//                        }
//
//                    }
                    if (message.getContent().contains(param2) || message.getContent().contains(param3)) {
                        logger.info("got sell");

                        return message.getChannel().flatMap(channel -> {
                            return channel.createMessage("<@&" + customerPing + "> Karen is here, with sell :speaking_head:");
                        });
                    }
                    if (message.getContent().contains(param)) {
                        logger.info("got unscramble");
                        return message.getChannel().flatMap(channel -> {
                            return channel.createMessage("<@&" + customerPing + "> Karen is here, with unscramble :speaking_head:");
                        });
                    }
                    for (Embed embed: message.getEmbeds()){
                        if (embed.getTitle().isPresent() && embed.getDescription().isPresent()) {

                            String [] question = embed.getDescription().get().split("`");
                            if (embed.getTitle().get().contains(param4)){
                                logger.info("got sell");
                                return message.getChannel().flatMap(channel -> {
                                    return channel.createMessage("<@&" + customerPing + "> Karen is here, with sell :speaking_head:");
                                });
                            }
                            if (embed.getTitle().get().contains(param5)){
                                logger.info("got math");
                                return message.getChannel().flatMap(channel -> {
                                    channel.createMessage("Bill was " + question[1]).block();
                                    return channel.createMessage("<@&" + customerPing + "> Karen is here, with math :speaking_head:");
                                });
                            }
                            if (embed.getTitle().get().contains(param6) || embed.getTitle().get().contains(param8)){
                                logger.info("got unscramble");
                                return message.getChannel().flatMap(channel -> {
                                    channel.createMessage("Unscramble was " + question[1]).block();
                                    return channel.createMessage("<@&" + customerPing + "> Karen is here, with unscramble :speaking_head:");
                                });
                            }
                            if (embed.getTitle().get().contains(param7)){
                                logger.info("got trivia");
                                return message.getChannel().flatMap(channel -> {
                                    channel.createMessage("Question was " + question[1]).block();
                                    return channel.createMessage("<@&" + customerPing + "> Karen is here, with trivia :speaking_head:");
                                });
                            }
                        }
                    }
                }
            }
//            if (message.getChannelId().asString().equals(customerChannelPond) ||
//                    message.getChannelId().asString().equals(customerChannelPond2) ||
//                    message.getChannelId().asString().equals(customerChannelPond3)) {
//
//                if (message.getData().author().id().asString().equals(customerBot)) {
//                    if (message.getContent().contains(param2) || message.getContent().contains(param3)) {
//                        logger.info("got sell");
//
//                        return message.getChannel().flatMap(channel -> {
//                            return channel.createMessage("Customer arrived with sell, got any grapes? <@&" + customerPingPond + ">");
//                        });
//                    }
//                    if (message.getContent().contains(param)) {
//                        logger.info("got unscramble");
//                        return message.getChannel().flatMap(channel -> {
//                            return channel.createMessage("Customer arrived with unscramble, got any grapes? <@&" + customerPingPond + ">");
//                        });
//                    }
//                    for (Embed embed: message.getEmbeds()){
//                        if (embed.getTitle().isPresent() && embed.getDescription().isPresent()) {
//
//                            String [] question = embed.getDescription().get().split("`");
//                            if (embed.getTitle().get().contains(param4)){
//                                logger.info("got sell");
//                                return message.getChannel().flatMap(channel -> {
//                                    return channel.createMessage("Customer arrived with sell, got any grapes? <@&" + customerPingPond + ">");
//                                });
//                            }
//                            if (embed.getTitle().get().contains(param5)){
//                                logger.info("got math");
//                                return message.getChannel().flatMap(channel -> {
//                                    channel.createMessage("Bill was " + question[1]).block();
//                                    return channel.createMessage("Customer arrived with math, got any grapes? <@&" + customerPingPond + ">");
//                                });
//                            }
//                            if (embed.getTitle().get().contains(param6) || embed.getTitle().get().contains(param8)){
//                                logger.info("got unscramble");
//                                return message.getChannel().flatMap(channel -> {
//                                    channel.createMessage("Unscramble was " + question[1]).block();
//                                    return channel.createMessage("Customer arrived with unscramble, got any grapes? <@&" + customerPingPond + ">");
//                                });
//                            }
//                            if (embed.getTitle().get().contains(param7)){
//                                logger.info("got trivia");
//                                return message.getChannel().flatMap(channel -> {
//                                    channel.createMessage("Question was " + question[1]).block();
//                                    return channel.createMessage("Customer arrived with trivia, got any grapes? <@&" + customerPingPond + ">");
//                                });
//                            }
//                        }
//                    }
//                }
//
//
//
//
//            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {

        try {
            logger.info("got karen message");
            if (message.getChannelId().asString().equals(customerChannel) ||
                    message.getChannelId().asString().equals(customerChannel2)||
                    message.getChannelId().asString().equals(customerChannel3)||
                    message.getChannelId().asString().equals(customerChannel4) ) {
                if (message.getData().author().id().asString().equals(customerBot)) {
                    logger.info("got karen message");


                    if (message.getData().interaction().toOptional().isPresent()) {
                        logger.info("got karen message");

                        String userId = message.getData().interaction().get().user().id().toString();

                        String action = message.getData().interaction().get().name();
    //                        dmMe( userId+"message" + message.toString());
    //                        dmMe( userId+action+"message" +message.getData().embeds().size());
                        if (action.toLowerCase().contains("remove") || action.toLowerCase().contains("upgrade")) {
    //                            try {
    //                                Thread.sleep(1000);
    //                            } catch (InterruptedException e) {
    //                                e.printStackTrace();
    //                            }
//                            List<EmbedData> embeds = checkEmbeds(message, true);
                            if (action.toLowerCase().contains("remove")) {
                                if (embedData.get(0).description().get().contains("You have removed your")) {
                                    String msg = "BOO :( <@" + userId + "> removed multiplier in https://discord.com/channels/840395541791768599/" + message.getChannelId().asLong() + "/" + message.getId().asLong();
                                    client.getChannelById(Snowflake.of(office)).createMessage(msg).block();
                                }
                            }

                            if (action.toLowerCase().contains("upgrade")) {
                                if (embedData.get(0).description().get().contains("Customer bot has been upgraded to a")) {
                                    String msg = "YAY!! :) <@" + userId + "> added multiplier in https://discord.com/channels/840395541791768599/" + message.getChannelId().asLong() + "/" + message.getId().asLong();
                                    client.getChannelById(Snowflake.of(office)).createMessage(msg).block();
                                }
                            }
                        }

                    }
                }
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }
}
