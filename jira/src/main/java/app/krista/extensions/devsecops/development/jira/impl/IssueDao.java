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

package app.krista.extensions.devsecops.development.jira.impl;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.*;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.services.HtmlToAdfConverter;
import app.krista.extensions.devsecops.development.jira.services.JiraIssue;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import app.krista.extensions.devsecops.development.jira.util.DatePlus;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.FreeForm;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;
import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.VALUE;
import static app.krista.omni.sdk.complex_fields.impl.util.Constants.FIELDS;

@Service
public final class IssueDao {

    public static final String ERROR_MESSAGES = "errorMessages";
    public static final String ISSUES_NOT_FOUND = JiraConstants.ERROR_ISSUE_NOT_FOUND;
    public static final String RENDERED_FIELDS = "renderedFields";
    public static final String EXPAND = "expand";
    public static final String ISSUES = "issues";
    public static final int TOTAL_ISSUES_LIMIT = 5000;
    private final RestClientFactory restClientFactory;
    private final ProjectDao projectDao;
    private final UserDao userDao;
    private final TicketInfoDao ticketInfoDao;
    private RestClient restClient;
    private final Logger logger = LoggerFactory.getLogger(IssueDao.class);

    @Inject
    public IssueDao(ProjectDao projectDao, UserDao userDao, TicketInfoDao ticketInfoDao, RestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
        this.restClient = restClientFactory.getInstance();
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.ticketInfoDao = ticketInfoDao;
    }

    public IssueDao(RestClientFactory restClientFactory, RestClient restClient) {
        this.restClientFactory = restClientFactory;
        this.restClient = restClient;
        this.projectDao = new ProjectDao(restClientFactory, restClient);
        this.userDao = new UserDao(restClientFactory, restClient);
        this.ticketInfoDao = new TicketInfoDao(restClientFactory, restClient);
    }

    private static JsonObject getPriorityJsonObject(TicketPriority priority) {
        JsonObject priorityObj = new JsonObject();
        if (priority.id != null) {
            priorityObj.addProperty("id", priority.id);
        }
        if (priority.name != null) {
            priorityObj.addProperty("name", priority.name);
        }
        return priorityObj;
    }

    public RestClient getRestClient() {
        return getRestClient(null);
    }

    private RestClient getRestClient(Boolean invokeAsUser) {
        if (restClientFactory == null) {
            return restClient;
        }

        boolean invokeAsUserAuth = invokeAsUser != null ? invokeAsUser : restClientFactory.invokeAsUser();
        if (restClient == null || (restClient instanceof OAuth2RestClient && !invokeAsUserAuth) || (restClient instanceof AuthRestClient && invokeAsUserAuth)) {
            restClient = restClientFactory.getInstance(invokeAsUserAuth);
        }
        return restClient;
    }

    public String getAccountIdByEmail(String email) {
        try {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("query", email);
            String out = restClient.fetch("user/search", queryParams, String.class);
            JsonArray jsonArray = GSON.fromJson(out, JsonArray.class);
            if (!jsonArray.isEmpty()) {
                JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                return jsonObject.get("accountId").getAsString();
            } else {
                System.err.println("No user found with email " + email);
                return "No user found with email " + email;
            }
        } catch (IllegalCallerException cause) {
            throw new IllegalCallerException(cause.getMessage(), cause);
        }
    }

    public Map<String, Object> createUser(String emailAddress) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("user", new HashMap<>());
        JsonObject userDetails = new JsonObject();
        userDetails.addProperty("emailAddress", emailAddress);
        Response response = invoker.post(Entity.entity(userDetails.toString(), MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() == 201) {
            String jsonResponse = response.readEntity(String.class);
            JsonObject responseBody = GSON.fromJson(jsonResponse, JsonObject.class);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("Self", responseBody.get("self").getAsString());
            userMap.put("Account Id", responseBody.get("accountId").getAsString());
            userMap.put("Email Address", responseBody.get("emailAddress").getAsString());
            userMap.put("Display Name", responseBody.get("displayName").getAsString());
            return userMap;
        } else {
            System.out.println("Failed to create user");
            return Map.of();
        }
    }

