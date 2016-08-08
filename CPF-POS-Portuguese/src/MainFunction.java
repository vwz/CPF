import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import experiment.constraint.ConstraintClass;
import experiment.decoder.ConfigDecode;
import experiment.decoder.Evaluator;
import experiment.graph.GetDataDomain;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;
import experiment.training.ConfigTrain;

public class MainFunction {
	public static void main(String[] args) {
		if (args.length == 0)
			return;
		
		if (args.length != 3)
			System.err.println("not enough parameters");
		else { 
			double alpha1 = Double.valueOf(args[0]);
			double alpha2 = Double.valueOf(args[1]);
			double alpha3 = Double.valueOf(args[2]);
			Process(alpha1, alpha2, alpha3);
		}
	}

	public static void Process(double alpha1, double alpha2, double alpha3) {
		// Get global information
		GetDataDomain gdd = new GetDataDomain();
		ConstraintClass cc = new ConstraintClass(gdd.labelDic);
		metaParaClass mpc = new metaParaClass();

		// set parameters
		mpc.alpha1 = alpha1;
		mpc.alpha2 = alpha2;
		mpc.alpha3 = alpha3;

		String para = String.format("alpha1_%.3f", alpha1) + " " + String.format("alpha2_%.3f", alpha2) + " " + String.format("alpha3_%.3f", alpha3);

		// Some more global information (e.g. IndexMap) can be obtained after configuring training
		String workspace = "Set_Your_Workspace_Path";
		String dir = workspace + "/cikm2016-cpf/Data/POS/CoNLL-X_Shared_Task/portuguese/features/";
		String res = workspace + "/cikm2016-cpf/Prediction/POS/CoNLL-X_Shared_Task/portuguese/cpf " + para + ".txt";

		System.out.println("alpha1 = " + mpc.alpha1 + ", alpha2 = " + mpc.alpha2 + ", alpha3 = " + mpc.alpha3);

		int numdata = 100;
		int nFolds = 5;

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(res)));
			ConfigTrain ct = new ConfigTrain(cc, mpc);
			ConfigDecode cd = new ConfigDecode(cc, mpc);

			double elapseTrain = 0;
			double elapseTest = 0;

			double prec = 0;
			double recall = 0;
			double f1score = 0;

			for (int i = 0; i < nFolds; i++) {
				String paradir = workspace + "/cikm2016-cpf/Parameter/POS/CoNLL-X_Shared_Task/portuguese/";

				// training
				long tstart = System.currentTimeMillis();
				ParaClass param = ct.Configure(dir, i, paradir, numdata);
				long tend = System.currentTimeMillis();
				long tDelta = tend - tstart;
				double hours = (double) tDelta / 1000.0 / 60.0 / 60.0;
				elapseTrain += (double) tDelta / 1000.0 / 60.0 / 60.0; // in hours
				
				bw.write("\n start time\t" + hours + "\n");

				// testing
				tstart = System.currentTimeMillis();
				Evaluator eval = cd.Configure(dir, param, bw);
				tend = System.currentTimeMillis();
				tDelta = tend - tstart;
				hours = (double) tDelta / 1000.0 / 60.0 / 60.0;
				elapseTest += (double) tDelta / 1000.0 / 60.0 / 60.0; // in hours
				
				bw.write("\n end time\t" + hours + "\n");

				prec += eval.avgprec;
				recall += eval.avgrecall;
				f1score += eval.avgf1;

				bw.newLine();
			}

			elapseTrain /= (double) nFolds;
			elapseTest /= (double) nFolds;

			prec /= (double) nFolds;
			recall /= (double) nFolds;
			f1score /= (double) nFolds;

			String time = String.valueOf(elapseTrain) + "\t" + String.valueOf(elapseTest);
			bw.write("\n train test time\t" + time + "\n");
			System.out.println(time);
			String result = String.valueOf(prec) + "\t" + String.valueOf(recall) + "\t" + String.valueOf(f1score);
			bw.write("\n avg prec recall f1\t" + result + "\n");
			System.out.println(result);

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
