# GeoCeDG 1.2.0 · Cierre autoral del mantenimiento de interacción con puntos sobre Locus V2

**MANTENIMIENTO CORRECTIVO POSTERIOR A 1.2.0 — PASS — AUTHOR APPROVED**

Decisión autoral de 2026-11-19 (Europe/Madrid), transcrita en la sección G.
`selfApproved=false`. Este registro es documental: no contiene código, no crea
una versión, una etiqueta ni una release, y no modifica el libro.

## A. Identidad

Naturaleza: mantenimiento correctivo focalizado posterior a la publicación de
GeoCeDG 1.2.0. No es una fase G ni una fase del roadmap, y no es una versión
de producto.

| Identidad | Commit | Árbol |
|---|---|---|
| Baseline publicado, etiqueta `v1.2.0` | `4f6a0a9e3ae25702c8c43fef287bf5881e0cb24e` | `ab96f99ad2cfe423e40a0229b0b2f415e02fdff0` |
| Candidato técnico final, integrado en `main` | `335755b2090871b6c36b100de17a1535f96c6016` | `c82c0857472c2eccf4ad9b8054fcdfa8d7568046` |

La etiqueta anotada `v1.2.0` es el objeto
`e7a16a3e23504fb3d7a00989e0b9b3129a989cd4`. El árbol del baseline es el que
declara el mensaje de la etiqueta. Los árboles del mantenimiento se leyeron de
Git.

Rama de procedencia: `maintenance/post-v1.2.0-locus-v2-point-interaction`,
cuatro commits lineales sobre el baseline.

| Commit | Árbol | Propósito |
|---|---|---|
| `daf795dcdc5966d475b2ed0256cd19b19da23585` | `fff4c84cd75ed60f142562a35a66d88105f2372c` | Recuperación del gesto, continuación paramétrica local, creación sobre loci con métricas anidadas, presentación de direcciones y muestreo junto a extremos excluidos |
| `afb70e25f6775fac80c335867dc1382a2c2568ed` | `4758cedd50f78f4460e30a5baeaa2cfaf68533d6` | Conserva las identidades de test inventariadas de las suites de continuación |
| `6d0ea2c17fff811f233b55ea42f782f683f553ef` | `9eb0129882b6a49ef16917961fe5fb38ce3138da` | Revisión correctiva R1–R3 y las mismas muestras en las capacidades de intersección y en sus derivadas |
| `335755b2090871b6c36b100de17a1535f96c6016` | `c82c0857472c2eccf4ad9b8054fcdfa8d7568046` | Solo documentación: A2 expresada como correspondencia de mejor esfuerzo |

Fecha de aprobación autoral y de publicación en `main`: 2026-11-19. El commit
que contiene este registro es un descendiente exclusivamente documental de
`335755b2`; su identidad la informa el artefacto siguiente.

## B. Alcance cerrado

El diseño, el diagnóstico, las mediciones y la revisión correctiva están en el
[documento de mantenimiento](../architecture/geocedg-1.2.0-point-interaction-maintenance.md).
Este registro no los duplica. El alcance aprobado es:

- **Recuperación del gesto de arrastre.** Un rechazo recuperable no termina
  el gesto ni publica nada (secciones 1 y 2, punto 1).
- **Continuación paramétrica local.** Ruta numérica de mejor esfuerzo por
  evaluación semántica, por debajo de las cartas certificadas (sección 2,
  punto 2).
- **Creación sobre loci con métricas anidadas.** El descubrimiento histórico
  de una operación se debita una vez y se reutiliza (sección 2, punto 3).
- **Presentación legible de direcciones semánticas** y de los rechazos, en
  inglés y español (sección 2, puntos 5 y 6).
- **Muestras espurias en extremos excluidos.** Una sola solución donde había
  dos coincidentes (sección 1, defecto D; sección 2, punto 4).
- **`Point on Object` conservada**: es la única herramienta que coloca un
  punto en el interior de un polígono o de una cónica rellena (sección 1,
  defecto E).
- **Correcciones R1–R3** y las mismas muestras en la capacidad de pares y en
  las dos derivadas por diferencias finitas (sección 7).

El mantenimiento no cambia la evaluación, la longitud, la estructura de
existencia, la persistencia ni la serialización de Locus V2.

## C. Evidencia técnica

