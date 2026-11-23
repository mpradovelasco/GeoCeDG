# PRE-G9B-MAINT-P0 — C. Plan correctivo de las limitaciones de GeoCeDG 1.2.2

| Campo | Valor |
|---|---|
| Carácter | **Plan candidato.** No modifica `locus-v2-metrics.md`, el contrato de uso condicionado, ADR 0028/0038/0042 ni ninguna política; no autoriza implementación. `implementationAuthorized=false`; `selfApproved=false` |
| Estado | `PLAN CANDIDATE — PENDING AUTHOR REVIEW` (revisión 2) |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-P0», bloque B, y enmiendas autorales finales del expediente, punto 2 (ambas del 2026-11-23) |
| Base | `f80f0e9b029e678007f399508163bf794951992c` (GeoCeDG 1.2.2) |
| Evidencia | [informe de caracterización](../validation/pre_g9b_maint_p0_characterization_report.md), §B y §F |

## Registro de revisiones

| Revisión | Fecha | Contenido |
|---|---|---|
| 1 | 2026-11-23 | Versión inicial. Para B1 recomendaba no estimar el subtotal (`MAINT-B1a`) con enmienda de §28.2 |
| 2 | 2026-11-23 | Enmiendas autorales, punto 2: B5a distingue discontinuidad no detectada, incumplimiento de H3 y ausencia de certificación, y exige presupuesto acotado, pruebas adversariales y no regresión de admisibilidad (§1); B1 pasa a comparar tres opciones con la decisión normativa registrada como pendiente, sin aprobar la desaparición del subtotal (§3) |

## 0. Resumen y prioridad

| Prioridad | Limitación | Impacto práctico | Intervención mínima propuesta | Norma afectada | Coste |
|---|---|---|---|---|---|
| **1** | **B5** — salto alineado con la cuerda | **Corrección:** la sonda P-1 confirma que una ruta que cruza el salto se admite como número `ESTIMATED` que incluye la longitud del salto | `MAINT-B5a`: guarda de escalado de cuerdas en la admisión de rutas condicionadas (§28.3), comprobación numérica conservadora con presupuesto acotado | enmienda de métricas §28.3 | S |
| 2 | **B2** — huérfano tras redefinir | Recalcula en silencio una autoridad inalcanzable en cada cambio (hasta 4,6–6,5 s si era un total) y queda guardada | `MAINT-B2`: barrido tras completar la redefinición, en el mismo paso de Undo; opcionalmente al cargar | enmienda de métricas §29 | S |
| 3 | **B1** — longitud total condicionada | 4,6–6,5 s por cambio mientras exista, para un subtotal que no es escalar público | **Decisión normativa pendiente** entre tres opciones (§3.2): no calcular el subtotal, calcularlo solo a petición expresa o mantener la estimación actual con optimizaciones justificadas. Recomendación técnica: cálculo a petición (`MAINT-B1a`, opción O2) | §28.2 solo con O1 u O2 | S |
| 4 | **B3** — `Intersect` condicionado | +0,55–0,75 s por cambio; sin trabajo repetido dentro de la política | Diferir los cambios de política; `MAINT-PERF-O2a` opcional (coste por evaluación, sin cambio normativo) | ninguna para O2-a | M |
| 5 | **B4** — piezas de deslizadores | Ningún caso constructivo demostrado | Cerrar como límite declarado y fijar con una prueba la reordenación por redefinición de la lista | ninguna | XS |
| — | D-V6 | 0,84–0,99 s con el documento guardado; el autor ya no exige < 1 s | Ninguna; O2-a lo mejoraría | — | — |

Ninguna intervención reabre Locus V2, cambia la serialización, la identidad o el DAG, ni amplía
garantías de existencia, continuidad o completitud. B5a y B2 exigen una enmienda acotada de
`locus-v2-metrics.md` §28.3 y §29; B1 solo la exige si el autor elige O1 u O2 (hoja F, D-B-1a,
D-B-1b y D-B-2). La parte de B1 de la enmienda queda pendiente de esa decisión y no bloquea las
otras dos.

## 1. B5 — Discontinuidades alineadas con la cuerda

