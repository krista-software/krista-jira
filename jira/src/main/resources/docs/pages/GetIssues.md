# Get Issues

## Overview

Retrieve issues filtered by various criteria such as summary, status, priority, assignee, and project to find specific tickets efficiently.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Summary | Text | No | Filter issues by text in the summary/title (partial matches supported) | `login bug` |
| Status | Text | No | Filter issues by current status | `In Progress`, `Done`, `To Do` |
| Priority | Text | No | Filter issues by priority level | `High`, `Medium`, `Low` |
| Assignee | Text | No | Filter issues by assignee name or email | `john.doe@company.com` |
| Project Key | Text | No | Filter issues by project key | `DEMO`, `PROJ` |
| Issue Type | Text | No | Filter issues by issue type | `Bug`, `Story`, `Task` |
| Reporter | Text | No | Filter issues by reporter name or email | `jane.smith@company.com` |
| Labels | Text | No | Filter issues by labels (comma-separated) | `urgent,frontend` |
| Created Date From | Text | No | Filter issues created after this date (YYYY-MM-DD) | `2025-01-01` |
| Created Date To | Text | No | Filter issues created before this date (YYYY-MM-DD) | `2025-12-31` |
| Updated Date From | Text | No | Filter issues updated after this date (YYYY-MM-DD) | `2025-01-01` |
| Updated Date To | Text | No | Filter issues updated before this date (YYYY-MM-DD) | `2025-12-31` |
| Page Size | Number | No | Number of issues to return per page (default: 20, max: 100) | `50` |
| Page Number | Number | No | Page number to retrieve (default: 1) | `2` |

### Parameter Details

**Summary**
- Supports partial text matching (case-insensitive)
- Searches within issue titles/summaries
- Use keywords that appear in issue titles

**Status**
- Must match exact status names configured in JIRA
- Case-sensitive matching
- Common statuses: To Do, In Progress, Done, Closed

**Priority**
- Must match exact priority names configured in JIRA
- Case-sensitive matching
- Common priorities: Blocker, Critical, High, Medium, Low

**Assignee/Reporter**
- Can use email address or display name
- Partial name matching supported
- Leave empty to find unassigned issues (for assignee)

**Date Filters**
- Use YYYY-MM-DD format
- Created Date: When the issue was originally created
- Updated Date: When the issue was last modified
- Date ranges are inclusive

**Pagination**
- Page Size: Controls number of results per page (1-100)
- Page Number: Specifies which page to retrieve (starts from 1)
- Use pagination for large result sets to improve performance

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| List of Filtered Issues | [ Entity(Issue) ] | Array of issues matching the filter criteria | Array of Issue objects |

### Issue Entity Structure

Each Issue entity contains:
- `id`: Internal JIRA issue ID
- `key`: Human-readable issue key (e.g., DEMO-123)
- `summary`: Issue title/summary
- `description`: Issue description
- `status`: Current issue status
- `priority`: Issue priority level
- `assigneeName`: Name of assigned user
- `reporterName`: Name of reporting user
- `projectKey`: Project key
- `issueType`: Type of issue
- `comments`: Issue comments
- `created`: Creation timestamp
- `updated`: Last update timestamp

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Date format | The field value is invalid. Please check the format and requirements for this field. | Use YYYY-MM-DD format for dates |
| Page size range | The field value is invalid. Please check the format and requirements for this field. | Use page size between 1 and 100 |
| Page number | The field value is invalid. Please check the format and requirements for this field. | Use positive page numbers starting from 1 |
| Status names | Please enter valid status names from your JIRA workflow. | Use exact status names from your JIRA configuration |
| Priority names | Please enter valid status names from your JIRA workflow. | Use exact priority names from your JIRA configuration |
| Project access | You don't have access to this JIRA project. Please contact your JIRA administrator to request access to the project. | Request project access from administrator |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Invalid date formats, page parameters out of range
- **Error Message**: "The field value is invalid. Please check the format and requirements for this field."
- **Common Scenarios**: Invalid date format, negative page numbers, page size > 100
- **Resolution**: Use correct formats and valid parameter ranges

### Logic Errors (LOGIC_ERROR)
- **Cause**: Invalid filter values or no matching results
- **Error Message**: "Please enter valid status names from your JIRA workflow."
- **Common Scenarios**: Misspelled status names, invalid priority levels
- **Resolution**: Use exact names as configured in JIRA

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or query timeout
- **Error Message**: "The JQL query took too long to execute. Please try simplifying the query or adding more specific filters to reduce the result set."
- **Common Scenarios**: Complex queries, large datasets, server performance issues
- **Resolution**: Simplify filters or add more specific criteria

### Authorization Errors
- **Cause**: Authentication failure or insufficient project access
- **Error Message**: "You don't have access to this JIRA project. Please contact your JIRA administrator to request access to the project."
- **Common Scenarios**: Token expiration, project permission changes
- **Resolution**: Re-authenticate and verify project access

## Usage Examples

