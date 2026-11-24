# Get Issues by User Names

## Overview

Retrieve all Jira issues **assigned** to one or more people, identified by **name** rather than by Jira account ID. Provide a list of names and this request resolves each name to its Jira account(s), searches on assignee, and returns a single combined list of issues across everyone named.

Use this when you know people by their display name, full name, or username (for example *"show me the open tickets for Alice and Bob"*, *"what is John working on"*, or *"list issues assigned to these team members"*) and you do **not** already have their Jira account IDs — no prior account-ID lookup is needed.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **MCP Tool**: ✅ Yes (`tool = true`)
- **Required Scopes**: `read:jira-user` (name lookup), `read:jira-work` (issue search)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|--------|----------|-------------|---------|
| User Names | [ Text ] | Yes | List of people whose assigned issues you want, one name per element | `["Alice Smith", "Bob", "jdoe"]` |

### Parameter Details

**User Names**
- A **list** of names — supply one entry per person. A single person is still a one-element list, e.g. `["Avadh"]`.
- Each entry may be a **display name, full name, or username**; an **email address or account ID** also works, because all are matched the same way through Jira's user search.
- Matching is a **partial/contains** match, so a partial name may match several users — in that case, all matching users' issues are included.
- Do **not** pass a single comma-separated string (e.g. `"Alice, Bob"`); use a proper list with one name per element.
- Blank/empty entries are ignored. At least one name is required.

## Output Parameters

| Parameter Name | Type | Description | Example |
|----------------|------|-------------|---------|
| Issues | [ Entity(Issue) ] | Combined list of issues assigned to the matched users, ordered by priority (highest first) then due date | Array of Issue objects |

### Issue Entity Structure

Each Issue entity contains: `id`, `key` (e.g. `DEMO-123`), `summary`, `description`, `status`, `priority`, `assigneeName`, `reporterName`, `projectKey`, `issueType`, `created`, `updated`, `dueDate`, `labels`, and `components`.

## Behavior

1. **Name resolution** — Every name is resolved to its Jira account id(s) via Jira user search. Order is preserved and duplicate account ids (across overlapping names) are collapsed.
2. **Ambiguous names** — A name matching more than one Jira user contributes the issues of **all** matching users.
3. **Unmatched names** — A name that matches no user is silently skipped.
4. **No matches at all** — If none of the given names match any user, an **empty list is returned** (not an error).
5. **Assignee only** — Results are filtered on **assignee**; reporter, watcher, and creator relationships are not considered.
6. **Ordering & cap** — Issues are ordered by priority (descending) then due date, and capped at **30** issues.

Internally this builds a JQL query of the form:

```
assignee in (accountId1, accountId2, ...) order by priority DESC, duedate DESC
```

## Usage Examples

### Example 1: A single person

**Input:**
- User Names: `["Avadh"]`

**Expected Output:**
```json
{
  "Issues": [
    {
      "id": "10001",
      "key": "DEMO-123",
      "summary": "Fix login authentication bug",
      "status": "In Progress",
      "priority": "High",
      "assigneeName": "Avadh Patel",
      "projectKey": "DEMO",
      "issueType": "Bug",
      "dueDate": "2026-09-20"
    }
  ]
}
```

### Example 2: Multiple people, combined list

**Input:**
- User Names: `["Alice Smith", "Bob"]`

**Expected Output:**
```json
{
  "Issues": [
    {
      "id": "10002",
      "key": "PROJ-456",
      "summary": "Implement user dashboard",
      "status": "To Do",
      "priority": "Medium",
      "assigneeName": "Alice Smith",
      "projectKey": "PROJ",
      "issueType": "Story"
    },
    {
      "id": "10003",
      "key": "TEST-789",
      "summary": "Create automated test suite",
      "status": "Code Review",
      "priority": "Medium",
      "assigneeName": "Bob Jones",
      "projectKey": "TEST",
      "issueType": "Task"
    }
  ]
}
```

### Example 3: Ambiguous name (matches several users)

**Input:**
- User Names: `["John"]` — matches *John A* and *John B*

**Expected Output:** issues assigned to **both** John A and John B, in one combined list.

### Example 4: No matching users

**Input:**
- User Names: `["Nonexistent Person"]`

**Expected Output:**
```json
{
  "Issues": []
}
```

## Error Handling

| Type | Cause | Behavior |
|------|-------|----------|
| Empty result | No name matched any Jira user | Returns an empty `Issues` list — **not** an error |
| Authorization (MustAuthorizeException) | Per-user OAuth not yet completed for an MCP invocation | Re-thrown so the platform can drive the authorization flow |
| System error (SYSTEM_ERROR) | Jira API unavailable, network/timeout, or unexpected failure | Returns an error response: *"Error occurred while getting issues by user names"* |

## Limitations

- **Assignee only** — Does not return issues a user reported, watches, or created.
- **Result cap** — Returns at most 30 issues; no pagination inputs on this request.
- **Permission dependent** — Results are limited to issues the authenticated caller is allowed to view.
- **Partial matching** — Because names are matched with a contains search, a short name may match more users than intended and include their issues too.

## Related Catalog Requests

- [Get User Issues](pages/GetUserIssues.md) — A single user's issues when you already have their **account ID**
- [Get Users Account Id](pages/GetUsersAccountId.md) — Find a user's account ID from their email
- [Get User Account Information](pages/GetUserAccountInformation.md) — Look up full account details by email or name
- [Get Issues](pages/GetIssues.md) — Flexible issue filtering by multiple criteria
- [Fetch by JQL Query](pages/FetchByJQLQuery.md) — Run an arbitrary JQL query

## Technical Implementation

- **Handler**: `RitualsArea.getIssuesByUserNames(List<String>)`
- **Name resolution**: `UserDao.searchUser(name)` → collects every matched `accountId` (deduped, order preserved)
- **Issue search**: `IssueDao.searchByJQL(jql, 30, 1)` with an `assignee in (...)` clause
- **Telemetry**: `Jira.RitualsArea.getIssuesByUserNames.{count,success,failure}`
