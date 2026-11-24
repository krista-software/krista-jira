/*
 * JIRA Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.devsecops.development.jira.catalog.entities;


import app.krista.extension.impl.anno.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Searchable
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "e69f2365-1bc2-47c8-b966-57c3b3604231")
@Entity(name = "Issue", id = "localDomainEntity_4735e2d8-bd37-4a7d-892f-dfd899ff2336", primaryKey = "Key", supportStore = true, description = "Represents a generic issue in Jira that can be any issue type. Belongs to a Ticket Project (via Project Key). Issue Type references Issue Type.Name. Parent Key references the parent issue key (Bug.Bug Key, Story.Story Key, Task.Task Key, or Epic.Epic Key). Related to User (Reporter Name and Assignee Name match User.Display Name). Priority references Ticket Priority.Name. Status references Ticket Status.Name. Labels reference Label.Name. Components reference Component.Name.")
public class Issue {

    @Searchable
    @ToString
    @Field.Text(name = "Summary", description = "Brief summary of the issue")
    public String summary;

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false, description = "List of comments added to the issue")
    public List<String> comments;

    @Field.Text(name = "Reporter Name", required = false, description = "Name of the person who reported the issue. References User.Display Name.")
    public String reporterName;

    @ToString
    @Field.Text(name = "Assignee Name", required = false, description = "Name of the person assigned to the issue. References User.Display Name.")
    public String assigneeName;

    @Searchable
    @ToString
    @Field.Text(name = "Key", required = false, description = "Unique Jira key identifier for the issue")
    public String key;

    @Field.Text(name = "Id", required = false, description = "Internal Jira identifier for the issue")
    public String id;

    @Field.Text(name = "Parent Key", required = false, description = "Jira key of the parent issue. References Bug.Bug Key, Story.Story Key, Task.Task Key, or Epic.Epic Key.")
    public String parentKey;

    @Field.Text(name = "Project Key", description = "Key of the Jira project this issue belongs to. References Ticket Project.Key.")
    public String projectKey;

    @Searchable
    @Field(name = "Description", type = "RichText", required = false)
    public String description;

    @Searchable
    @Field.Text(name = "Issue Type", required = false, description = "Type of the Jira issue. References Issue Type.Name.")
    public String issueType;

    @Searchable
    @Field.Text(name = "Priority", required = false, description = "Priority level of the issue. References Ticket Priority.Name.")
    public String priority;

    @Searchable
    @ToString
    @Field.Text(name = "Status", required = false, description = "Current workflow status of the issue. References Ticket Status.Name.")
    public String status;

    @Searchable
    @Field.Desc(name = "Labels", type = "[ Text ]", required = false, description = "Labels assigned to the issue. Each label references Label.Name.")
    public List<String> labels;

    @Searchable
    @Field.Desc(name = "Components", type = "[ Text ]", required = false, description = "Components associated with the issue. Each component references Component.Name.")
    public List<String> components;

    @Searchable
    @Field.Date(name = "Created Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the issue was created", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long createdDate;

    @Searchable
    @Field.Date(name = "Last Modified Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the issue was last modified", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long lastModifiedDate;


    public Map<String, Object> toFields() {
        Issue issue = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Summary", issue.summary);
        fieldMap.put("Comments", issue.comments);
        fieldMap.put("Reporter Name", issue.reporterName);
        fieldMap.put("Assignee Name", issue.assigneeName);
        fieldMap.put("Key", issue.key);
        fieldMap.put("Id", issue.id);
        fieldMap.put("Parent Key", issue.parentKey);
        fieldMap.put("Project Key", issue.projectKey);
        fieldMap.put("Description", issue.description);
        fieldMap.put("Issue Type", issue.issueType);
        fieldMap.put("Priority", issue.priority);
        fieldMap.put("Status", issue.status);
        fieldMap.put("Labels", issue.labels);
        fieldMap.put("Components", issue.components);
        fieldMap.put("Created Date", issue.createdDate);
        fieldMap.put("Last Modified Date", issue.lastModifiedDate);
        return fieldMap;
    }

}
