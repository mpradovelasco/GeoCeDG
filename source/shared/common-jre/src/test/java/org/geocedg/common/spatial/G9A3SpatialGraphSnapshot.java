/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityKind;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialRecordResolution;
import org.geocedg.common.kernel.spatial.identity.SpatialRecordXmlCodec;
import org.geocedg.common.kernel.spatial.runtime.ProjectionSystemPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialSemanticRuntime;
import org.geocedg.common.kernel.spatial.semantic.DiagramMapEvidence;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationEvidence;
import org.geocedg.common.kernel.spatial.semantic.ProjectionResidualEvidence;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemCertificate;
import org.geocedg.common.kernel.spatial.semantic.SpatialPointCertificate;
import org.geocedg.common.kernel.spatial.semantic.Vector2;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Canonical, label-independent graph snapshot used by G9A3 atomicity tests.
 *
 * <p>The snapshot deliberately records typed IDs, canonical record XML,
 * resolution/attachment state, complete runtime certificate evidence, revision
 * tuples, payload values and the deterministic complete spatial section. It
 * does not use labels, construction order, coordinates or Java object identity
 * as graph keys. Attached host XML and payload coordinates are captured only as
 * evidence, so an attachment swap or payload corruption cannot hide behind an
 * otherwise unchanged semantic graph. Transaction counters are asserted
 * separately because a correct rejected transaction advances rollback evidence
 * while preserving this graph state.</p>
 */
final class G9A3SpatialGraphSnapshot {

	private G9A3SpatialGraphSnapshot() {
	}

	/** Captures the complete current spatial graph owned by a construction. */
	static Snapshot capture(Construction construction) {
		Objects.requireNonNull(construction);
		SpatialIdentityRegistry registry = construction.getSpatialIdentityRegistry();
		SpatialSemanticRuntime runtime = construction.getSpatialSemanticRuntime();
		List<SpatialIdentityRecord> records = new ArrayList<>(registry.getRecords());
		records.sort(Comparator.comparing(record -> record.getId().toExternalForm()));

		StringBuilder canonical = new StringBuilder();
		Map<SpatialIdentityKind, Integer> counts = new EnumMap<>(SpatialIdentityKind.class);
		for (SpatialIdentityRecord record : records) {
			counts.merge(record.getId().getKind(), 1, Integer::sum);
			appendRecord(canonical, registry, record);
			if (record instanceof ProjectionSystemRecord) {
				appendSystemCertificate(canonical, runtime,
						(ProjectionSystemRecord) record);
			} else if (record instanceof SpatialObjectRecord) {
				appendPointCertificate(canonical, runtime, (SpatialObjectRecord) record);
			}
		}

		String spatialSection = registry.writeSpatialSection();
		canonical.append("SECTION|").append(spatialSection).append('\n');
		return new Snapshot(canonical.toString(), sha256(spatialSection), records.size(),
				counts);
	}

	private static void appendRecord(StringBuilder target,
			SpatialIdentityRegistry registry, SpatialIdentityRecord record) {
		SpatialRecordResolution resolution = registry.getResolution(record.getId());
		target.append("RECORD|").append(record.getId().toExternalForm())
				.append('|').append(SpatialRecordXmlCodec.writeRecord(record))
				.append("|resolution=")
				.append(resolution == null ? "ABSENT" : resolution.getState().name())
				.append("|diagnostics=");
		if (resolution != null) {
			target.append(resolution.getDiagnostics().stream()
					.map(G9A3SpatialGraphSnapshot::diagnosticKey)
					.sorted()
					.collect(Collectors.joining(",")));
		}
		if (record instanceof GeoIdentityRecord) {
			GeoElement attached = registry.getGeo(
					((GeoIdentityRecord) record).getId());
			target.append("|attached=").append(attached != null);
			if (attached != null) {
				String hostXml = attached.getXML();
				target.append("|hostClass=").append(attached.getGeoClassType())
						.append("|hostXmlSha256=").append(sha256(hostXml));
			}
		}
		target.append('\n');
	}

	private static String diagnosticKey(SpatialIdentityDiagnostic diagnostic) {
		return diagnostic.getCode().name() + ':' + external(diagnostic.getSubject())
				+ ':' + external(diagnostic.getReference());
	}

