package experiment.inference;

import java.util.Map;

import experiment.graph.LineClass2;
import experiment.graph.dataStructure;
import experiment.parameter.ParaClass;

public class GraphPotentialClass {
	public double[][] nodePot;
	public double[][][] edgePot;
	private Map<String, String> labelDic;
	private IndexMap im;
	
	public GraphPotentialClass(Map<String, String> labelDic, IndexMap im) {
		this.labelDic = labelDic;
		this.im = im;
	}
	
	/**
	 * This function has two purposes: 1) compute potential for each node and each edge, 2) accumulate the sufficient statistics for each possible configuration
	 * @param ds
	 * @param edgeStruct
	 * @param param
	 */
	public void MakePotentials(dataStructure ds, EdgeStructClass edgeStruct, ParaClass param) {
		int nStates = labelDic.size();
		
		int nNodes = edgeStruct.nNodes;
		nodePot = new double[nNodes][nStates];

		// Compute node potentials
		for (int j = 0; j < ds.data.size(); j++) {
			LineClass2 lc = ds.data.get(j);

			// Initialise the node potential just in case that the line is not successfully parsed
			for (int stateID = 0; stateID < nStates; stateID++)
				nodePot[j][stateID] = 0;
					
			// compute the node potential
			for (int stateID = 0; stateID < nStates; stateID++) {
				double pot = ComputeNodePotential(stateID, lc, im, param);
				//if (pot > 20) System.out.println("wrong node pot: " + pot);
				nodePot[j][stateID] = Math.exp(pot);;
			}
		}

		// Compute edge potentials
		int nEdges = edgeStruct.nEdges;
		edgePot = new double[nStates][nStates][nEdges];

		for (int i = 0; i < nEdges; i++) {
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nStates; k++) {
					double pot = ComputeEdgePotential(j, k, im, param);
					//if (pot > 20) System.out.println("wrong edge pot: " + pot);
					edgePot[j][k][i] = Math.exp(pot);
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
		
		// label_feature
		factorName = "Label_Features";
		weights = param.v.get(factorName);
		for (int j = 0; j < lc.features.length; j++) {
			double count = lc.features[j];
			index = im.GetIndexStateFeatures(stateID, j);
			pot += count * weights[index];
		}
		
		if (pot > 20) pot = 20;
		
		return pot;
	}
}
