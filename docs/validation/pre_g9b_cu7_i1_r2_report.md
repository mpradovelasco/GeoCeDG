# CU7-I1-M08-S-R2 — expediente de preparación normativa

Estado final previsto: **CANDIDATO DOCUMENTAL/MATEMÁTICO PENDIENTE DE REVISIÓN AUTORAL**.
M08-S no es PASS — AUTHOR APPROVED. No implementación nueva; M09 sigue pendiente y bloqueado
para implementación productiva. `selfApproved=false`.

## 1. Base, alcance y artefactos

Solicitud clasificada como diseño/preparación normativa y caracterización matemática focal.
Capa afectada: documentación candidata del kernel compartido y una sonda matemática aislada
sin integración en el producto o en el registro de verificación. Fuente de verdad actual:
código R1 y contratos aceptados; las propuestas siguientes aún no los sustituyen.

Identidad de entrada verificada localmente antes de editar:

- R1: `3816029333e8035323f9a702e29b60a6ab7e19ce`;
  árbol `3d1dec62bd2c9e8ac2409a4a3a587bc0db09c10d`; worktree e índice limpios.
- Rama inicial `codex/pre-g9b-cu7-i1-m08-s-r1`; nueva rama independiente
  `codex/pre-g9b-cu7-i1-m08-s-r2-design`, desde esa base exacta.
- `main` y referencia local `origin/main`: `caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4`,
  antecesor de R1, `git merge-base --is-ancestor` exit 0. No contacto remoto ni promoción.
- Un único worktree registrado, el repositorio de trabajo. R1, main e historias M09 intactos.
- Archivo autoral sólo leído: `C:/Temp/TestM08-S.cedg`, SHA-256
  `4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d`, igual a R1.

Archivos nuevos, conjunto editable completo:

| Artefacto | Propósito |
|---|---|
| [diseño](../architecture/pre_g9b_cu7_i1_r2_local_direction_design.md) | hechos del código, alternativas, teorema, proveedores y caso nativo |
| [enmienda candidata](../architecture/pre_g9b_cu7_i1_r2_normative_amendment_candidate.md) | contrato tipado previo a APIs, textos exactos ADR/especificaciones y ubicación futura |
| [matriz y puertas](pre_g9b_cu7_i1_r2_matrix_and_gates.md) | 32 escenarios, evidencia real vs pendiente, incremento futuro y deuda de arrastre |
| este informe | base, trazabilidad, verificación y decisiones pendientes |
| [sonda racional](../../geocedg/validation/pre-g9b-cu/m08s-r2-mathematical-probe.py) | tres grupos de aserciones matemáticas reproducibles, sin runtime Java ni admisión |

No se editan `source/`, ADR aceptados, especificaciones normativas, formatos, registro o
selección PHASE, guías vivas, libro, configuraciones, cachés ni infraestructura. Los únicos
derivados nuevos están bajo `artifacts/pre-g9b-cu7-i1-r2/`, ignorados por Git.

## 2. Resultado científico y límites

Recomendación: **NEAR_REGULAR_ADDRESS**, dirección elegida explícitamente en una carta C1
regular e inyectiva, con cota exterior de distancia a q y contexto vigente. No exige un
mínimo de distancia. La separación de carta no se presenta como aislamiento de raíz
estacionaria. La propuesta requiere que el autor apruebe expresamente esa diferencia.

Demostrado en matemáticas: un signo estricto uniforme de una componente de F' establece
regularidad e inyectividad local; una caja exterior de F(U) dentro del radio da la cercanía
sin minimizar. La familia elíptica con r entre 1.27 y 1.29 satisface esas condiciones en
[-1/1024,1/1024] para los valores q/radio del caso nativo. La sonda produce cotas racionales
exactas; no estima cos/sin en flotante para simular una certificación.

Observado en el código: la evidencia R6 usa muestreo/diferenciales puntuales con grado
numérico, no ese testigo uniforme. Ya hay modelos intervalares de primer jet en
intersecciones, privados a ese paquete, y captura real v1/v2; no existe la nueva frontera
de selección. El resumen de barreras necesario no puede inferirse del reason histórico.

Evidencia Java histórica conservada: R1 reproduce UNRESOLVED en h tanto en main anterior
como en el candidato; forward explícito válido. Es limitación heredada. El original no se
ha vuelto a ejecutar ni se ha modificado. Referencia primaria: informe R1 y
`artifacts/pre-g9b-cu7-i1-r1/diagnostic-author-current-2/`, `process.json` con
`inner_exit_code=0` y JUnit del caso nativo. Es evidencia R1, no validación de este diseño.

**No establecido:** el puente entre la familia matemática y la captura real de h,
constantes/bases/parametrización reales y autoridad activa; prueba exterior en esa captura;
seguridad/vigencia completa de la frontera nueva; comportamiento integrado, fallos, Undo,
native .cedg y compatibilidad de la vía nueva. No se admite h/ReferenceR1 por este informe.
Un certificado representado no se transfiere por proximidad a la autoridad constructiva.
No se afirma exactitud de las coordenadas Java, mínimo local/global ni STRICT.

La alternativa que exige mínimo aislado sigue disponible como diseño distinto, con prueba
de existencia/aislamiento de la raíz estacionaria. No es lo que demuestra una etiqueta R6
ESTABLISHED ni lo que necesita la construcción forward estática por sí misma.

## 3. Fuentes inspeccionadas

Autoridad: `AGENTS.md`; `.github/copilot-instructions.md` ausente (confirmado, sin sustituto
inventado); prompt R6 histórico leído sólo como contexto de esa fase, no como autorización R2.
Contrato operativo: `geocedg/specs/operations/verification-levels.md`, registro y
`documentation-maintenance.md`; `tools/agent/verify.ps1` y productor Python existente.

