package experiment.inference;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ConstraintFeatureClass;
import experiment.constraint.ObsClass;
import experiment.graph.LineClass2;
import experiment.graph.dataStructure;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;

public class ConstraintGraphPotentialClass {
	public double[][] nodePot;
	public double[][][] edgePot;
	private Map<String, String> labelDic;
	private IndexMap im;
	private ConstraintClass cc;
	private metaParaClass mpc;

	public ConstraintGraphPotentialClass(Map<String, String> labelDic, IndexMap im, ConstraintClass cc, metaParaClass mpc) {
		this.labelDic = labelDic;
		this.im = im;
		this.cc = cc;
		this.mpc = mpc;
	}

	/**
	 * This function has two purposes: 1) compute potential for each node and each edge, 2) accumulate the sufficient statistics for each possible configuration
	 * @param ds
	 * @param edgeStructCons
	 * @param param
	 * @param obsList
	 */
	public void MakePotentials(dataStructure ds, MergedEdgeStructClass mesc, ParaClass param, List<ObsClass> obsList) {
		int nStates = labelDic.size();

		// Compute node potentials
		int nNodes = mesc.mergedEdgeStruct.nNodes;
		nodePot = new double[nNodes][nStates];

		for (int j = 0; j < ds.data.size(); j++) {
			LineClass2 lc = ds.data.get(j);

			// Initialize the node potential just in case that the line is not successfully parsed
			for (int stateID = 0; stateID < nStates; stateID++)
				nodePot[j][stateID] = 0;

			// compute the node potential
			for (int stateID = 0; stateID < nStates; stateID++) {
				double pot = ComputeNodePotential(stateID, lc, im, param);
				nodePot[j][stateID] += pot;
			}

			// compute the node constraint potential
			for (Entry<String, String> e : labelDic.entrySet()) {
				String yprop = e.getKey();
				int stateID = Integer.valueOf(e.getValue());
				double pot = ComputeNodeConsPotential(yprop, ds, j, im, param, obsList, cc);
				nodePot[j][stateID] += pot;
			}
			
			// get exponential value
			for (int stateID = 0; stateID < nStates; stateID++)
				nodePot[j][stateID] = Math.exp(nodePot[j][stateID]);
		}

		// Compute edge potentials
		int nEdges = mesc.mergedEdgeStruct.nEdges;
		edgePot = new double[nStates][nStates][nEdges];

		for (int i = 0; i < nEdges; i++) {
			// compute the edge potential
			int[] edge = mesc.mergedEdgeStruct.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			String edgeID = n1 + "_" + n2;
			
			// Initialize the edge potential
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nStates; k++) {
					edgePot[j][k][i] = 0;
				}
			}
			
			// If this edge comes from the general feature's edge structure
			if (mesc.IndexEdgeStruct.containsKey(edgeID)) {
				for (int j = 0; j < nStates; j++) {
					for (int k = 0; k < nStates; k++) {
						double pot = ComputeEdgePotential(j, k, im, param);
						edgePot[j][k][i] += pot;
					}
				}
				//System.out.println("general feature");
			}
			// If this edge comes from the constraint feature's edge structure
			if (mesc.IndexEdgeStructCons.containsKey(edgeID)) {
				//System.out.println("constraint feature");
				for (Entry<String, String> e1 : labelDic.entrySet()) {
					String yprop1 = e1.getKey();
					int j = Integer.valueOf(e1.getValue());
					for (Entry<String, String> e2 : labelDic.entrySet()) {
						String yprop2 = e2.getKey();
						int k = Integer.valueOf(e2.getValue());
						double pot = ComputeEdgeConsPotential(yprop1, yprop2, ds, n1, n2, im, param, obsList, cc);
						edgePot[j][k][i] += pot;
					}
				}
			}
			
