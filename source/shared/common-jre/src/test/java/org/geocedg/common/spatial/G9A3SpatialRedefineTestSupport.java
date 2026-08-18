/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineCandidateOutput;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineEffect;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineOutputGroup;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProposal;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineSignature;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.error.ErrorLogger;
import org.geogebra.common.util.AsyncOperation;

abstract class G9A3SpatialRedefineTestSupport extends BaseUnitTest {
	static final String PROVIDER = "g9a3.redefine.test";
	static final String SCHEMA = "cedg.g9a3.redefine.test";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D();
	}

	SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	GeoIdentityRecord register(GeoElement geo, SpatialRedefineSignature signature) {
		return register(geo, signature, 0);
	}

	GeoIdentityRecord register(GeoElement geo, SpatialRedefineSignature signature,
			long definitionRevision) {
		GeoIdentityRecord record = reserveRecord(signature, definitionRevision);
		registry().registerParticipation(geo, record);
		return record;
	}

	GeoIdentityRecord reserveRecord(SpatialRedefineSignature signature) {
		return reserveRecord(signature, 0);
	}

	private GeoIdentityRecord reserveRecord(SpatialRedefineSignature signature,
			long definitionRevision) {
		return new GeoIdentityRecord(registry().allocatePersistentGeoId(),
				signature.getProvider(),
				signature.getFamily(), signature.getSchemaId(),
				signature.getSchemaVersion(), signature.getAuthority(),
				signature.getBindingRole(), signature.getStableOutputRole(),
				signature.getOutputCardinality(), definitionRevision, 0);
	}

	static SpatialRedefineSignature signature(String provider, String family,
			String role, int cardinality) {
		return new SpatialRedefineSignature(provider, family, SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING, role, cardinality);
	}

	CapturingErrorHandler edit(GeoElement target, String definition) {
		CapturingErrorHandler errors = attemptRedefine(target, definition);
		assertFalse(errors.hasError(), errors::describe);
		return errors;
	}

	CapturingErrorHandler attemptRedefine(GeoElement target, String definition) {
		CapturingErrorHandler errors = new CapturingErrorHandler();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(target, definition,
				EvalInfoFactory.getEvalInfoForRedefinition(getKernel(), target, true),
				false, null, errors);
		return errors;
	}

	OutputPair intersectPair(String circleLabel, String lineLabel) {
		add(circleLabel + "=Circle((0,0),2)");
		add(lineLabel + ":y=0");
		return intersectExisting(circleLabel, lineLabel);
	}

	OutputPair intersectExisting(String circleLabel, String lineLabel) {
		GeoElementND[] outputs = getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling(
						"Intersect(" + circleLabel + "," + lineLabel + ")", false,
						new CapturingErrorHandler(), false, null);
		assertEquals(2, outputs.length);
		return new OutputPair((GeoElement) outputs[0], (GeoElement) outputs[1]);
	}

	static final class OutputPair {
		final GeoElement left;
		final GeoElement right;

		OutputPair(GeoElement left, GeoElement right) {
			this.left = left;
			this.right = right;
		}
	}

	static class CompatibleProvider implements SpatialRedefineProvider {
		private final String provider;
		private final SpatialRedefineSignature signature;
		private final SpatialRedefineDecision decision;

		CompatibleProvider(String provider, SpatialRedefineSignature signature,
				SpatialRedefineDecision decision) {
			this.provider = provider;
			this.signature = signature;
			this.decision = decision;
		}

		@Override
		public String getProviderId() {
			return provider;
		}

		@Override
		public SpatialRedefineSignature describeCandidate(
				SpatialRedefineContext context, GeoElement candidate) {
			return signature;
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return true;
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			return decision;
		}
	}

	static final class FreshOnThreeProvider extends CompatibleProvider {
		FreshOnThreeProvider() {
			super(PROVIDER, signature(PROVIDER, "NUMERIC", "VALUE", 1),
					SpatialRedefineDecision.RETAIN);
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			return proposal.getCandidate() instanceof GeoNumeric
					&& ((GeoNumeric) proposal.getCandidate()).getDouble() == 3
							? SpatialRedefineDecision.FRESH
							: SpatialRedefineDecision.RETAIN;
		}
	}

	static class GroupProvider implements SpatialRedefineProvider {
		private final String provider;
		private final GeoElement first;
		private final GeoElement second;
		private final String firstRole;
		private final String secondRole;
		private final SpatialRedefineDecision decision;
		boolean inspected;

		GroupProvider(String provider, GeoElement first, GeoElement second,
				String firstRole, String secondRole, SpatialRedefineDecision decision) {
			this.provider = provider;
			this.first = first;
			this.second = second;
			this.firstRole = firstRole;
			this.secondRole = secondRole;
			this.decision = decision;
		}

		@Override
		public String getProviderId() {
			return provider;
		}

		@Override
		public SpatialRedefineSignature describeCandidate(
				SpatialRedefineContext context, GeoElement candidate) {
			throw new AssertionError("Grouped provider requires stable-role mapping");
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return true;
		}

		@Override
		public SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
				describeCandidateGroup(SpatialRedefineContext context,
						List<GeoElement> candidates) {
			return SpatialRedefineOutputGroup.of(List.of(
					new SpatialRedefineCandidateOutput(first,
							signature(provider, "POINT", firstRole, 2)),
					new SpatialRedefineCandidateOutput(second,
							signature(provider, "POINT", secondRole, 2))));
		}

		@Override
		public SpatialRedefineEffect describeEffect(SpatialRedefineContext context,
				SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput> outputs) {
			return SpatialRedefineEffect.DEFINITION_CHANGE;
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			inspected = true;
			return decision;
		}
	}

	static final class CapturingErrorHandler implements ErrorLogger {
		private Throwable error;
		private String message;

		@Override
		public void showError(String errorMessage) {
			message = errorMessage;
		}

		@Override
		public void showCommandError(String command, String errorMessage) {
			message = errorMessage;
		}

		@Override
		public String getCurrentCommand() {
			return null;
		}

		@Override
		public boolean onUndefinedVariables(String variables,
				AsyncOperation<String[]> callback) {
			return false;
		}

		@Override
		public void resetError() {
			error = null;
			message = null;
		}

		@Override
		public void log(Throwable throwable) {
			error = throwable;
		}

		boolean hasError() {
			return error != null || message != null;
		}

		boolean sawSpatialIdentityFailure() {
			Throwable current = error;
			while (current != null) {
				if (current instanceof SpatialIdentityException) {
					return true;
				}
				current = current.getCause();
			}
			return message != null && message.contains("REDEFINE_");
		}

		String describe() {
			return error == null ? String.valueOf(message) : error.toString();
		}
	}
}
