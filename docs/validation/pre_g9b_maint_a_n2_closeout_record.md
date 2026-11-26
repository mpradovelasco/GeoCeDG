# PRE-G9B-MAINT-A-N2 — registro de cierre normativo consolidado

**PRE-G9B-MAINT-A-N2 — NORMATIVE CONTRACTS — AUTHOR APPROVED — CLOSEOUT VERIFIED — PENDING PUBLICATION**

| Campo | Valor |
|---|---|
| Carácter | Registro del cierre documental autorizado por el autor. `selfApproved=false`; la aprobación es la del autor ([registro de aprobación](pre_g9b_maint_a_n2_author_approval_record.md)) |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT-A-N2. Aprobación autoral de los tres contratos y ejecución del cierre normativo», 2026-11-26 |
| Prompt | [`pre-g9b-maint-a-n2-normative-closeout.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-n2-normative-closeout.prompt.md), commit de gobernanza `10a94dd3c` (`execution_safe = true`) |
| Rama | `phase/pre-g9b-maint-a-n2-closeout`, creada desde `cadad2b5630e9928a737a9c4b51c2832b1b1395b` (árbol `47edc050828bd94044e9dcb30bc3829be7b567fb`) tras verificar que contiene el contenido aprobado `9c7e612294a2a0521a2c21d8f9c4580cb3b34efb` (árbol `7faccc1fc5d41f5d55d9fbb1ca5fd3473aa3e4fe`) |
| Comprobaciones previas | identidades y árboles exactos; `9c7e61229` ancestro de `cadad2b56`, que solo añade registros e informe; worktree limpio, sin bloqueo de índice; recibo `STATIC` `verification-24030c21…` autenticado (resumen recalculado igual al almacenado, SHA-256 del informe `d7fd496c…`); `main` remoto = `origin/main` = `a9ee803bf`, ancestro de `cadad2b56` |
| Ruta y clase | `ORDINARY`; `DOCUMENTATION_STATUS_ONLY`; `STATIC`; sin `PHASE`, `INTEGRATION` ni `FINAL` |

## 1. Contratos y ADR adoptados

| Contrato | Estado | ADR |
|---|---|---|
| [`cedg-reference-store/v1`](../../geocedg/specs/import/reference-store.md) | `AUTHOR APPROVED` (2026-11-26), revisión R4 | [ADR 0044](../adr/0044-document-store-of-imported-references.md): `ACCEPTED` |
| [`cedg-reference-export/v1`](../../geocedg/specs/export/dxf-reference-export.md) | `AUTHOR APPROVED` (2026-11-26), revisión R4 | [ADR 0045](../adr/0045-separate-dxf-export-of-imported-references.md): `ACCEPTED` |
| [`dxf-spline-approximation/v1`](../../geocedg/specs/import/dxf-spline-approximation.md) | `AUTHOR APPROVED` (2026-11-26), revisión R4 | ninguno nuevo |

Cada estado cita el contenido aprobado `9c7e61229`. Las palabras «candidate», «proposed» o
«when accepted» que quedan en el cuerpo registran el momento en que se escribieron, como dice
cada estado. Ningún contenido técnico cambió en el cierre: el diff de los tres contratos y de las
dos ADR respecto de `9c7e61229` se limita a sus líneas de estado.

## 2. Decisión autoral registrada

[Registro de aprobación](pre_g9b_maint_a_n2_author_approval_record.md):

- la aprobación individual de los tres contratos;
- el alcance que el autor ratificó expresamente;
- el carácter provisional de las constantes y de las puertas;
- las decisiones incluidas (D-R1-1 a D-R1-10, RX-D18, RX-D19, instrucción R4 y D-R4-1);
- la autorización del cierre, y la no autorización de la publicación y de la implementación.

[Registros de adopción](pre_g9b_maint_a_n2_adoption_records.md): pasan a «adoptados», con la
decisión en cada uno de los tres.

## 3. Enmiendas aplicadas

Aplicadas literalmente desde el
[conjunto de enmiendas](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md). Cada
texto aprobado conserva su aprobación anterior. La enmienda se identifica con su fecha, el
registro de aprobación y la base `a9ee803bf` del texto previo.

| Texto aprobado | Enmiendas | Dónde |
|---|---|---|
| [`dxf-spline-reference.md`](../../geocedg/specs/import/dxf-spline-reference.md) | A-IMP-1 a A-IMP-12 | §0, §1, §2, §4.1, §4.3, §10, §11, §12, §14, §14.1 y §17; fila «Amendments» y sección final fechada |
| [`bspline-2d.md`](../../geocedg/specs/curves/bspline-2d.md) | A-BS-1, A-BS-2 | §10.5; fila «Amendments» |
| [`dxf-curve-fidelity-and-approximation.md`](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md) | A-EXP-1 a A-EXP-6 | §3, §4, §7, §8, §9 y nueva §12; fila de estado propia |
| [`geometry-export-foundation.md`](../../geocedg/specs/export/geometry-export-foundation.md) | A-GEF-1 | límite, tipos, área explícita, capas, diagnósticos y orden; viñeta de enmienda en la cabecera; el estado global sigue `Experimental` |
| [`native-document-identity.md`](../../geocedg/specs/ui/native-document-identity.md) | A-NDI-1 | tras la enmienda CU4 |
| [ADR 0014](../adr/0014-export-only-dxf-approximation-and-sidecar.md) | A-ADR-1 | línea de refinamiento |
| [ADR 0043](../adr/0043-explicit-2d-bspline-family-rational-ready.md) | A-ADR-2, A-ADR-3 | línea de refinamientos |

**Ajustes de forma en la aplicación.**

- Los enlaces marcados «link to» se escribieron como enlaces relativos al fichero de destino.
- En A-GEF-1, «A-EXP-3» se expresa como `dxf-curve-fidelity-and-approximation.md` §7, que es
  donde A-EXP-3 queda aplicada.
- En A-EXP-6, la línea de estado de §12 dice ahora aprobada en lugar de candidata.
- Los apartados 12.1 a 12.9 van separados por líneas en blanco.
- En A-IMP-8, la frase de los pasos 5 y 6 se incorpora al final del paso 6.
- En A-IMP-2, la tabla `R-a` se conserva bajo «Under `R-a` …».

Ninguno de estos ajustes cambia una regla.

**Estados de los documentos de apoyo:**

- conjunto de enmiendas: `APPLIED — AUTHOR APPROVED`;
- matriz de validación: aprobada con los contratos, como plan de pruebas;
- plan operativo: planificación aprobada;
- cinco prompts candidatos: dependencias normativas satisfechas; ningún incremento autorizado.

Los informes de A-N2, R1, R3 y R4 y sus evidencias quedan intactos.

## 4. Roadmap

Registro aditivo «2026-11-26 — Aprobación autoral de los contratos PRE-G9B-MAINT-A-N2» al
final de [`geocedg_roadmap.md`](../roadmap/geocedg_roadmap.md). Es el texto canónico A-RM-1 con
la fecha y el SHA rellenados. Registra:

- C-4 y C-5 como puertas de cierre de RC;
- las dependencias de los cinco incrementos;
- D-R4-1;
- el flujo DXF extendido;
- la no autorización de implementación y publicación.

Se añadió por bytes, conservando el final de línea del fichero mixto. Ninguna otra línea del
roadmap cambia, salvo el salto de línea final de la última línea existente.

## 5. Coordinación de las cinco implementaciones

La [propuesta de ejecución coordinada](../architecture/pre_g9b_maint_a_n2_r1_coordinated_execution_proposal.md),
el prompt maestro de coordinación, queda actualizada:

- requisito de adopción cumplido;
- alcance conjunto de RC-S0, RC, EXP, DXF1-S0 y DXF1, con autonomía para coordinar ramas,
  subagentes y verificaciones dentro de las paradas;
- exclusiones expresas: `MAINT-A-DXF1b`, `MAINT-A3`, NURBS nativa y mantenimientos
  independientes DT-1 y DT-2;
- nueva tabla §1 bis de dependencias y puertas por incremento;
- hitos compartidos RC-M y DXF1-M;
- orden de integración RC → EXP → DXF1.

No se inicia ningún código ni se crea ningún worktree de implementación.

**Puertas pendientes de implementación:**

| Incremento | Puertas |
|---|---|
| `MAINT-A-DXF0-RC-S0` | C-1 a C-3; C-5 (comportamiento actual) |
| `MAINT-A-DXF1-S0` | NUM-1 a NUM-10 |
| `MAINT-A-DXF0-RC` | RC-1 a RC-26; D-R4-1 con sus seis condiciones; C-4 y C-5 en el presupuesto |
| `MAINT-A-DXF0-EXP` | RX-1 a RX-6, RX-8 a RX-17 y RX-19; RX-15 (e); conformidad RX-16, RX-17 y RX-19 |
| `MAINT-A-DXF1` | AP, NUM, RX-7 y RX-18; constantes `bspline-param-lsq-constants/v1` fijadas |

## 6. Verificación del cierre

Se ejecutaron, antes del commit:

- la validación de los prompts (la tarea de cierre y los cinco candidatos);
- enlaces relativos, UTF-8 y finales de línea;
- columnas de tablas;
- estados y referencias cruzadas;
- `git diff --check`;
- la comprobación de que ningún cambio toca `source/`, `AGENTS.md` ni `CLAUDE.md`.

El commit de cierre, su árbol y su `STATIC` autenticado se registran en el descendiente
documental de este registro (§7).

## 7. Identidad del cierre y propuesta de publicación

Registrados en un descendiente documental del commit de cierre. El descendiente solo cambia este
registro.

| Elemento | Valor |
|---|---|
| **Commit de cierre** | `2f913473a8ac789b85d55ca5277ac59b0ec3cc7f`, árbol `8ccbc365b804931d96b66480166f36bf42055b6c`, padre `10a94dd3c` (prompt de cierre) |
| Orden | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-maint-a-n2-closeout-static-2f913473`, desde el worktree |
| Código de salida | `0` |
| Ejecución | `verification-8cdcafb1dff84decbff78a882311e822`; plan `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52` |
| Identidad ligada | `candidate_commit = 2f913473a…`, `candidate_tree = 8ccbc365b…`; resumen del resultado recalculado igual al almacenado |
| Veredictos | `ACCEPTED` (3 satisfechas, 0 violadas); `COMPLETE` |
| Diagnósticos | los dos permanentes: `diagnostic.governance` `DIAGNOSTIC_FINDING` y `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`; el resto `DIAGNOSTIC_CLEAR` |
| Informe | `artifacts/agent/pre-g9b-maint-a-n2-closeout-static-2f913473/verification-result.json` (ignorado), SHA-256 `758bb60262977b729525bbc2e6f4fbb42bae2024221dbfeeb3c0b01bb244c757` |

**Delta respecto de `main` `a9ee803bf`** hasta el commit de cierre: 18 commits, 38 ficheros,
6 665 inserciones y 37 supresiones, solo en:

| Ruta | Ficheros |
|---|---|
| `.github/prompts/tasks` | 5, los prompts de tarea de A-N2 a R4 y del cierre |
| `docs/adr` | 4 |
| `docs/architecture` | 8 |
| `docs/roadmap` | 1, un registro aditivo |
| `docs/validation` | 12 |
| `geocedg/specs` | 8 |

No hay código, recursos, pruebas, verificador ni empaquetado. `AGENTS.md` y `CLAUDE.md` no
cambian.

**Remoto.** `refs/heads/main` en `origin` = `origin/main` = `main` local =
`a9ee803bf4be92c58ab7e120111ebacc70055487`, ancestro del cierre. La promoción es un avance
rápido sin `--force`.

**Propuesta de publicación** (no autorizada; exige una instrucción autoral que nombre el SHA):

1. Tomar como candidato único de publicación el descendiente documental de este registro. Su
   SHA y su árbol se entregan al autor con su propio `STATIC`; el descendiente solo cambia este
   registro.
2. Seguir AGENTS §8.1:
   - leer y conciliar el `main` remoto;
   - verificar el SHA y el árbol del candidato y la ascendencia;
   - comprobar que ambos worktrees están limpios;
   - avanzar `main` local sin `--force`;
   - volver a confirmar el remoto y ejecutar `git push origin main`;
   - verificar que `main` remoto, `main` local y `origin/main` resuelven al mismo commit y
     árbol.
3. No crear etiquetas, releases ni paquetes.
4. Tras la publicación, añadir un registro de publicación en un descendiente documental o en el
   siguiente encargo, según indique el autor.

**Estado:** `PRE-G9B-MAINT-A-N2 — NORMATIVE CONTRACTS — AUTHOR APPROVED — CLOSEOUT VERIFIED —
PENDING PUBLICATION`.
