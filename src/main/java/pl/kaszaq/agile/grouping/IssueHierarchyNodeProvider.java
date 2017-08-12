package pl.kaszaq.agile.grouping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import pl.kaszaq.agile.AgileProject;
import pl.kaszaq.agile.IssueData;

/**
 *
 * @author michal.kasza
 */
public class IssueHierarchyNodeProvider {

    Map<IssueData, IssueHierarchyNode> issuesHierarchyNodes = new HashMap<>();
    List<Set<AgileProject>> agileProjectsGroupingOrder;
    Map<AgileProject, Set<AgileProject>> agileParentWithCurrentProjects;
    private final Set<AgileProject> allProjects;

    public IssueHierarchyNodeProvider(List<Set<AgileProject>> agileProjectsGroupingOrder) {
        this.agileProjectsGroupingOrder = agileProjectsGroupingOrder;
        agileParentWithCurrentProjects = new HashMap(agileProjectsGroupingOrder.size());
        for (int i = 0; i < agileProjectsGroupingOrder.size(); i++) {
            Set<AgileProject> parentProjects = agileProjectsGroupingOrder.subList(i + 1, agileProjectsGroupingOrder.size())
                    .stream().flatMap(set -> set.stream()).collect(Collectors.toSet());
            final Set<AgileProject> currentProjects = agileProjectsGroupingOrder.get(i);
            currentProjects.forEach(project -> {
                HashSet<AgileProject> parentProjectsWithCurrent = new HashSet<>(parentProjects);
                parentProjectsWithCurrent.add(project);
                agileParentWithCurrentProjects.put(project, parentProjectsWithCurrent);
            });
        }
        this.allProjects = agileProjectsGroupingOrder
                .stream().flatMap(set -> set.stream()).collect(Collectors.toSet());
    }

    public IssueHierarchyNode getHierarchy(IssueData issue) {
        return issuesHierarchyNodes.computeIfAbsent(issue, i -> {
            IssueHierarchyNode node = createHierarchyNode(i);
            reduce(node);
            return node;
        });
    }

    private IssueHierarchyNode createHierarchyNode(IssueData issue) {
        Set<IssueData> directlyRelatedIssues = getDirectlyParentRelatedIssues(issue);
        IssueHierarchyNode node = new IssueHierarchyNode(issue);
        directlyRelatedIssues.forEach((directlyRelatedIssue) -> {
            node.link(createHierarchyNode(directlyRelatedIssue));
        });

        return node;
    }

    private Set<IssueData> getDirectlyParentRelatedIssues(IssueData issue) {
        Set<IssueData> directlyRelatedIssues = new HashSet<>();

        Set<AgileProject> projectsSet = getParentAndCurrentProjects(issue);
        projectsSet.forEach((agileProject) -> {
            if (agileProject.contains(issue)) {
                checkParentIssue(issue, agileProject)
                        .ifPresent(i -> directlyRelatedIssues.add(i));
                checkEpic(issue, agileProject)
                        .ifPresent(i -> directlyRelatedIssues.add(i));
            } else {
                checkLinkedIssues(issue, agileProject)
                        .ifPresent(issues -> directlyRelatedIssues.addAll(issues));
            }
        });

        return directlyRelatedIssues;
    }

    private Optional<IssueData> checkParentIssue(IssueData issue, AgileProject agileProject) {
        return Optional.of(issue).map(IssueData::getParentIssueKey).map(key -> agileProject.getIssue(key));
    }

    private Optional<IssueData> checkEpic(IssueData issue, AgileProject agileProject) {
        
        // TODO: Add support for linking through epics;
        return Optional.empty();
    }

    private Optional<Set<IssueData>> checkLinkedIssues(IssueData issue, AgileProject agileProject) {
        List<String> issueLinks = issue.getLinkedIssuesKeys();
        if (issueLinks != null) {
            return Optional.of(issueLinks.stream().filter(key -> agileProject.contains(key)).map(issueId -> agileProject.getIssue(issueId)).collect(Collectors.toSet()));
        }
        return Optional.empty();
    }

    private Set<AgileProject> getParentAndCurrentProjects(IssueData issue) {
        AgileProject issueAgileProject = getIssueProject(issue);
        return agileParentWithCurrentProjects.get(issueAgileProject);
    }

    private AgileProject getIssueProject(IssueData issue) {
        return allProjects.stream().filter(project -> project.contains(issue)).findFirst().get();
    }

    private void reduce(IssueHierarchyNode node) {
        Iterator<IssueHierarchyNode> childNodesIterator = node.getChildNodes().iterator();
        while (childNodesIterator.hasNext()) {
            IssueHierarchyNode childNode = childNodesIterator.next();
            if (node.getChildNodes().stream().filter(n -> n.hasChild(childNode.getIssue())).findFirst().isPresent()) {
                childNodesIterator.remove();
            }
        }
        node.getChildNodes().forEach(n -> reduce(n));
    }

}
