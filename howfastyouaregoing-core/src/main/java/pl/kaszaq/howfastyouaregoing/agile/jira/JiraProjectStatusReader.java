/*
 * Copyright 2018 Michał Kasza <kaszaq@gmail.com>.
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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import static pl.kaszaq.howfastyouaregoing.Config.OBJECT_MAPPER;
import pl.kaszaq.howfastyouaregoing.agile.pojo.AgileProjectStatuses;
import pl.kaszaq.howfastyouaregoing.http.HttpClient;
import pl.kaszaq.howfastyouaregoing.json.JsonNodeOptional;
import pl.kaszaq.howfastyouaregoing.storage.FileStorage;

public class JiraProjectStatusReader {

    private final HttpClient httpClient;
    private final String jiraUrl;
    private final File jiraCacheIssuesDirectory;

    private final FileStorage fileStorage;

    public JiraProjectStatusReader(HttpClient httpClient, String jiraUrl, File jiraCacheIssuesDirectory, FileStorage fileStorage) {
        this.httpClient = httpClient;
        this.jiraUrl = jiraUrl;
        this.jiraCacheIssuesDirectory = jiraCacheIssuesDirectory;
        this.fileStorage = fileStorage;
    }

 

    boolean areStatusesCached(String projectKey) {
        return new File(jiraCacheIssuesDirectory, projectKey + "-STATUSES" + ".json").exists();
    }

    AgileProjectStatuses getProjectStatuses(String projectKey, boolean cache) throws IOException {
        String url = jiraUrl + "/rest/api/2/project/" + projectKey + "/statuses";
        File file = new File(jiraCacheIssuesDirectory, projectKey + "-STATUSES" + ".json");
        String response;
        if (cache) {
            response = fileStorage.loadFile(file);
        } else {
            response = httpClient.get(url);
            fileStorage.storeFile(file, response);
        }

        Set<String> indeterminateStatuses = new HashSet<>();
        Set<String> newStatuses = new HashSet<>();
        Set<String> doneStatuses = new HashSet<>();
        Set<String> undefinedStatuses = new HashSet<>();

        JsonNodeOptional tree = JsonNodeOptional.of(OBJECT_MAPPER.readTree(response));
        Iterator<JsonNodeOptional> it = tree.elements();
        while (it.hasNext()) {
            JsonNodeOptional val = it.next();
            Iterator<JsonNodeOptional> it2 = val.get("statuses").elements();
            while (it2.hasNext()) {
                JsonNodeOptional val2 = it2.next();
                String name = val2.get("name").asText();
                String category = val2.get("statusCategory").get("key").asText();
                if ("new".equals(category)) {
                    newStatuses.add(name);
                }
                if ("done".equals(category)) {
                    doneStatuses.add(name);
                }
                if ("indeterminate".equals(category)) {
                    indeterminateStatuses.add(name);
                }
                if ("undefined".equals(category)) {
                    undefinedStatuses.add(name);
                }
            }
        }

        return new AgileProjectStatuses(indeterminateStatuses, newStatuses, doneStatuses, undefinedStatuses);
    }

}
