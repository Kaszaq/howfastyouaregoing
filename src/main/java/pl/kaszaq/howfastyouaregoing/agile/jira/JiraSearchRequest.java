package pl.kaszaq.howfastyouaregoing.agile.jira;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class JiraSearchRequest {

    private final String jql;
    private int startAt = 0;
    private int maxResults = 50;
    private final Set<String> fields;
    private final Set<String> expand;
}
