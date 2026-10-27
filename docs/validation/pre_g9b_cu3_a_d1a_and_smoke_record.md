# PRE-G9B-CU3 — D1-A, smoke autoral de `CU3-A` y candidato definitivo

| Campo | Valor |
|---|---|
| Carácter | Registro técnico. No es una aprobación: `CU3-A` y `CU3` no están `PASS — AUTHOR APPROVED` |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Decisión autoral D1-A y resultado del smoke de CU3-A», de 2026-10-27 |
| Referencia anterior | `9f6f27f265725b27673f5c37880947ad6c4f2399`, referencia geométrica y de rendimiento previa a D1-A |
| Candidato definitivo | `f61f295447d75755e4c932a0f8f41533563550dd` (árbol `875e1cbf5dca2b6d0bbea8b72d0c24ec3aa306c0`), rama local `phase/pre-g9b-cu3-implementation`, sin publicar |
| `selfApproved` | `false` |

## 1. Decisiones y resultado del smoke

> DECISIÓN AUTORAL: D1-A APROBADA — SHARED BUDGET.
> SMOKE AUTORAL: FUNCIONALMENTE SATISFACTORIO, CON DOS OBSERVACIONES PENDIENTES DE INVESTIGACIÓN.

- **Smoke de `9f6f27f2`:** `AUTHOR SMOKE — FUNCTIONALLY SATISFACTORY WITH TWO OPEN OBSERVATIONS`.
  - Comprobado por el autor: los contactos y la dinámica general del modelo, sin otros defectos
    funcionales ni objeciones de rendimiento.
  - Observaciones abiertas: `OBS-CU3A-SMOKE-01` (§3) y `OBS-CU3A-SMOKE-02` (§4).

## 2. D1-A: presupuesto compartido por comprobación

**Normativa.** `locus-v2-constructive-authority.md` pasa a la versión 1.1 (§10). Una comprobación
de un certificado en una revisión es una sola unidad de `constructive-identity-certificate-budget/v1`:

`W = W_memoria + Σ W_preparación,i + W_respaldo ≤ 16 000 000` productos de términos.

- Los demás límites no cambian: 16 indeterminadas, 200 000 términos y 4 096 bits.
- Ninguna estrategia interna reinicia el presupuesto.
- Un agotamiento hace fallar cerrada la comprobación y nunca decide.
- Una preparación interrumpida por el presupuesto no se guarda.
- Los intentos no cambian.

**Cambio en el producto** (commit `17766fca`):

- `ConstructiveIdentityCertifier.recheckCell` crea un único juego de contadores y lo pasa a la
  memoria y al respaldo. Una sobrecarga recibe los contadores, para las pruebas.
- `ConstructiveIdentityRecheckMemory` carga la preparación de cada margen a los contadores de la
  comprobación que la desencadena. Si el presupuesto la interrumpe, propaga el agotamiento y no la
  guarda.

**Pruebas** (`ConstructiveIdentityRecheckMemoryTest`, tres casos nuevos sobre la réplica del modelo
del autor):

| Prueba | Qué demuestra |
|---|---|
| `oneRecheckSharesOneBudgetAcrossItsStrategies` | `o_1·h_2`, con dos salidas comprobadas: la primera comprobación cuenta la memoria y las dos preparaciones (624 679 productos) y la siguiente solo la memoria (88); contadores deterministas; con el agregado exacto como presupuesto, éxito; con un producto menos, fallo cerrado aunque cada estrategia por separado cabría (el agotamiento nuevo que la decisión acepta); las preparaciones completas se conservan y la interrumpida se repite con el presupuesto de la comprobación siguiente |
| `theFallbackSharesTheBudgetOfItsRecheck` | sin memoria, el respaldo cabe justo en su consumo y falla cerrado con un producto menos |
| `anExhaustedBudgetNeverDecides` | con un presupuesto agotado no hay decisión exacta, y por tanto ni contacto exacto ni `STRICT` |

Las 223 pruebas del paquete constructivo y de las intersecciones de `CU3` pasan, y `checkstyle`
está limpio.

**Caracterización de los agotamientos nuevos.**

- **En el corpus,** con el presupuesto aprobado, ninguno: el agregado máximo en los cinco contactos
  obligatorios es de 1 282 953 productos, el 8 % ([registro de estabilización](pre_g9b_cu3_a_stabilization_record.md)
  §3.2). Los resultados publicados del modelo no cambian (§5).
- **Fuera del corpus,** una comprobación cuyo agregado supere el presupuesto, aunque cada estrategia
  quepa por separado, falla cerrada. La decisión pasa entonces a un intento nuevo, con su propio
  presupuesto. La prueba con un producto por debajo del agregado lo muestra, y no se oculta.

## 3. OBS-CU3A-SMOKE-01: `e_2` al variar `rbCone`

**Reproducción controlada** sobre `9f6f27f2`, con una sonda externa en el escritorio
(`PreG9BCu3SmokeObservationProbe`, en el directorio temporal) y copias de
`InterCilConoOblique.cedg` con `e_2`:

