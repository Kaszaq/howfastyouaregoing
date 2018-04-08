package pl.kaszaq.howfastyouaregoing.agile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.kaszaq.howfastyouaregoing.utils.DateUtils;

@AllArgsConstructor
class IssueDataWrapper {

    private final IssueData issue;

    @Getter(lazy = true)
    private final Map<String, Duration> timeInStatus = calculateTimeInStatus();

    @Getter(lazy = true)
    private final Set<LocalDate> allDayBlockedDays = calculateDatesWhenAllDayBlocked();

    public Duration getDurationInStatuses(String... statuses) {
        return Stream.of(statuses).map(status -> getTimeInStatus().getOrDefault(status, Duration.ZERO)).reduce(Duration::plus).get();
    }

    private Set<LocalDate> calculateDatesWhenAllDayBlocked() {
        Set<LocalDate> blockedDays = new HashSet<>();

        ZonedDateTime temp = null;
        for (IssueBlockedTransition issueBlockedTransition : issue.getIssueBlockedTransitions()) {
            if (!issueBlockedTransition.isBlocked()) {
                blockedDays.addAll(DateUtils.getCollectionOfLocalDatesBetweenDateExclusive(temp, issueBlockedTransition.getDate()));
                temp = null;
            } else if (temp == null) {
                temp = issueBlockedTransition.getDate();
            }
        }
        if (temp != null) {
            blockedDays.addAll(DateUtils.getCollectionOfLocalDatesBetweenDateExclusive(temp, ZonedDateTime.now()));
            blockedDays.add(LocalDate.now());
        }

        return blockedDays;
    }

    private Duration calculateTotalTimeInStatus(IssueData issue, String status) {
        return calculateTimeInStatus(issue, status, (from, to) -> Duration.between(from, to));
    }

    private Duration calculateTimeInStatus(IssueData issue, String status, BiFunction<ZonedDateTime, ZonedDateTime, Duration> durationFunction) {
        ZonedDateTime temp = null;
        Duration duration = Duration.ZERO;
        for (IssueStatusTransition issueStatusTransition : issue.getIssueStatusTransitions()) {
            if (temp != null) {
                duration = duration.plus(durationFunction.apply(temp, issueStatusTransition.getDate()));
                temp = null;
            }
            if (status.equals(issueStatusTransition.getToStatus())) {
                temp = issueStatusTransition.getDate();
            }
        }
        if (temp != null) {
            duration = duration.plus(durationFunction.apply(temp, ZonedDateTime.now()));
        }
        return duration;
    }

    private Map<String, Duration> calculateTimeInStatus() {
        return issue.getIssueStatusTransitions().stream().map(t -> t.getToStatus()).distinct().collect(
                () -> new HashMap<>(),
                (map, status) -> map.put(status, calculateTotalTimeInStatus(issue, status)),
                Map::putAll);
    }
}
