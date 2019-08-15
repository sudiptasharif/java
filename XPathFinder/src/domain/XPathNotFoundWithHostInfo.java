package domain;

public class XPathNotFoundWithHostInfo {
	private String xpath;
	private String keyname;
	private String hostname;
	private String chaincode;
	private String companyname;
	
	public XPathNotFoundWithHostInfo(String xpath, String keyname, String hostname, String chaincode, String companyname) {
		this.xpath = xpath.trim();
		this.xpath = this.xpath.replaceAll("\'", "\'\'");
		
		this.keyname = keyname.trim();
		this.keyname = this.keyname.replaceAll("\'", "\'\'");
		
		this.hostname = hostname.trim();
		this.hostname = this.hostname.replaceAll("\'", "\'\'");
		
		this.chaincode = chaincode.trim();
		
		this.companyname = companyname.trim();
		this.companyname = this.companyname.replaceAll("\'", "\'\'");
	}
	
	public String toValues() {
		String result = "";
		result += "(" + quoatedStr(xpath) + "," + quoatedStr(keyname) + ",";
		result += quoatedStr(hostname) + "," + quoatedStr(chaincode) +"," + quoatedStr(companyname)+")";
		return result;
	}
	private String quoatedStr(String str) {
		return "\'"+str+"\'";
	}
}
