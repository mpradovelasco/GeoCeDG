# GeoCeDG 1.2.2 — registro de la aprobación autoral del candidato de cierre

**GeoCeDG 1.2.2 — CANDIDATO DE CIERRE `a6562fa30` — PASS — AUTHOR APPROVED**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad del estado de aprobación de GeoCeDG 1.2.2. `selfApproved=false`: la decisión es del autor y este registro solo la transcribe |
| Fecha de la decisión | 23 de noviembre de 2026 |
| Rama | `maintenance/geocedg-1.2.2-post-r1`, local y sin publicar |
| Candidato aprobado | `a6562fa30dc7a468b8e62e16843f1ae2f440285b`, árbol `aac4b541b28660233cccd295791bb1a82871d108` |
| Versión y fecha de versión | GeoCeDG 1.2.2, `2026-11-22`, la fecha que el autor mantuvo el 2026-11-22 |
| Evidencia presentada | [evidencia final de cierre](geocedg_1_2_2_closing_evidence.md), commit `e24af7ba3` |
| `main` | local y `origin/main` en `9ccefdcfe922d8c0e329bdeec30c12fc058de463`, sin tocar; etiquetas de versión `v1.2.0` y `v1.2.1` |

## 1. Decisión

Pregunta presentada al autor el 2026-11-23, con la evidencia final de cierre:

> ¿Aprueba el candidato `a6562fa30` como GeoCeDG 1.2.2, o prefiere revisarlo antes?

Respuesta del autor, escrita por él en la conversación:

> Lo apruebo

El autor aprueba así el candidato `a6562fa30` como GeoCeDG 1.2.2, tal como se presentó en
la evidencia final de cierre. Esa evidencia incluye la corrección de la comprobación de cierre
(`781629cb1`, `locus-v2-metrics.md` §29) y las limitaciones residuales declaradas en su §5.
Por eso las limitaciones quedan aceptadas como límites declarados de 1.2.2, no como defectos
resueltos.

«PASS» designa aquí el resultado técnico de las verificaciones ordinarias que se ejecutaron
(`STATIC`, `PHASE -Phase POST-R1-LOCUS`, `INTEGRATION` y `PACKAGING`) y nada más; `FINAL` no
se ejecutó. «AUTHOR APPROVED» es la decisión del autor transcrita en este apartado. Son dos
hechos independientes.

## 2. Qué no decide

La aprobación no autoriza ninguna de estas acciones. Siguen sin realizarse y exigen una nueva
autorización expresa del autor para el candidato exacto:

- la ejecución de `FINAL`;
- la promoción a `main`;
- la creación de la etiqueta `v1.2.2`;
- la release y la publicación de los paquetes;
- la instalación o la prueba de los instaladores MSI y EXE, incluida una prueba en un
  entorno aislado.

El fallo histórico de Desktop
`PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`, presente en la
base `9ccefdcf`, sigue visible y fuera de esta aprobación.

## 3. Identidad aprobada

| Papel | Identidad |
|---|---|
| Candidato de cierre | `a6562fa30dc7a468b8e62e16843f1ae2f440285b`, árbol `aac4b541b28660233cccd295791bb1a82871d108` |
| `STATIC` | `verification-443faf5422ea48d5a13ef0d366037c10`, `ACCEPTED / COMPLETE` |
| `PHASE -Phase POST-R1-LOCUS` | `verification-6bb2140c1e714c28add05f4ee8d83416`, `ACCEPTED / COMPLETE` |
| `INTEGRATION` | `verification-b81359f645654b699a81d85f7473a19d`, `ACCEPTED / COMPLETE` |
| `PACKAGING` | `verification-675aa2ec12b248e7b0a9f3a31e105427`, `ACCEPTED / COMPLETE` |
| `GeoCeDG-1.2.2-windows-x64-nc.zip` | `d19ba613857d15baa0d5be639df404a488dc324ba4fd1a61a795c13cd1d5086d` |
| `GeoCeDG-1.2.2-windows-x64-nc.msi` | `ac2568077698b5b1f5621793095ab63a6c72a717a823e7004b6a2078966319f6` |
| `GeoCeDG-1.2.2-windows-x64-nc.exe` | `accb437afb6a7503b88cacf04c473e06994a35fea4a122337e2768e84d567ea1` |

Los paquetes están en `artifacts/packaging/windows-1.2.2-nc-candidate-4`, una carpeta
ignorada por Git, y se construyeron desde un árbol limpio del candidato con perfil NC.

El commit que contiene este registro es un descendiente exclusivamente documental del
candidato. No cambia código, pruebas, selecciones ni el contenido del producto.
