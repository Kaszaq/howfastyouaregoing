/*
 * Copyright 2017 michal.kasza.
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
package pl.kaszaq.howfastyouaregoing.agile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class Issue {

    private final IssueData issueData;
    private final IssueDataWrapper issueDataWrapper;

    Issue(IssueData issueData) {
        this.issueData = issueData;
        this.issueDataWrapper = new IssueDataWrapper(issueData);
    }

    public Duration getDurationInStatuses(String... statuses) {
        return issueDataWrapper.getDurationInStatuses(statuses);
    }

    public boolean isStatusOnDay(LocalDate date, Set<String> statuses) {
        return issueDataWrapper.isStatusOnDay(date, statuses);
    }

    public Map<String, Duration> getTimeInStatus() {
        return issueDataWrapper.getTimeInStatus();
    }

    public Map<String, Duration> getWorkTimeInStatus() {
        return issueDataWrapper.getWorkTimeInStatus();
    }

    public Set<LocalDate> getAllDayBlockedDays() {
        return issueDataWrapper.getAllDayBlockedDays();
    }

    public String getPrettyName() {
        return issueData.getPrettyName();
    }

    public String getKey() {
        return issueData.getKey();
    }

    public String getCreator() {
        return issueData.getCreator();
    }

    public String getType() {
        return issueData.getType();
    }

    public String getResolution() {
        return issueData.getResolution();
    }

    public String getStatus() {
        return issueData.getStatus();
    }

    public String getSummary() {
        return issueData.getSummary();
    }

    public String getDescription() {
        return issueData.getDescription();
    }

    public ZonedDateTime getCreated() {
        return issueData.getCreated();
    }

    public ZonedDateTime getUpdated() {
        return issueData.getUpdated();
    }

    public boolean isSubtask() {
        return issueData.isSubtask();
    }

    public String getParentIssueKey() {
        return issueData.getParentIssueKey();
    }

    public List<String> getSubtaskKeys() {
        return issueData.getSubtaskKeys();
    }

    public List<String> getLinkedIssuesKeys() {
        return issueData.getLinkedIssuesKeys();
    }

    public List<String> getLabels() {
        return issueData.getLabels();
    }

    public List<String> getComponents() {
        return issueData.getComponents();
    }

    public TreeSet<IssueStatusTransition> getIssueStatusTransitions() {
        return issueData.getIssueStatusTransitions();
    }

    public TreeSet<IssueBlockedTransition> getIssueBlockedTransitions() {
        return issueData.getIssueBlockedTransitions();
    }

    public Map<String, Object> getCustomFields() {
        return issueData.getCustomFields();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.issueData);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Issue other = (Issue) obj;
        return Objects.equals(this.issueData, other.issueData);
    }

}