    public List<String> getUserGroups(String userKey) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("accountId", userKey);
        String out = restClient.fetch("user/groups", queryParams, String.class);
        if (!out.isEmpty()) {
            JsonObject responseBody = GSON.fromJson(out, JsonObject.class);
            JsonArray groupsArray = responseBody.getAsJsonArray("values");
            List<String> groupNames = new ArrayList<>();
            for (JsonElement groupElement : groupsArray) {
                JsonObject groupObject = groupElement.getAsJsonObject();
                groupNames.add(groupObject.get("name").getAsString());
            }
            return groupNames;
        } else {
            throw new RuntimeException("Failed to fetch user groups");
        }
    }

    private boolean addUserToGroup(String userKey, String groupName) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("groupname", groupName);
        Invocation.Builder invoker = getRestClient().getResourceInvoker("group/user", queryParams);
        JsonObject payload = new JsonObject();
        payload.addProperty("accountId", userKey);
        Response response = invoker.post(Entity.entity(payload.toString(), MediaType.APPLICATION_JSON_TYPE));
        return response.getStatus() == 201;
    }

    public boolean updateUser(String userKey, List<String> addGroupNames) {
        if (addGroupNames != null && !addGroupNames.isEmpty()) {
            for (String groupName : addGroupNames) {
                if (!addUserToGroup(userKey, groupName)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean deleteUser(String userKey) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("user", Map.of("accountId", userKey));
        Response response = invoker.delete();
        return response.getStatus() == 204;
    }

    public Issue createIssue(String projectKey, String summary, String description, String parentKey, String issueType) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue", new HashMap<>());
        JsonObject fields = new JsonObject();
        JsonObject project = new JsonObject();
        JsonObject issueTypeMap = new JsonObject();
        issueTypeMap.addProperty("id", getIssuesTypeId(projectKey, issueType));
        project.addProperty("key", projectKey);
        fields.add("project", project);
        fields.add("issuetype", issueTypeMap);
        fields.addProperty("summary", summary);
        fields.add("description", getAtlassianDoc(description));
        if (parentKey != null) {
            JsonObject parent = new JsonObject();
            parent.addProperty("key", parentKey);
            fields.add("parent", parent);
        }
        if (issueType.equalsIgnoreCase("Epic") && findProjectStyle(projectKey).equalsIgnoreCase("classic")) {
            fields.addProperty("customfield_10011", description);
        }
        JsonObject data = new JsonObject();
        data.add("fields", fields);
        Response response;
        response = invoker.post(Entity.entity(data.toString(), MediaType.APPLICATION_JSON));
        String jsonResponse = response.readEntity(String.class);
        JsonObject jsonObject = GSON.fromJson(jsonResponse, JsonObject.class);
        try {
            String key = jsonObject.get("key").getAsString();
            return getIssueFinal(key);
        } catch (Exception cause) {
            throw new IllegalArgumentException(jsonResponse);
        }
    }

    private String findProjectStyle(String projectKey) {
        String projectResponse = projectDao.getProjectByIdResponse(projectKey);
        JsonObject jsonObject = GSON.fromJson(projectResponse, JsonObject.class);
        return jsonObject.get("style").getAsString();
    }

    private String getIssuesTypeId(String projectKey, String issueType) {
        try {
            String jsonResponse = projectDao.getIssueTypeFromProject(projectKey);
            JsonArray issueTypes = GSON.fromJson(jsonResponse, JsonArray.class);
            for (int i = 0; i < issueTypes.size(); i++) {
                JsonObject issueTypeJson = issueTypes.get(i).getAsJsonObject();
                if (issueTypeJson.get("name").getAsString().equalsIgnoreCase(issueType)) {
                    return issueTypeJson.get("id").getAsString();
                }
            }
        } catch (Exception cause) {
            throw new IllegalArgumentException("The Issue type or the Project key provided are invalid", cause);
        }
        throw new IllegalArgumentException("Invalid issue type");
    }

    public List<String> getIssueTypes(String projectKey) {
        String jsonResponse = projectDao.getIssueTypeFromProject(projectKey);
        JsonArray issueTypes = GSON.fromJson(jsonResponse, JsonArray.class);
        List<String> issueTypesNames = new ArrayList<>();
        for (int i = 0; i < issueTypes.size(); i++) {
            JsonObject issueTypeJson = issueTypes.get(i).getAsJsonObject();
            issueTypesNames.add(issueTypeJson.get("name").getAsString());
        }
        return issueTypesNames;
    }

    public Boolean validateIssueType(String projectKey, String issueType) {
        try {
            getIssuesTypeId(projectKey, issueType);
            return true;
        } catch (IllegalArgumentException cause) {
            return false;
        }
    }

    public void addComment(String issueKey, String commentInput, String visibility) {
        JsonObject commentPayload = prepareCommentPayload(commentInput, visibility);

        logger.debug("Adding comment to issueKey: {}, isPublic: {}", issueKey,
                !"Internal".equalsIgnoreCase(visibility != null ? visibility.trim() : ""));

        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/comment", new HashMap<>());
        Response response = invoker.post(Entity.entity(commentPayload.toString(), MediaType.APPLICATION_JSON));

        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed updating status " + response.readEntity(String.class));
        }
    }

    private JsonObject prepareCommentPayload(String commentInput, String visibility) {
        String comment = commentInput.endsWith("<br>") ? commentInput.replaceAll("<br>$", "") : commentInput;
        JsonObject bodyContent = getAtlassianDoc(comment);
        JsonObject fields = new JsonObject();
        fields.add("body", bodyContent);

        boolean isPublic = !"Internal".equalsIgnoreCase(visibility != null ? visibility.trim() : "");

        JsonObject visibilityValue = new JsonObject();
        visibilityValue.addProperty("internal", !isPublic);

        JsonObject visibilityProp = new JsonObject();
        visibilityProp.addProperty("key", "sd.public.comment");
        visibilityProp.add("value", visibilityValue);

        JsonArray properties = new JsonArray();
        properties.add(visibilityProp);
        fields.add("properties", properties);

        return fields;
    }


    private JsonObject getAtlassianDoc(String description) {
        HtmlToAdfConverter converter = new HtmlToAdfConverter();
        return converter.convert(description);
    }

    public List<Issue> searchByJQL(String jql, long size, long page) {
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", jql);
        qparams.put("fields", "*all");
        qparams.put("startAt", Math.max((page - 1) * size, 0));
        qparams.put("maxResults", size == 0 ? 20 : size);
        String out = restClient.fetch("search/jql", qparams, String.class);
        JsonObject jsonObject = GSON.fromJson(out, JsonObject.class);
        if (jsonObject.has(ISSUES)) {
            JsonArray issues = (JsonArray) jsonObject.get(ISSUES);
            return getIssues(issues);
        } else {
            throw new RuntimeException(jsonObject.has(ERROR_MESSAGES) ? (jsonObject.get(ERROR_MESSAGES)).getAsJsonArray().get(0).getAsString() : ISSUES_NOT_FOUND);
        }
    }

    public List<Issue> searchByJqlQuery(boolean invokeAsUser, String accountId, String invokerId, String jql, long size) {
        List<Issue> totalIssues = new ArrayList<>();
        String nextPageToken = null;
        while (totalIssues.size() < TOTAL_ISSUES_LIMIT) {
            Map<String, Object> qparams = prepareQueryparam(jql, size, nextPageToken);
            String out = getRestClient(invokeAsUser).fetchIssues(invokeAsUser, accountId, invokerId, "search/jql", qparams, String.class);
            JsonObject jsonObject = GSON.fromJson(out, JsonObject.class);
            if (jsonObject.has(ISSUES)) {
                JsonArray issues = (JsonArray) jsonObject.get(ISSUES);
                List<Issue> fetchedIssues = getIssues(issues);
                totalIssues.addAll(fetchedIssues);
                if ((issues.size() < size) || fetchedIssues.size() < size) {
                    break;
                }
                nextPageToken = jsonObject.has("nextPageToken") ? jsonObject.get("nextPageToken").getAsString() : null;
                boolean isLast = jsonObject.has("isLast") && jsonObject.get("isLast").getAsBoolean();
                if (isLast) {
                    break;
                }
            } else {
                throw new RuntimeException(jsonObject.has(ERROR_MESSAGES) ? (jsonObject.get(ERROR_MESSAGES)).getAsJsonArray().get(0).getAsString() : ISSUES_NOT_FOUND);
            }
        }
        return totalIssues;
    }

    /**
     * This method which is used to validate JQL queries, if JQL is incorrect then it returns Map of query and errors contain in JQL query.
     *
     * @param jql       query which needs to be validated and fetch metadata.
     * @param accountId denotes user information.
     * @param invokerId denotes invoker information.
     * @return Map of query and errors contain in JQL query
     */

    public Map<String, String> fetchErrorsInJQLQuery(boolean invokeAsUser, String accountId, String invokerId, String jql) {
        Map<String, String> queryData = new HashMap<>();
        String jsonResponse = getRestClient(invokeAsUser).parseAndValidateJQL(jql, invokeAsUser, accountId, invokerId);
        if (jsonResponse != null && jsonResponse.isBlank()) {
            return Collections.emptyMap();
        }
        JsonObject jsonObject = GSON.fromJson(jsonResponse, JsonObject.class);
        if (jsonObject.get(JiraConstants.QUERIES) != null && jsonObject.get(JiraConstants.QUERIES).getAsJsonArray().get(0) != null) {
            JsonObject data = jsonObject.get(JiraConstants.QUERIES).getAsJsonArray().get(0).getAsJsonObject();
            if (data.get(JiraConstants.ERRORS) != null && data.get(JiraConstants.ERRORS).getAsJsonArray().get(0) != null) {
                queryData.put(JiraConstants.ERRORS, data.get(JiraConstants.ERRORS).getAsJsonArray().get(0).getAsString());
                if (data.get(JiraConstants.QUERY) != null) {
                    queryData.put(JiraConstants.QUERY, data.get(JiraConstants.QUERY).getAsString());
                }
            } else if (data.get(JiraConstants.QUERY) != null) {
                queryData.put(JiraConstants.QUERY, data.get(JiraConstants.QUERY).getAsString());
            }
            return queryData;
        }

        return Collections.emptyMap();
    }

    public List<Issue> getIssues(JsonArray issues) {
        List<Issue> issuesFinal = new ArrayList<>();
        for (int i = 0; i < issues.size(); i++) {
            JsonElement element = issues.get(i);
            if (element.isJsonObject()) {
                JsonObject asJsonObject = element.getAsJsonObject();
                JiraIssue issue = JiraIssue.create(asJsonObject);
                issuesFinal.add(issue.getIssue());
            }
        }
        return issuesFinal;
    }

    private Map<String, Object> prepareQueryparam(String jql, long size, String nextPageToken) {
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", jql);
        qparams.put("fields", "*all");
        qparams.put("maxResults", size == 0 ? 50 : size);
        if (nextPageToken != null) {
            qparams.put("nextPageToken", nextPageToken);
        }
        return qparams;
    }

    public void updateStatus(String issueKey, String statusName) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/transitions", new HashMap<>());
        List<TransitionStatus> transitionStatusList = ticketInfoDao.getTransitions(issueKey);
        TransitionStatus status = transitionStatusList.stream().filter(tstatus -> tstatus.toName.equalsIgnoreCase(statusName)).findFirst().orElseThrow(() -> new RuntimeException("We can't change the status of the issue key " + issueKey + " to " + statusName + " because no transitions are available to this status."));
        JsonObject transitions = new JsonObject();
        transitions.addProperty("id", status.fromId);
        JsonObject body = new JsonObject();
        body.add("transition", transitions);
        Response response;
        response = invoker.post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed updating status " + response.readEntity(String.class));
        }
    }

    public void updateIssue(Issue request) {
        updateIssue(request, null);
    }

    public void updateIssue(Issue request, FreeForm customFields) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + request.key, new HashMap<>());
        Issue old = getIssueFinal(request.key);
        JsonObject update = getUpdateJsonObject(old, request);
        JsonObject fields = getFieldsJsonObject(old, request);
        if (request.status != null && !old.status.trim().equalsIgnoreCase(request.status.trim())) {
            updateStatus(request);
        }
        if (request.assigneeName != null && !old.assigneeName.trim().equalsIgnoreCase(request.assigneeName.trim())) {
            updateAssignee(request);
        }
        if (request.labels != null && !old.labels.equals(request.labels)) {
            updateLabels(request);
        }
        if (request.components != null && !old.components.equals(request.components)) {
            updateComponents(request);
        }
        if (customFields != null && !customFields.isEmpty()) {
            updateCustomFieldToFields(customFields, fields, request.key);
        }
        JsonObject actualObject = getActualUpdateObject(update, fields);
        Response response;
        response = invoker.put(Entity.entity(actualObject.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed to update : :" + response.readEntity(String.class));
        }
    }

    private void updateCustomFieldToFields(FreeForm customFields, JsonObject fields, String issueKey) {
        JsonObject fieldsEditMetaData = getJiraFieldsData(issueKey);

        for (String customFieldName : customFields.keySet()) {
            updateCustomFieldByType(customFields, fields, fieldsEditMetaData, customFieldName);
        }
    }

    private void updateCustomFieldByType(FreeForm customFields, JsonObject fields, JsonObject fieldsEditMetaData, String customFieldName) {
        for (String fieldId : fieldsEditMetaData.keySet()) {
            JsonObject fieldMetaData = fieldsEditMetaData.getAsJsonObject(fieldId);
            String name = fieldMetaData.get("name").getAsString();
            String optionId;

            if (customFieldName.equals(name)) {
                Object updatedFieldValue = customFields.get(customFieldName);
                String fieldType = getFieldType(fieldMetaData.getAsJsonObject("schema"));

                switch (fieldType) {
                    case "date":
                        if (updatedFieldValue instanceof Number) {
                            updateDate(fields, fieldId, updatedFieldValue);
                        }
                        break;
                    case "string":
                        fields.addProperty(fieldId, updatedFieldValue.toString());
                        break;
                    case "option":
                        optionId = findOptionIdByName(fieldMetaData.getAsJsonArray("allowedValues"), updatedFieldValue.toString());
                        if (optionId != null) {
                            JsonObject optionObject = new JsonObject();
                            optionObject.addProperty("id", optionId);
                            fields.add(fieldId, optionObject);
                        }
                        break;
                    case "array":
                        JsonArray arrayValue = new JsonArray();

                        String itemsType = fieldMetaData.getAsJsonObject("schema").get("items").getAsString();
                        if (!Objects.equals(itemsType, "string")) {
                            if (updatedFieldValue instanceof List<?>) {
                                for (Object value : (List<?>) updatedFieldValue) {
                                    if (value instanceof String) {
                                        optionId = findOptionIdByName(fieldMetaData.getAsJsonArray("allowedValues"), (String) value);
                                        if (optionId != null) {
                                            JsonObject optionObject = new JsonObject();
                                            optionObject.addProperty("id", optionId);
                                            arrayValue.add(optionObject);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (updatedFieldValue instanceof List<?>) {
                                for (Object value : (List<?>) updatedFieldValue) {
                                    if (value instanceof String) {
                                        arrayValue.add((String) value);
                                    }
                                }
                            }
                        }

                        fields.add(fieldId, arrayValue);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private String findOptionIdByName(JsonArray allowedValues, String optionValue) {
        for (JsonElement element : allowedValues) {
            JsonObject option = element.getAsJsonObject();
            JsonElement valueElement = option.get("value");

            if (valueElement != null && optionValue.equals(valueElement.getAsString())) {
                return option.get("id").getAsString();
            }
        }
        return null;
    }

    private String getFieldType(JsonObject schema) {
        return schema.get("type").getAsString();
    }

    public JsonObject getJiraFieldsData(String issueKey) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(EXPAND, RENDERED_FIELDS);
        String res = getRestClient().fetch("issue/" + issueKey + "/editmeta", queryParams, String.class);
        JsonObject jsonObject = GSON.fromJson(res, JsonObject.class);
        return jsonObject.getAsJsonObject("fields");
    }

    private void updateDate(JsonObject fields, String fieldId, Object date) {
        fields.addProperty(fieldId, new SimpleDateFormat("yyyy-MM-dd").format(new Date((long) ((double) date))));
    }

    private void updateComponents(Issue issue) {
        issue.components.forEach(component -> addComponent(issue.key, component));
    }

    private void updateLabels(Issue issue) {
        issue.labels.forEach(label -> addLabel(label, issue.key));
    }

    public void addLabel(String label, String key) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + key, new HashMap<>());
        JsonObject labelObject = new JsonObject();
        labelObject.addProperty("add", label);
        JsonObject set = new JsonObject();
        JsonArray labelsArray = new JsonArray();
        labelsArray.add(labelObject);
        set.add("labels", labelsArray);
        JsonObject actualObject = new JsonObject();
        actualObject.add("update", set);
        Response response;
        response = invoker.put(Entity.entity(actualObject.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed to update : :" + response.readEntity(String.class));
        }
    }

    private void updateAssignee(Issue issue) {
        try {
            List<JiraUser> jiraUsers = userDao.searchUser(issue.assigneeName);
            JiraUser user = jiraUsers.stream().findFirst().orElseThrow(() -> new RuntimeException("Assignee user not found"));
            assignTask(issue.key, user.accountId);
        } catch (Exception ignore) {
        }
    }

    private void updateStatus(Issue issue) {
        try {
            updateStatus(issue.key, issue.status);
        } catch (Exception ignore) {
        }
    }

    private JsonObject getActualUpdateObject(JsonObject update, JsonObject fields) {
        JsonObject actualObject = new JsonObject();
        actualObject.add("update", update);
        actualObject.add("fields", fields);
        return actualObject;
    }

    private JsonObject getUpdateJsonObject(Issue old, Issue request) {
        JsonObject update = new JsonObject();
        if (request.summary != null && !old.summary.trim().equalsIgnoreCase(request.summary.trim())) {
            JsonObject set = new JsonObject();
            set.addProperty("set", request.summary);
            JsonArray array = new JsonArray();
            array.add(set);
            update.add("summary", array);
        }
        return update;
    }

    private JsonObject getFieldsJsonObject(Issue old, Issue request) {
        JsonObject fields = new JsonObject();
        if (request.description != null && !old.description.trim().equalsIgnoreCase(request.description.trim())) {
            JsonObject description = getAtlassianDoc(request.description);
            fields.add("description", description);
        }
        if (request.priority != null && !old.priority.trim().equalsIgnoreCase(request.priority.trim())) {
            TicketPriority priority1 = new TicketPriority();
            priority1.name = request.priority;
            fields.add("priority", getPriorityJsonObject(priority1));
        }
        return fields;
    }

    public void assignTask(String issueKey, String userAccountId) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/assignee", new HashMap<>());
        JsonObject fields = new JsonObject();
        fields.addProperty("accountId", userAccountId);
        Response response;
        response = invoker.put(Entity.entity(fields.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed assigning task to user: :" + response.readEntity(String.class));
        }
    }

    public Issue getIssueFinal(String key) {
        String res = getIssueResponse(key);
        JsonObject jsonObject = GSON.fromJson(res, JsonObject.class);
        JiraIssue issue = JiraIssue.create(jsonObject);
        return issue.getIssue();
    }


    public List<String> getCustomIssue(String issueKey, String customField) {
        Issue issue = getIssueFinal(issueKey);
        if (issue.key == null) {
            logger.error("Provided issue key is not valid : " + issueKey);
            throw new RuntimeException(String.format("Issue key '%s' is invalid. %s", issueKey, JiraConstants.ERROR_ISSUE_KEY_INVALID));
        }
        String projectId = getProjectId(getProjectKey(issueKey));
        String customFieldId = getCustomField(projectId, customField);
        String issueResponse = getIssueResponse(issueKey);
        JsonObject jsonObject = GSON.fromJson(issueResponse, JsonObject.class);
        JsonElement jsonResponse = jsonObject.get(FIELDS).getAsJsonObject().get(customFieldId);
        List<String> outputList = new ArrayList<>();

        if (jsonResponse == null) {
            return outputList;
        }
        if (jsonResponse.isJsonObject()) {
            JsonObject obj = jsonResponse.getAsJsonObject();
            if (obj.has(VALUE)) {
                outputList.add(obj.get(VALUE).getAsString());
            }
            return outputList;
        }
        if (jsonResponse.isJsonArray()) {
            JsonArray jsonArray = jsonResponse.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    outputList.add(element.getAsString());
                } else {
                    String value = element.getAsJsonObject().get("value").getAsString();
                    outputList.add(value);
                }
            }
            return outputList;
        }
        if (jsonResponse.isJsonPrimitive() && jsonResponse.getAsJsonPrimitive().isString()) {
            outputList.add(jsonResponse.getAsString());
        }
        return outputList;
    }


    public static String getKeyByName(String jsonResponse, String name) {
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("name").equalsIgnoreCase(name)) {
                return jsonObject.getString("key");
            }
        }

        return null;
    }

    public boolean isIssueExists(String key) {
        try {
            getIssueFinal(key);
            return true;
        } catch (NoSuchElementException | IllegalStateException | IllegalArgumentException cause) {
            return false;
        }
    }

    public String getIssueResponse(String issueKey) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(EXPAND, RENDERED_FIELDS);
        return getRestClient().fetch("issue/" + issueKey, queryParams, String.class);
    }

    public String getFieldResponse() {
        return getRestClient().fetch("field", Map.of(), String.class);
    }

    public Issue updatePriority(TicketPriority priority, String issueKey) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey, new HashMap<>());
        JsonObject fields = new JsonObject();
        fields.add("priority", getPriorityJsonObject(priority));
        JsonObject body = new JsonObject();
        body.add("fields", fields);
        Response res;
        res = invoker.put(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        if (res.getStatus() >= HttpsURLConnection.HTTP_BAD_REQUEST) {
            throw new RuntimeException("Failed to update priority for ticket " + issueKey + res.readEntity(String.class));
        }
        return getIssueFinal(issueKey);
    }

    public void addWorklog(String timeSpent, long date, String issueKey, String comment) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/worklog", new HashMap<>());
        JsonObject body = new JsonObject();
        body.addProperty("timeSpent", timeSpent);
        body.addProperty("started", DatePlus.getISODate(new Date(date)));
        if (comment != null && !comment.isEmpty()) {
            body.add("comment", getAtlassianDoc(comment));
        }
        Response res;
        res = invoker.post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        if (res.getStatus() >= HttpsURLConnection.HTTP_BAD_REQUEST) {
            throw new RuntimeException(res.readEntity(String.class));
        }
    }

    public boolean attachFileToTicket(List<File> files, String ticketId) {
        return getRestClient().attachFile(ticketId, files).getStatus() == 200;
    }


    public void assignIssueToEpic(String issueKey, String epicKey) {
        String epicLinkId = getEpicLinkId();
        JSONObject epicLink = new JSONObject();
        epicLink.put(epicLinkId, epicKey);
        JSONObject fields = new JSONObject();
        fields.put("fields", epicLink);

        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/", new HashMap<>());
        Response response;
        response = invoker.put(Entity.entity(fields.toString(), MediaType.APPLICATION_JSON));

        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException(response.readEntity(String.class));
        }

    }

    private String getEpicLinkId() {
        String out = getRestClient().fetch("field", new HashMap<>(), String.class);
        JSONArray jsonArray = new JSONArray(out);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.get("name").equals("Epic Link")) {
                return jsonObject.get("key").toString();
            }
        }
        return "";

    }

    public void assignChildIssueToEpic(String childIssueKey, String parentEpicKey) {
        try {
            Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + childIssueKey, new HashMap<>());
            JSONObject parentdetails = new JSONObject();
            parentdetails.put("key", parentEpicKey);
            JSONObject parent = new JSONObject();
            parent.put("parent", parentdetails);
            JSONObject fields = new JSONObject();
            fields.put("fields", parent);
            Response response;
            response = invoker.put(Entity.entity(fields.toString(), MediaType.APPLICATION_JSON));
            if (response.getStatus() != HttpsURLConnection.HTTP_NO_CONTENT) {
                throw new RuntimeException(response.readEntity(String.class));
            }
        } catch (Exception cause) {
            System.out.println("Did not complete request");
            throw cause;
        } finally {
            System.out.println("Assign Child Issue to Epic finished");
        }
    }

    public void addComponent(String key, String component) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + key, new HashMap<>());
        JsonObject actualObject = getactualJsonObject(component);
        Response response;
        response = invoker.put(Entity.entity(actualObject.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed to update : :" + response.readEntity(String.class));
        }
    }

    private JsonObject getactualJsonObject(String component) {
        JsonObject componentName = new JsonObject();
        componentName.addProperty("name", component);
        JsonObject componentObject = new JsonObject();
        componentObject.add("add", componentName);
        JsonObject set = new JsonObject();
        JsonArray components = new JsonArray();
        components.add(componentObject);
        set.add("components", components);
        JsonObject actualObject = new JsonObject();
        actualObject.add("update", set);
        return actualObject;
    }

    public List<Component> getComponents() {
        List<TicketProject> allProjects = projectDao.getAllProjects();
        List<Component> allComponents = new ArrayList<>();
        for (TicketProject project : allProjects) {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("maxResults", 150);
            String jsonResponse = getRestClient().fetch("project/" + project.key + "/components", queryParams, String.class);
            if (!jsonResponse.isBlank()) {
                JsonArray jsonComponents = GSON.fromJson(jsonResponse, JsonArray.class);
                jsonComponents.forEach(jsonComponent -> allComponents.add(new Component(jsonComponent.getAsJsonObject().get("id").getAsString(), jsonComponent.getAsJsonObject().get("name").getAsString(), jsonComponent.getAsJsonObject().get("project").getAsString())));
            }
        }
        return allComponents;
    }

    public List<Label> getLabels() {
        List<Label> allLabels = new ArrayList<>();
        String jsonResponse = getRestClient().fetch("label", new HashMap<>(), String.class);
        JsonObject jsonLabels = GSON.fromJson(jsonResponse, JsonObject.class);
        JsonArray values = jsonLabels.get("values").getAsJsonArray();
        values.forEach(value -> allLabels.add(new Label(value.getAsString())));
        return allLabels;
    }

    /**
     * This method validates the credentials that are currently stored in setup attributes.
     */
    public void validateCredentials() {
        getRestClient(true).getJiraAccessToken();
    }

    public ExtensionResponse prepareErrorResponseForInvalidKey(ExtensionResponseGenerator responseGenerator, ErrorHandlingStateManager internalStateManager, List<ValidationOrchestrator.ValidationResult> epicKeyValidation, String issueKey) {
        String stateId = UUID.randomUUID().toString();
        internalStateManager.put(stateId, Map.of("Validation Results", epicKeyValidation));
        return responseGenerator.generateConfirmationResponse(
                ExtensionResponse.Error.ExceptionType.INPUT_ERROR, epicKeyValidation,
                "confirmReenterGetIssueByKey", Map.of(
                        "stateId", stateId,
                        JiraResources.TICKET_ID, issueKey));
    }

    private String getProjectKey(String issueKey) {
        if (issueKey == null || !issueKey.contains("-")) {
            return "Invalid issue key";
        }
        String[] split = issueKey.split("-");
        return split[0];
    }

    private String getProjectId(String projectKey) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(EXPAND, RENDERED_FIELDS);
        String response = getRestClient().fetch("project/" + projectKey, queryParams, String.class);
        JsonObject jsonObject = GSON.fromJson(response, JsonObject.class);
        return jsonObject.get("id").getAsString();
    }

    private String getCustomField(String projectId, String customField) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("projectIds", projectId);
        queryParams.put("expand", "projects.issuetypes.fields");
        String apiUrl = "issue/createmeta";

        String response = getRestClient().fetch(apiUrl, queryParams, String.class);
        JsonObject jsonResponse = GSON.fromJson(response, JsonObject.class);
        JsonObject projects = jsonResponse.getAsJsonArray("projects").get(0).getAsJsonObject();
        JsonObject issueTypes = projects.getAsJsonArray("issuetypes").get(0).getAsJsonObject();
        JsonObject fields = issueTypes.getAsJsonObject("fields");
        Set<Map.Entry<String, JsonElement>> fieldEntries = fields.entrySet();
        for (Map.Entry<String, JsonElement> entry : fieldEntries) {
            String fieldKey = entry.getKey();
            if (fieldKey.startsWith("customfield_")) {
                JsonObject fieldDetails = entry.getValue().getAsJsonObject();
                String fieldName = fieldDetails.get("name").getAsString();
                if (fieldName.equalsIgnoreCase(customField)) {
                    return fieldKey;
                }
            }
        }
        return null;
    }
}
