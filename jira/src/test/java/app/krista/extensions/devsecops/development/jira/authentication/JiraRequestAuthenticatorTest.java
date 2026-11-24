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

package app.krista.extensions.devsecops.development.jira.authentication;

import app.krista.ksdk.context.AuthorizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JiraRequestAuthenticator Tests")
class JiraRequestAuthenticatorTest {

    @Mock
    private JiraService jiraService;

    @Mock
    private AuthorizationContext authorizationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Constructor injects JiraService and AuthorizationContext")
    void testConstructor_InjectsDependencies() {
        JiraRequestAuthenticator auth = new JiraRequestAuthenticator(jiraService, authorizationContext);
        assertNotNull(auth);
    }

    @Test
    @DisplayName("buildAuthorizationUrl delegates to jiraService.buildSecuredAuthorizationUrl")
    void testBuildAuthorizationUrl_DelegatesToJiraService() throws Exception {
        String accountId = "account-123";
        String expectedUrl = "https://auth.atlassian.com/authorize?state=secured";
        when(jiraService.buildSecuredAuthorizationUrl(accountId)).thenReturn(expectedUrl);

        JiraRequestAuthenticator auth = new JiraRequestAuthenticator(jiraService, authorizationContext);

        java.lang.reflect.Method method = JiraRequestAuthenticator.class.getDeclaredMethod("buildAuthorizationUrl", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(auth, accountId);

        assertEquals(expectedUrl, result);
        verify(jiraService).buildSecuredAuthorizationUrl(accountId);
    }

    @Test
    @DisplayName("JiraRequestAuthenticator has @Service annotation")
    void testHasServiceAnnotation() {
        assertTrue(JiraRequestAuthenticator.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }
}
