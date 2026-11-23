# GeoCeDG — PRE-G9B-MAINT-S0/S1: sustitución explícita de un punto

**PROMPT CANDIDATO — NO AUTORIZADO.** S0 no tiene requisitos previos más allá de una
instrucción autoral con base exacta. S1 requiere el informe de S0 revisado por el autor, B2 e
I2G0 cerrados, las decisiones D-M09-1 a D-M09-4 y su propia instrucción autoral. S0 y S1 se
autorizan por separado; la implementación de S1 no se aprueba antes de conocer S0. La dirección
de D-M09-1 a D-M09-4 quedó aprobada el 2026-11-23 con implementación pendiente; prevalece el
diseño D §3.0.

## Objective
<!-- geocedg-field: objective -->

**S0** (`BOUNDED_PHASE`, solo pruebas e informe), caracterización previa obligatoria.
Caracterizar, distinguiendo las garantías que el núcleo ya proporciona de las que no están
demostradas, y sin proponer infraestructura transaccional nueva si los mecanismos existentes
bastan para el alcance reducido:

- las operaciones de sustitución disponibles en GeoCeDG y en GeoGebra, y el modo
  `LEGACY_REPLACEMENT` del ADR 0026;
- la identidad y las dependencias de puntos ordinarios y semánticos;
- el estado del DAG y del registro de identidad durante la operación;
- el impacto real sobre dependientes duraderos y ordinarios;
- las referencias por nombre fuera del DAG (guiones GGBScript y JavaScript, textos dinámicos,
  condiciones de visibilidad, colores dinámicos, cadenas de `Execute`, herramientas de
  usuario y macros, subtítulos) y su comportamiento cuando `P'` recibe la etiqueta de `P`;
- las fronteras de la transacción de redefinición: notificación de vistas (P07),
  almacenamiento asíncrono del punto de Undo (P09), arrendamiento de publicación, guarda del
  registro de identidad y borde compartido con B2;
- las condiciones de restauración: qué restaura exactamente la reversión del ADR 0026 (XML,
  objetos Java, registros de identidad, reservas, reclamaciones de token, selección, vistas,
  historial de Undo) y qué no, con inyección de fallos en cada etapa;
- la fusión nativa con un `P'` existente;
- el estado de los hermanos tras una redefinición nativa fallida;
- el impacto anunciado frente al eliminado en la sustitución legada;
- la frecuencia, en los modelos del autor, de puntos aguas arriba de objetos duraderos.

**S1** (`INTEGRATED_PHASE`), incremento de transparencia y seguridad, no solución funcional
completa de M09: operación «Sustituir punto…» con el nivel transaccional que el autor fije tras
S0 y con las siete garantías no negociables del diseño D §3.0 (atomicidad, coherencia del DAG,
integridad de identidad, revalidación, reversión propia ante fallo, Undo/Redo que reproduce
identidades y dependencias, vigencia). No se aprueba de antemano ningún debilitamiento de las
garantías de M09:

- `P` libre o de productor de salida única, fuera de la cadena de conductor o soporte de Locus V2;
- `P'` es una definición nueva que recibe la etiqueta de `P` como asa del analizador (D-M09-3);
- evaluación sin mutación con impacto completo en cuatro clases (conservados, revalidados,
  perdidos, no determinables);
- confirmación o cancelación;
- ejecución con una reconstrucción y un paso de Undo, y restauración al XML de entrada o fallo
  tipado si algo falla; la reversión es de la propia operación y nunca depende de un Undo.

Incluye corregir el informe legado (OD-3) y, si D-I2G lo decide, la retirada explícita de
grupo (I2G-1).

`CHANGE_ROUTE=ORDINARY`. Niveles: S0 `STATIC` + `PHASE PRE-G9B-MAINT-S0`; S1 `STATIC` +
`PHASE PRE-G9B-MAINT-S1` + `INTEGRATION`, más smoke autoral. S1 puede pedir un escalado
razonado (`VERIFICATION_ESCALATION_REQUEST`) por tocar el camino de sustitución del anfitrión.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor, posteriores a B2 e I2G0.

## Authority and evidence hierarchy

