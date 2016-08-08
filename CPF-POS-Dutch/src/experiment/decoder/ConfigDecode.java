package experiment.decoder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import experiment.constraint.ConstraintClass;
import experiment.constraint.EntityObsClass;
import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.inference.EntityEdgeStructClass;
import experiment.inference.InferClass;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;

public class ConfigDecode {
	private ConstraintClass cc;
	private metaParaClass mpc;
	
	public ConfigDecode(ConstraintClass cc, metaParaClass mpc) {
		this.cc = cc;
		this.mpc = mpc;
	}
	
	// After getting the word dictionary, configure the data for training
	public Evaluator Configure(String dir, ParaClass param, BufferedWriter bw) throws IOException {
		String WordDicFn = dir + "fea_map.txt";
		
		System.out.println("Updating constraint threshold...");
		UpdateThreshold(param);
		
		System.out.println("Getting data domain statistics...");
		GetDataDomain gdd = new GetDataDomain();
		gdd.LoadWordDicFromFile(WordDicFn);
		
		System.out.println("Initializing data...");
		Map<String, String> wordDic = gdd.wordDic; 
		String testLabeled = dir + "test.txt";
		EntityDataClass edc = new EntityDataClass();
		edc.LoadData(testLabeled, wordDic);

		System.out.println("Collecting each entity data's observation for constraints...");
		EntityObsClass eoc = new EntityObsClass();
		eoc.Process(edc, gdd);
		gdd.nFeatures = edc.getFeatures(eoc); // get the features that are used in the constraints as well
		
		System.out.println("Building each entity data's graph...");
		EntityEdgeStructClass eesc = new EntityEdgeStructClass();
		eesc.ProcessH(edc, eoc, gdd);
		Map<String, InferClass> infermap = eesc.GenInferP(edc, gdd, param, mpc);
		//eesc.ProcessC(edc, eoc, gdd, cc, mpc, infermap);
		eesc.ProcessC(edc, eoc, gdd, cc);
		
		System.out.println("Decoding...");
		EntityDecodeClass model = new EntityDecodeClass();
		Evaluator eval = model.Decode(edc, eesc, eoc, gdd, param, mpc, cc, bw, infermap);
		
		return eval;
	}
	
	private void UpdateThreshold(ParaClass param) {
		for (Entry<String, Double> e : mpc.eps.entrySet()) {
			double u = param.u.get(e.getKey());
			System.out.println("update threshold: " + e.getKey() + " = " + param.sigmoid(u));
			mpc.eps.put(e.getKey(), Math.log(param.sigmoid(u)));
		}
	}
}
