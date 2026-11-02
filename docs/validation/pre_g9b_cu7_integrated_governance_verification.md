# CU7 integrado — Gobernanza, cierre y cobertura de verificación

Fecha: 2026-11-02, Europe/Madrid. Base inspeccionada: `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17`. Anexo de investigación candidato, sin implementación ni adopción normativa. `selfApproved=false`. Las filas futuras de este anexo son obligaciones de un eventual encargo, no resultados PASS.

## 1. Dictamen de autoridad y sucesión

La autorización histórica I2a/I2b de 2026-10-29 no autoriza reanudar hoy M09. La disposición posterior del 2026-10-30 detiene A0 y la reanudación de I2a/I2b, prioriza M08-S y conserva la reconstrucción general como deuda. La enmienda del 2026-11-02 y el registro canónico cierran exclusivamente M08-S-R2 y reiteran el bloqueo de M09. Esto consta en `docs/architecture/pre_g9b_cu7_i_increment_proposal.md:3`, `:131`, `:140`, `:144`, y `docs/adr/0041-locus-v2-point-placement-and-point-redefinition.md:158`, `:175`. La continuidad del estudio autoriza concluir la investigación; no convierte esas exclusiones en autorización productiva.

El estado verificable es:

| Perímetro | Estado actual | Lo que no acredita |
| --- | --- | --- |
| CU7-D | Diseño y contrato prospectivo aprobados sobre `7e7a90e4e39db04ea49ee9ab1acf69ea34c81080`, árbol `25e0e2f0a78670c31be39766021d3f7561bc3bf6` | Implementación M09, atomicidad demostrada o autorización vigente para reanudarla |
| M08-S-R2 | PASS — AUTHOR APPROVED sobre `86438edabaed06a6509c1be37cf06278941e0e46`, árbol `96e90172201440739bfe8afb503059379c70ff70` | M08-D, M09, I2G/I3, cierre CU7, arrastre general o selector configurable de ramas/componentes |
| M09 singleton | Diseñado prospectivamente, caracterizado, no implementado con garantías completas | Ni A0 ni el éxito de JUnit en sondas negativas equivalen a transacción segura |
| I2G / I3 / M08-D | Contratos y puertas de entrada propias pendientes | No se habilitan por aprobar singleton ni por disponer de creación M08-S |
| I5 global | No aprobado | Revisar un perímetro no borra diferimientos; el estudio no cambia alcance autoral |

Fuentes: `docs/validation/pre_g9b_cu7_d_closeout_record.md:3`, `:31`; `docs/validation/pre_g9b_cu7_i1_m08s_r2_closeout_record.md:3`, `:19`, `:61`; propuesta de incrementos, enmienda final. El registro M08-S-R2 atribuye 196 ejecuciones PHASE, tres pruebas nativas y once omisiones históricas a su candidato técnico; no se repiten ni se atribuyen al presente estudio.

Hay una inconsistencia documental visible: `docs/roadmap/geocedg_roadmap.md:8` sigue describiendo el checkpoint anterior de M08-S y `:4347` registra el cierre sucesor. La fecha y el registro canónico resuelven el estado. Corregir el resumen del roadmap sería una actuación documental futura acotada; este anexo no reescribe estados históricos ni declara aprobada una reducción CU7.

## 2. Contrato operativo que está activo

La cabecera de `geocedg/specs/operations/verification-levels.md:1`–`:63` es **ACTIVE RECOVERY CONTRACT**. Desde `:65` se conserva el contrato pre-recovery archivado; el propio documento prohíbe usarlo en lugar del registro/esquemas. No reactivar sus secuencias READINESS/PREPARE/FINALIZE ni su universalidad FULL mediante una plantilla histórica. Los prompts canónicos actuales y el `verify.ps1` real prevalecen como autoridad ejecutable.

