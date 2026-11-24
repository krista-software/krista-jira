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
import java.util.Map;
@Searchable
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "9ecbdb67-0c39-44de-85e6-af51f0714136")
@Entity(name = "Component", id = "localDomainEntity_b2264514-6722-477f-86c9-5a8365e65ce5", primaryKey = "Id", supportStore = true, description = "Represents a project component in Jira. Belongs to a Ticket Project (via Project referencing Ticket Project.Name). Referenced by Issue.Components.")
public class Component {

    @Field.Text(name = "Id", description = "Internal Jira identifier for the component", required = false)
    public final String id;

    @Searchable
    @ToString
    @Field.Text(name = "Name", description = "Name of the project component. Referenced by Issue.Components.", required = false)
    public final String name;

    @Searchable
    @ToString
    @Field.Text(name = "Project", description = "Jira project this component belongs to. References Ticket Project.Name.", required = false)
    public final String project;

    public Map<String, Object> toFields() {
        Component component = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Id", component.id);
        fieldMap.put("Name", component.name);
        fieldMap.put("Project", component.project);
        return fieldMap;
    }

    public Component(String id, String name, String project){
        this.id = id;
        this.project = project;
        this.name = name;
    }

}
