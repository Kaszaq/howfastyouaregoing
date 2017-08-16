package pl.kaszaq.howfastyouaregoing.agile;

import java.util.Map;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class IssueStatusMapping {

    private final Map<String, String> statusMap;

    public String mapStatus(String oldStatus) {
        return oldStatus == null ? null : statusMap
                .getOrDefault(oldStatus, oldStatus);
    }
}
