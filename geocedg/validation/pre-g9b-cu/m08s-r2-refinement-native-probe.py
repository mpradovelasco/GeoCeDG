"""Read-only native inspection and rational model facts; no product admission.

The source archive is immutable. No Java loader/capture is run. XML command
references are inspected as stored syntax, never used to associate identities.
The mathematical model is conditional, not a certificate of a live evaluator.
"""

import argparse
from fractions import Fraction as F
import hashlib
import json
from pathlib import Path
import xml.etree.ElementTree as ET
from zipfile import ZipFile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--native", default="C:/Temp/TestM08-S.cedg")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    data = Path(args.native).read_bytes()
    digest = hashlib.sha256(data).hexdigest()
    assert digest == "4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d"
    with ZipFile(args.native) as archive:
        xml_bytes = archive.read("geogebra.xml")
        root = ET.fromstring(xml_bytes)
    construction = root.find("construction")
    elements = {element.get("label"): element for element in construction.findall("element")}
    commands = []
    for command in construction.findall("command"):
        inputs = dict(command.find("input").attrib)
        outputs = dict(command.find("output").attrib)
        if outputs.get("a0") in {"f", "L", "g", "K", "M", "h"}:
            commands.append({"name": command.get("name"), "inputs": inputs, "outputs": outputs})
    assert [command["name"] for command in commands] == [
        "Circle", "Point", "Line", "Intersect", "Midpoint", "LocusV2"
    ]
    assert elements["h"].get("type") == "locusv2"
    assert elements["h"].get("geocedgId") == "geo:aa7268331ad4d3a3d1cc946e989b109b"
    assert "Q" not in elements and "ReferenceR1" not in elements
    policies = list(root.iter("authorityPolicy"))
    assert len(policies) == 0

    def point(label):
        coords = elements[label].find("coords").attrib
        assert coords["z"] == "1"
        return F.from_float(float(coords["x"])), F.from_float(float(coords["y"]))

    cx, cy = point("I")
    jx, jy = point("J")
    rlo, rhi = F(127, 100), F(129, 100)
    assert rlo ** 2 < (jx - cx) ** 2 + (jy - cy) ** 2 < rhi ** 2
    # q is the explicit R1 fixture, absent from the source archive.
    qx = F.from_float(float("0.8976484384297626"))
    qy = F.from_float(float("1.7854818601338847"))
    radius = F.from_float(0.1)
    # Family E_r(t)=(cx/2+r*cos(t)/2, cy+r*sin(t)), exact binary64 constants.
    # At t=0: no transcendental arithmetic or neighborhood bound is required.
    xlo, xhi = cx / 2 + rlo / 2, cx / 2 + rhi / 2
    d2_upper = max((xlo - qx) ** 2, (xhi - qx) ** 2) + (cy - qy) ** 2
    assert d2_upper < radius ** 2
    # E'_y(0)=r>0; E is smooth. This proves existence of a local injective
    # chart, without computing its width or minimizing squared distance.
    assert rlo > 0
    # Preserve the exact binary64 address found by R1; never snap it to zero.
    seed = F.from_float(float("-6.407985253531479e-11"))
    seed_cos_lower = 1 - seed ** 2 / 2
    seed_xlo = cx / 2 + rlo * seed_cos_lower / 2
    seed_xhi = cx / 2 + rhi / 2
    seed_d2_upper = max((seed_xlo - qx) ** 2, (seed_xhi - qx) ** 2)
    seed_d2_upper += (abs(cy - qy) + rhi * abs(seed)) ** 2
    assert seed_d2_upper < radius ** 2
    assert rlo * seed_cos_lower > 0
    # Even a wide interval enclosure over [-1/2,1/2] cannot lie in that ball:
    # sin(1/2)>=1/2-(1/2)^3/6, so an actual endpoint is outside it.
    far_y_lower = rlo * (F(1, 2) - F(1, 2) ** 3 / 6)
    assert far_y_lower > radius

    result = {
        "evidence_kind": "NATIVE_XML_INSPECTION_AND_CONDITIONAL_RATIONAL_MODEL",
        "archive_sha256": digest,
        "xml_sha256": hashlib.sha256(xml_bytes).hexdigest(),
        "archive_bytes": len(data),
        "commands_inspected": commands,
        "locus_durable_id_read_from_xml": elements["h"].get("geocedgId"),
        "q_present": False,
        "reference_r1_present": False,
        "authority_record_present": False,
        "stored_authority_under_existing_reader_contract": "legacy-representation-authority/v1",
        "conic_eigenvectors_as_stored": dict(elements["f"].find("eigenvectors").attrib),
        "conditional_model": {
            "parameter": "0",
            "distance_squared_upper_exact": str(d2_upper),
            "radius_squared_exact": str(radius ** 2),
            "derivative_y_lower_exact": str(rlo),
            "r1_seed_binary64_as_rational": str(seed),
            "r1_seed_distance_squared_upper_exact": str(seed_d2_upper),
            "wide_cell_endpoint_y_distance_lower": str(far_y_lower),
            "minimum_claim": "NONE",
        },
        "live_program_capture": "NOT_EXECUTED",
        "new_product_admission": "NOT_IMPLEMENTED",
    }
    assert hashlib.sha256(Path(args.native).read_bytes()).hexdigest() == digest
    print(json.dumps(result, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()
