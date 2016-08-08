package experiment.parameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import experiment.constraint.ConstraintClass;
import experiment.graph.GetDataDomain;
import experiment.inference.IndexMap;
import experiment.suffstats.SuffStatsClass;

public class ParaClass {
	public Map<String, double[]> v;
	public Map<String, Double> w;
	public Map<String, Double> u;
	public double size;

	public ParaClass() {
		v = new HashMap<String, double[]>();
		w = new HashMap<String, Double>();
		u = new HashMap<String, Double>();
	}
	
	public ParaClass(ConstraintClass cc) {
		v = new HashMap<String, double[]>();
		w = new HashMap<String, Double>();
		u = new HashMap<String, Double>();
		
		for (int i = 0; i < cc.constraints.size(); i++) {
			String factorName = cc.constraints.get(i);
			w.put(factorName, 0.0);
			u.put(factorName, 0.0);
		}
	}

	public Map<String, Double> ComputeThreshold() {
		// eps = log [1/1+e^{-x}]
		Map<String, Double> eps = new HashMap<String, Double>();
		for (Entry<String, Double> e : u.entrySet()) {
			double value  = Math.log(sigmoid(e.getValue()));
			eps.put(e.getKey(), value);
		}
		return eps;
	}
	
	public void SetByThreshold(Map<String, Double> eps) {
		// u = -log [1/e^{eps}-1]
		for (Entry<String, Double> e : eps.entrySet()) {
			double value  = eps.get(e.getKey());
			double u_value = - Math.log(1 / Math.pow(Math.E, value) - 1);
			u.put(e.getKey(), u_value);
		}
	}

	public double GetTwoNormOnV() {
		double twoNorm = 0;
		int n = 0;
		for (Entry<String, double[]> e : v.entrySet()) {
			double[] value = e.getValue();
			n += value.length;
			for (int i = 0; i < value.length; i++)
				twoNorm += Math.pow(value[i],2);
		}
		if (n > 0)
			twoNorm = Math.sqrt(twoNorm / (double) n);
		return twoNorm;
	}

	public double GetTwoNormOnW() {
		double twoNorm = 0;
		int n = 0;
		for (Entry<String, Double> e : w.entrySet()) {
			double value = e.getValue();
			twoNorm += Math.pow(value,2);
			n++;
		}
		if (n > 0)
			twoNorm = Math.sqrt(twoNorm / (double) n);
		return twoNorm;
	}

	public double GetTwoNormOnU() {
		double twoNorm = 0;
		int n = 0;
		for (Entry<String, Double> e : u.entrySet()) {
			double value = e.getValue();
			twoNorm += Math.pow(value,2);
			n++;
		}
		if (n > 0)
			twoNorm = Math.sqrt(twoNorm / (double) n);
		return twoNorm;
	}

	public double sigmoid(double x) {
		return 1.0 / ( 1 + Math.exp(- x));
	}

	// Various operations
	public ParaClass Minus(ParaClass other) {
		ParaClass out = new ParaClass();

		for (Entry<String, double[]> e : v.entrySet()) {
			double[] value = e.getValue().clone();
			double[] otherValue = other.v.get(e.getKey());
			for (int i = 0; i < value.length; i++)
				value[i] = value[i] - otherValue[i];
			out.v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : w.entrySet()) {
			double value = e.getValue();
			double otherValue = other.w.get(e.getKey());
			value -= otherValue;
			out.w.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : u.entrySet()) {
			double value = e.getValue();
			double otherValue = other.u.get(e.getKey());
			value -= otherValue;
			out.u.put(e.getKey(), value);
		}

		return out;
	}

	public void MinusOnV(ParaClass other) {
		for (Entry<String, double[]> e : v.entrySet()) {
			double[] value = e.getValue();
			double[] otherValue = other.v.get(e.getKey());
			for (int i = 0; i < value.length; i++)
				value[i] = value[i] - otherValue[i];
			v.put(e.getKey(), value);
		}
	}

	public void AddOnW(ParaClass other) {
		for (Entry<String, Double> e : w.entrySet()) {
			double value = e.getValue();
			double otherValue = other.w.get(e.getKey());
			value += otherValue;
			w.put(e.getKey(), value);
		}
	}

	public void MinusOnU(ParaClass other) {
		for (Entry<String, Double> e : u.entrySet()) {
			double value = e.getValue();
			double otherValue = other.u.get(e.getKey());
			value -= otherValue;
			u.put(e.getKey(), value);
		}
	}

	public double Multiply(ParaClass other) {
		double out = 0;

		for (Entry<String, double[]> e : v.entrySet()) {
			double[] value = e.getValue();
			double[] otherValue = other.v.get(e.getKey());
			for (int i = 0; i < value.length; i++)
				out += value[i] * otherValue[i];
		}

		for (Entry<String, Double> e : w.entrySet()) {
			double value = e.getValue();
			double otherValue = other.w.get(e.getKey());
			out += value * otherValue;
		}
		
		for (Entry<String, Double> e : u.entrySet()) {
			double value = e.getValue();
			double otherValue = other.u.get(e.getKey());
			out += value * otherValue;
		}

		return out;
	}

