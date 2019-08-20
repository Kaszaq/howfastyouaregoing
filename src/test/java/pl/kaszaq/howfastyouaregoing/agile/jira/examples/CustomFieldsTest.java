package pl.kaszaq.howfastyouaregoing.agile.jira.examples;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import org.junit.Test;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileClientFactory;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import pl.kaszaq.howfastyouaregoing.agile.jira.JiraAgileProjectProviderBuilderFactory;

/**
 * This examples does show use of custom fields from jira. In this example -
 * field with priority name. Please note that once you add custom fields library
 * may need to rebuild cache.
 */
public class CustomFieldsTest {

    @Test
    public void customFieldsTest() throws IOException {

        File cacheDirectory = Files.createTempDir();
        cacheDirectory.deleteOnExit();
        FileUtils.copyDirectory(new File("src/test/resources/AWW_data_before_update"), cacheDirectory);
        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("cookievalue")
                .withCacheDir(cacheDirectory)
                .withJiraUrl("http://localhost:8080/")
                .withCacheOnly(true)
                .withCustomFieldsParsers(ImmutableMap.of(
                        "priority", fieldsNode -> fieldsNode.get("priority").get("name").asText()
                ))
                .build();

        AgileClient agileClient = AgileClientFactory.newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .create();
        TypeReference<String> tr = new TypeReference<String>() {
        };

        //when
        Map<String, Object> awwMap = agileClient.getAgileProject("AWW").getAllIssues().stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.get("priority", tr)));
        //then
        assertThat(awwMap).containsOnly(
                entry("AWW-38", "Minor"),
                entry("AWW-37", "Major"),
                entry("AWW-39", "Minor"),
                entry("AWW-34", "Minor"),
                entry("AWW-33", "Minor"),
                entry("AWW-36", "Minor"),
                entry("AWW-35", "Major"),
                entry("AWW-30", "Minor"),
                entry("AWW-32", "Major"),
                entry("AWW-31", "Major"),
                entry("AWW-49", "Minor"),
                entry("AWW-48", "Minor"),
                entry("AWW-45", "Major"),
                entry("AWW-44", "Minor"),
                entry("AWW-47", "Minor"),
                entry("AWW-46", "Minor"),
                entry("AWW-41", "Major"),
                entry("AWW-40", "Major"),
                entry("AWW-43", "Major"),
                entry("AWW-42", "Minor"),
                entry("AWW-19", "Major"),
                entry("AWW-16", "Minor"),
                entry("AWW-15", "Major"),
                entry("AWW-18", "Minor"),
                entry("AWW-17", "Major"),
                entry("AWW-12", "Minor"),
                entry("AWW-11", "Major"),
                entry("AWW-99", "Minor"),
                entry("AWW-14", "Minor"),
                entry("AWW-13", "Major"),
                entry("AWW-96", "Minor"),
                entry("AWW-95", "Major"),
                entry("AWW-10", "Major"),
                entry("AWW-98", "Major"),
                entry("AWW-97", "Major"),
                entry("AWW-92", "Major"),
                entry("AWW-91", "Minor"),
                entry("AWW-94", "Minor"),
                entry("AWW-93", "Major"),
                entry("AWW-90", "Minor"),
                entry("AWW-27", "Minor"),
                entry("AWW-26", "Minor"),
                entry("AWW-29", "Minor"),
                entry("AWW-28", "Major"),
                entry("AWW-23", "Major"),
                entry("AWW-22", "Minor"),
                entry("AWW-25", "Minor"),
                entry("AWW-24", "Minor"),
                entry("AWW-21", "Minor"),
                entry("AWW-20", "Major"),
                entry("AWW-126", "Minor"),
                entry("AWW-125", "Major"),
                entry("AWW-128", "Major"),
                entry("AWW-127", "Minor"),
                entry("AWW-129", "Major"),
                entry("AWW-120", "Major"),
                entry("AWW-122", "Minor"),
                entry("AWW-121", "Major"),
                entry("AWW-124", "Minor"),
                entry("AWW-123", "Minor"),
                entry("AWW-78", "Major"),
                entry("AWW-77", "Major"),
                entry("AWW-79", "Major"),
                entry("AWW-74", "Major"),
                entry("AWW-73", "Major"),
                entry("AWW-76", "Minor"),
                entry("AWW-75", "Major"),
                entry("AWW-70", "Major"),
                entry("AWW-72", "Minor"),
                entry("AWW-71", "Major"),
                entry("AWW-131", "Minor"),
                entry("AWW-130", "Major"),
                entry("AWW-133", "Major"),
                entry("AWW-132", "Minor"),
                entry("AWW-135", "Minor"),
                entry("AWW-134", "Minor"),
                entry("AWW-89", "Major"),
                entry("AWW-88", "Minor"),
                entry("AWW-85", "Major"),
                entry("AWW-84", "Minor"),
                entry("AWW-87", "Minor"),
                entry("AWW-86", "Major"),
                entry("AWW-81", "Major"),
                entry("AWW-80", "Major"),
                entry("AWW-83", "Minor"),
                entry("AWW-82", "Major"),
                entry("AWW-104", "Minor"),
                entry("AWW-103", "Major"),
                entry("AWW-106", "Minor"),
                entry("AWW-105", "Minor"),
                entry("AWW-108", "Major"),
                entry("AWW-107", "Minor"),
                entry("AWW-109", "Major"),
                entry("AWW-100", "Minor"),
                entry("AWW-59", "Minor"),
                entry("AWW-102", "Minor"),
                entry("AWW-101", "Minor"),
                entry("AWW-56", "Major"),
                entry("AWW-55", "Minor"),
                entry("AWW-58", "Major"),
                entry("AWW-57", "Minor"),
                entry("AWW-52", "Major"),
                entry("AWW-51", "Minor"),
                entry("AWW-54", "Minor"),
                entry("AWW-53", "Major"),
                entry("AWW-50", "Major"),
                entry("AWW-115", "Minor"),
                entry("AWW-114", "Minor"),
                entry("AWW-117", "Minor"),
                entry("AWW-116", "Minor"),
                entry("AWW-119", "Major"),
                entry("AWW-118", "Minor"),
                entry("AWW-111", "Minor"),
                entry("AWW-110", "Major"),
                entry("AWW-113", "Major"),
                entry("AWW-112", "Major"),
                entry("AWW-67", "Major"),
                entry("AWW-8", "Major"),
                entry("AWW-66", "Minor"),
                entry("AWW-9", "Minor"),
                entry("AWW-69", "Major"),
                entry("AWW-68", "Major"),
                entry("AWW-4", "Minor"),
                entry("AWW-63", "Minor"),
                entry("AWW-5", "Major"),
                entry("AWW-62", "Major"),
                entry("AWW-6", "Major"),
                entry("AWW-65", "Minor"),
                entry("AWW-64", "Major"),
                entry("AWW-7", "Minor"),
                entry("AWW-1", "Minor"),
                entry("AWW-2", "Minor"),
                entry("AWW-61", "Minor"),
                entry("AWW-3", "Major"),
                entry("AWW-60", "Minor"));
    }

}
