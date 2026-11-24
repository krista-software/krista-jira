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

package app.krista.extensions.devsecops.development.jira.mcp;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;

import javax.inject.Inject;

/**
 * Catalog entry point for the MCP "Ask Agent" natural language query.
 */
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "24e04e29-ab06-4aa5-8f68-500563996cfc")
public class MCPArea {

    private final JiraMcpQueryService mcpQueryService;

    @Inject
    public MCPArea(JiraMcpQueryService mcpQueryService) {
        this.mcpQueryService = mcpQueryService;
    }

    @CatalogRequest(
            id = "localDomainRequest_a28e033b-8a7b-46e3-8569-54f53535057d",
            name = "Ask Agent",
            description = "Accepts natural language requests, enables the AI agent to reason and invoke available tools as needed, and returns the generated response.",
            area = "MCP",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "Response", type = "[ FreeForm ]", required = false, description = "List of Jira issues matching the query")
    public ExtensionResponse askAgent(
            @Field(name = "Query", type = "Paragraph", required = true, description = "",
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String query) {
        return mcpQueryService.query(query);
    }
}
