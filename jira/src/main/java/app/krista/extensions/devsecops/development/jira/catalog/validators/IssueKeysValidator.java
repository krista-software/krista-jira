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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IssueKeysValidator implements Validator {

    private final IssueDao issueDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueKeysValidator.class);
    private static final Gson GSON = new Gson();

    public IssueKeysValidator(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            // Parse the JSON string to List<String>
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> issueKeys = GSON.fromJson(resourceId, listType);
            
            if (issueKeys == null || issueKeys.isEmpty()) {
                LOGGER.debug("Issue keys list is null or empty");
                return false;
            }
            
            // Check for duplicates
            Set<String> uniqueKeys = issueKeys.stream()
                    .map(String::trim)
                    .filter(key -> !key.isEmpty())
                    .collect(Collectors.toSet());
            
            if (uniqueKeys.size() != issueKeys.size()) {
                LOGGER.debug("Duplicate issue keys found in the list");
                return false;
            }
            
            // Validate each issue key exists
            for (String issueKey : uniqueKeys) {
                if (!issueDao.isIssueExists(issueKey.trim())) {
                    LOGGER.debug("Issue key does not exist: {}", issueKey);
                    return false;
                }
            }
            
            return true;
            
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalCallerException(cause.getMessage(), cause);
        } catch (Exception e) {
            LOGGER.error("Error validating issue keys: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return "Issue Key";
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid issue keys. Each issue key must exist in your Jira instance.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> issueKeys = GSON.fromJson(resourceId, listType);
            
            if (issueKeys == null || issueKeys.isEmpty()) {
                return "No issue keys were provided.";
            }
            
            // Find invalid issue keys
            List<String> invalidKeys = issueKeys.stream()
                    .filter(key -> {
                        try {
                            return !issueDao.isIssueExists(key.trim());
                        } catch (Exception e) {
                            return true; // Consider as invalid if error occurs
                        }
                    })
                    .collect(Collectors.toList());
            
            // Check for duplicates
            Set<String> uniqueKeys = issueKeys.stream()
                    .map(String::trim)
                    .filter(key -> !key.isEmpty())
                    .collect(Collectors.toSet());
            
            if (uniqueKeys.size() != issueKeys.size()) {
                return String.format("Duplicate issue keys detected in your list: %s\n\n" +
                                   "Please remove the duplicates and provide only unique issue keys. " +
                                   "Each issue should be listed only once.",
                        String.join(", ", issueKeys));
            }

            if (!invalidKeys.isEmpty()) {
                return String.format("The following issue keys could not be found in JIRA: %s\n\n" +
                                   "Please verify that:\n" +
                                   "• The issue keys are spelled correctly\n" +
                                   "• The issues exist in your JIRA instance\n" +
                                   "• You have permission to view these issues\n" +
                                   "• The format follows PROJECT-NUMBER pattern (e.g., DEMO-123)",
                        String.join(", ", invalidKeys));
            }

            return "Some issue keys in your list are invalid or cannot be accessed.\n\n" +
                   "Please verify that all issue keys:\n" +
                   "• Exist in your JIRA instance\n" +
                   "• Follow the correct format (PROJECT-NUMBER)\n" +
                   "• Are accessible with your current permissions";
            
        } catch (Exception e) {
            LOGGER.error("Error creating confirmation message: {}", e.getMessage());
            return "Invalid issue keys provided. Please check and provide valid issue keys.";
        }
    }

    @Override
    public String getErrMessage(String resourceId) {
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> issueKeys = GSON.fromJson(resourceId, listType);
            
            if (issueKeys == null || issueKeys.isEmpty()) {
                return "Issue keys list cannot be null or empty.";
            }
            
            return String.format("Invalid issue keys provided: %s", String.join(", ", issueKeys));
            
        } catch (Exception e) {
            LOGGER.error("Error creating error message: {}", e.getMessage());
            return "Invalid issue keys format or content.";
        }
    }
}
