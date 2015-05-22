package server;

import java.util.List;

public class Request {

	private List<String> header;
	private String getheaderpart;
	private String hostheaderpart;
	private String cookieHeaderPart;
	public boolean containsCookie;
	
	public Request(List<String> header)
	{
		this.header = header;
		getheaderpart = "";
		hostheaderpart = "";
		cookieHeaderPart = "";
		containsCookie = false;
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
			if(upperLine.startsWith("COOKIE"))
			{
				cookieHeaderPart = line;
				containsCookie = true;
			}
		}
		
	}
	
	
	public String setCookie(int workerIndex)
	{
		StringBuilder builder = new StringBuilder();
		for (String line : header) {
			builder.append(line + "\r\n");
		}
		String cookieline = "Set-Cookie: " + "server=" + workerIndex;
		builder.append(cookieline + "\r\n");
		return builder.toString();
	}


	public String getCookieHeaderPart() {
		return cookieHeaderPart;
	}


	public void setCookieHeaderPart(String cookieHeaderPart) {
		this.cookieHeaderPart = cookieHeaderPart;
	}


	public boolean isContainsCookie() {
		return containsCookie;
	}


	public void setContainsCookie(boolean containsCookie) {
		this.containsCookie = containsCookie;
	}
	
	
}
