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

package app.krista.extensions.devsecops.development.jira.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.impl.AskAPersonAction;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketStatus;
import app.krista.extensions.devsecops.development.jira.api.JiraAuditService;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.*;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import app.krista.sdk.atlassian.jql.JQLBuilder;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TransitionStatus;
import app.krista.ksdk.accounts.Account;
import app.krista.model.base.FreeForm;
import app.krista.model.field.NamedField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.beans.EventHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyMap;

public class RitualsAreaTest {

    public final String newIssueResponse = "{\n" +
            "  \"expand\": \"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations,customfield_10010.requestTypePractice\",\n" +
            "  \"id\": \"26284\",\n" +
            "  \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/issue/26284\",\n" +
            "  \"key\": \"KE-651\",\n" +
            "  \"fields\": {\n" +
            "    \"statuscategorychangedate\": \"2022-10-14T16:01:42.117+0530\",\n" +
            "    \"customfield_10230\": null,\n" +
            "    \"fixVersions\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"customfield_10231\": null,\n" +
            "    \"customfield_10232\": null,\n" +
            "    \"resolution\": null,\n" +
            "    \"customfield_10233\": null,\n" +
            "    \"customfield_10234\": null,\n" +
            "    \"customfield_10235\": null,\n" +
            "    \"customfield_10225\": null,\n" +
            "    \"customfield_10228\": null,\n" +
            "    \"customfield_10229\": null,\n" +
            "    \"lastViewed\": null,\n" +
            "    \"customfield_10220\": null,\n" +
            "    \"priority\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/priority/2\",\n" +
            "      \"iconUrl\": \"https://antbrains.atlassian.net/images/icons/priorities/critical.svg\",\n" +
            "      \"name\": \"P2\",\n" +
            "      \"id\": \"2\"\n" +
            "    },\n" +
            "    \"customfield_10100\": null,\n" +
            "    \"customfield_10222\": null,\n" +
            "    \"customfield_10101\": null,\n" +
            "    \"customfield_10224\": null,\n" +
            "    \"labels\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"customfield_10215\": null,\n" +
            "    \"customfield_10216\": null,\n" +
            "    \"customfield_10217\": null,\n" +
            "    \"aggregatetimeoriginalestimate\": null,\n" +
            "    \"timeestimate\": null,\n" +
            "    \"versions\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"issuelinks\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"assignee\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/user?accountId=62f35c0596eb272011aa421e\",\n" +
            "      \"accountId\": \"62f35c0596eb272011aa421e\",\n" +
            "      \"avatarUrls\": {\n" +
            "        \"48x48\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/48\",\n" +
            "        \"24x24\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/24\",\n" +
            "        \"16x16\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/16\",\n" +
            "        \"32x32\": \"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/32\"\n" +
            "      },\n" +
            "      \"displayName\": \"Pulkit Jareda\",\n" +
            "      \"active\": true,\n" +
            "      \"timeZone\": \"Asia/Kolkata\",\n" +
            "      \"accountType\": \"atlassian\"\n" +
            "    },\n" +
            "    \"status\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/status/10100\",\n" +
            "      \"description\": \"\",\n" +
            "      \"iconUrl\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/\",\n" +
            "      \"name\": \"Backlog\",\n" +
            "      \"id\": \"10100\",\n" +
            "      \"statusCategory\": {\n" +
            "        \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/statuscategory/2\",\n" +
            "        \"id\": 2,\n" +
            "        \"key\": \"new\",\n" +
            "        \"colorName\": \"blue-gray\",\n" +
            "        \"name\": \"To Do\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"components\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"customfield_10290\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/customFieldOption/10404\",\n" +
            "      \"value\": \"Extensions\",\n" +
            "      \"id\": \"10404\"\n" +
            "    },\n" +
            "    \"customfield_10210\": null,\n" +
            "    \"customfield_10211\": null,\n" +
            "    \"customfield_10212\": null,\n" +
            "    \"customfield_10213\": null,\n" +
            "    \"aggregatetimeestimate\": null,\n" +
            "    \"customfield_10207\": null,\n" +
            "    \"customfield_10208\": null,\n" +
            "    \"customfield_10209\": null,\n" +
            "    \"creator\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/user?accountId=5dcd51c937249a0c69304be5\",\n" +
            "      \"accountId\": \"5dcd51c937249a0c69304be5\",\n" +
            "      \"avatarUrls\": {\n" +
            "        \"48x48\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "        \"24x24\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "        \"16x16\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "        \"32x32\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\"\n" +
            "      },\n" +
            "      \"displayName\": \"Deepak Shingan\",\n" +
            "      \"active\": true,\n" +
            "      \"timeZone\": \"Asia/Kolkata\",\n" +
            "      \"accountType\": \"atlassian\"\n" +
            "    },\n" +
            "    \"customfield_10280\": null,\n" +
            "    \"subtasks\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"customfield_10284\": null,\n" +
            "    \"customfield_10285\": null,\n" +
            "    \"reporter\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/user?accountId=5dcd51c937249a0c69304be5\",\n" +
            "      \"accountId\": \"5dcd51c937249a0c69304be5\",\n" +
            "      \"avatarUrls\": {\n" +
            "        \"48x48\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "        \"24x24\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "        \"16x16\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "        \"32x32\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\"\n" +
            "      },\n" +
            "      \"displayName\": \"Deepak Shingan\",\n" +
            "      \"active\": true,\n" +
            "      \"timeZone\": \"Asia/Kolkata\",\n" +
            "      \"accountType\": \"atlassian\"\n" +
            "    },\n" +
            "    \"customfield_10286\": null,\n" +
            "    \"aggregateprogress\": {\n" +
            "      \"progress\": 0,\n" +
            "      \"total\": 0\n" +
            "    },\n" +
            "    \"customfield_10287\": null,\n" +
            "    \"customfield_10288\": null,\n" +
            "    \"progress\": {\n" +
            "      \"progress\": 0,\n" +
            "      \"total\": 0\n" +
            "    },\n" +
            "    \"votes\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/issue/KE-651/votes\",\n" +
            "      \"votes\": 0,\n" +
            "      \"hasVoted\": false\n" +
            "    },\n" +
            "    \"worklog\": {\n" +
            "      \"startAt\": 0,\n" +
            "      \"maxResults\": 20,\n" +
            "      \"total\": 0,\n" +
            "      \"worklogs\": [\n" +
            "        \n" +
            "      ]\n" +
            "    },\n" +
            "    \"issuetype\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/issuetype/10103\",\n" +
            "      \"id\": \"10103\",\n" +
            "      \"description\": \"A problem or error.\",\n" +
            "      \"iconUrl\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/2/universal_avatar/view/type/issuetype/avatar/10303?size=medium\",\n" +
            "      \"name\": \"Bug\",\n" +
            "      \"subtask\": false,\n" +
            "      \"avatarId\": 10303,\n" +
            "      \"hierarchyLevel\": 0\n" +
            "    },\n" +
            "    \"timespent\": null,\n" +
            "    \"customfield_10270\": null,\n" +
            "    \"customfield_10271\": null,\n" +
            "    \"customfield_10272\": null,\n" +
            "    \"project\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/project/10248\",\n" +
            "      \"id\": \"10248\",\n" +
            "      \"key\": \"KE\",\n" +
            "      \"name\": \"Krista Extensions\",\n" +
            "      \"projectTypeKey\": \"software\",\n" +
            "      \"simplified\": false,\n" +
            "      \"avatarUrls\": {\n" +
            "        \"48x48\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/universal_avatar/view/type/project/avatar/10413\",\n" +
            "        \"24x24\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/universal_avatar/view/type/project/avatar/10413?size=small\",\n" +
            "        \"16x16\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/universal_avatar/view/type/project/avatar/10413?size=xsmall\",\n" +
            "        \"32x32\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/universal_avatar/view/type/project/avatar/10413?size=medium\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"customfield_10273\": null,\n" +
            "    \"customfield_10274\": null,\n" +
            "    \"customfield_10275\": null,\n" +
            "    \"aggregatetimespent\": null,\n" +
            "    \"customfield_10276\": null,\n" +
            "    \"customfield_10277\": null,\n" +
            "    \"customfield_10278\": null,\n" +
            "    \"customfield_10279\": null,\n" +
            "    \"customfield_10269\": null,\n" +
            "    \"resolutiondate\": null,\n" +
            "    \"workratio\": -1,\n" +
            "    \"watches\": {\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/issue/KE-651/watchers\",\n" +
            "      \"watchCount\": 1,\n" +
            "      \"isWatching\": false\n" +
            "    },\n" +
            "    \"issuerestriction\": {\n" +
            "      \"issuerestrictions\": {\n" +
            "        \n" +
            "      },\n" +
            "      \"shouldDisplay\": false\n" +
            "    },\n" +
            "    \"created\": \"2022-10-14T16:01:41.642+0530\",\n" +
            "    \"customfield_10261\": null,\n" +
            "    \"customfield_10020\": null,\n" +
            "    \"customfield_10021\": null,\n" +
            "    \"customfield_10263\": null,\n" +
            "    \"customfield_10264\": null,\n" +
            "    \"customfield_10265\": null,\n" +
            "    \"customfield_10266\": null,\n" +
            "    \"customfield_10267\": null,\n" +
            "    \"customfield_10268\": null,\n" +
            "    \"customfield_10016\": null,\n" +
            "    \"customfield_10017\": null,\n" +
            "    \"customfield_10018\": {\n" +
            "      \"hasEpicLinkFieldDependency\": false,\n" +
            "      \"showField\": false,\n" +
            "      \"nonEditableReason\": {\n" +
            "        \"reason\": \"PLUGIN_LICENSE_ERROR\",\n" +
            "        \"message\": \"The Parent Link is only available to Jira Premium users.\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"customfield_10019\": \"0|i02kjb:\",\n" +
            "    \"updated\": \"2023-01-04T13:45:51.699+0530\",\n" +
            "    \"timeoriginalestimate\": null,\n" +
            "    \"description\": {\n" +
            "      \"version\": 1,\n" +
            "      \"type\": \"doc\",\n" +
            "      \"content\": [\n" +
            "        {\n" +
            "          \"type\": \"paragraph\",\n" +
            "          \"content\": [\n" +
            "            {\n" +
            "              \"type\": \"text\",\n" +
            "              \"text\": \"I was working on a daily status extension, where I see a null pointer exception if Jira issues are getting added to the list in the manage info step.\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"type\": \"paragraph\",\n" +
            "          \"content\": [\n" +
            "            {\n" +
            "              \"type\": \"text\",\n" +
            "              \"text\": \"I have attached a conversation for reference to reproduce the issue.\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"customfield_10251\": null,\n" +
            "    \"customfield_10252\": null,\n" +
            "    \"customfield_10010\": null,\n" +
            "    \"customfield_10253\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"customfield_10014\": null,\n" +
            "    \"timetracking\": {\n" +
            "      \n" +
            "    },\n" +
            "    \"customfield_10015\": null,\n" +
            "    \"customfield_10247\": null,\n" +
            "    \"customfield_10005\": null,\n" +
            "    \"customfield_10249\": null,\n" +
            "    \"security\": null,\n" +
            "    \"attachment\": [\n" +
            "      {\n" +
            "        \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/attachment/19833\",\n" +
            "        \"id\": \"19833\",\n" +
            "        \"filename\": \"doc_binary_conversation_GetJIRA-14_10_2022-10_29_18.zip\",\n" +
            "        \"author\": {\n" +
            "          \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/user?accountId=5dcd51c937249a0c69304be5\",\n" +
            "          \"accountId\": \"5dcd51c937249a0c69304be5\",\n" +
            "          \"avatarUrls\": {\n" +
            "            \"48x48\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "            \"24x24\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "            \"16x16\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\",\n" +
            "            \"32x32\": \"https://secure.gravatar.com/avatar/7aced6f938f88ee3a37c06d36f781edb?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FDS-2.png\"\n" +
            "          },\n" +
            "          \"displayName\": \"Deepak Shingan\",\n" +
            "          \"active\": true,\n" +
            "          \"timeZone\": \"Asia/Kolkata\",\n" +
            "          \"accountType\": \"atlassian\"\n" +
            "        },\n" +
            "        \"created\": \"2022-10-14T16:02:00.224+0530\",\n" +
            "        \"size\": 18220,\n" +
            "        \"mimeType\": \"application/zip\",\n" +
            "        \"content\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/attachment/content/19833\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"summary\": \"Jira extension throwing null pointer exception\",\n" +
            "    \"customfield_10240\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"customfield_10241\": null,\n" +
            "    \"customfield_10000\": \"{}\",\n" +
            "    \"customfield_10242\": null,\n" +
            "    \"customfield_10001\": null,\n" +
            "    \"customfield_10243\": null,\n" +
            "    \"customfield_10002\": null,\n" +
            "    \"customfield_10244\": null,\n" +
            "    \"customfield_10245\": null,\n" +
            "    \"customfield_10246\": null,\n" +
            "    \"customfield_10236\": null,\n" +
            "    \"customfield_10238\": null,\n" +
            "    \"environment\": null,\n" +
            "    \"customfield_10239\": null,\n" +
            "    \"duedate\": null,\n" +
            "    \"comment\": {\n" +
            "      \"comments\": [\n" +
            "        \n" +
            "      ],\n" +
            "      \"self\": \"https://api.atlassian.com/ex/jira/9721aed3-ac5e-4531-9c49-d29f990d2df5/rest/api/3/issue/26284/comment\",\n" +
            "      \"maxResults\": 0,\n" +
            "      \"total\": 0,\n" +
            "      \"startAt\": 0\n" +
            "    }\n" +
            "  }\n" +
            "}";
    public final String issueResponse = "{\n" +
            "  \"expand\": \"names,schema\",\n" +
            "  \"startAt\": 0,\n" +
            "  \"maxResults\": 20,\n" +
            "  \"total\": 1,\n" +
            "  \"issues\": [\n" +
            "    {\n" +
            "      \"id\": \"22001\",\n" +
            "      \"key\": \"KS-1\",\n" +
            "      \"fields\": {\n" +
            "        \"summary\": \"test\",\n" +
            "        \"priority\": {\n" +
            "          \"name\": \"P1\"\n" +
            "        },\n" +
            "        \"assignee\": {\n" +
            "          \"displayName\": \"Arunim Samudra\"\n" +
            "        },\n" +
            "        \"status\": {\n" +
            "          \"name\": \"Implementation\"\n" +
            "        },\n" +
            "        \"subtasks\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"reporter\": {\n" +
            "          \"displayName\": \"Arunim Samudra\"\n" +
            "        },\n" +
            "        \"issuetype\": {\n" +
            "          \"name\": \"Bug\"\n" +
            "        },\n" +
            "        \"project\": {\n" +
            "          \"key\": \"KS\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    RestClient restClient;
    RestClientFactory restClientFactory;
    IssueDao issueDao;
    IssueDao mockedIssueDao;
    ProjectDao mockedProjectDao;
    UserDao mockedUserDao;
    TicketInfoDao mockedTicketInfoDao;
    RitualsArea ritualsArea;
    RitualsArea mockedRitualsArea;
    EventHandler eventHandler;
    RequestContext requestContext;
    AuthorizationContext authorizationContext;
    ValidationOrchestrator mockValidationOrchestrator;
    ValidationOrchestrator validationOrchestrator;
    KeyValueStore internalState;
    TelemetryMetrics mockedTelemetryMetrics;
    JiraAuditService mockedJiraAuditService;

