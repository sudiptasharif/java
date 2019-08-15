package logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {
	private Connection conn;
	private final String url = "jdbc:postgresql://localhost/logger";
	private final String user = "postgres";
	private final String password = "postgrespa$$";
	private String os; 
	private String logFilePath;
	private Logger logger;
	private String userName, hostName, IP, application_dir;
	
	public MyLogger() {
		os = System.getProperty("os.name").toLowerCase();
		conn = connect();
		logFilePath = getUpLocalLogFolder() + getUpLocalLogFileName();
		setUpJavaAPILogger();
		setUpUserNameHostNameApplicationDirAndIP();
	}
	private void setUpJavaAPILogger() {
		String fileName = getUpLocalLogFolder() + getUpLocalLogFileName();
		logger = Logger.getLogger(MyLogger.class.getName());
		LogManager.getLogManager().reset();
		logger.setLevel(Level.ALL);
		
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.SEVERE);
		logger.addHandler(ch);
		
		try {
			//FileHandler fh = new FileHandler(fileName);
			FileHandler fh = new FileHandler(fileName, true); // Pass true as 2nd param to append logs to that file
			fh.setLevel(Level.FINEST);
			fh.setFormatter(new SimpleFormatter()); // if we comment this, the log will show up as xml
			logger.addHandler(fh);
			
			// Test
			logger.log(Level.SEVERE, "Test Severe. Should Showup both in console and in the local file.");
			logger.log(Level.FINE, "Test Fine. Should only show up in local file.");
		}catch(IOException e) {
			logger.log(Level.SEVERE, "IOException. Error message: "+e.getMessage());
		}
	}
	private void setUpUserNameHostNameApplicationDirAndIP(){
		userName = System.getProperty("user.name").toLowerCase();
		application_dir = System.getProperty("user.dir").toLowerCase();
		hostName = "Unknown";
		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    hostName = addr.getHostName();
		    IP = addr.getHostAddress();
		}
		catch (UnknownHostException ex)
		{
			log(Severity.SEVERE, MyLogger.class.getName()+" dbLog()","Hostname can not be resolved");
		}		
	}
	public boolean isConnected() {
		return conn != null;
	}
	
	private Connection connect() {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			log(Severity.SEVERE, MyLogger.class.getName()+" connect()", "Unable to Connect to logger database: "+e.getMessage());
			return null;
		}
	}
	public void log(Severity severity, String event_name, String event_msg) {
		if(isConnected()) {
			dbLog(severity, event_name, event_msg);	
		}else {
			localLog(severity, event_name, event_msg);
		}
	}
	private String getUpLocalLogFolder() {
		if(isOSWindows()) {
			return  "C:\\Logs\\";
		}else if(isOSMac()) {
			String username = System.getProperty("user.name").toLowerCase(); 
			return "/Users/"+username+"/Documents/Logs/";
		}else {
			return "";
		}
		
	}
	public boolean isOSWindows() {
		return (os.indexOf("win") >= 0);
	}

	public boolean isOSMac() {
		return (os.indexOf("mac") >= 0);
	}
	private String getUpLocalLogFileName() {
		String result;
		result = "logs of " + getCurrentDateAsMMDDYYYY() + ".log";
		return result;
	}
	private String getCurrentDateAsMMDDYYYY() {
		String dateFormat = "MM-dd-yyyy";
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		Date date = new Date();	
		return dateFormatter.format(date);
	}
	private void localLog(Severity severity, String event_name, String event_msg) {
		String log = severity.getLevelCode() + "\t" + event_name + "\t" + event_msg;
		try {
			FileWriter fwriter = new FileWriter(logFilePath, true);
			PrintWriter outputFile = new PrintWriter(fwriter);
			outputFile.println(log);
			outputFile.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException for "+logFilePath+". Error: message"+e.getMessage()+". The error message sent to this method: "+log);
		}
	}
	
	private void dbLog(Severity severity, String event_name, String event_msg) {
		String SQL = "INSERT INTO logger(ip, host_name, user_name, application_dir, severity, severity_code, event_name, event_msg)"
				+"VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, IP);
			pstmt.setString(2, hostName);
			pstmt.setString(3, userName);
			pstmt.setString(4, application_dir);
			pstmt.setString(5, severity.toString());
			pstmt.setInt(6, severity.getLevelCode());
			pstmt.setString(7, event_name);
			pstmt.setString(8, event_msg);
			pstmt.executeUpdate();
		}catch (SQLException ex) {
            localLog(severity, event_name, event_msg);
            localLog(Severity.SEVERE, MyLogger.class.getName()+" dbLog()", "SQLException. Error message"+ ex.getMessage());
        }
	}
	
//	public static void main(String args[]) {
//		MyLogger logger = new MyLogger();		
//		logger.log(Severity.SEVERE, "Test Event 2", "Test Message 2");
//	}
}
