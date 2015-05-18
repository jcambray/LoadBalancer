package server;

import java.util.List;

public class Request {

	private List<String> header;
	private String getheaderpart;
	private String hostheaderpart;
	
	public Request(List<String> header)
	{
		this.header = header;
		parseHeader();
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
