package action.export;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import action.Action;
import action.Warn;
import action.export.model.Donations;
import action.export.model.FranchiseConfig;
import action.export.model.GiveawayData;
import action.export.model.WarningData;
import action.export.model.WeeklyBestData;
import action.reminder.DoReminder;
import action.reminder.ReminderType;
import action.reminder.ReminderUtils;
import action.reminder.model.Reminder;
import bot.Clean;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.Id;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class Import2 extends Action {

    String guildId;
    Long finalWarning;
    String hitThread;
    String flex;
    Long recruiter;
    Long immunityId;
    String giveawayRole;
    String zeroVotes;
    boolean skipWarnings;

    public Import2() {
        param = "ouiimport";
        guildId = config.get("guildId");

        finalWarning = Long.parseLong(config.get("finalWarning"));
        hitThread = config.get("hitThread");
        flex = config.get("flex");
        recruiter = Long.parseLong(config.get("recruiter"));
        immunityId = Long.parseLong(config.get("immunityId"));
        skipWarnings = Boolean.parseBoolean(config.get("skipWarnings", "false"));
        giveawayRole = config.get("giveawayRole");
        zeroVotes = "1102390411252744312";
    }
//
//    @Override
//    public Mono<Object> doAction(Message message) {
//        String actionData = getAction(message);
//        if (actionData == null || !hasPermission(message, true)) return Mono.empty();
//
//        String author;
//        if (message.getInteraction().isPresent() && message.getInteraction().get().getName().contains("franchise memberdata export")) {
//            actionData = message.getId().asString();
//            author = message.getInteraction().get().getUser().getId().asString();
//
//        } else {
//            author = message.getAuthor().get().getId().asString();
//        }
//
//        Snowflake messageId = Snowflake.of(actionData);
//        int workLimit = 5;
//        int uncleanLimit = 7;
//
//        // Single reactive chain returned to caller (no subscribe here)
//        return message.getChannel()
//                .flatMap(channel -> channel.createMessage("Starting import").thenReturn(channel))
//                .flatMap(channel ->
//                        // run workflow and keep the original channel for followups
//                        runImportWorkflow(message, messageId, author, workLimit, uncleanLimit)
//                                .then(channel.createMessage("Importing Done"))
////                                .thenReturn((Object) "done")
//                                .then(Mono.empty())
//                )
//                .onErrorResume(e -> {
//                    printException(e);
//                    return message.getChannel()
//                            .flatMap(ch -> ch.createMessage("Failed to import data, please import manually"))
//                            .then(Mono.empty());
//                });
//    }

    @Override
    public Mono<Object> doAction(Message message) {
        String actionData = getAction(message);

        String author;
        if (message.getInteraction().isPresent() && message.getInteraction().get().getName().contains("franchise memberdata export")) {
            actionData = message.getId().asString();
            author = message.getInteraction().get().getUser().getId().asString();

        } else {
            author = message.getAuthor().get().getId().asString();
        }


        if (actionData != null && hasPermission(message, true)) {
            logger.info("starting import");
            Snowflake messageId = Snowflake.of(actionData);
            int workLimit = 5;
            int uncleanLimit = 7;

            message.getChannel()
                    .flatMap(channel ->
                            channel.createMessage("Starting import")
                                    .then(Mono.fromCallable(() -> {
                                        logger.info("First step");
                                        return runImportWorkflow(message, messageId, author, workLimit, uncleanLimit)
                                                .subscribe(
                                                        unused -> {},
                                                        err -> logger.error("Import workflow failed", err)
                                                );
                                    }).subscribeOn(Schedulers.boundedElastic()))
                                    .onErrorResume(e -> channel.createMessage("Failed to import data, please import manually").then(Mono.empty()))
                    )
                    .subscribe(result -> logger.info("Workflow completed"));


            return Mono.empty();
        }

        return Mono.empty();
    }


    private Mono<Void> runImportWorkflow(
            Message message,
            Snowflake messageId,
            String author,
            int workLimit,
            int uncleanLimit
    ){

        Snowflake guildId = message.getGuildId().get();
        FranchiseConfig franchiseConfig = ExportUtils.getFranchiseConfig(guildId.asString());

        return message.getChannel()
                .flatMap(channel -> channel.createMessage("Starting import 2")
                        .then(Mono.fromRunnable(() -> {
                            Instant reminderTime = message.getTimestamp().plus(4, ChronoUnit.HOURS);
                            Reminder reminder = ReminderUtils.addReminder(author, ReminderType.importData, Timestamp.from(reminderTime), channel.getId().asString());
                            new DoReminder(gateway, client).runReminder(reminder);
                        }))
                        .then(channel.getMessageById(messageId))
                        .flatMap(data -> {
                            String url = data.getData().attachments().get(0).url();
                            String franchiseName = url.substring(url.lastIndexOf('/') + 1).split("_")[0];
                            return Mono.fromCallable(() -> Clean.main(url, franchiseName + "historic.csv", workLimit, uncleanLimit, Timestamp.from(data.getTimestamp()), franchiseName))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .then(Mono.just(franchiseName));
                        }).flatMap(franchiseName ->
                                Mono.fromCallable(() -> {
                                            HashMap<Long, List<ExportData>> history = ExportUtils.loadMemberHistory(franchiseName);
                                            EmbedWithGiveaway embed = buildWeeklyEmbed(history, franchiseName);
                                            HistoryWithGiveaway historyWithGiveaway = new HistoryWithGiveaway();
                                            historyWithGiveaway.history = history;
                                            historyWithGiveaway.giveawayData = embed.giveawayData;
                                            return Tuples.of(historyWithGiveaway, embed.embed);
                                        }) // wrap blocking/CPU work
                                        .subscribeOn(Schedulers.boundedElastic())
                        )
                        .flatMap(tuple -> channel.createMessage(tuple.getT2()).thenReturn(tuple))
                        .flatMap(tuple -> client.getChannelById(Snowflake.of(franchiseConfig.getFlex()))
                                .createMessage(tuple.getT2().asRequest())
                                .thenReturn(tuple.getT1()))
                        .flatMap(historyWithGiveaway -> loadRoles(historyWithGiveaway.history, guildId).map(userRoles -> Tuples.of(historyWithGiveaway, userRoles)))
                        .flatMap(tuple -> channel.createMessage("Loaded Roles").then(Mono.just(tuple)))
                        .flatMap(tuple -> checkRoles(tuple.getT1().history, franchiseConfig.getName(), tuple.getT2(), message).then(Mono.just(tuple)))
                        .flatMap(tuple -> processOUI(tuple.getT2(), franchiseConfig, channel, tuple.getT1().giveawayData))
                        .flatMap(tuple -> channel.createMessage("Importing Done").then(Mono.just(tuple)))
                ).onErrorResume(e -> {
                    printException(e);
                    return message.getChannel().flatMap(ch ->
                            ch.createMessage("Failed to import data, please import manually")
                    ).then(Mono.empty());
                });

    }


    private Mono<Void> processOUI(HashMap<Long, List<Id>> userRoles, FranchiseConfig franchiseConfig, MessageChannel channel, List<GiveawayData> giveawayData) {
        if (!franchiseConfig.getName().equals("OUI")) {
            return Mono.empty();
        }

        // Step 1: Start import
        return channel.createMessage("Doing Warnings")
                .then(Mono.defer(() -> {
                    if (!skipWarnings) {
                        Warn warn = new Warn();
                        warn.action(gateway, client);
                        warn.doWarnings(5, 300, true, franchiseConfig, userRoles);
                    }
                    return Mono.empty();
                }))

                // Step 2: Remove Mercy
                .then(channel.createMessage("Removing Mercy"))
                .thenMany(Flux.fromIterable(ExportUtils.loadWarningDataAfterImmunity()))
                .flatMap(warningData -> {
                    RestGuild guild = client.getGuildById(Snowflake.of(guildId));
                    return guild.removeMemberRole(
                                    Snowflake.of(warningData.getName()),
                                    Snowflake.of(franchiseConfig.getImmunity()),
                                    "Mercy has expired")
                            .onErrorResume(ClientException.class, e -> {
                                logger.info("user left the server " + warningData.getName());
                                return Mono.empty();
                            })
                            .then(Mono.fromRunnable(() -> {
                                warningData.setImmunityUntil(null);
                                ExportUtils.updateWarningData(warningData);
                            }));
                })
                .then(channel.createMessage("Adding Giveaway"))

                // Step 3: Process giveaways
                .thenMany(Flux.fromIterable(giveawayData))
                .flatMap(member -> {

                    if (member.qualifiesForGiveaway()) return Mono.empty();

                    RestGuild guild = client.getGuildById(Snowflake.of(guildId));

                    Mono<Void> addRole = Mono.empty();
                    if (!hasRole(userRoles.get(member.getId()), franchiseConfig.getGiveawayRole())) {
                        addRole = guild.addMemberRole(
                                        Snowflake.of(member.getId()),
                                        Snowflake.of(franchiseConfig.getGiveawayRole()),
                                        "Add Giveaway role")
                                .onErrorResume(ClientException.class, e -> {
                                    logger.info("user left the server " + member.getId());
                                    return Mono.empty();
                                });
                    }

                    LocalDateTime now = LocalDateTime.now().plusDays(7);
                    return addRole.then(Mono.fromRunnable(() ->
                            ExportUtils.updateWarningData(member.getId() + "", Timestamp.valueOf(now))
                    ));
                })
                .then(channel.createMessage("Removing Giveaway"))

                // Step 4: Remove expired giveaways
                .thenMany(Flux.fromIterable(ExportUtils.loadWarningDataAfterGiveaway()))
                .flatMap(warningData -> {
                    RestGuild guild = client.getGuildById(Snowflake.of(guildId));
                    List<Id> roles = userRoles.get(Long.parseLong(warningData.getName()));

                    Mono<Void> removeRole = Mono.empty();
                    if (hasRole(roles, franchiseConfig.getGiveawayRole())) {
                        removeRole = guild.removeMemberRole(
                                        Snowflake.of(warningData.getName()),
                                        Snowflake.of(franchiseConfig.getGiveawayRole()),
                                        "Giveaway role has expired")
                                .onErrorResume(ClientException.class, e -> {
                                    logger.info("user left the server " + warningData.getName());
                                    return Mono.empty();
                                });
                    }

                    return removeRole.then(Mono.fromRunnable(() -> {
                        warningData.setGiveawayUntil(null);
                        ExportUtils.updateWarningData(warningData);
                    }));
                })
                .then(channel.createMessage("Doing vote check"))

                // Step 5: Zero vote check
                .thenMany(Flux.fromIterable(giveawayData))
                .flatMap(member -> {
                    RestGuild guild = client.getGuildById(Snowflake.of(guildId));
                    List<Id> roles = userRoles.get(member.getId());

                    if (member.getVotes() == 0 && !hasRole(roles, zeroVotes)) {
                        return guild.addMemberRole(Snowflake.of(member.getId()), Snowflake.of(zeroVotes), "Add 0 Vote role")
                                .onErrorResume(ClientException.class, e -> {
                                    logger.info("user left the server " + member.getId());
                                    return Mono.empty();
                                });
                    } else if (member.getVotes() > 0 && hasRole(roles, zeroVotes)) {
                        return guild.removeMemberRole(Snowflake.of(member.getId()), Snowflake.of(zeroVotes), "User has voted")
                                .onErrorResume(ClientException.class, e -> {
                                    logger.info("user left the server " + member.getId());
                                    return Mono.empty();
                                });
                    }
                    return Mono.empty();
                })
                .then(channel.createMessage("Vote check done"))
                .then();
    }


    private Mono<HashMap<Long, List<Id>>> loadRoles(
            HashMap<Long, List<ExportData>> history,
            Snowflake guildId) {

        RestGuild guild = client.getGuildById(guildId); // synchronous (OK)

        return Flux.fromIterable(history.keySet())
                .flatMap(id ->
                        guild.getMember(Snowflake.of(id))          // Mono<MemberData>
                                .map(member -> Tuples.of(id, member.roles()))
                                .onErrorResume(ClientException.class, e -> {
                                    logger.info("user left the server " + id);
                                    return Mono.empty();
                                })
                )
                .collectMap(Tuple2::getT1, Tuple2::getT2)
                .map(HashMap::new);
    }


    private EmbedWithGiveaway buildWeeklyEmbed(HashMap<Long, List<ExportData>> history, String franchiseName) {
        logger.info("processed data");
        List<WeeklyBestData> work = new ArrayList<>();
        List<WeeklyBestData> tips = new ArrayList<>();
        List<WeeklyBestData> donations = new ArrayList<>();
        List<WeeklyBestData> votes = new ArrayList<>();
        List<WeeklyBestData> overtime = new ArrayList<>();
        List<GiveawayData> giveawayData = new ArrayList<>();

        history.forEach((id, dataList) -> {
            //get old entry
            int startWork = dataList.get(0).getMember().getShifts();
            int startTips = dataList.get(0).getMember().getTips();
            long startDonations = dataList.get(0).getMember().getDonations();
            int startOvertime = dataList.get(0).getMember().getOvertime();
            int startVotes = dataList.get(0).getMember().getVotes();

            //get current entry
            int endWork = dataList.get(dataList.size() - 1).getMember().getShifts();
            int endTips = dataList.get(dataList.size() - 1).getMember().getTips();
            long endDonations = dataList.get(dataList.size() - 1).getMember().getDonations();
            int endOvertime = dataList.get(dataList.size() - 1).getMember().getOvertime();
            int endVotes = dataList.get(dataList.size() - 1).getMember().getVotes();

            work.add(new WeeklyBestData(id, endWork - startWork));
            tips.add(new WeeklyBestData(id, endTips - startTips));
            donations.add(new WeeklyBestData(id, endDonations - startDonations));
            overtime.add(new WeeklyBestData(id, endOvertime - startOvertime));
            votes.add(new WeeklyBestData(id, endVotes - startVotes));
            giveawayData.add(new GiveawayData(id, endVotes - startVotes, endOvertime - startOvertime, endWork - startWork));

        });

        HashMap<Long, List<ExportData>> historyYesterday = ExportUtils.loadMemberHistoryYesterday(franchiseName);
        HashMap<Long, WeeklyBestData> workYesterday = new HashMap<>();
        HashMap<Long, WeeklyBestData> tipsYesterday = new HashMap<>();
        HashMap<Long, WeeklyBestData> donationsYesterday = new HashMap<>();
        HashMap<Long, WeeklyBestData> overtimeYesterday = new HashMap<>();
        HashMap<Long, WeeklyBestData> votesYesterday = new HashMap<>();

        historyYesterday.forEach((id, dataList) -> {
            //get old entry
            int startWork = dataList.get(0).getMember().getShifts();
            int startTips = dataList.get(0).getMember().getTips();
            long startDonations = dataList.get(0).getMember().getDonations();
            long startOvertime = dataList.get(0).getMember().getOvertime();
            long startVotes = dataList.get(0).getMember().getVotes();

            //get current entry
            int endWork = dataList.get(dataList.size() - 1).getMember().getShifts();
            int endTips = dataList.get(dataList.size() - 1).getMember().getTips();
            long endDonations = dataList.get(dataList.size() - 1).getMember().getDonations();
            long endOvertime = dataList.get(dataList.size() - 1).getMember().getOvertime();
            long endVotes = dataList.get(dataList.size() - 1).getMember().getVotes();

            workYesterday.put(id, new WeeklyBestData(id, endWork - startWork));
            tipsYesterday.put(id, new WeeklyBestData(id, endTips - startTips));
            donationsYesterday.put(id, new WeeklyBestData(id, endDonations - startDonations));
            overtimeYesterday.put(id, new WeeklyBestData(id, endOvertime - startOvertime));
            votesYesterday.put(id, new WeeklyBestData(id, endVotes - startVotes));

        });

        WeeklyBestData.sort(work);
        WeeklyBestData.sort(tips);
        WeeklyBestData.sort(donations);
        WeeklyBestData.sort(overtime);
        WeeklyBestData.sort(votes);
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        embed.color(Color.SUMMER_SKY);
        embed.title(franchiseName +" Weekly Best");
        embed.addField("Top Work", getBest(work, workYesterday, false), true);
        embed.addField("Top Tips", getBest(tips, tipsYesterday,false), false);
        embed.addField("Top Donations", getBest(donations, donationsYesterday,true), false);
        embed.addField("Top Overtime", getBest(overtime, overtimeYesterday, false), false);
        embed.addField("Top Votes", getBest(votes, votesYesterday,false), false);

        EmbedWithGiveaway embedWithGiveaway = new EmbedWithGiveaway();
        embedWithGiveaway.embed = embed.build();
        embedWithGiveaway.giveawayData = giveawayData;
        return embedWithGiveaway;
    }

    private class EmbedWithGiveaway {

        List<GiveawayData> giveawayData = new ArrayList<>();
        EmbedCreateSpec embed;
    }

    private class HistoryWithGiveaway {
        HashMap<Long, List<ExportData>> history;
        List<GiveawayData> giveawayData;
    }

    private Mono<Void> checkRoles(
            HashMap<Long, List<ExportData>> history,
            String franchise,
            HashMap<Long, List<Id>> userRoles,
            Message message) {

        Snowflake guildId = message.getGuildId().get();
        List<Donations> donations = ExportUtils.loadDonations(franchise);

        return Flux.fromIterable(history.entrySet())
                .flatMap(entry -> {
                    long userId = entry.getKey();
                    List<ExportData> data = entry.getValue();
                    long donationAmount = data.get(data.size() - 1).getMember().getDonations();

                    List<Id> roles = userRoles.get(userId);
                    List<String> roleIds = roles == null
                            ? new ArrayList<>()
                            : roles.stream()
                            .map(Id::asString)
                            .collect(Collectors.toList());


                    return Flux.fromIterable(donations)
                            .flatMap(donate -> {
                                boolean inRange =
                                        donationAmount >= donate.getMinDonation() &&
                                                donationAmount <= donate.getMaxDonation();

                                boolean hasRole =
                                        roleIds.contains(donate.getRole());

                                Snowflake userSnowflake = Snowflake.of(userId);
                                Snowflake roleSnowflake = Snowflake.of(donate.getRole());

                                if (inRange && !hasRole) {
                                    // Give role
                                    return client.getGuildById(guildId)
                                            .addMemberRole(userSnowflake, roleSnowflake, "donation role")
                                            .doOnSuccess(v -> logger.info("Gave user " + userId + " role " + donate.getRole()))
                                            .onErrorResume(e -> {
                                                logger.warn("Could not give role to user " + userId, e);
                                                return Mono.empty();
                                            });
                                } else if (!inRange && hasRole) {
                                    // Remove role
                                    return client.getGuildById(guildId)
                                            .removeMemberRole(userSnowflake, roleSnowflake, "donation role")
                                            .doOnSuccess(v -> logger.info("Removed role " + donate.getRole() + " from " + userId))
                                            .onErrorResume(e -> {
                                                logger.warn("Could not remove role from user " + userId, e);
                                                return Mono.empty();
                                            });
                                }

                                return Mono.empty();
                            });
                })
                .then();
    }

    private String getBest(List<WeeklyBestData> weeklyBestData, HashMap<Long, WeeklyBestData> weeklyBestDataYesterday,  boolean money) {

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (WeeklyBestData data : weeklyBestData) {
            if (count == 10) {
                break;
            }
            count++;

            String value = "";
            if (money) {
                value = "$";
            }
            value = value + String.format("%,d", data.getValue());
            long change = 0;
            if (weeklyBestDataYesterday.size() > 0 && weeklyBestDataYesterday.containsKey(data.getId())) {
                change = data.getValue() - weeklyBestDataYesterday.get(data.getId()).getValue();
            }

            String emote = " <a:greenup:1015028862368878723> ";
            if (change < 0) {
                emote = " <a:reddown:1015028786292592701> ";
            }
            if (change == 0){
                emote = " <a:orange_dots:1015118419047235585> ";
            }

            sb.append("**" + count + "** - <@" + data.getId() + "> - " + value + emote + String.format("%,d", change) +" \r\n");
        }
        return sb.toString();
    }

}
