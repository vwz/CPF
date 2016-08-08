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
		factorName = "CONJ_notinlast";
		constraints.add(factorName);
		value = new ArrayList<String>();
		value.add("CONJ");
		UpdateLabelConsIndex(factorName, value);
		UpdateConsLabelIndex(factorName, value);
		ConsNodeOrEdgeIndex.put(factorName, "node");
		ConsTypeIndex.put(factorName, "y");
		
		factorName = "NUM_NUM_close";
		constraints.add(factorName);
		value = new ArrayList<String>();
		value.add("NUM");
		UpdateLabelConsIndex(factorName, value);
		UpdateConsLabelIndex(factorName, value);
		ConsNodeOrEdgeIndex.put(factorName, "edge");
		ConsTypeIndex.put(factorName, "y");
		
		factorName = "ADJ_NUM_faraway";
		constraints.add(factorName);
		value = new ArrayList<String>();
		value.add("ADJ");
		value.add("NUM");
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
