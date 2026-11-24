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
import app.krista.sdk.atlassian.client.AbstractBasicAuthRestClient;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthRestClient Tests")
class AuthRestClientTest {

    private Map<String, Object> attributes;
    private Map<String, Object> unreachableAttributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://example.atlassian.net");
        attributes.put(JiraAttributes.USER_NAME, "user@example.com");
        attributes.put(JiraAttributes.AUTH_TOKEN, "token123");
        attributes.put(JiraAttributes.CLIENT_ID, "client-id");
        attributes.put(JiraAttributes.CLIENT_SECRET, "client-secret");

        unreachableAttributes = new HashMap<>();
        unreachableAttributes.put(JiraAttributes.HOST_URL, "http://127.0.0.1:1");
        unreachableAttributes.put(JiraAttributes.USER_NAME, "user@test.com");
        unreachableAttributes.put(JiraAttributes.AUTH_TOKEN, "token123");
        unreachableAttributes.put(JiraAttributes.CLIENT_ID, "clientId");
        unreachableAttributes.put(JiraAttributes.CLIENT_SECRET, "clientSecret");
    }

    /**
     * Injects a pre-built mock WebTarget into the parent class's private
     * {@code webTarget} field, bypassing Jersey client creation entirely.
     * The javax/jakarta namespace conflict on the test classpath would
     * otherwise cause {@code ClientBuilder.newClient()} to fail before any
     * JaCoCo probe inside the production method body can fire.
     */
    private static void injectWebTarget(AuthRestClient client, WebTarget wt) throws Exception {
        Field field = AbstractBasicAuthRestClient.class.getDeclaredField("webTarget");
        field.setAccessible(true);
        field.set(client, wt);
    }

    // ── Constructor tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("Constructor with valid attributes does not throw")
    void testConstructor_ValidAttributes_DoesNotThrow() {
        assertDoesNotThrow(() -> new AuthRestClient(attributes, false, false));
    }

    @Test
    @DisplayName("Constructor with invokeAsUser=true does not throw")
    void testConstructor_InvokeAsUserTrue_DoesNotThrow() {
        assertDoesNotThrow(() -> new AuthRestClient(attributes, true, false));
    }

    @Test
    @DisplayName("Constructor with callFromValidateAttributes=true does not throw")
    void testConstructor_CallFromValidateAttributesTrue_DoesNotThrow() {
        assertDoesNotThrow(() -> new AuthRestClient(attributes, false, true));
    }

    // ── attachFile — null / blank resource ──────────────────────────────────

    @Test
    @DisplayName("attachFile null resource throws IllegalArgumentException with message resource not found")
    void testAttachFile_NullResource_ThrowsIllegalArgumentException() {
        AuthRestClient client = new AuthRestClient(attributes, false, false);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile(null, List.of(new File("test.txt")))
        );
        assertEquals("resource not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile blank resource throws IllegalArgumentException with message resource not found")
    void testAttachFile_BlankResource_ThrowsIllegalArgumentException() {
        AuthRestClient client = new AuthRestClient(attributes, false, false);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("   ", List.of(new File("test.txt")))
        );
        assertEquals("resource not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile empty-string resource throws IllegalArgumentException with message resource not found")
    void testAttachFile_EmptyStringResource_ThrowsIllegalArgumentException() {
        AuthRestClient client = new AuthRestClient(attributes, false, false);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("", List.of(new File("test.txt")))
        );
        assertEquals("resource not found", ex.getMessage());
    }

    // ── attachFile — null / empty files ─────────────────────────────────────

    @Test
    @DisplayName("attachFile null files throws IllegalArgumentException with message file not found")
    void testAttachFile_NullFiles_ThrowsIllegalArgumentException() {
        AuthRestClient client = new AuthRestClient(attributes, false, false);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("PROJ-123", null)
        );
        assertEquals("file not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile empty files list throws IllegalArgumentException with message file not found")
    void testAttachFile_EmptyFilesList_ThrowsIllegalArgumentException() {
        AuthRestClient client = new AuthRestClient(attributes, false, false);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("PROJ-123", Collections.emptyList())
        );
        assertEquals("file not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile empty files via List.of throws IllegalArgumentException with message file not found")
    void testAttachFile_EmptyFilesViaListOf_ThrowsIllegalArgumentException() {
        AuthRestClient client = new AuthRestClient(attributes, false, false);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("PROJ-123", List.of())
        );
        assertEquals("file not found", ex.getMessage());
    }

    // ── attachFile — uploadMultipart path (line 102) ─────────────────────────

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("attachFile with valid resource and non-empty files calls uploadMultipart and returns response")
    void testAttachFile_ValidResourceAndFiles_CallsUploadMultipart() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);
        File mockFile = mock(File.class);

        HttpRequestWithBody mockPost = mock(HttpRequestWithBody.class);
        MultipartBody mockMultipart = mock(MultipartBody.class);
        HttpResponse<JsonNode> mockHttpResponse = mock(HttpResponse.class);

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.post(anyString())).thenReturn(mockPost);
            when(mockPost.basicAuth(anyString(), anyString())).thenReturn(mockPost);
            when(mockPost.header(anyString(), anyString())).thenReturn(mockPost);
            when(mockPost.field(anyString(), any(Collection.class))).thenReturn(mockMultipart);
            doReturn(mockHttpResponse).when(mockMultipart).asJson();

            HttpResponse<JsonNode> result = client.attachFile("PROJ-123", List.of(mockFile));
            assertNotNull(result);
        }
    }

    // ── parseAndValidateJQL — all branches via injected WebTarget ───────────

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL success response returns json string")
    void testParseAndValidateJQL_SuccessResponse_ReturnsJson() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockResp.readEntity(String.class)).thenReturn("{\"queries\":[]}");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);

        injectWebTarget(client, mockWt);
        try (MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            when(mockBuilder.post(mockEntity)).thenReturn(mockResp);
            String result = client.parseAndValidateJQL("project = TEST", null, null, null);
            assertEquals("{\"queries\":[]}", result);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL 404 response throws IllegalStateException with host URL message")
    void testParseAndValidateJQL_404Response_ThrowsIllegalStateException() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockResp.readEntity(String.class)).thenReturn("");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
        when(mockResp.getStatus()).thenReturn(404);

        injectWebTarget(client, mockWt);
        try (MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            when(mockBuilder.post(mockEntity)).thenReturn(mockResp);
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> client.parseAndValidateJQL("project = TEST", null, null, null));
            assertEquals("Please Provide Valid Host URL", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL 400 response throws IllegalStateException with auth token message")
    void testParseAndValidateJQL_400Response_ThrowsIllegalStateException() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockResp.readEntity(String.class)).thenReturn("");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
        when(mockResp.getStatus()).thenReturn(400);

        injectWebTarget(client, mockWt);
        try (MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            when(mockBuilder.post(mockEntity)).thenReturn(mockResp);
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> client.parseAndValidateJQL("project = TEST", null, null, null));
            assertEquals("Please Provide Valid Auth Token OR User Name", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL other error response returns empty string")
    void testParseAndValidateJQL_OtherErrorResponse_ReturnsEmpty() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockResp.readEntity(String.class)).thenReturn("");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.SERVER_ERROR);
        when(mockResp.getStatus()).thenReturn(500);

        injectWebTarget(client, mockWt);
        try (MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            when(mockBuilder.post(mockEntity)).thenReturn(mockResp);
            String result = client.parseAndValidateJQL("project = TEST", null, null, null);
            assertEquals("", result);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL null response covers try-with-resources null-close branch")
    void testParseAndValidateJQL_NullResponse_ThrowsNullPointerException() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        injectWebTarget(client, mockWt);
        try (MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            // null response causes NPE in body, which takes the r==null branch in the
            // try-with-resources finally block (JaCoCo implicit null-check on close).
            when(mockBuilder.post(mockEntity)).thenReturn(null);
            assertThrows(NullPointerException.class,
                    () -> client.parseAndValidateJQL("project = TEST", null, null, null));
        }
    }

    // ── fetchIssues(Boolean, ...) — success and error branches ───────────────

    @Test
    @DisplayName("fetchIssues with Boolean invokeAsUser - success response returns entity")
    void testFetchIssues_Boolean_SuccessResponse_ReturnsEntity() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);

        when(mockWt.path(anyString())).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockBuilder.get()).thenReturn(mockResp);
        when(mockResp.readEntity(String.class)).thenReturn("{\"issues\":[]}");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);

        injectWebTarget(client, mockWt);
        String result = client.fetchIssues(false, null, null,
                "rest/api/3/search", Collections.emptyMap(), String.class);
        assertEquals("{\"issues\":[]}", result);
    }

    @Test
    @DisplayName("fetchIssues with Boolean invokeAsUser - error response throws IllegalStateException")
    void testFetchIssues_Boolean_ErrorResponse_ThrowsIllegalStateException() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);

        when(mockWt.path(anyString())).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockBuilder.get()).thenReturn(mockResp);
        when(mockResp.readEntity(String.class)).thenReturn("Unauthorized");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);

        injectWebTarget(client, mockWt);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.fetchIssues(false, null, null,
                        "rest/api/3/search", Collections.emptyMap(), String.class));
        assertEquals("Unauthorized", ex.getMessage());
    }

    // ── fetchIssues(String, String, ...) — success and error branches ─────────

    @Test
    @DisplayName("fetchIssues with accountId and invokerId - success response returns entity")
    void testFetchIssues_String_SuccessResponse_ReturnsEntity() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);

        when(mockWt.path(anyString())).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockBuilder.get()).thenReturn(mockResp);
        when(mockResp.readEntity(String.class)).thenReturn("{\"key\":\"PROJ-1\"}");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);

        injectWebTarget(client, mockWt);
        String result = client.fetchIssues("account1", "invoker1",
                "rest/api/3/issue/PROJ-1", Collections.emptyMap(), String.class);
        assertEquals("{\"key\":\"PROJ-1\"}", result);
    }

    @Test
    @DisplayName("fetchIssues with accountId and invokerId - error response throws IllegalStateException")
    void testFetchIssues_String_ErrorResponse_ThrowsIllegalStateException() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);

        when(mockWt.path(anyString())).thenReturn(pathTarget);
        when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockBuilder.get()).thenReturn(mockResp);
        when(mockResp.readEntity(String.class)).thenReturn("Not Found");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);

        injectWebTarget(client, mockWt);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.fetchIssues("account1", "invoker1",
                        "rest/api/3/issue/PROJ-1", Collections.emptyMap(), String.class));
        assertEquals("Not Found", ex.getMessage());
    }

    // ── fetchIssues with queryParams (applyQueryParams coverage) ─────────────

    @Test
    @DisplayName("fetchIssues with non-empty queryParams exercises applyQueryParams")
    void testFetchIssues_WithQueryParams_ExercisesApplyQueryParams() throws Exception {
        AuthRestClient client = new AuthRestClient(attributes, false, false);

        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        WebTarget queryTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);

        when(mockWt.path(anyString())).thenReturn(pathTarget);
        when(pathTarget.queryParam(eq("jql"), any(Object[].class))).thenReturn(queryTarget);
        when(queryTarget.request(any(String[].class))).thenReturn(mockBuilder);
        when(mockBuilder.get()).thenReturn(mockResp);
        when(mockResp.readEntity(String.class)).thenReturn("{}");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);

        Map<String, Object> params = new HashMap<>();
        params.put("jql", "project = TEST");
        injectWebTarget(client, mockWt);
        String result = client.fetchIssues(false, null, null,
                "rest/api/3/search", params, String.class);
        assertEquals("{}", result);
    }

    // ── RestClient default method coverage ──────────────────────────────────

    @Test
    @DisplayName("getJiraAccessToken default method delegates to getAccessToken without throwing")
    void testGetJiraAccessToken_DelegatesToGetAccessToken() {
        AuthRestClient client = new AuthRestClient(attributes, false, false);
        // The default method getJiraAccessToken() simply delegates to getAccessToken().
        // For basic-auth clients there is no OAuth token, so null is a valid return value.
        assertDoesNotThrow(client::getJiraAccessToken);
    }

    // ── Annotation / class-structure tests ──────────────────────────────────

    @Test
    @DisplayName("AuthRestClient is a final class")
    void testClassIsFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(AuthRestClient.class.getModifiers()));
    }

    @Test
    @DisplayName("AuthRestClient implements RestClient")
    void testImplementsRestClient() {
        assertTrue(RestClient.class.isAssignableFrom(AuthRestClient.class));
    }
}
