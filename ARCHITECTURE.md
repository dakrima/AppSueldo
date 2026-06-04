# AppSueldo Architecture

## Objetivo

AppSueldo transforma movimientos bancarios desordenados en informacion clara para tomar mejores decisiones financieras. El MVP sigue siendo manual, pero el backend queda preparado para integrar conexiones bancarias reales sin reescribir el modelo principal.

## Dominios principales

- `auth`: registro, login, refresh, logout, cookies `HttpOnly`, JWT de acceso y refresh token hasheado.
- `users`: identidad del usuario y ownership de datos financieros.
- `bank connections`: conexiones logicas a proveedores bancarios. Hoy existe una conexion `MANUAL`; Fintoc se agregara despues.
- `bank accounts`: cuentas asociadas a una conexion. Las transacciones siempre deben poder asociarse a una cuenta.
- `transactions`: movimientos normalizados del usuario, con monto positivo, moneda, tipo y fuente.
- `categories`: categorias definidas por usuario para agrupar ingresos y gastos.
- `classification`: clasificacion manual o por reglas simples de transacciones.
- `dashboard`: lecturas agregadas sobre transacciones normalizadas.

## Modelo conceptual

- `User`: dueno de conexiones, cuentas, categorias, transacciones y clasificaciones.
- `BankConnection`: representa una conexion bancaria por usuario. Campos sensibles futuros, como referencias a tokens, deben guardarse cifrados o como referencia a un vault. No se guardan credenciales bancarias reales.
- `BankAccount`: representa una cuenta bancaria o una cuenta manual. Pertenece a un usuario y a una conexion.
- `Transaction`: pertenece a un usuario y a una cuenta. Tambien puede tener una categoria.
- `Category`: pertenece a un usuario.
- `TransactionClassification`: registra como se clasifico una transaccion y puede apuntar a una categoria.

## Flujo futuro esperado

1. El usuario se registra o ingresa.
2. El usuario tendra conexiones bancarias.
3. Cada conexion tendra cuentas bancarias.
4. Cada cuenta tendra transacciones.
5. Las transacciones se normalizaran al contrato interno de AppSueldo.
6. Las transacciones podran clasificarse manualmente o con reglas.
7. El dashboard leera transacciones normalizadas, no respuestas crudas del proveedor.

## Integracion bancaria futura

Fintoc no se implementa en esta etapa. La integracion debe entrar despues detras de una interfaz de aplicacion, por ejemplo `BankProviderClient`, con una implementacion `FintocClient`. La logica de negocio no debe depender directamente del cliente HTTP de Fintoc.

El modelo ya reserva:

- `BankProvider.FINTOC`
- identificadores externos de conexion, cuenta y transaccion
- `TransactionSource.FINTOC`
- indice unico parcial para evitar duplicados de transacciones Fintoc con `(bank_account_id, source, external_id)`

No se deben loguear tokens bancarios ni exponerlos en DTOs.

## Estrategia de montos y tipos

El contrato interno usa `amount` positivo y `type` para la semantica:

- `INCOME`: ingreso real.
- `EXPENSE`: gasto real.
- `TRANSFER`: movimiento entre cuentas propias o movimientos que no deberian contarse como gasto/ingreso real sin logica adicional.

No se debe interpretar un monto negativo como gasto. Las transferencias internas son una deuda de dominio importante: cuando existan varias cuentas reales, deberan agruparse con `transferGroupId` o una estrategia equivalente para no duplicar gasto/ingreso en el dashboard.

## Clasificacion

La clasificacion parte simple:

- `MANUAL`: el usuario asigno la categoria.
- `RULE_KEYWORD`: regla por palabras clave en descripcion.
- `RULE_AMOUNT`: regla por monto.
- `UNCLASSIFIED`: pendiente de clasificacion.

No hay IA en esta etapa. Si se agrega IA despues, debe quedar detras del servicio de clasificacion y producir sugerencias auditables.

## Frontend

El frontend Next.js se organiza para separar rutas, features, componentes compartidos, tipos, cliente API y mocks:

- `src/app`: rutas App Router y composicion de pantallas.
- `src/features/auth`: llamadas reales de autenticacion. Auth ya usa backend, cookies `HttpOnly` y `credentials: "include"`.
- `src/features/transactions`, `src/features/categories`, `src/features/dashboard`, `src/features/bank-connections`: API clients y data providers por dominio.
- `src/lib/api`: cliente HTTP comun. No usa `localStorage`, no arma tokens manualmente y siempre envia cookies con `credentials: "include"`.
- `src/lib/mocks`: datos mock centralizados.
- `src/types`: tipos de dominio financiero y tipos de presentacion.
- `src/components/ui` y `src/components/layout`: piezas visuales compartidas.

En esta etapa el frontend queda preparado pero no conectado a las APIs reales de transacciones, categorias, dashboard ni conexiones bancarias. Las pantallas protegidas consumen data providers de `features/*/data`; esos providers devuelven mocks centralizados. La etapa posterior cambiara esos providers para llamar a `features/*/api`, sin tener que reestructurar las pantallas.

Los mocks se dividen en dos capas:

- `src/lib/mocks/finance`: objetos alineados con los DTOs esperados del backend (`Transaction`, `Category`, `BankConnection`, `BankAccount`, `MonthlySummary`).
- `src/lib/mocks/presentation`: modelos de presentacion con iconos, tonos, strings formateados y datos de UI derivados de los mocks de dominio.

## Migraciones y persistencia

El backend usa Flyway y `spring.jpa.hibernate.ddl-auto=validate`. Los cambios de esquema deben entrar como migraciones versionadas.

La migracion `V3__banking_model_foundation.sql` crea:

- `bank_connections`
- `bank_accounts`
- `transaction_classifications`

Tambien agrega a `transactions`:

- `bank_account_id`
- `currency`
- `external_id`
- `transfer_group_id`

Para compatibilidad, V3 crea una conexion manual y una cuenta manual por usuario existente, y asocia transacciones existentes a esa cuenta.

## Validacion local

Desde la raiz del proyecto:

```bash
docker compose up -d
```

Backend:

```bash
cd backend
set -a
source .env
set +a
mvn test
```

Para levantar la API:

```bash
cd backend
set -a
source .env
set +a
mvn spring-boot:run
```

Frontend, fuera del alcance de esta etapa:

```bash
cd frontend
npm run lint
npm run build
npm run dev
```
