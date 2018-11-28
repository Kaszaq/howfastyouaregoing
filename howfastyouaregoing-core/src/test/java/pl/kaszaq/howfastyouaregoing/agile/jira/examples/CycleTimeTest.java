package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import org.junit.Test;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileProject;
import static pl.kaszaq.howfastyouaregoing.agile.IssuePredicates.hasSubtasks;
import pl.kaszaq.howfastyouaregoing.cycletime.CycleTimeComputer;

public class CycleTimeTest {

    @Test
    public void testSampleCycleTime() {

        AgileClient agileClient = AgileClientProvider.createClient();
        AgileProject agileProject = agileClient.getAgileProject("AWW");
        final HashSet<String> finalStatuses = Sets.newHashSet("Closed");

        SortedMap<LocalDate, Double> cycleTime = CycleTimeComputer.calulcateCycleTime(agileProject, hasSubtasks().negate(), finalStatuses, "In Progress");
        Map<String, String> cycleTimeInString = new HashMap<>();
        cycleTime.forEach((k, v) -> cycleTimeInString.put(k.toString(), v.toString()));
        assertThat(cycleTimeInString).containsOnly(
                entry("2018-06-28", "52.80027777777778"),
                entry("2018-07-02", "81.46472222222222"),
                entry("2018-07-04", "150.32944444444445"),
                entry("2018-07-05", "52.88194444444444"),
                entry("2018-07-12", "2.203333333333333"),
                entry("2018-07-19", "31.706944444444446"),
                entry("2018-07-25", "2.2402777777777776"),
                entry("2018-07-27", "98.99833333333333"),
                entry("2018-08-06", "36.803472222222226"),
                entry("2018-08-07", "78.7725"),
                entry("2018-08-15", "115.16527777777777"),
                entry("2018-08-16", "168.46416666666667"),
                entry("2018-08-17", "187.4386111111111"),
                entry("2018-08-22", "97.17333333333333"),
                entry("2018-08-28", "263.7391666666667"),
                entry("2018-09-04", "28.835277777777776"),
                entry("2018-09-13", "25.15222222222222"),
                entry("2018-09-28", "142.95277777777778"),
                entry("2018-10-03", "116.54972222222223"),
                entry("2018-10-05", "127.21777777777778"),
                entry("2018-10-09", "4.362222222222222"),
                entry("2018-10-10", "789.4393055555556"),
                entry("2018-10-17", "152.30777777777777"),
                entry("2018-10-26", "212.26916666666668"),
                entry("2018-11-09", "251.27277777777778"),
                entry("2018-11-12", "157.14083333333335"),
                entry("2018-11-14", "332.9625"));
    }
}
