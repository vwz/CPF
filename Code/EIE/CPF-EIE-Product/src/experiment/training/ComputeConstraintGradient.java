package experiment.training;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ObsClass;
import experiment.graph.dataStructure;
import experiment.inference.EdgeStructClass;
import experiment.inference.IndexMap;
import experiment.inference.MergedEdgeStructClass;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;
import experiment.suffstats.ComputeSufficientStats;
import experiment.suffstats.SuffStatsClass;

public class ComputeConstraintGradient {
	private Map<String, String> labelDic;
	private IndexMap im;
	private ConstraintClass cc;
	private metaParaClass mpc;
	public ParaClass g;
	public SuffStatsClass suffstats;
	public SuffStatsClass suffstatsExp;
	public SuffStatsClass suffstatsExpMetaCons;

	public ComputeConstraintGradient(Map<String, String> labelDic, IndexMap im, ConstraintClass cc, metaParaClass mpc) {
		this.labelDic = labelDic;
		this.im = im;
		this.mpc = mpc;
		this.cc = cc;
		this.g = new ParaClass();
	}

	/**
	 * This function is to get gradient for constraint features: E_q[zeta(X,Y)] with confidence
	 * @param ds
	 * @param edgeStructCons: this is the constraint feature's edge structure
	 * @param mesc: this contains the index mapping between constraint feature edges and the merged edge structure edges 
	 * @param obsList
	 * @param nodeBel: this is computed from the merged edge structure
	 * @param edgeBel: this is computed from the merged edge structure
	 * @param param
	 */
	public void GetGradient(dataStructure ds, EdgeStructClass edgeStructCons, MergedEdgeStructClass mesc, List<ObsClass> obsList, double[][] nodeBelP, double[][][] edgeBelP, double[][] nodeBelQ, double[][][] edgeBelQ, ParaClass param) {
		this.suffstats = new SuffStatsClass();
		suffstats.Initialize(param);
		this.suffstatsExp = new SuffStatsClass();
		suffstatsExp.Initialize(param);
		
		ComputeSufficientStats css = new ComputeSufficientStats(labelDic, im);

		// Compute node sufficient statistics
		int nNodes = ds.data.size();
		for (int j = 0; j < nNodes; j++) {
			
			// No need to get f(X,Y) and g(X,Y)
			
			// Get E_q[f(X,Y)] and E_q[g(X,Y)]
			for (Entry<String, String> e : labelDic.entrySet()) {
				int stateID = Integer.valueOf(e.getValue());
				double p = nodeBelP[j][stateID];
				double q = nodeBelQ[j][stateID];
				css.GetNodeSuffStatsForW(ds, j, e.getKey(), suffstatsExp, obsList, cc, mpc, p, q, "training");
			}
		}

		// Compute edge sufficient statistics
		int nEdges = edgeStructCons.nEdges;
		for (int i = 0; i < nEdges; i++) {
			// get i-th edge's ground truth
			int[] edge = edgeStructCons.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			String edgeID = n1 + "_" + n2;
			int index = mesc.IndexEdgeStructCons.get(edgeID);
			
			// for each constraint edge, only compute gradient with regards to its corresponding constraints
			List<String> consList = edgeStructCons.EdgeToConstraints.get(edgeID);
			
			// No need to get f(X,Y)
			
			// Get E_q[f(X,Y)]
			for (Entry<String, String> e1 : labelDic.entrySet()) {
				String yprop1 = e1.getKey();
				int stateID1 = Integer.valueOf(e1.getValue());
				for (Entry<String, String> e2 : labelDic.entrySet()) {
					String yprop2 = e2.getKey();
					int stateID2 = Integer.valueOf(e2.getValue());
					double q = edgeBelQ[stateID1][stateID2][index];
					css.GetEdgeSuffStatsForW(ds, n1, n2, yprop1, yprop2, suffstatsExp, obsList, cc, consList, q);
				}
			}
		}
		
		// Merge sufficient statistics: E_q[zeta(X,Y)] = E_q[f(X,Y) - conf * g(X,Y)]
		Map<String, Double> conf = mpc.b;
		css.MergeSuffStatsForW(suffstatsExp, conf);
	}
	
