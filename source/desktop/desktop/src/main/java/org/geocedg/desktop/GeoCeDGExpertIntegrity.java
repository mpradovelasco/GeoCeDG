/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Locateable;
import org.geogebra.common.kernel.algos.AlgoConicPartCircle;
import org.geogebra.common.kernel.algos.AlgoDependentBoolean;
import org.geogebra.common.kernel.algos.AlgoDependentEquationList;
import org.geogebra.common.kernel.algos.AlgoDependentFunction;
import org.geogebra.common.kernel.algos.AlgoDependentFunctionNVar;
import org.geogebra.common.kernel.algos.AlgoDependentListExpression;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.algos.AlgoDependentText;
import org.geogebra.common.kernel.algos.AlgoDependentVector;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoSequence;
import org.geogebra.common.kernel.algos.DependentAlgo;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoCurveCartesian;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.headless.GFileHandler;

/** Expert-only native transport and integrity checks, shared by rehearsal and commit. */
final class GeoCeDGExpertIntegrity {
	static final String VERSION = "POST_CU4_EXPERT_4";
	private static final int MAX_GRAPH_DEPTH = 160;
	private static final int MAX_GRAPH_NODES = 4096;
	// These producers populate getInput() via setInputFrom(), which enumerates
	// a HashSet of variables rather than ordered command ports.
	private static final Set<Class<? extends AlgoElement>> EXPRESSION_INPUT_SETS = Set.of(
			AlgoDependentBoolean.class, AlgoDependentEquationList.class, AlgoDependentFunction.class,
			AlgoDependentFunctionNVar.class, AlgoDependentListExpression.class, AlgoDependentNumber.class,
			AlgoDependentPoint.class, AlgoDependentText.class, AlgoDependentVector.class);

	private GeoCeDGExpertIntegrity() {
	}

	record CopyResult(Map<GeoElement, GeoElement> copies, String fingerprint) {
	}

	static final class MalformedGeometry extends IOException {
		MalformedGeometry(String message) {
			super(message);
		}
	}

	static AppGeoCeDG loadSource(AppGeoCeDG host, byte[] bytes) throws IOException {
		var archive = GeoCeDGInspectionArchive.inspect(bytes);
		if (!archive.loadable()) {
			if (archive.findings().stream().anyMatch(finding ->
					finding.code() == GeoCeDGInspectionArchive.Code.CONTROL_LAYOUT)) {
				throw new IOException(host.layerText("Insert.Reject.ControlLayoutSource"));
			}
			throw new IOException("The source is outside the passive native envelope: "
					+ archive.findings());
		}
		AppGeoCeDG source = host.newInsertionHelper();
		boolean success = false;
		try {
			source.setBlockUpdateScripts(true);
			source.getEventDispatcher().disableListeners();
			source.getKernel().setUndoActive(false);
			source.restoreMaxLayerUsed(0);
			if (!GFileHandler.loadPreflightedNativeXML(source, archive.openForNativeLoad())
					|| source.getXMLio().hasErrors()) {
				throw new IOException("Native source reader reported errors");
			}
			source.setBlockUpdateScripts(true);
			source.documentReplacementCommitted();
			GeoCeDGExpertInspection.checkDeclarations(archive, source);
			verifyPassiveDocument(source);
			prepareBaseline(source);
			success = true;
			return source;
		} catch (XMLParseException malformed) {
			throw new IOException("Native source XML failed to load", malformed);
		} finally {
			if (!success) {
				releaseSource(host, source);
			}
		}
	}

	static void releaseSource(AppGeoCeDG host, AppGeoCeDG source) {
		if (source != null) {
			try {
				var undo = (GeoCeDGUndoManager) source.getUndoManager();
				for (var command : undo.detach().getCommands()) {
					command.delete();
				}
			} finally {
				host.releaseInsertionHelper(source);
			}
		}
	}

	static void prepareBaseline(AppGeoCeDG app) throws IOException {
		GeoCeDGUndoManager undo = (GeoCeDGUndoManager) app.getUndoManager();
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
	}

