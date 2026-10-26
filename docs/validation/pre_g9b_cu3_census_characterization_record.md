# PRE-G9B-CU3 — Caracterización externa del censo de procedencia y conciliación de G-AUT-1

| Campo | Valor |
|---|---|
| Carácter | Registro técnico de la caracterización externa que autoriza la disposición autoral de 2026-10-26, punto 3, y conciliación del alcance de la puerta G-AUT-1. Es evidencia: no aprueba nada, no cambia el presupuesto productivo ni declara demostrado lo que el producto dejó sin decidir |
| Autoridad | [disposición autoral](pre_g9b_cu3_author_disposition_record.md) §3; [registro de E1](pre_g9b_cu3_e1_gate_record.md) §3 y §10.1; [matriz](pre_g9b_cu3_validation_matrix.md), fila V-AUT-PROV-1; [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md) §2, §7 y §10; [ADR 0040](../adr/0040-constructive-geometric-authority-and-relation-integrity.md), decisión 3 |
| Herramienta | `tools/benchmark/pre-g9b-cu/run-cu3-census-characterization.ps1` (prueba `PreG9BCu3CensusCharacterizationProbe`), fuera del producto y de todo conjunto de fuentes, en el commit `451c1b501f7b010f279b0a1c9af34202c9913af4` |
| Evidencia | [`pre-g9b-cu3-census-characterization-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-census-characterization-evidence.json), copia byte a byte de la ejecución A (SHA-256 `35b3d3d795d573668120cc463b99901dcf87dd49c33687ad335f846444fccc0f`); la ejecución B es idéntica byte a byte |
| `selfApproved` | `false` |

## 1. Método

La sonda decide las 160 relaciones del censo de G-AUT-1 con el evaluador constructivo del producto.
Son las mismas relaciones de `PreG9BCu3ProvenanceCensusTest`, con la misma semántica, sobre una
copia del modelo del autor sin `e_2`. Cada relación se decide dos veces:

- con el presupuesto productivo aprobado, `cu3-exact-evaluation-budget/v1` (100 000 operaciones,
  profundidad 8, 4 096 bits), como hace la prueba del censo;
- con un presupuesto de caracterización igual al del estudio de la autoridad constructiva
  (200 000 000 operaciones, profundidad 48, 4 194 304 bits).

La interfaz del presupuesto del producto solo admite restringirlo (`restrictedTo`). La sonda
construye el presupuesto de caracterización por reflexión, solo dentro de ella: el producto, su
presupuesto y sus pruebas no cambian. Por cada decisión registra los contadores deterministas y si
la evaluación lee un **sustituto de frontera declarado**.

Esa última distinción importa. Una igualdad exacta que lee un sustituto es exacta **relativa a ese
sustituto**: vale en la configuración constructiva con el objeto de frontera fijado en su valor
binary64. No es una demostración para el objeto ideal (especificación §7: la evidencia con
sustitutos da como mucho `RELAXED`, y solo `SUBSTITUTE_GENERIC` o el certificado de identidad
constructiva van más allá).

## 2. Resultados

Dos ejecuciones sobre `451c1b50` con el árbol limpio dan salidas idénticas byte a byte.

| Presupuesto | Se cumplen sin sustitutos | Se cumplen con sustitutos | Agotadas | Refutadas |
|---|---:|---:|---:|---:|
| `cu3-exact-evaluation-budget/v1` (producto) | 27 | 113 | 20 | 0 |
| caracterización (fuera del producto) | 27 | 133 | 0 | 0 |

- **Las veinte relaciones agotadas** son las del cono del giro de 47°, en el mismo orden que fija la
  prueba del censo: `H`, `S` y `W` sobre `f_1`; `W` sobre `i_1` y en la parte limitada de `f_1`;
  `H`, `U` y `A_1` sobre `j_1`; `A_1` sobre `i_1` y en la parte limitada de `j_1`; `W` sobre `h_1`;
  `h_1` paralela a `k`; `A_1` sobre `k_1`; `k_1` paralela a `k`; `B_1` sobre `m_1` y `h_1` y en la
  parte limitada de `m_1`; y `C_1` sobre `l_1` y `k_1` y en la parte limitada de `l_1`.
- **Con el producto**, todas se detienen por bits, con 4 126 bits frente a 4 096, entre 1 544 y
  1 869 operaciones y profundidad 2.
- **Con la caracterización**, las veinte se cumplen exactamente, **todas leyendo sustitutos de
  frontera**. Necesitan entre 4 126 y 4 514 bits, como mucho 3 887 operaciones y profundidad 2. El
  mayor contador de todo el censo es de 10 893 operaciones, profundidad 3 y 4 514 bits.
- **Coherencia entre presupuestos.** Ninguna relación que se cumple con el producto deja de
  cumplirse con la caracterización, y ninguna se refuta con ninguno de los dos.

## 3. Conciliación de G-AUT-1 y de su matriz

La fila V-AUT-PROV-1 dice que el evaluador constructivo «hace exactas las relaciones de procedencia
del modelo» y que el censo 160/160 del estudio «solo entra como regresión». El registro de E1 dio la
puerta por superada con 140 relaciones decididas, ninguna refutada y veinte agotadas. Los resultados
de §2 fijan el alcance exacto de esa afirmación:

1. **Lo que decide el producto.**
   - Con su presupuesto aprobado, el evaluador reproduce exactamente 140 de las 160 relaciones:
     27 son identidades de los objetos constructivos, sin ningún sustituto, y 113 son igualdades
     exactas relativas a los sustitutos de frontera declarados.
   - No refuta ninguna.
   - Deja sin decidir veinte, con la causa `EXACT_EVALUATION_BUDGET_EXHAUSTED`. Siguen sin decidir
     en el producto: esta caracterización no cambia ninguna decisión del producto.
2. **Lo que añade la caracterización externa.** Fuera del producto, con un presupuesto mayor, esas
   veinte también se reproducen exactamente, relativas a los sustitutos, con 4 514 bits como mucho.
   Es evidencia de coherencia del evaluador, no una decisión del producto.
3. **Lo que ninguno de los dos demuestra.** Ni las 133 igualdades con sustitutos ni el censo en su
   conjunto demuestran las relaciones para los objetos ideales. La integridad `STRICT` de una
   relación de procedencia no viene del censo:
   - viene de la regla de procedencia, cuando la existencia del objeto ideal está establecida
     rigurosamente (ADR 0040, decisión 3; especificación §2);
   - o, para una incidencia que no es de procedencia, viene del certificado de identidad
     constructiva (los cinco contactos, G-AUT-2).

   El censo es una regresión de la semántica del evaluador: comprueba que no contradice las
   relaciones declaradas.
4. **Formulación conciliada de G-AUT-1.** Propuesta para que el autor la adopte en lugar de la
   lectura «160/160 exactas en el producto». Con `cu3-exact-evaluation-budget/v1`, el evaluador del
   producto:
   - no refuta ninguna relación de procedencia del modelo;
   - reproduce exactamente 140 relaciones: 27 sin sustitutos y 113 relativas a los sustitutos de
     frontera;
   - falla cerrado en las otras veinte, sin aproximación.

   La reproducción de esas veinte es evidencia de caracterización externa, con presupuesto propio.
   No es un resultado del producto ni una demostración para los objetos ideales.

Esta conciliación no cambia el criterio aprobado de la fila V-AUT-PROV-1. La matriz recibe una nota
fechada que remite a este registro. El registro de E1 no se reescribe.

## 4. Lo que no se ha hecho

- El presupuesto productivo no cambia. Ningún agotamiento se acepta como aproximación.
- La prueba del censo no cambia: sigue fijando las veinte relaciones agotadas.
- No se ha intentado `SUBSTITUTE_GENERIC` ni el certificado de identidad constructiva sobre las
  relaciones de procedencia. Ninguno de los dos es un consumidor del censo en el producto.
