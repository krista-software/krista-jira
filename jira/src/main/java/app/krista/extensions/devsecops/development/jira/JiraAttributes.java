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
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.attributes.BaseAtlassianAttributes;
import app.krista.sdk.atlassian.attributes.CredentialFieldNames;
import app.krista.sdk.atlassian.constants.AtlassianConstants;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Jira credential attributes. Credential resolution, complete/partial/mixed
 * detection and the {@code validateAttributes} template flow are provided by
 * the shared {@link BaseAtlassianAttributes}; this class contributes Jira's
 * historical field labels and the product-specific connection test against
 * the {@code /myself} endpoint.
 */
@Service
public class JiraAttributes extends BaseAtlassianAttributes {

    public static final String HOST_URL = "Host Url";
    public static final String USER_NAME = "Admin User Name";
    public static final String AUTH_TOKEN = "Admin Auth Token";
    public static final String CLIENT_ID = "Client Id";
    public static final String CLIENT_SECRET = "Client Secret";
    public static final String CREDENTIALS_MODE = "Credentials Mode";
    public static final String SERVICE_ACCOUNT = "Service Account";

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraAttributes.class);

    /**
     * Key prefix for credential snapshots persisted by {@code AbstractAtlassianAuthResource}
     * — mirrors the private constant there so per-request attribute reconstruction can
     * supplement missing fields from the Authentication tab's saved state.
     */
    private static final String CRED_PREFIX = "cred.";

    private final RequestContext requestContext;
    private final SessionManager sessionManager;
    private final AuthorizationContext authorizationContext;

    @Inject
    public JiraAttributes(Invoker invoker, RequestContext requestContext, SessionManager sessionManager,
                          AuthorizationContext authorizationContext) {
        super(invoker, new CredentialFieldNames(HOST_URL, USER_NAME, AUTH_TOKEN, CLIENT_ID, CLIENT_SECRET));
        this.requestContext = requestContext;
        this.sessionManager = sessionManager;
        this.authorizationContext = authorizationContext;
    }

    /**
     * Exposes the "Krista Managed" credentials chooser: when the
     * {@value #CREDENTIALS_MODE} attribute is {@code "managed"}, the OAuth
     * client id/secret come from the SDK's bundled
     * {@code atlassian-managed-credentials.properties}.
     */
    @Override
    protected String getCredentialsModeFieldName() {
        return CREDENTIALS_MODE;
    }

    /** Extension type for per-extension managed-credential overrides. */
    @Override
    protected String getExtensionType() {
        return "jira";
    }

    /**
     * Exposes the server-to-server (2LO) toggle: when the
     * {@value #SERVICE_ACCOUNT} attribute is {@code true}, OAuth credentials
     * are used with the client-credentials grant instead of the 3LO
     * user-consent flow.
     */
    @Override
    protected String getServiceAccountFieldName() {
        return SERVICE_ACCOUNT;
    }

    // ── Credential getters: supplement from Authentication-tab snapshot ──────
    //
    // JiraAttributes is recreated per-request from invoker.getAttributes() (the
    // Krista/Setup attribute store).  When credentials were saved exclusively
    // through the Authentication tab they live only in the KeyValueStore snapshot
    // (keys: "cred.<invokerId>.<field>") and are absent from the attribute store.
    // Overriding the five credential getters to fall back to that snapshot makes
    // areBasicCredentialsPresent() / areOAuthCredentialsPresent() return the
    // correct answer so RestClientSelector picks the right REST client for every
    // catalog request.

    private String credSnap(String field) {
        if (sessionManager == null || requestContext == null) return null;
        return sessionManager.get(CRED_PREFIX + requestContext.getInvokerId() + "." + field);
    }

    @Override
    public String getHostUrl() {
        String value = super.getHostUrl();
        return (value != null && !value.isBlank()) ? value : credSnap("hostUrl");
    }

    @Override
    public String getUserName() {
        String value = super.getUserName();
        return (value != null && !value.isBlank()) ? value : credSnap("userName");
    }

    @Override
    public String getAuthToken() {
        String value = super.getAuthToken();
        return (value != null && !value.isBlank()) ? value : credSnap("authToken");
    }

    @Override
    public String getClientId() {
        String value = super.getClientId();
        return (value != null && !value.isBlank()) ? value : credSnap("clientId");
    }

    @Override
    public String getClientSecret() {
        String value = super.getClientSecret();
        return (value != null && !value.isBlank()) ? value : credSnap("clientSecret");
    }

    /**
     * Determines whether managed (Quick Setup) credentials mode is active.
     *
     * <p>Priority order:</p>
     * <ol>
     *   <li><b>In-memory state</b> — set by {@code setAttributes(candidate)} /
     *       {@code update(candidate)} during the <em>current</em> request (e.g.
     *       when the auth tab's Save/Test Connection calls
     *       {@code testManagedOAuthConnection}).  This MUST take precedence so
     *       that the OAuth callback URL built within the same request reflects
     *       the user's <em>current</em> intent, not the previous save.</li>
     *   <li><b>Persistent credential snapshot</b> — written by
     *       {@code persistCredentialSnapshot} on a successful auth-tab save.
     *       Consulted when the in-memory state is null (fresh per-request
     *       attribute construction).</li>
     *   <li><b>Base-class auto-detection</b> — invoker attributes +
     *       {@code ManagedCredentialStore} fallback.</li>
     * </ol>
     */
    @Override
    public boolean isManagedCredentialsEnabled() {
        // 1. In-memory state (current request's intent — highest priority).
        String inMemoryMode = getCredentialsModeValue();
        if ("managed".equalsIgnoreCase(inMemoryMode)) {
            return true;
        }
        if ("own".equalsIgnoreCase(inMemoryMode)) {
            return false;
        }
        // 2. Persistent credential snapshot (previous save's intent).
        String storedMode = credSnap("credentialsMode");
        if ("managed".equalsIgnoreCase(storedMode)) {
            return true;
        }
        if ("own".equalsIgnoreCase(storedMode)) {
            return false;
        }
        // 3. Base-class auto-detection.
        return super.isManagedCredentialsEnabled();
    }

    @Override
    protected void testConnection(Map<String, Object> attributes) {
        if (attributes == null) {
            return;
        }
        if (isPartialOAuthCredentialPresent(attributes)) {
            throw new IllegalArgumentException(
                    "Incomplete OAuth 2.0 credentials detected.\n"
                            + "Please provide BOTH " + getFieldNames().clientId() + " and " + getFieldNames().clientSecret()
                            + " for OAuth authentication.");
        }
        if (isPartialBasicCredentialPresent(attributes)) {
            throw new IllegalArgumentException(
                    "Incomplete Basic Auth credentials detected.\n"
                            + "Please provide ALL three fields: " + getFieldNames().authToken() + ", " + getFieldNames().userName()
                            + ", and " + getFieldNames().hostUrl() + " for Basic authentication.");
        }
        if (basicCredentialsPresentInAttributes(attributes)) {
            testConnectionWithRestClient(new AuthRestClient(attributes, requestContext.invokeAsUser(), true));
        } else if (oAuthCredentialsPresentInAttributes(attributes) || isManagedCredentialsEnabled()) {
            JiraService jiraService =
                    new JiraService(requestContext, sessionManager, getInvoker(), authorizationContext, this);
            jiraService.setCalledFromSetup(true);
            testConnectionWithRestClient(new OAuth2RestClient(jiraService));
        }
    }

    private void testConnectionWithRestClient(RestClient restClient) {
        try {
            String out = restClient.fetch("myself", new HashMap<>(), String.class);
            LOGGER.debug("Test Connection Response Details : {}", out);
            JsonObject jsonObject = AtlassianConstants.GSON.fromJson(out, JsonObject.class);
            if (jsonObject.has("message") || (!jsonObject.has("self") && !jsonObject.has("accountId"))) {
                setAttributes(getInvoker().getAttributes());
                throw new IllegalArgumentException("The credentials provided are incorrect. Please ensure the Host Url, User Name and Admin Auth Token are correct.");
            }
            LOGGER.info("Connection test successful!");
        } catch (app.krista.extension.authorization.MustAuthorizeException cause) {
            LOGGER.info("OAuth authorization required - redirecting to Atlassian login...");
            throw cause;
        } catch (IllegalStateException cause) {
            setAttributes(getInvoker().getAttributes());
            LOGGER.error("Connection test failed: {}", cause.getMessage());
            throw new IllegalArgumentException(cause.getMessage(), cause);
        } catch (Exception cause) {
            setAttributes(getInvoker().getAttributes());
            LOGGER.error("Connection test failed: {}", cause.getMessage());
            throw new IllegalArgumentException("The credentials provided are incorrect. Please ensure the Host Url, User Name and Admin Auth Token are correct.", cause);
        }
    }

}
