package domain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import enums.ConfigType;

public class ConfigFinder {
	private ArrayList<String> webConfigs;
	private ArrayList<String> profileConfigs;
	private ArrayList<String> webConfigsInFunc;
	private ArrayList<String> webConfigsInHDDB;
	private ArrayList<String> profileConfigsInHDDB;
	private ArrayList<String> linesRead;
	private String unitName;
	
	private ArrayList<String> foundWebConfigs;
	private ArrayList<String> foundProfileConfigs;
	private ArrayList<String> notFoundWebConfigs;
	private ArrayList<String> notFounProfileConfigs;
	
	public ConfigFinder() {
		webConfigs = new ArrayList<String>();
		profileConfigs = new ArrayList<String>();
		webConfigsInFunc = new ArrayList<String>(); 
		webConfigsInHDDB = new ArrayList<String>();
		profileConfigsInHDDB = new ArrayList<String>();
		linesRead = new ArrayList<String>();
		unitName = "";
		
		foundWebConfigs = new ArrayList<String>();
		foundProfileConfigs = new ArrayList<String>();
		
		notFoundWebConfigs = new ArrayList<String>();
		notFounProfileConfigs = new ArrayList<String>();
	}
	private ArrayList<String> configTypeToArrayList(ConfigType configType){
		if(configType == ConfigType.WEBCONFIG) {
			return webConfigs;
		}
		else if(configType == ConfigType.PROFILECONFIG) {
			return profileConfigs;
		}
		else {
			System.out.println("INVALID CONFIG");
			return null;
		}
	}
	public void printArrayList(String listName,ArrayList<String> aArrayList) {
		int listSize = aArrayList.size();
		System.out.println(listName.toUpperCase()+":");
		System.out.println("TOTAL FOUND: "+Integer.toString(listSize));
		System.out.println("-----------------------------------------------------------------------------");
		for(int i = 0 ; i < listSize; i++) {
			System.out.println(aArrayList.get(i));
		}
		System.out.println("-----------------------------------------------------------------------------");
	}
	
