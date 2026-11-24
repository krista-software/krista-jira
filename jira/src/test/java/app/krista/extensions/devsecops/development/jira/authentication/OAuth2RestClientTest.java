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

import app.krista.sdk.atlassian.oauth.AccessibleResource;
import app.krista.sdk.atlassian.oauth.AtlassianTokenSource;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OAuth2RestClient Tests")
class OAuth2RestClientTest {

    @Mock private JiraService jiraService;
    @Mock private AtlassianTokenSource tokenSource;

    private AccessibleResource accessibleResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accessibleResource = new AccessibleResource("test-cloud-id", List.of());
        when(tokenSource.getAccessibleResources()).thenReturn(accessibleResource);
        when(tokenSource.getAccessibleResources(any(), any())).thenReturn(accessibleResource);
        when(tokenSource.getToken()).thenReturn("bearer-token");
        when(tokenSource.getToken(anyString(), anyString())).thenReturn("bearer-token");
    }

    // ── Constructor tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("Constructor with JiraService does not throw")
    void testConstructorWithJiraService_DoesNotThrow() {
        assertDoesNotThrow(() -> new OAuth2RestClient(jiraService));
    }

    @Test
    @DisplayName("Constructor with JiraService returns non-null instance")
    void testConstructorWithJiraService_ReturnsNonNullInstance() {
        OAuth2RestClient client = new OAuth2RestClient(jiraService);
        assertNotNull(client);
    }

    @Test
    @DisplayName("Constructor with AtlassianTokenSource does not throw")
    void testConstructorWithTokenSource_DoesNotThrow() {
        assertDoesNotThrow(() -> new OAuth2RestClient(tokenSource));
    }

    @Test
    @DisplayName("Constructor with AtlassianTokenSource returns non-null instance")
    void testConstructorWithTokenSource_ReturnsNonNullInstance() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);
        assertNotNull(client);
    }

    // ── attachFile — null / blank resource ──────────────────────────────────

    @Test
    @DisplayName("attachFile null resource throws IllegalArgumentException with message resource not found")
    void testAttachFile_NullResource_ThrowsIllegalArgumentException() {
        OAuth2RestClient client = new OAuth2RestClient(jiraService);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile(null, List.of(new File("test.txt")))
        );
        assertEquals("resource not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile blank resource throws IllegalArgumentException with message resource not found")
    void testAttachFile_BlankResource_ThrowsIllegalArgumentException() {
        OAuth2RestClient client = new OAuth2RestClient(jiraService);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("   ", List.of(new File("test.txt")))
        );
        assertEquals("resource not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile empty-string resource throws IllegalArgumentException with message resource not found")
    void testAttachFile_EmptyStringResource_ThrowsIllegalArgumentException() {
        OAuth2RestClient client = new OAuth2RestClient(jiraService);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("", List.of(new File("test.txt")))
        );
        assertEquals("resource not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile null resource throws IllegalArgumentException with tokenSource constructor")
    void testAttachFile_NullResource_TokenSourceConstructor_ThrowsIllegalArgumentException() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile(null, List.of(new File("test.txt")))
        );
        assertEquals("resource not found", ex.getMessage());
    }

    // ── attachFile — null / empty files ─────────────────────────────────────

    @Test
    @DisplayName("attachFile null files throws IllegalArgumentException with message file not found")
    void testAttachFile_NullFiles_ThrowsIllegalArgumentException() {
        OAuth2RestClient client = new OAuth2RestClient(jiraService);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("PROJ-123", null)
        );
        assertEquals("file not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile empty files list throws IllegalArgumentException with message file not found")
    void testAttachFile_EmptyFilesList_ThrowsIllegalArgumentException() {
        OAuth2RestClient client = new OAuth2RestClient(jiraService);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("PROJ-123", Collections.emptyList())
        );
        assertEquals("file not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile empty files via List.of throws IllegalArgumentException with message file not found")
    void testAttachFile_EmptyFilesViaListOf_ThrowsIllegalArgumentException() {
        OAuth2RestClient client = new OAuth2RestClient(jiraService);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("PROJ-123", List.of())
        );
        assertEquals("file not found", ex.getMessage());
    }

    @Test
    @DisplayName("attachFile null files throws IllegalArgumentException with tokenSource constructor")
    void testAttachFile_NullFiles_TokenSourceConstructor_ThrowsIllegalArgumentException() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> client.attachFile("PROJ-123", null)
        );
        assertEquals("file not found", ex.getMessage());
    }

    // ── attachFile — uploadMultipart path (lines 100-102) ────────────────────

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("attachFile with valid resource and non-empty files calls uploadMultipart and returns response")
    void testAttachFile_ValidResourceNonEmptyFiles_CallsUploadMultipart() throws Exception {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);
        File mockFile = mock(File.class);

        HttpRequestWithBody mockPost = mock(HttpRequestWithBody.class);
        MultipartBody mockMultipart = mock(MultipartBody.class);
        HttpResponse<JsonNode> mockHttpResponse = mock(HttpResponse.class);

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.post(anyString())).thenReturn(mockPost);
            when(mockPost.header(anyString(), anyString())).thenReturn(mockPost);
            when(mockPost.field(anyString(), any(Collection.class))).thenReturn(mockMultipart);
            doReturn(mockHttpResponse).when(mockMultipart).asJson();

            HttpResponse<JsonNode> result = client.attachFile("PROJ-123", List.of(mockFile));
            assertNotNull(result);
        }
    }

    // ── parseAndValidateJQL — all branches via MockedStatic<ClientBuilder> + MockedStatic<Entity> ───

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL success response returns json string")
    void testParseAndValidateJQL_SuccessResponse_ReturnsJson() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);

        Client mockClient = mock(Client.class);
        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockResp.readEntity(String.class)).thenReturn("{\"queries\":[]}");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);

        try (MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class);
             MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWt);
            when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
            when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            when(mockBuilder.post(mockEntity)).thenReturn(mockResp);

            String result = client.parseAndValidateJQL("project = TEST", null, "account1", "invoker1");
            assertEquals("{\"queries\":[]}", result);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL 404 response throws IllegalStateException with host URL message")
    void testParseAndValidateJQL_404Response_ThrowsIllegalStateException() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);

        Client mockClient = mock(Client.class);
        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockResp.readEntity(String.class)).thenReturn("");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
        when(mockResp.getStatus()).thenReturn(404);

        try (MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class);
             MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWt);
            when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
            when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            when(mockBuilder.post(mockEntity)).thenReturn(mockResp);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> client.parseAndValidateJQL("project = TEST", null, "account1", "invoker1"));
            assertEquals("Please Provide Valid Host URL", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL 400 response throws IllegalStateException with auth token message")
    void testParseAndValidateJQL_400Response_ThrowsIllegalStateException() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);

        Client mockClient = mock(Client.class);
        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockResp.readEntity(String.class)).thenReturn("");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
        when(mockResp.getStatus()).thenReturn(400);

        try (MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class);
             MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWt);
            when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
            when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            when(mockBuilder.post(mockEntity)).thenReturn(mockResp);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> client.parseAndValidateJQL("project = TEST", null, "account1", "invoker1"));
            assertEquals("Please Provide Valid Auth Token OR User Name", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL other error response returns empty string")
    void testParseAndValidateJQL_OtherErrorResponse_ReturnsEmpty() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);

        Client mockClient = mock(Client.class);
        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        Entity<Object> mockEntity = mock(Entity.class);

        when(mockResp.readEntity(String.class)).thenReturn("");
        when(mockResp.getStatusInfo()).thenReturn(mockStatus);
        when(mockStatus.getFamily()).thenReturn(Response.Status.Family.SERVER_ERROR);
        when(mockResp.getStatus()).thenReturn(500);

        try (MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class);
             MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWt);
            when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
            when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            when(mockBuilder.post(mockEntity)).thenReturn(mockResp);

            String result = client.parseAndValidateJQL("project = TEST", null, "account1", "invoker1");
            assertEquals("", result);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("parseAndValidateJQL null response covers try-with-resources null-close branch")
    void testParseAndValidateJQL_NullResponse_ThrowsNullPointerException() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);

        Client mockClient = mock(Client.class);
        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Entity<Object> mockEntity = mock(Entity.class);

        try (MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class);
             MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
            cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWt);
            when(mockWt.path(eq("jql/parse"))).thenReturn(pathTarget);
            when(pathTarget.request(any(String[].class))).thenReturn(mockBuilder);
            entityMock.when(() -> Entity.entity(anyString(), anyString())).thenReturn(mockEntity);
            // null response causes NPE in body, which takes the r==null branch in the
            // try-with-resources finally block (JaCoCo implicit null-check on close).
            when(mockBuilder.post(mockEntity)).thenReturn(null);

            assertThrows(NullPointerException.class,
                    () -> client.parseAndValidateJQL("project = TEST", null, "account1", "invoker1"));
        }
    }

    // ── fetchIssues(Boolean, ...) — delegates to fetchIssues(String, ...) ────

    @Test
    @DisplayName("fetchIssues with Boolean invokeAsUser delegates to String-based fetchIssues and returns entity")
    void testFetchIssues_Boolean_DelegatesToStringVariant_ReturnsEntity() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);

        Client mockClient = mock(Client.class);
        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilderIntermediate = mock(Invocation.Builder.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);

        try (MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class)) {
            cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWt);
            when(mockWt.path(anyString())).thenReturn(pathTarget);
            when(pathTarget.request(any(String[].class))).thenReturn(mockBuilderIntermediate);
            when(mockBuilderIntermediate.header(anyString(), any())).thenReturn(mockBuilder);
            when(mockBuilder.get()).thenReturn(mockResp);
            when(mockResp.readEntity(String.class)).thenReturn("{\"total\":0}");

            String result = client.fetchIssues(false, "account1", "invoker1",
                    "rest/api/3/search", Collections.emptyMap(), String.class);
            assertEquals("{\"total\":0}", result);
        }
    }

    // ── fetchIssues(String, String, ...) — direct coverage ───────────────────

    @Test
    @DisplayName("fetchIssues with accountId and invokerId - success response returns entity")
    void testFetchIssues_String_SuccessResponse_ReturnsEntity() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);

        Client mockClient = mock(Client.class);
        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilderIntermediate = mock(Invocation.Builder.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);

        try (MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class)) {
            cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWt);
            when(mockWt.path(anyString())).thenReturn(pathTarget);
            when(pathTarget.request(any(String[].class))).thenReturn(mockBuilderIntermediate);
            when(mockBuilderIntermediate.header(anyString(), any())).thenReturn(mockBuilder);
            when(mockBuilder.get()).thenReturn(mockResp);
            when(mockResp.readEntity(String.class)).thenReturn("{\"key\":\"PROJ-1\"}");

            String result = client.fetchIssues("account1", "invoker1",
                    "rest/api/3/issue/PROJ-1", Collections.emptyMap(), String.class);
            assertEquals("{\"key\":\"PROJ-1\"}", result);
        }
    }

    @Test
    @DisplayName("fetchIssues with non-empty queryParams exercises applyQueryParams")
    void testFetchIssues_String_WithQueryParams_ExercisesApplyQueryParams() {
        OAuth2RestClient client = new OAuth2RestClient(tokenSource);

        Client mockClient = mock(Client.class);
        WebTarget mockWt = mock(WebTarget.class);
        WebTarget pathTarget = mock(WebTarget.class);
        WebTarget queryTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilderIntermediate = mock(Invocation.Builder.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResp = mock(Response.class);

        try (MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class)) {
            cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWt);
            when(mockWt.path(anyString())).thenReturn(pathTarget);
            when(pathTarget.queryParam(eq("jql"), any(Object[].class))).thenReturn(queryTarget);
            when(queryTarget.request(any(String[].class))).thenReturn(mockBuilderIntermediate);
            when(mockBuilderIntermediate.header(anyString(), any())).thenReturn(mockBuilder);
            when(mockBuilder.get()).thenReturn(mockResp);
            when(mockResp.readEntity(String.class)).thenReturn("{}");

            Map<String, Object> params = new HashMap<>();
            params.put("jql", "project = TEST");
            String result = client.fetchIssues("account1", "invoker1",
                    "rest/api/3/search", params, String.class);
            assertEquals("{}", result);
        }
    }

    // ── Annotation / class-structure tests ──────────────────────────────────

    @Test
    @DisplayName("OAuth2RestClient is a final class")
    void testClassIsFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(OAuth2RestClient.class.getModifiers()));
    }

    @Test
    @DisplayName("OAuth2RestClient implements RestClient")
    void testImplementsRestClient() {
        assertTrue(RestClient.class.isAssignableFrom(OAuth2RestClient.class));
    }

    @Test
    @DisplayName("OAuth2RestClient has Service annotation")
    void testHasServiceAnnotation() {
        assertTrue(OAuth2RestClient.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("OAuth2RestClient JiraService constructor has Inject annotation")
    void testJiraServiceConstructorHasInjectAnnotation() throws NoSuchMethodException {
        var constructor = OAuth2RestClient.class.getConstructor(JiraService.class);
        assertTrue(constructor.isAnnotationPresent(javax.inject.Inject.class));
    }
}
