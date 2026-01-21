# Update an Issue

## Overview

Update various properties of a JIRA issue including status, assignee, summary, description, priority, and custom fields in a single operation.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Issue Key | Text | Yes | Key of the issue you want to update | `DEMO-123` |
| Summary | Text | No | New summary/title for the issue | `Updated: Fix login authentication bug` |
| Description | Text | No | New description for the issue | `Updated description with more details...` |
| Status | Text | No | New status for the issue | `In Progress`, `Done`, `Code Review` |
| Priority | Text | No | New priority level for the issue | `High`, `Medium`, `Low`, `Critical` |
| Assignee | Text | No | Account ID of the new assignee | `5d7c8a9b1234567890abcdef` |
| Reporter | Text | No | Account ID of the new reporter | `5d7c8a9b1234567890abcdef` |
| Labels | Text | No | Comma-separated list of labels | `bug,urgent,frontend` |
| Components | Text | No | Comma-separated list of components | `Authentication,Frontend` |
| Fix Version | Text | No | Version where the issue will be fixed | `2.1.0` |
| Due Date | Text | No | New due date (YYYY-MM-DD format) | `2025-12-31` |
| Environment | Text | No | Environment information | `Production` |
| Resolution | Text | No | Resolution status | `Fixed`, `Won't Fix`, `Duplicate` |
| Custom Field Values | Text | No | JSON string of custom field updates | `{"customfield_10001": "new value"}` |

### Parameter Details

**Issue Key**
- Must be a valid, existing JIRA issue key
- Format: PROJECT-NUMBER (e.g., DEMO-123)
- User must have edit permissions on the issue

**Status**
- Must be a valid status in the issue's workflow
- Status transitions must be allowed by workflow rules
- Some status changes may require additional fields

**Priority**
- Must be a valid priority level configured in JIRA
- Common priorities: Blocker, Critical, High, Medium, Low

**Assignee/Reporter**
- Must be valid JIRA account IDs (not usernames)
- Users must exist and have access to the project
- Use empty string to unassign (for assignee)

**Labels**
- Comma-separated list replaces all existing labels
- Use empty string to remove all labels
- Labels are case-sensitive

**Custom Field Values**
- JSON format: `{"fieldId": "value"}`
- Use actual custom field IDs (e.g., customfield_10001)
- Values must match field type requirements

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Updated Issue | Entity(Issue) | Complete details of the updated issue | Issue object with all current field values |

### Issue Entity Structure

The returned Issue entity contains all current field values after the update:
- `id`: Internal JIRA issue ID
- `key`: Issue key (unchanged)
- `summary`: Updated summary
- `description`: Updated description
- `status`: Current status after update
- `priority`: Current priority
- `assigneeName`: Name of current assignee
- `reporterName`: Name of current reporter
- `projectKey`: Project key (unchanged)
- `issueType`: Issue type (unchanged)
- `updated`: Timestamp of the update

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Issue exists | The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue. | Verify issue key and access permissions |
| Edit permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request edit permissions for the issue |
| Status transition | This status transition is not allowed by the workflow. Please check the available transitions for this issue. | Use valid status transition from current state |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide values for workflow-required fields |
| Field validation | The field value is invalid. Please check the format and requirements for this field. | Use valid values for field types |
| User exists | The specified user could not be found in JIRA. Please verify the username or email address is correct. | Use valid account IDs for assignee/reporter |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing issue key or invalid field formats
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty issue key, invalid date format, malformed JSON
- **Resolution**: Provide valid issue key and properly formatted field values

### Logic Errors (LOGIC_ERROR)
- **Cause**: Issue not found, invalid status transition, or field validation failures
- **Error Message**: "Failed to update the issue in JIRA. Please verify you have edit permissions for this issue and that all field values are valid."
- **Common Scenarios**: Invalid status transition, required field missing, invalid field values
- **Resolution**: Check workflow rules and field requirements

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or update processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "Your JIRA session has expired. Please re-authorize your account by clicking the 'Authorize' button and signing in again."
- **Common Scenarios**: Token expiration, permission changes
- **Resolution**: Re-authenticate and verify edit permissions

## Usage Examples

### Example 1: Update Status and Assignee
**Input:**
- Issue Key: `DEMO-123`
- Status: `In Progress`
- Assignee: `5d7c8a9b1234567890abcdef`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "12345",
    "key": "DEMO-123",
    "summary": "Fix login authentication bug",
    "status": "In Progress",
    "assigneeName": "John Doe",
    "updated": "2025-01-15T14:30:00.000Z"
  }
}
```

### Example 2: Update Summary and Priority
**Input:**
- Issue Key: `PROJ-456`
- Summary: `Critical: Database connection timeout`
- Priority: `Critical`
- Labels: `database,critical,production`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "12346",
    "key": "PROJ-456",
    "summary": "Critical: Database connection timeout",
    "priority": "Critical",
    "labels": ["database", "critical", "production"],
    "updated": "2025-01-15T14:35:00.000Z"
  }
}
```