	/**
	 * This function is to the meta constraints: pi
	 * @param ds
	 * @param edgeStructCons: this is the constraint feature's edge structure
	 * @param mesc: this contains the index mapping between constraint feature edges and the merged edge structure edges 
	 * @param obsList
	 * @param nodeBel: this is computed from the merged edge structure
	 * @param edgeBel: this is computed from the merged edge structure
	 * @param param
	 */
	public void GetMetaCons(dataStructure ds, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, MergedEdgeStructClass mesc, List<ObsClass> obsList, double[][] nodeBel, double[][][] edgeBel, ParaClass param, Map<String, Double> m_cs) {
		this.suffstatsExpMetaCons = new SuffStatsClass();
		suffstatsExpMetaCons.Initialize(param);
		
		ComputeSufficientStats css = new ComputeSufficientStats(labelDic, im);

		// Compute node sufficient statistics
		int nNodes = ds.data.size();
		for (int j = 0; j < nNodes; j++) {

			// Get E_q[f(X,Y)] and E_q[g(X,Y)]
			for (Entry<String, String> e : labelDic.entrySet()) {
				int stateID = Integer.valueOf(e.getValue());
				double p = nodeBel[j][stateID];

				css.GetNodeSuffStats_Ytype_MetaCons(ds, j, e.getKey(), suffstatsExpMetaCons, obsList, cc, mpc, p);
			}
		}

		// Compute edge sufficient statistics
		int nEdges = edgeStructCons.nEdges;
		for (int i = 0; i < nEdges; i++) {
			// get i-th edge's ground truth
			int[] edge = edgeStructCons.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			String edgeID = n1 + "_" + n2;
			int indexp = -1;
			if (edgeStruct.IndexEdgeStruct.containsKey(edgeID))
				indexp = edgeStruct.IndexEdgeStruct.get(edgeID);
			
			// for each constraint edge, only compute gradient with regards to its corresponding constraints 
			List<String> consList = edgeStructCons.EdgeToConstraints.get(edgeID);

			// Get E_q[f(X,Y)]
			for (Entry<String, String> e1 : labelDic.entrySet()) {
				String yprop1 = e1.getKey();
				int stateID1 = Integer.valueOf(e1.getValue());
				for (Entry<String, String> e2 : labelDic.entrySet()) {
					String yprop2 = e2.getKey();
					int stateID2 = Integer.valueOf(e2.getValue());

					double p = 0;
					if (indexp != -1)
						p = edgeBel[stateID1][stateID2][indexp]; // if (n1,n2) is also the general feature
					else 
						p = nodeBel[n1][stateID1] * nodeBel[n2][stateID2]; // if (n1,n2) is not the general feature, then P(n1) and P(n2) are independent

					css.GetEdgeSuffStats_Ytype_MetaCons(ds, n1, n2, yprop1, yprop2, suffstatsExpMetaCons, obsList, cc, consList, mpc, p);
				}
			}
		}
		
		// compute tilde{pi}_c - pi_c
		for (Entry<String, Double> e : suffstatsExpMetaCons.w.entrySet()) {
			String factorName = e.getKey();
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			double pi_c = mpc.pi.get(factorName);
			double m_c = m_cs.get(factorName); // m_c that is only unique to this entity
			
			double tilde_pi_c = suffstatsExpMetaCons.w.get(factorName);
			double value = tilde_pi_c - pi_c * m_c; // compute meta prior
			suffstatsExpMetaCons.w.put(factorName, value);
		}
	}
	
