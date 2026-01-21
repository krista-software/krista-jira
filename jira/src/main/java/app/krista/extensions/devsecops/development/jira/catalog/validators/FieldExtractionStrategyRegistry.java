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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for different field extraction strategies.
 * Provides a strategy pattern for extracting field information from different sources.
 */
public class FieldExtractionStrategyRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldExtractionStrategyRegistry.class);
    
    private static final FieldExtractionStrategyRegistry INSTANCE = new FieldExtractionStrategyRegistry();
    
    private final Map<String, FieldExtractionStrategy> strategies = new HashMap<>();
    
    private FieldExtractionStrategyRegistry() {
        // Register default strategies
        strategies.put("annotation", new AnnotationBasedExtractionStrategy());
        strategies.put("default", new DefaultExtractionStrategy());
    }
    
    public static FieldExtractionStrategyRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Gets the appropriate extraction strategy.
     * 
     * @param strategyName The strategy name ("annotation", "default", etc.)
     * @return The extraction strategy
     */
    public FieldExtractionStrategy getStrategy(String strategyName) {
        return strategies.getOrDefault(strategyName, strategies.get("default"));
    }
    
    /**
     * Registers a new extraction strategy.
     * 
     * @param name The strategy name
     * @param strategy The strategy implementation
     */
    public void registerStrategy(String name, FieldExtractionStrategy strategy) {
        strategies.put(name, strategy);
        LOGGER.debug("Registered field extraction strategy: {}", name);
    }
    
    /**
     * Interface for field extraction strategies.
     */
    public interface FieldExtractionStrategy {
        String extractFieldType(Method method, String fieldName);
        Map<String, Object> extractFieldAttributes(Method method, String fieldName);
    }
    
    /**
     * Annotation-based extraction strategy using @Field.Desc annotations.
     */
    public static class AnnotationBasedExtractionStrategy implements FieldExtractionStrategy {
        
        @Override
        public String extractFieldType(Method method, String fieldName) {
            return FieldAttributeExtractor.extractFieldType(method, fieldName);
        }
        
        @Override
        public Map<String, Object> extractFieldAttributes(Method method, String fieldName) {
            return FieldAttributeExtractor.extractFieldAttributes(method, fieldName);
        }
    }
    
    /**
     * Default extraction strategy that provides safe fallbacks.
     */
    public static class DefaultExtractionStrategy implements FieldExtractionStrategy {
        
        @Override
        public String extractFieldType(Method method, String fieldName) {
            return "Text"; // Safe default
        }
        
        @Override
        public Map<String, Object> extractFieldAttributes(Method method, String fieldName) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("isList", false);
            attributes.put("required", true);
            return attributes;
        }
    }
}
