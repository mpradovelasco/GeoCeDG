# GeoCeDG 1.2.2 — registro de la autorización autoral de las correcciones posteriores a 3B

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad del estado de aprobación de las decisiones normativas DN-1, DN-2 y DN-3 y de la autorización del plan correctivo C0 a C7. No aprueba ninguna implementación, no declara ningún incremento `PASS` y no aprueba ningún candidato |
| Instrucción | «GeoCeDG 1.2.2 — Autorización de correcciones posteriores a 3B», decisión del autor de 22 de noviembre de 2026: **PLAN CORRECTIVO C0–C7 AUTORIZADO, CON DN-1, DN-2 Y DN-3 APROBADAS BAJO CONDICIONES** |
| Repositorio | `mpradovelasco/GeoCeDG` |
| Informe que la origina | [informe de revisión técnica complementaria](post_r1_1_2_2_candidate_review_report.md), sobre el candidato `b2c5aad355d955d684512f6e08f986c200e25bb3` |
| Punta de la rama al registrar | `03580658449d346010a014ebf77ea03e91124d19`, en `maintenance/geocedg-1.2.2-post-r1`, local y sin publicar |
| `main` | local y `origin/main` en `9ccefdcfe922d8c0e329bdeec30c12fc058de463`; etiquetas de versión `v1.2.0` y `v1.2.1` |
| Fecha de versión | **`2026-11-22`**, designada por el autor en su instrucción anterior del mismo día («Se mantiene 2026-11-22 como fecha de versión del candidato GeoCeDG 1.2.2»). Deja de ser provisional |
| Registro de progreso | [progreso y checkpoints](post_r1_locus_repairs_progress.md), checkpoint 9 |
| `selfApproved` | `false` |

Este registro transcribe y ordena la instrucción del autor; ante cualquier diferencia
prevalece la instrucción autoral.

## 1. Qué autoriza

Ejecutar el plan correctivo C0 a C7 completo del informe (§7), mediante incrementos
trazables, con pruebas discriminantes y las verificaciones correspondientes a cada cambio, y
preparar un candidato funcional sucesor de `b2c5aad35`.

| Paso | Contenido | Decisión |
|---|---|---|
| C0 | Pruebas: restaurar con pruebas equivalentes los invariantes que dejaron de comprobarse en las adaptaciones anteriores; corregir las referencias obsoletas de la matriz CU2 | autorizado |
| C1 | Colisión de la clave de par | autorizado |
| C2 | Linaje estructural de componente en el selector de pares | **DN-1 aprobada** |
| C3 | Vocabulario del aislamiento `SAFEGUARDED_REGULAR_JACOBIAN` | autorizado |
| C4 | Admisión escalar de `LocusLength` condicionado | **DN-2 aprobada** |
| C5 | Memo de evaluaciones anidadas por instancia de evaluador | **DN-3 aprobada** |
| C6 | Segunda optimización | **solo estudio**: no se implementa sin justificar su alcance y compatibilidad |
| C7 | Candidato sucesor, regresiones, comprobaciones de Desktop y de empaquetado | autorizado |

## 2. Condiciones de DN-1 — identidad semántica de pares multicomponente

Aprobada adoptando el linaje estructural de componentes conforme a la decisión E-3 y al
contrato `spline-v2-pair-materialization.md`. La corrección debe resolver tanto la colisión de
claves como la asociación incorrecta de tokens tras cambios en los ordinales de componentes.

1. La identidad durable nunca dependerá de índices, posiciones, coordenadas, proximidad,
   orden de enumeración ni historia accidental.
2. La migración de entradas heredadas preservará tokens e identidades cuando exista una
   correspondencia semántica exacta y demostrable.
3. Si una entrada heredada no puede reconciliarse inequívocamente, conservará su identidad
   sin reasignación y quedará indefinida hasta disponer de evidencia admisible.
4. Deben verificarse persistencia nativa, Undo/Redo, copias, redefiniciones compatibles,
   cambios topológicos y recuperación de configuraciones anteriores.
5. No se introducirán cambios silenciosos de identidad ni de significado de los selectores.

## 3. Condiciones de DN-2 — admisión escalar de `LocusLength` condicionado

Aprobada mediante una enmienda específica de `locus-v2-metrics.md`, propuesta como §28.3. El
objetivo es que una longitud calculada sobre una ruta admisible de una región condicionada
pueda utilizarse directamente como valor numérico en el DAG. La admisibilidad exigirá:

