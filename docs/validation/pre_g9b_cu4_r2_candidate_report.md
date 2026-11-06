# PRE-G9B-CU4-R2

El candidato local desciende de R1 `46574bfd680c08511626466d81c6ffae1ef6cd64`, árbol `2b5acd1255513c005337759d7f2e8f3e4e08b71a`. El baseline aprobado de CU4 es `1c3893f539d926c16da837a42d439a1cf8bea34f`, árbol `1c0f6e9187663b586acfc7c425053d7be0349c21`. La auditoría independiente de R1 pidió cambios. Los checkpoints, los comandos, los resultados y sus hashes se registran en `artifacts/agent/pre-g9b-cu4-r2`.

## A · Compatibilidad de las preferencias heredadas

El lector nativo de defaults reinicia la opacidad de línea a 255 para cada prototipo presente. Si hay opacidad explícita, la aplica después. Un prototipo ausente conserva su estado. Si `animation` omite `step`, el lector conserva el paso y el modo de incremento del prototipo. Si recibe un paso finito explícito en un numérico o ángulo, aplica el incremento fijo. El centinela de estilo de punto `-1` se entrega al setter del tipo, que conserva su significado para listas.

`LegacyStyleDefaults` incorpora estas reglas en la representación tipada que se publica después de validar el bloque. Continúan vigentes la separación entre preferencias personales y valores documentales, la precedencia aprobada y la validación antes de aplicar los campos. Los lectores nativos de construcciones y archivos de documento conservan su ruta. El historial de Undo/Redo y la identidad durable siguen bajo sus contratos existentes.

El ensayo del auditor compara 37 bloques exactos con el lector nativo en hosts desechables. El test permanente usa los 35 archivos versionados y dos archivos generados para cubrir el contraste entre atributos explícitos y omitidos. En el corpus de auditoría, los dos archivos adicionales aportaban un bloque duplicado. La regresión permanente compara los estilos efectivos de los prototipos y de 14 objetos creados por entrada. Incluye pruebas directas de los tres contraejemplos. El test de preferencias Portable añade los 38 adversarios del auditor y una referencia en el mínimo del deslizador. Comprueba la conservación de prototipos, objetos y algoritmos por instancia, aristas, identidades, pasos e historial con Redo disponible.

## Vigencia y alcance

`VERIFICATION_CLASS=INTEGRATED_PHASE`. R2 exige también FINAL sobre el SHA definitivo. `VERIFICATION_INFRASTRUCTURE_IMPACT=CATALOG_ONLY` por las identidades focales de la etapa A. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED` porque la corrección y las pruebas mantienen los mismos prerrequisitos de Windows, JDK, Gradle y Conda. `GUIDE_IMPACT = NONE` para A porque no cambia el flujo público de preferencias.

La deuda histórica F3 de la instantánea Undo de `InterCilConoOblique.cedg` sigue registrada en el [informe R1](pre_g9b_cu4_r1_candidate_report.md#p3-y-deuda-f3). El dictamen independiente y el smoke de R2 quedan pendientes de actuaciones separadas.
