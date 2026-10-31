# CU7-I1-M08-S-R2 — candidato focal del puente de kernel

Estado: **CANDIDATO TÉCNICO PARA REVISIÓN AUTORAL; NO DESKTOP NI APROBACIÓN FINAL M08-S**.
Fecha: 2026-10-31. selfApproved=false. La decisión autoral autoriza el puente y su puerta
científica; la dirección normativa adoptada no autoriza editar todavía ADR aceptados.

## 1. Base, clasificación y cambios

Base exacta verificada limpia: 9e6abd1c1737a1324a9910c645565e5dd31c8074, árbol
cb64ec7fda7b838d6eb786c2591cb6ad4cae329b. Nueva rama independiente:
`codex/pre-g9b-cu7-i1-m08-s-r2-kernel`. Un solo worktree registrado. Referencias main y
origin/main conservadas en caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4; R1 conserva
3816029333e8035323f9a702e29b60a6ab7e19ce; R2/refinement conservados. No fetch ni push.

Clasificación: implementación focal del kernel compartido, con pruebas reales, registro
acotado de cobertura y documentación. Fuentes de verdad: código, contratos aprobados y
decisión autoral de admisión individual. [Plan previo](../architecture/pre_g9b_cu7_i1_r2_kernel_bridge_plan.md)
y [enmienda concreta candidata](../architecture/pre_g9b_cu7_i1_r2_kernel_normative_candidate.md).

| Archivo/grupo | Efecto y razón de ubicación |
|---|---|
| `locus/interaction/M08SnapshotPlacement2D.java` | frontera exclusiva M08-S, tipos de evidencia/permiso/vigencia, selección explícita y revalidación; requiere DAG/identidad compartidos |
| `locus/intersection/M08LocalAddressEvidence2D.java` | puente mínimo a los modelos intervalares privados reales; conserva su geometría/aritmética, sin duplicarla ni hacerla pública en Desktop |
| `LocusPointInteractionResolver2D.java` | auditoría package-private optativa de barreras; ruta pública, estados, cobertura y resultados históricos conservados |
| `LocusV2PublicOperations.java` | reutiliza el mismo constructor forward y ownership; handshake impide publicar una Choice fuera de su commit, sin fabricar un candidate R6 «mínimo» |
| `M08SnapshotPlacementKernelTest.java` | 24 casos nuevos sobre kernel real, además de los 52 R6 intactos |
| `geocedg/validation/pre-g9b-cu/m08s-r2-kernel-native-probe/` e init opt-in | fuente reproducible de los dos probes nativos, fuera del discovery normal; sólo lectura del original privado, no incluido en Git |
| registro/inventario JUnit y fixture de cantidades | sólo datos de nueva selección/PHASE y catálogos derivados por herramientas oficiales; sin cambiar runner o aceptación |
| guía de desarrollador y documentos CU7-R2 kernel | API experimental, matriz, comparación normativa, límites y propuesta Desktop posterior |

Los cuatro archivos productivos anteriores están bajo
`source/shared/common/src/main/java/org/geocedg/common/kernel/`; la prueba bajo
`source/shared/common-jre/src/test/java/org/geocedg/common/locus/`. No se modifica código
productivo Desktop, formatos, comandos, ADR aceptados, libro ni archivos históricos R1/R2.
Los cambios en el árbol heredado están restringidos a código GeoCeDG, con cabeceras EUPL.

## 2. Dictamen científico acotado

El mecanismo real de h **sí proporciona** la evidencia del primer perímetro. La sonda Java
carga el archivo nativo exacto, obtiene un programa certificado v1 y prueba enclosures de
valor/primer jet. No se infiere esa capacidad de una elipse ideal o de tests sintéticos.

Archivo: C:/Temp/TestM08-S.cedg, 28228 bytes, SHA-256
4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d, original inalterado.
No contiene ReferenceR1: la sonda crea expresamente
(.8976484384297626,1.7854818601338847). El archivo usa autoridad representada heredada.

