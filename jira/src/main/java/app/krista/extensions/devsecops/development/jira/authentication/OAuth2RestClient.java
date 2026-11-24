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

package app.krista.extensions.devsecops.development.jira.authentication;

import app.krista.sdk.atlassian.client.AbstractOAuth2RestClient;
import app.krista.sdk.atlassian.config.AtlassianProduct;
import app.krista.sdk.atlassian.oauth.AtlassianTokenSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;

import static app.krista.sdk.atlassian.constants.AtlassianConstants.BEARER;

/**
 * OAuth 2.0 (3LO) Jira REST client over the Atlassian API gateway. Cloud-id
 * resolution, gateway URL construction, bearer decoration and multipart
 * upload are provided by the shared {@link AbstractOAuth2RestClient}.
 */
@Service
public final class OAuth2RestClient extends AbstractOAuth2RestClient implements RestClient {

    @Inject
    public OAuth2RestClient(JiraService jiraService) {
        super(jiraService, AtlassianProduct.JIRA);
    }

    /**
     * Server-to-server (2LO) variant: same client, but bearer tokens come
     * from a {@link app.krista.sdk.atlassian.oauth.ClientCredentialsTokenProvider}
     * instead of the 3LO refresh-token flow.
     */
    public OAuth2RestClient(AtlassianTokenSource tokenSource) {
        super(tokenSource, AtlassianProduct.JIRA);
    }

    /**
     * This is post method which Parses and validates JQL queries, if JQL is incorrect then it returns query and errors contain in JQL query.
     *
     * @param jql query which needs to be validated and fetch metadata.
     * @param accountId denotes user information.
     * @param invokerId denotes invoker information.
     * @return String of JSON which contain metaData of JQL.
     */
    @Override
    public String parseAndValidateJQL(String jql, Boolean invokeAsUser, String accountId, String invokerId) {
        WebTarget resourceTarget = getWebTarget(accountId, invokerId).path("jql/parse");
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON);
        JsonObject data = new JsonObject();
        JsonArray queries = new JsonArray();
        queries.add(jql);
        data.add("queries", queries);
        try (Response response = invoker.post(Entity.entity(data.toString(), MediaType.APPLICATION_JSON))) {
            String jsonResponse = response.readEntity(String.class);
            if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                return jsonResponse;
            } else if (response.getStatus() == 404) {
                throw new IllegalStateException("Please Provide Valid Host URL");
            } else if (response.getStatus() == 400) {
                throw new IllegalStateException("Please Provide Valid Auth Token OR User Name");
            }
            return "";
        }
    }

    @Override
    public <T> T fetchIssues(Boolean invokeAsUser, String accountId, String invokerId, String resource, Map<String, Object> queryParams, Class<T> type) {
        return fetchIssues(accountId, invokerId, resource, queryParams, type);
    }

    @Override
    public <T> T fetchIssues(String accountId, String invokerId, String resource, Map<String, Object> queryParams, Class<T> type) {
        WebTarget resourceTarget = applyQueryParams(getWebTarget(accountId, invokerId).path(resource), queryParams);
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + getOAuthService().getToken(accountId, invokerId));
        Response response = invoker.get();
        return response.readEntity(type);
    }

    @Override
    public HttpResponse<JsonNode> attachFile(String resource, List<File> files) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource not found");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("file not found");
        }
        // Note: the legacy URL intentionally contained "/rest/api/3//issue/..." (double slash);
        // preserved exactly to avoid any behaviour change.
        String callUrl = AtlassianProduct.JIRA.gatewayUrl(getAccessibleResource().getId())
                + "/issue/" + resource + "/attachments";
        return uploadMultipart(callUrl, files);
    }

}
