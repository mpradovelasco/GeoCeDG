# PRE-G9B-CU1 — Registro de verificación del candidato técnico

| Campo | Valor |
|---|---|
| Carácter | Registro documental de la verificación del candidato técnico `CU1`. Lo añade un commit documental descendiente, que no modifica el candidato ni es un candidato nuevo |
| Estado | `CU1 TECHNICAL CANDIDATE — VERIFICATION INCOMPLETE — PENDING AUTHOR DECISION AND REVIEW`: `STATIC`, `INFRA_UNIT` y los dos `PHASE` son `ACCEPTED / COMPLETE`; las dos ejecuciones de `INTEGRATION` son `REJECTED_SEMANTIC` por una prueba inestable preexistente (§4.1); `selfApproved=false`; no se declara `PASS — AUTHOR APPROVED`; `CU2`–`CU10` no autorizados |
| Candidato | `8ee196679e0a6ab101eee0a4e60682efe3f11be0`, árbol `f3a895843cfc5c7e0ab124b544e4ff845bcef08f`, en la rama local `phase/pre-g9b-cu1-implementation` (sin publicar) |
| Especificación (S0) | `435ed78c547abf12642561c3b56a209527be535b`, árbol `f7b60f4b45a1f47dd970bcdcd31f1895849caeab`: padre directo del candidato |
| Base autorizada | `e66ae4c06007f9aba47436a0c03f36608f0f4fd5`, árbol `c90ab8fb61648f231cb383b900af6b4b1e622f8f`: padre directo de S0 |
| Informe | [informe del candidato](pre_g9b_cu1_implementation_candidate_report.md) |

## 1. Identidad y delta

| Comprobación | Resultado |
|---|---|
| Delta S0 → candidato | 62 archivos: 17 nuevos y 45 modificados; 6 574 inserciones y 161 borrados |
| Delta base → candidato | 65 archivos, S0 incluido; 6 976 inserciones y 184 borrados |
| Código de producto | 41 archivos bajo `source/` fuera de `src/test`: 12 fuentes nuevas y 26 modificadas en `source/shared/common`, y las tres tablas `menu*.properties` (4 claves cada una); 4 244 inserciones y 96 borrados |
| Pruebas | 4 clases nuevas, 7 clases modificadas y la fijación `pre-g9b-r5-b-base-fingerprints.json` |
| Límite upstream | `Assert-GeoCeDGUpstreamBoundary` (base `9b93256b`) correcto sobre el árbol de trabajo final: 1 094 archivos registrados en `docs/upstream/modified-files.yml` |
| Libro, ADR, prompts, `AGENTS.md`, `CLAUDE.md` | sin cambios. Después de S0, la única especificación modificada es `locus-v2-metrics.md` §25.12, texto del candidato sujeto a la decisión D-CU1-R2 |
| Finales de línea | 54 archivos normalizados a LF. Los 8 archivos que ya tenían finales mixtos en la base (roadmap, manifiesto upstream, `menu*.properties`, `GeoLocusV2.java` y los dos resolvedores de intersección) conservan los terminadores originales; solo sus líneas nuevas o editadas usan LF. `git diff --cached --check` limpio |
| Árbol de trabajo e índice | limpios antes y después de cada ejecución de aceptación |
| Publicación | `main` = `origin/main` = remoto `main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1`; el remoto solo tiene la rama de revisión de `CU0` (`d684e740`); ninguna rama de `CU1` existe en el remoto |

## 2. Evidencia previa al commit

### 2.1 Inventario de JUnit

El productor oficial `tools/agent/checks/gradle-test-evidence-producer.ps1` se ejecutó sobre el
árbol de trabajo, con las fuentes de producto y de prueba del candidato.

| Selección | Modo | Resultado | Duración |
|---|---|---|---:|
| `discovery.shared` | descubrimiento en seco | `COMPLETED`, código 0; 6 098 identidades | 2,1 min |
| `discovery.desktop` | descubrimiento en seco | `COMPLETED`, código 0; 2 185 identidades | 1,5 min |
| `pre-g9b-cu1.shared` | ejecución | 6 clases, 57 pruebas, 0 fallos, 0 omitidas | 1,2 min |
| `pre-g9b-cu1.desktop` | ejecución | 4 clases, 20 pruebas, 0 fallos, 0 omitidas | 1,9 min |
| `final.shared` | ejecución | 703 clases, 7 015 pruebas, 0 fallos, 10 omitidas | 2,9 min |
| `final.desktop` | ejecución | 180 clases, 2 179 pruebas, 0 fallos, 1 omitida | 14,9 min |

