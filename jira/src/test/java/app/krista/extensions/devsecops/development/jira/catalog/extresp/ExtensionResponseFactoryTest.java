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

package app.krista.extensions.devsecops.development.jira.catalog.extresp;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.RemediationAction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionResponseFactoryTest {

    @Test
    void create_withValues_returnsSuccessResponse() {
        Map<String, Object> values = Map.of("key", "val");
        ExtensionResponse response = ExtensionResponseFactory.create(values);
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    void create_withExceptionAndMessage_returnsFailureResponse() {
        Exception ex = new RuntimeException("test error");
        ExtensionResponse response = ExtensionResponseFactory.create(
                ex, "Something went wrong", ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    void create_withExceptionActionsAndState_returnsFailureResponse() {
        Exception ex = new RuntimeException("test error");
        List<RemediationAction> actions = List.of(
                RemediationActionFactory.createInformAction("inform msg", List.of()));
        ExtensionResponse response = ExtensionResponseFactory.create(
                ex, "Error message", ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                actions, "subCatalogReq", Map.of("key", "value"));
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    void create_withMessageActionsAndStateNoCause_returnsFailureResponse() {
        List<RemediationAction> actions = List.of(
                RemediationActionFactory.createInformAction("inform msg", List.of()));
        ExtensionResponse response = ExtensionResponseFactory.create(
                "Error message", ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                actions, "subCatalogReq", Map.of("key", "value"));
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }
}
