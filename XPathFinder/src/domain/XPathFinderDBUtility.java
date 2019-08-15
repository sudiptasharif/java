package domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class XPathFinderDBUtility {
	private Connection conn;

	public XPathFinderDBUtility(String connectionURL) throws SQLException {
		conn = DriverManager.getConnection(connectionURL);
	}

	public boolean isConnected() {
		if (conn != null)
			return true;
		else
			return false;
	}

	public void closeConnection() throws SQLException {
		if (conn != null)
			conn.close();
	}
	public void dumpXPathNotFoundWithHostInfo(ArrayList<XPathNotFoundWithHostInfo> xpathNotFoundWithHostInfoList) throws SQLException {
		int size = xpathNotFoundWithHostInfoList.size();
		String colnames = "xpath,keyname,hostname,chaincode,companyname";
		String values = "";
		for(int i = 0; i<size; i++ ) {
			if (i == (size - 1))
				values +=  xpathNotFoundWithHostInfoList.get(i).toValues()+ "\n";
			else
				values +=  xpathNotFoundWithHostInfoList.get(i).toValues()+ ",\n";
		}
		System.out.println(values);
		String sql = "Insert Into XPathNotFoundWithHostInfo("+colnames+") Values "+values;
		PreparedStatement preps = null;
		preps = conn.prepareStatement(sql);
		preps.execute();
	}
	
	public ArrayList<XPathNotFoundWithHostInfo> getXPathNotFoundWithHostInfoByXPath(String xpath) throws SQLException{		
		ArrayList<XPathNotFoundWithHostInfo> xpathNotFoundWithHostInfoList = new ArrayList<XPathNotFoundWithHostInfo>();
		XPathNotFoundWithHostInfo xpathNotFoundWithHostInfo = null;
		
		int rowCounter = 0;
		
		String keyname = "";
		String hostname = "";
		String chaincode = "";
		String companyname = "";
		
		if(xpath.endsWith("/")) { // this breaks hd and sql search: EXCEPTION THROWN: XQuery [query()]: Syntax error near '<eof>', expected a step expression. example: "/CONFIGDATA/UIPARAMS/ADMINREPORTS/CUSTOMREPORTS/CUSTOMREPORT/QUERY/PARAMSLIST/PARAM/"
			xpathNotFoundWithHostInfo = new XPathNotFoundWithHostInfo(xpath, keyname, hostname, chaincode, companyname);
			xpathNotFoundWithHostInfoList.add(xpathNotFoundWithHostInfo);	
			return xpathNotFoundWithHostInfoList;
		}
		String sql = "Select Cast(ConfigData as xml).query("+quoatedStr(xpath)+") as keyname, hostname, chaincode, companyname From WWWHost Where ConfigData is not null";
		sql += " and cast(cast(configdata as xml).query("+quoatedStr(xpath)+") as varchar(max)) != \'\'";
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
	
		while (rs.next()) {
			keyname = rs.getString("keyname");
			if(keyname == null) {
				keyname = "";
			}
			hostname = rs.getString("hostname");
			if(hostname == null) {
				hostname = "";
			}
			chaincode = rs.getString("chaincode");
			if(chaincode == null) {
				chaincode = "";
			}
			companyname = rs.getString("companyname");
			if(companyname == null) {
				companyname = "";
			}
			xpathNotFoundWithHostInfo = new XPathNotFoundWithHostInfo(xpath, keyname, hostname, chaincode, companyname);
			xpathNotFoundWithHostInfoList.add(xpathNotFoundWithHostInfo);
			xpathNotFoundWithHostInfo = null;
			rowCounter++;
		}
		if(rowCounter == 0) {
			xpathNotFoundWithHostInfo = new XPathNotFoundWithHostInfo(xpath, keyname, hostname, chaincode, companyname);
			xpathNotFoundWithHostInfoList.add(xpathNotFoundWithHostInfo);
			xpathNotFoundWithHostInfo = null;
		}
		return xpathNotFoundWithHostInfoList; 
	}
	
	public ArrayList<String> getDistinctNotFoundXPathsFromLocalDB() throws SQLException {
		ArrayList<String> xPathList = new ArrayList<String>();
		String sql = "Select Distinct xpath From XPathNotFound Where xpath Not In ( Select Distinct xf.xpath From XPathFound xf) And xpath not like '/PROFILE%' Order by xpath";
		String xPath = "";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			xPath = rs.getString("XPath");
			xPathList.add(xPath.trim());
		}
		return xPathList;
	}
	
	public ArrayList<String> getFoundXPathListFromLocalDB() throws SQLException {
		ArrayList<String> xPathList = new ArrayList<String>();
		String sql = "SELECT DISTINCT XPATH FROM XPATHFOUND";
		String xPath = "";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			xPath = rs.getString("XPath");
			xPathList.add(xPath.trim());
		}
		return xPathList;
	}

	public ArrayList<String> getConfigDataXPathListFromHDDB() throws SQLException {
		ArrayList<String> xPathList = new ArrayList<String>();
		String sql = "SELECT DISTINCT XPATH FROM PROFILEXMLDICT WHERE PRODUCTS = 'WEB'";
		String xPath = "";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			xPath = rs.getString("XPath");
			xPathList.add(xPath.trim());
		}
		return xPathList;
	}

	public ArrayList<String> getProfileXPathListFromHDDB() throws SQLException {
		ArrayList<String> xPathList = new ArrayList<String>();
		String sql = "SELECT DISTINCT XPATH FROM PROFILEXMLDICT WHERE XPATH LIKE '/PROFILE%'";
		String xPath = "";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			xPath = rs.getString("XPath");
			xPathList.add(xPath.trim());
		}
		return xPathList;
	}

	private String prepDumpValues(ArrayList<String> arrayList, String configType, String filepath, String filename) {
		String result = "", value = "";
		int size = arrayList.size();
		for (int i = 0; i < size; i++) {
			value = arrayList.get(i);
			value = value.replaceAll("'", "''");
			if (i == (size - 1))
				result += "("+ quoatedStr(filepath) +","+ quoatedStr(filename) +","+ quoatedStr(value) + "," + quoatedStr(configType) + ")\n";
			else
				result += "("+ quoatedStr(filepath) +","+ quoatedStr(filename) +","+ quoatedStr(value) + "," + quoatedStr(configType) + "),\n";
		}
		return result;
	}

	private String prepDumpValues(ArrayList<String> arrayList, String configType, String filepath, String filename, int start,int end) {
		String result = "", value = "";
		for (int i = start; i < end; i++) {
			value = arrayList.get(i);
			value = value.replaceAll("'", "''");
			if (i == (end - 1))
				result += "("+ quoatedStr(filepath) +","+ quoatedStr(filename) +","+ quoatedStr(value) + "," + quoatedStr(configType) + ")\n";
			else
				result += "("+ quoatedStr(filepath) +","+ quoatedStr(filename) +","+ quoatedStr(value) + "," + quoatedStr(configType) + "),\n";
		}
		return result;
	}

	public void dumpFoundXPathToLocalDB(ArrayList<String> arrayList, String configType, String filepath, String filename)throws SQLException {
		int size = arrayList.size();
		String tableName = "XPathFound";
		if (size > 0 && size < 1000) {
			dumpLessThanThousandRows(tableName, arrayList, configType, filepath, filename);
		} else if (size > 0 && size > 1000) {
			dumpMoreThanThousandRows(tableName, arrayList, configType, filepath, filename);
		} else {
			//System.out.println("NOTHING INSERTED TO DATABASE. NO DATA PASSED IN FOR: " + configType);
		}
	}

	public void dumpNotFoundXPathToLocalDB(ArrayList<String> arrayList, String configType, String filepath, String filename)throws SQLException {
		int size = arrayList.size();
		String tableName = "XPathNotFound";
		if (size > 0 && size < 1000) {
			dumpLessThanThousandRows(tableName, arrayList, configType, filepath, filename);
		} else if (size > 0 && size > 1000) {
			dumpMoreThanThousandRows(tableName, arrayList, configType, filepath, filename);
		} else {
			//System.out.println("NOTHING INSERTED TO DATABASE. NO DATA PASSED IN FOR: " + configType);
		}
	}

	private void dumpLessThanThousandRows(String tableName, ArrayList<String> arrayList, String configType, String filepath, String filename) throws SQLException {
		String columns = "filepath,filename,xpath,configtype";
		String sql = "INSERT INTO " + tableName + "("+columns+") VALUES "+ prepDumpValues(arrayList, configType, filepath, filename);
		PreparedStatement preps = null;
		preps = conn.prepareStatement(sql);
		preps.execute();
	}

	private void dumpMoreThanThousandRows(String tableName, ArrayList<String> arrayList, String configType, String filepath, String filename) throws SQLException {
		int start = 0;
		int end = 1000;
		int size = arrayList.size();
		String sql = "";
		String columns = "filepath,filename,xpath,configtype";
		while (start < size) {
			if (end >= size) {
				end = size;
			}
			sql = "INSERT INTO " + tableName + "("+columns+") VALUES " + prepDumpValues(arrayList, configType, filepath, filename, start, end);
			PreparedStatement preps = null;
			preps = conn.prepareStatement(sql);
			preps.execute();
			start = end;
			end = end + 1000;
		}
	}

	public void dumpScanAndSearchDetails(String filepath,String filename, int scanned_webconfig, int loaded_hddb_webconfig,
			int found_webconfig, int notfound_webconfig, int webconfig_in_getxmlconfigfunc, int scanned_profile_config,
			int loaded_hddb_profile_config, int found_profile_config, int notfound_profileconfig) throws SQLException {

		String colNames = "filepath,filename,scanned_webconfig,loaded_hddb_webconfig,found_webconfig,notfound_webconfig,webconfig_in_getxmlconfigfunc,scanned_profile_config,loaded_hddb_profile_config,found_profile_config,notfound_profileconfig";

		String scanned_webcongStr = Integer.toString(scanned_webconfig);
		String loaded_hddb_webconfigStr = Integer.toString(loaded_hddb_webconfig);
		String found_webconfigStr = Integer.toString(found_webconfig);
		String notfound_webconfigStr = Integer.toString(notfound_webconfig);
		String webconfig_in_getxmlconfigfuncStr = Integer.toString(webconfig_in_getxmlconfigfunc);

		String scanned_profile_configStr = Integer.toString(scanned_profile_config);
		String loaded_hddb_profile_configStr = Integer.toString(loaded_hddb_profile_config);
		String found_profile_configStr = Integer.toString(found_profile_config);
		String notfound_profileconfigStr = Integer.toString(notfound_profileconfig);

		filepath = quoatedStr(filepath);
		filename = quoatedStr(filename);

		String values = filepath + "," + filename + "," + scanned_webcongStr + "," + loaded_hddb_webconfigStr + "," + found_webconfigStr
				+ "," + notfound_webconfigStr + "," + webconfig_in_getxmlconfigfuncStr + "," + scanned_profile_configStr
				+ "," + loaded_hddb_profile_configStr + "," + found_profile_configStr + "," + notfound_profileconfigStr;

		String sql = "INSERT INTO XPathFileSearchDetails(" + colNames + ") VALUES (" + values + ")";
		PreparedStatement preps = null;
		preps = conn.prepareStatement(sql);
		preps.execute();
	}
	
	private String quoatedStr(String str) {
		return "\'"+str+"\'";
	}

}
