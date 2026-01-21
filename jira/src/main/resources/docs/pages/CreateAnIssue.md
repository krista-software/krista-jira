# Create an Issue

## Overview

Create a new JIRA issue with comprehensive details including project assignment, issue type, priority, and custom field values.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Project Key | Text | Yes | Key of the project where the issue should be created | `DEMO` |
| Summary | Text | Yes | Short summary or title for the issue | `Fix login authentication bug` |
| Description | Text | No | Detailed description of the issue | `Users cannot log in with valid credentials...` |
| Issue Type | Text | Yes | Type of issue to create | `Bug`, `Story`, `Task`, `Epic` |
| Priority | Text | No | Priority level for the issue | `High`, `Medium`, `Low`, `Critical` |
| Assignee | Text | No | Account ID of the user to assign the issue to | `5d7c8a9b1234567890abcdef` |
| Reporter | Text | No | Account ID of the user reporting the issue | `5d7c8a9b1234567890abcdef` |
| Labels | Text | No | Comma-separated list of labels to apply | `bug,authentication,urgent` |
| Components | Text | No | Comma-separated list of project components | `Frontend,Authentication` |
| Fix Version | Text | No | Version where the issue should be fixed | `1.2.0` |
| Due Date | Text | No | Due date for the issue (YYYY-MM-DD format) | `2025-12-31` |
| Environment | Text | No | Environment where the issue occurs | `Production` |
| Custom Field Values | Text | No | JSON string of custom field values | `{"customfield_10001": "value"}` |

### Parameter Details

**Project Key**
- Must be a valid project key that exists in your JIRA instance
- User must have create issue permissions in the project
- Case-sensitive (typically uppercase)

**Summary**
- Brief, descriptive title for the issue
- Maximum length: 255 characters
- Should clearly identify the problem or requirement

**Issue Type**
- Must be a valid issue type configured in the target project
- Common types: Bug, Story, Task, Epic, Sub-task
- Case-sensitive and must match exactly

**Priority**
- Must be a valid priority level configured in JIRA
- Common priorities: Blocker, Critical, High, Medium, Low
- Optional - defaults to project default priority

**Assignee/Reporter**
- Must be valid JIRA account IDs (not usernames)
- Users must exist and have access to the project
- Optional - defaults to current user for reporter

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| New Issue | Entity(Issue) | Complete details of the newly created issue | Issue object with key, ID, and all field values |

### Issue Entity Structure

The returned Issue entity contains:
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
- `comments`: Any initial comments

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Project exists | The specified project could not be found in JIRA. Please verify the project key is correct and that you have access to this project. | Verify project key and access permissions |
| Issue type valid | The specified status is not valid for this issue type or project. Please select a valid status from the available options. | Use valid issue type for the project |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Fill all required fields |
| Field length | The field value is too long. Please shorten the text to meet the maximum length requirement. | Reduce field content length |
| User permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request create issue permissions |
| Custom fields | The field value is invalid. Please check the format and requirements for this field. | Verify custom field format and values |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid field formats
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty project key, missing summary, invalid date format
- **Resolution**: Provide all required fields with valid formats

### Logic Errors (LOGIC_ERROR)
- **Cause**: Project not found, invalid issue type, or permission issues
- **Error Message**: "Failed to create the issue in JIRA. Please check that all required fields are filled correctly and that you have permission to create issues in this project."
- **Common Scenarios**: Invalid project key, unsupported issue type, insufficient permissions
- **Resolution**: Verify project access and issue type availability

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or network connectivity issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server downtime, API rate limits
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or expired tokens
- **Error Message**: "Authentication is required to access JIRA. Please authorize your account by clicking the 'Authorize' button and signing in with your JIRA credentials."
- **Common Scenarios**: Token expiration, invalid credentials
- **Resolution**: Re-authenticate with JIRA

## Usage Examples

### Example 1: Create Bug Report
**Input:**
- Project Key: `DEMO`
- Summary: `Login page crashes on mobile devices`
- Description: `The login page becomes unresponsive on iOS Safari when entering credentials`
- Issue Type: `Bug`
- Priority: `High`
- Labels: `mobile,login,crash`