### 1.1 Reproducibilidad y causa

- Recorrido: `findingOf` solo usa la desviación perpendicular del punto medio y el giro entre
  semicuerdas; un salto dentro de unos `0,35 rad` de la cuerda no deja rastro
  (`LocusConditionedTraversal2D.java:413-439`). Fijado por
  `PostR1ConditionedEvidenceTest.aJumpAlongTheChordLeavesNoTraceAndIsNotDetected`.
- Métrica: el estimador, en modo de solo evaluador, acepta la hoja cuando todos los niveles de
  cuerdas coinciden; sobre nodos alineados la suma de cuerdas incluye el salto.
- **Sonda P-1:** recta con salto `(1, 2)` en `u = 0,75`: escalar admitido `2,4597` (= √5 · 1,1)
  en lugar de ninguno; arco exponencial con salto tangente de longitud `0,117`: admitido
  `0,3513` ≈ arco `0,2341` + salto.

### 1.2 Impacto

Un número admitido por `LocusLength(L, A, B)` en una región condicionada puede ser falso sin
ningún aviso distinto del de toda ruta condicionada (`H1–H4` asumidas). Por inferencia, el
mismo punto ciego afecta al seguimiento de puntos y al descubrimiento de raíces de
`Intersect` a través del salto.

Tres situaciones que no deben confundirse:

| Situación | Qué significa | Qué hace hoy 1.2.2 | Qué haría `MAINT-B5a` |
|---|---|---|---|
| Ausencia de certificación de continuidad | Estado normal de toda región condicionada: la continuidad no está probada, pero nada indica que falle. Es la razón de declarar `H1–H4` | admite rutas `ESTIMATED` bajo hipótesis declaradas | nada cambia |
| Incumplimiento de la hipótesis H3 | La curva no es continua en el tramo de la ruta: la hipótesis declarada es falsa y la garantía condicionada queda vacía, aunque el número se publique «bajo hipótesis» | si el salto deja rastro, el recorrido lo detecta; si no, admite un número falso (P-1) | si la prueba de escalado observa la firma del salto, niega el número con diagnóstico propio |
| Discontinuidad no detectada | Un incumplimiento de H3 que ningún criterio de nodos ve: salto en la dirección de la cuerda, por debajo de la resolución o entre muestras | admite un número falso | reduce el conjunto de casos no detectados, pero **no lo vacía**: la guarda es detección numérica conservadora, no prueba universal |

La guarda transforma algunos incumplimientos de H3 en negativas diagnosticadas. No certifica
la continuidad de las rutas que siguen admitidas: esas rutas permanecen en la primera fila.

### 1.3 Tres niveles que no deben confundirse

| Nivel | Qué puede afirmar | Mecanismo |
|---|---|---|
| Detección numérica | «se observó una firma de discontinuidad»; su ausencia no prueba continuidad | prueba de escalado de cuerdas (§1.4) |
| Evidencia bajo hipótesis | «continuo si la velocidad está acotada por `L` en el intervalo», con `L` declarada como hipótesis | cota de Lipschitz declarada, nunca estimada desde dos nodos («a slope observed between two nodes … is not a Lipschitz bound», contrato §4) |
| Certificación rigurosa | continuidad probada | envolventes de intervalo de `F'` del modelo certificado (ADR 0038, clases v1–v3); por definición no existen en las regiones condicionadas. Promoción futura K1/K2 (contrato §11) |

No es posible afirmar detección universal con muestreo finito: un defecto en un conjunto de
parámetros no muestreado es invisible para cualquier criterio de nodos.

### 1.4 Intervención mínima: `MAINT-B5a`

Fundamento: sea `c(h)` la longitud de la cuerda de un subintervalo de anchura `h` que contiene
el punto `u*`.

- Si `F` es `C^1` con velocidad no nula cerca de `u*`, `c(h) ≈ h·‖F'(u*)‖` y
  `c(h/2)/c(h) → 1/2`.
