# PRE-G9B-CU3 — Hoja de resultados del smoke autoral de `CU3-A`

| Campo | Valor |
|---|---|
| Carácter | Hoja en blanco para el autor. Ningún resultado está anotado: el agente no ejecuta ni simula el smoke |
| Candidato | `9f6f27f265725b27673f5c37880947ad6c4f2399` (árbol `abe10e3d4eda338be2b3c36373c4b09af21e7fdd`) |
| Guía | [guía de prueba manual](pre_g9b_cu3_manual_test_guide.md) |
| Lanzamiento | `gradlew.bat :desktop:desktop:runGeoCeDG` desde la rama local `phase/pre-g9b-cu3-implementation`, sin cambios de producto respecto de `9f6f27f2` |
| Fecha y versión de Java | _(a rellenar por el autor)_ |
| `selfApproved` | `false` |

Cada fila se marca con `PASS`, `FAIL` o `NOT TESTED`. En la observación van la configuración, la
posición del modelo y la causa que muestre el inspector.

| Bloque | Paso | Comprobación | Resultado | Observación y evidencia |
|---|---|---|---|---|
| A. Autoridad constructiva | 1–2 | oferta y cancelación sin efecto ni paso de Deshacer | | |
| A | 3 | adopción con un único paso de Deshacer y Rehacer | | |
| A | 4 | diálogo en inglés | | |
| B. Cinco incidencias | 5–6 | `X1` a `X5` definidos y `STRICT` (pertenencia por transferencia; incidencia con el objetivo constructivo por `CONSTRUCTIVE_IDENTITY_CERTIFICATE`) | | |
| C. Contactos | 7 | pliegues de `e_1` (`0` y costura), tangencia `TWO` de `u_1`, sin raíces contradichas ni tokens reasociados; política `v2` en los diagnósticos | | |
| D. Dinámica | 8 | arrastre de `H`: `X1` a `X5` `STRICT` | | |
| D | 9 | arrastre de `B` | | |
| D | 10 | `D_1` ×3: `X1` y `X5` `NOT_ON_LIMITED_TARGET`, `X3` indefinido, `X2` y `X4` `STRICT` | | |
| D | 11 | `α_cone` a 35°: solo los dos contactos en pliegue | | |
| D | 12 | `D_1` a la mitad: una sola tangencia de `u_1` | | |
| D | 12a | `rbCone` a 7,5: raíz doble certificada de `u_1` (T7-a) | | |
| D | 12b | `rbCone` a 3 y `α_cone` a 60° | | |
| D | 12c | recuperación de la configuración guardada | | |
| D | 13 | deshacer la adopción restaura la raíz transversal de la costura con su token | | |
| E. Persistencia | 14 | guardar, reabrir, Deshacer y Rehacer; política e identidades restauradas; sin evidencia obsoleta | | |
| E | 15 | rechazo cerrado por un producto anterior; una copia sin adoptar se abre | | |
| F. Rendimiento | §E | adopción | | tiempo percibido |
| F | §E | primer arrastre | | tiempo percibido |
| F | 16 | arrastres sucesivos y fluidez | | |
| F | 16 | pausas anómalas | | posición y momento |

**Resultado global** (a rellenar por el autor):

- funcional: `PASS` / `FAIL`;
- rendimiento interactivo: `ACCEPTED` / `NOT ACCEPTED`.
