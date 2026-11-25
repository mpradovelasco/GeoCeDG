# PRE-G9B-MAINT-A-DXF0-R1 — guía breve de smoke focal

Guía para el autor. Solo cubre los cambios de R1; no repite el smoke de A-DXF0, cuyo resultado
(`AUTHOR_SMOKE = PASS — AUTHOR REPORTED`) se conserva. Duración estimada: 10 minutos.

Preparación: arrancar GeoCeDG desde el worktree `GeoCeDG-wt-maint-a-dxf0-r1` (candidato R1).
Archivo de prueba: `source/shared/common-jre/src/test/resources/org/geocedg/common/importer/dxf/smoke-mixed.dxf`.

| Paso | Acción | Resultado esperado |
|---|---|---|
| S1 | Abrir **Archivo → Insertar** (EN: **File → Insert**) | La última entrada es **Importar referencia DXF…** (EN: **Import DXF Reference…**). En **Archivo** ya no aparece junto a las exportaciones |
| S2 | Abrir **Archivo** | La exportación se llama **Exportar geometría 2D como DXF…** (EN: **Export 2D geometry as DXF…**), sin «(experimental)»; su ayuda emergente tampoco lo dice |
| S3 | Importar `smoke-mixed.dxf` con la transformación identidad | El informe es breve: archivo, referencias admitidas por nombre (capa · identificador) y naturaleza, y avisos en una línea. **Detalles…** abre el informe técnico (SHA-256, versión, recuentos, transformación, datos estructurales, causas de rechazo) |
| S4 | En la ventana **Referencias DXF importadas**, seleccionar una referencia y pulsar **Detalles…** | Primero nombre, naturaleza y visibilidad; bajo «Detalles», contrato, cierre, continuidad, procedencia, resúmenes y diagnósticos |
| S5 | Cerrar la ventana; **Archivo → Vista gráfica como imagen…**: exportar PNG, SVG y PDF del área por defecto | Las referencias visibles aparecen con su estilo discontinuo morado, en su posición, dentro del área. Al ampliar el SVG o el PDF, las curvas siguen nítidas (vectoriales) |
| S6 | **Vista previa de impresión** y **copiar la vista gráfica al portapapeles** (Ctrl+Mayús+C), pegando en otra aplicación | Las referencias visibles aparecen igual |
| S7 | Ocultar una referencia (**Mostrar/Ocultar**) y repetir una exportación PNG | La referencia oculta no aparece; las demás sí |
| S8 | Cambiar el zoom y repetir la exportación PNG o SVG del mismo área | La curva ocupa la misma posición relativa en la imagen |
| S9 | Abrir la Vista Gráfica 2 y exportarla como imagen | No contiene referencias (Gráficos 2 queda diferido) |
| S10 | **Exportar geometría 2D como DXF…** y PGF/TikZ | No contienen las referencias (exportaciones semánticas) |
| S11 | Deshacer y Rehacer tras las exportaciones | La construcción no cambia por exportar ni por importar referencias |

Resultado que se pide comunicar: `AUTHOR_SMOKE = PASS`, o el paso y lo observado si algo no
coincide. Las referencias siguen siendo de sesión (`R-a`): no se guardan con el documento.
