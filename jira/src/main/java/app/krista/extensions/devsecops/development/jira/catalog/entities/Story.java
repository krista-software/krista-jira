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
        ecosystemVersion ="33aa9347-d825-447c-be08-a2b02f8af859")
@Entity(name = "Story", id = "localDomainEntity_5faf6082-c1d7-4cfa-b23b-d23ce2a22877", primaryKey = "Story Key", supportStore = true, description = "Represents a user story issue in Jira. Belongs to a Ticket Project (via Project Key). Parent Key references the parent issue (typically Epic.Epic Key). Related to User (Reporter Name and Assignee Name match User.Display Name). Priority references Ticket Priority.Name. Status references Ticket Status.Name. May have Subtasks where Subtask.Parent Key matches Story Key.")
public class Story {

    @Searchable
    @ToString
    @Field.Text(name = "Summary", description = "Brief summary of the story", required = false)
    public String summary;

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false, description = "List of comments added to the story")
    public List<String> comments;

    @Field.Text(name = "Reporter Name", description = "Name of the person who reported the story. References User.Display Name.", required = false)
    public String reporterName;

    @Field.Text(name = "Assignee Name", description = "Name of the person assigned to the story. References User.Display Name.", required = false)
    public String assigneeName;

    @Field.Text(name = "Story Type", description = "Type classification of the story", required = false)
    public String storyType;

    @Searchable
    @ToString
    @Field.Text(name = "Story Key", description = "Unique Jira key identifier for the story. Referenced by Subtask.Parent Key for parent-child relationships.", required = false)
    public String storyKey;

    @Field.Text(name = "Id", description = "Internal Jira identifier for the story", required = false)
    public String id;

    @Field.Text(name = "Parent Key", description = "Jira key of the parent issue (typically an Epic). References Epic.Epic Key.", required = false)
    public String parentKey;

    @Field.Text(name = "Project Key", description = "Key of the Jira project this story belongs to. References Ticket Project.Key.")
    public String projectKey;

    @Field.Text(name = "Priority", description = "Priority level of the story. References Ticket Priority.Name.", required = false)
    public String priority;

    @ToString
    @Field.Text(name = "Status", description = "Current workflow status of the story. References Ticket Status.Name.", required = false)
    public String status;

    @Searchable
    @Field(name = "Description", type = "RichText")
    public String description;

    @Searchable
    @Field.Date(name = "Created Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the story was created", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long createdDate;

    @Searchable
    @Field.Date(name = "Last Modified Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, description = "Date and time when the story was last modified", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long lastModifiedDate;

    public Map<String, Object> toFields() {
        Story story = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Comments", story.comments);
        fieldMap.put("Summary", story.summary);
        fieldMap.put("Reporter Name", story.reporterName);
        fieldMap.put("Assignee Name", story.assigneeName);
        fieldMap.put("Story Type", story.storyType);
        fieldMap.put("Story Key", story.storyKey);
        fieldMap.put("Id", story.id);
        fieldMap.put("Parent Key", story.parentKey);
        fieldMap.put("Project Key", story.projectKey);
        fieldMap.put("Priority", story.priority);
        fieldMap.put("Status", story.status);
        fieldMap.put("Description", story.description);
        fieldMap.put("Created Date", story.createdDate);
        fieldMap.put("Last Modified Date", story.lastModifiedDate);
        return fieldMap;
    }

}
