# Plan de integracion Fintoc Movements

## Resumen

Este documento define el plan tecnico interno para integrar Fintoc Movements en
AppSueldo. El objetivo es obtener automaticamente cuentas bancarias y
movimientos del usuario para transformarlos en informacion financiera clara.

El alcance actual es exclusivamente Fintoc Movements / data aggregation. Esta
etapa no incluye pagos, transferencias, payouts, direct debit, direct payments,
checkout, subscriptions ni ningun flujo que mueva dinero desde AppSueldo.

La integracion debe mantener la arquitectura existente:

- Backend: Spring Boot.
- Frontend: Next.js / React / Tailwind.
- Base de datos: PostgreSQL.
- Auth: cookies HttpOnly existentes.
- Modelo base: `BankConnection`, `BankAccount`, `Transaction`,
  `TransactionClassification`.

## Principios de diseno

- El backend es el unico componente que conoce secretos de Fintoc.
- El frontend solo usa la Public Key y tokens temporales del Widget cuando
  Fintoc lo requiere.
- La logica de negocio debe depender de una interfaz de proveedor bancario, no
  directamente del cliente HTTP de Fintoc.
- Los movimientos importados desde Fintoc deben normalizarse al contrato interno
  de AppSueldo: `amount` positivo + `TransactionType`.
- La integracion debe convivir con movimientos manuales y CSV sin romperlos.
- La sincronizacion debe ser idempotente y segura por usuario.

## Alcance y exclusiones

### Incluido

- Crear intentos de conexion bancaria con Fintoc Movements.
- Abrir Fintoc Widget desde frontend.
- Recibir `exchange_token` del Widget.
- Intercambiar `exchange_token` en backend por datos del Link, incluido
  `link_token`.
- Guardar conexion bancaria Fintoc en `BankConnection`.
- Guardar cuentas Fintoc en `BankAccount`.
- Obtener movimientos Fintoc y guardarlos como `Transaction`.
- Actualizar movimientos via Refresh Intents y Webhooks.
- Mostrar estado basico de conexion y sincronizacion.

### Excluido explicitamente

- Payment Initiation.
- Direct payments.
- Direct debit.
- Checkout sessions.
- Payment links.
- Subscriptions.
- Transfers API.
- Inbound/outbound transfers.
- Payouts.
- JWS para mover dinero.
- Cualquier accion que inicie, autorice, reciba, devuelva o ejecute dinero.

## Flujo completo de conexion

### 1. Crear Link Intent

Cuando el usuario quiere conectar un banco, el frontend llama al backend:

```http
POST /api/bank-connections/fintoc/link-intents
```

El backend crea un Link Intent en Fintoc:

```http
POST https://api.fintoc.com/v1/link_intents
Authorization: FINTOC_SECRET_KEY
Content-Type: application/json
```

Parametros iniciales:

- `product`: `movements`
- `country`: `cl`
- `holder_type`: `individual` por defecto para AppSueldo personal

La respuesta de Fintoc incluye `widget_token`. El backend devuelve al frontend
solo lo necesario para abrir el Widget:

- `widgetToken`
- `publicKey`
- estado del intento

Nunca devuelve `FINTOC_SECRET_KEY` ni `link_token`.

### 2. Abrir Fintoc Widget

El frontend carga el Widget por script externo, sin agregar dependencia nueva:

```html
<script src="https://js.fintoc.com/v1/"></script>
```

Luego crea el Widget con:

- `publicKey`: `NEXT_PUBLIC_FINTOC_PUBLIC_KEY`
- `widgetToken`: recibido desde el backend
- `onSuccess`: recibe el Link Intent completado
- `onExit`: marca el flujo como cancelado por el usuario
- `onEvent`: solo para telemetria/UI, no como fuente de verdad

El frontend no debe asumir que la conexion quedo persistida solo por el
callback. La fuente de verdad es el backend despues de intercambiar el
`exchange_token`.

### 3. Recibir `exchange_token`

