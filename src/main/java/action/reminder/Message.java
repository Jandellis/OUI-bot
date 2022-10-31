package action.reminder;

import action.Action;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Message extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public Message() {
        param = "cymsg";
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }


    @Override
    public Mono<Object> doAction(discord4j.core.object.entity.Message message) {
        try {

            AtomicBoolean watched = new AtomicBoolean(true);

//            watchChannels.forEach(channel -> {
//                if (message.getChannelId().asString().equals(channel)) {
//                    watched.set(true);
//                }
//            });
            //if in watch channel
            if (watched.get()) {

                String action = getAction(message);

                if (action != null) {
                    action = message.getContent().substring(param.length() + 1);
                    if (action.contains("<@")) {
                        message.getChannel().block().createMessage("You can not try to ping other people or roles!").block();
                    } else {
                        if (action.length() > 256) {

                            message.getChannel().block().createMessage("Sorry, too long. Max length is 255").block();
                        } else {
                            if ((action.contains("{task}") || action.contains("{cmd}")) && action.contains("{ping}")) {
                                ReminderUtils.addMessage(message.getAuthor().get().getId().asString(), action);
                                action = action.replace("{ping}", "<@" + message.getAuthor().get().getId().asString() + ">");
                                action = action.replace("{task}", ReminderType.work.getName());
                                action = action.replace("{cmd}", "</work:1006354978274820109>");

                                message.getChannel().block().createMessage("Message will be like \r\n " + action).block();
                            } else {
                                if (action.equalsIgnoreCase("delete")) {
                                    ReminderUtils.addMessage(message.getAuthor().get().getId().asString(), "");
                                    message.getChannel().block().createMessage("Deleted custom message").block();
                                } else {
                                    message.getChannel().block().createMessage("You need to have {task} or {cmd} and {ping} in your message").block();
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
