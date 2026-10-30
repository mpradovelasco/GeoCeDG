# CU7-I1 — M08-S: candidato de implementación

Estado: **CANDIDATE — PENDING TECHNICAL VERIFICATION AND AUTHOR REVIEW**, 2026-10-30.
No PASS autoral, no publicación. Rama `phase/pre-g9b-cu7-i1-m08-s`.
Base: main publicado `caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4`, árbol
`ca8ec1dd869ce8acc9d3c271036f11b6ee878c24`, worktree limpio al crear la rama.
No se ha incorporado código de ramas de investigación M09. El tracking local origin/main
coincide; consulta de red inicial no disponible por resolución DNS, sin alterar Git/configuración.

## Disposición autoral de A0 y M09

Decisión humana «CU7: cierre experimental de A0 y continuación con M08-S», recibida
2026-10-30. Acepta R3-E como caracterización, no como transacción segura ni aprobación
de sus puertas. Expediente conservado en commit `249934d09cb313e39f36b0dd79d288b7c0b800d7`,
árbol `44042542d755224e89263fc9a10f3fab6aa297fc`, rama `codex/pre-g9b-cu7-i2-r3-e-a0`.
Informe histórico: `docs/validation/pre_g9b_cu7_i2_r3_e_a0_experiment_report.md` en ese commit.
No se copia/reinterpreta retrospectivamente su evidencia en esta rama de main.

El cambio mecánico de token dentro del mismo resultado es posible. La operación
transaccional segura no está demostrada. A0 sigue candidato no autorizado; M09 permanece
parcialmente diseñado y pendiente de implementación segura. Ninguna puerta anterior se
considera superada. R1/R2/R3-D/R3-E quedan conservados en sus commits/rama, sin cambios.
`DEBT-CU7-GENERAL-REDEFINE-RECONSTRUCTION` sigue como propuesta futura separada, sin fase,
fecha ni código autorizado; no es prerrequisito de M08-S. No aplicar la enmienda ADR 0026.

## Puerta técnica previa y diseño mínimo

Clasificación: implementación I1 con inspección/validación; semántica en kernel Java
compartido, Desktop consumidor. Autoridad: ADR 0041, contrato M08 aprobado prospectivo,
ADR 0019 y contratos vigentes de interacción/persistencia. No introducir geometría en UI.

Antes de la edición productiva se inspeccionaron `LocusV2PublicOperations`,
`AlgoSemanticLocusPoint2D`, query/resolver/result/candidate de interacción, codec de dirección,
`GeoCeDGPointInteraction` y controller, y pruebas R6/archivo nativo G9U1. Resultado estático:

| Condición de entrada | Resultado y mecanismo existente |
|---|---|
| Forward expresable | sí: createInteractiveSemanticPoint → createSemanticPoint → AlgoSemanticLocusPoint2D |
| Identidad/parametrización persistibles | sí: ID/rol R6 y LocusSemanticAddressState2D, branch/componente/parámetro/lift/seam; normal form Point existente |
| Sin nueva redefinición | sí: creación de un punto nuevo, sin reemplazar target, productor ni dependientes |
| Referencia separable | sí: query contiene x/y primitivos; forward solo source y entradas de dirección dedicadas |
| Ambigüedad/degeneración controlables | sí: estados del resolver; elegir candidato explícito en MULTIPLE, nunca promover UNRESOLVED; forward revalida componente/revisión |

Esto permite implementar la composición; no declara superadas pruebas de producto antes
de ejecutarlas. Se conservan grados de evidencia: unique exige cobertura del scope declarado,
no del universo de cualquier locus; una elección múltiple admisible no afirma completitud
global. Las búsquedas inseguras/no resueltas no se vuelven admisibles por confirmación.

Nuevo `LocusSnapshotPlacement2D.Selection`: consulta emitida por kernel, resultado/evidencia
inmutables, elección de miembro de esa consulta y consumo único. Rechaza elección ajena,
estado no admisible, fuente retirada/cargada de nuevo o revisión distinta antes de crear.
La comparación de instancia de definición es invalidación efímera, jamás identidad durable.
No implementa generación universal de mutaciones ni contrato M09. Cancelar no crea objeto.
Tras commit solo persiste la construcción forward; no consulta, residual, Q ni selector UI.

