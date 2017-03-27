package pl.kaszaq.agile;

import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import static pl.kaszaq.utils.CommonPredicates.alwaysTrue;

public class IssuePredicates {
    //TODO: this is probably terrible idea to have such map here...

    private static final Map<Issue, IssueWrapper> issuesWrappers = new HashMap<>();

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

    public static Predicate<Issue> isEpic() {
        return issue -> issue.getType().equals("Epic");
    }

    public static Predicate<Issue> inResolution(String... resolutions) {
        Set<String> allowedStatuses = ImmutableSet.copyOf(resolutions);
        return issue -> issue.getResolution() != null && allowedStatuses.contains(issue.getResolution());
    }

    public static Predicate<Issue> inStatus(String... statusNames) {
        Set<String> allowedStatuses = ImmutableSet.copyOf(statusNames);
        return issue -> allowedStatuses.contains(issue.getStatus());
    }

    public static Predicate<Issue> hasComponents(String... components) {
        Set<String> requiredComponents = ImmutableSet.copyOf(components);
        return issue -> issue.getComponents().containsAll(requiredComponents);
    }

    public static Predicate<Issue> hasLabels(String... labels) {
        Set<String> requiredLabels = ImmutableSet.copyOf(labels);
        return issue -> issue.getLabels().containsAll(requiredLabels);
    }

    public static Predicate<Issue> inStatusOnDay(LocalDate date, String... statusNames) {
        Set<String> allowedStatuses = ImmutableSet.copyOf(statusNames);
        return issue -> {
            IssueWrapper wrapper = getWrapper(issue);
            return wrapper.isStatusOnDay(date, allowedStatuses);
        };
    }
    
    public static Predicate<Issue> isBlockedEntireDay(LocalDate date) {
        return issue -> getWrapper(issue).getAllDayBlockedDays().contains(date);
    }

    private static IssueWrapper getWrapper(Issue issue) {
        return issuesWrappers.computeIfAbsent(issue, i -> new IssueWrapper(i));
    }
}
