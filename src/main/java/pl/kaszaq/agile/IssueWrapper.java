package pl.kaszaq.agile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.kaszaq.utils.DateUtils;

@AllArgsConstructor
public class IssueWrapper {

    @Getter
    private final IssueData issue;

    @Getter(lazy = true)
    private final Map<String, Duration> timeInStatus = calculateTimeInStatus();
    @Getter(lazy = true)
    private final Map<String, Duration> workTimeInStatus = calculateWorkTimeInStatus();

    @Getter(lazy = true)
    private final Set<LocalDate> allDayBlockedDays = calculateDatesWhenAllDayBlocked();
    private final Map<String, SortedSet<LocalDate>> datesInStatus = new HashMap<>();

    public Duration getDurationInStatuses(String... statuses) {
        return Stream.of(statuses).map(status -> getTimeInStatus().getOrDefault(status, Duration.ZERO)).reduce(Duration::plus).get();
    }

    public boolean isStatusOnDay(LocalDate date, Set<String> statuses) {
        return statuses.stream().anyMatch((status) -> (getDatesInStatus(status).contains(date)));
    }

    private SortedSet<LocalDate> getDatesInStatus(String status) {
        return datesInStatus.computeIfAbsent(status, s -> calculateDatesInStatus(s));
    }

    private SortedSet<LocalDate> calculateDatesInStatus(String requiredStatus) {
        TreeSet<LocalDate> datesInCurrentStatus = new TreeSet<>();
        ZonedDateTime temp = null;
        for (IssueStatusTransition issueStatusTransition : getIssue().getIssueStatusTransitions()) {
            if (temp != null) {
                datesInCurrentStatus.addAll(DateUtils.getCollectionOfLocalDates(temp, issueStatusTransition.getDate()));
                temp = null;
            }
            if (requiredStatus.equals(issueStatusTransition.getToStatus())) {
                temp = issueStatusTransition.getDate();
            }
        }
        if (temp != null) {
            datesInCurrentStatus.addAll(DateUtils.getCollectionOfLocalDates(temp, ZonedDateTime.now()));
        }

        return datesInCurrentStatus;
    }

    private Set<LocalDate> calculateDatesWhenAllDayBlocked() {
        Set<LocalDate> blockedDays = new HashSet<>();

        ZonedDateTime temp = null;
        for (IssueBlockedTransition issueBlockedTransition : getIssue().getIssueBlockedTransitions()) {
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

    private Duration calculateWorkTimeInStatus(IssueData issue, String status) {
        return calculateTimeInStatus(issue, status, IssueWrapper::calculateDurationBetweenInWorkingHours);
    }

    private Duration calculateTotalTimeInStatus(IssueData issue, String status) {
        return calculateTimeInStatus(issue, status, (from, to) -> Duration.between(from, to));
    }

    private Duration calculateTimeInStatus(IssueData issue, String status, BiFunction<ZonedDateTime, ZonedDateTime, Duration> durationFunction) {
        ZonedDateTime temp = null;
        Duration duration = Duration.ZERO;
        for (IssueStatusTransition issueStatusTransition : issue.getIssueStatusTransitions()) {
            if (temp != null) {
                if (issueStatusTransition.getDate() == null) {
                    //TODO: what is that?...
                    System.out.println("aaaa");
                }
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

    private static Duration calculateDurationBetweenInWorkingHours(ZonedDateTime from, ZonedDateTime to) {
        Duration duration = Duration.between(from, to);
        duration = duration.minusHours(duration.toDays() * 16);
        return duration;
    }

    private Map<String, Duration> calculateWorkTimeInStatus() {
        return getIssue().getIssueStatusTransitions().stream().map(t -> t.getToStatus()).distinct().collect(
                () -> new HashMap<>(),
                (map, status) -> map.put(status, calculateWorkTimeInStatus(getIssue(), status)),
                Map::putAll);
    }

    private Map<String, Duration> calculateTimeInStatus() {
        return getIssue().getIssueStatusTransitions().stream().map(t -> t.getToStatus()).distinct().collect(
                () -> new HashMap<>(),
                (map, status) -> map.put(status, calculateTotalTimeInStatus(getIssue(), status)),
                Map::putAll);
    }
}