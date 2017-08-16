package pl.kaszaq.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonNodeOptional {

    private final Optional<JsonNode> optionalNode;

    private JsonNodeOptional(JsonNode node) {
        this.optionalNode = Optional.ofNullable(node);
    }

    private JsonNodeOptional(Optional<JsonNode> node) {
        this.optionalNode = node;
    }

    public static JsonNodeOptional of(JsonNode node) {
        return new JsonNodeOptional(node);
    }

    private static JsonNodeOptional of(Optional<JsonNode> node) {
        return new JsonNodeOptional(node);
    }

    public JsonNodeOptional get(String key) {
        Optional<JsonNode> optional = optionalNode.map(value -> value.get(key));
        return of(optional);
    }

    public String asText() {
        return optionalNode.filter(node -> !node.isNull()).map(JsonNode::asText).orElse(null);
    }

    public boolean asBoolean() {
        return optionalNode.filter(node -> !node.isNull()).map(JsonNode::asBoolean).orElse(false);
    }

    public Iterator<JsonNodeOptional> elements() {
        return optionalNode.map(JsonNode::elements).map((Iterator<JsonNode> iterator) -> {
            Iterable<JsonNode> iterable = () -> iterator;
            return StreamSupport.stream(iterable.spliterator(), false).map(jsonNode -> of(jsonNode)).collect(Collectors.toList());
        }).orElse(new ArrayList<>()).iterator();
    }

    public int asInt() {
        return optionalNode.filter(node -> !node.isNull()).map(JsonNode::asInt).orElse(0);
    }

    public boolean has(String fieldName) {
        return get(fieldName).isPresent();
    }

    private boolean isPresent() {
        return optionalNode.isPresent();
    }

}
