package pl.kaszaq.howfastyouaregoing.agile.jira;

import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.howfastyouaregoing.agile.CachingAgileProjectProvider;
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
        private String encryptionPassword = null;
        private File cacheDir;
        private String jiraUrl;
        private int minutesUntilUpdate = 15;
        private Map<String, Function<JsonNodeOptional, Object>> customFieldsParsers = Collections.emptyMap();
        private boolean cacheOnly;

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
        
        public JiraAgileProjectProviderBuilder withCacheOnly(boolean cacheOnly) {
            this.cacheOnly = cacheOnly;
            return this;
        }
        
         public JiraAgileProjectProviderBuilder withCacheEncryption(String encryptionPassword) {
            this.encryptionPassword = encryptionPassword;
            return this;
        }

        public JiraAgileProjectProviderBuilder withMinutesUntilUpdate(int minutesUntilUpdate) {
            this.minutesUntilUpdate = minutesUntilUpdate;
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
            File jiraCacheIssuesDirectory = new File(cacheDir, "jira/issues/");
            jiraCacheIssuesDirectory.mkdirs();
            String jiraSearchUrl = jiraUrl + "/rest/api/2/search";
            JiraAgileProjectDataReader reader = new JiraAgileProjectDataReader(client, jiraCacheIssuesDirectory, jiraSearchUrl, customFieldsParsers, minutesUntilUpdate);
            return new CachingAgileProjectProvider(cacheDir, customFieldsParsers.keySet(), reader, cacheOnly);
        }

    }

}
