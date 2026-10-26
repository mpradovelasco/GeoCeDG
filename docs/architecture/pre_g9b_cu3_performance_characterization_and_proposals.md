# PRE-G9B-CU3 — Caracterización del rendimiento de la autoridad constructiva y propuestas de optimización

| Campo | Valor |
|---|---|
| Carácter | Caracterización y propuestas que autoriza la disposición autoral de 2026-10-26, punto 5. No modifica código productivo, no fija objetivos de rendimiento y no adelanta ninguna aprobación. Los tiempos medidos quedan aceptados provisionalmente como limitación del candidato técnico |
| Autoridad | [disposición autoral](../validation/pre_g9b_cu3_author_disposition_record.md) §5; [registro de verificación](../validation/pre_g9b_cu3_verification_record.md) §4, §5 y §8 |
| Herramienta | `tools/benchmark/pre-g9b-cu/run-cu3-performance-characterization.ps1` (prueba `PreG9BCu3PerformanceCharacterizationProbe`), fuera del producto, en el commit `451c1b501f7b010f279b0a1c9af34202c9913af4` |
| Evidencia | [`pre-g9b-cu3-performance-characterization-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-performance-characterization-evidence.json), copia byte a byte de la ejecución A (SHA-256 `cc2ead89c32b3ed126a6e26c1f972ded72fe6add466519923a8c6332d53da0cb`); la ejecución B da los mismos contadores y solo difiere en los tiempos |
| `selfApproved` | `false` |

## 1. Qué se mide

La sonda reproduce el modelo adoptado del banco de E3. Para cada una de las cuatro intersecciones de
los contactos crea una consulta interna con la configuración de decisión del documento. Esa
configuración va envuelta para medir el trabajo que la consulta pide a su fuente residual:

- la captura de la fuente;
- las cajas de las envolventes de `h`, `h′` y `h″`;
- las decisiones exactas de los contactos en los parámetros `kπ/2`, que comprueban de nuevo el
  certificado de identidad constructiva y el pliegue de `CU2`.

Lo mide en dos momentos: durante un arrastre de `H` dentro de su celda, que cambia la revisión, y
en una nueva computación en la misma revisión. Registra además las cajas que comparten las tres
consultas de `e_1`. Como referencia, mide el arrastre de `H` del documento con las mismas órdenes
que el banco de E3. Cinco repeticiones por medida. Los recuentos son deterministas; los tiempos son
de una sola estación e informativos.

## 2. Resultados

**Referencia** (mediana de cinco arrastres, ejecución A): 94,5 ms con la política heredada y
748,2 ms con la constructiva. La ejecución B da 88,8 ms y 767,1 ms.

**Arrastre de `H`** (mediana por consulta):

| Consulta | Cajas | Envolventes | Decisiones exactas | Contactos exactos |
|---|---:|---:|---:|---:|
| `o_1·h_2` | 504 | 33,3 ms | 454,9 ms | 1 |
| `e_1·n` | 283 | 13,6 ms | 7,1 ms | 1 |
| `e_1·c_1` | 1 760 | 62,5 ms | 83,0 ms | 2 |
| `e_1·r` | 932 | 45,7 ms | 7,1 ms | 1 |
| **total** | 3 479 | **155,1 ms** | **552,1 ms** | 5 |

La captura de la fuente cuesta menos de 0,1 ms por consulta.

**Nueva computación en la misma revisión:**

- Repite las mismas cajas con el mismo tiempo de envolventes: 34,0, 14,6, 64,1 y 47,0 ms de cómputo
  total por consulta, 159,7 ms en conjunto.
- Las decisiones exactas, en cambio, salen de la memoria de E3 en menos de 0,1 ms.

**Cajas compartidas** por las consultas de `e_1`:

- `e_1·c_1` y `e_1·r` comparten 775 de sus 1 759 y 931 cajas distintas;
- `e_1·n` no comparte ninguna;
- las tres juntas tienen 2 197 cajas distintas, frente a la suma de 2 972.

## 3. Dónde se va el tiempo

1. **La nueva comprobación de las decisiones exactas** es el 74 % del arrastre (552 de 748 ms), y
   la de `o_1·h_2` sola el 61 %.
   - En una revisión nueva, la memoria de E3 no puede reutilizar la decisión, porque la huella del
     cono cambia. Comprueba el certificado anterior en su celda:
     `ConstructiveIdentityCertifier.recheckAtParameter` y `recheckCell`.
   - La comprobación encierra cada condición de la celda sobre la caja de las indeterminadas: 33
     condiciones en `o_1·h_2`, con polinomios de hasta 21 685 términos en ocho indeterminadas.
   - Usa la forma de valor medio, `CertificatePolynomial.enclose`: el valor **exacto** del
     polinomio en el centro de la caja más las derivadas parciales encerradas. Es la parte cara.
   - La caja es casi puntual, el encierro binary64 de cada indeterminada. Por eso la forma ingenua
     pierde el signo por cancelación, y el código lo resuelve con aritmética exacta en el centro.
   - En `e_1`, la decisión de un contacto estacionario transfiere además el pliegue de `CU2` en
     el parámetro exacto.
2. **Las envolventes de las consultas** son el 21 %: 155 ms y 3 479 cajas. Pesan sobre todo los
   análisis de los pliegues de `e_1` bajo `Σ_C` (1 760 y 932 cajas).
3. El resto (unos 40 ms) es el trabajo de la intersección heredada, el dibujo y la propagación,
   igual que con la política heredada.

## 4. Propuestas que conservan las garantías

Ninguna propuesta cambia una decisión, una tolerancia ni un presupuesto. Cada una devuelve
exactamente la misma evidencia que hoy, o una evidencia igual de rigurosa, y falla cerrada cuando
no puede probar lo que el cálculo actual prueba. Las cifras de ganancia son cotas superiores
estimadas sobre las medidas de §2, no medidas de una implementación.

| Id | Propuesta | Garantía | Ganancia estimada | Riesgo y validación |
|---|---|---|---|---|
| O1 | **Especialización exacta en las indeterminadas que no cambian.** En un arrastre de `H` solo cambian `H.x` y `H.y`; las otras seis indeterminadas conservan su valor exacto. Sustituir esos seis valores exactos en cada condición de la celda, una vez por cada conjunto de valores, da un polinomio exacto en dos variables, que se encierra en cada revisión en lugar del original de ocho | la misma condición y el mismo signo certificado: la especialización es una identidad exacta, y la memoria se liga a los valores exactos sustituidos | hasta la mayor parte de los 552 ms, si el polinomio especializado es mucho menor | memoria por certificado; invalidación exacta cuando cambia cualquier valor sustituido; pruebas: mismas condiciones y signos que la comprobación actual en las doce posiciones del estudio y en V-AUT-DYN |
| O2 | **Vecindad certificada.** Al intentar o comprobar un certificado, certificar los signos de la celda en una caja ampliada alrededor de los valores actuales, tan grande como las condiciones lo admitan, y guardarla con el certificado. Una revisión cuyas indeterminadas caigan dentro de esa caja no necesita evaluar las condiciones: un signo certificado en una caja vale en cada subcaja. Las correspondencias de rama y la clave estructural se siguen comprobando | rigurosa por inclusión de cajas; fuera de la vecindad se comprueba como hoy | casi todo el coste de las comprobaciones mientras `H` no salga de la vecindad | la ampliación tiene un coste inicial; la vecindad se liga a la clave y la celda; pruebas adversarias de salida de la vecindad y de cambio de rama |
| O3 | **Memoria por revisión de la evidencia de una consulta.** Una nueva computación con la misma huella de la fuente y del objetivo, la misma política y las mismas versiones devuelve la evidencia ya calculada | idéntica por determinismo: las mismas entradas exactas dan la misma salida | los 160 ms de cada computación repetida (§2); no reduce el primer cálculo de una revisión | huella completa (la de la revisión, la del objetivo y la de la política); pruebas de invalidación |
| O4 | **Envolventes de la fuente compartidas** entre las consultas de un mismo locus y revisión. La envolvente del punto trazado sobre una caja no depende del objetivo | idéntica: se reutiliza el mismo encierro de la fuente | hasta un 26 % de las cajas de la fuente de `e_1` (775 compartidas), dentro de los 121 ms de sus envolventes | la memoria se liga a la revisión del locus; medir antes la fracción del coste que corresponde a la fuente |
| O5 | **Menos cajas en el análisis de los pliegues.** Partir de la banda certificada del pliegue de `CU2` en lugar de dividir la celda de muestreo, y parar en cuanto el certificado de conteo es decisivo | exige demostrar los mismos certificados (o más fuertes) en la matriz de E3 | parte de los 108 ms de `e_1·c_1` y `e_1·r` | es un cambio del análisis: necesita sus propias pruebas de equivalencia y una decisión antes de cualquier código |
| O6 | **Certificación fuera del camino interactivo.** Durante un arrastre, la intersección muestra su estado anterior como pendiente (`NOT_ESTABLISHED`, sin token admisible nuevo) y certifica en segundo plano o al soltar | ninguna afirmación sin prueba: lo pendiente falla cerrado | el arrastre volvería a tiempos heredados | cambia el comportamiento visible durante el arrastre y el modelo de hilos (las cargas fuera del hilo de eventos ya dieron carreras): decisión autoral expresa |

**Orden recomendado para un incremento futuro:** O1 y O3 primero, porque no cambian ninguna
decisión, son locales y se prueban por igualdad con el cálculo actual. Después O2 y O4, con pruebas
adversarias propias. O5 y O6 solo con una decisión específica.

## 5. Lo que no se propone

- Ninguna tolerancia, umbral dependiente de la vista ni constante de posición.
- Ningún atajo que acepte un signo, una celda o un pliegue sin la comprobación rigurosa que hoy se
  hace: una reutilización solo vale cuando una identidad exacta o una inclusión de cajas la
  justifica.
- Ninguna ampliación de `constructive-identity-certificate-budget/v1` ni de
  `cu3-exact-evaluation-budget/v1`.
- La promoción futura de la autoridad constructiva a predeterminada necesita una evaluación de
  rendimiento y de compatibilidad general independiente (disposición autoral §5). Esta
  caracterización no la sustituye.

## 6. Limitaciones

- Una estación y un modelo, el del autor, con los cinco contactos.
- La adopción, de unos 2,3 s, no se ha desglosado.
- La sonda no separa, dentro de una envolvente, la parte de la fuente y la del objetivo. Por eso la
  ganancia de O4 se acota por el número de cajas.
