# PRE-G9B-CU3 — Diseño de `CU3-B2` (G-B2-0): tangencias regulares certificadas

| Campo | Valor |
|---|---|
| Carácter | Diseño normativo y arquitectónico de la puerta G-B2-0, autorizado por la instrucción autoral de 2026-10-28. Diseño previo al código, para revisión autoral; no aprueba nada ni autoriza implementar |
| Autoridad | instrucción autoral de 2026-10-28 (direcciones preferentes 1 a 4); R1–R6 ([hoja de decisiones de la Etapa B](pre_g9b_cu3_b_decision_sheet.md)); [ADR 0039](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md); [ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md); [ADR 0040](../adr/0040-constructive-geometric-authority-and-relation-integrity.md); [especificación de intersecciones](../../geocedg/specs/locus/locus-v2-intersections.md) §6.2, §8.3 y CU3.3 |
| Base | `CU3-A` y `CU3-B1` `PASS — AUTHOR APPROVED` ([cierre de `CU3-B1`](../validation/pre_g9b_cu3_b1_closeout_record.md)); [informe de preparación](pre_g9b_cu3_b2_preparation_brief.md) |
| Documentos de la puerta | este diseño; [hoja de decisiones de `CU3-B2`](../validation/pre_g9b_cu3_b2_decision_sheet.md); [enmiendas normativas candidatas](pre_g9b_cu3_b2_normative_proposal.md); [matriz de pruebas](../validation/pre_g9b_cu3_b2_validation_matrix.md) |
| Estado | G-B2-0 **aprobado con precisiones P1 y P2** por la decisión autoral de 2026-10-28 (D-B2-1(a) a D-B2-7(a)); G-B2-1 entregado ([expediente](../validation/pre_g9b_cu3_b2_g1_characterization_record.md); §12); G-B2-2 no autorizado; `CU3` global no aprobado; `selfApproved=false` |

## 1. Objeto y alcance

`CU3-B2` publicaría, solo bajo `constructive-authority/v1` y en `Intersect(L, r)` sin conductor,
una identidad durable y un token admisible para cada **tangencia regular certificada** de una
componente de `L` con el objetivo `r`.

- **Tangencia regular.** Raíz `t₀` del indicador orientado `h = F_r ∘ L` con `h(t₀) = 0`,
  `h'(t₀) = 0` y `h''(t₀) ≠ 0`, aislada en su caja y **sin** certificado de pliegue (si lo hay, la
  atiende `CU3-B1`).
- **Evidencia de existencia.** Solo las clases 1 (estructura) y 2 (aritmética exacta) de la
  taxonomía de la Etapa B. Un punto crítico con residuo pequeño (clase 3) no es una intersección
  (`OBS-CU0-CU3-TANGENT-EXISTENCE`).
- **Caso obligatorio:** `o_1·h_2` del modelo del autor (`u_1`), hoy `EXISTENCE_EXACT`, orden
  `TWO`, sin token admisible.
- **Fuera de alcance:**
  - los contactos en pliegues (`CU3-B1`);
  - los contactos de orden superior (`h''(t₀) = 0`);
  - los extremos incluidos de un dominio (Etapa B §6.4);
  - Locus V2 × Locus V2 (ADR 0017, decisión 12);
  - los documentos bajo la política heredada, cuyo comportamiento no cambia.

## 2. Precondiciones de materialización

Todas a la vez, en la revisión actual:

1. la componente es una componente certificada por `CU2`, con linaje estable;
2. el objetivo pertenece a una familia con orientación canónica (§6.2 de la especificación):
   recta o segmento, rayo, cónica central, parábola o curva implícita polinómica regular;
3. la existencia de `t₀` es de clase 1 o 2, con `h(t₀) = 0` y `h'(t₀) = 0` decididos sin
   `binary64` ni tolerancias;
4. el signo de `h''(t₀)` está certificado lejos del cero (§3);
5. el certificado de conteo de la caja de `t₀` da exactamente una raíz doble (`ONE_DOUBLE`) y
   ningún otro cero de `h`;
