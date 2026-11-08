# GeoCeDG · Inserción con revisión experta

**Normativa v1 · AUTHOR APPROVED.** El autor adopta el perímetro pasivo 2D
demostrado por el producto exacto del registro de cierre. Se conserva la ruta
histórica de esta especificación para las referencias de I3–I5.

[Cierre autoral](../../../docs/validation/post_cu4_expert_insertion_closeout_record.md) ·
[Diseño PRE-I3](../../../docs/architecture/post_cu4_expert_publication_contract_candidate.md) ·
[Inserción certificada F3/P4/B2](document-insertion.md)

## Alcance y decisiones EXP

| Decisión | Perímetro adoptado |
|---|---|
| EXP-01 | **Archivo → Insertar → Insertar con revisión experta…** inspecciona una fuente `.cedg` o `.ggb` frente al receptor vivo. La admisión exige integridad verificable en el perímetro ensayado. Un cambio conocido de capa, vista, estilo, rótulo o geometría válida se puede consentir si figura en el informe y el ensayo y la publicación reproducen el efecto. `NOT_ESTABLISHED` expresa falta de evidencia. Una incertidumbre en equivalencia geométrica puede consentirse cuando las comprobaciones técnicas independientes cubren la adición. |
| EXP-02 | El ensayo y el commit usan la copia nativa. Los ganchos optativos de diagnóstico, procedencia y reservas se acotan a la operación. El commit reutiliza la barrera, la historia y la recuperación F3. El ensayo no modifica el receptor vivo. La vía certificada mantiene sus comprobaciones y alcance. |
| EXP-03 | La acción está disponible por defecto en GeoCeDG. El consentimiento es explícito y de un solo uso. Identifica la captura exacta de origen, la sesión viva, la versión de análisis, el informe y el resultado revisado. Una invalidación observada es terminal. Después de incorporar, se recomienda **Guardar como…** en un destino distinto. La escritura requiere una decisión del usuario y el estado de sesión conserva el formato nativo. |
| EXP-04 | El trabajador intenta las construcciones pasivas admitidas por su gramática y mide el cierre, la trazabilidad, la geometría y la reapertura. La pertenencia a P4 queda fuera de este predicado. La pérdida de cobertura obligatoria se comunica por capacidad o propiedad concreta. El arco circular directo puede participar como construcción experta. ARC-01/P4 sigue una decisión independiente. |

La activación pertenece al perfil GeoCeDG. Para diagnóstico se puede usar
`--enableExpertInsertion=false` o solicitar detalle de consola mediante
`--logLevel=DEBUG`. Classic conserva su perfil y sus acciones. El arranque
ordinario de GeoCeDG usa nivel `INFO` y conserva los diagnósticos de error
y de recuperación grave.

## Geometría e integridad

El núcleo conserva la autoridad sobre los objetos, los productores y las
dependencias. La operación aditiva verifica el inventario de adiciones y
las referencias ordenadas disponibles. Las etiquetas del transporte sirven
para seguir la copia cuando hay traza nativa. Las identidades durables
requieren su propio mecanismo de remapeo y verificación. La presencia de
autoridad espacial o identidad participante sin cobertura demostrada
impide publicar. El UUID y las unidades del receptor se conservan.

El arco circular directo usa los puertos nativos ordenados de centro, inicio
y dirección final. En un círculo de radio positivo, el arco tiene orientación
y dominio nativos. Las degeneraciones durante la edición se rigen por el
productor. La inspección separa un estado indefinido válido de la pérdida de
un objeto o una dependencia. Una afirmación de equivalencia geométrica
requiere un oráculo y un dominio explícitos. El ensayo y la vista previa
usan la construcción nativa sin aproximaciones geométricas nuevas.
La comprobación focal del arco compara radio relativo, amplitud y longitud
normalizada por radio con tolerancia `1e-9`. Las tres evaluaciones puntuales
de una curva cartesiana sirven al diferencial. Su equivalencia global queda
sin establecer.

Un productor de punto sobre trayectoria se evalúa con el modo nativo de
parámetros del receptor. La revisión distingue ese modo en origen y
receptor y muestra las coordenadas observadas de los puntos trazados. El
consentimiento vincula el modo efectivo a la huella del resultado. Cambiar
el modo durante la revisión invalida la oferta aunque se restituya.
El caso verificado de `Point(s,t)` sobre un segmento conserva el productor
y sus puertos, aunque el modo del receptor produzca un punto válido en
otras coordenadas. Su valor aceptado y su actualización dinámica se
comprueban en la incorporación, el historial y la reapertura.

