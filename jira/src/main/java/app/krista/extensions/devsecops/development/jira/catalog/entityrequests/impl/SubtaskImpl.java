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

package app.krista.extensions.devsecops.development.jira.catalog.entityrequests.impl;

import app.krista.extension.executor.QueryClause;
import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Subtask;
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.SearchConditionTranslator;
import app.krista.extensions.devsecops.development.jira.util.IssueMapper;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public final class SubtaskImpl extends EntityImplementationService {

    public SubtaskImpl(IssueDao issueDao) {
        super(issueDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        String projectKey, summary, description, parentKey;
        validateInputs(entityAttributeField, List.of(JiraConstants.PROJECT_KEY, JiraConstants.SUMMARY, JiraConstants.PARENT_KEY));
        projectKey = entityAttributeField.get(JiraConstants.PROJECT_KEY).toString();
        summary = entityAttributeField.get(JiraConstants.SUMMARY).toString();
        parentKey = entityAttributeField.get(JiraConstants.PARENT_KEY).toString();
        description = entityAttributeField.containsKey(JiraConstants.DESCRIPTION) ? entityAttributeField.get(JiraConstants.DESCRIPTION).toString() : summary;
        Issue issue = getIssueService().createIssue(projectKey, summary, description, parentKey, "Sub-task");
        if(entityAttributeField.get(JiraConstants.COMMENTS) != null){
            issue.comments = (List<String>) entityAttributeField.get(JiraConstants.COMMENTS);
            issue.comments.forEach(comment -> getIssueService().addComment(issue.key, comment,""));
        }
        if(entityAttributeField.get(JiraConstants.ASSIGNEE_NAME) != null){
            issue.assigneeName = entityAttributeField.get(JiraConstants.ASSIGNEE_NAME).toString();
        }
        if(entityAttributeField.get(JiraConstants.PRIORITY) != null){
            issue.priority = entityAttributeField.get(JiraConstants.PRIORITY).toString();
        }
        if(entityAttributeField.get("Status") != null){
            issue.status = entityAttributeField.get("Status").toString();
        }
        getIssueService().updateIssue(issue);
        return new EntityValue(entityType.getEntityName(), IssueMapper.getSubtask(getIssueService().getIssueFinal(issue.key)).toFields());
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        if(getIssueService().getIssueFinal(entity.getFields().get("Key").toString()) == null) {
            throw new NoSuchElementException();
        }
        Subtask subtask = getSubtaskEntity(entity.getFields());
        getIssueService().updateIssue(IssueMapper.getIssue(subtask));
        return new EntityValue(entityType.getEntityName(), subtask.toFields());
    }

    private Subtask getSubtaskEntity(Map<String, Object> attributeFields) {
        Subtask subtask = new Subtask();
        subtask.summary = attributeFields.get(JiraConstants.SUMMARY).toString();
        subtask.comments = attributeFields.containsKey(JiraConstants.COMMENTS) ? Collections.singletonList(attributeFields.get(JiraConstants.COMMENTS).toString()) : Collections.emptyList();
        subtask.reporterName = attributeFields.get(JiraConstants.REPORTER_NAME).toString();
        subtask.assigneeName = attributeFields.get(JiraConstants.ASSIGNEE_NAME).toString();
        subtask.key = attributeFields.get("Key").toString();
        subtask.id = attributeFields.get(JiraConstants.ID).toString();
        subtask.parentKey = attributeFields.get(JiraConstants.PARENT_KEY).toString();
        subtask.projectKey = attributeFields.get(JiraConstants.PROJECT_KEY).toString();
        subtask.description = attributeFields.get(JiraConstants.DESCRIPTION).toString();
        subtask.taskType = attributeFields.get("Task Type").toString();
        subtask.priority = attributeFields.get(JiraConstants.PRIORITY).toString();
        subtask.status = attributeFields.get(JiraConstants.STATUS).toString();
        return subtask;
    }

    @Override
    public List<EntityValue> search(EntityType entityType, SearchQuery searchQuery, Long pageIndex, Integer pageSize) {
        List<SearchCondition> conditions = resolveUserConditions(searchQuery.getSearchConditions());
        List<QueryClause> clauses = searchQuery.getQueryClauses();

        // Special case: single Key lookup by equals or null operator — direct GET
        if (conditions.size() == 1) {
            SearchCondition only = conditions.get(0);
            if ("key".equalsIgnoreCase(only.getFieldName())
                    && only.getOperand() != null
                    && (only.getOperator() == null
                        || only.getOperator() == SearchCondition.Operator.equals)) {
                String key = String.valueOf(only.getOperand()).trim();
                Issue issue = getIssueService().getIssueFinal(key);
                if (issue != null && JiraConstants.SUBTASK.equalsIgnoreCase(issue.issueType)) {
                    return List.of(new EntityValue(entityType.getEntityName(), IssueMapper.getSubtask(issue).toFields()));
                }
                return List.of();
            }
        }

        // Build JQL from all conditions and clauses, filtered by issuetype = "Sub-task"
        String jql = SearchConditionTranslator.toJql(conditions, clauses, JiraConstants.SUBTASK);

        if (jql == null || jql.isBlank()) {
            List<Issue> issues = searchBySummaryIssueType("", JiraConstants.SUBTASK, pageSize, pageIndex);
            return getEntityList(issues, pageSize, entityType.getEntityName());
        }

        List<Issue> issues = getIssueService().searchByJQL(jql, pageSize, pageIndex);
        return getEntityList(issues, pageSize, entityType.getEntityName());
    }

    private List<EntityValue> getEntityList(List<Issue> issues, Integer pageSize, String entityName) {
        return issues.stream()
                .limit(pageSize)
                .filter(issue -> issue.issueType.equalsIgnoreCase(JiraConstants.SUBTASK))
                .map(IssueMapper::getSubtask)
                .map(subtask -> new EntityValue(entityName, subtask.toFields()))
                .collect(Collectors.toList());
    }

    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        Subtask subtask = IssueMapper.getSubtask(getIssueService().getIssueFinal(primaryKey));
        return new EntityValue(entityType.getEntityName(), subtask.toFields());
    }
}