Programa real: CIRCLE con radio capturado 1.2787102323238368, base (1,0),(0,1), centro I;
pasos PARALLEL, MEET, MIDPOINT. En el argumento exacto de R1
-6.407985253531479e-11, la cota de distancia cuadrada del puente es
1.0000000000000913e-4, estrictamente menor que el radio .1 al cuadrado. Una componente
del primer jet excluye cero en vecindad interior. Se conserva el argumento, sin redondearlo
a 0 ni transportar identidad por proximidad. La definición y la componente se revalidan.

Resultado histórico: UNRESOLVED_NUMERICAL_SEARCH, búsqueda BOUNDED_EVALUATOR_SEARCH,
getUniqueCandidate null. Resultado nuevo: EXPLICIT_LOCAL_SELECTION_REQUIRED, cobertura
global de la modalidad nueva NOT_ESTABLISHED, Choice con evidencia local adicional.
El enclosure tiene grado CERTIFIED_ERROR_BOUND de la configuración representada. No es
EXACT_ARITHMETIC, mínimo local/global, proyección exacta ni STRICT; el forward Java conserva
su garantía anterior. El permiso no se deduce de isEstablished del R6 ni de sus residuos.

La prueba nativa crea P por commit explícito, verifica su dirección e identidad, movimiento
de Q sin seguimiento, DAG sin Q añadido y sin otorgarle ID, guardado de una copia .cedg y
reapertura nativa, copia con ID/linaje nuevos y original intacto. La matriz compartida cubre
fuente y referencia caducadas, ABA de publicaciones de L, singularidad/fold, distancia,
seams, gaps, modelo insuficiente, presupuesto, cancelación, replay y Undo/Redo.

Dictamen recomendado: **FAVORABLE PARA LA PUERTA CIENTÍFICA DEL PERÍMETRO ACOTADO**, sujeto
a los resultados finales identificados en el índice de entrega. No es PASS de M08-S ni de
la interacción L→Q. El [detalle de pruebas y limitaciones](pre_g9b_cu7_i1_r2_kernel_bridge_matrix.md)
distingue evidencia nativa, fixtures compartidas, regresión y trabajo UI pendiente.

## 3. Límites, compatibilidad y decisión normativa

Primer perímetro: programas/splines que el modelo existente puede capturar, bajo autoridad
heredada, con componente y vecindad regulares interiores. Documentos constructivos sin
transferencia establecida se rechazan con AUTHORITY_NOT_ESTABLISHED; no se cambia su política.
Extremos/seams, modelo no capturable, barreras y trabajo agotado siguen rechazados.

La búsqueda sigue usando semillas R6 y sus barreras conservadoras. La implementación inicial
rechaza toda oferta nueva si una de las semillas retenidas no supera la prueba local.
Así, h con I como Q/radio 1 puede rechazar por semilla de frontera, aunque otras direcciones
sean matemáticamente válidas. No se interpreta como imposibilidad arquitectónica ni ausencia;
la prueba positiva de antecedencia usa spline/semejanza, y el caso I permanece como negativo.
Ningún rechazo nuevo prohíbe las rutas R6 anteriores que ya eran válidas.

Vigencia acotada: contexto de un hilo y valor de consulta inmutable; vínculo al registro y
fuente exactos, revisión publicada no reutilizada, autoridad, componente y Q actual al
confirmar. No se afirma detectar todas las escrituras invisibles posibles de GeoGebra ni
una continuidad dinámica de Q: el snapshot no es M08-D. Se evita reutilizar epochs de M09.
El Result es evidencia inmutable; Selection.getValidity informa CURRENT/STALE/CONSUMED.

