package pl.kaszaq.cfd;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.kaszaq.agile.IssueStatusTransition;
import static pl.kaszaq.utils.DateUtils.printSimpleDate;

public class CfdData {


    Map<String, CfdItem> cfdItems = new HashMap();
    private ZonedDateTime earliestDate;
    private ZonedDateTime latiestDate;

    private class CfdItem {

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
        ZonedDateTime eventDate = transition.getDate();
        CfdItem item = cfdItems.get(printSimpleDate(eventDate));
        if (item == null) {
            item = new CfdItem();
            cfdItems.put(printSimpleDate(eventDate), item);
        }
        item.transitionedToStatus(toStatus);
        item.transitionedFromStatus(fromStatus);
        markDate(eventDate);
    }

    private void markDate(ZonedDateTime eventDate) {
        if (earliestDate == null || earliestDate.isAfter(eventDate)) {
            earliestDate = eventDate;
        }
        if (latiestDate == null || latiestDate.isBefore(eventDate)) {
            latiestDate = eventDate;
        }
    }

    public void printoutForCfd(List<String> statusOrder) {
        Map<String, Integer> statusValues = new HashMap<>();
        statusOrder.forEach(status -> statusValues.put(status, 0));
        statusOrder.stream().map(o -> "\t" + o).forEach(System.out::print);
        System.out.println("");
        ZonedDateTime dayToPrint = earliestDate;
        while (dayToPrint.isBefore(latiestDate.plusDays(1))) {
            String currentDate = printSimpleDate(dayToPrint);
            CfdItem item = cfdItems.get(currentDate);
            if (item != null) {
                System.out.print(currentDate + "\t");
                statusOrder.forEach(status -> {
                    int newValue = statusValues.get(status) + item.getValueForStatus(status);
                    statusValues.put(status, newValue);
                    System.out.print(newValue + "\t");
                });
                System.out.println("");
            }
            dayToPrint = dayToPrint.plusDays(1);
        }
    }

    public void printoutForSpeedComparison(List<String> statusOrder) {
        Map<String, Integer> statusValues = new HashMap<>();
        statusOrder.forEach(status -> statusValues.put(status, 0));
        statusOrder.stream().map(o -> "\t" + o).forEach(System.out::print);
        System.out.println("");
        ZonedDateTime dayIterator = earliestDate;
        ZonedDateTime monthToPrint = monthOnly(earliestDate);
        boolean lastMonthPrinted = false;
        while (dayIterator.isBefore(latiestDate.plusDays(1))) {
            String currentDate = printSimpleDate(dayIterator);
            CfdItem item = cfdItems.get(currentDate);
            if (item != null) {
                statusOrder.forEach(status -> {
                    statusValues.put(status, statusValues.get(status) + item.getValueForStatus(status));
                });
            }
            dayIterator = dayIterator.plusDays(1);
            lastMonthPrinted = false;
            if (monthOnly(dayIterator).isAfter(monthToPrint)) {
                lastMonthPrinted = true;
                monthToPrint = printMonth(monthToPrint, statusValues, dayIterator, statusOrder);
            }
        }
        if (!lastMonthPrinted) {
            printMonth(monthToPrint, statusValues, dayIterator, statusOrder);
        }
    }

    private ZonedDateTime printMonth(ZonedDateTime monthToPrint, Map<String, Integer> statusValues, ZonedDateTime dayIterator, List<String> statusOrder) {
        System.out.print(printSimpleDate(monthToPrint) + "\t");
        statusOrder.forEach(status -> {
            System.out.print(statusValues.get(status) + "\t");
        });
        System.out.println("");
        statusOrder.forEach(status -> statusValues.put(status, 0));
        monthToPrint = monthOnly(dayIterator);
        return monthToPrint;
    }

    private static ZonedDateTime monthOnly(ZonedDateTime dayToPrint) {
        return dayToPrint.withDayOfMonth(1).withHour(0).withMinute(0).withNano(0).withSecond(0);
    }
}
