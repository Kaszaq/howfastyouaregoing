package pl.kaszaq.howfastyouaregoing.cfd;

import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import pl.kaszaq.howfastyouaregoing.agile.AgileProject;
import pl.kaszaq.howfastyouaregoing.agile.Issue;

@UtilityClass
public class CfdDataComputer {

    public CfdData calculateCfdData(AgileProject agileProject, Predicate<Issue> filter) {
        return agileProject.getAllIssues().stream()
                .filter(filter)
                .flatMap(issue -> issue.getIssueStatusTransitions().stream())
                .collect(new CfdDataCollector());
    }

}