6. no hay certificado de pliegue en `t₀`.

Si falta una, no hay ancla: el contacto sigue siendo evidencia visible sin token admisible
(comportamiento de `CU3-A`).

## 3. Lado del contacto: definición e invariancia (dirección 2)

### 3.1 Definición

El **lado del contacto** es `σ = sign h''(t₀) ∈ {+1, −1}`, donde `h = F_r ∘ L` y `F_r` es el
indicador del objetivo con su **orientación canónica** (§6.2 y §8.3 de la especificación de
intersecciones).

Interpretación geométrica: `σ = +1` si, junto al contacto, la componente queda en el lado positivo
del objetivo, y `σ = −1` si queda en el negativo. Para dos curvas tangentes en el punto, con
curvaturas con signo `κ_L` y `κ_r` medidas respecto de la normal canónica `n` del objetivo:

`h''(t₀) = |∇F_r| · |L'(t₀)|² · (κ_L − κ_r)`,

de modo que `σ` es el **signo certificado de la curvatura relativa** respecto de la orientación
canónica del objetivo.

### 3.2 Invariancias (proposiciones que G-B2-2 debe probar y cubrir con pruebas)

| Transformación | Efecto en `σ` | Razón |
|---|---|---|
| reparametrización regular `t = φ(s)` de `L`, con `φ' ≠ 0`, incluida la inversión de la orientación del conductor | ninguno | en un punto crítico, `(h∘φ)'' = h''·φ'² + h'·φ'' = h''·φ'²`, y `φ'² > 0` |
| cambio de representación equivalente del objetivo por un escalar `c ≠ 0` | ninguno | la orientación canónica normaliza el signo de `c` (§6.2) |
| cambio de indicador equivalente (distancia con signo frente a ecuación implícita) | ninguno | cerca de la curva, `F = g·d` con `g > 0` en el lado canónico; como `h_d(t₀) = h_d'(t₀) = 0`, `h_F'' = g·h_d''` |
| vista, zoom, tamaño del lienzo | ninguno | ningún dato de vista entra en `h` |
| orden de descubrimiento de las raíces, índice de salida, `Solution N` | ninguno | `σ` no depende del orden |
| Deshacer, Rehacer, guardado y reapertura | ninguno | se recalcula en cada revisión desde la geometría; el germen persistido lo compara |

**Consecuencia.** El lado es una propiedad semántica del par (componente, objetivo) en el contacto,
no de la parametrización ni de la representación.

### 3.3 Barreras del lado

`σ` no puede cambiar de forma continua sin pasar por `h''(t₀) = 0`. Por eso son **barreras de
identidad** (ADR 0017, decisión 8), con el token latente y sin reasociación:

| Suceso | Estado |
|---|---|
| `h''` deja de estar certificado lejos del cero (contacto de orden superior o envolvente que contiene el cero) | `IDENTITY_DISCONTINUITY` |
| cambio de la orientación canónica del objetivo por una modificación semántica (inversión de un rayo, cambio de familia, paso de cónica central a parábola) | `IDENTITY_DISCONTINUITY` |
| redefinición efectiva de la componente o del soporte del conductor que cambia el linaje | barrera de linaje (§5.4) |
| aparición de un pliegue certificado en `t₀` | el contacto pasa a `CU3-B1` con su propio germen; el token regular queda latente |

## 4. Germen, selector y rango

- **Germen.** `pre-g9b-cu3/regular-tangent-germ/v1/` + enmarcado(linaje de la componente) +
  enmarcado(familia y orientación canónica del objetivo) + enmarcado(`σ`). Sin coordenadas,
  parámetros, índices, orden de contacto ni clase de evidencia (R2 y P3).
- **Selector.** Linaje de la componente + germen. Dos tangencias regulares de la misma componente y
  el mismo objetivo pueden compartir germen (mismo lado): en ese caso el selector se enriquece con
  la fase y el rango de ADR 0017 (decisiones 6 a 9), con sus barreras y su cuarentena periódica.
  A diferencia del ancla de pliegue de `CU3-B1`, que es una etiqueta exacta, aquí el rango sí es
  necesario.
