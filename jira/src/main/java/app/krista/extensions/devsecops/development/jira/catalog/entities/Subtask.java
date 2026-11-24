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
        ecosystemVersion = "f376e5a4-a15d-4a3f-b274-cafb36d98bc0")
@Entity(name = "Subtask", id = "localDomainEntity_a021b0e2-d62c-41c6-83f7-3bdc278fae9c", primaryKey = "Key", supportStore = true, description = "Represents a subtask issue in Jira linked to a parent issue. Belongs to a Ticket Project (via Project Key). Parent Key references the parent issue key (Bug.Bug Key, Story.Story Key, Task.Task Key, or Epic.Epic Key). Related to User (Reporter Name and Assignee Name match User.Display Name). Priority references Ticket Priority.Name. Status references Ticket Status.Name.")
public class Subtask {

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false, description = "List of comments added to the subtask")
    public List<String> comments;

    @Searchable
    @ToString
    @Field.Text(name = "Summary", description = "Brief summary of the subtask")
    public String summary;

    @Field.Text(name = "Reporter Name", required = false, description = "Name of the person who reported the subtask. References User.Display Name.")
    public String reporterName;

    @Field.Text(name = "Assignee Name", required = false, description = "Name of the person assigned to the subtask. References User.Display Name.")
    public String assigneeName;

    @Field.Text(name = "Task Type", required = false, description = "Type classification of the subtask")
    public String taskType;

    @Searchable
    @ToString
    @Field.Text(name = "Key", required = false, description = "Unique Jira key identifier for the subtask")
    public String key;

    @Field.Text(name = "Id", required = false, description = "Internal Jira identifier for the subtask")
    public String id;

    @Field.Text(name = "Parent Key", description = "Jira key of the parent issue for this subtask. References Bug.Bug Key, Story.Story Key, Task.Task Key, or Epic.Epic Key.")
    public String parentKey;

    @Field.Text(name = "Project Key", description = "Key of the Jira project this subtask belongs to. References Ticket Project.Key.")
    public String projectKey;

    @Field.Text(name = "Priority", required = false, description = "Priority level of the subtask. References Ticket Priority.Name.")
    public String priority;

    @ToString
    @Field.Text(name = "Status", required = false, description = "Current workflow status of the subtask. References Ticket Status.Name.")
    public String status;

    @Searchable
    @Field(name = "Description", type = "RichText")
    public String description;

    @Searchable
    @Field.Date(name = "Created Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the subtask was created", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long createdDate;

    @Searchable
    @Field.Date(name = "Last Modified Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the subtask was last modified", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long lastModifiedDate;

    public Map<String, Object> toFields() {
        Subtask subtask = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Comments", subtask.comments);
        fieldMap.put("Summary", subtask.summary);
        fieldMap.put("Reporter Name", subtask.reporterName);
        fieldMap.put("Assignee Name", subtask.assigneeName);
        fieldMap.put("Task Type", subtask.taskType);
        fieldMap.put("Key", subtask.key);
        fieldMap.put("Id", subtask.id);
        fieldMap.put("Parent Key", subtask.parentKey);
        fieldMap.put("Project Key", subtask.projectKey);
        fieldMap.put("Priority", subtask.priority);
        fieldMap.put("Status", subtask.status);
        fieldMap.put("Description", subtask.description);
        fieldMap.put("Created Date", subtask.createdDate);
        fieldMap.put("Last Modified Date", subtask.lastModifiedDate);
        return fieldMap;
    }
}
