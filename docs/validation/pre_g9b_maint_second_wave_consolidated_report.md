# PRE-G9B-MAINT, segunda ola — informe consolidado de B5a, A-DXF0 y A2

**PRE-G9B-MAINT SECOND WAVE — TECHNICAL CANDIDATES — PENDING AUTHOR REVIEW**

| Campo | Valor |
|---|---|
| Carácter | Informe técnico consolidado del coordinador. `selfApproved=false`. Evidencia, no autoridad; no registra ninguna aprobación |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Segunda ola coordinada: MAINT-B5a + MAINT-A-DXF0 + MAINT-A2», del autor, 2026-11-24 |
| Base común | `228580e9684d2c54650f8d461c2a4d1142a7ef55`, árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae` (`main` = `origin/main` = remoto, comprobado al empezar) |
| Ruta y clase | `CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = INTEGRATED_PHASE` en los tres; nivel `STATIC + PHASE + INTEGRATION` (`CORE + FOCUSED`, no exhaustivo), más las suites completas compartida y Desktop como diagnóstico |
| `FINAL` | No ejecutado (no autorizado). `VERIFICATION_ESCALATION_REQUEST` pendiente (A2, §I-3) |
| Publicación | Nada publicado: sin push, sin `main`, sin etiquetas, releases ni paquetes |
| Este documento | Descendiente documental del candidato integrado `18b8ff905`; no cambia código, pruebas, inventarios ni registros. Sus propias ejecuciones (`STATIC`) las informa el artefacto siguiente |

## A. Resumen ejecutivo

| Trabajo | Estado técnico | Candidato | Pendiente del autor |
|---|---|---|---|
| `PRE-G9B-MAINT-B5A` | verificado (`STATIC`, `PHASE`, `INTEGRATION`); suites completas sin fallos propios | `65ca487da` | parámetros D-B5a-1 a 6; revisión |
| `PRE-G9B-MAINT-A-DXF0` | verificado; suites completas sin fallos propios | `177da00f6` | smoke manual; Q-DXF-3; revisión |
| `PRE-G9B-MAINT-A2` | verificado; suites completas sin fallos propios | `c63b10aad` | opción A (CD-1) y escalado de verificación; `κ_max`; hueco BS-24; smoke manual; revisión |
| Biblioteca compartida | hito técnico de dependencia (no PASS) | `d22c4cc85` | — |
| Ensayo de integración | verificado (`STATIC`, tres `PHASE`, `INTEGRATION`); suite Desktop completa solo con el fallo histórico | `18b8ff905` | decisión sobre la integración definitiva |

Capacidades implementadas:

- **B5a:** comprobación de escalado de cuerdas de §28.3.1 (`conditioned-route-jump-guard/v1`) como
  condición 8 de `conditioned-length-scalar/v2`. La sonda P-1 deja de dar número; las otras 60
  rutas condicionadas admitidas hoy siguen admitidas con el mismo valor bit a bit.
- **A-DXF0:** importación de entidades DXF `SPLINE` 2D, polinómicas y racionales, como referencias
  de sesión `R-a` de solo lectura, fuera del DAG, con procedencia, render determinado, inspector,
  transformación explícita y diagnósticos tipados.
- **A2:** familia `bspline-2d/v1` (`BSpline` con sus tres formas) como proveedor de `GeoLocusV2`;
  representación polinómica local por tramo; persistencia `<geocedgBSpline>` con fallo cerrado y
  reversión; Undo/Redo, copia y redefinición con paridad SplineV2; SplineV2 bit-idéntica.
- **Biblioteca:** `org.geocedg.common.math.bspline`, una sola implementación pura, compartida por
  A-DXF0 y A2.

No implementado (fuera de alcance): B2, B5b, T-A, §28.2, A-DXF1, A3, A4, A5, A7, A-R, `R-c`, `R-d`,
I2G0, F8, G9B.

Bloqueos abiertos: ninguno. Durante la ola hubo una detención (A2, BS-8) resuelta por la decisión
del coordinador CD-1, que se somete al autor (§I-2), y dos defectos aislados (B5a y A2) detectados
por el ensayo y corregidos con commits focales (§G.2). Decisiones necesarias: §I.

## B. Arquitectura y cambios

```text
                 org.geocedg.common.math.bspline   (biblioteca pura, d22c4cc85)
                   │                          │
   importer.dxf (adaptador, sin núcleo)       kernel.bspline + AlgoBSpline + CmdBSpline (núcleo)
                   │                          │
   Desktop: almacén de sesión R-a,            Locus V2: LocalPiecewisePolynomialLocus2D (aditiva),
   overlay de la vista gráfica 1,             accesor compartido de coeficientes,
   inspector, menú (perfil)                   OneSidedLocusDifferentialEvaluator2D (aditiva, CD-1)

   B5a (independiente): kernel.locus.metric — LocusConditionedRouteJumpGuard2D,
   retención aditiva de nodos en LocusMetricEstimator2D, condición 8 en conditionedBetween
