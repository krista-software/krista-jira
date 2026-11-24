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

import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.model.base.FreeForm;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 100% coverage for the custom-field / create-issue changes added to {@link IssueDao}
 * on this branch: the 5/6/7-arg {@code createIssue} overloads, {@code extractJiraErrors},
 * {@code applyCustomFields}, {@code updateCustomFieldByType} (every field type), {@code optionRef},
 * {@code valuesOf}, {@code findOptionIdByName}, {@code getCreateFieldsMetaData},
 * {@code getRequiredCustomFieldNames}, {@code getMissingRequiredCustomFields},
 * {@code validateCustomFieldsForCreate}, plus the logger conversions in {@code createUser}
 * and {@code assignChildIssueToEpic}.
 */
public class IssueDaoCustomFieldsTest {

    private static final Gson GSON = new Gson();

    private RestClientFactory factory;
    private RestClient rest;
    private Invocation.Builder builder;
    private Response response;
    private ProjectDao projectDao;
    private IssueDao dao;

    private static final String TASK_TYPES = "[{\"id\":\"10001\",\"name\":\"Task\"}]";

    /**
     * This repo's test JVM has a JAX-RS provider conflict (Jersey 3.0.3 / jakarta vs the
     * {@code javax.ws.rs-api} on the compile path), so {@code MediaType.valueOf} — reached by
     * {@code Entity.entity(x, MediaType.APPLICATION_JSON)} inside the production code — blows up
     * with a {@code RuntimeDelegate} LinkageError (see {@code AuthenticationResourceTest}).
     * Installing a tiny working {@code javax} delegate lets the create/assign code run to
     * completion here; it is cleared afterwards so classes that rely on the conflict are unaffected.
     */
    @BeforeClass
    public static void installRuntimeDelegate() {
        RuntimeDelegate.setInstance(new RuntimeDelegate() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
                return (HeaderDelegate<T>) new HeaderDelegate<MediaType>() {
                    @Override
                    public MediaType fromString(String value) {
                        return MediaType.APPLICATION_JSON_TYPE;
                    }

                    @Override
                    public String toString(MediaType value) {
                        return "application/json";
                    }
                };
            }

            @Override
            public javax.ws.rs.core.UriBuilder createUriBuilder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public javax.ws.rs.core.Response.ResponseBuilder createResponseBuilder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public javax.ws.rs.core.Variant.VariantListBuilder createVariantListBuilder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T createEndpoint(javax.ws.rs.core.Application application, Class<T> endpointType) {
                throw new UnsupportedOperationException();
            }

            @Override
            public javax.ws.rs.core.Link.Builder createLinkBuilder() {
                throw new UnsupportedOperationException();
            }
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

        when(factory.getInstance()).thenReturn(rest);
        when(factory.getInstance(anyBoolean())).thenReturn(rest);
        when(factory.invokeAsUser()).thenReturn(false);
        when(rest.getResourceInvoker(anyString(), any())).thenReturn(builder);

        dao = new IssueDao(projectDao, mock(UserDao.class), mock(TicketInfoDao.class), factory);
    }

    // ── reflection helpers ───────────────────────────────────────────────────

    private static Object callPrivate(Object target, String name, Class<?>[] sig, Object... args) throws Exception {
        Method m = IssueDao.class.getDeclaredMethod(name, sig);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    private static JsonObject obj(String json) {
        return GSON.fromJson(json, JsonObject.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createIssue — reject / unexpected / success / overloads
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void createIssue_jiraRejects_throwsWithFlattenedError() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(builder.post(any())).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn("{\"errorMessages\":[\"Field required\"]}");
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.getStatus()).thenReturn(400);

        try {
            dao.createIssue("KE", "sum", "desc", null, "Task", null, null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Jira rejected the create"));
            assertTrue(e.getMessage().contains("Field required"));
        }
    }

    @Test
    public void createIssue_emptyCustomFields_skipsApply() {
        // non-null but empty map -> the second half of `customFields != null && !isEmpty()` is false,
        // so applyCustomFields is skipped. Uses the reject path to assert we still reach the create call.
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(builder.post(any())).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn("{\"errorMessages\":[\"nope\"]}");
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.getStatus()).thenReturn(400);

        try {
            dao.createIssue("KE", "sum", "desc", null, "Task", null, new HashMap<>());
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("nope"));
        }
        verify(rest, never()).fetch(anyString(), any(), any());   // create-meta never fetched
    }

