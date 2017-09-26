package pl.kaszaq.howfastyouaregoing.agile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import pl.kaszaq.howfastyouaregoing.agile.pojo.AgileProjectData;

public class AgileProjectFactory {

    public AgileProject createAgileProject(AgileProjectData projectData, IssueStatusMapping statusMapping) {
        Map<String, Issue> transformedAgileProjectData = new HashMap<>();
        projectData.getIssues().entrySet().forEach((entry) -> {
            String k = entry.getKey();
            IssueData v = entry.getValue();
            Issue issue = new Issue(IssueData.builder()
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
                    .customFields(v.getCustomFields())
                    .build());
            transformedAgileProjectData.put(k, issue);
        });
        return new AgileProject(projectData.getProjectId(), transformedAgileProjectData);
    }

    private TreeSet<IssueStatusTransition> remapStatusTransitions(IssueData v, IssueStatusMapping statusMapping) {
        if (statusMapping == null) {
            return v.getIssueStatusTransitions();
        }

        TreeSet<IssueStatusTransition> newStatusTransitionSet = new TreeSet<>();

        String previousStatus = null;
        for (IssueStatusTransition issueStatusTransition : v.getIssueStatusTransitions()) {
            String newFromStatus = issueStatusTransition.getFromStatus();
            String newToStatus = issueStatusTransition.getToStatus();
            
            newFromStatus = fixStatusFlowContinuityIfBroken(previousStatus, newFromStatus);
            previousStatus = newToStatus;
            
            IssueStatusTransition newStatusTransition
                    = new IssueStatusTransition(
                            issueStatusTransition.getUser(),
                            issueStatusTransition.getDate(),
                            statusMapping.mapStatus(newFromStatus),
                            statusMapping.mapStatus(newToStatus));
            newStatusTransitionSet.add(newStatusTransition);
        }
        return newStatusTransitionSet;
    }

    private String fixStatusFlowContinuityIfBroken(String previousStatus, String newFromStatus) {
        if ((previousStatus == null && newFromStatus != null)
                || (previousStatus != null && !previousStatus.equals(newFromStatus))) {
            newFromStatus = previousStatus;
        }
        return newFromStatus;
    }
}
