/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusBranchSnapshot2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSourceSnapshot2D;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;

/** Creates the bounded, nonpersistent developer fixtures for one process. */
final class LocusV2LaboratoryFixtures {
	private static final double EPS_DOMAIN = 1E-14;
	private static final String MAIN_BRANCH = "laboratory.sheet.main";
	private static final GColor[] COLORS = {
			GColor.BLUE, GColor.DARK_GREEN, GColor.MAGENTA, GColor.ORANGE,
			GColor.DARK_RED, GColor.PURPLE, GColor.CYAN
	};

	private LocusV2LaboratoryFixtures() {
	}

	static State create(Construction construction) {
		List<Entry> entries = new ArrayList<>();
		addAnalyticFixture(construction, entries);
		addSegmentFixture(construction, entries);
		addTopologyFixture(construction, entries);
		addDiscontinuityFixture(construction, entries);
		addUnboundedFixture(construction, entries);
		addNestedFixtures(construction, entries);
		for (int index = 0; index < entries.size(); index++) {
			GeoLocusV2 locus = entries.get(index).getLocus();
			locus.setObjColor(COLORS[index % COLORS.length]);
			locus.setLineThickness(index >= entries.size() - 5 ? 2 : 4);
			locus.setLabel(entries.get(index).getLabel());
			locus.setLabelVisible(false);
		}
		return new State(entries);
	}

