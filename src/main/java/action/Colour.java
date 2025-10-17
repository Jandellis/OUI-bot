package action;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class Colour extends Action {


    String guildId;
    String fittingRoom;

    private static final Map<String, Color> COLOR_MAP = new LinkedHashMap<>();
    static {
//        COLOR_MAP.put("BLACK", Color.of(0, 0, 0));

        COLOR_MAP.put("midnight blue", Color.of(25, 25, 112));
        COLOR_MAP.put("deep blue", Color.of(0, 0, 139));
        COLOR_MAP.put("navy blue", Color.of(0, 0, 128));
        COLOR_MAP.put("deep purple", Color.of(75, 0, 130));
        COLOR_MAP.put("royal purple", Color.of(102, 51, 153));
        COLOR_MAP.put("periwinkle", Color.of(204, 204, 255));

        COLOR_MAP.put("ocean blue", Color.of(0, 0, 205));
        COLOR_MAP.put("denim", Color.of(21, 96, 189));
        COLOR_MAP.put("steel blue", Color.of(70, 130, 180));
        COLOR_MAP.put("azure", Color.of(0, 127, 255));
        COLOR_MAP.put("powder blue", Color.of(176, 224, 230));
        COLOR_MAP.put("ice blue", Color.of(240, 248, 255));

        COLOR_MAP.put("dark teal", Color.of(0, 128, 128));
        COLOR_MAP.put("teal", Color.of(1, 141, 129));
        COLOR_MAP.put("turquoise", Color.of(64, 224, 208));
        COLOR_MAP.put("cyan", Color.of(0, 255, 255));
        COLOR_MAP.put("aqua", Color.of(127, 255, 212));
        COLOR_MAP.put("mint", Color.of(189, 252, 201));

        COLOR_MAP.put("forest green", Color.of(34, 139, 34));
        COLOR_MAP.put("lime green", Color.of(50, 205, 50));
        COLOR_MAP.put("neon green", Color.of(57, 255, 20));
        COLOR_MAP.put("emerald", Color.of(80, 200, 120));
        COLOR_MAP.put("spring green", Color.of(0, 255, 127));
        COLOR_MAP.put("pale green", Color.of(152, 251, 152));

        COLOR_MAP.put("olive", Color.of(128, 128, 0));
        COLOR_MAP.put("moss green", Color.of(85, 107, 47));
        COLOR_MAP.put("clover", Color.of(75, 128, 40));
        COLOR_MAP.put("fern", Color.of(79, 121, 66));
        COLOR_MAP.put("jade", Color.of(0, 168, 107));
        COLOR_MAP.put("light jade", Color.of(195, 253, 184));

        COLOR_MAP.put("burnt orange", Color.of(204, 85, 0));
        COLOR_MAP.put("pumpkin", Color.of(255, 117, 24));
        COLOR_MAP.put("coral", Color.of(255, 127, 80));
        COLOR_MAP.put("sand", Color.of(244, 164, 96));
        COLOR_MAP.put("amber", Color.of(255, 191, 0));
        COLOR_MAP.put("canary yellow", Color.of(255, 239, 0));

        COLOR_MAP.put("red", Color.of(230, 0, 0));
        COLOR_MAP.put("burgundy", Color.of(139, 0, 0));
        COLOR_MAP.put("cherry red", Color.of(220, 20, 60));
        COLOR_MAP.put("salmon", Color.of(250, 128, 114));
        COLOR_MAP.put("golden yellow", Color.of(255, 215, 0));
        COLOR_MAP.put("ducky yellow", Color.of(255, 216, 1));

        COLOR_MAP.put("dark brown", Color.of(101, 67, 33));
        COLOR_MAP.put("chocolate", Color.of(123, 63, 0));
        COLOR_MAP.put("tan", Color.of(210, 180, 140));
        COLOR_MAP.put("beige", Color.of(245, 245, 220));
        COLOR_MAP.put("cream", Color.of(255, 253, 208));
        COLOR_MAP.put("sunflower", Color.of(255, 204, 0));

        COLOR_MAP.put("dark gray", Color.of(105, 105, 105));
        COLOR_MAP.put("slate gray", Color.of(112, 128, 144));
        COLOR_MAP.put("ash gray", Color.of(190, 190, 190));
        COLOR_MAP.put("silver", Color.of(192, 192, 192));
        COLOR_MAP.put("white", Color.of(255, 255, 255));
        COLOR_MAP.put("pearl", Color.of(234, 224, 200));

        COLOR_MAP.put("lavender", Color.of(230, 230, 250));
        COLOR_MAP.put("mauve", Color.of(221, 160, 221));
        COLOR_MAP.put("lilac", Color.of(200, 162, 200));
        COLOR_MAP.put("heather", Color.of(183, 132, 167));
        COLOR_MAP.put("blush", Color.of(255, 182, 193));
        COLOR_MAP.put("orchid", Color.of(218, 112, 214));


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
                        String colourText = message.getContent().toLowerCase().replace(param.toLowerCase() + " ", "");
                        Role role = updateRoleWithUserId(guild, message.getAuthor().get().getId().asString(), colourText).block();
//                        ImmutablePositionModifyRequest.Builder builder = ImmutablePositionModifyRequest.builder();
//                        builder.id(role.getId().asString());
//                        builder.position(role.getRawPosition() + 1);
//                        client.getGuildById(guild.getId()).modifyRolePositions()
                        client.getGuildById(guild.getId()).addMemberRole(
                                message.getAuthor().get().getId(),
                                role.getId(),
                                "colour update").block();

                    } catch (Exception e) {
//                        printException(e);
//                        return channel.createMessage(e.getMessage());

                        makeColourImage();

                        InputStream inputStream = null;
                        try {
                            inputStream = new BufferedInputStream(new FileInputStream("color_chart.png"));
                        } catch (FileNotFoundException e2) {
                            throw new RuntimeException(e2);
                        }
                        MessageCreateSpec msg = MessageCreateSpec.builder()
                                .addFile("color_chart.png", inputStream)
                                .build();

                        client.getChannelById(Snowflake.of(fittingRoom)).createMessage(msg.asRequest()).block();
                        return channel.createMessage("Use one of the colour names or go to https://redketchup.io/color-picker and use the hex code");
                    }
                    return channel.createMessage("Done");
                }

                return Mono.empty();
            });
        } else if (getAction(message, "cycolor") != null) {
            return message.getChannel().flatMap(channel -> {
                if (channel.getId().asString().equals(fittingRoom)) {
                    return channel.createMessage("Spell **colour** correctly!");
                }
                return Mono.empty();
            });
        } else if (message.getContent().toLowerCase().startsWith(param.toLowerCase())) {
            return message.getChannel().flatMap(channel -> {
                if (channel.getId().asString().equals(fittingRoom)) {
                    makeColourImage();

                    InputStream inputStream = null;
                    try {
                        inputStream = new BufferedInputStream(new FileInputStream("color_chart.png"));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    MessageCreateSpec msg = MessageCreateSpec.builder()
                            .addFile("color_chart.png", inputStream)
                            .build();

                    client.getChannelById(Snowflake.of(fittingRoom)).createMessage(msg.asRequest()).block();
                    return channel.createMessage("Use one of the colour names or go to https://redketchup.io/color-picker and use the hex code");
                }
                return Mono.empty();
            });

        }


        return Mono.empty();
    }
    private Mono<Role> createRole(Guild guild, String roleName, Color color) {
        return guild.createRole(spec -> {
            spec.setName(roleName);
            spec.setColor(color);
            spec.setMentionable(false); // Make the role mentionable
            spec.setHoist(false);       // Display role separately
        });
    }

    private void moveRoleAbove(Guild guild, Role newRole) {
//        Snowflake guildId = guild.getId(); // Get the Guild's ID
        try {
            int position = guild.getRoles()
                    .filter(role -> role.getName().equalsIgnoreCase("platinum")).blockFirst().getRawPosition();
            //unsure if should use getRawPosition or getPosition
            if (position > newRole.getRawPosition())
            newRole.changePosition(position + 1).blockFirst();
        } catch (Exception e) {
            printException(e);
            System.out.println("Error moving role");
        }


        // Create a PositionModifyRequest to move the new role above the target role
//        PositionModifyRequest request = PositionModifyRequest.builder()
//                .id(newRole.getId().asString())
//                .position(position + 1)
//                .build();
//        List<PositionModifyRequest> requests = Collections.singletonList(request);
//
//        // Modify the role positions using RestGuild
//        guild.getClient()
//                .rest()
//                .getGuildById(guildId)
//                .modifyRolePositions(requests).blockFirst();

    }

    private Mono<Role> updateRoleWithUserId(Guild guild, String userId, String hexColor) {
        try {
            // Convert hex color to Color
            Color color = parseColor(hexColor);
            String roleName = "USER-"+userId;

            // Find or create the role with the user's ID as the name
            return guild.getRoles()
                    .filter(role -> role.getName().equals(roleName))
                    .next()
                    .switchIfEmpty(createRole(guild, roleName, color)) // Create the role if it doesn't exist
                    .flatMap(role -> {
                        moveRoleAbove(guild, role);
                        return role.edit(spec -> spec.setColor(color));
                    }); // Update the role with the new color
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalArgumentException("Invalid hex color: " + hexColor));
        }
    }

    private Color parseColor(String input) {
        // Check if the input matches a predefined color name
        logger.info("Checking if {} is a color", input);
        Color color = COLOR_MAP.get(input.toLowerCase());
        if (color != null) {
            return color;
        }

        // If not a color name, try to parse it as a hex code
        return convertHexToColor(input);
    }

    private Color convertHexToColor(String hexColor) {
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

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return Arrays.stream(input.toLowerCase().split("\\s+"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }



    private static void makeColourImage( ){
        int imageWidth = 1800;
        int imageHeight = 2000;
        int cellWidth = imageWidth / 6;
        int cellHeight = imageHeight / 10;
        int fontSize = 35;
        int columns = 6;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setFont(new Font("Arial", Font.BOLD, fontSize));

        int index = 0;
        for (Map.Entry<String, Color> entry : COLOR_MAP.entrySet()) {
            int row = index / columns;
            int col = index % columns;
            int x = col * cellWidth;
            int y = row * cellHeight;

            java.awt.Color awtColor = new java.awt.Color(entry.getValue().getRed(), entry.getValue().getGreen(), entry.getValue().getBlue());
            g.setColor(awtColor);
            g.fillRect(x, y, cellWidth, cellHeight);

            g.setColor(java.awt.Color.BLACK);
            g.drawRect(x, y, cellWidth, cellHeight);
            if ( awtColor.equals(Color.of(0, 255, 255))) {
                g.setColor(java.awt.Color.BLACK);
            } else {
                g.setColor((awtColor.getRed() * 0.299 + awtColor.getGreen() * 0.687 + awtColor.getBlue() * 0.114) > 186 ? java.awt.Color.BLACK : java.awt.Color.WHITE);
            }
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(entry.getKey());
            int textHeight = metrics.getAscent();
            int textX = x + (cellWidth - textWidth) / 2;
            int textY = y + (cellHeight + textHeight) / 2 - 5;
            g.drawString(toTitleCase(entry.getKey()), textX, textY);

            index++;
        }

        g.dispose();

        try {
            File outputFile = new File("color_chart.png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Color chart saved as color_chart.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