Cada fila pertenece al commit que indica. La evidencia de un commit no se
presenta como ejecutada sobre otro. Los resultados tipados son archivos
`verification-result.json` locales bajo `artifacts/agent/`, que Git ignora; se
citan como evidencia, no como autoridad. Todos tienen `run_state` y diario
`COMPLETED`, y el commit y el árbol candidatos coinciden con los de la fila.

### Candidato final `335755b2090871b6c36b100de17a1535f96c6016`

| Perfil | Resultado | Ejecución y `result_hash` |
|---|---|---|
| `STATIC` | `ACCEPTED / COMPLETE`, 3/3 obligaciones | `verification-a49f11db58084f8f83244ad330d1afcd` · `7b3596b0db680c516ddf9046476526be3d044fc38a6f5dccdbce6b4f3c2a338e` |
| `PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION-R4` | `ACCEPTED / COMPLETE`, 10/10 obligaciones, 0 diagnósticos | `verification-b9dce2a22bac4f42ab0cbd670d4ec1d0` · `eef30808dc3e5a57d7c8521e5a5da0728adb54b6bddb3d6bf9f270cd029753a9` |
| `PHASE PRE-G9B-CU7-R5` | `ACCEPTED / COMPLETE`, 10/10 obligaciones, 0 diagnósticos | `verification-72d719cede274ad7b88481bc7af6848d` · `43dbd4e61148b46fbe036b9985571d1edcdbbe2d520bcb28f56e6d356e150929` |

Directorios: `artifacts/agent/maint120-point-interaction-335755b2-static`,
`…-335755b2-phase-pre-g9b-cu7-general-point-interaction-r4` y
`…-335755b2-phase-pre-g9b-cu7-r5`. `STATIC` lleva los dos diagnósticos
permanentes del verificador, `diagnostic.governance` (`DIAGNOSTIC_FINDING`) y
`diagnostic.historical-consistency` (`DIAGNOSTIC_UNAVAILABLE`); no afectan a la
aceptación. Los dos `PHASE` compilan el núcleo y Desktop y ejecutan sus
selecciones JUnit compartidas y de Desktop sobre este commit.

### Commits anteriores del mantenimiento

| Commit | Perfil | Resultado | Ejecución |
|---|---|---|---|
| `6d0ea2c17…` | `STATIC` | `ACCEPTED / COMPLETE` | `verification-76d4dc15b8554ce8b7a297144e2bcf48` |
| `afb70e25f…` | `STATIC` | `ACCEPTED / COMPLETE` | `verification-6079ef9d482b44b6a382f97f9dd593bf` |
| `afb70e25f…` | `PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION-R4` | `ACCEPTED / COMPLETE`, 10/10 | `verification-32f2d2eaf1ed49e5a9752a44c84f7380` |
| `afb70e25f…` | `PHASE PRE-G9B-CU7-R5` | `ACCEPTED / COMPLETE`, 10/10 | `verification-9d138b27e1d340a09c18a826de87d197` |
| `afb70e25f…` | `PHASE GEOCEDG-1-2-0` | `ACCEPTED / COMPLETE`, 6/6 | `verification-b8664364420b46a29ebc19c8ceb4f8b1` |
| `daf795dcd…` | `STATIC` | `ACCEPTED / COMPLETE` | `verification-a646745638eb45cbb373d6451d3ef7cf` |
| `daf795dcd…` | `PHASE GEOCEDG-1-2-0` | `ACCEPTED / COMPLETE`, 6/6 | `verification-5ab5dbf55b1d4d89a339a8e04fd37d1e` |
| `daf795dcd…` | `PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION-R4` | `REJECTED_VERIFICATION_CORE / UNTRUSTED`: 8 satisfechas, 1 violada, 1 no fiable | `verification-d43e20532a124cce93d65a717b618bbb` |
| `daf795dcd…` | `PHASE PRE-G9B-CU7-R5` | `REJECTED_VERIFICATION_CORE / UNTRUSTED`: 9 satisfechas, 1 no fiable | `verification-fd37542346f14a35ac1225035c1a748e` |

Los dos rechazos sobre `daf795dcd` son evidencia histórica: precedieron al
commit `afb70e25f`, sobre el que las mismas selecciones fueron aceptadas.
`PHASE GEOCEDG-1-2-0` no se ejecutó sobre `6d0ea2c17` ni sobre `335755b2`. Los
`PHASE` de CU7 no se ejecutaron sobre `6d0ea2c17`.

