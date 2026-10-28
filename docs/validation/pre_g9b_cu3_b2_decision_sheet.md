# PRE-G9B-CU3 — Hoja de decisiones de `CU3-B2` (G-B2-0)

| Campo | Valor |
|---|---|
| Carácter | Hoja de decisiones de la puerta G-B2-0, para revisión autoral. Cada decisión tiene una recomendación concluyente; ninguna está aprobada |
| Diseño | [diseño de `CU3-B2`](../architecture/pre_g9b_cu3_b2_regular_tangent_design.md); [enmiendas candidatas](../architecture/pre_g9b_cu3_b2_normative_proposal.md); [matriz](pre_g9b_cu3_b2_validation_matrix.md) |
| Estado | **decidida por el autor el 2026-10-28**: D-B2-1(a) a D-B2-7(a) aprobadas, con las precisiones P1 (regularidad de la tangencia: fuente, objetivo, dominio, pertenencia al objetivo limitado, aislamiento y continuidad; sin confundir una raíz doble en una fuente singular con una tangencia regular) y P2 (la corrección de `nextIncarnation` preserva unicidad, determinismo, reclamaciones, latencia, duplicación, Deshacer/Rehacer y reapertura, sin redefinir el contrato de identidad durable); D-B2-3 condicionada a que no se distribuya ningún lector v6 anterior; D-B2-4 limitada a eliminar reservas transitorias indebidas. Autorizada solo G-B2-1 ([expediente](pre_g9b_cu3_b2_g1_characterization_record.md)); implementación no autorizada; `selfApproved=false` |

| Id | Decisión | Alternativas | Recomendación | Fundamento |
|---|---|---|---|---|
| D-B2-1 | Regla de división | (a) `CONTACT_SPLIT`: el token regular no pasa a ninguna raíz; las raíces conservan o adquieren identidad por ADR 0017; (b) herencia del token en una raíz elegida | **(a)**, dirección preferente 1 | (b) es una elección silenciosa y una continuidad por proximidad (ADR 0017, decisiones 8 y 9; `AGENTS.md` §11.4). Diseño §5.2 |
| D-B2-2 | Definición del lado | (a) `σ = sign h''(t₀)` con la orientación canónica del objetivo (curvatura relativa); (b) sin lado: una sola identidad por tangencia y objetivo | **(a)**, dirección preferente 2 | `σ` es invariante por reparametrización regular, por escalar, por indicador equivalente y por vista; solo cambia a través de `h'' = 0`, que es una barrera (diseño §3). (b) obliga a usar solo el rango para separar tangencias de lados opuestos, y el rango no distingue una tangencia que cambia de lado de otra distinta |
| D-B2-3 | Versión del libro | (a) v6 con la entrada `T`; (b) v7 | **(a)** | discriminador aprobado en R5 para esto; rechazo cerrado en todos los lectores anteriores en ambos casos; ningún lector v6 distribuido; v7 añade una regla de elección sin ganar compatibilidad (diseño §6). (b) solo si se publica `CU3-B1` sin `CU3-B2` y se quiere un rechazo por versión en esos lectores |
| D-B2-4 | Deuda del contador de encarnaciones | (a) resolverla dentro de `CU3-B2` para `F` y `T`: las evaluaciones transitorias de la carga no reservan encarnación; (b) mantenerla como deuda registrada | **(a)**, con la fila B2-13 como prueba | con `T`, la subida por reapertura se multiplica por el número de contactos anclados; la idempotencia de abrir y guardar sin editar vuelve a ser una propiedad comprobable. Es un cambio acotado del libro, sin efecto en tokens ni reclamaciones. Si el autor prefiere no tocar `CU3-B1`, (b) es coherente con el cierre de `CU3-B1` |
| D-B2-5 | Barrera por redefinición del soporte del conductor | (a) sonda en G-B2-1 y pruebas B2-14 y B2-15 para `F` y `T`; (b) solo para `T` | **(a)** | la barrera de linaje es común; demostrarla una vez para ambos tipos de entrada salda `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER` si el núcleo admite una redefinición efectiva. Si no la admite, se registra como límite, sin cambiar el núcleo dentro de `CU3-B2` |
| D-B2-6 | Rango entre tangencias del mismo germen | (a) fase y rango de ADR 0017 con sus barreras y cuarentena; (b) rechazar el ancla cuando dos tangencias comparten germen | **(a)** | es la regla vigente para cualquier selector repetido; (b) dejaría sin token casos simétricos legítimos |
| D-B2-7 | Alcance y verificación | (a) G-B2-1 (solo sondas) primero, después G-B2-2 a G-B2-4 con `INTEGRATED_PHASE`; (b) G-B2-1 y G-B2-2 juntas | **(a)** | la sonda decide el alcance real de la decisión exacta y el comportamiento de los tokens transversales; el código no debe depender de una hipótesis sin caracterizar |

## Compromiso documental del cierre global de `CU3`

Registrado por la instrucción autoral de 2026-10-28 (plan consolidado §2.26 y §6): antes del cierre
global de `CU3`, comprobar que `Point(L, D)` e `Intersect(L, r, D)` están documentados y registrados
en la [matriz de cobertura de la guía](../../geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json)
que usará `CU9`. Hoy la matriz no tiene entradas para ellos (sí para `Point/semantic-address`,
`Point/principal-parameter`, `Intersect/semantic-curve-and-object`, `Intersect/semantic-curve-pair`
e `Intersect/solution-token`). No es una decisión de esta hoja; se cita para que no se pierda.
