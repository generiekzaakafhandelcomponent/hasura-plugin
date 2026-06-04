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
import com.ritense.valtimoplugins.hasura.client.HasuraClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.delegate.DelegateExecution
import kotlin.io.path.Path
import kotlin.io.path.readText

private val logger = KotlinLogging.logger {}

@Plugin(
    key = "hasura-plugin",
    title = "Hasura Plugin",
    description = "Interact with a Hasura-managed database via SQL and GraphQL",
)
open class HasuraPlugin(
    private val hasuraClient: HasuraClient,
) {
    private val objectMapper = jacksonObjectMapper()

    @PluginProperty(key = "hasuraUrl", secret = false)
    lateinit var hasuraUrl: String

    @PluginProperty(key = "hasuraAdminSecret", secret = true)
    lateinit var hasuraAdminSecret: String

    @PluginAction(
        key = "execute-sql-files",
        title = "Execute SQL Files",
        description = "Reads SQL files from the HASURA_DDL_DIR environment variable (default: $DEFAULT_DDL_DIR) and executes them in order via the Hasura Schema API",
        activityTypes = [SERVICE_TASK_START],
    )
    open fun runSql(
        execution: DelegateExecution,
        @PluginActionProperty files: List<String>,
    ) {
        val ddlDir = Path(System.getenv("HASURA_DDL_DIR") ?: DEFAULT_DDL_DIR).normalize()
        files.forEach { fileName ->
            val resolved = ddlDir.resolve(fileName).normalize()
            require(resolved.startsWith(ddlDir)) { "File '$fileName' escapes DDL directory" }
            logger.info { "Executing $resolved via Hasura at $hasuraUrl" }
            val sql = try {
                resolved.readText()
            } catch (e: Exception) {
                throw IllegalStateException("Could not read SQL file '$resolved': ${e.message}", e)
            }
            hasuraClient.runSql(hasuraUrl, hasuraAdminSecret, sql)
        }
    }

    @PluginAction(
        key = "track-tables",
        title = "Track Tables",
        description = "Tracks tables in Hasura so they are exposed via the GraphQL API",
        activityTypes = [SERVICE_TASK_START],
    )
    open fun trackTables(
        execution: DelegateExecution,
        @PluginActionProperty tables: List<String>,
    ) {
        logger.info { "Tracking tables in Hasura at $hasuraUrl: $tables" }
        hasuraClient.trackTables(hasuraUrl, hasuraAdminSecret, tables)
    }

    @PluginAction(
        key = "graphql-by-input",
        title = "GraphQL by Input",
        description = "Executes a GraphQL query and stores the result in a process variable",
        activityTypes = [SERVICE_TASK_START],
    )
    open fun graphQlByInput(
        execution: DelegateExecution,
        @PluginActionProperty query: String,
        @PluginActionProperty variables: String?,
        @PluginActionProperty resultProcessVariableName: String,
    ) {
        logger.info { "Executing GraphQL query via Hasura at $hasuraUrl" }
        val data = hasuraClient.executeGraphQlQuery(hasuraUrl, hasuraAdminSecret, query, parseVariables(variables))
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
        val data = hasuraClient.executeGraphQlQuery(hasuraUrl, hasuraAdminSecret, mutation, vars)
        resultProcessVariableName?.let { execution.setVariable(it, data) }
    }

    private fun parseVariables(variables: String?): Map<String, Any> =
        if (!variables.isNullOrBlank()) {
            objectMapper.readValue(variables, object : TypeReference<Map<String, Any>>() {})
        } else {
            emptyMap()
        }

    companion object {
        const val DEFAULT_DDL_DIR = "/opt/hasura/ddl"
    }
}
