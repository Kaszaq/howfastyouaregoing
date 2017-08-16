package pl.kaszaq.cfd;

import java.util.function.Predicate;
import pl.kaszaq.agile.AgileClient;
import pl.kaszaq.agile.Issue;

public class CfdDataFactory {

    private final AgileClient agileClient;

    public CfdDataFactory(AgileClient issueProvider) {
        this.agileClient = issueProvider;
    }

    public CfdData calculateCfdData(String projectId, Predicate<Issue> filter) {
        // TODO: extract those filters. This should be provided as parameter!
        return agileClient.getAgileProject(projectId).getAllIssues().stream()
                .filter(filter)
                .flatMap(issue -> issue.getIssueStatusTransitions().stream())
                .collect(new CfdDataCollector());
    }

}
