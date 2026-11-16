# GeoCeDG 1.2.0 — Resultado y bloqueo tras R12

**TECHNICAL CLOSEOUT BLOCKED — CORRECTION BUDGET EXHAUSTED (12/12).**
R12 fue autorizada expresamente para catálogo, defaults y recuperación de
imágenes. Se implementó, se revisó independientemente y su foco terminó con
fallos. No se ha aplicado una corrección posterior ni repetido la selección.
Este informe no califica un candidato ni sustituye la aceptación autoral.

## Ejecución y procedencia

```powershell
pwsh -NoLogo -NoProfile -File tools/agent/checks/gradle-test-evidence-producer.ps1 -RepositoryRoot . -InventoryPath artifacts/agent/geocedg-1.2.0/r12-focused-inventory.json -SelectionId clipboard.r12 -EvidencePath artifacts/agent/geocedg-1.2.0/r12-focal-desktop-01/evidence.json -OutputDirectory artifacts/agent/geocedg-1.2.0/r12-focal-desktop-01
```

El proceso Gradle terminó naturalmente con salida **1**, en 383 814.7128 ms.
No hubo timeout ni interrupción. Los veinte XML contienen **253 casos: 247
aprobados y seis fallidos, cero errores y cero omisiones**. Las dos clases de
imágenes contienen 56 casos, 50 aprobados y seis fallidos; la clase de
inserción pasa 13/13 y la de recuperación 37/43. Las cuatro clases del cono y
de vistas pasan 14/14. Todos los demás casos seleccionados pasan, incluidos
catálogo, perfil, workspace y consumidores de Undo; esto es evidencia focal,
no aceptación global.

Se comprobaron las 70 fuentes del manifiesto antes de actualizar la
documentación: ninguna cambió durante la ejecución. El estado probado sigue
siendo desarrollo sin commit sobre `6f1d357ca2224dedd0e84fda0faed2d600154bab`,
árbol `b6708de39577009752d45e0b73e9d44d94d695f9`, candidato retirado. No se
ha fijado un nuevo candidato exacto ni se afirma un worktree limpio. El modelo
original conserva SHA-256
`4cde946eba57bfd36d531c677dc9f622b9036d6e95cd92941311b51d5f786a01`.

| Evidencia | SHA-256 |
|---|---|
| [Input de fuentes](../../artifacts/agent/geocedg-1.2.0/r12-focal-desktop-01/source-before-runtime.json) | `86e05027b94c96257b733f67e13d655b55033040249dba3e8e5743491f2dc5a2` |
| [Productor](../../artifacts/agent/geocedg-1.2.0/r12-focal-desktop-01/evidence.json) | `d4023c85577f7d23e8c8691ff5999f6f3e6e289ea9890c1ecd16a4c9e3cf2a55` |
| [Recuento y disposición](../../artifacts/agent/geocedg-1.2.0/r12-focal-desktop-01/diagnostic-disposition.json) | `ba8c999bf952e778c1c325e960cbed4569d46e12c924ff610df9c73f597c0bee` |

Los logs y XML están junto a esas evidencias. Los directorios temporales de
JUnit se conservaron deliberadamente para contrastar archivos nativos,
defaults y campos pasivos. El [resultado R11](geocedg_1_2_0_r11_blocker_report.md)
permanece histórico y no se reescribe.

La revisión terminal independiente está en el expediente externo
`geocedg-120-clipboard-image-design/r12-terminal-independent-review.md`,
SHA-256 `f3ef6c3db0ee23979d817f1064cbf9dcc3d7d3c2a9111d93833701cec57ebb64`.
Confirma los veinte XML, las 70 fuentes sin drift antes de las ediciones
documentales, el original, los pins R10 y la diferencia de miembros nativos.
Los diagnósticos separados de producto y oráculo se conservan en ese mismo
expediente: `r12-runtime-diagnosis-scientific/diagnosis.md`, SHA-256
`bd42dbc7b7ea2e59140a34f11518891804b50dbb4685f50f94bfcccfabb5751d`,
y `r12-dynamic-probe-diagnosis/diagnosis.md`, SHA-256
`fd063f6ac0f30f039f71fb8268c8873886b51d7013ecf5bcb9f866861fea7491`.

## Clasificación causal

