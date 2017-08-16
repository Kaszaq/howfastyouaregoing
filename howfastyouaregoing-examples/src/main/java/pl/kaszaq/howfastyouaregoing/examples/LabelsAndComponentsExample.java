package pl.kaszaq.howfastyouaregoing.examples;

import java.util.Set;
import java.util.stream.Collectors;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;

public class LabelsAndComponentsExample {

    private static AgileClient agileClient;

    public static void main(String[] args) {
        agileClient = AgileClientProvider.createClient();

        runExample();
    }

    private static void runExample() {
        System.out.println("Components:");
        System.out.println("  MYPROJECTID: " + getComponentsUsedInProject("MYPROJECTID"));
        System.out.println("  PARENTPROJECTID: " + getComponentsUsedInProject("PARENTPROJECTID"));
        System.out.println("Labels:");
        System.out.println("  MYPROJECTID: " + getLabelsUsedInProject("MYPROJECTID"));
        System.out.println("  PARENTPROJECTID: " + getLabelsUsedInProject("PARENTPROJECTID"));
    }

    private static Set<String> getComponentsUsedInProject(String projectId) {
        return agileClient.getAgileProject(projectId).getAllIssues().stream().flatMap(issue -> issue.getComponents().stream()).collect(Collectors.toSet());
    }

    private static Set<String> getLabelsUsedInProject(String projectId) {
        return agileClient.getAgileProject(projectId).getAllIssues().stream().flatMap(issue -> issue.getLabels().stream()).collect(Collectors.toSet());
    }

}
