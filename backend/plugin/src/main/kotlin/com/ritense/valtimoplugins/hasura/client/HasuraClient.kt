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
import org.springframework.graphql.client.HttpSyncGraphQlClient
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@SkipComponentScan
@Component
class HasuraClient(
    private val restClient: RestClient,
) {
    private val graphQlClientCache = ConcurrentHashMap<String, HttpSyncGraphQlClient>()

    fun executeGraphQlQuery(
        hasuraUrl: String,
        adminSecret: String,
        query: String,
        variables: Map<String, Any?>,
    ): Map<String, Any>? {
        val client = graphQlClientCache.computeIfAbsent("$hasuraUrl|$adminSecret") {
            HttpSyncGraphQlClient.builder(restClient)
                .url("$hasuraUrl/v1/graphql")
                .header("x-hasura-admin-secret", adminSecret)
                .build()
        }

        val response = client.document(query)
            .variables(variables)
            .executeSync()

        if (response.errors.isNotEmpty()) {
            throw IllegalStateException("GraphQL errors: ${response.errors.joinToString { it.message }}")
        }

        return response.getData<Map<String, Any>>()
    }
}