**Expected Output:**
```json
{
  "New Issue": {
    "id": "12345",
    "key": "DEMO-456",
    "summary": "Login page crashes on mobile devices",
    "status": "To Do",
    "priority": "High",
    "issueType": "Bug",
    "projectKey": "DEMO"
  }
}
```

### Example 2: Create User Story
**Input:**
- Project Key: `PROJ`
- Summary: `As a user, I want to reset my password`
- Description: `Users should be able to reset their password via email link`
- Issue Type: `Story`
- Priority: `Medium`

**Expected Output:**
```json
{
  "New Issue": {
    "id": "12346",
    "key": "PROJ-789",
    "summary": "As a user, I want to reset my password",
    "status": "To Do",
    "priority": "Medium",
    "issueType": "Story",
    "projectKey": "PROJ"
  }
}
```

### Example 3: Retry Scenario - Invalid Project
**Input:**
- Project Key: `INVALID`
- Summary: `Test issue`
- Issue Type: `Bug`

**Expected Output:**
```json
{
  "error": "The specified project could not be found in JIRA. Please verify the project key is correct and that you have access to this project."
}
```

## Business Rules

1. **Project Access**: User must have "Create Issues" permission in the target project
2. **Required Fields**: All project-configured required fields must be provided
3. **Issue Type Availability**: Issue type must be available in the project's issue type scheme
4. **Field Constraints**: All field values must comply with project-specific constraints
5. **Workflow Initialization**: New issues start in the initial status of the project workflow

## Limitations

- **Custom Fields**: Complex custom field types may require specific formatting
- **Bulk Creation**: Only one issue can be created per request
- **File Attachments**: Files cannot be attached during issue creation (use separate attachment request)
- **Sub-tasks**: Sub-tasks require parent issue specification
- **Workflow Restrictions**: Some projects may have restricted issue creation workflows

## Best Practices

1. **Descriptive Summaries**: Use clear, concise summaries that identify the issue quickly
2. **Detailed Descriptions**: Provide comprehensive descriptions with steps to reproduce for bugs
3. **Appropriate Issue Types**: Choose the correct issue type based on the nature of the work
4. **Priority Assignment**: Use priority levels consistently across the team
5. **Label Usage**: Apply relevant labels for better categorization and searchability
6. **Component Assignment**: Assign appropriate components for better organization

## Common Use Cases

### Bug Reporting Workflow
1. Identify a defect or problem in the system
2. Gather relevant details (steps to reproduce, environment, impact)
3. Create bug issue with comprehensive description
4. Assign appropriate priority and labels
5. Notify relevant team members

### Feature Request Process
1. Define new feature or enhancement requirement
2. Create story or epic issue type
3. Include acceptance criteria in description
4. Set appropriate priority and target version
5. Assign to product owner or development team

### Task Management
1. Identify work items that need completion
2. Create task issues with clear objectives
3. Set realistic due dates and assignments
4. Track progress through workflow states
5. Close tasks upon completion

## Related Catalog Requests

- [Update an Issue](pages/UpdateAnIssue.md) - Modify created issues
- [Attach File to Ticket](pages/AttachFileToTicket.md) - Add attachments to created issues
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve created issue details
- [Assign Issue to Epic](pages/AssignIssueToEpic.md) - Link issues to epics

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Project validation → Issue type validation → Field validation → Creation execution
- **Service Delegation**: Delegates to JIRA REST API v3 issue creation endpoints
- **Telemetry Metrics**: Tracks creation success rates, issue types, and performance metrics

## Troubleshooting

### Issue Creation Fails
**Problem**: Issue creation returns error or fails silently
**Solution**:
1. Verify all required fields are provided
2. Check project key spelling and access
3. Ensure issue type is available in project
4. Verify user has create permissions

### Custom Field Errors
**Problem**: Custom field validation fails
**Solution**:
1. Check custom field configuration in project
2. Verify field value format matches requirements
3. Ensure custom field is available for the issue type
4. Contact JIRA administrator for field configuration

### Permission Denied
**Problem**: User cannot create issues in project
**Solution**:
1. Contact JIRA administrator to grant create permissions
2. Verify user is assigned to the project
3. Check project permission scheme
4. Ensure user account is active

## See Also

- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [JQL Query Guide](pages/JQLQueryGuide.md) - Advanced issue searching
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
