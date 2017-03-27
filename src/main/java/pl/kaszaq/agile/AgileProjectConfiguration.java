package pl.kaszaq.agile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import lombok.Value;
import pl.kaszaq.utils.CommonPredicates;

/**
 *
 * @author michal.kasza
 */
@Value
public class AgileProjectConfiguration {

    private final String[] wipStatuses;
    private final Predicate<Issue> defaultFilter;
    private final IssueStatusMapping issueStatusMapping;

    private AgileProjectConfiguration(final String[] wipStatuses, final Predicate<Issue> defaultFilter, final IssueStatusMapping issueStatusMapping) {

        this.wipStatuses = wipStatuses;

        this.defaultFilter = defaultFilter;
        this.issueStatusMapping = issueStatusMapping;
    }

    public static AgileProjectConfigurationBuilder builder() {
        return new AgileProjectConfigurationBuilder();
    }

    public static class AgileProjectConfigurationBuilder {

        private String[] wipStatuses = new String[0];
        private Predicate<Issue> defaultFilter = CommonPredicates.alwaysTrue();
        private Map<String, String> statusMapping = new HashMap<>();

        private AgileProjectConfigurationBuilder() {
        }
//TODO: maybe add asserts that null cannot be provided

        public AgileProjectConfigurationBuilder wipStatuses(String... wipStatuses) {

            this.wipStatuses = wipStatuses;
            return this;
        }

        public AgileProjectConfigurationBuilder defaultFilter(Predicate<Issue> defaultFilter) {
            this.defaultFilter = defaultFilter;
            return this;
        }

        public AgileProjectConfigurationBuilder statusMapping(Map<String, String> statusMapping) {
            this.statusMapping = statusMapping;
            return this;
        }

        public AgileProjectConfiguration build() {

            return new AgileProjectConfiguration(wipStatuses, defaultFilter, new IssueStatusMapping(statusMapping));
        }

    }

}