Cuando el Widget termina correctamente, Fintoc entrega un Link Intent con
`exchange_token`.

El frontend debe enviar ese token inmediatamente al backend:

```http
POST /api/bank-connections/fintoc/exchange
Content-Type: application/json

{
  "exchangeToken": "li_xxx_exchange_token_xxx"
}
```

Reglas:

- `exchange_token` es temporal.
- No se guarda en frontend.
- No se guarda en `localStorage`, `sessionStorage`, query params ni logs.
- Se usa una vez para que el backend obtenga el Link real.

### 4. Intercambiar `exchange_token` por Link

El backend llama a Fintoc:

```http
GET https://api.fintoc.com/v1/links/exchange?exchange_token=...
Authorization: FINTOC_SECRET_KEY
```

Fintoc devuelve el Link, incluido `link_token`.

El backend debe:

- crear o actualizar `BankConnection` con `provider = FINTOC`.
- guardar el identificador externo del Link en `providerConnectionId`.
- guardar nombre de institucion cuando venga disponible.
- guardar estado inicial `ACTIVE` o equivalente.
- cifrar `link_token` antes de persistirlo.
- nunca exponer `link_token` por DTO.

### 5. Obtener cuentas

Con el `link_token` descifrado en backend, se listan cuentas:

```http
GET https://api.fintoc.com/v1/accounts?link_token=LINK_TOKEN
Authorization: FINTOC_SECRET_KEY
```

Cada cuenta de Fintoc se normaliza a `BankAccount`:

- `externalId`: `account.id`
- `name`: `account.name` u `official_name`
- `accountType`: `account.type`
- `currency`: `account.currency`
- `balance`: preferir `balance.current` si existe
- relacion con `BankConnection`
- relacion con `User`

Campos recomendados para una migracion futura:

- `officialName`
- `accountNumberLast4` o numero enmascarado, nunca numero completo si no es
  necesario para el producto
- `lastRefreshedAt`
- `refreshStatus`
- `removedFromLink`

### 6. Obtener movimientos

Por cada cuenta se listan movimientos:

```http
GET https://api.fintoc.com/v1/accounts/{account_id}/movements?link_token=LINK_TOKEN&per_page=300&page=1
Authorization: FINTOC_SECRET_KEY
```

Parametros utiles:

- `since`: fecha ISO para sincronizacion incremental.
- `until`: fecha ISO opcional.
- `per_page`: usar hasta 300.
- `page`: iterar hasta agotar resultados.
- `confirmed_only`: mantener `true` por defecto para evitar movimientos
  transitorios en MVP.

Normalizacion a `Transaction`:

- `externalId`: `movement.id`
- `source`: `FINTOC`
- `bankAccount`: cuenta asociada
- `user`: usuario propietario
- `currency`: `movement.currency`
- `description`: `movement.description`
- `transactionDate`: preferir `post_date`; usar `transaction_date` solo como
  metadata futura si se agrega campo.
- `amount`: valor absoluto del monto convertido a unidad principal cuando
  corresponda.
- `type`: `INCOME` si Fintoc amount > 0, `EXPENSE` si amount < 0.
- `notes`: no guardar payload completo; si se requiere auditoria, usar tabla
  controlada y sin secretos.

AppSueldo guarda montos positivos. Fintoc entrega montos con signo, donde
positivo significa entrada de dinero y negativo salida. La conversion debe ser
explicita y testeada.

Deduplicacion:

- usar `externalId = movement.id`.
- mantener indice unico parcial por `(bank_account_id, source, external_id)`
  para `source = FINTOC`.
- los reintentos de sync deben ser idempotentes.

## Actualizacion de movimientos

### Webhooks

Fintoc usa webhooks para notificar eventos relevantes de cuentas y Links. El
backend debe exponer:

```http
POST /api/webhooks/fintoc
```

Este endpoint debe estar fuera del flujo de auth de usuario, pero protegido por
validacion de firma/secreto de Fintoc cuando este disponible.

Eventos relevantes para Movements:

