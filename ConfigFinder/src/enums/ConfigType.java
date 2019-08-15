package enums;

public enum ConfigType {
	WEBCONFIG, PROFILECONFIG;
	
	@Override
	public String toString() {
		switch(this) {
			case WEBCONFIG: return "CONFIGDATA";
			case PROFILECONFIG: return "PROFILE";
			default: return "Invalid ConfigType Enum";
		}
	}
}
