package experiment.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public List<Double> numberList;
	public List<Double> oneDigitNumList;
	public List<Double> twoDigitNumList;
	public List<Double> threeDigitNumList;
	public double minOneDigitNum;
	public double maxOneDigitNum;
	public double minTwoDigitNum;
	public double maxTwoDigitNum;
	public double minThreeDigitNum;
	public double maxThreeDigitNum;

	public String originalText;
	public String rawText;
	
	public double[] features;
	
	public boolean success;
	public double conf;

	public LineClassConsistency() {
		wordList = new ArrayList<String>();
		wordIDList = new ArrayList<String>();
		countList = new ArrayList<Integer>();
		countIDList = new ArrayList<Double>();
		numberList = new ArrayList<Double>();
		oneDigitNumList = new ArrayList<Double>();
		twoDigitNumList = new ArrayList<Double>();
		threeDigitNumList = new ArrayList<Double>();
		success = false;
		conf = 1.0 / 5.0;
		minOneDigitNum = -1;
		maxOneDigitNum = -1;
		minTwoDigitNum = -1;
		maxTwoDigitNum = -1;
		minThreeDigitNum = -1;
		maxThreeDigitNum = -1;
		features = new double[]{0, 0};
	}

	public void Clear() {
		label = "";
		predLabel = "";
		pageID = "";
		lineID = -1;
		wordList.clear();
		countList.clear();
		wordIDList.clear();
		countIDList.clear();
		numberList.clear();
		oneDigitNumList.clear();
		twoDigitNumList.clear();
		threeDigitNumList.clear();
		minTwoDigitNum = -1;
		maxTwoDigitNum = -1;
		minThreeDigitNum = -1;
		maxThreeDigitNum = -1;
		success = false;

		originalText = "";
		rawText = "";
		features = new double[]{0, 0};
	}

	/**
	 * This is used in the graph construction phase, in order to check the content similarity
	 * @param line
	 * @param labelDic
	 */
	public void ParseLine(String line) {
		String[] segs = line.split("\t\\|\\|\t", -1);
		
		// segs[0] is: "entityID pageID lineIndex"
		String[] tmp = segs[0].split(" ");
		if (tmp.length != 3)
			return;
		entityID = tmp[0];
		pageID = tmp[1];
		lineID = Integer.valueOf(tmp[2]).intValue();
		
		// segs[1] is label (which should not be empty)
		label = segs[1];
		predLabel = "";
		
		// segs[2] is the original text
		originalText = segs[2];
		originalText = originalText.trim();
		rawText = originalText;
		originalText = originalText.toLowerCase();

		ParseNumbers(originalText);
		
		// segs[3] is: "word:count word:count ... word:count"
		ParseWordCount(segs[3]);
		
		success = true;
	}
	
	private void ParseNumbers(String content) {
		String content2 = content;
		Pattern pattern = Pattern.compile("\\d+(\\.\\d+){2,}");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find())
			content2 = content2.replace(matcher.group(), "");
		
		pattern = Pattern.compile("\\s+1\\s*\\/\\s*2");
		matcher = pattern.matcher(content2);
		while (matcher.find()) {
			String number = matcher.group();
			content2 = content2.replace(number, ".5");
		}
		
		pattern = Pattern.compile("\\d+(\\.\\d+)*");
		matcher = pattern.matcher(content2);
		while (matcher.find()) {
			if (!matcher.group().matches("^0.*"))
				numberList.add(Double.valueOf(matcher.group()));
		}
		
		// get two-digit and three-digit number list
		for (int i = 0; i < numberList.size(); i++) {
			double value = numberList.get(i);
			if (value >= 0 && value < 10)
				oneDigitNumList.add(value);
			else if (value >= 10 && value < 100)
				twoDigitNumList.add(value);
			else if (value >= 100 && value < 1000)
				threeDigitNumList.add(value);
		}
		
		// get minmax values
		double[] minmax = GetMinMax(oneDigitNumList);
		minOneDigitNum = minmax[0];
		maxOneDigitNum = minmax[1];
		minmax = GetMinMax(twoDigitNumList);
		minTwoDigitNum = minmax[0];
		maxTwoDigitNum = minmax[1];
		minmax = GetMinMax(threeDigitNumList);
		minThreeDigitNum = minmax[0];
		maxThreeDigitNum = minmax[1];
	}
	
	private double[] GetMinMax(List<Double> value) {
		double[] minmax = new double[] {-1, -1};
		if (value.size() == 0)
			return minmax;
		double min = value.get(0);
		double max = value.get(0);
		for (int i = 0; i < value.size(); i++) {
			double tmp = value.get(i);
			if (tmp > max)
				max = tmp;
			if (tmp < min)
				min = tmp;
		}
		minmax[0] = min;
		minmax[1] = max;
		return minmax;
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
		String[] wordCount = words.split(" ");
		for (int i = 0; i < wordCount.length; i++) {
			String[] tmp = wordCount[i].split(":");
			if (tmp.length!=2)
				continue;

			String word = tmp[0];
			int count = Integer.valueOf(tmp[1]);

			wordList.add(word); // wordList keeps the raw words
			countList.add(count); // countList still keeps the raw counts
		}
	}
}
