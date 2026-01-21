# Get Release Summary

## Overview

Retrieve comprehensive release analytics and reporting with detailed summaries of user stories, tasks, bugs, and overall progress filtered by various criteria.

## Request Details

- **Area**: Release Management
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Status | Text | No | Filter issues by status | `Open`, `In Progress`, `Done`, `Closed` |
| Assignee | Text | No | Filter issues by assignee name or email | `john.doe@company.com` |
| Priority | Text | No | Filter issues by priority level | `High`, `Medium`, `Low`, `Critical` |
| Issue Type | Text | No | Filter by issue type | `Bug`, `Story`, `Task`, `Epic` |
| Page Size | Number | No | Number of results to display per page (default: 20) | `50` |
| Page Number | Number | No | Page number to retrieve (default: 1) | `2` |
| Fix Version | Text | No | Filter issues by fix version | `2.1.0`, `Sprint 5` |

### Parameter Details

**Status**
- Filter issues by their current workflow status
- Must match exact status names configured in JIRA
- Common statuses: To Do, In Progress, Done, Closed

**Assignee**
- Filter by assignee name or email address
- Supports partial name matching
- Leave empty to include all assignees

**Priority**
- Filter by priority level
- Must match exact priority names configured in JIRA
- Common priorities: Blocker, Critical, High, Medium, Low

**Issue Type**
- Filter by specific issue types
- Common types: Story, Task, Bug, Epic, Sub-task
- Case-sensitive matching

**Fix Version**
- Filter by target release version
- Must match version names configured in project
- Useful for release-specific reporting

**Pagination**
- Page Size: Controls number of detailed issues returned (1-100)
- Page Number: Specifies which page to retrieve (starts from 1)
- Summary statistics include all matching issues regardless of pagination

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Total user stories | Number | Total count of user stories in the release | `25` |
| Open user stories | Number | Number of user stories that are still open | `8` |
| Closed user stories | Number | Number of user stories that are completed | `17` |
| Total tasks | Number | Total count of tasks in the release | `45` |
| Open tasks | Number | Number of tasks that are still open | `12` |
| Closed tasks | Number | Number of tasks that are completed | `33` |
| Total bugs | Number | Total count of bugs in the release | `15` |
| Open bugs | Number | Number of bugs that are still open | `3` |
| Closed bugs | Number | Number of bugs that are resolved | `12` |
| User Stories | [ Entity(Issue) ] | Detailed list of user story issues | Array of Story Issue objects |
| Tasks | [ Entity(Issue) ] | Detailed list of task issues | Array of Task Issue objects |
| Bugs | [ Entity(Issue) ] | Detailed list of bug issues | Array of Bug Issue objects |

### Issue Entity Structure

Each Issue entity in the detailed lists contains:
- `id`: Internal JIRA issue ID
- `key`: Human-readable issue key
- `summary`: Issue title/summary
- `status`: Current issue status
- `priority`: Issue priority level
- `assigneeName`: Name of assigned user
- `reporterName`: Name of reporting user
- `projectKey`: Project key
- `issueType`: Type of issue
- `created`: Creation timestamp
- `updated`: Last update timestamp
- `fixVersion`: Target fix version

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Status names | Please enter valid status names from your JIRA workflow. | Use exact status names from JIRA configuration |
| Priority names | Please enter valid status names from your JIRA workflow. | Use exact priority names from JIRA configuration |
| Issue type | The specified status is not valid for this issue type or project. Please select a valid status from the available options. | Use valid issue type names |
| Page parameters | The field value is invalid. Please check the format and requirements for this field. | Use positive numbers for page size and page number |
| User access | You don't have access to this JIRA project. Please contact your JIRA administrator to request access to the project. | Request project access permissions |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Invalid page parameters or malformed filter values
- **Error Message**: "The field value is invalid. Please check the format and requirements for this field."
- **Common Scenarios**: Negative page numbers, page size > 100, invalid number formats
- **Resolution**: Use valid parameter ranges and formats

### Logic Errors (LOGIC_ERROR)
- **Cause**: Invalid filter values or no accessible projects
- **Error Message**: "Please enter valid status names from your JIRA workflow."
- **Common Scenarios**: Misspelled status/priority names, invalid issue types
- **Resolution**: Use exact names as configured in JIRA

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or query processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again."
- **Common Scenarios**: Network issues, JIRA server problems, query timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient project access
- **Error Message**: "You don't have access to this JIRA project. Please contact your JIRA administrator to request access to the project."
- **Common Scenarios**: Token expiration, project permission changes
- **Resolution**: Re-authenticate and verify project access

## Usage Examples

### Example 1: Overall Release Progress
**Input:**
- Fix Version: `2.1.0`
- Page Size: `20`

**Expected Output:**
```json
{
  "Total user stories": 25,
  "Open user stories": 8,
  "Closed user stories": 17,
  "Total tasks": 45,
  "Open tasks": 12,
  "Closed tasks": 33,
  "Total bugs": 15,
  "Open bugs": 3,
  "Closed bugs": 12,
  "User Stories": [
    {
      "key": "DEMO-101",
      "summary": "User login functionality",
      "status": "Done",
      "priority": "High",
      "assigneeName": "John Doe"
    }
  ]
}
```

