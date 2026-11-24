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

import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for status names against actual Jira statuses.
 * Validates List<String> of status names and provides user-friendly error messages.
 */
public class StatusNamesValidator implements Validator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusNamesValidator.class);
    private static final Gson GSON = new Gson();
    
    private final TicketInfoDao ticketInfoDao;
    
    public StatusNamesValidator(TicketInfoDao ticketInfoDao) {
        this.ticketInfoDao = ticketInfoDao;
    }
    
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            // Parse the JSON string to List<String>
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> statusNames = GSON.fromJson(resourceId, listType);
            
            if (statusNames == null || statusNames.isEmpty()) {
                LOGGER.debug("Status names list is null or empty");
                return false;
            }
            
            // Get available statuses from Jira
            List<TicketStatus> availableStatuses = ticketInfoDao.getStatuses();
            Set<String> validStatusNames = availableStatuses.stream()
                    .map(status -> status.name)
                    .collect(Collectors.toSet());
            
            // Check for duplicates (case-insensitive)
            Set<String> seenStatuses = new HashSet<>();
            for (String statusName : statusNames) {
                if (statusName == null || statusName.trim().isEmpty()) {
                    LOGGER.debug("Found null or empty status name");
                    return false;
                }
                
                String normalizedStatus = statusName.trim().toLowerCase();
                if (seenStatuses.contains(normalizedStatus)) {
                    LOGGER.debug("Found duplicate status name: {}", statusName);
                    return false;
                }
                seenStatuses.add(normalizedStatus);
                
                // Check if status exists in Jira
                boolean statusExists = validStatusNames.stream()
                        .anyMatch(validStatus -> validStatus.equalsIgnoreCase(statusName.trim()));
                
                if (!statusExists) {
                    LOGGER.debug("Invalid status name: {}", statusName);
                    return false;
                }
            }
            
            LOGGER.debug("All status names are valid: {}", statusNames);
            return true;
            
        } catch (Exception e) {
            LOGGER.error("Error validating status names: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getFetchFieldName() {
        return "Status Names";
    }
    
    @Override
    public String getFetchStepMessage() {
        try {
            List<TicketStatus> availableStatuses = ticketInfoDao.getStatuses();
            String statusList = availableStatuses.stream()
                    .map(status -> status.name)
                    .distinct()  // Remove duplicate status names
                    .sorted()
                    .collect(Collectors.joining(", "));
            
            return String.format("Please enter valid status names from your JIRA workflow.\n\n" +
                                "Available statuses in your JIRA instance:\n%s\n\n" +
                                "Note: Status names are case-sensitive and must match exactly as they appear in JIRA.", statusList);
        } catch (Exception e) {
            LOGGER.error("Error getting available statuses: {}", e.getMessage());
            return "Unable to retrieve available status names from JIRA at this time.\n\n" +
                   "Please enter valid status names that exist in your JIRA workflow. " +
                   "Common statuses include: To Do, In Progress, Done, Closed.\n\n" +
                   "If you're unsure of the exact status names, check your JIRA project's workflow configuration.";
        }
    }
    
    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> statusNames = GSON.fromJson(resourceId, listType);
            
            if (statusNames == null || statusNames.isEmpty()) {
                return "No status names were provided.";
            }
            
            // Get available statuses
            List<TicketStatus> availableStatuses = ticketInfoDao.getStatuses();
            Set<String> validStatusNames = availableStatuses.stream()
                    .map(status -> status.name)
                    .collect(Collectors.toSet());
            
            // Find invalid statuses
            List<String> invalidStatuses = statusNames.stream()
                    .filter(statusName -> statusName != null && !statusName.trim().isEmpty())
                    .filter(statusName -> validStatusNames.stream()
                            .noneMatch(validStatus -> validStatus.equalsIgnoreCase(statusName.trim())))
                    .collect(Collectors.toList());
            
            // Check for duplicates
            Set<String> seenStatuses = new HashSet<>();
            List<String> duplicateStatuses = statusNames.stream()
                    .filter(statusName -> statusName != null && !statusName.trim().isEmpty())
                    .filter(statusName -> !seenStatuses.add(statusName.trim().toLowerCase()))
                    .collect(Collectors.toList());
            
            StringBuilder message = new StringBuilder();
            
            if (!invalidStatuses.isEmpty()) {
                message.append("The following status names are not valid in your JIRA workflow: ")
                       .append(String.join(", ", invalidStatuses))
                       .append("\n\nPlease use only status names that exist in your JIRA project's workflow.");
            }

            if (!duplicateStatuses.isEmpty()) {
                if (message.length() > 0) {
                    message.append("\n\n");
                }
                message.append("Duplicate status names detected: ").append(String.join(", ", duplicateStatuses))
                       .append("\n\nPlease remove duplicates and provide each status name only once.");
            }

            if (message.length() == 0) {
                message.append("Some status names in your list are invalid or duplicated.\n\n")
                       .append("Please ensure all status names:\n")
                       .append("• Exist in your JIRA workflow\n")
                       .append("• Are spelled correctly (case-sensitive)\n")
                       .append("• Are not duplicated in the list");
            }
            
            return message.toString();
            
        } catch (Exception e) {
            LOGGER.error("Error creating confirmation message: {}", e.getMessage());
            return "The provided status names are invalid.";
        }
    }
    
    @Override
    public String getErrMessage(String resourceId) {
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> statusNames = GSON.fromJson(resourceId, listType);
            
            if (statusNames == null || statusNames.isEmpty()) {
                return "Status names list cannot be null or empty.";
            }
            
            return String.format("Invalid status names provided: %s", String.join(", ", statusNames));
            
        } catch (Exception e) {
            LOGGER.error("Error creating error message: {}", e.getMessage());
            return "Invalid status names format or content.";
        }
    }
}
