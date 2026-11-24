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


import java.util.LinkedHashMap;

import java.util.Map;
import java.util.List;

import app.krista.extension.impl.anno.*;
@Searchable
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion ="12c57bb9-5b53-4ff7-b51f-aecbbfc7173c")
@Entity(name = "Bug", id = "localDomainEntity_65413b5b-acbb-49d3-8a6b-5de7dacce1ef", primaryKey = "Bug Key", description = "Represents a bug issue tracked in Jira. Belongs to a Ticket Project (via Project Key). Parent Key references the parent issue (typically Epic.Epic Key). Related to User (Reporter Name and Assignee Name match User.Display Name). Priority references Ticket Priority.Name. Status references Ticket Status.Name. May have Subtasks where Subtask.Parent Key matches Bug Key.", supportStore = true)
public class Bug {

    @Field.Desc(name = "Comments", description = "List of comments added to the bug", type = "[ Text ]", required = false)
    public List<String> comments;

    @ToString
    @Searchable
    @Field.Text(name = "Summary", description = "Brief summary of the bug")
    public String summary;

    @Field.Text(name = "Reporter Name", description = "Name of the person who reported the bug. References User.Display Name.", required = false)
    public String reporterName;

    @Field.Text(name = "Assignee Name", description = "Name of the person assigned to fix the bug. References User.Display Name.", required = false)
    public String assigneeName;

    @Field.Text(name = "Type", description = "Type classification of the bug", required = false)
    public String type;
    @Searchable
    @ToString
    @Field.Text(name = "Bug Key", description = "Unique Jira key identifier for the bug. Referenced by Subtask.Parent Key for parent-child relationships.", required = false)
    public String bugKey;

    @Searchable
    @Field.Text(name = "Id", description = "Internal Jira identifier for the bug", required = false)
    public String id;

    @Field.Text(name = "Parent Key", description = "Jira key of the parent issue (typically an Epic). References Epic.Epic Key, Story.Story Key, or Task.Task Key.", required = false)
    public String parentKey;

    @Field.Text(name = "Project Key", description = "Key of the Jira project this bug belongs to. References Ticket Project.Key.")
    public String projectKey;

    @Field.Text(name = "Priority", description = "Priority level of the bug. References Ticket Priority.Name.", required = false)
    public String priority;

    @Searchable
    @ToString
    @Field.Text(name = "Status", description = "Current workflow status of the bug. References Ticket Status.Name.", required = false)
    public String status;

    @Searchable
    @Field(name = "Description", type = "RichText")
    public String description;

    @Searchable
    @Field.Date(name = "Created Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the bug was created", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long createdDate;

    @Searchable
    @Field.Date(name = "Last Modified Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the bug was last modified", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long lastModifiedDate;


    public Map<String, Object> toFields() {
        Bug bug = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Summary", bug.summary);
        fieldMap.put("Comments", bug.comments);
        fieldMap.put("Reporter Name", bug.reporterName);
        fieldMap.put("Assignee Name", bug.assigneeName);
        fieldMap.put("Bug Key", bug.bugKey);
        fieldMap.put("Id", bug.id);
        fieldMap.put("Parent Key", bug.parentKey);
        fieldMap.put("Project Key", bug.projectKey);
        fieldMap.put("Description", bug.description);
        fieldMap.put("Type", bug.type);
        fieldMap.put("Priority", bug.priority);
        fieldMap.put("Status", bug.status);
        fieldMap.put("Created Date", bug.createdDate);
        fieldMap.put("Last Modified Date", bug.lastModifiedDate);
        return fieldMap;
    }
}