- Si `‖F'(u)‖ ~ |u − u*|^k` (punto estacionario de orden `k`), `c(h/2)/c(h) → 2^{-(k+1)} ≤ 1/2`.
- Si la velocidad diverge como `|u − u*|^{-α}` con `0 < α < 1` (extremo de tipo pliegue,
  longitud finita), `c(h/2)/c(h) → 2^{α-1}`, menor que 1.
- Si hay un salto de tamaño `J > 0`, `c(h) → J` y `c(h/2)/c(h) → 1`.

Regla propuesta: en la admisión de una ruta (§28.3), toda hoja aceptada se somete a la prueba
de escalado. Se bisecan los subintervalos de cuerda máxima hasta el paso mínimo de la
política. Si el cociente `c(h/2)/c(h)` se mantiene por encima de `ρ` en `m` niveles
consecutivos con `c > ε_res`, la ruta deja de ser admisible: escalar indefinido, causa
`CONSISTENCY_NOT_ESTABLISHED` y diagnóstico nuevo «posible discontinuidad en la dirección de
la cuerda». Valores iniciales para el diseño: `ρ = 0,85`, `m = 3`; `ε_res` es la resolución de
la política.

- No cambia el recorrido ni la identidad de contenido de la evidencia; solo la admisión del
  escalar. Los certificados, P2 y H10 no se tocan.
- Falsos positivos: velocidad que diverge con `α > 0,77` aproximadamente (cociente
  `2^{α−1} > 0,85`). Es un caso casi singular, y negar el número allí es conservador.
- Es detección numérica declarada, no certificación (§1.2 y §1.3).

Presupuesto de trabajo acotado:

- la guarda solo evalúa hojas ya aceptadas por el estimador y como mucho `m` niveles
  adicionales de bisección por hoja, sin bajar del paso mínimo de la política;
- un tope total de evaluaciones adicionales por ruta, fijado en la enmienda (N1) y medido
  frente al coste actual de la ruta en los modelos del autor;
- si el tope se agota antes de completar la prueba, la ruta no se admite como número, con una
  causa distinta de la de salto («comprobación de continuidad no completada»). Es la opción
  conservadora; la medición de §1.5 debe mostrar que no ocurre en las rutas admitidas hoy;
- el resultado es determinista para una revisión y una política fijas.

`MAINT-B5b` (diferido): versión `conditioned-traversal/v2` con la misma prueba en el recorrido
para crear `DISCONTINUITY_BAND`. Cambia la identidad de contenido de toda la evidencia, añade
trabajo (límite 1 024) y exige decisión normativa propia.

### 1.5 Riesgo, pruebas y cierre

- Riesgo de regresión: bajo para certificados y P2; medio para rutas condicionadas (pueden
  dejar de admitirse rutas casi singulares). Ninguna curva continua relevante debe perder su
  admisibilidad sin una firma de salto que lo justifique.
- Pruebas discriminantes:
  - P-1 convertida en prueba: recta y arco, sin escalar;
  - el tramo antes del salto sigue siendo número;
  - el extremo de tipo pliegue sigue sin número por la causa actual;
  - `aRouteAcrossAJumpThatTheTraversalDoesNotSeeIsNoNumber` sin cambios;
  - la prueba del recorrido `aJumpAlongTheChordLeavesNoTraceAndIsNotDetected` sigue fijada (el recorrido no cambia en B5a).
- Batería adversarial (debe negar el número o, si no puede, quedar fijada como límite
  declarado con su prueba):
  - saltos con ángulos respecto de la cuerda de 0 a `0,35 rad` y tamaños desde la resolución
    hasta órdenes de magnitud mayores;
  - saltos en posiciones diádicas y no diádicas respecto de la malla, junto a un extremo de la
    ruta y varios saltos en una misma ruta;
  - salto de ida y vuelta dentro de una celda (la cuerda no ve desplazamiento neto);
  - discontinuidad evitable (un solo valor distinto): invisible para cualquier criterio de
    nodos; se declara y se fija;
  - oscilación rápida junto al salto.