- `account.refresh_intent.succeeded`
- `account.refresh_intent.failed`
- `account.refresh_intent.rejected`

Comportamiento:

- `succeeded`: sincronizar solo la cuenta indicada por
  `data.refreshed_object_id`.
- `failed`: registrar estado de error recuperable y mensaje seguro.
- `rejected`: marcar conexion o cuenta como requiere reconexion.

Cada evento debe procesarse de forma idempotente. Se recomienda una tabla futura
de eventos procesados con:

- `event_id`
- `event_type`
- `provider`
- `received_at`
- `processed_at`
- `status`

### Refresh Intents

Para sincronizacion manual, el backend crea un Refresh Intent:

```http
POST https://api.fintoc.com/v1/refresh_intents?link_token=LINK_TOKEN&refresh_type=only_last
Authorization: FINTOC_SECRET_KEY
Content-Type: application/json
```

Endpoint AppSueldo propuesto:

```http
POST /api/bank-connections/{id}/sync
```

Reglas:

- validar que la conexion pertenece al usuario autenticado.
- descifrar `link_token` solo en backend.
- no hacer polling agresivo.
- esperar webhook de resultado cuando corresponda.
- si Fintoc responde `requires_mfa.widget_token`, devolver al frontend solo el
  `widgetToken` necesario para abrir el Widget de MFA.
- respetar limites de Fintoc, incluido el intervalo minimo entre refresh
  intents cuando aplique.

## Endpoints backend propuestos

### Crear Link Intent

```http
POST /api/bank-connections/fintoc/link-intents
```

Autenticacion: usuario autenticado por cookies HttpOnly.

Respuesta:

```json
{
  "provider": "FINTOC",
  "publicKey": "pk_test_xxx",
  "widgetToken": "li_xxx_sec_xxx",
  "country": "cl",
  "product": "movements"
}
```

### Intercambiar Exchange Token

```http
POST /api/bank-connections/fintoc/exchange
```

Request:

```json
{
  "exchangeToken": "li_xxx_exchange_token_xxx"
}
```

Respuesta:

```json
{
  "id": 1,
  "provider": "FINTOC",
  "institutionName": "Banco ejemplo",
  "status": "ACTIVE",
  "accounts": []
}
```

La respuesta nunca incluye `link_token`, tokens cifrados ni referencias
sensibles.

### Sincronizar manualmente

```http
POST /api/bank-connections/{id}/sync
```

Respuesta sin MFA:

```json
{
  "status": "PENDING",
  "requiresMfa": false
}
```

Respuesta con MFA:

```json
{
  "status": "REQUIRES_MFA",
  "requiresMfa": true,
  "widgetToken": "ri_xxx_sec_xxx"
}
```

### Listar conexiones

```http
GET /api/bank-connections
```

Debe mantener el contrato actual, agregando solo campos seguros si son
necesarios:

- estado de sync.
- fecha de ultima sincronizacion.
- si requiere reconexion.
- cuentas asociadas.

### Recibir Webhook

```http
POST /api/webhooks/fintoc
```

Debe:

- validar firma/secreto.
- registrar evento recibido.
- procesar de forma idempotente.
- responder rapido con `2xx` si el evento fue aceptado.
- no depender de una sesion de usuario.

## Cambios backend minimos futuros

No se implementan en esta etapa, pero el plan de implementacion debe considerar:

- `BankProviderClient` como interfaz.
- `FintocClient` como cliente HTTP concreto.
- `FintocConnectionService` para Link Intent y exchange.
- `FintocSyncService` para cuentas y movimientos.
- `FintocWebhookService` para eventos.
- `FintocTokenCrypto` para cifrar y descifrar `link_token`.
- DTOs especificos que no expongan secretos.
- tests unitarios para normalizacion y seguridad.

## Cambios frontend minimos futuros

