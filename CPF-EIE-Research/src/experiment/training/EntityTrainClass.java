package experiment.training;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import experiment.constraint.ConstraintClass;
import experiment.constraint.EntityObsClass;
import experiment.constraint.ObsClass;
import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.graph.dataStructure;
import experiment.inference.EdgeStructClass;
import experiment.inference.EntityEdgeStructClass;
import experiment.inference.IndexMap;
import experiment.inference.InferClass;
import experiment.inference.MergedEdgeStructClass;
import experiment.parameter.ParaClass;
import experiment.parameter.ParaGeneratorClass;
import experiment.parameter.metaParaClass;

public class EntityTrainClass {
	public ParaClass param;
	public EntityTrainClass() {
		this.param = new ParaClass();
	}
	
	public void Train(EntityDataClass edc, EntityEdgeStructClass eesc, EntityObsClass eoc, GetDataDomain gdd, metaParaClass mpc, ConstraintClass cc, ParaGeneratorClass pgc, Map<String, InferClass> infermap) {
		// Construct the mapping
		int nWords = gdd.wordDic.size();
		int nStates = gdd.labelDic.size();
		int nFeatures = gdd.nFeatures;
		IndexMap im = new IndexMap(nStates, nWords, nFeatures);

		// Initialize model <ParaClass>, including v (for data) and w (for constraints)
		System.out.println("Initialize parameters...");
		param.v.putAll(pgc.v);
		param.w.putAll(pgc.w);
		
		Set<String> set = new HashSet<String>(cc.constraints);
		List<String> list = new ArrayList<String>();
		
		for (String factorName : param.w.keySet()) {
			if (!set.contains(factorName)) 
				list.add(factorName);
		}
		for (String factorName : list) {
			param.w.remove(factorName);
		}
		
		// Set y-type constraint threshold
		param.SetByThreshold(mpc.eps); // use eps to set the param.u, instead of the initial values
		
		// Get trainer
		TrainerClass model = new TrainerClass(gdd, im, mpc, param, cc); // param will get automatically updated
		
		System.err.println("Initially, v = " + param.GetTwoNormOnV() + "\t" + "w = " + param.GetTwoNormOnW());
		System.out.println("============Constraint confidence:==========");
		for (Entry<String, Double> e : mpc.b.entrySet()) {
			System.out.println(e.getKey() + "\t" + e.getValue());
		}
		System.out.println("============Constraint threshold prior:==========");
		for (Entry<String, Double> e : param.u.entrySet()) {
			System.out.println(e.getKey() + " = " + param.sigmoid(e.getValue()));
		}
		
		int Ne = edc.entityData.size();
		for (int iter = 0; iter < mpc.maxIter; iter++) {
			System.out.println("Training iteration " + iter + " ...");
			
			mpc.adjustStepLen(iter, 0.1);
			System.out.println("step length = " + mpc.stepLen);
			
			// Shuffle the entity data
			String[] entityArray = Shuffle(edc.entityData.keySet());

			// Stochastic gradient descent
			for (int eid = 0; eid < Ne; eid++) {
				//if (eid % 10 == 0)
				//	System.out.println("Have processed " + eid + " entities ...");

				// Sample one entity
				String entity = entityArray[eid];
				//System.out.println(entity);

				// Get that entity's data
				if (!edc.entityData.containsKey(entity))
					continue;
				dataStructure trainData = edc.entityData.get(entity);

				EdgeStructClass edgeStruct = eesc.entityEdgeStruct.get(entity); 
				EdgeStructClass edgeStructCons = eesc.entityEdgeStructCons.get(entity);
				MergedEdgeStructClass mesc = eesc.entityMergedEdgeStruct.get(entity);
				List<ObsClass> obsList = eoc.EntityObsList.get(entity);

				// Do entity-level stochastic gradient descent
				if (!edc.entityIndicator.containsKey(entity))
					continue;

				if (edc.entityIndicator.get(entity).equalsIgnoreCase("labeled"))
					model.TrainLabeled(trainData, edgeStruct, edgeStructCons, mesc, obsList); // currently let's consider only labeled
				else {
					Map<String, Double> m_cs = mpc.m_unlabeled.get(entity); // m_cs = <factorName, count>
					model.TrainUnlabeled(trainData, edgeStruct, edgeStructCons, mesc, obsList, m_cs); // if we want to consider unlabeled, we need to use decode, as labeled data do not help unlabeled data due to coming from different entities
				}
				
				// update threshold
				mpc.eps = model.ReturnThreshold();
			}
			
			System.out.println("v = " + param.GetTwoNormOnV() + ", " + "w = " + param.GetTwoNormOnW());
			for (Entry<String, Double> e : param.w.entrySet()) {
				System.out.println("w." + e.getKey() + " = " + param.w.get(e.getKey()));
			}
			for (Entry<String, Double> e : param.u.entrySet()) {
				System.out.println("u." + e.getKey() + " = " + param.sigmoid(e.getValue()));
			}
			
			// Update the graph structure, by pruning
			infermap = eesc.GenInferP(edc, gdd, param, mpc);
			eesc.ProcessC(edc, eoc, gdd, cc, mpc, infermap);
		}
	}
	
	private String[] Shuffle(Set<String> keySet) {
		String[] value = new String[keySet.size()];
		int n = 0;
		for (String key : keySet) {
			value[n] = key;
			n++;
		}

		/*Random r = new Random();
		for (int i = 0; i < value.length; i++) {
			int j = r.nextInt(value.length);
			String tmp = value[i];
			value[i] = value[j];
			value[j] = tmp;
		}*/
		return value;
	}
}
