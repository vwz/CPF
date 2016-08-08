package experiment.initialize;

import java.util.Random;
import java.util.Set;

import experiment.constraint.EntityObsClass;
import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.graph.dataStructure;
import experiment.inference.EdgeStructClass;
import experiment.inference.EntityEdgeStructClass;
import experiment.inference.IndexMap;
import experiment.parameter.ParaClass;
import experiment.parameter.ParaGeneratorClass;
import experiment.parameter.metaParaClass;
import experiment.training.TrainerClass;

public class Initializer {
	public ParaClass param;
	public Initializer() {
		this.param = new ParaClass();
	}
	
	public void Train(EntityDataClass edc, EntityEdgeStructClass eesc, EntityObsClass eoc, GetDataDomain gdd, metaParaClass mpc, ParaGeneratorClass pgc) {
		// Construct the mapping
		int nWords = gdd.wordDic.size();
		int nStates = gdd.labelDic.size();
		int nFeatures = gdd.nFeatures;
		IndexMap im = new IndexMap(nStates, nWords, nFeatures);

		// Initialize model <ParaClass>, including v (for data) and w (for constraints)
		System.out.print("Initialize parameters...");
		param.v.putAll(pgc.v);
		
		// Get trainer
		TrainerClass model = new TrainerClass(gdd, im, mpc, param, null); // param will get automatically updated
		
		System.err.println("Initially, v = " + param.GetTwoNormOnV());
		
		int Ne = edc.entityData.size();
		//for (int iter = 0; iter < mpc.maxIter; iter++) {
		for (int iter = 0; iter < 100; iter++) {  // to remove later
			System.out.println("Training iteration " + iter + " ...");
			
			mpc.adjustStepLen(iter, 1.0);
			System.out.println("step length = " + mpc.stepLen);
			
			// Shuffle the entity data
			String[] entityArray = Shuffle(edc.entityData.keySet());

			// Stochastic gradient descent
			for (int eid = 0; eid < Ne; eid++) {
				//System.out.println("Have processed " + eid + " entities ...");
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

				// Do entity-level stochastic gradient descent
				if (!edc.entityIndicator.containsKey(entity))
					continue;

				if (edc.entityIndicator.get(entity).equalsIgnoreCase("labeled"))
					model.TrainLabeled(trainData, edgeStruct); // currently let's consider only labeled
			}
			
			System.err.println("v = " + param.GetTwoNormOnV());
		}
		
		pgc.v.putAll(param.v); // update the parameters with the initial training results
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
