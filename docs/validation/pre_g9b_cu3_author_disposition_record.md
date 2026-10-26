# PRE-G9B-CU3 — Registro de la disposición autoral tras la revisión del candidato publicado

| Campo | Valor |
|---|---|
| Carácter | Transcripción de la instrucción autoral «PRE-G9B-CU3 — Disposición autoral tras la revisión del candidato publicado» (2026-10-26) y registro de su ejecución. No es un registro de cierre: `CU3` no está `PASS — AUTHOR APPROVED` |
| Candidato revisado | técnico `3c2ca7e800001a19b96b686f1adb8fcc1866c796`; descendiente documental publicado `735c55c6b98765d8b87359bbd4c6ed7f838051b9` |
| Publicación previa | autorizada el 2026-10-26: `phase/pre-g9b-cu3-implementation` avanzó en el remoto de `1a0903aa` a `735c55c6` por push normal, sin forzar, sin etiquetas y sin PR; `main` (`e6c908f2`), las 73 referencias de etiqueta y las demás ramas sin cambios. La auditoría, ignorada por Git, está en `artifacts/agent/pre-g9b-cu3-implementation-publication-audit/` |
| Rama | los commits de esta disposición son locales y no se publican: la disposición pide no repetir la publicación |
| `selfApproved` | `false` |

## 1. `CU3-A`

> Acepto provisionalmente el candidato técnico de CU3-A, condicionado a las pruebas manuales del
> autor y a reconciliar el alcance del censo de G-AUT-1. No declaro todavía `PASS — AUTHOR
> APPROVED`. Las cinco incidencias estrictas y su integración dinámica constituyen el resultado
> científico principal del candidato.

- **Estado:** aceptado provisionalmente, con dos condiciones. No hay `PASS` ni cierre.
- **Condición 1, pruebas manuales:** se prepara la
  [guía de prueba manual](pre_g9b_cu3_manual_test_guide.md) (§6). Resultado posterior, del mismo
  día: smoke funcional `PASS — AUTHOR REPORTED` y rendimiento interactivo `NOT ACCEPTED`
  ([registro del smoke](pre_g9b_cu3_author_smoke_record.md)).
- **Condición 2, censo:** conciliado en el
  [registro de la caracterización del censo](pre_g9b_cu3_census_characterization_record.md) (§3).

## 2. `CU3-B`

> Mantengo CU3-B pendiente. No autorizo todavía cambios productivos de tokens, selectores, gérmenes
> ni identidad. Autorizo únicamente una revisión arquitectónica focal de sus tres problemas […]. La
> revisión debe proponer una solución compatible con ADR 0017, ADR 0039, ADR 0040, persistencia,
> DAG y documentos antiguos. No anticipar aprobación de nuevas reglas de identidad.

[Revisión arquitectónica focal](../architecture/pre_g9b_cu3_b_identity_architecture_review.md):

- **P1, identidad anclada en el pliegue.** Una sola solución y una sola identidad para el contacto
  en un pliegue y para su pareja de preimágenes. Los tokens transversales existentes no se fusionan
  ni se reasocian: dejan de ser actuales con causa tipada.
- **P2, etiqueta exacta del pliegue.** La involución y el punto fijo de `fold-symmetry/v1` distinguen
  los dos contactos de `e_1·c_1`, incluida la costura, sin rango de fase.
- **P3, germen sin evidencia.** El germen contiene solo hechos estructurales, sin clase de evidencia
  ni orden de contacto.
- **Decisiones pedidas:** CU3-B-R1 a CU3-B-R6.

Ninguna regla de identidad se da por aprobada y ningún código cambia.

## 3. Censo de procedencia

> Autorizo caracterizar externamente los 20 casos que agotaron los 4.096 bits, sin modificar el
> presupuesto productivo. Reconciliar el resultado con la formulación de G-AUT-1 y su matriz. No
> declarar demostrado lo que quedó sin decidir.

