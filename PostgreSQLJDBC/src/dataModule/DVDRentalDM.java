package dataModule;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DVDRentalDM {
	private Connection conn;
	
	public void connectToDBDVDRenatal(String user, String password) {
		final String url = "jdbc:postgresql://localhost/dvdrental";
		try {
			conn =  DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			System.out.println(DVDRentalDM.class.getName() + " connectToDBDVDRenatal(): Unable to connect to Database. "+e.getMessage());
			conn = null;
		}
	}
	public boolean isConnectedToDB() {
		if(conn != null) {
			return true;
		}else {
			return false;
		}
	}
	
	public int getActorCount() {
		String SQL = "SELECT count(*) FROM actor";
		int count = 0;
		
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			rs.next();
			count = rs.getInt(1);
		}catch(SQLException e) {
			System.out.println(DVDRentalDM.class.getName() + " getActorCount(): "+e.getMessage());
		}
		return count;
	}
	
	public void getActors() {
		String SQL = "SELECT actor_id, first_name, last_name FROM actor";
		
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			displayActors(rs);
		}catch(SQLException e) {
			System.out.println(DVDRentalDM.class.getName() + " getActors(): " + e.getMessage());
		}
	}
	
	private void displayActors(ResultSet rs) throws SQLException{
		while(rs.next()) {
			System.out.println(rs.getString("actor_id").trim() + "\t"
					+rs.getString("first_name").trim() + "\t" 
					+rs.getString("last_name").trim());
		}
	}
	
	
	public static void main(String args[]) {
		DVDRentalDM dvdRentalDM = new DVDRentalDM();
		dvdRentalDM.connectToDBDVDRenatal("postgres", "postgrespa$$");
		if(dvdRentalDM.isConnectedToDB()) {
			System.out.println("Connected to Database");
			System.out.println("Total Number of Actors in Actor Table: "+dvdRentalDM.getActorCount());
			dvdRentalDM.getActors();
		}else {
			System.out.println("Unable to Connect to Database");
		}
		
		System.out.println("Project Directory: "+System.getProperty("user.dir"));
	}

}
