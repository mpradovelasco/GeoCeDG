/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.NativePathState2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.layout.DockManagerD;
import org.geogebra.desktop.main.AppD;

/**
 * One immutable geometric request across Desktop callbacks and kernel publication.
 * View/object references and context tickets are ephemeral liveness witnesses,
 * never durable identities or geometric selection evidence.
 */
final class GeoCeDGPointRequest {

	static final class StaleContext extends IllegalStateException {
		private static final long serialVersionUID = 1L;
		StaleContext() { super("STALE_SELECTION"); }
	}

	private final AppD app;
	private final SpatialIdentityRegistry document;
	private final Object view;
	private final DockManagerD viewActivationOwner;
	private final long viewActivationGeneration;
	private final int mode;
	private final double x;
	private final double y;
	private final LocusPointInteractionPolicy2D policy;
	private final double[] viewTransform;
	private final List<SourceState> sourceStates;
	private final SourceState selectedSource;
	private final GeoPoint reference;
	private final BooleanSupplier contextCurrent;

	private GeoCeDGPointRequest(AppD app, Object view, double x, double y,
			LocusPointInteractionPolicy2D policy, List<? extends GeoElement> sources) {
		this.app = Objects.requireNonNull(app);
		this.view = Objects.requireNonNull(view);
		viewActivationOwner = activationOwner(app);
		viewActivationGeneration = viewActivationOwner == null ? 0
				: viewActivationOwner.getEuclidianViewActivationGeneration();
		if (!Double.isFinite(x) || !Double.isFinite(y)) {
			throw new IllegalArgumentException("INVALID_REFERENCE");
		}
		this.x = x;
		this.y = y;
		this.policy = Objects.requireNonNull(policy);
		document = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		mode = app.getMode();
		viewTransform = view instanceof EuclidianView euclidian
				? new double[] {euclidian.getXZero(), euclidian.getYZero(),
						euclidian.getXscale(), euclidian.getYscale()}
				: new double[0];
		sourceStates = sources.stream().map(source -> new SourceState(source, document)).toList();
		selectedSource = null;
		reference = null;
		contextCurrent = () -> true;
		requireCurrent();
	}

	private GeoCeDGPointRequest(GeoCeDGPointRequest original, SourceState selected,
			GeoPoint reference, BooleanSupplier current) {
		app = original.app;
		document = original.document;
		view = original.view;
		viewActivationOwner = original.viewActivationOwner;
		viewActivationGeneration = original.viewActivationGeneration;
		mode = original.mode;
		x = original.x;
		y = original.y;
		policy = original.policy;
		viewTransform = original.viewTransform;
		sourceStates = original.sourceStates;
		selectedSource = selected;
		this.reference = reference;
		contextCurrent = current;
	}

	static GeoCeDGPointRequest capture(AppD app, Object view, double x, double y,
			LocusPointInteractionPolicy2D policy, List<? extends GeoElement> sources) {
		return new GeoCeDGPointRequest(app, view, x, y, policy, sources);
	}

	static GeoCeDGPointRequest forSource(AppD app, GeoElement source, double x, double y,
			LocusPointInteractionPolicy2D policy) {
		return capture(app, app.getActiveEuclidianView(), x, y, policy, List.of(source))
				.select(source);
	}

	GeoCeDGPointRequest select(GeoElement source) {
		requireCurrent();
		SourceState state = sourceStates.stream().filter(value -> value.source == source)
				.findFirst().orElseThrow(StaleContext::new);
		if (!state.supports()) { throw new IllegalArgumentException("UNSUPPORTED_CAPABILITY"); }
		var selected = new GeoCeDGPointRequest(this, state, reference, contextCurrent);
		selected.requireCurrent();
		return selected;
	}

	GeoCeDGPointRequest withReference(GeoPoint point) {
		var result = new GeoCeDGPointRequest(this, selectedSource,
				Objects.requireNonNull(point), contextCurrent);
		result.requireCurrent();
		return result;
	}

	GeoCeDGPointRequest withContext(BooleanSupplier current) {
		Objects.requireNonNull(current);
		var result = new GeoCeDGPointRequest(this, selectedSource, reference,
				() -> contextCurrent.getAsBoolean() && current.getAsBoolean());
		result.requireCurrent();
		return result;
	}

	void requireCurrent() {
		if (!isCurrent()) { throw new StaleContext(); }
	}

	void requireSource(GeoElement source) {
		requireCurrent();
		if (selectedSource == null || selectedSource.source != source) {
			throw new StaleContext();
		}
	}

	boolean isCurrent() {
		var construction = app.getKernel().getConstruction();
		return app.getActiveEuclidianView() == view && app.getMode() == mode
				&& activationOwner(app) == viewActivationOwner
				&& viewActivationGeneration != Long.MAX_VALUE
				&& (viewActivationOwner == null || viewActivationOwner
						.getEuclidianViewActivationGeneration() == viewActivationGeneration)
				&& construction.getSpatialIdentityRegistry() == document
				&& !construction.isFileLoading() && contextCurrent.getAsBoolean()
				&& (selectedSource == null || selectedSource.source.getConstruction() == construction
						&& selectedSource.matches(document))
				&& (reference == null || reference.getConstruction() == construction
						&& construction.getGeoSetConstructionOrder().contains(reference)
						&& reference.isDefined() && !reference.isGeoElement3D()
						&& same(reference.getInhomX(), x) && same(reference.getInhomY(), y));
	}

	double getX() { return x; }
	double getY() { return y; }
	double getRadius() { return policy.getMaximumWorldDistance(); }
	LocusPointInteractionPolicy2D getPolicy() { return policy; }
	GeoElement getSource() { return selectedSource == null ? null : selectedSource.source; }
	GeoPoint getReference() { return reference; }
	double[] getCapturedViewTransform() { return viewTransform.clone(); }

	private static DockManagerD activationOwner(AppD app) {
		return app.getGuiManager() instanceof GuiManagerD gui && gui.getLayout() != null
				? gui.getLayout().getDockManager() : null;
	}

	private static boolean same(double first, double second) {
		return Double.isFinite(first) && Double.doubleToLongBits(first)
				== Double.doubleToLongBits(second);
	}

	private static final class SourceState {
		private final GeoElement source;
		private final Object algorithm;
		private final PersistentGeoId id;
		private final LocusDefinition2D definition;
		private final long revision;
		private final NativePathState2D nativeState;

		SourceState(GeoElement source, SpatialIdentityRegistry document) {
			this.source = Objects.requireNonNull(source);
			algorithm = source.getParentAlgorithm();
			id = document.getPersistentGeoId(source);
			if (source instanceof GeoLocusV2 locus) {
				definition = locus.getSemanticDefinition();
				revision = definition == null ? -1 : definition.getSemanticRevision();
				nativeState = null;
			} else {
				definition = null;
				revision = -1;
				nativeState = NativePathState2D.capture(source);
			}
		}

		boolean supports() { return source instanceof GeoLocusV2 ? definition != null : nativeState != null; }

		boolean matches(SpatialIdentityRegistry document) {
			if (!source.isDefined() || source.getParentAlgorithm() != algorithm
					|| !source.getConstruction().getGeoSetConstructionOrder().contains(source)
					|| id != null && (document.getGeo(id) != source
							|| !id.equals(document.getPersistentGeoId(source)))) {
				return false;
			}
			if (source instanceof GeoLocusV2 locus) {
				return definition != null && locus.getSemanticDefinition() == definition
						&& definition.getSemanticRevision() == revision
						&& Objects.equals(locus.getPersistentLocusId(), id);
			}
			return nativeState != null && nativeState.matches(source);
		}
	}
}
