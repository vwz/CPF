package experiment.decoder;

import java.io.BufferedWriter;
import java.io.File;
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
import experiment.specs.EntitySpecClass;
import experiment.specs.SpecNounClass;

public class ConfigDecode {
	private ConstraintClass cc;
	private metaParaClass mpc;
	
	public ConfigDecode(ConstraintClass cc, metaParaClass mpc) {
		this.cc = cc;
		this.mpc = mpc;
	}
	
	// After getting the word dictionary, configure the data for training
	public Evaluator Configure(String dir, int iFold, ParaClass param, BufferedWriter bw) throws IOException {
		String out = dir + "fold-" + String.valueOf(iFold) + "/";
		String WordDicFn = out + "train.wordDic.txt";
		String TestNameDicFn = out + "test.labeled.entity.txt";
		
		System.out.println("Updating constraint threshold...");
		UpdateThreshold(param);
		
		System.out.println("Getting data domain statistics...");
		GetDataDomain gdd = new GetDataDomain();
		gdd.LoadWordDicFromFile(WordDicFn);
		gdd.LoadEntityDicFromFile(TestNameDicFn);
		
		System.out.println("Initializing data...");
		Map<String, String> wordDic = gdd.wordDic; 
		String testLabeled = out + "test.labeled.txt";
		EntityDataClass edc = new EntityDataClass();
		edc.LoadData(testLabeled, wordDic);
		
		System.out.println("Collecting each entity's specifications...");
		File f = new File(dir);
		String out2 = f.getAbsolutePath() + "/tag-specs/";
		EntitySpecClass esc = new EntitySpecClass();
		esc.Process(out2);
		SpecNounClass snc = new SpecNounClass();
		snc.Process(out2);

		System.out.println("Collecting each entity data's observation for constraints...");
		EntityObsClass eoc = new EntityObsClass();
		eoc.Process(edc, gdd, esc, snc);
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
