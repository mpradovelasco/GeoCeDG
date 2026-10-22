# PRE-G9B-CU2 — Registro de nueva línea base de `CU0` (paso T12)

| Campo | Valor |
|---|---|
| Carácter | Registro de nueva línea base del procedimiento C1-4 ([estrategia de implementación](../architecture/pre_g9b_cu2_implementation_strategy.md) §5, puntos 3 y 4; decisión D-CU2-14). Lista cada campo de la línea base de `CU0` que cambia con su valor anterior y nuevo, su causa y la prueba de `CU2` que lo justifica. No es evidencia de verificación de aceptación ni aprueba nada |
| Estado | Candidato técnico del paso T12, en la rama local de implementación; pendiente de revisión autoral; `selfApproved=false` |
| Línea base | [`cu0-geometric-reference-baseline.json`](../../geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json), **sin cambios**: conserva las firmas fijadas como evidencia histórica (procedimiento, punto 4; resolución autoral previa a T12, punto 5) |
| Ejecución | `tools/benchmark/pre-g9b-cu/run-harness.ps1 -Scenario references` con `source_commit` `6382adfeef29ca4801acf56dcbb8078d37147ca3`, `source_tree` `85dccad2853b517032eb0e7f3fbe133d69747bf9` y `product_sources_modified=false`. El árbol de trabajo solo difería en los cuatro ficheros de este paso: las tres pruebas de `CU0` afectadas, una de ellas el soporte de referencias, y el manifiesto del corpus. La salida quedó en el directorio temporal de la sesión, fuera del repositorio; SHA-256 de la ejecución: `4ceb9f7d1b8590df8470b77348eff7a6ba51a468b38749bd0425f53948d4ca54` |
| Tolerancias de la línea base | puntos `1E-9`, longitudes `1E-6`, parámetros de extremos de rama `1E-7` |

## 1. Resumen

| Grupo | Hojas | Idénticas bit a bit | Cambian | Resultado |
|---|---:|---:|---:|---|
| `preserve` | 661 | 621 | 40 | Las 40 están dentro de su tolerancia (V-CU0-1) |
| `pins` de `CU2` (`B07`, `H01`, `B06`, `B10.2`) | — | — | 100 | Invertidas con el procedimiento C1-4 (V-CU0-2) |
| `pins` de `CU1` (`B10.1`, `H03`, `H10_H11`) | — | — | 17 | Conservan la inversión de `CU1` (V-CU0-2) |
| `pins` de `CU3` (`B08_B09`, `H02`, `H06`) | — | — | 0 | Sin cambios (V-CU0-2) |
| Total de `pins` | 346 | 229 | 117 | |

Los anexos A y B listan cada campo que cambia, con su valor en la línea base y en la ejecución.

## 2. Secciones `preserve` (V-CU0-1 y V-TR-4)

- **621 hojas idénticas bit a bit**, entre ellas todas las muestras de los loci, los extremos de
  rama y los contactos: el mismo resultado que registró `CU1`.
- **40 hojas derivadas de longitudes métricas**: `d_2`, las abscisas de `Q_1` y `R_1`, el perfil
  de longitud orientada, la longitud total de `o_1`, las muestras de `e_2` y su distancia de costura.
  Es el mismo grupo de 40 hojas que registró `CU1`, y coincide con los valores que su registro de
  verificación cita: la longitud total de `o_1`, 13,236458195804072, y `d_2`, 5,026489133092055. La
  mayor diferencia con la línea base es 1,084E-7, en la longitud total de `o_1`, por debajo de la
  tolerancia de longitud 1E-6.
- **V-TR-4.** `preserve.branch_ends` mide el evaluador del corte, que `CU2` no cambia. Desde el paso
  T11 la definición publicada ya no devuelve puntos donde el certificado establece la no existencia,
  y eso incluye el exceso medido del evaluador más allá de un extremo exacto (F-CU2-1). Por eso
  `PreG9BCu0References` localiza los extremos en el evaluador, con el parámetro canonicalizado por el
  proveedor, como hace la definición antes de leerlo. Así los extremos se reproducen sin cambio, bit a
  bit. Sin esa canonicalización, que redondea el parámetro en un ulp de `π`, las coordenadas diferían
  entre 1,6E-16 y 8,3E-16.