- No regresión de admisibilidad (deben seguir siendo número):
  - curvas suaves con parametrización muy no uniforme (`u ↦ u³`, `u ↦ u⁸`);
  - puntos estacionarios de orden 1 y 2;
  - lazos cerrados estrechos y curvatura alta;
  - velocidad divergente con `α` por debajo del umbral;
  - todas las rutas condicionadas admitidas hoy en los modelos del autor (`w_2`, `a_3`,
    `trHole`) y en las baterías post-R1, medidas antes y después, con el recuento de
    evaluaciones adicionales de la guarda.
- Resuelto cuando ninguna ruta que cruce un salto de las baterías se admita, las rutas
  admitidas hoy sigan admitidas y el trabajo adicional quede dentro del tope. Diferido solo si
  el autor acepta el número erróneo como límite declarado; en ese caso debe fijarse con una
  prueba.

B5a se mantiene como prioridad científica del programa: es la única limitación de 1.2.2 que
produce un número erróneo admitido.

## 2. B2 — Autoridad rica huérfana tras redefinir el número

### 2.1 Reproducibilidad y causa

`PostR1PrivateMetricAuthorityTest.aRedefinedNumberKeepsTheRecordedLimitation`.
`AlgoLocusMetricScalarAdapter.remove()` omite la retirada durante una sustitución, porque
retirarla dentro de la transacción choca con la guarda de identidad. No hay barrido posterior
ni al cargar.

### 2.2 Impacto

Coste oculto por cambio (el de la métrica huérfana: hasta 4,6–6,5 s si era un total
condicionado), objeto oculto e inalcanzable que se guarda con el documento, y acumulación si se
redefine varias veces. Los documentos con huérfanos de borrados anteriores a `781629cb1`
también los conservan.

### 2.3 Intervención mínima: `MAINT-B2`

- **B2-F1 (núcleo):**
  - cuando el adaptador se retira por sustitución, registrar su autoridad privada como candidata en una cola de la operación de redefinición;
  - al completar la operación (`completeSpatialRedefineOperation`; en lotes, tras el conmutador final), fuera del arrendamiento de publicación y antes de almacenar el punto de Undo, retirar las candidatas que sigan sin lector;
  - si la redefinición se revierte, descartar la cola.
- **B2-F2 (opcional, decisión D-B-2):** barrido al cargar un documento, que retira toda autoridad
  privada (`isPrivateScalarAuthority`) sin lectores y deja un diagnóstico. Limpia los huérfanos
  heredados, pero cambia el contenido de esos documentos al guardarlos de nuevo.
- No se propone tratar la redefinición como borrar y crear, porque rompería la continuidad de
  identidad del número.

Relación con la sustitución de puntos (bloque C): comparten el borde de la transacción, pero
no sus invariantes. B2 retira un objeto privado cuyo único lector desapareció; la sustitución
decide el destino de dependientes públicos. Se implementan por separado y B2 primero.

### 2.4 Riesgo, pruebas y cierre

- Riesgo: el borde de la transacción, las redefiniciones en lote y su reversión, y el paso
  de Undo. Protecciones que deben seguir valiendo: un resultado rico con nombre del usuario
  (`w_1`, `n_2` en modelos anteriores a 1.2.0) nunca se retira, y una autoridad con un segundo
  lector tampoco (`PostR1LengthDeletionDesktopTest.java:65-79, 154-177`).
- Pruebas:
  - invertir la prueba fijada;
  - redefinir hacia otra `LocusLength`: queda exactamente una autoridad;
  - redefinición en lote;
  - una redefinición fallida y revertida conserva el par;
  - Undo y Redo exactos;
  - guardar y reabrir sin huérfano;
  - (F2) un documento con huérfano, escrito por un candidato anterior, queda limpio al cargar y no cambia nada más;
  - Desktop: tras `cuT = 5` sobre el modelo del autor, las evaluaciones de `w_2` vuelven a la base.
- Medición previa (S0): coste aislado de la redefinición en el arnés, que hoy no consta.
- Resuelto cuando la prueba invertida pasa con las pruebas de transacción sin cambios.

## 3. B1 — Longitud total condicionada

### 3.1 Dos objetivos separados

