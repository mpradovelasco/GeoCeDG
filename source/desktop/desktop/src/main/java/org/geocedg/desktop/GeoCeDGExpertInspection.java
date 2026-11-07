/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.GeoCeDGInspectionResult.compare;
import static org.geocedg.desktop.GeoCeDGInspectionResult.AdmissionState.ADMITTED;
import static org.geocedg.desktop.GeoCeDGInspectionResult.AdmissionState.REJECTED;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Basis.DURABLE_ID_IN_SAME_DOCUMENT;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Basis.LIVE_INSTANCE;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Basis.NATIVE_ARCHIVE_DECLARATION;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Basis.NATIVE_COPY_TRACE;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Phase.RECEIVER;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Phase.REOPENED;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Phase.SOURCE;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Phase.TRIAL;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Status.INVALID;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Status.NOT_ESTABLISHED;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Status.NOT_EVALUATED;
import static org.geocedg.desktop.GeoCeDGInspectionResult.Status.TECHNICAL_FAILURE;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.desktop.GeoCeDGInspectionArchive.Inspection;
import org.geocedg.desktop.GeoCeDGInspectionResult.Admission;
import org.geocedg.desktop.GeoCeDGInspectionResult.Comparison;
import org.geocedg.desktop.GeoCeDGInspectionResult.Diagnostic;
import org.geocedg.desktop.GeoCeDGInspectionResult.DocumentContext;
import org.geocedg.desktop.GeoCeDGInspectionResult.ObservedObject;
import org.geocedg.desktop.GeoCeDGInspectionResult.ParameterDomain;
import org.geocedg.desktop.GeoCeDGInspectionResult.Phase;
import org.geocedg.desktop.GeoCeDGInspectionResult.Stage;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoSequence;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoCurveCartesian;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.main.App;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.gui.MyImageD;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.geogebra.desktop.util.LoggerD;

/** Standalone Desktop worker for the I2 passive inspection policy. */
public final class GeoCeDGExpertInspection {
	private final List<Stage> stages = new ArrayList<>();
	private final List<Diagnostic> diagnostics = new ArrayList<>();
	private final List<Comparison> comparisons = new ArrayList<>();
	private boolean started;
	private Admission admission = new Admission(
			GeoCeDGInspectionResult.AdmissionState.NOT_EVALUATED, "INPUT_UNAVAILABLE", null);
	private ExpertTrial expert;

	record ExpertTrial(boolean available, String reason, String fingerprint, String previewSha256,
			String trialContextSha256) {
	}

	/**
	 * Worker entry point. Launch in a fresh JVM with a private scratch directory.
	 *
	 * @param args source, receiver, private preferences file, new JSON report path,
	 *             optionally the staged live context and a new preview PNG path
	 */
	public static void main(String[] args) {
		if (args.length != 4 && args.length != 6) {
			System.err.println("Expected source, receiver, preferences, report and optionally context and preview");
			System.exit(2);
		}
		try {
			Log.setLogger(new LoggerD());
			GeoGebraPreferencesD.setPropertyFileName(args[2]);
			var context = args.length == 6 ? GeoCeDGExpertContext.read(Path.of(args[4])) : null;
			var preview = args.length == 6 ? Path.of(args[5]) : null;
			var worker = new GeoCeDGExpertInspection();
			GeoCeDGInspectionResult[] result = new GeoCeDGInspectionResult[1];
			SwingUtilities.invokeAndWait(() -> result[0] = worker.inspectExpert(
					Path.of(args[0]), Path.of(args[1]), context, preview));
			Files.writeString(Path.of(args[3]), GeoCeDGInspectionReport.json(result[0], worker.expert) + "\n",
					StandardOpenOption.CREATE_NEW);
			System.out.print(GeoCeDGInspectionReport.summary(result[0], worker.expert));
			System.exit(0);
		} catch (Exception failure) {
			failure.printStackTrace(System.err);
			System.exit(2);
		}
	}

	// Worker-only entry. Desktop must launch a process instead of invoking it in a live JVM.
	GeoCeDGInspectionResult inspect(Path sourcePath, Path receiverPath) {
		return inspectExpert(sourcePath, receiverPath, null, null);
	}

