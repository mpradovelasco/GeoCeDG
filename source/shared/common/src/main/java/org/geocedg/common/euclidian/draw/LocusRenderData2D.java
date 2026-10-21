/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusPoint2D;

/**
 * Derived and disposable render vertices; never a semantic locus API.
 *
 * <p>PRE-G9B-CU2 ({@code locus-v2-presentation.md}, "PRE-G9B-CU2 amendment (version 2)"):
 * the drawing declares its effective fidelity per component, with the typed marks of what it
 * could not represent as the certificate states it, and the point components it declares
 * without drawing them. Marks and fidelity are representation facts only.</p>
 */
public final class LocusRenderData2D {
	/** Certified exemption of the turning criterion at a vertex. */
	public enum TurnExemption {
		/** No exemption: the turn at the vertex is bounded by the policy. */
		NONE,
		/** The vertex lies in the isolation band of a certified fold. */
		FOLD_BAND,
		/** The vertex is the exact image of a certified singular parameter. */
		SINGULAR_VERTEX
	}

	/** How the point of a vertex was obtained. */
	public enum VertexSource {
		/** Evaluation of a canonical parameter. */
		EVALUATED,
		/** One-sided limit at an excluded structural-event end. */
		ONE_SIDED_LIMIT,
		/** Limit form of the certificate at a root of the component. */
		LIMIT_FORM
	}

	/** Typed record of what a drawing could not represent as the certificate states it. */
	public enum MarkKind {
		/**
		 * The evaluation failed computationally inside a certified component, up to the
		 * resolution of the parameter: the path is cut, never interpolated.
		 */
		COMPUTATIONAL_GAP,
		/**
		 * The evaluator does not define the point inside a component whose existence is not
		 * proven: the path is cut, never interpolated.
		 */
		EVALUATION_GAP,
		/**
		 * An end of the component, its one-sided limit or its limit form could not be
		 * evaluated: the path ends at the last evaluated vertex.
		 */
		END_NOT_EVALUATED,
		/** An excluded end without a limit is approached from inside, never reached. */
		OPEN_END_APPROACHED,
		/** A turn above the policy outside the exemptions, at the resolution of the parameter. */
		UNCERTIFIED_TURN,
		/** A chord deviation above the policy at the resolution of the parameter. */
		DEVIATION_AT_RESOLUTION,
		/** The limit form of a certified singular parameter is not available. */
		SINGULAR_VERTEX_NOT_EXACT,
		/** The drawing is coarser than the policy to fit the vertex budget. */
		BUDGET_COARSENED,
		/** The component does not fit the vertex budget even at its coarsest; not drawn. */
		VERTEX_BUDGET_EXHAUSTED,
		/** A point component is declared and never drawn (D-CU2-19). */
		POINT_COMPONENT_NOT_DRAWN
	}

	/** Effective fidelity of the drawing of one component. */
	public enum Fidelity {
		/** Every span meets the policy and every end was evaluated. */
		COMPLETE,
		/** Drawn, with the causes its marks enumerate. */
		PARTIAL,
		/** Not drawn, with its cause. */
		FAILED
	}

	/** One world-coordinate vertex plus a presentation subpath marker. */
	public static final class Vertex {
		private final LocusPoint2D point;
		private final double semanticParameter;
		private final boolean startsSubpath;
		private final TurnExemption exemption;
		private final VertexSource source;

		Vertex(LocusPoint2D point, double semanticParameter,
				boolean startsSubpath) {
			this(point, semanticParameter, startsSubpath, TurnExemption.NONE,
					VertexSource.EVALUATED);
		}

		Vertex(LocusPoint2D point, double semanticParameter, boolean startsSubpath,
				TurnExemption exemption, VertexSource source) {
			this.point = point;
			this.semanticParameter = semanticParameter;
			this.startsSubpath = startsSubpath;
			this.exemption = Objects.requireNonNull(exemption);
			this.source = Objects.requireNonNull(source);
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

		/** @return certified exemption of the turning criterion at this vertex */
		public TurnExemption getTurnExemption() {
			return exemption;
		}

		/** @return how the point of this vertex was obtained */
		public VertexSource getSource() {
			return source;
		}
	}

