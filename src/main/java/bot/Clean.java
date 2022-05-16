package bot;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Clean {


    public static KickList main(String url, String historyFile, int work, int unclean) throws Exception {


        HashMap<Long, Member> current = parseCurrent(url);
        HashMap<Long, MemberHistory> history = parseHistory(new File(historyFile));

        return writeHistory(new File(historyFile), current, history, work, unclean);

    }

    public static List<KickMember> mainNoImport(String historyFile) throws Exception {

        HashMap<Long, MemberHistory> history = parseHistory(new File(historyFile));
        return readHistory(history);

    }

    public static void kickMember(String id) throws IOException {
        BufferedWriter bwKick = new BufferedWriter(new FileWriter("kicked.txt", true));
        bwKick.write(id);
        bwKick.close();
    }

    public static List<KickMember> kickedMembers() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("kicked.txt")));
        String line;
        List<KickMember>  members = new ArrayList<>();
        while ((line = br.readLine()) != null) {
                KickMember member = new KickMember();
                member.id = Long.parseLong(line);
                members.add(member);
        }
        br.close();
        return members;
    }

    public static void addGift(int value) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(new File("giveaway.txt")));
        String line;
        int total = 0;
        while ((line = br.readLine()) != null) {
            total = Integer.parseInt(line);
        }
        br.close();
        total = total + value;

        BufferedWriter bwKick = new BufferedWriter(new FileWriter("giveaway.txt"));
        bwKick.write(total + "");
        bwKick.close();
    }

    public static int getGift() throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(new File("giveaway.txt")));
        String line;
        int total = 0;
        while ((line = br.readLine()) != null) {
            total = Integer.parseInt(line);
        }
        br.close();

        //clear
        BufferedWriter bwKick = new BufferedWriter(new FileWriter("giveaway.txt"));
        bwKick.write("0");
        bwKick.close();
        return total;
    }

    private static KickList writeHistory(File history, HashMap<Long, Member> current, HashMap<Long, MemberHistory> historic, int noWork, int noHappy) throws IOException {

        //reset kicked list
        BufferedWriter bwKick = new BufferedWriter(new FileWriter("kicked.txt", true));
        bwKick.close();

        KickList kickList = new KickList();
        BufferedWriter bw = new BufferedWriter(new FileWriter(history));
        BufferedWriter bwWarnWork = new BufferedWriter(new FileWriter("warnWork.txt"));
        BufferedWriter bwWarnHappy = new BufferedWriter(new FileWriter("warnHappy.txt"));
        current.forEach((id, member) -> {
            MemberHistory memberHistory = historic.get(id);

                try {
                    if (memberHistory != null && (memberHistory.happy <= .5 || memberHistory.shifts == member.shifts)) {
                        System.out.println("member id " + id + " is unhappy or has 0 shifts");

                        LocalDateTime unhappy;
                        LocalDateTime work;
                        //both unhappy and no shifts
                        if (memberHistory.happy <= .5 && memberHistory.shifts == member.shifts) {
                            System.out.println("member is unhappy and has 0 shifts");
                            unhappy = LocalDateTime.parse(memberHistory.timeUnhappy);
                            work = LocalDateTime.parse(memberHistory.timeNoWork);
                            bw.write(id + "," + member.shifts + "," + member.happy + "," + memberHistory.timeNoWork + "," + memberHistory.timeUnhappy);
                        } else {
                            //happy but no shifts
                            if (memberHistory.shifts == member.shifts) {
                                bw.write(id + "," + member.shifts + "," + member.happy + "," + memberHistory.timeNoWork + "," + LocalDateTime.now().toString());
                                unhappy = LocalDateTime.now();
                                work = LocalDateTime.parse(memberHistory.timeNoWork);
                            } else {
                                //unhappy but shifts
                                bw.write(id + "," + member.shifts + "," + member.happy + "," + LocalDateTime.now().toString() + "," + memberHistory.timeUnhappy);
                                unhappy = LocalDateTime.parse(memberHistory.timeUnhappy);
                                work = LocalDateTime.now();
                            }
                        }

                        if (Duration.between(work, LocalDateTime.now() ).toDays() > noWork) {
                            kickList.addWork(id);
                            bwWarnWork.write("<@" + id + ">");
                            bwWarnWork.newLine();
                        }

                        if (Duration.between(unhappy, LocalDateTime.now()).toDays() > noHappy) {
                            kickList.addUnhappy(id);
                            bwWarnHappy.write("<@" + id + ">");
                            bwWarnHappy.newLine();
                        }

                    } else {
                        bw.write(id + "," + member.shifts + "," + member.happy + "," + LocalDateTime.now().toString() +"," + LocalDateTime.now().toString());
                    }
                    bw.newLine();

                } catch (IOException e) {
                    e.printStackTrace();
                }
        });

        //id, shifts worked, happy, days unhappy/0 shift increase
        bw.close();
        bwWarnHappy.close();
        bwWarnWork.close();
        return kickList;
    }


    private static List<KickMember> readHistory(HashMap<Long, MemberHistory> history) throws IOException {
        List<KickMember> kickList = new ArrayList<>();



        history.forEach((id, member) -> {

                    KickMember kickMember = new KickMember();
                    LocalDateTime unhappy;
                    LocalDateTime work;
                    unhappy = LocalDateTime.parse(member.timeUnhappy);
                    work = LocalDateTime.parse(member.timeNoWork);

                kickMember.daysNoWork = Duration.between(work, LocalDateTime.now() ).toDays();
                kickMember.daysUnhappy = Duration.between(unhappy, LocalDateTime.now() ).toDays();
                kickMember.id = id;

                kickList.add(kickMember);
        });

        return kickList;
    }


    private static HashMap<Long, Member> parseCurrent(String url) throws Exception {

        URLConnection hc = new URL(url).openConnection();
        hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");


        try (BufferedInputStream in = new BufferedInputStream(hc.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream("temp.csv")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception
        }


        BufferedReader br = new BufferedReader(new FileReader(new File("temp.csv")));
//        BufferedReader br = new BufferedReader(new FileReader(new File("test data\\OUI_MemberData_123-4.csv")));

        String line;

        HashMap<Long, Member> members = new HashMap<>();


        while ((line = br.readLine()) != null) {

            if (!line.startsWith("Member ID")) {
                String[] data = line.split(",");
                Member member = new Member(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);

                members.put(Long.parseLong(data[0]), member);
            }
        }
        br.close();
        return members;
    }

    private static HashMap<Long, MemberHistory> parseHistory(File history) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(history));

        String line;

        HashMap<Long, MemberHistory> members = new HashMap<>();


        while ((line = br.readLine()) != null) {

            if (!line.startsWith("Member ID")) {
                String[] data = line.split(",");
                //id, shifts worked, happy, days unhappy/0 shift increase
                MemberHistory member = new MemberHistory(data[0], data[1], data[2], data[3], data[4]);

                members.put(Long.parseLong(data[0]), member);
            }
        }
        br.close();
        return members;
    }


}

