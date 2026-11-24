# Get Issue By Key

## Overview

Retrieve detailed information about a specific JIRA issue using its unique issue key, providing complete access to all issue fields and metadata.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Issue Key | Text | Yes | Unique key of the issue you want to retrieve | `DEMO-123` |

### Parameter Details

**Issue Key**
- Must be a valid JIRA issue key in PROJECT-NUMBER format
- Case-sensitive (typically uppercase project key)
- Issue must exist and be accessible to the requesting user
- Examples: DEMO-123, PROJ-456, BUG-789

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Issue | Entity(Issue) | Complete details of the requested issue | Issue object with all fields and metadata |

### Issue Entity Structure

The returned Issue entity contains comprehensive issue information:
- `id`: Internal JIRA issue ID
- `key`: Human-readable issue key (matches input)
- `summary`: Issue title/summary
- `description`: Full issue description
- `status`: Current issue status
- `priority`: Issue priority level
- `assigneeName`: Name of assigned user
- `reporterName`: Name of reporting user
- `projectKey`: Project key
- `issueType`: Type of issue (Bug, Story, Task, Epic, etc.)
- `created`: Creation timestamp
- `updated`: Last update timestamp
- `resolved`: Resolution timestamp (if resolved)
- `dueDate`: Due date (if set)
- `labels`: Array of issue labels
- `components`: Array of project components
- `fixVersion`: Target fix version
- `affectedVersion`: Affected version
- `environment`: Environment information
- `resolution`: Resolution status
- `comments`: Issue comments and history
- `attachments`: File attachments
- `customFields`: Custom field values

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Issue key format | The field value is invalid. Please check the format and requirements for this field. | Use valid PROJECT-NUMBER format (e.g., DEMO-123) |
| Issue exists | The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue. | Verify issue key spelling and access permissions |
| Issue access | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request view permissions for the issue |
| Required field | Required field is missing. Please provide a value for this field to continue. | Provide valid issue key |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing issue key or invalid format
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty issue key, malformed key format
- **Resolution**: Provide valid issue key in PROJECT-NUMBER format

### Logic Errors (LOGIC_ERROR)
- **Cause**: Issue not found or insufficient permissions
- **Error Message**: "The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue."
- **Common Scenarios**: Non-existent issue, typos in key, permission restrictions
- **Resolution**: Verify issue existence and check permissions

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or issue retrieval processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or session expiration
- **Error Message**: "Authentication is required to access JIRA. Please authorize your account by clicking the 'Authorize' button and signing in with your JIRA credentials."
- **Common Scenarios**: Token expiration, authentication issues
- **Resolution**: Re-authenticate with JIRA

## Usage Examples

### Example 1: Get Bug Details
**Input:**
- Issue Key: `DEMO-123`

**Expected Output:**
```json
{
  "Issue": {
    "id": "10001",
    "key": "DEMO-123",
    "summary": "Fix login authentication bug",
    "description": "Users are unable to login when using special characters in passwords...",
    "status": "In Progress",
    "priority": "High",
    "assigneeName": "John Doe",
    "reporterName": "Jane Smith",
    "projectKey": "DEMO",
    "issueType": "Bug",
    "created": "2025-01-10T09:00:00.000Z",
    "updated": "2025-01-15T14:30:00.000Z",
    "lastModifiedDate": 1736952600000,
    "dueDate": "2025-01-20",
    "labels": ["authentication", "urgent"],
    "components": ["Login", "Security"],
    "environment": "Production"
  }
}
```

### Example 2: Get Story Details
**Input:**
- Issue Key: `PROJ-456`

**Expected Output:**
```json
{
  "Issue": {
    "id": "10002",
    "key": "PROJ-456",
    "summary": "Implement user dashboard",
    "description": "Create a personalized dashboard for users to view their activity...",
    "status": "Code Review",
    "priority": "Medium",
    "assigneeName": "Alice Johnson",
    "reporterName": "Bob Wilson",
    "projectKey": "PROJ",
    "issueType": "Story",
    "created": "2025-01-05T10:15:00.000Z",
    "updated": "2025-01-14T16:45:00.000Z",
    "lastModifiedDate": 1736869500000,
    "fixVersion": "2.1.0",
    "labels": ["dashboard", "ui"],
    "components": ["Frontend"]
  }
}
```

### Example 3: Get Epic Details
**Input:**
- Issue Key: `EPIC-789`

**Expected Output:**
```json
{
  "Issue": {
    "id": "10003",
    "key": "EPIC-789",
    "summary": "User Management System",
    "description": "Complete overhaul of user management functionality...",
    "status": "In Progress",
    "priority": "High",
    "assigneeName": "Team Lead",
    "reporterName": "Product Manager",
    "projectKey": "EPIC",
    "issueType": "Epic",
    "created": "2025-01-01T08:00:00.000Z",
    "updated": "2025-01-15T12:00:00.000Z",
    "labels": ["epic", "user-management"],
    "components": ["Backend", "Frontend", "Database"]
  }
}
```

