/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.SetRandomValue;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersect;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Fidelity of a reconstructed slice to the live inputs it reads (PRE-G9B-CU2, D-CU2-20-R;
 * {@code locus-v2-semantics.md} v1.3; certification design section 8.1, phases 4 and 5).
 *
 * <p>The slice copy is rebuilt from the serialized slice; nothing guarantees that it reads
 * the same binary64 values or the same inherited selection state as the live construction.
 * The validation compares, bit for bit: every labelled independent element that does not
 * depend on the driver, every random value, every unlabelled input of an algorithm (by its
 * input path), every number literal of a definition (by tree order), the selection state of
 * every line-conic intersection and the label state of every intersection output.</p>
 *
 * <p>Restoration is admitted only from the live construction to the copy and only through
 * the correspondence of the reconstruction itself: the label with which the serialized slice
 * defined an element, the input path of an algorithm, and the order of literals in trees of
 * the same shape. Each restoration is an exact value assignment followed by the update of the
 * copy. Selection and label states are never written (D-CU2-17); a discrepancy in them is
 * never restored. A slice with a remaining discrepancy is unfaithful, and its evaluator fails
 * explicitly; the copy is never an authority over the live construction.</p>
 */
public final class ReconstructionFidelity2D {
	/** Version of the reconstruction fidelity contract. */
	public static final String VERSION = "reconstruction-fidelity/v1";

	private static final Comparator<GeoElement> CONSTRUCTION_ORDER =
			Comparator.comparingInt(GeoElement::getConstructionIndex)
					.thenComparingLong(GeoElement::getID);

	/** Kind of a discrepancy between the live construction and its slice copy. */
	public enum DiscrepancyKind {
		/** The copy has no element for a labelled element of the slice. */
		MISSING_COPY,
		/** The copy differs in kind, algorithm, inputs or expression shape. */
		STRUCTURE,
		/** A labelled independent value differs. */
		VALUE,
		/** An unlabelled input of an algorithm differs. */
		INLINE_VALUE,
		/** A number literal of a definition differs. */
		LITERAL,
		/** The selection state of an intersection differs; never restored. */
		SELECTION,
		/** The label state of an intersection output differs; never restored. */
		LABELS
	}

	/** One discrepancy found by the validation. */
	public static final class Discrepancy {
		private final String element;
		private final DiscrepancyKind kind;
		private final String live;
		private final String copy;

		private Discrepancy(String element, DiscrepancyKind kind, String live,
				String copy) {
			this.element = element;
			this.kind = kind;
			this.live = live;
			this.copy = copy;
		}

		/** @return label or input path of the element */
		public String getElement() {
			return element;
		}

		/** @return kind of the discrepancy */
		public DiscrepancyKind getKind() {
			return kind;
		}

		/** @return exact live state */
		public String getLive() {
			return live;
		}

		/** @return exact copy state */
		public String getCopy() {
			return copy;
		}

		@Override
		public String toString() {
			return element + ":" + kind + ":live=" + live + ":copy=" + copy;
		}
	}

	/** Typed result of one validation. */
	public static final class Report {
		private final List<Discrepancy> discrepancies;
		private final int compared;
		private final int restorations;

		private Report(List<Discrepancy> discrepancies, int compared, int restorations) {
			this.discrepancies = Collections.unmodifiableList(
					new ArrayList<>(discrepancies));
			this.compared = compared;
			this.restorations = restorations;
		}

		/** @return whether the copy reproduces every authoritative input exactly */
		public boolean isFaithful() {
			return discrepancies.isEmpty();
		}

		/** @return discrepancies that remain after the admitted restorations */
		public List<Discrepancy> getDiscrepancies() {
			return discrepancies;
		}

		/** @return whether every remaining discrepancy is a selection or label state */
		public boolean isSelectionOnly() {
			for (Discrepancy discrepancy : discrepancies) {
				if (discrepancy.kind != DiscrepancyKind.SELECTION
						&& discrepancy.kind != DiscrepancyKind.LABELS) {
					return false;
				}
			}
			return !discrepancies.isEmpty();
		}

