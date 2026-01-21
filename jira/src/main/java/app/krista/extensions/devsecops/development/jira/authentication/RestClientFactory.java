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

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.ksdk.context.RequestContext;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Service
public class RestClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientFactory.class);

    private final JiraAttributes jiraAttributes;
    private final OAuth2RestClient oAuth2RestClient;
    private final RequestContext requestContext;
    private final Invoker invoker;

    @Inject
    public RestClientFactory(JiraAttributes jiraAttributes, OAuth2RestClient oAuth2RestClient,
                             RequestContext requestContext, Invoker invoker) {
        this.jiraAttributes = jiraAttributes;
        this.oAuth2RestClient = oAuth2RestClient;
        this.requestContext = requestContext;
        this.invoker = invoker;
    }

    public boolean invokeAsUser() {
        return requestContext.invokeAsUser();
    }

    public RestClient getInstance(boolean invokeAsUser) {
        RestClient restClient;
        // Prioritize Basic Auth if credentials are configured
        if (jiraAttributes.areBasicCredentialsPresent()) {
            LOGGER.debug("Auth Rest Client created (Basic Auth)");
            restClient = new AuthRestClient(invoker.getAttributes(), invokeAsUser, false);
        } else if (jiraAttributes.areOAuthCredentialsPresent()) {
            LOGGER.debug("OAuth Rest Client created (OAuth 2.0)");
            restClient = oAuth2RestClient;
        } else {
            throw new IllegalStateException("No authentication credentials configured. Please provide either Basic Auth (Username, Auth Token, Host URL) or OAuth 2.0 (Client ID, Client Secret) credentials.");
        }
        return restClient;
    }

    public RestClient getInstance() {
        return getInstance(requestContext.invokeAsUser());
    }


}