	public ParaClass Multiply(double x) {
		ParaClass out = new ParaClass();

		for (Entry<String, double[]> e : v.entrySet()) {
			double[] value = e.getValue().clone();
			for (int i = 0; i < value.length; i++)
				value[i] *= x;
			out.v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : w.entrySet()) {
			double value = e.getValue();
			value *= x;
			out.w.put(e.getKey(), value);
		}
		
		for (Entry<String, Double> e : u.entrySet()) {
			double value = e.getValue();
			value *= x;
			out.u.put(e.getKey(), value);
		}

		return out;
	}

	public ParaClass Divide(double x) {
		if (x == 0) return null;

		ParaClass out = new ParaClass();

		for (Entry<String, double[]> e : v.entrySet()) {
			double[] value = e.getValue().clone();
			for (int i = 0; i < value.length; i++)
				value[i] /= x;
			out.v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : w.entrySet()) {
			double value = e.getValue();
			value /= x;
			out.w.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : u.entrySet()) {
			double value = e.getValue();
			value /= x;
			out.u.put(e.getKey(), value);
		}

		return out;
	}

	public ParaClass Add(ParaClass other) {
		ParaClass out = new ParaClass();

		for (Entry<String, double[]> e : v.entrySet()) {
			double[] value = e.getValue().clone();
			double[] otherValue = other.v.get(e.getKey());
			for (int i = 0; i < value.length; i++)
				value[i] = value[i] + otherValue[i];
			out.v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : w.entrySet()) {
			double value = e.getValue();
			double otherValue = other.w.get(e.getKey());
			value += otherValue;
			out.w.put(e.getKey(), value);
		}
		
		for (Entry<String, Double> e : u.entrySet()) {
			double value = e.getValue();
			double otherValue = other.u.get(e.getKey());
			value += otherValue;
			out.u.put(e.getKey(), value);
		}

		return out;
	}

	// this is for copying the other parameters
	public void Copy(ParaClass other) {
		for (Entry<String, double[]> e : other.v.entrySet()) {
			double[] value = e.getValue().clone();
			v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : other.w.entrySet()) {
			double value = e.getValue();
			w.put(e.getKey(), value);
		}
		
		for (Entry<String, Double> e : other.u.entrySet()) {
			double value = e.getValue();
			u.put(e.getKey(), value);
		}
	}

	public void CopyOnW(ParaClass other) {
		for (Entry<String, Double> e : other.w.entrySet()) {
			double value = e.getValue();
			w.put(e.getKey(), value);
		}
	}

	private double[] f_minus(double[] src, List<double[]> tar) {
		double[] value = src.clone();
		for (int i = 0; i < tar.size(); i++) {
			double[] otherValue = tar.get(i);
			int index = (int) otherValue[0];
			double count = otherValue[1];
			value[index] = value[index] - count;
		}
		return value;
	}

	private double[] f_add(double[] src, List<double[]> tar) {
		double[] value = src.clone();
		for (int i = 0; i < tar.size(); i++) {
			double[] otherValue = tar.get(i);
			int index = (int) otherValue[0];
			double count = otherValue[1];
			value[index] = value[index] + count;
		}
		return value;
	}

	public void Minus(SuffStatsClass other) {
		for (Entry<String, List<double[]>> e : other.v.entrySet()) {
			List<double[]> tar = e.getValue();
			double[] src = v.get(e.getKey());
			double[] value = f_minus(src, tar);
			v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : other.w.entrySet()) {
			double tar = e.getValue();
			double src = w.get(e.getKey());
			double value = src - tar;
			w.put(e.getKey(), value);
		}
		
		for (Entry<String, Double> e : other.u.entrySet()) {
			double tar = e.getValue();
			double src = u.get(e.getKey());
			double value = src - tar;
			u.put(e.getKey(), value);
		}
	}

	public void MinusOnV(SuffStatsClass other) {
		for (Entry<String, List<double[]>> e : other.v.entrySet()) {
			List<double[]> tar = e.getValue();
			double[] src = v.get(e.getKey());
			double[] value = f_minus(src, tar);
			v.put(e.getKey(), value);
		}
	}

	public void AddOnV(SuffStatsClass other) {
		for (Entry<String, List<double[]>> e : other.v.entrySet()) {
			List<double[]> tar = e.getValue();
			double[] src = v.get(e.getKey());
			double[] value = f_add(src, tar);
			v.put(e.getKey(), value);
		}
	}

	public void MinusOnW(SuffStatsClass other) {
		for (Entry<String, Double> e : other.w.entrySet()) {
			double tar = e.getValue();
			if (!w.containsKey(e.getKey())) System.out.println(e.getKey());
			double src = w.get(e.getKey());
			double value = src - tar;
			w.put(e.getKey(), value);
		}
	}
	
	public void AddOnU(SuffStatsClass other) {
		for (Entry<String, Double> e : other.u.entrySet()) {
			double tar = e.getValue();
			double src = u.get(e.getKey());
			double value = src + tar;
			u.put(e.getKey(), value);
		}
	}

