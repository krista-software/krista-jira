# Get User Issues

## Overview

Retrieve all issues assigned to a specific user, providing comprehensive visibility into their current workload and responsibilities.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Account Id | Text | Yes | Unique account ID of the user whose issues you want to retrieve | `5d7c8a9b1234567890abcdef` |
| Page Size | Number | No | Number of issues to return per page (default: 20, max: 100) | `50` |
| Page Number | Number | No | Page number to retrieve (default: 1) | `2` |

### Parameter Details

**Account Id**
- Must be a valid JIRA account ID (24-character alphanumeric string)
- Can be obtained using [Get Users Account Id](pages/GetUsersAccountId.md) request
- User must exist and be accessible to the requesting user
- Use "currentUser()" to get issues for the authenticated user

**Page Size**
- Controls number of issues returned per request
- Range: 1-100 issues per page
- Default: 20 issues
- Use smaller values for better performance with large datasets

**Page Number**
- Specifies which page of results to retrieve
- Starts from 1 (first page)
- Use for pagination through large result sets
- Returns empty array if page number exceeds available pages

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| List of User Issues | [ Entity(Issue) ] | Array of issues assigned to the specified user | Array of Issue objects |

### Issue Entity Structure

Each Issue entity contains:
- `id`: Internal JIRA issue ID
- `key`: Human-readable issue key (e.g., DEMO-123)
- `summary`: Issue title/summary
- `description`: Issue description
- `status`: Current issue status
- `priority`: Issue priority level
- `assigneeName`: Name of assigned user (should match requested user)
- `reporterName`: Name of reporting user
- `projectKey`: Project key
- `issueType`: Type of issue (Bug, Story, Task, etc.)
- `created`: Creation timestamp
- `updated`: Last update timestamp
- `dueDate`: Due date if set
- `labels`: Issue labels
- `components`: Project components

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Account ID format | The field value is invalid. Please check the format and requirements for this field. | Use valid 24-character JIRA account ID |
| User exists | The specified user could not be found in JIRA. Please verify the username or email address is correct. | Verify account ID exists and is active |
| Page parameters | The field value is invalid. Please check the format and requirements for this field. | Use positive numbers for page size (1-100) and page number |
| User access | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request permissions to view user's issues |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing account ID or invalid page parameters
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty account ID, invalid page size/number
- **Resolution**: Provide valid account ID and page parameters

### Logic Errors (LOGIC_ERROR)
- **Cause**: User not found or insufficient permissions to view user's issues
- **Error Message**: "The specified user could not be found in JIRA. Please verify the username or email address is correct."
- **Common Scenarios**: Invalid account ID, user doesn't exist, permission restrictions
- **Resolution**: Verify user existence and check permissions

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or query processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, query timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions."
- **Common Scenarios**: Token expiration, missing browse permissions
- **Resolution**: Re-authenticate and verify permissions

## Usage Examples

### Example 1: Get Current User's Issues
**Input:**
- Account Id: `currentUser()`
- Page Size: `25`

**Expected Output:**
```json
{
  "List of User Issues": [
    {
      "id": "10001",
      "key": "DEMO-123",
      "summary": "Fix login authentication bug",
      "status": "In Progress",
      "priority": "High",
      "assigneeName": "John Doe",
      "projectKey": "DEMO",
      "issueType": "Bug",
      "dueDate": "2025-01-20"
    },
    {
      "id": "10002",
      "key": "PROJ-456",
      "summary": "Implement user dashboard",
      "status": "To Do",
      "priority": "Medium",
      "assigneeName": "John Doe",
      "projectKey": "PROJ",
      "issueType": "Story"
    }
  ]
}
```

### Example 2: Get Specific User's Issues
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`
- Page Size: `50`
- Page Number: `1`

**Expected Output:**
```json
{
  "List of User Issues": [
    {
      "id": "10003",
      "key": "TEST-789",
      "summary": "Create automated test suite",
      "status": "Code Review",
      "priority": "Medium",
      "assigneeName": "Jane Smith",
      "projectKey": "TEST",
      "issueType": "Task"
    }
  ]
}
```

### Example 3: Paginated Results
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`
- Page Size: `10`
- Page Number: `3`

**Expected Output:**
```json
{
  "List of User Issues": [
    {
      "id": "10004",
      "key": "DEMO-101",
      "summary": "Update documentation",
      "status": "To Do",
      "priority": "Low",
      "assigneeName": "Jane Smith",
      "projectKey": "DEMO",
      "issueType": "Task"
    }
  ]
}
```

