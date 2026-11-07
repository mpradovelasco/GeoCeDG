/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.units.UnitState;

/** Immutable observations and differential evidence for the Desktop inspection worker. */
record GeoCeDGInspectionResult(List<Stage> stages, Admission admission,
		List<Comparison> comparisons, List<Diagnostic> diagnostics) {
	GeoCeDGInspectionResult {
		stages = List.copyOf(stages);
		comparisons = List.copyOf(comparisons);
		diagnostics = List.copyOf(diagnostics);
	}

	enum Phase { SOURCE, RECEIVER, TRIAL, REOPENED }

	enum Status {
		VERIFIED_UNCHANGED, VERIFIED_CHANGED, KNOWN_OMISSION, NOT_ESTABLISHED,
		NOT_EVALUATED, INVALID, TECHNICAL_FAILURE
	}

	enum Basis { LIVE_INSTANCE, NATIVE_COPY_TRACE, DURABLE_ID_IN_SAME_DOCUMENT }

	enum Dimension {
		DOCUMENT_INTEGRITY, CONSTRUCTIVE_CORRESPONDENCE, GEOMETRIC_EQUIVALENCE,
		PRESENTATION, PERSISTENCE, PUBLICABILITY
	}

	enum ObjectOutcome {
		CORRESPONDENCE_PROVEN, RECONSTRUCTED_DIFFERENTLY, OMISSION_PROVEN, NOT_ESTABLISHED
	}

	enum Property {
		PRESENCE, RUNTIME_INSTANCE, LABEL, DURABLE_ID, CLASS, PRODUCER, INPUTS, OUTPUTS,
		DEFINITION_TEXT, DEFINEDNESS, CURRENT_VALUE, PARAMETER_DOMAIN, PRESENTATION_FIELDS
	}

	enum AdmissionState { ADMITTED, REJECTED, NOT_EVALUATED }

	record Admission(AdmissionState state, String reason, String detail) {
	}

	record Diagnostic(Phase phase, Status status, String code, String detail) {
	}

	record DocumentContext(UnitState units, List<Integer> hiddenLayers, int displayPrecision,
			boolean significantFigures) {
		DocumentContext {
			hiddenLayers = List.copyOf(hiddenLayers);
		}
	}

	record Stage(Phase phase, String sha256, String documentId, Integer angleUnit,
			DocumentContext context,
			List<GeoCeDGInspectionArchive.Entry> entries,
			List<GeoCeDGInspectionArchive.Declaration> archiveDeclarations,
			List<ObservedObject> objects) {
		Stage(Phase phase, String sha256, String documentId, Integer angleUnit,
				List<GeoCeDGInspectionArchive.Entry> entries, List<ObservedObject> objects) {
			this(phase, sha256, documentId, angleUnit, null, entries, List.of(), objects);
		}

		Stage {
			entries = List.copyOf(entries);
			archiveDeclarations = List.copyOf(archiveDeclarations);
			objects = List.copyOf(objects);
			Set<String> identities = new HashSet<>();
			if (objects.stream().map(ObservedObject::identity).filter(Objects::nonNull)
					.anyMatch(id -> !identities.add(id))) {
				throw new IllegalArgumentException("Duplicate durable identity in inspection stage");
			}
		}
	}

	/** A ref is a locator within one stage. An identity is attached by the native registry. */
	record ObservedObject(String ref, String label, String identity, String copySourceId, String geoClass,
			String producer, String p4Family, List<String> inputs, List<String> outputs,
			String definition, boolean defined, String value, ParameterDomain domain, String presentation) {
		ObservedObject {
			inputs = List.copyOf(inputs);
			outputs = List.copyOf(outputs);
		}
	}

	/** Current native parameter bounds. Hex values preserve the observed floating-point state. */
	record ParameterDomain(String parameterization, String start, String end, String step,
			String orientation) {
	}

	record Observation(Property property, Status status, String before, String after) {
	}

	record ObjectDelta(String beforeRef, String afterRef, List<Observation> observations) {
		ObjectDelta {
			observations = List.copyOf(observations);
		}

		ObjectOutcome outcome() {
			if (afterRef == null) {
				return observations.stream().anyMatch(fact -> fact.status() == Status.KNOWN_OMISSION)
						? ObjectOutcome.OMISSION_PROVEN : ObjectOutcome.NOT_ESTABLISHED;
			}
			return observations.stream().anyMatch(GeoCeDGInspectionResult::structuralChange)
					? ObjectOutcome.RECONSTRUCTED_DIFFERENTLY : ObjectOutcome.CORRESPONDENCE_PROVEN;
		}
	}

	record Addition(String afterRef, String sourceRef, Basis basis) {
	}

	record Assessment(Dimension dimension, Status status, String coverage) {
	}

	record Comparison(Phase before, Phase after, Basis basis, Status status,
			List<ObjectDelta> objects, List<Addition> additions, List<String> unmatchedAfter) {
		Comparison {
			objects = List.copyOf(objects);
			additions = List.copyOf(additions);
			unmatchedAfter = List.copyOf(unmatchedAfter);
		}

		List<String> added() {
			return additions.stream().map(Addition::afterRef).toList();
		}

		List<Assessment> assessments() {
			List<Observation> fields = objects.stream().flatMap(delta -> delta.observations().stream())
					.toList();
			Status incomplete = basis == null ? status : Status.NOT_ESTABLISHED;
			boolean structural = !additions.isEmpty() || fields.stream().anyMatch(fact ->
					structuralChange(fact) || fact.status() == Status.KNOWN_OMISSION);
			boolean presentation = fields.stream().anyMatch(fact -> fact.status() == Status.VERIFIED_CHANGED
					&& (fact.property() == Property.LABEL || fact.property() == Property.PRESENTATION_FIELDS));
			boolean durableChange = fields.stream().anyMatch(fact -> fact.property() == Property.DURABLE_ID
					&& fact.status() == Status.VERIFIED_CHANGED);
			Status persistence = after != Phase.REOPENED ? Status.NOT_EVALUATED
					: durableChange ? Status.VERIFIED_CHANGED : incomplete;
			return List.of(
					new Assessment(Dimension.DOCUMENT_INTEGRITY, incomplete,
							"NATIVE_LOAD_AND_DECLARATIONS_ONLY"),
					new Assessment(Dimension.CONSTRUCTIVE_CORRESPONDENCE,
							structural ? Status.VERIFIED_CHANGED : incomplete,
							"WITNESSED_OBJECTS_AND_ORDERED_PRODUCER_PORTS"),
					new Assessment(Dimension.GEOMETRIC_EQUIVALENCE, incomplete,
							"CURRENT_VALUES_DOMAINS_AND_DEFINEDNESS_ONLY"),
					new Assessment(Dimension.PRESENTATION, presentation ? Status.VERIFIED_CHANGED : incomplete,
							"LABEL_AND_OBSERVED_PRESENTATION_FIELDS"),
					new Assessment(Dimension.PERSISTENCE, persistence,
							"DURABLE_RECORDS_IN_THE_SAME_PERSISTED_UUID"),
					new Assessment(Dimension.PUBLICABILITY, Status.NOT_EVALUATED, "INSPECTION_ONLY"));
		}
	}

	private static boolean structuralChange(Observation fact) {
		return fact.status() == Status.VERIFIED_CHANGED && switch (fact.property()) {
		case CLASS, PRODUCER, INPUTS, OUTPUTS -> true;
		default -> false;
		};
	}

	/**
	 * Compares fields for established pairs. Callers supply only correspondence
	 * and omissions for which their stage-specific evidence is complete.
	 */
	static Comparison compare(Stage before, Stage after, Basis basis,
			Map<String, String> correspondence, Set<String> omissions) {
		return compare(before, after, basis, correspondence, omissions, Set.of(), Map.of());
	}

	static Comparison compare(Stage before, Stage after, Basis basis,
			Map<String, String> correspondence, Set<String> omissions, Set<String> lostIdentities,
			Map<String, String> additionOrigins) {
		Map<String, ObservedObject> indexed = new LinkedHashMap<>();
		after.objects().forEach(object -> indexed.put(object.ref(), object));
		Set<String> fromRefs = new HashSet<>();
		before.objects().forEach(object -> fromRefs.add(object.ref()));
		if (fromRefs.size() != before.objects().size() || indexed.size() != after.objects().size()
				|| !fromRefs.containsAll(correspondence.keySet()) || !fromRefs.containsAll(omissions)
				|| !fromRefs.containsAll(lostIdentities) || !indexed.keySet().containsAll(additionOrigins.keySet())
				|| !indexed.keySet().containsAll(correspondence.values())
				|| new HashSet<>(correspondence.values()).size() != correspondence.size()
				|| lostIdentities.stream().anyMatch(correspondence::containsKey)
				|| correspondence.values().stream().anyMatch(additionOrigins::containsKey)
				|| omissions.stream().anyMatch(correspondence::containsKey)) {
			throw new IllegalArgumentException("Invalid inspection correspondence");
		}
		List<ObjectDelta> changes = new ArrayList<>();
		for (ObservedObject object : before.objects()) {
			ObservedObject target = indexed.get(correspondence.get(object.ref()));
			if (target == null) {
				Status status = omissions.contains(object.ref())
						? Status.KNOWN_OMISSION : Status.NOT_ESTABLISHED;
				List<Observation> evidence = new ArrayList<>();
				evidence.add(new Observation(Property.PRESENCE, status, object.ref(), null));
				if (basis == Basis.LIVE_INSTANCE) {
					evidence.add(new Observation(Property.RUNTIME_INSTANCE,
							Status.VERIFIED_CHANGED, object.ref(), null));
				}
				if (lostIdentities.contains(object.ref())) {
					evidence.add(new Observation(Property.DURABLE_ID, Status.VERIFIED_CHANGED,
							object.identity(), null));
				}
				changes.add(new ObjectDelta(object.ref(), null, evidence));
				continue;
			}
			changes.add(compareObject(object, target, correspondence));
		}
		Set<String> matched = new HashSet<>(correspondence.values());
		List<String> unmatched = new ArrayList<>();
		List<Addition> additions = new ArrayList<>();
		// A runtime-only addition requires retention of every original instance.
		boolean additive = basis == Basis.LIVE_INSTANCE
				&& correspondence.size() == before.objects().size();
		for (String ref : indexed.keySet()) {
			if (matched.contains(ref)) {
				continue;
			}
			String source = additionOrigins.get(ref);
			if (source != null || additive) {
				additions.add(new Addition(ref, source, source == null ? Basis.LIVE_INSTANCE
						: Basis.NATIVE_COPY_TRACE));
			} else {
				unmatched.add(ref);
			}
		}
		boolean changed = !additions.isEmpty()
				|| changes.stream().flatMap(delta -> delta.observations().stream())
						.anyMatch(observation -> observation.status() == Status.VERIFIED_CHANGED
								|| observation.status() == Status.KNOWN_OMISSION);
		// Aggregate equivalence is outside I2's sample coverage.
		return new Comparison(before.phase(), after.phase(), basis,
				changed ? Status.VERIFIED_CHANGED : Status.NOT_ESTABLISHED, changes,
				additions, unmatched);
	}

	private static ObjectDelta compareObject(ObservedObject before, ObservedObject after,
			Map<String, String> correspondence) {
		return new ObjectDelta(before.ref(), after.ref(), List.of(
				new Observation(Property.PRESENCE, Status.VERIFIED_UNCHANGED,
						before.ref(), after.ref()),
				field(Property.LABEL, before.label(), after.label()),
				new Observation(Property.DURABLE_ID,
						Objects.equals(before.identity(), after.identity())
								? Status.VERIFIED_UNCHANGED : Status.VERIFIED_CHANGED,
						before.identity(), after.identity()),
				field(Property.CLASS, before.geoClass(), after.geoClass()),
				field(Property.PRODUCER, before.producer(), after.producer()),
				edges(Property.INPUTS, before.inputs(), after.inputs(), correspondence),
				edges(Property.OUTPUTS, before.outputs(), after.outputs(), correspondence),
				field(Property.DEFINITION_TEXT, before.definition(), after.definition()),
				field(Property.DEFINEDNESS, Boolean.toString(before.defined()),
						Boolean.toString(after.defined())),
				field(Property.CURRENT_VALUE, before.value(), after.value()),
				field(Property.PARAMETER_DOMAIN, Objects.toString(before.domain(), null),
						Objects.toString(after.domain(), null)),
				field(Property.PRESENTATION_FIELDS, before.presentation(), after.presentation())));
	}

	private static Observation field(Property property, String before, String after) {
		Status status = before == null || after == null ? Status.NOT_ESTABLISHED
				: Objects.equals(before, after) ? Status.VERIFIED_UNCHANGED : Status.VERIFIED_CHANGED;
		return new Observation(property, status, before, after);
	}

	private static Observation edges(Property property, List<String> before, List<String> after,
			Map<String, String> correspondence) {
		Status status = before.size() == after.size() ? Status.VERIFIED_UNCHANGED
				: Status.VERIFIED_CHANGED;
		for (int i = 0; i < before.size() && status != Status.VERIFIED_CHANGED; i++) {
			String source = before.get(i);
			String target = after.get(i);
			String expected = source.startsWith("axis:") ? source : correspondence.get(source);
			if (expected == null || target.startsWith("unresolved:")) {
				status = Status.NOT_ESTABLISHED;
			} else if (!expected.equals(target)) {
				status = Status.VERIFIED_CHANGED;
			}
		}
		return new Observation(property, status, before.toString(), after.toString());
	}
}
