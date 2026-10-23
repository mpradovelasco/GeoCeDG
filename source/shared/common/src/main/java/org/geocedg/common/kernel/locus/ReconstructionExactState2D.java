/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.RegionParameters;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoPointInRegion;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoLocusND;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.GeoVec3D;
import org.geogebra.common.kernel.kernelND.GeoConicND;

/**
 * Exact, representation-independent state of the authoritative inputs read by a
 * reconstruction slice (PRE-G9B-CU2, D-CU2-20-R), shared by the revision fingerprint and the
 * fidelity validation: binary64 values as hexadecimal bits, and the number literals of a
 * definition in tree order.
 */
final class ReconstructionExactState2D {
	private static final int MAXIMUM_INLINE_DEPTH = 8;

	private ReconstructionExactState2D() {
		// Utility class.
	}

	/**
	 * @return exact state of a value, never a rounded text
	 */
	static String of(GeoElement geo) {
		if (geo instanceof GeoNumeric) {
			return "num:" + Double.toHexString(((GeoNumeric) geo).getValue());
		}
		if (geo instanceof GeoBoolean) {
			return "bool:" + ((GeoBoolean) geo).getBoolean();
		}
		if (geo instanceof GeoVec3D) {
			GeoVec3D vector = (GeoVec3D) geo;
			return geo.getClass().getSimpleName() + ":" + Double.toHexString(vector.getX())
					+ "," + Double.toHexString(vector.getY()) + ","
					+ Double.toHexString(vector.getZ());
		}
		if (geo instanceof GeoConicND) {
			StringBuilder matrix = new StringBuilder("conic:");
			for (double coefficient : ((GeoConicND) geo).getMatrix()) {
				matrix.append(Double.toHexString(coefficient)).append(',');
			}
			return matrix.toString();
		}
		if (geo instanceof GeoText) {
			return "text:" + ((GeoText) geo).getTextString();
		}
		if (geo instanceof GeoList) {
			GeoList list = (GeoList) geo;
			StringBuilder items = new StringBuilder("list:[");
			for (int index = 0; index < list.size(); index++) {
				items.append(of(list.get(index))).append(';');
			}
			return items.append(']').toString();
		}
		XMLStringBuilder xml = new XMLStringBuilder(new StringBuilder());
		geo.getXML(false, xml);
		return geo.getClass().getSimpleName() + ":" + xml;
	}

	/**
	 * The place of a point on a path without a parameter input, or of a point in a region, is
	 * its own state: its parameters are an input of the construction, not a value derived from
	 * its path or region, and the serialized form keeps only the coordinates that they give
	 * (PRE-G9B-CU2, finding H-CU2A-9).
	 *
	 * @return exact parameters of that place, or {@code null} for any other element
	 */
	static String freePlace(GeoElement geo) {
		AlgoElement parent = geo.getParentAlgorithm();
		if (!(geo instanceof GeoPoint)) {
			return null;
		}
		if (parent instanceof AlgoPointOnPath && parent.getInputLength() == 1) {
			PathParameter path = ((GeoPoint) geo).getPathParameter();
			return "path:" + Double.toHexString(path.getT()) + "," + path.getPathType();
		}
		if (parent instanceof AlgoPointInRegion) {
			RegionParameters region = ((GeoPoint) geo).getRegionParameters();
			return "region:" + Double.toHexString(region.getT1()) + ","
					+ Double.toHexString(region.getT2()) + "," + region.isOnPath();
		}
		return null;
	}

	/**
	 * @return number literals of an expression tree, in child order
	 */
	static List<MyDouble> literals(ExpressionValue root) {
		if (root == null) {
			return Collections.emptyList();
		}
		List<MyDouble> literals = new ArrayList<>();
		collect(root, literals, 0);
		return literals;
	}

