package action.export;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import action.Action;
import action.Warn;
import action.export.model.*;
import action.reminder.DoReminder;
import action.reminder.ReminderType;
import action.reminder.ReminderUtils;
import bot.Clean;
import bot.KickList;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.Id;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class Import2Reactive extends Action {

    String guildId;
    boolean skipWarnings;
    String zeroVotes;

    public Import2Reactive() {
        param = "ouiimport";
        guildId = config.get("guildId");
        skipWarnings = Boolean.parseBoolean(config.get("skipWarnings", "false"));
        zeroVotes = "1102390411252744312";
    }

    @Override
    public Mono<Object> doAction(Message message) {
        String actionData = getAction(message);
        if (actionData == null || !hasPermission(message, true)) return Mono.empty();
        Snowflake messageId = Snowflake.of(actionData);

        return message.getChannel()
                .flatMap(channel ->
                        channel.createMessage("Starting import...")
                                .flatMap(startMsg ->
                                        processImportReactive(message, channel, messageId)
                                                .then(channel.createMessage("Imported Data"))
                                )
                                .onErrorResume(e -> {
                                    logger.error("Import failed", e);
                                    return message.getChannel()
                                            .flatMap(ch -> ch.createMessage("Failed to import data, please import manually"));
                                })
                );
    }

    private Mono<Void> processImportReactive(Message message, MessageChannel channel, Snowflake messageId) {
        String guildId = message.getGuildId().get().asString();

        return getFranchiseConfigReactive(guildId)
                .flatMap(config ->
                        getFileInfoReactive(message, messageId)
                                .flatMap(fileInfo -> runHeavyImportReactive(fileInfo, config, message, channel))
                );
    }

    private Mono<FranchiseConfig> getFranchiseConfigReactive(String guildId) {
        return Mono.fromCallable(() -> ExportUtils.getFranchiseConfig(guildId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<FileInfo> getFileInfoReactive(Message message, Snowflake messageId) {
        return message.getChannel()
                .flatMap(channel -> channel.getMessageById(messageId))
                .map(msg -> {
                    String url = msg.getData().attachments().get(0).url();
                    String[] parts = url.split("/");
                    String franchiseName = parts[parts.length - 1].split("_")[0];
                    return new FileInfo(url, franchiseName, msg.getTimestamp());
                });
    }

    private Mono<Void> runHeavyImportReactive(FileInfo fileInfo, FranchiseConfig config,
                                              Message message, MessageChannel channel) {

        return Mono.fromCallable(() -> {
                    // 🔹 Heavy CPU/IO work
                    KickList kickList = Clean.main(
                            fileInfo.url,
                            fileInfo.franchiseName + "historic.csv",
                            5, 7,
                            Timestamp.from(fileInfo.timestamp),
                            fileInfo.franchiseName
                    );

                    HashMap<Long, List<ExportData>> history = ExportUtils.loadMemberHistory(fileInfo.franchiseName);

                    ImportResult result = new ImportResult();
                    result.history = history;
                    result.franchiseName = fileInfo.franchiseName;
                    result.weeklyEmbed = buildWeeklyEmbed(history, fileInfo.franchiseName);
                    result.voteEmbed = buildVoteEmbed(history, fileInfo.franchiseName);
                    result.giveawayData = buildGiveawayData(history);

                    return result;
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> sendEmbedsReactive(result, channel)
                        .then(updateRolesReactive(result, config, message))
                );
    }

    private Mono<Void> sendEmbedsReactive(ImportResult result, MessageChannel channel) {
        return Mono.when(
                channel.createMessage(result.weeklyEmbed).then(),
                channel.createMessage(result.voteEmbed).then()
        ).then();
    }

    private Mono<Void> updateRolesReactive(ImportResult result, FranchiseConfig config, Message message) {

        return Mono.fromCallable(() -> {
                    HashMap<Long, List<Id>> userRoles = new HashMap<>();
                    for (Long id : result.history.keySet()) {
                        try {
                            List<Id> roles = client.getGuildById(message.getGuildId().get())
                                    .getMember(Snowflake.of(id))
                                    .block()
                                    .roles();
                            userRoles.put(id, roles);
                        } catch (ClientException e) {
                            logger.info("User left the server " + id);
                        }
                    }
                    return userRoles;
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(userRoles -> applyRoleLogicReactive(result, config, message, userRoles));
    }

    private Mono<Void> applyRoleLogicReactive(ImportResult result, FranchiseConfig config, Message message,
                                              HashMap<Long, List<Id>> userRoles) {

        List<Mono<Object>> roleOps = new ArrayList<>();

        for (GiveawayData member : result.giveawayData) {
            roleOps.add(Mono.fromRunnable(() -> {
                try {
                    if (member.qualifiesForGiveaway()) {
                        if (!hasRole(userRoles.get(member.getId()), config.getGiveawayRole())) {
                            client.getGuildById(Snowflake.of(guildId))
                                    .addMemberRole(Snowflake.of(member.getId()),
                                            Snowflake.of(config.getGiveawayRole()),
                                            "Add Giveaway role")
                                    .block();
                        }
                    }
                } catch (ClientException e) {
                    logger.info("User left the server " + member.getId());
                }
            }).subscribeOn(Schedulers.boundedElastic()));
        }

        return Flux.merge(roleOps).then();
    }

    private EmbedCreateSpec buildWeeklyEmbed(HashMap<Long, List<ExportData>> history, String franchiseName) {
        // Build embed with weekly stats
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        embed.color(Color.SUMMER_SKY);
        embed.title(franchiseName + " Weekly Best");
        // Add fields dynamically
        return embed.build();
    }

    private EmbedCreateSpec buildVoteEmbed(HashMap<Long, List<ExportData>> history, String franchiseName) {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        embed.color(Color.SUMMER_SKY);
        embed.title(franchiseName + " Vote Stats");
        return embed.build();
    }

    private List<GiveawayData> buildGiveawayData(HashMap<Long, List<ExportData>> history) {
        // compute giveaway data
        return new ArrayList<>();
    }

//    private boolean hasRole(List<Id> roles, String role) {
//        if (roles == null) return false;
//        return roles.stream().anyMatch(r -> r.asLong() == Long.parseLong(role));
//    }

    // Supporting classes
    private static class FileInfo {
        String url;
        String franchiseName;
        Instant timestamp;
        FileInfo(String url, String franchiseName, Instant timestamp) {
            this.url = url;
            this.franchiseName = franchiseName;
            this.timestamp = timestamp;
        }
    }

    private static class ImportResult {
        HashMap<Long, List<ExportData>> history;
        String franchiseName;
        EmbedCreateSpec weeklyEmbed;
        EmbedCreateSpec voteEmbed;
        List<GiveawayData> giveawayData;
    }
}