Aceptación y diagnósticos son independientes. Solo `SEMANTIC`, `SAFETY` y `VERIFICATION_CORE` afectan aceptación; todo resultado semántico identifica PRODUCT/SCIENTIFIC/MIXED. Documentación, gobernanza, historia, estilo y duración son diagnósticos visibles. Un productor de proceso es neutral: preserva ejecución; la proyección pura interpreta su evidencia. `exit=0` de una sonda que demuestra un fallo arquitectónico no satisface la puerta cuya garantía refuta. Véanse `verification-levels.md:10`, `:17`, `:40`; `.github/prompts/canonical/verification.prompt.md:24`; esquema del registro `verification-registry.schema.json:22` y `:25`.

`verify.ps1:123` emite el plan y sale antes de la captura de entorno/identidad de ejecución que comienza en `:128`. **PlanOnly no ejecuta aceptación, no produce receipt y no prueba compilación, tests, runtime, smoke ni equivalencia de resultados.** El plan resuelve cobertura y dependencias declaradas, no inventaría casos nuevos de CU7 que todavía no figuran en él.

### 2.1 Qué corresponde a este estudio documental

Clasificación: planificación/diseño con entregables documentales expresamente autorizados; capa de investigación/arquitectura/operación, sin efecto de producto o formato. El perímetro mínimo son el informe integrado, sus anexos y el prompt futuro. Validación apropiada: una ejecución canónica `STATIC` al terminar, seguida de comprobación de diff y estado. No hacen falta INFRA_UNIT ni campañas de producto si no cambian infraestructura, código, tests, tolerancias o fixtures. La inspección de planes puede precederla.

```powershell
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo-del-estudio>
git diff --check
git status --short
```

No se propone `AUTHOR_DIRECT`: esa ruta exige una instrucción explícita con alcance/rutas y no se infiere del carácter documental. `VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY` puede describir el impacto, como en el cierre M08-S-R2 actual, sin invocar maquinaria archivada. La documentación del candidato debe fijar hechos, con una decisión autoral posterior en su propio registro; no un campo de aprobación mutable incrustado en el candidato.

Impactos de este estudio: `PRODUCT_PHASE_EFFECT=NONE`; `VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`; `BOOTSTRAP IMPACT — NO CHANGE REQUIRED` porque no cambian prerrequisitos, herramientas, runtime, compilación o instalación; `GUIDE_IMPACT=NONE` sobre comportamiento disponible. Las obligaciones de integración de guía/CU10/E1 se conservan. Es una conclusión justificada, no una petición de editar bootstrap arbitrariamente.

### 2.2 Qué exigir a una futura implementación

Antes del código, la autorización debe nombrar base SHA/árbol, capacidades, exclusiones, contrato/ADR aplicable, archivos/subsistemas, clase de impacto y cobertura. Debe preparar la selección PHASE por el procedimiento canónico y registrar sus pruebas con las proyecciones puras requeridas. El nombre del encargo o de la rama no es un selector ejecutable.

En el registro actual están G9A3, POST-G9U1-A3 y A3-FRONTEND, G9U0-R6, PRE-G9B-R6, PRE-G9B-CU3 y las fases M08-S-R2, entre otras. No hay selección PHASE M09/I2 ni CU7 integrado. `DEV` está INCOMPLETE (`dev.focused-semantic-selection`) y `OPERATIONAL` también (`operational.remaining-pure-coverage`), según `verification-registry.json:14`–`:15`. No sustituir una fase pendiente por M08-S ni modificar infraestructura únicamente para hacer viable este estudio. `verification-registry.psm1:512` rechaza una fase ausente y `:529`–`:581` resuelve cada ID una vez.

Una futura extensión acotada se puede proponer con PHASE propia e INTEGRATION si esta añade obligaciones concretas, como exige el diseño CU7-D; la invasividad real puede justificar una autorización distinta. Si termina exigiendo infraestructura general host/Undo/reconstrucción o una obligación global, detener el perímetro acotado, explicar el delta y solicitar nueva decisión. La recomendación no debe esconder ese coste bajo la palabra singleton. No ejecutar FINAL automáticamente por prudencia ni rebajar una obligación científica por permanecer bloqueada otra capacidad.

