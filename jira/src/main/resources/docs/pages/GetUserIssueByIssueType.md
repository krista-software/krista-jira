# Get User Issue by Issue Type

## Overview

Retrieve issues assigned to a specific user filtered by issue type, enabling focused analysis of user workload by work category.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Account Id | Text | Yes | Unique account ID of the user whose issues you want to retrieve | `5d7c8a9b1234567890abcdef` |
| Issue Type | Text | Yes | Type of issues to filter by | `Bug`, `Story`, `Task`, `Epic` |
| Page Size | Number | No | Number of issues to return per page (default: 20, max: 100) | `50` |
| Page Number | Number | No | Page number to retrieve (default: 1) | `2` |

### Parameter Details

**Account Id**
- Must be a valid JIRA account ID (24-character alphanumeric string)
- Can be obtained using [Get Users Account Id](pages/GetUsersAccountId.md) request
- User must exist and be accessible to the requesting user
- Use "currentUser()" to get issues for the authenticated user

**Issue Type**
- Must be a valid issue type configured in JIRA
- Case-sensitive matching with JIRA issue type configuration
- Common types: Bug, Story, Task, Epic, Sub-task
- Must exist in projects accessible to the user

**Page Size**
- Controls number of issues returned per request
- Range: 1-100 issues per page
- Default: 20 issues
- Use smaller values for better performance

**Page Number**
- Specifies which page of results to retrieve
- Starts from 1 (first page)
- Use for pagination through large result sets

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| List of User Issues by Type | [ Entity(Issue) ] | Array of user's issues matching the specified type | Array of Issue objects |

### Issue Entity Structure

Each Issue entity contains:
- `id`: Internal JIRA issue ID
- `key`: Human-readable issue key
- `summary`: Issue title/summary
- `description`: Issue description
- `status`: Current issue status
- `priority`: Issue priority level
- `assigneeName`: Name of assigned user
- `reporterName`: Name of reporting user
- `projectKey`: Project key
- `issueType`: Type of issue (matches filter)
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
| Issue type valid | The specified status is not valid for this issue type or project. Please select a valid status from the available options. | Use valid issue type name as configured in JIRA |
| Page parameters | The field value is invalid. Please check the format and requirements for this field. | Use positive numbers for page size (1-100) and page number |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide account ID and issue type |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid parameters
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty account ID/issue type, invalid page parameters
- **Resolution**: Provide valid account ID, issue type, and page parameters

### Logic Errors (LOGIC_ERROR)
- **Cause**: User not found, invalid issue type, or permission issues
- **Error Message**: "The specified user could not be found in JIRA. Please verify the username or email address is correct."
- **Common Scenarios**: Invalid account ID, non-existent issue type, permission restrictions
- **Resolution**: Verify user existence and issue type validity

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or query processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again."
- **Common Scenarios**: Network issues, JIRA server problems, query timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "You don't have sufficient permissions to perform this action in JIRA."
- **Common Scenarios**: Token expiration, missing browse permissions
- **Resolution**: Re-authenticate and verify permissions

## Usage Examples

### Example 1: Get User's Bug Assignments
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`
- Issue Type: `Bug`
- Page Size: `25`

**Expected Output:**
```json
{
  "List of User Issues by Type": [
    {
      "id": "10001",
      "key": "DEMO-123",
      "summary": "Fix login authentication bug",
      "status": "In Progress",
      "priority": "High",
      "assigneeName": "John Doe",
      "issueType": "Bug",
      "projectKey": "DEMO"
    },
    {
      "id": "10002",
      "key": "PROJ-456",
      "summary": "Database connection timeout",
      "status": "To Do",
      "priority": "Medium",
      "assigneeName": "John Doe",
      "issueType": "Bug",
      "projectKey": "PROJ"
    }
  ]
}
```

### Example 2: Get User's Story Assignments
**Input:**
- Account Id: `currentUser()`
- Issue Type: `Story`
- Page Size: `50`

**Expected Output:**
```json
{
  "List of User Issues by Type": [
    {
      "id": "10003",
      "key": "EPIC-789",
      "summary": "Implement user dashboard",
      "status": "Code Review",
      "priority": "Medium",
      "assigneeName": "Current User",
      "issueType": "Story",
      "projectKey": "EPIC"
    }
  ]
}
```

### Example 3: No Issues Found
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`
- Issue Type: `Epic`