- **Vinculación de token único.** No se aplica a las entradas de tangencia, como en `CU3-B1`.
- **Admisibilidad.** `Intersect(L, r, token)` es admisible solo si la revisión actual cumple el §2
  y el selector completo se resuelve de forma única.

## 5. Ciclo de vida

### 5.1 Estados

| Situación tras un cambio | Estado | Efecto |
|---|---|---|
| §2 se cumple con el mismo selector | `CURRENT` | el punto se actualiza |
| clase 4 sin intersección (`NONE` certificado en la caja) | `CONTACT_LOST` | punto indefinido; token latente |
| clase 4 con dos raíces transversales certificadas en la caja | `CONTACT_SPLIT` | punto indefinido; token latente (§5.2) |
| clase 3 (indecidible) | `CONTACT_NOT_CERTIFIED` | punto indefinido; la evidencia se muestra; no es `LOST` ni `SPLIT` |
| barrera del §3.3 | `IDENTITY_DISCONTINUITY` | punto indefinido; token latente; sin reasociación |
| §2 vuelve a cumplirse con el mismo selector completo | `CURRENT` | reactivación del mismo token (ADR 0017, decisión 13) |

### 5.2 `CONTACT_SPLIT` (dirección 1)

- El token de la tangencia **no se transfiere** a ninguna de las dos raíces transversales. Queda
  latente si un punto lo reclama; si no, se poda como cualquier token no reclamado (ADR 0017).
- Las dos raíces transversales **conservan o adquieren sus propias identidades** con el germen
  transversal vigente (`g9u0-r4/current-transverse-root-germ/v1/`) y las reglas de ADR 0017. No
  se crea ninguna regla de continuidad entre el contacto y las raíces.
- **Conservar** solo es posible si un token transversal reclamado vuelve a resolverse con su mismo
  selector completo, de forma única y con admisibilidad actual (decisión 13). La fusión y la
  división son barreras (decisión 8): ninguna raíz recibe un token por ocupar el mismo rango.
- **Pendiente de caracterización (G-B2-1).** El comportamiento vigente de los tokens transversales
  de ambos lados de una tangencia regular se fija con una sonda antes de cerrar las filas B2-5 a
  B2-7 de la matriz. El diseño no presupone que se conserven.
- **Vuelta a la tangencia.** Si las dos raíces se reúnen en una tangencia certificada con el mismo
  selector, el token regular latente se reactiva. Las transversales dejan de ser actuales con su
  causa tipada de ADR 0017 y quedan latentes si están reclamadas.
- **Diagnóstico.** `REGULAR_CONTACT_SPLIT` (nombre candidato) en la revisión de la división, con
  los dos tokens transversales actuales y el token regular latente.

### 5.3 Diferencia con el pliegue

En un pliegue, las dos raíces son preimágenes de un **mismo** punto geométrico y conservan una
sola identidad (D-CU3-11, `CU3-B1`). En una tangencia regular son **dos** puntos distintos del
plano; elegir uno para heredar el token sería una elección silenciosa (`AGENTS.md` §11.4) y una
continuidad por proximidad, prohibidas por ADR 0017 (decisión 9).

### 5.4 Redefinición del soporte del conductor y de la componente

- Una redefinición que **cambia el linaje** de la componente (otro soporte del conductor, otro
  conductor, otra fuente) es una barrera: el token queda latente y no se reasocia, aunque la nueva
  componente tenga un contacto en la misma posición.
- Una redefinición que **conserva el linaje** (cambio de parámetros del mismo soporte) no es una
  barrera por sí misma: rige el §5.1.
- `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER`: `N = Point(c3)` no lo aplicó el núcleo. G-B2-1
  caracteriza por qué y busca una redefinición efectiva admitida; las filas B2-14 y B2-15 de la
  matriz la cubren para las entradas de pliegue y de tangencia regular.

