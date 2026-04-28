package action;

import action.export.ExportUtils;
import action.export.model.FranchiseConfig;
import action.reminder.ReminderUtils;
import action.reminder.model.Profile;
import action.upgrades.model.LocationEnum;
import bot.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.http.client.ClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class Action {

    protected String param;
    protected Config config = Config.getInstance();
    protected GatewayDiscordClient gateway;
    protected DiscordClient client;
    protected String guildId;
    //    String defaultReact = "\uD83D\uDC4B";
    protected String defaultReact = "<a:cylon:1014777339114168340>";

    protected static final Logger logger = LogManager.getLogger("ouiBot");
    protected ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);


    public Mono<Void> action(GatewayDiscordClient gateway, DiscordClient client) {
        this.client = client;
        this.gateway = gateway;
        guildId = config.get("guildId");
        return gateway.on(MessageCreateEvent.class, event -> doAction(event.getMessage())).then();
    }

    public Mono<Void> reaction(GatewayDiscordClient gateway, DiscordClient client) {
        this.client = client;
        this.gateway = gateway;
        guildId = config.get("guildId");
        return gateway.on(ReactionAddEvent.class, reactionAddEvent -> doReactionEvent(reactionAddEvent)).then();
    }

    /**
     * Change to return true, if true it means that action processed the message and to stop all others from processing it.
     * Move gateway and client to constructor
     *
     * @param message
     * @return
     */
    protected abstract Mono<Object> doAction(Message message);

    protected Mono<Object> doReactionEvent(ReactionAddEvent reactionAddEvent) {
        return Mono.empty();
    }

    protected String getAction(Message message) {
        return getAction(message, param.toLowerCase());
    }

    protected String getAction(Message message, String paramInput) {
        return getAction(message, paramInput, 0);
    }

    protected String getAction(Message message, String paramInput, int position) {

        return getAction(message.getContent(), paramInput, position);
    }

    protected String getAction(String message, String paramInput, int position) {

        try {
            if (message.toLowerCase().startsWith(paramInput)) {
                logger.info(message);

                String temp = message.toLowerCase().replaceAll(paramInput + " ", "");
                String action = temp.split(" ")[position];
                logger.info("action " + action);
                return action;
            } else {
                if (paramInput.startsWith("cy")) {
                    //enable old commands to still work
                    paramInput = paramInput.replaceFirst("cy", "oui");
                    if (message.toLowerCase().startsWith(paramInput)) {
                        logger.info(message);

                        String temp = message.toLowerCase().replaceAll(paramInput + " ", "");
                        String action = temp.split(" ")[position];
                        logger.info("action " + action);
                        return action;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            logger.info("no var at position " + position + ", message was " + message);
            return "";
        } catch (Exception e) {
            printException(e);
        }
        return null;
    }

    protected boolean hasPermission(Message message, String role) {
        return hasPermission(message, Long.parseLong(role));
    }

    protected boolean hasPermission(Message message, Long role) {
        try {
            if (!message.getAuthor().isPresent()) {
                return false;
            }

            return hasPermission(message.getAuthor().get().getId().asString(), role);

        } catch (Exception e) {
            printException(e);
        }
        return false;
    }

    protected boolean hasPermission(Message message) {
        return hasPermission(message, false);
    }

    protected boolean hasPermission(Message message, boolean checkInteraction) {
        try {
            if (!message.getAuthor().isPresent()) {
                return false;
            }

            if (!message.getGuildId().isPresent()) {
                return false;
            }
            FranchiseConfig franchiseConfig = ExportUtils.getFranchiseConfig(message.getGuildId().get().asString());
            if (franchiseConfig == null) {
                return false;
            }
            String userId = message.getAuthor().get().getId().asString();
            logger.info("Found franchise " + franchiseConfig.getName());
            logger.info("Checking if user " +userId + " has role " + franchiseConfig.getRecruiter());

            boolean result = hasPermission(userId, Long.parseLong(franchiseConfig.getRecruiter()), franchiseConfig.getGuild());
            if (checkInteraction && !result) {
                if (message.getInteraction().isPresent()) {
                    logger.info("Checking interaction");
                    userId = message.getInteraction().get().getUser().getId().asString();
                    logger.info("Checking if user " +userId + " has role " + franchiseConfig.getRecruiter());
                    result = hasPermission(userId, Long.parseLong(franchiseConfig.getRecruiter()), franchiseConfig.getGuild());
                }
            }
            return result;

        } catch (Exception e) {
            printException(e);
        }
        return false;
    }
    protected boolean isOUI(Message message) {
        try {
            if (!message.getAuthor().isPresent()) {
                return false;
            }

            if (!message.getGuildId().isPresent()) {
                return false;
            }
            FranchiseConfig franchiseConfig = ExportUtils.getFranchiseConfig(message.getGuildId().get().asString());
            if (franchiseConfig == null) {
                return false;
            }
            return franchiseConfig.getName().equals("oui");
        } catch (Exception e) {
            printException(e);
        }
        return false;
    }

    protected boolean hasPermission(String userId, Long role) {
        return hasPermission(userId, role, guildId);
    }

    protected boolean hasPermission(String userId, Long role, String guildId) {
        try {
            MemberData memberData = null;

            memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(userId)).getData().block();

            for (Id id : memberData.roles()) {
                if (id.asLong() == role)
                    return true;
            }

        } catch (ClientException e) {
            logger.info("Member not in server");
        } catch (Exception e) {

            printException(e);
        }
        return false;
    }

    protected void printException(Throwable e) {
        try {
            logger.error("Exception", e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();

            if (sStackTrace.length() > 1000) {
                sStackTrace = sStackTrace.substring(0, 999);
            }

            String finalSStackTrace = sStackTrace;
//            gateway.getUserById(Snowflake.of("292839877563908097")).block().getPrivateChannel().flatMap(channel -> {
//                channel.createMessage("**something broke!!**\r\n\r\n " + finalSStackTrace).block();
//                logger.info("sent DM");
//                return Mono.empty();
//            }).block();
            dmMe("**something broke!!**\r\n\r\n " + finalSStackTrace);
        } catch (Throwable e2) {

            logger.error("Exception", e2);
        }
    }

    protected  void dmMe(String message) {
        gateway.getUserById(Snowflake.of("292839877563908097")).block().getPrivateChannel().flatMap(channel -> {
            channel.createMessage(message).block();
            logger.info("sent DM");
            return Mono.empty();
        }).block();
    }

    protected Mono<Void> timeoutMember(Member member, Duration duration) {
        return member.edit(GuildMemberEditSpec.builder().communicationDisabledUntil(Instant.now().plus(duration)).reason("code pings").build())
                .doOnSuccess(v -> logger.info("Member " + member.getUsername() + " has been muted for " + duration.toMinutes() + " minutes."))
                .doOnError(e -> {
                    logger.error("Error muting member: " + e.getMessage());
                    printException(e);
                })
                .then();
    }

    protected LocationEnum getLocation(EmbedData embedData) {
        if (embedData != null && embedData.footer().toOptional().isPresent()) {
            String location = embedData.footer().get().text();
            if (location.contains("\n")){
                String[] footer = location.split("\n");
                location = footer[footer.length-1];
            }
            try {
            location = location.split(" \\| ")[1];
            } catch (Exception e) {
                return LocationEnum.event;
            }
            if (location.endsWith("#0")) {
                location = location.replace("#0", "");
            }
            //remove shack from location, so its just the name
            //also remove the emote at the start then we are left with the name
            location = location.replace("Shack", "").trim();
            String[] split = location.split(" ");
            location = split[split.length-1];

            LocationEnum locationEnum = LocationEnum.getLocation(location);
            return locationEnum;

        }
        return LocationEnum.shack;
    }


    protected Long getBalance(EmbedData embedData) {
        if (embedData != null && embedData.footer().toOptional().isPresent()) {
            logger.info("Checking balance - footer " + embedData.footer().get().text());
            String balance = embedData.footer().get().text();
            if (balance.contains("\n")){
                String[] footer = balance.split("\n");
                balance = footer[0];
            }
            balance = balance.split("\\$")[1];
            balance = balance.replaceAll(",", "").trim();
            return Long.parseLong(balance);
        }
        return null;
    }

    protected String getId(Message message, EmbedData embedData) {
        //check if embed data is not null

        // if it has a footer with the username in it
        // find the profile by username
        //load profile by username and return that
        //if no profile go to the old method

        if (embedData != null && embedData.footer().toOptional().isPresent()) {

            String username = embedData.footer().get().text();
            if (username.contains("\n")){
                String[] footer = username.split("\n");
                username = footer[footer.length-1];
            }
            username = username.split(" \\| ")[0];
            if (username.endsWith("#0")) {
                username = username.replace("#0", "");
            }
            Profile profile = ReminderUtils.loadProfileByUserName(username);

            if (profile != null) {
                logger.info("Got profile via footer " + profile.getName());
                return profile.getName();
            }

//
//            embedData.footer().get().text().split("\\|");
        }





        Message original = message;
        int count = 0;
        while (true) {
            count++;
            if (count == 150) {
                logger.info("hit 150 limit - message " + message.getId().asLong() + " channel " + message.getChannelId().asLong());
                return "";
            }
            if (message.getData().interaction().isAbsent()) {
                if (message.getReferencedMessage().isPresent()) {
                    message = message.getReferencedMessage().get();
                    continue;
                }
                if (message.getMessageReference().isPresent()) {
                    try {
                        Message msg = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), Snowflake.of(message.getMessageReference().get().getMessageId().get().asLong())).block();
                        message = msg;
                        continue;
                    }
                    catch (ClientException e) {

                        if (e.getErrorResponse().isPresent() &&
                                e.getErrorResponse().get().getFields().get("code").equals(10008) &&
                                e.getErrorResponse().get().getFields().get("message").equals("Unknown Message")) {
                            logger.info("UserName is not know, and message history was deleted, skipping trying to do anything for this message");
                        } else {
                            printException(e);
                        }
                        return "";
                    } catch (RuntimeException e){
                        ;
                    }
                }
                return "";
            }
            String userId = message.getData().interaction().get().user().id().toString();
            Profile profile = ReminderUtils.loadProfileById(userId);

            if (profile == null) {
                return "";
            }

            if (profile.getDepth() >= count - 1) {
                return userId;
            } else {
                logger.info("hit history limit"); //<a:lights:1017394940789145610>
                count--;
//                message.getChannel().block().createMessage("You have exceeded your history limit. This reminder will not be created.\n" +
//                        "To stop seeing this message increase your history limit with `cyrm history <limit>`, or stop clicking on other peoples buttons!\n" +
//                        "The higher the history, the more message I will go back and check who was the owner. For me to read this message your history would need to be more than " + count).block();
                if (profile.getEnabled()) {
                    original.addReaction(Emoji.unicode("\uD83D\uDEAB")).block();
                }
                return "";
            }
        }
    }

//    public String getContainerUser(Message message) {
//
//        Possible<List<ComponentData>> possibleComponents = message.getData().components();
//
//        if (possibleComponents.isAbsent() || possibleComponents.get().isEmpty()) {
//            return null;
//        }
//
//        List<ComponentData> components = possibleComponents.get();
//
//        for (ComponentData container : components) {
//
//            // container level (rows)
//            Possible<List<ComponentData>> inner = container.components();
//
//            if (inner.isAbsent()) continue;
//
//            for (ComponentData item : inner.get()) {
//
//                // type 10 = text block
//                if (item.type() == 10 && item.content().isPresent()) {
//
//                    String text = item.content().get();
//
//                    if (text.contains(" | ")) {
//                        String id = text.split("\\s*\\|\\s*")[0];
//                        logger.info("found id of {}", id);
//                        return id;
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
public String getContainerUser(Message message) {
    Possible<List<ComponentData>> possible = message.getData().components();
    if (possible.isAbsent()) return null;

    for (ComponentData top : possible.get()) {
        if (top.components().isAbsent()) continue;
        for (ComponentData item : top.components().get()) {
            if (item.type() != 10 || item.content().isAbsent()) continue;
            String text = item.content().get();
            if (text.contains(" | ")) {
                String id = text.split("\\s*\\|\\s*")[0].trim();
                logger.info("found id of '{}' length {}", id, id.length());
                return id;
            }
        }
    }
    return null;
}


    protected void checkMessageAgain(Message message) {

        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
//                logger.info("checking message again");
                Message msg = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), Snowflake.of(message.getId().asString())).block();
                doAction(msg);
            }

        };
