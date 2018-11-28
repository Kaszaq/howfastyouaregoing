package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import org.junit.Test;
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
public class WorkloadTest {

    @Test
    public void sampleWorkloadFeatureTest() {
        // todo: maybe this should be named "effort" spend or smth?
        AgileClient agileClient = AgileClientProvider.createClient();
        WorkloadReportFactory workloadReportFactory = new WorkloadReportFactory(agileClient, new IssueHierarchyNodeProvider(
                ImmutableList.of(
                        ImmutableSet.of(agileClient.getAgileProject("AWW"))
                )));
        Map<LocalDate, WorkloadDailyReport> workloadReport = workloadReportFactory.calculateWorkload(LocalDate.of(2018, Month.JULY, 1), LocalDate.of(2018, Month.NOVEMBER, 1), "AWW", "In Progress");
        Map<Issue, Double> issueCost = new HashMap<>();
        workloadReport.forEach((k, v) -> {
            v.calculateDistribution().forEach((k1, v1) -> {
                issueCost.merge(k1, v1, Double::sum);
            });
        });
        Map<String, String> issueCostInString = new HashMap<>();
        issueCost.forEach((k, v) -> issueCostInString.put(k.toString(), v.toString()));
        assertThat(issueCostInString).containsOnly(
                entry("[AWW-95] Server error message BSOD Windows 10 to my", "300.0"),
                entry("[AWW-98] Windows 10 Problems/Previous versions lingering Removing Unnecessary Context", "366.6666666666667"),
                entry("[AWW-97] INACCESSIBLE_BOOT_DEVICE Daily BSOD IRQL_NOT_LESS_OR_EQUAL More-or-less", "233.33333333333334"),
                entry("[AWW-92] Wait Before Shutting Down? Here we go again", "250.00000000000003"),
                entry("[AWW-93] Creation? After Lock Screen At Startup", "33.333333333333336"),
                entry("[AWW-118] My Computer Computer crashes to windows", "550.0"),
                entry("[AWW-122] Windows 10 Problems/Previous versions lingering Removing icons", "450.0"),
                entry("[AWW-124] Is my boot HD when I mark", "550.0"),
                entry("[AWW-102] USB port not load how", "50.0"),
                entry("[AWW-125] Cmd window pops up ShellCommand", "100.0"),
                entry("[AWW-127] HDD? \"not synched yet\" error message", "200.0"),
                entry("[AWW-89] BSOD Windows 10 Windows 10 Windows 10 Iaquired", "150.0"),
                entry("[AWW-22] More-or-less random freezes, startup failures.", "150.0"),
                entry("[AWW-69] Server Window: explorer.exe - BSODs every few", "150.0"),
                entry("[AWW-25] ShellCommand instead of Local Disc D:", "100.0"),
                entry("[AWW-68] Windows 10 HDD freeing space", "333.33333333333337"),
                entry("[AWW-63] BSOD Windows ? Win10 The", "433.33333333333337"),
                entry("[AWW-84] BSOD when playing games + Unrelated BSOD?", "183.33333333333334"),
                entry("[AWW-21] Lost my emails No Bios screen on", "83.33333333333334"),
                entry("[AWW-87] Aspire 5735z Desktop Not Booting. INACCESSIBLE_BOOT_DEVICE", "333.3333333333333"),
                entry("[AWW-42] Windows 10 Applications take extremely long to Win", "350.0"),
                entry("[AWW-129] Lost sound Best softwares for", "100.0"),
                entry("[AWW-107] Games freezing, no internet connection on", "100.0"),
                entry("[AWW-110] Recovered data collection Defraglar $MFT problem", "583.3333333333334"),
                entry("[AWW-59] Acer 5560 upgrades weird sound", "250.0"),
                entry("[AWW-15] Seemingly due to close, they also freezes", "283.33333333333337"),
                entry("[AWW-56] Multiple Problems with BSOD during POST", "283.33333333333337"),
                entry("[AWW-14] Shutting Down? Here we go again Win 10", "200.0"),
                entry("[AWW-57] One drive slow pc problem - Not", "50.0")
        );
    }
}
