package pl.kaszaq.agile.pojo;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import lombok.Value;
import pl.kaszaq.agile.IssueData;

@Value
public class AgileProjectData {
    String projectId;
    ZonedDateTime lastUpdatedIssue;
    ZonedDateTime lastUpdated;
    Map<String, IssueData> issues;
    Set<String> customFieldsNames;

}
