# Delete User

## Overview

Permanently remove a JIRA user from your workspace. This action requires admin privileges and cannot be undone.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and confirmation)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Account Id | Text | Yes | Unique account ID of the user you want to permanently delete from JIRA | `5d7c8a9b1234567890abcdef` |

### Parameter Details

**Account Id**
- Must be a valid JIRA account ID (not username or email)
- User must exist in the JIRA instance
- Can be obtained using [Get Users Account Id](pages/GetUsersAccountId.md) request
- Format: 24-character alphanumeric string
- **Warning**: Deletion is permanent and cannot be undone

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Deleted | Boolean | Confirmation indicating whether the user was successfully deleted from JIRA | `true` |

### Output Details

**Deleted**
- Returns `true` if user was successfully deleted from JIRA
- Returns `false` if deletion failed or user was not found
- Successful deletion removes user from all projects, groups, and assignments

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Account ID format | The field value is invalid. Please check the format and requirements for this field. | Use valid 24-character JIRA account ID |
| User exists | The specified user could not be found in JIRA. Please verify the username or email address is correct. | Verify account ID exists and is active |
| Admin permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request user management permissions |
| Self-deletion | Cannot delete your own user account. Please contact another administrator. | Use different administrator account |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide valid account ID |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing account ID or invalid format
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty account ID, invalid ID format
- **Resolution**: Provide valid 24-character account ID

### Logic Errors (LOGIC_ERROR)
- **Cause**: User not found, self-deletion attempt, or business rule violations
- **Error Message**: "Failed to delete the user from JIRA. Please ensure you have user management permissions and try again."
- **Common Scenarios**: User doesn't exist, trying to delete own account, user has dependencies
- **Resolution**: Verify user existence and check business rules

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or deletion processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Insufficient permissions for user management
- **Error Message**: "You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions."
- **Common Scenarios**: Missing admin permissions, restricted user management
- **Resolution**: Request user management permissions from JIRA administrator

## Usage Examples

### Example 1: Delete Former Employee Account
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`

**Expected Output:**
```json
{
  "Deleted": true
}
```

**Result**: User account permanently removed from JIRA workspace

### Example 2: Delete Test Account
**Input:**
- Account Id: `5d7c8a9b1234567890fedcba`

**Expected Output:**
```json
{
  "Deleted": true
}
```

**Result**: Test user account successfully deleted

### Example 3: Delete Contractor Account
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`

**Expected Output:**
```json
{
  "Deleted": true
}
```

**Result**: Contractor account removed after contract completion

### Example 4: Retry Scenario - User Not Found
**Input:**
- Account Id: `5d7c8a9b1234567890invalid`

**Expected Output:**
```json
{
  "error": "The specified user could not be found in JIRA. Please verify the username or email address is correct."
}
```

**Result**: User prompted to verify account ID

## Business Rules

1. **Permanent Action**: User deletion cannot be undone - all data is permanently removed
2. **Admin Requirements**: Only JIRA administrators can delete users
3. **Self-Deletion Prevention**: Users cannot delete their own accounts
4. **License Recovery**: Deleting users frees up JIRA licenses for reuse
5. **Data Cleanup**: All user associations (assignments, comments, history) are handled per JIRA policy

## Limitations

- **Irreversible Action**: Deleted users cannot be recovered
- **Data Dependencies**: Some user data may be preserved for audit purposes
- **Bulk Operations**: Only one user can be deleted per request
- **Admin Only**: Requires JIRA administrator permissions
- **Active Sessions**: User sessions may remain active until expiration

## Best Practices

1. **Confirmation Process**: Always confirm user deletion with stakeholders before proceeding
2. **Data Backup**: Consider exporting user data before deletion if needed
3. **Issue Reassignment**: Reassign user's issues before deletion to prevent orphaned work
4. **Access Review**: Review and document user's access and responsibilities
5. **License Management**: Track license recovery for cost management
6. **Audit Documentation**: Document deletion reasons for compliance

## Common Use Cases

### Employee Offboarding
1. **Preparation Phase**:
   - Identify all issues assigned to departing employee
   - Reassign critical issues to other team members
   - Export any necessary user data or reports
   - Document user's project roles and responsibilities

2. **Deletion Process**:
   - Use this request to permanently delete user account
   - Verify successful deletion
   - Update team documentation and contact lists
   - Notify relevant stakeholders of account removal

### Contractor Access Cleanup
1. **Contract Completion**:
   - Review contractor's work and issue assignments
   - Transfer ownership of any ongoing work
   - Export project deliverables and documentation
   - Remove contractor from project communications

2. **Account Removal**:
   - Delete contractor account to free up licenses
   - Update project team lists and permissions
   - Document contractor contributions for future reference

### Test Account Management
1. **Development Cleanup**:
   - Identify test accounts no longer needed
   - Verify test accounts have no production data
   - Clean up test issues and projects
   - Remove test accounts to maintain clean environment

2. **License Optimization**:
   - Regular cleanup of unused test accounts
   - Monitor license usage and optimize costs
   - Maintain minimal necessary test accounts

### Security Incident Response
1. **Immediate Response**:
   - Identify compromised or suspicious accounts
   - Disable account access immediately
   - Review account activity and permissions
   - Document security incident details

2. **Account Removal**:
   - Delete compromised accounts permanently
   - Create new accounts with fresh credentials if needed
   - Update security procedures and monitoring
   - Report incident per organizational policy

## Related Catalog Requests

- [Create User](pages/CreateUser.md) - Create replacement user accounts
- [Update User](pages/UpdateUser.md) - Modify user permissions before deletion
- [Get Users Account Id](pages/GetUsersAccountId.md) - Find account IDs for deletion
- [Update an Issue](pages/UpdateAnIssue.md) - Reassign issues before user deletion

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.UserDao`
- **Validation Flow**: Account ID validation → User existence check → Permission verification → Self-deletion check → Deletion execution
- **Service Delegation**: Delegates to JIRA REST API v3 user management endpoints
- **Telemetry Metrics**: Tracks deletion success rates, license recovery, and user lifecycle management

## Troubleshooting

### User Not Found Errors
**Problem**: Account ID is not recognized or user doesn't exist
**Solution**:
1. Verify account ID format is correct (24-character string)
2. Use [Get Users Account Id](pages/GetUsersAccountId.md) to find correct ID
3. Check if user was already deleted
4. Ensure user exists in the JIRA instance

### Permission Denied Errors
**Problem**: Cannot delete user due to permission restrictions
**Solution**:
1. Contact JIRA administrator to grant user management permissions
2. Verify your account has admin privileges
3. Check organizational policies for user deletion
4. Ensure you're not trying to delete your own account

### Self-Deletion Prevention
**Problem**: Cannot delete own user account
**Solution**:
1. Use different administrator account for deletion
2. Contact another JIRA administrator
3. Follow organizational procedures for admin account changes
4. Consider account deactivation instead of deletion

### Deletion Fails with Dependencies
**Problem**: User deletion fails due to data dependencies
**Solution**:
1. Reassign all issues currently assigned to the user
2. Transfer ownership of projects and components
3. Update workflow assignments and permissions
4. Contact JIRA administrator about dependency resolution

## See Also

- [Create User](pages/CreateUser.md) - Create replacement user accounts
- [Update User](pages/UpdateUser.md) - Modify user permissions
- [Get Users Account Id](pages/GetUsersAccountId.md) - Find account IDs
- [Update an Issue](pages/UpdateAnIssue.md) - Reassign user issues
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
