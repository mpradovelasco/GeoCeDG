# CU7 — Diagnóstico de creación general en el kernel

Fecha: 2026-11-02, Europe/Madrid. Base inspeccionada: `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17`. Anexo de caracterización y diseño candidato. `selfApproved=false`. Tras el diseño previo se preparó una fachada kernel y sus pruebas en staging para integración por el coordinador, sin cambiar los predicados existentes, contratos o infraestructura. No se ejecutaron campañas, pruebas Java o sondas nuevas por este agente.

## 1. Dictamen

La evaluación forward exige menos que la admisión inversa actual. Una dirección canónica reconstruible, resuelta a una componente continua certificada y evaluable en la revisión actual, puede definir un punto sin demostrar regularidad C1, mínimo, inyectividad local o completitud de una búsqueda inversa. Esa posibilidad forward **no** autoriza convertir cualquier candidato numérico de una consulta unresolved en una oferta de creación.

Hay una ampliación acotada de autoría potencialmente realizable sin cambiar los predicados de admisión: permitir que el clic y la referencia Q compartan la prioridad histórica R1/R6 y, únicamente ante `GLOBAL_UNIQUENESS_NOT_ESTABLISHED`, la ruta R2/H1 ya existente. La API `M08SnapshotPlacement2D.query(source,x,y,policy)` ya acepta la petición transitoria necesaria para el clic; la variante Q añade la validación/revalidación de la referencia. Esto ampliaría el clic a las fuentes que R2/H1 ya admite; no resolvería los evaluadores sin ese certificado.

Una nueva ruta de **elección individual forward válida sin C1/minimum** para fuentes generales requiere un criterio normativo nuevo. Debe separar descubrimiento de admisión y no alterar las garantías de R6/R2/H1. La existencia de una factory de publicación menos restrictiva no constituye ese permiso.

## 2. Contratos efectivos de las fronteras existentes

Abreviatura `K/`: `source/shared/common/src/main/java/org/geocedg/common/kernel/`. `T/`: `source/shared/common-jre/src/test/java/org/geocedg/common/locus/`.

| Frontera | Hecho estático y utilidad | Restricción actual |
| --- | --- | --- |
| `K/locus/interaction/LocusPointInteractionResolver2D.java:56–156` | Resolver R6 compartido; affine → polynomial → evaluator fallback; presupuestos tipados | No contiene una ruta de descubrimiento de direcciones arbitrarias libre del contrato de mínimos/regularidad |
| `:421–452`, `:455–535` | Fallback finito: partición semántica, hasta 96 subdivisiones por segmento, muestras y golden refinement | Emite candidatos solo tras `retain/localEvidence`; no todos los argumentos forward válidos se consideran candidatos |
| `:585–694` | Cada candidato tiene evaluación y evidencia de mínimo estricto/regular; componentes puntuales tienen rama especial | Singularidad/no-minimum/no evidencia no se promocionan por elección |
| `:697–767` | Regularidad admitida por evaluator explícito, diferencial o estimación multiescala | Esta regularidad R6 estimada no es la prueba C1 intervalar H1 |
| `:884–947` | Admisibilidad filtra world radius y separabilidad; respeta unsafe barriers | Con una sola dirección de bounded fallback devuelve unresolved/global uniqueness; con varias direcciones seguras puede devolver MULTIPLE sin C1 H1 |
| `:1333–1344` | Enumera canonical pieces de componentes certificadas | Para creación sin current address exige `existence.Completeness.COMPLETE`; una estructura parcial no se busca, aunque contenga componentes individuales certificadas |
| `K/locus/interaction/LocusSnapshotPlacement2D.java:39–88` | Captura fuente/definición; selección de un solo uso; el commit publica dirección forward | Solo UNIQUE o MULTIPLE; unresolved no se convierte en permiso |
| `K/locus/interaction/M08SnapshotPlacement2D.java:206–231` | `query(x,y)` y `queryFromReference(Q)` confluyen en una Selection | Q es transitorio y debe ser definido, mismo documento y miembro real de la construcción |
| `:269–328`, `:330–383` | Descubrimiento R6 reutilizado, componente resuelta, prueba independiente por dirección | Rechaza política constructiva no acreditada; noObservedBarrier; cada seed debe satisfacer H1; fallo de una proof vacía el conjunto |
| `:405–441`, `:476–518` | Binder forward actual; currentness; commit single-use/thread-owned y revalidación con presupuesto acumulado | No basta existencia del candidato: debe rehacerse proof, forward y vigencia; failure no publica |
| `K/locus/intersection/M08LocalAddressEvidence2D.java:45–103` | Enclosure del valor/proximidad en configuración representada y primer jet | Modelo soportado, interior con vecinos binary64, C1 regular en esa vecindad, distancia estricta dentro del radio; sin prueba → causa tipada |
| `K/locus/LocusV2PublicOperations.java:245–307` | Factory R6, factory sellada M08 y inputs ocultos de dirección existentes | La factory R6 no sustituye la frontera de resultado admisible y elección explícita |
| `:379–420` | Participación normal; algoritmo; exige que el resultado reconstruya exactamente la dirección requerida | Reutilizable para un nuevo protocolo aprobado, sin codec nuevo; no demuestra admisión inversa |

