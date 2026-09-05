# G9U1 — re-smoke autoral, revisión 1

**PENDIENTE DEL AUTOR. No marcar automáticamente PASS.** Esta revisión recoge
el smoke manual realizado por Manuel Prado-Velasco sobre el candidato G9U1 de
estabilización. Debe leerse junto con la
[guía rápida](../user/geocedg_construction_quick_guide.md), el roadmap vivo,
las specs/ADR vigentes y la evidencia de validación de G9U1.

Use un archivo nuevo o una copia conservada de `Revision1.cedg`. No sobrescriba
`TestBasic1.cedg` ni ningún artefacto que reproduzca un fallo. Un fallo de
guardado no autoriza a reparar, sustituir ni sobrescribir silenciosamente la
evidencia.

## Convenciones de esta revisión

- `PASS`: el comportamiento observado satisface el smoke previsto.
- `PASS*`: pasa el smoke actual, pero queda una mejora explícitamente diferida.
- `PASS-n`: pasa el smoke actual, con una observación autoral numerada `n`.
- `FAIL-n`: fallo observado ligado a la observación `n`.
- Una observación autoral no modifica por sí sola una spec/ADR ya aprobada.
  Cuando la petición requiere cambiar semántica de kernel, identidad,
  persistencia o contrato público, debe diseñarse y autorizarse separadamente.
- Las correcciones puramente frontend/producto de esta revisión sí pueden formar
  parte de una continuación acotada de G9U1 cuando preserven el catálogo único de
  acciones, el DAG, la identidad, la serialización y las autoridades ya aprobadas.

## Disposición previa para la continuación G9U1

### A. Correcciones/aceptación G9U1 antes de un nuevo re-smoke autoral

**G9U1-B1 — bloqueo de guardado tras herramienta persistente.**<br>
La invocación de `EllipseAxis`/`ellipseAxis` desde la biblioteca persistente
produce una elipse visible, pero el guardado posterior falla y la elipse no queda
serializada en `Revision1.cedg`. Es un bloqueo de cierre: reproducir, aislar causa,
corregir sin cambiar la semántica del motor `Macro`, y validar guardar →
cerrar → reabrir, incluyendo undo/redo y save-after-undo. Conservar el artefacto
fallido y el log.

**G9U1-B2 — auxiliares internos de interacción.**<br>
Los auxiliares creados por GeoCeDG para un punto semántico interactivo
(p. ej. `text3`, `f` en una definición interna `Point(b,text3,f)`) no deben
aparecer sobre el lienzo ni confundirse con objetos autorales. Deben seguir
siendo dependencias trazables cuando sean necesarias, pero con presentación
auxiliar/interna coherente y no visible en la Vista Gráfica por defecto.
Reutilizar, si es suficiente, la semántica upstream de objetos auxiliares y su
control de visualización; no crear una segunda autoridad de dependencias.

**G9U1-B3 — edición ordinaria de parámetros.**<br>
Un número ordinario usado como factor, p. ej. `kesc` en `Dilate(b,kesc)`, debe
seguir siendo editable por las rutas algebraicas ordinarias aprobadas, además
del slider. No ampliar redefine semántico de Locus/Spline para resolverlo.

**G9U1-A1 — ayudas contextualizadas.**<br>
Eliminar la coletilla genérica
`Use the current construction and its explicit selection.` /
`Usa la construcción actual y su selección explícita.` cuando no describe la
acción. `short_help` y `long_help` deben ser específicos; la ayuda larga debe
añadir información real de uso, inputs/selección, efectos, límites o
disponibilidad. Revisar especialmente Undo/Redo, idioma, Spline V2,
materialización, herramientas persistentes y Laboratory.

**G9U1-A2 — organización profesional del menú y settings.**<br>
La revisión autoral prefiere aproximar CeDG Construction a la organización
familiar de GeoGebra: `File`, `Edit`, `View`, `Construction`, `Options`,
`Automation`, `Help`; `Construction` se añade tras `View` y `Automation`
ocupa el papel de `Tools`. Recuperar en `Options` las configuraciones upstream
compatibles, filtrando/bloqueando las que contradicen política GeoCeDG:
`Continuity` debe seguir bloqueado `OFF`, y el selector de idioma del producto
debe seguir restringido a EN/ES. Mantener una sola autoridad de acciones y
preferencias; no restaurar menús upstream como segunda autoridad paralela.
Al tratarse de una modificación respecto al diseño de seis menús del candidato,
actualizar de forma coherente manifest/spec/guía/validación como enmienda de
revisión autoral de G9U1.

