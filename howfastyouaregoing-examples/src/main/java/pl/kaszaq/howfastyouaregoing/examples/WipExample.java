package pl.kaszaq.howfastyouaregoing.examples;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.IssueStatusTransitionPredicates;
import static pl.kaszaq.howfastyouaregoing.agile.IssueStatusTransitionPredicates.betweenStatuses;
import pl.kaszaq.howfastyouaregoing.utils.DateUtils;

public class WipExample {

    private static AgileClient agileClient;

    public static void main(String[] args) {
        agileClient = AgileClientProvider.createClient();

        runExample();
    }

    private static void runExample() {
        // TODO: rewrite this example due to the below:
        // this is very hard to read and should be rewritten
        // for simple wip (based on what is in progress at the end of the day cfd should be used
        SortedMap<ZonedDateTime, Integer> wip = new TreeMap<>();
        String[] wipStatuses = {"In Progress", "Ready for Testing"};
        AtomicInteger counter = new AtomicInteger();
        agileClient.getAgileProject("MYPROJECTID").getAllIssues().stream()
                .flatMap(i -> i.getIssueStatusTransitions().stream())
                .sorted()
                .filter(betweenStatuses(wipStatuses).negate())
                .forEach(st -> {
                    if (IssueStatusTransitionPredicates.to(wipStatuses).test(st)) {
                        wip.put(st.getDate(), counter.incrementAndGet());
                    } else if (IssueStatusTransitionPredicates.from(wipStatuses).test(st)) {
                        wip.put(st.getDate(), counter.decrementAndGet());
                    }
                });

        LocalDate prevDate = null;
        LocalDateTime prevDateTime = null;
        Duration totalDuration = Duration.ZERO;
        Map<Integer, Duration> wipPerTime = new HashMap<>();
        SortedMap<LocalDate, Double> avgWip = new TreeMap<>();
        for (Map.Entry<ZonedDateTime, Integer> entry : wip.entrySet()) {
            ZonedDateTime k = entry.getKey();
            Integer v = entry.getValue();

            LocalDate eventDate = LocalDate.from(k);
            LocalDateTime eventDateTime = LocalDateTime.from(k);
            if (prevDate == null) {
                prevDate = eventDate.minusDays(1);
            }
            if (!eventDate.isEqual(prevDate)) {
                prevDateTime = LocalDateTime.of(eventDate, LocalTime.of(8, 0));
                if (prevDateTime.isAfter(eventDateTime)) {
                    prevDateTime = eventDateTime;
                }
                if (!totalDuration.isZero()) {
                    Double avgDailyWip = 0.;
                    for (Map.Entry<Integer, Duration> entry1 : wipPerTime.entrySet()) {
                        avgDailyWip += entry1.getKey().doubleValue() * (double) entry1.getValue().getSeconds() / (double) totalDuration.getSeconds();
                    }
                    avgWip.put(prevDate, avgDailyWip);
                }
                prevDate = eventDate;

                totalDuration = Duration.ZERO;
                wipPerTime = new HashMap<>();
            }
            Duration duration = Duration.between(prevDateTime, eventDateTime);
            wipPerTime.merge(v, duration, Duration::plus);
            totalDuration = totalDuration.plus(duration);

        }
        avgWip.forEach((k1, v1) -> {
            System.out.println(DateUtils.printSimpleDate(k1) + "\t" + NumberUtils.prettyPrint(v1));
        });
    }
}
