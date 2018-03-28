package pl.kaszaq.howfastyouaregoing.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileProject;
import pl.kaszaq.howfastyouaregoing.cfd.CfdData;
import pl.kaszaq.howfastyouaregoing.cfd.CfdDataComputer;
import static pl.kaszaq.howfastyouaregoing.utils.CommonPredicates.alwaysTrue;
import static pl.kaszaq.howfastyouaregoing.utils.DateUtils.printSimpleDate;

public class CfdExample {

    public static void main(String[] args) {
        AgileClient agileClient = AgileClientProvider.createClient();
        runExample(agileClient);
    }

    private static void runExample(AgileClient agileClient) {
        AgileProject project = agileClient.getAgileProject("MYPROJECTID");
        CfdData data = CfdDataComputer.calculateCfdData(project, alwaysTrue());

        Map<String, Integer> statusValues = new HashMap<>();
        List<String> statusOrder = project.getProbableStatusOrder();
        statusOrder.forEach(status -> statusValues.put(status, 0));
        statusOrder.stream().map(o -> "\t" + o).forEach(System.out::print);
        System.out.println("");
        data.getDailyTransitions().forEach((k, v) -> {
            System.out.print(printSimpleDate(k) + "\t");
            statusOrder.forEach(status -> {
                Integer newValue = statusValues.merge(status, v.getValueChangeForStatus(status), Integer::sum);
                System.out.print(newValue + "\t");
            });
            System.out.println("");
        });

    }
}
