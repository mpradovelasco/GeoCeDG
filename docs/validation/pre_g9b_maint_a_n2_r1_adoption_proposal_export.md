# Propuesta de adopción — `cedg-reference-export/v1` y ADR 0045

**PROPUESTA DE APROBACIÓN — NO APROBADA.** Preparada por `PRE-G9B-MAINT-A-N2-R1`, completada
por `PRE-G9B-MAINT-A-N2-R2` y corregida por `PRE-G9B-MAINT-A-N2-R3` (M2, M3) y `PRE-G9B-MAINT-A-N2-R4` (m-1 a m-3). `selfApproved=false`. Este documento no registra `AUTHOR APPROVED`.

| Campo | Valor |
|---|---|
| Contrato | [`geocedg/specs/export/dxf-reference-export.md`](../../geocedg/specs/export/dxf-reference-export.md), `cedg-reference-export/v1`, revisión R4 |
| ADR | [ADR 0045](../adr/0045-separate-dxf-export-of-imported-references.md), propuesto, revisión R4 |
| Candidato | commit de contenido R4 y su árbol: [informe R4](pre_g9b_maint_a_n2_r4_report.md) §1 (un documento no puede contener su propio SHA) |
| `STATIC` | [informe R4](pre_g9b_maint_a_n2_r4_report.md) §8 |

## 1. Decisiones autorales ya fijadas que aplica

- D-R1-1, D-R1-5 (ningún estado de aproximación fallida), D-R1-6 (estado 4: alternativa A, la
  referencia se incluye por defecto y la construcción modificada se exporta desde
  `Construction`), D-R1-7 (AutoCAD Core Console y ezdxf), D-R1-10 (sin bandera por novedad).
- **RX-D18 (R2):** alternativa A con condiciones. Tres niveles de evidencia distintos
  (cumplimiento del formato DXF; fidelidad `REFERENCE_DEFINITION`, cuya relectura correctamente
  redondeada ezdxf debe reproducir **exactamente**; conformidad AutoCAD). Para AutoCAD: cada
  valor dentro de 4 ulp como criterio inicial de ensayo para AutoCAD 2026 (`ACADVER` 25.1), no
  como cota del producto ni del
  lector; además estructura conservada (grado, nudos y multiplicidades, dominio, cierre,
  condiciones de pesos) y geometría evaluada dentro de la tolerancia; si una de estas falla, no
  hay conformidad aunque todos los valores estén dentro de 4 ulp; se registran lector, versión,
  fixtures, máximos y límites (contrato §12.3).
- **RX-D19 (R2):** un valor de coordenada, nudo o peso cuyo decimal llevaría exponente de tres
  cifras (`|x| < 10^-99` no nulo o `|x| ≥ 10^100`, ambos signos) hace la referencia
  `NOT_WRITABLE`/`EXPONENT_NOT_PORTABLE`; nunca se reescribe ni se sustituye por polilínea; el
  usuario puede excluirla y el diagnóstico se conserva; la escritura sin exponente queda como
  mejora posterior sujeta a pruebas (contrato §5.2). Es una limitación de AutoCAD, no del formato
  ni de ezdxf. **R4:** no se amplía a referencias excluidas por otras causas (preferencia, área,
  representada); decisión del autor de 2026-11-26.

## 2. Decisiones que se proponen con la adopción

