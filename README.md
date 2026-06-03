# AppSueldo

AppSueldo es una app web responsive para transformar movimientos bancarios desordenados en informacion clara para tomar mejores decisiones.

El MVP inicial es manual: no se conectan bancos, no se hace scraping y no se guardan credenciales bancarias. La base queda preparada para registrar movimientos, categorias, usuarios por Google y resumen mensual por usuario.

## Stack

- Frontend: Next.js, React, Tailwind CSS.
- Backend: Spring Boot, Java 17, Spring Security, Spring Data JPA.
- Base de datos: PostgreSQL.
- Autenticacion: Google OAuth2, backend como fuente de verdad, JWT de acceso y refresh token persistido.

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
```

El backend usa Flyway. La primera migracion crea `users`, `categories`, `transactions` y `refresh_tokens`.

Endpoints iniciales:

- `GET /api/health`
- `GET /api/me`
- `GET /api/categories`
- `POST /api/categories`
- `GET /api/transactions`
- `POST /api/transactions`
- `GET /api/dashboard/monthly-summary`
- `GET /api/auth/google`

Salvo `GET /api/health` y rutas de autenticacion, los endpoints requieren usuario autenticado.

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
- `/dashboard`
- `/transactions`
- `/categories`
- `/settings`

## Flujo de autenticacion con Google

1. El usuario entra a `/login`.
2. Presiona `Continuar con Google`.
3. El frontend abre `GET /api/auth/google`.
4. Spring Security redirige a Google.
5. Al volver, el backend lee `sub`, `email`, `name` y `picture`.
6. Si el usuario no existe, se crea en PostgreSQL.
7. Si ya existe, se actualiza su informacion basica.
8. El backend emite un access token JWT y crea un refresh token.
9. El usuario vuelve a `/dashboard`.

En esta primera base el handler redirige con tokens en la URL para dejar el flujo visible durante desarrollo. Para produccion conviene migrar a cookies `HttpOnly`, `Secure`, `SameSite=Lax/Strict` o a un flujo de intercambio de codigo propio del frontend.

## MVP inicial incluido

- Monorepo con `frontend/` y `backend/`.
- Dashboard responsive con datos mock.
- Componentes reutilizables: layout, dashboard, transactions y UI.
- Entidades iniciales: `User`, `Category`, `Transaction`, `RefreshToken`.
- Migraciones PostgreSQL con Flyway.
- Seguridad base con Google OAuth2, JWT y refresh token preparado.
- Endpoints base por usuario autenticado.

## Proximos pasos sugeridos

- Persistir tokens del frontend de forma segura y agregar guard de rutas.
- Agregar refresh endpoint y revocacion/cierre de sesion.
- Conectar las pantallas del frontend a los endpoints reales.
- Agregar CRUD completo de categorias y movimientos.
- Agregar importacion CSV antes de cualquier conexion bancaria.
- Agregar tests de servicios y controladores.
