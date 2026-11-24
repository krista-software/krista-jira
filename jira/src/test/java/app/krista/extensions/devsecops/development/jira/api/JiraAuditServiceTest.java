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

import app.krista.extensions.util.KeyValueStore;
import app.krista.sdk.atlassian.audit.KeyValueAuditStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 100% coverage tests for {@link JiraAuditService}.
 *
 * <p>{@link KeyValueAuditStore} internally swallows its own errors, so the
 * only reliable way to exercise the {@code catch (Exception e)} paths in
 * {@code JiraAuditService} is to replace the private {@code auditStore}
 * field with a mock that throws. A dedicated {@code JiraAuditService}
 * instance with the injected throwing mock is used for those tests;
 * the regular instance backed by the real {@code KeyValueAuditStore} covers
 * the normal (non-throwing) paths.</p>
 */
@DisplayName("JiraAuditService Tests")
class JiraAuditServiceTest {

    @Mock
    private KeyValueStore keyValueStore;

    @Mock
    private KeyValueAuditStore throwingAuditStore;

    /** Normal service — real KeyValueAuditStore wrapping a mock KV store. */
    private JiraAuditService jiraAuditService;

    /** Service with a mock KeyValueAuditStore that throws on every call. */
    private JiraAuditService throwingService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        jiraAuditService = new JiraAuditService(keyValueStore);

        // Build a JiraAuditService whose auditStore is replaced with a throwing mock
        throwingService = new JiraAuditService(keyValueStore);
        injectAuditStore(throwingService, throwingAuditStore);

