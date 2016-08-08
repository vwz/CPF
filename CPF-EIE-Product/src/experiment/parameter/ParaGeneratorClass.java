package experiment.parameter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import experiment.constraint.ConstraintClass;
import experiment.graph.GetDataDomain;
import experiment.inference.IndexMap;

public class ParaGeneratorClass {
	public Map<String, double[]> v;
	public Map<String, Double> w;
	public Map<String, Double> u;
	
	public void GenPara(String dir, String res, int nFeatures, Map<String, Double> b) {
		// Get global information
		GetDataDomain gdd = new GetDataDomain();
		ConstraintClass cc = new ConstraintClass(gdd.labelDic);

		// Some more global information (e.g. IndexMap) can be obtained after configuring training
		int nFolds = 5;
		
		File f = new File(res);
		if (!f.exists()) f.mkdir();

		try {
			for (int i = 0; i < nFolds; i++) {
				String out = dir + "fold-" + String.valueOf(i) + "/";
				String WordDicFn = out + "train.wordDic.txt";
				String LabeledTrainNameDicFn = out + "train.labeled.entity.txt";
				String UnlabeledTrainNameDicFn = out + "train.unlabeled.entity.txt";

				System.out.println("Getting data domain statistics...");
				gdd = new GetDataDomain();
				gdd.LoadWordDicFromFile(WordDicFn);
				gdd.LoadEntityDicFromFile(LabeledTrainNameDicFn, UnlabeledTrainNameDicFn);
				
				int nWords = gdd.wordDic.size();
				int nStates = gdd.labelDic.size();
				IndexMap im = new IndexMap(nStates, nWords, nFeatures);

				// Initialize model <ParaClass>, including v (for data) and w (for constraints)
				System.out.print("Initialize parameters...");
				ParaClass param = new ParaClass();
				param.InitializeByRandom(nStates, nWords, nFeatures, cc, b);
				
				// Save parameters
				String resdir = res + "fold-" + String.valueOf(i) + "/";
				f = new File(resdir);
				if (!f.exists()) f.mkdir();
				
				BufferedWriter bw;
				
				bw = new BufferedWriter(new FileWriter(new File(resdir + "parav.txt")));
				for (Entry<String, double[]> e : param.v.entrySet()) {
					StringBuilder sb = new StringBuilder();
					sb.append(e.getKey() + ":");
					double[] value = e.getValue();
					for (int j = 0; j < value.length; j++)
						sb.append(value[j] + " ");
					bw.write(sb.toString().trim() + "\n");
				}
				bw.close();
				
				bw = new BufferedWriter(new FileWriter(new File(resdir + "paraw.txt")));
				for (Entry<String, Double> e : param.w.entrySet()) {
					StringBuilder sb = new StringBuilder();
					sb.append(e.getKey() + ":" + e.getValue());
					bw.write(sb.toString() + "\n");
				}
				bw.close();
				
				bw = new BufferedWriter(new FileWriter(new File(resdir + "parau.txt")));
				for (Entry<String, Double> e : param.u.entrySet()) {
					StringBuilder sb = new StringBuilder();
					sb.append(e.getKey() + ":" + e.getValue());
					bw.write(sb.toString() + "\n");
				}
				bw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void LoadPara(String dir) throws IOException {
		this.v = new HashMap<String, double[]>();
		this.w = new HashMap<String, Double>();
		this.u = new HashMap<String, Double>();
				
		BufferedReader br = new BufferedReader(new FileReader(new File(dir + "parav.txt")));
		String line = "";
		while ((line=br.readLine()) != null) {
			String[] segs = line.split(":");
			String factorName = segs[0];
			String[] tmp = segs[1].split(" ");
			double[] value = new double[tmp.length];
			for (int i = 0; i < tmp.length; i++)
				value[i] = Double.valueOf(tmp[i]);
			this.v.put(factorName, value);
		}
		br.close();
		
		br = new BufferedReader(new FileReader(new File(dir + "paraw.txt")));
		line = "";
		while ((line=br.readLine()) != null) {
			String[] segs = line.split(":");
			String factorName = segs[0];
			double value = Double.valueOf(segs[1]);
			this.w.put(factorName, value);
		}
		br.close();
		
		br = new BufferedReader(new FileReader(new File(dir + "parau.txt")));
		line = "";
		while ((line=br.readLine()) != null) {
			String[] segs = line.split(":");
			String factorName = segs[0];
			double value = Math.log(Double.valueOf(segs[1]));
			this.u.put(factorName, value);
		}
		br.close();
	}
}
