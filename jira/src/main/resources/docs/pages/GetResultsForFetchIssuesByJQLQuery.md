# Get Results for Fetch Issues by JQL Query

## Overview

Retrieve the results of a previously initiated JQL query using its Task ID to get the list of matching JIRA issues.

## Request Details

- **Area**: Rituals
- **Type**: WAIT_FOR_EVENT
- **Retry Support**: ✅ Yes (with task status validation)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| eventName | Text | Yes | Event name for the JQL query result retrieval | `jql_query_result` |
| eventData | FreeForm | Yes | Event data containing the Task ID from the JQL query | `{"taskId": "jql_query_12345_67890"}` |

### Parameter Details

**eventName**
- Fixed value: `jql_query_result`
- Used to identify the type of event being processed
- Must match exactly for proper event handling

**eventData**
- JSON object containing the Task ID
- Task ID must be from a previously executed [Fetch by JQL Query](pages/FetchByJQLQuery.md) request
- Format: `{"taskId": "TASK_ID_VALUE"}`

**Task ID Requirements**
- Must be a valid Task ID returned from a JQL query request
- Task must be completed (not still processing)
- Task must not be expired (typically valid for 24 hours)
- Task must belong to the authenticated user

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| List of Issues | [ Entity(Issue) ] | Array of issues matching the original JQL query | Array of Issue objects |

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
- `labels`: Issue labels
- `components`: Project components

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Task ID format | The field value is invalid. Please check the format and requirements for this field. | Provide valid Task ID from JQL query request |
| Task exists | The specified task could not be found. It may have expired or been processed already. | Use a recent, valid Task ID from JQL query |
| Task completion | The task is still processing. Please wait and try again. | Wait for JQL query processing to complete |
| Task ownership | You don't have permission to access this task result. | Use Task ID from your own JQL query requests |
| Event format | Invalid event data format. Please provide proper JSON structure. | Use correct JSON format for eventData |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing or invalid Task ID, malformed event data
- **Error Message**: "The field value is invalid. Please check the format and requirements for this field."
- **Common Scenarios**: Empty Task ID, invalid JSON format, wrong event name
- **Resolution**: Provide valid Task ID and properly formatted event data

### Logic Errors (LOGIC_ERROR)
- **Cause**: Task not found, expired, or still processing
- **Error Message**: "The specified task could not be found. It may have expired or been processed already."
- **Common Scenarios**: Expired Task ID, task still processing, invalid task reference
- **Resolution**: Use recent Task ID and ensure JQL query has completed

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or result retrieval issues
- **Error Message**: "Unable to retrieve query results from JIRA. Please try again later."
- **Common Scenarios**: JIRA server issues, network problems, result processing errors
- **Resolution**: Check JIRA server status and retry the request

### Authorization Errors
- **Cause**: Authentication failure or task ownership issues
- **Error Message**: "You don't have permission to access this task result."
- **Common Scenarios**: Token expiration, accessing another user's task
- **Resolution**: Re-authenticate and use your own Task IDs

## Usage Examples

### Example 1: Retrieve Simple Query Results
**Input:**
- eventName: `jql_query_result`
- eventData: `{"taskId": "jql_query_12345_67890"}`

**Expected Output:**
```json
{
  "List of Issues": [
    {
      "id": "10001",
      "key": "DEMO-123",
      "summary": "Fix login authentication bug",
      "status": "In Progress",
      "priority": "High",
      "assigneeName": "John Doe",
      "projectKey": "DEMO",
      "issueType": "Bug"
    },
    {
      "id": "10002",
      "key": "DEMO-124",
      "summary": "Update user interface",
      "status": "To Do",
      "priority": "Medium",
      "assigneeName": "Jane Smith",
      "projectKey": "DEMO",
      "issueType": "Story"
    }
  ]
}
```

### Example 2: Retrieve Complex Query Results
**Input:**
- eventName: `jql_query_result`
- eventData: `{"taskId": "jql_query_12346_67891"}`

**Expected Output:**
```json
{
  "List of Issues": [
    {
      "id": "10003",
      "key": "PROJ-456",
      "summary": "Critical security vulnerability",
      "status": "In Progress",
      "priority": "Critical",
      "assigneeName": "Security Team",
      "projectKey": "PROJ",
      "issueType": "Bug",
      "labels": ["security", "critical"],
      "created": "2025-01-15T10:30:00.000Z"
    }
  ]
}
```

