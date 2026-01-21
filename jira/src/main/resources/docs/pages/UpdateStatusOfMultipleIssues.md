# Update Status of Multiple Issues

## Overview

Update the status of multiple JIRA issues simultaneously in a single bulk operation, enabling efficient workflow management and batch processing.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Issue Keys | Text | Yes | Comma-separated list of issue keys to update | `DEMO-123,PROJ-456,TEST-789` |
| Status | Text | Yes | New status to apply to all specified issues | `In Progress`, `Done`, `Code Review` |

### Parameter Details

**Issue Keys**
- Comma-separated list of valid JIRA issue keys
- Each key must follow PROJECT-NUMBER format (e.g., DEMO-123)
- All issues must exist and be accessible to the user
- Maximum recommended: 50 issues per request for optimal performance
- Duplicate keys are automatically removed

**Status**
- Must be a valid status in the workflow for each issue type
- Status transitions must be allowed by workflow rules for each issue
- Case-sensitive matching with configured status names
- All issues must support the target status transition

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Updated Issues | [ Entity(Issue) ] | Array of successfully updated issues with their new status | Array of Issue objects |
| Failed Issues | [ Text ] | Array of issue keys that failed to update with error details | `["DEMO-123: Invalid transition", "PROJ-456: Permission denied"]` |

### Output Details

**Updated Issues**
- Contains full issue details for successfully updated issues
- Each issue shows the new status and update timestamp
- Includes all standard issue fields (summary, priority, assignee, etc.)

**Failed Issues**
- Lists issue keys that could not be updated
- Includes specific error reason for each failed issue
- Common failure reasons: invalid transition, permission denied, issue not found

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Issue keys format | The following issue keys are not valid in your JIRA workflow: KEYS. Please use only issue keys that exist in your JIRA project's workflow. | Use valid PROJECT-NUMBER format for all issue keys |
| Issues exist | The following issue keys could not be found in JIRA: KEYS. Please verify that the issue keys are spelled correctly and are accessible with your current permissions. | Verify all issue keys exist and are accessible |
| Status valid | Please enter valid status names from your JIRA workflow. Available statuses in your JIRA instance: STATUS_LIST | Use exact status name as configured in JIRA |
| Workflow transitions | This status transition is not allowed by the workflow. Please check the available transitions for this issue. | Verify status transition is allowed for each issue type |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide both issue keys and target status |
| Permission check | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request edit permissions for all specified issues |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid issue key formats
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty issue keys, missing status, malformed issue keys
- **Resolution**: Provide valid issue keys and status

### Logic Errors (LOGIC_ERROR)
- **Cause**: Issues not found, invalid status transitions, or permission issues
- **Error Message**: "Some issue keys in your list are invalid or cannot be accessed. Please verify that all issue keys exist in your JIRA instance and follow the correct format (PROJECT-NUMBER)."
- **Common Scenarios**: Non-existent issues, workflow restrictions, insufficient permissions
- **Resolution**: Verify issue existence and workflow rules

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or bulk update processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, bulk operation timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "Your JIRA session has expired. Please re-authorize your account by clicking the 'Authorize' button and signing in again."
- **Common Scenarios**: Token expiration, missing edit permissions
- **Resolution**: Re-authenticate and verify permissions

## Usage Examples

### Example 1: Move Issues to In Progress
**Input:**
- Issue Keys: `DEMO-123,DEMO-124,DEMO-125`
- Status: `In Progress`

**Expected Output:**
```json
{
  "Updated Issues": [
    {
      "id": "10001",
      "key": "DEMO-123",
      "summary": "Fix login bug",
      "status": "In Progress",
      "updated": "2025-01-15T14:30:00.000Z"
    },
    {
      "id": "10002",
      "key": "DEMO-124",
      "summary": "Update documentation",
      "status": "In Progress",
      "updated": "2025-01-15T14:30:00.000Z"
    }
  ],
  "Failed Issues": [
    "DEMO-125: This status transition is not allowed by the workflow"
  ]
}
```

### Example 2: Complete Sprint Issues
**Input:**
- Issue Keys: `PROJ-456,PROJ-457,PROJ-458`
- Status: `Done`

**Expected Output:**
```json
{
  "Updated Issues": [
    {
      "id": "10003",
      "key": "PROJ-456",
      "summary": "Implement feature X",
      "status": "Done",
      "updated": "2025-01-15T14:35:00.000Z"
    },
    {
      "id": "10004",
      "key": "PROJ-457",
      "summary": "Fix performance issue",
      "status": "Done",
      "updated": "2025-01-15T14:35:00.000Z"
    },
    {
      "id": "10005",
      "key": "PROJ-458",
      "summary": "Update UI components",
      "status": "Done",
      "updated": "2025-01-15T14:35:00.000Z"
    }
  ],
  "Failed Issues": []
}
```

### Example 3: Mixed Success and Failure
**Input:**
- Issue Keys: `TEST-789,INVALID-123,TEST-790`
- Status: `Code Review`

