# PRE-G9B-CU4 — Registro de cierre autoral

**`PRE-G9B-CU4 = PASS — AUTHOR APPROVED`.** Decisión expresa del autor del 2026-11-07. El autor confirma el éxito del smoke manual final y acepta la desincronización visual del selector angular como deuda diferida y no bloqueante. El `PASS` se aplica al producto exacto de esta tabla y a los descendientes documentales controlados de cierre. `selfApproved=false`.

| Identidad | Valor |
|---|---|
| Producto aprobado | `ba4d16018cf683750a20619d9d59c84428bcbef7` |
| Árbol del producto | `6dc9ae9ecdd4dc05f3fb0a5fb23d3320d06ea477` |
| Rama técnica | `codex/pre-g9b-cu4-r2-text-diag` |
| Baseline de `main` verificado en GitHub | `1c3893f539d926c16da837a42d439a1cf8bea34f` |
| Rama documental de revisión | `codex/pre-g9b-cu4-closeout` |
| Espejo estructurado | [pre-g9b-cu4-closeout.json](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu4-closeout.json) |

## Alcance aprobado

- B01, B05, M01, M05.1 y H09 dentro de DC-1 y DC-3, incluida la compatibilidad de preferencias de apariencia heredadas sin efectos sobre la construcción.
- B2 entre modos angulares, incluido `Text(theta)` terminal dentro de la clase certificada. Se acepta la corrección del falso rechazo por el modo de rótulo interno de los escalares trigonométricos sin representación gráfica.
- Diagnósticos diferenciados de P4, B2 y herramientas. La alternativa manual se ofrece cuando la admisión ordinaria resulta potencialmente aplicable y exige volver a pasar F3/P4.
- Garantías de identidad, dependencias, transacción, persistencia y Undo/Redo. La pertenencia a vistas sigue la copia nativa F3.

La [especificación de inserción §10](../../geocedg/specs/ui/document-insertion.md#10-pre-g9b-cu4-amendment) adopta B2 con la clase y las precondiciones verificadas por el guard y el certificado del producto. La matriz P4 conserva sus filas. Los modos iguales siguen la ruta ordinaria. Con modos distintos, se exige certificación, confirmación vigente, prueba en un receptor aislado y transacción atómica. La [propuesta B2](../architecture/pre_g9b_cu4_b2_document_insertion_amendment_candidate.md) queda como antecedente del candidato `ba4d16018`. Los casos fuera de la clase certificada siguen sujetos a una futura caracterización y decisión autoral.

## Evidencia y alcance de la verificación

| Evidencia | Atribución |
|---|---|
| `FINAL` `verification-09c9bfe645124b19976cc9975ca7bd6e`, código 0, `ACCEPTED / COMPLETE`, 41 comprobaciones satisfechas | Producto `ba4d16018` y su árbol exacto. La campaña comprende `INFRA_UNIT`, `INTEGRATION` y las 130 pruebas autenticadas de `PHASE PRE-G9B-CU4`. |
| Recibo `65a6ab2e89a1e83b7f375624903cc4912f9f152b7b6b796e8de5e2923bad47b0` | `INSPECT` confirmó la identidad del producto y el árbol. El informe aceptado tiene identidad `f4da6fb58fbd0e16207a7822620d7946c30afd1987e516c7e05f3af0e6ff9262`. |
| `STATIC` y comprobaciones focales de enlaces, espejo y delta documental | Puerta del commit documental limpio, con identidad y resultados en la evidencia de cierre. |

El recibo, su informe y el cotejo de cobertura se conservan en `artifacts/agent/pre-g9b-cu4-r2-text-diag/`. El [informe técnico](pre_g9b_cu4_r2_text_diag_candidate_report.md) describe las pruebas y sus diagnósticos. `FINAL` pertenece al producto congelado. El cierre solo cambia documentación normativa, de planificación y de trazabilidad. Los resultados del cierre y de publicación se registran en `artifacts/agent/pre-g9b-cu4-closeout/`, fuera del commit de cierre para evitar autorreferencias.

`VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY`. `VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED` porque se conservan los ejecutables, el verificador y los prerrequisitos de la estación.

```text
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = Se revisaron el índice y las dos guías vigentes. Ya describen la inserción B2 certificada, la confirmación y la política de vistas. Este cierre adopta ese comportamiento y registra una deuda, con las guías empaquetadas intactas.
```

## OBS-CU4-ANGLE-PREFERENCES-REFRESH

**Disposición.** Deuda funcional abierta, diferida por el autor y no bloqueante para CU4. Prioridad para la siguiente actuación que reciba autorización. Fase del roadmap por determinar.

Al abrir un documento guardado en grados con el panel de Preferencias ya creado, su selector puede seguir marcando radianes. El kernel carga correctamente grados. La discrepancia puede inducir una lectura errónea del modo del documento y del flujo F3/B2. La sonda Swing lo reprodujo con `native-receiver.cedg` y `formatted.ggb`, ambos guardados en grados. Al refrescar el panel se ve el modo efectivo y, si se selecciona radianes antes de insertar, aparece la confirmación B2. Evidencia local en `artifacts/agent/pre-g9b-cu4-r2-text-diag/smoke-angle-preferences/observacion.md` y `java-desktop-02.log` (código 0).

La futura corrección deberá refrescar el selector al abrir o cambiar de documento y comprobar su coherencia con el kernel para grados, radianes y DMS. Cubrirá panel abierto, creación posterior del panel, reapertura y Undo/Redo cuando afecte al estado mostrado. El refresco visual leerá el modo efectivo sin cambiarlo. Se requieren regresiones Swing y un smoke focal.

## Límites y publicación

Siguen abiertos H08, las exclusiones de P4 y la deuda histórica F3 de la instantánea Undo con macros de `InterCilConoOblique.cedg`. La pertenencia a gráficas conserva la política heredada. La [propuesta separada de seguimiento](../architecture/pre_g9b_cu4_p4_expert_insertion_followup_proposal.md) registra la revisión P4, la modalidad experta y la deuda del selector. Cada actuación futura exige su propia delimitación y autorización.

El autor autoriza publicar la rama de revisión e integrar su commit documental en `main` exclusivamente por avance rápido no forzado desde el baseline de la tabla, previa comprobación del remoto. La protección del remoto sigue aplicándose. La autorización de publicación se limita al resultado de CU4 y a su cierre documental, con `geocedg_book` preservado.