		/** @return number of corresponding labelled elements compared */
		public int getCompared() {
			return compared;
		}

		/** @return number of admitted restorations applied to the copy */
		public int getRestorations() {
			return restorations;
		}

		@Override
		public String toString() {
			return (isFaithful() ? "faithful" : "unfaithful " + discrepancies)
					+ ":compared=" + compared + ":restorations=" + restorations;
		}
	}

	private ReconstructionFidelity2D() {
		// Utility class.
	}

	/**
	 * Validates a reconstructed slice against the live construction and, when asked,
	 * restores the admitted discrepancies in the copy and validates again.
	 *
	 * @param liveSlice live elements serialized into the slice
	 * @param isolated reconstructed copy of the slice
	 * @param driverRoots live driver state and coordinate
	 * @param restore whether admitted discrepancies are restored in the copy
	 * @return typed validation report
	 */
	public static Report validate(Collection<GeoElement> liveSlice, Construction isolated,
			Collection<GeoElement> driverRoots, boolean restore) {
		List<GeoElement> ordered = new ArrayList<>(Objects.requireNonNull(liveSlice));
		ordered.sort(CONSTRUCTION_ORDER);
		Set<GeoElement> driverDependent = ReconstructionExactState2D.driverDependent(
				ordered, driverRoots);
		Pass first = new Pass(isolated, driverDependent, restore);
		first.compare(ordered);
		if (!restore || first.restorations == 0) {
			return new Report(first.discrepancies, first.compared, 0);
		}
		first.refresh();
		Pass second = new Pass(isolated, driverDependent, false);
		second.compare(ordered);
		return new Report(second.discrepancies, first.compared, first.restorations);
	}

	/** One comparison pass over the slice. */
	private static final class Pass {
		private final Construction isolated;
		private final Set<GeoElement> driverDependent;
		private final boolean restore;
		private final List<Discrepancy> discrepancies = new ArrayList<>();
		private final Set<AlgoElement> visited =
				Collections.newSetFromMap(new IdentityHashMap<>());
		private final List<GeoElement> restoredValues = new ArrayList<>();
		private final List<AlgoElement> restoredAlgorithms = new ArrayList<>();
		private int compared;
		private int restorations;

		private Pass(Construction isolated, Set<GeoElement> driverDependent,
				boolean restore) {
			this.isolated = isolated;
			this.driverDependent = driverDependent;
			this.restore = restore;
		}

		private void compare(List<GeoElement> ordered) {
			for (GeoElement live : ordered) {
				if (!live.isLabelSet()) {
					continue;
				}
				String label = live.getLabelSimple();
				GeoElement copy = isolated.lookupLabel(label);
				if (copy == null) {
					add(label, DiscrepancyKind.MISSING_COPY, "present", "absent");
					continue;
				}
				if (copy.getClass() != live.getClass()) {
					add(label, DiscrepancyKind.STRUCTURE, live.getClass().getSimpleName(),
							copy.getClass().getSimpleName());
					continue;
				}
				compared++;
				if (live.isIndependent()) {
					if (!driverDependent.contains(live)) {
						compareValue(label, DiscrepancyKind.VALUE, live, copy, null);
					}
					continue;
				}
				AlgoElement parent = live.getParentAlgorithm();
				AlgoElement copyParent = copy.getParentAlgorithm();
				if (copyParent == null || parent.getClass() != copyParent.getClass()) {
					add(label, DiscrepancyKind.STRUCTURE, String.valueOf(parent),
							String.valueOf(copyParent));
					continue;
				}
				if (live.isRandomGeo() && !driverDependent.contains(live)) {
					compareRandom(label, live, copy, copyParent);
				}
				compareLiterals(label, live.getDefinition(), copy.getDefinition(),
						copyParent);
				if (visited.add(parent)) {
					compareInline(label, parent, copyParent);
					compareSelection(label, parent, copyParent);
				}
			}
		}

