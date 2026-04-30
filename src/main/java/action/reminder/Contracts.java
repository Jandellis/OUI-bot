package action.reminder;

import action.Action;
import action.reminder.model.Contract;
import action.reminder.model.ContractMessage;
import action.reminder.model.FlexStats;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import action.reminder.model.ReminderTimes;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contracts extends Action  {

    String tacoBot = "490707751832649738";
    Long recruiter;
    List<String> patreonServers;
    String reloadEmote = "\uD83D\uDD04";

    public Contracts() {
        patreonServers = Arrays.asList(config.get("patreonServers").split(","));
        recruiter = Long.parseLong(config.get("recruiter"));
    }
    // Make a db table with id's taco of messages and ids of cylon matching message
    // With reaction event, update the same message


    @Override
    public Mono<Object> doAction(Message message) {

        if (message.getData().author().id().asString().equals(tacoBot)) {
            try {

                if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0) {


                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title("Contract Estimates");
                    if (contractTypeCheck(message, "Active Contract")) {
                        Profile profile = null;
                        String userId = getContainerUser(message);
                        profile = ReminderUtils.loadProfileByUserName(userId);
                        Contract contract = getContract(message);
                        if (contract == null){
                            return Mono.empty();
                        }
                        logger.info(contract.toString());

                        ReminderTimes reminderTimes = getReminderTimes(profile, message);
                        boolean work = false;
                        boolean overtime = false;
                        boolean tips = false;
                        if (contract.getName().contains("Overtime")) {
                            overtime = true;
                        }
                        if (contract.getName().contains("Work") || contract.getName().contains("Shift")) {
                            work = true;
                        }
                        if (contract.getName().contains("Tips") || contract.getName().contains("Service")) {
                            tips = true;
                        }
                        if (contract.getName().contains("Double Time")) {
                            work = true;
                            overtime = true;
                        }
                        if (contract.getName().contains("Corporate Restructuring")) {
                            work = true;
                            overtime = true;
                            tips = true;
                        }
                        if (contract.getObjective().contains("from work")) {
                            work = true;
                        }
                        if (contract.getObjective().contains("from tips")) {
                            tips = true;
                        }
                        if (contract.getObjective().contains("from overtime")) {
                            overtime = true;
                        }
                        if (contract.getObjective().contains("from work and overtime")) {
                            overtime = true;
                            work = true;
                        }


                        if (contract.getName().contains("Corporate Restructuring") || contract.getName().contains("VIP") || contract.getName().contains("Double Time")) {

                            if (profile.getOvertimeIncome() > 1 && profile.getWorkIncome() > 1 && profile.getTipsIncome() > 1) {
                                int sleep = minutesToCompleteEarningsGoal(profile,
                                        (contract.getTotal() - contract.getProgress()),
                                        reminderTimes.getWork() * contract.getTipsCoolDown(),
                                        reminderTimes.getOvertime() * contract.getOvertimeCoolDown(),
                                        reminderTimes.getTips() * contract.getTipsCoolDown(),
                                        work,
                                        overtime,
                                        tips,
                                        contract.getWorkBuff(),
                                        contract.getOvertimeBuff(),
                                        contract.getTipsBuff(),
                                        true
                                );
                                logger.info("time left is {} min", sleep);
                                String timeStr;
                                if (sleep >= 60) {
                                    int hours = sleep / 60;
                                    int mins = sleep % 60;
                                    timeStr = hours + "h " + mins + "m";
                                } else {
                                    timeStr = sleep + "m";
                                }

                                String printMessage = "It will take you " + timeStr + " to earn $" + String.format("%,d", (contract.getTotal() - contract.getProgress()));
                                embed.addField(contract.getName(), printMessage, false);
                            }
                        } else {
                            if (work) {
                                int sleep = 0;
                                sleep = profile.getStatus().getWork();
                                embed.addField(contract.getName(), printSimpleContract(contract, profile, message, sleep, "work", true), false);
                            }
                            if (tips) {
                                int sleep = 0;
                                sleep = profile.getStatus().getTips();
                                embed.addField(contract.getName(), printSimpleContract(contract, profile, message, sleep, "tips", true), false);
                            }
                            if (overtime) {
                                int sleep = 0;
                                sleep = profile.getStatus().getOt();
                                embed.addField(contract.getName(), printSimpleContract(contract, profile, message, sleep, "overtime", true), false);
                            }
                        }

                        Message cylonMsg = message.getChannel().flatMap(channel -> {
                            return channel.createMessage(MessageCreateSpec.builder()
                                    .addEmbed(embed.build())
                                    .build());
                        }).block();
                        ReminderUtils.createContractMessage(message.getId().asString(), cylonMsg.getId().asString());

                        react(message, profile);

                    }
                    if (contractTypeCheck(message, "Catering Contracts")){

                        Profile profile = null;
                        String userId = getContainerUser(message);
                        profile = ReminderUtils.loadProfileByUserName(userId);
                        List<Contract> contracts = getContracts(message);
                        for (Contract contract : contracts) {
                            if (contract.getName().endsWith("Service Specialist")){
                                embed.addField(contract.getName(), tipsContracts(contract, profile, message), false);
                            }
                            if (contract.getName().endsWith("Overtime Specialist")){
                                embed.addField(contract.getName(), overtimeContracts(contract, profile, message), false);
                            }
                            if (contract.getName().endsWith("Shift Specialist")){
                                embed.addField(contract.getName(), workContracts(contract, profile, message), false);
                            }
                            if (contract.getName().endsWith("Double Time Specialist")){
                                embed.addField(contract.getName(), doubleTimeContracts(contract, profile, message), false);
                            }
                            if (contract.getName().endsWith("Corporate Restructuring")){
                                embed.addField(contract.getName(), restructuringContract(contract, profile, message), false);
                            }
                            if (contract.getName().contains("VIP")){
                                if (contract.getObjective().endsWith("work")) {
                                    embed.addField(contract.getName(), workVIPContracts(contract, profile, message), false);
                                }
                                if (contract.getObjective().endsWith("overtime")) {
                                    embed.addField(contract.getName(), overtimeVIPContracts(contract, profile, message), false);
                                }
                                if (contract.getObjective().endsWith("tips")) {
                                    embed.addField(contract.getName(), tipsVIPContracts(contract, profile, message), false);
                                }
                            }
                        }
                        ContractMessage contractMessage = ReminderUtils.loadContractMessage(message.getId().asString());
                        if (contractMessage != null) {
                            message.getChannel().flatMap(channel ->
                                    channel.getMessageById(Snowflake.of(contractMessage.getCylonId()))
                                            .flatMap(msg -> msg.edit(MessageEditSpec.builder()
                                                    .addEmbed(embed.build())
                                                    .build()))
                            ).block();
                        }
                        else {

                            Message cylonMsg = message.getChannel().flatMap(channel -> {
                                return channel.createMessage(MessageCreateSpec.builder()
                                        .addEmbed(embed.build())
                                        .build());
                            }).block();
                            ReminderUtils.createContractMessage(message.getId().asString(), cylonMsg.getId().asString());

                            react(message, profile);
                        }
                    }
                    return Mono.empty();
                } else {

                    return Mono.empty();
                }

            } catch (Exception e) {
                printException(e);
            }

        }

        return Mono.empty();
    }


