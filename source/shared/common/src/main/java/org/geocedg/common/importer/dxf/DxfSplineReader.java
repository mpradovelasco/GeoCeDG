/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineDataCause;
import org.geocedg.common.math.bspline.BSplineLimits;
import org.geocedg.common.math.bspline.BSplineValidation;

/**
 * ASCII DXF reader of {@code dxf-spline-reference/v1}. Stateless and deterministic: the same
 * bytes, base name, transformation and document unit give an identical report. The file is
 * only read; the reader has no access to it beyond the given bytes.
 *
 * <p>File rules (section 4.1), selection (section 4.2), group codes (section 4.3), admission
 * predicates in their order (section 4.4), decimal reading (section 4.5), weight
 * reconstruction (section 4.6), record (section 5), transformation (section 6), render
 * (section 8) and provenance (section 9) follow the contract; each entity is admitted or
 * rejected atomically with its first failing cause.</p>
 */
public final class DxfSplineReader {

	private static final byte[] BINARY_SENTINEL = "AutoCAD Binary DXF"
			.getBytes(StandardCharsets.US_ASCII);
	private static final Pattern REAL = Pattern.compile(
			"[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)?");
	private static final Pattern INTEGER = Pattern.compile("[+-]?[0-9]+");
	private static final Set<Integer> REAL_CODES = Set.of(10, 20, 30, 11, 21, 31, 12, 22, 32,
			13, 23, 33, 40, 41, 42, 43, 44, 210, 220, 230);
	private static final Set<Integer> INTEGER_CODES = Set.of(67, 70, 71, 72, 73, 74);
	private static final Set<Integer> IGNORED_CODES = Set.of(5, 8, 100, 102, 330, 360, 6, 62,
			370, 48, 60, 420, 430, 440, 390, 284, 347, 999);
	private static final BSplineLimits LIMITS = BSplineLimits.of(DxfImportLimits.P_MAX_IMPORT,
			DxfImportLimits.N_MAX_IMPORT);
	private static final String ENTITIES = "ENTITIES";
	private static final String BLOCKS = "BLOCKS";

	private DxfSplineReader() {
	}

