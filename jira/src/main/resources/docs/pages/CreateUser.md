# Create User

## Overview

Create a new JIRA user by sending an invitation email to join your JIRA workspace with appropriate access permissions.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Email | Text | Yes | Email address of the user to be invited | `john.doe@company.com` |
| Display Name | Text | No | Full name to display for the user | `John Doe` |
| Send Email | Boolean | No | Whether to send invitation email (default: true) | `true` |

### Parameter Details

**Email**
- Must be a valid email address format
- Email address must not already exist in JIRA
- Will be used as the username for the new account
- Required for account creation and invitation delivery

**Display Name**
- Optional friendly name for the user
- If not provided, defaults to the email address
- Can be changed later by the user or administrator
- Used in issue assignments and user interfaces

**Send Email**
- Controls whether invitation email is sent automatically
- Default value is true (send invitation)
- Set to false for bulk user creation without immediate invitations
- Users must be manually notified if email is not sent

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Self | Text | URL reference to the created user profile in JIRA | `https://your-domain.atlassian.net/rest/api/3/user?accountId=123` |
| Account Id | Text | Unique account identifier assigned to the newly created user | `5d7c8a9b1234567890abcdef` |

### Output Details

**Self**
- REST API URL for accessing the user's profile
- Can be used for subsequent API calls
- Contains the account ID parameter
- Useful for programmatic user management

**Account Id**
- Unique identifier for the user in JIRA
- Used for issue assignments and user references
- Required for other user management operations
- Remains constant even if email/display name changes

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Email format | The email address format is invalid. Please provide a valid email address (e.g., 'user@company.com'). | Use valid email format with @ symbol and domain |
| Email uniqueness | The specified user already exists in JIRA. Please use a different email address. | Check if user already exists or use different email |
| Admin permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request user management permissions from administrator |
| Domain restrictions | Email domain is not allowed for user creation. | Use email from approved domain or contact administrator |
| License limits | Cannot create user due to license limitations. | Contact administrator about user license availability |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Invalid email format or missing required fields
- **Error Message**: "The email address format is invalid. Please provide a valid email address (e.g., 'user@company.com')."
- **Common Scenarios**: Malformed email, missing @ symbol, invalid domain format
- **Resolution**: Provide valid email address in correct format

### Logic Errors (LOGIC_ERROR)
- **Cause**: User already exists or business rule violations
- **Error Message**: "Failed to create the user in JIRA. Please check that all required information is provided and that you have user management permissions."
- **Common Scenarios**: Duplicate email, domain restrictions, license limits
- **Resolution**: Check user existence and verify business rules

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or user creation processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Insufficient permissions for user management
- **Error Message**: "You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions."
- **Common Scenarios**: Missing admin permissions, restricted user management
- **Resolution**: Request user management permissions from JIRA administrator

## Usage Examples

### Example 1: Create User with Full Details
**Input:**
- Email: `jane.smith@company.com`
- Display Name: `Jane Smith`
- Send Email: `true`

**Expected Output:**
```json
{
  "Self": "https://your-domain.atlassian.net/rest/api/3/user?accountId=5d7c8a9b1234567890abcdef",
  "Account Id": "5d7c8a9b1234567890abcdef"
}
```

**Result**: User created successfully with invitation email sent

### Example 2: Create User with Email Only
**Input:**
- Email: `bob.wilson@company.com`

**Expected Output:**
```json
{
  "Self": "https://your-domain.atlassian.net/rest/api/3/user?accountId=5d7c8a9b1234567890fedcba",
  "Account Id": "5d7c8a9b1234567890fedcba"
}
```

**Result**: User created with email as display name, invitation sent

### Example 3: Bulk User Creation (No Email)
**Input:**
- Email: `temp.user@company.com`
- Display Name: `Temporary User`
- Send Email: `false`

**Expected Output:**
```json
{
  "Self": "https://your-domain.atlassian.net/rest/api/3/user?accountId=5d7c8a9b1234567890abcdef",
  "Account Id": "5d7c8a9b1234567890abcdef"
}
```

**Result**: User created without invitation email for bulk processing

