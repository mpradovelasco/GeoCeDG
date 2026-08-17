/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 *
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

package org.geogebra.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadPurpose;
import org.geogebra.common.awt.GRectangle2D;
import org.geogebra.common.euclidian.EmbedManager;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoInputBox;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoCurveCartesian;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoEmbed;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.kernel.geos.GeoInline;
import org.geogebra.common.kernel.geos.GeoLocusStroke;
import org.geogebra.common.kernel.geos.GeoMindMapNode;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.geos.GeoStadium;
import org.geogebra.common.kernel.geos.GeoWidget;
import org.geogebra.common.kernel.geos.MoveGeos;
import org.geogebra.common.kernel.geos.groups.Group;
import org.geogebra.common.kernel.matrix.Coords;
import org.geogebra.common.kernel.statistics.AlgoTableToChart;
import org.geogebra.common.main.App;
import org.geogebra.common.main.MyError;
import org.geogebra.common.util.debug.Log;

public class InternalClipboard {
	private static final ArrayList<String> copiedXmlLabels = new ArrayList<>();
	private static final StringBuilder copiedXml = new StringBuilder();
	private static final Map<String, String> copiedImages = new HashMap<>();
	private static final Map<String, String> copiedEmbeds = new HashMap<>();
	public static final String imagePrefix = "ggbimagedata";
	public static final String embedPrefix = "ggbembeddata";

	/**
	 * Copy XML for given geos into internal clipboard
	 * @param app application
	 * @param geos selected geos
	 */
	public static void copyToXMLInternal(App app, List<GeoElement> geos) {
		if (geos.isEmpty()) {
			return;
		}

		boolean scriptsBlocked = app.isBlockUpdateScripts();
		app.setBlockUpdateScripts(true);

		// create geosLocal and geosToHide
		ArrayList<ConstructionElement> geosLocal = new ArrayList<>();
		HashSet<Group> selectedGroups = new HashSet<>(app
				.getSelectionManager().getSelectedGroups());
		for (GeoElement geo : geos) {
			if (!(geo instanceof GeoEmbed && ((GeoEmbed) geo).isGraspableMath())) {
				geosLocal.add(geo);
			}
		}

		CopyPaste.addSubGeos(geosLocal, selectedGroups);
		addSpatialIdentityClosure(geosLocal, false);

		if (geosLocal.isEmpty()) {
			app.setBlockUpdateScripts(scriptsBlocked);
			return;
		}

		ArrayList<ConstructionElement> geosToHide = CopyPaste.addPredecessorGeos(geosLocal);

		geosToHide.addAll(addAlgosDependentFromInside(geosLocal, null));
		final Set<GeoElement> spatialClosure =
				addSpatialIdentityClosure(geosLocal, true);
		// topological order to make sure client listener can process predecessor objects
		// before child objects (e.g. for multiuser)
		Collections.sort(geosLocal);
		Kernel kernel = app.getKernel();
		EmbedManager embedManager = app.getEmbedManager();
		if (embedManager != null) {
			embedManager.persist();
		}
		beforeSavingToXML(geosLocal, geosToHide);

		final boolean saveScriptsToXML = kernel.getSaveScriptsToXML();
		kernel.setSaveScriptsToXML(false);

		copiedXml.setLength(0);
		copiedImages.clear();
		copiedEmbeds.clear();

		Construction cons = app.getKernel().getConstruction();
		XMLStringBuilder xmlStringBuilder = new XMLStringBuilder(copiedXml);
		SpatialIdentityRegistry spatialRegistry = cons.getSpatialIdentityRegistry();
		List<SpatialIdentityRecord> spatialRecords =
				spatialRegistry.getClosureRecords(spatialClosure);
		if (!spatialRecords.isEmpty()) {
			cons.beginSpatialIdentityXML();
		}
		try {
			for (int i = 0; i < cons.steps(); ++i) {
				ConstructionElement ce = cons.getConstructionElement(i);
				if (ce instanceof AlgoTableToChart) {
					ce = ((AlgoTableToChart) ce).getOutput(0);
				}
				if (geosLocal.contains(ce)) {
					if (ce instanceof GeoMindMapNode
							&& !geosLocal.contains(((GeoMindMapNode) ce).getParent())) {
						((GeoMindMapNode) ce).getXMLNoParent(xmlStringBuilder);
					} else {
						ce.getXML(false, xmlStringBuilder);
					}

					if (ce instanceof GeoImage) {
						GeoImage image = (GeoImage) ce;
						String name = image.getImageFileName();
						ImageManager imageManager = app.getImageManager();
						copiedImages.put(name, imageManager.getExternalImageSrc(name));
					}
					if (ce instanceof GeoEmbed && embedManager != null) {
						int embedID = ((GeoEmbed) ce).getEmbedID();
						String name = String.valueOf(embedID);
						copiedEmbeds.put(name, embedManager.getContent(embedID));
					}
				}
			}
			if (!spatialRecords.isEmpty()) {
				xmlStringBuilder.append(new XMLStringBuilder(new StringBuilder(
						spatialRegistry.writeClosureSection(spatialClosure))));
			}
		} finally {
			if (!spatialRecords.isEmpty()) {
				cons.endSpatialIdentityXML();
			}
		}

		for (Group group : selectedGroups) {
			group.getXML(new XMLStringBuilder(copiedXml));
		}

		kernel.setSaveScriptsToXML(saveScriptsToXML);

		afterSavingToXML(geosLocal, geosToHide);
		app.setBlockUpdateScripts(scriptsBlocked);
	}

