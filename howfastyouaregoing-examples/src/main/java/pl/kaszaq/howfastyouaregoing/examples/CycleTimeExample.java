package pl.kaszaq.howfastyouaregoing.examples;

import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.SortedMap;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileProject;
import static pl.kaszaq.howfastyouaregoing.agile.IssuePredicates.hasSubtasks;
import pl.kaszaq.howfastyouaregoing.cycletime.CycleTimeComputer;

public class CycleTimeExample {

    public static void main(String[] args) {
        AgileClient agileClient = AgileClientProvider.createClient();
        runExample(agileClient);

    }

    private static void runExample(AgileClient agileClient) {
        LocalDate from = LocalDate.of(2011, Month.JULY, 1);
        int daysAverage = 30;
        LocalDate to = LocalDate.of(2014, Month.JANUARY, 1);
        AgileProject agileProject = agileClient.getAgileProject("MYPROJECTID");
        String[] cycleTimeStatuses = {"In Progress", "Ready for Testing"};
        final HashSet<String> finalStatuses = Sets.newHashSet("Closed");

        SortedMap<LocalDate, Double> cycleTime = CycleTimeComputer.calulcateCycleTime(agileProject, hasSubtasks().negate(), finalStatuses, cycleTimeStatuses);

        System.out.println(
                "\tCycle time for issues that do not have sub tasks in hours");
        for (LocalDate k = from; !k.isAfter(to); k = k.plusDays(daysAverage)) {
            OptionalDouble valueOptional = cycleTime.subMap(k.minusDays(daysAverage), k.plusDays(1)).values().stream()
                    .mapToDouble(val -> val)
                    .average();
            if (valueOptional.isPresent()) {
                System.out.println(k + "\t" + valueOptional.getAsDouble());
            }
        }

    }
}
