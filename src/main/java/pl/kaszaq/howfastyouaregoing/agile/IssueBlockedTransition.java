package pl.kaszaq.howfastyouaregoing.agile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 *
 * @author michal.kasza
 */
@Getter
@EqualsAndHashCode
public class IssueBlockedTransition implements Comparable<IssueBlockedTransition> {

    private final String user;
    private final ZonedDateTime date;
    private final String fromStatus;
    private final String toStatus;

    public IssueBlockedTransition(String user, ZonedDateTime date, String fromStatus, String toStatus) {
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

    @JsonIgnore
    public boolean isBlocked() {
        return toStatus != null && !toStatus.isEmpty(); //TODO: is this the right way to do it.. maybe remove this..
    }

    @Override
    public int compareTo(IssueBlockedTransition o) {
        return getDate().compareTo(o.getDate());
    }
}
