package pl.kaszaq.howfastyouaregoing.examples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.Issue;
import pl.kaszaq.howfastyouaregoing.agile.grouping.IssueHierarchyNodeProvider;
import pl.kaszaq.howfastyouaregoing.workload.WorkloadDailyReport;
import pl.kaszaq.howfastyouaregoing.workload.WorkloadReportFactory;

/**
 * How much effort we have spent during last month per project. Tasks are
 * grouped according to hierarchy hence in the end are grouped under one top
 * hierarchy item (project).
 *
 * @author michal.kasza
 */
public class WorkloadExample {

    private static AgileClient agileClient;

    public static void main(String[] args) {
        agileClient = AgileClientProvider.createClient();

        runExample();
    }

    private static void runExample() {
        WorkloadReportFactory workloadReportFactory = new WorkloadReportFactory(agileClient, new IssueHierarchyNodeProvider(
                ImmutableList.of(
                        ImmutableSet.of(agileClient.getAgileProject("MYPROJECTID")),
                        ImmutableSet.of(agileClient.getAgileProject("PARENTPROJECTID"))
                )));
        Map<LocalDate, WorkloadDailyReport> workloadReport = workloadReportFactory.calculateWorkload(LocalDate.of(2013, Month.JULY, 1), LocalDate.of(2013, Month.NOVEMBER, 1), "MYPROJECTID", "In Progress", "Ready for Testing");
        Map<Issue, Double> issueCost = new HashMap<>();
        workloadReport.forEach((k, v) -> {
            v.calculateDistribution().forEach((k1, v1) -> {
                issueCost.merge(k1, v1, Double::sum);
            });
        });
        issueCost.forEach((k1, v1) -> {
            System.out.println(k1.getPrettyName() + "\t" + NumberUtils.prettyPrint(v1));
        });
    }
}
