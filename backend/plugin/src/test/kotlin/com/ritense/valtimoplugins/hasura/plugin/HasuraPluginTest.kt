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
import com.ritense.valtimoplugins.hasura.client.HasuraGraphQlException
import com.ritense.valueresolver.ValueResolverService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution

class HasuraPluginTest : BaseTest() {

    private lateinit var hasuraClient: HasuraClient
    private lateinit var execution: DelegateExecution
    private lateinit var plugin: HasuraPlugin

    @BeforeEach
    fun setUp() {
        hasuraClient = mock()
        execution = mock()
        plugin = HasuraPlugin(hasuraClient, mock<ValueResolverService>()).apply {
            hasuraUrl = HASURA_URL
            tokenUrl = TOKEN_URL
            clientId = CLIENT_ID
            clientSecret = CLIENT_SECRET
        }
    }

    @Test
    fun `graphQlByInput fails the task and does not store a result when Hasura denies permission`() {
        val permissionError = HasuraGraphQlException(
            statusCode = 400,
            errors = listOf("field \"secret\" not found in type: 'query_root'"),
        )
        whenever(
            hasuraClient.executeGraphQlQuery(any(), any(), any(), any(), any(), any()),
        ).thenThrow(permissionError)

        val exception = assertThrows(HasuraGraphQlException::class.java) {
            plugin.graphQlByInput(execution, QUERY, null, "result")
        }

        assertEquals(permissionError, exception)
        verify(execution, never()).setVariable(any(), any())
    }

    private companion object {
        const val HASURA_URL = "https://hasura.example.com"
        const val TOKEN_URL = "https://keycloak.example.com/realms/test/protocol/openid-connect/token"
        const val CLIENT_ID = "client-id"
        const val CLIENT_SECRET = "client-secret"
        const val QUERY = "query { ping }"
    }
}