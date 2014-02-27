package da25.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import da25.base.NetworkInterface;
import da25.base.ProcessInterface;

/**
 * Main class for server, holding RMI logic only.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 *
 */
public class Server {
	public static Network network;

	public static void main(String[] args) {
		System.setProperty("java.rmi.server.codebase", NetworkInterface.class.getProtectionDomain().getCodeSource().getLocation().toString());
		
		network = new Network();
		
		try {
			NetworkInterface stub = (NetworkInterface) UnicastRemoteObject.exportObject(network, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(NetworkInterface.class.getCanonicalName(), stub);
		} catch (RemoteException e) {
			System.out.println("Unable to init RMI environment.");
			throw new RuntimeException(e);
		}
		
		System.out.println("Network is running, waiting for clients.");
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("type \"start\" to begin algorithm");
		boolean start = false;
		while(!start){
			String command = scanner.nextLine();
			if(command.equals("start")){
				start = true;
			}
			else{
				System.out.println("unknown input"); 
			}
		}
		scanner.close();
		network.start();
	}

}
