# Revisión P4 e inserción experta · Propuesta posterior a CU4

**Estado. Propuesta registrada para una actuación separada, pendiente de delimitación y autorización autoral.** La referencia de partida es el [cierre de CU4](../validation/pre_g9b_cu4_closeout_record.md) y su [contrato de inserción](../../geocedg/specs/ui/document-insertion.md). Esta propuesta conserva la admisión y el comportamiento vigentes.

## A. Revisión de exclusiones P4

Inventariar las exclusiones por clase, productor y dependencias. Distinguir exclusión histórica, falta de pruebas, incompatibilidad técnica y riesgo efectivo. Para cada clase que pueda justificarse, definir invariantes de valores, dependencias, identidad, apariencia aplicable, persistencia y Undo/Redo. Caracterizar con documentos temporales y pruebas focales antes de proponer una fila nueva. Cada ampliación requerirá una decisión expresa sobre el perímetro demostrado.

## B. Inserción experta

Estudiar una operación separada bajo responsabilidad del usuario en la que la clase de un objeto por sí sola no determine el rechazo. La operación propuesta usaría copias temporales aisladas, un análisis diferencial y una revisión visual y constructiva. Informaría de los cambios y de las incertidumbres detectadas, exigiría aceptación explícita del riesgo y recomendaría guardar el resultado en otra ruta. La autorización de una oferta se vincularía al origen y al estado del receptor examinados.

Los controles de carga, seguridad, integridad del DAG, dependencias, identidad y persistencia siguen siendo condiciones de admisión. Un fallo técnico de recuperación, una pérdida de integridad o un cambio no evaluable que comprometa estos controles debe detener la operación. El consentimiento cubre únicamente los riesgos presentados dentro del perímetro que llegue a autorizarse.

## Deuda prioritaria

Incluir `OBS-CU4-ANGLE-PREFERENCES-REFRESH` al delimitar la siguiente actuación autorizada. El estado mostrado por Preferencias debe corresponder al modo efectivo del kernel para grados, radianes y DMS al abrir o cambiar documentos. El [registro de deuda](../validation/pre_g9b_cu4_closeout_record.md#obs-cu4-angle-preferences-refresh) fija el caso reproducido, la validación Swing y el smoke focal. La planificación de la corrección queda pendiente de asignación autoral.
