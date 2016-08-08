package experiment.suffstats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import experiment.parameter.ParaClass;

public class SuffStatsClass {
	public Map<String, List<double[]>> v; // this is for data-dependent features: each type of feature (e.g. Label_Label) contains multiple parameters
	public Map<String, Double> w; // this is for constraint-dependent features w = function (f, g): each type of constraint (e.g. Bio_Bio_Similar) contains only one parameter (i.e. the constraint holds)
	public Map<String, Double> u; // this is for constraint-dependent features u: each type of constraint contains only one parameter
	public Map<String, Double> f; // this is for constraint-dependent features f : each type of constraint (e.g. Bio_Bio_Similar) contains only one parameter (i.e. the constraint holds)
	public Map<String, Double> g; // this is for constraint-dependent features g: each type of constraint (e.g. Bio_Bio_Similar) contains only one parameter (i.e. the constraint holds)
	
	public SuffStatsClass() {
		v = new HashMap<String, List<double[]>>();
		w = new HashMap<String, Double>();
		u = new HashMap<String, Double>();
		f = new HashMap<String, Double>();
		g = new HashMap<String, Double>();
	}

	public void Initialize(ParaClass para) {
		for (Entry<String, double[]> e : para.v.entrySet()) {
			List<double[]> value = new ArrayList<double[]>();
			v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : para.w.entrySet()) {
			Double value = new Double(0);
			w.put(e.getKey(), value);
			f.put(e.getKey(), value);
			g.put(e.getKey(), value);
		}
		
		for (Entry<String, Double> e : para.u.entrySet()) {
			Double value = new Double(0);
			u.put(e.getKey(), value);
		}
	}
	
	public void Addfg(SuffStatsClass other) {
		for (Entry<String, Double> e : other.f.entrySet()) {
			String key = e.getKey();
			Double value = e.getValue();
			if (!f.containsKey(key)) {
				f.put(key, value);
			} else {
				f.put(key, value + f.get(key));
			}
		}
		
		for (Entry<String, Double> e : other.g.entrySet()) {
			String key = e.getKey();
			Double value = e.getValue();
			if (!g.containsKey(key)) {
				g.put(key, value);
			} else {
				g.put(key, value + g.get(key));
			}
		}
	}
	
	public void Printfg(BufferedWriter bw) throws IOException {
		for (Entry<String, Double> e : f.entrySet()) {
			String key = e.getKey();
			Double value = e.getValue();
			if (!g.containsKey(key)) {
				bw.write(key + "\t" + 0);
			} else {
				if (g.get(key) != 0)
					bw.write(key + "\t" + (value / g.get(key)));
				else 
					bw.write(key + "\t" + 0);
			}
			bw.write("\n");
		}
	}
	// ---------------------------------------------------------------------

	private List<double[]> f_multiply(List<double[]> src, double x) {
		List<double[]> value = new ArrayList<double[]>();
		for (int i = 0; i < src.size(); i++) {
			double[] record = src.get(i).clone(); // record[0] = index, record[1] = count
			record[1] = record[1] * x;
			value.add(record);
		}
		return value;
	}
	
