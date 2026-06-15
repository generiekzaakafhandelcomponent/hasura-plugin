# Getting Started

## Prerequisites

- Java 21
- Node.js 20+
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- A running Valtimo instance (v13.24+)
- A running Hasura instance with a PostgreSQL data source configured

## Backend

Add the dependency to your Valtimo backend project:

```kotlin
dependencies {
    implementation("com.ritense.valtimoplugins:hasura-plugin:1.0.0")
}
```

The plugin auto-configures via Spring Boot's autoconfiguration mechanism — no manual bean registration is required.

## Frontend

Install the package:

```shell
npm install @valtimo-plugins/hasura-plugin
```

Register the module and specification in your `app.module.ts`:

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

## Plugin Configuration

Once the plugin is installed, create a plugin configuration in the Valtimo admin UI under **Plugins**:

| Property | Description |
|---|---|
| **Hasura URL** | Base URL of the Hasura instance, e.g. `http://hasura:8080` |
| **Admin Secret** | The `x-hasura-admin-secret` credential |

## SQL file directory

The **Execute SQL Files** action resolves files relative to a base directory. Set the `HASURA_DDL_DIR` environment variable on your backend container to the directory containing your `.sql` files (default: `/opt/hasura/ddl`).

## Further reading

- [Plugin reference](plugin.md) — all actions and their properties
- [Example Application](example-application.md) — running the bundled demo locally
- [Valtimo custom plugin documentation](https://docs.valtimo.nl/features/plugins/plugins/custom-plugin-definition)
