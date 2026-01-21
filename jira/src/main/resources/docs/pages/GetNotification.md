# Get Notification

## Overview

Wait for and retrieve real-time notifications from JIRA about issue changes, comments, and other events through webhook-based event handling.

## Request Details

- **Area**: Rituals
- **Type**: WAIT_FOR_EVENT
- **Retry Support**: ✅ Yes (with event validation and timeout handling)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| eventName | Text | Yes | Name of the JIRA event to wait for | `jira_issue_updated`, `jira_issue_created`, `jira_comment_added` |
| eventData | FreeForm | No | Additional event filtering criteria or configuration | `{"projectKey": "DEMO", "issueType": "Bug"}` |
| timeout | Number | No | Maximum time to wait for event in seconds (default: 300, max: 3600) | `600` |

### Parameter Details

**eventName**
- Specifies the type of JIRA event to wait for
- Must match configured webhook event types
- Common events: `jira_issue_updated`, `jira_issue_created`, `jira_issue_deleted`, `jira_comment_added`
- Case-sensitive matching with webhook configuration

**eventData**
- Optional JSON object for event filtering
- Can include project keys, issue types, user filters
- Format: `{"field": "value", "field2": "value2"}`
- Used to filter which events trigger the notification

**timeout**
- Maximum wait time in seconds before request times out
- Range: 1-3600 seconds (1 hour maximum)
- Default: 300 seconds (5 minutes)
- Request returns with timeout error if no matching event occurs

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Event Details | FreeForm | Complete details of the JIRA event that triggered the notification | Event object with issue details and change information |

### Event Details Structure

The returned Event Details contain:
- `eventType`: Type of event that occurred
- `timestamp`: When the event occurred
- `user`: User who triggered the event
- `issue`: Complete issue details
- `changes`: Specific changes made (for update events)
- `comment`: Comment details (for comment events)
- `webhookEvent`: Raw webhook event data

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event name required | Required field is missing. Please provide a value for this field to continue. | Provide valid event name |
| Event name valid | The specified event name is not supported. Please use a valid JIRA webhook event name. | Use supported event name from webhook configuration |
| Timeout range | The field value is invalid. Please check the format and requirements for this field. | Use timeout value between 1 and 3600 seconds |
| Webhook configured | Webhook notifications are not configured for this JIRA instance. Please contact your administrator. | Configure JIRA webhooks for real-time notifications |
| Event data format | Invalid event data format. Please provide proper JSON structure. | Use valid JSON format for event filtering |

## Error Handling

### Input Errors (INPUT_ERROR)
- **Cause**: Missing event name or invalid timeout value
- **Error Message**: "Required field is missing. Please provide a value for this field to continue."
- **Common Scenarios**: Empty event name, invalid timeout range, malformed event data
- **Resolution**: Provide valid event name and timeout parameters

### Logic Errors (LOGIC_ERROR)
- **Cause**: Unsupported event name or webhook configuration issues
- **Error Message**: "The specified event name is not supported. Please use a valid JIRA webhook event name."
- **Common Scenarios**: Invalid event names, webhook not configured, event filtering issues
- **Resolution**: Use supported event names and verify webhook configuration

### System Errors (SYSTEM_ERROR)
- **Cause**: Webhook system unavailable or event processing issues
- **Error Message**: "Unable to process webhook events. Please check webhook configuration and try again."
- **Common Scenarios**: Webhook service issues, network problems, event processing failures
- **Resolution**: Check webhook configuration and system status

### Timeout Errors
- **Cause**: No matching event occurred within timeout period
- **Error Message**: "No matching event received within the specified timeout period."
- **Common Scenarios**: No activity matching event criteria, long wait periods
- **Resolution**: Increase timeout or adjust event filtering criteria

## Usage Examples

### Example 1: Wait for Issue Update
**Input:**
- eventName: `jira_issue_updated`
- eventData: `{"projectKey": "DEMO"}`
- timeout: `600`

**Expected Output:**
```json
{
  "Event Details": {
    "eventType": "jira_issue_updated",
    "timestamp": "2025-01-15T14:30:00.000Z",
    "user": "john.doe@company.com",
    "issue": {
      "key": "DEMO-123",
      "summary": "Fix login bug",
      "status": "In Progress"
    },
    "changes": {
      "status": {
        "from": "To Do",
        "to": "In Progress"
      }
    }
  }
}
```

### Example 2: Wait for New Issue Creation
**Input:**
- eventName: `jira_issue_created`
- eventData: `{"issueType": "Bug"}`
- timeout: `300`

**Expected Output:**
```json
{
  "Event Details": {
    "eventType": "jira_issue_created",
    "timestamp": "2025-01-15T14:35:00.000Z",
    "user": "jane.smith@company.com",
    "issue": {
      "key": "PROJ-456",
      "summary": "New critical bug",
      "status": "Open",
      "priority": "Critical",
      "issueType": "Bug"
    }
  }
}
```

### Example 3: Wait for Comment Addition
**Input:**
- eventName: `jira_comment_added`
- timeout: `180`

