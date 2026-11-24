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

import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.sdk.atlassian.attributes.CredentialFieldNames;
import app.krista.sdk.atlassian.client.AbstractBasicAuthRestClient;
import app.krista.sdk.atlassian.error.HttpErrorMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Basic-auth (account email + API token) Jira REST client. Target creation,
 * query-parameter expansion, status-code translation and multipart upload are
 * provided by the shared {@link AbstractBasicAuthRestClient}.
 */
public final class AuthRestClient extends AbstractBasicAuthRestClient implements RestClient {

    private static final HttpErrorMapper ERROR_MAPPER = HttpErrorMapper.withDefaults()
            .message(401, "Admin Auth Token is invalid or expired. Please regenerate your API token from Atlassian account settings (id.atlassian.com → Security → API tokens).")
            .message(403, "Access denied. The account does not have permission to perform this operation.")
            .message(404, "Please Provide Valid Host URL")
            .message(400, "Please Provide Valid Auth Token OR User Name");

    public AuthRestClient(Map<String, Object> attributes, boolean invokeAsUser, boolean callFromValidateAttributes) {
        super(attributes,
                new CredentialFieldNames(JiraAttributes.HOST_URL, JiraAttributes.USER_NAME,
                        JiraAttributes.AUTH_TOKEN, JiraAttributes.CLIENT_ID, JiraAttributes.CLIENT_SECRET),
                invokeAsUser, callFromValidateAttributes, ERROR_MAPPER);
    }

    /**
     * This is post method which Parses and validates JQL queries, if JQL is incorrect then it returns JSON of query and errors contain in JQL query.
     *
     * @param jql query which needs to be validated and fetch metadata.
     * @param accountId denotes user information.
     * @param invokerId denotes invoker information.
     * @return String of JSON which contain metaData of JQL.
     */
    @Override
    public String parseAndValidateJQL(String jql, Boolean invokeAsUser, String accountId, String invokerId) {
        WebTarget resourceTarget = getWebTarget(invokeAsUser).path("jql/parse");
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
        WebTarget resourceTarget = getWebTarget(invokeAsUser).path(resource);
        return fetchIssue(queryParams, type, resourceTarget);
    }

    @Override
    public <T> T fetchIssues(String accountId, String invokerId, String resource, Map<String, Object> queryParams, Class<T> type) {
        WebTarget resourceTarget = getWebTarget(null).path(resource);
        return fetchIssue(queryParams, type, resourceTarget);
    }

    private <T> T fetchIssue(Map<String, Object> queryParams, Class<T> type, WebTarget resourceTarget) {
        resourceTarget = applyQueryParams(resourceTarget, queryParams);
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON);
        Response response = invoker.get();
        T entityType = response.readEntity(type);
        if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            return entityType;
        }
        throw new IllegalStateException((String) entityType);
    }

    @Override
    public HttpResponse<JsonNode> attachFile(String resource, List<File> files) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource not found");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("file not found");
        }
        String callUrl = getAttributes().get(JiraAttributes.HOST_URL) + "/issue/" + resource + "/attachments";
        return uploadMultipart(callUrl, files);
    }

}
