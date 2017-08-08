package pl.kaszaq.agile;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import pl.kaszaq.agile.pojo.AgileProjectData;

public class AgileProjectFactory {

    public AgileProject createAgileProject(AgileProjectData projectData, IssueStatusMapping statusMapping) {
        Map<String, IssueData> transformedAgileProjectData = new HashMap<>();
        for (Map.Entry<String, IssueData> entry : projectData.getIssues().entrySet()) {
            String k = entry.getKey();
            IssueData v = entry.getValue();
            IssueData issue = IssueData.builder()
                    .created(v.getCreated())
                    .creator(v.getCreator())
                    .summary(v.getSummary())
                    .description(v.getDescription())
                    .issueStatusTransitions(remapStatusTransitions(v, statusMapping))
                    .issueBlockedTransitions(v.getIssueBlockedTransitions())
                    .key(v.getKey())
                    .linkedIssuesKeys(v.getLinkedIssuesKeys())
                    .parentIssueKey(v.getParentIssueKey())
                    .resolution(v.getResolution())
                    .subtask(v.isSubtask())
                    .subtaskKeys(v.getSubtaskKeys())
                    .status(v.getStatus())
                    .updated(v.getUpdated())
                    .type(v.getType())
                    .labels(v.getLabels())
                    .components(v.getComponents())
                    .timesheetsCode(v.getTimesheetsCode())
                    .build();
            transformedAgileProjectData.put(k, issue);
        }
        return new AgileProject(projectData.getProjectId(), transformedAgileProjectData);
    }

    private TreeSet<IssueStatusTransition> remapStatusTransitions(IssueData v, IssueStatusMapping statusMapping) {
        if (statusMapping == null) {
            return v.getIssueStatusTransitions();
        }
        TreeSet<IssueStatusTransition> newStatusTransitionSet = new TreeSet<>();
        for (IssueStatusTransition issueStatusTransition : v.getIssueStatusTransitions()) {
            IssueStatusTransition newStatusTransition
                    = new IssueStatusTransition(
                            issueStatusTransition.getUser(),
                            issueStatusTransition.getDate(),
                            statusMapping.mapStatus(issueStatusTransition.getFromStatus()),
                            statusMapping.mapStatus(issueStatusTransition.getToStatus()));
            newStatusTransitionSet.add(newStatusTransition);
        }
        return newStatusTransitionSet;
    }
}
