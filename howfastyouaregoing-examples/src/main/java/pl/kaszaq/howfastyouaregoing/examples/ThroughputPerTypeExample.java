package pl.kaszaq.howfastyouaregoing.examples;

import static java.time.DayOfWeek.MONDAY;
import java.time.LocalDate;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;

public class ThroughputPerTypeExample {

    private static AgileClient agileClient;

    public static void main(String[] args) {
        agileClient = AgileClientProvider.createClient();

        runExample();
    }

    private static void runExample() {
        String finalStatus = "Closed";

        Map<String, Map<LocalDate, Integer>> result = agileClient.getAgileProject("MYPROJECTID").getAllIssues().stream()
                .filter(i -> finalStatus.equals(i.getStatus()))
                .filter(i -> i.getIssueStatusTransitions().stream().anyMatch(st -> finalStatus.equals(st.getToStatus())))
                .collect(
                        groupingBy(i -> i.getType(),
                                Collectors.toMap(i -> i.getIssueStatusTransitions().stream().filter(st -> finalStatus.equals(st.getToStatus())).reduce((a, b) -> b).map(it -> it.getDate().toLocalDate().with(previousOrSame(MONDAY))).orElse(null),
                                        i -> 1,
                                        Integer::sum,
                                        TreeMap::new
                                )
                        )
                );
        System.out.println(result);

    }

}
