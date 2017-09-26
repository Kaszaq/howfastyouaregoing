package pl.kaszaq.howfastyouaregoing.agile;

import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import static pl.kaszaq.howfastyouaregoing.utils.CommonPredicates.alwaysTrue;

public class IssuePredicates {

    public static Predicate<Issue> updatedAfter(ZonedDateTime date) {

        return issue -> issue.getUpdated().isAfter(date);
    }

    public static Predicate<Issue> createdAfter(ZonedDateTime date) {

        return issue -> issue.getCreated().isAfter(date);
    }

    public static Predicate<Issue> hasStatusTransitionsThat(Predicate<IssueStatusTransition>... predicates) {
        Optional<Predicate<IssueStatusTransition>> predicate
                = Arrays.stream(predicates).reduce((Predicate<IssueStatusTransition> p1, Predicate<IssueStatusTransition> p2) -> p1.and(p2));

        return issue -> issue.getIssueStatusTransitions().stream().anyMatch(predicate.orElse(alwaysTrue()));
    }

    public static Predicate<Issue> hasSubtasks() {
        return issue -> !issue.getSubtaskKeys().isEmpty();
    }

    public static Predicate<Issue> isSubtask() {
        return issue -> issue.isSubtask();
    }

    public static Predicate<Issue> isEpic() {
        return issue -> issue.getType().equals("Epic");
    }

    public static Predicate<Issue> isType(String... acceptableTypes) {
        return IssuePredicates.isType(ImmutableSet.copyOf(acceptableTypes));
    }

    public static Predicate<Issue> isType(Set<String> acceptableTypes) {
        return issue -> acceptableTypes.contains(issue.getType());
    }

    public static Predicate<Issue> reportedBy(String... reporters) {
        return reportedBy(ImmutableSet.copyOf(reporters));
    }

    public static Predicate<Issue> reportedBy(Set<String> allowedReporters) {
        return issue -> issue.getCreator() != null && allowedReporters.contains(issue.getCreator());
    }

    public static Predicate<Issue> inResolution(String... resolutions) {
        return inResolution(ImmutableSet.copyOf(resolutions));
    }

    public static Predicate<Issue> inResolution(Set<String> allowedResolutions) {
        return issue -> issue.getResolution() != null && allowedResolutions.contains(issue.getResolution());
    }

    public static Predicate<Issue> inStatus(String... statusNames) {
        return inStatus(ImmutableSet.copyOf(statusNames));
    }

    public static Predicate<Issue> inStatus(Set<String> allowedStatuses) {
        return issue
                -> allowedStatuses.contains(issue.getStatus());
    }

    public static Predicate<Issue> hasAllComponents(String... components) {
        return hasAllComponents(ImmutableSet.copyOf(components));
    }

    public static Predicate<Issue> hasAllComponents(Set<String> requiredComponents) {
        return issue -> issue.getComponents().containsAll(requiredComponents);
    }

    public static Predicate<Issue> hasAllLabels(String... labels) {
        return hasAllLabels(ImmutableSet.copyOf(labels));
    }

    public static Predicate<Issue> hasAllLabels(Set<String> requiredLabels) {
        return issue -> issue.getLabels().containsAll(requiredLabels);
    }

    public static Predicate<Issue> hasAnyComponents(String... components) {
        return hasAnyComponents(ImmutableSet.copyOf(components));
    }

    public static Predicate<Issue> hasAnyComponents(Set<String> requiredComponents) {
        return issue -> !Collections.disjoint(issue.getComponents(), requiredComponents);
    }

    public static Predicate<Issue> hasAnyLabels(String... labels) {
        return hasAnyLabels(ImmutableSet.copyOf(labels));
    }

    public static Predicate<Issue> hasAnyLabels(Set<String> requiredLabels) {
        return issue -> !Collections.disjoint(issue.getLabels(), requiredLabels);
    }

    public static Predicate<Issue> inStatusOnDay(LocalDate date, String... statusNames) {
        Set<String> allowedStatuses = ImmutableSet.copyOf(statusNames);
        return issue -> issue.isStatusOnDay(date, allowedStatuses);
    }

    public static Predicate<Issue> isBlockedEntireDay(LocalDate date) {
        return issue -> issue.getAllDayBlockedDays().contains(date);
    }

}
