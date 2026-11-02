> **HISTÓRICO — NO EJECUTAR COMO DIRECCIÓN VIGENTE.** La dirección autoral de 2026-11-02 añade creación por clic/Q como única ampliación funcional candidata y pide una nueva propuesta de cierre. El cierre exclusivamente M08-S-R2 de este prompt anterior no se adopta por la nueva instrucción. Véase [candidato sucesor](../../../docs/validation/pre_g9b_cu7_general_creation_candidate_report.md).

# GeoCeDG — PRE-G9B-CU7-RS: disposición de alcance reducido

**PROMPT CANDIDATO PARA REVISIÓN/AUTORIZACIÓN. NO EJECUTADO POR EL ESTUDIO.**

## Objective
<!-- geocedg-field: objective -->

Ejecutar una sola actuación documental de conciliación de CU7 tras la decisión autoral expresa de reducir su perímetro cerrable exclusivamente a I1 — M08-S-R2. Otras capacidades conservan sus aprobaciones propias y no se incorporan a este cierre. Conservar M09/I2G/I3/M08-D como funcionalidades no implementadas, diferidas o bloqueadas según el registro candidato; entregar un candidato exacto para revisión independiente. No implementar nuevas capacidades productivas.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY` describe el delta; no reactiva el contrato de verificación archivado. `PRODUCT_PHASE_EFFECT=NONE`. `selfApproved=false`. Nivel previsto: STATIC documental conforme a la autoridad activa de recuperación. No usar AUTHOR_DIRECT para evitar un fallo.

Esta ejecución necesita una instrucción autoral que identifique el SHA del estudio revisado y adopte expresamente: «Autorizo CU7-RS y la reducción de alcance descrita en el estudio; M09/I2G/I3/M08-D no se consideran implementados; no autorizo producto nuevo ni publicación». La aprobación del estudio por sí sola no se interpreta como esa adopción. No pedir decisiones nuevas sobre exclusiones ya resueltas por esa instrucción.

## Implementation base
<!-- geocedg-field: implementation_base -->

Base productiva fija inspeccionada por el estudio:

- Commit `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17`.
- Árbol `d62179e72dd2bb37226cba5956198809467cb7c1`.
- Upstream `9b93256b7df401ff056c37b502d82df4d72b1522`.
- Rama de estudio: `research/pre-g9b-cu7-integrated-reassessment`.

El SHA documental del estudio se toma literalmente de la autorización y se verifica con Git y su manifiesto externo; no se inventa dentro de un documento que forma parte de ese mismo commit. Comprobar ancestro/base y delta: solo documentos del estudio y prompt, sin producto. Si la base seleccionada por el autor es posterior, inspeccionar su delta y confirmar que no contradice las precondiciones; una incompatibilidad sustantiva exige detener, no rebasar o reinterpretar silenciosamente.

Usar rama/worktree aislados descendientes del estudio revisado. Prefijo por defecto para una rama nueva: `codex/`. No cambiar el worktree principal ni eliminar worktrees.

## Authority and evidence hierarchy

Leer desde disco `AGENTS.md`; código/configuración/serialización actuales; ADR0041 y su enmienda 2026-11-02; cierre canónico M08-S-R2; contratos M08/M09 y ADR0026/0029/0017/0039/0040; instrucciones autorales exactas de reducción. Después leer:

- [Estudio integrado](../../../docs/architecture/pre_g9b_cu7_integrated_reassessment.md), especialmente §§8–11.
- [Anexo M09/I2G](../../../docs/validation/pre_g9b_cu7_integrated_m09_evidence.md).
- [Anexo I3/M08-D](../../../docs/validation/pre_g9b_cu7_integrated_i3_m08d_evidence.md).
- [Anexo de gobernanza/pruebas](../../../docs/validation/pre_g9b_cu7_integrated_governance_verification.md).
- `docs/architecture/pre_g9b_cu7_i_increment_proposal.md`.
- `docs/validation/pre_g9b_cu7_d_validation_and_implementation_gates.md`.
- `docs/roadmap/geocedg_roadmap.md`.
- `geocedg/specs/operations/verification-levels.md`, sección activa inicial, registry/esquemas y `tools/agent/verify.ps1`.
- Prompts canónicos de gobernanza/verificación y task template.

Las fuentes históricas no autorizan ejecución nueva. No usar I2a/I2b autorizado en octubre como sucesor de la disposición de noviembre que no permite reanudar M09. Los anexos del estudio son evidencia/argumento, no autoridad geométrica nueva.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

Solo estas rutas durables y su contenido delimitado:

1. Nuevo `docs/validation/pre_g9b_cu7_reduced_scope_candidate.md`: inventario del perímetro implementado, exclusiones, evidencia histórica por cohorte, decisión de reducción recibida y estado técnico candidato.
2. Nuevo `docs/validation/pre_g9b_cu7_deferred_capabilities_register.md`: convertir las obligaciones propuestas del estudio §10 en registro, fijando fase propietaria y condición de reconsideración. No inventar persona responsable ni calendario; los propietarios son líneas de trabajo futuras explícitas.
3. `docs/architecture/pre_g9b_cu7_i_increment_proposal.md`: añadir disposición fechada posterior y enlazar candidato/registro. Preservar todo el historial y la matriz de garantías.
4. `docs/adr/0041-locus-v2-point-placement-and-point-redefinition.md`: añadir solamente una enmienda de alcance/estado basada en la decisión expresa. No cambiar reglas matemáticas, de identidad, persistencia o aprobación técnica M08-S.
5. `docs/roadmap/geocedg_roadmap.md`: conciliar la fila inicial «Disposición CU7 vigente» y el hito CU7 pertinente para mostrar M08-S aprobado, el perímetro reducido y las exclusiones. No reformular todo el roadmap.

Artefactos generados propios: logs, reportes, diff, manifiesto de identidad y comprobación de enlaces/formato, en directorio nuevo externo al candidato. Cambios incidentales no entran en el conjunto editable.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Código productivo o experimental nuevo integrado, APIs, comandos, UI, kernel, codecs, modelos, exportadores, packaging, dependencias e infraestructura/registry.
- Cambiar specs M08/M09 u otros contratos científicos, predicados de retención, grados de integridad, claims, barreras F/T/transversal, versiones, migración o autoridad documental.
- Modificar guías o `geocedg_book`; ejecutar CU10/E1 o reordenar obligaciones editoriales.
- Reabrir M08-S-R2, corregir atajos A0 o implementar el paquete alternativo D-AFFINE-CLOSED.
- Rebautizar una composición manual como M08-D implementado; declarar nuevos rechazos tipados que el producto aún no ofrece.
- Rechazar por este paquete las redefiniciones de fuente actualmente admitidas.
- Mutar cualquier archivo cuyo basename sea `claude.md`, sin distinguir mayúsculas/minúsculas; también preservarlo en staging y scripts.
- Publicar ramas, merge/promoción a main, push, tags/releases, force-push, reset destructivo, modificación/eliminación de evidencias históricas o de worktrees ajenos.

## Architectural placement

Documentación de planificación/estado y operación. No hay verdad geométrica nueva. El kernel sigue siendo autoridad de geometría/identidad/DAG; el registro de verificación conserva su autoridad ejecutable. Este prompt no crea un sistema paralelo de aceptación.

## Required design/specification

Aplicar el paquete CU7-RS del estudio aprobado y la decisión expresa de reducción. El contenido de cada enmienda debe decir qué parte se implementó y cuál sigue fuera. Registrar la autorización recibida como hecho histórico; no convertirla en aprobación técnica del candidato aún no revisado.

La primera entrada del candidato debe fijar, sin estado mutable de aprobación futura:

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT
PRODUCT_PHASE_EFFECT = NONE
```

