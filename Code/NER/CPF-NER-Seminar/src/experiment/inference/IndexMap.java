package experiment.inference;

public class IndexMap {
	private int nStates;
	private int nWords;
	private int nFeatures;
	
	public IndexMap(int nStates, int nWords, int nFeatures) {
		this.nStates = nStates;
		this.nWords = nWords;
		this.nFeatures = nFeatures;
	}
	
	public int GetIndexState(int stateID) {
		return stateID;
	}
	
	public int GetIndexStateFeatures(int stateID, int featureID) {
		return (stateID * nFeatures + featureID);
	}
	
	public int GetIndexStateToken(int stateID, int wordID) {
		return (stateID * nWords + wordID);
	}
	
	public int GetIndexStateStateFeature(int prevStateID, int stateID, int featureID) {
		return (prevStateID * nStates * nFeatures + stateID * nFeatures + featureID);
	}
	
	public int GetIndexStateStateToken(int prevStateID, int stateID, int wordID) {
		return (prevStateID * nStates * nWords + stateID * nWords + wordID);
	}
}
