package pl.kaszaq.howfastyouaregoing.cfd;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import pl.kaszaq.howfastyouaregoing.agile.IssueStatusTransition;
import static pl.kaszaq.howfastyouaregoing.utils.DateUtils.printSimpleDate;

public class CfdData {

    SortedMap<LocalDate, DailyStatusChanges> cfdItems = new TreeMap<>();

    void addTransition(IssueStatusTransition transition) {

        String fromStatus = transition.getFromStatus();
        String toStatus = transition.getToStatus();

        if (toStatus.equals(fromStatus)) {
            return;
        }
        LocalDate eventDate = LocalDate.from(transition.getDate());
        DailyStatusChanges item = cfdItems.computeIfAbsent(eventDate, date -> new DailyStatusChanges());
        item.transitionedToStatus(toStatus);
        item.transitionedFromStatus(fromStatus);
    }
    
    public SortedMap<LocalDate, DailyStatusChanges> getDailyTransitions(){
        return Collections.unmodifiableSortedMap(cfdItems);
    }

    public static class DailyStatusChanges {

        private final Map<String, Integer> statusChanges = new HashMap<>();

        public Map<String, Integer> getStatusChanges() {
            return statusChanges;
        }

        private void transitionedToStatus(String toStatus) {
            statusChanges.merge(toStatus, 1, Integer::sum);
        }

        private void transitionedFromStatus(String fromStatus) {
            statusChanges.merge(fromStatus, -1, Integer::sum);
        }

        public int getValueChangeForStatus(String status) {
            return statusChanges.getOrDefault(status, 0);
        }
    }
}
