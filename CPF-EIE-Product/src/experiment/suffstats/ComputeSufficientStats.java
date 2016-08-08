package experiment.suffstats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ConstraintFeatureClass;
import experiment.constraint.ObsClass;
import experiment.decoder.InvertedIndexClass;
import experiment.graph.LineClass2;
import experiment.graph.dataStructure;
import experiment.inference.EdgeStructClass;
import experiment.inference.IndexMap;
import experiment.parameter.metaParaClass;

public class ComputeSufficientStats {
	private Map<String, String> labelDic;
	private IndexMap im;

	public ComputeSufficientStats(Map<String, String> labelDic, IndexMap im) {
		this.labelDic = labelDic;
		this.im = im;
	}

	private List<double[]> MergeStats(List<double[]> value) {
		Map<Double, Double> m = new HashMap<Double, Double>();
		for (int i = 0; i < value.size(); i++) {
			// sum up the values with the same key (i.e. index)
			Double key = Double.valueOf(value.get(i)[0]);
			double x = 0;
			if (m.containsKey(key))
				x = m.get(key).doubleValue();
			Double y = Double.valueOf(x + value.get(i)[1]);
			m.put(key, y);
		}

		List<double[]> out = new ArrayList<double[]>();
		for (Entry<Double, Double> e : m.entrySet()) {
			double[] record = new double[] {e.getKey().doubleValue(), e.getValue().doubleValue()};
			out.add(record);
		}

		return out;
	}

	public void MergeSuffStatsForW(SuffStatsClass suffstats, Map<String, Double> b) {
		for (Entry<String, Double> e : suffstats.w.entrySet()) {
			String factorName = e.getKey();
			double value = suffstats.f.get(factorName) - b.get(factorName) * suffstats.g.get(factorName);
			suffstats.w.put(factorName, value);
		}
	}

	public void MergeSuffStatsForV(SuffStatsClass suffstats) {
		for (Entry<String, List<double[]>> e : suffstats.v.entrySet()) {
			List<double[]> before = e.getValue();
			List<double[]> after = MergeStats(before);
			suffstats.v.put(e.getKey(), after);
		}
	}

	/**
	 * [Decoding] Aggregate both node and edge sufficient statistics for each v parameter (i.e. general feature)
	 * @param ds
	 * @param i
	 * @param yprop
	 * @param suffstats
	 * @param edgeStruct
	 */
	public void GetSuffStatsForV(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, EdgeStructClass edgeStruct) {
		// collect the node and edge sufficient statistics for node i
		GetNodeSuffStatsForV(ds, i, yprop, suffstats, 1.0);
		GetEdgeSuffStatsForV(ds, i, yprop, suffstats, edgeStruct, 1.0);	
	}

	/**
	 * [Decoding] Aggregate both node and edge sufficient statistics for each w parameter (i.e. constraint feature)
	 * @param ds
	 * @param i
	 * @param yprop
	 * @param suffstats
	 * @param obsList
	 * @param iic
	 * @param cc
	 */
	public void GetSuffStatsForW(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, EdgeStructClass edgeStructCons, List<ObsClass> obsList, InvertedIndexClass iic, ConstraintClass cc, metaParaClass mpc, double p) {
		// collect the node and edge sufficient statistics for node i
		GetNodeSuffStatsForW(ds, i, yprop, suffstats, obsList, cc, mpc, p, 1.0, "decoding"); // if getting sufficient statistics, then q = 1.0;
		GetEdgeSuffStatsForW(ds, i, yprop, suffstats, edgeStructCons, obsList, iic, cc, 1.0);

		// no need to merge the sufficient statistics, because w is scalar and we already did the addition during accumulation
	}

	/**
	 * [Decoding, Training] Gather node sufficient statistics for each v parameter
	 * @param ds
	 * @param im
	 * @param edgeStruct
	 * @param i
	 * @param yprop
	 * @param suffstats
	 */
	public void GetNodeSuffStatsForV(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, double p) {
		LineClass2 lc = ds.data.get(i);

		// propose a change
		String y0 = lc.predLabel;
		lc.predLabel = yprop;

		int stateID = Integer.valueOf(labelDic.get(lc.predLabel)).intValue();

		String factorName = "";
		int index = -1;
		List<double[]> value = new ArrayList<double[]>();

		// Label
		factorName = "Label";
		index = im.GetIndexState(stateID);
		value = suffstats.v.get(factorName);
		double[] record = new double[] {index, p * 1.0};
		value.add(record);
		suffstats.v.put(factorName, value);

		// Label_Token
		factorName = "Label_Token";
		for (int j = 0; j < lc.wordIDList.size(); j++) {
			int wordID = Integer.valueOf(lc.wordIDList.get(j)).intValue();
			double count = lc.countIDList.get(j);
			if (count == 0) continue;

			index = im.GetIndexStateToken(stateID, wordID);
			value = suffstats.v.get(factorName);
			record = new double[] {index, p * (double) count};
			value.add(record);
			suffstats.v.put(factorName, value);
		}
		
		// Label_Feature
		factorName = "Label_Features";
		for (int j = 0; j < lc.features.length; j++) {
			double count = lc.features[j];
			if (count == 0) continue;

			index = im.GetIndexStateFeatures(stateID, j);
			value = suffstats.v.get(factorName);
			record = new double[] {index, p * count};
			value.add(record);
			suffstats.v.put(factorName, value);
		}

		// undo the change
		lc.predLabel = y0;
	}

