# Assign Issue to Epic

## Overview

Link a JIRA issue to an Epic, establishing a hierarchical relationship for better project organization and tracking.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Issue Key | Text | Yes | Key of the issue you want to assign to an Epic | `DEMO-123` |
| Epic Key | Text | Yes | Key of the Epic to which the issue should be assigned | `EPIC-456` |

### Parameter Details

**Issue Key**
- Must be a valid JIRA issue key in PROJECT-NUMBER format
- Issue must exist and be accessible to the requesting user
- Issue cannot already be an Epic itself
- User must have edit permissions on the issue

**Epic Key**
- Must be a valid JIRA Epic key in PROJECT-NUMBER format
- Epic must exist and be accessible to the requesting user
- Must be an issue of type "Epic"
- User must have view permissions on the Epic

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Issue Assigned to Epic | Boolean | Confirmation indicating whether the issue was successfully assigned to the Epic | `true` |

### Output Details

**Issue Assigned to Epic**
- Returns `true` if issue was successfully linked to the Epic
- Returns `false` if assignment failed
- Successful assignment creates Epic Link relationship in JIRA

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Issue key format | The field value is invalid. Please check the format and requirements for this field. | Use valid PROJECT-NUMBER format for issue key |
| Epic key format | The field value is invalid. Please check the format and requirements for this field. | Use valid PROJECT-NUMBER format for Epic key |
| Issue exists | The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue. | Verify issue key and access permissions |
| Epic exists | The specified Epic could not be found in JIRA. Please verify the Epic key is correct and that you have permission to view this Epic. | Verify Epic key and access permissions |
| Epic type validation | The specified issue is not an Epic. Please provide a valid Epic key. | Ensure target issue is of type "Epic" |
| Issue not Epic | Cannot assign an Epic to another Epic. Please provide a non-Epic issue key. | Use regular issue (Story, Task, Bug) not Epic |
| Edit permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request edit permissions for the issue |
| Required fields | Required field is missing. Please provide a value for this field to continue. | Provide both issue key and Epic key |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing required fields or invalid key formats
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty issue/Epic keys, malformed key formats
- **Resolution**: Provide valid issue and Epic keys

### Logic Errors (LOGIC_ERROR)
- **Cause**: Issues/Epic not found, type validation failures, or permission issues
- **Error Message**: "Failed to assign the issue to the Epic. Please verify that both the issue and Epic exist and that you have the necessary permissions."
- **Common Scenarios**: Non-existent issues, invalid Epic type, insufficient permissions
- **Resolution**: Verify issue/Epic existence and check permissions

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or Epic assignment processing issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network issues, JIRA server problems, API timeouts
- **Resolution**: Check connectivity and JIRA server status

### Authorization Errors
- **Cause**: Authentication failure or insufficient permissions
- **Error Message**: "You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions."
- **Common Scenarios**: Token expiration, missing edit permissions
- **Resolution**: Re-authenticate and verify permissions

## Usage Examples

### Example 1: Assign Story to Epic
**Input:**
- Issue Key: `DEMO-123`
- Epic Key: `EPIC-456`

**Expected Output:**
```json
{
  "Issue Assigned to Epic": true
}
```

**Result**: Story DEMO-123 successfully linked to Epic EPIC-456

### Example 2: Assign Bug to Epic
**Input:**
- Issue Key: `BUG-789`
- Epic Key: `EPIC-456`

**Expected Output:**
```json
{
  "Issue Assigned to Epic": true
}
```

**Result**: Bug BUG-789 successfully linked to Epic EPIC-456

### Example 3: Assign Task to Epic
**Input:**
- Issue Key: `TASK-101`
- Epic Key: `EPIC-789`

**Expected Output:**
```json
{
  "Issue Assigned to Epic": true
}
```

**Result**: Task TASK-101 successfully linked to Epic EPIC-789

### Example 4: Retry Scenario - Epic Not Found
**Input:**
- Issue Key: `DEMO-123`
- Epic Key: `INVALID-999`

**Expected Output:**
```json
{
  "error": "The specified Epic could not be found in JIRA. Please verify the Epic key is correct and that you have permission to view this Epic."
}
```

**Result**: User prompted to verify Epic key

## Business Rules

1. **Epic Hierarchy**: Issues can only be assigned to one Epic at a time
2. **Issue Type Restrictions**: Only non-Epic issues can be assigned to Epics
3. **Permission Requirements**: User must have edit permissions on the issue and view permissions on the Epic
4. **Project Scope**: Issues and Epics can be from different projects if permissions allow
5. **Relationship Tracking**: Epic links are tracked in JIRA's issue hierarchy

## Limitations