**Expected Output:**
```json
{
  "Updated Issues": [
    {
      "id": "10006",
      "key": "TEST-789",
      "summary": "Add unit tests",
      "status": "Code Review",
      "updated": "2025-01-15T14:40:00.000Z"
    },
    {
      "id": "10007",
      "key": "TEST-790",
      "summary": "Integration testing",
      "status": "Code Review",
      "updated": "2025-01-15T14:40:00.000Z"
    }
  ],
  "Failed Issues": [
    "INVALID-123: The specified issue could not be found in JIRA"
  ]
}
```

## Business Rules

1. **Workflow Compliance**: All status changes must follow configured workflow rules
2. **Atomic Operations**: Each issue is updated independently - partial success is possible
3. **Permission Requirements**: User must have edit permissions on all specified issues
4. **Audit Trail**: All status changes are logged in issue history
5. **Validation Order**: Issues are validated before any updates are attempted

## Limitations

- **Batch Size**: Recommended maximum of 50 issues per request for optimal performance
- **Workflow Restrictions**: Status changes limited by individual issue workflow configurations
- **Single Status**: All issues receive the same status - cannot set different statuses per issue
- **Required Fields**: Some status transitions may require additional fields not supported in bulk
- **Performance Impact**: Large batches may impact JIRA server performance

## Best Practices

1. **Batch Sizing**: Use reasonable batch sizes (10-50 issues) to balance efficiency and performance
2. **Workflow Validation**: Verify status transitions are valid for all issue types before bulk update
3. **Permission Verification**: Ensure edit permissions on all issues before attempting bulk update
4. **Error Handling**: Always check both successful and failed updates in the response
5. **Progress Tracking**: For large batches, consider breaking into smaller chunks with progress tracking
6. **Audit Preparation**: Document bulk status changes for audit and compliance purposes

## Common Use Cases

### Sprint Management
1. **Sprint Start**:
   - Move all sprint backlog items from "To Do" to "In Progress"
   - Update multiple user stories simultaneously
   - Coordinate team workflow transitions
   - Track sprint progress efficiently

2. **Sprint Completion**:
   - Move completed items to "Done" status
   - Update multiple deliverables at once
   - Prepare for sprint review and retrospective
   - Clean up sprint board status

### Release Management
1. **Release Preparation**:
   - Move all release-ready features to "Ready for Release"
   - Update multiple components simultaneously
   - Coordinate release workflow across teams
   - Prepare release notes and documentation

2. **Post-Release Cleanup**:
   - Move deployed features to "Released" status
   - Update bug fixes to "Resolved" status
   - Clean up release board and tracking
   - Archive completed release items

### Quality Assurance
1. **Testing Phases**:
   - Move multiple items to "Testing" status
   - Coordinate QA workflow transitions
   - Track testing progress across features
   - Manage test execution phases

2. **Bug Triage**:
   - Move triaged bugs to "Confirmed" status
   - Update multiple bug reports simultaneously
   - Coordinate bug fix workflows
   - Track resolution progress

### Project Cleanup
1. **Workflow Standardization**:
   - Align multiple issues to correct workflow states
   - Clean up inconsistent status assignments
   - Standardize project workflow compliance
   - Prepare for project audits

2. **Bulk Corrections**:
   - Fix incorrectly assigned statuses
   - Update legacy issues to current workflow
   - Correct workflow violations
   - Maintain data consistency

## Related Catalog Requests

- [Update an Issue](pages/UpdateAnIssue.md) - Update individual issues with more fields
- [Get Issues](pages/GetIssues.md) - Find issues for bulk status updates
- [Get User Issues](pages/GetUserIssues.md) - Get user's issues for status updates
- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Find issues using complex criteria

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Issue key validation → Issue existence check → Workflow validation → Permission verification → Bulk update execution
- **Service Delegation**: Delegates to JIRA REST API v3 bulk update endpoints
- **Telemetry Metrics**: Tracks bulk update success rates, batch sizes, and performance metrics

## Troubleshooting

### Partial Update Success
**Problem**: Some issues update successfully while others fail
**Solution**:
1. Review failed issues list for specific error reasons
2. Address individual issue problems (permissions, workflow, etc.)
3. Retry failed issues separately after resolving issues
4. Consider smaller batch sizes to isolate problems

### Workflow Transition Errors
**Problem**: Status transitions are rejected for some issues
**Solution**:
1. Check workflow configuration for each issue type
2. Verify target status is available for all issue types
3. Ensure required fields are not needed for transition
4. Contact JIRA administrator for workflow clarification

### Permission Denied Errors
**Problem**: Cannot update some issues due to permission restrictions
**Solution**:
1. Verify edit permissions on all specified issues
2. Check project-level permissions for each issue
3. Ensure issues are not in restricted statuses
4. Contact JIRA administrator for permission review

### Performance Issues
**Problem**: Bulk updates take too long or timeout
**Solution**:
1. Reduce batch size to improve performance
2. Break large updates into smaller chunks
3. Check JIRA server performance and load
4. Consider off-peak hours for large bulk operations

## See Also

- [Update an Issue](pages/UpdateAnIssue.md) - Individual issue updates
- [Get Issues](pages/GetIssues.md) - Find issues for bulk operations
- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Advanced issue selection
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
