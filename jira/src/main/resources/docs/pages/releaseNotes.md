# Release Notes

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