package pl.kaszaq.agile.pojo;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import pl.kaszaq.agile.IssueData;

@Value
public class AgileProjectData {

    String projectId;
    ZonedDateTime lastUpdatedIssue;
    ZonedDateTime lastUpdated;
    Map<String, IssueData> issues;

}
