# PRE-G9B-CU3 — Evidencia focal final de `CU3-B1` y guía del smoke

| Campo | Valor |
|---|---|
| Carácter | Comprobación focal pedida por la decisión autoral «CU3-B1: candidato técnico favorable, aprobación definitiva pendiente» (2026-10-27). No aprueba nada |
| Candidato | `aabfd0b952838f964d2f2c25b3ebcba643935345` (árbol `c2b061ec0e6345147e561b93dbe0e8ef62898e8d`): descendiente de solo pruebas, inventario y registro de [`dd3a2676`](pre_g9b_cu3_b1_candidate_record.md); el producto es byte a byte el de `dd3a2676` |
| Estado | `CU3-B1 TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`; `CU3-B2` no autorizado; `CU3` global sin `PASS — AUTHOR APPROVED`; nada publicado |

## 1. B1-14 (copia y duplicación) y B1-16 (redefinición)

**B1-14** (`PreG9BCu3FoldContactTest.b1x14DuplicationMintsFreshIdentities`).

- `InternalClipboard.duplicate` del resultado y de su punto materializado crea `R_{1}` y `X_{1}`.
- La copia se ancla en el mismo pliegue con un token nuevo, distinto del original: es el cierre fresco, y ninguna identidad se reutiliza.
- El punto copiado queda definido con su propio token, y el original conserva el suyo.

**B1-16** (`b1x16RedefinitionsTreatAnchoredAndTransverseSelectorsAlike`). Mismo recorrido con una raíz anclada (autoridad constructiva) y con raíces transversales (política heredada):

| Redefinición del objetivo `g` | Ancla de pliegue | Transversal |
|---|---|---|
| inclinación que sigue cortando junto al pliegue | mismo selector y mismo token; el punto sigue definido | igual |
| inclinación sin raíz | token no actual; reclamación latente | igual |
| vuelta a la definición inicial | reactivación del mismo token, sin reasociación | igual |

- Redefinir con una definición idéntica (`g`, `N`, `lx`) tampoco cambia el token en ninguno de los dos casos.
- Cambiar el soporte del conductor a otra circunferencia (`N = Point(c3)`) no lo aplica el núcleo en ninguno de los dos casos: la definición queda como estaba. Es un comportamiento previo, no de `CU3-B1`.
- Conclusión: el selector anclado sigue exactamente las reglas de redefinición del selector transversal (ADR 0017), sin reglas propias.

## 2. Contador de reserva de encarnaciones tras la reapertura

Prueba: `b1x11ReopeningRaisesOnlyTheHighWaterMarkOfIncarnations`.

**Hechos (sonda y prueba).**

- Con un ancla, cada reapertura sube el contador `nextIncarnation` en 2 (2 → 4 → 6). Con raíces transversales (versión 4), con o sin autoridad constructiva, no cambia.
- Las entradas del libro, los tokens y las reclamaciones son idénticos tras cada reapertura.
- Una asignación posterior (el ancla de la costura) recibe un token nuevo que no coincide con ninguno anterior.
- La vuelta al pliegue reactiva el token reclamado.

**Causa.**

- Durante la carga hay dos evaluaciones transitorias que todavía no encuentran la entrada persistida por su contrato.
- Una raíz transversal la recupera con la vinculación heredada de token único, sin reservar encarnación.
- Las entradas ancladas no tienen esa vinculación, por diseño (CU3-B1, §3), así que la evaluación transitoria reserva una encarnación que luego se descarta.

**Efecto.**

- El contador solo garantiza que no se reutilizan encarnaciones; no forma parte del material de ningún token.
- No afecta a la identidad duradera ni a las asignaciones posteriores.
- El estado persistido de un documento con anclas cambia en ese contador tras abrir y guardar sin editar. Si el autor lo considera relevante, una mejora futura podría evitar la reserva transitoria. No se ha cambiado el producto porque no hay defecto respecto de los contratos.

## 3. Las pruebas modificadas no debilitan las protecciones

