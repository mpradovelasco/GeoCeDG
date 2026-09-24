/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3 D3-b: one focused test per failure cause that R0 found collapsed
 * onto {@code AMBIGUOUS}. Each cause now carries its own typed kind, and only a
 * genuine multiplicity of interpretations remains {@code AMBIGUOUS}.
 */
class PreG9bR3RedefineVocabularyTest extends BaseUnitTest {
	private static final String TEST_PROVIDER = "pre-g9b-r3.vocabulary.test";
	private static final String TEST_SCHEMA = "cedg.pre-g9b-r3.test";
	private static final String ROLE = ConstructionGeoRedefineProvider.STABLE_OUTPUT_ROLE;

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void familyChangeIsATypedDurableContractChangeWithoutAnyOperation() {
		witness();
		GeoElement circle = add("k=Circle(D,1)");
		assertKind(SpatialRedefineDescriptionException.Kind.DURABLE_CONTRACT_CHANGE,
				"Candidate changes the construction-defined geo family",
				() -> provider().describeCandidate(context(), circle));

		SpatialRedefineAssessment assessment = assess(circle);
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				assessment.getStatus());
		assertEquals("Candidate changes the construction-defined geo family",
				assessment.getDetail());
		assertNull(assessment.getProposal());
		assertFalse(assessment.isIdentityPreservingUpdateAvailable());
		assertFalse(assessment.isExplicitReplacementAvailable());
		// Without a describable proposal no mode is executable.
		for (SpatialRedefineExecutionMode mode : SpatialRedefineExecutionMode.values()) {
			SpatialIdentityException rejected = assertThrows(SpatialIdentityException.class,
					() -> registry().prepareRedefine(assessment, mode));
			assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					rejected.getDiagnostic().getCode());
			assertEquals("Candidate changes the construction-defined geo family",
					rejected.getDiagnostic().getMessage());
		}
	}

	@Test
	void changedCandidateCardinalityIsADurableContractChange() {
		witness();
		GeoElement first = add("k1=Midpoint(D,C)");
		GeoElement second = add("k2=Midpoint(C,D)");
		assertKind(SpatialRedefineDescriptionException.Kind.DURABLE_CONTRACT_CHANGE,
				"Candidate changes the construction-defined output cardinality",
				() -> provider().describeCandidateGroup(context(), List.of(first, second),
						registry()));
		SpatialRedefineAssessment assessment = registry().assessRedefine(context(), first,
				List.of(first, second));
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				assessment.getStatus());
		assertFalse(assessment.isIdentityPreservingUpdateAvailable());
		assertFalse(assessment.isExplicitReplacementAvailable());
	}

	@Test
	void severalOldOutputsRemainAGenuineAmbiguityOfTheGroup() {
		witness();
		GeoElement candidate = add("k=Midpoint(D,C)");
		SpatialRedefineContext twoOutputs = twoOutputContext();
		assertKind(SpatialRedefineDescriptionException.Kind.AMBIGUOUS,
				"Construction-defined redefine requires one output",
				() -> provider().describeCandidateGroup(twoOutputs, List.of(candidate),
						registry()));
	}

	@Test
	void anEffectWithoutOneTargetedOutputIsAmbiguous() {
		witness();
		GeoElement candidate = add("k=Midpoint(D,C)");
		SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput> group =
				SpatialRedefineOutputGroup.singleton(new SpatialRedefineCandidateOutput(
						candidate, provider().describeCandidate(context(), candidate)));
		assertKind(SpatialRedefineDescriptionException.Kind.AMBIGUOUS,
				"Construction-defined redefine requires one unambiguous output",
				() -> provider().describeEffect(twoOutputContext(), group));
	}

	@Test
	void aChangedStableRoleIsADurableContractChange() {
		witness();
		GeoElement candidate = add("k=Midpoint(D,C)");
		SpatialRedefineSignature described = provider().describeCandidate(context(),
				candidate);
		SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput> otherRole =
				SpatialRedefineOutputGroup.singleton(new SpatialRedefineCandidateOutput(
						candidate, withRole(described, "OTHER")));
		assertKind(SpatialRedefineDescriptionException.Kind.DURABLE_CONTRACT_CHANGE,
				"Construction-defined candidate changes its durable role contract",
				() -> provider().describeEffect(context(), otherRole));
	}

	@Test
	void anAmbiguousOrMissingContextIsAmbiguous() {
		witness();
		GeoElement candidate = add("k=Midpoint(D,C)");
		assertKind(SpatialRedefineDescriptionException.Kind.AMBIGUOUS,
				"Construction-defined redefine context is ambiguous",
				() -> provider().describeCandidate(twoOutputContext(), candidate));
		assertKind(SpatialRedefineDescriptionException.Kind.AMBIGUOUS,
				"Construction-defined redefine context is ambiguous",
				() -> provider().describeCandidate(null, candidate));
	}

	@Test
	void aNonNeutralOrFutureContextIsUndescribable() {
		witness();
		GeoElement candidate = add("k=Midpoint(D,C)");
		GeoElement e = lookup("E");
		SpatialRedefineSignature old = context().getOldSignature();
		for (SpatialRedefineSignature foreign : List.of(
				new SpatialRedefineSignature(old.getProvider(), old.getFamily(),
						old.getSchemaId(), old.getSchemaVersion(),
						EditAuthorityMode.PROJECTION_DEFINED, old.getBindingRole(),
						old.getStableOutputRole(), old.getOutputCardinality(),
						old.getDependencies()),
				new SpatialRedefineSignature(old.getProvider(), old.getFamily(),
						old.getSchemaId(), 3, old.getAuthority(), old.getBindingRole(),
						old.getStableOutputRole(), old.getOutputCardinality(),
						old.getDependencies()))) {
			SpatialRedefineContext context = new SpatialRedefineContext(e,
					registry().getPersistentGeoId(e), foreign, 1, 0, 0,
					context().getRollbackXml(), 1, 0, 0);
			assertKind(SpatialRedefineDescriptionException.Kind.UNDESCRIBABLE_PROPOSAL,
					"Context is not a neutral construction-defined geo",
					() -> provider().describeCandidate(context, candidate));
		}
	}

	@Test
	void anIdentityFreeInputNoLongerMakesTheProposalUndescribable() {
		witness();
		GeoElement candidate = add("k=Midpoint(D,C+(1,0))");
		SpatialRedefineSignature signature = provider().describeCandidate(context(),
				candidate);
		assertEquals(List.of(registry().getPersistentGeoId(lookup("C"))),
				signature.getDependencies());
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				signature.getSchemaVersion());
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				assess(candidate).getStatus());
	}

	@Test
	void providerRolesThatDoNotCoverTheHostGroupAreUndescribable() {
		GeoNumeric target = add("t=1");
		GeoNumeric other = add("u=2");
		GeoNumeric candidate = add("v=3");
		register(target);
		registry().registerRedefineProvider(new TestProvider(
				context -> SpatialRedefineOutputGroup.singleton(
						new SpatialRedefineCandidateOutput(other, signature()))));
		SpatialRedefineAssessment assessment = registry().assessRedefine(
				registry().captureRedefineContext(target), candidate, List.of(candidate));
		assertEquals(SpatialRedefineAssessmentStatus.UNDESCRIBABLE_PROPOSAL,
				assessment.getStatus());
		assertEquals("Provider candidate roles do not cover the host output group",
				assessment.getDetail());
	}

	@Test
	void anUnmappedTargetedCandidateIsUndescribable() {
		witness();
		GeoElement candidate = add("k=Midpoint(D,C)");
		GeoElement unrelated = add("j=Midpoint(C,D)");
		SpatialRedefineAssessment assessment = registry().assessRedefine(context(),
				unrelated, List.of(candidate));
		assertEquals(SpatialRedefineAssessmentStatus.UNDESCRIBABLE_PROPOSAL,
				assessment.getStatus());
		assertEquals("Provider did not map the explicit replacement candidate",
				assessment.getDetail());
		assertFalse(assessment.isExplicitReplacementAvailable());
	}

	@Test
	void onlyAProviderTypedAmbiguityIsReportedAsAmbiguous() {
		GeoNumeric target = add("t=1");
		GeoNumeric candidate = add("v=3");
		register(target);
		TestProvider provider = new TestProvider(context -> {
			throw new IllegalArgumentException("Opaque provider failure");
		});
		registry().registerRedefineProvider(provider);
		assertEquals(SpatialRedefineAssessmentStatus.UNDESCRIBABLE_PROPOSAL,
				assessNumeric(target, candidate).getStatus());
		provider.describe = context -> {
			throw SpatialRedefineDescriptionException.ambiguous("Two readings remain");
		};
		SpatialRedefineAssessment ambiguous = assessNumeric(target, candidate);
		assertEquals(SpatialRedefineAssessmentStatus.AMBIGUOUS, ambiguous.getStatus());
		assertEquals("Two readings remain", ambiguous.getDetail());
		provider.describe = context -> {
			throw SpatialRedefineDescriptionException.durableContractChange(
					"Role contract changed");
		};
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				assessNumeric(target, candidate).getStatus());
		provider.describe = context -> {
			throw new IllegalStateException("Provider state failure");
		};
		assertEquals(SpatialRedefineAssessmentStatus.UNSUPPORTED,
				assessNumeric(target, candidate).getStatus());
	}

	@Test
	void aDurableContractChangeOffersOnlyTheOperationsItsInspectionAuthorizes() {
		GeoNumeric target = add("t=1");
		GeoNumeric candidate = add("v=3");
		register(target);
		TestProvider provider = new TestProvider(context -> SpatialRedefineOutputGroup
				.singleton(new SpatialRedefineCandidateOutput(candidate, signature())));
		provider.durableContractChange = true;
		provider.inspect = proposal -> proposal.isReplacementOperationSelected()
				? SpatialRedefineDecision.FRESH : SpatialRedefineDecision.REJECT;
		registry().registerRedefineProvider(provider);

		SpatialRedefineAssessment replacementOnly = assessNumeric(target, candidate);
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				replacementOnly.getStatus());
		assertFalse(replacementOnly.isIdentityPreservingUpdateAvailable());
		assertTrue(replacementOnly.isExplicitReplacementAvailable());
		assertRejected(replacementOnly,
				SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE);
		assertRejected(replacementOnly, SpatialRedefineExecutionMode.ADVANCED_RETAIN);
		SpatialRedefineTransaction replacement = registry().prepareRedefine(
				replacementOnly, SpatialRedefineExecutionMode.LEGACY_REPLACEMENT);
		assertEquals(SpatialRedefineDecision.FRESH, replacement.getDecision());
		replacement.rollback();

		provider.inspect = proposal -> proposal.isContractUpdateSelected()
				? SpatialRedefineDecision.RETAIN : SpatialRedefineDecision.REJECT;
		SpatialRedefineAssessment updateOnly = assessNumeric(target, candidate);
		assertTrue(updateOnly.isIdentityPreservingUpdateAvailable());
		assertFalse(updateOnly.isExplicitReplacementAvailable());
		assertRejected(updateOnly, SpatialRedefineExecutionMode.LEGACY_REPLACEMENT);
		assertRejected(updateOnly, SpatialRedefineExecutionMode.ADVANCED_RETAIN);
		SpatialRedefineTransaction update = registry().prepareRedefine(updateOnly,
				SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE);
		assertEquals(SpatialRedefineDecision.RETAIN, update.getDecision());
		update.rollback();

		provider.inspect = proposal -> SpatialRedefineDecision.REJECT;
		SpatialRedefineAssessment none = assessNumeric(target, candidate);
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				none.getStatus());
		assertFalse(none.isIdentityPreservingUpdateAvailable());
		assertFalse(none.isExplicitReplacementAvailable());
		for (SpatialRedefineExecutionMode mode : SpatialRedefineExecutionMode.values()) {
			assertRejected(none, mode);
		}
	}

	@Test
	void theLegacyPreparationPathStillFailsClosedOnADescriptionFailure() {
		witness();
		GeoElement circle = add("k=Circle(D,1)");
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> registry().prepareRedefine(context(), circle, List.of(circle), false));
		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
				failure.getDiagnostic().getCode());
		assertNotNull(registry().getPersistentGeoId(lookup("E")));
	}

	private void witness() {
		getKernel().setContinuous(false);
		add("A=(0,0)");
		add("B=(2,0)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E=Midpoint(D,C)");
		assertNotNull(add("a=LocusV2(E,C)"));
		assertNotNull(registry().getPersistentGeoId(lookup("E")));
	}

	private SpatialRedefineContext context() {
		return registry().captureRedefineContext(lookup("E"));
	}

	private SpatialRedefineContext twoOutputContext() {
		SpatialRedefineContext single = context();
		GeoElement sibling = lookup("C");
		PersistentGeoId siblingId = registry().getPersistentGeoId(sibling);
		SpatialRedefinePersistedOutput second = new SpatialRedefinePersistedOutput(
				sibling, siblingId, withRole(registry().getGeoRecord(siblingId)
						.toRedefineSignature(), "SECOND"), 0, 0);
		return new SpatialRedefineContext(SpatialRedefineOutputGroup.of(List.of(
				single.getOldOutputs().get(ROLE), second)), ROLE, 2,
				single.getRollbackXml(), 1, 0, 0, null);
	}

	private SpatialRedefineAssessment assess(GeoElement candidate) {
		return registry().assessRedefine(context(), candidate, List.of(candidate));
	}

	private SpatialRedefineAssessment assessNumeric(GeoNumeric target,
			GeoNumeric candidate) {
		return registry().assessRedefine(registry().captureRedefineContext(target),
				candidate, List.of(candidate));
	}

	private ConstructionGeoRedefineProvider provider() {
		return new ConstructionGeoRedefineProvider(registry());
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private void register(GeoNumeric geo) {
		SpatialRedefineSignature signature = signature();
		registry().registerParticipation(geo, new GeoIdentityRecord(
				registry().allocatePersistentGeoId(), signature.getProvider(),
				signature.getFamily(), signature.getSchemaId(), signature.getSchemaVersion(),
				signature.getAuthority(), signature.getBindingRole(),
				signature.getStableOutputRole(), signature.getOutputCardinality(), 0, 0));
	}

	private static SpatialRedefineSignature signature() {
		return new SpatialRedefineSignature(TEST_PROVIDER, "NUMERIC", TEST_SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				ROLE, 1);
	}

	private static SpatialRedefineSignature withRole(SpatialRedefineSignature signature,
			String role) {
		return new SpatialRedefineSignature(signature.getProvider(),
				signature.getFamily(), signature.getSchemaId(),
				signature.getSchemaVersion(), signature.getAuthority(),
				signature.getBindingRole(), role, signature.getOutputCardinality(),
				signature.getDependencies());
	}

	private void assertRejected(SpatialRedefineAssessment assessment,
			SpatialRedefineExecutionMode mode) {
		SpatialIdentityException rejected = assertThrows(SpatialIdentityException.class,
				() -> registry().prepareRedefine(assessment, mode));
		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
				rejected.getDiagnostic().getCode(), mode.name());
	}

	private static void assertKind(SpatialRedefineDescriptionException.Kind kind,
			String message, Runnable description) {
		SpatialRedefineDescriptionException failure = assertThrows(
				SpatialRedefineDescriptionException.class, description::run);
		assertEquals(kind, failure.getKind());
		assertEquals(message, failure.getMessage());
	}

	/** Provider double whose group description is scripted by each test. */
	private static final class TestProvider implements SpatialRedefineProvider {
		private Function<SpatialRedefineContext,
				SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>> describe;
		private Function<SpatialRedefineProposal, SpatialRedefineDecision> inspect =
				proposal -> SpatialRedefineDecision.RETAIN;
		private boolean durableContractChange;

		TestProvider(Function<SpatialRedefineContext,
				SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>> describe) {
			this.describe = describe;
		}

		@Override
		public String getProviderId() {
			return TEST_PROVIDER;
		}

		@Override
		public SpatialRedefineSignature describeCandidate(SpatialRedefineContext context,
				GeoElement candidate) {
			return signature();
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return true;
		}

		@Override
		public SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
				describeCandidateGroup(SpatialRedefineContext context,
						List<GeoElement> candidates, SpatialIdentityGraph candidateGraph) {
			return describe.apply(context);
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			return inspect.apply(proposal);
		}

		@Override
		public boolean isDurableContractChange(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			return durableContractChange;
		}
	}
}
