package experiment.constraint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import experiment.graph.LineClass2;
import experiment.specs.SingleSpecClass;
import experiment.specs.SpecClass;
import experiment.specs.SpecNounClass;

public class ObsClass {
	public boolean HasWordForInterior;
	public boolean HasWordForExterior;
	public boolean HasWordForDriveRide;
	public boolean HasWordForReliability;
	public boolean HasWordForSafety;
	public boolean HasWordForPrice;
	public boolean HasMoney;
	public boolean HasMoreThanOneAttrsKeywords;

	public MatchClass MatchSpecForInterior;
	public MatchClass MatchSpecForExterior;
	public MatchClass MatchSpecForDriveRide;
	public MatchClass MatchSpecForReliability;
	public MatchClass MatchSpecForSafety;
	public boolean MatchMultipleAttr;

	public int MatchSpecNounForInterior;
	public int MatchSpecNounForExterior;
	public int MatchSpecNounForDriveRide;
	public int MatchSpecNounForReliability;
	public int MatchSpecNounForSafety;
	public boolean MatchNounMultipleAttr;
	public boolean HasInputBrand;
	public boolean HasOtherBrand;
	public boolean MatchAnyNoun;
	
	public boolean HasNoMoreThanSixWords;	
	public boolean SurroundIndicatorForReliability;
	public boolean SignalForMoney;
	
	public Set<String> DriveRideSet;
	public Set<Integer> NumberSet;
	public int SpecMapCount;
	public boolean IsDriveRide;
	
	public double[] ReturnFeatures() {
		boolean[] booleanfeatures = new boolean[]{
			
		};
		boolean[] booleanfeatures2 = new boolean[] {
			HasWordForInterior,
			HasWordForExterior,
			HasWordForDriveRide,
			HasWordForReliability,
			HasWordForSafety,
			HasWordForPrice,
			/*HasMoney,
			/SignalForMoney,*/
			HasMoreThanOneAttrsKeywords,
			MatchMultipleAttr,
			MatchNounMultipleAttr,
			MatchSpecNounForInterior > 0 ? true : false,
			MatchSpecNounForExterior > 0 ? true : false,
			MatchSpecNounForDriveRide > 0 ? true : false,
			MatchSpecNounForReliability > 0 ? true : false,
			MatchSpecNounForSafety > 0 ? true : false,
			HasInputBrand,
			HasOtherBrand,
			SurroundIndicatorForReliability
		};
		int n1 = booleanfeatures.length;
		int n2 = booleanfeatures2.length;
		double[] features = new double[n1*2 + n2];
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
		for (int i = 0; i < n2; i++) {
			features[n1*2 + i] = 0;
			if (booleanfeatures2[i])
				features[n1*2 + i] = 1;
			else
				features[n1*2 + i] = 0;
		}
		
		return features;
	}

