package experiment.specs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpecNounClass {
	public Map<String, Set<String>> MapAttrNoun;
	//private Map<String, String> stopwordDic;

	public SpecNounClass() {
		MapAttrNoun = new HashMap<String, Set<String>>();
		//stopwordDic = new HashMap<String, String>();
	}

	public void Process(String dir) { 
		//LoadStopWords();
		
		String fn = dir + "labeled_attr_terms.txt";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
			String line = "";
			String attribute = "";
			while ((line = br.readLine()) != null) {
				if (line.indexOf("attribute=") > -1) {
					String[] segs = line.split("=");
					attribute = segs[1];

					Set<String> set = new HashSet<String>();
					MapAttrNoun.put(attribute, set);
				}
				else {
					/*String out = "";
					String[] segs = line.split(" ");
					for (int k = 0; k < segs.length; k++) {
						if (!stopwordDic.containsKey(segs[k]))
							out += segs[k] + " ";
					}
					MapAttrNoun.get(attribute).add(out.trim());*/
					MapAttrNoun.get(attribute).add(line);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*private void LoadStopWords() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("accessories/StopWords")));
			
			String line = "";
			while ((line=br.readLine()) != null) {
				String[] segs = line.split("\\|");
				if (segs[0].length() > 0) {
					String stopword = segs[0].trim();
					if (!stopword.isEmpty()) {
						if (!stopwordDic.containsKey(stopword)) {
							stopwordDic.put(stopword, "");
						}
					}
				}
			}
			br.close();
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}*/
}
