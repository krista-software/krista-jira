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

import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public final class OAuth2RestClient implements RestClient {

    private final JiraService oAuthService;
    private JiraService.AccessibleResources accessibleResources = null;
    public static final String ATLASSIAN_URL = "https://api.atlassian.com/ex/jira/";
    public static final String JIRA_ENDPOINT = "/rest/api/3/";

    @Inject
    public OAuth2RestClient(JiraService jiraService) {
        oAuthService = jiraService;
    }

    private WebTarget getWebTarget() {
        Client client = ClientBuilder.newClient();
        String target = ATLASSIAN_URL + getAccessibleResources().getId() + JIRA_ENDPOINT;
        return client.target(target);
    }

    private WebTarget getWebTarget(String accountId, String invokerId) {
        Client client = ClientBuilder.newClient();
        String target = ATLASSIAN_URL + getAccessibleResources(accountId, invokerId).getId() + JIRA_ENDPOINT;
        return client.target(target);
    }

    public JiraService.AccessibleResources getAccessibleResources(String accountId, String invokerId) {
        if (accessibleResources == null) {
            accessibleResources = oAuthService.getAccessibleResources(accountId, invokerId);
        }
        return accessibleResources;
    }

    public JiraService.AccessibleResources getAccessibleResources() {
        if (accessibleResources == null) {
            accessibleResources = oAuthService.getAccessibleResources();
        }
        return accessibleResources;
    }

    @Override
    public Invocation.Builder getResourceInvoker(String resource, Map<String, Object> queryParams) {
        WebTarget resourceTarget = getWebTarget().path(resource);
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        return resourceTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + oAuthService.getJiraToken());
    }

    @Override
    public <T> T fetch(String resource, Map<String, Object> queryParams, Class<T> type) {
        WebTarget resourceTarget = getWebTarget().path(resource);
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + oAuthService.getJiraToken());
        Response response = invoker.get();
        return response.readEntity(type);
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
        WebTarget resourceTarget = getWebTarget(accountId, invokerId).path(resource);
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + oAuthService.getJiraTokenForIssues(accountId, invokerId));
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
        String callUrl = ATLASSIAN_URL + getAccessibleResources().getId() + JIRA_ENDPOINT + "/issue/" + resource + "/attachments";
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post(callUrl).header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + oAuthService.getJiraToken()).header(JiraConstants.ACCEPT, MediaType.APPLICATION_JSON).header(JiraConstants.X_ATLASSIAN_TOKEN, JiraConstants.NO_CHECK).field("file", files).asJson();
        } catch (UnirestException cause) {
            throw new IllegalStateException(cause);
        }
        return response;
    }

    @Override
    public String getJiraAccessToken() {
        return oAuthService.getJiraToken();
    }

}
