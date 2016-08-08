package experiment.parameter;

import java.util.HashMap;
import java.util.Map;

public class metaParaClass {
	public int maxIter;
	public int loopymaxIter;
	public double stepLen;
	public int burninIter;
	public int afterburninIter;
	public double gamma;
	public double alpha1;
	public double alpha2;
	public double alpha3;
	
	// constraint properties
	public Map<String, Double> b; // confidence for every constraint, fixed once initialized
	public Map<String, Double> eps; // constraint threshold for y-type constraint, optimized after initialized
	public double rho; // constraint pruning approximation function's parameter, fixed once initialized
	public Map<String, Double> pi; // constraint threshold prior for y-type constraint, fixed once initialized (based on labeled data)
	public Map<String, Map<String, Double>> m_unlabeled; // constraint candidate number for y-type constraint, fixed once initialized (based on unlabeled data)
	
	public metaParaClass() {
		//this.maxIter = 100; 
		this.maxIter = 50;
		this.loopymaxIter = 300;
		this.stepLen = 0.01;
		this.burninIter = 100;
		this.afterburninIter = 50;
		this.gamma = 0.001;
		this.alpha1 = 0.01;
		/** change 5: try smaller alpha2 **/
		this.alpha2 = 1; // 1
		this.alpha3 = 0.01; // 1
		
		this.b = new HashMap<String, Double>();
		this.eps = new HashMap<String, Double>();
		/** change 7: try a closer approximation function **/
		this.rho = 10; // 10
		this.pi = new HashMap<String, Double>();
		this.m_unlabeled = new HashMap<String, Map<String, Double>>();
	}
	
	public void adjustStepLen(int iter, double init) {
		this.stepLen = init / (1 + Math.log(1 + (double) iter));
	}
}
