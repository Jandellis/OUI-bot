package action.reminder;

import action.Action;
import action.reminder.model.Profile;
import action.reminder.model.ProfileStats;
import action.reminder.model.Status;
import action.upgrades.model.LocationEnum;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateProfile extends Action implements EmbedAction {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public CreateProfile() {
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    String reloadEmote = "\uD83D\uDD03";

    @Override
    public Mono<Object> doAction(Message message) {
        return doAction(message, true);
    }


    public Mono<Object> doAction(Message message, boolean checkEmbeds) {
        //work out how much people got in

        AtomicBoolean watched = new AtomicBoolean(true);
//
//        watchChannels.forEach(channel -> {
//            if (message.getChannelId().asString().equals(channel)) {
//                watched.set(true);
//            }
//        });
        //if in watch channel
        if (watched.get()) {
            if (message.getData().author().id().asString().equals(tacoBot)) {
                try {
                    //for some reason the embeds will be empty from slash, but if i load it again it will have data
//                    if (checkAge(message)) {
//                        checkMessageAgain(message);
//                    }


                    List<EmbedData> embedData;
                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0) {

                        embedData = checkEmbeds(message);
                    } else {
                        embedData = message.getData().embeds();
                    }

                    handleEmbedAction(message, embedData);
                } catch (Exception e) {
                    printException(e);
                }

            }
        }
        return Mono.empty();
    }


    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {

        try {

            for (EmbedData embed : embedData) {

                if (embed.fields().toOptional().isPresent()
                        && embed.fields().get().size() > 1
                        && embed.fields().get().get(0).name().equals("Shack Name")) {

                    String id = null;
                    if (message.getData().interaction().toOptional().isPresent()) {
                        if (message.getData().interaction().get().name().equalsIgnoreCase("shack")){
                            id = message.getData().interaction().get().user().id().toString();
                        }
                    }

                    if (embed.thumbnail().toOptional().isPresent() || id != null) {
                        if (embed.footer().toOptional().isPresent()) {

                            String line = embed.fields().get().get(0).value().split("\n")[0];
                            //replace up, replace taco, replace (), hq building
                            String shackName = line.replace("\uD83D\uDD3A ", "").replace("\uD83C\uDF2E", "").replace(" ()", "").replace(" \uD83C\uDFDB", "");
                            Status status = Status.getStatus(embed.footer().get().text());
                            if (id == null) {
                                id = embed.thumbnail().get().url().get().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];
                                logger.info("thumbnail id is " + embed.thumbnail().get().url().get());
                            }
                            MemberData memberData = null;
                            try {
                                logger.info("User id is " + id);
                                String guildId = message.getGuildId().get().asString();
                                memberData = client.getGuildById(Snowflake.of(guildId)).getMember(Snowflake.of(id)).block();

                            } catch (NumberFormatException e) {
//                                gateway.getUserById(Snowflake.of("292839877563908097")).block().getPrivateChannel().flatMap(channel -> {
//                                    channel.createMessage("**something broke!!**\r\n\r\n NumberFormatException in create profile! " + embed.thumbnail().get().url().get()).block();
//                                    return Mono.empty();
//                                }).block();
                                message.getChannel().block().createMessage("Sorry unable to create your profile. If you add an avatar i will be able to").block();
                                return Mono.empty();
                            } catch (ClientException e) {

                                if (e.getErrorResponse().isPresent() &&
                                        e.getErrorResponse().get().getFields().get("code").equals(10007) &&
                                        e.getErrorResponse().get().getFields().get("message").equals("Unknown Member")) {
                                    logger.info("Member not in server, not creating profile");
                                } else {
                                    printException(e);
                                }
                                return Mono.empty();
                            }catch (NoSuchElementException e) {
                                    logger.info("Member not in server, not creating profile");
                                return Mono.empty();
                            }
                            String userName = memberData.user().username();
                            String discriminator = memberData.user().discriminator();
                            boolean newProfile;
                            if (discriminator.equals("0")) {
                                newProfile = ReminderUtils.addProfile(id, shackName, status, userName);
                            } else {
                                newProfile = ReminderUtils.addProfile(id, shackName, status, userName + "#" + discriminator);
                            }



                            ProfileStats profileStats = new ProfileStats(id);
                            profileStats.setImportTime(Timestamp.from(Instant.now()));
                            for (EmbedFieldData fieldData : embed.fields().get()) {
                                if (fieldData.name().toLowerCase().contains("location")){
                                    for (LocationEnum value : LocationEnum.values()) {
                                        if (fieldData.value().toLowerCase().contains(value.getName())) {
                                            profileStats.setLocation(value);
                                        }
                                    }
                                }
                                if (fieldData.name().toLowerCase().contains("balance")){
                                    String balance = fieldData.value().split(" ")[1].replace(",", "").replace("$", "");
                                    profileStats.setBalance(Long.parseLong(balance));
                                }
                                if (fieldData.name().toLowerCase().contains("income")){
                                    String balance = fieldData.value().split(" ")[1].replace(",", "").replace("$", "");
                                    profileStats.setIncome(Long.parseLong(balance));
                                }
                            }
                            ReminderUtils.addProfileStats(profileStats);



                            Profile profile = ReminderUtils.loadProfileById(id);
                            if (profile.getEnabled()) {
                                message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();
                                message.addReaction(ReactionEmoji.unicode(reloadEmote)).block();
                            }
                            if (newProfile) {
                                message.getChannel().block().createMessage("Your profile has been created. Would you like to turn on reminders? Type `cyrm on` \r\nFor more details, type `cyhelp`").block();
                            }
                        }
                    } else {
                        message.getChannel().block().createMessage("Sorry unable to create your profile. If you add an avatar i will be able to or do /shack").block();
                    }


                }
                //author=Possible{EmbedAuthorData{name=Possible{Balances | Mall Shack}, url=Possible.absent, iconUrl=Possible{https://cdn.discordapp.com/avatars/292839877563908097/17211f5921073e431a0f28f6f4f864be.png},

                if (embed.author().toOptional().isPresent() &&
                        embed.author().get().name().toOptional().isPresent() &&
                        embed.author().get().name().get().toLowerCase().contains("balances") ) {
                    if (embed.author().get().iconUrl().toOptional().isPresent()) {
                        String id = embed.author().get().iconUrl().get().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];
                        for (EmbedFieldData fieldData : embed.fields().get()) {
//                            for (LocationEnum value : LocationEnum.values()) {
                                String locationName = fieldData.name().split(" ")[1];
                            LocationEnum location = LocationEnum.getLocation(locationName);
                                if (location != null) {

                                    ProfileStats profileStats = new ProfileStats(id);
                                    profileStats.setImportTime(Timestamp.from(Instant.now()));
                                    profileStats.setLocation(location);
                                    profileStats.setIncome(-1L);
                                    profileStats.setBalance(Long.parseLong(fieldData.value().replace("$", "").replace(",", "")));
                                    ReminderUtils.addProfileStats(profileStats);
                                }
//                            }
                        }
                        Profile profile = ReminderUtils.loadProfileById(id);
                        if (profile.getEnabled())
                            message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();


                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }
        return Mono.empty();
    }
//
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


    @Override
    protected Mono<Object> doReactionEvent(ReactionAddEvent reactionAddEvent) {

        try {

            if (reactionAddEvent.getEmoji().asUnicodeEmoji().isPresent())
                if (reactionAddEvent.getEmoji().asUnicodeEmoji().get().getRaw().equals(reloadEmote)) {
                    //got reaction
                    Message message = reactionAddEvent.getMessage().block();
                    EmbedData embedData = null;
                    if (message.getEmbeds().size()> 0) {
                        embedData = message.getEmbeds().get(0).getData();
                    }

                    String messageAuthorId = getId(message, embedData);
                    if (messageAuthorId.equals(reactionAddEvent.getUserId().asString())) {
                        //user is the same as who wrote the did the message
                        //remove all reactions
                        message.removeAllReactions().block();
                        doAction(message);

                    }
                }
        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }
}
