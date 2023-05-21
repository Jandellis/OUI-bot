package action;

import action.export.ExportUtils;
import action.export.model.WarningData;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Welcome extends Action {


    String guildId;
    long chefRole;
    String log;
    Long recruiter;

    Long immunityId;
    String giveawayRole;

    public Welcome() {
        param = "to the franchise!";
        guildId = config.get("guildId");
        chefRole = Long.parseLong(config.get("chefRole"));
        log = config.get("log");
        recruiter = Long.parseLong(config.get("recruiter"));
        immunityId = Long.parseLong(config.get("immunityId"));
        giveawayRole = config.get("giveawayRole");

    }

    @Override
    protected Mono<Object> doAction(Message message) {

        try {
            //watch for new users

            if (message.getChannelId().asString().equals(log)) {
                if (message.getContent().contains(param)) {
                    logger.info("Member joined");

                    String memberId = message.getContent().split("`")[1].split("]")[0].substring(1);

                    try {
                        joined(message, memberId);
                    } catch (Exception ignored) {
                        logger.info("user has dms closed");
                    }
                    try {
                        client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                Snowflake.of(memberId),
                                Snowflake.of(giveawayRole),
                                "Add Giveaway role").block();

                    } catch (ClientException e) {
                        //member left the server
                        logger.info("user left the server " + memberId);

                    }
                    LocalDateTime now = LocalDateTime.now().plusDays(4);
                    ExportUtils.updateWarningData(memberId + "", Timestamp.valueOf(now));

                    ExportUtils.addMember("oui");

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


            action = getAction(message, "ouihavemercy");
            if (action != null && hasPermission(message, recruiter)) {
                action = action.replace("<@", "").replace(">", "");
                if (!hasPermission(action, immunityId)) {
//                    joined(message, action);
                    String length = getAction(message, "ouihavemercy", 1);
                    int days;
                    if (length.equals("")) {
                        days = 7;
                    } else {
                        days = Integer.parseInt(length);
                    }
                    if (days > 46) {
                        client.getChannelById(message.getChannelId()).createMessage("Max is 45 days, setting to 45").block();
                        days = 45;
                    }

                    if (message.getAuthor().get().getId().asString().equals(action)) {
                        client.getChannelById(message.getChannelId()).createMessage("Sorry, you can not give mercy to yourself").block();
                        return Mono.empty();
                    }

                    WarningData warningData = ExportUtils.loadWarningData(action);
                    LocalDateTime now = LocalDateTime.now().plusDays(days);
                    warningData.setImmunityUntil(Timestamp.valueOf(now));

                    ExportUtils.updateWarningData(warningData);
                    try {
                        client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                                Snowflake.of(action),
                                Snowflake.of(immunityId),
                                "welcome").block();
                    } catch (ClientException e) {
                        logger.info("user left the server " + action);
                    }


                    client.getChannelById(message.getChannelId()).createMessage("Mercy given to <@" + action + ">for " + days + " days!").block();
                } else {
                    client.getChannelById(message.getChannelId()).createMessage("Mercy has already been given").block();
                    WarningData warningData = ExportUtils.loadWarningData(action);
                    if (warningData.getImmunityUntil() == null ) {
                        client.getChannelById(message.getChannelId()).createMessage("No end date is given... did someone give mercy by hand?").block();
                    } else {
                        LocalDateTime time = warningData.getImmunityUntil().toLocalDateTime();
                        LocalDateTime now = LocalDateTime.now();
                        long days = ChronoUnit.DAYS.between(now, time);
                        long hours = ChronoUnit.HOURS.between(now, time) % 24;

                        long minutes = ChronoUnit.MINUTES.between(now, time) % 60;
                        long seconds = ChronoUnit.SECONDS.between(now, time) % 60;

                        String display = "";
                        if (days > 0) {
                            display += days + " days, ";
                        }
                        if (hours > 0) {
                            display += hours + " hours, ";
                        }
                        if (minutes > 0) {
                            display += minutes + " minutes, ";
                        }
                        if (seconds > 0) {
                            display += seconds + " seconds";
                        }
                        if (!display.equals("")) {
                            client.getChannelById(message.getChannelId()).createMessage("Mercy will expire in " + display).block();
                        } else {
                            client.getChannelById(message.getChannelId()).createMessage("Mercy will expire after next data import").block();
                        }
                    }

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
        sb.append("Welcome to OUI you now have access to the command `/overtime` and an extra daily task in `/tasks view`. You will have some empty menu slots, fill up the empty slots with `/menu view`. \r\n");
        sb.append("To unlock the income achievements that you now will have, just buy any random upgrade.\r\n");
        sb.append("Please follow our rules <#840895366287065099> and look at <#887022305581092904> to see what each channel is for.\r\n");
        sb.append("If you have any questions reach out to one of our recruiters <@521725950736465970> | <@762526280435367986> | <@695518297168281640> | <@292839877563908097> | <@465668805448957952> | <@697784435214516278> | <@844458414586724362> and they can help you out.\n\n");
        sb.append("I can also remind you when your work/tips and others are ready. Type cyhelp in <#841087049129656320> or one of the other play channel to find out how to set this up\n");
        sb.append("I can also help you make lots of money on the sauce market by buying sauce when its low and selling when sauce high, type cysm in <#865115506813960222> to find out how");
        gateway.getUserById(Snowflake.of(memberId)).block().getPrivateChannel().flatMap(channel -> {
            channel.createMessage(sb.toString()).block();
            logger.info("sent DM");
            return Mono.empty();
        }).block();

        message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4D")).block();
    }
}