**G9U1-A3 — limpieza de navegación documental.**<br>
Usar un rótulo inequívoco para `Documents → New construction`, preferentemente
`New file` / `New construction file` y equivalente ES. Eliminar
`File → Import and export → Open` si solo duplica `Documents → Open`.
Auditar también la duplicación de `Open Classic diagnostic session`: debe quedar
una ruta clara, no dos entradas equivalentes sin razón.

**G9U1-A4 — herramientas persistentes en toolbar.**<br>
La fijación de herramientas de usuario debe integrarse visualmente con la barra:
mismo tamaño/apariencia que herramientas normales, orden configurable,
posibilidad de agrupación desplegable y soporte de icono cuando pueda reutilizarse
de forma segura el seam upstream de macros/toolbars o un icono aportado
explícitamente por el usuario. Posición, grupo e icono son preferencias de
presentación del perfil, no acciones estables nuevas ni geometría documental.
No importar iconos históricos de Templatev7 sin autoridad/licencia.

**G9U1-A5 — branding/versionado de producto.**<br>
Definir una única fuente de versión GeoCeDG y usar **0.9** para este candidato.
La ventana/título debe presentar de forma profesional el icono GeoCeDG y
`GeoCeDG 0.9 — <nombre de archivo>` (o equivalente coherente con convenciones
de plataforma). `About` debe distinguir versión GeoCeDG y baseline GeoGebra y
puede identificar al autor como `Manuel Prado-Velasco, Universidad de Sevilla`,
preservando créditos/licencias upstream requeridos. Usar la imagen autoral de
arranque solo si está realmente disponible e identificada en repo/worktree;
no fabricar ni adivinar un asset ausente.

**G9U1-A6 — discoverability de branch/component.**<br>
El usuario no debe necesitar conocer de memoria `spline-v2/main` o
`generator.main`. El inspector/ayuda debe mostrar branch/component/semantic
address de forma legible cuando proceda. Esto no autoriza aún un overload que
omita `branchKey` en el comando público.

**G9U1-A7 — variables auxiliares creadas por herramientas.**<br>
El grado que un diálogo de Spline V2 materializa como número ordinario puede ser
útil para edición/slider, pero debe identificarse claramente como auxiliar
creado por la herramienta y quedar oculto por defecto según la política upstream
de auxiliares cuando sea apropiado. Un valor fraccionario debe seguir
rechazándose/invalidándose porque el grado admitido es integral. Investigar el
caso de auxiliares huérfanos al redefinir/eliminar el consumidor: solo corregir
en G9U1 si puede demostrarse propiedad exclusiva del helper y limpieza segura
con seams existentes. No introducir garbage collection geométrico ni borrar
objetos que puedan ser autorales.

### B. Requisitos autorales registrados, pero fuera del alcance semántico actual de G9U1

**POST-U1-1 — `Length(S,A,C)` con nodos de interpolación.**<br>
Se desea admitir longitud parcial cuando `A` y `C` son nodos que definen
`SplineV2`, aunque no hayan sido creados mediante
`Point(S,"spline-v2/main",u)`. El contrato actual exige endpoints con dirección
semántica exacta y prohíbe inferir preimagen por coordenadas. Estudiar después de
G9U1 una extensión kernel en la que Spline V2 exponga, por provenance de su
constructor, las direcciones semánticas exactas de los nodos de interpolación.
Debe resolver explícitamente nodos repetidos, múltiples preimágenes, spline
periódica, autointersecciones, revisión y persistencia. No implementar por
proximidad ni como parche frontend.

**POST-U1-2 — orden inverso y longitud orientada.**<br>
La revisión autoral desea que invertir endpoints pueda producir una longitud
algebraicamente orientada, por ejemplo `Length(S,Q,P) = -Length(S,P,Q)`.
Esto contradice el contrato G7 vigente, donde la longitud es no negativa y
`FORWARD/REVERSE` selecciona la ruta. Tratarlo como revisión de contrato
métrico/público, no como bug G9U1.

