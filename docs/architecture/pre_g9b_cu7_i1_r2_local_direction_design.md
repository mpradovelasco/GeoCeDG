# CU7-I1-M08-S-R2 — dirección próxima en una carta regular

Estado: **CANDIDATO DE DISEÑO; REQUIERE REVISIÓN Y APROBACIÓN AUTORAL**.
Fecha de preparación: 2026-10-31. `selfApproved=false`.
No implementa admisibilidad nueva ni declara PASS de M08-S.

Base verificada: `3816029333e8035323f9a702e29b60a6ab7e19ce`, árbol
`3d1dec62bd2c9e8ac2409a4a3a587bc0db09c10d`, candidato R1 intacto.
Rama: `codex/pre-g9b-cu7-i1-m08-s-r2-design`.
La autorización R2 permite investigación matemática y textos candidatos, no cambios productivos.

## 1. Conclusión y significado de aislamiento

M08-S no necesita resolver un problema de optimización para construir un punto forward.
Recomendación: una modalidad nueva **NEAR_REGULAR_ADDRESS**, con selección explícita de
una dirección canónica válida dentro de una carta local C1, regular e inyectiva, y con cota
de distancia demostrada a la instantánea de Q. No se afirma que sea un mínimo de distancia.

Es una alternativa explícita al aislamiento de una raíz estacionaria, no una reinterpretación
de «mínimo aislado» en R6. En una curva continua las direcciones próximas forman un continuo;
no tiene sentido afirmar que una dirección ordinaria está aislada entre todas las direcciones
válidas. Aquí se demuestra la separación de la carta respecto de fronteras inseguras, su
pertenencia inequívoca a rama/componente y su inyectividad local. El aislamiento de una raíz
del objetivo de distancia **no se exige ni se declara**. Esta precisión requiere aprobación.
Si el autor exige un mínimo aislado, la alternativa de §4 tiene otra puerta probatoria.

La dirección estática elegida y sus padres semánticos definen P. Q sólo aporta sus coordenadas
actuales copiadas durante la consulta; no se añade un padre, algoritmo, comando ni codec.
La nueva modalidad no cambia el arrastre, R6, la autoridad constructiva ni STRICT.

## 2. Hechos del código y límites de la evidencia R1

Fuentes en `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/`:

| Fuente vigente | Hecho comprobado por inspección estática | Límite |
|---|---|---|
| `interaction/LocusPointInteractionResolver2D.localEvidence` | compara distancias en centro y vecinos con tolerancia; al menos un vecino debe ser estrictamente peor; prueba regularidad puntual o por diferencias a dos escalas | no es prueba sobre todos los parámetros de una celda arbitraria |
| `interaction/LocusPointInteractionLocalEvidence2D` | ESTABLISHED es un estado de evidencia numérica; no contiene una caja intervalar de primer jet ni prueba de monotonía | no autoriza por sí solo la modalidad nueva |
| `interaction/LocusPointInteractionResolver2D.SearchAccumulator` | mantiene `unsafeEvidence` privado; candidatos pueden sobrevivir a una barrera | lista, reason diagnóstico y residual no demuestran ausencia de barreras |
| `interaction/LocusPointInteractionResult2D` | conserva cobertura/status/getUniqueCandidate; un único candidato de búsqueda general queda UNRESOLVED | confirmación no convierte ese resultado en permiso |
| `interaction/LocusSnapshotPlacement2D` | query/commit de un solo uso, estado efímero y publicación forward; admite UNIQUE/MULTIPLE; guarda definición capturada y registro | no es aún el contexto probatorio más fuerte propuesto aquí |
| `../geos/GeoLocusV2` | publicación con revisión semántica estrictamente creciente; conserva lastPublishedRevision tras descartar definición | ayuda frente a ABA de publicaciones, no es prueba de todo cambio de host o de ciclo de vida |
| `LocusV2PublicOperations` | creación interactiva valida revisión/dirección, crea inputs ocultos, rol R6 y algoritmo forward con requiredAddress; limpia fallos | su parámetro candidate pertenece al modelo R6 de «mínimo», no al nuevo resultado tipado |
| `CertifiedAffineLocus2D` | certificado estructural, no ajuste a muestras | sus coeficientes binary64 no se vuelven exactos por llamarlo certificado |
| `PiecewisePolynomialLocus2D` | advierte que los coeficientes pueden ser expansiones redondeadas | no basta interpretarlos como polinomios exactos del objeto |
| `metric/LocusDifferentialEvaluator2D` | diferencial puntual | no encierra F' en una celda |
| `intersection/CertifiedIntervalCurveModel2D` | contrato privado al paquete; cajas exteriores de F/F', C1 y periodo/canonicalización | no es una API disponible hoy para interacción; no ofrece F'' genérico |
| `intersection/ConstructionIntervalModel2D` | consume captura v1/v2 del evaluador real, aplica predicados uniformes; lanza rechazo cuando no puede evaluar | no cubre todo evaluador ni todo programa v3; periodo 0 en este adaptador |
| `intersection/SplineIntervalModel2D` | captura spline representado y continuidad estructural, con primer jet intervalar | no certifica automáticamente toda curva por coeficientes públicos |