	/**
	 * copyToXML - Add the algos which belong to our selected geos Also
	 * add the geos which might be side-effects of these algos
	 * @param consElements input and output
	 * @param copiedMacros output set for collecting macros or null if macro collecting not needed
	 * @return the possible side-effect geos
	 */
	public static ArrayList<ConstructionElement> addAlgosDependentFromInside(
			ArrayList<ConstructionElement> consElements, Set<Macro> copiedMacros) {

		ArrayList<ConstructionElement> ret = new ArrayList<>();

		for (int i = consElements.size() - 1; i >= 0; i--) {
			if (!(consElements.get(i) instanceof GeoElement)) {
				continue;
			}
			GeoElement geo = (GeoElement) consElements.get(i);

			// also doing this here, which is not about the name of the method,
			// but making sure textfields (which require algos) are shown
			if ((geo.getParentAlgorithm() instanceof AlgoInputBox)
					&& !ret.contains(geo.getParentAlgorithm())
					&& !consElements.contains(geo.getParentAlgorithm())) {
				// other algos will be added to this anyway,
				// so we can handle this issue in this method
				ret.add(geo.getParentAlgorithm());
			}

			ArrayList<AlgoElement> geoal = geo.getAlgorithmList();
			for (AlgoElement ale : geoal) {
				if (ale instanceof AlgoTableToChart) {
					continue;
				}
				if (ale instanceof AlgoMacro && copiedMacros != null) {
					copiedMacros.add(((AlgoMacro) ale).getMacro());
				}
				List<ConstructionElement> ac = Arrays.asList(ale.getInput());

				if (consElements.containsAll(ac) && !consElements.contains(ale)) {
					consElements.add(ale);
					for (GeoElement geoElement : ale.getOutput()) {
						if (!ret.contains(geoElement)
								&& !consElements.contains(geoElement)) {
							ret.add(geoElement);
						}
					}
				}
			}
		}

		consElements.addAll(ret);
		return ret;
	}

	/**
	 * Expands participating geos to their complete semantic component. When
	 * requested, geometric predecessors and their algorithms are added after
	 * semantic expansion so the ordinary clipboard XML remains reconstructible.
	 *
	 * @param consElements clipboard construction elements
	 * @param includePredecessors whether to complete geometric dependencies
	 * @return all geos whose participating records belong in the fragment
	 */
	public static Set<GeoElement> addSpatialIdentityClosure(
			ArrayList<ConstructionElement> consElements,
			boolean includePredecessors) {
		Construction construction = null;
		LinkedHashSet<GeoElement> participatingSeeds = new LinkedHashSet<>();
		for (ConstructionElement element : consElements) {
			if (element instanceof GeoElement) {
				GeoElement geo = (GeoElement) element;
				construction = geo.getConstruction();
			}
		}
		if (construction == null) {
			return Collections.emptySet();
		}

		SpatialIdentityRegistry registry = construction.getSpatialIdentityRegistry();
		for (ConstructionElement element : consElements) {
			if (element instanceof GeoElement
					&& registry.isParticipating((GeoElement) element)) {
				participatingSeeds.add((GeoElement) element);
			}
		}
		if (participatingSeeds.isEmpty()) {
			return Collections.emptySet();
		}

		LinkedHashSet<GeoElement> semanticClosure = new LinkedHashSet<>();
		LinkedHashSet<GeoElement> needsPredecessors = new LinkedHashSet<>();
		boolean changed;
		do {
			changed = false;
			for (GeoElement geo : registry.expandSemanticClosure(
					participatingSeeds)) {
				changed |= semanticClosure.add(geo);
				if (!consElements.contains(geo)) {
					consElements.add(geo);
					needsPredecessors.add(geo);
				}
			}
			if (includePredecessors) {
				for (GeoElement geo : new ArrayList<>(needsPredecessors)) {
					addSpatialPredecessors(geo, construction, registry,
							participatingSeeds, consElements);
				}
				needsPredecessors.clear();
			}
		} while (changed);
		return Collections.unmodifiableSet(semanticClosure);
	}

