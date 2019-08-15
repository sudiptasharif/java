package logger;

import java.io.IOException;
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

public class CustomLogger {
	private Connection conn;
	private final String url = "jdbc:postgresql://localhost/logger";
	private final String user = "postgres";
	private final String password = "postgrespa$$";
	private String os; 
	private Logger logger;
	private String userName, hostName, IP, application_dir;	
	private String className;
	
	public CustomLogger(String className) {
		os = System.getProperty("os.name").toLowerCase();
		conn = connect();
		this.className = className;
		setUpLoggerFromJavaAPI();
		setUpUserNameHostNameApplicationDirAndIP();
	}
	private void setUpLoggerFromJavaAPI() {
		String fileName = getUpLocalLogFolder() + getUpLocalLogFileName();
		logger = Logger.getLogger(MyLogger.class.getName());
		LogManager.getLogManager().reset();
		logger.setLevel(Level.ALL);
		
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.SEVERE);
		logger.addHandler(ch);
		
		try {
			FileHandler fh = new FileHandler(fileName, true); // Pass true as 2nd param to append logs to that file
			fh.setLevel(Level.FINEST);
			fh.setFormatter(new SimpleFormatter()); // if we comment this, the log will show up as xml
			logger.addHandler(fh);
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
			log(Level.SEVERE, "setUpUserNameHostNameApplicationDirAndIP","Hostname can not be resolved");
		}		
	}
	private Connection connect() {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			log(Level.SEVERE, "connect", "Unable to Connect to logger database. Error: "+e.getMessage());
			return null;
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
	public boolean isConnected() {
		return conn != null;
	}
	public void log(Level level, String method_name, String log_msg) {
		method_name = formatMethodName(method_name);
		if(isConnected()) {
			dbLog(level, method_name, log_msg);	
		}else {
			logger.log(level, log_msg);// Logger API can display method name, don't need to pass it.
		}
	}
	private String formatMethodName(String method_name) {
		if(!(method_name.contains("(") && method_name.endsWith(")"))){
			method_name += "()";
		}
		return method_name;
	}
	private void dbLog(Level level, String method_name, String log_msg) {
		String SQL = "INSERT INTO logger(ip, host_name, user_name, application_dir, severity_level, severity_level_code, method_name, log_msg, class_name)"
				+"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, IP);
			pstmt.setString(2, hostName);
			pstmt.setString(3, userName);
			pstmt.setString(4, application_dir);
			pstmt.setString(5, level.toString());
			pstmt.setInt(6, level.intValue());
			pstmt.setString(7, method_name);
			pstmt.setString(8, log_msg);
			pstmt.setString(9, className);
			pstmt.executeUpdate();
		}catch (SQLException ex) {
            logger.log(level, "SQLException: "+ex.getMessage());
        }
	}
	public static void main(String args[]) {
		CustomLogger logger = new CustomLogger("logger.CustomLogger");		
		logger.log(Level.SEVERE, "main", "Test Message 2");
	}
	
}
