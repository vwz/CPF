package experiment.constraint;

import experiment.graph.LineClass2;

public class ObsClass {
	public boolean fea_match1;
	public boolean fea_match2;
	public boolean fea_match3;
	public boolean fea_match4;
	public boolean fea_match5;
	public boolean fea_name_honorifics;
	public boolean fea_time_preceed_dash; 
	public boolean fea_time_follow_dash; 
	public boolean fea_date;
	
	public boolean precededbyhost;
	public boolean istime;
	public String time_value;
	public boolean nearNumber;
	
	public double[] ReturnFeatures() {
		boolean[] booleanfeatures = new boolean[]{
				fea_match1, fea_match2, fea_match3, fea_match4, fea_match5, fea_name_honorifics, fea_time_preceed_dash, fea_time_follow_dash, fea_date
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

	public void CollectObs(LineClass2 lcc, LineClass2 prev1_lcc, LineClass2 prev2_lcc) {
		this.fea_match1 = lcc.fea_match1;
		this.fea_match2 = lcc.fea_match2;
		this.fea_match3 = lcc.fea_match3;
		this.fea_match4 = lcc.fea_match4;
		this.fea_match5 = lcc.fea_match5;
		this.fea_name_honorifics = lcc.fea_name_honorifics;
		this.fea_time_preceed_dash = lcc.fea_time_preceed_dash;
		this.fea_time_follow_dash = lcc.fea_time_follow_dash;
		this.fea_date = lcc.fea_date;
		
		String prev_token = lcc.wordList.get(1);
		if (prev_token.indexOf("host") > -1 | prev_token.indexOf("post") > -1 | prev_token.indexOf("by") > -1) 
			precededbyhost = true;
		else
			precededbyhost = false;
		
		nearNumber = false;
		// either the current word has number, or its previous two words have number
		if (lcc.word.matches(".*[0-9]+.*")) 
			nearNumber = true;
		if (prev1_lcc != null) {
			if (prev1_lcc.word.matches(".*[0-9]+.*")) 
				nearNumber = true;
		}
		if (prev2_lcc != null) {
			if (prev2_lcc.word.matches(".*[0-9]+.*")) 
				nearNumber = true;
		}
		
		istime = false;
		time_value = "";
		// either the current token is "token_time", or its previous two tokens are "token_time"
		String token = lcc.wordList.get(0);
		if (token.startsWith("token_time")) {
			istime = true;
			time_value = lcc.word.replaceAll("\\p{Punct}", "");
		}
		if (prev1_lcc != null && !istime) {
			token = prev1_lcc.wordList.get(0);
			if (token.startsWith("token_time")) { 
				istime = true;
				time_value = prev1_lcc.word.replaceAll("\\p{Punct}", "");
			}
		}
		if (prev2_lcc != null && !istime) {
			token = prev2_lcc.wordList.get(0);
			if (token.startsWith("token_time")) {
				istime = true;
				time_value = prev2_lcc.word.replaceAll("\\p{Punct}", "");
			}
		}
	}
}
