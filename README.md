# AppSueldo

AppSueldo es una app web responsive para transformar movimientos bancarios desordenados en informacion clara para tomar mejores decisiones.

El MVP inicial es manual: no se conectan bancos, no se hace scraping y no se guardan credenciales bancarias. La base queda preparada para registrar movimientos, categorias, usuarios locales o por Google y resumen mensual por usuario.

## Stack

- Frontend: Next.js, React, Tailwind CSS.
- Backend: Spring Boot, Java 17, Spring Security, Spring Data JPA.
- Base de datos: PostgreSQL.
- Autenticacion: Google OAuth2 y email/password local, backend como fuente de verdad, cookies `HttpOnly` para access token y refresh token.

## Estructura

```txt
appsueldo/
  frontend/
  backend/
  README.md
  docker-compose.yml
```

## PostgreSQL local

Desde la raiz:

```bash
docker compose up -d
```

Base local:

- database: `appsueldo`
- user: `appsueldo`
- password: `appsueldo`
- port: `5432`

## Backend

Requisitos: Java 17+ y Maven.

```bash
cd backend
cp .env.example .env
set -a
source .env
set +a
mvn spring-boot:run
```

Variables esperadas:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/appsueldo
SPRING_DATASOURCE_USERNAME=appsueldo
SPRING_DATASOURCE_PASSWORD=appsueldo
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
JWT_SECRET=change-this-secret-to-at-least-32-characters
JWT_EXPIRATION=900000
REFRESH_TOKEN_EXPIRATION=2592000000
FRONTEND_URL=http://localhost:3000
AUTH_COOKIE_SECURE=false
AUTH_COOKIE_SAME_SITE=Lax
```

El backend usa Flyway. `V1` crea `users`, `categories`, `transactions` y `refresh_tokens`; `V2` agrega soporte para cuentas locales, emails verificados y refresh tokens hasheados.

Endpoints de autenticacion:

- `GET /api/auth/google`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Endpoints de aplicacion:

- `GET /api/health`
- `GET /api/me`
- `GET /api/categories`
- `POST /api/categories`
- `GET /api/transactions`
- `POST /api/transactions`
- `GET /api/dashboard/monthly-summary`

Salvo `GET /api/health` y rutas de autenticacion, los endpoints requieren usuario autenticado. El JWT de acceso se lee desde la cookie `access_token`, con fallback a `Authorization: Bearer`.

## Frontend

```bash
cd frontend
cp .env.example .env.local
npm run dev
```

Variable esperada:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

Rutas incluidas:

- `/`
- `/login`
- `/register`
- `/dashboard`
- `/transactions`
- `/transactions/new`
- `/categories`
- `/settings`

Las llamadas al backend se hacen con `credentials: "include"` para enviar las cookies `HttpOnly`. El frontend no guarda tokens en `localStorage`.

## Flujo de autenticacion

1. El usuario entra a `/login` o `/register`.
2. Puede usar Google OAuth2 o una cuenta local de AppSueldo con email/password.
3. En Google, el frontend abre `GET /api/auth/google` y Spring Security redirige a Google.
4. Al volver, el backend lee `sub`, `email`, `email_verified`, `name` y `picture`.
5. Si el `googleId` existe, se actualiza la cuenta.
6. Si el email existe sin `googleId`, se vincula solo cuando Google entrega email verificado.
7. Si el email ya esta registrado localmente, un registro local duplicado responde `409`.
8. El backend emite un JWT de acceso corto y un refresh token crudo; solo guarda el hash SHA-256 del refresh token en PostgreSQL.
9. El backend setea cookies `HttpOnly` `access_token` y `refresh_token` con `SameSite=Lax`, `Path=/`, `Secure=true` en produccion.
10. El usuario vuelve a `/dashboard` sin tokens en la URL.

`POST /api/auth/refresh` rota el refresh token: revoca el token anterior, crea uno nuevo, refresca cookies y devuelve el usuario autenticado. `POST /api/auth/logout` revoca el refresh token actual y limpia ambas cookies.

## MVP inicial incluido

- Monorepo con `frontend/` y `backend/`.
- Dashboard responsive con datos mock.
- Componentes reutilizables: layout, dashboard, transactions y UI.
- Entidades iniciales: `User`, `Category`, `Transaction`, `RefreshToken`.
- Migraciones PostgreSQL con Flyway.
- Seguridad base con Google OAuth2, cuenta local, JWT en cookie, refresh token hasheado y rotacion.
- Endpoints base por usuario autenticado.

## Proximos pasos sugeridos

- Conectar las pantallas del frontend a los endpoints reales.
- Agregar CRUD completo de categorias y movimientos.
- Agregar importacion CSV antes de cualquier conexion bancaria.
- Agregar proteccion CSRF explicita con `XSRF-TOKEN` y `X-XSRF-TOKEN` antes de produccion.
- Agregar mas tests de controladores y flujos OAuth2.
