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

package app.krista.extensions.devsecops.development.jira;

import app.krista.extension.executor.Invoker;
import app.krista.extension.util.InvokerAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Service
public class JiraAttributes {

    public static final String HOST_URL = "Host Url";
    public static final String USER_NAME = "Admin User Name";
    public static final String AUTH_TOKEN = "Admin Auth Token";
    public static final String CLIENT_ID = "Client Id";
    public static final String CLIENT_SECRET = "Client Secret";
    private String hostUrl;
    private String userName;
    private String authToken;
    private String clientId;
    private String clientSecret;
    private final Invoker invoker;
    private final RequestContext requestContext;
    private final SessionManager sessionManager;
    private final AuthorizationContext authorizationContext;


    @Inject
    public JiraAttributes(Invoker invoker, RequestContext requestContext, SessionManager sessionManager,
                          AuthorizationContext authorizationContext) {
        this.hostUrl = (String) invoker.getAttributes().get(HOST_URL);
        this.userName = (String) invoker.getAttributes().get(USER_NAME);
        this.authToken = (String) invoker.getAttributes().get(AUTH_TOKEN);
        this.clientId = (String) invoker.getAttributes().get(CLIENT_ID);
        this.clientSecret = (String) invoker.getAttributes().get(CLIENT_SECRET);
        this.invoker = invoker;
        this.requestContext = requestContext;
        this.sessionManager = sessionManager;
        this.authorizationContext = authorizationContext;
    }

    public void setAttributes(Map<String, Object> attributes) {
        hostUrl = (String) attributes.get(HOST_URL);
        userName = (String) attributes.get(USER_NAME);
        authToken = (String) attributes.get(AUTH_TOKEN);
        clientId = (String) attributes.get(CLIENT_ID);
        clientSecret = (String) attributes.get(CLIENT_SECRET);
    }


    public String getHostUrl() {
        return hostUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }


    private boolean isEmailAddress(String userName) {
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        return Pattern.compile(emailRegex)
                .matcher(userName)
                .matches();
    }


    public boolean basicCredentialsPresentInAttributes(Map<String, Object> attributes) {
        return !(attributes.get(JiraAttributes.AUTH_TOKEN) == null || attributes.get(JiraAttributes.AUTH_TOKEN).toString().isBlank()) &&
                !(attributes.get(JiraAttributes.USER_NAME) == null || attributes.get(JiraAttributes.USER_NAME).toString().isBlank()) &&
                !(attributes.get(JiraAttributes.HOST_URL) == null || attributes.get(JiraAttributes.HOST_URL).toString().isBlank());
    }

    public boolean oAuthCredentialsPresentInAttributes(Map<String, Object> attributes) {
        return !(attributes.get(JiraAttributes.CLIENT_ID) == null || attributes.get(JiraAttributes.CLIENT_ID).toString().isBlank()) &&
                !(attributes.get(JiraAttributes.CLIENT_SECRET) == null || attributes.get(JiraAttributes.CLIENT_SECRET).toString().isBlank());
    }


    public void validateAttributes(Map<String, Object> attributes) {
        if (!basicCredentialsPresentInAttributes(attributes) && (!oAuthCredentialsPresentInAttributes(attributes)))
            throw new IllegalArgumentException("Either (Auth token, user name, host url) or (Client ID, Client secret) must be provided for authentication");

        if (userName != null && !isEmailAddress(userName)) {
            throw new IllegalArgumentException("User name is not a valid email address");
        }

        testConnection(attributes);
    }

    void testConnection(Map<String, Object> attributes) {
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", "assignee=currentUser() ORDER BY priority DESC, updated desc");
        qparams.put("fields", "*all");
        qparams.put("startAt", 0L);
        qparams.put("maxResults", 1L);
        RestClient restClient;
        if (isPartialOAuthCredentialPresent(attributes)) {
            throw new IllegalArgumentException("Please provide both Client Id and Client Secret");
        }
        if (isPartialBasicCredentialPresent(attributes)) {
            throw new IllegalArgumentException("Please provide Admin Auth Token, Admin User Name, and Host Url");
        }
        if (basicCredentialsPresentInAttributes(attributes)) {
            restClient = new AuthRestClient(attributes, requestContext.invokeAsUser(), true);
            testConnectionWithRestClient(qparams, restClient);
        }
        if (oAuthCredentialsPresentInAttributes(attributes)) {
            restClient = new OAuth2RestClient(new JiraService(requestContext, sessionManager, invoker,
                    authorizationContext, this));
            testConnectionWithRestClient(qparams, restClient);
        }
    }


    public boolean isPartialBasicCredentialPresent(Map<String, Object> attributes) {
        if ((isAttributePresent(attributes, AUTH_TOKEN) && (!isAttributePresent(attributes, HOST_URL) || !isAttributePresent(attributes, USER_NAME))) ||
                (isAttributePresent(attributes, HOST_URL) && (!isAttributePresent(attributes, AUTH_TOKEN) || !isAttributePresent(attributes, USER_NAME))) ||
                (isAttributePresent(attributes, USER_NAME) && (!isAttributePresent(attributes, AUTH_TOKEN) || !isAttributePresent(attributes, HOST_URL)))) {
            return true;
        }
        return false;
    }


    private boolean isPartialOAuthCredentialPresent(Map<String, Object> attributes) {
        if ((isAttributePresent(attributes, CLIENT_ID) && !isAttributePresent(attributes, CLIENT_SECRET)) ||
                (isAttributePresent(attributes, CLIENT_SECRET) && !isAttributePresent(attributes, CLIENT_ID))) {
            return true;
        }
        return false;
    }

    private boolean isAttributePresent(Map<String, Object> attributes, String attributeName) {
        return attributes.get(attributeName) != null && !attributes.get(attributeName).toString().isBlank();
    }


    private void testConnectionWithRestClient(Map<String, Object> qparams, RestClient restClient) {
        String out = restClient.fetch("search/jql", qparams, String.class);
        try {
            JsonObject jsonObject = GSON.fromJson(out, JsonObject.class);
            if (!jsonObject.has("issues")) {
                setAttributes(invoker.getAttributes());
                throw new IllegalArgumentException(jsonObject.has("message") ? (jsonObject.get("message")).getAsJsonObject().getAsString() : "");
            }
        } catch (Exception cause) {
            setAttributes(invoker.getAttributes());
            throw new IllegalArgumentException("Wrong Credentials!", cause);
        }
    }

    public boolean areBasicCredentialsPresent() {
        return !((userName == null || userName.isBlank()) ||
                (authToken == null || authToken.isBlank()) ||
                (hostUrl == null || hostUrl.isBlank()));
    }

    public boolean areOAuthCredentialsPresent() {
        return !((clientId == null || clientId.isBlank()) ||
                (clientSecret == null || clientSecret.isBlank()));
    }

    public void update(Map<String, Object> attributes) {
        clientId = InvokerAttributes.getStringOrNull(attributes, CLIENT_ID);
        clientSecret = InvokerAttributes.getStringOrNull(attributes, CLIENT_SECRET);
        userName = InvokerAttributes.getStringOrNull(attributes, USER_NAME);
        hostUrl = InvokerAttributes.getStringOrNull(attributes, HOST_URL);
        authToken = InvokerAttributes.getStringOrNull(attributes, AUTH_TOKEN);
    }

}
