# Hasura Plugin Reference

## Overview

The Hasura Plugin allows Valtimo BPMN process tasks to interact with a [Hasura](https://hasura.io)-managed PostgreSQL database. It exposes four service task actions: executing inline SQL, tracking tables, running GraphQL queries, and running GraphQL mutations.

## Plugin Configuration

| Property | Key | Secret | Description |
|---|---|---|---|
| Hasura URL | `hasuraUrl` | No | Base URL of the Hasura instance, e.g. `http://hasura:8080` |
| Admin Secret | `hasuraAdminSecret` | Yes | Value sent as the `x-hasura-admin-secret` header on every request |

## Dependencies

### Backend

```kotlin
dependencies {
    implementation("com.ritense.valtimoplugins:hasura-plugin:1.0.0")
}
```

### Frontend

```json
{
  "dependencies": {
    "@valtimo-plugins/hasura-plugin": "1.0.0"
  }
}
```

In your `app.module.ts`:

```typescript
import {
    HasuraPluginModule,
    hasuraPluginSpecification,
} from '@valtimo-plugins/hasura-plugin';

@NgModule({
    imports: [
        HasuraPluginModule,
    ],
    providers: [
        {
            provide: PLUGIN_TOKEN,
            useValue: [
                hasuraPluginSpecification,
            ]
        }
    ]
})
```

## Actions

### Execute SQL

**Key:** `execute-sql`

Executes a SQL statement configured inline via the Hasura Schema API (`POST /v2/query`). The SQL is entered directly in the process link configuration using a code editor.

| Property | Type | Required | Description |
|---|---|---|---|
| `sql` | `String` | Yes | The SQL statement to execute |

---

### Track Tables

**Key:** `track-tables`

Tracks tables in the `public` schema of the `default` Hasura data source so they are exposed via the GraphQL API (`POST /v1/metadata`). Multiple tables are submitted in a single bulk request; errors on individual tables do not abort the rest.

| Property | Type | Required | Description |
|---|---|---|---|
| `tables` | `List<String>` | Yes | Table names to track |

---

### GraphQL by Input

**Key:** `graphql-by-input`

Executes a GraphQL query against `POST /v1/graphql` and stores the response data in a process variable.

| Property | Type | Required | Description |
|---|---|---|---|
| `query` | `String` | Yes | The GraphQL query document |
| `variables` | `String` | No | JSON object string with query variables |
| `resultProcessVariableName` | `String` | Yes | Process variable name where the response data is stored |

---

### Mutation by Process Variable

**Key:** `mutation-by-process-variable`

Executes a GraphQL mutation, passing the value of a process variable as `{"objects": value}`. Useful for bulk-insert mutations generated from earlier process steps.

| Property | Type | Required | Description |
|---|---|---|---|
| `mutation` | `String` | Yes | The GraphQL mutation document |
| `objectsVariableName` | `String` | Yes | Name of the process variable whose value is passed as the `objects` variable |
| `resultProcessVariableName` | `String` | No | Process variable name where the response data is stored (optional) |