	/**
	 * This function is to get the gradient for general features: E_q[zeta(X,Y)*sigma*(1-sigma)*C*[h(X,Y) - E_p[h(X,Y)]]] 
	 * @param ds
	 * @param edgeStruct: this is the general feature's edge structure
	 * @param mesc: this contains the index mapping between general feature edges and the merged edge structure's edges
	 * @param nodeBel: this is computed on the merged edge structure
	 * @param edgeBel: this is computed on the merged edge structure
	 * @param param
	 */
	public void GetGradientP_Ytype(dataStructure ds, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, MergedEdgeStructClass mesc, double[][] nodeBelP, double[][][] edgeBelP, double[][] nodeBelQ, double[][][] edgeBelQ, ParaClass param, List<ObsClass> obsList, ConstraintClass cc, Map<String, Double> Eqzeta, Map<String, Double> pi) {
		this.suffstatsExp = new SuffStatsClass();
		suffstatsExp.Initialize(param);
		this.suffstatsExpMetaCons = new SuffStatsClass();
		suffstatsExpMetaCons.Initialize(param);
		
		ComputeSufficientStats css = new ComputeSufficientStats(labelDic, im);
		int nStates = labelDic.size();
		
		// Compute node sufficient statistics
		int nNodes = ds.data.size();
		for (int j = 0; j < nNodes; j++) {
			
			// Get node j's Markov blanket edges (which means, this blanket only keeps neighbor y's)
			List<int[]> blanketedges = new ArrayList<int[]>();
			if (edgeStruct.blanket.containsKey(j)) 
				blanketedges = edgeStruct.blanket.get(j);
			
			// Get E_q[f(X,Y)] and E_q[g(X,Y)]
			for (Entry<String, String> e : labelDic.entrySet()) {
				int stateID = Integer.valueOf(e.getValue());
				double p = nodeBelP[j][stateID];
				double q = nodeBelQ[j][stateID];
				
				// get the probability for each Markov blanket edge
				List<double[]> p_edges = new ArrayList<double[]>();
				for (int k = 0; k < blanketedges.size(); k++) {
					int[] edgei = blanketedges.get(k);
					String edgeID = edgei[0] + "_" + edgei[1];
					int index = edgeStruct.IndexEdgeStruct.get(edgeID);
					double[] tmp = new double[nStates];
					if (j == edgei[0]) {
						for (int l = 0; l < nStates; l++) 
							tmp[l] = edgeBelP[stateID][l][index];
					} else if (j == edgei[1]) {
						for (int l = 0; l < nStates; l++) 
							tmp[l] = edgeBelP[l][stateID][index];
					}
					p_edges.add(tmp);
				}
				
				// get the probability for each Markov blanket edge
				List<double[]> q_edges = new ArrayList<double[]>();
				for (int k = 0; k < blanketedges.size(); k++) {
					int[] edgei = blanketedges.get(k);
					String edgeID = edgei[0] + "_" + edgei[1];
					int index = mesc.IndexEdgeStruct.get(edgeID);
					double[] tmp = new double[nStates];
					if (j == edgei[0]) {
						for (int l = 0; l < nStates; l++) 
							tmp[l] = edgeBelQ[stateID][l][index];
					} else if (j == edgei[1]) {
						for (int l = 0; l < nStates; l++) 
							tmp[l] = edgeBelQ[l][stateID][index];
					}
					q_edges.add(tmp);
				}
				
				css.GetNodeSuffStatsForV_Ytype_alpha2(ds, j, e.getKey(), blanketedges, suffstatsExp, obsList, cc, mpc, p, q, p_edges, q_edges, Eqzeta);
				css.GetNodeSuffStatsForV_Ytype_alpha3(ds, j, e.getKey(), blanketedges, suffstatsExpMetaCons, obsList, cc, mpc, p, p_edges, pi);
			}
		}

		// Compute edge sufficient statistics
		int nEdges = edgeStructCons.nEdges;
		for (int i = 0; i < nEdges; i++) {
			// get i-th edge's ground truth
			int[] edge = edgeStructCons.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			String edgeID = n1 + "_" + n2;
			int indexp = -1;
			if (edgeStruct.IndexEdgeStruct.containsKey(edgeID))
				indexp = edgeStruct.IndexEdgeStruct.get(edgeID);
			int indexq = mesc.IndexEdgeStructCons.get(edgeID);
			
			// for each constraint edge, only compute gradient with regards to its corresponding constraints 
			List<String> consList = edgeStructCons.EdgeToConstraints.get(edgeID);
			
			// Get node j's Markov blanket edges
			Set<int[]> set = new HashSet<int[]>();
			if (edgeStruct.blanket.containsKey(n1)) {
				List<int[]> blanketedges1 = edgeStruct.blanket.get(n1);
				set.addAll(blanketedges1);
			}
			if (edgeStruct.blanket.containsKey(n2)) {
				List<int[]> blanketedges2 = edgeStruct.blanket.get(n2);
				set.addAll(blanketedges2);
			}
			
			List<int[]> blanketedges = new ArrayList<int[]>();
			for (int[] s : set) {
				boolean c1 = (n1 == s[0] && n2 == s[1]); // (n1,n2) exists in the blanket
				boolean c2 = (n1 == s[1] && n2 == s[0]); // (n2,n1) exists in the blanket
				if (!c1 && !c2)
					blanketedges.add(s); // this Markov blanket adoes not cover the original edge (n1,n2)
			}
			
			// Get E_q[f(X,Y)]
			for (Entry<String, String> e1 : labelDic.entrySet()) {
				String yprop1 = e1.getKey();
				int stateID1 = Integer.valueOf(e1.getValue());
				for (Entry<String, String> e2 : labelDic.entrySet()) {
					String yprop2 = e2.getKey();
					int stateID2 = Integer.valueOf(e2.getValue());
					
					double p = 0;
					if (indexp != -1)
						p = edgeBelP[stateID1][stateID2][indexp]; // if (n1,n2) is also the general feature
					else 
						p = nodeBelP[n1][stateID1] * nodeBelP[n2][stateID2]; // if (n1,n2) is not the general feature, then P(n1) and P(n2) are independent
					double q = edgeBelQ[stateID1][stateID2][indexq];
					
					// get the probability for each Markov blanket edge
					List<double[]> p_edges = new ArrayList<double[]>();
					for (int k = 0; k < blanketedges.size(); k++) {
						int[] edgei = blanketedges.get(k);
						edgeID = edgei[0] + "_" + edgei[1];
						int index = edgeStruct.IndexEdgeStruct.get(edgeID);
						double[] tmp = new double[nStates];
						if (n1 == edgei[0]) {
							for (int l = 0; l < nStates; l++) 
								tmp[l] = edgeBelP[stateID1][l][index];
						} else if (n1 == edgei[1]) {
							for (int l = 0; l < nStates; l++) 
								tmp[l] = edgeBelP[l][stateID1][index];
						} else if (n2 == edgei[0]) {
							for (int l = 0; l < nStates; l++) 
								tmp[l] = edgeBelP[stateID2][l][index];
						} else if (n2 == edgei[1]) {
							for (int l = 0; l < nStates; l++) 
								tmp[l] = edgeBelP[l][stateID2][index];
						}
						p_edges.add(tmp);
					}
					
					// get the probability for each Markov blanket edge
					List<double[]> q_edges = new ArrayList<double[]>();
					for (int k = 0; k < blanketedges.size(); k++) {
						int[] edgei = blanketedges.get(k);
						edgeID = edgei[0] + "_" + edgei[1];
						int index = mesc.IndexEdgeStruct.get(edgeID);
						double[] tmp = new double[nStates];
						if (n1 == edgei[0]) {
							for (int l = 0; l < nStates; l++) 
								tmp[l] = edgeBelQ[stateID1][l][index];
						} else if (n1 == edgei[1]) {
							for (int l = 0; l < nStates; l++) 
								tmp[l] = edgeBelQ[l][stateID1][index];
						} else if (n2 == edgei[0]) {
							for (int l = 0; l < nStates; l++) 
								tmp[l] = edgeBelQ[stateID2][l][index];
						} else if (n2 == edgei[1]) {
							for (int l = 0; l < nStates; l++) 
								tmp[l] = edgeBelQ[l][stateID2][index];
						}
						q_edges.add(tmp);
					}
					
					// get the probability for each node
					double[] p_nodes = new double[2];
					p_nodes[0] = nodeBelP[n1][stateID1];
					p_nodes[1] = nodeBelP[n2][stateID2];
					double[] q_nodes = new double[2];
					q_nodes[0] = nodeBelQ[n1][stateID1];
					q_nodes[1] = nodeBelQ[n2][stateID2];
					
					css.GetEdgeSuffStatsForV_Ytype_alpha2(ds, n1, n2, yprop1, yprop2, blanketedges, suffstatsExp, obsList, cc, consList, mpc, p, q, p_nodes, q_nodes, p_edges, q_edges, Eqzeta, indexp);
					css.GetEdgeSuffStatsForV_Ytype_alpha3(ds, n1, n2, yprop1, yprop2, blanketedges, suffstatsExpMetaCons, obsList, cc, consList, mpc, p, p_nodes, p_edges, pi, indexp);
				}
			}
		}
		
		// Merge sufficient statistics
		css.MergeSuffStatsForV(suffstatsExp);
		css.MergeSuffStatsForV(suffstatsExpMetaCons);
	}
	
