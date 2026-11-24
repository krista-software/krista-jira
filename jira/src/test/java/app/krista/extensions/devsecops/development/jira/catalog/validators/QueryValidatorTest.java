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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QueryValidatorTest {

    private IssueDao issueDao;
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private QueryValidator validator;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class, Answers.RETURNS_DEEP_STUBS);
        when(authorizationContext.getAuthorizedAccount().getAccountId()).thenReturn("acc-123");
        when(requestContext.getInvokerId()).thenReturn("invoker-1");
        validator = new QueryValidator(issueDao, requestContext, authorizationContext);
    }

    @Test
    void validate_validJQL_noInvokeAsUser_returnsTrue() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(issueDao.fetchErrorsInJQLQuery(false, "acc-123", "invoker-1", "project = TEST"))
                .thenReturn(Map.of());

        Boolean result = validator.validate("project = TEST", Map.of());
        assertTrue(result);
        verify(issueDao, never()).validateCredentials();
    }

    @Test
    void validate_validJQL_withInvokeAsUser_callsValidateCredentials() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(issueDao.fetchErrorsInJQLQuery(anyBoolean(), anyString(), anyString(), anyString()))
                .thenReturn(Map.of());

        Boolean result = validator.validate("project = TEST", Map.of());
        assertTrue(result);
        verify(issueDao).validateCredentials();
    }

    @Test
    void validate_jqlWithErrors_returnsFalse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(issueDao.fetchErrorsInJQLQuery(anyBoolean(), anyString(), anyString(), anyString()))
                .thenReturn(Map.of(JiraConstants.ERRORS, "Field 'xyz' does not exist"));

        Boolean result = validator.validate("xyz = bad", Map.of());
        assertFalse(result);
    }

    @Test
    void validate_mustAuthorizeException_rethrown() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(issueDao.fetchErrorsInJQLQuery(anyBoolean(), anyString(), anyString(), anyString()))
                .thenThrow(new MustAuthorizeException("auth required"));

        assertThrows(MustAuthorizeException.class,
                () -> validator.validate("project = TEST", Map.of()));
    }

    @Test
    void validate_genericException_returnsFalse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(issueDao.fetchErrorsInJQLQuery(anyBoolean(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("network error"));

        Boolean result = validator.validate("project = TEST", Map.of());
        assertFalse(result);
    }

    @Test
    void getFetchFieldName_returnsJqlQuery() {
        assertEquals(JiraConstants.JQL_QUERY, validator.getFetchFieldName());
    }

    @Test
    void getFetchStepMessage_returnsNonBlank() {
        String msg = validator.getFetchStepMessage();
        assertNotNull(msg);
        assertFalse(msg.isBlank());
    }

    @Test
    void getConfirmationStepMessage_withNoError_usesUnknownFallback() {
        String msg = validator.getConfirmationStepMessage("project = BROKEN", Map.of());
        assertNotNull(msg);
        assertTrue(msg.contains("project = BROKEN"));
        assertTrue(msg.contains("Unknown syntax error"));
    }

    @Test
    void getConfirmationStepMessage_afterValidateWithErrors_includesError() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(issueDao.fetchErrorsInJQLQuery(anyBoolean(), anyString(), anyString(), anyString()))
                .thenReturn(Map.of(JiraConstants.ERRORS, "Field 'bad' does not exist"));

        validator.validate("bad = query", Map.of());

        String msg = validator.getConfirmationStepMessage("bad = query", Map.of());
        assertTrue(msg.contains("Field 'bad' does not exist"));
    }

    @Test
    void getErrMessage_includesError() {
        String msg = validator.getErrMessage("project = TEST");
        assertNotNull(msg);
        assertTrue(msg.contains("Invalid JQL Query"));
    }
}
