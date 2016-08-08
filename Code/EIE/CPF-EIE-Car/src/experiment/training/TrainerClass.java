package experiment.training;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import experiment.constraint.ConstraintClass;
import experiment.constraint.ObsClass;
import experiment.graph.GetDataDomain;
import experiment.graph.dataStructure;
import experiment.inference.EdgeStructClass;
import experiment.inference.IndexMap;
import experiment.inference.InferClass;
import experiment.inference.MergedEdgeStructClass;
import experiment.parameter.ParaClass;
import experiment.parameter.metaParaClass;

public class TrainerClass {	
	public ParaClass param;
	private metaParaClass metaPara;
	private IndexMap im;
	private GetDataDomain gdd;
	private ConstraintClass cc;

	public TrainerClass(GetDataDomain gdd, IndexMap im, metaParaClass mpc, ParaClass param, ConstraintClass cc) {
		this.gdd = gdd;
		this.im = im;
		this.metaPara = mpc;
		this.param = param;
		this.cc = cc;
	}
	
	/**
	 * This is to update the gradient, given a labeled entity: the gradient is h(X,Y) - E_p[h(X,Y)].
	 * Step 1: Loopy belief propagation to get p(Y|X)
	 * Step 2: Compute the gradients
	 */
	public void TrainLabeled(dataStructure ds, EdgeStructClass edgeStruct) {
		// optimize P
		Objective obj = new Objective(im, metaPara, null, edgeStruct, null);
		InferClass inferP = obj.ReturnInferP(ds, param, gdd.labelDic);
		obj.OptimizeP_Labeled(ds, param, gdd.labelDic, inferP);
		
		param.MinusOnV(obj.gParam.Multiply(metaPara.stepLen / gdd.nLabeled)); // gradient descent

		// no need to optimize Q on labeled data
	}
	
	/**
	 * This is to update the gradient, given a labeled entity: the gradient is h(X,Y) - E_p[h(X,Y)].
	 * Step 1: Loopy belief propagation to get p(Y|X)
	 * Step 2: Compute the gradients
	 */
	public void TrainLabeled(dataStructure ds, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, MergedEdgeStructClass mesc, List<ObsClass> obsList) {
		// optimize P
		Objective obj = new Objective(im, metaPara, cc, edgeStruct, obsList);
		InferClass inferP = obj.ReturnInferP(ds, param, gdd.labelDic);
		obj.OptimizeP_Labeled(ds, param, gdd.labelDic, inferP);
		
		param.MinusOnV(obj.gParam.Multiply(metaPara.stepLen / gdd.nLabeled)); // gradient descent
		
		// no need to optimize Q and threshold prior on labeled data
	}

	public void TrainUnlabeled(dataStructure ds, EdgeStructClass edgeStruct, EdgeStructClass edgeStructCons, MergedEdgeStructClass mesc, List<ObsClass> obsList, Map<String, Double> m_cs) {
		Objective obj = new Objective(im, metaPara, cc, edgeStruct, edgeStructCons, mesc, obsList);
		InferClass inferP = obj.ReturnInferP(ds, param, gdd.labelDic);
		/** change 9: add inferP to do filtering **/
		//InferClass inferQ = obj.ReturnInferQ(ds, param, gdd.labelDic);
		InferClass inferQ = obj.ReturnInferQ(ds, param, gdd.labelDic, inferP);
		
		// optimize Q
		obj.OptimizeQ_Unlabeled(ds, param, gdd.labelDic, inferP, inferQ);
		param.AddOnW(obj.gParam.Multiply(metaPara.stepLen / gdd.nUnlabeled)); // gradient ascent for maximization, 0.001
		
		// optimize P and threshold at the same time for efficiency
		//// part I: alpha1 for P
		Objective obj1 = new Objective(im, metaPara, cc, edgeStruct, edgeStructCons, mesc, obsList);
		obj1.OptimizeP_Unlabeled(ds, param, gdd.labelDic, inferP, inferQ);
		
		//// part II: alpha2 and alpha3 for both P and U
		Objective obj2 = new Objective(im, metaPara, cc, edgeStruct, edgeStructCons, mesc, obsList);
		obj2.OptimizePU_Prune(ds, param, gdd.labelDic, m_cs, inferP, inferQ);

		param.MinusOnV(obj1.gParam.Add(obj2.gParam).Multiply(metaPara.stepLen / gdd.nUnlabeled)); // gradient descent for minimization
		param.MinusOnU(obj2.gParam.Multiply(metaPara.stepLen / gdd.nUnlabeled));
		UpdateThreshold();
	}
	
	private void display(ParaClass tmp) {
		System.out.println("------ u gradient update ------");
		for (Entry<String, Double> e : tmp.u.entrySet()) {
			String factorName = e.getKey();
			System.out.println(factorName + "\t" + param.u.get(factorName) + "\t" + e.getValue());
		}
	}
	
	private void UpdateThreshold() {
		for (Entry<String, Double> e : metaPara.eps.entrySet()) {
			double u = param.u.get(e.getKey());
			//System.out.println("update threshold: " + e.getKey() + " = " + param.sigmoid(u));
			metaPara.eps.put(e.getKey(), Math.log(param.sigmoid(u)));
		}
	}
	
	public Map<String, Double> ReturnThreshold() {
		return metaPara.eps;
	}
}
