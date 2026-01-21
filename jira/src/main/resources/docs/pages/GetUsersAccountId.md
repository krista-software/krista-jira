# Get Users Account Id

## Overview

Retrieve the unique account ID of a JIRA user by providing their email address for use in other user management operations.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Email | Text | No | Email address of the user whose account ID you want to retrieve | `john.doe@company.com` |

### Parameter Details

**Email**
- Must be a valid email address format
- Email must be associated with an existing JIRA user account
- Case-insensitive matching
- If not provided, returns current user's account ID

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Account Id | Text | The unique JIRA account ID associated with the provided email address | `5d7c8a9b1234567890abcdef` |

### Output Details

**Account Id**
- 24-character alphanumeric string uniquely identifying the user
- Used for all other user management operations
- Remains constant even if user changes email or display name
- Required for issue assignments and user references

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Email format | The email address format is invalid. Please provide a valid email address (e.g., 'user@company.com'). | Use valid email format with @ symbol and domain |
| User exists | The specified user could not be found in JIRA. Please verify the username or email address is correct. | Verify email address is correct and user exists |
| User access | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request user lookup permissions |
| Multiple matches | Multiple users found with similar email addresses. Please provide exact email. | Use exact email address as registered in JIRA |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Invalid email format
- **Error Message**: "The email address format is invalid. Please provide a valid email address (e.g., 'user@company.com')."
- **Common Scenarios**: Malformed email, missing @ symbol, invalid domain
- **Resolution**: Provide valid email address format

### Logic Errors (LOGIC_ERROR)
- **Cause**: User not found or multiple matches
- **Error Message**: "The specified user could not be found in JIRA. Please verify the username or email address is correct."
- **Common Scenarios**: Email not registered, typos in email, user account disabled
- **Resolution**: Verify email address and user account status

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or user lookup processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Insufficient permissions for user lookup
- **Error Message**: "You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions."
- **Common Scenarios**: Missing user browse permissions, restricted user directory access
- **Resolution**: Request user lookup permissions from JIRA administrator

## Usage Examples

### Example 1: Find Account ID by Email
**Input:**
- Email: `jane.smith@company.com`

**Expected Output:**
```json
{
  "Account Id": "5d7c8a9b1234567890abcdef"
}
```

**Result**: Account ID retrieved for user management operations

### Example 2: Get Current User's Account ID
**Input:**
- Email: (empty/not provided)

**Expected Output:**
```json
{
  "Account Id": "5d7c8a9b1234567890fedcba"
}
```

**Result**: Current authenticated user's account ID returned

### Example 3: Find Account ID for Issue Assignment
**Input:**
- Email: `developer@company.com`

**Expected Output:**
```json
{
  "Account Id": "5d7c8a9b1234567890abcdef"
}
```

**Result**: Account ID ready for use in issue assignment operations

### Example 4: Retry Scenario - User Not Found
**Input:**
- Email: `nonexistent@company.com`

**Expected Output:**
```json
{
  "error": "The specified user could not be found in JIRA. Please verify the username or email address is correct."
}
```

**Result**: User prompted to verify email address

## Business Rules

1. **Email Uniqueness**: Each email address is associated with exactly one JIRA account
2. **Case Insensitivity**: Email matching is case-insensitive
3. **Account ID Stability**: Account IDs remain constant throughout user lifecycle
4. **Permission Requirements**: User lookup requires appropriate JIRA permissions
5. **Current User Default**: Empty email returns current user's account ID

## Limitations

- **Single User Lookup**: Only one user can be looked up per request
- **Email Required**: Cannot lookup by username or display name
- **Permission Dependent**: Results limited by user's access permissions
- **Active Users Only**: May not return results for deactivated users
- **Directory Restrictions**: Limited by JIRA user directory configuration

## Best Practices

1. **Email Verification**: Verify email addresses before lookup to avoid errors
2. **Account ID Storage**: Store retrieved account IDs for subsequent operations
3. **Permission Planning**: Ensure appropriate permissions for user lookup operations
4. **Error Handling**: Implement proper error handling for user not found scenarios
5. **Batch Processing**: For multiple users, implement efficient lookup strategies
6. **Security Awareness**: Protect account IDs as they are sensitive identifiers

## Common Use Cases

### Issue Assignment Preparation
1. **Identify Assignee**:
   - Collect email address of intended assignee
   - Use this request to get their account ID
   - Store account ID for issue assignment operations

2. **Assignment Process**:
   - Use retrieved account ID in [Update an Issue](pages/UpdateAnIssue.md) request
   - Verify successful assignment
   - Notify assignee of new issue assignment

### User Management Workflows
1. **User Administration**:
   - Collect email addresses of users to manage
   - Retrieve account IDs for each user
   - Use account IDs for [Update User](pages/UpdateUser.md) or [Delete User](pages/DeleteUser.md) operations

2. **Group Management**:
   - Identify users for group assignment
   - Get account IDs for all target users
   - Add users to appropriate groups using account IDs

### Reporting and Analytics
1. **User Activity Analysis**:
   - Collect email addresses from reports or logs
   - Convert emails to account IDs for JIRA queries
   - Generate user-specific activity reports

2. **Permission Auditing**:
   - Identify users by email for permission reviews
   - Get account IDs for detailed permission analysis
   - Generate compliance reports with user details

### Integration Development
1. **External System Integration**:
   - Map external system users to JIRA accounts
   - Use email addresses as common identifier
   - Convert to account IDs for JIRA API operations

2. **Automated Workflows**:
   - Process user lists from external sources
   - Convert email addresses to account IDs
   - Execute automated user management tasks

## Related Catalog Requests

- [Create User](pages/CreateUser.md) - Create users whose account IDs you'll need
- [Update User](pages/UpdateUser.md) - Use account IDs for group management
- [Delete User](pages/DeleteUser.md) - Use account IDs for user deletion
- [Update an Issue](pages/UpdateAnIssue.md) - Use account IDs for issue assignments
- [Create an Issue](pages/CreateAnIssue.md) - Use account IDs for issue creation

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.UserDao`
- **Validation Flow**: Email validation → User search → Account ID retrieval → Response formatting
- **Service Delegation**: Delegates to JIRA REST API v3 user search endpoints
- **Telemetry Metrics**: Tracks lookup success rates, user search patterns, and performance metrics

## Troubleshooting

### User Not Found Errors
**Problem**: Email address doesn't return any account ID
**Solution**:
1. Verify email address spelling and format
2. Check if user account exists in JIRA
3. Ensure user account is active and not suspended
4. Verify you have permission to view the user

### Permission Denied Errors
**Problem**: Cannot lookup user account IDs
**Solution**:
1. Contact JIRA administrator to grant user browse permissions
2. Verify your account has access to user directory
3. Check organizational policies for user information access
4. Ensure you're authenticated properly

### Multiple Users Found
**Problem**: Multiple users match the provided email
**Solution**:
1. Use exact email address as registered in JIRA
2. Check for duplicate accounts in JIRA
3. Contact JIRA administrator about duplicate email addresses
4. Use additional identifying information if available

### Email Format Errors
**Problem**: Email validation fails
**Solution**:
1. Verify email format includes @ symbol and valid domain
2. Check for extra spaces or special characters
3. Ensure email follows standard format (user@domain.com)
4. Test with known valid email addresses

## See Also

- [Create User](pages/CreateUser.md) - Create users for account ID retrieval
- [Update User](pages/UpdateUser.md) - Use account IDs for user management
- [Delete User](pages/DeleteUser.md) - Use account IDs for user deletion
- [Update an Issue](pages/UpdateAnIssue.md) - Use account IDs for assignments
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
