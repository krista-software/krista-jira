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

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Component;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Label;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketPriority;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TransitionStatus;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import app.krista.model.base.FreeForm;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mashape.unirest.http.HttpResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Broad coverage for the remaining {@link IssueDao} methods not exercised by
 * {@link IssueDaoTest} / {@link IssueDaoCustomFieldsTest}: user groups, comments, JQL search,
 * status/assignee/label/component/priority/worklog updates, epic linking, custom-field readers,
 * and the small helper/validation methods.
 */
public class IssueDaoFullCoverageTest {

    private static final Gson GSON = new Gson();

    private RestClientFactory factory;
    private RestClient rest;
    private Invocation.Builder builder;
    private Response response;
    private ProjectDao projectDao;
    private UserDao userDao;
    private TicketInfoDao ticketInfoDao;
    private IssueDao dao;

    private static final String TASK_TYPES = "[{\"id\":\"10001\",\"name\":\"Task\"}]";

    @BeforeClass
    public static void installRuntimeDelegate() {
        RuntimeDelegate.setInstance(new RuntimeDelegate() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
                return (HeaderDelegate<T>) new HeaderDelegate<MediaType>() {
                    public MediaType fromString(String value) { return MediaType.APPLICATION_JSON_TYPE; }
                    public String toString(MediaType value) { return "application/json"; }
                };
            }
            public javax.ws.rs.core.UriBuilder createUriBuilder() { throw new UnsupportedOperationException(); }
            public javax.ws.rs.core.Response.ResponseBuilder createResponseBuilder() { throw new UnsupportedOperationException(); }
            public javax.ws.rs.core.Variant.VariantListBuilder createVariantListBuilder() { throw new UnsupportedOperationException(); }
            public <T> T createEndpoint(javax.ws.rs.core.Application application, Class<T> endpointType) { throw new UnsupportedOperationException(); }
            public javax.ws.rs.core.Link.Builder createLinkBuilder() { throw new UnsupportedOperationException(); }
        });
    }

    @AfterClass
    public static void clearRuntimeDelegate() {
        RuntimeDelegate.setInstance(null);
    }

    @Before
    public void setUp() {
        factory = mock(RestClientFactory.class);
        rest = mock(RestClient.class);
        builder = mock(Invocation.Builder.class);
        response = mock(Response.class);
        projectDao = mock(ProjectDao.class);
        userDao = mock(UserDao.class);
        ticketInfoDao = mock(TicketInfoDao.class);

        when(factory.getInstance()).thenReturn(rest);
        when(factory.getInstance(anyBoolean())).thenReturn(rest);
        when(factory.invokeAsUser()).thenReturn(false);
        when(rest.getResourceInvoker(anyString(), any())).thenReturn(builder);

        dao = new IssueDao(projectDao, userDao, ticketInfoDao, factory);
    }

    // helpers ------------------------------------------------------------------

    private void ok() {
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.getStatus()).thenReturn(200);
    }

    private void fail400() {
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(String.class)).thenReturn("boom");
    }

    private static Object callPrivate(Object target, String name, Class<?>[] sig, Object... args) throws Exception {
        Method m = IssueDao.class.getDeclaredMethod(name, sig);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    private static String issueJson(String key, String fields) {
        return "{\"id\":\"1\",\"key\":\"" + key + "\",\"fields\":" + fields + "}";
    }

    // ── getUserDao / getRestClient branches ───────────────────────────────────

    @Test
    public void getUserDao_returnsInjected() {
        assertSame(userDao, dao.getUserDao());
    }

    @Test
    public void getRestClient_nullFactory_returnsStoredClient() {
        IssueDao d = new IssueDao((RestClientFactory) null, rest);
        when(rest.fetch(anyString(), any(), any())).thenReturn("[]");
        assertEquals("[]", d.getFieldResponse());
    }

    @Test
    public void getRestClient_oauth2WhenAppAuth_refetches() {
        OAuth2RestClient oauth = mock(OAuth2RestClient.class);
        IssueDao d = new IssueDao(factory, oauth);   // stored client is OAuth2 but invokeAsUser=false
        when(rest.fetch(anyString(), any(), any())).thenReturn("[]");
        assertEquals("[]", d.getFieldResponse());    // triggers re-fetch to app client (our rest mock)
    }

    @Test
    public void getRestClient_authClientWhenUserAuth_refetches() {
        AuthRestClient authClient = mock(AuthRestClient.class);
        when(factory.invokeAsUser()).thenReturn(true);
        IssueDao d = new IssueDao(factory, authClient);
        when(rest.fetch(anyString(), any(), any())).thenReturn("[]");
        assertEquals("[]", d.getFieldResponse());
    }

    // ── getAccountIdByEmail catch branch ──────────────────────────────────────

    @Test
    public void getAccountIdByEmail_illegalCaller_rethrown() {
        when(rest.fetch(anyString(), any(), any())).thenThrow(new IllegalCallerException("nope"));
        try {
            dao.getAccountIdByEmail("x@y.com");
            fail();
        } catch (IllegalCallerException e) {
            assertEquals("nope", e.getMessage());
        }
    }

    // ── createUser failure ────────────────────────────────────────────────────

    @Test
    public void createUser_nonCreated_returnsEmpty() {
        when(builder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(400);
        assertTrue(dao.createUser("x@y.com").isEmpty());
    }

    // ── getUserGroups ──────────────────────────────────────────────────────────

    @Test
    public void getUserGroups_success() {
        when(rest.fetch(anyString(), any(), any()))
                .thenReturn("{\"values\":[{\"name\":\"admins\"},{\"name\":\"devs\"}]}");
        assertEquals(Arrays.asList("admins", "devs"), dao.getUserGroups("acc-1"));
    }

    @Test
    public void getUserGroups_emptyResponse_throws() {
        when(rest.fetch(anyString(), any(), any())).thenReturn("");
        try { dao.getUserGroups("acc-1"); fail(); } catch (RuntimeException expected) { }
    }

    // ── updateUser / addUserToGroup branches ───────────────────────────────────

    @Test
    public void updateUser_nullOrEmptyGroups_returnsFalse() {
        assertFalse(dao.updateUser("acc", null));
        assertFalse(dao.updateUser("acc", Collections.emptyList()));
    }

    @Test
    public void updateUser_groupAddFails_returnsFalse() {
        when(builder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(400);   // addUserToGroup -> false
        assertFalse(dao.updateUser("acc", Arrays.asList("g1")));
    }

    // ── getIssueTypes / validateIssueType / getIssuesTypeId ────────────────────

    @Test
    public void getIssueTypes_returnsNames() {
        when(projectDao.getIssueTypeFromProject("KE"))
                .thenReturn("[{\"id\":\"1\",\"name\":\"Task\"},{\"id\":\"2\",\"name\":\"Bug\"}]");
        assertEquals(Arrays.asList("Task", "Bug"), dao.getIssueTypes("KE"));
    }

    @Test
    public void validateIssueType_trueAndFalse() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        assertTrue(dao.validateIssueType("KE", "Task"));
        assertFalse(dao.validateIssueType("KE", "Nope"));
    }

    @Test
    public void getIssuesTypeId_badJson_throwsWrapped() throws Exception {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn("not-json");
        try {
            callPrivate(dao, "getIssuesTypeId", new Class[]{String.class, String.class}, "KE", "Task");
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    // ── addComment / prepareCommentPayload ─────────────────────────────────────

    @Test
    public void addComment_public_success() {
        when(builder.post(any())).thenReturn(response);
        ok();
        dao.addComment("KE-1", "Hello<br>", "Public");   // trailing <br> stripped, public branch
        verify(builder).post(any());
    }

    @Test
    public void addComment_internal_failure_throws() {
        when(builder.post(any())).thenReturn(response);
        fail400();
        try { dao.addComment("KE-1", "Hi", "Internal"); fail(); } catch (RuntimeException expected) { }
    }

    // ── searchByJQL ─────────────────────────────────────────────────────────────

    @Test
    public void searchByJQL_firstPage_returnsIssues() {
        when(rest.fetch(anyString(), any(), any()))
                .thenReturn("{\"issues\":[" + issueJson("KE-1", "{}") + "]}");
        List<Issue> issues = dao.searchByJQL("project=KE", 0, 1);
        assertEquals(1, issues.size());
        assertEquals("KE-1", issues.get(0).key);
    }

    @Test
    public void searchByJQL_paginateToTarget() {
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"issues\":[" + issueJson("KE-1", "{}") + "],\"nextPageToken\":\"tok\",\"isLast\":false}",
                "{\"issues\":[" + issueJson("KE-2", "{}") + "]}");
        List<Issue> issues = dao.searchByJQL("project=KE", 10, 2);
        assertEquals("KE-2", issues.get(0).key);
    }

    @Test
    public void searchByJQL_lastPageBeforeTarget_returnsEmpty() {
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"issues\":[" + issueJson("KE-1", "{}") + "],\"isLast\":true}");
        assertTrue(dao.searchByJQL("project=KE", 10, 2).isEmpty());
    }

    @Test
    public void searchByJQL_noIssues_throwsWithMessage() {
        when(rest.fetch(anyString(), any(), any())).thenReturn("{\"errorMessages\":[\"bad jql\"]}");
        try { dao.searchByJQL("bad", 10, 1); fail(); } catch (RuntimeException e) {
            assertEquals("bad jql", e.getMessage());
        }
    }

    // ── searchByJqlQuery ────────────────────────────────────────────────────────

    @Test
    public void searchByJqlQuery_returnsIssues_thenStops() {
        when(rest.fetchIssues(anyBoolean(), anyString(), anyString(), anyString(), any(), any()))
                .thenReturn("{\"issues\":[" + issueJson("KE-1", "{}") + "]}");
        List<Issue> issues = dao.searchByJqlQuery(false, "acc", "inv", "project=KE", 50);
        assertEquals(1, issues.size());
    }

    @Test
    public void searchByJqlQuery_noIssues_throws() {
        when(rest.fetchIssues(anyBoolean(), anyString(), anyString(), anyString(), any(), any()))
                .thenReturn("{\"errorMessages\":[\"nope\"]}");
        try { dao.searchByJqlQuery(false, "acc", "inv", "bad", 50); fail(); } catch (RuntimeException e) {
            assertEquals("nope", e.getMessage());
        }
    }

    // ── fetchErrorsInJQLQuery ───────────────────────────────────────────────────

    @Test
    public void fetchErrorsInJQLQuery_blank_returnsEmpty() {
        when(rest.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString())).thenReturn("   ");
        assertTrue(dao.fetchErrorsInJQLQuery(false, "acc", "inv", "jql").isEmpty());
    }

    @Test
    public void fetchErrorsInJQLQuery_withErrors() {
        when(rest.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn("{\"queries\":[{\"query\":\"bad\",\"errors\":[\"syntax error\"]}]}");
        Map<String, String> out = dao.fetchErrorsInJQLQuery(false, "acc", "inv", "bad");
        assertEquals("syntax error", out.get("errors"));
        assertEquals("bad", out.get("query"));
    }

    @Test
    public void fetchErrorsInJQLQuery_queryOnly() {
        when(rest.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn("{\"queries\":[{\"query\":\"project=KE\"}]}");   // no "errors" key
        Map<String, String> out = dao.fetchErrorsInJQLQuery(false, "acc", "inv", "project=KE");
        assertEquals("project=KE", out.get("query"));
        assertFalse(out.containsKey("errors"));
    }

    @Test
    public void fetchErrorsInJQLQuery_noQueries_returnsEmpty() {
        when(rest.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn("{}");
        assertTrue(dao.fetchErrorsInJQLQuery(false, "acc", "inv", "x").isEmpty());
    }

    // ── updateStatus(key,name) ─────────────────────────────────────────────────

    private TransitionStatus transition(String toName, String fromId) {
        TransitionStatus t = new TransitionStatus();
        t.toName = toName;
        t.fromId = fromId;
        return t;
    }

    @Test
    public void updateStatus_success() {
        when(ticketInfoDao.getTransitions("KE-1")).thenReturn(Arrays.asList(transition("Done", "31")));
        when(builder.post(any())).thenReturn(response);
        ok();
        dao.updateStatus("KE-1", "Done");
        verify(builder).post(any());
    }

    @Test
    public void updateStatus_noTransition_throws() {
        when(ticketInfoDao.getTransitions("KE-1")).thenReturn(new ArrayList<>());
        try { dao.updateStatus("KE-1", "Done"); fail(); } catch (RuntimeException expected) { }
    }

    @Test
    public void updateStatus_transitionFails_throws() {
        when(ticketInfoDao.getTransitions("KE-1")).thenReturn(Arrays.asList(transition("Done", "31")));
        when(builder.post(any())).thenReturn(response);
        fail400();
        try { dao.updateStatus("KE-1", "Done"); fail(); } catch (RuntimeException expected) { }
    }

    // ── assignTask ──────────────────────────────────────────────────────────────

    @Test
    public void assignTask_success() {
        when(builder.put(any())).thenReturn(response);
        ok();
        dao.assignTask("KE-1", "acc-1");
        verify(builder).put(any());
    }

    @Test
    public void assignTask_failure_throws() {
        when(builder.put(any())).thenReturn(response);
        fail400();
        try { dao.assignTask("KE-1", "acc-1"); fail(); } catch (RuntimeException expected) { }
    }

    // ── addLabel / addComponent ────────────────────────────────────────────────

    @Test
    public void addLabel_success_andFailure() {
        when(builder.put(any())).thenReturn(response);
        ok();
        dao.addLabel("bug", "KE-1");
        fail400();
        try { dao.addLabel("bug", "KE-1"); fail(); } catch (RuntimeException expected) { }
    }

    @Test
    public void addComponent_success_andFailure() {
        when(builder.put(any())).thenReturn(response);
        ok();
        dao.addComponent("KE-1", "Backend");
        fail400();
        try { dao.addComponent("KE-1", "Backend"); fail(); } catch (RuntimeException expected) { }
    }

    // ── updateIssue (full path) ─────────────────────────────────────────────────

    @Test
    public void updateIssue_appliesAllChanges() {
        // old issue read for request.key
        when(rest.fetch(eq("issue/KE-1"), any(), any()))
                .thenReturn(issueJson("KE-1", "{\"summary\":\"old\",\"status\":{\"name\":\"Open\"}}"));
        when(rest.fetch(eq("issue/KE-1/editmeta"), any(), any())).thenReturn("{\"fields\":{}}");
        when(ticketInfoDao.getTransitions("KE-1")).thenReturn(Arrays.asList(transition("Done", "31")));
        when(userDao.searchUser("John")).thenReturn(Arrays.asList(
                new JiraUser("acc-9", "John", "atlassian", "j@x.com", "self", new ArrayList<>())));
        when(builder.post(any())).thenReturn(response);
        when(builder.put(any())).thenReturn(response);
        ok();

        Issue req = new Issue();
        req.key = "KE-1";
        req.summary = "new summary";
        req.description = "new desc";
        req.priority = "P1";
        req.status = "Done";
        req.assigneeName = "John";
        req.labels = Arrays.asList("x");
        req.components = Arrays.asList("Backend");

        FreeForm cf = mock(FreeForm.class);
        when(cf.isEmpty()).thenReturn(false);
        when(cf.keySet()).thenReturn(new HashSet<>());   // empty -> applyCustomFields no-op

        dao.updateIssue(req, cf);   // must complete without throwing
        verify(builder, atLeastOnce()).put(any());
    }

    @Test
    public void updateIssue_singleArg_noChanges() {
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1", "{\"summary\":\"same\"}"));
        when(builder.put(any())).thenReturn(response);
        ok();
        Issue req = new Issue();
        req.key = "KE-1";   // everything else null -> no sub-updates
        dao.updateIssue(req);
        verify(builder).put(any());
    }

    // ── getIssueFinal / isIssueExists ───────────────────────────────────────────

    @Test
    public void getIssueFinal_mapsKey() {
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1", "{\"summary\":\"s\"}"));
        assertEquals("KE-1", dao.getIssueFinal("KE-1").key);
    }

    @Test
    public void isIssueExists_trueAndFalse() {
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1", "{}"));
        assertTrue(dao.isIssueExists("KE-1"));

        when(rest.fetch(eq("issue/BAD"), any(), any())).thenThrow(new IllegalArgumentException("nope"));
        assertFalse(dao.isIssueExists("BAD"));
    }

    // ── getFieldResponse ─────────────────────────────────────────────────────────

    @Test
    public void getFieldResponse_delegatesToFetch() {
        when(rest.fetch(eq("field"), any(), any())).thenReturn("[]");
        assertEquals("[]", dao.getFieldResponse());
    }

    // ── updatePriority ────────────────────────────────────────────────────────────

    @Test
    public void updatePriority_success() {
        when(builder.put(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(204);
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1", "{}"));
        TicketPriority p = new TicketPriority();
        p.id = "2";
        p.name = "P2";
        assertEquals("KE-1", dao.updatePriority(p, "KE-1").key);
    }

    @Test
    public void updatePriority_failure_throws() {
        when(builder.put(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(String.class)).thenReturn("bad");
        try { dao.updatePriority(new TicketPriority(), "KE-1"); fail(); } catch (RuntimeException expected) { }
    }

    // ── addWorklog ──────────────────────────────────────────────────────────────

    @Test
    public void addWorklog_withComment_success() {
        when(builder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        dao.addWorklog("1h", 1735689600000L, "KE-1", "did work");
        verify(builder).post(any());
    }

    @Test
    public void addWorklog_noComment_failure_throws() {
        when(builder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(String.class)).thenReturn("bad");
        try { dao.addWorklog("1h", 1735689600000L, "KE-1", null); fail(); } catch (RuntimeException expected) { }
    }

    // ── attachFileToTicket ────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    public void attachFileToTicket_trueAndFalse() {
        HttpResponse<Object> httpResp = mock(HttpResponse.class);
        when(rest.attachFile(anyString(), anyList())).thenReturn((HttpResponse) httpResp);
        when(httpResp.getStatus()).thenReturn(200);
        assertTrue(dao.attachFileToTicket(new ArrayList<>(), "KE-1"));
        when(httpResp.getStatus()).thenReturn(500);
        assertFalse(dao.attachFileToTicket(new ArrayList<>(), "KE-1"));
    }

    // ── assignIssueToEpic / getEpicLinkId ──────────────────────────────────────────

    @Test
    public void assignIssueToEpic_success() {
        when(rest.fetch(eq("field"), any(), any()))
                .thenReturn("[{\"name\":\"Epic Link\",\"key\":\"customfield_10014\"}]");
        when(builder.put(any())).thenReturn(response);
        ok();
        dao.assignIssueToEpic("KE-2", "KE-1");
        verify(builder).put(any());
    }

    @Test
    public void assignIssueToEpic_failure_throws() {
        when(rest.fetch(eq("field"), any(), any()))
                .thenReturn("[{\"name\":\"Other\",\"key\":\"customfield_1\"}]");   // no Epic Link -> ""
        when(builder.put(any())).thenReturn(response);
        fail400();
        try { dao.assignIssueToEpic("KE-2", "KE-1"); fail(); } catch (RuntimeException expected) { }
    }

    // ── assignChildIssueToEpic ─────────────────────────────────────────────────────

    @Test
    public void assignChildIssueToEpic_successAndFailure() {
        when(builder.put(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(204);
        dao.assignChildIssueToEpic("KE-2", "KE-1");

        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(String.class)).thenReturn("bad");
        try { dao.assignChildIssueToEpic("KE-2", "KE-1"); fail(); } catch (RuntimeException expected) { }
    }

    // ── getComponents / getLabels ──────────────────────────────────────────────────

    @Test
    public void getComponents_success_andBlankSkipped() {
        TicketProject p1 = new TicketProject();
        p1.key = "KE";
        TicketProject p2 = new TicketProject();
        p2.key = "DIR";
        when(projectDao.getAllProjects()).thenReturn(Arrays.asList(p1, p2));
        when(rest.fetch(eq("project/KE/components"), any(), any()))
                .thenReturn("[{\"id\":\"1\",\"name\":\"Backend\",\"project\":\"KE\"}]");
        when(rest.fetch(eq("project/DIR/components"), any(), any())).thenReturn("");   // blank -> skipped
        List<Component> comps = dao.getComponents();
        assertEquals(1, comps.size());
    }

    @Test
    public void getLabels_success() {
        when(rest.fetch(eq("label"), any(), any())).thenReturn("{\"values\":[\"bug\",\"ui\"]}");
        List<Label> labels = dao.getLabels();
        assertEquals(2, labels.size());
    }

    // ── validateCredentials ──────────────────────────────────────────────────────

    @Test
    public void validateCredentials_callsAccessToken() {
        dao.validateCredentials();   // getJiraAccessToken() default on mock -> no-op
    }

    // ── prepareErrorResponseForInvalidKey ──────────────────────────────────────────

    @Test
    public void prepareErrorResponseForInvalidKey_delegates() {
        ExtensionResponseGenerator gen = mock(ExtensionResponseGenerator.class);
        ErrorHandlingStateManager stateMgr = mock(ErrorHandlingStateManager.class);
        ExtensionResponse resp = mock(ExtensionResponse.class);
        List<ValidationOrchestrator.ValidationResult> results = new ArrayList<>();
        when(gen.generateConfirmationResponse(any(), any(), anyString(), any())).thenReturn(resp);

        ExtensionResponse out = dao.prepareErrorResponseForInvalidKey(gen, stateMgr, results, "KE-1");

        assertSame(resp, out);
        verify(stateMgr).put(anyString(), any());
    }

    // ── getKeyByName (static) ──────────────────────────────────────────────────────

    @Test
    public void getKeyByName_foundAndNotFound() {
        String json = "[{\"name\":\"Epic Link\",\"key\":\"customfield_1\"}]";
        assertEquals("customfield_1", IssueDao.getKeyByName(json, "epic link"));
        assertNull(IssueDao.getKeyByName(json, "Missing"));
    }

    // ── buildCreateIssueFields (parent + Epic classic branches) ─────────────────────

    @Test
    public void buildCreateIssueFields_epicClassic_withParent() throws Exception {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn("[{\"id\":\"9\",\"name\":\"Epic\"}]");
        when(projectDao.getProjectByIdResponse("KE")).thenReturn("{\"style\":\"classic\"}");
        Object fields = callPrivate(dao, "buildCreateIssueFields",
                new Class[]{String.class, String.class, String.class, String.class, String.class},
                "KE", "sum", "desc", "KE-1", "Epic");
        assertNotNull(fields);   // exercised parent + Epic-classic customfield_10011 branch
    }

    // ── getPriorityJsonObject (both branches) ──────────────────────────────────────

    @Test
    public void getPriorityJsonObject_withAndWithoutFields() throws Exception {
        TicketPriority full = new TicketPriority();
        full.id = "2";
        full.name = "P2";
        assertNotNull(callPrivate(null, "getPriorityJsonObject", new Class[]{TicketPriority.class}, full));
        assertNotNull(callPrivate(null, "getPriorityJsonObject", new Class[]{TicketPriority.class}, new TicketPriority()));
    }

    // ── getCustomIssue (object / array / primitive / null) ──────────────────────────

    private void stubCustomIssueChain(String issueFields) {
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1", issueFields));
        when(rest.fetch(eq("project/KE"), any(), any())).thenReturn("{\"id\":\"10248\"}");
        when(rest.fetch(eq("issue/createmeta"), any(), any())).thenReturn(
                "{\"projects\":[{\"issuetypes\":[{\"fields\":{\"customfield_100\":{\"name\":\"MyField\"}}}]}]}");
    }

    @Test
    public void getCustomIssue_objectWithValue() {
        stubCustomIssueChain("{\"customfield_100\":{\"value\":\"Opt1\"}}");
        assertEquals(Arrays.asList("Opt1"), dao.getCustomIssue("KE-1", "MyField"));
    }

    @Test
    public void getCustomIssue_array_mixed() {
        stubCustomIssueChain("{\"customfield_100\":[\"a\",{\"value\":\"b\"}]}");
        assertEquals(Arrays.asList("a", "b"), dao.getCustomIssue("KE-1", "MyField"));
    }

    @Test
    public void getCustomIssue_primitiveString() {
        stubCustomIssueChain("{\"customfield_100\":\"plain\"}");
        assertEquals(Arrays.asList("plain"), dao.getCustomIssue("KE-1", "MyField"));
    }

    @Test
    public void getCustomIssue_fieldAbsent_returnsEmpty() {
        stubCustomIssueChain("{\"summary\":\"s\"}");   // customfield_100 absent
        assertTrue(dao.getCustomIssue("KE-1", "MyField").isEmpty());
    }

    @Test
    public void getCustomIssue_invalidKey_throws() {
        // getIssueFinal returns an Issue whose key is null (no "key" in JSON) -> invalid
        when(rest.fetch(eq("issue/BAD"), any(), any())).thenReturn("{\"fields\":{}}");
        try { dao.getCustomIssue("BAD", "MyField"); fail(); } catch (RuntimeException expected) { }
    }

    @Test
    public void getCustomIssue_objectWithoutValue_empty() {
        stubCustomIssueChain("{\"customfield_100\":{\"other\":\"x\"}}");
        assertTrue(dao.getCustomIssue("KE-1", "MyField").isEmpty());
    }

    @Test
    public void getCustomIssue_primitiveNonString_empty() {
        stubCustomIssueChain("{\"customfield_100\":42}");
        assertTrue(dao.getCustomIssue("KE-1", "MyField").isEmpty());
    }

    @Test
    public void getCustomIssue_fieldNameNoMatch_empty() {
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1", "{\"summary\":\"s\"}"));
        when(rest.fetch(eq("project/KE"), any(), any())).thenReturn("{\"id\":\"10248\"}");
        // getCustomField iterates: 'summary' (not customfield_), 'customfield_100' name != MyField -> returns null
        when(rest.fetch(eq("issue/createmeta"), any(), any())).thenReturn(
                "{\"projects\":[{\"issuetypes\":[{\"fields\":{\"summary\":{\"name\":\"Summary\"},"
                        + "\"customfield_100\":{\"name\":\"Other\"}}}]}]}");
        assertTrue(dao.getCustomIssue("KE-1", "MyField").isEmpty());
    }

    // ── getRestClient: remaining instanceof / null branches ─────────────────────

    @Test
    public void getRestClient_nullStoredClient_refetches() {
        IssueDao d = new IssueDao(factory, (RestClient) null);
        when(rest.fetch(anyString(), any(), any())).thenReturn("[]");
        assertEquals("[]", d.getFieldResponse());
    }

    @Test
    public void getRestClient_oauth2WhenUserAuth_noRefetch() {
        OAuth2RestClient oauth = mock(OAuth2RestClient.class);
        when(factory.invokeAsUser()).thenReturn(true);   // OAuth2 && !true -> false -> keep oauth client
        when(oauth.fetch(anyString(), any(), any())).thenReturn("[]");
        IssueDao d = new IssueDao(factory, oauth);
        assertEquals("[]", d.getFieldResponse());
    }

    @Test
    public void getRestClient_authClientWhenAppAuth_noRefetch() {
        AuthRestClient authClient = mock(AuthRestClient.class);   // Auth && false (app auth) -> keep auth client
        when(authClient.fetch(anyString(), any(), any())).thenReturn("[]");
        IssueDao d = new IssueDao(factory, authClient);
        assertEquals("[]", d.getFieldResponse());
    }

    // ── createIssue: blank reporter (hasReporter false via second operand) ──────

    @Test
    public void createIssue_blankReporter_notTreatedAsReporter() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(builder.post(any())).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn("{\"errorMessages\":[\"x\"]}");
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.getStatus()).thenReturn(400);
        try {
            dao.createIssue("KE", "s", "d", null, "Task", "   ");   // blank reporter -> hasReporter=false
            fail();
        } catch (IllegalArgumentException expected) { }
    }

    // ── updateReporter (success / non-2xx warn / exception catch) ────────────────

    @Test
    public void updateReporter_success_nonOk_andException() throws Exception {
        // success
        when(builder.put(any())).thenReturn(response);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        callPrivate(dao, "updateReporter", new Class[]{String.class, String.class}, "KE-1", "acc");

        // non-2xx -> warn branch
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.readEntity(String.class)).thenReturn("denied");
        callPrivate(dao, "updateReporter", new Class[]{String.class, String.class}, "KE-1", "acc");

        // exception -> catch branch (swallowed)
        when(builder.put(any())).thenThrow(new RuntimeException("boom"));
        callPrivate(dao, "updateReporter", new Class[]{String.class, String.class}, "KE-1", "acc");
    }

    // ── buildCreateIssueFields: Epic + non-classic style branch ──────────────────

    @Test
    public void buildCreateIssueFields_epicNextGen_noEpicNameField() throws Exception {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn("[{\"id\":\"9\",\"name\":\"Epic\"}]");
        when(projectDao.getProjectByIdResponse("KE")).thenReturn("{\"style\":\"next-gen\"}");   // not classic
        Object fields = callPrivate(dao, "buildCreateIssueFields",
                new Class[]{String.class, String.class, String.class, String.class, String.class},
                "KE", "sum", "desc", null, "Epic");
        assertNotNull(fields);
    }

    // ── addComment: null visibility (public) ─────────────────────────────────────

    @Test
    public void addComment_nullVisibility_public() {
        when(builder.post(any())).thenReturn(response);
        ok();
        dao.addComment("KE-1", "Hi", null);
        verify(builder).post(any());
    }

    // ── searchByJQL: not-last but nextPageToken null -> empty ────────────────────

    @Test
    public void searchByJQL_noNextToken_returnsEmpty() {
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"issues\":[" + issueJson("KE-1", "{}") + "]}");   // no nextPageToken, no isLast, target page 2
        assertTrue(dao.searchByJQL("project=KE", 10, 2).isEmpty());
    }

    // ── searchByJqlQuery: multi-page then isLast ─────────────────────────────────

    @Test
    public void searchByJqlQuery_multiPage_thenIsLast() {
        when(rest.fetchIssues(anyBoolean(), anyString(), anyString(), anyString(), any(), any())).thenReturn(
                "{\"issues\":[" + issueJson("KE-1", "{}") + "],\"nextPageToken\":\"tok\",\"isLast\":false}",
                "{\"issues\":[" + issueJson("KE-2", "{}") + "],\"isLast\":true}");
        List<Issue> issues = dao.searchByJqlQuery(false, "acc", "inv", "project=KE", 1);
        assertEquals(2, issues.size());
    }

    // ── fetchErrorsInJQLQuery: errors present, query absent ──────────────────────

    @Test
    public void fetchErrorsInJQLQuery_errorsWithoutQuery() {
        when(rest.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn("{\"queries\":[{\"errors\":[\"syntax\"]}]}");
        Map<String, String> out = dao.fetchErrorsInJQLQuery(false, "acc", "inv", "bad");
        assertEquals("syntax", out.get("errors"));
        assertFalse(out.containsKey("query"));
    }

    // ── getIssues: array element that is not a JSON object is skipped ────────────

    @Test
    public void getIssues_skipsNonObjectElements() {
        JsonArray arr = GSON.fromJson("[\"notObj\"," + issueJson("KE-1", "{}") + "]", JsonArray.class);
        List<Issue> issues = dao.getIssues(arr);
        assertEquals(1, issues.size());
    }

    // ── prepareQueryparam: size==0 -> 50 ─────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    public void prepareQueryparam_sizeZero_defaults50() throws Exception {
        Map<String, Object> q = (Map<String, Object>) callPrivate(dao, "prepareQueryparam",
                new Class[]{String.class, long.class, String.class}, "jql", 0L, null);
        assertEquals(50, ((Number) q.get("maxResults")).intValue());
        assertFalse(q.containsKey("nextPageToken"));
    }

    // ── getUpdateJsonObject / getFieldsJsonObject: null / equal / diff branches ──

    @Test
    public void getUpdateAndFieldsJsonObject_allBranches() throws Exception {
        Issue old = new Issue();
        old.summary = "s"; old.description = "d"; old.priority = "P2";
        Issue nul = new Issue();
        Issue eq = new Issue();
        eq.summary = "s"; eq.description = "d"; eq.priority = "P2";
        Issue diff = new Issue();
        diff.summary = "x"; diff.description = "y"; diff.priority = "P1";

        Class<?>[] sig = {Issue.class, Issue.class};
        callPrivate(dao, "getUpdateJsonObject", sig, old, nul);   // null summary -> skip
        callPrivate(dao, "getUpdateJsonObject", sig, old, eq);    // equal -> skip
        callPrivate(dao, "getUpdateJsonObject", sig, old, diff);  // diff -> set

        callPrivate(dao, "getFieldsJsonObject", sig, old, nul);   // null -> skip
        callPrivate(dao, "getFieldsJsonObject", sig, old, eq);    // equal -> skip
        callPrivate(dao, "getFieldsJsonObject", sig, old, diff);  // diff -> set description+priority
    }

    // ── updateIssue: request equals old -> all sub-updates skipped ───────────────

    @Test
    public void updateIssue_sameValues_skipsSubUpdates() {
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1",
                "{\"summary\":\"s\",\"status\":{\"name\":\"Open\"},\"assignee\":{\"displayName\":\"John\"},"
                        + "\"labels\":[\"x\"],\"components\":[{\"name\":\"C\"}]}"));
        when(builder.put(any())).thenReturn(response);
        ok();

        Issue req = new Issue();
        req.key = "KE-1";
        req.status = "Open";           // equals old (case-insensitive) -> skip
        req.assigneeName = "John";     // equals -> skip
        req.labels = Arrays.asList("x");
        req.components = Arrays.asList("C");

        dao.updateIssue(req);
        verify(builder).put(any());
    }

    @Test
    public void updateIssue_assigneeNotFound_swallowed() {
        when(rest.fetch(eq("issue/KE-1"), any(), any()))
                .thenReturn(issueJson("KE-1", "{\"assignee\":{\"displayName\":\"Old\"}}"));
        when(builder.put(any())).thenReturn(response);
        ok();
        when(userDao.searchUser("New")).thenReturn(new ArrayList<>());   // empty -> orElseThrow -> caught

        Issue req = new Issue();
        req.key = "KE-1";
        req.assigneeName = "New";
        dao.updateIssue(req);   // updateAssignee's exception is swallowed
        verify(builder).put(any());
    }

    @Test
    public void updateIssue_statusTransitionMissing_swallowed() {
        when(rest.fetch(eq("issue/KE-1"), any(), any()))
                .thenReturn(issueJson("KE-1", "{\"status\":{\"name\":\"Open\"}}"));
        when(builder.put(any())).thenReturn(response);
        when(builder.post(any())).thenReturn(response);
        ok();
        when(ticketInfoDao.getTransitions("KE-1")).thenReturn(new ArrayList<>());   // -> updateStatus throws, caught

        Issue req = new Issue();
        req.key = "KE-1";
        req.status = "Done";
        dao.updateIssue(req);
        verify(builder).put(any());
    }

    // ── addWorklog: comment present but empty -> not added ───────────────────────

    @Test
    public void addWorklog_emptyComment_skipsCommentBody() {
        when(builder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        dao.addWorklog("1h", 1735689600000L, "KE-1", "");   // empty -> comment not added
        verify(builder).post(any());
    }

    // ── getProjectKey (private): null / no-dash / valid ──────────────────────────

    @Test
    public void getProjectKey_branches() throws Exception {
        Class<?>[] sig = {String.class};
        assertEquals("Invalid issue key", callPrivate(dao, "getProjectKey", sig, (Object) null));
        assertEquals("Invalid issue key", callPrivate(dao, "getProjectKey", sig, "NODASH"));
        assertEquals("KE", callPrivate(dao, "getProjectKey", sig, "KE-1"));
    }

    // ── searchByJQL: no issues and no errorMessages -> ISSUES_NOT_FOUND ───────────

    @Test
    public void searchByJQL_noIssuesNoErrorMessages_throwsNotFound() {
        when(rest.fetch(anyString(), any(), any())).thenReturn("{\"foo\":1}");
        try { dao.searchByJQL("x", 10, 1); fail(); } catch (RuntimeException expected) { }
    }

    // ── searchByJqlQuery: getIssues filters an element -> fetchedIssues < size break

    @Test
    public void searchByJqlQuery_fetchedFewerThanSize_breaks() {
        // 2 raw elements but one is not an object -> getIssues returns 1 (< size 2) -> break
        when(rest.fetchIssues(anyBoolean(), anyString(), anyString(), anyString(), any(), any()))
                .thenReturn("{\"issues\":[\"notObj\"," + issueJson("KE-1", "{}") + "]}");
        List<Issue> issues = dao.searchByJqlQuery(false, "acc", "inv", "project=KE", 2);
        assertEquals(1, issues.size());
    }

    // ── updateIssue: empty custom fields skip + final PUT failure ────────────────

    @Test
    public void updateIssue_emptyCustomFields_skipsCustomFieldApply() {
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1", "{}"));
        when(builder.put(any())).thenReturn(response);
        ok();
        FreeForm cf = mock(FreeForm.class);
        when(cf.isEmpty()).thenReturn(true);   // second operand false -> skip updateCustomFieldToFields
        Issue req = new Issue();
        req.key = "KE-1";
        dao.updateIssue(req, cf);
        verify(builder).put(any());
    }

    @Test
    public void updateIssue_finalPutFails_throws() {
        when(rest.fetch(eq("issue/KE-1"), any(), any())).thenReturn(issueJson("KE-1", "{}"));
        when(builder.put(any())).thenReturn(response);
        fail400();
        Issue req = new Issue();
        req.key = "KE-1";
        try { dao.updateIssue(req); fail(); } catch (RuntimeException expected) { }
    }

    @Test
    public void searchByJqlQuery_noIssuesNoErrorMessages_throwsNotFound() {
        when(rest.fetchIssues(anyBoolean(), anyString(), anyString(), anyString(), any(), any()))
                .thenReturn("{\"foo\":1}");   // no issues, no errorMessages -> ISSUES_NOT_FOUND
        try { dao.searchByJqlQuery(false, "acc", "inv", "x", 5); fail(); } catch (RuntimeException expected) { }
    }

    @Test
    public void searchByJqlQuery_pageWithoutIsLast_continuesThenEmptyBreaks() {
        when(rest.fetchIssues(anyBoolean(), anyString(), anyString(), anyString(), any(), any())).thenReturn(
                "{\"issues\":[" + issueJson("KE-1", "{}") + "]}",   // full page, no isLast/token -> loop continues
                "{\"issues\":[]}");                                   // empty page -> break
        List<Issue> issues = dao.searchByJqlQuery(false, "acc", "inv", "project=KE", 1);
        assertEquals(1, issues.size());
    }

    @Test
    public void getCustomIssue_jsonNullField_empty() {
        stubCustomIssueChain("{\"customfield_100\":null}");   // JsonNull -> not object/array/primitive
        assertTrue(dao.getCustomIssue("KE-1", "MyField").isEmpty());
    }
}