## 6. Persistencia: evaluación de v6 frente a v7 (dirección 3)

### 6.1 Hechos del formato vigente

- `LocusIntersectionTokenLedger2D` escribe el estado `versión|nextIncarnation|actual|origenDeCopia`.
- La versión 6 se elige solo si hay al menos una entrada `F`; si no, se escriben la 4 o la 5 byte
  a byte. La importación exige que un estado v6 contenga una entrada `F`.
- Cada entrada v6 lleva un discriminador explícito (`P`, `S` o `F`). Las entradas `S` y `F` tienen
  la misma aridad (11 campos con el discriminador); `P` tiene 10.
- Un lector v6 de `CU3-B1` rechaza cerrado un discriminador desconocido
  («Malformed ledger entry variant»). Un lector v5 o anterior rechaza cerrado la versión 6.
- El estado se reescribe en forma canónica y se rechaza si no lo es.
- **Distribución:** ningún lector v6 se ha distribuido. Los commits de `CU3-B1` no están en
  `origin/phase/pre-g9b-cu3-implementation` (`b1813da1`) ni en `main` (`e6c908f2`).

### 6.2 Comparación

| Criterio | v6 con una entrada `T` | v7 |
|---|---|---|
| Compatibilidad del formato | la entrada `T` reutiliza la aridad de `S`/`F`; el germen va en el campo del selector | formato nuevo con el mismo discriminador; misma aridad |
| Discriminadores | ya existen para esto (R5 los fijó «para que un segundo incremento pueda añadir tangencias regulares sin una versión 7») | duplica la función del discriminador con un número de versión |
| Lectura por un lector anterior | v6 de `CU3-B1`: rechazo cerrado por variante desconocida; v5 o anterior: rechazo cerrado por versión | v6 de `CU3-B1` y anteriores: rechazo cerrado por versión |
| Calidad del diagnóstico del lector anterior | la causa interna dice «malformada», no «más reciente» | la causa interna dice «versión no admitida» |
| Persistencia canónica | la versión se sigue derivando del contenido: v6 si hay `F` o `T`; la regla «v6 exige una entrada `F`» pasa a «v6 exige una entrada de contacto» | dos reglas de elección (v6 con `F`, v7 con `T`) y una tercera para documentos con ambas |
| Documentos `CU3-B1` existentes | se leen igual, byte a byte | se leen igual |
| Riesgo de ambigüedad | ninguno: cada variante tiene un discriminador distinto | ninguno |

### 6.3 Conclusión

**Recomendación: v6 con la entrada `T`.** No es una elección por conveniencia, por cuatro hechos:

- el discriminador de la versión 6 se aprobó precisamente para esto (R5);
- los dos caminos rechazan cerrado en todos los lectores anteriores;
- ningún lector v6 sin `T` se ha distribuido;
- v7 añade una regla de elección de versión más sin ganar compatibilidad.

La única diferencia real es el texto interno del rechazo en un lector v6 de `CU3-B1`. Si el autor
publica `CU3-B1` sin `CU3-B2` y quiere un rechazo por versión en esos lectores, la alternativa es
v7 (hoja de decisiones, D-B2-3).

### 6.4 Reglas comunes

- Una entrada `T` contiene el germen y el linaje persistido; nunca evidencia de la revisión.
- Copia y duplicación: cierre fresco; identidades nuevas.
- Deshacer, Rehacer y reapertura restauran el libro, incluidas las reclamaciones latentes.

## 7. Deudas de `CU3-B1` (dirección 4)

| Id | Efecto en `CU3-B2` | Disposición propuesta |
|---|---|---|
| `DEBT-CU3B1-LEDGER-V6-HIGHWATER-IDEMPOTENCE` | las entradas `T` tampoco tienen vinculación de token único: con este diseño, el contador subiría también por cada tangencia anclada al reabrir | sigue registrada. D-B2-4 decide si se resuelve dentro de `CU3-B2` (sin reservar encarnación en las evaluaciones transitorias de la carga) o se mantiene como deuda; la fila B2-13 mide el efecto en ambos casos |
| `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER` | la barrera de linaje del §5.4 es la misma | sigue registrada; filas B2-14 y B2-15 |

