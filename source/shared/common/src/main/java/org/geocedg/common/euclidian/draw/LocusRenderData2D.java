/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusPoint2D;

/** Derived and disposable render vertices; never a semantic locus API. */
public final class LocusRenderData2D {
	/** One world-coordinate vertex plus a presentation subpath marker. */
	public static final class Vertex {
		private final LocusPoint2D point;
		private final double semanticParameter;
		private final boolean startsSubpath;

		Vertex(LocusPoint2D point, double semanticParameter,
				boolean startsSubpath) {
			this.point = point;
			this.semanticParameter = semanticParameter;
			this.startsSubpath = startsSubpath;
		}

		public LocusPoint2D getPoint() {
			return point;
		}

		/** @return render provenance only; never a path or metric authority */
		public double getSemanticParameter() {
			return semanticParameter;
		}

		/** @return whether this vertex starts a disconnected render subpath */
		public boolean startsSubpath() {
			return startsSubpath;
		}
	}

	private final long semanticRevision;
	private final LocusRenderPolicy2D policy;
	private final List<Vertex> vertices;

	LocusRenderData2D(long semanticRevision, LocusRenderPolicy2D policy,
			List<Vertex> vertices) {
		this.semanticRevision = semanticRevision;
		this.policy = policy;
		this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
	}

	public long getSemanticRevision() {
		return semanticRevision;
	}

	public LocusRenderPolicy2D getPolicy() {
		return policy;
	}

	public List<Vertex> getVertices() {
		return vertices;
	}
}
