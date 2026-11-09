# PRE-G9B-CU5/CU6 — Cierre autoral conjunto

**PRE-G9B-CU5 — PASS — AUTHOR APPROVED**

**PRE-G9B-CU6 — PASS — AUTHOR APPROVED WITH EXPLICIT LIMITATION**

El autor comunicó ambas decisiones el 2026-11-09 en la instrucción
«GeoCeDG — PRE-G9B-CU5/CU6 · Cierre autoral conjunto, publicación y preparación
de CU8-I». Confirmó su smoke manual sobre el candidato y aceptó expresamente
la limitación M07 que se registra abajo. `selfApproved=false`.

| Identidad | Valor |
|---|---|
| Producto técnico aprobado | `547fdee503f837218db06991c6c17be366c10578` |
| Árbol del producto | `a22d1fe136fdbcc6dd953527394751c30935a082` |
| Rama técnica | `codex/pre-g9b-cu5-cu6` |
| Baseline de `main` verificado en GitHub | `b842165733261126e0f742586834e2bfc612b6f1`, árbol `607af3a916025d8a1bbd5cc8f4a21c9a95d7fd66` |
| Descendiente exclusivamente documental | El commit que contiene este registro en `codex/pre-g9b-cu5-cu6-closeout`, hijo directo del producto aprobado |
| Espejo estructurado | [pre-g9b-cu5-cu6-closeout.json](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu5-cu6-closeout.json) |

## CU5 — Protocolo, memoria y orientación

| Incidencia | Perímetro adoptado |
|---|---|
| B02 | La tabla Swing calcula el alto por fila con sus renderizadores y la fuente vigente. Dos líneas fijan el mínimo. Un contenido más alto amplía la fila con su margen. Apertura, cambios del modelo y de presentación actualizan las medidas. Navegar por el protocolo conserva el orden constructivo. |
| B03 | Paralela y Perpendicular tienen activación y ranuras de sesión independientes en Gráficos 1 de GeoCeDG Desktop. Una construcción satisfactoria certifica la referencia utilizada. La barra indica modo, disponibilidad, suspensión o invalidación. La memoria es de sesión y carece de escritura XML y de pasos Undo propios. |
| H05 | Un objeto de un paso posterior suspende su ranura. Avanzar puede recuperar la misma instancia vigente. Undo/Redo, reconstrucción o sustitución del documento invalidan las ranuras según el contrato. La vigencia de las referencias ordinarias se prueba por instancia y paso constructivo. |
| M04 / DC-4 | `xAxis` y `yAxis` canónicos transmiten respectivamente los sentidos positivos de X e Y. Solo las rectas resultantes elegibles y sus cadenas muestran el indicador. |
| Admisión focal de ejes | `Kernel.isAxis` acredita la instancia canónica del kernel actual. Su disponibilidad exige que el eje esté definido, visible y seleccionable en Gráficos 1. Ocultarlo suspende el uso y mostrar la misma instancia puede restaurarlo. La memoria se adquiere al certificar la recta y se invalida en las mismas fronteras que las demás ranuras. La excepción conserva a ambos ejes fuera del orden constructivo y de la identidad durable. |

El [diseño CU5](../architecture/pre_g9b_cu5_implementation_design.md), la
[memoria de autoría](../../geocedg/specs/ui/authoring-memory.md) y la
[orientación F1/M04](../../geocedg/specs/presentation/orientation-cue.md)
concretan la adopción. Etiquetas, coordenadas, ecuaciones y proximidad carecen
de autoridad de reasociación.

## CU6 — Interacción, presentación y persistencia