## 3. Fijaciones de `CU2` invertidas (V-CU0-2)

Los métodos de `CU0` no se renombran, aunque su nombre describa la fijación histórica, de modo que
las identidades de la selección `pre-g9b-cu0.desktop` no cambian. Cada aserción de fijación se
sustituye por una comprobación de inversión que nombra la prueba de `CU2` y no conserva el valor
defectuoso como expectativa.

| Fijación | Campos que cambian | Causa | Prueba de `CU0` (inversión) | Pruebas de `CU2` que lo justifican |
|---|---|---|---|---|
| `B07` (80) | En 2,9, el contrato del certificador y los vértices dibujados de `o`, `v`, `w` y `a_1`. En 3,7, 3,85 y 3,9, además: la componente certificada pasa de `[−π, π)` al arco de la mordedura que cruza la costura; `full_period_closure` pasa de verdadero a falso; un subtrazo en lugar de dos; la brecha del dibujo en el extremo de rama pasa de 6,1–24,1 px a 2,6E-5–4,1E-5 px | Certificado híbrido (ADR 0038; [EXI] §15), retirada del certificado de compatibilidad (T11) y política de dibujo v2 (T10) | `PreG9BCu0LocusReferenceTest.existenceSweepPreservesBranchEndsAndPinsTheCompatibilityCertificate` | `PreG9BCu2AuthorModelCertificationTest.everyBiteIsOneArcAcrossTheSeamWithTheAnalyticEnds`; `PreG9BCu2AuthorModelRepresentationTest.aBiteIsDrawnToTheImagesOfItsCertifiedEnds` |
| `H01` (4) | `defined_at_saved_driver` de `o`, `v`, `w` y `a_1`: de falso a verdadero | Regla de publicación de `locus-v2-semantics.md` v1.3 (T11): los loci se publican aunque el punto trazado no exista en el conductor guardado | `PreG9BCu0LocusReferenceTest.existenceSweepPreservesBranchEndsAndPinsTheCompatibilityCertificate` | `PreG9BCu2AuthorModelCertificationTest.theLociArePublishedWhileTheSavedDriverIsInTheGap` |
| `B06` (11) | Vértices dibujados de `o`, `v`, `w`, `a_1`, `e_1` y `o_1`; giro máximo entre cuerdas visibles de `o`, `v`, `w`, `a_1` y `o_1`: de 18,7°–156,8° a 7,95°–9,43° (≤ 10°) | Política de dibujo v2 (T10; DC-6) | `PreG9BCu0LocusReferenceTest.contactsAndTessellationArePreservedAndPinned` | `PreG9BCu2AuthorModelRepresentationTest.turnsAreBoundedOutsideTheCertifiedFolds` |
| `B10.2` (5) | Contrato del certificador; componente certificada de `[−π, π)` a `[0, 2π)`, un arco abierto de longitud completa desde `t_N = 0`; `full_period_closure` de verdadero a falso; segmento dibujado a través del salto: de 13,19 a 0,683, menos de la mitad de la longitud total; `jump_distance` se desplaza 1,084E-7 con la longitud de `o_1`, como en `CU1` | Evento `ORIGIN_WRAP` (decisión D-CU2-3; `locus-v2-metrics.md` §26.2) y política de dibujo v2 | `PreG9BCu0CompositionCharacterizationTest.orientedLengthResetIsDrawnAsAClosedPeriodicCurve` | `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`; `PreG9BCu2AuthorModelExportTest.eachCertifiedComponentIsOnePolylineClosedOnlyWhenPeriodic` |

## 4. Fijaciones de `CU1` (conservan la inversión)

