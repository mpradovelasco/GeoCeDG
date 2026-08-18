#!/usr/bin/env python3
"""Generate independent deterministic G9A2 point/reference evidence.

The generator uses only Python Decimal arithmetic and closed-form orthographic
frames. It imports no GeoCeDG implementation code. The resulting JSON is an
analytic oracle and planning/test input, not a product solver.
"""

from __future__ import annotations

import argparse
from decimal import Decimal, getcontext
import json
from pathlib import Path
import sys


getcontext().prec = 80

D = Decimal


def vector(*values: str) -> list[str]:
    return [str(D(value)) for value in values]


def frame(frame_id: str, origin: list[str], u: list[str], v: list[str],
          normal: list[str]) -> dict[str, object]:
    return {
        "id": frame_id,
        "origin": origin,
        "u": u,
        "v": v,
        "normal": normal,
        "direction": normal,
        "projectionFamily": "ORTHOGRAPHIC",
    }


def pythagorean_frame(t: Decimal) -> dict[str, object]:
    denominator = D(1) + t * t
    sine = D(2) * t / denominator
    cosine = (D(1) - t * t) / denominator
    return frame(
        "near-horizontal",
        vector("0", "0", "0"),
        [str(cosine), "0", str(-sine)],
        vector("0", "1", "0"),
        [str(sine), "0", str(cosine)],
    )


