# PRE-G9B-CU3 — Plan de la optimización focal del rendimiento de la autoridad constructiva

| Campo | Valor |
|---|---|
| Carácter | Propuesta concreta para la disposición específica que exige el autor antes de cualquier cambio productivo de rendimiento. No cambia código y no está autorizada |
| Autoridad | [registro del smoke](../validation/pre_g9b_cu3_author_smoke_record.md) §3; [caracterización y propuestas](pre_g9b_cu3_performance_characterization_and_proposals.md) |
| Base | candidato técnico `3c2ca7e8` (producto) y caracterización en `451c1b50`, publicada en `7f1a81ae` |
| `selfApproved` | `false` |

## 1. Punto de partida

**Situación medida.** El smoke autoral es funcionalmente correcto, pero no acepta el rendimiento
interactivo. El arrastre de `H` en el modelo adoptado tarda unos 748 ms, frente a unos 90 ms con la
política heredada. Se reparte así:

| Parte | Tiempo | Fracción |
|---|---:|---:|
| nueva comprobación de las decisiones exactas (`recheckCell`), 455 ms solo en `o_1·h_2` | 552 ms | 74 % |
| envolventes de las consultas (3 479 cajas) | 155 ms | 21 % |
| resto: intersección heredada, dibujo y propagación | unos 40 ms | 5 % |

El arrastre de `B` todavía no se ha medido por separado.

**Por qué es cara la comprobación.** Hechos del código de `CertificatePolynomial` y
`ConstructiveIdentityCertifier`:

1. Cada condición de la celda se encierra en la forma de valor medio: el valor **exacto** en el
   centro de la caja más las derivadas parciales encerradas.
2. `valueAt` evalúa cada término por separado, con productos racionales exactos repetidos para cada
   potencia, sin compartir potencias ni subexpresiones.
3. `partial` calcula las derivadas **de nuevo en cada comprobación**: el certificado guarda sus
   condiciones, nunca sus derivadas.
4. En `o_1·h_2` son 33 condiciones con polinomios de hasta 21 685 términos en ocho indeterminadas.
5. En un arrastre de `H`, las cajas de `A.x`, `A.y`, `rbCone`, `D_1.x` y `D_1.y` son **puntos
   exactos**, porque son entradas libres. `H.x` y `H.y` son intervalos del punto cortado, y `B.y`
   es un intervalo estrecho del punto sobre camino.

**Consecuencia para el objetivo.** Aunque la comprobación costara cero, el arrastre de `H` seguiría
en unos 200 ms: 155 ms de envolventes más el resto. El objetivo orientativo de menos de 100 ms
exige reducir también las envolventes.

## 2. Técnicas, por orden de prioridad

### Fase 1: comprobación del certificado y evaluación exacta de las condiciones

| Id | Técnica | Por qué conserva las garantías | Efecto esperado |
|---|---|---|---|
| T1 | **Derivadas y estructura por certificado.** Calcular una vez, al crear o adoptar el certificado, las derivadas parciales de cada condición (primer y segundo orden) y su forma compilada, y guardarlas con él, inmutables | las derivadas son polinomios exactos determinados por la condición; calcularlas una vez o cada vez da el mismo resultado | elimina el recálculo simbólico de cada comprobación |
| T2 | **Evaluación con potencias y subexpresiones compartidas.** Evaluar el valor exacto en el centro con un esquema de Horner multivariable, o con una tabla de potencias por indeterminada calculada una vez por caja, en lugar de multiplicar término a término | es la misma suma exacta de los mismos términos, reordenada; el valor racional resultante es idéntico | reduce el número de productos racionales en un factor del orden del grado medio |
| T3 | **Especialización exacta de las indeterminadas invariantes.** Para cada certificado, sustituir los valores racionales exactos de las indeterminadas cuya caja es un punto exacto. Esa caja corresponde a una entrada libre, y en el arrastre de `H` son cinco de las ocho. El resultado es un polinomio exacto en las restantes, que se guarda con la clave de esos valores exactos | la sustitución de un valor exacto en una indeterminada de caja puntual es una identidad exacta; el término de valor medio de esa indeterminada es nulo (radio cero); el encierro resultante está contenido en el actual, así que nunca certifica un signo distinto | el número de términos baja de miles a, previsiblemente, cientos; hay que medirlo (paso 0) |
| T4 | **Memoria por revisión con claves completas.** Guardar el resultado de una comprobación con la clave estructural del certificado, la celda y los extremos exactos de la caja de todas las indeterminadas; y el resultado de una consulta con la huella de la fuente, la del objetivo, la política y las versiones | la misma entrada exacta da la misma salida; una clave incompleta es la única forma de admitir evidencia obsoleta, y la clave incluye todo lo que la comprobación lee | elimina el trabajo repetido en la misma revisión (hoy, 160 ms por computación repetida) |
| T5 | **Vecindades certificadas.** Al probar o comprobar, certificar los signos de la celda en una caja ampliada alrededor de los valores actuales, por duplicaciones sucesivas mientras se mantengan, y guardarla con el certificado. En una revisión cuya caja esté contenida en la vecindad no se evalúa ninguna condición. Las correspondencias de rama y la clave estructural se comprueban siempre | un signo certificado en una caja vale en toda subcaja (inclusión de conjuntos); la clave y las ramas siguen comprobándose | casi todo el coste de la comprobación mientras `H` o `B` no salgan de la vecindad |

