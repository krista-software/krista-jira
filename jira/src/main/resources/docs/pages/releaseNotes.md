# Release Notes

## Krista Jira Extension 4.0.2 - Get Issues by User Names

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.125-sp1
- **Global Catalog Version** : GC-2026.08.1
- **Java Version** : 21
- **Release Date** : August 2026

### What's New

#### New Catalog Request — Get Issues by User Names

- New **Get issues by user names** catalog request (Area: **Rituals**, Type: **QUERY_SYSTEM**, MCP tool)
- Accepts a **list of user names** (`User Names: [ Text ]`) instead of account IDs — each name is resolved to its Jira account(s) automatically, so no prior `Get Users Account Id` lookup is needed
- Names may be a display name, full name, username, email, or account ID; matching uses Jira's user search (partial/contains match)
- Searches on **assignee only** and returns a single combined `Issues` (`[ Entity(Issue) ]`) list across everyone named, ordered by priority then due date, capped at 30
- **Behavior**: an ambiguous name (multiple matches) includes all matching users' issues; unmatched names are skipped; if no name matches any user, an **empty list** is returned instead of an error
- Registered under the `read:jira-user` and `read:jira-work` OAuth scopes
- Rich tool/input descriptions so MCP agents can select and call it correctly
- **Documentation**: [Get Issues by User Names](pages/GetIssuesByUserNames.md)

---

## Krista Jira Extension 4.0.1 - Per-User Auth Fix & Create Issue Custom Fields

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.125-sp1
- **Global Catalog Version** : GC-2026.07.5
- **Java Version** : 21
- **Release Date** : August 2026
- **Jira Ticket** : KE-3486

### What's New

#### Per-User Authentication Fix (KE-3486)
- Fixed the bug where a catalog request (e.g. **Get Issues**, **Create Issue**) run as the user stayed blocked after the user completed authorization
- `onOAuthCallbackSuccess` in `JiraAuthUiResource` now marks the **specific account** carried in the OAuth `state` authorized (`authorizationListener.authorized(accountId)`) instead of the no-arg call, so the platform resumes the exact pending request
- Falls back to the no-arg `authorized()` when the account id is blank or the account-specific call fails; the stored tokens remain valid so a manual re-invocation still succeeds

#### Create an Issue — Custom & Required Fields
- New **Custom Field** input on the **Create an Issue** catalog request, a list of `[{ Key, Value }]` entries where `Key` is the field's exact Jira display name and `Value` is the value to set
- Resolves each field name to its Jira field id and type using the project's create metadata, so text, single-/multi-select (option), date, and people custom fields all work on create — the same resolver used by Update Issue
- Supports project-required fields that differ per project/issue type (e.g. `Category` for DIR/Task; `Customer` and `Due date` for KE/Task and KE/Bug)
- No pre-emptive custom-field validation: the payload is built and Jira is the authority on which values/fields are valid. When Jira rejects a create, its own field-level errors are flattened into a clear message instead of a vague null/NPE

#### Reliable Required-Field Detection
- Create metadata is now read from the **project-scoped** endpoint `issue/createmeta/{projectKey}/issuetypes/{issueTypeId}` (paginated)
- Replaces the deprecated classic `issue/createmeta?projectKeys=` endpoint, which ignored the project filter and returned the wrong project's required fields (e.g. reporting DIR's `Category` as required for a KE issue)

#### Logging Improvements
- Replaced all `System.out` / `System.err` calls with SLF4J loggers in `IssueDao`, `RitualsArea`, `KristaMediaClient`, and `WebhookDao`
- `Create an Issue` failures now log the full exception (type + stack trace), not just the message — a null-message exception no longer hides the real cause

---

## Krista Jira Extension 2.0.31 - Atlassian Authentication SDK & MCP Support

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.125-sp1
- **Global Catalog Version** : GC-2026.05.3
- **Java Version** : 21
- **Release Date** : June 2026

### What's New

#### Authentication Tab — Atlassian SDK Integration
- Replaced the legacy attribute-screen OAuth flow with the **Atlassian Authentication SDK** (`atlassian-sdk:1.0.25`)
- New `JiraAuthUiResource` (`/sdk`) serves an embedded HTML authentication tab with three modes:
  - **Quick Setup (Krista Managed)** — pre-filled credentials; scopes shown as read-only chips
  - **OAuth 2.0 — Your Own Credentials** — client ID/secret entry with a toggleable scope panel (clickable chips)
  - **Basic Auth** — username, API token, and host URL