| Fijación | Campos que cambian respecto de la línea base | Comprobación |
|---|---|---|
| `B10.1` (8) | Deltas de revisión y de construcciones métricas anidadas de 2 a 0; llamadas anidadas al evaluador de 38 428 y 43 507 a 9; el corte exterior se construye y sincroniza una vez | `PreG9BCu0CompositionCharacterizationTest.developmentCurvePointsArePreservedAndTheNestedRebuildIsPinned` |
| `H03` (2) | Deltas de revisión y de construcciones métricas de `o_1` al mover el conductor: de 1 a 0 | `PreG9BCu0LocusReferenceTest.constructionStateAndLocusSamplesArePreserved` |
| `H10_H11` (7) | Longitudes de `y = sin(2πkx)`: de 1 (la cuerda) a 4,188275203684875, 8,111828618823123 y 16,06471513503404 para `k = 1, 2, 4`, los valores del informe de `CU1` (§8), bit a bit; circunferencias de radio 1 y 7 con la longitud estructural exacta y de radio 8 y 20 definidas (`2πr`) | `PreG9BCu0SyntheticCharacterizationTest.evaluatorOnlyMetricAliasesPeriodicCurvesAndExhaustsItsBudget`, sin modificar |

Las longitudes de H10 dejaron de estar definidas en el paso T11 y las restituye la decisión autoral
H10 (opción A; `locus-v2-metrics.md` §26.5): son estimaciones condicionadas de clase 3 que no
certifican nada.

## 5. Fijaciones de `CU3` (sin cambios)

`B08_B09`, `H02` y `H06` coinciden bit a bit con la línea base.

## 6. Decisión D-CU2-15

[`models/regression/pre-g9b-cu/manifest.yml`](../../models/regression/pre-g9b-cu/manifest.yml): la
descripción del dominio de validez decía «a vertical cylinder (circle g_1, centre C_c, radius
rbCylinder)»; ahora dice «an oblique cylinder (directrix circle g_1, centre C_c, radius rbCylinder;
frontal generators at alpha_{cyl} = 40 degrees)» (hallazgo F-CU2-6). No cambia ningún hash del
modelo ni ninguna referencia, y la prueba del corpus no fija la descripción.

## 7. Lo que este registro no establece

- No sustituye la verificación de aceptación del paso T13 (`PHASE -Phase PRE-G9B-CU0` y las demás);
  las cifras de este registro están ligadas a la ejecución citada.
- No modifica la línea base de `CU0` ni la sustituye por la ejecución, que no se versiona.
- No aprueba ninguna fijación, prueba ni decisión.

## Anexo A. Campos de `preserve` que cambian (40)

