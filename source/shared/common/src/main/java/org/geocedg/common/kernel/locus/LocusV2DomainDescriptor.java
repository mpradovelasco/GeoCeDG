/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;

/**
 * Strict public-domain descriptor normalized from a command {@link GeoList}.
 *
 * <p>The accepted shape is {@code {periodic,{a,b,includeA,includeB},...}}.
 * Ordered endpoints carry orientation. A finite descriptor contains one or
 * more disjoint components; a periodic descriptor contains exactly one
 * non-degenerate half-open fundamental interval.
 */
public final class LocusV2DomainDescriptor {

	private final GeoList source;
	private final boolean periodic;
	private final Orientation orientation;
	private final LocusInterval2D declaredDomain;
	private final List<LocusInterval2D> validComponents;

	private LocusV2DomainDescriptor(GeoList source, boolean periodic,
			Orientation orientation, LocusInterval2D declaredDomain,
			List<LocusInterval2D> validComponents) {
		this.source = source;
		this.periodic = periodic;
		this.orientation = orientation;
		this.declaredDomain = declaredDomain;
		this.validComponents = Collections.unmodifiableList(validComponents);
	}

	/**
	 * Parses and normalizes a strict domain declaration without inference.
	 *
	 * @param source public GeoList declaration
	 * @return immutable normalized descriptor
	 * @throws IllegalArgumentException when shape or domain policy is invalid
	 */
	public static LocusV2DomainDescriptor parse(GeoList source) {
		if (source == null || !source.isDefined() || source.size() < 2
				|| !(source.get(0) instanceof GeoBoolean)
				|| !source.get(0).isDefined()) {
			throw invalid("expected {periodic,{a,b,includeA,includeB},...}");
		}
		boolean periodic = ((GeoBoolean) source.get(0)).getBoolean();
		if (periodic && source.size() != 2) {
			throw invalid("a periodic domain has exactly one fundamental interval");
		}

		ArrayList<OrderedComponent> ordered = new ArrayList<>();
		Orientation established = null;
		for (int index = 1; index < source.size(); index++) {
			GeoElement element = source.get(index);
			if (!(element instanceof GeoList) || !element.isDefined()) {
				throw invalid("every domain component must be a defined four-item list");
			}
			GeoList component = (GeoList) element;
			if (component.size() != 4
					|| !(component.get(0) instanceof GeoNumberValue)
					|| !(component.get(1) instanceof GeoNumberValue)
					|| !(component.get(2) instanceof GeoBoolean)
					|| !(component.get(3) instanceof GeoBoolean)
					|| !allDefined(component)) {
				throw invalid("component shape is {a,b,includeA,includeB}");
			}
			double a = ((GeoNumberValue) component.get(0)).getDouble();
			double b = ((GeoNumberValue) component.get(1)).getDouble();
			if (!Double.isFinite(a) || !Double.isFinite(b)) {
				throw invalid("domain endpoints must be finite");
			}
			boolean includeA = ((GeoBoolean) component.get(2)).getBoolean();
			boolean includeB = ((GeoBoolean) component.get(3)).getBoolean();
			Orientation componentOrientation = a <= b
					? Orientation.INCREASING : Orientation.DECREASING;
			if (a != b) {
				if (established != null && established != componentOrientation) {
					throw invalid("all non-degenerate components need one orientation");
				}
				established = componentOrientation;
			}
			LocusInterval2D interval = a <= b
					? new LocusInterval2D(a, b, includeA, includeB)
					: new LocusInterval2D(b, a, includeB, includeA);
			ordered.add(new OrderedComponent(a, interval));
		}

		Orientation orientation = established == null
				? Orientation.INCREASING : established;
		validateTraversalOrder(ordered, orientation);
		ArrayList<LocusInterval2D> components = new ArrayList<>();
		for (OrderedComponent component : ordered) {
			components.add(component.interval);
		}
		validateDisjoint(components);

		if (periodic) {
			LocusInterval2D interval = components.get(0);
			if (interval.getLower() == interval.getUpper()
					|| interval.isLowerClosed() == interval.isUpperClosed()) {
				throw invalid("a periodic fundamental interval must be nonzero and half-open");
			}
		}

		double lower = components.stream().map(LocusInterval2D::getLower)
				.min(Comparator.naturalOrder()).orElseThrow();
		double upper = components.stream().map(LocusInterval2D::getUpper)
				.max(Comparator.naturalOrder()).orElseThrow();
		boolean lowerClosed = components.stream()
				.filter(component -> component.getLower() == lower)
				.anyMatch(LocusInterval2D::isLowerClosed);
		boolean upperClosed = components.stream()
				.filter(component -> component.getUpper() == upper)
				.anyMatch(LocusInterval2D::isUpperClosed);
		LocusInterval2D declared = new LocusInterval2D(lower, upper,
				lowerClosed, upperClosed);
		return new LocusV2DomainDescriptor(source, periodic, orientation,
				declared, components);
	}

	public GeoList getSource() {
		return source;
	}

	public boolean isPeriodic() {
		return periodic;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public LocusInterval2D getDeclaredDomain() {
		return declaredDomain;
	}

	public List<LocusInterval2D> getValidComponents() {
		return validComponents;
	}

	private static boolean allDefined(GeoList component) {
		for (int index = 0; index < component.size(); index++) {
			if (!component.get(index).isDefined()) {
				return false;
			}
		}
		return true;
	}

	private static void validateTraversalOrder(List<OrderedComponent> components,
			Orientation orientation) {
		for (int index = 1; index < components.size(); index++) {
			OrderedComponent prior = components.get(index - 1);
			OrderedComponent current = components.get(index);
			if (orientation == Orientation.INCREASING
					? current.a < prior.a : current.a > prior.a) {
				throw invalid("components must follow their declared orientation");
			}
		}
	}

	private static void validateDisjoint(List<LocusInterval2D> components) {
		ArrayList<LocusInterval2D> sorted = new ArrayList<>(components);
		sorted.sort(Comparator.comparingDouble(LocusInterval2D::getLower));
		for (int index = 1; index < sorted.size(); index++) {
			LocusInterval2D prior = sorted.get(index - 1);
			LocusInterval2D current = sorted.get(index);
			if (current.getLower() < prior.getUpper()
					|| current.getLower() == prior.getUpper()
							&& current.isLowerClosed() && prior.isUpperClosed()) {
				throw invalid("finite components must be disjoint");
			}
		}
	}

	private static IllegalArgumentException invalid(String detail) {
		return new IllegalArgumentException("Invalid Locus V2 domain: " + detail);
	}

	private static final class OrderedComponent {
		private final double a;
		private final LocusInterval2D interval;

		private OrderedComponent(double a, LocusInterval2D interval) {
			this.a = a;
			this.interval = interval;
		}
	}
}