	private static void appendSystemCertificate(StringBuilder target,
			SpatialSemanticRuntime runtime, ProjectionSystemRecord system) {
		ProjectionSystemPilotCertificate published =
				runtime.getProjectionSystemCertificate(system.getId());
		target.append("SYSTEM_CERT|").append(system.getId().toExternalForm());
		if (published == null) {
			target.append("|ABSENT\n");
			return;
		}
		ProjectionSystemCertificate semantic = published.getSemanticCertificate();
		target.append("|current=").append(published.isCurrentRevision())
				.append("|revisions=").append(revisionTuple(published.getRevisionTuple()))
				.append("|values=").append(published.getValueSnapshotToken());
		appendSystemEvidence(target, semantic, "");
		target.append('\n');
	}

	private static void appendPointCertificate(StringBuilder target,
			SpatialSemanticRuntime runtime, SpatialObjectRecord object) {
		SpatialPointPilotCertificate published =
				runtime.getSpatialPointCertificate(object.getId());
		target.append("POINT_CERT|").append(object.getId().toExternalForm());
		if (published == null) {
			target.append("|ABSENT\n");
			return;
		}
		SpatialPointCertificate semantic = published.getSemanticCertificate();
		target.append("|system=").append(published.getSystemId().toExternalForm())
				.append("|current=").append(published.isCurrentRevision())
				.append("|revisions=").append(revisionTuple(published.getRevisionTuple()))
				.append("|values=").append(published.getValueSnapshotToken())
				.append("|capability=").append(semantic.getCapabilityStatus())
				.append("|systemStatus=").append(semantic.getProjectionSystemStatus())
				.append("|definition=").append(semantic.getDefinitionStatus())
				.append("|certificate=").append(semantic.getCertificateStatus())
				.append("|currentness=").append(semantic.getCurrentnessStatus())
				.append("|fidelity=").append(semantic.getRepresentationFidelity())
				.append("|numerical=").append(semantic.getNumericalEvidenceStatus())
				.append("|correspondence=").append(semantic.getCorrespondenceStatus())
				.append("|sourceRevision=").append(semantic.getSourceRevision())
				.append("|rank=").append(semantic.getRank())
				.append("|condition=").append(bits(semantic.getConditionNumber()))
				.append("|intrinsicMax=")
				.append(bits(semantic.getMaximumIntrinsicNormalizedResidual()))
				.append("|diagramMax=")
				.append(bits(semantic.getMaximumDiagramNormalizedResidual()))
				.append("|arithmetic=").append(semantic.getArithmeticMethod())
				.append("|singular=");
		for (double singularValue : semantic.getSingularValues()) {
			target.append(bits(singularValue)).append(',');
		}
		target.append("|payload=");
		if (semantic.getPoint().isPresent()) {
			appendVector3(target, semantic.getPoint().get());
		} else {
			target.append("ABSENT");
		}
		appendSystemEvidence(target, semantic.getProjectionSystemCertificate(),
				"pointSystem.");
		List<ProjectionResidualEvidence> residuals =
				new ArrayList<>(semantic.getResidualEvidence());
		residuals.sort(Comparator.comparing(
				ProjectionResidualEvidence::getEvidenceKey));
		for (ProjectionResidualEvidence residual : residuals) {
			target.append("|residual[").append(residual.getEvidenceKey()).append("]=")
					.append(residual.getObservationIndex()).append(':');
			appendVector2(target, residual.getIntrinsicResidual());
			target.append(':').append(bits(residual.getIntrinsicResidualNorm()))
					.append(':')
					.append(bits(residual.getNormalizedIntrinsicResidual()))
					.append(':');
			appendVector2(target, residual.getDiagramResidual());
			target.append(':').append(bits(residual.getDiagramResidualNorm()))
					.append(':')
					.append(bits(residual.getNormalizedDiagramResidual()));
		}
		target.append('\n');
	}