**Expected Output:**
```json
{
  "List of User Issues by Type": []
}
```

**Result**: User has no Epic assignments

## Business Rules

1. **Type Filtering**: Only returns issues of the specified type assigned to the user
2. **Assignment Filter**: Only includes issues where user is current assignee
3. **Permission Filtering**: Results limited by requesting user's project access
4. **Real-time Data**: Results reflect current assignments at query time
5. **Case Sensitivity**: Issue type names must match exactly as configured

## Limitations

- **Single Type Filter**: Can only filter by one issue type per request
- **Assignment Only**: Only shows assigned issues, not reported issues
- **Current Assignments**: Historical assignments not included
- **Permission Dependent**: Results limited by user permissions
- **Single User**: Only one user's issues per request

## Best Practices

1. **Type Verification**: Verify exact issue type names in JIRA configuration
2. **Workload Analysis**: Use for analyzing user workload distribution by type
3. **Capacity Planning**: Monitor user assignments by issue type for planning
4. **Performance**: Use appropriate page sizes for optimal performance
5. **Regular Monitoring**: Track user workload patterns by issue type

## Common Use Cases

### Workload Analysis
1. **Bug Assignment Tracking**:
   - Monitor user's bug assignments
   - Track bug resolution progress
   - Balance bug fixing workload
   - Identify bug resolution patterns

2. **Story Development Tracking**:
   - Monitor user's story assignments
   - Track feature development progress
   - Plan story completion timelines
   - Coordinate story dependencies

### Team Management
1. **Specialization Tracking**:
   - Monitor team members' work by type
   - Identify specialization patterns
   - Plan skill development needs
   - Balance work type distribution

2. **Capacity Planning**:
   - Analyze team capacity by issue type
   - Plan resource allocation by work type
   - Identify bottlenecks in specific types
   - Optimize team structure

### Performance Analysis
1. **Individual Productivity**:
   - Track completion rates by issue type
   - Analyze user efficiency patterns
   - Identify areas for improvement
   - Generate performance reports

2. **Quality Metrics**:
   - Monitor bug assignment patterns
   - Track defect resolution rates
   - Analyze quality trends by assignee
   - Identify training needs

## Related Catalog Requests

- [Get User Issues](pages/GetUserIssues.md) - Get all user issues without type filtering
- [Get My Issues by Given Statuses](pages/GetMyIssuesByGivenStatuses.md) - Filter by status instead
- [Get Issues](pages/GetIssues.md) - More flexible issue filtering options
- [Update an Issue](pages/UpdateAnIssue.md) - Update retrieved issues

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Account ID validation → Issue type validation → User existence check → Issue retrieval with filters → Response formatting
- **Service Delegation**: Delegates to JIRA REST API v3 search endpoints with assignee and type filters
- **Telemetry Metrics**: Tracks query performance, type distribution patterns, and user workload analytics

## Troubleshooting

### No Issues Returned
**Problem**: Query returns empty array when issues expected
**Solution**:
1. Verify user has assigned issues of the specified type
2. Check issue type name spelling and case
3. Ensure requesting user has permission to view target user's issues
4. Verify issues exist in accessible projects

### Invalid Issue Type Errors
**Problem**: Issue type validation fails
**Solution**:
1. Check exact spelling and capitalization of issue type
2. Verify issue type exists in JIRA configuration
3. Use JIRA interface to confirm available issue types
4. Contact JIRA administrator for issue type configuration

### Permission Issues
**Problem**: Cannot retrieve user's issues by type
**Solution**:
1. Contact JIRA administrator for appropriate browse permissions
2. Verify access to projects where user has assignments
3. Check organizational policies for user data access
4. Ensure proper authentication

## See Also

- [Get User Issues](pages/GetUserIssues.md) - Get all user issues
- [Get My Issues by Given Statuses](pages/GetMyIssuesByGivenStatuses.md) - Filter by status
- [Get Users Account Id](pages/GetUsersAccountId.md) - Find user account IDs
- [Get Issues](pages/GetIssues.md) - Advanced issue filtering
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
