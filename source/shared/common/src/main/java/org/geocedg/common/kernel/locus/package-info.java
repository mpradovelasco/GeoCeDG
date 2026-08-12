/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

/**
 * Internal two-dimensional Locus V2 semantic contracts. A definition owns a
 * versioned driver domain, stable branches, an immutable semantic revision and
 * a viewport-independent evaluator. The package deliberately exposes neither a
 * public command nor GeoGebra {@code Path}, XML, metric, intersection or 3D
 * contracts.
 *
 * <p>Instances are confined to the existing kernel thread. A
 * {@link org.geocedg.common.kernel.locus.LocusEvaluationSession2D} is a bounded,
 * disposable evaluation context, not a dependency graph.</p>
 */
package org.geocedg.common.kernel.locus;
