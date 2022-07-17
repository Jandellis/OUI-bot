package action.sm;

import action.Action;
import bot.Sauce;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.EmbedAuthorData;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class UpdateAlerts extends Action {

    String tacoBot = "490707751832649738";
    String smChannel;

    public UpdateAlerts() {
        param = "ouiSmDrop";
        smChannel = config.get("smChannel");
    }


    @Override
    public Mono<Object> doAction(Message message) {
        try {

            if (message.getChannelId().asString().equals(smChannel)) {
                if (message.getAuthor().get().getId().asString().equals(tacoBot)) {

                    for (Embed embed : message.getEmbeds()) {

                        if (embed.getAuthor().isPresent()) {
                            EmbedAuthorData authorData = embed.getAuthor().get().getData();

                            if (authorData.name().get().startsWith("Your Sauces")) {
                                if (authorData.iconUrl().isAbsent()) {
                                    message.getChannel().block().createMessage("Sorry unable to update your sauces. If you add an avatar i will be able to update them").block();
                                } else {
                                    String id = authorData.iconUrl().get().replace("https://cdn.discordapp.com/avatars/", "").split("/")[0];

                                    String line = embed.getDescription().get();
                                    List<Sauce> sauces = new ArrayList<>();

                                    for (Sauce sauce : Sauce.values()) {
                                        if (line.toLowerCase().contains(sauce.getName())) {
                                            sauces.add(sauce);
                                        }
                                    }

                                    Utils.addAlerts(id, sauces);
                                    if (sauces.isEmpty()) {
                                        message.getChannel().block().createMessage("Alerts cleared").block();
                                    } else {
                                        StringBuilder sb = new StringBuilder("Updated alerts");
                                        for (Sauce sauce : sauces) {
                                            sb.append("\r\n - " + sauce);
                                        }
                                        message.getChannel().block().createMessage(sb.toString()).block();
                                    }
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
