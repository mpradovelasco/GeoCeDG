# GeoCeDG 1.2.0 — Expediente de verificación

**Estado en preparación.** Las ejecuciones focales y de caracterización documentadas aquí no son un recibo de aceptación de un árbol final.

## Procedencia

- Rama `codex/geocedg-1.2.0`, desde el `main` local y publicado `4fc0343a1f0dc59a645ee4fe8ec6c5c40fe2051e`, árbol `7cbabc4bd967d775aa730264614d2a1f5092c996`. `git ls-remote origin refs/heads/main` y la consulta de GitHub devolvieron ese commit. `git merge --ff-only origin/main` devolvió 0 y confirmó que `main` ya estaba al día.
- Original del cono `C:\Temp\InterCilConoOblique_2.cedg` y copia de referencia de 70 241 bytes con SHA-256 `4cde946eba57bfd36d531c677dc9f622b9036d6e95cd92941311b51d5f786a01`. Las pruebas de cambios en parámetros usan copias o construcciones en memoria. La evidencia generada está en el directorio local ignorado `artifacts/agent/geocedg-1.2.0/`.
- Autoridades inspeccionadas `AGENTS.md`, roadmap, ADR 0037/0038/0040/0041, contratos Locus V2 y de verificación, perfil Desktop y empaquetado. El árbol base no contiene `.github/copilot-instructions.md`. Se consultaron los prompts de los ámbitos existentes y el contrato y roadmap editoriales de `geocedg_book`.

## Matriz de requisitos y evidencia

| Ámbito | Criterio | Evidencia y estado |
| --- | --- | --- |
| F1 perfil y modos | Compass antes del primer arco e Hyperbola después de Parabola, en toolbar y Construction | Orden de la toolbar observado en Desktop. Regresión de perfil y prueba de funcionamiento pendientes de consolidar. |
| F1 captura | Referencias válidas, conservación del campo y cancelación en Redefinir, Input Box y diálogos compatibles | Desktop confirmó captura desde Álgebra, cancelación en Redefinir y creación de Input Box. Detectó un fallo de clip en Gráfica, corregido en fuente y pendiente de repetición interactiva. |
| F1 flyout y apariencia | Transferencia bidireccional, reposo sin apertura y recursos nativos | Reproducción anterior y posterior en Desktop. El ajuste final del icono del punto explícito queda pendiente de repetir. |
| F1 Control Layout | Supresión del aviso ordinario con persistencia intacta | Pruebas Desktop focales. |
| F2 equivalencia | Prueba tipada, identidad independiente, rechazo, recuperación y reapertura | Prueba focal shared exit 0 para tres casos de receta. `P_1` está fuera de la clase de pruebas adoptada. La ruta de colocación retiene una dirección seleccionada independientemente de `P_1`. |
| F3 valor y persistencia | Valor directamente utilizable, garantía, autoridad rica, antiguos consumidores e indicadores auxiliares | Focal shared exit 0, cuatro casos de contrato. Desktop reprodujo `m = 6.28 (Certified)` y `f = m + 1` con resultado `7.28`. El cono histórico preserva las marcas auxiliares grabadas de `m_2` y `n_2`. |
| F4 eventos y cortes | Propagación tipada, organización persistente de extremos y trazo derivado | Caracterización real del cono y diseño de composición en [diseño F4](../architecture/geocedg-1.2.0-locus-cuts-design.md). Verificación del incremento en preparación. |
| F5 | Contadores reproducibles, medición separada del trazado y comparación preservando diagnósticos | `f5-comparison.md` contiene cuatro ejecuciones A/B con valores, contadores y diagnósticos coincidentes. La variante de caché no redujo el trabajo en el cono y su revisión descubrió un control de dependencia aleatoria que la invalidaba. El código de producción conserva las tolerancias, presupuestos y métodos. |
| F6 | Guías ES/EN, transferencia editorial y empaquetado | [Transferencia editorial](geocedg_1_2_0_editorial_handoff.md) y [guion de smoke](geocedg_1_2_0_author_smoke.md) preparados. Preflight de herramientas aprobado. Fecha de versión autoral pendiente. |

## Revisiones independientes y correcciones