protected Mono<Object> doUpdateEvent(MessageUpdateEvent reactionAddEvent) {
//
    /**
     * Double Time Specialist - work and ot
     * VIP Service - tips
     * VIP Catering - work
     * VIP Overtime - ot
     * Service Specialist - tips
     * Shift Specialist - work
     * Overtime Specialist - ot
     * Corporate Restructuring - work, ot, tips
     *
     *
     *
     */
    Message message = reactionAddEvent.getMessage().block();
    if (message.getData().author().id().asString().equals(tacoBot)) {
        try {


            String userId = getContainerUser(message);
            if (userId == null) {
                return Mono.empty();
            }
            Profile profile = ReminderUtils.loadProfileByUserName(userId);
            if (profile == null) {
                return Mono.empty();
            }
            Message freshMessage = message.getClient()
                    .withRetrievalStrategy(EntityRetrievalStrategy.REST)
                    .getMessageById(message.getChannelId(), message.getId())
                    .block();
            ContractMessage contractMessage = ReminderUtils.loadContractMessage(message.getId().asString());


            if (contractTypeCheck(freshMessage, "Corporate Restructuring") || contractTypeCheck(freshMessage, "VIP") || contractTypeCheck(freshMessage, "Double Time Specialist") || contractTypeCheck(freshMessage, "Specialist")){

                // if buttons in it are disabled, need to ingore the message

                Contract contract = getContract(freshMessage);
                if (contract.getName().equals("Catering Contracts")) {
                    logger.info("Catering Contracts has been updated, it shout not get here");
                    return doAction(freshMessage);
                }
                // check if anything in type 1, then type 2 is disabled = true
                // if so return and do not process



                boolean work = false;
                boolean overtime = false;
                boolean tips = false;
                if (contract.getName().contains("Overtime")) {
                    overtime = true;
                }
                if (contract.getName().contains("Work") || contract.getName().contains("Shift")) {
                    work = true;
                }
                if (contract.getName().contains("Tips") || contract.getName().contains("Service")) {
                    tips = true;
                }
                if (contract.getName().contains("Double Time")) {
                    work = true;
                    overtime = true;
                }
                if (contract.getName().contains("Corporate Restructuring")) {
                    work = true;
                    overtime = true;
                    tips = true;
                }
                if (contract.getObjective().contains("from work")) {
                    work = true;
                }
                if (contract.getObjective().contains("from tips")) {
                    tips = true;
                }
                if (contract.getObjective().contains("from overtime")) {
                    overtime = true;
                }
                if (contract.getObjective().contains("from work and overtime")) {
                    overtime = true;
                    work = true;
                }


//                logger.info(freshMessage.toString());
                logger.info(contract.toString());

                List<String> id = new ArrayList<>();
                id.add(profile.getName());
                List<FlexStats> flexStats = ReminderUtils.loadFlexStats(0,7 , id);
                long flex = getFlexValues(profile, contract, message, flexStats);
                long flexBaseline = getBaselineFlexValues(profile, message, flexStats);
                long defaultBaseline = getBaselineDefaultValues(profile, freshMessage);
                long defaultValue = getDefaultValues(profile, contract, freshMessage);

                logger.info("flex={}, flexBaseline={}, defaultValue={}, defaultBaseline={}",
                        flex, flexBaseline, defaultValue, defaultBaseline);

                double flexPct = flex == 0 ? 0 : (double)(flex - flexBaseline) / flex * 100;
                double defaultPct = defaultValue == 0 ? 0 : (double)(defaultValue - defaultBaseline) / defaultValue * 100;

                ReminderTimes reminderTimes = getReminderTimes(profile, freshMessage);


                int sleep = minutesToCompleteEarningsGoal(profile,
                        (contract.getTotal() - contract.getProgress()),
                        reminderTimes.getWork() * contract.getTipsCoolDown(),
                        reminderTimes.getOvertime() * contract.getOvertimeCoolDown(),
                        reminderTimes.getTips() * contract.getTipsCoolDown(),
                        work,
                        overtime,
                        tips,
                        contract.getWorkBuff(),
                        contract.getOvertimeBuff(),
                        contract.getTipsBuff(), false);
                logger.info("time left is {} min", sleep);
                String timeStr;
                if (sleep >= 60) {
                    int hours = sleep / 60;
                    int mins = sleep % 60;
                    timeStr = hours + "h " + mins + "m";
                } else {
                    timeStr = sleep + "m";
                }
                String printMessage;


                EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                embed.color(Color.SUMMER_SKY);
                embed.title(contract.getName() + " Estimates");

                //need to work out how many it is, then / by the total they do in a week and work out how long
                // so 200 work, do 500 in a week, its 200/500 =  0.4. 0.4*7 = 2.8 days, then convert to minutes

                //for the money ones, count the tips/ot/work it took, then do the same as above and take the largest number
                // need to update the methods below to also return the counts so i dont have to work that out twice

                if (contract.getName().endsWith("Service Specialist")){
                    embed.addField("Length", tipsContracts(contract, profile, message), false);
                }
                if (contract.getName().endsWith("Overtime Specialist")){
                    embed.addField("Length", overtimeContracts(contract, profile, message), false);
                }
                if (contract.getName().endsWith("Shift Specialist")){
                    embed.addField("Length", workContracts(contract, profile, message), false);
                }
                if (contract.getName().endsWith("Double Time Specialist")){
                    embed.addField("Length", doubleTimeContracts(contract, profile, message), false);
                }
                if (contract.getName().endsWith("Corporate Restructuring")){
                    embed.addField("Length", restructuringContract(contract, profile, message), false);
                }
                if (contract.getName().contains("VIP")){
                    if (contract.getObjective().endsWith("work")) {
                        embed.addField("Length", workVIPContracts(contract, profile, message), false);
                    }
                    if (contract.getObjective().endsWith("overtime")) {
                        embed.addField("Length", overtimeVIPContracts(contract, profile, message), false);
                    }
                    if (contract.getObjective().endsWith("tips")) {
                        embed.addField("Length", tipsVIPContracts(contract, profile, message), false);
                    }
                }

                String increaseMessage = String.format(
                        "This will be a **%.1f%%** increase if you grind non stop. For you I recommend it will be a **%.1f%%** increase if you grind normally.",
                        defaultPct, flexPct
                );
                embed.addField("Gain", increaseMessage, false);



                message.getChannel().flatMap(channel ->
                        channel.getMessageById(Snowflake.of(contractMessage.getCylonId()))
                                .flatMap(msg -> msg.edit(MessageEditSpec.builder()
                                        .addEmbed(embed.build())
                                        .build()))
                ).block();

            }

        } catch (Exception e) {
            printException(e);
        }
    }
        return Mono.empty();

}


    public Contract getContract(Message message) {
        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) return null;

        Pattern effectPattern = Pattern.compile("\\*\\*([+-]\\d+)%\\*\\*\\s+(.*)");
        Pattern progressPattern = Pattern.compile("\\*\\*Progress:\\*\\*\\s*[^`]*`\\$?([\\d,]+)/\\$?([\\d,]+)`");
        Pattern objectiveTotalPattern = Pattern.compile("\\*\\*Objective:\\*\\*.*`\\$?([\\d,]+)`");
        Pattern namePattern = Pattern.compile("^#{1,3}\\s+\\S+\\s+(.+)$");

        Contract contract = null;

        for (ComponentData top : possible.get()) {
            if (top.components().isAbsent()) continue;
            for (ComponentData item : top.components().get()) {
                if (item.type() != 10 || item.content().isAbsent()) continue;
                String text = item.content().get();

                // parse contract name
                Matcher nameMatcher = namePattern.matcher(text.trim());
                if (nameMatcher.find() && !text.contains("Active Contract")) {
                    if (contract == null) {
                        contract = new Contract(nameMatcher.group(1).trim(), null, null);
                    }
                }

                // parse progress/total (active contract)
                Matcher progressMatcher = progressPattern.matcher(text);
                if (progressMatcher.find() && contract != null) {
                    contract.setProgress((int) Long.parseLong(progressMatcher.group(1).replace(",", "")));
                    contract.setTotal((int) Long.parseLong(progressMatcher.group(2).replace(",", "")));
                }

                // parse objective/rewards/total from offer-style contract
                for (String line : text.split("\n")) {
                    if (line.contains("Rewards:") && contract != null) {
                        contract.setRewards(line.replaceAll(".*\\*\\*Rewards:\\*\\*\\s*", "").trim());
                    }
                    if (line.contains("Objective:") && contract != null) {
                        contract.setObjective(line.replaceAll(".*\\*\\*Objective:\\*\\*\\s*", "").trim());
                        // try to extract total from objective line
                        Matcher totalMatcher = objectiveTotalPattern.matcher(line);
                        if (totalMatcher.find()) {
                            contract.setTotal((int) Long.parseLong(totalMatcher.group(1).replace(",", "")));
                        }
                    }
                    if (line.contains("Progress:") && contract != null) {
                        contract.setObjective(line.replaceAll(".*\\*\\*Progress:\\*\\*\\s*", "").trim());
                    }
                }

                // parse active effects OR contract effects
                if (!text.contains("Active Effects") && !text.contains("Contract Effects")) continue;
                for (String line : text.split("\n")) {
                    Matcher m = effectPattern.matcher(line.trim());
                    boolean found = m.find();
                    if (!found) continue;

                    int percent = Integer.parseInt(m.group(1));
                    double multiplier = 1 + (percent / 100.0);
                    String type = m.group(2).trim();

                    if (contract == null) continue;

                    switch (type) {
                        case "Work Payout":       contract.setWorkBuff(multiplier); break;
                        case "Work Cooldown":     contract.setWorkCoolDown(multiplier); break;
                        case "Tips Payout":       contract.setTipsBuff(multiplier); break;
                        case "Tips Cooldown":     contract.setTipsCoolDown(multiplier); break;
                        case "Overtime Payout":   contract.setOvertimeBuff(multiplier); break;
                        case "Overtime Cooldown": contract.setOvertimeCoolDown(multiplier); break;
                        default: logger.info("unknown effect type: {}", type); break;
                    }
                }
            }
        }
        return contract;
    }


    public List<Contract> getContracts(Message message) {
        List<Contract> contracts = new ArrayList<>();
        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) return contracts;

