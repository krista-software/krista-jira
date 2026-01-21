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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Service
public final class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_RESULTS_LIMIT = 1000;

    private final RestClientFactory restClientFactory;
    private RestClient restClient;

    @Inject
    public UserDao(RestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
    }

    public UserDao(RestClientFactory restClientFactory, RestClient restClient) {
        this.restClientFactory = restClientFactory;
        this.restClient = restClient;
    }

    private RestClient getRestClient() {
        if (restClientFactory == null) {
            return restClient;
        }

        boolean invokeAsUserAuth = restClientFactory.invokeAsUser();
        if (restClient == null || (restClient instanceof OAuth2RestClient && !invokeAsUserAuth) || (restClient instanceof AuthRestClient && invokeAsUserAuth)) {
            restClient = restClientFactory.getInstance();
        }
        return restClient;
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
     * Searches for Jira users matching the given query string.
     * Returns all matching users in a single request (no pagination).
     *
     * @param query The search query (email, username, or display name)
     * @return List of matching JiraUser objects, empty list if none found
     */
    public List<JiraUser> searchUser(String query) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("query", query);
        String jsonResponse = getRestClient().fetch("user/search", queryParams, String.class);
        List<JiraUser> users = new ArrayList<>();

        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            LOGGER.debug("Empty or null response from Jira user search API for query: {}", sanitizeForLogging(query));
            return users;
        }

        try {
            JsonArray userArray = GSON.fromJson(jsonResponse, JsonArray.class);
            if (userArray != null) {
                for (int i = 0; i < userArray.size(); i++) {
                    try {
                        JsonObject userObj = userArray.get(i).getAsJsonObject();
                        users.add(JiraUser.create(userObj));
                    } catch (Exception e) {
                        LOGGER.error("Error parsing user object at index {}: {}", i, e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing JSON response from user search for query '{}': {}",
                sanitizeForLogging(query), e.getMessage(), e);
        }

        return users;
    }

    /**
     * Searches for Jira users with pagination support using iterative approach.
     * Prevents stack overflow by avoiding recursion and limits total results to prevent memory issues.
     *
     * @param query The search query (email, username, or display name)
     * @param page  The page number (1-indexed)
     * @param size  The number of results per page (0 defaults to 50)
     * @return List of matching JiraUser objects across all pages (max 1000 results)
     * @throws RuntimeException if API call fails or pagination fails
     */
    public List<JiraUser> searchUser(String query, long page, long size) {
        List<JiraUser> totalUsers = new ArrayList<>();
        long currentPage = page;
        int fetchedCount = 0;
        int pageSize = size == 0 ? DEFAULT_PAGE_SIZE : (int) size;

        while (fetchedCount < MAX_RESULTS_LIMIT) {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("query", query);
            queryParams.put("startAt", Math.max((currentPage - 1) * pageSize, 0));
            queryParams.put("maxResults", pageSize);

            String jsonResponse = getRestClient().fetch("user/search", queryParams, String.class);
            JsonArray userArray = GSON.fromJson(jsonResponse, JsonArray.class);

            if (userArray == null) {
                String errorMsg = String.format("Failed to parse user search response for query: %s. Response was null or invalid.", query);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            if (userArray.size() == 0) {
                break; // No more results
            }

            List<JiraUser> pageUsers = parseUsersFromJsonArray(userArray);
            totalUsers.addAll(pageUsers);
            fetchedCount += pageUsers.size();

            if (userArray.size() < DEFAULT_PAGE_SIZE) {
                break; // Last page
            }

            currentPage++;
        }

        if (fetchedCount >= MAX_RESULTS_LIMIT) {
            LOGGER.debug("Reached maximum result limit of {} for query: {}", MAX_RESULTS_LIMIT, sanitizeForLogging(query));
        }

        return totalUsers;
    }

    /**
     * Parses a JSON array of user objects into a list of JiraUser instances.
     *
     * @param userArray JSON array containing user objects
     * @return List of JiraUser objects parsed from the array
     */
    private List<JiraUser> parseUsersFromJsonArray(JsonArray userArray) {
        List<JiraUser> users = new ArrayList<>();
        for (JsonElement element : userArray) {
            users.add(JiraUser.create(element.getAsJsonObject()));
        }
        return users;
    }

    /**
     * Searches for users by query and returns them as User entities.
     * Uses pagination with default page size of 50.
     *
     * @param query The search query (email, username, or display name)
     * @return List of User entities matching the query
     */
    public List<User> getUsersByQuery(String query) {
        return searchUser(query, 1, 50).stream().map(JiraUser::getUser).collect(Collectors.toList());
    }

    /**
     * Retrieves a user by their Jira account ID.
     *
     * @param accountId The Jira account ID
     * @return User entity for the given account ID
     * @throws IllegalStateException if the response is null or invalid
     */
    public User getUserByAccountId(String accountId) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("accountId", accountId);
        String jsonResponse = getRestClient().fetch("user", queryParams, String.class);

        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            LOGGER.error("Empty or null response from Jira API for accountId: {}", sanitizeForLogging(accountId));
            throw new IllegalStateException("Failed to retrieve user for accountId: " + accountId);
        }

        JsonObject userObject = GSON.fromJson(jsonResponse, JsonObject.class);
        if (userObject == null) {
            LOGGER.error("Failed to parse JSON response for accountId: {}", sanitizeForLogging(accountId));
            throw new IllegalStateException("Invalid JSON response for accountId: " + accountId);
        }

        return JiraUser.create(userObject).getUser();
    }

    /**
     * Checks if a user exists in Jira by their account ID.
     *
     * @param accountId The Jira account ID to check
     * @return true if user exists, false otherwise
     * @throws IllegalCallerException if there's a caller permission issue
     * @throws MustAuthorizeException if authorization is required
     */
    public Boolean isUserExist(String accountId) {
        try {
            getUserByAccountId(accountId);
            return true;
        } catch (IllegalCallerException cause) {
            throw new IllegalCallerException(cause.getMessage());
        } catch (MustAuthorizeException cause) {
            throw cause;
        } catch (Exception cause) {
            return false;
        }
    }

    /**
     * Search for a user by email or name and return the User object if found.
     * Returns null if no user is found.
     *
     * @param searchQuery The email address or name to search for
     * @return User object if found, null otherwise
     */
    public User getUserByEmailOrName(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return null;
        }

        List<JiraUser> users = searchUser(searchQuery.trim());

        if (users.isEmpty()) {
            return null;
        }
        return users.get(0).getUser();
    }

}