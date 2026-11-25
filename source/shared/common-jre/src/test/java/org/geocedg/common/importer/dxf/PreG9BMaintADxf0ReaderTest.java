/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.geocedg.common.importer.dxf.DxfTestFiles.Dxf;
import org.geocedg.common.importer.dxf.DxfTestFiles.Spline;
import org.geocedg.common.math.bspline.BSplineCanonicalEncoding;
import org.geocedg.common.math.bspline.BSplineWeightMode;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A-DXF0 reader: DX-2 (per-entity rejections and their order), DX-3
 * (excluded variants), DX-4 (closure flag), DX-10 (decimal reading), DX-11 (file limits and
 * binary DXF) and DX-13 (admitted weight representations) of the A-N1 matrix revision 2.
 */
final class PreG9BMaintADxf0ReaderTest {

	private static DxfImportReport read(byte[] bytes) {
		return DxfSplineReader.read(bytes, "C:\\drawings\\test.dxf", DxfAffineTransform.IDENTITY,
				"mm", DxfImportLimits.DEFAULT);
	}

	private static DxfCause single(Spline spline) {
		DxfImportReport report = read(DxfTestFiles.file(4, spline));
		assertNull(report.fileCause());
		assertEquals(0, report.records().size(), "no geometry for a rejected entity");
		assertEquals(1, report.rejected().size());
		return report.rejected().get(0).cause();
	}

	private static Spline quarterWith(String... weights) {
		return DxfTestFiles.quarter().weights(weights);
	}

	@Test
	void dx2RejectionsFollowThePredicateOrderAndTheRestIsImported() {
		assertEquals(DxfCause.NON_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG,
				single(quarterWith("1", "2", "1").flags(8)));
		assertEquals(DxfCause.WEIGHT_COUNT_MISMATCH, single(quarterWith("1", "0.5")));
		assertEquals(DxfCause.WEIGHT_COUNT_MISMATCH, single(quarterWith("1", "0.5", "1", "1")));
		assertEquals(DxfCause.NONPOSITIVE_WEIGHT, single(quarterWith("1", "0", "1")));
		assertEquals(DxfCause.NONPOSITIVE_WEIGHT, single(quarterWith("1", "-0.5", "1")));
		assertEquals(DxfCause.NONFINITE_VALUE, single(quarterWith("1", "1e999", "1")));
		assertEquals(DxfCause.MALFORMED_REAL, single(quarterWith("1", "NaN", "1")));
		assertEquals(DxfCause.WEIGHT_RATIO_OUT_OF_RANGE,
				single(quarterWith("1", "1000000.5", "1")));
		assertEquals(0, read(DxfTestFiles.file(4, quarterWith("1", "1000000", "1")))
				.rejected().size(), "ratio exactly W_RATIO_MAX is admitted");
		Spline counts = DxfTestFiles.cubic();
		counts.pointCount = 6;
		assertEquals(DxfCause.CONTROL_POINT_COUNT_MISMATCH, single(counts));
		Spline knots = DxfTestFiles.cubic();
		knots.knotCount = 8;
		assertEquals(DxfCause.KNOT_COUNT_MISMATCH, single(knots));
		Spline decreasing = Spline.of(3, DxfTestFiles.values(0, 0, 0, 0, 0.7, 0.5, 1, 1, 1),
				DxfTestFiles.values(0, 1, 2, 3, 4), DxfTestFiles.values(0, 1, 0, 1, 0));
		assertEquals(DxfCause.KNOTS_DECREASING, single(decreasing));
		// Order: non-unit without flag (11) before nonpositive (12).
		assertEquals(DxfCause.NON_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG,
				single(quarterWith("1", "-1", "2").flags(8)));
		// Order: weight count (10) before flag (11).
		assertEquals(DxfCause.WEIGHT_COUNT_MISMATCH, single(quarterWith("1", "2").flags(8)));
		// Partially valid file: the good entity is imported, the bad one reported.
		DxfImportReport mixed = read(DxfTestFiles.file(4, DxfTestFiles.cubic(),
				quarterWith("1", "0.5").handle("BAD")));
		assertEquals(1, mixed.records().size());
		assertEquals("C1", mixed.records().get(0).handle());
		assertEquals(1, mixed.rejected().size());
		assertEquals("BAD", mixed.rejected().get(0).handle());
		assertEquals(2, mixed.rejected().get(0).ordinal());
		assertEquals(DxfCause.WEIGHT_COUNT_MISMATCH, mixed.rejected().get(0).cause());
	}

