package pl.kaszaq.howfastyouaregoing.agile;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private List<IssueStatusTransition> remapStatusTransitions(IssueData v, IssueStatusMapping statusMapping, Set<String> newAllStatuses) {
        String initialStatus;
        List<IssueStatusTransition> issueStatusTransitions = new ArrayList<>(v.getIssueStatusTransitions());
        if (!issueStatusTransitions.isEmpty()) {
            IssueStatusTransition firstTransition = issueStatusTransitions.get(0);
            initialStatus = firstTransition.getFromStatus();

        } else {
            initialStatus = v.getStatus();
        }

        issueStatusTransitions.add(0, new IssueStatusTransition(v.getCreator(), v.getCreated(), null, initialStatus));

        List<IssueStatusTransition> newStatusTransitionSet = new ArrayList<>();

        String previousStatus = null;
        boolean added = true;
        String newFromStatus = null;
        ZonedDateTime date = null;
        String user = null;
        for (IssueStatusTransition issueStatusTransition : issueStatusTransitions) {
            if (added) {
                newFromStatus = statusMapping.mapStatus(issueStatusTransition.getFromStatus());
                newFromStatus = fixStatusFlowContinuityIfBroken(previousStatus, newFromStatus);
                date = issueStatusTransition.getDate();
                user = issueStatusTransition.getUser();
                added = false;
            }
            String newToStatus = statusMapping.mapStatus(issueStatusTransition.getToStatus());
            if (newAllStatuses.contains(newToStatus)) {
                added = true;
                previousStatus = newToStatus;
                IssueStatusTransition newStatusTransition
                        = new IssueStatusTransition(
                                user,
                                date,
                                newFromStatus,
                                newToStatus);
                newStatusTransitionSet.add(newStatusTransition);
            }
        }

        if (!added) {
            newStatusTransitionSet.add(new IssueStatusTransition(
                    user,
                    date,
                    newFromStatus,
                    statusMapping.mapStatus(v.getStatus())));
        }

        return new ArrayList<>(newStatusTransitionSet);
    }

    private String fixStatusFlowContinuityIfBroken(String previousStatus, String newFromStatus) {
        if ((previousStatus == null && newFromStatus != null)
                || (previousStatus != null && !previousStatus.equals(newFromStatus))) {
            newFromStatus = previousStatus;
        }
        return newFromStatus;
    }
}
