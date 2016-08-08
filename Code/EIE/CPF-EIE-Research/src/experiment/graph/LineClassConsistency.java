package experiment.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LineClassConsistency {
	public String label;
	public String predLabel;
	public String entityID;
	public String pageID;
	public int lineID;
	public String path;
	public int nLI;
	public List<String> wordList;
	public List<String> wordIDList;
	public List<Integer> countList;
	public List<Double> countIDList;

	public String originalText;
	public String rawText;
	public String IsFromEntityPage;
	public String IsNotFromEntityPage;
	public String IsFromEntitySegment;
	public String IsNotFromEntitySegment;
	public double[] EntitySignalVector;
	public List<String> tag_org;
	public List<String> tag_date;
	public List<String> tag_person;
	public List<String> tag_location;
	public List<String> tag_noun;
	
	public double[] features;

	public boolean success;

	public LineClassConsistency() {
		wordList = new ArrayList<String>();
		wordIDList = new ArrayList<String>();
		countList = new ArrayList<Integer>();
		countIDList = new ArrayList<Double>();
		success = false;

		tag_org = new ArrayList<String>();
		tag_date = new ArrayList<String>();
		tag_person = new ArrayList<String>();
		tag_location = new ArrayList<String>();
		tag_noun = new ArrayList<String>();
		features = new double[]{0,0};
	}

	public void Clear() {
		label = "";
		predLabel = "";
		pageID = "";
		lineID = -1;
		nLI = 0;
		path = "";
		wordList.clear();
		countList.clear();
		wordIDList.clear();
		countIDList.clear();
		success = false;

		originalText = "";
		rawText = "";
		tag_org.clear();
		tag_date.clear();
		tag_person.clear();
		tag_location.clear();
		tag_noun.clear();
		IsFromEntityPage = "";
		IsNotFromEntityPage = "";
		IsFromEntitySegment = "";
		IsNotFromEntitySegment = "";
		features = new double[]{0,0}; // just set dimension as 2 for simplicity
	}

	private void ParseTags(String tags) {
		String[] segs = tags.split("\t");

		for (int i = 0; i < segs.length; i++) {
			if (segs[i].matches("ORGANIZATION: .*")) {
				String record = segs[i].replace("ORGANIZATION: ", "");
				String[] tmp = record.split("; ");
				for (int j = 0; j < tmp.length; j++) {
					String org = tmp[j].toLowerCase();
					org = org.replace("-", " "); // remove "-" to avoid duplicates
					
					boolean hasDegree = org.indexOf("bachelor") > -1 || org.indexOf("master") > -1 || 
							org.indexOf("phd") > -1 || org.indexOf("ph.d") > -1 || org.indexOf("ph. d") > -1 ||  
							org.indexOf("b.sc") > -1 || org.indexOf("b. sc") > -1 || org.indexOf("b.eng") > -1 || org.indexOf("b. eng") > -1 ||
							org.indexOf("b.s.") > -1 || org.indexOf("m.s.") > -1 || org.indexOf("b.a.") > -1 || org.indexOf("m.a.") > -1 ||
							org.indexOf("b.tech") > -1 || org.indexOf("b. tech") > -1;
					
					boolean onlyUniversity = org.equalsIgnoreCase("university") || org.equals("univ.");
						
					if (!hasDegree & !onlyUniversity)
						this.tag_org.add(org);
				}
			}
			
			if (segs[i].matches("PERSON: .*")) {
				String record = segs[i].replace("PERSON: ", "");
				String[] tmp = record.split("; ");
				for (int j = 0; j < tmp.length; j++)
					this.tag_person.add(tmp[j].toLowerCase());
			}
			
			if (segs[i].matches("DATE: .*")) {
				String record = segs[i].replace("DATE: ", "");
				String[] tmp = record.split("; ");
				for (int j = 0; j < tmp.length; j++)
					this.tag_date.add(tmp[j].toLowerCase());
			}
			
			if (segs[i].matches("LOCATION: .*")) {
				String record = segs[i].replace("LOCATION: ", "");
				String[] tmp = record.split("; ");
				for (int j = 0; j < tmp.length; j++)
					this.tag_location.add(tmp[j].toLowerCase());
				for (int j = 0; j < tmp.length; j++) {
					String org = tmp[j].toLowerCase();
					if (org.indexOf("university") > -1 || org.indexOf("institute") > -1 || org.indexOf("school") > -1 || org.indexOf("college") > -1 || org.indexOf("academy") > -1)
						this.tag_org.add(org.replace("-", " "));
				}
			}
			
			if (segs[i].matches("NOUN: .*")) {
				String record = segs[i].replace("NOUN: ", "");
				String[] tmp = record.split("; ");
				for (int j = 0; j < tmp.length; j++)
					this.tag_noun.add(tmp[j].toLowerCase());
				for (int j = 0; j < tmp.length; j++) {
					String org = tmp[j].toLowerCase();
					if (org.indexOf("university") > -1 || org.indexOf("institute") > -1 || org.indexOf("school") > -1 || org.indexOf("college") > -1 || org.indexOf("academy") > -1)
						this.tag_org.add(org.replace("-", " "));
					// special treatment
					if (org.indexOf("MAXIM RAGINSKY".toLowerCase()) > -1 || org.indexOf("FUSHENG WANG".toLowerCase()) > -1 || org.indexOf("WENYE WANG".toLowerCase()) > -1 || org.indexOf("MEHMET CAN".toLowerCase()) > -1)
						this.tag_person.add(org);
				}
			}
		}
	}
	
	/**
	 * This is used in the graph construction phase, in order to check the content similarity
	 * @param line
	 * @param labelDic
	 */
	public void ParseLine(String line) {
		String[] segs = line.split("\t\\|\\|\t");

		// segs[1] is the original text
		//styledText = segs[1]; // this is the text with styled information
		
		originalText = segs[1];
		originalText = originalText.replaceAll("\t\\#\\#(false|true)\\@\\@(false|true)\\$\\$\t", " ");
		originalText = originalText.trim();
		rawText = originalText;
		originalText = originalText.toLowerCase();
		
		// segs[2] is the tags
		if (segs.length >= 3)
			ParseTags(segs[2]);
		
		// segs[3] is FromEntityPage, in the format of \t\\#\\#(false|true)\\@\\@(false|true)\\$\\$
		IsFromEntityPage = "false";
		IsNotFromEntityPage = "false";
		if (segs.length == 5) {
			String FromEntityPage = segs[3];
			String[] tmp = FromEntityPage.split("\\@\\@");
			if (tmp.length == 2) {
				IsFromEntityPage = tmp[0].replaceAll("\\#\\#", "");
				IsNotFromEntityPage = tmp[1].replaceAll("\\$\\$", "");
			}
		}
		
		// segs[4] is FromEntitySegment, in the format of \t\\#\\#(false|true)\\@\\@(false|true)\\$\\$
		IsFromEntitySegment = "false";
		IsNotFromEntitySegment = "false";
		if (segs.length == 5) {
			String FromEntitySegment = segs[4];
			String[] tmp = FromEntitySegment.split("\\@\\@");
			if (tmp.length == 2) {
				IsFromEntitySegment = tmp[0].replaceAll("\\#\\#", "");
				IsNotFromEntitySegment = tmp[1].replaceAll("\\$\\$", "");
			}
		}
		
		EntitySignalVector = new double[4];
		if (IsFromEntityPage.equalsIgnoreCase("true")) EntitySignalVector[0] = 1;
		else EntitySignalVector[0] = 0;
		if (IsNotFromEntityPage.equalsIgnoreCase("true")) EntitySignalVector[1] = 1;
		else EntitySignalVector[1] = 0;
		if (IsFromEntitySegment.equalsIgnoreCase("true")) EntitySignalVector[2] = 1;
		else EntitySignalVector[2] = 0;
		if (IsNotFromEntitySegment.equalsIgnoreCase("true")) EntitySignalVector[3] = 1;
		else EntitySignalVector[3] = 0;
		
		// segs[0] is the original line: "pageID lineIndex----label----path \t word:count word:count ... word:count"
		line = segs[0];
		segs = line.split("----");
		if (segs.length != 3) 
			return;

		// segs[0] is "entityID pageID lineIndex"
		String[] tmp = segs[0].split(" ");
		if (tmp.length != 3)
			return;
		entityID = tmp[0];
		pageID = tmp[1];
		lineID = Integer.valueOf(tmp[2]).intValue();
		
		/*String tmpid = entityID + " " + pageID + " " + lineID;
		if (tmpid.equals("4f8e5b67e80eb0e411ff1488 1 1"))
			System.out.println("debug");*/

		// segs[1] is label (which should not be empty)
		label = segs[1];
		predLabel = "";
		
		// segs[2] is "path \t word:count word:count ... word:count"
		tmp = segs[2].split("\t");
		if (tmp.length != 2)
			return;
		path = tmp[0];

		// tmp[1] is word count list
		ParseWordCount(tmp[1]); // remove name words
		
		 // get LI count
		nLI = GetLICount(path);

		success = true;
	}
	
	public void SetWordID(Map<String, String> wordDic) {
		for (int i = 0; i < wordList.size(); i++) {
			String word = wordList.get(i);
			if (wordDic.containsKey(word)) {
				wordIDList.add(wordDic.get(word)); // wordIDList keeps the global word IDs
				double discountedCount = Math.log((double) countList.get(i)) + 1; 
				countIDList.add(discountedCount); // countIDList keeps the discounted word counts
			}
		}
	}
	
	public void SetPredLabel() {
		predLabel = label;
	}
	
	private void ParseWordCount(String words) {
		String names = tag_person.toString().toLowerCase();
		
		String[] wordCount = words.split(" ");
		for (int i = 0; i < wordCount.length; i++) {
			String[] tmp = wordCount[i].split(":");
			if (tmp.length!=2)
				continue;

			String word = tmp[0];
			int count = Integer.valueOf(tmp[1]);

			// word should be not name, and word should be not too long (due to some parsing error)
			if (names.indexOf(word) == -1 && word.length() < 16) {
				wordList.add(word); // wordList keeps the raw words
				countList.add(count); // countList still keeps the raw counts
			}
		}
	}
	
	private int GetLICount(String path2) {
		int n = 0;
		String path = path2;
		while (path.indexOf(".LI.") > -1) {
			n++;
			path = path.replace(".LI.", ".");
		}
		return n;
	}
	
	public String GetLatestTime() {
		int maxYear = 0;
		for (int i = 0; i < tag_date.size(); i++) {
			int year = Integer.valueOf(tag_date.get(i)).intValue();
			if (year > maxYear) maxYear = year;
		}
		if (maxYear > 0) return String.valueOf(maxYear);
		else return "";
	}
}