		private void compareInline(String owner, AlgoElement parent,
				AlgoElement copyParent) {
			List<ReconstructionExactState2D.InlineInput> live =
					ReconstructionExactState2D.inlineInputs(parent);
			List<ReconstructionExactState2D.InlineInput> copy =
					ReconstructionExactState2D.inlineInputs(copyParent);
			if (live.size() != copy.size()) {
				add(owner, DiscrepancyKind.STRUCTURE, "inline=" + live.size(),
						"inline=" + copy.size());
				return;
			}
			for (int index = 0; index < live.size(); index++) {
				ReconstructionExactState2D.InlineInput liveInput = live.get(index);
				ReconstructionExactState2D.InlineInput copyInput = copy.get(index);
				String element = owner + "#" + liveInput.path;
				if (!liveInput.path.equals(copyInput.path)
						|| liveInput.geo.getClass() != copyInput.geo.getClass()
						|| liveInput.geo.isIndependent() != copyInput.geo.isIndependent()) {
					add(element, DiscrepancyKind.STRUCTURE, liveInput.path,
							copyInput.path);
				} else if (liveInput.geo.isIndependent()) {
					compareValue(element, DiscrepancyKind.INLINE_VALUE, liveInput.geo,
							copyInput.geo, copyParent);
				} else {
					compareLiterals(element, liveInput.geo.getDefinition(),
							copyInput.geo.getDefinition(),
							copyInput.geo.getParentAlgorithm());
				}
			}
		}

		private void compareValue(String element, DiscrepancyKind kind, GeoElement live,
				GeoElement copy, AlgoElement copyUser) {
			String liveState = ReconstructionExactState2D.of(live);
			String copyState = ReconstructionExactState2D.of(copy);
			if (liveState.equals(copyState)) {
				return;
			}
			if (restore) {
				copy.set(live);
				restorations++;
				restoredValues.add(copy);
				if (copyUser != null) {
					restoredAlgorithms.add(copyUser);
				}
			} else {
				add(element, kind, liveState, copyState);
			}
		}

		/**
		 * A random value is state of its algorithm, not a function of the slice inputs; it
		 * is restored through the algorithm's own random-value interface.
		 */
		private void compareRandom(String element, GeoElement live, GeoElement copy,
				AlgoElement copyParent) {
			String liveState = ReconstructionExactState2D.of(live);
			String copyState = ReconstructionExactState2D.of(copy);
			if (liveState.equals(copyState)) {
				return;
			}
			if (restore && copyParent instanceof SetRandomValue
					&& ((SetRandomValue) copyParent).setRandomValue(live)) {
				restorations++;
				restoredValues.add(copy);
			} else {
				add(element, DiscrepancyKind.VALUE, liveState, copyState);
			}
		}

		private void compareLiterals(String element, ExpressionNode live,
				ExpressionNode copy, AlgoElement copyParent) {
			if (live == null && copy == null) {
				return;
			}
			if (!ReconstructionExactState2D.sameShape(live, copy)) {
				add(element, DiscrepancyKind.STRUCTURE, "definition-shape",
						"definition-shape");
				return;
			}
			List<MyDouble> liveLiterals = ReconstructionExactState2D.literals(live);
			List<MyDouble> copyLiterals = ReconstructionExactState2D.literals(copy);
			for (int index = 0; index < liveLiterals.size(); index++) {
				double value = liveLiterals.get(index).getDouble();
				MyDouble copyLiteral = copyLiterals.get(index);
				if (Double.doubleToRawLongBits(value)
						== Double.doubleToRawLongBits(copyLiteral.getDouble())) {
					continue;
				}
				if (restore && copyParent != null) {
					copyLiteral.set(value);
					restorations++;
					restoredAlgorithms.add(copyParent);
				} else {
					add(element + "@" + index, DiscrepancyKind.LITERAL,
							Double.toHexString(value),
							Double.toHexString(copyLiteral.getDouble()));
				}
			}
		}