    @Test
    public void createIssue_success2xxButNoKey_throwsUnexpected() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(builder.post(any())).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn("{}");
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        try {
            dao.createIssue("KE", "sum", "desc", null, "Task", null, null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unexpected create response"));
        }
    }

    @Test
    public void createIssue_withCustomFields_success_appliesFields() {
        IssueDao spy = Mockito.spy(dao);
        Issue issue = new Issue();
        issue.key = "KE-1";
        doReturn(issue).when(spy).getIssueFinal("KE-1");

        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"fields\":[{\"fieldId\":\"customfield_100\",\"name\":\"Category\","
                        + "\"schema\":{\"type\":\"string\"}}],\"isLast\":true}");
        when(builder.post(any())).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn("{\"key\":\"KE-1\"}");
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        Map<String, Object> customFields = new LinkedHashMap<>();
        customFields.put("Category", "R&D Task");

        Issue result = spy.createIssue("KE", "sum", "desc", null, "Task", null, customFields);

        assertNotNull(result);
        assertEquals("KE-1", result.key);
        verify(spy).getIssueFinal("KE-1");
    }

    @Test
    public void createIssue_fiveArgOverload_delegates() {
        IssueDao spy = Mockito.spy(dao);
        Issue issue = new Issue();
        issue.key = "KE-3";
        doReturn(issue).when(spy).getIssueFinal("KE-3");

        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(builder.post(any())).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn("{\"key\":\"KE-3\"}");
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        Issue result = spy.createIssue("KE", "sum", "desc", null, "Task");

        assertEquals("KE-3", result.key);
    }

    @Test
    public void createIssue_sixArgOverload_setsReporterAfterCreate() {
        IssueDao spy = Mockito.spy(dao);
        Issue issue = new Issue();
        issue.key = "KE-2";
        doReturn(issue).when(spy).getIssueFinal("KE-2");

        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(builder.post(any())).thenReturn(response);   // create
        when(builder.put(any())).thenReturn(response);    // reporter PUT
        when(response.readEntity(String.class)).thenReturn("{\"key\":\"KE-2\"}");
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        Issue result = spy.createIssue("KE", "sum", "desc", null, "Task", "acc-99");

        assertEquals("KE-2", result.key);
        verify(builder).put(any());   // reporter was set via PUT
    }

    // ─────────────────────────────────────────────────────────────────────────
    // extractJiraErrors
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void extractJiraErrors_errorMessages() throws Exception {
        String r = (String) callPrivate(null, "extractJiraErrors", new Class[]{String.class},
                "{\"errorMessages\":[\"boom\",\"bang\"]}");
        assertEquals("boom; bang", r);
    }

    @Test
    public void extractJiraErrors_fieldErrors() throws Exception {
        String r = (String) callPrivate(null, "extractJiraErrors", new Class[]{String.class},
                "{\"errors\":{\"summary\":\"is required\"}}");
        assertEquals("summary: is required", r);
    }

    @Test
    public void extractJiraErrors_errorsNotObject_returnsRaw() throws Exception {
        // "errors" present but not a JSON object -> the isJsonObject() guard is false.
        String r = (String) callPrivate(null, "extractJiraErrors", new Class[]{String.class},
                "{\"errors\":[1,2]}");
        assertEquals("{\"errors\":[1,2]}", r);
    }

    @Test
    public void extractJiraErrors_noKnownKeys_returnsRaw() throws Exception {
        String r = (String) callPrivate(null, "extractJiraErrors", new Class[]{String.class}, "{}");
        assertEquals("{}", r);
    }

    @Test
    public void extractJiraErrors_malformed_returnsRaw() throws Exception {
        String r = (String) callPrivate(null, "extractJiraErrors", new Class[]{String.class}, "[1,2]");
        assertEquals("[1,2]", r);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // applyCustomFields — matched, not-matched, metadata without name
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void applyCustomFields_matchesKnown_warnsUnknown() throws Exception {
        JsonObject meta = obj("{\"customfield_1\":{\"name\":\"Category\",\"schema\":{\"type\":\"string\"}},"
                + "\"customfield_2\":{\"schema\":{\"type\":\"string\"}}}");   // second: no "name"
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new LinkedHashMap<>();
        cf.put("Category", "R&D");   // matches
        cf.put("Ghost", "x");        // no field with this name -> warn

        callPrivate(dao, "applyCustomFields", new Class[]{Map.class, JsonObject.class, JsonObject.class},
                cf, fields, meta);

        assertEquals("R&D", fields.get("customfield_1").getAsString());
        assertFalse(fields.has("Ghost"));
    }

    @Test
    public void updateCustomFieldToFields_delegatesToApplyCustomFields() throws Exception {
        // FreeForm update path (issue/{key}/editmeta) — empty FreeForm just exercises the delegation.
        FreeForm ff = mock(FreeForm.class);
        when(ff.keySet()).thenReturn(new HashSet<>());
        when(rest.fetch(anyString(), any(), any())).thenReturn("{\"fields\":{}}");
        JsonObject fields = new JsonObject();

        callPrivate(dao, "updateCustomFieldToFields", new Class[]{FreeForm.class, JsonObject.class, String.class},
                ff, fields, "KE-1");

        assertEquals(0, fields.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateCustomFieldByType — every switch branch
    // ─────────────────────────────────────────────────────────────────────────

    private boolean update(JsonObject meta, JsonObject fields, Map<String, Object> cf, String name) throws Exception {
        return (boolean) callPrivate(dao, "updateCustomFieldByType",
                new Class[]{Map.class, JsonObject.class, JsonObject.class, String.class}, cf, fields, meta, name);
    }

    @Test
    public void updateCustomFieldByType_dateFromEpochNumber() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Due\",\"schema\":{\"type\":\"date\"}}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Due", 1735689600000.0);   // Double epoch millis
        assertTrue(update(meta, fields, cf, "Due"));
        assertTrue(fields.has("cf"));
    }

    @Test
    public void updateCustomFieldByType_dateFromString() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Due\",\"schema\":{\"type\":\"date\"}}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Due", "2026-09-01");
        assertTrue(update(meta, fields, cf, "Due"));
        assertEquals("2026-09-01", fields.get("cf").getAsString());
    }

    @Test
    public void updateCustomFieldByType_datetime() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"When\",\"schema\":{\"type\":\"datetime\"}}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("When", "2026-09-01T00:00:00.000+0000");
        assertTrue(update(meta, fields, cf, "When"));
        assertEquals("2026-09-01T00:00:00.000+0000", fields.get("cf").getAsString());
    }

    @Test
    public void updateCustomFieldByType_string() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Note\",\"schema\":{\"type\":\"string\"}}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Note", "hello");
        assertTrue(update(meta, fields, cf, "Note"));
        assertEquals("hello", fields.get("cf").getAsString());
    }

    @Test
    public void updateCustomFieldByType_optionResolvedById() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Cat\",\"schema\":{\"type\":\"option\"},"
                + "\"allowedValues\":[{\"id\":\"7\",\"value\":\"R&D\"}]}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Cat", "R&D");
        assertTrue(update(meta, fields, cf, "Cat"));
        assertEquals("7", fields.getAsJsonObject("cf").get("id").getAsString());
    }

    @Test
    public void updateCustomFieldByType_optionFallsBackToValue_whenNoAllowedValues() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Cat\",\"schema\":{\"type\":\"option\"}}}");   // no allowedValues
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Cat", "Other");
        assertTrue(update(meta, fields, cf, "Cat"));
        assertEquals("Other", fields.getAsJsonObject("cf").get("value").getAsString());
    }

    @Test
    public void updateCustomFieldByType_optionResolvedByName() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Comp\",\"schema\":{\"type\":\"option\"},"
                + "\"allowedValues\":[{\"id\":\"5\",\"name\":\"Backend\"}]}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Comp", "Backend");
        assertTrue(update(meta, fields, cf, "Comp"));
        assertEquals("5", fields.getAsJsonObject("cf").get("id").getAsString());
    }

    @Test
    public void updateCustomFieldByType_arrayOfStrings_single() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Cust\",\"schema\":{\"type\":\"array\",\"items\":\"string\"}}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Cust", "KristaInternal");
        assertTrue(update(meta, fields, cf, "Cust"));
        JsonArray arr = fields.getAsJsonArray("cf");
        assertEquals(1, arr.size());
        assertEquals("KristaInternal", arr.get(0).getAsString());
    }

    @Test
    public void updateCustomFieldByType_arrayOfStrings_list() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Labels\",\"schema\":{\"type\":\"array\",\"items\":\"string\"}}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Labels", Arrays.asList("a", "b"));
        assertTrue(update(meta, fields, cf, "Labels"));
        assertEquals(2, fields.getAsJsonArray("cf").size());
    }

    @Test
    public void updateCustomFieldByType_arrayOfOptions() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Sel\",\"schema\":{\"type\":\"array\",\"items\":\"option\"},"
                + "\"allowedValues\":[{\"id\":\"9\",\"value\":\"opt\"}]}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Sel", "opt");
        assertTrue(update(meta, fields, cf, "Sel"));
        JsonArray arr = fields.getAsJsonArray("cf");
        assertEquals("9", arr.get(0).getAsJsonObject().get("id").getAsString());
    }

    @Test
    public void updateCustomFieldByType_nullValue_returnsTrueNothingSet() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Note\",\"schema\":{\"type\":\"string\"}}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Note", null);
        assertTrue(update(meta, fields, cf, "Note"));
        assertFalse(fields.has("cf"));
    }

    @Test
    public void updateCustomFieldByType_unsupportedType_returnsTrueNotSet() throws Exception {
        JsonObject meta = obj("{\"cf\":{\"name\":\"Who\",\"schema\":{\"type\":\"user\"}}}");
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Who", "someone");
        assertTrue(update(meta, fields, cf, "Who"));
        assertFalse(fields.has("cf"));
    }

    @Test
    public void updateCustomFieldByType_noMatch_skipsMetaWithoutNameOrSchema_returnsFalse() throws Exception {
        JsonObject meta = obj("{\"a\":{\"schema\":{\"type\":\"string\"}},"     // no name
                + "\"b\":{\"name\":\"HasNoSchema\"}}");                          // no schema
        JsonObject fields = new JsonObject();
        Map<String, Object> cf = new HashMap<>();
        cf.put("Unmatched", "v");
        assertFalse(update(meta, fields, cf, "Unmatched"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // valuesOf / findOptionIdByName
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @Test
    public void valuesOf_null_list_single() throws Exception {
        assertTrue(((List<String>) callPrivate(null, "valuesOf", new Class[]{Object.class}, (Object) null)).isEmpty());

        List<Object> withNull = Arrays.asList("x", null, "y");
        List<String> r = (List<String>) callPrivate(null, "valuesOf", new Class[]{Object.class}, withNull);
        assertEquals(Arrays.asList("x", "y"), r);   // null element skipped

        List<String> single = (List<String>) callPrivate(null, "valuesOf", new Class[]{Object.class}, "solo");
        assertEquals(Arrays.asList("solo"), single);
    }

    @Test
    public void findOptionIdByName_nullAllowedValues_returnsNull() throws Exception {
        Object r = callPrivate(dao, "findOptionIdByName", new Class[]{JsonArray.class, String.class}, null, "x");
        assertNull(r);
    }

    @Test
    public void findOptionIdByName_noMatch_returnsNull() throws Exception {
        JsonArray av = GSON.fromJson("[{\"id\":\"1\",\"value\":\"A\"}]", JsonArray.class);
        Object r = callPrivate(dao, "findOptionIdByName", new Class[]{JsonArray.class, String.class}, av, "Z");
        assertNull(r);
    }

    @Test
    public void findOptionIdByName_optionWithoutValueOrName_isSkipped() throws Exception {
        // option has neither "value" nor "name" -> valueElement is null -> skipped, returns null.
        JsonArray av = GSON.fromJson("[{\"id\":\"1\"}]", JsonArray.class);
        Object r = callPrivate(dao, "findOptionIdByName", new Class[]{JsonArray.class, String.class}, av, "A");
        assertNull(r);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCreateFieldsMetaData
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void getCreateFieldsMetaData_singlePage_fieldsKey() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"fields\":[{\"fieldId\":\"customfield_1\",\"name\":\"A\"}],\"isLast\":true}");

        JsonObject meta = dao.getCreateFieldsMetaData("KE", "Task");
        assertTrue(meta.has("customfield_1"));
        assertEquals(1, meta.size());
    }

    @Test
    public void getCreateFieldsMetaData_paginatedValuesKey_skipsFieldWithoutId() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"values\":[{\"key\":\"customfield_2\",\"name\":\"B\"},{\"name\":\"NoId\"}],\"isLast\":false}",
                "{\"values\":[]}");

        JsonObject meta = dao.getCreateFieldsMetaData("KE", "Task");
        assertTrue(meta.has("customfield_2"));
        assertEquals(1, meta.size());   // NoId (no fieldId/key) skipped
    }

    @Test
    public void getCreateFieldsMetaData_noFieldsOrValuesArray_returnsEmpty() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(rest.fetch(anyString(), any(), any())).thenReturn("{}");   // neither "fields" nor "values"
        assertEquals(0, dao.getCreateFieldsMetaData("KE", "Task").size());
    }

    @Test
    public void getCreateFieldsMetaData_noIsLastFlag_treatedAsLast() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"fields\":[{\"fieldId\":\"customfield_1\",\"name\":\"A\"}]}");   // no "isLast" -> last
        assertEquals(1, dao.getCreateFieldsMetaData("KE", "Task").size());
    }

    @Test
    public void getCreateFieldsMetaData_issueTypeInvalid_returnsEmpty() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn("[]");   // no matching type -> throws inside

        JsonObject meta = dao.getCreateFieldsMetaData("KE", "Task");
        assertEquals(0, meta.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getRequiredCustomFieldNames / getMissingRequiredCustomFields
    // ─────────────────────────────────────────────────────────────────────────

    private void stubCreateMetaWithRequiredCustomer() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"fields\":["
                        + "{\"fieldId\":\"customfield_1\",\"name\":\"Customer\",\"required\":true},"
                        + "{\"fieldId\":\"summary\",\"name\":\"Summary\",\"required\":true},"       // handled -> skipped
                        + "{\"fieldId\":\"customfield_2\",\"name\":\"Extra\",\"required\":false},"  // not required
                        + "{\"fieldId\":\"customfield_3\",\"required\":true},"                       // required, no name
                        + "{\"fieldId\":\"customfield_4\",\"name\":\"NoReqFlag\"}"                    // no "required" key
                        + "],\"isLast\":true}");
    }

    @Test
    public void getRequiredCustomFieldNames_onlyNamedNonHandledRequired() {
        stubCreateMetaWithRequiredCustomer();
        List<String> req = dao.getRequiredCustomFieldNames("KE", "Task");
        assertEquals(Arrays.asList("Customer"), req);
    }

    @Test
    public void getMissingRequiredCustomFields_suppliedCaseInsensitive() {
        stubCreateMetaWithRequiredCustomer();
        Map<String, Object> provided = new HashMap<>();
        provided.put("customer", "KristaInternal");   // lower-case still matches
        assertTrue(dao.getMissingRequiredCustomFields("KE", "Task", provided).isEmpty());
    }

    @Test
    public void getMissingRequiredCustomFields_nullProvided_allMissing() {
        stubCreateMetaWithRequiredCustomer();
        assertEquals(Arrays.asList("Customer"), dao.getMissingRequiredCustomFields("KE", "Task", null));
    }

    @Test
    public void getMissingRequiredCustomFields_notSupplied_listed() {
        stubCreateMetaWithRequiredCustomer();
        Map<String, Object> provided = new HashMap<>();
        provided.put("Something", "else");
        assertEquals(Arrays.asList("Customer"), dao.getMissingRequiredCustomFields("KE", "Task", provided));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateCustomFieldsForCreate
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void validateCustomFieldsForCreate_noMetadata_defers() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn("[]");   // -> empty meta
        assertTrue(dao.validateCustomFieldsForCreate("KE", "Task", null).isEmpty());
    }

    private void stubValidateMeta() {
        when(projectDao.getIssueTypeFromProject("KE")).thenReturn(TASK_TYPES);
        when(rest.fetch(anyString(), any(), any())).thenReturn(
                "{\"fields\":["
                        + "{\"fieldId\":\"customfield_1\",\"name\":\"Customer\",\"required\":true},"
                        + "{\"fieldId\":\"customfield_5\",\"name\":\"Optional\",\"required\":false},"  // required=false
                        + "{\"fieldId\":\"customfield_6\",\"name\":\"NoFlag\"},"                        // no "required" key
                        + "{\"fieldId\":\"summary\",\"name\":\"Summary\"},"   // handled -> skipped
                        + "{\"fieldId\":\"customfield_9\"}"                    // no name -> skipped
                        + "],\"isLast\":true}");
    }

    @Test
    public void validateCustomFieldsForCreate_nullProvided_reportsMissingRequired() {
        stubValidateMeta();
        List<String> problems = dao.validateCustomFieldsForCreate("KE", "Task", null);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("Missing required field 'Customer'"));
    }

    @Test
    public void validateCustomFieldsForCreate_supplied_ok() {
        stubValidateMeta();
        Map<String, Object> provided = new HashMap<>();
        provided.put("customer", "KristaInternal");
        assertTrue(dao.validateCustomFieldsForCreate("KE", "Task", provided).isEmpty());
    }

    @Test
    public void validateCustomFieldsForCreate_unknownField_andMissingRequired() {
        stubValidateMeta();
        Map<String, Object> provided = new HashMap<>();
        provided.put("Ghost", "v");
        List<String> problems = dao.validateCustomFieldsForCreate("KE", "Task", provided);
        assertTrue(problems.stream().anyMatch(p -> p.contains("Missing required field 'Customer'")));
        assertTrue(problems.stream().anyMatch(p -> p.contains("Field 'Ghost' does not exist")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // logger conversions: createUser failure, assignChildIssueToEpic
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void createUser_nonCreatedStatus_returnsEmptyMap() {
        when(builder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(400);   // not 201

        Map<String, Object> result = dao.createUser("nobody@example.com");
        assertTrue(result.isEmpty());
    }

    @Test
    public void assignChildIssueToEpic_success() {
        when(builder.put(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(204);   // HTTP_NO_CONTENT

        dao.assignChildIssueToEpic("KE-10", "KE-1");   // no exception

        verify(builder).put(any());
    }

    @Test
    public void assignChildIssueToEpic_failure_throws() {
        when(builder.put(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(String.class)).thenReturn("bad");

        try {
            dao.assignChildIssueToEpic("KE-10", "KE-1");
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("bad"));
        }
    }
}
