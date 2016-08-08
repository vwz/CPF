package experiment.decoder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import experiment.graph.GetDataDomain;
import experiment.graph.LineClass2;
import experiment.graph.dataStructure;


public class Evaluator {
	public Map<String, Double> f1;
	private Map<String, String> labelDic;
	private Map<String, String> invertedLabelDic;
	public double[][] confusionMatrix;
	public int nCorrect;
	public int nTotal;
	
	public double avgprec;
	public double avgrecall;
	public double avgf1;
	
	public Evaluator(GetDataDomain gdd) {
		f1 = new HashMap<String, Double>();
		this.labelDic = gdd.labelDic;
		this.invertedLabelDic = gdd.invertedLabelDic;
		
		int n = labelDic.size();
		this.confusionMatrix = new double[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				this.confusionMatrix[i][j] = 0;
		
		this.avgf1 = 0;
		this.avgprec = 0;
		this.avgrecall = 0;
		
		this.nCorrect = 0;
		this.nTotal = 0;
	}
	
	public void UpdateAccuracyCounts(dataStructure ds) {
		for (int i = 0; i < ds.data.size(); i++) {
			LineClass2 lc = ds.data.get(i);
			
			nTotal++;
			if (lc.predLabel.equalsIgnoreCase(lc.label))
				nCorrect++;

		}
	}
	
	public void PrintAccuracy() {
		System.out.println("acc = " + ((double) nCorrect / (double) nTotal));
	}
	
	public void UpdateConfusionMatrix(dataStructure ds) {
		for(int k = 0; k < ds.data.size(); k++) {
			LineClass2 lc = ds.data.get(k);

			String label1 = lc.label;
			String label2 = lc.predLabel;

			if (labelDic.containsKey(label1) && labelDic.containsKey(label2)) {
				int i = Integer.valueOf(labelDic.get(label1)).intValue();
				int j = Integer.valueOf(labelDic.get(label2)).intValue();

				confusionMatrix[i][j] += 1.0;
			}
		}
	}
	
	public void PrintMicroF1_new(BufferedWriter bw) throws IOException {
		Set<String> set = new HashSet<String>();
		set.add("title"); set.add("author"); set.add("booktitle");
		
		int n = labelDic.size();
		Map<String, double[]> map = new HashMap<String, double[]>();
		for (int i = 0; i < n; i++)
		{
			double tp = confusionMatrix[i][i];
			double fp = 0;
			for (int j = 0; j < n; j++)
				fp += confusionMatrix[j][i];
			fp -= tp;

			double fn = 0;
			for (int j = 0; j < n; j++)
				fn += confusionMatrix[i][j];
			fn -= tp;

			double prec = 0;
			if (tp + fp != 0)
				prec = tp / (tp + fp);
			double recall = 0;
			if (tp + fn != 0)
				recall = tp / (tp + fn);

			double f1score = 0;
			if (prec + recall != 0) {
				f1score = 2 * prec * recall / (prec + recall);
			}

			System.out.println(invertedLabelDic.get(String.valueOf(i)) + "\t" + prec + "\t" + recall + "\t" + f1score);
			map.put(invertedLabelDic.get(String.valueOf(i)), new double[]{prec, recall, f1score});
		}
		
		int m = 0;
		double irrPrec = 0;
		double irrRecall = 0;
		double irrF1 = 0;
		for (Entry<String, double[]> e : map.entrySet()) {
			String factorName = e.getKey();
			double[] values = e.getValue();
			if (set.contains(factorName)) {
				bw.write(factorName + "\t" + values[0] + "\t" + values[1] + "\t" + values[2]);
				bw.newLine();
			} else {
				irrPrec += values[0];
				irrRecall += values[1];
				irrF1 += values[2];
				m++;
			}
		}
		
		irrPrec /= (double) m;
		irrRecall /= (double) m;
		irrF1 /= (double) m;
		
		System.out.println("Irrelevant" + "\t" + irrPrec + "\t" + irrRecall + "\t" + irrF1);
		bw.write("Irrelevant" + "\t" + String.valueOf(irrPrec + "\t" + irrRecall + "\t" + irrF1));
		bw.newLine();
		
		avgprec = 0;
		avgrecall = 0;
		avgf1 = 0;
		for (Entry<String, double[]> e : map.entrySet()) {
			String factorName = e.getKey();
			double[] values = e.getValue();
			if (set.contains(factorName)) {
				avgprec += values[0];
				avgrecall += values[1];
				avgf1 += values[2];
			}
		}
		avgprec += irrPrec;
		avgrecall += irrRecall;
		avgf1 += irrF1;
		
		avgprec /= (double) (set.size() + 1);
		avgrecall /= (double) (set.size() + 1);
		avgf1 /= (double) (set.size() + 1);
		
		System.out.println("Average" + "\t" + avgprec + "\t" + avgrecall + "\t" + avgf1);
		bw.write("Average" + "\t" + String.valueOf(avgprec + "\t" + avgrecall + "\t" + avgf1));
		bw.newLine();
	}
	
	public void PrintMicroF1(BufferedWriter bw) throws IOException {
		double avgF1 = 0;
		double avgPrec = 0;
		double avgRecall = 0;
		int n = labelDic.size();
		for (int i = 0; i < n; i++)
		{
			double tp = confusionMatrix[i][i];
			double fp = 0;
			for (int j = 0; j < n; j++)
				fp += confusionMatrix[j][i];
			fp -= tp;

			double fn = 0;
			for (int j = 0; j < n; j++)
				fn += confusionMatrix[i][j];
			fn -= tp;

			double prec = 0;
			if (tp + fp != 0)
				prec = tp / (tp + fp);
			double recall = 0;
			if (tp + fn != 0)
				recall = tp / (tp + fn);

			double f1score = 0;
			if (prec + recall != 0) {
				f1score = 2 * prec * recall / (prec + recall);
			}

			System.out.println(invertedLabelDic.get(String.valueOf(i)) + "\t" + prec + "\t" + recall + "\t" + f1score);
			avgF1 += f1score;
			avgPrec += prec;
			avgRecall += recall;
			
			bw.write(invertedLabelDic.get(String.valueOf(i)) + "\t" + prec + "\t" + recall + "\t" + f1score);
			bw.newLine();
		}
		
		avgPrec /= (double) n;
		avgRecall /= (double) n;
		avgF1 /= (double) n;
		
		this.avgprec = avgPrec;
		this.avgrecall = avgRecall;
		this.avgf1 = avgF1;
		
		System.out.println(avgPrec + "\t" + avgRecall + "\t" + avgF1);
		bw.write("\t" + String.valueOf(avgPrec + "\t" + avgRecall + "\t" + avgF1));
		bw.newLine();
	}

	public void GetF1(dataStructure ds) {
		int n = labelDic.size();
		double[][] confusionMatrix = new double[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				confusionMatrix[i][j] = 0;

		for(int k = 0; k < ds.data.size(); k++) {
			LineClass2 lc = ds.data.get(k);

			String label1 = lc.label;
			String label2 = lc.predLabel;

			if (labelDic.containsKey(label1) && labelDic.containsKey(label2)) {
				int i = Integer.valueOf(labelDic.get(label1)).intValue();
				int j = Integer.valueOf(labelDic.get(label2)).intValue();

				confusionMatrix[i][j] += 1.0;
			}
		}

		// print out macro-F1
		for (Entry<String, String> e : labelDic.entrySet())
			f1.put(e.getKey(), 0.0);
		
		for (int i = 0; i < n; i++)
		{
			double tp = confusionMatrix[i][i];
			double fp = 0;
			for (int j = 0; j < n; j++)
				fp += confusionMatrix[j][i];
			fp -= tp;

			double fn = 0;
			for (int j = 0; j < n; j++)
				fn += confusionMatrix[i][j];
			fn -= tp;

			double prec = 0;
			if (tp + fp != 0)
				prec = tp / (tp + fp);
			double recall = 0;
			if (tp + fn != 0)
				recall = tp / (tp + fn);

			double f1score = 0;
			if (prec + recall != 0) {
				f1score = 2 * prec * recall / (prec + recall);
				f1.put(invertedLabelDic.get(String.valueOf(i)), f1score);
			}

			System.out.println(invertedLabelDic.get(String.valueOf(i)) + "\t" + prec + "\t" + recall + "\t" + f1score);
		}
	}
}