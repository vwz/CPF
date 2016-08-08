package experiment.inference;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ObsClass;
import experiment.decoder.InvertedIndexClass;
import experiment.graph.dataStructure;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;
import experiment.suffstats.ComputeSufficientStats;
import experiment.suffstats.SuffStatsClass;

public class VarPotentialClass {
	private ConstraintClass cc;
	private metaParaClass mpc;

	public VarPotentialClass(ConstraintClass cc, metaParaClass mpc) { 
		this.cc = cc;
		this.mpc = mpc;
	}

	/**
	 * This is to get the potential for each variable in Gibbs sampling
	 * @param ds
	 * @param i
	 * @param yprop
	 * @param suffstats
	 * @param edgeStruct
	 * @param obsList
	 * @param iic
	 * @param cc
	 * @param css
	 * @param param
	 * @return
	 */
	public double MakeVarPotential(dataStructure ds, int i, String yprop, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, List<ObsClass> obsList, InvertedIndexClass iic, ComputeSufficientStats css, ParaClass param, double p) {
		// Initialize the sufficient statistics
		SuffStatsClass suffstats = new SuffStatsClass();
		suffstats.Initialize(param);

		// go through each instance, and compute sufficient statistics for v parameter
		css.GetSuffStatsForV(ds, i, yprop, suffstats, edgeStruct);
		css.GetSuffStatsForW(ds, i, yprop, suffstats, edgeStructCons, obsList, iic, cc, mpc, p);

		// merge the sufficient statistics, because v is vector and we use some sparse representation during accumulation
		css.MergeSuffStatsForV(suffstats);
		Map<String, Double> conf = mpc.b;
		css.MergeSuffStatsForW(suffstats, conf);

		// Compute the node potential
		double value = ComputePot(suffstats, param);

		return value;
	}

	public double MakeVarPotentialForV(dataStructure ds, int i, String yprop, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, List<ObsClass> obsList, InvertedIndexClass iic, ComputeSufficientStats css, ParaClass param, double p) {
		// Initialize the sufficient statistics
		SuffStatsClass suffstats = new SuffStatsClass();
		suffstats.Initialize(param);

		// go through each instance, and compute sufficient statistics for v parameter
		css.GetSuffStatsForV(ds, i, yprop, suffstats, edgeStruct);

		// merge the sufficient statistics, because v is vector and we use some sparse representation during accumulation
		css.MergeSuffStatsForV(suffstats);

		// Compute the node potential
		double value = ComputePotV(suffstats, param);

		return value;
	}
	
	public double MakeVarPotentialForW(dataStructure ds, int i, String yprop, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, List<ObsClass> obsList, InvertedIndexClass iic, ComputeSufficientStats css, ParaClass param, double p) {
		// Initialize the sufficient statistics
		SuffStatsClass suffstats = new SuffStatsClass();
		suffstats.Initialize(param);

		// go through each instance, and compute sufficient statistics for v parameter
		css.GetSuffStatsForW(ds, i, yprop, suffstats, edgeStructCons, obsList, iic, cc, mpc, p);

		// merge the sufficient statistics, because v is vector and we use some sparse representation during accumulation
		Map<String, Double> conf = mpc.b;
		css.MergeSuffStatsForW(suffstats, conf);

		// Compute the node potential
		double value = ComputePotW(suffstats, param);

		return value;
	}

	/**
	 * Compute the variable potential by considering both v and w (which is f - b * g)
	 * @param suffstats
	 * @param param
	 * @return
	 */
	public double ComputePot(SuffStatsClass suffstats, ParaClass param) {
		double pot = 0;
		for (Entry<String, List<double[]>> e : suffstats.v.entrySet()) {
			List<double[]> valueList = e.getValue();
			double[] weights = param.v.get(e.getKey());
			for (int i = 0; i < valueList.size(); i++) {
				int index = (int) valueList.get(i)[0];
				double count = valueList.get(i)[1];
				pot += count * weights[index];
			}
		}

		for (Entry<String, Double> e : suffstats.w.entrySet()) {
			double count = e.getValue();
			double weights = param.w.get(e.getKey());
			pot += (count * weights);
		}

		//if (pot > 20) pot = 20;

		return pot;
	}
	
	public double ComputePotV(SuffStatsClass suffstats, ParaClass param) {
		double pot = 0;
		for (Entry<String, List<double[]>> e : suffstats.v.entrySet()) {
			List<double[]> valueList = e.getValue();
			double[] weights = param.v.get(e.getKey());
			for (int i = 0; i < valueList.size(); i++) {
				int index = (int) valueList.get(i)[0];
				double count = valueList.get(i)[1];
				pot += count * weights[index];
			}
		}

		return pot;
	}
	
	public double ComputePotW(SuffStatsClass suffstats, ParaClass param) {
		double pot = 0;

		/** change 10: remove w in decoding to debug **/
		for (Entry<String, Double> e : suffstats.w.entrySet()) {
			double count = e.getValue();
			double weights = param.w.get(e.getKey());
			pot += (count * weights);
		}

		return pot;
	}
}
