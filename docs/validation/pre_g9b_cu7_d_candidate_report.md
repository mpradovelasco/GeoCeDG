# PRE-G9B-CU7-D — Informe del candidato de diseño

| Campo | Valor |
|---|---|
| Estado | **DESIGN CANDIDATE — PENDING AUTHOR REVIEW**; `selfApproved=false`; sin aprobación normativa ni autorización CU7-I |
| Encargo | autorización autoral de diseño M08/M09 de 2026-10-28 |
| Rama | `phase/pre-g9b-cu7-d-design`, local, no publicada |
| Base | `72f1c069586f3cf53f26428d31bfd1cbc7014b43`; árbol `7e2d8a45594bad9f08eff52c41b51c49929379d9`; main local/remoto comprobados e iguales, worktree limpio al inicio |
| Worktree | `C:/DesarrolloyDatos/Areas/ProyectosNoFinanciados/CeDG/GeoCeDG` |
| Capa | diseño de semántica/ciclo de vida del kernel compartido y contratos de sus consumidores; ninguna modificación productiva |
| Identidad de entrega | commit y árbol del candidato se informan en la entrega Git; no se intenta incluir un hash autorreferente en este archivo |

## 1. Entregables y hallazgos

- [Diseño arquitectónico y análisis de fuentes](../architecture/pre_g9b_cu7_d_point_placement_and_redefinition_design.md).
- [ADR 0041 propuesto](../adr/0041-locus-v2-point-placement-and-point-redefinition.md).
- Contratos candidatos [M08](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md)
  y [M09](../../geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md), con texto de enmiendas
  en §8 y §10; no se aplican a las autoridades aprobadas.
- [13 decisiones reservadas al autor](pre_g9b_cu7_d_author_decision_sheet.md).
- [Matriz focal/adversaria, incrementos y smoke futuro](pre_g9b_cu7_d_validation_and_implementation_gates.md).
- [Manifiesto de caracterización reproducible](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu7-d-characterization.json).

Recomendación: dos operaciones M08, fijación de dirección y correspondencia dinámica explícita;
para M09, actualización compatible explícita de P separada de la identidad de raíz y del
reemplazo real. Se conserva DA-5: no se exige coincidencia. La dinámica general por proximidad,
el remapeo parcial automático y la conversión silenciosa de roles no se recomiendan.

| Hallazgo | Evidencia y alcance |
|---|---|
| R6/G9U1 ya resuelven y materializan direcciones | código y pruebas existentes; no hay aún una función dinámica general Q → P |
| Cambiar N por default no está autorizado implícitamente | assessment `DURABLE_CONTRACT_CHANGE`; añadir opciones de la ruta normal no cambia el rechazo |
| N con contract-update alcanza fallos prospectivos | `MALFORMED_RECORD`: padre público sin identidad; con nuevo soporte ya participante, dependencias distintas al DAG reconstruido |
| L con otro conductor no está universalmente prohibido | en caso mínimo se admite; en K5 con cierre materializado el assessment admite, pero sin nuevo cierre previamente registrado falla `GEO_ATTACHMENT_MISSING` |
| Registrar primero el nuevo cierre permite el cambio de L en la sonda K5 | se conserva ID de L y del claim; claim indefinido y antiguo token no admisible; ambas políticas. No prueba general F/T ni persistencia |
| Punto semántico VALUE → token distante | solo `LEGACY_REPLACEMENT` en la sonda; identidad nueva. RETAIN/contract-update no se ofrecen |
| Punto libre sin ID → token distante | falla con `GEO_NOT_SERIALIZABLE` antes de assessment; hace falta contexto/participación de target ordinario, no una comprobación de distancia |

Las dos observaciones heredadas quedan investigadas y delimitadas, **no cerradas como corrección
del producto**. El cierre histórico de CU3 permanece intacto. Ninguna raíz se reasocia para
hacer pasar una sonda; los rechazos se registran como resultados del producto actual.

## 2. Evidencia ejecutada CU7-D

