# GeoCeDG · I3–I5 · Inserción experta integrada

**Propuesta de ejecución pendiente de autorización autoral.** Identificador
de fase propuesto `POST-CU4-I3-I5-EXPERT-INSERTION`.

<!-- geocedg-field: objective -->
## Objetivo

Entregar en una misma actuación una revisión experta explícita, una
transacción aditiva con vigencia y recuperación, y las filas de familia del
piloto que superen sus pruebas. Aplicar `EXP-01`–`EXP-04` de la
[propuesta de contrato](../../../docs/architecture/post_cu4_expert_publication_contract_candidate.md)
en los términos autorizados por el autor. La primera envolvente candidata es
2D pasiva y experimental, con controles elementales y texto terminal pasivo,
y una fila experta de arco circular directo separada de la posible fila P4.

<!-- geocedg-field: implementation_base -->
## Base exacta

Usar el commit y árbol limpios que el autor seleccione del cierre PRE-I3,
descendiente de I2 `6ec68e788a81ff10da19ad64d7e721ed1d00ebb5`.
Registrar la ascendencia, los checkpoints de C1 y C2, la matriz G1–G8, el
inventario de evidencia y el SHA de `main` publicado. La autorización debe
seleccionar una base exacta y disponer los cuatro puntos `EXP`. Aplicar
`AGENTS.md`, código y pruebas, [F3/P4/B2](../../../geocedg/specs/ui/document-insertion.md),
[identidad nativa](../../../geocedg/specs/ui/native-document-identity.md),
[identidad espacial](../../../geocedg/specs/spatial/g9-spatial-projection-semantics.md),
unidades D1, los requisitos operativos y las enmiendas expresamente aprobadas.
Los informes y sondas prueban sus propios candidatos y casos.

<!-- geocedg-field: allowed_scope -->
## Alcance y orden de trabajo

1. **Acreditar la costura I2/I5 antes de mostrar una oferta.** En Desktop
   integrar `GeoCeDGExpertInspection`, el resultado tipado y la captura viva
   I2. Registrar una política versionada por clase, productor, variante,
   dominio, puertos y consumidores. Acreditar traza nativa de todas las
   salidas copiadas, productores y puertos, incluida la reapertura, con
   observadores efímeros si la API actual es insuficiente. Acotar cualquier
   enganche a `CopyPasteD`, `InternalClipboard`, `Construction` y los puntos
   nativos del escritor/lector estrictamente necesarios para la prueba de
   `EXP-02`. Validar integridad, resultados, persistencia y diagnósticos del
   worker. Aplicar el oráculo ARC-01 de I1 al primer arco y documentar las
   causas concretas de cada fila que quede en inspección.
2. **I3 · Acción y sesión de revisión.** `apps/geocedg/application-profile.yml`,
   los recursos EN/ES y los puntos Desktop `GeoCeDGActionRegistry`,
   `GeoCeDGMenuBar`, `AppGeoCeDG`, `GuiManagerGeoCeDG` y sus servicios de
   revisión. Integrar la acción experimental, modelo de revisión, ciclo de
   vida del proceso, resumen accesible, detalle por objeto, cobertura y
   condiciones de publicación. El controlador crea y consume ofertas y
   recibos de un uso. El worker y el diálogo no producen efectos sobre la
   construcción viva. Las ausencias de seguridad, integridad o recuperación
   dejan el botón de confirmación inhabilitado con motivo explícito.
3. **I4 · Transacción y recuperación.** Extraer la orquestación mínima desde
   `GeoCeDGDocumentInsertion` y usar `GeoCeDGInsertionCopyPaste`,
   `GeoCeDGUndoManager`, `GeoCeDGRecoverySnapshot` e
   `GeoCeDGInsertionIsolation` para la misma barrera F3. Añadir estricta
   validación de diagnósticos para el piloto y un alcance de reservas de
   etiquetas que restaure el conjunto anterior. Revalidar un sello vivo que
   invalide incluso después de una reversión. Cotejar el origen exacto, el
   plan y el consentimiento en su punto de uso. Ejecutar la copia nativa,
   certificar el receptor y las adiciones antes de publicar la historia,
   y preservar un solo paso Undo, UUID, archivo, unidades y D1. Conservar
   K1-S, K1-R-H e aislamiento grave con certificados separados. La ruta
   pública F3 conserva su admisión y su verificación.