### Example 4: Retry Scenario - Duplicate Email
**Input:**
- Email: `existing.user@company.com`
- Display Name: `Existing User`

**Expected Output:**
```json
{
  "error": "The specified user already exists in JIRA. Please use a different email address."
}
```

**Result**: User prompted to use different email address

## Business Rules

1. **Email Uniqueness**: Each email address can only be associated with one JIRA account
2. **License Consumption**: Creating users consumes available JIRA licenses
3. **Permission Requirements**: User creation requires JIRA administrator permissions
4. **Domain Restrictions**: Some organizations restrict user creation to specific email domains
5. **Invitation Workflow**: Users receive invitation emails to activate their accounts

## Limitations

- **License Limits**: User creation is limited by available JIRA licenses
- **Domain Restrictions**: Email domains may be restricted by organization policy
- **Bulk Operations**: Only one user can be created per request
- **Permission Requirements**: Requires JIRA administrator or user management permissions
- **Email Delivery**: Invitation emails depend on JIRA's email configuration

## Best Practices

1. **Email Validation**: Verify email addresses are correct before creation
2. **Display Names**: Provide meaningful display names for better user identification
3. **License Management**: Monitor license usage when creating multiple users
4. **Permission Planning**: Plan user permissions and group assignments in advance
5. **Communication**: Inform users about invitation emails and account activation
6. **Bulk Processing**: For multiple users, consider batch processing with email disabled

## Common Use Cases

### New Employee Onboarding
1. HR provides new employee email and details
2. Create JIRA user account with appropriate display name
3. Send invitation email for account activation
4. Follow up with group assignments and project access
5. Provide training on JIRA usage and workflows

### Contractor Access Management
1. Identify contractors requiring JIRA access
2. Create temporary user accounts with contractor email addresses
3. Set up limited permissions and project access
4. Monitor license usage for cost management
5. Deactivate accounts when contracts end

### Team Expansion
1. Plan for new team members joining projects
2. Create user accounts in advance of start dates
3. Prepare group memberships and project assignments
4. Coordinate with team leads for access requirements
5. Ensure proper onboarding documentation

### External Stakeholder Access
1. Identify external users needing JIRA visibility
2. Create limited access accounts for stakeholders
3. Configure appropriate permission levels
4. Provide training on relevant JIRA features
5. Monitor and review external access regularly

## Related Catalog Requests

- [Update User](pages/UpdateUser.md) - Add users to groups and manage permissions
- [Delete User](pages/DeleteUser.md) - Remove users from JIRA workspace
- [Get Users Account Id](pages/GetUsersAccountId.md) - Retrieve user account identifiers
- [Create an Issue](pages/CreateAnIssue.md) - Assign issues to newly created users

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.UserDao`
- **Validation Flow**: Email validation → Uniqueness check → Permission verification → User creation → Invitation sending
- **Service Delegation**: Delegates to JIRA REST API v3 user management endpoints
- **Telemetry Metrics**: Tracks user creation success rates, invitation delivery, and license usage

## Troubleshooting

### User Creation Fails
**Problem**: User creation returns error or fails silently
**Solution**:
1. Verify email format is correct and valid
2. Check if user already exists with that email
3. Ensure you have user management permissions
4. Verify JIRA license availability

### Permission Denied Errors
**Problem**: Cannot create users due to permission restrictions
**Solution**:
1. Contact JIRA administrator to grant user management permissions
2. Verify your account has admin privileges
3. Check organization policies for user creation
4. Ensure you're not restricted to specific domains

### Invitation Email Not Sent
**Problem**: User created but invitation email not received
**Solution**:
1. Check JIRA email configuration
2. Verify email address is correct and accessible
3. Check spam/junk folders
4. Contact JIRA administrator about email delivery
5. Manually send invitation through JIRA interface

### License Limit Errors
**Problem**: Cannot create user due to license restrictions
**Solution**:
1. Check available JIRA licenses in admin panel
2. Contact administrator about license upgrades
3. Consider deactivating unused accounts
4. Plan user creation based on license availability

## See Also

- [Update User](pages/UpdateUser.md) - Manage user groups and permissions
- [Delete User](pages/DeleteUser.md) - Remove users from workspace
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
