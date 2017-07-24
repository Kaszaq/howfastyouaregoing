package pl.kaszaq.cycletime;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import pl.kaszaq.agile.AgileProject;
import pl.kaszaq.agile.IssueData;
import pl.kaszaq.agile.IssuePredicates;
import static pl.kaszaq.agile.IssuePredicates.hasSubtasks;
import pl.kaszaq.agile.IssueStatusTransition;
import pl.kaszaq.agile.IssueStatusTransitionPredicates;
import pl.kaszaq.agile.IssueWrapper;

/**
 *
 * @author michal.kasza
 */
@AllArgsConstructor
public class CycleTimeComputer {
// TODO: add tests

    private final AgileProject agileProject;
    private final String finalStatus;

    public double calulcateCycleTimeOfAllIssues(Predicate<IssueData> filters, LocalDate toDate, LocalDate fromDate,
            String... statuses) {
        LongSummaryStatistics cycleTimeStatistics = agileProject.getAllIssues()
                .stream()
                .filter(filters.and(wasClosed(fromDate, toDate)))
                .mapToLong(i -> new IssueWrapper(i).getDurationInStatuses(statuses).getSeconds())
                .summaryStatistics();
        return cycleTimeStatistics.getAverage() / 3600;
    }

    /**
     * Counts time of stories closed within specified dates since first of its subtasks entered any of statuses till last one left any of statuses.This time will be counted only when all subtasks are in final status.
     * @param filters
     * @param toDate
     * @param fromDate
     * @param statuses
     * @return
     */
    public double calulcateCycleTimeOfStories(Predicate<IssueData> filters, LocalDate toDate, LocalDate fromDate,  String... statuses) {
        List<Duration> durations = new ArrayList<>();
        agileProject.getAllIssues().stream()
                .filter(filters.and(hasSubtasks().and(wasClosed(fromDate, toDate))))
                .forEach(issue -> {

            Set<IssueData> subtasks = getSubtasks(issue);
            if (allSubtasksInFinalStatus(subtasks)) {
                List<IssueStatusTransition> transitions = subtasks.stream()
                        .flatMap(i -> i.getIssueStatusTransitions().stream())
                        .filter(IssueStatusTransitionPredicates.to(statuses).or(IssueStatusTransitionPredicates.from(statuses)))
                        .sorted()
                        .collect(Collectors.toList());
                Duration duration;
                if (!transitions.isEmpty()) {
                    duration = Duration.between(transitions.get(0).getDate(), transitions.get(transitions.size() - 1).getDate());
                } else {
                    duration = Duration.ZERO;
                }
                durations.add(duration);
            }

        });
        LongSummaryStatistics cycleTimeStatistics = durations.stream().mapToLong(d -> d.getSeconds())
                //                .peek(n -> System.out.println(NumberUtils.prettyPrint((double) n / 3600.0)))
                .summaryStatistics();
        return cycleTimeStatistics.getAverage() / 3600;
    }

    private Predicate<IssueData> wasClosed(LocalDate fromDate, LocalDate toDate) {
        return IssuePredicates.hasStatusTransitionsThat(IssueStatusTransitionPredicates.createdAfter(fromDate),
                IssueStatusTransitionPredicates.createdBefore(toDate),
                IssueStatusTransitionPredicates.to(finalStatus)
        );
    }

    private boolean allSubtasksInFinalStatus(Set<IssueData> subtasks) {
        return !subtasks.stream().anyMatch(s -> !s.getStatus().equals(finalStatus));
    }

    private Set<IssueData> getSubtasks(IssueData i) {
        //TODO this could be moved to some helper method...
        return i.getSubtaskKeys().stream().map(id -> agileProject.getIssue(id)).collect(Collectors.toSet());
    }

}
