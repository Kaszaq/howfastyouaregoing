package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import java.time.LocalDate;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import org.junit.Test;

public class ThroughputPerTypeTest {

    @Test
    public void sampleThroughputPerTypeTest() {
        String finalStatus = "Closed";

        Map<String, Map<LocalDate, Integer>> result = AgileClientProvider.createClient().getAgileProject("AWW").getAllIssues().stream()
                .filter(i -> finalStatus.equals(i.getStatus()))
                .filter(i -> i.getIssueStatusTransitions().stream().anyMatch(st -> finalStatus.equals(st.getToStatus())))
                .collect(
                        groupingBy(i -> i.getType(),
                                Collectors.toMap(i -> i.getIssueStatusTransitions().stream().filter(st -> finalStatus.equals(st.getToStatus())).reduce((a, b) -> b)
                                .map(it -> it.getDate().toLocalDate().with(firstDayOfNextMonth())).orElse(null),
                                        i -> 1,
                                        Integer::sum,
                                        TreeMap::new
                                )
                        )
                );
        assertThat(result.get("Internal Task")).containsOnly(
                entry(LocalDate.of(2018, 7, 1), 1),
                entry(LocalDate.of(2018, 8, 1), 2),
                entry(LocalDate.of(2018, 9, 1), 3),
                entry(LocalDate.of(2018, 10, 1), 1),
                entry(LocalDate.of(2018, 11, 1), 3),
                entry(LocalDate.of(2018, 12, 1), 1)
        );
        assertThat(result.get("Improvement")).containsOnly(
                entry(LocalDate.of(2018, 9, 1), 2),
                entry(LocalDate.of(2018, 10, 1), 1)
        );
        assertThat(result.get("Epic")).containsOnly(
                entry(LocalDate.of(2018, 9, 1), 1),
                entry(LocalDate.of(2018, 11, 1), 1)
        );
        assertThat(result.get("Bug")).containsOnly(
                entry(LocalDate.of(2018, 8, 1), 3),
                entry(LocalDate.of(2018, 11, 1), 3),
                entry(LocalDate.of(2018, 12, 1), 1)
        );
        assertThat(result.get("New Feature")).containsOnly(
                entry(LocalDate.of(2018, 8, 1), 2),
                entry(LocalDate.of(2018, 9, 1), 3),
                entry(LocalDate.of(2018, 10, 1), 1),
                entry(LocalDate.of(2018, 12, 1), 1)
        );

    }

}
