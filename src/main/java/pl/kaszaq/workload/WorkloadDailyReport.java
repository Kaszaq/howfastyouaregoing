package pl.kaszaq.workload;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import pl.kaszaq.agile.Issue;

@Getter
public class WorkloadDailyReport {

    private final Map<Issue, Double> reportedWorkLoadOnIssue = new HashMap<>();
    private double totalWorkloadOnIssues;

    public void reportWorkloadOnIssue(Issue issue, Double value) {
        reportedWorkLoadOnIssue.merge(issue, value, Double::sum);
        totalWorkloadOnIssues += value;
    }

    /**
     * Returns as percentage per each issue from the total workload calculated.
     * Sum should be +/- 100 - the offset from 100 is caused by computional
     * precision of calculations.
     *
     * @param value
     * @return
     */
    public Map<Issue, Double> calculateDistribution() {
        Double totalVal = getTotalWorkloadOnIssues();
        Map<Issue, Double> workloadDistribution = new HashMap<>();

        getReportedWorkLoadOnIssue().forEach((k, v) -> {
            double val = 100. * v / totalVal;
            workloadDistribution.put(k, val);
        });
        return workloadDistribution;
    }

}