La campaña de desarrollo debe ser focal y repetir solo los casos afectados por cambios nuevos. La campaña final se hace una vez sobre candidato congelado y debe cubrir el perímetro autorizado completo y sus regresiones. La política activa de FINAL exige candidato inmutable, evidencia completa/confiable y receipt válido; no se fabrica por copiar resultados históricos. Closeout solo inspecciona identidad, no reejecuta, muta Git o registra aprobación. La promoción requiere instrucción autoral separada. Packaging, binarios de lectores previos y smoke interactivo no se dan por ejecutados a partir del plan o de JUnit.

## 3. Planes realmente resueltos en esta continuación

Se ejecutaron únicamente estas dos resoluciones de plan en la base actual, con PowerShell sin perfil. Los archivos son evidencia generada ignorada, no fuentes normativas:

| Comando | Exit | Nodos | execution_plan_hash | Salida guardada |
| --- | --- | --- | --- | --- |
| `& tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly` | 0 | 46 | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` | `artifacts/pre-g9b-cu7-integrated-reassessment/continuation-20261102/plans/integration-plan.json` del repositorio principal |
| `& tools/agent/verify.ps1 -Profile FINAL -PlanOnly` | 0 | 68 | `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517` | `artifacts/pre-g9b-cu7-integrated-reassessment/continuation-20261102/plans/final-plan.json` del repositorio principal |

Los originales se produjeron en `artifacts/cu7-integrated-reassessment-staging/` del repositorio principal; sus copias archivadas en el directorio `plans/` indicado conservan bytes/hashes. No se atribuye su ejecución al worktree del estudio. Los respectivos `*-plan.stderr.log` están vacíos; `*-plan.exit.txt` conservan `0`. La advertencia externa `InitializeDefaultDrives` se emitió por la inicialización restringida de PowerShell, fuera de las salidas capturadas del script; no se convirtió en defecto de GeoCeDG.

`plan-comparison.json` conserva `missingIntegrationInFinal=[]` y `changedCommonNodeDefinitions=[]`: los 46 nodos de INTEGRATION figuran con la misma definición completa en FINAL. Se compararon IDs y serialización de cada nodo común, no solo cantidades. Esto sustenta planear una única FINAL que subsuma integración **si una futura autorización exige FINAL y si, tras registrar sus nuevas pruebas/fase, se comprueba nuevamente toda la contención**. No demuestra hoy cobertura futura M09/I3/D ni autoriza FINAL en el estudio. Si el plan autorizado necesita solo PHASE+INTEGRATION, no se sustituye automáticamente por FINAL.

No se ejecutaron STATIC, INFRA_UNIT, PHASE, INTEGRATION o FINAL por este agente. La ejecución documental del estudio será responsabilidad de su coordinador sobre los archivos finales del worktree, con su propio comando/exit/log/identidad.

## 4. Matriz integrada: obligaciones comunes y específicas

Fuente canónica de los casos: `docs/validation/pre_g9b_cu7_d_validation_and_implementation_gates.md:33`–`:67`. La matriz siguiente distribuye garantías para evitar tres errores: considerar compartida una implementación por compartir un nombre; repetir toda integración por incremento; omitir una obligación de M08-D porque M09 esté bloqueado. **Todas son pruebas futuras y ninguna queda PASS por esta tabla.**

Abreviaturas de perímetro: **T** = contrato transaccional común candidato M09/I2G/I3; **P** = transiciones singleton; **G** = productor/grupo; **C** = soporte/conductor/correspondencia de I3; **D** = mapa dinámico M08-D; **L** = lifecycle de cada familia; **X** = regresión integrada. Compartir T supone reutilizar el protocolo y oráculos, no afirmar que hoy exista su implementación suficiente. D puede necesitar publicación atómica de su propia creación sin depender de reconstrucción general M09.

| Caso | Cobertura y fixture indispensable | Oráculo que no puede heredarse de otra puerta |
| --- | --- | --- |
| R01 | P/T; P distante, ordinarios y durables downstream | DA-5 sin coincidencia; conservar ID solo por predicado y recomputar cierre completo |
| R02 | P/T; VALUE/address/token, dos propietarios R | Release/retain una vez, sin transportar token; definición e ID P evaluados separadamente |
| R03 | P; rich result presentado como punto | Selector explícito o rechazo de familia; no coerción a punto por clase/render |
| R04 | P/T; cuarentena/latencia, token ajeno/forjado, contacto clase 3 | Fallo preserva libros/XML; admisibilidad no se promueve por confirmación |
| R05 | P; raíz local válida con global NOT_ESTABLISHED | Mantener alcance de admisión; no declarar completitud global |
| R06 | P/C; F/T con prueba y sin prueba | Grados CU3 reales; prueba de punto crítico no prueba intersección |
| R07 | P/G; interacción/principal/proyección | Compatibilidad por contrato/rol; Java class no basta; replacement independiente |
| R08 | T/P; target sin ID y helpers participantes | Snapshot antes de parseo; ID nuevo solo al commit; falla sin publicar objects/IDs |
| R09 | T/G; productor con dos salidas, hermanos con/sin ID y consumidores | Singleton rechaza antes de borrar; G solo admite mapping completo probado; M09 incompleto si G se difiere |
| R10 | T/P/G/C; dependencia nueva posterior al target | Mismos testigos P3-R1 y orden relativo no afectado; no cualquier sort topológico |
| R11 | T/P/G/C; ciclos directos/indirectos mediante helpers y fuentes | Rechazo antes de publicar; host DAG y grafo durable validados sobre el mismo overlay |
| R12 | P/T; operación válida degenera una recta downstream | Commit admisible y consumidor undefined explícito; ninguna nueva identidad por cercanía |
| R13 | T/P/G/C y creación D; cambio entre assessment y commit/cancel | Epoch/lease e invalidación ABA efectivos; sin Undo extra ni reuso; no transportar vigencia M08-S sin prueba |
| R14 | T/P/G/C; fallos en reserva, instalación, reconstrucción y postcondición | S0 completo: definiciones, instancias si contrato lo exige, IDs, revisiones, claims, cuarentena, high-water y Undo; fatal tipado si restaurar es imposible |
| R15 | T/P/G/C; ordinary+durable con reemplazo y cascada | Impacto distingue recomputado/undefined/borrado y es completo; incompleto no ofrece replacement |
| R16 | P/T; solicitud de operación nueva I3 no soportada | Rechazo tipado sin alterar XML/IDs/claims/Undo; preservar rutas antes válidas |
| K01 | C/T; nuevo soporte N, recién participante y ya participante | Registros y DAG prospectivo concordantes; no enrollment oportunista tras mutar |
| K02 | C/T; conductor nuevo misma familia e incompatible | Retención N/L/P distinta de continuidad de soluciones; roles incompatibles no RETAIN |
| K03 | C; soporte/driver con geometría igual y IDs distintos, F/T/transversal | Barrera efectiva en cada familia por selector/procedencia, nunca coordenadas iguales |
| K04 | C; cambiar valor/definición del soporte en sitio y regresar | Latencia/recuperación legítimas del mismo selector completo; no retiro por valor distinto |
| K05 | C; split/merge, orientación, seam, contacto→pareja | ADR0017/0039/0040, claims y retiro/cuarentena; sin transferencia entre familias |
| D01 | D; afín finita cerrada no colapsada, Q interior/exterior | Derivación ideal y realización aritmética separadas; clamp solo con extremos incluidos |
| D02 | D; extremo abierto, mínimo ausente, A=0/no finito | Causa tipada de inexistencia/degeneración/capacidad; no fabricar proyección |
| D03 | D/L; caminos directo/incremental/inverso/reopen | Mismo mapa/resultado desde mismo snapshot; historial/caché no eligen u |
| D04 | D; mínimos múltiples, estacionarios y distancia constante | Ambiguous/unsupported/map-not-established; no winner por distancia previa |
| D05 | D; pliegue/singularidad/seam/monodromía | Receta conserva identidad, binding inválido y punto undefined; no salto de mapa |
| D06 | D/L; pérdida/recuperación componente/prueba y caché vacía | Correspondencia demostrada para misma receta; nunca reemplazo por otro mínimo cercano |
| D07 | D; políticas constructiva/heredada y Q fuera de clase exacta | No STRICT por residuo pequeño o certificado float; preservar autoridad documental |
| D08 | D; escalas/traslación/overflow y presupuesto | Garantía/error veraces y fallo de capacidad tipado, independiente de pantalla |
| U01 | L para cada P/G/C/D admitida; definido y dormante | Guardar/reabrir y Undo/Redo repetidos, IDs/receta/selector; evidencia se recalcula |
| U02 | L; cierre completo y parcial de cada familia | IDs frescos/remap completo, política externa o rechazo para copia parcial |
| U03 | L/T; rename/delete/recreate/ABA | Recreado no impersona ID; selección obsoleta no recupera por etiqueta |
| U04 | G/D; versión nueva/corrupta, lector actual/previo | Lectura soportada o rechazo seguro; ningún descenso a punto libre/snapshot |
| U05 | L; muchos hijos misma revisión, invalidaciones | Reutilización por revisión con contadores deterministas; no resolver global por hijo |
| U06 | X; CU0/CU1/CU2/CU3, Classic, spline, transformaciones, DXF/LaTeX | Semántica/documentos existentes preservados; cohortes/fixtures exactas |

Los casos S01–S10 de creación M08-S y las autoridades R2/H1 permanecen regresión si un delta las afecta; sus éxitos históricos no prueban R13/R14 de redefinición. La selección registrada R2-DESKTOP y CU3/A3/R6 sirven para mapear regresión reutilizable, no como nombre sustituto de una nueva fase.

### 4.1 Campaña común y especializaciones

1. **Preflight de fuente y autoridad:** base/árbol/status; contrato aprobado y perímetro; clasificar fallos en entorno/permisos/contrato obsoleto/fuente faltante/runtime/regresión/ciencia antes de cambiar código.
2. **T focal:** snapshots previos, auditoría de escritores, mixed closure, IDs/high-water, ABA, publicación no observable a medias, fallos/Undo y ausencia de reentrada. Ejecutar fixtures neutrales pequeñas; una prueba que observa solo XML no puede certificar S0 entero.
3. **Especializaciones:** P prueba sus transiciones/roles/claims; G añade todos los hermanos y su reconstrucción; C añade correspondencia por soporte y barreras F/T/transversal; D añade matemática, realización numérica, receta y lectores. No esperar a T para resolver D01/D02 o demostrar la insuficiencia de su certificado actual.
4. **Lifecycle y compatibilidad:** U01–U05 en cada familia admitida; combinar código de arnés cuando sea real reutilización, conservando resultados individualizados por familia y estado.
5. **Integración final:** exactos A3/G9A3, CU3 y R6/M08-S más el corpus afectado U06. El nuevo registro debe mostrar qué obligación corresponde a qué producer/proyección/caso y no solo nombres de módulos.
6. **Smoke:** agente técnico y autor separados. Casos: redefinición distante, cancelación/stale, grupo admitido/rechazado antes de mutar, degenerate downstream, nuevo soporte/recuperación y barreras, definido/dormante tras Undo/reopen/copy; si D incluido, extremos/pérdida de prueba/historia directa e inversa. Pasos no ejecutados se declaran pendientes; ninguna aprobación se infiere del smoke técnico.

Criterios de salida: cobertura completa y confiable del perímetro; negativas que rechazan sin efectos; ningún consumidor obsoleto tras falla; evidencia guardada con comando/exit/identidades. Bloqueo propio de una capacidad detiene su gate; no cancela el análisis o pruebas independientes autorizados de otra. Aceptación técnica nunca concede aprobación o publicación.

## 5. Comparación de estrategias y cierre I5

Esta comparación es de gobierno y coste de garantías; la decisión científica final debe usar también los anexos M09 e I3/D.

| Estrategia | Valor/cobertura | Coste relativo y condición decisiva | Cierre honesto |
| --- | --- | --- | --- |
| A. CU7 completo | Atiende M09 singleton/multisalida, I3 y D, con I5 común | Muy alto: transacción host/Undo completa, grupos y mapas son garantías distintas; mayor invasividad upstream/formatos y amplio smoke. No hay base para autorización productiva inmediata sin resolver entradas | Solo si cada capacidad exigida se implementa y verifica; no convertir diferimiento D en completitud |
| B. CU7 acotado a singleton | Valor de cambiar un P preservando consumidores; excluye grupos/I3/D | Alto, no bajo por cardinalidad: debe demostrar T completo antes de UI. R3-E ya impide tratar setter/XML como solución segura | M09 PARCIAL — I2G pendiente; CU7 global abierto o alcance nuevo expresamente aprobado |
| C. Separar por viabilidad real | Mantiene P/G/C juntos por garantías T y trata D como problema matemático/codec independiente; aprovecha forward/CU3 existentes | Medio-alto para D riguroso propio; alto/muy alto para T+grupos. Evita falsa dependencia de D con reconstrucción M09, sin prometer capacidad nueva antes de sus gates | Cierre por perímetros de nombre exacto; un bloqueo de T no se hace condición ficticia de las alternativas existentes |
| D. Cierre conservador | Conserva M08-S-R2 aprobado y explicita pérdidas, rechazos, deudas y condiciones de reconsideración | Bajo para paquete documental; no añade funcionalidad M09/D. Reversible: no nuevos formatos o APIs; deuda visible con dueños/entrada | Solo cierre reducido si el autor reemplaza expresamente el alcance; sin esa decisión, M08-S cerrado y CU7 global abierto |

Recomendación de gobierno: **D como actuación inmediata verificable, con separación C para el backlog**, si la auditoría técnica no aporta una extensión positiva que cumpla su envolvente completa. No crear una falsa implementación mínima para llenar el formato del encargo. El paquete ejecutable inmediato puede ser documental: registrar la decisión de alcance, armonizar únicamente los resúmenes vigentes, conservar normativa histórica y crear el registro de deuda/puertas. Su prompt debe estar preparado pero no ejecutado en el estudio; una decisión autoral pendiente no se escribe como adoptada.

### 5.1 Cuatro resultados de I5, sin ambigüedad

| Resultado | Condición | Estado que puede declararse |
| --- | --- | --- |
| Completo | Todo el alcance CU7 vigente implementado y evidencia/autoría propias | CU7 completo solo tras decisión autoral exacta |
| Reducido aprobado | Autor redefine explícitamente alcance, exclusiones, pérdida temporal y deudas; se verifica ese perímetro | CU7 con alcance reducido aprobado, acompañado de las capacidades excluidas por nombre; nunca M09/D implementados |
| Parcial con puertas pendientes | Se cierra M08-S u otro perímetro implementado, sin sustituir el alcance global | Perímetro exacto PASS; CU7 global abierto |
| Abierto por bloqueo indispensable | Función exigida no cumple precondición o reconstrucción no compensa ahora | CU7 abierto; bloqueo técnico y decisión pendiente explícitos |

La salida por defecto del presente estudio es **candidato listo para revisión, CU7 global abierto**. La preferencia por D no constituye reducción aprobada. La autorización de implementación futura debe ser una sola decisión informada que nombre subperímetros y gates internos; superar un gate no amplía automáticamente alcance.

## 6. Diferimientos y obligaciones externas

La ficha de cada capacidad diferida debe conservar: pérdida temporal de usuario; contrato que sigue vigente; operación rechazada/ya válida; impacto de compatibilidad; deuda y responsable/puerta; evidencia que permitiría reconsiderar; y relación con el cierre global. Ningún diferimiento implica feature completa ni estado stable.

| Elemento | Conservación obligatoria / condición de reconsideración |
| --- | --- |
| M09 singleton | Sin nueva retención semántica aprobada en runtime; reabrir solo con T demostrado para el perímetro exacto, target sin ID, mixed closure, ABA, claims y Undo |
| I2G | M09 sigue parcial si se admite solo singleton; reconsiderar con adaptador de productor/grupo completo, hermanos y lectores, no con detach supuesto |
| I3 | Conservar rutas ya válidas y rechazar nueva coordinación no soportada; contrato de overlay + barreras F/T/transversal + lifecycle antes de habilitar |
| M08-D | Usar familias existentes solo dentro de sus precondiciones; no llamar dinámica certificada a una composición o snapshot. Reabrir con proveedor/receta/codec y D01–D08/U04 demostrados |
| Arrastre general Locus V2 | `DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG` sigue separada; no se cubre por M08-S-R2 ni por reducir CU7 |
| Restricción configurable de ramas/componentes | Requiere autorización independiente; no convertir selector visual efímero R2 en una política persistente nueva |
| CU10/guía/E1 | `CU10-GUIDE-INTEGRATION-CHECK`, FINAL de CU10 y expediente `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF` se conservan; este estudio no los ejecuta |
| Tangencia exacta general | `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` mantiene su secuencia posterior a CU10 y trabajo editorial; no ampliar STRICT por D o M09 |
| Libro | `geocedg_book` intacto; planificación/incorporación editorial necesitan autorización propia |

Fuentes: propuesta CU7 `:127` y `:140`; gates CU7-D `:129`; cierre M08-S-R2 `:61`; `docs/architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md:37`–`:38`; plan consolidado `pre_g9b_cu_corrective_usability_plan.md:86`.

## 7. Inventario y límites de evidencia

Leídos/consultados desde disco: `AGENTS.md`; prompts canónicos `governance.prompt.md`, `verification.prompt.md`, `tasks/task-template.prompt.md`; `docs/adr/0041-locus-v2-point-placement-and-point-redefinition.md`; propuesta de incrementos CU7; closeouts CU7-D/M08-S-R2; matriz CU7-D; cabecera/sucesor de roadmap y filas focales del plan consolidado y del plan de transferencia científica; `verification-levels.md` (contrato activo y frontera archivada, no activación del archivo); registro/esquema de verificación; `tools/agent/verify.ps1`; resolver `verification-registry.psm1`. El encargo original fue recuperado por el coordinador; este agente no lo sustituyó por un prompt inventado.

Archivos escritos por este agente: este anexo y los planes/logs/exit/comparación bajo `artifacts/cu7-integrated-reassessment-staging/`. No se modificaron fuentes versionadas main, producto, tests, registry, contratos aprobados, CLAUDE.md o libro. Las lecturas amplias con salida truncada se completaron mediante consultas focales de las secciones utilizadas; no se afirma lectura exhaustiva de todo roadmap.

La evidencia nueva de este anexo es **estática y PlanOnly**. Sin ejecución de producto, experimentos nuevos Java, render/UI, tests ni smoke autoral; sin aceptación ni receipt. El coordinador debe registrar por separado STATIC sobre el estudio final y sus limitaciones. La ausencia de nueva campaña de producto es deliberada y compatible con el encargo de estudio sin implementación; no se toma como prueba positiva de funcionalidad pendiente.
