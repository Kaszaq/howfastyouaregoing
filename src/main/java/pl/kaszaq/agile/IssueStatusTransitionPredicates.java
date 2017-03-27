package pl.kaszaq.agile;

import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Predicate;

public class IssueStatusTransitionPredicates {

    public static Predicate<IssueStatusTransition> createdAfter(LocalDate date) {
        return cl -> cl.getDate().isAfter(date.atStartOfDay(ZoneId.systemDefault()));
    }

    public static Predicate<IssueStatusTransition> createdBefore(LocalDate date) {
        return cl -> cl.getDate().isBefore(date.atStartOfDay(ZoneId.systemDefault()));
    }

    public static Predicate<IssueStatusTransition> from(String... statusNames) {
        ImmutableSet<String> statuses = ImmutableSet.copyOf(statusNames);
        return cl -> statuses.contains(cl.getFromStatus());
    }

    public static Predicate<IssueStatusTransition> to(String... statusNames) {
        ImmutableSet<String> statuses = ImmutableSet.copyOf(statusNames);
        return cl -> statuses.contains(cl.getToStatus());
    }

    public static Predicate<IssueStatusTransition> betweenStatuses(String... statusNames) {
        return from(statusNames).and(to(statusNames));
    }
}
