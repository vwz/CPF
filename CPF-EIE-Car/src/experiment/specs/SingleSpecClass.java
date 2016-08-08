package experiment.specs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleSpecClass {
	public String SpecName;
	public String attribute;
	public List<String> SpecValueList;
	public List<String> NounList;
	
	public SingleSpecClass() {
		SpecName = "";
		SpecValueList = new ArrayList<String>();
		NounList = new ArrayList<String>();
	}
	
	/*
	 * 1) "front track: 5 ft. 3.7 in. (63.7 in.)"
	 * 2) "multi-link rear suspension"
	 */
	public void Process(String line, String attribute) {
		this.attribute = attribute;
		String[] partition = line.split("\t\\|\\|\t", -1);

		if (partition.length != 2)
			System.out.println(line);
		SpecName = partition[0];
		
		/*** noun list ***/
		String[] nounsegs = partition[1].split("; ");
		for (int i = 0; i < nounsegs.length; i++)
			NounList.add(nounsegs[i]);
		
		
		if (line.indexOf(": ") > -1) {
			/*** specs ***/
			String[] segs = partition[0].split(": ");
			SpecName = segs[0];
			//System.out.println(SpecName);
			
			// process value list
			String regex = "\\d+(\\.\\d)*(\\W\\d+)*( )*\\D+";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(segs[1]);
			while(m.find()) {
				String value = segs[1].substring(m.start(), m.end());
				value = value.replaceAll("[\\(\\)\\/\\@]\\s*$", "");
				value = value.replace("ft.", "feet ");
				value = value.replace("in.", "inch ");
				value = value.replace("mi.", "mile ");
				value = value.replace("lbs.", "lbs ");
				value = value.replace("cu.", "cubic ");
				value = value.replace("yr.", "year ");
				value = value.replace("gal.", "gallon ");
				value = value.trim();
				SpecValueList.add(value);
				//System.out.println(value);
			}
		}
	}
}
