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
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectDataObserver;
import pl.kaszaq.howfastyouaregoing.agile.pojo.AgileProjectStatuses;
import pl.kaszaq.howfastyouaregoing.agile.IssueData;
import static pl.kaszaq.howfastyouaregoing.clock.HFYAGClock.getClock;
import pl.kaszaq.howfastyouaregoing.storage.FileStorage;

@Slf4j
public class JiraAgileProjectDataReader implements AgileProjectDataReader {

    private final JiraIssueParser issueParser;
    private final HttpClient httpClient;
    private final File jiraCacheIssuesDirectory;
    private final String jiraSearchEndpoint;
    private final Set<String> customFieldsNames;
    private final int minutesUntilUpdate;
    private final FileStorage fileStorage;
    private final JiraProjectStatusReader statusReader;
    private final boolean emptyDescriptionAndSummary;
    private final boolean cacheRawJiraFiles;

    JiraAgileProjectDataReader(
            HttpClient client,
            File jiraCacheIssuesDirectory,
            String jiraUrl,
            Map<String, Function<JsonNode, Object>> customFieldsParsers,
            int minutesUntilUpdate,
            FileStorage fileStorage,
            boolean emptyDescriptionAndSummary,
            boolean cacheRawJiraFiles) {
        this.jiraCacheIssuesDirectory = jiraCacheIssuesDirectory;
        this.jiraSearchEndpoint = jiraUrl + "/rest/api/2/search";
        // todo: it should be possible to close this client
        this.httpClient = client;
        this.issueParser = new JiraIssueParser(customFieldsParsers);
        this.customFieldsNames = new HashSet<>(customFieldsParsers.keySet());
        this.minutesUntilUpdate = minutesUntilUpdate;
        this.fileStorage = fileStorage;
        statusReader = new JiraProjectStatusReader(httpClient, jiraUrl, jiraCacheIssuesDirectory, fileStorage, cacheRawJiraFiles);
        this.emptyDescriptionAndSummary = emptyDescriptionAndSummary;
        this.cacheRawJiraFiles = cacheRawJiraFiles;
    }

    @Override
    public AgileProjectData updateProject(AgileProjectData projectData, AgileProjectDataObserver observer, boolean cacheOnly) throws IOException {
        if (requiresUpdate(projectData)) {
            if (cacheRawJiraFiles) {
                projectData = tryUpdateFromLocalJiraFiles(projectData, observer);
            }
            if (!cacheOnly) {
                projectData = updateCachedProject(projectData, observer);
            }
        }
        observer.updated(projectData, 1.0);
        return projectData;
    }

    private boolean requiresUpdate(AgileProjectData project) {
        return project.getLastUpdated().isBefore(ZonedDateTime.now(getClock()).minusMinutes(minutesUntilUpdate));
    }

    private File getIssueFile(String issueId) {
        return new File(jiraCacheIssuesDirectory, issueId + ".json");
    }

    private File[] getIssuesFiles(String projectId) {
        return jiraCacheIssuesDirectory.listFiles((File dir1, String name) -> name.matches(projectId + "-\\d+\\.json"));
    }

    private AgileProjectData updateCachedProject(AgileProjectData projectData, AgileProjectDataObserver observer) throws IOException {
        // TODO: the time zone should be taken from user configuration on jira side.
        ZoneId userJiraZoneId = ZoneId.systemDefault();
        String lastUpdatedQueryValue = projectData.getLastUpdatedIssue().withZoneSameInstant(userJiraZoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        AgileProjectStatuses newStatuses = statusReader.getProjectStatuses(projectData.getProjectId(), false);
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
                    .fields(ImmutableSet.of("*all"))
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
                IssueData issueData = issueParser.parseJiraIssue(node, emptyDescriptionAndSummary);
                if (issueData.getUpdated().isAfter(lastUpdatedIssue)) {
                    lastUpdatedIssue = issueData.getUpdated();
                }
                issues.put(issueData.getKey(), issueData);
                if (cacheRawJiraFiles) {
                    try {
                        fileStorage.storeFile(getIssueFile(issueData.getKey()), OBJECT_MAPPER.writeValueAsString(node));
                    } catch (IOException ex) {
                        LOG.warn("Unable to store vanila jira issue data. Issue id: {}", issueData.getKey(), ex);
                    }
                }

            };
            projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, lastUpdatedIssue, issues, customFieldsNames, newStatuses);
            startAt = startAt + maxResults;
            if (total > 0 && startAt < total) {
                observer.updated(projectData, (double) startAt / (double) total);
            }
        } while (startAt < total);
        projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, ZonedDateTime.now(getClock()), new HashMap<>(projectData.getIssues()), customFieldsNames, newStatuses);
        return projectData;
    }

    private AgileProjectData tryUpdateFromLocalJiraFiles(AgileProjectData projectData, AgileProjectDataObserver observer) throws IOException {
        if (projectData.getIssues().isEmpty()) {
            File[] files = getIssuesFiles(projectData.getProjectId());
            if (files.length > 0) {
                AgileProjectStatuses statuses = null;
                if (statusReader.areStatusesCached(projectData.getProjectId())) {
                    statuses = statusReader.getProjectStatuses(projectData.getProjectId(), true);
                }

                LOG.info("Project was empty but there were files from jira found in cache. "
                        + "Will try to create project from them before attempting to connect to jira. "
                        + "If this behavior was not expected and you need to read all files freshly from jira, "
                        + "you have to remove all cached files, not only project file.");

                ZonedDateTime lastUpdatedIssue = projectData.getLastUpdatedIssue();
                Map<String, IssueData> issues = new HashMap<>(projectData.getIssues());
                for (File file : files) {

                    IssueData issueData = issueParser.parseJiraIssue(OBJECT_MAPPER.readTree(fileStorage.loadFile(file)), emptyDescriptionAndSummary);
                    if (issueData.getUpdated().isAfter(lastUpdatedIssue)) {
                        lastUpdatedIssue = issueData.getUpdated();
                    }
                    issues.put(issueData.getKey(), issueData);
                }
                projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, lastUpdatedIssue, issues, customFieldsNames, statuses);
                observer.updated(projectData, 0.0);
            }
        }
        return projectData;
    }

}
