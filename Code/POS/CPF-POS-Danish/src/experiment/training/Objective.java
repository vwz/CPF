package experiment.training;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ObsClass;
import experiment.graph.dataStructure;
import experiment.inference.ConstraintGraphPotentialClass;
import experiment.inference.EdgeStructClass;
import experiment.inference.GraphPotentialClass;
import experiment.inference.IndexMap;
import experiment.inference.InferClass;
import experiment.inference.MergedEdgeStructClass;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;
import experiment.suffstats.SuffStatsClass;

public class Objective {
	private IndexMap im;
	private EdgeStructClass edgeStruct;
	private EdgeStructClass edgeStructCons;
	private MergedEdgeStructClass mesc;
	private List<ObsClass> obsList;
	public double loss;
	public ParaClass gParam;
	private metaParaClass metaPara;
	private ConstraintClass cc;
	public double logZ;

	public Objective(IndexMap im, metaParaClass metaPara, ConstraintClass cc, EdgeStructClass edgeStruct, List<ObsClass> obsList) {
		this.im = im;
		this.edgeStruct = edgeStruct;
		this.obsList = obsList;
		this.loss = 0;
		this.gParam = new ParaClass();
		this.metaPara = metaPara;
		this.cc = cc;
		this.logZ = 0;
	}
	
	public Objective(IndexMap im, metaParaClass metaPara, ConstraintClass cc, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, MergedEdgeStructClass mesc, List<ObsClass> obsList) {
		this.im = im;
		this.edgeStruct = edgeStruct;
		this.edgeStructCons = edgeStructCons;
		this.mesc = mesc;
		this.obsList = obsList;
		this.loss = 0;
		this.gParam = new ParaClass();
		this.metaPara = metaPara;
		this.cc = cc;
		this.logZ = 0;
	}

	public void Clear() {
		this.loss = 0;
		gParam.Clear();
		logZ = 0;
	}

	/**
	 * This is to do loopy BP is done on the general feature's edge structure
	 * @param ds
	 * @param param
	 * @param labelDic
	 * @return
	 */
	private InferClass LoopyBP_P(dataStructure ds, ParaClass param, Map<String, String> labelDic) {
		// Make potentials but not differentiating nodes and edges
		GraphPotentialClass gpc = new GraphPotentialClass(labelDic, im);
		gpc.MakePotentials(ds, edgeStruct, param); // 1) make potential for each node and edge, 2) obtain the sufficient statistics for each parameter
		
		// Infer by loopy BP
		int nNodes = edgeStruct.nNodes;
		int nEdges = edgeStruct.nEdges;
		int nStates = labelDic.size();
		InferClass infer = new InferClass(nNodes, nStates, nEdges, metaPara.loopymaxIter);
		infer.InferLoopyBP(gpc.nodePot, gpc.edgePot, edgeStruct);
		logZ = infer.logZ;

		return infer;
	}
	
	/**
	 * This is to do loopy BP is done on the merged edge structure 
	 * @param ds
	 * @param param
	 * @param labelDic
	 * @return
	 */
	private InferClass LoopyBP_Q(dataStructure ds, ParaClass param, Map<String, String> labelDic) {
		// Make potentials
		ConstraintGraphPotentialClass cgpc = new ConstraintGraphPotentialClass(labelDic, im, cc, metaPara);
		cgpc.MakePotentials(ds, mesc, param, obsList); // 1) make potential for each node and edge, 2) obtain the sufficient statistics for each parameter
		
		// Infer by loopy BP
		int nNodes = mesc.mergedEdgeStruct.nNodes;
		int nEdges = mesc.mergedEdgeStruct.nEdges;
		int nStates = labelDic.size();
		InferClass infer = new InferClass(nNodes, nStates, nEdges, metaPara.loopymaxIter);
		infer.InferLoopyBP(cgpc.nodePot, cgpc.edgePot, mesc.mergedEdgeStruct);
		logZ = infer.logZ;

		return infer;
	}
	
	/** change 9: include inferP to do filtering **/
	private InferClass LoopyBP_Q(dataStructure ds, ParaClass param, Map<String, String> labelDic, InferClass inferP) {
		// Make potentials
		ConstraintGraphPotentialClass cgpc = new ConstraintGraphPotentialClass(labelDic, im, cc, metaPara);
		cgpc.MakePotentials(ds, mesc, param, obsList, inferP); // 1) make potential for each node and edge, 2) obtain the sufficient statistics for each parameter
		
		// Infer by loopy BP
		int nNodes = mesc.mergedEdgeStruct.nNodes;
		int nEdges = mesc.mergedEdgeStruct.nEdges;
		int nStates = labelDic.size();
		InferClass infer = new InferClass(nNodes, nStates, nEdges, metaPara.loopymaxIter);
		infer.InferLoopyBP(cgpc.nodePot, cgpc.edgePot, mesc.mergedEdgeStruct);
		logZ = infer.logZ;

		return infer;
	}
	
