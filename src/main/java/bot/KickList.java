package bot;

import java.util.ArrayList;
import java.util.List;

public class KickList {

    List<Long> work;
    List<Long> unhappy;

    public KickList() {
        work = new ArrayList<>();
        unhappy = new ArrayList<>();
    }

    public void addWork(Long id) {
        work.add(id);
    }

    public void addUnhappy(Long id) {
        unhappy.add(id);
    }

    public List<Long> getWork() {
        return work;
    }

    public List<Long> getUnhappy() {
        return unhappy;
    }
}
