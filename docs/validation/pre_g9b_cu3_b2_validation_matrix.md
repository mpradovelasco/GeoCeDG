# PRE-G9B-CU3 — Matriz de pruebas de `CU3-B2` (G-B2-0)

| Campo | Valor |
|---|---|
| Carácter | Matriz candidata para revisión autoral. Las filas B2-5 a B2-7 y B2-14 a B2-15 se cierran tras las sondas de G-B2-1 |
| Diseño | [diseño de `CU3-B2`](../architecture/pre_g9b_cu3_b2_regular_tangent_design.md); [hoja de decisiones](pre_g9b_cu3_b2_decision_sheet.md) |
| `selfApproved` | `false` |

| Fila | Caso | Prueba prevista | Prueba adversaria |
|---|---|---|---|
| B2-1 | ancla de la tangencia obligatoria | réplica del autor: `Intersect(o_1, h_2)` con token admisible y germen regular | el mismo caso bajo la política heredada: sin ancla |
| B2-2 | lado: reparametrización | componente con el conductor invertido y con otra parametrización regular: mismo `σ`, mismo germen, mismo token | `σ` calculado con una orientación no canónica del objetivo: debe cambiar, y el producto no debe usarla |
| B2-3 | lado: representación del objetivo | recta y cónica multiplicadas por `c < 0` y `c > 0`; indicador implícito frente a distancia | rayo invertido: barrera `TARGET_ORIENTATION_CHANGED` |
| B2-4 | lado: vista | zoom y tamaño del lienzo distintos: mismo token | auditoría estática: ninguna entrada de vista en `σ` |
| B2-5 | `CONTACT_SPLIT` | mover el objetivo para que corte en dos raíces: punto indefinido, token regular latente, dos transversales actuales | ninguna transversal tiene el token regular; ninguna lo hereda por rango |
| B2-6 | reunión | volver a la tangencia: reactivación del mismo token regular | las transversales dejan de ser actuales con su causa tipada |
| B2-7 | tokens transversales a ambos lados | comportamiento caracterizado en G-B2-1 | ninguna reasociación por proximidad |
| B2-8 | `CONTACT_LOST` y reaparición | `NONE` certificado y vuelta: mismo token | sin token nuevo durante la ausencia |
| B2-9 | `CONTACT_NOT_CERTIFIED` | caso casi tangente con clase 3: punto indefinido, evidencia visible | no se materializa ninguna intersección |
| B2-10 | barrera por `h''` | contacto que pasa a orden superior: `IDENTITY_DISCONTINUITY` | tras la barrera, el mismo lugar con otra tangencia no reactiva el token |
| B2-11 | rango | dos tangencias con el mismo germen: tokens distintos por rango | rotación periódica no excluida: cuarentena, no elección |
| B2-12 | persistencia | guardado y reapertura con entradas `T`: mismos tokens; versión según D-B2-3 | lector v5 y lector v6 de `CU3-B1`: rechazo cerrado |
| B2-13 | contador de encarnaciones | abrir y guardar sin editar, con entradas `F` y `T`: contador estable si D-B2-4 (a); subida medida y registrada si (b) | asignaciones posteriores con tokens nuevos |
| B2-14 | redefinición efectiva del soporte del conductor (entrada `T`) | redefinición admitida por el núcleo que cambia el linaje: barrera, token latente, sin reasociación; Deshacer la restaura | contacto en la misma posición tras la redefinición: no hereda el token |
| B2-15 | redefinición efectiva del soporte del conductor (entrada `F`) | la misma prueba para un ancla de pliegue (salda `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER`) | igual |
| B2-16 | redefinición que conserva el linaje | cambio de parámetros del soporte: §5.1 del diseño | sin barrera espuria |
| B2-17 | Deshacer, Rehacer, copia y duplicación | estado del libro, reclamaciones; copia con identidades nuevas | ningún token reutilizado en la copia |
| B2-18 | no regresión | contactos de `CU3-A`, anclas de `CU3-B1` y tokens dorados transversales sin cambios | documentos heredados y adoptados sin tangencias: libro sin cambio de versión |
| B2-19 | integridad y admisibilidad | `Intersect(L, r, token)` admisible solo con §2 del diseño en la revisión actual | token regular latente usado como selector: inadmisible |
| B2-20 | rendimiento y corpus | medición E3 de los barridos del modelo del autor; corpus idéntico fuera de las tangencias regulares | presupuesto agotado: fallo cerrado |
