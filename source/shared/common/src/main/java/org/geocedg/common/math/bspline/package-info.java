/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

/**
 * Pure, stateless 2D B-spline mathematics shared by the {@code bspline-2d} family and the
 * {@code dxf-spline-reference/v1} import (specification {@code geocedg/specs/curves/bspline-2d.md}
 * section 12; ADR 0043).
 *
 * <p>This package is <b>not</b> a geometric authority. It imports no construction, kernel,
 * view or Desktop type, holds no global state and no cache, and never reads a viewport.
 * Authority comes from the family contract in the kernel for constructions and from nothing
 * for imported references.</p>
 *
 * <h2>Contents</h2>
 * <ul>
 * <li>{@link org.geocedg.common.math.bspline.BSplineData}: validated immutable data (degree,
 * clamped knot vector, control points, implicit unit or explicit weights) with exact
 * structural facts: domain, non-degenerate spans, knot ownership, interior multiplicities,
 * structural continuity, exact closure and weight facts.</li>
 * <li>{@link org.geocedg.common.math.bspline.BSplineEvaluation}: homogeneous de Boor point
 * evaluation and first derivatives (hodograph de Boor for polynomial data, quotient rule
 * for rational data).</li>
 * <li>{@link org.geocedg.common.math.bspline.BSplineKnotInsertion}: Boehm knot
 * insertion.</li>
 * <li>{@link org.geocedg.common.math.bspline.BSplineSpanForms}: span Bernstein form, span
 * local power form and correctly rounded global power coefficients.</li>
 * <li>{@link org.geocedg.common.math.bspline.BSplineCanonicalEncoding}: canonical encoding
 * and SHA-256 definition digest.</li>
 * </ul>
 *
 * <h2>Numerical policy (specification section 11)</h2>
 *
 * <p>Every operation is binary64 with a fixed, documented operation order, without
 * {@code Math.fma}, without parallelism and without dependence on view, zoom, DPI,
 * tessellation or time. Structural facts (multiplicities, clamping, continuity orders,
 * exact closure, equality of weights) are decided by exact binary64 comparison, never with
 * a tolerance. Results of evaluation are floating-point values; they are declared
 * numerical and certify nothing.</p>
 *
 * <h2>Work limits</h2>
 *
 * <p>Every operation is bounded by the size of its data. With degree {@code p},
 * {@code n + 1} control points and {@code m + 1 = n + p + 2} knots: validation is
 * {@code O(n + p)} comparisons; span lookup is a binary search over at most {@code n - p + 1}
 * spans; a point or first derivative costs {@code p (p + 1) / 2} interpolation steps (each one
 * division, three subtractions and two multiplications plus one addition per coordinate);
 * one knot insertion is {@code O(n + p)}; a span Bernstein form needs at most
 * {@code 2 (p - 1)} local insertions of {@code O(p)} each; the local power transform is
 * {@code O(p^2)} per coordinate; global power coefficients use {@code O(p^2)} exact rational
 * operations on numbers of at most a few thousand bits per factor of {@code p}; the canonical
 * encoding has {@code 8 (2 + m + 1 + 2 (n + 1) + w) + |id| + 2} bytes. The caller supplies
 * the admissible degree and size ({@link org.geocedg.common.math.bspline.BSplineLimits});
 * the library additionally enforces absolute caps
 * ({@link org.geocedg.common.math.bspline.BSplineLimits#ABSOLUTE_MAX_DEGREE},
 * {@link org.geocedg.common.math.bspline.BSplineLimits#ABSOLUTE_MAX_CONTROL_POINTS}).</p>
 */
package org.geocedg.common.math.bspline;
