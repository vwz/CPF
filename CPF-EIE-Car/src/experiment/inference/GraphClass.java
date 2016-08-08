package experiment.inference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import experiment.graph.LineClass2;
import experiment.graph.dataStructure;

public class GraphClass {
	
	public GraphClass() { }

	/**
	 * Step 1. Construct the graph by maintaining an EdgeStructClass
	 * @param fin: training data file
	 * @return
	 * @throws IOException
	 */
	public EdgeStructClass ConstructGraph(dataStructure ds, Map<String, String> labelDic) {
		// Link data records in the same Table/List
		List<int[]> adj = LinkSameListRecords(ds.data);
		//List<int[]> adj = new ArrayList<int[]>();
		
		int nStates = labelDic.size();
		EdgeStructClass edgeStruct = new EdgeStructClass(ds.data.size(), nStates);
		edgeStruct.MakeEdgeVE(adj);
		return edgeStruct;
	}
	
	private List<int[]> LinkSameListRecords(List<LineClass2> data) {
		List<int[]> adj = new ArrayList<int[]>();
		
		int[] nLIArray = new int[data.size()];
		int[] lineIDArray = new int[data.size()];
		String[] pageIDArray = new String[data.size()];
		for (int i = 0; i < data.size(); i++) {
			LineClass2 lc = data.get(i);
			lineIDArray[i] = Integer.valueOf(lc.lineID).intValue();
			pageIDArray[i] = lc.pageID;
			String path = CleanHTMLPath(lc.path);
			nLIArray[i] = GetLICount(path);
		}
		
		for (int i = 1; i < data.size(); i++) {
			int lineID1 = lineIDArray[i];
			String pageID1 = pageIDArray[i];
			int nLI1 = nLIArray[i];
			
			if (nLI1 > 0) {
				int j = i - 1;
				int lineID2 = lineIDArray[j];
				String pageID2 = pageIDArray[j];
				int nLI2 = nLIArray[j];
				
				if (pageID2.equalsIgnoreCase(pageID1)) { // same page
					if ((lineID1-lineID2) == (i-j)) { // consecutive lines
						if (nLI1 >= nLI2) {
							int[] edge = new int[]{j, i};
							adj.add(edge);
						}
					}
				}
			}
		}
		
		return adj;
	}
	
	private String CleanHTMLPath(String path2) {
		String path = path2;
		path = path.replace(".A.", ".");
		path = path.replace(".B.", ".");
		path = path.replace(".I.", ".");
		path = path.replace(".STRONG.", ".");
		path = path.replace(".EM.", ".");
		return path;
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
}
