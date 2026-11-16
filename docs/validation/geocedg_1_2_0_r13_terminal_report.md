# GeoCeDG 1.2.0 — Informe terminal R13

**TECHNICAL CLOSEOUT BLOCKED — R13 FAILED — 13/13 ROUNDS CONSUMED.**

Este checkpoint conserva el trabajo autorizado para revisión y diagnóstico.
No es un candidato técnicamente aceptado ni está listo para smoke de
aceptación. El autor exige terminar y entregar el informe incluso si R13 no
satisface el cierre. No hay autorización R14. No se promueve `main`, crean
tags ni publican binarios. La fecha autoral canónica es `2026-11-16`.

## Resultado y causa

El productor canónico `gradle-test-evidence-producer.ps1`, selección
`clipboard.r13`, terminó naturalmente con código interno **1** en
358 872.867 ms. Emitió veinte XML: **265 casos, 262 aprobados, tres fallidos,
cero errores y cero omisiones**. Imágenes: 65/68; inserción: 13/13;
recuperación: 52/55. Cono y consumidores de vistas incluidos: 14/14.
La salida externa neutral del productor no constituye un aprobado.

| Fallo | Evidencia y efecto |
| --- | --- |
| Fallo real de lector nativo, variante false | Se alcanza la segunda lectura acotada y se restituyen los bytes JavaScript. Antes y después coinciden byte a byte los cuatro miembros del archivo de recuperación. El certificado rechaza una imagen sin etiqueta que permanece en el registro global. Resultado SERIOUS en lugar de SESSION_RESET. |
| Mismo caso, variante true con fallo de restitución | La cadena conserva el fallo original y dos errores OWNER distintos; el scope se cierra. Los archivos siguen siendo idénticos. Persiste el mismo rechazo por imagen sin etiqueta tras la segunda lectura. |
| Fallo tardío beforeAttach, location=2 | La operación declara SESSION_RESET y recupera XML e historial, pero el oráculo detecta una imagen huérfana en el registro reconstruido. El certificado sólo detecta imágenes nuevas sin etiqueta y no rechaza esta imagen etiquetada que ya no pertenece a la construcción viva. |

La creación de `GeoImage` registra su instancia en `App.images`; su retirada
ordinaria la elimina. El lector completo puede limpiar las colecciones de
Construction y recrear defaults sin retirar de ese registro las imágenes de
una lectura intermedia o de una inserción fallida. Esta es la causa de ciclo
de vida identificada por código y oráculos. No se capturaron las identidades
de cada huérfano durante el proceso: la atribución de cada prototipo concreto
es una explicación del mecanismo, no un volcado runtime inexistente.

La igualdad exacta de los archivos y la restitución de JavaScript no bastan
para certificar el estado vivo completo. Los tres fallos son regresiones de
producto, no incidencias de entorno ni motivos para rebajar el certificado.
No se cambian fuentes ni tests después del foco para ocultarlos.

## Procedencia y revisión independiente

El foco usó el HEAD retirado
`6f1d357ca2224dedd0e84fda0faed2d600154bab`, árbol
`b6708de39577009752d45e0b73e9d44d94d695f9`, con el manifiesto explícito
de 73 rutas modificadas; no era un candidato limpio. El árbol histórico
correcto consta en el manifiesto fuente y los diarios Git. Los resultados
posteriores del checkpoint limpio se conservarán con sus propios SHA.

La [ejecución R13](geocedg_1_2_0_r13_execution.md) registra los tres hashes de
producto/tests, la propuesta autorizada y las revisiones previas. El manifiesto
`r13-focal-desktop-01/source-before-runtime.json` tiene SHA-256
`3659c2cd149703f0248c629d46997ba2e78399f803510a21ada203090827311d`.
Las 73 rutas conservaron sus bytes durante el foco. El modelo original y R10
no cambiaron. Compilación previa: código interno 0, 97 995.3328 ms.

Revisión terminal independiente:
`geocedg-120-clipboard-image-design/r13-terminal-independent-review.md`,
SHA-256 `2a4e44a24e35e1b7f5f8b051aefc32589a46b184135e385a9e3563208f1ba550`.
Diagnóstico Desktop conservado: `r13-runtime-diagnosis/diagnosis.md`,
SHA-256 `a6930083110536f0736549d54f88fc404b2458d4f398dbb9ee3367e2c056b519`.
Ambos están bajo el directorio externo de evidencias de esta sesión.

## Computer Use y persistencia ordinaria

Se utilizó la habilidad Computer Use en Desktop real con preferencias propias,
sin modificar el modelo original ni las ventanas del otro agente. El menú y
diálogo se observaron en EN y ES. Se canceló antes de cargar; se insertó una
imagen en capa 7 oculta después del paso 2 con pasos posteriores; se mostró
expresamente la capa para ver el bitmap; se insertó otra al final desde un
cursor intermedio. La capa de trabajo permaneció en 0 y el sufijo `C` y
`Suffix=2` se conservó. Un Undo retiró la segunda imagen y sus esquinas;
un Redo recuperó la operación. El cursor nativo volvió a 8/8 y luego 11/11.

Se guardó por Archivo → Guardar como, se cerró normalmente y se reabrió el
archivo propio en otro proceso. Volvieron los once pasos, la imagen visible,
la capa 7 oculta y el sufijo. El sabor GUI del portapapeles fue una lista de
archivos PNG copiada en Explorer; la cobertura de `imageFlavor` corresponde
a los tests automatizados, no a esta observación. El botón Cancel de la
composición nativa quedó en inglés en el diálogo ES: observación menor de
localización, sin corrección adicional. No se afirma persistencia del idioma.

Capturas y observaciones:
`geocedg-120-r13-cu/02-menu-en.png` a `16-reopen-show-layer7.png` y
`observations.json`, en el directorio externo de evidencias de la sesión.
El archivo local propio es
`artifacts/agent/geocedg-1.2.0/r13-clipboard-cu/clipboard-flow.cedg`.
Este smoke normal no ejercita ni invalida los tres fallos excepcionales.

## Verificaciones y distribución restantes

Los cambios de kernel, DAG, persistencia y Desktop siguen requiriendo PHASE,
INTEGRATION y FINAL; un foco aprobado no los sustituiría. El catálogo JUnit
incorpora filtros nuevos, pero sus recuentos ejecutados no pueden actualizarse
mediante el productor oficial desde una selección fallida. Se corrige sólo
el pin mecánico del catálogo en el registro, sin inventar recuentos, IDs ni
recibos. INTEGRATION/FINAL no pueden obtener aceptación confiable con esta
preparación y estos defectos. Su plan e impedimentos deben quedar explícitos.

Sobre un checkpoint limpio rechazado se completan los controles independientes
que sean aplicables, mediciones F5 y preparación local INTERNAL/NC. Sus diarios,
resultados y paquetes tienen identidad propia; cualquier paquete generado es
**diagnóstico y no autorizado para distribución pública**. Ni PACKAGING ni un
instalador funcional convierten este resultado en aceptación técnica. El informe
externo final registra los comandos, códigos, SHA/árbol, pruebas realizadas y
pendientes sobre ese checkpoint sin modificar las fuentes verificadas.

Las [guías](../user/geocedg_user_guide_es.md), el
[expediente editorial](geocedg_1_2_0_editorial_handoff.md) y el
[smoke selectivo](geocedg_1_2_0_author_smoke.md) conservan los límites científicos
y el bloqueo actual. El manuscrito y baseline del libro no se promueven.