	/**
	 * Reads one ASCII DXF file.
	 *
	 * @param bytes the complete file content
	 * @param fileName file name; only its base name is recorded
	 * @param transform explicitly chosen transformation ({@link DxfAffineTransform#IDENTITY}
	 *            by default)
	 * @param documentUnit document construction unit token, or
	 *            {@link DxfFileProvenance#UNSPECIFIED_MODEL_UNIT}
	 * @param limits import limits ({@link DxfImportLimits#DEFAULT} in the product)
	 * @return the deterministic import report
	 */
	public static DxfImportReport read(byte[] bytes, String fileName,
			DxfAffineTransform transform, String documentUnit, DxfImportLimits limits) {
		Objects.requireNonNull(bytes, "bytes");
		Objects.requireNonNull(transform, "transform");
		Objects.requireNonNull(limits, "limits");
		String baseName = baseName(fileName);
		String unit = documentUnit == null ? DxfFileProvenance.UNSPECIFIED_MODEL_UNIT
				: documentUnit;
		String digest = sha256(bytes);
		if (bytes.length > limits.fileBytesMax()) {
			return fileRejected(DxfCause.FILE_LIMIT_EXCEEDED, new DxfFileProvenance(digest,
					baseName, DxfFileProvenance.UNKNOWN_VERSION, null, unit), transform);
		}
		if (startsWith(bytes, BINARY_SENTINEL)) {
			return fileRejected(DxfCause.BINARY_DXF_UNSUPPORTED, new DxfFileProvenance(digest,
					baseName, DxfFileProvenance.UNKNOWN_VERSION, null, unit), transform);
		}
		List<Pair> pairs = pairs(bytes);
		Sections sections = pairs == null ? null : sections(pairs);
		if (sections == null || sections.entities == null) {
			return fileRejected(DxfCause.MALFORMED_DXF, new DxfFileProvenance(digest, baseName,
					DxfFileProvenance.UNKNOWN_VERSION, null, unit), transform);
		}
		String version = DxfFileProvenance.UNKNOWN_VERSION;
		Integer insUnits = null;
		List<Pair> header = sections.header;
		for (int i = 0; i + 1 < header.size(); i++) {
			if (header.get(i).code == 9 && "$ACADVER".equals(header.get(i).ascii())
					&& header.get(i + 1).code == 1) {
				version = header.get(i + 1).ascii();
			}
			if (header.get(i).code == 9 && "$INSUNITS".equals(header.get(i).ascii())
					&& header.get(i + 1).code == 70) {
				Long code = parseInteger(header.get(i + 1).ascii());
				insUnits = code == null ? null : (int) (long) code;
			}
		}
		boolean utf8 = version.length() == 6 && version.startsWith("AC")
				&& version.compareTo("AC1021") >= 0;
		DxfFileProvenance file = new DxfFileProvenance(digest, baseName, version, insUnits,
				unit);
		List<List<Pair>> entities = split(sections.entities);
		SortedMap<String, Integer> counts = new TreeMap<>();
		int splineCount = 0;
		for (List<Pair> entity : entities) {
			String type = entity.get(0).ascii();
			counts.merge(type, 1, Integer::sum);
			if ("SPLINE".equals(type)) {
				splineCount++;
			}
		}
		Set<DxfDiagnostic> diagnostics = EnumSet.noneOf(DxfDiagnostic.class);
		if (file.unitsDiffer()) {
			diagnostics.add(DxfDiagnostic.UNITS_DIFFER);
		}
		if (counts.containsKey("INSERT")) {
			diagnostics.add(DxfDiagnostic.INSERT_NOT_EXPANDED);
		}
		if (splineCount > limits.splineEntitiesMax()) {
			return new DxfImportReport(DxfCause.FILE_LIMIT_EXCEEDED, file, transform, counts,
					List.of(), List.of(), diagnostics);
		}
		List<DxfSplineReference> records = new ArrayList<>();
		List<DxfRejectedEntity> rejected = new ArrayList<>();
		for (int ordinal = 1; ordinal <= entities.size(); ordinal++) {
			List<Pair> entity = entities.get(ordinal - 1);
			if (!"SPLINE".equals(entity.get(0).ascii())) {
				continue;
			}
			Entity parsed = new Entity(entity, utf8);
			DxfCause cause = parsed.admissionCause();
			if (cause == null) {
				DxfSplineReference record = parsed.record(file, transform, ordinal, limits);
				if (record == null) {
					rejected.add(new DxfRejectedEntity(ENTITIES, ordinal, parsed.handle,
							parsed.layer, DxfCause.NONFINITE_VALUE));
				} else {
					records.add(record);
				}
			} else {
				rejected.add(new DxfRejectedEntity(ENTITIES, ordinal, parsed.handle,
						parsed.layer, cause));
			}
		}
		if (sections.blocks != null) {
			List<List<Pair>> blockEntries = split(sections.blocks);
			for (int ordinal = 1; ordinal <= blockEntries.size(); ordinal++) {
				List<Pair> entry = blockEntries.get(ordinal - 1);
				if ("SPLINE".equals(entry.get(0).ascii())) {
					Entity parsed = new Entity(entry, utf8);
					rejected.add(new DxfRejectedEntity(BLOCKS, ordinal, parsed.handle,
							parsed.layer, DxfCause.BLOCK_CONTENT_UNSUPPORTED));
				}
			}
		}
		return new DxfImportReport(null, file, transform, counts, records, rejected,
				diagnostics);
	}

	/**
	 * Report of a file whose size exceeds the limit, rejected before it is read (no digest:
	 * the bytes were not read).
	 *
	 * @param fileName file name; only its base name is recorded
	 * @param transform requested transformation
	 * @param documentUnit document unit token
	 * @return a report with {@link DxfCause#FILE_LIMIT_EXCEEDED}
	 */
	public static DxfImportReport oversized(String fileName, DxfAffineTransform transform,
			String documentUnit) {
		return fileRejected(DxfCause.FILE_LIMIT_EXCEEDED, new DxfFileProvenance("",
				baseName(fileName), DxfFileProvenance.UNKNOWN_VERSION, null,
				documentUnit == null ? DxfFileProvenance.UNSPECIFIED_MODEL_UNIT : documentUnit),
				transform);
	}

