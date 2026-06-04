# AppSueldo Frontend

Frontend Next.js de AppSueldo.

```bash
cp .env.example .env.local
npm run dev
```

La URL del backend se configura con:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Estructura

```txt
src/
  app/                       # Rutas App Router
  components/
    layout/                  # Navegacion y shell autenticado
    ui/                      # Primitivas visuales compartidas
  features/
    auth/                    # API real de auth
    bank-connections/        # API/data provider de conexiones
    categories/              # API/data provider de categorias
    dashboard/               # API/data provider de resumen
    transactions/            # API/data provider de movimientos
  lib/
    api/                     # Cliente HTTP centralizado
    mocks/                   # Mocks de dominio y presentacion
  types/                     # Tipos de dominio y UI
```

## Datos y API

Auth ya usa backend real con cookies `HttpOnly`. Las pantallas protegidas siguen usando mocks en esta etapa.

El flujo preparado es:

1. Las rutas importan data providers desde `features/*/data`.
2. Los data providers devuelven mocks centralizados desde `lib/mocks`.
3. Los API clients en `features/*/api` ya existen y usan `lib/api/client`.
4. En la siguiente etapa, los data providers pueden cambiar de mocks a API real sin reordenar pantallas.

El cliente HTTP usa `credentials: "include"` y no usa `localStorage`, `sessionStorage` ni tokens en URL.
