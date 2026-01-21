# Report a Bug

## Overview

Create a specialized bug report in JIRA with structured fields optimized for defect tracking and resolution workflows.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Project Key | Text | Yes | Key of the project where the bug should be reported | `DEMO` |
| Summary | Text | Yes | Brief, descriptive title for the bug | `Login page crashes on mobile Safari` |
| Description | Text | No | Detailed description of the bug including steps to reproduce | `When users attempt to login on mobile Safari...` |
| Priority | Text | No | Priority level for the bug | `High`, `Critical`, `Medium`, `Low` |
| Environment | Text | No | Environment where the bug occurs | `Production`, `Staging`, `iOS Safari 15.0` |
| Steps to Reproduce | Text | No | Step-by-step instructions to reproduce the bug | `1. Open mobile Safari\n2. Navigate to login page\n3. Enter credentials` |
| Expected Result | Text | No | What should happen when following the steps | `User should be logged in successfully` |
| Actual Result | Text | No | What actually happens when following the steps | `Page crashes and shows white screen` |
| Assignee | Text | No | Account ID of the user to assign the bug to | `5d7c8a9b1234567890abcdef` |
| Reporter | Text | No | Account ID of the user reporting the bug | `5d7c8a9b1234567890abcdef` |
| Labels | Text | No | Comma-separated list of labels for categorization | `mobile,login,crash,safari` |
| Components | Text | No | Comma-separated list of affected components | `Authentication,Frontend,Mobile` |
| Affects Version | Text | No | Version where the bug was discovered | `2.0.1` |

### Parameter Details

**Project Key**
- Must be a valid project key that exists in JIRA
- User must have create issue permissions in the project
- Case-sensitive (typically uppercase)

**Summary**
- Concise description of the bug (max 255 characters)
- Should clearly identify the problem
- Use action-oriented language (e.g., "crashes", "fails", "returns error")

**Description**
- Detailed explanation of the bug
- Include context, impact, and any relevant background information
- Can reference related issues or documentation

**Steps to Reproduce**
- Clear, numbered steps that consistently reproduce the bug
- Include specific data, URLs, or configuration details
- Should be detailed enough for others to follow

**Expected vs Actual Result**
- Expected Result: What the system should do
- Actual Result: What the system actually does
- Clear contrast helps developers understand the issue

**Environment**
- Operating system, browser, device information
- Application version, configuration details
- Network conditions or other environmental factors

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| New Bug | Entity(Issue) | Complete details of the newly created bug report | Bug Issue object with key, ID, and all field values |

### Bug Entity Structure

The returned Bug entity contains:
- `id`: Internal JIRA issue ID
- `key`: Human-readable issue key (e.g., DEMO-123)
- `summary`: Bug title/summary
- `description`: Bug description
- `status`: Current bug status (typically "Open" or "To Do")
- `priority`: Bug priority level
- `assigneeName`: Name of assigned user
- `reporterName`: Name of reporting user
- `projectKey`: Project key
- `issueType`: "Bug"
- `environment`: Environment information
- `labels`: Applied labels

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Project exists | The specified project could not be found in JIRA. Please verify the project key is correct and that you have access to this project. | Verify project key and access permissions |
| Summary required | Required field is missing. Please provide a value for this field to continue. | Provide bug summary/title |
| Summary length | The field value is too long. Please shorten the text to meet the maximum length requirement. | Reduce summary to 255 characters or less |
| Priority valid | The specified status is not valid for this issue type or project. Please select a valid status from the available options. | Use valid priority level for the project |
| User permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request create issue permissions |
| Field validation | The field value is invalid. Please check the format and requirements for this field. | Verify field formats and requirements |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid field formats
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty project key, missing summary, invalid priority
- **Resolution**: Provide all required fields with valid formats

### Logic Errors (LOGIC_ERROR)
- **Cause**: Project not found, invalid priority, or permission issues
- **Error Message**: "Failed to create the issue in JIRA. Please check that all required fields are filled correctly and that you have permission to create issues in this project."
- **Common Scenarios**: Invalid project key, unsupported priority, insufficient permissions
- **Resolution**: Verify project access and field values

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or bug creation processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "Authentication is required to access JIRA. Please authorize your account by clicking the 'Authorize' button and signing in with your JIRA credentials."
- **Common Scenarios**: Token expiration, missing create permissions
- **Resolution**: Re-authenticate and verify permissions

## Usage Examples

### Example 1: Critical Production Bug
**Input:**
- Project Key: `PROD`
- Summary: `Payment processing fails for credit card transactions`
- Description: `Users cannot complete purchases using credit cards. Error occurs during payment validation step.`
- Priority: `Critical`
- Environment: `Production - Payment Gateway v3.2`
- Steps to Reproduce: `1. Add items to cart\n2. Proceed to checkout\n3. Select credit card payment\n4. Enter valid card details\n5. Click 'Complete Purchase'`
- Expected Result: `Payment should process successfully and order should be confirmed`
- Actual Result: `Error message 'Payment validation failed' appears and transaction is rejected`
- Labels: `payment,critical,production`

**Expected Output:**
```json
{
  "New Bug": {
    "id": "12345",
    "key": "PROD-789",
    "summary": "Payment processing fails for credit card transactions",
    "status": "Open",
    "priority": "Critical",
    "issueType": "Bug",
    "projectKey": "PROD"
  }
}
```

