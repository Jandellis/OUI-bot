package action.export;

import bot.Member;

import java.sql.Timestamp;

public class ExportData {

    private Member member;
    private Timestamp exportTime;


    public ExportData(Member member, Timestamp exportTime) {
        this.member = member;
        this.exportTime = exportTime;
    }

    public Member getMember() {
        return member;
    }

    public Timestamp getExportTime() {
        return exportTime;
    }
}
