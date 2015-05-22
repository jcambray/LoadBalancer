package server;

public class Application {
	
	
public static void main(String[] args) {
	
	ProxyServer serverProxyRoundRobin = new ProxyServer("Round-robin", 4000);
	serverProxyRoundRobin.run();
	
	/*ProxyServer serverProxyStickySession = new ProxyServer("Sticky-session", 4005);
	serverProxyStickySession.run();*/
	
	/*CircularList<Integer> list = new CircularList<Integer>();
	list.add(1);
	list.add(13);
	list.add(18);
	list.add(8);
	list.add(2);
	
	System.out.println("return " + list.pol());
	System.out.println("reste " + list.getSize() + " elements");*/
}
	
	
}
