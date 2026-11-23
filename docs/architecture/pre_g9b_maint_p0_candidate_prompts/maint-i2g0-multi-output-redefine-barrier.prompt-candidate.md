# GeoCeDG — PRE-G9B-MAINT-I2G0: barrera de productores con varias salidas

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere la decisión D-I2G y una instrucción autoral con
base exacta.

## Objective
<!-- geocedg-field: objective -->

Impedir la destrucción silenciosa de identidades y dependencias CeDG al redefinir o sustituir
una salida de un productor con varias salidas, sin rechazar innecesariamente las operaciones
Classic legítimas. La barrera se basa en el riesgo demostrado para el productor y su grupo
(diseño D §4.3), no solo en el número de salidas. Dos pasos con puerta autoral entre ellos:

1. **Caracterización (solo pruebas):**
   - la conducta Classic legítima de cada productor heredado de varias salidas cuando el grupo
     no contiene nada CeDG, fijada como referencia que no debe cambiar;
   - la conducta en los casos en riesgo: hermanos con dependientes duraderos CeDG, objetivo con
     identidad y hermano sin ella, objetivo sin identidad y hermano con identidad;
   - el estado de los hermanos tras un fallo de reconstrucción nativa.
2. **Barrera:** antes de `prepareReplace`, si el conjunto que se eliminaría contiene una
   identidad duradera, un registro de identidad, un dependiente duradero CeDG o una reserva o
   reclamación de token, rechazo tipado (`CU7_GROUP_TRANSITION_UNSUPPORTED` o la que fije la
   decisión) que nombre el grupo y lo que se destruiría. Sin riesgo, la conducta Classic no
   cambia.

Esto corrige la destrucción silenciosa fijada hoy (hermano con identidad) y la causa engañosa
del caso con objetivo con identidad.

`CHANGE_ROUTE=ORDINARY`. Niveles: caracterización `STATIC` + `PHASE PRE-G9B-MAINT-I2G0-S0`
(`BOUNDED_PHASE`); barrera `STATIC` + `PHASE PRE-G9B-MAINT-I2G0` + `INTEGRATION`
(`INTEGRATED_PHASE`).

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor.

## Authority and evidence hierarchy

`AGENTS.md` §16; ADR 0010, 0026, 0041 (decisión 7); contrato M09 §5. Código:
`Construction.prepareReplace` y `replacePrepared`, `AlgoElement.removeOutputExcept`,
`SpatialIdentityRegistry` (contexto y grupo). Evidencia: diseño D §4, informe §C.4.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Paso 1: pruebas de caracterización y su selección.
- Paso 2: predicado de riesgo y comprobación de la barrera antes de `prepareReplace` en el
  camino de redefinición.
- Causa tipada y su texto en Desktop.
- Actualización de `G9A1SpatialIdentityRedefineHostTest.java:483-497` según D-I2G.
- Pruebas y selecciones `PRE-G9B-MAINT-I2G0`.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Separar salidas.
- Ranuras ocultas (I2G-3).
- Reexpresar hermanos.
- `AlgoIntersectLineConic` y demás productores.
- Retirada de grupo (I2G-1, que pertenece a S1).
- La redefinición de objetos de salida única.

## Architectural placement

Núcleo compartido, camino de sustitución del anfitrión; cambio mínimo justificado por la
decisión D-I2G (AGENTS §3.1).

## Required design/specification

Decisión D-I2G registrada. Si cambia el comportamiento de GeoGebra Classic para documentos
`.ggb` en el perfil Classic, detenerse y pedir decisión.

## Geometric invariants and degeneracies

- Ninguna identidad, dependiente duradero ni registro CeDG desaparece sin confirmación.
- El rechazo no muta XML, identidades, reservas ni Undo.
- Un grupo sin nada CeDG en riesgo se redefine exactamente como en Classic.

## Compatibility and serialization

Sin cambios de XML. Cambio de conducta solo en los casos con riesgo: una redefinición que hoy
destruye identidades o dependientes CeDG pasa a rechazarse. Declararlo en la guía y en la nota
de versión. Las redefiniciones Classic sin riesgo no cambian.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Paso 1: la batería de caracterización descrita, con la conducta Classic legítima fijada.
- Paso 2:
  - los casos con riesgo con `Intersect(c, g)`: rechazo antes de eliminar; XML y estado
    nativo de selección de los hermanos idénticos;
  - un productor de varias salidas sin nada CeDG en el grupo: conducta idéntica a la fijada en
    el paso 1;
  - la redefinición de una salida única no cambia.
- `RedefineTest` (upstream) sin cambios salvo el caso de varias salidas, que debe analizarse
  y declararse.
- Comandos: `STATIC`, `PHASE PRE-G9B-MAINT-I2G0` e `INTEGRATION`; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Este incremento.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado para revisión autoral.

## Required artifacts

Commits, verificación, informe con la lista de comportamientos cambiados, `GUIDE_IMPACT`
según el texto visible.

## Stop conditions

- El cambio afecta a pruebas upstream fuera del caso de varias salidas, o a una redefinición
  Classic sin riesgo.
- El predicado de riesgo no puede decidirse antes de mutar.
- Exige tocar productores.
- El perfil Classic cambia sin decisión.

## Deferred debts

I2G-1 (en S1), I2G-3.
