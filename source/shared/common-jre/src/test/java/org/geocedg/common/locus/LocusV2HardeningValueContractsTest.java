/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusBranchSnapshot2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticKey2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSourceSnapshot2D;
import org.geocedg.common.kernel.locus.StablePathDomainProvider2D;
import org.junit.jupiter.api.Test;

class LocusV2HardeningValueContractsTest {
	private static final double EPS_DOMAIN = 1E-14;

	@Test
	void semanticKeysCanonicalizeSignedZeroAndRejectInvalidAddresses() {
		LocusSemanticKey2D positiveZero =
				new LocusSemanticKey2D("locus", 1, "branch", 0.0);
		LocusSemanticKey2D negativeZero =
				new LocusSemanticKey2D("locus", 1, "branch", -0.0);
		assertEquals(positiveZero, negativeZero);
		assertEquals(positiveZero.hashCode(), negativeZero.hashCode());

		assertThrows(IllegalArgumentException.class,
				() -> new LocusSemanticKey2D("", 1, "branch", 0));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusSemanticKey2D("locus", 0, "branch", 0));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusSemanticKey2D("locus", 1, "", 0));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusSemanticKey2D("locus", 1, "branch", Double.NaN));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusSemanticKey2D("locus", 1, "branch",
						Double.POSITIVE_INFINITY));
	}

	@Test
	void sourceSnapshotsAreDefensiveCanonicalValueObjects() {
		double[] source = {-0.0, 2};
		LocusSourceSnapshot2D snapshot = new LocusSourceSnapshot2D(source);
		source[1] = 9;
		LocusSourceSnapshot2D equivalent =
				new LocusSourceSnapshot2D(new double[] {0.0, 2});

		assertEquals(0L, Double.doubleToLongBits(snapshot.get(0)));
		assertEquals(2, snapshot.get(1), 0);
		assertEquals(snapshot, equivalent);
		assertEquals(snapshot.hashCode(), equivalent.hashCode());
		assertNotEquals(snapshot,
				new LocusSourceSnapshot2D(new double[] {0, 3}));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusSourceSnapshot2D(new double[] {Double.NaN}));
	}

	@Test
	void lineageIsImmutableValueDataWithTypedShapeRules() {
		List<String> children = new ArrayList<>(Arrays.asList("upper", "lower"));
		LocusLineage2D split = new LocusLineage2D(LineageTransition.SPLIT,
				Collections.singletonList("root"), children);
		children.clear();
		LocusLineage2D equivalent = new LocusLineage2D(LineageTransition.SPLIT,
				Collections.singletonList("root"), Arrays.asList("upper", "lower"));

		assertEquals(Arrays.asList("upper", "lower"), split.getChildKeys());
		assertEquals(split, equivalent);
		assertEquals(split.hashCode(), equivalent.hashCode());
		assertThrows(UnsupportedOperationException.class,
				() -> split.getChildKeys().add("third"));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusLineage2D(LineageTransition.SPLIT,
						Collections.emptyList(), Arrays.asList("upper", "lower")));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusLineage2D(LineageTransition.APPEARED,
						Collections.emptyList(), Arrays.asList("same", "same")));
	}

	@Test
	void branchesAndPublishedSnapshotsHaveCompleteValueSemantics() {
		ExplicitNumericDomainProvider2D provider = numeric();
		List<LocusInterval2D> components = new ArrayList<>(
				Collections.singletonList(provider.getDeclaredDomain()));
		LocusBranch2D branch = branch(provider, components);
		components.clear();
		LocusBranch2D equivalent = branch(provider,
				Collections.singletonList(provider.getDeclaredDomain()));
		List<LocusBranch2D> branches = new ArrayList<>(
				Collections.singletonList(branch));
		LocusBranchSnapshot2D snapshot =
				new LocusBranchSnapshot2D(DefinitionStatus.VALID, branches);
		branches.clear();

		assertEquals(branch, equivalent);
		assertEquals(branch.hashCode(), equivalent.hashCode());
		assertEquals(Collections.singletonList(branch), snapshot.getBranches());
		assertEquals(snapshot, new LocusBranchSnapshot2D(DefinitionStatus.VALID,
				Collections.singletonList(equivalent)));
		assertThrows(UnsupportedOperationException.class,
				() -> branch.getValidDomainComponents().clear());
		assertThrows(NullPointerException.class,
				() -> new LocusBranchSnapshot2D(DefinitionStatus.VALID,
						Collections.singletonList(null)));
	}

	@Test
	void approvedProvidersAreCanonicalValueObjects() {
		ExplicitNumericDomainProvider2D numeric = numeric();
		assertEquals(numeric, numeric());
		assertEquals(numeric.hashCode(), numeric().hashCode());

		StablePathDomainProvider2D segment = StablePathDomainProvider2D.segment(
				"segment-native-t/v1", new LocusPoint2D(0, 0),
				new LocusPoint2D(2, 3), EPS_DOMAIN);
		StablePathDomainProvider2D equivalent = StablePathDomainProvider2D.segment(
				"segment-native-t/v1", new LocusPoint2D(-0.0, 0),
				new LocusPoint2D(2, 3), EPS_DOMAIN);
		assertEquals(segment, equivalent);
		assertEquals(segment.hashCode(), equivalent.hashCode());
	}

	private static ExplicitNumericDomainProvider2D numeric() {
		return new ExplicitNumericDomainProvider2D("hardening-parameter/v1",
				new LocusInterval2D(-1, 1, true, true), Orientation.INCREASING,
				false, EPS_DOMAIN);
	}

	private static LocusBranch2D branch(
			ExplicitNumericDomainProvider2D provider,
			List<LocusInterval2D> components) {
		return new LocusBranch2D("hardening.sheet.main",
				provider.getDeclaredDomain(), components, Orientation.INCREASING,
				"g6r-value-contract/v1", LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
	}
}
