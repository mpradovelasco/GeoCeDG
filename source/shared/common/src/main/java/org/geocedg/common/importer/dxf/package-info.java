/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

/**
 * Read-only import of 2D DXF {@code SPLINE} entities as external references
 * ({@code dxf-spline-reference/v1}, specification
 * {@code geocedg/specs/import/dxf-spline-reference.md}, revision 2).
 *
 * <p>An import adapter outside the geometric kernel (AGENTS section 4): the reader parses
 * an ASCII DXF file and produces one typed outcome per candidate entity; an admitted entity
 * becomes an immutable {@link org.geocedg.common.importer.dxf.DxfSplineReference} with its
 * provenance, diagnostics and a render tessellation computed once in model units. A reference
 * is not a construction object: nothing here imports a construction, kernel, view or Desktop
 * type, and evaluation uses the pure library {@code org.geocedg.common.math.bspline}, which
 * gives the reference no CeDG semantics. The DXF file is only read.</p>
 */
package org.geocedg.common.importer.dxf;
