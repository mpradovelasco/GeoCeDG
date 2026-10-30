# CU7-I1 — M08-S: candidato de implementación

Estado: **CANDIDATE — PENDING AUTHOR REVIEW**, 2026-10-30.
No PASS autoral, no publicación. Rama `phase/pre-g9b-cu7-i1-m08-s`.
Base: main publicado `caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4`, árbol
`ca8ec1dd869ce8acc9d3c271036f11b6ee878c24`, worktree limpio al crear la rama.
No se ha incorporado código de ramas de investigación M09. El tracking local origin/main
coincide; consulta de red inicial no disponible por resolución DNS, sin alterar Git/configuración.
La consulta posterior read-only `git ls-remote origin refs/heads/main`, fuera del sandbox,
terminó con exit 0 y confirmó caf67ffc. Main y tracking local permanecen intactos.

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

### Intentos conservados y conciliación de catálogos

Primer candidato técnico: `c82e16f67138916ff80b1bc3101844136f8d012e`, árbol
`2d51c224585bdde852d749e936d0a09250c8f13a`. STATIC ACCEPTED/COMPLETE y PHASE
ACCEPTED/COMPLETE (52 shared + 5 Desktop, sin fallos/omisiones). INFRA_UNIT rechazado
por contadores fijos de selección anteriores al registro I1. INTEGRATION
`verification-7bcd899a3d414b759b8b466c46c91a28` REJECTED_VERIFICATION_CORE/UNTRUSTED:
shared 7517 tests, 0 fallos, 10 omisiones preexistentes; catálogo aún esperaba 7514.
Desktop 2265 tests, 3 fallos y 1 omisión: los tres fallos corresponden a índices/jerarquía
de guías, por la nueva cabecera añadida. Estos resultados no se relabelan como aceptados.

Correcciones: integrar M08-S como párrafo de §8.1 sin alterar el índice protegido; derivar
el catálogo shared global con el productor ejecutado y `update-verification-junit-inventory.ps1`;
actualizar las aserciones de cantidades 70→71 selecciones y 72→73 fases por el registro I1.
No se alteran geometría, tolerancias, allowlists de omisión ni reglas del verificador.
Durante la conciliación focal se detectó también hash de catálogo calculado sobre CRLF;
se normalizó LF y se utilizó la función canónica de hash. El posterior chequeo focal detectó
el segundo contador PHASE antiguo, corregido explícitamente. No fueron defectos del kernel.

La evidencia inicial queda en `artifacts/pre-g9b-cu7-i1/{static,infra-unit,phase,integration}`;
la verificación del candidato conciliado se guarda en `artifacts/pre-g9b-cu7-i1/final-2/`.
Sus recibos identifican SHA/árbol y resultado real; no se incrusta un hash autorreferencial.
La fuente productiva y los tests Java permanecen como en c82e16f; esta conciliación modifica
documentación, procedencia y catálogo/fixture de verificación. La API sigue las reglas
normales del hilo de construcción del kernel; no ofrece una transacción concurrente M09.

El foco correctivo de las guías se ejecuta por `gradle-command-evidence-producer.ps1`
con filtros PreG9BR5AGuideOutlineTest y PreG9BR6PlusGProductMetadataTest: hijo exit 0,
evidencia `artifacts/pre-g9b-cu7-i1/guide-focus/process.json`. Es un foco, no aceptación.
El test puro del catálogo actualizado termina con 8 casos, 576 aserciones y 0 fallos.
El STATIC de trabajo en repair-static tampoco sustituye la verificación del commit limpio.

Comandos canónicos del candidato conciliado, cada uno en su directorio nuevo:

```powershell
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/pre-g9b-cu7-i1/final-2/static
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts/pre-g9b-cu7-i1/final-2/infra-unit
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU7-I1 -LogDirectory artifacts/pre-g9b-cu7-i1/final-2/phase
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/pre-g9b-cu7-i1/final-2/integration
git diff --check HEAD^ HEAD
```

Los resultados de esos comandos están en sus verification-result.json; ninguna aprobación
autoral se deduce de ellos. La comprobación de integridad de INTEGRATION recorre también
artefactos históricos protegidos; no se eliminan para reducir la campaña.

## Smoke manual del autor — NO EJECUTADO

1. Abrir GeoCeDG con Locus V2 habilitado. Crear con la herramienta de deslizador `h`, rango
   [-1,1], paso 1, valor 1. Introducir `A=(-2,1/h)`, `B=(-2/3,1/h)`, `C=(2/3,1/h)`,
   `D=(2,1/h)`, `S=SplineV2({A,B,C,D},3)` y `Q=(0.25,1)`.
2. Con la herramienta Punto, pulsar sobre S cerca de Q. Anotar el nombre del nuevo punto
   (puede renombrarse P). Inmediatamente, un Undo debe quitar solo ese punto y Redo
   restaurarlo; realizarlo antes de las ediciones siguientes.
3. Con Mover, desplazar Q fuera del locus: P permanece en su dirección inicial. Desplazar
   el deslizador h a -1: S cambia y P se recalcula con él. Parar en h=0: fuente/punto quedan
   indefinidos, sin coordenadas antiguas presentadas como válidas. Volver a h=1: comprobar
   recuperación de la misma dirección. No redefinir soporte/conductor para este ensayo.
4. Crear `CA=(-1,0)`, `CB=(0,1)`, `CC=(1,0)`, `CD=(0,1)`, `CE=(-1,0)`,
   `Cross=SplineV2({CA,CB,CC,CD,CE},3)`. Pulsar con Punto en (0,1); si se ofrecen S y Cross,
   elegir Cross. Deben ofrecerse direcciones diferentes (parámetros próximos a .25 y .75),
   aunque las posiciones coincidan. Cancelar: no aparece un punto nuevo. Repetir eligiendo
   explícitamente una dirección; Undo/Redo debe retirar/restaurar solo el punto creado.
5. En estado válido, guardar por la ruta nativa .ggb/.cedg, cerrar y reabrir. Comprobar
   definición/dirección y recomputación. Renombrar no debe reasociar direcciones. La igualdad
   exacta de ID se comprueba automáticamente en JUnit, no por igualdad de etiquetas en pantalla.
6. Abrir un documento anterior y otro sin asociaciones: no migración automática ni cambio
   de política; las rutas Point existentes conservan sus contratos. Si Q ya fuera antecedente
   del propio locus, su efecto a través de L seguiría siendo legítimo: S no elimina aristas reales.

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
