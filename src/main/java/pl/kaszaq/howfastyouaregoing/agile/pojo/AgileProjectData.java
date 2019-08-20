package pl.kaszaq.howfastyouaregoing.agile.pojo;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import lombok.Value;
import pl.kaszaq.howfastyouaregoing.agile.IssueData;

@Value
public class AgileProjectData {
    String projectId;
    ZonedDateTime lastUpdatedIssue;
    ZonedDateTime lastUpdated;
    Map<String, IssueData> issues;
    Set<String> customFieldsNames;
    AgileProjectStatuses statuses;
}