			// get exponential value
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nStates; k++) {
					edgePot[j][k][i] = Math.exp(edgePot[j][k][i]);
				}
			}
		}
	}
	
	/** change 9: add inferP to do filtering **/
	public void MakePotentials(dataStructure ds, MergedEdgeStructClass mesc, ParaClass param, List<ObsClass> obsList, InferClass inferP) {
		int nStates = labelDic.size();

		// Compute node potentials
		int nNodes = mesc.mergedEdgeStruct.nNodes;
		nodePot = new double[nNodes][nStates];

		for (int j = 0; j < ds.data.size(); j++) {
			LineClass2 lc = ds.data.get(j);

			// Initialize the node potential just in case that the line is not successfully parsed
			for (int stateID = 0; stateID < nStates; stateID++)
				nodePot[j][stateID] = 0;

			// compute the node potential
			for (int stateID = 0; stateID < nStates; stateID++) {
				double pot = ComputeNodePotential(stateID, lc, im, param);
				nodePot[j][stateID] += pot;
			}

			// compute the node constraint potential
			for (Entry<String, String> e : labelDic.entrySet()) {
				String yprop = e.getKey();
				int stateID = Integer.valueOf(e.getValue());
				double p = inferP.nodeBel[j][stateID];
				double pot = ComputeNodeConsPotential(yprop, ds, j, im, param, obsList, cc, p);
				nodePot[j][stateID] += pot;
			}
			
			// get exponential value
			for (int stateID = 0; stateID < nStates; stateID++)
				nodePot[j][stateID] = Math.exp(nodePot[j][stateID]);
		}

		// Compute edge potentials
		int nEdges = mesc.mergedEdgeStruct.nEdges;
		edgePot = new double[nStates][nStates][nEdges];

		for (int i = 0; i < nEdges; i++) {
			// compute the edge potential
			int[] edge = mesc.mergedEdgeStruct.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			String edgeID = n1 + "_" + n2;
			
			// Initialize the edge potential
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nStates; k++) {
					edgePot[j][k][i] = 0;
				}
			}
			
			// If this edge comes from the general feature's edge structure
			if (mesc.IndexEdgeStruct.containsKey(edgeID)) {
				for (int j = 0; j < nStates; j++) {
					for (int k = 0; k < nStates; k++) {
						double pot = ComputeEdgePotential(j, k, im, param);
						edgePot[j][k][i] += pot;
					}
				}
				//System.out.println("general feature");
			}
			
			// If this edge comes from the constraint feature's edge structure
			if (mesc.IndexEdgeStructCons.containsKey(edgeID)) {
				//System.out.println("constraint feature");
				for (Entry<String, String> e1 : labelDic.entrySet()) {
					String yprop1 = e1.getKey();
					int j = Integer.valueOf(e1.getValue());
					for (Entry<String, String> e2 : labelDic.entrySet()) {
						String yprop2 = e2.getKey();
						int k = Integer.valueOf(e2.getValue());
						double pot = ComputeEdgeConsPotential(yprop1, yprop2, ds, n1, n2, im, param, obsList, cc);
						edgePot[j][k][i] += pot;
					}
				}
			}
			
			// get exponential value
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nStates; k++) {
					edgePot[j][k][i] = Math.exp(edgePot[j][k][i]);
				}
			}
		}
	}

	/**
	 * This is to compute the edge potential w.r.t. two stateID's
	 * @param stateID1
	 * @param stateID2
	 * @param im
	 * @param param
	 * @param suffstats: also update during computation
	 * @return
	 */
	private double ComputeEdgePotential(int stateID1, int stateID2, IndexMap im, ParaClass param) {
		double pot = 0;

		String factorName = "";
		int index = -1;

		// label_label
		factorName = "Label_Label";
		index = im.GetIndexStateState(stateID1, stateID2);
		double[] weights = param.v.get(factorName);
		pot += weights[index];
		
		if (pot > 20) pot = 20;

		return pot;
	}

	/**
	 * This is to compute the node potential w.r.t a specific stateID
	 * @param stateID
	 * @param lc
	 * @param im
	 * @param param
	 * @param suffstats: also update during computation
	 * @return
	 */
	private double ComputeNodePotential(int stateID, LineClass2 lc, IndexMap im, ParaClass param) {
		double pot = 0;

		String factorName = "";
		int index = -1;

		// label
		factorName = "Label";
		index = im.GetIndexState(stateID);
		double[] weights = param.v.get(factorName);
		pot += weights[index];

		// label_token
		factorName = "Label_Token";
		weights = param.v.get(factorName);
		for (int j = 0; j < lc.wordIDList.size(); j++) {
			int wordID = Integer.valueOf(lc.wordIDList.get(j)).intValue();
			double count = lc.countIDList.get(j);
			index = im.GetIndexStateToken(stateID, wordID);
			pot += count * weights[index];
		}
		
		// label_entity
		factorName = "Label_Entity";
		weights = param.v.get(factorName);
		for (int j = 0; j < lc.EntitySignalVector.length; j++) {
			double count = lc.EntitySignalVector[j];
			index = im.GetIndexStateEntitySignal(stateID, j);
			pot += count * weights[index];
		}

		if (pot > 20) pot = 20;
		
		return pot;
	}

	/**
	 * This is to compute the node's constraint potential w.r.t a specific stateID
	 * @param yprop
	 * @param ds
	 * @param i
	 * @param im
	 * @param param
	 * @param eoc
	 * @param cc
	 * @return
	 */
	private double ComputeNodeConsPotential(String yprop, dataStructure ds, int i, IndexMap im, ParaClass param, List<ObsClass> obsList, ConstraintClass cc) {
		double pot = 0;

		List<LineClass2> variables = ds.data;
		ObsClass obs1 = obsList.get(i);
		LineClass2 lc1 = variables.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;
		
		ConstraintFeatureClass cfc = new ConstraintFeatureClass();
		
		/**
		 * Update the confidence - part I
		 */
		Map<String, Double> conf = mpc.b;

		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
			
			cfc.GetNodeFeatureSignal(factorName, lc1, obs1, variables, obsList, i);
			
			double f_value = cfc.f_flag ? 1.0 : 0;
			double g_value = cfc.g_flag ? 1.0 : 0;
			
			/**
			 * Update the confidence - part II
			 */
			double zeta_value = f_value - conf.get(factorName) * g_value;
			pot += (param.w.get(factorName) * zeta_value); 
			
			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y0;
		
		if (pot > 20) pot = 20;

		return pot;
	}
	
	/** change 9: add inferP to do filtering **/
	private double ComputeNodeConsPotential(String yprop, dataStructure ds, int i, IndexMap im, ParaClass param, List<ObsClass> obsList, ConstraintClass cc, double p) {
		double pot = 0;

		List<LineClass2> variables = ds.data;
		ObsClass obs1 = obsList.get(i);
		LineClass2 lc1 = variables.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;
		
		ConstraintFeatureClass cfc = new ConstraintFeatureClass();
		
		/**
		 * Update the confidence - part I
		 */
		Map<String, Double> conf = mpc.b;

		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
			
			/** change 9: include the filtering as well **/
			// for y-type constraint, we need to check whether to evaluate this instance or not
			if (cc.ConsTypeIndex.get(factorName).equals("y")) {
				// do filtering
				Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
				if (!labels.contains(yprop)) continue; // the constraint must involve yprop

				if (Math.log(p) < mpc.eps.get(factorName)) continue; // the probability of being yprop must exceed the constraint threshold
				/** change 6.1: only prune for other constraints **/
				/*if (!factorName.equals("Phone_HasNumber") && !factorName.equals("Fax_HasNumber")) {
					if (Math.log(p) < mpc.eps.get(factorName)) continue; // the probability of being yprop must exceed the constraint threshold 
				}*/
			}
			
			cfc.GetNodeFeatureSignal(factorName, lc1, obs1, variables, obsList, i);
			
			double f_value = cfc.f_flag ? 1.0 : 0;
			double g_value = cfc.g_flag ? 1.0 : 0;
			
			/*// compute: (1 / alpha1) * [w * zeta(X,Y) - alpha4 * g(X,Y)] 
			double zeta_value = f_value - mpc.b.get(factorName) * g_value;
			pot += (1.0 / mpc.alpha1) * (param.w.get(factorName) * zeta_value - mpc.alpha4 * g_value);*/
			
			/**
			 * Update the confidence - part II
			 */
			double zeta_value = f_value - conf.get(factorName) * g_value;
			pot += (param.w.get(factorName) * zeta_value); 
			
			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y0;
		
		if (pot > 20) pot = 20;

		return pot;
	}

	/**
	 * This is to compute the edge constraint potential w.r.t. two stateID's
	 * @param yprop1
	 * @param yprop2
	 * @param ds
	 * @param n1
	 * @param n2
	 * @param im
	 * @param param
	 * @param eoc
	 * @param cc
	 * @return
	 */
	private double ComputeEdgeConsPotential(String yprop1, String yprop2, dataStructure ds, int n1, int n2, IndexMap im, ParaClass param, List<ObsClass> obsList, ConstraintClass cc) {
		double pot = 0;

		List<LineClass2> variables = ds.data;
		ObsClass obs1 = obsList.get(n1);
		LineClass2 lc1 = variables.get(n1);
		ObsClass obs2 = obsList.get(n2);
		LineClass2 lc2 = variables.get(n2);

		// propose a change
		String y10 = lc1.predLabel;
		lc1.predLabel = yprop1;
		String y20 = lc2.predLabel;
		lc2.predLabel = yprop2;

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();
		
		/**
		 * Update the confidence - part I
		 */
		Map<String, Double> conf = mpc.b;
		
		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			
			cfc.GetEdgeFeatureSignal(factorName, lc1, lc2, obs1, obs2, ds, obsList);
			
			double f_value = cfc.f_flag ? 1.0 : 0;
			double g_value = cfc.g_flag ? 1.0 : 0;
			
			/**
			 * Update the confidence - part II
			 */
			double zeta_value = f_value - conf.get(factorName) * g_value;
			pot += (param.w.get(factorName) * zeta_value); 
			
			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y10;
		lc2.predLabel = y20;

		if (pot > 20) pot = 20;
		
		return pot;
	}
}


