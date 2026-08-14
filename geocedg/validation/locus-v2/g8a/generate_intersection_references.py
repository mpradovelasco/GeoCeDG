#!/usr/bin/env python3
"""Generate independent high-precision G8A intersection references.

The test-private Java characterization strategies are intentionally not reused.
Run with the pinned ``om_env`` Conda environment recorded in the output.
"""

from __future__ import annotations

import argparse
import json
import math
import platform
from pathlib import Path

import mpmath as mp


PRECISION_DECIMAL_DIGITS = 80
EXPECTED_PYTHON = "3.12.13"
EXPECTED_MPMATH = "1.4.1"
EXPECTED_PATH = Path(__file__).with_name("intersection-reference-values.json")


def decimal(value: mp.mpf) -> str:
    """Render enough digits to constrain an independent double result."""
    return mp.nstr(value, PRECISION_DECIMAL_DIGITS + 2)


def root_record(label: str, value: mp.mpf, multiplicity: int) -> dict[str, object]:
    """Create one exact/high-precision root record."""
    return {
        "label": label,
        "parameter": decimal(value),
        "multiplicity": multiplicity,
    }


def float_normalized_circle_residual(height: float, root: float) -> float:
    """Measure binary64 residual independently from the Java probe."""
    return abs(root * root + height * height - 1.0) / max(
        1.0, root * root + height * height, 1.0
    )


def tolerance_measurements() -> dict[str, object]:
    """Produce deterministic evidence for proposed, non-normative tolerances."""
    residuals: list[float] = []
    for geometry_scale in (1e-6, 1.0, 1e6):
        for height_ratio in (0.0, 0.6, 0.999999999999):
            height = geometry_scale * height_ratio
            root = geometry_scale * math.sqrt(1.0 - height_ratio**2)
            raw = root * root + height * height - geometry_scale**2
            residuals.append(
                abs(raw) / max(1.0, geometry_scale * geometry_scale)
            )
    near_tangent_delta = mp.mpf("1e-12")
    near_tangent_root = mp.sqrt(near_tangent_delta)
    near_tangent_derivative = 2 * near_tangent_root
    clustered_half_gap = mp.mpf("1e-8")
    mapping_error = abs(
        float(float(mp.root(mp.mpf("0.512"), 3)) ** 3) - 0.512
    )
    return {
        "binary64_max_normalized_residual": format(max(residuals), ".17g"),
        "near_tangent_delta": decimal(near_tangent_delta),
        "near_tangent_root": decimal(near_tangent_root),
        "near_tangent_derivative_magnitude": decimal(near_tangent_derivative),
        "clustered_distinct_root_gap": decimal(2 * clustered_half_gap),
        "monotone_mapping_binary64_roundtrip_error": format(mapping_error, ".17g"),
        "candidate_policy": {
            "status": "PROPOSED_FROM_G8A_MEASUREMENTS_NOT_NORMATIVE",
            "root_parameter_tolerance": "1e-12",
            "absolute_residual_tolerance": "2e-12",
            "relative_residual_tolerance": "2e-12",
            "tangency_threshold": "1e-10",
            "deduplication_parameter_tolerance": "4e-12",
            "continuation_parameter_tolerance": "1e-8",
            "coordinate_verification_tolerance": "4e-12",
        },
    }