### 2.1 El forward no exige C1

`K/locus/metric/LocusMetricPositionBinder2D.java:39–123` valida locus/provider, branch, resuelve el selector de componente por correspondencia estructural y evalúa. Acepta una evaluación válida sin leer derivada, regularidad o mínimo. Una componente puntual declarada no se materializa por argumento binary64 (`:92–99`). Un selector no resuelto o evaluación fallida produce status/diagnóstico, no reparación por proximidad.

`K/algos/AlgoSemanticLocusPoint2D.java:87–92` registra fuente, entrada de branch/state y parámetro como inputs del DAG. `:95–225` decodifica estado, valida parámetros/provider/lift/componente, consume binder y conserva la dirección aceptada separada del binding actual; un binding inválido deja el punto indefinido. `:372–404` exige componente única para un argumento sin selector o correspondencia demostrada para un selector retenido. No inventa componente por contener el punto cartesiano.

La persistencia se puede reutilizar: `K/locus/LocusSemanticAddressState2D.java:27–64` codifica branch, structural component, provider, bits canónicos, lift y seam; `AlgoSemanticLocusPoint2D` conserva el selector aunque el binding quede inválido y lo revalida. Esto permite que P siga L y no una petición Q anterior; no implica que P sea arrastrable por cualquier gesto futuro.

### 2.2 La factory de nivel inferior no es una autorización alternativa

`LocusV2PublicOperations.createInteractiveSemanticPoint` comprueba revisión y dirección actual, y exige reconstrucción exacta. Tests R6 `r609` y `r624` construyen fixtures de punto periódico a partir de candidatos descubiertos de resultados unresolved para probar estado/codec/drag. Eso no amplía la autoría UI aprobada: contrato M08 §3 exige conservar la frontera resultado admisible/elección y dice expresamente que un valor candidate no es permission token. Una implementación no debe llamar esta factory para saltar `LocusSnapshotPlacement` o H1.

## 3. Tres obstáculos independientes al evaluador general

1. **Existencia/componentes:** un dominio finito declarado no demuestra componente continua válida. `T/G9U0PublicSurfaceTestBase.java:245–273` conserva la regla CU2: una construcción fuera de clase v3 mantiene cobertura unresolved, sin componente ni completitud. Expresiones como `(s,exp(s))`, una parábola escalar o un seno arbitrario no adquieren componente por el hecho de poder devolver valores finitos en unas muestras. Su forward ordinario tampoco queda automáticamente definido.
2. **Descubrimiento:** el fallback actual solo busca/retiene mínimos localmente regulares bajo el contrato R6. Puede no descubrir una dirección válida próxima, una cúspide, extremo o fuente muy oscilatoria. Presupuesto agotado o inventario no completo no es ausencia de dirección.
3. **Admisión individual:** R2/H1 añade certificado de modelo/valor/proximidad/C1 al único resultado local de R6. Un argumento forward válido no satisface esta proof. Cambiar esta condición afecta permiso nuevo, aunque no cambie el codec.

No conviene resolver los tres problemas mediante un único `isValid()` o reinterpretar `Regularity.UNKNOWN` como seguro. El permiso de selección debe nombrar exactamente qué garantías obtuvo y qué no obtuvo.

## 4. Casos concretos para la caracterización focal

Todos los siguientes son **casos propuestos o tests inspeccionados**, no resultados de una ejecución nueva en este anexo.

