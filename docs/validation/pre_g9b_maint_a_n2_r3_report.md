# PRE-G9B-MAINT-A-N2-R3 — informe único de la corrección focal y del paquete de cierre

**PRE-G9B-MAINT-A-N2-R3 — NORMATIVE DESIGN CANDIDATE — READY FOR AUTHOR ADOPTION**

| Campo | Valor |
|---|---|
| Carácter | Informe de la corrección focal R3. `selfApproved=false`, `authorApproved=false`, `passClaimed=false`. Evidencia, no autoridad; no registra ninguna aprobación |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT-A-N2-R3. Corrección de M1–M3, auditoría normativa y preparación del cierre», del autor, 2026-11-25 |
| Prompt | [`pre-g9b-maint-a-n2-r3-focal-correction.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-n2-r3-focal-correction.prompt.md), commit de gobernanza `39f57ec12` (contrato de prompt validado, `execution_safe = true`) |
| Base | descendiente R2 `ea38a168472b327aaa8796d7098387956e03822a`, árbol `3b5f2f9952f7cfd5bea2e3912a744065b92d45f4`; contenido R2 `101f574ab2929e09ed8295f427c7c67c4f951f06`, árbol `14fa0d1d8fc7c9c7955c87d4c81bc73d350c02fc`; `main` = `origin/main` = remoto = `a9ee803bf` |
| Rama / worktree | `phase/pre-g9b-maint-a-n2-r3` en el worktree reutilizado `GeoCeDG-wt-maint-a-n2` (limpio, sin bloqueo ni otro agente); las ramas A-N2, R1, R2 y la rama de adopción vacía `phase/pre-g9b-maint-a-n2-adoption` quedan intactas |
| Ruta y clase | `ORDINARY`; `DOCUMENTATION_STATUS_ONLY`; `STATIC`; sondas aisladas como evidencia experimental, no `PHASE` ni `INTEGRATION`; sin `FINAL` |

## 1. Candidato R3

Commit de contenido y árbol: se registran en el descendiente documental de este informe (§8),
porque un documento no puede contener su propio SHA.

## 2. M1 — diagnóstico y resolución

**Diagnóstico.** El contrato R2 proponía 3 000 000 de valores con `maxEntryBytes = 17 × maxValues
+ 7 KiB × maxReferences`, justificados por extrapolación desde 1,21 M valores; C-4 no alcanzaba
el presupuesto.

**Resolución.** Se midió directamente el presupuesto (§3). La medición **no confirma** 3 M: el
caso extremo admisible necesita 704–768 MiB para el ciclo completo en un heap de 1 GiB (menos de
1,45 veces de margen) y la asignación de cadenas por referencia añade por sí sola unos 256 MiB.
Conforme a la instrucción R3 §3.3, sin ocultarlo:

- **Fórmula revisada:** `maxEntryBytes = 17 × maxValues + 1 KiB × maxReferences`, con 1 KiB por
  referencia como asignación **agregada** de atributos y cadenas (las referencias ordinarias
  miden unos 690 bytes entre atributos fijos y cadenas ordinarias); cada cadena conserva su límite de 1 024 bytes tras el escape.
- **Presupuesto inicial medido:** 10 000 referencias (igual que el importador), 1 500 000 valores,
  bytes de entrada derivados (unos 34,1 MiB). Los corpus medidos a 1,5 M (R3-T y R3-NS: 100 % de
  `maxValues`, 77 % y 91 % de `maxEntryBytes`) completan el ciclo en 256 MiB, un cuarto de un
  heap de 1 GiB. El peor caso admisible (10 000 referencias en `maxValues` con toda la asignación
  de cadenas usada, 100 % de `maxEntryBytes`, unos 3,4 MB más de cadenas que R3-NS) **no se
  midió**: la afirmación del cuarto de heap vale para los corpus medidos y ese caso es puerta de
  C-4. Alternativa documentada: 2 000 000 valores (256–320 MiB).
- **Protección de lectura reforzada:** lectura incremental desde el flujo inflado, sin almacenar
  la entrada completa, con recuentos declarados comprobados primero y **cada atributo acotado
  mientras se lee**; la medición mostró que un analizador SAX del JDK acumula un atributo entero
  (509 MiB en una bomba de expansión) antes de que salte el contador de bytes.
- **Conservado:** configuración explícita, rechazo seguro sin truncar, `source` y `data`
  obligatorios, capacidad de importación y presupuesto de persistencia como conceptos distintos.
- **Puerta:** C-4 deja de pertenecer a RC-S0 (que no tiene implementación que medir) y pasa a ser
  **puerta de cierre de `MAINT-A-DXF0-RC`**: mide el ciclo completo en el presupuesto sobre la
  implementación, incluido el caso al 100 % de `maxEntryBytes`, con barrido de heap mínimo hasta
  1 GiB y los casos de seguridad del lector; RC no cierra si C-4 no se cumple, y un presupuesto no
  confirmado se revisa sobre esa evidencia.

Documentos: `reference-store.md` §5.2 (cadenas y diagnósticos), §5.3, §7.2, §10, §14 (C-4),
RC-D9; ADR 0044 decisión 8 y consecuencias; A-IMP-9; matriz (incrementos, C-4, RC-21, RC-23,
RC-24); plan operativo y propuesta de ejecución coordinada (C-4 en RC); prompts candidatos RC-S0
y RC; propuesta de adopción R-c.

## 3. Mediciones reales del presupuesto

**Entorno.** Temurin 25.0.4, G1, `-Xmx1g` salvo en los barridos, sin otras opciones; i7-13700F,
32 GiB, Windows 11; clases de producto de `a9ee803bf` (validación, constructor de registro con
ambos resúmenes, teselación del importador); códec y lector del almacén escritos como sonda según
el contrato (no hay implementación). Ciclo medido como lo haría `AppD`: lectura, preflight sin
teselación, captura del documento previo, carga viva con teselación, confirmación; guardado en
temporal, relectura, preflight, renombrado; reapertura con el documento vivo retenido. Por corpus:
1 ciclo en frío, 2 de calentamiento y 5 medidos a 1 GiB; 58 ejecuciones en total. Las sondas se
ejecutaron en el checkout principal sin cambios rastreados (huella de 12 678 ficheros de
compilación idéntica antes y después; `git status --porcelain` vacío; `HEAD` `a9ee803bf`).

**Corpus.**

| Corpus | Referencias | Valores | Entrada | Comprimida |
|---|---|---|---|---|
| R3-T representativo (60 % cúbicas 20–80 puntos, 25 % racionales 9–60, 15 % de 200–600, algunos grados 5–7) | 5 551 | 2 998 060 | 54,8 MB | 21,4 MB |
| R3-A7 adversarial (`p = 7`, 1 024 puntos, pesos explícitos) | 487 | 2 998 946 | 51,3 MB | 25,5 MB |
| R3-N casi en el límite (10 000 referencias, 3 M valores, cadenas de 1 024 bytes) | 10 000 | 3 000 000 | 119,1 MB | 72,4 MB |
| R3-NS (como R3-N con cadenas cortas) | 10 000 | 3 000 000 | 57,9 MB | 21,2 MB |
| control (10 000 típicas) | 10 000 | 1 200 000 | 27,2 MB | 10,8 MB |

**A 1 GiB (lector con búfer).**

| Corpus | Open | Save | Reopen | Heap en Reopen: antes / después de GC |
|---|---|---|---|---|
| R3-T | 2,2 s | 2,5 s | 3,9 s | 716 / 527 MiB |
| R3-N | 2,9 s | 5,0 s | 6,5 s | 971 / 863 MiB (84 % del heap) |

La teselación domina la carga viva (1,0–1,8 s); validación, comprobación de `T` y los dos
resúmenes suman como máximo 50 ms.

**Heap mínimo del ciclo completo** (falla, pasa]:

| `maxValues` | R3-T | R3-A7 | R3-NS | R3-N (cadenas máximas) | control |
|---|---|---|---|---|---|
| 3 M | (448, 512] | (384, 448] | (448, 512] | (704, 768] | (160, 192] |
| 2 M | (256, 320] | — | pasa a 320 | (576, 640] | — |
| 1,5 M | (192, 256] | — | pasa a 256 | (448, 512] | — |

Casi todos los fallos ocurren en la carga viva de Reopen, con almacén anterior, captura, bytes del
archivo y almacén nuevo retenidos a la vez. Con la asignación agregada de cadenas, el caso R3-N ya
no es admisible y el peor caso pasa a ser del tipo R3-NS.

**Lector incremental frente a búfer.** El lector incremental baja un escalón el heap mínimo
(R3-T a (384, 448], R3-N a (576, 640] MiB). Rechazos: un almacén honesto por encima del
presupuesto (3,05 M) se rechaza en el recuento declarado con unos 0,3 MiB (incremental) frente a
229 MiB (búfer); uno que declara menos de lo que contiene se detiene en el contador tras
2 999 986 valores (333–562 MiB asignados en ambos modos); una bomba de expansión de un solo
atributo (139 MB inflados, 136 KB comprimidos) se detiene en la lectura acotada con búfer
(234 MiB) pero el analizador SAX incremental asigna 509 MiB antes de que salte el contador. De ahí
el límite por atributo de la clase 3. No se midió el caso de un atributo enorme dentro del
presupuesto; C-4 lo exige.

**Lo que no se afirma.** Las cifras son de una estación y de una sonda, no del producto; la
ausencia de `OutOfMemoryError` no se toma como margen; el presupuesto es una configuración.

## 4. M2 — manifiesto, referencia excluida y exportación rechazada

`dxf-reference-export.md` §5.2, §7.3 y §10 distinguen ahora tres situaciones con la opción de
referencias activada: **A** — alguna referencia escrita: DXF y manifiesto emparejados; **B** — la
exportación se completa y alguna referencia queda excluida por el usuario, por preferencia o por
la política aprobada (representada, fuera del área, o por el usuario tras
`EXPONENT_NOT_PORTABLE`, causa que nunca excluye por sí sola): DXF y
manifiesto emparejados, el manifiesto registra cada exclusión y su causa aunque no se escriba
ninguna referencia; **C** — una referencia incluida es `NOT_WRITABLE`: la operación se rechaza sin
DXF ni manifiesto, nada parcial se presenta como éxito, la causa va en el resultado y el informe, y
el usuario puede cambiar su selección y repetir. Nunca se sustituye una `SPLINE` por una polilínea.
La salida emparejada aprobada (ADR 0014 decisión 7, contrato DXF §8) admite las tres situaciones
sin una transacción nueva. Unificados: A-EXP-3, A-EXP-4, A-EXP-6 §12.6, ADR 0045 decisión 5,
matriz RX-11, RX-12, RX-19 y propuesta de exportación.

## 5. M3 — exportación solo de referencias

A-GEF-1 sustituye la regla aprobada «The Desktop controller writes no file when the neutral model
contains zero exportable entities» por una excepción explícita: poblaciones distintas; la
exportación ordinaria sin cambios; con la opción elegida, una operación con modelo neutral vacío
es exportable si al menos una referencia queda incluida y escribible tras resolver preferencias,
estados, elecciones del usuario y área; sin entidades ni referencias escribibles no se escribe
archivo; las referencias nunca entran en el modelo neutral ni como `GeoElement`; preflight,
diagnóstico, escritura emparejada y rollback sin cambio. A-GEF-1 añade además la regla de área por
la envolvente del polígono de control. Coherente con el contrato §9, ADR 0045 (consecuencias),
A-EXP-3 y la matriz RX-15 (casos con referencia escribible, sin ninguna y con la opción
desactivada).

## 6. Hallazgos menores corregidos

| Hallazgo | Corrección |
|---|---|
| alcance de X1C | A-EXP-5: X1C se limita a la geometría de construcción; las referencias se rigen por §12 |
| lista de lectura de §14 del contrato de importación | A-IMP-7 la amplía (datos, grado, nudos, pesos efectivos y campos de procedencia que copia el bloque); A-DXF1 §3 alineado |
| cota de pesos por tramo | A-IMP-8 admite `min_k ω_k` por tramo, también válido |
| «declared fit» | nueva A-IMP-11: «declared exact conversion or fit» |
| Classic: «directly open `.cedg`» frente al rechazo por presupuesto | A-NDI-1 recoge la excepción; `R-c` §7.2 precisa que Classic comprueba la lectura acotada y los recuentos, no el contenido |
| cadenas de más de 1 024 bytes y expansión del escape | `R-c` §5.2: límite tras el escape; importación rechazada entera o cambio de nombre rechazado, nunca truncado |
| identificador real | el contrato de exportación nombra la acción `export.dxf-2d` y las banderas existentes `cedg.export.dxf.2d` y `cedg.export.dxf.extended` |
| criterio de 4 ulp | ligado a AutoCAD 2026 (`ACADVER` 25.1); otra versión lo reabre con su evidencia |
| versión del manifiesto | A-EXP-4: la versión 4 solo cuando se escribe con la opción de referencias |
| A-DXF1 | orden de causas de parada explícito; condicionamiento «en aritmética exacta» y AP-11 mide el efecto en coma flotante; fijar las constantes es puerta de cierre de `MAINT-A-DXF1`; regla de escape de cadenas del bloque; la autocomprobación verifica también el bloque |
| estados y enlaces | cabeceras R3 en contratos, ADR, enmiendas, matriz y propuestas |

Ninguno cambia la arquitectura ni debilita una garantía. Dos añaden obligaciones verificables a
`MAINT-A-DXF1` (constantes fijadas como puerta de cierre; relectura del bloque de procedencia en
la autocomprobación), y la gramática de `normal` (`R-c` §5.2 punto 11) completa un atributo que
solo aparecía en el ejemplo: `none` o tres valores binary64, con el valor DXF por defecto
(0, 0, 1) para un grupo ausente; el autor puede revisarla al adoptar.

## 7. Revisión independiente

Dos revisiones independientes, de solo lectura, sobre el árbol de trabajo del candidato, con
el mandato expreso de verificar M1, M2 y M3 contra los documentos y contra la evidencia de §3.

**Primera revisión.** M2 y M3 resueltos. M1 **no** resuelto por tres causas materiales:

1. el límite de 64 bytes por atributo de la clase 3 rompía `diagnostics codes` y
   `unrecognized`;
2. la afirmación del cuarto de heap se hacía para el presupuesto, cuando los corpus medidos solo
   alcanzan el 77 % y el 91 % de `maxEntryBytes`;
3. C-4 en el presupuesto no tenía incremento ejecutable: RC-S0 no tiene implementación que medir.

Hallazgos menores:

- B por `EXPONENT_NOT_PORTABLE` solo tras exclusión del usuario;
- 4 ulp ligado a AutoCAD 2026;
- A-EXP-4 y el filtrado por área;
- el preflight de A-GEF-1 y el caso RX-15 (d);
- RC-21 con `STRING_TOO_LONG`;
- deriva de estados.

Corrección:

- `R-c` §5.2 punto 10 (gramática de diagnósticos) y límites de §5.3 clase 3;
- §5.3 y ADR 0044 limitados a lo medido, con el caso al 100 % como puerta;
- C-4 como puerta de cierre de RC en contrato, matriz, plan, propuesta de ejecución y prompts
  candidatos;
- los menores, salvo uno, registrado en §11 por exigir otra decisión normativa.

**Segunda revisión.** Veredicto: **M1, M2 y M3 resueltos**. Las cifras se recalcularon contra
`char-budget3m.md` con la fórmula nueva (35 740 000 bytes a 1,5 M; R3-T 76,6 %, R3-NS 90,6 %;
diferencia hasta el 100 % de 3,37 MB). Cada límite por atributo cabe en su gramática. Además
señaló:

- **Un hallazgo material nuevo:** C-5 tenía el mismo defecto que C-4, porque exige el cambio de
  los escáneres, que es alcance de RC, y estaba asignado a RC-S0. Corregido: C-5 se divide en el
  comportamiento actual en RC-S0 y la puerta de cierre en el presupuesto en RC, en el contrato
  §14, la matriz, el plan, la propuesta de ejecución y ambos prompts candidatos.
- **Nueve menores, todos corregidos:**
  - 4 ulp ligado a AutoCAD 2026 en RX-D18, la propuesta de exportación, el plan y la propuesta
    de ejecución;
  - RX-11 y §4 de este informe: exclusión por el usuario;
  - RX-D12 alineado con A/B/C;
  - título de la sección 1 de la matriz;
  - A-DXF1 R3 descrito como redacción más dos obligaciones añadidas;
  - los 690 bytes incluyen las cadenas ordinarias;
  - el contador de bytes produce `STORE_BUDGET_EXCEEDED` y un atributo fuera de su límite
    produce `MALFORMED_STORE`;
  - gramática de `normal` (§5.2 punto 11).

Tras estas correcciones no queda ningún hallazgo material abierto. Las correcciones de la segunda
revisión no fueron revisadas por un tercer auditor; se comprobaron con las verificaciones
documentales de §8.

## 8. Identidad y resultado de `STATIC`

Se registran en el descendiente documental del commit de contenido R3.

## 9. Contratos y ADR listos para aprobación

| Documento | Revisión | Ruta |
|---|---|---|
| `cedg-reference-store/v1` | R3 | [`reference-store.md`](../../geocedg/specs/import/reference-store.md) |
| ADR 0044 | R3 | [`0044-document-store-of-imported-references.md`](../adr/0044-document-store-of-imported-references.md) |
| `cedg-reference-export/v1` | R3 | [`dxf-reference-export.md`](../../geocedg/specs/export/dxf-reference-export.md) |
| ADR 0045 | R3 | [`0045-separate-dxf-export-of-imported-references.md`](../adr/0045-separate-dxf-export-of-imported-references.md) |
| `dxf-spline-approximation/v1` | R3 (redacción respecto de R1, más dos obligaciones añadidas de `MAINT-A-DXF1`: constantes como puerta de cierre y relectura del bloque en la autocomprobación) | [`dxf-spline-approximation.md`](../../geocedg/specs/import/dxf-spline-approximation.md) |

## 10. Enmiendas listas para adopción

[Conjunto de enmiendas R3](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md):

- grupo R (con `R-c`): A-IMP-1, A-IMP-2, A-IMP-3, A-IMP-9, A-IMP-10, A-NDI-1, A-ADR-3;
- grupo X (con la exportación): A-IMP-4, A-IMP-5, A-IMP-6, A-BS-1, A-EXP-1 a A-EXP-6, A-GEF-1,
  A-ADR-1, A-ADR-2;
- grupo A (con A-DXF1): A-IMP-7, A-IMP-8, A-IMP-11, A-BS-2.

## 11. Decisiones residuales

Ninguna bloqueante. El autor puede, al adoptar `R-c`, preferir el valor alternativo de 2 000 000
valores (RC-D9). Siguen pendientes de validación, como puertas de sus incrementos, la nueva
medición C-4 sobre la implementación (incluido el caso al 100 % de `maxEntryBytes`), C-5 en el
presupuesto y las constantes de A-DXF1 (AX-D6).

**Registrado y no aplicado (necesita decisión del autor).** La primera auditoría R3 propuso
registrar el diagnóstico `EXPONENT_NOT_PORTABLE` en el manifiesto también para una referencia
excluida por otra causa (preferencia, área, representada) que contenga un valor no portable. RX-D19
solo fija que la exclusión por el usuario conserva el diagnóstico; extenderlo a otras exclusiones
obligaría a comprobar el exponente de referencias que no se escriben, lo que es una decisión
normativa distinta. Conforme a la instrucción R3, se registra aquí y no se cambia el contrato; no
bloquea la adopción.

## 12. Paquete preparado para el cierre y la implementación coordinada

### 12.1 Registros de adopción (borradores, sin aprobación registrada)

Cada uno se escribirá como registro autoral solo tras la decisión expresa del autor sobre el
candidato R3 exacto, con este contenido:

| Registro | Contenido |
|---|---|
| `R-c` y ADR 0044 | contrato `cedg-reference-store/v1` R3 y ADR 0044 R3 con SHA y árbol del candidato; decisiones D-R1-1 a D-R1-5, D-R1-8, D-R1-10 y RC-D1 a RC-D15; enmiendas del grupo R; presupuesto medido y puerta C-4; dependencias: ninguna; habilita `MAINT-A-DXF0-RC-S0` y `MAINT-A-DXF0-RC` con autorización aparte |
| exportación y ADR 0045 | contrato `cedg-reference-export/v1` R3 y ADR 0045 R3; decisiones D-R1-5 a D-R1-7, D-R1-10, RX-D18, RX-D19 y RX-D1 a RX-D17; situaciones A, B y C; exportación solo de referencias; enmiendas del grupo X; dependencia: `R-c` adoptado; habilita `MAINT-A-DXF0-EXP` |
| A-DXF1 | contrato `dxf-spline-approximation/v1` R3; decisiones D-R1-5, D-R1-9, D-R1-10 y AX-D1 a AX-D14; enmiendas del grupo A; constantes pendientes de medición; dependencia: `R-c` adoptado; habilita `MAINT-A-DXF1-S0` y `MAINT-A-DXF1`; `MAINT-A-DXF1b` aparte |

Las propuestas detalladas son las de [R-c](pre_g9b_maint_a_n2_r1_adoption_proposal_rc.md),
[exportación](pre_g9b_maint_a_n2_r1_adoption_proposal_export.md) y
[A-DXF1](pre_g9b_maint_a_n2_r1_adoption_proposal_a_dxf1.md), actualizadas a R3.

### 12.2 Actualizaciones normativas que aplicará el cierre

Sobre la rama del cierre, desde el candidato R3 adoptado, en un único commit documental: estados
de los tres contratos y de ADR 0044 y 0045 a aprobados con enlace a sus registros; aplicación
literal de las enmiendas de los tres grupos a `dxf-spline-reference.md`, `bspline-2d.md`,
`dxf-curve-fidelity-and-approximation.md`, `geometry-export-foundation.md`,
`native-document-identity.md` y a las líneas de estado de ADR 0014 y ADR 0043, como secciones o
reemplazos fechados que conservan la historia de aprobación; registro aditivo del roadmap (§12.3);
plan operativo marcado como planificación aprobada; prompts candidatos con sus dependencias
marcadas como satisfechas, sin autorizar nada. Después: `STATIC` sobre el commit exacto y, si
procede, un descendiente documental clasificado por el procedimiento canónico.

### 12.3 Registro del roadmap (texto preparado)

> **2026-11-25 — `PRE-G9B-MAINT-A-N2`: adopción normativa de `R-c`, exportación DXF de
> referencias y A-DXF1.** Contratos `cedg-reference-store/v1`, `cedg-reference-export/v1` y
> `dxf-spline-approximation/v1` (revisión R3), ADR 0044 y 0045 aceptados, enmiendas de
> `dxf-spline-reference.md`, `bspline-2d.md`, `dxf-curve-fidelity-and-approximation.md`,
> `geometry-export-foundation.md`, `native-document-identity.md`, ADR 0014 y ADR 0043 aplicadas.
> Incrementos habilitados normativamente, no autorizados: `MAINT-A-DXF0-RC-S0`,
> `MAINT-A-DXF0-RC`, `MAINT-A-DXF0-EXP`, `MAINT-A-DXF1-S0`, `MAINT-A-DXF1`; `MAINT-A-DXF1b`,
> `MAINT-A3` y `MAINT-A-R` siguen sin autorizar. Deudas separadas DT-1 y DT-2 registradas. G9B
> sigue `DESIGNED — NOT AUTHORIZED`.

### 12.4 Propuesta de publicación documental

Tras el cierre y su `STATIC`, la publicación sería un avance rápido sin forzar de `main` desde
`a9ee803bf` al commit de cierre exacto (o a su descendiente documental), conforme a AGENTS §8.1,
con una instrucción autoral que nombre ese SHA. Delta previsto respecto de `main`: solo
documentación, especificaciones, ADR y prompts de tarea; ningún código, recurso, prueba,
verificador ni empaquetado.

### 12.5 Implementación coordinada

Los cinco [prompts candidatos](../architecture/pre_g9b_maint_a_n2_r1_candidate_prompts/) y la
[propuesta de ejecución coordinada](../architecture/pre_g9b_maint_a_n2_r1_coordinated_execution_proposal.md)
siguen preparados (hitos RC-M y DXF1-M, puntos de sincronización P1 a P6, integración RC → EXP →
DXF1). M1 sí exigió cambiar dos: el prompt candidato de RC-S0 ya no incluye C-4 y el de RC lo
incluye como puerta de cierre; el plan operativo (estado R3, filas 1 y 2) y la propuesta de
ejecución (P1) se ajustaron igual. M2 y M3 no exigen cambiarlos. Ningún
incremento se ejecuta ni se crea su worktree.

## Evidencia

Sonda y salidas de R3 en `artifacts/agent/pre-g9b-maint-a-n2-r3-characterization/budget3m/` del
worktree (ignorado): fuente de la sonda, scripts de lote, `steps.tsv`, `gc.log`, `log.txt` y
`command.txt` por ejecución, y `char-budget3m.md`; 7 887 ficheros con `SHA256SUMS.txt` (SHA-256
del manifiesto `e137d332442dda71a8645210818a7fb436c15072a0aa34ed11a76cfc6c638f6f`).