`ReconstructibleLocusEvaluator2D.captureCertifiedProgram()` valida fidelidad sobre su copia
aislada; no es una extracción de una elipse ideal desde puntos dibujados. La captura y la
evaluación pueden cambiar el estado de esa copia; la futura integración debe comprobar que
no cambia el documento ni la evidencia de la revisión consumidora.

R1 aporta ejecuciones Java reales previas sobre el nativo del autor, tanto en main anterior
como en el candidato. R2 las conserva y las cita; no las vuelve a ejecutar ni atribuye a R2.
El contraejemplo racional de §4 sólo refuta la suficiencia lógica de un patrón de muestreo;
no se ha ejecutado como consulta del resolvedor ni se afirma un nuevo fallo productivo.

## 3. Teorema local suficiente y garantías separadas

Sea q la pareja binary64 finita copiada de Q, interpretada como valor transitorio de la
consulta; no se afirma que sea el valor ideal exacto de un Q dependiente. Sea rho > 0 el
radio world explícito, y F la curva semántica de una configuración identificada y admitida
por la política del documento. No se sustituye F por la curva que convenga a esta consulta.

Una evidencia aceptable consiste en:

1. Dirección canónica a=(fuente,b,c,u0), u0 persistible, perteneciente inequívocamente a un
   componente vigente certificado. Intervalo cerrado no trivial U que contiene u0 en su
   interior y está contenido en el interior de una carta válida de ese componente.
2. Captura fiel de F y garantía C1 sobre **todo** U, sin divisores dudosos, predicados
   indecididos, fronteras no certificadas, singularidad ni cambio de salida.
3. Vector fijo n, inicialmente uno de los ejes coordenados, y cota rigurosa mu > 0 tal que
   n·F'(u) >= mu para todo u de U (o la desigualdad negativa orientada equivalente).
4. Caja exterior B de F(U). Con aritmética exterior, sup(x en B)||x-q||² <= rho².
   Se recomienda margen estricto para que el redondeo o la transferencia de configuración
   no decidan una igualdad; igualdad no demostrada implica rechazo.
5. Evaluación forward normal de u0 finita y coherente con el modelo admitido. Como control
   adicional, su representación flotante también debe satisfacer el límite de la consulta;
   ese control numérico no sustituye la prueba exterior del punto semántico.

Para u<v en U, n·(F(v)-F(u)) = integral[u,v] n·F'(t) dt >= mu(v-u)>0.
Luego F es regular e inyectiva en U. La caja demuestra la cota de distancia para u0 sin
localizar ningún cero de d'(u), donde d(u)=||F(u)-q||². La conclusión no excluye otra
preimagen fuera de U, otro componente ni otra dirección próxima. El radio es política de
descubrimiento, no identidad. Las cotas no son una proyección ortogonal ni un mínimo global.