La construcción guardada sigue siendo la forma forward aprobada, inputs de dirección
dedicados y rol interactivo anterior. No nuevo codec, versión o migración; documentos
históricos siguen su política. No identidad de raíz ni redefinición. El punto nuevo conserva
su ID al guardar/reabrir/Undo; copiar utiliza el remapeo/linaje normal. El host almacena un
Undo; el kernel no añade un segundo. No se ofrece rollback universal frente a callbacks o
escritores arbitrarios: se reutiliza la frontera forward existente y los rechazos de admisión
se producen antes de reservar/publicar, sin pretender resolver la transacción general M09.

La enmienda concreta se propone a ADR 0019 decisión 4/Evidence model, ADR 0041 decisiones
2/3 y especificaciones de interacción/M08/superficie pública. No se aplica aún. Conserva
todo resultado R6 y separa grados locales, cobertura y permiso. La limitación de autoridad
y el perímetro probado deben figurar en su aprobación, sin ampliar STRICT ni arrastre.

## 4. Ejecuciones de preparación y correcciones preservadas

Todas las ejecuciones Gradle usan los productores existentes bajo tools/agent/checks,
con --rerun-tasks, --no-build-cache, --no-daemon y toolchain auto-download deshabilitado.
Wrapper exit 0 no equivale a éxito: se inspecciona siempre inner_exit_code y JUnit.
Evidencias bajo `artifacts/pre-g9b-cu7-i1-r2-kernel/`:

| Carpeta | Resultado/interpretación |
|---|---|
| capture-probe | hijo 1 por acceso denegado al lock Gradle; entorno/permisos, no defecto científico |
| capture-probe-2 / capture-prerequisite-preserved | reintento gestionado hijo 0, captura real 1/1; XML original conservado por hash en native-test.xml |
| focal-shared-1 | 13/13, hijo 0; prueba temprana, no candidato final |
| focal-shared-2 | 73 casos, un fallo: rechazo tras carga era genérico; guardas reordenadas para validar registro antes de acceder al objeto desligado |
| native-bridge-1 | hijo 0, dos casos reales; creación/reapertura/copia del nativo y caracterización |
| runtime-prerequisites | 52 R6 pasan y dos nativos pasan; campaña conjunta hijo 1 por expectativa incorrecta de admisión de I/seam en una prueba de antecedencia |
| catalog-preparation/shared-executed | hijo 0, 7541 casos compartidos, sin fallos/errores; 24 casos nuevos pasan, incluidos antecedencia positiva y rechazo de seam separados |
| catalog-preparation/shared-discovery, desktop-discovery | discovery canónico --test-dry-run, no aceptación de producto; 6624 y 2282 identidades, sin probes privados en Desktop |

Se conserva el fallo de contrato del fixture I, sin rebajar el rechazo para pasar la prueba.
También se corrigió un problema del arnés: focal-shared-1 reutilizó el destino JUnit del
primer probe; antes de sobrescribirse se preservó el XML nativo con SHA-256
f42a4756886d0ba21e4a22d0575e26539eb815a7d2f786853d7076c000bb8292; el XML compartido se
archivó en focal-shared-1/junit. Las ejecuciones siguientes usan salidas propias. No se
modifica ni reutiliza como resultado nuevo ninguna evidencia R1 o candidato histórico.
Errores auxiliares de lectura (ruta/patrón o un directorio de salida del plan inexistente)
se corrigieron sin cambiar producto. El archivo R6 tenía finales de línea mixtos heredados:
se conservaron los de líneas intactas y se usó LF sólo en las líneas añadidas/modificadas.

## 5. Verificación del candidato exacto

VERIFICATION_CLASS=INTEGRATED_PHASE; frozenAtPhaseStart=true en el plan previo.
VERIFICATION_INFRASTRUCTURE_IMPACT=BOUNDED_CATALOG_REGISTRATION. No cambio del runner,
proyecciones, schemas, reglas de aceptación, allowances, cache policy ni bootstrap.