### Example 4: No Issues Found
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`

**Expected Output:**
```json
{
  "List of User Issues": []
}
```

**Result**: User has no assigned issues

## Business Rules

1. **Assignment Filter**: Only returns issues where the user is the current assignee
2. **Permission Filtering**: Results limited by requesting user's project access permissions
3. **Status Independence**: Returns issues in all statuses (open, in progress, done, etc.)
4. **Project Scope**: Includes issues from all projects the user has access to view
5. **Real-time Data**: Results reflect current assignments at query time

## Limitations

- **Assignment Only**: Only shows issues assigned to the user, not reported by them
- **Current Assignments**: Historical assignments are not included
- **Permission Dependent**: Results limited by requesting user's permissions
- **No Status Filtering**: Cannot filter by specific statuses in this request
- **Single User**: Only one user's issues can be retrieved per request

## Best Practices

1. **Pagination Strategy**: Use appropriate page sizes (20-50) for optimal performance
2. **Regular Monitoring**: Check user workloads regularly for capacity planning
3. **Permission Management**: Ensure appropriate permissions for team visibility
4. **Workload Analysis**: Use results for workload balancing and resource planning
5. **Performance Optimization**: Use pagination for users with many assigned issues
6. **Data Freshness**: Results are real-time, consider caching for frequent queries

## Common Use Cases

### Team Workload Management
1. **Daily Standup Preparation**:
   - Retrieve each team member's assigned issues
   - Review progress and identify blockers
   - Plan daily work priorities
   - Identify overloaded team members

2. **Sprint Planning**:
   - Check current assignments before adding new work
   - Balance workload across team members
   - Identify capacity for new sprint items
   - Plan resource allocation

### Individual Productivity Tracking
1. **Personal Task Management**:
   - Get current user's issues for daily planning
   - Prioritize work based on due dates and priority
   - Track progress on assigned work
   - Identify overdue or high-priority items

2. **Performance Reviews**:
   - Analyze individual workload and completion rates
   - Review issue types and complexity
   - Track assignment patterns over time
   - Generate productivity reports

### Project Management
1. **Resource Allocation**:
   - Monitor team member assignments across projects
   - Identify resource bottlenecks
   - Plan project timelines based on current workload
   - Balance work distribution

2. **Progress Tracking**:
   - Monitor individual contributions to project goals
   - Track issue completion rates by assignee
   - Identify team members needing support
   - Generate project status reports

### Manager Oversight
1. **Team Monitoring**:
   - Review direct reports' current assignments
   - Identify overloaded or underutilized team members
   - Plan work redistribution if needed
   - Support team members with heavy workloads

2. **Capacity Planning**:
   - Assess team capacity for new projects
   - Plan hiring needs based on workload analysis
   - Identify skill gaps in current assignments
   - Optimize team structure and responsibilities

## Related Catalog Requests

- [Get Issues](pages/GetIssues.md) - More flexible issue filtering options
- [Get My Issues by Given Statuses](pages/GetMyIssuesByGivenStatuses.md) - Filter by specific statuses
- [Get User Issue by Issue Type](pages/GetUserIssueByIssueType.md) - Filter by issue types
- [Update an Issue](pages/UpdateAnIssue.md) - Update retrieved issues
- [Get Users Account Id](pages/GetUsersAccountId.md) - Find account IDs for user lookup

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Account ID validation → User existence check → Permission verification → Issue retrieval → Response formatting
- **Service Delegation**: Delegates to JIRA REST API v3 search endpoints with assignee filter
- **Telemetry Metrics**: Tracks query performance, user workload patterns, and access frequency

## Troubleshooting

### No Issues Returned
**Problem**: Query returns empty array when issues are expected
**Solution**:
1. Verify user has assigned issues in JIRA
2. Check if requesting user has permission to view target user's issues
3. Ensure account ID is correct and user exists
4. Verify user's issues are in projects you can access

### Permission Denied Errors
**Problem**: Cannot retrieve user's issues due to permission restrictions
**Solution**:
1. Contact JIRA administrator to grant appropriate browse permissions
2. Verify you have access to projects where user has assignments
3. Check organizational policies for user data access
4. Ensure you're authenticated properly

### Performance Issues
**Problem**: Query takes too long or times out
**Solution**:
1. Use smaller page sizes to improve response time
2. Implement pagination for users with many assignments
3. Check JIRA server performance and load
4. Consider caching results for frequently accessed users

### Invalid Account ID Errors
**Problem**: Account ID format is rejected or user not found
**Solution**:
1. Verify account ID is 24-character alphanumeric string
2. Use [Get Users Account Id](pages/GetUsersAccountId.md) to find correct ID
3. Check if user account is active and not suspended
4. Ensure user exists in the JIRA instance

## See Also

- [Get Issues](pages/GetIssues.md) - Advanced issue filtering and search
- [Get My Issues by Given Statuses](pages/GetMyIssuesByGivenStatuses.md) - Status-based filtering
- [Get Users Account Id](pages/GetUsersAccountId.md) - Find user account IDs
- [Update an Issue](pages/UpdateAnIssue.md) - Update user's assigned issues
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
