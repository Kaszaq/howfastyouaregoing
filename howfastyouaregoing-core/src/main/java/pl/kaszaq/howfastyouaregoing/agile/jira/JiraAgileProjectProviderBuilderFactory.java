package pl.kaszaq.howfastyouaregoing.agile.jira;

import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.howfastyouaregoing.agile.jira.JiraAgileProjectProvider;
import pl.kaszaq.howfastyouaregoing.http.HttpClient;
import pl.kaszaq.howfastyouaregoing.json.JsonNodeOptional;

@Slf4j
public class JiraAgileProjectProviderBuilderFactory {

    public static JiraAgileProjectProviderBuilder withJsession(String jsessionId) {
        return new JiraAgileProjectProviderBuilder(jsessionId);
    }

    public static JiraAgileProjectProviderBuilder withCredentials(String username, String password) {
        return new JiraAgileProjectProviderBuilder(username, password);
    }

    public static class JiraAgileProjectProviderBuilder {

        private String jsessionId;
        private String username;
        private String password;
        private File cacheDir;
        private String jiraUrl;
        private Map<String, Function<JsonNodeOptional, Object>> customFieldsParsers = Collections.emptyMap();

        private JiraAgileProjectProviderBuilder(String username, String password) {
            this.username = username;
            this.password = password;
        }

        private JiraAgileProjectProviderBuilder(String jsessionId) {
            this.jsessionId = jsessionId;
        }

        public static JiraAgileProjectProviderBuilder withJsessionId(String jsessionId) {
            return new JiraAgileProjectProviderBuilder(jsessionId);
        }

        public static JiraAgileProjectProviderBuilder withCredentials(String username, String password) {
            return new JiraAgileProjectProviderBuilder(username, password);
        }

        public JiraAgileProjectProviderBuilder withCacheDir(File cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }
        public JiraAgileProjectProviderBuilder withCustomFieldsParsers(
                Map<String, Function<JsonNodeOptional, Object>> customFieldsParsers) {
            this.customFieldsParsers = customFieldsParsers;
            return this;
        }

        public JiraAgileProjectProviderBuilder withJiraUrl(String jiraUrl) {
            this.jiraUrl = jiraUrl;
            return this;
        }

        public AgileProjectProvider build() {
            HttpClient client;
            if (jsessionId != null) {
                client = new HttpClient(jsessionId);
            } else {
                client = new HttpClient(username, password);
            }
            if (cacheDir == null) {
                cacheDir = new File("cache/");
            }
            File jiraCacheDirectory = new File(cacheDir, "jira/");
            File jiraCacheIssuesDirectory = new File(jiraCacheDirectory, "issues/");
            jiraCacheDirectory.mkdirs();
            jiraCacheIssuesDirectory.mkdirs();
            String jiraSearchUrl = jiraUrl + "/rest/api/2/search";
            return new JiraAgileProjectProvider(client, jiraCacheDirectory, jiraCacheIssuesDirectory, jiraSearchUrl, customFieldsParsers);
        }

    }

}