- Agregar seccion "Conectar banco" en `settings` o `bank-connections`.
- Cargar `https://js.fintoc.com/v1/` solo en la pantalla de conexion.
- Usar `NEXT_PUBLIC_FINTOC_PUBLIC_KEY`.
- Llamar al backend para crear Link Intent.
- Abrir Widget con `widgetToken`.
- En `onSuccess`, enviar `exchange_token` al backend.
- Mostrar estados:
  - conectando.
  - conexion completada.
  - sincronizando.
  - requiere MFA.
  - requiere reconexion.
  - error recuperable.
- No usar `localStorage` para tokens.
- No poner tokens en URL.
- No llamar directamente a la API de Fintoc desde frontend.

## Variables de entorno

### Backend

```env
FINTOC_SECRET_KEY=sk_test_xxx
FINTOC_WEBHOOK_SECRET=whsec_xxx
FINTOC_BASE_URL=https://api.fintoc.com
FINTOC_TOKEN_ENCRYPTION_KEY=base64-encoded-32-byte-key
FINTOC_ENV=test
APP_PUBLIC_BASE_URL=https://app.example.com
```

Notas:

- `FINTOC_SECRET_KEY` puede ser `sk_test_` o `sk_live_`, segun ambiente.
- `FINTOC_TOKEN_ENCRYPTION_KEY` debe vivir fuera del repo y debe permitir cifrar
  `link_token` antes de persistirlo.
- `FINTOC_WEBHOOK_SECRET` se usa para validar webhooks si Fintoc entrega firma o
  secreto de endpoint.
- `APP_PUBLIC_BASE_URL` permite construir o registrar URLs publicas de webhook.

### Frontend

```env
NEXT_PUBLIC_FINTOC_PUBLIC_KEY=pk_test_xxx
```

Notas:

- La Public Key puede estar en frontend.
- Ninguna Secret Key puede estar en frontend.
- `link_token` no puede estar en frontend.
- tokens cifrados o referencias internas tampoco pueden estar en frontend.

## Modelo de seguridad

### Datos que nunca van al frontend

- `FINTOC_SECRET_KEY`.
- `link_token`.
- `FINTOC_WEBHOOK_SECRET`.
- `FINTOC_TOKEN_ENCRYPTION_KEY`.
- token cifrado en DB.
- headers `Authorization` enviados a Fintoc.
- payloads completos con datos sensibles si no son necesarios para la UI.

### Datos temporales permitidos en frontend

- `NEXT_PUBLIC_FINTOC_PUBLIC_KEY`.
- `widgetToken` de Link Intent o Refresh Intent MFA.
- `exchange_token` solo en memoria y solo para enviarlo inmediatamente al
  backend.

### Persistencia segura

- `link_token` se guarda cifrado en backend.
- La DB nunca debe guardar credenciales bancarias del usuario.
- La app no debe solicitar credenciales bancarias directamente; eso lo maneja
  el Widget de Fintoc.
- Los logs deben omitir o enmascarar cualquier token, exchange token, secret key
  o header sensible.
- Los DTOs de conexiones bancarias no deben incluir `accessTokenRef`,
  `link_token`, tokens cifrados ni secretos.

### Ownership

- Cada `BankConnection` pertenece a un `User`.
- Cada `BankAccount` pertenece a un `User` y una `BankConnection`.
- Cada `Transaction` pertenece a un `User` y una `BankAccount`.
- Todo endpoint autenticado debe validar ownership por usuario.
- Webhooks no tienen usuario autenticado; deben resolver ownership desde
  identificadores externos guardados en backend.

## Roadmap por etapas

### Etapa 1: documentacion

- Crear este documento.
- No modificar codigo, entidades, migraciones ni dependencias.

### Etapa 2: foundation backend

- Crear interfaz `BankProviderClient`.
- Crear `FintocClient` con configuracion por env vars.
- Crear DTOs internos para Link, Account, Movement y Refresh Intent.
- Crear servicio de cifrado para `link_token`.
- Agregar tests unitarios de configuracion, normalizacion y seguridad.

### Etapa 3: conexion bancaria

