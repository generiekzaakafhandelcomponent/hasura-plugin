# Hasura Plugin Reference

## Overview

The Hasura Plugin allows Valtimo BPMN process tasks to interact with a [Hasura](https://hasura.io)-managed PostgreSQL database. It exposes two service task actions: running GraphQL queries and running GraphQL mutations.

## Plugin Configuration

| Property | Key | Secret | Description |
|---|---|---|---|
| Hasura URL | `hasuraUrl` | No | Base URL of the Hasura instance, e.g. `http://hasura:8080` |
| Token URL | `tokenUrl` | No | OAuth2 token endpoint used to obtain a client-credentials access token, e.g. `https://keycloak.example.com/realms/<realm>/protocol/openid-connect/token` |
| Client ID | `clientId` | No | OAuth2 client ID used for the client-credentials grant |
| Client Secret | `clientSecret` | Yes | OAuth2 client secret used for the client-credentials grant |

The plugin authenticates with Hasura via a Keycloak client-credentials grant: it fetches an access token from `tokenUrl` using `clientId`/`clientSecret`, caches it until shortly before expiry, and sends it as a `Bearer` token on every GraphQL request to `hasuraUrl`. If Hasura reports the cached token as an invalid JWT, the plugin automatically fetches a fresh token and retries the request once.

## Error Handling

If Hasura returns GraphQL errors (including an unrecoverable invalid-JWT error after the retry), the action fails the service task instead of storing an error result in the process variable. The thrown exception's message has the form `GraphQL errors: <messages>`; check the process incident or the application logs for details.

## Dependencies

### Backend

```kotlin
dependencies {
    implementation("com.ritense.valtimoplugins:hasura-plugin:1.5.0")
}
```

### Frontend

```json
{
  "dependencies": {
    "@valtimo-plugins/hasura-plugin": "1.5.0"
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
