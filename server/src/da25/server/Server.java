package da25.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import da25.base.NetworkInterface;

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
		System.setProperty("java.rmi.server.codebase", NetworkInterface.class
				.getProtectionDomain().getCodeSource().getLocation().toString());

		switch (args[0]) {
		case "bss":
			network = new BssNetwork();
			break;
		case "ag":
			network = new AgNetwork();
			break;
		default:
			System.out.println("No assignment specified.");
		}

		try {
			NetworkInterface stub = (NetworkInterface) UnicastRemoteObject
					.exportObject(network, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(NetworkInterface.class.getCanonicalName(), stub);
		} catch (RemoteException e) {
			System.out.println("Unable to init RMI environment.");
			throw new RuntimeException(e);
		}

		network.start();
	}
}
