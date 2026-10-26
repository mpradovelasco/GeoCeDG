# PRE-G9B-CU3 — Revisión arquitectónica focal de `CU3-B`: identidad de los contactos sin conductor

| Campo | Valor |
|---|---|
| Carácter | Revisión arquitectónica candidata que autoriza la disposición autoral de 2026-10-26, punto 2. No cambia código, tokens, selectores, gérmenes ni identidad, y no adelanta la aprobación de ninguna regla de identidad nueva: todo lo propuesto es candidato y queda para una decisión autoral posterior |
| Autoridad | [disposición autoral](../validation/pre_g9b_cu3_author_disposition_record.md) §2; [ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md); [ADR 0039](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md), decisiones 2, 3 y 5; [ADR 0040](../adr/0040-constructive-geometric-authority-and-relation-integrity.md); [diseño de la Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §6; [diseño de E3](pre_g9b_cu3_e3_implementation_design.md) §4 |
| Base | candidato técnico `3c2ca7e8` y su descendiente documental publicado `735c55c6` |
| Compatibilidad exigida | ADR 0017, ADR 0039, ADR 0040, persistencia, DAG y documentos anteriores |
| `selfApproved` | `false` |

## 1. Objeto

`CU3-B` es la materialización **sin conductor** de los contactos de clase 1 y 2: un punto público
`Intersect(L, r)` que designe un contacto sin pasar por `Intersect(L, r, D)`. El candidato `CU3-A`
ya los clasifica y los muestra en el resultado rico, sin token admisible. Esta revisión estudia los
tres problemas que impidieron implementarlo (diseño de E3 §4) y propone una solución candidata
compatible con los contratos vigentes:

1. identidad única de un contacto en pliegue con dos preimágenes, sin fusionar ni reasociar tokens
   transversales existentes;
2. distinción intrínseca de los dos contactos estructurales de una misma componente, incluida la
   costura periódica;
3. separación entre identidad durable y evidencia de la revisión, sin clase de evidencia ni orden de
   contacto en ningún material que determine identidad.

## 2. Hechos de partida

**Raíces transversales** (código del candidato):

- **Germen.** El germen transversal es
  `g9u0-r4/current-transverse-root-germ/v1/` + la clave de identidad de la componente + la
  orientación del cruce, `POSITIVE` o `NEGATIVE` (`PublicTargetIntersectionCapability2D`). Es
  evidencia de la revisión.
- **Selector y token.** El selector R4 añade el tipo de dominio, la cardinalidad del grupo y, solo
  cuando el selector base se repite, el rango de fase (ADR 0017, decisión 4). El libro de tokens
  liga el selector con los contratos de proveedor y objetivo. El token `locus-root/v3` es opaco.
- **Persistencia.** El estado del libro se persiste en las versiones 4 y 5, y se importan las
  versiones 1 a 5. Una versión desconocida lanza una excepción y aborta la carga.
- **Selectores relegidos.** El selector se vuelve a leer en cada instantánea, así que su texto debe
  reconstruirse exactamente.

**Contactos** (E3):

- `Intersect(e_1, n)`, `Intersect(e_1, c_1)`, `Intersect(e_1, r)` y `u_1 = Intersect(o_1, h_2)`
  publican sus contactos de clase 2 como `TANGENT_ESTABLISHED`, `EXISTENCE_EXACT`, con orden
  `FOLD_CONTACT` o `TWO`.
- No tienen germen y, por tanto, no tienen token admisible.
- La raíz transversal de la costura que publicaba la política heredada pierde su admisibilidad tras
  la adopción. No se reasocia, y Deshacer la restaura.

**Pliegues de `CU2`:**

- Cada pliegue certificado es un `LocusCertifiedFold2D`, con el encierro canónico de `t₀`, la banda
  de aislamiento, el nombre de la simetría verificada y la naturaleza de la prueba.
- La simetría es una de las dos involuciones de `fold-symmetry/v1` (`ConstructionSymmetry2D`):
  `t ↦ −t`, cuyos puntos fijos son `0` y `π`, y `t ↦ π − t`, cuyos puntos fijos son `π/2` y `−π/2`.
- Por tanto, todo pliegue certificado de la clase de `CU2` está en un parámetro exacto `kπ/2` y
  lleva una etiqueta discreta exacta: su involución y su punto fijo.

**Contratos:**

- ADR 0039, decisión 2: un pliegue certificado da `FOLD_CONTACT`, y sus dos preimágenes perturbadas
  son un único punto con una sola identidad (D-CU3-11).
- ADR 0039, decisión 5: los grados de evidencia son evidencia de la revisión y nunca forman parte de
  un token.
- El germen tangente aprobado en la Etapa B §6.1 contiene, sin embargo, la clase de evidencia y el
  orden. Esa es la tensión del problema 3.
- ADR 0017:
  - ninguna identidad por orden extrínseco, coordenadas, proximidad ni historia;
  - el rango de fase solo desempata selectores base repetidos, con intervalos aislantes disjuntos;
  - en la costura no se asigna por heurística (decisión 7);
  - los cambios de cardinalidad, topología o costura son barreras de identidad (decisión 8);
  - un token reclamado puede quedar latente y reactivarse solo con el mismo selector completo
    (decisión 13).
- ADR 0040: una adopción que contradice un resultado le quita la admisibilidad con causa tipada y
  nunca lo reasocia; ningún token ni selector cambia.

## 3. Problema 1: un contacto en pliegue con dos preimágenes

**Análisis.**

- En un pliegue con simetría exacta, `F(σ(t)) = F(t)`. Si el objetivo cruza la imagen junto al
  pliegue, las dos raíces `t₀ − s` y `t₀ + s` son **el mismo punto geométrico**.
- Como `h(σ(t)) = h(t)`, la derivada `h′` cambia de signo entre ellas. Los dos cruces tienen
  orientaciones opuestas y, por tanto, selectores base distintos.
- Hoy el motor publicaría el mismo punto dos veces, con dos tokens. Cuando el objetivo pasa por
  `F(t₀)`, las dos preimágenes se juntan en el contacto `t₀`. Cuando sale de la imagen, el punto
  desaparece.

**Propuesta candidata P1: identidad anclada en el pliegue.**

1. **Una sola solución.** Bajo `constructive-authority/v1`, cuando un pliegue certificado de `CU2`
   y un certificado de conteo sobre una caja simétrica por `σ` establecen una de estas dos
   situaciones, la consulta publica una sola solución, el **contacto anclado en el pliegue**:
   - la raíz doble exacta en `t₀` (forma `ONE_DOUBLE` de E3);
   - o exactamente dos raíces simples emparejadas por `σ`.

   Su parámetro semántico es el representante canónico de la pareja, y su caja aislante es la caja
   simétrica. El estado («contacto» o «pareja») es evidencia de la revisión, no identidad.
2. **Una sola identidad a través de la transición.** El paso de la pareja al contacto y del contacto
   a la pareja conserva el selector y el token, porque el ancla no cambia. Si no queda ninguna raíz
   en la banda (forma `NONE` certificada), el punto queda indefinido y el token latente
   (`CONTACT_LOST`). Si las raíces vuelven en el mismo ancla, el mismo selector reactiva el mismo
   token (ADR 0017, decisión 13).
3. **Sin fusión ni reasociación.** Las raíces de una caja de pareja certificada nunca reciben
   gérmenes transversales bajo la política constructiva. Los tokens transversales que ya existían
   (acuñados con la política heredada o en una revisión sin la pareja certificada):
   - no se fusionan ni se redirigen, y su material no cambia;
   - dejan de ser actuales con una causa tipada, propuesta: `SUPERSEDED_BY_CERTIFIED_FOLD_PAIR`;
   - si tienen un punto materializado, el punto queda indefinido y su reclamación latente;
   - solo se reactivan si el mismo selector transversal completo vuelve a ser actual, por ejemplo
     al deshacer la adopción.

   El token anclado se asigna nuevo. Nunca hereda por la regla de enlace del único token de una
   componente: `allowLegacySingletonBinding` queda desactivado para los contactos.
4. **Fallo cerrado.** Sin certificado de pliegue, o con el emparejamiento ambiguo, no hay ancla: las
   raíces siguen las reglas transversales actuales o quedan sin identidad
   (`IDENTITY_DISCONTINUITY`), y nunca se elige un lado en silencio.

**Alternativas.**

- (a) Mantener dos identidades transversales y añadir una relación de «mismo punto». Se rechaza,
  porque contradice D-CU3-11.
- (b) Fusionar los dos tokens existentes en uno. Se rechaza: es una reasociación.
- (c) P1, recomendada.

## 4. Problema 2: dos contactos estructurales de una misma componente

**Análisis.**

- Los dos contactos de `e_1·c_1` (en `t₀ = 0` y en la costura `t₀ = π`) tienen el mismo linaje, el
  mismo objetivo, el mismo orden, la misma clase y el mismo lado (`h″ < 0`).
- Con el germen aprobado, sus selectores base coinciden.
- El rango de fase de ADR 0017 no puede desempatarlos en una componente periódica sin asignar la
  costura por heurística (decisión 7), así que los dos fallarían cerrados o quedarían en cuarentena.

**Propuesta candidata P2: la etiqueta exacta del pliegue como dimensión intrínseca.**

1. **Qué es el ancla.**
   - El ancla de un contacto en pliegue es la pareja (involución, punto fijo) del pliegue
     certificado, en el conjunto finito {`t ↦ −t` en `0`, `t ↦ −t` en `π`, `t ↦ π − t` en `π/2`,
     `t ↦ π − t` en `−π/2`}.
   - No es un valor binary64 de parámetro, ni un intervalo, ni un ordinal: es el punto fijo exacto
     de una simetría estructural del programa, verificada por `fold-symmetry/v1`.
   - Por eso no entra en la prohibición de ADR 0017, decisión 3, que excluye el valor bruto del
     parámetro y los bits del intervalo.
2. **Invariancia.**
   - El punto fijo de una involución no se mueve con el movimiento regular. La etiqueta se conserva
     mientras el pliegue siga certificado.
   - `π` y `−π` son un mismo parámetro canónico, así que la etiqueta de la costura es la misma a los
     dos lados.
   - Una redefinición del conductor, de su marco o de la componente es una barrera de identidad
     (ADR 0017, decisión 8).
3. **Sin rango.**
   - Dos contactos en pliegue de una misma componente y un mismo objetivo tienen etiquetas
     distintas por construcción, así que su selector nunca se repite y no necesitan rango de fase.
   - Los contactos tangentes regulares, sin pliegue, siguen las reglas de ADR 0017 tal como están,
     con el lado del contacto como tipo de germen.
   - Si dos de ellos comparten selector base, se desempatan por rango, con la cuarentena de la
     costura sin cambios.

**Alternativas.**

- (a) Ampliar el rango de fase con una regla para el punto fijo de un pliegue en la costura. Se
  rechaza: es una asignación heurística en la costura.
- (b) Usar como ancla los puntos estructurales `K` y `J`. Se rechaza: no son intrínsecos a una
  consulta sin conductor, porque dependen de puntos que el usuario haya creado.
- (c) P2, recomendada.

## 5. Problema 3: identidad durable frente a evidencia de la revisión

**Propuesta candidata P3: un germen solo con hechos estructurales.**

| Material del germen | Contacto anclado en un pliegue | Contacto tangente regular |
|---|---|---|
| tipo de germen versionado | sí | sí |
| clave de identidad de la componente | sí | sí |
| objetivo (por el contrato de objetivo del libro) | sí | sí |
| etiqueta del pliegue (involución y punto fijo) | sí | — |
| lado del contacto (signo de `h″` respecto de la orientación canónica del objetivo) | — | sí |
| clase de evidencia, orden de contacto, grado, integridad, certificado, celda, intervalos, valores de parámetro, configuración de decisión | **nunca** | **nunca** |

- **El tipo de germen no es el orden de contacto.** Que un contacto esté anclado en un pliegue lo
  decide la estructura del locus fuente, el pliegue certificado de su componente. Es independiente
  del objetivo y de la evidencia del contacto, y tiene el mismo estatus que la clave de linaje de la
  componente. El orden (`TWO` o `FOLD_CONTACT`) se decide con los jets del residuo con el objetivo
  y es evidencia de la revisión. En los casos obligatorios coinciden, pero no son el mismo hecho.
- **El lado** es el análogo de la orientación del cruce del germen transversal: un tipo geométrico
  del contacto regular. Si cambia de signo, el contacto degenera y hay barrera
  (`IDENTITY_DISCONTINUITY`).
- **Evidencia de la revisión.** La clase de evidencia (1 o 2), el orden, el grado y la integridad
  viven en la evidencia de la revisión (`IntersectionExistenceEvidence2D`) y en el resultado rico.
  La materialización exige en cada revisión que la solución sea actual y de clase 1 o 2 (opción A).
  Un paso de clase 1 a clase 2 conserva el token. Las clases 3, 4 y 5 dejan el token latente, sin
  crear otro.

Con P3, la Etapa B §6.1 y ADR 0039, decisión 3, deberían enmendarse para retirar la clase de
evidencia y el orden del germen, en coherencia con ADR 0039, decisión 5, y V-EVID-3.

## 6. Compatibilidad

- **ADR 0017.** Los gérmenes, selectores, prefijos, marcos y tokens transversales no cambian; la
  prueba de tokens dorados lo fija. El contacto anclado es un tipo nuevo de selector sin rango. Los
  contactos regulares usan el rango sin cambios. Las barreras, la latencia y la cuarentena
  periódica se aplican igual.
- **ADR 0039.** Se cumplen la identidad única de la pareja (decisión 2) y la exclusión de los grados
  de los tokens (decisión 5). La decisión 3 cambia de composición: es la enmienda que se pide.
- **ADR 0040.** Todo ocurre solo bajo `constructive-authority/v1`. La adopción no cambia ningún token
  ni selector, y solo deja de ser actuales, con causa tipada, los tokens transversales de las parejas
  certificadas. El informe de impacto de la adopción los nombra, y Deshacer los restaura.
- **Persistencia.**
  - Los gérmenes y selectores nuevos tienen nombres versionados propios. Se proponen
    `pre-g9b-cu3/fold-contact-germ/v1` y `pre-g9b-cu3/regular-tangent-germ/v1`, distintos del
    `…/tangent-contact-germ/v1` aprobado y nunca implementado.
  - El libro de tokens escribiría una versión 6 de su estado solo cuando contenga una entrada de
    contacto, así que los documentos sin contactos siguen en las versiones 4 y 5.
  - Un lector anterior rechaza la versión 6 y aborta la carga, como ya hace con el registro de
    política de un documento adoptado.
  - El selector de contacto debe reconstruirse exactamente en cada instantánea.
- **DAG.**
  - Un punto dependiente `Intersect(L, r, token)` usa el mecanismo actual.
  - La admisibilidad del contacto se evalúa en cada revisión.
  - Un punto no actual queda indefinido con causa tipada. Esto corrige la laguna actual de
    `AlgoLocusIntersectionPointV2`, que hoy lo deja indefinido sin causa.
  - Deshacer, Rehacer, la copia y la reapertura conservan las reclamaciones como hoy.
- **Documentos anteriores.** Los que no han adoptado la política no cambian. Ni su resultado ni su
  libro de tokens contienen contactos.

## 7. Validación futura, si se autoriza

- **Filas.** V-B-7 a V-B-12 revisadas con P1 a P3.
- **Pruebas adversarias:**
  - tokens transversales dorados sin cambios;
  - un contacto nunca hereda el único token de su componente;
  - la pareja y el contacto conservan una sola identidad;
  - el contacto de la costura y el de `t = 0` son distintos sin rango;
  - un paso de clase 1 a clase 2 conserva el token;
  - un lector anterior falla cerrado ante la versión 6;
  - Deshacer la adopción restaura los tokens transversales superados;
  - la pérdida del certificado de pliegue es una barrera sin reasociación.
- **Clase de verificación.** La que decida el autor; los cambios de identidad son de alto riesgo.

## 8. Decisiones que se piden al autor

| Decisión | Pregunta | Recomendación |
|---|---|---|
| CU3-B-R1 | identidad única de la pareja de un pliegue | P1, contacto anclado en el pliegue |
| CU3-B-R2 | distinción intrínseca de dos contactos de una componente | P2, etiqueta exacta del pliegue, sin rango |
| CU3-B-R3 | composición del germen | P3, sin clase de evidencia ni orden; enmiendas de la Etapa B §6.1 y de ADR 0039, decisión 3 |
| CU3-B-R4 | tokens transversales de una pareja certificada | dejan de ser actuales con causa tipada y quedan latentes, sin fusión ni reasociación |
| CU3-B-R5 | persistencia | versión 6 del libro de tokens, solo con contactos, y fallo cerrado de los lectores anteriores |
| CU3-B-R6 | alcance del incremento | contactos en pliegue y tangencias regulares juntos, o solo los primeros |

Ninguna de estas decisiones está tomada. Ningún código, token, selector, germen ni regla de
identidad cambia hasta una decisión autoral posterior.
