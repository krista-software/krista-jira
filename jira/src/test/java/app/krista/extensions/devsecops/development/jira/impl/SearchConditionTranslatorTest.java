/*
 * JIRA Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.devsecops.development.jira.impl;

import app.krista.extension.executor.QueryClause;
import app.krista.extension.executor.SearchCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SearchConditionTranslator — converts SearchCondition/QueryClause
 * objects into Jira JQL query strings.
 */
@DisplayName("SearchConditionTranslator Tests")
class SearchConditionTranslatorTest {

    // ========== toJql() — TEXT fields (~ operator) ==========

    @Test
    @DisplayName("TEXT field with contains produces ~ operator")
    void testToJql_TextContains() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "login bug", "Summary")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("summary ~ \"login bug\""));
    }

    @Test
    @DisplayName("TEXT field with startswith produces ~ with wildcard suffix")
    void testToJql_TextStartsWith() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.startswith, "AUTH", "Summary")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("summary ~ \"AUTH*\""));
    }

    @Test
    @DisplayName("TEXT field with endswith produces ~ with wildcard prefix")
    void testToJql_TextEndsWith() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.endswith, "error", "Summary")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("summary ~ \"*error\""));
    }

    @Test
    @DisplayName("TEXT field with equals produces = operator")
    void testToJql_TextEquals() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Exact Title", "Summary")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("summary = \"Exact Title\""));
    }

    // ========== toJql() — EXACT fields ==========

    @Test
    @DisplayName("EXACT field with equals produces = with quoted value")
    void testToJql_ExactEquals() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "In Progress", "Status")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("status = \"In Progress\""));
    }

    @Test
    @DisplayName("EXACT field with notequals produces != ")
    void testToJql_ExactNotEquals() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.notequals, "Done", "Status")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("status != \"Done\""));
    }

    @Test
    @DisplayName("multiple equals on same EXACT field produces IN clause")
    void testToJql_ExactMultipleEquals_InClause() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "In Progress", "Status"),
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("status IN ("), "Should produce IN clause: " + jql);
        assertTrue(jql.contains("\"In Progress\""));
        assertTrue(jql.contains("\"Open\""));
    }

    @Test
    @DisplayName("multiple notequals on same EXACT field produces NOT IN clause")
    void testToJql_ExactMultipleNotEquals_NotInClause() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.notequals, "Done", "Status"),
                new SearchCondition(SearchCondition.Operator.notequals, "Closed", "Status")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("status NOT IN ("), "Should produce NOT IN clause: " + jql);
        assertTrue(jql.contains("\"Done\""));
        assertTrue(jql.contains("\"Closed\""));
    }

    // ========== toJql() — COMPARABLE fields ==========

    @Test
    @DisplayName("COMPARABLE field with gte produces >=")
    void testToJql_ComparableGte() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.gte, "High", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("priority >= \"High\""));
    }

    @Test
    @DisplayName("COMPARABLE field with lt produces <")
    void testToJql_ComparableLt() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.lt, "Medium", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("priority < \"Medium\""));
    }

    // ========== toJql() — DATE fields ==========

    @Test
    @DisplayName("DATE field with gt produces > with date value")
    void testToJql_DateGt() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.gt, "2025-01-01T00:00:00Z", "Created Date")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("created > \""), "Should contain date comparison: " + jql);
    }

    @Test
    @DisplayName("DATE field with epoch millis is converted to JQL date format")
    void testToJql_DateEpochMillis_Converted() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.gte, "1710511845000", "Created Date")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("created >= \"2024/03/15"), "Epoch should be converted: " + jql);
    }

    // ========== toJql() — IDENTITY fields ==========

    @Test
    @DisplayName("IDENTITY field with equals produces = with key value")
    void testToJql_IdentityEquals() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "PROJ-123", "Bug Key")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("key = \"PROJ-123\""));
    }

    // ========== toJql() — FULL_TEXT fields ==========

    @Test
    @DisplayName("FULL_TEXT field uses ~ operator for search")
    void testToJql_FullTextContains() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "NullPointerException", "Description")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("text ~ \"NullPointerException\""));
    }

    // ========== toJql() — pipe-delimited multi-value ==========

    @Test
    @DisplayName("pipe-delimited value on EXACT field produces IN clause")
    void testToJql_PipeDelimitedContains_ProducesInClause() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "acc1|acc2|acc3", "Assignee Name")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("assignee IN ("), "Pipe-delimited should produce IN: " + jql);
        assertTrue(jql.contains("\"acc1\""));
        assertTrue(jql.contains("\"acc2\""));
        assertTrue(jql.contains("\"acc3\""));
    }

    @Test
    @DisplayName("pipe-delimited value on TEXT field does NOT produce IN clause")
    void testToJql_PipeDelimitedOnTextField_UsesContains() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "word1|word2", "Summary")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("summary ~ \"word1|word2\""), "TEXT field should use ~ as-is: " + jql);
    }

    // ========== toJql() — multiple conditions joined with AND ==========

    @Test
    @DisplayName("multiple conditions are joined with AND")
    void testToJql_MultipleConditions_JoinedWithAnd() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "login", "Summary"),
                new SearchCondition(SearchCondition.Operator.equals, "High", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains(" AND "));
        assertTrue(jql.contains("summary ~ \"login\""));
        assertTrue(jql.contains("priority = \"High\""));
    }

    // ========== toJql() — with issue type filter ==========

    @Test
    @DisplayName("toJql with issueType prepends issuetype filter")
    void testToJql_WithIssueType() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "crash", "Summary")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null, "Bug");
        assertTrue(jql.startsWith("issuetype = \"Bug\""), "Should start with issuetype filter: " + jql);
        assertTrue(jql.contains("AND summary ~ \"crash\""));
    }

    @Test
    @DisplayName("toJql with issueType and empty conditions returns type filter with default order")
    void testToJql_WithIssueType_EmptyConditions() {
        String jql = SearchConditionTranslator.toJql(null, null, "Story");
        assertEquals("issuetype = \"Story\" ORDER BY created DESC", jql);
    }

    // ========== toJql() — ORDER BY ==========

    @Test
    @DisplayName("QueryClause orderby is included in JQL")
    void testToJql_WithOrderByClause() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, "ASC", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, clauses);
        assertTrue(jql.contains("ORDER BY priority ASC"));
    }

    @Test
    @DisplayName("default ORDER BY is 'created DESC' when no clauses provided")
    void testToJql_DefaultOrderBy() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("ORDER BY created DESC"), "Should have default order: " + jql);
    }

    @Test
    @DisplayName("ORDER BY is placed correctly with issueType filter")
    void testToJql_OrderByWithIssueType() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, "DESC", "Created Date")
        );
        String jql = SearchConditionTranslator.toJql(conditions, clauses, "Task");
        assertTrue(jql.contains("issuetype = \"Task\" AND status = \"Open\" ORDER BY created DESC"),
                "Full JQL: " + jql);
    }

    // ========== toJql() — edge cases ==========

    @Test
    @DisplayName("null conditions and clauses returns null")
    void testToJql_NullConditionsAndClauses_ReturnsNull() {
        assertNull(SearchConditionTranslator.toJql(null, null));
    }

    @Test
    @DisplayName("empty conditions returns null")
    void testToJql_EmptyConditions_ReturnsNull() {
        assertNull(SearchConditionTranslator.toJql(Collections.emptyList(), null));
    }

    @Test
    @DisplayName("null operand is skipped")
    void testToJql_NullOperand_Skipped() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, null, "Status")
        );
        assertNull(SearchConditionTranslator.toJql(conditions, null));
    }

    @Test
    @DisplayName("empty operand is skipped")
    void testToJql_EmptyOperand_Skipped() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "  ", "Status")
        );
        assertNull(SearchConditionTranslator.toJql(conditions, null));
    }

    @Test
    @DisplayName("unmapped field is skipped")
    void testToJql_UnmappedField_Skipped() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "value", "NonExistentField")
        );
        assertNull(SearchConditionTranslator.toJql(conditions, null));
    }

    @Test
    @DisplayName("JQL value escaping — quotes are escaped")
    void testToJql_EscapesQuotes() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "can't \"reproduce\"", "Summary")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("\\\"reproduce\\\""), "Quotes should be escaped: " + jql);
    }

    // ========== resolveJqlOperator() ==========

    @Test
    @DisplayName("null operator defaults to ~ for TEXT fields")
    void testResolveJqlOperator_NullOp_TextDefaultsToContains() {
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(null, JqlFieldMapping.FieldType.TEXT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(null, JqlFieldMapping.FieldType.FULL_TEXT));
    }

    @Test
    @DisplayName("null operator defaults to = for EXACT fields")
    void testResolveJqlOperator_NullOp_ExactDefaultsToEquals() {
        assertEquals("=", SearchConditionTranslator.resolveJqlOperator(null, JqlFieldMapping.FieldType.EXACT));
    }

    @Test
    @DisplayName("comparison operators map correctly for DATE/COMPARABLE fields")
    void testResolveJqlOperator_ComparisonOperators() {
        assertEquals(">=", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.gte, JqlFieldMapping.FieldType.DATE));
        assertEquals("<=", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.lte, JqlFieldMapping.FieldType.DATE));
        assertEquals(">", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.gt, JqlFieldMapping.FieldType.COMPARABLE));
        assertEquals("<", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.lt, JqlFieldMapping.FieldType.COMPARABLE));
    }

    @Test
    @DisplayName("comparison operators fall back to ~ for TEXT fields")
    void testResolveJqlOperator_ComparisonOnText_FallsBackToContains() {
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.gte, JqlFieldMapping.FieldType.TEXT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.lt, JqlFieldMapping.FieldType.TEXT));
    }

    // ========== End-to-end: realistic search scenarios ==========

    @Test
    @DisplayName("End-to-end: Bug search by summary and status with ordering")
    void testEndToEnd_BugSearch() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "login", "Summary"),
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, "DESC", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, clauses, "Bug");

        assertTrue(jql.startsWith("issuetype = \"Bug\""));
        assertTrue(jql.contains("summary ~ \"login\""));
        assertTrue(jql.contains("status = \"Open\""));
        assertTrue(jql.contains("ORDER BY priority DESC"));
    }

    @Test
    @DisplayName("End-to-end: Epic search by project key")
    void testEndToEnd_EpicSearchByProject() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "MYPROJ", "Project Key")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null, "Epic");

        assertTrue(jql.contains("issuetype = \"Epic\""));
        assertTrue(jql.contains("project = \"MYPROJ\""));
    }

    // ========== Additional branch coverage ==========

    @Test
    @DisplayName("toJql with null issueType falls back to base toJql behavior")
    void testToJql_NullIssueType_FallsBackToBase() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        String jqlThreeArg = SearchConditionTranslator.toJql(conditions, null, null);
        String jqlTwoArg  = SearchConditionTranslator.toJql(conditions, null);
        assertEquals(jqlTwoArg, jqlThreeArg);
    }

    @Test
    @DisplayName("toJql with blank issueType falls back to base toJql behavior")
    void testToJql_BlankIssueType_FallsBackToBase() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        String jqlThreeArg = SearchConditionTranslator.toJql(conditions, null, "   ");
        String jqlTwoArg  = SearchConditionTranslator.toJql(conditions, null);
        assertEquals(jqlTwoArg, jqlThreeArg);
    }

    @Test
    @DisplayName("toJql with issueType and no conditions or clauses returns type filter with ORDER BY")
    void testToJql_IssueType_NullConditionsAndClauses() {
        String jql = SearchConditionTranslator.toJql(null, null, "Epic");
        assertEquals("issuetype = \"Epic\" ORDER BY created DESC", jql);
    }

    @Test
    @DisplayName("toJql with issueType and base without ORDER BY — prepends type filter with AND")
    void testToJql_IssueType_BaseWithoutOrderBy() {
        // Base JQL without ORDER BY is produced when we use the two-arg overload
        // with conditions only and disable default ORDER BY — but since the two-arg
        // always appends ORDER BY, test the three-arg where base already has ORDER BY.
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        // Passing clauses with ORDER BY so base will have ORDER BY; issueType is prepended
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, "ASC", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, clauses, "Story");
        assertTrue(jql.startsWith("issuetype = \"Story\""), "Should start with issuetype: " + jql);
        assertTrue(jql.contains("status = \"Open\""), "Should contain status: " + jql);
        assertTrue(jql.contains("ORDER BY priority ASC"), "Should contain ORDER BY: " + jql);
    }

    @Test
    @DisplayName("SearchCondition.Operator.orderby is handled in conditions loop — adds to ORDER BY")
    void testToJql_SearchConditionOrderby_AddsToOrderBy() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status"),
                new SearchCondition(SearchCondition.Operator.orderby, "ASC", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("status = \"Open\""), "Should contain status: " + jql);
        assertTrue(jql.contains("ORDER BY priority ASC"), "Should contain ORDER BY from condition: " + jql);
    }

    @Test
    @DisplayName("SearchCondition.Operator.orderby with unmapped field is skipped")
    void testToJql_SearchConditionOrderby_UnmappedField_Skipped() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status"),
                new SearchCondition(SearchCondition.Operator.orderby, "ASC", "NonExistentOrderField")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertFalse(jql.contains("NonExistentOrderField"), "Unmapped orderby field should be skipped: " + jql);
    }

    @Test
    @DisplayName("QueryClause with null operand defaults to DESC")
    void testToJql_QueryClause_NullOperand_DefaultsToDesc() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Open", "Status")
        );
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, null, "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, clauses);
        assertTrue(jql.contains("ORDER BY priority DESC"), "Null operand should default to DESC: " + jql);
    }

    @Test
    @DisplayName("resolveJqlOperator gte/lte/gt/lt on EXACT fields falls back to ~")
    void testResolveJqlOperator_ComparisonOnExact_FallsBackToContains() {
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.gte, JqlFieldMapping.FieldType.EXACT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.lte, JqlFieldMapping.FieldType.EXACT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.gt,  JqlFieldMapping.FieldType.EXACT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.lt,  JqlFieldMapping.FieldType.EXACT));
    }

    @Test
    @DisplayName("resolveJqlOperator gte/lte/gt/lt on FULL_TEXT fields falls back to ~")
    void testResolveJqlOperator_ComparisonOnFullText_FallsBackToContains() {
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.gte, JqlFieldMapping.FieldType.FULL_TEXT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.lte, JqlFieldMapping.FieldType.FULL_TEXT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.gt,  JqlFieldMapping.FieldType.FULL_TEXT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.lt,  JqlFieldMapping.FieldType.FULL_TEXT));
    }

    @Test
    @DisplayName("resolveJqlOperator gte/lte on COMPARABLE maps correctly")
    void testResolveJqlOperator_GteLteOnComparable() {
        assertEquals(">=", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.gte, JqlFieldMapping.FieldType.COMPARABLE));
        assertEquals("<=", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.lte, JqlFieldMapping.FieldType.COMPARABLE));
    }

    @Test
    @DisplayName("resolveJqlOperator with startswith returns ~")
    void testResolveJqlOperator_StartsWith() {
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.startswith, JqlFieldMapping.FieldType.TEXT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.startswith, JqlFieldMapping.FieldType.EXACT));
    }

    @Test
    @DisplayName("resolveJqlOperator with endswith returns ~")
    void testResolveJqlOperator_EndsWith() {
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.endswith, JqlFieldMapping.FieldType.TEXT));
        assertEquals("~", SearchConditionTranslator.resolveJqlOperator(SearchCondition.Operator.endswith, JqlFieldMapping.FieldType.EXACT));
    }

    @Test
    @DisplayName("resolveJqlOperator null op on COMPARABLE defaults to =")
    void testResolveJqlOperator_NullOp_ComparableDefaultsToEquals() {
        assertEquals("=", SearchConditionTranslator.resolveJqlOperator(null, JqlFieldMapping.FieldType.COMPARABLE));
        assertEquals("=", SearchConditionTranslator.resolveJqlOperator(null, JqlFieldMapping.FieldType.DATE));
        assertEquals("=", SearchConditionTranslator.resolveJqlOperator(null, JqlFieldMapping.FieldType.IDENTITY));
    }

    @Test
    @DisplayName("parseDirection via QueryClause with ASC value produces ASC in JQL")
    void testParseDirection_Asc_ViaQueryClause() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "High", "Priority")
        );
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, "asc", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, clauses);
        assertTrue(jql.contains("ORDER BY priority ASC"), "lowercase asc should produce ASC: " + jql);
    }

    @Test
    @DisplayName("parseDirection with value containing ASC (mixed case) produces ASC")
    void testParseDirection_ContainsAsc_Mixed() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "High", "Priority")
        );
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, "Ascending", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, clauses);
        assertTrue(jql.contains("ORDER BY priority ASC"), "Ascending should produce ASC: " + jql);
    }

    @Test
    @DisplayName("toJql with only ORDER BY SearchCondition and no where parts still produces ORDER BY")
    void testToJql_OnlyOrderByCondition_ProducesOrderBy() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.orderby, "DESC", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertNotNull(jql);
        assertTrue(jql.contains("ORDER BY priority DESC"), "Should contain ORDER BY: " + jql);
    }

    @Test
    @DisplayName("Multiple DATE conditions on same field appear as separate clauses")
    void testToJql_MultipleDateConditions() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.gte, "2024-01-01T00:00:00Z", "Created Date"),
                new SearchCondition(SearchCondition.Operator.lte, "2024-12-31T23:59:59Z", "Created Date")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertNotNull(jql);
        assertTrue(jql.contains("created >= \""), "Should have >= clause: " + jql);
        assertTrue(jql.contains("created <= \""), "Should have <= clause: " + jql);
        assertTrue(jql.contains(" AND "), "Should be joined by AND: " + jql);
    }

    @Test
    @DisplayName("COMPARABLE field with lte produces <=")
    void testToJql_ComparableLte() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.lte, "Medium", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("priority <= \"Medium\""), "Should produce <=: " + jql);
    }

    @Test
    @DisplayName("COMPARABLE field with gt produces >")
    void testToJql_ComparableGt() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.gt, "Low", "Priority")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("priority > \"Low\""), "Should produce >: " + jql);
    }

    @Test
    @DisplayName("DATE field with lte produces <=")
    void testToJql_DateLte() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.lte, "2025-06-01T00:00:00Z", "Created Date")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("created <= \""), "Should produce <=: " + jql);
    }

    @Test
    @DisplayName("toJql with issueType and empty where clause — only type filter and ORDER BY")
    void testToJql_IssueType_EmptyBase_OrderByFromClause() {
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, "ASC", "Priority")
        );
        // null conditions with clauses — base has only ORDER BY, no WHERE
        String jql = SearchConditionTranslator.toJql(null, clauses, "Bug");
        assertNotNull(jql);
        assertTrue(jql.contains("issuetype = \"Bug\""), "Should have issuetype: " + jql);
        assertTrue(jql.contains("ORDER BY priority ASC"), "Should have ORDER BY from clause: " + jql);
    }

    @Test
    @DisplayName("pipe-delimited contains on FULL_TEXT field does NOT produce IN clause — uses ~ directly")
    void testToJql_PipeDelimitedOnFullTextField_UsesContains() {
        // "Description" maps to "text" field with FULL_TEXT type.
        // The pipe-delimited IN-clause optimisation is skipped for FULL_TEXT fields.
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "word1|word2", "Description")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("text ~ \"word1|word2\""), "FULL_TEXT pipe should use ~ as-is: " + jql);
        assertFalse(jql.contains(" IN ("), "Should not produce IN clause for FULL_TEXT: " + jql);
    }

    @Test
    @DisplayName("pipe-delimited contains with empty part in the middle — blank segment is skipped")
    void testToJql_PipeDelimitedWithEmptyPart_SkipsEmptyPart() {
        // "acc1||acc3" → split gives ["acc1", "", "acc3"]; empty part must be skipped
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "acc1||acc3", "Assignee Name")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("assignee IN ("), "Should produce IN clause: " + jql);
        assertTrue(jql.contains("\"acc1\""), "acc1 should be included: " + jql);
        assertTrue(jql.contains("\"acc3\""), "acc3 should be included: " + jql);
        // Verify the empty segment did not produce an empty-quoted entry
        assertFalse(jql.contains("\"\""), "Empty part should be skipped: " + jql);
    }

    @Test
    @DisplayName("QueryClause with orderby and a field that has no JQL mapping is skipped")
    void testToJql_QueryClause_OrderBy_UnmappedField_Skipped() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "High", "Priority")
        );
        List<QueryClause> clauses = List.of(
                new QueryClause(QueryClause.Operator.orderby, "ASC", "UnmappedQueryClauseField_xyz")
        );
        String jql = SearchConditionTranslator.toJql(conditions, clauses);
        assertNotNull(jql);
        assertFalse(jql.contains("UnmappedQueryClauseField_xyz"), "Unmapped QueryClause field should be skipped: " + jql);
    }

    @Test
    @DisplayName("DATE field with lt operator produces < comparison")
    void testToJql_DateLt() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.lt, "2025-12-31T23:59:59Z", "Created Date")
        );
        String jql = SearchConditionTranslator.toJql(conditions, null);
        assertTrue(jql.contains("created < \""), "DATE lt should produce <: " + jql);
    }

    @Test
    @DisplayName("resolveJqlOperator: lt on DATE returns < (covers DATE short-circuit branch)")
    void testResolveJqlOperator_LtOnDate_ReturnsLessThan() {
        assertEquals("<", SearchConditionTranslator.resolveJqlOperator(
                SearchCondition.Operator.lt, JqlFieldMapping.FieldType.DATE));
    }


}
