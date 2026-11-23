# PRE-G9B-MAINT-P0 — registro de la resolución autoral definitiva

**PRE-G9B-MAINT-P0 — PLANIFICACIÓN APROBADA POR EL AUTOR — IMPLEMENTACIÓN NO AUTORIZADA**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. `selfApproved=false`: la decisión es del autor y este registro solo la transcribe |
| Fecha de la decisión | 23 de noviembre de 2026 |
| Resolución | «GeoCeDG — PRE-G9B-MAINT-P0. Resolución autoral definitiva del expediente de planificación», entregada por el autor en la conversación |
| Naturaleza | Aprobación de planificación y autorización de cierre documental. Implementación productiva: **no autorizada** |
| Base | GeoCeDG 1.2.2, `f80f0e9b029e678007f399508163bf794951992c` |
| Candidato resuelto | `f0f41c4bf186944f227d5b38addc719fb701ce07`, árbol `91e0525f4eb409b953fed708e2d85f135361522b`; `STATIC` `verification-d76d97b6f098471e9786c077c43f4540`, `ACCEPTED / COMPLETE` |
| Expediente conciliado | `d5d183a9662e991f5166572988b5546330509a49`, árbol `57b1f61fb0094009428cb29b9600eb73481a9055` (revisión 3); `STATIC` `verification-83820232b5db4e90aa1c7bcac05fce82`, `ACCEPTED / COMPLETE` |
| Rama | `phase/pre-g9b-maint-p0-planning`, local y sin publicar |

## 1. Decisión

El autor aprueba el marco de planificación `PRE-G9B-MAINT-P0` con tres bloques: A, B-splines,
NURBS e interoperabilidad geométrica 2D; B, corrección de limitaciones científicas y funcionales
de GeoCeDG 1.2.2; C, mejoras pendientes de PRE-G9B-CU7, con prioridad en U2 y en la sustitución
explícita de puntos. Se mantiene la arquitectura CeDG: geometría autoritativa, dependencias
dinámicas, identidad semántica, trazabilidad, persistencia y separación entre procedimientos y
resultados.

La aprobación comprende la orientación arquitectónica, la planificación incremental, sus
dependencias y las decisiones transcritas en la
[hoja de decisiones](pre_g9b_maint_p0_author_decision_sheet.md) §0. En resumen:

- aprobadas: D-0, D-ORD (como prioridad de planificación), D-V; D-A-1 a D-A-5 (dirección
  arquitectónica), D-A-6, D-A-7 (R-a primero, R-c después, R-d no autorizada), D-A-8, D-A-9
  (política de diseño), D-A-10 (dirección); D-B-1a (dirección normativa), D-B-2 (solo B2-F1),
  D-B-3, D-B-4; D-U2 (alcance propuesto); D-F8 (caracterización); D-M09-1 a D-M09-4
  (dirección, implementación pendiente); D-I2G (caracterización y diseño);
- decisión expresa negativa: **no se implementa B2-F2**; ningún barrido de auxiliares ricos al
  cargar, ninguna migración ni reparación silenciosa de documentos antiguos; los huérfanos
  heredados quedan como limitación documentada;
- pendiente: D-B-1b (política del subtotal del total condicionado; §28.2 no se modifica
  todavía);
- diferidas: D-B-1c (T-A, hasta validar B5a), D-B-1d (B5b), `MAINT-A-R` (NURBS nativa,
  diseñada) y las capacidades enumeradas en la hoja F §0.

Primera ola de trabajo futuro, aprobada como prioridad de planificación: `MAINT-N1` (enmiendas
de B5a y B2), `MAINT-B5a`, `MAINT-U2`, `MAINT-I2G0` (caracterización y, previa autorización,
barrera) y `MAINT-A-N1`.

## 2. Qué no decide

La resolución no autoriza ninguna de estas acciones, que exigen una decisión autoral expresa
posterior:

- la implementación de cualquier incremento productivo, incluidos los de la primera ola; cada
  uno requiere autorización propia sobre una base exacta;
- la promoción del expediente a `main`;
- etiquetas, releases o cambios en `geocedg_book`;
- la ejecución de `FINAL`;
- la apertura de G9B, que sigue `DESIGNED — NOT AUTHORIZED`.

La aceptación técnica de las verificaciones (`STATIC`) es un hecho independiente de la
aprobación autoral.

## 3. Cierre documental

Siguiendo el procedimiento de la resolución:

1. la rama desciende del candidato aprobado `f0f41c4b`;
2. el commit `d5d183a9` aplica solo enmiendas documentales al expediente A–G;
3. `STATIC` se ejecutó sobre ese commit exacto con resultado `ACCEPTED / COMPLETE` (los dos
   diagnósticos permanentes de gobernanza e histórico, sin efecto en la aceptación);
4. el commit que contiene este registro añade la entrada aditiva del roadmap (D-0) y es un
   descendiente exclusivamente documental de `d5d183a9`.

No cambia código, pruebas, selecciones, especificaciones ni ADR aprobados.
