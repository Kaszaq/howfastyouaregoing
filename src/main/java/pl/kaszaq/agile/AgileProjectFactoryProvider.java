package pl.kaszaq.agile;

import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.agile.jira.JiraAgileProjectFactory;

@Slf4j
public class AgileProjectFactoryProvider {

    private final JiraAgileProjectFactory jiraAgileProjectFactory = new JiraAgileProjectFactory();

    public JiraAgileProjectFactory getJiraAgileProjectFactory() {
        return jiraAgileProjectFactory;
    }

}