	/**
	 * [Decoding] Gather edge sufficient statistics for each v parameter
	 * @param ds
	 * @param i
	 * @param yprop
	 * @param suffstats
	 * @param edgeStruct
	 * @param p
	 */
	public void GetEdgeSuffStatsForV(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, EdgeStructClass edgeStruct, double p) {
		LineClass2 lc = ds.data.get(i);

		// propose a change
		String y0 = lc.predLabel;
		lc.predLabel = yprop;

		int stateID = Integer.valueOf(labelDic.get(lc.predLabel)).intValue();

		String factorName = "";
		int index = -1;
		List<double[]> value = new ArrayList<double[]>();

		// Label_Label
		factorName = "Label_Label";
		int[] V = edgeStruct.V;
		int[] E = edgeStruct.E;
		List<int[]> edgeEnds = edgeStruct.edgeEnds;
		int[] edges = GetEdges(E, V[i], V[i+1]-1); // get edges for node i

		// get the list of neighbor nodes' stateIDs
		for (int j = 0; j < edges.length; j++) {
			int e = edges[j];
			int n1 = edgeEnds.get(e)[0]; // edge e's node1 ID 
			int n2 = edgeEnds.get(e)[1]; // edge e's node2 ID

			if (n1 == i) {
				LineClass2 other = ds.data.get(n2);
				int nextStateID = Integer.valueOf(labelDic.get(other.predLabel)).intValue();

				if (nextStateID > -1 && stateID > -1) {
					index = im.GetIndexStateState(stateID, nextStateID); 
					value = suffstats.v.get(factorName);
					double[] record = new double[] {index, p * 1.0}; 
					value.add(record);
					suffstats.v.put(factorName, value);
				}
			}
			else {
				LineClass2 other = ds.data.get(n1);
				int prevStateID = Integer.valueOf(labelDic.get(other.predLabel)).intValue();

				if (prevStateID > -1 && stateID > -1) {
					index = im.GetIndexStateState(prevStateID, stateID); 
					value = suffstats.v.get(factorName);
					double[] record = new double[] {index, p * 1.0}; 
					value.add(record);
					suffstats.v.put(factorName, value);
				}
			}
		}

		// undo the change
		lc.predLabel = y0;
	}

	/**
	 * [Training] Gather edge sufficient statistics for each v parameter
	 * @param ds
	 * @param n1
	 * @param n2
	 * @param suffstats
	 * @param p
	 */
	public void GetEdgeSuffStatsForV(dataStructure ds, int n1, int n2, String yprop1, String yprop2, SuffStatsClass suffstats, double p) {
		String factorName = "";
		int index = -1;
		List<double[]> value = new ArrayList<double[]>();

		List<LineClass2> variables = ds.data;
		LineClass2 lc1 = variables.get(n1);
		LineClass2 lc2 = variables.get(n2);

		// propose a change
		String y10 = lc1.predLabel;
		lc1.predLabel = yprop1;
		String y20 = lc2.predLabel;
		lc2.predLabel = yprop2;

		int stateID1 = Integer.valueOf(labelDic.get(lc1.predLabel));
		int stateID2 = Integer.valueOf(labelDic.get(lc2.predLabel));

		// label_label
		factorName = "Label_Label";
		index = im.GetIndexStateState(stateID1, stateID2);

		value = suffstats.v.get(factorName);
		double[] record = new double[] {index, p * 1.0};
		value.add(record);
		suffstats.v.put(factorName, value);

		// undo the change
		lc1.predLabel = y10;
		lc2.predLabel = y20;
	}

	/**
	 * [Decoding, Training] Gather node sufficient statistics for each w parameter
	 * @param ds
	 * @param i
	 * @param yprop
	 * @param suffstats_f
	 * @param suffstats_g
	 * @param obsList
	 * @param cc
	 * @param q
	 */
	public void GetNodeSuffStatsForW(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, metaParaClass mpc, double p, double q, String option) {
		List<LineClass2> variables = ds.data;

		LineClass2 lc1 = variables.get(i);
		ObsClass obs1 = obsList.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();

		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
			
			/*String id = lc1.entityID + " " + lc1.pageID + " " + lc1.lineID;
			if (id.equals("79 8 2")) {
				System.out.println(yprop + ": " + (Math.log(p) - mpc.eps.get(factorName)));
			}*/
			
			// do filtering
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!labels.contains(yprop)) continue; // the constraint must involve yprop
			
			if (cc.ConsTypeIndex.get(factorName).equals("y")) {
				if (option.equals("training")) {
					if (Math.log(p) < mpc.eps.get(factorName)) continue; // the probability of being yprop must exceed the constraint threshold
				}
			}

			cfc.GetNodeFeatureSignal(factorName, lc1, obs1, variables, obsList, i);