	ExpertTrial expertTrial() {
		return expert;
	}

	GeoCeDGInspectionResult inspectExpert(Path sourcePath, Path receiverPath,
			GeoCeDGExpertContext.Captured context, Path preview) {
		if (started || !SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Inspection requires a new worker session on the EDT");
		}
		started = true;
		if (context != null) {
			expert = new ExpertTrial(false, "NATIVE_INPUT_UNAVAILABLE", null, null, context.sha256());
		}
		Inspection source = capture(SOURCE, sourcePath);
		Inspection receiver = capture(RECEIVER, receiverPath);
		AppGeoCeDG host = null;
		AppGeoCeDG from = null;
		AppGeoCeDG to = null;
		try {
			if ((source != null && source.loadable()) || (receiver != null && receiver.loadable())) {
				host = new AppGeoCeDG(new CommandLineArguments(
						new String[]{"--silent", "--versionCheckAllow=false"}), new JPanel());
				from = load(host, SOURCE, source);
				to = load(host, RECEIVER, receiver);
			}
			boolean parity = true;
			if (context != null && to != null) {
				try {
					context.context().apply(to);
				} catch (IOException mismatch) {
					parity = false;
					diagnostics.add(new Diagnostic(RECEIVER, NOT_ESTABLISHED, "RECEIVER_CONTEXT",
							describe(mismatch)));
					expert = new ExpertTrial(false, "RECEIVER_CONTEXT", null, null, context.sha256());
				}
			}
			Snapshot initial = observe(SOURCE, from, source);
			Snapshot original = observe(RECEIVER, to, receiver);
			if (initial != null && original != null && parity) {
				if (context == null) {
					trial(host, from, to, initial, original);
				} else {
					expertTrial(host, from, to, initial, original, context, preview);
				}
			}
		} catch (Exception failure) {
			diagnostics.add(new Diagnostic(TRIAL, TECHNICAL_FAILURE, "WORKER",
					describe(failure)));
		} finally {
			if (host != null) {
				host.releaseInsertionHelper(from);
				host.releaseInsertionHelper(to);
				host.releaseInsertionHelper(host);
			}
		}
		recordUnrunComparison(SOURCE, TRIAL);
		recordUnrunComparison(RECEIVER, TRIAL);
		recordUnrunComparison(TRIAL, REOPENED);
		return new GeoCeDGInspectionResult(stages, admission, comparisons, diagnostics);
	}

	private Inspection capture(Phase phase, Path path) {
		try {
			return GeoCeDGInspectionArchive.capture(path);
		} catch (IOException failure) {
			diagnostics.add(new Diagnostic(phase, INVALID, "CAPTURE", describe(failure)));
			return null;
		}
	}

	private AppGeoCeDG load(AppGeoCeDG host, Phase phase, Inspection archive) {
		if (archive == null || !archive.loadable()) {
			return null;
		}
		AppGeoCeDG helper = null;
		try {
			helper = host.newInsertionHelper();
			helper.setBlockUpdateScripts(true);
			helper.getEventDispatcher().disableListeners();
			helper.getKernel().setUndoActive(false);
			if (expert != null) {
				helper.restoreMaxLayerUsed(0);
			}
			if (!GFileHandler.loadPreflightedNativeXML(helper, archive.openForNativeLoad())
					|| helper.getXMLio().hasErrors()) {
				throw new IOException("Native reader reported errors");
			}
			helper.setBlockUpdateScripts(true);
			helper.documentReplacementCommitted();
			checkDeclarations(archive, helper);
			return helper;
		} catch (Exception failure) {
			diagnostics.add(new Diagnostic(phase, TECHNICAL_FAILURE, "NATIVE_LOAD",
					describe(failure)));
			host.releaseInsertionHelper(helper);
			return null;
		}
	}

