# PRE-G9B-MAINT — E. Roadmap candidato del mantenimiento previo a G9B

| Campo | Valor |
|---|---|
| Carácter | **Plan candidato de programa.** No es el roadmap normativo, no abre ninguna fase y no autoriza ningún incremento. `implementationAuthorized=false`; `selfApproved=false` |
| Estado | `PROGRAM PLAN CANDIDATE — PENDING AUTHOR REVIEW` (revisión 2) |
| Identificador propuesto | `PRE-G9B-MAINT`, con incrementos `PRE-G9B-MAINT-<id>`; familia nueva sin colisión con `PRE-G9B-S/D/P/R/CU` ni con `G9B` |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-P0» y enmiendas autorales finales del expediente (ambas del 2026-11-23) |
| Base | `f80f0e9b029e678007f399508163bf794951992c` (GeoCeDG 1.2.2) |
| Expediente | [portada](../validation/pre_g9b_maint_p0_dossier.md) · [hoja de decisiones](../validation/pre_g9b_maint_p0_author_decision_sheet.md) · [prompts candidatos](pre_g9b_maint_p0_candidate_prompts/README.md) |

## Registro de revisiones

| Revisión | Fecha | Contenido |
|---|---|---|
| 1 | 2026-11-23 | Versión inicial: bloque A como `A1` → `A2` → `A3`; NURBS e importación DXF diferidas; B1 como `MAINT-B1a` sin subtotal |
| 2 | 2026-11-23 | Enmiendas autorales: bloque A reordenado en `A-N1`, `A-DXF0`, `A2`, `A-DXF1`, `A3` y `A-R` (diseñada, no autorizada), sin secuencia artificial entre importación, familia y exportación; B1 con decisión normativa pendiente entre tres opciones; S0 amplía la caracterización previa a S1 y S2; I2G0 como barrera basada en el riesgo con caracterización previa; F8 condicionado a una prueba discriminante |

## 0. Principios del programa

1. **Separado de G9B.** Ningún incremento abre, prepara ni autoriza G9B, que sigue
   `DESIGNED — NOT AUTHORIZED`.
2. **Sin dependencias artificiales entre bloques ni dentro de ellos.** A (B-splines, NURBS e
   interoperabilidad), B (limitaciones de 1.2.2) y C (CU7) avanzan por separado. Las únicas
   dependencias son técnicas y están en §3.
3. **1.2.2 es la base funcional que se preserva.** Cada incremento declara su efecto sobre
   documentos existentes; ninguno cambia en silencio la geometría de un documento guardado.
4. **Un incremento, una autorización.** Cada uno necesita autorización expresa del autor
   sobre una base exacta, con su `VERIFICATION_CLASS` congelada al empezar.
5. **Diseño antes que código.** Las decisiones normativas se toman y registran antes del
   incremento de código que las usa.
6. **Capas separadas.** Geometría autoritativa CeDG, definición importada de referencia,
   aproximación matemática y render no se confunden ni se sustituyen en silencio
   (propuesta B §5.4).
7. **Sin publicación implícita.** Ningún incremento promueve a `main`, crea etiquetas o
   releases ni toca el libro. Una versión que agrupe incrementos aprobados es una decisión
   autoral separada.
8. **Verificación proporcional.** `CORE + FOCUSED` (`INTEGRATION` + `PHASE` registrada) por
   defecto; `FINAL` nunca por categoría (§5).

## 1. Clasificación del trabajo

