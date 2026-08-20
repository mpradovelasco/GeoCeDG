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

package org.geogebra.common.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadPurpose;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.RedefinePublicationLease;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.RedefineRebuildToken;
import org.geocedg.common.kernel.spatial.identity.SpatialPointPilotRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineCandidateOutput;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefinePersistedOutput;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineTransaction;
import org.geocedg.common.kernel.spatial.runtime.SpatialSemanticRuntime;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.LayerManager;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.algos.AlgoDistancePoints;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgorithmSet;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.arithmetic.ArbitraryConstantRegistry;
import org.geogebra.common.kernel.arithmetic.Equation;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionNodeConstants;
import org.geogebra.common.kernel.arithmetic.ValidExpression;
import org.geogebra.common.kernel.cas.AlgoDependentCasCell;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoAxis;
import org.geogebra.common.kernel.geos.GeoCasCell;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoElementSpreadsheet;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoSymbolic;
import org.geogebra.common.kernel.geos.LabelManager;
import org.geogebra.common.kernel.geos.groups.Group;
import org.geogebra.common.kernel.kernelND.GeoAxisND;
import org.geogebra.common.kernel.kernelND.GeoDirectionND;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.optimization.ExtremumFinderI;
import org.geogebra.common.kernel.prover.AlgoLocusEquation;
import org.geogebra.common.kernel.prover.AlgoProve;
import org.geogebra.common.kernel.prover.AlgoProveDetails;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.MyError;
import org.geogebra.common.main.MyError.Errors;
import org.geogebra.common.main.SelectionManager;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.common.main.undo.UndoManager;
import org.geogebra.common.plugin.GeoClass;
import org.geogebra.common.plugin.ScriptManager;
import org.geogebra.common.util.StringUtil;
import org.geogebra.common.util.debug.Log;
import org.geogebra.editor.share.input.Character;

import com.google.j2objc.annotations.Weak;

/**
 * Manages construction elements
 * @author Markus
 */
public class Construction {

	private ConstructionCompanion companion;
	/** maps arbconst indices to related numbers */
	private final Map<Integer, GeoNumeric> arbitraryConstantsMap = new TreeMap<>();
	/** List of arbitrary constants that were loaded from a file but were not claimed yet */
	private List<GeoNumeric> unclaimedArbitraryConstants = new ArrayList<>();
	/** maps arbint indices to related numbers */
	private final Map<Integer, GeoNumeric> arbitraryIntegersMap = new TreeMap<>();
	/** maps arbcomplex indices to related numbers */
	private final Map<Integer, GeoNumeric> arbitraryComplexNumbersMap = new TreeMap<>();

	/**
	 * used to keep track if file is 3D or just 2D
	 *
	 * cleared in Construction.newConstructionDefaults() (after default geos are
	 * loaded)
	 */
	private TreeSet<GeoClass> usedGeos = new TreeSet<>();

	// list of Macro commands used in this construction
	// TODO: specify type once Macro is ported
	private ArrayList<Macro> usedMacros;
	/** UndoManager */
	protected UndoManager undoManager;

	/** default elements */
	private ConstructionDefaults consDefaults;
	private String title;
	private String author;
	private String date;
	// text for dynamic worksheets: 0 .. above, 1 .. below
	private String[] worksheetText = new String[2];

	// showOnlyBreakpoints in construction protocol
	private boolean showOnlyBreakpoints;

	/** construction belongs to kernel */
	@Weak
	protected final @Nonnull Kernel kernel;

	// current construction step (-1 ... ceList.size() - 1)
	// step == -1 shows empty construction
	private int step;

	// in macro mode no new labels or construction elements
	// can be added
	private boolean suppressLabelCreation = false;

	// a map for sets with all labeled GeoElements in alphabetical order of
	// specific types
	// (points, lines, etc.)
	//
	private HashMap<GeoClass, TreeSet<GeoElement>> geoSetsTypeMap;

	// ConstructionElement List (for objects of type ConstructionElement)
	private final ArrayList<ConstructionElement> ceList;

	// AlgoElement List (for objects of type AlgoElement)
	private final ArrayList<AlgoElement> algoList; // used in updateConstruction()

	/** Table for (label, GeoElement) pairs, contains global variables */
	protected HashMap<String, GeoElement> geoTable;

	// list of algorithms that need to be updated when EuclidianView changes
	private ArrayList<EuclidianViewCE> euclidianViewCE;
	private ArrayList<EuclidianViewCE> corner5Algos;
	private ArrayList<EuclidianViewCE> corner11Algos;

	/** Table for (label, GeoElement) pairs, contains local variables */
	protected HashMap<String, GeoElement> localVariableTable;

	// set with all labeled GeoElements in ceList order
	private TreeSet<GeoElement> geoSetConsOrder;

	// set with all labeled GeoElements in alphabetical order
	private TreeSet<GeoElement> geoSetLabelOrder;
	private TreeSet<GeoElement> geoSetWithCasCells;
	// table of arbitraryConstants with casTable row key
	private HashMap<Integer, ArbitraryConstantRegistry> arbitraryConsTable = new HashMap<>();

	// list of random numbers or lists
	private TreeSet<GeoElement> randomElements;
	/** algo set currently updated by GeoElement.updateDependentObjects() */
	private AlgorithmSet algoSetCurrentlyUpdated;

	private final HashSet<String> protectedLabels = new HashSet<>();

	/**
	 * Table for (label, GeoCasCell) pairs, contains global variables used in
	 * CAS view
	 */
	protected HashMap<String, GeoCasCell> geoCasCellTable;

	// collect replace() requests to improve performance
	// when many cells in the spreadsheet are redefined at once
	private boolean collectRedefineCalls = false;
	private HashMap<GeoElement, GeoElement> redefineMap;
	private HashMap<GeoElement, SpatialRedefineTransaction> spatialRedefineMap;
	private String collectedRedefineRollbackXml;
	private long spatialRedefineHostEpoch;
	private long collectedSpatialRedefineHostEpoch;
	private GeoElement keepGeo;
	private ArrayList<GeoElement> latexGeos;

	// axis objects
	private GeoAxis xAxis;
	private GeoAxis yAxis;
	private String xAxisLocalName;
	private String yAxisLocalName;
	private GeoPoint origin;

	private Stack<GeoElement> selfGeoStack = new Stack<>();

	private boolean isGettingXMLForReplace;
	private boolean spreadsheetTraces;
	private boolean allowUnboundedAngles = true;

	private GeoElement geoBeingRemovedForReplace;
	private Set<GeoElement> spatialGeosBeingRemovedForReplace;
	private boolean ignoringNewTypes;
	private int newTypeRegistrationSuppressionDepth;
	private int spatialSemanticAdapterNotificationSuppressionDepth;

	private MyXMLio xmlio;
	private final SpatialIdentityRegistry spatialIdentityRegistry;
	private final SpatialSemanticRuntime spatialSemanticRuntime;
	private LoadPurpose nextSpatialIdentityLoadPurpose;
	private RedefineRebuildToken nextSpatialIdentityRedefineRebuildToken;
	private int spatialIdentityXmlDepth;

	private GeoElement outputGeo;

	private ArrayList<String> registeredFV = new ArrayList<>();

	private boolean fileLoading;
	private boolean casCellUpdate = false;
	private boolean notXmlLoading = false;
	private boolean updateConstructionRunning;
	private LabelManager labelManager;

	private ArrayList<Group> groups;

	private LayerManager layerManager;

	/**
	 * Creates a new Construction.
	 * @param k Kernel
	 */
	public Construction(@Nonnull Kernel k) {
		this(k, null);
	}

	/**
	 * Creates a new Construction.
	 * @param k Kernel
	 * @param parentConstruction parent construction (used for macro constructions)
	 */
	protected Construction(@Nonnull Kernel k, Construction parentConstruction) {
		kernel = k;
		spatialIdentityRegistry = new SpatialIdentityRegistry(this);
		spatialSemanticRuntime = new SpatialSemanticRuntime(this);
		spatialIdentityRegistry.registerLifecycleRuntime(spatialSemanticRuntime);
		spatialIdentityRegistry.registerRedefineProvider(
				new SpatialPointPilotRedefineProvider(spatialIdentityRegistry));
		spatialIdentityRegistry.registerRedefineProvider(
				new ConstructionGeoRedefineProvider(spatialIdentityRegistry));

		companion = kernel.createConstructionCompanion(this);

		ceList = new ArrayList<>();
		algoList = new ArrayList<>();
		step = -1;

		geoSetConsOrder = new TreeSet<>();
		geoSetWithCasCells = new TreeSet<>();
		geoSetLabelOrder = new TreeSet<>(new LabelComparator());
		geoSetsTypeMap = new HashMap<>();
		euclidianViewCE = new ArrayList<>();

		layerManager = new LayerManager();

		if (parentConstruction != null) {
			consDefaults = parentConstruction.getConstructionDefaults();
		} else {
			newConstructionDefaults();
		}
		// consDefaults = new ConstructionDefaults(this);
		setIgnoringNewTypes(true);
		initAxis();
		setIgnoringNewTypes(false);
		geoTable = new HashMap<>(200);
		initGeoTables();
		groups = new ArrayList<>();
	}

	/**
	 * @return this construction's durable spatial identity registry
	 */
	public SpatialIdentityRegistry getSpatialIdentityRegistry() {
		return spatialIdentityRegistry;
	}

	/** @return this construction's G9A2 normal-DAG spatial runtime */
	public SpatialSemanticRuntime getSpatialSemanticRuntime() {
		return spatialSemanticRuntime;
	}

	/**
	 * Suppresses View removal callbacks while an obsolete non-authoritative
	 * spatial adapter is detached from the normal DAG. The runtime queues the
	 * corresponding presentation withdrawal until the owning identity operation
	 * is terminal, so a listener can never reenter a sealed graph switch.
	 *
	 * @return nesting-safe construction-local suppression scope
	 */
	public SpatialSemanticAdapterNotificationScope
			suppressSpatialSemanticAdapterNotifications() {
		spatialSemanticAdapterNotificationSuppressionDepth++;
		return new SpatialSemanticAdapterNotificationScope();
	}

	/** @return whether runtime-only adapter View callbacks are being deferred */
	public boolean isSpatialSemanticAdapterNotificationSuppressed() {
		return spatialSemanticAdapterNotificationSuppressionDepth > 0;
	}

	/** One-shot scope used only by the construction-owned spatial runtime. */
	public final class SpatialSemanticAdapterNotificationScope
			implements AutoCloseable {
		private boolean closed;

		private SpatialSemanticAdapterNotificationScope() {
			// Construction-owned factory only.
		}

		@Override
		public void close() {
			if (closed) {
				return;
			}
			if (spatialSemanticAdapterNotificationSuppressionDepth <= 0) {
				throw new IllegalStateException(
						"Spatial adapter notification scope underflow");
			}
			spatialSemanticAdapterNotificationSuppressionDepth--;
			closed = true;
		}
	}

	/**
	 * Opens one synchronous host redefine rollback boundary. Collected spreadsheet
	 * redefines share the batch boundary; every other capture supersedes the
	 * previous boundary so an old context can never rewind a newer operation.
	 *
	 * @return positive construction-confined operation epoch
	 */
	public long captureSpatialRedefineHostOperationEpoch() {
		if (collectRedefineCalls && collectedSpatialRedefineHostEpoch > 0) {
			spatialIdentityRegistry
					.requireCollectedRedefineHostCaptureAllowed();
			return collectedSpatialRedefineHostEpoch;
		}
		spatialIdentityRegistry.requireRedefineHostCaptureAllowed();
		spatialRedefineHostEpoch = Math.addExact(spatialRedefineHostEpoch, 1);
		return spatialRedefineHostEpoch;
	}

	/** @return current construction-confined redefine operation epoch */
	public long getSpatialRedefineHostOperationEpoch() {
		return spatialRedefineHostEpoch;
	}

	/**
	 * Reconciles semantic algorithms after an atomic identity publication.
	 * Registry publication remains authoritative even if a presentation/runtime
	 * listener cannot be wired.
	 *
	 * @param changedIds published durable identities
	 */
	public void onSpatialIdentityRecordsPublished(
			Collection<SpatialIdentityId> changedIds) {
		try {
			spatialSemanticRuntime.onRecordsPublished(changedIds);
		} catch (RuntimeException exception) {
			Log.debug(exception);
		}
	}

	/**
	 * Withdraws current semantic payloads during identity retirement without
	 * changing the registry transaction outcome.
	 *
	 * @param retiredIds retired durable identities
	 */
	public void onSpatialIdentityRecordsRetired(
			Collection<SpatialIdentityId> retiredIds) {
		try {
			spatialSemanticRuntime.onRecordsRetired(retiredIds);
		} catch (RuntimeException exception) {
			Log.debug(exception);
		}
	}

	/**
	 * Selects the interpretation of the next identity-bearing XML parse. The
	 * parser consumes this value once; ordinary merges cannot infer import
	 * authority from labels or registry state.
	 *
	 * @param purpose explicit load purpose
	 */
	public void setNextSpatialIdentityLoadPurpose(LoadPurpose purpose) {
		if (purpose == LoadPurpose.REDEFINE_REBUILD) {
			throw new IllegalArgumentException(
					"REDEFINE_REBUILD requires exact context-bound authority");
		}
		nextSpatialIdentityLoadPurpose = purpose;
	}

	private void setNextSpatialIdentityRedefineRebuild(
			Collection<SpatialRedefineContext> contexts) {
		if (nextSpatialIdentityLoadPurpose != null
				|| nextSpatialIdentityRedefineRebuildToken != null) {
			throw new IllegalStateException(
					"A spatial identity load authority is already pending");
		}
		nextSpatialIdentityLoadPurpose = LoadPurpose.REDEFINE_REBUILD;
		nextSpatialIdentityRedefineRebuildToken =
				spatialIdentityRegistry.beginRedefineRebuild(contexts);
	}

	/**
	 * Consumes an explicit load purpose or returns the parser's safe default.
	 *
	 * @param defaultPurpose parser default for this XML operation
	 * @return purpose for the current construction section
	 */
	public LoadPurpose consumeSpatialIdentityLoadPurpose(LoadPurpose defaultPurpose) {
		LoadPurpose purpose = nextSpatialIdentityLoadPurpose;
		nextSpatialIdentityLoadPurpose = null;
		return purpose == null ? defaultPurpose : purpose;
	}

	/**
	 * Consumes the opaque rebuild token after its matching purpose was selected.
	 *
	 * @return matching rebuild token, or {@code null} for another load purpose
	 */
	public RedefineRebuildToken consumeSpatialIdentityRedefineRebuildToken(
			LoadPurpose purpose) {
		RedefineRebuildToken token = nextSpatialIdentityRedefineRebuildToken;
		if (purpose == LoadPurpose.REDEFINE_REBUILD && token == null) {
			throw new IllegalStateException(
					"REDEFINE_REBUILD load has no opaque authority token");
		}
		if (purpose != LoadPurpose.REDEFINE_REBUILD && token != null) {
			throw new IllegalStateException(
					"Spatial rebuild token does not match the selected load purpose");
		}
		nextSpatialIdentityRedefineRebuildToken = null;
		return token;
	}

	/** Clears an unused one-shot load purpose after a failed or legacy parse. */
	public void clearNextSpatialIdentityLoadPurpose() {
		if (nextSpatialIdentityRedefineRebuildToken != null) {
			spatialIdentityRegistry.abortRedefineRebuild(
					nextSpatialIdentityRedefineRebuildToken);
			nextSpatialIdentityRedefineRebuildToken = null;
		}
		nextSpatialIdentityLoadPurpose = null;
	}

	/** Starts an explicit full/semantic-fragment identity XML scope. */
	public void beginSpatialIdentityXML() {
		spatialIdentityXmlDepth++;
	}

	/** Ends an explicit full/semantic-fragment identity XML scope. */
	public void endSpatialIdentityXML() {
		if (spatialIdentityXmlDepth <= 0) {
			throw new IllegalStateException("Spatial identity XML scope is not active");
		}
		spatialIdentityXmlDepth--;
	}

	/**
	 * @return whether participating geo IDs may be emitted into element XML
	 */
	public boolean isSpatialIdentityXMLActive() {
		return spatialIdentityXmlDepth > 0;
	}

	/**
	 * Returns the point (0,0)
	 * @return point (0,0)
	 */
	public final GeoPoint getOrigin() {
		if (origin == null) {
			origin = new GeoPoint(this);
			origin.setCoords(0.0, 0.0, 1.0);
		}
		return origin;
	}

	/**
	 * @return geo temporarily kept inside this construction
	 */
	public GeoElement getKeepGeo() {
		return keepGeo;
	}

	/**
	 * @param selfGeo new value of "self" variable
	 */
	public void setSelfGeo(GeoElement selfGeo) {
		this.selfGeoStack.add(selfGeo);
	}

	/**
	 * Sets self geo to the previous one
	 */
	public void restoreSelfGeo() {
		this.selfGeoStack.pop();
	}

	/**
	 * @return whether a click/update script is currently running
	 */
	public boolean isScriptRunningForGeo() {
		return !selfGeoStack.isEmpty();
	}

	/**
	 * Returns x-axis
	 * @return x-axis
	 */
	final public GeoAxis getXAxis() {
		return xAxis;
	}

	/**
	 * Returns y-axis
	 * @return y-axis
	 */
	final public GeoAxis getYAxis() {
		return yAxis;
	}

	/**
	 * init the axis
	 */
	private void initAxis() {
		xAxis = new GeoAxis(this, GeoAxisND.X_AXIS);
		yAxis = new GeoAxis(this, GeoAxisND.Y_AXIS);

		companion.init();
	}

	/**
	 * creates the ConstructionDefaults consDefaults
	 */
	private void newConstructionDefaults() {
		consDefaults = companion.newConstructionDefaults();
	}

	public Map<Integer, GeoNumeric> getArbitraryConstants() {
		return arbitraryConstantsMap;
	}

	public List<GeoNumeric> getUnclaimedArbitraryConstants() {
		return unclaimedArbitraryConstants;
	}

	public Map<Integer, GeoNumeric> getArbitraryInts() {
		return arbitraryIntegersMap;
	}

	public Map<Integer, GeoNumeric> getArbitraryComplexNumbers() {
		return arbitraryComplexNumbersMap;
	}

	/**
	 * Construction constants (xAxis, yAxis, ...)
	 */
	public enum Constants {
		/**
		 * not a constant
		 */
		NOT,
		/**
		 * x axis
		 */
		X_AXIS,
		/**
		 * y axis
		 */
		Y_AXIS,
		/**
		 * z axis
		 */
		Z_AXIS,
		/**
		 * xOy plane
		 */
		XOY_PLANE,
		/**
		 * space
		 */
		SPACE
	}