	static void checkDeclarations(Inspection archive, AppGeoCeDG helper)
			throws IOException {
		if (archive.documentId().isPresent()
				&& !archive.documentId().get().equals(helper.getUniqueId())) {
			throw new IOException("Document ID did not survive native load");
		}
		for (var declaration : archive.declarations()) {
			// Use the native XML address only to verify that the reader loaded its declaration.
			GeoElement geo = helper.getKernel().lookupLabel(declaration.label());
			if (geo == null || !declaration.label().equals(geo.getLabelSimple())
					|| !declaration.nativeType().equals(geo.getXMLTypeString())
					|| !Objects.equals(declaration.durableId(), durableId(geo))) {
				throw new IOException("Native declaration mismatch at " + declaration.label());
			}
		}
	}

	private Snapshot observe(Phase phase, AppGeoCeDG app, Inspection archive) {
		if (archive != null) {
			for (var finding : archive.findings()) {
				var status = switch (finding.code()) {
				case INVALID_ARCHIVE, INVALID_XML -> INVALID;
				default -> NOT_EVALUATED;
				};
				diagnostics.add(new Diagnostic(phase, status, finding.code().name(),
						finding.location() + " " + finding.detail()));
			}
		}
		if (app == null) {
			if (archive != null) {
				stages.add(new Stage(phase, archive.sha256(), archive.documentId().orElse(null),
						null, null, archive.entries(), archive.declarations(), List.of()));
			}
			return null;
		}
		Snapshot snapshot = snapshot(phase, app, archive, archive.documentId().orElse(null));
		stages.add(snapshot.stage());
		return snapshot;
	}

	private void trial(AppGeoCeDG host, AppGeoCeDG source, AppGeoCeDG receiver, Snapshot initial,
			Snapshot original) {
		GeoCeDGInsertionAdmission decision = certifiedAdmission(source, receiver);
		if (!decision.isAdmitted()) {
			return;
		}
		if (source.getKernel().getAngleUnit() != receiver.getKernel().getAngleUnit()) {
			diagnostics.add(new Diagnostic(TRIAL, NOT_EVALUATED, "ANGLE_MODE",
					"I2 trial requires identical angular modes"));
			return;
		}
		try {
			GeoCeDGExpertIntegrity.prepareBaseline(source);
			GeoCeDGExpertIntegrity.prepareBaseline(receiver);
			String uuid = receiver.getUniqueId();
			String tube = receiver.getTubeId();
			GeoCeDGInsertionCopyPaste buffer = new GeoCeDGInsertionCopyPaste(Set.of(), true);
			buffer.insertFrom(source, receiver, decision.getDuplicateLabels(), false);
			// F3 restores receiver identity after the native buffer evaluates clipboard XML.
			receiver.setUniqueId(uuid);
			receiver.setTubeId(tube);
			if (receiver.getXMLio().hasErrors()) {
				throw new IOException("Native reader reported errors during the copy");
			}
			Snapshot trial = snapshot(TRIAL, receiver, null, original.stage().documentId());
			stages.add(trial.stage());
			compareTrial(initial, original, trial, buffer.getAddressedCopies());
			if (buffer.getUnresolvedAuxiliaryAddresses() > 0) {
				diagnostics.add(new Diagnostic(TRIAL, NOT_ESTABLISHED, "AUXILIARY_COPY_ADDRESS",
						buffer.getUnresolvedAuxiliaryAddresses()
								+ " clipboard auxiliary addresses have no top-level target"));
			}
			if (trial.refs().keySet().stream().anyMatch(geo -> !original.refs().containsKey(geo))) {
				reopen(host, receiver, trial);
			} else {
				diagnostics.add(new Diagnostic(TRIAL, TECHNICAL_FAILURE, "COPY_NO_ADDITIONS",
						"The native attempt produced no new construction object instances"));
			}
		} catch (Exception failure) {
			diagnostics.add(new Diagnostic(TRIAL, TECHNICAL_FAILURE, "NATIVE_COPY",
					describe(failure)));
		}
	}

	private GeoCeDGInsertionAdmission certifiedAdmission(AppGeoCeDG source, AppGeoCeDG receiver) {
		var decision = GeoCeDGInsertionAdmission.admit(receiver, source);
		admission = decision.isAdmitted() ? new Admission(ADMITTED, "P4", null)
				: new Admission(REJECTED, decision.getReasonKey(), decision.getReasonArgument());
		return decision;
	}

