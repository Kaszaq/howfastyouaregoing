package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileClientFactory;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectConfiguration;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import pl.kaszaq.howfastyouaregoing.agile.jira.JiraAgileProjectProviderBuilderFactory;

/**
 * This examples does show use of status mappings. When you would like for
 * whatever reason merge statuses when looking at metrics, for instance
 * "Resolved" with "Closed" you can do this using status mappings.
 */
public class StatusMappingsTest {

    @Test
    public void sampleStatusMappingTest() {
        assertThat(getAllStatuses(getClientWithoutMappingForProjectMYPROJECTID())).containsOnly(
                "Reopened", "Closed", "In Progress", "Resolved", "Open"
        );
        assertThat(getAllStatuses(getClientWithMappingForProjectMYPROJECTID())).containsOnly(
                "Closed", "In Progress", "Open"
        );
    }

    private static Set<String> getAllStatuses(AgileClient agileClient) {
        return agileClient.getAgileProject("AWW").getAllIssues().stream().flatMap(i -> i.getIssueStatusTransitions().stream()).map(t -> t.getToStatus()).distinct().collect(Collectors.toSet());
    }

    private static AgileClient getClientWithoutMappingForProjectMYPROJECTID() {
        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("")
                .withCacheDir(new File("src/test/resources/AWW_data_after_update/"))
                .withCacheOnly(true)
                .withJiraUrl("").build();
        return AgileClientFactory.newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .create();
    }

    private static AgileClient getClientWithMappingForProjectMYPROJECTID() {
        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("")
                .withCacheDir(new File("src/test/resources/AWW_data_after_update/"))
                .withCacheOnly(true)
                .withJiraUrl("")
                .build();
        return AgileClientFactory.newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .withAgileProjectConfig("AWW",
                        AgileProjectConfiguration.builder()
                                .statusMapping(
                                        ImmutableMap.<String, String>builder()
                                                .put("Resolved", "Closed")
                                                .put("Reopened", "Open")
                                                .build())
                                .build())
                .create();
    }
}
