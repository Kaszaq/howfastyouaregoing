package pl.kaszaq.agile.jira;

import pl.kaszaq.agile.AgileProjectProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.Config;
import static pl.kaszaq.Config.OBJECT_MAPPER;
import pl.kaszaq.agile.AgileProject;
import pl.kaszaq.agile.pojo.AgileProjectData;
import pl.kaszaq.http.HttpClient;
import pl.kaszaq.json.JsonNodeOptional;
import pl.kaszaq.agile.AgileProjectConfiguration;
import pl.kaszaq.agile.AgileProjectFactory;
import pl.kaszaq.agile.IssueData;

@Slf4j
public class JiraAgileProjectProvider implements AgileProjectProvider {
// TODO: move from here Jira part and part responsible for storing project.

    private static final ZonedDateTime INITIAL_DATE = ZonedDateTime.of(1970, Month.JANUARY.getValue(), 1, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final int MINUTES_UNTIL_UPDATE_REQUESTED = 15;
    private final JiraIssueParser issueParser;
    private final HttpClient httpClient;
    private final File jiraCacheDirectory;
    private final File jiraCacheIssuesDirectory;
    private final String jiraSearchEndpoint;
    private final Set<String> customFieldsNames;

    JiraAgileProjectProvider(
            HttpClient client,
            File jiraCacheDirectory,
            File jiraCacheIssuesDirectory,
            String jiraSearchEndpoint,
            Map<String, Function<JsonNodeOptional, Object>> customFieldsParsers) {
        this.jiraCacheDirectory = jiraCacheDirectory;
        this.jiraCacheIssuesDirectory = jiraCacheIssuesDirectory;
        this.jiraSearchEndpoint = jiraSearchEndpoint;
        this.httpClient = client;
        this.issueParser = new JiraIssueParser(customFieldsParsers);
        this.customFieldsNames = new HashSet<>(customFieldsParsers.keySet());
    }

    @Override
    public Optional<AgileProject> loadProject(String projectId, AgileProjectConfiguration configuration) {
        try {
            Optional<AgileProjectData> projectDataOptional = loadProjectFromFile(projectId);
            AgileProjectData projectData = projectDataOptional.orElse(createNewEmptyProject(projectId));
            if (!customFieldsNames.equals(projectData.getCustomFieldsNames())) {
                LOG.info("Noticied different setup of custom fields. Forcing to recreate project.");
                projectData = createNewEmptyProject(projectId);
            }
            projectData = tryUpdateFromLocalJiraFiles(projectData);
            if (!Config.cacheOnly && requiresUpdate(projectData)) {
                projectData = updateCachedProject(projectData);
            }

            return Optional.of(new AgileProjectFactory().createAgileProject(projectData, configuration.getIssueStatusMapping()));
        } catch (IOException ex) {
            LOG.warn("Problem while reading project data of project {}" + projectId, ex);
            return Optional.empty();
        }
    }

    private AgileProjectData createNewEmptyProject(String projectId) {
        return new AgileProjectData(projectId, INITIAL_DATE, INITIAL_DATE, new HashMap<>(), customFieldsNames);
    }

    private boolean requiresUpdate(AgileProjectData project) {
        return project.getLastUpdated().isBefore(ZonedDateTime.now().minusMinutes(MINUTES_UNTIL_UPDATE_REQUESTED));
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
        return new File(jiraCacheDirectory, projectId + ".json");
    }

    private File getIssueFile(String issueId) {
        return new File(jiraCacheIssuesDirectory, issueId + ".json");
    }

    private File[] getIssuesFiles(String projectId) {
        return jiraCacheIssuesDirectory.listFiles((File dir1, String name) -> name.startsWith(projectId + "-"));

    }

    private void saveProjectToFile(AgileProjectData project) throws IOException {
        File projectFile = getProjectFile(project.getProjectId());
        OBJECT_MAPPER.writeValue(projectFile, project);
    }

    private AgileProjectData updateCachedProject(AgileProjectData projectData) throws IOException {
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

            String response = httpClient.postJson(jiraSearchEndpoint, searchRequest);
            JsonNode tree = OBJECT_MAPPER.readTree(response);
            maxResults = tree.get("maxResults").asInt();
            startAt = tree.get("startAt").asInt();
            total = tree.get("total").asInt();

            ZonedDateTime lastUpdatedIssue = projectData.getLastUpdatedIssue();
            ZonedDateTime lastUpdated = projectData.getLastUpdated();
            Map<String, IssueData> issues = new HashMap<>(projectData.getIssues());
            Iterator<JsonNode> issuesIterator = tree.get("issues").elements();
            while (issuesIterator.hasNext()) {
                JsonNode node = issuesIterator.next();
                IssueData issueData = issueParser.parseJiraIssue(node);
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
            projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, lastUpdated, issues, customFieldsNames);
            saveProjectToFile(projectData);
            startAt = startAt + maxResults;
        } while (startAt < total);
        return projectData;
    }

    private AgileProjectData tryUpdateFromLocalJiraFiles(AgileProjectData projectData) throws IOException {
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

                ZonedDateTime lastUpdated = ZonedDateTime.now();
                projectData = new AgileProjectData(projectData.getProjectId(), lastUpdatedIssue, lastUpdated, issues, customFieldsNames);
                saveProjectToFile(projectData);
            }
        }
        return projectData;
    }

}