### Example 2: UI Bug Report
**Input:**
- Project Key: `DEMO`
- Summary: `Button text overlaps on mobile devices`
- Description: `Submit button text is cut off on screens smaller than 375px width`
- Priority: `Medium`
- Environment: `Mobile Safari iOS 15, Chrome Android`
- Components: `Frontend,Mobile`
- Labels: `ui,mobile,responsive`

**Expected Output:**
```json
{
  "New Bug": {
    "id": "12346",
    "key": "DEMO-456",
    "summary": "Button text overlaps on mobile devices",
    "status": "To Do",
    "priority": "Medium",
    "issueType": "Bug",
    "projectKey": "DEMO"
  }
}
```

### Example 3: Performance Bug
**Input:**
- Project Key: `PERF`
- Summary: `Dashboard loads slowly with large datasets`
- Description: `Dashboard takes over 30 seconds to load when displaying more than 1000 records`
- Priority: `High`
- Environment: `Production - Database v5.7, 10k+ records`
- Steps to Reproduce: `1. Login as admin\n2. Navigate to dashboard\n3. Select 'All Records' filter\n4. Wait for page to load`
- Expected Result: `Dashboard should load within 5 seconds`
- Actual Result: `Dashboard takes 30+ seconds to load and sometimes times out`

**Expected Output:**
```json
{
  "New Bug": {
    "id": "12347",
    "key": "PERF-123",
    "summary": "Dashboard loads slowly with large datasets",
    "status": "Open",
    "priority": "High",
    "issueType": "Bug",
    "projectKey": "PERF"
  }
}
```

## Business Rules

1. **Issue Type**: All bugs are created with issue type "Bug"
2. **Required Fields**: Project key and summary are mandatory
3. **Permission Requirements**: User must have create issue permissions in target project
4. **Priority Defaults**: If no priority specified, uses project default
5. **Status Initialization**: New bugs start in project's initial workflow status

## Limitations

- **Single Bug Creation**: Only one bug can be created per request
- **File Attachments**: Screenshots and files must be attached separately using [Attach File to Ticket](pages/AttachFileToTicket.md)
- **Custom Fields**: Complex custom fields may require specific formatting
- **Workflow Restrictions**: Some projects may have restricted bug creation workflows
- **Bulk Reporting**: Multiple bugs require separate requests

## Best Practices

1. **Clear Summaries**: Use specific, action-oriented summaries that clearly describe the problem
2. **Detailed Steps**: Provide comprehensive reproduction steps that others can follow
3. **Environment Details**: Include specific version numbers, browsers, and system information
4. **Expected vs Actual**: Clearly distinguish between expected and actual behavior
5. **Categorization**: Use appropriate labels and components for better organization
6. **Priority Assessment**: Assign priority based on business impact and urgency

## Common Use Cases

### Production Issue Reporting
1. **Immediate Response**:
   - Identify critical production issue
   - Gather essential details (environment, impact, steps)
   - Create high-priority bug report
   - Assign to appropriate team member
   - Notify stakeholders of issue tracking

2. **Follow-up Actions**:
   - Attach screenshots or logs using [Attach File to Ticket](pages/AttachFileToTicket.md)
   - Update bug with additional findings
   - Track resolution progress
   - Verify fix in production

### Quality Assurance Testing
1. **Test Execution**:
   - Execute test cases and identify defects
   - Document detailed reproduction steps
   - Capture expected vs actual results
   - Create bug reports with comprehensive details

2. **Bug Triage**:
   - Assign appropriate priority levels
   - Categorize bugs by component and severity
   - Link related bugs and test cases
   - Coordinate with development team

### User-Reported Issues
1. **Issue Collection**:
   - Gather user reports from support channels
   - Standardize issue information
   - Create structured bug reports
   - Assign to appropriate development teams

2. **User Communication**:
   - Provide bug tracking numbers to users
   - Update users on resolution progress
   - Verify fixes with affected users
   - Close loop on issue resolution

## Related Catalog Requests

- [Create an Issue](pages/CreateAnIssue.md) - General issue creation for other types
- [Attach File to Ticket](pages/AttachFileToTicket.md) - Add screenshots and logs to bug reports
- [Update an Issue](pages/UpdateAnIssue.md) - Update bug details and status
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve bug details for follow-up

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Project validation → Field validation → Bug creation → Response formatting
- **Service Delegation**: Delegates to JIRA REST API v3 issue creation endpoints with Bug issue type
- **Telemetry Metrics**: Tracks bug creation rates, priority distribution, and resolution patterns

## Troubleshooting

### Bug Creation Fails
**Problem**: Bug report creation returns error
**Solution**:
1. Verify project key exists and is accessible
2. Check all required fields are provided
3. Ensure user has create issue permissions
4. Verify field values match project configuration

### Priority Validation Errors
**Problem**: Priority value is rejected
**Solution**:
1. Check available priority levels in target project
2. Use exact priority names (case-sensitive)
3. Contact JIRA administrator for priority configuration
4. Use default priority if unsure

### Permission Denied
**Problem**: Cannot create bugs in target project
**Solution**:
1. Contact JIRA administrator for create issue permissions
2. Verify project access and visibility
3. Check if project allows bug creation
4. Ensure user account is active and properly configured

## See Also

- [Create an Issue](pages/CreateAnIssue.md) - General issue creation
- [Attach File to Ticket](pages/AttachFileToTicket.md) - Add evidence to bug reports
- [Update an Issue](pages/UpdateAnIssue.md) - Update bug status and details
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
