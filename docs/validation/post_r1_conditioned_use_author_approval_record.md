# Uso condicionado de Locus V2 — registro de la aprobación autoral del ADR 0042 y de la autorización del bloque 3B

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad del estado de aprobación del ADR 0042 y del contrato de uso condicionado, y de la autorización de su implementación. No aprueba ninguna implementación, no declara ningún incremento `PASS — AUTHOR APPROVED` y no autoriza publicar |
| Instrucción | «APROBACIÓN AUTORAL — ADR 0042 Y USO CONDICIONADO DE LOCUS V2», decisión del autor de 22 de noviembre de 2026: **TEXTO NORMATIVO APROBADO — BLOQUE 3B AUTORIZADO (I0, I1, I2, I3, I4, I4T)** |
| Repositorio | `mpradovelasco/GeoCeDG` |
| Commit normativo aprobado | `9160d67d7586c91dd6ef09f4ebff603bab57036e`, árbol `b1f877edbbbe4156328eaeda9ef73c9778f21e08`: ADR 0042 revisión 4 y contrato `conditioned-use/v4-candidate` |
| Punta de la rama al registrar | `975ccd162eb229725ca33d94ffc24475c12ce844`, árbol `9e04900f9e47e95ab0d1b9b17108ba3c54e1ddac`, en `maintenance/geocedg-1.2.2-post-r1`, local y sin publicar |
| `main` | local y `origin/main` en `9ccefdcfe922d8c0e329bdeec30c12fc058de463`; etiquetas de versión `v1.2.0` y `v1.2.1`; versión de producto 1.2.1 |
| Expediente aprobado | [ADR 0042](../adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md); [contrato de uso condicionado](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md); [diseño](../architecture/post_r1_locus_conditioned_use_design.md); [registro de decisiones](post_r1_conditioned_use_author_decision_sheet.md) |
| Registro de progreso | [progreso y checkpoints](post_r1_locus_repairs_progress.md) |
| `selfApproved` | `false` |

Este registro transcribe y ordena la instrucción del autor; ante cualquier diferencia
prevalece la instrucción autoral.

## 1. Identidad y comprobaciones de entrada

| Comprobación | Resultado |
|---|---|
| Commit normativo | `9160d67d7` existe en la rama; su árbol es `b1f877ed…`; los ficheros del ADR 0042 y del contrato no cambian entre `9160d67d7` y la punta `975ccd162` |
| Punta y ascendencia | `975ccd162` desciende de `349c1856c` (candidato técnico `T4`), de `9160d67d7` y de `main` `9ccefdcfe` |
| Espacio de trabajo | limpio |
| `main` | local y `origin/main` en `9ccefdcfe`, sin divergencia; ninguna etiqueta creada ni movida |
| Autoridad leída | `AGENTS.md`, prompts canónicos de gobierno y verificación, hoja de ruta, ADR 0042, contrato, diseño y registros del encargo |
| Verificación vigente | la de `T4` `349c1856c`: `STATIC`, `PHASE POST-R1-LOCUS` e `INTEGRATION`, `ACCEPTED / COMPLETE` (registro de progreso, checkpoint 6). No se repite |

## 2. Qué aprueba el autor

1. **ADR 0042, revisión 4**, como decisión de arquitectura.
2. **Contrato `conditioned-use/v4-candidate`**, texto del commit `9160d67d7`, como base
   normativa autorizada para implementar el uso condicionado de Locus V2.
3. **Las ocho cuestiones** de las revisiones anteriores quedan resueltas: los seis puntos
   de la enmienda focal de la revisión 3 y las dos precisiones focales de la revisión 4.
4. **Las decisiones D-V1 a D-V7** y las precisiones sobre la identidad de las raíces entre
   configuraciones y sobre la resolución geométrica bajo semejanzas.

## 3. Qué autoriza