| Estado | Barrido | Resultado |
|---|---|---|
| copia limpia, política heredada | `rbCone` de 7,00 a 7,10 en pasos de 0,01, de vuelta a 7,00, y 7,1 y 5 directos | `e_2` definido en todos los valores, con definición semántica, estado `VALID` y 360 de 360 muestras válidas; ningún ancestro indefinido |
| copia limpia adoptada | igual | igual |
| copia limpia, política heredada | todo el intervalo del deslizador, de 0,5 a 10 en pasos de 0,1 | definido en todo el intervalo |
| estado del smoke: adoptada, con `X1`–`X5`, `R1` y `R2` | de 0,5 a 10 | definido en todo el intervalo |
| tras un arrastre de `H` y otro de `B` | de 0,5 a 10 | definido en todo el intervalo |
| `E_1` en doce posiciones de su circunferencia | `rbCone` 7,0 → 7,1 → 7,0 | definido en las 36 combinaciones |

- **Deslizador.** `rbCone` tiene el intervalo de 0,5 a 10 y paso de animación 0,1, así que 7,1 está
  dentro.
- **Definición de «definido».** `GeoLocusV2.isDefined()` es cierto con una definición `VALID` o
  `EMPTY_DOMAIN`, y la vista gráfica solo la dibuja con `VALID`.

**Conclusión.**

- **Clasificación:** `NO ESTABLECIDO`. No se reproduce sobre una copia controlada en ningún estado
  probado, en ninguna de las dos políticas.
- **Causa:** no demostrada. No hay ninguna precondición fallida ni ningún objeto del grafo
  indefinido que identificar.
- **Sin efecto de este trabajo.** `D1-A`, T7-a, T6 y T1 a T3 no intervienen en la publicación de un
  Locus V2 ni en su existencia certificada de `CU2`. No hay evidencia de regresión ni de defecto
  preexistente.
- **Alcance sobre `CU3-A`:** ninguno demostrado. No se cambia el producto.
- **Lo que haría falta para avanzar.** Que el autor aporte:
  - la copia del documento en el que lo observó;
  - cómo vio la indefinición: el texto de la vista algebraica, el dibujo que desaparece o el
    inspector;
  - y, si puede, la secuencia previa de acciones.

  Dos explicaciones posibles siguen abiertas y no se pueden distinguir sin esos datos: un estado del
  documento distinto de los probados, o una manifestación solo de la representación (por ejemplo, un
  dibujo vacío con la entidad definida).

## 4. OBS-CU3A-SMOKE-02: el diálogo «Autoridad constructiva»

**Contrato y mecanismo.**

- En un documento bajo `legacy-representation-authority/v1`,
  `LocusV2PublicOperations.createDriverCorrespondencePoint` rechaza `Intersect(L, r, D)` con
  `CONSTRUCTIVE_AUTHORITY_REQUIRED`, salvo durante la carga de un archivo.
- La entrada algebraica de escritorio (`GeoCeDGAlgebraInputSubmission`) retiene ese rechazo y
  ofrece la adopción.
- En un documento bajo `constructive-authority/v1` no hay rechazo, así que tampoco hay oferta.
- No existe ningún otro camino que cree el punto en un documento heredado.

**Comprobación sobre una copia limpia del modelo del autor** con el producto de `9f6f27f2`:

| Paso | Resultado |
|---|---|
| Política inicial | `legacy-representation-authority/v1` |
| `X1 = Intersect(e_1, n, K)` y Cancelar | 1 oferta; `X1` no se crea; la política no cambia; error `CONSTRUCTIVE_AUTHORITY_REQUIRED` |
| Repetir y Adoptar | 2 ofertas en total; `X1` definido; política `constructive-authority/v1`, sin errores |
| Deshacer | `X1` y la adopción se retiran en un único paso, sin más pasos que deshacer |
| Rehacer | vuelven `X1` y la adopción |
| Guardar y reabrir | la política adoptada y `X1` se conservan |
| Reabrir una copia no adoptada | política heredada; `X1` vuelve a ofrecer la adopción, sin adopción implícita |

**Conclusión.**

- **Clasificación:** comportamiento conforme; no hay regresión. El producto ofrece la adopción
  exactamente como en la versión anterior y como exigen las pruebas `PHASE` de escritorio.
- **Causa.** La única causa compatible con el mecanismo es el **caso A**: el documento del autor ya
  estaba bajo `constructive-authority/v1`, por ejemplo la copia guardada tras la adopción en la
  primera prueba manual (paso 14 de la guía).
- **Verificación sobre el archivo del autor.** No he podido inspeccionarlo, pero el autor puede
  comprobarlo: el registro `<authorityPolicy` aparece en `geogebra.xml` dentro del `.cedg` solo en
  un documento adoptado.
- **Producto:** sin cambios.

## 5. Comparación geométrica con `9f6f27f2`

