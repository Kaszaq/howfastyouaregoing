# How fast are you going #
This is a library that helps analyze data from JIRA. Once connected to JIRA, it pulls all issue data for requested project to cache. This allows very quick data analysis without requirement to pull data from server each time you run your project. Moreover if any data was updated in JIRAit will be automatically synchronized with local cache - only issues that did change will be pulled from the server.

### Motivation
Initially I had problem with JIRA - as awesome as this tool is it does lack in terms of data analysis. For instance for some reason my CFD drawn by JIRA had a huge hole at some point in time.
> pic here

After I wrote this tool I understood why - JIRA does draw CFD using only currently used statuses, ignoring those that were removed. CFD drawn from data generated from this tool does not do that.
> pic here

Moreover it has ability to map statuses from one to another, meaning that if you had used in passed status `Resolved` you can map it to currently used `Closed` and while working with data you will no longer have to care about `Resolved` as you will only have `Closed`. So for instance CFD will look like this:
> pic here

And from that point I started to increase capabilities of this library to be able to get much more data than it is visible in JIRA - to make it easy to calculate wip, cycle time, throughput, wait time, flow efficiency etc. . To make it easy investigate how estimated values compare to time we actually spent or analyse any other data we put into JIRA custom fields.

### Features
1. Caching JIRA data locally
2. Easy to get any data regarding your project instantly
3. Some classes to help calculate data like `CFD`, `cycle time` etc. .
4. Examples with sample data. [soon]

### Installation
Library is available in [![Maven Central](https://maven-badges.herokuapp.com/maven-central/pl.kaszaq.agile/how-fast-you-are-going/badge.svg)](https://maven-badges.herokuapp.com/maven-central/pl.kaszaq.agile/how-fast-you-are-going)

```xml
<dependency>
    <groupId>pl.kaszaq.agile</groupId>
    <artifactId>how-fast-you-are-going</artifactId>
    <version>0.1</version>
</dependency>
```


### Sample code
This code will read all labels of project `MYAWESOMEPROJECT` from JIRA under url `http://localhost:8080/`
```java
// configure jira data provider
AgileProjectProvider agileProjectProvider = JiraAgileProjectProviderBuilderFactory
                .withJsession("cookievalue")
                .withCacheDir(new File("src/main/resources/cache/"))
                .withJiraUrl("http://localhost:8080/")
                .build();

// configure client
AgileClient agileClient = AgileClientFactory.getInstance().newClient()
        .withAgileProjectProvider(agileProjectProvider)
        .create()

// get set of all labels used in project MYAWESOMEPROJECT
Set<String> labels = 
	agileClient.getAgileProject("MYAWESOMEPROJECT").getAllIssues().stream()
	.flatMap(issue -> issue.getLabels().stream())
	.collect(Collectors.toSet())
```

### Examples

Examples will be added to project shortly with sample data.

### Code quality is...
I am quite ashamed of this code and its quality. Currently I consider it to be a far alpha stage as I am moving packages/interfaces quite often. Not to mention any tests...

However I was told by a friend of mine
> it doesn't matter, it **WORKS**, **does what we need it to do** I use it, its awesome. It is better that you share it now, than to keep it till it is perfect as others **can use it already** - and that what it is all about.

I want to make it better. But I agree with the above and and that is why it is here.
 
It is not perfect but it does what I wanted it to do so maybe other will find it useful as well.

### Contribution guidelines ###
If you think you can contribute something please create an issue with description of the problem so we could discuss it. You can also make just a merge request but then we might get a feature/code collision.

### Feedback

I'd really love to hear your feedback - if you can please give me one using [issue tracker](https://bitbucket.org/kaszaq/how-fast-are-you-going/issues)

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
