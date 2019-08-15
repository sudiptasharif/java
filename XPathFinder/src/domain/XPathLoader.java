package domain;

import java.sql.SQLException;
import java.util.ArrayList;

public class XPathLoader {
	public String hddbConnString;
	public String localdbConnString;
	public String retailnetdbConnString;
	
	public ArrayList<String> hddbConfigDataXPathList;
	public ArrayList<String> hddbProfileXPathList;
	public ArrayList<String> existingFoundXPathList;
	
	public ArrayList<String> distinctXPathNotFoundList;
	
	public XPathLoader(String localdbConnString, String hddbConnString, String retailnetdbConnString) {
		this.hddbConnString = hddbConnString;
		this.localdbConnString = localdbConnString;
		this.retailnetdbConnString = retailnetdbConnString;
		
		hddbConfigDataXPathList = null;
		hddbProfileXPathList = null;
		existingFoundXPathList = null;
		distinctXPathNotFoundList = null;
	}
	
	public void loadXPathFromDB() throws SQLException {
		loadConfigXPathFromHDDB();
		loadFoundConfigXPathFromLocalDB();
		loadDistinctXPathNotFoundListFromLocalDB();
	}
	
	public void loadConfigXPathFromHDDB() throws SQLException {
		XPathFinderDBUtility hddb = new XPathFinderDBUtility(hddbConnString);
		hddbConfigDataXPathList = hddb.getConfigDataXPathListFromHDDB();
		hddbProfileXPathList = hddb.getProfileXPathListFromHDDB();
	}
	public void loadFoundConfigXPathFromLocalDB() throws SQLException {
		XPathFinderDBUtility localdb = new XPathFinderDBUtility(localdbConnString);
		existingFoundXPathList = localdb.getFoundXPathListFromLocalDB();
	}
	public void loadDistinctXPathNotFoundListFromLocalDB() throws SQLException {
		XPathFinderDBUtility localdb = new XPathFinderDBUtility(localdbConnString);
		distinctXPathNotFoundList = localdb.getDistinctNotFoundXPathsFromLocalDB();
	}	
	
	public void dumpXPathNotFoundWithHostInfoByXPath() throws SQLException {
		int size = distinctXPathNotFoundList.size();
		String xpath = "";
		ArrayList<XPathNotFoundWithHostInfo> xpathNotFoundWithHostInfoList = null;
		XPathFinderDBUtility rndb = new XPathFinderDBUtility(retailnetdbConnString);
		XPathFinderDBUtility localdb = new XPathFinderDBUtility(localdbConnString);
		
		for(int i=0; i< size; i++) {
			xpath = distinctXPathNotFoundList.get(i);
			System.out.println(Integer.toString(i+1));
			System.out.println(xpath);
			
			xpathNotFoundWithHostInfoList = rndb.getXPathNotFoundWithHostInfoByXPath(xpath);
			localdb.dumpXPathNotFoundWithHostInfo(xpathNotFoundWithHostInfoList);
			xpathNotFoundWithHostInfoList = null;
		}
	}
	
	// For Testing;
	public void printLoadedXPaths() {
		printList(hddbConfigDataXPathList, "ConfigData");
		printList(hddbProfileXPathList, "Profile");
		printList(existingFoundXPathList, "Existing ConfigData & Profile");
	}
	private void printList(ArrayList<String> arrayList, String listName) {
		int size = arrayList.size();
		String msg = "";
		msg = listName.toUpperCase()+" XPATH: TOTAL = "+Integer.toString(size);
		System.out.println(msg);
		for(int i = 0; i < size; i++) {
			System.out.println(arrayList.get(i));
		}
	}
}
