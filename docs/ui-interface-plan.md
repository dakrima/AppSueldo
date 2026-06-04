# AppSueldo UI Interface Plan

## Alcance

Este documento es un diagnostico y plan de mejora de interfaz para AppSueldo. No implementa cambios de UI, no toca backend, no cambia logica de negocio y no propone un redisenio visual completo.

El objetivo es ordenar, limpiar y jerarquizar la interfaz actual manteniendo la identidad visual existente: fondo calido, cards claras, bordes suaves, sombras discretas, botones actuales, iconos Lucide, paleta principal y tono cercano.

## Resumen del estado actual

La interfaz actual ya tiene una base visual consistente:

- Usa Next.js App Router con rutas principales en `frontend/src/app`.
- Las pantallas protegidas comparten `AppShell`, `Navbar`, `Sidebar` y `MobileNav`.
- La estetica es coherente: canvas calido, cards `soft-card`, bordes `border-soft`, radios amplios, sombras suaves y tono visual tranquilo.
- Auth ya usa API real y debe mantenerse intacto; las pantallas financieras aun trabajan con mocks centralizados.
- Hay componentes base reutilizables (`Button`, `Input`, `Badge`, `EmptyState`), pero todavia falta una capa mas clara para headers, estados, filtros, errores y acciones de formulario.

Problemas generales detectados:

- Algunas pantallas operativas usan titulos y espacios muy grandes, lo que reduce densidad util sin mejorar decision.
- Hay controles que parecen funcionales pero no lo son todavia, como filtros, botones de opciones y algunas acciones de settings.
- Algunos textos hablan de implementacion tecnica futura, por ejemplo `POST /api/transactions`, y eso no deberia aparecer en UI final.
- Transferencias, gastos e ingresos no estan suficientemente separados visualmente; una transferencia puede verse como gasto negativo.
- Faltan convenciones consistentes para loading, error, empty, success, disabled y estado "preparado para futura integracion".
- En mobile, el encabezado global, la navegacion y los titulos pueden ocupar demasiado espacio antes de llegar a la tarea principal.

## Pantallas revisadas

- `/`: landing / inicio publico.
- `/login`: inicio de sesion.
- `/register`: registro.
- `/dashboard`: resumen financiero.
- `/transactions`: listado de movimientos.
- `/transactions/new`: creacion manual de movimiento.
- `/categories`: categorias.
- `/settings`: configuracion.

Componentes principales revisados:

- Layout: `AppShell`, `Navbar`, `Sidebar`, `MobileNav`.
- Auth: `AuthProvider`, `ProtectedRoute`, `PublicOnlyRoute`, `SessionLoading`, `LogoutButton`.
- Dashboard: `MainBalanceCard`, `SummaryCard`, `CategoryBreakdown`, `InsightCard`.
- Movimientos: `TransactionList`, `TransactionItem`, `PeriodSummaryCard`.
- Categorias: `CategoryCard`, `CreateCategoryCard`.
- UI base: `Button`, `Input`, `Badge`, `EmptyState`.

## Problemas y mejoras por pantalla