| Clase | Contenido |
|---|---|
| Ya implementado y reutilizable | aparato genérico de Locus V2 para una familia polinómica por tramos (longitud, intersecciones, pares ricos, puntos, semejanzas, dibujo, exportación, persistencia); fachada única de creación de puntos `LocusPointCreation2D`; sustitución legada con identidad nueva, impacto tipado y confirmación (ADR 0026); rechazo de ciclos; permiso individual de creación hacia delante; exportación DXF exacta de primitivas y manifiesto lateral; comandos `Point(L, u)` sobre componentes locales |
| Caracterizaciones previas (solo pruebas) | primer paso de `MAINT-I2G0`; `MAINT-S0`; `MAINT-F8-S0`; medición del resultado rico de B1 |
| Correcciones focales implementables tras decisión acotada | `MAINT-B5a`, `MAINT-B2`, `MAINT-I2G0`; `MAINT-B1a` solo si D-B-1b elige O1 u O2 |
| Decisiones normativas previas | enmiendas de métricas §28-§29 (N-1, con la parte de B1 pendiente); diseño normativo de la familia B-spline preparada para NURBS y contrato de importación de referencia (N-2, `MAINT-A-N1`); contrato breve de aproximación (al inicio de `MAINT-A-DXF1`); enmienda de sustitución con actualización de contrato de dependientes (N-3, previa a `MAINT-S2`) |
| Extensiones del núcleo | `MAINT-A2` (familia polinómica), `MAINT-F8` (permiso local), `MAINT-S1` y `MAINT-S2` (sustitución), `MAINT-PERF-O2a`; `MAINT-A-R` (racional nativa, diseñada y no autorizada) |
| Solo interfaz | `MAINT-U2` |
| Adaptadores y servicios fuera del núcleo | `MAINT-A3` (exportación DXF `SPLINE`); `MAINT-A-DXF0` (importación DXF de referencia); el ajuste de `MAINT-A-DXF1`, que produce una construcción del núcleo |
| Condicionados por precondiciones aún no satisfechas | `MAINT-F8-min` (requiere confirmación por F8-S0), `MAINT-S1` y `MAINT-S2` (requieren S0 y, S2, N-3), `MAINT-A3` (requiere `MAINT-A2` y lectores de conformidad), `MAINT-A-DXF1` (requiere `MAINT-A2` y `MAINT-A-DXF0`), `MAINT-B1a` (requiere D-B-1b), `MAINT-B5b` y los cambios de política de B3 (requieren decisión y objetivo autoral) |

## 2. Incrementos propuestos

Esfuerzo relativo: XS < S < M < L (comparativo, no en días).

