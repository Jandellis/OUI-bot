package action.reminder;

import action.Action;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateProfile extends Action {

    String tacoBot = "490707751832649738";

    public CreateProfile() {
    }

    @Override
    public Mono<Object> doAction(Message message) {
        //work out how much people got in

        // add list of channels to watch
        List<String> watchChannels = new ArrayList<>();
        watchChannels.add("841034380822577182");
        watchChannels.add("889662502324039690");
        AtomicBoolean watched = new AtomicBoolean(false);

        watchChannels.forEach(channel -> {
            if (message.getChannelId().asString().equals(channel)) {
                watched.set(true);
            }
        });
        //if in watch channel
        if (watched.get()) {
            if (message.getAuthor().get().getId().asString().equals(tacoBot)) {
                try {
                    for (Embed embed : message.getEmbeds()) {

                        if (embed.getFields().size() > 1 && embed.getFields().get(0).getName().equals("Shack Name")) {

                            if (!embed.getThumbnail().isPresent()) {
                                message.getChannel().block().createMessage("Sorry unable to create your profile. If you add an avatar i will be able to").block();
                            } else {


                                String line = embed.getFields().get(0).getValue().split("\n")[0];
                                //replace up, replace taco, replace ()
                                String shackName = line.replace("\uD83D\uDD3A ", "").replace("\uD83C\uDF2E", "").replace(" ()", "");
                                Status status = Status.getStatus(embed.getFooter().get().getData().text());
                                String id = embed.getThumbnail().get().getUrl().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];

                                Utils.addProfile(id, shackName, status);

                                message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();
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
}