	public InferClass ReturnInferP(dataStructure ds, ParaClass param, Map<String, String> labelDic) {
		/**** Get sufficient statistics w.r.t. P ****/
		InferClass inferP = LoopyBP_P(ds, param, labelDic);
		return inferP;
	}
	
	public InferClass ReturnInferQ(dataStructure ds, ParaClass param, Map<String, String> labelDic) {
		/**** Get sufficient statistics w.r.t. Q ****/
		InferClass inferQ = LoopyBP_Q(ds, param, labelDic);
		return inferQ;
	}
	
	/** inferQ needs inferP as the input to filter factors **/
	public InferClass ReturnInferQ(dataStructure ds, ParaClass param, Map<String, String> labelDic, InferClass inferP) {
		/**** Get sufficient statistics w.r.t. Q ****/
		InferClass inferQ = LoopyBP_Q(ds, param, labelDic, inferP);
		return inferQ;
	}
	
	public ComputeConstraintGradient OptimizeQ_Unlabeled(dataStructure ds, ParaClass param, Map<String, String> labelDic, InferClass inferP, InferClass inferQ) {
		// Compute gradients for zeta(X,Y) = f(X,Y) - b * g(X,Y) for E_q[zeta(X,Y)]
		ComputeConstraintGradient ccg = new ComputeConstraintGradient(labelDic, im, cc, metaPara);
		ccg.GetGradient(ds, edgeStructCons, mesc, obsList, inferP.nodeBel, inferP.edgeBel, inferQ.nodeBel, inferQ.edgeBel, param);
		SuffStatsClass suffstatsExp = ccg.suffstatsExp;
		
		// do gradient descent: gParam = gParam - E_q[zeta(X,Y)]
		gParam.InitializeByZeros(param);
		gParam.MinusOnW(suffstatsExp); // only update w
		
		// add regularization term: gParam = gParam - alpha1 / alpha2 * w
		penalizedOnW(param);
		
		return ccg;
	}

	public void OptimizeP_Unlabeled(dataStructure ds, ParaClass param, Map<String, String> labelDic, InferClass inferP, InferClass inferQ) {
		// Compute gradients for h(X,Y)
		ComputeGradient cgP = new ComputeGradient(labelDic, im);
		cgP.GetGradient(ds, edgeStruct, inferP.nodeBel, inferP.edgeBel, param);
		SuffStatsClass suffstatsExp_P = cgP.suffstatsExp;
		
		// Compute gradients for h(X,Y)
		ComputeGradient cgQ = new ComputeGradient(labelDic, im);
		cgQ.GetGradient(ds, edgeStruct, mesc, inferQ.nodeBel, inferQ.edgeBel, param);
		SuffStatsClass suffstatsExp_Q = cgQ.suffstatsExp;

		/**** Get sufficient statistics difference ****/
		SuffStatsClass suffstats = (suffstatsExp_Q.Minus(suffstatsExp_P, "v")).Multiply(metaPara.alpha1, "v");

		// do gradient descent: gParam = gParam - alpha1 * (E_q[h(X,Y)] - E_p[h(X,Y)])
		gParam.InitializeByZeros(param);
		gParam.MinusOnV(suffstats); // only update v
	}

	public void OptimizeP_Labeled(dataStructure ds, ParaClass param, Map<String, String> labelDic, InferClass inferP) {
		// Compute gradients
		ComputeGradient cg = new ComputeGradient(labelDic, im);
		cg.GetGradient(ds, edgeStruct, inferP.nodeBel, inferP.edgeBel, param);
		SuffStatsClass suffstats = cg.suffstats.Minus(cg.suffstatsExp, "v"); // i.e., h(X,Y) - E_p[h(X,Y)]

		// do gradient descent: gParam = gParam - (h(X,Y) - E_p[h(X,Y)])
		gParam.InitializeByZeros(param);
		gParam.MinusOnV(suffstats);

		// add L2 regularization term: gParam = gParam + gamma * v
		penalizedL2OnV(param);
	}
	