| Pantalla / area | Problema detectado | Principio asociado | Propuesta concreta | Archivos probables | Prioridad | Riesgo |
| --- | --- | --- | --- | --- | --- | --- |
| Layout global | Titulos muy grandes para pantallas operativas y mucho espacio inicial. En mobile la combinacion de navbar, nav y header puede empujar la tarea principal hacia abajo. | Jerarquia visual, estetica minimalista, contexto de uso. | Definir variantes de `PageHeader`: compacto para flujos operativos y amplio solo donde aporte. Mantener estilos actuales, ajustando escala y espaciado. | `AppShell`, `Navbar`, `MobileNav`, paginas protegidas. | Alta | Medio |
| Navegacion desktop/mobile | Mobile usa abreviaturas como `Mov.` y `Cat.`. Las acciones primarias aparecen en sidebar y headers sin criterio uniforme. | Reconocer antes que recordar, consistencia y estandares. | Usar nombres claros cuando el espacio lo permita. Mantener accion primaria visible por contexto y evitar duplicacion innecesaria. | `Sidebar`, `MobileNav`, paginas. | Alta | Bajo |
| Landing `/` | El CTA `Ver ejemplo` apunta a una ruta protegida; puede parecer demo publica, pero exige sesion. | Prevencion de errores, visibilidad del estado del sistema. | Cambiar copy a algo que indique que requiere sesion o definir despues una demo publica real. | `frontend/src/app/page.tsx`. | Media | Bajo |
| Login | Error general aparece bajo ambos campos. Mensajes pueden venir con tono tecnico. | Formularios, ayuda para reconocer y recuperarse de errores. | Mantener flujo auth, pero usar mensajes simples, especificos y, cuando sea posible, cerca del campo correspondiente. | `login/page.tsx`, `Input`, futuro `ErrorMessage`. | Alta | Bajo |
| Registro | Ayuda de contrasena depende del placeholder y el error aparece despues de enviar. Hay un texto sin tilde por ASCII en codigo: `contrasena`. | Prevencion de errores, labels y ayuda cercana. | Mostrar regla de contrasena como ayuda persistente debajo del campo. Mantener validacion actual sin tocar auth. | `register/page.tsx`, `Input`, futuro `ErrorMessage`. | Alta | Bajo |
| Dashboard | Varias cards compiten con el saldo principal. El insight puede aparecer sin titulo visible. | Jerarquia visual, pocos puntos focales, visibilidad del estado. | Priorizar saldo, ingresos, gastos y balance. Usar insight con titulo siempre visible y copy orientado a decision. | `dashboard/page.tsx`, `MainBalanceCard`, `SummaryCard`, `InsightCard`. | Alta | Medio |
| Dashboard | Transferencias y gastos pueden mezclarse en resumenes si no se explicita su tratamiento. | Similitud con mundo real, prevencion de errores financieros. | Separar transferencias como categoria neutral: no sumar como ingreso/gasto real salvo que haya regla explicita. | `features/dashboard/data.ts`, `SummaryCard`, `PeriodSummaryCard`. | Alta | Medio |
| Movimientos | Filtros y busqueda parecen activos, pero hoy no filtran. | Visibilidad del estado del sistema, control y libertad. | Si siguen mock/no funcionales, mostrarlos como disabled o con etiqueta "proximamente". Si se mantienen activos, deben filtrar. | `transactions/page.tsx`, futuro `FilterBar`. | Alta | Medio |
| Movimientos | La lista es legible, pero no muestra claramente cuenta, fuente, clasificacion o estado sin saturar. | Listas/tablas, reconocimiento, minimalismo. | Mantener formato feed/card para MVP y mobile; agregar metadatos secundarios compactos: fecha, categoria, cuenta/fuente si aporta, estado sin clasificar. | `TransactionList`, `TransactionItem`, futuro `TransactionRow`. | Media | Medio |
| Movimientos | Los montos estan visualmente correctos, pero transferencias se pueden percibir como gasto negativo. | Semantica financiera, mundo real. | Mostrar transferencias con tono neutral, icono propio y texto como "Transferencia", sin signo negativo de gasto. | `TransactionItem`, mocks de presentacion. | Alta | Medio |
| Nuevo movimiento | Fecha es estatica y parece seleccionable. El formulario no muestra validaciones cercanas al campo. | Formularios, visibilidad, prevencion de errores. | Convertir fecha en input real cuando se implemente o marcarla como valor editable claro. Agregar errores por campo y ayuda breve. | `transactions/new/page.tsx`, `Input`, futuro `FormActions`. | Alta | Medio |
| Nuevo movimiento | Success dice `Movimiento listo para conectar con POST /api/transactions`, texto tecnico para usuario final. | Contenido, similitud con mundo real. | Cambiar a mensaje de usuario: "Movimiento guardado" o "Listo, se agrego al historial" cuando se implemente. | `transactions/new/page.tsx`, futuro `SuccessState`. | Alta | Bajo |
| Categorias | `Crear categoria` y `Crear nueva` parecen acciones finales, pero no hay flujo visible. | Control, visibilidad del estado. | Si aun no crean, dejarlas disabled con microcopy o activar flujo simple en etapa futura. Evitar botones aparentemente funcionales sin resultado. | `categories/page.tsx`, `CategoryCard`. | Media | Bajo |
| Categorias | Montos y porcentajes no indican periodo o base de calculo. | Reconocer antes que recordar, mundo real. | Agregar contexto compacto: "Junio" o "Este mes", sin llenar la card de texto. | `CategoryCard`, `features/categories/data.ts`. | Media | Bajo |
| Settings | Datos hardcodeados y controles desplegables parecen funcionales. | Visibilidad del estado, consistencia. | Diferenciar ajustes activos de opciones futuras. Usar disabled o badge "preparado" cuando no haya accion real. | `settings/page.tsx`, `Badge`, futuro `SectionHeader`. | Media | Medio |
| Settings | Texto de privacidad es correcto, pero largo y con mucho peso visual. | Minimalismo, contenido escaneable. | Mantener mensaje de seguridad, resumirlo en bullets cortos y dejar detalle secundario. | `settings/page.tsx`. | Baja | Bajo |
| Mobile | Formularios y headers pueden ocupar demasiado alto; los filtros apilados hacen la pantalla pesada. | Mobile/responsive, flexibilidad. | En mobile, mostrar primero tarea principal, reducir texto de apoyo, convertir filtros en barra compacta o drawer futuro. | `MobileNav`, `AppShell`, paginas financieras. | Alta | Medio |