| Prueba modificada | Qué se mantiene | Qué cambia |
|---|---|---|
| `ConstructiveIntersectionAdmissionTest` (costura) | la aserción heredada de la costura (V-B-2, admisible bajo la política heredada) y el informe de adopción | bajo la autoridad constructiva se exige además el germen de pliegue, `EXISTENCE_EXACT` y `STRICT`: la condición es más estricta, no más débil |
| `ConstructiveExactContactTest`, `ConstructiveEarlyExactDecisionTest` | clase, orden, multiplicidad y configuración del contacto; `NOT_ESTABLISHED` para todo contacto no anclado; el token transversal anterior sigue inadmisible y sin reasociación; exactamente dos soluciones (V-CONF-5) | solo el contacto anclado en un pliegue pasa a `STRICT` |
| `PreG9BCu3SeamReclassificationAuthorModelTest` (escritorio) | los pines históricos `B08_B09`, la clase `TANGENT_ESTABLISHED`, los puntos preservados y la evidencia de `u_1` (`TWO`); `Intersect(o_1, h_2)` sigue sin admitirse | los contactos de `e_1` en sus pliegues pasan a admisibles (inversión explícita `B08_B09` → `CU3-B1`) |

**Protecciones con evidencia directa.**

- **Documentos heredados:**
  - `b1x12` comprueba que no hay anclas y que el libro sigue en versión 4;
  - las suites de `CU0` con la política heredada y sus pines pasan sin cambios.
- **Raíces transversales:**
  - los tokens dorados de `G9U0R4TokenLedgerAllocationTest` no cambian;
  - `b1x16` muestra la paridad de reglas;
  - el corpus del autor es idéntico módulo identificadores fuera de los pliegues.
- **Tangencias regulares (`CU3-B2`):**
  - `b1x18`: `Intersect(o_1, h_2)` sin ancla y sin admitir;
  - en el corpus, `u_1` es idéntico;
  - la aserción de escritorio citada arriba.

## 4. Transición pareja ↔ raíz doble

**Con existencia certificada** (`PreG9BCu3FoldAnchorTransitionTest`).

- En el segmento plegado, la pareja certificada (`x = 1,9999`, `TWO_SIMPLE`, `EXISTENCE_CERTIFIED`) y la raíz doble exacta (`x = 2`, `ONE_DOUBLE`, `EXISTENCE_EXACT`, con la decisión exacta declarada por la fuente, como en la prueba E3) llevan la misma ancla `(t->-t, 0)`.
- Por tanto llevan el mismo germen y el mismo selector, y el libro reutiliza el mismo token.
- En la réplica del autor, las raíces dobles de `e_1·c_1` en `0` y en `π` son de clase 2 y están ancladas (`b1x5and6`).

**Sin existencia establecida** (`b1x2to4`).

- Con un objetivo libre que pasa por el pliegue, la raíz doble es de clase 3: no se ancla nada y el diagnóstico es `IDENTITY_DISCONTINUITY`.
- El token reclamado queda latente, sin crear otro, y vuelve igual cuando reaparece la pareja.
- Con `NONE` no hay solución; el diagnóstico es `FOLD_CONTACT_LOST` cuando hay candidatos en la caja.

**Límite.** En los modelos de prueba, la decisión exacta en una raíz doble solo aparece en contactos estructurales del autor. Un objetivo que se mueve libremente nunca la da: la transición dinámica pareja → doble de clase 2 no se ha observado moviendo un objetivo. Sí está demostrado que ambas formas comparten el selector.

## 5. Verificación

**Pruebas e inventario.**

- `PreG9BCu3FoldContactTest` tiene ahora 13 pruebas (cuatro nuevas), y `PreG9BCu3FoldAnchorTransitionTest` es nueva.
- `pre-g9b-cu3.shared` pasa de 232 a 237 identidades; `discovery.*` y `final.shared` se renovaron con evidencia ejecutada, junto con el pin y el recuento de cobertura.
- No se ha cambiado código productivo.

**Campaña `INTEGRATED_PHASE` de `aabfd0b9`.**