		private void compareSelection(String owner, AlgoElement live, AlgoElement copy) {
			if (live instanceof AlgoIntersectSingle) {
				AlgoIntersectSingle liveSingle = (AlgoIntersectSingle) live;
				AlgoIntersectSingle copySingle = (AlgoIntersectSingle) copy;
				if (liveSingle.getSelectedIndex() != copySingle.getSelectedIndex()) {
					add(owner, DiscrepancyKind.SELECTION,
							"index=" + liveSingle.getSelectedIndex(),
							"index=" + copySingle.getSelectedIndex());
				}
				AlgoIntersect liveSource = liveSingle.getSourceIntersection();
				AlgoIntersect copySource = copySingle.getSourceIntersection();
				if (liveSource != null && copySource != null
						&& liveSource.getClass() == copySource.getClass()) {
					compareSelection(owner + "/source", liveSource, copySource);
				} else if (liveSource != copySource) {
					add(owner + "/source", DiscrepancyKind.STRUCTURE,
							String.valueOf(liveSource), String.valueOf(copySource));
				}
				return;
			}
			if (live instanceof AlgoIntersectLineConic) {
				AlgoIntersectLineConic liveLineConic = (AlgoIntersectLineConic) live;
				AlgoIntersectLineConic copyLineConic = (AlgoIntersectLineConic) copy;
				// A pending live selection has no effective rule yet, so there is nothing
				// to reproduce; when it settles, the selection part of the snapshot
				// changes, the slice is rebuilt and this comparison runs again.
				boolean livePending = liveLineConic.isSelectionPermutationPending()
						|| liveLineConic.isSelectionFirstIntersectionPending();
				String liveState = selectionState(liveLineConic);
				String copyState = selectionState(copyLineConic);
				if (!livePending && !liveState.equals(copyState)) {
					add(owner, DiscrepancyKind.SELECTION, liveState, copyState);
				}
			}
			if (live instanceof AlgoIntersect) {
				String liveLabels = labelState(live);
				String copyLabels = labelState(copy);
				if (!liveLabels.equals(copyLabels)) {
					add(owner, DiscrepancyKind.LABELS, liveLabels, copyLabels);
				}
			}
		}

		/**
		 * Updates the copy after the restorations, in construction order, without
		 * reinitializing any inherited selection state.
		 */
		private void refresh() {
			List<GeoElement> roots = new ArrayList<>(restoredValues);
			roots.sort(CONSTRUCTION_ORDER);
			for (GeoElement root : roots) {
				root.updateCascade();
			}
			Set<AlgoElement> done = Collections.newSetFromMap(new IdentityHashMap<>());
			List<AlgoElement> algorithms = new ArrayList<>(restoredAlgorithms);
			algorithms.sort(Comparator.comparingInt(AlgoElement::getConstructionIndex));
			for (AlgoElement algorithm : algorithms) {
				if (done.add(algorithm)) {
					algorithm.compute();
					for (GeoElement output : algorithm.getOutput()) {
						output.updateCascade();
					}
				}
			}
		}

		private void add(String element, DiscrepancyKind kind, String live, String copy) {
			discrepancies.add(new Discrepancy(element, kind, live, copy));
		}
	}

	private static String selectionState(AlgoIntersectLineConic intersection) {
		return "permutation=" + Arrays.toString(intersection.getSelectionPermutation())
				+ ",first=" + intersection.isSelectionFirstIntersectionPending()
				+ ",pending=" + intersection.isSelectionPermutationPending();
	}

	private static String labelState(AlgoElement intersection) {
		StringBuilder state = new StringBuilder();
		for (GeoElement output : intersection.getOutput()) {
			state.append(output.isLabelSet() ? '1' : '0');
		}
		return state.toString();
	}
}
