package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import org.junit.Test;
import pl.kaszaq.howfastyouaregoing.agile.IssueStatusTransitionPredicates;
import static pl.kaszaq.howfastyouaregoing.agile.IssueStatusTransitionPredicates.betweenStatuses;

public class WipTest {

    @Test
    public void sampleWipTest() {
        // TODO: rewrite this example due to the below:
        // this is very hard to read and should be rewritten
        // for simple wip (based on what is in progress at the end of the day cfd should be used
        SortedMap<ZonedDateTime, Integer> wip = new TreeMap<>();
        String[] wipStatuses = {"In Progress"};
        AtomicInteger counter = new AtomicInteger();
        AgileClientProvider.createClient().
                getAgileProject("AWW").getAllIssues().stream()
                .flatMap(i -> i.getIssueStatusTransitions().stream())
                .sorted()
                .filter(betweenStatuses(wipStatuses).negate())
                .forEach(st -> {
                    if (IssueStatusTransitionPredicates.to(wipStatuses).test(st)) {
                        wip.put(st.getDate(), counter.incrementAndGet());
                    } else if (IssueStatusTransitionPredicates.from(wipStatuses).test(st)) {
                        wip.put(st.getDate(), counter.decrementAndGet());
                    }
                });

        LocalDate prevDate = null;
        LocalDateTime prevDateTime = null;
        Duration totalDuration = Duration.ZERO;
        Map<Integer, Duration> wipPerTime = new HashMap<>();
        SortedMap<LocalDate, Double> avgWip = new TreeMap<>();
        for (Map.Entry<ZonedDateTime, Integer> entry : wip.entrySet()) {
            ZonedDateTime k = entry.getKey();
            Integer v = entry.getValue();

            LocalDate eventDate = LocalDate.from(k);
            LocalDateTime eventDateTime = LocalDateTime.from(k);
            if (prevDate == null) {
                prevDate = eventDate.minusDays(1);
            }
            if (!eventDate.isEqual(prevDate)) {
                prevDateTime = LocalDateTime.of(eventDate, LocalTime.of(8, 0));
                if (prevDateTime.isAfter(eventDateTime)) {
                    prevDateTime = eventDateTime;
                }
                if (!totalDuration.isZero()) {
                    Double avgDailyWip = 0.;
                    for (Map.Entry<Integer, Duration> entry1 : wipPerTime.entrySet()) {
                        avgDailyWip += entry1.getKey().doubleValue() * (double) entry1.getValue().getSeconds() / (double) totalDuration.getSeconds();
                    }
                    avgWip.put(prevDate, avgDailyWip);
                }
                prevDate = eventDate;

                totalDuration = Duration.ZERO;
                wipPerTime = new HashMap<>();
            }
            Duration duration = Duration.between(prevDateTime, eventDateTime);
            wipPerTime.merge(v, duration, Duration::plus);
            totalDuration = totalDuration.plus(duration);

        }
        Map<String, String> avgWipInString = new HashMap<>();
        avgWip.forEach((k, v) -> avgWipInString.put(k.toString(), v.toString()));
        assertThat(avgWipInString).containsOnly(
                entry("2018-06-22", "0.4370435058748809"),
                entry("2018-06-25", "0.529593886894371"),
                entry("2018-06-26", "1.3375"),
                entry("2018-06-27", "4.026055634727381"),
                entry("2018-06-28", "1.2180712423979148"),
                entry("2018-06-29", "3.311189537056259"),
                entry("2018-07-02", "3.254569161749431"),
                entry("2018-07-04", "2.0"),
                entry("2018-07-05", "1.1869448556624722"),
                entry("2018-07-09", "2.0"),
                entry("2018-07-11", "1.3984634491522976"),
                entry("2018-07-12", "0.12557687636628614"),
                entry("2018-07-13", "0.0"),
                entry("2018-07-16", "0.3869383816308574"),
                entry("2018-07-17", "0.0"),
                entry("2018-07-19", "0.104541255063606"),
                entry("2018-07-20", "0.7836494299454261"),
                entry("2018-07-23", "2.250470681857883"),
                entry("2018-07-24", "3.764238911290323"),
                entry("2018-07-25", "4.385228404724634"),
                entry("2018-07-26", "2.668551736325881"),
                entry("2018-07-27", "0.8504827162626438"),
                entry("2018-07-30", "1.0"),
                entry("2018-08-01", "2.0"),
                entry("2018-08-02", "1.4031678016680695"),
                entry("2018-08-03", "3.5465930653786444"),
                entry("2018-08-06", "4.523665546845838"),
                entry("2018-08-07", "5.306407524590517"),
                entry("2018-08-08", "5.426540865090063"),
                entry("2018-08-09", "5.0"),
                entry("2018-08-10", "5.459860489496391"),
                entry("2018-08-13", "2.0"),
                entry("2018-08-14", "1.5217391304347827"),
                entry("2018-08-15", "0.9949771364939465"),
                entry("2018-08-16", "0.39696808956140595"),
                entry("2018-08-17", "1.5277806589911145"),
                entry("2018-08-20", "1.3748664244496687"),
                entry("2018-08-21", "3.2192186065490156"),
                entry("2018-08-22", "4.345767897205793"),
                entry("2018-08-23", "4.258062082751858"),
                entry("2018-08-24", "4.0"),
                entry("2018-08-28", "1.9434148596402756"),
                entry("2018-08-29", "3.0"),
                entry("2018-08-30", "3.0"),
                entry("2018-08-31", "2.605402049053089"),
                entry("2018-09-03", "3.0"),
                entry("2018-09-04", "2.961245041196216"),
                entry("2018-09-05", "4.562495284086622"),
                entry("2018-09-06", "2.409033347403968"),
                entry("2018-09-07", "2.0"),
                entry("2018-09-10", "1.0"),
                entry("2018-09-11", "3.592604199310561"),
                entry("2018-09-13", "2.0"),
                entry("2018-09-14", "4.137525858502276"),
                entry("2018-09-17", "5.012963087776055"),
                entry("2018-09-18", "3.727249902996827"),
                entry("2018-09-19", "6.422991566799823"),
                entry("2018-09-20", "4.72077570720516"),
                entry("2018-09-21", "7.423842656379168"),
                entry("2018-09-24", "8.36575395138931"),
                entry("2018-09-25", "8.475576179427687"),
                entry("2018-09-26", "6.800548320767649"),
                entry("2018-09-27", "6.0"),
                entry("2018-09-28", "5.954526999593991"),
                entry("2018-10-01", "3.0"),
                entry("2018-10-03", "3.3786497137761415"),
                entry("2018-10-04", "4.106126393094119"),
                entry("2018-10-05", "5.388823929774349"),
                entry("2018-10-08", "5.0"),
                entry("2018-10-09", "7.647048936945012"),
                entry("2018-10-10", "5.612008160108801"),
                entry("2018-10-11", "3.0"),
                entry("2018-10-12", "2.0"),
                entry("2018-10-15", "2.0"),
                entry("2018-10-16", "3.3339295075395476"),
                entry("2018-10-17", "4.191642999016168"),
                entry("2018-10-18", "4.0"),
                entry("2018-10-19", "5.0"),
                entry("2018-10-22", "4.316127981763413"),
                entry("2018-10-23", "5.38758151403459"),
                entry("2018-10-24", "6.427600321613918"),
                entry("2018-10-25", "5.0"),
                entry("2018-10-26", "5.5336411861237265"),
                entry("2018-10-29", "6.0"),
                entry("2018-10-30", "7.284970604231955"),
                entry("2018-10-31", "8.310351579496997"),
                entry("2018-11-01", "7.639927985597119"),
                entry("2018-11-02", "5.408937580226276"),
                entry("2018-11-05", "4.673038750323638"),
                entry("2018-11-07", "5.0"),
                entry("2018-11-09", "4.488053815819995"),
                entry("2018-11-12", "6.0"),
                entry("2018-11-13", "7.0"),
                entry("2018-11-14", "7.477909465546434"),
                entry("2018-11-15", "4.734990562628007"),
                entry("2018-11-16", "5.515807736794756"),
                entry("2018-11-19", "5.232487095754767"),
                entry("2018-11-20", "8.0"),
                entry("2018-11-21", "5.84360424943859")
        );
    }
}
