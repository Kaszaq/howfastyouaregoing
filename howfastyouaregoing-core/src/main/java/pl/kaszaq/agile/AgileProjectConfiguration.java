package pl.kaszaq.agile;

import java.util.HashMap;
import java.util.Map;
import lombok.Value;

/**
 *
 * @author michal.kasza
 */
@Value
public class AgileProjectConfiguration {

    private final IssueStatusMapping issueStatusMapping;

    private AgileProjectConfiguration( IssueStatusMapping issueStatusMapping) {
        this.issueStatusMapping = issueStatusMapping;
    }

    public static AgileProjectConfigurationBuilder builder() {
        return new AgileProjectConfigurationBuilder();
    }

    public static class AgileProjectConfigurationBuilder {

        private Map<String, String> statusMapping = new HashMap<>();

        private AgileProjectConfigurationBuilder() {
        }

        public AgileProjectConfigurationBuilder statusMapping(Map<String, String> statusMapping) {
            this.statusMapping = statusMapping;
            return this;
        }

        public AgileProjectConfiguration build() {
            return new AgileProjectConfiguration(new IssueStatusMapping(statusMapping));
        }

    }

}