## Flujos principales de usuario

### Revisar dashboard financiero

Objetivo del usuario: entender rapidamente como va su mes, cuanto ingreso, cuanto gasto y que decision deberia tomar.

Fricciones posibles:

- El saldo principal compite con varias cards secundarias.
- Las transferencias pueden contaminar la lectura de gasto/ingreso.
- El insight no siempre tiene titulo, lo que reduce escaneo.
- No queda claro si los datos son manuales, reales o mock/preparados.

Mejoras:

- Mantener un solo foco principal: saldo disponible o balance del mes.
- Mostrar ingresos y gastos como lecturas principales, y transferencias como dato neutral.
- Hacer visible el periodo revisado.
- Usar un estado "MVP manual" o "Datos manuales" discreto cuando aplique.
- Mantener el CTA "Agregar movimiento" arriba, porque es una accion frecuente en MVP.

### Revisar movimientos

Objetivo del usuario: encontrar un movimiento, entender si fue ingreso/gasto/transferencia, ver categoria y detectar pendientes de clasificar.

Fricciones posibles:

- Busqueda y filtros parecen funcionales aunque aun no filtran.
- La lista prioriza descripcion y monto, pero puede ocultar estado de clasificacion o cuenta.
- Los montos de transferencia pueden leerse como gasto.
- En mobile, muchos filtros apilados ocupan espacio antes de la lista.

Mejoras:

- Si filtros no funcionan aun, mostrarlos disabled o como "preparado".
- Mantener columnas/metadatos esenciales: fecha, descripcion, monto, categoria y estado.
- Alinear montos a la derecha y usar signos/tonos consistentes.
- Mostrar transferencias sin color de gasto y sin signo negativo de gasto.
- En mobile, priorizar lista y dejar filtros secundarios colapsables o simplificados en etapa futura.

### Crear un nuevo movimiento manual

Objetivo del usuario: registrar rapido un ingreso, gasto o transferencia sin entender la estructura bancaria interna.

Fricciones posibles:

- La fecha parece un control, pero hoy esta estatica.
- No hay validacion visible cerca del campo de monto o descripcion.
- El mensaje de exito menciona `POST /api/transactions`, que es interno.
- La seleccion de categoria es visualmente agradable, pero puede crecer mal cuando haya muchas categorias.

Mejoras:

- Mostrar errores cerca de monto, descripcion, tipo y categoria.
- Usar mensajes de exito en lenguaje de usuario.
- Dejar transferencia como opcion clara, pero explicitar que no afecta gasto/ingreso real.
- Mantener pocos campos: tipo, monto, descripcion, categoria, fecha y nota opcional.
- Preparar un selector de categoria escalable despues, sin redisenio visual.

### Revisar categorias

Objetivo del usuario: entender como se agrupan sus gastos e ingresos y ajustar categorias cuando sea necesario.

Fricciones posibles:

- Las cards muestran monto y porcentaje sin periodo claro.
- Las acciones de crear parecen disponibles, pero no hay flujo de creacion.
- No se distingue si una categoria esta activa, sin uso, o pendiente de configuracion.

Mejoras:

- Agregar contexto temporal: "Este mes" o periodo actual.
- Mostrar empty state cuando no existan categorias.
- Si crear/editar aun no esta listo, usar estado disabled o preparado para futura integracion.
- Mantener cards, iconos y tonos actuales.

### Ajustar configuracion

Objetivo del usuario: revisar perfil, privacidad, preferencias y acciones sensibles.

Fricciones posibles:

