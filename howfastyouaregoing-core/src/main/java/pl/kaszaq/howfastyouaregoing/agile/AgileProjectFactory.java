package pl.kaszaq.howfastyouaregoing.agile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import pl.kaszaq.howfastyouaregoing.agile.pojo.AgileProjectData;

public class AgileProjectFactory {

    public AgileProject createAgileProject(AgileProjectData projectData, IssueStatusMapping statusMapping) {
        Map<String, Issue> transformedAgileProjectData = new HashMap<>();
        Set<String> newAllStatuses = calculateNewValidStatuses(statusMapping, projectData);
        projectData.getIssues().entrySet().forEach((entry) -> {
            String k = entry.getKey();
            IssueData v = entry.getValue();
            Issue issue = new Issue(IssueData.builder()
                    .created(v.getCreated())
                    .creator(v.getCreator())
                    .summary(v.getSummary())
                    .description(v.getDescription())
                    .issueStatusTransitions(remapStatusTransitions(v, statusMapping, newAllStatuses))
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

    private Set<String> calculateNewValidStatuses(IssueStatusMapping statusMapping, AgileProjectData projectData) {
        Set<String> newAllStatuses = new HashSet<>();
        //TODO: it shoyuldnt be possible to set this as null, or this should be maybe calculated earlier? This is just a silly work around when someone uses library of previous version and does not have this value in storage... I think maybe this should be done better way?
        if (projectData.getStatuses() == null) {
            newAllStatuses = projectData.getIssues().values().stream().flatMap(i -> i.getIssueStatusTransitions().stream()).map(t -> t.getToStatus()).distinct().collect(Collectors.toSet());
        } else {
            newAllStatuses.addAll(projectData.getStatuses().getDoneStatuses());
            newAllStatuses.addAll(projectData.getStatuses().getIndeterminateStatuses());
            newAllStatuses.addAll(projectData.getStatuses().getNewStatuses());
            newAllStatuses.addAll(projectData.getStatuses().getUndefinedStatuses());
        }

        if (statusMapping != null) {
            newAllStatuses = newAllStatuses.stream().map(s -> statusMapping.getMappings().getOrDefault(s, s)).collect(Collectors.toSet());
        }
        return newAllStatuses;
    }

    private TreeSet<IssueStatusTransition> remapStatusTransitions(IssueData v, IssueStatusMapping statusMapping, Set<String> newAllStatuses) {
        if (statusMapping == null) {
            return v.getIssueStatusTransitions();
        }

        TreeSet<IssueStatusTransition> newStatusTransitionSet = new TreeSet<>();

        String previousStatus = null;
        boolean added = true;
        String newFromStatus = null;
        for (IssueStatusTransition issueStatusTransition : v.getIssueStatusTransitions()) {
            if (added) {
                newFromStatus = statusMapping.mapStatus(issueStatusTransition.getFromStatus());
                newFromStatus = fixStatusFlowContinuityIfBroken(previousStatus, newFromStatus);
                added = false;
            }
            String newToStatus = statusMapping.mapStatus(issueStatusTransition.getToStatus());
            if (newAllStatuses.contains(newToStatus)) {
                added = true;
                previousStatus = newToStatus;
                IssueStatusTransition newStatusTransition
                        = new IssueStatusTransition(
                                issueStatusTransition.getUser(),
                                issueStatusTransition.getDate(),
                                newFromStatus,
                                newToStatus);
                newStatusTransitionSet.add(newStatusTransition);
            }
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
