package enums;

public enum XPathType {
	WEBCONFIG, PROFILECONFIG;
	
	@Override
	public String toString() {
		switch(this) {
			case WEBCONFIG: return "CONFIGDATA";
			case PROFILECONFIG: return "PROFILE";
			default: return "INVALID";
		}
	}
}
