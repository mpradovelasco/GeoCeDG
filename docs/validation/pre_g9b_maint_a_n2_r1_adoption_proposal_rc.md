# Propuesta de adopción — `cedg-reference-store/v1` (`R-c`) y ADR 0044

**PROPUESTA DE APROBACIÓN — NO APROBADA.** Preparada por `PRE-G9B-MAINT-A-N2-R1`; confirmada por `PRE-G9B-MAINT-A-N2-R2` y corregida por `PRE-G9B-MAINT-A-N2-R3` (M1) y `PRE-G9B-MAINT-A-N2-R4` (I-1, I-2).
`selfApproved=false`. Este documento no registra `AUTHOR APPROVED`: la adopción exige una
decisión expresa del autor sobre el candidato exacto.

| Campo | Valor |
|---|---|
| Contrato | [`geocedg/specs/import/reference-store.md`](../../geocedg/specs/import/reference-store.md), `cedg-reference-store/v1`, revisión R4 |
| ADR | [ADR 0044](../adr/0044-document-store-of-imported-references.md), propuesto, revisión R4 |
| Candidato | commit de contenido R4 y su árbol: [informe R4](pre_g9b_maint_a_n2_r4_report.md) §1 (un documento no puede contener su propio SHA) |
| Historia | A-N2 `874286e95` (árbol `30c21042c`), descendiente `e7803badb`; R1 en `phase/pre-g9b-maint-a-n2-r1` |
| `STATIC` | [informe R4](pre_g9b_maint_a_n2_r4_report.md) §8 |

## 1. Decisiones autorales ya fijadas que aplica

D-R1-1 (nada en `Construction`, el DAG ni la identidad CeDG), D-R1-2 (sin compatibilidad con
lectores antiguos; integridad, fallo seguro, sin pérdida silenciosa), D-R1-3 (`source` y `data`
obligatorios), D-R1-4 (sin límite fijo arbitrario; política configurable y medida), D-R1-5 (sin
historial de derivaciones ni de fallos), D-R1-8 (Classic: conservación opaca o barrera de
guardado; nunca pérdida silenciosa), D-R1-10 (sin bandera por novedad).

## 2. Decisiones que se proponen con la adopción

| Id | Propuesta |
|---|---|
| RC-D1, RC-D2 | entrada ZIP fija `geocedg_references.xml`, versión `cedg-reference-store/v1` dentro |
| RC-D3 | números como patrones de bits en 16 hex (medido: tras deflate, 7–17 % más que binario) |
| RC-D5 | quitar una referencia fuera de Undo, con confirmación |
| RC-D6 | Insertar archivo no transfiere referencias |
| RC-D7 | almacén corrupto: rechazo del documento entero; «Abrir sin referencias» como incremento aparte |
| RC-D8 | guarda `geocedg_*` y entrada única |
| RC-D9 | valores iniciales del presupuesto **medidos en R3** y **adoptados por el autor el 2026-11-26** como valor provisional y configurable de producto (2 000 000 no se elige sin nueva evidencia): 10 000 referencias (igual que el importador), 1 500 000 valores binary64 serializados contados por el contrato §5.2 punto 12 y bytes de entrada `17 × maxValues + 1 KiB × maxReferences` (asignación agregada de cadenas); ciclo completo Open → Save → Reopen dentro de un cuarto de un heap de 1 GiB para los corpus medidos (100 % de `maxValues`; 77 % y 91 % de `maxEntryBytes`); el peor caso admisible al 100 % de `maxEntryBytes` no se midió; el valor 3 000 000 de R1 se midió y no se confirmó (caso extremo 704–768 MiB); alternativa documentada: 2 000 000 valores (256–320 MiB); C-4, puerta de cierre de `MAINT-A-DXF0-RC`, los vuelve a medir sobre la implementación, incluido ese peor caso |
| RC-D11 | nombre visible guardado solo al renombrar |
| RC-D16 (R4) | normal del plano de origen: se conservan los grupos 210, 220 y 230 realmente presentes y su presencia (`normalGroups`, `normal`); presencia parcial distinta de una terna completa; la normal efectiva (210 y 220 ausentes = 0, 230 ausente = 1) es derivada y nunca se guarda; el registro de importación gana la presencia por grupo de forma aditiva en `MAINT-A-DXF0-RC` (A-IMP-12), porque el lector actual rellena los ausentes con `0.0` (contrato §5.2 punto 11, F22) |
| RC-D17 (R4) | contador `values` normativo y exhaustivo: `v_r = K_r + (4 + e_r) · c_r + 7 + g_r` sobre los binary64 serializados (nudos, coordenadas de control, pesos explícitos, coordenadas transformadas, seis coeficientes, `planeZ`, componentes presentes de la normal); nada implícito cuenta; mismo cálculo en escritor y lector, independiente del orden XML; sin restricción geométrica nueva; mediciones de R3 válidas como cota superior (contrato §5.2 punto 12, §5.3) |
| RC-D13 | forma normativa: contrato + enmiendas + ADR 0044 |
| RC-D15 | configuración por preferencia GeoCeDG con anulación administrativa por propiedad del sistema; solo valores finitos |

