# Simplificación operativa — informe del candidato

| Campo | Valor |
|---|---|
| Estado | `OPERATIONAL SIMPLIFICATION — PASS — AUTHOR APPROVED` (decisión autoral explícita del 2026-11-20 sobre `T` y `D`, incluida la excepción procedimental de la sección 3) |
| Estado al redactar este informe | `CANDIDATE FOR AUTHOR REVIEW` (conservado como historia) |
| `selfApproved` | `false` |
| Publicación | `main`: `c311829d` → `8f9218dbc8a6312eb3505faa313f1b2a51e3c47f` por fast-forward sin force (2026-11-20); GitHub informó del bypass habitual de las reglas «Changes must be made through a pull request» y «Cannot change this locked branch» |
| Rama de trabajo | `maintenance/operational-simplification` |
| Baseline | `main` = `origin/main` = `c311829d0cfe809a0b7a9ba006b476545cd3a867`, árbol `df5ddaee93909fd6551f361c2885de2da4ad5c03` |
| Commit técnico `T` | `4cd5dc9d3b46dbec3e9c4c53bf2cbaf08f667af7`, árbol `e68e19c81a6bdcebfb6e0018a93026233a0f859d` |
| Commit documental `D` | este informe, descendiente directo de `T` |
| Libro | `geocedg_book` `main` = `origin/main` = `40b9547b5ed787da2ed34705815df0b1393b4f6b`, árbol `ebd981557b03f316d76ca59557604fe35fba41d9`; solo lectura |
| Efecto de producto | Ninguno: sin cambios en kernel, Desktop, serialización, referencias numéricas ni tolerancias |

