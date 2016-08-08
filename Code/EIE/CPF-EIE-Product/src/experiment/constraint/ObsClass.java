package experiment.constraint;

import java.util.List;

import experiment.graph.LineClass2;

public class ObsClass {
	public boolean HasWordSize;
	public boolean HasWordForUnstrungWeight;
	public boolean HasWordForStrungWeight;
	public boolean HasWordForBeamWidth;
	public boolean HasWordForLength;
	public boolean HasNoNumber;
	public boolean HasWordWeight;

	public double[] ReturnFeatures() {
		boolean[] booleanfeatures = new boolean[] {
				
		};
		boolean[] booleanfeatures2 = new boolean[] {
			HasWordForBeamWidth, HasWordForLength, HasWordSize
		};
		int n1 = booleanfeatures.length;
		int n2 = booleanfeatures2.length;
		double[] features = new double[n1*2+n2];
		for (int i = 0; i < n1; i++) {
			if (booleanfeatures[i]) {
				features[i*2+0] = 1;
				features[i*2+1] = 0;
			} else {
				features[i*2+0] = 0;
				features[i*2+1] = 1;
			}
		}
		for (int i = 0; i < n2; i++) {
			features[n1*2+i] = 0;
			if (booleanfeatures2[i])
				features[n1*2+i] = 1;
			else
				features[n1*2+i] = 0;
		}
		return features;
	}
	
	public void CollectObs(LineClass2 lcc) {
		//String startText = lcc.originalText.substring(0, Math.min(20, lcc.originalText.length()));

		HasWordSize = lcc.originalText.indexOf("size") > -1;
		HasWordWeight = lcc.originalText.indexOf("weight") > -1;

		// HasWordForUnstrungWeight
		HasWordForUnstrungWeight = lcc.originalText.indexOf("unstrung") > -1;

		// HasWordForStrungWeight
		HasWordForStrungWeight = lcc.originalText.indexOf("strung") > -1 && lcc.originalText.indexOf("unstrung") == -1;

		// HasWordForBeamWidth
		HasWordForBeamWidth = lcc.originalText.indexOf("beam") > -1;

		// HasWordForLength
		HasWordForLength = lcc.originalText.indexOf("length") > -1;
	}

	public void CollectObs(LineClass2 lc1, int index, List<LineClass2> data, List<ObsClass> obsList) {
		
	}
	
	public static boolean HasSameNumber(LineClass2 lc1, LineClass2 lc2) {
		boolean case1 = MatchNumber(lc1.twoDigitNumList, lc2.twoDigitNumList);
		boolean case2 = MatchNumber(lc1.threeDigitNumList, lc2.threeDigitNumList);
		boolean case3 = MatchNumber(lc1.oneDigitNumList, lc2.oneDigitNumList);
		return (case1 | case2 | case3);
	}
	
	private static boolean MatchNumber(List<Double> v1, List<Double> v2) {
		boolean flag = false;
		for (int i = 0; i < v1.size(); i++) {
			for (int j = 0; j < v2.size(); j++) {
				double diff = Math.abs(v1.get(i) - v2.get(j));
				if (diff <= 0.2)
					return true;
			}
		}
		return flag;
	}
	
	public static boolean HasLargerValue(LineClass2 lc1, LineClass2 lc2) {
		boolean case1 = (lc1.maxTwoDigitNum > lc2.minTwoDigitNum) & (lc2.minTwoDigitNum > 0 & lc1.maxTwoDigitNum > 0);
		boolean case2 = (lc1.maxThreeDigitNum > lc2.minThreeDigitNum) & (lc2.minThreeDigitNum > 0 & lc1.maxThreeDigitNum > 0);
		boolean case3 = (lc1.maxOneDigitNum > lc2.minOneDigitNum) & (lc2.minOneDigitNum > 0 & lc1.maxOneDigitNum > 0);
		return (case1 | case2 | case3); 
	}
}