1. Extremos semánticamente válidos y actuales.
2. Ruta completa dentro de una misma región condicionada admisible, conforme a las hipótesis
   H1 a H4.
3. Cobertura numérica de toda la ruta solicitada, diferenciada de la cobertura geométrica
   global.
4. Valor finito, estado satisfactorio y error estimado dentro de la tolerancia aplicable.
5. Ausencia de bandas o discontinuidades que impidan la travesía requerida.
6. Identificación explícita de las hipótesis, garantía numérica y vigencia.

El resultado admitido seguirá siendo condicionado y, cuando corresponda, de clase
`ESTIMATED_ERROR`. Podrá alimentar cálculos ordinarios, pero nunca constituirá por sí mismo
una prueba rigurosa de existencia, continuidad, completitud, incidencia o intersección. La
enmienda será compatible con §§25.4–25.5, 26.5–26.6 y el ADR 0042, manteniendo sus
restricciones fuera del nuevo supuesto expresamente autorizado. Debe verificarse el caso real
`LocusLength(w_2, P, Q)` y los casos negativos y de recuperación.

## 4. Condiciones de DN-3 — optimización D-V6

Aprobada la implementación de memoización de evaluaciones anidadas por instancia de
evaluador.

1. No habrá caché global ni compartición de objetos geométricos mutables.
2. Las claves comprenderán todas las dependencias semánticamente relevantes.
3. La reutilización será válida únicamente mientras permanezcan vigentes esas dependencias y
   el contexto de evaluación.
4. Se mantendrán presupuestos, diagnósticos, propagación por el DAG, invalidación y
   comportamiento determinista.
5. La implementación deberá reproducir bit a bit los resultados de referencia en los casos
   donde esa equivalencia sea exigible.
6. Se conservará una ejecución sin reutilización como oráculo de regresión.

La mejora debe evaluarse sobre cambios reales, configuraciones degeneradas, transiciones
topológicas y recuperación de estados válidos. Se miden por separado grafo, evidencia y
representación, comunicando mediana y p95. No se declara alcanzado un objetivo de latencia
interactiva basándose exclusivamente en medidas parciales. Si la memoización no proporciona
una mejora suficiente del tiempo total, se estudia la segunda optimización identificada, sin
implementarla hasta justificar su alcance y compatibilidad.

## 5. Resto del plan y gobierno

- El aislamiento numérico no certificado se mantiene claramente diferenciado de los
  certificados rigurosos.
- Cualquier contradicción normativa nueva se registra antes de modificar contratos fuera de
  las decisiones aquí aprobadas. Si una contradicción requiere modificar otras decisiones
  normativas, se detiene únicamente el incremento afectado y se presenta el problema con sus
  alternativas para decisión del autor.
- El informe de revisión se confirma en Git, en la rama local de trabajo.
- Se conservan los artefactos y los resultados históricos.
- Sobre el candidato sucesor se ejecutan las regresiones y verificaciones aplicables,
  incluidas las comprobaciones de Desktop y de empaquetado que quedaron pendientes, y se
  presentan resultados, desviaciones y riesgos residuales para revisión del autor.

## 6. Qué no autoriza

- la promoción a `main`;
- la creación de la etiqueta `v1.2.2`;
- una release;
- la ejecución de `FINAL`;
- la declaración `PASS — AUTHOR APPROVED` del candidato.

La autorización se limita a las correcciones y mejoras previstas en C0 a C7 con DN-1, DN-2 y
DN-3, respetando los contratos y garantías anteriores.

## 7. Aplicación

| Destino | Qué cambia |
|---|---|
| `spline-v2-pair-materialization.md` y especificaciones de intersección de pares | enmienda versionada de DN-1, con el incremento C2 |
| `locus-v2-metrics.md` | §28.3 nuevo de DN-2, con el incremento C4; §§25.4, 25.5, 26.5 y 26.6 no se reescriben |
| Especificaciones de la sesión de evaluación | sección de DN-3, con el incremento C5, si la norma vigente la requiere; la política de evidencia `conditioned-traversal/v1` no cambia |
| ADR 0042 y contrato `conditioned-use/v4` | no cambian |
| Hoja de ruta | registro aditivo fechado de esta decisión |

Ningún paso queda aprobado por anticipado: un resultado automatizado aceptado no constituye
aprobación autoral.