	private static void addSpatialPredecessors(GeoElement geo,
			Construction construction, SpatialIdentityRegistry registry,
			Set<GeoElement> participatingSeeds,
			ArrayList<ConstructionElement> consElements) {
		for (GeoElement predecessor : geo.getAllPredecessors()) {
			if (!construction.isConstantElement(predecessor)) {
				if (!consElements.contains(predecessor)) {
					consElements.add(predecessor);
				}
				if (registry.isParticipating(predecessor)) {
					participatingSeeds.add(predecessor);
				}
				AlgoElement parent = predecessor.getParentAlgorithm();
				if (parent != null && !consElements.contains(parent)) {
					consElements.add(parent);
				}
			}
		}
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent != null && !consElements.contains(parent)) {
			consElements.add(parent);
		}
	}

	/**
	 * copyToXML - Before saving the consElements to xml, we have to rename its
	 * labels with labelPrefix and memorize those renamed labels and also hide
	 * the GeoElements in geosToHide, and keep in geosToHide only those which
	 * were actually hidden...
	 * @param consElements construction elements
	 */
	private static void beforeSavingToXML(ArrayList<ConstructionElement> consElements,
			ArrayList<ConstructionElement> geosToHide) {

		copiedXmlLabels.clear();

		ConstructionElement geo;
		String label;

		for (ConstructionElement consElement : consElements) {
			geo = consElement;
			if (geo.isGeoElement()) {
				label = ((GeoElement) geo).getLabelSimple();
				if (label != null) {
					copiedXmlLabels.add(CopyPaste.labelPrefix + label);
					((GeoElement) geo).addLabelPrefix(CopyPaste.labelPrefix);
				}
			}
		}

		for (int j = geosToHide.size() - 1; j >= 0; j--) {
			geo = geosToHide.get(j);
			if (geo.isGeoElement() && ((GeoElement) geo).isEuclidianVisible()) {
				((GeoElement) geo).setEuclidianVisible(false);
			} else {
				geosToHide.remove(geo);
			}
		}
	}

	/**
	 * copyToXML - Step 6 After saving the consElements to xml, we have to rename its
	 * labels and also show the GeoElements in geosToShow
	 * @param consElements construction elements
	 */
	private static void afterSavingToXML(ArrayList<ConstructionElement> consElements,
			ArrayList<ConstructionElement> geosToShow) {

		ConstructionElement geo;
		String label;
		for (ConstructionElement consElement : consElements) {
			geo = consElement;
			if (geo.isGeoElement()) {
				label = ((GeoElement) geo).getLabelSimple();
				if (label != null && label.length() >= CopyPaste.labelPrefix.length()) {
					if (label.startsWith(CopyPaste.labelPrefix)) {
						try {
							((GeoElement) geo).setLabelSimple(
									label.substring(CopyPaste.labelPrefix.length()));
						} catch (Exception e) {
							Log.debug(e);
						}
					}
				}
			}
		}

		for (int j = geosToShow.size() - 1; j >= 0; j--) {
			geo = geosToShow.get(j);
			if (geo.isGeoElement()) {
				((GeoElement) geo).setEuclidianVisible(true);
			}
		}
	}

	private static void print(StringBuilder textToSave, Map<String, String> copiedImages,
			String prefix, EscapeFunction fn) {
		for (Map.Entry<String, String> image : copiedImages.entrySet()) {
			textToSave.append(prefix);
			textToSave.append(" ");
			textToSave.append(fn.escape(image.getKey()));
			textToSave.append(" ");
			textToSave.append(image.getValue());
			textToSave.append("\n");
		}
	}

	/**
	 * @param app application
	 * @param geos selected geos
	 * @param escape escape function
	 * @return text for clipboard
	 */
	public static String getTextToSave(App app, List<GeoElement> geos, EscapeFunction escape) {
		InternalClipboard.copyToXMLInternal(app, geos);

		StringBuilder textToSave = new StringBuilder();
		for (String label : copiedXmlLabels) {
			textToSave.append(label).append(" ");
		}
		textToSave.append("\n");

		print(textToSave, copiedImages, imagePrefix, escape);
		print(textToSave, copiedEmbeds, embedPrefix, escape);

		textToSave.append(copiedXml);
		return textToSave.toString();
	}

	/**
	 * @param app application
	 * @param geos elements to duplicate
	 */
	public static void duplicate(App app, List<GeoElement> geos) {
		InternalClipboard.copyToXMLInternal(app, geos);
		pasteGeoGebraXMLInternal(app, copiedXmlLabels, copiedXml.toString());
	}

	/**
	 * @param app application
	 * @param copiedXmlLabels labels of copied elements
	 * @param copiedXml copied XML
	 */
	public static void pasteGeoGebraXMLInternal(App app,
			List<String> copiedXmlLabels, String copiedXml) {
		try {
			SpatialIdentityRegistry.preflightClipboardFragment(copiedXml);
		} catch (SpatialIdentityException failure) {
			Log.debug(failure.getMessage());
			return;
		}
		app.getKernel().notifyPaste(copiedXml);
		// it turned out to be necessary for e.g. handleLabels
		final boolean scriptsBlocked = app.isBlockUpdateScripts();
		app.setBlockUpdateScripts(true);

		EuclidianView ev = app.getActiveEuclidianView();
		EuclidianController euclidianController = ev.getEuclidianController();

		// first we save the selected MindMap node (if there is one)
		final MindMapPaster mindMapPaster = new MindMapPaster();
		mindMapPaster.setTargetFromSelection(app.getSelectionManager());

		// then we clear the selection and stop edit mode for all widgets
		euclidianController.clearSelections(true, false);
		euclidianController.widgetsToBackground();

		// don't update properties view
		app.updateSelection(false);
		if (!evalClipboardXMLAtomically(app, copiedXml)) {
			app.setBlockUpdateScripts(scriptsBlocked);
			return;
		}
		app.getKernel().getConstruction().updateConstruction(false);
		if (ev == app.getEuclidianView1()) {
			app.setActiveView(App.VIEW_EUCLIDIAN);
		} else if (app.isEuclidianView3D(ev)) {
			app.setActiveView(App.VIEW_EUCLIDIAN3D);
		} else {
			app.setActiveView(App.VIEW_EUCLIDIAN2);
		}

		Set<String> duplicateLabels = copiedXmlLabels
				.stream()
				.map(label -> label.substring(CopyPaste.labelPrefix.length()))
				.collect(Collectors.toSet());

		ArrayList<GeoElement> createdElements = CopyPaste.handleLabels(
				app, copiedXmlLabels, duplicateLabels, false);

		app.setBlockUpdateScripts(scriptsBlocked);
		app.getActiveEuclidianView().invalidateDrawableList();
		app.getKernel().notifyPasteComplete(createdElements);

		if (app.isWhiteboardActive()) {
			ArrayList<GeoElement> shapes = new ArrayList<>();
			ArrayList<GeoElement> movable = new ArrayList<>();
			ArrayList<GeoMindMapNode> mindMaps = new ArrayList<>();
			for (GeoElement created : createdElements) {
				if (created.isGeoPolygon() || created instanceof GeoPolyLine
						|| created.isGeoConic() || created instanceof GeoLocusStroke
						|| created instanceof GeoWidget || created instanceof GeoImage
						|| created instanceof GeoInline || created instanceof GeoStadium
						|| created instanceof GeoCurveCartesian) {
					shapes.add(created);
				}
				if (created instanceof GeoMindMapNode) {
					mindMaps.add((GeoMindMapNode) created);
				} else if (!groupedWithMindMap(created)) {
					movable.add(created);
				}
			}

			app.getSelectionManager().setSelectedGeos(shapes);
			euclidianController.updateBoundingBoxFromSelection(false);

			int viewCenterX = ev.getWidth() / 2;
			int viewCenterY = ev.getHeight() / 2;

			GRectangle2D boundingBoxRectangle = ev.getBoundingBox().getRectangle();

			double boxCenterX = boundingBoxRectangle.getX() + boundingBoxRectangle.getWidth() / 2;
			double boxCenterY = boundingBoxRectangle.getY() + boundingBoxRectangle.getHeight() / 2;

			Coords coords = new Coords(ev.getInvXscale() * (viewCenterX - boxCenterX),
					ev.getInvYscale() * (boxCenterY - viewCenterY), 0);

			MoveGeos.moveObjects(movable, coords, null, null, ev);
			mindMapPaster.joinToTarget(mindMaps);
			ev.updateAllDrawables(true);

			euclidianController.updateBoundingBoxFromSelection(false);
			euclidianController.showDynamicStylebar();
		}

		app.getKernel().getConstruction().getUndoManager().storeAddGeo(createdElements);
	}

	/**
	 * Evaluates one clipboard fragment with an explicit import plan. A rejected
	 * semantic closure restores the exact pre-paste construction before control
	 * returns to label handling or undo notification.
	 *
	 * @param app application receiving the fragment
	 * @param clipboardXml construction-element fragment
	 * @return whether the complete fragment was imported
	 */
	public static boolean evalClipboardXMLAtomically(App app, String clipboardXml) {
		return evalClipboardXMLAtomically(app, clipboardXml, null, null);
	}

	/**
	 * Evaluates a clipboard fragment inside a caller-started host mutation.
	 * The mutation runs after paste pre-notification but inside the same rollback
	 * boundary as XML evaluation.
	 *
	 * @param app application receiving the fragment
	 * @param clipboardXml construction-element fragment
	 * @param rollbackXml snapshot captured before the caller mutation, or null
	 * @param beforeImport caller mutation to include in the rollback, or null
	 * @return whether the caller mutation and complete fragment were imported
	 */
	public static boolean evalClipboardXMLAtomically(App app, String clipboardXml,
			String rollbackXml, Runnable beforeImport) {
		app.getActiveEuclidianView().saveInlines();
		String effectiveRollbackXml = rollbackXml == null ? app.getXML() : rollbackXml;
		XMLStringBuilder wrapped = new XMLStringBuilder();
		MyXMLio.addXMLHeader(wrapped);
		MyXMLio.addGeoGebraHeader(wrapped, false, null, app);
		wrapped.startOpeningTag("construction", 0).endTag();
		wrapped.append(new XMLStringBuilder(new StringBuilder(clipboardXml)))
				.closeTag("construction");
		wrapped.closeTag("geogebra");

		Construction construction = app.getKernel().getConstruction();
		try {
			if (beforeImport != null) {
				beforeImport.run();
			}
			construction.setNextSpatialIdentityLoadPurpose(LoadPurpose.CLIPBOARD_IMPORT);
			app.getXMLio().processXMLString(wrapped.toString(), false, false);
			app.getActiveEuclidianView().updateInlines();
			return true;
		} catch (Exception | MyError failure) {
			try {
				construction.setNextSpatialIdentityLoadPurpose(
						LoadPurpose.ROLLBACK_RESTORE);
				app.getXMLio().processXMLString(effectiveRollbackXml, true, false);
				app.getActiveEuclidianView().updateInlines();
			} catch (Exception | MyError rollbackFailure) {
				throw new IllegalStateException(
						"Clipboard identity rollback could not restore the construction",
						rollbackFailure);
			} finally {
				construction.clearNextSpatialIdentityLoadPurpose();
			}
			return false;
		} finally {
			construction.clearNextSpatialIdentityLoadPurpose();
		}
	}

	private static boolean groupedWithMindMap(GeoElement created) {
		Group parentGroup = created.getParentGroup();
		return parentGroup != null && parentGroup.stream()
				.anyMatch(geo -> geo instanceof GeoMindMapNode);
	}

	/**
	 * Escape function.
	 */
	public interface EscapeFunction {
		/**
		 * @param key raw string
		 * @return escaped string
		 */
		String escape(String key);
	}
}
