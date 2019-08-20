package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import java.io.File;
import pl.kaszaq.howfastyouaregoing.Config;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileClientFactory;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import pl.kaszaq.howfastyouaregoing.agile.jira.JiraAgileProjectProviderBuilderFactory;

public class AgileClientProvider {

    public static AgileClient createClient() {

        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("")
                .withCacheOnly(true)
                .withCacheDir(new File("src/test/resources/AWW_data_after_update/"))
                .withJiraUrl("/")
                .build();

        return AgileClientFactory.newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .create();
    }
}
