# PRE-I3–I5 · Diferencial de inspección I2

Propuesta técnica de la capa de aplicación Desktop. La clasificación P4 sigue
siendo independiente del inventario y el kernel conserva la autoridad sobre
objetos, dependencias, evaluación e identidad. Este contrato de informe no
modifica el archivo nativo ni establece una garantía geométrica nueva.

## Evidencia y alcance

| Frontera | Testigo admitido | Límite |
|---|---|---|
| Receptor a ensayo | Misma instancia retenida dentro del trabajador y, para incorporaciones, instancia nueva más la traza de transporte cuando existe | Una instancia perdida puede haberse reconstruido. Sin procedencia no se clasifica como omisión. |
| Origen a ensayo | Instancia de origen, token sintético emitido por el portapapeles de esta operación y resolución a instancia nueva antes de L1 | Los tokens se usan en la operación que los emitió. Etiquetas ordinarias, coordenadas y orden de enumeración son localizadores o datos observados. |
| Ensayo a reapertura | Mismo UUID persistido y adjunto durable comprobado por el registro nativo | Si desaparece un adjunto, se acredita pérdida de identidad. La omisión del objeto requiere evidencia adicional. |
| Inventario estático | Declaraciones XML acotadas del archivo y hallazgos de preinspección | Las declaraciones son afirmaciones del archivo y pueden ser parciales. Un bloqueo de seguridad impide evaluar el contenido. |

Las aristas se cotejan en el puerto ordenado del productor con extremos
acreditados. El eje nativo se identifica por su rol. Los operandos anónimos y
locales mantienen `NOT_ESTABLISHED` aunque coincidan sus valores. Una arista
conocida distinta aporta un cambio aunque otras sigan sin establecerse.

Se informa por separado de integridad de documento, correspondencia
constructiva, equivalencia geométrica, presentación, persistencia y
publicabilidad. El estado de una dimensión se interpreta con su alcance. Una
lectura sin errores y un inventario limitado no certifican clausura ni
integridad global. Las definiciones se conservan como texto nativo, los
dominios y valores como observaciones de estado y la validez como `isDefined`
del kernel. Falta un comparador certificado por familia para equivalencia y
degeneraciones. La presentación cubre exclusivamente los campos observados.
La reapertura acredita persistencia de los hechos comparados, con cobertura
incompleta para objetos ordinarios sin ID y para recursos. Publicación está
fuera de I2.

## Secuencia

En la evidencia del candidato I2 aparecen el escalar y la lista del origen en
`TRIAL`, pero `SOURCE → TRIAL` carece de parejas. La copia nativa agrega a la
lista de transporte predecesores con etiqueta local. El escritor solo emite
elementos registrados de la construcción y reconstruye los operandos locales
a partir del comando. La resolución global no devuelve la variable local.
El cotejo posicional de cardinalidades del búfer descarta por ello el mapa
completo. La reparación candidata observa cada dirección de transporte por
separado antes de L1. El mapa y el comportamiento de copia de F3 conservan su
contrato. Los puertos locales, los literales anónimos y las referencias dentro
de expresiones anónimas conservan su incertidumbre, aunque se conozcan la
pareja de la lista y su muestra actual.

La secuencia preservada contiene `a=2` y `Sequence(k*a,k,1,4)`. Su SHA-256
es `7cbe4fa86cc15df59615628ee91be706767576167f81eb2923dd599ab5b7baf4`.
El XML declara `a` y `obj`, y solo expresa `k` como puerto local del comando.
En el informe I2 de `candidate-smoke/sequence`, el ensayo observa ambas
declaraciones y la lista `2, 4, 6, 8`. La advertencia de traza incompleta se
refiere al mapa de top-level y no constituye evidencia de pérdida de esos geos.
Las llamadas nativas que separan ambos conjuntos son
`CopyPaste.addPredecessorGeos`, `CopyPasteD.beforeSavingToXML`, la emisión
acotada por `Construction.getConstructionElement` y `CopyPaste.handleLabels`.

## Validación focal

Comparar casos de arco, curva, secuencia, texto dependiente y construcción
elemental, incluidos homónimos y un nombre que coincida con un token de
transporte. Verificar falta de correspondencia para puertos anónimos, cambio
de arista con extremos probados, pérdida de adjunto sin omisión inferida y
bloqueo previo a carga para macros, scripts, recursos y archivo inválido.
Una discrepancia de presentación se registra por propiedad. Repetir la
comparación de archivos idénticos y cotejar estados, parejas y observaciones.

### Evidencia de desarrollo

`artifacts/agent/post-cu4-pre-i3/development-gradle-retry/evidence.json`
registra la ejecución de `gradle-command-evidence-producer.ps1` sobre
`:desktop:desktop:test` con los filtros de captura, diferencial y preinspección.
El proceso Gradle devolvió `1` por un fixture de captura corregido posteriormente.
Los XML de ese mismo proceso registran 6/6 casos del diferencial y 8/8 de la
envoltura sin fallos ni omisiones. El resultado pertenece a la compilación de
desarrollo que recoge el log. La validación del candidato congelado se registra
por separado.

La ejecución de desarrollo del lanzador aislado usó la ruta de clases capturada
por esa compilación y Java Desktop 25. Conservó los bytes de los ocho fixtures
históricos y ejecutó la secuencia dos veces. `replay-i2-fixtures.ps1` devolvió
`0` y guardó manifestación, informes y logs individuales en
`artifacts/agent/post-cu4-pre-i3/development-i2-replay/`. SHA-256 del manifiesto:
`66647129205d626d0bfbd2675e09893e3fdd095b7dccff1e42704dfd601797d9`.

| Fixture | Admisión P4 | Inventario nativo de origen | Parejas origen a ensayo |
|---|---|---:|---:|
| Elemental | `ADMITTED` | 3 | 3 |
| Arco | `REJECTED` | 5 | 0 |
| Curva cartesiana | `REJECTED` | 2 | 0 |
| Secuencia | `ADMITTED` | 2 | 2 |
| Texto dinámico | `ADMITTED` | 2 | 2 |
| Macro, script y archivo inválido | `NOT_EVALUATED` | 0 | 0 |

En la secuencia, `g1 → g2` vincula el escalar y `g2 → g3` la lista. El único
diagnóstico de copia es `AUXILIARY_COPY_ADDRESS` para una dirección auxiliar
sin destino de primer nivel. `INPUTS` de la lista sigue `NOT_ESTABLISHED`.
El valor actual y el dominio observado se conservaron. Las dos ejecuciones
produjeron comparaciones y diagnósticos idénticos. La reapertura del ensayo
ordinario carece de correspondencia durable de objetos y conserva
`NOT_ESTABLISHED`.
