package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import java.util.Set;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;

public class LabelsAndComponentsTest {

    @Test
    public void testSampleLabelsAndComponentsList() {
        AgileClient agileClient = AgileClientProvider.createClient();
        assertThat(getComponentsUsedInProject(agileClient, "AWW")).containsOnly(
                "Drawing", "Validation", "Server", "Infrastucture", "Spell", "Web Client",
                "Controller Unit", "Schema Builder", "Fax Server", "Performance",
                "Overall System", "Export to XML", "Windows Client"
        );
        assertThat(getLabelsUsedInProject(agileClient, "AWW")).containsOnly(                
                "WebScale", "Backend", "Paradigm", "Sales", "Scalability", "Legacy", "Proactive",
                "Synergy", "Upgrade", "Communication", "International", "Customer_Service"
        );
    }

    private static Set<String> getComponentsUsedInProject(AgileClient agileClient, String projectId) {
        return agileClient.getAgileProject(projectId).getAllIssues().stream().flatMap(issue -> issue.getComponents().stream()).collect(Collectors.toSet());
    }

    private static Set<String> getLabelsUsedInProject(AgileClient agileClient, String projectId) {
        return agileClient.getAgileProject(projectId).getAllIssues().stream().flatMap(issue -> issue.getLabels().stream()).collect(Collectors.toSet());
    }

}
