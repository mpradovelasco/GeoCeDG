# G9U1 — re-smoke autoral, revisión 1

**PENDIENTE DEL AUTOR. No marcar automáticamente PASS.** Siga la
[guía rápida](../user/geocedg_construction_quick_guide.md) en una sesión coherente.
Use un archivo nuevo `Revision1.cedg`; no sobrescriba `TestBasic1.cedg`.

| Acción | Resultado visible esperado | Dónde / ruta | PASS / FAIL |
| --- | --- | --- | --- |
| Iniciar con `--enableLocusV2=true` | Construction y creación V2 habilitada | Guía §1 | ___ |
| Revisar menú y barra | Seis menús superiores; barra compacta, sin tira de once botones-menú | Parte superior; Ver → reaplicar espacio si el documento conservaba otra disposición | ___ |
| Crear A/B/C/D y h según guía | Cuatro puntos alineados, h=0 | Entrada; Intro por línea | ___ |
| Crear S y revisar icono Spline | Curva semántica definida, icono coherente | Construcción → Spline V2 / barra | ___ |
| Crear P/Q explícitos y M/MP | M=4, MP=2 | Guía §3; Entrada | ___ |
| Inspeccionar MP | Definición `Length(S,P,Q)`, no sólo auxiliar oculto | Álgebra / definición / Propiedades | ___ |
| Probar `Length(S,A,C)` | Indefinido con explicación de endpoints; no preimagen inferida | Entrada; guía §3 | ___ |
| Crear otro punto con Punto y arrastrarlo | Mismo punto interactivo se desplaza sobre la spline | Barra Punto; clic en trazo, luego Mover | ___ |
| Cambiar h y devolver a 0 | S/P/Q recomputan; M=4, MP=2 | Fila h o deslizador | ___ |
| Crear L/U/V/LL/LP según guía | Locus recto ordinario; LL=4, LP=2 | Guía §4 | ___ |
| Crear círculo c y R | Resultado rico con dos soluciones admisibles | Guía §5; Intersect | ___ |
| Abrir inspector, crear una y luego la restante | Dos puntos distintos; inspector sigue usable; indicación de ya creados | R → inspector; materializar seleccionadas/todas | ___ |
| Inspeccionar S y un número ordinario | S sólo lectura con explicación; número editable por ruta habitual | Álgebra, contexto y Propiedades | ___ |
| Introducir entrada sin Intro; cancelar | Ningún objeto nuevo | Entrada → Escape | ___ |
| Redefinir k compatible, dilatar S, pasar por 0 y recuperar | Misma identidad conforme G9A; imagen colapsada en 0; recuperación no arbitraria | Guía §6 | ___ |
| Abrir protocolo y hacer zoom | Procedimiento visible; zoom por ventana y Ctrl+± operativos | Ver → protocolo/navegación; foco en Gráficos | ___ |
| Instalar .ggt planar propio y fijarlo | Disponible en Herramientas de usuario y grupo propio; documento sin cambios por instalación | Automatización → Herramientas de usuario → Gestionar herramientas de usuario… → Instalar .ggt…; guía §8 para exportación local o creación en Classic | ___ |
| Invocar herramienta propia | Sólo la invocación crea sus resultados | Menú de herramientas / barra fijada | ___ |
| Crear objeto auxiliar, Deshacer/Rehacer/Deshacer | Desaparece/reaparece sólo la operación prevista | Editar o Ctrl+Z/Ctrl+Y | ___ |
| Guardar después de Deshacer y cerrar | Guardado correcto; ningún error ni pérdida inesperada | Archivo → Guardar; cerrar documento | ___ |
| Abrir documento nuevo | Herramienta instalada sigue disponible; no macros ajenas auto-instaladas | Archivo → Nuevo; Automatización | ___ |
| Primera reapertura de Revision1.cedg | Curvas, métricas y puntos definidos/indefinidos como antes; bindings conservados | Archivo → Abrir | ___ |
| Mover punto interactivo, guardar, cerrar, reabrir otra vez | Segunda reapertura correcta y edición persistente | Mover → Guardar → Abrir | ___ |
| Reiniciar, comprobar herramienta en documento nuevo y después eliminarla | Instalación sobrevive reinicio; eliminación explícita persiste y no borra resultados del documento. Si el documento ya contiene la macro, la entrada instalada indica colisión sin reemplazarla | Automatización → Herramientas de usuario → Gestionar herramientas de usuario… → Eliminar paquete instalado | ___ |
| Cambiar EN/ES y comprobar ayuda/Classic diagnóstico | Rótulos coherentes; Classic conserva su conducta | Ayuda / idiomas / Classic | ___ |

Si una operación hace desaparecer objetos inesperadamente, anote el comando o
gesto exacto **antes** de continuar. Conserve una copia del archivo y el log;
un fallo de guardado no autoriza sobrescribir la evidencia anterior. Anote aparte
costura periódica, chooser de autointersección y casos de par spline si forman
parte de su revisión ampliada: las rutas siguen en la guía y el plan G9U1.

Resultado del autor: __________  Fecha: __________  Incidencias: __________