1. **F1.** Revisión de interacción y UI. Corrección del selector manual de Input Box y de su estado tras captura, de la propiedad de grupo mixto que consumen las pruebas y del recurso visual del modo explícito. Computer Use detectó además la exclusión de un punto elegible por los límites de los dibujables. La corrección toma las dimensiones del viewport real. Evidencia en `f1-review-record.md`.
2. **F2.** Revisión científica y de DAG. La ruta de colocación recuperó el adaptador existente con prioridad y estado de selección comprobados. El productor de estructura métrica reconoce el nuevo punto explícito como origen si el enlace pertenece a la revisión vigente y es independiente del conductor. Se añadió la entrada al asistente por el inspector de un resultado de Álgebra inadmisible. Se revisó de nuevo el código. La integración modal y la prueba interactiva de Undo/Redo siguen pendientes.
3. **F3.** Revisión de persistencia y UI. El selector del inspector oculta los padres que ya tienen un resultado numérico `LocusLength` representado en la lista. Las métricas históricas auxiliares siguen accesibles. La regresión amplia descubrió que los auxiliares podían ocupar etiquetas ordinarias en el momento de crear o activar el padre de una consulta. Los auxiliares nuevos usan el espacio oculto del kernel, incluido el flujo de redefinición diferida. Se añadieron pruebas de resultado rico ordinario, auxiliar histórico y valor numérico de recorrido explícito que conserva el estilo elegido por el usuario. Queda consolidar la nueva ejecución.
4. **F4.** Revisión científica del testigo de corte, autenticidad de sus datos y límites de exploración. La dirección exacta mediante diámetro queda acotada a la autoridad de representación heredada. Los extremos que pierden un operando quedan no resueltos y el trazado exploratorio evita unirlos. Se verificó la conservación de los cortes y ambos subtrazos del cono en la sonda `cone-cuts-r2.log`. La sonda `cone-cancellation.log` verificó un caso exacto de cancelación periódica y su control de divisor distinto con valor coincidente. Ambas devolvieron 0. La prueba de separación lateral de `s_2` conserva su clasificación de evidencia analítica fuera de la clase certificada del producto.

## Ejecuciones registradas

| Ejecución | Resultado y evidencia |
| --- | --- |
| Baseline `gradlew.bat :desktop:desktop:runGeoCeDG --offline --console=plain` | 0, `desktop-baseline.log`. Se reprodujo el comportamiento de flyout nativo y mixto en ventanas reales. |
| Segunda ejecución `runGeoCeDG` | 0, `desktop-after-f1-retry.log`. Comparación de flyouts en `desktop-after-native-to-mixed-*.png` y `desktop-after-mixed-to-native-*.png`. Capturas `desktop-after-scalar-formula.png`, `desktop-after-redefine-capture.png`, `desktop-after-input-box-algebra.png`. No corresponde a la última corrección de captura gráfica. |
| Sonda Desktop del cono y contraste con una consulta | 0 ambas, `cone-both-gui.log` y `cone-single-gui.log`. Parámetros, valores, contadores y limitaciones en `cone-baseline-characterization.md`. |
| Gradle focal con `tools/agent/checks/gradle-command-evidence-producer.ps1 -GradleTask :shared:common-jre:test` | 0 interno, 13 pruebas entre asistencia, contrato numérico y comandos, 0 fallos, `f2-f3-focused/command.evidence.json`, `stdout.log` y `stderr.log`. |
| `tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU6` | Diagnóstico en árbol de trabajo. Primer intento bloqueado por caché Gradle en sandbox. Ejecución elevada 115 pruebas, 1 fallo de propiedad de grupo identificado y corregido. `f1-cu6-phase-elevated/verification-result.json`. |
| Baseline `tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain` | 0, `ACCEPTED / COMPLETE` 3/3 y 0 hallazgos, `verification-63c2e9f87b9340f6bb6520e7483ab398`, `baseline-packaging-isolated-canonical.log`. Solo toolchain. |
| Baseline `tools/agent/verify.ps1 -Profile WORKSTATION` | 0 con runtime nativo por escalada gestionada, `baseline-workstation-native.log`. |

`GUIDE_IMPACT = UPDATED`, con ediciones ES/EN e inventario de cobertura. Bootstrap sin cambio de prerrequisitos. Las fuentes de producto, los contratos geométricos y de interacción, las pruebas y la verificación de fase se separan de los artefactos generados. El autor designó `2026-11-16` como `application.version_date` de 1.2.0. El empaquetado definitivo espera el árbol técnico fijado. La aprobación del candidato y la publicación son decisiones autorales posteriores.
