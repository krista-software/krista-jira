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

package app.krista.extensions.devsecops.development.jira.catalog.validators;

import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationOrchestratorTest {

    private IssueDao issueDao;
    private ProjectDao projectDao;
    private UserDao userDao;
    private TicketInfoDao ticketInfoDao;
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private ValidationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        projectDao = mock(ProjectDao.class);
        userDao = mock(UserDao.class);
        ticketInfoDao = mock(TicketInfoDao.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class, Answers.RETURNS_DEEP_STUBS);
        when(authorizationContext.getAuthorizedAccount().getAccountId()).thenReturn("acc-123");
        orchestrator = new ValidationOrchestrator(issueDao, projectDao, userDao, ticketInfoDao, requestContext, authorizationContext);
    }

    @Test
    void validate_validEmail_noResults() {
        Map<Validator.ValidationResource, String> resources = Map.of(
                Validator.ValidationResource.EMAIL, "user@example.com"
        );
        List<ValidationOrchestrator.ValidationResult> results = orchestrator.validate(resources);
        assertTrue(results.isEmpty());
    }

    @Test
    void validate_invalidEmail_oneResult() {
        Map<Validator.ValidationResource, String> resources = Map.of(
                Validator.ValidationResource.EMAIL, "not-an-email"
        );
        List<ValidationOrchestrator.ValidationResult> results = orchestrator.validate(resources);
        assertEquals(1, results.size());
        ValidationOrchestrator.ValidationResult result = results.get(0);
        assertNotNull(result.getConfirmStepMessage());
        assertNotNull(result.getFetchFieldName());
        assertNotNull(result.getFetchStepMessage());
        assertNotNull(result.getErrMessage());
        assertEquals("Text", result.getFieldType());
        assertNotNull(result.getFieldAttributes());
    }

    @Test
    void validate_validSummary_noResults() {
        Map<Validator.ValidationResource, String> resources = Map.of(
                Validator.ValidationResource.SUMMARY, "Short summary"
        );
        List<ValidationOrchestrator.ValidationResult> results = orchestrator.validate(resources);
        assertTrue(results.isEmpty());
    }

    @Test
    void validate_invalidSummary_oneResult() {
        String longSummary = "x".repeat(256);
        Map<Validator.ValidationResource, String> resources = Map.of(
                Validator.ValidationResource.SUMMARY, longSummary
        );
        List<ValidationOrchestrator.ValidationResult> results = orchestrator.validate(resources);
        assertEquals(1, results.size());
    }

    @Test
    void validate_withClassAndMethod_sourceMethodNotFound_usesDefaults() {
        Map<Validator.ValidationResource, String> resources = Map.of(
                Validator.ValidationResource.EMAIL, "not-an-email"
        );
        List<ValidationOrchestrator.ValidationResult> results =
                orchestrator.validate(resources, String.class, "nonExistentMethodXyz");
        assertEquals(1, results.size());
        assertEquals("Text", results.get(0).getFieldType());
    }

    @Test
    void validate_withClassAndMethod_sourceMethodFound() {
        Map<Validator.ValidationResource, String> resources = Map.of(
                Validator.ValidationResource.EMAIL, "not-an-email"
        );
        List<ValidationOrchestrator.ValidationResult> results =
                orchestrator.validate(resources, String.class, "valueOf");
        assertEquals(1, results.size());
        assertNotNull(results.get(0).getFieldType());
    }

    @Test
    void validate_withClassAndMethod_noFailures_emptyResults() {
        Map<Validator.ValidationResource, String> resources = Map.of(
                Validator.ValidationResource.EMAIL, "user@example.com"
        );
        List<ValidationOrchestrator.ValidationResult> results =
                orchestrator.validate(resources, String.class, "valueOf");
        assertTrue(results.isEmpty());
    }

    @Test
    void validate_withNullClass_fallsBackToBasicValidation() {
        Map<Validator.ValidationResource, String> resources = Map.of(
                Validator.ValidationResource.EMAIL, "not-an-email"
        );
        // null class causes NPE in getDeclaredMethods() → caught → fallback to basic validate()
        List<ValidationOrchestrator.ValidationResult> results =
                orchestrator.validate(resources, null, "anyMethod");
        assertEquals(1, results.size());
    }

    @Test
    void validationResult_4argConstructor_setsDefaults() {
        ValidationOrchestrator.ValidationResult result =
                new ValidationOrchestrator.ValidationResult("confirm", "field", "fetch", "error");
        assertEquals("confirm", result.getConfirmStepMessage());
        assertEquals("field", result.getFetchFieldName());
        assertEquals("fetch", result.getFetchStepMessage());
        assertEquals("error", result.getErrMessage());
        assertEquals("Text", result.getFieldType());
        assertNotNull(result.getFieldAttributes());
    }

    @Test
    void validationResult_6argConstructor_nullFieldType_defaultsToText() {
        ValidationOrchestrator.ValidationResult result =
                new ValidationOrchestrator.ValidationResult("c", "f", "fs", "e", null, null);
        assertEquals("Text", result.getFieldType());
        assertNotNull(result.getFieldAttributes());
    }

    @Test
    void validationResult_6argConstructor_withExplicitValues() {
        Map<String, Object> attrs = Map.of("key", "val");
        ValidationOrchestrator.ValidationResult result =
                new ValidationOrchestrator.ValidationResult("c", "f", "fs", "e", "Select", attrs);
        assertEquals("Select", result.getFieldType());
        assertEquals(attrs, result.getFieldAttributes());
    }
}