### Example 1: Find High Priority Bugs
**Input:**
- Issue Type: `Bug`
- Priority: `High`
- Status: `In Progress`
- Page Size: `25`

**Expected Output:**
```json
{
  "List of Filtered Issues": [
    {
      "id": "12345",
      "key": "DEMO-456",
      "summary": "Login authentication fails",
      "status": "In Progress",
      "priority": "High",
      "issueType": "Bug",
      "assigneeName": "John Doe",
      "projectKey": "DEMO"
    }
  ]
}
```

### Example 2: Find Issues by Assignee and Date Range
**Input:**
- Assignee: `jane.smith@company.com`
- Created Date From: `2025-01-01`
- Created Date To: `2025-01-31`
- Page Size: `50`

**Expected Output:**
```json
{
  "List of Filtered Issues": [
    {
      "id": "12346",
      "key": "PROJ-789",
      "summary": "Implement user dashboard",
      "status": "To Do",
      "priority": "Medium",
      "issueType": "Story",
      "assigneeName": "Jane Smith",
      "projectKey": "PROJ"
    }
  ]
}
```

### Example 3: Search by Summary Keywords
**Input:**
- Summary: `authentication`
- Project Key: `DEMO`
- Page Size: `10`

**Expected Output:**
```json
{
  "List of Filtered Issues": [
    {
      "id": "12347",
      "key": "DEMO-101",
      "summary": "Fix authentication timeout issue",
      "status": "Done",
      "priority": "Medium",
      "issueType": "Bug",
      "assigneeName": "Bob Wilson",
      "projectKey": "DEMO"
    }
  ]
}
```

## Business Rules

1. **Filter Combination**: All provided filters are combined with AND logic
2. **Partial Matching**: Summary and name fields support partial matching
3. **Case Sensitivity**: Status, priority, and issue type filters are case-sensitive
4. **Date Inclusivity**: Date range filters include the specified start and end dates
5. **Permission Filtering**: Results only include issues the user has permission to view
6. **Pagination Limits**: Maximum page size is 100 to prevent performance issues

## Limitations

- **Maximum Results**: Single query limited to 1000 total results across all pages
- **Complex Queries**: Very complex filter combinations may impact performance
- **Real-time Data**: Results reflect data at query time, not real-time updates
- **Custom Fields**: Custom field filtering not supported through this interface
- **Archived Projects**: Issues from archived projects may not be included

## Best Practices

1. **Specific Filters**: Use specific filters to reduce result set size and improve performance
2. **Pagination**: Use appropriate page sizes (20-50) for optimal performance
3. **Date Ranges**: Limit date ranges to reasonable periods to avoid large result sets
4. **Status Accuracy**: Verify exact status names in your JIRA configuration
5. **Project Scoping**: Filter by project when possible to improve query performance
6. **Regular Cleanup**: Regularly review and clean up old or irrelevant issues

## Common Use Cases

### Sprint Planning
1. Filter issues by project and status (To Do, In Progress)
2. Review priority levels and assignees
3. Identify issues ready for sprint inclusion
4. Analyze workload distribution across team members

### Bug Triage
1. Filter by issue type (Bug) and priority (High, Critical)
2. Review recent bugs using date filters
3. Identify unassigned bugs for assignment
4. Track bug resolution progress by status

### Progress Reporting
1. Filter by assignee and date range for individual progress
2. Use project filters for team-level reporting
3. Analyze issue completion rates by status
4. Generate workload reports using pagination

### Quality Assurance
1. Filter by reporter to track issue sources
2. Use labels to identify specific feature areas
3. Monitor issue creation trends with date filters
4. Track resolution times across different priorities

## Related Catalog Requests

- [Get User Issues](pages/GetUserIssues.md) - Get issues for specific users
- [Get My Issues by Given Statuses](pages/GetMyIssuesByGivenStatuses.md) - Personal issue tracking
- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Advanced JQL-based searches
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve specific issue details
- [Update an Issue](pages/UpdateAnIssue.md) - Modify retrieved issues

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Parameter validation → Filter construction → Query execution → Result formatting
- **Service Delegation**: Delegates to JIRA REST API v3 search endpoints
- **Telemetry Metrics**: Tracks query performance, result counts, and filter usage patterns

## Troubleshooting

### No Results Returned
**Problem**: Query returns empty result set
**Solution**:
1. Verify filter values match JIRA configuration exactly
2. Check user permissions for target projects
3. Expand date ranges or remove restrictive filters
4. Verify issues exist matching the criteria

### Performance Issues
**Problem**: Query takes too long or times out
**Solution**:
1. Add more specific filters to reduce result set
2. Use smaller page sizes
3. Limit date ranges to shorter periods
4. Contact administrator about JIRA performance

### Invalid Filter Values
**Problem**: Validation errors for status or priority
**Solution**:
1. Check exact spelling of status and priority names
2. Verify values exist in your JIRA configuration
3. Use JIRA admin interface to confirm available values
4. Contact JIRA administrator for configuration details

## See Also

- [JQL Query Guide](pages/JQLQueryGuide.md) - Advanced query syntax
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