### Example 2: High Priority Items Analysis
**Input:**
- Priority: `High`
- Status: `In Progress`
- Page Size: `50`

**Expected Output:**
```json
{
  "Total user stories": 5,
  "Open user stories": 5,
  "Closed user stories": 0,
  "Total tasks": 8,
  "Open tasks": 8,
  "Closed tasks": 0,
  "Total bugs": 2,
  "Open bugs": 2,
  "Closed bugs": 0
}
```

### Example 3: Team Member Workload
**Input:**
- Assignee: `jane.smith@company.com`
- Status: `In Progress`
- Page Size: `25`

**Expected Output:**
```json
{
  "Total user stories": 3,
  "Open user stories": 3,
  "Closed user stories": 0,
  "Total tasks": 7,
  "Open tasks": 7,
  "Closed tasks": 0,
  "Total bugs": 1,
  "Open bugs": 1,
  "Closed bugs": 0
}
```

## Business Rules

1. **Issue Classification**: Issues are automatically categorized by type (Story, Task, Bug)
2. **Status Interpretation**: "Open" includes all non-final statuses, "Closed" includes final statuses
3. **Permission Filtering**: Results only include issues the user has permission to view
4. **Real-time Data**: Statistics reflect current state at query time
5. **Pagination Scope**: Summary counts include all matching issues, detailed lists are paginated

## Limitations

- **Project Scope**: Results may span multiple projects based on user permissions
- **Custom Issue Types**: Custom issue types are categorized based on hierarchy
- **Large Datasets**: Very large result sets may impact performance
- **Real-time Updates**: Results don't reflect changes made after query execution
- **Complex Filtering**: Advanced filtering requires JQL queries

## Best Practices

1. **Specific Filtering**: Use fix version or other specific filters for focused reports
2. **Regular Monitoring**: Run release summaries regularly to track progress
3. **Team Coordination**: Share summary reports with team members for visibility
4. **Trend Analysis**: Compare summaries over time to identify trends
5. **Performance**: Use appropriate page sizes to balance detail and performance
6. **Version Management**: Use consistent fix version naming for accurate reporting

## Common Use Cases

### Release Planning
1. Analyze overall scope of upcoming release
2. Review distribution of work across issue types
3. Identify high-priority items requiring attention
4. Assess team capacity and workload distribution
5. Plan sprint allocation based on remaining work

### Progress Tracking
1. Monitor daily/weekly progress toward release goals
2. Track completion rates for different issue types
3. Identify bottlenecks in specific areas
4. Measure team velocity and productivity
5. Generate status reports for stakeholders

### Quality Assurance
1. Monitor bug discovery and resolution rates
2. Track critical and high-priority defects
3. Analyze bug distribution across components
4. Ensure quality gates are met before release
5. Identify areas requiring additional testing

### Stakeholder Reporting
1. Generate executive summaries of release progress
2. Provide detailed breakdowns by priority and type
3. Track milestone completion and deadlines
4. Communicate risks and blockers to management
5. Demonstrate value delivery through completed stories

## Related Catalog Requests

- [Get Issues](pages/GetIssues.md) - Detailed issue filtering and retrieval
- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Advanced custom queries for releases
- [Get User Issues](pages/GetUserIssues.md) - Individual team member analysis
- [Update an Issue](pages/UpdateAnIssue.md) - Update issues based on release analysis

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.ReleaseDao`
- **Validation Flow**: Parameter validation → Filter construction → Issue categorization → Statistics calculation → Response formatting
- **Service Delegation**: Delegates to JIRA REST API v3 search and project endpoints
- **Telemetry Metrics**: Tracks query performance, result distribution, and usage patterns

## Troubleshooting

### No Results Returned
**Problem**: Summary shows zero counts for all issue types
**Solution**:
1. Verify filter values match JIRA configuration exactly
2. Check user permissions for target projects
3. Ensure issues exist matching the criteria
4. Try broader filters to test functionality

### Incomplete Results
**Problem**: Expected issues are missing from results
**Solution**:
1. Check user permissions for all relevant projects
2. Verify issue types are configured correctly
3. Ensure fix versions match exactly
4. Review project access and visibility settings

### Performance Issues
**Problem**: Query takes too long or times out
**Solution**:
1. Add more specific filters to reduce scope
2. Use smaller page sizes for detailed results
3. Contact administrator about JIRA performance
4. Consider using JQL queries for complex filtering

### Permission Errors
**Problem**: Cannot access release data
**Solution**:
1. Verify project access permissions
2. Check if user can view issues in target projects
3. Contact JIRA administrator for permission review
4. Ensure authentication is working properly

## See Also

- [Get Issues](pages/GetIssues.md) - Detailed issue filtering
- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Advanced release queries
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