Esta es una condición **suficiente, conservadora**, no necesaria: una carta que gira mucho
puede necesitar partición; si no se prueba un eje monótono dentro del presupuesto se rechaza.
No se ajusta un eje a muestras para inferir un signo. Las cajas deben encerrar el modelo
semántico real bajo su contrato, incluidos los errores de expansión o captura pertinentes.

Separación de garantías:

| Proposición | Prueba requerida | Lo que NO prueba |
|---|---|---|
| dirección forward válida | componente, dominio, canonicalización y F(u0) | proximidad ni mínimo |
| cota a q | caja exterior con margen | unicidad ni estado ideal de Q |
| regularidad e inyectividad local | C1 y signo uniforme de una proyección de F' | inyectividad global ni aislamiento de un mínimo |
| mínimo local aislado | evidencia específica de §4, si se pide | completitud global |
| unicidad global | exclusión y completitud del alcance declarado | no se deduce de tener un solo candidato |
| vigencia/seguridad | contexto verificable y revalidación al publicar | no se deduce de las cotas geométricas |

En `constructive-authority/v1` se preservan ADR 0040 y el modelo certificado §12: una prueba
en la configuración capturada no se transfiere al objeto constructivo por proximidad.
Sólo sirven rutas ya admitidas, por ejemplo margen con enclosures constructivos rigurosos,
con sus premisas y revisión. Sin vínculo probado con la autoridad activa no se ofrece el
candidato. La modalidad nueva no establece incidencia STRICT ni altera el grado de P.
En documentos de autoridad heredada se conserva su configuración representada aprobada.
No cambiar la política del documento para hacer admisible una consulta.

## 4. Alternativas y caracterización matemática reproducible

| Alternativa | Garantía y coste | Decisión recomendada |
|---|---|---|
| confirmar ESTABLISHED del R6 actual | sólo evidencia flotante; faltan caja, separación y barreras públicas | rechazar como ampliación |
| exigir siempre mínimo local aislado | probar raíz estacionaria y aislamiento; por ejemplo d'=0 y d''>0 con existencia, o prueba equivalente | viable con capacidad adicional específica; innecesario para crear forward |
| carta regular próxima, selección explícita | primer jet intervalar + cota world; no afirma mínimo | elegir para el primer perímetro |
| cualquier muestra finita próxima | no demuestra regularidad ni seguridad del componente | rechazar |

Para la segunda alternativa, el criterio de un mínimo interior sería demostrar existencia
de un cero de g=(F-q)·F' y aislamiento, por ejemplo g'>0 en la celda más signos opuestos
en sus extremos. g' requiere F'' o una prueba equivalente. El adaptador intervalar común
inspeccionado ofrece primer jet, no esa prueba genérica. Las capacidades exactas/estacionarias
de clases específicas CU2/CU3 no autorizan extrapolarla a todo proveedor.

La [sonda racional aislada](../../geocedg/validation/pre-g9b-cu/m08s-r2-mathematical-probe.py)
demuestra tres proposiciones sin evaluar Java ni usar trigonometría flotante como oráculo:

- En F(u)=(u,0), q=(0,.01), u0=.02, el punto está a menos de .1 y d'(u0)=.04: cercanía
  rigurosa no exige mínimo. La búsqueda inicial puede seguir usando semillas R6; este
  ejemplo no promete que la búsqueda actual descubra ese u0 no estacionario.
- H(u)=(u,1-16u²(u²-1)²(u²-1/4)²) es regular, con H'_x=1. Para q=(0,0), los vecinos
  u=±1,±1/2 están más lejos que u=0, pero d''(0)=-2: el centro es un máximo local estricto.
  Las derivadas en esos vecinos también coinciden con las de (u,1). Un número finito de
  guardas no prueba un mínimo en una familia arbitraria.
