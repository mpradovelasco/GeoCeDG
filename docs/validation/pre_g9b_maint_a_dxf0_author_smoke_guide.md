# PRE-G9B-MAINT-A-DXF0 — guía de smoke manual del autor

| Campo | Valor |
|---|---|
| Incremento | `PRE-G9B-MAINT-A-DXF0`, importación DXF `SPLINE` como referencia externa de solo lectura (`dxf-spline-reference/v1` revisión 2, persistencia `R-a`) |
| Estado | `AUTHOR_SMOKE = PENDING`. `selfApproved=false`. Esta guía prepara la comprobación; no la registra |
| Informe del candidato | [pre_g9b_maint_a_dxf0_candidate_report.md](pre_g9b_maint_a_dxf0_candidate_report.md) |

## 1. Archivos de prueba

Los archivos DXF son ASCII, escritos a mano y deterministas (ninguna herramienta externa los
generó). Están en el árbol de pruebas, en
`source/shared/common-jre/src/test/resources/org/geocedg/common/importer/dxf/`, y la prueba
`PreG9BMaintADxf0FidelityTest.committedFixturesAreTheDocumentedBuilderOutput` comprueba byte a
byte que son la salida documentada de `DxfTestFiles`. Copie la carpeta a un directorio de
trabajo antes del smoke y anote el SHA-256 de cada archivo
(`Get-FileHash -Algorithm SHA256 *.dxf`).

| Archivo | Contenido |
|---|---|
| `dx1-quarter-circle-Q.dxf` | cuarto de circunferencia racional Q de la matriz §2.1 (pesos `1, 0.7071067811865476, 1`), `$INSUNITS = 4` (mm) |
| `dx1-semicircle-H.dxf` | semicircunferencia H: dos cuartos racionales, nudo interior `1` de multiplicidad 2 |
| `dx1-polynomial-cubic.dxf` | cúbica polinómica con un nudo interior simple, `$INSUNITS = 0` |
| `dx13-weight-representations.dxf` | las seis representaciones de pesos admitidas de DX-13 (W1 a W6) |
| `smoke-mixed.dxf` | una `LINE`, Q, la cúbica, una entidad rechazada (`BAD1`: pesos no unitarios sin bandera racional) y una curva cerrada exactamente sin bandera de cierre (`CL1`) |

## 2. Preparación

1. Construya el candidato y abra GeoCeDG desde ese árbol. Use un dibujo nuevo.
2. En **Opciones → Unidades del documento…** deje la unidad sin especificar para el primer
   recorrido; en el recorrido 6 elija `mm`.
3. Cree algún objeto de construcción (por ejemplo `A=(0,0)` y `s=Segmento((-3,-2),(3,-2))`) y
   anote el estado de **Deshacer**.

## 3. Recorridos

| # | Pasos | Resultado esperado |
|---|---|---|
| 1 | **Archivo → Importar referencia DXF…**, elegir `smoke-mixed.dxf`, aceptar la transformación identidad | Informe: `LINE 1, SPLINE 4`; 3 referencias admitidas (Q1, C1, CL1); 1 rechazada `BAD1 ... NON_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG`; `CL1` con `CLOSED_FLAG_CONTRADICTED`; texto final que no afirma exactitud respecto del texto decimal ni del render. Se abre la ventana **Referencias DXF importadas** |
| 2 | Observar la vista gráfica | Curvas discontinuas moradas; la `LINE` no se dibuja (no es candidata); la construcción no cambia; Álgebra y el protocolo no muestran nada nuevo; **Deshacer** igual que antes |
| 3 | Pasar el cursor y pulsar sobre una referencia con **Mover**, y con **Punto** | Nada se selecciona ni se resalta; con **Punto** se crea un punto libre corriente, no un punto «sobre» la referencia |
| 4 | **Inspeccionar** cada referencia | Estado «Referencia importada, solo lectura»; naturaleza (`RATIONAL` para Q, `POLYNOMIAL` para la cúbica), contrato `dxf-spline-reference/v1`, cierre, continuidad estructural, archivo y SHA-256, versión `AC1015`, entidad (ordinal, identificador, capa), unidades, plano, transformación, resúmenes de datos, diagnósticos, render y aviso de que no se publica ninguna medida |
| 5 | **Mostrar/Ocultar** y **Quitar** | La curva desaparece/reaparece o se elimina; **Deshacer** no cambia; Deshacer/Rehacer de un paso de construcción no afecta a las referencias |
| 6 | Unidad del documento `mm`; importar `dx1-polynomial-cubic.dxf` con `$INSUNITS = 0`, luego un archivo con unidades distintas (por ejemplo `dx1-quarter-circle-Q.dxf` con la unidad del documento en `cm`) | El diálogo muestra ambas unidades; con unidades distintas aparece el aviso no bloqueante; con identidad nada se escala |
| 7 | Importar `dx1-quarter-circle-Q.dxf` con escala uniforme `10` y después con afín `a=1 b=0 c=0 d=1 e=2 f=0` | La curva aparece escalada o desplazada; el inspector registra los seis coeficientes; una afín singular (`a=1 b=2 c=2 d=4`) se rechaza con `SINGULAR_TRANSFORM` sin importar nada |
| 8 | Zoom in/out, cambiar tamaño de letra o mover la vista | La forma de la curva no cambia (render calculado una vez en unidades del modelo); el arco de Q se ve liso a la escala normal |
| 9 | **Archivo → Guardar** con referencias presentes | Aviso no bloqueante en la barra de estado: las referencias son de sesión y no se han guardado |
| 10 | **Nuevo**; repetir importación y **Abrir** el dibujo guardado; repetir importación y **Cerrar** | En los tres casos las referencias desaparecen; al reabrir no queda ninguna |
| 11 | Exportar **Vista gráfica como imagen** (PNG/PDF/SVG), **Imprimir**, copiar la vista al portapapeles, **Exportar DXF** y PGF/PSTricks/Asymptote con referencias visibles | Ninguna exportación contiene referencias (Q-DXF-3: comportamiento actual declarado; decisión del autor pendiente). Las exportaciones semánticas DXF/LaTeX nunca las incluyen |
| 12 | Tras todo el recorrido, recalcular el SHA-256 de los archivos DXF | Idéntico al anotado: el archivo nunca se modifica |
| 13 | Repetir 1 y 4 con la interfaz en inglés | Textos en inglés («Import DXF reference…», «Imported reference, read-only») |

## 4. Registro

Anote para cada recorrido `PASS`, `FAIL` u `OBSERVATION` con la versión del candidato. El
smoke del autor es independiente de la aceptación técnica (`STATIC`, `PHASE`,
`INTEGRATION`), que no lo sustituye.