	/**
	 * @param geo GeoElement
	 * @return Whether {@code geo} is a constant element.
	 */
	final public boolean isConstantElement(GeoElement geo) {
		return getConstantElement(geo) != Constants.NOT;
	}

	/**
	 * @param geo GeoElement
	 * @return The constant element associated with {@code geo},
	 * {@link Constants#NOT} if it is no constant element.
	 */
	final public Constants getConstantElement(GeoElement geo) {
		if (geo == xAxis) {
			return Constants.X_AXIS;
		}
		if (geo == yAxis) {
			return Constants.Y_AXIS;
		}

		return companion.getConstantElement(geo);
	}

	/**
	 * Renames xAxis and yAxis in the geoTable and sets axisLocalName-s accordingly
	 */
	final public void updateLocalAxesNames() {
		xAxisLocalName = updateLocalAxisName(xAxis, xAxisLocalName, "xAxis");
		yAxisLocalName = updateLocalAxisName(yAxis, yAxisLocalName, "yAxis");
		companion.updateLocalAxesNames();
	}

	/**
	 * In case a constant element has been overwritten by the XML,
	 * this method makes sure to keep the label unchanged (Issue with language change)
	 * @param element The element whose local name is to be updated
	 * @param localName The currently used name for element whose name is to be updated
	 * @param key The key used to retrieve the standard localized name of the given element
	 * @return The local name which may have been updated
	 */
	final public String updateLocalAxisName(GeoElement element, String localName, String key) {
		Localization localization = kernel.getLocalization();
		if (localName != null && !localName.equals(key)) {
			geoTable.remove(localName);
		}
		String changedLocalName = localization.getMenu(key);
		if (!geoTable.containsKey(changedLocalName)) {
			geoTable.put(changedLocalName, element);
			return changedLocalName;
		}
		return localName;
	}

	/**
	 * Returns the construction default object of this construction.
	 * @return construction default object of this construction.
	 */
	final public ConstructionDefaults getConstructionDefaults() {
		return consDefaults;
	}

	/**
	 * @return table of arbitraryConstants from CAS with assignmentVar key
	 */
	public HashMap<Integer, ArbitraryConstantRegistry> getArbitraryConsTable() {
		return arbitraryConsTable;
	}

	/**
	 * @param arbitraryConsTable - table of arbitraryConstants from CAS with assignmentVar key
	 */
	public void setArbitraryConsTable(
			HashMap<Integer, ArbitraryConstantRegistry> arbitraryConsTable) {
		this.arbitraryConsTable = arbitraryConsTable;
	}

	/**
	 * Returns construction's author
	 * @return construction's author
	 */
	public String getAuthor() {
		return (author != null) ? author : "";
	}

	/**
	 * Returns construction's date
	 * @return construction's date
	 */
	public String getDate() {
		return (date != null) ? date : "";
	}

	/**
	 * Returns construction's title
	 * @return construction's title
	 */
	public String getTitle() {
		return (title != null) ? title : "";
	}

	/**
	 * Sets construction's author
	 * @param string new author
	 */
	public void setAuthor(String string) {
		author = string;
	}

	/**
	 * Sets construction's date
	 * @param string new date
	 */
	public void setDate(String string) {
		date = string;
	}

	/**
	 * Sets construction's title
	 * @param string new title
	 */
	public void setTitle(String string) {
		title = string;
	}

	/**
	 * Returns part of worksheet text
	 * @param i 0 for first part, 1 for second part
	 * @return given part of worksheet text
	 */
	public String getWorksheetText(int i) {
		return (worksheetText[i] != null) ? worksheetText[i] : "";
	}

	/**
	 * Sets part of worksheet text
	 * @param i 0 for first part, 1 for second part
	 * @param text new text for that part
	 */
	public void setWorksheetText(String text, int i) {
		worksheetText[i] = text;
	}