- Para la familia elíptica de §5, una celda interior próxima tiene distancia acotada y
  derivada y estrictamente positiva. Es una demostración condicional de una familia, no un
  certificado del programa capturado de h.

## 5. Primera clase de proveedores y h/ReferenceR1

Perímetro recomendado: celdas interiores finitas de programas reconstruibles v1/v2 que el
capturador existente acepta fielmente, con predicados uniformes y primer jet intervalar.
No se añade un lenguaje de expresiones ni un proveedor universal. Afinidad estructural y
spline estructural C1 son controles y posibles adaptadores adicionales, sólo si se demuestra
fidelidad de sus representaciones. Las rutas R6 ya válidas siguen su contrato sin obligarlas
a pasar por esta modalidad. No ampliar a cajas con seam, knot no certificado, extremo,
gaps, componentes puntuales/colapsadas o salida multivaluada no decidida.

El nativo `C:/Temp/TestM08-S.cedg` conserva SHA-256
`4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d`.
Su definición y la evidencia Java se conservan en el [informe R1](../validation/pre_g9b_cu7_i1_m08s_r1_report.md).
I=(.4965866445356883,1.7854818601338847), J=(-.5082406249102692,2.5763181370126467),
f=Circle(I,J), L=Point(f), g=Line(L,xAxis), K=Intersect(g,yAxis), M=Midpoint(K,L),
h=LocusV2(M,L). Q de la sonda es ReferenceR1=(.8976484384297626,1.7854818601338847).

En la construcción ideal sin sustitución de autoridad, si r=||J-I||, la fórmula esperada es
F(u)=(I_x/2+(r/2)cos u,I_y+r sin u), con la orientación/parametrización correspondiente.
La sonda racional verifica r en (1.27,1.29), U=[-1/1024,1/1024], y, para **toda** esa familia,
F'_y >= 1.27(1-1/(2·1024²)) > 0 y una caja de F(U) dentro del radio .1 de ReferenceR1.
No calcula r mediante ajuste a F(0); usa las constantes libres y desigualdades reales
|sin u|<=|u|, cos u>=1-u²/2. Es un margen suficiente incluso con esa banda amplia de radios.

**No establecido para h real:** captura v1/v2 aceptada en runtime, constantes y base del
driver realmente capturadas, equivalencia con la fórmula, semántica activa del documento y
transferencia pertinente, predicados uniformes, caja exterior efectiva, vínculo a revisión,
sumario de barreras y publicación por el nuevo permiso. No se equiparan la elipse ideal y
la representación binary64 del círculo. El parámetro R1 cercano a cero no es prueba de raíz
exacta, ni la posición redondeada de Q implica un mínimo exactamente en u=0.

R1 observó estado VALID, componente `generator.main/component/periodic-closure` y existencia
COMPLETE; consulta bounded con un candidato numérico local y unicidad global no establecida.
La completitud de componentes no es completitud inversa. El adaptador intervalar v1/v2 devuelve
periodo 0 aunque el proveedor declare cierre: no se certifica una caja que cruce el seam por
esa declaración. La celda interior de la familia evita ese problema, sin resolverlo globalmente.
Por tanto h es un objetivo plausible para la primera clase, **no admitido todavía**.

## 6. Frontera y desarrollo posterior

El [contrato tipado y textos candidatos](pre_g9b_cu7_i1_r2_normative_amendment_candidate.md)
definen primero significados, antes de proponer ubicación de código. La
[matriz y puertas](../validation/pre_g9b_cu7_i1_r2_matrix_and_gates.md) distinguen prueba
matemática, evidencia Java heredada y pruebas futuras no ejecutadas. El
[informe de entrega](../validation/pre_g9b_cu7_i1_r2_report.md) registra verificación y alcance.

No dependencia de M09, A0, reconstrucción general ni M08-D. CU10, integración posterior de la
guía, deuda de tangencias exactas y aprobaciones históricas permanecen intactos.