	// --------------------------------------------------------------------

	public void Clear() {
		for (Entry<String, double[]> e : v.entrySet()) {
			double[] value = e.getValue();
			Arrays.fill(value, 0);
			v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : w.entrySet()) {
			w.put(e.getKey(), 0.0);
		}
		
		for (Entry<String, Double> e : u.entrySet()) {
			u.put(e.getKey(), 0.0);
		}
	}

	public void InitializeByZeros(ParaClass para) {
		for (Entry<String, double[]> e : para.v.entrySet()) {
			double[] value = e.getValue().clone();
			Arrays.fill(value, 0);
			v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : para.w.entrySet()) {
			w.put(e.getKey(), 0.0);
		}

		for (Entry<String, Double> e : para.u.entrySet()) {
			u.put(e.getKey(), 0.0);
		}
	}

	// --------------------------------------------------------------------
	
	public void display(GetDataDomain gdd) {
		int nWords = gdd.wordDic.size();
		int nStates = gdd.labelDic.size();
		int nFeatures = gdd.nFeatures;
		IndexMap im = new IndexMap(nStates, nWords, nFeatures);
		
		// for v
		System.out.println("------------------Label------------------");
		String factorName = "Label";
		double[] weights = v.get(factorName);
		
		for (Entry<String, String> e : gdd.labelDic.entrySet()) {
			int stateID = Integer.valueOf(e.getValue());
			int index = im.GetIndexState(stateID);
			System.out.println(e.getKey() + "\t" + weights[index]);
		}
		
		System.out.println("------------------Label_Token------------------");
		factorName = "Label_Token";
		weights = v.get(factorName);
		int wordID = 666;
		for (Entry<String, String> e : gdd.labelDic.entrySet()) {
			int stateID = Integer.valueOf(e.getValue());
			int index = im.GetIndexStateToken(stateID, wordID);
			System.out.println(e.getKey() + "\t" + weights[index]);
		}

		System.out.println("------------------Label_features------------------");
		factorName = "Label_Features";
		String[] features = new String[] {
				"HasNoMoreThanTenWords", "!HasNoMoreThanTenWords", 
				"IsBioCandidate", 
				"HasWordForEducation", "StartWithEducation", "PrecededByEducationKeywords", 
				"HasWordForEmployment", "StartWordForEmployment", 
				"HasWordSpeaker", "HasWordForPresentation", "StartWordForPresentation", "PrecededByPresentationKeywords",
				"HasWordForAward", "HasWordForAwardEarly", 
				"HasAtLeastTwoNamesSideBySide", 
				"HasNumber", "StartWordForPhone", "HasPhoneKeywords", 
				"StartWordForFax", 
				"HasWordForEmail", "IsEmailPattern", "HasEmailKeywords", "PrecededByEmailKeywords", 
				"HasLocation", "StartWordForAddress", "MatchStreet", "MatchStateZip", "StartNumberWordCapitalized", "SurroundedByAddress", 
				"IsName", "StartWithOtherEntityName & !HasEntityName", "StartSingularAndHasEntityName"};
		System.out.println(features.length);
		weights = v.get(factorName);
		int stateID = Integer.valueOf(gdd.labelDic.get("Bio"));
		for (int i = 0; i < nFeatures; i++) {
			int index = im.GetIndexStateFeatures(stateID, i);
			System.out.println(features[i] + "\t" + weights[index]);
		}
	}

	public void InitializeByRandom(int nStates, int nWords, int nFeatures, ConstraintClass cc, Map<String, Double> b) {
		Random r = new Random(100);

		// for v
		String factorName = "Label";
		double[] para = SetByRandom(nStates, r);
		v.put(factorName, para);

		factorName = "Label_Label";
		para = SetByRandom(nStates * nStates, r);
		v.put(factorName, para);

		factorName = "Label_Token";
		para = SetByRandom(nStates * nWords, r);
		v.put(factorName, para);

		factorName = "Label_Entity";
		para = SetByRandom(nStates * 4, r);
		v.put(factorName, para);
		
		factorName = "Label_Features";
		para = SetByRandom(nStates * nFeatures, r);
		v.put(factorName, para);

		// for w
		for (int i = 0; i < cc.constraints.size(); i++) {
			factorName = cc.constraints.get(i);
			double conf = b.get(factorName);
			//double value = r.nextDouble();
			double value = 5 * conf;
			w.put(factorName, value);
		}

		// for u
		for (int i = 0; i < cc.constraints.size(); i++) {
			factorName = cc.constraints.get(i);
			double value = r.nextDouble();
			u.put(factorName, value);
		}
	}

	private double[] SetByRandom(int n, Random r) {
		double[] value = new double[n];
		for (int i = 0; i < n; i++)
			value[i] = r.nextDouble() * 0.1;
		return value;
	}

	public double GetSize() {
		size = 0;
		for (Entry<String, double[]> e : v.entrySet())
			size += e.getValue().length;
		return size;
	}
}
