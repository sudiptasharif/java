package domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import enums.XPathType;

public class XPathFinder {
	private File file;
	private XPathLoader loadedXPath;
	
	private ArrayList<String> scannedConfigDataXPath;
	private ArrayList<String> scannedProfileXPath;
	private ArrayList<String> scannedGetXmlConfigFunction;
	
	private ArrayList<String> foundConfigDataXPath;
	private ArrayList<String> notFoundConfigDataXPath;
	
	private ArrayList<String> foundProfileXPath;
	private ArrayList<String> notFoundProfileXPath;
	
	private ArrayList<String> fileScannedLines;
	
	public XPathFinder(File inputFile, XPathLoader xpathLoader) {
		file = inputFile;
		loadedXPath = xpathLoader;
		
		scannedConfigDataXPath = new ArrayList<String>();
		foundConfigDataXPath = new ArrayList<String>();
		notFoundConfigDataXPath = new ArrayList<String>();
		
		scannedGetXmlConfigFunction = new ArrayList<String>();
		
		scannedProfileXPath = new ArrayList<String>();
		foundProfileXPath = new ArrayList<String>();
		notFoundProfileXPath = new ArrayList<String>();
	
		fileScannedLines = new ArrayList<String>(); 
	}
	
	public void scanFile() throws IOException {
		final String PASCAL_FILEEXT = ".PAS";
		final String WEB_CONFIG_PATTERN = "/CONFIGDATA/";
		final String GETCONFIG_STRING_PATTERN = "GETCONFIGSTRING('";
		final String GETCONFIG_BOOLEAN_PATTERN = "GETCONFIGBOOLEAN('";
		final String PROFILE_CONFIG_PATTERN = "/PROFILE/";
		boolean ignoreComments = false;
		
		String line = "", originalLine = "", fileName = "";
		fileName = file.getAbsolutePath().toUpperCase().trim();
		
		if(fileName.endsWith(PASCAL_FILEEXT)) {
			ignoreComments = true;
		}
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		line = bufferedReader.readLine();
		while(line != null) {
			originalLine = line; // will retain line format
			fileScannedLines.add(originalLine);
			
			// dismantle line format for scanning
			line = line.toUpperCase();
			line = line.trim().replaceAll(" +", " "); // will replace multiple spaces in a line with single space;
			line = line.replaceAll(" \\(", "\\("); // will replace ' '( with ''( -- remove space before ( 
			line = line.replaceAll(", ", ",");// remove space after ,
			line = line.replaceAll(" ,", "");// remove space before ,
			line = line.trim();	
			
			if(ignoreComments) {
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
			}
			if(line.contains(WEB_CONFIG_PATTERN)) {
				scannedConfigDataXPath.add(line);
			}			
			else if(line.contains(GETCONFIG_STRING_PATTERN)) {
				addToList(scannedGetXmlConfigFunction, line, GETCONFIG_STRING_PATTERN);
			}
			else if(line.contains(GETCONFIG_BOOLEAN_PATTERN)) {
				addToList(scannedGetXmlConfigFunction, line, GETCONFIG_BOOLEAN_PATTERN);				
			}
			else if(line.contains(PROFILE_CONFIG_PATTERN)) {
				scannedProfileXPath.add(line);
			}
			line = bufferedReader.readLine();
		}
	}
	private void addToList(ArrayList<String> list, String line, String extractionPattern) {
		if(extractionPattern.length() > 0) {
			line = extractGetXmlConfigFunctionFromLine(line, extractionPattern);
			if(!list.contains(line)) {
				list.add(line);
			}			
		}
	}
	private String extractGetXmlConfigFunctionFromLine(String target, String targetPattern) {
		int i = target.indexOf(targetPattern);
		target = target.substring(i);
		i = target.indexOf(")");
		target = target.substring(0, i+1);
		return target;
	}
	public void searchForXPath() {
		searchXPathFromHDDBInScannedXPathFromFile(
				loadedXPath.existingFoundXPathList, // this has both config data and profile xpaths
				scannedConfigDataXPath,
				loadedXPath.hddbConfigDataXPathList,
				foundConfigDataXPath,
				notFoundConfigDataXPath);
		
		searchXPathFromHDDBInScannedXPathFromFile(
				loadedXPath.existingFoundXPathList,
				scannedProfileXPath,
				loadedXPath.hddbProfileXPathList,
				foundProfileXPath,
				notFoundProfileXPath);
	}
	private void searchXPathFromHDDBInScannedXPathFromFile(ArrayList<String> existingFoundXPath, 
															ArrayList<String> scannedXPathFromFile, 
															ArrayList<String>  loadedXPathFromHDDB, 
															ArrayList<String>  dumpCurrentFound, 
															ArrayList<String>  dumpCurrentNotFound) {
		String aScannedLineContainingXPathFromFile = "", aXPathFromHDDB = "";
		
		int totalScannedXPaths = scannedXPathFromFile.size();
		int totalXPathFromHDDB = loadedXPathFromHDDB.size();
		
		dumpCurrentFound.clear();
		dumpCurrentNotFound.clear();
		boolean xpathAdded; 
		
		if(totalScannedXPaths > 0) { // search only if xpaths were scanned
			for(int i = 0; i < totalXPathFromHDDB; i++) {
				aXPathFromHDDB = loadedXPathFromHDDB.get(i);
				if(existingFoundXPath.contains(aXPathFromHDDB)) {
					continue; // if it was previously found then don't search for it again, go to the next one
				}
				xpathAdded = false;
				for(int j = 0; j < totalScannedXPaths; j++) {
					aScannedLineContainingXPathFromFile = scannedXPathFromFile.get(j);
					
					if(aScannedLineContainingXPathFromFile.contains(aXPathFromHDDB)) {
						if(!dumpCurrentFound.contains(aXPathFromHDDB)) {
							dumpCurrentFound.add(aXPathFromHDDB);
							xpathAdded = true;
							break;
						}
					}
				}
				if(!xpathAdded) { 
					if(!dumpCurrentNotFound.contains(aXPathFromHDDB)) {
						dumpCurrentNotFound.add(aXPathFromHDDB);
						xpathAdded = false;
					}
				}
			}			
		}
	}
	private String extractFileName(String filepath) {
		String fileParts[] = filepath.split("\\\\");
		String filename = fileParts[fileParts.length-1];
		return filename;
	}	
	public void dumpXPaths() throws IOException, SQLException {
		String configType = "";
		String filepath = this.file.getAbsolutePath().trim();
		String filename = extractFileName(filepath);
		XPathFinderDBUtility db = new XPathFinderDBUtility(loadedXPath.localdbConnString);
		
		configType = "CONFIGDATA";
		//found
		db.dumpFoundXPathToLocalDB(foundConfigDataXPath, configType, filepath, filename);
		db.dumpFoundXPathToLocalDB(scannedGetXmlConfigFunction, configType, filepath, filename);
		//not found
		db.dumpNotFoundXPathToLocalDB(notFoundConfigDataXPath, configType, filepath, filename);
		
		configType = "PROFILE";
		//found
		db.dumpFoundXPathToLocalDB(foundProfileXPath, configType, filepath, filename);
		//not found
		db.dumpNotFoundXPathToLocalDB(notFoundProfileXPath, configType, filepath, filename);
	}
	
