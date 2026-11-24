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

package app.krista.extensions.devsecops.development.jira.services;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * Service class for handling user-related operations with telemetry and error handling.
 * This class encapsulates the business logic for user account information retrieval.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final TelemetryMetrics telemetryMetrics;

    @Inject
    public UserService(UserDao userDao, TelemetryMetrics telemetryMetrics) {
        this.userDao = userDao;
        this.telemetryMetrics = telemetryMetrics;
    }

    /**
     * Sanitizes input strings for safe logging by removing newlines, carriage returns, and tabs.
     * This prevents log injection attacks.
     *
     * @param input The string to sanitize
     * @return Sanitized string safe for logging
     */
    private String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }
        return input.replaceAll("[\n\r\t]", "_");
    }

    /**
     * Retrieves user account information by email or name.
     *
     * @param searchQuery The email address or name to search for
     * @return ExtensionResponse containing the User entity or an empty User if not found
     */
    public ExtensionResponse getUserAccountInformation(String searchQuery) {
        try {
            logger.info("getUserAccountInformation: searchQuery={}", sanitizeForLogging(searchQuery));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_COUNT, 1);
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                logger.info("Search query is null or empty, returning empty user");
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE, 1);
                return ExtensionResponseFactory.create(Map.of("User", new User()));
            }
            User user = userDao.getUserByEmailOrName(searchQuery);

            if (user == null) {
                logger.info("No user found for search query: {}", sanitizeForLogging(searchQuery));
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("User", new User()));
            }

            logger.info("Successfully retrieved user account information for: {}", sanitizeForLogging(user.name));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_SUCCESS, 1);
            return ExtensionResponseFactory.create(Map.of("User", user));

        } catch (IllegalCallerException cause) {
            logger.error("IllegalCallerException occurred while getting user account information: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            logger.error("MustAuthorizeException occurred while getting user account information: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE, 1);
            throw cause;
        } catch (Exception cause) {
            logger.error("Error occurred while getting user account information: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting user account information.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }
}

