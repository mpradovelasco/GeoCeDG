# Autorización de diseño e implementación local de PRE-G9B-CU4

El autor aprobó la dirección de CU4 y autorizó la implementación local el 2026-11-05. La resolución nombra el baseline `1c3893f539d926c16da837a42d439a1cf8bea34f`, árbol `1c0f6e9187663b586acfc7c425053d7be0349c21`, y el candidato de preparación `0e56d0cb81c73a69eb10abf4e5208dd48efb5f64`, árbol `13226944eaafc26bedf1ae3748915f90066d76a5`. [Expediente aprobado como referencia](pre_g9b_cu4_preparation_candidate_report.md)

- B01, B05, M01, M05.1 y H09 autorizados bajo DC-1 y DC-3 y las precisiones de la resolución. H08 diferido expresamente. La inserción conserva el remapeo de referencias mediante L1.
- B01 exige predicado angular verificable, preservación de los campos del deslizador y rechazo atómico. La entrada de defaults V2 se versiona y valida como extensión opcional. La precedencia opera sobre la creación, con persistencia personal separada del documento y del estilo individual.
- Los incrementos I0–I3 y puertas A1–A6 del expediente gobiernan el alcance. La clase se fija al inicio como `INTEGRATED_PHASE`. El candidato técnico requiere STATIC, INFRA_UNIT para el alta del catálogo, PHASE PRE-G9B-CU4 e INTEGRATION. COMPOSED resuelve a INTEGRATION en el registro activo. [Autoridad de verificación](../../geocedg/specs/operations/verification-levels.md)
- `selfApproved=false`. Revisión técnica, smoke autoral, aprobación del producto y promoción tienen decisiones separadas. CU7, las demás fases, `main`, el libro y la publicación quedan fuera de esta autorización.

`BOOTSTRAP IMPACT — NO CHANGE REQUIRED`. El perímetro conserva JDK, Gradle, dependencias y comandos de instalación. `VERIFICATION_INFRASTRUCTURE_IMPACT=CATALOG_ONLY` describe el alta de la selección y las identidades medidas.

```text
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_es.md, docs/user/geocedg_user_guide_en.md
```