4. **I5 · Persistencia, guardado y familias.** Integrar las filas autorizadas,
   con estado definido, límites, variación, degeneración, consumidor y
   persistencia. Los casos fuera de fila permanecen inspeccionables cuando
   son pasivos. Incorporar a `GeoCeDGDocumentPolicy` y al ciclo de guardado
   Desktop la marca de sesión y la decisión de Guardar como con destino
   distinto. Pasar por la escritura atómica ya usada por el producto.
   Resolver Undo/Redo, cierre, Save As cancelado y asociación tras guardar.

Dentro de la autorización, añadir regresiones Desktop y de los ganchos
nativos, la especificación separada de inserción experta, enlaces normativos
dispuestos, inventario `docs/upstream/modified-files.yml`, guía EN/ES que
corresponda, registro de fase y evidencia. La geometría y el DAG conservan
su autoridad en el kernel. La revisión, el aislamiento de aplicación y
la decisión de publicación viven en Desktop.

<!-- geocedg-field: forbidden_scope -->
## Límites

- La ampliación P4, la edición de B2, las conversiones de unidad o ángulo,
  los cambios de formato, los IDs nuevos en disco y las omisiones de
  geometría o dependencias pertenecen a otras autorizaciones.
- Los documentos activos, macros, imágenes y recursos, CAS, 3D, autoridad
  espacial, identidad durable participante y el receptor no recuperable
  quedan fuera del piloto publicable. La UI identifica inspección estática
  o bloqueo según la política.
- Preservar la ruta ordinaria, Classic, el portapapeles global, los contratos
  del escritor y lector, y la política general de copia. Los ganchos de
  `EXP-02` son optativos para la ruta experta. Evitar rutas paralelas de
  mutación, un segundo serializador o una nueva autoridad geométrica.
- Cualquier herramienta de confinamiento nueva requiere la disposición
  autoral del cambio de prerrequisito. El piloto pasivo bloquea capacidades
  para las que esa separación adicional resulte indispensable.

<!-- geocedg-field: required_checks -->
## Pruebas y aceptación

