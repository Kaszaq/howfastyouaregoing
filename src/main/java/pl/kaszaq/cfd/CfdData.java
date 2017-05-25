package pl.kaszaq.cfd;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import pl.kaszaq.agile.IssueStatusTransition;
import static pl.kaszaq.utils.DateUtils.printSimpleDate;

public class CfdData {

    SortedMap<LocalDate, DailyStatusChanges> cfdItems = new TreeMap<>();

    private class DailyStatusChanges {

        Map<String, Integer> itemsInStatuses = new HashMap<>();

        public Map<String, Integer> getItemsInStatuses() {
            return itemsInStatuses;
        }

        private void transitionedToStatus(String toStatus) {
            if (toStatus == null) {
                return;
            }
            int toStatusCount = itemsInStatuses.getOrDefault(toStatus, 0) + 1;
            itemsInStatuses.put(toStatus, toStatusCount);
        }

        private void transitionedFromStatus(String fromStatus) {
            if (fromStatus == null) {
                return;
            }
            int fromStatusCount = itemsInStatuses.getOrDefault(fromStatus, 0) - 1;
            itemsInStatuses.put(fromStatus, fromStatusCount);
        }

        private int getValueForStatus(String status) {
            return itemsInStatuses.getOrDefault(status, 0);
        }
    }

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

    public void printoutForCfd(List<String> statusOrder) {
        Map<String, Integer> statusValues = new HashMap<>();
        statusOrder.forEach(status -> statusValues.put(status, 0));
        statusOrder.stream().map(o -> "\t" + o).forEach(System.out::print);
        System.out.println("");
        cfdItems.forEach((k, v) -> {
            System.out.print(printSimpleDate(k) + "\t");
            statusOrder.forEach(status -> {
                Integer newValue = statusValues.merge(status, v.getValueForStatus(status), Integer::sum);
                System.out.print(newValue + "\t");
            });
            System.out.println("");
        });
    }
}
