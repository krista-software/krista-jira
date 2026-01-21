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

import com.google.gson.JsonSyntaxException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class WebhookDaoTest {

    private WebhookDao webhookDao;

    private String validJson;

    @Before
    public void setUp() {
        webhookDao = new WebhookDao();
        validJson = "{\"timestamp\":1702365396690,\"webhookEvent\":\"comment_deleted\",\"comment\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/issue/33073/comment/53758\",\"id\":\"53758\",\"author\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/user?accountId=62f35c0596eb272011aa421e\",\"accountId\":\"62f35c0596eb272011aa421e\",\"avatarUrls\":{\"48x48\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/48\",\"24x24\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/24\",\"16x16\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/16\",\"32x32\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/32\"},\"displayName\":\"Pulkit Jareda\",\"active\":true,\"timeZone\":\"Asia/Kolkata\",\"accountType\":\"atlassian\"},\"body\":\"Testing snsdkcvnsd\",\"updateAuthor\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/user?accountId=62f35c0596eb272011aa421e\",\"accountId\":\"62f35c0596eb272011aa421e\",\"avatarUrls\":{\"48x48\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/48\",\"24x24\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/24\",\"16x16\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/16\",\"32x32\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/32\"},\"displayName\":\"Pulkit Jareda\",\"active\":true,\"timeZone\":\"Asia/Kolkata\",\"accountType\":\"atlassian\"},\"created\":\"2023-12-12T12:46:07.501+0530\",\"updated\":\"2023-12-12T12:46:22.260+0530\",\"jsdPublic\":true},\"issue\":{\"id\":\"33073\",\"self\":\"https://antbrains.atlassian.net/rest/api/2/33073\",\"key\":\"KE-1231\",\"fields\":{\"summary\":\"Test case Writing and Review\",\"issuetype\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/issuetype/10102\",\"id\":\"10102\",\"description\":\"A small piece of work that's part of a larger task.\",\"iconUrl\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/issuetype/avatar/10316?size=medium\",\"name\":\"Sub-task\",\"subtask\":true,\"avatarId\":10316,\"hierarchyLevel\":-1},\"parent\":{\"id\":\"31848\",\"key\":\"KE-1193\",\"self\":\"https://antbrains.atlassian.net/rest/api/2/issue/31848\",\"fields\":{\"summary\":\"Outlook extension should take input as list of entities and should be able print as table in the extension\",\"status\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/status/6\",\"description\":\"The issue is considered finished, the resolution is correct. Issues which are closed can be reopened.\",\"iconUrl\":\"https://antbrains.atlassian.net/images/icons/statuses/closed.png\",\"name\":\"Closed\",\"id\":\"6\",\"statusCategory\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/statuscategory/3\",\"id\":3,\"key\":\"done\",\"colorName\":\"green\",\"name\":\"Done\"}},\"priority\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/priority/2\",\"iconUrl\":\"https://antbrains.atlassian.net/images/icons/priorities/critical.svg\",\"name\":\"P2\",\"id\":\"2\"},\"issuetype\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/issuetype/10245\",\"id\":\"10245\",\"description\":\"An improvement or enhancement to an existing feature or task.\",\"iconUrl\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/issuetype/avatar/10310?size=medium\",\"name\":\"Improvement\",\"subtask\":false,\"avatarId\":10310,\"hierarchyLevel\":0}}},\"project\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/project/10248\",\"id\":\"10248\",\"key\":\"KE\",\"name\":\"Krista Extensions\",\"projectTypeKey\":\"software\",\"simplified\":false,\"avatarUrls\":{\"48x48\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/project/avatar/10413\",\"24x24\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/project/avatar/10413?size=small\",\"16x16\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/project/avatar/10413?size=xsmall\",\"32x32\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/project/avatar/10413?size=medium\"}},\"assignee\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/user?accountId=641bfdbd407493675d477943\",\"accountId\":\"641bfdbd407493675d477943\",\"avatarUrls\":{\"48x48\":\"https://secure.gravatar.com/avatar/16a20fab8f422ea7f0d63c0c0d433646?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FP-1.png\",\"24x24\":\"https://secure.gravatar.com/avatar/16a20fab8f422ea7f0d63c0c0d433646?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FP-1.png\",\"16x16\":\"https://secure.gravatar.com/avatar/16a20fab8f422ea7f0d63c0c0d433646?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FP-1.png\",\"32x32\":\"https://secure.gravatar.com/avatar/16a20fab8f422ea7f0d63c0c0d433646?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FP-1.png\"},\"displayName\":\"Preeti\",\"active\":true,\"timeZone\":\"Asia/Kolkata\",\"accountType\":\"atlassian\"},\"priority\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/priority/2\",\"iconUrl\":\"https://antbrains.atlassian.net/images/icons/priorities/critical.svg\",\"name\":\"P2\",\"id\":\"2\"},\"status\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/status/10001\",\"description\":\"\",\"iconUrl\":\"https://antbrains.atlassian.net/\",\"name\":\"Done\",\"id\":\"10001\",\"statusCategory\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/statuscategory/3\",\"id\":3,\"key\":\"done\",\"colorName\":\"green\",\"name\":\"Done\"}}}}}\n"; // Replace with actual valid JSON

    }

    @Test
    public void testParseWebhookResponse_ValidJson() {
        String validJson = "{\"timestamp\":1702365396690,\"webhookEvent\":\"comment_deleted\",\"comment\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/issue/33073/comment/53758\",\"id\":\"53758\",\"author\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/user?accountId=62f35c0596eb272011aa421e\",\"accountId\":\"62f35c0596eb272011aa421e\",\"avatarUrls\":{\"48x48\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/48\",\"24x24\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/24\",\"16x16\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/16\",\"32x32\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/32\"},\"displayName\":\"Pulkit Jareda\",\"active\":true,\"timeZone\":\"Asia/Kolkata\",\"accountType\":\"atlassian\"},\"body\":\"Testing snsdkcvnsd\",\"updateAuthor\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/user?accountId=62f35c0596eb272011aa421e\",\"accountId\":\"62f35c0596eb272011aa421e\",\"avatarUrls\":{\"48x48\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/48\",\"24x24\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/24\",\"16x16\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/16\",\"32x32\":\"https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/62f35c0596eb272011aa421e/27c9f2cd-2478-44c5-a3a7-6c422ddd872d/32\"},\"displayName\":\"Pulkit Jareda\",\"active\":true,\"timeZone\":\"Asia/Kolkata\",\"accountType\":\"atlassian\"},\"created\":\"2023-12-12T12:46:07.501+0530\",\"updated\":\"2023-12-12T12:46:22.260+0530\",\"jsdPublic\":true},\"issue\":{\"id\":\"33073\",\"self\":\"https://antbrains.atlassian.net/rest/api/2/33073\",\"key\":\"KE-1231\",\"fields\":{\"summary\":\"Test case Writing and Review\",\"issuetype\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/issuetype/10102\",\"id\":\"10102\",\"description\":\"A small piece of work that's part of a larger task.\",\"iconUrl\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/issuetype/avatar/10316?size=medium\",\"name\":\"Sub-task\",\"subtask\":true,\"avatarId\":10316,\"hierarchyLevel\":-1},\"parent\":{\"id\":\"31848\",\"key\":\"KE-1193\",\"self\":\"https://antbrains.atlassian.net/rest/api/2/issue/31848\",\"fields\":{\"summary\":\"Outlook extension should take input as list of entities and should be able print as table in the extension\",\"status\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/status/6\",\"description\":\"The issue is considered finished, the resolution is correct. Issues which are closed can be reopened.\",\"iconUrl\":\"https://antbrains.atlassian.net/images/icons/statuses/closed.png\",\"name\":\"Closed\",\"id\":\"6\",\"statusCategory\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/statuscategory/3\",\"id\":3,\"key\":\"done\",\"colorName\":\"green\",\"name\":\"Done\"}},\"priority\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/priority/2\",\"iconUrl\":\"https://antbrains.atlassian.net/images/icons/priorities/critical.svg\",\"name\":\"P2\",\"id\":\"2\"},\"issuetype\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/issuetype/10245\",\"id\":\"10245\",\"description\":\"An improvement or enhancement to an existing feature or task.\",\"iconUrl\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/issuetype/avatar/10310?size=medium\",\"name\":\"Improvement\",\"subtask\":false,\"avatarId\":10310,\"hierarchyLevel\":0}}},\"project\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/project/10248\",\"id\":\"10248\",\"key\":\"KE\",\"name\":\"Krista Extensions\",\"projectTypeKey\":\"software\",\"simplified\":false,\"avatarUrls\":{\"48x48\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/project/avatar/10413\",\"24x24\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/project/avatar/10413?size=small\",\"16x16\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/project/avatar/10413?size=xsmall\",\"32x32\":\"https://antbrains.atlassian.net/rest/api/2/universal_avatar/view/type/project/avatar/10413?size=medium\"}},\"assignee\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/user?accountId=641bfdbd407493675d477943\",\"accountId\":\"641bfdbd407493675d477943\",\"avatarUrls\":{\"48x48\":\"https://secure.gravatar.com/avatar/16a20fab8f422ea7f0d63c0c0d433646?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FP-1.png\",\"24x24\":\"https://secure.gravatar.com/avatar/16a20fab8f422ea7f0d63c0c0d433646?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FP-1.png\",\"16x16\":\"https://secure.gravatar.com/avatar/16a20fab8f422ea7f0d63c0c0d433646?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FP-1.png\",\"32x32\":\"https://secure.gravatar.com/avatar/16a20fab8f422ea7f0d63c0c0d433646?d=https%3A%2F%2Favatar-management--avatars.us-west-2.prod.public.atl-paas.net%2Finitials%2FP-1.png\"},\"displayName\":\"Preeti\",\"active\":true,\"timeZone\":\"Asia/Kolkata\",\"accountType\":\"atlassian\"},\"priority\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/priority/2\",\"iconUrl\":\"https://antbrains.atlassian.net/images/icons/priorities/critical.svg\",\"name\":\"P2\",\"id\":\"2\"},\"status\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/status/10001\",\"description\":\"\",\"iconUrl\":\"https://antbrains.atlassian.net/\",\"name\":\"Done\",\"id\":\"10001\",\"statusCategory\":{\"self\":\"https://antbrains.atlassian.net/rest/api/2/statuscategory/3\",\"id\":3,\"key\":\"done\",\"colorName\":\"green\",\"name\":\"Done\"}}}}}\n"; // Replace with actual valid JSON
        Map<String, Object> result = webhookDao.parseWebhookResponse(validJson);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain 'Issue Key'", result.containsKey("Issue Key"));
        assertTrue("Result should contain 'Update'", result.containsKey("Update"));
        assertEquals("KE-1231", result.get("Issue Key"));
    }

    @Test
    public void testParseWebhookResponse_ExtractSpecificField() {
        Map<String, Object> result = webhookDao.parseWebhookResponse(validJson);

        assertTrue("Result should contain 'Update'", result.containsKey("Update"));
        assertNotNull("Update object should not be null", result.get("Update"));

        if (result.get("Update") instanceof List<?>) {
            List<Map<String, Object>> issueList = (List<Map<String, Object>>) result.get("Update");
            assertFalse("Issue list should not be empty", issueList.isEmpty());
            Map<String, Object> firstIssue = issueList.get(0);
            assertEquals("comment_deleted", firstIssue.get("webhookEvent"));
        } else {
            fail("Update object is not a List<Map<String, Object>>");
        }
    }


    @Test(expected = JsonSyntaxException.class)
    public void testParseWebhookResponse_InvalidJson() {
        String invalidJson = "Invalid JSON";
        webhookDao.parseWebhookResponse(invalidJson);
    }


    @Test
    public void testParseWebhookResponse_EmptyJson() {
        String emptyJson = "";
        Map<String, Object> result = webhookDao.parseWebhookResponse(emptyJson);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testParseWebhookResponse_NullJson() {
        Map<String, Object> result = webhookDao.parseWebhookResponse(null);
        assertTrue("Result should be empty", result.isEmpty());
    }

}