//        Pattern namePattern = Pattern.compile("\\*\\*(.+?)\\*\\*\\s*$");
        Pattern namePattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
        Pattern objectivePattern = Pattern.compile("\\*\\*Objective:\\*\\*\\s*(.+)");
        Pattern rewardsPattern = Pattern.compile("\\*\\*Rewards:\\*\\*\\s*(.+)");

        for (ComponentData top : possible.get()) {
            if (top.components().isAbsent()) continue;
            for (ComponentData section : top.components().get()) {
                // type 9 = Section
                if (section.type() != 9) continue;
                if (section.components().isAbsent()) continue;

                for (ComponentData item : section.components().get()) {
                    if (item.type() != 10 || item.content().isAbsent()) continue;

                    String text = item.content().get();
                    String name = null;
                    String objective = null;
                    String rewards = null;

                    logger.info("parsing section text: {}", text.substring(0, Math.min(50, text.length())));

                    for (String line : text.split("\n")) {
                        line = line.trim();

                        if (name == null && !line.startsWith("📌")) {
                            Matcher m = namePattern.matcher(line);
                            if (m.find()) {
                                name = m.group(1).trim();
                            }
                        }

                        if (objective == null) {
                            Matcher m = objectivePattern.matcher(line);
                            if (m.find()) {
                                objective = m.group(1).trim();
                            }
                        }
                        if (rewards == null) {
                            Matcher m = rewardsPattern.matcher(line);
                            if (m.find()) {
                                rewards = m.group(1).trim();
                            }
                        }
                    }

                    if (name != null && objective != null) {
                        logger.info("found contract: '{}' -> '{}', {}", name, objective, rewards);
                        contracts.add(new Contract(name, objective, rewards));
                    }
                }
            }
        }
        return contracts;
    }

    public String tipsVIPContracts(Contract contract, Profile profile, Message message) {
        return VIPContracts(contract, profile, message, "tips");
    }

    public String workVIPContracts(Contract contract, Profile profile, Message message) {
        return VIPContracts(contract, profile, message, "work");
    }

    public String overtimeVIPContracts(Contract contract, Profile profile, Message message) {

        return VIPContracts(contract, profile, message, "overtime");
    }
    public String restructuringContract(Contract contract, Profile profile, Message message) {

        return VIPContracts(contract, profile, message, "work, tips and overtime");
    }

    public String VIPContracts(Contract contract, Profile profile, Message message, String type) {

//        String obj = contract.getObjective().split("`")[1];
        String obj = "";
        if (contract.getObjective().contains("/")) {
            obj = (contract.getTotal() - contract.getProgress()) + "";
        } else {
            obj = contract.getObjective().split("`")[1];
        }
        int value = Integer.parseInt(obj.replace(",", "").replace("$", ""));
        ReminderTimes reminderTimes = getReminderTimes(profile, message);
        boolean work = false;
        boolean overtime = false;
        boolean tips = false;
        if (type.equals("overtime")) {
            overtime = true;
        }
        if (type.equals("work")) {
            work = true;
        }
        if (type.equals("tips")) {
            tips = true;
        }
        if (type.equals("work, tips and overtime")) {
            tips = true;
            work = true;
            overtime = true;
        }
        if (profile.getOvertimeIncome() < 1 && profile.getWorkIncome() < 1 && profile.getTipsIncome() < 1) {
            return "";
        }

        int sleep = minutesToCompleteEarningsGoal(profile, value, reminderTimes.getWork(), reminderTimes.getOvertime(), reminderTimes.getTips(), work, overtime, tips, contract.getWorkBuff(), contract.getOvertimeBuff(),contract.getTipsBuff(), false);
        String timeStr;
        if (sleep >= 60) {
            int hours = sleep / 60;
            int mins = sleep % 60;
            timeStr = hours + "h " + mins + "m";
        } else {
            timeStr = sleep + "m";
        }
        String without = "**Without the buffs**, ";
        if (contract.getWorkBuff() > 1 || contract.getOvertimeBuff() > 1 || contract.getTipsBuff() > 1) {
            without = "With Buffs, ";
        }

        String printMessage = without + "it will take you **" + timeStr + "** to earn " + obj + " with "+ type;
        return printMessage;

    }

    public String tipsContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getTips();
        return printSimpleContract(contract, profile, message, sleep, "tips", false);
    }

    public String workContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getWork();
        return printSimpleContract(contract, profile, message, sleep, "work", false);
    }

    public String overtimeContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getOt();
        return printSimpleContract(contract, profile, message, sleep, "overtime", false);
    }

    public String doubleTimeContracts(Contract contract, Profile profile, Message message) {


//        String obj = contract.getObjective().split("`")[1];

        String obj = "";
        if (contract.getObjective().contains("/")) {
            obj = (contract.getTotal() - contract.getProgress()) + "";
        } else {
            obj = contract.getObjective().split("`")[1];
        }
        int value = Integer.parseInt(obj.replace(",", "").replace("$", ""));

        ReminderTimes reminderTimes = getReminderTimes(profile, message);

        if (profile.getOvertimeIncome() < 1 && profile.getWorkIncome() < 1 && profile.getTipsIncome() < 1) {
            return "";
        }

        int sleep = minutesToCompleteEarningsGoal(profile, value, reminderTimes.getWork(), reminderTimes.getOvertime(), profile.getStatus().getTips(), true, true, false, contract.getWorkBuff(), contract.getOvertimeBuff(), contract.getTipsBuff(), false);
        String timeStr;
        if (sleep >= 60) {
            int hours = sleep / 60;
            int mins = sleep % 60;
            timeStr = hours + "h " + mins + "m";
        } else {
            timeStr = sleep + "m";
        }
        String without = "**Without the buffs**, ";
        if (contract.getWorkBuff() > 1 || contract.getOvertimeBuff() > 1 || contract.getTipsBuff() > 1) {
            without = "With Buffs, ";
        }

        String printMessage = without + "it will take you " + timeStr + " to earn " + obj + " with with overtime and work";

        return printMessage;
    }