Usar [verificación canónica](../canonical/verification.prompt.md), la
[clase congelada](../../../geocedg/specs/operations/verification-levels.md#128-frozen-impact-classification)
y la matriz siguiente. Ejecutar primero el productor de evidencia Gradle
`tools/agent/checks/gradle-command-evidence-producer.ps1`, con la tarea
`:desktop:desktop:test`, filtros explícitos, `-EvidencePath` y
`-OutputDirectory` propios. Añadir las suites de `source/shared` que cubran
un enganche autorizado, por la tarea y filtros correspondientes. Conservar
entradas, hashes, procedencia de runtime, logs, JUnit y códigos internos.

| Prueba | Resultados observables |
|---|---|
| Revisión I3 | Acción explícita separada, EN/ES, accesibilidad y modo predeterminado. Mostrar diferencia, incertidumbre y bloqueo separadamente. Cancelación, cierre, timeout y respuesta tardía liberan auxiliares y dejan intactos receptor, ruta, selección, herramienta e historial. |
| Vigencia | Cambio de archivo, sustitución del receptor, redefinición, cambio de dependencia, editar y revertir, Undo/Redo, historial pendiente, vista, capa, recurso, preferencias y cambios de versión. Consentimiento anterior, duplicado, truncado o aplicado a otro plan provoca rechazo previo. |
| Integridad y procedencia | Homónimos y coordenadas iguales, puertos del mismo tipo intercambiados, salida secundaria o anónima perdida, cambio de productor, listas del mismo tamaño distintas, identidad duplicada y diagnóstico recuperable del lector. Ausencia de traza no se convierte en eliminación ni equivalencia. |
| Capa y familias | Reproducir la sonda reconciliada C1, incluidos capa 0→4 y `f3i01`, `f3i02`, `f3i07`. Controles elementales y de texto terminal. En arco ARC-01, radio, orientación, costura, degeneración, consumidor, variación y recuperación. Curva cartesiana y secuencia revelan límites cuando no tienen fila. |
| Persistencia y guardado | Diferencia anunciada y conservada, fallo por diferencia nueva tras reabrir, igualdad de archivos nativos del ensayo y commit o verificación de reapertura real. Save As a otro destino, origen y receptor intactos, alias, cancelación, fallo de escritura, Undo/Redo y reapertura. |
| Seguridad | ZIP/XML hostil, gramáticas divergentes, presupuesto agotado, informe del worker malformado, script, biblioteca JS, listeners, animación, comando activo, macro igual/incompatible, recurso ausente o externo y ruta escapada. Verificar rechazo antes del punto de efecto, limpieza, sin registros residuales y sin ejecución de contenido. |
| Transacción y aislamiento | Fallos inyectados antes y después del snapshot, mutación, preflight, diagnóstico, store y attach. Pruebas de K1-S y K1-R-H, doble fallo de recarga, store inesperado, pérdida de reservas previas, fallos de certificado, historia y cancelación de guardado de P7. Verificar los únicos caminos de salida del aislamiento y el archivo previo intacto. |
| Regresión | `GeoCeDGExpertInspectionTest`, `GeoCeDGInspectionArchiveTest`, `PreG9BR6PlusF3InsertionTest`, `PreG9BR6PlusF3RecoveryTest`, `PreG9BR6PlusF3RecoverySnapshotTest`, `PreG9BR6PlusF3UndoAccountingTest`, `PreG9BCu4InsertionTest`, `Cu4B2InsertionTest`, captura viva I2 y rutas Classic afectadas por los ganchos. |

La actuación futura declara `CHANGE_ROUTE=ORDINARY` y congela la clase al
autorizar. Se propone `INTEGRATED_PHASE` con `PHASE` y cobertura adicional
`INTEGRATION` por el cruce concreto de menú, kernel, portapapeles, Undo,
escritor/lector y ciclo de archivo. Si la autorización y los impactos exigen
`FINAL`, ejecutar una sola campaña física sobre el SHA exacto. No escalar
desde la clase congelada por conveniencia. Registrar la fase, sus hojas
SEMANTIC/PRODUCT y SAFETY, `CLOSEOUT_READINESS` y el resultado de `-PlanOnly`
para la cobertura exigida. Un plan incompleto impide aceptación. `DEV`
incompleto carece de autoridad de aceptación. En el candidato final limpio,
reconciliar el delta, ejecutar `git diff --check`, `verify.ps1 -Profile STATIC`
y la cobertura congelada por `verify.ps1`. Atribuir cada resultado a su
SHA y árbol. Cotejar impacto bootstrap, infraestructura y `GUIDE_IMPACT`.

<!-- geocedg-field: authorization_boundary -->
## Autorización y paradas

La instrucción futura debe indicar commit, árbol, clase congelada, alcance de
`EXP-01`–`EXP-04`, pruebas autorizadas y ganchos nativos permitidos. La
redacción de esta propuesta no autoriza cambios. Con autorización completa,
resolver defectos focales dentro del perímetro y añadir sus regresiones.
Si una fila incumple seguridad, puertos, vigencia, persistencia o recuperación,
mantenerla sin publicación, preservar evidencia e identificar la condición
concreta. Una barrera común sin certificado, una regresión F3/Classic, un
riesgo de ejecución o un cambio de formato detienen el candidato integrado.
Documentar qué puerta falló y qué decisión o evidencia falta. No sustituir
una aprobación técnica por el smoke manual reservado al autor.

<!-- geocedg-field: publication_boundary -->
## Entrega y frontera Git

Entregar un candidato técnico local congelado con informe, matriz de filas y
pruebas, inventario de archivos, manifiesto SHA-256, impactos, límites y
guion de smoke EN/ES para autor. La autorización de I3–I5 incluye solamente
los commits locales descritos. Push, integración en `main`, tags, releases y
cierre autoral requieren decisiones separadas que nombren el SHA exacto.