El documento puede citar una decisión previa de alcance, identificada y fechada. El cierre/aprobación posterior del candidato exacto pertenecerá a otro registro, fuera de esta ejecución.

## Geometric invariants and degeneracies

No se modifican. Preservar los contratos listados en §3 del estudio: DA-5; separación objeto/definición/raíz; roles/grupos; ordinary y durable DAG; P3-R1; no aproximación silenciosa; Locus/render/world; raíces F/T/transversal; latencia/recuperación legítima; grados/política; Q transitorio en M08-S. No describir como válida una operación actualmente no demostrada.

## Compatibility and serialization

Sin cambios de formato, lector, normal form, identidad, migración, copia o Undo/Redo. Los documentos existentes se cargan bajo las mismas reglas. La garantía es ausencia de delta productivo, comprobada por diff, no una nueva campaña runtime atribuida por documentación.

## Execution sequence and internal gates

1. **RS0:** verificar autoridad/decisión/SHA/base/estado. Preservar trabajo ajeno y las evidencias. Sin reducción expresa, detener la aplicación; comunicar CU7 abierto, sin sustituirla por cierre parcial global.
2. **RS1:** redactar inventario y deudas en los dos documentos nuevos, con categorías exactas del estudio. M08-S ya aprobado; M09 singleton bloqueado, I2G diferido e incompletitud M09 expresa, I3 bloqueado, D afín diferido viable e independiente, D general bloqueado. No implementar.
3. **RS2:** añadir disposiciones mínimas en las tres autoridades de estado indicadas. Usar enmiendas fechadas; no borrar historia. Denominación candidata: **CU7 — alcance reducido M08-S-R2**. No escribir CU7 completo, M09 implementado o PASS global incondicional.
4. Revisar coherencia bilateral de enlaces y exclusiones. Ningún `PROP-DEBT-*` se vuelve una capacidad; conservar R/K/D/U como obligaciones de paquetes futuros, no marcarlas PASS.
5. Crear un commit local completo dentro del perímetro, exigir `git status --porcelain=v1` vacío y guardar `git rev-parse HEAD 'HEAD^{tree}'`. Ejecutar **RS3**, comprobaciones documentales y STATIC una vez sobre ese commit limpio. Comprobar también el diff completo desde el SHA del estudio revisado; un diff de trabajo vacío no valida los cambios ya comprometidos. Clasificar fallos antes de cambiar nada. Un hallazgo documental se corrige dentro del perímetro y exige identificar su nuevo candidato; un defecto de producto queda fuera. No repetir campañas pesadas ni instalar herramientas para forzar éxito.
6. **RS4:** entregar candidato y evidencia para revisión independiente posterior. No ejecutar el propio cierre autoral ni promoción. La revisión debe examinar la reducción explícita, vigencia temporal, inventario, gates/deudas conservados, alcance exacto del diff y fidelidad de la evidencia.