//    public int minutesToCompleteEarningsGoal(Profile profile, long goal, int sleepWork, int sleepOvertime, int sleepTips, boolean includeWork, boolean includeOvertime, boolean includeTips, double workBuff, double overtimeBuff, double tipBuff, boolean inProgress) {
//        logger.info("minutesToCompleteEarningsGoal - goal: {}, sleepWork: {}, sleepOvertime: {}, sleepTips: {}, includeWork: {}, includeOvertime: {}, includeTips: {}, workBuff: {}, overtimeBuff: {}, tipBuff: {}",
//                goal, sleepWork, sleepOvertime, sleepTips, includeWork, includeOvertime, includeTips, workBuff, overtimeBuff, tipBuff);
//
//        List<Reminder> reminders =  ReminderUtils.loadReminder(profile.getName());
//
//
//        long totalEarned = 0;
//        int totalMinutes = 0;
//
//        int workCooldown = 0;
//        int overtimeCooldown = 0;
//        int tipsCooldown = 0;
//
//        long nowMillis = System.currentTimeMillis();
//
//        if (inProgress) {
//            for (Reminder reminder : reminders) {
//                int minutesUntil = (int) ((reminder.getTime().getTime() - nowMillis) / 1000 / 60);
//                if (minutesUntil < 0) minutesUntil = 0;
//
//                switch (reminder.getType()) {
//                    case work:
//                        workCooldown = minutesUntil;
//                        break;
//                    case tips:
//                        tipsCooldown = minutesUntil;
//                        break;
//                    case ot:
//                        overtimeCooldown = minutesUntil;
//                        break;
//                }
//            }
//        }
//
//        int max = 60*24*7*4;
//        if (!includeWork && !includeOvertime && !includeTips) {
//            return max;
//        }
//
//
//        while (totalEarned < goal && totalMinutes < max) {
//            // advance time by 1 minute
//            totalMinutes++;
//            if (workCooldown > 0) workCooldown--;
//            if (overtimeCooldown > 0) overtimeCooldown--;
//            if (tipsCooldown > 0) tipsCooldown--;
//
//            // do work if available
//            if (workCooldown == 0 && includeWork) {
//                totalEarned += (long) (profile.getWorkIncome()* workBuff);
//                workCooldown = sleepWork;
//            }
//
//            // do overtime if available
//            if (overtimeCooldown == 0 && includeOvertime) {
//                totalEarned += (long) (profile.getOvertimeIncome()*  overtimeBuff);
//                overtimeCooldown = sleepOvertime;
//            }
//            if (tipsCooldown == 0 && includeTips) {
//                totalEarned += (long) (profile.getTipsIncome()*  tipBuff);
//                tipsCooldown = sleepTips;
//            }
//        }
//
//        return totalMinutes;
//    }


    public int minutesToCompleteEarningsGoal(Profile profile, long goal, double sleepWork, double sleepOvertime, double sleepTips, boolean includeWork, boolean includeOvertime, boolean includeTips, double workBuff, double overtimeBuff, double tipBuff, boolean inProgress) {
        logger.info("minutesToCompleteEarningsGoal - goal: {}, sleepWork: {}, sleepOvertime: {}, sleepTips: {}, includeWork: {}, includeOvertime: {}, includeTips: {}, workBuff: {}, overtimeBuff: {}, tipBuff: {}",
                goal, sleepWork, sleepOvertime, sleepTips, includeWork, includeOvertime, includeTips, workBuff, overtimeBuff, tipBuff);

        List<Reminder> reminders = ReminderUtils.loadReminder(profile.getName());

        long totalEarned = 0;
        int totalSeconds = 0;

        // convert sleep minutes to seconds
        double workCooldown = 0;
        double overtimeCooldown = 0;
        double tipsCooldown = 0;

        double sleepWorkSec = sleepWork * 60;
        double sleepOvertimeSec = sleepOvertime * 60;
        double sleepTipsSec = sleepTips * 60;

        long nowMillis = System.currentTimeMillis();

        if (inProgress) {
            for (Reminder reminder : reminders) {
                double secondsUntil = (reminder.getTime().getTime() - nowMillis) / 1000.0;
                if (secondsUntil < 0) secondsUntil = 0;

                switch (reminder.getType()) {
                    case work:
                        workCooldown = secondsUntil;
                        break;
                    case tips:
                        tipsCooldown = secondsUntil;
                        break;
                    case ot:
                        overtimeCooldown = secondsUntil;
                        break;
                }
            }
        }

        int max = 60 * 24 * 7 * 2 * 60; // in seconds
        if (!includeWork && !includeOvertime && !includeTips) {
            return max / 60;
        }

        while (totalEarned < goal && totalSeconds < max) {
            totalSeconds++;
            if (workCooldown > 0) workCooldown--;
            if (overtimeCooldown > 0) overtimeCooldown--;
            if (tipsCooldown > 0) tipsCooldown--;

            if (workCooldown <= 0 && includeWork) {
                totalEarned += (long) (profile.getWorkIncome() * workBuff);
                workCooldown = sleepWorkSec;
            }

            if (overtimeCooldown <= 0 && includeOvertime) {
                totalEarned += (long) (profile.getOvertimeIncome() * overtimeBuff);
                overtimeCooldown = sleepOvertimeSec;
            }

            if (tipsCooldown <= 0 && includeTips) {
                totalEarned += (long) (profile.getTipsIncome() * tipBuff);
                tipsCooldown = sleepTipsSec;
            }
        }

        return (int) Math.round(totalSeconds / 60.0);
    }

    public String  printSimpleContract(Contract contract, Profile profile, Message message, int sleep, String type, boolean remianing){

        String obj = "";
        if (remianing || contract.getObjective().contains("/")) {
            obj = (contract.getTotal() - contract.getProgress()) + "";
        } else {
            obj = contract.getObjective().split("`")[1];
        }
        int value = Integer.parseInt(obj.replace(",", ""));


        AtomicBoolean isPatreonServer = new AtomicBoolean(false);
        if (message.getGuildId().isPresent()) {
            patreonServers.forEach(server -> {
                if (message.getGuildId().get().asString().equals(server)) {
                    isPatreonServer.set(true);
                }
            });
        }

        if (!isPatreonServer.get() && !type.equals("overtime")) {
            sleep = sleep + 1;
        }
        sleep = sleep * value;
        String timeStr;
        if (sleep >= 60) {
            int hours = sleep / 60;
            int mins = sleep % 60;
            timeStr = hours + "h " + mins + "m";
        } else {
            timeStr = sleep + "m";
        }
        String printMessage ="It will take you **" + timeStr + "** to complete " + value + " " + type;
        return printMessage;
    }

