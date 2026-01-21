# Fetch by JQL Query

## Overview

Initiate a search operation in JIRA using JQL (JIRA Query Language) syntax to find issues based on complex criteria and filters.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with JQL validation and syntax help)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| JQL Query | Text | Yes | JIRA Query Language expression to search for issues | `project = "DEMO" AND status = "In Progress"` |

### Parameter Details

**JQL Query**
- Must be valid JQL (JIRA Query Language) syntax
- Supports all standard JQL operators and functions
- Can include complex conditions with AND, OR, NOT operators
- Supports parentheses for grouping conditions
- Case-sensitive for field names and some values

**JQL Syntax Examples:**
- Basic: `project = "DEMO"`
- Multiple conditions: `project = "DEMO" AND status = "To Do"`
- Date functions: `created >= -7d`
- User functions: `assignee = currentUser()`
- Text search: `summary ~ "login"`
- Priority filter: `priority IN (High, Critical)`
- Complex: `(project = "DEMO" OR project = "TEST") AND assignee = currentUser() AND status != Done`

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Task ID | Text | Unique reference ID for this query execution. Use this ID to retrieve the results later | `jql_query_12345_67890` |

### Task ID Usage

The Task ID is used with the [Get Results for Fetch Issues by JQL Query](pages/GetResultsForFetchIssuesByJQLQuery.md) request to retrieve the actual search results. This asynchronous pattern allows for:
- Non-blocking query execution
- Handling of large result sets
- Better performance for complex queries
- Timeout management for long-running queries

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| JQL syntax | The JQL query 'QUERY' contains errors and cannot be executed. Error details: DETAILS. Please review the query syntax and ensure: • Field names are spelled correctly • Operators are valid (=, !=, >, <, IN, etc.) • String values are in quotes • Parentheses are balanced | Fix JQL syntax errors based on provided details |
| Field names | Invalid field name in JQL query | Use valid JIRA field names (project, status, assignee, etc.) |
| Operators | Invalid operator in JQL query | Use valid JQL operators (=, !=, >, <, >=, <=, IN, NOT IN, ~, !~) |
| Functions | Invalid function in JQL query | Use valid JQL functions (currentUser(), now(), startOfDay(), etc.) |
| Quotes | String values must be quoted in JQL | Enclose string values in double quotes |
| Parentheses | Unbalanced parentheses in JQL query | Ensure all opening parentheses have matching closing parentheses |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Empty or missing JQL query
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: No JQL query provided
- **Resolution**: Provide a valid JQL query string

### Logic Errors (LOGIC_ERROR)
- **Cause**: Invalid JQL syntax or unsupported query elements
- **Error Message**: "The JQL query 'project = INVALID' contains errors and cannot be executed. Error details: Project 'INVALID' does not exist."
- **Common Scenarios**: Syntax errors, invalid field names, non-existent projects
- **Resolution**: Correct JQL syntax and verify field/value existence

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or query processing issues
- **Error Message**: "Failed to execute the JQL query. Please verify the query syntax and that you have permission to access the requested data."
- **Common Scenarios**: JIRA server issues, API timeouts, processing errors
- **Resolution**: Check JIRA server status and retry the query

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions."
- **Common Scenarios**: Token expiration, insufficient project access
- **Resolution**: Re-authenticate and verify project permissions

## Usage Examples

### Example 1: Find Open Issues in Project
**Input:**
- JQL Query: `project = "DEMO" AND status != "Done"`

**Expected Output:**
```json
{
  "Task ID": "jql_query_12345_67890"
}
```

**Result**: Task ID generated for retrieving open issues in DEMO project

### Example 2: Find High Priority Bugs Assigned to Current User
**Input:**
- JQL Query: `assignee = currentUser() AND priority = High AND type = Bug`

**Expected Output:**
```json
{
  "Task ID": "jql_query_12346_67891"
}
```

**Result**: Task ID generated for retrieving high priority bugs assigned to current user

### Example 3: Complex Query with Date Range
**Input:**
- JQL Query: `(project = "DEMO" OR project = "TEST") AND created >= -30d AND status IN ("To Do", "In Progress")`

**Expected Output:**
```json
{
  "Task ID": "jql_query_12347_67892"
}
```

**Result**: Task ID generated for retrieving recent issues from multiple projects

### Example 4: Retry Scenario - Invalid JQL Syntax
**Input:**
- JQL Query: `project = DEMO AND status =`

