package pl.kaszaq.howfastyouaregoing.examples;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import pl.kaszaq.howfastyouaregoing.Config;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileClientFactory;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import pl.kaszaq.howfastyouaregoing.agile.jira.JiraAgileProjectProviderBuilderFactory;

/**
 * This examples does show use of custom fields from jira. In this example -
 * field with estimates about size of issues. Please note that once you add
 * custom fields library may need to rebuild cache.
 */
public class CustomFieldsExample {

    private static AgileClient agileClient;

    public static void main(String[] args) {
        Config.cacheOnly = true;
        runExample();
    }

    private static void runExample() {
        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("cookievalue")
                .withCacheDir(new File("src/main/resources/cache/"))
                .withJiraUrl("http://localhost:8080/")
                .withCustomFieldsParsers(ImmutableMap.of(
                        "estimatedIssueSize", fieldsNode -> fieldsNode.get("customfield_12345").asInt()
                ))
                .build();

        agileClient = AgileClientFactory.getInstance().newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .create();

        System.out.println("Average estimates of issue sizes in project MYPROJECTID: " + getAverageSize("MYPROJECTID"));
        System.out.println("Average estimates of issue sizes in project PARENTPROJECTID: " + getAverageSize("PARENTPROJECTID"));
    }

    private static double getAverageSize(final String projectId) {
        return agileClient.getAgileProject(projectId).getAllIssues().stream().mapToInt(issue -> (Integer) issue.getCustomFields().get("estimatedIssueSize")).average().getAsDouble();
    }
}
