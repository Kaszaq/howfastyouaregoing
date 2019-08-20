package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import java.util.List;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import pl.kaszaq.howfastyouaregoing.agile.IssuePredicates;

/**
 * Prints key of issues which given user has created or moved from one status to
 * another.
 */
public class UserIssuesTest {

    @Test
    public void sampleUserIssuesListTest() {
        List<String> issueKeys = AgileClientProvider.createClient().getAgileProject("AWW").getAllIssues().stream()
                .filter(IssuePredicates.hasStatusTransitionsThat(t -> "david.castro".equals(t.getUser()))
                        .or(i -> "david.castro".equals(i.getCreator()))
                )
                .map(i -> i.getKey())
                .collect(Collectors.toList());
        assertThat(issueKeys).containsOnly(
                "AWW-39", "AWW-33", "AWW-15", "AWW-12", "AWW-99", "AWW-29", "AWW-23", "AWW-127",
                "AWW-121", "AWW-78", "AWW-73", "AWW-76", "AWW-131", "AWW-134", "AWW-88", "AWW-84",
                "AWW-102", "AWW-56", "AWW-54", "AWW-115", "AWW-119", "AWW-8", "AWW-63", "AWW-5", "AWW-2"
        );
    }
}