- Test Connection and Save Changes are handled by the SDK's `AbstractAtlassianAuthResource`; all credential events are audited automatically

#### MCP Support
- All catalog requests already marked with `tool=true` (MCP-exposed)
- `RestClientFactory.getInstance(invokeAsUser)` forces OAuth 2.0 (3LO) when `invokeAsUser=true` and OAuth credentials are present, triggering `MustAuthorizeException` for unauthenticated MCP users
- `onOAuthCallbackSuccess` in `JiraAuthUiResource` calls `authorizationListener.authorized()` — notifies the Krista runtime when a user completes the MCP per-user OAuth flow
- `MustAuthorizeException` is always re-thrown (never swallowed) across all catalog request methods

#### Full Audit Coverage
- All catalog requests now emit `CATALOG_REQUEST_TRIGGERED`, `CATALOG_REQUEST_SUCCESS`, `CATALOG_REQUEST_FAILURE`, and auth-specific events via `JiraAuditService`
- `JiraAuthUiResource` passes the shared `JiraAuditService` audit store to the SDK parent — credentials events (`CREDENTIALS_TEST_*`, `CREDENTIALS_SAVE_*`) appear on the same Audit Dashboard as catalog request events
- `JiraAuditResource` (`/sdk/audit`) serves the SDK-driven audit dashboard with credential health summary

#### Scope Documentation
- `JiraExtensionConfig` declares 5 `@OAuthScope` annotations mapping all catalog requests to their required Jira API scopes:
  - `read:jira-user` → Get Users Account Id, Get User Account Information
  - `read:jira-work` → 9 read catalog requests
  - `write:jira-work` → 10 write catalog requests
  - `manage:jira-data-provider` → Get Notification, Check System Health, Save Extension Configuration
  - `manage:jira-project` → Get release summary, Assign Issue to Epic, Assign Child Issue to Epic

#### Documentation Updates
- Completely rewritten **Authentication** guide following the Outlook 4.0 format
- New sections: Krista Managed (Quick Setup), OAuth 2.0 scope panel, MCP per-user auth flow, Token Management, Audit Dashboard, Security Best Practices

---

## Krista Jira Extension 2.0.30 - JQL Query Resilience Improvements

- **Developer Name** : Ramgopal Patidar
- **Krista Service APIs Java** : 1.0.125
- **Global Catalog Version** : GC-2026.05.3
- **Java Version** : 21
- **Release Date** : May 2026

### What's New

#### Fetch by JQL Query — Re-entry Flow Removed
- Removed the human-in-the-loop re-entry flow (`confirmReenterJQLQuery` / `handleReenterJQLQuery`) from the **Fetch by JQL Query** catalog request
- Invalid or failed JQL queries now execute directly; on failure the error is logged at `ERROR` level and an empty issue list is returned to the workflow — no human prompt, no indefinite wait
- Eliminates the previous issue where a wrong JQL query could cause the `Get Results for Fetch Issues by JQL Query` (`WAIT_FOR_EVENT`) step to hang for up to 1 hour waiting for human re-entry input

---

## Krista Jira Extension 2.0.29 - Connection Test & Error Handling Improvements (KE-3136)

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.124
- **Global Catalog Version** : GC-2026.04.04
- **Java Version** : 21
- **Release Date** : April 2026

### What's New

#### Connection Test Improvements
- Specific error messages are now passed through per HTTP status code instead of a generic error:
    - **401**: "Admin Auth Token is invalid or expired. Please regenerate your API token from Atlassian account settings."
    - **403**: "Access denied. The account does not have permission to perform this operation."
    - **404**: "Please Provide Valid Host URL"
    - **400**: "Please Provide Valid Auth Token OR User Name"
- `MustAuthorizeException` is properly propagated for OAuth authorization flows

#### Error Handling in AuthRestClient
- Added dedicated catch blocks for HTTP 401 and 403 status codes in `AuthRestClient.fetch()` with clear, specific error messages
- Each HTTP error status now returns a distinct message to help users identify the exact issue

---

## Krista Jira Extension 2.0.28 - Centralized JQL Search Engine (KE-3136)

- **Developer Name** : Ganesh Gawali
- **Krista Service APIs Java** : 1.0.124
- **Global Catalog Version** : GC-2026.04.02
- **Java Version** : 21
- **Release Date** : April 2026

### What's New

