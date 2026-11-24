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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FieldExtractionStrategyRegistryTest {

    @Test
    void getInstance_returnsNonNull() {
        FieldExtractionStrategyRegistry registry = FieldExtractionStrategyRegistry.getInstance();
        assertNotNull(registry);
    }

    @Test
    void getInstance_returnsSameInstance() {
        FieldExtractionStrategyRegistry r1 = FieldExtractionStrategyRegistry.getInstance();
        FieldExtractionStrategyRegistry r2 = FieldExtractionStrategyRegistry.getInstance();
        assertSame(r1, r2);
    }

    @Test
    void getStrategy_annotation_returnsAnnotationStrategy() {
        FieldExtractionStrategyRegistry registry = FieldExtractionStrategyRegistry.getInstance();
        FieldExtractionStrategyRegistry.FieldExtractionStrategy strategy = registry.getStrategy("annotation");
        assertNotNull(strategy);
        assertInstanceOf(FieldExtractionStrategyRegistry.AnnotationBasedExtractionStrategy.class, strategy);
    }

    @Test
    void getStrategy_default_returnsDefaultStrategy() {
        FieldExtractionStrategyRegistry registry = FieldExtractionStrategyRegistry.getInstance();
        FieldExtractionStrategyRegistry.FieldExtractionStrategy strategy = registry.getStrategy("default");
        assertNotNull(strategy);
        assertInstanceOf(FieldExtractionStrategyRegistry.DefaultExtractionStrategy.class, strategy);
    }

    @Test
    void getStrategy_unknownName_fallsBackToDefault() {
        FieldExtractionStrategyRegistry registry = FieldExtractionStrategyRegistry.getInstance();
        FieldExtractionStrategyRegistry.FieldExtractionStrategy strategy = registry.getStrategy("nonexistent");
        assertNotNull(strategy);
        assertInstanceOf(FieldExtractionStrategyRegistry.DefaultExtractionStrategy.class, strategy);
    }

    @Test
    void registerStrategy_thenGetStrategy_returnsRegistered() {
        FieldExtractionStrategyRegistry registry = FieldExtractionStrategyRegistry.getInstance();
        FieldExtractionStrategyRegistry.FieldExtractionStrategy custom = new FieldExtractionStrategyRegistry.DefaultExtractionStrategy();
        registry.registerStrategy("custom-test", custom);
        FieldExtractionStrategyRegistry.FieldExtractionStrategy retrieved = registry.getStrategy("custom-test");
        assertSame(custom, retrieved);
    }

    @Test
    void defaultStrategy_extractFieldType_returnsText() {
        FieldExtractionStrategyRegistry.DefaultExtractionStrategy strategy =
                new FieldExtractionStrategyRegistry.DefaultExtractionStrategy();
        String type = strategy.extractFieldType(null, "anyField");
        assertEquals("Text", type);
    }

    @Test
    void defaultStrategy_extractFieldAttributes_returnsExpectedKeys() {
        FieldExtractionStrategyRegistry.DefaultExtractionStrategy strategy =
                new FieldExtractionStrategyRegistry.DefaultExtractionStrategy();
        Map<String, Object> attrs = strategy.extractFieldAttributes(null, "anyField");
        assertNotNull(attrs);
        assertTrue(attrs.containsKey("isList"));
        assertTrue(attrs.containsKey("required"));
        assertEquals(false, attrs.get("isList"));
        assertEquals(true, attrs.get("required"));
    }

    @Test
    void annotationStrategy_extractFieldType_withNullMethod_returnsResult() {
        FieldExtractionStrategyRegistry.AnnotationBasedExtractionStrategy strategy =
                new FieldExtractionStrategyRegistry.AnnotationBasedExtractionStrategy();
        // FieldAttributeExtractor.extractFieldType handles null gracefully
        String type = strategy.extractFieldType(null, "anyField");
        assertNotNull(type);
    }

    @Test
    void annotationStrategy_extractFieldAttributes_withNullMethod_returnsMap() {
        FieldExtractionStrategyRegistry.AnnotationBasedExtractionStrategy strategy =
                new FieldExtractionStrategyRegistry.AnnotationBasedExtractionStrategy();
        Map<String, Object> attrs = strategy.extractFieldAttributes(null, "anyField");
        assertNotNull(attrs);
    }
}