**POST-U1-3 — recorrido forzado en la dirección positiva del locus.**<br>
También se desea un modo explícito que, para
`start → A → B → end`, permita desde `B` a `A` recorrer la dirección positiva,
con resultado conceptual
`L(B,end) + L(start,A)`. El contrato interno G7 ya contempla una política
`WRAP_TO_START` no negativa para esta clase de recorrido, pero la superficie
pública actual no debe ampliarse durante G9U1 sin diseño. Estudiar un argumento,
overload o comando separado sin fijar el nombre antes de revisar compatibilidad,
periodicidad, gaps y políticas de borde.

**POST-U1-4 — redefinición de `SplineV2` / `LocusV2`.**<br>
Investigar una operación explícita de redefinición/reemplazo. El candidato
actual los presenta read-only salvo redefine compatible aprobado por G9A.
Una redefinición incompatible o un reemplazo real debe producir identidad nueva;
no se permite `delete + recreate` conservando la identidad antigua bajo cuerda.
Cualquier preservación de downstream debe ser transaccional y compatible con
DAG, undo/redo, save/reopen y reglas de identidad.

**POST-U1-5 — hipótesis de “retroceder el protocolo” para redefinir.**<br>
Analizar la idea como requisito de orden de construcción, pero no usar la
navegación del Construction Protocol como autoridad semántica. El protocolo es
una vista/historial del procedimiento, no el mecanismo correcto para reescribir
el DAG. Si existe una necesidad real de mantener posición/orden de construcción,
estudiar los seams kernel/upstream de reemplazo y transacción; documentar si la
hipótesis debe rechazarse o reformularse.

**POST-U1-6 — colisión entre macro local e instalada.**<br>
Se desea estudiar una resolución explícita de colisión que permita preferir la
herramienta instalada. No eliminar del `.cedg` una definición macro que sea
necesaria para reconstruir `AlgoMacro` ni convertir silenciosamente un documento
autocontenido en uno dependiente de preferencias de aplicación. Investigar
equivalencia por digest/provenance, posible migración o expansión explícita de
resultados, y portabilidad `.ggb` → `.cedg`. No implementar en G9U1 sin contrato
de persistencia.

**G12 — zoom por teclado configurable centrado en cursor.**<br>
El roadmap ya reserva para G12 zoom centrado en cursor, tecla configurable,
navegación precisa y escalas avanzadas. Mantener en G9U1 el `ZoomWindow` básico
y `Ctrl`+`+` / `Ctrl`+`-`; registrar/verificar que el requisito autoral queda
cubierto por G12, sin adelantarlo.

**POST-U1-7 — branchKey opcional.**<br>
Analizar posteriormente si un overload sin `branchKey` puede usar una rama
principal únicamente cuando la fuente tenga una única rama principal inequívoca.
No introducir un default que oculte ambigüedad en loci multirrama. La
discoverability del branch sí pertenece a G9U1-A6.

## Observaciones numeradas del smoke

**1. Spline V2 y grado auxiliar.**<br>
El grado del spline se lleva a una variable visible en Álgebra y puede
modificarse incluso mediante slider. Esto puede ser útil, pero un grado
fraccionario deja correctamente de producir una spline válida: el contrato
admite grado integral. Debe revisarse la presentación/propiedad del helper,
su estado auxiliar y la limpieza segura si queda sin consumidor. Véase
G9U1-A7.

**2. `Length(b,P,Q)` y orden inverso.**<br>
`Length(b,P,Q)` funciona. Al escribir `Length(b,Q,P)` se observa error/estado no
definido. Caracterizar primero si se trata de error sintáctico/preview o del
resultado semántico `TARGET_NOT_REACHABLE` bajo la política actual. La petición
de signo negativo es POST-U1-2 y no debe incorporarse silenciosamente al contrato
G7.

**3. Punto semántico interactivo y auxiliares.**<br>
Tras crear el punto `E`, la inspección muestra una definición interna del tipo
`Point(b,text3,f)`. `text3` y `f` aparecen como auxiliares. La trazabilidad es
aceptable, pero estos helpers internos deben tener presentación coherente y no
ser visibles sobre el lienzo por defecto. Véase G9U1-B2.

