# PRE-G9B-MAINT-A-N2-R4 — informe único de la corrección final de gramática y del paquete de adopción

**PRE-G9B-MAINT-A-N2-R4 — NORMATIVE DESIGN CANDIDATE — ADOPTION PREPARATION STOPPED: ONE AUTHOR DECISION REQUIRED (D-R4-1)**

La revisión independiente final encontró dos contradicciones materiales (§7):

- **M1:** la ruta de exportación solo de referencias se había descrito también para el flujo G5
  heredado. Está **corregida** en el candidato.
- **M2:** la condición de parada del prompt R4 se cumple. La conservación sin pérdida de la
  normal exige un cambio aditivo del importador en `MAINT-A-DXF0-RC`, y eso es una decisión del
  autor, **D-R4-1** (§9).

Conforme a la instrucción («si detecta una contradicción material, detener la preparación de
adopción y comunicarla»), la preparación de la adopción se detiene en D-R4-1. Los registros de
§10 quedan como borradores preparados antes de la revisión y no se usan hasta esa decisión.

| Campo | Valor |
|---|---|
| Carácter | Informe de la corrección documental R4. `selfApproved=false`, `authorApproved=false`, `passClaimed=false`. Evidencia, no autoridad; no registra ninguna aprobación |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT-A-N2-R4. Corrección final de gramática R-c y cierre de observaciones de auditoría», del autor, 2026-11-26 |
| Prompt | [`pre-g9b-maint-a-n2-r4-final-grammar-correction.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-n2-r4-final-grammar-correction.prompt.md), commit de gobernanza `7e03d0fa1` (contrato de prompt validado, `execution_safe = true`) |
| Base | descendiente R3 `476f6976fd07679c919ea7782ba9fa393fd9a396`, árbol `862b3975a840da36af2f0d76bf8c77a3d13e73d3`; contenido R3 `da27d459091341b4952d4fc14250fc6e29b838aa`, árbol `c0aa42375cda55dd0109b3f981f788a62f6e2d75`; `main` = `origin/main` = remoto = `a9ee803bf` |
| Rama / worktree | `phase/pre-g9b-maint-a-n2-r4`, creada desde `476f6976f` en el worktree reutilizado `GeoCeDG-wt-maint-a-n2` (limpio, sin bloqueo de índice ni proceso que lo use); las ramas A-N2, R1, R2, R3 y la rama de adopción vacía quedan intactas |
| Ruta y clase | `ORDINARY`; `DOCUMENTATION_STATUS_ONLY`; `STATIC`; sin `PHASE`, `INTEGRATION` ni `FINAL`; ningún fichero de producto modificado |

## 1. Candidato R4

Commit de contenido y árbol: se registran en el descendiente documental de este informe (§8),
porque un documento no puede contener su propio SHA.

## 2. I-1 — gramática de `normal`

**Comportamiento real del lector (inspeccionado en la base).** `DxfSplineReader` guarda los
grupos 210, 220 y 230 en un único vector de tres componentes inicializado a `0.0`. Una aparición
del grupo asigna su componente; si el grupo se repite, gana la última. Un único indicador se
activa si aparece cualquiera de los tres. `DxfSplineReference.normal()` devuelve `null` o ese
vector. Por tanto:

- un componente ausente se lee `0.0`, y un 230 ausente no toma el valor DXF por defecto 1;
- no se conserva qué grupos estaban presentes;
- el diagnóstico `NORMAL_RECORDED` coincide exactamente con «algún grupo presente».

R3 rellenaba los ausentes con `(0, 0, 1)`, lo que no coincidía ni con el lector ni con la
procedencia.

**Corrección** (`reference-store.md` §5.2 punto 11, hecho F22, decisión RC-D16; enmienda
A-IMP-12):

- `normalGroups` = `none` o el subconjunto presente de `210 220 230`, en orden creciente;
  `normal` está presente si y solo si hay grupos, con un valor binary64 por grupo listado.
- Los cuatro casos quedan distintos:
  - ninguno;
  - los tres explícitos, aunque valgan `0 0 1`;
  - presencia parcial, que nunca se confunde con una terna completa;
  - la normal efectiva (210 y 220 ausentes = 0, 230 ausente = 1), que se deriva y nunca se
    guarda ni se atribuye al DXF.
- Ningún ausente se escribe como `0.0` ni como `1.0`.
- Validación: los malformados se rechazan con `MALFORMED_STORE`, y `NORMAL_RECORDED` debe
  aparecer si y solo si hay grupos.
- El contrato describe cómo representa hoy el lector los ausentes y por qué ese vector no se
  serializa por sí solo.
- El importador **no** se modifica en R4. Se propone que `MAINT-A-DXF0-RC` añada al registro la
  presencia por grupo de forma aditiva, sin cambiar admisión ni valores (A-IMP-12, plan §4.1,
  prompt candidato de RC). Ese cambio queda **pendiente de la decisión del autor D-R4-1** (§9).
- **Exportación:** se conserva la normal de salida `0/0/1` de la curva 2D (RX-D6). El contrato
  dice ahora que nunca son los grupos de origen y que estos no se escriben ni se atribuyen al
  DXF.
- Matriz: nuevo caso RC-25.
- ADR 0044 no necesita cambio por I-1: la gramática es un detalle del contrato.

## 3. I-2 — contador `values`

**Hallazgo.** R3 definía `values` como «todos los binary64 de la entrada», pero el ejemplo
(`values="27"`) y la sonda de R3 excluían `planeZ`, mientras que el punto 11 contaba `normal`.
La sonda contaba `6 + K + (4 + e) · c` por referencia (fuente de la sonda, `values()`).

**Corrección** (`reference-store.md` §5.2 puntos 1 y 12; decisión RC-D17):

- `values` cuenta todos los binary64 **serializados** y nada más:
  `v_r = K_r + (4 + e_r) · c_r + 7 + g_r`, con:
  - nudos `K_r`;
  - `x` e `y` de origen `c_r` cada una;
  - pesos explícitos `e_r · c_r`;
  - `x` e `y` transformadas `c_r` cada una;
  - seis coeficientes de la transformación;
  - `planeZ` 1;
  - los `g_r` componentes presentes de la normal.
- Se enumeran los campos que no cuentan: enteros, identificadores y resúmenes, banderas y
  enumeraciones, cadenas, y todo valor implícito o derivado.
- Escritor y lector aplican la misma fórmula. El lector cuenta cada token de 16 dígitos con un
  único contador, independiente del orden XML, y comprueba cada lista contra su tamaño.
- Un recuento incoherente da `MALFORMED_STORE`, y un `values` declarado por encima de
  `maxValues` da `STORE_BUDGET_EXCEEDED` antes de asignar.
- Un campo opcional ausente no aporta nada.
- No hay restricción geométrica nueva: una referencia admisible cuenta como máximo 6 162.
- El ejemplo pasa a `values="29"` (con solo el grupo 230), y a 28 sin normal.
- Matriz: nuevo caso RC-26.

**Mediciones.** El recuento R4 nunca es menor que el de la sonda para el mismo almacén. Los
corpus medidos a 1,5 M cuentan 1 502 697 (R3-T) y 1 510 000 (R3-NS) con la regla R4. Por tanto,
un almacén admitido en `maxValues` con la regla R4 no contiene más valores serializados que
ellos, y las mediciones siguen siendo cota superior para la misma composición. `maxEntryBytes`
y sus porcentajes no cambian, porque esos `planeZ` ya estaban serializados (§5.3, párrafo
«Author decision and counter of R4»).

## 4. Presupuesto

Se registra la decisión del autor:

- 1 500 000 valores como valor inicial provisional y configurable de producto (RC-D9, ADR 0044
  decisión 8, A-IMP-9, propuesta de R-c);
- 2 000 000 no se elige sin nueva evidencia.

Se mantienen:

- la configuración explícita;
- `source` y `data` persistidos;
- la lectura incremental con límites;
- el rechazo seguro;
- C-4 y C-5 como puertas de cierre de RC;
- la distinción entre límites geométricos, seguridad del lector y recursos configurables.

## 5. Hallazgos menores

| Hallazgo | Resolución |
|---|---|
| m-1 lectura informativa de procedencia | A-BS-1 (R4) admite exactamente dos lecturas informativas del significado del bloque pasivo: presentar al usuario el estado de conversión o aproximación, y decidir la inclusión predeterminada en la exportación DXF. Ninguna es dependencia geométrica ni altera el DAG. El códec del bloque y la autocomprobación de persistencia, que solo verifican sus bytes, no son lecturas de su significado. Alineados ADR 0045 decisión 4, A-ADR-2, el inventario de enmiendas, el contrato de exportación §6, A-DXF1 §10.2 y §14, y la propuesta de A-DXF1 |
| m-2 decisiones pendientes | Propuesta de exportación: la ampliación de RX-D19 a referencias excluidas por otras causas queda resuelta negativamente por el autor; RX-D19 en el contrato lo registra, y «ninguna decisión pendiente» deja de contradecir el informe R3 §11 |
| m-3 controlador DXF | Hecho nuevo del contrato de exportación §2: `showExportDialog` ejecuta el flujo G5 heredado (`showLegacyExportDialog`, sin preflight ni manifiesto) salvo con la bandera extendida activa, que lleva al flujo emparejado (`showExtendedExportDialog`, `chooseRequest`); ambos rechazan una población `GeoElement` vacía antes del modelo y del preflight. Nueva §9.1: la ruta de exportación solo de referencias es obligación de `MAINT-A-DXF0-EXP` **solo en el flujo extendido**. El G5 heredado no se amplía (§8 del contrato): no ofrece la opción y su guarda queda idéntica; §11 lo hace explícito. Caso discriminante RX-15 (e) por el controlador real en ambos flujos; prompt candidato de EXP, nota en A-GEF-1 y plan §4.2. El código del controlador no se modifica |
| m-4 roadmap | A-RM-1 (enmiendas §5) es el **único** borrador canónico, en el idioma del roadmap, con C-4 y C-5 como puertas de cierre de RC. El borrador del informe R3 §12.3 queda marcado como sustituido. El roadmap vigente no se modifica |
| m-5 descendiente documental | §8: el `STATIC` pertenece al commit de contenido; el descendiente solo tiene la clasificación canónica, sin `STATIC` propio. Se registra también la clasificación del descendiente R3, que no constaba en el informe R3 |

**Clasificación del descendiente R3 (m-5).** Para la orden
`tools/agent/documentary-descendant.ps1 -TechnicalCommit da27d459… -DocumentCommit 476f6976…`:

| Elemento | Valor |
|---|---|
| Ascendencia | verificada |
| Ruta cambiada | `docs/validation/pre_g9b_maint_a_n2_r3_report.md`, `DOCUMENTARY` |
| Veredicto | `DOCUMENTARY_ONLY` |
| Resultado técnico reutilizable | el de `da27d459…` |
| `rerun_required` | `false` |

El JSON fue idéntico en la ejecución original de R3 y en la repetición de R4 (SHA-256
`bd96bc47d915b13e3d72e066fb33ee9f8107e7e7174238625f6ad69d326577fa`, fichero de la sesión). El
`STATIC` de R3 (`verification-1ac443ed…`) es el resultado técnico de `da27d4590`; el
descendiente `476f6976f` no tiene `STATIC` propio.

## 6. Comprobaciones documentales

Se ejecutaron sobre el árbol de trabajo antes del commit de contenido:

- validación del contrato de prompt de la tarea R4 y de los prompts candidatos RC-S0, RC y EXP;
- enlaces relativos;
- UTF-8 y LF;
- columnas de tablas;
- `git diff --check`.

Los resultados constan en §8.

## 7. Revisión independiente final

Revisión independiente, solo de lectura, del contenido resultante. Incluyó el código del lector
y del controlador, la sonda de R3 y `char-budget3m.md`, no solo el diff.

**Veredictos:**

- **I-2, presupuesto, m-2, m-4 y m-5: resueltos.** Las cifras se recalcularon:
  - el ejemplo da 29 y 28;
  - el máximo por referencia es 6 162;
  - los recuentos R4 de los corpus son 1 502 697 y 1 510 000;
  - la sonda sí serializaba `planeZ`, así que la cota superior se sostiene.
- **I-1:** resuelto en contenido. F22 es exacto respecto de `DxfSplineReader` y
  `DxfSplineReference`.

**Global: MATERIAL CONTRADICTION FOUND**, con dos contradicciones.

- **M1, corregida.** RX-15 (e), el hecho §2 y §9.1 pedían la ruta también en el flujo
  «ordinario», que es el G5 heredado: sin preflight ni manifiesto. Eso contradice «The legacy
  G5 route is not extended» (§8) y el alcance prohibido del prompt de EXP. Además, el hecho §2
  nombraba un método inexistente (`exportDxf`).
  - Corrección: la ruta queda solo en el flujo extendido; el G5 heredado queda idéntico y sin
    opción; métodos reales nombrados.
  - Ajustados: contrato §2, §9.1 y §11; RX-15 (e); prompt de EXP; nota de A-GEF-1; plan §4.2.
- **M2, no corregible por el agente.** La condición de parada del prompt R4 se cumple: el
  lector actual no admite una representación sin pérdida sin un cambio del importador (F22).
  El informe la había dispensado por su cuenta.
  - Se convierte en la decisión pendiente **D-R4-1** del autor: §9, RC-D16 del contrato, la
    propuesta de R-c y A-IMP-12.

**Menores, todos corregidos:**

- A-DXF1 §10.2 hablaba de un único lector: ahora nombra las dos lecturas.
- El códec y la autocomprobación no son lecturas del significado del bloque: A-BS-1, ADR 0045 y
  A-DXF1 §10.2.
- `normalGroups="none"` añade 6 bytes por referencia, un 0,17 % de `maxEntryBytes`, dentro de
  la asignación agregada; R3-NS pasa del 90,6 % al 90,7 % (§5.3).
- El ejemplo usaba códigos que el lector ignora (`62`, `370`): ahora usa `90` y `280` y añade
  `UNRECOGNIZED_GROUP_CODES` en el orden de la enumeración.
- La propuesta de R-c aclara que de RC-D9, RC-D16 y RC-D17 son del autor el requisito y el valor
  del presupuesto, y que la gramática es propuesta de R4.
- Las letras de los casos de RC-25 se alinean con el contrato: (d) normal efectiva, (e) grupo
  repetido.

Las correcciones posteriores a la revisión no se sometieron a otra revisión, conforme a «no
abrir otro ciclo general de rediseño». Se comprobaron con las verificaciones documentales de §6.

## 8. Identidad y resultado de `STATIC`

Se registran en el descendiente documental del commit de contenido R4.

## 9. Decisiones residuales

**Una decisión bloqueante del autor: D-R4-1.**

- **D-R4-1 — cambio aditivo del registro de importación en `MAINT-A-DXF0-RC`.** Conservar sin
  pérdida los grupos 210, 220 y 230 y su presencia (política del autor para I-1) exige que el
  registro de importación sepa qué grupos estaban presentes. El lector actual no lo conserva y
  rellena los ausentes con `0.0` (F22).
  - **Propuesta:** autorizar que `MAINT-A-DXF0-RC` añada esa presencia al registro de forma
    aditiva (enmienda A-IMP-12 de `dxf-spline-reference.md` §4.3), sin cambiar admisión,
    valores, transformación, resúmenes ni el resto de la importación. El plan §4.1 y el prompt
    candidato de RC ya lo permiten.
  - R4 no toca el importador. La condición de parada del prompt R4 se cumple y, por eso, la
    preparación de la adopción se detiene aquí.
  - **Alternativa, si el autor no lo autoriza:** `cedg-reference-store/v1` no podría cumplir
    RC-D16 tal como está escrito. Habría que volver a decidir la política de I-1, por ejemplo
    guardando el vector actual con una marca de presencia desconocida, lo que contradice
    «la presencia parcial no puede confundirse con una terna explícita completa».
- **Limitación declarada, no decisión.** Un grupo 210, 220 o 230 repetido dentro de una entidad
  conserva su última aparición, como hace hoy el lector. Las apariciones anteriores no forman
  parte de la procedencia.
- **Puertas de sus incrementos, no decisiones de adopción:**
  - C-4 y C-5 en el presupuesto (RC);
  - la evidencia de conformidad RX-16, RX-17 y RX-19 (EXP);
  - las constantes de A-DXF1 (AX-D6, puerta de cierre de `MAINT-A-DXF1`).

## 10. Registros de aprobación preparados

Borradores, **sin aprobación registrada**, preparados antes de la revisión final. Su uso queda
**detenido hasta la decisión D-R4-1** (§9). Si el autor la toma a favor, cada registro se
escribirá como registro autoral solo tras su decisión expresa sobre el candidato R4 exacto
(§1, §8), con D-R4-1 citada en 10.1. Ninguno requiere otra fase de diseño.

### 10.1 `R-c` + ADR 0044

| Elemento | Contenido |
|---|---|
| Contrato y revisión | [`cedg-reference-store/v1`](../../geocedg/specs/import/reference-store.md), revisión R4; [ADR 0044](../adr/0044-document-store-of-imported-references.md), revisión R4 |
| Candidato | commit y árbol de contenido R4 (§8) |
| Enmiendas | grupo R: A-IMP-1, A-IMP-2, A-IMP-3, A-IMP-9, A-IMP-10, A-IMP-12, A-NDI-1, A-ADR-3 |
| Decisiones adoptadas | D-R1-1 a D-R1-5, D-R1-8 y D-R1-10; RC-D1 a RC-D17. Incluyen el presupuesto inicial de 10 000 referencias y 1 500 000 valores (RC-D9), la normal de origen con presencia (RC-D16) y el contador `values` (RC-D17) |
| Limitaciones | los lectores ≤ 1.2.2 pierden el almacén al guardar; un almacén por encima del presupuesto se rechaza; el presupuesto procede de una estación y de una sonda; el caso al 100 % de `maxEntryBytes` no se midió |
| Dependencias | ninguna para adoptar; requisito de los otros dos contratos |
| Verificación | `STATIC` del commit de contenido R4 (§8); revisión independiente final (§7); evidencia de medición de R3 (informe R3 §3) |
| Obligaciones de implementación | `MAINT-A-DXF0-RC-S0`: C-1 a C-3 y la parte actual de C-5. `MAINT-A-DXF0-RC`: RC-M primero; códec con contador `values`; presencia por grupo en el registro (A-IMP-12, sujeta a D-R4-1); preparación y confirmación en todas las rutas; Classic; escáneres; RC-1 a RC-26; **puertas de cierre** C-4 y C-5 en el presupuesto |

### 10.2 Exportación DXF de referencias + ADR 0045

| Elemento | Contenido |
|---|---|
| Contrato y revisión | [`cedg-reference-export/v1`](../../geocedg/specs/export/dxf-reference-export.md), revisión R4; [ADR 0045](../adr/0045-separate-dxf-export-of-imported-references.md), revisión R4 |
| Candidato | commit y árbol de contenido R4 (§8) |
| Enmiendas | grupo X: A-IMP-4, A-IMP-5, A-IMP-6, A-BS-1, A-EXP-1 a A-EXP-6, A-GEF-1, A-ADR-1, A-ADR-2 |
| Decisiones adoptadas | D-R1-5 a D-R1-7 y D-R1-10; RX-D1 a RX-D19, con RX-D18 y RX-D19 fijadas por el autor y RX-D19 no ampliada (R4). También: situaciones A, B y C del manifiesto; exportación solo de referencias; las dos lecturas informativas de la procedencia |
| Limitaciones | la conformidad AutoCAD se declara solo para AutoCAD 2026 (`ACADVER` 25.1) con ≤ 4 ulp por valor más estructura y geometría, y ezdxf 1.4.2 exacto, sobre los fixtures; los exponentes de tres cifras son `NOT_WRITABLE`; deudas separadas DT-1 y DT-2 |
| Dependencias | `R-c` adoptado; EXP tras el hito RC-M; nunca en paralelo con `MAINT-A3` |
| Verificación | `STATIC` del commit de contenido R4 (§8); revisión independiente final (§7) |
| Obligaciones de implementación | `MAINT-A-DXF0-EXP`: escritor `SPLINE` con comprobación de ida y vuelta; resolutor; preflight y manifiesto v4 (A, B, C); ruta del controlador de §9.1, con RX-15 (e) por el controlador real; normal de salida `0/0/1` nunca atribuida al origen; RX-1 a RX-6, RX-8 a RX-17 y RX-19; identidad de bytes de la exportación ordinaria; evidencia externa RX-16, RX-17 y RX-19 |

### 10.3 A-DXF1

| Elemento | Contenido |
|---|---|
| Contrato y revisión | [`dxf-spline-approximation/v1`](../../geocedg/specs/import/dxf-spline-approximation.md), revisión R4; sin ADR nuevo (ADR 0043 decisión 11; ADR 0045 registra las dos lecturas informativas) |
| Candidato | commit y árbol de contenido R4 (§8) |
| Enmiendas | grupo A: A-IMP-7, A-IMP-8, A-IMP-11, A-BS-2 |
| Decisiones adoptadas | D-R1-5, D-R1-9 y D-R1-10; AX-D1 a AX-D14 |
| Limitaciones | sin cota certificada en `MAINT-A-DXF1`, que queda para `MAINT-A-DXF1b`; medidas `SAMPLE_RESIDUAL` y `ESTIMATED` con sus grados; constantes pendientes de medición |
| Dependencias | `R-c` adoptado; el servicio, tras RC y DXF1-S0; DXF1b, tras DXF1 |
| Verificación | `STATIC` del commit de contenido R4 (§8); revisión independiente final (§7) |
| Obligaciones de implementación | `MAINT-A-DXF1-S0`: NUM-1 a NUM-10. `MAINT-A-DXF1`: fijar las constantes `bspline-param-lsq-constants/v1` como puerta de cierre; autocomprobación que relee también el bloque de procedencia; normalización de `-0.0`; sin historial de fallos; interfaz por lectura informativa; RX-7 y RX-18 |

## 11. Cierre documental conjunto propuesto

Tras la decisión del autor sobre el candidato R4 exacto, en una rama de cierre desde ese
candidato y en un único commit documental:

1. Los tres registros de §10, escritos como registros autorales con la fecha y las palabras del
   autor.
2. Estados de los tres contratos y de ADR 0044 y 0045 a aprobado o aceptado, con enlace a sus
   registros.
3. Aplicación literal de las enmiendas de los tres grupos, como secciones o reemplazos fechados
   que conservan la historia de aprobación, a:
   - `dxf-spline-reference.md`;
   - `bspline-2d.md`;
   - `dxf-curve-fidelity-and-approximation.md`;
   - `geometry-export-foundation.md`;
   - `native-document-identity.md`;
   - las líneas de estado de ADR 0014 y ADR 0043.
4. El registro aditivo del roadmap con el texto canónico A-RM-1 (enmiendas §5), con la fecha y
   el SHA rellenados.
5. El plan operativo marcado como planificación aprobada.
6. Los prompts candidatos con sus dependencias satisfechas, sin autorizar nada.

Después:

- `STATIC` sobre ese commit exacto y, si procede, un descendiente documental clasificado por
  `tools/agent/documentary-descendant.ps1`.
- **Publicación:** solo con una instrucción autoral que nombre el SHA. Sería un avance rápido
  sin forzar de `main` desde `a9ee803bf` (AGENTS §8.1). El delta sería solo documentación,
  especificaciones, ADR y prompts de tarea.

## 12. Ejecución de las cinco implementaciones

Los cinco [prompts candidatos](../architecture/pre_g9b_maint_a_n2_r1_candidate_prompts/) y la
[propuesta de ejecución coordinada](../architecture/pre_g9b_maint_a_n2_r1_coordinated_execution_proposal.md)
quedan listos para una instrucción autoral de ejecución sin otra fase de diseño.

| Incremento | Rama / worktree | Clase | Cierre |
|---|---|---|---|
| `MAINT-A-DXF0-RC-S0` | `phase/pre-g9b-maint-a-dxf0-rc-s0` | `BOUNDED_PHASE` | C-1 a C-3, parte actual de C-5 |
| `MAINT-A-DXF1-S0` | `phase/pre-g9b-maint-a-dxf1-s0` | `BOUNDED_PHASE` | NUM-1 a NUM-10 |
| `MAINT-A-DXF0-RC` | `phase/pre-g9b-maint-a-dxf0-rc` | `INTEGRATED_PHASE` | RC-1 a RC-26, A-IMP-12, C-4 y C-5 en el presupuesto |
| `MAINT-A-DXF0-EXP` | `phase/pre-g9b-maint-a-dxf0-exp` | `INTEGRATED_PHASE` + evidencia externa | RX (salvo RX-7 y RX-18), RX-15 (e), RX-16, RX-17 y RX-19 |
| `MAINT-A-DXF1` | `phase/pre-g9b-maint-a-dxf1` | `INTEGRATED_PHASE` | AP, NUM, RX-7, RX-18, constantes fijadas |

- Orden: RC-S0 y DXF1-S0 en paralelo; RC, cuyo primer commit es el hito RC-M; EXP tras RC-M y
  DXF1 tras RC y DXF1-S0, en paralelo; integración RC → EXP → DXF1.
- Puntos de sincronización P1 a P6 de la propuesta de ejecución.
- `MAINT-A-DXF1b`, `MAINT-A3` y `MAINT-A-R` quedan fuera.
- Ningún incremento se ejecuta ni se crea su worktree en R4.

## Evidencia

- Inspección de solo lectura de `DxfSplineReader`, `DxfSplineReference` y
  `GeoCeDGDxfExportController` en el árbol del candidato (idéntico a `a9ee803bf` en código de
  producto).
- Fuente de la sonda de R3 en
  `artifacts/agent/pre-g9b-maint-a-n2-r3-characterization/budget3m/probe-src/` (ignorado),
  método `values()`.
- Clasificación repetida del descendiente R3 (§5).