| Campo | Línea base `CU0` | Ejecución | Diferencia |
|---|---|---|---|
| `preserve.state.d_2` | 5.026489096595441 | 5.026489133092055 | 3.65E-8 |
| `preserve.state.points.Q_1[0]` | 22.016898948437557 | 22.01689891194094 | 3.65E-8 |
| `preserve.state.points.R_1[0]` | 22.016898948437557 | 22.01689891194094 | 3.65E-8 |
| `preserve.oriented_length_profile[0][1]` | 6.901127801723794 | 6.90112785710905 | 5.539E-8 |
| `preserve.oriented_length_profile[1][1]` | 10.490475792123593 | 10.49047587328344 | 8.116E-8 |
| `preserve.oriented_length_profile[2][1]` | 13.232945858978653 | 13.232945967333261 | 1.084E-7 |
| `preserve.oriented_length_profile[3][1]` | 0.0035122284512554567 | 0.0035122284708307178 | 1.958E-11 |
| `preserve.oriented_length_profile[4][1]` | 2.745982303666066 | 2.7459823225205247 | 1.885E-8 |
| `preserve.oriented_length_profile[5][1]` | 6.335330288994372 | 6.335330338695095 | 4.97E-8 |
| `preserve.oriented_length_profile[6][1]` | 6.616229982545692 | 6.616230036696987 | 5.415E-8 |
| `preserve.oriented_length_profile[7][1]` | 6.620228104910285 | 6.620228159107107 | 5.42E-8 |
| `preserve.o_1_total_length` | 13.2364580874242 | 13.236458195804072 | 1.084E-7 |
| `preserve.e_2[0][1]` | 20.425159001320896 | 20.42515894713096 | 5.419E-8 |
| `preserve.e_2[1][1]` | 19.90277816106905 | 19.902778104978715 | 5.609E-8 |
| `preserve.e_2[2][1]` | 19.386264404445573 | 19.386264346349236 | 5.81E-8 |
| `preserve.e_2[3][1]` | 18.881616456954838 | 18.88161638889016 | 6.806E-8 |
| `preserve.e_2[4][1]` | 18.39487046529426 | 18.394870402177943 | 6.312E-8 |
| `preserve.e_2[5][1]` | 17.931307394650826 | 17.93130732108981 | 7.356E-8 |
| `preserve.e_2[6][1]` | 17.492821194767046 | 17.492821119225347 | 7.554E-8 |
| `preserve.e_2[7][1]` | 17.071236282134393 | 17.071236206848365 | 7.529E-8 |
| `preserve.e_2[8][1]` | 16.636713711737983 | 16.63671363151282 | 8.023E-8 |
| `preserve.e_2[9][1]` | 16.13243018901952 | 16.13243010267978 | 8.634E-8 |
| `preserve.e_2[10][1]` | 15.496684237656634 | 15.49668414405093 | 9.361E-8 |
| `preserve.e_2[11][1]` | 14.705833653333976 | 14.705833552244204 | 1.011E-7 |
| `preserve.e_2[13][1]` | 26.144484347629966 | 26.14448434201766 | 5.612E-9 |
| `preserve.e_2[14][1]` | 25.35363376299955 | 25.353633750210932 | 1.279E-8 |
| `preserve.e_2[15][1]` | 24.717887804534737 | 24.717887791582143 | 1.295E-8 |
| `preserve.e_2[16][1]` | 24.21360428149904 | 24.213604262749215 | 1.875E-8 |
| `preserve.e_2[17][1]` | 23.77908170664425 | 23.77908168741359 | 1.923E-8 |
| `preserve.e_2[18][1]` | 23.357496807874746 | 23.357496775036573 | 3.284E-8 |
| `preserve.e_2[19][1]` | 22.91901061147991 | 22.919010573172088 | 3.831E-8 |
| `preserve.e_2[20][1]` | 22.45544752794276 | 22.455447492083962 | 3.586E-8 |
| `preserve.e_2[21][1]` | 21.96870154180389 | 21.96870150537176 | 3.643E-8 |
| `preserve.e_2[22][1]` | 21.46405358623213 | 21.46405354791255 | 3.832E-8 |
| `preserve.e_2[23][1]` | 20.94753983524569 | 20.947539789283116 | 4.596E-8 |
| `preserve.e_2[24][1]` | 13.806933469838409 | 13.806933361458585 | 1.084E-7 |
| `preserve.e_2[26][1]` | 13.810442186054345 | 13.810442077699737 | 1.084E-7 |
| `preserve.e_2[27][1]` | 27.03987581658174 | 27.039875816562166 | 1.958E-11 |
| `preserve.e_2[28][1]` | 20.425159003319962 | 20.425158949130022 | 5.419E-8 |
| `preserve.e_2_seam_distance` | 1.9990658017832175E-09 | 1.9990622490695387E-09 | 3.553E-15 |

## Anexo B. Campos de `pins` que cambian (117)

