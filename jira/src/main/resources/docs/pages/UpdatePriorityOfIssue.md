# Update Priority of Issue

## Overview

Change the priority level of a specific JIRA issue to reflect its importance and urgency in the project workflow.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Issue Key | Text | Yes | Key of the issue whose priority you want to update | `DEMO-123` |
| Priority | Text | Yes | New priority level to apply to the issue | `High`, `Critical`, `Medium`, `Low` |

### Parameter Details

**Issue Key**
- Must be a valid JIRA issue key in PROJECT-NUMBER format
- Issue must exist and be accessible to the requesting user
- User must have edit permissions on the issue
- Case-sensitive (typically uppercase project key)

**Priority**
- Must be a valid priority level configured in JIRA
- Case-sensitive matching with configured priority names
- Common priorities: Blocker, Critical, High, Medium, Low
- Must exist in the project's priority scheme

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Updated Issue | Entity(Issue) | Complete details of the issue with updated priority | Issue object with new priority and update timestamp |

### Issue Entity Structure

The returned Issue entity contains:
- `id`: Internal JIRA issue ID
- `key`: Issue key (unchanged)
- `summary`: Issue title/summary
- `description`: Issue description
- `status`: Current issue status
- `priority`: New priority level after update
- `assigneeName`: Name of assigned user
- `reporterName`: Name of reporting user
- `projectKey`: Project key
- `issueType`: Issue type
- `updated`: Timestamp of the priority update

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Issue exists | The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue. | Verify issue key and access permissions |
| Priority valid | The specified status is not valid for this issue type or project. Please select a valid status from the available options. | Use valid priority level configured in JIRA |
| Edit permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request edit permissions for the issue |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide both issue key and priority level |
| Priority exists | The specified priority level does not exist in your JIRA configuration. | Use priority level from project's priority scheme |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid issue key format
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty issue key or priority, malformed key format
- **Resolution**: Provide valid issue key and priority level

### Logic Errors (LOGIC_ERROR)
- **Cause**: Issue not found, invalid priority, or permission issues
- **Error Message**: "The specified status is not valid for this issue type or project. Please select a valid status from the available options."
- **Common Scenarios**: Non-existent priority, insufficient permissions, invalid issue key
- **Resolution**: Verify priority exists and check permissions

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or priority update processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "Your JIRA session has expired. Please re-authorize your account by clicking the 'Authorize' button and signing in again."
- **Common Scenarios**: Token expiration, missing edit permissions
- **Resolution**: Re-authenticate and verify permissions

## Usage Examples

### Example 1: Escalate to High Priority
**Input:**
- Issue Key: `DEMO-123`
- Priority: `High`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "10001",
    "key": "DEMO-123",
    "summary": "Fix login authentication bug",
    "priority": "High",
    "updated": "2025-01-15T14:30:00.000Z"
  }
}
```

### Example 2: Set Critical Priority
**Input:**
- Issue Key: `PROD-456`
- Priority: `Critical`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "10002",
    "key": "PROD-456",
    "summary": "Database connection failure",
    "priority": "Critical",
    "updated": "2025-01-15T14:35:00.000Z"
  }
}
```

### Example 3: Lower Priority
**Input:**
- Issue Key: `FEAT-789`
- Priority: `Low`

**Expected Output:**
```json
{
  "Updated Issue": {
    "id": "10003",
    "key": "FEAT-789",
    "summary": "Add nice-to-have feature",
    "priority": "Low",
    "updated": "2025-01-15T14:40:00.000Z"
  }
}
```

## Business Rules

1. **Priority Scheme**: Priority levels must exist in the project's priority scheme
2. **Permission Requirements**: User must have edit permissions on the issue
3. **Audit Trail**: Priority changes are logged in issue history
4. **Single Issue**: Only one issue can be updated per request
5. **Immediate Effect**: Priority changes take effect immediately

## Limitations