- **Método.** La sonda versionada del corpus (`PreG9BCu3T7aCorpusProbe`) se ejecutó sobre el candidato tal como se construye, sin sombra, en las mismas 19 configuraciones del modelo del autor, y se comparó con los resultados publicados de `9f6f27f2`.
- **Resultado.** De los 140 objetos, 78 son idénticos byte a byte y 62 solo difieren en las identidades `geo:<hash>` que genera cada carga de la copia; no hay ninguna diferencia. Los cinco contactos obligatorios, las incidencias `STRICT`, los pliegues, la tangencia regular `TWO` (también en `rbCone = 7,5`), la costura, la admisibilidad y los grados son los de `9f6f27f2`.
- **Salidas.** Están en `artifacts/benchmarks/pre-g9b-cu/cu3-d1a-candidate-f61f2954/`, sin seguimiento.

## 6. Verificación `INTEGRATED_PHASE` del candidato `f61f2954`

Ejecuté una sola campaña, sobre el árbol limpio del candidato congelado. Todos los resultados se volvieron a autenticar: hash, diario `COMPLETED`, identidad del candidato y del árbol, y firma de entorno `de567676…`. Los diagnósticos son los de todas las ejecuciones (gobierno `FINDING`, consistencia histórica `UNAVAILABLE`).

| Perfil | Ejecución | Resultado |
|---|---|---|
| `STATIC` | `verification-80b6ae338d504831906be53ae008b1fb` | `ACCEPTED / COMPLETE` 3/3 |
| `INFRA_UNIT` (PowerShell 7.6.6 aislado) | `verification-b373cdfdf58b4fe1b38b475486afdefd` | `ACCEPTED / COMPLETE` 22/22 |
| `PHASE` `PRE-G9B-CU3` | `verification-e1bf5c61e39c45a382b7b756ff44223d` | `ACCEPTED / COMPLETE` |
| `PHASE` `PRE-G9B-CU2` | `verification-99063f59d1084745ac46f5d93b337911` | `ACCEPTED / COMPLETE` |
| `PHASE` `PRE-G9B-CU1` | `verification-1a4dad5532a247079ed77d8f0891b1d1` | `ACCEPTED / COMPLETE` |
| `PHASE` `PRE-G9B-CU0` | `verification-6cf174d0b6e64b57a6f50213f64d4cf0` | `ACCEPTED / COMPLETE` |
| `INTEGRATION` | `verification-ef34ade20c5d44a79a93926dce2c2ee5` | `ACCEPTED / COMPLETE` 19/19 |

El inventario (`f61f2954`) recoge las tres pruebas nuevas: `pre-g9b-cu3.shared` tiene 223 identidades y `final.shared` 7 480, ejecutadas.

## 7. Rendimiento

Tres ejecuciones del producto, sin sombra, en la misma estación que `9f6f27f2`. Valores en ms: mediana / percentil 95.

| Ejecución | E3 `H` | E3 `B` | Smoke `H` | Smoke `B` | Primer arrastre del smoke |
|---|---|---|---|---|---|
| A | 49,2 / 63,6 | 43,3 / 55,9 | 76,0 / 108,2 | 71,0 / 93,0 | 1,63 s |
| B | 43,9 / 51,8 | 45,5 / 59,8 | 65,7 / 85,8 | 68,4 / 80,8 | 1,67 s |
| C | 43,7 / 60,2 | 42,6 / 46,9 | 69,9 / 83,7 | 67,4 / 74,7 | 1,51 s |
| `9f6f27f2` (cinco ejecuciones) | 43,0–46,2 | 43,1–44,5 | 66,8–67,9 | 66,6–69,3 | 1,54–1,70 s |

- **Comparación.** El rendimiento es equivalente al de `9f6f27f2`. La mediana de 76 ms del smoke en la ejecución A está dentro de la dispersión ya observada.
- **Adopción y memoria.** La adopción tarda entre 1,9 y 2,7 s, y el montón retenido es de unos 40 MB en E3 y 65 MB en el smoke, como antes.
- **D1-A no añade trabajo.** Solo cambia qué contadores se cargan: en el corpus la memoria nunca recurre al respaldo y las preparaciones son las mismas.

## 8. Estado y decisiones del autor

- **Commits locales** sobre `afe43934`:
  - `17766fca`: D1-A;
  - `f61f2954`: inventario, candidato congelado;
  - el commit de este registro.
- **Publicación:** nada publicado; `main`, las etiquetas y `geocedg_book` no cambian.
- **Separado:** la rama del verificador (`08776f45`) sigue aparte.
- **Decisiones del autor:**
  - **OBS-01:** aportar el documento y el canal de observación, o cerrarla como no reproducible.
  - **OBS-02:** confirmar que su copia estaba adoptada, o cerrarla como conforme.
  - **Repetición focal opcional** sobre `f61f2954`: no hay ningún defecto corregido en las
    observaciones. D1-A solo cambia la contabilidad, con resultados publicados idénticos (§5).
  - **Aprobación de `CU3-A`** sobre el candidato definitivo.
  - **R1–R6** de `CU3-B`.
  - **Incorporación** de la corrección del verificador.