	@Test
	void dx3ExcludedVariantsAreNotImported() {
		assertEquals(DxfCause.PERIODIC_UNSUPPORTED, single(DxfTestFiles.cubic().flags(10)));
		Spline fitOnly = DxfTestFiles.cubic();
		fitOnly.pointCount = 0;
		fitOnly.xs = new String[0];
		fitOnly.ys = new String[0];
		fitOnly.extra(11, "0").extra(21, "0").extra(31, "0");
		assertEquals(DxfCause.FIT_POINTS_ONLY_UNSUPPORTED, single(fitOnly));
		Spline unclamped = Spline.of(3, DxfTestFiles.values(0, 1, 2, 3, 4, 5, 6, 7, 8),
				DxfTestFiles.values(0, 1, 2, 3, 4), DxfTestFiles.values(0, 1, 0, 1, 0));
		assertEquals(DxfCause.NOT_CLAMPED_UNSUPPORTED, single(unclamped));
		Spline spatial = DxfTestFiles.cubic();
		spatial.zs = DxfTestFiles.values(0, 0, 1, 0, 0);
		assertEquals(DxfCause.NONPLANAR_UNSUPPORTED, single(spatial));
		Spline paper = DxfTestFiles.cubic().extra(67, "1");
		assertEquals(DxfCause.PAPER_SPACE_UNSUPPORTED, single(paper));
		assertEquals(DxfCause.DEGREE_OUT_OF_RANGE, single(Spline.of(8,
				DxfTestFiles.values(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1),
				DxfTestFiles.values(0, 1, 2, 3, 4, 5, 6, 7, 8),
				DxfTestFiles.values(0, 1, 0, 1, 0, 1, 0, 1, 0))));
		// Block content and INSERT: the spline in BLOCKS is reported, INSERT never expanded.
		Dxf dxf = new Dxf().header("AC1015", 4);
		dxf.pair(0, "SECTION").pair(2, "BLOCKS").pair(0, "BLOCK").pair(2, "B1");
		DxfTestFiles.cubic().handle("INBLOCK").writeTo(dxf);
		dxf.pair(0, "ENDBLK").pair(0, "ENDSEC").entities();
		dxf.pair(0, "INSERT").pair(2, "B1").pair(10, "0").pair(20, "0").pair(30, "0");
		DxfImportReport blocks = read(dxf.bytes());
		assertEquals(0, blocks.records().size());
		assertEquals(1, blocks.rejected().size());
		assertEquals("BLOCKS", blocks.rejected().get(0).section());
		assertEquals("INBLOCK", blocks.rejected().get(0).handle());
		assertEquals(DxfCause.BLOCK_CONTENT_UNSUPPORTED, blocks.rejected().get(0).cause());
		assertEquals(Integer.valueOf(1), blocks.entityCounts().get("INSERT"));
		assertTrue(blocks.diagnostics().contains(DxfDiagnostic.INSERT_NOT_EXPANDED));
	}

	@Test
	void dx4ClosureIsDecidedByTheDataAlone() {
		Spline closedNoFlag = Spline.of(3, DxfTestFiles.values(0, 0, 0, 0, 1, 2, 2, 2, 2),
				DxfTestFiles.values(5, 7, 7, 5, 5), DxfTestFiles.values(0, 0, 2, 2, 0)).flags(8);
		DxfSplineReference closed = read(DxfTestFiles.file(4, closedNoFlag)).records().get(0);
		assertTrue(closed.isExactlyClosed());
		assertTrue(closed.diagnostics().contains(DxfDiagnostic.CLOSED_FLAG_CONTRADICTED));
		Spline nearWithFlag = Spline.of(3, DxfTestFiles.values(0, 0, 0, 0, 1, 2, 2, 2, 2),
				new String[] {"5.0", "7.0", "7.0", "5.0", "5.000000000000001"},
				DxfTestFiles.values(0, 0, 2, 2, 0)).flags(9)
				.extra(42, "0.001").extra(43, "0.001");
		DxfSplineReference near = read(DxfTestFiles.file(4, nearWithFlag)).records().get(0);
		assertFalse(near.isExactlyClosed(), "no closure by proximity or by codes 42-44");
		assertTrue(near.diagnostics().contains(DxfDiagnostic.CLOSED_FLAG_CONTRADICTED));
		assertTrue(near.diagnostics().contains(DxfDiagnostic.TOLERANCES_IGNORED));
		Spline closedWithFlag = Spline.of(3, DxfTestFiles.values(0, 0, 0, 0, 1, 2, 2, 2, 2),
				DxfTestFiles.values(5, 7, 7, 5, 5), DxfTestFiles.values(0, 0, 2, 2, 0)).flags(9);
		assertFalse(read(DxfTestFiles.file(4, closedWithFlag)).records().get(0).diagnostics()
				.contains(DxfDiagnostic.CLOSED_FLAG_CONTRADICTED));
	}

