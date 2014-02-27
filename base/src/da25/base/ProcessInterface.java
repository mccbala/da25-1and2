package da25.base;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface defining a single process, running on a separate JVM and passing
 * messages using RMI.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public interface ProcessInterface extends Remote {
	/**
	 * RMI operations are concluded and the network is now locked, starts actual
	 * process commands.
	 * 
	 * @throws RemoteException
	 */
	public void start() throws RemoteException;

	/**
	 * A message is received from the network. The process may deliver it
	 * immediately to processing functions or put it in a buffer.
	 * 
	 * @param message
	 * @throws RemoteException
	 */
	public void recieveMessage(Message message) throws RemoteException;

	/**
	 * Remote exit command, useful while testing
	 */
	public void exit() throws RemoteException;
}
