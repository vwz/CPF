package experiment.specs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecClass {
	public Map<String, List<SingleSpecClass>> MapAttrSpec; // Attribute - A list of specs.

	public SpecClass() {
		MapAttrSpec = new HashMap<String, List<SingleSpecClass>>();
	}

	public void Process(File file, Map<String, String> MapCateAttr) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			String attribute = "";
			while ((line = br.readLine()) != null) {
				if (line.indexOf("=") > -1) {
					String[] segs = line.split("=");
					String category = segs[1];
					attribute = MapCateAttr.get(category);
					if (!MapAttrSpec.containsKey(attribute)) {
						List<SingleSpecClass> specList = new ArrayList<SingleSpecClass>();
						MapAttrSpec.put(attribute, specList);
					}
				} else {
					SingleSpecClass sc = new SingleSpecClass();
					sc.Process(line, attribute);
					MapAttrSpec.get(attribute).add(sc);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
