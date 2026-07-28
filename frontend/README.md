# Stock Management — React Client

The React single-page application for the Stock Management System. It talks to the Spring Boot
backend over its REST API using JWT authentication.

For full-stack setup (backend, database and frontend together with Docker), see the
[root README](../README.md).

## Requirements

- Node.js 20+ and npm

## Getting started

Install dependencies:

```bash
npm ci
```

Start the development server (http://localhost:3000):

```bash
npm start
```

Run the tests:

```bash
npm test -- --watchAll=false
```

Create a production build:

```bash
npm run build
```

## Configuration

The API base URL is read from the `REACT_APP_API_URL` environment variable
(Create React App only exposes variables prefixed with `REACT_APP_`).

When it is not set, the client falls back to `http://localhost:8080/api`.

Environment templates are provided:

- `.env.example` — template to copy
- `.env.development` — local development defaults
- `.env.production` — production build defaults

## Implemented areas

Routes currently served by the application:

| Route | Area |
|---|---|
| `/login` | Sign in |
| `/register` | Create an account |
| `/dashboard` | Overview and low-stock summary |
| `/products` | Product management |
| `/categories` | Category management |
| `/suppliers` | Supplier management |
| `/transactions` | Stock transactions (purchase / sale / adjustment) |
| `/profile` | User profile |

Unknown routes and unauthenticated visitors are redirected to `/login`.
