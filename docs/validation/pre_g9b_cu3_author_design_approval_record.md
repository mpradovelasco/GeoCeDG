# PRE-G9B-CU3 — Registro de la aprobación autoral del diseño y de la implementación por incrementos

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad del estado de aprobación del diseño de `CU3` y de la autorización de su implementación. No aprueba ninguna implementación todavía no ejecutada y no declara `CU3` `PASS — AUTHOR APPROVED` |
| Instrucción | «PRE-G9B-CU3 — Aprobación autoral del diseño e implementación por incrementos», decisión del autor de 24 de octubre de 2026: **DISEÑO APROBADO — IMPLEMENTACIÓN AUTORIZADA CON PUERTAS BLOQUEANTES** |
| Repositorio | `mpradovelasco/GeoCeDG` |
| Base autorizada | `80d72a50557a82a7a9d501a26252d1758ac6d94d`, árbol `7625ec35dbced93dd56a11ff775ce7a0f91ce62d` (rama publicada `phase/pre-g9b-cu3-preparation`) |
| `main` | `e6c908f2203cd8feb5d4938013e16b212e0fe016`, cierre de `CU2`; `CU0`, `CU1` y `CU2` siguen `PASS — AUTHOR APPROVED` y sus contratos no se reabren |
| Rama de implementación | `phase/pre-g9b-cu3-implementation`, local, creada desde el SHA exacto de la base |
| Expediente aprobado | [informe del candidato de preparación](pre_g9b_cu3_preparation_candidate_report.md), con la revisión científica focal (§11) y la comprobación final de coherencia (§12); [hoja de decisiones](pre_g9b_cu3_author_decision_sheet.md) |
| Prompt canónico | [`pre-g9b-cu3-structural-points-and-certified-contact-evidence.prompt.md`](../../.github/prompts/tasks/pre-g9b-cu3-structural-points-and-certified-contact-evidence.prompt.md), creado en el paso T0 |
| `selfApproved` | `false` |

El autor revisó el expediente de la preparación y sus dos revisiones científicas focales, y aprobó
el diseño conforme a las recomendaciones consolidadas de D-CU3-1 a D-CU3-19, con las condiciones
expresas de su instrucción. Este registro las transcribe y las ordena; ante cualquier diferencia
prevalece la instrucción autoral.

## 1. Identidad y puerta de entrada