	private static DxfImportReport fileRejected(DxfCause cause, DxfFileProvenance file,
			DxfAffineTransform transform) {
		return new DxfImportReport(cause, file, transform, new TreeMap<>(), List.of(),
				List.of(), EnumSet.noneOf(DxfDiagnostic.class));
	}

	static String baseName(String fileName) {
		if (fileName == null) {
			return "";
		}
		int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		return fileName.substring(slash + 1);
	}

	private static boolean startsWith(byte[] bytes, byte[] prefix) {
		if (bytes.length < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; i++) {
			if (bytes[i] != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	static String sha256(byte[] bytes) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
			StringBuilder text = new StringBuilder(64);
			for (byte b : digest) {
				text.append(Character.forDigit((b >> 4) & 0xF, 16))
						.append(Character.forDigit(b & 0xF, 16));
			}
			return text.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}

	/** One group-code/value pair; the value keeps its raw bytes. */
	private static final class Pair {
		final int code;
		final byte[] value;

		Pair(int code, byte[] value) {
			this.code = code;
			this.value = value;
		}

		String ascii() {
			return new String(value, StandardCharsets.ISO_8859_1).trim();
		}
	}

	/** Splits the file into pairs; {@code null} for malformed structure. */
	private static @CheckForNull List<Pair> pairs(byte[] bytes) {
		List<byte[]> lines = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == '\n') {
				lines.add(line(bytes, start, i));
				start = i + 1;
			}
		}
		if (start < bytes.length) {
			lines.add(line(bytes, start, bytes.length));
		}
		if (lines.size() % 2 != 0) {
			return null;
		}
		List<Pair> pairs = new ArrayList<>(lines.size() / 2);
		for (int i = 0; i < lines.size(); i += 2) {
			Long code = parseInteger(new String(lines.get(i), StandardCharsets.ISO_8859_1)
					.trim());
			if (code == null) {
				return null;
			}
			pairs.add(new Pair((int) (long) code, lines.get(i + 1)));
		}
		return pairs;
	}

	private static byte[] line(byte[] bytes, int start, int end) {
		int stop = end > start && bytes[end - 1] == '\r' ? end - 1 : end;
		byte[] result = new byte[stop - start];
		System.arraycopy(bytes, start, result, 0, result.length);
		return result;
	}

	/** Pairs of the HEADER, BLOCKS and ENTITIES sections. */
	private static final class Sections {
		List<Pair> header = new ArrayList<>();
		List<Pair> blocks;
		List<Pair> entities;
	}

	private static @CheckForNull Sections sections(List<Pair> pairs) {
		Sections sections = new Sections();
		int i = 0;
		while (i < pairs.size()) {
			Pair pair = pairs.get(i);
			if (pair.code == 0 && "EOF".equals(pair.ascii())) {
				break;
			}
			if (pair.code != 0 || !"SECTION".equals(pair.ascii())) {
				i++;
				continue;
			}
			if (i + 1 >= pairs.size() || pairs.get(i + 1).code != 2) {
				return null;
			}
			String name = pairs.get(i + 1).ascii();
			List<Pair> body = new ArrayList<>();
			int j = i + 2;
			boolean terminated = false;
			while (j < pairs.size()) {
				Pair inner = pairs.get(j);
				if (inner.code == 0 && "ENDSEC".equals(inner.ascii())) {
					terminated = true;
					break;
				}
				if (inner.code == 0 && ("SECTION".equals(inner.ascii())
						|| "EOF".equals(inner.ascii()))) {
					break;
				}
				body.add(inner);
				j++;
			}
			if (!terminated) {
				return null;
			}
			if ("HEADER".equals(name)) {
				sections.header = body;
			} else if (BLOCKS.equals(name)) {
				sections.blocks = body;
			} else if (ENTITIES.equals(name)) {
				if (sections.entities != null) {
					return null;
				}
				sections.entities = body;
			}
			i = j + 1;
		}
		return sections;
	}

