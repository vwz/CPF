package experiment.inference;

public class IndexMap {
	private int nStates;
	private int nWords;
	private int nSignals;
	private int nFeatures;
	
	public IndexMap(int nStates, int nWords, int nFeatures) {
		this.nStates = nStates;
		this.nWords = nWords;
		this.nSignals = 4;
		this.nFeatures = nFeatures;
	}
	
	public int GetIndexState(int stateID) {
		return stateID;
	}
	
	public int GetIndexStateState(int prevStateID, int stateID) {
		return (prevStateID * nStates + stateID);
	}
	
	public int GetIndexStateToken(int stateID, int wordID) {
		return (stateID * nWords + wordID);
	}
	
	public int GetIndexStateEntitySignal(int stateID, int signalID) {
		return (stateID * nSignals + signalID);
	}
	
	public int GetIndexStateFeatures(int stateID, int featureID) {
		return (stateID * nFeatures + featureID);
	}
}