	public void CollectObs(LineClass2 lcc, String name, SpecClass sc, SpecNounClass snc) {
		lcc.originalText = lcc.originalText.replaceAll("20[012]\\d", "");
		lcc.originalText = lcc.originalText.replaceAll("19\\d{2}", "");
		
		name = name.toLowerCase();
		String[] brands = new String[]{"Acura", "Alfa Romeo", "Aston Martin", "Audi", "Bentley", "BMW", "Buick", "Cadillac", "Chevrolet", "Chrysler", "Dodge", "Ferrari", "FIAT", "Fisker", "Ford", "GMC", "Honda", "Hyundai", "Infiniti", "Jaguar", "Jeep", "Kia", "Lamborghini", "Land Rover", "Lexus", "Lincoln", "Lotus", "Maserati", "Maybach", "Mazda", "McLaren", "Mercedes-Benz", "MINI", "Mitsubishi", "Nissan", "Porsche", "Ram", "Rolls-Royce", "Saab", "Scion", "smart", "Subaru", "Suzuki", "Tesla", "Toyota", "Volkswagen", "Volvo"};
		String[] tmp = name.split(" ");
		String inputBrand = tmp[1];
		if (lcc.originalText.indexOf(inputBrand) > -1)
			HasInputBrand = true;
		for (int i = 0; i < brands.length; i++) {
			brands[i] = brands[i].toLowerCase();
			if (lcc.originalText.indexOf(brands[i]) > -1 && !brands[i].equalsIgnoreCase(inputBrand)) 
				HasOtherBrand = true;
		}

		HasWordForInterior = lcc.originalText.indexOf("seat") > -1 || lcc.originalText.indexOf("rear") > -1 || lcc.originalText.indexOf("system") > -1 
				|| lcc.originalText.indexOf("control") > -1 || lcc.originalText.indexOf("interior") > -1 || lcc.originalText.indexOf("front") > -1 
				|| lcc.originalText.indexOf("row") > -1 || lcc.originalText.indexOf("cargo") > -1 || lcc.originalText.indexOf("feature") > -1; 

		HasWordForExterior = lcc.originalText.indexOf("look") > -1 || lcc.originalText.indexOf("inch") > -1 || lcc.originalText.indexOf("style") > -1 
				|| lcc.originalText.indexOf("wheel") > -1 || lcc.originalText.indexOf("body") > -1 || lcc.originalText.indexOf("door") > -1 
				|| lcc.originalText.indexOf("grill") > -1 || lcc.originalText.indexOf("trim") > -1; 

		HasWordForDriveRide = lcc.originalText.indexOf("engine") > -1 || lcc.originalText.indexOf("speed") > -1 || lcc.originalText.indexOf("drive") > -1 
				|| lcc.originalText.indexOf("power") > -1 || lcc.originalText.indexOf("automat") > -1 || lcc.originalText.indexOf("mpg") > -1 
				|| lcc.originalText.indexOf("liter") > -1 || lcc.originalText.indexOf("transmiss") > -1 || lcc.originalText.indexOf("fuel") > -1;

		HasWordForSafety = lcc.originalText.indexOf("safety") > -1 || lcc.originalText.indexOf("airbag") > -1 || lcc.originalText.indexOf("brake") > -1 
				|| lcc.originalText.indexOf("test") > -1 || lcc.originalText.indexOf("star") > -1 || lcc.originalText.indexOf("stability") > -1;		

		HasWordForPrice = lcc.originalText.indexOf("price") > -1 || lcc.originalText.indexOf("package") > -1 || lcc.originalText.indexOf("option") > -1 
				|| lcc.originalText.indexOf("$") > -1 || lcc.originalText.indexOf("dollar") > -1 || lcc.originalText.indexOf("msrp") > -1;

		HasMoney = false;
		if (lcc.originalText.indexOf("price") > -1 || lcc.originalText.indexOf("msrp") > -1 
				|| lcc.originalText.matches(".*\\(.{0,15}\\d+(,\\d{3})+.{0,15}\\).*") || lcc.originalText.matches(".*\\$\\d+(,\\d{3})*.*") 
				|| lcc.originalText.matches(".*(pay|start).{0,15}\\d+(,\\d{3})*.*")
				//|| lcc.originalText.matches(".*used.{0,10}\\d+(,\\d{3})+.*")
				|| (lcc.originalText.indexOf("package") > -1 && lcc.originalText.matches(".*\\d+(,\\d{3})+(?![ -]*(mile|rpm|km|lbs|pound)).*")))
				//|| lcc.originalText.matches(".*(used|pay|sale).{0,15}\\d+(,\\d{3})+(?![ -]*(mile|rpm|km|lbs)).*"))
				HasMoney = true;
		
		SignalForMoney = lcc.originalText.matches(".*(pay|start|used).{0,15}\\d+(,\\d{3})+(?![ -]*(mile|rpm|km|lbs)).*");  
		
		NumberSet = new HashSet<Integer>();
		Pattern p = Pattern.compile("(\\d{5}|\\d{2},\\d{3}|\\d{2}\\.\\d{3})");
		Matcher m = p.matcher(lcc.originalText);
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			int left = Math.max(0, start - 10);
			int right = Math.min(end + 10, lcc.originalText.length());
			String context = lcc.originalText.substring(left, right);
			if (context.indexOf("mile") == -1 && context.indexOf("rpm") == -1 && context.indexOf("km") == -1 && context.indexOf("lbs") == -1) {
				String number = lcc.originalText.substring(start, end).replace(",", "").replace(".", "");
				NumberSet.add(Integer.valueOf(number));
			}
		}
		
