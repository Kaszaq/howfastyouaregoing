package pl.kaszaq.agile.jira;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.agile.IssueData;
import pl.kaszaq.agile.IssueBlockedTransition;
import pl.kaszaq.agile.IssueStatusTransition;
import pl.kaszaq.json.JsonNodeOptional;
import static pl.kaszaq.Config.OBJECT_MAPPER;
import static pl.kaszaq.utils.DateUtils.parseDate;

@Slf4j
@AllArgsConstructor
class JiraIssueParser {

    private final Map<String, Function<JsonNodeOptional, Object>> customFieldsParsers;

    IssueData parseJiraIssue(String jsonIssue) throws IOException {
        JsonNode node = OBJECT_MAPPER.readTree(jsonIssue);
        return parseJiraIssue(node);
    }

    IssueData parseJiraIssue(JsonNode node) {
        JsonNodeOptional issueNode = JsonNodeOptional.of(node);

        String key = issueNode.get("key").asText();
        LOG.debug("Parsing issue {}", key);

        JsonNodeOptional fieldsNode = issueNode.get("fields");
        String creator = fieldsNode.get("creator").get("name").asText();
        ZonedDateTime created = parseDate(fieldsNode.get("created").asText());
        ZonedDateTime updated = parseDate(fieldsNode.get("updated").asText());
        String status = fieldsNode.get("status").get("name").asText();
        String summary = fieldsNode.get("summary").asText();
        String description = fieldsNode.get("description").asText();

        TreeSet<IssueStatusTransition> issueStatusTransitions = getIssueStatusTransitions(issueNode, status, creator, created);
        TreeSet<IssueBlockedTransition> issueBlockedTransitions = getIssueBlockedTransitions(issueNode, status, creator, created);

        JsonNodeOptional issueTypeNode = fieldsNode.get("issuetype");
        boolean subtask = issueTypeNode.get("subtask").asBoolean();
        String type = issueTypeNode.get("name").asText();

        List<String> linkedIssuesKeys = getLinkedIssuesKeys(fieldsNode);
        List<String> subtasksKeys = getSubtasksKeys(fieldsNode);
        String parentKey = fieldsNode.get("parent").get("key").asText();
        String resolution = fieldsNode.get("resolution").get("name").asText();
        List<String> labels = new ArrayList<>();
        fieldsNode.get("labels").elements().forEachRemaining(labelNode -> labels.add(labelNode.asText()));
        List<String> components = new ArrayList<>();
        fieldsNode.get("components").elements().forEachRemaining(componentNode -> components.add(componentNode.get("name").asText()));
        // TODO: this filed should not be a part of issue but rather additional, in some custom fields map.
        Map<String, Object> customFields = new HashMap<>();
        customFieldsParsers.forEach((k, v) -> {
            Object val = v.apply(fieldsNode);
            if (val != null) {
                customFields.put(k, val);
            }
        });

        IssueData issue = IssueData.builder()
                .created(created)
                .creator(creator)
                .summary(summary)
                .description(description)
                .issueStatusTransitions(issueStatusTransitions)
                .issueBlockedTransitions(issueBlockedTransitions)
                .key(key)
                .linkedIssuesKeys(linkedIssuesKeys)
                .parentIssueKey(parentKey)
                .resolution(resolution)
                .subtask(subtask)
                .subtaskKeys(subtasksKeys)
                .status(status)
                .updated(updated)
                .type(type)
                .labels(labels)
                .components(components)
                .customFields(customFields)
                .build();
        return issue;
    }

    private List<String> getSubtasksKeys(JsonNodeOptional fieldsNode) {
        List<String> subtasksKeys = new ArrayList<>();
        fieldsNode.get("subtasks").elements().forEachRemaining(subtaskNode -> {
            subtasksKeys.add(subtaskNode.get("key").asText());
        });
        return subtasksKeys;
    }

    private List<String> getLinkedIssuesKeys(JsonNodeOptional fieldsNode) {
        List<String> linkedIssuesKeys = new ArrayList<>();
        fieldsNode.get("issuelinks").elements().forEachRemaining(issueLinkNode -> {
            if (issueLinkNode.has("outwardIssue")) {
                linkedIssuesKeys.add(issueLinkNode.get("outwardIssue").get("key").asText());
            } else {
                linkedIssuesKeys.add(issueLinkNode.get("inwardIssue").get("key").asText());
            }
        });
        return linkedIssuesKeys;
    }

    private TreeSet<IssueStatusTransition> getIssueStatusTransitions(JsonNodeOptional issueNode, String status, String creator, ZonedDateTime created) {
        TreeSet<IssueStatusTransition> issueStatusTransitions = new TreeSet<>();
        JsonNodeOptional changelogNode = issueNode.get("changelog");
        int totalChangelogEntries = changelogNode.get("total").asInt();
        if (totalChangelogEntries > 0) {
            changelogNode.get("histories").elements().forEachRemaining(changelogEntry -> {
                String username = changelogEntry.get("author").get("name").asText();
                ZonedDateTime createdDate = parseDate(changelogEntry.get("created").asText());
                //LOG.debug("Parsed date of issue transition to {}", createdDate);
                changelogEntry.get("items").elements().forEachRemaining(changelogItem -> {
                    if ("status".equals(changelogItem.get("field").asText())) {
                        String fromStatus = changelogItem.get("fromString").asText();
                        String toStatus = changelogItem.get("toString").asText();
                        IssueStatusTransition issueStatusTransition = new IssueStatusTransition(username, createdDate, fromStatus, toStatus);
                        issueStatusTransitions.add(issueStatusTransition);
                    }
                });

            });
        }

        String initialStatus;
        if (!issueStatusTransitions.isEmpty()) {
            IssueStatusTransition firstTransition = issueStatusTransitions.first();
            initialStatus = firstTransition.getFromStatus();

        } else {
            initialStatus = status;
        }
        IssueStatusTransition issueStatusTransition = new IssueStatusTransition(creator, created, null, initialStatus);
        issueStatusTransitions.add(issueStatusTransition);
        return issueStatusTransitions;
    }

    private TreeSet<IssueBlockedTransition> getIssueBlockedTransitions(JsonNodeOptional issueNode, String status, String creator, ZonedDateTime created) {
        TreeSet<IssueBlockedTransition> issueBlockedTransitions = new TreeSet<>();
        JsonNodeOptional changelogNode = issueNode.get("changelog");
        int totalChangelogEntries = changelogNode.get("total").asInt();
        if (totalChangelogEntries > 0) {
            changelogNode.get("histories").elements().forEachRemaining(changelogEntry -> {
                String username = changelogEntry.get("author").get("name").asText();
                ZonedDateTime createdDate = parseDate(changelogEntry.get("created").asText());
                changelogEntry.get("items").elements().forEachRemaining(changelogItem -> {
                    if ("Flagged".equals(changelogItem.get("field").asText())) {
                        String fromStatus = changelogItem.get("fromString").asText();
                        String toStatus = changelogItem.get("toString").asText();
                        IssueBlockedTransition issueStatusTransition = new IssueBlockedTransition(username, createdDate, fromStatus, toStatus);
                        issueBlockedTransitions.add(issueStatusTransition);
                    }
                });

            });
        }

        return issueBlockedTransitions;
    }

}
