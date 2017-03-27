package pl.kaszaq.cfd;

import static pl.kaszaq.agile.IssuePredicates.inResolution;
import static pl.kaszaq.agile.IssuePredicates.isEpic;
import pl.kaszaq.agile.AgileClient;

public class CfdDataFactory {

    private final AgileClient agileClient;

    public CfdDataFactory(AgileClient issueProvider) {
        this.agileClient = issueProvider;
    }

    public CfdData calculateCfdData(String projectId) {
        return agileClient.getAgileProject(projectId).getAllIssues().stream()
                .filter(
                        inResolution("Won't Fix", "Cannot Reproduce", "Duplicate", "Incomplete", "Not an Issue", "Not Enough Information", "Retest", "Unresolved").negate()
                                .and(isEpic().negate())
                )
                .flatMap(issue -> issue.getIssueStatusTransitions().stream())
                .collect(new CfdDataCollector());
    }

}