**(a) Trabajo innecesario.** Dentro del contrato vigente no hay trabajo repetido: el recuento
de ≈ 5 700 evaluaciones lo fija §28.2, que exige el valor del subtotal en el resultado rico, y
el estimador refina hasta su presupuesto junto a los extremos de región. La reutilización entre
revisiones está prohibida (§28.2; contrato §7.6) y no se propone.

**(b) Cuándo admitir un escalar.** Solo sin ampliar garantías:

- **T-A:** una sola región que cubre el dominio pedido, sin bandas ni celdas sin explorar ni
  causas excluidas. Es numéricamente la ruta ya admitida entre sus extremos; solo cambia la
  forma pública. No aplica a `w_2` (dos regiones y cuatro bandas).
- **T-B:** cada banda omitida con contribución o cota tipada. Exige envolventes certificadas
  que las regiones condicionadas no tienen; no es viable en este programa.
- **T-C:** un escalar con nombre distinto de «subtotal». Exige cambiar la superficie pública
  y un usuario podría leerlo como total; no se recomienda.

### 3.2 Tres opciones (decisión normativa pendiente, D-B-1b)

La prioridad es eliminar el coste automático innecesario sin perder información científica
potencialmente útil. No se propone aprobar de antemano la desaparición del subtotal del
resultado rico.

| | O1 — no calcular el subtotal | O2 — calcularlo solo a petición expresa | O3 — mantener la estimación con optimizaciones justificadas |
|---|---|---|---|
| Qué hace | El resultado rico declara regiones y bandas y remite a `LocusLength(L, A, B)` por región; nunca estima el subtotal | Igual que O1 en cada cambio; el subtotal se estima cuando el usuario o un consumidor lo pide expresamente, para la revisión vigente | Como hoy: el subtotal se estima en cada revisión |
| Coste por cambio ordinario | ≈ base | ≈ base | ≈ 4,4–6,4 s (O2-a ahorra solo 0,10–0,17 s; el recuento lo fija §28.2) |
| Coste de la petición | — | el actual (4,6–6,5 s) una vez por petición | — |
| Información científica | el subtotal desaparece; queda la suma manual por regiones, que tampoco cubre las bandas | conservada bajo demanda | conservada siempre |
| Norma | enmienda §28.2 («the rich result carries the value») | enmienda §28.2: el resultado rico lleva el valor cuando se ha pedido para esa revisión | ninguna para O2-a; cualquier reducción del recuento cambia el valor y exige enmienda |
| Estado tras un cambio | — | el valor pedido queda marcado como de otra revisión y no se recalcula solo; nunca se muestra como vigente | — |
| Persistencia | ninguna | la petición es transitoria (acción del inspector), no se guarda y no cambia el XML; un comando explícito para guiones queda como opción posterior | ninguna |
| Riesgo | pierde un dato que algún uso podría necesitar | una acción de interfaz más y la regla de vigencia del valor | conserva el coste que motivó B1 |
| Escalar público | indefinido (sin cambio) | indefinido (sin cambio): el subtotal nunca es número | indefinido (sin cambio) |

**Recomendación técnica: O2**, porque elimina el coste automático y conserva el dato. La
decisión normativa queda **pendiente** del autor. Antes de decidir conviene medir (S0 de B1)
el estado y el valor del resultado rico actual de `LocusLength(w_2)`, que no constan en los
registros, y si algún consumidor lo lee.

Independiente de la opción: admitir T-A como número (enmienda de §28.3) es opcional y de bajo
coste.

### 3.2.1 Intervención según la opción: `MAINT-B1a`

- O1 u O2: cuando el total solo puede ser subtotal (hay una banda no ausente omitida), no
  construir el estimador durante la revisión. Publicar el resultado rico con un estado
  explícito («subtotal no calculado» o «subtotal disponible a petición»), la lista de regiones
  y de bandas y la remisión a `LocusLength(L, A, B)` por región. Elimina las ≈ 5 700
  evaluaciones por cambio. En O2, la acción del inspector construye el estimador para la
  revisión vigente y publica el valor con esa revisión.
- O3: `MAINT-B1a` no existe; la mejora posible es `MAINT-PERF-O2a`.
- Riesgo (O1 y O2): el inspector y el modo valor de la vista algebraica dejan de mostrar el
  subtotal sin petición; cambian las pruebas que lo afirman y el contenido rico mostrado en
  documentos de 1.2.0–1.2.2 con totales. El escalar público no cambia: ya era indefinido.
