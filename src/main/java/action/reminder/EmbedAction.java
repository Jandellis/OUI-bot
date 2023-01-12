package action.reminder;


import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.EmbedData;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EmbedAction {
    Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData);
}
