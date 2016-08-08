package experiment.constraint;

import experiment.graph.LineClass2;

public class ObsClass {
	public boolean captialized;
	public boolean inleft4;
	public boolean inright4;
	public boolean pos_is_NNP;
	public boolean isPunctuation;
	public int index;
	public boolean inlefthalf;
	public boolean inonethird;
	public boolean intwothird; 
	
	public double[] ReturnFeatures() {
		boolean[] booleanfeatures = new boolean[]{
				captialized, inleft4, inright4
		};
		int n1 = booleanfeatures.length;
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
		}
		
		return features;
	}

	public void CollectObs(LineClass2 lcc, int index) {
		this.captialized = lcc.capitalized;
		this.inleft4 = lcc.inleft4;
		this.inright4 = lcc.inright4;
		
		this.pos_is_NNP = (lcc.pos.equals("pos_NNP") || lcc.pos.equals("pos_punctuation"));
		this.isPunctuation = lcc.isPunctuation;
		this.index = index;
	}
}
