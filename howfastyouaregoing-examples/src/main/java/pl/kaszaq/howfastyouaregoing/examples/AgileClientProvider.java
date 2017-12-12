package pl.kaszaq.howfastyouaregoing.examples;

import java.io.File;
import pl.kaszaq.howfastyouaregoing.Config;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileClientFactory;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import pl.kaszaq.howfastyouaregoing.agile.jira.JiraAgileProjectProviderBuilderFactory;

public class AgileClientProvider {

    public static AgileClient createClient() {

        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("cookievalue")
                .withCacheOnly(true)
                .withCacheDir(new File("src/main/resources/cache/"))
                .withJiraUrl("http://localhost:8080/")
                .build();

        return AgileClientFactory.newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .create();
    }
}
