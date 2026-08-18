# Hasura Plugin for Valtimo

A [Valtimo](https://www.valtimo.nl) plugin that integrates with [Hasura](https://hasura.io) to interact with a Hasura-managed PostgreSQL database from BPMN process tasks. It allows you to do GraphQL query/mutation actions as service task actions.

## Actions

| Action | Key | Description |
|---|---|---|
| GraphQL by Input | `graphql-by-input` | Executes a GraphQL query with optional variables and stores the result in a named process variable |
| Mutation by Process Variable | `mutation-by-process-variable` | Executes a GraphQL mutation, passing the value of a process variable as `{"objects": value}` |

## Plugin Configuration

| Property | Key | Secret | Description |
|---|---|---|---|
| Hasura URL | `hasuraUrl` | No | Base URL of the Hasura instance, e.g. `http://hasura:8080` |
| Admin Secret | `hasuraAdminSecret` | Yes | The `x-hasura-admin-secret` used to authenticate all requests |

## GraphQL by Input

**Action properties:**

| Property | Type | Description |
|---|---|---|
| `query` | `String` | The GraphQL query document |
| `variables` | `String?` | Optional JSON object string with query variables |
| `resultProcessVariableName` | `String` | Process variable name where the response data is stored |

## Mutation by Process Variable

**Action properties:**

| Property | Type | Description |
|---|---|---|
| `mutation` | `String` | The GraphQL mutation document |
| `objectsVariableName` | `String` | Name of the process variable whose value is passed as `{"objects": value}` |
| `resultProcessVariableName` | `String?` | Optional process variable name where the response data is stored |

## Documentation

- [Getting Started](documentation/getting-started.md) — setup and development instructions
- [Example Application](documentation/example-application.md) — running the example app locally
- [Release notes](documentation/release-notes.md) — versiegeschiedenis en wijzigingen
