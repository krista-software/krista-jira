# Update the Priority of Multiple Issues

## Overview

Change the priority level for multiple JIRA issues simultaneously to efficiently manage workload and project priorities.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Issue Keys | Text | Yes | Comma-separated list of issue keys to update | `DEMO-123,PROJ-456,TEST-789` |
| Priority | Text | Yes | New priority level to apply to all specified issues | `High`, `Critical`, `Medium`, `Low` |

### Parameter Details

**Issue Keys**
- Comma-separated list of valid JIRA issue keys
- Each key must follow PROJECT-NUMBER format (e.g., DEMO-123)
- All issues must exist and be accessible to the user
- Maximum recommended: 50 issues per request for optimal performance
- Duplicate keys are automatically removed

**Priority**
- Must be a valid priority level configured in JIRA
- Case-sensitive matching with configured priority names
- Common priorities: Blocker, Critical, High, Medium, Low
- All issues will receive the same priority level

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| List of Issues | [ Entity(Issue) ] | Array of successfully updated issues with their new priority | Array of Issue objects |

### Issue Entity Structure

Each Issue entity contains:
- `id`: Internal JIRA issue ID
- `key`: Human-readable issue key
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
| Issue keys format | The following issue keys are not valid in your JIRA workflow: KEYS. Please use only issue keys that exist in your JIRA project's workflow. | Use valid PROJECT-NUMBER format for all issue keys |
| Issues exist | The following issue keys could not be found in JIRA: KEYS. Please verify that the issue keys are spelled correctly and are accessible with your current permissions. | Verify all issue keys exist and are accessible |
| Priority valid | The specified status is not valid for this issue type or project. Please select a valid status from the available options. | Use valid priority level configured in JIRA |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide both issue keys and priority level |
| Permission check | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request edit permissions for all specified issues |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid issue key formats
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty issue keys, missing priority, malformed issue keys
- **Resolution**: Provide valid issue keys and priority level

### Logic Errors (LOGIC_ERROR)
- **Cause**: Issues not found, invalid priority, or permission issues
- **Error Message**: "Some issue keys in your list are invalid or cannot be accessed. Please verify that all issue keys exist in your JIRA instance and follow the correct format (PROJECT-NUMBER)."
- **Common Scenarios**: Non-existent issues, invalid priority, insufficient permissions
- **Resolution**: Verify issue existence and priority validity

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

### Example 1: Escalate Multiple Issues to High Priority
**Input:**
- Issue Keys: `DEMO-123,DEMO-124,DEMO-125`
- Priority: `High`

**Expected Output:**
```json
{
  "List of Issues": [
    {
      "id": "10001",
      "key": "DEMO-123",
      "summary": "Fix login bug",
      "priority": "High",
      "updated": "2025-01-15T14:30:00.000Z"
    },
    {
      "id": "10002",
      "key": "DEMO-124",
      "summary": "Update documentation",
      "priority": "High",
      "updated": "2025-01-15T14:30:00.000Z"
    },
    {
      "id": "10003",
      "key": "DEMO-125",
      "summary": "Performance optimization",
      "priority": "High",
      "updated": "2025-01-15T14:30:00.000Z"
    }
  ]
}
```

### Example 2: Set Critical Priority for Production Issues
**Input:**
- Issue Keys: `PROD-456,PROD-457,PROD-458`
- Priority: `Critical`

**Expected Output:**
```json
{
  "List of Issues": [
    {
      "id": "10004",
      "key": "PROD-456",
      "summary": "Database connection failure",
      "priority": "Critical",
      "updated": "2025-01-15T14:35:00.000Z"
    },
    {
      "id": "10005",
      "key": "PROD-457",
      "summary": "Payment processing error",
      "priority": "Critical",
      "updated": "2025-01-15T14:35:00.000Z"
    },
    {
      "id": "10006",
      "key": "PROD-458",
      "summary": "User authentication failure",
      "priority": "Critical",
      "updated": "2025-01-15T14:35:00.000Z"
    }
  ]
}
```

### Example 3: Lower Priority for Non-Critical Items
**Input:**
- Issue Keys: `FEAT-789,FEAT-790`
- Priority: `Low`

**Expected Output:**
```json
{
  "List of Issues": [
    {
      "id": "10007",
      "key": "FEAT-789",
      "summary": "Add nice-to-have feature",
      "priority": "Low",
      "updated": "2025-01-15T14:40:00.000Z"
    },
    {
      "id": "10008",
      "key": "FEAT-790",
      "summary": "UI enhancement",
      "priority": "Low",
      "updated": "2025-01-15T14:40:00.000Z"
    }
  ]
}
```

