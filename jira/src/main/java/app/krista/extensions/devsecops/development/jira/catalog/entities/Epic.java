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
        ecosystemVersion = "3a0013af-cb88-411e-83f4-f02ad1ea5aa2")
@Entity(name = "Epic", id = "localDomainEntity_1d6d49bd-74d1-4c71-97e9-48cdc85034f4", primaryKey = "Epic Key", supportStore = true, description = "Represents an epic issue in Jira for tracking large bodies of work. Belongs to a Ticket Project (via Project Key). Related to User (Reporter Name and Assignee Name match User.Display Name). Priority references Ticket Priority.Name. Status references Ticket Status.Name. Contains Stories, Tasks, and Bugs that share the same Project Key.")
public class Epic {

    @Searchable
    @ToString
    @Field.Text(name = "Summary", description = "Brief summary of the epic")
    public String summary;

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false, description = "List of comments added to the epic")
    public List<String> comments;

    @Field.Text(name = "Reporter Name", description = "Name of the person who reported the epic. References User.Display Name.", required = false)
    public String reporterName;

    @Field.Text(name = "Assignee Name", description = "Name of the person assigned to the epic. References User.Display Name.", required = false)
    public String assigneeName;

    @Field.Text(name = "Type", description = "Type classification of the epic", required = false)
    public String type;
    @Searchable
    @ToString
    @Field.Text(name = "Epic Key", description = "Unique Jira key identifier for the epic. Referenced by Subtask.Parent Key for parent-child relationships.", required = false)
    public String epicKey;

    @Field.Text(name = "Id", description = "Internal Jira identifier for the epic", required = false)
    public String id;

    @Field.Text(name = "Project Key", description = "Key of the Jira project this epic belongs to. References Ticket Project.Key.")
    public String projectKey;

    @Field.Text(name = "Priority", description = "Priority level of the epic. References Ticket Priority.Name.", required = false)
    public String priority;

    @ToString
    @Field.Text(name = "Status", description = "Current workflow status of the epic. References Ticket Status.Name.", required = false)
    public String status;

    @Searchable
    @Field(name = "Description", type = "RichText")
    public String description;

    @Searchable
    @Field.Date(name = "Created Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the epic was created", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long createdDate;

    @Searchable
    @Field.Date(name = "Last Modified Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the epic was last modified", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long lastModifiedDate;

    public Map<String, Object> toFields() {
        Epic epic = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Summary", epic.summary);
        fieldMap.put("Comments", epic.comments);
        fieldMap.put("Reporter Name", epic.reporterName);
        fieldMap.put("Assignee Name", epic.assigneeName);
        fieldMap.put("Type", epic.type);
        fieldMap.put("Epic Key", epic.epicKey);
        fieldMap.put("Id", epic.id);
        fieldMap.put("Project Key", epic.projectKey);
        fieldMap.put("Priority", epic.priority);
        fieldMap.put("Status", epic.status);
        fieldMap.put("Description", epic.description);
        fieldMap.put("Created Date", epic.createdDate);
        fieldMap.put("Last Modified Date", epic.lastModifiedDate);
        return fieldMap;
    }

}