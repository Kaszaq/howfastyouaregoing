# CHANGELOG

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
