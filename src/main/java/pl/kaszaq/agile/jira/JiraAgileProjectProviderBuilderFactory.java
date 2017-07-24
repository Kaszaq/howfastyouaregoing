package pl.kaszaq.agile.jira;

import pl.kaszaq.agile.AgileProjectProvider;
import java.io.File;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.agile.jira.JiraAgileProjectProvider;
import pl.kaszaq.http.HttpClient;

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
            return new JiraAgileProjectProvider(client, jiraCacheDirectory, jiraCacheIssuesDirectory, jiraSearchUrl);
        }

    }

}
