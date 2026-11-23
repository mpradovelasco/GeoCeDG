# GeoCeDG 1.2.2 — registro de la autorización autoral de publicación definitiva

**AUTHOR APPROVED — PUBLICATION AUTHORIZED**

**`FINAL NOT EXECUTED — EXPLICIT AUTHOR-APPROVED RELEASE EXCEPTION`**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad de la autorización de publicación de GeoCeDG 1.2.2 y de su excepción relativa a `FINAL`. `selfApproved=false`: la decisión es del autor y este registro solo la transcribe |
| Fecha de la decisión | 23 de noviembre de 2026 |
| Versión y fecha de versión | GeoCeDG 1.2.2, `2026-11-22` |
| Candidato técnico aprobado | `a6562fa30dc7a468b8e62e16843f1ae2f440285b`, árbol `aac4b541b28660233cccd295791bb1a82871d108` |
| Commit documental de aprobación | `2735d79e5debd306f9e4070d8d85f4b417a006fd` ([registro de la aprobación](geocedg_1_2_2_author_approval_record.md)) |
| Rama de origen | `maintenance/geocedg-1.2.2-post-r1` |
| Perfil de distribución | `NC`, no comercial; solo Windows x64 |
| Base publicada anterior | GeoCeDG 1.2.1 (`v1.2.1`, `4267cff35`); `main` y `origin/main` en `9ccefdcfe922d8c0e329bdeec30c12fc058de463` al recibir la instrucción |

Este registro transcribe y ordena la instrucción del autor «GeoCeDG 1.2.2 — Autorización
autoral de publicación definitiva». Ante cualquier diferencia prevalece la instrucción autoral.

## 1. Qué aprueba y autoriza

El autor aprueba definitivamente GeoCeDG 1.2.2 y autoriza su publicación. La aprobación comprende:

- el candidato técnico `a6562fa30`;
- las correcciones posteriores al cierre de R1, la implementación 3B (I0 a I4T), las
  correcciones C0 a C5, la C2 final y el ciclo de vida de `LocusLength`;
- las limitaciones residuales documentadas en la
  [evidencia final de cierre](geocedg_1_2_2_closing_evidence.md), §5, que quedan como deuda
  de mantenimiento posterior;
- la promoción del estado aprobado a `main` por fast-forward, sin rebase, squash, reescritura
  ni force-push;
- la etiqueta anotada `v1.2.2`;
- la release de GitHub «GeoCeDG 1.2.2 — Conditioned Locus V2 Support», solo con los
  paquetes Windows x64 del perfil `NC` y sus metadatos.

No autoriza nuevas funcionalidades, correcciones adicionales ni cambios de arquitectura.

## 2. Excepción expresa de `FINAL`

Decisión del autor: **no ejecutar `FINAL` para el cierre y la publicación de GeoCeDG 1.2.2.**
El autor la fundamenta en:

- las verificaciones `STATIC`, `PHASE POST-R1-LOCUS`, `INTEGRATION` y `PACKAGING` aceptadas;
- la regresión compartida completa (2 536 pruebas, 0 fallos);
- la regresión Desktop completa (1 573 pruebas, con un único fallo histórico presente en la base);
- las pruebas focales y el humo del ejecutable real;
- la ausencia de regresiones nuevas conocidas.

Para este cierre, el autor no considera proporcionada la campaña adicional de `FINAL`.

Se registra como `FINAL NOT EXECUTED — EXPLICIT AUTHOR-APPROVED RELEASE EXCEPTION`. `FINAL` no
se declara superado, aceptado ni completo. No existe ningún recibo de `FINAL` para este
candidato, y las evidencias de otros perfiles no lo sustituyen.

**Comprobación de la regla aplicable** (hecha antes de publicar, como pide la instrucción):