| Id | Bloque | Contenido | Capa | Requiere antes | Esfuerzo | Clase de verificación propuesta | Alcance |
|---|---|---|---|---|---|---|---|
| `MAINT-P0` | — | Esta planificación | documental | — | — | `DOCUMENTATION_STATUS_ONLY` | autorizado (en curso) |
| `MAINT-N1` | B | Enmienda de `locus-v2-metrics.md` §28.3 y §29 para B5a y B2; §28.2 para B1 solo si D-B-1b elige O1 u O2 | normativa | D-B-1a, D-B-2 (D-B-1b para la parte de B1) | XS | `DOCUMENTATION_STATUS_ONLY` | propuesto |
| `MAINT-B5a` | B | Guarda de escalado de cuerdas en la admisión de rutas condicionadas, con presupuesto acotado | núcleo (métricas) | N1 | S | `INTEGRATED_PHASE` | **propuesto, prioridad científica 1** |
| `MAINT-U2` | C | Panel único de Punto, alias 145, asistencia de extremos, mensaje de rechazo, guías | Desktop | D-U2 | S | `INTEGRATED_PHASE` | **propuesto, prioridad 1 de interfaz** |
| `MAINT-I2G0` | C | Caracterización de la conducta Classic legítima y de los casos en riesgo; después, barrera basada en el riesgo antes de eliminar nada | núcleo (anfitrión) | D-I2G | S | caracterización: `BOUNDED_PHASE`; barrera: `INTEGRATED_PHASE` | propuesto |
| `MAINT-B2` | B | Barrido de la autoridad rica huérfana al completar la redefinición (y opcionalmente al cargar) | núcleo | N1, D-B-2 | S | `INTEGRATED_PHASE` | propuesto |
| `MAINT-B1a` | B | Total condicionado sin estimación automática del subtotal (O1) o con estimación solo a petición (O2); T-A opcional | núcleo (métricas) | D-B-1b ∈ {O1, O2}; N1 | S | `INTEGRATED_PHASE` | propuesto, condicionado a decisión pendiente |
| `MAINT-A-N1` | A | Diseño normativo: especificación `bspline-2d` preparada para NURBS (v1 polinómica) y su ADR; contrato de importación `dxf-spline-reference/v1`; biblioteca matemática; versión serializada | normativa | D-A-1..D-A-7 | S | `DOCUMENTATION_STATUS_ONLY` | propuesto |
| `MAINT-A-DXF0` | A | Importación DXF `SPLINE` 2D de solo lectura, polinómica y racional, como referencia | adaptador de importación + Desktop | contrato de importación de A-N1 aprobado | M | `INTEGRATED_PHASE` | propuesto, **prioridad de interoperabilidad** |
| `MAINT-A2` | A | Familia polinómica semántica como proveedor de `GeoLocusV2` (propuesta B §6.1) | núcleo + mínimo de producto | especificación de familia de A-N1 aprobada | L | `INTEGRATED_PHASE` | propuesto |
| `MAINT-F8` | C | F8-S0 (prueba discriminante sobre locus mixto real); F8-min solo si se confirma | núcleo + Desktop | U2 cerrado; D-F8 | S + M | S0: `BOUNDED_PHASE`; min: `INTEGRATED_PHASE` | propuesto, condicionado |
| `MAINT-S0` | C | Caracterización: referencias por nombre, fronteras de transacción, condiciones de restauración, fusión, hermanos tras fallo, veracidad del impacto, frecuencia en modelos | pruebas | — | S | `BOUNDED_PHASE` | propuesto |
| `MAINT-S1` | C | Sustitución explícita con impacto en cuatro clases, sobre de entrada sin identidad, informe veraz, atomicidad y reversión propia (transparencia y seguridad) | núcleo (anfitrión) + Desktop | S0 y su revisión autoral, B2 cerrado, I2G0 cerrado, D-M09-1..4 | M | `INTEGRATED_PHASE`; posible `VERIFICATION_ESCALATION_REQUEST` (§5) | propuesto, condicionado |
| `MAINT-A-DXF1` | A | Construcción explícita de una B-spline polinómica aproximante de una `SPLINE` importada; conversión exacta cuando la fuente es polinómica; medidas de error declaradas | servicio externo + núcleo (procedencia) | A2 y A-DXF0 cerrados; D-A-8..D-A-10; contrato breve aprobado | M | `INTEGRATED_PHASE` | propuesto, condicionado |
| `MAINT-A3` | A | Exportación DXF `SPLINE` polinómica con datos autoritativos y conformidad de lectores | adaptador de exportación | A2 aprobado; D-A-4 | M | `INTEGRATED_PHASE` + evidencia externa de lectores | propuesto, condicionado |
| `MAINT-N3` | C | Enmienda ADR 0026/0029/0041 y M09 §3: actualización de contrato de dependientes compatibles con el mapa `{P ↦ P'}` | normativa | S1 aprobado; D-M09-2 | S | `DOCUMENTATION_STATUS_ONLY` | propuesto, condicionado |
| `MAINT-S2` | C | Objetivo funcional principal: dependientes compatibles conservan identidad con dependencias actualizadas y evidencia revalidada (incluidos Locus V2) | núcleo | N3 aprobada | M | `INTEGRATED_PHASE`; posible escalado | propuesto, condicionado |
| `MAINT-PERF-O2a` | B | Huella de valores más barata por evaluación (B1, B3, D-V6) | núcleo | D-B-3 | M | `INTEGRATED_PHASE` | opcional |
| `MAINT-A-DXF1b` | A | Cota global certificada del error de aproximación (propuesta B §9.5), si no entra en A-DXF1 | servicio externo | A-DXF1; D-A-9 | S | `INTEGRATED_PHASE` | opcional |
| `MAINT-B5b` | B | Recorrido condicionado v2 con la prueba de escalado | núcleo (evidencia) | decisión propia | M | `INTEGRATED_PHASE` | diferido |
| Política de aislamiento B3 | B | Sección áurea o semillas desde la evidencia | núcleo | objetivo autoral | M | `INTEGRATED_PHASE` | diferido |
| `MAINT-A-R` | A | NURBS semántica nativa (propuesta B §10) | núcleo | A2 y decisión propia | L | por decidir | **diseñado, no autorizado** |
| `MAINT-A4`, `A5`, `A7` | A | Modelo certificado, `SPLINE` derivada de SplineV2, periódica/no anclada y afinidades exactas | — | decisiones propias | — | — | diferidos |

