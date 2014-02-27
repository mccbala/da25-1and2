package da25.process;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import da25.base.NetworkInterface;
import da25.base.ProcessInterface;

/**
 * Main class for clients, holding RMI logic only.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class Client {
	public static Process process;

	public static void main(String[] args) {
		NetworkInterface network;

		try {
			Registry registry = LocateRegistry.getRegistry();
			network = (NetworkInterface) registry.lookup(NetworkInterface.class
					.getCanonicalName());
		} catch (Exception e) {
			System.out.println("Unable to init RMI environment.");
			throw new RuntimeException(e);
		}

		process = new Process();

		try {
			ProcessInterface stub = (ProcessInterface) UnicastRemoteObject
					.exportObject(process, 0);

			process.network = network;
			process.id = network.register(stub);
		} catch (RemoteException e) {
			System.out.println("Unable to register in the network.");
			throw new RuntimeException(e);
		}
		
		System.out.println("Client " + process.id
				+ " is registered.");
		process.start();
	}

}