	public void OptimizePU_Prune(dataStructure ds, ParaClass param, Map<String, String> labelDic, Map<String, Double> m_cs, InferClass inferP, InferClass inferQ) {
		gParam.InitializeByZeros(param);
		
		/**
		 * Preparation: Compute gradients for zeta(X,Y) = f(X,Y) - b * g(X,Y) for E_q[zeta(X,Y)]
		 */
		ComputeConstraintGradient ccg = new ComputeConstraintGradient(labelDic, im, cc, metaPara);
		ccg.GetGradient(ds, edgeStructCons, mesc, obsList, inferP.nodeBel, inferP.edgeBel, inferQ.nodeBel, inferQ.edgeBel, param); 
		Map<String, Double> Eqzeta = ccg.suffstatsExp.w;
		
		ccg.GetMetaCons(ds, edgeStruct, edgeStructCons, mesc, obsList, inferP.nodeBel, inferP.edgeBel, param, m_cs);
		Map<String, Double> piterm = ccg.suffstatsExpMetaCons.w; // piterm = tilde{pi_c} - pi_c
		
		/**
		 * optimize P on y-type constraints, for alpha2 and alpha3
		 * do gradient descent: gParam = gParam + alpha2 * E_q[zeta(X,Y)] * E_q[zeta*sigma*(1-sigma)*rho*(h-E_p[h])] ...
		 * 								+ alpha3 * (tilde{pi}_c - pi_c) * E[g*sigma*(1-sigma)*rho*(h-E_p[h])]
		 */
		ComputeConstraintGradient ccg2 = new ComputeConstraintGradient(labelDic, im, cc, metaPara);
		ccg2.GetGradientP_Ytype(ds, edgeStruct, edgeStructCons, mesc, inferP.nodeBel, inferP.edgeBel, inferQ.nodeBel, inferQ.edgeBel, param, obsList, cc, Eqzeta, piterm);
		
		//// part I: gParam + alpha2 * E_q[zeta(X,Y)] * E_q[zeta*sigma*(1-sigma)*rho*(h-E_p[h])]
		SuffStatsClass suffstats = ccg2.suffstatsExp.Multiply(metaPara.alpha2, "v");
		gParam.AddOnV(suffstats);
		
		//// part II: gParam + alpha3 * (tilde{pi}_c - pi_c) * E[g*sigma*(1-sigma)*rho*(h-E_p[h])]
		suffstats = ccg2.suffstatsExpMetaCons.Multiply(metaPara.alpha3, "v");
		gParam.AddOnV(suffstats);
		
		/**
		 * optimize U on y-type constraints, for alpha2 and alpha3
		 * do gradient descent: gParam = gParam + { E_q[zeta(X,Y)] * E_q[zeta*sigma*(1-sigma)*rho*(-1)] ...
		 * 								+ alpha3/alpha2 * (tilde{pi}_c - pi_c) * E[g*sigma*(1-sigma)*rho*(-1)] } * (1-sigma')
		 */
		ComputeConstraintGradient ccg3 = new ComputeConstraintGradient(labelDic, im, cc, metaPara);
		ccg3.GetGradientU_Ytype(ds, edgeStruct, edgeStructCons, mesc, inferP.nodeBel, inferP.edgeBel, inferQ.nodeBel, inferQ.edgeBel, param, obsList, cc, Eqzeta, piterm);

		Map<String, Double> sigterm = new HashMap<String, Double>();
		for (Entry<String, Double> e : param.u.entrySet()) {
			String factorName = e.getKey();
			double sig = 1 - param.sigmoid(e.getValue()); 
			sigterm.put(factorName, sig);
		}
		
		//// part I: gParam +  E_q[zeta(X,Y)] * E_q[zeta*sigma*(1-sigma)*rho*(-1)] * (1-sigma')
		suffstats = ccg3.suffstatsExp.ElementwiseMultiply(sigterm, "u");
		/*System.out.println("------ alpha2 part ------");
		for (Entry<String, Double> e : suffstats.u.entrySet()) {
			String factorName = e.getKey();
			System.out.println(factorName + "\t" + e.getValue());
		}*/
		gParam.AddOnU(suffstats);
		
		//// part II: gParam + alpha3/alpha2 * (tilde{pi}_c - pi_c) * E[g*sigma*(1-sigma)*rho*(-1)] * (1-sigma')
		suffstats = ccg3.suffstatsExpMetaCons.ElementwiseMultiply(sigterm, "u").Multiply(metaPara.alpha3 / metaPara.alpha2, "u");
		/*System.out.println("------ alpha3 part ------");
		for (Entry<String, Double> e : suffstats.u.entrySet()) {
			String factorName = e.getKey();
			System.out.println(factorName + "\t" + e.getValue());
		}*/
		gParam.AddOnU(suffstats);
	}
	
	private void penalizedL2OnV(ParaClass param) {
		// change param: g_v = g_v + gamma * v
		for (Entry<String, double[]> e : param.v.entrySet()) {
			double[] otherValue = e.getValue();
			if (!gParam.v.containsKey(e.getKey())) {
				double[] value = new double[otherValue.length];
				Arrays.fill(value, 0.0);
				gParam.v.put(e.getKey(), value);
			}
			double[] value = gParam.v.get(e.getKey());
			for (int i = 0; i < value.length; i++)
				value[i] += metaPara.gamma * otherValue[i];
			gParam.v.put(e.getKey(), value);
		}
	}
	
	private void penalizedOnW(ParaClass param) {
		// change param: g_w = g_w - (alpha1 / alpha2) * w
		for (Entry<String, Double> e : param.w.entrySet()) {
			double otherValue = e.getValue();
			if (!gParam.w.containsKey(e.getKey()))
				gParam.w.put(e.getKey(), 0.0);
			
			double value = gParam.w.get(e.getKey());
			value -= (metaPara.alpha1 / metaPara.alpha2) * otherValue;
			gParam.w.put(e.getKey(), value);
		}
	}
}
