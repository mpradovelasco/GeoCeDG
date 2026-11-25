# PRE-G9B-MAINT-A-N2-R1 — informe consolidado

**PRE-G9B-MAINT-A-N2-R1 — NORMATIVE DESIGN CANDIDATE — READY FOR AUTHOR ADOPTION**

| Campo | Valor |
|---|---|
| Carácter | Informe del refinamiento normativo. `selfApproved=false`, `authorApproved=false`, `passClaimed=false`. Evidencia, no autoridad; no registra ninguna aprobación |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT-A-N2-R1. Refinamiento normativo, resolución de decisiones y preparación de implementaciones coordinadas», del autor, 2026-11-25 |
| Prompt | [`pre-g9b-maint-a-n2-r1-normative-refinement.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-n2-r1-normative-refinement.prompt.md), commit de gobernanza `51d8500b5` (contrato de prompt validado: siete campos en orden, `execution_safe = true`) |
| Ruta y clase | `CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY`; `REQUIRED_VERIFICATION = STATIC`; sin `PHASE`, `INTEGRATION` ni `FINAL` |
| Rama / worktree | `phase/pre-g9b-maint-a-n2-r1`, creada desde `e7803badb`, en el worktree reutilizado `GeoCeDG-wt-maint-a-n2` (limpio y libre al empezar) |

Convención: **[hecho]** comprobado en código o Git; **[medida]** resultado experimental de una
sonda; **[política]** regla de diseño propuesta; **[aprobado]** norma vigente; **[pendiente]**
decisión que queda para el autor.

## A. Estado de A-N2 original y de R1

- **[hecho]** Base publicada `a9ee803bf4be92c58ab7e120111ebacc70055487`, árbol
  `92f8ab0674587f3bc249ae7964e0eddb18b6a35c`; `main`, `origin/main` y el remoto coincidían.
- **[hecho]** A-N2: contenido `874286e95564234d27094c9c983e69f9bff160bf` (árbol
  `30c21042c4b18a1416374a1ea0cfd9a4802cf6fa`), descendiente documental
  `e7803badb3344c8d26af36c82a5b05a20eb5f7e6` (árbol `2c9d8f8d814052a904fc9f11bac8a8d9f4316d37`),
  rama `phase/pre-g9b-maint-a-n2`, sin cambios: su historia no se reescribe.
- R1: commit de gobernanza `51d8500b5`; commit de contenido y descendiente en §J.
- Documentos de A-N2 revisados en R1: los tres contratos, ADR 0044 y 0045, enmiendas, matriz,
  plan operativo; los dos informes de A-N2 reciben solo una nota que remite a este informe.
- Documentos nuevos: este informe; tres [propuestas de adopción](#l-paquete-de-adopción-normativa);
  cinco [prompts candidatos](../architecture/pre_g9b_maint_a_n2_r1_candidate_prompts/); la
  [propuesta de ejecución coordinada](../architecture/pre_g9b_maint_a_n2_r1_coordinated_execution_proposal.md).

## B. Decisiones del autor aplicadas y trazabilidad

| Decisión | Dónde queda aplicada |
|---|---|
| D-R1-1 sin geometría de referencia en `Construction`, DAG ni identidad CeDG | `R-c` §0; A-DXF1 §0; exportación §0; ADR 0044 d. 1 |
| D-R1-2 sin compatibilidad con lectores antiguos; integridad y sin pérdida silenciosa | `R-c` §11; ADR 0044 d. 6; A-NDI-1 |
| D-R1-3 `source` y `data` persistidos; `data` nunca recalculado en su lugar | `R-c` §5.2 punto 3, RC-D12; ADR 0044 d. 3 |
| D-R1-4 sin límite fijo arbitrario; política configurable y medida | `R-c` §5.3, RC-D9, RC-D15; ADR 0044 d. 8; A-IMP-9; RC-23, RC-24 |
| D-R1-5 sin historial persistente de aproximaciones fallidas | A-DXF1 §9, §13.2, AX-D14; `R-c` §0, §7, RC-D10; exportación §6.2, §6.5 caso 6, RX-D17; A-IMP-7; AP-8, AP-10, RX-18, SEP-7 |
| D-R1-6 estado 4: alternativa A | exportación §6.4, §6.5, §11, RX-D2; ADR 0045 d. 4; A-EXP-6; RX-8 |
| D-R1-7 AutoCAD Core Console y ezdxf | exportación §12.1, §12.2, RX-D16; ADR 0045 d. 6; A-EXP-6 (12.8); RX-16, RX-17 |
| D-R1-8 Classic: conservación opaca o barrera; nunca pérdida silenciosa | `R-c` §7.2, RC-D4; ADR 0044 d. 9; A-NDI-1; C-3, RC-14 |
| D-R1-9 cota certificada separada en `MAINT-A-DXF1b` | A-DXF1 §7.2, AX-D2; plan; prompt candidato de A-DXF1 |
| D-R1-10 sin bandera por novedad; madurez según AGENTS §5 al cierre | A-DXF1 §14, AX-D10; `R-c` §12, RC-D14; exportación §11; ADR 0045 d. 7. Conciliación propuesta con AGENTS §5: cada capacidad existe solo en su rama hasta el cierre autoral, que declara su estado formal; sin estado intermedio en el producto |

## C. Política de almacenamiento y mediciones de capacidad

**[hecho]** El tope de 4 MiB no lo imponen Abrir ni Guardar: es una constante privada de dos
escáneres pasivos (inspección experta y comprobación de modo angular de Insertar archivo), que
además fijan 8 MiB de archivo y 32 entradas, y que hoy rechazan **cualquier** almacén porque
tratan toda entrada desconocida como hallazgo. Ningún lector tiene protección contra
expansión ZIP; las entradas conocidas se cargan sin tope. La aplicación empaquetada no fija
`-Xmx` (un cuarto de la RAM; 1 GiB con 4 GiB). Abrir lee el almacén dos veces (preflight y carga
viva) y captura el documento previo en memoria; Guardar escribe, relee y verifica.

**[medida]** JDK 25.0.4 con `-Xmx1g` (contraste en JDK 17 con cobertura: ±20 %), i7-13700F, 32
GiB; medianas de 3 a 5 ejecuciones tras calentamiento; sin `OutOfMemoryError`:

| Escenario | Entrada hex | Deflate | Análisis y decodificación | Validaciones, `T`, resúmenes | Teselación |
|---|---|---|---|---|---|
| 100 típicas | 0,27 MB | 0,11 MB | 3,6 ms | 0,5 ms | 2,8 ms |
| 1 000 típicas | 2,7 MB | 1,1 MB | 13 ms | 2,6 ms | 25 ms |
| 10 000 típicas | 27,2 MB | 10,8 MB | 106 ms | 24 ms | 274 ms |
| 1 / 10 / 40 / 100 máximas (`p = 7`, 1 024 puntos) | 0,105 / 1,05 / 4,21 / 10,5 MB | ≈ 50 % | 1,2 / 8 / 20 / 45 ms | ≤ 11 ms | 4,6 / 40 / 162 / 371 ms |

A 10 000 típicas una lectura verificada cuesta unos 0,5 s (Abrir +1 s, Guardar +1,3 s con la
deflación de 27 MB), unos 45 MB transitorios y unos 50 MB retenidos. La teselación es el 55–80 %
del coste. Base64 o una entrada binaria solo reducen el archivo comprimido un 7–17 %. Importar
10 000 splines típicas tarda 0,65 s.

**[política]** `R-c` §5.3: cinco clases de límite (admisión geométrica, recursos por operación,
seguridad del lector siempre activa, presupuesto documental configurable, plataforma).
Presupuesto inicial derivado de la memoria medida (unos 80 bytes por valor almacenado entre
datos, teselación y búferes): 10 000 referencias (nunca menos que el importador), unos
3 000 000 valores (un cuarto de un heap de 1 GiB) y bytes de entrada derivados de ambos;
propuestos y confirmados por C-4. El caso extremo del importador (10 000 referencias máximas,
unos 61 M valores) exige ampliar el presupuesto y la memoria de forma explícita; ampliable solo por una acción administrativa explícita con valores finitos; documento
por encima del presupuesto → `STORE_BUDGET_EXCEEDED` con los valores requeridos, sin truncar ni
alterar el archivo. Seguridad: inflado acotado con contador, entrada única, analizador estricto,
`count` y `values` declarados comprobados antes de reservar memoria. Los preflights validan pero
no teselan. Los escáneres reconocen la entrada y la validan con la misma política; la capacidad
del producto no se reduce para no actualizarlos.

**[hecho]** Transiciones atómicas: la instalación debe ser un intercambio que no falle en el
gancho de confirmación nativa (que corre fuera del bloque protegido), nunca en
`commitLoadedDocument` (sirve también a recargas que no cambian de documento). `setBase64`, HTML
con base64, `loadURL` de bytes de archivo y `base64://` no son transaccionales hoy: se envían por
la transacción nativa (`R-c` §7.1). **[hecho]** Classic usa el mismo código de abrir y guardar sin
configuración de producto; la conservación opaca cabe en `MyXMLioJre` y cubre todas las rutas de
guardado; una barrera solo en `saveGeoGebraFile` dejaría las exportaciones base64 y HTML con
pérdida (`R-c` §7.2).

## D. Modelo de resultados de aproximación sin historial de fallos

- **[política]** Un fallo no crea construcción ni Undo, no toca el almacén, no marca el documento
  como modificado y conserva la referencia y sus preferencias; el motivo se muestra en el
  resultado y en un informe de sesión en memoria, nunca persistido (A-DXF1 §9).
- **[política]** El único registro persistente de una conversión o aproximación es el bloque de
  procedencia de la construcción creada; el almacén no guarda historial (A-DXF1 §13.2). El
  resolutor de exportación calcula la representación vigente desde `Construction`.
- **[política]** Los estados «aproximación fallida» y «derivada eliminada» de A-N2 desaparecen como
  estados: tras guardar y reabrir, la referencia es simplemente una referencia sin
  representación CeDG.

## E. Reglas de exportación y estados

**[política]** Seis estados resueltos (exportación §6.4): elección del usuario; representada
por conversión exacta; representada por aproximación (ambas excluidas por defecto);
construcción relacionada modificada (incluida, alternativa A, con aviso breve); asociación no
resoluble (incluida); sin representación CeDG (incluida). Los ocho casos de la instrucción se
proyectan sobre ellos en §6.5: los casos 1, 5 y 6 coinciden en «sin representación CeDG». Se
mantienen: opción desactivada por defecto; poblaciones separadas; racional como `SPLINE`; ninguna
mutación del documento; asociación solo por identificadores explícitos y procedencia aplicable.

## F. Conformidad AutoCAD / ezdxf

**[medida]** Piloto con 20 fixtures escritos a mano con `Double.toString` (JDK 22), SHA-256
registrados; AutoCAD Core Console 2026 (`ACADVER` 25.1) y ezdxf 1.4.2 (copia ya existente en una
carpeta ignorada, cargada en solo lectura; nada se instaló):

- ezdxf lee exactamente todas las definiciones; `construction_tool()` redondea los nudos según el
  código 42, por lo que la evaluación debe construirse desde las listas brutas.
- AutoCAD abre todos con AUDIT limpio y lee exactos los fixtures racionales de referencia, pero
  su lectura decimal no está correctamente redondeada: 268 de 2 822 valores con 1 ulp de error
  (hasta 3 ulp cerca de `10^-50`); trunca en silencio los exponentes de tres cifras; deduce el
  cierre de los datos (devuelve `70 = 15` sin tratar la curva como periódica) e ignora 42/43.
- Las evaluaciones de ambos coinciden con una evaluación racional exacta en unos `1,5e-16`; un
  tramo de anchura subnormal los derrota a ambos.

**[política]** RX-D4 un código 41 tras cada punto de control; RX-D5 bandera 1 solo con cierre
exacto; RX-D6 escribir 210/220/230 = 0/0/1 y omitir 42/43. **[pendiente]** RX-D18 y RX-D19 (§K).

## G. Matemática y persistencia de A-DXF1

- **[política]** `MAINT-A-DXF1`: conversión exacta y aproximación con residuo muestral y
  estimación reservada; `MAINT-A-DXF1b`: cota certificada con procedimiento y presupuesto propios.
- **[hecho matemático]** A-DXF1 §5.9: rango completo en aritmética exacta por Schoenberg–Whitney
  con la condición de Hall para soportes intervalares (multiplicidad interior `≤ q`, al menos
  `2q + 2` muestras interiores por tramo); extremos fijos exactos; cierre `C^0`; continuidad en
  nudos múltiples; determinismo de QR. Marcados como **pendientes de validación**: el umbral
  numérico de rango, cotas cuantitativas de condicionamiento y la convergencia junto a esquinas.
- **[hecho]** Persistencia caracterizada en A-N2: rutas exactas salvo `-0.0`; ruta real de 15 cifras
  evitada; normalización explícita de `-0.0` antes de crear; conservación del bloque en la
  redefinición por sustitución como obligación de `MAINT-A-DXF1`; sin cambio del formato
  numérico del núcleo.

## H. ADR y enmiendas revisadas

ADR 0044 (decisiones 3, 5, 8 y 9 nuevas o ampliadas) y ADR 0045 (decisiones 4, 6 y 7); no se
abre ningún ADR nuevo. Enmiendas: A-IMP-7 sin entradas de derivación; A-IMP-9 y A-NDI-1 con la
política de recursos, la transición atómica y la regla de Classic; A-EXP-6 con el estado 4 y los
lectores. Ningún texto aprobado se modifica.

## I. Matriz de validación

[Matriz R1](pre_g9b_maint_a_n2_validation_matrix.md): C-1 a C-5; RC-1 a RC-24 (nuevos RC-23
presupuesto y RC-24 seguridad del lector; RC-14 Classic en todas las rutas de guardado; RC-18
rutas de sustitución); RX-1 a RX-18 (RX-16 AutoCAD con distancia en ulp; RX-17 ezdxf con listas
brutas; RX-18 fallo sin estado); AP-1 a AP-14; NUM-1 a NUM-10; SEP-4 enmendado y SEP-5 a SEP-9.

## I bis. Revisión independiente de coherencia

Un análisis separado revisó los tres contratos, los ADR, las enmiendas, la matriz, el plan, los
prompts candidatos y las propuestas frente a D-R1-1 a D-R1-10 y a los textos aprobados. Sin
defectos bloqueantes ni redacción de autoaprobación; la demostración de §5.9 de A-DXF1 se
comprobó correcta. Hallazgos importantes corregidos antes de congelar: (1) el presupuesto inicial
contradecía D-R1-4 (ahora derivado de la memoria, nunca inferior al importador en número de
referencias, confirmado por C-4); (2) EXP no podía cumplir sus casos antes de DXF1 (nuevo hito
DXF1-M con el analizador del bloque y la aplicabilidad; RX-7 y RX-18 pasan a DXF1); (3) D-R1-10
frente a los estados de AGENTS §5 (conciliación propuesta: solo en la rama hasta el cierre
autoral); (4) presupuesto y conducta de Classic ante un almacén fuera de presupuesto (Classic
rehúsa abrir). Trece hallazgos menores corregidos: bloque no interpretable con `referenceId`
legible, restos de «salvo decisión del autor», el caso `q < p`, la redacción de tramos comunes,
la identidad de bytes de `Double.toString` por versión de Java, el orden de las reglas de
parada, el límite del informe de sesión, el aviso de Insertar archivo, los predicados
re-comprobables, las etiquetas de estado, la asignación de SEP-5, el contenido de RC-M y una
referencia cruzada.

## J. Resultados de `STATIC`

Registrados en un descendiente documental del commit de contenido R1 (un documento no puede
contener su propio SHA). El descendiente solo cambia este informe; las tres propuestas de
adopción remiten a esta sección.

| Elemento | Valor |
|---|---|
| Commit de gobernanza (prompt R1) | `51d8500b5`, padre `e7803badb` |
| **Candidato de contenido R1** | `228d91d443f38032f1264e133cebf557038820fb`, árbol `0a677f3b21b99c1d1f992c4f216af26dce839798`, padre `51d8500b5` |
| Orden | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-maint-a-n2-r1-static-228d91d4`, desde el worktree (PowerShell 7; directorio de proceso fijado al worktree; consola en la carpeta temporal de la sesión) |
| Código de salida | `0` |
| Ejecución | `verification-34584321c5c3432e9b79ef52c6499e3b`; plan `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52` |
| Identidad ligada | `candidate_commit = 228d91d44…`, `candidate_tree = 0a677f3b2…` (árbol confirmado y limpio) |
| Veredictos | aceptación `ACCEPTED`; cobertura `COMPLETE` |
| Diagnósticos | 1 hallazgo: `diagnostic.governance` `DIAGNOSTIC_FINDING` (diagnóstico permanente, sin efecto en la aceptación); `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`; documentación, estructura de guías, estilo y espacios `DIAGNOSTIC_CLEAR` |
| Informe | `artifacts/agent/pre-g9b-maint-a-n2-r1-static-228d91d4/verification-result.json` (ignorado), SHA-256 `8e795e541ebe46081a00ef3a9a02d4f9a4f6bc70937da335c4dcb96d272a7a59` |
| Comprobaciones previas | contrato del prompt R1 y de los cinco prompts candidatos (`execution_safe = true`); enlaces, UTF-8, LF, columnas de tablas, identificadores y restos de textos retirados; `git diff --check` limpio |

`STATIC` es aceptación técnica de la forma del candidato; no es adopción autoral ni valida el
contenido normativo.

## K. Decisiones todavía pendientes

| Id | Contrato | Cuestión | Recomendación | Por qué es del autor |
|---|---|---|---|---|
| RX-D18 | exportación | regla de lectura de AutoCAD, que yerra 1–3 ulp en ≈10 % de los decimales largos | A: exactitud para ezdxf; AutoCAD ≤ 4 ulp como limitación documentada del lector; B: condición de parada | cambia qué garantía de interoperabilidad se afirma |
| RX-D19 | exportación | valores con exponente de tres cifras, truncados por AutoCAD | rechazar como `NOT_WRITABLE`/`EXPONENT_NOT_PORTABLE`; alternativa: decimales sin exponente para los pequeños | cambia qué referencias son exportables |
| N-R1-1 | fuera de los tres contratos | la exportación DXF ordinaria (aprobada) también escribe `Double.toString` y hereda el problema de exponentes con AutoCAD | tratarlo en un mantenimiento aparte de la exportación aprobada | afecta a un contrato aprobado |
| N-R1-2 | importación aprobada | una referencia cerrada guardada de nuevo por AutoCAD lleva `70 = 15` y la importación la rechaza como `PERIODIC_UNSUPPORTED` aunque no sea periódica | estudiarlo en un mantenimiento de la importación; no se cambia aquí | afecta a un contrato aprobado |
| AX-D6 | A-DXF1 | constantes del método | fijarlas tras las mediciones de DXF1-S0 y DXF1 | pendiente de validación |

Ninguna impide adoptar `R-c` ni A-DXF1; RX-D18 y RX-D19 deben decidirse con la adopción del
contrato de exportación o antes de cerrar `MAINT-A-DXF0-EXP`.

## L. Paquete de adopción normativa

| Contrato | Propuesta | Enmiendas | Pendiente |
|---|---|---|---|
| `cedg-reference-store/v1` + ADR 0044 | [propuesta R-c](pre_g9b_maint_a_n2_r1_adoption_proposal_rc.md) | grupo R | ninguna |
| `cedg-reference-export/v1` + ADR 0045 | [propuesta de exportación](pre_g9b_maint_a_n2_r1_adoption_proposal_export.md) | grupo X | RX-D18, RX-D19 |
| `dxf-spline-approximation/v1` | [propuesta A-DXF1](pre_g9b_maint_a_n2_r1_adoption_proposal_a_dxf1.md) | grupo A | constantes (validación) |

Cada propuesta indica contrato y revisión, candidato, decisiones fijadas y propuestas,
enmiendas, garantías y límites, dependencias, `STATIC` y recomendación. Ninguna registra
`AUTHOR APPROVED`.

## M. Propuesta de ejecución coordinada

[Propuesta](../architecture/pre_g9b_maint_a_n2_r1_coordinated_execution_proposal.md) y
[prompts candidatos](../architecture/pre_g9b_maint_a_n2_r1_candidate_prompts/): una autorización
conjunta de RC-S0, RC, EXP, DXF1-S0 y DXF1 sobre un `main` exacto con los contratos adoptados,
coordinada con puntos de sincronización P1 a P6, integración serial RC → EXP → DXF1 y un informe
final único; DXF1b y A3 fuera. No se han creado worktrees de implementación.

## Evidencia

Las caracterizaciones y sondas de R1 se conservan en
`artifacts/agent/pre-g9b-maint-a-n2-r1-characterization/` del worktree (ignorado): capacidad
(fuente de la sonda, scripts de Gradle, salidas de JDK 25 y JDK 17), análisis de transiciones y
Classic, piloto de conformidad (fixtures, scripts de AutoCAD y ezdxf, salidas); 412 ficheros con
`SHA256SUMS.txt` (SHA-256 del manifiesto
`bcc7e3edb33676a44554aa24026338250b3278a3e439bae3cda6b65a0ff17ef3`). Las sondas de capacidad se
ejecutaron en el checkout principal (mismo commit), que quedó limpio; una de ellas sustituyó el
fichero ignorado `build/jacoco/test.exec`.

## Estado final

```text
PRE-G9B-MAINT-A-N2-R1 — NORMATIVE DESIGN CANDIDATE — READY FOR AUTHOR ADOPTION

selfApproved             = false
authorApproved           = false
passClaimed              = false
implementationAuthorized = false   (RC-S0, RC, EXP, DXF1-S0, DXF1, DXF1b, A3)
BOOTSTRAP_IMPACT         = NO_CHANGE
GUIDE_IMPACT             = NONE en este incremento
```

Sin push, merge, `main`, etiquetas, releases ni empaquetado. El worktree se conserva.