**Expected Output:**
```json
{
  "Event Details": {
    "eventType": "jira_comment_added",
    "timestamp": "2025-01-15T14:40:00.000Z",
    "user": "developer@company.com",
    "issue": {
      "key": "DEMO-789",
      "summary": "Feature implementation"
    },
    "comment": {
      "id": "12345",
      "body": "Implementation completed, ready for review",
      "author": "developer@company.com"
    }
  }
}
```

### Example 4: Timeout Scenario
**Input:**
- eventName: `jira_issue_deleted`
- timeout: `60`

**Expected Output:**
```json
{
  "error": "No matching event received within the specified timeout period."
}
```

**Result**: No deletion events occurred within 60 seconds

## Business Rules

1. **Real-time Processing**: Events are processed as they occur in JIRA
2. **Event Filtering**: Only events matching criteria trigger notifications
3. **Timeout Management**: Requests automatically timeout to prevent indefinite waiting
4. **Webhook Dependency**: Requires properly configured JIRA webhooks
5. **Event Ordering**: Events are processed in chronological order

## Limitations

- **Webhook Requirement**: Requires JIRA webhook configuration for real-time events
- **Network Dependency**: Depends on stable network connection between JIRA and Krista
- **Event Filtering**: Limited to webhook event types supported by JIRA
- **Timeout Constraints**: Maximum wait time limited to prevent resource exhaustion
- **Single Event**: Returns on first matching event, not multiple events

## Best Practices

1. **Webhook Configuration**: Ensure proper webhook setup for reliable event delivery
2. **Timeout Management**: Use appropriate timeout values based on expected activity
3. **Event Filtering**: Use specific event criteria to reduce noise and improve relevance
4. **Error Handling**: Implement proper timeout and error handling in workflows
5. **Resource Management**: Avoid excessive concurrent event waiting requests
6. **Monitoring**: Monitor webhook delivery and event processing success rates

## Common Use Cases

### Automated Workflow Triggers
1. **Issue Status Changes**:
   - Wait for issues to move to specific statuses
   - Trigger automated actions on status changes
   - Coordinate workflow transitions across systems
   - Implement business rule enforcement

2. **Priority Escalation**:
   - Monitor for high-priority issue creation
   - Trigger escalation workflows automatically
   - Notify stakeholders of critical issues
   - Implement SLA monitoring and alerts

### Real-time Notifications
1. **Team Coordination**:
   - Notify team members of relevant issue changes
   - Coordinate work handoffs between team members
   - Implement real-time collaboration workflows
   - Trigger communication and notification systems

2. **External Integration**:
   - Synchronize JIRA changes with external systems
   - Trigger external workflow processes
   - Maintain data consistency across platforms
   - Implement real-time reporting and analytics

### Quality Assurance
1. **Testing Triggers**:
   - Wait for development completion events
   - Trigger automated testing workflows
   - Coordinate QA and development handoffs
   - Implement continuous integration triggers

2. **Bug Tracking**:
   - Monitor for new bug reports
   - Trigger bug triage and assignment workflows
   - Implement automated bug categorization
   - Coordinate bug fix and testing cycles

### Project Management
1. **Progress Monitoring**:
   - Track project milestone completion
   - Monitor sprint progress and completion
   - Trigger project status updates
   - Implement automated progress reporting

2. **Resource Management**:
   - Monitor workload changes and assignments
   - Trigger resource reallocation workflows
   - Implement capacity planning automation
   - Coordinate team resource optimization

## Related Catalog Requests

- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve details of issues from events
- [Update an Issue](pages/UpdateAnIssue.md) - Respond to events with issue updates
- [Get Issues](pages/GetIssues.md) - Query issues based on event information
- [Create an Issue](pages/CreateAnIssue.md) - Create follow-up issues from events

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.WebhookDao`
- **Validation Flow**: Event name validation → Webhook configuration check → Event filtering setup → Event waiting → Response formatting
- **Service Delegation**: Integrates with JIRA webhook system and Krista event processing
- **Telemetry Metrics**: Tracks event delivery rates, processing times, and webhook reliability

## Troubleshooting

### No Events Received
**Problem**: Request times out without receiving expected events
**Solution**:
1. Verify JIRA webhook configuration is active
2. Check webhook URL and authentication
3. Ensure events are actually occurring in JIRA
4. Verify event filtering criteria are not too restrictive

### Webhook Configuration Issues
**Problem**: Webhook events are not being delivered
**Solution**:
1. Check JIRA webhook configuration and URL
2. Verify webhook authentication and permissions
3. Test webhook delivery using JIRA webhook test feature
4. Contact JIRA administrator for webhook troubleshooting

### Event Filtering Problems
**Problem**: Receiving too many or wrong events
**Solution**:
1. Refine event filtering criteria in eventData
2. Use more specific project or issue type filters
3. Verify event name matches intended event type
4. Test event filtering with known event scenarios

### Timeout Issues
**Problem**: Requests timeout too quickly or take too long
**Solution**:
1. Adjust timeout value based on expected event frequency
2. Use shorter timeouts for high-frequency events
3. Implement retry logic for timeout scenarios
4. Monitor event patterns to optimize timeout values

## See Also

- [Webhook Configuration](pages/WebhookConfiguration.md) - Setup real-time notifications
- [Get Issue By Key](pages/GetIssueByKey.md) - Process issues from events
- [Update an Issue](pages/UpdateAnIssue.md) - Respond to events
- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