### Tests focales, regresiones y modelo real

Ejecuciones de desarrollo con Gradle, sin identidad tipada del verificador. Se
hicieron sobre el árbol de trabajo que se confirmó como `6d0ea2c17`; sus
recuentos son los comunicados en esa sesión y sus registros no se conservan en
el repositorio: `REPORTED — NOT RE-AUTHENTICATED`.

- `Maintenance120PointInteractionTest`: 17 de 17. Siete de los nueve tests de
  la revisión correctiva fallan contra `afb70e25f` y pasan con las
  correcciones; los otros dos pasan antes y después (sección 7 del documento
  de mantenimiento).
- Regresión del núcleo Locus: 1911 tests, 0 fallos. Esta ejecución empezó
  antes de la última edición de una línea de la continuación; los 201 tests
  del núcleo directamente afectados se repitieron sobre el código final, con
  0 fallos.
- Tests de Desktop del mantenimiento sobre el modelo real del autor, con
  eventos reales de lienzo: 7 de 7, ninguno omitido.
- Suites de Desktop afectadas: 290 tests, 0 fallos.

Sobre `335755b2` no se repitieron estas ejecuciones de desarrollo. Ese commit
solo cambia el documento de mantenimiento respecto de `6d0ea2c17`, y sus dos
`PHASE` aceptados cubren las selecciones registradas. Las tres clases
`Maintenance120…` no están en el inventario JUnit (sección F).

No se ejecutaron `INTEGRATION`, `FINAL` ni packaging.

## D. Smoke manual del autor

`AUTHOR MANUAL SMOKE = PASS — AUTHOR REPORTED`

- Fecha: 2026-11-19.
- Modelo: `InterCilConoObliqueFinal.cedg`, SHA-256
  `de9d494e2c4883521494dfb4ce8827e2e629e2a2b77972fd5721b41c92c097e8`, sobre
  una copia de trabajo. El original no se modificó.
- El autor confirma que el smoke manual de la aplicación Desktop ha resultado
  satisfactorio.

La aplicación se arrancó con `:desktop:desktop:runGeoCeDG` desde el árbol
limpio en `335755b2`. La consola de ese arranque, conservada fuera del
repositorio, no contiene errores ni excepciones. Se preparó una lista de
comprobación con seis grupos. No hay resultados individuales por grupo,
tiempos ni capturas documentados, y este registro no los atribuye. La
afirmación autoral acredita la aceptación manual. No añade ninguna
certificación matemática.

## E. Promoción ejecutada

- Fast-forward directo de `main` de `4f6a0a9e3ae25702c8c43fef287bf5881e0cb24e`
  a `335755b2090871b6c36b100de17a1535f96c6016`, el 2026-11-19, por
  instrucción expresa del autor que nombraba el commit.
- Sin pull request y sin force push.
- Tras el push, `main` local, `origin/main` y la referencia remota leída con
  `git ls-remote` resolvían a `335755b2090871b6c36b100de17a1535f96c6016`.
- GitHub aceptó el push e informó: «Bypassed rule violations for
  refs/heads/main: Changes must be made through a pull request. Cannot change
  this locked branch». Las reglas de protección no se modificaron.
- Sin cambio de etiqueta, versión, release ni packaging. `v1.2.0` sigue en
  `4f6a0a9e3ae25702c8c43fef287bf5881e0cb24e` y la aplicación sigue declarando
  la versión 1.2.0.

## F. Limitaciones residuales

Aceptadas por el autor como limitaciones o pendientes no bloqueantes. La
aceptación del mantenimiento no resuelve esta deuda técnica.

- **Tiempo inicial de búsqueda sobre `s_2`.** El primer clic tarda alrededor
  de un segundo y cabe en el presupuesto sin cambios con un margen cercano al
  20 %.
- **Evaluación inválida aislada.** `s_2` se evalúa como inválida en el
  parámetro `u = -π/6`. Observada y no investigada; queda pendiente de una
  caracterización independiente.
- **Casos extremos y rutas no investigados en esta ronda.** El extremo
  incluido de un arco abierto de longitud completa se muestrea en
  `inicio + periodo` sin comprobación, y no se encontró ninguna construcción
  real que lo ejercite. Las rutas de carta certificada no se revisaron.