	/** Entries of a section: each starts at a code-0 pair. */
	private static List<List<Pair>> split(List<Pair> body) {
		List<List<Pair>> entries = new ArrayList<>();
		List<Pair> current = null;
		for (Pair pair : body) {
			if (pair.code == 0) {
				current = new ArrayList<>();
				entries.add(current);
			}
			if (current != null) {
				current.add(pair);
			}
		}
		return entries;
	}

	/** Section 4.5 integer of codes 70-74 and 67; {@code null} when malformed. */
	static @CheckForNull Long parseInteger(String token) {
		if (!INTEGER.matcher(token).matches() || token.length() > 11) {
			return null;
		}
		long value = Long.parseLong(token.startsWith("+") ? token.substring(1) : token);
		return value < Integer.MIN_VALUE || value > Integer.MAX_VALUE ? null : value;
	}

	/**
	 * Section 4.5 real: the trimmed token must match the grammar; it is converted to the
	 * nearest binary64 (ties to even, {@link Double#parseDouble}).
	 *
	 * @param token trimmed token
	 * @return the value (possibly infinite on overflow), or {@code null} when malformed
	 */
	static @CheckForNull Double parseReal(String token) {
		if (!REAL.matcher(token).matches()) {
			return null;
		}
		return Double.parseDouble(token);
	}

	/** Parsed SPLINE entity. */
	private static final class Entity {
		final String handle;
		final String layer;
		private DxfCause valueCause;
		private boolean paperSpace;
		private int flags;
		private int degree;
		private int knotCount = -1;
		private int pointCount;
		private int fitCount;
		private final List<Double> knots = new ArrayList<>();
		private final List<Double> weights = new ArrayList<>();
		private final List<Double> xs = new ArrayList<>();
		private final List<Double> ys = new ArrayList<>();
		private final List<Double> zs = new ArrayList<>();
		private final double[] normal = new double[3];
		private boolean hasNormal;
		private int fitPoints;
		private boolean tolerances;
		private boolean tangents;
		private final TreeSet<Integer> unrecognized = new TreeSet<>();

		Entity(List<Pair> pairs, boolean utf8) {
			String entityHandle = "";
			String entityLayer = "";
			for (int i = 1; i < pairs.size(); i++) {
				Pair pair = pairs.get(i);
				int code = pair.code;
				if (code == 5 && entityHandle.isEmpty()) {
					entityHandle = decode(pair.value, utf8);
				} else if (code == 8 && entityLayer.isEmpty()) {
					entityLayer = decode(pair.value, utf8);
				} else if (INTEGER_CODES.contains(code)) {
					integer(code, pair.ascii());
				} else if (REAL_CODES.contains(code)) {
					real(code, pair.ascii());
				} else if (!IGNORED_CODES.contains(code) && (code < 1000 || code > 1071)) {
					unrecognized.add(code);
				}
			}
			handle = entityHandle;
			layer = entityLayer;
		}

		private void integer(int code, String token) {
			Long value = parseInteger(token);
			if (value == null) {
				fail(DxfCause.MALFORMED_REAL);
				return;
			}
			int v = (int) (long) value;
			switch (code) {
			case 67:
				paperSpace |= v == 1;
				break;
			case 70:
				flags = v;
				break;
			case 71:
				degree = v;
				break;
			case 72:
				knotCount = v;
				break;
			case 73:
				pointCount = v;
				break;
			default:
				fitCount = v;
				break;
			}
		}

		private void real(int code, String token) {
			Double value = parseReal(token);
			if (value == null) {
				fail(DxfCause.MALFORMED_REAL);
				return;
			}
			if (!Double.isFinite(value)) {
				fail(DxfCause.NONFINITE_VALUE);
				return;
			}
			switch (code) {
			case 40:
				knots.add(value);
				break;
			case 41:
				weights.add(value);
				break;
			case 10:
				xs.add(value);
				break;
			case 20:
				ys.add(value);
				break;
			case 30:
				zs.add(value);
				break;
			case 11:
				fitPoints++;
				break;
			case 42:
			case 43:
			case 44:
				tolerances = true;
				break;
			case 210:
				normal[0] = value;
				hasNormal = true;
				break;
			case 220:
				normal[1] = value;
				hasNormal = true;
				break;
			case 230:
				normal[2] = value;
				hasNormal = true;
				break;
			case 12:
			case 13:
				tangents = true;
				break;
			default:
				// 21, 31, 22, 32, 23, 33: coordinates of ignored fit data
				break;
			}
		}

