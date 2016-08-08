package experiment.specs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntitySpecClass {
	public Map<String, String> MapCateAttr; // Category - Attribute
	public Map<String, SpecClass> MapEntitySpecs; // Entity - SpecsLoader (i.e. Attribute/Value Map)
	
	public EntitySpecClass() {
		MapCateAttr = new HashMap<String, String>();
		MapEntitySpecs = new HashMap<String, SpecClass>();
	}
	
	public void Process(String dir) {
		try {
			LoadCateAttr(dir); // Category - Attribute 
			File f = new File(dir);
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; i++) {
				String fname = files[i].getName();
				if (fname.matches("-*\\d+\\.txt")) {
					String entityID = fname.replace(".txt", "");
					SpecClass sl = new SpecClass();
					sl.Process(files[i], MapCateAttr);
					MapEntitySpecs.put(entityID, sl);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void LoadCateAttr(String dir) throws IOException {
		String fn = dir + "reference.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
		String line = "";
		String attribute = "";
		while ((line = br.readLine()) != null) {
			if (line.indexOf("attribute=") > -1) {
				String[] segs = line.split("=");
				attribute = segs[1];
			}
			else {
				MapCateAttr.put(line, attribute);
			}
		}
		br.close();
	}
}