**Decisiones pendientes:** una, **D-R4-1**: autorizar que `MAINT-A-DXF0-RC` añada de forma
aditiva al registro de importación la presencia de cada grupo 210, 220 y 230 (enmienda
A-IMP-12), sin cambiar admisión ni valores. Sin ese cambio RC-D16 no puede cumplirse, porque el
lector actual rellena los ausentes con `0.0` (contrato F22); la revisión final R4 lo clasificó
como decisión del autor, no del agente. RC-D9, RC-D16 y RC-D17 figuran en esta tabla porque su
gramática es propuesta de R4; el requisito y el valor del presupuesto son decisiones del autor
de la instrucción R4. El presupuesto de 1 500 000 valores quedó adoptado por el
autor en la instrucción R4 (RC-D9); C-4 vuelve a medir el presupuesto sobre la
implementación, incluido el caso al 100 % de `maxEntryBytes` no medido en R3, como puerta de
cierre de `MAINT-A-DXF0-RC`. El lector incremental con límites por atributo (§5.3, clase 3)
es una protección añadida por la medición de R3, no una decisión abierta.

## 3. Enmiendas incluidas (grupo R)

A-IMP-1, A-IMP-2, A-IMP-3, A-IMP-9, A-IMP-10, A-IMP-12 (R4), A-NDI-1 y A-ADR-3 del
[conjunto de enmiendas](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md). Se aplican
a los textos aprobados solo mediante un descendiente documental posterior a la adopción.

## 4. Garantías y límites

- Copia autoritativa de cada referencia en el documento; reapertura sin el DXF; verificación bit
  a bit de la transformación y de los resúmenes.
- Procedencia de la normal sin pérdida: grupos presentes y su presencia; la normal efectiva es
  derivada (R4).
- Transición atómica en todas las rutas de apertura y sustitución; fallo seguro con restauración.
- Sin pérdida silenciosa en las versiones que declaran `R-c`, Classic incluido.
- Límite: los lectores ≤ 1.2.2 pierden el almacén al guardar (declarado en notas de versión).
- Límite: un almacén por encima del presupuesto de la estación se rechaza con causa tipada; el
  presupuesto se amplía de forma explícita.

## 5. Dependencias

Ninguna para adoptar. Habilita, con autorización aparte: `MAINT-A-DXF0-RC-S0` (C-1 obligatoria)
y después `MAINT-A-DXF0-RC`, que cierra solo con C-4 y C-5 en el presupuesto. Es requisito de
los otros dos contratos (identidad y preferencias).

## 6. Recomendación

Adoptar `cedg-reference-store/v1` revisión R4 y aceptar ADR 0044 revisión R4, con las enmiendas del grupo R
y las decisiones de §2.