	/**
	 * This function is to get the gradient for general features: E_q[zeta(X,Y)*sigma*(1-sigma)*C*[h(X,Y) - E_p[h(X,Y)]]] 
	 * @param ds
	 * @param edgeStruct: this is the general feature's edge structure
	 * @param mesc: this contains the index mapping between general feature edges and the merged edge structure's edges
	 * @param nodeBel: this is computed on the merged edge structure
	 * @param edgeBel: this is computed on the merged edge structure
	 * @param param
	 */
	public void GetGradientU_Ytype(dataStructure ds, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, MergedEdgeStructClass mesc, double[][] nodeBelP, double[][][] edgeBelP, double[][] nodeBelQ, double[][][] edgeBelQ, ParaClass param, List<ObsClass> obsList, ConstraintClass cc, Map<String, Double> Eqzeta, Map<String, Double> piterm) {
		this.suffstatsExp = new SuffStatsClass();
		suffstatsExp.Initialize(param);
		this.suffstatsExpMetaCons = new SuffStatsClass();
		suffstatsExpMetaCons.Initialize(param);
		
		ComputeSufficientStats css = new ComputeSufficientStats(labelDic, im);
		
		// Compute node sufficient statistics
		int nNodes = ds.data.size();
		for (int j = 0; j < nNodes; j++) {
			
			// Get E_q[f(X,Y)] and E_q[g(X,Y)]
			for (Entry<String, String> e : labelDic.entrySet()) {
				int stateID = Integer.valueOf(e.getValue());
				double p = nodeBelP[j][stateID];
				double q = nodeBelQ[j][stateID];
				
				css.GetNodeSuffStatsForU_Ytype_alpha2(ds, j, e.getKey(), suffstatsExp, obsList, cc, mpc, p, q, Eqzeta);
				css.GetNodeSuffStatsForU_Ytype_alpha3(ds, j, e.getKey(), suffstatsExpMetaCons, obsList, cc, mpc, p, piterm);
			}
		}

		// Compute edge sufficient statistics
		int nEdges = edgeStructCons.nEdges;
		for (int i = 0; i < nEdges; i++) {
			// get i-th edge's ground truth
			int[] edge = edgeStructCons.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			String edgeID = n1 + "_" + n2;
			int indexp = -1;
			if (edgeStruct.IndexEdgeStruct.containsKey(edgeID))
				indexp = edgeStruct.IndexEdgeStruct.get(edgeID);
			int indexq = mesc.IndexEdgeStructCons.get(edgeID);
			
			// for each constraint edge, only compute gradient with regards to its corresponding constraints 
			List<String> consList = edgeStructCons.EdgeToConstraints.get(edgeID);
			
			// Get E_q[f(X,Y)]
			for (Entry<String, String> e1 : labelDic.entrySet()) {
				String yprop1 = e1.getKey();
				int stateID1 = Integer.valueOf(e1.getValue());
				for (Entry<String, String> e2 : labelDic.entrySet()) {
					String yprop2 = e2.getKey();
					int stateID2 = Integer.valueOf(e2.getValue());
					
					double p = 0;
					if (indexp != -1)
						p = edgeBelP[stateID1][stateID2][indexp]; // if (n1,n2) is also the general feature
					else 
						p = nodeBelP[n1][stateID1] * nodeBelP[n2][stateID2]; // if (n1,n2) is not the general feature, then P(n1) and P(n2) are independent
					double q = edgeBelQ[stateID1][stateID2][indexq];
					
					css.GetEdgeSuffStatsForU_Ytype_alpha2(ds, n1, n2, yprop1, yprop2, suffstatsExp, obsList, cc, consList, mpc, p, q, Eqzeta);
					css.GetEdgeSuffStatsForU_Ytype_alpha3(ds, n1, n2, yprop1, yprop2, suffstatsExpMetaCons, obsList, cc, consList, mpc, p, piterm);
				}
			}
		}
	}
}
