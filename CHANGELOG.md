# CHANGELOG

- removed examples project as examples were moved to tests of core project
- !SUBSTRACTED one day when loading data from Jira. This is to avoid zone problems and should be resolved in future by actually reading zone which given user has setup in Jira. This is required for jql queries to work as expected and actually pull updated issues.
- added test for updating project
- added tests. These are basically moved examples to main project - hence they servethe purpose of examples as well and are not formatted in a test correct way ;).

### 0.6
- removed JsonNodeOptional -> should JsonNode with `path()` method be used instead of `get()` from JsonNodeOptional for accessing custom fields
- reading all issue data now so those who would want could easily access comments or anything else like this
- changed custom fields. Now it is required to provide TypeReference (instead of casting) as a parametr to `get()` method that is available directly on issue - it is no longer possible to acces custom field via getCustomFields(). This also allows to retrive Object and store for later where in the past only simple object were working correctly. However this might now work a bit slower.

### 0.5
* fixed issue when most recent status tranition trainsitioned to status that was renamed / replaced
* changed fixing of tranitions that are to statuses that are no longer used - now the time of this tranition is taken from the trantion where last currently used status was used.

### Current (xx-xx-xx)
* added example for throughput per type
* added autocloseable to HttpClient
* changed cycle time calculator - removed calulcateCycleTimeOfStories and replaced calulcateCycleTimeOfAllIssues to return map LocalDate to Double to increase efficiency for instance for drawing graphs
* modified cfd data calculator to accept agile project as argument
* changed cfddatafactory to cfddatacomputer and changed it to be used as utilityclass
* changed cycletimecomputer class to utility class
* parametrized time required to call jira for update in JiraAgileProjectProvider class
* small tweaks to JiraAgileProjectProvider regarding reading data
* modified a little bit IssueStatusMapping class in case if provided mapping had a loop
* added more predicates, some just with alternative paramerers now based on both collections and arrays
* AgileProjectFactory -> when there is incorrect data with status transitions for given issue, they are automatically fixed dynamically following we have two transitions A->B and then Z->D it is changed to A->B and B->D
* updated examples
* Changed AgileClientFactory from singleton to UtilityClass
* Not caching Optionals for projects anymore - didnt work good in CLI
* added ability to observe / listen for changes when loading tasks from Jira in interface AgileProjectProvider
* moved move cacheonly option from Config to JiraAgileProjectProviderBuilderFactory
* some refactoring in area of loading tasks
* added calculation of proable status order (flow) in AgileProject class
* added FileStorage so now one can add storage with for instance encryption
* fixed examples with cacheOnly
* toString on issue now returns prettyname
* agile project has methods to retrive first issue start date
* some exception handling
* added reading from Jira currently used statuses in project. Based on that the number of statuses is narrowed or mapped only to those currently used
* fixed error with custom fields
* Removed few methods from issue wrapper, including getDatesInStatus, isStatusOnDay and especially getWorkTimeInStatus
* Added ability to not store raw jira files
* Added ability to not store description and summaries of issues
* Fixes required to use proxy
* refactored StatusOrderCalculator to eliminate null being returned in some cases
* fixed blocked to include transitions to empty string ""


### License
Copyright 2017 Michał Kasza

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
