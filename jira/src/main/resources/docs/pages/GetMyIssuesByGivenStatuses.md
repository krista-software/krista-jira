# Get My Issues by Given Statuses

## Overview

Retrieve issues assigned to the current user filtered by specific status values, enabling focused personal task management and workflow tracking.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Status Names | Text | Yes | Comma-separated list of status names to filter by | `To Do,In Progress,Code Review` |
| Page Size | Number | No | Number of issues to return per page (default: 20, max: 100) | `50` |
| Page Number | Number | No | Page number to retrieve (default: 1) | `2` |

### Parameter Details

**Status Names**
- Comma-separated list of exact status names as configured in JIRA
- Case-sensitive matching with JIRA status configuration
- Must be valid statuses that exist in your JIRA instance
- Common statuses: "To Do", "In Progress", "Done", "Code Review", "Testing"
- Filters issues assigned to current user with any of the specified statuses

**Page Size**
- Controls number of issues returned per request
- Range: 1-100 issues per page
- Default: 20 issues
- Use smaller values for better performance with large result sets

**Page Number**
- Specifies which page of results to retrieve
- Starts from 1 (first page)
- Use for pagination through large result sets
- Returns empty array if page number exceeds available pages

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| List of My Issues | [ Entity(Issue) ] | Array of current user's issues matching the specified statuses | Array of Issue objects |

### Issue Entity Structure

Each Issue entity contains:
- `id`: Internal JIRA issue ID
- `key`: Human-readable issue key (e.g., DEMO-123)
- `summary`: Issue title/summary
- `description`: Issue description
- `status`: Current issue status (will match one of the requested statuses)
- `priority`: Issue priority level
- `assigneeName`: Current user's name
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
| Status names required | Required field is missing. Please provide a value for this field to continue. | Provide comma-separated list of status names |
| Status names valid | The following status names are not valid in your JIRA workflow: STATUS_NAMES. Please use only status names that exist in your JIRA project's workflow. | Use exact status names as configured in JIRA |
| Page parameters | The field value is invalid. Please check the format and requirements for this field. | Use positive numbers for page size (1-100) and page number |
| User authentication | Authentication is required to access JIRA. Please authorize your account by clicking the 'Authorize' button and signing in with your JIRA credentials. | Re-authenticate with JIRA |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing status names or invalid page parameters
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty status names, invalid page size/number
- **Resolution**: Provide valid status names and page parameters

### Logic Errors (LOGIC_ERROR)
- **Cause**: Invalid status names or no matching issues
- **Error Message**: "The following status names are not valid in your JIRA workflow: [status names]. Please use only status names that exist in your JIRA project's workflow."
- **Common Scenarios**: Misspelled status names, non-existent statuses
- **Resolution**: Use exact status names as configured in JIRA

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or query processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, query timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or session expiration
- **Error Message**: "Your JIRA session has expired. Please re-authorize your account by clicking the 'Authorize' button and signing in again."
- **Common Scenarios**: Token expiration, authentication issues
- **Resolution**: Re-authenticate with JIRA

## Usage Examples

### Example 1: Get Active Work Items
**Input:**
- Status Names: `In Progress,Code Review`
- Page Size: `25`

**Expected Output:**
```json
{
  "List of My Issues": [
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
      "status": "Code Review",
      "priority": "Medium",
      "assigneeName": "John Doe",
      "projectKey": "PROJ",
      "issueType": "Story"
    }
  ]
}
```

### Example 2: Get Pending Work
**Input:**
- Status Names: `To Do,Backlog`
- Page Size: `50`

**Expected Output:**
```json
{
  "List of My Issues": [
    {
      "id": "10003",
      "key": "TEST-789",
      "summary": "Create automated test suite",
      "status": "To Do",
      "priority": "Medium",
      "assigneeName": "John Doe",
      "projectKey": "TEST",
      "issueType": "Task"
    }
  ]
}
```

### Example 3: Get Completed Work
**Input:**
- Status Names: `Done,Resolved,Closed`
- Page Size: `20`
- Page Number: `1`

**Expected Output:**
```json
{
  "List of My Issues": [
    {
      "id": "10004",
      "key": "DEMO-101",
      "summary": "Update documentation",
      "status": "Done",
      "priority": "Low",
      "assigneeName": "John Doe",
      "projectKey": "DEMO",
      "issueType": "Task"
    }
  ]
}
```

### Example 4: No Matching Issues
**Input:**
- Status Names: `Testing,QA Review`

**Expected Output:**
```json
{
  "List of My Issues": []
}
```

**Result**: Current user has no issues in the specified statuses

## Business Rules

