#!/usr/bin/env python3
"""Build or check the deterministic PRE-G9B-S3 author-smoke GGB."""

from __future__ import annotations

import argparse
from io import BytesIO
from pathlib import Path
import sys
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo


ZIP_EPOCH = (1980, 1, 1, 0, 0, 0)


def build(xml: bytes) -> bytes:
    output = BytesIO()
    with ZipFile(output, "w", compression=ZIP_DEFLATED, compresslevel=9) as ggb:
        entry = ZipInfo("geogebra.xml", date_time=ZIP_EPOCH)
        entry.compress_type = ZIP_DEFLATED
        entry.create_system = 0
        entry.external_attr = 0
        entry.flag_bits = 0
        ggb.writestr(entry, xml, compress_type=ZIP_DEFLATED, compresslevel=9)
    return output.getvalue()


def main() -> int:
    root = Path(__file__).resolve().parent
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, default=root / "geogebra.xml")
    parser.add_argument(
        "--output", type=Path,
        default=root / "pre-g9b-s3-text-view-scaling.ggb")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if not args.input.is_file():
        print(f"missing canonical model source: {args.input}", file=sys.stderr)
        return 1
    expected = build(args.input.read_bytes())
    if args.check:
        if not args.output.is_file() or args.output.read_bytes() != expected:
            print(f"stale PRE-G9B-S3 smoke model: {args.output}", file=sys.stderr)
            return 1
        print(f"PRE-G9B-S3 smoke model is current: {args.output}")
        return 0
    args.output.write_bytes(expected)
    print(f"wrote {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