| Id | Propuesta |
|---|---|
| RX-D1 | casilla en el diálogo DXF existente del flujo extendido emparejado (`chooseRequest`), desmarcada por defecto; el flujo G5 heredado no se amplía |
| RX-D3 | pesos en forma canónica: solo si algún peso efectivo ≠ 1 |
| RX-D4 | un código 41 tras cada punto de control (forma del escritor de AutoCAD; ambos lectores aceptan las dos) |
| RX-D5 | bandera 1 solo con cierre exacto |
| RX-D6 | escribir 210/220/230 = 0/0/1; omitir 42/43 |
| RX-D7 | capa fija `GEOCEDG_REF` |
| RX-D8 | referencias ocultas incluidas con `60 = 1` |
| RX-D9 | área de exportación por la envolvente del polígono de control |
| RX-D10 | conmutadores por operación; preferencia persistente en el gestor |
| RX-D11 | sistema de coordenadas del modelo (`data`) |
| RX-D12 | manifiesto obligatorio con referencias (versión 4): DXF y manifiesto emparejados en las situaciones A y B; en la situación C, ni DXF ni manifiesto (R3) |
| RX-D13 | dependencia estricta del contrato y de los hitos RC-M y DXF1-M; integración tras RC |
| RX-D15 | referencias con datos idénticos: ambas incluidas y señaladas |

**Decisiones normativas pendientes:** ninguna. La única cuestión que el informe R3 (§11) dejó
registrada, ampliar el diagnóstico `EXPONENT_NOT_PORTABLE` de RX-D19 a referencias excluidas
por otras causas, quedó **resuelta negativamente** por el autor el 2026-11-26 (instrucción R4):
RX-D19 no se amplía ahora. Las obligaciones de conformidad externa son verificables y se
ejecutan en `MAINT-A-DXF0-EXP` (RX-16, RX-17, RX-19), como la nueva ruta del controlador para
la exportación solo de referencias (RX-15, R4).

## 3. Enmiendas incluidas (grupo X)

A-IMP-4, A-IMP-5, A-IMP-6, A-BS-1, A-EXP-1 a A-EXP-6 (con §12.6 de R3, §12.8 y §12.9 de R2;
A-EXP-5 con el alcance de X1C), A-GEF-1 (con la exportación solo de referencias y la regla de
área, R3), A-ADR-1
y A-ADR-2 del [conjunto de enmiendas](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md).

## 4. Garantías y límites

- Exportación ordinaria idéntica byte a byte con la opción desactivada.
- Fidelidad `REFERENCE_DEFINITION`: relectura correctamente redondeada al mismo binary64,
  comprobada antes de promover; no afirma nada sobre el archivo original.
- Racionales como `SPLINE` racional, nunca como polilínea.
- Inclusión por asociación explícita vigente; nunca por coordenadas.
- ezdxf: lectura exacta exigida. AutoCAD: conformidad por criterios de ensayo (4 ulp para AutoCAD 2026, `ACADVER` 25.1; estructura,
  geometría) para las versiones y fixtures registrados; no es una garantía universal.
- Límite: una referencia con exponentes no portables no se exporta mientras no se excluya.
- Límite (R4): la opción existe solo en el flujo extendido emparejado; con
  `cedg.export.dxf.extended` desactivada (activada por defecto) solo corre el flujo G5 heredado
  y no puede exportarse ninguna referencia.
- Manifiesto (R3, M2): con la opción activada, una exportación completada produce DXF y
  manifiesto emparejados si alguna referencia se escribe (A) o se excluye, por el usuario, por
  preferencia o por la política aprobada (B); una operación rechazada por una referencia
  `NOT_WRITABLE` no produce DXF ni manifiesto y da la causa en su resultado (C).
- Exportación solo de referencias (R3, M3): posible cuando el modelo neutral está vacío y al menos
  una referencia queda incluida y escribible; sin entidades ni referencias escribibles no se
  escribe archivo; las referencias nunca entran en el modelo neutral.

## 5. Dependencias

Requiere la adopción de `cedg-reference-store/v1`. Sus estados representados dependen de
`dxf-spline-approximation/v1` §10 y §11 (hito DXF1-M); antes de la operación A-DXF1 solo existen
referencias sin representación y elecciones del usuario. Habilita `MAINT-A-DXF0-EXP`, nunca en
paralelo con `MAINT-A3`.

## 6. Recomendación

Adoptar `cedg-reference-export/v1` revisión R4 y aceptar ADR 0045 revisión R4, con las enmiendas
del grupo X.
