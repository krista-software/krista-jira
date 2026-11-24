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
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ValidationOrchestrator {

    @Inject
    public ValidationOrchestrator(IssueDao issueDao, ProjectDao projectDao, UserDao userDao,
                                  TicketInfoDao ticketInfoDao, RequestContext requestContext, AuthorizationContext authorizationContext) {
        validators.put(Validator.ValidationResource.ISSUE_TYPE, new IssueTypeValidator(issueDao));
        validators.put(Validator.ValidationResource.PROJECT_KEY, new ProjectValidator(projectDao));
        validators.put(Validator.ValidationResource.TICKET_ID, new IssueValidator(issueDao));
        validators.put(Validator.ValidationResource.USER_ID, new UserValidator(userDao));
        validators.put(Validator.ValidationResource.STATUS_NAME, new StatusNameValidator(ticketInfoDao));
        validators.put(Validator.ValidationResource.STATUS_NAMES, new StatusNamesValidator(ticketInfoDao));
        validators.put(Validator.ValidationResource.EMAIL, new EmailValidator());
        validators.put(Validator.ValidationResource.EPIC_KEY, new EpicKeyValidator(issueDao));
        validators.put(Validator.ValidationResource.CHILD_ISSUE_KEY, new ChildIssueValidator(issueDao));
        validators.put(Validator.ValidationResource.PARENT_EPIC_KEY, new ParentEpicValidator(issueDao));
        validators.put(Validator.ValidationResource.JQL_QUERY, new QueryValidator(issueDao, requestContext, authorizationContext));
        validators.put(Validator.ValidationResource.SUMMARY, new SummaryValidator());
        validators.put(Validator.ValidationResource.ISSUE_KEYS, new IssueKeysValidator(issueDao));
        validators.put(Validator.ValidationResource.STATUS_FOR_MULTIPLE_ISSUES, new StatusForMultipleIssuesValidator(ticketInfoDao));
        validators.put(Validator.ValidationResource.GLOBAL_ISSUE_TYPE, new GlobalIssueTypeValidator(projectDao));
    }

    public static class ValidationResult {
        private final String confirmStepMessage;
        private final String fetchFieldName;
        private final String fetchStepMessage;
        private final String errMessage;
        private final String fieldType;
        private final Map<String, Object> fieldAttributes;

        // Backward compatible constructor
        public ValidationResult(String confirmStepMessage, String fetchFieldName, String fetchStepMessage, String errMessage) {
            this(confirmStepMessage, fetchFieldName, fetchStepMessage, errMessage, "Text", Map.of());
        }

        // Enhanced constructor with field type and attributes
        public ValidationResult(String confirmStepMessage, String fetchFieldName, String fetchStepMessage,
                               String errMessage, String fieldType, Map<String, Object> fieldAttributes) {
            this.confirmStepMessage = confirmStepMessage;
            this.fetchFieldName = fetchFieldName;
            this.fetchStepMessage = fetchStepMessage;
            this.errMessage = errMessage;
            this.fieldType = fieldType != null ? fieldType : "Text";
            this.fieldAttributes = fieldAttributes != null ? fieldAttributes : Map.of();
        }

        public String getConfirmStepMessage() {
            return confirmStepMessage;
        }

        public String getErrMessage() {
            return errMessage;
        }

        public String getFetchFieldName() {
            return fetchFieldName;
        }

        public String getFetchStepMessage() {
            return fetchStepMessage;
        }

        public String getFieldType() {
            return fieldType;
        }

        public Map<String, Object> getFieldAttributes() {
            return fieldAttributes;
        }
    }

    private final Map<Validator.ValidationResource, Validator> validators = new HashMap<>();

    public List<ValidationResult> validate(Map<Validator.ValidationResource, String> resources) {
        List<ValidationResult> results = new ArrayList<>();
        for (Map.Entry<Validator.ValidationResource, String> entry : resources.entrySet()) {
            Validator validator = validators.get(entry.getKey());
            assert validator != null;
            if (!validator.validate(entry.getValue(), resources)) {
                results.add(new ValidationResult(validator.getConfirmationStepMessage(entry.getValue(), resources),
                        validator.getFetchFieldName(), validator.getFetchStepMessage(),
                        validator.getErrMessage(entry.getValue())));
            }
        }
        return results;
    }

    /**
     * Enhanced validation method that extracts field types and attributes from source method annotations.
     * This enables proper field recreation during error handling flows.
     *
     * @param resources The validation resources to validate
     * @param sourceClass The class containing the source method
     * @param sourceMethodName The source method name for field extraction
     * @return List of validation results with field type information
     */
    public List<ValidationResult> validate(Map<Validator.ValidationResource, String> resources,
                                         Class<?> sourceClass, String sourceMethodName) {
        List<ValidationResult> results = new ArrayList<>();

        // Try to find the source method for field extraction
        java.lang.reflect.Method sourceMethod = null;
        try {
            for (java.lang.reflect.Method method : sourceClass.getDeclaredMethods()) {
                if (method.getName().equals(sourceMethodName)) {
                    sourceMethod = method;
                    break;
                }
            }
        } catch (Exception e) {
            // Fall back to basic validation if method lookup fails
            return validate(resources);
        }

        for (Map.Entry<Validator.ValidationResource, String> entry : resources.entrySet()) {
            Validator validator = validators.get(entry.getKey());
            assert validator != null;
            if (!validator.validate(entry.getValue(), resources)) {
                String fieldName = validator.getFetchFieldName();
                String fieldType = "Text";
                Map<String, Object> fieldAttributes = Map.of();

                // Extract field type and attributes if source method is available
                if (sourceMethod != null) {
                    try {
                        fieldType = FieldAttributeExtractor.extractFieldType(sourceMethod, fieldName);
                        fieldAttributes = FieldAttributeExtractor.extractFieldAttributes(sourceMethod, fieldName);
                    } catch (Exception e) {
                        // Use defaults if extraction fails
                        fieldType = "Text";
                        fieldAttributes = Map.of();
                    }
                }

                results.add(new ValidationResult(
                    validator.getConfirmationStepMessage(entry.getValue(), resources),
                    fieldName,
                    validator.getFetchStepMessage(),
                    validator.getErrMessage(entry.getValue()),
                    fieldType,
                    fieldAttributes
                ));
            }
        }
        return results;
    }
}