	@Test
	void dx10DecimalReading() {
		for (String bad : new String[] {"0,5", "NaN", "Infinity", "-Infinity", "0x1p-1", "1e",
			"abc", "1.0.0", "", "--1"}) {
			assertEquals(DxfCause.MALFORMED_REAL, single(quarterWith("1", bad, "1")), bad);
		}
		assertEquals(DxfCause.NONFINITE_VALUE, single(quarterWith("1", "1e309", "1")));
		assertEquals(DxfCause.NONFINITE_VALUE, single(quarterWith("1", "-1.8e308", "1")));
		assertEquals(DxfCause.MALFORMED_REAL, single(DxfTestFiles.cubic().flags(8)
				.extra(71, "3.0")));
		for (String good : new String[] {"0.1", "+.5", "5.", "  2.5e-3 ", "1E+2", "-0.0",
			"4.9e-324", "2.2250738585072011e-308"}) {
			assertEquals(Double.parseDouble(good.trim()), DxfSplineReader.parseReal(good.trim()));
		}
		// Correct rounding (ties to even) of representative tokens.
		assertEquals(0x3FE6A09E667F3BCDL,
				Double.doubleToRawLongBits(DxfSplineReader.parseReal(DxfTestFiles.W)));
		assertEquals(0x3FB999999999999AL,
				Double.doubleToRawLongBits(DxfSplineReader.parseReal("0.1")));
		// Just below the midpoint of 1 and nextUp(1): rounds down.
		assertEquals(1.0, DxfSplineReader.parseReal("1.00000000000000011102230246251565404"));
	}

	@Test
	void dx11FileLimitsAndBinaryDxf() {
		byte[] binary = ("AutoCAD Binary DXF\r\n" + (char) 26 + (char) 0 + "junk")
				.getBytes(StandardCharsets.ISO_8859_1);
		assertEquals(DxfCause.BINARY_DXF_UNSUPPORTED, read(binary).fileCause());
		byte[] valid = DxfTestFiles.file(4, DxfTestFiles.cubic());
		DxfImportReport small = DxfSplineReader.read(valid, "a.dxf", DxfAffineTransform.IDENTITY,
				"mm", DxfImportLimits.reduced(valid.length - 1, 10, 1000));
		assertEquals(DxfCause.FILE_LIMIT_EXCEEDED, small.fileCause());
		assertTrue(small.records().isEmpty());
		assertEquals(DxfCause.FILE_LIMIT_EXCEEDED,
				DxfSplineReader.oversized("big.dxf", DxfAffineTransform.IDENTITY, "mm")
						.fileCause());
		byte[] atLimit = new byte[(int) DxfImportLimits.FILE_BYTES_MAX + 1];
		assertEquals(DxfCause.FILE_LIMIT_EXCEEDED, read(atLimit).fileCause());
		Spline[] many = new Spline[DxfImportLimits.SPLINE_ENTITIES_MAX + 1];
		for (int i = 0; i < many.length; i++) {
			many[i] = DxfTestFiles.cubic().handle(Integer.toHexString(i + 16));
		}
		DxfImportReport tooMany = read(DxfTestFiles.file(4, many));
		assertEquals(DxfCause.FILE_LIMIT_EXCEEDED, tooMany.fileCause());
		assertTrue(tooMany.records().isEmpty(), "no prefix is imported");
		DxfImportReport exact = read(DxfTestFiles.file(4, java.util.Arrays.copyOf(many,
				DxfImportLimits.SPLINE_ENTITIES_MAX)));
		assertNull(exact.fileCause());
		assertEquals(DxfImportLimits.SPLINE_ENTITIES_MAX, exact.records().size());
		// Structure.
		assertEquals(DxfCause.MALFORMED_DXF, read("0\nSECTION\n2\n".getBytes(
				StandardCharsets.US_ASCII)).fileCause());
		assertEquals(DxfCause.MALFORMED_DXF, read("X\nSECTION\n".getBytes(
				StandardCharsets.US_ASCII)).fileCause());
		assertEquals(DxfCause.MALFORMED_DXF, read("0\nSECTION\n2\nENTITIES\n0\nEOF\n"
				.getBytes(StandardCharsets.US_ASCII)).fileCause());
		assertEquals(DxfCause.MALFORMED_DXF, read(new Dxf().header("AC1015", 4).bytes())
				.fileCause(), "no ENTITIES section");
	}