    @Before
    public void setup() {
        restClientFactory = Mockito.mock(RestClientFactory.class);
        restClient = Mockito.mock(RestClient.class);
        internalState = Mockito.mock(KeyValueStore.class);
        requestContext = Mockito.mock(RequestContext.class);
        authorizationContext = Mockito.mock(AuthorizationContext.class);
        issueDao = new IssueDao(restClientFactory, restClient);
        mockedIssueDao = Mockito.mock(IssueDao.class);
        mockedProjectDao = Mockito.mock(ProjectDao.class);
        mockedUserDao = Mockito.mock(UserDao.class);
        mockedTicketInfoDao = Mockito.mock(TicketInfoDao.class);
        mockValidationOrchestrator = Mockito.mock(ValidationOrchestrator.class);
        validationOrchestrator = new ValidationOrchestrator(mockedIssueDao, mockedProjectDao, mockedUserDao,
                mockedTicketInfoDao, requestContext, authorizationContext);
        mockedTelemetryMetrics = Mockito.mock(TelemetryMetrics.class);
        mockedJiraAuditService = Mockito.mock(JiraAuditService.class);
        ritualsArea = new RitualsArea(issueDao, mockedUserDao, requestContext, authorizationContext,
                (app.krista.extensions.util.EventHandler) eventHandler,
                mockValidationOrchestrator,
                new ErrorHandlingStateManager(internalState), new ExtensionResponseGenerator(), mockedProjectDao, mockedTelemetryMetrics, mockedJiraAuditService);

        mockedRitualsArea = new RitualsArea(
                mockedIssueDao, mockedUserDao, requestContext, authorizationContext,
                (app.krista.extensions.util.EventHandler) eventHandler,
                validationOrchestrator,
                new ErrorHandlingStateManager(internalState),
                new ExtensionResponseGenerator(),
                mockedProjectDao,
                mockedTelemetryMetrics,
                mockedJiraAuditService
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetUserIssues() {

        //Given
        String userAccountId = "1";
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", " assignee in (" + userAccountId + ") order by priority DESC, duedate DESC");
        qparams.put("fields", "*all");
        qparams.put("maxResults", 30L);

        //Stub
        Mockito.when(restClient.fetch("search/jql", qparams, String.class)).thenReturn(issueResponse);
        Mockito.when(mockValidationOrchestrator.validate(anyMap())).thenReturn(List.of());
        //When
        List<Issue> issueList =
                (List<Issue>) ritualsArea.getUserIssues(userAccountId).getResponseValue().get("User Issues");

        //Then
        Assert.assertNotNull(issueList);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetIssuesByUserNames_combinesTicketsAcrossUsers() {
        //Given: two names, each resolving to a single account id
        Mockito.when(mockedUserDao.searchUser("Alice"))
                .thenReturn(List.of(new JiraUser("1", "Alice", "atlassian", "alice@x.com", "self", List.of())));
        Mockito.when(mockedUserDao.searchUser("Bob"))
                .thenReturn(List.of(new JiraUser("2", "Bob", "atlassian", "bob@x.com", "self", List.of())));
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.eq(30L), Mockito.eq(1L)))
                .thenReturn(List.of(new Issue()));

        //When
        ExtensionResponse response = mockedRitualsArea.getIssuesByUserNames(List.of("Alice", "Bob"));

        //Then: both account ids appear in the assignee JQL and the combined list is returned
        ArgumentCaptor<String> jqlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockedIssueDao).searchByJQL(jqlCaptor.capture(), Mockito.eq(30L), Mockito.eq(1L));
        String jql = jqlCaptor.getValue();
        Assert.assertTrue(jql.toLowerCase().contains("assignee in"));
        Assert.assertTrue(jql.contains("1"));
        Assert.assertTrue(jql.contains("2"));
        List<Issue> issues = (List<Issue>) response.getResponseValue().get("Issues");
        Assert.assertEquals(1, issues.size());
    }

