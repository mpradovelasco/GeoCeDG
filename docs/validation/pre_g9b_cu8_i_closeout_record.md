# PRE-G9B-CU8-I — Cierre autoral

**PRE-G9B-CU8-I — PASS — AUTHOR APPROVED**

El 2026-11-10, en «GeoCeDG — PRE-G9B-CU8-I / Cierre autoral y
publicación», el autor confirmó el smoke manual final de R2, aprobó
conjuntamente CU8-I, R1 y R2 y autorizó publicar directamente en
`main` el producto exacto con su descendiente exclusivamente
documental. `selfApproved=false`. El smoke autoral se registra como
`PASS — AUTHOR REPORTED`.

| Identidad | Valor |
|---|---|
| Baseline vivo verificado en GitHub | `876a67362939cb5208a9562e396bbd3add7dfc04`, árbol `211a518644a51e6507a042c6320bdf13b9d8c349` |
| CU8-I | `81b6fc0661a61c7fa7a853ab17584d6a55b39a19`, árbol `b5d4028fa7aa32b95f175e7ac7a2467921cdb289` |
| Checkpoint R1, rama `codex/pre-g9b-cu8-i-r1` | `075407a3142e1afe3cffebb3327a1d8aca9beddc`, árbol `f60e6a0a1c72fbcc5418704296b950132349942c` |
| Producto técnico aprobado, rama `codex/pre-g9b-cu8-i-r2` | `5b4f7f712c7a4fa32881ad58da3c46ff9808f92e`, árbol `0c693aa6b9f6b83e16cf53efd0e7c736cb5155a8` |
| Cierre documental | Commit que incorpora este registro, hijo directo del producto, rama `codex/pre-g9b-cu8-i-closeout` |

La instrucción nombra la rama de R1. Las referencias locales distinguen
R1 y R2, y el SHA y árbol explícitos de la decisión identifican el
producto. [Espejo estructurado](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu8-i-closeout.json).

## Matriz de capacidades y límites

| Área | Perímetro adoptado |
|---|---|
| Superficie | GeoCeDG Desktop, Gráficos 1. Deslizadores, botones, casillas y campos de entrada elegibles. Paneles fijos o vinculados al lienzo. Gráficos 2, anidamiento y transferencia documental de paneles quedan fuera. |
| Disposición y composición | Registro de pertenencia por instancia e ID. El fondo de un panel superior atenúa los controles que cubre. Un control desacoplado vuelve a la pasada nativa. Nombre, color, transparencia y orden se conservan. |
| Interacción | El contexto de un miembro accesible ofrece sus Propiedades y el gestor de su panel. Fondo y encabezado ofrecen el gestor del panel. Los clics respetan la cobertura, el foco y la captura activa. El cierre de Propiedades sin edición conserva Rehacer. |
| Archivo e historial | Persistencia v1, restauración transaccional, Undo/Redo y recuperación. GeoCeDG y Classic de la distribución conservan las propiedades garantizadas. Classic muestra las posiciones de respaldo. Los documentos sin paneles conservan la escritura histórica. |
| Lectores anteriores | Al volver a guardar pueden perder la disposición, el desbloqueo de posición de ciertas casillas y «Nombre y valor» en deslizadores con definición. Se conservan las garantías de geometría, DAG, identidad, valores y dependencias. La distribución nueva muestra el aviso y aconseja conservar el original. |
| Inserción | F3/P4/B2 y la vía experta rechazan fuentes con paneles, incluso vacíos. Se admite un receptor con paneles solo mediante certificación de pertenencia y recuperación, incluido Redo pendiente. La envoltura pasiva experta vigente lo rechaza. |
| M07 | `OBS-CU6-M07-GV1-SLIDER-PICK` sigue abierta y no bloqueante. La captura desde Álgebra y la cancelación conservan su contrato. |

[Contrato v1](../../geocedg/specs/ui/control-layout-v1.md) ·
[Composición y contexto R1/R2](../../geocedg/specs/ui/control-layout-r1.md) ·
[Cierre CU5/CU6](pre_g9b_cu5_cu6_closeout_record.md#obs-cu6-m07-gv1-slider-pick).

## Evidencia atribuida

| Evidencia | Resultado |
|---|---|
| STATIC del producto | `verification-8c3258fd351d43a1b40146c7e5ae2619`, salida 0, `ACCEPTED / COMPLETE`, 3/3. |
| PHASE CU8-I | `verification-90f0bcec514d42deb3ba5120807340df`, salida 0, `ACCEPTED / COMPLETE`, 6/6. 285 casos Desktop y 49 compartidos. |
| INTEGRATION | `verification-3af39acf0b194950b11d98d3c6fa0aba`, salida 0, `ACCEPTED / COMPLETE`, 19/19. 2.512 casos Desktop y 7.732 compartidos, cero fallos y errores, 11 omisiones admitidas. |
| Focales R1 y R2 | R1 pasó 16 casos Desktop y 6 compartidos. El arreglo de recuperación del campo pasó la regresión de primer cuadro tras Undo y miniatura. R2 pasó 12 casos, incluidos cinco de M07. |
| Computer Use | R1 ejercitó S1–S9 y el ciclo GeoCeDG–Classic–GeoCeDG. R2 comprobó los cuatro destinos de Propiedades, la cobertura, el gestor, cancelación, Undo/Redo y reapertura en el SHA aprobado. El XML de construcción guardado en R2 coincidió con el de la escena. |
| Smoke manual | Satisfactorio por comunicación expresa del autor en la decisión de cierre. |

Los informes canónicos y las capturas locales conservan sus identidades
en el espejo. El primer INTEGRATION quedó interrumpido en Desktop.
Windows registró un apagado inesperado. No se atribuye causalidad al
producto. El diario y las huellas se conservan en
`artifacts/agent/pre-g9b-cu8-i-r2/interruption-evidence/`. La última clase
identificable pasó sus 16 casos en el reintento aceptado.

## Publicación y transición

La puerta del cierre documental es STATIC sobre su commit congelado.
El delta se limita a documentación. PHASE e INTEGRATION quedan ligados
al SHA técnico y FINAL permanece reservado a CU10. La publicación se
registra por avance rápido con el baseline vivo de GitHub, los SHA y
árboles de ambos candidatos y las observaciones local, tracking y remota
en `artifacts/agent/pre-g9b-cu8-i-closeout/`.

CU9 es la siguiente fase pendiente de autorización. El traspaso local
`artifacts/agent/pre-g9b-cu8-i-closeout/CU9-TRASPASO.md` reúne los
compromisos B11, B12 y H07, las novedades de CU5–CU8 y sus límites.
[Plan consolidado](../architecture/pre_g9b_cu_corrective_usability_plan.md#cu9--guía-de-usuario) ·
[Matriz de cobertura bilingüe](../../geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json).

```text
VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
BOOTSTRAP_JUSTIFICATION = El delta documental conserva los ejecutables, el verificador y los prerrequisitos.
GUIDE_IMPACT = UPDATED
GUIDE_JUSTIFICATION = Se registra la adopción en la matriz. La cobertura bilingüe efectiva se reserva a CU9.
selfApproved = false
```