**4. `Length(L,U,V)` y orden inverso.**<br>
El caso directo funciona. La petición de signo contrario al conmutar U/V y la
petición de recorrido positivo pasando por `end/start` quedan en POST-U1-2 y
POST-U1-3. No modificar G7 como parte de una corrección frontend.

**5. Factor de `Dilate`.**<br>
Con `Dilate(b,kesc)` el slider modifica `kesc`, pero no se consiguió editarlo por
la ruta algebraica habitual. Debe conservarse edición ordinaria de números.
Véase G9U1-B3.

**6. Herramientas persistentes, colisiones y Laboratory.**<br>
La biblioteca persistente funciona y `Legacy Laboratory` resulta útil para
abrir `.ggb`/`.ggt` históricos e incluso apoyar creación/inspección de `.ggt`.
Debe mantenerse, mejorando su ayuda. La sustitución de una macro local por una
instalada se difiere a POST-U1-6. La integración profesional de las herramientas
fijadas en toolbar se trata en G9U1-A4.

**7. Fallo de guardado tras `EllipseAxis`.**<br>
Después de invocar la herramienta aparece la elipse, pero el guardado falla y
la elipse no queda persistida en `Revision1.cedg`. Es G9U1-B1 y bloquea el
closeout hasta corrección y revalidación.

## Notas generales de interfaz

La revisión autoral considera que el entorno es funcionalmente prometedor, pero
la organización y discoverability todavía necesitan refinamiento profesional.
Debe privilegiarse compatibilidad mental con GeoGebra donde no contradiga CeDG,
sin volver a un frontend upstream no gobernado.

Las opciones/configuración deben reutilizar seams upstream cuando sea posible.
No crear una preferencia paralela si ya existe una opción upstream apta. Los
controles incompatibles con GeoCeDG deben quedar ocultos, bloqueados o explicados
de forma explícita; nunca deben reactivar comportamiento no determinista.

La ayuda de `Legacy Laboratory` debe explicar claramente que abre un contexto
legacy/diagnóstico aislado y que no promueve automáticamente macros o semántica
histórica a autoridad GeoCeDG.

## Checklist ejecutado por el autor

