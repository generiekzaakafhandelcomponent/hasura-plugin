/*
 * Copyright 2026 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimoplugins.hasura.plugin

import com.ritense.valtimoplugins.hasura.BaseTest
import com.ritense.valtimoplugins.hasura.client.HasuraClient
import com.ritense.valueresolver.ValueResolverService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution

internal class HasuraPluginTest : BaseTest() {
    private lateinit var hasuraClient: HasuraClient
    private lateinit var valueResolver: ValueResolverService
    private lateinit var execution: DelegateExecution
    private lateinit var plugin: HasuraPlugin

    @BeforeEach
    fun setUp() {
        hasuraClient = mock()
        valueResolver = mock()
        execution = mock()
        whenever(execution.processInstanceId).thenReturn(PROCESS_INSTANCE_ID)

        plugin = HasuraPlugin(hasuraClient, valueResolver)
        plugin.hasuraUrl = "http://hasura:8080"
        plugin.hasuraAdminSecret = "secret"
    }

    @Test
    fun `should replace supported string values with resolved values`() {
        whenever(valueResolver.supportsValue("pv:caseId")).thenReturn(true)
        whenever(valueResolver.resolveValues(PROCESS_INSTANCE_ID, execution, listOf("pv:caseId")))
            .thenReturn(mapOf("pv:caseId" to "123"))

        val variables = executeAndCaptureVariables("""{"id": "pv:caseId"}""")

        assertEquals(mapOf("id" to "123"), variables)
    }

    @Test
    fun `should keep unsupported and non-string values unchanged`() {
        whenever(valueResolver.supportsValue("pv:caseId")).thenReturn(true)
        whenever(valueResolver.supportsValue("https://example.com")).thenReturn(false)
        whenever(valueResolver.resolveValues(PROCESS_INSTANCE_ID, execution, listOf("pv:caseId")))
            .thenReturn(mapOf("pv:caseId" to "123"))

        val variables = executeAndCaptureVariables(
            """{"id": "pv:caseId", "url": "https://example.com", "limit": 10, "filter": {"name": "pv:caseId"}}""",
        )

        assertEquals(
            mapOf("id" to "123", "url" to "https://example.com", "limit" to 10, "filter" to mapOf("name" to "pv:caseId")),
            variables,
        )
    }

    @Test
    fun `should keep supported value unchanged when it is not resolved`() {
        whenever(valueResolver.supportsValue("pv:unknown")).thenReturn(true)
        whenever(valueResolver.resolveValues(PROCESS_INSTANCE_ID, execution, listOf("pv:unknown")))
            .thenReturn(emptyMap())

        val variables = executeAndCaptureVariables("""{"id": "pv:unknown"}""")

        assertEquals(mapOf("id" to "pv:unknown"), variables)
    }

    @Test
    fun `should pass null when value resolves to null`() {
        whenever(valueResolver.supportsValue("pv:empty")).thenReturn(true)
        whenever(valueResolver.resolveValues(PROCESS_INSTANCE_ID, execution, listOf("pv:empty")))
            .thenReturn(mapOf("pv:empty" to null))

        val variables = executeAndCaptureVariables("""{"id": "pv:empty"}""")

        assertEquals(mapOf("id" to null), variables)
    }

    @Test
    fun `should pass empty variables without resolving when variables are blank`() {
        val variables = executeAndCaptureVariables(" ")

        assertEquals(emptyMap<String, Any?>(), variables)
        verify(valueResolver, never()).resolveValues(any<String>(), any(), any())
    }

    private fun executeAndCaptureVariables(variables: String?): Map<String, Any?> {
        plugin.graphQlByInput(execution, QUERY, variables, "result")

        val captor = argumentCaptor<Map<String, Any?>>()
        verify(hasuraClient).executeGraphQlQuery(eq("http://hasura:8080"), eq("secret"), eq(QUERY), captor.capture())
        return captor.firstValue
    }

    companion object {
        private const val PROCESS_INSTANCE_ID = "process-instance-id"
        private const val QUERY = "query { cases { id } }"
    }
}