### 2.1 Sonda no productiva final

Desde la raíz del repositorio, con PowerShell 7.6.9:

```powershell
& tools/agent/checks/gradle-command-evidence-producer.ps1 `
  -GradleTask ':shared:common-jre:test' `
  -AdditionalArguments @('--tests',
    'org.geocedg.common.locus.PreG9bCu7DRedefineCharacterizationTest',
    '--init-script','tools/benchmark/pre-g9b-cu/cu7-d/probe.init.gradle','--offline') `
  -EvidencePath 'artifacts/pre-g9b-cu7-d/characterization-05/process.json' `
  -OutputDirectory 'artifacts/pre-g9b-cu7-d/characterization-05'
```

Resultado: productor exit **0**, Gradle `inner_exit_code=0`; JUnit **3 métodos, 0 fallos,
0 errores, 0 omitidos**; 24 observaciones, 7 operaciones aplicadas y 17 rechazos con
comprobación de XML idéntico al de entrada. Son observaciones de APIs existentes y comprobaciones
de rollback, **no 24 pruebas de conformidad de M08/M09**. Runtime Java real, no fake-first.

Logs/evidencia: [process.json](../../artifacts/pre-g9b-cu7-d/characterization-05/process.json),
[stdout](../../artifacts/pre-g9b-cu7-d/characterization-05/stdout.log),
[stderr](../../artifacts/pre-g9b-cu7-d/characterization-05/stderr.log),
[JUnit archivado](../../artifacts/pre-g9b-cu7-d/characterization-05/junit.xml).
El productor es neutral y devuelve 0 incluso ante un hijo fallido: se comprobaron el código
interno y el XML; no se dedujo aceptación de su exit exterior.

| Entrada/evidencia final | SHA-256 |
|---|---|
| Java de la sonda | `2f06ce2b7e8f412813e8211dea6aac222d5449928a550207312ccaf65f594567` |
| init optativo | `7c36ea16627bf0c5ebdb3a01bc460c07cbcd0f35c4911e15949b63f3d8a3ae85` |
| XML JUnit de ejecución 05 | `05ed9650602ddb1df763980dab58e17fb3ebab30651516c518848cc2404895d5` |

La sonda está en `tools/benchmark/pre-g9b-cu/cu7-d/`, fuera de los source sets normales.
El init solo la añade al invocarlo expresamente; no se modifica build.gradle ni el registro de
verificación. El manifiesto registra los hashes y extrae filas del JUnit; no es fuente normativa.
Los artefactos brutos son locales ignorados y deben acompañar cualquier auditoría que necesite
los logs, mientras el resumen/manifiesto durable queda en Git.

### 2.2 Intentos anteriores y clasificación

| Ejecución | Resultado y disposición |
|---|---|
| characterization-01 | hijo exit 1 antes de tests: lock de caché Gradle denegado por sandbox Windows. Reejecución mediante escalado gestionado; no cambio de producto |
| characterization-02 | hijo exit 0, sonda inicial de 2 métodos, anterior a la instrumentación/casos completos |
| characterization-03 | hijo exit 1: preparación incorrecta de la sonda intentaba redefinir X0 con `add` después de registrarlo; `REDEFINE_CONTEXT_MISSING`. Fallo del arnés de caracterización, no regresión productiva; se corrigió creando el fixture completo antes de registrar L |
| characterization-04 | hijo exit 0, 3 métodos, 0 fallos; antes del traslado optativo y de observar assessment/claim |
| characterization-05 | hijo exit 0, sonda final desde ubicación optativa; evidencia anterior no se relabela |

No se modificó código del kernel para compensar fallos de entorno, de arnés ni rechazos
semánticos. La excepción de perfil PowerShell/Conda al inicio del shell pertenece al entorno;
las invocaciones de trabajo usaron `login=false`. El JDK/Gradle y los imports se consumieron de
la estación existente, offline, sin instalaciones ni descargas.

### 2.3 STATIC y revisión final

