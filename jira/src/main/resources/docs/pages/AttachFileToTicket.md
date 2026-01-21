# Attach File to Ticket

## Overview

Upload a file to a specified JIRA ticket to provide additional context, documentation, or evidence related to the issue.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Ticket Id | Identifier | Yes | Unique identifier of the JIRA ticket to which the file will be attached | `DEMO-123` |
| File | File | Yes | The file to be uploaded and attached to the ticket | `screenshot.png` |

### Parameter Details

**Ticket Id**
- Must be a valid JIRA issue key format (PROJECT-NUMBER)
- The issue must exist and be accessible to the authenticated user
- User must have permission to edit the issue

**File**
- Supported file formats: Images (PNG, JPG, GIF), Documents (PDF, DOC, DOCX), Text files (TXT, CSV), Archives (ZIP, RAR)
- Maximum file size: 10MB (configurable by JIRA administrator)
- File name should not contain special characters that are invalid in JIRA

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Upload Status | Boolean | Status indicating whether the file upload was successful | `true` |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Ticket ID format | The issue key format is invalid. Issue keys should follow the format 'PROJECT-123' (e.g., 'DEMO-456'). Please check the key and try again. | Provide a valid JIRA issue key in PROJECT-NUMBER format |
| Issue exists | The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue. | Verify the issue key exists and you have access permissions |
| File size | The file is too large to upload. Please reduce the file size or contact your administrator about file size limits. | Reduce file size or compress the file |
| File format | The file format is not supported. Please use a supported file format and try again. | Use a supported file format (images, documents, text files) |
| Upload permissions | You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions. | Request edit permissions for the issue from JIRA administrator |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Invalid ticket ID format or missing file
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty ticket ID, no file selected
- **Resolution**: Provide valid ticket ID and select a file to upload

### Logic Errors (LOGIC_ERROR)
- **Cause**: Issue not found or insufficient permissions
- **Error Message**: "The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue."
- **Common Scenarios**: Typo in issue key, issue deleted, no access permissions
- **Resolution**: Verify issue key spelling and check access permissions

### System Errors (SYSTEM_ERROR)
- **Cause**: JIRA API unavailable or network issues
- **Error Message**: "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator."
- **Common Scenarios**: Network connectivity issues, JIRA server downtime
- **Resolution**: Check network connection and JIRA server status

### Authorization Errors
- **Cause**: Authentication token expired or insufficient permissions
- **Error Message**: "Your JIRA session has expired. Please re-authorize your account by clicking the 'Authorize' button and signing in again."
- **Common Scenarios**: Token expiration, permission changes
- **Resolution**: Re-authenticate and verify permissions

## Usage Examples

### Example 1: Upload Screenshot to Bug Report
**Input:**
- Ticket Id: `BUG-456`
- File: `error_screenshot.png`

**Expected Output:**
```json
{
  "Upload Status": true
}
```

**Result**: Screenshot successfully attached to bug report BUG-456

### Example 2: Upload Documentation to Story
**Input:**
- Ticket Id: `STORY-789`
- File: `requirements_document.pdf`

**Expected Output:**
```json
{
  "Upload Status": true
}
```

**Result**: Requirements document attached to story STORY-789

### Example 3: Retry Scenario - Invalid Issue Key
**Input:**
- Ticket Id: `INVALID-KEY`
- File: `document.pdf`

**Expected Output:**
```json
{
  "error": "The issue key format is invalid. Issue keys should follow the format 'PROJECT-123' (e.g., 'DEMO-456'). Please check the key and try again."
}
```

**Result**: User prompted to correct the issue key format

## Business Rules

1. **File Size Limits**: Files must not exceed the maximum size limit configured in JIRA (typically 10MB)
2. **Permission Requirements**: User must have edit permissions on the target issue
3. **Issue Accessibility**: The target issue must be visible and accessible to the authenticated user
4. **File Format Restrictions**: Only supported file formats can be uploaded as determined by JIRA configuration
5. **Attachment Limits**: JIRA may have limits on the total number of attachments per issue

## Limitations

- **File Size**: Maximum file size is determined by JIRA server configuration (typically 10MB)
- **File Types**: Supported file types are configured at the JIRA instance level
- **Concurrent Uploads**: Only one file can be uploaded per request
- **Virus Scanning**: Files may be subject to virus scanning which can delay upload completion
- **Storage Limits**: JIRA instance storage limits may prevent large file uploads

## Best Practices

1. **File Naming**: Use descriptive file names that clearly indicate the content and purpose
2. **File Size Optimization**: Compress large files or images before uploading to improve performance
3. **File Organization**: Consider using consistent naming conventions for better organization
4. **Security Considerations**: Avoid uploading sensitive information in file names or metadata
5. **Format Selection**: Use appropriate file formats (PNG for screenshots, PDF for documents)

## Common Use Cases

### Bug Reporting with Evidence
1. Create or identify a bug report issue
2. Capture screenshots or logs demonstrating the issue
3. Use this request to attach evidence files
4. Files provide visual or technical proof of the reported problem

### Documentation Attachment
1. Identify a story or task requiring additional documentation
2. Prepare relevant documents (requirements, specifications, designs)
3. Attach documents to provide comprehensive context
4. Team members can access all relevant information in one place

### Test Results and Reports
1. Execute testing activities for an issue
2. Generate test reports or capture test evidence
3. Attach test results to the corresponding issue
4. Provide clear audit trail of testing activities

## Related Catalog Requests

- [Create an Issue](pages/CreateAnIssue.md) - Create issues that can receive file attachments
- [Update an Issue](pages/UpdateAnIssue.md) - Update issues that may need additional attachments
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve issue details including attachment information

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: File validation → Issue existence check → Permission verification → Upload execution
- **Service Delegation**: Delegates to JIRA REST API v3 attachment endpoints
- **Telemetry Metrics**: Tracks upload success/failure rates, file sizes, and performance metrics

## Troubleshooting

### File Upload Fails
**Problem**: Upload status returns false or error occurs
**Solution**: 
1. Verify file size is within limits
2. Check file format is supported
3. Ensure issue key is correct and accessible
4. Verify edit permissions on the issue

### Permission Denied Errors
**Problem**: User cannot attach files to issues
**Solution**:
1. Contact JIRA administrator to verify edit permissions
2. Check if user is assigned to the project
3. Verify issue is not in a restricted status

### Large File Upload Issues
**Problem**: Large files fail to upload or timeout
**Solution**:
1. Compress files before uploading
2. Use alternative file formats with better compression
3. Contact administrator about file size limits
4. Consider splitting large files into smaller parts

## See Also

- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