		private void fail(DxfCause cause) {
			if (valueCause == null) {
				valueCause = cause;
			}
		}

		/** First failing selection rule or admission predicate, or {@code null}. */
		@CheckForNull DxfCause admissionCause() {
			if (paperSpace && valueCause == null) {
				return DxfCause.PAPER_SPACE_UNSUPPORTED;
			}
			if (valueCause != null) {
				return valueCause;
			}
			if ((flags & 2) != 0) {
				return DxfCause.PERIODIC_UNSUPPORTED;
			}
			if (pointCount <= 0 || xs.isEmpty()) {
				return DxfCause.FIT_POINTS_ONLY_UNSUPPORTED;
			}
			if (degree < 1 || degree > DxfImportLimits.P_MAX_IMPORT) {
				return DxfCause.DEGREE_OUT_OF_RANGE;
			}
			if (xs.size() != pointCount || ys.size() != pointCount
					|| !zs.isEmpty() && zs.size() != pointCount) {
				return DxfCause.CONTROL_POINT_COUNT_MISMATCH;
			}
			if (pointCount < degree + 1) {
				return DxfCause.TOO_FEW_CONTROL_POINTS;
			}
			if (pointCount > DxfImportLimits.N_MAX_IMPORT) {
				return DxfCause.SIZE_OUT_OF_RANGE;
			}
			if (knots.size() != knotCount || knotCount != pointCount + degree + 1) {
				return DxfCause.KNOT_COUNT_MISMATCH;
			}
			BSplineValidation structure = BSplineData.validate(degree, array(knots),
					array(xs), array(ys), null, LIMITS);
			if (!structure.isValid()) {
				return map(structure.cause());
			}
			if (!weights.isEmpty() && weights.size() != pointCount) {
				return DxfCause.WEIGHT_COUNT_MISMATCH;
			}
			boolean rationalFlag = (flags & 4) != 0;
			if (!rationalFlag) {
				for (double weight : weights) {
					if (weight != 1.0) {
						return DxfCause.NON_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG;
					}
				}
			}
			double min = Double.POSITIVE_INFINITY;
			double max = 0;
			for (double weight : weights) {
				if (!(weight > 0.0)) {
					return DxfCause.NONPOSITIVE_WEIGHT;
				}
				min = Math.min(min, weight);
				max = Math.max(max, weight);
			}
			if (!weights.isEmpty() && max / min > DxfImportLimits.W_RATIO_MAX) {
				return DxfCause.WEIGHT_RATIO_OUT_OF_RANGE;
			}
			for (double z : zs) {
				if (z != zs.get(0)) {
					return DxfCause.NONPLANAR_UNSUPPORTED;
				}
			}
			return null;
		}

		private static DxfCause map(BSplineDataCause cause) {
			switch (cause) {
			case KNOTS_DECREASING:
				return DxfCause.KNOTS_DECREASING;
			case NOT_CLAMPED:
				return DxfCause.NOT_CLAMPED_UNSUPPORTED;
			case EMPTY_DOMAIN:
				return DxfCause.EMPTY_DOMAIN;
			case INTERIOR_MULTIPLICITY_EXCEEDED:
				return DxfCause.INTERIOR_MULTIPLICITY_UNSUPPORTED;
			case KNOT_COUNT_MISMATCH:
				return DxfCause.KNOT_COUNT_MISMATCH;
			case TOO_FEW_CONTROL_POINTS:
				return DxfCause.TOO_FEW_CONTROL_POINTS;
			case TOO_MANY_CONTROL_POINTS:
				return DxfCause.SIZE_OUT_OF_RANGE;
			case DEGREE_INVALID:
				return DxfCause.DEGREE_OUT_OF_RANGE;
			default:
				return DxfCause.NONFINITE_VALUE;
			}
		}