| Casos fallidos | Diagnóstico | Evidencia y límite |
|---|---|---|
| Dos variantes de `aDynamicDefaultStepIsRefusedBeforeOrDuringTheDialogWithoutLosingTheExternalLink` | Desajuste del oráculo. El rechazo tipado, ausencia de loader, preservación de instancia y primera igualdad completa pasan. Después, la propia sonda llama `GeoNumeric.setValue`, que elimina definición y valor exacto; devolver el valor numérico anterior no restaura esa metadata. La igualdad final de XML falla tras la edición del test. | Stack en línea 177 y fuente de `GeoNumeric.setValue`/`GeoElement.getExpressionXML`. No hay un diff de XML capturado para este par y no se presenta como tal. Debe conservarse el oráculo completo inmediatamente después de la operación y corregirse la sonda, sin relajar la igualdad. |
| Dos variantes de `failedSubtractionOrCertificateUsesTheNativeReloadAndDeclaresItsReset`, mutación inesperada del default y fallo `beforeAttach` | Defecto productivo de recuperación exacta. La recarga nativa completa puede cambiar el miembro JavaScript del archivo, por lo que el certificado de recursos rechaza la recuperación y devuelve `SERIOUS_RECOVERY_FAILURE`. | Los cuatro stacks llegan al certificado con diferencias de documento vacías. En el caso de default inesperado se capturaron ambos archivos: construcción y defaults 2D/3D son idénticos; solo cambia `geogebra_javascript.js`, de 23 a 24 bytes por un LF añadido. Los otros tres comparten ruta y síntoma, pero no se dispone de su diff de miembros retornados. |

En el par nativo capturado, el JavaScript pasa de SHA-256
`3aa5d8316bbad619954d1c93da49792492f64eaed2efe3aada9985cce9c75bf2`
a `90d66739c7658fadada2635213419190cd92f745f4950b71c6573970a8bd1c98`.
La fuente confirma que `MyXMLioJre` usa `StreamUtil.loadIntoString`: lee líneas
y añade LF a cada una. El writer emite después esa cadena UTF-8. Los campos
pasivos de defaults capturados se recuperan según el contrato de recarga
completa; la discrepancia observada no es corrupción de PNG ni pérdida de
autoridad geométrica. No se clasifica como fallo de entorno.

El certificado estricto ha evitado declarar una recuperación inexacta como
válida. No se autoriza excluir JavaScript del digest, normalizar su contenido,
ignorar el fallo ni convertir una recuperación seria en aprobación.

## Estado y trabajo restante

El diseño y la implementación R12 recibieron revisión estática independiente
antes de la ejecución. Ese dictamen no es aprobación de runtime; los fallos
anteriores impiden cerrar la ronda como satisfecha. No se modifica kernel,
R10, I2, codecs ni lector de portapapeles para ocultar el resultado.

El límite de doce rondas está consumido. Una corrección adicional requiere
autorización expresa según el mandato original. Quedan pendientes el nuevo
foco y su revisión, Computer Use de imágenes, integración de guías ES/EN y
cobertura, fijación del candidato e inventarios derivados, PHASE afectados,
INTEGRATION, FINAL, rendimiento del árbol definitivo y empaquetado completo
INTERNAL/NC con comprobación en Windows. Las guías de imágenes y el arnés de
rendimiento tienen preparación externa; no se presentan como validaciones.

La fecha autoral `2026-11-16` está disponible. El bloqueo de empaquetado es
técnico, no `PACKAGING PENDING AUTHOR INPUT`. Los paquetes del candidato
retirado no representan este árbol. No se ha promovido main, creado tags,
publicado binarios ni modificado el manuscrito del libro.

## Propuesta preparada para decisión autoral

La [propuesta R13](../architecture/geocedg-1.2.0-r13-exact-image-recovery-proposal.md),
SHA-256 `e28f598beb6f7fa3a0e95c4259f9aef43325d054b66cacd31d222d061d207ca5`,
limita la corrección a dos fuentes productivas Desktop
(`GeoCeDGDocumentInsertion` y `AppGeoCeDG`) y la clase de tests de recuperación.
Conservaría la cadena JavaScript exacta dentro de un scope propio de la misma
recuperación, sin ejecutar de nuevo el script ni borrar su estado inicializado
o pendiente. El setter nativo por sí solo vuelve a evaluar código; por eso se
requiere este límite explícito. Los consumidores FILE ordinarios y los demás
incrementos conservan su comportamiento. Los dos oráculos numéricos mantendrían
ambas comparaciones completas de estado y restituirían la metadata que cambia
su propia sonda.

La revisión independiente de ese diseño no detectó bloqueantes estáticos:
`geocedg-120-clipboard-image-design/r13-design-independent-review.md`, SHA-256
`846c01f665252e99e260d687975f453554a85b595a1d149aeabd58a282232738`.
No se ha implementado, ejecutado ni autorizado R13. El dictamen de diseño no
aprueba su runtime ni aumenta el presupuesto correctivo.