| Acción | Resultado visible esperado | Dónde / ruta | PASS / FAIL |
| --- | --- | --- | --- |
| Iniciar con `--enableLocusV2=true` | Construction y creación V2 habilitada | Guía §1 | PASS |
| Revisar menú y barra | Menús/product toolbar utilizables; la organización profesional queda sujeta a G9U1-A2/A3/A4 | Parte superior; Ver → reaplicar espacio si el documento conservaba otra disposición | PASS* |
| Crear A/B/C/D y h según guía | Cuatro puntos alineados, h=0 | Entrada; Intro por línea | PASS |
| Crear S y revisar icono Spline | Curva semántica definida, icono coherente | Construcción → Spline V2 / barra | PASS-1 |
| Crear P/Q explícitos y M/MP | M=4, MP=2 | Guía §3; Entrada | PASS-2 |
| Inspeccionar MP | Definición `Length(S,P,Q)`, no sólo auxiliar oculto | Álgebra / definición / Propiedades | PASS |
| Probar `Length(S,A,C)` | Bajo el contrato actual: indefinido con explicación de endpoints; no preimagen inferida. La ampliación deseada queda POST-U1-1 | Entrada; guía §3 | PASS |
| Crear otro punto con Punto y arrastrarlo | Mismo punto interactivo se desplaza sobre la spline; helpers internos no deben contaminar el lienzo | Barra Punto; clic en trazo, luego Mover | PASS-3 |
| Cambiar h y devolver a 0 | S/P/Q recomputan; M=4, MP=2 | Fila h o deslizador | PASS |
| Crear L/U/V/LL/LP según guía | Locus recto ordinario; LL=4, LP=2 | Guía §4 | PASS-4 |
| Crear círculo c y R | Resultado rico con dos soluciones admisibles | Guía §5; Intersect | PASS |
| Abrir inspector, crear una y luego la restante | Dos puntos distintos; inspector sigue usable; indicación de ya creados | R → inspector; materializar seleccionadas/todas | PASS |
| Inspeccionar S y un número ordinario | S sólo lectura con explicación; número editable por ruta habitual | Álgebra, contexto y Propiedades | PASS |
| Introducir entrada sin Intro; cancelar | Ningún objeto nuevo | Entrada → Escape | PASS |
| Redefinir k compatible, dilatar S, pasar por 0 y recuperar | Misma identidad conforme G9A; imagen colapsada en 0; recuperación no arbitraria | Guía §6 | PASS-5 |
| Abrir protocolo y hacer zoom | Procedimiento visible; ZoomWindow y Ctrl+± operativos. Zoom avanzado queda en G12 | Ver → protocolo/navegación; foco en Gráficos | PASS |
| Instalar `.ggt` planar propio y fijarlo | Disponible en Herramientas de usuario; instalación no modifica el documento; integración toolbar pendiente de G9U1-A4 | Automatización → Herramientas de usuario → Gestionar herramientas de usuario… → Instalar `.ggt`… | PASS-6 |
| Invocar herramienta propia | Sólo la invocación crea sus resultados y éstos deben poder guardarse/reabrirse | Menú de herramientas / barra fijada | FAIL-7 |
| Crear objeto auxiliar, Deshacer/Rehacer/Deshacer | Desaparece/reaparece sólo la operación prevista | Editar o Ctrl+Z/Ctrl+Y | PASS |
| Guardar después de Deshacer y cerrar | Guardado correcto; ningún error ni pérdida inesperada | Archivo → Guardar; cerrar documento | PASS |
| Abrir documento nuevo | Herramienta instalada sigue disponible; no macros ajenas auto-instaladas | Archivo → Nuevo; Automatización | PASS |
| Primera reapertura de `Revision1.cedg` | Curvas, métricas y puntos definidos/indefinidos como antes; bindings conservados | Archivo → Abrir | PASS |
| Mover punto interactivo, guardar, cerrar, reabrir otra vez | Segunda reapertura correcta y edición persistente | Mover → Guardar → Abrir | PASS |
| Reiniciar, comprobar herramienta en documento nuevo y después eliminarla | Instalación sobrevive reinicio; eliminación explícita persiste y no borra resultados del documento. La política actual de colisión local/instalada sigue fail-closed | Automatización → Herramientas de usuario → Gestionar herramientas de usuario… → Eliminar paquete instalado | PASS |
| Cambiar EN/ES y comprobar ayuda/Classic diagnóstico | Rótulos coherentes; Classic conserva su conducta; ayuda genérica requiere G9U1-A1 | Ayuda / idiomas / Classic | PASS* |

Si una operación hace desaparecer objetos inesperadamente, anote el comando o
gesto exacto **antes** de continuar. Conserve una copia del archivo y el log.
Anote aparte costura periódica, chooser de autointersección y casos de par spline
si forman parte de la revisión ampliada: las rutas siguen en la guía y el plan
G9U1.

## Criterio de salida de la siguiente estabilización

La continuación del agente puede terminar como
`G9U1 IMPLEMENTATION CANDIDATE — READY FOR AUTHOR RE-SMOKE/CLOSEOUT` únicamente
si:

1. G9U1-B1/B2/B3 están corregidos o, tras inspección, el agente demuestra con
   evidencia versionada que la observación no era reproducible y no existe
   regresión;
2. las enmiendas frontend G9U1-A1..A7 autorizadas que resulten implementables
   sin cambiar semántica de kernel quedan coherentes en código, manifest,
   specs/guía y tests;
3. los requisitos POST-U1/G12 se documentan y aparcan sin adelantarlos;
4. pasan los tests focales y la verificación canónica PHASE/COMPOSED/FULL que
   corresponda al conjunto final de fuentes; y
5. se publica un nuevo candidato limpio sin promover `main`, sin crear tag PASS
   y sin autoaprobar G9U1.

Después de ese estado sigue siendo necesaria una **decisión explícita del autor**.

Autor: Manuel Prado-Velasco<br>
Fecha de revisión: 5 de septiembre de 2026<br>
Resultado del autor: **PENDIENTE DE CORRECCIÓN Y NUEVO RE-SMOKE**<br>
Incidencias principales: **G9U1-B1, G9U1-B2, G9U1-B3; refinamientos A1–A7; requisitos POST-U1/G12 registrados**
