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
import {GraphQlByInputActionConfigurationComponent} from "./components/graphql-by-input-action-configuration/graphql-by-input-action-configuration.component";
import {MutationByProcessVariableActionConfigurationComponent} from "./components/mutation-by-process-variable-action-configuration/mutation-by-process-variable-action-configuration.component";

const hasuraPluginSpecification: PluginSpecification = {
  pluginId: "hasura-plugin",
  pluginConfigurationComponent: HasuraPluginConfigurationComponent,
  pluginLogoBase64: HASURA_PLUGIN_LOGO_BASE64,
  functionConfigurationComponents: {
    "graphql-by-input": GraphQlByInputActionConfigurationComponent,
    "mutation-by-process-variable": MutationByProcessVariableActionConfigurationComponent,
  },
  pluginTranslations: {
    nl: {
      title: "Hasura Plugin",
      description: "Interactie met een Hasura-beheerde database via SQL en GraphQL.",
      configurationTitle: "Configuratienaam",
      hasuraUrl: "Hasura URL",
      hasuraAdminSecret: "Hasura Admin Secret",
      "graphql-by-input": "GraphQL via invoer",
      query: "GraphQL query",
      "mutation-by-process-variable": "Mutatie via procesvariabele",
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
      "graphql-by-input": "GraphQL by Input",
      query: "GraphQL query",
      "mutation-by-process-variable": "Mutation by Process Variable",
      mutation: "GraphQL mutation",
      variables: "Variables (JSON)",
      objectsVariableName: "Process variable containing objects for mutation",
      resultProcessVariableName: "Result process variable",
    },
  },
};

export {hasuraPluginSpecification};