		p = Pattern.compile("(pay|start|used|from|about) \\d+(,\\d{3})*");
		m = p.matcher(lcc.originalText);
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			int left = Math.max(0, start - 10);
			int right = Math.min(end + 10, lcc.originalText.length());
			String context = lcc.originalText.substring(left, right);
			if (context.indexOf("mile") == -1 && context.indexOf("rpm") == -1 && context.indexOf("km") == -1 && context.indexOf("lbs") == -1 && context.indexOf("pound") == -1) {
				String number = lcc.originalText.substring(start, end).replaceAll("\\D", "");
				if (number.length() >= 4) 
					NumberSet.add(Integer.valueOf(number));
			}
		}
	
		HasMoreThanOneAttrsKeywords = ((HasWordForInterior ? 1 : 0) + (HasWordForExterior ? 1 : 0) + (HasWordForDriveRide ? 1 : 0) 
				+ (HasWordForReliability ? 1 : 0) + (HasWordForSafety ? 1 : 0) + (HasWordForPrice ? 1 : 0)) > 1;

		/*** Spec matching ***/
		MatchSpecForInterior = SpecMatch(lcc, "Interior", sc);
		MatchSpecForExterior = SpecMatch(lcc, "Exterior", sc);
		MatchSpecForDriveRide = SpecMatch(lcc, "DriveRide", sc);
		MatchSpecForSafety = SpecMatch(lcc, "Safety", sc);
		MatchSpecForReliability = SpecMatch(lcc, "Reliability", sc);

		MatchMultipleAttr = MultiAttrMaxPortion();
		
		this.DriveRideSet = MatchSpecForDriveRide.matches;

		MatchSpecNounForInterior = SpecNounMatch(lcc, "Interior", snc);
		MatchSpecNounForExterior = SpecNounMatch(lcc, "Exterior", snc);
		MatchSpecNounForDriveRide = SpecNounMatch(lcc, "DriveRide", snc, this.DriveRideSet);
		MatchSpecNounForSafety = SpecNounMatch(lcc, "Safety", snc);
		MatchSpecNounForReliability = SpecNounMatch(lcc, "Reliability", snc);

		MatchNounMultipleAttr = (MatchSpecNounForInterior > 0 ? 1 : 0) + (MatchSpecNounForExterior > 0 ? 1 : 0) + (MatchSpecNounForDriveRide > 0 ? 1 : 0) + 
				(MatchSpecNounForSafety > 0 ? 1 : 0) + (MatchSpecNounForReliability > 0 ? 1 : 0) > 1;
		//MatchNounMultipleAttr = MultiAttrNounMaxPortion();
				
		MatchAnyNoun = 	(MatchSpecNounForInterior > 0 ? 1 : 0) + (MatchSpecNounForExterior > 0 ? 1 : 0) + (MatchSpecNounForDriveRide > 0 ? 1 : 0) + 
				(MatchSpecNounForSafety > 0 ? 1 : 0) + (MatchSpecNounForReliability > 0 ? 1 : 0) > 0;
				
		int n1 = (MatchSpecNounForInterior > 0 ? 1 : 0) + (MatchSpecNounForExterior > 0 ? 1 : 0) + (MatchSpecNounForDriveRide > 0 ? 1 : 0) + 
				(MatchSpecNounForSafety > 0 ? 1 : 0) + (MatchSpecNounForReliability > 0 ? 1 : 0);
		int n2 = (MatchSpecForInterior.nMatchSpecName > 0 ? 1 : 0) + (MatchSpecForExterior.nMatchSpecName > 0 ? 1 : 0) + (MatchSpecForDriveRide.nMatchSpecName > 0 ? 1 : 0) + 
				(MatchSpecForSafety.nMatchSpecName > 0 ? 1 : 0) + (MatchSpecForReliability.nMatchSpecName > 0 ? 1 : 0);
		SpecMapCount = n1; // + n2;
		
		IsDriveRide = HasWordForDriveRide | MatchSpecNounForDriveRide > 0 | MatchSpecForDriveRide.nMatchSpecName > 0;
		boolean IsInterior = HasWordForInterior | MatchSpecNounForInterior > 0 | MatchSpecForInterior.nMatchSpecName > 0;
		boolean IsExterior = HasWordForExterior | MatchSpecNounForExterior > 0 | MatchSpecForExterior.nMatchSpecName > 0;
		boolean IsSafety = HasWordForSafety | MatchSpecNounForSafety > 0 | MatchSpecForSafety.nMatchSpecName > 0;
		boolean IsReliability = HasWordForReliability | MatchSpecNounForReliability > 0 | MatchSpecForReliability.nMatchSpecName > 0;
		boolean IsPrice = HasWordForPrice | HasMoney | SignalForMoney;
		boolean IsOthers = HasOtherBrand;
		IsDriveRide = IsDriveRide & (!IsReliability & !IsPrice);
				
		HasNoMoreThanSixWords = lcc.wordList.size() < 6;
		
		SurroundIndicatorForReliability = lcc.path.matches(".*\\.H\\d\\..*") && lcc.originalText.indexOf("trouble spot") > -1 || lcc.originalText.indexOf("recall history") > -1;
		HasWordForReliability = CheckWordsForReliability(lcc.originalText); 
	}
	
	public static double Similarity(LineClass2 lc1, LineClass2 lc2) {
        int nSize1 = 0;
        Map<String, Integer> dic = new HashMap<String, Integer>();
        for (int k = 0; k < lc1.wordList.size(); k++) {
            dic.put(lc1.wordList.get(k), lc1.countList.get(k));
            nSize1 += lc1.countList.get(k).intValue();
        }

        int nMatch = 0;
        int nSize2 = 0;
        for (int k = 0; k < lc2.wordList.size(); k++) {
            String word = lc2.wordList.get(k);
            int n2 = lc2.countList.get(k).intValue();

            if (dic.containsKey(word)) {
                int n1 = dic.get(word).intValue();
                nMatch += Math.min(n1, n2);
            }

            nSize2 += n2;
        }
       
        double minValue = (double) Math.min(nSize1, nSize2);
        return (double) nMatch / minValue;
    }

	private boolean CheckWordsForReliability(String text) {
		int n = 0;
		String[] words = new String[] {"reliab", "reputation", "repair", "trouble", "complain", "leak", "not work"};
		for (int i = 0; i < words.length; i++) {
			if (text.indexOf(words[i]) > -1)
				n++;
			if (n >= 2) return true;
		}
		return false;
	}
	private boolean MultiAttrNounMaxPortion() {
		// if the max MatchSpecNoun does not dominate, i.e. < 80%, then it is multi-attributed
		int max = -1;
		int secondmax = -1;
		double sum = 0;
		int[] array = new int[] {MatchSpecNounForInterior, MatchSpecNounForExterior, MatchSpecNounForDriveRide, MatchSpecNounForSafety, MatchSpecNounForReliability};

		max = array[0];
		secondmax = array[1];
		if (array[1] > array[0]) {
			max = array[1];
			secondmax = array[0];
		}

		for (int i = 2; i < array.length; i++) {
			if (array[i] > max) {
				secondmax = max;
				max = array[i];
			}
			else if (array[i] > secondmax && array[i] < max) {
				secondmax = array[i];
			}
		}

		if (((double) max / sum < 0.8) || secondmax > 2)
			return true;

		return false;
	}

	private boolean MultiAttrMaxPortion() {
		// case 1: if the max nMatchSpecName does not dominate, i.e. < 80%, then it is multi-attributed
		int max = -1;
		int secondmax = -1;
		double sum = 0;
		int[] array = new int[] {MatchSpecForInterior.nMatchSpecName, MatchSpecForExterior.nMatchSpecName, 
				MatchSpecForDriveRide.nMatchSpecName, MatchSpecForSafety.nMatchSpecName, MatchSpecForReliability.nMatchSpecName};

		max = array[0];
		secondmax = array[1];
		if (array[1] > array[0]) {
			max = array[1];
			secondmax = array[0];
		}

		for (int i = 2; i < array.length; i++) {
			if (array[i] > max) {
				secondmax = max;
				max = array[i];
			}
			else if (array[i] > secondmax && array[i] < max) {
				secondmax = array[i];
			}
		}

		if (((double) max / sum < 0.8) || secondmax > 2)
			return true;

		// case 2: if there are more than one nMatchSpecNumber > 0, then it is multi-attributed  
		boolean flag = (MatchSpecForInterior.nMatchSpecNumber > 0 ? 1 : 0) +
				(MatchSpecForExterior.nMatchSpecNumber > 0 ? 1 : 0) +
				(MatchSpecForDriveRide.nMatchSpecNumber > 0 ? 1 : 0) +
				(MatchSpecForSafety.nMatchSpecNumber > 0 ? 1 : 0) +
				(MatchSpecForReliability.nMatchSpecNumber > 0 ? 1 : 0) > 1;

		return flag;
	}

	private MatchClass SpecMatch(LineClass2 lcc, String attribute, SpecClass sc) {
		MatchClass mc = new MatchClass();
		if (!sc.MapAttrSpec.containsKey(attribute))
			return null;

		// get the line text and restore the abbreviations
		String text = lcc.originalText;
		text = text.replace("ft.", "feet ");
		text = text.replace("in.", "inch ");
		text = text.replace("mi.", "mile ");
		text = text.replace("lbs.", "lbs ");
		text = text.replace("cu.", "cubic ");
		text = text.replace("yr.", "year ");
		text = text.replace("gal.", "gallon ");

		List<SingleSpecClass> specList = sc.MapAttrSpec.get(attribute);

		for (int i = 0; i < specList.size(); i++) {
			SingleSpecClass ssc = specList.get(i);

			/*// match the spec names
			String[] segs = ssc.SpecName.split("\t");
			int nMatch = 0;
			for (int j = 0; j < segs.length; j++) {
				if (text.indexOf(segs[j]) > -1)
					nMatch++;
			}
			if ((double) nMatch / (double) segs.length >= 0.2)
				mc.nMatchSpecName++;*/

			// match the spec nouns
			int nMatch = 0;
			for (int j = 0; j < ssc.NounList.size(); j++) {
				String value = ssc.NounList.get(j);
				if (value.length() < 3) {
					//System.out.println(value);
					continue;
				}
				
				//if (text.indexOf(ssc.NounList.get(j)) > -1)
				//	nMatch++;
				int ntmp = 0;
				String[] segs = ssc.NounList.get(j).split(" ");
				for (int k = 0; k < segs.length; k++) {
					if (text.indexOf(segs[k]) > -1)
						ntmp++;
				}
				if ((double) ntmp / (double) segs.length >= 0.3) {
					nMatch++;
					
					if (value.length() > 0)
						mc.matches.add(value); // put the match noun
				}
			}
			if (nMatch > 0)
				mc.nMatchSpecName++;

			// match the spec values
			List<String> values = ssc.SpecValueList;
			for (int j = 0; j < values.size(); j++) {
				String value = values.get(j);
				if (value.length() < 3) {
					//System.out.println(value);
					continue;
				}
				
				if (text.indexOf(value) > -1 || text.indexOf(value.replace(" ", "")) > -1) {
					mc.nMatchSpecNumber++;
					
					if (value.length() > 0)
						mc.matches.add(value); // put the match value
				}
			}
		}

		return mc;
	}

	private int SpecNounMatch(LineClass2 lcc, String attribute, SpecNounClass snc, Set<String> InputSet) {
		if (!snc.MapAttrNoun.containsKey(attribute))
			return 0;
		
		// get the line text and restore the abbreviations
		String text = lcc.originalText;
		Set<String> set = snc.MapAttrNoun.get(attribute);

		int nMatch = 0;
		for (String s : set) {
			if (s.length() < 3) {
				//System.out.println(s);
				continue;
			}
			
			if (s.equals("steering wheel") || s.equals("wheel base") || s.equals("trim door") || s.equals("wheel drive") 
					|| s.equals("remote engine start") || s.equals("hard drive") || s.equals("wheel lock") || s.equals("real time traffic")
					|| s.equals("head room") || s.equals("shoulder room") || s.equals("leg room") || s.equals("hip room")) {
				String[] segs = s.split(" ");
				String regex = ".*";
				for (int j = 0; j < segs.length - 1; j++) {
					regex += segs[j] + "[ -]*";
				}
				regex += segs[segs.length - 1] + ".*";
				if (text.matches(regex)) {
					nMatch++;
					
					if (s.length() > 0)
						InputSet.add(s);
				}
			}
			else if (s.equals("wheel") || s.equals("tire") || s.equals("tyre")) {
				if (text.indexOf(s) > -1) {
					String tmp = text;
					tmp = tmp.replaceAll("steering[ -]*wheel", "");
					tmp = tmp.replaceAll("wheel[ -]*base", "");
					tmp = tmp.replaceAll("wheel[ -]*drive", "");
					tmp = tmp.replaceAll("\\wtire", "");
					tmp = tmp.replaceAll("tire[^s]", "");
					if (tmp.indexOf(s) > -1) {
						String regex1 = ".*\\d+.{0,20}(wheel|tire).*";
						String regex2 = ".*(wheel|tire).{0,20}\\d+.*";
						String id2 = lcc.entityID + " " + lcc.pageID + " " + lcc.lineID;
						if (tmp.matches(regex1) || tmp.matches(regex2)) {
							nMatch++;
							
							if (s.length() > 0)
								InputSet.add(s);
						}
					}
				}
			}
			// for other cases, require certain match
			else {
				String[] segs = s.split(" ");
				int ntmp = 0;
				// choice 1: overlap in whole text
				for (int k = 0; k < segs.length; k++) {
					// avoid the case when the text is used as prefix or postfix
					String regex1 = ".*\\w" + segs[k] + ".*";
					String regex2 = ".*" + segs[k] + "\\w{2,}.*";
					if (text.indexOf(segs[k]) > -1 && !text.matches(regex1) && !text.matches(regex2))
						ntmp++;
				}
				
				// choice 2: overlap with >=2 words, unless segs.length = 1
				if (ntmp >= 2 || (ntmp == 1 && segs.length == 1)) {
					nMatch++;
					
					if (s.length() > 0)
						InputSet.add(s);
				}
			}
		}

		return nMatch;
	}

	
	private int SpecNounMatch(LineClass2 lcc, String attribute, SpecNounClass snc) {
		if (!snc.MapAttrNoun.containsKey(attribute))
			return 0;
		
		// get the line text and restore the abbreviations
		String text = lcc.originalText;
		Set<String> set = snc.MapAttrNoun.get(attribute);

		int nMatch = 0;
		for (String s : set) {
			if (s.equals("steering wheel") || s.equals("wheel base") || s.equals("trim door") || s.equals("wheel drive") 
					|| s.equals("remote engine start") || s.equals("hard drive") || s.equals("wheel lock") || s.equals("real time traffic")
					|| s.equals("head room") || s.equals("shoulder room") || s.equals("leg room") || s.equals("hip room")) {
				String[] segs = s.split(" ");
				String regex = ".*";
				for (int j = 0; j < segs.length - 1; j++) {
					regex += segs[j] + "[ -]*";
				}
				regex += segs[segs.length - 1] + ".*";
				if (text.matches(regex))
					nMatch++;
			}
			else if (s.equals("wheel") || s.equals("tire") || s.equals("tyre")) {
				if (text.indexOf(s) > -1) {
					String tmp = text;
					tmp = tmp.replaceAll("steering[ -]*wheel", "");
					tmp = tmp.replaceAll("wheel[ -]*base", "");
					tmp = tmp.replaceAll("wheel[ -]*drive", "");
					tmp = tmp.replaceAll("\\wtire", "");
					tmp = tmp.replaceAll("tire[^s]", "");
					if (tmp.indexOf(s) > -1) {
						String regex1 = ".*\\d+.{0,20}(wheel|tire).*";
						String regex2 = ".*(wheel|tire).{0,20}\\d+.*";
						String id2 = lcc.entityID + " " + lcc.pageID + " " + lcc.lineID;
						if (tmp.matches(regex1) || tmp.matches(regex2)) 
							nMatch++;
					}
				}
			}
			// for other cases, require certain match
			else {
				String[] segs = s.split(" ");
				int ntmp = 0;
				// choice 1: overlap in whole text
				for (int k = 0; k < segs.length; k++) {
					// avoid the case when the text is used as prefix or postfix
					String regex1 = ".*\\w" + segs[k] + ".*";
					String regex2 = ".*" + segs[k] + "\\w{2,}.*";
					if (text.indexOf(segs[k]) > -1 && !text.matches(regex1) && !text.matches(regex2))
						ntmp++;
				}
				
				// choice 2: overlap with >=2 words, unless segs.length = 1
				if (ntmp >= 2 || (ntmp == 1 && segs.length == 1)) {
					nMatch++;
				}
			}
		}

		return nMatch;
	}
	
	public static boolean Match(Set<String> a, Set<String> b) {
		boolean flag = false;
		for (String s : a) {
			if (b.contains(s)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * Other is lc's previous neighboring line
	 * @param lc
	 * @param other
	 * @return
	 */
	public static boolean IsPrevLine(LineClass2 lc, LineClass2 other) {
		if (lc.pageID.equalsIgnoreCase(other.pageID) && other.lineID == lc.lineID - 1)
			return true;
		return false;
	}

	/**
	 * Other is lc's next neighboring line
	 * @param lc
	 * @param other
	 * @return
	 */
	public static boolean IsNextLine(LineClass2 lc, LineClass2 other) {
		if (lc.pageID.equalsIgnoreCase(other.pageID) && other.lineID == lc.lineID + 1)
			return true;
		return false;
	}

	public static boolean HasSimilarValue(ObsClass obs1, ObsClass obs2) {
		double threshold = 20000;
		for (Integer v1 : obs1.NumberSet) {
			for (Integer v2 : obs2.NumberSet) {
				if (Math.abs(v1 -v2) < threshold) 
					return true;
			}
		}
		
		return false;
	}
}

class MatchClass {
	public int nMatchSpecName;
	public int nMatchSpecNumber;
	public Set<String> matches;

	public MatchClass() {
		this.nMatchSpecName = 0;
		this.nMatchSpecNumber = 0;
		this.matches = new HashSet<String>();
	}
}