def build_reference() -> dict[str, object]:
    horizontal = frame(
        "horizontal",
        vector("0", "0", "0"),
        vector("1", "0", "0"),
        vector("0", "1", "0"),
        vector("0", "0", "1"),
    )
    vertical = frame(
        "vertical",
        vector("0", "0", "0"),
        vector("1", "0", "0"),
        vector("0", "0", "1"),
        vector("0", "-1", "0"),
    )
    profile = frame(
        "profile",
        vector("0", "0", "0"),
        vector("0", "1", "0"),
        vector("0", "0", "1"),
        vector("1", "0", "0"),
    )
    identity_map = {
        "family": "ORIENTED_ISOMETRY",
        "matrix": [["1", "0"], ["0", "1"]],
        "translation": ["0", "0"],
        "determinantSign": 1,
        "unitScale": "1",
    }
    folded_vertical_map = {
        "family": "ORIENTED_ISOMETRY",
        "matrix": [["1", "0"], ["0", "-1"]],
        "translation": ["0", "0"],
        "determinantSign": -1,
        "unitScale": "1",
        "foldSide": "NEGATIVE_INTRINSIC_V",
    }
    profile_auxiliary_map = {
        "family": "ORIENTED_ISOMETRY",
        "matrix": [["0", "-1"], ["1", "0"]],
        "translation": ["0", "0"],
        "determinantSign": 1,
        "unitScale": "1",
        "frameUseRole": "AUXILIARY",
    }
    gauge = {
        "family": "ORIENTED_ISOMETRY",
        "matrix": [["0", "-1"], ["1", "0"]],
        "translation": ["7", "-11"],
        "determinantSign": 1,
        "unitScale": "1",
    }
    rank_factor = D("1e-12")
    below_parameter = rank_factor
    above_parameter = rank_factor * D(8)
    below = pythagorean_frame(below_parameter)
    above = pythagorean_frame(above_parameter)
    maximum_singular_value = D(2).sqrt()
    rank_threshold = (rank_factor * D(4)
                      * max(D(1), maximum_singular_value))

    def smallest_singular_value(parameter: Decimal) -> Decimal:
        cosine = (D(1) - parameter * parameter) / (
            D(1) + parameter * parameter)
        return (D(1) - cosine).sqrt()

    return {
        "schemaVersion": 1,
        "id": "cedg.validation.g9a2.point-reference-values",
        "status": "G9A2_IMPLEMENTATION_CANDIDATE_ANALYTIC_REFERENCE",
        "generator": {
            "runtime": "Python standard library Decimal",
            "precisionDigits": getcontext().prec,
            "candidateImplementationImported": False,
            "randomness": False,
        },
        "numericPolicy": {
            "absoluteTolerance": "1e-10",
            "relativeTolerance": "1e-10",
            "rankRelativeTolerance": "1e-12",
            "mapTolerance": "1e-10",
            "hingeTolerance": "1e-10",
            "conditionLimit": "1e12",
            "rankThresholdFormula": (
                "rankRelativeTolerance * max(rows, columns) * "
                "max(1, abs(sigmaMax))"
            ),
            "nearRankMatrixShape": [4, 3],
            "nearRankMaximumSingularValue": str(maximum_singular_value),
            "nearRankThreshold": str(rank_threshold),
        },
        "frames": [horizontal, vertical, profile],
        "diagramMaps": {
            "horizontalIdentity": identity_map,
            "verticalFolded": folded_vertical_map,
            "profileAuxiliary": profile_auxiliary_map,
            "commonGauge": gauge,
        },
        "relations": [
            {
                "id": "horizontal-vertical-hinge",
                "kind": "HINGE_UNFOLD",
                "sourceMap": "horizontalIdentity",
                "destinationMap": "verticalFolded",
                "supportStart": vector("0", "0", "0"),
                "supportEnd": vector("1", "0", "0"),
                "orientation": "POSITIVE",
                "provenance": "EXPLICIT_CONSTRUCTION",
                "foldSide": "OPPOSITE_DIAGRAM_SIDE",
                "horizontalIntrinsicLine": "q_v=0",
                "verticalIntrinsicLine": "q_v=0",
                "commonDiagramLine": "p_v=0",
            },
            {
                "id": "horizontal-profile-change",
                "kind": "CHANGE_OF_PLANE",
                "sourceMap": "horizontalIdentity",
                "destinationMap": "profileAuxiliary",
                "destinationFrameUseRole": "AUXILIARY",
                "supportStart": vector("0", "0", "0"),
                "supportEnd": vector("0", "1", "0"),
                "orientation": "POSITIVE",
                "provenance": "EXPLICIT_CONSTRUCTION",
                "foldSide": "NOT_APPLICABLE",
                "commonDiagramLine": "p_u=0",
            },
        ],
        "pointCases": [
            {
                "id": "general-two-frame",
                "spatialPoint": vector("2", "3", "5"),
                "intrinsic": {
                    "horizontal": vector("2", "3"),
                    "vertical": vector("2", "5"),
                },
                "diagram": {
                    "horizontal": vector("2", "3"),
                    "vertical": vector("2", "-5"),
                },
                "certificate": "VALID",
            },
            {
                "id": "point-on-horizontal-plane",
                "spatialPoint": vector("2", "3", "0"),
                "intrinsic": {
                    "horizontal": vector("2", "3"),
                    "vertical": vector("2", "0"),
                },
                "certificate": "VALID",
            },
            {
                "id": "one-view",
                "intrinsic": {"horizontal": vector("2", "3")},
                "certificate": "UNDERDETERMINED",
            },
            {
                "id": "repeated-frame",
                "intrinsic": {
                    "horizontal-1": vector("2", "3"),
                    "horizontal-2": vector("2", "3"),
                },
                "certificate": "UNDERDETERMINED",
            },
            {
                "id": "three-view-consistent",
                "spatialPoint": vector("2", "3", "5"),
                "intrinsic": {
                    "horizontal": vector("2", "3"),
                    "vertical": vector("2", "5"),
                    "profile": vector("3", "5"),
                },
                "certificate": "VALID",
                "requiredResidualCount": 3,
            },
            {
                "id": "incompatible-lifts",
                "intrinsic": {
                    "horizontal": vector("2", "3"),
                    "vertical": vector("4", "5"),
                },
                "certificate": "INCONSISTENT_PROJECTIONS",
            },
        ],
        "nearRankCases": [
            {
                "id": "below-relative-threshold",
                "parameterT": str(below_parameter),
                "frame": below,
                "smallestSingularValue": str(
                    smallest_singular_value(below_parameter)),
                "expectedRank": 2,
                "expectedCertificate": "UNDERDETERMINED",
            },
            {
                "id": "above-relative-threshold",
                "parameterT": str(above_parameter),
                "frame": above,
                "smallestSingularValue": str(
                    smallest_singular_value(above_parameter)),
                "expectedRank": 3,
                "expectedCertificate": "VALID",
            },
        ],
        "gaugeCase": {
            "sourcePoint": vector("2", "3", "5"),
            "expectedSpatialPointAfterGauge": vector("2", "3", "5"),
            "expectedCertificate": "VALID",
            "intrinsicCoordinatesUnchanged": True,
        },
        "limitations": [
            "The rank-side expectations consume the separately versioned numeric policy.",
            "The reference does not implement a general primitive or product evaluator.",
            "Binary64 results remain numerical even when analytic inputs are exact.",
        ],
    }


def canonical_bytes(payload: dict[str, object]) -> bytes:
    text = json.dumps(payload, indent=2, sort_keys=True, ensure_ascii=False)
    return (text + "\n").encode("utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true",
                        help="fail instead of rewriting when output differs")
    parser.add_argument("--output", type=Path,
                        default=Path(__file__).with_name(
                            "point-reference-values.json"))
    args = parser.parse_args()
    expected = canonical_bytes(build_reference())
    if args.check:
        if not args.output.is_file() or args.output.read_bytes() != expected:
            print(f"stale G9A2 reference: {args.output}", file=sys.stderr)
            return 1
        print(f"G9A2 analytic reference is current: {args.output}")
        return 0
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(expected)
    print(f"wrote {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
