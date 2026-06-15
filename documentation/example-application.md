# Example Application

This project includes a working example application that demonstrates the Hasura plugin integrated with Valtimo, using a horeca-vergunningen (hospitality licence) case as a showcase.

## Running the example application

All commands below should be run from the **project root** directory unless stated otherwise.

### Prerequisites

- Java 21
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Node.js 20 (use `nvm use 20`)

### Start Docker

Start the supporting services (PostgreSQL, Hasura, Keycloak):

```shell
./gradlew :backend:app:composeUp
```

### Start backend

```shell
./gradlew :backend:app:bootRun
```

### Start frontend

Run the following from the `frontend/` directory:

```shell
nvm use 20
npm run clean
npm install
npm run build
npm start
```

The frontend dev server proxies API calls to the backend via `conf/proxy.conf.json`.

### Keycloak users

The example application has a few test users that are preconfigured.

| Name         | Role           | Username  | Password  |
|--------------|----------------|-----------|-----------|
| James Vance  | ROLE_USER      | user      | user      |
| Asha Miller  | ROLE_ADMIN     | admin     | admin     |
| Morgan Finch | ROLE_DEVELOPER | developer | developer |

## Source code

The source code is split up into two modules:

1. [Frontend](/frontend)
2. [Backend](/backend)
