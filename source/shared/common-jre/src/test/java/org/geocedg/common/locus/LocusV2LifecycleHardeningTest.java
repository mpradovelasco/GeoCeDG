/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.plugin.GeoClass;
import org.junit.jupiter.api.Test;

class LocusV2LifecycleHardeningTest extends BaseUnitTest {
	private static final String BRANCH_KEY = "lifecycle.sheet.main";

	@Test
	void equivalentRecomputeAndSignedZeroDoNotPublishSpuriousRevision() {
		Fixture fixture = fixture(1);
		long revision = fixture.outer().getSemanticRevision();
		fixture.source.setValue(-0.0);
		fixture.source.updateCascade();

		assertEquals(revision, fixture.outer().getSemanticRevision());
		assertEquals(0L, Double.doubleToLongBits(fixture.outer().evaluate(
				BRANCH_KEY, 0, LocusEvaluationSession2D.reference())
				.getPoint().getY()));
	}

	@Test
	void explicitlyUndefinedGeoRecoversOnEquivalentNormalDagRecompute() {
		Fixture fixture = fixture(1);
		GeoLocusV2 locus = fixture.outer();
		long revision = locus.getSemanticRevision();
		locus.setUndefined();
		assertFalse(locus.isDefined());

		fixture.source.updateCascade();
		assertTrue(locus.isDefined());
		assertEquals(revision, locus.getSemanticRevision());
	}

	@Test
	void copyAssignmentListAndSequenceSeamsFailExplicitly() {
		Fixture fixture = fixture(1);
		GeoLocusV2 locus = fixture.outer();
		assertThrows(UnsupportedOperationException.class, locus::copy);
		assertThrows(UnsupportedOperationException.class,
				() -> locus.copyInternal(getConstruction()));
		assertThrows(UnsupportedOperationException.class,
				() -> locus.set(locus));
		assertThrows(UnsupportedOperationException.class, locus::deepCopyGeo);
	}

	@Test
	void labelsSelectionAndGenericConstructionDefaultsRemainDiagnosticOnly() {
		Fixture fixture = fixture(1);
		GeoLocusV2 locus = fixture.outer();
		locus.setLabel("experimentalLocusV2");
		getApp().getSelectionManager().addSelectedGeo(locus);

		assertEquals("experimentalLocusV2", locus.getLabelSimple());
		assertTrue(getApp().getSelectionManager().containsSelectedGeo(locus));
		assertEquals(ConstructionDefaults.DEFAULT_LINE,
				getConstruction().getConstructionDefaults().getDefaultType(locus));
		locus.setConstructionDefaults();
		assertEquals(GeoClass.LOCUS_V2, locus.getGeoClassType());
	}

	@Test
	void undefinedNestedChainRecoversThroughNormalDagWithoutStaleSnapshots() {
		Fixture fixture = fixture(3);
		long[] before = revisions(fixture.loci);
		fixture.source.setUndefined();
		fixture.source.updateCascade();
		for (int index = 0; index < fixture.loci.size(); index++) {
			assertFalse(fixture.loci.get(index).isDefined());
			assertEquals(before[index] + 1,
					fixture.loci.get(index).getSemanticRevision());
		}

		fixture.source.setValue(2);
		fixture.source.updateCascade();
		for (int index = 0; index < fixture.loci.size(); index++) {
			assertTrue(fixture.loci.get(index).isDefined());
			assertEquals(before[index] + 2,
					fixture.loci.get(index).getSemanticRevision());
		}
		assertEquals(new LocusPoint2D(0, 2), fixture.outer().evaluate(BRANCH_KEY,
				0, LocusEvaluationSession2D.reference()).getPoint());
	}

	@Test
	void deletingInnermostInputRemovesTheNormalDagChain() {
		Fixture fixture = fixture(3);
		for (GeoLocusV2 locus : fixture.loci) {
			assertTrue(getConstruction().getAlgoList()
					.contains(locus.getParentAlgorithm()));
		}

		fixture.source.remove();
		for (GeoLocusV2 locus : fixture.loci) {
			assertFalse(getConstruction().getAlgoList()
					.contains(locus.getParentAlgorithm()));
		}
	}

	@Test
	void noXmlMeansNoUndoRestoreContractYet() {
		Fixture fixture = fixture(1);
		fixture.outer().setLabel("notPersistedLocusV2");
		assertEquals("", fixture.outer().getXML());
		assertFalse(getApp().getXML().contains("notPersistedLocusV2"));
		assertFalse(getApp().getXML().contains("locusv2"));
	}

	private Fixture fixture(int depth) {
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D("lifecycle-parameter/v1",
						new LocusInterval2D(-1, 1, true, true),
						Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(BRANCH_KEY,
				provider, "g6r-lifecycle/v1", EnumSet.of(BranchProperty.FINITE));
		List<LocusBranch2D> branches = Collections.singletonList(branch);
		GeoNumeric source = new GeoNumeric(getConstruction(), 0);
		List<GeoLocusV2> loci = new ArrayList<>();
		GeoLocusV2 current = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "lifecycle-L1-" + source.hashCode(), source,
				provider, branches,
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter, value + parameter),
				"g6r-lifecycle-leaf/v1");
		loci.add(current);
		for (int level = 2; level <= depth; level++) {
			current = LocusV2Factory.createNested(LocusV2Mode.V2,
					getConstruction(), "lifecycle-L" + level + "-"
							+ source.hashCode(), current, BRANCH_KEY, provider, branches,
					parameter -> parameter,
					(parameter, upstream) -> upstream,
					"g6r-lifecycle-nested-" + level + "/v1");
			loci.add(current);
		}
		return new Fixture(source, loci);
	}

	private static long[] revisions(List<GeoLocusV2> loci) {
		long[] revisions = new long[loci.size()];
		for (int index = 0; index < loci.size(); index++) {
			revisions[index] = loci.get(index).getSemanticRevision();
		}
		return revisions;
	}

	private static final class Fixture {
		private final GeoNumeric source;
		private final List<GeoLocusV2> loci;

		Fixture(GeoNumeric source, List<GeoLocusV2> loci) {
			this.source = source;
			this.loci = loci;
		}

		GeoLocusV2 outer() {
			return loci.get(loci.size() - 1);
		}
	}
}
