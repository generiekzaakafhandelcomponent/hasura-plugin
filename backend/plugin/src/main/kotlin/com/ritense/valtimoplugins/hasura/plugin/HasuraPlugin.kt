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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.valtimo.contract.utils.SecurityUtils
import com.ritense.valtimoplugins.hasura.client.HasuraClient
import com.ritense.valueresolver.ValueResolverService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.delegate.DelegateExecution

private val logger = KotlinLogging.logger {}

@Plugin(
    key = "hasura-plugin",
    title = "Hasura Plugin",
    description = "Interact with a Hasura-managed database via GraphQL",
)
open class HasuraPlugin(
    private val hasuraClient: HasuraClient,
    private val valueResolver: ValueResolverService,
) {
    private val objectMapper = jacksonObjectMapper()

    @PluginProperty(key = "hasuraUrl", secret = false)
    lateinit var hasuraUrl: String

    @PluginProperty(key = "hasuraAdminSecret", secret = true)
    lateinit var hasuraAdminSecret: String

    @PluginAction(
        key = "graphql-by-input",
        title = "GraphQL by Input",
        description = "Executes a GraphQL query and stores the result in given named process variable",
        activityTypes = [SERVICE_TASK_START],
    )
    open fun graphQlByInput(
        execution: DelegateExecution,
        @PluginActionProperty query: String,
        @PluginActionProperty variables: String?,
        @PluginActionProperty resultProcessVariableName: String,
    ) {
        logger.info { "Executing GraphQL query via Hasura at $hasuraUrl" }
        val userToken = SecurityUtils.getCurrentJwtTokenValue()
        val data = hasuraClient.executeGraphQlQuery(hasuraUrl, hasuraAdminSecret, userToken, query, parseVariables(execution, variables))
        execution.setVariable(resultProcessVariableName, data)
    }

    @PluginAction(
        key = "mutation-by-process-variable",
        title = "Mutation by Process Variable",
        description = "Executes a GraphQL mutation, passing the value of a process variable as {\"objects\": value}.",
        activityTypes = [SERVICE_TASK_START],
    )
    open fun mutationByProcessVariable(
        execution: DelegateExecution,
        @PluginActionProperty mutation: String,
        @PluginActionProperty objectsVariableName: String,
        @PluginActionProperty resultProcessVariableName: String?,
    ) {
        logger.info { "Executing GraphQL mutation via Hasura at $hasuraUrl" }
        val raw = execution.getVariable(objectsVariableName)
        val objects = if (raw is String) objectMapper.readValue(raw, object : TypeReference<Any>() {}) else raw
        val vars = mapOf("objects" to objects)
        val data = hasuraClient.executeGraphQlQuery(hasuraUrl, hasuraAdminSecret, null, mutation, vars)
        resultProcessVariableName?.let { execution.setVariable(it, data) }
    }

    private fun parseVariables(
        execution: DelegateExecution,
        variables: String?,
    ): Map<String, Any?> =
        if (!variables.isNullOrBlank()) {
            val variableMap: Map<String, Any> = objectMapper.readValue(variables, object : TypeReference<Map<String, Any>>() {})
            val requestedValues = variableMap.values.filterIsInstance<String>().filter { valueResolver.supportsValue(it) }
            val resolvedValues = valueResolver.resolveValues(execution.processInstanceId, execution, requestedValues)
            variableMap.mapValues { (_, value) -> if (value in resolvedValues) resolvedValues[value] else value }
        } else {
            emptyMap()
        }
}