| Caso | Evidencia disponible y resultado esperado a contrastar |
| --- | --- |
| NativeGraph autoral h, círculo→recta→intersección ejes→Midpoint | `T/M08SnapshotPlacementKernelTest.java:35–79` obtiene proof H1 para un candidato R6 unresolved y prueba world bound. Es candidato para paridad clic/Q sin norma nueva |
| Circunferencia de `GeoPoint` sobre `Circle` más `(0,0)` | `source/desktop/desktop/src/test/java/org/geocedg/desktop/Cu7M08SGeneralLocusDiagnosticTest.java:34–68`: hit gráfico, un candidato local, R6 unresolved y forward explícito válido. No asumir que no tiene modelo H1: puede tenerlo y el seam/interior importa |
| Componente CU2 certificada, clase-v3 vectorial | Sondeo recomendado: `c=Circle((0,0),1); N=Point(c); V=Vector((0,0),N); W=2*V; Q=N+W; L=LocusV2(Q,N)`. Ideal radius 3, pero comprobar la captura real y completitud sin promoverlas por la fórmula. `CertifiedConstructionProgram2D` tiene VECTOR_POINTS/SCALE/POINT_PLUS_VECTOR v3; H1 captura v1/v2, por lo que constituye hipótesis concreta de componente certificada sin H1 actual |
| Componentes heredadas de Locus soporte | `T/PreG9BCu2ConsumerMigrationTest.java:309–333` demuestra por assertions la composición estructural total `S=Point(L,branch,u); Y=S+(1,0); M=LocusV2(Y,S)`, heredando componentes/topología/completitud. Su H1 puede carecer de driver de clase v1/v2; comprobar sin suponer |
| Varios candidatos R6 seguros | R6 general puede publicar MULTIPLE con regularidad estimada y sin modelo intervalar H1, si componentes e inventario son admisibles; mantener elección explícita. No afirmar que cualquier fixture general de dos imágenes iguales reúne esas condiciones |
| `(s,exp(s))` en dominio finito | `T/M08SnapshotPlacementKernelTest.java:294–300` prueba que no gana certificado H1 por seed. Además está fuera de clase v3: caracterizar el rechazo de componente, no venderlo como solo una limitación C1 |
| Cúspide cardioide geométrica | `T/G9U0R6SemanticLocusPointInteractionTest.java:1006–1027`: fallback unresolved con singularidad. El forward singular y la admisión inversa son preguntas distintas; nuevo criterio debe decidir si puede admitir dirección exacta sin afirmar inyectividad |
| Mínimo estrecho no muestreado | `r646`, `:1610–1634`: bounded fallback nunca obtiene unique/no-candidate definitivo por ese scan. Nueva creación individual no debe declarar exhaustividad ni ausencia |
| Fuentes v3 line-circle y ray | Fixtures CU2 de `PreG9BCu2RepresentationTest.java:911–924` contienen ramas/componentes y frontera exploratoria. Útiles para probar componente certificada individual frente a inventario parcial, sin usar vértices exploratorios como dominio válido |

La distinción v1/v2/v3 es tangible: `K/locus/ReconstructibleLocusEvaluator2D.java:471–508` tiene dos capturas separadas, `captureCertifiedProgram()` v1/v2 y `captureCertifiedProgramV3()` para existencia. `K/locus/intersection/ConstructionIntervalModel2D.java:66–78` usa solo la primera. No transferir automáticamente la certificación de existencia v3 a una enclosure C1 H1.

## 5. Propuesta mínima de separación arquitectónica

### 5.1 Reutilización bajo contratos vigentes

Un coordinador kernel de autoría puede ofrecer una sola operación de selección/commit para petición world transitoria o Q existente:

1. Capturar fuente/revisión/política y, si existe Q, su valor actual y validación.
2. Consultar `LocusSnapshotPlacement2D`; conservar UNIQUE/MULTIPLE históricos y sus garantías.
3. Solo ante reason `GLOBAL_UNIQUENESS_NOT_ESTABLISHED`, consultar `M08SnapshotPlacement2D`; conservar H1 completo. No fallback tras otro rechazo o fallo de commit.
4. Exponer una unión tipada de choices, con dueños/protocolos independientes y confirmación explícita de la ruta pertinente. No mezclar candidate R6 y Choice H1 ni inventar presupuesto agregado entre rutas.
5. Revalidar y consumir una sola vez; publicar mediante su factory actual y un Undo normal. Q no es padre nuevo; click/screen coordinates no se serializan.

