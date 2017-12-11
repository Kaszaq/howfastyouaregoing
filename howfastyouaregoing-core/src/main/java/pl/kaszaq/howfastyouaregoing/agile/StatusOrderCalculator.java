/*
 * Copyright 2017 Michał Kasza <kaszaq@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.kaszaq.howfastyouaregoing.agile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;

public class StatusOrderCalculator {

    static List<String> getStatusOrder(Collection<Issue> issues) {
        Map<Optional<String>, Map<String, Long>> recentTransitions = issues.stream()
                .filter(IssuePredicates.updatedAfter(LocalDate.now().minusMonths(6).atStartOfDay(ZoneId.systemDefault())))
                .flatMap(i -> i.getIssueStatusTransitions().stream())
                .collect(Collectors.groupingBy(i -> Optional.ofNullable(i.getFromStatus()), Collectors.groupingBy(i -> i.getToStatus(), Collectors.counting())));
        Map<Optional<String>, Map<String, Long>> transitions = issues.stream()
                //.filter(IssuePredicates.createdAfter(LocalDate.now().minusMonths(6).atStartOfDay(ZoneId.systemDefault())))
                .flatMap(i -> i.getIssueStatusTransitions().stream())
                //.filter(i -> i.getFromStatus()!=null)
                .collect(Collectors.groupingBy(i -> Optional.ofNullable(i.getFromStatus()), Collectors.groupingBy(i -> i.getToStatus(), Collectors.counting())));
        System.out.println(transitions);

        Map<String, String> mapping;
        if (recentTransitions.isEmpty()) {
            mapping = calculateMapping(transitions);
        } else {
            mapping = calculateMapping(recentTransitions);
        }

        System.out.println(mapping);
        String tempStatus = null;
        LinkedList<String> statusOrder = new LinkedList<>();
        do {
            tempStatus = mapping.get(tempStatus);
            if (tempStatus != null && !statusOrder.contains(tempStatus)) {
                statusOrder.add(tempStatus);
            } else {
                tempStatus = null;
            }
        } while (tempStatus != null);
        while (statusOrder.size() < mapping.size()) {
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                if (!statusOrder.contains(entry.getKey()) && statusOrder.contains(entry.getValue())) {
                    int pos = statusOrder.indexOf(entry.getValue());
                    statusOrder.add(pos, entry.getKey());
                }
            }
        }

        List<String> statuses = transitions.values().stream().flatMap(map -> map.keySet().stream()).distinct().collect(toList());
        for (String status : statuses) {
            if (!statusOrder.contains(status)) {
                String highestFrom = null;
                long val = 0L;
                for (Map.Entry<Optional<String>, Map<String, Long>> entry : transitions.entrySet()) {
                    Long tempVal = entry.getValue().get(status);
                    if (tempVal != null && tempVal > val) {
                        val = tempVal;
                        highestFrom = entry.getKey().orElse(null);
                    }
                }
                statusOrder.add(statusOrder.indexOf(highestFrom) + 1, status);
            }
        }
        statusOrder.removeFirst();
        return statusOrder;
    }

    private static Map<String, String> calculateMapping(Map<Optional<String>, Map<String, Long>> transitions) {
        Map<String, String> mapping = new HashMap<>();
        transitions.forEach((k, v) -> {
            String highestTo = null;
            Long val = 0l;
            for (Map.Entry<String, Long> entry : v.entrySet()) {
                if (val < entry.getValue()) {
                    val = entry.getValue();
                    highestTo = entry.getKey();
                }
            }
            mapping.put(k.orElse(null), highestTo);
        });
        return mapping;
    }
}
