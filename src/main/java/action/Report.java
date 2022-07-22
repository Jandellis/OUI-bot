package action;

import bot.Clean;
import bot.KickMember;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Report extends Action {


    String guildId;
    Long finalWarning;
    String hitThread;
    String warnChannel;

    Long immunityId;
    Long firstWarning;
    Long secondWarning;

    public Report() {
        param = "ouireport";
        guildId = config.get("guildId");

        hitThread = config.get("hitThread");
        immunityId = Long.parseLong(config.get("immunityId"));
        firstWarning = Long.parseLong(config.get("firstWarning"));
        secondWarning = Long.parseLong(config.get("secondWarning"));
        finalWarning = Long.parseLong(config.get("finalWarning"));
    }

    @Override
    public Mono<Object> doAction(Message message) {

        //create various report
        return Mono.empty();
//        return gateway.on(MessageCreateEvent.class, event -> {
//
//            String action = getAction(message);
//            if (action != null) {
//                int worklimit = 5;
//                int uncleanlimit = 7;
////
////                return message.getChannel().flatMap(channel -> {
////
////                    List<KickMember> kickMemberList = new ArrayList<>();
////                    try {
////                        kickMemberList = Clean.mainNoImport("historic.csv");
////                        logger.info("processed data");
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
////
////                    if (action.equals("report")) {
////
////                        StringBuilder workList = new StringBuilder();
////                        workList.append("**No work for " + worklimit + " days** \r\n");
////                        StringBuilder uncleanList = new StringBuilder();
////                        uncleanList.append("**Unhappy for " + uncleanlimit + " days** \r\n");
////                        for (KickMember kickMember : kickMemberList) {
////
////                            if (kickMember.getDaysNoWork() >= worklimit) {
////                                workList.append("<@" + kickMember.getId() + "> \r\n");
////                            }
////
////                            if (kickMember.getDaysUnhappy() >= uncleanlimit) {
////                                uncleanList.append("<@" + kickMember.getId() + "> \r\n");
////                            }
////                        }
////                        channel.createMessage(workList.toString()).block();
////                        logger.info(workList);
////
////                        channel.createMessage(uncleanList.toString()).block();
////                        logger.info(uncleanList);
////                    }
////                    if (action.equals("clean")) {
////                        StringBuilder list = new StringBuilder();
////                        list.append("**Unhappy for days** \r\n");
////                        int count = 0;
////                        kickMemberList.sort((o1, o2) -> {
////                            if (o1.getDaysUnhappy() == o2.getDaysUnhappy())
////                                return 0;
////                            if (o1.getDaysUnhappy() < o2.getDaysUnhappy())
////                                return 1;
////                            else
////                                return -1;
////                        });
////                        for (KickMember kickMember : kickMemberList) {
////                            if (kickMember.getDaysUnhappy() > 0) {
////                                count++;
////                                list.append("<@" + kickMember.getId() + "> - " + kickMember.getDaysUnhappy() + "\r\n");
////                                if (count == 49) {
////                                    channel.createMessage(list.toString()).block();
////                                    list = new StringBuilder();
////                                    count = 0;
////                                }
////                            }
////                        }
////                        channel.createMessage(list.toString()).block();
////
////                    }
////                    if (action.equals("work")) {
////                        StringBuilder list = new StringBuilder();
////                        list.append("**No work for days** \r\n");
////                        int count = 0;
////                        kickMemberList.sort((o1, o2) -> {
////                            if (Objects.equals(o1.getDaysNoWork(), o2.getDaysNoWork()))
////                                return 0;
////                            if (o1.getDaysNoWork() < o2.getDaysNoWork())
////                                return 1;
////                            else
////                                return -1;
////                        });
////                        for (KickMember kickMember : kickMemberList) {
////                            if (kickMember.getDaysNoWork() > 0) {
////                                count++;
////                                list.append("<@" + kickMember.getId() + "> - " + kickMember.getDaysNoWork() + "\r\n");
////                                if (count == 49) {
////                                    channel.createMessage(list.toString()).block();
////                                    list = new StringBuilder();
////                                    count = 0;
////                                }
////                            }
////                        }
////                        channel.createMessage(list.toString()).block();
////                    }
////
////
////                    return channel.createMessage("Done");
////                });
//            }
//
//            return Mono.empty();
    }
}