Preparación canónica: Read-VerificationJUnitFiles y Get-VerificationDeterministicHash
derivan 76 identidades (52 R6 + 24 nuevas) del XML compartido realmente ejecutado. Se registra
pre-g9b-cu7-i1-r2-kernel.shared, productor/proyección estándar y PHASE explícita
PRE-G9B-CU7-I1-M08-S-R2-KERNEL. update-verification-junit-inventory.ps1 actualiza discovery
de ambos módulos y final.shared desde la ejecución completa; se reencuaderna su hash único.
Fixture de cantidades: 73 selecciones / 75 PHASE. No identidades, ejecución o hash supuestos.

Plan PHASE resuelto COMPLETE: compilaciones, 76 shared, 31 Desktop R1 y 5 aislados.
INTEGRATION añade consumidores/constructores, modelos intervalares e intersecciones, identidad,
autoridad, DAG y codecs no cubiertos por el foco. No se ejecuta FINAL ni se genera recibo FINAL.
Comandos finales, sobre el commit limpio indicado en la entrega:

```powershell
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/pre-g9b-cu7-i1-r2-kernel/final/static
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts/pre-g9b-cu7-i1-r2-kernel/final/infra_unit
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU7-I1-M08-S-R2-KERNEL -LogDirectory artifacts/pre-g9b-cu7-i1-r2-kernel/final/phase
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/pre-g9b-cu7-i1-r2-kernel/final/integration
git diff --check
```

Probe nativo reproducible, adicional y explícito, sin cambiar la selección normal:

```powershell
& tools/agent/checks/gradle-command-evidence-producer.ps1 `
  -EvidencePath artifacts/pre-g9b-cu7-i1-r2-kernel/final/native/process.json `
  -GradleTask ':desktop:desktop:test' `
  -AdditionalArguments @('--tests','org.geocedg.desktop.Cu7M08SRealCaptureProbeTest',
    '--init-script','geocedg/validation/pre-g9b-cu/m08s-r2-kernel-native.init.gradle') `
  -OutputDirectory artifacts/pre-g9b-cu7-i1-r2-kernel/final/native
```

El init sólo añade la fuente bajo geocedg/validation en esa invocación y fija un destino
JUnit propio; no entra en el build/discovery normal ni altera wrappers o configuración.

El índice final `artifacts/pre-g9b-cu7-i1-r2-kernel/delivery.json` conserva commit/árbol,
veredictos, exits, hashes y cohortes de cada evidencia, incluido el probe nativo final.
No atribuir los resultados de preparación a un commit posterior ni convertir esta tabla
en aprobación. Los informes canónicos finales identifican el candidato exacto ejecutado.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: no nuevos runtimes, prerequisitos, JDK/Conda,
Gradle, instalación, packaging ni configuración. Acceso gestionado al caché existente,
sin corregir permisos/configuraciones ajenos. GUIDE_IMPACT=UPDATED:
docs/developer/geocedg_developer_guide.md explica la API compartida y sus límites reales.

## 6. Parada y propuesta Desktop, no ejecución

El candidato hereda todas las capacidades R1 por ascendencia y no sustituye aún su interfaz:
abrir la aplicación y usar el diálogo anterior conserva el rechazo histórico de h. El puente
se consume por API compartida en las pruebas; la nueva interacción requiere autorización propia.

Recomendar tras revisión: autorizar Desktop L→Q con política world explícita y accesible,
sin diálogo ordinario; confirmación contextual de una sola oferta local sin unicidad y
selector pequeño para varias. Mantener R6/SplineV2 y dirección explícita avanzada, compactar
su ventana sin eliminar validación/EN/ES. Propuesta y smoke futuro están en los documentos
R2 de interacción y matriz, con la precisión sobre I/seam indicada aquí. No smoke ejecutado.

M08-S sigue pendiente de smoke/aprobación final; ADR aceptados intactos. No implementar
M08-D, M09/A0, I2G/I3 ni drag general. DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG queda independiente,
CU10/integración editorial y DEBT-CU3-EXACT-TANGENCY-GENERALIZATION se conservan. Main, libro,
históricos intactos, sin publicación/tags/releases. Entregar candidato y detenerse.