**Expected Output:**
```json
{
  "error": "The JQL query 'project = DEMO AND status =' contains errors and cannot be executed. Error details: Incomplete expression. Please review the query syntax and ensure: • Field names are spelled correctly • Operators are valid (=, !=, >, <, IN, etc.) • String values are in quotes • Parentheses are balanced"
}
```

**Result**: User prompted to correct JQL syntax with specific guidance

## Business Rules

1. **Query Complexity**: Complex queries may take longer to process and return results
2. **Result Limits**: Large result sets may be paginated in the results retrieval
3. **Permission Filtering**: Results only include issues the user has permission to view
4. **Field Availability**: Only fields available in the user's JIRA configuration can be queried
5. **Function Support**: All standard JQL functions are supported (currentUser(), now(), etc.)

## Limitations

- **Query Timeout**: Very complex queries may timeout (typically 60 seconds)
- **Result Size**: Extremely large result sets may be limited or paginated
- **Custom Fields**: Custom field queries require exact field names (customfield_XXXXX)
- **Real-time Data**: Results reflect data at query execution time
- **Concurrent Queries**: Multiple simultaneous queries may impact performance

## Best Practices

1. **Query Optimization**: Use specific filters to reduce result set size
2. **Field Validation**: Verify field names and values exist in your JIRA instance
3. **Syntax Testing**: Test complex queries in JIRA's issue navigator first
4. **Error Handling**: Always check for Task ID before proceeding to results retrieval
5. **Performance Consideration**: Avoid overly broad queries that return thousands of results
6. **Documentation**: Keep complex queries documented for team reference

## Common Use Cases

### Sprint Planning Queries
1. Find all unassigned issues in current sprint
2. Query: `project = "DEMO" AND sprint in openSprints() AND assignee is EMPTY`
3. Retrieve issues ready for assignment during sprint planning
4. Use results to balance workload across team members

### Bug Analysis Queries
1. Find critical bugs created in the last week
2. Query: `type = Bug AND priority = Critical AND created >= -7d`
3. Analyze recent critical issues for patterns
4. Prioritize bug fixes based on severity and recency

### Release Management Queries
1. Find all issues for specific fix version
2. Query: `fixVersion = "2.1.0" AND status != Done`
3. Track remaining work for upcoming release
4. Identify potential release blockers

### Team Performance Queries
1. Find completed issues by team member in date range
2. Query: `assignee = "john.doe" AND status = Done AND resolved >= -30d`
3. Analyze individual productivity and contribution
4. Generate performance reports and metrics

## Related Catalog Requests

- [Get Results for Fetch Issues by JQL Query](pages/GetResultsForFetchIssuesByJQLQuery.md) - Retrieve JQL query results
- [Get Issues](pages/GetIssues.md) - Simpler issue filtering without JQL
- [Get User Issues](pages/GetUserIssues.md) - User-specific issue queries
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve specific issues found in results

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: JQL syntax validation → Query parsing → Execution initiation → Task ID generation
- **Service Delegation**: Delegates to JIRA REST API v3 search endpoints with async processing
- **Telemetry Metrics**: Tracks query complexity, execution time, success rates, and error patterns

## Troubleshooting

### JQL Syntax Errors
**Problem**: Query validation fails with syntax errors
**Solution**:
1. Test query in JIRA's issue navigator first
2. Check field names match your JIRA configuration exactly
3. Ensure string values are properly quoted
4. Verify parentheses are balanced
5. Use JIRA's JQL documentation for reference

### Permission Errors
**Problem**: Query fails due to insufficient permissions
**Solution**:
1. Verify access to all projects referenced in query
2. Check field-level permissions (some fields may be restricted)
3. Contact JIRA administrator for permission review
4. Test with simpler queries to isolate permission issues

### Performance Issues
**Problem**: Query takes too long or times out
**Solution**:
1. Add more specific filters to reduce result set
2. Avoid queries that scan all issues (use project filters)
3. Use date ranges to limit scope
4. Consider breaking complex queries into smaller parts
5. Contact administrator about JIRA performance optimization

### No Task ID Returned
**Problem**: Request completes but no Task ID is provided
**Solution**:
1. Check for error messages in response
2. Verify JQL query syntax is completely valid
3. Ensure authentication is working properly
4. Try a simpler query to test functionality
5. Check JIRA server logs for processing errors

## See Also

- [JQL Query Guide](pages/JQLQueryGuide.md) - Comprehensive JQL syntax reference
- [Get Results for Fetch Issues by JQL Query](pages/GetResultsForFetchIssuesByJQLQuery.md) - Results retrieval
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
