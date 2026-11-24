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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class JqlFieldMapping {

    public enum FieldType {
        TEXT,
        FULL_TEXT,
        EXACT,
        COMPARABLE,
        DATE,
        IDENTITY
    }

    private final String jqlFieldName;
    private final FieldType fieldType;

    private JqlFieldMapping(String jqlFieldName, FieldType fieldType) {
        this.jqlFieldName = jqlFieldName;
        this.fieldType = fieldType;
    }

    public String getJqlFieldName() {
        return jqlFieldName;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    private static final Map<String, JqlFieldMapping> ISSUE_FIELDS = new LinkedHashMap<>();

    static {
        ISSUE_FIELDS.put("summary", new JqlFieldMapping("summary", FieldType.TEXT));
        ISSUE_FIELDS.put("description", new JqlFieldMapping("text", FieldType.FULL_TEXT));
        ISSUE_FIELDS.put("status", new JqlFieldMapping("status", FieldType.EXACT));
        ISSUE_FIELDS.put("issue type", new JqlFieldMapping("issuetype", FieldType.EXACT));
        ISSUE_FIELDS.put("key", new JqlFieldMapping("key", FieldType.IDENTITY));
        ISSUE_FIELDS.put("project key", new JqlFieldMapping("project", FieldType.EXACT));
        ISSUE_FIELDS.put("priority", new JqlFieldMapping("priority", FieldType.COMPARABLE));
        ISSUE_FIELDS.put("assignee name", new JqlFieldMapping("assignee", FieldType.EXACT));
        ISSUE_FIELDS.put("reporter name", new JqlFieldMapping("reporter", FieldType.EXACT));
        ISSUE_FIELDS.put("created date", new JqlFieldMapping("created", FieldType.DATE));
        ISSUE_FIELDS.put("last modified date", new JqlFieldMapping("updated", FieldType.DATE));
        ISSUE_FIELDS.put("labels", new JqlFieldMapping("labels", FieldType.EXACT));
        ISSUE_FIELDS.put("components", new JqlFieldMapping("component", FieldType.EXACT));
        ISSUE_FIELDS.put("parent key", new JqlFieldMapping("parent", FieldType.EXACT));
        ISSUE_FIELDS.put("id", new JqlFieldMapping("id", FieldType.EXACT));
        ISSUE_FIELDS.put("comments", new JqlFieldMapping("comment", FieldType.FULL_TEXT));

        // Entity-specific key aliases (Bug Key, Epic Key, etc. all map to JQL "key")
        ISSUE_FIELDS.put("bug key", new JqlFieldMapping("key", FieldType.IDENTITY));
        ISSUE_FIELDS.put("epic key", new JqlFieldMapping("key", FieldType.IDENTITY));
        ISSUE_FIELDS.put("story key", new JqlFieldMapping("key", FieldType.IDENTITY));
        ISSUE_FIELDS.put("task key", new JqlFieldMapping("key", FieldType.IDENTITY));

        // Entity-specific type aliases (Type, Story Type, Task Type all map to JQL "issuetype")
        ISSUE_FIELDS.put("type", new JqlFieldMapping("issuetype", FieldType.EXACT));
        ISSUE_FIELDS.put("story type", new JqlFieldMapping("issuetype", FieldType.EXACT));
        ISSUE_FIELDS.put("task type", new JqlFieldMapping("issuetype", FieldType.EXACT));
    }

    public static JqlFieldMapping forIssueField(String entityFieldName) {
        if (entityFieldName == null) return null;
        return ISSUE_FIELDS.get(entityFieldName.toLowerCase(Locale.ROOT));
    }

    private static final Map<String, String> ORDERBY_FIELDS = new LinkedHashMap<>();

    static {
        ORDERBY_FIELDS.put("summary", "summary");
        ORDERBY_FIELDS.put("status", "status");
        ORDERBY_FIELDS.put("priority", "priority");
        ORDERBY_FIELDS.put("created date", "created");
        ORDERBY_FIELDS.put("last modified date", "updated");
        ORDERBY_FIELDS.put("key", "key");
        ORDERBY_FIELDS.put("issue type", "issuetype");
        ORDERBY_FIELDS.put("assignee name", "assignee");
        ORDERBY_FIELDS.put("reporter name", "reporter");
        ORDERBY_FIELDS.put("project key", "project");
    }

    public static String orderByField(String entityFieldName) {
        if (entityFieldName == null) return null;
        return ORDERBY_FIELDS.get(entityFieldName.toLowerCase(Locale.ROOT));
    }
}
