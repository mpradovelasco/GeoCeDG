# PRE-G9B-MAINT, segunda ola — registro de integración de B5a, A-DXF0-R1, A2 y la biblioteca compartida

| Campo | Valor |
|---|---|
| Carácter | Registro de la integración técnica autorizada. `selfApproved=false` |
| Fecha | 25 de noviembre de 2026 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre de segunda ola, consolidación Git y planificación de interoperabilidad DXF ampliada», §6, entregada por el autor en la conversación |
| Ruta | `CHANGE_ROUTE = ORDINARY` |
| Verificación de la integración | `STATIC` + `PHASE PRE-G9B-MAINT-B5A` + `PHASE PRE-G9B-MAINT-A-DXF0` + `PHASE PRE-G9B-MAINT-A2` + `INTEGRATION` (`CORE + FOCUSED`, no exhaustiva) |
| `FINAL` | `NOT AUTHORIZED` (cierre sin `FINAL` aceptado por el autor para esta ola) |
| Base común | `228580e9684d2c54650f8d461c2a4d1142a7ef55`, árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae` (`main` publicado) |
| Rama y worktree | `integration/pre-g9b-maint-second-wave-final`, worktree `GeoCeDG-wt-maint-wave2-final`, continuada desde el ensayo verificado (`integration/pre-g9b-maint-second-wave`, que se conserva sin cambios) |

## 1. Candidatos

| Incremento | Commit | Árbol | Registro |
|---|---|---|---|
| Biblioteca compartida (hito técnico) | `d22c4cc85aefc454ee8598711d717d21db57da7c` | `2d722f50cad78f656b49f9312167ed819c385b80` | informe consolidado §F |
| `PRE-G9B-MAINT-B5A` | `65ca487da35d34d500fb12e5d5c815e03eca5754` | `31ee1a88242ac3790030053e9cfe367281e64bfc` | [B5a](pre_g9b_maint_b5a_author_decision_record.md) |
| `PRE-G9B-MAINT-A-DXF0` + R1 | `47c1c2e41d6d6bea6be13f3537168e59ed280d6c` (sobre `177da00f6`) | `92e53d38bd8964e3e2dac9b0a2db4d8026894085` | [A-DXF0 y R1](pre_g9b_maint_a_dxf0_author_decision_record.md) |
| `PRE-G9B-MAINT-A2` | `c63b10aade4f2792c855c1bc4ad6278f9a54fcfc` | `602e70b7a17e9e50c7d56504acab6799ac8ca35c` | [A2](pre_g9b_maint_a2_author_decision_record.md) |

## 2. Historia

Por `git merge --no-ff`, sin squash, rebase, cherry-pick ni reescritura:

| Paso | Commit | Árbol | Contenido |
|---|---|---|---|
| B5a (primera versión `69347d9f6`) | `1e52254830887982329cc61651fa14714e9e323d` | `42a4f529fe15cbb4633afd769dc3d2fbbf80afc0` | ensayo |
| A-DXF0 `177da00f6` (trae la biblioteca una vez) | `4b7d04476ac64b6684583e3fd39af7e5138809cc` | `7082c156a05804b62fe3ebe28cd4a5a97df1c6e0` | ensayo |
| A2 (primera versión `1b82a6ed2`) | `d848713d32187e9f65bb381eac334d7de94546f9` | `011646b42a5dcd3fc9e0b222d83cb835d959d996` | ensayo |
| Conciliación del ensayo | `418d35231ab35e58604a11efde88c5354dc75f17` | `e19976fce5c29163ca26fb48651fe487a31f809d` | datos derivados |
| B5a `65ca487da` (corrección focal) | `d54a85b3142ce626fbf7118bb2c4be1434844010` | `0b155129879ff58e81970772643872a5d8ff7332` | ensayo |
| A2 `c63b10aad` (corrección focal) | `18b8ff9053071d52d21d011ca1db5f2a104f311b` | `026823261704e03edb52227d05dce6b1c0c1130e` | ensayo verificado |
| Informe consolidado del ensayo | `03cd5b135a38de3a99e9aceb69db704ebc778047` | `6488d67a442087fb0202283f4d5c6c03eed8bcbc` | solo documento |
| **A-DXF0-R1 `47c1c2e41`** | `76691ffd6b13a99ff141da1e5a2095847a90ee7e` | `8a13d974c51f6f6e864caea5a07f4bc8341379b6` | esta integración |

Los cinco candidatos son ancestros del resultado y sus ramas de origen no se modificaron.
Conflictos del merge de R1 solo en datos derivados: `docs/upstream/modified-files.yml` (unión de
las entradas añadidas: 1 491 + 3 = 1 494, sin duplicados), el inventario JUnit y los pines del
registro (reconstruidos con evidencia ejecutada en el commit de conciliación). El código de
producto, incluido `AppGeoCeDG.java`, se fusionó sin conflicto.

## 3. Conciliación

El commit de conciliación añade los tres registros de decisión autoral, este registro y la
entrada aditiva del roadmap; corrige el texto desfasado de `modified-files.yml` sobre el recuento
de encabezados de nivel 3 (105 en A2, 106 en el árbol integrado); y refresca, con los productores
y el actualizador oficiales sobre el árbol integrado, el descubrimiento Desktop y la selección
`pre-g9b-maint-a-dxf0.desktop`, únicas identidades que cambian con R1 (las demás clases que R1
toca solo cambian aserciones). `final.desktop` conserva su huella anterior por el fallo
histórico CU2, previo a la ola. Los informes de candidato, guías de smoke y matrices congelados
no se modifican.

## 4. Deudas registradas

- B5a, lectura L-6: un fallo de evaluación de la comprobación conserva la estimación del
  resultado rico con estado `SUCCESS` (frente a `locus-v2-metrics.md` §28.1); no se eleva a norma
  y queda pendiente de decisión autoral.
- Fallo histórico `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`
  (desde `9ccefdcf`), previo a la ola; no justifica modificar pruebas ajenas.
- Pin del contrato estático de `assets-manifest.yml` y `final.desktop` desfasados antes de
  cualquier `FINAL` futuro.

## 5. Verificación y publicación

La verificación integrada se ejecuta sobre el candidato que contiene este registro, con
directorios de registro nuevos. Sus identificadores y resultados los informa el artefacto
siguiente, porque un commit no puede citar sus propias ejecuciones.

La promoción a `main` exige, según `AGENTS.md` §8.1, una autorización autoral expresa que nombre
el SHA exacto del candidato. Este registro no la contiene ni la origina. La autorización de la
primera ola no se reutiliza. No se crean etiquetas, releases ni paquetes.
