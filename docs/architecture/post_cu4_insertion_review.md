# POST-CU4-INSERTION-REVIEW

**Candidato de estudio y diseño para revisión autoral.** Base publicada
`d5332e400b0ab68afd14273495b9ae5f03e6c911`, árbol
`e8ee4b4b371d34f7f22cc081dee1f429963150dd`. Producto aprobado en CU4
`ba4d16018cf683750a20619d9d59c84428bcbef7`.
El [cierre de CU4](../validation/pre_g9b_cu4_closeout_record.md) y el
[contrato de inserción v1.3](../../geocedg/specs/ui/document-insertion.md)
fijan el comportamiento vigente. El estudio desarrolla la
[propuesta de seguimiento](pre_g9b_cu4_p4_expert_insertion_followup_proposal.md).

| Entrega | Contenido |
|---|---|
| [Inventario P4](post_cu4_p4_inventory.md) | Clases, productores, dependencias y decisiones A1–A4 |
| [Sondas con documentos temporales](post_cu4_p4_probe_evidence.md) | Rechazo ordinario, copia nativa, estado, historia y reapertura en ejemplos reproducibles |
| [Diseño de inserción experta](post_cu4_expert_insertion_design.md) | Aislamiento, estados, análisis diferencial, interfaz, consentimiento y recuperación |
| [Selector angular](post_cu4_angle_preferences_observation.md) | Causa, reproducción Swing y candidato de corrección |

## Hallazgos que condicionan el diseño

- El arco circular directo, el sector, las dos cónicas focales y la curva
  cartesiana del ensayo cargaron y recibieron el rechazo ordinario de P4.
  Sus testigos de valor cambiaron de forma coherente al editar entradas
  después de copiar en un receptor desechable y reabrir. Curva y secuencia
  produjeron objetos nuevos con mapa de copia vacío. La primera fila P4
  debe especificar el productor y las aristas, con su propio oráculo.
- La copia directa cambió el UUID y careció de un checkpoint automático
  en los siete casos sin identidad espacial. La modalidad experta necesita
  el perímetro transaccional y la verificación del resultado. Dos controles
  F3 esperados como positivos fueron rechazados por capa en el runtime local.
  I1 debe resolver la discrepancia con evidencia ligada al build.
- La sonda angular reprodujo el modo obsoleto del panel Global existente.
  Un refresco explícito sincronizó los tres modos sin eventos de acción de
  los radios. I0 puede validarse como corrección Desktop independiente.

## Elección arquitectónica

| Opción | Cobertura | Seguridad y mantenibilidad | Reutilización de F3 y compatibilidad |
|---|---|---|---|
| I. Ampliar P4 | Aumenta por productor, dominio, dependencias y consumidores comprobados. | Preserva una garantía uniforme por fila. Cada productor y degeneración añadidos amplían el coste de regresión. | Reutiliza admisión, ensayo y transacción. Los testigos geométricos nuevos complementan las comprobaciones estructurales de F3. |
| II. Añadir inserción experta | Ofrece diagnóstico para casos que carecen de fila certificada. El alcance publicable depende de la integridad y de poder explicar cada pérdida. | Concentra el coste en aislamiento, comparación y recuperación. Las pérdidas conocidas y la incertidumbre requieren un contrato de publicación. | Reutiliza primitivas de F3 y carga nativa detrás de una frontera de seguridad nueva. La comparación por procedencia exige un protocolo adicional. |
| III. Conservar ambas modalidades | Permite certificar los casos repetibles y estudiar por separado archivos con mayor variabilidad. | Los dos flujos mantienen garantías distintas y una base común de seguridad. Tiene el mayor coste inicial y permite entregas independientes. | Mantiene la ruta certificada y aprovecha la barrera, el escritor nativo, Undo/Redo y el aislamiento donde su contrato resulte suficiente. |

Se propone **III**, con **Insertar archivo…** como acción certificada e
**Insertar con revisión experta…** como nombre provisional de una acción
separada. Un rechazo ordinario puede indicar la disponibilidad del estudio
experto cuando exista. La segunda acción exige una elección explícita. Una
fila P4 se justifica por conservación demostrable en su dominio completo.
Una oferta experta se justifica por la integridad del resultado concreto y
por el conocimiento de las diferencias que el usuario puede aceptar.

## Ubicación y dependencias

Se propone una actuación independiente de **interoperabilidad de documentos**
posterior a PRE-G9B-CU4, con I0 como corrección Desktop separable y dos líneas
posteriores, P4 y modo experto. El autor decidirá el identificador definitivo
y la secuencia para el [roadmap vivo](../roadmap/geocedg_roadmap.md).

- **Dependencias duras.** I1 consume F3/P4 y respeta D1, M1 y la clase B2
  aprobada. I3 depende de un análisis y un aislamiento comprobables en I2.
  I4 depende del plan revalidable de I2 y del consentimiento de I3. Los casos
  espaciales de I5 requieren identidad, cierre de relaciones, reconstrucción
  y validez de cada tipo.
- **Orden operativo propuesto.** I0 primero por la deuda aceptada en CU4.
  I1 e I2 pueden avanzar en paralelo tras acordar testigos y criterios de
  clasificación. I5 depende de las pruebas de cada familia considerada.
- **Puertas globales.** G9B y G9C conservan sus dependencias publicadas.
  La futura importación de una familia espacial consume su esquema
  autorizado de proyección y degeneración. La ampliación ordinaria 2D y el
  piloto experto pasivo son independientes de ese trabajo espacial.

## Incrementos propuestos