def generate() -> dict[str, object]:
    """Generate the deterministic reference and measurement manifest."""
    mp.mp.dps = PRECISION_DECIMAL_DIGITS
    focal_root = mp.sqrt(1 - mp.mpf("0.6") ** 2)
    cluster = mp.mpf("1e-8")
    monotone_root = mp.root(mp.mpf("0.512"), 3)
    return {
        "schema_version": 1,
        "id": "cedg.validation.g8a.intersection-reference-values",
        "authority": "independent validation evidence; never kernel authority",
        "runtime": {
            "implementation": "CPython",
            "version": platform.python_version(),
            "distribution": "conda-forge om_env",
            "library": "mpmath",
            "library_version": mp.__version__,
            "precision_decimal_digits": PRECISION_DECIMAL_DIGITS,
            "rounding_policy": "mpmath decimal rendering; binary64 sweeps use IEEE-754 CPython float",
        },
        "references": [
            {
                "id": "transverse-two-roots",
                "equation": "h(t)=t^2-1",
                "domain": "[-2,2]",
                "method": "symbolic factorization",
                "completeness": "COMPLETE",
                "roots": [
                    root_record("left", mp.mpf(-1), 1),
                    root_record("right", mp.mpf(1), 1),
                ],
            },
            {
                "id": "even-tangent-root",
                "equation": "h(t)=t^2",
                "domain": "[-1,1]",
                "method": "symbolic factorization and derivative",
                "completeness": "COMPLETE",
                "roots": [root_record("tangent", mp.mpf(0), 2)],
            },
            {
                "id": "higher-and-clustered-roots",
                "equation": "h(t)=(t+1/3)^3*(t-1e-8)*(t+1e-8)",
                "domain": "[-1,1]",
                "method": "symbolic factorization",
                "completeness": "COMPLETE",
                "roots": [
                    root_record("triple", -mp.mpf(1) / 3, 3),
                    root_record("cluster-left", -cluster, 1),
                    root_record("cluster-right", cluster, 1),
                ],
            },
            {
                "id": "complete-empty",
                "equation": "h(t)=t^2+1",
                "domain": "[-10,10]",
                "method": "positive polynomial lower bound h(t)>=1",
                "completeness": "COMPLETE",
                "roots": [],
            },
            {
                "id": "focal-reduced-secant",
                "scientific_role": "reduced focal sphere/cone separatrix pilot",
                "equation": "F_mu(t)=(t,mu), G(x,y)=x^2+y^2-1, mu=0.6",
                "method": "substitution and analytic discriminant",
                "completeness": "COMPLETE",
                "roots": [
                    root_record("left", -focal_root, 1),
                    root_record("right", focal_root, 1),
                ],
            },
            {
                "id": "focal-reduced-tangent",
                "scientific_role": "reduced focal sphere/cone separatrix pilot",
                "equation": "F_mu(t)=(t,mu), G(x,y)=x^2+y^2-1, mu=1",
                "method": "substitution and zero discriminant",
                "completeness": "COMPLETE",
                "roots": [root_record("tangent", mp.mpf(0), 2)],
            },
            {
                "id": "focal-reduced-empty",
                "scientific_role": "reduced focal sphere/cone separatrix pilot",
                "equation": "F_mu(t)=(t,mu), G(x,y)=x^2+y^2-1, mu=1.2",
                "method": "negative analytic discriminant",
                "completeness": "COMPLETE",
                "roots": [],
            },
            {
                "id": "lsim-reduced-four-leaf",
                "scientific_role": "two-branch 2D cone-cylinder LSIM topology proxy",
                "equation": "F_sigma(t)=(t,sigma*(t^2-lambda)), target y=0, sigma=+-1, lambda=0.25",
                "method": "per-branch symbolic factorization",
                "completeness": "COMPLETE_PER_TWO_DECLARED_BRANCHES",
                "roots_per_branch": [
                    root_record("negative-leaf", mp.mpf("-0.5"), 1),
                    root_record("positive-leaf", mp.mpf("0.5"), 1),
                ],
                "total_constructive_preimages": 4,
            },
            {
                "id": "reparameterization-increasing",
                "equation": "t=s^3, original t=0.512",
                "method": "principal real arbitrary-precision cube root",
                "original_parameter": "0.512",
                "reparameterized_parameter": decimal(monotone_root),
                "durable_identity_effect": "UNCHANGED_ONLY_WITH_EXPLICIT_SEMANTIC_CONTINUATION_MAP",
            },
            {
                "id": "reparameterization-reversal",
                "equation": "t=-s, original roots t=-0.8,+0.8",
                "method": "exact affine substitution",
                "reparameterized_roots": ["-0.8", "0.8"],
                "orientation": "REVERSED",
            },
            {
                "id": "periodic-seam",
                "equation": "F(t)=(cos(t),sin(t)), domain [0,2*pi]",
                "method": "exact periodic equivalence",
                "equivalent_parameters": [decimal(mp.mpf(0)), decimal(2 * mp.pi)],
                "constructive_preimages": 1,
            },
        ],
        "tolerance_measurements": tolerance_measurements(),
        "scientific_source_ids": [
            "cedg.reference.lsim-preprint-2022",
            "cedg.reference.intersection-flattening-2023",
            "cedg.reference.book-2023",
            "cedg.reference.tools-and-oblique-cone-2025",
        ],
        "scientific_boundary": "sources motivate topology and dependency; reduced formulas are independently declared G8A fixtures",
    }


def rendered() -> str:
    """Render with stable key order and whitespace."""
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
        expected = EXPECTED_PATH.read_text(encoding="utf-8")
        if output != expected:
            raise SystemExit("intersection-reference-values.json is stale")
        print("G8A independent intersection references match the manifest.")
        return 0
    print(output, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
