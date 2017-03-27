package pl.kaszaq.agile.pojo;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import pl.kaszaq.agile.Issue;

@Value
public class AgileProjectData {

    String projectId;
    ZonedDateTime lastUpdatedIssue;
    ZonedDateTime lastUpdated;
    Map<String, Issue> issues;

}
