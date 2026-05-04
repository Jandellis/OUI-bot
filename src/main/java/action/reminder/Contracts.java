package action.reminder;

import action.Action;
import action.reminder.model.Contract;
import action.reminder.model.ContractEstimate;
import action.reminder.model.ContractMessage;
import action.reminder.model.ContractActionType;
import action.reminder.model.ContractType;
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

    @Override
    public Mono<Object> doAction(Message message) {

        if (message.getData().author().id().asString().equals(tacoBot)) {
            try {

                if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0) {

                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title("Contract Estimates");
                    if (contractTypeCheck(message, ContractType.ACTIVE_CONTRACT)) {
                        Profile profile = null;
                        String userId = getContainerUser(message);
                        profile = ReminderUtils.loadProfileByUserName(userId);

                        List<String> id = new ArrayList<>();
                        id.add(profile.getName());
                        List<FlexStats> flexStats = ReminderUtils.loadFlexStats(0,7 , id);

                        Contract contract = getContract(message);
                        parseParticipantsAndRewards(message, contract);

                        if (contract == null){
                            return Mono.empty();
                        }
                        logger.info( "Active - " + contract.toString());

                        ReminderTimes reminderTimes = getReminderTimes(profile, message);
                        boolean work = false;
                        boolean overtime = false;
                        boolean tips = false;
                        if (contract.isContractType(ContractType.OVERTIME_SPECIALIST) || contract.isContractType(ContractType.VIP_OVERTIME)) {
                            overtime = true;
                        }
                        if (contract.isContractType(ContractType.SHIFT_SPECIALIST) || contract.isContractType(ContractType.VIP_CATERING)) {
                            work = true;
                        }
                        if (contract.isContractType(ContractType.SERVICE_SPECIALIST) || contract.isContractType(ContractType.VIP_SERVICE)) {
                            tips = true;
                        }
                        if (contract.isContractType(ContractType.DOUBLE_TIME_SPECIALIST)) {
                            work = true;
                            overtime = true;
                        }
                        if (contract.isContractType(ContractType.CORPORATE_RESTRUCTURING)) {
                            work = true;
                            overtime = true;
                            tips = true;
                        }
                        //fall back just in case
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


                        if (contract.isContractType(ContractType.CORPORATE_RESTRUCTURING) || contract.isContractType(ContractType.VIP) || contract.isContractType(ContractType.DOUBLE_TIME_SPECIALIST)) {

                            if (profile.getOvertimeIncome() > 1 && profile.getWorkIncome() > 1 && profile.getTipsIncome() > 1) {
                                ContractEstimate sleep = minutesToCompleteEarningsGoal(profile,
                                        contract,
                                        reminderTimes,
                                        work,
                                        overtime,
                                        tips,
                                        true
                                );
                                logger.info("time left is {} min", sleep);
                                String timeStr = formatMin(sleep.getMin());

                                String extra = "";
                                switch (contract.getActionType()) {
                                    case tips:
                                        extra = " (" + sleep.getTips() + " tips)";
                                        break;
                                    case work:
                                        extra = " (" + sleep.getWork() + " work)";
                                        break;
                                    case overtime:
                                        extra = " (" + sleep.getOvertime() + " overtime)";
                                        break;
                                    case all:
                                        extra = " (" + sleep.getOvertime() + " overtime, " + sleep.getWork() + " work, "+ sleep.getTips() + " tips)";
                                        break;
                                    case overtimeWork:
                                        extra = " (" + sleep.getOvertime() + " overtime, " + sleep.getWork() + " work)";
                                        break;
                                }

                                String printMessage = "It will take you " + timeStr + " to earn $" + String.format("%,d", (contract.getTotal() - contract.getProgress())) + extra;
                                embed.addField("⭐ " +contract.getName(), printMessage, false);
                            }
                        } else {
                            if (work) {
                                int sleep = 0;
                                sleep = profile.getStatus().getWork();
                                embed.addField("⭐ " +contract.getName(), grindXContract(contract, profile, message, sleep, ContractActionType.work, true, flexStats), false);
                            }
                            if (tips) {
                                int sleep = 0;
                                sleep = profile.getStatus().getTips();
                                embed.addField("⭐ " +contract.getName(), grindXContract(contract, profile, message, sleep, ContractActionType.tips, true, flexStats), false);
                            }
                            if (overtime) {
                                int sleep = 0;
                                sleep = profile.getStatus().getOt();
                                embed.addField("⭐ " +contract.getName(), grindXContract(contract, profile, message, sleep, ContractActionType.overtime, true, flexStats), false);
                            }
                        }
                        String rewards = getParticipantRewards(contract);
                        if (!rewards.isEmpty()){
                            embed.addField("Rewards", rewards, false);
                        }



                        Message cylonMsg = message.getChannel().flatMap(channel -> {
                            return channel.createMessage(MessageCreateSpec.builder()
                                    .addEmbed(embed.build())
                                    .build());
                        }).block();
                        ReminderUtils.createContractMessage(message.getId().asString(), cylonMsg.getId().asString());

                        react(message, profile);

                    }
                    if (contractTypeCheck(message, ContractType.CATERING_CONTRACTS)){

                        Profile profile = null;

                        String userId = getContainerUser(message);
                        profile = ReminderUtils.loadProfileByUserName(userId);
                        if (profile == null) {
                            return Mono.empty();
                        }
                        List<String> id = new ArrayList<>();
                        id.add(profile.getName());
                        List<FlexStats> flexStats = ReminderUtils.loadFlexStats(0,7 , id);

                        List<Contract> contracts = getContracts(message);
                        for (Contract contract : contracts) {
                            logger.info( "Catering Contract - " + contract.toString());
                            String estimate = getContractEstimate(contract, profile, message, flexStats);
                            if (!estimate.isEmpty()) {
                                embed.addField("⭐ " + contract.getName(), estimate, false);
                            }
                            ReminderUtils.insertContract(contract, false);
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


                if (contractTypeCheck(freshMessage, ContractType.CORPORATE_RESTRUCTURING) || contractTypeCheck(freshMessage, ContractType.VIP) || contractTypeCheck(freshMessage, ContractType.SPECIALIST)){

                    // if buttons in it are disabled, need to ingore the message

                    Contract contract = getContract(freshMessage);
                    if (contract.getName().equals("Catering Contracts")) {
                        logger.info("Catering Contracts has been updated, it shout not get here");
                        return doAction(freshMessage);
                    }
                    // check if anything in type 1, then type 2 is disabled = true
                    // if so return and do not process


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


                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title(contract.getName() + " Estimates");

                    //need to work out how many it is, then / by the total they do in a week and work out how long
                    // so 200 work, do 500 in a week, its 200/500 =  0.4. 0.4*7 = 2.8 days, then convert to minutes

                    //for the money ones, count the tips/ot/work it took, then do the same as above and take the largest number
                    // need to update the methods below to also return the counts so i dont have to work that out twice

                    String estimate = getContractEstimate(contract, profile, freshMessage, flexStats);
                    if (!estimate.isEmpty()) {
                        embed.addField("Length", estimate, false);
                    }

                    String increaseMessage = String.format(
                            " - Grind based on your history **%.1f%%** " +getEmote(flexPct)+"\n"+
                            " - Grind without missing any cooldowns **%.1f%%** "+getEmote(defaultPct),
                            defaultPct, flexPct
                    );
                    embed.addField("Boost", increaseMessage, false);



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
                    ReminderUtils.insertContract(contract, true);


    //                message.getChannel().flatMap(channel ->
    //                        channel.getMessageById(Snowflake.of(contractMessage.getCylonId()))
    //                                .flatMap(msg -> msg.edit(MessageEditSpec.builder()
    //                                        .addEmbed(embed.build())
    //                                        .build()))
    //                ).block();

                }

            } catch (Exception e) {
                printException(e);
            }
        }
        return Mono.empty();

    }


    private String getContractEstimate(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        ContractType contractType = ContractType.fromName(contract.getName());
        ContractActionType actionType = contractType.getAction();

        // fallback to objective if action is unknown
        if (actionType == null) {
            if (contract.getObjective().contains("from work and overtime")) {
                actionType = ContractActionType.overtimeWork;
            } else if (contract.getObjective().contains("from work")) {
                actionType = ContractActionType.work;
            } else if (contract.getObjective().contains("from tips")) {
                actionType = ContractActionType.tips;
            } else if (contract.getObjective().contains("from overtime")) {
                actionType = ContractActionType.overtime;
            } if (contract.getObjective().contains("active income")) {
                actionType = ContractActionType.all;
            }
        }

        if (actionType == null) {
            logger.info("getContractEstimate - could not determine action type for contract: {}", contract.getName());
            return "";
        }

        switch (contractType) {
            case SERVICE_SPECIALIST:
                return tipsContracts(contract, profile, message, flexStats);
            case OVERTIME_SPECIALIST:
                return overtimeContracts(contract, profile, message, flexStats);
            case SHIFT_SPECIALIST:
                return workContracts(contract, profile, message, flexStats);
            case DOUBLE_TIME_SPECIALIST:
                return doubleTimeContracts(contract, profile, message, flexStats);
            case CORPORATE_RESTRUCTURING:
                return restructuringContract(contract, profile, message, flexStats);
            case VIP_SERVICE:
                return tipsVIPContracts(contract, profile, message, flexStats);
            case VIP_CATERING:
                return workVIPContracts(contract, profile, message, flexStats);
            case VIP_OVERTIME:
                return overtimeVIPContracts(contract, profile, message, flexStats);
            default:
                // fallback using actionType for unknown VIP or other contracts
                switch (actionType) {
                    case work:     return workVIPContracts(contract, profile, message, flexStats);
                    case overtime: return overtimeVIPContracts(contract, profile, message, flexStats);
                    case tips:     return tipsVIPContracts(contract, profile, message, flexStats);
                    case overtimeWork: return doubleTimeContracts(contract, profile, message, flexStats);
                    case all:      return restructuringContract(contract, profile, message, flexStats);
                    default:
                        logger.info("getContractEstimate - unhandled action type: {}", actionType);
                        return "";
                }
        }
    }

    private String getEmote(double change) {
        String direction = "<a:up:1015020767244714004>";
        if (change < 0) {
            direction = "<a:down:1015020716929851453>";
        }
        return direction;
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
                        Pattern repPattern = Pattern.compile("\\+(\\d+)\\s+Rep(?:utation)?");
                        Matcher repMatcher = repPattern.matcher(line);
                        if (repMatcher.find()) {
                            contract.setRep(Integer.parseInt(repMatcher.group(1)));
//                            logger.info("found rep: {}", contract.getRep());
                        } else {
//                            logger.info("no rep found in rewards line: {}", line);
                        }
                    }

                    if (line.contains("Reputation:") && contract != null) {
                        Pattern repPattern = Pattern.compile("\\+?(\\d+)\\s+Reputation");
                        Matcher repMatcher = repPattern.matcher(line);
                        if (repMatcher.find()) {
                            contract.setRep(Integer.parseInt(repMatcher.group(1)));
                        }
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
        Pattern repPattern = Pattern.compile("\\+(\\d+)\\s+Rep(?:utation)?");

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
                    int rep = 0;
                    int total = 0;

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
                                total = Integer.parseInt(objective.split("`")[1].replace(",", "").replace("$", ""));
                            }
                        }

                        if (rewards == null) {
                            Matcher m = rewardsPattern.matcher(line);
                            if (m.find()) {
                                rewards = m.group(1).trim();
                                Matcher repMatcher = repPattern.matcher(line);
                                if (repMatcher.find()) {
                                    rep = Integer.parseInt(repMatcher.group(1));
                                    logger.info("found rep: {}", rep);
                                } else {
                                    logger.info("no rep found in rewards line: {}", line);
                                }
                            }
                        }
                    }

                    if (name != null && objective != null) {
                        logger.info("found contract: '{}' -> '{}', {}, {}", name, objective, rewards, rep);
                        contracts.add(new Contract(name, objective, rewards, rep, total));
                    }
                }
            }
        }
        return contracts;
    }

    public String tipsVIPContracts(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        return incomeContracts(contract, profile, message, ContractActionType.tips, flexStats);
    }

    public String workVIPContracts(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        return incomeContracts(contract, profile, message, ContractActionType.work, flexStats);
    }

    public String overtimeVIPContracts(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        return incomeContracts(contract, profile, message, ContractActionType.overtime, flexStats);
    }
    public String restructuringContract(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        return incomeContracts(contract, profile, message, ContractActionType.all, flexStats);
    }

    public String doubleTimeContracts(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        return incomeContracts(contract, profile, message, ContractActionType.overtimeWork, flexStats);//"work, tips and overtime");
    }

    public String incomeContracts(Contract contract, Profile profile, Message message, ContractActionType type, List<FlexStats> flexStats) {

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
        switch (type) {
            case work:
                work = true;
                break;
            case overtime:
                overtime = true;
                break;
            case tips:
                tips = true;
                break;
            case overtimeWork:
                overtime = true;
                work = true;
                break;
            case all:
                tips = true;
                work = true;
                overtime = true;
        }
        if (profile.getOvertimeIncome() < 1 && profile.getWorkIncome() < 1 && profile.getTipsIncome() < 1) {
            return "";
        }

        ContractEstimate sleep = minutesToCompleteEarningsGoal(profile, contract, reminderTimes, work, overtime, tips, false);

        int flex = 0;
        int length = 0;


        ReminderTimes flexReminderTimes = yourGrindReminderEstimate(flexStats, reminderTimes);
        ContractEstimate flexSleep = minutesToCompleteEarningsGoal(profile, contract, flexReminderTimes, work, overtime, tips, false);
        flex = flexSleep.getMin();
//        switch (type) {
//            case work:
//                flex = yourGrind(flexStats, contract, type, sleep.getWork());
//                break;
//            case overtime:
//                flex = yourGrind(flexStats, contract, type, sleep.getOvertime());
//                break;
//            case tips:
//                flex = yourGrind(flexStats, contract, type, sleep.getTips());
//                break;
//            case all:
//                // work out how many work/ot/tips they normlly do in a week
//                // then work out how long between work/ot/tips
//                // pass that in as the reminder times into minutesToCompleteEarningsGoal
////                flex = yourGrind(flexStats, contract, ContractActionType.work, sleep.getWork());
////
////                length = yourGrind(flexStats, contract, ContractActionType.tips, sleep.getTips());
////                if (length > flex)
////                    flex = length;
////                length = yourGrind(flexStats, contract, ContractActionType.overtime, sleep.getOvertime());
////                if (length > flex)
////                    flex = length;
////                break;
//            case overtimeWork:
//                ReminderTimes flexReminderTimes = yourGrindReminderEstimate(flexStats, reminderTimes);
//                ContractEstimate sleep2 = minutesToCompleteEarningsGoal(profile, contract, flexReminderTimes, work, overtime, tips, false);
//                flex = sleep2.getMin();
////                flex = yourGrind(flexStats, contract, ContractActionType.work, sleep.getWork());
////
////                length = yourGrind(flexStats, contract, ContractActionType.overtime, sleep.getOvertime());
////                if (length > flex)
////                    flex = length;
//        }

        String extra = "";
        switch (contract.getActionType()) {
            case tips:
                extra = " (" + sleep.getTips() + " tips)";
                break;
            case work:
                extra = " (" + sleep.getWork() + " work)";
                break;
            case overtime:
                extra = " (" + sleep.getOvertime() + " overtime)";
                break;
            case all:
                extra = " (" + sleep.getOvertime() + " overtime, " + sleep.getWork() + " work, "+ sleep.getTips() + " tips)";
                break;
            case overtimeWork:
                extra = " (" + sleep.getOvertime() + " overtime, " + sleep.getWork() + " work)";
                break;
        }

        String timeStr = formatMin(sleep.getMin());
        String without = " **without the buffs**, ";
        if (contract.getWorkBuff() > 1 || contract.getOvertimeBuff() > 1 || contract.getTipsBuff() > 1) {
            without = " with Buffs, ";
        }

        StringBuilder builder = new StringBuilder();

        builder.append("To earn `$" + String.format("%,d", value) + "` from " + type.getName() +extra+without+"\n");
        if (flex > 0) {
            String flexTime = formatMin(flex);
            double repPerHourFlex = (contract.getRep() / (flex / 60.0));;
            String repFlex = "";
            if (contract.getProgress() == 0 ){
                repFlex = " (" + String.format("%.1f", repPerHourFlex) + " rep/hr)";
            }
            builder.append(" - Grind estimate from your last week history **" + flexTime + "**"+repFlex+"\n");
        }

        double repPerHourDefault = sleep.getMin() > 0 ? (contract.getRep() / (sleep.getMin() / 60.0)) : 0;

        String repDefault = "";
        if (contract.getProgress() == 0 ){
            repDefault = " (" + String.format("%.1f", repPerHourDefault) + " rep/hr)";
        }
        builder.append(" - Grind without missing cooldowns **" + timeStr + "**" + repDefault);
        return builder.toString();

    }

    public String tipsContracts(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        int sleep = 0;
        sleep = profile.getStatus().getTips();
        return grindXContract(contract, profile, message, sleep, ContractActionType.tips, false, flexStats);
    }

    public String workContracts(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        int sleep = 0;
        sleep = profile.getStatus().getWork();
        return grindXContract(contract, profile, message, sleep, ContractActionType.work, false, flexStats);
    }

    public String overtimeContracts(Contract contract, Profile profile, Message message, List<FlexStats> flexStats) {
        int sleep = 0;
        sleep = profile.getStatus().getOt();
        return grindXContract(contract, profile, message, sleep, ContractActionType.overtime, false, flexStats);
    }

    public ContractEstimate minutesToCompleteEarningsGoal(Profile profile, Contract contract, ReminderTimes reminderTimes, boolean includeWork, boolean includeOvertime, boolean includeTips, boolean inProgress) {
        logger.info("minutesToCompleteEarningsGoal - contract: {}, reminderTimes: {}, includeWork: {}, includeOvertime: {}, includeTips: {}",
                contract, reminderTimes, includeWork, includeOvertime, includeTips);

        List<Reminder> reminders = ReminderUtils.loadReminder(profile.getName());
        ContractEstimate contractEstimate = new ContractEstimate();

        int max = 60 * 24 * 7 * 2 * 60; // in seconds
        double sleepWork = reminderTimes.getWork() * contract.getWorkCoolDown();
        double sleepOvertime = reminderTimes.getOvertime() * contract.getOvertimeCoolDown();
        double sleepTips = reminderTimes.getTips() * contract.getTipsCoolDown();

        if (!inProgress) {
            // income per minute for each activity
            double workIncomePerMin = includeWork ? (profile.getWorkIncome() * contract.getWorkBuff()) / sleepWork : 0;
            double overtimeIncomePerMin = includeOvertime ? (profile.getOvertimeIncome() * contract.getOvertimeBuff()) / sleepOvertime : 0;
            double tipsIncomePerMin = includeTips ? (profile.getTipsIncome() * contract.getTipsBuff()) / sleepTips : 0;

            double totalIncomePerMin = workIncomePerMin + overtimeIncomePerMin + tipsIncomePerMin;

            if (totalIncomePerMin <= 0) {
                contractEstimate.setMin(max / 60);
                return contractEstimate;
            }

            int minutes = (int) Math.round(contract.getTotal() / totalIncomePerMin);

            // estimate the counts
            contractEstimate.setWork((int) Math.round(minutes / sleepWork));
            contractEstimate.setOvertime((int) Math.round(minutes / sleepOvertime));
            contractEstimate.setTips((int) Math.round(minutes / sleepTips));
            contractEstimate.setMin(minutes);
            return contractEstimate;
        }

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

        if (!includeWork && !includeOvertime && !includeTips) {
            contractEstimate.setMin(max / 60);
            return contractEstimate;
        }

        while (totalEarned < contract.getTotal() - contract.getProgress() && totalSeconds < max) {
            totalSeconds++;
            if (workCooldown > 0) workCooldown--;
            if (overtimeCooldown > 0) overtimeCooldown--;
            if (tipsCooldown > 0) tipsCooldown--;

            if (workCooldown <= 0 && includeWork) {
                totalEarned += (long) (profile.getWorkIncome() * contract.getWorkBuff());
                workCooldown = sleepWorkSec;
                contractEstimate.addWork();
            }

            if (overtimeCooldown <= 0 && includeOvertime) {
                totalEarned += (long) (profile.getOvertimeIncome() * contract.getOvertimeBuff());
                overtimeCooldown = sleepOvertimeSec;
                contractEstimate.addOvertime();
            }

            if (tipsCooldown <= 0 && includeTips) {
                totalEarned += (long) (profile.getTipsIncome() * contract.getTipsBuff());
                tipsCooldown = sleepTipsSec;
                contractEstimate.addTips();
            }
        }

        contractEstimate.setMin((int) Math.round(totalSeconds / 60.0));
        return contractEstimate;
    }

    public String grindXContract(Contract contract, Profile profile, Message message, int sleep, ContractActionType type, boolean remianing, List<FlexStats> flexStats){

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

        if (!isPatreonServer.get() && type != ContractActionType.overtime) {
            sleep = sleep + 1;
        }
        sleep = sleep * value / contract.participantCount();
        String timeStr = formatMin(sleep);
        int flex = yourGrind(flexStats, contract, type, value);
        StringBuilder builder = new StringBuilder();
        builder.append("To complete `" + value + "` " + type + "\n");
        double repPerHourDefault = sleep > 0 ? (contract.getRep() / (sleep / 60.0)) : 0;
        if (flex > 0) {
            String timeStr2 = formatMin(flex);
            double repPerHourFlex = (contract.getRep() / (flex / 60.0));
            String repFlex = "";
            if (contract.getProgress() == 0 ){
                repFlex = " (" + String.format("%.1f", repPerHourFlex) + " rep/hr)";
            }
            builder.append(" - Grind estimate from your last week history **" + timeStr2 + "**"+repFlex+"\n");
        }

        String repDefault = "";
        if (contract.getProgress() == 0 ){
            repDefault = " (" + String.format("%.1f", repPerHourDefault) + " rep/hr)";
        }
        String people = "";
        if (contract.participantCount() > 1 ){
            people = " for "+ contract.participantCount() + " people";
        }
        builder.append(" - Grind without missing cooldowns"+people+" **" + timeStr + "**" + repDefault);
        return builder.toString();
    }

    private String formatMin(int min) {
        String timeStr = "";
        if (min >= 1440) {
            int days = min / 1440;
            int hours = (min % 1440) / 60;
            int mins = min % 60;
            timeStr = days + "d " + hours + "h " + mins + "m";
        } else if (min >= 60) {
            int hours = min / 60;
            int mins = min % 60;
            timeStr = hours + "h " + mins + "m";
        } else {
            timeStr = min + "m";
        }
        return timeStr;
    }

    int yourGrind(List<FlexStats> flexStats, Contract contract, ContractActionType type, int value){
        if (!flexStats.isEmpty()) {
            int flexValue = 0;
            switch (type) {
                case work:
                    flexValue = (int) ((flexStats.get(flexStats.size() - 1).getWork() - flexStats.get(0).getWork()) / contract.getWorkCoolDown() / 7);
                    break;
                case tips:
                    flexValue = (int) ((flexStats.get(flexStats.size() - 1).getTips() - flexStats.get(0).getTips()) / contract.getTipsCoolDown() / 7);
                    break;
                case overtime:
                    flexValue = (int) ((flexStats.get(flexStats.size() - 1).getOvertime() - flexStats.get(0).getOvertime()) / contract.getOvertimeCoolDown() / 7);
                    break;
            }

            if (flexValue > 0) {
                double daysToComplete = (double) value / flexValue;
                int minutesToComplete = (int) Math.round(daysToComplete * 24 * 60);
                return minutesToComplete;
            }
        }
        return -1;
    }


    ReminderTimes yourGrindReminderEstimate(List<FlexStats> flexStats, ReminderTimes reminderTimes) {
        if (!flexStats.isEmpty()) {
            int workCount = (int) ((flexStats.get(flexStats.size() - 1).getWork() - flexStats.get(0).getWork()));
            int tipsCount = (int) ((flexStats.get(flexStats.size() - 1).getTips() - flexStats.get(0).getTips()));
            int otCount   = (int) ((flexStats.get(flexStats.size() - 1).getOvertime() - flexStats.get(0).getOvertime()));

            if (workCount > 0) reminderTimes.setWork((int) Math.round(10080.0 / workCount));
            if (tipsCount > 0) reminderTimes.setTips((int) Math.round(10080.0 / tipsCount ));
            if (otCount > 0)   reminderTimes.setOvertime((int) Math.round(10080.0 / otCount));
        }
        return reminderTimes;
    }


    public boolean contractTypeCheck(Message message, ContractType type) {
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
                    if (item.content().get().contains(type.getName())) return true;
                }
                // check inside type 9 sections
                if (item.type() == 9 && item.components().isPresent()) {
                    for (ComponentData inner : item.components().get()) {
                        if (inner.type() == 10 && inner.content().isPresent()) {
//                            logger.info("contractTypeCheck - checking inner: '{}'", inner.content().get().substring(0, Math.min(50, inner.content().get().length())));
                            if (inner.content().get().contains(type.getName())) {
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

    public void parseParticipantsAndRewards(Message message, Contract contract) {
        Pattern participantPattern = Pattern.compile("<@(\\d+)>.*?`(\\d+)`.*?\\(`(\\d+)%`\\)");
        Pattern rewardItemPattern = Pattern.compile("[🎫🎟️💰⏰🎖️]\\s*[^,\n]+");
        Pattern repPattern = Pattern.compile("\\+(\\d+)\\s+Rep(?:utation)?");

        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) return;

        List<String[]> participants = new ArrayList<>(); // [userId, count, percent]
        List<String> rewards = new ArrayList<>();

        for (ComponentData top : possible.get()) {
            if (top.components().isAbsent()) continue;
            for (ComponentData item : top.components().get()) {
                if (item.type() != 10 || item.content().isAbsent()) continue;
                String text = item.content().get();

                // parse participants
                if (text.contains("Participants")) {
                    for (String line : text.split("\n")) {
                        Matcher m = participantPattern.matcher(line);
                        if (m.find()) {
                            participants.add(new String[]{m.group(1), m.group(2), m.group(3)});
                            logger.info("participant: userId={}, count={}, percent={}%", m.group(1), m.group(2), m.group(3));
                        }
                    }
                }

                // parse rewards
                if (text.contains("Rewards:")) {
                    for (String line : text.split("\n")) {
                        if (line.contains("Rewards:")) {
                            String rewardsPart = line.replaceAll(".*\\*\\*Rewards:\\*\\*\\s*", "").trim();
                            for (String reward : rewardsPart.split(",\\s*")) {
                                rewards.add(reward.trim());
                                logger.info("reward: {}", reward.trim());
                            }
                        }
                        if (line.contains("Reputation:")) {
                            Matcher repMatcher = repPattern.matcher(line);
                            if (repMatcher.find()) {
                                contract.setRep(Integer.parseInt(repMatcher.group(1)));
                                logger.info("rep: {}", contract.getRep());
                            }
                        }
                    }
                }
            }
        }

        contract.setParticipants(participants);
        contract.setRewardList(rewards);
    }

    public String getParticipantRewards(Contract contract) {
        List<String[]> participants = contract.getParticipants();
        List<String> rewards = contract.getRewardList();

        if (participants.isEmpty() || rewards.isEmpty() || participants.size() == 1) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        // multiple participants - split by percentage
        for (String[] participant : participants) {
            String userId = participant[0];
            int percent = Integer.parseInt(participant[2]);

            builder.append("<@").append(userId).append("> (").append(percent).append("%):\n");

            for (String reward : rewards) {
                // try to extract quantity and item name
                Matcher m = Pattern.compile("([^\\d]*)(\\d+)(.*)").matcher(reward.trim());
                if (m.find()) {
                    String prefix = m.group(1).trim();
                    int quantity = Integer.parseInt(m.group(2));
                    String suffix = m.group(3).trim();
                    int split = (int) Math.round(quantity * (percent / 100.0));
//                    int split = (int) Math.floor(quantity * (percent / 100.0));
                    builder.append(" - ").append(prefix).append(" ").append(split).append(" ").append(suffix).append("\n");
                } else {
                    builder.append(" - ").append(reward).append("\n");
                }
            }

//            int repSplit = (int) Math.floor(contract.getRep() * (percent / 100.0));
            int repSplit = (int) Math.round(contract.getRep() * (percent / 100.0));
            builder.append(" - ⭐ +").append(repSplit).append(" Reputation\n");
            builder.append("\n");
        }

        return builder.toString();
    }
}