Desktop compone esa operación compartida con su chooser actual. Se conserva el controlador
y su única frontera Undo por gesto (incluido tratamiento previo de modal que consume release).
No comando snapshot de tres padres, no formato/ID nuevo, no dependencia ficticia de Q.
Headless consume query/commit explícitos; quien ya tiene dirección sigue usando Point forward.
No se habilita M08-D, M09/A0, I2G/I3, reconstrucción general ni nueva autoridad/STRICT.

## Matriz y aceptación

VERIFICATION_CLASS=INTEGRATED_PHASE. PHASE propuesta PRE-G9B-CU7-I1, registro tipado y
catálogo derivados mediante productores oficiales: discovery shared/Desktop --test-dry-run,
ejecución focal y update-verification-junit-inventory.ps1. Ningún hash/recuento supuesto
se acepta como evidencia. INTEGRATION añade regresión global de DAG, identidad, codecs,
constructive authority y consumidores que una selección focal de R6 no cubre.

| Caso | Evidencia requerida |
|---|---|
| Unique y evidencia | selección focal R6 + tests CU7 de query/commit |
| Multiple y componentes distintos con mismo valor | R6 self-intersection/branch/seam y consumidores Desktop; elección explícita |
| Referencia inicial cambia | cu7SnapshotDoesNotDependOnReferenceAndSurvivesReopen; no parent Q |
| Locus cambia/stale | cu7SnapshotCancelAndStaleChoiceDoNotMutate y R6 r622 |
| Foreign/cancel/consumo | tests CU7; comparar XML antes/después de rechazo |
| Discontinuidad/indefinición temporal | suite R6 de componentes, gaps, collapse/recovery; sin reasignación cercana |
| DAG/ciclos/compatibilidad | inputs forward existentes y regresión integrada; Q descendiente no se inserta como padre |
| Identidad/Undo/Redo/reopen | R6 r613–615, CU7 reopen y Desktop G9U1 native lifecycle/archive |

Resultados reales y SHA/árbol congelados se entregan en recibos bajo
`artifacts/pre-g9b-cu7-i1/`. No declarar aceptación integrada por un focal ni por STATIC.
Un fallo de entorno se separa de regresión productiva y se conserva con sus logs/exits.

## Smoke manual del autor — NO EJECUTADO

1. Abrir GeoCeDG con Locus V2 habilitado; construir una SplineV2 recta y seleccionar su
   punto próximo con herramienta de punto. Confirmar punto definido y un solo Undo.
2. Construir spline auto-intersecante; solicitar punto en el cruce. Revisar las direcciones
   ofrecidas, cancelar y comprobar ausencia de punto/Undo; repetir eligiendo explícitamente.
3. Mover la referencia geométrica inicial: el punto creado conserva su dirección. Mover
   entradas del locus: se reevalúa según dirección, no se vuelve a proyectar la referencia.
4. Cruzar una degeneración/pérdida de componente admitida por fixture R6: comprobar estado
   indefinido explícito y recuperación únicamente de la misma dirección admisible.
5. Undo/Redo creación; guardar nativo .ggb/.cedg, cerrar/reabrir y comprobar dirección e ID
   mediante inspector/diagnóstico. Renombrar objetos no debe reasociar direcciones.
6. Abrir documento anterior y otro sin asociaciones: no migración automática ni nueva
   política geométrica; las rutas Point existentes conservan sus contratos.

## Impacto y límites

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: ningún JDK/runtime/Gradle, instalación, requisito
numérico o packaging cambiado. VERIFICATION_INFRASTRUCTURE_IMPACT=BOUNDED_CATALOG_REGISTRATION:
solo selección, derivación de catálogo y hash del registro; no cambio del runner/proyecciones.
Verificación STATIC/INFRA_UNIT pertinente además de PHASE e INTEGRATION; nunca FINAL por
inferencia de esta autorización. GUIDE_IMPACT=UPDATED: aclaración M08-S en guías EN/ES.
CU10 conserva integración/empaquetado de guía; E1/editorial y
DEBT-CU3-EXACT-TANGENCY-GENERALIZATION intactos. Libro sin cambios.

Limitaciones: descubrimiento soportado por resolver actual, radio world explícito,
sin proyección exacta garantizada de Q; sin comando nuevo de geometría dinámica.
La elección persiste una dirección, no la prueba de que Q pertenece a L o sea su mínimo global.
M09 sigue pendiente y no se declara implementado. Detener tras entregar candidato.
