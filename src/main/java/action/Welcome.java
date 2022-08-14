package action;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public class Welcome extends Action {


    String guildId;
    long chefRole;
    String log;
    Long recruiter;

    public Welcome() {
        param = "to the franchise!";
        guildId = config.get("guildId");
        chefRole = Long.parseLong(config.get("chefRole"));
        log = config.get("log");
        recruiter = Long.parseLong(config.get("recruiter"));

    }

    @Override
    protected Mono<Object> doAction(Message message) {

        try {
            //watch for new users

            if (message.getChannelId().asString().equals(log)) {
                if (message.getContent().contains(param)) {
                    logger.info("Member joined");

                    String memberId = message.getContent().split("`")[1].split("]")[0].substring(1);

                    joined(message, memberId);

//                    logger.info("Member joined is " + memberId);
//                    client.getGuildById(Snowflake.of(guildId)).addMemberRole(
//                            Snowflake.of(memberId),
//                            Snowflake.of(chefRole),
//                            "welcome").block();
//
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("Welcome to OUI you now have access to the command `!ot` and an extra daily task in `!g`. You also should have some free menu slots.\r\n");
//                    sb.append("Please follow our rules <#840895366287065099> and look at <#887022305581092904> to see what each channel is for.\r\n");
//                    sb.append("If you have any questions reach out to a recruiter and they can help you out.");
//                    gateway.getUserById(Snowflake.of(memberId)).block().getPrivateChannel().flatMap(channel -> {
//                        channel.createMessage(sb.toString()).block();
//                        logger.info("sent DM");
//                        return Mono.empty();
//                    }).block();
//
//                    message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4D")).block();
                }
            }

            String action = getAction(message, "ouiwelcome");
            if (action != null && hasPermission(message, recruiter)) {
                if (!hasPermission(action, chefRole)) {
                    joined(message, action);
                    client.getChannelById(message.getChannelId()).createMessage("welcomes member").block();
                } else {
                    client.getChannelById(message.getChannelId()).createMessage("member has role already").block();

                }


            }


//            String action = getAction(message);
//            if (action != null) {
//                if (!message.getAuthor().isPresent()) {
//                    logger.info("no author!!! " + action);
//                } else {
//                    User author = message.getAuthor().get();
//                    client.getGuildById(Snowflake.of(guildId)).addMemberRole(
//                            author.getId(),
//                            Snowflake.of(chefRole),
//                            "welcome").block();
//
//                    logger.info("Member joined " + author.getId().asString());
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("Welcome to OUI you now have access to the command `!ot` and an extra daily task in `!g`. You also should have some free menu slots.\r\n");
//                    sb.append("Please follow our rules <#840895366287065099> and look at <#887022305581092904> to see what each channel is for.\r\n");
//                    sb.append("If you have any questions reach out to a recruiter and they can help you out.");
//
//                    author.getPrivateChannel().flatMap(channel -> {
//                        channel.createMessage(sb.toString()).block();
//                        logger.info("sent DM");
//                        return Mono.empty();
//                    }).block();
//                }
//            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

    private void joined(Message message, String memberId) {
        logger.info("Member joined is " + memberId);
        client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                Snowflake.of(memberId),
                Snowflake.of(chefRole),
                "welcome").block();

        StringBuilder sb = new StringBuilder();
        sb.append("Welcome to OUI you now have access to the command `!ot` and an extra daily task in `!g`. You also should have some free menu slots.\r\n");
        sb.append("Please follow our rules <#840895366287065099> and look at <#887022305581092904> to see what each channel is for.\r\n");
        sb.append("If you have any questions reach out to one of our recruiters <@708173299351420979> | <@762526280435367986> | <@819389319030636565> | <@292839877563908097> | <@849571699723730964> | <@697784435214516278> | <@844458414586724362> and they can help you out.\n\n");
        sb.append("I can also remind you when your work/tips and others are ready. Type ouihelp in <#841087049129656320> or one of the other play channel to find out how to set this up\n");
        sb.append("I can also help you make lots of money on the sauce market by buying sauce when its low and selling when sauce high, type ouism in <#865115506813960222> to find out how");
        gateway.getUserById(Snowflake.of(memberId)).block().getPrivateChannel().flatMap(channel -> {
            channel.createMessage(sb.toString()).block();
            logger.info("sent DM");
            return Mono.empty();
        }).block();

        message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4D")).block();
    }
}
