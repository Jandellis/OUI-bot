package action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bot.Clean;
import bot.KickMember;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class Colour extends Action {


    String guildId;
    String fittingRoom;

    private static final Map<String, Color> COLOR_MAP = new HashMap<>();
    static {
        COLOR_MAP.put("RED", Color.RED);
        COLOR_MAP.put("GREEN", Color.GREEN);
        COLOR_MAP.put("BLUE", Color.BLUE);
        COLOR_MAP.put("YELLOW", Color.of(255, 255, 0));
        COLOR_MAP.put("PURPLE", Color.of(128, 0, 128));
        COLOR_MAP.put("CYAN", Color.CYAN);
        COLOR_MAP.put("ORANGE", Color.of(255, 165, 0));
        // Add more colors as needed
    }

    public Colour() {
        param = "cycolour";
        guildId = config.get("guildId");

        fittingRoom = "887388634465042462";
    }

    @Override
    public Mono<Object> doAction(Message message) {

        String action = getAction(message);
        if (action != null ) {
            return message.getChannel().flatMap(channel -> {
                if (channel.getId().asString().equals(fittingRoom)) {
                    try {
                        Guild guild = message.getGuild().block();
                        Role role = updateRoleWithUserId(guild, message.getAuthor().get().getId().asString(), action).block();

                        client.getGuildById(guild.getId()).addMemberRole(
                                message.getAuthor().get().getId(),
                                role.getId(),
                                "colour update").block();

                    } catch (Exception e) {
//                        printException(e);
                        return channel.createMessage(e.getMessage());
                    }
                    return channel.createMessage("Done");
                }

                return Mono.empty();
            });
        }


        return Mono.empty();
    }
    private static Mono<Role> createRole(Guild guild, String roleName, Color color) {
        return guild.createRole(spec -> {
            spec.setName(roleName);
            spec.setColor(color);
            spec.setMentionable(false); // Make the role mentionable
            spec.setHoist(false);       // Display role separately
        });
    }

    private static Mono<Role> updateRoleWithUserId(Guild guild, String userId, String hexColor) {
        try {
            // Convert hex color to Color
            Color color = parseColor(hexColor);
            String roleName = "USER-"+userId;

            // Find or create the role with the user's ID as the name
            return guild.getRoles()
                    .filter(role -> role.getName().equals(roleName))
                    .next()
                    .switchIfEmpty(createRole(guild, roleName, color)) // Create the role if it doesn't exist
                    .flatMap(role -> role.edit(spec -> spec.setColor(color))); // Update the role with the new color
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalArgumentException("Invalid hex color: " + hexColor));
        }
    }

    private static Color parseColor(String input) {
        // Check if the input matches a predefined color name
        Color color = COLOR_MAP.get(input.toUpperCase());
        if (color != null) {
            return color;
        }

        // If not a color name, try to parse it as a hex code
        return convertHexToColor(input);
    }

    private static Color convertHexToColor(String hexColor) {
        // Remove the '#' if present
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }

        // Ensure the hex string is valid
        if (hexColor.length() != 6) {
            throw new IllegalArgumentException("Hex color must be 6 characters long");
        }

        try {
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
            return Color.of(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color: " + hexColor);
        }
    }
}
