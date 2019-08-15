package domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import enums.ConfigType;

public class ConfigFinderDBModule {
	private Connection conn;
	
	public ConfigFinderDBModule(String connectionURL) throws SQLException{
		conn = DriverManager.getConnection(connectionURL);
	}
	public boolean isConnected() {
		if(conn != null)
			return true;
		else 
			return false;
	}
	public void closeConnection() throws SQLException {
		if(conn != null)
			conn.close();
	}
	public ArrayList<String> getFoundXPathList() throws SQLException{
		ArrayList<String> xPathList = new ArrayList<String>();
		String sql = "SELECT DISTINCT XPATH FROM XPATHFOUND";
		String xPath = "";
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()) {
			xPath = rs.getString("XPath");
			xPathList.add(xPath.trim());
		}
		return xPathList;
	}
	
	public ArrayList<String> getWebXPathList() throws SQLException{
		ArrayList<String> xPathList = new ArrayList<String>();
		String sql = "SELECT DISTINCT XPATH FROM PROFILEXMLDICT WHERE PRODUCTS = 'WEB'";
		String xPath = "";
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()) {
			xPath = rs.getString("XPath");
			xPathList.add(xPath.trim());
		}
		return xPathList;
	}
	public ArrayList<String> getProfileXPathList() throws SQLException{
		ArrayList<String> xPathList = new ArrayList<String>();
		String sql = "SELECT DISTINCT XPATH FROM PROFILEXMLDICT WHERE XPATH LIKE '/PROFILE%'";
		String xPath = "";
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()) {
			xPath = rs.getString("XPath");
			xPathList.add(xPath.trim());
		}
		return xPathList;
	}
	
	public void foundXpathDump(ArrayList<String> arrayList, String unitName, String webconfig) throws SQLException {
		int size = arrayList.size();
		String tableName = "XPathFound";
		if(size > 0 && size < 1000) {
			xPathDumpLessThan1000Rows(tableName,arrayList, unitName, webconfig);
		}else if(size > 0 && size > 1000){
			xPathDumpMoreThan1000Rows(tableName, arrayList, unitName, webconfig);
		}else {
			System.out.println("NOTHING INSERTED TO DATABASE. NO DATA PASSED IN FOR: "+webconfig);
		}
	}
	private void xPathDumpLessThan1000Rows(String tableName, ArrayList<String> arrayList, String unitName, String webconfig) throws SQLException {
		String sql = "INSERT INTO "+tableName+"(unitName, xpath, configType) VALUES "+ arrayListToValues(arrayList, unitName, webconfig);
		PreparedStatement preps = null;
		preps = conn.prepareStatement(sql);
		preps.execute();
		System.out.println("TOTAL ROWS INSERTED @ ["+tableName+"] FOR "+webconfig.toUpperCase()+": " + preps.getUpdateCount());			
		
	}
	private void xPathDumpMoreThan1000Rows(String tableName, ArrayList<String> arrayList, String unitName, String webconfig) throws SQLException {
		int start = 0; int end = 1000;
		int size = arrayList.size();
		String sql = "";
		while(start < size) {
			if(end >= size) {
				end = size;
			}
			sql = "INSERT INTO "+tableName+"(unitName, xpath, configType) VALUES "+ arrayListToValues(arrayList, unitName, webconfig, start, end);
			PreparedStatement preps = null;
			preps = conn.prepareStatement(sql);
			preps.execute();
			System.out.println("TOTAL ROWS INSERTED @ ["+tableName+"] FOR "+webconfig.toUpperCase()+": " + preps.getUpdateCount());	
			start = end;
			end = end + 1000;
		}	
	}	
	
	public void notFoundXpathDump(ArrayList<String> arrayList, String unitName, String webconfig) throws SQLException {
		int size = arrayList.size();
		String tableName = "XPathNotFound";
		if(size > 0 && size < 1000) {
			xPathDumpLessThan1000Rows(tableName, arrayList, unitName, webconfig);
		}else if(size > 0 && size > 1000){
			xPathDumpMoreThan1000Rows(tableName, arrayList, unitName, webconfig);
		}else {
			System.out.println("NOTHING INSERTED TO DATABASE. NO DATA PASSED IN FOR: "+webconfig);
		}
	}	
	public void xPathFileSearchDetailsDump(String unitName, int scanned_webconfig,
																	int loaded_hddb_webconfig,
																	int found_webconfig,
																	int notfound_webconfig,
																	int webconfig_in_getxmlconfigfunc,
																	int scanned_profile_config,
																	int loaded_hddb_profile_config,
																	int found_profile_config,
																	int notfound_profileconfig) throws SQLException {
		
		String colNames = "unitname,scanned_webconfig,loaded_hddb_webconfig,found_webconfig,notfound_webconfig,webconfig_in_getxmlconfigfunc,scanned_profile_config,loaded_hddb_profile_config,found_profile_config,notfound_profileconfig";
		
		String scanned_webcongStr = Integer.toString(scanned_webconfig); 
		String loaded_hddb_webconfigStr = Integer.toString(loaded_hddb_webconfig);
		String found_webconfigStr = Integer.toString(found_webconfig); 
		String notfound_webconfigStr = Integer.toString(notfound_webconfig);
		String webconfig_in_getxmlconfigfuncStr = Integer.toString(webconfig_in_getxmlconfigfunc);
		
		String scanned_profile_configStr = Integer.toString(scanned_profile_config); 
		String loaded_hddb_profile_configStr = Integer.toString(loaded_hddb_profile_config); 
		String found_profile_configStr = Integer.toString(found_profile_config); 
		String notfound_profileconfigStr = Integer.toString(notfound_profileconfig);
		
		unitName = "\'"+unitName+"\'";
		
		String values = unitName+","+scanned_webcongStr+","+loaded_hddb_webconfigStr+","+found_webconfigStr+","+notfound_webconfigStr+","+webconfig_in_getxmlconfigfuncStr+","+scanned_profile_configStr+","+loaded_hddb_profile_configStr+","+found_profile_configStr+","+notfound_profileconfigStr;
		
		String sql = "INSERT INTO XPathFileSearchDetails("+colNames+") VALUES ("+values+")";                                 
		PreparedStatement preps = null;
		preps = conn.prepareStatement(sql);
		preps.execute();
		System.out.println("TOTAL ROWS INSERTED: " + preps.getUpdateCount());
	}	
	private String arrayListToValues(ArrayList<String> arrayList, String unitName, String configType) {
		String result = "", value = "";
		int size = arrayList.size(); 
		for(int i=0; i< size; i++) {
			value = arrayList.get(i);
			value = value.replaceAll("'", "''");
			if (i == (size - 1))
				result += "(\'"+unitName+"\',\'" + value + "\',\'"+configType+"\')\n";
			else
				result += "(\'"+unitName+"\',\'" + value + "\',\'"+configType+"\'),\n";		
		}
		return result;
	}
	private String arrayListToValues(ArrayList<String> arrayList, String unitName, String configType, int start, int end) {
		String result = "", value = "";
		for(int i=start; i< end; i++) {
			value = arrayList.get(i);
			value = value.replaceAll("'", "''");
			if (i == (end - 1))
				result += "(\'"+unitName+"\',\'" + value + "\',\'"+configType+"\')\n";
			else
				result += "(\'"+unitName+"\',\'" + value + "\',\'"+configType+"\'),\n";		
		}
		return result;
	}
}