- La subsección «Exhaustive campaigns and the prospective FINAL obligation» de
  [`verification-levels.md`](../../geocedg/specs/operations/verification-levels.md):
  - modifica prospectivamente las reglas que hacían obligatorio `FULL`/`FINAL` por categoría,
    entre ellas la de release (`RELEASE_OR_MILESTONE`);
  - establece que la verificación ordinaria es `CORE + FOCUSED`;
  - establece que `FINAL` exige una autorización autoral expresa.
- No se ha encontrado ninguna regla normativa del alcance de 1.2.2 que exija `FINAL` sin
  admitir excepción. Se revisaron `AGENTS.md`, `verification-levels.md`, la puerta de release
  de `windows-packaging.md` y el registro de aprobación del uso condicionado.
- Por tanto, la publicación no se detiene y la gobernanza no cambia.

## 3. Auditoría previa a la publicación

Auditoría no invasiva del 2026-11-23. No se reconstruyó ningún paquete ni se repitió ninguna
verificación.

| Comprobación | Resultado |
|---|---|
| Rama limpia y descendiente del candidato | Árbol de trabajo limpio; `a6562fa30` es ancestro de `HEAD`. `origin/main` (`9ccefdcfe`) es ancestro del candidato, así que el fast-forward es posible |
| Commits posteriores al candidato | Solo `e24af7ba3` y `2735d79e5`, únicamente en `docs/roadmap` y `docs/validation`. `tools/agent/documentary-descendant.ps1 -TechnicalCommit a6562fa30 -DocumentCommit 2735d79e5` da `DOCUMENTARY_ONLY` (código 0, `rerun_required=false`) |
| Identidad del candidato y evidencias | Los recibos de `STATIC` `verification-443faf5422ea48d5a13ef0d366037c10`, `PHASE` `verification-6bb2140c1e714c28add05f4ee8d83416`, `INTEGRATION` `verification-b81359f645654b699a81d85f7473a19d` y `PACKAGING` `verification-675aa2ec12b248e7b0a9f3a31e105427` están ligados a `a6562fa30`, todos `ACCEPTED / COMPLETE` |
| Procedencia de los paquetes | `build-manifest.json`: `source_revision` = `a6562fa30`, versión `1.2.2`, fecha `2026-11-22`, perfil `NC`. El registro instalado `app/geocedg-distribution.json` declara lo mismo |
| Sumas | Las siete entradas de `SHA256SUMS.txt` se recalcularon y coinciden: ZIP `d19ba613…`, MSI `ac256807…`, EXE `accb437a…`, manifiesto, SBOM, inventario de componentes y sumas de la app-image |
| Composición NC | Respecto a 1.2.1 son idénticos: el bloque legal (`LICENSES/manifest.json`, manifiesto de acceso al código fuente, disposición de componentes), el perfil `NC`, la biblioteca GGT admitida, las exclusiones, la asociación de ficheros y la cadena de herramientas. Los 56 componentes de ejecución coinciden; solo cambian los JAR propios `common.jar` y `common-jre.jar`. El aviso `NC_DISTRIBUTION_NOTICE.txt` es el mismo (`eea919bb…`). La composición auditada y las condiciones del perfil `NC` siguen siendo aplicables |
| Estado remoto | `main` = `origin/main` = `git ls-remote` = `9ccefdcfe`; no existe etiqueta `v1.2.2`, local ni remota, ni release `v1.2.2` |

## 4. Límites definitivos

No se autoriza:

- ejecutar `FINAL`;
- abrir fases nuevas;
- corregir defectos históricos adicionales;
- volver a optimizar D-V6, `Intersect` o la longitud total condicionada;
- modificar las políticas normativas de Locus V2;
- cambiar la serialización, la identidad o el DAG;
- instalar MSI o EXE en el entorno de trabajo;
- modificar las versiones 1.2.0 o 1.2.1;
- declarar certificaciones geométricas superiores a las establecidas.

El commit que contiene este registro es un descendiente exclusivamente documental de
`a6562fa30`, y es el estado que se promueve a `main` y que identifica la etiqueta `v1.2.2`. El
registro de la publicación se añade después, sin modificar el commit etiquetado.
