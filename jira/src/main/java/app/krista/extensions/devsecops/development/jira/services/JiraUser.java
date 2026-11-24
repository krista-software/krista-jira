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

package app.krista.extensions.devsecops.development.jira.services;

import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class JiraUser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraUser.class);

    // JSON field name constants
    private static final String FIELD_ACCOUNT_ID = "accountId";
    private static final String FIELD_DISPLAY_NAME = "displayName";
    private static final String FIELD_ACCOUNT_TYPE = "accountType";
    private static final String FIELD_EMAIL_ADDRESS = "emailAddress";
    private static final String FIELD_SELF = "self";
    private static final String FIELD_APPLICATION_KEYS = "applicationKeys";

    public final String accountId;
    public final String displayName;
    public final String accountType;
    public final String emailId;
    public final String self;
    public final List<String> applicationKeys;

    public JiraUser(String accountId, String displayName, String accountType, String emailId, String self, List<String> applicationKeys) {
        this.accountId = accountId;
        this.displayName = displayName;
        this.accountType = accountType;
        this.emailId = emailId;
        this.self = self;
        this.applicationKeys = applicationKeys;
    }

    public static JiraUser create(JsonObject userObject) {
        // Safely extract required fields with null checks
        String accountId = userObject.has(FIELD_ACCOUNT_ID) && !userObject.get(FIELD_ACCOUNT_ID).isJsonNull()
                ? userObject.get(FIELD_ACCOUNT_ID).getAsString() : "";
        String displayName = userObject.has(FIELD_DISPLAY_NAME) && !userObject.get(FIELD_DISPLAY_NAME).isJsonNull()
                ? userObject.get(FIELD_DISPLAY_NAME).getAsString() : "";
        String accountType = userObject.has(FIELD_ACCOUNT_TYPE) && !userObject.get(FIELD_ACCOUNT_TYPE).isJsonNull()
                ? userObject.get(FIELD_ACCOUNT_TYPE).getAsString() : "";
        String emailId = userObject.has(FIELD_EMAIL_ADDRESS) && !userObject.get(FIELD_EMAIL_ADDRESS).isJsonNull()
                ? userObject.get(FIELD_EMAIL_ADDRESS).getAsString() : "";
        String self = userObject.has(FIELD_SELF) && !userObject.get(FIELD_SELF).isJsonNull()
                ? userObject.get(FIELD_SELF).getAsString() : "";


        // Email may be missing due to user privacy settings in Jira
        // See: https://confluence.atlassian.com/jirakb/user-email-address-is-not-returned-via-rest-api-1043597298.html
        if (emailId.isEmpty()) {
            LOGGER.warn("Email address is empty (likely due to privacy settings). JSON has '{}' field: {}",
                    FIELD_EMAIL_ADDRESS, userObject.has(FIELD_EMAIL_ADDRESS));
            if (userObject.has(FIELD_EMAIL_ADDRESS)) {
                LOGGER.warn("{} field value: {}", FIELD_EMAIL_ADDRESS, userObject.get(FIELD_EMAIL_ADDRESS));
            }
        }

        // Extract applicationKeys if present
        List<String> applicationKeys = new ArrayList<>();
        if (userObject.has(FIELD_APPLICATION_KEYS) && !userObject.get(FIELD_APPLICATION_KEYS).isJsonNull()) {
            JsonArray keysArray = userObject.getAsJsonArray(FIELD_APPLICATION_KEYS);
            for (JsonElement element : keysArray) {
                if (!element.isJsonNull()) {
                    applicationKeys.add(element.getAsString());
                }
            }
        }

        return new JiraUser(accountId, displayName, accountType, emailId, self, applicationKeys);
    }

    public String summary() {
        return displayName;
    }

    public User getUser() {
        User userFinal = new User();
        userFinal.accountId = this.accountId;
        userFinal.accountType = this.accountType;
        userFinal.email = this.emailId;
        userFinal.name = this.displayName;
        userFinal.displayName = this.displayName;
        userFinal.self = this.self;
        userFinal.applicationKeys = this.applicationKeys;

        return userFinal;
    }

}