Contrato geométrico: ADR 0019 y ADR 0041; M08 prospectivo aprobado;
`locus-v2-point-interaction.md`, `locus-v2-public-surface.md`,
`locus-v2-certified-construction-model.md` (incluido §12 de autoridad constructiva),
`locus-v2-constructive-authority.md`; propuesta local e informe R1. No se cambia su estado.

Implementación real: resolvedor, resultado, evidencia local y `LocusSnapshotPlacement2D`;
`CertifiedAffineLocus2D`, `PiecewisePolynomialLocus2D`, evaluador reconstruible y su captura;
diferenciales métricos; `CertifiedIntervalCurveModel2D`, `ConstructionIntervalModel2D`,
`SplineIntervalModel2D`; `GeoLocusV2` y `LocusV2PublicOperations`; pruebas R1 y trazas guardadas. Rutas completas relativas a la raíz
del kernel figuran en la tabla del diseño. Las búsquedas iniciales en un directorio de tests
equivocado y un subpaquete inexistente dieron errores de ruta de rg; se corrigieron mediante
inventario de archivos. No son defectos del producto ni resultados de pruebas.

## 4. Verificación acotada y evidencias

`VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY` para el candidato de diseño; no cambio
ejecutable del producto. La sonda es evidencia matemática focal suplementaria, no nueva
selección de aceptación, baseline numérico normativo ni infraestructura ejecutable canónica.
`VERIFICATION_INFRASTRUCTURE_IMPACT = NONE`: no se modifican wrappers, registro, selección,
orquestación, cachés, schemas ni clasificación de aceptación.

`BOOTSTRAP IMPACT — NO CHANGE REQUIRED`.
Rationale: inspeccionados contrato de perfiles y productor Python; no cambian prerrequisitos,
JDK, Conda, versiones, entradas Gradle, herramientas, packaging ni supuestos de bootstrap.
No editar configuración para solucionar detección del entorno en esta sesión.

`GUIDE_IMPACT = NONE`.
`GUIDE_JUSTIFICATION =` documentos candidatos y matemáticas aisladas; ningún comportamiento,
comando, interfaz o garantía vigente cambia. Si se aprueba implementación, actualizar las
guías vivas para la oferta local y confirmación, preservando la integración editorial CU10.

Sonda reproducible con wrapper existente, desde la raíz del repositorio:

```powershell
pwsh -NoProfile -File tools/agent/checks/python-check-evidence-producer.ps1 `
  -ScriptPath geocedg/validation/pre-g9b-cu/m08s-r2-mathematical-probe.py `
  -EnvironmentPrefix C:\DesarrolloyDatos\Areas\Working\Python\cedg_env `
  -EvidencePath artifacts/pre-g9b-cu7-i1-r2/mathematics-explicit/process.json `
  -OutputDirectory artifacts/pre-g9b-cu7-i1-r2/mathematics-explicit
```

| Ejecución | Resultado real |
|---|---|
| intento inicial sin prefijo, `mathematics/process.json` | wrapper exit 0, pero LAUNCH_FAILED, sin exit hijo: detección Conda no disponible; NO prueba completada |
| segundo intento con prefijo existente verificado en R1 | wrapper exit 0, COMPLETED, identidad exit 0, hijo exit 0, tres grupos de aserciones completados; stderr vacío |

Se conserva el intento fallido sin sobrescribirlo. Es un problema de resolución del entorno,
no un fallo de la matemática ni del kernel. Python 3.12.13 de cedg_env; sólo `fractions/json`
en la sonda. La carga mpmath del productor es su comprobación de identidad, no un oráculo
numérico de esta demostración. No se instala ni modifica nada fuera del expediente.

Verificación documental canónica a ejecutar sobre el commit limpio de este candidato:

```powershell
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile STATIC `
  -LogDirectory artifacts/pre-g9b-cu7-i1-r2/final/static
git diff --check
```

El resultado y SHA/árbol exactos se entregan junto al candidato, con los logs del perfil.
No atribuir a STATIC una prueba de admisibilidad o integración. PHASE, INTEGRATION, FINAL,
Gradle y pruebas Java nuevas no se ejecutan en esta preparación normativa. No hay recibo
FINAL ni declaración de smoke. La matriz documenta el trabajo futuro, no resultados ficticios.

## 5. Decisiones requeridas y parada

1. Aprobar o rechazar la modalidad de dirección próxima en carta regular **sin** mínimo;
   si se exige mínimo, elegir la puerta adicional y mantener los casos actuales rechazados.
2. Aprobar las enmiendas candidatas a ADR 0019/0041 y especificaciones y sus dimensiones
   separadas, preservando R6, Q transitorio, autoridad constructiva y STRICT.
3. Confirmar primer perímetro v1/v2 interior, rechazo de extremos/seams no certificados,
   barreras y presupuesto; permitir sólo puente interno mínimo a modelos existentes.
4. Autorizar por separado P1..P4 de la matriz, con G1/G2 demostradas antes de ofrecer nada.
   Recomendación favorable condicionada a esas puertas, no permiso para programar ahora.
5. Mantener propuesta independiente `DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG`, sin fase ni fecha
   aprobadas y sin bloquear M08-S. La creación no demuestra ni amplía arrastre general.

No se inicia M08-D, M09/A0, I2G/I3 ni reconstrucción general. CU10, integración de la guía y
`DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` intactos. No publicación, main, tags, releases,
paquetes ni libro. Entregar este candidato para revisión y detenerse.
