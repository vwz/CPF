package experiment.constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import experiment.graph.EntityDataClass;
import experiment.graph.GetDataDomain;
import experiment.graph.LineClass2;
import experiment.graph.dataStructure;

public class EntityObsClass {
	public Map<String, List<ObsClass>> EntityObsList;

	public EntityObsClass() {
		this.EntityObsList = new HashMap<String, List<ObsClass>>();
	}

	public void Process(EntityDataClass edc, GetDataDomain gdd) {
		for (Entry<String, dataStructure> e : edc.entityData.entrySet()) {
			String entity = e.getKey();
			String name = gdd.nameDic.get(entity);
			dataStructure ds = e.getValue();

			List<ObsClass> obsList = new ArrayList<ObsClass>();

			for (int i = 0; i < ds.data.size(); i++) {
				LineClass2 lcc = ds.data.get(i);			
				ObsClass obs = new ObsClass();

				// get observations that do not require context
				obs.CollectObs(lcc, name);
				obsList.add(obs);
			}

			for (int i = 0; i < ds.data.size() - 1; i++) {
				// get observations that require context
				int LI2 = obsList.get(i + 1).nLI;
				int LI1 = obsList.get(i).nLI;
				if (LI2 > 0) {
					if (LI2 - LI1 == 1) {
						ObsClass obs = obsList.get(i);
						obs.IsListStarter = true;
						obsList.set(i, obs);
					}
				}
			}

			// to be used in both education and employment
			List<Integer> BioCandList = GetBioCandidate(obsList);
			for (int i = 0; i < ds.data.size(); i++) {
				LineClass2 lcc = ds.data.get(i);

				boolean flag = MatchOrgAtLeastOnce(BioCandList, ds.data, lcc, i);
				ObsClass obs = obsList.get(i);
				obs.HasOrgInBioCandidate = flag;
				obsList.set(i, obs);
			}

			// for education
			for (int i = 0; i < ds.data.size(); i++) {
				LineClass2 lcc = ds.data.get(i);
				ObsClass obs = obsList.get(i);

				if (lcc.hasNext) {
					ObsClass obs2 = obsList.get(i + 1);
					if (obs2.HasOrgInBioCandidate && obs2.HasOrgForEducation) {
						obs.FollowedByEducationOrgInBio = true;
					}
				}
				if (lcc.hasPrev) {
					ObsClass obs2 = obsList.get(i - 1);
					if (obs2.StartWordForBio && obs2.HasNoMoreThanSixWords)
						obs.PrecededByBioKeywords = true;
				}
				if (lcc.hasPrev) {
					ObsClass obs2 = obsList.get(i - 1);
					if ((obs2.HasWordForEducation || obs2.StartWithEducation) && obs2.HasNoMoreThanTenWords) {
						obs.PrecededByEducationKeywords = true;
					}
					
					if (obs2.HasOrgInBioCandidate && obs2.HasOrgForEducation) {
						obs.PrecededByEducationOrgInBio = true;
					}
					if ((obs2.HasWordForPresentation || obs2.StartWordForPresentation) && (obs2.HasNoMoreThanTenWords || obs.nLI > 0)) {
						obs.PrecededByPresentationKeywords = true;
					}
					if (obs2.PrecededByPresentationKeywords && obs2.nLI > 0 && obs2.nLI == obs.nLI) {
						obs.PrecededByPresentationKeywords = true;
					}
					if (obs2.HasEmailKeywords && obs2.HasNoMoreThanThreeWords) {
						obs.PrecededByEmailKeywords = true;
					}
				}
				if (lcc.hasPrev) {
					LineClass2 lcc2 = ds.data.get(i - 1);
					ObsClass obs2 = obsList.get(i - 1);
					if (SimilarLength(lcc, lcc2)) {
						if (obs2.MatchStreet || obs2.MatchStateZip || obs2.StartWordForAddress || obs2.IsLocation || obs2.StartNumberWordCapitalized) {
							obs.SurroundedByAddress = true;
						}
					}
					if (lcc2.originalText.indexOf("course") > -1 || lcc2.originalText.indexOf("teach") > -1) {
						if (obs.InListOrTable) {
							obs.PrecededByCourse = true;
						}
					}
					if (obs2.PrecededByCourse && obs.InListOrTable) {
						obs.PrecededByCourse = true;
					}
				}
				if (lcc.hasNext) {
					LineClass2 lcc2 = ds.data.get(i + 1);
					ObsClass obs2 = obsList.get(i + 1);
					if (SimilarLength(lcc, lcc2) && !obs2.StartWordForAddress) {
						if (obs2.MatchStreet || obs2.MatchStateZip || obs2.IsLocation || obs2.StartNumberWordCapitalized) {
							obs.SurroundedByAddress = true;
						}
					}
				}
				obsList.set(i, obs);
			}

			// for BioBioSimilar constraint
			for (int i = 0; i < ds.data.size(); i++) {
				LineClass2 lcc1 = ds.data.get(i);

				if (lcc1.countIDList.size() > 0) {
					double maxsim = -1;
					int mostSimID = -1;

					for (int j = 0; j < ds.data.size(); j++) {
						if (j == i) continue;
						LineClass2 lcc2 = ds.data.get(j);
						if (lcc1.pageID.equals(lcc2.pageID)) continue; // same page, then don't count the similarity
						
						if (lcc1.countIDList.size() > 0) {
							// this similarity only makes sense when lcc1 and lcc2 both have words (because we removed names from text)
							double sim = ObsClass.Similarity(lcc1, lcc2);
							if (sim > maxsim) {
								maxsim = sim;
								mostSimID = j;
							}
						}
					}
					
					ObsClass obs = obsList.get(i);
					obs.mostSimScore = maxsim; 
					obs.mostSimID = mostSimID; 
				}
			}

			EntityObsList.put(entity,  obsList);
		}
	}

	private boolean SimilarLength(LineClass2 lcc1, LineClass2 lcc2) {
		if (Math.abs(lcc1.countList.size() - lcc2.countIDList.size()) < 5)
			return true;
		return false;
	}

	private boolean MatchOrgAtLeastOnce(List<Integer> BioCandList, List<LineClass2> data, LineClass2 lcc, int index) {
		if (lcc.tag_org.size() > 0) {
			for (int i = 0; i < BioCandList.size(); i++) {
				int j = BioCandList.get(i); // the i-th bio candidate's index in the data
				if (j == index) continue; // exclude itself

				List<String> orgList = data.get(j).tag_org;
				for (int k = 0; k < lcc.tag_org.size(); k++) {
					String org = lcc.tag_org.get(k);
					if (orgList.contains(org)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private List<Integer> GetBioCandidate(List<ObsClass> obsList) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < obsList.size(); i++) {
			if (obsList.get(i).IsBioCandidate) {
				list.add(i);
			}
		}
		return list;
	}
}