- **Single Issue Only**: Cannot update multiple issues in one request
- **Priority Scheme Dependent**: Limited to priorities configured in project
- **Permission Dependent**: Update limited by user's permissions on the issue
- **Priority Only**: This request only updates priority, not other fields
- **No Bulk Operations**: Each issue requires separate request

## Best Practices

1. **Priority Understanding**: Understand your organization's priority definitions
2. **Permission Verification**: Ensure edit permissions before attempting updates
3. **Priority Validation**: Verify priority names match JIRA configuration exactly
4. **Impact Assessment**: Consider impact of priority changes on team workflow
5. **Documentation**: Document reasons for priority changes
6. **Stakeholder Communication**: Notify relevant stakeholders of priority changes

## Common Use Cases

### Issue Escalation
1. **Production Issues**: Escalate production bugs to Critical or High priority
2. **Customer Impact**: Increase priority for issues affecting customers
3. **Deadline Pressure**: Raise priority for issues blocking releases
4. **Resource Allocation**: Adjust priorities to guide team focus

### Priority Balancing
1. **Workload Management**: Balance team workload by adjusting priorities
2. **Sprint Planning**: Set appropriate priorities for sprint items
3. **Resource Optimization**: Align priorities with available resources
4. **Stakeholder Expectations**: Match priorities to business expectations

### Quality Management
1. **Bug Triage**: Set appropriate priorities during bug triage
2. **Risk Assessment**: Adjust priorities based on risk analysis
3. **Impact Analysis**: Set priorities based on business impact
4. **Technical Debt**: Prioritize technical debt items appropriately

### Project Management
1. **Milestone Planning**: Adjust priorities for milestone deliverables
2. **Dependency Management**: Prioritize issues based on dependencies
3. **Critical Path**: Increase priority for critical path items
4. **Release Planning**: Set priorities for release-specific items

## Related Catalog Requests

- [Update an Issue](pages/UpdateAnIssue.md) - Update multiple issue fields including priority
- [Update the Priority of Multiple Issues](pages/UpdateThePriorityOfMultipleIssues.md) - Bulk priority updates
- [Get Issue By Key](pages/GetIssueByKey.md) - Check current priority before updating
- [Get Issues](pages/GetIssues.md) - Find issues for priority updates

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Issue key validation → Issue existence check → Priority validation → Permission verification → Priority update
- **Service Delegation**: Delegates to JIRA REST API v3 issue update endpoints
- **Telemetry Metrics**: Tracks priority update success rates, priority distribution, and usage patterns

## Troubleshooting

### Priority Not Found Errors
**Problem**: Priority level is not recognized
**Solution**:
1. Check exact spelling and capitalization of priority name
2. Verify priority exists in the project's priority scheme
3. Use JIRA interface to confirm available priorities
4. Contact JIRA administrator for priority configuration

### Permission Denied Errors
**Problem**: User cannot update issue priority
**Solution**:
1. Verify user has edit permissions on the issue
2. Check project-level permissions
3. Ensure issue is not in a restricted status
4. Contact JIRA administrator for permission review

### Invalid Issue Key Errors
**Problem**: Issue key is not recognized
**Solution**:
1. Verify issue key format (PROJECT-NUMBER)
2. Check if issue exists in JIRA
3. Ensure user has access to the issue
4. Verify issue hasn't been deleted or moved

### Priority Scheme Issues
**Problem**: Priority not available for specific issue type
**Solution**:
1. Check if priority is available for the issue type
2. Verify project priority scheme configuration
3. Contact JIRA administrator about priority scheme
4. Use alternative priority levels if available

## See Also

- [Update an Issue](pages/UpdateAnIssue.md) - Comprehensive issue updates
- [Update the Priority of Multiple Issues](pages/UpdateThePriorityOfMultipleIssues.md) - Bulk priority updates
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve issue details
- [Get Issues](pages/GetIssues.md) - Find issues by priority
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
