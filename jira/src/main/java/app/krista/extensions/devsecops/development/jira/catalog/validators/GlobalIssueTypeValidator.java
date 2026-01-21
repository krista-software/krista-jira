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
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalIssueTypeValidator implements Validator {

    private final ProjectDao projectDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalIssueTypeValidator.class);

    public GlobalIssueTypeValidator(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            String issueType = resourceId.trim();
            
            if (issueType.isEmpty()) {
                LOGGER.debug("Issue type is empty");
                return false;
            }
            
            // Get all available issue types across all accessible projects
            Set<String> allIssueTypes = getAllAvailableIssueTypes();
            
            // Check if the issue type exists (case-insensitive)
            boolean exists = allIssueTypes.stream()
                    .anyMatch(type -> type.equalsIgnoreCase(issueType));
            
            if (!exists) {
                LOGGER.debug("Issue type '{}' does not exist in any accessible project", issueType);
                return false;
            }
            
            return true;
            
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalCallerException(cause.getMessage(), cause);
        } catch (Exception e) {
            LOGGER.error("Error validating global issue type: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return "Issue Type";
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid issue type that exists in your accessible projects.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        try {
            String issueType = resourceId.trim();
            
            // Get all available issue types
            Set<String> allIssueTypes = getAllAvailableIssueTypes();
            
            if (allIssueTypes.isEmpty()) {
                return String.format("The issue type '%s' is not valid. No issue types are available in your accessible projects.", issueType);
            }
            
            // Create a sorted list of available issue types for better user experience
            String availableTypes = allIssueTypes.stream()
                    .sorted()
                    .collect(Collectors.joining(", "));
            
            return String.format("The issue type '%s' does not exist in any of your accessible projects. " +
                    "Available issue types: %s", issueType, availableTypes);
            
        } catch (Exception e) {
            LOGGER.error("Error creating confirmation message: {}", e.getMessage());
            return String.format("The issue type '%s' is not valid. Please check and provide a valid issue type.", resourceId);
        }
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid issue type: %s", resourceId);
    }

    /**
     * Gets all available issue types across all accessible projects
     */
    private Set<String> getAllAvailableIssueTypes() {
        Set<String> allIssueTypes = new HashSet<>();

        try {
            // Get all accessible projects
            List<TicketProject> projects = projectDao.getAllProjects();

            // For each project, get its issue types
            for (TicketProject project : projects) {
                String projectKey = project.key;

                try {
                    // Get issue types for this project
                    String issueTypesResponse = projectDao.getIssueTypeFromProject(projectKey);
                    JsonArray issueTypes = JsonParser.parseString(issueTypesResponse).getAsJsonArray();

                    // Add all issue type names to our set
                    for (JsonElement issueTypeElement : issueTypes) {
                        JsonObject issueType = issueTypeElement.getAsJsonObject();
                        String issueTypeName = issueType.get("name").getAsString();
                        allIssueTypes.add(issueTypeName);
                    }
                } catch (Exception e) {
                    LOGGER.debug("Could not get issue types for project {}: {}", projectKey, e.getMessage());
                    // Continue with other projects
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error getting all available issue types: {}", e.getMessage());
        }

        return allIssueTypes;
    }
}
