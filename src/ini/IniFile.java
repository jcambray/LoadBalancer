package ini;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class IniFile {

//	public static void main(String[] args) throws IOException{
//		IniFile ini = new IniFile("config.ini");
//	}
	
	private Pattern _section = Pattern.compile("\\s*([^=]*)=(.*)");
	
	private Map<String, Map<String, String>> workerMap = new HashMap();
	private Map<String, String> ipPortMap = new HashMap();
	
	private ArrayList<String> arrayList = new ArrayList<String>();
	private ArrayList<String> arrayListNoWorker = new ArrayList<String>();
	
	public IniFile(String path) throws IOException {
		load(path);
	}

	public void load(String path) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				Matcher m = _section.matcher(line);
				if (m.matches()) {
					if(!arrayListNoWorker.contains(m.group(1).trim()))
						arrayListNoWorker.add(m.group(1).trim());
						
					arrayList.add(m.group(2).trim());
				}
			}
			
			
			int i = 0;
			while(arrayList.size()>i){
				String nbW  = arrayListNoWorker.get(i).replace("worker.", "");
				nbW  = nbW.replace(".ip", "");
				
				System.out.println("ipPortMap "+arrayList.get(i)+" - "+arrayList.get(i+1));
				System.out.println("workerMap "+nbW+" - "+ipPortMap);
				
				ipPortMap.put( arrayList.get(i), arrayList.get(i+1) );
				workerMap.put(nbW,ipPortMap);
				i=i+2;
				
			}
            
		}
	}

	public Map<String, Map<String, String>> getWorkerMap() {
		return workerMap;
	}

	public void setWorkerMap(Map<String, Map<String, String>> workerMap) {
		this.workerMap = workerMap;
	}

	
	
}

