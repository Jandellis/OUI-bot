package action.export;

import action.Action;
import action.export.model.MemberDonations;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class Donate extends Action {


    String guildId;
    String log;
    Long finalWarning;
    Long firstWarning;
    Long secondWarning;
    String warnChannel;

    public Donate() {
        param = "has donated ";
        guildId = config.get("guildId");
        log = config.get("donationLog");
        firstWarning = Long.parseLong(config.get("firstWarning"));
        secondWarning = Long.parseLong(config.get("secondWarning"));
        finalWarning = Long.parseLong(config.get("finalWarning"));
        warnChannel = config.get("warnChannel");
    }

    @Override
    public Mono<Object> doAction(Message message) {


        try {

            if (message.getChannelId().asString().equals(log)) {
                if (message.getContent().contains(param)) {
                    logger.info("Donated to the franchise " + message.getContent());

                    String memberId = message.getContent().split("`")[1].split("]")[0].substring(1);
                    //Donated to the franchise **Top Tacos Throughout Ten Towns** `[292839877563908097]` has donated `$100`
                    long donation = Long.parseLong(message.getContent().split("`")[3].replace("`", "").replace(",", "").replace("$", ""));

                    logger.info("Member donated " + memberId + ", $" + donation);

                    ExportUtils.updateMemberDonations(memberId, donation);


                    try {
                        List<Id> roles = client.getGuildById(Snowflake.of(guildId)).getMember(Snowflake.of(memberId)).block().roles();

                        MemberDonations donations = ExportUtils.loadMemberDonations(memberId);
                        boolean check = true;
                        while (check) {
                            check = clearWarnings(memberId, donations.getDonation(), roles);
                            donations = ExportUtils.loadMemberDonations(memberId);
                        }
                    } catch (ClientException e) {
                        logger.info("user left the server " + memberId);
                    }

                    //add to total for that member

                    //if total more than 5mill and has warning
                    //lower warning level
                    //if still more than 5mill, check again. maybe have some kind of while loop or something??

                    message.addReaction(ReactionEmoji.unicode("\uD83D\uDCB0")).block();
                    return Mono.empty();
                }
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

    /**
     * returns true if it needs to be checked again
     * @param memberId
     * @param donations
     * @param roles
     * @return
     */
    private boolean clearWarnings(String memberId, long donations, List<Id> roles) {
        if (donations >= 5000000) {
            logger.info("user " + memberId + " has donated more than 5 mill, checking if warning should be removed");
            //if has warning, remove and apply the lower level
            for(Id roleId : roles) {
                if (roleId.asLong() == finalWarning) {

                    logger.info("Removed final warning");
                    client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                            Snowflake.of(memberId),
                            Snowflake.of(secondWarning),
                            "final warning clear").block();
                    client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                            Snowflake.of(memberId),
                            Snowflake.of(finalWarning),
                            "final warning clear").block();
                    Id id = Id.of(secondWarning);
                    roles.add(id);
                    roles.remove(roleId);
                    ExportUtils.clearWarning(memberId);
                    String cleared = "Thank you for your donation, your Final Warning has been cleared <@"+memberId+">";
                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(cleared).block();
                    return true;
                }


                if (roleId.asLong() == secondWarning) {
                    logger.info("Removed second warning");
                    client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                            Snowflake.of(memberId),
                            Snowflake.of(firstWarning),
                            "second warning clear").block();
                    client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                            Snowflake.of(memberId),
                            Snowflake.of(secondWarning),
                            "second warning clear").block();
                    Id id = Id.of(firstWarning);
                    roles.add(id);
                    roles.remove(roleId);
                    ExportUtils.clearWarning(memberId);
                    String cleared = "Thank you for your donation, your Second Warning has been cleared <@"+memberId+">";
                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(cleared).block();
                    return true;
                }

                if (roleId.asLong() == firstWarning) {
                    logger.info("Removed first warning");
                    client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                            Snowflake.of(memberId),
                            Snowflake.of(firstWarning),
                            "first warning clear").block();
                    String cleared = "Thank you for your donation, your First Warning has been cleared <@"+memberId+">";
                    client.getChannelById(Snowflake.of(warnChannel)).createMessage(cleared).block();
                    ExportUtils.clearWarning(memberId);
                    return false;
                }
            }
        }
        return false;
    }

}
