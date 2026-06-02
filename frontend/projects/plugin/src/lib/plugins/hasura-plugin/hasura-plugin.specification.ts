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

import {PluginSpecification} from "@valtimo/plugin";
import {HasuraPluginConfigurationComponent} from "./components/hasura-plugin-configuration/hasura-plugin-configuration.component";
import {HASURA_PLUGIN_LOGO_BASE64} from "./assets";
import {RunSqlActionConfigurationComponent} from "./components/run-sql-action-configuration/run-sql-action-configuration.component";
import {TrackTablesActionConfigurationComponent} from "./components/track-tables-action-configuration/track-tables-action-configuration.component";
import {ExecuteGraphQlQueryActionConfigurationComponent} from "./components/execute-graphql-query-action-configuration/execute-graphql-query-action-configuration.component";
import {ExecuteGraphQlMutationActionConfigurationComponent} from "./components/execute-graphql-mutation-action-configuration/execute-graphql-mutation-action-configuration.component";

const hasuraPluginSpecification: PluginSpecification = {
  pluginId: "hasura-plugin",
  pluginConfigurationComponent: HasuraPluginConfigurationComponent,
  pluginLogoBase64: HASURA_PLUGIN_LOGO_BASE64,
  functionConfigurationComponents: {
    "run-sql": RunSqlActionConfigurationComponent,
    "track-tables": TrackTablesActionConfigurationComponent,
    "execute-graphql-query": ExecuteGraphQlQueryActionConfigurationComponent,
    "execute-graphql-mutation": ExecuteGraphQlMutationActionConfigurationComponent,
  },
  pluginTranslations: {
    nl: {
      title: "Hasura Plugin",
      description: "Interactie met een Hasura-beheerde database via SQL en GraphQL.",
      configurationTitle: "Configuratienaam",
      hasuraUrl: "Hasura URL",
      hasuraAdminSecret: "Hasura Admin Secret",
      "run-sql": "SQL uitvoeren",
      sql: "SQL",
      "track-tables": "Tabellen volgen",
      tables: "Te volgen tabellen",
      "execute-graphql-query": "GraphQL query uitvoeren",
      query: "GraphQL query",
      "execute-graphql-mutation": "GraphQL mutatie uitvoeren",
      mutation: "GraphQL mutatie",
      variables: "Variabelen (JSON)",
      objectsVariableName: "Procesvariabele met objecten",
      resultProcessVariableName: "Resultaat procesvariabele",
    },
    en: {
      title: "Hasura Plugin",
      description: "Interact with a Hasura-managed database via SQL and GraphQL.",
      configurationTitle: "Configuration Name",
      hasuraUrl: "Hasura URL",
      hasuraAdminSecret: "Hasura Admin Secret",
      "run-sql": "Run SQL",
      sql: "SQL",
      "track-tables": "Track Tables",
      tables: "Tables to track",
      "execute-graphql-query": "Execute GraphQL Query",
      query: "GraphQL query",
      "execute-graphql-mutation": "Execute GraphQL Mutation",
      mutation: "GraphQL mutation",
      variables: "Variables (JSON)",
      objectsVariableName: "Process variable containing objects",
      resultProcessVariableName: "Result process variable",
    },
  },
};

export {hasuraPluginSpecification};
