/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.geocedg.common.kernel.locus.constructive.AuthorityPolicy;
import org.geogebra.common.kernel.algos.AlgoCirclePointRadius;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;

/** Versioned declarations of driver directions, independent of locus image coordinates. */
public final class ConstructiveLocusCuts2D {

	public static final String CONTRACT = "constructive-cuts/v1";

	private final GeoText version;
	private final List<GeoPoint> witnesses;

	public ConstructiveLocusCuts2D(GeoText version, List<GeoPoint> witnesses) {
		if (version.isLabelSet() || !version.isIndependent() || !CONTRACT.equals(version.getTextString())
				|| witnesses.isEmpty()) {
			throw new IllegalArgumentException("Invalid constructive cut declaration");
		}
		this.version = version;
		this.witnesses = List.copyOf(witnesses);
	}

	public List<GeoPoint> getWitnesses() {
		return witnesses;
	}

	/** The version is a command literal, and the witnesses are ordinary DAG inputs. */
	public List<GeoElement> getCommandArguments() {
		List<GeoElement> arguments = new ArrayList<>();
		arguments.add(version);
		arguments.addAll(witnesses);
		return arguments;
	}

	/** @return revision-scoped directions, or {@code null} if a witness has lost its premises */
	public List<LocusTraversalCut2D> resolve(GeoElement state, GeoElement support,
			SemanticGeneratorDescriptor1D descriptor, LocusDriverDomainProvider2D provider) {
		if (!(state instanceof GeoPoint driver)
				|| descriptor.getFamily() != SemanticGeneratorFamily1D.CIRCLE_POINT
				&& descriptor.getFamily() != SemanticGeneratorFamily1D.SEGMENT_POINT
				&& descriptor.getFamily() != SemanticGeneratorFamily1D.CIRCULAR_ARC_POINT) {
			return null;
		}
		Map<Double, List<String>> declarations = new TreeMap<>();
		for (GeoPoint witness : witnesses) {
			if (witness == driver || witness.getConstruction() != driver.getConstruction()
					|| witness.isChildOf(driver) || !witness.isDefined()) {
				return null;
			}
			var id = witness.getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(witness);
			double parameter = provider.canonicalize(parameter(witness, driver, support));
			if (id == null || !descriptor.getDeclaredDomain().contains(parameter, 0)) {
				return null;
			}
			declarations.computeIfAbsent(parameter, key -> new ArrayList<>()).add(id.toExternalForm());
		}
		return declarations.entrySet().stream().map(entry ->
				new LocusTraversalCut2D(entry.getKey(), entry.getValue())).toList();
	}

	private static double parameter(GeoPoint witness, GeoPoint driver, GeoElement support) {
		if (witness.getParentAlgorithm() instanceof AlgoPointOnPath point
				&& point.getP() == witness && point.getPath() == driver.getPath()
				&& witness.getPath() == driver.getPath()) {
			for (GeoElement input : point.getInput()) {
				if (!input.isDefined()) {
					return Double.NaN;
				}
			}
			return witness.getPathParameter().getT();
		}
		if (support instanceof GeoConic circle
				&& !AuthorityPolicy.of(circle.getConstruction()).isConstructive()) {
			return horizontalDiameterDirection(witness, circle);
		}
		return Double.NaN;
	}

	/** Reads the selected root of a proved horizontal diameter, including the cone witness K. */
	private static double horizontalDiameterDirection(GeoPoint witness, GeoConic circle) {
		if (!(witness.getParentAlgorithm() instanceof AlgoIntersectLineConic intersection)
				|| intersection.getConic() != circle
				|| !(circle.getParentAlgorithm() instanceof AlgoCirclePointRadius definedCircle)
				|| intersection.isSelectionFirstIntersectionPending()
				|| intersection.isSelectionPermutationPending()
				|| intersection.getSelectionExistingIntersection() != null) {
			return Double.NaN;
		}
		GeoLine diameter = intersection.getLine();
		if (diameter.getClass() != GeoLine.class
				|| !(diameter.getParentAlgorithm() instanceof AlgoLinePointLine throughCenter)
				|| throughCenter.getInput(0) != definedCircle.getCenter()
				|| diameter.getX() != 0 || diameter.getY() == 0
				|| circle.getEigenvec(0).getY() != 0 || Math.abs(circle.getEigenvec(0).getX()) != 1
				|| !(circle.getHalfAxis(0) > 0)) {
			return Double.NaN;
		}
		GeoPoint[] outputs = intersection.getIntersectionPoints();
		int index = outputs[0] == witness ? 0 : outputs[1] == witness ? 1 : -1;
		int[] permutation = intersection.getSelectionPermutation();
		if (index < 0 || permutation == null || permutation[index] < 0 || permutation[index] > 1) {
			return Double.NaN;
		}
		// Raw roots are ordered along (line.y, -line.x). The center and positive radius
		// guarantee distinct roots of opposite direction in the provider's local axes.
		boolean positive = (permutation[index] == 1)
				== (Math.copySign(1, diameter.getY()) == circle.getEigenvec(0).getX());
		return positive ? 0 : -Math.PI;
	}
}
