# AI Assistant

## Overview

The **AI Assistant** tab provides a natural language interface to Jira. Instead of manually constructing JQL queries or browsing Jira directly, you can ask questions in plain English and the assistant will translate them to JQL, execute the search, and return the results.

The AI Assistant is powered by Claude and uses the same authentication credentials configured in the **Authentication** tab. No additional setup is required beyond having a working Jira connection.

---

## Accessing the AI Assistant

The AI Assistant is available as a dedicated tab in the Jira Project Management Extension:

1. Open the Jira Extension in Krista
2. Click the **AI Assistant** tab in the top navigation

The tab loads the MCP (Model Context Protocol) setup interface, which connects to Jira using your configured credentials.

---

## How It Works

When you submit a question, the assistant follows a two-step process:

```
User submits a natural language question
  → Claude generates a JQL query from the question
  → Extension executes the JQL query against the Jira Cloud API
  → Results are returned and formatted

If the question is about a specific attribute (e.g. priority of a ticket):
  → Claude extracts and answers the specific field from the results
  → Returns a direct one-sentence answer

If the question asks for a list of issues:
  → Returns a formatted table of matching issues
```

### Query Modes

| Mode | When Used | Example Question | Output |
|------|-----------|-----------------|--------|
| **Specific** | Question about a single field or attribute of a known issue | "What is the priority of KE-3337?" | Direct answer: "The priority of KE-3337 is High." |
| **List** | Question returning multiple issues | "Show my open bugs" | Table of matching issues |

---

## Example Questions

The following examples illustrate the types of questions the AI Assistant supports:

### Specific Field Queries

| Question | Generated JQL | Response |
|----------|--------------|---------|
| What is the priority of KE-3337? | `key = KE-3337` | The priority of KE-3337 is High. |
| Who is assigned to KE-100? | `key = KE-100` | KE-100 is assigned to Jane Smith. |
| What is the status of KE-500? | `key = KE-500` | The status of KE-500 is In Progress. |
| What type of issue is KE-200? | `key = KE-200` | KE-200 is a Bug. |

### List Queries

| Question | Generated JQL |
|----------|--------------|
| Show all tickets assigned to me | `assignee = currentUser()` |
| Show my backlog issues | `assignee = currentUser() AND status = Backlog` |
| Show open bugs assigned to John Doe | `assignee = "John Doe" AND issuetype = Bug AND status != Done` |
| Find all high priority issues in my project | `priority = High AND project = MYPROJECT` |
| Show all issues in progress | `status = "In Progress"` |

---

## Result Columns

When the assistant returns a list of issues, the results are displayed in a table with the following columns:

| Column | Type | Description |
|--------|------|-------------|
| Key | Link | Issue key (e.g. KE-3337) — links directly to the issue in Jira |
| Type | Pill | Issue type (e.g. Bug, Task, Story) |
| Summary | Text | Issue title / summary |
| Status | Pill | Current workflow status (e.g. In Progress, Done) |
| Priority | Pill | Issue priority (e.g. High, Medium, Low) |
| Assignee | Text | Display name of the assigned user |

---

## Authentication

The AI Assistant uses the same credentials configured in the **Authentication** tab. Authentication must be set up and validated before the AI Assistant will work.

### Supported Authentication Modes

| Auth Mode | AI Assistant Support |
|----------|---------------------|
| Quick Setup | Yes |
| OAuth 2.0 — User Authentication | Yes |
| OAuth 2.0 — Server to Server | Yes (shared service account) |
| Basic Auth | Yes (shared API token) |

> **Note**: For Quick Setup and OAuth 2.0 — User Authentication, each user may be prompted to sign in with their own Atlassian account via a browser popup on first use. See [Authentication](pages/Authentication.md) for details on per-user MCP authentication.

---

## Catalog Request: Ask Agent

The AI Assistant is also exposed as a catalog request for use in automated workflows and Krista integrations.

| Property | Value |
|----------|-------|
| **Catalog Request Name** | Ask Agent |
| **Area** | MCP |
| **Domain** | Development |
| **Type** | Query System |

### Input

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| Query | Paragraph | Yes | A natural language question about your Jira issues |

### Output

| Field | Type | Description |
|-------|------|-------------|
| Response | FreeForm | List of Jira issues matching the query, or a direct answer for specific field questions |

### Response Fields (per issue)

| Field | Description |
|-------|-------------|
| `key` | Issue key (e.g. KE-3337) |
| `issueType` | Issue type name |
| `summary` | Issue summary |
| `status` | Status name |
| `priority` | Priority name |
| `assignee` | Assignee display name |
| `url` | Direct link to the issue in Jira |

---

## JQL Generation Rules

The AI Assistant follows these rules when converting questions to JQL:

- Uses standard Jira Cloud JQL syntax
- String values are wrapped in double quotes (e.g. `status = "In Progress"`)
- For the current user, always uses `currentUser()` — never quotes it
- Issue keys (e.g. KE-3337) do not require quotes
- Assignee matching requires the exact full display name for other users
- If your email prefix matches a name mentioned in the question (e.g. email `simran.sethi@company.com` and name `simran`), the assistant uses `assignee = currentUser()` automatically

---

## Limitations

- Results are limited to the fields fetched by the query (summary, status, assignee, priority, issue type, project). Custom fields are not returned in standard list queries.
- The assistant cannot create, update, or delete issues — it is read-only. Use the dedicated catalog requests (Create an Issue, Update an Issue, etc.) for write operations.
- Complex multi-project or cross-board queries may produce unexpected JQL. Review the results and use [Fetch by JQL Query](pages/FetchByJQLQuery.md) for precise control.
- Assignee name matching is case-sensitive and requires the exact Atlassian display name.

---

## Troubleshooting

### "No results found" for a valid question

**Cause**: The generated JQL may not match your Jira project structure or field values.

**Fix**:
1. Try rephrasing the question with more specific field values (e.g. exact status names like "In Progress" instead of "active")
2. Use [Fetch by JQL Query](pages/FetchByJQLQuery.md) to test the JQL directly
3. Check the [JQL Query Guide](pages/JQLQueryGuide.md) for correct syntax

### "Authentication required" / Authorization popup appears

**Cause**: The current user has not authenticated with Jira for MCP use.

**Fix**: Follow the prompts to sign in with your Atlassian account via the browser popup. See [Authentication — MCP Authentication](pages/Authentication.md#mcp-authentication-per-user-oauth) for details.

### "Connection error" or "Jira API error"

**Cause**: Credentials may be expired or the Jira instance is unreachable.

**Fix**:
1. Go to the **Authentication** tab
2. Click **Test Connection** to verify and refresh credentials
3. Click **Save Changes** to persist the updated tokens

### Results show wrong user's issues

**Cause**: The query is using the operator's shared token instead of a per-user token.

**Fix**: Ensure the authentication mode is Quick Setup or OAuth 2.0 — User Authentication. Server to Server and Basic Auth use a single shared credential for all requests.

---

## See Also

- [Authentication](pages/Authentication.md) — Configure Jira credentials
- [Fetch by JQL Query](pages/FetchByJQLQuery.md) — Direct JQL query execution
- [JQL Query Guide](pages/JQLQueryGuide.md) — JQL syntax reference
- [Get Results for JQL Query](pages/GetResultsForFetchIssuesByJQLQuery.md) — Retrieve results from a previously executed JQL query
