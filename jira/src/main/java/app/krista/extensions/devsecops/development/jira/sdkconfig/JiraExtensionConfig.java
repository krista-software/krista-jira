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

package app.krista.extensions.devsecops.development.jira.sdkconfig;

import app.krista.sdk.atlassian.config.AtlassianProduct;
import app.krista.sdk.atlassian.registry.annotations.AtlassianExtension;
import app.krista.sdk.atlassian.registry.annotations.OAuthScope;

/**
 * Marker class carrying the Jira extension's metadata for the atlassian-sdk
 * extension registry. The declared scopes mirror
 * {@link app.krista.extensions.devsecops.development.jira.authentication.JiraService#scopes}.
 */
@AtlassianExtension(extensionType = "jira",
        product = AtlassianProduct.JIRA,
        jaxrsId = "jiraextn",
        displayName = "Jira Project Management")
@OAuthScope(value = "read:jira-user", description = "Read Jira user information",
        catalogRequests = {"Get Users Account Id", "Get User Account Information", "Get issues by user names"})
@OAuthScope(value = "read:jira-work", description = "Read Jira issues and work items",
        catalogRequests = {"Get Issue By Key", "Get issues", "Get user issues",
                "Get my issues by given statuses", "Get user issue by issue type",
                "Fetch by JQL query", "Get Results for Fetch Issues by JQL Query",
                "Get Custom Field Value", "Get issues by user names"})
@OAuthScope(value = "write:jira-work", description = "Create and update Jira issues",
        catalogRequests = {"Create an issue", "Report a bug", "Update an issue",
                "Update status of issue", "Update status of multiple issues",
                "Update priority of issue", "Assign a task to user", "Add a comment",
                "Add worklog", "Attach File to Ticket"})
@OAuthScope(value = "manage:jira-data-provider", description = "Manage webhooks and data providers",
        catalogRequests = {"Get Notification", "Check System Health", "Save Extension Configuration"})
@OAuthScope(value = "manage:jira-project", description = "Manage Jira projects, versions and releases",
        catalogRequests = {"Get release summary", "Assign Issue to Epic", "Assign Child Issue to Epic"})
@OAuthScope(value = "manage:jira-configuration", description = "Manage Jira global settings",
        catalogRequests = {"Save Extension Configuration"})
@OAuthScope(value = "manage:jira-webhook", description = "Manage Jira webhooks",
        catalogRequests = {"Get Notification"})
public final class JiraExtensionConfig {

    private JiraExtensionConfig() {
    }
}
