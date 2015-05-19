package server;

import java.util.List;

public class Request {

	private List<String> header;
	private String getheaderpart;
	private String hostheaderpart;
	
	public Request(List<String> header)
	{
		this.header = header;
		getheaderpart = "";
		hostheaderpart = "";
		parseHeader();
	}
	
	
	public List<String> getHeader() {
		return header;
	}


	public void setHeader(List<String> header) {
		this.header = header;
	}


	public String getGetheaderpart() {
		return getheaderpart;
	}


	public void setGetheaderpart(String getheaderpart) {
		this.getheaderpart = getheaderpart;
	}


	public String getHostheaderpart() {
		return hostheaderpart;
	}


	public void setHostheaderpart(String hostheaderpart) {
		this.hostheaderpart = hostheaderpart;
	}


	private void parseHeader()
	{
		for (String line : header) {
			String upperLine = line.toUpperCase();

			if (upperLine.startsWith("GET")) {
				getheaderpart = line.split(" ")[1];
			}
			if (upperLine.startsWith("HOST")) {
				hostheaderpart = line.split(" ")[1].split(":")[0];
			}
		}
		
	}
	
	
}
