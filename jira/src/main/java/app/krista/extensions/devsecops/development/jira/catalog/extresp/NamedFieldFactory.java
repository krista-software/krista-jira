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

package app.krista.extensions.devsecops.development.jira.catalog.extresp;

import app.krista.extensions.devsecops.development.jira.catalog.validators.FieldAttributeExtractor;
import app.krista.model.field.NamedField;
import app.krista.sdk.atlassian.extresp.FieldTypeResolver;

import java.util.Map;

/**
 * Thin Jira-specific wrapper around the shared SDK
 * {@link app.krista.sdk.atlassian.extresp.NamedFieldFactory}, wiring field
 * type resolution to Jira's {@link FieldAttributeExtractor}.
 */
public class NamedFieldFactory {

    /** Adapts {@link FieldAttributeExtractor}'s static helpers to the SDK resolver SPI. */
    private static final FieldTypeResolver RESOLVER = new FieldTypeResolver() {
        @Override
        public boolean isListField(String fieldType) {
            return FieldAttributeExtractor.isListField(fieldType);
        }

        @Override
        public String extractInnerFieldType(String fieldType) {
            return FieldAttributeExtractor.extractInnerFieldType(fieldType);
        }

        @Override
        public String getKristaFieldType(String fieldType) {
            return FieldAttributeExtractor.getKristaFieldType(fieldType);
        }
    };

    private NamedFieldFactory() {
    }

    public static NamedField createTextField(String fieldName) {
        return app.krista.sdk.atlassian.extresp.NamedFieldFactory.createTextField(fieldName);
    }

    public static NamedField createSwitchField(String fieldName) {
        return app.krista.sdk.atlassian.extresp.NamedFieldFactory.createSwitchField(fieldName);
    }

    /**
     * Creates a field with attributes based on field type and attributes.
     * Handles both simple fields and list fields.
     *
     * @param fieldName The field name
     * @param fieldType The field type (e.g., "[ Text ]", "Text")
     * @param fieldAttributes The field attributes
     * @return The created NamedField
     */
    public static NamedField createFieldWithAttributes(String fieldName, String fieldType, Map<String, Object> fieldAttributes) {
        return app.krista.sdk.atlassian.extresp.NamedFieldFactory
                .createFieldWithAttributes(fieldName, fieldType, fieldAttributes, RESOLVER);
    }

    /**
     * Creates a list field following the Office 365 Calendar pattern.
     *
     * @param fieldName The field name
     * @param fieldType The field type (e.g., "[ Text ]")
     * @param fieldAttributes The field attributes
     * @return The created list field
     */
    public static NamedField createListField(String fieldName, String fieldType, Map<String, Object> fieldAttributes) {
        return app.krista.sdk.atlassian.extresp.NamedFieldFactory
                .createListField(fieldName, fieldType, fieldAttributes, RESOLVER);
    }

    /**
     * Creates a simple (non-list) field.
     *
     * @param fieldName The field name
     * @param fieldType The field type
     * @param fieldAttributes The field attributes
     * @return The created field
     */
    public static NamedField createSimpleField(String fieldName, String fieldType, Map<String, Object> fieldAttributes) {
        return app.krista.sdk.atlassian.extresp.NamedFieldFactory
                .createSimpleField(fieldName, fieldType, fieldAttributes, RESOLVER);
    }

}