Esta separación traslada orchestration del Desktop al kernel y permite que ambas formas de autoría usen los mismos predicados. No obliga a tocar el resolver histórico, la proof H1, binder, codec o drag. El GUI solo selecciona fuente, transforma el click en world request y presenta ofertas.

### 5.2 Ruta nueva que exige decisión normativa

La operación candidata general debería tener un nombre de permiso distinto a H1 y evidencia por dirección que distinga:

- fuente/revisión/provider, branch y selector de componente actuales;
- parámetro canónico y seam/lift reconstruibles;
- pertenencia a una componente certificada con correspondencia única;
- forward finito válido en la semántica del documento y garantía numérica real;
- proximidad world a la petición bajo la política aprobada;
- el hecho de que no se exige C1, mínimo, unicidad cartesiana ni cobertura global;
- fallos tipados cuando cualquiera de las condiciones necesarias no se establece.

No reutilizar `LocusPointInteractionLocalEvidence2D` para llamar «minimum established» a una dirección que solo tiene prueba forward. El tipo actual describe precisamente un mínimo local aislado y su constructor impide candidatos sin ese estado. Un nuevo servicio puede reutilizar parámetros, session/budget, selector y binder; su result/permission/evidence deben expresar otra garantía.

El primer perímetro útil y honesto sería finito, con componentes ya certificadas, sin introducir nuevas recetas de existencia ni persistencia. Puede considerar solo las componentes certificadas de un inventario parcial si el nuevo contrato permite alcance individual, registrando cobertura parcial y sin certificar ausencia; hoy `componentsForQuery` no lo hace. Una petición cuyo candidato cae en zona exploratoria/sin componente queda rechazada. La elección del usuario no convierte en componente una banda unresolved.

La búsqueda nueva podría usar muestras y refinamiento semánticos como descubrimiento, nunca render. La aprobación debe fijar si basta proximidad respecto del valor semántico representado evaluado con garantía declarada o si requiere enclosure certificada de valor; esta decisión no se deriva de quitar C1. Si exige la misma enclosure H1, algunos v3 seguirán fuera hasta existir capability de valor. No inventar una tolerancia que diga EXACT/STRICT ni confundir dirección binary64 explícita con inversa exacta.

## 6. Matriz focal sugerida y reutilización de tests

| Garantía | Tests existentes inspeccionados / ampliación focal necesaria |
| --- | --- |
| Equivalencia de petición click/Q | Mismos source snapshot, world policy y request values → misma lista semántica/ofertas. Q movido/borrado antes de confirmar invalida solo su selección; Q no persiste como padre |
| Paridad R6/R2 sin modificación | R6 `r601–r649` y `M08SnapshotPlacementKernelTest`; confirmar que el nuevo coordinador conserva reason y fallback exactos |
| Distinción global/individual | R6 `r618`, `r631`, `r646`; un único candidato no recibe UNIQUE. Nueva permission debe ser distinta y solo tras norma aprobada |
| Componente y singularidad | R6 `r617`, `r629`, `r630`, `r645`; nuevo permiso conserva branch/component concretos, no salta gap por proximidad |
| Periodicidad y codec | R6 `r609`, `r624–r626`, `r635`, `r649`; no duplicar seam o reconstruir canonical bits desde coordenadas |
| Currentness y consumo | M08 kernel sourceABA/ref-change/foreign/cancel/replay/thread tests; readonly result separado de selection validity |
| Trabajo acumulado | `M08SnapshotPlacementBudgetTest`: 149 insuficiente vs 154 llamadas, exact sufficient, una unidad corta en query/commit, passes intervalares, nested sessions y capability prepared. No transportar H1 a otra ruta por compartir contador |
| Persistencia y DAG | R6 `r613–r616`, `r634`, `r638`, `r641`; M08 ref-independence/sourceABA/reload/nativeUndo. Guardar/reabrir .cedg definido/dormante, copia/remap y dependientes ordinarios |
| Prueba gráfica real | Debe verificar entrada clic real y selección contextual/confirmación, además de Q. Consultas programáticas/hit() aislado no son esa prueba |

