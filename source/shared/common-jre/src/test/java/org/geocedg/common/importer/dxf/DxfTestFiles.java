/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic, hand-written DXF test files (no external tool produced them). The
 * committed fixtures under {@code src/test/resources/org/geocedg/common/importer/dxf/} are
 * exactly the output of {@link #fixture(String)}; a test checks it byte for byte.
 */
final class DxfTestFiles {

	/** The written weight of the circle fixtures (matrix section 2.1). */
	static final String W = "0.7071067811865476";

	private DxfTestFiles() {
	}

	/** Builder of one ASCII DXF file with LF line ends. */
	static final class Dxf {
		private final StringBuilder text = new StringBuilder();
		private boolean entities;

		Dxf pair(int code, String value) {
			text.append(code).append('\n').append(value).append('\n');
			return this;
		}

		Dxf header(String version, Integer insUnits) {
			pair(0, "SECTION").pair(2, "HEADER");
			if (version != null) {
				pair(9, "$ACADVER").pair(1, version);
			}
			if (insUnits != null) {
				pair(9, "$INSUNITS").pair(70, Integer.toString(insUnits));
			}
			return pair(0, "ENDSEC");
		}

		Dxf entities() {
			entities = true;
			return pair(0, "SECTION").pair(2, "ENTITIES");
		}

		Dxf raw(String lines) {
			text.append(lines);
			return this;
		}

		String end() {
			if (entities) {
				pair(0, "ENDSEC");
			}
			return pair(0, "EOF").text.toString();
		}

		byte[] bytes() {
			return end().getBytes(StandardCharsets.ISO_8859_1);
		}
	}

	/** One SPLINE entity description. */
	static final class Spline {
		String handle = "2A";
		String layer = "REF";
		int flags = 8;
		int degree;
		String[] knots;
		String[] xs;
		String[] ys;
		String[] zs;
		String[] weights;
		Integer knotCount;
		Integer pointCount;
		List<String> extra = new ArrayList<>();

		static Spline of(int degree, String[] knots, String[] xs, String[] ys) {
			Spline spline = new Spline();
			spline.degree = degree;
			spline.knots = knots;
			spline.xs = xs;
			spline.ys = ys;
			return spline;
		}

		Spline flags(int value) {
			flags = value;
			return this;
		}

		Spline weights(String... values) {
			weights = values;
			return this;
		}

		Spline handle(String value) {
			handle = value;
			return this;
		}

		Spline extra(int code, String value) {
			extra.add(code + "\n" + value + "\n");
			return this;
		}

		void writeTo(Dxf dxf) {
			dxf.pair(0, "SPLINE").pair(5, handle).pair(330, "1F").pair(100, "AcDbEntity")
					.pair(8, layer).pair(100, "AcDbSpline");
			for (String line : extra) {
				dxf.raw(line);
			}
			dxf.pair(70, Integer.toString(flags)).pair(71, Integer.toString(degree))
					.pair(72, Integer.toString(knotCount == null ? knots.length : knotCount))
					.pair(73, Integer.toString(pointCount == null ? xs.length : pointCount))
					.pair(74, "0");
			for (String knot : knots) {
				dxf.pair(40, knot);
			}
			if (weights != null) {
				for (String weight : weights) {
					dxf.pair(41, weight);
				}
			}
			for (int i = 0; i < xs.length; i++) {
				dxf.pair(10, xs[i]);
				if (i < ys.length) {
					dxf.pair(20, ys[i]);
				}
				dxf.pair(30, zs == null ? "0.0" : zs[i]);
			}
		}
	}

	static String[] values(double... values) {
		String[] result = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = Double.toString(values[i]);
		}
		return result;
	}

	static Spline quarter() {
		return Spline.of(2, values(0, 0, 0, 1, 1, 1), values(1, 1, 0), values(0, 1, 1))
				.flags(12).weights("1.0", W, "1.0").handle("Q1");
	}

	static Spline semicircle() {
		return Spline.of(2, values(0, 0, 0, 1, 1, 2, 2, 2), values(1, 1, 0, -1, -1),
				values(0, 1, 1, 1, 0)).flags(12).weights("1.0", W, "1.0", W, "1.0")
				.handle("H1");
	}

	static Spline cubic() {
		return Spline.of(3, values(0, 0, 0, 0, 0.5, 1, 1, 1, 1), values(0, 1, 2, 3, 4),
				values(0, 2, -1, 2, 0)).handle("C1");
	}

	static byte[] file(Integer insUnits, Spline... splines) {
		Dxf dxf = new Dxf().header("AC1015", insUnits).entities();
		for (Spline spline : splines) {
			spline.writeTo(dxf);
		}
		return dxf.bytes();
	}

	/** The committed fixtures, by file name. */
	static byte[] fixture(String name) {
		switch (name) {
		case "dx1-quarter-circle-Q.dxf":
			return file(4, quarter());
		case "dx1-semicircle-H.dxf":
			return file(4, semicircle());
		case "dx1-polynomial-cubic.dxf":
			return file(0, cubic());
		case "dx13-weight-representations.dxf":
			return file(null, weightRepresentations());
		case "smoke-mixed.dxf":
			return smokeMixed();
		default:
			throw new IllegalArgumentException(name);
		}
	}

	static final String[] FIXTURES = {"dx1-quarter-circle-Q.dxf", "dx1-semicircle-H.dxf",
		"dx1-polynomial-cubic.dxf", "dx13-weight-representations.dxf", "smoke-mixed.dxf"};

	/** DX-13: the six admitted weight representations, in the matrix order. */
	static Spline[] weightRepresentations() {
		String[] knots = values(0, 0, 0, 1, 1, 1);
		String[] xs = values(1, 1, 0);
		return new Spline[] {
			Spline.of(2, knots, xs, values(0, 1, 1)).flags(8).handle("W1"),
			Spline.of(2, knots, xs, values(0, 1, 2)).flags(12).handle("W2"),
			Spline.of(2, knots, xs, values(0, 1, 3)).flags(12).weights("1", "2", "1")
					.handle("W3"),
			Spline.of(2, knots, xs, values(0, 1, 4)).flags(12).weights("3", "3", "3")
					.handle("W4"),
			Spline.of(2, knots, xs, values(0, 1, 5)).flags(12).weights("1", "1", "1")
					.handle("W5"),
			Spline.of(2, knots, xs, values(0, 1, 6)).flags(8).weights("1.0", "1.0", "1.0")
					.handle("W6")};
	}

	/** Smoke file: admitted, rejected and ignored entities side by side. */
	static byte[] smokeMixed() {
		Dxf dxf = new Dxf().header("AC1015", 4).entities();
		dxf.pair(0, "LINE").pair(5, "L1").pair(8, "0").pair(10, "0").pair(20, "0").pair(30, "0")
				.pair(11, "4").pair(21, "0").pair(31, "0");
		quarter().writeTo(dxf);
		cubic().writeTo(dxf);
		Spline.of(2, values(0, 0, 0, 1, 1, 1), values(1, 1, 0), values(0, 1, 1)).flags(8)
				.weights("1", "2", "1").handle("BAD1").writeTo(dxf);
		Spline closed = Spline.of(3, values(0, 0, 0, 0, 1, 2, 2, 2, 2),
				values(5, 7, 7, 5, 5), values(0, 0, 2, 2, 0)).flags(8).handle("CL1");
		closed.writeTo(dxf);
		return dxf.bytes();
	}

	static byte[] resource(String name) throws IOException {
		try (InputStream input = DxfTestFiles.class.getResourceAsStream(name)) {
			if (input == null) {
				throw new IOException("missing fixture " + name);
			}
			return input.readAllBytes();
		}
	}

	static String text(double value) {
		return String.format(Locale.ROOT, "%s", value);
	}
}
