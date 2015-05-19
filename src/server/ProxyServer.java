package server;

import ini.IniFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class ProxyServer {

	private Map<String, Map<String, String>> workersMap;
	private ServerSocket proxyServer;
	private int currentWorkerIndex = 0;
	private boolean stopListening;
    final byte[] request = new byte[1024];
    byte[] reply = new byte[4096];

	public ProxyServer() {
		try
		{
		init();
		stopListening = false;
		Map<String,Map<String,String>> worker = workersMap;
		
		//int port = Integer.parseInt(worker.get("port"));
		//String ip = workersMap.get(String.valueOf(currentWorkerIndex))
				//.get("ip");
		//proxyServer = new ServerSocket(port, 20, InetAddress.getByName(ip));
		proxyServer = new ServerSocket(4000);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void init() throws IOException {
		IniFile ini = new IniFile("config.ini");
		workersMap = ini.getWorkerMap();
	}

	public void run() {
		try {
			while (!stopListening) {
				handle(proxyServer.accept());
				String ip = workersMap.get(String.valueOf(currentWorkerIndex)).get("ip");
				System.out.println("connected to " + ip + " on port " + proxyServer.getLocalPort());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handle(Socket clientSocket) {
		try
		{
			//flux du client
			final InputStream streamFromClient = clientSocket.getInputStream();
			OutputStream streamToClient = clientSocket.getOutputStream();
			
			//Connection au serveur
			String workerIP = workersMap.get(String.valueOf(currentWorkerIndex)).get("ip");
			String workerPort = workersMap.get(String.valueOf(currentWorkerIndex)).get("port");
			Socket serverSocket = new Socket(InetAddress.getByName(workerIP),Integer.parseInt(workerPort));
			
			//les flux du serveur
			InputStream streamFromServer = serverSocket.getInputStream();
			final OutputStream streamToServer = serverSocket.getOutputStream();
			
			//lecture asynchrone de la requete du client et envoi des données au serveur
			Thread t = new Thread()
			{
				public void run()
				{
					try
					{
					int bytesRead;
					 while ((bytesRead = streamFromClient.read(request)) != -1) {
			                streamToServer.write(request, 0, bytesRead);
			                streamToServer.flush();
					 }
					 streamToServer.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			t.start();
			
			//Renvoi des la reponse du serveur au client
			   int bytesRead;
		        try {
		          while ((bytesRead = streamFromServer.read(reply)) != -1) {
		            streamToClient.write(reply, 0, bytesRead);
		            streamToClient.flush();
		          }
		          streamToClient.close();
		        } catch (IOException e) {
		        }
			serverSocket.close();
			clientSocket.close();
			//incremente le worker pour la requete suivante
			incrementWorkerIndex();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void incrementWorkerIndex()
	{
		if(currentWorkerIndex == workersMap.keySet().size())
			currentWorkerIndex = 0;
		else
			currentWorkerIndex++;
	}
	
	
	
}
