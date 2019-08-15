package logger;

public enum Severity {
	SEVERE, WARNING, INFO, DEBUG;
	
	public String toString() {
		switch(this) {
		case SEVERE: return "severe";
		case WARNING: return "warning";
		case INFO: return "info";
		case DEBUG: return "debug";
		default: return "unspecified";
		}
	}
	
	public int getLevelCode() {
		switch(this) {
		case SEVERE: return 1;
		case WARNING: return 2;
		case INFO: return 3;
		case DEBUG: return 4;
		default: return -1;
		}	
	}
}
