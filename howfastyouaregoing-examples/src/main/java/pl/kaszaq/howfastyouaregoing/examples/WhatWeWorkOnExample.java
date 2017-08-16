package pl.kaszaq.howfastyouaregoing.examples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.Issue;
import pl.kaszaq.howfastyouaregoing.agile.grouping.IssueHierarchyNodeProvider;
import pl.kaszaq.howfastyouaregoing.workload.WorkloadDailyReport;
import pl.kaszaq.howfastyouaregoing.workload.WorkloadReportFactory;

/**
 * This example shows distribution of work per day per project in last month.
 * Tasks from given day are taken and grouped using hierarchy between projects.
 * Tasks are grouped according to hierarchy hence in the end are grouped under
 * one top hierarchy item (project)
 */
public class WhatWeWorkOnExample {

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
        SortedMap<LocalDate, WorkloadDailyReport> workloadReport = new TreeMap<>(workloadReportFactory.calculateWorkload(LocalDate.of(2012, Month.JULY, 1), LocalDate.of(2012, Month.NOVEMBER, 1), "MYPROJECTID", "In Progress", "Ready for Testing"));

        List<Issue> issues = workloadReport.values().stream().flatMap(dr -> dr.getReportedWorkLoadOnIssue().keySet().stream()).distinct().collect(Collectors.toList());
        issues.forEach((issue) -> {
            System.out.print("\t" + "[" + issue.getKey() + "]" + issue.getSummary());
        });
        System.out.println();
        workloadReport.forEach((k, v) -> {
            System.out.print(k + "\t");
            double[] workloadofIssue = new double[issues.size()];
            v.calculateDistribution().forEach((k1, v1) -> {
                workloadofIssue[issues.indexOf(k1)] = v1;
            });
            for (int i = 0; i < workloadofIssue.length; i++) {
                double d = workloadofIssue[i];

                System.out.print(NumberUtils.prettyPrint(d) + "\t");
            }
            System.out.println();
        });
    }
}
