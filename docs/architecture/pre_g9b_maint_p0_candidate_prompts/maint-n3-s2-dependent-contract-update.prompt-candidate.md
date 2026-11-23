# GeoCeDG — PRE-G9B-MAINT-N3/S2: actualización de contrato de dependientes en una sustitución

**PROMPT CANDIDATO — NO AUTORIZADO.** Condicionado a S1 aprobado y a la decisión D-M09-2. N3
(normativa) y S2 (código) se autorizan por separado.

## Objective
<!-- geocedg-field: objective -->

S2 es el **objetivo funcional principal** de la sustitución: sustituir un punto por otro con
identidad nueva preservando las construcciones dependientes compatibles, mediante la
actualización explícita de sus dependencias y la revalidación de su evidencia, sin reducir las
garantías de coherencia del DAG ni aceptar publicación parcial.

**N3** (`DOCUMENTATION_STATUS_ONLY`): redactar las enmiendas candidatas de ADR 0026 (decisión
6), ADR 0029 (decisión 4), ADR 0041 (decisión 8) y del contrato M09 §3. Admiten, dentro de una
sustitución explícita, el mapa trivial y total `{P ↦ P'}`: un dependiente duradero directo
compatible (su paso de construcción propio sigue siendo válido con `P'`: mismo comando, mismas
entradas salvo `P ↦ P'` y tipo admitido) conserva su identidad, con frontera actualizada
explícitamente, revisión nueva y evidencia revalidada. Si la revalidación no establece la
evidencia, el objeto queda indefinido o con el grado que corresponda, con diagnóstico, y no se
pierde. Incluye Locus V2 públicos. Tokens y reclamaciones se revalidan por sus guardas y nunca
se transfieren.

**S2** (`INTEGRATED_PHASE`): implementar esa regla sobre S1, con sus mismos requisitos de
coherencia. Pasa a «revalidados» lo que S1 clasificaba como «perdidos» cuando el dependiente
es compatible.

`CHANGE_ROUTE=ORDINARY`. Niveles: N3 `STATIC`; S2 `STATIC` + `PHASE PRE-G9B-MAINT-S2` +
`INTEGRATION`, más smoke autoral, con posible escalado razonado.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor, posteriores al cierre de S1.

## Authority and evidence hierarchy

ADR 0010, 0011, 0026, 0028, 0029, 0040, 0041 y 0042; contrato M09;
`durable-dependency-projection.md` §9-§10; `spline-v2-pair-materialization.md` §6 (ranuras);
existencia §20. Evidencia: diseño D §3.4, informe §C.3.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- N3: ADR y contrato, solo enmiendas fechadas y marcadas como candidatas.
- S2: la actualización de contrato por dependiente dentro de la transacción de S1, su
  clasificación, pruebas y selecciones.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Mapas distintos del trivial.
- Transferencia de tokens.
- Dependientes cuyo paso propio cambia.
- I3, M08-D, reconstrucción general, I2G-3.

## Architectural placement

Núcleo compartido: identidad duradera y transacción de redefinición.

## Required design/specification

N3 aprobada antes de S2.

## Geometric invariants and degeneracies

- La identidad de un dependiente solo se conserva si es compatible; la compatibilidad se
  decide estructuralmente antes de mutar.
- Una revalidación fallida degrada el estado o el grado del dependiente, nunca lo elimina en
  silencio.
- La evidencia se recalcula por revisión.
- Las ranuras D2 y E-3 pueden quedar inactivas y nunca se reasignan por coincidencia.
- Ninguna asociación por coordenadas, etiquetas o proximidad.

## Compatibility and serialization

Registros duraderos v2 con frontera nueva; documentos existentes sin cambios al abrirse.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- El caso de uso de M09: un punto provisional sustituido por una intersección Locus V2
  conserva los loci dependientes y sus identidades.
- Un dependiente con paso propio cambiado sigue perdido.
- Tokens revalidados sin transferencia.
- Undo, Redo y reapertura.
- Inyección de fallos.
- Batería T1-T16 de S1 sin regresión.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

N3 y S2 por separado.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

N3: enmienda candidata para aprobación. S2: candidato congelado y guía de smoke.

## Required artifacts

Enmiendas, pruebas, verificación, informe; `GUIDE_IMPACT = UPDATE`.

## Stop conditions

- La regla exige transferir tokens.
- Un Locus V2 conservado publica evidencia de la revisión anterior.
- Aparece una necesidad de I3.

## Deferred debts

I3, M08-D.