| Incidencia | Perímetro adoptado |
|---|---|
| B04 | Al seleccionar una salida visible y definida de la intersección ordinaria recta–cónica, la insignia refleja el índice actual base uno del productor. El índice es presentación. Las intersecciones ricas conservan sus tokens semánticos. |
| M02 | ESC cierra el menú compacto y descarta el texto de grosor pendiente. Conserva la herramienta y las ediciones ya confirmadas. |
| M03 | El arrastre de rótulos en las vistas gráficas de GeoCeDG amplía el recorrido heredado. El recorte de presentación al borde conserva el desplazamiento elegido para cuando vuelva a caber. El lector y escritor conservan los dos enteros de `labelOffset`. |
| M06 | Búsqueda por nombre visible en Álgebra con coincidencias parciales y exactas, subíndices y selección con desplazamiento a la fila. La consulta sigue el árbol vigente. |
| M07 | Captura explícita de argumentos numéricos en los cuatro campos del diseño CU6-D. La aceptación alcanza los contextos comprobados, incluida Álgebra, con la limitación de deslizadores de Gráficos 1 que se detalla abajo. |
| M10 | La ayuda emergente ofrece Valor o Definición para complementar el modo efectivo de la fila. Tiene textos bilingües para indefinidos y definiciones ausentes, y limita la presentación larga a 240 puntos de código más elipsis. |

En M07 el texto se introduce en el cursor o en la selección del campo.
La confirmación vuelve a verificar la instancia y la vigencia de la referencia,
del documento y de los operandos. Una coincidencia de etiqueta no acredita
identidad. Al caducar la prueba se exige una selección nueva.
[Perímetro y decisiones CU6-D](../architecture/pre_g9b_cu6_d_interaction_design.md)

