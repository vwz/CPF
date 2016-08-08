package experiment.training;

import java.io.IOException;
import java.util.Map;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ConstraintPropertyClass;
import experiment.constraint.EntityObsClass;
import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.inference.EntityEdgeStructClass;
import experiment.inference.InferClass;
import experiment.initialize.Initializer;
import experiment.parameter.ParaClass;
import experiment.parameter.ParaGeneratorClass;
import experiment.parameter.metaParaClass;

public class ConfigTrain {
	private ConstraintClass cc;
	private metaParaClass mpc;

	public ConfigTrain(ConstraintClass cc, metaParaClass mpc) {
		this.cc = cc;
		this.mpc = mpc;
	}

	// After getting the word dictionary, configure the data for training
	public ParaClass Configure(String dir, int iFold, String paradir, int numdata) throws IOException {
		String WordDicFn = dir + "fea_map.txt";

		System.out.println("Getting data domain statistics...");
		GetDataDomain gdd = new GetDataDomain();
		gdd.LoadWordDicFromFile(WordDicFn);

		System.out.println("Initializing data...");
		Map<String, String> wordDic = gdd.wordDic; 
		String trainLabeled = dir + "citation_train." + numdata + "." + iFold + ".txt";
		String trainUnlabeled = dir + "citation_unlabeled.txt";
		EntityDataClass edc = new EntityDataClass();
		edc.LoadData(trainLabeled, trainUnlabeled, wordDic);
		edc.setPredLabel(); // let predLabel in training data the same as ground truth label
		gdd.nLabeled = edc.nLabeled;
		gdd.nUnlabeled = edc.nUnlabeled;
		System.out.println("nLabeled=" + gdd.nLabeled + ", " + "nUnlabeled=" + gdd.nUnlabeled);

		System.out.println("Collect each entity data's observation for constraints...");
		EntityObsClass eoc = new EntityObsClass();
		eoc.Process(edc, gdd);
		gdd.nFeatures = edc.getFeatures(eoc); // get the features that are used in the constraints as well
		System.out.println("nFeatures = " + gdd.nFeatures);

		System.out.println("Compute constraint properties, including confidence and threshold prior, and initialize threshold...");
		ConstraintPropertyClass cpc = new ConstraintPropertyClass();
		cpc.Process(edc, eoc, gdd, cc, mpc);
		
		System.out.println("Load params for comparison ...");
		ParaGeneratorClass pgc = new ParaGeneratorClass();
		pgc.GenPara(dir, paradir, gdd.nFeatures, mpc.b); // comment this line if want to re-use the parameter
		pgc.LoadPara(paradir, iFold);
		
		System.out.println("Build each entity data's graph...");
		EntityEdgeStructClass eesc = new EntityEdgeStructClass();
		eesc.ProcessH(edc, eoc, gdd);
		
		System.out.println("Initialize the P_theta and use it to do initial pruning...");
		Initializer initializer = new Initializer();
		initializer.Train(edc, eesc, eoc, gdd, mpc, pgc);
		Map<String, InferClass> infermap = eesc.GenInferP(edc, gdd, initializer.param, mpc);
		eesc.ProcessC(edc, eoc, gdd, cc, mpc, infermap); // first pruning based on the initial constraint threshold
		pgc.v = initializer.param.v; // update the parameters with initial training results
	
		/*pgc.w.put("title_title_Continuous", 1.0);
		pgc.w.put("booktitle_title_CloseAfter", 1.0);*/

		System.out.println("Training...");
		EntityTrainClass model = new EntityTrainClass();
		model.Train(edc, eesc, eoc, gdd, mpc, cc, pgc, infermap);

		return model.param;
	}
}