### Fase 2: envolventes de las consultas

| Id | Técnica | Por qué conserva las garantías | Efecto esperado |
|---|---|---|---|
| T6 | **Envolventes de la fuente compartidas** entre las consultas de un mismo locus y revisión, con la clave de la revisión del locus y de la caja | el encierro de la fuente sobre una caja no depende del objetivo | hasta un 26 % de las cajas de `e_1` (775 compartidas) |
| T7 | **Menos cajas en el análisis de los pliegues**, partiendo de la banda certificada del pliegue de `CU2` | exige demostrar los mismos certificados de conteo, o más fuertes, en la matriz de E3 | parte de los 108 ms de `e_1·c_1` y `e_1·r`; es la técnica de mayor riesgo |

Las técnicas T1 a T4 no cambian ninguna decisión ni ninguna caja, por lo que se verifican por
igualdad con el cálculo actual. T5 y T7 cambian el camino de la prueba y necesitan pruebas
adversarias propias. T6 cambia solo de dónde sale un encierro idéntico.

## 3. Garantías que no se tocan

- **Decisiones.** Ninguna tolerancia, umbral de vista ni constante de posición. Ninguna decisión se
  acepta sin la prueba que hoy la establece: una reutilización solo vale por una identidad exacta
  (T1 a T4, T6) o por inclusión de cajas (T5).
- **Identidad.** No cambia ninguna identidad, token, selector, germen, clave estructural ni linaje.
- **Presupuestos.** No cambian `cu3-exact-evaluation-budget/v1` ni
  `constructive-identity-certificate-budget/v1`. Los contadores siguen siendo deterministas y los
  agotamientos fallan cerrados. Los contadores de una comprobación pueden bajar con T1 a T3; se
  informa de la diferencia.
- **Memorias.**
  - Ninguna memoria sobrevive a un cambio de política, de clave, de celda, de revisión o de versión.
  - Deshacer, Rehacer y la reapertura recalculan o invalidan.
  - Nada se persiste.
- **Degeneraciones.** Las degeneraciones y los fallos cerrados de E1 a E3 dan los mismos resultados:
  `D_1` ×3, `H` +40 en otra celda, `α_cone` a 35°, casi tangencias, inyecciones de presupuesto y
  un objetivo desplazado un ulp.

## 4. Incremento propuesto y su verificación

1. **Paso 0, medidas de partida en la misma estación**, sobre `3c2ca7e8`:
   - ampliar la sonda de rendimiento, que es una herramienta fuera del producto, con el arrastre de
     `B`;
   - separar dentro de la comprobación el valor en el centro, las derivadas y las correspondencias;
   - medir el tamaño de los polinomios especializados de T3.

   Si T3 no reduce los términos de forma sustancial, se informa antes de seguir.
2. **Fase 1 (T1 a T4) en el núcleo compartido**, con pruebas de igualdad:
   - mismos estados, causas, celdas, signos, órdenes y claves que el cálculo actual en los cinco
     contactos, las doce posiciones del estudio, V-AUT-DYN y V-AUT-ID;
   - pruebas adversarias de invalidación de cada memoria.
3. **T5 y Fase 2**, en ese orden y solo si la Fase 1 no basta, con sus pruebas adversarias:
   - salida de la vecindad;
   - cambio de rama;
   - caja que no contiene el valor ideal;
   - equivalencia de los certificados de conteo de E3.
4. **Comparación final** del candidato congelado `3c2ca7e8` con el optimizado, en la misma estación
   y configuración:
   - arrastres de `H` y de `B` por separado;
   - comprobaciones del certificado, envolventes y resto;
   - adopción;
   - mismos contadores donde la técnica no los cambia por diseño;
   - mismos resultados de integridad y de degeneración.
5. **Verificación de la clase que decida el autor.** Se propone `INTEGRATED_PHASE` (`STATIC`,
   `INFRA_UNIT`, `PHASE` de `CU3`, `CU2`, `CU1` y `CU0`, e `INTEGRATION`), porque el cambio está en
   el núcleo compartido.

**Objetivo orientativo.** Menos de 100 ms en las actualizaciones habituales de `H` y `B`, cerca de
la política heredada. Es una referencia, no un criterio de aceptación: la aceptación del
rendimiento es autoral.

**Condiciones de parada.** Detener y presentar la evidencia si ocurre cualquiera de estas cosas:

- una técnica cambiaría una decisión, una celda o un signo certificado de forma no explicada por
  una inclusión rigurosa;
- haría falta tocar la identidad o un presupuesto;
- el objetivo exigiría admitir evidencia obsoleta o una tolerancia.

## 5. Lo que se pide al autor

Una disposición específica que autorice, o no:

- el paso 0;
- la Fase 1;
- T5 y la Fase 2 de forma condicionada;
- la clase de verificación.

No hay ninguna autorización implícita en este plan. `CU3-B` queda fuera de este incremento.
