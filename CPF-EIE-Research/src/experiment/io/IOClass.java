package experiment.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IOClass {
	public static Map<String, String> LoadWordDic(String fin) throws IOException {
		Map<String, String> dic = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(fin)));
		String line = "";
		while((line=br.readLine())!=null) {
			String[] segs = line.split("\t");
			if (segs.length < 2) {
				System.err.println("Unexpected word index.");
				return null;
			}
			if (!dic.containsKey(segs[0]))
				dic.put(segs[0], segs[1]);
		}
		br.close();
		return dic;
	}
	
	public static Map<String, String> LoadLabelDic(String fin) throws IOException {
		Map<String, String> dic = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(fin)));
		String line = "";
		while((line=br.readLine())!=null) {
			String[] segs = line.split("\t");
			if (segs.length < 2) {
				System.err.println("Unexpected label index.");
				return null;
			}
			if (!dic.containsKey(segs[0]))
				dic.put(segs[0], segs[1]);
		}
		br.close();
		return dic;
	}
	
	public static Map<String, String> LoadIDMap(String fn) throws IOException {
		Map<String, String> dic = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
		String line = "";
		while ((line=br.readLine()) != null) {
			String[] segs = line.split("\t", -1);
			if (!dic.containsKey(segs[0]))
				dic.put(segs[0], segs[1]);
		}
		br.close();
		return dic;
	}
}
