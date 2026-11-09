# CU8-I-R1 · Composición y apariencia de paneles

Incremento de presentación Desktop en Gráficos 1 sobre el checkpoint
`81b6fc0661a61c7fa7a853ab17584d6a55b39a19`. La disposición sigue siendo
estado del documento ajeno al DAG. La relación panel–control se obtiene
del registro por instancia. [Contrato v1](control-layout-v1.md).

## Orden y gestos

El fondo de la vista, la geometría y los controles sueltos conservan
sus pasadas nativas. A continuación se dibujan los paneles de atrás
hacia delante. Cada panel dibuja el fondo, después sus controles
activos con el orden nativo de capa e índice constructivo dentro de su
panel, y por último el borde, el encabezado y el tirador de edición.
El fondo siguiente atenúa todo lo que hay detrás. Una separación
retira el control de la pasada de su antiguo panel inmediatamente.
Los paneles ocultos solo muestran una guía de edición en el gestor.
El orden de creación pone los paneles nuevos y duplicados delante.
Seleccionar, mover, editar el estilo u ocultar un panel conserva su
orden. El lector v1 original usa el orden canónico por UUID.
Las máscaras y las ayudas de herramientas que el anfitrión dibuja
encima de los objetos mantienen sus pasadas y prioridades nativas.

Se usa el mismo contorno efectivo para componer y determinar el panel
superior en el puntero. Sus miembros visibles pueden recibir hits.
El fondo impide accionar controles sueltos o de paneles inferiores bajo
el puntero, incluso con la transparencia máxima. La geometría mantiene
sus filtros nativos y se prioriza el control accesible en modo Mover.
El clic derecho en un panel accesible ofrece abrir el gestor existente
con ese panel seleccionado, fuera de capturas de herramienta, diálogos
y edición pendiente. La navegación del gestor no crea entradas de Undo.
El widget Swing del campo requiere que su rectángulo esté libre de
paneles superiores. Con cobertura parcial se dibuja el campo estático
y el editor no se abre. Si una transformación cubre el editor activo,
este sale mediante el flujo nativo. Exportación y otras vistas
conservan su pintura nativa.

## Apariencia y persistencia

Cada panel guarda un RGB, una opacidad entera entre 25 % y 80 % y un
nombre Unicode. El RGB predeterminado es `e1edf7` y la opacidad es 65 %.
El nombre vacío usa el nombre localizado «Panel». Los nombres admiten
hasta 64 escalares Unicode válidos sin caracteres de control ni no
caracteres. Se permite repetirlos. El encabezado recorta por anchura
medida con puntos suspensivos. Sus textos y controles usan tamaños
lógicos y contraste independiente del color de fondo. El gestor aplica
una previsualización local inmediata y una entrada de Undo al confirmar
un cambio. Cancelar descarta la previsualización.

La extensión opcional `geocedgControlAppearance` acompaña la cabecera
original de la construcción. Contiene `1|id,rrggbb,opacidad,nombreHex;…`
en orden de atrás hacia delante, con un registro por panel. `nombreHex`
codifica UTF-8 estricto en hexadecimal minúsculo. El lector exige UUID
del registro original, cobertura exacta y límites de v1. La ausencia
del atributo aplica los tres valores predeterminados y el orden v1.
El atributo se omite si la apariencia y el orden coinciden con esos
valores. Cabecera y ranuras v1 conservan su forma original. Sin paneles
se conserva la escritura histórica. Una extensión aislada, incompleta,
duplicada, malformada o de versión desconocida se rechaza antes de
publicar. Los lectores anteriores a R1 pueden descartar la extensión.
Las pérdidas aprobadas para lectores previos a CU8 siguen delimitadas
por el contrato v1. Classic de R1 conserva la extensión al guardar.

## Validación

Las pruebas focales cruzan composición por píxeles, hits y acciones,
editor activo, vista previa, Undo/Redo, archivo Classic, archivo v1,
negativos de XML y certificación del receptor de inserción con orden y
estilo. La campaña usa STATIC, PHASE `PRE-G9B-CU8-I` e INTEGRATION.
Computer Use verifica el producto Desktop real y conserva evidencia por
SHA. El impacto de verificación se limita a inventario y selección CU8.
El launcher, el JDK, Gradle y los prerrequisitos de estación conservan
su configuración. La matriz de cobertura registra la ampliación de la
acción existente. La guía de revisión se actualiza en el expediente.

```text
VERIFICATION_CLASS = INTEGRATED_PHASE
VERIFICATION_PROFILES = STATIC, PHASE PRE-G9B-CU8-I, INTEGRATION
VERIFICATION_INFRASTRUCTURE_IMPACT = CU8 AND FULL JUNIT INVENTORY PINS
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = geocedg/specs/ui/application-profile.md, geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json
selfApproved = false
```
