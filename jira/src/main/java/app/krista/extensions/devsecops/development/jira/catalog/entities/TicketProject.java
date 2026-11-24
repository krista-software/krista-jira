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

import app.krista.extension.impl.anno.*;
@Searchable
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "7339e9c9-76fd-4b24-bf95-4c5fdc3cef85")
@Entity(name = "Ticket Project", id = "localDomainEntity_0ddcdd3c-6ef1-4477-8b5e-9fecb00748a0", primaryKey = "Key", supportStore = true, description = "Represents a Jira project. Parent entity for Bug, Story, Epic, Task, Subtask, and Issue (their Project Key references Ticket Project.Key). Lead Account Id references User.Account Id. Contains Versions (via Version.Project Id matching Ticket Project.Project Id) and Components (via Component.Project matching Ticket Project.Name).")
public class TicketProject {

    @Field.Text(name = "Lead Account Id", description = "Account identifier of the project lead. References User.Account Id.")
    public String leadAccountId;

    @Field.Text(name = "Type", description = "Type of the Jira project")
    public String type;

    @Searchable
    @ToString
    @Field.Text(name = "Project Id", description = "Internal Jira identifier for the project. Referenced by Version.Project Id.")
    public String projectId;

    @Field.Text(name = "Style", description = "Style of the Jira project")
    public String style;

    @Field.Text(name = "Assignee Type", description = "Default assignee type for the project")
    public String assigneeType;

    @Searchable
    @ToString
    @Field.Text(name = "Key", description = "Unique key identifier for the project. Referenced by Bug.Project Key, Story.Project Key, Epic.Project Key, Task.Project Key, Subtask.Project Key, and Issue.Project Key.")
    public String key;

    @Searchable
    @ToString
    @Field.Text(name = "Name", description = "Name of the Jira project")
    public String name;

    @Field.Text(name = "Lead Name", description = "Name of the project lead. References User.Display Name.")
    public String leadName;

    public Map<String, Object> toFields() {
        TicketProject ticketProject = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Lead Account Id", ticketProject.leadAccountId);
        fieldMap.put("Type", ticketProject.type);
        fieldMap.put("Project Id", ticketProject.projectId);
        fieldMap.put("Style", ticketProject.style);
        fieldMap.put("Assignee Type", ticketProject.assigneeType);
        fieldMap.put("Key", ticketProject.key);
        fieldMap.put("Name", ticketProject.name);
        fieldMap.put("Lead Name", ticketProject.leadName);
        return fieldMap;
    }

}