	/**
	 * TODO: make private again
	 * @return true if at least one text is nonempty
	 */
	protected boolean worksheetTextDefined() {
		for (int i = 0; i < worksheetText.length; i++) {
			if (worksheetText[i] != null && worksheetText[i].length() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns current kernel
	 * @return current kernel
	 */
	public final @Nonnull Kernel getKernel() {
		return kernel;
	}

	/**
	 * If this is set to true new construction elements won't get labels.
	 * @param flag true iff label creation should be suppressed
	 */
	public void setSuppressLabelCreation(boolean flag) {
		suppressLabelCreation = flag;
	}

	/**
	 * @return context that prevents labeling objects until it's closed
	 */
	public LabelingContext getSilentContext() {
		final boolean oldSuppression = suppressLabelCreation;
		suppressLabelCreation = true;
		return () -> suppressLabelCreation = oldSuppression;
	}

	/**
	 * Returns true iff new construction elements won't get labels.
	 * @return true iff new construction elements won't get labels.
	 */
	public boolean isSuppressLabelsActive() {
		return suppressLabelCreation;
	}

	/**
	 * Returns current application
	 * @return current application
	 */
	public App getApplication() {
		return kernel.getApplication();
	}

	/**
	 * Tests if this construction has no elements.
	 * @return true if this construction has no GeoElements; false otherwise.
	 */
	public boolean isEmpty() {
		return ceList.isEmpty();
	}

	/**
	 * Returns the total number of construction steps.
	 * @return Total number of construction steps.
	 */
	public int steps() {
		return ceList.size();
	}

	/**
	 * Returns the last GeoElement object in the construction list.
	 * @return the last GeoElement object in the construction list.
	 */
	public GeoElement getLastGeoElement() {
		if (geoSetWithCasCells.size() > 0) {
			return geoSetWithCasCells.last();
		}
		return null;
	}

	/**
	 * Returns the last Cas Evaluable GeoElement object in the construction list.
	 * @return the last Cas Evaluable GeoElement object in the construction list.
	 */
	public GeoElement getLastCasEvaluableGeoElement() {
		Iterator<GeoElement> descending = geoSetWithCasCells.descendingIterator();
		while (descending.hasNext()) {
			GeoElement lastElement = descending.next();
			if (lastElement.isCasEvaluableObject()) {
				return lastElement;
			}
		}
		return null;
	}

	/***
	 * Returns the n-th GeoCasCell object (free or dependent) in the
	 * construction list. This is the GeoCasCell in the n-th row of the CAS
	 * view.
	 *
	 * @param row
	 *            number starting at 0
	 * @return cas cell or null if there are less cas cells in the construction
	 *         list
	 */
	public GeoCasCell getCasCell(int row) {
		if (row < 0) {
			return null;
		}

		int counter = 0;
		for (ConstructionElement ce : ceList) {
			if (ce instanceof GeoCasCell) {
				if (counter == row) {
					return (GeoCasCell) ce;
				}
				++counter;
			} else if (ce instanceof AlgoCasCellInterface) {
				if (counter == row) {
					return ((AlgoCasCellInterface) ce).getCasCell();
				}
				++counter;
			}
		}

		// less than n casCell
		return null;
	}

	/**
	 * Hide all CAS twin geos that were not loaded from a file
	 */
	public void updateCasCellTwinVisibility() {
		for (GeoElement ce : geoSetWithCasCells) {
			if (ce.getCorrespondingCasCell() != null) {
				ce.getCorrespondingCasCell().updateTwinGeoVisibility();
			}
		}
	}

	/***
	 * Returns the last GeoCasCell object (free or dependent) in the
	 * construction list.
	 *
	 * @return last cas cell
	 */
	public GeoCasCell getLastCasCell() {
		GeoCasCell lastCell = null;
		for (ConstructionElement ce : ceList) {
			if (ce instanceof GeoCasCell) {
				lastCell = (GeoCasCell) ce;
			} else if (ce instanceof AlgoCasCellInterface) {
				lastCell = ((AlgoCasCellInterface) ce).getCasCell();
			}
		}
		return lastCell;
	}

	/***
	 * Adds the given GeoCasCell object to the construction list so that it
	 * becomes the n-th GeoCasCell in the list. Other cas cells are shifted
	 * right.
	 *
	 * @param casCell
	 *            CAS cell to be added to construction list
	 *
	 * @param n
	 *            number starting at 0
	 */
	public void setCasCellRow(GeoCasCell casCell, int n) {
		GeoCasCell nthCasCell = getCasCell(n);
		if (nthCasCell == null) {
			addToConstructionList(casCell, false);
		} else {
			addToConstructionList(casCell, nthCasCell.getConstructionIndex());
		}

		addToGeoSetWithCasCells(casCell);
	}

	/**
	 * Adds a geo to the list of local variables using the specified local
	 * variable name .
	 * @param varname local variable name
	 * @param geo local variable object
	 */
	final public void addLocalVariable(String varname, GeoElement geo) {
		if (localVariableTable == null) {
			localVariableTable = new HashMap<>();
		}
		localVariableTable.put(varname, geo);
		geo.setLocalVariableLabel(varname);
	}

	/**
	 * Removes local variable of given name. Note that the underlying GeoElement
	 * object gets back its previous label as a side effect.
	 * @param varname name of variable to be removed
	 */
	final public void removeLocalVariable(String varname) {
		if (localVariableTable != null) {
			GeoElement geo = localVariableTable.remove(varname);
			if (geo != null) {
				geo.undoLocalVariableLabel();
			}
		}
	}

	/**
	 * Looks for geo with given label, doesn't work for e.g. A$1
	 * @param label Label to be looked up
	 * @return Geo with given label
	 */
	public GeoElement geoTableVarLookup(String label) {
		GeoElement ret = geoTable.get(label);
		return ret;
	}

	/**
	 * Looks for equation with given label
	 * @param label - label of the searched geo
	 * @return returns the equation defined by label in CAS
	 */
	public ValidExpression geoCeListLookup(String label) {
		for (int i = 0; i < ceList.size(); i++) {
			if (ceList.get(i) instanceof GeoCasCell) {
				// get current cell
				GeoCasCell currCell = (GeoCasCell) ceList.get(i);
				// we found the equation
				if (currCell.getLocalizedInput()
						.startsWith(label + "=")
						&& ((ExpressionNode) currCell.getInputVE())
						.getLeft() instanceof Equation) {
					// return the equation
					return (ValidExpression) ((ExpressionNode) currCell
							.getInputVE()).getLeft();
				}
			}
		}
		return null;
	}

	/**
	 * Sets how steps in the construction protocol are handled.
	 * @param flag true iff construction protocol should show only breakpoints
	 */
	public void setShowOnlyBreakpoints(boolean flag) {
		showOnlyBreakpoints = flag;
	}

	/**
	 * True iff construction protocol should show only breakpoints
	 * @return true iff construction protocol should show only breakpoints
	 */
	final public boolean showOnlyBreakpoints() {
		return showOnlyBreakpoints;
	}

	/**
	 * @param pos position
	 */
	private void updateConstructionIndex(int pos) {
		if (pos < 0) {
			return;
		}
		int size = ceList.size();
		for (int i = pos; i < size; ++i) {
			ceList.get(i).setConstructionIndex(i);
		}
	}

	/**
	 * Updates all algos
	 * @return true iff there were any algos that wanted update
	 * @author Michael Borcherds
	 */
	private boolean updateAllConstructionProtocolAlgorithms() {
		// update all algorithms
		ArrayList<AlgoElement> updateAlgos = null;
		for (AlgoElement algo : algoList) {
			if (algo.wantsConstructionProtocolUpdate()) {
				if (updateAlgos == null) {
					updateAlgos = new ArrayList<>();
				}
				updateAlgos.add(algo);
			}
		}

		// propagate update down all dependent GeoElements
		if (updateAlgos != null) {
			AlgoElement.updateCascadeAlgos(updateAlgos);
		}

		if (updateAlgos != null) {
			App app = kernel.getApplication();
			if (app.isUsingFullGui() && app.getGuiManager() != null) {
				app.getGuiManager().updateConstructionProtocol();
			}
		}

		return updateAlgos != null;
	}

	/**
	 * Adds the given Construction Element to this Construction at position
	 * index
	 * @param ce element to be added
	 * @param index index
	 */
	public void addToConstructionList(ConstructionElement ce, int index) {
		++step;
		ceList.add(index, ce);
		updateConstructionIndex(index);

		// update cas row references
		if (ce instanceof GeoCasCell) {
			updateCasCellRows();
		}

		updateAllConstructionProtocolAlgorithms();
	}

	/**
	 * Tells all GeoCasCells that the order of cas cells may have changed. They
	 * can then update their row number and input strings with row references.
	 */
	public void updateCasCellRows() {
		// update all row numbers first
		int counter = 0;
		for (ConstructionElement ce : ceList) {
			if (ce instanceof GeoCasCell) {
				((GeoCasCell) ce).setRowNumber(counter);
				counter++;
			} else if (ce instanceof AlgoCasCellInterface) {
				((AlgoCasCellInterface) ce).getCasCell().setRowNumber(counter);
				counter++;
			}
		}

		// now update all row references
		for (ConstructionElement ce : ceList) {
			if (ce instanceof GeoCasCell) {
				((GeoCasCell) ce).updateInputStringWithRowReferences();
			} else if (ce instanceof AlgoCasCellInterface) {
				((AlgoCasCellInterface) ce).getCasCell()
						.updateInputStringWithRowReferences();
			}
		}
	}

	/**
	 * Moves object at position from to position to in this construction.
	 * @param fromIndex index of element to be moved
	 * @param toIndex target index of this element
	 * @return whether construction list was changed or not.
	 */
	public boolean moveInConstructionList(int fromIndex, int toIndex) {
		// check if move is possible
		ConstructionElement ce = ceList.get(fromIndex);
		boolean change = fromIndex != toIndex
				&& ce.getMinConstructionIndex() <= toIndex
				&& toIndex <= ce.getMaxConstructionIndex();
		if (change) {
			// move the construction element
			ceList.remove(fromIndex);
			ceList.add(toIndex, ce);

			// update construction indices
			updateConstructionIndex(Math.min(toIndex, fromIndex));

			// update construction step
			if (fromIndex <= step && step < toIndex) {
				--step;

				ce.notifyRemove();
			} else if (toIndex <= step && step < fromIndex) {
				++step;

				ce.notifyAdd();
			}

			// update cas row references
			if (ce instanceof GeoCasCell || ce instanceof AlgoCasCellInterface) {
				updateCasCellRows();
			}

			updateAllConstructionProtocolAlgorithms();
		}

		return change;
	}

	/**
	 * Adds the given Construction Element to this Construction at position
	 * getStep() + 1.
	 * @param ce Construction element to be added
	 * @param checkContains : true to first check if ce is already in list
	 */
	public void addToConstructionList(ConstructionElement ce,
			boolean checkContains) {
		if (suppressLabelCreation) {
			return;
		}
		if (checkContains && ce.isInConstructionList()) {
			return;
		}

		addToConstructionList(ce, step + 1);
	}

	/**
	 * Removes the given Construction Element from this Construction and updates
	 * step if necessary (i.e. if ce.getConstructionIndex() &lt;= getStep()).
	 * @param ce ConstructionElement to be removed
	 */
	public void removeFromConstructionList(ConstructionElement ce) {

		int pos = ceList.indexOf(ce);
		if (pos == -1) {
			return;
		}
		if (pos <= step) {
			ceList.remove(ce);
			ce.setConstructionIndex(-1);
			--step;
		} else { // pos > step
			ceList.remove(ce);
			ce.setConstructionIndex(-1);
		}

		updateConstructionIndex(pos);

		// update cas row references
		if (ce instanceof GeoCasCell || (ce instanceof AlgoCasCellInterface)) {
			// needed for GGB-808
			// remove geoCasCell from CasView table before update of cell rows
			for (View view : kernel.views) {
				if (view.getViewID() == App.VIEW_CAS) {
					if (ce instanceof GeoCasCell) {
						view.remove((GeoCasCell) ce);
					}
					if (ce instanceof AlgoDependentCasCell) {
						view.remove(((AlgoDependentCasCell) ce).getCasCell());
					}
				}
			}
			updateCasCellRows();
		}

		updateAllConstructionProtocolAlgorithms();
	}

	/**
	 * Adds the given algorithm to this construction's algorithm list
	 * @param algo to be added
	 * @see #updateConstruction(boolean)
	 */
	public void addToAlgorithmList(AlgoElement algo) {
		algoList.add(algo);
	}

	/**
	 * The list of algo elements.
	 * @return list of algos
	 */
	public ArrayList<AlgoElement> getAlgoList() {
		return algoList;
	}

	/**
	 * Removes the given algorithm from this construction's algorithm list
	 * @param algo algo to be removed
	 */
	public void removeFromAlgorithmList(AlgoElement algo) {
		algoList.remove(algo);
	}

	/**
	 * Moves geo to given position toIndex in this construction. Note: if ce (or
	 * its parent algorithm) is not in the construction list nothing is done.
	 * @param geo element to bemoved
	 * @param toIndex new index
	 * @return whether construction list was changed or not.
	 */
	public boolean moveInConstructionList(GeoElement geo, int toIndex) {
		AlgoElement algoParent = geo.getParentAlgorithm();
		int fromIndex = (algoParent == null) ? ceList.indexOf(geo)
				: ceList.indexOf(algoParent);
		if (fromIndex >= 0) {
			return moveInConstructionList(fromIndex, toIndex);
		}
		return false;
	}

	/**
	 * Returns true iff geo is independent and in the construction list or geo
	 * is dependent and its parent algorithm is in the construction list.
	 * @param geo GeoElement to be looked for
	 * @return true iff geo or its parent algo are in construction list
	 */
	public boolean isInConstructionList(GeoElement geo) {
		if (geo.isIndependent()) {
			return geo.isInConstructionList();
		}
		return geo.getParentAlgorithm().isInConstructionList();
	}

	/**
	 * Updates all algorithms in this construction
	 */
	public final void updateAllAlgorithms() {
		// update all algorithms

		// *** algoList.size() can change during the loop
		for (int i = 0; i < algoList.size(); ++i) {
			AlgoElement algo = algoList.get(i);
			algo.update();
		}
	}

	/**
	 * Registers an algorithm that wants to be notified when
	 * setEuclidianViewBounds() is called.
	 * @param elem construction element to be registered
	 */
	public final void registerEuclidianViewCE(EuclidianViewCE elem) {
		if (!euclidianViewCE.contains(elem)) {
			euclidianViewCE.add(elem);
		}
	}

	/**
	 * @param elem construction element
	 * @return whether construction element is registered as EV listener
	 */
	public final boolean isRegisteredEuclidianViewCE(EuclidianViewCE elem) {
		return euclidianViewCE.contains(elem);
	}

	/**
	 * Unregisters an algorithm that wants to be notified when
	 * setEuclidianViewBounds() is called.
	 * @param elem construction element to be unregistered
	 */
	public final void unregisterEuclidianViewCE(EuclidianViewCE elem) {
		euclidianViewCE.remove(elem);
		if (this.corner5Algos != null) {
			this.corner5Algos.remove(elem);
		}
		if (this.corner11Algos != null) {
			this.corner11Algos.remove(elem);
		}
	}

	/**
	 * Calls euclidianViewUpdate on all registered euclidian view construction
	 * elements Those elements which return true, will also get an update of
	 * their dependent objects.
	 * @param type changed property
	 * @return true iff there were any elements to update
	 */
	public boolean notifyEuclidianViewCE(EVProperty type) {
		boolean didUpdate = false;
		ArrayList<EuclidianViewCE> toUpdate = type == EVProperty.SIZE ? this.corner5Algos
				: type == EVProperty.ROTATION ? this.corner11Algos
				: this.euclidianViewCE;
		if (toUpdate == null || toUpdate.size() == 0) {
			return false;
		}
		int size = toUpdate.size();
		AlgorithmSet updateSet = null;
		for (int i = 0; i < size; i++) {
			didUpdate = true;

			EuclidianViewCE elem = toUpdate.get(i);

			boolean needsUpdateCascade = elem.euclidianViewUpdate();
			if (needsUpdateCascade) {
				if (updateSet == null) {
					updateSet = new AlgorithmSet();
				}
				if (elem instanceof GeoElement) {
					GeoElement geo = (GeoElement) elem;
					updateSet.addAllSorted(geo.getAlgoUpdateSet());
				} else if (elem instanceof AlgoElement) {
					AlgoElement algo = (AlgoElement) elem;
					GeoElement[] geos = algo.getOutput();
					for (GeoElement geo : geos) {
						geo.update();
						updateSet.addAllSorted(geo.getAlgoUpdateSet());
					}
				}
			}
		}
		if (updateSet != null) {
			updateSet.updateAll();
		}
		return didUpdate;
	}

	/**
	 * Gets number of construction elements that are affected by zoom/pan of euclidian views.
	 * @return number of euclidian view-sensitive construction elements
	 */
	public int getEuclidianViewCECount() {
		return euclidianViewCE.size();
	}

	/**
	 * Updates all free random numbers of this construction.
	 */
	final public void updateAllFreeRandomGeosNoCascade() {
		if (randomElements == null) {
			return;
		}

		for (GeoElement num : randomElements) {
			if ((num.isGeoNumeric() || num instanceof GeoSymbolic)
					&& num.getParentAlgorithm() == null) {
				num.updateRandomNoCascade();
				num.update();
			}
		}
	}

	/**
	 * Adds a number to the set of random numbers of this construction.
	 * @param num Element to be added
	 */
	public void addRandomGeo(GeoElement num) {
		if (randomElements == null) {
			randomElements = new TreeSet<>();
		}
		randomElements.add(num);
		num.setRandomGeo(true);
	}

	/**
	 * Removes a number from the set of random numbers of this construction.
	 * @param element Element to be removed
	 */
	public void removeRandomGeo(GeoElement element) {
		if (randomElements != null) {
			randomElements.remove(element);
		}
		element.setRandomGeo(false);
	}

	/**
	 * Updates all objects in this construction.
	 * @param randomize whether to also update random algos
	 */
	final public void updateConstruction(boolean randomize) {
		// collect notifyUpdate calls using xAxis as dummy geo
		updateConstructionRunning = true;
		try {
			// update all independent GeoElements
			int size = ceList.size();
			for (int i = 0; i < size; ++i) {
				ConstructionElement ce = ceList.get(i);
				if (ce.isIndependent()) {
					ce.update();
				}
			}

			// update all free random numbers() (dependent random numbers will
			// be updated from algo list)
			// no update cascade is done: algos will be updated
			if (randomize) {
				updateAllFreeRandomGeosNoCascade();
			}

			// init and update all algorithms
			// make sure we call algo.initNearToRelationship() fist
			// for all algorithms because algo.update() could have
			// the side-effect to call updateCascade() for points
			// that have locateables (see GeoPoint.update())
			size = algoList.size();

			// init near to relationship for all algorithms:
			// this makes sure intersection points stay at their saved positions
			for (int i = 0; i < size; ++i) {
				AlgoElement algo = algoList.get(i);
				algo.initForNearToRelationship();
			}

			// copy array to avoid problems with the list changing during the
			// loop
			// eg Polygon[A,B,RandomBetween[4,5]]
			// http://www.geogebra.org/forum/viewtopic.php?p=56618
			ArrayList<AlgoElement> tempList = new ArrayList<>(
					algoList);

			// update all algorithms
			for (int i = 0; i < size; ++i) {
				AlgoElement algo = tempList.get(i);

				// reinit near to relationship to make sure points stay at their
				// saved position
				// keep this line, see
				// http://code.google.com/p/geogebra/issues/detail?id=62
				algo.initForNearToRelationship();

				// update algorithm
				if (algo instanceof AlgoLocusEquation) {
					((AlgoLocusEquation) algo).resetFingerprint(kernel, true);
				}
				if (randomize || !(algo instanceof SetRandomValue)
						|| !((SetRandomValue) algo).setRandomValue(algo.getOutput(0))) {
					algo.update();
				}
			}
		} finally {
			updateConstructionRunning = false;
		}
	}

	/**
	 * Similar to updateConstruction, but only updates CAS cells
	 */
	final public void updateCasCells() {
		// collect notifyUpdate calls using xAxis as dummy geo
		updateConstructionRunning = true;
		try {
			// update all independent GeoElements
			// check the size every time as Delete may change it
			for (int i = 0; i < ceList.size(); ++i) {
				ConstructionElement ce = ceList.get(i);
				if ((ce.isGeoElement() && ((GeoElement) ce).isGeoCasCell())
						|| ((ce instanceof AlgoElement)
						&& ce instanceof AlgoCasCellInterface)) {
					ce.update();
				}
			}
		} finally {
			updateConstructionRunning = false;
		}
	}

	/**
	 * Returns this construction in XML format. GeoGebra File Format.
	 * @param sb StringBuilder to which the XML is appended
	 * @param getListenersToo whether to include JS listener names
	 */
	public void getConstructionXML(XMLStringBuilder sb, boolean getListenersToo) {
		beginSpatialIdentityXML();
		try {
			// save construction elements
			sb.startOpeningTag("construction", 0);
			sb.attr("title", getTitle());
			sb.attr("author", getAuthor());
			sb.attr("date", getDate());
			sb.endTag();

			// worksheet text
			if (worksheetTextDefined()) {
				sb.startTag("worksheetText");
				sb.attr("above", getWorksheetText(0));
				sb.attr("below", getWorksheetText(1));
				sb.endTag();
			}

			if (!spatialIdentityRegistry.isEmpty()) {
				sb.append(new XMLStringBuilder(new StringBuilder(
						spatialIdentityRegistry.writeSpatialSection())));
			}

			getConstructionElementsXML(sb, getListenersToo);

			getGroupsXML(sb);

			sb.closeTag("construction");
		} catch (Exception e) {
			Log.debug(e);
		} finally {
			endSpatialIdentityXML();
		}
	}

	private void getGroupsXML(XMLStringBuilder sb) {
		for (Group gr : getGroups()) {
			gr.getXML(sb);
		}
	}

	/**
	 * Appends minimal version of the construction XML to given string builder.
	 * Only elements/commands are preserved, the rest is ignored.
	 * @param sb String builder
	 * @param getListenersToo whether to include JS listener names
	 */
	public void getConstructionElementsXML(XMLStringBuilder sb,
			boolean getListenersToo) {

		ConstructionElement ce;
		int size = ceList.size();
		for (int i = 0; i < size; ++i) {
			ce = ceList.get(i);
			ce.getXML(getListenersToo, sb);
		}
	}

	/**
	 * Appends minimal version of the construction XML to given string builder.
	 * OGP version. Only elements/commands are preserved, the rest is ignored.
	 * @param sb String builder
	 * @param statement The statement to prove
	 */
	public void getConstructionElementsXML_OGP(XMLStringBuilder sb,
			GeoElement statement) {

		ConstructionElement ce;
		int size = ceList.size();
		for (int i = 0; i < size; ++i) {
			ce = ceList.get(i);
			if (!(ce instanceof AlgoProve)
					&& !(ce instanceof AlgoProveDetails)) {
				// Collecting non-Prove* elements:
				ce.getXML_OGP(sb);
			}
		}
		// Inserting Prove* element:
		statement.getAlgorithmList().get(0).getXML_OGP(sb);
	}

	/*
	 * Construction List Management
	 */

	/**
	 * Returns the ConstructionElement for the given construction index.
	 * @param index Construction index of element to look for
	 * @return the ConstructionElement for the given construction index.
	 */
	public ConstructionElement getConstructionElement(int index) {
		if (index < 0 || index >= ceList.size()) {
			return null;
		}
		return ceList.get(index);
	}

	/**
	 * @return first geo if exists
	 */
	public GeoElement getFirstGeo() {

		ConstructionElement ce;
		GeoElement geo = null;
		int index = 0;

		while (index < ceList.size() && geo == null) {
			ce = ceList.get(index);
			if (ce instanceof GeoElement) {
				geo = (GeoElement) ce;
			}
			index++;
		}

		return geo;

	}

	/**
	 * Returns a set with all labeled GeoElement objects of this construction in
	 * construction order.
	 * @return set with all labeled geos in construction order.
	 */
	final public TreeSet<GeoElement> getGeoSetConstructionOrder() {
		return geoSetConsOrder;
	}

	/**
	 * Returns a set with all labeled GeoElement and all GeoCasCell objects of
	 * this construction in construction order.
	 * @return set with all labeled geos and CAS cells in construction order.
	 */
	final public TreeSet<GeoElement> getGeoSetWithCasCellsConstructionOrder() {
		return geoSetWithCasCells;
	}

	/**
	 * Returns a set with all labeled GeoElement objects of this construction in
	 * alphabetical order of their labels.
	 * @return set with all labeled geos in alphabetical order.
	 */
	final public TreeSet<GeoElement> getGeoSetLabelOrder() {
		return geoSetLabelOrder;
	}

	/**
	 * Starts to collect all redefinition calls for the current construction.
	 * This is used to improve performance of many redefines in the spreadsheet
	 * caused by e.g. relative copy.
	 * @see #processCollectedRedefineCalls()
	 */
	public void startCollectingRedefineCalls() {
		spatialIdentityRegistry.requireRedefineHostCaptureAllowed();
		spatialRedefineHostEpoch = Math.addExact(spatialRedefineHostEpoch, 1);
		collectedSpatialRedefineHostEpoch = spatialRedefineHostEpoch;
		collectedRedefineRollbackXml = spatialIdentityRegistry.isEmpty() ? null
				: getCurrentUndoXML(false).toString();
		collectRedefineCalls = true;
		if (redefineMap == null) {
			redefineMap = new HashMap<>();
		}
		redefineMap.clear();
		if (spatialRedefineMap == null) {
			spatialRedefineMap = new LinkedHashMap<>();
		}
		spatialRedefineMap.clear();
	}

	/**
	 * Stops collecting redefine calls.
	 * @see #processCollectedRedefineCalls()
	 */
	public void stopCollectingRedefineCalls() {
		spatialIdentityRegistry.requireRedefineHostCaptureAllowed();
		collectRedefineCalls = false;
		if (spatialRedefineMap != null) {
			rollbackCollectedSpatialRedefines();
		}
		if (redefineMap != null) {
			redefineMap.clear();
		}
		if (spatialRedefineMap != null) {
			spatialRedefineMap.clear();
		}
		collectedRedefineRollbackXml = null;
		if (collectedSpatialRedefineHostEpoch > 0
				&& spatialRedefineHostEpoch
						== collectedSpatialRedefineHostEpoch) {
			spatialRedefineHostEpoch = Math.addExact(spatialRedefineHostEpoch, 1);
		}
		collectedSpatialRedefineHostEpoch = 0;
	}

	/**
	 * Stages a provider-validated participating redefine in the current collected
	 * host batch. Algebra's independent/in-place shortcuts must not publish or
	 * mutate the old value before the batch XML and identity graph are ready to
	 * switch atomically.
	 *
	 * @param oldGeo exact participating output being redefined
	 * @param candidate provider-enumerated candidate for the targeted role
	 * @param transaction sealed retained redefine transaction
	 * @return whether collection was active and the redefine was staged
	 */
	public boolean stageCollectedSpatialRedefine(GeoElement oldGeo,
			GeoElement candidate, SpatialRedefineTransaction transaction) {
		if (!collectRedefineCalls) {
			return false;
		}
		if (transaction == null) {
			throw new SpatialIdentityException(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Collected spatial redefine requires its sealed transaction"));
		}
		if (transaction.getDecision() != SpatialRedefineDecision.RETAIN) {
			throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
					"Collected spatial redefine admits retained provider groups only",
					transaction.getContext().getOldId()));
		}
		if (redefineMap.containsKey(oldGeo)) {
			throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Collected batch repeats one participating host output",
					transaction.getContext().getOldId()));
		}
		for (SpatialRedefineTransaction existing : spatialRedefineMap.values()) {
			if (sharesOldSpatialOutput(existing, transaction)) {
				throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Collected batch overlaps one stable-role output group",
						transaction.getContext().getOldId()));
			}
		}
		spatialRedefineMap.put(Objects.requireNonNull(oldGeo), transaction);
		redefineMap.put(oldGeo, Objects.requireNonNull(candidate));
		return true;
	}

	private static boolean sharesOldSpatialOutput(
			SpatialRedefineTransaction first,
			SpatialRedefineTransaction second) {
		for (SpatialRedefinePersistedOutput left
				: first.getContext().getOldOutputs().getOutputs()) {
			for (SpatialRedefinePersistedOutput right
					: second.getContext().getOldOutputs().getOutputs()) {
				if (left.getId().equals(right.getId())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Replaces oldGeo by newGeo in the current construction. This may change
	 * the logic of the construction and is a very powerful operation
	 * @param oldGeo Geo to be replaced.
	 * @param newGeo Geo to be used instead.
	 * @throws CircularDefinitionException i.e. for circular definition
	 * @throws XMLParseException if replacement creates invalid XML
	 */
	public void replace(GeoElement oldGeo, GeoElement newGeo)
			throws CircularDefinitionException, XMLParseException {
		replaceInternal(oldGeo, newGeo, null, true, null);
	}

	/**
	 * Replaces oldGeo by newGeo in the current construction. This may change
	 * the logic of the construction and is a very powerful operation
	 * @param oldGeo Geo to be replaced.
	 * @param newGeo Geo to be used instead.
	 * @param info EvalInfo (can be null)
	 * @throws CircularDefinitionException i.e. for circular definition
	 * @throws XMLParseException if replacement causes invalid XML
	 */
	public void replace(GeoElement oldGeo, GeoElement newGeo, EvalInfo info)
			throws CircularDefinitionException, XMLParseException {
		replaceInternal(oldGeo, newGeo, info, true, null);
	}

	/**
	 * Executes a host replacement whose target was discovered only by a label or
	 * XML compatibility seam. Participating geos are rejected because this route
	 * has no explicit semantic old-target authority.
	 *
	 * @param oldGeo label/XML-discovered host target
	 * @param newGeo replacement candidate
	 * @throws CircularDefinitionException for a circular definition
	 * @throws XMLParseException if replacement creates invalid XML
	 */
	public void replaceWithoutSpatialRedefineAuthority(GeoElement oldGeo,
			GeoElement newGeo) throws CircularDefinitionException, XMLParseException {
		replaceInternal(oldGeo, newGeo, null, false, null);
	}

	/**
	 * Replaces an explicit target using a context captured before the caller
	 * created persistent helper objects or otherwise began its host operation.
	 *
	 * @param oldGeo explicit old target
	 * @param newGeo replacement candidate
	 * @param operationContext pre-operation identity and rollback context
	 * @throws CircularDefinitionException for a circular definition
	 * @throws XMLParseException if replacement creates invalid XML
	 */
	public void replaceFromSpatialRedefineOperation(GeoElement oldGeo,
			GeoElement newGeo, SpatialRedefineContext operationContext)
			throws CircularDefinitionException, XMLParseException {
		replaceInternal(oldGeo, newGeo, null, true, operationContext);
	}

	private void replaceInternal(GeoElement oldGeo, GeoElement newGeo, EvalInfo info,
			boolean captureExplicitOldTarget,
			SpatialRedefineContext preOperationContext)
			throws CircularDefinitionException, XMLParseException {
		boolean participating = oldGeo != null
				&& spatialIdentityRegistry.isParticipating(oldGeo);
		String entryRollbackXml = participating || preOperationContext != null
				? getCurrentUndoXML(false).toString() : null;
		boolean candidateInstalledAtEntry = newGeo != null
				&& isInConstructionList(newGeo);
		String operationRollbackXml = entryRollbackXml;
		SpatialRedefineContext operationContext = preOperationContext;
		if (preOperationContext != null) {
			if (oldGeo == null || preOperationContext.getOldTarget() != oldGeo
					|| oldGeo.getConstruction() != this) {
				throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
						"Pre-operation redefine context belongs to another target",
						preOperationContext.getOldId()));
			}
			try {
				spatialIdentityRegistry.validateRedefineContext(preOperationContext,
						oldGeo);
				spatialIdentityRegistry.validateRedefineHostRollback(
						preOperationContext);
			} catch (RuntimeException | MyError failure) {
				// Validation precedes all host mutation in this method. Keep the
				// entry state rather than applying an untrusted earlier snapshot.
				throw failure;
			}
			operationRollbackXml = preOperationContext.getRollbackXml();
		}
		SpatialRedefineTransaction suppliedTransaction = info == null ? null
				: info.getSpatialRedefineTransaction();
		EvalInfo operationInfo = info;
		if (participating && suppliedTransaction == null && captureExplicitOldTarget) {
			if (operationContext == null && info != null) {
				operationContext = info.getSpatialRedefineContext();
			}
			if (preOperationContext == null) {
				operationContext = operationContext == null
						? spatialIdentityRegistry.captureRedefineContext(oldGeo)
						: operationContext.withRollbackXml(operationRollbackXml);
			}
			if (operationContext != null) {
				operationInfo = (info == null ? new EvalInfo(true) : info)
						.withSpatialRedefineContext(operationContext);
			}
		}
		SpatialRedefineTransaction spatialTransaction;
		try {
			spatialTransaction = prepareSpatialRedefine(oldGeo, newGeo, operationInfo);
		} catch (RuntimeException | MyError failure) {
			if (suppliedTransaction != null
					&& suppliedTransaction.getState()
							!= SpatialRedefineTransaction.State.ROLLED_BACK) {
				rollbackSpatialRedefine(suppliedTransaction,
						candidateInstalledAtEntry ? null : entryRollbackXml);
			} else if (operationContext != null
					&& spatialIdentityRegistry.isRedefineHostRollbackAvailable(
							operationContext)) {
				rollbackSpatialRedefinePreparation(operationContext);
			}
			throw failure;
		}
		EvalInfo preparedInfo = spatialTransaction == null || info == null ? info
				: operationInfo.withSpatialRedefineTransaction(spatialTransaction);
		try (RedefinePublicationLease ignored =
				beginSpatialRedefinePublicationLease(spatialTransaction)) {
			replacePrepared(oldGeo, newGeo, preparedInfo, spatialTransaction);
		} catch (CircularDefinitionException | XMLParseException | RuntimeException
				| MyError failure) {
			if (spatialTransaction != null
					&& spatialTransaction.getState()
							!= SpatialRedefineTransaction.State.ROLLED_BACK) {
				rollbackSpatialRedefine(spatialTransaction,
						suppliedTransaction != null && !candidateInstalledAtEntry
								? entryRollbackXml : null);
			} else if (operationContext != null
					&& spatialIdentityRegistry.isRedefineHostRollbackAvailable(
							operationContext)) {
				rollbackSpatialRedefinePreparation(operationContext);
			} else if (operationContext == null && entryRollbackXml != null) {
				restoreSpatialRedefineSnapshot(entryRollbackXml);
			}
			throw failure;
		}
		if (spatialTransaction != null && suppliedTransaction == null
				&& preOperationContext == null && !collectRedefineCalls) {
			spatialIdentityRegistry.completeRedefineHostOperation(
					spatialTransaction.getContext());
		}
	}

	private void replacePrepared(GeoElement oldGeo, GeoElement newGeo, EvalInfo info,
			SpatialRedefineTransaction spatialTransaction)
			throws CircularDefinitionException, XMLParseException {
		if (oldGeo == null || newGeo == null || oldGeo == newGeo) {
			commitSpatialRedefine(spatialTransaction, oldGeo);
			return;
		}
		// assignment v=? should make v undefined, not change its type
		if (oldGeo.isIndependent() && newGeo instanceof GeoNumeric
				&& newGeo.isIndependent() && !newGeo.isDefined()) {
			oldGeo.setUndefined();
			oldGeo.updateRepaint();
			commitSpatialRedefine(spatialTransaction, oldGeo);
			return;
		}

		// if an object is redefined the same (eg in a script) rather than
		// reloading the whole XML, just update it
		// NB xmlTemplate has faster serialization than maxPrecision template
		if (oldGeo.getDefinition(StringTemplate.xmlTemplate)
				.equals(newGeo.getDefinition(StringTemplate.xmlTemplate))
				&& oldGeo.getParentAlgorithm() != null) {
			ArrayList<AlgoElement> ae = new ArrayList<>();
			ae.add(oldGeo.getParentAlgorithm());

			// make sure typing a=random() twice updates OK
			oldGeo.getParentAlgorithm().updateUnlabeledRandomGeos();

			// make sure b=a+1 also updates
			AlgoElement.updateCascadeAlgos(ae);
			if (updateConstructionOrder(oldGeo, newGeo)) {
				kernel.notifyRemove(oldGeo);
				kernel.notifyAdd(oldGeo);
			}
			// repaint here to make sure #4114 is OK
			kernel.notifyRepaint();
			commitSpatialRedefine(spatialTransaction, oldGeo);
			return;
		}
		if (softRedefine(oldGeo, newGeo)) {
			commitSpatialRedefine(spatialTransaction, oldGeo);
			return;
		}
		boolean preserveSpatialGroupDependencies =
				requiresDependencyPreservingSpatialRebuild(oldGeo, newGeo,
						spatialTransaction);

		// if oldGeo does not have any children, we can simply
		// delete oldGeo and give newGeo the name of oldGeo
		if (!oldGeo.hasChildren() && !preserveSpatialGroupDependencies) {
			String oldGeoLabel = oldGeo.getLabelSimple();
			newGeo.moveDependencies(oldGeo);
			final Group grp = oldGeo.getParentGroup();
			removeForReplace(oldGeo, spatialTransaction);
			// set properties first, set label later. See #933
			copyStyleForRedefine(oldGeo, newGeo);

			if (newGeo.isIndependent()) {
				addToConstructionList(newGeo, true);
			} else {
				AlgoElement parentAlgo = newGeo.getParentAlgorithm();
				addToConstructionList(parentAlgo, true);
				// make sure all output objects get labels, see #218
				parentAlgo.resetLabels(oldGeoLabel);
			}

			// copy label of oldGeo to newGeo
			// use setLoadedLabel() instead of setLabel() to make sure that
			// hidden objects also get the label, see #379
			newGeo.setLoadedLabel(oldGeoLabel);
			layerManager.replace(oldGeo.getOrdering(), newGeo);
			if (grp != null) {
				newGeo.setParentGroup(grp);
				grp.getGroupedGeos().remove(oldGeo);
				grp.getGroupedGeos().add(newGeo);
			}
			if (newGeo.isGeoText()) {
				newGeo.updateRepaint();
			}

			commitSpatialRedefine(spatialTransaction, newGeo);
			return;
		}

		// check for circular definition
		if (newGeo.isChildOf(oldGeo)) {

			// check for eg a = a + 1, A = A + (1,1), a = !a
			if (oldGeo.isIndependent() && oldGeo instanceof GeoNumeric
					&& newGeo instanceof GeoNumeric) {

				((GeoNumeric) oldGeo)
						.setValue(((GeoNumeric) newGeo).getDouble());
				oldGeo.updateRepaint();
				newGeo.remove();
				commitSpatialRedefine(spatialTransaction, oldGeo);
				return;

			} else if (oldGeo.isIndependent() && !(oldGeo instanceof GeoSymbolic) && (
					oldGeo.isGeoPoint() || oldGeo.isGeoVector() || oldGeo.isGeoBoolean())) {
				oldGeo.set(newGeo);
				oldGeo.setDefinition(null);
				oldGeo.updateRepaint();
				newGeo.remove();
				commitSpatialRedefine(spatialTransaction, oldGeo);
				return;

			} else {
				throw new CircularDefinitionException();
			}
		}
		if (collectRedefineCalls && spatialTransaction != null
				&& spatialTransaction.getDecision() != SpatialRedefineDecision.RETAIN) {
			throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
					"Collected G9A1 redefine admits only retained single outputs",
					spatialTransaction.getContext().getOldId()));
		}
		// 1) remove all brothers and sisters of oldGeo
		// 2) move all predecessors of newGeo to the left of oldGeo in
		// construction list
		if (!preserveSpatialGroupDependencies) {
			prepareReplaceWithSpatialRetirementAuthority(oldGeo, newGeo,
					spatialTransaction);
		} else {
			// Keep the provider-mapped old output group intact while moving the
			// installed candidate and its predecessors ahead of the old dependents.
			// The subsequent role-label snapshot can then bind every dependent in
			// normal construction order without destroying a sibling first.
			updateConstructionOrder(oldGeo, newGeo);
		}

		if (collectRedefineCalls) {
			if (spatialTransaction != null) {
				stageCollectedSpatialRedefine(oldGeo, newGeo,
						spatialTransaction);
				return;
			}
			// collecting redefine calls in redefineMap
			redefineMap.put(oldGeo, newGeo);
			return;
		}
		App app = kernel.getApplication();

		// store views for plane
		app.getCompanion().storeViewCreators();

		SelectionManager selection = kernel.getApplication()
				.getSelectionManager();
		boolean moveMode = EuclidianConstants.isMoveOrSelectionMode(app.getMode())
				&& selection.getSelectedGeos().size() > 0;
		String oldSelection = null;
		if (moveMode) {
			oldSelection = selection.getSelectedGeos().get(0)
					.getLabelSimple();
		}
		// get current construction XML
		isGettingXMLForReplace = true;
		StringBuilder consXML = getCurrentUndoXML(false);
		isGettingXMLForReplace = false;

		// 3) replace oldGeo by newGeo in XML
		String oldXML = consXML.toString();
		boolean refreshSpatialGroupXml = preserveSpatialGroupDependencies
				&& prepareDependencyPreservingSpatialGroupLabels(
						spatialTransaction, newGeo);
		boolean canReplace;
		beginSpatialIdentityXML();
		try (SpatialIdentityRegistry.SerializationOverlay ignored =
				spatialTransaction == null ? null
						: spatialIdentityRegistry.beginRedefineSerializationOverlay(
								spatialTransaction)) {
			if (spatialTransaction != null || refreshSpatialGroupXml) {
				// Candidate dependencies are promoted only after provider approval.
				// Refresh inside the serialization overlay so their element XML and
				// the transaction record view carry the same durable attachments.
				isGettingXMLForReplace = true;
				consXML.setLength(0);
				consXML.append(getCurrentUndoXML(false));
				isGettingXMLForReplace = false;
			}
			canReplace = doReplaceInXML(consXML, oldGeo, newGeo);
			if (canReplace && spatialTransaction != null) {
				replaceSpatialIdentitySection(consXML,
						spatialIdentityRegistry.writeSpatialSection(),
						spatialIdentityRegistry.writeSpatialSectionForRedefine(
								spatialTransaction));
			}
		} finally {
			endSpatialIdentityXML();
		}
		// moveDependencies(oldGeo,newGeo);

		// 4) build new construction
		if (canReplace) {
			if (spatialTransaction != null) {
				setNextSpatialIdentityRedefineRebuild(Collections.singletonList(
						spatialTransaction.getContext()));
			}
			buildConstructionWithGlobalListeners(new XMLStringBuilder(consXML),
					oldXML, info);
		} else {
			throw new MyError(getApplication().getLocalization(),
					Errors.ReplaceFailed);
		}
		if (spatialTransaction != null) {
			GeoElement result = spatialIdentityRegistry.getGeo(
					spatialTransaction.getDecidedId());
			if (result == null) {
				throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Rebuilt redefine result did not resolve by durable identity",
						spatialTransaction.getDecidedId()));
			}
			commitSpatialRedefine(spatialTransaction, result);
		}
		if (moveMode && oldSelection != null) {
			GeoElement selGeo = kernel.lookupLabel(oldSelection);
			selection.addSelectedGeo(selGeo, false, true);
			app.getActiveEuclidianView().getEuclidianController()
					.handleMovedElement(selGeo, false, PointerEventType.MOUSE);
		}

		// recall views for plane
		app.getCompanion().recallViewCreators();
	}

	private void removeForReplace(GeoElement oldGeo,
			SpatialRedefineTransaction spatialTransaction) {
		GeoElement previousRemovalTarget = geoBeingRemovedForReplace;
		Set<GeoElement> previousSpatialRemovalGroup =
				spatialGeosBeingRemovedForReplace;
		geoBeingRemovedForReplace = oldGeo;
		Set<GeoElement> operationGroup = spatialRetirementSuppressionGroup(
				spatialTransaction);
		if (operationGroup != null) {
			spatialGeosBeingRemovedForReplace = operationGroup;
		}
		try {
			oldGeo.setParentGroup(null);
			oldGeo.remove();
		} finally {
			geoBeingRemovedForReplace = previousRemovalTarget;
			spatialGeosBeingRemovedForReplace = previousSpatialRemovalGroup;
		}
	}

	/**
	 * Opens the exact construction-host publication boundary for one prepared
	 * redefine. An AlgebraProcessor-owned boundary is reused; collected calls
	 * carry every earlier pending context forward.
	 *
	 * @param transaction prepared provider-owned decision, or {@code null}
	 * @return owning lease, or {@code null} when an exact outer lease is active
	 */
	public RedefinePublicationLease beginSpatialRedefinePublicationLease(
			SpatialRedefineTransaction transaction) {
		if (transaction == null) {
			return null;
		}
		SpatialRedefineContext context = transaction.getContext();
		if (spatialIdentityRegistry.isRedefinePublicationLeaseActiveFor(context)) {
			return null;
		}
		ArrayList<SpatialRedefineContext> contexts = new ArrayList<>();
		Set<SpatialRedefineContext> unique = Collections.newSetFromMap(
				new IdentityHashMap<SpatialRedefineContext, Boolean>());
		if (collectRedefineCalls && spatialRedefineMap != null) {
			for (SpatialRedefineTransaction pending : spatialRedefineMap.values()) {
				if (unique.add(pending.getContext())) {
					contexts.add(pending.getContext());
				}
			}
		}
		if (unique.add(context)) {
			contexts.add(context);
		}
		return spatialIdentityRegistry.beginRedefinePublicationLease(contexts);
	}

	private void prepareReplaceWithSpatialRetirementAuthority(GeoElement oldGeo,
			GeoElement newGeo, SpatialRedefineTransaction transaction) {
		Set<GeoElement> previousSpatialRemovalGroup =
				spatialGeosBeingRemovedForReplace;
		Set<GeoElement> operationGroup = spatialRetirementSuppressionGroup(
				transaction);
		if (operationGroup != null) {
			spatialGeosBeingRemovedForReplace = operationGroup;
		}
		try {
			removeFreshRetiredHostDependents(transaction);
			prepareReplace(oldGeo, newGeo);
		} finally {
			spatialGeosBeingRemovedForReplace = previousSpatialRemovalGroup;
		}
	}

	private void removeFreshRetiredHostDependents(
			SpatialRedefineTransaction transaction) {
		if (transaction == null || transaction.getState()
				!= SpatialRedefineTransaction.State.PREPARED
				|| transaction.getDecision() != SpatialRedefineDecision.FRESH) {
			return;
		}
		Set<GeoElement> providerOutputs = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		for (SpatialRedefinePersistedOutput output
				: transaction.getContext().getOldOutputs().getOutputs()) {
			providerOutputs.add(output.getGeo());
		}
		IdentityHashMap<GeoElement, PersistentGeoId> retiredByGeo =
				new IdentityHashMap<>();
		for (SpatialIdentityId retiredId : transaction.getRetiredIds()) {
			if (retiredId instanceof PersistentGeoId) {
				PersistentGeoId persistentId = (PersistentGeoId) retiredId;
				GeoElement geo = spatialIdentityRegistry.getGeo(persistentId);
				if (geo == null || !persistentId.equals(
						spatialIdentityRegistry.getPersistentGeoId(geo))) {
					throw new SpatialIdentityException(
							SpatialIdentityDiagnostic.forSubject(
									SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
									"Fresh redefine retirement attachment is no longer current",
									persistentId));
				}
				if (!providerOutputs.contains(geo)) {
					retiredByGeo.put(geo, persistentId);
				}
			}
		}
		ArrayList<GeoElement> retiredDependents =
				new ArrayList<>(retiredByGeo.keySet());
		Collections.sort(retiredDependents, (first, second) -> {
			int byConstruction = Integer.compare(second.getConstructionIndex(),
					first.getConstructionIndex());
			return byConstruction != 0 ? byConstruction
					: retiredByGeo.get(first).compareTo(retiredByGeo.get(second));
		});
		for (GeoElement retired : retiredDependents) {
			PersistentGeoId expectedId = retiredByGeo.get(retired);
			if (spatialIdentityRegistry.getGeo(expectedId) != retired
					|| !expectedId.equals(
							spatialIdentityRegistry.getPersistentGeoId(retired))) {
				throw new SpatialIdentityException(
						SpatialIdentityDiagnostic.forSubject(
								SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
								"Fresh redefine retirement attachment is no longer current",
								expectedId));
			}
			if (isInConstructionList(retired)) {
				retired.setParentGroup(null);
				retired.remove();
			}
		}
	}

	private Set<GeoElement> spatialRetirementSuppressionGroup(
			SpatialRedefineTransaction transaction) {
		if (transaction == null || transaction.getState()
				!= SpatialRedefineTransaction.State.PREPARED) {
			return null;
		}
		Set<GeoElement> group = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		for (SpatialRedefinePersistedOutput output
				: transaction.getContext().getOldOutputs().getOutputs()) {
			GeoElement geo = output.getGeo();
			if (spatialIdentityRegistry.getGeo(output.getId()) != geo
					|| !output.getId().equals(
							spatialIdentityRegistry.getPersistentGeoId(geo))) {
				throw new SpatialIdentityException(
						SpatialIdentityDiagnostic.forSubject(
								SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
								"Grouped redefine removal authority is no longer current",
								output.getId()));
			}
			group.add(geo);
		}
		if (transaction.getDecision() == SpatialRedefineDecision.FRESH) {
			for (SpatialIdentityId retiredId : transaction.getRetiredIds()) {
				if (!(retiredId instanceof PersistentGeoId)) {
					continue;
				}
				PersistentGeoId persistentId = (PersistentGeoId) retiredId;
				GeoElement geo = spatialIdentityRegistry.getGeo(persistentId);
				if (geo == null || !persistentId.equals(
						spatialIdentityRegistry.getPersistentGeoId(geo))) {
					throw new SpatialIdentityException(
							SpatialIdentityDiagnostic.forSubject(
									SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
									"Fresh redefine retirement attachment is no longer current",
									persistentId));
				}
				group.add(geo);
			}
		}
		return group;
	}

	private boolean requiresDependencyPreservingSpatialRebuild(GeoElement oldGeo,
			GeoElement newGeo, SpatialRedefineTransaction transaction) {
		if (transaction == null || transaction.getState()
				!= SpatialRedefineTransaction.State.PREPARED
				|| transaction.getDecision() != SpatialRedefineDecision.RETAIN
				|| transaction.getContext().getOldOutputs().size() <= 1
				|| oldGeo.getParentAlgorithm() == newGeo.getParentAlgorithm()) {
			return false;
		}
		for (SpatialRedefinePersistedOutput output
				: transaction.getContext().getOldOutputs().getOutputs()) {
			if (output.getGeo().hasChildren()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Aligns XML labels only after provider-owned stable-role validation. Labels
	 * transport host dependencies during rebuild; they never decide continuity.
	 *
	 * @return whether the labeled candidate group must be reserialized in-place
	 */
	private boolean prepareDependencyPreservingSpatialGroupLabels(
			SpatialRedefineTransaction transaction, GeoElement targetedCandidate) {
		boolean installedCandidate = targetedCandidate.isLabelSet();
		for (String role : transaction.getContext().getOldOutputs().getRoles()) {
			SpatialRedefinePersistedOutput oldOutput =
					transaction.getContext().getOldOutputs().get(role);
			SpatialRedefineCandidateOutput candidateOutput =
					transaction.getProposal().getCandidateOutputs().get(role);
			GeoElement candidateGeo = candidateOutput.getGeo();
			if (installedCandidate && !candidateGeo.isLabelSet()) {
				candidateGeo.setLabel(null);
			}
			if (installedCandidate) {
				// Provider roles, not labels, establish the correspondence. Preserve
				// the old host names as dependency transport so an intact child command
				// continues to resolve after the candidate parent moves into place.
				candidateGeo.setLabelSimple(oldOutput.getGeo().getLabelSimple());
			} else {
				copyStyleForRedefine(oldOutput.getGeo(), candidateGeo);
				if (candidateGeo != targetedCandidate) {
					candidateGeo.setLabelSimple(
							oldOutput.getGeo().getLabelSimple());
					candidateGeo.setLabelSet(true);
				}
			}
		}
		return installedCandidate;
	}

	private boolean softRedefine(GeoElement oldGeo, GeoElement newGeo) {
		AlgoElement oldParent = oldGeo.getParentAlgorithm();
		AlgoElement newParent = newGeo.getParentAlgorithm();
		if (oldParent != null && newParent != null) {
			return oldParent.setFrom(newParent);
		}
		return false;
	}

	private SpatialRedefineTransaction prepareSpatialRedefine(GeoElement oldGeo,
			GeoElement newGeo, EvalInfo info) {
		if (oldGeo == null || !spatialIdentityRegistry.isParticipating(oldGeo)) {
			return null;
		}
		SpatialRedefineTransaction transaction = info == null ? null
				: info.getSpatialRedefineTransaction();
		if (transaction != null) {
			spatialIdentityRegistry.validatePreparedRedefineTransaction(transaction);
			if (transaction.getContext().getOldTarget() != oldGeo
					|| transaction.getProposal().getCandidate() != newGeo) {
				throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
						"Redefine transaction does not belong to the explicit target",
						transaction.getContext().getOldId()));
			}
		} else {
			SpatialRedefineContext context = info == null ? null
					: info.getSpatialRedefineContext();
			if (context != null && context.getOldTarget() != oldGeo) {
				throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
						"Redefine context does not belong to the explicit target",
						context.getOldId()));
			}
			transaction = spatialIdentityRegistry.prepareRedefine(context, newGeo,
					java.util.Collections.singletonList(newGeo), info != null
							&& info.isSpatialReplacementOperationSelected(), info == null
									? null
									: info.getSpatialRedefineCandidateParticipation());
		}
		if (transaction.getDecision() == SpatialRedefineDecision.REJECT) {
			transaction.rollback();
			throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
					"Provider rejected redefine before host mutation",
					transaction.getContext().getOldId()));
		}
		try {
			transaction.activateCandidateParticipation();
		} catch (RuntimeException failure) {
			transaction.rollback();
			throw failure;
		}
		return transaction;
	}

	private static void commitSpatialRedefine(
			SpatialRedefineTransaction transaction, GeoElement result) {
		if (transaction != null
				&& transaction.getState() == SpatialRedefineTransaction.State.PREPARED) {
			transaction.commit(result);
		}
	}

	/** Restores the exact pre-parse host snapshot and abandons the decision. */
	public void rollbackSpatialRedefine(SpatialRedefineTransaction transaction) {
		rollbackSpatialRedefine(transaction, null);
	}

	private void rollbackSpatialRedefine(SpatialRedefineTransaction transaction,
			String explicitRollbackXml) {
		if (transaction != null
				&& transaction.getState()
						!= SpatialRedefineTransaction.State.ROLLED_BACK) {
			if (collectRedefineCalls && spatialRedefineMap != null
					&& !spatialRedefineMap.isEmpty()) {
				rollbackCollectedSpatialRedefines(transaction);
				return;
			}
			String rollbackXml = explicitRollbackXml == null
					? transaction.getContext().getRollbackXml() : explicitRollbackXml;
			try {
				spatialIdentityRegistry.closeRedefinePublicationLeaseForRollback(
						transaction.getContext());
				spatialIdentityRegistry.claimRedefineHostRollback(
						transaction.getContext());
			} catch (RuntimeException stale) {
				if (transaction.getState()
						== SpatialRedefineTransaction.State.PREPARED) {
					transaction.rollback();
				}
				throw stale;
			}
			if (transaction.getState()
					== SpatialRedefineTransaction.State.PREPARED) {
				transaction.rollback();
			}
			restoreSpatialRedefineSnapshot(rollbackXml);
		}
	}

	/** Restores a pre-candidate snapshot when preparation failed before a transaction. */
	public void rollbackSpatialRedefinePreparation(SpatialRedefineContext context) {
		if (context != null) {
			if (context.getOldTarget().getConstruction() != this) {
				throw new IllegalArgumentException(
						"Spatial redefine context belongs to another construction");
			}
			if (collectRedefineCalls && spatialRedefineMap != null
					&& !spatialRedefineMap.isEmpty()) {
				rollbackCollectedSpatialRedefines(null, context);
				return;
			}
			spatialIdentityRegistry.closeRedefinePublicationLeaseForRollback(context);
			spatialIdentityRegistry.claimRedefineHostRollback(context);
			restoreSpatialRedefineSnapshot(context.getRollbackXml());
		}
	}

	/** Closes a successful caller-owned pre-operation redefine boundary. */
	public void completeSpatialRedefineOperation(SpatialRedefineContext context) {
		if (context == null || context.getOldTarget().getConstruction() != this) {
			throw new IllegalArgumentException(
					"Spatial redefine context belongs to another construction");
		}
		spatialIdentityRegistry.completeRedefineHostOperation(context);
	}

	private void rollbackCollectedSpatialRedefines() {
		rollbackCollectedSpatialRedefines(null, null);
	}

	private void rollbackCollectedSpatialRedefines(
			SpatialRedefineTransaction additional) {
		rollbackCollectedSpatialRedefines(additional, null);
	}

	private void rollbackCollectedSpatialRedefines(
			SpatialRedefineTransaction additional,
			SpatialRedefineContext additionalContext) {
		rollbackCollectedSpatialRedefines(additional, additionalContext, true);
	}

	private void rollbackCollectedSpatialRedefines(
			SpatialRedefineTransaction additional,
			SpatialRedefineContext additionalContext, boolean restoreHostSnapshot) {
		String transactionRollbackXml = null;
		ArrayList<SpatialRedefineTransaction> rollback = new ArrayList<>();
		ArrayList<SpatialRedefineTransaction> abandon = new ArrayList<>();
		Set<SpatialRedefineTransaction> unique = Collections.newSetFromMap(
				new IdentityHashMap<SpatialRedefineTransaction, Boolean>());
		ArrayList<SpatialRedefineTransaction> candidates = new ArrayList<>();
		if (spatialRedefineMap != null) {
			for (SpatialRedefineTransaction transaction : spatialRedefineMap.values()) {
				if (unique.add(transaction)) {
					candidates.add(transaction);
				}
			}
		}
		if (additional != null && unique.add(additional)) {
			candidates.add(additional);
		}
		ArrayList<SpatialRedefineContext> contexts = new ArrayList<>();
		Set<SpatialRedefineContext> uniqueContexts = Collections.newSetFromMap(
				new IdentityHashMap<SpatialRedefineContext, Boolean>());
		for (SpatialRedefineTransaction transaction : candidates) {
			if (uniqueContexts.add(transaction.getContext())) {
				contexts.add(transaction.getContext());
			}
		}
		boolean additionalContextIsSeparate = additionalContext != null
				&& uniqueContexts.add(additionalContext);
		if (additionalContextIsSeparate) {
			contexts.add(additionalContext);
		}
		if (!contexts.isEmpty()) {
			spatialIdentityRegistry.closeRedefinePublicationLeaseForRollback(
					contexts.get(0));
		}
		for (SpatialRedefineTransaction transaction : candidates) {
			if (transaction.getState()
					!= SpatialRedefineTransaction.State.ROLLED_BACK
					&& spatialIdentityRegistry.isRedefineHostRollbackAvailable(
							transaction.getContext())) {
				if (transactionRollbackXml == null) {
					transactionRollbackXml = transaction.getContext().getRollbackXml();
				}
				rollback.add(transaction);
			} else if (transaction.getState()
					== SpatialRedefineTransaction.State.PREPARED) {
				abandon.add(transaction);
			}
		}
		if (additionalContextIsSeparate
				&& spatialIdentityRegistry.isRedefineHostRollbackAvailable(
						additionalContext)) {
			if (transactionRollbackXml == null) {
				transactionRollbackXml = additionalContext.getRollbackXml();
			}
			spatialIdentityRegistry.validateRedefineHostRollback(additionalContext);
		} else if (additionalContextIsSeparate) {
			throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Collected redefine preparation rollback is stale",
					additionalContext.getOldId()));
		}
		// Validate the complete batch before consuming any one-shot authority.
		for (SpatialRedefineTransaction transaction : rollback) {
			spatialIdentityRegistry.validateRedefineHostRollback(
					transaction.getContext());
		}
		for (SpatialRedefineTransaction transaction : abandon) {
			// A superseded batch must never replay its XML, but it must not leak
			// fresh reservations either. Defer this mutation until every replaying
			// context has passed the all-or-nothing preflight above.
			transaction.rollback();
		}
		for (SpatialRedefineTransaction transaction : rollback) {
			spatialIdentityRegistry.claimRedefineHostRollback(
					transaction.getContext());
			if (transaction.getState()
					== SpatialRedefineTransaction.State.PREPARED) {
				transaction.rollback();
			}
		}
		if (additionalContextIsSeparate) {
			spatialIdentityRegistry.claimRedefineHostRollback(additionalContext);
		}
		if (restoreHostSnapshot
				&& (!rollback.isEmpty() || additionalContextIsSeparate)) {
			String rollbackXml = collectedRedefineRollbackXml == null
					? transactionRollbackXml : collectedRedefineRollbackXml;
			restoreSpatialRedefineSnapshot(rollbackXml);
		} else if (!restoreHostSnapshot) {
			discardCollectedRedefineCandidates();
		}
	}

	private void discardCollectedRedefineCandidates() {
		if (redefineMap == null) {
			return;
		}
		for (Entry<GeoElement, GeoElement> entry : redefineMap.entrySet()) {
			GeoElement oldGeo = entry.getKey();
			GeoElement candidate = entry.getValue();
			if (candidate == oldGeo || !isInConstructionList(candidate)
					|| candidate.getParentAlgorithm() != null
							&& candidate.getParentAlgorithm()
									== oldGeo.getParentAlgorithm()) {
				continue;
			}
			candidate.remove();
		}
	}

	private void restoreSpatialRedefineSnapshot(String rollbackXml) {
		spatialSemanticRuntime.beginRollbackRestore();
		setNextSpatialIdentityLoadPurpose(LoadPurpose.ROLLBACK_RESTORE);
		boolean restored = false;
		try {
			getXMLio().processXMLString(rollbackXml, true, false);
			restored = true;
		} catch (XMLParseException | RuntimeException failure) {
			throw new IllegalStateException(
					"Spatial redefine rollback could not restore the construction",
					failure);
		} finally {
			clearNextSpatialIdentityLoadPurpose();
			spatialSemanticRuntime.finishRollbackRestore(restored);
		}
	}

	private static void replaceSpatialIdentitySection(StringBuilder constructionXml,
			String oldSection, String newSection) {
		int sectionPosition = constructionXml.indexOf(oldSection);
		if (oldSection.isEmpty() || sectionPosition < 0) {
			throw new IllegalStateException(
					"Participating construction XML has no spatial identity section");
		}
		constructionXml.replace(sectionPosition,
				sectionPosition + oldSection.length(), newSection);
	}

	private void buildConstructionWithGlobalListeners(
			XMLStringBuilder consXML, String oldXML,
			EvalInfo info) throws XMLParseException {

		ScriptManager scriptManager = kernel.getApplication().getScriptManager();
		scriptManager.keepListenersOnReset();
		buildConstruction(consXML, oldXML, info);
		scriptManager.dropListenersOnReset();
	}

	/**
	 * @return true if is getting XML for replace
	 */
	public boolean isGettingXMLForReplace() {
		return isGettingXMLForReplace;
	}

	/**
	 * @return true if construction is removing an old geo to replace it (used
	 * to prevent closing of object properties when replacing a single
	 * geo)
	 */
	public boolean isRemovingGeoToReplaceIt() {
		return geoBeingRemovedForReplace != null;
	}

	/**
	 * @param geo candidate removal in the current replacement cascade
	 * @return whether identity retirement is suppressed for the exact target or
	 *         its provider-validated participating output group
	 */
	public boolean isRemovingGeoToReplaceIt(GeoElement geo) {
		return geoBeingRemovedForReplace == geo
				|| (spatialGeosBeingRemovedForReplace != null
						&& spatialGeosBeingRemovedForReplace.contains(geo));
	}

	/**
	 * Processes all collected redefine calls as a batch to improve performance.
	 * @throws XMLParseException if replacement produces invalid XML
	 * @see #startCollectingRedefineCalls()
	 */
	public void processCollectedRedefineCalls() throws XMLParseException {
		collectRedefineCalls = false;

		if (redefineMap == null || redefineMap.size() == 0) {
			stopCollectingRedefineCalls();
			return;
		}

		StringBuilder consXML = new StringBuilder();
		ArrayList<SpatialRedefineContext> publicationContexts = new ArrayList<>();
		if (spatialRedefineMap != null) {
			for (SpatialRedefineTransaction transaction : spatialRedefineMap.values()) {
				publicationContexts.add(transaction.getContext());
			}
		}
		RedefinePublicationLease publicationLease = null;
		boolean hostMutationStarted = false;
		try {
			if (!publicationContexts.isEmpty()) {
				publicationLease = spatialIdentityRegistry
						.beginRedefinePublicationLease(publicationContexts);
			}
			String currentSpatialSection = null;
			String retainedSpatialSection = null;
			// Validate every retained revision before label preparation or XML mutation.
			if (spatialRedefineMap != null && !spatialRedefineMap.isEmpty()) {
				currentSpatialSection = spatialIdentityRegistry.writeSpatialSection();
				retainedSpatialSection = spatialIdentityRegistry
						.writeSpatialSectionForRetainedRedefines(
								spatialRedefineMap.values());
			}
			hostMutationStarted = true;
			ArrayList<SpatialIdentityRegistry.SerializationOverlay> snapshotOverlays =
					new ArrayList<>();
			beginSpatialIdentityXML();
			try {
				for (Entry<GeoElement, GeoElement> entry : redefineMap.entrySet()) {
					SpatialRedefineTransaction transaction = spatialRedefineMap == null
							? null : spatialRedefineMap.get(entry.getKey());
					if (requiresDependencyPreservingSpatialRebuild(entry.getKey(),
							entry.getValue(), transaction)) {
						prepareDependencyPreservingSpatialGroupLabels(transaction,
								entry.getValue());
						snapshotOverlays.add(spatialIdentityRegistry
								.beginRedefineSerializationOverlay(transaction));
					}
					if (entry.getValue().isLabelSet()
							&& !entry.getKey().getLabelSimple().equals(
									entry.getValue().getLabelSimple())) {
						entry.getKey().setLabelSimple(
								entry.getValue().getLabelSimple());
					}
				}
				consXML.append(getCurrentUndoXML(false));
			} finally {
				for (int index = snapshotOverlays.size() - 1; index >= 0; index--) {
					snapshotOverlays.get(index).close();
				}
				endSpatialIdentityXML();
			}
			String oldXML = consXML.toString();
			// replace all oldGeo -> newGeo pairs in XML
			boolean canReplace = true;
			for (Entry<GeoElement, GeoElement> entry : redefineMap.entrySet()) {
				GeoElement oldGeo = entry.getKey();
				GeoElement newGeo = entry.getValue();
				SpatialRedefineTransaction transaction = spatialRedefineMap == null
						? null : spatialRedefineMap.get(oldGeo);
				beginSpatialIdentityXML();
				try (SpatialIdentityRegistry.SerializationOverlay ignored =
						transaction == null ? null
								: spatialIdentityRegistry.beginRedefineSerializationOverlay(
										transaction)) {
					canReplace = canReplace
							&& doReplaceInXML(consXML, oldGeo, newGeo, false);
				} finally {
					endSpatialIdentityXML();
				}
			}
			if (canReplace && retainedSpatialSection != null) {
				replaceSpatialIdentitySection(consXML, currentSpatialSection,
						retainedSpatialSection);
			}

			// 4) build new construction for all changes at once
			if (canReplace) {
				if (!publicationContexts.isEmpty()) {
					setNextSpatialIdentityRedefineRebuild(publicationContexts);
				}
				buildConstructionWithGlobalListeners(
						new XMLStringBuilder(consXML), oldXML, null);
			} else {
				throw new MyError(getApplication().getLocalization(),
						Errors.ReplaceFailed);
			}
			if (spatialRedefineMap != null) {
				for (SpatialRedefineTransaction transaction
						: spatialRedefineMap.values()) {
					GeoElement result = spatialIdentityRegistry.getGeo(
							transaction.getDecidedId());
					if (result == null) {
						throw new SpatialIdentityException(
								SpatialIdentityDiagnostic.forSubject(
										SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
										"Collected redefine result did not resolve by ID",
										transaction.getDecidedId()));
					}
					commitSpatialRedefine(transaction, result);
				}
				ArrayList<SpatialRedefineContext> completedContexts = new ArrayList<>();
				for (SpatialRedefineTransaction transaction
						: spatialRedefineMap.values()) {
					completedContexts.add(transaction.getContext());
				}
				if (publicationLease != null) {
					publicationLease.close();
				}
				spatialIdentityRegistry.completeRedefineHostOperations(
						completedContexts);
			}
		} catch (XMLParseException | RuntimeException | MyError failure) {
			if (spatialRedefineMap != null) {
				if (hostMutationStarted) {
					rollbackCollectedSpatialRedefines();
				} else {
					rollbackCollectedSpatialRedefines(null, null, false);
				}
			}
			throw failure;
		} finally {
			if (publicationLease != null) {
				publicationLease.close();
			}
			stopCollectingRedefineCalls();
			consXML.setLength(0);
		}
	}

	/**
	 * Changes the given casCell taking care of necessary redefinitions. This
	 * may change the logic of the construction and is a very powerful
	 * operation.
	 * @param casCell casCell to be changed
	 * @throws XMLParseException in case of malformed XML
	 */
	public void changeCasCell(GeoCasCell casCell, String oldXML) throws XMLParseException {
		GeoElement participatingTarget = spatialIdentityRegistry.isParticipating(casCell)
				? casCell : casCell.getTwinGeo();
		if (participatingTarget != null
				&& spatialIdentityRegistry.isParticipating(participatingTarget)) {
			throw new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
					"GeoCasCell redefine is unsupported for spatial participants",
					spatialIdentityRegistry.getPersistentGeoId(participatingTarget)));
		}
		setUpdateConstructionRunning(true);
		// move all predecessors of casCell to the left of casCell in
		// construction list
		updateConstructionOrder(casCell);

		// get current construction XML
		StringBuilder consXML = getCurrentUndoXML(false);

		// build new construction to make sure all ceIDs are correct after the
		// redefine
		buildConstruction(new XMLStringBuilder(consXML), oldXML, new EvalInfo(true, true, false));
		setUpdateConstructionRunning(false);
	}

	/**
	 * Replaces oldGeo by newGeo in consXML.
	 * @param consXML string builder
	 * @param oldGeo old element
	 * @param newGeo replacement
	 */
	protected boolean doReplaceInXML(StringBuilder consXML, GeoElement oldGeo,
			GeoElement newGeo) {
		return doReplaceInXML(consXML, oldGeo, newGeo, true);
	}

	private boolean doReplaceInXML(StringBuilder consXML, GeoElement oldGeo,
			GeoElement newGeo, boolean allowSnapshotRefresh) {
		String oldXML, newXML; // a = old string, b = new string

		AlgoElement oldGeoAlgo = oldGeo.getParentAlgorithm();
		AlgoElement newGeoAlgo = newGeo.getParentAlgorithm();
		GeoElementND[] newGeoInputs = null;

		// change kernel settings temporarily

		// change kernel settings temporarily
		// set label to get replaceable XML
		if (newGeo.isLabelSet()) { // newGeo already exists in construction
			// oldGeo is replaced by newGeo, so oldGeo get's newGeo's label
			if (!oldGeo.getLabelSimple().equals(newGeo.getLabelSimple())) {
				oldGeo.setLabelSimple(newGeo.getLabelSimple());

				if (allowSnapshotRefresh) {
					// reload consXML to get the new name in the description of
					// dependent elements. A collected redefine pre-aligns every
					// installed label before its one canonical snapshot instead.
					isGettingXMLForReplace = true;
					consXML.setLength(0);
					consXML.append(getCurrentUndoXML(false));
					isGettingXMLForReplace = false;
				}
			}

			oldXML = (oldGeoAlgo == null) ? oldGeo.getXML()
					: oldGeoAlgo.getXML();
			newXML = ""; // remove oldGeo from construction
		} else {
			// newGeo doesn't exist in construction, so we take oldGeo's label
			newGeo.setLabelSimple(oldGeo.getLabelSimple());
			newGeo.setLabelSet(true); // to get right XML output
			copyStyleForRedefine(oldGeo, newGeo);

			// NEAR-TO-RELATION for dependent new geo:
			// copy oldGeo's values to newGeo so that the
			// near-to-relationship can do its job if possible
			if (newGeoAlgo != null && newGeoAlgo.isNearToAlgorithm()) {
				try {
					newGeo.set(oldGeo);
				} catch (Exception e) {
					// do nothing
				}
			}

			isGettingXMLForReplace = true;
			oldXML = (oldGeoAlgo == null) ? oldGeo.getXML()
					: oldGeoAlgo.getXML();
			if (newGeoAlgo == null) {
				newXML = newGeo.getXML();
			} else {
				newXML = newGeoAlgo.getXML();
				// get new geo inputs to check if we have to put the newXML
				// further in consXML
				newGeoInputs = newGeoAlgo.getInputForUpdateSetPropagation();
			}
			isGettingXMLForReplace = false;
		}

		// restore old kernel settings

		// replace Strings: oldXML by newXML in consXML
		int pos = consXML.indexOf(oldXML);
		if (pos < 0) {
			Log.debug("replace failed: oldXML string not found:\n" + oldXML);
			return false;
		}

		// get inputs position in consXML: we want to put new geo after that
		int inputEndPos = -1;
		if (newGeoInputs != null && newGeoInputs.length > 0) {
			int labelPos = 0;
			for (int i = 0; i < newGeoInputs.length; i++) {
				String label = newGeoInputs[i].getLabelSimple();
				if (label != null) {
					int labelPos0 = consXML.indexOf("label=\"" + label + "\"");
					if (labelPos0 > labelPos) {
						labelPos = labelPos0;
						inputEndPos = consXML.indexOf("</element>", labelPos)
								+ 11;
					}
				}
			}
		}

		// replace oldXML by newXML in consXML
		if (pos >= inputEndPos) {
			// old pos is ok
			consXML.replace(pos, pos + oldXML.length(), newXML);
		} else {
			// we put new geo after its inputs
			consXML.insert(inputEndPos, newXML);
			consXML.replace(pos, pos + oldXML.length(), "");
		}
		return true;
	}

	private static void copyStyleForRedefine(GeoElement oldGeo,
			GeoElement newGeo) {
		newGeo.setAllVisualProperties(oldGeo, false);
		newGeo.setViewFlags(oldGeo.getViewSet());
		newGeo.setScripting(oldGeo);
		if (newGeo.getGeoClassType() != oldGeo.getGeoClassType()
				&& newGeo.isFunctionOrEquationFromUser()) {
			newGeo.setFixed(true);
		}
	}

	/**
	 * Sets construction step position. Objects 0 to step in the construction
	 * list will be visible in the views, objects step+1 to the end will be
	 * hidden.
	 * @param s : step number from range -1 ... steps()-1 where -1 shows an
	 * empty construction.
	 */
	public void setStep(int s) {
		// Log.debug("setStep"+step+" "+s);
		Log.debug(step + " to" + s);
		if (s == step || s < -1 || s >= ceList.size()) {
			return;
		}

		kernel.setAllowVisibilitySideEffects(false);

		boolean cpara = kernel
				.isNotifyConstructionProtocolViewAboutAddRemoveActive();
		kernel.setNotifyConstructionProtocolViewAboutAddRemoveActive(false);

		if (s < step) {
			Log.debug(step + " to" + s);
			// we must go from high to low there as otherwise the CAS cells
			// would
			// rearrange their numbers meanwhile
			for (int i = step; i >= s + 1; i--) {
				ceList.get(i).notifyRemove();
			}
		} else {
			for (int i = step + 1; i <= s; ++i) {
				ceList.get(i).notifyAdd();
			}
		}

		kernel.setNotifyConstructionProtocolViewAboutAddRemoveActive(cpara);

		step = s;

		kernel.setAllowVisibilitySideEffects(true);

		updateAllConstructionProtocolAlgorithms();
	}

	/**
	 * Returns current construction step position.
	 * @return current construction step position.
	 */
	public int getStep() {
		return step;
	}

	/*
	 * GeoElementTable Management
	 */

	/**
	 * Adds given GeoElement to a table where (label, object) pairs are stored.
	 * @param geo GeoElement to be added, must be labeled
	 * @see #removeLabel(GeoElement)
	 * @see #lookupLabel(String)
	 */
	public void putLabel(GeoElement geo) {
		if (suppressLabelCreation || geo.getLabelSimple() == null) {
			return;
		}

		geoTable.put(geo.getLabelSimple(), geo);
		addToGeoSets(geo);
	}

	/**
	 * Removes given GeoElement from a table where (label, object) pairs are
	 * stored.
	 * @param geo GeoElement to be removed
	 * @see #putLabel(GeoElement)
	 */
	public void removeLabel(GeoElement geo) {
		geoTable.remove(geo.getLabelSimple());
		removeFromGeoSets(geo);
	}

	private void addToGeoSets(GeoElement geo) {
		geoSetConsOrder.add(geo);
		geoSetWithCasCells.add(geo);
		geoSetLabelOrder.add(geo);

		if (getApplication().isWhiteboardActive()) {
			layerManager.addGeo(geo);
		}

		// get ordered type set
		GeoClass type = geo.getGeoClassType();
		TreeSet<GeoElement> typeSet = geoSetsTypeMap.get(type);
		if (typeSet == null) {
			typeSet = createTypeSet(type);
		}
		typeSet.add(geo);
	}

	/**
	 * Compares geos by labels (if set)
	 */
	protected static class LabelComparator implements Comparator<GeoElement> {
		@Override
		public int compare(GeoElement ob1, GeoElement ob2) {
			GeoElement geo1 = ob1;
			GeoElement geo2 = ob2;

			return GeoElement.compareLabels(geo1.getLabelSimple(),
					geo2.getLabelSimple());
		}
	}

	/**
	 * Returns a set with all labeled GeoElement objects of a specific type in
	 * alphabetical order of their labels.
	 * @param geoClassType use {@link GeoClass} constants
	 * @return Set of elements of given type.
	 */
	final public TreeSet<GeoElement> getGeoSetLabelOrder(
			GeoClass geoClassType) {
		TreeSet<GeoElement> typeSet = geoSetsTypeMap.get(geoClassType);
		if (typeSet == null) {
			typeSet = createTypeSet(geoClassType);
		}
		return typeSet;
	}

	private TreeSet<GeoElement> createTypeSet(GeoClass type) {
		TreeSet<GeoElement> typeSet = new TreeSet<>(
				new LabelComparator());
		geoSetsTypeMap.put(type, typeSet);
		return typeSet;
	}

	private void removeFromGeoSets(GeoElement geo) {
		geoSetConsOrder.remove(geo);
		geoSetWithCasCells.remove(geo);
		geoSetLabelOrder.remove(geo);

		if (getApplication().isWhiteboardActive()) {
			layerManager.removeGeo(geo);
		}

		// set ordered type set
		GeoClass type = geo.getGeoClassType();
		TreeSet<GeoElement> typeSet = geoSetsTypeMap.get(type);
		if (typeSet != null) {
			typeSet.remove(geo);
		}
	}

	/**
	 * Adds given GeoCasCell to a table where (label, object) pairs of CAS view
	 * variables are stored.
	 * @param geoCasCell GeoElement to be added, must have assignment variable
	 * @param label label for CAS cell
	 * @see #removeCasCellLabel(String)
	 * @see #lookupCasCellLabel(String)
	 */
	public void putCasCellLabel(GeoCasCell geoCasCell, String label) {
		if (label == null) {
			return;
		}

		if (geoCasCellTable == null) {
			geoCasCellTable = new HashMap<>();
		}
		geoCasCellTable.put(label, geoCasCell);
	}

	/**
	 * Removes given GeoCasCell from the CAS variable table and from the
	 * underlying CAS.
	 * @param variable to be removed
	 * @see #putCasCellLabel(GeoCasCell, String)
	 */

	public void removeCasCellLabel(String variable) {
		if (geoCasCellTable != null) {
			geoCasCellTable.remove(variable);
		}
	}

	/**
	 * Returns a GeoElement for the given label. Note: only geos with
	 * construction index 0 to step are available.
	 * @param label label to be looked for
	 * @return may return null
	 */
	public GeoElement lookupLabel(String label) {
		return lookupLabel(label, false);
	}

	/**
	 * Returns a GeoCasCell for the given label. Note: only objects with
	 * construction index 0 to step are available.
	 * @param label to be looked for
	 * @return may return null
	 */
	public GeoCasCell lookupCasCellLabel(String label) {
		GeoCasCell geoCasCell = null;

		// global var handling
		if (geoCasCellTable != null) {
			geoCasCell = geoCasCellTable.get(label);
		}

		// TODO add lookupCasCellLabel support for construction steps
		// // STANDARD CASE: variable name found
		// if (geoCasCell != null) {
		// return (GeoCasCell) checkConstructionStep(geoCasCell);
		// }

		return geoCasCell;
	}

	/**
	 * Returns GeoCasCell referenced by given row label.
	 * @param label row reference label, e.g. $5 for 5th row or $ for previous row
	 * @return referenced row or null
	 * @throws CASException thrown if one or more row references are invalid (like $x or
	 * if the number is higher than the number of rows)
	 */
	public GeoCasCell lookupCasRowReference(String label) throws CASException {
		if (!label
				.startsWith(ExpressionNodeConstants.CAS_ROW_REFERENCE_PREFIX)) {
			return null;
		}

		// $5 for 5th row
		int rowRef;
		try {
			rowRef = Integer.parseInt(label.substring(1));
		} catch (NumberFormatException e) {
			Log.error("Malformed CAS row reference: " + label);
			CASException ex = new CASException("CAS.InvalidReferenceError", e);
			ex.setKey("CAS.InvalidReferenceError");
			throw ex;
		}

		// we start to count at 0 internally but at 1 in the user interface
		GeoCasCell ret = getCasCell(rowRef - 1);
		if (ret == null) {
			Log.error("invalid CAS row reference: " + label);
			CASException ex = new CASException("CAS.InvalidReferenceError");
			ex.setKey("CAS.InvalidReferenceError");
			throw ex;
		}
		return ret;
	}

	/**
	 * Returns a GeoElement for the given label. Note: only geos with
	 * construction index 0 to step are available.
	 * @param label to be looked for
	 * @param allowAutoCreate : true = allow automatic creation of missing labels (e.g. for
	 * spreadsheet)
	 * @return may return null
	 */
	protected GeoElement lookupLabel(String label, boolean allowAutoCreate) {
		String label1 = label;
		if (label1 == null || label1.isEmpty()) {
			return null;
		}

		// local var handling
		if (localVariableTable != null) {
			GeoElement localGeo = localVariableTable.get(label1);
			if (localGeo != null) {
				return localGeo;
			}
		}

		// global var handling
		GeoElement geo = geoTableVarLookup(label1);

		// STANDARD CASE: variable name found
		if (geo != null) {
			return checkConstructionStep(geo);
		}

		// DESPERATE CASE: variable name not found

		/*
		 * CAS VARIABLE HANDLING e.g. ggbtmpvara for a
		 */
		label1 = Kernel.removeCASVariablePrefix(label1);
		if (label1 == null || label1.isEmpty()) {
			return null;
		}
		geo = geoTableVarLookup(label1);
		if (geo != null) {
			// geo found for name that starts with TMP_VARIABLE_PREFIX or
			// GGBCAS_VARIABLE_PREFIX
			return checkConstructionStep(geo);
		}

		/*
		 * SPREADSHEET $ HANDLING In the spreadsheet we may have variable names
		 * like "A$1" for the "A1" to deal with absolute references. Let's
		 * remove all "$" signs from label and try again.
		 */
		if (label1.indexOf('$') > -1) {
			StringBuilder labelWithoutDollar = new StringBuilder(
					label1.length() - 1);
			for (int i = 0; i < label1.length(); i++) {
				char ch = label1.charAt(i);
				if (ch != '$') {
					labelWithoutDollar.append(ch);
				}
			}
			String labelString = labelWithoutDollar.toString();
			if (labelString.isEmpty()) {
				return null;
			}
			// allow automatic creation of elements
			geo = lookupLabel(labelString, allowAutoCreate);
			if (geo != null) {
				// geo found for name that includes $ signs
				return checkConstructionStep(geo);
			}
			if (labelString.charAt(0) >= '0' && labelString.charAt(0) <= '9') {
				int cell = 0;
				try {
					cell = Integer.parseInt(labelString);
				} catch (Exception e) {
					Log.debug(e.getMessage());
				}
				if (cell > 0) {
					return this.getCasCell(cell - 1);
				}
			}
		}
		if ("self".equals(label1)) {
			return this.selfGeoStack.peek();
		}
		if ("undefined".equals(label1)) {
			GeoNumeric n = new GeoNumeric(this);
			n.setUndefined();
			return n;
		}
		if (!fileLoading) {
			geo = geoTableVarLookup(label1);
			if (geo != null) {
				return checkConstructionStep(geo);
			}

			List<String> variants = StringUtil.labelVariants(label1);
			if (variants != null) {
				for (String variant : variants) {
					geo = geoTableVarLookup(variant);
					if (geo != null) {
						return checkConstructionStep(geo);
					}
				}
			}
		}
		// try upper case version for spreadsheet label like a1
		if (allowAutoCreate) {
			// starts with letter & ends with digit
			if (Character.isLetter(label1.charAt(0))
					&& StringUtil.isDigit(label1.charAt(label1.length() - 1))) {
				String upperCaseLabel = label1.toUpperCase(Locale.ROOT);
				geo = geoTableVarLookup(upperCaseLabel);
				if (geo != null) {
					return checkConstructionStep(geo);
				}
			}
		}

		// look in CAS table too for label
		// needed for TRAC-2719; causes GGB-100
		// geo = lookupCasCellLabel(label1);
		// if (geo != null) {
		// return geo;
		// }

		// if we get here, nothing worked:
		// possibly auto-create new GeoElement with that name
		if (allowAutoCreate
				&& getApplication().getKernel()
				.getAlgebraProcessor().enableStructures()) {
			return autoCreateGeoElement(label1);
		}
		return null;
	}

	/**
	 * Search for constant with given label
	 * @param label - label of constant
	 * @return constant(GeoNumeric) from arbitraryConsTable with label
	 */
	public GeoNumeric lookupConstantLabel(String label) {
		if (!getArbitraryConsTable().isEmpty()) {
			for (ArbitraryConstantRegistry arbConst : getArbitraryConsTable()
					.values()) {
				ArrayList<GeoNumeric> constList = arbConst.getConstList();
				if (constList != null && !constList.isEmpty()) {
					for (GeoNumeric constant : constList) {
						if (constant.getLabelSimple().equals(label)) {
							return constant;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns geo if it is available at the current construction step,
	 * otherwise returns null.
	 */
	private GeoElement checkConstructionStep(GeoElement geo) {
		// check if geo is available for current step
		if (geo.isAvailableAtConstructionStep(step)) {
			return geo;
		}
		return null;
	}

	/**
	 * Returns true if label is not occupied by any GeoElement including
	 * GeoCasCells.
	 * @param label label to be checked
	 * @return true iff label is not occupied by any GeoElement.
	 */
	public boolean isFreeLabel(String label) {
		return isFreeLabel(label, true, false);
	}

	/**
	 * Returns true if label is not occupied by any GeoElement.
	 * @param label label to be checked
	 * @param includeCASVariables whether GeoCasCell labels should be checked too
	 * @param checkProtectedLabels when true, this method also checks that label is not protected
	 * (used by CAS dummy, or by merged construction)
	 * @return true iff label is not occupied by any GeoElement.
	 */
	public boolean isFreeLabel(@CheckForNull String label, boolean includeCASVariables,
			boolean checkProtectedLabels) {
		if (label == null) {
			return false;
		}
		if (!fileLoading && getKernel().getApplication().getParserFunctions()
				.isReserved(label)) {
			return false;
		}

		if (fileLoading && casCellUpdate) {
			GeoNumeric geoNum = lookupConstantLabel(label);
			if (geoNum != null) {
				return false;
			}
		}

		// GGB-843
		if (fileLoading && !casCellUpdate && isNotXmlLoading()) {
			GeoNumeric geoNum = lookupConstantLabel(label);
			if (geoNum != null) {
				return false;
			}
		}

		// check standard geoTable
		if (geoTable.containsKey(label)) {
			return false;
		}

		// optional: also check CAS variable table
		if (includeCASVariables && geoCasCellTable != null
				&& geoCasCellTable.containsKey(label)) {
			return false;
		}
		return !checkProtectedLabels || !protectedLabels.contains(label);
	}

	/**
	 * Moves all predecessors of newGeo (i.e. all objects that newGeo depends
	 * upon) to the left of oldGeo in the construction list.
	 * @return true if construction order has changed
	 */
	private boolean updateConstructionOrder(GeoElement oldGeo, GeoElement newGeo) {
		TreeSet<GeoElement> predSet = newGeo.getAllPredecessors();

		// check if moving is needed
		// find max construction index of newGeo's predecessors and newGeo
		// itself
		int maxPredIndex = newGeo.getConstructionIndex();
		for (GeoElement pred : predSet) {
			int predIndex = pred.getConstructionIndex();
			if (predIndex > maxPredIndex) {
				maxPredIndex = predIndex;
			}
		}

		// no reordering is needed
		if (oldGeo.getConstructionIndex() > maxPredIndex) {
			return false;
		}

		// reordering is needed

		// move oldGeo to its maximum construction index
		boolean changed = moveInConstructionList(oldGeo,
				Math.min(oldGeo.getMaxConstructionIndex(), maxPredIndex));
		// move all predecessors of newGeo (i.e. all objects that geo depends
		// upon) as far as possible to the left in the construction list
		for (GeoElement pred : predSet) {
			if (pred.getConstructionIndex() >= oldGeo.getConstructionIndex()) {
				changed |= moveInConstructionList(pred, Math.max(pred.getMinConstructionIndex(),
						oldGeo.getConstructionIndex()));
			}
		}

		// move newGeo to the left as well (important if newGeo already existed
		// in construction)
		changed |= moveInConstructionList(newGeo, newGeo.getMinConstructionIndex());

		return changed;
	}

	/**
	 * Makes sure that geoCasCell comes after all its predecessors in the
	 * construction list.
	 * @param casCell CAS cell
	 * @return whether construction list order was changed
	 */
	protected boolean updateConstructionOrder(GeoCasCell casCell) {
		// collect all predecessors of casCell
		TreeSet<GeoElement> allPred = new TreeSet<>();
		if (casCell.getGeoElementVariables() != null) {
			for (GeoElement directInput : casCell.getGeoElementVariables()) {
				allPred.addAll(directInput.getAllPredecessors());
				allPred.add(directInput);
			}
		}

		if (allPred.size() == 0) { // there are no predecessors
			return false; // nothing changed
		}

		// Find max construction index of casCell's predecessors
		int maxPredIndex = 0;
		for (GeoElement pred : allPred) {
			int predIndex = pred.getConstructionIndex();
			if (predIndex > maxPredIndex) {
				maxPredIndex = predIndex;
			}
		}

		// if casCell comes after all its new predecessors,
		// no reordering is needed
		if (casCell.getConstructionIndex() > maxPredIndex) {
			return false;
		}

		// reordering is needed
		// maybe we can move casCell down in the construction list
		int maxCellIndex = casCell.getMaxConstructionIndex();
		if (maxCellIndex >= maxPredIndex) {
			moveInConstructionList(casCell,
					maxPredIndex + (maxCellIndex > maxPredIndex ? 1 : 0));
			return true;
		}

		// reordering is needed but we cannot simply move down the casCell
		// because it has dependent objects:
		// move all predecessors of casCell up as far as possible
		maxPredIndex = 0;
		for (GeoElement pred : allPred) {
			moveInConstructionList(pred, pred.getMinConstructionIndex());
			maxPredIndex = Math.max(maxPredIndex, pred.getConstructionIndex());
		}

		// if casCell still comes before one of its predecessors
		// we have to move casCell
		if (casCell.getConstructionIndex() < maxPredIndex) {
			return true;
		}

		// maybe we can move casCell down in the construction list now
		if (casCell.getMaxConstructionIndex() > maxPredIndex) {
			moveInConstructionList(casCell, maxPredIndex + 1);
			return true;
		}
		return false;
	}

	// 1) remove all brothers and sisters of oldGeo
	// 2) move all predecessors of newGeo to the left of oldGeo in construction
	// list

	/**
	 * @param oldGeo old element
	 * @param newGeo replacement
	 */
	protected void prepareReplace(GeoElement oldGeo, GeoElement newGeo) {
		AlgoElement oldGeoAlgo = oldGeo.getParentAlgorithm();
		AlgoElement newGeoAlgo = newGeo.getParentAlgorithm();

		// 1) remove all brothers and sisters of oldGeo
		if (oldGeoAlgo != null) {
			keepGeo = oldGeo;
			oldGeoAlgo.removeOutputExcept(oldGeo);
			keepGeo = null;
		}

		// if newGeo is not in construction index, we must set its index now
		// in order to let (2) and (3) work
		if (newGeo.getConstructionIndex() == -1) {
			int ind = ceList.size();
			if (newGeoAlgo == null) {
				newGeo.setConstructionIndex(ind);
			} else {
				newGeoAlgo.setConstructionIndex(ind);
			}
		}

		// make sure all output objects of newGeoAlgo are labeled, otherwise
		// we may end up with several objects that have the same label
		if (newGeoAlgo != null) {
			for (int i = 0; i < newGeoAlgo.getOutputLength(); i++) {
				GeoElement geo = newGeoAlgo.getOutput(i);
				if (geo != newGeo && geo.isDefined() && !geo.isLabelSet()) {
					geo.setLabel(null); // get free label
				}
			}
		}

		// 2) move all predecessors of newGeo to the left of oldGeo in
		// construction list
		updateConstructionOrder(oldGeo, newGeo);
	}

	/**
	 * Adds the given GeoCasCell to a set with all labeled GeoElements and CAS
	 * cells needed for notifyAll().
	 * @param geoCasCell CAS cell to be added
	 */
	public void addToGeoSetWithCasCells(GeoCasCell geoCasCell) {
		geoSetWithCasCells.add(geoCasCell);
	}

	/**
	 * Removes the given GeoCasCell from a set with all labeled GeoElements and
	 * CAS cells needed for notifyAll().
	 * @param geoCasCell CAS cell to be removed
	 */
	public void removeFromGeoSetWithCasCells(GeoCasCell geoCasCell) {
		geoSetWithCasCells.remove(geoCasCell);
	}

	/**
	 * Creates a new GeoElement for the spreadsheet of same type as
	 * neighbourCell.
	 * @param neighbourCell another geo of the desired type
	 * @param label Label for the new geo
	 * @return new GeoElement of desired type
	 */
	final public GeoElement createSpreadsheetGeoElement(
			GeoElement neighbourCell, String label) {
		GeoElement result;

		// found neighbouring cell: create geo of same type
		if (neighbourCell != null) {
			result = neighbourCell.copy();
			result.setZero();
		}
		// no neighbouring cell: create number with value 0
		else {
			result = new GeoNumeric(this);
		}

		// set result as empty cell geo
		if (!kernel.getApplication().isUnbundled()) {
			result.setUndefined();
		}
		result.setEmptySpreadsheetCell(true);

		// make sure that label creation is turned on
		boolean oldSuppressLabelsActive = isSuppressLabelsActive();
		setSuppressLabelCreation(false);

		// set auxiliary and label
		result.setAuxiliaryObject(true);
		result.setLabel(label);

		// revert to previous label creation state
		setSuppressLabelCreation(oldSuppressLabelsActive);

		return result;
	}

	/**
	 * Returns the next free indexed label using the given prefix.
	 * @param prefix e.g. "c"
	 * @return indexed label, e.g. "c_2"
	 */
	public String getIndexLabel(String prefix) {
		return getIndexLabel(prefix, false);
	}

	/**
	 * Returns the next free indexed label using the given prefix.
	 * @param prefix e.g. "c"
	 * @param includeDummies to include cas dummy variables
	 * @return indexed label, e.g. "c_{2}"
	 */
	public String getIndexLabel(String prefix, boolean includeDummies) {
		// start numbering with indices using suggestedLabel
		// as prefix
		String pref;
		int pos = prefix.indexOf('_');
		if (pos == -1) {
			pref = prefix;
		} else {
			pref = prefix.substring(0, pos);
		}
		// TRAC-3519 avoid invalid labels like "Vertex(poly1')_1"
		if (!LabelManager.isValidLabel(pref, kernel, null)
				&& !pref.equals(LabelManager.HIDDEN_PREFIX)) {
			pref = "a";
		}

		return buildIndexedLabel(pref, includeDummies);
	}

	/**
	 * Please note that we do not check here for valid label
	 * (see {@link #getIndexLabel(String, boolean)})
	 * @param pref - prefix
	 * @param includeDummies - to include cas dummy variables
	 * @return indexed label, e.g. "y_{2}"
	 */
	public String buildIndexedLabel(String pref, boolean includeDummies) {
		String longIndexLabel;
		boolean freeLabelFound;
		int n = 0;

		do {
			n++;

			longIndexLabel = pref + "_{" + n + '}';
			String indexLabel = pref + '_' + n;
			freeLabelFound = isFreeLabel(longIndexLabel, true, includeDummies)
					&& ((n >= 10) || isFreeLabel(indexLabel, true, includeDummies));
		} while (!freeLabelFound);

		return longIndexLabel;
	}

	/**
	 * Automatically creates a GeoElement object for a certain label that is not
	 * yet used in the geoTable of this construction. This is done for e.g.
	 * point i = (0,1), number e = Math.E, empty spreadsheet cells
	 * @param labelNew label for new element, may not be null
	 * @return created element
	 */
	protected GeoElement autoCreateGeoElement(String labelNew) {
		GeoElementND createdGeo = null;
		boolean fix = true;
		boolean auxiliary = true;
		String label = labelNew;
		int length = label.length();
		// expression like AB, autocreate AB=Distance[A,B] or AB = A * B
		// according to whether A,B are points or numbers
		if (length == 3 && label.charAt(2) == '\'') {
			createdGeo = distance(label.charAt(0) + "",
					label.charAt(1) + "'");
			fix = false;

		} else if (length == 3 && label.charAt(1) == '\'') {
			createdGeo = distance(label.charAt(0) + "'",
					label.charAt(2) + "");
			fix = false;

		} else if (length == 4 && label.charAt(1) == '\''
				&& label.charAt(3) == '\'') {
			createdGeo = distance(label.charAt(0) + "'",
					label.charAt(2) + "'");
			fix = false;

		} else if (length == 2) {
			createdGeo = distance(label.charAt(0) + "",
					label.charAt(1) + "");
			fix = false;

		} else if (length == 1) {
			if ("O".equals(label)) {

				createdGeo = new GeoPoint(this, 0d, 0d, 1d);
				label = "O";
				auxiliary = true;
				fix = true;
			}
		}

		// handle i or e case
		if (createdGeo != null) {

			// removed: not needed for e,i and causes bug with using Circle[D,
			// CD 2] in locus
			// boolean oldSuppressLabelsActive = isSuppressLabelsActive();
			// setSuppressLabelCreation(false);

			createdGeo.setAuxiliaryObject(auxiliary);
			createdGeo.setLabel(label);
			createdGeo.setFixed(fix);

			// revert to previous label creation state
			// setSuppressLabelCreation(oldSuppressLabelsActive);
			return createdGeo.toGeoElement();
		}

		// check spreadsheet cells
		// for missing spreadsheet cells, create object
		// of same type as above
		createdGeo = GeoElementSpreadsheet.autoCreate(label, this);
		if (createdGeo == null) {
			return null;
		}
		return createdGeo.toGeoElement();
	}

	private GeoNumberValue distance(String string, String string2) {
		GeoElement geo1 = kernel.lookupLabel(string);
		if (geo1 != null && geo1.isGeoPoint()) {
			GeoElement geo2 = kernel.lookupLabel(string2);
			if (geo2 != null && geo2.isGeoPoint()) {
				AlgoDistancePoints dist = new AlgoDistancePoints(this,
						(GeoPointND) geo1, (GeoPointND) geo2);
				return dist.getDistance();

			}
		}
		return null;
	}

	/**
	 * Make geoTable contain only xAxis and yAxis
	 */
	private void initGeoTables() {
		geoTable.clear();
		geoCasCellTable = null;
		localVariableTable = null;
		arbitraryConstantsMap.clear();
		unclaimedArbitraryConstants.clear();
		arbitraryComplexNumbersMap.clear();
		arbitraryIntegersMap.clear();
		// add axes labels both in English and current language
		geoTable.put("xAxis", xAxis);
		geoTable.put("yAxis", yAxis);
		usedGeos.clear();
		if (xAxisLocalName != null) {
			geoTable.put(xAxisLocalName, xAxis);
			geoTable.put(yAxisLocalName, yAxis);
		}

		companion.initGeoTables();
	}

	/**
	 * @param b flag to ignore new types (for creating default geos)
	 */
	public void setIgnoringNewTypes(boolean b) {
		this.ignoringNewTypes = b;
	}

	/**
	 * Suppresses used-GeoClass registration for speculative internal objects.
	 * Scopes are nesting-safe and do not change the legacy boolean switch.
	 *
	 * @return lexical suppression scope
	 */
	public NewTypeRegistrationScope suppressNewTypeRegistration() {
		newTypeRegistrationSuppressionDepth++;
		return new NewTypeRegistrationScope();
	}

	/** Lexical, idempotently closed used-type registration suppression. */
	public final class NewTypeRegistrationScope implements AutoCloseable {
		private boolean closed;

		private NewTypeRegistrationScope() {
			// Created only by suppressNewTypeRegistration().
		}

		@Override
		public void close() {
			if (!closed) {
				newTypeRegistrationSuppressionDepth--;
				closed = true;
			}
		}
	}

	/**
	 * @param c used class of element (needed to decide about 2D
	 * compatibility)
	 */
	public void addUsedType(GeoClass c) {
		if (this.ignoringNewTypes || newTypeRegistrationSuppressionDepth > 0) {
			return;
		}
		this.usedGeos.add(c);
	}

	/**
	 * @return whether there are some objects incompatible with the 2D version
	 */
	private boolean has3DObjects() {

		Iterator<GeoClass> it = usedGeos.iterator();

		boolean kernelHas3DObjects = false;

		while (it.hasNext()) {
			GeoClass geoType = it.next();

			if (geoType.is3D) {
				Log.debug("found 3D geo: " + geoType.xmlName);
				kernelHas3DObjects = true;
				break;
			}
		}

		return kernelHas3DObjects;
	}

	/**
	 * @param filter what kind of inputboxes to look for
	 * @return whether construction contains any inputboxes matching the filter
	 */
	public boolean hasInputBoxes(Predicate<GeoInputBox> filter) {
		return usedGeos.contains(GeoClass.TEXTFIELD) && geoSetLabelOrder.stream()
				.anyMatch(geo -> geo instanceof GeoInputBox
						&& filter.test((GeoInputBox) geo));
	}

	/**
	 * @return Whether some objects were created in this cons
	 */
	public boolean isStarted() {
		return usedGeos.size() > 0 || kernel.getMacroNumber() > 0;
	}

	/**
	 * Returns a set with all labeled GeoElement objects sorted in alphabetical
	 * order of their type strings and labels (e.g. Line g, Line h, Point A,
	 * Point B, ...). Note: the returned TreeSet is a copy of the current
	 * situation and is not updated by the construction later on.
	 * @return Set of all labeled GeoElements sorted by name and description
	 */
	final public TreeSet<GeoElement> getGeoSetNameDescriptionOrder() {
		// sorted set of geos
		TreeSet<GeoElement> sortedSet = new TreeSet<>(
				new NameDescriptionComparator());

		// get all GeoElements from construction and sort them
		Iterator<GeoElement> it = geoSetConsOrder.iterator();
		while (it.hasNext()) {
			GeoElement geo = it.next();
			// sorted inserting using name description of geo
			sortedSet.add(geo);
		}
		return sortedSet;
	}

	/**
	 * Returns extremum finder
	 * @return extremum finder
	 */
	public ExtremumFinderI getExtremumFinder() {
		return kernel.getExtremumFinder();
	}

	/*
	 * redo / undo
	 */

	/**
	 * Stores current state of construction.
	 * @see UndoManager#storeUndoInfo()
	 */
	public void storeUndoInfo() {
		getUndoManager().storeUndoInfo();
	}

	/**
	 * Returns true iff undo is possible
	 * @return true iff undo is possible
	 */
	public boolean undoPossible() {
		// undo unavailable in applets
		// if (getApplication().isApplet()) return false;

		return undoManager != null && undoManager.undoPossible();
	}

	/**
	 * Returns true iff redo is possible
	 * @return true iff redo is possible
	 */
	public boolean redoPossible() {
		// undo unavailable in applets
		// if (getApplication().isApplet()) return false;

		return undoManager != null && undoManager.redoPossible();
	}

	/**
	 * Add a macro to list of used macros
	 * @param macro Macro to be added
	 */
	public final void addUsedMacro(Macro macro) {
		if (usedMacros == null) {
			usedMacros = new ArrayList<>();
		}
		usedMacros.add(macro);
	}

	/**
	 * Returns list of macros used in this construction
	 * @return list of macros used in this construction
	 */
	public ArrayList<Macro> getUsedMacros() {
		return usedMacros;
	}

	/**
	 * Calls remove() for every ConstructionElement in the construction list.
	 * After this the construction list will be empty.
	 */
	public void clearConstruction() {
		if (nextSpatialIdentityLoadPurpose == LoadPurpose.REDEFINE_REBUILD) {
			spatialIdentityRegistry.clearForRedefineRebuild(
					nextSpatialIdentityRedefineRebuildToken);
		} else if (nextSpatialIdentityLoadPurpose == LoadPurpose.ROLLBACK_RESTORE) {
			spatialSemanticRuntime.clearForRollbackRestore();
		} else {
			spatialSemanticRuntime.clear();
		}
		if (nextSpatialIdentityLoadPurpose == LoadPurpose.ROLLBACK_RESTORE) {
			spatialIdentityRegistry.clearPreservingRetiredTokens();
		} else if (nextSpatialIdentityLoadPurpose != LoadPurpose.REDEFINE_REBUILD) {
			spatialIdentityRegistry.clear();
		}
		arbitraryConstantsMap.clear();
		unclaimedArbitraryConstants.clear();
		arbitraryComplexNumbersMap.clear();
		arbitraryIntegersMap.clear();
		ceList.clear();
		algoList.clear();

		geoSetConsOrder.clear();
		geoSetWithCasCells.clear();
		geoSetLabelOrder.clear();

		layerManager.clear();

		geoSetsTypeMap.clear();
		euclidianViewCE.clear();

		this.corner5Algos = null;
		this.corner11Algos = null;
		this.protectedLabels.clear();
		initGeoTables();

		// reinit construction step
		step = -1;

		// delete title, author, date
		title = null;
		author = null;
		date = null;
		worksheetText[0] = null;
		worksheetText[1] = null;

		usedMacros = null;
		spreadsheetTraces = false;
		suppressLabelCreation = false;
		groups.clear();
	}

	/**
	 * Returns undo xml string of this construction.
	 * @param getListenersToo whether to include JS listeners
	 * @return StringBuilder with xml of this construction.
	 */
	public StringBuilder getCurrentUndoXML(boolean getListenersToo) {
		return MyXMLio.getUndoXML(this, getListenersToo);
	}

	/**
	 * Each construction has its own IO because of strong coupling between
	 * those.
	 * @return MyXMLio for this construction
	 */
	public MyXMLio getXMLio() {
		if (xmlio == null) {
			xmlio = kernel.getApplication().createXMLio(this);
		}
		return xmlio;
	}

	/**
	 * Clears the undo info list of this construction and adds the current
	 * construction state to the undo info list.
	 */
	public void initUndoInfo() {
		getUndoManager().initUndoInfo();
	}

	/**
	 * Tries to build the new construction from the given XML string.
	 */
	private void buildConstruction(XMLStringBuilder consXML, String oldXML,
			EvalInfo info) throws XMLParseException {
		boolean spatialRedefineRebuild =
				nextSpatialIdentityLoadPurpose == LoadPurpose.REDEFINE_REBUILD;
		// try to process the new construction
		try {
			getXMLio().setErrorHandler(ErrorHelper.silent());
			processXML(consXML.toString(), false, info);
			kernel.notifyReset();
			// Update construction is done during parsing XML
			// kernel.updateConstruction();
		} catch (Exception | MyError e) {
			if (!spatialRedefineRebuild) {
				restoreAfterRedefine(oldXML, info);
			}
			throw e;
		} finally {
			getXMLio().setErrorHandler(getApplication().getDefaultErrorHandler());
		}
		if (kernel.getConstruction().getXMLio().hasErrors()) {
			if (!spatialRedefineRebuild) {
				restoreAfterRedefine(oldXML, info);
			}
			throw new MyError(getApplication().getLocalization(),
					Errors.ReplaceFailed);
		}
	}

	private void restoreAfterRedefine(String oldXML, EvalInfo info) throws XMLParseException {
		if (oldXML != null) {
			buildConstruction(new XMLStringBuilder(new StringBuilder(oldXML)), null, info);
		}
	}

	/**
	 * process xml to create construction
	 * @param xml XML builder
	 */
	public void processXML(StringBuilder xml) {
		try {
			processXML(xml.toString(), false, null);
		} catch (Exception e) {
			Log.debug(e);
		}
	}

	/**
	 * Processes XML
	 * @param strXML XML string
	 * @param isGGTOrDefaults whether to treat the XML as defaults
	 * @param info EvalInfo (can be null)
	 * @throws XMLParseException when XML is not valid
	 */
	final public synchronized void processXML(String strXML,
			boolean isGGTOrDefaults, EvalInfo info) throws XMLParseException {

		boolean randomize = info != null && info.updateRandom();
		boolean previousFileLoading = isFileLoading();
		boolean previousCasCellUpdate = isCasCellUpdate();
		setFileLoading(true);
		setCasCellUpdate(true);
		try {
			getXMLio().processXMLString(strXML, true, isGGTOrDefaults,
					true, randomize);
		} finally {
			setFileLoading(previousFileLoading);
			setCasCellUpdate(previousCasCellUpdate);
		}
	}

	/**
	 * Returns the UndoManager (for Copy &amp; Paste)
	 * @return UndoManager
	 */
	public UndoManager getUndoManager() {
		ensureUndoManagerExists();
		return undoManager;
	}

	private void ensureUndoManagerExists() {
		if (undoManager == null) {
			undoManager = kernel.getApplication().getUndoManager(this);
		}
	}

	/**
	 * used by commands Element[] and Cell[] as they need to know their output
	 * type in advance
	 * @param type type generated by getXMLTypeString()
	 */
	public void setOutputGeo(String type) {
		if (type == null) {
			this.outputGeo = null;
			return;
		}
		this.outputGeo = kernel.createGeoElement(this, type);
	}

	/**
	 * used by commands Element[] and Cell[] as they need to know their output
	 * type in advance default: return new GeoNumeric(this)
	 * @return output of command currently parsed from XML
	 */
	public GeoElement getOutputGeo() {
		return outputGeo == null ? new GeoNumeric(this) : outputGeo;
	}

	/**
	 * Registers function variable that should be recognized in If and Function
	 * commands
	 * @param fv local function variable
	 */
	public void registerFunctionVariable(String fv) {
		if (fv == null) {
			registeredFV.clear();
		} else if (!registeredFV.contains(fv)) {
			registeredFV.add(fv);
		}
	}

	/**
	 * @return whether any function variables are registered
	 */
	public boolean hasRegisteredFunctionVariable() {
		return !registeredFV.isEmpty();
	}

	/**
	 * @param s variable name
	 * @return whether s is among registered function variables
	 */
	public boolean isRegisteredFunctionVariable(String s) {
		return registeredFV.contains(s);
	}

	/**
	 * Let construction know about file being loaded. When this is true, user
	 * defined objects called sin, cos, ... are accepted
	 * @param b true if file is loading
	 */
	public void setFileLoading(boolean b) {
		fileLoading = b;
	}

	/**
	 * @return whether we are just loading a file
	 */
	public boolean isFileLoading() {
		return fileLoading;
	}

	/**
	 * @param b true if cas cell is updated
	 */
	public void setCasCellUpdate(boolean b) {
		casCellUpdate = b;
	}

	/**
	 * @return whether we have cas cell update
	 */
	public boolean isCasCellUpdate() {
		return casCellUpdate;
	}

	/**
	 * @return whether we need to create a new arbitrary constant and it's not
	 * read from xml
	 */
	public boolean isNotXmlLoading() {
		return notXmlLoading;
	}

	/**
	 * it is called0 in MyArbitraryConstant
	 * @param b - false if constant is created by xml reading, true if
	 * constant is created by MyArbitraryConstant
	 */
	public void setNotXmlLoading(boolean b) {
		this.notXmlLoading = b;
	}

	/**
	 * @return whether updateConstruction is running
	 */
	public boolean isUpdateConstructionRunning() {
		return updateConstructionRunning;
	}

	/**
	 * Make sure a label won't be used by automatic labeling.
	 */
	public void addProtectedLabel(String label) {
		protectedLabels.add(label);
	}

	/**
	 * Updates all algos in the set. Guards against double updates if location is involved.
	 * @param algoSet algo set
	 */
	public void updateAllAlgosInSet(@Nonnull AlgorithmSet algoSet) {
		this.algoSetCurrentlyUpdated = algoSet;
		algoSet.updateAll();
		this.algoSetCurrentlyUpdated = null;
	}

	/**
	 * @return the algo set currently updated by
	 * GeoElement.updateDependentObjects()
	 */
	public AlgorithmSet getAlgoSetCurrentlyUpdated() {
		return algoSetCurrentlyUpdated;
	}

	/**
	 * @param b new value of update construction flag
	 */
	public void setUpdateConstructionRunning(boolean b) {
		updateConstructionRunning = b;
	}

	/**
	 * @return a copy of the set of all geo labels that are currently being used
	 */
	public Set<String> getAllGeoLabels() {
		return new HashSet<>(geoTable.keySet());
	}

	/**
	 * @return a copy of the set of all labels that are currently being used
	 */
	public Set<String> getAllLabels() {
		Set<String> ret = new HashSet<>(getAllGeoLabels());
		if (geoCasCellTable != null) {
			ret.addAll(geoCasCellTable.keySet());
		}
		return ret;
	}

	/**
	 * @return whether some geos have activated spreadsheet trace
	 */
	public boolean hasSpreadsheetTracingGeos() {
		return spreadsheetTraces;
	}

	/**
	 * Notify the construction about a geo with spreadsheet tracing
	 */
	public void addTracingGeo() {
		spreadsheetTraces = true;
	}

	/**
	 * @param allow whether unbounded angles are allowed
	 */
	public void setAllowUnboundedAngles(boolean allow) {
		this.allowUnboundedAngles = allow;
	}

	/**
	 * @return whether unbounded angles are allowed on file load
	 */
	public boolean isAllowUnboundedAngles() {
		return this.allowUnboundedAngles;
	}

	/**
	 * Update construction after language change (affects Name[] and similar
	 * algos)
	 */
	public void updateConstructionLanguage() {
		// collect notifyUpdate calls using xAxis as dummy geo
		updateConstructionRunning = true;
		boolean oldFlag = this.kernel.getApplication().isBlockUpdateScripts();
		this.kernel.getApplication().setBlockUpdateScripts(true);
		try {
			// G.Sturr 2010-5-28: turned this off so that random numbers can be
			// traced
			// if (!kernel.isMacroKernel() && kernel.app.hasGuiManager())
			// kernel.app.getGuiManager().startCollectingSpreadsheetTraces();

			// update all independent GeoElements
			int size = ceList.size();
			for (int i = 0; i < size; ++i) {
				ConstructionElement ce = ceList.get(i);
				if (ce.isGeoElement()) {
					if (((GeoElement) ce).isGeoText()
							&& ((GeoElement) ce).getParentAlgorithm() != null) {
						((GeoElement) ce).getParentAlgorithm().update();
					}
					ce.update();
				}
			}
		} finally {
			this.kernel.getApplication().setBlockUpdateScripts(oldFlag);
			updateConstructionRunning = false;
		}
	}

	/** TODO can we kill this now that we don't use MQ? */
	public void updateConstructionLaTeX() {
		boolean oldFlag = this.kernel.getApplication().isBlockUpdateScripts();
		this.kernel.getApplication().setBlockUpdateScripts(true);
		// TODO we do not need the whole construction update here
		if (latexGeos != null) {
			GeoElement.updateCascade(latexGeos, new TreeSet<>(),
					true);
		}
		this.latexGeos = null;
		this.kernel.getApplication().setBlockUpdateScripts(oldFlag);

	}

	/**
	 * @param algo algo dependent on view pixel size
	 */
	public void registerCorner5(EuclidianViewCE algo) {
		if (this.corner5Algos == null) {
			this.corner5Algos = new ArrayList<>();
		}
		this.corner5Algos.add(algo);
	}

	/**
	 * @param algo algo dependent on rotation of 3D view
	 */
	public void registerCorner11(EuclidianViewCE algo) {
		if (this.corner11Algos == null) {
			this.corner11Algos = new ArrayList<>();
		}
		this.corner11Algos.add(algo);
	}

	/**
	 * @return all function variables registered for parsing
	 */
	public String[] getRegisteredFunctionVariables() {
		return registeredFV.toArray(new String[0]);
	}

	/**
	 * @param geo element using LaTeX
	 */
	public void addLaTeXGeo(GeoElement geo) {
		if (latexGeos == null) {
			latexGeos = new ArrayList<>();
		}
		this.latexGeos.add(geo);

	}

	/**
	 * @return number of CAS cells
	 */
	public int getCASObjectNumber() {
		int counter = 0;
		for (ConstructionElement ce : ceList) {
			if (ce instanceof GeoCasCell) {
				++counter;
			} else if (ce instanceof AlgoCasCellInterface) {
				++counter;
			}
		}
		return counter;
	}

	/**
	 * @param A - start point of segment
	 * @param B - end point of segment
	 * @return segment defined by A and B
	 */
	public GeoSegment getSegmentFromAlgoList(GeoPoint A, GeoPoint B) {
		for (AlgoElement curr : algoList) {
			if (curr instanceof AlgoJoinPointsSegment) {
				if ((curr.getInput(0).equals(A)
						&& curr.getInput(1).equals(B))
						|| (curr.getInput(0).equals(B)
						&& curr.getInput(1).equals(A))) {
					return ((AlgoJoinPointsSegment) curr).getSegment();
				}
			}
		}
		return null;
	}

	/**
	 * @return z-axis
	 */
	final public GeoAxisND getZAxis() {
		return companion.getZAxis();
	}

	/**
	 * @return plane z=0
	 */
	final public GeoDirectionND getXOYPlane() {
		return companion.getXOYPlane();
	}

	/**
	 * @return space placeholder
	 */
	final public GeoDirectionND getSpace() {
		return companion.getSpace();
	}

	/**
	 * @return clipping cube
	 */
	final public GeoElement getClippingCube() {
		return companion.getClippingCube();
	}

	/**
	 * @return map label =&gt; geo
	 */
	public HashMap<String, GeoElement> getGeoTable() {
		return geoTable;
	}

	/**
	 * @return whether this is a 3D instance
	 */
	public boolean is3D() {
		return companion.is3D();
	}

	/**
	 * @param ce1 construction element
	 * @param check filter
	 * @return previous element in construction order that fits the filter
	 */
	public GeoElementND getPrevious(GeoElementND ce1, Predicate<GeoElementND> check) {
		ConstructionElement ce = getConstructionElement(ce1);

		int idx = ceList.indexOf(ce);
		if (idx < 0) {
			idx = ceList.size();
		}
		for (int i = idx - 1; i >= 0; i--) {
			if (check.test(ceList.get(i).getGeoElements()[0])) {
				return ceList.get(i).getGeoElements()[0];
			}
		}
		return null;
	}

	/**
	 * @param ce1 construction element
	 * @param check filter
	 * @return next element in construction order that fits the filter
	 */
	public GeoElementND getNext(GeoElementND ce1, Predicate<GeoElementND> check) {
		ConstructionElement ce = getConstructionElement(ce1);

		int idx = ceList.indexOf(ce);
		if (idx < 0) {
			return null;
		}
		for (int i = idx + 1; i < ceList.size(); i++) {
			if (check.test(ceList.get(i).getGeoElements()[0])) {
				return ceList.get(i).getGeoElements()[0];
			}
		}
		return null;
	}

	private ConstructionElement getConstructionElement(GeoElementND geo) {
		return geo.getParentAlgorithm() != null ? geo.getParentAlgorithm() : geo.toGeoElement();
	}

	/**
	 * Initializes and returns the LabelManager instance
	 * @return the LabelManager instance
	 */
	public LabelManager getLabelManager() {
		if (labelManager == null) {
			labelManager = new LabelManager(this);
			labelManager.setAngleLabels(
					kernel.getApplication().getConfig().isGreekAngleLabels());
		}
		return labelManager;
	}

	/**
	 * Checks for objects that are only supported with the 3D app.
	 * @return whether 3D object or inputboxes are present
	 */
	public boolean requires3D() {
		return has3DObjects() || hasInputBoxes(any -> true);
	}

	public ArrayList<Group> getGroups() {
		return groups;
	}

	/**
	 * Add a group to the list.
	 * @param group group to add
	 */
	public void addGroupToGroupList(Group group) {
		getGroups().add(group);
	}

	/**
	 * Remove a group from the list.
	 * @param group group to remove
	 */
	public void removeGroupFromGroupList(Group group) {
		getGroups().remove(group);
	}

	/**
	 * creates group of selected geos and adds it to the construction
	 * @param geos - list of geos selected for grouping
	 */
	public void createGroup(ArrayList<GeoElement> geos) {
		Group group = new Group(geos);
		addGroupToGroupList(group);
	}

	public LayerManager getLayerManager() {
		return layerManager;
	}

	/**
	 * creates group of geos
	 * @param geos - list of geos to be grouped
	 */
	public void createGroupFromSelected(ArrayList<GeoElement> geos) {
		EuclidianView ev = getApplication().getActiveEuclidianView();

		ungroupGroups(geos);
		unfixAll(geos);
		ev.getEuclidianController().splitSelectedStrokes(true);

		createGroup(geos);
		getLayerManager().groupObjects(geos);
		ev.invalidateDrawableList();
	}

	/**
	 * ungroups a group of geos
	 * @param geos - list of geos to be ungrouped
	 */
	public void ungroupGroups(ArrayList<GeoElement> geos) {
		for (GeoElement geo : geos) {
			Group groupOfGeo = geo.getParentGroup();
			if (groupOfGeo != null) {
				removeGroupFromGroupList(groupOfGeo);
				geo.setParentGroup(null);
			}
		}
	}

	private void unfixAll(ArrayList<GeoElement> geos) {
		for (GeoElement geo : geos) {
			geo.setFixed(false);
		}
	}

	/**
	 * creates group of objects given by their labels
	 * @param objects - list of labels of objects to be grouped
	 */
	public void groupObjects(String[] objects) {
		ArrayList<GeoElement> geos = getGeosByLabel(objects);
		createGroupFromSelected(geos);
	}

	/**
	 * ungroups group of objects given by their labels
	 * @param objects - list of labels of objects to be ungrouped
	 */
	public void ungroupObjects(String[] objects) {
		ArrayList<GeoElement> geos = getGeosByLabel(objects);
		ungroupGroups(geos);
	}

	/**
	 * @param object label of object
	 * @return array of labels of objects in the same group as the given object
	 */
	public String[] getObjectsOfItsGroup(String object) {
		Group parentGroup = getParentGroup(object);
		if (parentGroup != null) {
			ArrayList<GeoElement> geos = parentGroup.getGroupedGeos();
			String[] objectsInGroup = new String[geos.size()];
			for (int i = 0; i < objectsInGroup.length; i++) {
				objectsInGroup[i] = geos.get(i).getLabelSimple();
			}
			return objectsInGroup;
		}
		return null;
	}

	private Group getParentGroup(String object) {
		GeoElement geoInGroup = geoTable.get(object);
		return geoInGroup.getParentGroup();
	}

	private ArrayList<GeoElement> getGeosByLabel(String[] list) {
		ArrayList<GeoElement> geos = new ArrayList<>();
		for (String g : list) {
			if (geoTable.containsKey(g)) {
				geos.add(geoTable.get(g));
			}
		}
		return geos;
	}

	/**
	 * @param geo construction element
	 * @return whether object has unlabeled predecessors
	 */
	public boolean hasUnlabeledPredecessors(GeoElement geo) {
		final AlgoElement algo = geo.getParentAlgorithm();
		if (algo != null) {
			for (GeoElement el : algo.getInput()) {
				if (el.getLabelSimple() == null) {
					return true;
				}
			}
		}
		return false;
	}
}
