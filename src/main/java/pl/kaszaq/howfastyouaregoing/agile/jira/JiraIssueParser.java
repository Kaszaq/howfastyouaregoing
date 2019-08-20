package pl.kaszaq.howfastyouaregoing.agile.jira;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.howfastyouaregoing.agile.IssueData;
import pl.kaszaq.howfastyouaregoing.agile.IssueBlockedTransition;
import pl.kaszaq.howfastyouaregoing.agile.IssueStatusTransition;
import static pl.kaszaq.howfastyouaregoing.Config.OBJECT_MAPPER;
import static pl.kaszaq.howfastyouaregoing.utils.DateUtils.parseDate;

@Slf4j
@AllArgsConstructor
class JiraIssueParser {

    private final Map<String, Function<JsonNode, Object>> customFieldsParsers;

    IssueData parseJiraIssue(String jsonIssue, boolean emptyDescriptionAndSummary) throws IOException {
        JsonNode node = OBJECT_MAPPER.readTree(jsonIssue);
        return parseJiraIssue(node, emptyDescriptionAndSummary);
    }

    IssueData parseJiraIssue(JsonNode issueNode, boolean emptyDescriptionAndSummary) {

        String key = issueNode.path("key").asText(null);
        LOG.debug("Parsing issue {}", key);

        JsonNode fieldsNode = issueNode.path("fields");
        String creator = fieldsNode.path("creator").path("name").asText(null);
        ZonedDateTime created = parseDate(fieldsNode.path("created").asText(null));
        ZonedDateTime updated = parseDate(fieldsNode.path("updated").asText(null));
        String status = fieldsNode.path("status").path("name").asText(null);

        String summary;
        String description;
        if (emptyDescriptionAndSummary) {
            summary = "";
            description = "";
        } else {
            summary = fieldsNode.path("summary").asText(null);
            description = fieldsNode.path("description").asText(null);
        }

        List<IssueStatusTransition> issueStatusTransitions = getIssueStatusTransitions(issueNode, status, creator, created);
        List<IssueBlockedTransition> issueBlockedTransitions = getIssueBlockedTransitions(issueNode, status, creator, created);

        JsonNode issueTypeNode = fieldsNode.path("issuetype");
        boolean subtask = issueTypeNode.path("subtask").asBoolean(false);
        String type = issueTypeNode.path("name").asText(null);

        List<String> linkedIssuesKeys = getLinkedIssuesKeys(fieldsNode);
        List<String> subtasksKeys = getSubtasksKeys(fieldsNode);
        String parentKey = fieldsNode.path("parent").path("key").asText(null);
        String resolution = fieldsNode.path("resolution").path("name").asText(null);
        List<String> labels = new ArrayList<>();
        fieldsNode.path("labels").elements().forEachRemaining(labelNode -> labels.add(labelNode.asText(null)));
        List<String> components = new ArrayList<>();
        fieldsNode.path("components").elements().forEachRemaining(componentNode -> components.add(componentNode.path("name").asText(null)));
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

    private List<String> getSubtasksKeys(JsonNode fieldsNode) {
        List<String> subtasksKeys = new ArrayList<>();
        fieldsNode.path("subtasks").elements().forEachRemaining(subtaskNode -> {
            String key = subtaskNode.path("key").asText(null);
            if (key != null) {
                subtasksKeys.add(key);
            }
        });
        return subtasksKeys;
    }

    private List<String> getLinkedIssuesKeys(JsonNode fieldsNode) {
        List<String> linkedIssuesKeys = new ArrayList<>();
        fieldsNode.path("issuelinks").elements().forEachRemaining(issueLinkNode -> {
            if (issueLinkNode.has("outwardIssue")) {
                String key = issueLinkNode.path("outwardIssue").path("key").asText(null);
                if (key != null) { // TODO these checks (if key !=null) were added just because obfuscated data has null, it should not be possible with normal data. Wort considering either modifying example data or decdigint whether this is ok.
                    linkedIssuesKeys.add(key);
                }
            } else {
                String key = issueLinkNode.path("inwardIssue").path("key").asText(null);
                if (key != null) {
                    linkedIssuesKeys.add(key);
                }
            }
        });
        return linkedIssuesKeys;
    }

    private List<IssueStatusTransition> getIssueStatusTransitions(JsonNode issueNode, String status, String creator, ZonedDateTime created) {
        List<IssueStatusTransition> issueStatusTransitions = new ArrayList<>();
        JsonNode changelogNode = issueNode.path("changelog");

        int totalChangelogEntries = changelogNode.path("total").asInt(0);
        if (totalChangelogEntries > 0) {
            changelogNode.path("histories").elements().forEachRemaining(changelogEntry -> {
                String username = changelogEntry.path("author").path("name").asText(null);
                ZonedDateTime createdDate = parseDate(changelogEntry.path("created").asText(null));
                //LOG.debug("Parsed date of issue transition to {}", createdDate);
                changelogEntry.path("items").elements().forEachRemaining(changelogItem -> {
                    if ("status".equals(changelogItem.path("field").asText(null))) {
                        String fromStatus = changelogItem.path("fromString").asText(null);
                        String toStatus = changelogItem.path("toString").asText(null);
                        IssueStatusTransition issueStatusTransition = new IssueStatusTransition(username, createdDate, fromStatus, toStatus);
                        issueStatusTransitions.add(issueStatusTransition);
                    }
                });

            });
        }
        return issueStatusTransitions;
    }

    private List<IssueBlockedTransition> getIssueBlockedTransitions(JsonNode issueNode, String status, String creator, ZonedDateTime created) {
        List<IssueBlockedTransition> issueBlockedTransitions = new ArrayList<>();
        JsonNode changelogNode = issueNode.path("changelog");
        int totalChangelogEntries = changelogNode.path("total").asInt(0);
        if (totalChangelogEntries > 0) {
            changelogNode.path("histories").elements().forEachRemaining(changelogEntry -> {
                String username = changelogEntry.path("author").path("name").asText(null);
                ZonedDateTime createdDate = parseDate(changelogEntry.path("created").asText(null));
                changelogEntry.path("items").elements().forEachRemaining(changelogItem -> {
                    if ("Flagged".equals(changelogItem.path("field").asText(null))) {
                        String fromStatus = changelogItem.path("fromString").asText(null);
                        String toStatus = changelogItem.path("toString").asText(null);
                        IssueBlockedTransition issueStatusTransition = new IssueBlockedTransition(username, createdDate, fromStatus, toStatus);
                        issueBlockedTransitions.add(issueStatusTransition);
                    }
                });

            });
        }
        // TODO: either remove this transitions [prefered] or find out way to read initial status

        if (!issueBlockedTransitions.isEmpty()) {
            String initialStatus;
            IssueBlockedTransition firstTransition = issueBlockedTransitions.get(0);
            initialStatus = firstTransition.getFromStatus();
            if (initialStatus != null) {
                IssueBlockedTransition trans = new IssueBlockedTransition(creator, created, null, initialStatus);
                issueBlockedTransitions.add(trans);
            }
        }

        return issueBlockedTransitions;
    }

}
