# PRE-G9B-CU3 — Registro de T6: reutilización de los encierros de la fuente

| Campo | Valor |
|---|---|
| Carácter | Candidato técnico optimizado, entregado para la revisión del autor. No es una aprobación ni un cierre; `CU3` no está `PASS — AUTHOR APPROVED` |
| Autoridad | instrucciones autorales «PRE-G9B-CU3 — Publicación, auditoría independiente y optimización focal T6» y «PRE-G9B-CU3 — Resultado de auditoría independiente y activación de T6», de 2026-10-26 (§1), y la elección autoral sobre el alcance tras la caracterización (§3) |
| Referencia | candidato `68bfd7640b12c25de0122a666d24f355488d0e22`, publicado con su descendiente documental en `b1813da1` ([registro de T1 a T3](pre_g9b_cu3_performance_optimization_record.md)) |
| Candidato T6 | `3f0968385920e36b2b48191a4f6e736c4fc11c09` (árbol `d3502024fb91ee60a125f3f2a3b34701aafc29c2`), rama local `phase/pre-g9b-cu3-implementation`, sin publicar |
| Evidencia | [`pre-g9b-cu3-enclosure-reuse-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-enclosure-reuse-evidence.json) |
| `selfApproved` | `false` |

## 1. Autorización y secuencia

1. **Publicación.** La rama `phase/pre-g9b-cu3-implementation` se publicó por avance rápido, sin
   forzar, de `7f1a81ae` a `b1813da1` (árbol `36b81d1c`). Solo cambió esa referencia: 87
   referencias, 13 ramas y 73 etiquetas antes y después, `main` en `e6c908f2`, ningún PR y
   ninguna versión nueva. La auditoría local, sin seguimiento, está en
   `artifacts/agent/pre-g9b-cu3-optimization-publication-audit/`.
2. **Auditoría independiente.** El autor comunicó su resultado favorable sobre `b1813da1` y activó
   la autorización condicionada de T6: «AUDITORÍA FAVORABLE — ACTIVACIÓN DE LA AUTORIZACIÓN
   CONDICIONADA DE T6».
3. **Alcance de T6.** Reutilizar solo encierros rigurosamente equivalentes de la misma fuente,
   revisión, caja, parametrización y contrato de derivación. Caracterizar antes de tocar el
   producto, y detenerse si la ganancia no es significativa. No modificar decisiones, autoridad,
   identidades, admisibilidad, presupuestos, persistencia ni algoritmos heredados. Clase
   `INTEGRATED_PHASE`.
4. **Fuera de alcance:** T5, T7, `CU3-B`, `main`, los contratos de identidad, la autoridad
   constructiva predeterminada, las fases posteriores y `geocedg_book`.
5. **Deuda conservada:** `DEBT-VERIFIER-PWSH-769-TESTJSON` (§8).

## 2. Caracterización previa sobre `68bfd764`

Con la sonda versionada `run-cu3-enclosure-reuse.ps1`, en un arrastre de `H` del modelo adoptado
(ms; el arrastre de `B` es análogo):

| Consulta | Cajas | Encierro completo | Punto trazado | Objetivo | Punto trazado con los valores independientes dados | Objetivo con ellos |
|---|---:|---:|---:|---:|---:|---:|
| `o_1·h_2` | 504 | 33,7 | 18,7 | 21,3 | 10,1 | 0,29 |
| `e_1·n` | 283 | 13,7 | 9,6 | 6,3 | 4,8 | 0,14 |
| `e_1·c_1` | 1 760 | 63,4 | 58,2 | 23,2 | 30,0 | 0,82 |
| `e_1·r` | 932 | 45,8 | 31,0 | 21,1 | 16,1 | 0,46 |

- **Fuente frente a objetivo.** El punto trazado (la fuente) y el objetivo se evalúan por separado;
  el encierro completo los comparte en un solo evaluador. El objetivo no depende del conductor en
  ninguna de las cuatro consultas, pero se recalcula en cada caja: unos 38 ms por arrastre.
- **Cajas compartidas.** Las consultas de `e_1` comparten 775 de sus 2 197 cajas distintas en el
  arrastre de `H`, y 984 de 2 376 en el de `B`. En todas, el punto trazado es idéntico bit a bit
  en los conos de las distintas consultas.
- **Ahorro de T6 tal como estaba definida** (punto trazado por caja compartido entre consultas):
  como máximo ~26 ms en `H` y ~33 ms en `B`, un 13–17 % del arrastre.
- **Ahorro de reutilizar por revisión lo que no depende del conductor:** ~57 ms en el punto trazado
  y ~37 ms en el objetivo.
- **Coste de una búsqueda en memoria:** 20–45 ns por caja, frente a ~20–35 µs por caja de
  evaluación.

## 3. Decisión sobre el alcance

T6 estricta daba un ahorro modesto. Presenté al autor tres opciones: T6 estricta, T6 más la
reutilización por revisión de los valores independientes del conductor, o detenerse. El autor
eligió **«T6 + independientes»**. Por tanto, el candidato implementa dos reutilizaciones, ambas
exactas:

- **Valores independientes del conductor, por revisión.** Son los nodos del cono que no dependen
  del conductor y alimentan nodos que sí dependen, más el objetivo cuando no depende del
  conductor. Se calculan una vez por fuente, es decir, por consulta y revisión.
- **Punto trazado por caja, entre las consultas del mismo locus.** Esta es la T6 del plan.

## 4. Qué cambia

| Commit | Cambio |
|---|---|
| `9e11496d` | `ConstructiveResidualJetSource` reutiliza en cada caja los valores independientes del conductor y el punto trazado compartido; nueva `ConstructiveSliceEnclosureMemory`; nueva prueba `ConstructiveSliceEnclosureMemoryTest`; registro en `modified-files.yml` |
| `b1fed72f` | herramienta `tools/benchmark/pre-g9b-cu/run-cu3-enclosure-reuse.ps1` con sus fuentes en `cu3-enclosure-java/`, fuera de todo conjunto de fuentes |
| `3eb5bcdd` | tope de la memoria del corte en 16 384 cajas por locus |
| `3f096838` | inventario JUnit (§7) |

**Valores independientes.** Un nodo cuenta como dependiente si es el nodo del conductor, pertenece
al corte o tiene una entrada dependiente. El valor de un nodo independiente es el que da cualquier
evaluación, porque solo lee sus ancestros y su estado capturado; se fija en el evaluador de cada
caja. Un nodo cuya evaluación falla queda fuera, y cada caja lo evalúa y falla igual que antes.

**Memoria del corte (`ConstructiveSliceEnclosureMemory`).**

- **Contenido.** Una por locus (`WeakHashMap`). Guarda el valor exacto del punto trazado de cada
  caja tal como lo produjo la evaluación, o la marca de fallo.
- **Clave.** Es el texto canónico de todo lo que lee la evaluación:
  - las versiones de la tabla de clase y de la memoria;
  - la revisión semántica del locus, la familia del conductor y su marco;
  - los ancestros del punto trazado y del soporte, numerados canónicamente (cada nodo tras sus
    entradas, por posición estructural, sea cual sea el orden del cono de la consulta), con su
    identidad durable, su definición estructural con las entradas renumeradas y, fuera del corte,
    su estado binary64 capturado;
  - el nodo del conductor;
  - los extremos binary64 exactos de la caja.
- **Invalidación.** Una clave nueva descarta todos los encierros de la anterior, así que la memoria
  guarda como mucho una revisión de un locus. Tiene un tope de 16 384 cajas: una caja de más se
  calcula y no se guarda. Es transitoria: nada se persiste y se libera con el locus.
- **Inyección.** El valor guardado se fija en el evaluador de la caja como valor del nodo trazado.
  No se guarda ninguna decisión, recuento, certificado ni evidencia de integridad.

**Separación fuente/objetivo.** La memoria compartida guarda solo el punto trazado, que nunca depende
del objetivo. Un objetivo fuera del corte no entra en la clave, y dos consultas del mismo locus
comparten la memoria aunque tengan objetivos distintos. El objetivo solo se reutiliza dentro de su
propia consulta y revisión.

## 5. Equivalencia geométrica

- **Modelo del autor.** Con la sonda versionada sobre el candidato, los encierros acelerados son
  idénticos bit a bit a los de una evaluación completa en todas las cajas: 3 479 de 3 479 en el
  arrastre de `H` y 3 870 de 3 870 en el de `B`.
- **Decisiones.** Los recuentos deterministas de la sonda de rendimiento son iguales que en
  `68bfd764` para `H` y para `B`: cajas (504/506, 283, 1 760/2 173 y 932/908), consultas y
  contactos exactos (1, 1, 2 y 1) y residuos normalizados (0). Las decisiones, estados, causas,
  signos e integridad se derivan de esos encierros idénticos, así que no pueden cambiar.
- **`ConstructiveSliceEnclosureMemoryTest`** (7 pruebas, en la réplica):
  - igualdad bit a bit con la evaluación completa, también en las cajas donde falla, en estas
    revisiones: base, `H` −20/+0,5/+40, `B` +3, `rbCone` = 3 y 7,5 y `D_1` ×3, más una segunda
    pasada con la memoria ya llena;
  - las consultas de un locus comparten el punto trazado, y otro locus nunca lee ni escribe en su
    memoria;
  - la clave sigue a la fuente: cambia con una revisión de `H`, con otra parametrización (`rbCone`)
    y con otra rama (`H_1 = Intersect(d_1, i, 2)`), y no con un objetivo fuera del corte que se
    mueve;
  - la caja forma parte de la clave: a un ulp es otra caja;
  - la configuración heredada nunca llega a la memoria;
  - tras Deshacer y Rehacer, los encierros son los de una evaluación completa;
  - el tope se respeta y una caja de más se calcula de nuevo.
- **Pruebas de E1 a E3 y de T1 a T3.** Los 470 casos de los paquetes `constructive` e
  `intersection` pasan sin fallos, y la campaña de §7 ejecuta las selecciones de `CU0` a `CU3`.

**Presupuestos y contadores.** La evaluación por intervalos de la fuente no consume
ningún presupuesto normativo. Los presupuestos de la evaluación exacta y del certificado no
cambian, ni su contabilidad ni sus fallos cerrados. El contador de trabajo del certificado de
conteo (`MAXIMUM_EVALUATIONS`) cuenta las llamadas a `enclose`, que no cambian. T6 no deja dudas
sobre la agregación de contadores.

**Duda registrada sobre la memoria de T1 a T3.** En una comprobación acelerada hay hasta tres
contabilidades separadas, todas con el presupuesto `constructive-identity-certificate-budget/v1`:

- la de la memoria;
- la del respaldo, que es la del procedimiento del candidato y no cambia;
- la de la preparación única del margen.

Cada una respeta el presupuesto y falla cerrada por separado. Pero el trabajo total de una
comprobación que recurre al respaldo puede superar el de un solo presupuesto: en el peor caso,
la memoria agota el suyo y el respaldo gasta otro tanto.

Esto no cambia ninguna decisión: el agotamiento de la memoria solo devuelve el control al
procedimiento del candidato. Corresponde al autor decidir si el presupuesto normativo se aplica a
cada procedimiento o a la comprobación entera. En las revisiones medidas, la memoria nunca agotó
su presupuesto ni recurrió al respaldo.

## 6. Rendimiento

Misma estación y mismas condiciones, con dos ejecuciones por candidato de
`run-cu3-performance-characterization.ps1` (21 arrastres por medida), sin cambios del producto
respecto de cada commit. Valores en ms, mediana / percentil 95, de las ejecuciones A y B.

**Banco E3 (cuatro intersecciones públicas):**

| Candidato | Arrastre de `H` | Arrastre de `B` | Adopción |
|---|---|---|---|
| `68bfd764` | 196,8 / 200,4 · 198,7 / 205,1 | 213,5 / 215,1 · 216,0 / 220,8 | 2 671 · 2 565 |
| `3f096838` (T6) | 103,3 / 115,3 · 101,6 / 120,5 | 106,1 / 108,6 · 106,1 / 111,6 | 2 393 · 2 321 |
| Política heredada | 67,5–68,2 | 47,1–51,2 | — |

**Configuración del smoke (con los cinco puntos estructurales):**

| Candidato | Arrastre de `H` | Arrastre de `B` | Adopción |
|---|---|---|---|
| `68bfd764` | 219,3 / 221,6 · 225,4 / 227,3 | 236,6 / 239,3 · 242,2 / 244,3 | 2 275 · 2 192 |
| `3f096838` (T6) | 125,6 / 144,2 · 127,2 / 141,3 | 131,5 / 136,0 · 131,9 / 136,5 | 2 001 · 1 986 |
| Política heredada | 41,4–43,9 | 42,4–43,5 | — |

T6 reduce el arrastre a la mitad, aproximadamente: de ~198 a ~102 ms en `H` y de ~215 a ~106 ms en
`B` en el banco E3. Desde `3c2ca7e8` (~738 y ~754 ms), el arrastre es unas 7 veces más rápido. La
adopción se acelera un 10 %.

**Primera actualización y siguientes.**

- La primera consulta de un locus en una revisión evalúa el punto trazado de sus cajas, y las
  demás lo leen de la memoria. Por eso las consultas instrumentadas de la sonda dan tiempos de
  encierro desiguales: `e_1·c_1`, la primera de `e_1` con cajas propias, 34 ms; las demás, de 1,5
  a 7,6 ms.
- La primera comprobación de un certificado nuevo sigue preparando la memoria de T1 a T3 (unos
  320 ms en `o_1·h_2`, una vez por certificado).

**Dónde va el tiempo restante.** Perfil JFR de 60 arrastres de `H` y 60 de `B` en la configuración
E3, con 105 y 108 ms de mediana:

- certificados de conteo, `RootCountCertificate2D.certify`: 77 % de las muestras;
- encierros de cajas (`ConstructiveResidualJetSource.enclose`): 61 %, casi todo evaluación por
  intervalos de los nodos que dependen del conductor;
- comprobación de los certificados de identidad constructiva: 15 %;
- recálculo del locus (`AlgoLocusV2`): 11 %.

**Contadores.** Las cajas, consultas exactas, contactos y residuos normalizados son iguales que en
`68bfd764` en todas las consultas y en ambos arrastres, y deterministas en las cinco repeticiones.

## 7. Verificación `INTEGRATED_PHASE` del candidato `3f096838`

Comandos, desde la raíz del repositorio. `INFRA_UNIT` se ejecutó con el PowerShell 7.6.6 oficial
aislado y el resto con 7.6.9, la versión de la estación:

```powershell
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu3-t6-static-3f096838
& <pwsh 7.6.6> -NoProfile -NoLogo -File tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu3-t6-infra-unit-pwsh766-3f096838
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU3 -LogDirectory artifacts\agent\pre-g9b-cu3-t6-phase-pre-g9b-cu3-3f096838
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2 -LogDirectory artifacts\agent\pre-g9b-cu3-t6-phase-pre-g9b-cu2-3f096838
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1 -LogDirectory artifacts\agent\pre-g9b-cu3-t6-phase-pre-g9b-cu1-3f096838
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu3-t6-phase-pre-g9b-cu0-3f096838
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu3-t6-integration-3f096838
```

Para `INFRA_UNIT`, el directorio de 7.6.6 se antepuso al PATH, de modo que los subprocesos que
invocan `pwsh` por nombre también usaran 7.6.6. Cada resultado se reautenticó con
`Read-VerificationJson` y `Get-VerificationResultHash`. En todos, el hash coincide, el diario está
`COMPLETED` y el candidato es exactamente `3f0968385920e36b2b48191a4f6e736c4fc11c09`, árbol
`d3502024fb91ee60a125f3f2a3b34701aafc29c2`.

| Perfil | `run_id` | `result_hash` | Plan | PowerShell | Duración | Resultado |
|---|---|---|---|---|---|---|
| `STATIC` | `verification-a80c32066ab64f419cc31566d24afa01` | `ad10f0bb491a0e96f5691db8f2ee9b47b5acadd605772fdf2c6cf824a494f942` | `25dfacc8` | 7.6.9 | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo los diagnósticos permanentes |
| `INFRA_UNIT` | `verification-4c50f40dac084244b2716234359138bc` | `083a89466a9c6e5113eb84eaccdae500cdd3f9563ce0a3e407636213109c236f` | `830887b1` | 7.6.6 | 2,3 min | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU3` | `verification-ff8a0c32573b4ac4bb7a2b66070942c2` | `bc9242ebbd039472ebd1e7f303b0420cb26754b9d2b3fe7aa81f69675a9266a2` | `1d298a3a` | 7.6.9 | 6,3 min | `ACCEPTED / COMPLETE`, 4/4 |
| `PHASE -Phase PRE-G9B-CU2` | `verification-0162f426ce67493c9b5b27354c4df669` | `f3be2bfd1b1350e50fd788f3cc5dc7ec93b6a71aae2dfea30f159269912cda60` | `b9dc6d9e` | 7.6.9 | 9,7 min | `ACCEPTED / COMPLETE`, 4/4 |
| `PHASE -Phase PRE-G9B-CU1` | `verification-e23a73e3daf64b96a02f2ead064e7370` | `d6c98a5f12cf467c7318e3b9053b8df1a461937b4f262339375b0f0286ca410e` | `b0e4f130` | 7.6.9 | 5,7 min | `ACCEPTED / COMPLETE`, 4/4 |
| `PHASE -Phase PRE-G9B-CU0` | `verification-26d21941d569418cbb6e1edea40414d2` | `155af5a313cadefb1c939e8a14fa5d9868f2df74d4f03f68388ac629b9aa01d3` | `43e3e97e` | 7.6.9 | 2,6 min | `ACCEPTED / COMPLETE`, 2/2 |
| `INTEGRATION` | `verification-e97d8c11be1d45909f26c3b12fb3dbd4` | `d37caa75c6442a6efc3553c69bb92aec39d22bb39b2ca1c812060464bdfe8c5a` | `4502c4bd` | 7.6.9 | 54,4 min | `ACCEPTED / COMPLETE`, 19/19 |

- **Planes.** Son los mismos que en las verificaciones de `3c2ca7e8` y `68bfd764`.
- **Firma de entorno.** Es la misma en las siete ejecuciones; la versión de revisión de
  PowerShell solo figura en la observación.
- **Clase.** `INTEGRATED_PHASE` queda completa y aceptada técnicamente sobre el candidato congelado
  `3f096838`. No es una aprobación autoral.
- **Selecciones de `CU3`.** La compartida tiene 23 clases y 209 pruebas (la nueva clase añade 7);
  la de escritorio, 5 clases y 14 pruebas. Se mantienen los casos obligatorios, las cinco
  incidencias estrictas y las inversiones autorizadas de los pines.

## 8. Deuda `DEBT-VERIFIER-PWSH-769-TESTJSON`

- **Qué es.** Con PowerShell 7.6.9, `Test-Json` ya no conserva el `FullyQualifiedErrorId`
  `InvalidJsonAgainstSchemaDetailed,Microsoft.PowerShell.Commands.TestJsonCommand`. Las suites
  `verification-registry.Tests.ps1` y `verification-report.Tests.ps1` (`infra.registry` e
  `infra.report`) fallan con esa versión en cualquier commit, también en `3c2ca7e8`.
- **Cómo se resuelve aquí.** Este candidato ejecuta `INFRA_UNIT` con el PowerShell 7.6.6 oficial,
  aislado ([registro de T1 a T3](pre_g9b_cu3_performance_optimization_record.md) §7.1).
- **Qué falta.** Esto no elimina la deuda. Adaptar las pruebas a 7.6.9 es un mantenimiento
  independiente de la infraestructura, que no está autorizado.

## 9. Memoria

- **Heap retenido tras la recolección**, según la sonda de rendimiento:
  - documento E3: 40,0 MB con `68bfd764` y 40,9–41,0 MB con T6, unos +1 MB;
  - documento del smoke: unos +1,9 MB.
- **Acumulado con T1 a T3** desde `3c2ca7e8`: unos +6,7 MB en el documento E3 y unos +12 MB en el
  del smoke, con sus cinco puntos estructurales y sus certificados.
- **Contenido de la memoria del corte en el modelo.** Por revisión, 2 197–2 376 cajas para `e_1` y
  503–505 para `o_1`.
- **Varias consultas.** Las consultas de un locus comparten una sola memoria: más consultas no la
  hacen crecer, solo reutilizan más.
- **Cambios de revisión.** Cada locus guarda una sola revisión: la clave nueva descarta la anterior.
  El peor caso por locus es el tope de 16 384 cajas. Los valores independientes del conductor viven
  con la fuente de una consulta y una revisión, y se liberan con ella.
- **Documentos con muchos certificados.** La memoria de T1 a T3 crece con el número y el tamaño de
  los certificados: unos 21 000 términos para `o_1·h_2`. La de T6 crece con el número de loci con
  consultas constructivas, no con el de certificados.
- **Mantenimiento.** No hay tareas de fondo ni limpieza explícita. La sustitución por clave es
  inmediata, y las memorias se liberan con su locus (`WeakHashMap`) o con su certificado.
- **Persistencia.** Ninguna memoria se persiste. Ninguna conserva evidencia vigente tras una
  invalidación, porque no guarda evidencia: solo valores exactos indexados por todo lo que los
  determina.

## 10. Evaluación de T7, sin implementar

T7 no está autorizada. Esta valoración sigue los puntos que pide el autor y no cambia nada.

**Qué parte del coste residual corresponde al análisis de pliegues.**

- **Perfil del arrastre.** Perfil JFR de 60 arrastres de `H` y 60 de `B` sobre el candidato T6,
  configuración E3, con 105 y 108 ms de mediana:
  - `RootCountCertificate2D.certify` está en el 77 % de las muestras;
  - los encierros de cajas (`ConstructiveResidualJetSource.enclose`), en el 61 %;
  - la comprobación de los certificados de identidad constructiva, en el 15 %;
  - el recálculo del locus (`AlgoLocusV2`), en el 11 %.
- **Atribución por llamador.** Atribuyendo cada caja a la cadena de llamadas, el 99 % procede de
  `CertifiedContactAnalysis2D.certificate > RootCountCertificate2D.run > critical > fixedSign`.
  Esta es la cuenta en un arrastre de `H`:

  | Consulta | Cajas de `critical > fixedSign` | Total de cajas |
  |---|---:|---:|
  | `e_1·c_1` | 1 733 | 1 760 |
  | `e_1·r` | 928 | 932 |
  | `e_1·n` | 279 | 283 |
  | `o_1·h_2` | 500 | 504 |

  Es el paso en que `critical` intenta certificar el signo de `h` en el corchete del punto crítico
  con `fixedSign(low, high, 0)`, bisecando hasta `MAXIMUM_DEPTH` = 24. En un contacto doble,
  `h(t0) = 0` hace imposible certificar un signo en un subintervalo que contenga `t0`. La bisección
  agota la profundidad y el certificado acaba en `exactDouble`, con la decisión exacta de E3.
- **Conclusión.** Casi todo el coste residual de los encierros, unos 60 de los ~105 ms, es este
  paso.

**Qué certificados se podrían reutilizar.** Hay dos vías, de distinto alcance:

- **T7-a, decisión exacta primero.** La decisión exacta de E3 (`exactContacts`: `h(t0) = 0` y
  `h'(t0) = 0` en un parámetro exacto, con el signo exacto de `h''`) ya se calcula y se reutiliza
  entre revisiones. Si establece un `t0` dentro del corchete, el resultado de
  `fixedSign(low, high, 0)` está determinado: ningún encierro riguroso puede certificar un signo
  fijo de `h` en un intervalo donde `h` se anula. Consultar primero la decisión exacta y, si sitúa
  `t0` en el corchete, pasar directamente a `exactDouble` da el mismo certificado sin bisecar. Vale
  para los pliegues de `e_1` y para la tangencia regular de `o_1·h_2`, que E3 también decide.
- **T7-b, banda del pliegue de `CU2`.** Usar la banda certificada del pliegue de `CU2` y sus chorros
  de segundo orden para certificar el signo de `h''` y la posición del punto crítico sin bisecar.
  Solo vale en los pliegues, no en la tangencia regular de `o_1·h_2`. Exige demostrar que la banda
  transferida por estructura acota el punto crítico del residuo compuesto, no solo el de la
  fuente.

**Qué condiciones tendrían que conservarse.**

- **Aislamiento.** El certificado de conteo de cada caja `T` debe dar la misma forma (`NONE`,
  `ONE_SIMPLE`, `TWO_SIMPLE`, `ONE_DOUBLE` o `NOT_CERTIFIED`) con los mismos corchetes. En T7-a, el
  corchete del punto crítico se calcula antes del paso que se omite y no cambia.
- **Conteo y multiplicidad.** `ONE_DOUBLE` sigue exigiendo `h''` de signo fijo en `T` y la
  concordancia del signo exacto de `h''` con su encierro. T7-a solo omite la bisección del valor
  crítico, no esas condiciones.
- **Validez.** Hoy, la bisección omitida puede devolver `INVALID` si la fuente no es válida en un
  subintervalo. T7-a debe conservar esa rama: la omisión solo es segura si la validez en `T` ya
  está certificada y la evaluación es monótona respecto de la inclusión, lo que hay que demostrar
  para `IntervalJet` y `DriverParameterEnclosure.cosSin`.
- **Límite de trabajo.** El contador `evaluations` y el límite `MAXIMUM_EVALUATIONS` = 4 096
  cambian: un certificado que hoy agota el límite y queda `NOT_CERTIFIED` podría certificarse. Es
  una diferencia de decisión que habría que caracterizar y que decide el autor.

**¿Se puede demostrar la equivalencia con los resultados actuales?** En T7-a, sí, salvo los dos
casos frontera anteriores (`INVALID` y el límite de trabajo):

- **Razonamiento.** Si `h(t0) = 0` con `t0` en el corchete, ningún encierro riguroso certifica un
  signo fijo de `h` en él, así que `fixedSign` devuelve 0 salvo `INVALID` o agotamiento.
- **Comprobación previa.** Se haría con una sonda de solo lectura que calcule los dos caminos en
  cada llamada a `certify` del corpus y de la matriz de E3, y compare la forma, los corchetes, el
  contacto y el motivo.

T7-b exige una demostración nueva: que la banda de `CU2` acota el punto crítico del residuo
compuesto. Es una investigación, no una optimización.

**Qué degeneraciones serían condiciones de parada.**

- **Decisiones no establecidas.** Un pliegue sin transferencia (`Cu2CertificateTransfer` distinto
  de `TRANSFERRED`) o un contacto de orden superior (`h''(t0) = 0`). Con ellos no hay `exactDouble`
  y debe seguir la bisección actual.
- **Corchete con varios puntos críticos o con la costura en ±π.**
- **Fuente no válida en parte del corchete.** Es la rama `INVALID`.
- **Certificado que hoy agota el límite de trabajo.**
- **Desacuerdo entre el signo exacto de `h''` y su encierro.** Hoy da `NOT_CERTIFIED`, y así debe
  seguir.
- Cualquier diferencia de forma, corchete o motivo en la sonda de equivalencia detiene T7.

**Qué mejora cabe esperar.** Si T7-a omitiera casi todas las cajas de `critical > fixedSign`,
desaparecería la mayor parte de los ~60 ms de encierros del arrastre. Los arrastres de `H` y `B`
podrían bajar de unos 100–105 ms a unos 45–60 ms en E3, cerca de la política heredada (50–67 ms).
Es una estimación del perfil, no una medida.

**Recomendación.** Autorizar primero, si el autor lo desea, solo la caracterización de T7-a: la
sonda de equivalencia de solo lectura, el recuento de los casos con `INVALID` y con el límite de
trabajo, y el ahorro medido. La implementación quedaría condicionada a una equivalencia completa en
el corpus y en la matriz de E3. T7-b no se recomienda ahora. Ninguna de las dos sustituye una
demostración de conteo por un muestreo, una tolerancia o una hipótesis de continuidad.

## 11. Limitaciones y decisiones para el autor

- **Objetivo orientativo de 100 ms.** Se alcanza casi en el banco E3 (~102 y ~106 ms) y no en la
  configuración del smoke (~126 y ~132 ms). Lo que queda es sobre todo la bisección del análisis de
  los puntos críticos (§10).
- **Alcance ampliado.** La reutilización por revisión de los valores independientes del conductor
  excede T6 tal como estaba definida. Se implementó por decisión expresa del autor (§3).
- **Revisión semántica en la clave.** Incluirla hace que, al volver a un estado anterior (por
  ejemplo, devolver `rbCone` a su valor), la clave cambie. Es conservador: no reutiliza entre
  revisiones aunque los valores coincidan.
- **Primera consulta.** La primera consulta de cada locus en cada revisión paga la evaluación de
  sus cajas; el ahorro de T6 viene de las demás.
- **Deuda de verificación.** `DEBT-VERIFIER-PWSH-769-TESTJSON` sigue abierta (§8).
- **Duda sobre los contadores de T1 a T3.** Registrada en §5.
- **Medida.** Una sola estación y el arnés de pruebas; el smoke interactivo real es del autor.
- **Decisiones del autor:**
  - aceptación o no del candidato y de su rendimiento;
  - la duda de §5 sobre el presupuesto de la comprobación acelerada;
  - si autoriza la caracterización de T7-a (§10);
  - T5, que sigue sin autorizar.

## 12. Estado

- Commits locales sobre `b1813da1`: `9e11496d`, `b1fed72f`, `3eb5bcdd`, `3f096838` (candidato) y
  el de este registro. Nada publicado; `main`, las etiquetas, las publicaciones y `geocedg_book`
  no cambian.
- `CU3-A` sigue pendiente de aceptación definitiva y el rendimiento interactivo, de la decisión
  del autor. `CU3-B`, T5, T7 y la autoridad constructiva predeterminada siguen sin autorizar.
- `CU3` no está `PASS — AUTHOR APPROVED`.