Para los casos generales sin H1 se necesitan sondas guardadas que verifiquen fuente real, capture v3, structure/completeness, binder forward y causas actuales. No llamar pruebas de creación a una query seguida de factory de bajo nivel que elude la selección permitida. No relajar una prueba adversaria para ampliar la admisión.

## 7. Límites, inventario e impacto

Inspeccionados: `AGENTS.md`; interacción M08 y ADR0019; resolver y sus candidates/results/local evidence; ambos placements; public operations; semantic address/state; binder y algoritmo de punto; proof H1/capture intervalar; capturas y programa v1/v2/v3; tests G9U0R6/M08/kernel-budget/GeneralLocusDiagnostic y fuentes CU2 de componentes/captura. Las referencias a tests son lectura de sus assertions, no PASS nuevo.

No se profundizó M09 ni se propone reconstrucción de algoritmos, `Path` genérico, nuevo codec, nueva clase de existencia o arrastre general. El nuevo criterio sin H1 y el comportamiento en inventarios parciales permanecen decisiones candidatas. La unificación de las dos autorías bajo los permisos ya aprobados es separable de esas decisiones.

Archivos preparados por este agente en staging ignorado: este anexo; `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/interaction/LocusPointCreation2D.java`; `source/shared/common-jre/src/test/java/org/geocedg/common/locus/M08PointCreationKernelTest.java`. El coordinador integra el conjunto y ejecuta su verificación. Capa afectada: servicio kernel de autoría y documentación de diagnóstico. Semántica admisible/compatibilidad/codec: sin cambio; autoría clic obtiene acceso a una ruta existente, condicionada a la integración Desktop.

## 8. Implementación focal preparada después del diseño

Se leyó `docs/architecture/pre_g9b_cu7_general_creation_design.md` del candidato antes del código. La fachada nueva traslada la composición de `GeoCeDGReferencePlacement` al kernel compartido; conserva la misma tabla de selección y failures subyacentes. No toca resolver, candidate, H1/proof, evaluator, binder, forward algorithm, codec o drag.

API preparada:

```text
LocusPointCreation2D.query(source, x, y, policy)
LocusPointCreation2D.queryFromReference(source, Q, policy)
    -> Selection
Selection: getRoute, getChoices, getHistoricalResult, getLocalSelection,
           getFailure, getValidity, commit(label,Choice), close
Choice: getAddress
Route: R1_R6 | R2_H1 | REJECTED
```

La selección captura fuente/definición/revisión/registry y, solo si existe Q, su valor actual. Thread, single-use, dueño del Choice y RuntimeFeatureService se revalidan antes del commit. La composición no hace parsing de diagnósticos. La string de error es presentación; `Failure/Rejected`, `getHistoricalResult()` y `getLocalSelection()` conservan causas kernel tipadas. Un fallo de publicación jamás intenta otra ruta. Los presupuestos de R1 y H1 conservan sus contratos independientes.

Pruebas preparadas (12): equivalencia click/Q sobre h y XML no mutante; prioridad R6/Spline sin abrir H1; elección explícita de dirección múltiple; Q stale; source ABA; cancelación/foreign/replay; thread owner; agotamiento R6 terminal; independencia Q/propagación L y dependiente; reapertura forward con ID/dirección y tres inputs; diagnóstico v3 vector con componente certificada y sin captura H1; fórmula scalar fuera de clase sin componente ni permiso.

El caso v3 vector es una hipótesis de fixture que la prueba exige verificar antes de probar el rechazo: `captureCertifiedProgramV3` presente, captura v1/v2 ausente, existencia COMPLETE y componente no vacía. No se da por demostrado hasta la ejecución guardada. Si falla el fixture se diagnostica/corrige la construcción o se informa su límite; no se cambia producción para hacerlo pasar. El integrador conserva los logs/exit/identidades reales y decide el reporte de esa ejecución.

Validación propia: lectura estática y preparación de fuentes/tests; sin Gradle/aceptación ni logs runtime de este agente. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`: ninguna dependencia/toolchain/runtime nueva. `VERIFICATION_INFRASTRUCTURE_IMPACT=NONE` para estos archivos; selección/inventario corresponden al integrador. `GUIDE_IMPACT=UPDATE` para la autoría clic/Q si la fachada y su consumidor se integran y validan; `NONE` para el anexo aislado.