#### 🔍 Centralized JQL Query Translation Engine

##### New Utility Classes (2)
1. **JqlFieldMapping** - Centralized registry mapping 60+ entity field names to Jira JQL field names and types. Supports 6 field types: TEXT, FULL_TEXT, EXACT, COMPARABLE, DATE, IDENTITY. Includes entity-specific aliases (e.g., "bug key"/"epic key"/"story key" all map to JQL "key").
2. **SearchConditionTranslator** - Converts SearchCondition/QueryClause objects into valid JQL query strings with type-aware operator mapping, automatic IN/NOT IN clause generation for multi-value conditions, JQL value escaping, and epoch-to-JQL date conversion.

##### New Shared Service
3. **EntityImplementationService** - Abstract base class providing common functionality for Bug, Story, Task, Epic, Subtask implementations. Handles user field resolution (display names → Jira account IDs), common search helpers, and validation.

##### New Jira Response Parser
4. **JiraIssue** - Parses Jira API JSON responses including complex ADF (Atlassian Document Format) descriptions. Handles paragraphs, code blocks, lists, tables, mentions, status badges, and dates — all converted to plain text.

##### New Utility: IssueMapper
5. **IssueMapper** - Bidirectional mapper between generic Issue and type-specific entities (Epic, Task, Bug, Story, Subtask). Preserves entity-specific field naming conventions.

#### Entity Request Impls Refactored (6 files)
BugImpl, EpicImpl, IssueImpl, StoryImpl, SubtaskImpl, TaskImpl — all refactored from hand-rolled per-entity JQL building to using `SearchConditionTranslator.toJql()` with shared `EntityImplementationService` base class.

#### Entity Enrichment (14 entities)
* Added `description` attributes on `@Entity` and every `@Field` — documenting relationships and cross-entity references
* Added `parentKey`, `createdDate`, `lastModifiedDate` fields to Bug, Story, Task, Subtask
* Bug entity: `supportStore` changed from `false` to `true`
* Updated `toFields()` methods to include new fields

#### 🔧 IssueDao Pagination Fix
* `searchByJQL()` refactored from offset-based to cursor-based pagination using Jira's `nextPageToken`/`isLast` API
* Exposed `getUserDao()` getter for user field resolution

#### 🧪 Test Coverage
* **JqlFieldMappingTest** — 52 tests covering field types, entity-specific aliases, case insensitivity, ORDER BY fields
* **SearchConditionTranslatorTest** — 34 tests covering all operators, field types, multi-value, escaping, end-to-end scenarios
* **DatePlusTest** — expanded with JQL date conversion tests
* **Entity tests updated** — Bug, Epic, Story, Subtask, Task tests updated for new fields

#### Utility Additions
* **DatePlus.toJqlDate()** — Converts epoch millis/ISO-8601 to JQL date format ("yyyy/MM/dd HH:mm")

### Build Changes
* **Krista API**: 1.0.121 → 1.0.124
* **Extension Version**: 2.0.27 → 2.0.28
* Removed `@ChangeLog` annotation

---

## Krista Jira Extension 2.0.27 - Create Issue Reporter Support

- **Developer Name** : Asem Abdelfattah
- **Krista Service APIs Java** : 1.0.121
- **Global Catalog Version** : GC-2026.3.4
- **Java Version** : 21

### What's New Improvement
[KE-3109]
- Added optional `Reporter Account Id` parameter to the "Create an Issue" catalog request
    - Allows specifying the issue reporter using their Atlassian account ID
    - If not provided, the authenticated user is used as the reporter (default Jira behavior)
    - Reporter value is preserved through the validation retry flow
    - Requires the "Modify Reporter" project permission in Jira when setting a different reporter
    - **Documentation**: [Create an Issue](pages/CreateAnIssue.md)

### BugFix
[KE-3109]
- Extended `HtmlToAdfConverter` to support additional HTML tags in issue descriptions
    - Added support for `<strong>`, `<em>`, `<s>`, `<del>`, `<strike>`, `<code>` inline formatting tags
    - Added support for `<a href="...">` links — converted to text nodes with ADF link marks
    - Added support for `<br>` line breaks — converted to ADF `hardBreak` nodes
    - Added support for `<h1>`–`<h6>` headings and `<blockquote>` block elements
    - Fixed unsupported HTML tags (e.g., `<span>`, `<div>`) producing malformed ADF that caused Jira `INVALID_INPUT` errors — now gracefully extracts text content instead