- Implementar endpoint de Link Intent.
- Implementar endpoint de exchange.
- Persistir `BankConnection` Fintoc.
- Persistir `BankAccount` Fintoc.
- Ejecutar primer sync de movimientos despues del exchange.
- Mantener login, registro y endpoints existentes intactos.

### Etapa 4: UI minima

- Agregar boton "Conectar banco".
- Cargar Widget por script externo.
- Enviar `exchange_token` al backend.
- Mostrar conexion y cuentas conectadas.
- No redisenar la app.

### Etapa 5: webhooks y refresh manual

- Crear endpoint de webhooks.
- Validar firma/secreto.
- Registrar eventos procesados para idempotencia.
- Implementar sync manual por conexion.
- Manejar MFA de Refresh Intent con Widget.

### Etapa 6: hardening

- Reintentos controlados.
- Backoff y manejo de rate limits.
- Monitoreo de errores de sync.
- Reconexiones por credenciales rechazadas.
- Clasificacion automatica posterior.
- Mejoras de dashboard sobre transacciones sincronizadas.

## Test plan

### Backend unitario

- `FintocClient` arma requests con `Authorization` solo en backend.
- `FintocTokenCrypto` cifra y descifra `link_token`.
- Normalizacion de Movement:
  - monto Fintoc positivo -> `INCOME` con `amount` positivo.
  - monto Fintoc negativo -> `EXPENSE` con `amount` positivo.
  - currency se conserva.
  - `externalId = movement.id`.
- Deduplicacion por `bank_account_id`, `source = FINTOC`, `external_id`.
- DTOs no exponen secretos.

### Backend service/controller

- Crear Link Intent requiere usuario autenticado.
- Exchange token crea conexion y cuentas del usuario autenticado.
- Sync manual rechaza conexiones de otro usuario.
- Webhook `succeeded` sincroniza solo la cuenta indicada.
- Webhook `failed` deja estado de error seguro.
- Webhook `rejected` marca reconexion requerida.
- Procesar el mismo webhook dos veces no duplica transacciones.

### Frontend

- La vista de conexion carga el script externo solo donde corresponde.
- El Widget usa `publicKey` y `widgetToken`.
- `exchange_token` se envia al backend sin persistirse.
- No hay `localStorage` para tokens.
- No hay tokens en URL.
- Estados de carga, exito, error, MFA y reconexion son visibles.

### Validacion local

```bash
cd backend && set -a && source .env && set +a && mvn test
cd frontend && npm run lint
cd frontend && npm run build
git diff --check
```

## Fuentes oficiales consultadas

- Fintoc documentation index: https://docs.fintoc.com/llms.txt
- Integration Flow: https://docs.fintoc.com/docs/integration-with-exchange-token
- Widget: https://docs.fintoc.com/docs/widget
- Web Integration: https://docs.fintoc.com/docs/web-integration
- Exchange: https://docs.fintoc.com/reference/exchange
- List Accounts: https://docs.fintoc.com/reference/accounts-list
- Account Object: https://docs.fintoc.com/reference/accounts-object
- List Movements: https://docs.fintoc.com/reference/movements-list
- Movement Object: https://docs.fintoc.com/reference/movements-object
- Refresh Intents: https://docs.fintoc.com/docs/refresh-intents-walkthrough
- Create Refresh Intent: https://docs.fintoc.com/docs/refresh-intents-creating
- Refresh Intent errors: https://docs.fintoc.com/docs/refresh-intents-errors
- Webhooks: https://docs.fintoc.com/docs/webhooks-walkthrough
- Refresh Intent webhook results:
  https://docs.fintoc.com/docs/refresh-intents-webhook

## Nota sobre MCP

El plan se basa en documentacion oficial de Fintoc. Si el MCP `fintoc-docs`
esta disponible en la sesion de Codex, debe usarse para refrescar esta
informacion antes de implementar etapas posteriores. Si el MCP remoto responde
con un challenge HTML o no completa handshake, usar `llms.txt` y las paginas
`.md` oficiales como fuente primaria alternativa.