			double f_value = suffstats.f.get(factorName) + (cfc.f_flag ? (q * 1.0) : 0);
			suffstats.f.put(factorName, f_value);
			double g_value = suffstats.g.get(factorName) + (cfc.g_flag ? (q * 1.0) : 0);
			suffstats.g.put(factorName, g_value);

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y0;
	}

	/**
	 * [Training] Gather node sufficient statistics for each w parameter w.r.t. y-type constraints
	 * @param ds
	 * @param i
	 * @param yprop: target label
	 * @param blanketedges: node i's Markov blanket edges, each edge has node i and a node i's neighbor
	 * @param suffstats
	 * @param obsList
	 * @param cc
	 * @param mpc
	 * @param p: node i's probability to have label yprop, i.e., P_theta(yi = yprop|X)
	 * @param q: node i's probability to have label yprop, i.e., q(yi = yprop|X)
	 * @param p_edges: probabilities for node i's Markov blanket edges, i.e., each edge is a nstates*nstates matrix indicating the probability values P_theta(vi=state1, vj=state2)
	 * @param q_edges: probabilities for node i's Markov blanket edges, i.e., each edge is a nstates*nstates matrix indicating the probability values q(vi=state1, vj=state2)
	 * @param Eqzeta: each constraint has an expectation value for zeta(X,Y)
	 */
	public void GetNodeSuffStatsForV_Ytype_alpha2(dataStructure ds, int i, String yprop, List<int[]> blanketedges, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, metaParaClass mpc, double p, double q, List<double[]> p_edges, List<double[]> q_edges, Map<String, Double> Eqzeta) {
		List<LineClass2> variables = ds.data;

		LineClass2 lc1 = variables.get(i);
		ObsClass obs1 = obsList.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();
		int nStates = labelDic.size();

		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			// do filtering
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!labels.contains(yprop)) continue; // the constraint must involve yprop  
			
			if (Math.log(p) < mpc.eps.get(factorName)) continue; // the probability of being yprop must exceed the constraint threshold 
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));
			double sigmaterm = sigma * (1-sigma) * mpc.rho;

			double b = mpc.b.get(factorName);
			double e = Eqzeta.get(factorName);
			
			if (e == 0) {
				cfc.Clear();
				continue; // because all the below update becomes zero
			}

			cfc.GetNodeFeatureSignal(factorName, lc1, obs1, variables, obsList, i);

			// we definitely have value for f_value and g_value
			double f_value = cfc.f_flag ? 1.0 : 0;
			double g_value = cfc.g_flag ? 1.0 : 0;

			int index = -1;
			List<double[]> value = new ArrayList<double[]>();
			int stateID = Integer.valueOf(labelDic.get(lc1.predLabel)).intValue();

			// Label
			factorName = "Label";
			index = im.GetIndexState(stateID);
			value = suffstats.v.get(factorName);
			double[] record = new double[] {index, e * q * (f_value - b * g_value) * sigmaterm * (1 - p) * 1.0};
			value.add(record);
			suffstats.v.put(factorName, value);

			// Label_Token
			factorName = "Label_Token";
			for (int j = 0; j < lc1.wordIDList.size(); j++) {
				int wordID = Integer.valueOf(lc1.wordIDList.get(j)).intValue();
				double count = lc1.countIDList.get(j);
				if (count == 0) continue;

				index = im.GetIndexStateToken(stateID, wordID);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * q * (f_value - b * g_value) * sigmaterm * (1 - p) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			
			// Label_Feature
			factorName = "Label_Features";
			for (int j = 0; j < lc1.features.length; j++) {
				double count = lc1.features[j];
				if (count == 0) continue;

				index = im.GetIndexStateFeatures(stateID, j);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * q * (f_value - b * g_value) * sigmaterm * (1 - p) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}

			// for edge features h: we need to sum up all the values for each node i's neighbor
			factorName = "Label_Label";
			for (int j = 0; j < blanketedges.size(); j++) {
				// for each blanket edge 
				int[] edge = blanketedges.get(j);
				double[] p_edgeBel = p_edges.get(j);
				double[] q_edgeBel = q_edges.get(j);

				for (int l = 0; l < nStates; l++) {
					if (i == edge[0]) 
						index = im.GetIndexStateState(stateID, l);
					else if (i == edge[1]) 
						index = im.GetIndexStateState(l, stateID);
					else 
						System.err.println("error in getting the Markov blanket");

					value = suffstats.v.get(factorName);
					record = new double[] {index, e * q_edgeBel[l] * (f_value - b * g_value) * sigmaterm * (1 - p_edgeBel[l]) * 1.0};
					value.add(record);
					suffstats.v.put(factorName, value);
				}
			}

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y0;
	}
	
	/**
	 * [Training] Gather node sufficient statistics for each w parameter w.r.t. y-type constraints
	 * @param ds
	 * @param i
	 * @param yprop: target label
	 * @param blanketedges: node i's Markov blanket edges, each edge has node i and a node i's neighbor
	 * @param suffstats
	 * @param obsList
	 * @param cc
	 * @param mpc
	 * @param p: node i's probability to have label yprop, i.e., P_theta(yi = yprop|X)
	 * @param p_edges: probabilities for node i's Markov blanket edges, i.e., each edge is a nstates*nstates matrix indicating the probability values P_theta(vi=state1, vj=state2)
	 * @param pi: each constraint has a value for E[pi_c] - pi_c
	 */
	public void GetNodeSuffStatsForV_Ytype_alpha3(dataStructure ds, int i, String yprop, List<int[]> blanketedges, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, metaParaClass mpc, double p, List<double[]> p_edges, Map<String, Double> pi) {
		List<LineClass2> variables = ds.data;

		LineClass2 lc1 = variables.get(i);
		ObsClass obs1 = obsList.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();
		int nStates = labelDic.size();

		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			// do filtering
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!labels.contains(yprop)) continue; // the constraint must involve yprop
			
			if (Math.log(p) < mpc.eps.get(factorName)) continue; // the probability of being yprop must exceed the constraint threshold 
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));
			double sigmaterm = sigma * (1-sigma) * mpc.rho;

			double e = pi.get(factorName);
			
			if (e == 0) {
				cfc.Clear();
				continue;
			}

			cfc.GetNodeFeatureSignal(factorName, lc1, obs1, variables, obsList, i);

			// we definitely have value for g_value
			double g_value = cfc.g_flag ? 1.0 : 0;
			if (g_value == 0) continue; // if it is not constraint related

			int index = -1;
			List<double[]> value = new ArrayList<double[]>();
			int stateID = Integer.valueOf(labelDic.get(lc1.predLabel)).intValue();

			// Label
			factorName = "Label";
			index = im.GetIndexState(stateID);
			value = suffstats.v.get(factorName);
			double[] record = new double[] {index, e * g_value * sigmaterm * (1 - p) * 1.0};
			value.add(record);
			suffstats.v.put(factorName, value);

			// Label_Token
			factorName = "Label_Token";
			for (int j = 0; j < lc1.wordIDList.size(); j++) {
				int wordID = Integer.valueOf(lc1.wordIDList.get(j)).intValue();
				double count = lc1.countIDList.get(j);
				if (count == 0) continue;

				index = im.GetIndexStateToken(stateID, wordID);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * g_value * sigmaterm * (1 - p) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			
			// Label_Feature
			factorName = "Label_Features";
			for (int j = 0; j < lc1.features.length; j++) {
				double count = lc1.features[j];
				if (count == 0) continue;

				index = im.GetIndexStateFeatures(stateID, j);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * g_value * sigmaterm * (1 - p) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}

			// for edge features h: we need to sum up all the values for each node i's neighbor
			factorName = "Label_Label";
			for (int j = 0; j < blanketedges.size(); j++) {
				// for each blanket edge 
				int[] edge = blanketedges.get(j);
				double[] p_edgeBel = p_edges.get(j);

				for (int l = 0; l < nStates; l++) {
					if (i == edge[0]) 
						index = im.GetIndexStateState(stateID, l);
					else if (i == edge[1]) 
						index = im.GetIndexStateState(l, stateID);
					else 
						System.err.println("error in getting the Markov blanket");

					value = suffstats.v.get(factorName);
					record = new double[] {index, e * g_value * sigmaterm * (1 - p_edgeBel[l]) * 1.0};
					value.add(record);
					suffstats.v.put(factorName, value);
				}
			}

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y0;
	}
	

	public void GetNodeSuffStats_Ytype_MetaCons(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, metaParaClass mpc, double p) {
		List<LineClass2> variables = ds.data;

		LineClass2 lc1 = variables.get(i);
		ObsClass obs1 = obsList.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();

		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			// do filtering
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!labels.contains(yprop)) continue; // the constraint must involve yprop  
			
			if (Math.log(p) < mpc.eps.get(factorName)) continue; // the probability of being yprop must exceed the constraint threshold 
			
			// compute simga
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));

			cfc.GetNodeFeatureSignal(factorName, lc1, obs1, variables, obsList, i);

			// we definitely have value for g_value
			double g_value = cfc.g_flag ? 1.0 : 0;
			if (g_value == 0) continue; // if it is not constraint related
			
			double value = suffstats.w.get(factorName) + g_value * sigma; 
			suffstats.w.put(factorName, value);

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y0;
	}

	/**
	 * [Training] Gather edge sufficient statistics for each w parameter w.r.t. y-type constraints
	 * @param ds
	 * @param n1
	 * @param n2
	 * @param yprop1
	 * @param yprop2
	 * @param blanketedges
	 * @param suffstats
	 * @param obsList
	 * @param cc
	 * @param mpc
	 * @param p
	 * @param q
	 * @param p_nodes
	 * @param q_nodes
	 * @param p_edges
	 * @param q_edges
	 * @param Eqzeta
	 * @param indexp: if indexp = -1, then it means this edge (n1,n2) is not the general feature
	 */
	public void GetEdgeSuffStatsForV_Ytype_alpha2(dataStructure ds, int n1, int n2, String yprop1, String yprop2, List<int[]> blanketedges, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, List<String> consList, metaParaClass mpc, double p, double q, double[] p_nodes, double[] q_nodes, List<double[]> p_edges, List<double[]> q_edges, Map<String, Double> Eqzeta, int indexp) {
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
		
		int stateID1 = Integer.valueOf(labelDic.get(lc1.predLabel));
		int stateID2 = Integer.valueOf(labelDic.get(lc2.predLabel));

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();
		int nStates = labelDic.size();

		// get related constraint list
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!(labels.contains(yprop1) && labels.contains(yprop2))) continue; // the constraint must involve yprop1 and yprop2 at the same time
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));
			double sigmaterm = sigma * (1-sigma) * mpc.rho;
			
			double b = mpc.b.get(factorName);
			double e = Eqzeta.get(factorName);

			cfc.GetEdgeFeatureSignal(factorName, lc1, lc2, obs1, obs2, ds, obsList);

			// we definitely have value for f_value and g_value
			double f_value = cfc.f_flag ? 1.0 : 0;
			double g_value = cfc.g_flag ? 1.0 : 0;

			int index = -1;
			List<double[]> value = new ArrayList<double[]>();

			// Label
			factorName = "Label";
			value = suffstats.v.get(factorName);
			index = im.GetIndexState(stateID1);
			double[] record = new double[] {index, e * q_nodes[0] * (f_value - b * g_value) * sigmaterm * (1 - p_nodes[0]) * 1.0};
			value.add(record);
			index = im.GetIndexState(stateID2);
			record = new double[] {index, e * q_nodes[1] * (f_value - b * g_value) * sigmaterm * (1 - p_nodes[1]) * 1.0};
			value.add(record);
			suffstats.v.put(factorName, value);

			// Label_Token
			factorName = "Label_Token";
			for (int j = 0; j < lc1.wordIDList.size(); j++) {
				int wordID = Integer.valueOf(lc1.wordIDList.get(j)).intValue();
				double count = lc1.countIDList.get(j);
				
				index = im.GetIndexStateToken(stateID1, wordID);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * q_nodes[0] * (f_value - b * g_value) * sigmaterm * (1 - p_nodes[0]) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			for (int j = 0; j < lc2.wordIDList.size(); j++) {
				int wordID = Integer.valueOf(lc2.wordIDList.get(j)).intValue();
				double count = lc2.countIDList.get(j);
				
				index = im.GetIndexStateToken(stateID2, wordID);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * q_nodes[1] * (f_value - b * g_value) * sigmaterm * (1 - p_nodes[1]) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			
			// Label_Feature
			factorName = "Label_Features";
			for (int j = 0; j < lc1.features.length; j++) {
				double count = lc1.features[j];
				if (count == 0) continue;

				index = im.GetIndexStateFeatures(stateID1, j);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * q_nodes[0] * (f_value - b * g_value) * sigmaterm * (1 - p_nodes[0]) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			for (int j = 0; j < lc2.features.length; j++) {
				double count = lc2.features[j];
				if (count == 0) continue;

				index = im.GetIndexStateFeatures(stateID2, j);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * q_nodes[1] * (f_value - b * g_value) * sigmaterm * (1 - p_nodes[1]) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}

			// for edge features h
			factorName = "Label_Label";
			if (indexp > -1) {
				// (n1,n2) exists in the edgeStruct 
				index = im.GetIndexStateState(stateID1, stateID2);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * q * (f_value - b * g_value) * sigmaterm * (1 - p) * 1.0};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			
			// for each Markov blanket edge with either n1 or n2
			for (int j = 0; j < blanketedges.size(); j++) {
				int[] edge = blanketedges.get(j);
				double[] p_edgeBel = p_edges.get(j);
				double[] q_edgeBel = q_edges.get(j);

				// sum up all the possibilities for each Markov blanket edge's states except n1 and n2
				for (int l = 0; l < nStates; l++) {
					if (n1 == edge[0]) 
						index = im.GetIndexStateState(stateID1, l);
					else if (n1 == edge[1]) 
						index = im.GetIndexStateState(l, stateID1);
					else if (n2 == edge[0]) 
						index = im.GetIndexStateState(stateID2, l);
					else if (n2 == edge[1]) 
						index = im.GetIndexStateState(l, stateID2);
					else 
						System.err.println("error in getting the Markov blanket");

					value = suffstats.v.get(factorName);
					record = new double[] {index, e * q_edgeBel[l] * (f_value - b * g_value) * sigmaterm * (1 - p_edgeBel[l]) * 1.0};
					value.add(record);
					suffstats.v.put(factorName, value);
				}
			}

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y10;
		lc2.predLabel = y20;
	}
	
	/**
	 * [Training] Gather edge sufficient statistics for each w parameter w.r.t. y-type constraints
	 * @param ds
	 * @param n1
	 * @param n2
	 * @param yprop1
	 * @param yprop2
	 * @param blanketedges
	 * @param suffstats
	 * @param obsList
	 * @param cc
	 * @param mpc
	 * @param p
	 * @param p_nodes
	 * @param p_edges
	 * @param pi
	 * @param indexp: if indexp = -1, then it means this edge (n1,n2) is not the general feature
	 */
	public void GetEdgeSuffStatsForV_Ytype_alpha3(dataStructure ds, int n1, int n2, String yprop1, String yprop2, List<int[]> blanketedges, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, List<String> consList, metaParaClass mpc, double p, double[] p_nodes, List<double[]> p_edges, Map<String, Double> pi, int indexp) {
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
		
		int stateID1 = Integer.valueOf(labelDic.get(lc1.predLabel));
		int stateID2 = Integer.valueOf(labelDic.get(lc2.predLabel));

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();
		int nStates = labelDic.size();

		// get related constraint list
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!(labels.contains(yprop1) && labels.contains(yprop2))) continue; // the constraint must involve yprop1 and yprop2 at the same time  
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));
			double sigmaterm = sigma * (1-sigma) * mpc.rho;
			
			double e = pi.get(factorName);

			cfc.GetEdgeFeatureSignal(factorName, lc1, lc2, obs1, obs2, ds, obsList);

			// we definitely have value for f_value and g_value
			double g_value = cfc.g_flag ? 1.0 : 0;
			if (g_value == 0) continue;

			int index = -1;
			List<double[]> value = new ArrayList<double[]>();

			// Label
			factorName = "Label";
			value = suffstats.v.get(factorName);
			index = im.GetIndexState(stateID1);
			double[] record = new double[] {index, e * g_value * sigmaterm * (1 - p_nodes[0]) * 1.0};
			value.add(record);
			index = im.GetIndexState(stateID2);
			record = new double[] {index, e * g_value * sigmaterm * (1 - p_nodes[1]) * 1.0};
			value.add(record);
			suffstats.v.put(factorName, value);

			// Label_Token
			factorName = "Label_Token";
			for (int j = 0; j < lc1.wordIDList.size(); j++) {
				int wordID = Integer.valueOf(lc1.wordIDList.get(j)).intValue();
				double count = lc1.countIDList.get(j);
				
				index = im.GetIndexStateToken(stateID1, wordID);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * g_value * sigmaterm * (1 - p_nodes[0]) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			for (int j = 0; j < lc2.wordIDList.size(); j++) {
				int wordID = Integer.valueOf(lc2.wordIDList.get(j)).intValue();
				double count = lc2.countIDList.get(j);
				
				index = im.GetIndexStateToken(stateID2, wordID);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * g_value * sigmaterm * (1 - p_nodes[1]) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			
			// Label_Feature
			factorName = "Label_Features";
			for (int j = 0; j < lc1.features.length; j++) {
				double count = lc1.features[j];
				if (count == 0) continue;

				index = im.GetIndexStateFeatures(stateID1, j);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * g_value * sigmaterm * (1 - p_nodes[0]) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			for (int j = 0; j < lc2.features.length; j++) {
				double count = lc2.features[j];
				if (count == 0) continue;

				index = im.GetIndexStateFeatures(stateID2, j);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * g_value * sigmaterm * (1 - p_nodes[1]) * count};
				value.add(record);
				suffstats.v.put(factorName, value);
			}

			// for edge features h
			factorName = "Label_Label";
			if (indexp > -1) {
				// (n1,n2) exists in the edgeStruct 
				index = im.GetIndexStateState(stateID1, stateID2);
				value = suffstats.v.get(factorName);
				record = new double[] {index, e * g_value * sigmaterm * (1 - p) * 1.0};
				value.add(record);
				suffstats.v.put(factorName, value);
			}
			
			// for each Markov blanket edge with either n1 or n2
			for (int j = 0; j < blanketedges.size(); j++) {
				int[] edge = blanketedges.get(j);
				double[] p_edgeBel = p_edges.get(j);

				// sum up all the possibilities for each Markov blanket edge's states except n1 and n2
				for (int l = 0; l < nStates; l++) {
					if (n1 == edge[0]) 
						index = im.GetIndexStateState(stateID1, l);
					else if (n1 == edge[1]) 
						index = im.GetIndexStateState(l, stateID1);
					else if (n2 == edge[0]) 
						index = im.GetIndexStateState(stateID2, l);
					else if (n2 == edge[1]) 
						index = im.GetIndexStateState(l, stateID2);
					else 
						System.err.println("error in getting the Markov blanket");

					value = suffstats.v.get(factorName);
					record = new double[] {index, e * g_value * sigmaterm * (1 - p_edgeBel[l]) * 1.0};
					value.add(record);
					suffstats.v.put(factorName, value);
				}
			}

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y10;
		lc2.predLabel = y20;
	}
	
	
	public void GetEdgeSuffStats_Ytype_MetaCons(dataStructure ds, int n1, int n2, String yprop1, String yprop2, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, List<String> consList, metaParaClass mpc, double p) {
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

		// get related constraint list
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!(labels.contains(yprop1) && labels.contains(yprop2))) continue; // the constraint must involve yprop1 and yprop2 at the same time  
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));

			cfc.GetEdgeFeatureSignal(factorName, lc1, lc2, obs1, obs2, ds, obsList);

			// we definitely have value for f_value and g_value
			double g_value = cfc.g_flag ? 1.0 : 0;
			if (g_value == 0) continue;

			double value = suffstats.w.get(factorName) + g_value * sigma;
			suffstats.w.put(factorName, value);

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y10;
		lc2.predLabel = y20;
	}
	
	/**
	 * [Training] Gather node sufficient statistics for each u parameter w.r.t. y-type constraints
	 * @param ds
	 * @param i
	 * @param yprop
	 * @param suffstats
	 * @param obsList
	 * @param cc
	 * @param mpc
	 * @param p
	 * @param q
	 * @param Eqzeta
	 */
	public void GetNodeSuffStatsForU_Ytype_alpha2(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, metaParaClass mpc, double p, double q, Map<String, Double> Eqzeta) {
		List<LineClass2> variables = ds.data;

		LineClass2 lc1 = variables.get(i);
		ObsClass obs1 = obsList.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();

		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			// do filtering
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!labels.contains(yprop)) continue; // the constraint must involve yprop  
			
			if (Math.log(p) < mpc.eps.get(factorName)) continue; // the probability of being yprop must exceed the constraint threshold 
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));
			double sigmaterm = sigma * (1-sigma) * mpc.rho;

			double b = mpc.b.get(factorName);
			double e = Eqzeta.get(factorName);

			cfc.GetNodeFeatureSignal(factorName, lc1, obs1, variables, obsList, i);

			// we definitely have value for f_value and g_value
			double f_value = cfc.f_flag ? 1.0 : 0;
			double g_value = cfc.g_flag ? 1.0 : 0;
			
			double value = suffstats.u.get(factorName) + e * q * (f_value - b * g_value) * sigmaterm * (-1.0);
			suffstats.u.put(factorName, value);

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y0;
	}
	
	/**
	 * [Training] Gather node sufficient statistics for each u parameter w.r.t. y-type constraints
	 * @param ds
	 * @param i
	 * @param yprop
	 * @param suffstats
	 * @param obsList
	 * @param cc
	 * @param mpc
	 * @param p
	 * @param pi
	 */
	public void GetNodeSuffStatsForU_Ytype_alpha3(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, metaParaClass mpc, double p, Map<String, Double> piterm) {
		List<LineClass2> variables = ds.data;

		LineClass2 lc1 = variables.get(i);
		ObsClass obs1 = obsList.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();

		// get related constraint list
		List<String> consList = cc.constraints;
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("node")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			// do filtering
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!labels.contains(yprop)) continue; // the constraint must involve yprop  
			if (Math.log(p) < mpc.eps.get(factorName)) continue; // the probability of being yprop must exceed the constraint threshold 
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));
			double sigmaterm = sigma * (1-sigma) * mpc.rho;

			double e = piterm.get(factorName);

			cfc.GetNodeFeatureSignal(factorName, lc1, obs1, variables, obsList, i);

			// we definitely have value for f_value and g_value
			double g_value = cfc.g_flag ? 1.0 : 0;
			if (g_value == 0) continue;
			
			double value = suffstats.u.get(factorName) + e * g_value * sigmaterm * (-1.0);
			suffstats.u.put(factorName, value);

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y0;
	}
	
	/**
	 * [Training] Gather edge sufficient statistics for each u parameter w.r.t. y-type constraints
	 * @param ds
	 * @param n1
	 * @param n2
	 * @param yprop1
	 * @param yprop2
	 * @param suffstats
	 * @param obsList
	 * @param cc
	 * @param mpc
	 * @param p
	 * @param q
	 * @param Eqzeta
	 */
	public void GetEdgeSuffStatsForU_Ytype_alpha2(dataStructure ds, int n1, int n2, String yprop1, String yprop2, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, List<String> consList, metaParaClass mpc, double p, double q, Map<String, Double> Eqzeta) {
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

		// get related constraint list
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!(labels.contains(yprop1) && labels.contains(yprop2))) continue; // the constraint must involve yprop1 and yprop2 at the same time  
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));
			double sigmaterm = sigma * (1-sigma) * mpc.rho;
			
			double b = mpc.b.get(factorName);
			double e = Eqzeta.get(factorName);

			cfc.GetEdgeFeatureSignal(factorName, lc1, lc2, obs1, obs2, ds, obsList);

			// we definitely have value for f_value and g_value
			double f_value = cfc.f_flag ? 1.0 : 0;
			double g_value = cfc.g_flag ? 1.0 : 0;
			
			double value = suffstats.u.get(factorName) + e * q * (f_value - b * g_value) * sigmaterm * (-1.0);
			suffstats.u.put(factorName, value);

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y10;
		lc2.predLabel = y20;
	}
	
	/**
	 * [Training] Gather edge sufficient statistics for each u parameter w.r.t. y-type constraints
	 * @param ds
	 * @param n1
	 * @param n2
	 * @param yprop1
	 * @param yprop2
	 * @param suffstats
	 * @param obsList
	 * @param cc
	 * @param mpc
	 * @param p
	 * @param pi
	 */
	public void GetEdgeSuffStatsForU_Ytype_alpha3(dataStructure ds, int n1, int n2, String yprop1, String yprop2, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, List<String> consList, metaParaClass mpc, double p, Map<String, Double> piterm) {
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

		// get related constraint list
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!(labels.contains(yprop1) && labels.contains(yprop2))) continue; // the constraint must involve yprop1 and yprop2 at the same time
			
			// compute simga*(1-sigma)
			double sigma = 1 / (1 + Math.exp(- mpc.rho * (Math.log(p) - mpc.eps.get(factorName))));
			double sigmaterm = sigma * (1-sigma) * mpc.rho;
			
			double e = piterm.get(factorName);

			cfc.GetEdgeFeatureSignal(factorName, lc1, lc2, obs1, obs2, ds, obsList);

			// we definitely have value for f_value and g_value
			double g_value = cfc.g_flag ? 1.0 : 0;
			if (g_value == 0) continue;
			
			double value = suffstats.u.get(factorName) + e * g_value * sigmaterm * (-1.0);
			suffstats.u.put(factorName, value);

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y10;
		lc2.predLabel = y20;
	}

	/**
	 * [Decoding] Gather edge sufficient statistics for each w parameter
	 * @param ds
	 * @param i
	 * @param yprop
	 * @param suffstats
	 * @param edgeStruct
	 * @param p
	 */
	public void GetEdgeSuffStatsForW(dataStructure ds, int i, String yprop, SuffStatsClass suffstats, EdgeStructClass edgeStructCons, List<ObsClass> obsList, InvertedIndexClass iic, ConstraintClass cc, double q) {
		List<LineClass2> variables = ds.data;

		LineClass2 lc1 = variables.get(i);
		ObsClass obs1 = obsList.get(i);

		// propose a change
		String y0 = lc1.predLabel;
		lc1.predLabel = yprop;

		ConstraintFeatureClass cfc = new ConstraintFeatureClass();

		// Label_Label
		int[] V = edgeStructCons.V;
		int[] E = edgeStructCons.E;
		List<int[]> edgeEnds = edgeStructCons.edgeEnds;
		int[] edges = GetEdges(E, V[i], V[i+1]-1); // get edges for node i
		
		/*String id = lc1.entityID + " " + lc1.pageID + " " + lc1.lineID;
		if (id.equals("9 2 5") && (yprop.equals("StrungWeight") || yprop.equals("UnstrungWeight"))) {
			System.out.println(yprop + ": " + edges.length);
		}*/

		// get the list of neighbor nodes' stateIDs
		for (int j = 0; j < edges.length; j++) {
			int e = edges[j];
			int n1 = edgeEnds.get(e)[0]; // edge e's node1 ID 
			int n2 = edgeEnds.get(e)[1]; // edge e's node2 ID
			String edgeID = n1 + "_" + n2;
			
			// for each constraint edge, only compute gradient with regards to its corresponding constraints 
			List<String> consList = edgeStructCons.EdgeToConstraints.get(edgeID);

			for (int k = 0; k < consList.size(); k++) {
				String factorName = consList.get(k);
				if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;

				int l = (n1 == i) ? n2 : n1;
				LineClass2 lc2 = variables.get(l);
				ObsClass obs2 = obsList.get(l);

				cfc.GetEdgeFeatureSignal(factorName, lc1, lc2, obs1, obs2, ds, obsList);

				double f_value = suffstats.f.get(factorName) + (cfc.f_flag ? (q * 1.0) : 0);
				suffstats.f.put(factorName, f_value);
				double g_value = suffstats.g.get(factorName) + (cfc.g_flag ? (q * 1.0) : 0);
				suffstats.g.put(factorName, g_value);

				cfc.Clear();
			}
		}

		// undo the change
		lc1.predLabel = y0;
	}

	/**
	 * [Training] Gather edge sufficient statistics for each w parameter
	 * @param ds
	 * @param n1
	 * @param n2
	 * @param yprop1
	 * @param yprop2
	 * @param suffstats_f
	 * @param suffstats_g
	 * @param obsList
	 * @param cc
	 * @param q
	 */
	public void GetEdgeSuffStatsForW(dataStructure ds, int n1, int n2, String yprop1, String yprop2, SuffStatsClass suffstats, List<ObsClass> obsList, ConstraintClass cc, List<String> consList, double q) {
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

		// get related constraint list
		for (int k = 0; k < consList.size(); k++) {
			String factorName = consList.get(k);
			
			if (!cc.ConsNodeOrEdgeIndex.get(factorName).equals("edge")) continue;
			
			// do filtering, but no need to check constraint threshold anymore because we already prune
			Set<String> labels = new HashSet<String>(cc.ConsLabelIndex.get(factorName));
			if (!(labels.contains(yprop1) && labels.contains(yprop2))) continue; // the constraint must involve yprop1 and yprop2 at the same time

			cfc.GetEdgeFeatureSignal(factorName, lc1, lc2, obs1, obs2, ds, obsList);

			double f_value = suffstats.f.get(factorName) + (cfc.f_flag ? (q * 1.0) : 0);
			suffstats.f.put(factorName, f_value);
			double g_value = suffstats.g.get(factorName) + (cfc.g_flag ? (q * 1.0) : 0);
			suffstats.g.put(factorName, g_value);

			cfc.Clear();
		}

		// undo the change
		lc1.predLabel = y10;
		lc2.predLabel = y20;
	}

	private int[] GetEdges(int[] E, int s1, int s2) {
		int[] edges = new int[s2 - s1 + 1];
		for (int i = 0; i < s2-s1+1; i++)
			edges[i] = E[s1 + i];
		return edges;
	}
}


