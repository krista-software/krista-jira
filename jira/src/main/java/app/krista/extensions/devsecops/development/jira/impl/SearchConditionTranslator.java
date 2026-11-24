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
import app.krista.sdk.atlassian.util.DatePlus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class SearchConditionTranslator {

    private SearchConditionTranslator() {
    }

    public static String toJql(List<SearchCondition> conditions, List<QueryClause> clauses, String issueType) {
        String base = toJql(conditions, clauses);
        if (issueType == null || issueType.isBlank()) {
            return base;
        }
        String typeFilter = "issuetype = \"" + issueType + "\"";
        if (base == null || base.isBlank()) {
            return typeFilter + " ORDER BY created DESC";
        }
        // Insert issuetype filter before ORDER BY if present, otherwise prepend with AND
        int orderByIdx = base.toUpperCase(Locale.ROOT).indexOf(" ORDER BY ");
        if (orderByIdx >= 0) {
            String where = base.substring(0, orderByIdx).trim();
            String orderBy = base.substring(orderByIdx);
            if (where.isEmpty()) {
                return typeFilter + orderBy;
            }
            return typeFilter + " AND " + where + orderBy;
        }
        return typeFilter + " AND " + base;
    }

    public static String toJql(List<SearchCondition> conditions, List<QueryClause> clauses) {
        List<String> whereParts = new ArrayList<>();
        List<String> orderParts = new ArrayList<>();

        // Collect equals/notequals values grouped by JQL field for IN/NOT IN support
        Map<String, List<String>> equalsGroups = new LinkedHashMap<>();
        Map<String, List<String>> notEqualsGroups = new LinkedHashMap<>();

        if (conditions != null) {
            for (SearchCondition sc : conditions) {
                String fieldName = sc.getFieldName();
                Object operand = sc.getOperand();
                SearchCondition.Operator operator = sc.getOperator();
                String value = operand != null ? String.valueOf(operand).trim() : "";

                if (value.isEmpty()) continue;

                if (operator == SearchCondition.Operator.orderby) {
                    String orderField = JqlFieldMapping.orderByField(fieldName);
                    if (orderField != null) {
                        orderParts.add(orderField + " " + parseDirection(value));
                    }
                    continue;
                }

                JqlFieldMapping mapping = JqlFieldMapping.forIssueField(fieldName);
                if (mapping == null) continue;

                if (mapping.getFieldType() == JqlFieldMapping.FieldType.DATE) {
                    value = DatePlus.toJqlDate(value);
                }

                String jqlOp = resolveJqlOperator(operator, mapping.getFieldType());
                String jqlField = mapping.getJqlFieldName();

                // Handle pipe-delimited contains on non-text fields → convert to IN clause
                if (operator == SearchCondition.Operator.contains
                        && value.contains("|")
                        && mapping.getFieldType() != JqlFieldMapping.FieldType.TEXT
                        && mapping.getFieldType() != JqlFieldMapping.FieldType.FULL_TEXT) {
                    String[] parts = value.split("\\|");
                    for (String part : parts) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) {
                            equalsGroups.computeIfAbsent(jqlField, k -> new ArrayList<>()).add(trimmed);
                        }
                    }
                    continue;
                }

                // Group equals (=) and notequals (!=) by field for IN/NOT IN collapsing
                if ("=".equals(jqlOp)) {
                    equalsGroups.computeIfAbsent(jqlField, k -> new ArrayList<>()).add(value);
                } else if ("!=".equals(jqlOp)) {
                    notEqualsGroups.computeIfAbsent(jqlField, k -> new ArrayList<>()).add(value);
                } else {
                    String clause = buildClause(jqlField, jqlOp, value, operator);
                    if (clause != null) {
                        whereParts.add(clause);
                    }
                }
            }
        }

        // Build IN clauses for grouped equals conditions
        for (Map.Entry<String, List<String>> entry : equalsGroups.entrySet()) {
            String jqlField = entry.getKey();
            List<String> values = entry.getValue();
            if (values.size() == 1) {
                whereParts.add(jqlField + " = \"" + escapeJqlValue(values.get(0)) + "\"");
            } else {
                String inList = values.stream()
                        .map(v -> "\"" + escapeJqlValue(v) + "\"")
                        .collect(Collectors.joining(", "));
                whereParts.add(jqlField + " IN (" + inList + ")");
            }
        }

        // Build NOT IN clauses for grouped notequals conditions
        for (Map.Entry<String, List<String>> entry : notEqualsGroups.entrySet()) {
            String jqlField = entry.getKey();
            List<String> values = entry.getValue();
            if (values.size() == 1) {
                whereParts.add(jqlField + " != \"" + escapeJqlValue(values.get(0)) + "\"");
            } else {
                String inList = values.stream()
                        .map(v -> "\"" + escapeJqlValue(v) + "\"")
                        .collect(Collectors.joining(", "));
                whereParts.add(jqlField + " NOT IN (" + inList + ")");
            }
        }

        if (clauses != null) {
            for (QueryClause qc : clauses) {
                if (qc.getOperator() == QueryClause.Operator.orderby) {
                    String orderField = JqlFieldMapping.orderByField(qc.getFieldName());
                    if (orderField != null) {
                        String dir = qc.getOperand() != null
                                ? parseDirection(String.valueOf(qc.getOperand()).trim())
                                : "DESC";
                        orderParts.add(orderField + " " + dir);
                    }
                }
            }
        }

        if (whereParts.isEmpty() && orderParts.isEmpty()) {
            return null;
        }

        StringBuilder jql = new StringBuilder();
        if (!whereParts.isEmpty()) {
            jql.append(String.join(" AND ", whereParts));
        }
        if (!orderParts.isEmpty()) {
            jql.append(" ORDER BY ").append(String.join(", ", orderParts));
        } else if (!whereParts.isEmpty()) {
            jql.append(" ORDER BY created DESC");
        }

        return jql.toString();
    }

    static String resolveJqlOperator(SearchCondition.Operator op, JqlFieldMapping.FieldType fieldType) {
        if (op == null) {
            switch (fieldType) {
                case TEXT:
                case FULL_TEXT:
                    return "~";
                default:
                    return "=";
            }
        }
        switch (op) {
            case equals:
                return "=";
            case notequals:
                return "!=";
            case contains:
                return "~";
            case gte:
                if (fieldType == JqlFieldMapping.FieldType.DATE
                        || fieldType == JqlFieldMapping.FieldType.COMPARABLE) return ">=";
                return "~";
            case lte:
                if (fieldType == JqlFieldMapping.FieldType.DATE
                        || fieldType == JqlFieldMapping.FieldType.COMPARABLE) return "<=";
                return "~";
            case gt:
                if (fieldType == JqlFieldMapping.FieldType.DATE
                        || fieldType == JqlFieldMapping.FieldType.COMPARABLE) return ">";
                return "~";
            case lt:
                if (fieldType == JqlFieldMapping.FieldType.DATE
                        || fieldType == JqlFieldMapping.FieldType.COMPARABLE) return "<";
                return "~";
            case startswith:
                return "~";
            case endswith:
                return "~";
            default:
                return "=";
        }
    }

    private static String buildClause(String jqlField, String jqlOp, String value,
                                       SearchCondition.Operator op) {
        if (op == SearchCondition.Operator.startswith) {
            return jqlField + " ~ \"" + escapeJqlValue(value) + "*\"";
        }
        if (op == SearchCondition.Operator.endswith) {
            return jqlField + " ~ \"*" + escapeJqlValue(value) + "\"";
        }
        if ("~".equals(jqlOp)) {
            return jqlField + " ~ \"" + escapeJqlValue(value) + "\"";
        }
        return jqlField + " " + jqlOp + " \"" + escapeJqlValue(value) + "\"";
    }

    private static String escapeJqlValue(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String parseDirection(String value) {
        if (value == null) return "DESC";
        String upper = value.toUpperCase(Locale.ROOT).trim();
        if ("ASC".equals(upper) || upper.contains("ASC")) return "ASC";
        return "DESC";
    }
}
