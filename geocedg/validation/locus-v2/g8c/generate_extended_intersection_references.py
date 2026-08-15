#!/usr/bin/env python3
"""Generate independent high-precision G8C design references.

The Java characterization probes are intentionally not imported.  This file is
validation evidence only and has no runtime or kernel authority.
"""

from __future__ import annotations

import argparse
import json
import platform
from pathlib import Path

import mpmath as mp


PRECISION_DECIMAL_DIGITS = 80
EXPECTED_PYTHON = "3.12.13"
EXPECTED_MPMATH = "1.4.1"
EXPECTED_PATH = Path(__file__).with_name(
    "extended-intersection-reference-values.json"
)


def decimal(value: mp.mpf) -> str:
    """Render an arbitrary-precision real deterministically."""
    return mp.nstr(value, PRECISION_DECIMAL_DIGITS + 2)


def point(x: mp.mpf, y: mp.mpf) -> dict[str, str]:
    """Return one reference point."""
    return {"x": decimal(x), "y": decimal(y)}


def parameter_pair(t: mp.mpf, u: mp.mpf) -> dict[str, str]:
    """Return one dual-parameter reference solution."""
    return {"t": decimal(t), "u": decimal(u)}


def generate() -> dict[str, object]:
    """Build the deterministic G8C reference manifest."""
    mp.mp.dps = PRECISION_DECIMAL_DIGITS
    sqrt_three_over_two = mp.sqrt(3) / 2
    circle_angle = mp.acos(mp.mpf("0.5"))
    return {
        "schema_version": 1,
        "id": "cedg.validation.g8c.extended-intersection-references",
        "authority": "independent design evidence; never kernel authority",
        "runtime": {
            "implementation": "CPython",
            "version": platform.python_version(),
            "distribution": "conda-forge om_env",
            "library": "mpmath",
            "library_version": mp.__version__,
            "precision_decimal_digits": PRECISION_DECIMAL_DIGITS,
        },
        "single_parameter_references": [
            {
                "id": "ellipse-secant",
                "target": "x^2/4+y^2=1",
                "source": "F(t)=(t,3/5), t in [-2,2]",
                "method": "exact substitution and quadratic factorization",
                "completeness": "COMPLETE",
                "roots": [decimal(mp.mpf("-1.6")), decimal(mp.mpf("1.6"))],
                "multiplicities": [1, 1],
            },
            {
                "id": "parabola-tangent",
                "target": "y=x^2",
                "source": "F(t)=(t,0), t in [-1,1]",
                "method": "exact substitution h(t)=t^2",
                "completeness": "COMPLETE",
                "roots": [decimal(mp.mpf("0"))],
                "multiplicities": [2],
            },
            {
                "id": "hyperbola-secant",
                "target": "x^2/4-y^2=1",
                "source": "F(t)=(t,0), t in [-3,3]",
                "method": "exact substitution",
                "completeness": "COMPLETE",
                "roots": [decimal(mp.mpf("-2")), decimal(mp.mpf("2"))],
                "multiplicities": [1, 1],
            },
            {
                "id": "function-polynomial",
                "target": "y=f(x), f(x)=x^2-1",
                "source": "F(t)=(t,0), t in [-2,2]",
                "residual": "rho=y-f(x)",
                "method": "exact polynomial factorization",
                "completeness": "COMPLETE",
                "roots": [decimal(mp.mpf("-1")), decimal(mp.mpf("1"))],
            },
            {
                "id": "function-pole-components",
                "target": "y=1/x",
                "source": "F(t)=(t,0)",
                "declared_components": ["[-1,0)", "(0,1]"],
                "method": "analytic nonzero numerator on each valid component",
                "completeness": "COMPLETE_ONLY_FOR_DECLARED_COMPONENTS",
                "roots": [],
                "invalid_boundary": decimal(mp.mpf("0")),
            },
            {
                "id": "regular-polynomial-implicit",
                "target": "G(x,y)=x^2+y^2-1",
                "source": "F(t)=(t,0), t in [-2,2]",
                "method": "exact polynomial factorization",
                "completeness": "COMPLETE",
                "roots": [decimal(mp.mpf("-1")), decimal(mp.mpf("1"))],
                "gradient_norm_at_roots": decimal(mp.mpf("2")),
            },
            {
                "id": "singular-polynomial-implicit",
                "target": "G(x,y)=y^2-x^3",
                "source": "F(t)=(t,0), t in [-1,1]",
                "method": "exact substitution h(t)=-t^3",
                "roots": [decimal(mp.mpf("0"))],
                "gradient_norm_at_root": decimal(mp.mpf("0")),
                "regular_normal_residual": "UNDEFINED",
                "local_isolation": "REQUIRES_STRONGER_EVIDENCE",
            },
            {
                "id": "implicit-scale-invariance",
                "target_forms": ["G=x^2+y^2-1", "1000G=0"],
                "point": point(mp.mpf("0.8"), mp.mpf("0.8")),
                "raw_residual_ratio": decimal(mp.mpf("1000")),
                "first_order_normal_residual_ratio": decimal(mp.mpf("1")),
                "method": "symbolic scaling cancellation in G/|gradient G|",
            },
        ],
        "locus_locus_references": [
            {
                "id": "transverse-line-pair",
                "first": "F(t)=(t,t)",
                "second": "Q(u)=(u,1-u)",
                "domains": "t,u in [0,1]",
                "method": "exact linear system",
                "solutions": [parameter_pair(mp.mpf("0.5"), mp.mpf("0.5"))],
                "coordinates": [point(mp.mpf("0.5"), mp.mpf("0.5"))],
                "normalized_tangent_determinant": decimal(mp.mpf("-1")),
                "completeness": "COMPLETE",
            },
            {
                "id": "unit-circle-pair",
                "first": "F(t)=(cos(t),sin(t))",
                "second": "Q(u)=(1+cos(u),sin(u))",
                "fundamental_domains": "t,u in [0,2*pi)",
                "method": "circle subtraction plus exact trigonometry",
                "solutions": [
                    parameter_pair(circle_angle, 2 * circle_angle),
                    parameter_pair(2 * mp.pi - circle_angle,
                                   2 * mp.pi - 2 * circle_angle),
                ],
                "coordinates": [
                    point(mp.mpf("0.5"), sqrt_three_over_two),
                    point(mp.mpf("0.5"), -sqrt_three_over_two),
                ],
                "completeness": "COMPLETE_ON_FUNDAMENTAL_DOMAINS",
            },
            {
                "id": "tangent-parametric-pair",
                "first": "F(t)=(t,0)",
                "second": "Q(u)=(u,u^2)",
                "domains": "t,u in [-1,1]",
                "method": "exact elimination t=u, u^2=0",
                "solutions": [parameter_pair(mp.mpf("0"), mp.mpf("0"))],
                "jacobian_determinant": decimal(mp.mpf("0")),
                "local_isolation": "EXACT_HIGHER_ORDER_EVIDENCE_REQUIRED",
                "multiplicity": 2,
            },
            {
                "id": "reverse-reparameterized-overlap",
                "first": "F(t)=(t,t^2), t in [-1,1]",
                "second": "Q(u)=(-u,u^2), u in [-1,1]",
                "parameter_map": "u=-t",
                "method": "exact component-wide substitution",
                "result_kind": "OVERLAP_ESTABLISHED",
                "finite_samples_are_authority": False,
            },
            {
                "id": "source-order-symmetry",
                "unordered_source_pair": "{A,B}",
                "forward_evidence": parameter_pair(mp.mpf("0.5"), mp.mpf("0.5")),
                "reverse_evidence": parameter_pair(mp.mpf("0.5"), mp.mpf("0.5")),
                "durable_identity": "EQUIVALENT_UNDER_OPERAND_SWAP",
                "method": "exact permutation of the two-parameter system",
            },
            {
                "id": "constructive-multiplicity",
                "first": "F(t)=(t^2-1,0)",
                "second": "Q(u)=(0,u)",
                "domains": "t in [-2,2], u in [-1,1]",
                "method": "exact factorization t^2-1=0 and u=0",
                "solutions": [
                    parameter_pair(mp.mpf("-1"), mp.mpf("0")),
                    parameter_pair(mp.mpf("1"), mp.mpf("0")),
                ],
                "coordinates": [point(mp.mpf("0"), mp.mpf("0"))] * 2,
                "semantic_solution_count": 2,
                "cartesian_coordinate_count": 1,
            },
        ],
        "characterization_boundaries": {
            "unbounded_domains": "no COMPLETE claim from a finite window",
            "singular_roots": "small residual alone cannot establish isolation",
            "overlap": "matching samples can only support suspicion",
            "source_order": "ordered solver inputs, unordered geometric source pair",
            "identity": "parameter pairs and isolating boxes are revision evidence",
        },
        "scientific_source_ids": [
            "cedg.reference.lsim-preprint-2022",
            "cedg.reference.intersection-flattening-2023",
            "cedg.reference.book-2023",
            "cedg.reference.tools-and-oblique-cone-2025",
        ],
    }


def rendered() -> str:
    """Render with stable JSON layout."""
    return json.dumps(generate(), indent=2, ensure_ascii=True) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if platform.python_version() != EXPECTED_PYTHON:
        raise SystemExit(
            f"Expected CPython {EXPECTED_PYTHON}, got {platform.python_version()}"
        )
    if mp.__version__ != EXPECTED_MPMATH:
        raise SystemExit(f"Expected mpmath {EXPECTED_MPMATH}, got {mp.__version__}")
    output = rendered()
    if args.check:
        expected = json.loads(EXPECTED_PATH.read_text(encoding="utf-8"))
        if generate() != expected:
            raise SystemExit(
                "extended-intersection-reference-values.json is stale"
            )
        print("G8C independent extended-intersection references match.")
        return 0
    print(output, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
