package pl.kaszaq.agile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgileClient {

    private static final Pattern ISSUE_PATTERN = Pattern.compile("(\\w+)-\\d+.*");
    private final Map<String, Optional<AgileProject>> agileProjects = new HashMap<>();
    private final AgileProjectProvider agileProjectProvider;
    private final Map<String, AgileProjectConfiguration> projectsConfiguration;
    private final AgileProjectConfiguration DEFAULT_PROJECT_CONFIGURATION = AgileProjectConfiguration.builder().build();

   

    AgileClient(Map<String, AgileProjectConfiguration> configuration, AgileProjectProvider agileProjectProvider) {
        this.projectsConfiguration = configuration;
        this.agileProjectProvider=agileProjectProvider;
    }


    public AgileProject getAgileProject(String projectId) {
        return getProject(projectId).orElseThrow(() -> new RuntimeException("Project " +projectId+" not found"));
    }

    public Optional<Issue> getIssue(String issueId) {
        Matcher m = ISSUE_PATTERN.matcher(issueId);
        if (!m.matches()) {
            return Optional.empty();
        }
        String projectId = m.group(1);
        Optional<AgileProject> agileProject = getProject(projectId);
        return agileProject.map(project -> project.getIssue(issueId));
    }

    private Optional<AgileProject> getProject(String projectId) {
        return agileProjects.computeIfAbsent(projectId, id -> agileProjectProvider.loadProject(id, getConfiguration(id)));
    }

    private AgileProjectConfiguration getConfiguration(String id) {
        return projectsConfiguration.getOrDefault(id, DEFAULT_PROJECT_CONFIGURATION);
    }
}
