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

data class HasuraRunSqlRequest(
    val type: String = "run_sql",
    val args: HasuraRunSqlArgs,
)

data class HasuraRunSqlArgs(
    val source: String = "default",
    val sql: String,
)

data class HasuraRunSqlResponse(
    val result_type: String? = null,
)

data class HasuraTableRef(
    val schema: String = "public",
    val name: String,
)

data class HasuraTrackTableArgs(
    val source: String = "default",
    val table: HasuraTableRef,
)

data class HasuraTrackTableRequest(
    val type: String = "pg_track_table",
    val args: HasuraTrackTableArgs,
)

data class HasuraBulkRequest(
    val type: String = "bulk",
    val args: List<HasuraTrackTableRequest>,
    val continue_on_error: Boolean = true,
)
