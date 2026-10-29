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

package com.ritense.valtimoplugins.hasura.client

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.graphql.client.ClientGraphQlResponse
import org.springframework.graphql.client.HttpSyncGraphQlClient
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@SkipComponentScan
@Component
class HasuraClient(
    private val restClient: RestClient,
) {
    private val graphQlClientCache = ConcurrentHashMap<String, HttpSyncGraphQlClient>()
    private val tokenCache = ConcurrentHashMap<String, CachedToken>()

    fun fetchClientCredentialsToken(
        tokenUrl: String,
        clientId: String,
        clientSecret: String,
        forceRefresh: Boolean = false,
    ): String {
        val cacheKey = "$tokenUrl|$clientId"
        if (!forceRefresh) {
            tokenCache[cacheKey]?.let { cached ->
                if (cached.expiresAt.isAfter(Instant.now())) {
                    return cached.value
                }
            }
        }

        logger.debug { "Requesting client-credentials token from $tokenUrl for client $clientId" }
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("client_id", clientId)
            add("client_secret", clientSecret)
        }
        val response = restClient.post()
            .uri(tokenUrl)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: throw IllegalStateException("No response from token endpoint $tokenUrl")

        val accessToken = response["access_token"] as? String
            ?: throw IllegalStateException("Token response from $tokenUrl did not contain an access_token")
        // Refresh a bit before actual expiry to avoid sending an almost-expired token.
        val expiresInSeconds = ((response["expires_in"] as? Number)?.toLong() ?: 60L) - 10L

        tokenCache[cacheKey] = CachedToken(accessToken, Instant.now().plusSeconds(expiresInSeconds))
        return accessToken
    }

    fun executeGraphQlQuery(
        hasuraUrl: String,
        tokenUrl: String,
        clientId: String,
        clientSecret: String,
        query: String,
        variables: Map<String, Any?>,
    ): Map<String, Any>? {
        val token = fetchClientCredentialsToken(tokenUrl, clientId, clientSecret)
        var response = requestGraphQl(hasuraUrl, token, query, variables)

        // Hasura reports an expired/invalid JWT as a GraphQL error on an HTTP 200 response
        // (extensions.code == "invalid-jwt"), not as an HTTP 401, so that's what triggers the retry.
        if (response.errors.any { it.extensions?.get("code") == "invalid-jwt" }) {
            logger.warn { "Hasura rejected the cached token as an invalid JWT, refreshing it and retrying once" }
            val freshToken = fetchClientCredentialsToken(tokenUrl, clientId, clientSecret, forceRefresh = true)
            response = requestGraphQl(hasuraUrl, freshToken, query, variables)
        }

        if (response.errors.isNotEmpty()) {
            val stillInvalidJwt = response.errors.any { it.extensions?.get("code") == "invalid-jwt" }
            throw HasuraGraphQlException(
                statusCode = if (stillInvalidJwt) 401 else 400,
                errors = response.errors.map { it.message },
            )
        }

        return response.getData<Map<String, Any>>()
    }

    private fun requestGraphQl(
        hasuraUrl: String,
        token: String,
        query: String,
        variables: Map<String, Any?>,
    ): ClientGraphQlResponse {
        val baseClient = graphQlClientCache.computeIfAbsent(hasuraUrl) {
            HttpSyncGraphQlClient.builder(restClient)
                .url("$hasuraUrl/v1/graphql")
                .build()
        }
        val client = baseClient.mutate().header("Authorization", "Bearer $token").build()

        return client.document(query)
            .variables(variables)
            .executeSync()
    }

    private data class CachedToken(
        val value: String,
        val expiresAt: Instant,
    )
}