B4 no tiene incremento propio: la prueba que fija la reordenación de piezas de deslizadores se
añade en `MAINT-B5a` (solo prueba) y la limitación queda declarada.

## 3. Dependencias y secuencia

Dependencias reales:

- B: `N1` → `B5a`, `B2`; `D-B-1b` → (`N1` parte §28.2) → `B1a`.
- A: `A-N1` (familia) → `A2` → `A3`; `A-N1` (importación) → `A-DXF0`; `A2` + `A-DXF0` →
  `A-DXF1`; `A2` → `A-R` (no autorizada). `A-DXF0` y `A2` solo comparten la biblioteca
  matemática (propuesta B §5.3 y §11).
- C, sustitución: `S0` → `S1` → `N3` → `S2`; además `B2` → `S1` (mismo borde de la
  transacción de redefinición) e `I2G0` → `S1` (S1 usa su barrera).
- C, Punto: `U2` → `F8` (mismo panel y fachada).

```text
Ola 1   N1 (B5a, B2) ─ B5a        U2        I2G0 (caracterización → barrera)        A-N1
Ola 2   B2      B1a (si O1/O2)    F8-S0     S0          A-DXF0      A2
Ola 3   S1 (tras B2, I2G0, S0)    F8-min (si S0 confirma)           A-DXF1      A3
Ola 4   N3 → S2                   PERF-O2a (opcional)               A-DXF1b (opcional)
—       A-R: diseñada, no autorizada
```

Las olas ordenan por utilidad y riesgo, no son puertas: dentro de una ola los incrementos son
independientes. Pueden ejecutarse en paralelo solo en ramas separadas y sin compartir
archivos (en particular, `B2` y `S1` comparten `Construction`; `U2` y `F8`, el panel de Punto;
`A-DXF0` y `A2`, la biblioteca matemática, que crea el primero que se autorice).

## 4. Puertas

| Puerta | Condición |
|---|---|
| Entrada de cada incremento | autorización autoral expresa con base exacta; decisiones previas registradas; `VERIFICATION_CLASS` congelada |
| Entrada de S1 y S2 | además, informe de `MAINT-S0` revisado por el autor, con el nivel de restauración que cumple los requisitos de coherencia del diseño D §3.3 |
| Entrada de la barrera I2G0 | además, caracterización de la conducta Classic legítima y de los casos en riesgo |
| Entrada de F8-min | además, confirmación empírica por F8-S0 |
| Salida técnica | verificación de la clase congelada aceptada y completa sobre el candidato exacto; pruebas discriminantes del prompt; `git diff --check` |
| Salida autoral | revisión del candidato y, cuando haya interfaz, smoke manual del autor; registro de cierre. La aceptación técnica nunca implica aprobación |
| Parada | las del prompt candidato; en particular, cualquier cambio de serialización sin migración, una regresión en documentos de 1.2.2, una segunda autoridad geométrica o un DAG paralelo, o la necesidad de tocar I3, M08-D o reconstrucción general |
| Cierre del programa | cada incremento propuesto aprobado, diferido o cancelado por decisión autoral registrada; deudas diferidas listadas en el registro de cierre; sin efecto sobre G9B |

## 5. Verificación

- Ordinaria: `STATIC` + `PHASE <MAINT-id>` + `INTEGRATION`. Cada incremento con código
  registra su propia selección `PHASE` (inventario JUnit y registro), porque el perfil `DEV`
  no es ejecutable como evidencia.
