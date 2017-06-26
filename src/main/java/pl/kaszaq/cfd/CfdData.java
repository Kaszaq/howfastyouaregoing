package pl.kaszaq.cfd;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import pl.kaszaq.agile.IssueStatusTransition;
import static pl.kaszaq.utils.DateUtils.printSimpleDate;

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

    public void printoutForCfd(List<String> statusOrder) {
        Map<String, Integer> statusValues = new HashMap<>();
        statusOrder.forEach(status -> statusValues.put(status, 0));
        statusOrder.stream().map(o -> "\t" + o).forEach(System.out::print);
        System.out.println("");
        getDailyTransitions().forEach((k, v) -> {
            System.out.print(printSimpleDate(k) + "\t");
            statusOrder.forEach(status -> {
                Integer newValue = statusValues.merge(status, v.getValueChangeForStatus(status), Integer::sum);
                System.out.print(newValue + "\t");
            });
            System.out.println("");
        });
    }

    public static class DailyStatusChanges {

        private final Map<String, Integer> itemsInStatuses = new HashMap<>();

        public Map<String, Integer> getItemsInStatuses() {
            return itemsInStatuses;
        }

        private void transitionedToStatus(String toStatus) {
            itemsInStatuses.merge(toStatus, 1, Integer::sum);
        }

        private void transitionedFromStatus(String fromStatus) {
            itemsInStatuses.merge(fromStatus, -1, Integer::sum);
        }

        public int getValueChangeForStatus(String status) {
            return itemsInStatuses.getOrDefault(status, 0);
        }
    }
}
