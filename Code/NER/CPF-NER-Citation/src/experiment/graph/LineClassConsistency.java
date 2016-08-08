package experiment.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LineClassConsistency {
	public String label;
	public String predLabel;
	public List<String> wordList;
	public List<String> wordIDList;
	public List<Double> countList;
	public boolean capitalized;
	public boolean inleft4;
	public boolean inright4; 
	public String line;
	public String pos;
	public boolean isPunctuation;
	public boolean isjournal;
	public boolean isbooktitle;

	public double[] features;

	public boolean success;

	public LineClassConsistency() {
		wordList = new ArrayList<String>();
		wordIDList = new ArrayList<String>();
		countList = new ArrayList<Double>();
		capitalized = false;
		inleft4 = false;
		inright4 = false;
		success = false;
		features = new double[]{0,0};
		line = "";
		pos = "";
		isPunctuation = false;
		isjournal = false;
		isbooktitle = false;
	}

	public void Clear() {
		label = "";
		predLabel = "";
		wordList.clear();
		countList.clear();
		wordIDList.clear();
		capitalized = false;
		inleft4 = false;
		inright4 = false;
		success = false;
		features = new double[]{0,0}; // just set dimension as 2 for simplicity
		line = "";
		pos = "";
		isPunctuation = false;
		isjournal = false;
		isbooktitle = false;
	}

	/**
	 * This is used in the graph construction phase, in order to check the content similarity
	 * fea[i] = fea_token[i] + "|" + fea_lefttoken[i] + "|" + fea_righttoken[i] + "|" 
			+ fea_capitalized[i] + "|" + fea_inleft4[i] + "|" + fea_inright4[i] + "|" + fea_pos[i];
	 * @param line
	 * @param labelDic
	 */
	public void ParseLine(String line) {
		this.line = line;
		
		String[] segs = line.split("\t");

		// segs[1] is label (which should not be empty)
		label = segs[1];
		predLabel = "";

		String[] tokens = segs[2].split("\\|");

		// current_token
		String tmp = tokens[0];
		isjournal = (tmp.equalsIgnoreCase("journal") | tmp.equalsIgnoreCase("transaction") | tmp.equalsIgnoreCase("trans."));
		isbooktitle = (tmp.equalsIgnoreCase("conference") | tmp.equalsIgnoreCase("conf.") | tmp.equalsIgnoreCase("proceedings") | tmp.equalsIgnoreCase("proc."));
		
		if (tmp.matches("[0-9]+")) 
			tmp = "number_size" + tmp.length();
		if (tmp.matches("[0-9]+-[0-9]+")) 
			tmp = "number-number";
		String token = tmp;
		wordList.add(token);
		countList.add(1.0);

		// isPunctuation
		boolean exception = tokens[0].equals(":") | tokens[tokens.length - 1].equals("``") | tokens[tokens.length - 1].equals("''");
		if (tokens[0].matches("\\p{Punct}+") && !exception) // \\p{P}+
			isPunctuation = true;

		// prev_token
		tmp = tokens[1];
		if (tmp.matches("[0-9]+")) 
			tmp = "number_size" + tmp.length();
		if (tmp.matches("[0-9]+-[0-9]+")) 
			tmp = "number-number";
		token = "prev_" + tmp;
		wordList.add(token);
		countList.add(1.0);

		// next_token
		tmp = tokens[2];
		if (tmp.matches("[0-9]+")) 
			tmp = "number_size" + tmp.length();
		if (tmp.matches("[0-9]+-[0-9]+")) 
			tmp = "number-number";
		token = "next_" + tmp;
		wordList.add(token);
		countList.add(1.0);
		
		// capitalized
		capitalized =  Boolean.valueOf(tokens[3]);
		
		// inleft4
		inleft4 =  Boolean.valueOf(tokens[4]);
		
		// inright4
		inright4 = Boolean.valueOf(tokens[5]);

		// pos
		tmp = tokens[tokens.length - 1];
		if (tmp.matches("\\p{Punct}+")) // \\p{P}+
			tmp = "punctuation";
		this.pos = "pos_" + tmp;
		wordList.add(pos);
		countList.add(1.0);
		
		success = true;
	}

	public void SetWordID(Map<String, String> wordDic) {
		for (int i = 0; i < wordList.size(); i++) {
			String word = wordList.get(i);
			if (wordDic.containsKey(word)) {
				wordIDList.add(wordDic.get(word)); // wordIDList keeps the global word IDs
			}
		}
	}

	public void SetPredLabel() {
		predLabel = label;
	}
}
