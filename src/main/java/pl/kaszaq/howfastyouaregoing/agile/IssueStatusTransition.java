package pl.kaszaq.howfastyouaregoing.agile;

import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class IssueStatusTransition implements Comparable<IssueStatusTransition> {

    private final String user;
    private final ZonedDateTime date;
    private final String fromStatus;
    private final String toStatus;

    public IssueStatusTransition(String user, ZonedDateTime date, String fromStatus, String toStatus) {
        this.user = user;
        this.date = date;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public String getUser() {
        return user;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    @Override
    public int compareTo(IssueStatusTransition o) {
        return getDate().compareTo(o.getDate());
    }
}
