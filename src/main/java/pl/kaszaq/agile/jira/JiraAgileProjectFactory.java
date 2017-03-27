package pl.kaszaq.agile.jira;

import pl.kaszaq.agile.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.Config;
import static pl.kaszaq.Config.OBJECT_MAPPER;
import pl.kaszaq.agile.pojo.AgileProjectData;
import pl.kaszaq.http.HttpClient;

@Slf4j
public class JiraAgileProjectFactory {

    private static final File JIRA_CACHE_DIRECTORY = new File("cache/jira/");
    private static final File JIRA_CACHE_ISSUES_DIRECTORY = new File("cache/jira/issues/");
    private static final ZonedDateTime INITIAL_DATE = ZonedDateTime.of(1970, Month.JANUARY.getValue(), 1, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final int MINUTES_UNTIL_UPDATE_REQUESTED = 15;
    private final JiraIssueParser issueParser = new JiraIssueParser();
    private final HttpClient httpClient = new HttpClient(Config.getInstance().getJiraJsessionId());

    public JiraAgileProjectFactory() {

        //TODO: this should not be inside constructor
        JIRA_CACHE_DIRECTORY.mkdirs();
        JIRA_CACHE_ISSUES_DIRECTORY.mkdirs();
    }

    public Optional<AgileProject> loadProject(String projectId, AgileProjectConfiguration configuration) {
        try {
            Optional<AgileProjectData> projectDataOptional = loadProjectFromFile(projectId);
            AgileProjectData projectData = projectDataOptional.orElse(new AgileProjectData(projectId, INITIAL_DATE, INITIAL_DATE, new HashMap<>()));

            projectData = updateProjectData(projectData);
            return Optional.of(new AgileProjectFactory().createAgileProject(projectData, configuration.getIssueStatusMapping()));
        } catch (IOException ex) {
            LOG.warn("Problem while reading project data of project {}" + projectId, ex);
            return Optional.empty();
        }
    }

    private boolean requiresUpdate(AgileProjectData project) {
        return project.getLastUpdated().isBefore(ZonedDateTime.now().minusMinutes(MINUTES_UNTIL_UPDATE_REQUESTED));
    }

    private AgileProjectData updateProjectData(AgileProjectData projectData) throws IOException {
        if (!Config.cacheOnly && requiresUpdate(projectData)) {
            return getUpdatedProject(projectData);
        } else {
            return projectData;
        }
    }

    private Optional<AgileProjectData> loadProjectFromFile(String projectId) throws IOException {
        File projectFile = getProjectFile(projectId);
        if (projectFile.exists()) {
            return Optional.of(OBJECT_MAPPER.readValue(projectFile, AgileProjectData.class));
        } else {
            return Optional.empty();
        }
    }

    private File getProjectFile(String projectId) {

        return new File(JIRA_CACHE_DIRECTORY, projectId + ".json");
    }

    private File getIssueFile(String issueId) {
        return new File(JIRA_CACHE_ISSUES_DIRECTORY, issueId + ".json");
    }

    private void saveProjectToFile(AgileProjectData project) throws IOException {
        File projectFile = getProjectFile(project.getProjectId());
        OBJECT_MAPPER.writeValue(projectFile, project);
    }

    private AgileProjectData getUpdatedProject(AgileProjectData projectData) throws IOException {

        // TODO: the time zone should be taken from user configuration on jira side.
        ZoneId userJiraZoneId = ZoneId.systemDefault();
        String lastUpdatedQueryValue = projectData.getLastUpdatedIssue().withZoneSameInstant(userJiraZoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        int maxResults = 50;
        int startAt = 0;
        int total = 0;
        do {
            //TODO: maybe bound fields list here to list that is inside POJO and use it instead?
            JiraSearchRequest searchRequest = JiraSearchRequest.builder()
                    .expand(ImmutableSet.of("changelog"))
                    .jql("project = " + projectData.getProjectId() + " AND updated >= \"" + lastUpdatedQueryValue + "\" ORDER BY updated ASC")
                    .maxResults(maxResults)
                    .startAt(startAt)
                    .build();
            String url = Config.getInstance().getJiraUrl()+"/rest/api/2/search";

            String response = httpClient.postJson(url, searchRequest);
            JsonNode tree = OBJECT_MAPPER.readTree(response);
            maxResults = tree.get("maxResults").asInt();
            startAt = tree.get("startAt").asInt();
            total = tree.get("total").asInt();

            ZonedDateTime lastUpdatedIssue = projectData.getLastUpdatedIssue();
            ZonedDateTime lastUpdated = projectData.getLastUpdated();
            Map<String, Issue> issues = new HashMap<>(projectData.getIssues());
            Iterator<JsonNode> issuesIterator = tree.get("issues").elements();
            while (issuesIterator.hasNext()) {
                JsonNode node = issuesIterator.next();
                Issue issueData = issueParser.parseJiraIssue(node);
                if (issueData.getUpdated().isAfter(lastUpdatedIssue)) {
                    lastUpdatedIssue = issueData.getUpdated();
                }
                issues.put(issueData.getKey(), issueData);
                lastUpdated = ZonedDateTime.now();
                try {
                    OBJECT_MAPPER.writeValue(getIssueFile(issueData.getKey()), node);
                } catch (IOException ex) {
                    LOG.warn("Unable to store vanila jira issue data. Issue id: {}", issueData.getKey(), ex);
                }
            };
            projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, lastUpdated, issues);
            saveProjectToFile(projectData);
            startAt = startAt + maxResults;
        } while (startAt < total);
        return projectData;
    }

}
