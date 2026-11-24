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

/**
 * Converts HTML to ADF (Atlassian Document Format).
 *
 * <p>Implementation moved to the shared atlassian-sdk
 * ({@link app.krista.sdk.atlassian.adf.HtmlToAdfConverter}); this subclass is
 * retained for backward compatibility with existing call sites.</p>
 */
public class HtmlToAdfConverter extends app.krista.sdk.atlassian.adf.HtmlToAdfConverter {
}