		/**
		 * Builds the record of an admitted entity; {@code null} when the transformation
		 * overflows a coordinate (rejected as {@link DxfCause#NONFINITE_VALUE}).
		 */
		@CheckForNull DxfSplineReference record(DxfFileProvenance file,
				DxfAffineTransform transform, int ordinal, DxfImportLimits limits) {
			double[] w = weights.isEmpty() ? null : array(weights);
			double[] x = array(xs);
			double[] y = array(ys);
			double[] u = array(knots);
			BSplineData source = BSplineData.validate(degree, u, x, y, w, LIMITS).data();
			double[] tx = new double[x.length];
			double[] ty = new double[y.length];
			for (int i = 0; i < x.length; i++) {
				tx[i] = transform.applyX(x[i], y[i]);
				ty[i] = transform.applyY(x[i], y[i]);
			}
			BSplineValidation transformed = BSplineData.validate(degree, u, tx, ty, w, LIMITS);
			if (!transformed.isValid()) {
				return null;
			}
			Set<DxfDiagnostic> diagnostics = EnumSet.noneOf(DxfDiagnostic.class);
			boolean rationalFlag = (flags & 4) != 0;
			if (rationalFlag && w == null) {
				diagnostics.add(DxfDiagnostic.RATIONAL_FLAG_WITH_IMPLICIT_UNIT_WEIGHTS);
			}
			if (!rationalFlag && w != null) {
				diagnostics.add(DxfDiagnostic.EXPLICIT_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG);
			}
			if (((flags & 1) != 0) != source.isExactlyClosed()) {
				diagnostics.add(DxfDiagnostic.CLOSED_FLAG_CONTRADICTED);
			}
			if (fitPoints > 0 || fitCount > 0) {
				diagnostics.add(DxfDiagnostic.FIT_POINTS_IGNORED);
			}
			if (tolerances) {
				diagnostics.add(DxfDiagnostic.TOLERANCES_IGNORED);
			}
			if (tangents) {
				diagnostics.add(DxfDiagnostic.TANGENTS_IGNORED);
			}
			if (hasNormal) {
				diagnostics.add(DxfDiagnostic.NORMAL_RECORDED);
			}
			if (!unrecognized.isEmpty()) {
				diagnostics.add(DxfDiagnostic.UNRECOGNIZED_GROUP_CODES);
			}
			DxfNature nature = !source.allWeightsEqual() ? DxfNature.RATIONAL
					: source.allWeightsUnit() ? DxfNature.POLYNOMIAL
							: DxfNature.RATIONAL_EQUAL_WEIGHTS;
			BSplineData data = transformed.data();
			BSplineData evaluation = data.polynomialEquivalent();
			DxfTessellation tessellation = DxfTessellation.of(
					evaluation == null ? data : evaluation, limits.renderMaxEvaluations());
			if (!tessellation.isComplete()) {
				diagnostics.add(DxfDiagnostic.RENDER_INCOMPLETE);
			}
			return new DxfSplineReference(nature, rationalFlag, source,
					zs.isEmpty() ? 0.0 : zs.get(0), transform, data, file, handle, layer,
					ordinal, hasNormal ? normal.clone() : null, diagnostics,
					new ArrayList<>(unrecognized), tessellation);
		}

		private static double[] array(List<Double> values) {
			double[] result = new double[values.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = values.get(i);
			}
			return result;
		}
	}

	/**
	 * String values are provenance only: UTF-8 for {@code AC1021} and later, ISO-8859-1
	 * otherwise; undecodable bytes are kept as escapes {@code \xNN}.
	 */
	static String decode(byte[] value, boolean utf8) {
		if (!utf8) {
			return new String(value, StandardCharsets.ISO_8859_1).trim();
		}
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
		try {
			CharBuffer chars = decoder.decode(ByteBuffer.wrap(value));
			return chars.toString().trim();
		} catch (CharacterCodingException exception) {
			StringBuilder escaped = new StringBuilder();
			for (byte b : value) {
				int unsigned = b & 0xFF;
				if (unsigned < 0x80) {
					escaped.append((char) unsigned);
				} else {
					escaped.append("\\x").append(Character.forDigit(unsigned >> 4, 16))
							.append(Character.forDigit(unsigned & 0xF, 16));
				}
			}
			return escaped.toString().trim();
		}
	}
}
