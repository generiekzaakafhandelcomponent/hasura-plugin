# @valtimo-plugins/hasura-plugin

Angular plugin library for the [Hasura Plugin](../../backend/plugin) in the Valtimo platform. Provides configuration UI components for all Hasura plugin actions.

## Installation

```bash
npm install @valtimo-plugins/hasura-plugin
```

### Register in your app module

```typescript
import { HasuraPluginModule } from '@valtimo-plugins/hasura-plugin';
import { hasuraPluginSpecification } from '@valtimo-plugins/hasura-plugin';
import { PLUGINS_TOKEN } from '@valtimo/plugin';

@NgModule({
  imports: [
    HasuraPluginModule,
    // ...
  ],
  providers: [
    {
      provide: PLUGINS_TOKEN,
      useValue: [hasuraPluginSpecification],
    },
  ],
})
export class AppModule {}
```

## Plugin configuration

| Property           | Type     | Secret | Description                                      |
|--------------------|----------|--------|--------------------------------------------------|
| `hasuraUrl`        | `string` | No     | Base URL of the Hasura GraphQL Engine (e.g. `http://localhost:8080`) |
| `hasuraAdminSecret`| `string` | Yes    | Admin secret used in the `X-Hasura-Admin-Secret` header |

## Actions

### `execute-sql-files` — Execute SQL Files

Reads SQL files from the directory specified by the `HASURA_DDL_DIR` environment variable (default: `/opt/hasura/ddl`) and executes them in order via the Hasura Schema API (`/v2/query`).

| Property | Type            | Required | Description                                        |
|----------|-----------------|----------|----------------------------------------------------|
| `files`  | `Array<string>` | Yes      | File names (relative to `HASURA_DDL_DIR`) to execute in order |

---

### `track-tables` — Track Tables

Exposes one or more tables via the Hasura GraphQL API by tracking them through the Metadata API (`/v1/metadata`). Already-tracked tables are silently skipped.

| Property | Type            | Required | Description                    |
|----------|-----------------|----------|--------------------------------|
| `tables` | `Array<string>` | Yes      | Table names to track in Hasura |

---

### `graphql-by-input` — GraphQL by Input

Executes an arbitrary GraphQL query with optional static variables and stores the result in a process variable.

| Property                  | Type     | Required | Description                                        |
|---------------------------|----------|----------|----------------------------------------------------|
| `query`                   | `string` | Yes      | GraphQL query string                               |
| `variables`               | `string` | No       | Variables as a JSON string                         |
| `resultProcessVariableName` | `string` | Yes    | Process variable name where the result is stored   |

---

### `mutation-by-process-variable` — Mutation by Process Variable

Executes a GraphQL mutation, passing the value of a process variable as the `$objects` variable. The process variable may be a JSON string (serialized by a script task) or a direct object.

| Property                    | Type     | Required | Description                                                     |
|-----------------------------|----------|----------|-----------------------------------------------------------------|
| `mutation`                  | `string` | Yes      | GraphQL mutation string; must declare `$objects` as a variable  |
| `objectsVariableName`       | `string` | Yes      | Name of the process variable containing the objects to insert   |
| `resultProcessVariableName` | `string` | No       | Process variable where the mutation result is stored            |

## Development setup

This library is built with [ng-packagr](https://github.com/ng-packagr/ng-packagr). To build:

```bash
ng build @valtimo-plugins/hasura-plugin
# output: projects/plugin/dist/
```
