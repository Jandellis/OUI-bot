package action.export.model;

import java.util.List;

public class WeeklyBestData {

    public WeeklyBestData(long id, long value) {
        this.id = id;
        this.value = value;
    }

    long id;
    long value;

    public long getId() {
        return id;
    }

    public long getValue() {
        return value;
    }

    public static void sort (List<WeeklyBestData> list)
    {
        list.sort((o1, o2) -> {
            if (o1.getValue() == o2.getValue())
                return 0;
            if (o1.getValue() < o2.getValue())
                return 1;
            else
                return -1;
        });
    }
}