class MemberHistory {
    Long id;
    int shifts;
    double happy;
    String timeNoWork;
    String timeUnhappy;

    public MemberHistory(String id, String shifts, String happy, String time, String timeUnhappy) {
        this.id = Long.parseLong(id);
        this.shifts = Integer.parseInt(shifts);
        this.happy = Double.parseDouble(happy);
        this.timeNoWork = time;
        this.timeUnhappy = timeUnhappy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getShifts() {
        return shifts;
    }

    public void setShifts(int shifts) {
        this.shifts = shifts;
    }

    public double getHappy() {
        return happy;
    }

    public void setHappy(double happy) {
        this.happy = happy;
    }

}

class Member {
    Long id;
    String name;
    int income;
    int shifts;
    int weeklyShifts;
    int tips;
    int donations;
    double happy;

    public Member(Long id, String name, int income, int shifts, int weeklyShifts, int tips, int donations, double happy) {
        this.id = id;
        this.name = name;
        this.income = income;
        this.shifts = shifts;
        this.weeklyShifts = weeklyShifts;
        this.tips = tips;
        this.donations = donations;
        this.happy = happy;
    }

    public Member(String id, String name, String income, String shifts, String weeklyShifts, String tips, String donations, String happy) {
        this.id = Long.parseLong(id);
        this.name = name;
        this.income = Integer.parseInt(income);
        this.shifts = Integer.parseInt(shifts);
        this.weeklyShifts = Integer.parseInt(weeklyShifts);
        this.tips = Integer.parseInt(tips);
        //this.donations = Integer.parseInt(donations);
        this.happy = Double.parseDouble(happy);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public int getShifts() {
        return shifts;
    }

    public void setShifts(int shifts) {
        this.shifts = shifts;
    }

    public int getWeeklyShifts() {
        return weeklyShifts;
    }

    public void setWeeklyShifts(int weeklyShifts) {
        this.weeklyShifts = weeklyShifts;
    }

    public int getTips() {
        return tips;
    }

    public void setTips(int tips) {
        this.tips = tips;
    }

    public int getDonations() {
        return donations;
    }

    public void setDonations(int donations) {
        this.donations = donations;
    }

    public double getHappy() {
        return happy;
    }

    public void setHappy(double happy) {
        this.happy = happy;
    }
}

