package pl.kaszaq.howfastyouaregoing.agile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

public class AgileProject {

    private final Map<String, Issue> data;
    private final String id;
    @Getter(lazy = true)
    private final LocalDateTime firstIssueCreateDate = calculateFirstIssueCreateDate();
    @Getter(lazy = true)
    private final List<String> probableStatusOrder = calculateProbableStatusOrder();
    @Getter(lazy = true)
    private final Collection<Issue> allIssues = calculateAllIssues();

    AgileProject(String id, Map<String, Issue> data) {
        this.id = id;
        this.data = new HashMap<>(data);
    }

    public Issue getIssue(String issueId) {
        return data.get(issueId);
    }

    public boolean contains(Issue issue) {
        return data.containsKey(issue.getKey());
    }

    public boolean contains(String issueKey) {
        return data.containsKey(issueKey);
    }

    public Collection<Issue> calculateAllIssues() {
        return Collections.unmodifiableCollection(data.values());
    }

    private List<String> calculateProbableStatusOrder() {
        return StatusOrderCalculator.getStatusOrder(getAllIssues());
    }

    private LocalDateTime calculateFirstIssueCreateDate() {
        return getAllIssues().stream().map(i -> i.getCreated().toLocalDateTime()).min((o1, o2) -> o1.compareTo(o2)).orElse(null);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AgileProject other = (AgileProject) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
