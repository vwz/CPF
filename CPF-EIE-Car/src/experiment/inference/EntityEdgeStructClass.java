package experiment.inference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import experiment.constraint.ConstraintClass;
import experiment.constraint.EntityObsClass;
import experiment.constraint.ObsClass;
import experiment.constraint.ConstraintPruneIndex;
import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.graph.dataStructure;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;

public class EntityEdgeStructClass {
	public Map<String, EdgeStructClass> entityEdgeStruct; // entityID - the edgeStruct with general features
	public Map<String, EdgeStructClass> entityEdgeStructCons; // entityID - the edgeStruct with constraint features
	public Map<String, MergedEdgeStructClass> entityMergedEdgeStruct; // entityID - the mergedEdgeStruct with both types of features and their edge indices

	public EntityEdgeStructClass() {
		this.entityEdgeStruct = new HashMap<String, EdgeStructClass>();
		this.entityEdgeStructCons = new HashMap<String, EdgeStructClass>();
		this.entityMergedEdgeStruct = new HashMap<String, MergedEdgeStructClass>();
	}

	public void ProcessH(EntityDataClass edc, EntityObsClass eoc, GetDataDomain gdd) {
		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();
			dataStructure ds = e.getValue();

			// Build the graph for general feature edge structure
			GraphClass gc = new GraphClass();
			EdgeStructClass edgeStruct = gc.ConstructGraph(ds, gdd.labelDic);
			entityEdgeStruct.put(entity, edgeStruct);
		}
	}

	public void ProcessC(EntityDataClass edc, EntityObsClass eoc, GetDataDomain gdd, ConstraintClass cc, metaParaClass mpc, Map<String, InferClass> infermap) {
		// clean up the earlier constraint edge structure, if any
		this.entityEdgeStructCons.clear();
		this.entityMergedEdgeStruct.clear();

		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();
			dataStructure ds = e.getValue();

			// Build the graph for constraint feature edge structure
			ConstraintGraphClass cgc = new ConstraintGraphClass();
			List<ObsClass> obsList = eoc.EntityObsList.get(entity);
			InferClass inferP = infermap.get(entity);
			EdgeStructClass edgeStructCons = cgc.ConstructGraph(ds, obsList, gdd.labelDic, cc, mpc, inferP);
			entityEdgeStructCons.put(entity, edgeStructCons);

			// Merge the edgeStruct and edgeStructCons
			MergedEdgeStructClass mergedEdgeStruct = new MergedEdgeStructClass();
			mergedEdgeStruct.Process(entityEdgeStruct.get(entity), edgeStructCons);
			entityMergedEdgeStruct.put(entity, mergedEdgeStruct);
		}
	}

	public void ProcessC(EntityDataClass edc, EntityObsClass eoc, GetDataDomain gdd, ConstraintClass cc) {
		// clean up the earlier constraint edge structure, if any
		this.entityEdgeStructCons.clear();
		this.entityMergedEdgeStruct.clear();

		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();
			dataStructure ds = e.getValue();

			// Build the graph for constraint feature edge structure
			ConstraintGraphClass cgc = new ConstraintGraphClass();
			List<ObsClass> obsList = eoc.EntityObsList.get(entity);
			EdgeStructClass edgeStructCons = cgc.ConstructGraph(ds, obsList, gdd.labelDic, cc);
			entityEdgeStructCons.put(entity, edgeStructCons);

			// Merge the edgeStruct and edgeStructCons
			MergedEdgeStructClass mergedEdgeStruct = new MergedEdgeStructClass();
			mergedEdgeStruct.Process(entityEdgeStruct.get(entity), edgeStructCons);
			entityMergedEdgeStruct.put(entity, mergedEdgeStruct);
		}
	}

	public Map<String, InferClass> GenInferP(EntityDataClass edc, GetDataDomain gdd, ParaClass param, metaParaClass mpc) {
		Map<String, InferClass> map = new HashMap<String, InferClass>();

		int nWords = gdd.wordDic.size();
		int nStates = gdd.labelDic.size();
		int nFeatures = gdd.nFeatures;
		IndexMap im = new IndexMap(nStates, nWords, nFeatures);

		/**** Get sufficient statistics w.r.t. P ****/
		for (String entity : edc.entityData.keySet()) {
			dataStructure ds = edc.entityData.get(entity);
			EdgeStructClass edgeStruct = entityEdgeStruct.get(entity);
			InferClass inferP = LoopyBP_P(ds, param, gdd.labelDic, edgeStruct, im, mpc);
			map.put(entity, inferP);
		}

		return map;
	}

	/**
	 * This is to do loopy BP is done on the general feature's edge structure
	 * @param ds
	 * @param param
	 * @param labelDic
	 * @return
	 */
	private InferClass LoopyBP_P(dataStructure ds, ParaClass param, Map<String, String> labelDic, EdgeStructClass edgeStruct, IndexMap im, metaParaClass mpc) {
		// Make potentials but not differentiating nodes and edges
		GraphPotentialClass gpc = new GraphPotentialClass(labelDic, im);
		gpc.MakePotentials(ds, edgeStruct, param); // 1) make potential for each node and edge, 2) obtain the sufficient statistics for each parameter

		// Infer by loopy BP
		int nNodes = edgeStruct.nNodes;
		int nEdges = edgeStruct.nEdges;
		int nStates = labelDic.size();
		InferClass infer = new InferClass(nNodes, nStates, nEdges, mpc.loopymaxIter);
		infer.InferLoopyBP(gpc.nodePot, gpc.edgePot, edgeStruct);

		return infer;
	}
}