## Ensayo y consentimiento

La captura se realiza en EDT y recoge el dibujo efectivo, incluido lo que
queda fuera del archivo asociado. El trabajador separado valida la envoltura
pasiva antes de cargar y ejecuta una copia nativa desechable. El contexto de
capas y vistas del receptor participa en el cotejo. El informe conserva por
separado los cambios conocidos, las propiedades no establecidas y las
condiciones que impiden ofrecer la incorporación. La reapertura usa el
escritor y el lector nativos. La vista previa gráfica, cuando está disponible,
procede del dibujo de ensayo.

La publicación comprueba de nuevo la huella antes de capturar Undo y bajo
la barrera antes de publicar el historial. Una diferencia sobrevenida en
el resultado sigue la ruta de recuperación F3.

El ajuste de apertura en ventana separada de un panel oculto es pasivo y
puede acompañar a la captura. Si el panel está visible en una ventana
separada, el filtro impide el ensayo e identifica ese ajuste en el informe.

El XML de construcción de GeoCeDG conserva la capa de cada número mediante
el elemento nativo `<layer>`, tanto al guardar como en el historial. Al cargar
archivos anteriores sin ese dato se aplica la asignación nativa. La copia
desde el portapapeles mantiene su propia serialización.

Los defaults y recursos se comprueban antes y después de la copia en cada
entorno. La reapertura puede normalizar defaults según el lector nativo. El
cotejo entre JVM compara el resultado efectivo y mantiene el archivo original
de captura como evidencia de los defaults del receptor vivo. Las diferencias
de presentación observadas en la reapertura se someten a la misma revisión.

La primera versión bloquea scripts, bibliotecas ejecutables, animaciones y
recursos fuera de la envoltura pasiva. Las macros quedan en inspección.
También quedan fuera los objetos 3D, la semántica espacial, el remapeo de
identidad durable participante y la conversión entre modos angulares.
La geometría malformada, las dependencias rotas y la recuperación sin una
vía certificable impiden ofrecer la incorporación. La JVM separada aísla
la sesión de trabajo. La barrera pasiva es el control de ejecución de código.

Tras el cotejo final bajo barrera, una incorporación correcta añade un paso
Undo. Los fallos durante la mutación usan las rutas de recuperación F3. Una
recuperación sin certificado conduce al aislamiento grave. Mientras quede
pendiente guardar el paso experto, Guardar abre **Guardar como…** y el
destino debe ser distinto del origen y del receptor inicial. La asociación
documental se actualiza después de guardar correctamente la copia nativa.

## Verificación del perímetro adoptado

- `CHANGE_ROUTE = ORDINARY`
- `VERIFICATION_CLASS = INTEGRATED_PHASE`, `frozenAtPhaseStart = true`
- El `PHASE` registrado cubre carga pasiva, revisión, copia, caducidad,
  recuperación, reapertura y persistencia experta. Incorpora las hojas de
  integración pertinentes para el perfil Desktop, el portapapeles compartido,
  la historia nativa y el lector, con regresiones F3/P4/B2 y CU4. Se conserva
  la atribución al SHA y árbol exactos.
- El plan usa una selección integrada acotada a las rutas afectadas. El
  perfil global `INTEGRATION` incluye además las suites científicas y los
  paquetes ajenos a esta operación. El alcance de este cambio no exige una
  campaña `FINAL` de producto. El cierre identifica los informes tipados
  del SHA verificado y las comprobaciones de arranque ordinario y de la
  app-image interna Windows sin argumentos de aplicación.
- `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`. Se usa la JVM y el launcher
  Desktop ya distribuidos. Los cambios de catálogos de verificación se
  limitan a la cobertura de esta fase y a las identidades derivadas de sus
  pruebas, según los planes registrados.

[Autoridad de verificación](../operations/verification-levels.md) ·
[Identidad documental](native-document-identity.md) ·
[Identidad espacial](../spatial/g9-spatial-projection-semantics.md) ·
[Unidades y procedencia D1](../units/unit-system.md)