        // Configure the throwing mock
        doThrow(new RuntimeException("store failure"))
                .when(throwingAuditStore).record(anyString(), anyString(), anyString(),
                        anyString(), any(), any(), any());
        doThrow(new RuntimeException("auth store failure"))
                .when(throwingAuditStore).recordAuthError(anyString(), anyString(), any());
    }

    // ── Helper to inject auditStore via reflection ──────────────────────────

    private static void injectAuditStore(JiraAuditService service, KeyValueAuditStore mock)
            throws Exception {
        Field field = JiraAuditService.class.getDeclaredField("auditStore");
        field.setAccessible(true);
        field.set(service, mock);
    }

    // ── Constructor / annotation checks ────────────────────────────────────

    @Test
    @DisplayName("Should have @Service annotation")
    void testHasServiceAnnotation() {
        assertTrue(JiraAuditService.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("Should have @Inject annotation on constructor")
    void testHasInjectAnnotation() throws NoSuchMethodException {
        var ctor = JiraAuditService.class.getConstructor(KeyValueStore.class);
        assertTrue(ctor.isAnnotationPresent(javax.inject.Inject.class));
    }

    // ── getAuditStore ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAuditStore returns non-null KeyValueAuditStore")
    void testGetAuditStore_ReturnsNonNull() {
        assertNotNull(jiraAuditService.getAuditStore());
    }

    @Test
    @DisplayName("getAuditStore returns same instance on repeated calls")
    void testGetAuditStore_SameInstanceOnRepeatedCalls() {
        KeyValueAuditStore first = jiraAuditService.getAuditStore();
        KeyValueAuditStore second = jiraAuditService.getAuditStore();
        assertSame(first, second);
    }

    // ── triggered — happy path ──────────────────────────────────────────────

    @Test
    @DisplayName("triggered — normal path: completes without throwing")
    void testTriggered_NormalPath_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.triggered("invoker-1", "Attach File to Ticket"));
    }

    @Test
    @DisplayName("triggered — accepts null catalogName without throwing")
    void testTriggered_NullCatalogName_NoException() {
        assertDoesNotThrow(() -> jiraAuditService.triggered("invoker-3", null));
    }

    @Test
    @DisplayName("triggered — accepts null invokerId without throwing")
    void testTriggered_NullInvokerId_NoException() {
        assertDoesNotThrow(() -> jiraAuditService.triggered(null, "Some Catalog"));
    }

    // ── triggered — exception path (catch block coverage) ──────────────────

    @Test
    @DisplayName("triggered — swallows exception thrown by auditStore.record")
    void testTriggered_ExceptionFromStore_Swallowed() {
        assertDoesNotThrow(() ->
                throwingService.triggered("invoker-err", "Attach File to Ticket"));
    }

    @Test
    @DisplayName("triggered — swallows exception when catalogName is null and store throws")
    void testTriggered_NullCatalogName_ExceptionSwallowed() {
        assertDoesNotThrow(() -> throwingService.triggered("invoker-err", null));
    }

    // ── success — happy path ────────────────────────────────────────────────

    @Test
    @DisplayName("success — normal path: completes without throwing")
    void testSuccess_NormalPath_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.success("invoker-1", "Create an issue"));
    }

    @Test
    @DisplayName("success — accepts null catalogName without throwing")
    void testSuccess_NullCatalogName_NoException() {
        assertDoesNotThrow(() -> jiraAuditService.success("invoker-3", null));
    }

    @Test
    @DisplayName("success — accepts null invokerId without throwing")
    void testSuccess_NullInvokerId_NoException() {
        assertDoesNotThrow(() -> jiraAuditService.success(null, "Some Catalog"));
    }

    // ── success — exception path ────────────────────────────────────────────

    @Test
    @DisplayName("success — swallows exception thrown by auditStore.record")
    void testSuccess_ExceptionFromStore_Swallowed() {
        assertDoesNotThrow(() ->
                throwingService.success("invoker-err", "Report a bug"));
    }

    @Test
    @DisplayName("success — swallows exception when catalogName is null and store throws")
    void testSuccess_NullCatalogName_ExceptionSwallowed() {
        assertDoesNotThrow(() -> throwingService.success("invoker-err", null));
    }

    // ── failure — happy path ────────────────────────────────────────────────

    @Test
    @DisplayName("failure — normal path with non-null cause")
    void testFailure_NonNullCause_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.failure("invoker-1", "Update an issue",
                        new RuntimeException("something failed")));
    }

    @Test
    @DisplayName("failure — null cause: no exception")
    void testFailure_NullCause_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.failure("invoker-2", "Update an issue", null));
    }

    @Test
    @DisplayName("failure — null catalogName does not throw")
    void testFailure_NullCatalogName_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.failure("invoker-4", null, new RuntimeException("err")));
    }

    @Test
    @DisplayName("failure — cause with null message does not throw")
    void testFailure_CauseWithNullMessage_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.failure("invoker-5", "Fetch by JQL query",
                        new RuntimeException((String) null)));
    }

    // ── failure — exception path ────────────────────────────────────────────

    @Test
    @DisplayName("failure — swallows exception thrown by auditStore.record (non-null cause)")
    void testFailure_ExceptionFromStore_NonNullCause_Swallowed() {
        assertDoesNotThrow(() ->
                throwingService.failure("invoker-err", "Get issues",
                        new RuntimeException("catalog failure")));
    }

    @Test
    @DisplayName("failure — swallows exception thrown by auditStore.record (null cause)")
    void testFailure_ExceptionFromStore_NullCause_Swallowed() {
        assertDoesNotThrow(() ->
                throwingService.failure("invoker-err", "Get issues", null));
    }

    // ── authFailure — happy path ────────────────────────────────────────────

    @Test
    @DisplayName("authFailure — normal path with non-null cause")
    void testAuthFailure_NonNullCause_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.authFailure("invoker-1", "Attach File to Ticket",
                        new RuntimeException("auth error")));
    }

    @Test
    @DisplayName("authFailure — null cause does not throw")
    void testAuthFailure_NullCause_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.authFailure("invoker-2", "Create an issue", null));
    }

    @Test
    @DisplayName("authFailure — null catalogName does not throw")
    void testAuthFailure_NullCatalogName_NoException() {
        assertDoesNotThrow(() ->
                jiraAuditService.authFailure("invoker-4", null, new RuntimeException("err")));
    }

    // ── authFailure — exception path ────────────────────────────────────────

    @Test
    @DisplayName("authFailure — swallows exception thrown by auditStore.recordAuthError")
    void testAuthFailure_ExceptionFromStore_Swallowed() {
        assertDoesNotThrow(() ->
                throwingService.authFailure("invoker-err", "Report a bug",
                        new RuntimeException("oauth failed")));
    }

    @Test
    @DisplayName("authFailure — swallows exception when catalogName is null and store throws")
    void testAuthFailure_NullCatalogName_ExceptionSwallowed() {
        assertDoesNotThrow(() ->
                throwingService.authFailure("invoker-err", null, new RuntimeException("err")));
    }
}