//        logger.info("checking message again in 1 sec");
        executorService.schedule(taskWrapper, 500, TimeUnit.MILLISECONDS);
    }

    protected boolean checkAge(Message message) {
        LocalDateTime now = LocalDateTime.now().minusSeconds(10);
        boolean notTooOld = now.isBefore(Timestamp.from(message.getTimestamp()).toLocalDateTime());
        if (notTooOld && (message.getEmbeds().isEmpty() || message.getContent().isEmpty())) {
//            logger.info("message has no embeds or content " + message.getId());
//            checkMessageAgain(message);
            return true;
        }
//        if (!notTooOld) {
//            logger.info("message is too old " + message.getId());
//        }
//        logger.info("message has embeds or content " + message.getId());
        return false;
    }
    protected List<EmbedData> checkEmbeds(Message message) {
        return checkEmbeds(message, false);
    }

    protected List<EmbedData> checkEmbeds(Message message, Boolean force) {

        logger.info("checking embeds " + message.getId());
        if (message.getEmbeds().isEmpty()) {
//            try {
//                Thread.sleep(250);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }


            // this may break again in the future
            Message result = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), message.getId()).block();

            if (result != null ) {
                if (force) {
                    if (result.getData().embeds().size() > 0) {
                        return result.getData().embeds();
                    }
                } else {
                    return result.getData().embeds();
                }
            }


            List<MessageData> history = getMessagesOfChannel(message);
            for (MessageData messageData : history) {
                if (messageData.id().asLong() == message.getId().asLong()) {
                    List<Embed> embeds = new ArrayList<>();
//                    messageData.embeds().get(0).fields();
                    logger.info("checking embeds size " + messageData.embeds().size());
                    return messageData.embeds();
                }
            }

        }
        return new ArrayList<>();
    }

    public static List<MessageData> getMessagesOfChannel(Message message) {

//        Snowflake time = Snowflake.of(message.getTimestamp().minus(15, ChronoUnit.SECONDS));
        return getMessagesOfChannel(message, 5);
    }

    public static List<MessageData> getMessagesOfChannel(Message message, int time) {
        Snowflake snowflakeTime = Snowflake.of(message.getTimestamp().minus(time, ChronoUnit.SECONDS));
        try {
            return message.getRestChannel().getMessagesAfter(snowflakeTime).collectList().block();
        } catch (Throwable e ) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean hasRole(List<Id> roles, String role) {
        if (roles == null) {
            return false;
        }
        AtomicBoolean found = new AtomicBoolean(false);
        roles.forEach(id -> {
            if (id.asLong() == Long.parseLong(role)) {
                found.set(true);
            }
        });

        return found.get();
    }

    protected void react(Message message, Profile profile) {
        if (!profile.getEnabled())
            return;
        String react;
        if (profile == null) {
            react = defaultReact;
        } else {
            react = profile.getEmote();
            if (react == null || react.equals("")) {
                react = defaultReact;
            }
        }
        react(message, react);
    }

//    protected void react(Message message, String react) {
//        if (react.startsWith("<")) {
//            String[] emote = react.split(":");
//            Long id = Long.parseLong(emote[2].replace(">", ""));
//            String name = emote[1];
//            boolean animated = true;
//            message.addReaction(Emoji.of(id, name, true)).block();
//        } else {
//            message.addReaction(Emoji.unicode(react)).block();
//        }
//    }

    protected void react(Message message, String react) {
        if (react.startsWith("<")) {
            String[] emote = react.split(":");
            // Strip everything that isn’t a number (handles >, spaces, etc.)
            String idStr = emote[2].replaceAll("\\D", "");
            long id = Long.parseLong(idStr);

            String name = emote[1];
            boolean animated = react.startsWith("<a:");

            message.addReaction(Emoji.custom(Snowflake.of(id), name, animated)).block();
        } else {
            message.addReaction(Emoji.unicode(react)).block();
        }
    }
}
