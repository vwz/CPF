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
	
	public static Map<String, String> LoadUrlTime(String fn) throws IOException {
		Map<String, String> UrlTimeDic = new HashMap<String, String>();
		
		File input = new File(fn);
		if (input.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
			String line = "";
			while ((line=br.readLine()) != null) {
				String[] segs = line.split("\t");
				String pageID = segs[0];
				String url = segs[1];
				String time = "";
				if (segs.length == 3)
					time = segs[2];

				String key = pageID;
				String value = url + "\t" + time;
				if (!UrlTimeDic.containsKey(key))
					UrlTimeDic.put(key, value);
			}
			br.close();
		}
		
		return UrlTimeDic;
	}
	
	public static Map<String, String> LoadTime(String fn) throws IOException {
		Map<String, String> timeDic = new HashMap<String, String>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
		String line = "";
		while ((line=br.readLine()) != null) {
			String[] segs = line.split("\t");
			String key = segs[0]; // "pageID lineID"
			String value = segs[1]; // time
			if (!timeDic.containsKey(key))
				timeDic.put(key, value);
		}
		br.close();
		
		return timeDic;
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
