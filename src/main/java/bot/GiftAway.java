package bot;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GiftAway {

    protected static final Logger logger = LogManager.getLogger("ouiBot");
    static HashMap<String, Person> franchiseMembers = new HashMap<>();
    static int voteTotal = 7;
    static int workTotal = 50;
    static int otTotal = 30;

    public static List<String> main(String work, String ot, String vote, int shifts, int overtimes, int votes, String guildId, String giveawayRole, DiscordClient client) throws Exception {
        List<String> result = new ArrayList<>();

        StringBuilder output = new StringBuilder("");

        franchiseMembers = new HashMap<>();


        work(work);
        ot(ot);
        vote(vote);


        int finalVotes = votes;
        int finalShifts = shifts;
        int finalOvertimes = overtimes;
        franchiseMembers.forEach((s, person) -> {
            if (person.getVote() >= finalVotes)
                person.addMatch();
            if (person.getWork() >= finalShifts)
                person.addMatch();
            if (person.getOt() >= finalOvertimes)
                person.addMatch();
        });

        output.append("\r\n**==========================**");
        output.append("\r\nWork = " + workTotal);
        output.append("\r\nVote = " + voteTotal);
        output.append("\r\nOt   = " + otTotal);
        output.append("\r\n**==========================**");

        output.append("\r\n**==========================**");
        output.append("\r\n          Match All");
        output.append("\r\nName, work, overtime, vote");
        output.append("\r\n**==========================**");
        result.add(output.toString());

        franchiseMembers.forEach((s, person) -> {

            if (person.getMatchCount() == 3) {
                result.add("\r\n" + person.getName() + ", " + person.getWork() + ", " + person.getOt() + ", " + person.getVote());
                try {
                    client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                            Snowflake.of(person.getNameAsId()),
                            Snowflake.of(giveawayRole),
                            "givaway role").block();
                    logger.info("Given role to " + person.getName());
                } catch (Exception e) {
                    logger.info("Not in server " + person.getName());

                }
            }
        });
        StringBuilder match2 = new StringBuilder();

        match2.append("\r\n");
        match2.append("\r\n**==========================**");
        match2.append("\r\n          Match 2");
        match2.append("\r\nName, work, overtime, vote");
        match2.append("\r\n**==========================**");
        result.add(match2.toString());

        franchiseMembers.forEach((s, person) -> {

            if (person.getMatchCount() == 2) {
                result.add("\r\n" + person.getName() + ", " + person.getWork() + ", " + person.getOt() + ", " + person.getVote());
                try {
                    client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                            Snowflake.of(person.getNameAsId()),
                            Snowflake.of(giveawayRole),
                            "givaway role").block();
                    logger.info("Given role to " + person.getName());
                } catch (Exception e) {
                    logger.info("Not in server " + person.getName());
                }
            }
        });

        StringBuilder match1 = new StringBuilder();
        match1.append("\r\n");
        match1.append("\r\n**==========================**");
        match1.append("\r\n          Match 1");
        match1.append("\r\nName, work, overtime, vote");
        match1.append("\r\n**==========================**");
        result.add(match1.toString());

        franchiseMembers.forEach((s, person) -> {

            if (person.getMatchCount() == 1) {
                result.add("\r\n" + person.getName() + ", " + person.getWork() + ", " + person.getOt() + ", " + person.getVote());
                try {
                    client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                            Snowflake.of(person.getNameAsId()),
                            Snowflake.of(giveawayRole),
                            "givaway role").block();
                    logger.info("Removed role from " + person.getName());
                } catch (Exception e) {
                    logger.info("Not in server " + person.getName());

                }
            }
        });


        StringBuilder match0 = new StringBuilder();
        match0.append("\r\n");
        match0.append("\r\n**==========================**");
        match0.append("\r\n          Match 0");
        match0.append("\r\nName, work, overtime, vote");
        match0.append("\r\n**==========================**");
        result.add(match0.toString());

        franchiseMembers.forEach((s, person) -> {

            if (person.getMatchCount() == 0) {
                result.add("\r\n" + person.getName() + ", " + person.getWork() + ", " + person.getOt() + ", " + person.getVote());
                try {
                    client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                            Snowflake.of(person.getNameAsId()),
                            Snowflake.of(giveawayRole),
                            "givaway role").block();
                    logger.info("Removed role from " + person.getName());
                } catch (Exception e) {
                    logger.info("Not in server " + person.getName());

                }
            }
        });

        return result;

    }

    public static void reset() throws IOException {
        BufferedWriter bwKick = new BufferedWriter(new FileWriter("ot.csv"));
        bwKick.close();

        BufferedWriter ot = new BufferedWriter(new FileWriter("vote.csv"));
        ot.close();

        BufferedWriter work = new BufferedWriter(new FileWriter("work.csv"));
        work.close();
    }

    public static void addData(String data,String file) throws IOException {


        BufferedWriter workFile = new BufferedWriter(new FileWriter(file, true));
        workFile.newLine();
        workFile.write(data.replace("*", ""));
        workFile.close();
    }




    private static void work(String url) throws Exception {

//        URLConnection hc = new URL(url).openConnection();
//        hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
//
//
//        try (BufferedInputStream in = new BufferedInputStream(hc.getInputStream());
//             FileOutputStream fileOutputStream = new FileOutputStream("work.csv")) {
//            byte dataBuffer[] = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
//                fileOutputStream.write(dataBuffer, 0, bytesRead);
//            }
//        } catch (IOException e) {
//            // handle exception
//        }


        BufferedReader br = new BufferedReader(new FileReader("work.csv"));

        String line;
        while ((line = br.readLine()) != null) {
            if (!line.equals("")) {
                String[] splitline = line.split(". ", 2)[1].split(" - ");
                Person user;
                String userName = getUser(line);
                if (franchiseMembers.containsKey(userName))
                    user = franchiseMembers.get(userName);
                else
                    user = new Person(userName);
                int value = Integer.parseInt(splitline[splitline.length - 1]);
                workTotal += value;
                user.setWork(value);
                franchiseMembers.put(user.getName(), user);
            }
        }
    }

    static String getUser(String line) {
        String splitter = " - ";
        String[] splitline = line.split(". ", 2)[1].split(splitter);
        String user = splitline[0];
        for (int i = 1; i < splitline.length -1; i++) {
            user += splitter + splitline[i];
        }
        return user;
    }

    private static void ot(String url) throws Exception {


//        URLConnection hc = new URL(url).openConnection();
//        hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
//
//
//        try (BufferedInputStream in = new BufferedInputStream(hc.getInputStream());
//             FileOutputStream fileOutputStream = new FileOutputStream("ot.csv")) {
//            byte dataBuffer[] = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
//                fileOutputStream.write(dataBuffer, 0, bytesRead);
//            }
//        } catch (IOException e) {
//            // handle exception
//        }
        BufferedReader br = new BufferedReader(new FileReader("ot.csv"));

        String line;
        while ((line = br.readLine()) != null) {

            if (!line.equals("")) {
                String[] splitline = line.split(". ", 2)[1].split(" - ");
                Person user;
                String userName = getUser(line);
                if (franchiseMembers.containsKey(userName))
                    user = franchiseMembers.get(userName);
                else
                    user = new Person(userName);
                int value = Integer.parseInt(splitline[splitline.length - 1].split(" ")[0]);
                otTotal += value;
                user.setOt(value);
                franchiseMembers.put(user.getName(), user);
            }
        }
    }

    private static void vote(String url) throws Exception {


//        URLConnection hc = new URL(url).openConnection();
//        hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
//
//
//        try (BufferedInputStream in = new BufferedInputStream(hc.getInputStream());
//             FileOutputStream fileOutputStream = new FileOutputStream("vote.csv")) {
//            byte dataBuffer[] = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
//                fileOutputStream.write(dataBuffer, 0, bytesRead);
//            }
//        } catch (IOException e) {
//            // handle exception
//        }

        BufferedReader br = new BufferedReader(new FileReader("vote.csv"));

        String line;
        while ((line = br.readLine()) != null) {
            if (!line.equals("")) {
                String[] splitline = line.split(". ", 2)[1].split(" - ");

                Person user;
                String userName = getUser(line);
                if (franchiseMembers.containsKey(userName))
                    user = franchiseMembers.get(userName);
                else
                    user = new Person(userName);
                int value = Integer.parseInt(splitline[splitline.length - 1].split(" ")[0]);
                voteTotal += value;
                user.setVote(value);
                franchiseMembers.put(user.getName(), user);
            }
        }
    }

}


class Person {
    private int work = -1;
    private int ot = -1;
    private int vote = -1;

    private int matchCount = 0;

    private String name;

    public Person(String name) {
        this.name = name;
    }

    public int getWork() {
        return work;
    }

    public void setWork(int work) {
        this.work = work;
    }

    public int getOt() {
        return ot;
    }

    public void setOt(int ot) {
        this.ot = ot;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public String getName() {
        return name;
    }

    public String getNameAsId() {
        return name.replace("<@", "").replace(">", "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    public void addMatch() {
        matchCount++;
    }
}
