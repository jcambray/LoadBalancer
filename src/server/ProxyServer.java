package server;

import ini.IniFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProxyServer {

	private Map<String, Map<String, String>> workersMap;
	private ServerSocket proxyServer;
	private String strategy;

	public ServerSocket getProxyServer() {
		return proxyServer;
	}

	public void setProxyServer(ServerSocket proxyServer) {
		this.proxyServer = proxyServer;
	}

	public int getCurrentWorkerIndex() {
		return currentWorkerIndex;
	}

	public void setCurrentWorkerIndex(int currentWorkerIndex) {
		this.currentWorkerIndex = currentWorkerIndex;
	}

	private int currentWorkerIndex = 0;
	private boolean stopListening;
	private final int serverPort;
	final byte[] request = new byte[1024];
	byte[] reply = new byte[4096];

	public ProxyServer(String strategie, int port) {

		this.serverPort = port;
		this.strategy = strategie;

		try {
			init(strategie);
			stopListening = false;
			Map<String, Map<String, String>> worker = workersMap;
			proxyServer = new ServerSocket(serverPort);
			System.out.println("Listening on port " + serverPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init(String strategy) throws IOException {
		IniFile ini = new IniFile("config.ini");
		workersMap = ini.getWorkerMap();

	}

	public void run() {
		try {
			while (!stopListening) {
				handle(proxyServer.accept());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handle(Socket clientSocket) {
		try {
			System.out.println("connected to "+ clientSocket.getInetAddress().getHostName() + " on port "+ clientSocket.getLocalPort());

			

			if (this.strategy.equals("Round-robin")) {
				
				// flux du client
				final InputStream streamFromClient = clientSocket.getInputStream();
				final BufferedReader streamFromClientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				final OutputStream streamToClient = clientSocket.getOutputStream();

				// Connection au serveur
				String workerIP = workersMap.get(String.valueOf(currentWorkerIndex)).get("ip");
				String workerPort = workersMap.get(String.valueOf(currentWorkerIndex)).get("port");
				Socket serverSocket = new Socket(InetAddress.getByName(workerIP), Integer.parseInt(workerPort));
				System.out.println("Connected to the server...");

				// les flux du serveur
				InputStream streamFromServer = serverSocket.getInputStream();
				final OutputStream streamToServer = serverSocket.getOutputStream();
				final PrintWriter streamToServerWriter = new PrintWriter(serverSocket.getOutputStream());
				
				// lecture asynchrone de la requete du client et envoi des
				// données au serveur
				Thread t = new Thread() {
					public void run() {
						try {
							int bytesRead;
							while ((bytesRead = streamFromClient.read(request)) != -1) {
								streamToServer.write(request, 0, bytesRead);
								streamToServer.flush();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				t.start();
				
				// Renvoi des la reponse du serveur au client
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
			}

			else {

				// flux du client
				final InputStream streamFromClient = clientSocket.getInputStream();
				final BufferedReader streamFromClientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				final OutputStream streamToClient = clientSocket.getOutputStream();
				
				String line;
				List<String> headerList = new ArrayList<String>();
				while (!(line = streamFromClientReader.readLine()).equals("")) {
					headerList.add(line);
				}
				Request request = new Request(headerList);

				if (!request.containsCookie) {

					// Connection au serveur
					String workerIP = workersMap.get(String.valueOf(currentWorkerIndex)).get("ip");
					String workerPort = workersMap.get(String.valueOf(currentWorkerIndex)).get("port");
					Socket serverSocket = new Socket(InetAddress.getByName(workerIP), Integer.parseInt(workerPort));
					String headerWithCookie = request.setCookie(currentWorkerIndex);

					incrementWorkerIndex();
					
					// les flux du serveur
					InputStream streamFromServer = serverSocket.getInputStream();
					final OutputStream streamToServer = serverSocket.getOutputStream();
					final PrintWriter streamToServerWriter = new PrintWriter(serverSocket.getOutputStream());

					streamToServerWriter.println(headerWithCookie);
					
					// Renvoi des la reponse du serveur au client
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
					
				} else {
					String cookieLine = request.getCookieHeaderPart();
					String server = cookieLine.split("Set-Cookie: server=")[1];
					int workerIndex = Integer.parseInt(server);
					
					// Connection au serveur
					String workerIP = workersMap.get(String.valueOf(workerIndex)).get("ip");
					String workerPort = workersMap.get(String.valueOf(workerIndex)).get("port");
					Socket serverSocket = new Socket(InetAddress.getByName(workerIP),Integer.parseInt(workerPort));
					
					// les flux du serveur
					InputStream streamFromServer = serverSocket.getInputStream();
					final OutputStream streamToServer = serverSocket.getOutputStream();
					final PrintWriter streamToServerWriter = new PrintWriter(serverSocket.getOutputStream());
					
					// Renvoi des la reponse du serveur au client
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

				}

			}

			if (this.strategy.equals("Round-robin"))
				// incremente le worker pour la requete suivante
				incrementWorkerIndex();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void incrementWorkerIndex() {
		if (currentWorkerIndex == workersMap.keySet().size())
			currentWorkerIndex = 0;
		else
			currentWorkerIndex++;
	}

}
