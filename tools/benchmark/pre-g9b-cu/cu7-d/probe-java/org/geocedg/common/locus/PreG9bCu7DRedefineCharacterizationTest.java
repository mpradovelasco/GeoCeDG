/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * CU7-D observational probe of existing APIs, not acceptance of M08 or M09.
 * Each row starts from a new construction. Errors are deliberately observable;
 * no new redefine mode or product capability is supplied by this test.
 */
class PreG9bCu7DRedefineCharacterizationTest extends G9U0PublicSurfaceTestBase {
	private boolean constructivePolicy;

	@Test
	void characterizeOriginalK5WithMaterializedResult() {
		for (boolean constructive : new boolean[] {false, true}) {
			for (boolean enrollSupport : new boolean[] {false, true}) {
				for (String target : new String[] {"N", "L"}) {
					fixture(constructive, true);
					if (enrollSupport) {
						add("K=LocusV2(X2,N2)");
					}
					add("A=(1.99,0)");
					add("g=Line(A,Line((0,0),(0,1)))");
					GeoLocusIntersectionResult rich = add("R=Intersect(L,g)");
					var root = rich.getIntersectionResult().getFiniteSolutions().stream()
							.filter(candidate -> rich.findExactPointAdmissibleSolution(
									candidate.getIdentity().getRootToken()).isPresent())
							.findFirst().orElseThrow();
					add("Claim=Intersect(R,\"" + root.getIdentity().getRootToken() + "\")");
					System.out.println("CU7D-CONTEXT|originalK5=true|enrollSupport="
							+ enrollSupport);
					observe(target, "N".equals(target) ? "Point(c3)"
							: "LocusV2(X2,N2)", true, "N".equals(target)
									? SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE
									: SpatialRedefineExecutionMode.ADVANCED_RETAIN);
				}
			}
		}
	}

	@Test
	void characterizeDriverAndSourceCalls() {
		for (boolean constructive : new boolean[] {false, true}) {
			for (boolean hostOptions : new boolean[] {false, true}) {
				for (String target : new String[] {"N", "L"}) {
					fixture(constructive);
					observe(target, "N".equals(target) ? "Point(c3)"
							: "LocusV2(X2,N2)", hostOptions,
							SpatialRedefineExecutionMode.ADVANCED_RETAIN);
				}
			}
			fixture(constructive);
			observe("N", "Point(c3)", true,
					SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE);
		}
	}

	@Test
	void characterizeNoncoincidentPointRedefine() {
		for (boolean durable : new boolean[] {false, true}) {
			for (SpatialRedefineExecutionMode mode : SpatialRedefineExecutionMode.values()) {
				fixture(false);
				if (durable) {
					add("P=Point(L,\"" + BRANCH + "\",1)");
				} else {
					add("P=(10,10)");
				}
				GeoLocusIntersectionResult result = add("R=Intersect(L,xAxis)");
				var solution = result.getIntersectionResult().getFiniteSolutions().stream()
						.filter(candidate -> result.findExactPointAdmissibleSolution(
								candidate.getIdentity().getRootToken()).isPresent())
						.findFirst().orElseThrow();
				observe("P", "Intersect(R,\"" + solution.getIdentity().getRootToken()
						+ "\")", true, mode);
			}
		}
	}

	private void fixture(boolean constructive) {
		fixture(constructive, false);
	}

	private void fixture(boolean constructive, boolean projected) {
		constructivePolicy = constructive;
		getKernel().clearConstruction(true);
		add("c=Circle((0,0),2)");
		add("c3=Circle((0,0),2)");
		add("N=Point(c)");
		add("N2=Point(c3)");
		add(projected ? "X0=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))"
				: "X0=N+(0,0)");
		add(projected ? "X2=Intersect(Line(N2,Line((0,0),(0,1))),Line((0,3),(1,3)))"
				: "X2=N2+(0,0)");
		add("L=LocusV2(X0,N)");
		if (constructive) {
			AuthorityPolicyAdoption.adopt(getConstruction());
		}
	}

	private void observe(String label, String definition, boolean hostOptions,
			SpatialRedefineExecutionMode mode) {
		GeoElement old = getKernel().lookupLabel(label);
		assertNotNull(old);
		var registry = getConstruction().getSpatialIdentityRegistry();
		var oldId = registry.getPersistentGeoId(old);
		GeoElement oldClaim = getKernel().lookupLabel("Claim");
		var claimId = oldClaim == null ? null : registry.getPersistentGeoId(oldClaim);
		var rich = (GeoLocusIntersectionResult) getKernel().lookupLabel("R");
		var richId = rich == null ? null : registry.getPersistentGeoId(rich);
		String oldToken = oldClaim == null ? null
				: ((AlgoLocusIntersectionPointV2) oldClaim.getParentAlgorithm())
						.getEffectiveRootToken();
		String before = old.getDefinitionForInputBar();
		String beforeXml = getApp().getXML();
		EvalInfo info = new EvalInfo(true, true);
		if (hostOptions) {
			info = info.withSymbolicMode(AlgebraProcessor.getRedefinitionMode(old,
					getKernel())).withLabelRedefinitionAllowedFor(label)
					.withSymbolic(true).withSliders(true);
		}
		info = info.withSpatialRedefineExecutionMode(mode);
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND> callback = new AtomicReference<>();
		AtomicReference<SpatialRedefineAssessment> assessment = new AtomicReference<>();
		getKernel().getAlgebraProcessor().setSpatialRedefineAssessmentHandler(value -> {
			assessment.set(value);
			// Observe classification; keep the caller's original requested mode.
			// The registry still rejects any unavailable operation.
			return mode;
		});
		String thrown = "";
		try {
			getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
					old, definition, info, false, callback::set, errors);
		} catch (RuntimeException exception) {
			thrown = exception.toString();
		} finally {
			getKernel().getAlgebraProcessor().setSpatialRedefineAssessmentHandler(null);
		}
		GeoElement after = getKernel().lookupLabel(label);
		assertNotNull(after);
		GeoElement currentClaim = claimId == null ? null : registry.getGeo(claimId);
		var currentRich = richId == null ? null
				: (GeoLocusIntersectionResult) registry.getGeo(richId);
		if (callback.get() == null) {
			assertEquals(beforeXml, getApp().getXML(), "Rejected probe must restore XML");
		}
		System.out.println("CU7D|target=" + label + "|hostOptions=" + hostOptions
				+ "|constructive=" + constructivePolicy
				+ "|mode=" + mode + "|durableBefore=" + (oldId != null)
				+ "|retained=" + (oldId != null
						&& oldId.equals(registry.getPersistentGeoId(after)))
				+ "|before=" + before + "|after=" + after.getDefinitionForInputBar()
				+ "|xmlUnchanged=" + beforeXml.equals(getApp().getXML())
				+ "|callback=" + (callback.get() != null)
				+ "|assessment=" + (assessment.get() == null ? "none"
						: assessment.get().getStatus() + ":" + assessment.get().getDetail())
				+ "|claimRetained=" + (currentClaim != null)
				+ "|claimDefined=" + (currentClaim != null && currentClaim.isDefined())
				+ "|oldTokenAdmissible=" + (oldToken != null && currentRich != null
						&& currentRich.findExactPointAdmissibleSolution(oldToken).isPresent())
				+ "|errors=" + errors.getErrors().replace('\n', ' ')
						.replaceAll("locus-root/v3/[^\\\"\\s]+", "TOKEN")
				+ "|thrown=" + thrown);
	}
}