- **Single Epic Assignment**: Issues can only be linked to one Epic at a time
- **Epic-to-Epic Restriction**: Epics cannot be assigned to other Epics
- **Permission Dependent**: Assignment limited by user's permissions on both issues
- **Project Boundaries**: Cross-project assignments may have additional restrictions
- **Bulk Operations**: Only one issue can be assigned per request

## Best Practices

1. **Epic Planning**: Plan Epic structure before assigning issues
2. **Permission Verification**: Ensure appropriate permissions before assignment
3. **Issue Organization**: Use Epic assignments for logical grouping of related work
4. **Project Coordination**: Coordinate Epic assignments across teams and projects
5. **Hierarchy Maintenance**: Regularly review and maintain Epic hierarchies
6. **Documentation**: Document Epic purposes and scope for team clarity

## Common Use Cases

### Feature Development
1. **Epic Creation**:
   - Create Epic for major feature development
   - Define Epic scope and acceptance criteria
   - Plan Epic timeline and milestones
   - Identify Epic stakeholders and owners

2. **Story Assignment**:
   - Break down Epic into user stories
   - Assign stories to the Epic for tracking
   - Organize development work under Epic umbrella
   - Track Epic progress through story completion

### Project Organization
1. **Work Categorization**:
   - Group related issues under thematic Epics
   - Organize work by business value or feature area
   - Create Epic hierarchy for complex projects
   - Maintain clear project structure

2. **Progress Tracking**:
   - Monitor Epic completion through assigned issues
   - Track Epic burndown and velocity
   - Generate Epic-based progress reports
   - Coordinate Epic delivery across teams

### Release Planning
1. **Release Scope Definition**:
   - Assign issues to release-specific Epics
   - Group features by release themes
   - Plan Epic delivery across release timeline
   - Coordinate Epic dependencies

2. **Release Tracking**:
   - Monitor release progress through Epic completion
   - Track Epic readiness for release
   - Generate release reports by Epic
   - Coordinate Epic testing and validation

### Team Coordination
1. **Cross-Team Collaboration**:
   - Assign issues from multiple teams to shared Epics
   - Coordinate Epic delivery across teams
   - Track Epic dependencies and blockers
   - Facilitate Epic-level communication

2. **Resource Planning**:
   - Plan team capacity based on Epic assignments
   - Balance Epic workload across teams
   - Identify Epic resource requirements
   - Coordinate Epic timeline and delivery

## Related Catalog Requests

- [Create an Issue](pages/CreateAnIssue.md) - Create issues to assign to Epics
- [Get Issue By Key](pages/GetIssueByKey.md) - Verify issue and Epic details
- [Update an Issue](pages/UpdateAnIssue.md) - Update Epic or issue details
- [Get Issues](pages/GetIssues.md) - Find issues for Epic assignment

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Issue key validation → Epic key validation → Issue/Epic existence check → Type validation → Permission verification → Epic link creation
- **Service Delegation**: Delegates to JIRA REST API v3 Epic link endpoints
- **Telemetry Metrics**: Tracks Epic assignment success rates, hierarchy patterns, and usage analytics

## Troubleshooting

### Issue Not Found Errors
**Problem**: Issue or Epic key is not recognized
**Solution**:
1. Verify issue/Epic key spelling and format (PROJECT-NUMBER)
2. Check if issue/Epic exists in JIRA interface
3. Ensure you have permission to view both issue and Epic
4. Verify issues haven't been deleted or moved

### Epic Type Validation Errors
**Problem**: Target Epic is not actually an Epic type
**Solution**:
1. Verify the Epic key points to an issue of type "Epic"
2. Check issue type in JIRA interface
3. Use correct Epic key if multiple similar issues exist
4. Contact project administrator about Epic configuration

### Permission Denied Errors
**Problem**: Cannot assign issue due to permission restrictions
**Solution**:
1. Contact JIRA administrator to grant edit permissions on the issue
2. Verify you have view permissions on the Epic
3. Check project-level permissions for both issues
4. Ensure issues are not in restricted statuses

### Already Assigned Issues
**Problem**: Issue is already assigned to a different Epic
**Solution**:
1. Remove existing Epic assignment first (manual process in JIRA)
2. Verify current Epic assignment in JIRA interface
3. Coordinate with Epic owners about reassignment
4. Update Epic planning and tracking accordingly

## See Also

- [Create an Issue](pages/CreateAnIssue.md) - Create issues for Epic assignment
- [Get Issue By Key](pages/GetIssueByKey.md) - Verify issue and Epic details
- [Update an Issue](pages/UpdateAnIssue.md) - Update Epic or issue information
- [Get Issues](pages/GetIssues.md) - Find issues for Epic organization
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