## Krista Jira Extension 2.0.26 - OAuth 2.0 Shared User Support

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.121
- **Global Catalog Version** : GC-2026.2.4
- **Java Version** : 21

### BugFix
[KE-3040]
- Store and retrieve OAuth refresh tokens using a client-level key, shared across users.

- **OAuth Token Storage Enhancement**
    - OAuth refresh tokens are now stored under an app-wide key (`clientId#clientSecret#invokerId`) shared across all users
    - Per-user tokens are also stored under `accountId#invokerId` when the user's state is available
    - When a user-specific token is not found, the extension falls back to the app-wide token
    - Admin authorization via Validate Attributes is now shared with all users of the extension
    - Individual users can also authenticate independently via the authorization checkbox flow

- **Comprehensive Test Coverage**
    - JiraServiceTest: 45 tests covering OAuth service, token management, accessible resources, and scope matching
    - JiraAttributesTest: 69 tests covering attribute validation, credential checking, email validation, and configuration
    - AuthenticationResourceIntegrationTest: 27 tests covering OAuth callback, webhook handling, and token exchange
    - Total: 141 tests with 100% pass rate

- **Documentation Updates**
    - Updated Authentication guide with OAuth popup flow, user authentication via checkbox, and troubleshooting
    - Updated Extension Configuration with streamlined setup instructions matching Jira Service Management format
    - Added media assets for OAuth configuration screenshots


## Krista Jira Extension 2.0.25 - Feature Enhancement & Testing

- **Developer Name** : Ramgopal Patidar
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.2.2
- **Java Version** : 21

### What's New
Catalog Request: Get Issue by Key
- Enhancement: Added seconds precision to Last Modified Date field display
- Previous Format: `Feb 12, 2026 7:38 PM`
- New Format: `Feb 12, 2026 7:38:51 PM`
- Impact: Provides more accurate timestamp information for audit and tracking purposes
- **Technical Details:**
- Updated display format from `MMM d, yyyy h:mm a to MMM d, yyyy h:mm:ss a`
- Backend already stored full precision (epoch milliseconds)


## Krista Jira Extension 2.0.24 - Feature Enhancement & Testing

- **Developer Name** : Ramgopal Patidar
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.2.2
- **Java Version** : 21

### What's New

#### Enhanced Catalog Requests

- **Get Issue by Key** - Added Last Modified Date field
    - **Field Name**: Last Modified Date
    - **Type**: Date with time of day
    - **Source**: Jira API `updated` field (ISO 8601 format)
    - **Features**:
        - Supports all timezone formats (UTC/Zulu, positive/negative offsets)
        - Handles milliseconds precision
        - Robust error handling for invalid date formats
        - Returns epoch milliseconds (Long type)
    - **Available For**: Issue entity only (returned by "Get Issue by Key" catalog request)
    - **Use Cases**:
        - Track when issues were last updated
        - Sort issues by modification time
        - Audit issue change history
        - Monitor issue activity patterns
    - **Documentation**: [Get Issue by Key](pages/GetIssueByKey.md)

## Krista Jira Extension 2.0.23 - Feature Enhancement & Testing

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.1.3
- **Java Version** : 21

### What's New

#### New Catalog Request

- **Get User Account Information** - Retrieve detailed account information for a Jira user by searching with their email
  address or name
    - **Area**: Organization
    - **Type**: QUERY_SYSTEM
    - **Input**: Search Query (email or username)
    - **Output**: Complete User entity with 7 fields (Name, Account Id, Email, Account Type, Self, Display Name,
      Application Keys)
    - **Features**:
        - Search by email address or username
        - Case-insensitive exact matching
        - Privacy-compliant (respects user email visibility settings)
        - Returns empty user object if no match found
        - Comprehensive error handling for authentication, permissions, and validation
    - **Use Cases**:
        - User lookup and verification before issue assignment
        - Retrieve Account Id for API operations
        - Validate user existence in automation workflows
        - User directory integration and synchronization
    - **Documentation**: [Get User Account Information](pages/GetUserAccountInformation.md)

---

## Krista Jira Extension 2.0.22 - Bug Fix

- **Developer Name** : Amruta Pawar
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2025.11.3
- **Java Version** : 21
- **Gradle Version** : 8.5
- **Release Date** : October 15, 2025

## Resolved Issues

- [KE-2672]: JIRA Extension: Unable to fetch Jira tickets using Fetch by JQL query request