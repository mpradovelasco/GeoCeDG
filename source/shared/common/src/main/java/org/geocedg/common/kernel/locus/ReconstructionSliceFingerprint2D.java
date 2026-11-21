/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersect;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Exact, versioned fingerprint of the non-driver inputs read by a reconstruction slice
 * (PRE-G9B-CU2, D-CU2-20-R; {@code locus-v2-semantics.md} v1.3).
 *
 * <p>It has four parts, each digested separately so that the cause of a change is
 * typed:</p>
 * <ul>
 * <li>structure: the serialized definition of every algorithm and the serialized options of
 * every dependent element, without the tags of its derived value, which its inputs and its
 * definition determine, and without the tags of its drawing state that the file loader
 * applies only after the whole construction is read; a free element enters only by its kind
 * and label, because its serialized form is representation only; the token ledger of a
 * Locus V2 intersection that follows the driver enters without its evaluation state at the
 * driver position, the localization evidence of its roots (post-R1 repair A);</li>
 * <li>values: the exact binary64 bits of every independent element, of every unlabelled
 * input of an algorithm and of every number literal of a definition, and the parameters of
 * the free place of every point on a path or in a region that depends on the driver and is
 * not a driver root (finding H-CU2A-9); the values that are evaluation state of a nested
 * Locus V2 at the position of its own driver, and reach the traced point only through the
 * output of a locus of that driver, enter like the values that follow the driver of the
 * slice: without their exact state, with the free place of every point that has one and is
 * not a nested driver root (post-R1 repair A');</li>
 * <li>selection: the selection state of every line-conic intersection, read through
 * read-only accessors (D-CU2-17);</li>
 * <li>labels: the label state of the outputs of every intersection.</li>
 * </ul>
 *
 * <p>Moving only the driver leaves every part unchanged. The fingerprint uses no Java
 * object identity, proximity, circumstantial index or drawing data, and never replaces a
 * durable identity.</p>
 */
public final class ReconstructionSliceFingerprint2D {
	/** Version participating in the evaluator signature. */
	public static final String VERSION = "reconstruction-slice-fingerprint/v3";

	/**
	 * Tags that carry the derived value of a dependent element, recomputed from its inputs
	 * and its definition, or the driver position of a point on a path or in a region. Each
	 * is removed with the whitespace before it, so that its presence never changes the
	 * structure part.
	 */
	private static final Pattern DERIVED_VALUE_TAG = Pattern.compile(
			"\\s*<(?:coords|value|matrix|eigenvectors|pathParameter|regionParameters"
					+ "|conicPointState)\\b[^>]*/>");
	/**
	 * Tags of drawing state that the file loader applies after the whole construction is
	 * read, once the slice has been published (animation, visibility condition, dynamic
	 * colour and caption, start point, linked input): they never change a value of the
	 * slice, and keeping them would publish a second revision of every locus after loading.
	 * Each is removed with the whitespace before it: an element that a document rebuilt by
	 * undo writes without its colour, such as an unlabelled angle input, has the structure
	 * of the element written with it (PRE-G9B-CU2; the fingerprint is never based on a
	 * graphical representation, {@code locus-v2-semantics.md} v1.3).
	 */
	private static final Pattern DEFERRED_DRAWING_TAG = Pattern.compile(
			"\\s*<(?:animation|condition|objColor|dynamicCaption|startPoint|linkedGeo)"
					+ "\\b[^>]*/>");
	/**
	 * The persisted token ledger of a Locus V2 intersection result. For a result that follows
	 * the driver, its state at the driver position is evaluation state: the provider-canonical
	 * parameter of each root is revision-scoped localization evidence, not durable identity
	 * ({@code locus-v2-intersections.md} section 6.1), and a claimed root is current or dormant
	 * only at that position. The structure part takes the ledger without them.
	 */
	private static final Pattern TOKEN_LEDGER_TAG = Pattern.compile(
			"<locusIntersectionTokenLedger\\b[^>]*/>");
	private static final Comparator<GeoElement> CONSTRUCTION_ORDER =
			Comparator.comparingInt(GeoElement::getConstructionIndex)
					.thenComparingLong(GeoElement::getID);

	/** Part of the snapshot whose change explains a new revision. */
	public enum Change {
		/** The snapshot is unchanged: an equivalent recompute. */
		NONE,
		/** A definition, an element option or the slice composition changed. */
		STRUCTURE,
		/** An exact authoritative value changed. */
		VALUES,
		/** The label state of an intersection output changed. */
		LABELS,
		/** The inherited selection state of an intersection changed. */
		SELECTION
	}

	/** Exact, structured snapshot of the non-driver inputs of a slice. */
	public static final class Snapshot {
		private final String structure;
		private final String values;
		private final String selection;
		private final String labels;
		private final String digest;

		private Snapshot(String structure, String values, String selection,
				String labels) {
			this.structure = structure;
			this.values = values;
			this.selection = selection;
			this.labels = labels;
			this.digest = sha256(VERSION + "|" + ReconstructionFidelity2D.VERSION
					+ "|" + structure + "|" + values + "|" + selection + "|" + labels);
		}

		/** @return digest of the whole snapshot */
		public String getDigest() {
			return digest;
		}

		/** @return digest of the structure part */
		public String getStructureDigest() {
			return structure;
		}

		/** @return digest of the exact values part */
		public String getValuesDigest() {
			return values;
		}

		/** @return digest of the selection part */
		public String getSelectionDigest() {
			return selection;
		}

		/** @return digest of the label part */
		public String getLabelsDigest() {
			return labels;
		}

		/**
		 * @param previous snapshot of the published revision, or {@code null}
		 * @return the first changed part, in the order structure, values, labels,
		 *         selection
		 */
		public Change changeFrom(Snapshot previous) {
			if (previous == null || !structure.equals(previous.structure)) {
				return Change.STRUCTURE;
			}
			if (!values.equals(previous.values)) {
				return Change.VALUES;
			}
			if (!labels.equals(previous.labels)) {
				return Change.LABELS;
			}
			return selection.equals(previous.selection) ? Change.NONE : Change.SELECTION;
		}
	}

	private ReconstructionSliceFingerprint2D() {
		// Utility class.
	}

	/**
	 * @param slice elements read by the reconstruction slice
	 * @param driverRoots the driver state and coordinate
	 * @return SHA-256 hexadecimal digest of the exact slice snapshot
	 */
	public static String compute(Collection<GeoElement> slice,
			Collection<GeoElement> driverRoots) {
		return snapshot(slice, driverRoots).getDigest();
	}

	/**
	 * @param slice elements read by the reconstruction slice
	 * @param driverRoots the driver state and coordinate
	 * @return exact, structured snapshot of the non-driver inputs
	 */
	public static Snapshot snapshot(Collection<GeoElement> slice,
			Collection<GeoElement> driverRoots) {
		return snapshot(slice, driverRoots, null);
	}

	/**
	 * Whether every reuse of a {@link StructureMemo} is checked against the complete
	 * computation (system property or environment variable; verification only).
	 */
	private static final boolean VERIFY_MEMO = Boolean.getBoolean(
			"geocedg.fingerprint.verifyMemo")
			|| "1".equals(System.getenv("GEOCEDG_FINGERPRINT_VERIFY_MEMO"));

	/**
	 * Structure part and dependency analysis of the last snapshot of one producer whose slice
	 * lives in an isolated evaluation copy (post-R1, D-V6). Such a copy is never edited: the
	 * definition and the options of its elements are fixed when it is filled, and afterwards
	 * only the values that follow a driver, the labels withheld by its owner and the token
	 * ledgers of its intersections change. The memo is reused only while every element of
	 * the slice is the same object with the same label state and definedness, the same parent
	 * algorithm with the same output labels, and the same ledger text; anything else is
	 * computed again. It holds no value, no selection state and no result: those parts are
	 * always read afresh. A live document never uses it.
	 */
	public static final class StructureMemo {
		private Object[] roots;
		private Object[] elements;
		private Set<GeoElement> driverDependent;
		private Set<GeoElement> nestedState;
		private Set<GeoElement> nestedRoots;
		private Set<GeoElement> neutral;
		private Object[] key;
		private String structure;
		private long reuses;
		private long computations;

		/** @return snapshots that reused the structure part */
		public long getReuses() {
			return reuses;
		}

		/** @return snapshots that computed the structure part */
		public long getComputations() {
			return computations;
		}
	}

	/**
	 * @param slice elements read by the reconstruction slice
	 * @param driverRoots the driver state and coordinate
	 * @param memo the memo of a producer in an isolated evaluation copy, or {@code null}
	 * @return exact, structured snapshot of the non-driver inputs
	 */
	public static Snapshot snapshot(Collection<GeoElement> slice,
			Collection<GeoElement> driverRoots, StructureMemo memo) {
		List<GeoElement> ordered = new ArrayList<>(Objects.requireNonNull(slice));
		ordered.sort(CONSTRUCTION_ORDER);
		Object[] rootKey = driverRoots.toArray();
		Object[] elementKey = ordered.toArray();
		boolean sameGraph = memo != null && memo.driverDependent != null
				&& sameObjects(memo.roots, rootKey) && sameObjects(memo.elements, elementKey);
		Set<GeoElement> driverDependent;
		Set<GeoElement> nestedState;
		Set<GeoElement> nestedRoots;
		Set<GeoElement> neutral;
		if (sameGraph && !VERIFY_MEMO) {
			driverDependent = memo.driverDependent;
			nestedState = memo.nestedState;
			nestedRoots = memo.nestedRoots;
			neutral = memo.neutral;
		} else {
			driverDependent = ReconstructionExactState2D.driverDependent(ordered, driverRoots);
			// Post-R1 repair A': evaluation state of a nested locus at its own driver position.
			nestedState = ReconstructionExactState2D.nestedDriverState(ordered, driverDependent);
			nestedRoots = ReconstructionExactState2D.nestedDriverRoots(ordered);
			neutral = Collections.newSetFromMap(new IdentityHashMap<>());
			neutral.addAll(driverDependent);
			neutral.addAll(nestedState);
			if (sameGraph && (!sameSet(driverDependent, memo.driverDependent)
					|| !sameSet(nestedState, memo.nestedState)
					|| !sameSet(nestedRoots, memo.nestedRoots))) {
				throw new IllegalStateException(
						"Slice dependency analysis changed in an isolated copy");
			}
			if (memo != null) {
				memo.roots = rootKey;
				memo.elements = elementKey;
				memo.driverDependent = driverDependent;
				memo.nestedState = nestedState;
				memo.nestedRoots = nestedRoots;
				memo.neutral = neutral;
				if (!sameGraph) {
					memo.key = null;
					memo.structure = null;
				}
			}
		}
		Object[] structureKey = memo == null ? null : structureKey(ordered, neutral);
		boolean reuse = memo != null && memo.key != null && sameKey(memo.key, structureKey);
		Set<AlgoElement> written = Collections.newSetFromMap(new IdentityHashMap<>());
		StringBuilder structure = reuse && !VERIFY_MEMO ? null : new StringBuilder();
		StringBuilder values = new StringBuilder();
		StringBuilder selection = new StringBuilder();
		StringBuilder labels = new StringBuilder();
		for (GeoElement geo : ordered) {
			String label = geo.getLabelSimple();
			AlgoElement parent = geo.getParentAlgorithm();
			if (parent != null && written.add(parent)) {
				if (structure != null) {
					XMLStringBuilder algorithm = new XMLStringBuilder(new StringBuilder());
					parent.getXML(algorithm, false);
					structure.append("|A:").append(algorithm);
				}
				for (ReconstructionExactState2D.InlineInput inline
						: ReconstructionExactState2D.inlineInputs(parent)) {
					appendInline(values, label, inline, neutral);
				}
				appendSelection(selection, labels, label, parent);
			}
			String text = null;
			if (geo.isIndependent()) {
				// The serialized form of a free element is representation only: a value
				// set by program drops its literal and adds options. Its exact value is
				// in the values part, unless it is the driver.
				text = geo.getClass().getSimpleName() + ":" + label;
				if (!neutral.contains(geo)) {
					values.append("|V:").append(label).append(':')
							.append(ReconstructionExactState2D.of(geo));
				}
			} else {
				if (structure != null) {
					XMLStringBuilder element = new XMLStringBuilder(new StringBuilder());
					geo.getXML(false, element);
					text = DEFERRED_DRAWING_TAG.matcher(DERIVED_VALUE_TAG.matcher(
							element.toString()).replaceAll("")).replaceAll("");
					if (geo instanceof GeoLocusIntersectionResult && neutral.contains(geo)) {
						text = withEvaluationNeutralLedger(text,
								(GeoLocusIntersectionResult) geo);
					}
				}
				if (!neutral.contains(geo)) {
					// Derived, but not always a function of the slice inputs: a random
					// value changes on recomputation and must publish a revision.
					values.append("|D:").append(label).append(':')
							.append(ReconstructionExactState2D.of(geo));
				} else if (driverRoots.stream().noneMatch(root -> root == geo)
						&& !(nestedState.contains(geo) && nestedRoots.contains(geo))) {
					// Its coordinates follow the driver, but the place of a point on a path
					// or in a region is its own input (PRE-G9B-CU2, finding H-CU2A-9).
					String place = ReconstructionExactState2D.freePlace(geo);
					if (place != null) {
						values.append("|F:").append(label).append(':').append(place);
					}
				}
			}
			if (structure != null) {
				structure.append("|G:").append(text);
			}
			if (!geo.isIndependent()) {
				appendLiterals(values, "D:" + label, geo);
			}
		}
		String structureDigest;
		if (structure == null) {
			structureDigest = memo.structure;
			memo.reuses++;
		} else {
			structureDigest = sha256(structure.toString());
			if (reuse && !structureDigest.equals(memo.structure)) {
				throw new IllegalStateException(
						"Slice structure changed in an isolated copy under an equal key");
			}
			if (memo != null) {
				memo.key = structureKey;
				memo.structure = structureDigest;
				if (reuse) {
					memo.reuses++;
				} else {
					memo.computations++;
				}
			}
		}
		return new Snapshot(structureDigest, sha256(values.toString()),
				sha256(selection.toString()), sha256(labels.toString()));
	}

	/**
	 * Everything that can change the structure text of a slice inside an isolated copy, read
	 * without serializing: per element, the object, its label state and definedness, its parent
	 * algorithm with the labels of its outputs, and the ledger text of an intersection result
	 * exactly as the structure part would take it.
	 */
	private static Object[] structureKey(List<GeoElement> ordered, Set<GeoElement> neutral) {
		List<Object> key = new ArrayList<>(ordered.size() * 6);
		Set<AlgoElement> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		for (GeoElement geo : ordered) {
			key.add(geo);
			key.add(geo.getLabelSimple());
			key.add(geo.isLabelSet());
			key.add(geo.isDefined());
			AlgoElement parent = geo.getParentAlgorithm();
			key.add(parent);
			if (parent != null && seen.add(parent)) {
				for (GeoElement output : parent.getOutput()) {
					key.add(output == null ? null : output.getLabelSimple());
				}
			}
			if (geo instanceof GeoLocusIntersectionResult) {
				GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) geo;
				key.add(neutral.contains(geo) ? result.getTokenLedgerEvaluationNeutralState()
						: result.getTokenLedgerState());
			}
		}
		return key.toArray();
	}

	private static boolean sameKey(Object[] first, Object[] second) {
		if (first.length != second.length) {
			return false;
		}
		for (int index = 0; index < first.length; index++) {
			Object a = first[index];
			Object b = second[index];
			boolean same = a instanceof GeoElement || a instanceof AlgoElement ? a == b
					: Objects.equals(a, b);
			if (!same) {
				return false;
			}
		}
		return true;
	}

	private static boolean sameObjects(Object[] first, Object[] second) {
		if (first == null || first.length != second.length) {
			return false;
		}
		for (int index = 0; index < first.length; index++) {
			if (first[index] != second[index]) {
				return false;
			}
		}
		return true;
	}

	private static boolean sameSet(Set<GeoElement> first, Set<GeoElement> second) {
		return first.size() == second.size() && first.containsAll(second);
	}

	/**
	 * A result that follows neither the driver of the slice nor, as nested evaluation state,
	 * the driver of a nested locus keeps its complete serialized ledger; this replacement is
	 * applied only to one that does.
	 */
	private static String withEvaluationNeutralLedger(String text,
			GeoLocusIntersectionResult result) {
		String neutral = result.getTokenLedgerEvaluationNeutralState();
		if (neutral == null) {
			return text;
		}
		return TOKEN_LEDGER_TAG.matcher(text).replaceAll(Matcher.quoteReplacement(
				"<locusIntersectionTokenLedger neutral=\"" + neutral + "\"/>"));
	}

	private static void appendInline(StringBuilder values, String owner,
			ReconstructionExactState2D.InlineInput inline, Set<GeoElement> driverDependent) {
		values.append("|I:").append(owner).append(':').append(inline.path);
		if (inline.geo.isIndependent()) {
			values.append(':').append(ReconstructionExactState2D.of(inline.geo));
			return;
		}
		appendLiterals(values, "", inline.geo);
		if (!driverDependent.contains(inline.geo)
				&& !readsDriver(inline.geo, driverDependent)) {
			values.append(":=").append(ReconstructionExactState2D.of(inline.geo));
		}
	}

	private static boolean readsDriver(GeoElement geo, Set<GeoElement> driverDependent) {
		for (GeoElement predecessor : geo.getAllPredecessors()) {
			if (driverDependent.contains(predecessor)) {
				return true;
			}
		}
		return false;
	}

	private static void appendLiterals(StringBuilder values, String owner,
			GeoElement geo) {
		int index = 0;
		for (MyDouble literal : ReconstructionExactState2D.literals(geo.getDefinition())) {
			values.append("|L:").append(owner).append(':').append(index++).append(':')
					.append(Double.toHexString(literal.getDouble()));
		}
	}

	private static void appendSelection(StringBuilder selection, StringBuilder labels,
			String owner, AlgoElement algorithm) {
		if (algorithm instanceof AlgoIntersectSingle) {
			AlgoIntersectSingle single = (AlgoIntersectSingle) algorithm;
			selection.append("|S1:").append(owner).append(":index=")
					.append(single.getSelectedIndex());
			appendSelection(selection, labels, owner + "/source",
					single.getSourceIntersection());
			return;
		}
		if (algorithm instanceof AlgoIntersectLineConic) {
			AlgoIntersectLineConic intersection = (AlgoIntersectLineConic) algorithm;
			selection.append("|S:").append(owner).append(":permutation=")
					.append(Arrays.toString(intersection.getSelectionPermutation()))
					.append(":first=")
					.append(intersection.isSelectionFirstIntersectionPending())
					.append(":pending=")
					.append(intersection.isSelectionPermutationPending());
		}
		if (algorithm instanceof AlgoIntersect) {
			labels.append("|O:").append(owner).append(':');
			for (GeoElement output : algorithm.getOutput()) {
				labels.append(output.isLabelSet() ? '1' : '0');
			}
		}
	}

	private static String sha256(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				hex.append(Character.forDigit((value >> 4) & 0xF, 16))
						.append(Character.forDigit(value & 0xF, 16));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}