### Example 3: Complete Issue Resolution
**Input:**
- Issue Key: `BUG-789`
- Status: `Done`
- Resolution: `Fixed`
- Fix Version: `2.1.0`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "12347",
    "key": "BUG-789",
    "status": "Done",
    "resolution": "Fixed",
    "fixVersion": "2.1.0",
    "updated": "2025-01-15T14:40:00.000Z"
  }
}
```

### Example 4: Retry Scenario - Invalid Status Transition
**Input:**
- Issue Key: `DEMO-123`
- Status: `Closed`

**Expected Output:**
```json
{
  "error": "This status transition is not allowed by the workflow. Please check the available transitions for this issue."
}
```

## Business Rules

1. **Workflow Compliance**: All status changes must follow configured workflow rules
2. **Required Fields**: Workflow may require additional fields for certain transitions
3. **Permission Requirements**: User must have edit permissions on the issue
4. **Field Constraints**: All field values must comply with project-specific constraints
5. **Audit Trail**: All changes are logged in issue history with timestamp and user

## Limitations

- **Bulk Updates**: Only one issue can be updated per request
- **Workflow Restrictions**: Status changes limited by workflow configuration
- **Custom Fields**: Complex custom field types may have specific update requirements
- **File Attachments**: File attachments cannot be updated through this request
- **Issue Type Changes**: Issue type changes may not be supported in all configurations

## Best Practices

1. **Incremental Updates**: Update only the fields that need to change
2. **Status Workflow**: Understand your project's workflow before changing status
3. **Required Fields**: Check workflow requirements for status transitions
4. **Permission Verification**: Ensure user has appropriate edit permissions
5. **Validation**: Validate field values before submitting updates
6. **Audit Awareness**: Remember that all changes are tracked in issue history

## Common Use Cases

### Bug Triage Workflow
1. Identify bug that needs priority adjustment
2. Update priority and assignee based on severity
3. Add relevant labels for categorization
4. Set appropriate due date for resolution
5. Move to "In Progress" status

### Story Completion Process
1. Update story status to "Code Review" when development complete
2. Add resolution details in description
3. Set fix version for release tracking
4. Assign to QA team member for testing
5. Update labels to reflect completion stage

### Issue Escalation
1. Increase priority level for urgent issues
2. Reassign to senior team member or lead
3. Update summary to reflect escalation
4. Add escalation labels and components
5. Set shorter due date for resolution

### Release Preparation
1. Update fix version for issues in upcoming release
2. Verify all issues have appropriate status
3. Update descriptions with release notes information
4. Assign final reviewers for release validation
5. Set resolution status for completed items

## Related Catalog Requests

- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve issue details before updating
- [Create an Issue](pages/CreateAnIssue.md) - Create new issues
- [Update Status of Multiple Issues](pages/UpdateStatusOfMultipleIssues.md) - Bulk status updates
- [Assign Issue to Epic](pages/AssignIssueToEpic.md) - Link issues to epics

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Issue existence check → Permission validation → Field validation → Workflow validation → Update execution
- **Service Delegation**: Delegates to JIRA REST API v3 issue update endpoints
- **Telemetry Metrics**: Tracks update success rates, field change patterns, and performance metrics

## Troubleshooting

### Status Transition Errors
**Problem**: Cannot change issue status to desired value
**Solution**:
1. Check workflow configuration for allowed transitions
2. Verify current status allows transition to target status
3. Ensure required fields are provided for transition
4. Contact JIRA administrator for workflow clarification

### Permission Denied Errors
**Problem**: User cannot update issue fields
**Solution**:
1. Verify user has edit permissions on the issue
2. Check project-level permissions
3. Ensure issue is not in a restricted status
4. Contact JIRA administrator for permission review

### Field Validation Errors
**Problem**: Field values are rejected during update
**Solution**:
1. Verify field values match expected formats
2. Check field configuration for validation rules
3. Ensure custom field IDs are correct
4. Test with simpler field updates first

### Workflow Requirement Errors
**Problem**: Status change requires additional fields
**Solution**:
1. Identify required fields for the status transition
2. Provide values for all required fields in the update
3. Check field configuration in workflow
4. Contact JIRA administrator for workflow requirements

## See Also

- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve issue details
- [Create an Issue](pages/CreateAnIssue.md) - Create new issues
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