Se incorporan por referencia las enmiendas de compatibilidad y persistencia
del 2026-11-09 que ya constan en el árbol técnico aprobado. El cotejo R5-B/D1
en GeoCeDG proyecta únicamente las nuevas líneas de capa numérica que
satisfacen las condiciones de propietario, sintaxis, posición, causalidad,
ausencia histórica y persistencia. Los bytes restantes se cotejan con las
huellas históricas, con la excepción CU7 de `conicPointState` verificada por
separado. Classic conserva D1.7 en su ruta histórica. En sus rutas nativas
validadas y snapshots locales, Classic conserva las capas numéricas explícitas
admitidas, incluido `0`. La omisión genuina conserva la política heredada.
[Identidad y persistencia nativa](../../geocedg/specs/ui/native-document-identity.md#author-approved-amendment-of-2026-11-09--cu6-explicit-numeric-layers) ·
[D1 §8.3](../../geocedg/specs/units/unit-system.md#83-omission-rule-and-legacy-byte-identity) ·
[ADR 0031 y R5-B](../adr/0031-canonical-english-command-surface-compatibility.md#author-approved-amendment-of-2026-11-09--cu6) ·
[Perfil Classic](../../geocedg/specs/ui/application-profile.md)

### OBS-CU6-M07-GV1-SLIDER-PICK

**Deuda abierta y no bloqueante para CU6 por decisión expresa del autor.**
En `cu6-m07-reproduccion.cedg`, elegir Homotecia, pulsar la spline V2 original
`S` y luego el centro `O`. Escribir `1+*2`, situar el cursor tras `+`, armar
«Seleccionar objeto» y pulsar un deslizador visible en Gráficos 1. Con `K`
y con `c` creado por la herramienta estándar, el diálogo rechazó el clic,
conservó el texto y mostró «No numeric reference is available here».
La misma ruta se reprodujo con `c` en `0728b0948` y en `547fdee50`.

Desde la fila de Álgebra, la selección comprobada inserta el nombre en la
posición del cursor, conserva el texto y permite confirmar el resultado.
ESC desarma primero la captura y después cancela el diálogo. La aceptación
cubre este perímetro y mantiene abierta la inserción del nombre por clic sobre
un deslizador de Gráficos 1. Para CU8-I se compararán deslizadores sueltos y
agrupados durante una captura M07 activa. El clic debe quedar aislado de la
edición de panel y del cambio de valor. La selección desde Álgebra y la
cancelación conservan su conducta comprobada. La limitación gráfica es una
línea base de regresión, no una puerta que exija reparar M07.
[Matriz CU8-I](../architecture/pre_g9b_cu8_d_control_containers_candidate.md#4-matriz-de-validación)

## Evidencia y atribución

| Evidencia | Cohorte y resultado |
|---|---|
| Foco de memoria de ejes | 52 pruebas satisfactorias, código 0, 0 omisiones. Evidencia de la corrección inmediatamente antes de congelar el candidato. PHASE volvió a verificar las clases de memoria y orientación en el SHA exacto. |
| PHASE CU5 | `verification-65f0a05cae344b31a4c081f9133c9750`, código 0, `ACCEPTED / COMPLETE`, 4 contratos y 57 casos en `547fdee50`. |
| PHASE CU6 | `verification-f242fe2b9ffa47e0bde2d69ed8f717f8`, código 0, `ACCEPTED / COMPLETE`, 4 contratos y 114 casos en `547fdee50`. |
| INTEGRATION | `verification-a6a750c5b255430384b74f6f3881ca4d`, código 0, `ACCEPTED / COMPLETE`, los 19 contratos satisfechos en `547fdee50`. Las regresiones incluyen el cotejo R5-B/D1, las capas numéricas y MIG06 en GeoCeDG y Classic. |
| Persistencia ampliada | Evidencia histórica en `0728b0948`, con corpus de 68 filas por perfil, 67 cotejos y una exclusión histórica, capas 6/3/0, cuatro capas espaciales 0 y conservación en archivo e historial Classic. La campaña de ese SHA se registra separadamente. |
| Smoke gráfico del agente | GeoCeDG Desktop real en `547fdee50` mediante Computer Use. El autor hizo los dos Ctrl+clic de activación. El agente comprobó cuatro pares herramienta/eje, reutilización, alternancia, cancelación, Undo/Redo y reapertura de diez rectas. M07 se comprobó con el resultado anterior. |
| Smoke manual del autor | `PASS — AUTHOR REPORTED`, comunicado en la instrucción de cierre. La limitación explícita de M07 acompaña a CU6. Este registro conserva la comunicación del autor y la evidencia gráfica del agente como entradas distintas. |

Las omisiones declaradas por las suites integradas, los tres avisos de
Checkstyle de las pruebas focales y la ausencia local de `Revision3.cedg`
conservan su clasificación. Los recibos rechazados de las cohortes
históricas conservan su resultado.

El manifiesto local `artifacts/cu5-axis-memory/manifest.json` tiene SHA-256
`93b60e9be71325764eba2506388c4937cb13824b04c2af49b421b5a444ddf153`.
El manifiesto de la reconciliación nativa
`artifacts/cu6-native-reconciliation/manifest.json` tiene SHA-256
`d534e7b987e8db08a89e8d121ee4161660e3c2fc4749bbc1fb31d7855ecda3d4`.
Las huellas de los informes canónicos y las rutas de la evidencia se
detallan en el espejo estructurado.

## Publicación y transición

El autor autorizó el avance rápido de `main` al descendiente documental de la
tabla. Su puerta es `STATIC`, con el cotejo exhaustivo del delta y del espejo.
La campaña de producto corresponde al SHA técnico y el plan consolidado
reserva `FINAL` para CU10. Las identidades del cierre, el resultado de
`STATIC` y la observación del remoto quedan en
`artifacts/agent/pre-g9b-cu5-cu6-closeout/` de la raíz local.

CU8-D tiene dirección de complejidad media aceptada. CU8-I es la siguiente
actuación autorizada en una sesión nueva. Su alcance es Desktop, Gráficos 1,
deslizadores, botones, casillas y campos de entrada, con anclaje fijo o
vinculado al lienzo. La conciliación separa el alcance adoptado y las
decisiones de formato e interoperabilidad previas al código.
[Adopción de CU8-D](../architecture/pre_g9b_cu8_d_control_containers_candidate.md#5-adopción-y-puertas-de-cu8-i-2026-11-09)

El [cierre reducido de CU7](pre_g9b_cu7_r5_author_decision_record.md) conserva
su perímetro. CU9 y CU10 permanecen reservados.

```text
VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
BOOTSTRAP_JUSTIFICATION = Este cierre conserva los ejecutables, el verificador y los prerrequisitos de la estación.
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = Las dos guías vigentes ya describen la memoria de ejes y el alcance Classic. La precisión M07 queda en el diseño, el registro y el plan vivo. La revisión bilingüe general corresponde a CU9.
```