Las omitidas son preexistentes y upstream: en Desktop,
`org.geogebra.CasValueExtractor.extractMockValues`, la misma que en `CU0`; en shared, las mismas
10 pruebas `org.geogebra.*` que en el `INTEGRATION` de `CU0`.

El actualizador oficial `tools/agent/update-verification-junit-inventory.ps1` se ejecutó en la
sesión con las dos evidencias de descubrimiento y las cuatro de ejecución. Recuentos:

- `discovery.shared`: 6 075 → 6 098; `final.shared`: 6 992 → 7 015 (las 23 pruebas nuevas de shared);
- `discovery.desktop`: 2 180 → 2 185; `final.desktop`: 2 174 → 2 179 (las 5 pruebas nuevas de Desktop);
- `pre-g9b-cu1.shared`: 57 y `pre-g9b-cu1.desktop`: 20 (selecciones nuevas);
- `input_identities` de las selecciones nuevas: el commit S0 `435ed78c`, que era `HEAD` al registrarlas.

La huella `junit_inventory` del registro cambia de
`963f5c9cce4a9e9c4e6501d97372e8b47d3e68ab7a0bb0276bf3dabc900bd507` a
`4e1058513384f2de7c107c1e91224b4eab3ae351b750b56446d7bea56cdb07f8`. La huella anterior se
reprodujo antes desde el blob de `HEAD` con `Get-VerificationCanonicalTextSha256`.

Después de los productores solo hubo cambios sin efecto en las identidades de las pruebas:
la supresión de un parámetro sin uso y de dos constantes sin uso, comentarios en ASCII
(§2.2), la restauración de los terminadores originales en tres archivos de finales mixtos, el
orden de tres aserciones de `G9U0MetricPositionTest.m04` y dos variables locales `final` en las
pruebas nuevas. La selección `pre-g9b-cu1.shared` se volvió a ejecutar después (57 pruebas, 0
fallos), y las ejecuciones de aceptación de §4 ejecutan de nuevo todas las selecciones sobre el
candidato.

### 2.2 Ejecuciones de desarrollo

- Checkstyle (`:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`,
  `:desktop:desktop:checkstyleTest`): sin avisos en los archivos del candidato. Antes del commit
  se corrigieron 36 avisos `UnicodeRange` (caracteres no ASCII en comentarios de las fuentes de
  producto) y 4 avisos `VariableDeclarationUsageDistance` de las pruebas. Los 4 avisos de las
  pruebas Desktop están en archivos que el candidato no modifica.
- `INFRA_UNIT` sobre el índice preparado: `verification-e762f39d2fd04f63ae2637224694353f`,
  `ACCEPTED / COMPLETE`, 22/22 y plan `830887b1…ce1d`. El verificador la liga a la identidad de
  `HEAD` (S0). Es evidencia de contenido, no de aceptación del candidato.

## 3. Banco y referencias sobre el candidato

El arnés de `CU0` (`tools/benchmark/pre-g9b-cu/run-harness.ps1`) se ejecutó sobre el commit del
candidato, con el árbol de trabajo limpio (`product_sources_modified_relative_to_commit = false`).
Las salidas están en `artifacts/benchmarks/pre-g9b-cu/`, que Git ignora.

**Banco `composition`** (`cu1-composition-run2`): todos los contadores de trabajo coinciden con
los de `cu1-composition-run1`, el banco del árbol de trabajo citado en §6 del informe del
candidato; solo cambian los tiempos. Las cifras de G1–G8 del informe quedan ligadas al
candidato.

**Referencias** (`cu1-references-run1`) frente a la línea base de `CU0`
(`geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json`, sin cambios):

| Grupo | Resultado |
|---|---|
| `preserve` | 661 hojas: 621 idénticas bit a bit, incluidas todas las muestras de los lugares, los extremos de rama y los contactos. Las 40 restantes son valores derivados de longitudes métricas (`d_2`, las abscisas de `Q_1` y `R_1`, que se definen a partir de `d_2`, el perfil de longitud orientada, la longitud total de `o_1`, las muestras de longitud de `e_2` y su distancia de costura). La mayor diferencia es 1,084·10⁻⁷, en la longitud total de `o_1`, por debajo de la tolerancia de longitud 10⁻⁶ con la que las pruebas de `CU0` comparan esos valores |
| `pins` | H03 y B10.1 invertidos según el contrato (§7 del informe); H10/H11 definidos (§8 del informe); los valores de B10.2, propiedad de `CU2`, se desplazan 1,08·10⁻⁷ con la longitud de `o_1`, sin cambio de semántica |

