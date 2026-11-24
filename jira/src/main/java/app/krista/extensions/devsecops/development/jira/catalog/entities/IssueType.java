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
        ecosystemVersion = "f376e5a4-a15d-4a3f-b274-cafb36d98bc0")
@Entity(name = "Issue Type", id = "localDomainEntity_03c74f6d-d9ca-4ad4-9935-d1877df27b68", primaryKey = "Id", supportStore = true, description = "Represents a type of issue in Jira. The Name field is referenced by Issue.Issue Type.")
public class IssueType {

    @Field.Text(name = "Sub Task", description = "Indicates whether this issue type is a subtask")
    public String subTask;

    @Field.Text(name = "Id", description = "Internal Jira identifier for the issue type")
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Name", description = "Name of the issue type. Referenced by Issue.Issue Type.")
    public String name;

    @ToString
    @Field(name = "Description", type = "RichText")
    public String description;

    public Map<String, Object> toFields() {
        IssueType issueType = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Sub Task", issueType.subTask);
        fieldMap.put("Id", issueType.id);
        fieldMap.put("Name", issueType.name);
        fieldMap.put("Description", issueType.description);
        return fieldMap;
    }


}