Cada incremento partirá del `main` publicado al autorizarlo y registrará su
SHA y árbol. Los nombres de rama son provisionales. El candidato se congela
antes de su aceptación. La autorización de implementación fijará el
contrato, el perímetro y la clase aplicable según la
[política de verificación](../../geocedg/specs/operations/verification-levels.md#128-frozen-impact-classification).

| Incremento y rama candidata | Autorización y cambio a solicitar | Evidencia focal y puerta candidata | Esfuerzo y riesgo |
|---|---|---|---|
| I0 · `codex/post-cu4-angle-refresh` | Refresco silencioso del selector desde el kernel al completar la transición de documento y las transiciones de historial que restauren el modo. | Regresión EDT con DEG/RAD/DMS, `.cedg` y `.ggb`, cambio/reapertura, panel existente/tardío y restauración del modo por historial. Smoke Swing. `BOUNDED_PHASE` si el delta queda en sincronización Desktop. | Bajo. Reentrancia y cobertura de las distintas entradas de apertura. |
| I1 · `codex/post-cu4-p4-first-rows` | Una primera fila acotada por productor y dominio 2D con su testigo geométrico. Candidatura de arco circular directo sujeta a evidencia, con consumidores semánticos y macros fuera de la primera autorización. | Positivos y negativos por productor, orientación, degeneración, aristas exactas, L1, variación, rollback, Undo/Redo y reapertura. B2, M1 y F3 como controles. `BOUNDED_PHASE` si la autoridad queda local a la fila. | Medio. Una clase Java agrupa productores y regímenes geométricos distintos. |
| I2 · `codex/post-cu4-expert-differential` | Prototipo aislado del inventario, cierre de dependencias, procedencia, diferencias y saneamiento previo. | ZIP/XML hostiles, comandos activos, pérdidas conocidas, clase sustituida, referencias externas, aristas sin etiqueta y cotejo tras reapertura. Límites de recursos y cancelación. Revisión de diseño y prueba focal antes de integrar. | Alto. La carga evalúa comandos y puede perder información antes de comparar. |
| I3 · `codex/post-cu4-expert-preview` | Acción explícita, sesión de vista previa, informe y oferta ligada al contexto. Perímetro limitado a revisión aislada. | Pruebas Swing de selección/cancelación, foco, caducidad por edición o cambio de origen y limpieza. Smoke de accesibilidad y rendimiento. Clase congelada al autorizar. | Medio-alto. El diálogo y los callbacks pueden invalidar la oferta. |
| I4 · `codex/post-cu4-expert-transaction` | Publicación aditiva con aceptación explícita de diferencias autorizadas. Primera envolvente propuesta de 2D pasiva. | Aceptar, cancelar y caducar. Repetir el plan en destino, un paso Undo, Redo y reapertura. Fallos inyectados en barreras, K1-S, K1-R-H y aislamiento. `PHASE` propia más integración si hay cobertura cruzada concreta. | Alto. Equivalencia de transacción, historia viva y recuperación. |
| I5 · `codex/post-cu4-insertion-family-<id>` | Ampliaciones independientes por productor y dependencias. Recursos, herramientas, CAS y espacial requieren decisiones propias. | Persistencia, degradación y dinamismo por familia. En espacial, roles, identidades, correspondencia, degeneraciones y reproyección. Puerta según el impacto de cada candidato. | Variable. Alto con recursos activos, 3D o relaciones espaciales. |

La aceptación futura reutilizará cobertura en el candidato exacto cuando lo
permita el contrato. Para una clase que requiera `FINAL`, una campaña física
cubrirá las obligaciones inferiores autenticadas que correspondan.

## Enmiendas que requieren decisión

1. **P4.** Añadir a §2.2 una fila por productor y dominio autorizado, con
   dependencias, consumidores, estados inválidos y orientación. Complementar
   §2.6 con su testigo geométrico y de persistencia para ensayo y commit.
2. **Inserción experta.** Crear un contrato separado de Desktop/documento
   para el piloto. Referenciar desde §1 las dos acciones y definir estados,
   integridad, consentimiento, caducidad, persistencia, historia y recuperación.
   Una extensión que necesite formato o relaciones persistentes nuevas
   requerirá su propia enmienda de identidad nativa o espacial.
3. **Capacidades activas.** Formalizar la política previa al lector y la
   matriz de conservación pasiva, neutralización verificable, omisión y
   rechazo. La activación de scripts, CAS o recursos externos exige su
   propia autorización y pruebas de aislamiento.
4. **Selector angular.** Documentar el comportamiento tras I0. El modo
   efectivo pertenece al kernel y el panel lo refleja cuando cambia.

## Decisiones para la revisión autoral

1. Aprobar o ajustar la opción III, la ubicación en el roadmap y el orden
   de I0 respecto al piloto experto.
2. Elegir el primer productor, dominio, degeneraciones y consumidores de I1.
3. Fijar la envolvente inicial de I2–I4, el aislamiento y sus límites.
   Decidir si se permite publicar una reconstrucción parcial con pérdidas
   enumeradas y subgrafo íntegro y cerrado.
4. Elegir nombre, tipo de vista previa y texto de consentimiento. Resolver
   si la primera entrega termina en inspección o incluye commit y qué
   experiencia de «Guardar como» debe ofrecer.
5. Acordar la política futura para macros, recursos activos e identidad
   espacial. Delimitar roles, formatos y consumidores para cada ampliación.
6. Autorizar cada incremento con alcance, clase, gate y criterio de smoke.

## Perímetro del candidato de estudio

```text
PRODUCT_PHASE_EFFECT = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = El candidato documenta un estudio y añade sondas aisladas. El producto observable y su contrato operativo conservan su base.
```

Las sondas se ejecutan con el runtime Desktop local y la configuración
portátil de ensayo. La rama contiene documentación candidata y el arnés
P4 fuera del descubrimiento de pruebas de producto. La documentación de
cada ensayo registra la procedencia y los límites del resultado.
