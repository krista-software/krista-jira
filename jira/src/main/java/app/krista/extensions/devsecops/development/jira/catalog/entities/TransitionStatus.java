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
@Searchable
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion ="7339e9c9-76fd-4b24-bf95-4c5fdc3cef85")
@Entity(name = "Transition Status", id = "localDomainEntity_42cd1d52-47ec-4f3e-bdbf-182afcd853ab", primaryKey = "From Id", supportStore = true, description = "Represents a workflow transition between statuses in Jira. From Name and To Name reference Ticket Status.Name.")
public class TransitionStatus {

    @Field.Text(name = "From Id", description = "Identifier of the source status")
    public String fromId;

    @ToString
    @Field.Text(name = "From Name", description = "Name of the source status. References Ticket Status.Name.")
    public String fromName;

    @Field.Text(name = "To Id", description = "Identifier of the target status")
    public String toId;

    @ToString
    @Field.Text(name = "To Name", description = "Name of the target status. References Ticket Status.Name.")
    public String toName;

    @Field.Text(name = "To Category Name", description = "Category name of the target status")
    public String toCategoryName;

}