- Pruebas:
  - arnés `intersect -Configuration total`: el coste con total pasa a ser ≈ la base, y los compendios de resultados son idénticos salvo el campo cambiado;
  - en las pruebas compartidas, una rama con bandas construye 0 estados de región sin petición, y una región sin bandas no cambia;
  - (O2) la petición produce exactamente el valor actual para la misma revisión; tras un cambio, el valor se declara de otra revisión y no se recalcula solo;
  - totales H10 y P2 sin cambios;
  - adaptar `PostR1LengthDeletionDesktopTest`.

### 3.3 Alternativa sin cambio normativo

`MAINT-PERF-O2a` (huella de valores más barata por evaluación) reduce el coste de cada
evaluación (estimado −0,10 a −0,17 s por cambio en el modelo del autor), no el recuento. No
resuelve B1 por sí sola.

## 4. B3 — Coste de `Intersect` condicionado

- Causa: la política de aislamiento (rejilla uniforme de 256 subdivisiones por región y
  búsqueda ternaria de mínimos) multiplicada por el coste de evaluar `w_2` (≈ 1,2 ms).
- No queda trabajo repetido dentro de la política vigente.
- Todas las palancas de recuento cambian la política de aislamiento:
  - sección áurea: la mitad del refinado; cambian los últimos bits de las raíces;
  - semillas desde la evidencia: hasta −514 de 776 evaluaciones; malla más gruesa, que podría encontrar otras raíces;
  - omitir mínimos donde hay cambio de signo: incompatible con AGENTS §11.5 (tangencias).
- **Recomendación:** no cambiar la política en este programa; aceptar el coste como límite
  cuantificado y, si el autor quiere mejora, autorizar `MAINT-PERF-O2a`, que beneficia a B1, B3
  y D-V6 sin cambio normativo.
- Si el autor fija un objetivo, la primera opción es la sección áurea, con:
  - un banco adverso: dos raíces cercanas en un tramo, tangencia junto a un cruce, raíz en el borde de una banda, raíz que pasa entre regiones (C17) y costura periódica;
  - aceptación expresa del cambio de los últimos bits.
- Resuelto cuando se cumpla el objetivo autoral con raíces y tokens idénticos en el banco; en
  otro caso, diferido como límite cuantificado.

## 5. B4 — Identidad de piezas gobernadas por deslizadores

- La semántica de ranura D2 es la aprobada; no es un defecto por ser posicional.
- No hay caso constructivo demostrado: el único defecto demostrado (piezas literales) está
  corregido, y ningún modelo de `models/` usa listas de dominio con deslizadores.
- Dar identidad duradera a esos numéricos cambiaría su ciclo de vida de redefinición y la
  serialización. Usar etiquetas contradice AGENTS principio 10.
- **Recomendación:** cerrar como límite declarado y añadir una prueba que fije la reordenación
  de dos piezas de deslizadores al redefinir la lista (hoy razonada en existencia §20.6, sin
  prueba). Reabrir solo con un modelo constructivo que lo necesite.

## 6. Resumen de condiciones de cierre

| Id | Resuelto cuando | Diferido cuando |
|---|---|---|
| B5a | ninguna ruta a través de un salto de la batería es número; las rutas admitidas hoy siguen admitidas; trabajo adicional dentro del tope | el autor acepta el riesgo, y entonces se fija con prueba |
| B2 | prueba fijada invertida; transacciones, Undo y reapertura sin cambios | el autor mantiene §29 |
| B1 | decisión D-B-1b tomada; con O1 u O2, coste del total no calculable ≈ base, con hechos publicados idénticos salvo el campo declarado (y, en O2, el valor a petición idéntico al actual) | el autor elige O3: límite cuantificado, con O2-a opcional |
| B3 | objetivo autoral cumplido sin cambio de raíces ni tokens | sin objetivo autoral: límite cuantificado |
| B4 | prueba de reordenación añadida; límite declarado | — |
