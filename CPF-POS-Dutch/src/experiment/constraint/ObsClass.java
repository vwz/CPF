package experiment.constraint;

import experiment.graph.LineClass2;

public class ObsClass {
	public boolean isCapitalized;
	public boolean isNumber;
	public boolean hasHyphen;
	
	public boolean isLast;
	public boolean precededbyComma;
	public boolean precededbyPunct;
	public boolean surroundedbyPunct;
	public boolean surffixPattern;
	
	public double[] ReturnFeatures() {
		boolean[] booleanfeatures = new boolean[]{
			isCapitalized, isNumber, hasHyphen
		};
		/*int n1 = booleanfeatures.length;
		double[] features = new double[n1*2];
		for (int i = 0; i < n1; i++) {
			if (booleanfeatures[i]) {
				features[i*2+0] = 1;
				features[i*2+1] = 0;
			}
			else {
				features[i*2+0] = 0;
				features[i*2+1] = 1;
			}
		}*/
		int n2 = booleanfeatures.length;
		double[] features = new double[n2];
		for (int i = 0; i < n2; i++) {
			if (booleanfeatures[i])
				features[i] = 1;
			else 
				features[i] = 0;
		}
		
		return features;
	}

	public void CollectObs(LineClass2 lcc, int i, int n) {
		this.isCapitalized = lcc.isCapitalized;
		this.hasHyphen = lcc.hasHyphen;
		this.hasHyphen = lcc.hasHyphen;
		
		if (getslot(i, n) >= 3)
			isLast = true;
		else
			isLast = false;
		
		this.precededbyComma = lcc.precededbyComma;
		this.precededbyPunct = lcc.precededbyPunct;
		this.surroundedbyPunct = lcc.surroundedbyPunct;
		this.surffixPattern = lcc.surffixPattern;
	}
	
	private int getslot(int i, int n) {
		return (int) Math.floor(((double) i / (double) n) / (1.0 / 5));
	}
}