```

| Incremento | Capa | Módulos principales | Interfaces nuevas | Persistencia |
|---|---|---|---|---|
| B5a | núcleo, métrica Locus V2 | `LocusConditionedRouteJumpGuard2D` (nuevo), `LocusMetricEngine2D`, `LocusMetricEstimator2D` (accesor aditivo condicionado a una bandera), `LocusMetricConditionedRoute2D` (versión `v2`), `MetricDiagnosticCode2D`, `PolicyV2LocusMetricCapability2D`, etiquetas `menu*.properties` | ninguna pública | sin XML nuevo |
| A-DXF0 | adaptador de importación + Desktop | `org.geocedg.common.importer.dxf` (nuevo), `AppGeoCeDG`, `GeoCeDGEuclidianView`, diálogos, `apps/geocedg/application-profile.yml` | ninguna en el núcleo | ninguna (`R-a`) |
| A2 | núcleo | `org.geocedg.common.kernel.bspline` (nuevo), `AlgoBSpline`, `CmdBSpline`, registro de comandos, consumidores polinómicos, `LocusSimilarityEvaluator2D`, `LocusMetricEstimator2D` (rama unilateral), `AlgoElement` (gancho vacío), `MyXMLHandler`, `MyXMLio`, `AppGeoCeDG` (mensaje tipado) | `LocalPiecewisePolynomialLocus2D`, `OneSidedLocusDifferentialEvaluator2D` (opcionales, aditivas) | `<geocedgBSpline contract="bspline-2d/v1" knots=…>` con fallo cerrado tipado |

Separación núcleo/adaptadores: la biblioteca y el importador no importan `Construction`,
`GeoElement` ni tipos del núcleo (pruebas de frontera en ambos). Compatibilidad: ningún documento
existente cambia de significado salvo lo declarado en la nota de versión de B5a (§C); un documento
con `BSpline` es rechazado entero por la base y por 1.2.2 (BS-22).

## C. B5a

Informe focal: `docs/validation/pre_g9b_maint_b5a_candidate_report.md` (rama
`phase/pre-g9b-maint-b5a`, worktree `GeoCeDG-wt-maint-b5a`).

- **Parámetros seleccionados** (dentro de los intervalos de §28.3.1): `ρ_J = 0,90`, `m = 3`,
  `L = 6`, `ε_res(λ) = max(τ_λ, 2^8·u·M_λ)`, `E_cap = 16 384`.
  - `ρ_J` 0,85 → 0,90: con 0,85, una ruta continua admitida hoy con velocidad divergente interior
    `α = 0,7` (por debajo del umbral 0,766 de un extremo) quedaba refusada.
  - `L` 5 → 6: devuelve el salto mínimo detectable a `0,5·s·h_0`.
  - `E_cap` 8 192 → 16 384: con 4 evaluaciones por hoja, 8 192 refusaba rutas suaves de más de
    2 048 hojas (`sin 20u`, 2 145 hojas).
  - Factor `2^8`: mismas decisiones que `2^4` hasta `10^11`, con 16 veces más margen.
- **Resultados adversariales:** P-1 recta y arco sin número con `CHORD_SCALING_JUMP_SIGNATURE`; los
  tramos antes y después del salto siguen siendo número; batería de ángulos 0–0,35 rad, tamaños
  desde la resolución, posiciones diádicas y no diádicas, varios saltos, ida y vuelta, oscilación;
  los seis límites declarados fijados por pruebas; el extremo de pliegue conserva
  `WORK_BUDGET_EXHAUSTED`.
- **Modelos del autor:** 36 rutas de `w_2`, `a_3` y `trHole`, admitidas antes y después con el mismo
  valor bit a bit.
- **Costes:** 4 evaluaciones por hoja (+60–80 % por ruta); máximo `a_3`, 512 hojas ≈ 2 048
  evaluaciones frente a 16 384; ninguna hoja bajo resolución ni presupuesto agotado.
- **Traslaciones:** idéntico hasta `|T| = 10^5`; primeras hojas bajo resolución en `10^7`. Rango
  declarado (no garantía universal): `≤ 10^5` para el resultado completo y `≤ 10^6` para las
  decisiones de la comprobación.
- **Falsos positivos (negativas conservadoras):** velocidad divergente con `α ≥ 0,85` en un nodo y
  desde `α ≈ 0,78–0,80` en el interior; rampas muy estrechas; estacionarios de orden `≥ 34`.
  Ninguna ruta de las baterías ni de los modelos la produce.
- **Determinismo de caché:** las claves de ruta llevan el sufijo del tramo y solo se construyen con
  retención de nodos; pruebas de orden (compartida y Desktop `w_2`) idénticas en ambos órdenes.
- **Limitaciones de muestreo:** los seis límites de §28.3.1, cada uno fijado por una prueba que
  documenta su alcance.
- **Regresiones:** ninguna. Defecto aislado corregido (§G.2): faltaban las etiquetas de los tres
  diagnósticos en `menu*.properties`.
- **Nota de versión:** un `LocusLength(L, A, B)` sobre una ruta condicionada con firma, inconclusa
  o con presupuesto agotado pasa de número a indefinido; el resultado rico conserva la estimación.

## D. A-DXF0

Informe focal: `docs/validation/pre_g9b_maint_a_dxf0_candidate_report.md`; guía de smoke:
`docs/validation/pre_g9b_maint_a_dxf0_author_smoke_guide.md` (rama
`phase/pre-g9b-maint-a-dxf0`, worktree `GeoCeDG-wt-maint-a-dxf0`).

- **Cobertura DXF:** ASCII; sección `ENTITIES`; `SPLINE` de espacio modelo por puntos de control,
  anclados, no periódicos, planos, grado ≤ 7, ≤ 1 024 puntos; pesos explícitos o implícitos
  (contrato §4.6).
- **DX-1 a DX-13:** todos cubiertos y en verde (informe focal §3). DX-1: (a) evaluador frente a la
  evaluación racional exacta de los datos binary64, razón máxima 0,617 (Q y H) y 1,57 (cúbica) bajo
  las constantes racionales derivadas; (b) curva de peso exacto frente al oráculo circular,
  5·10⁻⁸⁰; (c) discrepancia por redondeo del peso, 1,31·10⁻¹⁷; redondeo de lectura, 2,73·10⁻¹⁷.
- **Procedencia:** SHA-256 del archivo y de los datos (codificación canónica con marca de ausencia
  de pesos), nombre base, versión, identificador, capa, ordinal, unidades, plano y transformación;
  sin campos temporales; idéntica en dos importaciones.
- **Render:** teselación única en unidades de modelo, tolerancia `10^-4·diag`, límite de 200 000
  evaluaciones con marca de incompleto; idéntica a dos zooms, dos tamaños de letra y escala 1/2.
- **Separación del DAG:** `Construction`, su XML y el historial de Undo idénticos tras importar,
  ocultar, quitar, deshacer y rehacer; sin impacto ni ajuste magnético; DXF y LaTeX idénticos con y
  sin referencias; archivo DXF intacto.
- **Constantes:** valores candidatos sin cambio (64 MiB, 10 000 entidades, grado 7, 1 024 puntos,
  razón de pesos 10⁶, `10^-4`); 10 000 splines se importan en menos de 1 s.
- **Desviaciones registradas** (no constan todas en el informe focal congelado; se recogen aquí):
  la acción vive en `apps/geocedg/application-profile.yml` (el prompt citaba `menu*.properties`; el
  perfil es el mecanismo real); una entrada en `tools/agent/workspace-profile-validation.ps1` con
  nuevo pin de contrato estático; `.gitattributes` fija `eol=lf` de los fixtures DXF; literales de
  recuento de acciones y de titulares; **sin bandera de función** (madurez `experimental`).
- **Limitación declarada (Q-DXF-3):** las exportaciones de imagen (PNG, portapapeles, impresión, PDF,
  SVG, EMF) no muestran referencias; solo las dibuja la vista gráfica 1 en vivo; la vista 2 no.
- **Smoke manual:** pendiente del autor, con la guía y los archivos DXF reproducibles.

## E. A2

Informe focal: `docs/validation/pre_g9b_maint_a2_candidate_report.md`; guía de smoke:
`docs/validation/pre_g9b_maint_a2_author_smoke_guide.md` (rama `phase/pre-g9b-maint-a2`,
worktree `GeoCeDG-wt-maint-a2`).

- **Resultados BS:** BS-1 a BS-13 y BS-15 a BS-23 cubiertos; BS-14 corresponde a A3; BS-24 cubierto
  en lectura, validación de versión, conservación del infoset, Undo y copia, con la aplicabilidad
  condicionada a A-DXF1 (informe focal §4).
- **Error numérico:** `K_pos = 8p+1`, `K_der = 8p−3`, `ρ_der = K_der·2^-33`,
  `K_loc = (17p−14)·3^p+1`; máximos observados E1 3,34 y E3 2,48 incluidas familias adversas; el
  evaluador del núcleo es, bit a bit, el de la biblioteca.
- **Condicionamiento local:** coeficientes locales dentro de `K_loc` incluso con nudos en
  `[10^6, 10^6+11]` (κ ≈ 10⁴²) y rango `[−10^8, 10^8]` (κ ≈ 10⁵⁶), donde los globales perderían toda
  precisión; inventario de los 13 ficheros de producción que usan `PiecewisePolynomialLocus2D`:
  ningún consumidor lee coeficientes globales de una fuente que ofrece locales; la regla residual
  `κ_max = 2^16` está implementada y probada, inactiva en producción.
- **Longitud (BS-8) y CD-1:** la longitud de clase 3 a través de un nudo `C^0` (todo nudo interior de
  grado 1, nudo de multiplicidad `p`, costura cerrada) fallaba cerrada (`MAXIMUM_DEPTH`) porque el
  estimador tomaba en el extremo superior de cada pieza la derivada del tramo propietario (el
  derecho). Corregido con la opción A (§I-2). Todos los casos quedan dentro del error declarado
  frente a Gauss-Legendre de 60 dígitos (nudo triple: 4,4·10⁻¹² frente a 1,8·10⁻⁹).
- **Persistencia:** elemento `<geocedgBSpline>` antes de `<input>`; causas
  `UNSUPPORTED_CONTRACT_VERSION`, `CONTRACT_MISSING`, `MALFORMED_CONTRACT` y
  `UNSUPPORTED_PROVENANCE_VERSION`; reversión completa en apertura nativa, `setXML`, Undo/Redo,
  portapapeles, Insert File, redefinición y macros; mensaje tipado en la apertura; protección para
  macros llamadas `BSpline`.
- **Identidad:** identidad durable nueva al copiar; redefinición con paridad exacta con SplineV2
  (ruta ordinaria rechazada en ambas; sustitución ADR 0026 aplicada con identidad nueva en ambas).
- **Consumidores:** intersección unilateral, pares, interacción de puntos y envoltura de semejanzas
  por el accesor de coeficientes locales; por encima de 64 tramos responde la ruta del evaluador
  (conducta de la base, sin motivo propio).
- **SplineV2 (BS-13):** dorado capturado en la base antes de cualquier edición; coeficientes,
  intersecciones, pares, interacción, longitudes, imágenes por semejanza y XML idénticos bit a bit.
- **Lectores anteriores (BS-22):** la base y 1.2.2 rechazan el archivo entero con
  `MALFORMED_RECORD` y el documento abierto no cambia; evidencia en
  `geocedg/validation/pre-g9b-maint-a2/bs22-older-reader/` (fixture SHA-256 `671ee7ad…`).
- **Corrección focal** (§G.2): literal de titulares de `PreG9BR6PlusGProductMetadataTest` (103 →
  105) y tres selecciones históricas (`pre-g9b-r4.shared` 9, `pre-g9b-r6.shared` 15,
  `pre-g9b-r6-plus-a1.shared` 21) que la adición de `cmdBSpline` había dejado desfasadas.

## F. Biblioteca común

- **Commit compartido:** `d22c4cc85aefc454ee8598711d717d21db57da7c`, árbol
  `2d722f50cad78f656b49f9312167ed819c385b80`, rama `phase/pre-g9b-maint-a-math`, padre la base;
  declarado hito técnico de dependencia por el coordinador (no PASS autoral). `STATIC`
  `verification-80bf049d51e347cb924dba7268b3221d` aceptado.
- **API:** `BSplineData.validate` (una causa tipada en el orden de §7) y consultas estructurales
  (dominio, tramos, propiedad de nudos, continuidad `C^{p-k}`, cierre exacto, hodógrafa);
  `BSplineEvaluation` (punto y primera derivada por de Boor homogéneo; ruta polinómica sin pesos
  materializados, BS-15 por construcción; evaluación en un tramo dado para límites laterales);
  `BSplineKnotInsertion`; `BSplineSpanForms` (Bernstein, potencias locales ascendente y descendente
  solo con datos locales, globales correctamente redondeados con redondeo propio);
  `BSplineCanonicalEncoding` (SHA-256); resultados tipados sin excepciones de control.
- **Pruebas:** 45 pruebas directas con referencia racional exacta independiente
  (`ExactRationalBSplineReference`, reutilizada por A-DXF0 y A2), criterios E1–E5 y nota de
  derivación de constantes.
- **Una sola implementación:** A-DXF0 (`8cf6cf8f9`) y A2 (`0207b605b`) la incorporaron por merge
  normal, sin copias ni cambios de API; en el ensayo, el único commit que toca sus fuentes es
  `d22c4cc85`.

## G. Verificación

Todas las ejecuciones con `tools/agent/verify.ps1`, PowerShell 7.6.10, registros en
`artifacts/agent/<nombre>/` del worktree correspondiente. El coordinador comprobó cada recibo
(`run_state = COMPLETED`, candidato y árbol exactos). Diagnósticos: solo los dos permanentes
(`diagnostic.governance` FINDING y `diagnostic.historical-consistency` UNAVAILABLE).

| Candidato | Commit / árbol | `STATIC` | `PHASE` | `INTEGRATION` |
|---|---|---|---|---|
| B5a | `65ca487da35d34d500fb12e5d5c815e03eca5754` / `31ee1a88242ac3790030053e9cfe367281e64bfc` | `verification-63909aac9da14f2181c0adf302a955b8` | `verification-f4b3e898806a494bbc1a8fabc1f779be` (6/6) | `verification-9bad09f9b51e494a9ae1cfd7d27e8395` (18/18) |
| A-DXF0 | `177da00f6ca886a40b538713fafb49f01514992d` / `f2f078287623f2566002e2f9e35a89f2e0872727` | `verification-e089f558b4de4b749d7736776ed86b50` | `verification-2e88ca1ce62e4a9b9c07714b2734deb2` (6/6) | `verification-afb1353d2c7c47b38bcaef5bd5b1d9a1` (18/18) |
| A2 | `c63b10aade4f2792c855c1bc4ad6278f9a54fcfc` / `602e70b7a17e9e50c7d56504acab6799ac8ca35c` | `verification-3ed8d4fa0f11481cb878d49eee90ecaf` | `verification-41dfd9fa847f46a09d83baaff0efe37a` (6/6) | `verification-496a70d0ef6841a9bfb2793ffd692525` (18/18) |
| Biblioteca | `d22c4cc85aefc454ee8598711d717d21db57da7c` / `2d722f50cad78f656b49f9312167ed819c385b80` | `verification-80bf049d51e347cb924dba7268b3221d` | — | — |
| Ensayo integrado | `18b8ff9053071d52d21d011ca1db5f2a104f311b` / `026823261704e03edb52227d05dce6b1c0c1130e` | `verification-d9d9fda608d344ad9e7da52c9bcd3ce9` | B5A `verification-a842f2057b384440931fa67b1c529b9e`; A-DXF0 `verification-74f8dd9bd6014873a8a1c9a63b1c2c1e`; A2 `verification-8070a349fcc24c6ba7c701437a665697` (6/6 cada una) | `verification-936bb240ceea412eaf660ebfd22593e1` (18/18) |

Todas `ACCEPTED / COMPLETE`, código de salida 0. Cada incremento:
`TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`; ninguno aprobado.

| Candidato | Rama / worktree | Suites completas (diagnóstico, productor ejecutado) |
|---|---|---|
| B5a | `phase/pre-g9b-maint-b5a` / `GeoCeDG-wt-maint-b5a` | compartida 8 077, 0 fallos; Desktop 2 676, 1 fallo (histórico CU2) |
| A-DXF0 | `phase/pre-g9b-maint-a-dxf0` / `GeoCeDG-wt-maint-a-dxf0` | Desktop completa ejecutada durante el desarrollo (encontró y corrigió un literal); compartida por `final.shared` 8 094 |
| A2 | `phase/pre-g9b-maint-a2` / `GeoCeDG-wt-maint-a2` | compartida 8 126, 0 fallos; Desktop 2 677, 1 fallo (histórico CU2) |
| Ensayo | `integration/pre-g9b-maint-second-wave` / `GeoCeDG-wt-maint-wave2-integration` | compartida 8 184, 0 fallos; Desktop 2 687: 2 685 pasan, 1 omitida (lista permitida), 1 fallo (histórico CU2) |

Selecciones de fase en el árbol integrado: B5a 171 compartidas / 50 Desktop; A-DXF0 60 / 274; A2
178 / 113. El fallo histórico es
`PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority` (desde `9ccefdcf`,
reproducido en la base). Cobertura: `CORE + FOCUSED` más las suites completas como diagnóstico; no se
extrapola a `FINAL`.

Ejecuciones sustituidas (registradas, no relabeladas): B5a `69347d9f6` (`STATIC`
`verification-236f4cb4…`, `PHASE` `verification-411a1cb7…`, `INTEGRATION`
`verification-d740f624…`); A2 `1b82a6ed2` (`STATIC` `verification-b1228fdf…`, `PHASE`
`verification-7666789a…`, `INTEGRATION` `verification-a295c756…`); primera pasada del ensayo
`418d35231` (`STATIC` `verification-048a35db…`, `PHASE` `verification-1c016d84…`,
`verification-9bc3ae24…`, `verification-0ef3df29…`, `INTEGRATION` `verification-fc856961…`). Todas
aceptadas sobre su commit, que ya no es el candidato.

### G.1 Ensayo de integración

- Worktree `GeoCeDG-wt-maint-wave2-integration`, rama `integration/pre-g9b-maint-second-wave` desde
  la base. Merges `--no-ff`, sin squash, rebase ni cherry-pick:
  1. B5a `69347d9f6` → `1e5225483` (árbol idéntico al de B5a);
  2. A-DXF0 `177da00f6` → `4b7d04476` (trae la biblioteca una vez);
  3. A2 `1b82a6ed2` → `d848713d3`;
  4. reconciliación `418d35231` (primera pasada);
  5. corrección de B5a `65ca487da` → `d54a85b31`;
  6. corrección de A2 `c63b10aad` → `18b8ff905` (candidato integrado).
- Conflictos, todos en datos derivados o adiciones paralelas: valores de descubrimiento y pines del
  registro (regenerados), selecciones del inventario (compuestas como datos: 106), fases y nodos
  del registro (unión: 93 fases, 271 nodos, sin duplicados), `modified-files.yml` (unión, 1 491
  entradas), recuentos de titulares de la guía (123/106, confirmados por ejecución). El código,
  incluido `LocusMetricEstimator2D` (retención de B5a y velocidad unilateral de A2), se fusionó sin
  conflicto y las pruebas de ambos pasan juntas.
- Reconstrucción determinista con los procedimientos canónicos: descubrimiento 7 207 / 2 641;
  `final.shared` 8 184 (ejecutado, verde); `pre-g9b-r4.shared` 9, `pre-g9b-r6.shared` 15,
  `pre-g9b-r6-plus-a1.shared` 21; pines `junit_inventory` `2a4fc54f…` y `static_contracts`
  `a1dc3e85…`; literales de cobertura 106/93. En la segunda pasada los merges ya daban el estado
  derivado correcto (el actualizador sobre una copia reprodujo recuentos y resúmenes), por lo que no
  hubo commit de reconciliación adicional.
- Observación menor: la entrada de `PreG9BR6PlusGProductMetadataTest` en `modified-files.yml`
  conserva el texto de A2 («moves to 105»); en el árbol compuesto el recuento es 106. Se corrige en
  la integración definitiva.

### G.2 Defectos aislados detectados por el ensayo

Las campañas aisladas solo ejecutaban la selección de fase y el núcleo de `INTEGRATION`; el ensayo
añadió las suites completas y encontró dos defectos que ninguna selección cubría:

| Defecto | Incremento | Causa | Corrección |
|---|---|---|---|
| `G9U0LocalizationHelpTest.l02…` falla | B5a | tres constantes nuevas de `MetricDiagnosticCode2D` sin etiqueta `LocusV2.Results.Value.*` en `menu`, `menu_en` y `menu_es` | `65ca487da`: etiquetas (inserción por bytes, EOL mixto conservado) y la clase en la selección Desktop de B5a |
| `PreG9BR6PlusGProductMetadataTest` falla; tres pines desfasados | A2 | dos titulares nuevos de la guía (103 → 105) y `cmdBSpline` en `GeoCeDGCommandsTest` | `c63b10aad`: literal 105 y selecciones refrescadas con evidencia ejecutada |

Ambas correcciones se verificaron aisladas (`STATIC`, `PHASE`, `INTEGRATION`, suites completas) y en
la segunda pasada del ensayo.

## H. Riesgos y deuda

**Errores bloqueantes:** ninguno abierto.

**Limitaciones aceptadas o declaradas (para decisión, §I):**

- B5a: negativas conservadoras; límites 1–6 de §28.3.1 no detectables por muestreo finito; coste
  +60–80 %; rango de traslaciones mostrado `≤ 10^5`.
- A-DXF0: exportaciones de imagen sin referencias; solo la vista gráfica 1; persistencia de sesión.
- A2: por encima de 64 tramos, ruta del evaluador sin motivo propio; aplicabilidad BS-24 pendiente
  de A-DXF1; sin certificados (A4).

**Deuda previa a la ola (no corregida, con evidencia):**

1. Pin del contrato estático de `geocedg/resources/assets-manifest.yml` desfasado desde U2-R1
   (`f99a99785`): `INFRA_UNIT infra.static-semantic`, y por tanto `FINAL`, rechazarían sobre la base
   publicada.
2. `final.desktop` desfasado: contiene el fallo histórico
   `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority` y el actualizador
   rechaza evidencia con fallos.
3. Estimador §25 en coordenadas `≥ 10^11`: admite valores erróneos (0,1186 en lugar de 0,0894 en
   `10^11`; 0 en `10^16`).
4. Orden no determinista de los resultados públicos de pares SplineV2 (identificadores durables
   aleatorios).
5. Selecciones históricas ya desfasadas en la base por filtros de método (lista en la evidencia del
   ensayo y en el informe de A2 §7.4).
6. Colisión de nombres K15: una macro de usuario llamada `BSpline` oculta el comando nativo.

**Trabajo diferido:** B5b, T-A, descubrimiento de raíces de `Intersect` a través de un salto
alineado; `R-c`; A-DXF1; A3; A4; A5; A7; A-R.

**Propuestas nuevas:** un mantenimiento del verificador (deudas 1 y 2) antes de cualquier `FINAL`;
incluir las clases de recuento de guías y de etiquetas de diagnósticos en las selecciones de las
fases que tocan guías o diagnósticos.

## I. Decisiones solicitadas al autor

1. **Parámetros de B5a (D-B5a-1 a 6).** Recomendación: aceptar `ρ_J = 0,90`, `m = 3`, `L = 6`,
   factor `2^8`, `E_cap = 16 384` y las lecturas L-1 a L-9. Consecuencia: política final registrada,
   coste +60–80 % por ruta, negativas conservadoras declaradas. Alternativa: volver a los candidatos,
   con la refusa de una ruta continua admitida hoy.
2. **CD-1, opción A de A2** (interfaz opcional `OneSidedLocusDifferentialEvaluator2D` y rama del
   estimador métrico de clase 3). La autorizó el coordinador como extensión aditiva de consumidor
   bajo la instrucción §9.2; no es decisión del autor mientras no la tome (el informe focal la
   titula «decisión del autor» en ese sentido). Recomendación: aceptarla. Consecuencia: longitudes de
   B-splines con nudos `C^0` disponibles; toda otra fuente conserva su aritmética (dorado BS-13).
   Alternativa B: declarar esas longitudes no disponibles, contraria a `bspline-2d.md` §9.1 y §15.
3. **`VERIFICATION_ESCALATION_REQUEST` de A2** (superficie genérica opcional nueva de Locus V2).
   Recomendación: dispensa explícita de `FINAL` para esta ola. Si se desea `FINAL`, autorizar antes el
   mantenimiento de las deudas 1 y 2 de §H, porque hoy `FINAL` rechazaría por causas previas ajenas a
   la ola. Consecuencia de la dispensa: aceptación en `CORE + FOCUSED` más las suites completas
   compartida y Desktop del ensayo.
4. **`κ_max = 2^16`.** Recomendación: mantenerlo como valor candidato con la regla inactiva.
   Consecuencia: ninguna en producción.
5. **Hueco BS-24.** Recomendación: aceptar que la aplicabilidad de la procedencia la defina el
   contrato de A-DXF1 (atributo de resumen). Consecuencia: A2 solo lee y conserva el bloque.
6. **Q-DXF-3** (exportaciones de imagen sin referencias; solo la vista 1). Recomendación: aceptarlo
   como limitación declarada de `v1`/`R-a`. Alternativa: un incremento posterior que añada el overlay
   a `exportPaint`.
7. **Constantes de importación de A-DXF0.** Recomendación: mantener los valores candidatos.
8. **Desviaciones de A-DXF0** (perfil, puerta del perfil, `.gitattributes`, sin bandera de función).
   Recomendación: aceptarlas.
9. **Notas de versión.** B5a: número → indefinido en rutas con firma, inconclusas o sin presupuesto.
   A2: los lectores ≤ 1.2.2 rechazan el archivo; colisión K15. Recomendación: incluirlas en la
   primera versión que contenga la ola.
10. **Smoke manual** de A-DXF0 y A2 (opcional en B5a con `w_2`). Requerido antes de aprobar.
11. **Integración definitiva.** Recomendación: tras las decisiones 1–10 y los smokes, autorizar una
    integración definitiva desde los SHAs aprobados exactos; el ensayo muestra que es mecánica y no
    requiere decisiones científicas.

## J. Próximos pasos

- **Correcciones focales posibles:** las que resulten del smoke o de las decisiones 1–8.
- **Smoke manual requerido:** A-DXF0 y A2, con sus guías.
- **Aprobación de candidatos:** decisión individual por SHA exacto: B5a `65ca487da`, A-DXF0
  `177da00f6`, A2 `c63b10aad`.
- **Integración definitiva:** instrucción nueva; reconstrucción determinista de inventarios como en
  el ensayo.
- **Promoción futura:** autorización expresa del SHA exacto (`AGENTS.md` §8.1).
- **Incrementos posteriores no autorizados:** B2, B5b, T-A, A-DXF1, A3, A4, A5, A7, A-R, I2G0, F8,
  mantenimiento del verificador, G9B.

Se conservan todos los worktrees y ramas: `GeoCeDG-wt-maint-b5a`, `GeoCeDG-wt-maint-a-math`,
`GeoCeDG-wt-maint-a-dxf0`, `GeoCeDG-wt-maint-a2` y `GeoCeDG-wt-maint-wave2-integration`; los de la
primera ola y el checkout principal no se tocaron.
