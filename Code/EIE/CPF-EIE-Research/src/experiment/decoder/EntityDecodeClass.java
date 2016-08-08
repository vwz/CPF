package experiment.decoder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;

public class EntityDecodeClass {
	public EntityDecodeClass() { }

	public Evaluator Decode(EntityDataClass edc, EntityEdgeStructClass eesc, EntityObsClass eoc, GetDataDomain gdd, ParaClass param, metaParaClass mpc, ConstraintClass cc, BufferedWriter bw, Map<String, InferClass> infermap) {
		// Construct the mapping
		int nWords = gdd.wordDic.size();
		int nStates = gdd.labelDic.size();
		int nFeatures = gdd.nFeatures;
		IndexMap im = new IndexMap(nStates, nWords, nFeatures);

		// Get decoder
		DecoderClass decoder = new DecoderClass(gdd, im, mpc, param, cc);

		// Get evaluator
		Evaluator evaluator = new Evaluator(gdd);

		// Decoder each test entity
		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();
			dataStructure testData = e.getValue();

			EdgeStructClass edgeStruct = eesc.entityEdgeStruct.get(entity); 
			EdgeStructClass edgeStructCons = eesc.entityEdgeStructCons.get(entity);
			List<ObsClass> obsList = eoc.EntityObsList.get(entity);

			InferClass inferP = infermap.get(entity);

			decoder.GibbsSampling(testData, obsList, edgeStruct, edgeStructCons, inferP);

			/** Get entity F1, i.e. regardless of entity, and average **/
			evaluator.UpdateConfusionMatrix(testData);
		}

		/** Get entity micro F1, i.e. regardless of entity, and average **/
		System.out.println("\nPrint micro F1: ");
		try {
			evaluator.PrintMicroF1_new(bw);
			//evaluator.PrintAccF1(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return evaluator;
	}
}
