package experiment.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LineClassConsistency {
	public String label;
	public String predLabel;
	public List<String> wordList;
	public List<String> wordIDList;
	public List<String> wordTypeList;
	public List<Double> countList;
	public boolean isCapitalized;
	public boolean hasNumber;
	public boolean hasHyphen;
	public String line;

	public double[] features;
	public double[] next_features;

	public boolean success;
	
	public boolean precededbyComma;
	public boolean precededbyPunct;
	public boolean surroundedbyPunct;
	public int position;
	public boolean surffixPattern;
	
	public boolean isPunct;
	public boolean hasPunct;
	
	public boolean isnumber;
	private Set<String> NUMtokens;
	
	private Set<String> PRONtokens;
	public boolean precededbyPron;
	public boolean isADJ;
	private Set<String> ADJtopsuffix;
	
	public boolean isShort;

	public LineClassConsistency() {
		wordList = new ArrayList<String>();
		wordIDList = new ArrayList<String>();
		wordTypeList = new ArrayList<String>();
		countList = new ArrayList<Double>();
		isCapitalized = false;
		hasNumber = false;
		hasHyphen = false;

		success = false;
		features = new double[]{0,0};
		next_features = new double[]{0,0};
		line = "";
		
		precededbyComma = false;
		precededbyPunct = false;
		surroundedbyPunct = false;
		position = -1;
		surffixPattern = false;
		isPunct = false;
		hasPunct = false;
		
		ADJtopsuffix = new HashSet<String>();
		ADJtopsuffix.add("se"); ADJtopsuffix.add("jk"); ADJtopsuffix.add("ge"); ADJtopsuffix.add("ig"); 
		ADJtopsuffix.add("ke"); ADJtopsuffix.add("le");  
		ADJtopsuffix.add("re"); ADJtopsuffix.add("he"); 
		//ADJtopsuffix.add("dt"); ADJtopsuffix.add("st"); ADJtopsuffix.add("rt"); ADJtopsuffix.add("je"); ADJtopsuffix.add("jt");  
		
		isnumber = false;
		
		NUMtokens = new HashSet<String>();
		NUMtokens.add("een"); NUMtokens.add("twee"); NUMtokens.add("drie"); NUMtokens.add("vier"); NUMtokens.add("vijf");
		NUMtokens.add("zes"); NUMtokens.add("zeven"); NUMtokens.add("acht"); NUMtokens.add("nacht"); NUMtokens.add("tien"); 
		NUMtokens.add("elf"); NUMtokens.add("twaalf"); NUMtokens.add("dertien"); NUMtokens.add("veertien"); NUMtokens.add("vijftien");
		NUMtokens.add("zestien"); NUMtokens.add("zeventien"); NUMtokens.add("achttien"); NUMtokens.add("negentien"); NUMtokens.add("vijftien"); 
		NUMtokens.add("honderd"); NUMtokens.add("miljoen"); NUMtokens.add("miljard"); NUMtokens.add("twintig"); NUMtokens.add("veertien"); 
		NUMtokens.add("hoevell"); NUMtokens.add("zoveel"); NUMtokens.add("veel"); NUMtokens.add("meer"); 
		
		NUMtokens.add("eerste"); NUMtokens.add("minder"); NUMtokens.add("tweede"); NUMtokens.add("vijfentwintig");
		NUMtokens.add("derde"); NUMtokens.add("negende"); NUMtokens.add("veertig"); NUMtokens.add("tienduizenden"); 
		NUMtokens.add("weinige");

		
		precededbyPron = false;
		
		PRONtokens = new HashSet<String>();
		PRONtokens.add("de"); PRONtokens.add("een"); PRONtokens.add("het"); PRONtokens.add("en"); PRONtokens.add(",");
		PRONtokens.add("niet"); PRONtokens.add("van"); PRONtokens.add("is"); PRONtokens.add("zijn"); PRONtokens.add("welke");
		PRONtokens.add("zo"); PRONtokens.add("die"); PRONtokens.add("in"); PRONtokens.add("met"); PRONtokens.add("zijn"); PRONtokens.add("hoe");
		PRONtokens.add("\""); PRONtokens.add("zeer"); PRONtokens.add("dat"); PRONtokens.add("voor"); PRONtokens.add("op");
		
		isADJ = false;
		isShort = false;
	}

	public void Clear() {
		label = "";
		predLabel = "";
		wordList.clear();
		countList.clear();
		wordIDList.clear();
		wordTypeList.clear();
		isCapitalized = false;
		hasNumber = false;
		hasHyphen = false;
		
		success = false;
		features = new double[]{0,0}; // just set dimension as 2 for simplicity
		next_features = new double[]{0,0}; // just set dimension as 2 for simplicity
		line = "";
		
		precededbyComma = false;
		precededbyPunct = false;
		surroundedbyPunct = false;
		position = -1;
		surffixPattern = false;
		isPunct = false;
		hasPunct = false;
		isnumber = false;
		precededbyPron = false;
		
		PRONtokens.clear();
		NUMtokens.clear();
		
		isADJ = false;
		isShort = false;
	}

	private String parsetoken(String token) {
		String tmp = token;
		if (token.matches("[0-9]+"))
			tmp = "token_number";
		else if (token.matches("\\p{Punct}+")) 
			tmp = "token_punctuation";
		else if (token.replaceAll("\\p{Punct}", "").replaceAll("[0-9]", "").length() == 0)
			tmp = "token_numpunct";
		return tmp;
	}

	/**
	 * @param line
	 * @param labelDic
	 */
	public void ParseLine(String line, int position) {
		this.line = line;
		this.position = position;

		String[] segs = line.split("\t");

		//String output = cpostags.get(i) + "\t" + word + "|" + prev_word + "|" + next_word + "|" 
		//		+ isCapitalized + "|" + isNumber + "|" + hasHyphen + "|" + surfix_2gram + "|" + surfix_3gram;
		
		// label
		label = segs[0];
		predLabel = "";

		// feature list
		String[] tokens = segs[1].split("\\|");

		// current_token
		String current_token = parsetoken(tokens[0]);
		wordList.add(current_token);
		countList.add(1.0);
		if (current_token.equals("token_punctuation"))
			isPunct = true;
		String tmp = tokens[0].replaceAll("\\p{Punct}", "");
		if (tmp.length() > 0 && tmp.length() < tokens[0].length())
			hasPunct = true;
		
		
		isShort = current_token.length() < 4;

		// prev_token
		String prev_token = parsetoken(tokens[1]);
		wordList.add("prev_" + prev_token);
		countList.add(1.0);
		
		if (tokens[1].equals(","))
			precededbyComma = true;
		if (prev_token.equals("token_punctuation"))
			precededbyPunct = true;
		if (PRONtokens.contains(tokens[1]))
			this.precededbyPron = true;

		// next_token
		String next_token = parsetoken(tokens[2]);
		wordList.add("next_" + next_token);
		countList.add(1.0);

		surroundedbyPunct = prev_token.equals("token_punctuation") | next_token.equals("token_punctuation") | tokens[1].equals("_start_");
		//surroundedbyPunct = prev_token.equals("token_punctuation") | tokens[1].equals("_start_");
		
		// fea_match
		isCapitalized =  Boolean.valueOf(tokens[3]);
		hasNumber =  Boolean.valueOf(tokens[4]);
		hasHyphen =  Boolean.valueOf(tokens[5]);

		isnumber = hasNumber | NUMtokens.contains(tokens[0]);
		
		//isADJ = precededbyPron;
		isADJ = false;
		
		// surfix_ngram
		if (tokens.length > 6) {
			wordList.add("suffix2gram_" + tokens[6]);
			countList.add(1.0);
			
			//isADJ = isADJ & ADJtopsuffix.contains(tokens[6]);
			isADJ = ADJtopsuffix.contains(tokens[6]);
		} 
		
		if (tokens.length > 7) { 
			wordList.add("suffix3gram_" + tokens[7]);
			countList.add(1.0);
		}

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