	private static void addAnalyticFixture(Construction construction,
			List<Entry> entries) {
		ExplicitNumericDomainProvider2D provider = numeric(
				"laboratory-analytic-t/v1", -4, 4, true, true, false);
		GeoNumeric scale = new GeoNumeric(construction, 1.25);
		scale.setLabel("g6rAnalyticScale");
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				construction, "laboratory.analytic", scale, provider,
				fullBranches(provider), (value, branch, parameter, session) ->
						new LocusPoint2D(parameter,
								value * Math.sin(parameter) + 4.5),
				"laboratory-analytic-sine/v1");
		entries.add(new Entry("analytic", "g6rAnalyticV2", locus));
	}

	private static void addSegmentFixture(Construction construction,
			List<Entry> entries) {
		GeoPoint start = new GeoPoint(construction, "g6rSegmentA", -4, -3, 1);
		GeoPoint end = new GeoPoint(construction, "g6rSegmentB", 4, -3, 1);
		GeoSegment segment = new AlgoJoinPointsSegment(construction, start, end)
				.getSegment();
		segment.setLabel("g6rSegmentDriver");
		GeoPoint driver = (GeoPoint) new AlgoPointOnPath(construction, segment,
				-4, -3).getP();
		driver.setLabel("g6rSegmentPoint");
		GeoLocusV2 locus = LocusV2Factory.createSegmentPathDriven(
				LocusV2Mode.V2, construction, "laboratory.segment", segment,
				driver, EPS_DOMAIN,
				(driverPoint, branch, parameter, session) -> new LocusPoint2D(
						driverPoint.getX(),
						driverPoint.getY() + Math.sin(Math.PI * parameter)),
				"laboratory-segment-wave/v1");
		entries.add(new Entry("segment provider", "g6rSegmentV2", locus));
	}

	private static void addTopologyFixture(Construction construction,
			List<Entry> entries) {
		ExplicitNumericDomainProvider2D provider = numeric(
				"laboratory-topology-t/v1", -3, 3, true, true, false);
		GeoNumeric branchControl = new GeoNumeric(construction, 1);
		branchControl.setLabel("g6rTopologyBranches");
		GeoNumeric componentControl = new GeoNumeric(construction, 0.25);
		componentControl.setLabel("g6rTopologyGap");
		GeoLocusV2 locus = LocusV2Factory.createDynamicAnalytic(LocusV2Mode.V2,
				construction, "laboratory.topology",
				Arrays.asList(branchControl, componentControl), provider,
				(sources, previous) -> topologySnapshot(sources, previous, provider),
				(sources, branch, parameter, session) -> new LocusPoint2D(parameter,
						branch.getBranchKey().endsWith("/+")
								? 1.5 + 0.2 * parameter
								: branch.getBranchKey().endsWith("/-")
										? 0.25 + 0.2 * parameter
										: 0.8 + 0.2 * parameter),
				"laboratory-topology/v1");
		entries.add(new Entry("branch/components/lineage", "g6rTopologyV2", locus));
	}

	private static void addDiscontinuityFixture(Construction construction,
			List<Entry> entries) {
		ExplicitNumericDomainProvider2D provider = numeric(
				"laboratory-discontinuity-t/v1", -3, 3, true, true, false);
		LocusBranch2D branch = new LocusBranch2D(MAIN_BRANCH,
				provider.getDeclaredDomain(), Arrays.asList(
						new LocusInterval2D(-3, -0.25, true, true),
						new LocusInterval2D(0.25, 3, true, true)),
				Orientation.INCREASING, "laboratory-discontinuity/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
		GeoNumeric source = new GeoNumeric(construction, 0);
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				construction, "laboratory.discontinuity", source, provider,
				Collections.singletonList(branch),
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter + 5, 1 / parameter),
				"laboratory-discontinuity/v1");
		entries.add(new Entry("discontinuity/subpaths", "g6rDiscontinuityV2", locus));
	}

	private static void addUnboundedFixture(Construction construction,
			List<Entry> entries) {
		ExplicitNumericDomainProvider2D provider = numeric(
				"laboratory-unbounded-t/v1", -1.55, 1.55, false, false, false);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(MAIN_BRANCH,
				provider, "laboratory-unbounded/v1",
				EnumSet.of(BranchProperty.UNBOUNDED));
		GeoNumeric source = new GeoNumeric(construction, 0);
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				construction, "laboratory.unbounded", source, provider,
				Collections.singletonList(branch),
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter - 5, Math.tan(parameter)),
				"laboratory-unbounded-tangent/v1");
		entries.add(new Entry("unbounded/presentation clipping", "g6rUnboundedV2",
				locus));
	}

	private static void addNestedFixtures(Construction construction,
			List<Entry> entries) {
		ExplicitNumericDomainProvider2D provider = numeric(
				"laboratory-nested-t/v1", -2, 2, true, true, false);
		List<LocusBranch2D> branches = fullBranches(provider);
		GeoNumeric source = new GeoNumeric(construction, 1);
		source.setLabel("g6rNestedSource");
		List<GeoLocusV2> chain = new ArrayList<>();
		chain.add(LocusV2Factory.createAnalytic(LocusV2Mode.V2, construction,
				"laboratory.nested.1", source, provider, branches,
				(value, branch, parameter, session) -> new LocusPoint2D(parameter,
						-5 + value * 0.2 * parameter * parameter),
				"laboratory-nested-base/v1"));
		for (int depth = 2; depth <= 5; depth++) {
			GeoLocusV2 upstream = chain.get(chain.size() - 1);
			int currentDepth = depth;
			chain.add(LocusV2Factory.createNested(LocusV2Mode.V2, construction,
					"laboratory.nested." + depth, upstream, MAIN_BRANCH, provider,
					branches, parameter -> parameter,
					(parameter, point) -> new LocusPoint2D(point.getX(),
							point.getY() + 0.35
									+ 0.05 * Math.sin(currentDepth * parameter)),
					"laboratory-nested-level-" + depth + "/v1"));
		}
		for (int index = 0; index < chain.size(); index++) {
			int depth = index + 1;
			entries.add(new Entry("nested depth " + depth,
					"g6rNestedV2Level" + depth, chain.get(index)));
		}
	}

	private static LocusBranchSnapshot2D topologySnapshot(
			LocusSourceSnapshot2D sources, LocusDefinition2D previous,
			ExplicitNumericDomainProvider2D provider) {
		double branchControl = sources.get(0);
		double gapControl = sources.get(1);
		if (branchControl < 0 || gapControl > 3) {
			LocusBranch2D disappeared = topologyBranch("topology.root", provider,
					Collections.emptyList(), new LocusLineage2D(
							LineageTransition.DISAPPEARED,
							Collections.singletonList("topology.root"),
							Collections.emptyList()));
			return new LocusBranchSnapshot2D(DefinitionStatus.EMPTY_DOMAIN,
					Collections.singletonList(disappeared));
		}
		List<LocusInterval2D> components = topologyComponents(gapControl);
		if (branchControl > 0) {
			List<String> children = Arrays.asList("topology.root/+", "topology.root/-");
			LocusLineage2D lineage = previous != null
					&& previous.getBranch("topology.root") != null
							? new LocusLineage2D(LineageTransition.SPLIT,
									Collections.singletonList("topology.root"), children)
							: new LocusLineage2D(LineageTransition.APPEARED,
									Collections.emptyList(), children);
			return new LocusBranchSnapshot2D(DefinitionStatus.VALID,
					Arrays.asList(topologyBranch(children.get(0), provider, components,
							lineage), topologyBranch(children.get(1), provider,
							components, lineage)));
		}
		LocusLineage2D lineage = previous != null
				&& previous.getBranch("topology.root/+") != null
						? new LocusLineage2D(LineageTransition.MERGED,
								Arrays.asList("topology.root/+", "topology.root/-"),
								Collections.singletonList("topology.root"))
						: LocusLineage2D.unchanged();
		return new LocusBranchSnapshot2D(DefinitionStatus.VALID,
				Collections.singletonList(topologyBranch("topology.root", provider,
						components, lineage)));
	}

	private static List<LocusInterval2D> topologyComponents(double gap) {
		if (gap <= 0) {
			return Collections.singletonList(new LocusInterval2D(-3, 3, true, true));
		}
		double boundedGap = Math.min(gap, 3);
		return Arrays.asList(new LocusInterval2D(-3, -boundedGap, true, true),
				new LocusInterval2D(boundedGap, 3, true, true));
	}

	private static LocusBranch2D topologyBranch(String key,
			LocusDriverDomainProvider2D provider, List<LocusInterval2D> components,
			LocusLineage2D lineage) {
		return new LocusBranch2D(key, provider.getDeclaredDomain(), components,
				Orientation.INCREASING, "laboratory-topology/v1", lineage,
				EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
	}

	private static ExplicitNumericDomainProvider2D numeric(String descriptor,
			double lower, double upper, boolean lowerClosed, boolean upperClosed,
			boolean periodic) {
		return new ExplicitNumericDomainProvider2D(descriptor,
				new LocusInterval2D(lower, upper, lowerClosed, upperClosed),
				Orientation.INCREASING, periodic, EPS_DOMAIN);
	}

	private static List<LocusBranch2D> fullBranches(
			LocusDriverDomainProvider2D provider) {
		return Collections.singletonList(LocusV2Factory.fullDomainBranch(MAIN_BRANCH,
				provider, "locus-v2-laboratory/v1",
				EnumSet.noneOf(BranchProperty.class)));
	}

	/** Immutable fixture collection retained by the diagnostic panel. */
	static final class State {
		private final List<Entry> entries;

		State(List<Entry> entries) {
			this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
		}

		List<Entry> getEntries() {
			return entries;
		}
	}

	/** One named, visible laboratory locus. */
	static final class Entry {
		private final String purpose;
		private final String label;
		private final GeoLocusV2 locus;

		Entry(String purpose, String label, GeoLocusV2 locus) {
			this.purpose = purpose;
			this.label = label;
			this.locus = locus;
		}

		String getPurpose() {
			return purpose;
		}

		String getLabel() {
			return label;
		}

		GeoLocusV2 getLocus() {
			return locus;
		}
	}
}
