package experiment.training;

import java.io.File;
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
import experiment.specs.EntitySpecClass;
import experiment.specs.SpecNounClass;

public class ConfigTrain {
	private ConstraintClass cc;
	private metaParaClass mpc;

	public ConfigTrain(ConstraintClass cc, metaParaClass mpc) {
		this.cc = cc;
		this.mpc = mpc;
	}
	
	// Get word dictionary from labeled and unlabeled data in each folder, finally save them into some file
	public static void GetWordDic(String dir, int iFold) throws IOException {
		String out = dir + "fold-" + String.valueOf(iFold) + "/";
		String TrainLabeled = out + "train.labeled.txt";
		String TrainUnlabeled = out + "train.unlabeled.txt";
		String WordDicFn = out + "train.wordDic.txt";
		GetDataDomain gdd = new GetDataDomain();
		gdd.ProcessToGetWordDic(TrainLabeled, TrainUnlabeled, WordDicFn);
	}

	// After getting the word dictionary, configure the data for training
	public ParaClass Configure(String dir, int iFold, String paradir) throws IOException {
		String out = dir + "fold-" + String.valueOf(iFold) + "/";
		String WordDicFn = out + "train.wordDic.txt";
		String LabeledTrainNameDicFn = out + "train.labeled.entity.txt";
		String UnlabeledTrainNameDicFn = dir + "train.unlabeled.entity.txt";

		System.out.println("Getting data domain statistics...");
		GetDataDomain gdd = new GetDataDomain();
		gdd.LoadWordDicFromFile(WordDicFn);
		gdd.LoadEntityDicFromFile(LabeledTrainNameDicFn, UnlabeledTrainNameDicFn);

		System.out.println("Initializing data...");
		Map<String, String> wordDic = gdd.wordDic; 
		String trainLabeled = out + "train.labeled.txt";
		String trainUnlabeled = out + "train.unlabeled.txt";
		EntityDataClass edc = new EntityDataClass();
		edc.LoadData(trainLabeled, trainUnlabeled, wordDic);
		edc.setPredLabel(); // let predLabel in training data the same as ground truth label
		edc.LoadEntity(LabeledTrainNameDicFn, UnlabeledTrainNameDicFn);

		System.out.println("Collecting each entity's specifications...");
		File f = new File(dir);
		String out2 = f.getParentFile().getAbsolutePath() + "/tag-specs/";
		EntitySpecClass esc = new EntitySpecClass();
		esc.Process(out2);
		SpecNounClass snc = new SpecNounClass();
		snc.Process(out2);
		
		System.out.println("Collect each entity data's observation for constraints...");
		EntityObsClass eoc = new EntityObsClass();
		eoc.Process(edc, gdd, esc, snc);
		gdd.nFeatures = edc.getFeatures(eoc); // get the features that are used in the constraints as well
		System.out.println("nFeatures = " + gdd.nFeatures);

		System.out.println("Compute constraint properties, including confidence and threshold prior, and initialize threshold...");
		ConstraintPropertyClass cpc = new ConstraintPropertyClass();
		cpc.Process(edc, eoc, gdd, cc, mpc);
		
		System.out.println("Load params for comparison ...");
		String para = paradir + "fold-" + String.valueOf(iFold) + "/";
		ParaGeneratorClass pgc = new ParaGeneratorClass();
		pgc.GenPara(dir, paradir, gdd.nFeatures, mpc.b); // comment this line if want to re-use the parameters
		pgc.LoadPara(para);
		/*double weight = pgc.w.get("Verdict_MoreSpec_DriveRide") * 0.2;
		pgc.w.put("Verdict_MoreSpec_DriveRide", weight);*/
		
		System.out.println("Build each entity data's graph...");
		EntityEdgeStructClass eesc = new EntityEdgeStructClass();
		eesc.ProcessH(edc, eoc, gdd);
		
		System.out.println("Initialize the P_theta and use it to do initial pruning...");
		Initializer initializer = new Initializer();
		initializer.Train(edc, eesc, eoc, gdd, mpc, pgc);
		Map<String, InferClass> infermap = eesc.GenInferP(edc, gdd, initializer.param, mpc);
		eesc.ProcessC(edc, eoc, gdd, cc, mpc, infermap); // first pruning based on the initial constraint threshold
		pgc.v = initializer.param.v; // update the parameters with initial training results

		System.out.println("Training...");
		EntityTrainClass model = new EntityTrainClass();
		model.Train(edc, eesc, eoc, gdd, mpc, cc, pgc, infermap);

		return model.param;
	}
}
