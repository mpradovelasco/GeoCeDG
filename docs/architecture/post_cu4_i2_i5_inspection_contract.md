# POST-CU4 · Contrato candidato de inspección y revisión

Este texto conserva el contrato de la inspección I2 mediante archivos.
La revisión integrada del receptor vivo se rige por la
[normativa experta v1](../../geocedg/specs/ui/expert-document-insertion-candidate.md).

I2 pertenece a la capa de aplicación Desktop. Lee las construcciones y sus
algoritmos con las APIs del kernel y usa el lector, el escritor y el buffer de
inserción nativos. El DAG, sus aristas y la interpretación geométrica pertenecen
al kernel. Los identificadores de una observación solo sirven para citar objetos
dentro de una instantánea. El formato nativo, las reglas de P4, B2 y el contrato
de identidad siguen regidos por sus especificaciones vigentes.

El incremento tiene alcance integrado Desktop. La interfaz I2 se invoca
por separado. El arranque, los formatos y la inserción ordinaria conservan sus
rutas. La verificación focal incluye I0, I2 y regresiones colindantes F3/B2.

## Frontera de I2

La unidad de entrada es un par de archivos `.cedg` o `.ggb` capturados en bytes.
El receptor representa la instantánea de archivo examinada. Los cambios de una
ventana abierta que aún no se hayan guardado están fuera de esa instantánea. El
trabajador Desktop se invoca en una JVM nueva, con preferencias portátiles y
temporales de la invocación. Carece de referencias al host activo. Lee una vez
cada entrada, registra SHA-256 y conserva el original durante el análisis. Los
ensayos usan un receptor desechable. El informe incluye los hashes y la revisión
de la política. Una futura captura del receptor vivo necesitará un adaptador de
captura sin efectos laterales y un certificado del estado de sesión.

La preinspección admite como máximo 8 MiB comprimidos, 8 MiB expandidos, 4 MiB
por miembro y 32 miembros. Acota profundidad, nodos, atributos, objetos y
longitud de secuencias. El XML debe producir los mismos eventos con StAX y
con el analizador sintáctico nativo antes de entrar al lector del kernel.
Tags, atributos, expresiones y comandos evaluables tienen un perímetro pasivo
explícito. Macros, scripts, listeners, animación, recursos externos y
construcciones no auditadas se diagnostican antes del lector. Solo los miembros
admitidos pasan al runtime. La envoltura de proceso aísla la sesión, acota memoria
y tiempo y descarta temporales. La frontera frente al código no confiable es la
preinspección, no una garantía de confinamiento del sistema operativo.

## Estados y correspondencia

Cada estado describe un dominio de observaciones. `SOURCE` y `RECEIVER` son
lecturas nativas independientes. `TRIAL` es una copia F3 realmente ejecutada en
el receptor desechable. `REOPENED` es la lectura del ensayo escrito por el
escritor nativo, tras volver a aplicar la barrera pasiva. Si la admisión o la
seguridad impiden un ensayo, sus comparaciones quedan `NOT_EVALUATED`. Una
refusal P4 y una imposibilidad de evaluación son diagnósticos distintos.

La correspondencia declara el origen de su prueba. Durante un ensayo se usan
referencias de objeto retenidas y el mapa de copias entregado por el buffer
nativo bajo el alcance de esa operación. Tras reabrir se usan únicamente IDs
durables adjuntos por el registro nativo, dentro del mismo UUID de documento
confirmado. Los rótulos e índices se exponen como localizadores. Un homónimo o
una coincidencia de coordenadas carece de autoridad. Una arista compara la
entrada en la misma posición del productor mediante correspondencia de sus
extremos. Si faltan extremos demostrados, se informa la incertidumbre aunque
coincida el tamaño de la lista. Los objetos sin correspondencia demostrable no
cuentan como omisiones por ese solo motivo.

El informe tipado conserva valores anteriores y posteriores, estados, objetos,
evidencias y causas, con comparaciones separadas para identidad, clase y
productor, aristas, definición textual, valores geométricos medidos y campos de
presentación. `VERIFIED_UNCHANGED` y `VERIFIED_CHANGED` solo se aplican a la
propiedad indicada. `KNOWN_OMISSION` requiere ausencia demostrada. Se distinguen
`NOT_ESTABLISHED`, `NOT_EVALUATED`, `INVALID` y `TECHNICAL_FAILURE`. Por ejemplo,
dos curvas que imprimen la misma fórmula no acreditan equivalencia paramétrica.
Las muestras son de estado presente, sin equivalencia dinámica, métrica global,
tolerancia inferida ni remapeo de identidades inventado. Los recursos rechazados
siguen constando en el inventario. La publicabilidad es una decisión separada.

