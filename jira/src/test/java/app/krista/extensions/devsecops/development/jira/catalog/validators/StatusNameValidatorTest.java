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
import app.krista.extensions.devsecops.development.jira.catalog.entities.TransitionStatus;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatusNameValidatorTest {

    private TicketInfoDao ticketInfoDao;
    private StatusNameValidator validator;

    @BeforeEach
    void setUp() {
        ticketInfoDao = mock(TicketInfoDao.class);
        validator = new StatusNameValidator(ticketInfoDao);
    }

    private TransitionStatus makeTransition(String toName) {
        TransitionStatus ts = new TransitionStatus();
        ts.toName = toName;
        return ts;
    }

    private Map<Validator.ValidationResource, String> context(String ticketId) {
        return Map.of(Validator.ValidationResource.TICKET_ID, ticketId);
    }

    @Test
    void validate_statusInTransitions_returnsTrue() {
        when(ticketInfoDao.getTransitions("ISSUE-1"))
                .thenReturn(List.of(makeTransition("In Progress"), makeTransition("Done")));

        Boolean result = validator.validate("In Progress", context("ISSUE-1"));
        assertTrue(result);
    }

    @Test
    void validate_statusNotInTransitions_returnsFalse() {
        when(ticketInfoDao.getTransitions("ISSUE-2"))
                .thenReturn(List.of(makeTransition("In Progress"), makeTransition("Done")));

        Boolean result = validator.validate("Closed", context("ISSUE-2"));
        assertFalse(result);
    }

    @Test
    void validate_mustAuthorizeException_rethrown() {
        when(ticketInfoDao.getTransitions(anyString()))
                .thenThrow(new MustAuthorizeException("not authorized"));

        assertThrows(MustAuthorizeException.class,
                () -> validator.validate("In Progress", context("ISSUE-3")));
    }

    @Test
    void validate_illegalCallerException_rethrown() {
        when(ticketInfoDao.getTransitions(anyString()))
                .thenThrow(new IllegalCallerException("caller error"));

        assertThrows(IllegalCallerException.class,
                () -> validator.validate("Done", context("ISSUE-4")));
    }

    @Test
    void validate_genericException_returnsFalse() {
        when(ticketInfoDao.getTransitions(anyString()))
                .thenThrow(new RuntimeException("network error"));

        Boolean result = validator.validate("In Progress", context("ISSUE-5"));
        assertFalse(result);
    }

    @Test
    void getFetchFieldName_returnsStatusName() {
        assertNotNull(validator.getFetchFieldName());
    }

    @Test
    void getFetchStepMessage_returnsNonBlank() {
        String msg = validator.getFetchStepMessage();
        assertNotNull(msg);
        assertFalse(msg.isBlank());
    }

    @Test
    void getConfirmationStepMessage_includesAvailableStatuses() {
        when(ticketInfoDao.getTransitions("ISSUE-6"))
                .thenReturn(List.of(makeTransition("In Progress"), makeTransition("Done")));

        String msg = validator.getConfirmationStepMessage("Closed", context("ISSUE-6"));
        assertNotNull(msg);
        assertTrue(msg.contains("Closed"));
        assertTrue(msg.contains("In Progress"));
        assertTrue(msg.contains("Done"));
    }

    @Test
    void getErrMessage_includesStatusName() {
        String msg = validator.getErrMessage("Closed");
        assertNotNull(msg);
        assertTrue(msg.contains("Closed"));
    }
}
