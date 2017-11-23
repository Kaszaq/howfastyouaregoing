package pl.kaszaq.howfastyouaregoing.agile.jira;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import static pl.kaszaq.howfastyouaregoing.Config.OBJECT_MAPPER;
import pl.kaszaq.howfastyouaregoing.agile.pojo.AgileProjectData;
import pl.kaszaq.howfastyouaregoing.http.HttpClient;
import pl.kaszaq.howfastyouaregoing.json.JsonNodeOptional;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectDataObserver;
import pl.kaszaq.howfastyouaregoing.agile.IssueData;

@Slf4j
public class JiraAgileProjectDataReader implements AgileProjectDataReader {

    private final JiraIssueParser issueParser;
    private final HttpClient httpClient;
    private final File jiraCacheIssuesDirectory;
    private final String jiraSearchEndpoint;
    private final Set<String> customFieldsNames;
    private final int minutesUntilUpdate;

    JiraAgileProjectDataReader(
            HttpClient client,
            File jiraCacheIssuesDirectory,
            String jiraSearchEndpoint,
            Map<String, Function<JsonNodeOptional, Object>> customFieldsParsers,
            int minutesUntilUpdate) {
        this.jiraCacheIssuesDirectory = jiraCacheIssuesDirectory;
        this.jiraSearchEndpoint = jiraSearchEndpoint;
        // todo: it should be possible to close this client
        this.httpClient = client;
        this.issueParser = new JiraIssueParser(customFieldsParsers);
        this.customFieldsNames = new HashSet<>(customFieldsParsers.keySet());
        this.minutesUntilUpdate = minutesUntilUpdate;
    }

    @Override
    public AgileProjectData updateProject(AgileProjectData projectData, AgileProjectDataObserver observer, boolean cacheOnly) throws IOException {
        if (requiresUpdate(projectData)) {
            projectData = tryUpdateFromLocalJiraFiles(projectData, observer);
            if (!cacheOnly) {
                projectData = updateCachedProject(projectData, observer);
            }
        }
        observer.updated(projectData, 1.0);
        return projectData;
    }

    private boolean requiresUpdate(AgileProjectData project) {
        return project.getLastUpdated().isBefore(ZonedDateTime.now().minusMinutes(minutesUntilUpdate));
    }

    private File getIssueFile(String issueId) {
        return new File(jiraCacheIssuesDirectory, issueId + ".json");
    }

    private File[] getIssuesFiles(String projectId) {
        return jiraCacheIssuesDirectory.listFiles((File dir1, String name) -> name.startsWith(projectId + "-"));

    }

    private AgileProjectData updateCachedProject(AgileProjectData projectData, AgileProjectDataObserver observer) throws IOException {
        // TODO: the time zone should be taken from user configuration on jira side.
        ZoneId userJiraZoneId = ZoneId.systemDefault();
        String lastUpdatedQueryValue = projectData.getLastUpdatedIssue().withZoneSameInstant(userJiraZoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        int maxResults = 50;
        int startAt = 0;
        int total = 0;
        ZonedDateTime lastUpdatedIssue;
        do {
            //TODO: maybe bound fields list here to list that is inside POJO and use it instead?
            JiraSearchRequest searchRequest = JiraSearchRequest.builder()
                    .expand(ImmutableSet.of("changelog"))
                    .jql("project = " + projectData.getProjectId() + " AND updated >= \"" + lastUpdatedQueryValue + "\" ORDER BY updated ASC")
                    .maxResults(maxResults)
                    .startAt(startAt)
                    .build();

            String response = httpClient.postJson(jiraSearchEndpoint, searchRequest);
            JsonNode tree = OBJECT_MAPPER.readTree(response);
            maxResults = tree.get("maxResults").asInt();
            startAt = tree.get("startAt").asInt();
            total = tree.get("total").asInt();

            lastUpdatedIssue = projectData.getLastUpdatedIssue();
            Map<String, IssueData> issues = new HashMap<>(projectData.getIssues());
            Iterator<JsonNode> issuesIterator = tree.get("issues").elements();
            while (issuesIterator.hasNext()) {
                JsonNode node = issuesIterator.next();
                IssueData issueData = issueParser.parseJiraIssue(node);
                if (issueData.getUpdated().isAfter(lastUpdatedIssue)) {
                    lastUpdatedIssue = issueData.getUpdated();
                }
                issues.put(issueData.getKey(), issueData);
                try {
                    OBJECT_MAPPER.writeValue(getIssueFile(issueData.getKey()), node);
                } catch (IOException ex) {
                    LOG.warn("Unable to store vanila jira issue data. Issue id: {}", issueData.getKey(), ex);
                }
            };
            projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, lastUpdatedIssue, issues, customFieldsNames);
            startAt = startAt + maxResults;
            if (total > 0 && startAt < total) {
                observer.updated(projectData, (double) startAt / (double) total);
            }
        } while (startAt < total);
        projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, ZonedDateTime.now(), new HashMap<>(projectData.getIssues()), customFieldsNames);
        return projectData;
    }

    private AgileProjectData tryUpdateFromLocalJiraFiles(AgileProjectData projectData, AgileProjectDataObserver observer) throws IOException {
        if (projectData.getIssues().isEmpty()) {
            File[] files = getIssuesFiles(projectData.getProjectId());
            if (files.length > 0) {
                LOG.info("Project was empty but there were files from jira found in cache. "
                        + "Will try to create project from them before attempting to connect to jira. "
                        + "If this behavior was not expected and you need to read all files freshly from jira, "
                        + "you have to remove all cached files, not only project file.");
                ZonedDateTime lastUpdatedIssue = projectData.getLastUpdatedIssue();
                Map<String, IssueData> issues = new HashMap<>(projectData.getIssues());
                for (File file : files) {
                    IssueData issueData = issueParser.parseJiraIssue(OBJECT_MAPPER.readTree(file));
                    if (issueData.getUpdated().isAfter(lastUpdatedIssue)) {
                        lastUpdatedIssue = issueData.getUpdated();
                    }
                    issues.put(issueData.getKey(), issueData);
                }

                projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, lastUpdatedIssue, issues, customFieldsNames);
                observer.updated(projectData, 0.0);
            }
        }
        return projectData;
    }

}