	private void expertTrial(AppGeoCeDG host, AppGeoCeDG source, AppGeoCeDG receiver, Snapshot initial,
			Snapshot original, GeoCeDGExpertContext.Captured context, Path preview) {
		AppGeoCeDG reopened = null;
		String condition = "NATIVE_EXPERT_COPY";
		try {
			certifiedAdmission(source, receiver);
			GeoCeDGExpertIntegrity.prepareBaseline(source);
			GeoCeDGExpertIntegrity.prepareBaseline(receiver);
			var result = GeoCeDGExpertIntegrity.copy(source, receiver);
			Snapshot trial = snapshot(TRIAL, receiver, null, original.stage().documentId());
			stages.add(trial.stage());
			compareTrial(initial, original, trial, result.copies());
			condition = "NATIVE_EXPERT_ROUND_TRIP";
			var archive = GeoCeDGInspectionArchive.inspect(GeoCeDGExpertIntegrity.nativeArchive(receiver));
			reopened = load(host, REOPENED, archive);
			Snapshot roundTrip = observe(REOPENED, reopened, archive);
			if (roundTrip == null) {
				throw new IOException("The trial archive did not reopen successfully");
			}
			Map<GeoElement, GeoElement> pairs = GeoCeDGExpertIntegrity.verifyReopened(receiver, reopened, archive);
			comparisons.add(compare(trial.stage(), roundTrip.stage(), NATIVE_ARCHIVE_DECLARATION,
					mapInstances(trial, roundTrip, pairs::get), Set.of()));
			String previewSha = writePreview(receiver, preview);
			expert = new ExpertTrial(true, "PASSIVE_NATIVE_REPLAY", result.fingerprint(), previewSha,
					context.sha256());
		} catch (Exception failure) {
			Phase phase = "NATIVE_EXPERT_ROUND_TRIP".equals(condition) ? REOPENED : TRIAL;
			var status = failure instanceof GeoCeDGExpertIntegrity.MalformedGeometry
					? INVALID : TECHNICAL_FAILURE;
			diagnostics.add(new Diagnostic(phase, status, condition, describe(failure)));
			expert = new ExpertTrial(false, condition, null, null, context.sha256());
		} finally {
			host.releaseInsertionHelper(reopened);
		}
	}

	private String writePreview(AppGeoCeDG receiver, Path preview) {
		if (preview != null) {
			try {
				if (receiver.getActiveEuclidianView() instanceof EuclidianViewD view
						&& (view.getWidth() <= 0 || view.getHeight() <= 0)) {
					var settings = view.getSettings();
					view.setSize(new Dimension(previewSize(settings.getFileWidth(), 640),
							previewSize(settings.getFileHeight(), 480)));
					view.updateSize();
				}
				var export = (MyImageD) receiver.getExportImage(640, 480);
				if (export.getImage() instanceof BufferedImage image) {
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					if (ImageIO.write(image, "PNG", bytes) && bytes.size() <= 1024 * 1024) {
						byte[] png = bytes.toByteArray();
						Files.write(preview, png, StandardOpenOption.CREATE_NEW);
						return GeoCeDGInspectionArchive.digest(png);
					}
				}
			} catch (Exception unavailable) {
				diagnostics.add(new Diagnostic(TRIAL, NOT_EVALUATED, "GRAPHIC_PREVIEW",
						describe(unavailable)));
				return null;
			}
		}
		diagnostics.add(new Diagnostic(TRIAL, NOT_EVALUATED, "GRAPHIC_PREVIEW",
				"Native graphical preview unavailable; see the constructive trial"));
		return null;
	}

	private static int previewSize(int saved, int fallback) {
		return saved > 0 ? Math.min(saved, 1600) : fallback;
	}

