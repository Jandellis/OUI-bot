package action.reminder;

import action.Action;
import action.reminder.model.Stats;
import action.reminder.model.Team;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Olympics extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;
    Long recruiter;

    public Olympics() {
        param = "cyolylb";
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
        recruiter = Long.parseLong(config.get("recruiter"));
    }


    @Override
    public Mono<Object> doAction(Message message) {
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
                    List<Stats> statsList=ReminderUtils.loadStats();

                    statsList.forEach(stats -> {
                        int ot = stats.getOt() * 5;
                        int work = stats.getWork() * 2;
                        int tips = stats.getTips();
                        stats.setTotal(ot + work + tips);
                    });
                    statsList.sort((o1, o2) -> {
                        if (o1.getTotal() == o2.getTotal())
                            return 0;
                        if (o1.getTotal() < o2.getTotal())
                            return 1;
                        else
                            return -1;
                    });



                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title("Leaderboard");
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (Stats stats: statsList) {
                        count++;
                        sb.append("**" + count + "** - ");
                        sb.append("<@"+stats.getId()+"> **" + stats.getTotal()+"**\n");
                        if (count == 41) {
                            break;
                        }
                    }

                    embed.description(sb.toString());

                    message.getChannel().block().createMessage(embed.build()).block();

                }




                action = getAction(message, "cyolyteamlb");

                if (action != null) {
                    List<Stats> statsList=ReminderUtils.loadTeamStats();

                    statsList.forEach(stats -> {
                        int ot = stats.getOt() * 5;
                        int work = stats.getWork() * 2;
                        int tips = stats.getTips();
                        stats.setTotal(ot + work + tips);
                    });
                    statsList.sort((o1, o2) -> {
                        if (o1.getTotal() == o2.getTotal())
                            return 0;
                        if (o1.getTotal() < o2.getTotal())
                            return 1;
                        else
                            return -1;
                    });



                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title("Leaderboard");
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (Stats stats: statsList) {
                        count++;
                        sb.append("**" + count + "** - ");
                        sb.append(""+stats.getId()+" **" + stats.getTotal()+"**\n");
                        if (count == 41) {
                            break;
                        }
                    }

                    embed.description(sb.toString());

                    message.getChannel().block().createMessage(embed.build()).block();

                }


                action = getAction(message, "cyolyreset");

                if (action != null && hasPermission(message, recruiter)) {
                    ReminderUtils.resetStats();
                    message.getChannel().block().createMessage("reset stats").block();
                }

                action = getAction(message, "cyolycreate");
                if (action != null) {
                    String teamName = message.getContent().split(" ", 2)[1];
                    if (teamName.length() > 50) {
                        message.getChannel().block().createMessage("Team name is too long, max is 50 ").block();
                    } else {
                        String userid = message.getAuthor().get().getId().asString();
                        List<Team> teams = ReminderUtils.loadTeam(userid);
                        AtomicBoolean joined = new AtomicBoolean(false);
                        teams.forEach(team -> {
                            if (team.isJoined()) {
                                joined.set(true);
                            } else {
                                ReminderUtils.deleteTeam(team.getName(), team.getTeam());
                            }
                        });
                        if (joined.get()) {
                            //reject
                            message.getChannel().block().createMessage("You area already in a team").block();
                        } else {
                            Team team = new Team(userid, teamName, true, true);
                            ReminderUtils.createTeam(team);
                            message.getChannel().block().createMessage("Created new team " + teamName).block();
                        }
                        // check not part of a team
                        // if pending invite reject invite
                        // then create team
                    }
                }

                action = getAction(message, "cyolyinvite");
                if (action != null) {
                    // check no pending invites for user
                    // check user is team owner

                    String userInvite = action.replace("<@", "").replace(">", "");
                    List<Team> invitedTeams = ReminderUtils.loadTeam(userInvite);
                    invitedTeams.forEach(teamList -> {
                        if (!teamList.isJoined()) {
                            message.getChannel().block().createMessage("Already got a pending invite a team").block();
                        } else {
                            message.getChannel().block().createMessage("Already in a team").block();
                        }
                    });

                    if (invitedTeams.size() == 0) {
                        String userid = message.getAuthor().get().getId().asString();
                        List<Team> teams = ReminderUtils.loadTeam(userid);
                        AtomicBoolean owner = new AtomicBoolean(false);
                        AtomicReference<Team> team = new AtomicReference(null);
                        teams.forEach(teamList -> {
                            if (teamList.isOwner()) {
                                owner.set(true);
                                team.set(teamList);
                            }
                        });
                        if (owner.get()) {
                            Team teamInvite = new Team(userInvite, team.get().getTeam(), false, false);
                            ReminderUtils.createTeam(teamInvite);
                            message.getChannel().block().createMessage("User invited to your team, type `cyolyjoin` to join or `cyolyreject` ro reject").block();
                        } else {
                            message.getChannel().block().createMessage("You are not an owner and are unable to invite").block();
                        }

                    }



                }

                action = getAction(message, "cyolyjoin");
                if (action != null) {
                    //check pending invites and if so join
                    //otherwise error no pending
                    String userid = message.getAuthor().get().getId().asString();
                    List<Team> teams = ReminderUtils.loadTeam(userid);
                    teams.forEach(teamList -> {
                        if (!teamList.isJoined()) {
                            ReminderUtils.deleteTeam(teamList.getName(), teamList.getTeam());

                            teamList.setJoined(true);
                            ReminderUtils.createTeam(teamList);
                            message.getChannel().block().createMessage("Joined team " + teamList.getTeam()).block();
                        }
                    });
                }

                action = getAction(message, "cyolyreject");
                if (action != null) {
                    //check pending invites and if so reject
                    //otherwise error no pending
                    String userid = message.getAuthor().get().getId().asString();
                    List<Team> teams = ReminderUtils.loadTeam(userid);
                    teams.forEach(teamList -> {
                        if (!teamList.isJoined()) {
                            ReminderUtils.deleteTeam(teamList.getName(), teamList.getTeam());

                            message.getChannel().block().createMessage("Rejected team " + teamList.getTeam()).block();
                        }
                    });
                }


                action = getAction(message, "cyolymembers");
                if (action != null) {
                    //check pending invites and if so reject
                    //otherwise error no pending
                    String userid = message.getAuthor().get().getId().asString();
                    List<Team> teams = ReminderUtils.loadTeam(userid);
                    StringBuilder sb = new StringBuilder();
                    AtomicReference<String> teamName = new AtomicReference<>("");
                    teams.forEach(teamList -> {
                        List<Team> members = ReminderUtils.loadTeamMembers(teamList.getTeam());
                        members.forEach(member -> {
                            teamName.set(member.getTeam());
                            sb.append("<@"+member.getName()+">");
                            if (member.isOwner()) {
                                sb.append(" **Owner**");
                            }
                            sb.append("\n");
                        });
                    });

                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title("Members - " + teamName.get());
                    embed.description(sb.toString());

                    message.getChannel().block().createMessage(embed.build()).block();
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