	@Test
	void dx13AdmittedWeightRepresentations() {
		DxfImportReport report = read(DxfTestFiles.fixture("dx13-weight-representations.dxf"));
		assertEquals(0, report.rejected().size());
		List<DxfSplineReference> records = report.records();
		assertEquals(6, records.size());
		DxfNature[] natures = {DxfNature.POLYNOMIAL, DxfNature.POLYNOMIAL, DxfNature.RATIONAL,
			DxfNature.RATIONAL_EQUAL_WEIGHTS, DxfNature.POLYNOMIAL, DxfNature.POLYNOMIAL};
		boolean[] flags = {false, true, true, true, true, false};
		BSplineWeightMode[] provenance = {BSplineWeightMode.IMPLICIT_UNIT,
			BSplineWeightMode.IMPLICIT_UNIT, BSplineWeightMode.EXPLICIT,
			BSplineWeightMode.EXPLICIT, BSplineWeightMode.EXPLICIT, BSplineWeightMode.EXPLICIT};
		for (int i = 0; i < 6; i++) {
			DxfSplineReference record = records.get(i);
			assertEquals(natures[i], record.nature(), "W" + (i + 1));
			assertEquals(flags[i], record.rationalFlagSet(), "W" + (i + 1));
			assertEquals(provenance[i], record.weightProvenance(), "W" + (i + 1));
			if (provenance[i] == BSplineWeightMode.IMPLICIT_UNIT) {
				assertNull(record.sourceData().explicitWeights(),
						"implicit weights are never recorded as code-41 values");
				byte[] encoding = BSplineCanonicalEncoding.encode(DxfSplineReference.CONTRACT,
						record.sourceData());
				assertEquals('I', encoding[encoding.length - 1], "absence marker");
			}
			assertArrayEquals(i == 2 ? new double[] {1, 2, 1} : i == 3
					? new double[] {3, 3, 3} : new double[] {1, 1, 1}, record.effectiveWeights());
		}
		assertEquals(Set.of(), records.get(0).diagnostics());
		assertTrue(records.get(1).diagnostics()
				.contains(DxfDiagnostic.RATIONAL_FLAG_WITH_IMPLICIT_UNIT_WEIGHTS));
		assertFalse(records.get(4).diagnostics()
				.contains(DxfDiagnostic.EXPLICIT_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG));
		assertTrue(records.get(5).diagnostics()
				.contains(DxfDiagnostic.EXPLICIT_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG));
		for (int i = 0; i < 6; i++) {
			assertEquals(i == 1 || i == 5 ? 1 : 0, records.get(i).diagnostics().size(),
					"W" + (i + 1) + " " + records.get(i).diagnostics());
		}
	}

	@Test
	void groupCodesAndStringsAreProvenanceOnly() {
		Spline spline = DxfTestFiles.cubic().extra(62, "1").extra(370, "25").extra(1001, "APP")
				.extra(1000, "x").extra(77, "3").extra(210, "0").extra(220, "0").extra(230, "1")
				.extra(12, "1").extra(22, "0").extra(32, "0");
		spline.layer = "Capa\u00f1";
		DxfSplineReference record = read(DxfTestFiles.file(4, spline)).records().get(0);
		assertEquals(List.of(77), record.unrecognizedCodes());
		assertTrue(record.diagnostics().contains(DxfDiagnostic.UNRECOGNIZED_GROUP_CODES));
		assertTrue(record.diagnostics().contains(DxfDiagnostic.NORMAL_RECORDED));
		assertTrue(record.diagnostics().contains(DxfDiagnostic.TANGENTS_IGNORED));
		assertArrayEquals(new double[] {0, 0, 1}, record.normal());
		assertEquals("Capa\u00f1", record.layer(), "ISO-8859-1 before AC1021");
		byte[] utf8 = "\u00f1".getBytes(StandardCharsets.UTF_8);
		assertEquals("\u00f1", DxfSplineReader.decode(utf8, true));
		assertEquals("A\\xff", DxfSplineReader.decode(new byte[] {'A', (byte) 0xFF}, true));
		// Same geometry whatever the style codes.
		DxfSplineReference plain = read(DxfTestFiles.file(4, DxfTestFiles.cubic())).records()
				.get(0);
		assertEquals(plain.dataDigest(), record.dataDigest());
	}
}
