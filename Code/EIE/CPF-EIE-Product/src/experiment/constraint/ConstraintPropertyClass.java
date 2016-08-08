package experiment.constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.graph.dataStructure;
import experiment.inference.ConstraintGraphClass;
import experiment.inference.EdgeStructClass;
import experiment.inference.IndexMap;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;
import experiment.suffstats.ComputeSufficientStats;
import experiment.suffstats.SuffStatsClass;

public class ConstraintPropertyClass {
	public Map<String, Double> pi;
	public Map<String, Map<String, Double>> m_unlabeled;
	public Map<String, Double> b;

	public ConstraintPropertyClass() {
		this.pi = new HashMap<String, Double>();
		this.m_unlabeled = new HashMap<String, Map<String, Double>>(); // <entity, <factorName, count>> 
		this.b = new HashMap<String, Double>();
	}

	public void Process(EntityDataClass edc, EntityObsClass eoc, GetDataDomain gdd, ConstraintClass cc, metaParaClass mpc) {
		for (String factorName : cc.constraints) {
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;
			
			mpc.eps.put(factorName, Math.log(0.3)); // put a default value 0.2
		}

		ProcessLabeled(edc, eoc, gdd, cc, mpc);
		ProcessUnlabeled(edc, eoc, gdd, cc, mpc);

		mpc.pi = this.pi;
		mpc.m_unlabeled = this.m_unlabeled;
		mpc.b = this.b;
	}

	private void ProcessLabeled(EntityDataClass edc, EntityObsClass eoc, GetDataDomain gdd, ConstraintClass cc, metaParaClass mpc) {
		// Construct the mapping
		int nWords = gdd.wordDic.size();
		int nStates = gdd.labelDic.size();
		int nFeatures = gdd.nFeatures;
		IndexMap im = new IndexMap(nStates, nWords, nFeatures);

		// Initialize the sufficient statistics
		ParaClass param = new ParaClass(cc);

		// Count the micro confidence regardless of entity
		ComputeSufficientStats css = new ComputeSufficientStats(gdd.labelDic, im);

		Map<String, Double> count = new HashMap<String, Double>();
		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();

			SuffStatsClass suffstatsRel = new SuffStatsClass();
			suffstatsRel.Initialize(param);
			SuffStatsClass suffstatsTotal = new SuffStatsClass();
			suffstatsTotal.Initialize(param);

			// get data
			String indicator = edc.entityIndicator.get(entity);
			if (!indicator.equalsIgnoreCase("labeled")) 
				continue;

			dataStructure ds = e.getValue();
			List<ObsClass> obsList = eoc.EntityObsList.get(entity);
			Count(ds, css, obsList, cc, suffstatsRel, suffstatsTotal, gdd.labelDic, mpc);

			for (String factorName : cc.constraints) {
				if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;

				double rel = suffstatsRel.g.get(factorName);
				double total = suffstatsTotal.g.get(factorName);

				if (total > 0) {
					if (!count.containsKey(factorName)) 
						count.put(factorName, 1.0);
					else
						count.put(factorName, count.get(factorName) + 1.0); // number of labeled entities with non-zero eligible instances
					double ratio = rel/total;

					if (!pi.containsKey(factorName)) 
						pi.put(factorName, ratio);
					else
						pi.put(factorName, pi.get(factorName) + ratio);
				} else {
					count.put(factorName, 1.0);
					pi.put(factorName, 0.0);
				}
			}
		}

		System.out.println("Constraint threshold prior: ");
		for (String factorName : cc.constraints) {
			if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;

			// take the mean across all the labeled entities
			double value = pi.get(factorName) / count.get(factorName);
			pi.put(factorName, value);

			System.out.println(factorName + "\t" + pi.get(factorName));
		}

