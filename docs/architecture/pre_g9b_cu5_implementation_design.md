# PRE-G9B-CU5 — Diseño de implementación y puerta técnica

Base autorizada `b842165733261126e0f742586834e2bfc612b6f1`, árbol
`607af3a916025d8a1bbd5cc8f4a21c9a95d7fd66`. Diseño de implementación del mandato
autoral del 2026-11-08. El plan consolidado y los contratos F1/F2 siguen siendo
la autoridad en todo comportamiento ajeno a B02, B03, H05 y DC-4.

## B02

Desktop mide las celdas visibles con sus renderizadores y su tipografía real.
Dos líneas de la fuente del protocolo son el mínimo de legibilidad. Una celda
más alta aumenta su fila y se añade el margen de fila de Swing. El recálculo
sigue al cambio de modelo, columnas, fuente o Look & Feel. La navegación que
solo modifica el paso no vuelve a medir el contenido. Se conserva el orden del
modelo y la configuración de columnas, impresión y exportación. La prueba Swing
contrasta el tamaño con el contenido y repite los refrescos del Look & Feel.

## B03 y H05

Desktop, Gráficos 1. Ctrl+clic en un botón o elemento nativo alterna la
preferencia de memoria de esa herramienta. Un clic nativo ordinario o su
activación accesible por teclado aplica la preferencia vigente. Las preferencias
de Paralela y Perpendicular duran durante la sesión de la ventana. Esc y el
cambio de herramienta finalizan el gesto activo. Menús, acciones, scripts y API
conservan la activación ordinaria, sin alterar preferencias ni ranuras. Un
cambio efectivo de comportamiento durante el mismo modo conserva la cancelación
de gestos incompletos de F2-R1. La indicación bilingüe distingue modo activo y
estado de la referencia.

| Estado de la referencia | Regla |
|---|---|
| Vacía | No se ha confirmado una construcción con referencia utilizable para esta ranura |
| Disponible | Misma instancia del orden de construcción y vigente, disponible en el paso y usable según F2 |
| Suspendida por paso | Misma instancia vigente, con índice constructivo posterior al paso |
| Suspendida | Misma instancia vigente, temporalmente no utilizable por los predicados F2 |
| Invalidada | Instancia retirada o reconstruida sin correspondencia explícita. Requiere selección nueva |

La disponibilidad se consulta con `isAvailableAtConstructionStep` en el
momento de utilizar la ranura. La navegación no elimina miembros del conjunto
de orden constructivo. Un avance recupera la misma instancia si todos los
predicados vuelven a cumplirse. Borrado, reemplazo estructural y reconstrucción
invalidan cualquier instancia que no se pueda conservar con el contrato F2.
Antes de delegar un gesto de memoria, la herramienta comprueba también la
identidad y el paso de las entradas que ya había seleccionado explícitamente.
Si una entrada deja de estar disponible, descarta la selección parcial sin
sustituir la referencia perdida por otra guardada en esa misma pulsación.
Undo/Redo invalidan las referencias guardadas. No se incorpora un mecanismo
de correspondencia entre instancias reconstruidas. La referencia de un documento
anterior queda invalidada al reconstruir, aunque la nueva construcción contenga
la misma etiqueta. Continúan las verificaciones antes de cada uso y la
certificación por salida construida. La sesión no escribe XML, preferencias
persistentes ni puntos de Undo. La elección explícita y los algoritmos de F2
conservan sus entradas y su DAG.

## M04

Se admiten los ejes 2D de la construcción exclusivamente en la entrada
orientadora de `AlgoLinePointLine` y `AlgoOrthoLinePointLine`. El clasificador
continúa por las rutas F1 ya admitidas. La prueba de identidad usa el eje
canónico del kernel y excluye ejes de la clasificación de salidas dibujables.
El vector de `GeoLine.getDirection` para los ejes es (+1,0) o (0,+1).
Las salidas y sus cadenas usan el signo que producen los algoritmos existentes.
No hay nuevos cálculos, tolerancias, campos XML ni cambios de hit-testing.

## Verificación e impacto

`VERIFICATION_CLASS=BOUNDED_PHASE`, `frozenAtPhaseStart=true`,
`CLOSEOUT_MODE=AUTHOR_OPERATED`. El único cambio heredado previsto pertenece a
la presentación Swing del protocolo. Las pruebas de admisión F1, herramientas
F2/F2-R1, caracterización CU0 y perfil Desktop se incorporan a
`PHASE PRE-G9B-CU5`, junto con pruebas focales. El plan carece de obligaciones
adicionales de integración semántica porque el paquete consume la construcción,
sus pasos y su dirección mediante los contratos existentes. Guía y smoke
autoral quedan como evidencia separada. Impacto de infraestructura y bootstrap
`NONE`: se amplía la selección de pruebas del registro existente, con las
mismas dependencias, toolchain, entorno y productores.