### Example 4: Issue Not Found
**Input:**
- Issue Key: `INVALID-999`

**Expected Output:**
```json
{
  "error": "The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue."
}
```

## Business Rules

1. **Single Issue Retrieval**: Returns exactly one issue or an error
2. **Permission Filtering**: Issue must be accessible to the requesting user
3. **Complete Data**: Returns all available fields and metadata for the issue
4. **Real-time Data**: Results reflect current issue state at query time
5. **Field Availability**: Custom fields and project-specific fields included when available

## Limitations

- **Single Issue Only**: Cannot retrieve multiple issues in one request
- **Permission Dependent**: Results limited by user's access permissions
- **Field Visibility**: Some fields may be hidden based on user permissions or field configuration
- **Large Attachments**: Attachment content is not included, only metadata
- **Historical Data**: Returns current state, not historical versions

## Best Practices

1. **Key Validation**: Verify issue key format before making requests
2. **Error Handling**: Always check for error responses before processing issue data
3. **Permission Awareness**: Ensure appropriate permissions for issue access
4. **Data Processing**: Handle optional fields that may not be present
5. **Caching Strategy**: Consider caching frequently accessed issues
6. **Field Usage**: Check field availability before accessing specific properties

## Common Use Cases

### Issue Detail Display
1. **User Interface**:
   - Display complete issue information in applications
   - Show all relevant fields and metadata
   - Provide comprehensive issue view for users
   - Enable detailed issue analysis and review

2. **Issue Navigation**:
   - Retrieve issue details when user clicks on issue links
   - Show issue information in tooltips or popups
   - Provide context for related issues and dependencies
   - Enable drill-down from summary views

### Workflow Processing
1. **Status Validation**:
   - Check current issue status before workflow operations
   - Validate issue state for business rule compliance
   - Ensure issue is in correct state for processing
   - Verify workflow prerequisites

2. **Field Validation**:
   - Check required fields before updates
   - Validate current field values
   - Ensure data consistency before modifications
   - Verify business rule compliance

### Integration Development
1. **Data Synchronization**:
   - Retrieve current issue state for external systems
   - Synchronize issue data with other tools
   - Maintain data consistency across platforms
   - Enable real-time integration updates

2. **Reporting and Analytics**:
   - Gather detailed issue information for reports
   - Analyze issue patterns and trends
   - Generate comprehensive issue summaries
   - Support business intelligence and metrics

### Quality Assurance
1. **Issue Verification**:
   - Verify issue details before testing
   - Check issue requirements and acceptance criteria
   - Validate issue configuration and setup
   - Ensure proper issue categorization

2. **Test Case Preparation**:
   - Gather issue requirements for test planning
   - Extract acceptance criteria for test cases
   - Understand issue context for testing
   - Prepare comprehensive test scenarios

## Related Catalog Requests

- [Get Issues](pages/GetIssues.md) - Search for issues to get their keys
- [Update an Issue](pages/UpdateAnIssue.md) - Update retrieved issue details
- [Attach File to Ticket](pages/AttachFileToTicket.md) - Add files to retrieved issues
- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Find issues using complex criteria

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Issue key validation → Issue existence check → Permission verification → Issue retrieval → Response formatting
- **Service Delegation**: Delegates to JIRA REST API v3 issue retrieval endpoints
- **Telemetry Metrics**: Tracks retrieval success rates, response times, and access patterns

## Troubleshooting

### Issue Not Found Errors
**Problem**: Issue key is not recognized or returns not found error
**Solution**:
1. Verify issue key spelling and format (PROJECT-NUMBER)
2. Check if issue exists in JIRA interface
3. Ensure you have permission to view the issue
4. Verify issue hasn't been deleted or moved

### Permission Denied Errors
**Problem**: Cannot access issue due to permission restrictions
**Solution**:
1. Contact JIRA administrator to grant view permissions
2. Verify you have access to the issue's project
3. Check if issue has restricted visibility
4. Ensure your account has appropriate permissions

### Invalid Key Format Errors
**Problem**: Issue key format is rejected
**Solution**:
1. Use correct PROJECT-NUMBER format (e.g., DEMO-123)
2. Ensure project key is uppercase (typically)
3. Verify issue number is numeric
4. Check for extra spaces or special characters

### Incomplete Data Returned
**Problem**: Some expected fields are missing from response
**Solution**:
1. Check if fields are configured for the issue type
2. Verify you have permission to view specific fields
3. Ensure fields are not hidden by field configuration
4. Contact JIRA administrator about field visibility

## See Also

- [Get Issues](pages/GetIssues.md) - Search for multiple issues
- [Update an Issue](pages/UpdateAnIssue.md) - Modify issue details
- [Create an Issue](pages/CreateAnIssue.md) - Create new issues
- [Attach File to Ticket](pages/AttachFileToTicket.md) - Add attachments
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