		System.out.println("Constraint confidence: ");
		for (String factorName : cc.constraints) {
			double value = 0;

			if (factorName.equalsIgnoreCase("HeadSize_HasSize")) value = 0.7;
			if (factorName.equalsIgnoreCase("StrungWeight_StrungWeight_SameValue")) value = 0.9;
			if (factorName.equalsIgnoreCase("StrungWeight_Larger_UnstrungWeight")) value = 0.9;

			b.put(factorName, value);
			System.out.println(factorName + "\t" + value);
		}
	}

	private void ProcessUnlabeled(EntityDataClass edc, EntityObsClass eoc, GetDataDomain gdd, ConstraintClass cc, metaParaClass mpc) {
		// Construct the mapping
		int nWords = gdd.wordDic.size();
		int nStates = gdd.labelDic.size();
		int nFeatures = gdd.nFeatures;
		IndexMap im = new IndexMap(nStates, nWords, nFeatures);

		// Initialize the sufficient statistics
		ParaClass param = new ParaClass(cc);

		// Count the micro confidence regardless of entity
		ComputeSufficientStats css = new ComputeSufficientStats(gdd.labelDic, im);

		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();

			// get data
			String indicator = edc.entityIndicator.get(entity);
			if (!indicator.equalsIgnoreCase("unlabeled")) 
				continue;

			SuffStatsClass suffstatsTotal = new SuffStatsClass();
			suffstatsTotal.Initialize(param);

			dataStructure ds = e.getValue();
			List<ObsClass> obsList = eoc.EntityObsList.get(entity);
			Count(ds, css, obsList, cc, null, suffstatsTotal, gdd.labelDic, mpc);

			Map<String, Double> tmp = new HashMap<String, Double>();
			for (String factorName : cc.constraints) {
				if (!cc.ConsTypeIndex.get(factorName).equals("y")) continue;

				double total = suffstatsTotal.g.get(factorName);
				tmp.put(factorName,  total);
			}
			m_unlabeled.put(entity, tmp); // <entity, <factorName, count>>
		}
	}

	private void Count(dataStructure ds, ComputeSufficientStats css, List<ObsClass> obsList, ConstraintClass cc, SuffStatsClass suffstatsRel, SuffStatsClass suffstatsTotal, Map<String, String> labelDic, metaParaClass mpc) {
		int nNodes = ds.data.size();

		// Compute node constraints
		for (int j = 0; j < nNodes; j++) {
			// exclusively for labeled data
			if (suffstatsRel != null) {
				String yprop = ds.data.get(j).label;
				css.GetNodeSuffStatsForW(ds, j, yprop, suffstatsRel, obsList, cc, mpc, 1.0, 1.0, "training"); // p=1.0 to denote no pruning, q=1.0 to denote deterministic eligible instance counting
			}

			for (String ytmp : labelDic.keySet()) {
				css.GetNodeSuffStatsForW(ds, j, ytmp, suffstatsTotal, obsList, cc, mpc, 1.0, 1.0, "training"); // p=1.0 to denote no pruning, q=1.0 to denote deterministic eligible instance counting
			}
		}
		
		// Build the graph for constraint feature edge structure
		ConstraintGraphClass cgc = new ConstraintGraphClass();
		EdgeStructClass edgeStructCons = cgc.ConstructGraph(ds, obsList, labelDic, cc); // IMPORTANT!! without doing any pruning at this stage
		
		// Compute edge constraints
		int nEdges = edgeStructCons.nEdges;
		for (int i = 0; i < nEdges; i++) {
			int[] edge = edgeStructCons.edgeEnds.get(i);
			int n1 = edge[0];
			int n2 = edge[1];
			String edgeID = n1 + "_" + n2;
			
			List<String> consList = edgeStructCons.EdgeToConstraints.get(edgeID);
			
			// exclusively for labeled data
			if (suffstatsRel != null) {
				String yprop1 = ds.data.get(n1).label;
				String yprop2 = ds.data.get(n2).label;
				css.GetEdgeSuffStatsForW(ds, n1, n2, yprop1, yprop2, suffstatsRel, obsList, cc, consList, 1.0); // q=1.0 to denote deterministic eligible instance counting
			}

			for (String ytmp1 : labelDic.keySet()) {
				for (String ytmp2 : labelDic.keySet()) {
					css.GetEdgeSuffStatsForW(ds, n1, n2, ytmp1, ytmp2, suffstatsTotal, obsList, cc, consList, 1.0); // q=1.0 to denote deterministic eligible instance counting
				}
			}
		}
	}
}