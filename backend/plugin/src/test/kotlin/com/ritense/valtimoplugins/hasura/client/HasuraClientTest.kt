/*
 * Copyright 2015-2026 Ritense BV, the Netherlands.
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

package com.ritense.valtimoplugins.hasura.client

import com.ritense.valtimoplugins.hasura.BaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.ResponseActions
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

class HasuraClientTest : BaseTest() {

    private lateinit var restClient: RestClient
    private lateinit var hasuraClient: HasuraClient

    @BeforeEach
    fun setUp() {
        restClient = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        hasuraClient = HasuraClient(restClient)
    }

    @Test
    fun `fetchClientCredentialsToken returns access token from response`() {
        stubTokenResponses(mapOf("access_token" to "abc123", "expires_in" to 3600))

        val token = hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)

        assertEquals("abc123", token)
    }

    @Test
    fun `fetchClientCredentialsToken posts the client-credentials grant as form data`() {
        stubTokenResponses(mapOf("access_token" to "abc123", "expires_in" to 3600))

        hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)

        // Stubbing the chain above itself triggers one invocation, so the actual
        // request is the most recently captured one rather than the only one.
        val captor = argumentCaptor<LinkedMultiValueMap<String, String>>()
        verify(
            restClient.post().uri(TOKEN_URL).contentType(MediaType.APPLICATION_FORM_URLENCODED),
            atLeastOnce(),
        ).body(captor.capture())
        val form = captor.lastValue
        assertEquals(listOf("client_credentials"), form["grant_type"])
        assertEquals(listOf(CLIENT_ID), form["client_id"])
        assertEquals(listOf(CLIENT_SECRET), form["client_secret"])
    }

    @Test
    fun `fetchClientCredentialsToken reuses the cached token on subsequent calls`() {
        // If the cache were bypassed, the second call would see the second stubbed response.
        stubTokenResponses(
            mapOf("access_token" to "token-1", "expires_in" to 3600),
            mapOf("access_token" to "token-2", "expires_in" to 3600),
        )

        val firstToken = hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)
        val secondToken = hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)

        assertEquals("token-1", firstToken)
        assertEquals("token-1", secondToken)
    }

    @Test
    fun `fetchClientCredentialsToken bypasses the cache when forceRefresh is true`() {
        stubTokenResponses(
            mapOf("access_token" to "token-1", "expires_in" to 3600),
            mapOf("access_token" to "token-2", "expires_in" to 3600),
        )

        val firstToken = hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)
        val secondToken = hasuraClient.fetchClientCredentialsToken(
            TOKEN_URL, CLIENT_ID, CLIENT_SECRET, forceRefresh = true,
        )

        assertEquals("token-1", firstToken)
        assertEquals("token-2", secondToken)
    }

    @Test
    fun `fetchClientCredentialsToken refetches once the cached token has expired`() {
        // expires_in minus the 10s safety margin puts the cached expiry in the past immediately,
        // so the second call should trigger a refetch even without forceRefresh.
        stubTokenResponses(
            mapOf("access_token" to "token-1", "expires_in" to 5),
            mapOf("access_token" to "token-2", "expires_in" to 5),
        )

        val firstToken = hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)
        val secondToken = hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)

        assertEquals("token-1", firstToken)
        assertEquals("token-2", secondToken)
    }

    @Test
    fun `fetchClientCredentialsToken keeps separate cache entries per client id`() {
        stubTokenResponses(
            mapOf("access_token" to "token-1", "expires_in" to 3600),
            mapOf("access_token" to "token-2", "expires_in" to 3600),
        )

        val firstToken = hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)
        val secondToken = hasuraClient.fetchClientCredentialsToken(TOKEN_URL, "other-client-id", CLIENT_SECRET)

        assertNotEquals(firstToken, secondToken)
    }

    @Test
    fun `fetchClientCredentialsToken throws when the response has no access_token`() {
        stubTokenResponses(mapOf("token_type" to "Bearer"))

        val exception = assertThrows(IllegalStateException::class.java) {
            hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)
        }

        assertTrue(exception.message!!.contains("access_token"))
    }

    @Test
    fun `fetchClientCredentialsToken throws when the token endpoint returns no body`() {
        stubTokenResponses(null)

        val exception = assertThrows(IllegalStateException::class.java) {
            hasuraClient.fetchClientCredentialsToken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET)
        }

        assertTrue(exception.message!!.contains("No response"))
    }

    private fun stubTokenResponses(vararg responses: Map<String, Any>?) {
        whenever(
            restClient.post().uri(TOKEN_URL).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(any<LinkedMultiValueMap<String, String>>())
                .retrieve()
                .body(any<ParameterizedTypeReference<Map<String, Any>>>())
        ).thenReturn(responses.first(), *responses.drop(1).toTypedArray())
    }

    @Nested
    inner class ExecuteGraphQlQuery {

        private lateinit var mockServer: MockRestServiceServer
        private lateinit var hasuraClient: HasuraClient

        @BeforeEach
        fun setUp() {
            val builder = RestClient.builder()
            mockServer = MockRestServiceServer.bindTo(builder).build()
            hasuraClient = HasuraClient(builder.build())
        }

        @Test
        fun `returns the data from a successful response`() {
            expectTokenRequest(ACCESS_TOKEN)
            expectGraphQlRequest(ACCESS_TOKEN)
                .andRespond(withSuccess("""{"data":{"ping":"pong"}}""", MediaType.APPLICATION_JSON))

            val result = hasuraClient.executeGraphQlQuery(HASURA_URL, TOKEN_URL, CLIENT_ID, CLIENT_SECRET, QUERY, VARIABLES)

            assertEquals(mapOf("ping" to "pong"), result)
            mockServer.verify()
        }

        @Test
        fun `sends the query, variables and bearer token with the request`() {
            expectTokenRequest(ACCESS_TOKEN)
            mockServer.expect(requestTo("$HASURA_URL/v1/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $ACCESS_TOKEN"))
                .andExpect(jsonPath("$.query").value(QUERY))
                .andExpect(jsonPath("$.variables.id").value("42"))
                .andRespond(withSuccess("""{"data":{}}""", MediaType.APPLICATION_JSON))

            hasuraClient.executeGraphQlQuery(HASURA_URL, TOKEN_URL, CLIENT_ID, CLIENT_SECRET, QUERY, VARIABLES)

            mockServer.verify()
        }

        @Test
        fun `retries once with a fresh token when Hasura reports an invalid JWT`() {
            expectTokenRequest("token-1")
            expectGraphQlRequest("token-1")
                .andRespond(withSuccess(invalidJwtErrorResponse(), MediaType.APPLICATION_JSON))
            expectTokenRequest("token-2")
            expectGraphQlRequest("token-2")
                .andRespond(withSuccess("""{"data":{"ping":"pong"}}""", MediaType.APPLICATION_JSON))

            val result = hasuraClient.executeGraphQlQuery(HASURA_URL, TOKEN_URL, CLIENT_ID, CLIENT_SECRET, QUERY, VARIABLES)

            assertEquals(mapOf("ping" to "pong"), result)
            mockServer.verify()
        }

        @Test
        fun `throws a 401 HasuraGraphQlException when the token is still invalid after the retry`() {
            expectTokenRequest("token-1")
            expectGraphQlRequest("token-1")
                .andRespond(withSuccess(invalidJwtErrorResponse(), MediaType.APPLICATION_JSON))
            expectTokenRequest("token-2")
            expectGraphQlRequest("token-2")
                .andRespond(withSuccess(invalidJwtErrorResponse(), MediaType.APPLICATION_JSON))

            val exception = assertThrows(HasuraGraphQlException::class.java) {
                hasuraClient.executeGraphQlQuery(HASURA_URL, TOKEN_URL, CLIENT_ID, CLIENT_SECRET, QUERY, VARIABLES)
            }

            assertEquals(401, exception.statusCode)
            mockServer.verify()
        }

        @Test
        fun `throws a 400 HasuraGraphQlException for other graphql errors without retrying`() {
            expectTokenRequest(ACCESS_TOKEN)
            expectGraphQlRequest(ACCESS_TOKEN)
                .andRespond(withSuccess("""{"errors":[{"message":"field not found"}]}""", MediaType.APPLICATION_JSON))

            val exception = assertThrows(HasuraGraphQlException::class.java) {
                hasuraClient.executeGraphQlQuery(HASURA_URL, TOKEN_URL, CLIENT_ID, CLIENT_SECRET, QUERY, VARIABLES)
            }

            assertEquals(400, exception.statusCode)
            assertEquals(listOf("field not found"), exception.errors)
            mockServer.verify()
        }

        private fun expectTokenRequest(accessToken: String) {
            mockServer.expect(requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                    withSuccess(
                        """{"access_token":"$accessToken","expires_in":3600}""",
                        MediaType.APPLICATION_JSON,
                    ),
                )
        }

        private fun expectGraphQlRequest(accessToken: String): ResponseActions =
            mockServer.expect(requestTo("$HASURA_URL/v1/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken"))

        private fun invalidJwtErrorResponse() =
            """{"errors":[{"message":"Could not verify JWT: JWTExpired","extensions":{"code":"invalid-jwt"}}]}"""
    }

    private companion object {
        const val HASURA_URL = "https://hasura.example.com"
        const val TOKEN_URL = "https://keycloak.example.com/realms/test/protocol/openid-connect/token"
        const val CLIENT_ID = "client-id"
        const val CLIENT_SECRET = "client-secret"
        const val ACCESS_TOKEN = "abc123"
        const val QUERY = "query { ping }"
        val VARIABLES = mapOf<String, Any?>("id" to "42")
    }
}