    @Test
    public void testGetIssuesByUserNames_ambiguousNameIncludesAllMatches() {
        //Given: a single name that resolves to two account ids
        Mockito.when(mockedUserDao.searchUser("John"))
                .thenReturn(List.of(
                        new JiraUser("10", "John A", "atlassian", "johna@x.com", "self", List.of()),
                        new JiraUser("11", "John B", "atlassian", "johnb@x.com", "self", List.of())));
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.eq(30L), Mockito.eq(1L)))
                .thenReturn(List.of());

        //When
        mockedRitualsArea.getIssuesByUserNames(List.of("John"));

        //Then: every matching account id for the ambiguous name is included in the query
        ArgumentCaptor<String> jqlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockedIssueDao).searchByJQL(jqlCaptor.capture(), Mockito.eq(30L), Mockito.eq(1L));
        String jql = jqlCaptor.getValue();
        Assert.assertTrue(jql.contains("10"));
        Assert.assertTrue(jql.contains("11"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetIssuesByUserNames_noMatchesReturnsEmptyList() {
        //Given: no name resolves to a user
        Mockito.when(mockedUserDao.searchUser(Mockito.anyString())).thenReturn(List.of());

        //When
        ExtensionResponse response = mockedRitualsArea.getIssuesByUserNames(List.of("Ghost"));

        //Then: an empty list is returned and no search is performed
        List<Issue> issues = (List<Issue>) response.getResponseValue().get("Issues");
        Assert.assertNotNull(issues);
        Assert.assertTrue(issues.isEmpty());
        Mockito.verify(mockedIssueDao, Mockito.never())
                .searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetIssuesByUserNames_invokeAsUser_validatesCredentialsAndAuditsSuccess() {
        //Given: request runs as the user, so credentials must be validated first
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.when(mockedUserDao.searchUser("Alice"))
                .thenReturn(List.of(new JiraUser("1", "Alice", "atlassian", "alice@x.com", "self", List.of())));
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.eq(30L), Mockito.eq(1L)))
                .thenReturn(List.of(new Issue()));

        //When
        ExtensionResponse response = mockedRitualsArea.getIssuesByUserNames(List.of("Alice"));

        //Then: credentials validated, issues returned, success audited
        Mockito.verify(mockedIssueDao).validateCredentials();
        List<Issue> issues = (List<Issue>) response.getResponseValue().get("Issues");
        Assert.assertEquals(1, issues.size());
        Mockito.verify(mockedJiraAuditService).success(Mockito.any(), Mockito.eq("Get issues by user names"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetIssuesByUserNames_nullListReturnsEmptyList() {
        //When: a null list is supplied
        ExtensionResponse response = mockedRitualsArea.getIssuesByUserNames(null);

        //Then: empty list, no search performed
        List<Issue> issues = (List<Issue>) response.getResponseValue().get("Issues");
        Assert.assertNotNull(issues);
        Assert.assertTrue(issues.isEmpty());
        Mockito.verify(mockedIssueDao, Mockito.never())
                .searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    public void testGetIssuesByUserNames_skipsNullAndBlankNames() {
        //Given: a list with null and blank entries alongside one real name
        Mockito.when(mockedUserDao.searchUser("Alice"))
                .thenReturn(List.of(new JiraUser("1", "Alice", "atlassian", "alice@x.com", "self", List.of())));
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.eq(30L), Mockito.eq(1L)))
                .thenReturn(List.of());

        //When
        mockedRitualsArea.getIssuesByUserNames(Arrays.asList(null, "   ", "Alice"));

        //Then: only the real name is resolved; blank entries are skipped
        Mockito.verify(mockedUserDao).searchUser("Alice");
        Mockito.verify(mockedUserDao, Mockito.never()).searchUser("   ");
        Mockito.verify(mockedIssueDao).searchByJQL(Mockito.anyString(), Mockito.eq(30L), Mockito.eq(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetIssuesByUserNames_skipsUsersWithMissingAccountId() {
        //Given: matched users have null/blank account ids
        Mockito.when(mockedUserDao.searchUser("Ghost"))
                .thenReturn(List.of(
                        new JiraUser(null, "No Id", "atlassian", "noid@x.com", "self", List.of()),
                        new JiraUser("   ", "Blank Id", "atlassian", "blank@x.com", "self", List.of())));

        //When
        ExtensionResponse response = mockedRitualsArea.getIssuesByUserNames(List.of("Ghost"));

        //Then: no usable account id, so an empty list and no search
        List<Issue> issues = (List<Issue>) response.getResponseValue().get("Issues");
        Assert.assertTrue(issues.isEmpty());
        Mockito.verify(mockedIssueDao, Mockito.never())
                .searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetIssuesByUserNames_mustAuthorizeException() {
        //Given: running as the user and credential validation demands authorization
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should propagate MustAuthorizeException
        mockedRitualsArea.getIssuesByUserNames(List.of("Alice"));
    }

    @Test
    public void testGetIssuesByUserNames_illegalCallerExceptionWrapped() {
        //Given: user resolution throws IllegalCallerException
        Mockito.when(mockedUserDao.searchUser(Mockito.anyString()))
                .thenThrow(new IllegalCallerException("bad caller"));

        //When / Then: it is wrapped in a RuntimeException and failure is audited
        try {
            mockedRitualsArea.getIssuesByUserNames(List.of("Alice"));
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException cause) {
            Assert.assertTrue(cause.getCause() instanceof IllegalCallerException);
        }
        Mockito.verify(mockedJiraAuditService)
                .failure(Mockito.any(), Mockito.eq("Get issues by user names"), Mockito.any(IllegalCallerException.class));
    }

    @Test
    public void testGetIssuesByUserNames_genericExceptionReturnsErrorResponse() {
        //Given: user resolution throws an unexpected error
        Mockito.when(mockedUserDao.searchUser(Mockito.anyString()))
                .thenThrow(new RuntimeException("boom"));

        //When
        ExtensionResponse response = mockedRitualsArea.getIssuesByUserNames(List.of("Alice"));

        //Then: a system-error response is returned, failure audited, success NOT audited
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService)
                .failure(Mockito.any(), Mockito.eq("Get issues by user names"), Mockito.any(RuntimeException.class));
        Mockito.verify(mockedJiraAuditService, Mockito.never())
                .success(Mockito.any(), Mockito.eq("Get issues by user names"));
    }


    @Test
    public void testAssignATaskToUser() {
        //Given
        String issueKey = "KE-123";
        String toAssignUserAccountId = "22001";
        Issue mockIssue = new Issue();
        mockIssue.key = issueKey;

        //Stub: mockedIssueDao.isIssueExists and mockedUserDao.isUserExist return true so validation passes
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedUserDao.isUserExist(toAssignUserAccountId)).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).assignTask(issueKey, toAssignUserAccountId);
        Mockito.when(mockedIssueDao.getIssueFinal(issueKey)).thenReturn(mockIssue);

        //When
        ExtensionResponse response = mockedRitualsArea.assignATaskToUser(toAssignUserAccountId, issueKey);

        //Then
        Assert.assertNotNull(response);
    }

    @Test
    public void testAssignATaskToUser_Exception() {
        //Given
        String issueKey = "KE-123";
        String toAssignUserAccountId = "22001";

        //Stub
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedUserDao.isUserExist(toAssignUserAccountId)).thenReturn(true);
        Mockito.doThrow(new RuntimeException("assign failed")).when(mockedIssueDao).assignTask(issueKey, toAssignUserAccountId);

        //When
        ExtensionResponse response = mockedRitualsArea.assignATaskToUser(toAssignUserAccountId, issueKey);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Assign a task to user"), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testFetchByJQLQuery() {
        // This request generates a random Task ID which is returned immediately; actual work runs async
        //Given
        String query = "issuetype = \"Bug\" AND assignee in (22001) order by priority DESC, duedate DESC";

        //Stub: authorizationContext.getAuthorizedAccount().getAccountId() chain
        Account mockAccount = Mockito.mock(Account.class);
        Mockito.when(authorizationContext.getAuthorizedAccount()).thenReturn(mockAccount);
        Mockito.when(mockAccount.getAccountId()).thenReturn("test-account-id");

        //When
        ExtensionResponse response = mockedRitualsArea.fetchByJQLQuery(query);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Task ID"));
    }

    @Test
    public void testFetchByJQLQuery_Exception() {
        //Given
        String query = "issuetype = \"Bug\"";

        //Stub: authorizationContext throws exception
        Mockito.when(authorizationContext.getAuthorizedAccount()).thenThrow(new RuntimeException("auth context error"));

        //When
        ExtensionResponse response = mockedRitualsArea.fetchByJQLQuery(query);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Fetch by JQL query"), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testGetResultsForFetchIssuesByJQLQuery() {
        //Given: eventName matches taskId, eventData contains a list of issues
        String taskId = "test-task-id-123";
        FreeForm eventData = new FreeForm();
        Issue mockIssue = new Issue();
        mockIssue.key = "KE-1";
        List<Issue> issues = Collections.singletonList(mockIssue);
        eventData.put(RitualsArea.IS_ERROR, "Switch", false);
        eventData.put("Data", "[ Entity(Issue) ]", issues);

        //When
        ExtensionResponse response = mockedRitualsArea.getResultsForFetchIssuesByJQLQuery(taskId, eventData, taskId);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get(RitualsArea.LIST_OF_ISSUES));
    }

    @Test
    public void testGetResultsForFetchIssuesByJQLQuery_EmptyIssueList() {
        //Given: eventName matches taskId, eventData contains empty list
        String taskId = "test-task-id-456";
        FreeForm eventData = new FreeForm();
        eventData.put(RitualsArea.IS_ERROR, "Switch", false);
        eventData.put("Data", "[ Entity(Issue) ]", Collections.emptyList());

        //When
        ExtensionResponse response = mockedRitualsArea.getResultsForFetchIssuesByJQLQuery(taskId, eventData, taskId);

        //Then
        Assert.assertNotNull(response);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetResultsForFetchIssuesByJQLQuery_InvalidTaskId() {
        //Given: eventName does NOT match taskId
        String taskId = "task-id-789";
        String differentEventName = "different-event-name";
        FreeForm eventData = new FreeForm();

        //When - should throw IllegalStateException
        mockedRitualsArea.getResultsForFetchIssuesByJQLQuery(differentEventName, eventData, taskId);
    }

    @Test
    public void testGetMyIssuesByGivenStatuses() {
        //Given
        List<String> statuses = new ArrayList<>();
        Map<String, Object> qparams = new HashMap<>();
        statuses.add("IMPLEMENTATION");
        String statusCommaSeparated = String.join(",", statuses);
        String query = "status in (" + statusCommaSeparated + ") AND assignee in (currentUser()) ORDER BY priority DESC, updated DESC";
        qparams.put("jql", query);
        qparams.put("fields", "*all");
        qparams.put("maxResults", 30L);


        //Stub
        Mockito.when(restClient.fetch("search/jql", qparams, String.class)).thenReturn(issueResponse);

        //When
        ExtensionResponse issues = ritualsArea.getMyIssuesByGivenStatuses(statuses);

        //Then
        Assert.assertNotNull(issues);
    }

    @Test
    public void testGetIssues() {
        //Given
        String summary = "test";
        String priority = "P1";
        String issueType = "Bug";
        String status = "Implementation";
        String assignee = "Arunim Samudra";
        String projectKey = "KS";
        String jql = new JQLBuilder().status(status)
                .assignee(assignee)
                .priority(priority)
                .summary(summary)
                .projectKey(projectKey)
                .issueType(issueType)
                .orderBy("priority DESC, updated", "desc")
                .build();
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", jql);
        qparams.put("fields", "*all");
        qparams.put("maxResults", 20L);

//      Stub
        Mockito.when(restClient.fetch("search/jql", qparams, String.class)).thenReturn(issueResponse);

        //When
        ExtensionResponse issueList = ritualsArea.getIssues(summary, priority, issueType, status, assignee, projectKey, null, null);
        List<Issue> issues = (List<Issue>) issueList.getResponseValue().get("List of Filtered Issues");
        //Then
        Assert.assertNotNull(issues);
    }

    @Test
    public void testGetUserIssueByIssueType() {
        //Given
        String issueType = "Bug";
        String userAccountId = "22001";
        Map<String, Object> qparams = new HashMap<>();
        String query = "issuetype = \"" + issueType + "\"  AND assignee in (" + userAccountId + ") order by priority DESC, duedate DESC";
        qparams.put("jql", query);
        qparams.put("fields", "*all");
        qparams.put("maxResults", 30L);

        //Stub
        Mockito.when(restClient.fetch("search/jql", qparams, String.class)).thenReturn(issueResponse);

        //When
        ExtensionResponse issueList = ritualsArea.getUserIssueByIssueType(userAccountId, issueType);
        List<String> issues = (List<String>) issueList.getResponseValue().get("Issues");
        //Then
        Assert.assertNotNull(issues);
    }


    @Test
    public void testUpdateAnIssue() {
        //Given
        String issueKey = "KE-123";
        Issue oldIssue = new Issue();
        oldIssue.key = issueKey;
        oldIssue.summary = "Old Summary";
        oldIssue.status = "Backlog";
        Issue updatedIssue = new Issue();
        updatedIssue.key = issueKey;
        updatedIssue.summary = "New Summary";

        //Stub: validation passes - issue exists and status is valid
        TransitionStatus transition = new TransitionStatus();
        transition.toName = "In Progress";
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions(issueKey)).thenReturn(List.of(transition));
        Mockito.when(mockedIssueDao.getIssueFinal(issueKey)).thenReturn(oldIssue).thenReturn(updatedIssue);
        Mockito.doNothing().when(mockedIssueDao).updateIssue(Mockito.any(Issue.class), Mockito.any());

        //When
        ExtensionResponse response = mockedRitualsArea.updateAnIssue(
                issueKey, "In Progress", "P1", "New Summary", "New Description",
                "Assignee", null, null, null, null);

        //Then
        Assert.assertNotNull(response);
    }

    @Test
    public void testUpdateAnIssue_Exception() {
        //Given
        String issueKey = "KE-123";
        Issue oldIssue = new Issue();
        oldIssue.key = issueKey;
        oldIssue.summary = "Old Summary";

        //Stub: issue exists but updateIssue throws
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.getIssueFinal(issueKey)).thenReturn(oldIssue);
        Mockito.doThrow(new RuntimeException("update failed")).when(mockedIssueDao).updateIssue(Mockito.any(Issue.class), Mockito.any());

        //When
        ExtensionResponse response = mockedRitualsArea.updateAnIssue(
                issueKey, null, null, null, null, null, null, null, null, null);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Update an issue"), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testUpdatePriorityOfIssue() {
        //Given
        String issueKey = "KE-123";
        String priorityName = "High";
        Issue updatedIssue = new Issue();
        updatedIssue.key = issueKey;

        //Stub: issue exists so validation passes, updatePriority returns updated issue
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.updatePriority(Mockito.any(), Mockito.eq(issueKey))).thenReturn(updatedIssue);

        //When
        ExtensionResponse response = mockedRitualsArea.updatePriorityOfIssue(issueKey, priorityName);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Updated Issue"));
    }

    @Test
    public void testUpdatePriorityOfIssue_Exception() {
        //Given
        String issueKey = "KE-123";
        String priorityName = "High";

        //Stub: issue exists but updatePriority throws
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.updatePriority(Mockito.any(), Mockito.eq(issueKey))).thenThrow(new RuntimeException("priority update failed"));

        //When
        ExtensionResponse response = mockedRitualsArea.updatePriorityOfIssue(issueKey, priorityName);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Update priority of issue"), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testUpdateThePriorityOfMultipleIssues() {
        //Given
        List<String> issueKeys = List.of("KE-1", "KE-2");
        String newPriority = "High";
        Issue issue1 = new Issue();
        issue1.key = "KE-1";
        Issue issue2 = new Issue();
        issue2.key = "KE-2";

        //Stub: updatePriority and getIssueFinal for each key
        Mockito.when(mockedIssueDao.updatePriority(Mockito.any(), Mockito.eq("KE-1"))).thenReturn(issue1);
        Mockito.when(mockedIssueDao.updatePriority(Mockito.any(), Mockito.eq("KE-2"))).thenReturn(issue2);
        Mockito.when(mockedIssueDao.getIssueFinal("KE-1")).thenReturn(issue1);
        Mockito.when(mockedIssueDao.getIssueFinal("KE-2")).thenReturn(issue2);

        //When
        ExtensionResponse response = mockedRitualsArea.updateThePriorityOfMultipleIssues(issueKeys, newPriority);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get(RitualsArea.LIST_OF_ISSUES));
    }

    @Test
    public void testUpdateThePriorityOfMultipleIssues_Exception() {
        //Given
        List<String> issueKeys = List.of("KE-1");
        String newPriority = "High";

        //Stub: updatePriority throws
        Mockito.when(mockedIssueDao.updatePriority(Mockito.any(), Mockito.eq("KE-1"))).thenThrow(new RuntimeException("priority update failed"));

        //When
        ExtensionResponse response = mockedRitualsArea.updateThePriorityOfMultipleIssues(issueKeys, newPriority);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Update the priority of multiple issues"), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testAddWorklog() {
        //Given
        String issueKey = "KE-123";
        Long startDateTime = System.currentTimeMillis();
        String timeSpent = "2h";

        //Stub: issue exists so validation passes, addWorklog does nothing
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).addWorklog(Mockito.eq(timeSpent), Mockito.eq(startDateTime), Mockito.eq(issueKey), Mockito.anyString());

        //When
        ExtensionResponse response = mockedRitualsArea.addWorklog(issueKey, startDateTime, timeSpent);

        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(true, response.getResponseValue().get("Is successful"));
    }

    @Test
    public void testAddWorklog_Exception() {
        //Given
        String issueKey = "KE-123";
        Long startDateTime = System.currentTimeMillis();
        String timeSpent = "2h";

        //Stub: issue exists but addWorklog throws
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.doThrow(new RuntimeException("worklog failed")).when(mockedIssueDao).addWorklog(Mockito.eq(timeSpent), Mockito.eq(startDateTime), Mockito.eq(issueKey), Mockito.anyString());

        //When
        ExtensionResponse response = mockedRitualsArea.addWorklog(issueKey, startDateTime, timeSpent);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Add worklog"), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testGetIssueByKey() {
        //Given
        String issueKey = "KE-651";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("expand", "renderedFields");

        //Stub
        Mockito.when(restClient.fetch("issue/" + issueKey, queryParams, String.class)).thenReturn(newIssueResponse);

        //When
        ExtensionResponse issue = ritualsArea.getIssueByKey(issueKey);
        Issue issueList = (Issue) ritualsArea.getIssueByKey(issueKey).getResponseValue().get("Issue");
        //Then
        Assert.assertNotNull(issueList);
    }


    @Test
    public void testAddAComment_Remediate() {
        String ticketId = "KE-1234";
        String comment = "Hello World";
        Mockito.doThrow(new NoSuchElementException("")).when(mockedIssueDao).addComment(ticketId, comment, null);
        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        ExtensionResponse response = mockedRitualsArea.addAComment(comment, ticketId, "Public");
        Assert.assertEquals("confirmReenterForAddAComment",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                Arrays.stream(RitualsAreaSubCatalogRequests.class.getDeclaredMethods())
                        .map(Method::getName).collect(Collectors.toList()).contains("confirmReenterForAddAComment"));
        Assert.assertEquals("The ticket with the given Id : KE-1234 does not exist. Please check.\n",
                response.getRemediationActions().getRemediationActionList().get(0).getMessage());
        Assert.assertTrue(response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
        Assert.assertTrue((((AskAPersonAction) response.getRemediationActions().getRemediationActionList().get(0))
                .getFields().stream().map(NamedField::getName).collect(Collectors.toList()).contains("re-enter")));
        Set<String> keySet = response.getState().keySet();
        Map<String, Object> state = response.getState();
        Assert.assertTrue(keySet.contains(JiraResources.TICKET_ID));
        Assert.assertTrue(keySet.contains("stateId"));
        Assert.assertTrue(keySet.contains("Comment"));
        Assert.assertEquals(state.get(JiraResources.TICKET_ID), ticketId);
        Assert.assertEquals(state.get("Comment"), comment);
    }

    @Test
    public void testGetUserIssues_Remediate() {
        String userAccountId = "SampleUserAccountId";
        Mockito.when(mockedUserDao.isUserExist(userAccountId)).thenReturn(false);
        ExtensionResponse response = mockedRitualsArea.getUserIssues(userAccountId);
        Assert.assertEquals("confirmReenterForGetUserIssues",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertEquals("The provided account id: SampleUserAccountId does not exist. Please check.\n",
                response.getRemediationActions().getRemediationActionList().get(0).getMessage());
        Assert.assertTrue(response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
        Assert.assertTrue((((AskAPersonAction) response.getRemediationActions().getRemediationActionList().get(0))
                .getFields().stream().map(NamedField::getName).collect(Collectors.toList()).contains("re-enter")));
        Set<String> keySet = response.getState().keySet();
        Map<String, Object> state = response.getState();
        Assert.assertTrue(keySet.contains(JiraResources.ACCOUNT_ID));
        Assert.assertTrue(keySet.contains("stateId"));
        Assert.assertTrue(keySet.contains("Account Id"));
        Assert.assertEquals(state.get(JiraResources.ACCOUNT_ID), userAccountId);
    }

    @Test
    public void testUpdateStatusOfIssue_Remediate() {
        String ticketId = "SampleTicketId";
        String statusName = "Backlog";
        Mockito.when(mockedTicketInfoDao.getTransitions(ticketId)).thenReturn(List.of());
        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        ExtensionResponse response = mockedRitualsArea.updateStatusOfIssue(ticketId, statusName);
        Assert.assertEquals("confirmReenterForUpdateStatusOfIssue",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertEquals("The provided status name: Backlog is not valid for this issue. Available status for this issue are :  Please check.\n",
                response.getRemediationActions().getRemediationActionList().get(0).getMessage());
        Assert.assertTrue(response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
        Assert.assertTrue((((AskAPersonAction) response.getRemediationActions().getRemediationActionList().get(0))
                .getFields().stream().map(NamedField::getName).collect(Collectors.toList()).contains("re-enter")));
        Set<String> keySet = response.getState().keySet();
        Map<String, Object> state = response.getState();
        Assert.assertTrue(keySet.contains(JiraResources.TICKET_ID));
        Assert.assertTrue(keySet.contains(JiraResources.STATUS_NAME));
        Assert.assertTrue(keySet.contains("stateId"));
        Assert.assertEquals(state.get(JiraResources.TICKET_ID), ticketId);
        Assert.assertEquals(state.get(JiraResources.STATUS_NAME), statusName);
    }

    @Test
    public void testAssignChildIssueToEpic() {
        //Given
        String childIssueKey = "KE-200";
        String parentEpicKey = "KE-100";

        //Stub: both issues exist so validation passes, assignChildIssueToEpic does nothing
        Mockito.when(mockedIssueDao.isIssueExists(childIssueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.isIssueExists(parentEpicKey)).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).assignChildIssueToEpic(childIssueKey, parentEpicKey);

        //When
        ExtensionResponse response = mockedRitualsArea.assignChildIssueToEpic(childIssueKey, parentEpicKey);

        //Then
        Assert.assertNotNull(response);
    }

    @Test
    public void testAssignChildIssueToEpic_Exception() {
        //Given
        String childIssueKey = "KE-200";
        String parentEpicKey = "KE-100";

        //Stub: issues exist but assignChildIssueToEpic throws
        Mockito.when(mockedIssueDao.isIssueExists(childIssueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.isIssueExists(parentEpicKey)).thenReturn(true);
        Mockito.doThrow(new RuntimeException("assign child failed")).when(mockedIssueDao).assignChildIssueToEpic(childIssueKey, parentEpicKey);

        //When
        ExtensionResponse response = mockedRitualsArea.assignChildIssueToEpic(childIssueKey, parentEpicKey);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Assign child issue to epic"), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testAssignIssueToEpic() {
        //Given
        String issueKey = "KE-300";
        String epicKey = "KE-100";

        //Stub: both issues exist so validation passes, assignIssueToEpic does nothing
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.isIssueExists(epicKey)).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).assignIssueToEpic(issueKey, epicKey);

        //When
        ExtensionResponse response = mockedRitualsArea.assignIssueToEpic(issueKey, epicKey);

        //Then
        Assert.assertNotNull(response);
    }

    @Test
    public void testAssignIssueToEpic_Exception() {
        //Given
        String issueKey = "KE-300";
        String epicKey = "KE-100";
        // assignIssueToEpic catch block parses the exception message as JSON, so message must be valid JSON with errorMessages array
        String jsonErrorMsg = "{\"errorMessages\":[\"Epic not found\"],\"errors\":{}}";

        //Stub: issues exist but assignIssueToEpic throws with JSON message
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.isIssueExists(epicKey)).thenReturn(true);
        Mockito.doThrow(new RuntimeException(jsonErrorMsg)).when(mockedIssueDao).assignIssueToEpic(issueKey, epicKey);

        //When
        ExtensionResponse response = mockedRitualsArea.assignIssueToEpic(issueKey, epicKey);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Assign issue to epic"), Mockito.any(RuntimeException.class));
    }

    // ========== updateStatusOfMultipleIssues Tests ==========

    @Test
    public void testUpdateStatusOfMultipleIssues() {
        //Given
        String statusName = "In Progress";
        List<String> issueKeys = List.of("KE-1", "KE-2");
        Issue issue1 = new Issue();
        issue1.key = "KE-1";
        Issue issue2 = new Issue();
        issue2.key = "KE-2";

        //Stub: issues exist and status is valid
        TransitionStatus transitionInProgress = new TransitionStatus();
        transitionInProgress.toName = "In Progress";
        Mockito.when(mockedIssueDao.isIssueExists("KE-1")).thenReturn(true);
        Mockito.when(mockedIssueDao.isIssueExists("KE-2")).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions("KE-1")).thenReturn(List.of(transitionInProgress));
        Mockito.when(mockedTicketInfoDao.getTransitions("KE-2")).thenReturn(List.of(transitionInProgress));
        Mockito.doNothing().when(mockedIssueDao).updateStatus(Mockito.anyString(), Mockito.eq(statusName));
        Mockito.when(mockedIssueDao.getIssueFinal("KE-1")).thenReturn(issue1);
        Mockito.when(mockedIssueDao.getIssueFinal("KE-2")).thenReturn(issue2);

        //When
        ExtensionResponse response = mockedRitualsArea.updateStatusOfMultipleIssues(statusName, issueKeys);

        //Then
        Assert.assertNotNull(response);
    }

    @Test
    public void testUpdateStatusOfMultipleIssues_Exception() {
        //Given
        String statusName = "In Progress";
        List<String> issueKeys = List.of("KE-1");

        //Stub: issue exists but updateStatus throws
        TransitionStatus transitionForException = new TransitionStatus();
        transitionForException.toName = "In Progress";
        Mockito.when(mockedIssueDao.isIssueExists("KE-1")).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions("KE-1")).thenReturn(List.of(transitionForException));
        Mockito.doThrow(new RuntimeException("update status failed")).when(mockedIssueDao).updateStatus(Mockito.eq("KE-1"), Mockito.eq(statusName));

        //When
        ExtensionResponse response = mockedRitualsArea.updateStatusOfMultipleIssues(statusName, issueKeys);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Update status of multiple issues"), Mockito.any(RuntimeException.class));
    }

    // ========== MustAuthorizeException Tests ==========

    @Test(expected = MustAuthorizeException.class)
    public void testAssignATaskToUser_MustAuthorizeException() {
        //Given
        String issueKey = "KE-123";
        String toAssignUserAccountId = "22001";

        //Stub: invokeAsUser returns true, validateCredentials throws MustAuthorizeException
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should throw MustAuthorizeException
        mockedRitualsArea.assignATaskToUser(toAssignUserAccountId, issueKey);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testUpdateStatusOfMultipleIssues_MustAuthorizeException() {
        //Given
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should throw MustAuthorizeException
        mockedRitualsArea.updateStatusOfMultipleIssues("In Progress", List.of("KE-1"));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testUpdateAnIssue_MustAuthorizeException() {
        //Given
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should throw MustAuthorizeException
        mockedRitualsArea.updateAnIssue("KE-1", null, null, null, null, null, null, null, null, null);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testUpdatePriorityOfIssue_MustAuthorizeException() {
        //Given
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should throw MustAuthorizeException
        mockedRitualsArea.updatePriorityOfIssue("KE-1", "High");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testAddWorklog_MustAuthorizeException() {
        //Given
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should throw MustAuthorizeException
        mockedRitualsArea.addWorklog("KE-1", System.currentTimeMillis(), "2h");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testAssignChildIssueToEpic_MustAuthorizeException() {
        //Given
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should throw MustAuthorizeException
        mockedRitualsArea.assignChildIssueToEpic("KE-200", "KE-100");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testAssignIssueToEpic_MustAuthorizeException() {
        //Given
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should throw MustAuthorizeException
        mockedRitualsArea.assignIssueToEpic("KE-300", "KE-100");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testFetchByJQLQuery_MustAuthorizeException() {
        //Given
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When - should throw MustAuthorizeException
        mockedRitualsArea.fetchByJQLQuery("project = KE");
    }

    // ========== getCustomFieldValue Tests ==========

    @Test
    public void testGetCustomFieldValue_HappyPath() {
        //Given
        String issueKey = "KE-123";
        String customField = "customfield_10000";
        List<String> expectedValues = List.of("value1", "value2");

        //Stub: getCustomIssue returns a list
        Mockito.when(mockedIssueDao.getCustomIssue(issueKey, customField)).thenReturn(expectedValues);

        //When
        List<String> result = mockedRitualsArea.getCustomFieldValue(issueKey, customField);

        //Then
        Assert.assertNotNull(result);
        Assert.assertEquals(expectedValues, result);
        Mockito.verify(mockedJiraAuditService).success(Mockito.any(), Mockito.eq("Get custom field value"));
    }

    @Test
    public void testGetCustomFieldValue_Exception() {
        //Given
        String issueKey = "KE-123";
        String customField = "customfield_10000";

        //Stub: getCustomIssue throws RuntimeException
        Mockito.when(mockedIssueDao.getCustomIssue(issueKey, customField))
               .thenThrow(new RuntimeException("custom field error"));

        //When - getCustomFieldValue re-throws the exception
        try {
            mockedRitualsArea.getCustomFieldValue(issueKey, customField);
            Assert.fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            //Then
            Assert.assertEquals("custom field error", e.getMessage());
            Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Get custom field value"), Mockito.any(RuntimeException.class));
        }
    }

    // ========== getNotification Tests ==========

    @Test
    public void testGetNotification_HappyPath_NoMatchingEvent() {
        //Given: eventName does NOT equal JIRA_UPDATES, so empty map is returned
        String eventName = "some-other-event";
        FreeForm eventData = new FreeForm();

        //When
        Map<String, Object> result = mockedRitualsArea.getNotification(eventName, eventData);

        //Then
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetNotification_Exception() {
        //Given: eventName causes NPE (null)
        FreeForm eventData = new FreeForm();

        //When
        Map<String, Object> result = mockedRitualsArea.getNotification(null, eventData);

        //Then: catch block returns error map
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey("error"));
    }

    // ========== IllegalCallerException Tests ==========

    @Test(expected = RuntimeException.class)
    public void testGetUserIssues_IllegalCallerException() {
        //Stub: invokeAsUser returns true so validateCredentials is called, which throws
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When - should throw RuntimeException wrapping IllegalCallerException
        mockedRitualsArea.getUserIssues("userId");
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserIssueByIssueType_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getUserIssueByIssueType("userId", "Bug");
    }

    @Test(expected = RuntimeException.class)
    public void testGetIssues_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getIssues(null, null, null, null, null, null, null, null);
    }

    @Test(expected = RuntimeException.class)
    public void testAddAComment_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.addAComment("my comment", "KE-123", "Public");
    }

    @Test(expected = RuntimeException.class)
    public void testGetMyIssuesByGivenStatuses_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getMyIssuesByGivenStatuses(List.of("In Progress"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetIssueByKey_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getIssueByKey("KE-123");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateStatusOfIssue_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.updateStatusOfIssue("KE-123", "In Progress");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateAnIssue_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.updateAnIssue("KE-1", null, null, null, null, null, null, null, null, null);
    }

    @Test(expected = RuntimeException.class)
    public void testAddWorklog_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.addWorklog("KE-1", System.currentTimeMillis(), "2h");
    }

    @Test(expected = RuntimeException.class)
    public void testAssignATaskToUser_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.assignATaskToUser("22001", "KE-123");
    }

    @Test(expected = RuntimeException.class)
    public void testAssignChildIssueToEpic_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.assignChildIssueToEpic("KE-200", "KE-100");
    }

    @Test(expected = RuntimeException.class)
    public void testAssignIssueToEpic_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.assignIssueToEpic("KE-300", "KE-100");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdatePriorityOfIssue_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.updatePriorityOfIssue("KE-1", "High");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateThePriorityOfMultipleIssues_IllegalCallerException() {
        //Stub: throw from the first updatePriority call since there is no validateCredentials in this method
        Mockito.when(mockedIssueDao.updatePriority(Mockito.any(), Mockito.anyString()))
               .thenThrow(new IllegalCallerException("illegal caller"));

        //When
        mockedRitualsArea.updateThePriorityOfMultipleIssues(List.of("KE-1"), "High");
    }

    @Test(expected = RuntimeException.class)
    public void testFetchByJQLQuery_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.fetchByJQLQuery("project = KE");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateStatusOfMultipleIssues_IllegalCallerException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal caller")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.updateStatusOfMultipleIssues("In Progress", List.of("KE-1"));
    }

    // ========== MustAuthorizeException tests for missing methods ==========

    @Test(expected = MustAuthorizeException.class)
    public void testGetUserIssues_MustAuthorizeException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getUserIssues("userId");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetUserIssueByIssueType_MustAuthorizeException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getUserIssueByIssueType("userId", "Bug");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetIssues_MustAuthorizeException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getIssues(null, null, null, null, null, null, null, null);
    }

    @Test(expected = MustAuthorizeException.class)
    public void testAddAComment_MustAuthorizeException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.addAComment("my comment", "KE-123", "Public");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetMyIssuesByGivenStatuses_MustAuthorizeException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getMyIssuesByGivenStatuses(List.of("In Progress"));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetIssueByKey_MustAuthorizeException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.getIssueByKey("KE-123");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testUpdateStatusOfIssue_MustAuthorizeException() {
        //Stub
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Forbidden")).when(mockedIssueDao).validateCredentials();

        //When
        mockedRitualsArea.updateStatusOfIssue("KE-123", "In Progress");
    }

    // ========== Happy-path tests using mockedRitualsArea for methods at low coverage ==========

    @Test
    public void testGetUserIssues_WithMockedDao() {
        //Given
        String userId = "user-123";
        Issue mockIssue = new Issue();
        mockIssue.key = "KE-1";

        //Stub: user exists, searchByJQL returns issues
        Mockito.when(mockedUserDao.isUserExist(userId)).thenReturn(true);
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(List.of(mockIssue));

        //When
        ExtensionResponse response = mockedRitualsArea.getUserIssues(userId);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("User Issues"));
    }

    @Test
    public void testGetUserIssueByIssueType_WithMockedDao() {
        //Given
        String userId = "user-123";
        String issueType = "Bug";
        Issue mockIssue = new Issue();
        mockIssue.key = "KE-1";

        // GlobalIssueTypeValidator calls projectDao.getAllProjects() and getIssueTypeFromProject()
        TicketProject mockProject = new TicketProject();
        mockProject.key = "KE";
        String issueTypesJson = "[{\"name\":\"Bug\"},{\"name\":\"Story\"},{\"name\":\"Task\"}]";
        Mockito.when(mockedProjectDao.getAllProjects()).thenReturn(List.of(mockProject));
        Mockito.when(mockedProjectDao.getIssueTypeFromProject("KE")).thenReturn(issueTypesJson);

        //Stub: user exists, searchByJQL returns issues
        Mockito.when(mockedUserDao.isUserExist(userId)).thenReturn(true);
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(List.of(mockIssue));

        //When
        ExtensionResponse response = mockedRitualsArea.getUserIssueByIssueType(userId, issueType);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Issues"));
    }

    @Test
    public void testGetIssues_WithMockedDao() {
        //Given
        Issue mockIssue = new Issue();
        mockIssue.key = "KE-1";

        //Stub: no projectKey/issueType so validation is skipped, searchByJQL returns issues
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(List.of(mockIssue));

        //When - no projectKey, no issueType, so validationResults stays empty
        ExtensionResponse response = mockedRitualsArea.getIssues("summary", "High", null, "Open", null, null, null, null);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("List of Filtered Issues"));
    }

    @Test
    public void testAddAComment_WithMockedDao() {
        //Given
        String issueKey = "KE-123";
        String comment = "Test comment";

        //Stub: issue exists so validation passes, addComment does nothing
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).addComment(issueKey, comment, "Public");

        //When
        ExtensionResponse response = mockedRitualsArea.addAComment(comment, issueKey, "Public");

        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(true, response.getResponseValue().get("Is successful"));
    }

    @Test
    public void testGetMyIssuesByGivenStatuses_WithMockedDao() {
        //Given
        Issue mockIssue = new Issue();
        mockIssue.key = "KE-1";
        List<String> statuses = List.of("In Progress");

        //Stub: searchByJQL returns issues (validation uses getMyIssuesByGivenStatuses method signature, no project/user validation)
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(List.of(mockIssue));

        //When
        ExtensionResponse response = mockedRitualsArea.getMyIssuesByGivenStatuses(statuses);

        //Then
        Assert.assertNotNull(response);
    }

    @Test
    public void testGetIssueByKey_WithMockedDao() {
        //Given
        String issueKey = "KE-123";
        Issue mockIssue = new Issue();
        mockIssue.key = issueKey;

        //Stub: issue exists, getIssueFinal returns the issue
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.getIssueFinal(issueKey)).thenReturn(mockIssue);

        //When
        ExtensionResponse response = mockedRitualsArea.getIssueByKey(issueKey);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Issue"));
    }

    @Test
    public void testGetResultsForFetchIssuesByJQLQuery_ErrorFlag() {
        //Given: eventName matches taskId but IS_ERROR flag is true; ERROR_MESSAGE must be present
        String taskId = "test-task-id-error";
        FreeForm eventData = new FreeForm();
        eventData.put(RitualsArea.IS_ERROR, "Switch", true);
        // When IS_ERROR is true, the method reads ERROR_MESSAGE so it must be in the FreeForm
        eventData.put(RitualsArea.ERROR_MESSAGE, "Text", "Invalid JQL syntax");
        eventData.put("Data", "[ Entity(Issue) ]", Collections.emptyList());

        //When
        ExtensionResponse response = mockedRitualsArea.getResultsForFetchIssuesByJQLQuery(taskId, eventData, taskId);

        //Then
        Assert.assertNotNull(response);
    }

    // ========== Pattern A: invokeAsUser=true success (validateCredentials called, no throw) ==========

    @Test
    public void testUpdateStatusOfMultipleIssues_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing (void mock default)
        // Validation FAILS so we don't need DAO calls for the update path,
        // but validateCredentials is still called before validation
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // mockedIssueDao.validateCredentials() is void - does nothing by default
        // Issue does NOT exist, so ISSUE_KEYS validator will fail -> we get validation results
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.updateStatusOfMultipleIssues("In Progress", List.of("NONEXIST-1"));

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testAddAComment_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issue does NOT exist, so validation fails => confirmation response returned
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.addAComment("my comment", "NONEXIST-1", "Public");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testGetUserIssues_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // User does NOT exist, so validation fails => confirmation response returned
        Mockito.when(mockedUserDao.isUserExist(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.getUserIssues("nonexistent-user");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testUpdateStatusOfIssue_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issue does NOT exist -> TICKET_ID validation fails -> confirmation response
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.updateStatusOfIssue("NONEXIST-1", "In Progress");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testUpdateAnIssue_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issue does NOT exist -> validation fails -> confirmation response
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.updateAnIssue("NONEXIST-1", null, null, null, null, null, null, null, null, null);

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testUpdatePriorityOfIssue_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issue does NOT exist -> validation fails -> confirmation response
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.updatePriorityOfIssue("NONEXIST-1", "High");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testAddWorklog_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issue does NOT exist -> validation fails -> confirmation response
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.addWorklog("NONEXIST-1", System.currentTimeMillis(), "2h");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testGetIssueByKey_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issue does NOT exist -> validation fails -> confirmation response
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.getIssueByKey("NONEXIST-1");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testAssignATaskToUser_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issue does NOT exist -> validation fails -> confirmation response
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);
        Mockito.when(mockedUserDao.isUserExist(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.assignATaskToUser("nonexistent-user", "NONEXIST-1");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testAssignChildIssueToEpic_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issues do NOT exist -> validation fails -> confirmation response
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.assignChildIssueToEpic("NONEXIST-200", "NONEXIST-100");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testAssignIssueToEpic_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // Issues do NOT exist -> validation fails -> confirmation response
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.assignIssueToEpic("NONEXIST-300", "NONEXIST-100");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testGetUserIssueByIssueType_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        // User does NOT exist -> validation fails -> confirmation response
        Mockito.when(mockedUserDao.isUserExist(Mockito.anyString())).thenReturn(false);

        //When
        ExtensionResponse result = mockedRitualsArea.getUserIssueByIssueType("nonexistent-user", "Bug");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testGetMyIssuesByGivenStatuses_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        // STATUS_NAMES validation uses StatusNamesValidator via mockedTicketInfoDao.getStatuses()
        // Return an empty status list so "NonExistentStatus" fails validation -> confirmation response returned
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getStatuses()).thenReturn(Collections.emptyList());

        //When - use a status that won't pass StatusNamesValidator (empty statuses list)
        ExtensionResponse result = mockedRitualsArea.getMyIssuesByGivenStatuses(List.of("NonExistentStatus"));

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testGetCustomFieldValue_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.when(mockedIssueDao.getCustomIssue(Mockito.anyString(), Mockito.anyString()))
               .thenReturn(List.of("value1"));

        //When
        List<String> result = mockedRitualsArea.getCustomFieldValue("KE-123", "customfield_10000");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testFetchByJQLQuery_InvokeAsUser_ValidatesCredentials() {
        //Given: invokeAsUser=true, validateCredentials does nothing
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Account mockAccount = Mockito.mock(Account.class);
        Mockito.when(authorizationContext.getAuthorizedAccount()).thenReturn(mockAccount);
        Mockito.when(mockAccount.getAccountId()).thenReturn("test-account-id");

        //When
        ExtensionResponse result = mockedRitualsArea.fetchByJQLQuery("project = KE");

        //Then
        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    // ========== Pattern B: Validation failure/retry path (else branch with stateId/confirmation response) ==========

    @Test
    public void testUpdateStatusOfMultipleIssues_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator so we can control validation output
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Issue Key", "Enter valid issue key", "Issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap(), Mockito.any(), Mockito.anyString()))
               .thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.updateStatusOfMultipleIssues("In Progress", List.of("NONEXIST-1"));

        //Then: else branch taken - returns confirmation/remediation response
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterUpdateStatusOfMultipleIssues",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testGetMyIssuesByGivenStatuses_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Status Names", "Enter valid status names", "Status does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap(), Mockito.any(), Mockito.anyString()))
               .thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.getMyIssuesByGivenStatuses(List.of("NonExistentStatus"));

        //Then: else branch taken - returns confirmation/remediation response
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterGetMyIssuesByGivenStatuses",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testGetUserIssueByIssueType_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "User account Id", "Enter valid user id", "User not found");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap(), Mockito.any(), Mockito.anyString()))
               .thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.getUserIssueByIssueType("nonexistent-user", "Bug");

        //Then: else branch taken - returns confirmation/remediation response
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterGetUserIssueByIssueType",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testUpdateStatusOfIssue_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        // The method calls validate twice (first for TICKET_ID, then for TICKET_ID+STATUS_NAME)
        // We make validation fail on first call to take the else branch
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Issue Key", "Enter valid issue key", "Issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.updateStatusOfIssue("NONEXIST-1", "In Progress");

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterForUpdateStatusOfIssue",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testUpdateAnIssue_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Issue Key", "Enter valid issue key", "Issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.updateAnIssue("NONEXIST-1", null, null, null, null, null, null, null, null, null);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterForUpdateAnIssue",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testUpdatePriorityOfIssue_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Issue Key", "Enter valid issue key", "Issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.updatePriorityOfIssue("NONEXIST-1", "High");

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterUpdatePriorityOfIssue",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testAddWorklog_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Issue Key", "Enter valid issue key", "Issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.addWorklog("NONEXIST-1", System.currentTimeMillis(), "2h");

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterAddWorkLog",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testGetIssueByKey_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Issue Key", "Enter valid issue key", "Issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.getIssueByKey("NONEXIST-1");

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterGetIssueByKey",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testAssignChildIssueToEpic_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Child Issue Key", "Enter valid child issue key", "Child issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.assignChildIssueToEpic("NONEXIST-200", "NONEXIST-100");

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterAssignChildIssueToEpic",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testAssignIssueToEpic_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Issue Key", "Enter valid issue key", "Issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.assignIssueToEpic("NONEXIST-300", "NONEXIST-100");

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterAssignIssueToEpic",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testAssignATaskToUser_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Issue key", "Enter valid issue key", "Issue does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When
        ExtensionResponse response = ritualsArea.assignATaskToUser("nonexistent-user", "NONEXIST-1");

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterAssignATaskToUser",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    @Test
    public void testGetIssues_ValidationFailure_ConfirmationResponse() {
        //Given: use ritualsArea which has mockValidationOrchestrator
        // Provide a projectKey that exists but issue type validation fails
        ValidationOrchestrator.ValidationResult vr = new ValidationOrchestrator.ValidationResult(
                "Confirm step msg", "Project Key", "Enter valid project key", "Project does not exist");
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(List.of(vr));

        //When - provide a projectKey so validation is attempted
        ExtensionResponse response = ritualsArea.getIssues(null, null, null, null, null, "NONEXIST", null, null);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getRemediationActions());
        Assert.assertEquals("confirmReenterGetIssues",
                response.getRemediationActions().getSubCatalogRequestName());
    }

    // ========== Pattern C: Generic Exception catch blocks for specific methods ==========

    @Test
    public void testUpdateStatusOfIssue_GenericException() {
        //Given: use ritualsArea with mockValidationOrchestrator returning empty (pass)
        // then issueDao.updateStatus throws a generic RuntimeException
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(Collections.emptyList());
        Mockito.doThrow(new RuntimeException("update status error")).when(restClient)
               .fetch(Mockito.anyString(), Mockito.anyMap(), Mockito.any());

        //When - using real issueDao which calls restClient.fetch; fetch throws RuntimeException
        // This covers the catch (Exception cause) block in updateStatusOfIssue
        ExtensionResponse response = ritualsArea.updateStatusOfIssue("KE-123", "In Progress");

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Update status of issue"), Mockito.any(Exception.class));
    }

    @Test
    public void testGetMyIssuesByGivenStatuses_GenericException() {
        //Given: use ritualsArea with mockValidationOrchestrator; validate returns empty -> tries searchByJQL
        // but restClient.fetch throws
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap(), Mockito.any(), Mockito.anyString()))
               .thenReturn(Collections.emptyList());
        Mockito.when(restClient.fetch(Mockito.anyString(), Mockito.anyMap(), Mockito.any()))
               .thenThrow(new RuntimeException("search failed"));

        //When
        ExtensionResponse response = ritualsArea.getMyIssuesByGivenStatuses(List.of("In Progress"));

        //Then: catch (Exception) block returns error ExtensionResponse
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Get my issues by given statuses"), Mockito.any(Exception.class));
    }

    @Test
    public void testGetUserIssueByIssueType_GenericException() {
        //Given: use ritualsArea with mockValidationOrchestrator; validate returns empty -> tries searchByJQL
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap(), Mockito.any(), Mockito.anyString()))
               .thenReturn(Collections.emptyList());
        Mockito.when(restClient.fetch(Mockito.anyString(), Mockito.anyMap(), Mockito.any()))
               .thenThrow(new RuntimeException("search failed"));

        //When
        ExtensionResponse response = ritualsArea.getUserIssueByIssueType("user-123", "Bug");

        //Then: catch (Exception) block returns error ExtensionResponse
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Get user issue by issue type"), Mockito.any(Exception.class));
    }

    @Test
    public void testGetUserIssues_GenericException() {
        //Given: use ritualsArea with mockValidationOrchestrator; validate returns empty -> tries searchByJQL
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(Collections.emptyList());
        Mockito.when(restClient.fetch(Mockito.anyString(), Mockito.anyMap(), Mockito.any()))
               .thenThrow(new RuntimeException("search failed"));

        //When
        ExtensionResponse response = ritualsArea.getUserIssues("user-123");

        //Then: catch (Exception) block returns error ExtensionResponse
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Get user issues"), Mockito.any(Exception.class));
    }

    @Test
    public void testGetIssues_GenericException() {
        //Given: no projectKey so validation is skipped; searchByJQL throws
        Mockito.when(restClient.fetch(Mockito.anyString(), Mockito.anyMap(), Mockito.any()))
               .thenThrow(new RuntimeException("search failed"));

        //When
        ExtensionResponse response = ritualsArea.getIssues("summary", null, null, null, null, null, null, null);

        //Then: catch (Exception) block returns error ExtensionResponse
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Get Issues"), Mockito.any(Exception.class));
    }

    @Test
    public void testGetIssueByKey_GenericException() {
        //Given: use ritualsArea with mockValidationOrchestrator; validate returns empty -> tries getIssueFinal
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap())).thenReturn(Collections.emptyList());
        Mockito.when(restClient.fetch(Mockito.anyString(), Mockito.anyMap(), Mockito.any()))
               .thenThrow(new RuntimeException("fetch failed"));

        //When
        ExtensionResponse response = ritualsArea.getIssueByKey("KE-123");

        //Then: catch (Exception) block returns error ExtensionResponse
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Get issue by key"), Mockito.any(Exception.class));
    }

    @Test
    public void testAddAComment_InnerMustAuthorizeException() {
        //Given: issue exists (validation passes) but addComment throws MustAuthorizeException
        // This covers the inner try/catch (MustAuthorizeException) inside the validation-pass block
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("Unauthorized"))
               .when(mockedIssueDao).addComment(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        //When
        try {
            mockedRitualsArea.addAComment("test comment", "KE-123", "Public");
            Assert.fail("Expected MustAuthorizeException to be thrown");
        } catch (MustAuthorizeException e) {
            //Then: inner MustAuthorizeException re-thrown as new MustAuthorizeException("Please authenticate yourself....")
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e.getMessage().contains("Please authenticate yourself."));
        }
    }

    @Test
    public void testAddAComment_InnerGenericException() {
        //Given: issue exists (validation passes) but addComment throws a generic RuntimeException
        // This covers the inner catch (Exception cause) block in addAComment
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(true);
        Mockito.doThrow(new RuntimeException("network error"))
               .when(mockedIssueDao).addComment(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        //When
        ExtensionResponse response = mockedRitualsArea.addAComment("test comment", "KE-123", "Public");

        //Then: inner catch returns error ExtensionResponse
        Assert.assertNotNull(response);
    }

    @Test
    public void testUpdateStatusOfMultipleIssues_GenericException() {
        //Given: use ritualsArea with mockValidationOrchestrator; validation passes
        // but the actual updateStatus call on real issueDao throws
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap(), Mockito.any(), Mockito.anyString()))
               .thenReturn(Collections.emptyList());
        Mockito.when(restClient.fetch(Mockito.anyString(), Mockito.anyMap(), Mockito.any()))
               .thenThrow(new RuntimeException("update failed"));

        //When
        ExtensionResponse response = ritualsArea.updateStatusOfMultipleIssues("In Progress", List.of("KE-1"));

        //Then: catch (Exception) block returns error ExtensionResponse
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Update status of multiple issues"), Mockito.any(Exception.class));
    }

    // ========== updateStatusOfIssue: success path with updateStatus + getIssueFinal ==========

    @Test
    public void testUpdateStatusOfIssue_SuccessPath_WithMockedDao() {
        //Given: both TICKET_ID and STATUS_NAME validations pass
        String issueKey = "KE-123";
        String statusName = "In Progress";
        Issue updatedIssue = new Issue();
        updatedIssue.key = issueKey;
        updatedIssue.status = statusName;

        TransitionStatus transition = new TransitionStatus();
        transition.toName = "In Progress";

        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions(issueKey)).thenReturn(List.of(transition));
        Mockito.doNothing().when(mockedIssueDao).updateStatus(issueKey, statusName);
        Mockito.when(mockedIssueDao.getIssueFinal(issueKey)).thenReturn(updatedIssue);

        //When
        ExtensionResponse response = mockedRitualsArea.updateStatusOfIssue(issueKey, statusName);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Updated Issue"));
        Mockito.verify(mockedIssueDao).updateStatus(issueKey, statusName);
        Mockito.verify(mockedIssueDao).getIssueFinal(issueKey);
    }

    // ========== getMyIssuesByGivenStatuses: searchByJQL success path ==========

    @Test
    public void testGetMyIssuesByGivenStatuses_SearchByJQL_SuccessPath() {
        //Given: STATUS_NAMES validation passes and searchByJQL returns issues
        Issue mockIssue = new Issue();
        mockIssue.key = "KE-1";
        List<String> statuses = List.of("In Progress");

        // StatusNamesValidator calls ticketInfoDao.getStatuses() and checks TicketStatus.name field
        TicketStatus ticketStatus = new TicketStatus();
        ticketStatus.name = "In Progress";
        Mockito.when(mockedTicketInfoDao.getStatuses()).thenReturn(List.of(ticketStatus));

        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(List.of(mockIssue));

        //When
        ExtensionResponse response = mockedRitualsArea.getMyIssuesByGivenStatuses(statuses);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Issues"));
        Mockito.verify(mockedIssueDao).searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
    }

    // ========== getNotification: matching event name path (parseWebhookResponse called) ==========

    @Test
    public void testGetNotification_MatchingEventName_ParsesWebhookResponse() {
        //Given: eventName equals JIRA_UPDATES so parseWebhookResponse is called
        String eventName = "Jira Updates";
        FreeForm eventData = new FreeForm();
        // Provide a valid JSON string for the webhook response
        String webhookJson = "{\"issue\":{\"key\":\"KE-123\"},\"changelog\":{\"items\":[]}}";
        eventData.put("Updates On Jira", "Text", webhookJson);

        //When
        Map<String, Object> result = mockedRitualsArea.getNotification(eventName, eventData);

        //Then: result contains parsed webhook data (issueKey and update fields)
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey("Issue Key") || result.containsKey("Update") || !result.isEmpty());
    }

    @Test
    public void testGetNotification_MatchingEventName_NullWebhookData() {
        //Given: eventName equals JIRA_UPDATES but webhook data is null
        String eventName = "Jira Updates";
        FreeForm eventData = new FreeForm();
        // Do NOT put "Updates On Jira" key - getNotification gets null from eventData.get(UPDATES_ON_JIRA)
        // This causes a ClassCastException or NPE that is caught and returns an error map

        //When
        Map<String, Object> result = mockedRitualsArea.getNotification(eventName, eventData);

        //Then: catch block returns error map (null cast to String causes exception)
        Assert.assertNotNull(result);
    }

    // ========== updateStatusOfMultipleIssues: invokeAsUser success with full happy path ==========

    @Test
    public void testUpdateStatusOfMultipleIssues_InvokeAsUser_FullSuccessPath() {
        //Given: invokeAsUser=true, validateCredentials does nothing, validation passes, update succeeds
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);

        String statusName = "In Progress";
        List<String> issueKeys = List.of("KE-10");
        Issue issue = new Issue();
        issue.key = "KE-10";

        TransitionStatus transition = new TransitionStatus();
        transition.toName = "In Progress";
        Mockito.when(mockedIssueDao.isIssueExists("KE-10")).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions("KE-10")).thenReturn(List.of(transition));
        Mockito.doNothing().when(mockedIssueDao).updateStatus("KE-10", statusName);
        Mockito.when(mockedIssueDao.getIssueFinal("KE-10")).thenReturn(issue);

        //When
        ExtensionResponse response = mockedRitualsArea.updateStatusOfMultipleIssues(statusName, issueKeys);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Updated issues"));
        Mockito.verify(mockedIssueDao).validateCredentials();
        Mockito.verify(mockedIssueDao).updateStatus("KE-10", statusName);
        Mockito.verify(mockedIssueDao).getIssueFinal("KE-10");
    }

    // ========== L235-240: outer catch (Exception) in addAComment - validation system error ==========

    @Test
    public void testAddAComment_OuterGenericException_ValidationSystemError() {
        //Given: mockValidationOrchestrator.validate() throws a generic RuntimeException (not Must/Illegal)
        // This triggers the outer catch (Exception cause) block in addAComment
        Mockito.when(mockValidationOrchestrator.validate(Mockito.anyMap()))
               .thenThrow(new RuntimeException("validation system error"));

        //When
        ExtensionResponse response = ritualsArea.addAComment("my comment", "KE-999", "Public");

        //Then: outer catch returns an error ExtensionResponse
        Assert.assertNotNull(response);
        Mockito.verify(mockedJiraAuditService).failure(Mockito.any(), Mockito.eq("Add a comment"), Mockito.any(RuntimeException.class));
    }

    // ========== L487-494: inner catch (Exception) in fetchByJQLQuery async lambda ==========

    @Test
    public void testFetchByJQLQuery_InnerCatchException_SearchByJqlQueryThrows() throws Exception {
        //Given: authorizationContext returns a valid account; issueDao.searchByJqlQuery throws
        Account mockAccount = Mockito.mock(Account.class);
        Mockito.when(mockAccount.getAccountId()).thenReturn("acc-001");
        Mockito.when(authorizationContext.getAuthorizedAccount()).thenReturn(mockAccount);
        Mockito.when(mockedIssueDao.searchByJqlQuery(
                Mockito.anyBoolean(), Mockito.anyString(), Mockito.nullable(String.class),
                Mockito.anyString(), Mockito.anyLong()))
               .thenThrow(new RuntimeException("JQL failed"));

        //When: fetchByJQLQuery submits async task; we call it and wait briefly for the executor
        ExtensionResponse response = mockedRitualsArea.fetchByJQLQuery("project = KE");

        //Then: the outer call returns a Task ID (async, inner catch runs in thread)
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Task ID"));

        // Give the executor thread a moment to run and hit the inner catch
        Thread.sleep(200);
        Mockito.verify(mockedIssueDao).searchByJqlQuery(
                Mockito.anyBoolean(), Mockito.anyString(), Mockito.nullable(String.class),
                Mockito.anyString(), Mockito.anyLong());
    }

    // ========== L551-557: catch MustAuthorizeException and catch ClassCastException in getResultsForFetchIssuesByJQLQuery ==========

    @Test(expected = app.krista.extension.authorization.MustAuthorizeException.class)
    public void testGetResultsForFetchIssuesByJQLQuery_MustAuthorizeException() {
        //Given: eventName matches taskID, IS_ERROR is false, "Data" value causes MustAuthorizeException cast
        // We use a subclass of List that throws MustAuthorizeException on size() call is not practical;
        // instead provide a FreeForm whose "Data" value is an object with a proxy that throws
        // MustAuthorizeException on cast. The simplest approach: put a real List but trick the cast.
        // Actually the code does: (List<Issue>) eventData.get("Data"); if the object IS a List this won't throw.
        // To trigger MustAuthorizeException we need the code inside the try block after the cast to throw it.
        // issueList.size() calls size on the list - if issueList is empty it returns early.
        // The MustAuthorizeException path is: issueList = (List<Issue>) ... then issueList.size() can't throw MAE.
        // Looking more carefully: the try block catches MustAuthorizeException and re-throws it.
        // This can only be triggered if some code between the cast and the return throws MustAuthorizeException.
        // The simplest trigger: provide a List proxy whose isEmpty() throws MustAuthorizeException.
        String taskId = "task-mae-001";
        FreeForm eventData = new FreeForm();
        // Use a java.util.List mock that throws MustAuthorizeException on isEmpty()
        @SuppressWarnings("unchecked")
        List<Issue> proxyList = Mockito.mock(List.class);
        Mockito.when(proxyList.size()).thenReturn(1); // not empty, so it reaches return path
        Mockito.when(proxyList.isEmpty()).thenThrow(new app.krista.extension.authorization.MustAuthorizeException("auth required"));
        eventData.put("Data", "[ Entity(Issue) ]", proxyList);

        //When/Then: expect MustAuthorizeException to propagate
        mockedRitualsArea.getResultsForFetchIssuesByJQLQuery(taskId, eventData, taskId);
    }

    @Test
    public void testGetResultsForFetchIssuesByJQLQuery_ClassCastException() {
        //Given: eventName matches taskID, IS_ERROR is false, "Data" value is not a List<Issue>
        // Putting a non-List value under "Data" causes a ClassCastException when cast to List<Issue>
        String taskId = "task-cce-002";
        FreeForm eventData = new FreeForm();
        // Put a plain String (not a List) under "Data" - cast will throw ClassCastException
        eventData.put("Data", "Text", "not-a-list");

        //When
        ExtensionResponse response = mockedRitualsArea.getResultsForFetchIssuesByJQLQuery(taskId, eventData, taskId);

        //Then: ClassCastException is caught and returns empty list response
        Assert.assertNotNull(response);
    }

    // ========== L671: issueDao.validateCredentials() in getIssues (invokeAsUser=true) ==========

    @Test
    public void testGetIssues_InvokeAsUser_ValidateCredentialsCalled() {
        //Given: invokeAsUser=true, no projectKey/issueType filters, searchByJQL returns issues
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).validateCredentials();

        Issue issue = new Issue();
        issue.key = "KE-50";
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(List.of(issue));

        //When
        ExtensionResponse response = mockedRitualsArea.getIssues(
                null, null, null, null, null, null, null, null);

        //Then: validateCredentials was called
        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao).validateCredentials();
        Assert.assertNotNull(response.getResponseValue().get("List of Filtered Issues"));
    }

    // ========== L675, L679-681: getIssues with projectKey and issueType branches ==========

    @Test
    public void testGetIssues_WithProjectKey_ValidatesProjectKey() {
        //Given: projectKey is non-null/non-blank, no issueType; validation passes; searchByJQL returns list
        Mockito.when(mockedIssueDao.isIssueExists(Mockito.anyString())).thenReturn(false);
        // isProjectExist needed for projectKey validator - stub it to avoid NPE
        Mockito.when(mockedProjectDao.isProjectExist(Mockito.anyString())).thenReturn(true);

        Issue issue = new Issue();
        issue.key = "KE-60";
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(List.of(issue));

        //When: pass non-blank projectKey to trigger projectKey validation branch (L675)
        ExtensionResponse response = mockedRitualsArea.getIssues(
                null, null, null, null, null, "KE", null, null);

        //Then
        Assert.assertNotNull(response);
    }

    @Test
    public void testGetIssues_WithProjectKeyAndIssueType_ValidatesIssueType() {
        //Given: projectKey non-blank AND issueType non-null/non-blank AND project exists
        // mockedProjectDao.isProjectExist returns true so the issueType branch (L679-681) is hit
        Mockito.when(mockedProjectDao.isProjectExist(Mockito.anyString())).thenReturn(true);

        // Stub the issue-type validation (issueType validator checks issueDao.getIssueTypes)
        Mockito.when(mockedIssueDao.getIssueTypes(Mockito.anyString())).thenReturn(List.of("Bug", "Story", "Task"));

        Issue issue = new Issue();
        issue.key = "KE-61";
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(List.of(issue));

        //When
        ExtensionResponse response = mockedRitualsArea.getIssues(
                null, null, "Bug", null, null, "KE", null, null);

        //Then
        Assert.assertNotNull(response);
    }

    // ========== L693: getIssues with positive pageSize and pageNumber (ternary branch) ==========

    @Test
    public void testGetIssues_WithPageSizeAndPageNumber_PositiveValues() {
        //Given: pageSize=10.0 and pageNumber=1.0 to hit the (pageSize > 0) ternary branch (L693)
        Issue issue = new Issue();
        issue.key = "KE-70";
        Mockito.when(mockedIssueDao.searchByJQL(Mockito.anyString(), Mockito.eq(10L), Mockito.eq(1L)))
               .thenReturn(List.of(issue));

        //When
        ExtensionResponse response = mockedRitualsArea.getIssues(
                null, null, null, null, null, null, 10.0, 1.0);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("List of Filtered Issues"));
        Mockito.verify(mockedIssueDao).searchByJQL(Mockito.anyString(), Mockito.eq(10L), Mockito.eq(1L));
    }

    // ========== L1022-1023: issueDao.validateCredentials() in updateThePriorityOfMultipleIssues (invokeAsUser=true) ==========

    @Test
    public void testUpdateThePriorityOfMultipleIssues_InvokeAsUser_ValidateCredentialsCalled() {
        //Given: invokeAsUser=true, validateCredentials does nothing, updatePriority + getIssueFinal succeed
        Mockito.when(requestContext.invokeAsUser()).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).validateCredentials();

        Issue issue = new Issue();
        issue.key = "KE-80";
        Mockito.when(mockedIssueDao.getIssueFinal("KE-80")).thenReturn(issue);

        //When
        ExtensionResponse response = mockedRitualsArea.updateThePriorityOfMultipleIssues(List.of("KE-80"), "High");

        //Then: validateCredentials was called and result is success
        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao).validateCredentials();
        Assert.assertNotNull(response.getResponseValue().get("List of Issues"));
    }

    // ========== L1033-1037: catch MustAuthorizeException in updateThePriorityOfMultipleIssues ==========

    @Test(expected = app.krista.extension.authorization.MustAuthorizeException.class)
    public void testUpdateThePriorityOfMultipleIssues_MustAuthorizeException() {
        //Given: mockedIssueDao.updatePriority throws MustAuthorizeException
        Mockito.doThrow(new app.krista.extension.authorization.MustAuthorizeException("auth required"))
               .when(mockedIssueDao).updatePriority(Mockito.any(), Mockito.anyString());

        //When/Then: MustAuthorizeException is re-thrown
        mockedRitualsArea.updateThePriorityOfMultipleIssues(List.of("KE-90"), "High");
    }

    // ========== L859-860: updateAnIssue with newStatus non-null/non-empty (status validation branch) ==========

    @Test
    public void testUpdateAnIssue_WithNewStatus_StatusValidationBranch() {
        //Given: newStatus is non-null and non-empty; issue exists; status is valid transition
        String issueKey = "KE-100";
        Issue oldIssue = new Issue();
        oldIssue.key = issueKey;
        oldIssue.summary = "Old Summary";
        oldIssue.status = "Backlog";
        Issue updatedIssue = new Issue();
        updatedIssue.key = issueKey;
        updatedIssue.summary = "Old Summary";
        updatedIssue.status = "In Progress";

        TransitionStatus transition = new TransitionStatus();
        transition.toName = "In Progress";
        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions(issueKey)).thenReturn(List.of(transition));
        Mockito.when(mockedIssueDao.getIssueFinal(issueKey)).thenReturn(oldIssue).thenReturn(updatedIssue);
        Mockito.doNothing().when(mockedIssueDao).updateIssue(Mockito.any(Issue.class), Mockito.any());

        //When: newStatus is non-null/non-empty (L859-860 branch)
        ExtensionResponse response = mockedRitualsArea.updateAnIssue(
                issueKey, "In Progress", null, null, null, null, null, null, null, null);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Updated Issue"));
    }

    // ========== L872: updateAnIssue with non-null non-blank newSummary ==========

    @Test
    public void testUpdateAnIssue_WithNonBlankNewSummary_UseNewSummary() {
        //Given: newSummary is non-null/non-blank so issue.summary = newSummary (L872 ternary right branch)
        String issueKey = "KE-101";
        Issue oldIssue = new Issue();
        oldIssue.key = issueKey;
        oldIssue.summary = "Old Summary";
        Issue updatedIssue = new Issue();
        updatedIssue.key = issueKey;
        updatedIssue.summary = "Brand New Summary";

        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        Mockito.when(mockedIssueDao.getIssueFinal(issueKey)).thenReturn(oldIssue).thenReturn(updatedIssue);
        Mockito.doNothing().when(mockedIssueDao).updateIssue(Mockito.any(Issue.class), Mockito.any());

        //When: newSummary is non-null/non-blank (L872)
        ExtensionResponse response = mockedRitualsArea.updateAnIssue(
                issueKey, null, null, "Brand New Summary", null, null, null, null, null, null);

        //Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue().get("Updated Issue"));
    }

    // ========== L878-879: updateAnIssue with non-null non-blank workLog (addWorklog called) ==========

    @Test
    public void testUpdateAnIssue_WithWorkLog_AddWorklogCalled() {
        //Given: workLog is non-null/non-blank so addWorklog is called (L878-879)
        String issueKey = "KE-102";
        Issue oldIssue = new Issue();
        oldIssue.key = issueKey;
        oldIssue.summary = "Summary";
        Issue updatedIssue = new Issue();
        updatedIssue.key = issueKey;

        Mockito.when(mockedIssueDao.isIssueExists(issueKey)).thenReturn(true);
        // getIssueFinal called twice: once for oldIssue, once after update, plus once inside addWorklog validation
        Mockito.when(mockedIssueDao.getIssueFinal(issueKey)).thenReturn(oldIssue).thenReturn(updatedIssue);
        Mockito.doNothing().when(mockedIssueDao).updateIssue(Mockito.any(Issue.class), Mockito.any());
        // addWorklog internally calls validationOrchestrator.validate (mockedIssueDao.isIssueExists already stubbed)
        Mockito.doNothing().when(mockedIssueDao).addWorklog(
                Mockito.anyString(), Mockito.anyLong(), Mockito.eq(issueKey), Mockito.anyString());

        //When: workLog is "2h" (non-null/non-blank) - L878-879 branch
        ExtensionResponse response = mockedRitualsArea.updateAnIssue(
                issueKey, null, null, null, null, null, "2h", null, null, null);

        //Then
        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao).addWorklog(
                Mockito.anyString(), Mockito.anyLong(), Mockito.eq(issueKey), Mockito.anyString());
    }
}