//    public boolean contractTypeCheck(Message message, String type) {
//        Possible<List<ComponentData>> possible = message.getData().components();
//        if (possible.isAbsent()) return false;
//
//        for (ComponentData top : possible.get()) {
//            if (top.components().isAbsent()) continue;
//            for (ComponentData item : top.components().get()) {
//                if (item.type() != 10 || item.content().isAbsent()) continue;
//                if (item.content().get().contains(type)) return true;
//            }
//        }
//        return false;
//    }

    public boolean contractTypeCheck(Message message, String type) {
        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) {
            logger.info("contractTypeCheck - components absent");
            return false;
        }

        for (ComponentData top : possible.get()) {
            if (top.components().isAbsent()) continue;
            for (ComponentData item : top.components().get()) {
                // check type 10 directly
                if (item.type() == 10 && item.content().isPresent()) {
//                    logger.info("contractTypeCheck - checking: '{}'", item.content().get().substring(0, Math.min(50, item.content().get().length())));
                    if (item.content().get().contains(type)) return true;
                }
                // check inside type 9 sections
                if (item.type() == 9 && item.components().isPresent()) {
                    for (ComponentData inner : item.components().get()) {
                        if (inner.type() == 10 && inner.content().isPresent()) {
//                            logger.info("contractTypeCheck - checking inner: '{}'", inner.content().get().substring(0, Math.min(50, inner.content().get().length())));
                            if (inner.content().get().contains(type)) {
                                logger.info("contractTypeCheck - match found for '{}', matched to {}", type, inner.content().get());
                                return true;
                            }
                        }
                    }
                }
            }
        }
        logger.info("contractTypeCheck - no match found for '{}'", type);
        return false;
    }


    public long getAmount(Profile profile, Contract contract, int workCount, int tipsCount, int overtimeCount) {
        long total = 0;
        total = (long) (profile.getWorkIncome() * contract.getWorkBuff() * workCount);
        total = total + (long) (profile.getTipsIncome() * contract.getTipsBuff() * tipsCount);
        total = total + (long) (profile.getOvertimeIncome() * contract.getOvertimeBuff() * overtimeCount);

        return total;
    }

    public long getBaselineDefaultValues(Profile profile, Message message) {
        Contract contract = new Contract(null, null, null);
        return getDefaultValues(profile, contract, message);
    }
    public long getDefaultValues (Profile profile, Contract contract, Message message) {
        ReminderTimes reminderTimes = getReminderTimes(profile, message);
        int work = (int) (1440.0 / (reminderTimes.getWork() * contract.getWorkCoolDown()));
        int tips = (int) (1440.0 / (reminderTimes.getTips() * contract.getTipsCoolDown()));
        int ot   = (int) (1440.0 / (reminderTimes.getOvertime() * contract.getOvertimeCoolDown()));
        logger.info("getDefaultValues - work={}, tips={}, ot={}", work, tips, ot);
        return getAmount(profile, contract, work, tips, ot);
    }

    public long getBaselineFlexValues(Profile profile, Message message, List<FlexStats> flexStats ) {
            Contract contract = new Contract(null, null, null);
            return getFlexValues(profile, contract, message, flexStats);
    }

    public long getFlexValues (Profile profile, Contract contract, Message message, List<FlexStats> flexStats ) {
        if (flexStats.isEmpty()) {
            return getDefaultValues(profile, contract, message);
        }
        int work = (int) ((flexStats.get(flexStats.size()-1).getWork() - flexStats.get(0).getWork()) / contract.getWorkCoolDown() / 7);
        int tips = (int) ((flexStats.get(flexStats.size()-1).getTips() - flexStats.get(0).getTips()) / contract.getTipsCoolDown() / 7);
        int ot   = (int) ((flexStats.get(flexStats.size()-1).getOvertime() - flexStats.get(0).getOvertime()) / contract.getOvertimeCoolDown() / 7);
        logger.info("getFlexValues - work={}, tips={}, ot={}", work, tips, ot);
        return getAmount(profile, contract, work, tips, ot);
    }

    private ReminderTimes getReminderTimes (Profile profile, Message message) {
        ReminderTimes reminderTimes = new ReminderTimes();

        int sleepWork = 0;
        sleepWork = profile.getStatus().getWork();
        int sleepTips = 0;
        sleepTips = profile.getStatus().getTips();
        AtomicBoolean isPatreonServer = new AtomicBoolean(false);
        if (message.getGuildId().isPresent()) {
            patreonServers.forEach(server -> {
                if (message.getGuildId().get().asString().equals(server)) {
                    isPatreonServer.set(true);
                }
            });
        }

//        if (!isPatreonServer.get()) {
//            sleepWork = sleepWork + 1;
//            sleepTips = sleepTips + 1;
//        }
        reminderTimes.setWork(sleepWork);
        reminderTimes.setTips(sleepTips);
        reminderTimes.setOvertime(profile.getStatus().getOt());
        logger.info(reminderTimes.toString());
        return reminderTimes;
    }
}

