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

import app.krista.model.field.NamedField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NamedFieldFactory Tests")
class NamedFieldFactoryTest {

    @Test
    @DisplayName("createTextField returns a non-null NamedField")
    void testCreateTextField() {
        NamedField field = NamedFieldFactory.createTextField("TestTextField");
        assertNotNull(field, "createTextField should return a non-null NamedField");
    }

    @Test
    @DisplayName("createSwitchField returns a non-null NamedField")
    void testCreateSwitchField() {
        NamedField field = NamedFieldFactory.createSwitchField("TestSwitchField");
        assertNotNull(field, "createSwitchField should return a non-null NamedField");
    }

    @Test
    @DisplayName("createListField returns a non-null NamedField and invokes RESOLVER.extractInnerFieldType")
    void testCreateListField() {
        Map<String, Object> attrs = new HashMap<>();
        // "[ Text ]" is a list type — exercises FieldAttributeExtractor.isListField and
        // extractInnerFieldType via the RESOLVER anonymous inner class
        NamedField field = NamedFieldFactory.createListField("TestListField", "[ Text ]", attrs);
        assertNotNull(field, "createListField should return a non-null NamedField");
    }

    @Test
    @DisplayName("createSimpleField returns a non-null NamedField")
    void testCreateSimpleField() {
        Map<String, Object> attrs = new HashMap<>();
        NamedField field = NamedFieldFactory.createSimpleField("TestSimpleField", "Text", attrs);
        assertNotNull(field, "createSimpleField should return a non-null NamedField");
    }

    @Test
    @DisplayName("createFieldWithAttributes with list type exercises full RESOLVER delegation")
    void testCreateFieldWithAttributes_ListType() {
        Map<String, Object> attrs = new HashMap<>();
        NamedField field = NamedFieldFactory.createFieldWithAttributes("ListAttrField", "[ Text ]", attrs);
        assertNotNull(field, "createFieldWithAttributes (list type) should return a non-null NamedField");
    }

    @Test
    @DisplayName("createFieldWithAttributes with simple type returns a non-null NamedField")
    void testCreateFieldWithAttributes_SimpleType() {
        Map<String, Object> attrs = new HashMap<>();
        NamedField field = NamedFieldFactory.createFieldWithAttributes("SimpleAttrField", "Text", attrs);
        assertNotNull(field, "createFieldWithAttributes (simple type) should return a non-null NamedField");
    }
}