Las puertas resueltas dentro del paquete no requieren decisiones autorales repetidas. Una precondición fallida no permite ampliar el paquete, convertirlo en M09/D o adoptar normativa geométrica.

## Required tests and commands
<!-- geocedg-field: required_checks -->

Desde el worktree del candidato, resolver las rutas reales y usar un directorio de evidencia nuevo:

```powershell
git status --short
git rev-parse HEAD 'HEAD^{tree}'
git diff --check <sha-del-estudio-revisado> HEAD
git diff --name-status <sha-del-estudio-revisado> HEAD
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo-externo>
```

Conservar comandos exactos con valores resueltos, exits, stdout/stderr y el informe tipado. Comprobar archivos nuevos guardados: sin espacios finales/conflictos, salto final y enlaces relativos existentes. Las comprobaciones auxiliares no sustituyen STATIC; no escribir tests tautológicos de texto ni cambiar los checkers.

STATIC es la selección pertinente para este delta documental. No ejecutar INFRA_UNIT si no se altera infraestructura, ni PHASE/INTEGRATION/FINAL o producto por inercia. `DEV`/`OPERATIONAL` incompletos no son sustitutos. PlanOnly no es aceptación. Si una autoridad activa o un delta real exigiera nivel mayor, presentar razón y detener su ejecución hasta autorización aplicable; no invocar secciones archivadas como política vigente.

No se exige un receipt FINAL inexistente para este estudio/disposición documental. El informe STATIC y la identidad del candidato son la evidencia técnica de su alcance. Registrar diagnósticos separadamente de aceptación; no convertir un hallazgo documental en fallo de producto, ni ocultarlo. Closeout no relanza aceptación ni modifica Git.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

El estudio solo ha autorizado preparar este prompt. Su ejecución requiere la decisión expresa descrita en Objective, incluyendo reducción de alcance. Esa autorización permite la conciliación documental y sus comprobaciones; no aprueba el candidato ejecutado, no habilita producto, no reanuda M09 y no autoriza automáticamente ningún otro incremento.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Se permiten commits locales en la rama aislada cuando la ejecución del paquete haya sido autorizada. No push, merge, promoción, tags ni releases. No reescribir commits ya revisados. La posterior aprobación autoral nombrará el SHA exacto; cualquier publicación requiere instrucción separada.

## Acceptance and closeout

Terminar con **CANDIDATO PARA REVISIÓN INDEPENDIENTE**, resultado STATIC explícito y `selfApproved=false`. No codificar una aprobación futura en el candidato congelado.

La revisión y la posterior decisión autoral podrían cerrar **solo CU7 — alcance reducido M08-S-R2**, citando SHA/árbol y evidencia; deberán expresar M09/I2G/I3/M08-D no implementados y sus deudas. Si no se aprueba la reducción o el candidato, M08-S conserva su aprobación propia y CU7 global permanece abierto. Ni diferimiento ni I5 equivalen a implementación.

## Required artifacts

- Los dos documentos nuevos, tres enmiendas de estado y diff mínimo.
- Inventario de fuentes inspeccionadas/cambiadas y obligaciones R/K/D/U no ejecutadas.
- Informe STATIC, comando/exit/log, aceptación/cobertura/diagnósticos, commit/árbol y hashes en manifiesto externo.
- `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`, con razón: ningún requisito/instalación/runtime/entorno cambia; `VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`; `GUIDE_IMPACT=NONE` por delta de estado, manteniendo CU10/guía/E1 y deudas editoriales.
- Informe de entrega con limitaciones y omisiones; separar evidencia histórica aprobada de validación documental nueva.

## Stop conditions

Detener ante: ausencia o ambigüedad de decisión de reducción; SHA/base incompatible; pérdida de evidencia; necesidad de cambiar una ruta o regla fuera del perímetro; delta productivo; candidato final sucio o identidad incoherente; verificación incompleta/no confiable; solicitud de mutar `CLAUDE.md`; intento de atribuir PASS de producto por PlanOnly/lectura; push/promoción no autorizados. Un fallo de permisos/entorno se diagnostica y puede usar escalación gestionada; nunca se disfraza de defecto geométrico ni se corrige cambiando producto.
