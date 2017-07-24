package pl.kaszaq.cfd;

import java.util.function.Predicate;
import static pl.kaszaq.agile.IssuePredicates.inResolution;
import static pl.kaszaq.agile.IssuePredicates.isEpic;
import pl.kaszaq.agile.AgileClient;
import pl.kaszaq.agile.IssueData;
import static pl.kaszaq.agile.IssuePredicates.hasSubtasks;

public class CfdDataFactory {

    private final AgileClient agileClient;

    public CfdDataFactory(AgileClient issueProvider) {
        this.agileClient = issueProvider;
    }

    public CfdData calculateCfdData(String projectId, Predicate<IssueData> filter) {
        // TODO: extract those filters. This should be provided as parameter!
        return agileClient.getAgileProject(projectId).getAllIssues().stream()
                .filter(filter)
                .flatMap(issue -> issue.getIssueStatusTransitions().stream())
                .collect(new CfdDataCollector());
    }

}