`AGENTS.md` principios 1, 7 y 10; ADR 0010, 0011, 0026, 0029, 0040, 0041 y sus enmiendas;
contrato M09 (aprobado como prospectivo; S1 no lo ejecuta ni lo sustituye);
`durable-dependency-projection.md`. Código: `Construction.replace`, `replaceInternal`,
`removeFreshRetiredHostDependents`, `doReplaceInXML`, `restoreSpatialRedefineSnapshot`;
`SpatialIdentityRegistry` (evaluación, impacto legado, cierre dependiente, ciclos);
`ConstructionGeoRedefineProvider`; `GeoCeDGSpatialRedefineFrontend`. Evidencia: diseño D §3,
informe §C.3.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- S0: pruebas y selección.
- S1:
  - sobre de entrada para objetivos sin identidad, capturado antes de analizar;
  - cálculo del impacto completo (DAG del anfitrión y cierre duradero) y su clasificación;
  - corrección del vocabulario del informe legado;
  - entrada Desktop «Sustituir punto…» sobre el diálogo de redefinición y el frontal de impacto existentes;
  - textos EN/ES;
  - pruebas y selecciones.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Conservar identidad de `P` en `P'` o transferir tokens o reclamaciones.
- Cambios de roles especializados no caracterizados; productores con varias salidas sin
  adaptador aprobado.
- Referencias por nombre cuyo impacto sea relevante y no pueda determinarse: la operación se
  rechaza o el caso se declara fuera del alcance admitido, según la política fijada tras S0.
- Actualizar el contrato de dependientes duraderos (eso es S2, tras N3).
- I3, M08-D, reconstrucción general.
- Separar salidas.
- La fusión con un `P'` existente (hasta decisión tras S0).
- Asociaciones por coordenadas, etiquetas o proximidad.
- `AlgoIntersectLineConic` y demás productores.

## Architectural placement

Núcleo compartido (transacción de redefinición e identidad) y Desktop (flujo y presentación).

## Required design/specification

Antes de S1: informe de S0 revisado y contrato candidato de sustitución explícita (nivel de
restauración decidido, requisitos de coherencia, clases de impacto, perímetro, política de
referencias por nombre, causas tipadas), aprobado por el autor. No modifica ADR 0041 ni el
contrato M09.

## Geometric invariants and degeneracies

- Ciclos rechazados antes de mutar (`INVALID_DAG`).
- Una degeneración aguas abajo deja objetos indefinidos con diagnóstico, no rechaza.
- El impacto anunciado es igual al ejecutado.
- Cancelar no consume Undo ni reservas.
- Ningún éxito parcial ni estado intermedio observable por las vistas.
- Ningún punto de Undo almacenado antes del éxito.
- Tras cualquier fallo, DAG, registro de identidad, reservas, reclamaciones y selección
  iguales a los de antes de la operación.
- Los tokens nunca se transfieren.
- Los certificados se recalculan, sin `STRICT` obsoleto.

## Compatibility and serialization

Sin formato nuevo; `P'` persiste su propia identidad; una etiqueta reutilizada no hereda la
identidad retirada; los documentos de 1.2.2 no cambian al abrirse.

## Required tests and commands
<!-- geocedg-field: required_checks -->

Pruebas discriminantes T1-T16 de la caracterización:

- ciclo;
- reasociación ordinaria;
- degeneración;
- dependiente duradero perdido;
- Locus V2 público aguas abajo;
- barrera I3;
- reclamaciones de token;
- coincidencia numérica sin asociación;
- veracidad del impacto;
- varias salidas;
- inyección de fallos;
- vigencia y cancelación;
- persistencia;
- fusión (S0);
- referencias por nombre;
- certificados.

Sin cambios: `RedefineTest`, `G9A3SpatialRedefineTransactionTest`,
`PreG9bR3DurableContractRedefineTest` (salvo la corrección declarada del informe),
`PostG9U1A3R1RedefineAssessmentTest`, `PreG9BR6PlusF3RecoveryTest`,
`DesktopInputRedefineCancelRecoveryTest`. Comandos según el nivel; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

S0 y S1 por separado; no autoriza N3 ni S2.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

S0: informe con las respuestas, el nivel de restauración que cumple los requisitos de
coherencia y la estimación de utilidad de S1 frente a S2; se detiene para revisión autoral.
S1: candidato congelado y guía de smoke.

## Required artifacts

Contrato candidato, pruebas, verificación, informe; `GUIDE_IMPACT = UPDATE`.

## Stop conditions

- El perímetro exige conservar identidad o reasociar duraderos.
- Un fallo deja estado parcial o la coherencia depende de un Undo.
- S0 muestra que ningún nivel de restauración disponible cumple los requisitos de coherencia.
- El impacto anunciado difiere del ejecutado.
- Cualquier necesidad de I3.

## Deferred debts

S2 (N3), fusión con `P'` existente, I2G-3.
