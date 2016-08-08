package experiment.constraint;

import java.util.List;

import experiment.graph.LineClass2;
import experiment.graph.dataStructure;

public class ConstraintFeatureClass {
	public boolean f_flag;
	public boolean g_flag;

	public ConstraintFeatureClass() {
		f_flag = false;
		g_flag = false;
	}

	public void GetNodeFeatureSignal(String factorName, LineClass2 lc1, ObsClass obs1, List<LineClass2> variables, List<ObsClass> obsList, int i) {
		if (factorName.equalsIgnoreCase("Education_NotCourseAndBio")) {
			// Education_HasOrg: if line i is Education, then it has organization
			f_flag = lc1.predLabel.equalsIgnoreCase("Education") & !obs1.PrecededByCourse & (obs1.StartWithEducation | obs1.HasWordForEducation | obs1.PrecededByEducationKeywords);
			g_flag = lc1.predLabel.equalsIgnoreCase("Education");
		}
	}

	public void GetEdgeFeatureSignal(String factorName, LineClass2 lc1, LineClass2 lc2, ObsClass obs1, ObsClass obs2, dataStructure ds, List<ObsClass> obsList) {
		if (factorName.equalsIgnoreCase("Bio_Bio_Similar")) {
			// Bio_Bio_Similar: if line i and line j are both Bio, then they share the similar content
			double maxsim = -1;

			// similarity between lc1 and lc2
			double sim = ObsClass.Similarity(lc1, lc2);
			if (sim > maxsim) maxsim = sim;

			// similarity between lc1 and lc2.prev
			if (lc2.hasPrev) {
				int k = lc2.Prev;
				LineClass2 lc3 = ds.data.get(k);
				ObsClass obs3 = obsList.get(k);
				if (!obs3.HasNoMoreThanTenWords && !lc1.pageID.equals(lc3.pageID)) {
					sim = ObsClass.Similarity(lc1, lc3);
					if (sim > maxsim) maxsim = sim;
				}
			}

			// similarity between lc1 and lc2.next
			if (lc2.hasNext) {
				int k = lc2.Next;
				LineClass2 lc3 = ds.data.get(k);
				ObsClass obs3 = obsList.get(k);
				if (!obs3.HasNoMoreThanTenWords && !lc1.pageID.equals(lc3.pageID)) {
					sim = ObsClass.Similarity(lc1, lc3);
					if (sim > maxsim) maxsim = sim;
				}
			}

			f_flag = lc1.predLabel.equalsIgnoreCase("Bio") & lc2.predLabel.equalsIgnoreCase("Bio") & (maxsim >= 0.5); // we construct (lc1, lc2) pairs such that the mostSimScore must come from lc1
			g_flag = lc1.predLabel.equalsIgnoreCase("Bio") & lc2.predLabel.equalsIgnoreCase("Bio");
		}
		else if (factorName.equalsIgnoreCase("Employment_Bio_ShareOrg")) {
			// Employment_Bio_ShareOrg: if line i is Employment and line j is Bio, then they share organization
			g_flag = false;
			f_flag = false;

			if (lc1.predLabel.equalsIgnoreCase("Employment") & lc2.predLabel.equalsIgnoreCase("Bio")) {
				if (obs1.HasOrg) {
					g_flag = true;
					boolean IsOthers = !obs1.HasEntityName & lc1.tag_person.size() > 0;
					boolean IsEducation = obs1.HasWordForEducation | obs1.PrecededByEducationKeywords;
					boolean IsAddress = obs1.MatchStreet | obs1.MatchStateZip | obs1.StartWordForAddress;
					boolean EmploymentCondition = !IsEducation & !IsAddress & !obs1.HasWordForAward & !IsOthers & !obs1.HasWordForPresentation;
					if (EmploymentCondition & obs2.IsBioCandidate2) {
						f_flag = ObsClass.ShareNameEntity(lc1, lc2, "ORGANIZATION");

						if (lc2.hasNext) {
							int k = lc2.Next;
							LineClass2 lc3 = ds.data.get(k);
							f_flag = f_flag | ObsClass.ShareNameEntity(lc1, lc3, "ORGANIZATION");
						}

						if (lc2.hasPrev) {
							int k = lc2.Prev;
							LineClass2 lc3 = ds.data.get(k);
							f_flag = f_flag | ObsClass.ShareNameEntity(lc1, lc3, "ORGANIZATION");
						}
					} else if (EmploymentCondition & obs2.StartWordForBio) {
						f_flag = ObsClass.ShareNameEntity(lc1, lc2, "ORGANIZATION");

						if (lc2.hasNext) {
							int k = lc2.Next;
							LineClass2 lc3 = ds.data.get(k);
							f_flag = f_flag | ObsClass.ShareNameEntity(lc1, lc3, "ORGANIZATION");
						}
					}
				}
			} else if (lc2.predLabel.equalsIgnoreCase("Employment") & lc1.predLabel.equalsIgnoreCase("Bio")) {
				if (obs2.HasOrg) {
					g_flag = true;
					boolean IsOthers = !obs2.HasEntityName & lc2.tag_person.size() > 0;
					boolean IsEducation = obs2.HasWordForEducation | obs2.PrecededByEducationKeywords;
					boolean IsAddress = obs2.MatchStreet | obs2.MatchStateZip | obs2.StartWordForAddress;
					boolean EmploymentCondition = !IsEducation & !IsAddress & !obs2.HasWordForAward & !IsOthers & !obs2.HasWordForPresentation;
					if (EmploymentCondition & obs1.IsBioCandidate2) {
						f_flag = ObsClass.ShareNameEntity(lc1, lc2, "ORGANIZATION");

						if (lc1.hasNext) {
							int k = lc1.Next;
							LineClass2 lc3 = ds.data.get(k);
							f_flag = f_flag | ObsClass.ShareNameEntity(lc3, lc2, "ORGANIZATION");
						}

						if (lc1.hasPrev) {
							int k = lc1.Prev;
							LineClass2 lc3 = ds.data.get(k);
							f_flag = f_flag | ObsClass.ShareNameEntity(lc3, lc2, "ORGANIZATION");
						}
					} else if (EmploymentCondition & obs1.StartWordForBio) {
						f_flag = ObsClass.ShareNameEntity(lc1, lc2, "ORGANIZATION");

						if (lc1.hasNext) {
							int k = lc1.Next;
							LineClass2 lc3 = ds.data.get(k);
							f_flag = f_flag | ObsClass.ShareNameEntity(lc3, lc2, "ORGANIZATION");
						}
					} 
				}
			}
		}
	}

	public void Clear() {
		f_flag = false;
		g_flag = false;
	}
}