	/**
	 * Tests whether two expression trees have the same shape, ignoring representation only:
	 * wrapper nodes without an operation and the subclass of a number literal.
	 *
	 * @return whether both trees have the same operations, element references and literal
	 *         positions
	 */
	static boolean sameShape(ExpressionValue live, ExpressionValue copy) {
		ExpressionValue left = live == null ? null : live.unwrap();
		ExpressionValue right = copy == null ? null : copy.unwrap();
		if (left == null || right == null) {
			return left == right;
		}
		if (left instanceof MyDouble || right instanceof MyDouble) {
			return left instanceof MyDouble && right instanceof MyDouble;
		}
		if (left instanceof GeoElement || right instanceof GeoElement) {
			return left instanceof GeoElement && right instanceof GeoElement
					&& Objects.equals(((GeoElement) left).getLabelSimple(),
							((GeoElement) right).getLabelSimple());
		}
		if (left.getClass() != right.getClass()
				|| left instanceof ExpressionNode && ((ExpressionNode) left).getOperation()
						!= ((ExpressionNode) right).getOperation()
				|| left.getChildCount() != right.getChildCount()) {
			return false;
		}
		for (int index = 0; index < left.getChildCount(); index++) {
			if (!sameShape(left.getChild(index), right.getChild(index))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The elements of the slice whose values depend on the driver: the driver roots and
	 * every element computed from them. The value of a locus output does not depend on the
	 * driver position, so elements derived from one are classified by their other inputs.
	 *
	 * @param ordered slice elements in construction order
	 * @return identity set of the driver-dependent elements
	 */
	static Set<GeoElement> driverDependent(List<GeoElement> ordered,
			Collection<GeoElement> driverRoots) {
		Set<GeoElement> dependent = Collections.newSetFromMap(new IdentityHashMap<>());
		for (GeoElement root : driverRoots) {
			if (root != null) {
				dependent.add(root);
			}
		}
		for (GeoElement geo : ordered) {
			AlgoElement parent = geo.getParentAlgorithm();
			if (!dependent.contains(geo) && parent != null && !isLocusOutput(geo)
					&& readsAny(parent, dependent)) {
				dependent.add(geo);
			}
		}
		return dependent;
	}

	/** @return whether the element is the output of a locus */
	static boolean isLocusOutput(GeoElement geo) {
		return geo instanceof GeoLocusV2 || geo instanceof GeoLocusND;
	}

	/**
	 * Visits the unlabelled inputs of an algorithm, inline literals and inline expressions,
	 * with the inputs of those expressions, down to a bounded depth. A command keeps the
	 * order of its arguments through serialization; an algorithm defined by an expression
	 * collects its inputs in an order that a reconstruction need not keep, so its unlabelled
	 * inputs are visited in the order of its expression tree.
	 *
	 * @return unlabelled inputs with their input path
	 */
	static List<InlineInput> inlineInputs(AlgoElement algorithm) {
		List<InlineInput> inputs = new ArrayList<>();
		collectInline(algorithm, "", inputs, 0);
		return inputs;
	}

	/** One unlabelled input of an algorithm and its path of input indices. */
	static final class InlineInput {
		final String path;
		final GeoElement geo;

		private InlineInput(String path, GeoElement geo) {
			this.path = path;
			this.geo = geo;
		}
	}

	private static void collectInline(AlgoElement algorithm, String prefix,
			List<InlineInput> inputs, int depth) {
		if (depth > MAXIMUM_INLINE_DEPTH) {
			return;
		}
		List<GeoElement> ordered = new ArrayList<>();
		GeoElement output = algorithm.getOutputLength() > 0 ? algorithm.getOutput(0) : null;
		if (output != null && output.getDefinition() != null) {
			collectReferences(output.getDefinition(), ordered, 0);
		} else if (algorithm.getInput() != null) {
			ordered.addAll(java.util.Arrays.asList(algorithm.getInput()));
		}
		int index = 0;
		for (GeoElement geo : ordered) {
			if (geo == null || geo.isLabelSet()) {
				continue;
			}
			String path = prefix + index++;
			inputs.add(new InlineInput(path, geo));
			AlgoElement parent = geo.getParentAlgorithm();
			if (parent != null) {
				collectInline(parent, path + ".", inputs, depth + 1);
			}
		}
	}

	private static void collectReferences(ExpressionValue value, List<GeoElement> references,
			int depth) {
		ExpressionValue node = value == null ? null : value.unwrap();
		if (node instanceof GeoElement) {
			references.add((GeoElement) node);
			return;
		}
		if (node == null || node instanceof MyDouble || depth > 256) {
			return;
		}
		for (int child = 0; child < node.getChildCount(); child++) {
			collectReferences(node.getChild(child), references, depth + 1);
		}
	}

	private static void collect(ExpressionValue value, List<MyDouble> literals, int depth) {
		ExpressionValue node = value == null ? null : value.unwrap();
		if (node instanceof MyDouble) {
			literals.add((MyDouble) node);
			return;
		}
		if (node == null || node instanceof GeoElement || depth > 256) {
			return;
		}
		for (int index = 0; index < node.getChildCount(); index++) {
			collect(node.getChild(index), literals, depth + 1);
		}
	}

	private static boolean readsAny(AlgoElement algorithm, Set<GeoElement> dependent) {
		GeoElement[] inputs = algorithm.getInput();
		if (inputs == null) {
			return false;
		}
		for (GeoElement input : inputs) {
			if (input != null && dependent.contains(input)) {
				return true;
			}
		}
		return false;
	}
}
