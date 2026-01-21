# Update Status of Issue

## Overview

Change the status of a specific JIRA issue to reflect its current progress in the workflow (e.g., from 'To Do' to 'In Progress').

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Issue Key | Text | Yes | Key of the issue whose status you want to update | `DEMO-123` |
| Status Name | Text | Yes | New status to apply to the issue | `In Progress`, `Done`, `Code Review` |

### Parameter Details

**Issue Key**
- Must be a valid JIRA issue key in PROJECT-NUMBER format
- Issue must exist and be accessible to the requesting user
- User must have edit permissions on the issue
- Case-sensitive (typically uppercase project key)

**Status Name**
- Must be a valid status in the issue's workflow
- Status transition must be allowed by workflow rules
- Case-sensitive matching with configured status names
- Must be available as a transition from the current status

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Updated Issue | Entity(Issue) | Complete details of the issue with updated status | Issue object with new status and update timestamp |

### Issue Entity Structure

The returned Issue entity contains:
- `id`: Internal JIRA issue ID
- `key`: Issue key (unchanged)
- `summary`: Issue title/summary
- `description`: Issue description
- `status`: New status after update
- `priority`: Issue priority level
- `assigneeName`: Name of assigned user
- `reporterName`: Name of reporting user
- `projectKey`: Project key
- `issueType`: Issue type
- `updated`: Timestamp of the status update

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Issue exists | The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue. | Verify issue key and access permissions |
| Status valid | Please enter valid status names from your JIRA workflow. | Use exact status name as configured in JIRA |
| Status transition | This status transition is not allowed by the workflow. Please check the available transitions for this issue. | Use valid status transition from current state |
| Edit permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request edit permissions for the issue |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide both issue key and status name |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid issue key format
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty issue key or status name, malformed key format
- **Resolution**: Provide valid issue key and status name

### Logic Errors (LOGIC_ERROR)
- **Cause**: Issue not found, invalid status transition, or permission issues
- **Error Message**: "This status transition is not allowed by the workflow. Please check the available transitions for this issue."
- **Common Scenarios**: Invalid status transition, workflow restrictions, insufficient permissions
- **Resolution**: Check workflow rules and available transitions

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or status update processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "Your JIRA session has expired. Please re-authorize your account by clicking the 'Authorize' button and signing in again."
- **Common Scenarios**: Token expiration, missing edit permissions
- **Resolution**: Re-authenticate and verify permissions

## Usage Examples

### Example 1: Move Issue to In Progress
**Input:**
- Issue Key: `DEMO-123`
- Status Name: `In Progress`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "10001",
    "key": "DEMO-123",
    "summary": "Fix login authentication bug",
    "status": "In Progress",
    "updated": "2025-01-15T14:30:00.000Z"
  }
}
```

### Example 2: Complete Issue
**Input:**
- Issue Key: `PROJ-456`
- Status Name: `Done`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "10002",
    "key": "PROJ-456",
    "summary": "Implement user dashboard",
    "status": "Done",
    "updated": "2025-01-15T14:35:00.000Z"
  }
}
```

### Example 3: Move to Code Review
**Input:**
- Issue Key: `TEST-789`
- Status Name: `Code Review`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "10003",
    "key": "TEST-789",
    "summary": "Add unit tests",
    "status": "Code Review",
    "updated": "2025-01-15T14:40:00.000Z"
  }
}
```

## Business Rules

1. **Workflow Compliance**: Status changes must follow configured workflow rules
2. **Permission Requirements**: User must have edit permissions on the issue
3. **Transition Validation**: Only valid transitions from current status are allowed
4. **Audit Trail**: Status changes are logged in issue history
5. **Single Issue**: Only one issue can be updated per request

## Limitations

- **Single Issue Only**: Cannot update multiple issues in one request
- **Workflow Restrictions**: Status changes limited by workflow configuration
- **Permission Dependent**: Update limited by user's permissions on the issue
- **Transition Rules**: Some transitions may require additional fields
- **Status Only**: This request only updates status, not other fields

## Best Practices

1. **Workflow Understanding**: Understand your project's workflow before changing status
2. **Permission Verification**: Ensure edit permissions before attempting updates
3. **Status Validation**: Verify status names match JIRA configuration exactly
4. **Transition Planning**: Plan status transitions according to workflow rules
5. **Error Handling**: Always check for successful status updates
6. **Audit Awareness**: Remember that status changes are tracked in issue history

## Common Use Cases

### Development Workflow
1. **Start Work**: Move issues from "To Do" to "In Progress" when starting development
2. **Code Complete**: Move to "Code Review" when development is finished
3. **Review Complete**: Move to "Testing" after code review approval
4. **Testing Complete**: Move to "Done" after successful testing

### Bug Lifecycle Management
1. **Bug Confirmation**: Move from "Open" to "Confirmed" after validation
2. **Fix in Progress**: Move to "In Progress" when developer starts work
3. **Fix Complete**: Move to "Fixed" when bug is resolved
4. **Verification**: Move to "Closed" after verification

### Project Management
1. **Task Assignment**: Move from "Backlog" to "To Do" for sprint planning
2. **Work Tracking**: Update status as work progresses through workflow
3. **Completion Tracking**: Move to final status when work is complete
4. **Quality Gates**: Use status transitions to enforce quality checkpoints

## Related Catalog Requests

- [Update an Issue](pages/UpdateAnIssue.md) - Update multiple issue fields including status
- [Update Status of Multiple Issues](pages/UpdateStatusOfMultipleIssues.md) - Bulk status updates
- [Get Issue By Key](pages/GetIssueByKey.md) - Check current status before updating
- [Get Issues](pages/GetIssues.md) - Find issues for status updates

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Issue key validation → Issue existence check → Status validation → Workflow validation → Status update
- **Service Delegation**: Delegates to JIRA REST API v3 issue transition endpoints
- **Telemetry Metrics**: Tracks status update success rates, transition patterns, and workflow usage

## Troubleshooting

### Status Transition Errors
**Problem**: Cannot change issue status to desired value
**Solution**:
1. Check workflow configuration for allowed transitions
2. Verify current status allows transition to target status
3. Ensure required fields are not needed for transition
4. Contact JIRA administrator for workflow clarification

### Permission Denied Errors
**Problem**: User cannot update issue status
**Solution**:
1. Verify user has edit permissions on the issue
2. Check project-level permissions
3. Ensure issue is not in a restricted status
4. Contact JIRA administrator for permission review

### Invalid Status Name Errors
**Problem**: Status name is not recognized
**Solution**:
1. Check exact spelling and capitalization of status name
2. Verify status exists in the issue's workflow
3. Use JIRA interface to confirm available statuses
4. Contact JIRA administrator for workflow configuration

## See Also

- [Update an Issue](pages/UpdateAnIssue.md) - Comprehensive issue updates
- [Update Status of Multiple Issues](pages/UpdateStatusOfMultipleIssues.md) - Bulk status updates
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve issue details
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