	/**
	 * Only update some subset of parameters 
	 * @param x
	 * @param option
	 * @return
	 */
	public SuffStatsClass Multiply(double x, String option) {
		SuffStatsClass out = new SuffStatsClass();

		if (option.equalsIgnoreCase("v")) {
			for (Entry<String, List<double[]>> e : v.entrySet()) {
				List<double[]> src = e.getValue();
				List<double[]> value = f_multiply(src, x);
				out.v.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("f")) {
			for (Entry<String, Double> e : f.entrySet()) {
				double src = e.getValue();
				Double value = new Double(src * x);
				out.f.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("g")) {
			for (Entry<String, Double> e : g.entrySet()) {
				double src = e.getValue();
				Double value = new Double(src * x);
				out.g.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("w")) {
			for (Entry<String, Double> e : w.entrySet()) {
				double src = e.getValue();
				Double value = new Double(src * x);
				out.w.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("u")) {
			for (Entry<String, Double> e : u.entrySet()) {
				double src = e.getValue();
				Double value = new Double(src * x);
				out.u.put(e.getKey(), value);
			}
		}
		else {
			System.err.println("unknown option for multiply");
		}

		return out;
	}

	// ---------------------------------------------------------------------

	private List<double[]> f_minus(List<double[]> src, List<double[]> tar) {
		Map<Double, Double> res = new HashMap<Double, Double>();
		for (int i = 0; i < src.size(); i++) {
			Double key = Double.valueOf(src.get(i)[0]);
			Double value = Double.valueOf(src.get(i)[1]);
			res.put(key, value);
		}
		for (int i = 0; i < tar.size(); i++) {
			Double key = Double.valueOf(tar.get(i)[0]);
			double srcValue = 0;
			if (res.containsKey(key))
				srcValue = res.get(key).doubleValue();
			Double value = Double.valueOf(srcValue - tar.get(i)[1]);
			res.put(key, value);
		}

		List<double[]> value = new ArrayList<double[]>();
		for (Entry<Double, Double> e2 : res.entrySet()) {
			double[] record = new double[] {e2.getKey().doubleValue(), e2.getValue().doubleValue()};
			value.add(record);
		}
		return value;
	}

	public SuffStatsClass Minus(SuffStatsClass other, String option) {
		SuffStatsClass out = new SuffStatsClass();

		if (option.equalsIgnoreCase("v")) {
			for (Entry<String, List<double[]>> e : v.entrySet()) {
				List<double[]> src = e.getValue();
				List<double[]> tar = other.v.get(e.getKey());
				List<double[]> value = f_minus(src, tar);
				out.v.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("f")) {
			for (Entry<String, Double> e : f.entrySet()) {
				double src = e.getValue();
				double tar = other.f.get(e.getKey());
				Double value = new Double(src - tar);
				out.f.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("g")) {
			for (Entry<String, Double> e : g.entrySet()) {
				double src = e.getValue();
				double tar = other.g.get(e.getKey());
				Double value = new Double(src - tar);
				out.g.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("w")) {
			for (Entry<String, Double> e : w.entrySet()) {
				double src = e.getValue();
				double tar = other.w.get(e.getKey());
				Double value = new Double(src - tar);
				out.w.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("u")) {
			for (Entry<String, Double> e : u.entrySet()) {
				double src = e.getValue();
				double tar = other.u.get(e.getKey());
				Double value = new Double(src - tar);
				out.u.put(e.getKey(), value);
			}
		}
		else {
			System.err.println("unknown option for minus");
		}

		return out;
	}

	// ---------------------------------------------------------------------

	public SuffStatsClass ElementwiseMultiply(Map<String, Double> other, String option) {
		SuffStatsClass out = new SuffStatsClass();

		if (option.equalsIgnoreCase("f")) {
			for (Entry<String, Double> e : f.entrySet()) {
				double value = e.getValue() * other.get(e.getKey());
				out.f.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("g")) {
			for (Entry<String, Double> e : g.entrySet()) {
				double value = e.getValue() * other.get(e.getKey());
				out.g.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("w")) {
			for (Entry<String, Double> e : w.entrySet()) {
				double value = e.getValue() * other.get(e.getKey());
				out.w.put(e.getKey(), value);
			}
		}
		else if (option.equalsIgnoreCase("u")) {
			for (Entry<String, Double> e : u.entrySet()) {
				double value = e.getValue() * other.get(e.getKey());
				out.u.put(e.getKey(), value);
			}
		}
		else {
			System.err.println("unknown option for elementwise multiply");
		}
		
		return out;
	}
	
	// ---------------------------------------------------------------------
	
	public double Multiply(Map<String, Double> w1, Map<String, Double> w2) {
		double out = 0;

		for (Entry<String, Double> e : w1.entrySet()) {
			double value = e.getValue() * w2.get(e.getKey());
			out += value;
		}
		
		return out;
	}

	public void Clear() {
		for (Entry<String, List<double[]>> e : v.entrySet()) {
			List<double[]> value = new ArrayList<double[]>();
			v.put(e.getKey(), value);
		}

		for (Entry<String, Double> e : f.entrySet()) {
			Double value = new Double(0); 
			f.put(e.getKey(), value);
		}
		
		for (Entry<String, Double> e : g.entrySet()) {
			Double value = new Double(0); 
			g.put(e.getKey(), value);
		}
		for (Entry<String, Double> e : w.entrySet()) {
			Double value = new Double(0); 
			w.put(e.getKey(), value);
		}
		for (Entry<String, Double> e : u.entrySet()) {
			Double value = new Double(0); 
			u.put(e.getKey(), value);
		}
	}
}
