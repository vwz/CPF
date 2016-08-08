package experiment.constraint;

import java.util.HashMap;
import java.util.Map;

import experiment.graph.LineClass2;

public class ObsClass {
	public boolean HasNoMoreThanThreeWords;	
	public boolean HasNoMoreThanSixWords;
	public boolean HasNoMoreThanTenWords;
	public boolean HasYear;
	public boolean HasOrg;
	public boolean IsListStarter;
	public int nLI;
	public int mostSimID; // always from the same entity, this is the index within each entity's data
	public double mostSimScore; // the corresponding similarity score for mostSimID
	public boolean StartWithEduOrResearch;
	public boolean InListOrTable;

	/** bio **/
	public boolean StartWordForBio;
	public boolean IsBioCandidate;
	public boolean IsBioCandidate2;
	public boolean HasOrgInBioCandidate;
	public boolean IsBioTaboo;
	public boolean PrecededByBioKeywords;
	public boolean ShortHasWordForBio;

	/** education **/
	public boolean HasWordForEducation;
	public boolean HasOrgForEducation;
	public boolean StartWithEducation;
	public boolean PrecededByEducationOrgInBio;
	public boolean FollowedByEducationOrgInBio;
	public boolean PrecededByEducationKeywords;
	public boolean PrecededByCourse;
	
	/** employment **/
	public boolean HasWordForEmployment;
	public boolean StartWordForEmployment;
	
	/** award **/
	public boolean HasWordForAward;
	public boolean HasWordForAwardEarly;
	
	/** presentation **/
	public boolean HasWordSpeaker;
	public boolean HasWordForPresentation;
	public boolean StartWordForPresentation;
	public boolean PrecededByPresentationKeywords;
	
	/** research **/
	public boolean HasAtLeastTwoNamesSideBySide;
	
	/** phone **/
	public boolean HasNumber;
	public boolean StartWordForPhone;
	public boolean IsPhoneFaxCandidate;
	public boolean HasPhoneKeywords;
	
	/** fax **/
	public boolean StartWordForFax;
	
	/** address **/
	public boolean HasLocation;
	public boolean StartWordForAddress;
	public boolean IsLocation;
	public boolean SurroundedByAddress;
	public boolean MatchStreet;
	public boolean MatchStateZip;
	public boolean StartNumberWordCapitalized;
	
	/** email **/
	public boolean HasWordForEmail;
	public boolean IsEmailPattern;
	public boolean HasEmailKeywords;
	public boolean PrecededByEmailKeywords;
	
	/** others **/
	public boolean HasName;
	public boolean HasEntityName;
	public boolean StartWithEntityName;
	public boolean StartWithOtherEntityName;
	public boolean StartSingularAndHasEntityName;
	public boolean IsName;
	public boolean IsNameAndOrg;
	public boolean IsOrg;
	