| Valor | `CU0` | `CU1` | Referencia independiente |
|---|---:|---:|---:|
| Longitud total de `o_1` | 13,2364580874242 | 13,236458195804072 | 13,236458195821166 |
| `d_2` en el estado del modelo | 5,026489096595441 | 5,026489133092055 | — |

La referencia independiente es la suma de cuerdas que la línea base de `CU0` ya registraba
(`o_1_total_length_independent`). La diferencia con ella pasa de 1,08·10⁻⁷ en `CU0` a
1,7·10⁻¹¹ en `CU1`. Ningún valor `preserve` exige una nueva línea base; la línea base de `CU0`
no se modifica.

## 4. Verificación de aceptación sobre el candidato

La clase de verificación autorizada es `INTEGRATED_PHASE`: `STATIC`, `INFRA_UNIT`,
`PHASE -Phase PRE-G9B-CU1`, `PHASE -Phase PRE-G9B-CU0` e `INTEGRATION` sobre el candidato
congelado. Las ejecuciones fueron secuenciales, desde la raíz del repositorio y con el candidato
como `HEAD`:

```text
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu1-static-8ee19667
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu1-infra-unit-8ee19667
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1 -LogDirectory artifacts\agent\pre-g9b-cu1-phase-pre-g9b-cu1-8ee19667
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu1-phase-pre-g9b-cu0-8ee19667
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu1-integration-8ee19667
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu1-run2-integration-8ee19667
```

La consola de cada ejecución se redirigió al directorio temporal de la sesión, fuera del
repositorio. `STATIC`, `INFRA_UNIT` y los dos `PHASE` terminaron con código de salida 0; las dos
ejecuciones de `INTEGRATION`, con código 1. Los `result_hash` se autenticaron con
`Read-VerificationJson` y `Get-VerificationResultHash`.

| Perfil | `run_id` | `result_hash` | Plan | Duración | Resultado |
|---|---|---|---|---:|---|
| `STATIC` | `verification-543f22815c8b4127840d317ea73ae604` | `a659ed20ef935cd7d598b6f29b56795d4f18eb1f2334a997059537a69006ef7c` | `25dfacc8…cc52` | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo diagnósticos permanentes |
| `INFRA_UNIT` | `verification-71d48414f4354df19c63a14b1aaa5e94` | `8f8ef5a91a0f8ab18e7ed2007549eeabcaa798489e18352346b5c51324e3a548` | `830887b1…ce1d` | 2,1 min | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU1` | `verification-e46571eb1a7b452d9361774521cea852` | `2486a1cff2fa1d2aa8a2f814e20be69b412dd58772a8b45a20e2bcdf20fadc93` | `b0e4f130…4519` | 5,2 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu1.shared`: 6 clases y 57 pruebas; `pre-g9b-cu1.desktop`: 4 clases y 20 pruebas; 0 fallos, 0 omitidas |
| `PHASE -Phase PRE-G9B-CU0` | `verification-7a70805c68e941c593515e1c1513094f` | `5c1da640a174daf12852a08ba28683455e60a8b537886398f5593277a45ca3c2` | `43e3e97e…9173` | 2,4 min | `ACCEPTED / COMPLETE`, 2/2; `pre-g9b-cu0.desktop`: 6 clases y 21 pruebas, con los pines B10.1, H03 y H10/H11 invertidos; 0 fallos, 0 omitidas |
| `INTEGRATION` (1.ª) | `verification-eb1ff68a9d5d410ca74b887d2cfebe00` | `9b6a821580b2773fd75217fb8fab3aaea1a799ad75b26bf2f017d5a4a03aa115` | `4502c4bd…7aea` | 41,1 min | `REJECTED_SEMANTIC / COMPLETE`, 18/19: `junit.desktop.final.semantic` violada por un caso (§4.1) |
| `INTEGRATION` (2.ª) | `verification-c126f174db774b5a9c7ddca1e4382689` | `7f9a12f44ca5e5ff1b3241450b241fcd2a7644e53e9e5c9d002eed0bbbe8c799` | `4502c4bd…7aea` | 32,6 min | `REJECTED_SEMANTIC / COMPLETE`, 18/19: el mismo caso |

El plan de `PHASE -Phase PRE-G9B-CU0` es el mismo `43e3e97e…9173` de la ejecución de `CU0`
sobre `636a0070`: la selección conserva sus 21 identidades. El plan de `INTEGRATION` es el mismo
`4502c4bd…7aea` de la ejecución de `CU0`.