	private static void appendSystemEvidence(StringBuilder target,
			ProjectionSystemCertificate certificate, String prefix) {
		target.append('|').append(prefix).append("capability=")
				.append(certificate.getCapabilityStatus())
				.append('|').append(prefix).append("status=")
				.append(certificate.getStatus())
				.append('|').append(prefix).append("frames=")
				.append(certificate.getEvaluatedFrameCount())
				.append('|').append(prefix).append("maps=")
				.append(certificate.getEvaluatedMapCount())
				.append('|').append(prefix).append("maximum=")
				.append(bits(certificate.getMaximumNormalizedResidual()));

		List<DiagramMapEvidence> maps = new ArrayList<>(certificate.getMapEvidence());
		maps.sort(Comparator.comparing(DiagramMapEvidence::getEvidenceKey));
		for (DiagramMapEvidence map : maps) {
			target.append('|').append(prefix).append("map[")
					.append(map.getEvidenceKey()).append("]=")
					.append(map.getFamily()).append(':')
					.append(map.getOrientation()).append(':')
					.append(map.getSourceUnit()).append(':')
					.append(map.getDiagramUnit()).append(':')
					.append(bits(map.getDeclaredScale())).append(':')
					.append(map.getRevision()).append(':')
					.append(map.getStatus());
		}

		List<ProjectionRelationEvidence> relations =
				new ArrayList<>(certificate.getRelationEvidence());
		relations.sort(Comparator.comparing(
				ProjectionRelationEvidence::getEvidenceKey));
		for (ProjectionRelationEvidence relation : relations) {
			target.append('|').append(prefix).append("relation[")
					.append(relation.getEvidenceKey()).append("]=")
					.append(relation.getKind()).append(':')
					.append(relation.getStatus()).append(':')
					.append(bits(relation.getLineOffsetResidual())).append(':')
					.append(bits(relation.getDirectionResidual())).append(':')
					.append(bits(relation.getSupportPlaneResidual())).append(':')
					.append(bits(relation.getSupportOrientationResidual())).append(':')
					.append(bits(relation.getFoldSideDotProduct())).append(':')
					.append(bits(relation.getMaximumNormalizedResidual()));
		}
	}

	private static void appendVector2(StringBuilder target, Vector2 vector) {
		target.append(bits(vector.getX())).append(',').append(bits(vector.getY()));
	}

	private static void appendVector3(StringBuilder target, Vector3 vector) {
		target.append(bits(vector.getX())).append(',').append(bits(vector.getY()))
				.append(',').append(bits(vector.getZ()));
	}

	private static String bits(double value) {
		return Long.toHexString(Double.doubleToLongBits(value));
	}

	private static String revisionTuple(Map<SpatialIdentityId, String> tuple) {
		return tuple.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> entry.getKey().toExternalForm() + '=' + entry.getValue())
				.collect(Collectors.joining(","));
	}

	private static String external(SpatialIdentityId identity) {
		return identity == null ? "-" : identity.toExternalForm();
	}

	private static String sha256(String text) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(hash.length * 2);
			for (byte value : hash) {
				result.append(String.format("%02x", value & 0xff));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is required by the JRE", exception);
		}
	}

	/** Immutable value compared before and after hostile lifecycle operations. */
	static final class Snapshot {
		private final String canonicalText;
		private final String spatialSectionSha256;
		private final int recordCount;
		private final Map<SpatialIdentityKind, Integer> recordCountsByKind;

		private Snapshot(String canonicalText, String spatialSectionSha256,
				int recordCount, Map<SpatialIdentityKind, Integer> recordCountsByKind) {
			this.canonicalText = canonicalText;
			this.spatialSectionSha256 = spatialSectionSha256;
			this.recordCount = recordCount;
			this.recordCountsByKind = new EnumMap<>(recordCountsByKind);
		}

		String getCanonicalText() {
			return canonicalText;
		}

		String getSpatialSectionSha256() {
			return spatialSectionSha256;
		}

		int getRecordCount() {
			return recordCount;
		}

		Map<SpatialIdentityKind, Integer> getRecordCountsByKind() {
			return new EnumMap<>(recordCountsByKind);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Snapshot
					&& canonicalText.equals(((Snapshot) other).canonicalText);
		}

		@Override
		public int hashCode() {
			return canonicalText.hashCode();
		}

		@Override
		public String toString() {
			return canonicalText;
		}
	}
}
