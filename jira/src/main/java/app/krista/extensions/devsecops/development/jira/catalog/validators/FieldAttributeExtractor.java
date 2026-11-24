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

package app.krista.extensions.devsecops.development.jira.catalog.validators;

import app.krista.extension.impl.anno.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Extracts field types and attributes from method annotations for proper error handling.
 * This enables creation of correct field types (like List of Text) during validation failures.
 */
public class FieldAttributeExtractor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldAttributeExtractor.class);
    
    private FieldAttributeExtractor() {
        // Utility class
    }
    
    /**
     * Extracts the field type from method parameter annotations.
     * 
     * @param method The method to extract from
     * @param fieldName The field name to find
     * @return The field type (e.g., "[ Text ]", "Text", etc.) or "Text" as default
     */
    public static String extractFieldType(Method method, String fieldName) {
        try {
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                Field.Desc fieldDesc = parameter.getAnnotation(Field.Desc.class);
                if (fieldDesc != null && fieldName.equals(fieldDesc.name())) {
                    String fieldType = fieldDesc.type();
                    LOGGER.debug("Extracted field type '{}' for field '{}'", fieldType, fieldName);
                    return fieldType;
                }
            }
            
            LOGGER.debug("No field type found for field '{}', using default 'Text'", fieldName);
            return "Text"; // Default fallback
        } catch (Exception e) {
            LOGGER.warn("Error extracting field type for field '{}': {}", fieldName, e.getMessage());
            return "Text"; // Safe fallback
        }
    }
    
    /**
     * Extracts field attributes from method parameter annotations.
     * 
     * @param method The method to extract from
     * @param fieldName The field name to find
     * @return Map of field attributes including isList, required, etc.
     */
    public static Map<String, Object> extractFieldAttributes(Method method, String fieldName) {
        Map<String, Object> attributes = new HashMap<>();
        
        try {
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                Field.Desc fieldDesc = parameter.getAnnotation(Field.Desc.class);
                if (fieldDesc != null && fieldName.equals(fieldDesc.name())) {
                    String fieldType = fieldDesc.type();
                    
                    // Determine if this is a list field
                    boolean isList = isListField(fieldType);
                    attributes.put("isList", isList);
                    
                    // Extract required attribute
                    boolean required = fieldDesc.required();
                    attributes.put("required", required);
                    
                    // Extract inner field type for list fields
                    if (isList) {
                        String innerFieldType = extractInnerFieldType(fieldType);
                        attributes.put("innerFieldType", innerFieldType);
                    }
                    
                    LOGGER.debug("Extracted attributes for field '{}': {}", fieldName, attributes);
                    return attributes;
                }
            }
            
            // Default attributes if not found
            attributes.put("isList", false);
            attributes.put("required", true);
            LOGGER.debug("No attributes found for field '{}', using defaults: {}", fieldName, attributes);
            
        } catch (Exception e) {
            LOGGER.warn("Error extracting field attributes for field '{}': {}", fieldName, e.getMessage());
            // Safe defaults
            attributes.put("isList", false);
            attributes.put("required", true);
        }
        
        return attributes;
    }
    
    /**
     * Determines if a field type represents a list field.
     * 
     * @param fieldType The field type string (e.g., "[ Text ]", "Text")
     * @return true if it's a list field, false otherwise
     */
    public static boolean isListField(String fieldType) {
        if (fieldType == null) {
            return false;
        }
        
        String trimmed = fieldType.trim();
        return trimmed.startsWith("[") && trimmed.endsWith("]");
    }
    
    /**
     * Extracts the inner field type from a list field type.
     * 
     * @param listFieldType The list field type (e.g., "[ Text ]")
     * @return The inner field type (e.g., "Text")
     */
    public static String extractInnerFieldType(String listFieldType) {
        if (listFieldType == null || !isListField(listFieldType)) {
            return "Text"; // Default fallback
        }
        
        String trimmed = listFieldType.trim();
        // Remove the brackets and trim
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        
        return inner.isEmpty() ? "Text" : inner;
    }
    
    /**
     * Gets the appropriate field type constant for the Krista field system.
     * 
     * @param fieldType The field type from annotation
     * @return The corresponding Krista field type constant
     */
    public static String getKristaFieldType(String fieldType) {
        if (isListField(fieldType)) {
            return "com.krista.fields.List";
        }
        
        // Map other field types as needed
        switch (fieldType.toLowerCase()) {
            case "text":
                return "com.krista.fields.Text";
            case "boolean":
                return "com.krista.fields.Switch";
            case "email":
                return "com.krista.fields.Email";
            case "number":
                return "com.krista.fields.Number";
            default:
                return "com.krista.fields.Text"; // Safe default
        }
    }
}