	private void compareTrial(Snapshot initial, Snapshot original, Snapshot trial,
			Map<GeoElement, GeoElement> copies) {
		Map<String, String> retained = mapInstances(original, trial, Function.identity());
		Set<String> trialIds = new LinkedHashSet<>();
		trial.stage().objects().stream().filter(geo -> geo.identity() != null)
				.forEach(geo -> trialIds.add(geo.identity()));
		Set<String> lostIdentities = new LinkedHashSet<>();
		original.stage().objects().stream()
				.filter(geo -> geo.identity() != null && !trialIds.contains(geo.identity()))
				.forEach(geo -> lostIdentities.add(geo.ref()));
		// Retention and loss of a durable record are separate facts about the same instance.
		lostIdentities.removeAll(retained.keySet());
		Map<String, String> inserted = mapInstances(initial, trial, copies::get);
		Set<String> retainedOutputs = new LinkedHashSet<>(retained.values());
		inserted.values().removeIf(retainedOutputs::contains);
		Map<String, String> origins = new LinkedHashMap<>();
		inserted.forEach((sourceRef, targetRef) -> origins.put(targetRef, sourceRef));
		comparisons.add(compare(original.stage(), trial.stage(), LIVE_INSTANCE, retained, Set.of(),
				lostIdentities, origins));
		comparisons.add(compare(initial.stage(), trial.stage(), NATIVE_COPY_TRACE,
				inserted, Set.of()));
		if (inserted.size() != initial.refs().size()) {
			List<String> unresolved = initial.stage().objects().stream().map(ObservedObject::ref)
					.filter(ref -> !inserted.containsKey(ref)).toList();
			diagnostics.add(new Diagnostic(TRIAL, NOT_ESTABLISHED, "COPY_TRACE_INCOMPLETE",
					"Source references without a native copy witness: " + unresolved));
		}
	}

