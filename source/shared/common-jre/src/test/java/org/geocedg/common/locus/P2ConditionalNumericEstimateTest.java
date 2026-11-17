/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import static org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter.richResultOf;
import java.util.*;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.*;
import org.geocedg.common.kernel.locus.metric.*;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geogebra.common.kernel.geos.*;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** P2 section 26.6: full numeric traversal is independent of partial geometry. */
class P2ConditionalNumericEstimateTest extends PreG9bR3RedefineTestBase {
 private GeoLocusV2 fixture(String expression) {
  add("lo=999.99"); add("hi=1000.01");
  add("s=Segment((lo,0),(hi,0))"); add("P=Point(s)"); add("Q="+expression);
  return add("L=LocusV2(Q,P)");
 }
 private LocusMetricResult2D metric(String name,String command) {
  return richResultOf(add(name+"="+command)).getMetricResult();
 }
 private void assertPartial(LocusMetricResult2D r) {
  assertEquals(MetricComputationStatus.SUCCESS,r.getComputationStatus(),r.getDiagnostics().toString());
  assertEquals(MetricGuaranteeClass2D.ESTIMATED,r.getGuaranteeClass());
  assertEquals(MetricCoverage.INCOMPLETE,r.getCoverage());
  assertTrue(r.isConditionedEstimate()); assertTrue(r.isScalarAdmissible());
  var traversal=r.getNumericTraversal().orElseThrow();
  assertEquals(MetricCoverage.COMPLETE,traversal.getNumericCoverage());
  assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED,
    traversal.getGeometricExistence().getCompleteness());
  assertEquals(0,traversal.getRequestedDomain().getLower());
  assertEquals(1,traversal.getRequestedDomain().getUpper());
  assertTrue(r.getErrorEvidence().getEstablishedAbsoluteAmount()<=r.getErrorEvidence().getRequestedTolerance().orElseThrow());
  assertTrue(r.getErrorEvidence().getRigorousEnclosure().isEmpty());
  assertTrue(r.getErrorEvidence().getAssumptions().stream().anyMatch(x->x.contains("partial geometric proof")));
 }
 @Test void entireDomainAndAlgebraKeepPartialGeometry() {
  GeoLocusV2 l=fixture("(x(P),sin(x(P)))");
  String before=l.getSemanticDefinition().getBranches().get(0).getExistenceStructure().getSemanticSignature();
  LocusMetricResult2D r=metric("a","LocusLength(L)"); assertPartial(r);
  double v=((GeoNumeric)lookup("a")).getValue();
  // Independent high-resolution Simpson reference with the literal binary64 endpoints.
  assertEquals(.022945823133382146,v,r.getErrorEvidence().getRequestedTolerance().orElseThrow());
  assertEquals(2*v+1,((GeoNumeric)add("b=2*a+1")).getValue(),0);
  assertEquals(before,l.getSemanticDefinition().getBranches().get(0).getExistenceStructure().getSemanticSignature());
  assertEquals(1,r.getContributions().size());
 }
 @Test void similaritiesKeepEstimateAndWholeDomain() {
  fixture("(x(P),sin(x(P)))");LocusMetricResult2D base=metric("a","LocusLength(L)");
  add("T=Translate(L,(3,-2))");LocusMetricResult2D moved=metric("b","LocusLength(T)");
  add("D=Dilate(L,2,(1,1))");LocusMetricResult2D scaled=metric("c","LocusLength(D)");
  assertPartial(base);assertPartial(moved);assertPartial(scaled);
  assertEquals(base.getMetricValue().getFiniteValue().orElseThrow(),moved.getMetricValue().getFiniteValue().orElseThrow(),1E-12);
  assertEquals(2*base.getMetricValue().getFiniteValue().orElseThrow(),scaled.getMetricValue().getFiniteValue().orElseThrow(),1E-12);
 }
 @ParameterizedTest @ValueSource(strings={"(x(P),1/(sin(x(P))-sin(1000)))","(x(P),sqrt(sin(x(P))-sin(1000)))","(x(P),sin(sin(x(P))))"})
 void opaqueRangeMayNotHideNonTotalConsumers(String expression) {
  fixture(expression);LocusMetricResult2D r=metric("a","LocusLength(L)");
  assertFalse(r.isScalarAdmissible());assertFalse(((GeoNumeric)lookup("a")).isDefined());
  assertTrue(r.getNumericTraversal().isEmpty());
 }
 @ParameterizedTest @ValueSource(ints={2,4})
 void knownNativeJumpsRemainInadmissible(int frequency) {
  add("s=Segment((0,0),(1,0))");add("P=Point(s)");add("nativeSine=sin("+frequency+"*pi*x(P))");add("Q=(x(P),nativeSine)");add("L=LocusV2(Q,P)");
  LocusMetricResult2D r=metric("a","LocusLength(L)");assertNoTotalWithoutExistence(r,"native sine jumps");
  assertFalse(((GeoNumeric)add("b=2*a+1")).isDefined());
 }
 @Test void lossRecoveryAndFileKeepIdentityAndRecompute() {
  fixture("(x(P),sin(x(P)))");GeoNumeric scalar=add("a=LocusLength(L)");add("b=2*a+1");
  var id=getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(scalar);
  assertPartial(richResultOf(scalar).getMetricResult());
  ((GeoNumeric)lookup("lo")).setValue(Double.NaN);((GeoNumeric)lookup("hi")).setValue(4*Math.PI);getKernel().updateConstruction(false);
  assertFalse(scalar.isDefined());assertFalse(lookup("b").isDefined());
  ((GeoNumeric)lookup("lo")).setValue(999.99);((GeoNumeric)lookup("hi")).setValue(1000.01);getKernel().updateConstruction(false);
  assertSame(scalar,lookup("a"));assertTrue(scalar.isDefined());
  reload();GeoElement reopened=lookup("a");assertTrue(reopened.isDefined());
  assertEquals(id,getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(reopened));
  assertPartial(richResultOf(reopened).getMetricResult());
  assertEquals(2*((GeoNumeric)reopened).getValue()+1,((GeoNumeric)lookup("b")).getValue(),0);
 }
 @Test void compatibleRedefinitionRecomputesCurrentNumericEvidence() {
  GeoLocusV2 l=fixture("(x(P),sin(x(P)))");GeoElement a=add("a=LocusLength(L)");
  var id=getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(a);
  long revision=l.getSemanticRevision();
  Outcome changed=redefine("Q","(x(P),sin(x(P))+0.25)",PreG9bR3RedefineTestBase::retainWhenAvailable);
  assertTrue(changed.succeeded(),changed.errors());
  LocusMetricResult2D r=richResultOf(lookup("a")).getMetricResult();assertPartial(r);
  assertTrue(((GeoLocusV2)lookup("L")).getSemanticRevision()>revision);
  assertEquals(((GeoLocusV2)lookup("L")).getSemanticRevision(),r.getNumericTraversal().orElseThrow().getSemanticRevision());
  assertEquals(id,getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(lookup("a")));
 }

 @Test void undoRedoReconstructsCurrentCondition() {
  activateUndo();fixture("(x(P),sin(x(P)))");add("a=LocusLength(L)");getApp().storeUndoInfo();
  ((GeoNumeric)lookup("lo")).setValue(Double.NaN);((GeoNumeric)lookup("hi")).setValue(4*Math.PI);getKernel().updateConstruction(false);getApp().storeUndoInfo();
  assertFalse(lookup("a").isDefined());getKernel().undo();assertPartial(richResultOf(lookup("a")).getMetricResult());
  getKernel().redo();assertFalse(lookup("a").isDefined());getKernel().undo();assertTrue(lookup("a").isDefined());
 }
 @Test void clipboardCopiesDependenciesAndFreshEstimate() {
  fixture("(x(P),sin(x(P)))");GeoElement a=add("a=LocusLength(L)");
  var id=getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(a);
  String clip=InternalClipboard.getTextToSave(getApp(),List.of(a),text -> text);int sep=clip.indexOf('\n');
  InternalClipboard.pasteGeoGebraXMLInternal(getApp(),new ArrayList<>(Arrays.asList(clip.substring(0,sep).split(" "))),clip.substring(sep));
  var registry=getConstruction().getSpatialIdentityRegistry();
  var copy=registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast).filter(r->id.equals(r.getCopySourceId())).findFirst().orElseThrow();
  GeoElement copied=registry.getGeo(copy.getId());assertNotEquals(id,copy.getId());assertPartial(richResultOf(copied).getMetricResult());
  assertEquals(((GeoNumeric)a).getValue(),((GeoNumeric)copied).getValue(),0);
 }
 @Test void budgetFailureAndStaleRevisionCannotPublishScalar() {
  GeoLocusV2 l=fixture("(x(P),sin(x(P)))");LocusDefinition2D d=l.getSemanticDefinition();
  LocusMetricPolicy2D p=LocusMetricPolicy2D.cu1PublicV2().withWorkBudget(new MetricWorkBudget2D(2,1,1));
  LocusMetricResult2D r=compute(d,p,d.getSemanticRevision());assertFalse(r.isScalarAdmissible());
  assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,r.getComputationStatus());
  assertTrue(r.getDiagnostics().stream().anyMatch(x->x.getCode()==MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED));
  assertFalse(compute(d,LocusMetricPolicy2D.cu1PublicV2(),d.getSemanticRevision()+1).isScalarAdmissible());
 }
 @Test void estimatedScalarIsNotRigorousDownstreamEvidence() {
  fixture("(x(P),sin(x(P)))");metric("a","LocusLength(L)");add("Z=P+a*(0,1)");GeoLocusV2 z=add("Lz=LocusV2(Z,P)");
  ReconstructibleLocusEvaluator2D evaluator=(ReconstructibleLocusEvaluator2D)z.getSemanticDefinition().getEvaluatorCapability();
  assertEquals("metric-value-constant",evaluator.getProgramV3Refusal());
  assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED,z.getSemanticDefinition().getBranches().get(0).getExistenceStructure().getCompleteness());
 }
 @ParameterizedTest @org.junit.jupiter.params.provider.EnumSource(value=LocusCoverageCause2D.class,
   names={"NOT_CERTIFIED","BUDGET_EXHAUSTED","SELECTION_NOT_REPRODUCED",
     "RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED","UPSTREAM_NOT_ESTABLISHED",
     "COMPUTATIONAL_DEPENDENCY_FAILURE","INHERITED_TOLERANCE_BEHAVIOUR",
     "PREDICATE_CERTIFIED_FALSE","DEPENDENCY_CERTIFIED_NONEXISTENT"})
 void aDifferentUnresolvedOrAbsentCauseIsNotAdmitted(LocusCoverageCause2D cause) {
  LocusDefinition2D d=fixture("(x(P),sin(x(P)))").getSemanticDefinition();
  var branch=d.getBranches().get(0);var old=branch.getExistenceStructure();
  List<LocusExistenceStructure2D.Coverage> changed=new ArrayList<>();
  boolean absent=cause==LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE||cause==LocusCoverageCause2D.DEPENDENCY_CERTIFIED_NONEXISTENT;
  for(var c:old.getCoverage())changed.add(c.getState()==LocusExistenceStructure2D.CoverageState.UNRESOLVED
    ?new LocusExistenceStructure2D.Coverage(c.getInterval(),absent?LocusExistenceStructure2D.CoverageState.CERTIFIED_DOES_NOT_EXIST:LocusExistenceStructure2D.CoverageState.UNRESOLVED,
      cause,absent?LocusEvidenceNature2D.EXACT:LocusEvidenceNature2D.NONE,"p2-negative-fixture"):c);
  var structure=new LocusExistenceStructure2D(branch.getBranchKey(),branch.getDeclaredDriverDomain(),old.getTopology(),d.getSemanticRevision(),changed,old.getContinuousValidComponents(),List.of(),old.getCompleteness(),"p2-negative-fixture/v1");
  LocusMetricResult2D r=compute(withStructure(d,structure),LocusMetricPolicy2D.cu1PublicV2(),d.getSemanticRevision());
  assertFalse(r.isScalarAdmissible());assertTrue(r.getNumericTraversal().isEmpty());
 }
 @Test void aProofLimitWithoutAnyCertifiedComponentIsNotThePartialMode() {
  LocusDefinition2D d=fixture("(x(P),sin(x(P)))").getSemanticDefinition();var branch=d.getBranches().get(0);
  var structure=LocusExistenceStructure2D.notEstablished(branch.getBranchKey(),branch.getDeclaredDriverDomain(),LocusExistenceStructure2D.DomainTopology.BOUNDED,LocusCoverageCause2D.NUMERIC_PROOF_METHOD_LIMIT,"p2-negative-no-component");
  assertFalse(compute(withStructure(d,structure),LocusMetricPolicy2D.cu1PublicV2(),d.getSemanticRevision()).isScalarAdmissible());
 }
 private LocusDefinition2D withStructure(LocusDefinition2D d,LocusExistenceStructure2D structure) {
  var b=d.getBranches().get(0);
  var branch=new LocusBranch2D(b.getBranchKey(),b.getDeclaredDriverDomain(),structure,b.getOrientation(),b.getProvenance(),b.getLineage(),b.getProperties(),b.getQuality());
  return new LocusDefinition2D(d.getLocusIdentity(),d.getSemanticRevision(),d.getDefinitionStatus(),d.getProvider(),List.of(branch),d.getEvaluatorCapability(),d.getDeterminism(),"p2-cause-frontier/v1",new LocusInstrumentation2D());
 }

 private LocusMetricResult2D compute(LocusDefinition2D d,LocusMetricPolicy2D p,long revision) {
  LocusMetricInstrumentation2D work=new LocusMetricInstrumentation2D();LocusMetricSharedOwner2D owner=new LocusMetricSharedOwner2D(d.getLocusIdentity(),work);
  try(LocusMetricOwnerLease2D lease=owner.acquireLease()) {
   return new LocusMetricEngine2D().compute(new TotalLocusMetricQuery(d.getLocusIdentity(),revision,p),d,new LocusMetricCapabilityHierarchy2D(List.of(new PolicyV2LocusMetricCapability2D())),owner,LocusMetricIndexMode.LAZY_COMPONENT_REVISION,work);
  } catch(LocusMetricComponentBuildException ex) {
   return LocusMetricResults2D.failure(d.getLocusIdentity(),d.getSemanticRevision(),p,ex.getComputationStatus(),Optional.empty(),ex.getDiagnostics());
  }
 }
}