## Business Rules

1. **Uniform Priority**: All specified issues receive the same priority level
2. **Atomic Operations**: Each issue is updated independently - partial success is possible
3. **Permission Requirements**: User must have edit permissions on all specified issues
4. **Audit Trail**: All priority changes are logged in issue history
5. **Validation Order**: Issues are validated before any updates are attempted

## Limitations

- **Batch Size**: Recommended maximum of 50 issues per request for optimal performance
- **Single Priority**: All issues receive the same priority - cannot set different priorities per issue
- **Permission Dependent**: Update limited by user's permissions on each issue
- **Priority Only**: This request only updates priority, not other fields
- **Performance Impact**: Large batches may impact JIRA server performance

## Best Practices

1. **Batch Sizing**: Use reasonable batch sizes (10-50 issues) to balance efficiency and performance
2. **Priority Validation**: Verify priority level exists in all target projects before bulk update
3. **Permission Verification**: Ensure edit permissions on all issues before attempting bulk update
4. **Impact Assessment**: Consider impact of priority changes on team workflow and planning
5. **Documentation**: Document reasons for bulk priority changes for audit purposes
6. **Stakeholder Communication**: Notify relevant stakeholders of bulk priority changes

## Common Use Cases

### Release Management
1. **Release Preparation**:
   - Escalate all release-critical items to High priority
   - Set Medium priority for nice-to-have features
   - Lower priority for post-release items
   - Coordinate priority alignment across teams

2. **Release Blocking Issues**:
   - Set Critical priority for all release blockers
   - Escalate related dependencies to High priority
   - Ensure proper priority hierarchy for resolution order

### Sprint Planning
1. **Sprint Prioritization**:
   - Set High priority for sprint commitment items
   - Medium priority for stretch goals
   - Low priority for backlog items
   - Align team focus through priority setting

2. **Mid-Sprint Adjustments**:
   - Escalate newly discovered critical issues
   - Adjust priorities based on sprint progress
   - Rebalance workload through priority changes

### Incident Response
1. **Production Incidents**:
   - Set Critical priority for all incident-related issues
   - Escalate supporting tasks to High priority
   - Coordinate incident response through priority alignment
   - Ensure proper escalation hierarchy

2. **Post-Incident Cleanup**:
   - Adjust priorities after incident resolution
   - Set appropriate priorities for follow-up tasks
   - Balance incident work with regular development

### Project Management
1. **Milestone Management**:
   - Escalate milestone-critical items
   - Adjust priorities based on milestone proximity
   - Coordinate cross-team priority alignment
   - Ensure milestone delivery focus

2. **Resource Reallocation**:
   - Adjust priorities based on resource availability
   - Balance workload across team members
   - Optimize team productivity through priority management

## Related Catalog Requests

- [Update Priority of Issue](pages/UpdatePriorityOfIssue.md) - Update individual issue priority
- [Update an Issue](pages/UpdateAnIssue.md) - Update multiple fields including priority
- [Update Status of Multiple Issues](pages/UpdateStatusOfMultipleIssues.md) - Bulk status updates
- [Get Issues](pages/GetIssues.md) - Find issues for priority updates

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Issue key validation → Issue existence check → Priority validation → Permission verification → Bulk priority update
- **Service Delegation**: Delegates to JIRA REST API v3 bulk update endpoints
- **Telemetry Metrics**: Tracks bulk priority update success rates, batch sizes, and priority distribution patterns

## Troubleshooting

### Partial Update Success
**Problem**: Some issues update successfully while others fail
**Solution**:
1. Check which issues failed and why
2. Verify permissions on failed issues
3. Ensure priority is valid for all issue types
4. Retry failed issues separately after resolving issues

### Priority Not Available
**Problem**: Priority level is not available for some issues
**Solution**:
1. Check if priority exists in all target projects
2. Verify priority scheme configuration across projects
3. Use priority levels available in all target projects
4. Contact JIRA administrator for priority scheme alignment

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

- [Update Priority of Issue](pages/UpdatePriorityOfIssue.md) - Individual priority updates
- [Update an Issue](pages/UpdateAnIssue.md) - Comprehensive issue updates
- [Get Issues](pages/GetIssues.md) - Find issues for bulk operations
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