| Campo | Línea base `CU0` | Ejecución | Diferencia |
|---|---|---|---|
| `pins.H03.o_1_revision_delta_on_driver_move` | 1 | 0 | 1E+0 |
| `pins.H03.o_1_metric_builds_delta_on_driver_move` | 1 | 0 | 1E+0 |
| `pins.B07.2.9.o.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.2.9.o.render_vertices` | 17 | 63 | 4.6E+1 |
| `pins.B07.2.9.v.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.2.9.v.render_vertices` | 17 | 62 | 4.5E+1 |
| `pins.B07.2.9.w.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.2.9.w.render_vertices` | 12 | 72 | 6E+1 |
| `pins.B07.2.9.a_1.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.2.9.a_1.render_vertices` | 19 | 57 | 3.8E+1 |
| `pins.B07.3.7.o.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.7.o.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.5117077290126846, 7.325007837228292]` | — |
| `pins.B07.3.7.o.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.7.o.render_vertices` | 31 | 45 | 1.4E+1 |
| `pins.B07.3.7.o.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.7.o.max_render_gap_at_branch_end_px` | 9.024924934331949 | 3.586662884667861E-05 | 9.025E+0 |
| `pins.B07.3.7.v.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.7.v.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.5117077290126846, 7.325007837228292]` | — |
| `pins.B07.3.7.v.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.7.v.render_vertices` | 31 | 42 | 1.1E+1 |
| `pins.B07.3.7.v.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.7.v.max_render_gap_at_branch_end_px` | 9.162175521729221 | 3.432675500444408E-05 | 9.162E+0 |
| `pins.B07.3.7.w.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.7.w.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.5117077290126846, 7.325007837228292]` | — |
| `pins.B07.3.7.w.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.7.w.render_vertices` | 31 | 55 | 2.4E+1 |
| `pins.B07.3.7.w.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.7.w.max_render_gap_at_branch_end_px` | 6.087774019839908 | 2.834584406084593E-05 | 6.088E+0 |
| `pins.B07.3.7.a_1.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.7.a_1.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.5117077290126846, 7.325007837228292]` | — |
| `pins.B07.3.7.a_1.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.7.a_1.render_vertices` | 31 | 45 | 1.4E+1 |
| `pins.B07.3.7.a_1.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.7.a_1.max_render_gap_at_branch_end_px` | 6.1928760328996795 | 2.5513956056254846E-05 | 6.193E+0 |
| `pins.B07.3.85.o.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.85.o.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.7783547084601496, 7.058360857780317]` | — |
| `pins.B07.3.85.o.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.85.o.render_vertices` | 27 | 36 | 9E+0 |
| `pins.B07.3.85.o.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.85.o.max_render_gap_at_branch_end_px` | 23.64981070255557 | 3.913948001176218E-05 | 2.365E+1 |
| `pins.B07.3.85.v.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.85.v.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.7783547084601496, 7.058360857780317]` | — |
| `pins.B07.3.85.v.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.85.v.render_vertices` | 27 | 39 | 1.2E+1 |
| `pins.B07.3.85.v.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.85.v.max_render_gap_at_branch_end_px` | 24.058931853942173 | 3.7766387364387186E-05 | 2.406E+1 |
| `pins.B07.3.85.w.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.85.w.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.7783547084601496, 7.058360857780317]` | — |
| `pins.B07.3.85.w.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.85.w.render_vertices` | 27 | 55 | 2.8E+1 |
| `pins.B07.3.85.w.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.85.w.max_render_gap_at_branch_end_px` | 15.731431401234957 | 2.978817865649632E-05 | 1.573E+1 |
| `pins.B07.3.85.a_1.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.85.a_1.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.7783547084601496, 7.058360857780317]` | — |
| `pins.B07.3.85.a_1.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.85.a_1.render_vertices` | 27 | 46 | 1.9E+1 |
| `pins.B07.3.85.a_1.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.85.a_1.max_render_gap_at_branch_end_px` | 16.319067740682286 | 2.9228474767618684E-05 | 1.632E+1 |
| `pins.B07.3.9.o.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.9.o.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.841152244725083, 6.995563321515119]` | — |
| `pins.B07.3.9.o.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.9.o.render_vertices` | 27 | 36 | 9E+0 |
| `pins.B07.3.9.o.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.9.o.max_render_gap_at_branch_end_px` | 19.64527362874546 | 4.0887131046516815E-05 | 1.965E+1 |
| `pins.B07.3.9.v.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.9.v.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.841152244725083, 6.995563321515119]` | — |
| `pins.B07.3.9.v.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.9.v.render_vertices` | 27 | 36 | 9E+0 |
| `pins.B07.3.9.v.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.9.v.max_render_gap_at_branch_end_px` | 19.914525853748255 | 3.952938756067644E-05 | 1.991E+1 |
| `pins.B07.3.9.w.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.9.w.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.841152244725083, 6.995563321515119]` | — |
| `pins.B07.3.9.w.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.9.w.render_vertices` | 27 | 54 | 2.7E+1 |
| `pins.B07.3.9.w.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.9.w.max_render_gap_at_branch_end_px` | 13.51950921110308 | 3.100897754877261E-05 | 1.352E+1 |
| `pins.B07.3.9.a_1.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B07.3.9.a_1.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[1.841152244725083, 6.995563321515119]` | — |
| `pins.B07.3.9.a_1.full_period_closure` | `true` | `false` | — |
| `pins.B07.3.9.a_1.render_vertices` | 27 | 45 | 1.8E+1 |
| `pins.B07.3.9.a_1.render_subpaths` | 2 | 1 | 1E+0 |
| `pins.B07.3.9.a_1.max_render_gap_at_branch_end_px` | 14.341477823715914 | 3.0690120817814276E-05 | 1.434E+1 |
| `pins.H01.defined_at_saved_driver.o` | `false` | `true` | — |
| `pins.H01.defined_at_saved_driver.v` | `false` | `true` | — |
| `pins.H01.defined_at_saved_driver.w` | `false` | `true` | — |
| `pins.H01.defined_at_saved_driver.a_1` | `false` | `true` | — |
| `pins.B06.loci.o.render_vertices` | 17 | 63 | 4.6E+1 |
| `pins.B06.loci.o.render_max_turn_degrees` | 49.37895463940455 | 7.950860542570332 | 4.143E+1 |
| `pins.B06.loci.v.render_vertices` | 17 | 62 | 4.5E+1 |
| `pins.B06.loci.v.render_max_turn_degrees` | 54.05340429259775 | 8.094943038875446 | 4.596E+1 |
| `pins.B06.loci.w.render_vertices` | 12 | 72 | 6E+1 |
| `pins.B06.loci.w.render_max_turn_degrees` | 156.810500831511 | 8.746583251117148 | 1.481E+2 |
| `pins.B06.loci.a_1.render_vertices` | 19 | 57 | 3.8E+1 |
| `pins.B06.loci.a_1.render_max_turn_degrees` | 38.40026237033245 | 9.1112970703082 | 2.929E+1 |
| `pins.B06.loci.e_1.render_vertices` | 9 | 17 | 8E+0 |
| `pins.B06.loci.o_1.render_vertices` | 27 | 51 | 2.4E+1 |
| `pins.B06.loci.o_1.render_max_turn_degrees` | 18.651115861187147 | 9.425772122370322 | 9.225E+0 |
| `pins.B10.1.single_point_evaluations[0].nested_revision_delta` | 2 | 0 | 2E+0 |
| `pins.B10.1.single_point_evaluations[0].nested_metric_builds_delta` | 2 | 0 | 2E+0 |
| `pins.B10.1.single_point_evaluations[0].nested_evaluator_calls_delta` | 38428 | 9 | 3.842E+4 |
| `pins.B10.1.single_point_evaluations[1].nested_revision_delta` | 2 | 0 | 2E+0 |
| `pins.B10.1.single_point_evaluations[1].nested_metric_builds_delta` | 2 | 0 | 2E+0 |
| `pins.B10.1.single_point_evaluations[1].nested_evaluator_calls_delta` | 43507 | 9 | 4.35E+4 |
| `pins.B10.1.outer_dependency_slice_builds` | 0 | 1 | 1E+0 |
| `pins.B10.1.outer_dependency_slice_synchronizations` | 0 | 1 | 1E+0 |
| `pins.B10.2.existence.certifier_contract` | `locus-existence/provider-compatibility/v1` | `locus-existence/hybrid/v1` | — |
| `pins.B10.2.existence.certified_components[0]` | `[-3.141592653589793, 3.141592653589793)` | `[0.0, 6.283185307179586)` | — |
| `pins.B10.2.existence.full_period_closure` | `true` | `false` | — |
| `pins.B10.2.jump_distance` | 13.236451062964928 | 13.236451171344752 | 1.084E-7 |
| `pins.B10.2.render_segment_across_jump_length` | 13.193358743124648 | 0.6831314720772641 | 1.251E+1 |
| `pins.H10_H11.sinus_on_unit_segment.k=1.reported_length` | 1 | 4.188275203684875 | 3.188E+0 |
| `pins.H10_H11.sinus_on_unit_segment.k=2.reported_length` | 1 | 8.111828618823123 | 7.112E+0 |
| `pins.H10_H11.sinus_on_unit_segment.k=4.reported_length` | 1 | 16.06471513503404 | 1.506E+1 |
| `pins.H10_H11.circle_radius.1.0.reported_length` | 6.283185268677126 | 6.283185307179586 | 3.85E-8 |
| `pins.H10_H11.circle_radius.7.0.reported_length` | 43.98229688073988 | 43.982297150257104 | 2.695E-7 |
| `pins.H10_H11.circle_radius.8.0.reported_length` | `null` | 50.26548245743669 | — |
| `pins.H10_H11.circle_radius.20.0.reported_length` | `null` | 125.66370614359172 | — |