	public void scanAndLoadConfigsFromFile(String inputFileName) throws IOException {
		final String WEB_CONFIG_PATTERN = "/CONFIGDATA/";
		final String GETCONFIG_STRING_PATTERN = "GETCONFIGSTRING('";
		final String GETCONFIG_BOOLEAN_PATTERN = "GETCONFIGBOOLEAN('";
		final String PROFILE_CONFIG_PATTERN = "/PROFILE/";
		final String UNIT_PATTERN = "UNIT";
		
		String line = "", originalLine = "";
		FileReader fileReader = new FileReader(inputFileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		line = bufferedReader.readLine();
		while(line != null) {
			originalLine = line;
			line = line.toUpperCase();
			line = line.trim().replaceAll(" +", " "); // will replace multuple spaces in a line with singe space;
			line = line.replaceAll(" \\(", "\\(");
			line = line.replaceAll(", ", ",");
			line = line.replaceAll(" ,", "");
			line = line.trim();
			
			/* Ignore all comments. However, not comments that are at the end of a line like:
			 * if (ATranType = 'GETPROFILEAJAX') then begin // this won't be ignored
			 */
			if(line.startsWith("//")) {
				line = bufferedReader.readLine();
				continue;
			}
			if(line.startsWith("{") && line.endsWith("}")) {
				line = bufferedReader.readLine();
				continue;
			}
			if(line.startsWith("{")) {
				line = bufferedReader.readLine();
				while(!line.endsWith("}") && !line.startsWith("}") && !line.contains("}")) {
					line = bufferedReader.readLine();
				}
				line = bufferedReader.readLine();
				continue;
			}
			linesRead.add(originalLine);
			// single if to get the unit name
			if(line.startsWith(UNIT_PATTERN) && (unitName.length() == 0)) {
				unitName = extractUnitNameFromLine(line);
			}

			if(line.contains(WEB_CONFIG_PATTERN)) {
				webConfigs.add(line);
			}
			else if(line.contains(GETCONFIG_STRING_PATTERN)) {
				addToList(webConfigsInFunc, line, GETCONFIG_STRING_PATTERN);
			}
			else if(line.contains(GETCONFIG_BOOLEAN_PATTERN)) {
				addToList(webConfigsInFunc, line, GETCONFIG_BOOLEAN_PATTERN);				
			}
			else if(line.contains(PROFILE_CONFIG_PATTERN)) {
				profileConfigs.add(line);
			}
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
		if(unitName.length() == 0) {
			unitName = "WWWROOT";
		}
		System.out.println("INPUT UNIT: "+unitName);
		System.out.println("FINSHED SCANNING FOR CONFIGS:");
		System.out.println("TOTAL WEB CONFIGS FOUND: "+Integer.toString(webConfigs.size()+webConfigsInFunc.size()));
		System.out.println("TOTAL PROFILE XML CONFIGS FOUND: "+Integer.toString(profileConfigs.size()));
	}
	public String extractUnitNameFromLine(String line) {
		String arr[] = line.split(" ");
		line = arr[1];
		line = line.replaceAll(";", "");
		return line;
	}
	private void addToList(ArrayList<String> list, String line, String extractionPattern) {
		if(extractionPattern.length() > 0) {
			line = extractConfigWithFuncFromTraget(line, extractionPattern);
			if(!list.contains(line)) {
				list.add(line);
			}			
		}
	}
	public void loadWebConfigsInHDDBFromFile(String fileName) throws IOException {
		String xPath = "";
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while((xPath = bufferedReader.readLine()) != null) {
			xPath = xPath.toUpperCase().trim();
			webConfigsInHDDB.add(xPath);
		}
		bufferedReader.close();
		System.out.println("FINSHED LOADING WEBCONFIGS IN HDDB FROM FILE:");
		System.out.println("TOTAL WEBCONFIGS: "+Integer.toString(this.webConfigsInHDDB.size()));	
	}
	public void loadProfileConfigsInHDDBFromFile(String fileName) throws IOException {
		String xPath = "";
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while((xPath = bufferedReader.readLine()) != null) {
			xPath = xPath.toUpperCase().trim();
			profileConfigsInHDDB.add(xPath);
		}
		bufferedReader.close();
		System.out.println("FINSHED LOADING PROFILE CONFIGS IN HDDB FROM FILE:");
		System.out.println("TOTAL PROFILE CONFIGS: "+Integer.toString(profileConfigsInHDDB.size()));	
	}
	
	public void printConfig(ConfigType configType) {
		ArrayList<String> targetList = configTypeToArrayList(configType);
		if(targetList != null) {
			printArrayList(configType.toString(),targetList);
		}
	}
	
	public void write(String load, String location, Boolean append) throws IOException {
		FileWriter fileWriter = new FileWriter(location, append); // appends to existing file if true is passed
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(load);
		bufferedWriter.close();
	}
	private String getArrayListAsFileLines(String headerForLines, ArrayList<String> aArrayList) {
		String lines = "";
		String lineDemarcator = "-----------------------------------------------------------------------------------------";
		int listSize = aArrayList.size();
		
		lines += headerForLines.toUpperCase()+":" + System.lineSeparator();
		lines += "TOTAL FOUND: "+Integer.toString(listSize) + System.lineSeparator();
		lines += lineDemarcator + System.lineSeparator();
		
		for(int i = 0 ; i < listSize; i++) {
			lines += aArrayList.get(i) + System.lineSeparator(); 
		}
		lines += lineDemarcator + System.lineSeparator();
		return lines;
	}
	
	public void writeFoundConfigToFile(String fileName) throws IOException {
		String validWebConfigsAsLines = "", validProfileConfigsAsLines = ""; 
		
		validWebConfigsAsLines = getArrayListAsFileLines(ConfigType.WEBCONFIG.toString(), foundWebConfigs);
		validProfileConfigsAsLines = getArrayListAsFileLines(ConfigType.PROFILECONFIG.toString(), foundProfileConfigs);
		
		write(validWebConfigsAsLines, fileName, true);
		write(validProfileConfigsAsLines, fileName, true);
		System.out.println("FINISHED WRITING VALID CONFIGS TO FILE: "+fileName);
	}
	public void writeNotFoundConfigToFile(String fileName) throws IOException {
		String inValidWebConfigsAsLines = "", inValidProfileConfigsAsLines = ""; 
		
		inValidWebConfigsAsLines = getArrayListAsFileLines(ConfigType.WEBCONFIG.toString(), notFoundWebConfigs);
		inValidProfileConfigsAsLines = getArrayListAsFileLines(ConfigType.PROFILECONFIG.toString(), notFounProfileConfigs);
		
		write(inValidWebConfigsAsLines, fileName, true);
		write(inValidProfileConfigsAsLines, fileName, true);
		System.out.println("FINISHED WRITING INVALID CONFIGS TO FILE: "+fileName);
	}
	
	public void writeConfigsInGetConfigFunction(String fileName) throws IOException {
		String ConfigInFuncHeader = "GetConfigString(...)/GetConfigBoolean(...)".toUpperCase();
		String validConfigs = ""; 
		validConfigs = getArrayListAsFileLines(ConfigInFuncHeader, webConfigsInFunc) + validConfigs;
		write(validConfigs, fileName, true);
		System.out.println("FINISHED WRITING VALID CONFIGS FOR: "+ConfigInFuncHeader);
	}
	
	private ArrayList<String> getValidXPathDefForConfig(ArrayList<String> hayList, ArrayList<String> needleList){
		ArrayList<String> validXPath = new ArrayList<String>();
		String hayStack = "", needle = "";
		int hayStackListSize = hayList.size();
		int needleListSize = needleList.size();
		for(int i = 0; i<needleListSize; i++) {
			needle = needleList.get(i);
			for(int j = 0; j<hayStackListSize; j++) {
				hayStack = hayList.get(j);
				if(hayStack.contains(needle)) {
					if(!validXPath.contains(needle)) {
						validXPath.add(needle);
					}
				}
			}
		}
		return validXPath;
	}	
	
	public void search() {
		searchXPathFromHDDBInScannedXPathLineFromFile(webConfigs, webConfigsInHDDB, foundWebConfigs, notFoundWebConfigs);
		searchXPathFromHDDBInScannedXPathLineFromFile(profileConfigs, profileConfigsInHDDB, foundProfileConfigs, notFounProfileConfigs);
	}
	
	private void searchXPathFromHDDBInScannedXPathLineFromFile(ArrayList<String> scannedXPathFromFile, ArrayList<String>  loadedXPathFromHDDB, ArrayList<String>  foundListToDump, ArrayList<String>  notfoundListToDump) {
		String aScannedLineContainingXPathFromFile = "", aXPathFromHDDB = "";
		
		int totalScannedXPaths = scannedXPathFromFile.size();
		int totalXPathFromHDDB = loadedXPathFromHDDB.size();
		
		foundListToDump.clear();
		notfoundListToDump.clear();
		boolean xpathAdded; 
		
		if(totalScannedXPaths > 0) { // search only if xpaths were scanned
			for(int i = 0; i < totalXPathFromHDDB; i++) {
				aXPathFromHDDB = loadedXPathFromHDDB.get(i);
				xpathAdded = false;
				for(int j = 0; j < totalScannedXPaths; j++) {
					aScannedLineContainingXPathFromFile = scannedXPathFromFile.get(j);
					
					if(aScannedLineContainingXPathFromFile.contains(aXPathFromHDDB)) {
						if(!foundListToDump.contains(aXPathFromHDDB)) {
							foundListToDump.add(aXPathFromHDDB);
							xpathAdded = true;
							break;
						}
					}
				}
				if(!xpathAdded) {
					if(!notfoundListToDump.contains(aXPathFromHDDB)) {
						notfoundListToDump.add(aXPathFromHDDB);
						xpathAdded = false;
					}
				}
			}			
		}
	}
	
	public void clearFile(String fileName) throws IOException {
		write("UNIT: "+unitName+System.lineSeparator(), fileName, false);
	}
	
	private String extractConfigWithFuncFromTraget(String target, String targetPattern) {
		int i = target.indexOf(targetPattern);
		target = target.substring(i);
		i = target.indexOf(")");
		target = target.substring(0, i+1);
		return target;
	}
	
	public void dumpInputFileWithoutComments(String fileName) throws IOException {
		System.out.println("WRITTING INPUT FILE WITHOUT COMMENTS.....");
		String lines = getArrayListAsFileLines("input file", linesRead);
		write(lines, fileName, false);
		System.out.println("FINISHED WRITTING INPUT FILE WITHOUT COMMENTS.");
	}
	
	public void setWebConfigListFromHDDB(String hddbConnStr) throws SQLException {
		ConfigFinderDBModule hddb = new ConfigFinderDBModule(hddbConnStr);
		webConfigsInHDDB = hddb.getWebXPathList();
		System.out.println("WEB CONFIGS LOADED FROM HDDB");
		System.out.println("TOTAL WEB CONFIG: "+Integer.toString(webConfigsInHDDB.size()));
	}
	public void setProfileConfigListFromHDDB(String hddbConnStr) throws SQLException {
		ConfigFinderDBModule hddb = new ConfigFinderDBModule(hddbConnStr);
		profileConfigsInHDDB = hddb.getProfileXPathList();
		System.out.println("PROFILE CONFIGS LOADED FROM HDDB");
		System.out.println("TOTAL PROFILE CONFIG: "+Integer.toString(profileConfigsInHDDB.size()));
	}
	
	public void dumpFoundConfigsToLocalDB(String localDBConnStr) throws SQLException {
		System.out.println("XPathDump:");
		String ConfigInFuncHeader = "GetConfigString(...)/GetConfigBoolean(...)".toUpperCase();
		
		ConfigFinderDBModule db = new ConfigFinderDBModule(localDBConnStr);
		db.foundXpathDump(foundWebConfigs,unitName,ConfigType.WEBCONFIG.toString());
		db.foundXpathDump(webConfigsInFunc,unitName,ConfigInFuncHeader);
		db.foundXpathDump(foundProfileConfigs,unitName,ConfigType.PROFILECONFIG.toString());
		
		System.out.println("FINISHED DUMPING VALID XPATH.");
	}
	public void dumpNotFoundConfigsToLocalDB(String localDBConnStr) throws SQLException {
		System.out.println("InvalidXPathDump:");
		ConfigFinderDBModule db = new ConfigFinderDBModule(localDBConnStr);
		db.notFoundXpathDump(notFoundWebConfigs,unitName,ConfigType.WEBCONFIG.toString());
		db.notFoundXpathDump(notFounProfileConfigs,unitName,ConfigType.PROFILECONFIG.toString());
		System.out.println("FINISHED DUMPING INVALID XPATH.");
	}
	
	public void dumpScannedConfigDetailsToLocalDB(String localDBConnStr) throws SQLException {
		System.out.println("XPathFileScanDetails:");
		ConfigFinderDBModule db = new ConfigFinderDBModule(localDBConnStr);
		
		int scanned_webconfig = webConfigs.size(); 
		int loaded_hddb_webconfig = webConfigsInHDDB.size();
		int found_webconfig = foundWebConfigs.size();
		int notfound_webconfig = notFoundWebConfigs.size();
		int webconfig_in_getxmlconfigfunc = webConfigsInFunc.size();
		
		int scanned_profile_config = profileConfigs.size();
		int loaded_hddb_profile_config = profileConfigsInHDDB.size();
		int found_profile_config = foundProfileConfigs.size();
		int notfound_profileconfig = notFounProfileConfigs.size();
		
		
		db.xPathFileSearchDetailsDump(unitName, scanned_webconfig, loaded_hddb_webconfig, found_webconfig, notfound_webconfig, webconfig_in_getxmlconfigfunc, scanned_profile_config, loaded_hddb_profile_config, found_profile_config, notfound_profileconfig);
		System.out.println("FINISHED DUMPING VALID XPATH SEARCH DETAILS.");
	}

}