	static CopyResult copy(AppGeoCeDG source, AppGeoCeDG receiver) throws IOException {
		verifyPassiveDocument(source);
		verifyPassiveDocument(receiver);
		if (source.getKernel().getAngleUnit() != receiver.getKernel().getAngleUnit()) {
			throw new IOException("Angular modes differ and no expert conversion is available");
		}
		var resourcesBefore = receiverResources(receiver);
		List<GeoElement> original = objects(receiver);
		Map<GeoElement, String> retained = new IdentityHashMap<>();
		for (GeoElement geo : original) {
			retained.put(geo, residentState(geo));
		}
		var receiverKernel = receiver.getKernel();
		var pathMode = receiverKernel.usePathAndRegionParameters;
		var construction = receiverKernel.getConstruction();
		var unsetSteps = construction.getConstructionDefaults().getDefaultGeos().stream()
				.map(Map.Entry::getValue).filter(geo -> geo.getAnimationStepObject() == null).toList();
		var units = receiver.getDocumentUnits().getState();
		var hidden = receiver.getLayerWorkspace().getHiddenLayers();
		int working = receiver.getLayerWorkspace().getWorkingLayer();
		int activeView = receiver.getActiveEuclidianView().getViewID();
		String uuid = receiver.getUniqueId();
		String tube = receiver.getTubeId();
		Set<String> reservations = construction.snapshotProtectedLabels();
		List<GeoElement> from = objects(source);
		if (from.isEmpty()) {
			throw new IOException("The source has no construction objects to insert");
		}
		Set<String> duplicates = new HashSet<>();
		for (GeoElement geo : from) {
			if (receiverKernel.lookupLabel(geo.getLabelSimple()) != null) {
				duplicates.add(geo.getLabelSimple());
			}
		}
		var buffer = new GeoCeDGInsertionCopyPaste(Set.of(), true, true);
		try {
			buffer.insertFrom(source, receiver, duplicates, false);
		} finally {
			// Copying a visual style can lazily initialize a prototype's animation step.
			// Restore the original unset value before comparing the exact native defaults.
			for (GeoElement prototype : unsetSteps) {
				if (prototype.getAnimationStepObject() instanceof MyDouble step
						&& step.getDouble() == GeoNumeric.DEFAULT_SLIDER_INCREMENT) {
					prototype.setAnimationStep((NumberValue) null);
				}
			}
			construction.restoreProtectedLabels(reservations);
			receiver.setUniqueId(uuid);
			receiver.setTubeId(tube);
		}
		List<GeoElement> after = objects(receiver);
		Map<GeoElement, GeoElement> copies = buffer.getAddressedCopies();
		Set<GeoElement> accounted = Collections.newSetFromMap(new IdentityHashMap<>());
		accounted.addAll(original);
		boolean newOutputs = copies.values().stream().allMatch(accounted::add);
		if (copies.size() != from.size() || !copies.keySet().containsAll(from)
				|| !newOutputs || accounted.size() != after.size() || !accounted.containsAll(after)) {
			throw new IOException("Native copy did not witness exactly one distinct result per source object");
		}
		for (var entry : retained.entrySet()) {
			if (!entry.getValue().equals(residentState(entry.getKey()))) {
				throw new IOException("The native copy changed resident object " + entry.getKey().getLabelSimple());
			}
		}
		if (!units.equals(receiver.getDocumentUnits().getState())
				|| !hidden.equals(receiver.getLayerWorkspace().getHiddenLayers())
				|| working != receiver.getLayerWorkspace().getWorkingLayer()
				|| activeView != receiver.getActiveEuclidianView().getViewID()
				|| pathMode != receiverKernel.usePathAndRegionParameters) {
			throw new IOException("The native copy changed receiver document context");
		}
		verifyMappedGraph(source, receiver, copies);
		verifyPassiveDocument(receiver);
		if (!resourcesBefore.equals(receiverResources(receiver))) {
			throw new IOException("Native copy changed receiver defaults or resources");
		}
		return new CopyResult(copies, stateFingerprint(receiver));
	}