La verificación canónica aplicable se ejecuta sobre el commit guardado, mediante
`tools/agent/verify.ps1 -Profile STATIC`, con directorio único bajo
`artifacts/pre-g9b-cu7-d/`. Su resultado/identidad exacta se añade tras la ejecución, sin
atribuirle aprobación del diseño. No se sustituye por una invocación de un perfil DEV incompleto.

Revisión focal completada: 7 documentos nuevos, **42 enlaces relativos existentes**, JSON
parseado, 24 observaciones y hashes de la sonda coincidentes; exit 0,
[registro](../../artifacts/pre-g9b-cu7-d/document-review.json). `git diff --check` exit 0;
sin cambios en `source/`; main permanece en la base.

Limpieza del source set optativo: mismo productor, tarea
`:shared:common-jre:compileTestJava`, `-AdditionalArguments @('--offline')`, sin init;
productor exit 0, hijo exit 0, **ninguna clase PreG9bCu7D restante en la salida normal**.
[Evidencia de limpieza](../../artifacts/pre-g9b-cu7-d/probe-cleanup/process.json).

## 3. Archivos inspeccionados

Inspección de las secciones relevantes, no afirmación de lectura exhaustiva de cada archivo:

| Grupo | Archivos |
|---|---|
| Gobierno/base | `AGENTS.md`; `.github/copilot-instructions.md` comprobado ausente; status, refs, árbol, log y worktrees Git |
| Planificación | `docs/roadmap/geocedg_roadmap.md`; `docs/architecture/pre_g9b_cu_corrective_usability_plan.md`; `pre_g9b_cu_corrective_usability_plan_candidate.md`; `pre_g9b_cu0_redefinition_and_canvas_picking_design.md` |
| ADR | `docs/adr/0011-g9-spatial-persistence-and-phase-gates.md`, `0017-deterministic-intersection-phase-rank-identity.md`, `0019-semantic-locus-point-interaction-support.md`, `0026-advanced-redefine-and-explicit-legacy-fallback.md`, `0039-locus-v2-structural-points-and-certified-contact-evidence.md`, `0040-constructive-geometric-authority-and-relation-integrity.md` |
| Contratos | `geocedg/specs/locus/locus-v2-semantics.md`, `locus-v2-point-interaction.md`, `locus-v2-public-surface.md`, `locus-v2-existence-components.md`, `locus-v2-intersections.md`, `locus-v2-constructive-authority.md`; `geocedg/specs/spatial/durable-dependency-projection.md` |
| Cierres CU3 | `docs/validation/pre_g9b_cu3_a_closeout_record.md`, `pre_g9b_cu3_b1_closeout_record.md`, `pre_g9b_cu3_b2_closeout_record.md`, `pre_g9b_cu3_global_closeout_record.md`, `pre_g9b_cu3_global_closeout_dossier.md`; `geocedg/validation/pre-g9b-cu/pre-g9b-cu3-global-closeout.json` |
| Antecedentes técnicos | registro G1 de CU3-B2; matriz/candidato B2; diseño regular B2; sonda histórica `PreG9BCu3B2CharacterizationProbe.java` y su runner; registro de PowerShell de CU3 |
| Operaciones | `geocedg/specs/operations/verification-levels.md`, `verification-registry.json`, `documentation-maintenance.md`; `tools/agent/verify.ps1`; `tools/agent/checks/gradle-command-evidence-producer.ps1` |

Código compartido inspeccionado bajo
`source/shared/common/src/main/java/`:

- `org/geocedg/common/kernel/locus/`: `LocusV2PublicOperations.java`,
  `SemanticGeneratorDescriptor1D.java`, `LocusComponentLineage2D.java`;
  `interaction/LocusPointInteractionQuery2D.java`, `LocusPointInteractionResult2D.java`,
  `LocusPointInteractionResolver2D.java`; `intersection/IntersectionSourceBinding2D.java`.
