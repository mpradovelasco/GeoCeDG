# PRE-G9B-MAINT-A-N2 — informe final consolidado

**PRE-G9B-MAINT-A-N2 — NORMATIVE DESIGN CANDIDATE — PENDING AUTHOR REVIEW**

> **Nota de R1 (2026-11-25).** Este informe registra el candidato A-N2 tal como se entregó y no
> se reescribe. Su estado vigente, las decisiones D-R1-1 a D-R1-10 y el paquete de adopción
> están en el [informe R1](pre_g9b_maint_a_n2_r1_report.md); donde ambos difieran, prevalece R1.

| Campo | Valor |
|---|---|
| Carácter | Informe del candidato normativo. `selfApproved=false`, `authorApproved=false`, `passClaimed=false`. Evidencia, no autoridad; no registra ninguna aprobación |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT-A-N2. Diseño normativo coordinado de persistencia R-c, exportación DXF de referencias y aproximación A-DXF1», del autor, 2026-11-25 |
| Prompt | [`pre-g9b-maint-a-n2-reference-store-export-approximation-normative-design.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-n2-reference-store-export-approximation-normative-design.prompt.md), commit de gobernanza `567c8714a` |
| Base | `a9ee803bf4be92c58ab7e120111ebacc70055487`, árbol `92f8ab0674587f3bc249ae7964e0eddb18b6a35c` (`PRE-G9B-MAINT SECOND WAVE — PASS — AUTHOR APPROVED — PUBLISHED ON MAIN`; `main`, `origin/main` y el remoto verificados iguales y limpios antes de empezar) |
| Rama / worktree | `phase/pre-g9b-maint-a-n2` / `GeoCeDG-wt-maint-a-n2` (hermano del repositorio principal; no se reutilizó ninguna rama ni se cambió el checkout principal) |
| Ruta y clase | `CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY`; `STATIC`; sin `PHASE`, `INTEGRATION` ni `FINAL` |
| Identidad del candidato y `STATIC` | §9 |

## 1. Decisiones generales del autor aplicadas

| Id | Decisión (transcrita en síntesis) | Dónde se aplica |
|---|---|---|
| D-A-N2-1 | referencia externa, construcción CeDG y render son entidades distintas; una referencia nunca se convierte implícitamente en `GeoElement`, `GeoLocusV2` ni participante del DAG | los tres contratos, §0 |
| D-A-N2-2 | sin compatibilidad exigida hacia lectores anteriores (1.2.2 incluida); sí: apertura correcta, persistencia íntegra, lectura de documentos antiguos, fallo seguro ante versiones desconocidas o datos malformados, restauración del documento previo, sin pérdidas silenciosas; incompatibilidad documentada | `R-c` §7 a §11; ADR 0044 |
| D-A-N2-3 | opción explícita de incluir referencias; por defecto las no materializadas; exclusión individual; construcciones exportadas desde `Construction`; sin duplicación automática; la elección del usuario prevalece con aviso; correspondencia explícita y persistente | exportación §6, §7; ADR 0045 |
| D-A-N2-4 | una referencia válida no convertible puede quedarse fuera de `Construction` indefinidamente y reexportarse; distinta de archivos malformados o datos insuficientes | A-DXF1 §4.1, §9; exportación §4, estado 6 |
| D-A-N2-5 | información ordinaria compacta; detalles técnicos en «Detalles…» | `R-c` §12; exportación §11; A-DXF1 §14 |

## 2. Cambios documentales

Commit de gobernanza `567c8714a`: el prompt de tarea (validado con
`Test-PromptContractDocument`, perfil `task`: siete campos en orden, ninguno ausente,
duplicado o desconocido, un título, `execution_safe = true`).

Commit de contenido (identidad en §9), todos nuevos y candidatos:

| Documento | Ruta |
|---|---|
| Contrato `R-c` `cedg-reference-store/v1` | [`geocedg/specs/import/reference-store.md`](../../geocedg/specs/import/reference-store.md) |
| Contrato de exportación `cedg-reference-export/v1` | [`geocedg/specs/export/dxf-reference-export.md`](../../geocedg/specs/export/dxf-reference-export.md) |
| Contrato A-DXF1 `dxf-spline-approximation/v1` | [`geocedg/specs/import/dxf-spline-approximation.md`](../../geocedg/specs/import/dxf-spline-approximation.md) |
| Enmiendas candidatas de textos aprobados | [`docs/architecture/pre_g9b_maint_a_n2_candidate_amendments.md`](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md) |
| ADR 0044 (propuesto) | [`docs/adr/0044-document-store-of-imported-references.md`](../adr/0044-document-store-of-imported-references.md) |
| ADR 0045 (propuesto) | [`docs/adr/0045-separate-dxf-export-of-imported-references.md`](../adr/0045-separate-dxf-export-of-imported-references.md) |
| Matriz de validación | [`pre_g9b_maint_a_n2_validation_matrix.md`](pre_g9b_maint_a_n2_validation_matrix.md) |
| Plan operativo de implementación | [`docs/architecture/pre_g9b_maint_interop_implementation_plan_candidate.md`](../architecture/pre_g9b_maint_interop_implementation_plan_candidate.md) |
| Informe de caracterización y decisiones | [`pre_g9b_maint_a_n2_characterization_report.md`](pre_g9b_maint_a_n2_characterization_report.md) |
| Este informe | `docs/validation/pre_g9b_maint_a_n2_candidate_report.md` |

Ningún texto aprobado, ADR aceptado, registro de aprobación, evidencia histórica, el roadmap
canónico, código, prueba, recurso, empaquetado ni verificador cambia. Los números de ADR 0044
y 0045 se comprobaron libres en las 116 referencias locales y remotas.

## 3. Evidencia de inspección del código

Resumen; el detalle con `fichero:línea` está en el
[informe de caracterización](pre_g9b_maint_a_n2_characterization_report.md) §2 a §4 y la
evidencia bruta en `artifacts/agent/pre-g9b-maint-a-n2-characterization/` del worktree
(ignorada por Git, con `SHA256SUMS.txt`).

- **Contenedor:** una entrada ZIP desconocida se ignora (imagen vacía registrada) y se pierde al
  guardar, igual en 1.2.2; un hijo desconocido de `<geogebra>` hace que los lectores anteriores
  lean sus hijos como elementos de primer nivel. De ahí la entrada `geocedg_references.xml`.
- **Transacciones:** las rutas nativas de apertura capturan y restauran el archivo y confirman
  el estado del anfitrión solo tras el éxito; `.html`, `setBase64` y `setXML` no son
  transaccionales; `openURL` upstream vacía antes de su diálogo (no conectado en GeoCeDG).
- **Fuera de Undo y del portapapeles:** Undo guarda una sola entrada; copiar/pegar ordinario no
  usa ZIP. El borrador previo suponía lo contrario para el portapapeles.
- **Estado guardado:** `App.isSaved()` y el almacén `R-a` permiten hoy descartar referencias sin
  pregunta; `R-c` sigue el patrón de capas ocultas.
- **Escáneres:** topes de 4 MiB por entrada; el almacén se acota a 4 MiB.
- **Exportación:** sin escritor `SPLINE`; el preflight exige una entidad de construcción; el DXF
  es ASCII; el validador del manifiesto iguala recuentos; las salidas de imagen ya incluyen
  referencias (R1).
- **Persistencia numérica:** sin riesgo de 15 cifras en las rutas de `BSpline`; `-0.0` se
  pierde; existe una ruta real de 15 cifras (números con decimal exacto en listas sin
  definición); la redefinición compatible por sustitución pierde el bloque de procedencia.
  Probado con una sonda desechable en el checkout principal, sin cambios rastreados.

## 4. Hipótesis confirmadas o descartadas

La tabla completa está en el informe de caracterización §5. Las correcciones con efecto
normativo son:

1. **Refutada:** el portapapeles nativo transporta toda entrada distinta de `geogebra.xml`.
2. **Refutada:** las exportaciones de imagen omiten referencias (R1 las incluye en Gráficos 1).
3. **Refutada para `BSpline`:** argumentos literales con 15 cifras; **hecho nuevo:** la ruta
   real de 15 cifras y la pérdida de `-0.0`.
4. **Refutada en parte:** el bloque de procedencia se conserva en toda redefinición compatible.
5. **Revisadas:** tope del almacén (64 MiB → 4 MiB), fallo de guardado por cadenas (sustituido
   por codificación), seis estados de exportación (→ ocho de la instrucción más la elección del
   usuario), conjunto de verificación que incluía las muestras (→ conjunto reservado, disjunto
   de las muestras y de la rejilla de refinamiento), cota
   certificada «alcanzable ahora» (→ `MAINT-A-DXF1b`), independencia de DXF1 respecto de RC
   (→ estricta).
6. **Hechos nuevos:** preflight con al menos una entidad de construcción; DXF solo ASCII;
   validador del manifiesto; `isSaved`; escáneres; Classic ya pierde capas ocultas.

## 5. Compatibilidad arquitectónica

Coherente con AGENTS §1, §4, §13 y §17 y con ADR 0016, 0014 y 0043 en los términos del informe
de caracterización §6: almacén documental fuera del núcleo; adaptador de exportación sin
geometría nueva; biblioteca pura y servicio externo para A-DXF1; ningún segundo DAG; ninguna
asociación inferida. Las tensiones con textos aprobados se resuelven mediante las enmiendas
candidatas y dos ADR propuestos; ninguna exige cambiar `MyXMLHandler`, Undo, la identidad
espacial, el formato numérico del núcleo ni la gramática de `<geocedgBSpline>`.

### 5.1 Revisión independiente antes de congelar

Un análisis separado revisó los nueve documentos frente a la instrucción y los textos
aprobados: ningún defecto bloqueante ni redacción de autoaprobación; nueve hallazgos
importantes y siete menores, todos corregidos antes del commit de contenido. Los principales:
la afirmación sobre condicionamiento (sin «guardas de longitud de tramo»); el conjunto de
verificación, que el refinamiento usaba (ahora reservado, con rejilla de refinamiento propia);
la imposibilidad práctica de aproximar polinómicas de más de 128 puntos con los nudos de la
fuente; el modelo de error de `ε_floor` y la propuesta del diálogo; el mejor indicador de un
fallo, que el almacén no podía guardar; límites comprobados en cada mutación y capacidad real
del almacén frente a los límites aprobados de importación (A-IMP-9); la redacción sobre
resúmenes de datos (comprobación de coherencia, nunca asociación); y la alineación de las
dependencias de EXP y A-DXF1 entre contratos, plan y registro propuesto del roadmap.

## 6. Decisiones propuestas al autor

Numeradas por contrato; cada una con su motivo. Las listas completas están en cada contrato
(`RC-D*`, `RX-D*`, `AX-D*`) y en el plan (`D-P-*`).

### 6.1 Contrato `R-c`

| N.º | Decisión | Recomendación | Motivo |
|---|---|---|---|
| N2-R1 | soporte del almacén | entrada ZIP fija `geocedg_references.xml`, versión dentro (RC-D1, RC-D2) | fuera de Undo y del XML de construcción; los lectores anteriores la ignoran sin interpretar nada; una versión futura llega al lector `v1` y se rechaza |
| N2-R2 | números | patrones de bits binary64 en 16 hex (RC-D3) | exactos, canónicos, incluido `-0.0` |
| N2-R3 | GeoCeDG Classic | conservar los bytes validados (RC-D4); alternativas: rehusar guardar o aceptar la pérdida como las desviaciones previas | «sin pérdida silenciosa» y ADR 0016 d. 7 |
| N2-R4 | quitar una referencia | fuera de Undo, con confirmación (RC-D5) | Undo de la construcción intacto; recuperación reabriendo o reimportando |
| N2-R5 | Insertar archivo | no transferir referencias (RC-D6) | sencillo y explícito; las derivadas insertadas quedan huérfanas (estado 8) |
| N2-R6 | almacén corrupto | rechazo del documento entero; acción explícita «Abrir sin referencias» en un incremento aparte (RC-D7) | ningún subconjunto silencioso |
| N2-R7 | guarda `geocedg_*` | sí (RC-D8) | rechazo seguro de bloques futuros |
| N2-R8 | límites | entrada ≤ 4 MiB y los de §5.3, comprobados en cada mutación (RC-D9): unas 39 referencias máximas o 1 300 típicas, menos que los límites aprobados de importación (enmienda A-IMP-9) | alineado con los escáneres; alternativas: tope mayor actualizando escáneres, o guardar solo `source` |
| N2-R9 | lectores anteriores | incompatibilidad declarada, sin marcador forzado (ADR 0044 d. 6) | el autor no exige compatibilidad; un marcador invadiría el XML de construcción |
| N2-R10 | nombre, `data` y reservas | nombre solo si se renombra; `data` además de `source`; `<derivations>` y `export` reservados desde `v1` (RC-D10 a RC-D12) | identidad nunca por nombre; detección de corrupción; sin `v2` al llegar EXP y DXF1 |

### 6.2 Contrato de exportación

| N.º | Decisión | Recomendación | Motivo |
|---|---|---|---|
| N2-X1 | **estado 4 «construcción derivada modificada»** (admite más de una interpretación: se presentan las alternativas antes de fijar la política) | **A: incluir la referencia**; B: excluirla con aviso; C: sin valor por defecto, elección obligatoria (RX-D2; contrato §6.5) | la construcción modificada ya no la representa válidamente (D-A-N2-3) y la referencia nunca se pierde (D-A-N2-4) |
| N2-X2 | superficie y elecciones | casilla en el diálogo DXF existente, desmarcada; conmutadores por operación; preferencia persistente en el gestor (RX-D1, RX-D10) | la exportación ordinaria no cambia; exportar nunca ensucia el documento |
| N2-X3 | clase de fidelidad | `REFERENCE_DEFINITION`: relectura al mismo binary64, comprobada antes de promover (contrato §5) | distinta de `EXACT` y `APPROXIMATE`; separa los cuatro redondeos |
| N2-X4 | pesos | forma canónica: pesos efectivos solo si alguno ≠ 1 (RX-D3) | conserva naturaleza y pesos efectivos; la representación original va al manifiesto |
| N2-X5 | capa | `GEOCEDG_REF` fija (RX-D7) | ASCII; la procedencia no se convierte en presentación |
| N2-X6 | conformidad externa | AutoCAD Core Console y **un segundo lector que designe el autor** (RX-16) | obligación verificable; el lector GeoCeDG no es independiente |
| N2-X7 | área de exportación | envolvente convexa conservadora del polígono de control (RX-D9) | regla B1: fuera solo con prueba de disjunción |
| N2-X8 | ADR 0014 d. 8 | precisada para referencias por ADR 0045; vigente para construcciones | la transferencia de datos externos no prueba exactitud de construcciones |
| N2-X9 | posición de los códigos 41, banderas 1 y 210/220/230 | fijadas por la evidencia de conformidad (RX-D4 a RX-D6) | evita suponer conducta de lectores |
| N2-X10 | referencias con datos idénticos (estado 7) | ambas incluidas como referencias distintas, señaladas con `SAME_DATA_AS_REFERENCE` (RX-D15) | deduplicar por datos sería asociar por coordenadas |
| N2-X11 | dependencia de la exportación | estricta: contrato aprobado e hito de tipos RC-M; integración tras RC (RX-D13) | alineada con el plan |

### 6.3 Contrato A-DXF1

| N.º | Decisión | Recomendación | Motivo |
|---|---|---|---|
| N2-A1 | dependencia de `R-c` | estricta (AX-D1) | la correspondencia persistente necesita el identificador del almacén |
| N2-A2 | cota certificada | separada en `MAINT-A-DXF1b`; A-DXF1 publica `SAMPLE_RESIDUAL` y `ESTIMATED` con `boundStatus="NOT_COMPUTED"` (AX-D2) | el procedimiento es válido (racional exacta = intervalos de anchura cero, denominadores positivos por pesos `> 0`) pero su coste no está medido; la gramática ya reserva los atributos |
| N2-A3 | medidas | tres conjuntos disjuntos: muestras de ajuste, rejilla de refinamiento y conjunto de verificación reservado, evaluado una sola vez sobre el candidato final; éxito si residuo y estimación reservada son ambos `≤ ε`; ningún orden afirmado; fallo reservado `VERIFICATION_NOT_PASSED` (AX-D3) | import §14.1 y observación AP-2; la estimación no se usa para elegir los nudos |
| N2-A4 | bloque `v1` malformado | cargar el objeto, conservar el bloque, `NOT_INTERPRETABLE` (AX-D4) | degradar la garantía, no el objeto |
| N2-A5 | grado y constantes | `q = 3` por defecto; constantes candidatas pendientes de validación (AX-D5, AX-D6) | valores revisables tras medir |
| N2-A6 | cero con signo | normalizar `-0.0` a `+0.0` antes de crear; «exacta» = igualdad bajo `==` (AX-D12) | los escritores exactos del núcleo pierden el signo; no se tocan |
| N2-A7 | representación de entradas | puntos libres por valor; nudos en una lista definida por decimales más cortos (o números libres); grado entero; nunca números con decimal exacto (AX-D9) | rutas probadas exactas |
| N2-A8 | bloque perdido en la redefinición por sustitución | corregirlo dentro de A-DXF1 (AX-D13); alternativa: correctivo previo de A2 | defecto latente: ningún documento tiene hoy un bloque |
| N2-A9 | nudos de la fuente que exceden `N_A` | fallo tipado; la referencia sigue exportable (AX-D11) | sin reducción silenciosa de nudos |
| N2-A10 | QR, puerta y madurez | Householder propio; misma puerta que `BSpline`; `experimental` (AX-D7, AX-D10) | orden de operaciones auditable; sin promoción implícita |

### 6.4 Enmiendas y plan

| N.º | Decisión | Recomendación | Motivo |
|---|---|---|---|
| N2-M1 | forma de las enmiendas | candidatas separadas, agrupadas por contrato; aplicadas por un descendiente documental tras la aprobación de cada grupo | ningún texto aprobado cambia antes de la decisión |
| N2-P1 | id del incremento de exportación | `MAINT-A-DXF0-EXP` (D-P-1) | agrupa la línea de importación |
| N2-P2 | hito de modelo RC-M | sí (D-P-2) | un solo modelo compartido |
| N2-P3 | paralelismo | RC-S0 ∥ DXF1-S0; RC ∥ EXP (tras RC-M) ∥ biblioteca de DXF1; integración RC → EXP → DXF1 → DXF1b (D-P-3, D-P-4) | calendario corto con integración serial |
| N2-P4 | EXP y `MAINT-A3` | nunca en paralelo; EXP antes (D-P-5) | mismos ficheros |

## 7. Riesgos y condiciones de parada

| Riesgo | Contrato | Mitigación o parada |
|---|---|---|
| pérdida declarada en 1.2.2 al guardar | `R-c` | notas de versión; decisión del autor |
| una ruta vacía el almacén antes de confirmar | `R-c` | C-1; parada si no se corrige en Desktop |
| Classic pierde contenido GeoCeDG (ya ocurre con capas ocultas) | `R-c` | RC-D4 |
| lectores externos no conformes con `SPLINE` racional | exportación | RX-16; **parada** |
| duplicación forzada por el usuario | exportación | aviso `DUPLICATE_REPRESENTATION` |
| estado obsoleto tras Undo | exportación | estados recalculados, nunca guardados |
| pérdida de bits al persistir | A-DXF1 | §12 y autocomprobación; **parada** si ninguna ruta estándar es exacta y estable |
| bloque perdido al redefinir | A-DXF1 | corrección y NUM-6 |
| coste de la cota exacta | A-DXF1b | presupuesto y última cota válida |
| churn de inventarios y pines | todos | integración serial y regeneración canónica |

Condiciones de parada de este incremento: ninguna se activó. No hizo falta tocar rutas
prohibidas, el formato numérico del núcleo ni una semántica de persistencia compartida.

## 8. Dependencias de los incrementos futuros

Véase el [plan operativo](../architecture/pre_g9b_maint_interop_implementation_plan_candidate.md)
§1 a §3. Estrictas: RC tras RC-S0 y la aprobación de `R-c`/ADR 0044; EXP tras la aprobación
de su contrato/ADR 0045 y el hito RC-M; DXF1 tras la aprobación de su contrato, DXF1-S0 y RC
integrado; DXF1b tras DXF1; EXP y `MAINT-A3` nunca en paralelo. Convenientes: integrar RC antes
que EXP y EXP antes que DXF1. Ningún worktree de implementación se ha creado.

## 9. Identidad del candidato y `STATIC`

Registrado en un descendiente documental del commit de contenido (un documento no puede
contener su propio SHA). El descendiente solo cambia este informe.

| Elemento | Valor |
|---|---|
| Commit de gobernanza (prompt) | `567c8714a`, padre `a9ee803bf` |
| **Candidato de contenido** | `874286e95564234d27094c9c983e69f9bff160bf`, árbol `30c21042c4b18a1416374a1ea0cfd9a4802cf6fa`, padre `567c8714a` |
| Orden | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-maint-a-n2-static-874286e9`, ejecutada desde el worktree A-N2 (PowerShell 7, directorio de proceso fijado al worktree; consola redirigida a la carpeta temporal de la sesión) |
| Código de salida | `0` |
| Ejecución | `verification-d3df6431fbfb4f64a893fb6af812525a`; plan `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52` |
| Identidad ligada | `candidate_commit = 874286e95…`, `candidate_tree = 30c21042c…` (el árbol estaba limpio y confirmado) |
| Veredictos | aceptación `ACCEPTED`; cobertura `COMPLETE` |
| Diagnósticos | 1 hallazgo: `diagnostic.governance` `DIAGNOSTIC_FINDING` (diagnóstico permanente de gobernanza, sin efecto en la aceptación); `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`; documentación, estructura de guías, estilo y espacios en blanco `DIAGNOSTIC_CLEAR` |
| Informe | `artifacts/agent/pre-g9b-maint-a-n2-static-874286e9/verification-result.json` del worktree (ignorado), SHA-256 `707a273ecf438dff21802a54e537616c80817fbdf980dadf4bbed0aa9a043a27` |
| Comprobaciones previas | validación del contrato del prompt (`execution_safe = true`); enlaces relativos, UTF-8 y LF de los documentos nuevos; `git diff --check` limpio |

`STATIC` es una aceptación técnica de la forma del candidato; no es aprobación del autor ni
valida el contenido normativo.

## 10. Cuadro final de decisiones por contrato

Cada contrato puede aprobarse por separado. Aprobar un contrato aprueba su texto, el grupo de
enmiendas que lo acompaña y, cuando lo hay, su ADR; no autoriza implementación.

| Contrato | Documentos que se aprueban | Decisiones a tomar | Dependencias para aprobarlo | Qué habilitaría (con autorización aparte) |
|---|---|---|---|---|
| **`R-c`** `cedg-reference-store/v1` | `reference-store.md`; ADR 0044; enmiendas grupo R (A-IMP-1, A-IMP-2, A-IMP-3, A-IMP-9, A-IMP-10, A-NDI-1, A-ADR-3) | N2-R1 a N2-R10 (en especial N2-R3 Classic y N2-R8 límites) | ninguna | `MAINT-A-DXF0-RC-S0`, `MAINT-A-DXF0-RC` |
| **Exportación** `cedg-reference-export/v1` | `dxf-reference-export.md`; ADR 0045; enmiendas grupo X (A-IMP-4 a A-IMP-6, A-BS-1, A-EXP-1 a A-EXP-6, A-GEF-1, A-ADR-1, A-ADR-2) | **N2-X1 (estado 4)**, N2-X2 a N2-X9 (en especial N2-X6, segundo lector) | `R-c` aprobado (identidad y preferencias del almacén) | `MAINT-A-DXF0-EXP` |
| **A-DXF1** `dxf-spline-approximation/v1` | `dxf-spline-approximation.md`; enmiendas grupo A (A-IMP-7, A-IMP-8, A-BS-2) | N2-A1 a N2-A10 (en especial N2-A2 separación de la cota y N2-A8) | `R-c` aprobado (N2-A1); la lectura del bloque por la exportación se aprueba con el contrato de exportación, cuyos estados 2 a 5 y 8 dependen a su vez de A-DXF1 §10 y §11 | `MAINT-A-DXF1-S0`, `MAINT-A-DXF1`, más tarde `MAINT-A-DXF1b` |
| Plan y matriz | plan operativo; matriz de validación (no son contratos) | N2-M1, N2-P1 a N2-P4 | — | orden y paralelismo de los incrementos |
| Registro del roadmap | texto A-RM-1 | redacción y momento | tras las aprobaciones | registro aditivo del roadmap |

## 11. Estado final

```text
PRE-G9B-MAINT-A-N2 — NORMATIVE DESIGN CANDIDATE — PENDING AUTHOR REVIEW

selfApproved             = false
authorApproved           = false
passClaimed              = false
implementationAuthorized = false   (RC-S0, RC, EXP, DXF1-S0, DXF1, DXF1b, A3, A-R)
BOOTSTRAP_IMPACT         = NO_CHANGE (documentación; sin requisitos de estación)
GUIDE_IMPACT             = NONE en este incremento
```

Las propuestas no son normas vigentes. Sin push, merge, `main`, etiquetas, releases ni
empaquetado. El worktree `GeoCeDG-wt-maint-a-n2` se conserva para la revisión.