## 8. Degeneraciones

| Caso | Tratamiento |
|---|---|
| contacto de orden superior (`h''(t₀) = 0` decidido) | sin ancla; fuera de alcance |
| envolvente de `h''(t₀)` que contiene el cero | `IDENTITY_DISCONTINUITY` si había un ancla; sin ancla si no |
| tangencia en la costura de una componente periódica | el marco de fase orientado de ADR 0017 (decisión 7); cuarentena si no se resuelve |
| dos tangencias con el mismo germen | rango de ADR 0017 |
| tangencia en un extremo incluido | fuera de alcance |
| agotamiento de un presupuesto | fallo cerrado, nunca una decisión |
| objetivo sin orientación canónica (familia no admitida) | sin ancla |
| tangencia que coincide con un pliegue | `CU3-B1` |

## 9. Compatibilidad

- **Transversales sin contacto:** sin cambios (tokens dorados).
- **Contactos de pliegue de `CU3-B1`:** sin cambios.
- **Contactos estructurales de `CU3-A`:** sin cambios.
- **Documentos heredados y documentos adoptados sin tangencias regulares:** su libro no cambia de
  versión.
- **ADR:** ADR 0017 (rango, barreras, cuarentena, decisión 13); ADR 0039 (decisiones 2 y 5,
  decisión 3 enmendada); ADR 0040 (solo bajo la autoridad constructiva).

## 10. Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| la certificación exacta de tangencias regulares solo llega a contactos estructurales del autor (T7-a), no a objetivos libres | alta | el incremento cubriría solo `u_1` | G-B2-1 caracteriza el alcance antes del código; el diseño no promete más |
| el signo de `h''` no se certifica lejos del cero en casos casi degenerados | media | contactos sin ancla | fallo cerrado; diagnóstico visible |
| comportamiento vigente de los tokens transversales alrededor de la tangencia distinto del supuesto | media | filas B2-5 a B2-7 | sonda en G-B2-1 antes de fijar la matriz |
| el contador de encarnaciones se dispara con muchos contactos | media | reproducibilidad del documento | D-B2-4; fila B2-13 |
| coste de una caja de certificación más por tangencia candidata | media | arrastre más lento | medición E3 y smoke |
| redefinición efectiva del soporte del conductor no admitida por el núcleo | media | la barrera de linaje sigue sin demostrarse | G-B2-1; filas B2-14 y B2-15; si no hay redefinición admitida, se registra como límite |
| confusión de `CONTACT_SPLIT` con una pérdida de puntos por el usuario | baja | experiencia | diagnóstico explícito y guía de usuario (`CU9`) |

## 11. Puertas de implementación

| Puerta | Contenido | Condición de superación | Estado |
|---|---|---|---|
| G-B2-0 | este diseño, la hoja de decisiones, las enmiendas candidatas y la matriz | revisión autoral y decisiones D-B2-1 a D-B2-7 | aprobado con precisiones (2026-10-28) |
| G-B2-1 | sondas: alcance de la decisión exacta en tangencias regulares; tokens transversales alrededor de una tangencia; redefinición efectiva del soporte del conductor; reservas transitorias; fuentes singulares | informe de sondas sin contradicción con este diseño; matriz cerrada | entregado (§12) |
| G-B2-2 | código: germen, `σ`, entrada `T`, estados del §5, diagnósticos | pruebas focales B2-1 a B2-20 | no autorizado |
| G-B2-3 | verificación `INTEGRATED_PHASE` (`STATIC`, `INFRA_UNIT`, `PHASE` de `CU3` a `CU0`, `INTEGRATION`), rendimiento y corpus | `ACCEPTED / COMPLETE` | no autorizado |
| G-B2-4 | smoke autoral con guía focal | `AUTHOR SMOKE — PASS` y decisión autoral | no autorizado |