### 4.1 `INTEGRATION`: rechazo por una prueba inestable preexistente

Las dos ejecuciones de `INTEGRATION` (2026-10-20, 10:07–10:48 y 11:04–11:37 UTC) terminaron
`REJECTED_SEMANTIC / COMPLETE`, 18/19. En ambas, la única hoja violada fue
`junit.desktop.final.semantic`, con un caso fallido de 2 179:
`org.geocedg.desktop.PreG9BR6PlusE3IsoABorderDesktopTest.toolCreatesOneSheetWithOneUndoPoint`,
con el mensaje «undo removes the whole sheet ==> expected: <0> but was: <1>». En ambas pasaron
las demás selecciones: `junit.shared.final` (703 clases, 7 015 pruebas, 0 fallos, 10 omitidas),
el resto de `junit.desktop.final` (2 178 pruebas, 1 omitida) y `junit.desktop.g9u1-isolated`
(2 clases, 5 pruebas). Todas las demás hojas fueron `CONTRACT_SATISFIED`.

Análisis:

- **Mecanismo.** La prueba ejecuta el escenario `tool` de `PRE-G9B-R6-plus-E3` en una JVM
  propia: crea un marco ISO A con la herramienta, deshace inmediatamente y espera que el marco
  desaparezca. `UndoManagerD.storeUndoInfo`, sin cambios en `CU1`, guarda cada punto de
  deshacer en un hilo nuevo, sin orden entre guardados ni con un `undo()` posterior. El
  escenario no espera a que terminen los guardados de la preparación y del clic: si terminan
  en otro orden, o el primero aún no ha terminado, el deshacer no retira el marco. El escenario
  `link` de la misma clase sí espera, con `addUndoInfoStoredListener`.
- **El escenario no ejecuta código de `CU1`.** De las 38 clases de producto que modifica el
  candidato, su JVM solo carga `GeoLocusV2` (con un método nuevo sin usar) y los enumerados
  `MetricDiagnosticCode2D` y `MetricMethod2D` (con constantes nuevas), y no crea ningún lugar
  (registro de carga de clases de una ejecución). El código de producto de `CU1` no crea hilos,
  ejecutores ni temporizadores.
- **Misma tasa en la base.** Sondeo directo del escenario fuera de Gradle: con el classpath de
  pruebas del candidato pasan 40 de 40 ejecuciones secuenciales y 60 de 60 con 6 JVM en paralelo;
  con 24 JVM en paralelo fallan 54 de 144 y 67 de 144. Con la base autorizada `e66ae4c0`,
  extraída con `git archive` y compilada en el directorio temporal de la sesión (su código de
  producto es el de `main`), fallan 58 de 144 con 24 JVM en paralelo. Siempre es el mismo
  mensaje, y la tasa del candidato (121 de 288) no se distingue de la de la base.
- **Sin efecto del candidato en el entorno del conjunto de pruebas.** Los productores de
  `INTEGRATION` se ejecutan en secuencia y `junit.desktop.final` se ejecuta solo. El orden de las
  clases alrededor de la prueba es el mismo que en el `INTEGRATION` de `CU0`, desplazado una
  posición por la clase nueva de `CU1`. Las 40 clases que se ejecutan entre esa clase nueva y la
  prueba suman 296 s en el `INTEGRATION` de `CU0`, 306 s en el `final.desktop` previo al commit
  y 310 s y 334 s en las dos ejecuciones rechazadas. La clase nueva `PreG9BCu1AcceptanceTest`
  dura entre 3,3 y 3,8 s y libera sus aplicaciones con `G9U1TestApp.Lifecycle`, como las de
  `CU0`.
- **Historial.** En las 33 ejecuciones anteriores conservadas en `artifacts/agent/` que ejecutan
  esta clase, incluido el `INTEGRATION` de `CU0`, no consta ningún fallo. En este candidato pasó
  en el `final.desktop` previo al commit (§2.1, 11:31–11:46 hora local) y falló en las dos
  ejecuciones de `INTEGRATION`.
- **Estación.** Durante el análisis, 2026-10-20 a las 11:40 UTC, la estación tenía comprometidos
  31,5 GB de un límite de 34,6 GB, con la compresión de memoria activa y otras aplicaciones
  pesadas abiertas. No se midió durante las ejecuciones.

