package experiment.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LineClassConsistency {
	public String label;
	public String predLabel;
	public List<String> wordList;
	public List<String> wordIDList;
	public List<String> wordTypeList;
	public List<Double> countList;
	public boolean fea_match1;
	public boolean fea_match2;
	public boolean fea_match3;
	public boolean fea_match4;
	public boolean fea_match5;
	public boolean fea_name_honorifics;
	public boolean fea_time_preceed_dash; 
	public boolean fea_time_follow_dash; 
	public boolean fea_date;
	public String line;
	public String pos;
	public int sentenceId;
	
	public String word;
	public String prev_word;

	public double[] features;
	public double[] next_features;

	public boolean success;

	public LineClassConsistency() {
		wordList = new ArrayList<String>();
		wordIDList = new ArrayList<String>();
		wordTypeList = new ArrayList<String>();
		countList = new ArrayList<Double>();
		fea_match1 = false;
		fea_match2 = false;
		fea_match3 = false;
		fea_match4 = false;
		fea_match5 = false;
		fea_name_honorifics = false;
		fea_time_preceed_dash = false;
		fea_time_follow_dash = false; 
		fea_date = false;
		success = false;
		features = new double[]{0,0};
		next_features = new double[]{0,0};
		line = "";
		pos = "";
		sentenceId = -1;
	}

	public void Clear() {
		label = "";
		predLabel = "";
		wordList.clear();
		countList.clear();
		wordIDList.clear();
		wordTypeList.clear();
		fea_match1 = false;
		fea_match2 = false;
		fea_match3 = false;
		fea_match4 = false;
		fea_match5 = false;
		fea_name_honorifics = false;
		fea_time_preceed_dash = false;
		fea_time_follow_dash = false; 
		fea_date = false;
		success = false;
		features = new double[]{0,0}; // just set dimension as 2 for simplicity
		next_features = new double[]{0,0}; // just set dimension as 2 for simplicity
		line = "";
		pos = "";
		sentenceId = -1;
	}

	private String parsetoken(String token) {
		String tmp = token;
		if (token.matches("[0-9]+"))
			tmp = "token_number" + token.length();
		if (token.matches("\\p{Punct}+")) 
			tmp = "token_punctuation";
		if (token.matches("[0-9]+:[0-9]+")) {
			if (token.matches("[0-9]+:00") || token.matches("[0-9]+:30") || token.matches("[0-9]+:15") || token.matches("[0-9]+:45"))
				tmp = "token_time_preferred";
			else 
				tmp = "token_time_random";
		}
		if (token.matches("[0-9]+-[a-z]+-[0-9]+")) 
			tmp = "token_date";
		if (token.matches(".*[a-z]+\\..*\\.[a-z]+.*") || token.matches(".*@.*"))
			tmp = "token_email";
		return tmp;
	}

	/**
	 * This is used in the graph construction phase, in order to check the content similarity
	 * String fea = lemma + "|" + fea_match1[i] + "|" + fea_match2[i] + "|" + fea_match3[i] + "|" + fea_match4[i] 
					+ "|" + fea_match5[i] + "|" + fea_name_honorifics[i] + "|" + fea_time_preceed_dash[i] 
					+ "|" + fea_time_follow_dash[i] + "|" + fea_date[i] + "|" + pos;
	 * @param line
	 * @param labelDic
	 */
	public void ParseLine(String line) {
		this.line = line;

		String[] segs = line.split("\t");

		// segs[1] is label (which should not be empty)
		if (segs[1].length() == 0)
			label = "others";
		else
			label = segs[1];
		predLabel = "";

		// segs[2] is feature list
		String[] tokens = segs[2].split("\\|");

		// current_token
		word = tokens[0];
		String current_token = parsetoken(tokens[0]);
		wordList.add(current_token);
		countList.add(1.0);

		// prev_token
		prev_word = tokens[1];
		String prev_token = parsetoken(tokens[1]);
		wordList.add("prev_" + prev_token);
		countList.add(1.0);
		/*wordList.add("prev2gram_" + prev_token + "_" + current_token);
		countList.add(1.0);*/

		// next_token
		String next_token = parsetoken(tokens[2]);
		wordList.add("next_" + next_token);
		countList.add(1.0);
		/*wordList.add("next2gram_" + current_token + "_" + next_token);
		countList.add(1.0);*/

		// fea_match
		fea_match1 =  Boolean.valueOf(tokens[3]);
		fea_match2 =  Boolean.valueOf(tokens[4]);
		fea_match3 =  Boolean.valueOf(tokens[5]);
		fea_match4 =  Boolean.valueOf(tokens[6]);
		fea_match5 =  Boolean.valueOf(tokens[7]);

		// fea_name_honorifics
		fea_name_honorifics =  Boolean.valueOf(tokens[8]);

		// fea_time_preceed_dash
		fea_time_preceed_dash = Boolean.valueOf(tokens[9]);

		// fea_time_follow_dash
		fea_time_follow_dash = Boolean.valueOf(tokens[10]);

		// fea_date
		fea_date = Boolean.valueOf(tokens[11]);

		// pos
		String tmp = tokens[tokens.length - 1];
		if (tmp.matches("\\p{Punct}+")) 
			tmp = "punctuation";
		this.pos = "pos_" + tmp;
		wordList.add(this.pos);
		countList.add(1.0);

		// segs[3] is sentence index
		sentenceId = Integer.valueOf(segs[3]);

		success = true;
	}

	public void SetWordID(Map<String, String> wordDic) {
		for (int i = 0; i < wordList.size(); i++) {
			String word = wordList.get(i);
			if (wordDic.containsKey(word)) {
				wordIDList.add(wordDic.get(word)); // wordIDList keeps the global word IDs
				wordTypeList.add(word);
			}
		}
	}

	public void SetPredLabel() {
		predLabel = label;
	}
}