- Perfil y correo aparecen hardcodeados.
- Preferencias parecen desplegables funcionales, pero no cambian nada.
- Privacidad tiene texto largo.
- Zona de peligro debe prevenir errores, no solo mostrar link.

Mejoras:

- Conectar datos reales de usuario en etapa posterior sin cambiar layout.
- Distinguir preferencias activas de opciones futuras.
- Resumir privacidad en puntos breves.
- Tratar acciones destructivas con confirmacion y texto especifico cuando se implementen.

## Estados de interfaz

Estos estados deben ser consistentes en todas las pantallas. La regla principal: ningun control debe parecer funcional si no produce una accion clara.

### Loading

- Usar `LoadingState` para carga de pagina/seccion, no textos sueltos.
- Mantener fondo y card actual.
- Indicar accion: "Cargando movimientos", "Validando sesion", "Preparando resumen".
- Evitar spinners sin texto en pantallas financieras.

### Error

- Usar `ErrorMessage` cerca del problema.
- En formularios, ubicar el error bajo el campo correspondiente cuando sea posible.
- En secciones, mostrar error dentro de la card afectada.
- Mensajes deben ser especificos y recuperables: "No pudimos cargar tus movimientos. Intenta nuevamente."
- No exponer mensajes tecnicos del backend si no ayudan al usuario.

### Empty state

- Usar `EmptyState` para listas sin datos.
- Debe explicar que falta y ofrecer una accion si corresponde.
- Ejemplos:
  - Sin movimientos: "Agrega tu primer movimiento manual."
  - Sin categorias: "Crea categorias para ordenar tus movimientos."
  - Sin conexiones bancarias: "Por ahora estas usando una cuenta manual."

### Success

- Debe confirmar en lenguaje de usuario.
- Evitar mensajes tecnicos como endpoints, DTOs o APIs.
- Ejemplos:
  - "Movimiento guardado."
  - "Categoria asignada."
  - "Preferencia actualizada."

### Disabled

- Usar disabled real para botones, filtros o selects que aun no esten disponibles.
- El estado disabled debe tener contraste suficiente y tooltip/texto corto si la razon no es obvia.
- No usar disabled para esconder errores; si el usuario necesita completar algo, indicar que falta.

### Preparado para futura integracion

Este estado aplica a filtros, conexion bancaria, importacion, bancos, dashboard real y acciones futuras.

Recomendacion:

- Usar un badge discreto: "Preparado", "Proximamente" o "MVP manual".
- No usar botones primarios para acciones futuras.
- No mostrar inputs interactivos si no se puede completar el flujo.
- Si una seccion existe para orientar al usuario, debe decir claramente que aun no conecta bancos.

## Accesibilidad basica

Checklist minimo para futuras etapas:

- Contraste: texto principal, secundario, badges y botones deben mantener contraste suficiente sobre `warm-canvas`, `soft-card`, `mint-bg`, `amber-bg` y `soft-coral-bg`.
- Foco visible: botones, links, inputs, filtros y nav deben tener `focus-visible` claro y consistente.
- Tamano de texto: evitar textos menores a `text-sm` para informacion importante; `text-xs` solo para etiquetas secundarias.
- Labels: todos los inputs deben tener label visible o `aria-label` si el diseno exige ocultarlo.
- Errores cercanos: cada error de formulario debe aparecer cerca del campo afectado y no solo como alerta global.
- Mobile touch: controles tactiles deben mantener altura aproximada de 44px o mas.
- Iconos: iconos interactivos deben tener `aria-label`; iconos decorativos no deben reemplazar informacion textual esencial.
- Navegacion: el estado activo debe ser visible por color y forma, no solo por color.
- Listas: montos/numeros alineados a la derecha en desktop; en mobile deben seguir siendo faciles de comparar.
- No depender solo del color para distinguir ingreso, gasto, transferencia o error.

## Semantica financiera

La interfaz debe reforzar el contrato financiero del producto:

- `INCOME`: ingreso real. Puede usar tono positivo/verde y signo `+`.
- `EXPENSE`: gasto real. Puede usar tono coral/rojo suave y signo `-`.
- `TRANSFER`: movimiento entre cuentas propias o movimiento neutral. No deberia contarse como gasto/ingreso real sin regla explicita.
- Cuenta: origen operativo del movimiento; no debe confundirse con categoria.
- Categoria: clasificacion semantica del gasto/ingreso.
- Movimiento sin clasificar: debe tener estado visible, por ejemplo "Sin categoria" o "Pendiente".

