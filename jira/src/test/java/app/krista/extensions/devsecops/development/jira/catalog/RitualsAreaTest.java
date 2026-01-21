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

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.impl.AskAPersonAction;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.*;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.model.field.NamedField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setup() {
        restClientFactory = Mockito.mock(RestClientFactory.class);
        restClient = Mockito.mock(RestClient.class);
        internalState = Mockito.mock(KeyValueStore.class);
        issueDao = new IssueDao(restClientFactory, restClient);
        mockedIssueDao = Mockito.mock(IssueDao.class);
        mockedProjectDao = Mockito.mock(ProjectDao.class);
        mockedUserDao = Mockito.mock(UserDao.class);
        mockedTicketInfoDao = Mockito.mock(TicketInfoDao.class);
        mockValidationOrchestrator = Mockito.mock(ValidationOrchestrator.class);
        validationOrchestrator = new ValidationOrchestrator(mockedIssueDao, mockedProjectDao, mockedUserDao,
                mockedTicketInfoDao, requestContext, authorizationContext);
        mockedTelemetryMetrics = Mockito.mock(TelemetryMetrics.class);
        ritualsArea = new RitualsArea(issueDao, requestContext, authorizationContext,
                (app.krista.extensions.util.EventHandler) eventHandler,
                mockValidationOrchestrator,
                new ErrorHandlingStateManager(internalState), new ExtensionResponseGenerator(), mockedProjectDao, mockedTelemetryMetrics);

        mockedRitualsArea = new RitualsArea(
                mockedIssueDao, requestContext, authorizationContext,
                (app.krista.extensions.util.EventHandler) eventHandler,
                validationOrchestrator,
                new ErrorHandlingStateManager(internalState),
                new ExtensionResponseGenerator(),
                mockedProjectDao,
                mockedTelemetryMetrics
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
        qparams.put("startAt", 0L);
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
    public void testAssignATaskToUser() {
        //Given
        String issueType = "Bug";
        String userAccountId = "22001";
        Map<String, Object> qparams = new HashMap<>();
        String query = "issuetype = \"" + issueType + "\"  AND assignee in (" + userAccountId + ") order by priority DESC, duedate DESC";
        qparams.put("jql", query);
        qparams.put("fields", "*all");
        qparams.put("startAt", 0L);
        qparams.put("maxResults", 30L);

        //Stub
        Mockito.when(restClient.fetch("search/jql", qparams, String.class)).thenReturn(issueResponse);

        //When
        ExtensionResponse issueList = ritualsArea.getUserIssueByIssueType(userAccountId, issueType);
        List<Issue> issues = (List<Issue>) issueList.getResponseValue().get("Issues");

        //Then
        Assert.assertNotNull(issues);
    }

    public void testFetchByJQLQuery() {
        // This request generate a random Task ID which is used in another request
        //Given
        final String taskId = UUID.randomUUID().toString();
        Map<String, Object> qparams = new HashMap<>();
        String query = "issuetype = \"" + "Bug" + "\"  AND assignee in (" + "22001" + ") order by priority DESC, duedate DESC";
        qparams.put("jql", query);
        qparams.put("fields", "*all");
        qparams.put("startAt", 0L);
        qparams.put("maxResults", 30L);

        //Stub
        Mockito.when(restClient.fetchIssues(null, null, "search/jql", qparams, String.class)).thenReturn(issueResponse);

        //When
        ExtensionResponse response = ritualsArea.fetchByJQLQuery(query);

        //Then
        Assert.assertNotNull(response);
    }

    //2
    @Test
    public void testGetResultsForFetchIssuesByJQLQuery() {
        //Given
        String issueType = "Bug";
        String userAccountId = "22001";
        Map<String, Object> qparams = new HashMap<>();
        String query = "issuetype = \"" + issueType + "\"  AND assignee in (" + userAccountId + ") order by priority DESC, duedate DESC";
        qparams.put("jql", query);
        qparams.put("fields", "*all");
        qparams.put("startAt", 0L);
        qparams.put("maxResults", 30L);

        //Stub
        Mockito.when(restClient.fetch("search/jql", qparams, String.class)).thenReturn(issueResponse);

        //When
        ExtensionResponse issueList = ritualsArea.getUserIssueByIssueType(userAccountId, issueType);
        List<Issue> issues = (List<Issue>) issueList.getResponseValue().get("Issues");
        //Then
        Assert.assertNotNull(issues);
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
        qparams.put("startAt", 0L);
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
        qparams.put("startAt", 0L);
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
        qparams.put("startAt", 0L);
        qparams.put("maxResults", 30L);

        //Stub
        Mockito.when(restClient.fetch("search/jql", qparams, String.class)).thenReturn(issueResponse);

        //When
        ExtensionResponse issueList = ritualsArea.getUserIssueByIssueType(userAccountId, issueType);
        List<String> issues = (List<String>) issueList.getResponseValue().get("Issues");
        //Then
        Assert.assertNotNull(issues);
    }


    public void testUpdateAnIssue() {
    }

    public void testUpdatePriorityOfIssue() {
    }

    public void testUpdateThePriorityOfMultipleIssues() {
    }

    public void testAddWorklog() {
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

    public void testAssignChildIssueToEpic() {
    }

    public void testAssignIssueToEpic() {
    }

    // ========== Exception Flow Integration Tests ==========
    // Note: These tests verify exception handling patterns in RitualsArea
    // The actual exception handling is tested in OrganizationAreaTest with similar patterns
}