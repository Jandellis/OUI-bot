package action.reminder;

import action.Action;
import action.reminder.model.Status;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateProfile extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public CreateProfile() {
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);


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
                    if (checkAge(message)) {
                        checkMessageAgain(message);
                    }
                    for (Embed embed : message.getEmbeds()) {

                        if (embed.getFields().size() > 1 && embed.getFields().get(0).getName().equals("Shack Name")) {

                            if (!embed.getThumbnail().isPresent()) {
                                message.getChannel().block().createMessage("Sorry unable to create your profile. If you add an avatar i will be able to").block();
                            } else {
                                if (embed.getFooter().isPresent()) {

                                    String line = embed.getFields().get(0).getValue().split("\n")[0];
                                    //replace up, replace taco, replace (), hq building
                                    String shackName = line.replace("\uD83D\uDD3A ", "").replace("\uD83C\uDF2E", "").replace(" ()", "").replace(" \uD83C\uDFDB", "");
                                    Status status = Status.getStatus(embed.getFooter().get().getData().text());
                                    String id = embed.getThumbnail().get().getUrl().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];

                                    boolean newProfile = ReminderUtils.addProfile(id, shackName, status);

                                    message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();
                                    if (newProfile) {
                                        message.getChannel().block().createMessage("Your profile has been created. Would you like to turn on reminders? Type `cyrm on` \r\nFor more details, type `cyhelp`").block();
                                    }
                                }
                            }


                        }
                    }
                } catch (Exception e) {
                    printException(e);
                }

            }
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
}