Propuesta para transferencias:

- Usar tono neutral o azul suave, no tono de gasto.
- Mostrar etiqueta "Transferencia".
- Evitar `-$80.000` si visualmente se lee como gasto. Alternativas:
  - `$80.000` con badge "Transferencia".
  - `Movido: $80.000`.
  - Icono de flechas/cuenta y texto "Entre cuentas".
- Excluir transferencias de cards de ingresos/gastos reales.
- Si se muestran en resumen, ubicarlas en card separada o nota: "Transferencias no incluidas en gasto real".

Propuesta para movimientos sin clasificar:

- Mostrar "Sin categoria" como badge neutral.
- Dar una accion secundaria clara: "Asignar categoria" cuando exista flujo.
- No ocultarlos del dashboard; deben ser visibles como pendiente.

## Componentes reutilizables propuestos

Estos componentes no implican cambiar identidad visual. Sirven para ordenar patrones y evitar soluciones distintas por pantalla.

| Componente | Responsabilidad | Uso esperado | Archivos probables |
| --- | --- | --- | --- |
| `PageHeader` | Titulo, descripcion, accion primaria y variante compacta/amplia. | Reemplazar header manual dentro de `AppShell` sin cambiar visual principal. | `components/layout/PageHeader.tsx`, `AppShell`. |
| `SectionHeader` | Titulo, descripcion breve y accion secundaria de una card/seccion. | Dashboard, movimientos, categorias, settings. | `components/ui/SectionHeader.tsx`. |
| `EmptyState` | Estado sin datos con icono, texto y accion opcional. | Reusar el actual, ampliandolo con accion opcional. | `components/ui/EmptyState.tsx`. |
| `LoadingState` | Carga de pagina o seccion con copy claro. | Auth, dashboard, transacciones, categorias. | `components/ui/LoadingState.tsx`, `SessionLoading`. |
| `ErrorMessage` | Error de campo o seccion con tono consistente. | Login, registro, nuevo movimiento, carga de datos. | `components/ui/ErrorMessage.tsx`, `Input`. |
| `StatCard` | Numero principal, label, helper, icono y tono. | Unificar `SummaryCard` y futuras cards financieras. | `components/ui/StatCard.tsx`, `SummaryCard`. |
| `TransactionItem` / `TransactionRow` | Movimiento compacto con fecha, descripcion, categoria, cuenta, tipo y monto. | Feed mobile y row desktop si se decide tabla ligera. | `components/transactions`. |
| `FormActions` | Acciones primarias/secundarias de formularios. | Nuevo movimiento, login/register si aplica, settings. | `components/ui/FormActions.tsx`. |
| `FilterBar` | Busqueda, filtros, disabled/preparado y resumen de filtros activos. | Movimientos y futuras categorias/dashboard. | `components/ui/FilterBar.tsx` o `components/transactions/FilterBar.tsx`. |

## Que no se debe cambiar

Para mantener la identidad visual de AppSueldo:

- No cambiar la paleta principal definida en `globals.css`.
- No reemplazar el fondo calido `warm-canvas`.
- No cambiar la estetica de cards claras con borde suave y sombra ligera.
- No reemplazar los botones actuales por otra libreria.
- No introducir gradientes, animaciones decorativas, carruseles o elementos ornamentales.
- No cambiar Lucide como familia de iconos.
- No convertir el producto en landing page de marketing; las pantallas protegidas deben seguir siendo operativas.
- No cambiar auth, cookies, `credentials: "include"`, ni el flujo de sesion.
- No usar `localStorage` ni tokens en URL.
- No tocar backend ni logica de negocio para estas mejoras de UI.

## Plan de implementacion por etapas

### Etapa 1: orden visual y consistencia basica

Objetivo: reducir friccion inmediata sin cambiar comportamiento.

Cambios:

- Crear `PageHeader`/`SectionHeader` o ajustar `AppShell` con variantes.
- Reducir jerarquia sobredimensionada en pantallas operativas.
- Unificar acciones primarias y secundarias.
- Reemplazar copy tecnico visible por lenguaje de usuario.
- Marcar acciones futuras como disabled/preparado si aun no funcionan.
- Ajustar transferencia para que no parezca gasto negativo en UI mock.

Archivos probables:

