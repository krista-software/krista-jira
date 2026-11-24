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
        ecosystemVersion = "7339e9c9-76fd-4b24-bf95-4c5fdc3cef85")
@Entity(name = "Task", id = "localDomainEntity_1b51d92a-7a4b-412b-b7bd-29cd074b206f", primaryKey = "Task Key", supportStore = true, description = "Represents a task issue in Jira. Belongs to a Ticket Project (via Project Key). Parent Key references the parent issue (typically Epic.Epic Key). Related to User (Reporter Name and Assignee Name match User.Display Name). Priority references Ticket Priority.Name. Status references Ticket Status.Name. May have Subtasks where Subtask.Parent Key matches Task Key.")
public class Task {

    @Searchable
    @ToString
    @Field.Desc(name = "Comments", type = "[ Text ]", required = false, description = "List of comments added to the task")
    public List<String> comments;

    @Searchable
    @ToString
    @Field.Text(name = "Summary", description = "Brief summary of the task", required = false)
    public String summary;

    @Searchable
    @ToString
    @Field.Text(name = "Reporter Name", description = "Name of the person who reported the task. References User.Display Name.", required = false)
    public String reporterName;

    @Searchable
    @ToString
    @Field.Text(name = "Assignee Name", description = "Name of the person assigned to the task. References User.Display Name.", required = false)
    public String assigneeName;

    @Searchable
    @ToString
    @Field.Text(name = "Task Type", description = "Type classification of the task", required = false)
    public String taskType;

    @Searchable
    @ToString
    @Field.Text(name = "Task Key", description = "Unique Jira key identifier for the task. Referenced by Subtask.Parent Key for parent-child relationships.", required = false)
    public String taskKey;

    @Field.Text(name = "Id", description = "Internal Jira identifier for the task", required = false)
    public String id;

    @Field.Text(name = "Parent Key", description = "Jira key of the parent issue (typically an Epic). References Epic.Epic Key.", required = false)
    public String parentKey;

    @Field.Text(name = "Project Key", description = "Key of the Jira project this task belongs to. References Ticket Project.Key.")
    public String projectKey;

    @Searchable
    @Field.Text(name = "Priority", description = "Priority level of the task. References Ticket Priority.Name.", required = false)
    public String priority;

    @Searchable
    @ToString
    @Field.Text(name = "Status", description = "Current workflow status of the task. References Ticket Status.Name.", required = false)
    public String status;

    @Searchable
    @Field(name = "Description", type = "RichText")
    public String description;

    @Searchable
    @Field.Date(name = "Created Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the task was created", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long createdDate;

    @Searchable
    @Field.Date(name = "Last Modified Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the task was last modified", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long lastModifiedDate;

    public Map<String, Object> toFields() {
        Task task = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Comments", task.comments);
        fieldMap.put("Summary", task.summary);
        fieldMap.put("Reporter Name", task.reporterName);
        fieldMap.put("Assignee Name", task.assigneeName);
        fieldMap.put("Task Type", task.taskType);
        fieldMap.put("Task Key", task.taskKey);
        fieldMap.put("Id", task.id);
        fieldMap.put("Parent Key", task.parentKey);
        fieldMap.put("Project Key", task.projectKey);
        fieldMap.put("Priority", task.priority);
        fieldMap.put("Status", task.status);
        fieldMap.put("Description", task.description);
        fieldMap.put("Created Date", task.createdDate);
        fieldMap.put("Last Modified Date", task.lastModifiedDate);
        return fieldMap;
    }

}