### Example 3: Empty Results
**Input:**
- eventName: `jql_query_result`
- eventData: `{"taskId": "jql_query_12347_67892"}`

**Expected Output:**
```json
{
  "List of Issues": []
}
```

**Result**: No issues matched the original JQL query criteria

### Example 4: Retry Scenario - Task Still Processing
**Input:**
- eventName: `jql_query_result`
- eventData: `{"taskId": "jql_query_12348_67893"}`

**Expected Output:**
```json
{
  "error": "The task is still processing. Please wait and try again."
}
```

**Result**: User prompted to wait and retry the request

## Business Rules

1. **Task Lifecycle**: Tasks have a limited lifetime (typically 24 hours)
2. **Single Use**: Task results can be retrieved multiple times until expiration
3. **User Isolation**: Users can only access results from their own JQL queries
4. **Result Consistency**: Results reflect the state at the time of query execution
5. **Permission Filtering**: Results only include issues the user has permission to view

## Limitations

- **Task Expiration**: Task IDs expire after a configured time period (typically 24 hours)
- **Result Size**: Very large result sets may be paginated or truncated
- **Processing Time**: Complex queries may take several seconds to complete
- **Concurrent Access**: Multiple simultaneous result retrievals may impact performance
- **Real-time Updates**: Results don't reflect changes made after query execution

## Best Practices

1. **Timely Retrieval**: Retrieve results promptly after JQL query completion
2. **Error Handling**: Always check for task completion before assuming failure
3. **Result Processing**: Handle empty result sets gracefully
4. **Task Management**: Keep track of Task IDs and their expiration times
5. **Performance**: Process large result sets efficiently to avoid timeouts
6. **Retry Logic**: Implement appropriate retry delays for processing tasks

## Common Use Cases

### Asynchronous Query Processing
1. Submit complex JQL query using [Fetch by JQL Query](pages/FetchByJQLQuery.md)
2. Receive Task ID for tracking query progress
3. Use this request to retrieve completed results
4. Process results for reporting or further analysis

### Large Dataset Handling
1. Execute JQL query that may return many results
2. Allow asynchronous processing to handle large datasets
3. Retrieve results when processing is complete
4. Implement pagination if results are very large

### Scheduled Reporting
1. Set up automated JQL queries for regular reports
2. Store Task IDs for later result retrieval
3. Retrieve results at scheduled intervals
4. Generate reports based on query results

### Interactive Dashboards
1. Allow users to submit custom JQL queries
2. Display processing status while query executes
3. Retrieve and display results when available
4. Provide real-time feedback on query progress

## Related Catalog Requests

- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Initiate JQL queries that generate Task IDs
- [Get Issues](pages/GetIssues.md) - Simpler synchronous issue filtering
- [Get User Issues](pages/GetUserIssues.md) - User-specific issue queries
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve specific issues from results

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Task ID validation → Task status check → Result retrieval → Response formatting
- **Service Delegation**: Delegates to JIRA REST API v3 with task management system
- **Telemetry Metrics**: Tracks result retrieval success rates, processing times, and result sizes

## Troubleshooting

### Task Not Found Errors
**Problem**: Task ID is not recognized or has expired
**Solution**:
1. Verify Task ID was copied correctly from JQL query response
2. Check if task has expired (typically 24 hours)
3. Re-execute the original JQL query to get a new Task ID
4. Ensure you're using your own Task ID, not another user's

### Task Still Processing
**Problem**: Results not available yet, task still executing
**Solution**:
1. Wait a few seconds and retry the request
2. Complex queries may take longer to process
3. Check JIRA server performance if delays are excessive
4. Consider simplifying the original JQL query

### Empty Results
**Problem**: Query returns no issues when results were expected
**Solution**:
1. Verify the original JQL query syntax was correct
2. Check if issues matching criteria exist and are accessible
3. Review user permissions for projects in the query
4. Test the JQL query directly in JIRA's issue navigator

### Permission Errors
**Problem**: Cannot access task results due to permission issues
**Solution**:
1. Ensure you're authenticated with the same account that created the task
2. Verify your session hasn't expired
3. Re-authenticate if necessary
4. Contact administrator if permission issues persist

## See Also

- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Create JQL queries that generate Task IDs
- [JQL Query Guide](pages/JQLQueryGuide.md) - JQL syntax and examples
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
