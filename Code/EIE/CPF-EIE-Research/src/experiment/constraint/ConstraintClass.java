package experiment.constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstraintClass {
	public List<String> constraints;
	public Map<String, List<String>> ConsLabelIndex; // <constraint, list of labels it uses>
	public Map<String, List<String>> LabelConsIndex; // <label, list of constrains it applies to>
	public Map<String, String> ConsNodeOrEdgeIndex; // <constraint, node or edge indicator>
	public Map<String, String> ConsTypeIndex; // <constraint, whether Y-type indicator>
	
	/**
	 * This is to update the LabelConsIndex, s.t. <label, a list of constraints>
	 * @param factorName
	 * @param labels
	 */
	private void UpdateLabelConsIndex(String factorName, List<String> labels) {
		for (int i = 0; i < labels.size(); i++) {
			String label = labels.get(i);
			List<String> value = LabelConsIndex.get(label);
			value.add(factorName);
			LabelConsIndex.put(label, value);
		}
	}
	
	/**
	 * This is to update the ConsLabelIndex, s.t. <constraint, a list of labels>
	 * @param factorName
	 * @param labels
	 */
	private void UpdateConsLabelIndex(String factorName, List<String> labels) {
		ConsLabelIndex.put(factorName, labels);
	}
	
	public ConstraintClass(Map<String, String> labelDic) {
		constraints = new ArrayList<String>();
		ConsNodeOrEdgeIndex = new HashMap<String, String>();
		ConsTypeIndex = new HashMap<String, String>();
		ConsLabelIndex = new HashMap<String, List<String>>();
		LabelConsIndex = new HashMap<String, List<String>>();
		InitLabelConsIndex(labelDic);
		
		String factorName = "";
		List<String> value;
		
		/******** y-dept constraints *********/
		factorName = "Education_NotCourseAndBio";
		constraints.add(factorName);
		value = new ArrayList<String>();
		value.add("Education");
		UpdateLabelConsIndex(factorName, value);
		UpdateConsLabelIndex(factorName, value);
		ConsNodeOrEdgeIndex.put(factorName, "node");
		ConsTypeIndex.put(factorName, "y");
		
		factorName = "Bio_Bio_Similar";
		constraints.add(factorName);
		value = new ArrayList<String>();
		value.add("Bio");
		UpdateLabelConsIndex(factorName, value);
		UpdateConsLabelIndex(factorName, value);
		ConsNodeOrEdgeIndex.put(factorName, "edge");
		ConsTypeIndex.put(factorName, "y");
		
		factorName = "Employment_Bio_ShareOrg";
		constraints.add(factorName);
		value = new ArrayList<String>();
		value.add("Employment");
		value.add("Bio");
		UpdateLabelConsIndex(factorName, value);
		UpdateConsLabelIndex(factorName, value);
		ConsNodeOrEdgeIndex.put(factorName, "edge");
		ConsTypeIndex.put(factorName, "y");
	}
	
	/**
	 * This is to initialize LabelConsIndex to have each label, with an empty list of constraints
	 * @param labelDic
	 */
	private void InitLabelConsIndex(Map<String, String> labelDic) {
		LabelConsIndex = new HashMap<String, List<String>>();
		
		for (String label : labelDic.keySet()) {
			List<String> value = new ArrayList<String>();
			LabelConsIndex.put(label, value);
		}
	}
}