- **Garantía numérica limitada.** La continuación paramétrica es
  `FLOATING_POINT_UNCERTIFIED`: no afirma unicidad, cota de error del
  argumento, estacionariedad ni mínimo de distancia. Un descenso estancado
  conserva el mejor argumento válido alcanzado, por decisión autoral.
- **Inventario JUnit.** Las tres clases de test añadidas no están
  inventariadas. El inventario debe regenerarse, con evidencia ejecutada de
  `final.shared` y `final.desktop`, antes de una verificación futura que lo
  requiera.

## G. Decisión autoral y enmiendas A1–A3

Instrucción: «GeoCeDG — Cierre documental autoral del mantenimiento Locus V2
posterior a v1.2.0», de 2026-11-19. Transcripción de la decisión:

> He revisado el resultado del mantenimiento de interacción con Locus V2 y sus
> evidencias técnicas, he realizado satisfactoriamente el smoke test manual y
> he autorizado su integración en `main`, ya ejecutada.
>
> APRUEBO el cierre del mantenimiento correctivo posterior a GeoCeDG 1.2.0
> como `PASS — AUTHOR APPROVED`.
>
> Esta aprobación comprende las correcciones de creación, arrastre,
> presentación e intersección de puntos sobre Locus V2, incluidas las
> correcciones posteriores R1–R3 y sus ajustes relacionados de intersección y
> derivadas.
>
> APRUEBO expresamente las enmiendas normativas A1, A2 y A3 tal como están
> formuladas en la sección 3 de
> `docs/architecture/geocedg-1.2.0-point-interaction-maintenance.md` en el
> commit `335755b2090871b6c36b100de17a1535f96c6016`.

Alcance aprobado, en los términos del autor:

- **A1 — Recuperación del gesto.** Un rechazo recuperable no termina el
  arrastre ni publica una dirección incompleta; el siguiente evento debe
  establecer de nuevo la evidencia necesaria desde la dirección vigente.
- **A2 — Continuación paramétrica local.** Correspondencia numérica
  interactiva de mejor esfuerzo, por evaluación semántica `F(u)`, sin exigir
  una proyección ortogonal óptima del cursor. Se permite aceptar el mejor
  parámetro válido alcanzado cuando el descenso se estanca, bajo las
  condiciones de admisión y las barreras geométricas documentadas. La garantía
  continúa siendo `FLOATING_POINT_UNCERTIFIED`.
- **A3 — Presupuesto acumulativo de creación.** Reutilización de evaluaciones
  semánticas deterministas de una misma operación, sin reiniciar presupuestos
  ni transferir indebidamente permisos o certificaciones.

La aprobación no autoriza ampliar el algoritmo, relajar las barreras
geométricas, modificar otros contratos ni ejecutar fases posteriores.

Ubicación normativa de la reconciliación:

- El texto aprobado de A1–A3 es la sección 3 del
  [documento de mantenimiento](../architecture/geocedg-1.2.0-point-interaction-maintenance.md),
  cuya sección 8 registra la decisión de cierre.
- La [especificación de interacción con puntos](../../geocedg/specs/locus/locus-v2-point-interaction.md)
  adopta A1–A3 por una enmienda fechada que fija la precedencia.
- El [ADR 0019](../adr/0019-semantic-locus-point-interaction-support.md) y el
  [contrato de continuación local de CU7](../architecture/pre_g9b_cu7_local_continuation_candidate.md)
  llevan una nota fechada que remite a esa enmienda.
- El contrato M08 no se modifica: A3 conserva su contabilidad acumulativa H1
  y solo la completa.

Regla de precedencia: la enmienda aprobada prevalece para las operaciones que
modifica. No invalida los contratos históricos ni extiende otras capacidades.

## H. Límites de este cierre

- El [roadmap](../roadmap/geocedg_roadmap.md) registra la versión publicada
  1.2.0, la integración correctiva posterior y este cierre.
- No se crea ninguna versión nueva, etiqueta de aprobación ni release, y no se
  generan instaladores ni paquetes.
- El repositorio del libro no se modifica y ninguna operación editorial queda
  autorizada por este cierre.
- Ninguna fase futura queda autorizada o promocionada.
