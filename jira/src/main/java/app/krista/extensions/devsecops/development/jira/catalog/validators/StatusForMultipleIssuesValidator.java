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
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TransitionStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StatusForMultipleIssuesValidator implements Validator {

    private final TicketInfoDao ticketInfoDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusForMultipleIssuesValidator.class);
    private static final Gson GSON = new Gson();

    public StatusForMultipleIssuesValidator(TicketInfoDao ticketInfoDao) {
        this.ticketInfoDao = ticketInfoDao;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            // Get the status name from resourceId
            String statusName = resourceId.trim();
            
            if (statusName.isEmpty()) {
                LOGGER.debug("Status name is empty");
                return false;
            }
            
            // Get issue keys from context
            String issueKeysJson = context.get(ValidationResource.ISSUE_KEYS);
            if (issueKeysJson == null) {
                LOGGER.debug("Issue keys not provided in context");
                return false;
            }
            
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> issueKeys = GSON.fromJson(issueKeysJson, listType);
            
            if (issueKeys == null || issueKeys.isEmpty()) {
                LOGGER.debug("Issue keys list is null or empty");
                return false;
            }
            
            // Check if the status can be applied to all issues
            for (String issueKey : issueKeys) {
                List<TransitionStatus> transitions = ticketInfoDao.getTransitions(issueKey.trim());
                boolean canTransition = transitions.stream()
                        .anyMatch(transition -> transition.toName.equalsIgnoreCase(statusName));
                
                if (!canTransition) {
                    LOGGER.debug("Status '{}' cannot be applied to issue '{}'", statusName, issueKey);
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
            LOGGER.error("Error validating status for multiple issues: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return "Status Name";
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid status name that can be applied to all selected issues.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        try {
            String statusName = resourceId.trim();
            
            // Get issue keys from context
            String issueKeysJson = context.get(ValidationResource.ISSUE_KEYS);
            if (issueKeysJson == null) {
                return String.format("The status '%s' cannot be validated without issue keys.", statusName);
            }
            
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> issueKeys = GSON.fromJson(issueKeysJson, listType);
            
            if (issueKeys == null || issueKeys.isEmpty()) {
                return String.format("The status '%s' cannot be validated without valid issue keys.", statusName);
            }
            
            // Find issues that cannot transition to this status
            List<String> invalidIssues = issueKeys.stream()
                    .filter(issueKey -> {
                        try {
                            List<TransitionStatus> transitions = ticketInfoDao.getTransitions(issueKey.trim());
                            return transitions.stream()
                                    .noneMatch(transition -> transition.toName.equalsIgnoreCase(statusName));
                        } catch (Exception e) {
                            return true; // Consider as invalid if error occurs
                        }
                    })
                    .collect(Collectors.toList());
            
            if (!invalidIssues.isEmpty()) {
                // Get available statuses for the first invalid issue as an example
                try {
                    String firstInvalidIssue = invalidIssues.get(0);
                    List<TransitionStatus> availableTransitions = ticketInfoDao.getTransitions(firstInvalidIssue);
                    String availableStatuses = availableTransitions.stream()
                            .map(transition -> transition.toName)
                            .distinct()
                            .sorted()
                            .collect(Collectors.joining(", "));
                    
                    return String.format("The status '%s' cannot be applied to the following issues: %s. " +
                            "For example, issue '%s' can only transition to: %s", 
                            statusName, String.join(", ", invalidIssues), firstInvalidIssue, availableStatuses);
                } catch (Exception e) {
                    return String.format("The status '%s' cannot be applied to the following issues: %s. " +
                            "Please check the available transitions for each issue.", 
                            statusName, String.join(", ", invalidIssues));
                }
            }
            
            return String.format("The status '%s' cannot be applied to all selected issues.", statusName);
            
        } catch (Exception e) {
            LOGGER.error("Error creating confirmation message: {}", e.getMessage());
            return String.format("The status '%s' is not valid for the selected issues.", resourceId);
        }
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid status name for multiple issues: %s", resourceId);
    }
}
