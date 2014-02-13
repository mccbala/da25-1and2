package da25.process;

import java.rmi.RemoteException;

import da25.base.Message;
import da25.base.NetworkInterface;
import da25.base.ProcessInterface;

/**
 * Concrete implementation of a Process
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class Process implements ProcessInterface {
	public int id;
	public NetworkInterface network;

	@Override
	public void processMessage(Message message) throws RemoteException {
		System.out.println("Incoming message: [" + message + "]");
	}
}
