/*
 * Copyright 2018 kaszaq.
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
package pl.kaszaq.howfastyouaregoing.agile.jira;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectDataObserver;
import pl.kaszaq.howfastyouaregoing.agile.pojo.AgileProjectData;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import pl.kaszaq.howfastyouaregoing.Config;
import static pl.kaszaq.howfastyouaregoing.Config.OBJECT_MAPPER;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileClientFactory;
import pl.kaszaq.howfastyouaregoing.agile.AgileProject;
import pl.kaszaq.howfastyouaregoing.agile.AgileProjectProvider;
import pl.kaszaq.howfastyouaregoing.agile.Issue;
import pl.kaszaq.howfastyouaregoing.clock.HFYAGClock;

public class JiraAgileProjectDataReaderIT {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort().usingFilesUnderClasspath("AWW_wiremock_mappings"));
    private File cacheDirectory;


    public JiraAgileProjectDataReaderIT() {
        //
    }

    @BeforeClass
    public static void setUpClass() {
        HFYAGClock.setClock(Clock.fixed(LocalDateTime.of(2018, 11, 25, 10, 0).atZone(ZoneId.of("Z")).toInstant(), ZoneId.of("Z")));
    }

    @AfterClass
    public static void tearDownClass() {
        HFYAGClock.setClock(Clock.systemDefaultZone());
    }

    @Before
    public void setUp() throws IOException {
         cacheDirectory = folder.newFolder("cache");

        
    }


    @Test
    public void shouldReadProjectFromExternal_whenNotLoadedBefore() throws Exception {
        // given
        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withCredentials("jira", "jira")
                .withCacheOnly(false)
                .withCacheDir(cacheDirectory)
                .withJiraUrl("http://localhost:" + wireMockRule.port() + "/")
                .build();

         AgileClient agileClient = AgileClientFactory.newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .create();
        // when
        AgileProject project = agileClient.getAgileProject("AWW");
        Map<String, Issue> issues = project.getAllIssues().stream().collect(Collectors.toMap(i -> i.getKey(), i->i));
        
        // then
        JsonNode result = OBJECT_MAPPER.valueToTree(issues);
        JsonNode expected = OBJECT_MAPPER.readTree(new File("src/test/resources/AWW_issues_sorted.json"));
        assertThatJson(result).isEqualTo(expected);
    }
    
        @Test
    public void shouldReadProjectFromFileAndExternal_whenLoadedBefore() throws Exception {
        // given
        FileUtils.copyDirectory(new File("src/test/resources/AWW_data_before_update"), cacheDirectory);
        AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withCredentials("jira", "jira")
                .withCacheOnly(false)
                .withCacheDir(cacheDirectory)
                .withJiraUrl("http://localhost:" + wireMockRule.port() + "/")
                .withMinutesUntilUpdate(0)
                .build();

         AgileClient agileClient = AgileClientFactory.newClient()
                .withAgileProjectProvider(agileProjectProvider)
                .create();
        // when
        AgileProject project = agileClient.getAgileProject("AWW");
        assertThat(project.getIssue("AWW-10")).isNotNull().extracting(Issue::getSummary).isEqualTo("Updated issue");
        assertThat(project.getIssue("AWW-136")).isNotNull().extracting(Issue::getSummary).isEqualTo("New issue");
    }



}
