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

package app.krista.extensions.devsecops.development.jira.api;

import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.constants.AtlassianConstants;
import app.krista.sdk.atlassian.extension.authentication.AbstractAtlassianAuthResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * SDK-driven authentication tab for the Jira extension.
 *
 * <p>All endpoint logic (serving the auth HTML, get/save/test credentials,
 * 3LO authorize URL + callback, token removal) lives in
 * {@link AbstractAtlassianAuthResource}; this class wires Jira's attribute
 * service and {@link SessionManager} (refresh-token store) into the hooks.</p>
 *
 * <p>Both managed (Quick Setup) and own-credentials OAuth callbacks are
 * handled by the SDK's {@code oauthCallback} at {@code /sdk/auth/callback}.
 * The legacy {@code /callback} endpoint in {@link AuthenticationResource}
 * is no longer used for OAuth flows.</p>
 */
@Path("/sdk")
public final class JiraAuthUiResource extends AbstractAtlassianAuthResource<JiraAttributes> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraAuthUiResource.class);

    private final JiraAttributes jiraAttributes;
    private final KeyValueStore keyValueStore;
    private final AuthorizationListener authorizationListener;

    @Inject
    public JiraAuthUiResource(JiraAttributes jiraAttributes, JiraService jiraService,
                              SessionManager sessionManager, RequestContext requestContext,
                              KeyValueStore keyValueStore,
                              AuthorizationListener authorizationListener,
                              JiraAuditService jiraAuditService) {
        super("jira", requestContext.getInvokerId(), sessionManager, jiraService,
                jiraAuditService.getAuditStore());
        this.jiraAttributes = jiraAttributes;
        this.keyValueStore = keyValueStore;
        this.authorizationListener = authorizationListener;
    }

    @Override
    protected JiraAttributes loadAttributes(String key) {
        return jiraAttributes;
    }

    @Override
    protected boolean saveAttributes(JiraAttributes attributes, String key) {
        // The validated candidate has already been applied in-memory via
        // BaseAtlassianAttributes#update; the Krista attribute screen remains
        // the system of record for invoker attributes.
        return true;
    }

    @Override
    protected void onOAuthCallbackSuccess(String refreshTokenKey, String accountId) {
        // The SDK passes the account id from the OAuth state as the second argument.
        // Mark THAT specific account authorized — not the callback's current browser
        // context — so the platform resumes the catalog request (e.g. Get Issues) that
        // is blocked on this account. The no-arg authorized() marks the wrong account,
        // so the pending request was never resumed after authorization completed.
        LOGGER.info("OAuth callback success — marking account authorized (accountId='{}')",
                AtlassianConstants.mask(accountId));
        try {
            if (accountId != null && !accountId.isBlank()) {
                authorizationListener.authorized(accountId);
            } else {
                authorizationListener.authorized();
            }
        } catch (Exception e) {
            LOGGER.warn("authorizationListener.authorized(accountId) failed; falling back to no-arg: {} ({})",
                    e.getMessage(), e.getClass().getSimpleName());
            try {
                authorizationListener.authorized();
            } catch (Exception ignored) {
                // Tokens are already stored; a manual re-invocation will still succeed.
            }
        }
    }

    @Override
    protected void removeTokens(JiraAttributes attributes) {
        // Restore credentialsMode from snapshot so isManagedCredentialsEnabled()
        // resolves correctly even when stale Basic Auth fields are present.
        restoreCredentialsModeFromSnapshot(attributes);
        if (attributes.areOAuthCredentialsPresent() || attributes.isManagedCredentialsEnabled()) {
            keyValueStore.remove(appWideTokenKey(attributes));
        }
    }
}
