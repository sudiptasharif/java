package main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import domain.ConfigFinder;
import domain.ConfigFinderDBModule;
import enums.ConfigType;

public class ConfigFinderApp {

	public static void main(String[] args) {
		// All conn string are stripped away for security reasons
		final String HDDB_CONN_STRING = "";
		final String LOCAL_EXPDB_CONN_STRING = "";
		
		final String INPUT_FILE = "input.txt";
		final String VALID_FILE = "valid.txt";
		final String INVALID_FILE = "invalid.txt";
		final String WEB_CONFIG_DUMP_FILE = "web.txt";
		final String PROFILE_CONFIG_DUMP_FILE = "profile.txt";
		final String INPUT_FILE_WITHOUT_COMMENTS_DUMP_FILE = "actualRead.txt";
		
		final boolean LOAD_CONFIGS_FROM_FILE = false;
		final boolean DUMP_CONFIGS_TO_LOCAL_DB = true;
		final boolean DUMP_INPUT_SOURCE_FILE_WITHOUT_COMMENTS = true;
		
		ConfigFinder configFinder = new ConfigFinder();
		try {
			configFinder.scanAndLoadConfigsFromFile(INPUT_FILE);
			
			if(LOAD_CONFIGS_FROM_FILE) {
				configFinder.loadWebConfigsInHDDBFromFile(WEB_CONFIG_DUMP_FILE);
				configFinder.loadProfileConfigsInHDDBFromFile(PROFILE_CONFIG_DUMP_FILE);				
			}else {
				configFinder.setWebConfigListFromHDDB(HDDB_CONN_STRING);
				configFinder.setProfileConfigListFromHDDB(HDDB_CONN_STRING);				
			}
	
			configFinder.search();
			
			configFinder.clearFile(VALID_FILE);
			configFinder.writeConfigsInGetConfigFunction(VALID_FILE);;
			configFinder.writeFoundConfigToFile(VALID_FILE);
			
			configFinder.clearFile(INVALID_FILE);
			configFinder.writeNotFoundConfigToFile(INVALID_FILE);
			
			if(DUMP_CONFIGS_TO_LOCAL_DB) {
				configFinder.dumpFoundConfigsToLocalDB(LOCAL_EXPDB_CONN_STRING);
				configFinder.dumpNotFoundConfigsToLocalDB(LOCAL_EXPDB_CONN_STRING);
				configFinder.dumpScannedConfigDetailsToLocalDB(LOCAL_EXPDB_CONN_STRING);
			}
			if(DUMP_INPUT_SOURCE_FILE_WITHOUT_COMMENTS) {
				configFinder.dumpInputFileWithoutComments(INPUT_FILE_WITHOUT_COMMENTS_DUMP_FILE);	
			}	
		} 
		catch (IOException | SQLException e) {
			System.out.println("EXCEPTION THROWN: "+e.getMessage());
		}
		
/*		ConfigFinderDBModule localdb;
		
		try {
			localdb = new ConfigFinderDBModule(LOCAL_EXPDB_CONN_STRING);
			if(localdb.isConnected()) {
				System.out.println("Wohoo");
			}
			else {
				System.out.println("Boo!");
			}
		} catch (SQLException e) {
			System.out.println("EXCEPTION THROWN: "+e.getMessage());
		}*/
		
		
	}

}