	/** One typed mark of a component, or of a point component. */
	public static final class Mark {
		private final MarkKind kind;
		private final String componentKey;
		private final double parameter;
		private final String detail;

		Mark(MarkKind kind, String componentKey, double parameter, String detail) {
			this.kind = Objects.requireNonNull(kind);
			this.componentKey = Objects.requireNonNull(componentKey);
			this.parameter = parameter;
			this.detail = detail == null ? "" : detail;
		}

		/** @return kind of the mark */
		public MarkKind getKind() {
			return kind;
		}

		/** @return branch key and revision-local evidence key of the component */
		public String getComponentKey() {
			return componentKey;
		}

		/** @return render parameter of the mark, or {@code NaN} for the whole component */
		public double getParameter() {
			return parameter;
		}

		/** @return deterministic detail, such as the evaluation diagnostic */
		public String getDetail() {
			return detail;
		}

		@Override
		public String toString() {
			return kind + "@" + componentKey + ":" + parameter
					+ (detail.isEmpty() ? "" : ":" + detail);
		}
	}

	/** Effective fidelity of one component and its causes. */
	public static final class ComponentFidelity {
		private final String componentKey;
		private final Fidelity fidelity;
		private final List<MarkKind> causes;
		private final int vertices;

		ComponentFidelity(String componentKey, Fidelity fidelity, List<MarkKind> causes,
				int vertices) {
			this.componentKey = Objects.requireNonNull(componentKey);
			this.fidelity = Objects.requireNonNull(fidelity);
			this.causes = Collections.unmodifiableList(new ArrayList<>(causes));
			this.vertices = vertices;
		}

		/** @return branch key and revision-local evidence key of the component */
		public String getComponentKey() {
			return componentKey;
		}

		/** @return effective fidelity of the drawing of the component */
		public Fidelity getFidelity() {
			return fidelity;
		}

		/** @return distinct kinds of the marks that make the fidelity partial or failed */
		public List<MarkKind> getCauses() {
			return causes;
		}

		/** @return vertices drawn for the component */
		public int getVertices() {
			return vertices;
		}

		@Override
		public String toString() {
			return componentKey + ":" + fidelity + causes + ":" + vertices;
		}
	}

	private final long semanticRevision;
	private final LocusRenderPolicy2D policy;
	private final List<Vertex> vertices;
	private final List<Mark> marks;
	private final List<ComponentFidelity> components;
	private final int coarseningLevel;

	LocusRenderData2D(long semanticRevision, LocusRenderPolicy2D policy,
			List<Vertex> vertices) {
		this(semanticRevision, policy, vertices, Collections.emptyList(),
				Collections.emptyList(), 0);
	}

	LocusRenderData2D(long semanticRevision, LocusRenderPolicy2D policy,
			List<Vertex> vertices, List<Mark> marks, List<ComponentFidelity> components,
			int coarseningLevel) {
		this.semanticRevision = semanticRevision;
		this.policy = policy;
		this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
		this.marks = Collections.unmodifiableList(new ArrayList<>(marks));
		this.components = Collections.unmodifiableList(new ArrayList<>(components));
		this.coarseningLevel = coarseningLevel;
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

	/** @return typed marks in component order */
	public List<Mark> getMarks() {
		return marks;
	}

	/** @return effective fidelity of each certified component, in kernel order */
	public List<ComponentFidelity> getComponentFidelities() {
		return components;
	}

	/** @return coarsening level used to fit the vertex budget, {@code 0} for none */
	public int getCoarseningLevel() {
		return coarseningLevel;
	}

	/**
	 * @return worst fidelity of the drawing: failed when a component is not drawn, partial
	 *         with any mark, complete otherwise
	 */
	public Fidelity getFidelity() {
		Fidelity worst = marks.isEmpty() ? Fidelity.COMPLETE : Fidelity.PARTIAL;
		for (ComponentFidelity component : components) {
			if (component.getFidelity() == Fidelity.FAILED) {
				return Fidelity.FAILED;
			}
			if (component.getFidelity() == Fidelity.PARTIAL) {
				worst = Fidelity.PARTIAL;
			}
		}
		return worst;
	}
}
