# GeoCeDG 1.2.0 — Registro de ejecución R13

**R13 COMPLETED — FAILED — CORRECTION BUDGET 13/13 EXHAUSTED.**

El foco terminó naturalmente: 265 casos, 262 aprobados y tres fallidos,
sin errores ni omisiones, código interno 1. La revisión independiente del
resultado confirma defectos del registro de imágenes durante la recuperación.
No se aplica una nueva corrección. El [informe terminal](geocedg_1_2_0_r13_terminal_report.md)
prevalece sobre las previsiones históricas que se conservan a continuación.

Clasificación: depuración e implementación en Desktop. La fuente de autoridad
es la [propuesta R13](../architecture/geocedg-1.2.0-r13-exact-image-recovery-proposal.md),
SHA-256 `e28f598beb6f7fa3a0e95c4259f9aef43325d054b66cacd31d222d061d207ca5`,
autorizada expresamente después de su revisión independiente. Su versión
aprobada se conserva sin reescribirla. El [resultado R12](geocedg_1_2_0_r12_blocker_report.md)
permanece fallido: 253 casos, 247 aprobados y seis fallidos.

## Disposición autoral y límite

El autor autoriza una ronda adicional R13, exige completar las actividades
restantes aplicables, entregar el informe final y el resumen por este hilo y
terminar tanto si se satisface la ronda como si no. El límite queda en trece
rondas. La posibilidad mencionada por el autor de considerar posteriormente
una corrección mínima y segura no autoriza una R14 ni una ampliación de R13.
No se solicita aceptación de un candidato sin las puertas exigibles.

## Alcance y contratos conservados

Dos fuentes productivas: `GeoCeDGDocumentInsertion.java` y `AppGeoCeDG.java`.
Una clase de pruebas: `GeoCeDG120ClipboardImageRecoveryTest.java`. Se conserva
la selección R12 completa y se añaden oráculos específicos para JavaScript.
No se modifica kernel, R10, I2, UndoManager, codecs, perfil, adquisición
nativa del portapapeles ni la autoridad de la rotación matemática.

La corrección captura la cadena original y acota la lectura nativa completa
y su restitución a un scope propio de la misma recuperación. Preserva los
bytes, el estado JavaScript inicializado o pendiente y los callbacks sin una
evaluación adicional. La rama con companion, las lecturas FILE ordinarias y
las ediciones de biblioteca conservan su comportamiento. Los certificados
de recursos, documento, defaults e historial mantienen su igualdad estricta.
Los dos oráculos numéricos conservan ambas comparaciones completas de estado;
su sonda debe restituir la metadata que ella misma cambia.

## Revisión y validación previstas

La revisión de diseño externa está en
`geocedg-120-clipboard-image-design/r13-design-independent-review.md`, SHA-256
`846c01f665252e99e260d687975f453554a85b595a1d149aeabd58a282232738`.
Una segunda lectura del scope está en `r13-js-scope-review.md`, SHA-256
`cfc94835ea8a651cfebd3426f5506d93a623a0ce135e32d010c6219afdf6058e`.
Ninguna revisión estática acredita ejecución o aceptación.

El parche productivo queda en dos fuentes, con SHA-256 de
`GeoCeDGDocumentInsertion.java`
`990f660304daf66845287ddc20e854e930bae6f002b5b39ff09a6d0575651fb7`
y `AppGeoCeDG.java`
`5e6c3c900eb54f796ca1a7ae4219771b54f660a40f0fd532bf4b9d0deff9b8d2`.
La clase de recuperación queda en
`bf71a5c5277f1e9a386570caaba2276bdb677e7a793be19589ddc9b48e4eb134`;
InsertionTest no cambia. Se definen 68 invocaciones de imágenes: 56 anteriores
conservadas y doce nuevas. Ese recuento estático no es un resultado JUnit.

La revisión del parche y los oráculos está en
`r13-implementation-independent-review.md`, SHA-256
`ee56e73510e1415e1da1c0a0e62d9bee8cf698fa41bff7db5f52c551d784668d`.
La revisión previa al foco completó el oráculo de fallo real de restitución
y eliminó una restricción sobre el número total de errores suprimidos que
no exigía el contrato: se conservan dos errores OWNER distintos y toda la
cadena, incluidos otros diagnósticos de limpieza. No cambió el producto ni
se ejecutó una campaña durante esas revisiones de implementación inicial.
El dictamen final del delta está en
`r13-restitution-oracle-independent-review.md`, SHA-256
`04ecc1250d6d6d904699932f876a9c57a8618640bdcb4371a0158a4529373d85`.
Ambos se conservarán con el input del foco.

La revisión de bootstrap concluye **sin cambios**: se usan las mismas APIs
Desktop, Rhino y herramientas Windows ya requeridas, sin dependencia, runtime
o prerrequisito nuevo. La infraestructura canónica de verificación permanece
intacta. Las guías ES/EN y su cobertura de imágenes tienen un borrador externo
que aún no se presenta como capacidad validada.

El autor ha indicado que otro agente usa Computer Use y avisará cuando estén
libres escritorio y portapapeles. Se espera ese aviso para los tests nativos
que usan el portapapeles y el smoke visual. La compilación aislada, sin tests
ni acceso gráfico, puede prepararse de forma independiente.

Antes del foco se revisarán de forma independiente el parche y los oráculos,
se registrarán hashes y se congelarán las fuentes. Se usará el productor
canónico, salida nueva y revisión de sus XML y código interno. Un resultado
fallido se conservará sin relabel ni repetición oportunista.

Después se completarán las actividades aplicables: Computer Use, guías y
cobertura, identidad limpia del candidato, inventarios derivados, controles
previos a congelación, tres PHASE afectados, INTEGRATION, FINAL, rendimiento
comparativo e INTERNAL/NC con app-image, ZIP, MSI y EXE y verificación Windows.
Una puerta no satisfecha se registrará como bloqueo; no se sustituye por un
foco aprobado. La fecha autoral de producto sigue siendo `2026-11-16`.

El informe final distinguirá resultados logrados, pendientes y bloqueos y
conservará commit/árbol cuando exista candidato, rondas, evidencias, guías,
expediente editorial y smoke selectivo. No se promueve main, crean tags ni
publican binarios sin la decisión autoral posterior.
