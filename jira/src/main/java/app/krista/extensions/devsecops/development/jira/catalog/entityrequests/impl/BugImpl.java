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
import app.krista.extensions.devsecops.development.jira.catalog.entities.Bug;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.SearchConditionTranslator;
import app.krista.extensions.devsecops.development.jira.util.IssueMapper;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class BugImpl extends EntityImplementationService {

    public BugImpl(IssueDao issueDao) {
        super(issueDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        String projectKey, summary, description;
        validateInputs(entityAttributeField, List.of(JiraConstants.PROJECT_KEY, JiraConstants.SUMMARY));
        projectKey = entityAttributeField.get(JiraConstants.PROJECT_KEY).toString();
        summary = entityAttributeField.get(JiraConstants.SUMMARY).toString();
        description = entityAttributeField.containsKey(JiraConstants.DESCRIPTION) ? entityAttributeField.get(JiraConstants.DESCRIPTION).toString() : summary;
        Issue issue = getIssueService().createIssue(projectKey, summary, description, null, JiraConstants.BUG);
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
        return new EntityValue(entityType.getEntityName(), IssueMapper.getBug(getIssueService().getIssueFinal(issue.key)).toFields());
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        if (getIssueService().getIssueFinal(entity.getFields().get("Bug Key").toString()) == null) {
            throw new NoSuchElementException();
        }
        Bug bug = getBugEntity(entity.getFields());
        getIssueService().updateIssue(IssueMapper.getIssue(bug));
        return new EntityValue(entityType.getEntityName(), bug.toFields());
    }

    private Bug getBugEntity(Map<String, Object> attributeFields) {
        Bug bug = new Bug();
        bug.summary = attributeFields.get(JiraConstants.SUMMARY).toString();
        bug.comments = attributeFields.containsKey(JiraConstants.COMMENTS) ? Collections.singletonList(attributeFields.get(JiraConstants.COMMENTS).toString()) : Collections.emptyList();
        bug.reporterName = attributeFields.get(JiraConstants.REPORTER_NAME).toString();
        bug.assigneeName = attributeFields.get(JiraConstants.ASSIGNEE_NAME).toString();
        bug.bugKey = attributeFields.get("Bug Key").toString();
        bug.id = attributeFields.get(JiraConstants.ID).toString();
        bug.projectKey = attributeFields.get(JiraConstants.PROJECT_KEY).toString();
        bug.description = attributeFields.get(JiraConstants.DESCRIPTION).toString();
        bug.type = attributeFields.get(JiraConstants.TYPE).toString();
        bug.priority = attributeFields.get(JiraConstants.PRIORITY).toString();
        bug.status = attributeFields.get(JiraConstants.STATUS).toString();
        return bug;
    }

    @Override
    public List<EntityValue> search(EntityType entityType, SearchQuery searchQueries, Long pageIndex, Integer pageSize) {
        List<SearchCondition> conditions = resolveUserConditions(searchQueries.getSearchConditions());
        List<QueryClause> clauses = searchQueries.getQueryClauses();

        // Special case: single Bug Key lookup by equals or null operator — direct GET
        if (conditions.size() == 1) {
            SearchCondition only = conditions.get(0);
            if ("bug key".equalsIgnoreCase(only.getFieldName())
                    && only.getOperand() != null
                    && (only.getOperator() == null
                        || only.getOperator() == SearchCondition.Operator.equals)) {
                String key = String.valueOf(only.getOperand()).trim();
                Issue issue = getIssueService().getIssueFinal(key);
                if (issue != null && JiraConstants.BUG.equalsIgnoreCase(issue.issueType)) {
                    return List.of(new EntityValue(entityType.getEntityName(), IssueMapper.getBug(issue).toFields()));
                }
                return List.of();
            }
        }

        // Build JQL from all conditions and clauses, filtered by issuetype = "Bug"
        String jql = SearchConditionTranslator.toJql(conditions, clauses, JiraConstants.BUG);

        if (jql == null || jql.isBlank()) {
            List<Issue> issues = searchBySummaryIssueType("", JiraConstants.BUG, pageSize, pageIndex);
            return getEntityList(issues, pageSize, entityType.getEntityName());
        }

        List<Issue> issues = getIssueService().searchByJQL(jql, pageSize, pageIndex);
        return getEntityList(issues, pageSize, entityType.getEntityName());
    }

    private List<EntityValue> getEntityList(List<Issue> issues, Integer pageSize, String entityName) {
        return issues.stream()
                .limit(pageSize)
                .filter(issue -> issue.issueType.equalsIgnoreCase(JiraConstants.BUG))
                .map(IssueMapper::getBug)
                .map(bug -> new EntityValue(entityName, bug.toFields()))
                .collect(Collectors.toList());
    }

    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        Bug bug = IssueMapper.getBug(getIssueService().getIssueFinal(primaryKey));
        return new EntityValue(entityType.getEntityName(), bug.toFields());
    }
}
