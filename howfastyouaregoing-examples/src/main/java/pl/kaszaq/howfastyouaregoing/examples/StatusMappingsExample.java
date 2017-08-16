package pl.kaszaq.howfastyouaregoing.examples;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import pl.kaszaq.howfastyouaregoing.Config;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileClientFactory;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectConfiguration;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import pl.kaszaq.howfastyouaregoing.agile.jira.JiraAgileProjectProviderBuilderFactory;

/**
 * This examples does show use of status mappings. When you change status names,
 * for instance from "Resolved" to "Closed" this may cause problems in
 * calculating cycle time or drawing CFD. Status mapping allows you to map all
 * statuses to their new, current names.
 */
public class StatusMappingsExample {

    private static AgileClient agileClient;
    private static final String sessionCookieValue = "";

    public static void main(String[] args) {
        Config.cacheOnly = true;
        runExample();
    }

    private static void runExample() {
        agileClient = getClientWithoutMappingForProjectMYPROJECTID();
        System.out.println("Statuses taken from issue transitions: " + getAllStatuses());
        agileClient = getClientWithMappingForProjectMYPROJECTID();
        System.out.println("Statuses taken from issue transitions: " + getAllStatuses());

    }

    private static Set<String> getAllStatuses() {
        return agileClient.getAgileProject("MYPROJECTID").getAllIssues().stream().flatMap(i -> i.getIssueStatusTransitions().stream()).map(t -> t.getToStatus()).distinct().collect(Collectors.toSet());
    }

    private static AgileClient getClientWithoutMappingForProjectMYPROJECTID() {
        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("cookievalue")
                .withCacheDir(new File("src/main/resources/cache/"))
                .withJiraUrl("http://localhost:8080/").build();
        return AgileClientFactory.getInstance().newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .create();
    }

    private static AgileClient getClientWithMappingForProjectMYPROJECTID() {
        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("cookievalue")
                .withCacheDir(new File("src/main/resources/cache/"))
                .withJiraUrl("http://localhost:8080/")
                .build();
        return AgileClientFactory.getInstance().newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .withAgileProjectConfig("MYPROJECTID",
                        AgileProjectConfiguration.builder()
                                .statusMapping(
                                        ImmutableMap.<String, String>builder()
                                                .put("Resolved", "Closed")
                                                .put("Reopened", "Open")
                                                .put("Pending", "Open")
                                                .build())
                                .build())
                .create();
    }
}
