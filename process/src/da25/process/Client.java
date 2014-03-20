package da25.process;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import da25.base.NetworkInterface;
import da25.base.ProcessInterface;
import da25.base.exceptions.LockedException;

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

		switch (args[0]) {
		case "bss":
			process = new BssProcess();
			break;
		case "ag":
			process = new AgProcess();
			break;
		default:
			System.out.println("No assignment specified.");
			return;
		}

		try {
			ProcessInterface stub = (ProcessInterface) UnicastRemoteObject
					.exportObject(process, 0);

			process.network = network;
			try {
				process.id = network.register(stub);
			} catch (LockedException e) {
				System.out.println("Unable to register: network is locked.");
				throw new RuntimeException(e);
			}
		} catch (RemoteException e) {
			System.out.println("Unable to register: RemoteException.");
			throw new RuntimeException(e);
		}

		System.out.println("Process " + process.id
				+ " is registered in the network.");
	}

}
