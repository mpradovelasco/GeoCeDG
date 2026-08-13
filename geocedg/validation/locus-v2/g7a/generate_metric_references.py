#!/usr/bin/env python3
"""Generate independent high-precision reference values for G7A.

The Java characterization algorithms are intentionally not reused here.
Run with the pinned ``om_env`` Conda environment documented in the output.
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
EXPECTED_PATH = Path(__file__).with_name("metric-reference-values.json")


def decimal(value: mp.mpf) -> str:
    """Return enough digits to independently constrain double candidates."""
    return mp.nstr(value, PRECISION_DECIMAL_DIGITS + 2)


def generate() -> dict[str, object]:
    """Generate the deterministic reference manifest."""
    mp.mp.dps = PRECISION_DECIMAL_DIGITS
    ellipse = 12 * mp.ellipe(1 - (mp.mpf(2) / 3) ** 2)
    exponential = mp.quad(
        lambda parameter: mp.sqrt(1 + mp.exp(2 * parameter)), [0, 1]
    )
    parabola = mp.quad(
        lambda parameter: mp.sqrt(1 + 4 * parameter**2), [-1, 0, 1]
    )
    cusp = mp.mpf(2) / 27 * (13 * mp.sqrt(13) - 8)
    alias_slope = mp.mpf("12.8") * mp.pi
    alias_period_integral = mp.quad(
        lambda phase: mp.sqrt(1 + alias_slope**2 * mp.cos(phase) ** 2),
        [0, mp.pi / 2, mp.pi, 3 * mp.pi / 2, 2 * mp.pi],
    )
    evaluator_alias = alias_period_integral / (2 * mp.pi)

    return {
        "schema_version": 1,
        "id": "cedg.validation.g7a.metric-reference-values",
        "authority": "independent validation evidence; never kernel authority",
        "runtime": {
            "implementation": "CPython",
            "version": platform.python_version(),
            "distribution": "conda-forge om_env",
            "library": "mpmath",
            "library_version": mp.__version__,
            "precision_decimal_digits": PRECISION_DECIMAL_DIGITS,
        },
        "references": [
            {
                "id": "ellipse-a3-b2-total",
                "curve": "F(t)=(3 cos(t), 2 sin(t)), t in [0,2*pi]",
                "equation": "4*a*E(1-b^2/a^2)",
                "method": "mpmath complete elliptic integral",
                "value": decimal(ellipse),
            },
            {
                "id": "exponential-graph-0-1",
                "curve": "F(t)=(t, exp(t)), t in [0,1]",
                "equation": "integral_0^1 sqrt(1+exp(2*t)) dt",
                "method": "mpmath arbitrary-precision quadrature",
                "value": decimal(exponential),
            },
            {
                "id": "parabola-minus1-1",
                "curve": "F(t)=(t,t^2), t in [-1,1]",
                "equation": "integral_-1^1 sqrt(1+4*t^2) dt",
                "method": "mpmath split arbitrary-precision quadrature",
                "value": decimal(parabola),
            },
            {
                "id": "cusp-minus1-1",
                "curve": "F(t)=(t^2,t^3), t in [-1,1]",
                "equation": "integral_-1^1 sqrt(4*t^2+9*t^4) dt = 2*(13*sqrt(13)-8)/27",
                "method": "symbolic antiderivative evaluated by mpmath",
                "value": decimal(cusp),
            },
            {
                "id": "evaluator-alias-64-cycles",
                "curve": "F(t)=(t,0.1*sin(128*pi*t)), t in [0,1]",
                "equation": "integral_0^1 sqrt(1+(12.8*pi*cos(128*pi*t))^2) dt",
                "method": "period reduction plus mpmath split quadrature",
                "value": decimal(evaluator_alias),
            },
        ],
    }


def rendered() -> str:
    """Render with a stable key and whitespace policy."""
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
            raise SystemExit("metric-reference-values.json is stale")
        print("G7A independent metric references match the versioned manifest.")
        return 0
    print(output, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
