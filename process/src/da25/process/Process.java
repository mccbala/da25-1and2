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

	/**
	 * RMI operations are concluded, starts actual process commands.
	 */
	@Override
	public void start() {
		Message message = new Message(id, Message.BROADCAST, new int[] {0}, "Hello World");
		
		try {
			network.sendMessage(message);
		} catch (RemoteException e) {
			System.out
			.println("Unable to send message [" + message.toString()
					+ "]");
		}
	}

	@Override
	public void processMessage(Message message) throws RemoteException {
		System.out.println("Incoming message: [" + message + "]");
	}
}