	private void reopen(AppGeoCeDG host, AppGeoCeDG receiver, Snapshot trial) {
		AppGeoCeDG copy = null;
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			receiver.getXMLio().writeGeoGebraFile(bytes, false);
			Inspection archive = GeoCeDGInspectionArchive.inspect(bytes.toByteArray());
			copy = load(host, REOPENED, archive);
			Snapshot reopened = observe(REOPENED, copy, archive);
			if (reopened != null) {
				comparisons.add(compareDurable(trial.stage(), reopened.stage()));
			}
		} catch (Exception failure) {
			diagnostics.add(new Diagnostic(REOPENED, TECHNICAL_FAILURE, "NATIVE_ROUND_TRIP",
					describe(failure)));
		} finally {
			host.releaseInsertionHelper(copy);
		}
	}

	static Comparison compareDurable(Stage before, Stage after) {
		if (before.angleUnit() == null || after.angleUnit() == null) {
			return new Comparison(before.phase(), after.phase(), null, NOT_EVALUATED,
					List.of(), List.of(), List.of());
		}
		Map<String, String> correspondence = new LinkedHashMap<>();
		Set<String> lostIdentities = new LinkedHashSet<>();
		if (before.documentId() != null && before.documentId().equals(after.documentId())) {
			Map<String, String> targetIds = new HashMap<>();
			after.objects().stream().filter(geo -> geo.identity() != null)
					.forEach(geo -> targetIds.put(geo.identity(), geo.ref()));
			for (ObservedObject geo : before.objects()) {
				if (geo.identity() != null) {
					String target = targetIds.get(geo.identity());
					if (target == null) {
						lostIdentities.add(geo.ref());
					} else {
						correspondence.put(geo.ref(), target);
					}
				}
			}
		}
		return compare(before, after, DURABLE_ID_IN_SAME_DOCUMENT, correspondence, Set.of(),
				lostIdentities, Map.of());
	}

	private void recordUnrunComparison(Phase from, Phase to) {
		if (comparisons.stream().noneMatch(row -> row.before() == from && row.after() == to)) {
			var status = diagnostics.stream().anyMatch(diagnostic -> diagnostic.phase() == to
					&& diagnostic.status() == TECHNICAL_FAILURE) ? TECHNICAL_FAILURE : NOT_EVALUATED;
			comparisons.add(new Comparison(from, to, null, status,
					List.of(), List.of(), List.of()));
		}
	}

	private record Snapshot(Stage stage, Map<GeoElement, String> refs) {
	}

	private static Map<String, String> mapInstances(Snapshot from, Snapshot to,
			Function<GeoElement, GeoElement> match) {
		Map<String, String> result = new LinkedHashMap<>();
		from.refs().forEach((geo, ref) -> {
			String target = to.refs().get(match.apply(geo));
			if (target != null) {
				result.put(ref, target);
			}
		});
		return result;
	}

	private static Snapshot snapshot(Phase phase, AppGeoCeDG app, Inspection archive,
			String documentId) {
		Kernel kernel = app.getKernel();
		List<GeoElement> geos = new ArrayList<>(
				kernel.getConstruction().getGeoSetWithCasCellsConstructionOrder());
		Map<GeoElement, String> refs = new IdentityHashMap<>();
		for (GeoElement geo : geos) {
			refs.put(geo, "g" + (refs.size() + 1));
		}
		Function<GeoElement, String> endpoint = geo -> {
			if (geo == kernel.getXAxis()) {
				return "axis:x";
			}
			if (geo == kernel.getYAxis()) {
				return "axis:y";
			}
			String reference = refs.get(geo);
			if (reference != null) {
				return reference;
			}
			// A sample of an anonymous operand is evidence, never an identity witness.
			return geo == null ? "unresolved:null"
					: "unresolved:" + geo.getGeoClassType() + ":" + currentValue(geo, 0);
		};
		List<ObservedObject> observations = geos.stream()
				.map(geo -> observeObject(geo, refs.get(geo), endpoint)).toList();
		var context = new DocumentContext(app.getDocumentUnits().getState(),
				app.getLayerWorkspace().getHiddenLayers(), kernel.useSignificantFigures
						? kernel.getPrintFigures() : kernel.getPrintDecimals(),
				kernel.useSignificantFigures, app.getLayerWorkspace().getWorkingLayer(),
				app.getMaxLayerUsed(), app.getActiveEuclidianView().getViewID());
		return new Snapshot(new Stage(phase, archive == null ? null : archive.sha256(),
				documentId, kernel.getAngleUnit(), context,
				archive == null ? List.of() : archive.entries(),
				archive == null ? List.of() : archive.declarations(),
				observations), refs);
	}

	private static ObservedObject observeObject(GeoElement geo, String ref,
			Function<GeoElement, String> endpoint) {
		AlgoElement parent = geo.getParentAlgorithm();
		var family = GeoCeDGInsertionAdmission.classify(geo);
		PersistentGeoId id = persistentId(geo);
		String lineage = null;
		if (id != null && geo.getConstruction().getSpatialIdentityRegistry()
				.getRecord(id) instanceof GeoIdentityRecord record && record.getCopySourceId() != null) {
			lineage = record.getCopySourceId().toExternalForm();
		}
		return new ObservedObject(ref, geo.getLabelSimple(), id == null ? null : id.toExternalForm(),
				lineage,
				geo.getClass().getName(), parent == null ? "independent" : parent.getClass().getName(),
				family == null ? null : family.name(),
				parent == null ? List.of() : GeoCeDGExpertIntegrity.observedInputs(parent).stream()
						.map(endpoint).toList(),
				parent == null ? List.of() : Arrays.stream(parent.getOutput()).map(endpoint).toList(),
				geo.getDefinition(StringTemplate.xmlTemplate), geo.isDefined(), currentValue(geo, 0),
				parameterDomain(geo, parent), presentation(geo));
	}

	private static ParameterDomain parameterDomain(GeoElement geo, AlgoElement parent) {
		if (!geo.isDefined()) {
			return null;
		}
		if (geo instanceof GeoConicPart conic) {
			return new ParameterDomain("CONIC_PART", Double.toHexString(conic.getParameterStart()),
					Double.toHexString(conic.getParameterEnd()), null,
					conic.positiveOrientation() ? "POSITIVE" : "NEGATIVE");
		}
		if (geo instanceof GeoCurveCartesian curve) {
			return new ParameterDomain("CARTESIAN_CURVE", Double.toHexString(curve.getMinParameter()),
					Double.toHexString(curve.getMaxParameter()), null, null);
		}
		if (parent instanceof AlgoSequence) {
			GeoElement[] inputs = parent.getInput();
			return new ParameterDomain("SEQUENCE", currentValue(inputs[2], 0), currentValue(inputs[3], 0),
					inputs.length == 4 ? Double.toHexString(1.0) : currentValue(inputs[4], 0), null);
		}
		return null;
	}

	private static String durableId(GeoElement geo) {
		PersistentGeoId id = persistentId(geo);
		return id == null ? null : id.toExternalForm();
	}

	private static PersistentGeoId persistentId(GeoElement geo) {
		return geo.getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(geo);
	}

	static String currentValue(GeoElement geo, int depth) {
		if (!geo.isDefined()) {
			return null;
		}
		if (geo instanceof GeoPoint point) {
			return Double.toHexString(point.getInhomX()) + ","
					+ Double.toHexString(point.getInhomY());
		}
		if (geo instanceof GeoNumeric number) {
			return Double.toHexString(number.getDouble());
		}
		if (geo instanceof GeoBoolean bool) {
			return Boolean.toString(bool.getBoolean());
		}
		if (geo instanceof GeoText text) {
			return text.getTextString();
		}
		if (geo instanceof GeoConicPart arc) {
			return "radius=" + Double.toHexString(arc.getHalfAxis(0))
					+ ",arcLength=" + Double.toHexString(arc.getArcLength());
		}
		if (geo instanceof GeoCurveCartesian curve) {
			double min = curve.getMinParameter();
			double max = curve.getMaxParameter();
			double[] coords = new double[2];
			StringBuilder samples = new StringBuilder();
			for (double parameter : new double[]{min, min / 2 + max / 2, max}) {
				curve.evaluateCurve(parameter, coords);
				samples.append(Double.toHexString(parameter)).append('=')
						.append(Double.toHexString(coords[0])).append(',')
						.append(Double.toHexString(coords[1])).append(' ');
			}
			return samples.toString();
		}
		return geo instanceof GeoList list ? listValue(list, depth) : null;
	}

	private static String listValue(GeoList list, int depth) {
		if (depth >= 2 || list.size() > 64) {
			return null;
		}
		StringBuilder value = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			GeoElement item = list.get(i);
			String sample = currentValue(item, depth + 1);
			if (sample == null) {
				return null;
			}
			value.append(item.getGeoClassType()).append(':').append(sample.length())
					.append(':').append(sample);
		}
		return value.toString();
	}

	static String presentation(GeoElement geo) {
		return presentation(geo, true);
	}

	static String presentation(GeoElement geo, boolean includeRequestedVisibility) {
		return "layer=" + geo.getLayer()
				+ (includeRequestedVisibility ? ",show=" + geo.isSetEuclidianVisible() : "")
				+ ",algebra=" + geo.isAlgebraVisible() + ",label=" + geo.isLabelVisible()
				+ ",labelMode=" + geo.getLabelMode() + ",argb=" + geo.getObjectColor().getARGB()
				+ ",view1=" + geo.isVisibleInView(App.VIEW_EUCLIDIAN)
				+ ",view2=" + geo.isVisibleInView(App.VIEW_EUCLIDIAN2)
				+ ",caption=" + geo.getRawCaption()
				+ ",line=" + geo.getLineType() + ",weight=" + geo.getLineThickness()
				+ (geo instanceof GeoPoint point ? ",pointStyle=" + point.getPointStyle()
						+ ",pointSize=" + point.getPointSize() : "")
				+ (geo instanceof GeoAngle angle ? ",angleStyle=" + angle.getAngleStyle() : "")
				+ (geo instanceof GeoText text ? ",fontStyle=" + text.getFontStyle()
						+ ",fontSize=" + text.getFontSizeMultiplier() : "");
	}

	private static String describe(Exception failure) {
		return failure.getClass().getSimpleName() + ": " + failure.getMessage();
	}
}