- `frontend/src/components/layout/AppShell.tsx`
- `frontend/src/components/layout/Navbar.tsx`
- `frontend/src/components/layout/MobileNav.tsx`
- `frontend/src/app/dashboard/page.tsx`
- `frontend/src/app/transactions/page.tsx`
- `frontend/src/app/transactions/new/page.tsx`
- `frontend/src/components/transactions/TransactionItem.tsx`
- `frontend/src/lib/mocks/presentation.ts`

### Etapa 2: navegacion y estructura de paginas

Objetivo: que el usuario siempre entienda donde esta y que puede hacer.

Cambios:

- Revisar labels mobile y desktop.
- Definir accion principal por pantalla.
- Ordenar headers, secciones y aside por frecuencia de uso.
- Evitar duplicacion de CTAs cuando no aporte.

Archivos probables:

- `Sidebar`, `MobileNav`, `AppShell`, paginas protegidas.

### Etapa 3: formularios, errores y estados de carga

Objetivo: mejorar prevencion y recuperacion de errores.

Cambios:

- Crear `ErrorMessage`, `LoadingState` y `FormActions`.
- Agregar mensajes cercanos al campo en login/register y nuevo movimiento.
- Separar success de mensajes tecnicos.
- Definir disabled state claro para acciones incompletas.

Archivos probables:

- `login/page.tsx`, `register/page.tsx`, `transactions/new/page.tsx`, `Input`, `Button`, nuevos componentes UI.

### Etapa 4: responsive/mobile

Objetivo: que mobile muestre primero la tarea esencial.

Cambios:

- Compactar headers en mobile.
- Revisar altura de nav y filtros.
- Evitar scroll horizontal.
- Mantener controles tactiles comodos.
- Decidir si filtros se muestran como barra compacta o seccion secundaria.

Archivos probables:

- `AppShell`, `MobileNav`, `transactions/page.tsx`, `transactions/new/page.tsx`, `categories/page.tsx`.

### Etapa 5: refinamiento final

Objetivo: coherencia fina antes de conectar datos reales.

Cambios:

- Revisar microcopy completo.
- Revisar estados empty/error/success en todas las pantallas.
- Alinear montos y etiquetas.
- Confirmar contraste, foco y accesibilidad basica.
- Preparar checklist visual antes de etapa de APIs reales.

## Criterio de implementacion futura

Recomendacion: implementar primero la Etapa 1.

Razon:

- Es la etapa de menor riesgo funcional.
- No requiere tocar backend ni auth.
- Ordena la base visual antes de agregar carga real, errores reales o filtros funcionales.
- Corrige fricciones visibles del MVP: jerarquia, copy tecnico, acciones falsas y semantica de transferencias.

Archivos que tocaria primero:

- `frontend/src/components/layout/AppShell.tsx`
- `frontend/src/components/layout/Navbar.tsx`
- `frontend/src/components/layout/MobileNav.tsx`
- `frontend/src/app/dashboard/page.tsx`
- `frontend/src/app/transactions/page.tsx`
- `frontend/src/app/transactions/new/page.tsx`
- `frontend/src/components/transactions/TransactionItem.tsx`
- `frontend/src/components/dashboard/SummaryCard.tsx`
- `frontend/src/lib/mocks/presentation.ts`

Criterios para aceptar Etapa 1:

- Las pantallas mantienen la identidad visual actual.
- Los titulos y secciones se sienten mas ordenados y menos pesados.
- Ningun boton o filtro parece funcional si aun no lo es.
- El copy no expone endpoints ni detalles internos.
- Las transferencias no se ven como gasto real.
- Login/register siguen usando el flujo actual de auth sin cambios.
- `npm run lint` y `npm run build` pasan desde `frontend`.

## Checklist de aceptacion general

- La tarea principal de cada pantalla se entiende en menos de 5 segundos.
- Cada pantalla tiene un unico foco visual principal.
- Las acciones primarias tienen texto claro y orientado a accion.
- Las acciones futuras o no implementadas no parecen funcionales.
- Loading, error, empty, success y disabled se muestran con patrones consistentes.
- Los errores de formularios son especificos y cercanos al campo.
- Las listas priorizan fecha, descripcion, monto, categoria y estado.
- Montos y numeros son comparables visualmente.
- Ingresos, gastos, transferencias y movimientos sin clasificar se distinguen por texto, icono y tono, no solo por color.
- La experiencia mobile no tiene scroll horizontal y mantiene controles tactiles comodos.
- No se cambiaron colores principales ni personalidad visual.
- No se cambio auth, backend ni logica de negocio.