	public void dumpDetails() throws IOException, SQLException {
		String filepath = file.getAbsolutePath().trim();
		String filename = extractFileName(filepath);
		XPathFinderDBUtility db = new XPathFinderDBUtility(loadedXPath.localdbConnString);
		
		int scanned_webconfig = scannedConfigDataXPath.size(); 
		int loaded_hddb_webconfig = loadedXPath.hddbConfigDataXPathList.size();
		int found_webconfig = foundConfigDataXPath.size();
		int notfound_webconfig = notFoundConfigDataXPath.size();
		
		int webconfig_in_getxmlconfigfunc = scannedGetXmlConfigFunction.size();
		
		int scanned_profile_config = scannedProfileXPath.size();
		int loaded_hddb_profile_config = loadedXPath.hddbProfileXPathList.size();
		int found_profile_config = foundProfileXPath.size();
		int notfound_profileconfig = notFoundProfileXPath.size();
		
		db.dumpScanAndSearchDetails(filepath, 
									filename, 
									scanned_webconfig, 
									loaded_hddb_webconfig, 
									found_webconfig, 
									notfound_webconfig, 
									webconfig_in_getxmlconfigfunc, 
									scanned_profile_config, 
									loaded_hddb_profile_config, 
									found_profile_config, 
									notfound_profileconfig);
		
	}
	public void processFile() throws IOException, SQLException {
		String msg = "PROCESSING: "+this.file.getAbsolutePath()+" ...";
		System.out.print(msg);
		
		scanFile();
		searchForXPath();
		dumpXPaths();
		dumpDetails();
		
		msg = "DONE";
		System.out.println(msg);
	}


}