[Registro de la caracterización](pre_g9b_cu3_census_characterization_record.md) y su evidencia
[`pre-g9b-cu3-census-characterization-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-census-characterization-evidence.json):

- **Fuera del producto.** Con un presupuesto de caracterización, las veinte relaciones se cumplen
  exactamente, relativas a los sustitutos de frontera, con 4 126 a 4 514 bits.
- **En el producto.** Siguen sin decidir.
- **Conciliación del censo completo.**
  - Con el presupuesto productivo: 27 relaciones sin sustitutos, 113 con sustitutos, 20 agotadas y
    ninguna refutada.
  - Ninguna igualdad con sustitutos demuestra la relación para los objetos ideales.
  - La matriz recibe una nota fechada en la fila V-AUT-PROV-1, sin cambiar su criterio.

## 4. Presupuesto del certificado de identidad constructiva

> Apruebo para `constructive-identity-certificate-budget/v1` los límites de 16.000.000 productos,
> 16 indeterminadas, 200.000 términos y 4.096 bits. Mantener los contadores, presupuestos por
> intento y fallos cerrados. No ampliar estos límites sin autorización.

- **Estado:** aprobado el 2026-10-26.
- **Especificación:** `locus-v2-constructive-authority.md` §10 deja de llamarlo provisional y cita
  este registro.
- **Código:** los límites ya son los del código (`ConstructiveIdentityBudget`), así que no cambia.
  El comentario de esa clase todavía los llama «techos iniciales». Se alineará en el próximo
  incremento de producto autorizado, sin cambiar ningún valor.
- **Garantías que se mantienen:** contadores deterministas, presupuesto por intento y
  `BUDGET_EXHAUSTED` con fallo cerrado. Ningún límite se amplía sin autorización.

## 5. Rendimiento

> Acepto provisionalmente los tiempos medidos como limitación del candidato técnico, no como
> objetivo de rendimiento del producto. Autorizar únicamente caracterización y propuestas de
> optimización que preserven las garantías geométricas, sin modificar todavía código productivo.

[Caracterización y propuestas](../architecture/pre_g9b_cu3_performance_characterization_and_proposals.md),
con su evidencia
[`pre-g9b-cu3-performance-characterization-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-performance-characterization-evidence.json):

- **Medida.** Del arrastre de `H` (748 ms), el 74 % es la nueva comprobación de las decisiones
  exactas, sobre todo `o_1·h_2`, y el 21 % las envolventes.
- **Propuestas.** O1 a O6, ninguna implementada. Las primeras recomendadas son la especialización
  exacta en las indeterminadas que no cambian y la memoria por revisión.
- **Autoridad predeterminada.** Su promoción futura sigue necesitando una evaluación de rendimiento
  y compatibilidad general independiente.

## 6. Estado y publicación

> La rama ya está publicada. No repetir la publicación. No modificar `main`, crear etiquetas,
> iniciar otras fases ni redactar un cierre `PASS — AUTHOR APPROVED`. Mantener pendiente el smoke
> manual. Preparar una guía concisa de ejecución manual con resultados esperados y causas de fallo.
> No iniciar implementación de CU3-B hasta una decisión autoral posterior.

- **Guía:** [guía de prueba manual](pre_g9b_cu3_manual_test_guide.md), con cuatro bloques (adopción,
  contactos, dinámica y persistencia), resultados esperados y causas de fallo.
- **Publicación:** no se repite.
- **Lo que no se hace:** no se toca `main`, no se crean etiquetas, no se inicia ninguna fase, no hay
  cierre y no se implementa `CU3-B`.

## 7. Continuidad editorial

> Conservar para CU10 toda la evidencia científica de autoridad constructiva, certificación CIC,
> identidad de intersecciones, pliegues, tangencias, integridad y limitaciones de representación
> numérica. No modificar todavía `geocedg_book`.

El [registro de transferencia científica E1](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md),
en su versión 13, enumera la evidencia que el expediente de `CU10` debe conservar. `geocedg_book` no
cambia.

## 8. Ejecución

| Paso | Resultado |
|---|---|
| Herramientas | commit `451c1b501f7b010f279b0a1c9af34202c9913af4` (árbol `0a40ec38`): sondas y lanzadores de las caracterizaciones del censo y del rendimiento, fuera del producto |
| Caracterizaciones | dos ejecuciones de cada una sobre `451c1b50`, con el árbol limpio antes y después: censo idéntico byte a byte; rendimiento con los mismos contadores y tiempos distintos |
| Documentos | este registro, los dos registros de caracterización, la revisión de `CU3-B`, la guía, la nota de la matriz, la §10 de la especificación, el registro E1 versión 13, el plan (§2.13) y el roadmap |
| Verificación | `STATIC` sobre el commit documental que añade este registro; un commit no puede citar su propia identidad, así que su resultado se informa al autor y se registrará en el siguiente paso documental |
