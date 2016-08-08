package experiment.decoder;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ObsClass;
import experiment.graph.GetDataDomain;
import experiment.graph.LineClass2;
import experiment.graph.dataStructure;
import experiment.inference.EdgeStructClass;
import experiment.inference.IndexMap;
import experiment.inference.InferClass;
import experiment.inference.VarPotentialClass;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;
import experiment.suffstats.ComputeSufficientStats;

public class DecoderClass {
	private GetDataDomain gdd;
	private IndexMap im;
	private metaParaClass metaPara;
	private ParaClass param;
	private ConstraintClass cc;

	public DecoderClass(GetDataDomain gdd, IndexMap im, metaParaClass metaPara, ParaClass param, ConstraintClass cc) {
		this.gdd = gdd;
		this.im = im;
		this.metaPara = metaPara;
		this.param = param;
		this.cc = cc;
	}

	public void GibbsSampling(dataStructure ds, List<ObsClass> obsList, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, InferClass inferP, String entity) {
		// Initialize the predictions by random
		//InitializeByRandom(ds);
		InitilizeByMaxNodePotential(ds);

		// Make potentials
		VarPotentialClass vpc = new VarPotentialClass(cc, metaPara);
		ComputeSufficientStats css = new ComputeSufficientStats(gdd.labelDic, im);

		Random r = new Random();
		int nStates = gdd.labelDic.size();

		// Initialize the inverted index for <label, a list of lines>
		InvertedIndexClass iic = new InvertedIndexClass(gdd.labelDic);
		iic.Initialize(ds);

		int maxDecoderIter = metaPara.burninIter;
		for (int iter = 0; iter < maxDecoderIter; iter++) {
			//if (iter % (maxDecodeIter/10) == 0) 
			//	System.out.println("Sampling iteration " + iter);

			// generate the proposal
			for (int i = 0; i < ds.data.size(); i++) {
				LineClass2 lc = ds.data.get(i);

				// current predicted label
				String y0 = lc.predLabel;

				// prepare the score list for each proposal
				double[] values = new double[nStates];
				Arrays.fill(values, 0);
				String[] yprops = new String[nStates];
				int index = 0;
				int index0 = -1;
				double c = Double.NEGATIVE_INFINITY;
								
				for (Entry<String, String> e : gdd.labelDic.entrySet()) {
					String yprop = e.getKey();
					if (yprop.equalsIgnoreCase(y0)) index0 = index;
					yprops[index] = yprop;

					// get this variable's potential
					int stateID = Integer.valueOf(e.getValue());
					double p = inferP.nodeBel[i][stateID];
					
					double potv = vpc.MakeVarPotentialForV(ds, i, yprop, edgeStruct, edgeStructCons, obsList, iic, css, param, p);
					double potw = vpc.MakeVarPotentialForW(ds, i, yprop, edgeStruct, edgeStructCons, obsList, iic, css, param, p);
					double pot = potv + potw;

					values[index] = pot;

					if (pot > c) c = pot;

					index++;
				}

				// compute the values
				double before = values[index0];
				c = c - before;
				for (int j = 0; j < nStates; j++) {
					values[j] = values[j] - before; // this array stores: value_after - value_before
				}

				// compute the probability to sample each configuration, and we use e^x1 / (e^x1 + e^x2) = e^{x1-c} / (e^{x1-c} + e^{x2-c})
				double expsum = 0;
				double[] tmp = new double[nStates];
				Arrays.fill(tmp, 0);
				for (int j = 0; j < nStates; j++) {
					tmp[j] = Math.exp(values[j] - c); // this tmp array stores: e^{xj-c}
					expsum += tmp[j];
				}

				// sample one proposal to be the next configuration
				/*if (iter < maxDecoderIter - 20)
					index = Sampling(tmp, expsum, r);
				else*/
					index = ReturnMaxIndex(tmp);
				if (index != -1) {
					String ychosen = yprops[index];

					// update the inverted index for <label, a list of lines>
					if (!ychosen.equalsIgnoreCase(lc.predLabel))
						iic.Update(i, lc.predLabel, ychosen);

					// accept the proposal
					lc.predLabel = ychosen;
				}
				else
					System.out.println("error in sampling");
			}
		}
	}

	private String ReturnMaxPred(int[] value) {
		int maxVal = -1;
		int maxIndex = -1;
		for (int i = 0; i < value.length; i++) {
			if (value[i] >= maxVal) {
				maxVal = value[i];
				maxIndex = i;
			}
		}
		String yprop = gdd.invertedLabelDic.get(String.valueOf(maxIndex));
		//System.out.println(yprop);
		return yprop;
	}

	private int Sampling(double[] values, double sum, Random r) {
		double u = sum * r.nextDouble();
		sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
			if (sum >= u) return i;
		}
		return -1;
	}

	private void InitializeByRandom(dataStructure ds) {
		Random r = new Random();

		int n = gdd.labelDic.size();
		String[] yprops = new String[n];
		int j = 0;
		for (String e : gdd.labelDic.keySet())
			yprops[j++] = e;

		for (int i = 0; i < ds.data.size(); i++) {
			LineClass2 lc = ds.data.get(i);
			String yprop = yprops[r.nextInt(n)];
			lc.predLabel = yprop;
			ds.data.set(i, lc);
		}
	}
	
	private int ReturnMaxIndex(double[] value) {
		double maxVal = -1;
		int maxIndex = -1;
		for (int i = 0; i < value.length; i++) {
			if (value[i] >= maxVal) {
				maxVal = value[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	private void InitilizeByMaxNodePotential(dataStructure ds) {
		for (int i = 0; i < ds.data.size(); i++) {
			LineClass2 lc = ds.data.get(i);

			String maxState = "";
			double max = -Double.MAX_VALUE;
			String factorName = "";
			double[] weights = null;
			int index = -1;

			for (Entry<String, String> e : gdd.labelDic.entrySet()) {
				String yprop = e.getKey();
				int stateID = Integer.valueOf(e.getValue()).intValue();

				double value = 0;

				// Label_Token
				factorName = "Label_Token";
				weights = param.v.get(factorName);
				for (int j = 0; j < lc.wordIDList.size(); j++) {
					int wordID = Integer.valueOf(lc.wordIDList.get(j)).intValue();
					double count = lc.countList.get(j);

					index = im.GetIndexStateToken(stateID, wordID);
					value += weights[index] * count;
				}
				
				// label_features
				factorName = "Label_Features";
				weights = param.v.get(factorName);
				for (int j = 0; j < lc.features.length; j++) {
					double count = lc.features[j];
					index = im.GetIndexStateFeatures(stateID, j);
					value += count * weights[index];
				}

				weights = param.v.get(factorName);
				for (int j = 0; j < lc.next_features.length; j++) {
					double count = lc.next_features[j];
					index = im.GetIndexStateFeatures(stateID, lc.features.length + j);
					value += count * weights[index];
				}
				
				if (value > max) {
					max = value;
					maxState = yprop;
				}
			}

			lc.predLabel = maxState;
		}
	}
}
