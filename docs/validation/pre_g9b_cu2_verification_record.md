# PRE-G9B-CU2 — Registro de verificación del candidato técnico

| Campo | Valor |
|---|---|
| Carácter | Registro documental de la verificación de T13 del candidato técnico `CU2`. Lo añade un commit documental descendiente, que no modifica el candidato ni es un candidato nuevo |
| Estado | `CU2 IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`: `STATIC`, `INFRA_UNIT` y las tres `PHASE` son `ACCEPTED / COMPLETE`; de las dos `INTEGRATION` sobre el mismo candidato, la primera es `REJECTED_SEMANTIC` por una carrera preexistente de una prueba (§4.1) y la segunda `ACCEPTED / COMPLETE`, 19/19; cuatro filas de la matriz no tienen prueba (§2.3); decisiones D-CU2-T13-R1 a D-CU2-T13-R3 pendientes (§9); `selfApproved=false`; no se declara `PASS — AUTHOR APPROVED` y no se ejecuta el cierre autoral |
| Candidato | `39741a0b44850a32258c2c73370fdc0842b8c666`, árbol `0bfeb76da3ad3a470b9de61eec0650ea0895b3aa`, en la rama local `phase/pre-g9b-cu2-implementation` (sin publicar) |
| Base autorizada | `070af6f902cd0be291247e73efb5629ef2a39eba`, árbol `49a659d8b334aae3f69c4db000cc842204e9a85f` |
| Informe | [informe del candidato](pre_g9b_cu2_implementation_candidate_report.md) y su espejo [`pre-g9b-cu2-candidate-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu2-candidate-evidence.json) |

## 1. Identidad y delta

| Comprobación | Resultado |
|---|---|
| Commits | 19 commits desde la base, de T0 a T13; la lista con su papel está en el espejo |
| Delta base → candidato | 246 archivos: 70 nuevos y 176 modificados; 39 527 inserciones y 1 768 borrados |
| Código de producto | 108 archivos bajo `source/` fuera de `src/test`: 33 nuevos y 75 modificados, entre ellos las tres tablas `menu*.properties` y siete archivos de Desktop; 19 926 inserciones y 1 064 borrados |
| Pruebas | 99 archivos: 31 nuevos (30 clases con pruebas y el soporte `LocusV2TestBranches`) y 68 modificados; 14 153 inserciones y 460 borrados. No se elimina ninguna prueba |
| Límite upstream | `Assert-GeoCeDGUpstreamBoundary` (base `9b93256b`) correcto sobre el árbol de trabajo de T13: 1 160 archivos registrados en `docs/upstream/modified-files.yml` |
| Libro y gobernanza | `geocedg_book`, `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/prompts/` y los prompts canónicos sin cambios. Solo cambió el prompt de tarea de `CU2`, en T0 y por instrucción autoral |
| Finales de línea | los archivos de finales mixtos (roadmap y tablas `menu*.properties`) conservan sus terminadores, y sus líneas nuevas toman el de la línea anterior; `git diff --cached --check` limpio en cada commit |
| Árbol de trabajo e índice | limpios antes de cada ejecución de aceptación |
| Publicación | `main` = `origin/main` = `main` remoto = `48a0775e4f2f122f5e2614c62b5773777752ccd1`. El remoto tiene `phase/pre-g9b-cu2-preparation` en la base `070af6f9` y ninguna rama de implementación de `CU2` |

## 2. Evidencia previa al commit

### 2.1 Inventario de JUnit

El productor oficial `tools/agent/checks/gradle-test-evidence-producer.ps1` se ejecutó sobre el
árbol de trabajo del commit `4e394dc3` con los cambios de registro e inventario de T13.

| Selección | Modo | Resultado | Duración |
|---|---|---|---:|
| `discovery.shared` | descubrimiento en seco | `COMPLETED`, código 0; 724 archivos y 6 290 casos | 1,6 min |
| `discovery.desktop` | descubrimiento en seco | `COMPLETED`, código 0; 190 archivos y 2 231 casos | 1,5 min |
| `pre-g9b-cu2.shared` | ejecución | 21 clases, 192 pruebas, 0 fallos, 0 omitidas | 2,2 min |
| `pre-g9b-cu2.desktop` | ejecución | 9 clases, 46 pruebas, 0 fallos, 0 omitidas | 1,9 min |
| `final.shared` | ejecución | 724 archivos, 7 207 pruebas, 0 fallos, 10 omitidas | 3,8 min |
| `final.desktop` | ejecución | 189 archivos, 2 225 pruebas, 0 fallos, 1 omitida | 19,0 min |

Las omitidas son preexistentes y upstream: en Desktop,
`org.geogebra.CasValueExtractor.extractMockValues`; en shared, las mismas 10 pruebas
`org.geogebra.*` de las ejecuciones de `CU0` y `CU1`.

La primera ejecución de `final.desktop` falló en
`G9U0LocalizationHelpTest.l02RichStatusAndDiagnosticLabelsAreLocalized`: el diagnóstico
`EXISTENCE_NOT_CERTIFIED` de la resolución previa a T12 no tenía etiquetas. Se corrigió en
`4e394dc3`, y las seis selecciones se volvieron a producir sobre el árbol corregido. Después,
la selección `pre-g9b-cu2.shared` se amplió con las 8 clases de la biblioteca exacta de T4 y su
evidencia se volvió a producir.

El actualizador oficial `tools/agent/update-verification-junit-inventory.ps1` se ejecutó en la
sesión con las dos evidencias de descubrimiento y las cuatro de ejecución:

- `discovery.shared`: 6 098 → 6 290, y `final.shared`: 7 015 → 7 207 (+192: las 21 clases nuevas de shared);
- `discovery.desktop`: 2 185 → 2 231, y `final.desktop`: 2 179 → 2 225 (+46: las 9 clases nuevas de Desktop);
- `pre-g9b-cu2.shared`: 192, y `pre-g9b-cu2.desktop`: 46 (selecciones nuevas), con el commit T0
  `9bfafbf0` como `input_identities`;
- las demás selecciones no cambian.

La huella `junit_inventory` del registro cambia de
`4e1058513384f2de7c107c1e91224b4eab3ae351b750b56446d7bea56cdb07f8` a
`6ba8034547fc9184d7e52fb5764e78c15f9860a9b7a551df6a38795cbdab5c2f`. La anterior se reprodujo
antes desde el blob de `HEAD` con `Get-VerificationCanonicalTextSha256`, y la huella canónica es
la misma con finales LF o CRLF.

### 2.2 Ejecuciones de desarrollo

Sobre el índice preparado, ligadas por el verificador a la identidad de `HEAD` (`4e394dc3`). Son
evidencia de contenido, no de aceptación.

| Perfil | `run_id` | Resultado |
|---|---|---|
| `STATIC` | `verification-aa2ef601c1534eb4a9500c2d3039e991` | `ACCEPTED / COMPLETE` |
| `INFRA_UNIT` | `verification-827c8f612b76423083b8e826a1dc910b` | `REJECTED_VERIFICATION_CORE / COMPLETE`, 20/22 |
| `PHASE -Phase PRE-G9B-CU2` | `verification-f2d7429cee2b48d79bf597fa8b35e567` | `ACCEPTED / COMPLETE` |
| `PHASE -Phase PRE-G9B-CU1` | `verification-45c51d00aae74b6e888a024503aa8262` | `ACCEPTED / COMPLETE` |
| `PHASE -Phase PRE-G9B-CU0` | `verification-4b5630276a564dda900409f4d2ce596b` | `ACCEPTED / COMPLETE` |
| `STATIC` (2.ª) | `verification-38f885c600614ad78b9ab5d6c2f1dfe0` | `ACCEPTED / COMPLETE` |
| `INFRA_UNIT` (2.ª) | `verification-f0104f24a8c8460f8089cf5e73218b25` | `ACCEPTED / COMPLETE`, 22/22 |

La primera `INFRA_UNIT` violó dos hojas:

- `infra.static-semantic`: «hash differs geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json».
  T11 había cambiado el fixture `scalar-locus` de la matriz GGBScript sin actualizar su huella en
  el catálogo de contratos estáticos ni registrarlo como conformidad. La huella canónica de la base
  era `f167fa71…`, la fijada, y la del árbol es `ee99a54f…`;
- `infra.final-coverage`: «Selection inventory count changed.», porque la prueba fija los
  recuentos de selecciones y de fases, como en `CU0` y `CU1`.

Correcciones en el commit del candidato: las fijaciones de cobertura en la forma de `CU1`, la
huella del contrato estático, la huella `static_contracts` del registro (`a670be95…d8ec` →
`286d787d…5c6e`) y la §8 del registro de conformidades de T11. Después, la segunda `STATIC` y la
segunda `INFRA_UNIT` aceptan.

Checkstyle: los últimos cambios Java del candidato son de `6382adfe` y `7f9eca77`. En sus
ejecuciones de `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`,
`:desktop:desktop:checkstyleMain` y `:desktop:desktop:checkstyleTest` no hubo avisos en líneas
del candidato. Los 4 avisos de pruebas Desktop están en líneas anteriores a `CU2`
(`636a0070`, `2a71133a` y `da1a0952`).

### 2.3 Auditoría de la matriz de validación

Se buscaron, en todas las fuentes de prueba, las citas de las 133 filas de la
[matriz de `CU2`](pre_g9b_cu2_validation_matrix.md), con los rangos «V-X-1 a V-X-n» expandidos.
Las filas de proceso (V-CU0-1 a V-CU0-3, V-CU1-1, V-PROD-1 y V-PROD-2) se comprueban con la
verificación de este registro y con los registros de T11 y T12; V-E2-2 está en las referencias
`preserve` de `PreG9BCu0LocusReferenceTest` y `PreG9BCu0CompositionCharacterizationTest`; y V-DXF-4
es informativa (§5). Cuatro filas
obligatorias (tipo F) no tienen prueba que las cite:

| Fila | Requisito | Situación |
|---|---|---|
| V-POINT-3 | Transición entre punto y arco con transiciones de linaje tipadas de aparición y de cambio de dimensión | no implementada: el linaje registra el paso de punto a arco como una desaparición y una aparición (`LocusComponentLineage2D`), y el vocabulario de `locus-v2-semantics.md` no tiene una transición de cambio de dimensión. Decisión D-CU2-T13-R1 (§9) |
| V-REV-EXACT-5 | Contraste de cada estado de V-REV-EXACT-1 a V-REV-EXACT-4 con la misma construcción recién creada | sin prueba propia. `PreG9BCu2AuthorModelCertificationTest.undoAndRedoRestoreTheCertificates` (V-CRIT-8) compara certificados y estado de selección con documentos recién abiertos, no la huella, el corte ni el programa capturado de esos estados |
| V-COINC-6 | Conductor que empieza sin intersección, en las dos variantes, guardadas y reabiertas | sin prueba propia. `PreG9BCu2AuthorModelCertificationTest.theLociArePublishedWhileTheSavedDriverIsInTheGap` (V-PUB-1) cubre el caso análogo del modelo del autor |
| V-COINC-9 | En toda muestra fuera de las bandas `UNRESOLVED`, la salida operativa es la raíz que designa la selección certificada, frente a la referencia de precisión ampliada | sin prueba propia. V-COINC-7 compara la definición de las salidas del vivo y del corte, no la raíz designada frente a la referencia |

Las tres últimas solo necesitan pruebas; ninguna exige cambiar un contrato. Decisión D-CU2-T13-R2 (§9).

## 3. Bancos y referencias sobre el candidato

El arnés de `CU0` (`tools/benchmark/pre-g9b-cu/run-harness.ps1`) se ejecutó sobre el commit del
candidato con el árbol de trabajo limpio (`product_sources_modified = false`). Las salidas están
en `artifacts/benchmarks/pre-g9b-cu/`, que Git ignora: las de antes en
`cu2-before-{certification,tessellation}-cfa30bad` (paso T1) y las de después en
`cu2-after-{certification,tessellation,references}-39741a0b`. Los contadores son la medida
reproducible; los tiempos son informativos (GC-7).

**Banco `certification`** (modelo del autor; contadores sumados sobre sus siete loci):

| Paso | Antes (`cfa30bad`) | Después (`39741a0b`) | Puerta |
|---|---|---|---|
| Carga | 2 revisiones y 2 certificados por locus; `e_2`, 1 certificado y 134 anidados | 1 revisión y 1 certificado por locus, con 65 a 67 cajas; `e_2`, 1 anidado; 0 lecturas métricas durante la certificación | GC-2, GC-4, GC-6 |
| Mover solo el conductor (cinco pasos de GC-1) | 3 o 4 cálculos de certificado por paso | 0 cálculos de certificado; solo recálculos equivalentes y huellas | GC-1 |
| excCyl 2,9 ↔ 3,0, al lado de penetración, un ulp al de mordedura y vuelta | 4 certificados y 4 revisiones por paso | 4 certificados y 4 revisiones por paso (`o`, `v`, `w` y `a_1`), con 260 a 300 cajas; 8 transiciones certificadas al entrar en la mordedura | GC-2, GC-4 |
| rbCone 5 ↔ 5,1 | 7 certificados y 2 anidados por paso | 7 certificados y 1 anidado por paso, con 459 cajas y 65 anidadas | GC-2 |
| Pintar la vista del autor | 44 certificados anidados | 0 certificados; 45 huellas anidadas | GC-3 |
| Consultas de punto de `e_2` | 6 certificados anidados | 0 certificados; 6 huellas anidadas | GC-3 |
| Longitud total de `o_1` | ningún certificado | ningún certificado | GC-3 |

Tiempos: carga, 3 355 → 4 198 ms; pasos de GC-2, de 23–79 ms a 32–92 ms.

**Banco `tessellation`** (política `locus-render-policy/v2` después):

| Vista | Vértices antes | Vértices después | Pintado antes → después |
|---|---:|---:|---|
| zoom 1 | 124 | 345 | 10,4 → 17,0 ms |
| zoom 2 | 166 | 347 | 5,0 → 5,5 ms |
| zoom 4 | 229 | 367 | 2,0 → 2,2 ms |

Con el candidato por defecto (profundidad mínima 4, giro máximo 10°): en el modelo del autor, como
mucho 74 vértices por locus y un giro máximo fuera de exenciones de 9,43°; en la mordedura de
excCyl 3,85, 55 vértices y 9,61°. Los 660 candidatos del banco tienen fidelidad `COMPLETE`. El
presupuesto es de 4 096 vértices (GC-5).

**Referencias** (`cu2-after-references-39741a0b`): idénticas hoja a hoja a la ejecución de T12
del [registro de nueva línea base](pre_g9b_cu2_t12_new_baseline_record.md) (SHA-256
`4ceb9f7d…d54`): 661 hojas `preserve` y 346 `pins`. Solo cambia la identidad del generador. Las
cifras de ese registro quedan ligadas al candidato; la línea base de `CU0` no cambia.

## 4. Verificación de aceptación sobre el candidato

La clase de verificación autorizada es `INTEGRATED_PHASE` (D-CU2-12). Las ejecuciones fueron
secuenciales, desde la raíz del repositorio, con el candidato como `HEAD` y el árbol de trabajo y
el índice limpios:

```text
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu2-static-39741a0b
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu2-infra-unit-39741a0b
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2 -LogDirectory artifacts\agent\pre-g9b-cu2-phase-pre-g9b-cu2-39741a0b
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1 -LogDirectory artifacts\agent\pre-g9b-cu2-phase-pre-g9b-cu1-39741a0b
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu2-phase-pre-g9b-cu0-39741a0b
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu2-integration-39741a0b
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu2-run2-integration-39741a0b
```

La consola de cada ejecución se redirigió al directorio temporal de la sesión, fuera del
repositorio. Todas terminaron con código de salida 0, salvo la primera `INTEGRATION`, con
código 1. Los `result_hash` se autenticaron con `Read-VerificationJson` y
`Get-VerificationResultHash`.

| Perfil | `run_id` | `result_hash` | Plan | Duración | Resultado |
|---|---|---|---|---:|---|
| `STATIC` | `verification-5aec340a96ea459a94852ef1319fbe7b` | `762b122ab0228fcee3b330fe2fe70ca744d7421d3fe7b6fa8e435db0665b9a83` | `25dfacc8…cc52` | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo diagnósticos permanentes |
| `INFRA_UNIT` | `verification-4a3db31225c944788661fb24ba18792a` | `2e150ac2a9eb52abfe2133fb390fb97aedb4f63c23ec2fd61aea2684d8e1e78f` | `830887b1…ce1d` | 2,2 min | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU2` | `verification-d2957d6ec5f14599adc817e8c65666a9` | `7ad1e9ccb0c943d779662004e510408edc4d543b18a551b180984d39b7b8a6db` | `b9dc6d9e…a930` | 6,0 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu2.shared`: 21 clases y 192 pruebas; `pre-g9b-cu2.desktop`: 9 clases y 46 pruebas; 0 fallos, 0 omitidas |
| `PHASE -Phase PRE-G9B-CU1` | `verification-898e709c055948cf9847b858556057d5` | `b37a5b9679de17992a437afa0ad2a8f5c464f4b704630483f62484f4a3ed3755` | `b0e4f130…4519` | 5,6 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu1.shared`: 6 clases y 57 pruebas; `pre-g9b-cu1.desktop`: 4 clases y 20 pruebas; 0 fallos |
| `PHASE -Phase PRE-G9B-CU0` | `verification-b9f68b8246814024890195be3a9d4670` | `964c55c3f571e7d2dcb85a34f4632bfd3d07ddb3fd222c6d44026d09605620f6` | `43e3e97e…9173` | 2,6 min | `ACCEPTED / COMPLETE`, 2/2; `pre-g9b-cu0.desktop`: 6 clases y 21 pruebas, con las fijaciones invertidas en T12; 0 fallos |
| `INTEGRATION` (1.ª) | `verification-7660aed90fdd45719122441e580150fb` | `223e7f8ca73c5729ae1e5f95efba37099912d7875e60889f72873dcade48ebba` | `4502c4bd…7aea` | 50,5 min | `REJECTED_SEMANTIC / COMPLETE`, 18/19: un caso de `junit.desktop.final` (§4.1) |
| `INTEGRATION` (2.ª) | `verification-a22f468137434ef283dcfaee88f102f0` | `9eb4b9fd30c4f20df134d060412d851852f1c4e4770ab780aa4ba28f9ceb9f87` | `4502c4bd…7aea` | 46,8 min | `ACCEPTED / COMPLETE`, 19/19 |

Los planes de `STATIC`, `INFRA_UNIT`, `PHASE -Phase PRE-G9B-CU1`, `PHASE -Phase PRE-G9B-CU0` e
`INTEGRATION` son los mismos de las ejecuciones de `CU1`: las selecciones de `CU0` y `CU1`
conservan sus identidades. El plan de `PHASE -Phase PRE-G9B-CU2` es nuevo.

En la segunda `INTEGRATION` pasaron:

- `junit.shared.final`: 724 archivos y 7 207 pruebas, con 10 omitidas upstream;
- `junit.desktop.final`: 189 archivos y 2 225 pruebas, con 1 omitida upstream;
- `junit.desktop.g9u1-isolated`: 2 clases y 5 pruebas;
- `junit.desktop.revision3-diagnostic`: 1 prueba.

La primera dio los mismos recuentos, salvo el caso fallido de §4.1. Los diagnósticos de `STATIC`
y de las dos `INTEGRATION` son `diagnostic.governance` = `DIAGNOSTIC_FINDING` y
`diagnostic.historical-consistency` = `DIAGNOSTIC_UNAVAILABLE`. Son las notas permanentes del
protocolo de recuperación de la infraestructura de verificación, iguales que en `CU0` y `CU1`.
Los demás diagnósticos de `INTEGRATION` son `DIAGNOSTIC_CLEAR`: los cuatro de Checkstyle,
`diagnostic.style`, `diagnostic.documentation`, `junit.desktop.revision3-diagnostic` y
`repository.whitespace-diagnostic`.

### 4.1 Primera `INTEGRATION`: rechazo por una carrera preexistente

La única hoja violada fue `junit.desktop.final.semantic`, con un caso fallido de 2 225:
`PreG9BR5BR2InputHelpAfterReopenTest.reopenAcrossLanguagesKeepsIdentityAndUsesTheCurrentSyntaxBody`,
con «NullPointerException: Cannot read field "width" because "d" is null» en
`Component.getMaximumSize`, llamado desde `GeoCeDGToolbarContainer.applyNativeToolPresentation`
mientras `AppD.buildApplicationPanel` construía la barra de herramientas.

- **Mecanismo.**
  - La prueba construye la interfaz fuera del hilo de eventos:
    `PreG9BR5BR1RuntimeLanguageSwitchTest.started` llama a `app.buildApplicationPanel()` en el hilo
    de JUnit.
  - El método fallido crea cuatro aplicaciones seguidas (dos documentos y dos idiomas) y carga cada
    documento en el hilo de eventos.
  - Los tamaños de un componente de Swing no son seguros entre hilos: `Component.getMaximumSize`
    lee `maxSizeSet` y después `maxSize`. Una invalidación o un cambio de tamaño en el hilo de
    eventos entre las dos lecturas deja `maxSizeSet` verdadero y `maxSize` nulo.
- **No procede de `CU2`.** El candidato no modifica ninguna clase de la traza. Su código de
  producto no añade trabajo al hilo de eventos (`invokeLater`, `SwingUtilities`, `EventQueue` o
  temporizadores) ni cambios de tamaño de Swing. Las clases que se ejecutaron justo antes son
  pruebas anteriores de R5-A y R5-B.
- **Precedente.** La misma traza rechazó el 2026-10-09 la `INTEGRATION`
  `verification-1247ac65be8f4c35a6572b00fd447a31`, sobre `27388d60` (antecesor de la base), en
  otra prueba (`PreG9BR6PlusCDrawingScaleTest`). Su informe la atribuyó a trabajo encolado en el
  hilo de eventos mientras el hilo de prueba reconstruía la barra.
- **Historial.** En las otras 45 ejecuciones conservadas en `artifacts/agent/` que ejecutan esta
  clase no consta ningún fallo. En este candidato pasó en el `final.desktop` previo al commit
  (§2.1) y en la segunda `INTEGRATION`.

Conclusión: es una carrera de la prueba, anterior a `CU2`, cuya probabilidad depende del trabajo
pendiente en el hilo de eventos, y no se ha identificado ninguna vía por la que el candidato la
cause. Se registra como observación `OBS-CU2-OFF-EDT-TOOLBAR-RACE`, y su tratamiento es la
decisión D-CU2-T13-R3 (§9). La segunda `INTEGRATION` se lanzó sobre el mismo candidato para
caracterizarla, sin cambiar nada; no se ha lanzado una tercera.

## 5. Comprobación externa V-DXF-4 (informativa)

Un sondeo del directorio temporal de la sesión, añadido al conjunto de pruebas de Desktop con un
`--init-script` y retirado después del árbol de compilación, escribió sobre el candidato las 11
exportaciones DXF estrictas de V-DXF-1:

- `o`, `v`, `w`, `a_1`, `e_1`, `o_1` y `e_2` en el estado guardado;
- `o`, `v`, `w` y `a_1` en la mordedura de excCyl 3,7.

AutoCAD 2026 Core Console (`accoreconsole.exe`) abrió cada archivo con un guion `.scr`. Ese guion
cuenta las entidades con AutoLISP y ejecuta `AUDIT` sin corregir. En los 11 archivos:

- no hubo errores de carga;
- `AUDIT` dio «Total errors found 0 fixed 0»;
- AutoCAD no modificó ningún archivo;
- hay una `LWPOLYLINE` por archivo, salvo en `e_2`, con tres: sus tres partes abiertas con los
  dos cortes declarados.

Es una comprobación externa e informativa, no una puerta.

Escaneo de autoridad de G9X1 (V-PROD-2): `Assert-GeoCeDGDxfSourceAuthority` sobre los 46 caminos
del perímetro de `g9x1-evidence.json` da las mismas tres faltas preexistentes en la base y en el
candidato. Son el roadmap y `modified-files.yml`, que nombran `LocusRenderCache`, y
`AppGeoCeDG.java`, con `EuclidianView`. No empeora.

## 6. Lo que no se ha ejecutado

- `FINAL`: la clase autorizada no lo exige y el plan lo reserva para `CU10`. Por eso no hay
  recibo de verificación.
- Prueba manual del autor: corresponde a la revisión autoral.
- Escenario ventaneado del arnés de `CU0`: no se repitió; los bancos de §3 miden certificación y
  teselación con contadores deterministas.

## 7. Limitaciones

Son las de §10 del informe del candidato. Además:

1. La evidencia del inventario (§2.1) y las ejecuciones de desarrollo (§2.2) se produjeron antes
   del commit del candidato, sobre el árbol de trabajo. Las ejecuciones de aceptación (§4) y los
   bancos de §3 son los únicos ligados a la identidad del candidato.
2. Las cuatro filas de la matriz de §2.3 no están verificadas por pruebas.
3. La aceptación de `INTEGRATION` descansa en la segunda ejecución sobre el mismo candidato; la
   primera se rechazó por la carrera de §4.1.
4. El sondeo de V-DXF-4 y el escaneo de autoridad de G9X1 son evidencia de diagnóstico ejecutada
   desde el directorio temporal de la sesión, no pruebas registradas.
5. Los tiempos son de una sola estación y no constituyen puertas.

## 8. Contenido de este commit documental

Este commit:

- añade este registro;
- actualiza a la versión 2 el [registro de transferencia científica E1](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md):
  estado y evidencia de las filas y fichas de los métodos de `CU2`, como exige su §8;
- enlaza este registro desde el informe del candidato;
- registra el resultado de la verificación en el roadmap.

No modifica código, pruebas, registro, inventario ni evidencias.

## 9. Decisiones pendientes y estado de autorización

La verificación añade tres decisiones a las de §11 del informe del candidato:

| Decisión | Pregunta | Opciones |
|---|---|---|
| D-CU2-T13-R1 | V-POINT-3: el cambio de dimensión entre punto y arco que exige `locus-v2-existence-components.md` §15.4 no tiene una transición tipada en `locus-v2-semantics.md` | (a) autorizar una enmienda mínima de `locus-v2-semantics.md`: una transición tipada de cambio de dimensión, con relación explícita entre la componente puntual y el arco que acotan las dos raíces simples de su raíz par; implementarla en `LocusComponentLineage2D` y en la resolución de selectores, con la prueba de V-POINT-3, como descendiente correctivo; (b) precisar §15.4 para expresar el cambio de dimensión con las transiciones existentes y una relación explícita, sin tipo nuevo; (c) registrar V-POINT-3 como limitación de `CU2` con propietario `CU3` |
| D-CU2-T13-R2 | V-REV-EXACT-5, V-COINC-6 y V-COINC-9 no tienen prueba | (a) añadir las tres pruebas como descendiente correctivo, sin cambiar contratos, corregir lo que revelen y repetir la verificación que exija su clase; (b) registrarlas como limitación con propietario |
| D-CU2-T13-R3 | Tratamiento de `OBS-CU2-OFF-EDT-TOOLBAR-RACE`, que rechazó la primera `INTEGRATION` | (a) aceptar la verificación con la segunda `INTEGRATION`, aceptada, y la primera registrada como rechazo por la carrera preexistente; (b) autorizar una corrección mínima solo de prueba, fuera del alcance de `CU2`: que `PreG9BR5BR1RuntimeLanguageSwitchTest.started` construya la aplicación y su panel en el hilo de eventos, sin cambiar aserciones, en el mismo descendiente correctivo de R1 y R2; (c) registrar la observación con un propietario de mantenimiento de la interfaz, sin corrección |

Propuesta del agente: R1 (a), porque §15.4 ya lo exige y el tipo no se persiste; R2 (a); y R3 (b)
si se autoriza ese descendiente, o (a) con (c) si no. Las tres pueden ir en un único descendiente
correctivo, que repetiría la verificación de su clase.

`CU2` sigue siendo `CU2 IMPLEMENTATION CANDIDATE`, pendiente de estas decisiones y de la revisión
autoral. No se publica nada, no cambian `main`, las etiquetas, las releases ni `geocedg_book`, y
no se ejecuta el cierre autoral.