- Es completa, porque no se reutiliza evidencia entre commits.
- Todo salió `ACCEPTED / COMPLETE` en la primera ejecución.
- Se reautenticó (hash, diario `COMPLETED`, commit y árbol exactos) con PowerShell 7.6.9.

| Comprobación | `run_id` | Hojas | `result_hash` |
|---|---|---|---|
| `STATIC` | `verification-204b70f61f254392bbdafc7d1c6e0b9e` | 3/3 | `9720bab728ae88cc90ba6d564e27a1e03a47a3281c13394e17331fd314c743e8` |
| `INFRA_UNIT` | `verification-71180eb1bbe44ff29a1489a5b69b13d3` | 22/22 | `7778fc6d6db595c0420c3ed49e58c6f8f0f921b186da76c880813b760cbad85d` |
| `PHASE` `PRE-G9B-CU3` | `verification-4f4f39f4c5e54aff9c1897533cf22503` | 4/4 | `976fe160dfbb3d887f1d402b19d38d84074e0526dd10880bc904a0c00ccc1315` |
| `PHASE` `PRE-G9B-CU2` | `verification-3cd48c0f159d4e869dca563552d306fb` | 4/4 | `3e2b5028443036c42aee80f3e4515313cf247c53d2ccdd6070c6342983d46147` |
| `PHASE` `PRE-G9B-CU1` | `verification-b693714b720043e5a39dd52672bc1f4d` | 4/4 | `5f9654a3a19e31a752283d11ed2b890fe40dfc6db0a5d254ebe3cc1214267794` |
| `PHASE` `PRE-G9B-CU0` | `verification-f708b23b3e484e84820eabe00d6d3ff4` | 2/2 | `e237870dadbd21818097833f04a8ae8f43dedcb767335ef7df86565eff1e719e` |
| `INTEGRATION` | `verification-bf0133909f704f3091c305b4b31c2ee5` | 19/19 | `4547902025b465efaa9a247dba0330f26bde990a7c40889c4097f94c4510cce9` |

**Rendimiento y corpus.** Los medidos sobre `dd3a2676` siguen siendo válidos, porque el producto es idéntico ([registro del candidato](pre_g9b_cu3_b1_candidate_record.md) §4).

## 6. Guía del smoke manual autoral

Usa una copia de `C:\Temp\InterCilConoOblique-K1-contragiro.cedg` (o del original), en GeoCeDG del candidato.

1. **Adopción.** La autoridad constructiva se ofrece solo al pedir un punto estructural con conductor. Un `Intersect(L, r)` sin conductor se crea con la política del documento, sin preguntar.
   - Escribe en la Entrada `X1 = Intersect(e_1, n, K)` y acepta el diálogo «Autoridad constructiva».
   - Escribe después `S = Intersect(e_1, c_1)`. Debe mostrar la costura y el contacto en `t = 0`, ambos con «Solución anclada en un pliegue».
   - Sin adopción se ve el comportamiento heredado: solo la raíz de la costura es materializable y aparece y desaparece al barrer `α_cone`. Es lo que `CU3-B1` corrige.
2. **Materialización.** Crea un punto con `Intersect(e_1, c_1, <token>)`, copiando el token de la solución en `t = 0`: debe quedar sobre el contacto.
3. **Arrastre.** Mueve `rbCone` y `α_cone` por todo su recorrido. El punto sigue al contacto o queda indefinido, sin saltar nunca a otra raíz, y vuelve al regresar.
4. **Duplicado.** Duplica `S` y el punto: la copia tiene su propio token.
5. **Guardado.** Guarda, cierra y reabre: el punto conserva su definición y su posición.
6. **Deshacer.** Deshacer de la adopción devuelve los resultados heredados sin cambios.
7. **Tangencia regular.** `Intersect(o_1, h_2)` (`CU3-B2`) sigue sin solución admisible para materializar.

Anota cualquier punto que salte de raíz, cualquier pérdida tras reabrir o cualquier diálogo inesperado.