1. **Current User Only**: Only returns issues assigned to the authenticated user
2. **Status Filtering**: Issues must match at least one of the specified statuses
3. **Permission Filtering**: Results limited by user's project access permissions
4. **Real-time Data**: Results reflect current assignments and statuses at query time
5. **Case Sensitivity**: Status names must match exactly as configured in JIRA

## Limitations

- **Current User Only**: Cannot retrieve other users' issues with this request
- **Status Dependency**: Results depend on exact status name matching
- **No Cross-Field Filtering**: Cannot combine with other filters (priority, project, etc.)
- **Single User Scope**: Limited to current user's assignments only
- **Status Configuration**: Limited by available statuses in user's accessible projects

## Best Practices

1. **Status Verification**: Verify exact status names in your JIRA configuration before use
2. **Workflow Understanding**: Understand your project workflows to use appropriate status combinations
3. **Pagination Strategy**: Use appropriate page sizes for optimal performance
4. **Regular Monitoring**: Use for daily task management and progress tracking
5. **Status Grouping**: Group related statuses for meaningful workflow views
6. **Performance Optimization**: Use reasonable page sizes to avoid performance issues

## Common Use Cases

### Daily Task Management
1. **Morning Planning**:
   - Get issues in "To Do" status for daily planning
   - Review "In Progress" items for continuation
   - Check "Code Review" items for follow-up
   - Prioritize work based on due dates and priority

2. **Progress Tracking**:
   - Monitor active work items throughout the day
   - Track items moving through workflow stages
   - Identify blockers in specific statuses
   - Update status as work progresses

### Sprint Management
1. **Sprint Planning**:
   - Review "Backlog" and "To Do" items for sprint inclusion
   - Check capacity based on "In Progress" items
   - Plan work distribution across sprint duration
   - Identify dependencies and blockers

2. **Sprint Execution**:
   - Focus on "In Progress" items during development
   - Monitor "Code Review" items for completion
   - Track "Testing" items for QA coordination
   - Prepare "Done" items for sprint review

### Workflow Optimization
1. **Bottleneck Identification**:
   - Monitor items stuck in specific statuses
   - Identify workflow stages with high item counts
   - Analyze time spent in each status
   - Optimize personal workflow efficiency

2. **Process Improvement**:
   - Track status transition patterns
   - Identify frequently used status combinations
   - Optimize workflow for personal productivity
   - Coordinate with team on workflow improvements

### Reporting and Analytics
1. **Personal Productivity**:
   - Generate reports on work completion rates
   - Analyze time distribution across statuses
   - Track personal velocity and throughput
   - Identify areas for improvement

2. **Status Distribution Analysis**:
   - Monitor workload distribution across statuses
   - Identify overloaded workflow stages
   - Plan capacity and resource allocation
   - Generate status-based progress reports

## Related Catalog Requests

- [Get User Issues](pages/GetUserIssues.md) - Get all user issues without status filtering
- [Get Issues](pages/GetIssues.md) - More flexible issue filtering options
- [Update an Issue](pages/UpdateAnIssue.md) - Update status of retrieved issues
- [Update Status of Multiple Issues](pages/UpdateStatusOfMultipleIssues.md) - Bulk status updates

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Status name validation → Current user identification → Issue retrieval with status filter → Response formatting
- **Service Delegation**: Delegates to JIRA REST API v3 search endpoints with assignee and status filters
- **Telemetry Metrics**: Tracks query performance, status usage patterns, and user workflow analytics

## Troubleshooting

### No Issues Returned
**Problem**: Query returns empty array when issues are expected
**Solution**:
1. Verify you have issues assigned in the specified statuses
2. Check status names are spelled exactly as configured in JIRA
3. Ensure you're authenticated as the correct user
4. Verify issues exist in projects you have access to

### Invalid Status Names
**Problem**: Status validation fails with error messages
**Solution**:
1. Check exact spelling and capitalization of status names
2. Verify statuses exist in your JIRA configuration
3. Use JIRA interface to confirm available status names
4. Contact JIRA administrator for status configuration details

### Performance Issues
**Problem**: Query takes too long or times out
**Solution**:
1. Use smaller page sizes to improve response time
2. Limit number of status names in filter
3. Check JIRA server performance and load
4. Consider pagination for large result sets

### Authentication Errors
**Problem**: Cannot access personal issues due to authentication failures
**Solution**:
1. Re-authenticate with JIRA using extension configuration
2. Verify authentication credentials are current
3. Check if user account is active and not suspended
4. Ensure proper permissions for issue access

## See Also

- [Get User Issues](pages/GetUserIssues.md) - Get all user issues
- [Get Issues](pages/GetIssues.md) - Advanced issue filtering
- [Update an Issue](pages/UpdateAnIssue.md) - Update issue status
- [Update Status of Multiple Issues](pages/UpdateStatusOfMultipleIssues.md) - Bulk status updates
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