Las enmiendas normativas se aplican a las especificaciones y los ADR solo tras su aprobación, al
inicio de G-B2-2.

## 12. Resultados de G-B2-1 y precisiones autorales P1 y P2 (2026-10-28)

Fuente: [expediente G-B2-1](../validation/pre_g9b_cu3_b2_g1_characterization_record.md) (conclusiones
K1 a K7). Esta sección prevalece sobre las anteriores donde las precisa.

### 12.1 Precondiciones ampliadas (P1)

Además del §2, la materialización exige, todo certificado en la caja de `t₀`:

- **validez de la fuente** en la caja y pertenencia de `t₀` a su dominio (sin extremos incluidos);
- **regularidad de la fuente**: el encierro de `L'` excluye el vector cero (CU3.2 de la
  especificación de intersecciones); una raíz doble del residuo en una fuente singular **no** es una
  tangencia regular;
- **regularidad del objetivo**: `∇F_r ≠ 0` en el punto de contacto (se excluyen los puntos singulares
  de cónicas e implícitas) y, para un objetivo limitado (segmento, rayo, arco), pertenencia del punto
  a su parte limitada, decidida sin tolerancias;
- **aislamiento**: el certificado de conteo da `ONE_DOUBLE` y ningún otro cero de `h`;
- **continuidad suficiente**: `L` y `F_r` de clase C² en la caja, para que `h''` exista y su signo sea
  el de la curvatura relativa (`h'' = |∇F_r|·|L'|²·(κ_L − κ_r)` con `L' ≠ 0` y `∇F_r ≠ 0`).

Si alguna falta, no hay ancla. K7: ninguna fuente singular se clasifica hoy con orden `TWO`.

### 12.2 Estados precisados

- `CONTACT_LOST` exige un `NONE` **certificado** en la caja. Bajo la autoridad constructiva, la
  separación sin corte da hoy `UNRESOLVED` con `CONSTRUCTIVE_ADMISSION_NOT_ESTABLISHED` (K2): ese caso
  es `CONTACT_NOT_CERTIFIED`. El efecto en el punto es el mismo (indefinido, token latente); cambia el
  diagnóstico.
- `CONTACT_SPLIT` coincide con la revisión en que el motor declara la continuación ambigua (K4). Las
  raíces transversales reaparecen con sus propios tokens reclamados en la siguiente revisión no
  ambigua, por ADR 0017 decisión 13, sin ninguna regla nueva.

### 12.3 Alcance real (K1)

Con el producto actual, la clase 2 de una tangencia regular solo se alcanza en `u_1` (vía estructural
de T7-a). `CU3-B2` no promete tokens admisibles para tangencias regulares con objetivos libres; esos
contactos siguen como evidencia de clase 3 sin token admisible.

### 12.4 Redefinición del soporte del conductor (K5)

El núcleo rechaza en silencio toda redefinición que cambie el soporte o el conductor de un Locus V2
existente, y las redefiniciones admitidas actúan sobre el mismo objeto soporte, que conserva su
identidad persistente. La barrera de linaje por redefinición no es alcanzable. El §5.4 se mantiene
como regla para cuando exista ese camino (`CU7`); las pruebas cubren los rechazos y las redefiniciones
en sitio para entradas `F` y `T`.

### 12.5 Persistencia (P2 y K6)

- Las reservas transitorias se deben a evaluaciones de la carga con la política heredada, antes de
  restaurar el registro de autoridad del documento. La corrección actúa sobre esa causa (vía 1:
  restaurar antes la autoridad; vía 2: no confirmar en el libro durante la carga), no sobre el
  contador.
- Debe preservar unicidad, determinismo, reclamaciones, latencia, duplicación, Deshacer/Rehacer y
  reapertura, sin redefinir el contrato de identidad durable. Si aparece una incompatibilidad
  normativa, se prepara una decisión autoral.
- Condición de D-B2-3 vigente: ningún lector v6 distribuido.