| Comprobación | Resultado |
|---|---|
| SHA, árbol y ascendencia | `80d72a50` existe; su árbol es `7625ec35…`; desciende de `cbd31633`, de `213ea5aa` y de `main` `e6c908f2` |
| Rama publicada | `phase/pre-g9b-cu3-preparation` apunta en `origin` a `80d72a50` |
| Espacio de trabajo | limpio |
| `main` | local, `origin/main` y el remoto vivo en `e6c908f2`, sin divergencia; 73 referencias de etiqueta remotas (37 etiquetas), sin cambios |
| Autoridad leída | `AGENTS.md`, prompts canónicos de gobierno y verificación, hoja de ruta, prompt y registros de `CU2` como precedente |
| Selección de entrada `PRE-G9B-CU0` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0` sobre la base: `verification-8777fbb4f55a4c648524054859bc6cf1`, resultado `5c0be2840ea77757e59ae181e7918571af0947c1adaf827f67605e450deefa27`, `ACCEPTED / COMPLETE` 2/2, re-autenticado |
| Selección de entrada `PRE-G9B-CU1` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1` sobre la base: `verification-2b29650199564c56bfebd750927c8f79`, resultado `fa5d4687a60a67ae248bd6cf03215fe3128299a284226f5c7972701c917e174f`, `ACCEPTED / COMPLETE` 4/4, re-autenticado |
| Selección de entrada `PRE-G9B-CU2` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2` sobre la base: `verification-86c971d50e3b476dbc708ee427afe571`, resultado `88d0cc811a17f9345de2e9c962e5245cbd4034bda8318ae9d5a744805fab3e37`, `ACCEPTED / COMPLETE` 4/4, re-autenticado |

Las tres selecciones se ejecutaron sobre la base exacta, en la rama de implementación todavía sin
cambios, para disponer de una referencia de regresión atribuible.

## 2. Aprobación del diseño y de la normativa

1. **ADR 0039** queda `ACCEPTED — DESIGN AUTHOR APPROVED` como decisión arquitectónica, con las
   precisiones de esta decisión. Su implementación y el texto incorporado a las especificaciones
   no quedan aprobados como producto hasta la revisión del candidato técnico congelado.
2. **Enmiendas A3.0 a A3.5**, aprobadas en la dirección científica documentada e incorporadas en
   T0 sin modificar los contratos históricos: cada especificación recibe una sección nueva de
   enmienda, y sus secciones aprobadas anteriores no se reescriben.

| Especificación | Versión | Sección | Enmiendas |
|---|---|---|---|
| `locus-v2-intersections.md` | 1.3 | «PRE-G9B-CU3 amendment (version 1.3)» | A3.0, A3.1-R, A3.2-R y A3.4-R, y la autoridad del objetivo |
| `locus-v2-certified-construction-model.md` | 1.3 | §11 | A3.5, con la autoridad del objetivo (§11.3) y las condiciones de conformidad de M3 (§11.4) |
| `locus-v2-public-surface.md` | — | §21 | A3.3-R (la propuesta citaba §20, ya ocupado) |
| `locus-v2-point-interaction.md` | — | §18 | A3.3-R, familia de punto |
| `locus-v2-existence-components.md` | — | §16 | A3.3-R, nota de consumidor |
| `g9-spatial-projection-semantics.md` | — | §8.3 | A3.4-R, participantes reservados |

3. **ADR anteriores.** Los ADR 0008, 0010 y 0017 reciben solo una línea de cabecera «Amended by»
   que remite a la enmienda acotada del ADR 0039; su texto de decisión no cambia.
4. **Lo que la aprobación no certifica.** La aprobación de la arquitectura no certifica por
   anticipado la viabilidad de M3 ni el cumplimiento de sus presupuestos. Ambos los decide la puerta
   G-M3 (§4).

## 3. Decisiones D-CU3-1 a D-CU3-19

Todas se aprueban conforme a su recomendación consolidada de la
[hoja de decisiones](pre_g9b_cu3_author_decision_sheet.md) §7, con estas condiciones expresas:

| Decisión | Disposición autoral |
|---|---|
| D-CU3-1 | APPROVED, opción A: `Point(L, D)` e `Intersect(L, r, D)`; `D` nunca es un punto inicial |
| D-CU3-2 | APPROVED: tabla cerrada y versionada de procedencias, con la selección exacta heredada |
| D-CU3-3 | APPROVED: autoridad geométrica de `L` en su configuración capturada `Σ_L`; `Intersect(L, r, D)` materializa el mismo punto que `Point(L, D)`; correspondencia exacta o encerrada, nunca desde las coordenadas binary64 |
| D-CU3-4 | APPROVED: M1 ∪ M3 con M3 en `Σ_L⁺` y `Σ_B` solo diagnóstico, **con la comprobación adicional de autoridad del objetivo de §4** |
| D-CU3-5 | APPROVED, opción A: solo las clases 1 y 2 se materializan como intersecciones; la clase 3 nunca |
| D-CU3-6 | APPROVED: G8B-R1 conservada, grados de evidencia, política de consumo y vía certificada aditiva (H02) |
| D-CU3-7 | APPROVED, alternativa (a), en su alcance documentado (§6) |
| D-CU3-8 | APPROVED, opción A: ejes como referencias reservadas de identidad estable |
| D-CU3-9 | APPROVED, opción (a): sin materialización en componentes puntuales; C22, C23 y C40 excluidos |
| D-CU3-10 | APPROVED: reformulación del criterio de la costura; la inversión de su pin depende de la evidencia que dé M3 bajo la autoridad del objetivo |
| D-CU3-11 | APPROVED, dentro de `CU3-B` |
| D-CU3-12 | APPROVED: fallo cerrado de los lectores anteriores, con nota de migración |
| D-CU3-13 | APPROVED en su versión final (§5) |
| D-CU3-14 | APPROVED: `INTEGRATED_PHASE`; base exacta `80d72a50` |
| D-CU3-15 | APPROVED: prompt canónico creado en T0 |
| D-CU3-16 | APPROVED: H-1 sigue pospuesta |
| D-CU3-17 | APPROVED: ninguna publicación en `main`, etiquetas, releases ni paquetes de distribución |
| D-CU3-18 | APPROVED: precisión de los pliegues |
| D-CU3-19 | APPROVED: `CU3-A` autorizado; `CU3-B` autorizado condicionalmente y no ejecutable hasta superar G-A |

## 4. Puerta temprana G-M3 y condiciones de conformidad de M3

G-M3 es **obligatoria, temprana y bloqueante**. Se mantienen sus criterios existentes:

- viabilidad matemática (G-M3-1);
- presupuesto (G-M3-2);
- correspondencia con el algoritmo vivo (G-M3-3);
- reconstrucción exacta de la selección;
- coherencia entre configuraciones.

Se añade **G-M3-4, autoridad del objetivo geométrico**. Para cada incidencia que M3 pretenda
demostrar se identifican dos objetos:

1. el punto exacto del Locus V2 en la configuración autoritativa `Σ_L`;
2. el objeto objetivo autoritativo conforme al contrato vigente de intersecciones Locus V2
   (`locus-v2-intersections.md` §2.1).

Se comprueba que la semántica exacta asignada al objetivo en `Σ_L⁺` coincide con su autoridad
geométrica aprobada:

- **rectas:** equivalencia exacta de sus coeficientes homogéneos, admitiendo únicamente factores
  escalares no nulos demostrados;
- **segmentos y rayos:** además, las condiciones de pertenencia a la parte limitada;
- **circunferencias:** se preserva la representación geométrica autoritativa, y se distinguen
  expresamente la matriz capturada, los semiejes y las evaluaciones numéricas;
- **objetivos reconstruidos desde un subgrafo:** se demuestra su correspondencia con el objeto
  objetivo existente, sin sustituir automáticamente sus coeficientes binary64 por los de una
  reconstrucción ideal;
- se comprueban los casos obligatorios, los controles adversarios y las situaciones en que el
  redondeo rompe una relación constructiva exacta.

La evaluación en `Σ_B` nunca sustituye esta comprobación. Si la semántica exacta reconstruida del
objetivo y su autoridad geométrica vigente no coinciden, el resultado falla cerrado, salvo que
exista una correspondencia exacta demostrada y compatible con la normativa aprobada. Cualquier
necesidad de modificar la autoridad geométrica del objetivo, el convenio de captura de `CU2` o un
contrato previo es una condición de parada y de decisión autoral. La semántica del objetivo no se
redefine nunca para que los contactos resulten incidentes.

**Condiciones adicionales de G-M3:**

- comprobar las hipótesis aritméticas del análisis §5.8, incluida `z = 1`;
- ejecutar V-CFG-1 a V-CFG-6 y los controles V-M3 correspondientes;
- demostrar que las ramas exactas elegidas reproducen la selección heredada o fallan con causa
  tipada;
- medir el coste con contadores deterministas;
- proponer un presupuesto versionado técnicamente justificado, con su evidencia conservada.

Si el presupuesto necesita aprobación autoral conforme al contrato, el resultado de G-M3 y el
techo propuesto se presentan juntos para una única decisión. El agente nunca declara aprobada una
cota normativa. Si G-M3 no supera sus condiciones, se detienen las partes dependientes y se informa
de las alternativas R-1, R-2, R-3 y C3; ninguna queda autorizada automáticamente.

## 5. Regla de conflicto de evidencias

Se adopta D-CU3-13 en su versión final. Una solución verificada localmente solo puede retirarse o
reclasificarse cuando un certificado riguroso de la misma revisión contradice realmente su
existencia, su multiplicidad o su clasificación. Se exige:

- continuidad certificada en el dominio del certificado;
- conteo y localización suficientes de las raíces;
- tratamiento correcto de extremos, costuras y discontinuidades;
- identidad y configuración geométrica coherentes;
- ninguna inferencia de contradicción a partir de un punto crítico aislado o de una clase 3.

La completitud global sigue siendo independiente de la admisibilidad local. Las capacidades
aprobadas de G8B-R1 se conservan fuera de las contradicciones establecidas rigurosamente.

## 6. Compatibilidad e identidad

Se adopta D-CU3-7, alternativa (a), en su alcance documentado. Las soluciones contradichas por
evidencia certificada nueva pueden perder su admisibilidad, pero nunca se reasocian en silencio. Se
preservan:

- las identidades durables;
- los tokens transversales existentes cuando su significado sigue siendo válido;
- formatos versionados nuevos para las capacidades nuevas;
- las dependencias del DAG;
- Deshacer, Rehacer, guardar y reabrir, y la copia, con sus reglas de identidad;
- el fallo cerrado y el diagnóstico tipado ante incompatibilidades.

Las coordenadas, las tolerancias, la proximidad, los índices y las etiquetas no se usan nunca como
identidad geométrica.

## 7. Implementación autorizada

**`CU3-A`, incremento mínimo** (I-1 a I-6 de la estrategia):

- envolventes rigurosas del residuo y de sus derivadas;
- grados explícitos de evidencia y conservación de la admisibilidad G8B-R1;
- vía certificada adicional de aceptación para H02;
- regla de conflicto sustentada exclusivamente en certificados rigurosos de conteo;
- taxonomía de contactos, incluidos los pliegues geométricos;
- evaluación exacta M3;
- `Point(L, D)` e `Intersect(L, r, D)`;
- ejes como referencias reservadas de identidad estable;
- compatibilidad y diagnósticos de fallo cerrado.

`CU3-A` debe resolver los tres casos obligatorios, el contacto de la costura, H02, H06 y H-CU3-1 a
H-CU3-4, dentro de las garantías aprobadas.

**`CU3-B`, materialización sin conductor** (I-7), autorizada condicionalmente y **no ejecutable
hasta superar G-A**:

- materialización sin conductor de contactos con existencia establecida;
- germen tangente versionado;
- identidad y ciclo de vida;
- tratamiento de las parejas de preimágenes de un pliegue certificado.

Los contactos de clase 3 no se materializan como intersecciones. Si G-B falla, `CU3-B` se detiene y
`CU3-A` se presenta como candidato parcial verificado, sin declarar aprobado el conjunto de `CU3`.

## 8. Validación y cierre técnico

- Se ejecuta la estrategia T0–T13 con las puertas G-M3 (incluida G-M3-4), G-A, G-B y G-final
  (`INTEGRATED_PHASE`).
- Las puertas ejercitan las propiedades esenciales con aserciones sustantivas y pruebas
  adversarias.
- El candidato se congela antes de la verificación final, y las selecciones oficiales se ejecutan
  sobre el mismo commit y el mismo árbol.
- Se mantienen las regresiones de `CU0`, `CU1`, `CU2`, G8 y G9U0, y las referencias `preserve`
  según sus contratos. Un cambio de `pins` solo se acepta cuando una prueba demuestra la corrección
  del defecto correspondiente.
- Se registran por separado: resultados certificados; verificaciones numéricas bajo hipótesis;
  aproximaciones de representación; limitaciones y degeneraciones; resultados no establecidos;
  coste computacional y presupuestos.
- Un PASS automatizado no sustituye la aprobación ni la comprobación manual del autor.

## 9. Autonomía y condiciones de parada

El autor autoriza la ejecución autónoma de los pasos previstos, sin consultas intermedias para
decisiones menores de implementación, organización, pruebas o documentación. El agente puede
corregir defectos locales dentro de los contratos aprobados, con evidencia adversaria y registro
del cambio. Solo se detiene ante una condición de parada real:

- fallo de G-M3;
- incompatibilidad normativa o de identidad;
- necesidad de cambiar contratos previamente aprobados;
- presupuesto que requiera una aprobación autoral nueva;
- imposibilidad de satisfacer las garantías obligatorias.

Un fallo de certificación nunca se convierte en una aproximación silenciosa.

## 10. Exclusiones

No se autoriza:

- la materialización pública de puntos críticos sin incidencia demostrada;
- la materialización en componentes puntuales; C22, C23 y C40;
- cambios en la semántica de pares de loci fuera de lo aprobado;
- la enmienda C3 del convenio de captura de `CU2`;
- tolerancias dependientes de la presentación;
- nuevas fases `CU4`–`CU10` o `G9B`;
- cambios en `geocedg_book`;
- la publicación en `main`, etiquetas, releases o paquetes de distribución.

El registro E1 conserva los resultados científicos y sus límites para `CU10`, en especial la
distinción entre integridad geométrica, precisión numérica y representación.

## 11. Entrega

Un candidato técnico congelado y un único paquete de revisión con:

- identidad Git exacta;
- decisiones ejecutadas;
- resultados de G-M3, G-A, G-B y G-final;
- evidencia de las pruebas y de los bancos;
- compatibilidad y migración;
- limitaciones y trabajo futuro;
- estado del libro y del repositorio;
- comprobaciones manuales pendientes de ejecución por el autor.

`CU3` no se declara `PASS — AUTHOR APPROVED` sin una decisión autoral posterior.

## 12. Registro del paso T0

El paso T0 registra esta decisión, incorpora las enmiendas aprobadas (§2), crea el prompt canónico,
actualiza el estado de los documentos de la preparación, la estrategia y la matriz (con G-M3-4 y
las filas que dependen de la autoridad del objetivo), el plan consolidado, la hoja de ruta y el
registro E1, sin código productivo.