Conclusión: el fallo es una carrera de la prueba, anterior a `CU1`, cuya probabilidad depende
de la carga de la estación. No se ha identificado ninguna vía por la que el candidato la cause.
La prueba no se modifica en este candidato, porque no pertenece al alcance de `CU1`. Se registra
como observación `OBS-CU1-ISOA-UNDO-STORE-RACE` y su tratamiento es la decisión D-CU1-R6 (§8).
No se ha lanzado una tercera ejecución de `INTEGRATION`.

Diagnósticos de `STATIC` y de las dos `INTEGRATION`: `diagnostic.governance` =
`DIAGNOSTIC_FINDING` y `diagnostic.historical-consistency` = `DIAGNOSTIC_UNAVAILABLE`. Son las
notas permanentes del protocolo temporal de recuperación de la infraestructura de verificación y
aparecen igual en las ejecuciones de `CU0`. Los demás diagnósticos de `INTEGRATION`, incluidos
los cuatro de Checkstyle, `diagnostic.style`, `diagnostic.documentation`,
`junit.desktop.revision3-diagnostic` y `repository.whitespace-diagnostic`, son
`DIAGNOSTIC_CLEAR`.

Los registros están en
`artifacts/agent/pre-g9b-cu1-{static,infra-unit,phase-pre-g9b-cu1,phase-pre-g9b-cu0,integration}-8ee19667/`
y `artifacts/agent/pre-g9b-cu1-run2-integration-8ee19667/`, que Git ignora.

## 5. Lo que no se ha ejecutado

- `FINAL`: la clase autorizada no lo exige y el plan lo reserva para `CU10`. Por eso no hay
  recibo de verificación.
- Una tercera ejecución de `INTEGRATION` o cualquier corrección de la prueba inestable: quedan
  sujetas a la decisión D-CU1-R6.
- Prueba de humo autoral y ejecutable empaquetado ejecutado por el autor: corresponden a la
  revisión autoral (D-CU1-R1). `INTEGRATION` construye y valida el paquete del producto
  (`packaging.product`, satisfecha en las dos ejecuciones).
- Escenario ventaneado del arnés de `CU0`: no se repitió. El banco `composition` mide el
  pintado y la actualización con el mismo modelo; los tiempos son informativos.

## 6. Limitaciones

Son las de §11 del informe del candidato. Además:

1. La evidencia del inventario (§2.1) se produjo antes del commit, sobre el árbol de trabajo,
   con los cambios posteriores sin efecto en las identidades descritos allí. Las ejecuciones de
   aceptación (§4) y el banco y las referencias de §3 son los únicos ligados a la identidad del
   candidato.
2. Los sondeos de §4.1 son evidencia de diagnóstico ejecutada desde el directorio temporal de la
   sesión, no pruebas registradas.
3. Los tiempos son de una sola estación y no constituyen puertas.

## 7. Contenido de este commit documental

Este commit:

- añade este registro;
- enlaza el registro desde el informe del candidato;
- registra la identidad del candidato y el resultado de su verificación en el roadmap (filas
  «Última fase ejecutada» y «Siguiente puerta» y sección «Track PRE-G9B-CU»).

No modifica código, pruebas, registro, inventario ni evidencias, y no cambia ningún registro de
cierre.

## 8. Decisión pendiente y estado de autorización

La verificación añade una decisión a las de §12 del informe del candidato:

| Decisión | Pregunta | Opciones |
|---|---|---|
| D-CU1-R6 | Tratamiento de `OBS-CU1-ISOA-UNDO-STORE-RACE`, que impide completar `INTEGRATION` | (a) autorizar una corrección mínima solo de prueba, fuera del alcance de `CU1`: en `PreG9BR6PlusE3IsoABorderDesktopTest`, el escenario `tool` fija una línea base de deshacer antes del clic y espera el guardado del clic con `addUndoInfoStoredListener` antes de deshacer, como el escenario `link`, sin cambiar sus aserciones; se aplica como descendiente correctivo del candidato y se repite la verificación que exija su clase; (b) repetir `INTEGRATION` sobre el mismo candidato en condiciones controladas de la estación; (c) aceptar la verificación con una excepción autoral explícita para este caso, registrada con propietario |

Propuesta del agente: (a), porque elimina la carrera en lugar de depender de la carga de la
estación, y la misma prueba forma parte de `FINAL`.

`CU1` se ha implementado según la autorización autoral de 2026-10-20 y está pendiente de la
decisión D-CU1-R6 y de la revisión autoral. No se declara `PASS — AUTHOR APPROVED`.
`CU2`–`CU10`, la publicación en `main`, las etiquetas, los releases, los binarios y los cambios
del libro siguen sin autorizar.
