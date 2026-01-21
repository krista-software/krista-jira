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
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class NamedFieldFactory {

    private static final Gson GSON = new Gson();

    private NamedFieldFactory() {
    }

    public static NamedField createTextField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.TEXT_FIELD, Map.of(), Map.of());
    }

    public static NamedField createSwitchField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.SWITCH_FIELD, Map.of(), Map.of());
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
        if (FieldAttributeExtractor.isListField(fieldType)) {
            return createListField(fieldName, fieldType, fieldAttributes);
        } else {
            return createSimpleField(fieldName, fieldType, fieldAttributes);
        }
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
        String innerFieldType = FieldAttributeExtractor.extractInnerFieldType(fieldType);
        String innerKristaFieldType = FieldAttributeExtractor.getKristaFieldType(innerFieldType);

        // Create the inner field configuration
        Map<String, Object> innerFieldConfig = new HashMap<>();
        innerFieldConfig.put("fieldType", innerKristaFieldType);

        // Create the list field attributes
        Map<String, Object> listAttributes = new HashMap<>();
        listAttributes.put("field", GSON.toJson(innerFieldConfig));

        return new NamedField(fieldName, FieldTypes.LIST_FIELD, listAttributes, Map.of());
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
        String kristaFieldType = FieldAttributeExtractor.getKristaFieldType(fieldType);
        return new NamedField(fieldName, kristaFieldType, Map.of(), Map.of());
    }

}