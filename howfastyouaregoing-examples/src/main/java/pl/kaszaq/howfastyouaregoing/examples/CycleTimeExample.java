package pl.kaszaq.howfastyouaregoing.examples;

import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.time.Month;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileProject;
import static pl.kaszaq.howfastyouaregoing.agile.IssuePredicates.hasSubtasks;
import static pl.kaszaq.howfastyouaregoing.agile.IssuePredicates.isSubtask;
import pl.kaszaq.howfastyouaregoing.cycletime.CycleTimeComputer;

public class CycleTimeExample {

    public static void main(String[] args) {
        AgileClient agileClient = AgileClientProvider.createClient();
        runExample(agileClient);

    }

    private static void runExample(AgileClient agileClient) {
        LocalDate from = LocalDate.of(2011, Month.JULY, 1);
        int daysAverage = 30;
        LocalDate to = from.plusDays(daysAverage);
        System.out.println(
                "\tCycle time for stories [hours]"
                + "\tCycle time for subtasks [hours]"
                + "\tCycle time for tasks without subtasks [hours]");
        while (to.isBefore(LocalDate.of(2014, Month.JANUARY, 1))) {
            cycleTimeExample(agileClient, from, to);
            from = from.plusDays(daysAverage);
            to = to.plusDays(daysAverage);
        }
    }

    private static void cycleTimeExample(AgileClient agileClient, LocalDate fromDate, LocalDate toDate) {
       
        AgileProject agileProject = agileClient.getAgileProject("MYPROJECTID");
        String[] cycleTimeStatuses = {"In Progress", "Ready for Testing"};
        CycleTimeComputer computer = new CycleTimeComputer(agileProject, Sets.newHashSet("Closed"));

        System.out.print(toDate);

        double calulcateCycleTimeOfStories = computer.calulcateCycleTimeOfStories(
                hasSubtasks(), 
                fromDate, toDate, cycleTimeStatuses);
        
        System.out.print("\t" + (calulcateCycleTimeOfStories > 0 ? NumberUtils.prettyPrint(calulcateCycleTimeOfStories) : ""));

        double calulcateCycleTimeOfSubtasks = computer.calulcateCycleTimeOfAllIssues(
                isSubtask(), 
                fromDate, toDate, cycleTimeStatuses);
        System.out.print("\t" + (calulcateCycleTimeOfSubtasks > 0 ? NumberUtils.prettyPrint(calulcateCycleTimeOfSubtasks) : ""));

        double calulcateCycleTimeOfAllIssuesWithoutSubtasks = computer.calulcateCycleTimeOfAllIssues(
                hasSubtasks().negate().and(isSubtask().negate()), 
                fromDate, toDate, cycleTimeStatuses);
        System.out.print("\t" + (calulcateCycleTimeOfAllIssuesWithoutSubtasks > 0 ? NumberUtils.prettyPrint(calulcateCycleTimeOfAllIssuesWithoutSubtasks) : ""));
        System.out.println();
    }

}
