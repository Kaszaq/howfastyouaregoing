package pl.kaszaq.howfastyouaregoing.cycletime;

import java.time.LocalDate;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import pl.kaszaq.howfastyouaregoing.agile.AgileProject;
import pl.kaszaq.howfastyouaregoing.agile.Issue;
import static pl.kaszaq.howfastyouaregoing.agile.IssuePredicates.inStatus;
import pl.kaszaq.howfastyouaregoing.agile.IssueStatusTransition;

/**
 *
 * @author michal.kasza
 */
@UtilityClass
public class CycleTimeComputer {
// TODO: add tests

    public SortedMap<LocalDate, Double> calulcateCycleTime(
            AgileProject agileProject, 
            Predicate<Issue> filters, 
            Set<String> finalStatuses, 
            String... statusNames) {
        SortedMap<LocalDate, Double> cycleTimeStatistics = new TreeMap<>(agileProject.getAllIssues()
                .stream()
                .filter(filters)
                .filter(inStatus(finalStatuses))
                .filter(i -> i.getIssueStatusTransitions()
                .stream()
                .anyMatch(st -> finalStatuses.contains(st.getToStatus()))
                )
                .collect(Collectors.groupingBy(i -> getClosedDate(i, finalStatuses),
                        Collectors.averagingDouble(i -> ((double) i.getDurationInStatuses(statusNames).getSeconds()) / 3600.0)
                )));
        return cycleTimeStatistics;
    }

    private LocalDate getClosedDate(Issue i, Set<String> finalStatuses) {
        LocalDate returnDate = null;
        boolean previousWasClosed = false;
        for (IssueStatusTransition issueStatusTransition : i.getIssueStatusTransitions()) {
            if (!previousWasClosed && (finalStatuses.contains(issueStatusTransition.getToStatus()))) {
                returnDate = issueStatusTransition.getDate().toLocalDate();
                previousWasClosed = true;
            } else if (!finalStatuses.contains(issueStatusTransition.getToStatus())) {
                previousWasClosed = false;
            }
        }
        return returnDate;
    }

}
