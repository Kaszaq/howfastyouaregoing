package pl.kaszaq.howfastyouaregoing.examples;

import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.IssuePredicates;

/**
 * Prints key of issues which given user has created or moved from one status to another.
 */
public class UserIssuesExample {
     private static AgileClient agileClient;

    public static void main(String[] args) {
        agileClient = AgileClientProvider.createClient();

        runExample();
    }

    private static void runExample() {
          agileClient.getAgileProject("MYPROJECTID").getAllIssues().stream()
                .filter(IssuePredicates.hasStatusTransitionsThat(t -> "keiracoombe".equals(t.getUser()))
                        .or(i -> "keiracoombe".equals(i.getCreator()))
                )
                .forEach(i -> System.out.println(i.getKey()));
    }
}
