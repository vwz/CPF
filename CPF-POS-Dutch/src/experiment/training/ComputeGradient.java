package experiment.training;

import java.util.Map;
import java.util.Map.Entry;

import experiment.graph.LineClass2;
import experiment.graph.dataStructure;
import experiment.inference.EdgeStructClass;
import experiment.inference.IndexMap;
import experiment.inference.MergedEdgeStructClass;
import experiment.parameter.ParaClass;
import experiment.suffstats.ComputeSufficientStats;
import experiment.suffstats.SuffStatsClass;

public class ComputeGradient {
	private Map<String, String> labelDic;
	private IndexMap im;
	public ParaClass g;
	public SuffStatsClass suffstats;
	public SuffStatsClass suffstatsExp;
	
	public ComputeGradient(Map<String, String> labelDic, IndexMap im) {
		this.labelDic = labelDic;
		this.im = im;
		this.g = new ParaClass();
	}
	
	/**
	 * This function is to get the gradient for general features: E_p[h(X,Y)] 
	 * @param ds
	 * @param edgeStruct: this is the general feature's edge structure
	 * @param nodeBel: this is computed from general feature's edge structure
	 * @param edgeBel: this is computed from general feature's edge structure
	 */
	public void GetGradient(dataStructure ds, EdgeStructClass edgeStruct, double[][] nodeBel, double[][][] edgeBel, ParaClass param) {
		this.suffstats = new SuffStatsClass();
		suffstats.Initialize(param);
		this.suffstatsExp = new SuffStatsClass();
		suffstatsExp.Initialize(param);
		
		ComputeSufficientStats css = new ComputeSufficientStats(labelDic, im);

		// Compute node sufficient statistics
		for (int j = 0; j < ds.data.size(); j++) {
			LineClass2 lc = ds.data.get(j);
			
			// Get h(X,Y)
			if (lc.label.length() > 0)
				css.GetNodeSuffStatsForV(ds, j, lc.label, suffstats, 1);
			
			// Get E_p[h(X,Y)]
			for (Entry<String, String> e : labelDic.entrySet()) {
				int stateID = Integer.valueOf(e.getValue());
				css.GetNodeSuffStatsForV(ds, j, e.getKey(), suffstatsExp, nodeBel[j][stateID]);
			}
		}

		// Compute edge sufficient statistics
		int nEdges = edgeStruct.nEdges;
		for (int i = 0; i < nEdges; i++) {
			// get i-th edge's ground truth
			int[] edge = edgeStruct.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			LineClass2 lc1 = ds.data.get(n1);
			LineClass2 lc2 = ds.data.get(n2);
			
			// Get h(X,Y)
			if (lc1.label.length() > 0 && lc2.label.length() > 0)
				css.GetEdgeSuffStatsForV(ds, n1, n2, lc1.label, lc2.label, suffstats, 1);
			
			// Get E_p[h(X,Y)]
			for (Entry<String, String> e1 : labelDic.entrySet()) {
				String yprop1 = e1.getKey();
				int stateID1 = Integer.valueOf(e1.getValue());
				for (Entry<String, String> e2 : labelDic.entrySet()) {
					String yprop2 = e2.getKey();
					int stateID2 = Integer.valueOf(e2.getValue());
					css.GetEdgeSuffStatsForV(ds, n1, n2, yprop1, yprop2, suffstatsExp, edgeBel[stateID1][stateID2][i]);
				}
			}
		}
		
		// Merge sufficient statistics
		css.MergeSuffStatsForV(suffstats);
		css.MergeSuffStatsForV(suffstatsExp);
	}
	
	/**
	 * This function is to get the gradient for general features: E_q[h(X,Y)] 
	 * @param ds
	 * @param edgeStruct: this is the general feature's edge structure
	 * @param mesc: this contains the index mapping between general feature edges and the merged edge structure's edges
	 * @param nodeBel: this is computed on the merged edge structure
	 * @param edgeBel: this is computed on the merged edge structure
	 * @param param
	 */
	public void GetGradient(dataStructure ds, EdgeStructClass edgeStruct, MergedEdgeStructClass mesc, double[][] nodeBel, double[][][] edgeBel, ParaClass param) {
		this.suffstats = new SuffStatsClass();
		suffstats.Initialize(param);
		this.suffstatsExp = new SuffStatsClass();
		suffstatsExp.Initialize(param);
		
		ComputeSufficientStats css = new ComputeSufficientStats(labelDic, im);

		// Compute node sufficient statistics
		for (int j = 0; j < ds.data.size(); j++) {
			LineClass2 lc = ds.data.get(j);
			
			// Get h(X,Y)
			if (lc.label.length() > 0)
				css.GetNodeSuffStatsForV(ds, j, lc.label, suffstats, 1);
			
			// Get E_p[h(X,Y)]
			for (Entry<String, String> e : labelDic.entrySet()) {
				int stateID = Integer.valueOf(e.getValue());
				double q = nodeBel[j][stateID];
				css.GetNodeSuffStatsForV(ds, j, e.getKey(), suffstatsExp, q);
			}
		}

		// Compute edge sufficient statistics
		int nEdges = edgeStruct.nEdges;
		for (int i = 0; i < nEdges; i++) {
			// get i-th edge's ground truth
			int[] edge = edgeStruct.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			String edgeID = n1 + "_" + n2;
			int index = mesc.IndexEdgeStruct.get(edgeID);
			
			LineClass2 lc1 = ds.data.get(n1);
			LineClass2 lc2 = ds.data.get(n2);
			
			// Get h(X,Y)
			if (lc1.label.length() > 0 && lc2.label.length() > 0)
				css.GetEdgeSuffStatsForV(ds, n1, n2, lc1.label, lc2.label, suffstats, 1);
			
			// Get E_p[h(X,Y)]
			for (Entry<String, String> e1 : labelDic.entrySet()) {
				String yprop1 = e1.getKey();
				int stateID1 = Integer.valueOf(e1.getValue());
				for (Entry<String, String> e2 : labelDic.entrySet()) {
					String yprop2 = e2.getKey();
					int stateID2 = Integer.valueOf(e2.getValue());
					double q = edgeBel[stateID1][stateID2][index];
					css.GetEdgeSuffStatsForV(ds, n1, n2, yprop1, yprop2, suffstatsExp, q);
				}
			}
		}
		
		// Merge sufficient statistics
		css.MergeSuffStatsForV(suffstats);
		css.MergeSuffStatsForV(suffstatsExp);
	}
}
