package pl.kaszaq.agile.grouping;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;
import pl.kaszaq.agile.IssueData;

/**
 *
 * @author michal.kasza
 */
@Getter
public class IssueHierarchyNode {

    private final Set<IssueHierarchyNode> childNodes = new HashSet<>();
    private final IssueData issue;

    IssueHierarchyNode(IssueData issue) {
        this.issue = issue;
    }

    void link(IssueHierarchyNode hierarchyNode) {
        childNodes.add(hierarchyNode);
    }

    public void print() {
        print(0);
    }
    private void print(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("\t");
        }
        System.out.println(issue.getKey());
        indent++;
        for (IssueHierarchyNode childNode : childNodes) {
            childNode.print(indent);
        }
    }

    boolean hasChild(IssueData issue) {
        if (childNodes.stream().filter(cn -> cn.getIssue().equals(issue)).findFirst().isPresent()){
            return true;
        } else {
            return childNodes.stream().filter(cn -> cn.hasChild(issue)).findFirst().isPresent();
        }
        
    }

    public Set<IssueData> getLeafsIssues() {
        Set<IssueData> leafIssues = new HashSet<>();
        if (childNodes.isEmpty()){
            leafIssues.add(issue);
        } else {
            childNodes.forEach(cn -> leafIssues.addAll(cn.getLeafsIssues()));
        }
        return leafIssues;
    }

}