Este informe es evidencia de ejecución. La autoridad normativa propuesta está en
[`verification-levels.md` — Operational simplification](../../geocedg/specs/operations/verification-levels.md#operational-simplification).

## 1. Precondiciones comprobadas

- GeoCeDG: árbol limpio; `HEAD`, `main`, `origin/main` y la referencia remota
  viva `refs/heads/main` resuelven a `c311829d`. El checkout estaba en la rama
  `feature/book-bridge-typed-references` con ese mismo SHA; la rama operativa
  se creó desde `main`.
- Libro: árbol limpio; `HEAD`, `origin/main` y la referencia remota viva
  resuelven a `40b9547b` (contiene la reconciliación `d4563a36`). El checkout
  está en `book/consolidated-outline-integration` con ese mismo SHA; no se
  cambió de rama porque el libro es solo de consulta.
- El enlace `book` es un `SymbolicLink` ignorado por Git que apunta a
  `C:\DESARROLLOYDATOS\Areas\PublicacionesYCharlas\Libros\GeoCeDG`. El nuevo
  inventario lo registra como enlace (destino) y no lo recorre.
- PowerShell 7.6.10. Las suites `verification-registry` y `verification-report`
  ya no reproducen el fallo de identidad de `Test-Json` observado con 7.6.9.

## 2. Problema A — hash e integridad del repositorio

### Causa medida

`Get-VerificationPackagingFileRecords` hacía `Get-ChildItem -Recurse -Force` de
todo el checkout **dos veces por instantánea** (dentro y fuera de las raíces
declaradas) y calculaba SHA-256 con `Get-FileHash` de cada archivo de su clase.
La clase protegida incluía todo `artifacts/` y todas las salidas Gradle anidadas
(`source/**/build`, `source/**/.gradle`), porque solo `build`, `.gradle` y
`.kotlin` de primer nivel estaban declarados.

Inventario del árbol real en `c311829d`:

| Clase | Archivos | Tamaño |
|---|---:|---:|
| `artifacts/` (110 entradas ignoradas) | 472 794 | 14 705 MB |
| Salidas Gradle anidadas ignoradas | ≈ 15 700 | ≈ 700 MB |
| Archivos versionados | 13 085 | 218,5 MB |
| No versionados no ignorados | 0 | 0 |

Había además tres instantáneas completas por ejecución de `PACKAGING`: una en
`verify.ps1` (`New-PackagingRepositoryBaseline`, publicada en
`GEOCEDG_PACKAGING_BASELINE_PATH`, variable que **ningún consumidor leía**), otra
en `packaging.repository-baseline.producer` y otra en
`packaging.repository-safety`. `INTEGRATION` y `FINAL` incluían también los dos
nodos de packaging.

Medición antes del cambio (módulo de `c311829d` copiado al scratchpad, árbol
real, sin builds concurrentes, 2026-11-20 19:38–20:17):

| Medida | Valor |
|---|---:|
| Un recorrido recursivo (`Get-ChildItem -Recurse -Force`) | 17,1 s; 502 026 archivos; 17 022 MB |
| — de ellos bajo `artifacts/` | 472 795 archivos; 15 421 MB |
| Una instantánea completa (dos recorridos + `Get-FileHash`) | **2 347 s (39,1 min)**; 501 621 protegidos + 37 declarados |
| Coste atribuible al hash | ≈ 2 313 s (total menos dos recorridos) |

Con tres instantáneas por `PACKAGING`, el coste de integridad era de unos
117 min con el tamaño actual de `artifacts/`; los ≈ 75 min comunicados en 1.2.1
corresponden a un `artifacts/` menor. El recorrido es además frágil: un primer
intento de medición se abortó a los 37 min porque un build Gradle concurrente
borró un archivo temporal en `source/build-logic/convention/build/tmp`.

### Corrección

`tools/agent/verification-packaging-state.psm1` usa el esquema
`GIT_INVENTORY_V1`:

- `git ls-files --cached` (versionados, hash de bytes crudos con
  `SHA256.HashData`; un versionado ausente se registra como `ABSENT`);
- `git ls-files --others --exclude-standard` (no versionados no ignorados);
- `git ls-files --others --ignored --exclude-standard --directory`
  (ignorados): se excluyen sin leerlos `artifacts/` y los directorios ignorados
  `build`, `.gradle` y `.kotlin`; el resto de entradas ignoradas se recorre sin
  seguir enlaces ni entrar en metadatos `.git` anidados;
- enlaces y junctions se registran por destino; `.git` nunca se recorre.

`repository-safety.ps1` registra el esquema en la baseline y rechaza como
`EVIDENCE_UNTRUSTED` una baseline de otro esquema. `verify.ps1` deja de calcular
la instantánea no consumida. Las comprobaciones propias de packaging
(`packaging.product`: ZIP/MSI/EXE, SBOM, manifiesto, registro de distribución,
checksums, versión, perfil NC, procedencia del commit y entradas versionadas)
no cambian.

### Medidas después

| Medida | Antes | Después |
|---|---:|---:|
| Archivos examinados por instantánea | 502 026 enumerados ×2; 501 658 hasheados | 13 087 hasheados + 1 enlace |
| Bytes leídos por instantánea | ≈ 17 022 MB | 229,2 MB |
| Tiempo de inventario | ≈ 34 s (dos recorridos) | listados Git < 0,5 s |
| Tiempo por instantánea (árbol real) | 2 347 s | 4,7–5,7 s (in-process); 6,5–6,7 s por nodo |
| Instantáneas por `PACKAGING` | 3 | 2 |
| `PACKAGING` completo (sin reconstruir instaladores) | ≈ 75 min comunicado en 1.2.1; 288 s solo el productor de baseline el 2026-11-13 | **14 s** (`verify.ps1 -Profile PACKAGING`, ACCEPTED/COMPLETE sobre `T`) |

Independencia del tamaño de `artifacts/` (fixture controlada, caso nuevo de
`verification-contract-boundary.Tests.ps1`): al añadir 400 archivos y modificar
salidas generadas, `files_hashed`, `bytes_hashed` y el hash protegido no
cambian. Negativos conservados en la misma fixture: modificación de un archivo
versionado, de un versionado bajo `artifacts/`, un nuevo no versionado, un
ignorado no generado, un versionado borrado y un enlace redirigido se detectan;
el contenido detrás de un enlace no se lee; el nodo de seguridad devuelve
`CONTRACT_VIOLATED` (exit 2) ante la modificación no autorizada de una fuente y
`CONTRACT_SATISFIED` ante el crecimiento de `artifacts/`.

## 3. Problema B — verificación ordinaria

### Composición

| Perfil | Antes | Después |
|---|---|---|
| `INTEGRATION` | 46 nodos; `final.shared` (7 867), `final.desktop` (2 634), `g9u1-isolated` (5) y diagnóstico Revision3 (1) | 42 nodos; `core.shared` (**453**) y `core.desktop` (**126**) |
| `FINAL` | 68 nodos | 68 nodos; mismas selecciones exhaustivas; no ejecuta CORE |

CORE: 30 clases Shared y 14 Desktop, elegidas por garantía y riesgo, con un caso
representativo por contrato y negativos de violaciones graves (rechazo atómico,
identidad no inferida, ausencia de autoridad de render, trabajo acotado). La
matriz `invariante → test → riesgo → complemento` es parte del contrato
propuesto en `verification-levels.md`.

Última `INTEGRATION` con selecciones completas (2026-11-13,
`post-cu8-i-r3-closeout`): 53 min; `final.desktop` 1 859 s, `final.shared`
363 s, baseline de packaging 288 s, Checkstyle 4 × ≈ 85 s, `g9u1-isolated`
82 s, Revision3 81 s, compilación 2 × ≈ 65 s. Con CORE los productores JUnit
pasan a 106 s + 168 s; Checkstyle y compilación pasan a dominar.

### Política FINAL (excepción procedimental solicitada)

El contrato vigente exige `FULL/FINAL` por categoría (secciones 2, 6, 8, 11.2,
12.1, 12.7 y 12.8 de `verification-levels.md`), en particular para cambios de
infraestructura del verificador. **Este candidato no ha ejecutado `FINAL`.** Se
solicita expresamente al autor que apruebe o rechace, junto con la política
nueva, la excepción procedimental de verificar este candidato de
infraestructura con evidencia focal (`STATIC`, `PACKAGING`, `INFRA_UNIT`, suites
focales y las dos selecciones CORE) en lugar del `FINAL` que exigía el contrato
anterior. La política nueva no es retroactiva y no aprueba campañas omitidas.

Decisión posterior (2026-11-20): el autor aprobó la política nueva, esta
excepción procedimental y la promoción de `T` y `D`. La aprobación no convierte
en ejecutadas las campañas que no se ejecutaron: `FINAL` y la `INTEGRATION`
completa siguen sin ejecutarse para este candidato.

## 4. Auditoría inicial de solapamientos en FINAL

Aplicado (eliminación demostrada y de bajo riesgo):

1. `INTEGRATION` y `FINAL` ejecutaban las mismas selecciones completas sobre el
   mismo commit; ahora `INTEGRATION` usa CORE y `FINAL` no repite CORE.
2. La instantánea de packaging de `verify.ps1` no tenía consumidor; eliminada.
3. El recorrido de `artifacts/` y de salidas Gradle en cada instantánea.

Propuesto para una consolidación posterior (no aplicado):

| Hallazgo | Evidencia | Propuesta |
|---|---|---|
| Cada productor Gradle usa `--rerun-tasks` | En `FINAL` hay 10 invocaciones Gradle (4 JUnit, 2 compile, 4 Checkstyle) y cada una vuelve a ejecutar todo el grafo, incluida la compilación; el propio §4 pide un rerun acotado a la tarea | Usar `test --rerun` (Gradle 9.4.1) en productores JUnit tras demostrar equivalencia de contexto; requiere nuevas identidades de selección |
| Checkstyle en `INTEGRATION` | 4 productores, ≈ 5,7 min, solo diagnóstico | Mantenerlo en `FINAL` y en un perfil diagnóstico explícito |
| Diagnóstico Revision3 en `FINAL` | Invocación Desktop completa para 1 prueba que no afecta a la aceptación | Moverlo a `DEV` bajo demanda |
| Caracterizaciones previas a implementación | 30 suites Shared y 7 Desktop de caracterización/benchmark (223 + 31 pruebas); p. ej. `G8A*Characterization` frente a `G8B*Kernel`, `G7A*Characterization` frente a `LocusMetricProductive*` | Revisión de equivalencia de precondiciones y oráculos clase por clase antes de retirar ninguna |
| Clases dominantes | `PreG9BCu2AuthorModelSelectionSettlementTest` 420 s; `PreG9BR6PlusE3R1ExportInteroperabilityTest` 137 s; `PreG9BR6CapabilityMatrixTest` 100 s | Perfilado y, si procede, reducción de casos equivalentes |

No se ha retirado ninguna prueba ni reescrito ninguna clase.

## 5. Problema C — discrepancias documentales

Los canales ya estaban separados en el supervisor (`SEMANTIC`, `SAFETY`,
`VERIFICATION_CORE` frente a diagnósticos que no alteran el código de salida;
caso de regresión existente en `verification-diagnostic-separation.Tests.ps1`).
Faltaba un procedimiento que evitara repetir la verificación por una corrección
documental posterior. Se añade `tools/agent/documentary-descendant.ps1`, de solo
lectura: prueba la ascendencia `T → D` y clasifica cada ruta por evidencia
(`EXECUTABLE`, `NORMATIVE`, `DOCUMENTARY`), incluida la cita literal o por nombre
de archivo desde fuentes de producto/test, checks canónicos, módulos, archivos
citados por el registro, ayudas de release y catálogos. No escribe recibos ni
cambia Git.

Ejemplos verificados sobre la historia real:

- `4267cff3` (versión 1.2.1) → `01aacee0` (registro de publicación):
  `DOCUMENTARY_ONLY`, resultado técnico reutilizable para `4267cff3`.
- `763cff09` → `2971e75d` (seis documentos de 1.2.x): `DOCUMENTARY_ONLY`.
- El roadmap es leído por `tools/agent/repository-state.ps1` y por el puente del
  libro, que no son autoridades de aceptación; la herramienta lo informa como
  `informational_citations` sin cambiar la clase.

Este informe se publica en el commit documental `D`, descendiente directo de
`T`, precisamente para aplicar el procedimiento: la evaluación `T → D` se
ejecuta tras crear `D` y su resultado se comunica en la entrega, no dentro de
`D`, para evitar una autorreferencia. Los resultados técnicos de la sección 7
pertenecen a `T` y no se repiten para `D`.

## 6. Problema D — registros y roadmap

Sin migraciones ni reestructuración. Se fija en el contrato una autoridad por
dato (resultado de verificación del commit exacto; decisión autoral en el
registro de cierre; fila de estado del roadmap; SHAs como vínculo) y se evita
un registro adicional por cada corrección documental. Propuestas no aplicadas:
retirar progresivamente los valores literales de hash de documentos aprobados
fijados dentro de verificadores de fase heredados (26 verificadores citan el
roadmap) y sustituirlos por la identidad Git del commit aprobado.

## 7. Tests focales ejecutados sobre `T`

Comandos ejecutados desde la raíz con PowerShell 7.6.10, árbol limpio en `T`;
registros fuera del repositorio (scratchpad de la sesión).

| Comando | Exit | Duración | Resultado |
|---|---:|---:|---|
| `verify.ps1 -Profile STATIC` | 0 | 8 s | ACCEPTED/COMPLETE, 3/3 hojas; diagnósticos permanentes `diagnostic.governance` FINDING e `historical-consistency` UNAVAILABLE (preexistentes, no de aceptación) |
| `verify.ps1 -Profile PACKAGING` | 0 | 19 s (14 s de supervisor) | ACCEPTED/COMPLETE, 3/3 hojas; baseline `GIT_INVENTORY_V1`, 13 088 registros protegidos, 134 entradas generadas excluidas |
| `verify.ps1 -Profile INFRA_UNIT` | 0 | 189 s | ACCEPTED/COMPLETE, 22/22 hojas `VERIFICATION_CORE`, 0 hallazgos |
| `verify.ps1 -Profile INTEGRATION -PlanOnly` | 0 | — | COMPLETE, 42 nodos; JUnit = `core.shared` + `core.desktop` |
| `verify.ps1 -Profile FINAL -PlanOnly` | 0 | — | COMPLETE, 68 nodos; selecciones exhaustivas sin CORE |
| `gradle-test-evidence-producer.ps1 -SelectionId core.shared` | 0 | 106 s | 453/453 PASSED, 30 clases |
| `gradle-test-evidence-producer.ps1 -SelectionId core.desktop` | 0 | 168 s | 126/126 PASSED, 14 clases |

`INFRA_UNIT` incluye las suites modificadas: `infra.contract-boundary`
(8 casos, 120 aserciones, con el caso nuevo de inventario),
`infra.final-coverage` (13 casos, 814 aserciones, con el caso nuevo
CORE/FINAL), `infra.diagnostic-separation` (3 casos, con el caso nuevo de
descendiente documental), `infra.registry` e `infra.cli-compatibility`.
`git diff --check` sin hallazgos.

## 8. Repositorio del libro

`NO CHANGE REQUIRED`. `tools/verify.ps1` del libro hashea archivos concretos
(`Get-CanonicalLfSha256` sobre prompts y registros aprobados), usa `git ls-files`
para el contrato de no versionar generados y `git check-ignore` para
`artifacts/`; el único recorrido recursivo es `tex/**/*.tex`. No hay inventario
global ni recorrido de artefactos. Ningún nodo del registro de GeoCeDG invoca
`tools/book` ni `verify-book-operations.ps1`, de modo que los cambios de
producto no disparan pruebas de paridad editorial. No se modificó el libro ni su
política de baseline; la reconciliación de etiquetas y baseline queda en su
propio trabajo.

## 9. Impactos declarados

```text
PRODUCT_PHASE_EFFECT = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATED (candidate)
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
packaging prerequisite or download contract changes; only registry
composition, the integrity inventory and a read-only assessment script.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/developer/geocedg_developer_guide.md;
  docs/developer/geocedg_operations_manual.md
selfApproved = false
```

Las instrucciones canónicas (`.github/prompts/**`, `AGENTS.md`, `CLAUDE.md`) no
se modificaron: remiten a `verification-levels.md`, que contiene la política.

## 10. Limitaciones y riesgos residuales

- No se ejecutó `FINAL` ni una `INTEGRATION` completa extremo a extremo. Las
  selecciones CORE se ejecutaron con sus productores canónicos (453/453 y
  126/126 PASSED) sobre un árbol previo al commit cuyas fuentes de producto y
  test son idénticas a `T`; la proyección canónica JUnit sobre esa evidencia y
  las fijaciones de `T` da `CONTRACT_SATISFIED / COMPLETE`. Es evidencia
  `DEVELOPMENT_DIAGNOSTIC`, no de cierre.
- CORE es cobertura reducida por diseño: las clases fuera de la matriz solo se
  ejecutan en `FINAL` o en su `PHASE`.
- La exclusión de salidas Gradle se basa en el nombre de directorios ignorados;
  otro directorio generado ignorado y voluminoso seguiría recorriéndose hasta
  que se declare explícitamente.
- Un worktree auxiliar bajo `.claude/worktrees/` (ignorado) se recorrería y
  hashearía; hoy está vacío.
- La clasificación documental es conservadora: una cita literal desde las
  autoridades ejecutables convierte un documento en entrada y obliga a decidir.
