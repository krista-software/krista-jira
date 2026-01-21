# Update User

## Overview

Add a JIRA user to specified groups to grant them appropriate permissions and access levels within your JIRA workspace.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Account Id | Text | Yes | Unique account ID of the user you want to add to groups | `5d7c8a9b1234567890abcdef` |
| Group Names | Text | Yes | Comma-separated list of group names to add the user to | `developers,project-managers,jira-users` |

### Parameter Details

**Account Id**
- Must be a valid JIRA account ID (not username or email)
- User must exist in the JIRA instance
- Can be obtained using [Get Users Account Id](pages/GetUsersAccountId.md) request
- Format: 24-character alphanumeric string

**Group Names**
- Comma-separated list of existing JIRA groups
- Group names are case-sensitive
- Groups must exist in the JIRA instance
- User performing the operation must have permission to manage these groups
- Common groups: jira-users, jira-administrators, developers, project-managers

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| User Added | Boolean | Confirmation indicating whether the user was successfully added to the specified groups | `true` |

### Output Details

**User Added**
- Returns `true` if user was successfully added to all specified groups
- Returns `false` if operation failed or user was already in all groups
- Partial success scenarios are treated as failures for consistency

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Account ID format | The field value is invalid. Please check the format and requirements for this field. | Use valid 24-character JIRA account ID |
| User exists | The specified user could not be found in JIRA. Please verify the username or email address is correct. | Verify account ID exists and is active |
| Group exists | The specified group could not be found in JIRA. Please verify the group name is correct. | Check group names exist and are spelled correctly |
| Group permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request group management permissions |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide both account ID and group names |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid account ID format
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty account ID, missing group names, invalid ID format
- **Resolution**: Provide valid account ID and group names

### Logic Errors (LOGIC_ERROR)
- **Cause**: User not found, groups don't exist, or user already in groups
- **Error Message**: "Failed to update the user in JIRA. Please verify you have user management permissions and that all information is valid."
- **Common Scenarios**: Invalid account ID, non-existent groups, permission issues
- **Resolution**: Verify user and group existence, check permissions

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or group management processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Insufficient permissions for group management
- **Error Message**: "You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions."
- **Common Scenarios**: Missing admin permissions, restricted group management
- **Resolution**: Request group management permissions from JIRA administrator

## Usage Examples

### Example 1: Add User to Development Groups
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`
- Group Names: `developers,jira-users,project-alpha`

**Expected Output:**
```json
{
  "User Added": true
}
```

**Result**: User successfully added to developers, jira-users, and project-alpha groups

### Example 2: Add User to Administrative Groups
**Input:**
- Account Id: `5d7c8a9b1234567890fedcba`
- Group Names: `jira-administrators,project-managers`

**Expected Output:**
```json
{
  "User Added": true
}
```

**Result**: User granted administrative and project management permissions

### Example 3: Add User to Single Group
**Input:**
- Account Id: `5d7c8a9b1234567890abcdef`
- Group Names: `qa-team`

**Expected Output:**
```json
{
  "User Added": true
}
```

**Result**: User added to QA team group for testing permissions

### Example 4: Retry Scenario - Invalid Account ID
**Input:**
- Account Id: `invalid-account-id`
- Group Names: `developers`

**Expected Output:**
```json
{
  "error": "The field value is invalid. Please check the format and requirements for this field."
}
```

**Result**: User prompted to provide valid account ID format

## Business Rules

1. **Group Membership**: Users can be members of multiple groups simultaneously
2. **Permission Inheritance**: Users inherit all permissions from assigned groups
3. **Duplicate Prevention**: Adding user to groups they're already in has no effect
4. **Admin Requirements**: Group management requires JIRA administrator permissions
5. **Audit Trail**: All group membership changes are logged in JIRA audit logs

## Limitations

- **Bulk Operations**: Only one user can be updated per request
- **Group Creation**: Cannot create new groups through this request
- **Permission Restrictions**: Limited by administrator's group management permissions
- **Group Removal**: This request only adds users to groups, cannot remove them
- **License Impact**: Adding users to certain groups may consume additional licenses

## Best Practices

1. **Permission Planning**: Plan group assignments based on user roles and responsibilities
2. **Least Privilege**: Grant minimum necessary permissions for user functions
3. **Group Organization**: Use consistent group naming conventions
4. **Regular Reviews**: Periodically review and audit group memberships
5. **Documentation**: Maintain documentation of group purposes and permissions
6. **Testing**: Test group permissions in non-production environment first

## Common Use Cases

### New Employee Onboarding
1. Create user account using [Create User](pages/CreateUser.md)
2. Determine appropriate groups based on role and responsibilities
3. Add user to base groups (jira-users) and role-specific groups
4. Verify user can access required projects and functions
5. Provide training on JIRA usage and workflows

### Role Change Management
1. Identify user's new role and required permissions
2. Add user to new role-appropriate groups
3. Remove from previous role groups (separate process)
4. Update project assignments and permissions
5. Notify user of permission changes

### Project Team Assignment
1. Identify project-specific groups and permissions
2. Add team members to project groups
3. Verify access to project resources and workflows
4. Set up project-specific permissions and roles
5. Coordinate with project managers on access requirements

### Contractor Access Management
1. Create temporary contractor accounts
2. Add to limited permission groups
3. Grant access to specific projects only
4. Monitor and review contractor permissions regularly
5. Remove access when contracts end

## Related Catalog Requests

- [Create User](pages/CreateUser.md) - Create users before adding to groups
- [Delete User](pages/DeleteUser.md) - Remove users from workspace
- [Get Users Account Id](pages/GetUsersAccountId.md) - Retrieve account IDs for group assignment
- [Create an Issue](pages/CreateAnIssue.md) - Assign issues to users with appropriate permissions

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.UserDao`
- **Validation Flow**: Account ID validation → User existence check → Group validation → Permission verification → Group assignment
- **Service Delegation**: Delegates to JIRA REST API v3 group management endpoints
- **Telemetry Metrics**: Tracks group assignment success rates, permission changes, and user management patterns

## Troubleshooting

### User Not Found Errors
**Problem**: Account ID is not recognized or user doesn't exist
**Solution**:
1. Verify account ID format is correct (24-character string)
2. Use [Get Users Account Id](pages/GetUsersAccountId.md) to find correct ID
3. Check if user account is active and not suspended
4. Ensure user exists in the JIRA instance

### Group Not Found Errors
**Problem**: Specified groups don't exist in JIRA
**Solution**:
1. Verify group names are spelled correctly (case-sensitive)
2. Check group exists in JIRA admin interface
3. Ensure you have permission to view the group
4. Contact JIRA administrator to create missing groups

### Permission Denied Errors
**Problem**: Cannot add user to groups due to permission restrictions
**Solution**:
1. Contact JIRA administrator to grant group management permissions
2. Verify your account has admin privileges for target groups
3. Check if groups have restricted membership policies
4. Ensure you're not trying to add users to system groups

### Partial Success Issues
**Problem**: User added to some groups but not others
**Solution**:
1. Check which groups were successfully assigned
2. Verify permissions for failed group assignments
3. Retry with only the failed groups
4. Contact administrator about restricted groups

## See Also

- [Create User](pages/CreateUser.md) - Create users for group assignment
- [Delete User](pages/DeleteUser.md) - Remove users from workspace
- [Get Users Account Id](pages/GetUsersAccountId.md) - Find account IDs
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