El inicio y la ejecución secuencial del bloque 3B completo, **I0, I1, I2, I3, I4 e I4T**,
sin nuevas consultas autorales rutinarias, desde la punta de
`maintenance/geocedg-1.2.2-post-r1`.

| # | Instrucción |
|---|---|
| 1 | Registrar la aprobación normativa mediante el procedimiento del repositorio, referenciando el commit exacto y aplicando progresivamente las enmiendas a las especificaciones vigentes |
| 2 | Completar I0, incluido el testimonio del evaluador real, la integración en el productor y la medición de rendimiento |
| 3 | Ejecutar los incrementos restantes con sus pruebas discriminantes, conservación de identidad, persistencia y propagación por el DAG |
| 4 | Validar especialmente los criterios 7a a 7e y 8a a 8f sobre el kernel real, no únicamente mediante la sonda externa |
| 5 | Preservar la separación entre evidencia condicionada, integridad constructiva, certificación rigurosa y completitud global |
| 6 | Conservar la reparación F8 y sus pruebas de regresión |
| 7 | Al completar todos los incrementos, preparar el candidato funcional GeoCeDG 1.2.2, con las comprobaciones de Desktop, persistencia, rendimiento y app-image correspondientes |

Cada incremento mantiene su trazabilidad, sus pruebas y su verificación ordinaria. Un
resultado automatizado aceptado no constituye por sí solo aprobación autoral del
incremento.

## 4. Qué no autoriza

- la publicación en `main`;
- la creación de la etiqueta `v1.2.2`;
- una release;
- la ejecución de `FINAL`.

Esas acciones requieren una decisión expresa posterior. No es necesario reabrir el diseño
ni repetir las verificaciones anteriores que sigan siendo válidas.

## 5. Aplicación de la aprobación

| Destino | Qué cambia con este registro |
|---|---|
| ADR 0042 | estado `ACCEPTED — ARCHITECTURE AUTHOR APPROVED (2026-11-22)`; su texto de decisión no cambia |
| Contrato de uso condicionado | estado de contrato normativo aprobado por el autor, versión `conditioned-use/v4`; el texto de sus secciones es el de `9160d67d7` |
| Especificaciones vigentes | reciben las enmiendas del contrato §12 de forma progresiva: cada incremento incorpora, antes de cerrarse, las que corresponden a lo que implementa, en una sección nueva y versionada. Las secciones aprobadas de fases anteriores no se reescriben |
| Hoja de ruta | registro aditivo fechado de esta decisión |
| Registro de decisiones y diseño | estado actualizado; su contenido no cambia |

Correspondencia entre incrementos y enmiendas del contrato §12:

| Incremento | Especificación enmendada |
|---|---|
| I0 | `locus-v2-existence-components.md` (productor de la naturaleza `NUMERICAL_UNDER_HYPOTHESES`) |
| I1 | `locus-v2-public-surface.md` (códec de dirección por parámetro), `locus-v2-point-interaction.md`, `locus-v2-existence-components.md` (admisiones de consumo) |
| I2 | `locus-v2-presentation.md` |
| I3 | `locus-v2-metrics.md` |
| I4 | `locus-v2-intersections.md`, `locus-v2-extended-intersections.md`, `locus-v2-public-surface.md` (estados de resultado y versión del ledger), especificaciones de exportación |
| I4T | `locus-v2-similarity-transformations.md` |

`locus-v2-constructive-authority.md` y `locus-v2-semantics.md` no reciben ninguna enmienda
(contrato §12).

## 6. Estado que este registro no cambia

- Las reparaciones posteriores a R1, el modelo corregido, D-V6, F8 y el paquete
  preparatorio de I0 siguen siendo candidatos técnicos pendientes de revisión autoral. La
  aprobación del texto normativo no los aprueba como producto.
- Ningún incremento de 3B queda aprobado por anticipado. Al completar I4T se presenta el
  candidato funcional GeoCeDG 1.2.2 para decisión del autor.
- La versión de producto no cambia hasta que la funcionalidad esté implementada y validada
  (D-V7).