## Consumidores posteriores

| Incremento | Interfaz y evidencia que deberá consumir |
|---|---|
| I3 | Proyección de estados, correspondencias, cambios y omisiones del informe. Una revisión futura vinculará selección y consentimiento al hash del informe, a la revisión de política y a las instantáneas exactas. Una propiedad incierta conserva su estado al proyectarla. |
| I4 | Plan transaccional y prueba de vigencia, con identidad de instancia del receptor, revisión, UUID, bytes, recursos, política y requisitos de recuperación. Deberá volver a capturar y comprobar bajo la barrera del EDT. Historial, archivo y estado guardado forman parte del certificado de sesión. La oferta caduca por sustitución, edición, cambios de recursos o de política y consumo previo. |
| I5 | Comparadores y capacidades auditables por familia y por versión. Cada política declara presupuestos, degeneraciones, pérdida admisible, cobertura de reconstrucción y persistencia. La recomendación de guardado deriva del resultado transaccional y su recuperación y es una acción independiente. |

Para I4 se revisará la reutilización de la barrera y de la instantánea P7 de
F3. Antes de autorizarla se requieren captura viva sin efectos, procedencia
persistente para las familias que la necesiten, política explícita de pérdidas,
revalidación de ofertas e inyección de fallos de publicación y recuperación.
I2 carece de transacción experta y de una oferta publicable.

## Invocación en Windows

El lanzador usa un runtime Desktop que contenga el candidato, por ejemplo el
`lib/*` absoluto de su distribución Gradle instalada, y el JDK Desktop 25.
El directorio de salida debe ser nuevo.

```powershell
./tools/agent/probes/post-cu4-i2/inspect-document.ps1 `
  -Source <fuente.cedg> -Receiver <receptor.cedg> `
  -Jdk <ruta-jdk-25> -Classpath <ruta-distribucion-desktop/lib/*> `
  -OutputDirectory <directorio-nuevo>
```

`report.json` contiene el resultado, `stdout.log` el resumen y
`invocation.json` el estado del proceso. El lanzador acota el trabajador a
512 MiB de heap y 60 segundos. El código 0 significa que hubo informe,
incluidos bloqueos previstos de seguridad y negativas P4. Los cambios
sustantivos se comparan por campos y testigos. El SHA del ZIP generado durante
una reapertura corresponde a esa ejecución y puede variar por los metadatos
temporales del escritor. Los hashes de las dos entradas conservan su significado
byte a byte.

El worker usa preferencias portátiles propias. Una diferencia de capa o
visibilidad de un ensayo bajo ese perfil se conserva en el informe. El
dictamen P4 representa la evaluación de admisión en ese entorno. La
comparación se limita a los campos observados y no equivale a un certificado
F3, B2, de recuperación o de equivalencia dinámica. Para documentos antiguos
sin UUID persistido el emparejamiento durable entre reaperturas se mantiene
sin establecer.

## Matriz focal

- Construcción elemental admitida y receptor con homónimo. Comprobar copia
  aislada, aristas demostradas, colisión L1 y archivo receptor intacto.
- Arco, curva cartesiana, secuencia acotada y texto dependiente. Comprobar
  productor, insumos y veredicto P4 nativos. Las familias excluidas pueden ser
  inspeccionables y dejar el ensayo sin ejecutar.
- Macros, script, recursos, archivo inválido y variantes adversarias de la
  envoltura. Comprobar inventario y bloqueo previo a la evaluación.
- Comparar identidad durable comprobada, dependencia sustituida y
  correspondencia incompleta. Comprobar que una ausencia ambigua y una
  equivalencia geométrica sin prueba conservan su estado de incertidumbre.
- Repetir la invocación sobre bytes idénticos. Comprobar las observaciones
  sustantivas y la identidad de los archivos. Separar errores de análisis,
  carga, reconstrucción y límites del trabajador.
