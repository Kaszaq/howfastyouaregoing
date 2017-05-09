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

    private static final Map<IssueData, IssueWrapper> issuesWrappers = new HashMap<>();

    public static Predicate<IssueData> updatedAfter(ZonedDateTime date) {

        return issue -> issue.getUpdated().isAfter(date);
    }

    public static Predicate<IssueData> createdAfter(ZonedDateTime date) {

        return issue -> issue.getCreated().isAfter(date);
    }

    public static Predicate<IssueData> hasStatusTransitionsThat(Predicate<IssueStatusTransition>... predicates) {
        Optional<Predicate<IssueStatusTransition>> predicate
                = Arrays.stream(predicates).reduce((Predicate<IssueStatusTransition> p1, Predicate<IssueStatusTransition> p2) -> p1.and(p2));

        return issue -> issue.getIssueStatusTransitions().stream().anyMatch(predicate.orElse(alwaysTrue()));
    }

    public static Predicate<IssueData> hasSubtasks() {
        return issue -> !issue.getSubtaskKeys().isEmpty();
    }
    
    public static Predicate<IssueData> isSubtask() {
        return issue -> issue.isSubtask();
    }

    public static Predicate<IssueData> isEpic() {
        return issue -> issue.getType().equals("Epic");
    }

    public static Predicate<IssueData> inResolution(String... resolutions) {
        Set<String> allowedStatuses = ImmutableSet.copyOf(resolutions);
        return issue -> issue.getResolution() != null && allowedStatuses.contains(issue.getResolution());
    }

    public static Predicate<IssueData> inStatus(String... statusNames) {
        Set<String> allowedStatuses = ImmutableSet.copyOf(statusNames);
        return issue -> allowedStatuses.contains(issue.getStatus());
    }

    public static Predicate<IssueData> hasComponents(String... components) {
        Set<String> requiredComponents = ImmutableSet.copyOf(components);
        return issue -> issue.getComponents().containsAll(requiredComponents);
    }

    public static Predicate<IssueData> hasLabels(String... labels) {
        Set<String> requiredLabels = ImmutableSet.copyOf(labels);
        return issue -> issue.getLabels().containsAll(requiredLabels);
    }

    public static Predicate<IssueData> inStatusOnDay(LocalDate date, String... statusNames) {
        Set<String> allowedStatuses = ImmutableSet.copyOf(statusNames);
        return issue -> {
            IssueWrapper wrapper = getWrapper(issue);
            return wrapper.isStatusOnDay(date, allowedStatuses);
        };
    }
    
    public static Predicate<IssueData> isBlockedEntireDay(LocalDate date) {
        return issue -> getWrapper(issue).getAllDayBlockedDays().contains(date);
    }

    private static IssueWrapper getWrapper(IssueData issue) {
        return issuesWrappers.computeIfAbsent(issue, i -> new IssueWrapper(i));
    }
}
