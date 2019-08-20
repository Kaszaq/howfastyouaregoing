package pl.kaszaq.howfastyouaregoing.agile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IssueStatusMapping {

    private final Map<String, String> statusMap;

    public IssueStatusMapping(Map<String, String> statusMap) {
        
        this.statusMap = shortenLinks(removeLoopsFromGraph(statusMap));
    }

    private Map<String, String> shortenLinks(Map<String, String> map) {
        Map<String, String> map2 = new HashMap<>();
        map.forEach((k, v) -> {
            map2.put(k, getParent(map, v));
        });
        return map2;
    }

    private Map<String, String> removeLoopsFromGraph(Map<String, String> statusMap1) {
        // TODO: I have no idea if this works correctly... should be tested
        Map<String, String> map = new HashMap<>();
        statusMap1.forEach((k, v) -> {
            putToMapIfWontCreateChain(map, k, v);
        });
        return map;
    }

    private static void putToMapIfWontCreateChain(Map<String, String> map, final String k, String v) {
        if (getInvalidChainConnection(map, k, v) == null) {
            map.put(k, v);
        }
    }

    private static String getParent(Map<String, String> map, String child) {
        String parent = map
                .get(child);
        if (parent == null) {
            return child;
        } else {
            return getParent(map, parent);
        }
    }
    
    private static String getInvalidChainConnection(Map<String, String> statusMap, String childStatus, String parentStatus) {
        String val = statusMap
                .get(parentStatus);
        if (val == null) {
            return null;
        }
        if (val.equals(childStatus)) {
            return parentStatus;
        } else {
            return getInvalidChainConnection(statusMap, childStatus, val);
        }
    }

    public String mapStatus(String oldStatus) {
        return oldStatus == null ? null : statusMap
                .getOrDefault(oldStatus, oldStatus);
    }

    public Map<String, String> getMappings() {
        return Collections.unmodifiableMap(statusMap);
    }
}
