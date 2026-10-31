"""CU7-I1-M08-S-R2: isolated rational mathematics, never product admission.

Run from the repository root with Python 3 (standard library only).
No GeoCeDG/GeoGebra runtime, files, source mutation, discovery or certificates.
The ellipse family is conditional: it is NOT a capture of the native h.
"""

from fractions import Fraction as F
import json


def binary64(text):
    return F.from_float(float(text))


def interval_square_max(lo, hi):
    return max(lo * lo, hi * hi)


def main():
    # Immutable R1 free inputs; exact binary64 values, not decimal idealization.
    cx = binary64("0.4965866445356883")
    cy = binary64("1.7854818601338847")
    jx = binary64("-0.5082406249102692")
    jy = binary64("2.5763181370126467")
    qx = binary64("0.8976484384297626")
    qy = binary64("1.7854818601338847")
    rho = binary64("0.1")
    rlo, rhi = F(127, 100), F(129, 100)
    radius_squared = (jx - cx) ** 2 + (jy - cy) ** 2
    assert rlo ** 2 < radius_squared < rhi ** 2

    # Family F_r(t)=(cx/2+r*cos(t)/2, cy+r*sin(t)), r in [rlo,rhi].
    # For |t|<=delta: |sin(t)|<=delta, 1-delta^2/2<=cos(t)<=1.
    # These are real inequalities; no floating trigonometry is a proof oracle.
    delta = F(1, 1024)
    cos_lower = 1 - delta ** 2 / 2
    xlo, xhi = cx / 2 + rlo * cos_lower / 2, cx / 2 + rhi / 2
    ylo, yhi = cy - rhi * delta, cy + rhi * delta
    distance_squared_upper = interval_square_max(xlo - qx, xhi - qx)
    distance_squared_upper += interval_square_max(ylo - qy, yhi - qy)
    monotone_derivative_lower = rlo * cos_lower
    assert monotone_derivative_lower > 0
    assert distance_squared_upper < rho ** 2

    # A nearby valid address need not minimize distance.
    # F(t)=(t,0), q=(0,1/100), t0=1/50; d'(t0)=2*t0 != 0.
    t0, q_line = F(1, 50), F(1, 100)
    assert t0 ** 2 + q_line ** 2 < F(1, 10) ** 2
    assert 2 * t0 != 0

    # Finite regular samples do not prove a minimum on a whole cell.
    # H(t)=(t,1-16*t^2*(t^2-1)^2*(t^2-1/4)^2), q=(0,0).
    # H'_x=1 everywhere. All four guard samples are farther than H(0).
    def curve(t):
        return t, 1 - 16 * t ** 2 * (t ** 2 - 1) ** 2 * (t ** 2 - F(1, 4)) ** 2

    guards = []
    for t in [F(-1), F(-1, 2), F(1, 2), F(1)]:
        x, y = curve(t)
        squared = x * x + y * y
        assert squared > 1
        guards.append({"t": str(t), "distance_squared": str(squared)})
    # y''(0)=-16/8=-2; (||H||^2)''(0)=2+2*y''(0)=-2.
    second_derivative_at_zero = F(2) + 2 * F(-16, 8)
    assert second_derivative_at_zero == -2
    x, y = curve(F(1, 64))
    assert x * x + y * y < 1

    result = {
        "evidence_kind": "ISOLATED_RATIONAL_MATHEMATICS_NOT_KERNEL",
        "native_h_admission": "NOT_ESTABLISHED",
        "ellipse_family": {
            "radius_bracket": [str(rlo), str(rhi)],
            "cell": [str(-delta), str(delta)],
            "distance_squared_upper_exact": str(distance_squared_upper),
            "radius_squared_exact": str(rho ** 2),
            "monotone_derivative_lower_exact": str(monotone_derivative_lower),
            "distance_margin_positive": True,
            "regular_injective_chart": True,
            "minimum_claim": "NONE",
        },
        "nearby_nonminimum": {"t": str(t0), "objective_derivative": str(2 * t0)},
        "finite_guard_counterexample": {
            "guards": guards,
            "objective_second_derivative_at_zero": str(second_derivative_at_zero),
            "actual_r6_query_executed": False,
        },
        "assertion_groups_completed": 3,
    }
    print(json.dumps(result, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()