- `org/geocedg/common/kernel/algos/`: `AlgoSemanticLocusPoint2D.java`,
  `AlgoLocusIntersectionPointV2.java`, `AlgoLocusDriverCorrespondencePoint2D.java`,
  `AlgoDependentPointLocusV2.java`, `AlgoLocusIntersectionV2.java`.
- `org/geocedg/common/kernel/spatial/identity/`: `ConstructionGeoRedefineProvider.java`,
  `SpatialIdentityRegistry.java`, `SpatialRedefineAssessment.java`,
  `SpatialRedefineAssessmentHandler.java`, `SpatialRedefineExecutionMode.java`.
- `org/geogebra/common/kernel/Construction.java`,
  `commands/AlgebraProcessor.java`, `commands/EvalInfo.java`.

Desktop y pruebas inspeccionadas: `GeoCeDGPointInteraction.java`,
`GeoCeDGSpatialRedefineFrontend.java` bajo `source/desktop/desktop/src/main/java/org/geocedg/desktop/`;
`G9U1SemanticPointInteractionTest.java` en su src/test; bajo
`source/shared/common-jre/src/test/java/org/geocedg/common/locus/`,
`G9U0PublicSurfaceTestBase.java`, `G9U0IntersectionTokenTest.java`,
`G9U0R6SemanticLocusPointInteractionTest.java`, `PostG9U1A3CompatibleV2RedefineTest.java`,
`PreG9bR3DurableContractRedefineTest.java`; fixture `ErrorAccumulator.java`.
Los tests existentes fueron inspeccionados, no reejecutados ni declarados PASS por CU7-D.

## 4. Archivos cambiados, contratos e impacto

Nuevos: este informe; arquitectura CU7-D; ADR 0041 candidato; candidatos M08 y M09;
hoja de decisiones; matriz/puertas; manifiesto de caracterización; Java e init optativos de
la sonda. Actualizados mínimamente: plan consolidado §2.31/estado CU7 y roadmap 5.101/puerta
actual. Ningún registro histórico, ADR aceptado, contrato aprobado ni archivo `source/` cambia.

Efecto semántico/compatibilidad del candidato: **ninguno en producto o documentos guardados**.
Se preservan identidad, parámetros, políticas de autoridad, garantías de CU2/CU3, tokens y
persistencia. Las propuestas futuras distinguen asunciones, derivaciones y evidencia; no se
concede exactitud por tolerancia, continuidad por cercanía ni verdad geométrica al frontend.

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: inspeccionados verify.ps1, el productor Gradle y el contrato operativo.
No cambian JDK, Gradle Wrapper, Conda/Python, prerequisitos, rutas de instalación,
perfiles, registro, bootstrap, packaging ni referencias numéricas. El init añade
solo la sonda optativa en esa invocación; no cambia la compilación ordinaria.
INFRASTRUCTURE_IMPACT = NONE ON CANONICAL VERIFICATION/BOOTSTRAP
REQUIRED_LEVEL = STATIC (CU7-D, autorización específica) + sonda observacional acotada
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = No cambia comportamiento ni contrato público/de desarrollo vigente;
las ampliaciones están marcadas candidatas. Guía del alcance implementado: puerta I5;
integración/empaquetado pendiente de CU10 conservado.
```

## 5. Límites y parada

No se ejecutan PHASE CU7-I, INTEGRATION nueva, FINAL, GUI/smoke de capacidades futuras,
matriz de lectores de codecs inexistentes ni workflows de Modelica/FMU/R/PKNCA (no aplicables).
No se prueba una proyección dinámica nueva, la separación productiva de hermanos, el remapeo
selectivo ni la certificación general F/T. La sonda mide un conjunto finito de casos y no
determina la corrección universal de todos los caminos de redefinición.

Se conservan `CU10-GUIDE-INTEGRATION-CHECK`, E1 y
`DEBT-CU3-EXACT-TANGENCY-GENERALIZATION`; no se adelantan CU10 ni las obligaciones editoriales.
No hay cambios del libro ni publicación. **CU7-D termina aquí como candidato pendiente del
autor; CU7-I no se inicia.**