	public double[] ReturnFeatures() {
		boolean[] booleanfeatures = new boolean[]{
				/**others**/
				HasNoMoreThanTenWords
		};
		boolean[] booleanfeatures2 = new boolean[] {
				/**Bio**/
				IsBioCandidate,
				
				/**Education**/
				HasWordForEducation, StartWithEducation, PrecededByEducationKeywords,
				
				/**Employment**/
				HasWordForEmployment, StartWordForEmployment, 
				
				/**Presentation**/
				HasWordSpeaker, HasWordForPresentation, StartWordForPresentation, PrecededByPresentationKeywords, 
				
				/**Award**/
				HasWordForAward, HasWordForAwardEarly, 
				
				/**Research**/
				HasAtLeastTwoNamesSideBySide, 
				
				/**Phone**/
				HasNumber, StartWordForPhone, HasPhoneKeywords,
				
				/**Fax**/
				StartWordForFax,
				
				/**Email**/
				HasWordForEmail, IsEmailPattern, HasEmailKeywords, PrecededByEmailKeywords,
				
				/**Address**/
				HasLocation, StartWordForAddress, MatchStreet, MatchStateZip, StartNumberWordCapitalized, 
				SurroundedByAddress,
				
				/**Others**/
				IsName, StartWithOtherEntityName & !HasEntityName, StartSingularAndHasEntityName
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

	public void CollectObs(LineClass2 lcc, String name) {
		mostSimID = -1;
		mostSimScore = -1;
		
		name = name.toLowerCase();
		String startText = lcc.originalText.substring(0, Math.min(20, lcc.originalText.length()));
		
		CleanNameInOrgTags(lcc, name);

		// IsEmailPattern
		String tmpText = ReplaceEmailIcons(lcc.rawText);
		IsEmailPattern = tmpText.matches("(\\s*|.+\\s+)[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})(\\s+.+|\\s*)");

		// HasName
		HasName = lcc.tag_person.size() > 0;

		// HasWordSpeaker
		HasWordSpeaker = lcc.originalText.indexOf("speaker") > -1;

		// HasWordForPresentation
		HasWordForPresentation = lcc.originalText.indexOf("keynote") > -1 || lcc.originalText.indexOf("plenary") > -1 || lcc.originalText.indexOf("seminar") > -1 || lcc.originalText.indexOf("presentation") > -1 || lcc.originalText.indexOf("lecture") > -1 || lcc.originalText.indexOf(" talk ") > -1; 
				//|| (lcc.originalText.indexOf("lecture") > -1 && !lcc.originalText.matches(".*\\wlecture.*") && !lcc.originalText.matches(".*lecture[^s].*") && !lcc.originalText.matches(".*lecture note.*")); // <---- CHANGE

		// StartWordForPresentation
		StartWordForPresentation = startText.indexOf("abstract") > -1 || startText.indexOf("speaker") > -1 || startText.indexOf("talk") > -1; // <--- CHANGE
		
		this.PrecededByPresentationKeywords = false;

		// HasNoMoreThanSixWords
		HasNoMoreThanSixWords = lcc.countList.size() <= 6;

		// HasAtLeastTwoNamesSideBySide
		HasAtLeastTwoNamesSideBySide = false;
		if (lcc.tag_person.size() >= 2) {
			for (int i = 0; i < lcc.tag_person.size() - 1; i++) {
				String regex = ".*" + lcc.tag_person.get(i) + "\\W+" + lcc.tag_person.get(i+1) + ".*";
				if (lcc.originalText.matches(regex)) {
					HasAtLeastTwoNamesSideBySide = true;
					break;
				}
			}
		}

		// HasYear
		HasYear = lcc.tag_date.size() > 0;

		// HasOrg
		HasOrg = lcc.tag_org.size() > 0;

		// StartWordForEmployment
		StartWordForEmployment = lcc.originalText.startsWith("affiliation") || lcc.originalText.startsWith("employment");   

		// IsName
		IsName = false;
		if (lcc.tag_person.size() == 1) {
			double nameLen = lcc.tag_person.get(0).length();
			String tmp = lcc.originalText.replaceAll("\\W", " ");
			tmp = tmp.replaceAll(" +", " ");
			double totalLen = tmp.length();
			if (nameLen >= totalLen * 0.5)
				IsName = true;				
		}

		// IsNameAndOrg
		IsNameAndOrg = false;
		double len = 0;
		if (lcc.tag_person.size() > 0 && lcc.tag_org.size() > 0) {
			for (int i = 0; i < lcc.tag_person.size(); i++)
				len += lcc.tag_person.get(i).length();
			for (int i = 0; i < lcc.tag_org.size(); i++)
				len += lcc.tag_org.get(i).length();
			String tmp = lcc.originalText.replaceAll("\\W", " ");
			tmp = tmp.replaceAll(" +", " ");
			if (len >= tmp.length() * 0.6)
				IsNameAndOrg = true;
		}

		// IsOrg
		IsOrg = false;
		if (lcc.tag_org.size() > 0) {
			double orgLen = 0;
			for (int i = 0; i < lcc.tag_org.size(); i++)
				orgLen += (lcc.tag_org.get(i).length() + 1); // add space
			double totalLen = lcc.originalText.length();
			if (orgLen >= totalLen * 0.6)
				IsOrg = true;
		}

		// HasNumber
		HasNumber = false;
		IsPhoneFaxCandidate = false;
		
		boolean HasEnoughDigits = false;
		if (lcc.originalText.matches(".*\\d{7,13}.*")) {
			HasEnoughDigits = true;
		}
		else {
			String[] segs2 = lcc.originalText.split(",");
			for (int i = 0; i < segs2.length; i++) {
				String y3 = segs2[i].replaceAll("\\D", "");
				if (y3.matches("\\d{7,13}")) {
					HasEnoughDigits = true;
					break;
				}
			}
		}
		
		if (HasEnoughDigits) {
			String y2 = lcc.originalText;
			y2 = y2.replace("-", "");
			y2 = y2.replace("/", "");
			y2 = y2.replace("(", "");
			y2 = y2.replace(")", "");
			y2 = y2.replace(",", "");
			y2 = y2.replace(".", "");

			if (y2.matches(".*\\d{3,13}.*")) 
				IsPhoneFaxCandidate = true;
			
			if (IsPhoneFaxCandidate && lcc.originalText.length() < 40)
				HasNumber = true;
		}
		
		// HasPhoneKeywords
		HasPhoneKeywords = lcc.originalText.indexOf("phone") > -1 || lcc.originalText.indexOf("ph:") > -1 || lcc.originalText.indexOf("tel:") > -1 || lcc.originalText.indexOf("tel.") > -1 
				|| lcc.originalText.indexOf("telephone") > -1 || lcc.originalText.indexOf("office") > -1; 
		
		// StartWordForPhone
		StartWordForPhone = lcc.originalText.startsWith("phone") || lcc.originalText.startsWith("ph:") || lcc.originalText.startsWith("ph.") || lcc.originalText.startsWith("tel:") || lcc.originalText.startsWith("tel.") || lcc.originalText.startsWith("telephone");

		// HasWordForEmail
		HasWordForEmail = lcc.originalText.indexOf(".edu") > -1 || lcc.originalText.indexOf("gmail.com") > -1;

		// HasEmailKeywords
		HasEmailKeywords = lcc.originalText.indexOf("email") > -1 || lcc.originalText.indexOf("e-mail") > -1;
		this.PrecededByEmailKeywords = false;
		
		// HasLocation
		HasLocation = lcc.tag_location.size() > 0;
		
		// StartWordForAddress
		StartWordForAddress = lcc.originalText.startsWith("mail") || lcc.originalText.startsWith("address") || (lcc.originalText.startsWith("office") & HasLocation) || lcc.originalText.startsWith("location") || lcc.originalText.startsWith("addr:") || (lcc.originalText.startsWith("office:") & !IsPhoneFaxCandidate);

		// IsLocation
		IsLocation = false;
		if (lcc.tag_location.size() > 0) {
			double orgLen = 0;
			for (int i = 0; i < lcc.tag_location.size(); i++)
				orgLen += (lcc.tag_location.get(i).length() + 1); // add space
			double totalLen = lcc.originalText.length();
			if (orgLen >= totalLen * 0.5)
				IsLocation = true;
		}
		
		String[] streets = new String[] {"court", " ct.", " ct,", " ct ", "street", " st.", " st,", " st ", "drive", " dr." , " dr," , " dr ", 
				"lane", " ln.", " ln,", " ln ", "road", " rd.", " rd,", " rd ", "blvd", "p.o. box", "room", "box", "building"};
		MatchStreet = MatchLocation(lcc.originalText, streets, "street");
		
		String[] states = new String[] {"AK", "AL", "AR", "AZ", "CA", 
				"CO", "CT", "DC", "DE", "FL", "GA", "GU", "HI", "IA", "ID", "IL", "IN", 
				"KS", "KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", 
				"ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI", 
				"SC", "SD", "TN", "TX", "UT", "VA", "VI", "VT", "WA", "WI", "WV", "WY", 
				"Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", 
				"Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", 
				"Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", 
				"Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", 
				"West Virginia", "Wisconsin", "Wyoming"};
		MatchStateZip = MatchLocation(lcc.rawText, states, "state");
		
		StartNumberWordCapitalized = false;
		if (startText.matches("\\d+.*")) {
			boolean Capitalized = true;
			String[] segs = lcc.rawText.split(" ");
			for (int i = 0; i < segs.length; i++) {
				String word = segs[i].replaceAll("\\W", "").trim();
				if (word.length() > 0) {
					if (Character.isLowerCase(word.charAt(0))) {
						Capitalized = false;
						break;
					}
				}
			}
			if (Capitalized)
				StartNumberWordCapitalized = true;
		}
				
		
		SurroundedByAddress = false;

		// HasNoMoreThanTenWords
		HasNoMoreThanTenWords = lcc.countList.size() <= 10;

		// HasNoMoreThanThreeWords
		HasNoMoreThanThreeWords = lcc.countList.size() <= 3;

		// HasWordForAward
		HasWordForAward = lcc.originalText.indexOf("award") > -1 || lcc.originalText.indexOf("prize") > -1 || lcc.originalText.indexOf("honor") > -1 || lcc.originalText.indexOf("winner") > -1 
				|| lcc.originalText.indexOf("fellow") > -1 || lcc.originalText.indexOf("scholarship") > -1 || lcc.originalText.indexOf("featured") > -1;  // <------ CHANGE
		HasWordForAwardEarly = false;
		if (HasWordForAward)
			HasWordForAwardEarly = (lcc.originalText.indexOf("award") > -1 && lcc.originalText.indexOf("award") < 100) 
			|| (lcc.originalText.indexOf("prize") > -1 && lcc.originalText.indexOf("prize") < 100) 
			|| (lcc.originalText.indexOf("honor") > -1 && lcc.originalText.indexOf("honor") < 100) 
			|| (lcc.originalText.indexOf("winner") > -1 && lcc.originalText.indexOf("winner") < 100);

		// HasWordForEducation
		HasWordForEducation = lcc.originalText.indexOf("bachelor") > -1 || lcc.originalText.indexOf("master") > -1 || lcc.originalText.indexOf("alma mater") > -1 || 
				lcc.originalText.indexOf("phd") > -1 || lcc.originalText.indexOf("ph.d") > -1 || lcc.originalText.indexOf("ph. d") > -1 ||  
				lcc.originalText.indexOf("b.sc") > -1 || lcc.originalText.indexOf("b. sc") > -1 || lcc.originalText.indexOf("b.eng") > -1 || lcc.originalText.indexOf("b. eng") > -1 ||
				lcc.originalText.indexOf("b.s.") > -1 || lcc.originalText.indexOf("m.s.") > -1 || lcc.originalText.indexOf("b.a.") > -1 || lcc.originalText.indexOf("m.a.") > -1 ||
				lcc.originalText.indexOf("b.tech") > -1 || lcc.originalText.indexOf("b. tech") > -1 || lcc.originalText.indexOf("high school") > -1;

		// StartWithEduOrResearch
		String startText2 = lcc.originalText.substring(0, Math.min(50, lcc.originalText.length()));
		boolean startWithEdu = startText2.indexOf("bachelor") > -1 || startText2.indexOf("master") > -1 || 
				startText2.indexOf("phd") > -1 || startText2.indexOf("ph.d") > -1 || startText2.indexOf("ph. d") > -1 ||  
				startText2.indexOf("b.sc") > -1 || startText2.indexOf("b. sc") > -1 || startText2.indexOf("b.eng") > -1 || startText2.indexOf("b. eng") > -1 ||
				startText2.indexOf("b.s.") > -1 || startText2.indexOf("m.s.") > -1 || startText2.indexOf("b.a.") > -1 || startText2.indexOf("m.a.") > -1 ||
				startText2.indexOf("b.tech") > -1 || startText2.indexOf("b. tech") > -1 || startText2.indexOf("high school") > -1;
		boolean startWithResearchInterest = startText2.indexOf("research") > -1 || startText2.indexOf("publish") > -1 || startText2.indexOf("author") > -1 || startText2.indexOf("serve") > -1;
		StartWithEduOrResearch = startWithEdu | startWithResearchInterest;		
		
		// HasWordForEmployment
		HasWordForEmployment = lcc.originalText.indexOf("affiliation") > -1 || lcc.originalText.indexOf("employment") > -1 || lcc.originalText.indexOf("manager") > -1 || lcc.originalText.indexOf("director") > -1 || lcc.originalText.indexOf("chancellor") > -1 || lcc.originalText.indexOf("president") > -1 ||
				lcc.originalText.indexOf("professor") > -1 || lcc.originalText.indexOf("prof ") > -1 || lcc.originalText.indexOf("prof.") > -1 || lcc.originalText.indexOf("scientist") > -1 || lcc.originalText.indexOf("postdoc") > -1 || lcc.originalText.indexOf("post doc") > -1 ||
				lcc.originalText.indexOf("ceo") > -1 || lcc.originalText.indexOf("cto") > -1 || lcc.originalText.indexOf("founder") > -1 || lcc.originalText.indexOf("intern") > -1 || lcc.originalText.indexOf("board committee") > -1 || lcc.originalText.indexOf("advisory committee") > -1;

		// IsListStarter
		IsListStarter = false;

		// HasEntityName, StartWithEntityName
		HasEntityName = false;
		StartWithEntityName = false;
		String[] segs = name.split(" ");
		String surName = segs[segs.length - 1];
		String firstName = segs[0];
		String originalTextLowerCase = lcc.originalText;
		if (originalTextLowerCase.indexOf(surName) > -1 && originalTextLowerCase.indexOf(firstName) > -1) 
			HasEntityName = true;
		if (!HasEntityName) {
			if (originalTextLowerCase.indexOf(". " + surName) > -1 || originalTextLowerCase.indexOf("dr " + surName) > -1 || originalTextLowerCase.indexOf("professor " + surName) > -1) 
				HasEntityName = true;
		}
		if (!HasEntityName) {
			if (originalTextLowerCase.indexOf(". " + firstName) > -1 || originalTextLowerCase.indexOf("dr " + firstName) > -1 || originalTextLowerCase.indexOf("professor " + firstName) > -1) 
				HasEntityName = true;
		}

		startText = originalTextLowerCase.substring(0, Math.min(30, lcc.originalText.length()));
		if (startText.indexOf(surName) > -1 && startText.indexOf(firstName) > -1)
			StartWithEntityName = true;
		if (!StartWithEntityName) {
			if (startText.indexOf(". " + surName) > -1 || startText.indexOf("dr " + surName) > -1 || startText.indexOf("professor " + surName) > -1)
				StartWithEntityName = true;
		}
		if (!StartWithEntityName) {
			if (startText.indexOf(". " + firstName) > -1 || startText.indexOf("dr " + firstName) > -1 || startText.indexOf("professor " + firstName) > -1)
				StartWithEntityName = true;
		}

		// StartWithOtherEntityName
		StartWithOtherEntityName = false;
		if (!StartWithEntityName) {
			for (int i = 0; i < lcc.tag_person.size(); i++) {
				String otherName = lcc.tag_person.get(i);
				if (startText.indexOf(otherName) > -1)
					StartWithOtherEntityName = true;
				break;
			}
		}
		
		// HasWordForBio
		StartWordForBio = (startText.indexOf("bio") > -1 && !startText.matches(".*\\wbio.*") && !startText.matches(".*bio\\w.*")) || startText.indexOf("biography") > -1; // || startText.startsWith("about");
				
		// Check whether it is Bio candidate
		boolean HasEnoughWords = lcc.countIDList.size() > 20;
		boolean StartWithPersonSingular = WhetherStartWithPersonSingular(startText) && !StartWithOtherEntityName;
		//IsBioCandidate = HasEnoughWords & (StartWithPersonSingular | StartWithEntityName | StartWordForBio);

		String endText = originalTextLowerCase.substring(Math.max(0, lcc.originalText.length() - 20), lcc.originalText.length());
		boolean EndWithYear = endText.matches(".*\\d{4}.*");
		boolean hasPubKeywords = originalTextLowerCase.indexOf("proc.") > -1 | originalTextLowerCase.indexOf("vol.") > -1 | originalTextLowerCase.indexOf("no.") > -1 | originalTextLowerCase.indexOf("pp.") > -1 | originalTextLowerCase.indexOf("proceeding") > -1 | originalTextLowerCase.indexOf("conference") > -1 | originalTextLowerCase.indexOf("conf.") > -1 | originalTextLowerCase.indexOf("workshop") > -1 | originalTextLowerCase.indexOf("journal") > -1 | originalTextLowerCase.indexOf("symposium") > -1;
		boolean IsPublication = (HasAtLeastTwoNamesSideBySide & hasPubKeywords) | (hasPubKeywords & originalTextLowerCase.indexOf("\"") > -1 & EndWithYear) | (originalTextLowerCase.indexOf("\"") > -1 & StartWithEntityName & EndWithYear);
		boolean IsResearch = originalTextLowerCase.indexOf("research interest") > -1 | originalTextLowerCase.indexOf("research focus") > -1 | originalTextLowerCase.indexOf("coauthor") > -1 | originalTextLowerCase.indexOf("publish") > -1 
				| originalTextLowerCase.indexOf("editor") > -1 | originalTextLowerCase.indexOf("program committee") > -1 | originalTextLowerCase.indexOf("work") > -1 | originalTextLowerCase.indexOf("research") > -1;
		boolean IsAward = startText.indexOf("award") > -1 | HasWordForAwardEarly;
		boolean IsBorn = startText.indexOf("born") > -1;
		
		int[] signals = new int[] {HasWordForAward ? 1 : 0, IsResearch ? 1 : 0, HasWordForEducation ? 1 : 0, HasWordForEmployment ? 1 : 0, IsBorn ? 1 : 0};
		int nsignal = 0;
		for (int i = 0; i < signals.length; i++)
			nsignal += signals[i];
		IsBioCandidate2 = ((StartWithPersonSingular & !HasEntityName) | StartWithEntityName) & (nsignal > 1);
		IsBioCandidate = IsBioCandidate2 | StartWordForBio;
		
		ShortHasWordForBio = false;
		if (HasNoMoreThanTenWords) {
			if ((originalTextLowerCase.indexOf("bio") > -1 && !originalTextLowerCase.matches(".*\\wbio.*") && !originalTextLowerCase.matches(".*bio\\w.*")) || originalTextLowerCase.indexOf("biography") > -1 || originalTextLowerCase.indexOf("biosketch") > -1)
				ShortHasWordForBio = true;
		}
		
		boolean SingularLong = StartWithPersonSingular & !HasNoMoreThanTenWords;
		boolean NameSignal = StartWithEntityName & (nsignal > 1);
		IsBioCandidate = SingularLong | NameSignal | StartWordForBio | ShortHasWordForBio;
		
		PrecededByBioKeywords = false;

		// 1) start with other name; 2) start with "i/he/she" and include the target entity name;
		StartSingularAndHasEntityName = (WhetherStartWithPersonSingular(startText) & HasEntityName);
		IsBioTaboo = StartWithOtherEntityName | StartSingularAndHasEntityName;

		HasOrgForEducation = false;
		for (int i = 0; i < lcc.tag_org.size(); i++) {
			String org = lcc.tag_org.get(i).toLowerCase();
			if (org.indexOf("university") > -1 || org.indexOf("institute") > -1 || org.indexOf("school") > -1 || org.indexOf("college") > -1 || org.indexOf("academy") > -1) {
				HasOrgForEducation = true;
				break;
			}
		}

		StartWithEducation = startText.indexOf("education") > -1 | startText.indexOf("alma mater") > -1;
		HasOrgInBioCandidate = true; // by default set as true, we always check later if there is any Bio candidate

		PrecededByEducationOrgInBio = false;
		FollowedByEducationOrgInBio = false;
		PrecededByEducationKeywords = false;
		PrecededByCourse = false;

		// StartWordForFax
		StartWordForFax = startText.indexOf("fax") > -1;
		
		nLI = GetLICount(lcc.path);
		
		InListOrTable = (nLI > 0) | lcc.path.indexOf("TD") > -1 | lcc.path.indexOf("TR") > -1 | lcc.path.indexOf("TABLE") > -1 | lcc.path.indexOf("DT") > -1 | lcc.path.indexOf("DL") > -1; 
	}
	
	private void CleanNameInOrgTags(LineClass2 lcc, String name) {
		String[] segs = name.split(" ");
		
		for (int i = 0; i < lcc.tag_org.size(); i++) {
			String org = lcc.tag_org.get(i);
			org = org.replace(name, "").trim();
			
			if (org.length() > 0) 
				lcc.tag_org.set(i, org);
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

	private boolean MatchLocation(String line, String[] keywordList, String option) {
		String content = "";
		String keyword = "";
		for (int i = 0; i < keywordList.length; i++) {
			int index = line.indexOf(keywordList[i]);
			if (index > -1) {
				content = line.substring(Math.max(index - 20, 0), 
						Math.min(index + 20, line.length()));
				keyword = keywordList[i];
				break;
			}
		}
		if (content.length() > 0) {
			if (option.equals("street")) {
				String regex = ".*\\d+.*";
				boolean flag = content.matches(regex) & !content.matches(".*\\d{7,13}.*"); // avoid random long digit string 
				return flag;
			} else if (option.equals("state")) {
				String regex = ".*" +  keyword + "\\D{1,5}\\d{5}.*";
				boolean flag = content.matches(regex) & !content.matches(".*\\d{6,13}.*"); // avoid random long digit string
				return flag;
			}
		}
		return false;
	}


	public static String ReplaceEmailIcons(String text) {
		String[] atList = { "(at)", "(AT)", "( at )", "( AT )", "[at]", "[AT]", "[ at ]", "[ AT ]"};
		String[] dotList = { "(dot)", "(DOT)", "( dot )", "( DOT )", "[dot]", "[DOT]", "[ dot ]", "[ DOT ]" };

		// normalize at
		for (String at : atList) {
			if (text.contains(at)) 	
				//text = text.replaceFirst(at, "@");
				text = text.replace(at, "@");
		}
		text = text.replaceAll("\\s*@\\s*", "@");

		// normalize at
		for (String dot : dotList) {
			if (text.contains(dot))
				text = text.replace(dot, ".");
		}
		text = text.replaceAll("\\s*\\.\\s*", "\\.");

		return text;
	}

	/*
	 * line is startText
	 */
	private boolean WhetherStartWithPersonSingular(String line) {
		//startText = line.substring(0, Math.min(10, startText.length()));
		String[] psArray = new String[] {"he", "i", "she", "his", "her"};
		for (int i = 0; i < psArray.length; i++) {
			String ps = psArray[i];
			String p1 = ps + "[^\\p{L}]*";
			String p2 = ps + "[^\\p{L}]+.*";
			String p3 = ".*[^\\p{L}]+" + ps;
			String p4 = ".*[^\\p{L}]+" + ps + "[^\\p{L}]+.*";
			if (line.matches(p1) | line.matches(p2) | line.matches(p3) | line.matches(p4))
				return true;
		}
		return false;
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

	public static boolean HasSimilarContent(LineClass2 lc1, LineClass2 lc2) {
		double threshold = 0.5;
		if (lc1.predLabel.equalsIgnoreCase("Research")) threshold = 0.7;

		double a1 = (double) lc1.wordList.size();
		double a2 = (double) lc2.wordList.size();
		if (!(a1 >= threshold * a2 && a2 >= threshold * a1))
			return false;

		int nSize1 = 0;
		Map<String, Integer> dic = new HashMap<String, Integer>();
		for (int k = 0; k < lc1.wordList.size(); k++) {
			dic.put(lc1.wordList.get(k), lc1.countList.get(k));
			nSize1 += lc1.countList.get(k).intValue();
		}
		int nMinMatch1 = (int) Math.ceil((double) nSize1 * threshold);

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
		int nMinMatch2 = (int) Math.ceil((double) nSize2 * threshold);

		if (nMatch >= nMinMatch1 && nMatch >= nMinMatch2)
			return true;
		return false;
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
	
	private static double SimScore(String a, String b) {
		a = a.replaceAll(" +", " ");
		String[] awords = a.split(" ");
		b = b.replaceAll(" +", " ");
		String[] bwords = b.split(" ");
		
		int nSize1 = awords.length;
		Map<String, Integer> dic = new HashMap<String, Integer>();
		for (int k = 0; k < nSize1; k++) {
			String word = awords[k];
			if (!dic.containsKey(word))
				dic.put(word, 0);
			dic.put(word, dic.get(word) + 1);
		}

		int nMatch = 0;
		int nSize2 = bwords.length;
		for (int k = 0; k < nSize2; k++) {
			String word = bwords[k];
			if (dic.containsKey(word)) {
				int n1 = dic.get(word).intValue();
				if (n1 > 0) {
					nMatch++;
					dic.put(word, dic.get(word) - 1);
				}
			}
		}

		return (double) nMatch / (double) Math.min(nSize1, nSize2);
	}
	
	public static boolean ShareNameEntity(LineClass2 lc1, LineClass2 lc2, String NE) {
		double count = 0;

		// ShareOrg
		if (NE.equalsIgnoreCase("ORGANIZATION")) {
			for (int i = 0; i < lc2.tag_org.size(); i++) {
				String org2 = lc2.tag_org.get(i);
				for (int j = 0; j < lc1.tag_org.size(); j++) {
					String org1 = lc1.tag_org.get(j);
					if (org2.equalsIgnoreCase(org1))
						count++;
					else if (org2.indexOf(org1) > -1) 
						count++;
					else if (org1.indexOf(org2) > -1)
						count++;
					else if (SimScore(org1, org2) >= 0.5)
						count++;
				}
			}
		}

		// ShareYear
		if (NE.equalsIgnoreCase("DATE")) {
			for (int i = 0; i < lc2.tag_date.size(); i++) {
				String date2 = lc2.tag_date.get(i);
				for (int j = 0; j < lc1.tag_date.size(); j++) {
					String date1 = lc1.tag_date.get(j);
					if (date2.equalsIgnoreCase(date1))
						count++;
				}
			}
		}

		// ShareNoun
		if (NE.equalsIgnoreCase("NOUN")) {
			for (int i = 0; i < lc2.tag_noun.size(); i++) {
				String noun2 = lc2.tag_noun.get(i);
				for (int j = 0; j < lc1.tag_noun.size(); j++) {
					String noun1 = lc1.tag_noun.get(j);
					if (noun2.equalsIgnoreCase(noun1))
						count++;
				}
			}
		}

		// ShareNumber
		if (NE.equalsIgnoreCase("NUMBER")) {
			String number1 = lc1.originalText.replaceAll("\\D", "");
			String number2 = lc2.originalText.replaceAll("\\D", "");
			if (number1.length() > 0 && number2.length() > 0 && number1.equalsIgnoreCase(number2))
				count++;
		}

		return (count > 0);
	}
}