- Caracterizaciones solo de pruebas: `STATIC` + `PHASE <MAINT-id>`.
- Documentales: `STATIC`; las correcciones documentales posteriores siguen la vía de
  descendiente documental (`tools/agent/documentary-descendant.ps1`).
- `FINAL`: no se propone para ningún incremento. Candidatos a una
  `VERIFICATION_ESCALATION_REQUEST`, que solo el autor puede autorizar:
  - `MAINT-S1` y `MAINT-S2`, porque tocan el camino de sustitución del anfitrión
    (`Construction.replace`) usado por toda redefinición. CORE cubre `RedefineTest`,
    `G9A3SpatialRedefineTransactionTest` y las pruebas de recuperación de Desktop, pero no las
    suites upstream completas.
  - `MAINT-A2`, solo si añade cambios en interfaces genéricas de Locus V2 (base local por
    tramo).
- `MAINT-A3` añade evidencia externa de lectores DXF (AutoCAD Core Console y un segundo lector
  independiente), que no es un perfil del verificador. `MAINT-A-DXF0` puede usar archivos de
  referencia producidos por lectores externos como fixtures, con su procedencia.
- Cada incremento con interfaz declara `GUIDE_IMPACT`; los que cambian requisitos de estación,
  el impacto de bootstrap.

## 6. Relación con versiones

Propuesta, no decisión:

- una versión de corrección agruparía `B5a`, `B2`, `U2`, `I2G0` y, si se decide, `B1a`;
- una versión funcional, la familia B-spline (A2, A3), la importación de referencia
  (A-DXF0), la aproximación (A-DXF1) y la sustitución (S1, S2).

Número, fecha, `FINAL` y publicación siguen siendo decisiones autorales separadas.

## 7. Deudas que permanecen diferidas

M08-D, I3, reconstrucción general, arrastre universal sobre Locus V2, redefinición general con
conservación de identidad, NURBS semántica nativa (`MAINT-A-R`, diseñada), importación DXF de
variantes no admitidas (3D, solo puntos de ajuste, periódicas, no ancladas, bloques),
persistencia de referencias en el documento (R-b, R-c, R-d) y aproximación dinámica vinculada,
`SPLINE` derivada de SplineV2, modelo certificado de la familia B-spline, recorrido condicionado
v2, cambios de la política de aislamiento de `Intersect`, identidad declarativa de piezas de
deslizadores, I2G-3 (conservar hermanos con el productor original), el fallo histórico de
Desktop OD-1 y la deriva de firma del proveedor OD-2. Ninguna es dependencia de un incremento
propuesto.

## 8. Riesgos del programa

| Riesgo | Mitigación |
|---|---|
| Ampliación de alcance desde la sustitución hacia I3 o la reconstrucción general | rechazo tipado de esos casos en S1 y S2; parada del prompt |
| Publicación parcial o reversión incompleta en la sustitución | requisitos de coherencia del diseño D §3.3; caracterización S0 antes de aprobar; inyección de fallos en cada etapa |
| Barrera I2G0 que rechace operaciones Classic legítimas | predicado basado en el riesgo; conducta Classic fijada por pruebas antes del cambio |
| Regresión en documentos 1.2.2 | pruebas de reapertura con los modelos del autor y con los documentos escritos por `9ccefdcf` en cada incremento de núcleo |
| Segunda autoridad geométrica por la importación | la referencia es un dato sin dependencias ni consumidores constructivos; R-d solo con responsabilidades del núcleo aprobadas |
| Aproximaciones leídas como geometría exacta de la fuente | conversión exacta y aproximación declaradas por separado; tres medidas de error con su grado |
| Coste de verificación excesivo | clase congelada por incremento; sin `FINAL` por categoría |
| Ediciones simultáneas del mismo borde | serialización B2 → S1, U2 → F8 y propiedad única de la biblioteca matemática |
| Expectativas de certificación de la familia B-spline | pares solo ricos declarados en A2; certificación diferida (A4) |