	static byte[] nativeArchive(AppGeoCeDG app) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		app.getXMLio().writeGeoGebraFile(bytes, false);
		return bytes.toByteArray();
	}

	static String fingerprint(AppGeoCeDG app, Map<String, String> expectedResources) throws IOException {
		if (!expectedResources.equals(receiverResources(app))) {
			throw new IOException("Receiver defaults or resources differ before history publication");
		}
		return stateFingerprint(app);
	}

	static Map<String, String> receiverResources(AppGeoCeDG app) throws IOException {
		return nonConstructionMembers(passiveArchive(app));
	}

	private static GeoCeDGInspectionArchive.Inspection passiveArchive(AppGeoCeDG app) throws IOException {
		var archive = GeoCeDGInspectionArchive.inspect(nativeArchive(app));
		if (!archive.loadable()) {
			throw new IOException("The result cannot be written within the passive native envelope: "
					+ archive.findings());
		}
		GeoCeDGExpertInspection.checkDeclarations(archive, app);
		return archive;
	}

	private static Map<String, String> nonConstructionMembers(GeoCeDGInspectionArchive.Inspection archive) {
		Map<String, String> result = new TreeMap<>();
		for (var entry : archive.entries()) {
			if (!MyXMLio.XML_FILE.equals(entry.name())) {
				result.put(entry.name(), entry.sha256());
			}
		}
		return result;
	}

	private static String stateFingerprint(AppGeoCeDG app) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (var out = new DataOutputStream(bytes)) {
			var context = GeoCeDGExpertContext.capture(app);
			field(out, VERSION);
			field(out, app.getUniqueId());
			out.writeInt(app.getKernel().getAngleUnit());
			field(out, app.getKernel().usePathAndRegionParameters.name());
			out.writeBoolean(app.getKernel().useSignificantFigures);
			out.writeInt(app.getKernel().useSignificantFigures ? app.getKernel().getPrintFigures()
					: app.getKernel().getPrintDecimals());
			out.writeInt(context.workingLayer());
			out.writeInt(context.maxLayer());
			out.writeInt(context.activeView());
			out.writeBoolean(context.secondViewShowing());
			out.writeBoolean(context.thirdViewInitialized());
			out.writeInt(context.protectedLabels().size());
			for (String reservation : context.protectedLabels()) {
				field(out, reservation);
			}
			field(out, app.getLayerWorkspace().getHiddenLayers().toString());
			field(out, constructionXml(app));
			for (GeoElement geo : objects(app)) {
				field(out, residentState(geo));
			}
		}
		return GeoCeDGInspectionArchive.digest(bytes.toByteArray());
	}

	private static void field(DataOutputStream out, String value) throws IOException {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	private static String constructionXml(AppGeoCeDG app) {
		XMLStringBuilder builder = new XMLStringBuilder();
		app.getKernel().getConstruction().getConstructionXML(builder, false);
		return builder.toString();
	}

	static Map<GeoElement, GeoElement> verifyReopened(AppGeoCeDG trial, AppGeoCeDG reopened,
			GeoCeDGInspectionArchive.Inspection archive) throws IOException {
		Map<GeoElement, GeoElement> pairs = new IdentityHashMap<>();
		if (!trial.getUniqueId().equals(reopened.getUniqueId())
				|| !trial.getLayerWorkspace().getHiddenLayers().equals(
						reopened.getLayerWorkspace().getHiddenLayers())
				|| trial.getKernel().getAngleUnit() != reopened.getKernel().getAngleUnit()
				|| trial.getKernel().usePathAndRegionParameters
						!= reopened.getKernel().usePathAndRegionParameters
				|| !trial.getDocumentUnits().getState().equals(reopened.getDocumentUnits().getState())
				|| !constructionXml(trial).equals(constructionXml(reopened))) {
			throw new IOException("Native reopening changed the document construction or context");
		}
		// Match each writer/reader declaration in this exact archive. Its XML address
		// is local to the round trip and never treated as a durable identity.
		for (var declaration : archive.declarations()) {
			GeoElement before = trial.getKernel().lookupLabel(declaration.label());
			GeoElement after = reopened.getKernel().lookupLabel(declaration.label());
			if (before == null || after == null || !samePersistedState(before, after)) {
				throw new IOException("Native reopening changed object " + declaration.label());
			}
			pairs.put(before, after);
		}
		if (pairs.size() != objects(trial).size() || pairs.size() != objects(reopened).size()) {
			throw new IOException("Native reopening did not account for every construction object");
		}
		verifyMappedGraph(trial, reopened, pairs);
		verifyPassiveDocument(reopened);
		if (!nonConstructionMembers(archive).equals(nonConstructionMembers(passiveArchive(reopened)))) {
			throw new IOException("Native reopening changed the trial defaults or resources");
		}
		return pairs;
	}

	private static String residentState(GeoElement geo) {
		return residentState(geo, true);
	}

	private static boolean samePersistedState(GeoElement before, GeoElement after) {
		// The native writer does not persist the requested graphic visibility of
		// a non-drawable numeric sequence. Its effective visibility cannot change.
		// I2 still reports the requested flag before and after reopening.
		boolean includeShow = !(nonDrawableNumericSequence(before) && nonDrawableNumericSequence(after));
		return residentState(before, includeShow).equals(residentState(after, includeShow));
	}

	private static boolean nonDrawableNumericSequence(GeoElement geo) {
		return geo instanceof GeoList list && !list.isDrawable()
				&& list.getParentAlgorithm() instanceof AlgoSequence sequence
				&& sequence.getInput()[0] instanceof GeoNumeric;
	}

	private static String residentState(GeoElement geo, boolean includeRequestedVisibility) {
		return geo.getClass().getName() + "\n" + geo.getXML() + "\n" + geo.isDefined() + "\n"
				+ GeoCeDGExpertInspection.currentValue(geo, 0) + "\n"
				+ GeoCeDGExpertInspection.presentation(geo, includeRequestedVisibility);
	}

	private static List<GeoElement> objects(AppGeoCeDG app) {
		return List.copyOf(app.getKernel().getConstruction().getGeoSetWithCasCellsConstructionOrder());
	}

	static void verifyPassiveDocument(AppGeoCeDG app) throws IOException {
		Kernel kernel = app.getKernel();
		var registry = kernel.getConstruction().getSpatialIdentityRegistry();
		if (!registry.isEmpty() || registry.hasDocumentAuthorityRecord()) {
			throw new IOException("Spatial identity remapping and recovery are not verified for expert insertion");
		}
		if (kernel.getMacroNumber() != 0) {
			throw new IOException("Macro closure and recovery are not verified for expert insertion");
		}
		List<GeoElement> geos = objects(app);
		Map<GeoElement, GeoElement> self = new IdentityHashMap<>();
		for (GeoElement geo : geos) {
			if (geo.getLabelSimple() == null || kernel.lookupLabel(geo.getLabelSimple()) != geo) {
				throw new IOException("Construction object has no unique native address");
			}
			checkGeometry(geo);
			self.put(geo, geo);
		}
		verifyMappedGraph(app, app, self);
	}

	private static void checkGeometry(GeoElement geo) throws IOException {
		if (!geo.isDefined()) {
			// Native undefinedness is a valid state. A defined source becoming
			// undefined during transfer is checked separately at the paired port.
			return;
		}
		if (geo instanceof GeoNumeric number && Double.isNaN(number.getDouble())
				|| geo instanceof GeoCurveCartesian curve && (!Double.isFinite(curve.getMinParameter())
						|| !Double.isFinite(curve.getMaxParameter())
						|| curve.getMinParameter() > curve.getMaxParameter())) {
			throw new MalformedGeometry("Malformed geometry or parameter domain at " + geo.getLabelSimple());
		}
		if (geo instanceof GeoConicPart arc && geo.getParentAlgorithm() instanceof AlgoConicPartCircle
				&& arc.getConicPartType() == GeoConicNDConstants.CONIC_PART_ARC) {
			checkCircularArc(arc);
		}
	}

	private static void checkCircularArc(GeoConicPart arc) throws IOException {
		GeoElement[] in = arc.getParentAlgorithm().getInput();
		if (in.length != 3 || !(in[0] instanceof GeoPoint a) || !(in[1] instanceof GeoPoint b)
				|| !(in[2] instanceof GeoPoint c) || !a.isFinite() || !b.isFinite() || !c.isFinite()) {
			throw new MalformedGeometry("Circular arc lacks three finite defining points");
		}
		double ux = b.getInhomX() - a.getInhomX();
		double uy = b.getInhomY() - a.getInhomY();
		double vx = c.getInhomX() - a.getInhomX();
		double vy = c.getInhomY() - a.getInhomY();
		double radius = Math.hypot(ux, uy);
		double end = Math.hypot(vx, vy);
		double expected = Math.atan2((ux / radius) * (vy / end) - (uy / radius) * (vx / end),
				(ux / radius) * (vx / end) + (uy / radius) * (vy / end));
		if (expected < 0) {
			expected += 2 * Math.PI;
		}
		double extent = arc.getParameterExtent();
		if (!arc.isCircle() || !arc.positiveOrientation() || !(radius > 0) || !(end > 0)
				|| !Double.isFinite(extent) || !Double.isFinite(arc.getArcLength())
				|| !(Math.abs(arc.getHalfAxis(0) / radius - 1) <= 1e-9)
				|| !(Math.abs(extent - expected) <= 1e-9)
				|| !(Math.abs(arc.getArcLength() / radius - extent) <= 1e-9)) {
			throw new MalformedGeometry("Circular arc support, orientation or domain is inconsistent at "
					+ arc.getLabelSimple());
		}
	}

	private static void verifyMappedGraph(AppGeoCeDG source, AppGeoCeDG target,
			Map<GeoElement, GeoElement> topLevel) throws IOException {
		var graph = new Graph(source.getKernel(), target.getKernel(), topLevel);
		for (var pair : topLevel.entrySet()) {
			graph.visit(pair.getKey(), pair.getValue(), 0);
		}
	}

	static List<GeoElement> observedInputs(AlgoElement producer) {
		if (EXPRESSION_INPUT_SETS.contains(producer.getClass())) {
			List<GeoElement> occurrences = new ArrayList<>();
			var expression = ((DependentAlgo) producer).getExpression();
			if (expression != null) {
				for (var part : expression) {
					if (part instanceof GeoElement geo) {
						occurrences.add(geo);
					}
				}
			}
			return occurrences;
		}
		return nativeInputs(producer);
	}

	private static List<GeoElement> nativeInputs(AlgoElement producer) {
		return producer.getInput() == null ? List.of() : Arrays.asList(producer.getInput());
	}

	private static Set<GeoElement> instances(List<GeoElement> geos) {
		Set<GeoElement> set = Collections.newSetFromMap(new IdentityHashMap<>());
		set.addAll(geos);
		return set;
	}

	private static List<GeoElement> verifiedInputs(AlgoElement producer) throws IOException {
		List<GeoElement> inputs = observedInputs(producer);
		if (EXPRESSION_INPUT_SETS.contains(producer.getClass())
				&& !instances(inputs).equals(instances(nativeInputs(producer)))) {
			throw new IOException("Native dependency set differs from the expression's bound operands");
		}
		return inputs;
	}

	private static final class Graph {
		private final Kernel source;
		private final Kernel target;
		private final Map<GeoElement, GeoElement> pairs = new IdentityHashMap<>();
		private final Map<GeoElement, GeoElement> inverse = new IdentityHashMap<>();
		private final Set<GeoElement> active = Collections.newSetFromMap(new IdentityHashMap<>());
		private final Set<GeoElement> verified = Collections.newSetFromMap(new IdentityHashMap<>());

		Graph(Kernel source, Kernel target, Map<GeoElement, GeoElement> topLevel) {
			this.source = source;
			this.target = target;
			pairs.putAll(topLevel);
			topLevel.forEach((from, to) -> inverse.put(to, from));
		}

		void visit(GeoElement from, GeoElement to, int depth) throws IOException {
			verifyEndpoint(from, to);
			if (verified.contains(from)) {
				return;
			}
			if (depth >= MAX_GRAPH_DEPTH || pairs.size() > MAX_GRAPH_NODES || !active.add(from)) {
				throw new IOException("Construction graph is cyclic or exceeds its expert budget");
			}
			var producer = from.getParentAlgorithm();
			var rebuilt = to.getParentAlgorithm();
			if (producer == null ? rebuilt != null : rebuilt == null || producer.getClass() != rebuilt.getClass()) {
				throw new IOException("Producer changed at " + from.getLabelSimple());
			}
			if (producer != null) {
				var inputs = verifiedInputs(producer);
				var rebuiltInputs = verifiedInputs(rebuilt);
				var outputs = producer.getOutput();
				var rebuiltOutputs = rebuilt.getOutput();
				if (inputs.size() != rebuiltInputs.size() || outputs.length != rebuiltOutputs.length) {
					throw new IOException("Producer port count changed at " + from.getLabelSimple());
				}
				boolean registeredOutput = false;
				for (int i = 0; i < outputs.length; i++) {
					verifyEndpoint(outputs[i], rebuiltOutputs[i]);
					registeredOutput |= outputs[i] == from && rebuiltOutputs[i] == to;
				}
				if (!registeredOutput) {
					throw new IOException("Object is missing from its producer output ports");
				}
				for (int i = 0; i < inputs.size(); i++) {
					visit(inputs.get(i), rebuiltInputs.get(i), depth + 1);
				}
			}
			verifyOptional(from.getShowObjectCondition(), to.getShowObjectCondition());
			verifyOptional(from.getDynamicCaption(), to.getDynamicCaption());
			verifyOptional(from.getColorFunction(), to.getColorFunction());
			if (from instanceof Locateable placed && to instanceof Locateable rebuiltPlacement) {
				verifyOptional(placed.getStartPoint(), rebuiltPlacement.getStartPoint());
			}
			if (from instanceof GeoPoint point && to instanceof GeoPoint rebuiltPoint) {
				verifyGeometryReference(point.getPath(), rebuiltPoint.getPath());
				verifyGeometryReference(point.getRegion(), rebuiltPoint.getRegion());
			}
			active.remove(from);
			verified.add(from);
		}

		private void verifyOptional(GeoElementND from, GeoElementND to) throws IOException {
			if (from != null || to != null) {
				if (from == null || to == null) {
					throw new IOException("A native presentation reference disappeared or was introduced");
				}
				verifyEndpoint(from.toGeoElement(), to.toGeoElement());
			}
		}

		private void verifyGeometryReference(Object from, Object to) throws IOException {
			if (from != null || to != null) {
				if (!(from instanceof GeoElement fromGeo) || !(to instanceof GeoElement toGeo)) {
					throw new IOException("A path or region reference cannot be reconstructed");
				}
				verifyEndpoint(fromGeo, toGeo);
			}
		}

		private void verifyEndpoint(GeoElement from, GeoElement to) throws IOException {
			if (from == null || to == null || from.getClass() != to.getClass()
					|| from.isDefined() && !to.isDefined()) {
				throw new IOException("Native producer port is missing, changed type or lost its value");
			}
			GeoElement known = pairs.get(from);
			if (known != null) {
				if (known != to) {
					throw new IOException("Native producer port points to a different object: "
							+ from.getGeoClassType() + " " + from.getLabelSimple());
				}
				return;
			}
			if (from == source.getXAxis() || from == source.getYAxis()) {
				if (to != (from == source.getXAxis() ? target.getXAxis() : target.getYAxis())) {
					throw new IOException("Native axis dependency changed role");
				}
			} else {
				verifyAuxiliary(from, to);
			}
			if (inverse.putIfAbsent(to, from) != null) {
				throw new IOException("Two source ports were conflated in the native copy");
			}
			pairs.put(from, to);
		}

		private void verifyAuxiliary(GeoElement from, GeoElement to) throws IOException {
			Construction fromConstruction = source.getConstruction();
			Construction toConstruction = target.getConstruction();
			if (from.getConstruction() != fromConstruction || to.getConstruction() != toConstruction
					|| from.isLocalVariable() != to.isLocalVariable()
					|| from.isLabelSet() && !from.isLocalVariable()
					|| to.isLabelSet() && !to.isLocalVariable()) {
				throw new IOException("An unresolved producer port refers to an external or named object");
			}
			if (from.getParentAlgorithm() == null && !from.isLocalVariable()
					&& !Objects.equals(GeoCeDGExpertInspection.currentValue(from, 0),
							GeoCeDGExpertInspection.currentValue(to, 0))) {
				throw new IOException("An anonymous native operand changed value");
			}
		}
	}
}
